package edu.fjut.mall.product.controller;

import edu.fjut.mall.common.exception.BusinessException;
import edu.fjut.mall.common.page.PageResult;
import edu.fjut.mall.common.result.Result;
import edu.fjut.mall.common.result.ResultCode;
import edu.fjut.mall.product.dto.SellerProductPageQuery;
import edu.fjut.mall.product.entity.ProductSku;
import edu.fjut.mall.product.entity.ProductSpu;
import edu.fjut.mall.product.service.ProductSkuService;
import edu.fjut.mall.product.service.ProductSpuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 商家商品只读接口。写操作在商品管理阶段通过同一前缀的专用接口提供。 */
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

    private void requireSeller(Integer role) {
        if (role == null || role != 1) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "仅商家可以访问商家商品数据");
        }
    }
}
