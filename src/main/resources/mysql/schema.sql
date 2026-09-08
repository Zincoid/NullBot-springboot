-- =============================================
-- NullBot Database Schema
-- Version: 1.0
-- Engine: InnoDB, Charset: utf8mb4
-- =============================================

CREATE DATABASE IF NOT EXISTS nullbot DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE nullbot;

-- =============================================
-- 1. File Table
-- =============================================
CREATE TABLE IF NOT EXISTS `file` (
    `id`             INT          NOT NULL AUTO_INCREMENT  COMMENT 'Primary Key',
    `file_name`      VARCHAR(255) NOT NULL                 COMMENT 'File name',
    `file_size`      BIGINT       NOT NULL                 COMMENT 'File size in bytes',
    `directory`      VARCHAR(255) NOT NULL                 COMMENT 'Parent directory path',
    `is_dir`         BOOLEAN      NOT NULL DEFAULT FALSE   COMMENT 'Whether it is a directory',
    `visible`        BOOLEAN      NOT NULL DEFAULT TRUE    COMMENT 'Whether visible to users',
    `owner_id`       BIGINT       DEFAULT NULL             COMMENT 'Owner QQ number',
    `owner_name`     VARCHAR(255) DEFAULT NULL             COMMENT 'Owner nickname',
    `last_modified`  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Last modified time',
    PRIMARY KEY (`id`),
    KEY `idx_owner_id` (`owner_id`),
    KEY `idx_directory` (`directory`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='File record table';

-- =============================================
-- 2. User Table
-- =============================================
CREATE TABLE IF NOT EXISTS `user` (
    `id`          BIGINT       NOT NULL                 COMMENT 'User QQ number',
    `name`        VARCHAR(255) NOT NULL                 COMMENT 'User nickname',
    `access`      INT          NOT NULL DEFAULT 0       COMMENT 'Access level: 2=ADMIN',
    `level`       INT          NOT NULL DEFAULT 1       COMMENT 'User level',
    `experience`  INT          NOT NULL DEFAULT 0       COMMENT 'Experience points',
    `cash`        INT          NOT NULL DEFAULT 0       COMMENT 'Cash balance',
    `capacity`    INT          NOT NULL DEFAULT 100     COMMENT 'Storage capacity',
    `draw_times`  INT          NOT NULL DEFAULT 50      COMMENT 'Remaining draw times',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User table';

-- =============================================
-- 3. Group Table
-- =============================================
CREATE TABLE IF NOT EXISTS `group` (
    `id`          BIGINT       NOT NULL                 COMMENT 'Group QQ number',
    `name`        VARCHAR(255) NOT NULL                 COMMENT 'Group name',
    `access`      INT          NOT NULL DEFAULT 2       COMMENT 'Access level',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Group table';

-- =============================================
-- 4. Admin Table
-- =============================================
CREATE TABLE IF NOT EXISTS `admin` (
    `id`          BIGINT       NOT NULL                 COMMENT 'Admin QQ number',
    `username`    VARCHAR(255) NOT NULL                 COMMENT 'Admin username',
    `password`    VARCHAR(255) NOT NULL                 COMMENT 'Hashed password',
    `email`       VARCHAR(255) NOT NULL                 COMMENT 'Email address',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Web admin table';

-- =============================================
-- 5. Item Table
-- =============================================
CREATE TABLE IF NOT EXISTS `item` (
    `id`           INT          NOT NULL AUTO_INCREMENT  COMMENT 'Primary Key',
    `name`         VARCHAR(100) NOT NULL                 COMMENT 'Item name',
    `category`     INT          NOT NULL                 COMMENT 'Item category',
    `rarity`       INT          NOT NULL                 COMMENT 'Item rarity',
    `price`        INT          NOT NULL                 COMMENT 'Item price',
    `weight`       INT          NOT NULL                 COMMENT 'Draw weight',
    `description`  VARCHAR(250) DEFAULT NULL             COMMENT 'Item description',
    `command`      VARCHAR(250) DEFAULT NULL             COMMENT 'Command to execute when used, NULL=not usable, e.g. BanUser {userId} 1',
    `image_path`   VARCHAR(100) DEFAULT NULL             COMMENT 'Item image path',
    `available`    BOOLEAN      NOT NULL DEFAULT TRUE    COMMENT 'Whether the item can be drawn',
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category`),
    KEY `idx_rarity` (`rarity`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Item table';

-- =============================================
-- 6. Inventory Table
-- =============================================
CREATE TABLE IF NOT EXISTS `inventory` (
    `id`          INT     NOT NULL AUTO_INCREMENT         COMMENT 'Primary Key',
    `owner_id`    BIGINT  NOT NULL                        COMMENT 'Owner QQ number',
    `item_id`     INT     NOT NULL                        COMMENT 'Item ID',
    `amount`      INT     NOT NULL DEFAULT 1              COMMENT 'Item amount',
    PRIMARY KEY (`id`),
    KEY `idx_owner_id` (`owner_id`),
    KEY `idx_item_id` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User inventory table';

-- =============================================
-- 7. Saying Table
-- =============================================
CREATE TABLE IF NOT EXISTS `saying` (
    `id`          INT          NOT NULL AUTO_INCREMENT  COMMENT 'Primary Key',
    `time`        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record time',
    `user_id`     BIGINT       NOT NULL                 COMMENT 'User QQ number',
    `user_name`   VARCHAR(50)  NOT NULL                 COMMENT 'User nickname',
    `text`        VARCHAR(1000) NOT NULL                COMMENT 'Saying content',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_time` (`time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Saying table';

-- =============================================
-- 8. Bottle Table
-- =============================================
CREATE TABLE IF NOT EXISTS `bottle` (
    `id`             INT          NOT NULL AUTO_INCREMENT  COMMENT 'Primary Key',
    `time`           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Throw time',
    `user_id`        BIGINT       NOT NULL                 COMMENT 'Thrower QQ number',
    `user_name`      VARCHAR(50)  NOT NULL                 COMMENT 'Thrower nickname',
    `content`        VARCHAR(2000) NOT NULL                COMMENT 'Bottle content',
    `is_image`       BOOLEAN      NOT NULL DEFAULT FALSE   COMMENT 'Whether the content is an image',
    `rethrow_times`  INT          NOT NULL DEFAULT 0       COMMENT 'Rethrow count',
    PRIMARY KEY (`id`),
    KEY `idx_time` (`time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Drift bottle table';

-- =============================================
-- 9. Daily Table
-- =============================================
CREATE TABLE IF NOT EXISTS `daily` (
    `id`          INT     NOT NULL AUTO_INCREMENT         COMMENT 'Primary Key',
    `date`        DATE    DEFAULT NULL                    COMMENT 'Statistics date',
    `visits`      BIGINT  NOT NULL DEFAULT 0              COMMENT 'Visit count',
    PRIMARY KEY (`id`),
    KEY `idx_date` (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Daily visit statistics';

-- =============================================
-- 10. Stats Table
-- =============================================
CREATE TABLE IF NOT EXISTS `stats` (
    `id`          INT          NOT NULL AUTO_INCREMENT   COMMENT 'Primary Key',
    `group_id`    BIGINT       NOT NULL                  COMMENT 'Group QQ number',
    `user_id`     BIGINT       NOT NULL                  COMMENT 'User QQ number',
    `command`     VARCHAR(50)  NOT NULL                  COMMENT 'Command name',
    `visits`      BIGINT       NOT NULL DEFAULT 0        COMMENT 'Visit count',
    PRIMARY KEY (`id`),
    KEY `idx_command` (`group_id`, `user_id`, `command`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Per-user command statistics';

-- =============================================
-- 11. TTS Template Table
-- =============================================
CREATE TABLE IF NOT EXISTS `tts_template` (
    `id`            INT          NOT NULL AUTO_INCREMENT  COMMENT 'Primary Key',
    `name`          VARCHAR(255) NOT NULL                 COMMENT 'Template name',
    `path`          VARCHAR(255) NOT NULL                 COMMENT 'Template audio path',
    `text`          VARCHAR(255) NOT NULL                 COMMENT 'Template text',
    `owner_id`      BIGINT       DEFAULT NULL             COMMENT 'Owner QQ number',
    `owner_name`    VARCHAR(255) DEFAULT NULL             COMMENT 'Owner nickname',
    `created_time`  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `used`          BIGINT       NOT NULL DEFAULT 0       COMMENT 'Usage count',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    UNIQUE KEY `uk_path` (`path`),
    KEY `idx_owner_id` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='TTS template table';

-- =============================================
-- Preset data: admin-level user
-- =============================================
INSERT INTO `user` (id, name, access) VALUES
    (2660181154, 'Zincoid', 2);

-- =============================================
-- Item seed data lives in mysql/data/itemlist.sql
-- =============================================
