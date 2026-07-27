-- 库存表（mall_inventory 数据库）
CREATE TABLE IF NOT EXISTS `inventory` (
    `id`              BIGINT   NOT NULL COMMENT '主键ID',
    `sku_id`          BIGINT   NOT NULL COMMENT '商品SKU ID',
    `total_stock`     INT      NOT NULL DEFAULT 0 COMMENT '总库存',
    `locked_stock`    INT      NOT NULL DEFAULT 0 COMMENT '锁定库存（预占未扣减）',
    `available_stock` INT      NOT NULL DEFAULT 0 COMMENT '可用库存',
    `safety_stock`    INT      NOT NULL DEFAULT 10 COMMENT '安全库存（预警阈值）',
    `create_time`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_sku_id` (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存表';

-- 库存变更日志表
CREATE TABLE IF NOT EXISTS `inventory_log` (
    `id`           BIGINT       NOT NULL COMMENT '主键ID',
    `sku_id`       BIGINT       NOT NULL COMMENT '商品SKU ID',
    `order_no`     VARCHAR(32)  DEFAULT NULL COMMENT '关联订单号',
    `change_type`  VARCHAR(20)  NOT NULL COMMENT '变更类型：LOCK-预占 DEDUCT-扣减 RELEASE-释放 ADD-补货 INIT-初始化',
    `change_count` INT          NOT NULL COMMENT '变更数量',
    `before_stock` INT          NOT NULL COMMENT '变更前库存',
    `after_stock`  INT          NOT NULL COMMENT '变更后库存',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_sku_id` (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存变更日志表';

-- 支付信息表（mall_payment 数据库）
CREATE TABLE IF NOT EXISTS `payment_info` (
    `id`          BIGINT         NOT NULL COMMENT '主键ID',
    `order_no`    VARCHAR(32)    NOT NULL COMMENT '订单号',
    `user_id`     BIGINT         NOT NULL COMMENT '用户ID',
    `amount`      DECIMAL(10,2)  NOT NULL DEFAULT 0.00 COMMENT '支付金额',
    `pay_type`    TINYINT        NOT NULL DEFAULT 1 COMMENT '支付方式：1-支付宝 2-微信',
    `pay_status`  TINYINT        NOT NULL DEFAULT 0 COMMENT '支付状态：0-待支付 1-已支付 2-已退款 3-已关闭',
    `trade_no`    VARCHAR(64)    DEFAULT NULL COMMENT '第三方交易号',
    `pay_time`    DATETIME       DEFAULT NULL COMMENT '支付时间',
    `create_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付信息表';

-- 退款记录表
CREATE TABLE IF NOT EXISTS `payment_refund` (
    `id`            BIGINT         NOT NULL COMMENT '主键ID',
    `order_no`      VARCHAR(32)    NOT NULL COMMENT '订单号',
    `user_id`       BIGINT         NOT NULL COMMENT '用户ID',
    `refund_amount` DECIMAL(10,2)  NOT NULL DEFAULT 0.00 COMMENT '退款金额',
    `refund_status` TINYINT        NOT NULL DEFAULT 0 COMMENT '退款状态：0-待处理 1-已退款 2-已拒绝',
    `reason`        VARCHAR(500)   DEFAULT NULL COMMENT '退款原因',
    `create_time`   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退款记录表';
