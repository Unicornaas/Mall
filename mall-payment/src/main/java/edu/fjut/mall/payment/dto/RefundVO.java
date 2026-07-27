package edu.fjut.mall.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class RefundVO {
    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal refundAmount;
    private Integer refundStatus;
    private String reason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
