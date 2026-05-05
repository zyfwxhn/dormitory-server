-- ===================================================
-- 项目补充索引优化（尚未执行的建议索引）
-- 在 dormitory_db 库中执行
-- ===================================================

-- 报修订单表（高频查询字段）
ALTER TABLE `repair_order` ADD INDEX `idx_student_id` (`student_id`);
ALTER TABLE `repair_order` ADD INDEX `idx_worker_id` (`worker_id`);
ALTER TABLE `repair_order` ADD INDEX `idx_status` (`status`);

-- 设备资源表（管理员条件查询 + 学生按楼栋查）
ALTER TABLE `device_resource` ADD INDEX `idx_building_no` (`building_no`);
ALTER TABLE `device_resource` ADD INDEX `idx_status` (`status`);

-- 通知表（学生查未读通知）
ALTER TABLE `notification` ADD INDEX `idx_student_read` (`student_id`, `is_read`);
