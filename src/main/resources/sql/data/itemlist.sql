# CREATE DATABASE NullBot;
# USE NullBot;

DROP TABLE IF EXISTS item;

CREATE TABLE `item` (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    category INT NOT NULL,
    rarity INT NOT NULL,
    price INT NOT NULL,
    weight INT NOT NULL,
    description VARCHAR(250),
    command VARCHAR(250),
    image_path VARCHAR(100),
    available BOOLEAN DEFAULT TRUE
);

-- 基本物品
INSERT INTO item (name, category, rarity, price, weight, description, command, image_path, available) VALUES
-- 白色稀有度物品
('干脆面', 3, 0, 50, 1, '好吃', NULL, NULL, TRUE),
-- 绿色稀有度物品
('电路板', 3, 1, 100, 1, '电子垃圾', NULL, NULL, TRUE),
-- 蓝色稀有度物品
('U盘', 3, 2, 500, 1, '存储介质', NULL, NULL, TRUE),
-- 紫色稀有度物品
('完整牛角', 3, 3, 1000, 2, '你老婆...', NULL, NULL, TRUE),
-- 金色稀有度物品
('高速固态', 3, 4, 10000, 2, '电脑配件', NULL, NULL, TRUE),
-- 红色稀有度物品
('海洋之泪', 3, 5, 202520, 1, '浑然天成，珠圆玉润的巨大天然珍珠', NULL, NULL, TRUE),
('非洲之心', 3, 5, 131452, 1, '世界上最大的钻石，象征永恒的爱', NULL, NULL, TRUE);

-- 可以使用的物品
INSERT INTO item (name, category, rarity, price, weight, description, command, image_path, available) VALUES
('口球', 1, 3, 25, 1, '(可使用物品) 若Bot有管理权限，使用会禁言自身1分钟', 'UserBan userId 1', NULL, TRUE);

-- 面包游戏物品
INSERT INTO item (name, category, rarity, price, weight, description, command, image_path, available) VALUES
('黑麦面包', 2, 0, 10, 1, '还行', NULL, NULL, TRUE),
('吐司', 2, 1, 100, 1, '方方正正', NULL, NULL, TRUE),
('面包', 2, 1, 100, 1, '朴实无华', NULL, NULL, FALSE),
('小圆面包', 2, 2, 200, 1, '好吃', NULL, NULL, TRUE),
('可颂', 2, 3, 500, 1, '好多层！', NULL, NULL, TRUE),
('法棍', 2, 4, 1000, 1, '杀人凶手', NULL, NULL, TRUE),
('馒头', 2, 4, 1000, 1, '中国面包', NULL, NULL, TRUE),
('菠萝包', 2, 5, 7500, 1, '白咲花最爱！', NULL, NULL, TRUE),
('甜甜圈', 2, 5, 8000, 1, '美式', NULL, NULL, TRUE),
('小面包?', 2, 5, 10000, 1, '你会记得自己至今为止吃过...', NULL, NULL, TRUE);
