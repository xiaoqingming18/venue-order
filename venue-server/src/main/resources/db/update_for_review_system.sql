-- 场馆表添加评分相关字段
ALTER TABLE `venue` ADD COLUMN `rating` DOUBLE DEFAULT 0 COMMENT '场馆评分';
ALTER TABLE `venue` ADD COLUMN `review_count` INT DEFAULT 0 COMMENT '评价次数';

-- 场馆评价表添加评分展示字段
ALTER TABLE `venue_review` ADD COLUMN `rating` DOUBLE DEFAULT 0 COMMENT '综合评分展示'; 