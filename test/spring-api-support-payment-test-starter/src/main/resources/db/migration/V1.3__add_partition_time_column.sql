-- ==========================================================
-- 分表配置表增加 time_column 字段
-- ==========================================================
ALTER TABLE `pay_partition_config`
    ADD COLUMN `pay_partition_config_time_column` VARCHAR(64) DEFAULT NULL
    COMMENT '时间列名(如 pay_order_created_at)' AFTER `pay_partition_config_migration_dry_run`;

-- 为已有记录设置默认时间列名
UPDATE `pay_partition_config`
SET `pay_partition_config_time_column` = CONCAT(`pay_partition_config_source_table`, '_created_at')
WHERE `pay_partition_config_time_column` IS NULL;
