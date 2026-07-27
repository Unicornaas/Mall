package edu.fjut.mall.product.controller;

import edu.fjut.mall.common.result.Result;
import edu.fjut.mall.product.dto.SkuRequest;
import edu.fjut.mall.product.entity.ProductSku;
import edu.fjut.mall.product.service.ProductSkuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品 SKU 接口
 */
@RestController
@RequestMapping("/api/product/skus")
@RequiredArgsConstructor
public class ProductSkuController {

    private final ProductSkuService skuService;

    /** 按 SPU 查询 SKU 列表 */
    @GetMapping("/spu/{spuId}")
    public Result<List<ProductSku>> listBySpu(@PathVariable Long spuId) {
        return Result.success(skuService.listBySpuId(spuId));
    }

    /** SKU 详情 */
    @GetMapping("/{id}")
    public Result<ProductSku> getById(@PathVariable Long id) {
        return Result.success(skuService.getById(id));
    }

    /** 新增 SKU */
    @PostMapping
    public Result<Void> add(@Valid @RequestBody SkuRequest request) {
        skuService.add(request);
        return Result.success("新增成功", null);
    }

    /** 修改 SKU */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody SkuRequest request) {
        skuService.update(id, request);
        return Result.success("修改成功", null);
    }

    /** 删除 SKU */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        skuService.delete(id);
        return Result.success("删除成功", null);
    }
}
