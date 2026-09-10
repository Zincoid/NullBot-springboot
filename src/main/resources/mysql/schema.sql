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
-- 5. Setting Table
-- =============================================
CREATE TABLE IF NOT EXISTS `setting` (
    `id`                     INT           NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
    `group_id`               BIGINT        NOT NULL               COMMENT 'Group QQ number',
    `limit_scope`            VARCHAR(20)   NOT NULL DEFAULT 'USER' COMMENT 'Rate limit scope: USER/GROUP/CMD',
    `limit_capacity`         INT           NOT NULL DEFAULT 5     COMMENT 'Rate limit capacity',
    `limit_refill`           INT           NOT NULL DEFAULT 2     COMMENT 'Rate limit refill amount',
    `limit_interval`         INT           NOT NULL DEFAULT 1     COMMENT 'Rate limit refill interval in minutes',
    `chat_scope`             VARCHAR(20)   NOT NULL DEFAULT 'GROUP' COMMENT 'Chat scope: GROUP/PERSONAL/MONITOR',
    `chat_strategy`          VARCHAR(20)   NOT NULL DEFAULT 'EMBEDDING' COMMENT 'Chat strategy: DIRECT/EMBEDDING/TOOLS',
    `thinking`               BOOLEAN       NOT NULL DEFAULT FALSE COMMENT 'AI thinking mode',
    `voice`                  BOOLEAN       NOT NULL DEFAULT FALSE COMMENT 'AI voice mode',
    `vision`                 BOOLEAN       NOT NULL DEFAULT FALSE COMMENT 'AI vision mode',
    `inner_cmd_auth`         BOOLEAN       NOT NULL DEFAULT FALSE COMMENT 'Inner command auth',
    `anti_injection`         BOOLEAN       NOT NULL DEFAULT TRUE  COMMENT 'Anti prompt injection',
    `custom`                 BOOLEAN       NOT NULL DEFAULT FALSE COMMENT 'Allow custom settings',
    `auto_reply`             BOOLEAN       NOT NULL DEFAULT FALSE COMMENT 'Auto reply',
    `reply_frequency`        DOUBLE        NOT NULL DEFAULT 0.001 COMMENT 'Auto reply frequency',
    `image_collect`          BOOLEAN       NOT NULL DEFAULT FALSE COMMENT 'Image collect',
    `message_collect`        BOOLEAN       NOT NULL DEFAULT TRUE  COMMENT 'Message collect',
    `keyword_detect`         BOOLEAN       NOT NULL DEFAULT FALSE COMMENT 'Keyword detect',
    `poke_detect`            BOOLEAN       NOT NULL DEFAULT TRUE  COMMENT 'Poke detect',
    `recall_detect`          BOOLEAN       NOT NULL DEFAULT FALSE COMMENT 'Recall detect',
    `guess_crop_ratio`       DOUBLE        NOT NULL DEFAULT 0.1   COMMENT 'Guess crop ratio',
    `guess_transparent_ratio` DOUBLE       NOT NULL DEFAULT 0.75  COMMENT 'Guess transparent ratio',
    `guess_padding`          INT           NOT NULL DEFAULT 250   COMMENT 'Guess padding',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Group setting table';

-- =============================================
-- 6. Item Table
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
-- 7. Inventory Table
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
-- 8. Saying Table
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
-- 9. Bottle Table
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
-- 10. Daily Table
-- =============================================
CREATE TABLE IF NOT EXISTS `daily` (
    `id`          INT     NOT NULL AUTO_INCREMENT         COMMENT 'Primary Key',
    `date`        DATE    DEFAULT NULL                    COMMENT 'Statistics date',
    `visits`      BIGINT  NOT NULL DEFAULT 0              COMMENT 'Visit count',
    PRIMARY KEY (`id`),
    KEY `idx_date` (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Daily visit statistics';

-- =============================================
-- 11. Stats Table
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
-- 12. TTS Template Table
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
-- 13. Message Table
-- =============================================
CREATE TABLE IF NOT EXISTS `message` (
    `id`        BIGINT       NOT NULL AUTO_INCREMENT  COMMENT 'Primary Key',
    `chat_id`   VARCHAR(64)  NOT NULL                 COMMENT 'Chat session id',
    `payload`   LONGTEXT     NOT NULL                 COMMENT 'Serialized message JSON',
    PRIMARY KEY (`id`),
    KEY `idx_chat` (`chat_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Chat history table';

-- =============================================
-- Preset data: admin-level user
-- =============================================
INSERT INTO `user` (id, name, access) VALUES
    (2660181154, 'Zincoid', 2);

-- =============================================
-- Item seed data lives in mysql/data.sql
-- =============================================
