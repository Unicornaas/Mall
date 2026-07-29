package edu.fjut.mall.product.service;

import edu.fjut.mall.product.dto.SpuRequest;
import edu.fjut.mall.product.dto.AdminProductPageQuery;
import edu.fjut.mall.product.dto.SellerProductPageQuery;
import edu.fjut.mall.common.page.PageResult;
import edu.fjut.mall.product.entity.ProductSpu;

import java.util.List;

/**
 * 商品 SPU 服务
 */
public interface ProductSpuService {

    /** 查询某分类下的 SPU 列表 */
    List<ProductSpu> listByCategoryId(Long categoryId);

    /** 获取 SPU 详情 */
    ProductSpu getById(Long id);

    /** 新增 SPU */
    void add(SpuRequest request, Long sellerId);

    /** 修改 SPU */
    void update(Long id, SpuRequest request);

    /** 上下架 */
    void updateStatus(Long id, Integer status);

    PageResult<ProductSpu> pageForAdmin(AdminProductPageQuery query);

    PageResult<ProductSpu> pageForSeller(SellerProductPageQuery query, Long sellerId);

    ProductSpu getByIdForSeller(Long id, Long sellerId);

    void addForSeller(SpuRequest request, Long sellerId);

    void updateForSeller(Long id, SpuRequest request, Long sellerId);

    void updateStatusForSeller(Long id, Integer status, Long sellerId);
}
