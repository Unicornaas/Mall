package edu.fjut.mall.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import edu.fjut.mall.payment.entity.PaymentRefundItem;

@Data
@Builder
public class RefundVO {
    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal refundAmount;
    private Integer refundStatus;
    private String reason;
    private Long processorId;
    private String processRemark;
    private LocalDateTime processTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<PaymentRefundItem> items;
}
