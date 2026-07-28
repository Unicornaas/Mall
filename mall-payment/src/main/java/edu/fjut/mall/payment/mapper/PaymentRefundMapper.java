package edu.fjut.mall.payment.mapper;

import edu.fjut.mall.payment.entity.PaymentRefund;
import edu.fjut.mall.payment.dto.AdminRefundPageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PaymentRefundMapper {
    int insert(PaymentRefund refund);
    PaymentRefund selectById(@Param("id") Long id);
    PaymentRefund selectLatestByOrderNo(@Param("orderNo") String orderNo);
    int updateStatus(@Param("id") Long id, @Param("refundStatus") Integer refundStatus);
    int updateProcess(@Param("id") Long id, @Param("refundStatus") Integer refundStatus,
                      @Param("processorId") Long processorId, @Param("processRemark") String processRemark);
    int countActiveByOrderNo(@Param("orderNo") String orderNo);
    List<PaymentRefund> selectPageForAdmin(@Param("query") AdminRefundPageQuery query);
    long countForAdmin(@Param("query") AdminRefundPageQuery query);
}
