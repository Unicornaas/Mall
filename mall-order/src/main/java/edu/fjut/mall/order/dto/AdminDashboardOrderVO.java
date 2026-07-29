package edu.fjut.mall.order.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 管理员运营概况中的最新订单。 */
@Data
public class AdminDashboardOrderVO {

    private Long id;
    private String orderNo;
    private Long userId;
    private String username;
    private BigDecimal totalAmount;
    private Integer status;
    private LocalDateTime payTime;
    private LocalDateTime createTime;
}
