-- 用户浏览历史表
DROP TABLE IF EXISTS `user_browse_history`;
CREATE TABLE `user_browse_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '历史记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `venue_id` bigint NOT NULL COMMENT '场馆ID',
  `browse_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
  `stay_duration` int DEFAULT 0 COMMENT '停留时长(秒)',
  `source` tinyint DEFAULT 0 COMMENT '来源: 0-首页推荐, 1-搜索结果, 2-分类浏览, 3-其他场馆跳转',
  `device_info` varchar(500) DEFAULT NULL COMMENT '设备信息',
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_venue_id` (`venue_id`),
  INDEX `idx_browse_time` (`browse_time`),
  INDEX `idx_user_venue` (`user_id`, `venue_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户浏览历史表';

-- 用户场馆偏好表
DROP TABLE IF EXISTS `user_venue_preference`;
CREATE TABLE `user_venue_preference` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `venue_id` bigint NOT NULL COMMENT '场馆ID',
  `preference_score` decimal(3,1) DEFAULT 0.0 COMMENT '偏好分数(0-10)',
  `browse_count` int DEFAULT 0 COMMENT '浏览次数',
  `order_count` int DEFAULT 0 COMMENT '预订次数',
  `last_interaction` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后交互时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_user_venue` (`user_id`, `venue_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_venue_id` (`venue_id`),
  INDEX `idx_preference_score` (`preference_score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户场馆偏好表';

-- 场馆推荐表
DROP TABLE IF EXISTS `venue_recommendation`;
CREATE TABLE `venue_recommendation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '推荐ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `venue_id` bigint NOT NULL COMMENT '推荐场馆ID',
  `recommendation_type` tinyint DEFAULT 0 COMMENT '推荐类型: 0-基于用户行为, 1-基于相似用户, 2-热门推荐, 3-新场馆推荐',
  `recommendation_score` decimal(3,1) DEFAULT 0.0 COMMENT '推荐分数(0-10)',
  `is_shown` tinyint DEFAULT 0 COMMENT '是否已展示: 0-未展示, 1-已展示',
  `is_clicked` tinyint DEFAULT 0 COMMENT '是否已点击: 0-未点击, 1-已点击',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_venue_id` (`venue_id`),
  INDEX `idx_recommendation_score` (`recommendation_score`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场馆推荐表';

-- 推荐反馈表
DROP TABLE IF EXISTS `recommendation_feedback`;
CREATE TABLE `recommendation_feedback` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '反馈ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `venue_id` bigint NOT NULL COMMENT '场馆ID',
  `recommendation_id` bigint DEFAULT NULL COMMENT '推荐ID',
  `feedback_type` tinyint NOT NULL COMMENT '反馈类型: 1-喜欢, 2-不喜欢',
  `reason` varchar(200) DEFAULT NULL COMMENT '原因(如果是不喜欢)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_venue_id` (`venue_id`),
  INDEX `idx_recommendation_id` (`recommendation_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推荐反馈表'; 