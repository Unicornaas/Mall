package edu.fjut.mall.cart.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CartBatchDeleteRequest {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotEmpty(message = "购物车项ID列表不能为空")
    private List<Long> ids;
}
