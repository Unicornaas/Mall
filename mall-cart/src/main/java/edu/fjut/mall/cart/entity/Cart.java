package edu.fjut.mall.cart.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Cart extends edu.fjut.mall.common.entity.BaseEntity {
    private Long userId;
    private Long skuId;
    private Integer quantity;
    private Integer selected;  // 0-未选中 1-已选中
}
