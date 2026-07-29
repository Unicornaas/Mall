package edu.fjut.mall.order.dto;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商家视角订单：只包含当前商家的订单项和金额。
 * 主订单可能包含其他商家的商品，因此不暴露主订单总金额。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerOrderVO {

    private Long id;
    private Long orderId;
    private String orderNo;
    private BigDecimal sellerAmount;
    private Integer status;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String remark;
    private String shippingCompany;
    private String trackingNo;
    private LocalDateTime paymentTime;
    private LocalDateTime shipTime;
    private LocalDateTime receiveTime;
    private LocalDateTime createTime;
    private List<OrderVO.OrderItemVO> items;
}
