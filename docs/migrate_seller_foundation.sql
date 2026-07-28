-- ============================================================================
-- Mall 商家端第一阶段：数据归属基础迁移脚本
-- 适用数据库：mall（MySQL 8.x）
-- 执行原则：先执行本脚本，再重启 mall-user、mall-product、mall-order 服务。
-- 可重复执行：列和索引会先检查是否已存在；历史数据统一标记为 seller_id = 0（平台自营）。
-- ============================================================================

-- 1. 一个商家账号对应一个店铺
CREATE TABLE IF NOT EXISTS `shop_t` (
    `id`            BIGINT          PRIMARY KEY,
    `seller_id`     BIGINT          NOT NULL           COMMENT '商家用户ID',
    `shop_name`     VARCHAR(128)    NOT NULL           COMMENT '店铺名称',
    `logo`          VARCHAR(500)    DEFAULT NULL       COMMENT '店铺Logo URL',
    `contact_name`  VARCHAR(64)     DEFAULT NULL       COMMENT '联系人',
    `contact_phone` VARCHAR(32)     DEFAULT NULL       COMMENT '联系电话',
    `description`   VARCHAR(500)    DEFAULT NULL       COMMENT '店铺简介',
    `status`        TINYINT         NOT NULL DEFAULT 1 COMMENT '状态: 0-停用 1-正常 2-待审核',
    `create_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_shop_seller_id` (`seller_id`),
    INDEX `idx_shop_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家店铺表';

-- 2. 为已有商家补建默认店铺。shop_t.id 复用 seller_id，仅用于历史迁移；新注册商家仍使用雪花 ID。
INSERT INTO shop_t (id, seller_id, shop_name, contact_name, contact_phone, status, create_time, update_time)
SELECT id,
       id,
       CONCAT(username, '的店铺'),
       COALESCE(nickname, username),
       phone,
       1,
       NOW(),
       NOW()
FROM user_t
WHERE role = 1
ON DUPLICATE KEY UPDATE seller_id = VALUES(seller_id);

-- 3. 为商品 SPU 增加所属商家字段。已有商品归属平台自营（seller_id = 0）。
SET @column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'product_spu' AND column_name = 'seller_id'
);
SET @ddl = IF(@column_exists = 0,
    'ALTER TABLE product_spu ADD COLUMN seller_id BIGINT NOT NULL DEFAULT 0 COMMENT ''所属商家用户ID，0表示平台自营'' AFTER id',
    'SELECT 1');
PREPARE seller_foundation_stmt FROM @ddl;
EXECUTE seller_foundation_stmt;
DEALLOCATE PREPARE seller_foundation_stmt;

UPDATE product_spu SET seller_id = 0 WHERE seller_id IS NULL;

SET @index_exists = (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'product_spu' AND index_name = 'idx_spu_seller_status'
);
SET @ddl = IF(@index_exists = 0,
    'CREATE INDEX idx_spu_seller_status ON product_spu (seller_id, status)',
    'SELECT 1');
PREPARE seller_foundation_stmt FROM @ddl;
EXECUTE seller_foundation_stmt;
DEALLOCATE PREPARE seller_foundation_stmt;

-- 4. 为订单项固化商品所属商家，后续商家订单查询不再依赖商品当前归属。
SET @column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'order_item' AND column_name = 'seller_id'
);
SET @ddl = IF(@column_exists = 0,
    'ALTER TABLE order_item ADD COLUMN seller_id BIGINT NOT NULL DEFAULT 0 COMMENT ''下单时商品所属商家用户ID，0表示平台自营'' AFTER order_id',
    'SELECT 1');
PREPARE seller_foundation_stmt FROM @ddl;
EXECUTE seller_foundation_stmt;
DEALLOCATE PREPARE seller_foundation_stmt;

-- 已存在的历史订单统一标记为平台自营；不要根据当前商品归属回填，避免历史订单归属被篡改。
UPDATE order_item SET seller_id = 0 WHERE seller_id IS NULL;

SET @index_exists = (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'order_item' AND index_name = 'idx_order_item_seller_order'
);
SET @ddl = IF(@index_exists = 0,
    'CREATE INDEX idx_order_item_seller_order ON order_item (seller_id, order_id)',
    'SELECT 1');
PREPARE seller_foundation_stmt FROM @ddl;
EXECUTE seller_foundation_stmt;
DEALLOCATE PREPARE seller_foundation_stmt;

-- 5. 执行后核对结果：应不存在 seller_id 为 NULL 的商品或订单项。
SELECT 'shop_t' AS check_name, COUNT(*) AS total FROM shop_t;
SELECT 'product_spu_without_seller' AS check_name, COUNT(*) AS total FROM product_spu WHERE seller_id IS NULL;
SELECT 'order_item_without_seller' AS check_name, COUNT(*) AS total FROM order_item WHERE seller_id IS NULL;
