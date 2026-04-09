package com.example.my_api_server.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "OrderProducts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OrderProduct {//주문한 상품
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //주문(1) <-> 주문상품(N,여러 상품) <-> 상품(1)
    //상품, 주문, 주문 수량
    @ManyToOne(fetch = FetchType.LAZY)//FK
    private Product product;//상품

    @ManyToOne(fetch = FetchType.LAZY)//FK
    private Order order;//주문

    private Long number;//수량
}
