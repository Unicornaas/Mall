package edu.fjut.mall.product.controller;

import edu.fjut.mall.common.exception.BusinessException;
import edu.fjut.mall.common.page.PageResult;
import edu.fjut.mall.common.result.Result;
import edu.fjut.mall.common.result.ResultCode;
import edu.fjut.mall.product.dto.SellerProductPageQuery;
import edu.fjut.mall.product.dto.SkuRequest;
import edu.fjut.mall.product.dto.SpuRequest;
import edu.fjut.mall.product.entity.ProductSku;
import edu.fjut.mall.product.entity.ProductSpu;
import edu.fjut.mall.product.service.ProductSkuService;
import edu.fjut.mall.product.service.ProductSpuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.util.List;

/** 商家商品管理接口，所有操作均以网关注入的当前商家身份校验数据归属。 */
@RestController
@RequestMapping("/api/seller/products")
@RequiredArgsConstructor
public class SellerProductController {

    private final ProductSpuService spuService;
    private final ProductSkuService skuService;

    @GetMapping("/page")
    public Result<PageResult<ProductSpu>> page(@ModelAttribute SellerProductPageQuery query,
                                                 @RequestHeader("X-User-Id") Long sellerId,
                                                 @RequestHeader("X-User-Role") Integer role) {
        requireSeller(role);
        return Result.success(spuService.pageForSeller(query, sellerId));
    }

    @GetMapping("/{spuId}")
    public Result<ProductSpu> detail(@PathVariable Long spuId,
                                     @RequestHeader("X-User-Id") Long sellerId,
                                     @RequestHeader("X-User-Role") Integer role) {
        requireSeller(role);
        return Result.success(spuService.getByIdForSeller(spuId, sellerId));
    }

    @GetMapping("/{spuId}/skus")
    public Result<List<ProductSku>> skus(@PathVariable Long spuId,
                                         @RequestHeader("X-User-Id") Long sellerId,
                                         @RequestHeader("X-User-Role") Integer role) {
        requireSeller(role);
        spuService.getByIdForSeller(spuId, sellerId);
        return Result.success(skuService.listBySpuId(spuId));
    }

    @PostMapping
    public Result<Void> add(@Valid @RequestBody SpuRequest request,
                            @RequestHeader("X-User-Id") Long sellerId,
                            @RequestHeader("X-User-Role") Integer role) {
        requireSeller(role);
        spuService.addForSeller(request, sellerId);
        return Result.success("商品已创建，默认处于下架状态", null);
    }

    @PutMapping("/{spuId}")
    public Result<Void> update(@PathVariable Long spuId,
                               @Valid @RequestBody SpuRequest request,
                               @RequestHeader("X-User-Id") Long sellerId,
                               @RequestHeader("X-User-Role") Integer role) {
        requireSeller(role);
        spuService.updateForSeller(spuId, request, sellerId);
        return Result.success("商品修改成功", null);
    }

    @PutMapping("/{spuId}/status")
    public Result<Void> updateStatus(@PathVariable Long spuId,
                                     @RequestParam Integer status,
                                     @RequestHeader("X-User-Id") Long sellerId,
                                     @RequestHeader("X-User-Role") Integer role) {
        requireSeller(role);
        spuService.updateStatusForSeller(spuId, status, sellerId);
        return Result.success("商品状态修改成功", null);
    }

    @PostMapping("/{spuId}/skus")
    public Result<Void> addSku(@PathVariable Long spuId,
                               @Valid @RequestBody SkuRequest request,
                               @RequestHeader("X-User-Id") Long sellerId,
                               @RequestHeader("X-User-Role") Integer role) {
        requireSeller(role);
        request.setSpuId(spuId);
        skuService.addForSeller(request, sellerId);
        return Result.success("规格新增成功", null);
    }

    @PutMapping("/skus/{skuId}")
    public Result<Void> updateSku(@PathVariable Long skuId,
                                  @Valid @RequestBody SkuRequest request,
                                  @RequestHeader("X-User-Id") Long sellerId,
                                  @RequestHeader("X-User-Role") Integer role) {
        requireSeller(role);
        skuService.updateForSeller(skuId, request, sellerId);
        return Result.success("规格修改成功", null);
    }

    @PutMapping("/skus/{skuId}/status")
    public Result<Void> updateSkuStatus(@PathVariable Long skuId,
                                        @RequestParam Integer status,
                                        @RequestHeader("X-User-Id") Long sellerId,
                                        @RequestHeader("X-User-Role") Integer role) {
        requireSeller(role);
        skuService.updateStatusForSeller(skuId, status, sellerId);
        return Result.success("规格状态修改成功", null);
    }

    private void requireSeller(Integer role) {
        if (role == null || role != 1) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "仅商家可以访问商家商品数据");
        }
    }
}
