# CREATE DATABASE nullbot;
# USE nullbot;

DROP TABLE IF EXISTS `daily`;
DROP TABLE IF EXISTS `stats`;

CREATE TABLE `daily` (
    id INT PRIMARY KEY AUTO_INCREMENT,
    date DATE,
    visits BIGINT DEFAULT 0
);

CREATE TABLE `stats` (
    id INT PRIMARY KEY AUTO_INCREMENT,
    group_id LONG NOT NULL,
    user_id LONG NOT NULL,
    command VARCHAR(50) NOT NULL,
    visits BIGINT DEFAULT 0
);