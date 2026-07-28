package edu.fjut.mall.payment.dto;

import edu.fjut.mall.common.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminPaymentPageQuery extends PageQuery {

    private String orderNo;
    private Integer payStatus;
}
