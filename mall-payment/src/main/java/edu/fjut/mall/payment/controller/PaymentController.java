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
    public Result<PaymentVO> create(@Valid @RequestBody CreatePaymentRequest request,
                                    @RequestHeader("X-User-Id") Long userId) {
        request.setUserId(userId);
        return Result.success(paymentService.create(request));
    }

    /**
     * PAY-02: 模拟支付
     */
    @PutMapping("/pay/{orderNo}")
    public Result<PaymentVO> pay(@PathVariable String orderNo,
                                 @RequestHeader("X-User-Id") Long userId) {
        return Result.success(paymentService.pay(orderNo, userId));
    }

    /**
     * PAY-03: 支付状态查询
     */
    @GetMapping("/status/{orderNo}")
    public Result<PaymentVO> queryStatus(@PathVariable String orderNo,
                                         @RequestHeader("X-User-Id") Long userId) {
        return Result.success(paymentService.queryStatus(orderNo, userId));
    }

    /**
     * PAY-04: 申请退款
     */
    @PostMapping("/refund")
    public Result<RefundVO> refund(@Valid @RequestBody RefundRequest request,
                                   @RequestHeader("X-User-Id") Long userId) {
        request.setUserId(userId);
        return Result.success(paymentService.refund(request));
    }

    /** PAY-04-1: 查询当前用户订单的退款申请状态 */
    @GetMapping("/refund/status/{orderNo}")
    public Result<RefundVO> refundStatus(@PathVariable String orderNo,
                                         @RequestHeader("X-User-Id") Long userId) {
        return Result.success(paymentService.queryRefundStatus(orderNo, userId));
    }

    /** Lists all item-level refund requests for the buyer's order. */
    @GetMapping("/refund/list/{orderNo}")
    public Result<List<RefundVO>> refundList(@PathVariable String orderNo,
                                              @RequestHeader("X-User-Id") Long userId) {
        return Result.success(paymentService.listRefunds(orderNo, userId));
    }

    /**
     * PAY-05: 退款处理
     */
    @PutMapping("/refund/{refundId}/process")
    public Result<RefundVO> processRefund(@PathVariable Long refundId, @RequestParam Integer refundStatus,
                                          @RequestHeader("X-User-Role") Integer role) {
        if (role == null || role != 2) {
            throw new edu.fjut.mall.common.exception.BusinessException(403, "仅管理员可以处理退款");
        }
        return Result.success(paymentService.processRefund(refundId, refundStatus));
    }

    /**
     * PAY-06: 支付记录列表
     */
    @GetMapping("/page")
    public Result<List<PaymentVO>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "20") Integer pageSize,
                                        @RequestHeader("X-User-Role") Integer role) {
        if (role == null || role != 2) {
            throw new edu.fjut.mall.common.exception.BusinessException(403, "仅管理员可以查看支付记录");
        }
        return Result.success(paymentService.page(pageNum, pageSize));
    }
}
