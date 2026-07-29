package edu.fjut.mall.order.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 商家子订单：一笔买家主订单按商家拆分后的履约单元。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SellerOrder extends edu.fjut.mall.common.entity.BaseEntity {

    private Long orderId;
    private Long sellerId;
    private BigDecimal sellerAmount;
    private Integer status; // 0-待支付 1-待发货 2-已发货 3-已完成 4-已取消 5-部分发货
    private String shippingCompany;
    private String trackingNo;
    private LocalDateTime shipTime;
    private LocalDateTime receiveTime;
}
