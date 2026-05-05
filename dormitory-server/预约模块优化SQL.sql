-- ===================================================
-- 生活服务预约模块 — 索引与约束优化
-- 在 dormitory_db 库中执行
-- ===================================================

-- 1. 为高频查询字段加索引
ALTER TABLE `service_reservation` ADD INDEX `idx_device_date` (`device_id`, `reservation_date`);
ALTER TABLE `service_reservation` ADD INDEX `idx_student_id` (`student_id`);
ALTER TABLE `service_reservation` ADD INDEX `idx_status` (`status`);

-- 2. 添加唯一约束，数据库层面防止同一设备同一时段重复预约
ALTER TABLE `service_reservation` ADD UNIQUE KEY `uk_device_date_time` (`device_id`, `reservation_date`, `start_time`);
