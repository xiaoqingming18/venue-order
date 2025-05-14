-- 创建场馆表
CREATE TABLE venue (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '场馆ID',
    name VARCHAR(100) NOT NULL COMMENT '场馆名称',
    type VARCHAR(50) NOT NULL COMMENT '场馆类型',
    address VARCHAR(200) NOT NULL COMMENT '场馆地址',
    description TEXT COMMENT '场馆描述',
    business_hours VARCHAR(100) COMMENT '营业时间',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-关闭，1-开放',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_type (type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场馆表';

-- 创建场馆位置表
CREATE TABLE venue_location (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '位置ID',
    venue_id BIGINT NOT NULL COMMENT '所属场馆ID',
    name VARCHAR(100) NOT NULL COMMENT '位置名称',
    type VARCHAR(50) NOT NULL COMMENT '位置类型',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-不可用，1-可用',
    description TEXT COMMENT '位置描述',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (venue_id) REFERENCES venue(id) ON DELETE CASCADE,
    INDEX idx_venue_id (venue_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场馆位置表'; 