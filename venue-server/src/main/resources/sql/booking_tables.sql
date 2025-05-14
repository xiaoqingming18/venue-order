-- 创建预约订单表
CREATE TABLE `booking_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '预约订单ID',
  `order_no` varchar(32) NOT NULL COMMENT '订单编号',
  `user_id` bigint(20) NOT NULL COMMENT '预约用户ID',
  `venue_id` bigint(20) NOT NULL COMMENT '场馆ID',
  `booking_date` date NOT NULL COMMENT '预约日期',
  `start_time` time NOT NULL COMMENT '开始时间',
  `end_time` time NOT NULL COMMENT '结束时间',
  `booking_type` tinyint(4) NOT NULL COMMENT '预约类型：1-包场预约，2-设施预约',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '订单状态：0-已取消，1-待支付，2-已预约，3-已完成',
  `remarks` varchar(500) DEFAULT NULL COMMENT '备注信息',
  `created_at` datetime NOT NULL COMMENT '创建时间',
  `updated_at` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_venue_id` (`venue_id`),
  KEY `idx_booking_date` (`booking_date`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约订单表';

-- 创建预约设施明细表
CREATE TABLE `booking_facility` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '预约设施ID',
  `order_id` bigint(20) NOT NULL COMMENT '关联的订单ID',
  `facility_id` bigint(20) NOT NULL COMMENT '设施ID',
  `quantity` int(11) NOT NULL COMMENT '预约数量',
  `unit_price` decimal(10,2) NOT NULL COMMENT '单价',
  `amount` decimal(10,2) NOT NULL COMMENT '金额小计',
  `created_at` datetime NOT NULL COMMENT '创建时间',
  `updated_at` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_facility_id` (`facility_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约设施明细表';

-- 创建时间段表
CREATE TABLE `time_slot` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '时间段ID',
  `venue_id` bigint(20) NOT NULL COMMENT '场馆ID',
  `start_time` time NOT NULL COMMENT '开始时间',
  `end_time` time NOT NULL COMMENT '结束时间',
  `price_rate` decimal(10,2) NOT NULL DEFAULT '1.00' COMMENT '价格系数，默认为1',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：0-不可用，1-可用',
  `created_at` datetime NOT NULL COMMENT '创建时间',
  `updated_at` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_venue_id` (`venue_id`),
  KEY `idx_time_range` (`venue_id`,`start_time`,`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='时间段表';

-- 添加外键约束
ALTER TABLE `booking_order`
  ADD CONSTRAINT `fk_booking_order_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  ADD CONSTRAINT `fk_booking_order_venue` FOREIGN KEY (`venue_id`) REFERENCES `venue` (`id`);

ALTER TABLE `booking_facility`
  ADD CONSTRAINT `fk_booking_facility_order` FOREIGN KEY (`order_id`) REFERENCES `booking_order` (`id`),
  ADD CONSTRAINT `fk_booking_facility_facility` FOREIGN KEY (`facility_id`) REFERENCES `venue_facility` (`id`);

ALTER TABLE `time_slot`
  ADD CONSTRAINT `fk_time_slot_venue` FOREIGN KEY (`venue_id`) REFERENCES `venue` (`id`); 