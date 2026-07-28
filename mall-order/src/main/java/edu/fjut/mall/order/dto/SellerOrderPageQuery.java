package edu.fjut.mall.order.dto;

import edu.fjut.mall.common.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 商家订单分页查询条件。商家身份由网关注入，禁止请求传入 sellerId。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SellerOrderPageQuery extends PageQuery {

    private String orderNo;
    private Integer status;
}
