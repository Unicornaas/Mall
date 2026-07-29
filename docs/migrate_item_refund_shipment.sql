-- Run once after the existing seller-order and payment migrations.
-- This migration adds item-level refund and shipment state without removing
-- the existing master-order and seller-order tables.

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE table_schema = DATABASE() AND table_name = 'order_item'
       AND column_name = 'shipped_quantity') = 0,
    'ALTER TABLE order_item ADD COLUMN shipped_quantity INT NOT NULL DEFAULT 0 COMMENT ''quantity already shipped'' AFTER quantity',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE table_schema = DATABASE() AND table_name = 'order_item'
       AND column_name = 'refunded_quantity') = 0,
    'ALTER TABLE order_item ADD COLUMN refunded_quantity INT NOT NULL DEFAULT 0 COMMENT ''quantity already refunded'' AFTER shipped_quantity',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE table_schema = DATABASE() AND table_name = 'order_item'
       AND column_name = 'shipping_company') = 0,
    'ALTER TABLE order_item ADD COLUMN shipping_company VARCHAR(64) NULL COMMENT ''item shipment company'' AFTER refunded_quantity',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE table_schema = DATABASE() AND table_name = 'order_item'
       AND column_name = 'tracking_no') = 0,
    'ALTER TABLE order_item ADD COLUMN tracking_no VARCHAR(64) NULL COMMENT ''item tracking number'' AFTER shipping_company',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE table_schema = DATABASE() AND table_name = 'order_item'
       AND column_name = 'ship_time') = 0,
    'ALTER TABLE order_item ADD COLUMN ship_time DATETIME NULL COMMENT ''item shipment time'' AFTER tracking_no',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE table_schema = DATABASE() AND table_name = 'payment_info'
       AND column_name = 'refund_amount') = 0,
    'ALTER TABLE payment_info ADD COLUMN refund_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT ''cumulative refunded amount'' AFTER amount',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS payment_refund_item (
    id              BIGINT        NOT NULL,
    refund_id       BIGINT        NOT NULL,
    order_item_id   BIGINT        NOT NULL,
    seller_order_id BIGINT        NULL,
    sku_id          BIGINT        NOT NULL,
    product_name    VARCHAR(255)  NOT NULL,
    price           DECIMAL(10,2) NOT NULL,
    quantity        INT           NOT NULL,
    refund_amount   DECIMAL(10,2) NOT NULL,
    item_status     TINYINT       NOT NULL DEFAULT 0 COMMENT 'same status as payment_refund',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_refund_item_refund_id (refund_id),
    KEY idx_refund_item_order_item_id (order_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Preserve shipment state for orders created before item-level shipment existed.
UPDATE order_item oi
INNER JOIN seller_order_t so ON so.id = oi.seller_order_id
SET oi.shipped_quantity = oi.quantity,
    oi.shipping_company = so.shipping_company,
    oi.tracking_no = so.tracking_no,
    oi.ship_time = so.ship_time
WHERE so.status IN (2, 3) AND COALESCE(oi.shipped_quantity, 0) = 0;

UPDATE payment_info pi
SET pi.refund_amount = (
    SELECT COALESCE(SUM(pr.refund_amount), 0)
    FROM payment_refund pr
    WHERE pr.order_no = pi.order_no AND pr.refund_status = 1
)
WHERE EXISTS (SELECT 1 FROM payment_refund pr2
              WHERE pr2.order_no = pi.order_no AND pr2.refund_status = 1);

-- Existing rows represent full-order refund requests. They remain valid;
-- newly submitted requests will always contain one or more item rows.
