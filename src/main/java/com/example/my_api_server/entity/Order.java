package com.example.my_api_server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor//기본생성자 생성
@AllArgsConstructor//매개변수를 다 받는 생성자 생성
@Table(name = "orders")
@Getter
@Builder
public class Order {

    //Order가 저장되면 OrderProduct도 같이 저장된다.(생명주기를 동일하게 하겠다. Cascade)
    //부모가 삭제하면 삭제할거냐? 아니면 부모가 수정하면 수정할꺼냐 이게 orphanRemoval의 역할
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    List<OrderProduct> orderProducts = new ArrayList<>();
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;//pk
    //주문자(구매자), 주문 상태, 주문 시간
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member Buyer;//구매자
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;//주문 상태


    //상품(N) 바지, 신발, 모자 : 주문(1) DB 1:N 관계를 나타내야함.!
    //주문(1) <-> 주문상품(N,여러 상품) <-> 상품(1)
    @Column(nullable = false)
    private LocalDateTime orderTime;//주문 시간

    public static Order createOrder(Member member, Clock clock) {
        Order order = Order.builder()
                .Buyer(member)
                .orderStatus(OrderStatus.PENDING)
                .orderTime(LocalDateTime.now(clock))
                .build();


        return order;
    }

    //루트 엔티티(에그리거트 루트) 내부 응집도 상승
    public OrderProduct createOrderProduct(Long orderCount, Product product) {
        return OrderProduct.builder()
                .order(this)
                .number(orderCount) //product에 맞는 주문개수를 찾는다!
                .product(product)
                .build();

    }

    public void addOrderProducts(List<OrderProduct> orderProduct) {
        this.orderProducts = orderProduct;
    }

    public void changeOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
}
