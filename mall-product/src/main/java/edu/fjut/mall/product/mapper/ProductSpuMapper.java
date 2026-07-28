package edu.fjut.mall.product.mapper;

import edu.fjut.mall.product.entity.ProductSpu;
import edu.fjut.mall.product.dto.AdminProductPageQuery;
import edu.fjut.mall.product.dto.SellerProductPageQuery;
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

    List<ProductSpu> selectPageForAdmin(AdminProductPageQuery query);

    long countForAdmin(AdminProductPageQuery query);

    ProductSpu selectByIdAndSellerId(@Param("id") Long id, @Param("sellerId") Long sellerId);

    List<ProductSpu> selectPageForSeller(@Param("sellerId") Long sellerId,
                                         @Param("query") SellerProductPageQuery query);

    long countForSeller(@Param("sellerId") Long sellerId,
                        @Param("query") SellerProductPageQuery query);
}
