-- 在订单表中添加积分相关字段
ALTER TABLE `booking_order` ADD COLUMN `points_used` INT DEFAULT 0 COMMENT '积分使用数量';
ALTER TABLE `booking_order` ADD COLUMN `points_discount` DECIMAL(10, 2) DEFAULT 0 COMMENT '积分抵扣金额';

-- 添加索引
ALTER TABLE `user_points` ADD INDEX `idx_update_time` (`update_time`);
ALTER TABLE `point_records` ADD INDEX `idx_source_type` (`source_type`);
ALTER TABLE `point_records` ADD INDEX `idx_source_id` (`source_id`); 