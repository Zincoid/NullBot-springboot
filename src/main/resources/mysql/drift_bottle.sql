# CREATE DATABASE nullbot;
# USE nullbot;

DROP TABLE IF EXISTS `drift_bottle`;

CREATE TABLE `drift_bottle` (
    id INT PRIMARY KEY AUTO_INCREMENT,
    time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id LONG NOT NULL,
    user_name VARCHAR(50) NOT NULL,
    content VARCHAR(2000) NOT NULL,
    is_image BOOL NOT NULL DEFAULT FALSE,
    rethrow_times INT NOT NULL DEFAULT 0
);