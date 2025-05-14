-- 为booking_order表添加location_id字段
ALTER TABLE `booking_order` 
ADD COLUMN `location_id` bigint(20) NULL COMMENT '场馆位置ID' AFTER `venue_id`,
ADD INDEX `idx_location_id` (`location_id`);

-- 添加外键约束
ALTER TABLE `booking_order`
ADD CONSTRAINT `fk_booking_order_location` FOREIGN KEY (`location_id`) REFERENCES `venue_location` (`id`); 