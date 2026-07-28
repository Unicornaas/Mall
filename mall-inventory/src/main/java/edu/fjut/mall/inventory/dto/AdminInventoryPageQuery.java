package edu.fjut.mall.inventory.dto;

import edu.fjut.mall.common.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminInventoryPageQuery extends PageQuery {

    private String keyword;
    private Boolean warningOnly = false;
}
