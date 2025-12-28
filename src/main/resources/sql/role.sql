# CREATE DATABASE NullBot;
# USE NullBot;

DROP TABLE IF EXISTS `group`;
DROP TABLE IF EXISTS `user`;
drop table if exists `admin`;

CREATE TABLE `group` (
    id BIGINT PRIMARY KEY,
    access INT DEFAULT 2
);

CREATE TABLE `user` (
    id BIGINT PRIMARY KEY,
    access INT DEFAULT 0,

    level INT DEFAULT 1,
    cash INT DEFAULT 0,
    capacity INT DEFAULT 100,
    draw_times INT DEFAULT 50
);

create table `admin`
(
    id bigint auto_increment primary key,
    username varchar(255) not null,
    password varchar(255) not null,
    email varchar(255) not null
);

INSERT INTO `user` (id, access) values
    (2660181154, 2);

INSERT INTO `admin` (id, username, password, email) values
    (2660181154, 'Zincoid', 'KJFHQAUJWDHKIA', '2660181154@qq.com');
