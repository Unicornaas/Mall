package edu.fjut.mall.order.dto;

import lombok.Data;

/** 管理员运营概况中的全平台库存预警。 */
@Data
public class AdminDashboardStockAlertVO {

    private Long skuId;
    private Long sellerId;
    private String skuCode;
    private String skuName;
    private String productName;
    private Integer availableStock;
    private Integer safetyStock;
}
