package edu.fjut.mall.product.controller;

import edu.fjut.mall.common.result.Result;
import edu.fjut.mall.product.dto.SpuRequest;
import edu.fjut.mall.product.entity.ProductSpu;
import edu.fjut.mall.product.service.ProductSpuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品 SPU 接口
 */
@RestController
@RequestMapping("/api/product/spus")
@RequiredArgsConstructor
public class ProductSpuController {

    private final ProductSpuService spuService;

    /** 按分类查询 SPU 列表 */
    @GetMapping("/category/{categoryId}")
    public Result<List<ProductSpu>> listByCategory(@PathVariable Long categoryId) {
        return Result.success(spuService.listByCategoryId(categoryId));
    }

    /** SPU 详情 */
    @GetMapping("/{id}")
    public Result<ProductSpu> getById(@PathVariable Long id) {
        return Result.success(spuService.getById(id));
    }

    /** 新增 SPU */
    @PostMapping
    public Result<Void> add(@Valid @RequestBody SpuRequest request) {
        spuService.add(request);
        return Result.success("新增成功", null);
    }

    /** 修改 SPU */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody SpuRequest request) {
        spuService.update(id, request);
        return Result.success("修改成功", null);
    }

    /** 上下架 */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        spuService.updateStatus(id, status);
        return Result.success("操作成功", null);
    }
}
