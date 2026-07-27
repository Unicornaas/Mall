package edu.fjut.mall.payment.service.impl;

import edu.fjut.mall.common.exception.BusinessException;
import edu.fjut.mall.common.result.ResultCode;
import edu.fjut.mall.common.util.SnowflakeIdGenerator;
import edu.fjut.mall.payment.dto.*;
import edu.fjut.mall.payment.entity.PaymentInfo;
import edu.fjut.mall.payment.entity.PaymentRefund;
import edu.fjut.mall.payment.mapper.PaymentInfoMapper;
import edu.fjut.mall.payment.mapper.PaymentRefundMapper;
import edu.fjut.mall.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentInfoMapper paymentInfoMapper;
    private final PaymentRefundMapper paymentRefundMapper;
    private final SnowflakeIdGenerator idGen;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public PaymentVO create(CreatePaymentRequest request) {
        // 检查是否已存在支付单
        PaymentInfo existing = paymentInfoMapper.selectByOrderNo(request.getOrderNo());
        if (existing != null) throw new BusinessException(ResultCode.CONFLICT.getCode(), "该订单支付单已存在");

        PaymentInfo info = new PaymentInfo();
        info.setId(idGen.nextId());
        info.setOrderNo(request.getOrderNo());
        info.setUserId(request.getUserId());
        info.setAmount(request.getAmount());
        info.setPayType(request.getPayType() != null ? request.getPayType() : 1);
        info.setPayStatus(0);  // 待支付
        info.setCreateTime(LocalDateTime.now());
        info.setUpdateTime(LocalDateTime.now());
        paymentInfoMapper.insert(info);

        log.info("支付单创建成功: orderNo={}, amount={}", request.getOrderNo(), request.getAmount());
        return toVO(info);
    }

    @Override
    @Transactional
    public PaymentVO pay(String orderNo) {
        PaymentInfo info = paymentInfoMapper.selectByOrderNo(orderNo);
        if (info == null) {
            // 兜底：如果支付单不存在，尝试从订单表自动创建
            info = createFromOrder(orderNo);
        }
        if (info.getPayStatus() != 0)
            throw new BusinessException(ResultCode.PAYMENT_FAILED.getCode(), "支付单状态异常，当前状态: " + info.getPayStatus());

        // 模拟支付成功
        String tradeNo = "PAY" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
            + String.format("%06d", (long) (Math.random() * 1000000));
        info.setPayStatus(1);  // 已支付
        info.setTradeNo(tradeNo);
        info.setPayTime(LocalDateTime.now());
        paymentInfoMapper.updateStatus(info.getId(), 1);
        paymentInfoMapper.updateTradeNo(info.getId(), tradeNo);

        // 同步更新订单状态为已支付
        jdbcTemplate.update("UPDATE order_t SET status = 1 WHERE order_no = ?", orderNo);

        log.info("支付成功: orderNo={}, tradeNo={}", orderNo, tradeNo);
        return toVO(info);
    }

    @Override
    @Transactional
    public PaymentVO queryStatus(String orderNo) {
        PaymentInfo info = paymentInfoMapper.selectByOrderNo(orderNo);
        if (info == null) {
            // 兜底：如果支付单不存在，尝试从订单表自动创建
            info = createFromOrder(orderNo);
        }
        return toVO(info);
    }

    @Override
    @Transactional
    public RefundVO refund(RefundRequest request) {
        PaymentInfo info = paymentInfoMapper.selectByOrderNo(request.getOrderNo());
        if (info == null) throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "支付单不存在");
        if (info.getPayStatus() != 1)
            throw new BusinessException(ResultCode.PAYMENT_FAILED.getCode(), "仅已支付的订单可申请退款");
        if (request.getRefundAmount().compareTo(info.getAmount()) > 0)
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "退款金额不能超过支付金额");

        PaymentRefund refund = new PaymentRefund();
        refund.setId(idGen.nextId());
        refund.setOrderNo(request.getOrderNo());
        refund.setUserId(request.getUserId());
        refund.setRefundAmount(request.getRefundAmount());
        refund.setRefundStatus(0);  // 待处理
        refund.setReason(request.getReason());
        refund.setCreateTime(LocalDateTime.now());
        refund.setUpdateTime(LocalDateTime.now());
        paymentRefundMapper.insert(refund);

        log.info("退款申请创建: orderNo={}, amount={}, reason={}", request.getOrderNo(), request.getRefundAmount(), request.getReason());
        return toRefundVO(refund);
    }

    @Override
    @Transactional
    public RefundVO processRefund(Long refundId, Integer refundStatus) {
        PaymentRefund refund = paymentRefundMapper.selectById(refundId);
        if (refund == null) throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "退款记录不存在");
        if (refund.getRefundStatus() != 0)
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "退款记录已处理");

        paymentRefundMapper.updateStatus(refundId, refundStatus);
        refund.setRefundStatus(refundStatus);

        if (refundStatus == 1) {
            // 退款成功，更新支付状态
            PaymentInfo info = paymentInfoMapper.selectByOrderNo(refund.getOrderNo());
            if (info != null) {
                paymentInfoMapper.updateStatus(info.getId(), 2);  // 已退款
            }
            log.info("退款成功: refundId={}, orderNo={}, amount={}", refundId, refund.getOrderNo(), refund.getRefundAmount());
        } else {
            log.info("退款拒绝: refundId={}, orderNo={}", refundId, refund.getOrderNo());
        }
        return toRefundVO(refund);
    }

    @Override
    public List<PaymentVO> page(Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<PaymentInfo> list = paymentInfoMapper.selectAll(offset, pageSize);
        return list.stream().map(this::toVO).toList();
    }

    /**
     * 兜底：根据 orderNo 从订单表查询并自动创建支付单
     */
    private PaymentInfo createFromOrder(String orderNo) {
        List<Map<String, Object>> results = jdbcTemplate.queryForList(
            "SELECT user_id, total_amount FROM order_t WHERE order_no = ?", orderNo);
        if (results.isEmpty()) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "订单不存在: " + orderNo);
        }
        Map<String, Object> order = results.get(0);
        Long userId = (Long) order.get("user_id");
        BigDecimal amount = (BigDecimal) order.get("total_amount");

        PaymentInfo info = new PaymentInfo();
        info.setId(idGen.nextId());
        info.setOrderNo(orderNo);
        info.setUserId(userId);
        info.setAmount(amount);
        info.setPayType(1);
        info.setPayStatus(0);
        info.setCreateTime(LocalDateTime.now());
        info.setUpdateTime(LocalDateTime.now());
        paymentInfoMapper.insert(info);

        log.info("兜底创建支付单: orderNo={}, amount={}", orderNo, amount);
        return info;
    }

    private PaymentVO toVO(PaymentInfo info) {
        return PaymentVO.builder()
            .id(info.getId()).orderNo(info.getOrderNo()).userId(info.getUserId())
            .amount(info.getAmount()).payType(info.getPayType())
            .payStatus(info.getPayStatus()).tradeNo(info.getTradeNo())
            .payTime(info.getPayTime()).createTime(info.getCreateTime())
            .build();
    }

    private RefundVO toRefundVO(PaymentRefund refund) {
        return RefundVO.builder()
            .id(refund.getId()).orderNo(refund.getOrderNo()).userId(refund.getUserId())
            .refundAmount(refund.getRefundAmount()).refundStatus(refund.getRefundStatus())
            .reason(refund.getReason())
            .createTime(refund.getCreateTime()).updateTime(refund.getUpdateTime())
            .build();
    }
}
