package edu.fjut.mall.inventory.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InventoryVO {
    private Long id;
    private Long skuId;
    private Integer totalStock;
    private Integer lockedStock;
    private Integer availableStock;
    private Integer safetyStock;
}
