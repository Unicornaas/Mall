package edu.fjut.mall.order.controller;

import edu.fjut.mall.common.result.Result;
import edu.fjut.mall.order.dto.CreateOrderRequest;
import edu.fjut.mall.order.dto.OrderVO;
import edu.fjut.mall.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Result<OrderVO> create(@Valid @RequestBody CreateOrderRequest request,
                                  @RequestHeader("X-User-Id") Long userId) {
        request.setUserId(userId);
        return Result.success(orderService.create(request));
    }

    @GetMapping("/{id}")
    public Result<OrderVO> getById(@PathVariable Long id,
                                   @RequestHeader("X-User-Id") Long userId) {
        return Result.success(orderService.getById(id, userId));
    }

    @GetMapping("/list")
    public Result<List<OrderVO>> listByUser(@RequestHeader("X-User-Id") Long userId) {
        return Result.success(orderService.listByUserId(userId));
    }

    /** Legacy URL kept for already deployed frontends; the path userId is ignored. */
    @GetMapping("/list/{ignoredUserId}")
    public Result<List<OrderVO>> listByLegacyUser(@PathVariable Long ignoredUserId,
                                                  @RequestHeader("X-User-Id") Long userId) {
        return Result.success(orderService.listByUserId(userId));
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id,
                               @RequestParam(required = false) Long ignoredUserId,
                               @RequestHeader("X-User-Id") Long userId) {
        orderService.cancel(id, userId);
        return Result.success("已取消", null);
    }
}
