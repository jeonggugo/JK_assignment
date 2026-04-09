package com.example.my_api_server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
@Getter
@Builder
public class Product {//상품

    //상품명, 상품번호(SHIRT-RED-S-001), 상품타입(의류, 음식, 장신구...), 가격, 재고수량

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;//pk

    private String productName;//상품명

    private String productNumber;//상품번호

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductType productType;//상품타입

    private Long price;//가격

    private Long stock;//재고

    @Version
    private Long version;//버전

    //필요한건만 바꿀수있게 Setter처럼 특정 의미있는 메서드로 만들어줍니다. 네이밍은 의미있는 메서드로 만들어줌
    public void changeProductName(String changeProductName) {
        this.productName = changeProductName;
    }

    public void increaseStock(Long addStock) {
        this.stock += addStock;//현재 재고에 더(+) 해줄 재고
    }

    public void decreaseStock(Long subStock) {
        this.stock -= subStock;//현재 재고에 감소(-) 해줄재고
    }

    //구매 가능 여부 확인
    //캡슐화를하게되면 변경지점이 되게 작아진다. 코드의 유지보수(변화)가 적어지게됩니다.
    public void buyProductWithStock(Long orderCount) {
        if (this.getStock() - orderCount < 0) {
            throw new RuntimeException("재고가 음수이니 주문 할 수 없습니다!");
        }
        this.decreaseStock(orderCount);
    }
}

