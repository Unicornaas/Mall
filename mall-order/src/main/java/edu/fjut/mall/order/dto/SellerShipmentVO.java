package edu.fjut.mall.order.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/** 买家订单详情中的店铺配送信息。 */
@Data
@Builder
public class SellerShipmentVO {

    private Long sellerOrderId;
    private Long sellerId;
    private Integer status;
    private String shippingCompany;
    private String trackingNo;
    private LocalDateTime shipTime;
    private LocalDateTime receiveTime;
}
