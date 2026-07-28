package edu.fjut.mall.inventory.dto;

import edu.fjut.mall.common.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 商家库存分页查询条件。商家身份由请求头获取，不接受 sellerId 参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SellerInventoryPageQuery extends PageQuery {

    private String keyword;
    private Boolean warningOnly = false;
}
