-- 迁移脚本 v4: 用户表增加 phone 字段
USE dream_share;

-- 如果 phone 字段不存在则添加
ALTER TABLE `user` ADD COLUMN IF NOT EXISTS `phone` VARCHAR(20) DEFAULT NULL;
