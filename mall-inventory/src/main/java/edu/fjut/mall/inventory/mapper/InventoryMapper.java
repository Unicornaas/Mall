package edu.fjut.mall.inventory.mapper;

import edu.fjut.mall.inventory.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InventoryMapper {
    int insert(Inventory inventory);
    Inventory selectBySkuId(@Param("skuId") Long skuId);
    Inventory selectBySkuIdForUpdate(@Param("skuId") Long skuId);
    List<Inventory> selectBySkuIds(@Param("skuIds") List<Long> skuIds);
    int updateStock(@Param("skuId") Long skuId,
                    @Param("totalStock") Integer totalStock,
                    @Param("lockedStock") Integer lockedStock,
                    @Param("availableStock") Integer availableStock);
    int updateSafetyStock(@Param("skuId") Long skuId, @Param("safetyStock") Integer safetyStock);
}
