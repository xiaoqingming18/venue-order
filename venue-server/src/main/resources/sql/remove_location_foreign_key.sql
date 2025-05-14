-- 删除booking_order表上的location_id外键约束
ALTER TABLE `booking_order`
DROP FOREIGN KEY `fk_booking_order_location`;
 
-- 修改location_id字段为允许NULL
ALTER TABLE `booking_order`
MODIFY COLUMN `location_id` bigint(20) NULL COMMENT '场馆位置ID（已弃用）'; 