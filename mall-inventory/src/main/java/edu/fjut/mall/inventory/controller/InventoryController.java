package edu.fjut.mall.inventory.controller;

import edu.fjut.mall.common.result.Result;
import edu.fjut.mall.inventory.dto.*;
import edu.fjut.mall.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * INV-01: 查询库存
     */
    @GetMapping("/{skuId}")
    public Result<InventoryVO> query(@PathVariable Long skuId) {
        return Result.success(inventoryService.query(skuId));
    }

    /**
     * INV-02: 批量查询库存
     */
    @PostMapping("/batch")
    public Result<List<InventoryVO>> batchQuery(@Valid @RequestBody InventoryBatchQueryRequest request) {
        return Result.success(inventoryService.batchQuery(request.getSkuIds()));
    }

    /**
     * INV-03: 库存预占（下单时调用）
     */
    @PutMapping("/{skuId}/lock")
    public Result<Void> lock(@PathVariable Long skuId, @Valid @RequestBody InventoryChangeRequest request) {
        inventoryService.lock(skuId, request.getQuantity(), request.getOrderNo());
        return Result.success();
    }

    /**
     * INV-04: 库存扣减（支付成功后调用）
     */
    @PutMapping("/{skuId}/deduct")
    public Result<Void> deduct(@PathVariable Long skuId, @Valid @RequestBody InventoryChangeRequest request) {
        inventoryService.deduct(skuId, request.getQuantity(), request.getOrderNo());
        return Result.success();
    }

    /**
     * INV-05: 库存释放（取消订单/超时调用）
     */
    @PutMapping("/{skuId}/release")
    public Result<Void> release(@PathVariable Long skuId, @Valid @RequestBody InventoryChangeRequest request) {
        inventoryService.release(skuId, request.getQuantity(), request.getOrderNo());
        return Result.success();
    }

    /**
     * INV-06: 入库/增加库存（卖家补货）
     */
    @PutMapping("/{skuId}/add")
    public Result<Void> add(@PathVariable Long skuId, @Valid @RequestBody InventoryChangeRequest request) {
        inventoryService.add(skuId, request.getQuantity());
        return Result.success();
    }

    /**
     * INV-07: 库存初始化（创建SKU时同步）
     */
    @PostMapping("/init")
    public Result<Void> init(@Valid @RequestBody InventoryInitRequest request) {
        inventoryService.init(request);
        return Result.success();
    }

    /**
     * INV-08: 库存日志查询
     */
    @GetMapping("/log/{skuId}")
    public Result<List<InventoryLogVO>> queryLog(@PathVariable Long skuId) {
        return Result.success(inventoryService.queryLog(skuId));
    }
}
