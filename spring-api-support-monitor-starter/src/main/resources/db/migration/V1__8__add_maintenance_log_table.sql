-- 创建维护日志表
CREATE TABLE IF NOT EXISTS `t_maintenance_log` (
  `maintenance_log_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `maintenance_group_id` bigint(20) NOT NULL COMMENT '维护组ID',
  `maintenance_host_id` bigint(20) DEFAULT NULL COMMENT '维护主机ID',
  `maintenance_script_id` bigint(20) DEFAULT NULL COMMENT '维护脚本ID',
  `maintenance_file_id` bigint(20) DEFAULT NULL COMMENT '维护文件ID',
  `operation_type` tinyint(4) NOT NULL COMMENT '操作类型: 1创建, 2更新, 3删除, 4启用/禁用, 5连接测试, 6执行脚本, 7上传文件, 8部署文件',
  `target_type` tinyint(4) NOT NULL COMMENT '操作对象类型: 1维护组, 2维护主机, 3维护脚本, 4维护文件',
  `operation_user` varchar(100) DEFAULT NULL COMMENT '操作用户',
  `operation_details` text COMMENT '操作详情（JSON格式记录详细信息）',
  `operation_result` tinyint(1) DEFAULT NULL COMMENT '操作结果: 1成功, 0失败',
  `result_message` varchar(500) DEFAULT NULL COMMENT '操作结果消息',
  `ip_address` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `operation_time` datetime NOT NULL COMMENT '操作时间',
  PRIMARY KEY (`maintenance_log_id`),
  KEY `idx_maintenance_group_id` (`maintenance_group_id`),
  KEY `idx_maintenance_host_id` (`maintenance_host_id`),
  KEY `idx_maintenance_script_id` (`maintenance_script_id`),
  KEY `idx_maintenance_file_id` (`maintenance_file_id`),
  KEY `idx_operation_time` (`operation_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维护操作日志表'; 