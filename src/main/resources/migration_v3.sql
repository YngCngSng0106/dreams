-- Admin 管理端迁移脚本 v3
-- 在已有数据库上执行以下SQL

USE dream_share;

-- 1. User 表加 role 和 is_banned
ALTER TABLE `user` ADD COLUMN `role` TINYINT DEFAULT 0 COMMENT '0=user, 1=admin';
ALTER TABLE `user` ADD COLUMN `is_banned` TINYINT DEFAULT 0 COMMENT '0=normal, 1=banned';

-- 2. Dream 表加 is_pinned
ALTER TABLE `dream` ADD COLUMN `is_pinned` TINYINT DEFAULT 0 COMMENT '0=not pinned, 1=pinned';

-- 3. DreamCategory 加 is_deleted
ALTER TABLE `dream_category` ADD COLUMN `is_deleted` TINYINT DEFAULT 0;

-- 4. Comment 加 is_hidden
ALTER TABLE `comment` ADD COLUMN `is_hidden` TINYINT DEFAULT 0 COMMENT '0=visible, 1=hidden';

-- 5. Notification 加 is_deleted
ALTER TABLE `notification` ADD COLUMN `is_deleted` TINYINT DEFAULT 0;

-- 6. AuditKeyword 加 is_deleted
ALTER TABLE `audit_keyword` ADD COLUMN `is_deleted` TINYINT DEFAULT 0;

-- 7. 创建 operation_log 表
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

-- 8. 插入默认管理员 (password MD5: admin123)
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `is_banned`, `is_deleted`) VALUES
('admin', '0192023a7bbd73250516f069df18b500', '系统管理员', 1, 0, 0)
ON DUPLICATE KEY UPDATE `role`=1, `nickname`='系统管理员';
