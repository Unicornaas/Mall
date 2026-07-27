package edu.fjut.mall.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentVO {
    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal amount;
    private Integer payType;
    private Integer payStatus;
    private String tradeNo;
    private LocalDateTime payTime;
    private LocalDateTime createTime;
}
