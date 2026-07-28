package edu.fjut.mall.product.controller;

import edu.fjut.mall.common.exception.BusinessException;
import edu.fjut.mall.common.page.PageResult;
import edu.fjut.mall.common.result.Result;
import edu.fjut.mall.common.result.ResultCode;
import edu.fjut.mall.product.dto.*;
import edu.fjut.mall.product.entity.ProductCategory;
import edu.fjut.mall.product.entity.ProductSku;
import edu.fjut.mall.product.entity.ProductSpu;
import edu.fjut.mall.product.service.ProductCategoryService;
import edu.fjut.mall.product.service.ProductSkuService;
import edu.fjut.mall.product.service.ProductSpuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/product")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductSpuService spuService;
    private final ProductSkuService skuService;
    private final ProductCategoryService categoryService;

    @GetMapping("/spus/page")
    public Result<PageResult<ProductSpu>> page(@ModelAttribute AdminProductPageQuery query,
                                                @RequestHeader("X-User-Role") Integer role) {
        requireAdmin(role);
        return Result.success(spuService.pageForAdmin(query));
    }

    @PostMapping("/spus")
    public Result<Void> addSpu(@Valid @RequestBody SpuRequest request,
                               @RequestHeader("X-User-Id") Long userId,
                               @RequestHeader("X-User-Role") Integer role) {
        requireAdmin(role);
        spuService.add(request, userId);
        return Result.success("商品创建成功", null);
    }

    @PutMapping("/spus/{id}")
    public Result<Void> updateSpu(@PathVariable Long id, @Valid @RequestBody SpuRequest request,
                                  @RequestHeader("X-User-Role") Integer role) {
        requireAdmin(role);
        spuService.update(id, request);
        return Result.success("商品更新成功", null);
    }

    @PutMapping("/spus/{id}/status")
    public Result<Void> updateSpuStatus(@PathVariable Long id, @RequestParam Integer status,
                                        @RequestHeader("X-User-Role") Integer role) {
        requireAdmin(role);
        if (status != 0 && status != 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "商品状态只能为 0 或 1");
        }
        spuService.updateStatus(id, status);
        return Result.success("商品状态已更新", null);
    }

    @GetMapping("/spus/{spuId}/skus")
    public Result<List<ProductSku>> listSku(@PathVariable Long spuId,
                                             @RequestHeader("X-User-Role") Integer role) {
        requireAdmin(role);
        return Result.success(skuService.listBySpuId(spuId));
    }

    @PostMapping("/skus")
    public Result<Void> addSku(@Valid @RequestBody SkuRequest request,
                               @RequestHeader("X-User-Role") Integer role) {
        requireAdmin(role);
        skuService.add(request);
        return Result.success("规格创建成功", null);
    }

    @PutMapping("/skus/{id}")
    public Result<Void> updateSku(@PathVariable Long id, @Valid @RequestBody SkuRequest request,
                                  @RequestHeader("X-User-Role") Integer role) {
        requireAdmin(role);
        skuService.update(id, request);
        return Result.success("规格更新成功", null);
    }

    @GetMapping("/categories")
    public Result<List<ProductCategory>> listCategory(@RequestHeader("X-User-Role") Integer role) {
        requireAdmin(role);
        return Result.success(categoryService.listAll());
    }

    @PostMapping("/categories")
    public Result<Void> addCategory(@Valid @RequestBody CategoryRequest request,
                                    @RequestHeader("X-User-Role") Integer role) {
        requireAdmin(role);
        categoryService.add(request);
        return Result.success("分类创建成功", null);
    }

    @PutMapping("/categories/{id}")
    public Result<Void> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request,
                                       @RequestHeader("X-User-Role") Integer role) {
        requireAdmin(role);
        categoryService.update(id, request);
        return Result.success("分类更新成功", null);
    }

    private void requireAdmin(Integer role) {
        if (role == null || role != 2) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "仅管理员可以操作商品管理功能");
        }
    }
}
