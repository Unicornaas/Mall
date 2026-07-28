package edu.fjut.mall.product.dto;

import lombok.Data;

/** 商家商品分页查询条件。sellerId 仅由服务端从登录身份获取。 */
@Data
public class SellerProductPageQuery {

    private int pageNum = 1;
    private int pageSize = 20;
    private String keyword;
    private Long categoryId;
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
