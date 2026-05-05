-- ===================================================
-- 二手交易模块 — 索引优化
-- 在 dormitory_db 库中执行
-- ===================================================

ALTER TABLE `secondhand_item` ADD INDEX `idx_status` (`status`);
ALTER TABLE `secondhand_item` ADD INDEX `idx_student_id` (`student_id`);
ALTER TABLE `secondhand_item` ADD INDEX `idx_category` (`category`);

ALTER TABLE `item_message` ADD INDEX `idx_item_id` (`item_id`);
