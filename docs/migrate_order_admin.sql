-- Execute once before using administrator order shipping features.
ALTER TABLE order_t
    ADD COLUMN shipping_company VARCHAR(64) NULL,
    ADD COLUMN tracking_no VARCHAR(64) NULL,
    ADD COLUMN ship_time DATETIME NULL,
    ADD COLUMN receive_time DATETIME NULL,
    ADD INDEX idx_order_status_create_time (status, create_time);
