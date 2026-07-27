package edu.fjut.mall.product.service.impl;

import edu.fjut.mall.common.exception.BusinessException;
import edu.fjut.mall.common.result.ResultCode;
import edu.fjut.mall.common.util.SnowflakeIdGenerator;
import edu.fjut.mall.product.dto.CategoryRequest;
import edu.fjut.mall.product.entity.ProductCategory;
import edu.fjut.mall.product.mapper.ProductCategoryMapper;
import edu.fjut.mall.product.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品分类服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private final ProductCategoryMapper categoryMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    public List<ProductCategory> listAll() {
        return categoryMapper.selectAll();
    }

    @Override
    public List<ProductCategory> listByParentId(Long parentId) {
        return categoryMapper.selectByParentId(parentId);
    }

    @Override
    public ProductCategory getById(Long id) {
        ProductCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "分类不存在");
        }
        return category;
    }

    @Override
    @Transactional
    public void add(CategoryRequest request) {
        ProductCategory category = new ProductCategory();
        category.setId(snowflakeIdGenerator.nextId());
        category.setParentId(request.getParentId());
        category.setName(request.getName());
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        category.setIcon(request.getIcon());
        category.setStatus(1);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());

        categoryMapper.insert(category);
        log.info("新增分类: id={}, name={}", category.getId(), category.getName());
    }

    @Override
    @Transactional
    public void update(Long id, CategoryRequest request) {
        getById(id); // 存在性校验

        ProductCategory category = new ProductCategory();
        category.setId(id);
        category.setName(request.getName());
        category.setSortOrder(request.getSortOrder());
        category.setIcon(request.getIcon());
        category.setUpdateTime(LocalDateTime.now());

        categoryMapper.updateById(category);
        log.info("更新分类: id={}", id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getById(id);
        categoryMapper.deleteById(id);
        log.info("删除分类: id={}", id);
    }
}
