CREATE TABLE IF NOT EXISTS system_server_setting_item (
  system_server_setting_item_id INT NOT NULL AUTO_INCREMENT COMMENT '主键',
  system_server_setting_item_setting_id INT DEFAULT NULL COMMENT '关联 system_server_setting_id',
  system_server_setting_item_name VARCHAR(255) DEFAULT NULL COMMENT '配置项名称/键',
  system_server_setting_item_value VARCHAR(2000) DEFAULT NULL COMMENT '配置项值',
  system_server_setting_item_description VARCHAR(500) DEFAULT NULL COMMENT '说明',
  system_server_setting_item_type VARCHAR(255) DEFAULT NULL COMMENT '值类型（字符串/数字等）',
  system_server_setting_item_required VARCHAR(1) DEFAULT NULL COMMENT '是否必填（Y/N）',
  system_server_setting_item_default_value VARCHAR(2000) DEFAULT NULL COMMENT '默认值',
  system_server_setting_item_validation_rule VARCHAR(500) DEFAULT NULL COMMENT '校验规则描述或表达式',
  system_server_setting_item_order INT DEFAULT NULL COMMENT '展示/加载顺序',
  create_name VARCHAR(255) DEFAULT NULL COMMENT '创建人姓名',
  create_by INT DEFAULT NULL COMMENT '创建人用户 ID',
  create_time TIMESTAMP NULL DEFAULT NULL COMMENT '创建时间',
  update_time TIMESTAMP NULL DEFAULT NULL COMMENT '更新时间',
  update_name VARCHAR(255) DEFAULT NULL COMMENT '更新人姓名',
  update_by INT DEFAULT NULL COMMENT '更新人用户 ID',
  PRIMARY KEY (system_server_setting_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='过滤器配置项键值明细';
