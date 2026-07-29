-- 商家子订单迁移：支持一次结算多个商家商品后按店铺独立发货。
-- 请在订单服务和支付服务升级前执行。脚本可重复执行。

CREATE TABLE IF NOT EXISTS seller_order_t (
    id               BIGINT       NOT NULL COMMENT '商家子订单ID',
    order_id         BIGINT       NOT NULL COMMENT '买家主订单ID',
    seller_id        BIGINT       NOT NULL COMMENT '商家用户ID',
    seller_amount    DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '该商家商品金额',
    status           TINYINT      NOT NULL DEFAULT 0 COMMENT '0待支付 1已支付 2已发货 3已完成 4已取消',
    shipping_company VARCHAR(64)  NULL COMMENT '物流公司',
    tracking_no      VARCHAR(64)  NULL COMMENT '运单号',
    ship_time        DATETIME     NULL COMMENT '发货时间',
    receive_time     DATETIME     NULL COMMENT '收货时间',
    create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_seller_order_order_seller (order_id, seller_id),
    KEY idx_seller_order_seller_status_time (seller_id, status, create_time),
    KEY idx_seller_order_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家子订单表';

-- 为订单项增加子订单归属。使用 information_schema 保证重复执行安全。
SET @has_seller_order_id := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE table_schema = DATABASE() AND table_name = 'order_item' AND column_name = 'seller_order_id'
);
SET @add_seller_order_id_sql := IF(
    @has_seller_order_id = 0,
    'ALTER TABLE order_item ADD COLUMN seller_order_id BIGINT NULL COMMENT ''商家子订单ID'' AFTER seller_id',
    'SELECT 1'
);
PREPARE stmt FROM @add_seller_order_id_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_seller_order_item_idx := (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE table_schema = DATABASE() AND table_name = 'order_item' AND index_name = 'idx_order_item_seller_order_id'
);
SET @add_seller_order_item_idx_sql := IF(
    @has_seller_order_item_idx = 0,
    'CREATE INDEX idx_order_item_seller_order_id ON order_item (seller_order_id)',
    'SELECT 1'
);
PREPARE stmt FROM @add_seller_order_item_idx_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 将历史订单按 seller_id 回填为商家子订单。历史主订单物流信息会保留到对应子订单。
INSERT INTO seller_order_t (id, order_id, seller_id, seller_amount, status, shipping_company, tracking_no, ship_time, receive_time, create_time, update_time)
SELECT UUID_SHORT(), oi.order_id, oi.seller_id, SUM(oi.total_price), o.status,
       o.shipping_company, o.tracking_no, o.ship_time, o.receive_time, o.create_time, o.update_time
FROM order_item oi
INNER JOIN order_t o ON o.id = oi.order_id
LEFT JOIN seller_order_t existing ON existing.order_id = oi.order_id AND existing.seller_id = oi.seller_id
WHERE existing.id IS NULL
GROUP BY oi.order_id, oi.seller_id, o.status, o.shipping_company, o.tracking_no, o.ship_time, o.receive_time, o.create_time, o.update_time;

UPDATE order_item oi
INNER JOIN seller_order_t so ON so.order_id = oi.order_id AND so.seller_id = oi.seller_id
SET oi.seller_order_id = so.id
WHERE oi.seller_order_id IS NULL;
