package edu.fjut.mall.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RefundRequest {
    @NotNull(message = "订单号不能为空")
    private String orderNo;

    private Long userId;

    @NotNull(message = "退款金额不能为空")
    private BigDecimal refundAmount;

    private String reason;
}
