package edu.fjut.mall.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class CreateOrderRequest {
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    @NotEmpty(message = "订单明细不能为空")
    @Valid
    private List<OrderItemRequest> items;
    @NotNull(message = "收货地址ID不能为空")
    private Long addressId;
    private String remark;

    @Data
    public static class OrderItemRequest {
        @NotNull private Long spuId;
        @NotNull private Long skuId;
        @NotNull @jakarta.validation.constraints.Min(1)
        private Integer quantity;
    }
}
