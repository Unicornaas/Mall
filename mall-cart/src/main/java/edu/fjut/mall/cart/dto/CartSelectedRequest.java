package edu.fjut.mall.cart.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartSelectedRequest {
    @NotNull(message = "选中状态不能为空")
    private Integer selected;  // 0-未选中 1-已选中
}
