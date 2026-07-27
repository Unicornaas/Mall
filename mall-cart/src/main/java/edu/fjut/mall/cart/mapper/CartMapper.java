package edu.fjut.mall.cart.mapper;

import edu.fjut.mall.cart.entity.Cart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CartMapper {
    int insert(Cart cart);
    Cart selectById(@Param("id") Long id);
    Cart selectByUserAndSku(@Param("userId") Long userId, @Param("skuId") Long skuId);
    List<Cart> selectByUserId(@Param("userId") Long userId);
    int updateQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);
    int updateSelected(@Param("id") Long id, @Param("selected") Integer selected);
    int selectAll(@Param("userId") Long userId, @Param("selected") Integer selected);
    int deleteById(@Param("id") Long id);
    int deleteBatch(@Param("ids") List<Long> ids, @Param("userId") Long userId);
    int countByUserId(@Param("userId") Long userId);
    int deleteByUserIdAndIds(@Param("userId") Long userId, @Param("ids") List<Long> ids);
}
