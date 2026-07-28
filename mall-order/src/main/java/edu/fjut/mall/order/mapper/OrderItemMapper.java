package edu.fjut.mall.order.mapper;

import edu.fjut.mall.order.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OrderItemMapper {
    int insertBatch(@Param("items") List<OrderItem> items);
    List<OrderItem> selectByOrderId(@Param("orderId") Long orderId);
    List<OrderItem> selectByOrderIdAndSellerId(@Param("orderId") Long orderId,
                                               @Param("sellerId") Long sellerId);
}
