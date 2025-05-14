-- 用户数据
INSERT INTO user (id, username, password, salt, email, phone, nickname, status, role, register_time, update_time) 
VALUES (1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', 'salt', 'admin@example.com', '13800138000', '管理员', 1, 2, NOW(), NOW());

INSERT INTO user (id, username, password, salt, email, phone, nickname, status, role, register_time, update_time) 
VALUES (2, 'user1', 'e10adc3949ba59abbe56e057f20f883e', 'salt', 'user1@example.com', '13800138001', '普通用户1', 1, 1, NOW(), NOW());

-- 场馆类型数据
INSERT INTO venue_type (id, name, description, base_price, created_at, updated_at) VALUES (1, '羽毛球馆', '标准羽毛球场地', 100.00, NOW(), NOW());
INSERT INTO venue_type (id, name, description, base_price, created_at, updated_at) VALUES (2, '篮球馆', '标准篮球场地', 200.00, NOW(), NOW());
INSERT INTO venue_type (id, name, description, base_price, created_at, updated_at) VALUES (3, '足球场', '标准足球场地', 500.00, NOW(), NOW());
INSERT INTO venue_type (id, name, description, base_price, created_at, updated_at) VALUES (4, '游泳馆', '标准游泳池', 150.00, NOW(), NOW());

-- 场馆数据
INSERT INTO venue (id, name, venue_type_id, base_price, capacity, address, description, business_hours, contact_phone, cover_image, status, created_at, updated_at) 
VALUES (1, '羽毛球一号馆', 1, 100.00, 50, '市中心体育馆1号场馆', '专业羽毛球场地，配有完善的照明和通风设施', '08:00-22:00', '13800138000', 'https://example.com/images/badminton1.jpg', 1, NOW(), NOW());

INSERT INTO venue (id, name, venue_type_id, base_price, capacity, address, description, business_hours, contact_phone, cover_image, status, created_at, updated_at) 
VALUES (2, '篮球二号馆', 2, 200.00, 80, '市中心体育馆2号场馆', '专业篮球场地，木质地板，标准NBA尺寸', '08:00-22:00', '13800138001', 'https://example.com/images/basketball2.jpg', 1, NOW(), NOW());

-- 场馆设施数据
INSERT INTO venue_facility (id, venue_id, facility_type, quantity, position_desc, created_at, updated_at) 
VALUES (1, 1, '羽毛球拍', 20, '{"area": "equipment", "row": 1, "column": 1}', NOW(), NOW());

INSERT INTO venue_facility (id, venue_id, facility_type, quantity, position_desc, created_at, updated_at) 
VALUES (2, 1, '更衣室', 2, '{"area": "rest", "row": 1, "column": 2}', NOW(), NOW());

INSERT INTO venue_facility (id, venue_id, facility_type, quantity, position_desc, created_at, updated_at) 
VALUES (3, 1, '淋浴间', 4, '{"area": "rest", "row": 1, "column": 3}', NOW(), NOW());

INSERT INTO venue_facility (id, venue_id, facility_type, quantity, position_desc, created_at, updated_at) 
VALUES (4, 2, '篮球', 10, '{"area": "equipment", "row": 1, "column": 1}', NOW(), NOW());

INSERT INTO venue_facility (id, venue_id, facility_type, quantity, position_desc, created_at, updated_at) 
VALUES (5, 2, '训练背心', 20, '{"area": "equipment", "row": 1, "column": 2}', NOW(), NOW());

INSERT INTO venue_facility (id, venue_id, facility_type, quantity, position_desc, created_at, updated_at) 
VALUES (6, 2, '更衣室', 4, '{"area": "rest", "row": 1, "column": 3}', NOW(), NOW()); 