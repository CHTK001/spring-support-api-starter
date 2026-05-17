-- ==========================================================
-- 分表配置表增加 dry-run 模式字段
-- ==========================================================
ALTER TABLE `pay_partition_config`
    ADD COLUMN `pay_partition_config_migration_dry_run` TINYINT(1) DEFAULT 1
    COMMENT 'dry-run 模式: 1-仅复制不删除源数据' AFTER `pay_partition_config_keep_source_data`;
