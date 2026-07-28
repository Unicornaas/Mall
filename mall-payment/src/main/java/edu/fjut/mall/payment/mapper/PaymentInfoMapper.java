package edu.fjut.mall.payment.mapper;

import edu.fjut.mall.payment.entity.PaymentInfo;
import edu.fjut.mall.payment.dto.AdminPaymentPageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PaymentInfoMapper {
    int insert(PaymentInfo paymentInfo);
    PaymentInfo selectById(@Param("id") Long id);
    PaymentInfo selectByOrderNo(@Param("orderNo") String orderNo);
    List<PaymentInfo> selectAll(@Param("pageNum") Integer pageNum, @Param("pageSize") Integer pageSize);
    int updateStatus(@Param("id") Long id, @Param("payStatus") Integer payStatus);
    int updateTradeNo(@Param("id") Long id, @Param("tradeNo") String tradeNo);
    int countAll();
    List<PaymentInfo> selectPageForAdmin(@Param("query") AdminPaymentPageQuery query);
    long countForAdmin(@Param("query") AdminPaymentPageQuery query);
}
