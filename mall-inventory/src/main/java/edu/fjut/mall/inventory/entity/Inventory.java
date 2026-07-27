package edu.fjut.mall.inventory.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Inventory extends edu.fjut.mall.common.entity.BaseEntity {
    private Long skuId;
    private Integer totalStock;
    private Integer lockedStock;
    private Integer availableStock;
    private Integer safetyStock;
}
