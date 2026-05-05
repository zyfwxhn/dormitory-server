-- ===================================================
-- 失物招领模块 — 索引优化
-- 在 dormitory_db 库中执行
-- ===================================================

ALTER TABLE `lost_found` ADD INDEX `idx_type_status` (`type`, `status`);
ALTER TABLE `lost_found` ADD INDEX `idx_student_id` (`student_id`);
ALTER TABLE `lost_found` ADD INDEX `idx_category` (`category`);
