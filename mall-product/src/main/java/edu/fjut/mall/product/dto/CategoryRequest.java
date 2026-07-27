package edu.fjut.mall.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 分类请求
 */
@Data
public class CategoryRequest {

    /** 父分类ID，0为顶级 */
    @NotNull(message = "父分类ID不能为空")
    private Long parentId;

    @NotBlank(message = "分类名称不能为空")
    private String name;

    private Integer sortOrder;

    private String icon;
}
