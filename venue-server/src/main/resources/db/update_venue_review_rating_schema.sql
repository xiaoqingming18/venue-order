-- 在场馆评价表中添加综合评分展示字段
ALTER TABLE `venue_review` ADD COLUMN `rating` DOUBLE DEFAULT 0 COMMENT '综合评分展示'; 