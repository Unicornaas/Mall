package edu.fjut.mall.product.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品 SPU 实体（标准化产品单元）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductSpu extends edu.fjut.mall.common.entity.BaseEntity {

    /** 分类ID */
    private Long categoryId;

    /** 商品名称 */
    private String name;

    /** 商品描述 */
    private String description;

    /** 品牌 */
    private String brand;

    /** 主图URL */
    private String mainImage;

    /** 图片列表 JSON */
    private String images;

    /** 状态: 0-下架 1-上架 */
    private Integer status;
}
