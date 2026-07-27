package edu.fjut.mall.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePaymentRequest {
    @NotNull(message = "订单号不能为空")
    private String orderNo;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "支付金额不能为空")
    private BigDecimal amount;

    private Integer payType;  // 1-支付宝 2-微信，默认1
}
