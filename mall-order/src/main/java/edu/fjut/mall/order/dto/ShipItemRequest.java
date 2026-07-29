package edu.fjut.mall.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShipItemRequest {
    @NotNull(message = "发货商品不能为空")
    private Long orderItemId;

    @NotNull(message = "发货数量不能为空")
    @Min(value = 1, message = "发货数量必须大于0")
    private Integer quantity;
}
