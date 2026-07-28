package edu.fjut.mall.payment.controller;

import edu.fjut.mall.common.exception.BusinessException;
import edu.fjut.mall.common.page.PageResult;
import edu.fjut.mall.common.result.Result;
import edu.fjut.mall.common.result.ResultCode;
import edu.fjut.mall.payment.dto.AdminPaymentPageQuery;
import edu.fjut.mall.payment.dto.AdminRefundPageQuery;
import edu.fjut.mall.payment.dto.PaymentVO;
import edu.fjut.mall.payment.dto.RefundProcessRequest;
import edu.fjut.mall.payment.dto.RefundVO;
import edu.fjut.mall.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/payment")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final PaymentService paymentService;

    @GetMapping("/payments")
    public Result<PageResult<PaymentVO>> payments(@ModelAttribute AdminPaymentPageQuery query,
                                                   @RequestHeader("X-User-Role") Integer role) {
        requireAdmin(role);
        return Result.success(paymentService.pageForAdmin(query));
    }

    @GetMapping("/refunds")
    public Result<PageResult<RefundVO>> refunds(@ModelAttribute AdminRefundPageQuery query,
                                                 @RequestHeader("X-User-Role") Integer role) {
        requireAdmin(role);
        return Result.success(paymentService.refundPageForAdmin(query));
    }

    @PutMapping("/refunds/{refundId}/process")
    public Result<RefundVO> processRefund(@PathVariable Long refundId, @Valid @RequestBody RefundProcessRequest request,
                                           @RequestHeader("X-User-Id") Long adminId,
                                           @RequestHeader("X-User-Role") Integer role) {
        requireAdmin(role);
        return Result.success(paymentService.processRefund(refundId, request.getRefundStatus(), request.getProcessRemark(), adminId));
    }

    private void requireAdmin(Integer role) {
        if (role == null || role != 2) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "仅管理员可以操作支付与退款管理功能");
        }
    }
}
