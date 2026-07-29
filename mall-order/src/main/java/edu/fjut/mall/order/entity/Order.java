package edu.fjut.mall.order.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class Order extends edu.fjut.mall.common.entity.BaseEntity {
    private String orderNo;
    private Long userId;
    private BigDecimal totalAmount;
    private Integer status;   // 0-待支付 1-已支付 2-已发货 3-已完成 4-已取消 5-部分发货
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String remark;
    private String shippingCompany;
    private String trackingNo;
    private java.time.LocalDateTime shipTime;
    private java.time.LocalDateTime receiveTime;
}
