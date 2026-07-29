package edu.fjut.mall.order.mapper;

import edu.fjut.mall.order.dto.SellerOrderPageQuery;
import edu.fjut.mall.order.dto.SellerOrderVO;
import edu.fjut.mall.order.entity.SellerOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SellerOrderMapper {

    int insert(SellerOrder sellerOrder);

    SellerOrder selectByIdAndSellerId(@Param("id") Long id, @Param("sellerId") Long sellerId);

    List<SellerOrder> selectByOrderId(@Param("orderId") Long orderId);

    List<SellerOrderVO> selectPageForSeller(@Param("sellerId") Long sellerId,
                                             @Param("query") SellerOrderPageQuery query);

    long countForSeller(@Param("sellerId") Long sellerId,
                        @Param("query") SellerOrderPageQuery query);

    SellerOrderVO selectDetailForSeller(@Param("id") Long id, @Param("sellerId") Long sellerId);

    int updateShipment(@Param("id") Long id, @Param("shippingCompany") String shippingCompany,
                       @Param("trackingNo") String trackingNo);

    int updateShipmentByOrderId(@Param("orderId") Long orderId,
                                @Param("shippingCompany") String shippingCompany,
                                @Param("trackingNo") String trackingNo);

    int updateStatusByOrderId(@Param("orderId") Long orderId, @Param("status") Integer status);

    int markReceivedByOrderId(@Param("orderId") Long orderId);

    int countByOrderIdAndStatus(@Param("orderId") Long orderId, @Param("status") Integer status);
}
