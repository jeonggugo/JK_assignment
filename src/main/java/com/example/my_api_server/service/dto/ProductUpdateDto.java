package com.example.my_api_server.service.dto;

//상품 Id, 상품명, 재고수량
public record ProductUpdateDto(
        Long productId,//상품Id
        String changeProductName,//상품명
        Long changeStock//상품재고
) {


}
