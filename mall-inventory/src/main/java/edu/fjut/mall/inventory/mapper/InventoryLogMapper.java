package edu.fjut.mall.inventory.mapper;

import edu.fjut.mall.inventory.entity.InventoryLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InventoryLogMapper {
    int insert(InventoryLog log);
    List<InventoryLog> selectBySkuId(@Param("skuId") Long skuId);
}
