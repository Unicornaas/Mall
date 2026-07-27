package edu.fjut.mall.payment.controller;

import edu.fjut.mall.common.result.Result;
import edu.fjut.mall.payment.dto.*;
import edu.fjut.mall.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * PAY-01: 创建支付单
     */
    @PostMapping("/create")
    public Result<PaymentVO> create(@Valid @RequestBody CreatePaymentRequest request) {
        return Result.success(paymentService.create(request));
    }

    /**
     * PAY-02: 模拟支付
     */
    @PutMapping("/pay/{orderNo}")
    public Result<PaymentVO> pay(@PathVariable String orderNo) {
        return Result.success(paymentService.pay(orderNo));
    }

    /**
     * PAY-03: 支付状态查询
     */
    @GetMapping("/status/{orderNo}")
    public Result<PaymentVO> queryStatus(@PathVariable String orderNo) {
        return Result.success(paymentService.queryStatus(orderNo));
    }

    /**
     * PAY-04: 申请退款
     */
    @PostMapping("/refund")
    public Result<RefundVO> refund(@Valid @RequestBody RefundRequest request) {
        return Result.success(paymentService.refund(request));
    }

    /**
     * PAY-05: 退款处理
     */
    @PutMapping("/refund/{refundId}/process")
    public Result<RefundVO> processRefund(@PathVariable Long refundId, @RequestParam Integer refundStatus) {
        return Result.success(paymentService.processRefund(refundId, refundStatus));
    }

    /**
     * PAY-06: 支付记录列表
     */
    @GetMapping("/page")
    public Result<List<PaymentVO>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(paymentService.page(pageNum, pageSize));
    }
}
