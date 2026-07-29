package edu.fjut.mall.order.service;

import edu.fjut.mall.order.dto.CreateOrderRequest;
import edu.fjut.mall.order.dto.OrderVO;
import edu.fjut.mall.order.dto.AdminOrderPageQuery;
import edu.fjut.mall.order.dto.ShipOrderRequest;
import edu.fjut.mall.order.dto.SellerOrderPageQuery;
import edu.fjut.mall.order.dto.SellerOrderVO;
import edu.fjut.mall.order.dto.SellerDashboardOrderVO;
import edu.fjut.mall.order.dto.SellerDashboardOverviewVO;
import edu.fjut.mall.order.dto.SellerDashboardStockAlertVO;
import edu.fjut.mall.order.dto.AdminDashboardOrderVO;
import edu.fjut.mall.order.dto.AdminDashboardOverviewVO;
import edu.fjut.mall.order.dto.AdminDashboardStockAlertVO;
import edu.fjut.mall.order.dto.AdminDashboardTrendVO;
import edu.fjut.mall.common.page.PageResult;

import java.util.List;

public interface OrderService {
    OrderVO create(CreateOrderRequest request);
    OrderVO getById(Long id, Long userId);
    List<OrderVO> listByUserId(Long userId);
    void cancel(Long id, Long userId);
    PageResult<OrderVO> pageForAdmin(AdminOrderPageQuery query);
    OrderVO getByIdForAdmin(Long id);
    void closeForAdmin(Long id);
    void shipForAdmin(Long id, ShipOrderRequest request);
    void receiveForAdmin(Long id);
    PageResult<SellerOrderVO> pageForSeller(SellerOrderPageQuery query, Long sellerId);
    SellerOrderVO getByIdForSeller(Long id, Long sellerId);
    void shipForSeller(Long sellerOrderId, ShipOrderRequest request, Long sellerId);
    SellerDashboardOverviewVO getDashboardOverview(Long sellerId);
    List<SellerDashboardOrderVO> getDashboardRecentOrders(Long sellerId, Integer limit);
    List<SellerDashboardStockAlertVO> getDashboardStockAlerts(Long sellerId, Integer limit);
    AdminDashboardOverviewVO getAdminDashboardOverview();
    List<AdminDashboardTrendVO> getAdminDashboardOrderTrend(Integer days);
    List<AdminDashboardOrderVO> getAdminDashboardLatestOrders(Integer limit);
    List<AdminDashboardStockAlertVO> getAdminDashboardStockAlerts(Integer limit);
}
