# CREATE DATABASE NullBot;
# USE NullBot;

DROP TABLE user;
DROP TABLE item;
DROP TABLE inventory;

CREATE TABLE user (
    id BIGINT PRIMARY KEY,
    level INT DEFAULT 1,
    draw_times INT DEFAULT 50,
    capacity INT DEFAULT 100
);

# description 可通过 {} 嵌入指令, 不需要前缀
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

CREATE TABLE inventory (
    id INT PRIMARY KEY AUTO_INCREMENT,
    owner_id LONG NOT NULL,
    item_id INT NOT NULL,
    item_name VARCHAR(100) NOT NULL,
    rarity INT NOT NULL,
    price INT NOT NULL,
    amount INT DEFAULT 1
);

INSERT INTO item (name, category, rarity, price, weight, description, image_path, available) VALUES
-- 白色稀有度物品 (0，15%，数量较多，价格较低)
('普通长剑', 0, 0, 50, 1, '一把普通的铁制长剑，适合新手使用', NULL, TRUE),
('木制盾牌', 0, 0, 30, 2, '轻便的木制盾牌，防御力有限', NULL, TRUE),
('布甲上衣', 0, 0, 80, 1, '简单的布制上衣，提供基础防护', NULL, TRUE),
('粗糙皮靴', 0, 0, 40, 1, '手工制作的粗糙皮靴', NULL, TRUE),
('恢复药水(小)', 0, 0, 25, 3, '少量恢复生命值的药水', NULL, TRUE),
('普通法杖', 0, 0, 60, 1, '新手魔法师使用的普通法杖', NULL, TRUE),
('铁匕首', 0, 0, 35, 1, '短小的铁制匕首，便于隐藏', NULL, TRUE),
('亚麻披风', 0, 0, 70, 1, '简单的亚麻披风，略有保暖效果', NULL, TRUE),
('火把', 0, 0, 15, 5, '可以照明的火把，持续燃烧时间有限', NULL, TRUE),
('简易绷带', 0, 0, 10, 10, '用于止血的基础医疗用品', NULL, TRUE),
('木箭x20', 0, 0, 20, 2, '普通木箭，一捆20支', NULL, TRUE),
('露营帐篷', 0, 0, 120, 10, '简易的单人露营帐篷', NULL, TRUE),
('水囊', 0, 0, 15, 2, '装满清水的皮制水囊', NULL, TRUE),
('面包', 0, 0, 5, 5, '新鲜出炉的面包，可以充饥', NULL, TRUE),
('鱼竿', 0, 0, 45, 3, '普通的钓鱼竿', NULL, TRUE),
('伐木斧', 0, 0, 55, 4, '用于砍伐树木的斧头', NULL, TRUE),
('矿工镐', 0, 0, 65, 4, '挖掘矿石的工具', NULL, TRUE),
('蜡烛', 0, 0, 8, 8, '可以长时间照明的蜡烛', NULL, TRUE),
('记事本', 0, 0, 12, 1, '用于记录信息的笔记本', NULL, TRUE),
('麻绳', 0, 0, 18, 2, '结实耐用的麻绳，长10米', NULL, TRUE),

-- 绿色稀有度物品 (1，15%，价格适中)
('精铁长剑', 0, 1, 180, 2, '经过精细锻造的铁制长剑', NULL, TRUE),
('镶边皮甲', 0, 1, 220, 3, '经过加固的皮甲，防御力不错', NULL, TRUE),
('魔法水晶', 0, 1, 150, 1, '蕴含微弱魔力的水晶', NULL, TRUE),
('恢复药水(中)', 0, 1, 60, 4, '中等效果的恢复药水', NULL, TRUE),
('旅行背包', 0, 1, 200, 8, '可以携带更多物品的背包', NULL, TRUE),
('精准短弓', 0, 1, 160, 3, '精准度更高的短弓', NULL, TRUE),
('秘银护符', 0, 1, 140, 1, '带有微弱魔力的护身符', NULL, TRUE),
('草药学套装', 0, 1, 190, 5, '采集和制作草药的工具套装', NULL, TRUE),
('魔导书·初级', 0, 1, 175, 3, '记录基础魔法的书籍', NULL, TRUE),
('猎人的陷阱', 0, 1, 95, 6, '可以捕捉野兽的陷阱', NULL, TRUE),
('迅捷皮靴', 0, 1, 125, 2, '增加移动速度的皮靴', NULL, TRUE),
('贤者墨水', 0, 1, 110, 1, '用于书写魔法卷轴的特制墨水', NULL, TRUE),
('火系魔法卷轴', 0, 1, 155, 1, '记载火球术的魔法卷轴', NULL, TRUE),
('精灵之茶', 0, 1, 85, 2, '可以恢复精神的茶叶', NULL, TRUE),
('夜视护目镜', 0, 1, 165, 2, '可以在黑暗中看清东西的护目镜', NULL, TRUE),

-- 蓝色稀有度物品 (2，25%，较好的装备)
('寒冰法杖', 0, 2, 350, 2, '镶嵌着冰晶的法杖，能释放冰系魔法', NULL, TRUE),
('骑士铠甲', 0, 2, 450, 15, '完整的骑士铠甲，提供优秀防护', NULL, TRUE),
('大师之剑', 0, 2, 420, 3, '名匠打造的精钢长剑', NULL, TRUE),
('魔力护盾', 0, 2, 380, 5, '可以吸收魔法伤害的护盾', NULL, TRUE),
('恢复药水(大)', 0, 2, 150, 3, '强效恢复药水', NULL, TRUE),
('幻影披风', 0, 2, 320, 2, '可以短时间隐身的魔法披风', NULL, TRUE),
('空间口袋', 0, 2, 280, 1, '内含异次元空间的魔法口袋', NULL, TRUE),
('古代石板', 0, 2, 400, 10, '记载古代知识的石板', NULL, TRUE),
('龙骨项链', 0, 2, 360, 1, '用龙骨制作的魔法项链', NULL, TRUE),
('雷霆战锤', 0, 2, 500, 20, '沉重但威力巨大的战锤', NULL, TRUE),

-- 紫色稀有度物品 (3，35%，高级装备)
('圣光权杖', 0, 3, 800, 4, '散发神圣光芒的强大权杖', NULL, TRUE),
('龙鳞铠甲', 0, 3, 1200, 25, '用龙鳞制作的传说级铠甲', NULL, TRUE),
('虚空之刃', 0, 3, 900, 3, '可以切割空间的魔法匕首', NULL, TRUE),
('凤凰之羽', 0, 3, 750, 1, '蕴含着凤凰魔力的羽毛', NULL, TRUE),
('贤者之石', 0, 3, 1500, 1, '传说中可以实现炼金术的宝石', NULL, TRUE),
('恶魔契约', 0, 3, 850, 1, '与恶魔签订的神秘契约', NULL, TRUE),
('时间沙漏', 0, 3, 1100, 5, '可以操控时间的魔法沙漏', NULL, TRUE),
('精灵王冠', 0, 3, 950, 2, '精灵王的冠冕，具有自然魔力', NULL, TRUE),

-- 金色稀有度物品 (4，8%，顶级装备)
('神之剑·裁决', 0, 4, 3000, 5, '传说中的神造武器，具有审判之力', NULL, TRUE),
('创世之书', 0, 4, 2500, 8, '记载创世秘密的神秘书籍', NULL, TRUE),
('永恒之心', 0, 4, 2800, 1, '永不停止跳动的水晶心脏', NULL, TRUE),
('命运之轮', 0, 4, 3200, 10, '可以窥探命运的魔法物品', NULL, TRUE),
('神格碎片', 0, 4, 4000, 1, '失落神明的神格碎片', NULL, TRUE),

-- 红色稀有度物品 (5，2%，传说级神器)
('混沌核心', 0, 5, 10000, 1, '维持世界平衡的混沌核心', NULL, TRUE),
('无限宝石', 0, 5, 15000, 1, '蕴含着无限可能性的宝石', NULL, TRUE),
('世界树之种', 0, 5, 8000, 1, '可以生长出世界树的种子', NULL, TRUE);