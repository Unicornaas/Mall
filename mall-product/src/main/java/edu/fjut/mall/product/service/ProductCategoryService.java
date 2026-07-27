package edu.fjut.mall.product.service;

import edu.fjut.mall.product.dto.CategoryRequest;
import edu.fjut.mall.product.entity.ProductCategory;

import java.util.List;

/**
 * 商品分类服务
 */
public interface ProductCategoryService {

    /** 查询所有分类（树形） */
    List<ProductCategory> listAll();

    /** 查询子分类 */
    List<ProductCategory> listByParentId(Long parentId);

    /** 获取详情 */
    ProductCategory getById(Long id);

    /** 新增分类 */
    void add(CategoryRequest request);

    /** 修改分类 */
    void update(Long id, CategoryRequest request);

    /** 删除分类 */
    void delete(Long id);
}
