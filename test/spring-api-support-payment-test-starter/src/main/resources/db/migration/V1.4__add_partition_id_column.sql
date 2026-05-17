-- ==========================================================
-- 分表配置表增加 id_column 字段，并补充已有记录的列名
-- ==========================================================
ALTER TABLE `pay_partition_config`
    ADD COLUMN `pay_partition_config_id_column` VARCHAR(64) DEFAULT NULL
    COMMENT '主键列名(如 pay_order_id)' AFTER `pay_partition_config_time_column`;

-- 为已有记录设置默认时间列名和主键列名
UPDATE `pay_partition_config`
SET `pay_partition_config_time_column` = CONCAT(`pay_partition_config_source_table`, '_created_at'),
    `pay_partition_config_id_column` = CONCAT(`pay_partition_config_source_table`, '_id')
WHERE `pay_partition_config_time_column` IS NULL
   OR `pay_partition_config_id_column` IS NULL;
