CREATE TABLE IF NOT EXISTS `sys_ai_global_setting` (
  `sys_ai_global_setting_id` INT NOT NULL AUTO_INCREMENT COMMENT '全局配置主键ID',
  `sys_ai_global_setting_group` VARCHAR(64) NOT NULL DEFAULT 'default' COMMENT '配置分组',
  `sys_ai_global_setting_field` VARCHAR(128) NOT NULL COMMENT '配置字段',
  `sys_ai_global_setting_value` LONGTEXT COMMENT '配置值',
  `sys_ai_global_setting_value_type` VARCHAR(32) NOT NULL DEFAULT 'STRING' COMMENT '配置值类型',
  `sys_ai_global_setting_description` VARCHAR(512) DEFAULT NULL COMMENT '配置说明',
  `sys_ai_global_setting_sort` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `sys_ai_global_setting_status` INT NOT NULL DEFAULT 1 COMMENT '状态',
  `create_name` VARCHAR(255) DEFAULT NULL,
  `create_by` INT DEFAULT NULL,
  `create_time` DATETIME DEFAULT NULL,
  `update_time` DATETIME DEFAULT NULL,
  `update_name` VARCHAR(255) DEFAULT NULL,
  `update_by` INT DEFAULT NULL,
  PRIMARY KEY (`sys_ai_global_setting_id`),
  UNIQUE KEY `uk_ai_global_setting_field` (`sys_ai_global_setting_group`, `sys_ai_global_setting_field`)
);
