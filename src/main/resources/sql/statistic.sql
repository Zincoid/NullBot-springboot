# CREATE DATABASE nullbot;
# USE nullbot;

DROP TABLE IF EXISTS `statistic_date`;
DROP TABLE IF EXISTS `statistic`;

CREATE TABLE `statistic_date` (
    id INT PRIMARY KEY AUTO_INCREMENT,
    date DATE,
    visits BIGINT DEFAULT 0
);

CREATE TABLE `statistic` (
    id INT PRIMARY KEY AUTO_INCREMENT,
    group_id LONG NOT NULL,
    user_id LONG NOT NULL,
    user_name VARCHAR(50) NOT NULL,
    command VARCHAR(50) NOT NULL,
    visits BIGINT DEFAULT 0
);