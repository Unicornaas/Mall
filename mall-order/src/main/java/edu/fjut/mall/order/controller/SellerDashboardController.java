package edu.fjut.mall.order.controller;

import edu.fjut.mall.common.exception.BusinessException;
import edu.fjut.mall.common.result.Result;
import edu.fjut.mall.common.result.ResultCode;
import edu.fjut.mall.order.dto.SellerDashboardOrderVO;
import edu.fjut.mall.order.dto.SellerDashboardOverviewVO;
import edu.fjut.mall.order.dto.SellerDashboardStockAlertVO;
import edu.fjut.mall.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 商家经营概况接口，统计范围严格限定为当前商家。 */
@RestController
@RequestMapping("/api/seller/dashboard")
@RequiredArgsConstructor
public class SellerDashboardController {

    private final OrderService orderService;

    @GetMapping("/overview")
    public Result<SellerDashboardOverviewVO> overview(
            @RequestHeader("X-User-Id") Long sellerId,
            @RequestHeader("X-User-Role") Integer role) {
        requireSeller(role);
        return Result.success(orderService.getDashboardOverview(sellerId));
    }

    @GetMapping("/recent-orders")
    public Result<List<SellerDashboardOrderVO>> recentOrders(
            @RequestParam(required = false) Integer limit,
            @RequestHeader("X-User-Id") Long sellerId,
            @RequestHeader("X-User-Role") Integer role) {
        requireSeller(role);
        return Result.success(orderService.getDashboardRecentOrders(sellerId, limit));
    }

    @GetMapping("/stock-alerts")
    public Result<List<SellerDashboardStockAlertVO>> stockAlerts(
            @RequestParam(required = false) Integer limit,
            @RequestHeader("X-User-Id") Long sellerId,
            @RequestHeader("X-User-Role") Integer role) {
        requireSeller(role);
        return Result.success(orderService.getDashboardStockAlerts(sellerId, limit));
    }

    private void requireSeller(Integer role) {
        if (role == null || role != 1) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "仅商家可以访问经营概况");
        }
    }
}
