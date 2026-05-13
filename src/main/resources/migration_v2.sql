-- Migration script for dreams database
-- Run this if your database already exists but lacks the new columns

-- 1. Add 'code' column to dream_category (if not exists)
-- MySQL doesn't support IF NOT EXISTS for ALTER TABLE column, so we use a stored procedure approach
SET @dbname = DATABASE();
SET @tablename = 'dream_category';
SET @columnname = 'code';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      TABLE_SCHEMA = @dbname
      AND TABLE_NAME = @tablename
      AND COLUMN_NAME = @columnname
  ) > 0,
  "SELECT 'Column code already exists in dream_category. Skipping.' AS result;",
  "ALTER TABLE dream_category ADD COLUMN code VARCHAR(30) NOT NULL DEFAULT '' UNIQUE COMMENT '英文标识';
   UPDATE dream_category SET code = 'flying' WHERE name = '飞行';
   UPDATE dream_category SET code = 'falling' WHERE name = '坠落';
   UPDATE dream_category SET code = 'exam' WHERE name = '考试';
   UPDATE dream_category SET code = 'chase' WHERE name = '被追逐';
   UPDATE dream_category SET code = 'water' WHERE name = '水';
   UPDATE dream_category SET code = 'family' WHERE name = '亲人';
   UPDATE dream_category SET code = 'work' WHERE name = '工作';
   UPDATE dream_category SET code = 'ghost' WHERE name = '灵异';
   UPDATE dream_category SET code = 'love' WHERE name = '爱情';
   UPDATE dream_category SET code = 'other' WHERE name = '其他' OR name = '' OR name IS NULL;
   ALTER TABLE dream_category MODIFY COLUMN code VARCHAR(30) NOT NULL UNIQUE COMMENT '英文标识';
   SELECT 'Column code added to dream_category successfully.' AS result;"
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. Update icon column to store emoji instead of text
UPDATE dream_category SET icon = '🦅' WHERE code = 'flying';
UPDATE dream_category SET icon = '🪨' WHERE code = 'falling';
UPDATE dream_category SET icon = '📝' WHERE code = 'exam';
UPDATE dream_category SET icon = '🏃' WHERE code = 'chase';
UPDATE dream_category SET icon = '🌊' WHERE code = 'water';
UPDATE dream_category SET icon = '👨‍👩‍👧' WHERE code = 'family';
UPDATE dream_category SET icon = '💼' WHERE code = 'work';
UPDATE dream_category SET icon = '👻' WHERE code = 'ghost';
UPDATE dream_category SET icon = '💕' WHERE code = 'love';
UPDATE dream_category SET icon = '🔮' WHERE code = 'other';

-- 3. Verify results
SELECT id, name, code, icon, description, sort_order FROM dream_category ORDER BY sort_order;
