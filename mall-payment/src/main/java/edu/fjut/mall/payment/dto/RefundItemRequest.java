package edu.fjut.mall.payment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RefundItemRequest {
    @NotNull(message = "退款商品不能为空")
    private Long orderItemId;

    @NotNull(message = "退款数量不能为空")
    @Min(value = 1, message = "退款数量必须大于0")
    private Integer quantity;
}
