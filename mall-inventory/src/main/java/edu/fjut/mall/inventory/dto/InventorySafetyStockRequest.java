package edu.fjut.mall.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventorySafetyStockRequest {

    @NotNull(message = "安全库存不能为空")
    @Min(value = 0, message = "安全库存不能小于0")
    private Integer safetyStock;
}
