package edu.fjut.mall.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * SKU 请求
 */
@Data
public class SkuRequest {

    @NotNull(message = "所属SPU ID不能为空")
    private Long spuId;

    @NotBlank(message = "SKU名称不能为空")
    private String name;

    private String skuCode;

    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    @NotNull(message = "库存不能为空")
    private Integer stock;

    private String images;

    private String specs;
}
