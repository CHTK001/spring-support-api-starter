-- 创建全局配置表
CREATE TABLE IF NOT EXISTS `monitor_sys_gen_global_setting` (
    `monitor_sys_gen_global_setting_id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '全局配置ID',
    `monitor_sys_gen_global_setting_name` VARCHAR(100) DEFAULT '默认全局配置' COMMENT '配置名称',
    `monitor_sys_gen_global_setting_description` VARCHAR(500) DEFAULT '系统默认全局配置' COMMENT '配置描述',
    `monitor_sys_gen_global_setting_dispatcher_enabled` TINYINT(1) DEFAULT 1 COMMENT '全局调度器总开关 0:关闭 1:开启',
    `monitor_sys_gen_global_setting_status_summary_enabled` TINYINT(1) DEFAULT 1 COMMENT '服务器状态汇总推送开关 0:关闭 1:开启',
    `monitor_sys_gen_global_setting_performance_trends_enabled` TINYINT(1) DEFAULT 1 COMMENT '性能趋势推送开关 0:关闭 1:开启',
    `monitor_sys_gen_global_setting_connection_stats_enabled` TINYINT(1) DEFAULT 1 COMMENT '连接统计推送开关 0:关闭 1:开启',
    `monitor_sys_gen_global_setting_health_status_enabled` TINYINT(1) DEFAULT 1 COMMENT '健康状态推送开关 0:关闭 1:开启',
    `monitor_sys_gen_global_setting_global_alert_enabled` TINYINT(1) DEFAULT 1 COMMENT '全局告警处理开关 0:关闭 1:开启',
    `monitor_sys_gen_global_setting_cleanup_enabled` TINYINT(1) DEFAULT 1 COMMENT '无效任务清理开关 0:关闭 1:开启',
    `monitor_sys_gen_global_setting_config_refresh_enabled` TINYINT(1) DEFAULT 1 COMMENT '配置刷新开关 0:关闭 1:开启',
    `monitor_sys_gen_global_setting_file_upload_check_enabled` TINYINT(1) DEFAULT 1 COMMENT '文件上传检查开关 0:关闭 1:开启',
    `monitor_sys_gen_global_setting_data_cleanup_enabled` TINYINT(1) DEFAULT 1 COMMENT '数据清理开关 0:关闭 1:开启',
    `monitor_sys_gen_global_setting_backup_enabled` TINYINT(1) DEFAULT 1 COMMENT '备份任务开关 0:关闭 1:开启',
    `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`monitor_sys_gen_global_setting_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全局配置设置表';

-- 插入默认全局配置（只有一条记录）
INSERT INTO `monitor_sys_gen_global_setting` (
    `monitor_sys_gen_global_setting_name`,
    `monitor_sys_gen_global_setting_description`,
    `monitor_sys_gen_global_setting_dispatcher_enabled`,
    `monitor_sys_gen_global_setting_status_summary_enabled`,
    `monitor_sys_gen_global_setting_performance_trends_enabled`,
    `monitor_sys_gen_global_setting_connection_stats_enabled`,
    `monitor_sys_gen_global_setting_health_status_enabled`,
    `monitor_sys_gen_global_setting_global_alert_enabled`,
    `monitor_sys_gen_global_setting_cleanup_enabled`,
    `monitor_sys_gen_global_setting_config_refresh_enabled`,
    `monitor_sys_gen_global_setting_file_upload_check_enabled`,
    `monitor_sys_gen_global_setting_data_cleanup_enabled`,
    `monitor_sys_gen_global_setting_backup_enabled`
) VALUES (
    '默认全局配置',
    '系统默认全局配置，控制所有全局任务的开关',
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1
);
