# CREATE DATABASE NullBot;
# USE NullBot;

DROP TABLE IF EXISTS `item`;
DROP TABLE IF EXISTS `inventory`;

# command 为 null 时 物品不可用
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

CREATE TABLE `inventory` (
    id INT PRIMARY KEY AUTO_INCREMENT,
    owner_id LONG NOT NULL,
    item_id INT NOT NULL,
    item_name VARCHAR(100) NOT NULL,
    category INT NOT NULL,
    rarity INT NOT NULL,
    price INT NOT NULL,
    amount INT DEFAULT 1
);
