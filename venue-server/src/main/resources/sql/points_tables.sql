-- 用户积分账户表
CREATE TABLE IF NOT EXISTS `user_points` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `total_points` int(11) NOT NULL DEFAULT 0 COMMENT '总积分',
  `available_points` int(11) NOT NULL DEFAULT 0 COMMENT '可用积分',
  `frozen_points` int(11) NOT NULL DEFAULT 0 COMMENT '冻结积分',
  `expired_points` int(11) NOT NULL DEFAULT 0 COMMENT '已过期积分',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_id` (`user_id`),
  INDEX `idx_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户积分账户表';

-- 积分记录表
CREATE TABLE IF NOT EXISTS `point_records` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `point_type` tinyint(4) NOT NULL COMMENT '积分类型：1-获取 2-使用 3-过期 4-冻结 5-解冻',
  `points` int(11) NOT NULL COMMENT '积分变动数量',
  `balance` int(11) NOT NULL COMMENT '变动后积分余额',
  `source_type` tinyint(4) NOT NULL COMMENT '来源类型：1-预约 2-评价 3-签到 4-邀请 5-兑换 6-抵扣 7-过期 8-其他',
  `source_id` varchar(64) DEFAULT NULL COMMENT '来源ID，关联相应的订单ID等',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_expire_time` (`expire_time`),
  KEY `idx_source_type` (`source_type`),
  KEY `idx_source_id` (`source_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分记录表';

-- 积分规则表
CREATE TABLE IF NOT EXISTS `point_rules` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `rule_type` tinyint(4) NOT NULL COMMENT '规则类型：1-预约 2-评价 3-签到 4-邀请 5-其他',
  `rule_name` varchar(50) NOT NULL COMMENT '规则名称',
  `point_value` int(11) NOT NULL COMMENT '积分值',
  `rule_description` varchar(255) DEFAULT NULL COMMENT '规则描述',
  `validity_days` int(11) DEFAULT 365 COMMENT '积分有效期(天)',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_rule_type` (`rule_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分规则表';

-- 会员等级表
CREATE TABLE IF NOT EXISTS `member_levels` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `level_name` varchar(50) NOT NULL COMMENT '等级名称',
  `level_value` int(11) NOT NULL COMMENT '等级值',
  `point_threshold` int(11) NOT NULL COMMENT '所需积分阈值',
  `discount_rate` decimal(3,2) DEFAULT 1.00 COMMENT '折扣率',
  `description` varchar(255) DEFAULT NULL COMMENT '等级描述',
  `icon_url` varchar(255) DEFAULT NULL COMMENT '等级图标URL',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_level_value` (`level_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员等级表';

-- 初始化默认会员等级
INSERT INTO `member_levels` (`level_name`, `level_value`, `point_threshold`, `discount_rate`, `description`, `icon_url`, `status`) 
VALUES 
('普通会员', 1, 0, 1.00, '基础会员权益', '/images/member/level1.png', 1),
('银卡会员', 2, 1000, 0.95, '享受95折预约优惠', '/images/member/level2.png', 1),
('金卡会员', 3, 5000, 0.90, '享受9折预约优惠，优先预约权', '/images/member/level3.png', 1),
('钻石会员', 4, 10000, 0.85, '享受85折预约优惠，专属客服', '/images/member/level4.png', 1);

-- 初始化默认积分规则
INSERT INTO `point_rules` (`rule_type`, `rule_name`, `point_value`, `rule_description`, `validity_days`, `status`) 
VALUES 
(1, '预约完成奖励', 100, '成功预约并完成场馆使用后获得积分', 365, 1),
(2, '评价奖励', 50, '对场馆进行评价获得积分', 365, 1),
(3, '每日签到', 5, '每日登录签到获得积分', 365, 1),
(4, '邀请好友', 200, '成功邀请好友注册并完成首次预约', 365, 1);

-- 添加执行数据库脚本的方法
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS create_points_tables()
BEGIN
    -- 创建积分相关表
    -- 用户积分账户表
    CREATE TABLE IF NOT EXISTS `user_points` (
      `id` bigint(20) NOT NULL AUTO_INCREMENT,
      `user_id` bigint(20) NOT NULL COMMENT '用户ID',
      `total_points` int(11) NOT NULL DEFAULT 0 COMMENT '总积分',
      `available_points` int(11) NOT NULL DEFAULT 0 COMMENT '可用积分',
      `frozen_points` int(11) NOT NULL DEFAULT 0 COMMENT '冻结积分',
      `expired_points` int(11) NOT NULL DEFAULT 0 COMMENT '已过期积分',
      `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
      `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
      PRIMARY KEY (`id`),
      UNIQUE KEY `idx_user_id` (`user_id`),
      INDEX `idx_update_time` (`update_time`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户积分账户表';
    
    -- 积分记录表
    CREATE TABLE IF NOT EXISTS `point_records` (
      `id` bigint(20) NOT NULL AUTO_INCREMENT,
      `user_id` bigint(20) NOT NULL COMMENT '用户ID',
      `point_type` tinyint(4) NOT NULL COMMENT '积分类型：1-获取 2-使用 3-过期 4-冻结 5-解冻',
      `points` int(11) NOT NULL COMMENT '积分变动数量',
      `balance` int(11) NOT NULL COMMENT '变动后积分余额',
      `source_type` tinyint(4) NOT NULL COMMENT '来源类型：1-预约 2-评价 3-签到 4-邀请 5-兑换 6-抵扣 7-过期 8-其他',
      `source_id` varchar(64) DEFAULT NULL COMMENT '来源ID，关联相应的订单ID等',
      `description` varchar(255) DEFAULT NULL COMMENT '描述',
      `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
      `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
      PRIMARY KEY (`id`),
      KEY `idx_user_id` (`user_id`),
      KEY `idx_create_time` (`create_time`),
      KEY `idx_expire_time` (`expire_time`),
      KEY `idx_source_type` (`source_type`),
      KEY `idx_source_id` (`source_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分记录表';
    
    -- 积分规则表
    CREATE TABLE IF NOT EXISTS `point_rules` (
      `id` int(11) NOT NULL AUTO_INCREMENT,
      `rule_type` tinyint(4) NOT NULL COMMENT '规则类型：1-预约 2-评价 3-签到 4-邀请 5-其他',
      `rule_name` varchar(50) NOT NULL COMMENT '规则名称',
      `point_value` int(11) NOT NULL COMMENT '积分值',
      `rule_description` varchar(255) DEFAULT NULL COMMENT '规则描述',
      `validity_days` int(11) DEFAULT 365 COMMENT '积分有效期(天)',
      `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
      `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
      `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
      PRIMARY KEY (`id`),
      KEY `idx_rule_type` (`rule_type`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分规则表';
    
    -- 会员等级表
    CREATE TABLE IF NOT EXISTS `member_levels` (
      `id` int(11) NOT NULL AUTO_INCREMENT,
      `level_name` varchar(50) NOT NULL COMMENT '等级名称',
      `level_value` int(11) NOT NULL COMMENT '等级值',
      `point_threshold` int(11) NOT NULL COMMENT '所需积分阈值',
      `discount_rate` decimal(3,2) DEFAULT 1.00 COMMENT '折扣率',
      `description` varchar(255) DEFAULT NULL COMMENT '等级描述',
      `icon_url` varchar(255) DEFAULT NULL COMMENT '等级图标URL',
      `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
      `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
      `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
      PRIMARY KEY (`id`),
      UNIQUE KEY `idx_level_value` (`level_value`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员等级表';
    
    -- 修改订单表添加积分字段
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'booking_order' AND COLUMN_NAME = 'points_used'
    ) THEN
        ALTER TABLE `booking_order` ADD COLUMN `points_used` INT DEFAULT 0 COMMENT '积分使用数量';
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'booking_order' AND COLUMN_NAME = 'points_discount'
    ) THEN
        ALTER TABLE `booking_order` ADD COLUMN `points_discount` DECIMAL(10, 2) DEFAULT 0 COMMENT '积分抵扣金额';
    END IF;
    
    -- 初始化默认数据
    -- 初始化默认会员等级
    IF NOT EXISTS (SELECT 1 FROM `member_levels` LIMIT 1) THEN
        INSERT INTO `member_levels` (`level_name`, `level_value`, `point_threshold`, `discount_rate`, `description`, `icon_url`, `status`) 
        VALUES 
        ('普通会员', 1, 0, 1.00, '基础会员权益', '/images/member/level1.png', 1),
        ('银卡会员', 2, 1000, 0.95, '享受95折预约优惠', '/images/member/level2.png', 1),
        ('金卡会员', 3, 5000, 0.90, '享受9折预约优惠，优先预约权', '/images/member/level3.png', 1),
        ('钻石会员', 4, 10000, 0.85, '享受85折预约优惠，专属客服', '/images/member/level4.png', 1);
    END IF;
    
    -- 初始化默认积分规则
    IF NOT EXISTS (SELECT 1 FROM `point_rules` LIMIT 1) THEN
        INSERT INTO `point_rules` (`rule_type`, `rule_name`, `point_value`, `rule_description`, `validity_days`, `status`) 
        VALUES 
        (1, '预约完成奖励', 100, '成功预约并完成场馆使用后获得积分', 365, 1),
        (2, '评价奖励', 50, '对场馆进行评价获得积分', 365, 1),
        (3, '每日签到', 5, '每日登录签到获得积分', 365, 1),
        (4, '邀请好友', 200, '成功邀请好友注册并完成首次预约', 365, 1);
    END IF;
END //
DELIMITER ;

-- 执行创建表的存储过程
CALL create_points_tables(); 