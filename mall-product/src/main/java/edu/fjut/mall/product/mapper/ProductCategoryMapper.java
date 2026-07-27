package edu.fjut.mall.product.mapper;

import edu.fjut.mall.product.entity.ProductCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品分类 Mapper
 */
@Mapper
public interface ProductCategoryMapper {

    List<ProductCategory> selectAll();

    List<ProductCategory> selectByParentId(@Param("parentId") Long parentId);

    ProductCategory selectById(@Param("id") Long id);

    int insert(ProductCategory category);

    int updateById(ProductCategory category);

    int deleteById(@Param("id") Long id);
}
