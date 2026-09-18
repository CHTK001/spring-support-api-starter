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

-- 菜单表 (H2 兼容)
CREATE TABLE IF NOT EXISTS `sys_menu` (
    `sys_menu_id` INT NOT NULL AUTO_INCREMENT,
    `sys_menu_name` VARCHAR(255) DEFAULT NULL,
    `sys_menu_path` VARCHAR(255) DEFAULT NULL,
    `sys_menu_i18n` VARCHAR(255) DEFAULT NULL,
    `sys_menu_title` VARCHAR(255) DEFAULT NULL,
    `sys_menu_perm` VARCHAR(255) DEFAULT NULL,
    `sys_menu_pid` INT DEFAULT NULL,
    `sys_menu_tree_id` VARCHAR(255) DEFAULT NULL,
    `sys_menu_component` VARCHAR(255) DEFAULT NULL,
    `sys_menu_icon` VARCHAR(255) DEFAULT NULL,
    `sys_menu_extra_icon` VARCHAR(255) DEFAULT NULL,
    `sys_menu_type` INT DEFAULT NULL,
    `sys_menu_sort` INT DEFAULT NULL,
    `sys_menu_hidden` INT DEFAULT NULL,
    `sys_menu_show_parent` INT DEFAULT NULL,
    `sys_menu_hidden_tag` INT DEFAULT NULL,
    `sys_menu_fixed_tag` INT DEFAULT NULL,
    `sys_menu_redirect` VARCHAR(255) DEFAULT NULL,
    `sys_menu_enter_transition` VARCHAR(255) DEFAULT NULL,
    `sys_menu_leave_transition` VARCHAR(255) DEFAULT NULL,
    `sys_menu_active_path` VARCHAR(255) DEFAULT NULL,
    `sys_menu_frame_src` VARCHAR(255) DEFAULT NULL,
    `sys_menu_keep_alive` INT DEFAULT NULL,
    `sys_menu_role` VARCHAR(255) DEFAULT NULL,
    `sys_menu_home` INT DEFAULT NULL,
    `sys_menu_engine` INT DEFAULT NULL,
    `create_name` VARCHAR(255) DEFAULT NULL,
    `create_by` INT DEFAULT NULL,
    `create_time` DATETIME DEFAULT NULL,
    `update_time` DATETIME DEFAULT NULL,
    `update_name` VARCHAR(255) DEFAULT NULL,
    `update_by` INT DEFAULT NULL,
    PRIMARY KEY (`sys_menu_id`)
);

-- 监控服务器表 (H2 兼容)
CREATE TABLE IF NOT EXISTS `monitor_sys_gen_server` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `server_name` VARCHAR(255) DEFAULT NULL,
    `server_host` VARCHAR(255) DEFAULT NULL,
    `server_port` INT DEFAULT NULL,
    `server_status` INT DEFAULT 0,
    `create_time` DATETIME DEFAULT NULL,
    `update_time` DATETIME DEFAULT NULL,
    PRIMARY KEY (`id`)
);

-- 系统服务器表 (H2 兼容)
CREATE TABLE IF NOT EXISTS `system_server` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `server_name` VARCHAR(255) DEFAULT NULL,
    `server_host` VARCHAR(255) DEFAULT NULL,
    `server_port` INT DEFAULT NULL,
    `server_status` INT DEFAULT 0,
    `create_time` DATETIME DEFAULT NULL,
    `update_time` DATETIME DEFAULT NULL,
    PRIMARY KEY (`id`)
);

-- 文件系统表 (H2 兼容)
CREATE TABLE IF NOT EXISTS `file_system` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `file_name` VARCHAR(255) DEFAULT NULL,
    `file_path` VARCHAR(1024) DEFAULT NULL,
    `file_size` BIGINT DEFAULT 0,
    `file_type` VARCHAR(64) DEFAULT NULL,
    `create_time` DATETIME DEFAULT NULL,
    `update_time` DATETIME DEFAULT NULL,
    PRIMARY KEY (`id`)
);

-- 文件系统设置表 (H2 兼容)
CREATE TABLE IF NOT EXISTS `file_system_setting` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `setting_key` VARCHAR(255) DEFAULT NULL,
    `setting_value` VARCHAR(1024) DEFAULT NULL,
    `create_time` DATETIME DEFAULT NULL,
    `update_time` DATETIME DEFAULT NULL,
    PRIMARY KEY (`id`)
);

-- server_host 表 (H2 兼容)
CREATE TABLE IF NOT EXISTS `server_host` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `host_name` VARCHAR(255) DEFAULT NULL,
    `host_ip` VARCHAR(64) DEFAULT NULL,
    `host_port` INT DEFAULT NULL,
    `host_status` INT DEFAULT 0,
    `create_time` DATETIME DEFAULT NULL,
    `update_time` DATETIME DEFAULT NULL,
    PRIMARY KEY (`id`)
);

-- ============================================================
-- AI 知识图谱实体表
-- ============================================================
CREATE TABLE IF NOT EXISTS `sys_ai_knowledge_graph_entity` (
  `sys_ai_knowledge_graph_entity_id`       INT           NOT NULL AUTO_INCREMENT,
  `sys_ai_knowledge_graph_entity_uid`      VARCHAR(64)   NOT NULL,
  `sys_ai_knowledge_graph_library_id`      INT           NOT NULL DEFAULT 1,
  `sys_ai_knowledge_graph_entity_name`     VARCHAR(255)  NOT NULL,
  `sys_ai_knowledge_graph_entity_type`     VARCHAR(128)  NOT NULL DEFAULT 'default',
  `sys_ai_knowledge_graph_entity_properties` VARCHAR(1024) DEFAULT NULL,
  `sys_ai_knowledge_graph_entity_source`   VARCHAR(32)   NOT NULL DEFAULT 'manual',
  `sys_ai_knowledge_graph_entity_weight`   DOUBLE        DEFAULT 1.0,
  `sys_ai_knowledge_graph_entity_logo`     VARCHAR(1024) DEFAULT NULL,
  `sys_ai_knowledge_graph_entity_status`   INT           NOT NULL DEFAULT 1,
  `create_name`  VARCHAR(255) DEFAULT NULL,
  `create_by`    INT          DEFAULT NULL,
  `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_name`  VARCHAR(255) DEFAULT NULL,
  `update_by`    INT          DEFAULT NULL,
  PRIMARY KEY (`sys_ai_knowledge_graph_entity_id`),
  UNIQUE KEY `uk_kg_entity_uid` (`sys_ai_knowledge_graph_entity_uid`)
);

-- ============================================================
-- AI 知识图谱关系表
-- ============================================================
CREATE TABLE IF NOT EXISTS `sys_ai_knowledge_graph_relation` (
  `sys_ai_knowledge_graph_relation_id`                 INT           NOT NULL AUTO_INCREMENT,
  `sys_ai_knowledge_graph_relation_uid`                VARCHAR(64)   NOT NULL,
  `sys_ai_knowledge_graph_relation_library_id`         INT           NOT NULL DEFAULT 1,
  `sys_ai_knowledge_graph_relation_source_entity_uid`  VARCHAR(64)   NOT NULL,
  `sys_ai_knowledge_graph_relation_target_entity_uid`  VARCHAR(64)   NOT NULL,
  `sys_ai_knowledge_graph_relation_type`               VARCHAR(128)  NOT NULL,
  `sys_ai_knowledge_graph_relation_properties`         VARCHAR(1024) DEFAULT NULL,
  `sys_ai_knowledge_graph_relation_weight`             DOUBLE        DEFAULT 1.0,
  `sys_ai_knowledge_graph_relation_source`             VARCHAR(32)   NOT NULL DEFAULT 'manual',
  `sys_ai_knowledge_graph_relation_status`             INT           NOT NULL DEFAULT 1,
  `create_name`  VARCHAR(255) DEFAULT NULL,
  `create_by`    INT          DEFAULT NULL,
  `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_name`  VARCHAR(255) DEFAULT NULL,
  `update_by`    INT          DEFAULT NULL,
  PRIMARY KEY (`sys_ai_knowledge_graph_relation_id`),
  UNIQUE KEY `uk_kg_relation_uid` (`sys_ai_knowledge_graph_relation_uid`)
);

-- ============================================================
-- AI 知识库表
-- ============================================================
CREATE TABLE IF NOT EXISTS `sys_ai_knowledge_library` (
  `sys_ai_knowledge_library_id`                      INT           NOT NULL AUTO_INCREMENT,
  `sys_ai_knowledge_library_name`                    VARCHAR(128)  NOT NULL,
  `sys_ai_knowledge_library_description`             VARCHAR(512)  DEFAULT NULL,
  `sys_ai_knowledge_library_summary`                 TEXT          DEFAULT NULL,
  `sys_ai_knowledge_library_llm_provider`            VARCHAR(64)   DEFAULT NULL,
  `sys_ai_knowledge_library_llm_model`               VARCHAR(128)  DEFAULT NULL,
  `sys_ai_knowledge_library_temperature`             DOUBLE        DEFAULT NULL,
  `sys_ai_knowledge_library_max_tokens`              INT           DEFAULT NULL,
  `sys_ai_knowledge_library_prompt_template`         TEXT          DEFAULT NULL,
  `sys_ai_knowledge_library_relation_extraction_enabled` INT       DEFAULT 1,
  `sys_ai_knowledge_library_max_entities_per_doc`    INT           DEFAULT 50,
  `sys_ai_knowledge_library_web_search_enabled`     INT           DEFAULT 0,
  `sys_ai_knowledge_library_sort`                    INT           DEFAULT 0,
  `sys_ai_knowledge_library_status`                  INT           NOT NULL DEFAULT 1,
  `create_name`  VARCHAR(255) DEFAULT NULL,
  `create_by`    INT          DEFAULT NULL,
  `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_name`  VARCHAR(255) DEFAULT NULL,
  `update_by`    INT          DEFAULT NULL,
  PRIMARY KEY (`sys_ai_knowledge_library_id`),
  UNIQUE KEY `uk_lib_name` (`sys_ai_knowledge_library_name`)
);

-- ============================================================
-- AI 知识图谱实体属性表
-- ============================================================
CREATE TABLE IF NOT EXISTS `sys_ai_knowledge_graph_entity_attribute` (
  `attr_id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `entity_uid`       VARCHAR(64)  NOT NULL,
  `library_id`       INT          NOT NULL DEFAULT 1,
  `attr_name`        VARCHAR(200) NOT NULL,
  `attr_value`       TEXT         DEFAULT NULL,
  `attr_type`        VARCHAR(32)  NOT NULL DEFAULT 'text',
  `attr_sort`        INT          NOT NULL DEFAULT 0,
  `status`           INT          NOT NULL DEFAULT 1,
  `create_name`      VARCHAR(255) DEFAULT NULL,
  `create_by`        INT          DEFAULT NULL,
  `create_time`      DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`      DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_name`      VARCHAR(255) DEFAULT NULL,
  `update_by`        INT          DEFAULT NULL,
  PRIMARY KEY (`attr_id`)
);

-- ============================================================
-- 种子数据：全局 AI 设置
-- 例外说明：本文件属 H2 本地联调目录（db/init/h2/），该目录不走
-- initdata 异步执行器（initdata_* 扫描仅针对 db/init/ 根目录），
-- 故种子内嵌于此；每条 INSERT 均带 WHERE NOT EXISTS 幂等防护，重复执行安全
-- ============================================================
INSERT INTO sys_ai_global_setting (sys_ai_global_setting_group, sys_ai_global_setting_field, sys_ai_global_setting_value, sys_ai_global_setting_value_type, sys_ai_global_setting_description, sys_ai_global_setting_sort, sys_ai_global_setting_status)
SELECT 'runtime', 'provider', 'zhipu', 'STRING', 'LLM提供商', 1, 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_ai_global_setting WHERE sys_ai_global_setting_group='runtime' AND sys_ai_global_setting_field='provider');

INSERT INTO sys_ai_global_setting (sys_ai_global_setting_group, sys_ai_global_setting_field, sys_ai_global_setting_value, sys_ai_global_setting_value_type, sys_ai_global_setting_description, sys_ai_global_setting_sort, sys_ai_global_setting_status)
SELECT 'runtime', 'factory', 'default', 'STRING', 'ChatClient工厂', 2, 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_ai_global_setting WHERE sys_ai_global_setting_group='runtime' AND sys_ai_global_setting_field='factory');

INSERT INTO sys_ai_global_setting (sys_ai_global_setting_group, sys_ai_global_setting_field, sys_ai_global_setting_value, sys_ai_global_setting_value_type, sys_ai_global_setting_description, sys_ai_global_setting_sort, sys_ai_global_setting_status)
SELECT 'runtime', 'provider_base_url', 'https://open.bigmodel.cn/api/paas/v4/', 'STRING', 'API基础URL', 3, 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_ai_global_setting WHERE sys_ai_global_setting_group='runtime' AND sys_ai_global_setting_field='provider_base_url');

INSERT INTO sys_ai_global_setting (sys_ai_global_setting_group, sys_ai_global_setting_field, sys_ai_global_setting_value, sys_ai_global_setting_value_type, sys_ai_global_setting_description, sys_ai_global_setting_sort, sys_ai_global_setting_status)
SELECT 'runtime', 'provider_api_key', '${GLM_API_KEY:your-api-key-here}', 'STRING', 'API密钥', 4, 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_ai_global_setting WHERE sys_ai_global_setting_group='runtime' AND sys_ai_global_setting_field='provider_api_key');

INSERT INTO sys_ai_global_setting (sys_ai_global_setting_group, sys_ai_global_setting_field, sys_ai_global_setting_value, sys_ai_global_setting_value_type, sys_ai_global_setting_description, sys_ai_global_setting_sort, sys_ai_global_setting_status)
SELECT 'runtime', 'default_model', 'glm-4-flash', 'STRING', '默认模型', 5, 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_ai_global_setting WHERE sys_ai_global_setting_group='runtime' AND sys_ai_global_setting_field='default_model');
