package com.example.my_api_server.service;

import com.example.my_api_server.entity.*;
import com.example.my_api_server.repo.MemberDBRepo;
import com.example.my_api_server.repo.OrderRepo;
import com.example.my_api_server.repo.ProductRepo;
import com.example.my_api_server.service.dto.OrderCreateDto;
import com.example.my_api_server.service.dto.OrderResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

//과제 참고해서 과제하기! 배포테스트 오류 Rerun
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepo orderRepo;
    private final MemberDBRepo memberRepo;
    private final ProductRepo productRepo;

    //주문 생성
    @Transactional//악의적인 사용자가 1만개씩 구매한다고 하였을 때, api가 1만번씩 돌면 ...
    public OrderResponseDto createOrder(OrderCreateDto dto, LocalDateTime orderTime) {
        Member member = memberRepo.findById(dto.memberId())
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));

//        //주문 시간에 대해서 값 범위 값을 통해서 로직을 수행한다고 가정
//        if (orderTime.getHour() == 13) {
//            //로직 실행(점심시간 쿠폰 발행)
//            return null;
//        }
        //사전 조건의 데이터는 맞지만 핵심 비지니스 로직에서 우선순위가 낮다.!
        /*이 builder 부분이 리펙토링을 통해 더 짧아질 수 있으니 생각해보고 변경해볼것...! */
//        Order order = Order.builder()
//                .Buyer(member)
//                .orderStatus(OrderStatus.PENDING)
//                .orderTime(orderTime)
//                .build();

        //1주문 여러 상품(n)
        //상품 id들을 통해서 상품들을 조회할거임.
        //쿼리가 N번 돌아감 1번으로 줄일수 있을까요? in쿼리 방식 사용
//        List<Product> products = dto.productId().stream()
//                .map((pId) -> productRepo.findById(pId).orElseThrow())
//                .toList(); //
        Order order = Order.createOrder(member, orderTime);
        List<Product> products = productRepo.findAllById(dto.productId());//IN 쿼리
        List<OrderProduct> orderProducts = IntStream.range(0, dto.count().size())
                .mapToObj(idx -> {
                    //재고차감 현재 음수처리 안됨
                    Product product = products.get(idx);
                    Long orderCount = dto.count().get(idx);
                    //만약에 변경이 된다면 사이 이펙트(어떻게하면 다른 쪽에서 영향을 적게 받을 수 있을까?)
                    //현재 재고에서 주문재고 감했을때 음수이면 <0 예외터트린다!(주문못하게 막는다!)
                    //캡슐화가 안되어있다.
                    if (product.getStock() - orderCount < 0) {
                        throw new RuntimeException("재고가 음수이니 주문 할 수 없습니다!");
                    }

                    //재고 감소
                    product.decreaseStock(dto.count().get(idx));

                    return order.createOrderProduct(orderCount, product);
                })
                .toList();

/*        //Product <-> 주문개수랑 매핑해줘야함 (상품1:10, 상품2:2, 상품3:5개)
        Map<Product, Long> productCountMap = new HashMap<>();
        for (int i = 0; i < dto.count().size(); i++) {
            productCountMap.put(products.get(i), dto.count().get(i));
        }

        List<OrderProduct> orderProducts = products.stream()
                .map(p -> OrderProduct.builder()  // ← 괄호 닫지 말고
                        .order(order)
                        .number(productCountMap.get(p))//product에 맞는 주문개수를 찾는다!
                        .product(p)
                        .build())  // ← 여기서 괄호 닫기
                .toList();*/

        order.addOrderProducts(orderProducts);

        //order save를 하기 전에는 영속화x
        Order savedOrder = orderRepo.save(order);
        //order save를 한 후 에는 영속화(내자식으로 관리하겠다.)
        //Entity ->Dto로 변환
        return OrderResponseDto.of(
                savedOrder.getOrderTime(),
                OrderStatus.COMPLETED,
                true);

        //        업데이트를 한 번에 1억개 -> 10000개씩 조깨서 1000단위(청크) 약간의 텀을 두거나 잘개 쪼개서 update DB입장에서는 부담이 줄어듬
//        return orderResponseDto;

    }

    //비관적 락 예시
    @Transactional//mvcc 스냅샷 범위
    public OrderResponseDto createOrderPLock(OrderCreateDto dto) {
        Member member = memberRepo.findById(dto.memberId()).orElseThrow();
        LocalDateTime orderTime = LocalDateTime.now();

        /*이 builder 부분이 리펙토링을 통해 더 짧아질 수 있으니 생각해보고 변경해볼것...! */
        Order order = Order.builder()
                .Buyer(member)
                .orderStatus(OrderStatus.PENDING)
                .orderTime(orderTime)
                .build();

        //Product id(1,2,3)에 대해서는 for no key update(x-lock) 걸리게된다.
        //그래서 다른 트랜잭션은 이전의 트랙잭션 끝나기를 기다리고 있다. 끝난 후 x -lock 얻어서 연산을 하게 되는 것이다.

        List<Product> products = productRepo.findAllByIdsWithXLock(dto.productId());//FOR no Update lock (베타락)
        List<OrderProduct> orderProducts = IntStream.range(0, dto.count().size())
                .mapToObj(idx -> {
                    //재고차감 현재 음수처리 안됨
                    Product product = products.get(idx);

                    //현재 재고에서 주문재고 감했을때 음수이면 <0 예외터트린다!(주문못하게 막는다!)
                    if (product.getStock() - dto.count().get(idx) < 0) {
                        throw new RuntimeException("재고가 음수이니 주문 할 수 없습니다!");
                    }

                    //재고 감소
                    product.buyProductWithStock(dto.count().get(idx));

//                    product.decreaseStock(dto.count().get(idx));

                    return OrderProduct.builder()
                            .order(order)
                            .number(dto.count().get(idx)) //product에 맞는 주문개수를 찾는다!
                            .product(products.get(idx))
                            .build();
                })
                .toList();

/*        //Product <-> 주문개수랑 매핑해줘야함 (상품1:10, 상품2:2, 상품3:5개)
        Map<Product, Long> productCountMap = new HashMap<>();
        for (int i = 0; i < dto.count().size(); i++) {
            productCountMap.put(products.get(i), dto.count().get(i));
        }

        List<OrderProduct> orderProducts = products.stream()
                .map(p -> OrderProduct.builder()  // ← 괄호 닫지 말고
                        .order(order)
                        .number(productCountMap.get(p))//product에 맞는 주문개수를 찾는다!
                        .product(p)
                        .build())  // ← 여기서 괄호 닫기
                .toList();*/

        order.addOrderProducts(orderProducts);

        //order save를 하기 전에는 영속화x
        Order savedOrder = orderRepo.save(order);
        //order save를 한 후 에는 영속화(내자식으로 관리하겠다.)
        //Entity ->Dto로 변환
        OrderResponseDto orderResponseDto = OrderResponseDto.of(
                savedOrder.getOrderTime(),
                OrderStatus.COMPLETED,
                true);

        //업데이트를 한 번에 1억개 -> 10000개씩 조깨서 1000단위(청크) 약간의 텀을 두거나 잘개 쪼개서 update DB입장에서는 부담이 줄어듬
        return orderResponseDto;

    }

    //낙관락 적용 예시
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Retryable(includes = ObjectOptimisticLockingFailureException.class, maxRetries = 3)//재시도 3번
    public OrderResponseDto createOrderOptLock(OrderCreateDto dto) {
        log.info("@Retryable Test: 반복이 되고 있나?");
        Member member = memberRepo.findById(dto.memberId()).orElseThrow();
        LocalDateTime orderTime = LocalDateTime.now();

        /*이 builder 부분이 리펙토링을 통해 더 짧아질 수 있으니 생각해보고 변경해볼것...! */
        Order order = Order.builder()
                .Buyer(member)
                .orderStatus(OrderStatus.PENDING)
                .orderTime(orderTime)
                .build();


        List<Product> products = productRepo.findAllById(dto.productId());//IN 쿼리
        List<OrderProduct> orderProducts = IntStream.range(0, dto.count().size())
                .mapToObj(idx -> {
                    //재고차감 현재 음수처리 안됨
                    Product product = products.get(idx);

                    //현재 재고에서 주문재고 감했을때 음수이면 <0 예외터트린다!(주문못하게 막는다!)
                    if (product.getStock() - dto.count().get(idx) < 0) {
                        throw new RuntimeException("재고가 음수이니 주문 할 수 없습니다!");
                    }

                    //재고 감소
                    product.decreaseStock(dto.count().get(idx));

                    return OrderProduct.builder()
                            .order(order)
                            .number(dto.count().get(idx)) //product에 맞는 주문개수를 찾는다!
                            .product(products.get(idx))
                            .build();
                })
                .toList();

/*        //Product <-> 주문개수랑 매핑해줘야함 (상품1:10, 상품2:2, 상품3:5개)
        Map<Product, Long> productCountMap = new HashMap<>();
        for (int i = 0; i < dto.count().size(); i++) {
            productCountMap.put(products.get(i), dto.count().get(i));
        }

        List<OrderProduct> orderProducts = products.stream()
                .map(p -> OrderProduct.builder()  // ← 괄호 닫지 말고
                        .order(order)
                        .number(productCountMap.get(p))//product에 맞는 주문개수를 찾는다!
                        .product(p)
                        .build())  // ← 여기서 괄호 닫기
                .toList();*/

        order.addOrderProducts(orderProducts);

        //order save를 하기 전에는 영속화x
        Order savedOrder = orderRepo.save(order);
        //order save를 한 후 에는 영속화(내자식으로 관리하겠다.)
        //Entity ->Dto로 변환
        OrderResponseDto orderResponseDto = OrderResponseDto.of(
                savedOrder.getOrderTime(),
                OrderStatus.COMPLETED,
                true);

        //업데이트를 한 번에 1억개 -> 10000개씩 조깨서 1000단위(청크) 약간의 텀을 두거나 잘개 쪼개서 update DB입장에서는 부담이 줄어듬
        return orderResponseDto;

    }


    //주문 조회 PENDING -> COMPLETED
    //사용자가 최종 주문 확정을 누르면 주문 상태를 확정으로 바꾼다.(주문 수정)
    @Transactional
    public OrderResponseDto updateOrder(Long orderId) {
        Order order = orderRepo.findById(orderId).orElseThrow();
        if (!order.getOrderStatus().equals(OrderStatus.PENDING)) {
            throw new RuntimeException("주문 상태가 PENDING 상태가 아닙니다.");
        }
        order.changeOrderStatus(OrderStatus.COMPLETED);
        return OrderResponseDto.of(
                order.getOrderTime(),
                order.getOrderStatus(),
                true

        );
    }

    @Transactional
    public OrderResponseDto canceledOrder(Long orderId) {
        Order order = orderRepo.findById(orderId).orElseThrow();
        if (!order.getOrderStatus().equals(OrderStatus.PENDING)) {
            throw new RuntimeException("PENDING 상태에서만 취소할 수 있습니다.");
        }
        order.changeOrderStatus(OrderStatus.CANCELED);
        return OrderResponseDto.of(
                order.getOrderTime(),
                order.getOrderStatus(),
                true
        );
    }

    /**
     * JPA는 내부적으로 캐시 메커니즘을 사용함
     * - 내부에 1차캐시, 2차캐시가 존재
     * - 1차 캐시에 Entity들을 내부적으로 영속화(내 자식으로 만들겠다.)시킴
     * - readOnly = true시 내부 하이버네이트 동작원리가 간소화된다.(더티 체킹x)
     */
    @Transactional(readOnly = true)//읽기전용
    public OrderResponseDto findOrder(Long orderId) { //주문 조회
        Order order = orderRepo.findById(orderId).orElseThrow();

        //주문조회->DTO변환
        //Entity -> Dto로 변환
        OrderResponseDto orderResponseDto = OrderResponseDto.of(
                order.getOrderTime(),
                order.getOrderStatus(),
                true);
        return orderResponseDto;


    }
    //1. oderProduct에 저장하는 로직이 없다.
    //2. 재고에 대해서 차감을 해야한다.


}
