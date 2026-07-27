package edu.fjut.mall.common.page;

import lombok.Data;

/**
 * 通用分页请求
 */
@Data
public class PageQuery {

    /** 当前页码（从1开始） */
    private int pageNum = 1;

    /** 每页条数 */
    private int pageSize = 20;

    /** 排序列 */
    private String sortField;

    /** 排序方向: asc / desc */
    private String sortOrder = "desc";

    /**
     * 获取偏移量（MyBatis 用）
     */
    public int getOffset() {
        return (pageNum - 1) * pageSize;
    }
}
