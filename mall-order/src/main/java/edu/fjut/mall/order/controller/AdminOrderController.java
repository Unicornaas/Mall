package edu.fjut.mall.order.controller;

import edu.fjut.mall.common.exception.BusinessException;
import edu.fjut.mall.common.page.PageResult;
import edu.fjut.mall.common.result.Result;
import edu.fjut.mall.common.result.ResultCode;
import edu.fjut.mall.order.dto.AdminOrderPageQuery;
import edu.fjut.mall.order.dto.OrderVO;
import edu.fjut.mall.order.dto.ShipOrderRequest;
import edu.fjut.mall.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/order")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping("/page")
    public Result<PageResult<OrderVO>> page(@ModelAttribute AdminOrderPageQuery query,
                                            @RequestHeader("X-User-Role") Integer role) {
        requireAdmin(role);
        return Result.success(orderService.pageForAdmin(query));
    }

    @GetMapping("/{id}")
    public Result<OrderVO> detail(@PathVariable Long id, @RequestHeader("X-User-Role") Integer role) {
        requireAdmin(role);
        return Result.success(orderService.getByIdForAdmin(id));
    }

    @PutMapping("/{id}/close")
    public Result<Void> close(@PathVariable Long id, @RequestHeader("X-User-Role") Integer role) {
        requireAdmin(role);
        orderService.closeForAdmin(id);
        return Result.success("订单已关闭", null);
    }

    @PutMapping("/{id}/ship")
    public Result<Void> ship(@PathVariable Long id, @Valid @RequestBody ShipOrderRequest request,
                             @RequestHeader("X-User-Role") Integer role) {
        requireAdmin(role);
        orderService.shipForAdmin(id, request);
        return Result.success("订单已发货", null);
    }

    @PutMapping("/{id}/receive")
    public Result<Void> receive(@PathVariable Long id, @RequestHeader("X-User-Role") Integer role) {
        requireAdmin(role);
        orderService.receiveForAdmin(id);
        return Result.success("订单已确认收货", null);
    }

    private void requireAdmin(Integer role) {
        if (role == null || role != 2) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "仅管理员可以操作订单管理功能");
        }
    }
}
