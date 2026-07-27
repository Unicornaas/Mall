package edu.fjut.mall.product.mapper;

import edu.fjut.mall.product.entity.ProductSpu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品 SPU Mapper
 */
@Mapper
public interface ProductSpuMapper {

    List<ProductSpu> selectByCategoryId(@Param("categoryId") Long categoryId);

    ProductSpu selectById(@Param("id") Long id);

    int insert(ProductSpu spu);

    int updateById(ProductSpu spu);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
