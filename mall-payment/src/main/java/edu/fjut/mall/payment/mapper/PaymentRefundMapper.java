package edu.fjut.mall.payment.mapper;

import edu.fjut.mall.payment.entity.PaymentRefund;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PaymentRefundMapper {
    int insert(PaymentRefund refund);
    PaymentRefund selectById(@Param("id") Long id);
    int updateStatus(@Param("id") Long id, @Param("refundStatus") Integer refundStatus);
}
