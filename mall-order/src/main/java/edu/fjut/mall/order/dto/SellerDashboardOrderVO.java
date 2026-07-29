package edu.fjut.mall.order.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 商家经营概况中的最近订单。 */
@Data
public class SellerDashboardOrderVO {

    private Long id;
    private String orderNo;
    private BigDecimal sellerAmount;
    private Integer status;
    private String receiverName;
    private LocalDateTime paymentTime;
    private LocalDateTime createTime;
}
