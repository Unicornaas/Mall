package edu.fjut.mall.cart.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CartVO {
    private Long id;
    private Long userId;
    private Long spuId;
    private Long skuId;
    private String productName;
    private String productImage;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalPrice;
    private Integer selected;
    private LocalDateTime createTime;
}
