package com.example.my_api_server.service;

import com.example.my_api_server.common.MemberFixture;
import com.example.my_api_server.common.ProductFixture;
import com.example.my_api_server.config.TestContainerConfig;
import com.example.my_api_server.entity.Member;
import com.example.my_api_server.entity.Product;
import com.example.my_api_server.entity.ProductType;
import com.example.my_api_server.repo.MemberDBRepo;
import com.example.my_api_server.repo.OrderProductRepo;
import com.example.my_api_server.repo.OrderRepo;
import com.example.my_api_server.repo.ProductRepo;
import com.example.my_api_server.service.dto.OrderCreateDto;
import com.example.my_api_server.service.dto.OrderResponseDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest//스프링 DI를 통해 빈(Bean)주입 해주는 어노테이션
@Import(TestContainerConfig.class)
@ActiveProfiles("test")//application-test.yml 값을 읽는다!
public class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private MemberDBRepo memberDBRepo;

    @Autowired
    private OrderProductRepo orderProductRepo;

    @BeforeEach
    public void setup() {
        orderProductRepo.deleteAllInBatch();
        productRepo.deleteAllInBatch();
        orderRepo.deleteAllInBatch();
        memberDBRepo.deleteAllInBatch();
    }

    private Member getSavedMember(String password) {
        return memberDBRepo.save(MemberFixture
                .defaultMember()
                .password(password)
                .build()
        );
    }

    private List<Product> getProducts() {
        return productRepo.saveAll(ProductFixture.defaultProducts());
    }

//    private Member getSavedMember() {
//        Member member = Member.builder()
//                .email("test1@gmail.com")
//                .password("1234")
//                .build();
//        Member savedMember = memberDBRepo.save(member);
//        return savedMember;
//    }

    private List<Long> getProductIds(List<Product> products) {
        return products.stream()
                .map(Product::getId)
                .toList();
    }

    @Nested()
    @DisplayName("주문 생성 TC")
    class OrderCreateTest {

        @Test
        @DisplayName("주문 생성 시 DB에 저장되고 주문시간이 Null이 아니다.")
        public void createOrderPersistAndReturn() {
            //given
            List<Long> counts = List.of(1L, 1L);

            Member member = Member.builder()
                    .email("test1@gmail.com")
                    .password("1234")
                    .build();

            Member savedMember = memberDBRepo.save(member);

            Product product1 = Product.builder()
                    .productNumber("TEST1")
                    .productName("티셔츠 1")
                    .productType(ProductType.CLOTHES)
                    .price(1000L)
                    .stock(1L)
                    .build();

            Product product2 = Product.builder()
                    .productNumber("TEST2")
                    .productName("티셔츠 2")
                    .productType(ProductType.CLOTHES)
                    .price(2000L)
                    .stock(2L)
                    .build();

            List<Product> products = productRepo.saveAll(List.of(product1, product2));

            //productId 추출 작업
            List<Long> productIds = products.stream()
                    .map(Product::getId)
                    .toList();

            OrderCreateDto createDto = new OrderCreateDto(savedMember.getId(), productIds, counts);

            //when
            OrderResponseDto retDto = orderService.createOrder(createDto);

            //then
            Assertions.assertThat(retDto.getOrderCompletedTime()).isNotNull();
        }

        @Test
        @DisplayName("주문 생성 시 재고가 정상적으로 차감된다.")
        public void createOrderStockDecreaseSuccess() {
            //given
            //공통적으로 데이터를 사용하지 않는 케이스(재고 관련해서 조금 수정필요하다.)

            List<Long> counts = List.of(1L, 1L);//주문 량
            Member savedMember = getSavedMember("1234");
            List<Product> products = getProducts();//상품 저장(DB에 값이 반영되기 전)

            //productId 추출 작업
            List<Long> productIds = getProductIds(products);

            //DB와 통신하지 않게 proxy처럼 임의로 실행을 시켜줘야함!
            OrderCreateDto createDto = new OrderCreateDto(savedMember.getId(), productIds, counts);
            //when
            OrderResponseDto retDto = orderService.createOrder(createDto);
            //then
            List<Product> resultProducts = productRepo.findAllById(productIds);
            //현재 재고(product 생성 시점) - 주문 재고(요청량) = 최신재고(결과값이 반영된 재고)
            for (int i = 0; i < products.size(); i++) {
                Product beforeProduct = products.get(i);//이전 상품 정보(재고)
                Product nowProduct = resultProducts.get(i);//최신 상품 정보(재고)
                Long orderStock = counts.get(i);//주문 재고

                //티셔츠 A, B, C 각각 3, 2, 1(재고) 1, 2, 1(주문 요청 개수) 남은 재고 = 2, 0, 0
                assertThat(beforeProduct.getStock() - orderStock).isEqualTo(nowProduct.getStock());

            }

        }

        @Test
        @DisplayName("주문 생성시 재고가 부족하면 예외가 정상 동작한다.")
        public void createOrderStockValidation() {
            //given
            //공통적으로 데이터를 사용하지 않는 케이스(재고 관련해서 조금 수정필요하다.)

            List<Long> counts = List.of(10L, 1L);//주문 량
            Member savedMember = getSavedMember("1234");
            List<Product> products = getProducts();//상품 저장(DB에 값이 반영되기 전)

            //productId 추출 작업
            List<Long> productIds = getProductIds(products);

            //DB와 통신하지 않게 proxy처럼 임의로 실행을 시켜줘야함!
            OrderCreateDto createDto = new OrderCreateDto(savedMember.getId(), productIds, counts);
            //when
            //then
            assertThatThrownBy(() -> orderService.createOrder(createDto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("재고가 음수이니 주문 할 수 없습니다!");

        }

    }

    @Nested
    @DisplayName("주문과 연결된 도메인 예외 TC")
    class OrderRelatedExceptionTest {
        @Test
        @DisplayName("주문 시 회원이 존재하지 않으면 예외가 발생한다.")
        public void validateMemberWhenCreateOrder() {
            //given
            List<Long> counts = List.of(1L, 1L);
            Member savedMember = getSavedMember("12354"); //멤버 저장
            List<Product> products = getProducts(); //상품 저장
            List<Long> productIds = getProductIds(products); //productId 추출 작업

            OrderCreateDto createDto = new OrderCreateDto(123422L, productIds, counts);

            //when

            //then
            assertThatThrownBy(() -> orderService.createOrder(createDto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("회원이 존재하지 않습니다.");
        }
    }

}

