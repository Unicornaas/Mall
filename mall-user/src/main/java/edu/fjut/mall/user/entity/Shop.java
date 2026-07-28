package edu.fjut.mall.user.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商家店铺实体。
 *
 * <p>一个商家账号对应一个店铺；商品通过 sellerId 与商家建立归属，店铺资料用于后续商家端展示和维护。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Shop extends edu.fjut.mall.common.entity.BaseEntity {

    private Long sellerId;
    private String shopName;
    private String logo;
    private String contactName;
    private String contactPhone;
    private String description;

    /** 0-停用，1-正常，2-待审核 */
    private Integer status;
}
