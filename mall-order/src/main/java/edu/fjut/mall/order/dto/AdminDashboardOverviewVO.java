package edu.fjut.mall.order.dto;

import lombok.Data;

import java.math.BigDecimal;

/** 管理员运营概况汇总，统计范围为整个平台。 */
@Data
public class AdminDashboardOverviewVO {

    private long todayOrderCount;
    private BigDecimal todayPaymentAmount = BigDecimal.ZERO;
    private long pendingPaymentOrderCount;
    private long pendingRefundCount;
    private long userCount;
    private long todayNewUserCount;
    private long onSaleProductCount;
    private long warningStockSkuCount;
}
