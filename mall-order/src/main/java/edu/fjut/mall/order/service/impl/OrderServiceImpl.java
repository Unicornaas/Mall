package edu.fjut.mall.order.service.impl;

import edu.fjut.mall.common.exception.BusinessException;
import edu.fjut.mall.common.result.ResultCode;
import edu.fjut.mall.common.util.SnowflakeIdGenerator;
import edu.fjut.mall.order.dto.CreateOrderRequest;
import edu.fjut.mall.order.dto.OrderVO;
import edu.fjut.mall.order.dto.OrderVO.OrderItemVO;
import edu.fjut.mall.order.entity.Order;
import edu.fjut.mall.order.entity.OrderItem;
import edu.fjut.mall.order.mapper.OrderItemMapper;
import edu.fjut.mall.order.mapper.OrderMapper;
import edu.fjut.mall.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final SnowflakeIdGenerator idGen;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public OrderVO create(CreateOrderRequest request) {
        // 1. 构建订单明细（查 product_sku 价格）
        List<OrderItem> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (var itemReq : request.getItems()) {
            Map<String, Object> sku = jdbcTemplate.queryForMap(
                "SELECT name, price, images FROM product_sku WHERE id = ?", itemReq.getSkuId());
            String name = (String) sku.get("name");
            BigDecimal price = (BigDecimal) sku.get("price");

            OrderItem item = new OrderItem();
            item.setId(idGen.nextId());
            item.setSpuId(itemReq.getSpuId());
            item.setSkuId(itemReq.getSkuId());
            item.setProductName(name);
            item.setProductImage((String) sku.get("images"));
            item.setPrice(price);
            item.setQuantity(itemReq.getQuantity());
            item.setTotalPrice(price.multiply(BigDecimal.valueOf(itemReq.getQuantity())));
            items.add(item);
            totalAmount = totalAmount.add(item.getTotalPrice());
        }

        // 2. 查询地址（user_address 表，简化处理直接用 addressId 拼接）
        Map<String, Object> addr = jdbcTemplate.queryForMap(
            "SELECT receiver_name, receiver_phone, CONCAT(province, city, district, detail) AS full_addr FROM user_address WHERE id = ?",
            request.getAddressId());

        // 3. 创建订单
        Order order = new Order();
        order.setId(idGen.nextId());
        order.setOrderNo(generateOrderNo());
        order.setUserId(request.getUserId());
        order.setTotalAmount(totalAmount);
        order.setStatus(0);
        order.setReceiverName((String) addr.get("receiver_name"));
        order.setReceiverPhone((String) addr.get("receiver_phone"));
        order.setReceiverAddress((String) addr.get("full_addr"));
        order.setRemark(request.getRemark());
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.insert(order);

        // 4. 保存明细
        items.forEach(i -> i.setOrderId(order.getId()));
        orderItemMapper.insertBatch(items);

        // 5. 扣库存
        for (var itemReq : request.getItems()) {
            int rows = jdbcTemplate.update(
                "UPDATE product_sku SET stock = stock - ? WHERE id = ? AND stock >= ?",
                itemReq.getQuantity(), itemReq.getSkuId(), itemReq.getQuantity());
            if (rows == 0) throw new BusinessException(ResultCode.STOCK_INSUFFICIENT.getCode(),
                "SKU " + itemReq.getSkuId() + " 库存不足");
        }

        // 6. 创建支付单（待支付状态）
        jdbcTemplate.update(
            "INSERT INTO payment_info (id, order_no, user_id, amount, pay_type, pay_status, create_time, update_time) "
                + "VALUES (?, ?, ?, ?, 1, 0, NOW(), NOW())",
            idGen.nextId(), order.getOrderNo(), request.getUserId(), totalAmount);

        log.info("订单创建成功: orderNo={}, userId={}, amount={}", order.getOrderNo(), request.getUserId(), totalAmount);
        return toVO(order, items);
    }

    @Override
    public OrderVO getById(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "订单不存在");
        List<OrderItem> items = orderItemMapper.selectByOrderId(id);
        return toVO(order, items);
    }

    @Override
    public List<OrderVO> listByUserId(Long userId) {
        List<Order> orders = orderMapper.selectByUserId(userId);
        List<OrderVO> vos = new ArrayList<>();
        for (Order order : orders) {
            List<OrderItem> items = orderItemMapper.selectByOrderId(order.getId());
            vos.add(toVO(order, items));
        }
        return vos;
    }

    @Override
    @Transactional
    public void cancel(Long id, Long userId) {
        Order order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "订单不存在");
        if (!order.getUserId().equals(userId))
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权操作");
        if (order.getStatus() != 0)
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR.getCode(), "仅待支付订单可取消");
        orderMapper.updateStatus(id, 4);
        log.info("订单已取消: id={}", id);
    }

    private String generateOrderNo() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%06d", (long)(Math.random() * 1000000));
    }

    private OrderVO toVO(Order o, List<OrderItem> items) {
        return OrderVO.builder()
            .id(o.getId()).orderNo(o.getOrderNo()).userId(o.getUserId())
            .totalAmount(o.getTotalAmount()).status(o.getStatus())
            .receiverName(o.getReceiverName()).receiverPhone(o.getReceiverPhone())
            .receiverAddress(o.getReceiverAddress()).remark(o.getRemark())
            .createTime(o.getCreateTime())
            .items(items.stream().map(i -> OrderItemVO.builder()
                .id(i.getId()).spuId(i.getSpuId()).skuId(i.getSkuId())
                .productName(i.getProductName()).productImage(i.getProductImage())
                .price(i.getPrice()).quantity(i.getQuantity()).totalPrice(i.getTotalPrice())
                .build()).toList())
            .build();
    }
}
