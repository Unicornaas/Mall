package edu.fjut.mall.inventory.controller;

import edu.fjut.mall.common.exception.BusinessException;
import edu.fjut.mall.common.page.PageResult;
import edu.fjut.mall.common.result.Result;
import edu.fjut.mall.common.result.ResultCode;
import edu.fjut.mall.inventory.dto.AdminInventoryPageQuery;
import edu.fjut.mall.inventory.dto.AdminInventoryVO;
import edu.fjut.mall.inventory.dto.InventoryChangeRequest;
import edu.fjut.mall.inventory.dto.InventoryLogVO;
import edu.fjut.mall.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/inventory")
@RequiredArgsConstructor
public class AdminInventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public Result<PageResult<AdminInventoryVO>> page(@ModelAttribute AdminInventoryPageQuery query,
                                                       @RequestHeader("X-User-Role") Integer role) {
        requireAdmin(role);
        return Result.success(inventoryService.pageForAdmin(query));
    }

    @PutMapping("/{skuId}/add")
    public Result<Void> add(@PathVariable Long skuId, @Valid @RequestBody InventoryChangeRequest request,
                            @RequestHeader("X-User-Role") Integer role) {
        requireAdmin(role);
        inventoryService.add(skuId, request.getQuantity());
        return Result.success("补货成功", null);
    }

    @GetMapping("/{skuId}/logs")
    public Result<List<InventoryLogVO>> logs(@PathVariable Long skuId,
                                               @RequestHeader("X-User-Role") Integer role) {
        requireAdmin(role);
        return Result.success(inventoryService.queryLog(skuId));
    }

    private void requireAdmin(Integer role) {
        if (role == null || role != 2) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "仅管理员可以操作库存管理功能");
        }
    }
}
