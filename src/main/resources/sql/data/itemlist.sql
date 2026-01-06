# CREATE DATABASE NullBot;
# USE NullBot;

DROP TABLE IF EXISTS item;

CREATE TABLE item (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    category INT NOT NULL,
    rarity INT NOT NULL,
    price INT NOT NULL,
    weight INT NOT NULL,
    description VARCHAR(250),
    image_path VARCHAR(100),
    available BOOLEAN DEFAULT TRUE
);

-- 基本物品
INSERT INTO item (name, category, rarity, price, weight, description, image_path, available) VALUES
-- 白色稀有度物品
('干脆面', 0, 0, 50, 1, '好吃', NULL, TRUE),
-- 绿色稀有度物品
('电路板', 0, 1, 100, 1, '电子垃圾', NULL, TRUE),
-- 蓝色稀有度物品
('U盘', 0, 2, 500, 1, '存储介质', NULL, TRUE),
-- 紫色稀有度物品
('完整牛角', 0, 3, 1000, 2, '你老婆...', NULL, TRUE),
-- 金色稀有度物品
('高速固态', 0, 4, 10000, 2, '电脑配件', NULL, TRUE),
-- 红色稀有度物品
('海洋之泪', 0, 5, 202520, 1, '浑然天成，珠圆玉润的巨大天然珍珠', NULL, TRUE),
('非洲之心', 0, 5, 131452, 1, '世界上最大的钻石，象征永恒的爱', NULL, TRUE);

-- 可以使用的物品
INSERT INTO item (name, category, rarity, price, weight, description, image_path, available) VALUES
('口球', 1, 3, 25, 1, '(可使用物品) 如果Bot是管理员，用了会禁言自己一分钟 {UserBan userId 1}', NULL, TRUE);

-- 面包游戏物品
INSERT INTO item (name, category, rarity, price, weight, description, image_path, available) VALUES
('面包', 2, 2, 10, 1, '朴实无华', NULL, TRUE),
('法棍', 2, 4, 1000, 1, '杀人凶手', NULL, TRUE),
('菠萝包', 2, 5, 7500, 1, '白咲花最爱！', NULL, TRUE),
('小面包', 2, 5, 10000, 1, '你会记得自己至今为止吃过...', NULL, TRUE);
