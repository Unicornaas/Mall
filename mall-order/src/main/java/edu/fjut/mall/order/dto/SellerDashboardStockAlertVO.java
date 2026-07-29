package edu.fjut.mall.order.dto;

import lombok.Data;

/** 商家经营概况中的库存预警。 */
@Data
public class SellerDashboardStockAlertVO {

    private Long skuId;
    private String skuCode;
    private String skuName;
    private String productName;
    private Integer availableStock;
    private Integer safetyStock;
}
