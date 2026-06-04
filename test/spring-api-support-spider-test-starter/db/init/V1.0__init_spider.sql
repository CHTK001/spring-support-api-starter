-- =============================================
-- Spider 模块完整初始化（合并版）
-- 包含：全部建表、种子数据、空数据清理
-- 合并自：V1.0~V1.8 所有 SQL 文件
-- =============================================

-- ==================== 基础架构表 ====================

CREATE TABLE IF NOT EXISTS `spider_definition`
(
    `spider_id`                       BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `spider_code`                     VARCHAR(128) NOT NULL COMMENT '爬虫编码',
    `spider_name`                     VARCHAR(255) NOT NULL COMMENT '爬虫名称',
    `spider_entry_url`                VARCHAR(1024) NOT NULL COMMENT '主入口 URL',
    `spider_site_host`                VARCHAR(255) DEFAULT NULL COMMENT '主体网站',
    `spider_executor_type`            VARCHAR(32) NOT NULL COMMENT '执行器类型：ONCE/N_TIMES/CRON',
    `spider_executor_rounds`          INT DEFAULT NULL COMMENT 'N 次执行时配置的轮次数',
    `spider_executor_job_enabled`     TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否启用定时任务',
    `spider_executor_job_config`      TEXT DEFAULT NULL COMMENT '定时任务配置 JSON',
    `spider_executor_config`          TEXT DEFAULT NULL COMMENT '执行器配置 JSON',
    `spider_collect_deep_url`         TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否深度采集地址',
    `spider_notify_on_failure`        TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否开启失败通知',
    `spider_dedup_strategy`           VARCHAR(32) NOT NULL DEFAULT 'URL_FINGERPRINT' COMMENT '去重策略：URL_FINGERPRINT/NORMALIZED_URL/CONTENT_FINGERPRINT/CUSTOM',
    `spider_url_storage_type`         VARCHAR(32) NOT NULL DEFAULT 'DATABASE' COMMENT 'URL 存储类型：DATABASE/REDIS/SPI',
    `spider_url_storage_spi`          VARCHAR(128) DEFAULT NULL COMMENT 'utils-common SPI 名称',
    `spider_url_storage_config`       LONGTEXT DEFAULT NULL COMMENT 'URL 存储配置 JSON',
    `spider_current_execution_no`     VARCHAR(128) DEFAULT NULL COMMENT '当前执行批次号',
    `spider_current_node_code`        VARCHAR(128) DEFAULT NULL COMMENT '当前运行节点编码',
    `spider_status`                   TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
    `spider_remark`                   VARCHAR(1000) DEFAULT NULL COMMENT '备注',
    `spider_create_name`              VARCHAR(255) DEFAULT NULL COMMENT '创建人姓名',
    `spider_create_by`                INT DEFAULT NULL COMMENT '创建人',
    `spider_create_time`              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `spider_update_name`              VARCHAR(255) DEFAULT NULL COMMENT '更新人姓名',
    `spider_update_by`                INT DEFAULT NULL COMMENT '更新人',
    `spider_update_time`              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_name`                     VARCHAR(255) DEFAULT NULL COMMENT '创建人姓名',
    `create_by`                       INT DEFAULT NULL COMMENT '创建人',
    `create_time`                     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_name`                     VARCHAR(255) DEFAULT NULL COMMENT '更新人姓名',
    `update_by`                       INT DEFAULT NULL COMMENT '更新人',
    `update_time`                     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_spider_definition_code` (`spider_code`)
) COMMENT ='爬虫定义';

CREATE TABLE IF NOT EXISTS `spider_flow`
(
    `spider_id`                       BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `spider_definition_code`          VARCHAR(128) NOT NULL COMMENT '爬虫编码',
    `spider_flow_version`             INT NOT NULL DEFAULT 1 COMMENT '流程版本',
    `spider_flow_name`                VARCHAR(255) NOT NULL COMMENT '流程名称',
    `spider_flow_scope`               VARCHAR(32) NOT NULL DEFAULT 'PRIVATE' COMMENT '流程作用域：PRIVATE',
    `spider_flow_parent_node_code`    VARCHAR(128) DEFAULT NULL COMMENT '来源父节点编码',
    `spider_flow_schema`              LONGTEXT NOT NULL COMMENT 'ScReteEditor 编排 JSON',
    `spider_flow_published`           TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已发布',
    `spider_create_name`              VARCHAR(255) DEFAULT NULL COMMENT '创建人姓名',
    `spider_create_by`                INT DEFAULT NULL COMMENT '创建人',
    `spider_create_time`              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `spider_update_name`              VARCHAR(255) DEFAULT NULL COMMENT '更新人姓名',
    `spider_update_by`                INT DEFAULT NULL COMMENT '更新人',
    `spider_update_time`              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_name`                     VARCHAR(255) DEFAULT NULL COMMENT '创建人姓名',
    `create_by`                       INT DEFAULT NULL COMMENT '创建人',
    `create_time`                     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_name`                     VARCHAR(255) DEFAULT NULL COMMENT '更新人姓名',
    `update_by`                       INT DEFAULT NULL COMMENT '更新人',
    `update_time`                     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY `idx_spider_flow_definition_code` (`spider_definition_code`)
) COMMENT ='爬虫编排定义';

CREATE TABLE IF NOT EXISTS `spider_flow_node`
(
    `spider_id`                       BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `spider_definition_code`          VARCHAR(128) NOT NULL COMMENT '爬虫编码',
    `spider_flow_version`             INT NOT NULL COMMENT '流程版本',
    `spider_node_code`                VARCHAR(128) NOT NULL COMMENT '节点编码',
    `spider_node_type`                VARCHAR(64) NOT NULL COMMENT '节点类型：START/PAGE_ACCESS/URL_PARSE/DATA_PARSE/DOWNLOADER/SUB_FLOW/HUMAN_INTERVENE/CONDITION/SAVE_RESULT/ASYNC_SAVE/END',
    `spider_node_name`                VARCHAR(255) NOT NULL COMMENT '节点名称',
    `spider_node_config`              LONGTEXT NOT NULL COMMENT '节点配置 JSON',
    `spider_notify_config`            LONGTEXT DEFAULT NULL COMMENT '通知配置 JSON',
    `spider_sort_num`                 INT NOT NULL DEFAULT 0 COMMENT '排序',
    `spider_create_name`              VARCHAR(255) DEFAULT NULL COMMENT '创建人姓名',
    `spider_create_by`                INT DEFAULT NULL COMMENT '创建人',
    `spider_create_time`              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `spider_update_name`              VARCHAR(255) DEFAULT NULL COMMENT '更新人姓名',
    `spider_update_by`                INT DEFAULT NULL COMMENT '更新人',
    `spider_update_time`              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_name`                     VARCHAR(255) DEFAULT NULL COMMENT '创建人姓名',
    `create_by`                       INT DEFAULT NULL COMMENT '创建人',
    `create_time`                     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_name`                     VARCHAR(255) DEFAULT NULL COMMENT '更新人姓名',
    `update_by`                       INT DEFAULT NULL COMMENT '更新人',
    `update_time`                     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_spider_flow_node` (`spider_definition_code`, `spider_flow_version`, `spider_node_code`)
) COMMENT ='爬虫流程节点';

CREATE TABLE IF NOT EXISTS `spider_url_queue`
(
    `spider_id`                       BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `spider_definition_code`          VARCHAR(128) NOT NULL COMMENT '爬虫编码',
    `spider_source_url`               VARCHAR(1024) NOT NULL COMMENT '来源 URL',
    `spider_target_url`               VARCHAR(1024) NOT NULL COMMENT '待解析 URL',
    `spider_normalized_url`           VARCHAR(1024) DEFAULT NULL COMMENT '规范化 URL',
    `spider_url_fingerprint`          VARCHAR(128) NOT NULL COMMENT 'URL 指纹',
    `spider_content_fingerprint`      VARCHAR(128) DEFAULT NULL COMMENT '内容指纹',
    `spider_dedup_key`                VARCHAR(255) DEFAULT NULL COMMENT '最终去重键',
    `spider_storage_type`             VARCHAR(32) NOT NULL DEFAULT 'DATABASE' COMMENT '当前落点：DATABASE/REDIS/SPI',
    `spider_storage_ref`              VARCHAR(255) DEFAULT NULL COMMENT '存储引用或 key',
    `spider_parse_status`             VARCHAR(32) NOT NULL DEFAULT 'WAITING' COMMENT 'WAITING/RUNNING/SUCCESS/FAILED/SKIPPED',
    `spider_retry_count`              INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    `spider_last_error_message`       VARCHAR(2000) DEFAULT NULL COMMENT '最后错误信息',
    `spider_create_name`              VARCHAR(255) DEFAULT NULL COMMENT '创建人姓名',
    `spider_create_by`                INT DEFAULT NULL COMMENT '创建人',
    `spider_create_time`              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `spider_update_name`              VARCHAR(255) DEFAULT NULL COMMENT '更新人姓名',
    `spider_update_by`                INT DEFAULT NULL COMMENT '更新人',
    `spider_update_time`              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_name`                     VARCHAR(255) DEFAULT NULL COMMENT '创建人姓名',
    `create_by`                       INT DEFAULT NULL COMMENT '创建人',
    `create_time`                     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_name`                     VARCHAR(255) DEFAULT NULL COMMENT '更新人姓名',
    `update_by`                       INT DEFAULT NULL COMMENT '更新人',
    `update_time`                     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_spider_url_queue_dedup_key` (`spider_definition_code`, `spider_dedup_key`),
    KEY `idx_spider_url_queue_status` (`spider_definition_code`, `spider_parse_status`)
) COMMENT ='地址队列';

CREATE TABLE IF NOT EXISTS `spider_parse_result`
(
    `spider_id`                       BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `spider_definition_code`          VARCHAR(128) NOT NULL COMMENT '爬虫编码',
    `spider_execution_no`             VARCHAR(128) NOT NULL COMMENT '执行批次号',
    `spider_target_url`               VARCHAR(1024) NOT NULL COMMENT '解析 URL',
    `spider_result_schema_code`       VARCHAR(128) DEFAULT NULL COMMENT '结果结构编码',
    `spider_result_payload`           LONGTEXT NOT NULL COMMENT '统一中间结果 JSON',
    `spider_result_status`            VARCHAR(32) NOT NULL DEFAULT 'SUCCESS' COMMENT '结果状态',
    `spider_result_sync_status`       VARCHAR(32) NOT NULL DEFAULT 'WAITING' COMMENT '同步状态：WAITING/SYNCING/SUCCESS/FAILED',
    `spider_result_sync_error`        VARCHAR(2000) DEFAULT NULL COMMENT '同步错误信息',
    `spider_create_name`              VARCHAR(255) DEFAULT NULL COMMENT '创建人姓名',
    `spider_create_by`                INT DEFAULT NULL COMMENT '创建人',
    `spider_create_time`              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `spider_update_name`              VARCHAR(255) DEFAULT NULL COMMENT '更新人姓名',
    `spider_update_by`                INT DEFAULT NULL COMMENT '更新人',
    `spider_update_time`              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_name`                     VARCHAR(255) DEFAULT NULL COMMENT '创建人姓名',
    `create_by`                       INT DEFAULT NULL COMMENT '创建人',
    `create_time`                     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_name`                     VARCHAR(255) DEFAULT NULL COMMENT '更新人姓名',
    `update_by`                       INT DEFAULT NULL COMMENT '更新人',
    `update_time`                     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY `idx_spider_parse_result_exec` (`spider_definition_code`, `spider_execution_no`)
) COMMENT ='统一中间解析结果';

CREATE TABLE IF NOT EXISTS `spider_execution`
(
    `spider_id`                       BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `spider_execution_no`             VARCHAR(128) NOT NULL COMMENT '执行批次号',
    `spider_definition_code`          VARCHAR(128) NOT NULL COMMENT '爬虫编码',
    `spider_trigger_type`             VARCHAR(32) NOT NULL COMMENT '触发类型：MANUAL/JOB/PREVIEW/SUB_FLOW',
    `spider_execution_status`         VARCHAR(32) NOT NULL DEFAULT 'WAITING' COMMENT '执行状态',
    `spider_current_node_code`        VARCHAR(128) DEFAULT NULL COMMENT '当前节点',
    `spider_need_manual_intervene`    TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否需要人工介入',
    `spider_continue_on_failure`      TINYINT(1) NOT NULL DEFAULT 0 COMMENT '失败后是否继续执行后续节点',
    `spider_trace_snapshot`           LONGTEXT DEFAULT NULL COMMENT '链路快照 JSON',
    `spider_started_at`               DATETIME DEFAULT NULL COMMENT '开始时间',
    `spider_finished_at`              DATETIME DEFAULT NULL COMMENT '结束时间',
    `spider_create_name`              VARCHAR(255) DEFAULT NULL COMMENT '创建人姓名',
    `spider_create_by`                INT DEFAULT NULL COMMENT '创建人',
    `spider_create_time`              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `spider_update_name`              VARCHAR(255) DEFAULT NULL COMMENT '更新人姓名',
    `spider_update_by`                INT DEFAULT NULL COMMENT '更新人',
    `spider_update_time`              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_name`                     VARCHAR(255) DEFAULT NULL COMMENT '创建人姓名',
    `create_by`                       INT DEFAULT NULL COMMENT '创建人',
    `create_time`                     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_name`                     VARCHAR(255) DEFAULT NULL COMMENT '更新人姓名',
    `update_by`                       INT DEFAULT NULL COMMENT '更新人',
    `update_time`                     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_spider_execution_no` (`spider_execution_no`),
    KEY `idx_spider_execution_status` (`spider_definition_code`, `spider_execution_status`)
) COMMENT ='执行记录';

CREATE TABLE IF NOT EXISTS `spider_execution_step`
(
    `spider_id`                       BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `spider_execution_no`             VARCHAR(128) NOT NULL COMMENT '执行批次号',
    `spider_node_code`                VARCHAR(128) NOT NULL COMMENT '节点编码',
    `spider_node_name`                VARCHAR(255) NOT NULL COMMENT '节点名称',
    `spider_node_type`                VARCHAR(64) NOT NULL COMMENT '节点类型',
    `spider_step_status`              VARCHAR(32) NOT NULL COMMENT '步骤状态',
    `spider_need_manual_intervene`    TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否人工介入',
    `spider_is_derived_node`          TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否运行时派生节点',
    `spider_source_node_code`         VARCHAR(128) DEFAULT NULL COMMENT '来源编排节点编码',
    `spider_input_payload`            LONGTEXT DEFAULT NULL COMMENT '入参',
    `spider_output_payload`           LONGTEXT DEFAULT NULL COMMENT '出参',
    `spider_error_message`            VARCHAR(2000) DEFAULT NULL COMMENT '错误信息',
    `spider_started_at`               DATETIME DEFAULT NULL COMMENT '开始时间',
    `spider_finished_at`              DATETIME DEFAULT NULL COMMENT '结束时间',
    `spider_create_name`              VARCHAR(255) DEFAULT NULL COMMENT '创建人姓名',
    `spider_create_by`                INT DEFAULT NULL COMMENT '创建人',
    `spider_create_time`              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `spider_update_name`              VARCHAR(255) DEFAULT NULL COMMENT '更新人姓名',
    `spider_update_by`                INT DEFAULT NULL COMMENT '更新人',
    `spider_update_time`              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_name`                     VARCHAR(255) DEFAULT NULL COMMENT '创建人姓名',
    `create_by`                       INT DEFAULT NULL COMMENT '创建人',
    `create_time`                     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_name`                     VARCHAR(255) DEFAULT NULL COMMENT '更新人姓名',
    `update_by`                       INT DEFAULT NULL COMMENT '更新人',
    `update_time`                     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY `idx_spider_execution_step_exec` (`spider_execution_no`, `spider_node_code`)
) COMMENT ='执行步骤明细';

CREATE TABLE IF NOT EXISTS `spider_manual_task`
(
    `spider_id`                       BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `spider_execution_no`             VARCHAR(128) NOT NULL COMMENT '执行批次号',
    `spider_node_code`                VARCHAR(128) NOT NULL COMMENT '节点编码',
    `spider_task_type`                VARCHAR(64) NOT NULL COMMENT '任务类型：SMS_CODE/CAPTCHA/SCAN_CONFIRM/MANUAL_CHOOSE',
    `spider_task_status`              VARCHAR(32) NOT NULL DEFAULT 'WAITING' COMMENT '任务状态：WAITING/PROCESSING/FINISHED/CANCELLED/EXPIRED',
    `spider_task_payload`             LONGTEXT DEFAULT NULL COMMENT '任务上下文 JSON',
    `spider_operator_name`            VARCHAR(255) DEFAULT NULL COMMENT '处理人',
    `spider_operator_result`          LONGTEXT DEFAULT NULL COMMENT '人工输入结果',
    `spider_derived_step_code`        VARCHAR(128) DEFAULT NULL COMMENT '对应派生步骤编码',
    `spider_expired_at`               DATETIME DEFAULT NULL COMMENT '过期时间',
    `spider_create_name`              VARCHAR(255) DEFAULT NULL COMMENT '创建人姓名',
    `spider_create_by`                INT DEFAULT NULL COMMENT '创建人',
    `spider_create_time`              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `spider_update_name`              VARCHAR(255) DEFAULT NULL COMMENT '更新人姓名',
    `spider_update_by`                INT DEFAULT NULL COMMENT '更新人',
    `spider_update_time`              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_name`                     VARCHAR(255) DEFAULT NULL COMMENT '创建人姓名',
    `create_by`                       INT DEFAULT NULL COMMENT '创建人',
    `create_time`                     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_name`                     VARCHAR(255) DEFAULT NULL COMMENT '更新人姓名',
    `update_by`                       INT DEFAULT NULL COMMENT '更新人',
    `update_time`                     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY `idx_spider_manual_task_exec` (`spider_execution_no`, `spider_task_status`)
) COMMENT ='人工介入任务';

CREATE TABLE IF NOT EXISTS `spider_ai_template`
(
    `spider_id`                       BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `spider_template_code`            VARCHAR(128) NOT NULL COMMENT '模板编码',
    `spider_template_name`            VARCHAR(255) NOT NULL COMMENT '模板名称',
    `spider_template_scene`           VARCHAR(64) NOT NULL COMMENT '模板场景：URL_PARSE/DATA_PARSE/LOGIN_ASSIST',
    `spider_prompt_template`          LONGTEXT NOT NULL COMMENT '固定提示词模板',
    `spider_output_schema`            LONGTEXT NOT NULL COMMENT '固定输出结构 JSON Schema',
    `spider_enabled`                  TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    `spider_create_name`              VARCHAR(255) DEFAULT NULL COMMENT '创建人姓名',
    `spider_create_by`                INT DEFAULT NULL COMMENT '创建人',
    `spider_create_time`              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `spider_update_name`              VARCHAR(255) DEFAULT NULL COMMENT '更新人姓名',
    `spider_update_by`                INT DEFAULT NULL COMMENT '更新人',
    `spider_update_time`              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_name`                     VARCHAR(255) DEFAULT NULL COMMENT '创建人姓名',
    `create_by`                       INT DEFAULT NULL COMMENT '创建人',
    `create_time`                     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_name`                     VARCHAR(255) DEFAULT NULL COMMENT '更新人姓名',
    `update_by`                       INT DEFAULT NULL COMMENT '更新人',
    `update_time`                     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_spider_ai_template_code` (`spider_template_code`)
) COMMENT ='AI 固定模板';

CREATE TABLE IF NOT EXISTS `spider_pool`
(
    `spider_id`                BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `spider_pool_code`         VARCHAR(128) NOT NULL COMMENT '池编码',
    `spider_pool_name`         VARCHAR(255) NOT NULL COMMENT '池名称',
    `spider_pool_type`         VARCHAR(32) NOT NULL COMMENT '池类型：ACCOUNT/DATA/DICTIONARY',
    `spider_pool_status`       TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
    `spider_pool_remark`       VARCHAR(1000) DEFAULT NULL COMMENT '备注',
    `create_name`              VARCHAR(255) DEFAULT NULL COMMENT '创建人姓名',
    `create_by`                INT DEFAULT NULL COMMENT '创建人',
    `create_time`              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_name`              VARCHAR(255) DEFAULT NULL COMMENT '更新人姓名',
    `update_by`                INT DEFAULT NULL COMMENT '更新人',
    `update_time`              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_spider_pool_code` (`spider_pool_code`)
) COMMENT ='爬虫池定义';

CREATE TABLE IF NOT EXISTS `spider_pool_item`
(
    `spider_id`                BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `spider_pool_code`         VARCHAR(128) NOT NULL COMMENT '所属池编码',
    `spider_item_code`         VARCHAR(128) NOT NULL COMMENT '数据项编码',
    `spider_item_name`         VARCHAR(255) NOT NULL COMMENT '数据项名称',
    `spider_item_type`         VARCHAR(32) DEFAULT NULL COMMENT '数据项类型',
    `spider_item_payload`      LONGTEXT NOT NULL COMMENT '数据项 JSON',
    `spider_sort_num`          INT NOT NULL DEFAULT 0 COMMENT '排序',
    `spider_item_status`       TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
    `create_name`              VARCHAR(255) DEFAULT NULL COMMENT '创建人姓名',
    `create_by`                INT DEFAULT NULL COMMENT '创建人',
    `create_time`              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_name`              VARCHAR(255) DEFAULT NULL COMMENT '更新人姓名',
    `update_by`                INT DEFAULT NULL COMMENT '更新人',
    `update_time`              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_spider_pool_item` (`spider_pool_code`, `spider_item_code`),
    KEY `idx_spider_pool_item_pool_code` (`spider_pool_code`)
) COMMENT ='爬虫池数据项';

CREATE TABLE IF NOT EXISTS `spider_flow_edge`
(
    `spider_id`                       BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `spider_definition_code`          VARCHAR(128) NOT NULL COMMENT '爬虫编码',
    `spider_flow_version`             INT NOT NULL DEFAULT 1 COMMENT '流程版本',
    `spider_edge_id`                  VARCHAR(128) NOT NULL COMMENT '边ID',
    `spider_source_node_code`         VARCHAR(128) NOT NULL COMMENT '源节点编码',
    `spider_target_node_code`         VARCHAR(128) NOT NULL COMMENT '目标节点编码',
    `spider_edge_label`               VARCHAR(255) DEFAULT NULL COMMENT '边标签',
    `spider_source_output`            VARCHAR(64) DEFAULT NULL COMMENT '源输出端口',
    `spider_target_input`             VARCHAR(64) DEFAULT NULL COMMENT '目标输入端口',
    `spider_sort_num`                 INT NOT NULL DEFAULT 0 COMMENT '排序',
    `spider_create_name`              VARCHAR(255) DEFAULT NULL COMMENT '创建人姓名',
    `spider_create_by`                INT DEFAULT NULL COMMENT '创建人',
    `spider_create_time`              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `spider_update_name`              VARCHAR(255) DEFAULT NULL COMMENT '更新人姓名',
    `spider_update_by`                INT DEFAULT NULL COMMENT '更新人',
    `spider_update_time`              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_name`                     VARCHAR(255) DEFAULT NULL COMMENT '创建人姓名',
    `create_by`                       INT DEFAULT NULL COMMENT '创建人',
    `create_time`                     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_name`                     VARCHAR(255) DEFAULT NULL COMMENT '更新人姓名',
    `update_by`                       INT DEFAULT NULL COMMENT '更新人',
    `update_time`                     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY `idx_spider_flow_edge_def` (`spider_definition_code`, `spider_flow_version`)
) COMMENT ='爬虫流程边';

CREATE TABLE IF NOT EXISTS `spider_execution_edge`
(
    `spider_id`                       BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `spider_execution_no`             VARCHAR(128) NOT NULL COMMENT '执行批次号',
    `spider_edge_id`                  VARCHAR(128) NOT NULL COMMENT '边ID',
    `spider_source_node_code`         VARCHAR(128) NOT NULL COMMENT '源节点编码',
    `spider_target_node_code`         VARCHAR(128) NOT NULL COMMENT '目标节点编码',
    `spider_edge_label`               VARCHAR(255) DEFAULT NULL COMMENT '边标签',
    `spider_source_output`            VARCHAR(64) DEFAULT NULL COMMENT '源输出端口',
    `spider_target_input`             VARCHAR(64) DEFAULT NULL COMMENT '目标输入端口',
    `spider_runtime_status`           VARCHAR(32) DEFAULT 'WAITING' COMMENT '运行时状态',
    `spider_visited`                  TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已访问',
    `spider_create_name`              VARCHAR(255) DEFAULT NULL COMMENT '创建人姓名',
    `spider_create_by`                INT DEFAULT NULL COMMENT '创建人',
    `spider_create_time`              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `spider_update_name`              VARCHAR(255) DEFAULT NULL COMMENT '更新人姓名',
    `spider_update_by`                INT DEFAULT NULL COMMENT '更新人',
    `spider_update_time`              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_name`                     VARCHAR(255) DEFAULT NULL COMMENT '创建人姓名',
    `create_by`                       INT DEFAULT NULL COMMENT '创建人',
    `create_time`                     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_name`                     VARCHAR(255) DEFAULT NULL COMMENT '更新人姓名',
    `update_by`                       INT DEFAULT NULL COMMENT '更新人',
    `update_time`                     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY `idx_spider_execution_edge_exec` (`spider_execution_no`)
) COMMENT ='执行过程边轨迹';

-- ==================== 数据存储表（V1.5） ====================

CREATE TABLE IF NOT EXISTS `spider_data_record`
(
    `spider_id`              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `spider_definition_code` VARCHAR(128) NOT NULL COMMENT '爬虫编码',
    `spider_execution_no`    VARCHAR(128) NOT NULL COMMENT '执行批次号',
    `spider_target_url`      VARCHAR(1024) DEFAULT NULL COMMENT '来源URL',
    `spider_data_code`       VARCHAR(128) NOT NULL COMMENT '数据类型编码',
    `spider_data_name`       VARCHAR(512) DEFAULT NULL COMMENT '数据名称/标题',
    `spider_data_payload`    LONGTEXT NOT NULL COMMENT '结构化数据JSON',
    `spider_data_status`     VARCHAR(32) NOT NULL DEFAULT 'SUCCESS' COMMENT '数据状态',

    `create_name`            VARCHAR(50) DEFAULT NULL COMMENT '创建人姓名',
    `create_by`              INT DEFAULT NULL COMMENT '创建人ID',
    `create_time`            DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_name`            VARCHAR(50) DEFAULT NULL COMMENT '更新人姓名',
    `update_by`              INT DEFAULT NULL COMMENT '更新人ID',
    `update_time`            DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    KEY `idx_sdr_exec`       (`spider_definition_code`, `spider_execution_no`),
    KEY `idx_sdr_data_code`  (`spider_data_code`)
) COMMENT ='爬虫数据存储表';

-- ==================== Gitee Explore 项目采集表（V1.6） ====================

CREATE TABLE IF NOT EXISTS `gitee_explore_project` (
    `spider_id`              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `spider_definition_code` VARCHAR(128) NOT NULL COMMENT '爬虫编码',
    `spider_execution_no`    VARCHAR(128) NOT NULL COMMENT '执行批次号',

    `name`                   VARCHAR(512) DEFAULT NULL COMMENT '项目名称',
    `owner`                  VARCHAR(255) DEFAULT NULL COMMENT '项目所有者',
    `stars`                  VARCHAR(64) DEFAULT '0' COMMENT '星标数',
    `language`               VARCHAR(128) DEFAULT NULL COMMENT '编程语言',
    `description`            TEXT DEFAULT NULL COMMENT '项目描述',
    `repo_url`               VARCHAR(1024) DEFAULT NULL COMMENT '项目地址',

    `spider_create_time`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY `idx_gkp_def_exec`   (`spider_definition_code`, `spider_execution_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT ='Gitee Explore 项目采集数据';

-- 兼容旧表 INT→VARCHAR 变更
ALTER TABLE `gitee_explore_project` MODIFY COLUMN `stars` VARCHAR(64) DEFAULT '0' COMMENT '星标数';

-- ==================== 执行截图表（V1.7 + V1.8） ====================

CREATE TABLE IF NOT EXISTS `spider_execution_screenshot` (
    `spider_id`                BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `spider_execution_no`      VARCHAR(128) NOT NULL COMMENT '执行批次号',
    `spider_node_code`         VARCHAR(64)  NOT NULL COMMENT '节点编码',
    `spider_screenshot_base64` LONGTEXT     DEFAULT NULL COMMENT '截图 Base64',
    `spider_screenshot_path`   VARCHAR(512) DEFAULT NULL COMMENT '截图文件相对路径',
    `spider_step_index`        INT          DEFAULT 0 COMMENT '步骤索引',
    `spider_event_time`        DATETIME     DEFAULT NULL COMMENT '事件时间',

    `create_name`              VARCHAR(64)  DEFAULT NULL COMMENT '创建人名称',
    `create_by`                VARCHAR(64)  DEFAULT NULL COMMENT '创建人',
    `create_time`              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_name`              VARCHAR(64)  DEFAULT NULL COMMENT '更新人名称',
    `update_by`                VARCHAR(64)  DEFAULT NULL COMMENT '更新人',
    `update_time`              DATETIME     DEFAULT NULL COMMENT '更新时间',

    KEY `idx_ss_exec_node`     (`spider_execution_no`, `spider_node_code`),
    KEY `idx_ss_exec`          (`spider_execution_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT ='Spider 执行截图';

-- ==================== 数据存储表（V1.5） ====================

CREATE TABLE IF NOT EXISTS `spider_data_record`
(
    `spider_id`              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `spider_definition_code` VARCHAR(128) NOT NULL COMMENT '爬虫编码',
    `spider_execution_no`    VARCHAR(128) NOT NULL COMMENT '执行批次号',
    `spider_target_url`      VARCHAR(1024) DEFAULT NULL COMMENT '来源URL',
    `spider_data_code`       VARCHAR(128) NOT NULL COMMENT '数据类型编码',
    `spider_data_name`       VARCHAR(512) DEFAULT NULL COMMENT '数据名称/标题',
    `spider_data_payload`    LONGTEXT NOT NULL COMMENT '结构化数据JSON',
    `spider_data_status`     VARCHAR(32) NOT NULL DEFAULT 'SUCCESS' COMMENT '数据状态',

    `create_name`            VARCHAR(50) DEFAULT NULL COMMENT '创建人姓名',
    `create_by`              INT DEFAULT NULL COMMENT '创建人ID',
    `create_time`            DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_name`            VARCHAR(50) DEFAULT NULL COMMENT '更新人姓名',
    `update_by`              INT DEFAULT NULL COMMENT '更新人ID',
    `update_time`            DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    KEY `idx_sdr_exec`       (`spider_definition_code`, `spider_execution_no`),
    KEY `idx_sdr_data_code`  (`spider_data_code`)
) COMMENT ='爬虫数据存储表';

-- ==================== Gitee Explore 项目采集表（V1.6） ====================

CREATE TABLE IF NOT EXISTS `gitee_explore_project` (
    `spider_id`              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `spider_definition_code` VARCHAR(128) NOT NULL COMMENT '爬虫编码',
    `spider_execution_no`    VARCHAR(128) NOT NULL COMMENT '执行批次号',

    `name`                   VARCHAR(512) DEFAULT NULL COMMENT '项目名称',
    `owner`                  VARCHAR(255) DEFAULT NULL COMMENT '项目所有者',
    `stars`                  VARCHAR(64) DEFAULT '0' COMMENT '星标数',
    `language`               VARCHAR(128) DEFAULT NULL COMMENT '编程语言',
    `description`            TEXT DEFAULT NULL COMMENT '项目描述',
    `repo_url`               VARCHAR(1024) DEFAULT NULL COMMENT '项目地址',

    `spider_create_time`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY `idx_gkp_def_exec`   (`spider_definition_code`, `spider_execution_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT ='Gitee Explore 项目采集数据';

-- 兼容旧表 INT→VARCHAR 变更
ALTER TABLE `gitee_explore_project` MODIFY COLUMN `stars` VARCHAR(64) DEFAULT '0' COMMENT '星标数';

-- ==================== 执行截图表（V1.7 + V1.8） ====================

CREATE TABLE IF NOT EXISTS `spider_execution_screenshot` (
    `spider_id`                BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `spider_execution_no`      VARCHAR(128) NOT NULL COMMENT '执行批次号',
    `spider_node_code`         VARCHAR(64)  NOT NULL COMMENT '节点编码',
    `spider_screenshot_base64` LONGTEXT     DEFAULT NULL COMMENT '截图 Base64',
    `spider_screenshot_path`   VARCHAR(512) DEFAULT NULL COMMENT '截图文件相对路径',
    `spider_step_index`        INT          DEFAULT 0 COMMENT '步骤索引',
    `spider_event_time`        DATETIME     DEFAULT NULL COMMENT '事件时间',

    `create_name`              VARCHAR(64)  DEFAULT NULL COMMENT '创建人名称',
    `create_by`                VARCHAR(64)  DEFAULT NULL COMMENT '创建人',
    `create_time`              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_name`              VARCHAR(64)  DEFAULT NULL COMMENT '更新人名称',
    `update_by`                VARCHAR(64)  DEFAULT NULL COMMENT '更新人',
    `update_time`              DATETIME     DEFAULT NULL COMMENT '更新时间',

    KEY `idx_ss_exec_node`     (`spider_execution_no`, `spider_node_code`),
    KEY `idx_ss_exec`          (`spider_execution_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT ='Spider 执行截图';

-- ==================== 种子数据：Gitee Explore Agent（V1.1） ====================

DELETE FROM spider_flow_edge WHERE spider_definition_code = 'gitee-explore-agent';
DELETE FROM spider_flow_node WHERE spider_definition_code = 'gitee-explore-agent';
DELETE FROM spider_flow WHERE spider_definition_code = 'gitee-explore-agent';
DELETE FROM spider_definition WHERE spider_code = 'gitee-explore-agent';

INSERT INTO spider_definition (
    spider_code, spider_name, spider_entry_url, spider_site_host,
    spider_executor_type, spider_executor_rounds, spider_executor_job_enabled,
    spider_executor_config,
    spider_collect_deep_url, spider_notify_on_failure,
    spider_dedup_strategy, spider_url_storage_type, spider_status,
    spider_remark
) VALUES (
    'gitee-explore-agent', 'Gitee Explore Agent 探索项目采集', 'https://gitee.com/explore/all', 'gitee.com',
    'ONCE', 1, 0,
    '{"orchestrationMode":"MANUAL"}',
    0, 0,
    'URL_FINGERPRINT', 'DATABASE', 1,
    '人工编排：Gitee Explore 项目采集流程（4 节点：DATA_PARSE(AI) → 旁路 SAVE_RESULT → CONDITION 500 上限循环, 翻页 AI 回环）'
);

INSERT INTO spider_flow (spider_definition_code, spider_flow_version, spider_flow_name, spider_flow_scope, spider_flow_schema, spider_flow_published)
VALUES ('gitee-explore-agent', 1, 'Gitee Explore 项目采集', 'PRIVATE',
'{
  "spiderDefinitionCode": "gitee-explore-agent",
  "spiderFlowVersion": 1,
  "spiderFlowName": "Gitee Explore 项目采集",
  "spiderFlowScope": "PRIVATE",
  "nodes": [
    {"id":"start","type":"START","title":"开始","position":{"x":80,"y":300},"config":{"entryUrl":"https://gitee.com/explore/all"}},
    {"id":"parse-list","type":"DATA_PARSE","title":"解析列表(AI)","position":{"x":260,"y":300},"config":{"parseMode":"AI","parseSource":"PAGE_TEXT","fieldMapping":"项目名称:name\n项目描述:description\n项目地址:url\nstar数:stars\nfork数:forks\n编程语言:language","schemaCode":"gitee_project","outputTableName":"gitee_project","resultMappings":[{"fieldName":"name","sourceField":"项目名称","dataType":"string"},{"fieldName":"description","sourceField":"项目描述","dataType":"string"},{"fieldName":"url","sourceField":"项目地址","dataType":"string"},{"fieldName":"stars","sourceField":"star数","dataType":"int"},{"fieldName":"forks","sourceField":"fork数","dataType":"int"},{"fieldName":"language","sourceField":"编程语言","dataType":"string"}],"aiConfig":{"provider":"zai","model":"GLM-4V-Flash","promptTemplate":"请从以下页面内容中提取Gitee项目信息，包括项目名称、描述、star数、fork数、编程语言。以JSON数组格式返回。"}}},
    {"id":"save-result","type":"SAVE_RESULT","title":"异步存储结果","position":{"x":420,"y":180},"config":{"dataSourceCode":"default","schemaCode":"gitee_project","outputTableName":"gitee_project","resultMappings":[{"contextField":"name","columnName":"name"},{"contextField":"description","columnName":"description"},{"contextField":"url","columnName":"repo_url"},{"contextField":"stars","columnName":"stars"},{"contextField":"forks","columnName":"forks"},{"contextField":"language","columnName":"language"}]}},
    {"id":"check-count","type":"CONDITION","title":"数量≥500？","position":{"x":420,"y":420},"config":{"targetTotal":500,"condition":"collectedCount >= 500"}},
    {"id":"next-page","type":"PAGE_ACCESS","title":"翻页(AI)","position":{"x":580,"y":420},"config":{"accessMode":"PLAYWRIGHT","timeout":30,"useAccountPool":true,"maxRetries":3,"carryCookiesAsContext":true,"contextKey":"cookies","aiNavigate":true,"navigateAction":"NEXT_PAGE"}},
    {"id":"end","type":"END","title":"结束","position":{"x":580,"y":580},"config":{}}
  ],
  "edges": [
    {"id":"e1","source":"start","target":"parse-list","sourceOutput":"output","targetInput":"input"},
    {"id":"e2","source":"parse-list","target":"check-count","sourceOutput":"output","targetInput":"input"},
    {"id":"e3","source":"parse-list","target":"save-result","sourceOutput":"output","targetInput":"input"},
    {"id":"e4","source":"check-count","target":"end","sourceOutput":"true","targetInput":"input","label":"≥500，结束"},
    {"id":"e5","source":"check-count","target":"next-page","sourceOutput":"false","targetInput":"input","label":"＜500，翻页"},
    {"id":"e6","source":"next-page","target":"parse-list","sourceOutput":"output","targetInput":"input"}
  ]
}', 1);

INSERT INTO spider_flow_node (spider_definition_code, spider_flow_version, spider_node_code, spider_node_type, spider_node_name, spider_node_config, spider_notify_config, spider_sort_num)
VALUES
('gitee-explore-agent', 1, 'start',          'START',       '开始',          '{"entryUrl":"https://gitee.com/explore/all"}', '{}', 10),
('gitee-explore-agent', 1, 'parse-list',    'DATA_PARSE',   '解析列表(AI)',  '{"parseMode":"AI","parseSource":"PAGE_TEXT","fieldMapping":"项目名称:name\n项目描述:description\n项目地址:url\nstar数:stars\nfork数:forks\n编程语言:language","schemaCode":"gitee_project","outputTableName":"gitee_project","resultMappings":[{"fieldName":"name","sourceField":"项目名称","dataType":"string"},{"fieldName":"description","sourceField":"项目描述","dataType":"string"},{"fieldName":"url","sourceField":"项目地址","dataType":"string"},{"fieldName":"stars","sourceField":"star数","dataType":"int"},{"fieldName":"forks","sourceField":"fork数","dataType":"int"},{"fieldName":"language","sourceField":"编程语言","dataType":"string"}],"aiConfig":{"provider":"zai","model":"GLM-4V-Flash","promptTemplate":"请从以下页面内容中提取Gitee项目信息，包括项目名称、描述、star数、fork数、编程语言。以JSON数组格式返回。"}}', '{}', 20),
('gitee-explore-agent', 1, 'save-result',    'SAVE_RESULT',  '异步存储结果',  '{"dataSourceCode":"default","schemaCode":"gitee_project","outputTableName":"gitee_project","resultMappings":[{"contextField":"name","columnName":"name"},{"contextField":"description","columnName":"description"},{"contextField":"url","columnName":"repo_url"},{"contextField":"stars","columnName":"stars"},{"contextField":"forks","columnName":"forks"},{"contextField":"language","columnName":"language"}]}', '{}', 25),
('gitee-explore-agent', 1, 'check-count',   'CONDITION',    '数量≥500？',    '{"targetTotal":500,"condition":"collectedCount >= 500"}', '{}', 30),
('gitee-explore-agent', 1, 'next-page',     'PAGE_ACCESS',  '翻页(AI)',      '{"accessMode":"PLAYWRIGHT","timeout":30,"useAccountPool":true,"maxRetries":3,"carryCookiesAsContext":true,"contextKey":"cookies","aiNavigate":true,"navigateAction":"NEXT_PAGE"}', '{}', 40),
('gitee-explore-agent', 1, 'end',            'END',          '结束',          '{}', '{}', 50);

INSERT INTO spider_flow_edge (spider_definition_code, spider_flow_version, spider_edge_id, spider_source_node_code, spider_target_node_code, spider_edge_label, spider_source_output, spider_target_input, spider_sort_num)
VALUES
('gitee-explore-agent', 1, 'e1', 'start',          'parse-list',    NULL,         'output', 'input', 10),
('gitee-explore-agent', 1, 'e2', 'parse-list',     'check-count',   NULL,         'output', 'input', 20),
('gitee-explore-agent', 1, 'e3', 'parse-list',     'save-result',   NULL,         'output', 'input', 25),
('gitee-explore-agent', 1, 'e4', 'check-count',    'end',           '≥500，结束',  'true',   'input', 30),
('gitee-explore-agent', 1, 'e5', 'check-count',    'next-page',     '＜500，翻页',  'false',  'input', 35),
('gitee-explore-agent', 1, 'e6', 'next-page',      'parse-list',    NULL,         'output', 'input', 40);

-- ==================== 清理空数据（V1.2） ====================

DELETE FROM `gitee_explore_project` WHERE `name` IS NULL OR `name` = '';
