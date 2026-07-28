package edu.fjut.mall.inventory.controller;

import edu.fjut.mall.common.exception.BusinessException;
import edu.fjut.mall.common.page.PageResult;
import edu.fjut.mall.common.result.Result;
import edu.fjut.mall.common.result.ResultCode;
import edu.fjut.mall.inventory.dto.InventoryLogVO;
import edu.fjut.mall.inventory.dto.SellerInventoryPageQuery;
import edu.fjut.mall.inventory.dto.SellerInventoryVO;
import edu.fjut.mall.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 商家库存只读接口；补货和安全库存维护在库存管理阶段通过该前缀扩展。 */
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

    private void requireSeller(Integer role) {
        if (role == null || role != 1) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "仅商家可以访问商家库存数据");
        }
    }
}
