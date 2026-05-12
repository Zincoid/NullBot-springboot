# CREATE DATABASE nullbot;
# USE nullbot;

DROP TABLE IF EXISTS `group`;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS `admin`;

CREATE TABLE `group` (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    access INT DEFAULT 2
);

CREATE TABLE `user` (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    access INT DEFAULT 0,

    level INT DEFAULT 1,
    experience INT DEFAULT 0,
    cash INT DEFAULT 0,
    capacity INT DEFAULT 100,
    draw_times INT DEFAULT 50
);

create table `admin`
(
    id BIGINT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL
);


# 预设置 - 管理级用户
INSERT INTO `user` (id, name, access) VALUES
    (2660181154, 'Zincoid', 2);
INSERT INTO `admin` (id, username, password, email) VALUES
    (2660181154, 'Zincoid', 'KJFHQAUJWDHKIA', '2660181154@qq.com');
