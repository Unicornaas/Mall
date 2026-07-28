package edu.fjut.mall.user.dto;

import lombok.Data;

@Data
public class AdminUserPageQuery {

    private int pageNum = 1;
    private int pageSize = 20;
    private String keyword;
    private Integer status;
    private Integer role;

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
