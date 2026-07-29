package edu.fjut.mall.product.mapper;

import edu.fjut.mall.product.entity.ProductSku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品 SKU Mapper
 */
@Mapper
public interface ProductSkuMapper {

    List<ProductSku> selectBySpuId(@Param("spuId") Long spuId);

    List<ProductSku> selectEnabledBySpuId(@Param("spuId") Long spuId);

    ProductSku selectById(@Param("id") Long id);

    ProductSku selectEnabledById(@Param("id") Long id);

    int insert(ProductSku sku);

    int updateById(ProductSku sku);

    int deleteById(@Param("id") Long id);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /** 扣库存（防超卖） */
    int deductStock(@Param("id") Long id, @Param("count") Integer count);
}
