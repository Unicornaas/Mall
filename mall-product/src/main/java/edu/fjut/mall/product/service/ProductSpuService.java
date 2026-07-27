package edu.fjut.mall.product.service;

import edu.fjut.mall.product.dto.SpuRequest;
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
    void add(SpuRequest request);

    /** 修改 SPU */
    void update(Long id, SpuRequest request);

    /** 上下架 */
    void updateStatus(Long id, Integer status);
}
