# CREATE DATABASE nullbot;
# USE nullbot;

DROP TABLE IF EXISTS `tts_template`;

CREATE TABLE `tts_template` (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) UNIQUE NOT NULL,
    path VARCHAR(255) UNIQUE NOT NULL,
    text VARCHAR(255) NOT NULL,
    owner_id BIGINT,
    owner_name VARCHAR(255),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    used BIGINT DEFAULT 0
);
