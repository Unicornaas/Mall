package edu.fjut.mall.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RefundRequest {
    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    private Long userId;

    /** Kept for old clients; the server always recalculates the amount. */
    private BigDecimal refundAmount;

    private String reason;
    private List<RefundItemRequest> items;
}
