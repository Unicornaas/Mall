package edu.fjut.mall.order.dto;

import lombok.Data;

import java.math.BigDecimal;

/** 管理员近期开启支付订单趋势。 */
@Data
public class AdminDashboardTrendVO {

    private String day;
    private long orderCount;
    private BigDecimal amount = BigDecimal.ZERO;
}
