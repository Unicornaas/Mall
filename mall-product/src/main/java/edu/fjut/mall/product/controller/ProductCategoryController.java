package edu.fjut.mall.product.controller;

import edu.fjut.mall.common.result.Result;
import edu.fjut.mall.product.dto.CategoryRequest;
import edu.fjut.mall.product.entity.ProductCategory;
import edu.fjut.mall.product.service.ProductCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类接口
 */
@RestController
@RequestMapping("/api/product/categories")
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryService categoryService;

    /** 查询所有分类 */
    @GetMapping
    public Result<List<ProductCategory>> listAll() {
        return Result.success(categoryService.listAll());
    }

    /** 查询子分类 */
    @GetMapping("/children/{parentId}")
    public Result<List<ProductCategory>> listChildren(@PathVariable Long parentId) {
        return Result.success(categoryService.listByParentId(parentId));
    }

    /** 分类详情 */
    @GetMapping("/{id}")
    public Result<ProductCategory> getById(@PathVariable Long id) {
        return Result.success(categoryService.getById(id));
    }

    /** 新增分类 */
    @PostMapping
    public Result<Void> add(@Valid @RequestBody CategoryRequest request) {
        categoryService.add(request);
        return Result.success("新增成功", null);
    }

    /** 修改分类 */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        categoryService.update(id, request);
        return Result.success("修改成功", null);
    }

    /** 删除分类 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success("删除成功", null);
    }
}
