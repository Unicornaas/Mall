-- Execute once before using administrator payment and refund management.
ALTER TABLE payment_refund
    ADD COLUMN processor_id BIGINT NULL COMMENT '处理管理员ID',
    ADD COLUMN process_remark VARCHAR(500) NULL COMMENT '处理备注',
    ADD COLUMN process_time DATETIME NULL COMMENT '处理时间',
    ADD INDEX idx_refund_status_create_time (refund_status, create_time);
