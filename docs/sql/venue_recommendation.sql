-- ----------------------------
-- Table structure for user_browse_history
-- ----------------------------
CREATE TABLE `user_browse_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '历史记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `venue_id` bigint NOT NULL COMMENT '场馆ID',
  `browse_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
  `stay_duration` int NULL DEFAULT 0 COMMENT '停留时长(秒)',
  `source` tinyint NOT NULL DEFAULT 0 COMMENT '来源: 0-首页推荐, 1-搜索结果, 2-分类浏览, 3-其他场馆跳转',
  `device_info` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备信息',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id` (`user_id` ASC) USING BTREE,
  INDEX `idx_venue_id` (`venue_id` ASC) USING BTREE,
  INDEX `idx_browse_time` (`browse_time` ASC) USING BTREE,
  CONSTRAINT `fk_history_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_history_venue` FOREIGN KEY (`venue_id`) REFERENCES `venue` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户浏览历史表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for venue_tag
-- ----------------------------
CREATE TABLE `venue_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标签名称',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标签分类',
  `weight` int NOT NULL DEFAULT 1 COMMENT '权重(1-10)',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tag_name` (`name` ASC) USING BTREE,
  INDEX `idx_category` (`category` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '场馆标签表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for venue_tag_relation
-- ----------------------------
CREATE TABLE `venue_tag_relation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关系ID',
  `venue_id` bigint NOT NULL COMMENT '场馆ID',
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_venue_tag` (`venue_id` ASC, `tag_id` ASC) USING BTREE,
  INDEX `idx_tag_id` (`tag_id` ASC) USING BTREE,
  CONSTRAINT `fk_relation_venue` FOREIGN KEY (`venue_id`) REFERENCES `venue` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_relation_tag` FOREIGN KEY (`tag_id`) REFERENCES `venue_tag` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '场馆标签关系表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user_preference
-- ----------------------------
CREATE TABLE `user_preference` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '偏好ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  `preference_score` decimal(5, 2) NOT NULL DEFAULT 0.00 COMMENT '偏好分数(0-10)',
  `source` tinyint NOT NULL DEFAULT 0 COMMENT '来源: 0-系统计算, 1-用户设置, 2-推荐反馈',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_tag` (`user_id` ASC, `tag_id` ASC) USING BTREE,
  INDEX `idx_tag_id` (`tag_id` ASC) USING BTREE,
  CONSTRAINT `fk_preference_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_preference_tag` FOREIGN KEY (`tag_id`) REFERENCES `venue_tag` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户偏好表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user_venue_preference
-- ----------------------------
CREATE TABLE `user_venue_preference` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `venue_id` bigint NOT NULL COMMENT '场馆ID',
  `preference_score` decimal(5, 2) NOT NULL DEFAULT 0.00 COMMENT '偏好分数(0-10)',
  `browse_count` int NOT NULL DEFAULT 0 COMMENT '浏览次数',
  `order_count` int NOT NULL DEFAULT 0 COMMENT '预订次数',
  `last_interaction` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后交互时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_venue` (`user_id` ASC, `venue_id` ASC) USING BTREE,
  INDEX `idx_venue_id` (`venue_id` ASC) USING BTREE,
  INDEX `idx_score` (`preference_score` DESC) USING BTREE,
  CONSTRAINT `fk_venue_pref_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_venue_pref_venue` FOREIGN KEY (`venue_id`) REFERENCES `venue` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户-场馆偏好表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for venue_similarity
-- ----------------------------
CREATE TABLE `venue_similarity` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `venue_id` bigint NOT NULL COMMENT '场馆ID',
  `similar_venue_id` bigint NOT NULL COMMENT '相似场馆ID',
  `similarity_score` decimal(5, 2) NOT NULL DEFAULT 0.00 COMMENT '相似度分数(0-1)',
  `calculation_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '计算时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_venue_pair` (`venue_id` ASC, `similar_venue_id` ASC) USING BTREE,
  INDEX `idx_similar_venue` (`similar_venue_id` ASC) USING BTREE,
  INDEX `idx_similarity` (`similarity_score` DESC) USING BTREE,
  CONSTRAINT `fk_similarity_venue1` FOREIGN KEY (`venue_id`) REFERENCES `venue` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_similarity_venue2` FOREIGN KEY (`similar_venue_id`) REFERENCES `venue` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '场馆相似度表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for venue_recommendation
-- ----------------------------
CREATE TABLE `venue_recommendation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '推荐ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `venue_id` bigint NOT NULL COMMENT '推荐场馆ID',
  `recommendation_type` tinyint NOT NULL DEFAULT 0 COMMENT '推荐类型: 0-基于用户行为, 1-基于相似用户, 2-热门推荐, 3-新场馆推荐',
  `recommendation_score` decimal(5, 2) NOT NULL DEFAULT 0.00 COMMENT '推荐分数(0-10)',
  `is_shown` tinyint NOT NULL DEFAULT 0 COMMENT '是否已展示: 0-未展示, 1-已展示',
  `is_clicked` tinyint NOT NULL DEFAULT 0 COMMENT '是否已点击: 0-未点击, 1-已点击',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_venue_rec` (`user_id` ASC, `venue_id` ASC) USING BTREE,
  INDEX `idx_venue_id` (`venue_id` ASC) USING BTREE,
  INDEX `idx_rec_score` (`recommendation_score` DESC) USING BTREE,
  INDEX `idx_rec_type` (`recommendation_type` ASC) USING BTREE,
  CONSTRAINT `fk_recommendation_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_recommendation_venue` FOREIGN KEY (`venue_id`) REFERENCES `venue` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '场馆推荐表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for recommendation_feedback
-- ----------------------------
CREATE TABLE `recommendation_feedback` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '反馈ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `venue_id` bigint NOT NULL COMMENT '场馆ID',
  `recommendation_id` bigint NULL DEFAULT NULL COMMENT '推荐记录ID',
  `feedback_type` tinyint NOT NULL DEFAULT 0 COMMENT '反馈类型: 0-喜欢, 1-不感兴趣, 2-不再推荐此类场馆',
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '反馈原因',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id` (`user_id` ASC) USING BTREE,
  INDEX `idx_venue_id` (`venue_id` ASC) USING BTREE,
  INDEX `idx_recommendation` (`recommendation_id` ASC) USING BTREE,
  CONSTRAINT `fk_rec_feedback_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_rec_feedback_venue` FOREIGN KEY (`venue_id`) REFERENCES `venue` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_rec_feedback_recommendation` FOREIGN KEY (`recommendation_id`) REFERENCES `venue_recommendation` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '推荐反馈表' ROW_FORMAT = DYNAMIC; 