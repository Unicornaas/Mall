package edu.fjut.mall.product.service;

import edu.fjut.mall.product.dto.SkuRequest;
import edu.fjut.mall.product.entity.ProductSku;

import java.util.List;

/**
 * 商品 SKU 服务
 */
public interface ProductSkuService {

    /** 查询某 SPU 下的 SKU 列表 */
    List<ProductSku> listBySpuId(Long spuId);

    /** 买家端仅查询启用的 SKU */
    List<ProductSku> listEnabledBySpuId(Long spuId);

    /** 获取 SKU 详情 */
    ProductSku getById(Long id);

    /** 买家端查询启用的 SKU 详情 */
    ProductSku getEnabledById(Long id);

    /** 新增 SKU */
    void add(SkuRequest request);

    /** 修改 SKU */
    void update(Long id, SkuRequest request);

    /** 删除 SKU */
    void delete(Long id);

    void addForSeller(SkuRequest request, Long sellerId);

    void updateForSeller(Long id, SkuRequest request, Long sellerId);

    void updateStatusForSeller(Long id, Integer status, Long sellerId);
}
