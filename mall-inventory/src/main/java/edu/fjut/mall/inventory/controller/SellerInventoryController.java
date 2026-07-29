package edu.fjut.mall.inventory.controller;

import edu.fjut.mall.common.exception.BusinessException;
import edu.fjut.mall.common.page.PageResult;
import edu.fjut.mall.common.result.Result;
import edu.fjut.mall.common.result.ResultCode;
import edu.fjut.mall.inventory.dto.InventoryLogVO;
import edu.fjut.mall.inventory.dto.InventoryChangeRequest;
import edu.fjut.mall.inventory.dto.InventorySafetyStockRequest;
import edu.fjut.mall.inventory.dto.SellerInventoryPageQuery;
import edu.fjut.mall.inventory.dto.SellerInventoryVO;
import edu.fjut.mall.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.util.List;

/** 商家库存管理接口，所有写操作均校验当前商家的 SKU 归属。 */
@RestController
@RequestMapping("/api/seller/inventory")
@RequiredArgsConstructor
public class SellerInventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public Result<PageResult<SellerInventoryVO>> page(@ModelAttribute SellerInventoryPageQuery query,
                                                        @RequestHeader("X-User-Id") Long sellerId,
                                                        @RequestHeader("X-User-Role") Integer role) {
        requireSeller(role);
        return Result.success(inventoryService.pageForSeller(query, sellerId));
    }

    @GetMapping("/{skuId}/logs")
    public Result<List<InventoryLogVO>> logs(@PathVariable Long skuId,
                                               @RequestHeader("X-User-Id") Long sellerId,
                                               @RequestHeader("X-User-Role") Integer role) {
        requireSeller(role);
        return Result.success(inventoryService.queryLogForSeller(skuId, sellerId));
    }

    @PutMapping("/{skuId}/add")
    public Result<Void> add(@PathVariable Long skuId,
                            @Valid @RequestBody InventoryChangeRequest request,
                            @RequestHeader("X-User-Id") Long sellerId,
                            @RequestHeader("X-User-Role") Integer role) {
        requireSeller(role);
        inventoryService.addForSeller(skuId, request.getQuantity(), sellerId);
        return Result.success("补货成功", null);
    }

    @PutMapping("/{skuId}/safety-stock")
    public Result<Void> updateSafetyStock(@PathVariable Long skuId,
                                          @Valid @RequestBody InventorySafetyStockRequest request,
                                          @RequestHeader("X-User-Id") Long sellerId,
                                          @RequestHeader("X-User-Role") Integer role) {
        requireSeller(role);
        inventoryService.updateSafetyStockForSeller(skuId, request.getSafetyStock(), sellerId);
        return Result.success("安全库存已更新", null);
    }

    private void requireSeller(Integer role) {
        if (role == null || role != 1) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "仅商家可以访问商家库存数据");
        }
    }
}
