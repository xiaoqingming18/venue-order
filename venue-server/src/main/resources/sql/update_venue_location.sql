-- 为venue_location表添加price字段
ALTER TABLE `venue_location` 
ADD COLUMN `price` decimal(10, 2) NULL DEFAULT NULL COMMENT '位置单独价格，为空则使用场馆基准价格' AFTER `status`; 