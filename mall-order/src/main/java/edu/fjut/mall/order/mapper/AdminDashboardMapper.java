package edu.fjut.mall.order.mapper;

import edu.fjut.mall.order.dto.AdminDashboardOrderVO;
import edu.fjut.mall.order.dto.AdminDashboardOverviewVO;
import edu.fjut.mall.order.dto.AdminDashboardStockAlertVO;
import edu.fjut.mall.order.dto.AdminDashboardTrendVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AdminDashboardMapper {

    AdminDashboardOverviewVO selectOverview();

    List<AdminDashboardTrendVO> selectOrderTrend(@Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime);

    List<AdminDashboardOrderVO> selectLatestOrders(@Param("limit") int limit);

    List<AdminDashboardStockAlertVO> selectStockAlerts(@Param("limit") int limit);
}
