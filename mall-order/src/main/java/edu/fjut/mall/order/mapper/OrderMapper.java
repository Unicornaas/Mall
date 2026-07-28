package edu.fjut.mall.order.mapper;

import edu.fjut.mall.order.entity.Order;
import edu.fjut.mall.order.dto.AdminOrderPageQuery;
import edu.fjut.mall.order.dto.SellerOrderPageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OrderMapper {
    int insert(Order order);
    Order selectById(@Param("id") Long id);
    List<Order> selectByUserId(@Param("userId") Long userId);
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    List<Order> selectPageForAdmin(AdminOrderPageQuery query);
    long countForAdmin(AdminOrderPageQuery query);
    int updateShipment(@Param("id") Long id, @Param("shippingCompany") String shippingCompany,
                       @Param("trackingNo") String trackingNo);
    int markReceived(@Param("id") Long id);
    List<Order> selectPageForSeller(@Param("sellerId") Long sellerId,
                                    @Param("query") SellerOrderPageQuery query);
    long countForSeller(@Param("sellerId") Long sellerId,
                        @Param("query") SellerOrderPageQuery query);
}
