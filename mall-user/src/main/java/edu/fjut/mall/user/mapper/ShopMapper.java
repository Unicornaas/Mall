package edu.fjut.mall.user.mapper;

import edu.fjut.mall.user.entity.Shop;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ShopMapper {

    Shop selectBySellerId(@Param("sellerId") Long sellerId);

    int insert(Shop shop);
}
