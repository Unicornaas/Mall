package edu.fjut.mall.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventoryInitRequest {
    @NotNull(message = "SKU ID不能为空")
    private Long skuId;

    @NotNull(message = "库存数量不能为空")
    @Min(value = 0, message = "库存数量不能为负")
    private Integer totalStock;

    private Integer safetyStock;
}
