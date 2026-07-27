package edu.fjut.mall.payment.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class PaymentInfo extends edu.fjut.mall.common.entity.BaseEntity {
    private String orderNo;
    private Long userId;
    private BigDecimal amount;
    private Integer payType;      // 1-支付宝 2-微信
    private Integer payStatus;    // 0-待支付 1-已支付 2-已退款 3-已关闭
    private String tradeNo;       // 第三方交易号
    private LocalDateTime payTime;
}
