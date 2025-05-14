-- 为venue_facility表添加price字段
ALTER TABLE `venue_facility` 
ADD COLUMN `price` decimal(10, 2) NULL DEFAULT NULL COMMENT '设施单独价格，为空则使用场馆基准价格' AFTER `position_desc`;

-- 更新索引
ALTER TABLE `venue_facility` 
ADD INDEX `idx_facility_type` (`facility_type`); 