package edu.fjut.mall.order.controller;

import edu.fjut.mall.common.exception.BusinessException;
import edu.fjut.mall.common.result.Result;
import edu.fjut.mall.common.result.ResultCode;
import edu.fjut.mall.order.dto.AdminDashboardOrderVO;
import edu.fjut.mall.order.dto.AdminDashboardOverviewVO;
import edu.fjut.mall.order.dto.AdminDashboardStockAlertVO;
import edu.fjut.mall.order.dto.AdminDashboardTrendVO;
import edu.fjut.mall.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 管理员运营概况接口，统计范围为整个平台。 */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final OrderService orderService;

    @GetMapping("/overview")
    public Result<AdminDashboardOverviewVO> overview(@RequestHeader("X-User-Role") Integer role) {
        requireAdmin(role);
        return Result.success(orderService.getAdminDashboardOverview());
    }

    @GetMapping("/order-trend")
    public Result<List<AdminDashboardTrendVO>> orderTrend(@RequestParam(required = false) Integer days,
                                                           @RequestHeader("X-User-Role") Integer role) {
        requireAdmin(role);
        return Result.success(orderService.getAdminDashboardOrderTrend(days));
    }

    @GetMapping("/latest-orders")
    public Result<List<AdminDashboardOrderVO>> latestOrders(@RequestParam(required = false) Integer limit,
                                                             @RequestHeader("X-User-Role") Integer role) {
        requireAdmin(role);
        return Result.success(orderService.getAdminDashboardLatestOrders(limit));
    }

    @GetMapping("/stock-alerts")
    public Result<List<AdminDashboardStockAlertVO>> stockAlerts(@RequestParam(required = false) Integer limit,
                                                                 @RequestHeader("X-User-Role") Integer role) {
        requireAdmin(role);
        return Result.success(orderService.getAdminDashboardStockAlerts(limit));
    }

    private void requireAdmin(Integer role) {
        if (role == null || role != 2) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "仅管理员可以访问运营概况");
        }
    }
}
