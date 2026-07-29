package edu.fjut.mall.order.mapper;

import edu.fjut.mall.order.dto.SellerDashboardOrderVO;
import edu.fjut.mall.order.dto.SellerDashboardOverviewVO;
import edu.fjut.mall.order.dto.SellerDashboardStockAlertVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SellerDashboardMapper {

    SellerDashboardOverviewVO selectOverview(@Param("sellerId") Long sellerId);

    List<SellerDashboardOrderVO> selectRecentOrders(@Param("sellerId") Long sellerId,
                                                    @Param("limit") int limit);

    List<SellerDashboardStockAlertVO> selectStockAlerts(@Param("sellerId") Long sellerId,
                                                        @Param("limit") int limit);
}
