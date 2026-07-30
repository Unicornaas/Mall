package edu.fjut.mall.order.service.impl;

import edu.fjut.mall.common.exception.BusinessException;
import edu.fjut.mall.common.result.ResultCode;
import edu.fjut.mall.common.util.SnowflakeIdGenerator;
import edu.fjut.mall.common.page.PageResult;
import edu.fjut.mall.order.dto.AdminOrderPageQuery;
import edu.fjut.mall.order.dto.CreateOrderRequest;
import edu.fjut.mall.order.dto.OrderVO;
import edu.fjut.mall.order.dto.ShipOrderRequest;
import edu.fjut.mall.order.dto.ShipItemRequest;
import edu.fjut.mall.order.dto.SellerOrderPageQuery;
import edu.fjut.mall.order.dto.SellerOrderVO;
import edu.fjut.mall.order.dto.SellerShipmentVO;
import edu.fjut.mall.order.dto.SellerDashboardOrderVO;
import edu.fjut.mall.order.dto.SellerDashboardOverviewVO;
import edu.fjut.mall.order.dto.SellerDashboardStockAlertVO;
import edu.fjut.mall.order.dto.AdminDashboardOrderVO;
import edu.fjut.mall.order.dto.AdminDashboardOverviewVO;
import edu.fjut.mall.order.dto.AdminDashboardStockAlertVO;
import edu.fjut.mall.order.dto.AdminDashboardTrendVO;
import edu.fjut.mall.order.dto.OrderVO.OrderItemVO;
import edu.fjut.mall.order.entity.Order;
import edu.fjut.mall.order.entity.OrderItem;
import edu.fjut.mall.order.entity.SellerOrder;
import edu.fjut.mall.order.mapper.OrderItemMapper;
import edu.fjut.mall.order.mapper.OrderMapper;
import edu.fjut.mall.order.mapper.SellerOrderMapper;
import edu.fjut.mall.order.mapper.SellerDashboardMapper;
import edu.fjut.mall.order.mapper.AdminDashboardMapper;
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
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final SellerOrderMapper sellerOrderMapper;
    private final SellerDashboardMapper sellerDashboardMapper;
    private final AdminDashboardMapper adminDashboardMapper;
    private final SnowflakeIdGenerator idGen;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public OrderVO create(CreateOrderRequest request) {
        // 1. 构建订单明细（查 product_sku 价格）
        List<OrderItem> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (var itemReq : request.getItems()) {
            List<Map<String, Object>> skuRows = jdbcTemplate.queryForList(
                "SELECT sku.id AS sku_id, sku.spu_id, sku.name, sku.price, "
                    + "COALESCE(NULLIF(TRIM(sku.images), ''), NULLIF(TRIM(spu.main_image), ''), spu.images) AS images, "
                    + "spu.seller_id "
                    + "FROM product_sku sku JOIN product_spu spu ON spu.id = sku.spu_id "
                    + "WHERE sku.id = ?",
                itemReq.getSkuId());
            if (skuRows.isEmpty()) {
                throw new BusinessException(ResultCode.NOT_FOUND.getCode(),
                    "商品或 SKU 不存在，请刷新购物车后重试");
            }
            Map<String, Object> sku = skuRows.get(0);
            String name = (String) sku.get("name");
            BigDecimal price = (BigDecimal) sku.get("price");
            Number sellerValue = (Number) sku.get("seller_id");
            // 兼容历史平台自营商品：未设置商家时固定归属平台（seller_id=0）。
            Long itemSellerId = sellerValue == null ? 0L : sellerValue.longValue();
            if (price == null) {
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "商品价格不存在");
            }

            OrderItem item = new OrderItem();
            item.setId(idGen.nextId());
            item.setSellerId(itemSellerId);
            item.setSpuId(((Number) sku.get("spu_id")).longValue());
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
        List<Map<String, Object>> addressRows = jdbcTemplate.queryForList(
            "SELECT receiver_name, receiver_phone, CONCAT(province, city, district, detail) AS full_addr "
                + "FROM user_address WHERE id = ? AND user_id = ?",
            request.getAddressId(), request.getUserId());
        if (addressRows.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(),
                "收货地址不存在或不属于当前用户");
        }
        Map<String, Object> addr = addressRows.get(0);

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

        // 4. 按商品归属拆分商家子订单，主订单仅负责一次支付和买家视角。
        Map<Long, Long> sellerOrderIds = createSellerOrders(order.getId(), items);

        // 5. 保存明细
        items.forEach(i -> {
            i.setOrderId(order.getId());
            i.setSellerOrderId(sellerOrderIds.get(i.getSellerId()));
        });
        orderItemMapper.insertBatch(items);

        // 6. 预占库存。inventory 是库存唯一来源；兼容旧数据时按 SKU stock 自动初始化。
        for (var itemReq : request.getItems()) {
            ensureInventoryRow(itemReq.getSkuId());
            Map<String, Object> inventory = jdbcTemplate.queryForMap(
                "SELECT available_stock, locked_stock FROM inventory WHERE sku_id = ? FOR UPDATE",
                itemReq.getSkuId());
            int available = ((Number) inventory.get("available_stock")).intValue();
            int locked = ((Number) inventory.get("locked_stock")).intValue();
            if (available < itemReq.getQuantity()) {
                throw new BusinessException(ResultCode.STOCK_INSUFFICIENT.getCode(),
                    "SKU " + itemReq.getSkuId() + " 库存不足");
            }
            jdbcTemplate.update(
                "UPDATE inventory SET available_stock = ?, locked_stock = ?, update_time = NOW() WHERE sku_id = ?",
                available - itemReq.getQuantity(), locked + itemReq.getQuantity(), itemReq.getSkuId());
            // product_sku.stock is kept as a display-compatible available-stock snapshot.
            jdbcTemplate.update("UPDATE product_sku SET stock = ? WHERE id = ?",
                available - itemReq.getQuantity(), itemReq.getSkuId());
            jdbcTemplate.update(
                "INSERT INTO inventory_log (id, sku_id, order_no, change_type, change_count, before_stock, after_stock) "
                    + "VALUES (?, ?, ?, 'LOCK', ?, ?, ?)",
                idGen.nextId(), itemReq.getSkuId(), order.getOrderNo(), itemReq.getQuantity(),
                available, available - itemReq.getQuantity());
        }

        // 7. 创建支付单（待支付状态）
        jdbcTemplate.update(
            "INSERT INTO payment_info (id, order_no, user_id, amount, pay_type, pay_status, create_time, update_time) "
                + "VALUES (?, ?, ?, ?, 1, 0, NOW(), NOW())",
            idGen.nextId(), order.getOrderNo(), request.getUserId(), totalAmount);

        log.info("订单创建成功: orderNo={}, userId={}, amount={}", order.getOrderNo(), request.getUserId(), totalAmount);
        return toVO(order, items);
    }

    @Override
    public OrderVO getById(Long id, Long userId) {
        Order order = orderMapper.selectById(id);
        if (order != null && !order.getUserId().equals(userId))
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权查看该订单");
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
        List<OrderItem> items = orderItemMapper.selectByOrderId(id);
        for (OrderItem item : items) {
            int rows = jdbcTemplate.update(
                "UPDATE inventory SET available_stock = available_stock + ?, "
                    + "locked_stock = locked_stock - ?, update_time = NOW() "
                    + "WHERE sku_id = ? AND locked_stock >= ?",
                item.getQuantity(), item.getQuantity(), item.getSkuId(), item.getQuantity());
            if (rows == 0) {
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "库存释放失败");
            }
            jdbcTemplate.update("UPDATE product_sku SET stock = stock + ? WHERE id = ?",
                item.getQuantity(), item.getSkuId());
            jdbcTemplate.update(
                "INSERT INTO inventory_log (id, sku_id, order_no, change_type, change_count, before_stock, after_stock) "
                    + "SELECT ?, sku_id, ?, 'RELEASE', ?, available_stock - ?, available_stock "
                    + "FROM inventory WHERE sku_id = ?",
                idGen.nextId(), order.getOrderNo(), item.getQuantity(), item.getQuantity(), item.getSkuId());
        }
        jdbcTemplate.update("UPDATE payment_info SET pay_status = 3 WHERE order_no = ? AND pay_status = 0",
            order.getOrderNo());
        orderMapper.updateStatus(id, 4);
        log.info("订单已取消: id={}", id);
    }

    @Override
    public PageResult<OrderVO> pageForAdmin(AdminOrderPageQuery query) {
        List<Order> orders = orderMapper.selectPageForAdmin(query);
        List<OrderVO> records = orders.stream()
            .map(order -> toVO(order, orderItemMapper.selectByOrderId(order.getId())))
            .toList();
        return new PageResult<>(records, orderMapper.countForAdmin(query), query.getPageNum(), query.getPageSize());
    }

    @Override
    public OrderVO getByIdForAdmin(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "订单不存在");
        }
        return toVO(order, orderItemMapper.selectByOrderId(id));
    }

    @Override
    @Transactional
    public void closeForAdmin(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR.getCode(), "仅待支付订单可以关闭");
        }
        releaseReservedStock(order);
        log.info("管理员关闭订单: id={}", id);
    }

    @Override
    @Transactional
    public void shipForAdmin(Long id, ShipOrderRequest request) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "订单不存在");
        }
        if (order.getStatus() != 1 || orderMapper.updateShipment(id, request.getShippingCompany(), request.getTrackingNo()) == 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR.getCode(), "仅已支付订单可以发货");
        }
        sellerOrderMapper.updateShipmentByOrderId(id, request.getShippingCompany(), request.getTrackingNo());
        log.info("管理员发货: id={}, trackingNo={}", id, request.getTrackingNo());
    }

    @Override
    @Transactional
    public void receiveForAdmin(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "订单不存在");
        }
        if (order.getStatus() != 2 || orderMapper.markReceived(id) == 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR.getCode(), "仅已发货订单可以确认收货");
        }
        sellerOrderMapper.markReceivedByOrderId(id);
        log.info("管理员确认收货: id={}", id);
    }

    @Override
    public PageResult<SellerOrderVO> pageForSeller(SellerOrderPageQuery query, Long sellerId) {
        normalizeSellerPageQuery(query);
        List<SellerOrderVO> records = sellerOrderMapper.selectPageForSeller(sellerId, query).stream()
            .map(order -> attachSellerItems(order, sellerId))
            .toList();
        return new PageResult<>(records, sellerOrderMapper.countForSeller(sellerId, query),
            query.getPageNum(), query.getPageSize());
    }

    @Override
    public SellerOrderVO getByIdForSeller(Long id, Long sellerId) {
        SellerOrderVO sellerOrder = sellerOrderMapper.selectDetailForSeller(id, sellerId);
        if (sellerOrder == null) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权查看该订单");
        }
        return attachSellerItems(sellerOrder, sellerId);
    }

    @Override
    @Transactional
    public void shipForSeller(Long sellerOrderId, ShipOrderRequest request, Long sellerId) {
        SellerOrder sellerOrder = sellerOrderMapper.selectByIdAndSellerId(sellerOrderId, sellerId);
        if (sellerOrder == null) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权操作该订单");
        }
        Order masterOrder = orderMapper.selectById(sellerOrder.getOrderId());
        if (masterOrder == null || (masterOrder.getStatus() != 1 && masterOrder.getStatus() != 2
            && masterOrder.getStatus() != 5)) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR.getCode(), "仅已支付店铺订单可以发货");
        }
        List<OrderItem> sellerItems = orderItemMapper.selectBySellerOrderIdAndSellerId(sellerOrderId, sellerId);
        List<ShipItemRequest> requested = request.getItems();
        if (requested == null || requested.isEmpty()) {
            requested = sellerItems.stream().map(item -> {
                ShipItemRequest all = new ShipItemRequest();
                all.setOrderItemId(item.getId());
                all.setQuantity(item.getQuantity() - safe(item.getShippedQuantity()) - safe(item.getRefundedQuantity()));
                return all;
            }).filter(item -> item.getQuantity() > 0).toList();
        }
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (ShipItemRequest item : requested) {
            if (item.getOrderItemId() == null || item.getQuantity() == null || item.getQuantity() < 1
                || quantities.put(item.getOrderItemId(), item.getQuantity()) != null) {
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "发货商品或数量不合法");
            }
        }
        if (quantities.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "没有可发货的商品");
        }
        Map<Long, OrderItem> itemMap = sellerItems.stream().collect(java.util.stream.Collectors.toMap(OrderItem::getId, item -> item));
        if (quantities.size() != quantities.keySet().stream().filter(itemMap::containsKey).count()) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "发货商品不属于当前店铺订单");
        }
        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            OrderItem item = itemMap.get(entry.getKey());
            int remaining = item.getQuantity() - safe(item.getShippedQuantity()) - safe(item.getRefundedQuantity());
            if (entry.getValue() > remaining) {
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "发货数量超过商品可发货数量");
            }
            int rows = jdbcTemplate.update(
                "UPDATE order_item SET shipped_quantity = COALESCE(shipped_quantity, 0) + ?, "
                    + "shipping_company = ?, tracking_no = ?, ship_time = NOW() "
                    + "WHERE id = ? AND quantity - COALESCE(shipped_quantity, 0) - COALESCE(refunded_quantity, 0) >= ?",
                entry.getValue(), request.getShippingCompany(), request.getTrackingNo(), entry.getKey(), entry.getValue());
            if (rows == 0) throw new BusinessException(ResultCode.ORDER_STATUS_ERROR.getCode(), "商品发货状态已发生变化");
        }
        jdbcTemplate.update("UPDATE seller_order_t SET shipping_company = ?, tracking_no = ?, ship_time = NOW() WHERE id = ?",
            request.getShippingCompany(), request.getTrackingNo(), sellerOrderId);
        sellerOrderMapper.refreshStatus(sellerOrderId);
        refreshMasterOrderStatus(sellerOrder.getOrderId());
        log.info("商家发货: sellerOrderId={}, sellerId={}, trackingNo={}",
            sellerOrderId, sellerId, request.getTrackingNo());
    }

    @Override
    public SellerDashboardOverviewVO getDashboardOverview(Long sellerId) {
        return sellerDashboardMapper.selectOverview(sellerId);
    }

    @Override
    public List<SellerDashboardOrderVO> getDashboardRecentOrders(Long sellerId, Integer limit) {
        return sellerDashboardMapper.selectRecentOrders(sellerId, normalizeDashboardLimit(limit));
    }

    @Override
    public List<SellerDashboardStockAlertVO> getDashboardStockAlerts(Long sellerId, Integer limit) {
        return sellerDashboardMapper.selectStockAlerts(sellerId, normalizeDashboardLimit(limit));
    }

    private int normalizeDashboardLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return 10;
        }
        return Math.min(limit, 20);
    }

    @Override
    public AdminDashboardOverviewVO getAdminDashboardOverview() {
        return adminDashboardMapper.selectOverview();
    }

    @Override
    public List<AdminDashboardTrendVO> getAdminDashboardOrderTrend(Integer days) {
        int normalizedDays = days == null || days < 1 ? 7 : Math.min(days, 30);
        LocalDateTime endTime = LocalDateTime.now();
        return adminDashboardMapper.selectOrderTrend(endTime.minusDays(normalizedDays - 1L).toLocalDate().atStartOfDay(), endTime);
    }

    @Override
    public List<AdminDashboardOrderVO> getAdminDashboardLatestOrders(Integer limit) {
        return adminDashboardMapper.selectLatestOrders(normalizeDashboardLimit(limit));
    }

    @Override
    public List<AdminDashboardStockAlertVO> getAdminDashboardStockAlerts(Integer limit) {
        return adminDashboardMapper.selectStockAlerts(normalizeDashboardLimit(limit));
    }

    private String generateOrderNo() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%06d", (long)(Math.random() * 1000000));
    }

    private void normalizeSellerPageQuery(SellerOrderPageQuery query) {
        if (query.getPageNum() < 1) {
            query.setPageNum(1);
        }
        if (query.getPageSize() < 1) {
            query.setPageSize(20);
        } else if (query.getPageSize() > 100) {
            query.setPageSize(100);
        }
    }

    private void releaseReservedStock(Order order) {
        List<OrderItem> items = orderItemMapper.selectByOrderId(order.getId());
        for (OrderItem item : items) {
            int rows = jdbcTemplate.update(
                "UPDATE inventory SET available_stock = available_stock + ?, "
                    + "locked_stock = locked_stock - ?, update_time = NOW() "
                    + "WHERE sku_id = ? AND locked_stock >= ?",
                item.getQuantity(), item.getQuantity(), item.getSkuId(), item.getQuantity());
            if (rows == 0) {
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "库存释放失败");
            }
            jdbcTemplate.update("UPDATE product_sku SET stock = stock + ? WHERE id = ?",
                item.getQuantity(), item.getSkuId());
            jdbcTemplate.update(
                "INSERT INTO inventory_log (id, sku_id, order_no, change_type, change_count, before_stock, after_stock) "
                    + "SELECT ?, sku_id, ?, 'RELEASE', ?, available_stock - ?, available_stock "
                    + "FROM inventory WHERE sku_id = ?",
                idGen.nextId(), order.getOrderNo(), item.getQuantity(), item.getQuantity(), item.getSkuId());
        }
        jdbcTemplate.update("UPDATE payment_info SET pay_status = 3 WHERE order_no = ? AND pay_status = 0",
            order.getOrderNo());
        orderMapper.updateStatus(order.getId(), 4);
        sellerOrderMapper.updateStatusByOrderId(order.getId(), 4);
    }

    private void ensureInventoryRow(Long skuId) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM inventory WHERE sku_id = ?", Integer.class, skuId);
        if (count != null && count == 0) {
            Map<String, Object> sku = jdbcTemplate.queryForMap(
                "SELECT stock FROM product_sku WHERE id = ?", skuId);
            int stock = ((Number) sku.get("stock")).intValue();
            jdbcTemplate.update(
                "INSERT IGNORE INTO inventory (id, sku_id, total_stock, locked_stock, available_stock, safety_stock) "
                    + "VALUES (?, ?, ?, 0, ?, 10)",
                idGen.nextId(), skuId, stock, stock);
        }
    }

    private int safe(Integer value) {
        return value == null ? 0 : value;
    }

    private void refreshMasterOrderStatus(Long orderId) {
        Integer remaining = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM order_item WHERE order_id = ? AND quantity > COALESCE(refunded_quantity, 0)", Integer.class, orderId);
        Integer unshipped = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM order_item WHERE order_id = ? AND quantity > COALESCE(shipped_quantity, 0) + COALESCE(refunded_quantity, 0)", Integer.class, orderId);
        if (remaining != null && remaining == 0) {
            orderMapper.updateStatus(orderId, 4);
        } else if (unshipped != null && unshipped == 0) {
            orderMapper.updateStatus(orderId, 2);
        } else if (jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM order_item WHERE order_id = ? AND COALESCE(shipped_quantity, 0) > 0", Integer.class, orderId) > 0) {
            orderMapper.updateStatus(orderId, 5);
        }
    }

    private OrderVO toVO(Order o, List<OrderItem> items) {
        return OrderVO.builder()
            .id(o.getId()).orderNo(o.getOrderNo()).userId(o.getUserId())
            .totalAmount(o.getTotalAmount()).status(o.getStatus())
            .receiverName(o.getReceiverName()).receiverPhone(o.getReceiverPhone())
            .receiverAddress(o.getReceiverAddress()).remark(o.getRemark())
            .shippingCompany(o.getShippingCompany()).trackingNo(o.getTrackingNo())
            .shipTime(o.getShipTime()).receiveTime(o.getReceiveTime())
            .createTime(o.getCreateTime())
            .items(items.stream().map(i -> OrderItemVO.builder()
                .id(i.getId()).spuId(i.getSpuId()).skuId(i.getSkuId())
                .productName(i.getProductName()).productImage(i.getProductImage())
                 .price(i.getPrice()).quantity(i.getQuantity()).totalPrice(i.getTotalPrice())
                 .shippedQuantity(safe(i.getShippedQuantity())).refundedQuantity(safe(i.getRefundedQuantity()))
                 .availableShipQuantity(Math.max(0, i.getQuantity() - safe(i.getShippedQuantity()) - safe(i.getRefundedQuantity())))
                 .availableRefundQuantity(Math.max(0, i.getQuantity() - safe(i.getShippedQuantity()) - safe(i.getRefundedQuantity())))
                 .shippingCompany(i.getShippingCompany()).trackingNo(i.getTrackingNo()).shipTime(i.getShipTime())
                 .build()).toList())
            .sellerOrders(sellerOrderMapper.selectByOrderId(o.getId()).stream().map(sellerOrder -> SellerShipmentVO.builder()
                .sellerOrderId(sellerOrder.getId()).sellerId(sellerOrder.getSellerId())
                .status(sellerOrder.getStatus()).shippingCompany(sellerOrder.getShippingCompany())
                .trackingNo(sellerOrder.getTrackingNo()).shipTime(sellerOrder.getShipTime())
                .receiveTime(sellerOrder.getReceiveTime()).build()).toList())
            .build();
    }

    private Map<Long, Long> createSellerOrders(Long orderId, List<OrderItem> items) {
        Map<Long, BigDecimal> sellerAmounts = new LinkedHashMap<>();
        for (OrderItem item : items) {
            sellerAmounts.merge(item.getSellerId(), item.getTotalPrice(), BigDecimal::add);
        }

        Map<Long, Long> sellerOrderIds = new LinkedHashMap<>();
        for (Map.Entry<Long, BigDecimal> entry : sellerAmounts.entrySet()) {
            SellerOrder sellerOrder = new SellerOrder();
            sellerOrder.setId(idGen.nextId());
            sellerOrder.setOrderId(orderId);
            sellerOrder.setSellerId(entry.getKey());
            sellerOrder.setSellerAmount(entry.getValue());
            sellerOrder.setStatus(0);
            sellerOrder.setCreateTime(LocalDateTime.now());
            sellerOrder.setUpdateTime(LocalDateTime.now());
            sellerOrderMapper.insert(sellerOrder);
            sellerOrderIds.put(entry.getKey(), sellerOrder.getId());
        }
        return sellerOrderIds;
    }

    private SellerOrderVO attachSellerItems(SellerOrderVO sellerOrder, Long sellerId) {
        List<OrderItem> items = orderItemMapper.selectBySellerOrderIdAndSellerId(sellerOrder.getId(), sellerId);
        sellerOrder.setItems(items.stream().map(i -> OrderItemVO.builder()
                .id(i.getId()).spuId(i.getSpuId()).skuId(i.getSkuId())
                .productName(i.getProductName()).productImage(i.getProductImage())
                 .price(i.getPrice()).quantity(i.getQuantity()).totalPrice(i.getTotalPrice())
                  .shippedQuantity(safe(i.getShippedQuantity())).refundedQuantity(safe(i.getRefundedQuantity()))
                  .availableShipQuantity(Math.max(0, i.getQuantity() - safe(i.getShippedQuantity()) - safe(i.getRefundedQuantity())))
                  .availableRefundQuantity(Math.max(0, i.getQuantity() - safe(i.getShippedQuantity()) - safe(i.getRefundedQuantity())))
                 .shippingCompany(i.getShippingCompany()).trackingNo(i.getTrackingNo()).shipTime(i.getShipTime())
                 .build()).toList());
        return sellerOrder;
    }
}
