package edu.fjut.mall.order.dto;

import lombok.Data;

@Data
public class AdminOrderPageQuery {

    private int pageNum = 1;
    private int pageSize = 20;
    private String orderNo;
    private Long userId;
    private Integer status;

    public int getOffset() {
        return (Math.max(pageNum, 1) - 1) * Math.min(Math.max(pageSize, 1), 100);
    }

    public int getPageNum() {
        return Math.max(pageNum, 1);
    }

    public int getPageSize() {
        return Math.min(Math.max(pageSize, 1), 100);
    }
}
