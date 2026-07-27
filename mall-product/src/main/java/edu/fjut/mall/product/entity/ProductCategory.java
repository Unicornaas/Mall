package edu.fjut.mall.product.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品分类实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductCategory extends edu.fjut.mall.common.entity.BaseEntity {

    /** 父分类ID，0为顶级 */
    private Long parentId;

    /** 分类名称 */
    private String name;

    /** 排序 */
    private Integer sortOrder;

    /** 图标 */
    private String icon;

    /** 状态: 0-禁用 1-启用 */
    private Integer status;
}
