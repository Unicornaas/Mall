package edu.fjut.mall.product.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品 SKU 实体（库存量单位）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductSku extends edu.fjut.mall.common.entity.BaseEntity {

    /** 所属 SPU ID */
    private Long spuId;

    /** SKU 名称 */
    private String name;

    /** SKU 编码 */
    private String skuCode;

    /** 价格 */
    private BigDecimal price;

    /** 库存 */
    private Integer stock;

    /** SKU 图片 */
    private String images;

    /** 规格参数 JSON */
    private String specs;

    /** 状态: 0-禁用 1-启用 */
    private Integer status;
}
