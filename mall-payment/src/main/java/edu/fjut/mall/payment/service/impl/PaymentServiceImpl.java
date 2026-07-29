package edu.fjut.mall.payment.service.impl;

import edu.fjut.mall.common.exception.BusinessException;
import edu.fjut.mall.common.page.PageQuery;
import edu.fjut.mall.common.page.PageResult;
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
        Map<String, Object> order = getOrder(request.getOrderNo(), request.getUserId());
        if (((Number) order.get("status")).intValue() != 1) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR.getCode(), "仅已支付且未发货的订单可申请退款");
        }
        request.setAmount((BigDecimal) order.get("total_amount"));
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
    public PaymentVO pay(String orderNo, Long userId) {
        Map<String, Object> order = getOrder(orderNo, userId);
        if (((Number) order.get("status")).intValue() != 0) {
            throw new BusinessException(ResultCode.PAYMENT_FAILED.getCode(), "订单当前不可支付");
        }
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
        List<Map<String, Object>> items = jdbcTemplate.queryForList(
            "SELECT sku_id, quantity FROM order_item "
                + "WHERE order_id = (SELECT id FROM order_t WHERE order_no = ?)", orderNo);
        for (Map<String, Object> item : items) {
            int rows = jdbcTemplate.update(
                "UPDATE inventory SET total_stock = total_stock - ?, locked_stock = locked_stock - ?, "
                    + "update_time = NOW() WHERE sku_id = ? AND locked_stock >= ? AND total_stock >= ?",
                ((Number) item.get("quantity")).intValue(), ((Number) item.get("quantity")).intValue(),
                ((Number) item.get("sku_id")).longValue(), ((Number) item.get("quantity")).intValue(),
                ((Number) item.get("quantity")).intValue());
            if (rows == 0) {
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "库存扣减失败");
            }
        }

        info.setPayStatus(1);  // 已支付
        info.setTradeNo(tradeNo);
        info.setPayTime(LocalDateTime.now());
        paymentInfoMapper.updateStatus(info.getId(), 1);
        paymentInfoMapper.updateTradeNo(info.getId(), tradeNo);

        // 同步更新订单状态为已支付
        jdbcTemplate.update("UPDATE order_t SET status = 1 WHERE order_no = ?", orderNo);
        jdbcTemplate.update(
            "UPDATE seller_order_t SET status = 1, update_time = NOW() "
                + "WHERE order_id = (SELECT id FROM order_t WHERE order_no = ?) AND status = 0", orderNo);

        log.info("支付成功: orderNo={}, tradeNo={}", orderNo, tradeNo);
        return toVO(info);
    }

    @Override
    @Transactional
    public PaymentVO queryStatus(String orderNo, Long userId) {
        getOrder(orderNo, userId);
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
        getOrder(request.getOrderNo(), request.getUserId());
        if (request.getRefundAmount() == null
                || request.getRefundAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "退款金额必须大于 0");
        }
        PaymentInfo info = paymentInfoMapper.selectByOrderNo(request.getOrderNo());
        if (info == null) throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "支付单不存在");
        if (info.getPayStatus() != 1)
            throw new BusinessException(ResultCode.PAYMENT_FAILED.getCode(), "仅已支付的订单可申请退款");
        if (hasShippedSellerOrder(request.getOrderNo())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR.getCode(), "订单已有店铺发货，不能申请整单退款");
        }
        if (request.getRefundAmount().compareTo(info.getAmount()) > 0)
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "退款金额不能超过支付金额");
        if (request.getRefundAmount().compareTo(info.getAmount()) != 0)
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "当前仅支持整单退款");
        if (paymentRefundMapper.countActiveByOrderNo(request.getOrderNo()) > 0)
            throw new BusinessException(ResultCode.CONFLICT.getCode(), "该订单已有待处理或已完成的退款申请");

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
    public RefundVO queryRefundStatus(String orderNo, Long userId) {
        getOrder(orderNo, userId);
        PaymentRefund refund = paymentRefundMapper.selectLatestByOrderNo(orderNo);
        return refund == null ? null : toRefundVO(refund);
    }

    @Override
    @Transactional
    public RefundVO processRefund(Long refundId, Integer refundStatus) {
        return processRefund(refundId, refundStatus, null, null);
    }

    @Override
    @Transactional
    public RefundVO processRefund(Long refundId, Integer refundStatus, String processRemark, Long processorId) {
        if (refundStatus == null || (refundStatus != 1 && refundStatus != 2)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "退款处理结果只能为同意或拒绝");
        }
        if (refundStatus == 2 && (processRemark == null || processRemark.isBlank())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "拒绝退款时必须填写处理备注");
        }
        PaymentRefund refund = paymentRefundMapper.selectById(refundId);
        if (refund == null) throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "退款记录不存在");
        if (refund.getRefundStatus() != 0)
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "退款记录已处理");

        if (refundStatus == 1) {
            PaymentInfo info = paymentInfoMapper.selectByOrderNo(refund.getOrderNo());
            if (info == null || info.getPayStatus() != 1) {
                throw new BusinessException(ResultCode.PAYMENT_FAILED.getCode(), "支付单当前不可退款");
            }
            if (hasShippedSellerOrder(refund.getOrderNo())) {
                throw new BusinessException(ResultCode.ORDER_STATUS_ERROR.getCode(), "订单已有店铺发货，不能执行整单退款");
            }
            int orderRows = jdbcTemplate.update(
                "UPDATE order_t SET status = 4 WHERE order_no = ? AND status = 1", refund.getOrderNo());
            if (orderRows == 0) {
                throw new BusinessException(ResultCode.ORDER_STATUS_ERROR.getCode(), "订单已发货或状态已变化，不能执行退款");
            }
            jdbcTemplate.update(
                "UPDATE seller_order_t SET status = 4, update_time = NOW() "
                    + "WHERE order_id = (SELECT id FROM order_t WHERE order_no = ?) AND status = 1", refund.getOrderNo());
            restoreInventory(refund.getOrderNo());
            paymentInfoMapper.updateStatus(info.getId(), 2);
            log.info("退款成功: refundId={}, orderNo={}, amount={}", refundId, refund.getOrderNo(), refund.getRefundAmount());
        } else {
            log.info("退款拒绝: refundId={}, orderNo={}", refundId, refund.getOrderNo());
        }
        int rows = paymentRefundMapper.updateProcess(refundId, refundStatus, processorId, processRemark);
        if (rows == 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "退款记录已处理");
        }
        refund.setRefundStatus(refundStatus);
        refund.setProcessorId(processorId);
        refund.setProcessRemark(processRemark);
        refund.setProcessTime(LocalDateTime.now());
        return toRefundVO(refund);
    }

    @Override
    public List<PaymentVO> page(Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<PaymentInfo> list = paymentInfoMapper.selectAll(offset, pageSize);
        return list.stream().map(this::toVO).toList();
    }

    @Override
    public PageResult<PaymentVO> pageForAdmin(AdminPaymentPageQuery query) {
        normalizePageQuery(query);
        long total = paymentInfoMapper.countForAdmin(query);
        if (total == 0) {
            return PageResult.empty(query);
        }
        List<PaymentVO> records = paymentInfoMapper.selectPageForAdmin(query).stream().map(this::toVO).toList();
        return new PageResult<>(records, total, query.getPageNum(), query.getPageSize());
    }

    @Override
    public PageResult<RefundVO> refundPageForAdmin(AdminRefundPageQuery query) {
        normalizePageQuery(query);
        long total = paymentRefundMapper.countForAdmin(query);
        if (total == 0) {
            return PageResult.empty(query);
        }
        List<RefundVO> records = paymentRefundMapper.selectPageForAdmin(query).stream().map(this::toRefundVO).toList();
        return new PageResult<>(records, total, query.getPageNum(), query.getPageSize());
    }

    private void restoreInventory(String orderNo) {
        List<Map<String, Object>> items = jdbcTemplate.queryForList(
            "SELECT sku_id, quantity FROM order_item WHERE order_id = (SELECT id FROM order_t WHERE order_no = ?)", orderNo);
        for (Map<String, Object> item : items) {
            Long skuId = ((Number) item.get("sku_id")).longValue();
            int quantity = ((Number) item.get("quantity")).intValue();
            int rows = jdbcTemplate.update(
                "UPDATE inventory SET total_stock = total_stock + ?, available_stock = available_stock + ?, update_time = NOW() WHERE sku_id = ?",
                quantity, quantity, skuId);
            if (rows == 0) {
                throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "退款库存记录不存在");
            }
            jdbcTemplate.update("UPDATE product_sku SET stock = stock + ? WHERE id = ?", quantity, skuId);
            jdbcTemplate.update(
                "INSERT INTO inventory_log (id, sku_id, order_no, change_type, change_count, before_stock, after_stock) "
                    + "SELECT ?, sku_id, ?, 'REFUND', ?, available_stock - ?, available_stock FROM inventory WHERE sku_id = ?",
                idGen.nextId(), orderNo, quantity, quantity, skuId);
        }
    }

    private boolean hasShippedSellerOrder(String orderNo) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM seller_order_t WHERE order_id = "
                + "(SELECT id FROM order_t WHERE order_no = ?) AND status IN (2, 3)",
            Integer.class, orderNo);
        return count != null && count > 0;
    }

    private void normalizePageQuery(PageQuery query) {
        if (query.getPageNum() < 1) {
            query.setPageNum(1);
        }
        if (query.getPageSize() < 1) {
            query.setPageSize(20);
        } else if (query.getPageSize() > 100) {
            query.setPageSize(100);
        }
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

    private Map<String, Object> getOrder(String orderNo, Long userId) {
        List<Map<String, Object>> results = jdbcTemplate.queryForList(
            "SELECT id, user_id, total_amount, status FROM order_t WHERE order_no = ?", orderNo);
        if (results.isEmpty()) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "订单不存在");
        }
        Map<String, Object> order = results.get(0);
        long ownerId = ((Number) order.get("user_id")).longValue();
        if (ownerId != userId) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权操作该订单");
        }
        return order;
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
            .reason(refund.getReason()).processorId(refund.getProcessorId())
            .processRemark(refund.getProcessRemark()).processTime(refund.getProcessTime())
            .createTime(refund.getCreateTime()).updateTime(refund.getUpdateTime())
            .build();
    }
}
