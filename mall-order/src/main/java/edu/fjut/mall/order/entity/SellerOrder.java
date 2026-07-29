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
    private Integer status;
    private String shippingCompany;
    private String trackingNo;
    private LocalDateTime shipTime;
    private LocalDateTime receiveTime;
}
