-- 检查user_message表结构
DESC user_message;

-- 添加缺失的type字段（如果不存在）
ALTER TABLE user_message ADD COLUMN type tinyint NOT NULL DEFAULT 1 COMMENT '消息类型：1-系统通知，2-订单消息，3-反馈回复，4-其他' AFTER user_id; 