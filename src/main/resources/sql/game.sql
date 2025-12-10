CREATE DATABASE NullBot;
USE NullBot;

DROP TABLE user;
DROP TABLE item;
DROP TABLE inventory;

CREATE TABLE user (
    id LONG PRIMARY KEY,
    level INT DEFAULT 1
);

CREATE TABLE item (
    id INT PRIMARY KEY AUTO_INCREMENT,
    rarity INT NOT NULL,
    name VARCHAR(50) NOT NULL,
    image_path VARCHAR(100),
    available BOOLEAN DEFAULT TRUE
);

CREATE TABLE inventory (
    id INT PRIMARY KEY AUTO_INCREMENT,
    owner_id LONG NOT NULL,
    item_id INT NOT NULL,
    amount INT DEFAULT 1
);