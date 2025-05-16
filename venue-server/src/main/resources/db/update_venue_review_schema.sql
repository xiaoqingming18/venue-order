-- 在场馆表中添加评价相关字段
ALTER TABLE `venue` ADD COLUMN `rating` DOUBLE DEFAULT 0 COMMENT '场馆评分';
ALTER TABLE `venue` ADD COLUMN `review_count` INT DEFAULT 0 COMMENT '评价次数'; 