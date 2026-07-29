package edu.fjut.mall.payment.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentRefundItem {
    private Long id;
    private Long refundId;
    private Long orderItemId;
    private Long sellerOrderId;
    private Long skuId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal refundAmount;
    private Integer itemStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
