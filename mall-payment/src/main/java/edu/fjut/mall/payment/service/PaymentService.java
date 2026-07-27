package edu.fjut.mall.payment.service;

import edu.fjut.mall.payment.dto.*;

import java.util.List;

public interface PaymentService {
    PaymentVO create(CreatePaymentRequest request);
    PaymentVO pay(String orderNo);
    PaymentVO queryStatus(String orderNo);
    RefundVO refund(RefundRequest request);
    RefundVO processRefund(Long refundId, Integer refundStatus);
    List<PaymentVO> page(Integer pageNum, Integer pageSize);
}
