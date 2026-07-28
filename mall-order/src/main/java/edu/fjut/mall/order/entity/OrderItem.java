package edu.fjut.mall.order.entity;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItem {
    private Long id;
    private Long orderId;
    /** 下单时固化的商品所属商家ID；0 表示平台自营 */
    private Long sellerId;
    private Long spuId;
    private Long skuId;
    private String productName;
    private String productImage;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalPrice;
}
