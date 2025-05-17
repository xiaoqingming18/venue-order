-- ----------------------------
-- Records of venue_tag (初始标签数据)
-- ----------------------------
INSERT INTO `venue_tag` (`name`, `category`, `weight`, `status`) VALUES 
-- 运动类型标签
('足球', '运动类型', 5, 1),
('篮球', '运动类型', 5, 1),
('羽毛球', '运动类型', 5, 1),
('乒乓球', '运动类型', 5, 1),
('网球', '运动类型', 5, 1),
('游泳', '运动类型', 5, 1),
('健身', '运动类型', 5, 1),
('瑜伽', '运动类型', 4, 1),
('排球', '运动类型', 4, 1),
('田径', '运动类型', 4, 1),

-- 场馆设施标签
('室内', '场地特点', 3, 1),
('室外', '场地特点', 3, 1),
('空调', '设施配套', 2, 1),
('淋浴', '设施配套', 2, 1),
('更衣室', '设施配套', 2, 1),
('停车场', '设施配套', 2, 1),
('WiFi', '设施配套', 1, 1),
('休息区', '设施配套', 1, 1),

-- 场馆环境标签
('安静', '环境特点', 2, 1),
('宽敞', '环境特点', 2, 1),
('明亮', '环境特点', 2, 1),
('通风良好', '环境特点', 2, 1),
('景色优美', '环境特点', 2, 1),

-- 场馆氛围标签
('适合初学者', '氛围', 3, 1),
('专业训练', '氛围', 3, 1),
('亲子友好', '氛围', 3, 1),
('适合团建', '氛围', 3, 1),
('比赛级别', '氛围', 4, 1),

-- 价格标签
('经济实惠', '价格', 3, 1),
('中等价位', '价格', 3, 1),
('高端', '价格', 3, 1),

-- 时间标签
('早晨可用', '时间', 2, 1),
('午间可用', '时间', 2, 1),
('晚间可用', '时间', 2, 1),
('全天开放', '时间', 2, 1),
('周末可用', '时间', 3, 1),
('工作日可用', '时间', 2, 1);

-- 为现有场馆添加标签的示例脚本
-- 注意：以下脚本假设venue表中已有数据，需根据实际场馆ID进行修改
-- 足球场馆标签示例（假设ID为1的场馆是足球场馆）
INSERT INTO `venue_tag_relation` (`venue_id`, `tag_id`) 
SELECT 
    1 as venue_id,  -- 直接使用实际存在的场馆ID，请替换为真实ID
    `id` as tag_id 
FROM `venue_tag` 
WHERE `name` IN ('足球', '室外', '宽敞', '适合团建', '周末可用');

-- 篮球场馆标签示例（假设ID为2的场馆是篮球场馆）
INSERT INTO `venue_tag_relation` (`venue_id`, `tag_id`) 
SELECT 
    2 as venue_id,  -- 直接使用实际存在的场馆ID，请替换为真实ID
    `id` as tag_id 
FROM `venue_tag` 
WHERE `name` IN ('篮球', '室内', '空调', '宽敞', '晚间可用');

-- 羽毛球场馆标签示例（假设ID为3的场馆是羽毛球场馆）
INSERT INTO `venue_tag_relation` (`venue_id`, `tag_id`) 
SELECT 
    3 as venue_id,  -- 直接使用实际存在的场馆ID，请替换为真实ID
    `id` as tag_id 
FROM `venue_tag` 
WHERE `name` IN ('羽毛球', '室内', '空调', '更衣室', '适合初学者');

-- 游泳馆标签示例（假设ID为4的场馆是游泳馆）
INSERT INTO `venue_tag_relation` (`venue_id`, `tag_id`) 
SELECT 
    4 as venue_id,  -- 直接使用实际存在的场馆ID，请替换为真实ID
    `id` as tag_id 
FROM `venue_tag` 
WHERE `name` IN ('游泳', '室内', '淋浴', '更衣室', '全天开放');

-- 健身房标签示例（假设ID为5的场馆是健身房）
INSERT INTO `venue_tag_relation` (`venue_id`, `tag_id`) 
SELECT 
    5 as venue_id,  -- 直接使用实际存在的场馆ID，请替换为真实ID
    `id` as tag_id 
FROM `venue_tag` 
WHERE `name` IN ('健身', '室内', '空调', '专业训练', '全天开放');

-- ------------------------------------------------------
-- 以下脚本需要确保相关表和数据已存在，否则需要注释掉
-- ------------------------------------------------------

-- 查找一个存在的用户ID，或者直接指定已知存在的用户ID
-- UNCOMMENT AND RUN THIS BLOCK SEPARATELY FIRST:
-- 
-- SELECT @existing_user_id := id FROM `user` LIMIT 1;
-- SELECT @existing_user_id AS 'Found User ID';
--
-- 如果上面查询返回了用户ID，请使用该ID替换下面的1
SET @user_id = 1; -- 请将此处修改为查询到的或已知存在的用户ID

-- ------------------------------------------------------
-- 以下内容需要确保上面的@user_id变量指向一个存在的用户ID
-- 如果没有找到用户，请注释掉以下内容
-- ------------------------------------------------------

-- 用户对标签的偏好示例
/* 
INSERT INTO `user_preference` (`user_id`, `tag_id`, `preference_score`, `source`)
SELECT 
    @user_id as user_id,
    `id` as tag_id,
    CASE 
        WHEN `name` = '足球' THEN 8.5
        WHEN `name` = '室外' THEN 7.5
        WHEN `name` = '周末可用' THEN 9.0
        ELSE 5.0
    END as preference_score,
    0 as source
FROM `venue_tag`
WHERE `name` IN ('足球', '室外', '周末可用', '适合团建');

-- 场馆相似度数据示例
-- 此处仅作示例，实际应通过算法计算场馆间的相似度
INSERT INTO `venue_similarity` (`venue_id`, `similar_venue_id`, `similarity_score`)
VALUES 
(1, 2, 0.85),  -- 假设场馆1和场馆2非常相似
(1, 3, 0.65),  -- 假设场馆1和场馆3较为相似
(2, 3, 0.75);  -- 假设场馆2和场馆3相似度中等

-- 推荐数据示例
-- 对用户的推荐示例
INSERT INTO `venue_recommendation` (`user_id`, `venue_id`, `recommendation_type`, `recommendation_score`)
VALUES
(@user_id, 2, 0, 8.7),  -- 基于用户行为推荐场馆2
(@user_id, 3, 0, 7.5),  -- 基于用户行为推荐场馆3
(@user_id, 4, 1, 6.8),  -- 基于相似用户推荐场馆4
(@user_id, 5, 2, 5.9);  -- 热门场馆推荐场馆5 
*/ 