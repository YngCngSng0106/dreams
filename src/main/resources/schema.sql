-- 创建数据库
CREATE DATABASE IF NOT EXISTS dream_share DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE dream_share;

-- 1. 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `nickname` VARCHAR(50) NOT NULL,
    `avatar` VARCHAR(255) DEFAULT NULL,
    `email` VARCHAR(100) DEFAULT NULL,
    `phone` VARCHAR(20) DEFAULT NULL,
    `gender` TINYINT DEFAULT 0,
    `bio` VARCHAR(500) DEFAULT '',
    `role` TINYINT DEFAULT 0 COMMENT '0=user, 1=admin',
    `is_banned` TINYINT DEFAULT 0 COMMENT '0=normal, 1=banned',
    `is_deleted` TINYINT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. 梦境表
DROP TABLE IF EXISTS `dream`;
CREATE TABLE `dream` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `category_id` BIGINT DEFAULT NULL,
    `dream_date` DATE DEFAULT NULL,
    `location` VARCHAR(255) DEFAULT NULL,
    `keywords` VARCHAR(500) DEFAULT NULL,
    `clarity` TINYINT DEFAULT NULL COMMENT '1-5 清晰度评分',
    `description` TEXT NOT NULL,
    `is_recurring` TINYINT(1) DEFAULT 0,
    `tags` VARCHAR(500) DEFAULT NULL,
    `images` TEXT DEFAULT NULL COMMENT 'JSON数组存储图片URL',
    `is_pinned` TINYINT DEFAULT 0 COMMENT '0=not pinned, 1=pinned',
    `is_deleted` TINYINT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_category_id` (`category_id`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_dream_date` (`dream_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 梦境分类表
DROP TABLE IF EXISTS `dream_category`;
CREATE TABLE `dream_category` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(50) NOT NULL,
    `code` VARCHAR(30) NOT NULL UNIQUE COMMENT '英文标识',
    `icon` VARCHAR(10) DEFAULT NULL COMMENT 'emoji图标',
    `description` VARCHAR(255) DEFAULT NULL,
    `sort_order` INT DEFAULT 0,
    `is_deleted` TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入预设分类
INSERT INTO `dream_category` (`name`, `code`, `icon`, `description`, `sort_order`) VALUES
('飞行', 'flying', '🦅', '在天空中飞翔的梦境', 1),
('坠落', 'falling', '🪨', '从高处坠落的梦境', 2),
('考试', 'exam', '📝', '参加考试的梦境', 3),
('被追逐', 'chase', '🏃', '被某物追赶的梦境', 4),
('水', 'water', '🌊', '与水相关的梦境', 5),
('亲人', 'family', '👨‍👩‍👧', '与亲人相关的梦境', 6),
('工作', 'work', '💼', '与工作相关的梦境', 7),
('灵异', 'ghost', '👻', '超自然现象相关的梦境', 8),
('爱情', 'love', '💕', '与爱情相关的梦境', 9),
('其他', 'other', '🔮', '其他类型梦境', 10);

-- 4. 梦境点赞表
DROP TABLE IF EXISTS `dream_like`;
CREATE TABLE `dream_like` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `dream_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_dream_user` (`dream_id`, `user_id`),
    INDEX `idx_dream_id` (`dream_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. 讨论组表
DROP TABLE IF EXISTS `discussion`;
CREATE TABLE `discussion` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(255) NOT NULL,
    `description` TEXT DEFAULT NULL,
    `cover_image` VARCHAR(255) DEFAULT NULL,
    `creator_id` BIGINT NOT NULL,
    `dream_id` BIGINT DEFAULT NULL,
    `is_preseted` TINYINT(1) DEFAULT 0,
    `member_count` INT DEFAULT 1,
    `is_deleted` TINYINT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_creator_id` (`creator_id`),
    INDEX `idx_dream_id` (`dream_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. 讨论组成员表
DROP TABLE IF EXISTS `discussion_member`;
CREATE TABLE `discussion_member` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `discussion_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `role` VARCHAR(20) DEFAULT 'MEMBER' COMMENT 'ADMIN/MEMBER',
    `join_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_discussion_user` (`discussion_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. 评论表
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `discussion_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `parent_id` BIGINT DEFAULT 0 COMMENT '0为顶级评论',
    `content` TEXT NOT NULL,
    `like_count` INT DEFAULT 0,
    `is_hidden` TINYINT DEFAULT 0 COMMENT '0=visible, 1=hidden',
    `is_deleted` TINYINT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_discussion_id` (`discussion_id`),
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. 评论点赞表
DROP TABLE IF EXISTS `comment_like`;
CREATE TABLE `comment_like` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `comment_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. 关注表
DROP TABLE IF EXISTS `follow`;
CREATE TABLE `follow` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `follower_id` BIGINT NOT NULL,
    `followee_id` BIGINT NOT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_follower_followee` (`follower_id`, `followee_id`),
    INDEX `idx_followee_id` (`followee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10. 通知表
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `type` VARCHAR(20) NOT NULL COMMENT 'LIKE/JOIN/COMMENT/FOLLOW/REPLY/INVITE',
    `source_user_id` BIGINT DEFAULT NULL,
    `related_id` BIGINT DEFAULT NULL,
    `content` VARCHAR(500) NOT NULL,
    `is_read` TINYINT(1) DEFAULT 0,
    `is_deleted` TINYINT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_is_read` (`is_read`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 11. 内容审核表
DROP TABLE IF EXISTS `content_audit`;
CREATE TABLE `content_audit` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `target_type` VARCHAR(20) NOT NULL COMMENT 'DREAM/COMMENT',
    `target_id` BIGINT NOT NULL,
    `content_snapshot` TEXT DEFAULT NULL,
    `audit_status` VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING/PASSED/REJECTED',
    `reject_reason` VARCHAR(500) DEFAULT NULL,
    `audited_at` DATETIME DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_audit_status` (`audit_status`),
    INDEX `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 12. 审核关键词表
DROP TABLE IF EXISTS `audit_keyword`;
CREATE TABLE `audit_keyword` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `keyword` VARCHAR(100) NOT NULL,
    `keyword_type` VARCHAR(50) DEFAULT 'SPAM' COMMENT 'SPAM/HARASSMENT/INAPPROPRIATE',
    `severity` VARCHAR(20) DEFAULT 'LOW' COMMENT 'LOW/MEDIUM/HIGH',
    `is_deleted` TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入预设敏感词
INSERT INTO `audit_keyword` (`keyword`, `keyword_type`, `severity`) VALUES
('广告', 'SPAM', 'LOW'),
('兼职', 'SPAM', 'MEDIUM'),
('加Q', 'SPAM', 'HIGH'),
('加微信', 'SPAM', 'HIGH'),
('刷单', 'SPAM', 'HIGH'),
('黄', 'INAPPROPRIATE', 'HIGH'),
('赌', 'INAPPROPRIATE', 'HIGH');

-- 13. 用户设置表
DROP TABLE IF EXISTS `user_settings`;
CREATE TABLE `user_settings` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL UNIQUE,
    `push_enabled` TINYINT(1) DEFAULT 1,
    `is_anonymous_enabled` TINYINT(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 14. 用户会话表
DROP TABLE IF EXISTS `user_session`;
CREATE TABLE `user_session` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `session_id` VARCHAR(255) NOT NULL,
    `online_status` TINYINT(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 15. 操作日志表
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `admin_id` BIGINT NOT NULL,
    `admin_name` VARCHAR(50) NOT NULL,
    `module_name` VARCHAR(50) NOT NULL,
    `operation` VARCHAR(50) NOT NULL,
    `target_id` BIGINT DEFAULT NULL,
    `target_name` VARCHAR(255) DEFAULT NULL,
    `remark` VARCHAR(500) DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_admin_id` (`admin_id`),
    INDEX `idx_module` (`module_name`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入默认管理员账号 (用户名: admin, 密码: admin123, MD5加密)
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `is_banned`, `is_deleted`) VALUES
('admin', '0192023a7bbd73250516f069df18b500', '系统管理员', 1, 0, 0);
