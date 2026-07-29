package edu.fjut.mall.order.dto;

import lombok.Data;

import java.math.BigDecimal;

/** 商家经营概况汇总。订单指标只统计当前商家的子订单。 */
@Data
public class SellerDashboardOverviewVO {

    private long productCount;
    private long onSaleProductCount;
    private long offSaleProductCount;
    private long skuCount;
    private long warningStockCount;
    private long pendingShipmentOrderCount;
    private long todayOrderCount;
    private BigDecimal todaySalesAmount = BigDecimal.ZERO;
}
