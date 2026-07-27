-- =============================================
-- mall-user 用户服务建表脚本
-- 数据库: mall
-- =============================================

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id`            BIGINT          AUTO_INCREMENT PRIMARY KEY,
    `username`      VARCHAR(50)     NOT NULL UNIQUE    COMMENT '用户名',
    `password`      VARCHAR(255)    NOT NULL           COMMENT '加密后的密码',
    `nickname`      VARCHAR(50)     DEFAULT NULL       COMMENT '昵称',
    `phone`         VARCHAR(20)     DEFAULT NULL       COMMENT '手机号',
    `email`         VARCHAR(100)    DEFAULT NULL       COMMENT '邮箱',
    `avatar`        VARCHAR(255)    DEFAULT NULL       COMMENT '头像URL',
    `role`          TINYINT         NOT NULL DEFAULT 0 COMMENT '角色: 0-买家 1-卖家 2-管理员',
    `status`        TINYINT         NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    `create_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_phone` (`phone`),
    INDEX `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 收货地址表
CREATE TABLE IF NOT EXISTS `user_address` (
    `id`             BIGINT          AUTO_INCREMENT PRIMARY KEY,
    `user_id`        BIGINT          NOT NULL            COMMENT '用户ID',
    `receiver_name`  VARCHAR(50)     NOT NULL            COMMENT '收货人姓名',
    `receiver_phone` VARCHAR(20)     NOT NULL            COMMENT '收货人电话',
    `province`       VARCHAR(50)     NOT NULL            COMMENT '省份',
    `city`           VARCHAR(50)     NOT NULL            COMMENT '城市',
    `district`       VARCHAR(50)     NOT NULL            COMMENT '区/县',
    `detail`         VARCHAR(255)    NOT NULL            COMMENT '详细地址',
    `is_default`     TINYINT         NOT NULL DEFAULT 0  COMMENT '是否默认: 0-否 1-是',
    `create_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址表';
