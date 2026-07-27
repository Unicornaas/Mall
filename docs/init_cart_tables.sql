-- 购物车表（mall_cart 数据库）
CREATE TABLE IF NOT EXISTS `cart` (
    `id`          BIGINT       NOT NULL COMMENT '主键ID',
    `user_id`     BIGINT       NOT NULL COMMENT '用户ID',
    `sku_id`      BIGINT       NOT NULL COMMENT '商品SKU ID',
    `quantity`    INT          NOT NULL DEFAULT 1 COMMENT '数量',
    `selected`    TINYINT      NOT NULL DEFAULT 1 COMMENT '是否选中：0-未选中 1-已选中',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_user_sku` (`user_id`, `sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';
