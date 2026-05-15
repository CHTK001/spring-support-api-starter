-- AI 模块 H2 兼容建表脚本
-- 来源: spring-support-ai-starter/db/init/V1.0__init_ai.sql (H2 适配版)

CREATE TABLE IF NOT EXISTS `sys_ai_module` (
  `sys_ai_module_id` INT NOT NULL AUTO_INCREMENT COMMENT '模块ID',
  `sys_ai_module_name` VARCHAR(255) DEFAULT NULL COMMENT '模块名称',
  `sys_ai_module_code` VARCHAR(255) DEFAULT NULL COMMENT '模块编码',
  `sys_ai_module_type` VARCHAR(64) DEFAULT NULL COMMENT '模块类型',
  `sys_ai_module_status` INT DEFAULT 1 COMMENT '模块状态',
  `sys_ai_module_config` TEXT DEFAULT NULL COMMENT '模块配置',
  `create_name` VARCHAR(255) DEFAULT NULL,
  `create_by` INT DEFAULT NULL,
  `create_time` DATETIME DEFAULT NULL,
  `update_time` DATETIME DEFAULT NULL,
  `update_name` VARCHAR(255) DEFAULT NULL,
  `update_by` INT DEFAULT NULL,
  PRIMARY KEY (`sys_ai_module_id`)
);

CREATE TABLE IF NOT EXISTS `sys_ai_manufacturer` (
  `sys_ai_manufacturer_id` INT NOT NULL AUTO_INCREMENT COMMENT '厂商ID',
  `sys_ai_manufacturer_name` VARCHAR(255) DEFAULT NULL COMMENT '厂商名称',
  `sys_ai_manufacturer_code` VARCHAR(255) DEFAULT NULL COMMENT '厂商编码',
  `sys_ai_manufacturer_type` VARCHAR(64) DEFAULT NULL COMMENT '厂商类型',
  `sys_ai_manufacturer_status` INT DEFAULT 1 COMMENT '厂商状态',
  `sys_ai_manufacturer_config` TEXT DEFAULT NULL COMMENT '厂商配置',
  `create_name` VARCHAR(255) DEFAULT NULL,
  `create_by` INT DEFAULT NULL,
  `create_time` DATETIME DEFAULT NULL,
  `update_time` DATETIME DEFAULT NULL,
  `update_name` VARCHAR(255) DEFAULT NULL,
  `update_by` INT DEFAULT NULL,
  PRIMARY KEY (`sys_ai_manufacturer_id`)
);

CREATE TABLE IF NOT EXISTS `sys_ai_session_compression` (
  `sys_ai_session_compression_id` INT NOT NULL AUTO_INCREMENT,
  `sys_ai_session_id` VARCHAR(128) DEFAULT NULL,
  `compression_type` VARCHAR(64) DEFAULT NULL,
  `compression_data` LONGTEXT DEFAULT NULL,
  `create_name` VARCHAR(255) DEFAULT NULL,
  `create_by` INT DEFAULT NULL,
  `create_time` DATETIME DEFAULT NULL,
  `update_time` DATETIME DEFAULT NULL,
  `update_name` VARCHAR(255) DEFAULT NULL,
  `update_by` INT DEFAULT NULL,
  PRIMARY KEY (`sys_ai_session_compression_id`)
);

CREATE TABLE IF NOT EXISTS `sys_ai_session` (
  `sys_ai_session_id` INT NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `sys_ai_session_code` VARCHAR(255) DEFAULT NULL COMMENT '会话编码',
  `sys_ai_session_name` VARCHAR(255) DEFAULT NULL COMMENT '会话名称',
  `sys_ai_session_type` VARCHAR(64) DEFAULT NULL COMMENT '会话类型',
  `sys_ai_session_status` INT DEFAULT 1 COMMENT '会话状态',
  `create_name` VARCHAR(255) DEFAULT NULL,
  `create_by` INT DEFAULT NULL,
  `create_time` DATETIME DEFAULT NULL,
  `update_time` DATETIME DEFAULT NULL,
  `update_name` VARCHAR(255) DEFAULT NULL,
  `update_by` INT DEFAULT NULL,
  PRIMARY KEY (`sys_ai_session_id`)
);

CREATE TABLE IF NOT EXISTS `sys_ai_message` (
  `sys_ai_message_id` INT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `sys_ai_session_code` VARCHAR(255) DEFAULT NULL COMMENT '会话编码',
  `sys_ai_message_type` VARCHAR(64) DEFAULT NULL COMMENT '消息类型',
  `sys_ai_message_role` VARCHAR(64) DEFAULT NULL COMMENT '消息角色',
  `sys_ai_message_content` LONGTEXT DEFAULT NULL COMMENT '消息内容',
  `create_name` VARCHAR(255) DEFAULT NULL,
  `create_by` INT DEFAULT NULL,
  `create_time` DATETIME DEFAULT NULL,
  `update_time` DATETIME DEFAULT NULL,
  `update_name` VARCHAR(255) DEFAULT NULL,
  `update_by` INT DEFAULT NULL,
  PRIMARY KEY (`sys_ai_message_id`)
);

CREATE TABLE IF NOT EXISTS `sys_ai_task` (
  `sys_ai_task_id` INT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `sys_ai_task_code` VARCHAR(255) DEFAULT NULL COMMENT '任务编码',
  `sys_ai_task_name` VARCHAR(255) DEFAULT NULL COMMENT '任务名称',
  `sys_ai_task_type` VARCHAR(64) DEFAULT NULL COMMENT '任务类型',
  `sys_ai_task_status` INT DEFAULT 1 COMMENT '任务状态',
  `create_name` VARCHAR(255) DEFAULT NULL,
  `create_by` INT DEFAULT NULL,
  `create_time` DATETIME DEFAULT NULL,
  `update_time` DATETIME DEFAULT NULL,
  `update_name` VARCHAR(255) DEFAULT NULL,
  `update_by` INT DEFAULT NULL,
  PRIMARY KEY (`sys_ai_task_id`)
);

CREATE TABLE IF NOT EXISTS `sys_ai_style` (
  `sys_ai_style_id` INT NOT NULL AUTO_INCREMENT COMMENT '风格ID',
  `sys_ai_style_name` VARCHAR(255) DEFAULT NULL COMMENT '风格名称',
  `sys_ai_style_code` VARCHAR(255) DEFAULT NULL COMMENT '风格编码',
  `sys_ai_style_type` VARCHAR(64) DEFAULT NULL COMMENT '风格类型',
  `sys_ai_style_status` INT DEFAULT 1 COMMENT '风格状态',
  `create_name` VARCHAR(255) DEFAULT NULL,
  `create_by` INT DEFAULT NULL,
  `create_time` DATETIME DEFAULT NULL,
  `update_time` DATETIME DEFAULT NULL,
  `update_name` VARCHAR(255) DEFAULT NULL,
  `update_by` INT DEFAULT NULL,
  PRIMARY KEY (`sys_ai_style_id`)
);

CREATE TABLE IF NOT EXISTS `sys_ai_template` (
  `sys_ai_template_id` INT NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `sys_ai_template_name` VARCHAR(255) DEFAULT NULL COMMENT '模板名称',
  `sys_ai_template_code` VARCHAR(255) DEFAULT NULL COMMENT '模板编码',
  `sys_ai_template_type` VARCHAR(64) DEFAULT NULL COMMENT '模板类型',
  `sys_ai_template_status` INT DEFAULT 1 COMMENT '模板状态',
  `sys_ai_template_content` TEXT DEFAULT NULL COMMENT '模板内容',
  `create_name` VARCHAR(255) DEFAULT NULL,
  `create_by` INT DEFAULT NULL,
  `create_time` DATETIME DEFAULT NULL,
  `update_time` DATETIME DEFAULT NULL,
  `update_name` VARCHAR(255) DEFAULT NULL,
  `update_by` INT DEFAULT NULL,
  PRIMARY KEY (`sys_ai_template_id`)
);

CREATE TABLE IF NOT EXISTS `sys_ai_setting` (
  `sys_ai_setting_id` INT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `sys_ai_setting_name` VARCHAR(255) DEFAULT NULL COMMENT '配置名称',
  `sys_ai_setting_code` VARCHAR(255) DEFAULT NULL COMMENT '配置编码',
  `sys_ai_setting_type` VARCHAR(64) DEFAULT NULL COMMENT '配置类型',
  `sys_ai_setting_value` TEXT DEFAULT NULL COMMENT '配置值',
  `sys_ai_setting_status` INT DEFAULT 1 COMMENT '配置状态',
  `create_name` VARCHAR(255) DEFAULT NULL,
  `create_by` INT DEFAULT NULL,
  `create_time` DATETIME DEFAULT NULL,
  `update_time` DATETIME DEFAULT NULL,
  `update_name` VARCHAR(255) DEFAULT NULL,
  `update_by` INT DEFAULT NULL,
  PRIMARY KEY (`sys_ai_setting_id`)
);

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

CREATE TABLE IF NOT EXISTS `sys_ai_app_key` (
  `sys_ai_app_key_id` INT NOT NULL AUTO_INCREMENT COMMENT '应用密钥主键ID',
  `sys_ai_app_key_app_id` VARCHAR(128) DEFAULT NULL COMMENT '应用ID',
  `sys_ai_app_key_app_key` VARCHAR(128) NOT NULL COMMENT '访问Key',
  `sys_ai_app_key_secret` VARCHAR(256) NOT NULL COMMENT '访问Secret',
  `sys_ai_app_key_name` VARCHAR(255) DEFAULT NULL COMMENT '密钥名称',
  `sys_user_id` INT DEFAULT NULL COMMENT '所属用户ID',
  `sys_ai_app_key_status` INT NOT NULL DEFAULT 1 COMMENT '状态',
  `sys_ai_app_key_delete` INT NOT NULL DEFAULT 0 COMMENT '删除标记',
  `sys_ai_app_key_expire_time` BIGINT DEFAULT NULL COMMENT '过期时间戳',
  `sys_ai_app_key_valid_days` INT DEFAULT NULL COMMENT '有效天数',
  `sys_ai_app_key_ip_whitelist` VARCHAR(512) DEFAULT NULL COMMENT 'IP白名单',
  `sys_ai_app_key_remark` VARCHAR(512) DEFAULT NULL COMMENT '备注说明',
  `sys_ai_app_key_last_used_time` BIGINT DEFAULT NULL COMMENT '最近使用时间戳',
  `sys_ai_app_key_use_count` INT DEFAULT 0 COMMENT '使用次数',
  `sys_ai_app_key_scope` VARCHAR(255) DEFAULT NULL COMMENT '授权范围',
  `create_name` VARCHAR(255) DEFAULT NULL,
  `create_by` INT DEFAULT NULL,
  `create_time` DATETIME DEFAULT NULL,
  `update_time` DATETIME DEFAULT NULL,
  `update_name` VARCHAR(255) DEFAULT NULL,
  `update_by` INT DEFAULT NULL,
  PRIMARY KEY (`sys_ai_app_key_id`),
  UNIQUE KEY `uk_ai_app_key_value` (`sys_ai_app_key_app_key`)
);

CREATE TABLE IF NOT EXISTS `sys_ai_face_library` (
  `sys_ai_face_library_id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `sys_ai_face_library_name` VARCHAR(128) NOT NULL COMMENT '人脸名称/标签',
  `sys_ai_face_library_full_name` VARCHAR(128) DEFAULT NULL COMMENT '姓名',
  `sys_ai_face_library_id_card` VARCHAR(64) DEFAULT NULL COMMENT '身份证号',
  `sys_ai_face_library_vector_id` VARCHAR(128) NOT NULL COMMENT '向量存储中的向量ID',
  `sys_ai_face_library_thumbnail` LONGTEXT COMMENT '人脸缩略图(base64)',
  `sys_ai_face_library_group` VARCHAR(64) DEFAULT 'default' COMMENT '人脸分组',
  `sys_ai_face_library_meta` VARCHAR(2048) DEFAULT NULL COMMENT '扩展元数据(JSON)',
  `sys_ai_face_library_status` INT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
  `create_name` VARCHAR(255) DEFAULT NULL,
  `create_by` INT DEFAULT NULL,
  `create_time` DATETIME DEFAULT NULL,
  `update_time` DATETIME DEFAULT NULL,
  `update_name` VARCHAR(255) DEFAULT NULL,
  `update_by` INT DEFAULT NULL,
  PRIMARY KEY (`sys_ai_face_library_id`),
  UNIQUE KEY `uk_face_vector_id` (`sys_ai_face_library_vector_id`),
  KEY `idx_face_group` (`sys_ai_face_library_group`),
  KEY `idx_face_name` (`sys_ai_face_library_name`)
);

CREATE TABLE IF NOT EXISTS `sys_ai_face_recognize_record` (
  `sys_ai_face_recognize_record_id` INT NOT NULL AUTO_INCREMENT COMMENT '识别记录ID',
  `sys_ai_face_library_id` INT DEFAULT NULL COMMENT '人脸库ID',
  `sys_ai_face_recognize_score` DOUBLE DEFAULT NULL COMMENT '识别分数',
  `sys_ai_face_recognize_image` LONGTEXT COMMENT '识别图片(base64)',
  `create_name` VARCHAR(255) DEFAULT NULL,
  `create_by` INT DEFAULT NULL,
  `create_time` DATETIME DEFAULT NULL,
  `update_time` DATETIME DEFAULT NULL,
  `update_name` VARCHAR(255) DEFAULT NULL,
  `update_by` INT DEFAULT NULL,
  PRIMARY KEY (`sys_ai_face_recognize_record_id`)
);

CREATE TABLE IF NOT EXISTS `sys_ai_face_group` (
  `sys_ai_face_group_id` INT NOT NULL AUTO_INCREMENT COMMENT '分组ID',
  `sys_ai_face_group_name` VARCHAR(128) NOT NULL COMMENT '分组名称',
  `sys_ai_face_group_code` VARCHAR(64) DEFAULT NULL COMMENT '分组编码',
  `sys_ai_face_group_status` INT NOT NULL DEFAULT 1 COMMENT '分组状态',
  `create_name` VARCHAR(255) DEFAULT NULL,
  `create_by` INT DEFAULT NULL,
  `create_time` DATETIME DEFAULT NULL,
  `update_time` DATETIME DEFAULT NULL,
  `update_name` VARCHAR(255) DEFAULT NULL,
  `update_by` INT DEFAULT NULL,
  PRIMARY KEY (`sys_ai_face_group_id`)
);

CREATE TABLE IF NOT EXISTS `sys_ai_image_library` (
  `sys_ai_image_library_id` INT NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `sys_ai_image_library_name` VARCHAR(255) DEFAULT NULL COMMENT '图片名称',
  `sys_ai_image_library_url` VARCHAR(1024) DEFAULT NULL COMMENT '图片地址',
  `sys_ai_image_library_type` VARCHAR(64) DEFAULT NULL COMMENT '图片类型',
  `sys_ai_image_library_status` INT DEFAULT 1 COMMENT '图片状态',
  `create_name` VARCHAR(255) DEFAULT NULL,
  `create_by` INT DEFAULT NULL,
  `create_time` DATETIME DEFAULT NULL,
  `update_time` DATETIME DEFAULT NULL,
  `update_name` VARCHAR(255) DEFAULT NULL,
  `update_by` INT DEFAULT NULL,
  PRIMARY KEY (`sys_ai_image_library_id`)
);

CREATE TABLE IF NOT EXISTS `sys_ai_knowledge_graph_entity` (
  `sys_ai_knowledge_graph_entity_id` INT NOT NULL AUTO_INCREMENT COMMENT '实体ID',
  `entity_name` VARCHAR(255) DEFAULT NULL COMMENT '实体名称',
  `entity_type` VARCHAR(255) DEFAULT NULL COMMENT '实体类型',
  `entity_description` TEXT DEFAULT NULL COMMENT '实体描述',
  `entity_weight` DOUBLE DEFAULT NULL COMMENT '实体权重',
  `entity_logo` VARCHAR(1024) DEFAULT NULL COMMENT '实体图标',
  `metadata_json` TEXT DEFAULT NULL COMMENT '扩展元数据',
  `create_name` VARCHAR(255) DEFAULT NULL,
  `create_by` INT DEFAULT NULL,
  `create_time` DATETIME DEFAULT NULL,
  `update_time` DATETIME DEFAULT NULL,
  `update_name` VARCHAR(255) DEFAULT NULL,
  `update_by` INT DEFAULT NULL,
  PRIMARY KEY (`sys_ai_knowledge_graph_entity_id`)
);

CREATE TABLE IF NOT EXISTS `sys_ai_knowledge_graph_relation` (
  `sys_ai_knowledge_graph_relation_id` INT NOT NULL AUTO_INCREMENT COMMENT '关系ID',
  `source_entity_name` VARCHAR(255) DEFAULT NULL COMMENT '源实体名称',
  `target_entity_name` VARCHAR(255) DEFAULT NULL COMMENT '目标实体名称',
  `relation_type` VARCHAR(255) DEFAULT NULL COMMENT '关系类型',
  `relation_weight` DOUBLE DEFAULT NULL COMMENT '关系权重',
  `relation_description` TEXT DEFAULT NULL COMMENT '关系描述',
  `create_name` VARCHAR(255) DEFAULT NULL,
  `create_by` INT DEFAULT NULL,
  `create_time` DATETIME DEFAULT NULL,
  `update_time` DATETIME DEFAULT NULL,
  `update_name` VARCHAR(255) DEFAULT NULL,
  `update_by` INT DEFAULT NULL,
  PRIMARY KEY (`sys_ai_knowledge_graph_relation_id`)
);

CREATE TABLE IF NOT EXISTS `sys_ai_knowledge_library` (
  `sys_ai_knowledge_library_id` INT NOT NULL AUTO_INCREMENT COMMENT '知识库ID',
  `library_name` VARCHAR(255) DEFAULT NULL COMMENT '知识库名称',
  `library_type` VARCHAR(64) DEFAULT NULL COMMENT '知识库类型',
  `library_description` TEXT DEFAULT NULL COMMENT '知识库描述',
  `create_name` VARCHAR(255) DEFAULT NULL,
  `create_by` INT DEFAULT NULL,
  `create_time` DATETIME DEFAULT NULL,
  `update_time` DATETIME DEFAULT NULL,
  `update_name` VARCHAR(255) DEFAULT NULL,
  `update_by` INT DEFAULT NULL,
  PRIMARY KEY (`sys_ai_knowledge_library_id`)
);

CREATE TABLE IF NOT EXISTS `sys_ai_reid_library` (
  `sys_ai_reid_library_id` INT NOT NULL AUTO_INCREMENT COMMENT 'ReID库ID',
  `sys_ai_reid_library_name` VARCHAR(128) NOT NULL COMMENT 'ReID库名称',
  `sys_ai_reid_library_type` VARCHAR(64) DEFAULT NULL COMMENT 'ReID库类型',
  `sys_ai_reid_library_vector_id` VARCHAR(128) NOT NULL COMMENT '向量ID',
  `create_name` VARCHAR(255) DEFAULT NULL,
  `create_by` INT DEFAULT NULL,
  `create_time` DATETIME DEFAULT NULL,
  `update_time` DATETIME DEFAULT NULL,
  `update_name` VARCHAR(255) DEFAULT NULL,
  `update_by` INT DEFAULT NULL,
  PRIMARY KEY (`sys_ai_reid_library_id`)
);

-- ── 默认厂商（ONNX 本地推理） ───────────────────────
INSERT INTO `sys_ai_manufacturer` (`sys_ai_manufacturer_id`, `sys_ai_manufacturer_name`, `sys_ai_manufacturer_code`, `sys_ai_manufacturer_type`, `sys_ai_manufacturer_status`)
SELECT 1, '本地 ONNX 推理', 'local-onnx', 'ONNX', 1
WHERE NOT EXISTS (SELECT 1 FROM `sys_ai_manufacturer` WHERE `sys_ai_manufacturer_code` = 'local-onnx');

-- ── 模块：文本超分辨率 ──────────────────────────────
INSERT INTO `sys_ai_module` (`sys_ai_module_code`, `sys_ai_module_name`, `sys_ai_module_type`, `sys_ai_module_status`)
SELECT 'local-onnx-text-sr', '本地 ONNX 文本超分辨率 (TextBSR)', 'TEXT_SR', 1
WHERE NOT EXISTS (SELECT 1 FROM `sys_ai_module` WHERE `sys_ai_module_code` = 'local-onnx-text-sr');

-- ── 模块：文档对齐校正 ──────────────────────────────
INSERT INTO `sys_ai_module` (`sys_ai_module_code`, `sys_ai_module_name`, `sys_ai_module_type`, `sys_ai_module_status`)
SELECT 'local-onnx-doc-align', '本地 ONNX 文档对齐校正 (MLSD)', 'ALIGNMENT', 1
WHERE NOT EXISTS (SELECT 1 FROM `sys_ai_module` WHERE `sys_ai_module_code` = 'local-onnx-doc-align');
