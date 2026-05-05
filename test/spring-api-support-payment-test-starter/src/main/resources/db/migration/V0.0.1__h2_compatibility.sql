-- H2 compatibility: V1.0__init_payment.sql fails on ON UPDATE CURRENT_TIMESTAMP
-- This script runs under migration-scan-mode: ALWAYS (no __init_ in name)
CREATE TABLE IF NOT EXISTS `payment_partition_config` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `business_type` VARCHAR(32) NOT NULL,
  `source_table` VARCHAR(64) NOT NULL,
  `partition_prefix` VARCHAR(64) NOT NULL,
  `partition_granularity` VARCHAR(16) NOT NULL DEFAULT 'MONTH',
  `retention_days` INT NOT NULL DEFAULT 365,
  `create_ahead_days` INT NOT NULL DEFAULT 7,
  `migrate_before_days` INT NOT NULL DEFAULT 90,
  `auto_create_enabled` TINYINT(1) NOT NULL DEFAULT 1,
  `auto_migrate_enabled` TINYINT(1) NOT NULL DEFAULT 0,
  `keep_source_data` TINYINT(1) NOT NULL DEFAULT 1,
  `create_task_key` VARCHAR(80) DEFAULT NULL,
  `migrate_task_key` VARCHAR(80) DEFAULT NULL,
  `last_partition_table` VARCHAR(80) DEFAULT NULL,
  `last_partition_at` DATETIME DEFAULT NULL,
  `last_migrate_at` DATETIME DEFAULT NULL,
  `remark` VARCHAR(500) DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_payment_partition_business_type` (`business_type`)
);
