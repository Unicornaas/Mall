package edu.fjut.mall.payment.service;

import edu.fjut.mall.payment.dto.*;
import edu.fjut.mall.common.page.PageResult;

import java.util.List;

public interface PaymentService {
    PaymentVO create(CreatePaymentRequest request);
    PaymentVO pay(String orderNo, Long userId);
    PaymentVO queryStatus(String orderNo, Long userId);
    RefundVO refund(RefundRequest request);
    RefundVO queryRefundStatus(String orderNo, Long userId);
    List<RefundVO> listRefunds(String orderNo, Long userId);
    RefundVO processRefund(Long refundId, Integer refundStatus);
    List<PaymentVO> page(Integer pageNum, Integer pageSize);
    RefundVO processRefund(Long refundId, Integer refundStatus, String processRemark, Long processorId);
    PageResult<PaymentVO> pageForAdmin(AdminPaymentPageQuery query);
    PageResult<RefundVO> refundPageForAdmin(AdminRefundPageQuery query);
}
