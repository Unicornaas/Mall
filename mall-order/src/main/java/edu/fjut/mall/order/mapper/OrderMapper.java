package edu.fjut.mall.order.mapper;

import edu.fjut.mall.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OrderMapper {
    int insert(Order order);
    Order selectById(@Param("id") Long id);
    List<Order> selectByUserId(@Param("userId") Long userId);
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
