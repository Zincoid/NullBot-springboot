# CREATE DATABASE NullBot;
# USE NullBot;

DROP TABLE IF EXISTS user;
DROP TABLE IF EXISTS item;
DROP TABLE IF EXISTS inventory;

CREATE TABLE user (
    id BIGINT PRIMARY KEY,
    level INT DEFAULT 1,
    cash INT DEFAULT 0,
    capacity INT DEFAULT 100,
    draw_times INT DEFAULT 50
);

# description 可通过 {} 嵌入指令, 指令不需要前缀
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
