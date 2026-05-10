CREATE TABLE IF NOT EXISTS `pay_global_config` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `config_key` VARCHAR(64) NOT NULL DEFAULT 'DEFAULT' COMMENT '配置键',
  `notify_base_url` VARCHAR(255) DEFAULT NULL COMMENT '回调基础地址',
  `return_url` VARCHAR(255) DEFAULT NULL COMMENT '默认回跳地址',
  `callback_path_template` VARCHAR(255) DEFAULT '/{orderNo}/{merchantId}' COMMENT '回调路径模板',
  `auto_refresh_seconds` INT DEFAULT 60 COMMENT '首页自动刷新秒数',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `sample_pay_notify_url` VARCHAR(500) DEFAULT NULL COMMENT '支付回调示例 URL',
  `sample_refund_notify_url` VARCHAR(500) DEFAULT NULL COMMENT '退款回调示例 URL',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `uk_global_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付全局配置表';
