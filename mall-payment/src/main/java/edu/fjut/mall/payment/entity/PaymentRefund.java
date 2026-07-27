package edu.fjut.mall.payment.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentRefund {
    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal refundAmount;
    private Integer refundStatus;  // 0-待处理 1-已退款 2-已拒绝
    private String reason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
