/*
 Navicat Premium Dump SQL

 Source Server         : ershou
 Source Server Type    : MySQL
 Source Server Version : 80035 (8.0.35)
 Source Host           : localhost:3306
 Source Schema         : venue

 Target Server Type    : MySQL
 Target Server Version : 80035 (8.0.35)
 File Encoding         : 65001

 Date: 17/05/2025 01:07:18
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for booking_config
-- ----------------------------
DROP TABLE IF EXISTS `booking_config`;
CREATE TABLE `booking_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `venue_id` bigint NOT NULL COMMENT '场馆ID',
  `advance_booking_days` int NOT NULL DEFAULT 7 COMMENT '提前预约天数',
  `max_booking_days` int NOT NULL DEFAULT 30 COMMENT '最大可预约未来天数',
  `cancel_ahead_hours` int NOT NULL DEFAULT 24 COMMENT '提前取消小时数',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uq_venue_id`(`venue_id` ASC) USING BTREE,
  CONSTRAINT `fk_config_venue` FOREIGN KEY (`venue_id`) REFERENCES `venue` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '预约配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of booking_config
-- ----------------------------

-- ----------------------------
-- Table structure for booking_facility
-- ----------------------------
DROP TABLE IF EXISTS `booking_facility`;
CREATE TABLE `booking_facility`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '预约设施ID',
  `order_id` bigint NOT NULL COMMENT '关联的订单ID',
  `facility_id` bigint NOT NULL COMMENT '设施ID',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '预约数量',
  `unit_price` decimal(10, 2) NOT NULL COMMENT '单价',
  `amount` decimal(10, 2) NOT NULL COMMENT '金额小计',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_facility_id`(`facility_id` ASC) USING BTREE,
  CONSTRAINT `fk_booking_facility` FOREIGN KEY (`facility_id`) REFERENCES `venue_facility` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_booking_order` FOREIGN KEY (`order_id`) REFERENCES `booking_order` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '预约订单设施明细表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of booking_facility
-- ----------------------------

-- ----------------------------
-- Table structure for booking_order
-- ----------------------------
DROP TABLE IF EXISTS `booking_order`;
CREATE TABLE `booking_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '预约订单ID',
  `order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单编号',
  `user_id` bigint NOT NULL COMMENT '预约用户ID',
  `venue_id` bigint NOT NULL COMMENT '场馆ID',
  `location_id` bigint NULL DEFAULT NULL COMMENT '场馆位置ID（已弃用）',
  `booking_date` date NOT NULL COMMENT '预约日期',
  `start_time` time NOT NULL COMMENT '开始时间',
  `end_time` time NOT NULL COMMENT '结束时间',
  `booking_type` tinyint NOT NULL COMMENT '预约类型：1-包场预约，2-设施预约',
  `total_amount` decimal(10, 2) NOT NULL COMMENT '订单总金额',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '订单状态：0-已取消，1-待支付，2-已预约，3-已完成',
  `remarks` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注信息',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uq_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_venue_id`(`venue_id` ASC) USING BTREE,
  INDEX `idx_booking_date`(`booking_date` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_location_id`(`location_id` ASC) USING BTREE,
  CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_order_venue` FOREIGN KEY (`venue_id`) REFERENCES `venue` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '预约订单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of booking_order
-- ----------------------------
INSERT INTO `booking_order` VALUES (4, '202505121858142abdc5', 10004, 2, NULL, '2025-05-13', '08:00:00', '09:00:00', 2, 12.00, 0, '123', '2025-05-12 18:58:14', '2025-05-12 19:06:40');
INSERT INTO `booking_order` VALUES (5, '20250513110441dfbde6', 10004, 2, NULL, '2025-05-16', '08:00:00', '09:00:00', 2, 12.00, 2, '要助教', '2025-05-13 11:04:42', '2025-05-13 11:35:29');
INSERT INTO `booking_order` VALUES (6, '20250513120704846e17', 10006, 2, NULL, '2025-05-13', '10:00:00', '11:00:00', 2, 12.00, 3, '要两助教', '2025-05-13 12:07:05', '2025-05-13 15:42:31');
INSERT INTO `booking_order` VALUES (7, '20250515095932e8fbe7', 10006, 2, NULL, '2025-05-16', '08:00:00', '09:00:00', 2, 12.00, 2, '要三个助教妹妹', '2025-05-15 09:59:32', '2025-05-15 11:27:25');

-- ----------------------------
-- Table structure for booking_rule
-- ----------------------------
DROP TABLE IF EXISTS `booking_rule`;
CREATE TABLE `booking_rule`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `rule_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规则类型',
  `rule_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规则键',
  `rule_value` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规则值',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '规则描述',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `created_at` datetime NOT NULL COMMENT '创建时间',
  `updated_at` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_rule_type_key`(`rule_type` ASC, `rule_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '预约规则表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of booking_rule
-- ----------------------------
INSERT INTO `booking_rule` VALUES (1, 'booking_limit', 'max_daily_bookings', '2', '每名用户每天最多只能预约两条', 1, '2025-05-12 19:36:03', '2025-05-12 19:36:03');

-- ----------------------------
-- Table structure for price_strategy
-- ----------------------------
DROP TABLE IF EXISTS `price_strategy`;
CREATE TABLE `price_strategy`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '策略ID',
  `venue_id` bigint NOT NULL COMMENT '场馆ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '策略名称',
  `day_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '日期类型：WORKDAY-工作日, WEEKEND-周末, HOLIDAY-节假日',
  `price_rate` decimal(3, 2) NOT NULL DEFAULT 1.00 COMMENT '价格系数，默认为1',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-不启用，1-启用',
  `start_date` date NULL DEFAULT NULL COMMENT '策略开始日期，可为空',
  `end_date` date NULL DEFAULT NULL COMMENT '策略结束日期，可为空',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '策略描述',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_venue_id`(`venue_id` ASC) USING BTREE,
  CONSTRAINT `fk_strategy_venue` FOREIGN KEY (`venue_id`) REFERENCES `venue` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '价格策略表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of price_strategy
-- ----------------------------

-- ----------------------------
-- Table structure for special_date_rule
-- ----------------------------
DROP TABLE IF EXISTS `special_date_rule`;
CREATE TABLE `special_date_rule`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `special_date` date NOT NULL COMMENT '特殊日期',
  `description` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `price_rate` decimal(10, 2) NOT NULL DEFAULT 1.00 COMMENT '价格系数',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-不可预约，1-可预约',
  `created_at` datetime NOT NULL COMMENT '创建时间',
  `updated_at` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_special_date`(`special_date` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '特殊日期规则表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of special_date_rule
-- ----------------------------

-- ----------------------------
-- Table structure for time_slot
-- ----------------------------
DROP TABLE IF EXISTS `time_slot`;
CREATE TABLE `time_slot`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '时间段ID',
  `venue_id` bigint NOT NULL COMMENT '场馆ID',
  `start_time` time NOT NULL COMMENT '开始时间',
  `end_time` time NOT NULL COMMENT '结束时间',
  `price_rate` decimal(3, 2) NOT NULL DEFAULT 1.00 COMMENT '价格系数，默认为1',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-不可用，1-可用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_venue_id`(`venue_id` ASC) USING BTREE,
  CONSTRAINT `fk_time_slot_venue` FOREIGN KEY (`venue_id`) REFERENCES `venue` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '时间段设置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of time_slot
-- ----------------------------

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '加密后的密码',
  `salt` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码加密盐值',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '电子邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '手机号码',
  `nickname` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户昵称',
  `avatar` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户头像URL',
  `gender` enum('male','female','other') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'other' COMMENT '性别',
  `birthday` date NULL DEFAULT NULL COMMENT '生日',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '地址',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '用户状态：0-禁用，1-正常',
  `role` tinyint NOT NULL DEFAULT 1 COMMENT '用户角色：1-普通用户，2-管理员',
  `login_fail_count` int NOT NULL DEFAULT 0 COMMENT '连续登录失败次数',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '最后登录IP',
  `register_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `register_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '注册IP',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `idx_email`(`email` ASC) USING BTREE,
  UNIQUE INDEX `idx_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_role`(`role` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10007 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (10003, 'admin', '$2a$10$.UP8DmC3WB9nJ36FyFhZzOVxSNsaynaZsrFQwfaIrmoZsA61WImRu', 'gvmsrph93m1qwhk9', 'xiaoqingming18@163.com', '15556916571', '小吊乐管理员', NULL, 'other', NULL, NULL, 1, 2, 0, '2025-05-17 00:08:02', '127.0.0.1', '2025-05-07 18:51:03', '127.0.0.1', '2025-05-07 18:51:02');
INSERT INTO `user` VALUES (10004, 'xiaoqingming18', '$2a$10$K2Qoj/T9LjLv2JgcrngWz.vPpnn3r/1pDJ8KcA3xUUXgsSBoA9H8.', 'av1oqdkw8t2fa7lc', 'testuser@test.com', '15567675656', '小清明十八', NULL, 'other', NULL, NULL, 1, 1, 2, '2025-05-13 16:56:06', '127.0.0.1', '2025-05-09 15:50:50', '127.0.0.1', '2025-05-09 15:50:50');
INSERT INTO `user` VALUES (10005, 'xiaoqingming', '$2a$10$jkbmg58MIHdtfYCrd28JwuZcHq6Gdu36neC817CfeKW2d/npmuLqG', '2u9zcazmzvss51v5', 'testuser2@test.com', '15567675653', '小清明十八', NULL, 'other', NULL, NULL, 1, 1, 0, '2025-05-12 20:14:21', '127.0.0.1', '2025-05-12 20:14:20', '127.0.0.1', '2025-05-12 20:14:20');
INSERT INTO `user` VALUES (10006, 'BirdSuper', '$2a$10$u3aFZA16Oph/bMuOaTz12ejESZjjB8I2aU.Z0hgwdFcQbuBOebkMm', 'vt0ddfjbiun64txl', '31142582@qq.com', '18326381722', '鸟超', NULL, 'other', NULL, NULL, 1, 1, 0, '2025-05-17 00:05:35', '127.0.0.1', '2025-05-13 12:06:24', '127.0.0.1', '2025-05-13 12:06:23');

-- ----------------------------
-- Table structure for user_favorite_venue
-- ----------------------------
DROP TABLE IF EXISTS `user_favorite_venue`;
CREATE TABLE `user_favorite_venue`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `venue_id` bigint NOT NULL COMMENT '场馆ID',
  `notes` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收藏备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_user_venue`(`user_id` ASC, `venue_id` ASC) USING BTREE COMMENT '用户和场馆的唯一组合索引',
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_venue_id`(`venue_id` ASC) USING BTREE,
  CONSTRAINT `fk_favorite_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_favorite_venue` FOREIGN KEY (`venue_id`) REFERENCES `venue` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户收藏场馆表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_favorite_venue
-- ----------------------------
INSERT INTO `user_favorite_venue` VALUES (2, 10006, 2, NULL, '2025-05-17 01:01:18', '2025-05-17 01:01:18');

-- ----------------------------
-- Table structure for venue
-- ----------------------------
DROP TABLE IF EXISTS `venue`;
CREATE TABLE `venue`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '场馆ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '场馆名称',
  `venue_type_id` int NOT NULL COMMENT '场馆类型ID',
  `base_price` decimal(10, 2) NOT NULL COMMENT '基准价格',
  `capacity` int NOT NULL COMMENT '可容纳人数',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '场馆地址',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '场馆描述',
  `business_hours` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '营业时间',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `cover_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '场馆封面图片URL',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-关闭，1-开放',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `rating` double NULL DEFAULT 0 COMMENT '场馆评分',
  `review_count` int NULL DEFAULT 0 COMMENT '评价次数',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_venue_type`(`venue_type_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `fk_venue_type` FOREIGN KEY (`venue_type_id`) REFERENCES `venue_type` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '场馆表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of venue
-- ----------------------------
INSERT INTO `venue` VALUES (1, '羽毛球一号馆', 4, 15.00, 20, '测试地址', '这是一号羽毛球场馆', '09:00-22:00', '15556916571', NULL, 1, '2025-05-09 15:39:19', '2025-05-09 15:39:19', 0, 0);
INSERT INTO `venue` VALUES (2, '狗狗羽毛球馆', 4, 30.00, 20, '狗狗场馆地址', '狗狗场馆描述', '10:00-20:00', '15556916571', 'http://172.20.10.4:9000/venue/4cad33e7-d865-4f6a-b50b-720622dc7cb6.jpg', 1, '2025-05-10 23:59:23', '2025-05-17 00:08:46', 0, 0);

-- ----------------------------
-- Table structure for venue_facility
-- ----------------------------
DROP TABLE IF EXISTS `venue_facility`;
CREATE TABLE `venue_facility`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '设施ID',
  `venue_id` bigint NOT NULL COMMENT '场馆ID',
  `facility_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '设施类型',
  `quantity` int NOT NULL COMMENT '设施数量',
  `position_desc` json NOT NULL COMMENT '位置描述(JSON格式)',
  `price` decimal(10, 2) NULL DEFAULT NULL COMMENT '设施单独价格，为空则使用场馆基准价格',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_venue`(`venue_id` ASC) USING BTREE,
  INDEX `idx_facility_type`(`facility_type` ASC) USING BTREE,
  CONSTRAINT `fk_venue_facility` FOREIGN KEY (`venue_id`) REFERENCES `venue` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '场馆设施明细表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of venue_facility
-- ----------------------------
INSERT INTO `venue_facility` VALUES (1, 2, '羽毛球场地', 4, '{\"area\": \"主区域\", \"floor\": \"1\", \"details\": \"\", \"section\": \"A\"}', 12.00, '2025-05-12 14:22:49', '2025-05-12 15:25:17');

-- ----------------------------
-- Table structure for venue_location
-- ----------------------------
DROP TABLE IF EXISTS `venue_location`;
CREATE TABLE `venue_location`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '位置ID',
  `venue_id` bigint NOT NULL COMMENT '所属场馆ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '位置名称',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '位置类型',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-不可用，1-可用',
  `price` decimal(10, 2) NULL DEFAULT NULL COMMENT '位置单独价格，为空则使用场馆基准价格',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '位置描述',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_venue_id`(`venue_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `venue_location_ibfk_1` FOREIGN KEY (`venue_id`) REFERENCES `venue` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '场馆位置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of venue_location
-- ----------------------------

-- ----------------------------
-- Table structure for venue_review
-- ----------------------------
DROP TABLE IF EXISTS `venue_review`;
CREATE TABLE `venue_review`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `order_id` bigint NOT NULL COMMENT '关联订单ID',
  `user_id` bigint NOT NULL COMMENT '评价用户ID',
  `venue_id` bigint NOT NULL COMMENT '场馆ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '评价内容',
  `environment_score` tinyint NOT NULL DEFAULT 5 COMMENT '环境评分(1-5)',
  `facility_score` tinyint NOT NULL DEFAULT 5 COMMENT '设施评分(1-5)',
  `service_score` tinyint NOT NULL DEFAULT 5 COMMENT '服务评分(1-5)',
  `cost_performance_score` tinyint NOT NULL DEFAULT 5 COMMENT '性价比评分(1-5)',
  `overall_score` tinyint NOT NULL DEFAULT 5 COMMENT '综合评分(1-5)',
  `images` json NULL COMMENT '评价图片URL列表',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-正常，1-已封禁',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `rating` double NULL DEFAULT 0 COMMENT '综合评分展示',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_order_id`(`order_id` ASC) USING BTREE COMMENT '一个订单只能有一条评价',
  INDEX `idx_venue_id`(`venue_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_review_order` FOREIGN KEY (`order_id`) REFERENCES `booking_order` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_review_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_review_venue` FOREIGN KEY (`venue_id`) REFERENCES `venue` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '场馆评价表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of venue_review
-- ----------------------------
INSERT INTO `venue_review` VALUES (1, 6, 10006, 2, '助教妹妹好好看', 5, 5, 5, 4, 4, '[\"http://192.168.167.159:9000/venue/2025/05/13/14dac941e2054c9ca9fc4c63c94ac527.jpg\"]', 0, '2025-05-13 16:16:01', '2025-05-14 14:39:23', 0);

-- ----------------------------
-- Table structure for venue_review_ban
-- ----------------------------
DROP TABLE IF EXISTS `venue_review_ban`;
CREATE TABLE `venue_review_ban`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '封禁ID',
  `review_id` bigint NOT NULL COMMENT '评价ID',
  `admin_id` bigint NOT NULL COMMENT '操作管理员ID',
  `report_id` bigint NULL DEFAULT NULL COMMENT '关联举报ID',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '封禁原因',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_review_id`(`review_id` ASC) USING BTREE,
  INDEX `idx_admin_id`(`admin_id` ASC) USING BTREE,
  INDEX `idx_report_id`(`report_id` ASC) USING BTREE,
  CONSTRAINT `fk_ban_admin` FOREIGN KEY (`admin_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_ban_report` FOREIGN KEY (`report_id`) REFERENCES `venue_review_report` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_ban_review` FOREIGN KEY (`review_id`) REFERENCES `venue_review` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评价封禁记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of venue_review_ban
-- ----------------------------

-- ----------------------------
-- Table structure for venue_review_reply
-- ----------------------------
DROP TABLE IF EXISTS `venue_review_reply`;
CREATE TABLE `venue_review_reply`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '回复ID',
  `review_id` bigint NOT NULL COMMENT '评价ID',
  `user_id` bigint NOT NULL COMMENT '回复用户ID',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '回复内容',
  `is_admin` tinyint NOT NULL DEFAULT 0 COMMENT '是否管理员回复：0-否，1-是',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_review_id`(`review_id` ASC) USING BTREE,
  CONSTRAINT `fk_reply_review` FOREIGN KEY (`review_id`) REFERENCES `venue_review` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评价回复表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of venue_review_reply
-- ----------------------------
INSERT INTO `venue_review_reply` VALUES (1, 1, 10006, '真的哦！', 0, '2025-05-14 16:06:30', '2025-05-14 16:06:30');
INSERT INTO `venue_review_reply` VALUES (2, 1, 10006, '666', 0, '2025-05-14 16:33:07', '2025-05-14 16:33:07');
INSERT INTO `venue_review_reply` VALUES (3, 1, 10003, '贴脸开大', 1, '2025-05-14 18:31:17', '2025-05-14 18:31:17');
INSERT INTO `venue_review_reply` VALUES (4, 1, 10006, '就是好看', 0, '2025-05-15 11:29:50', '2025-05-15 11:29:50');

-- ----------------------------
-- Table structure for venue_review_report
-- ----------------------------
DROP TABLE IF EXISTS `venue_review_report`;
CREATE TABLE `venue_review_report`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '举报ID',
  `review_id` bigint NOT NULL COMMENT '评价ID',
  `reporter_id` bigint NOT NULL COMMENT '举报人ID',
  `reason` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '举报原因',
  `reason_detail` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '举报详细说明',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-待处理，1-已通过，2-已拒绝',
  `admin_id` bigint NULL DEFAULT NULL COMMENT '处理管理员ID',
  `admin_notes` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '管理员处理备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_review_id`(`review_id` ASC) USING BTREE,
  INDEX `idx_reporter_id`(`reporter_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `fk_report_review` FOREIGN KEY (`review_id`) REFERENCES `venue_review` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_report_user` FOREIGN KEY (`reporter_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评价举报表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of venue_review_report
-- ----------------------------
INSERT INTO `venue_review_report` VALUES (1, 1, 10004, '含有不当言论', '妈的鸟超涉黄', 0, 10003, '', '2025-05-14 11:13:06', '2025-05-14 14:38:19');

-- ----------------------------
-- Table structure for venue_type
-- ----------------------------
DROP TABLE IF EXISTS `venue_type`;
CREATE TABLE `venue_type`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '类型ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型名称',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '类型描述',
  `base_price` decimal(10, 2) NOT NULL COMMENT '基础价格',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '场馆类型表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of venue_type
-- ----------------------------
INSERT INTO `venue_type` VALUES (4, '羽毛球馆', '添加羽毛球设施', 10.00, '2025-05-09 15:38:19', '2025-05-09 15:38:19');

SET FOREIGN_KEY_CHECKS = 1;
