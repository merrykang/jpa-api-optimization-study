package jpabook.jpashop.repository.order.query;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class OrderItemQueryDto {
    @JsonIgnore  // 중간에 뿌릴 DTO니까 이 어노테이션 사용 
    private Long orderId;
    private String itemName;
    private int orderPrice;
    private int count;

    public OrderItemQueryDto(Long orderId, String itemName, int orderPrice, int count) {
        this.orderId = orderId;
        this.itemName = itemName;
        this.orderPrice = orderPrice;
        this.count = count;
    }

//    @JsonIgnore
//    private Long orderId; //주문번호
//    private String itemName;//상품 명
//    private int orderPrice; //주문 가격
//    private int count;      //주문 수량
//
//    public OrderItemQueryDto(Long orderId, String itemName, int orderPrice, int count) {
//        this.orderId = orderId;
//        this.itemName = itemName;
//        this.orderPrice = orderPrice;
//        this.count = count;
//    }
}
