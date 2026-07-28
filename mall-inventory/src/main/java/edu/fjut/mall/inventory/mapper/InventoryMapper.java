package edu.fjut.mall.inventory.mapper;

import edu.fjut.mall.inventory.entity.Inventory;
import edu.fjut.mall.inventory.dto.AdminInventoryPageQuery;
import edu.fjut.mall.inventory.dto.AdminInventoryVO;
import edu.fjut.mall.inventory.dto.SellerInventoryPageQuery;
import edu.fjut.mall.inventory.dto.SellerInventoryVO;
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
    List<AdminInventoryVO> selectAdminPage(@Param("query") AdminInventoryPageQuery query);
    long countAdminPage(@Param("query") AdminInventoryPageQuery query);
    int updateSkuDisplayStock(@Param("skuId") Long skuId, @Param("stock") Integer stock);
    Integer selectSkuDisplayStock(@Param("skuId") Long skuId);
    List<SellerInventoryVO> selectSellerPage(@Param("sellerId") Long sellerId,
                                             @Param("query") SellerInventoryPageQuery query);
    long countSellerPage(@Param("sellerId") Long sellerId,
                         @Param("query") SellerInventoryPageQuery query);
    int countSkuBySeller(@Param("skuId") Long skuId, @Param("sellerId") Long sellerId);
}
