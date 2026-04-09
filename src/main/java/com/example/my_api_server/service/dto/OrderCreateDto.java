package com.example.my_api_server.service.dto;

import java.time.LocalDateTime;
import java.util.List;

//주문 시 필요한 데이터(DTO)
//구매자, 주문상태, 주문 시간이 필요함
public record OrderCreateDto(
        Long memberId,//주문자(구매자)
        List<Long> productId, //주문상품 Id
        List<Long> count, //주문수
        LocalDateTime orderTime //주문 시간

) {

    public OrderCreateDto {
        if (orderTime == null) {
            orderTime = LocalDateTime.now();
        }
    }

    public OrderCreateDto(Long memberId, List<Long> productId, List<Long> count) {
        this(memberId, productId, count, LocalDateTime.now());
    }
}