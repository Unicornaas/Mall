package edu.fjut.mall.payment.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PaymentRefund {
    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal refundAmount;
    private Integer refundStatus;  // 0-待处理 1-已退款 2-已拒绝
    private String reason;
    private Long processorId;
    private String processRemark;
    private LocalDateTime processTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<PaymentRefundItem> items;
}
