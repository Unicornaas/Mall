package edu.fjut.mall.inventory.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class InventoryBatchQueryRequest {
    @NotNull(message = "SKU ID列表不能为空")
    private List<Long> skuIds;
}
