-- Monitor System Schema
-- Generated from Entity Classes
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `file_system`;
CREATE TABLE `file_system`  (
  `file_system_id` int NOT NULL AUTO_INCREMENT COMMENT '文件ID',
  `file_system_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件名称',
  `file_system_original_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件原始名称',
  `file_system_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件路径',
  `file_system_size` bigint NULL DEFAULT NULL COMMENT '文件大小(字节)',
  `file_system_md5` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件MD5值',
  `file_system_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件类型',
  `file_system_extension` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件扩展名',
  `file_system_chunk_total` int NULL DEFAULT NULL COMMENT '分片总数',
  `file_system_chunk_uploaded` int NULL DEFAULT NULL COMMENT '已上传分片数',
  `file_system_status` int NULL DEFAULT NULL COMMENT '文件状态：0-待合并，1-合并中，2-已完成，3-合并失败',
  `file_system_http_access_enabled` char(1) NULL DEFAULT NULL COMMENT '是否启用HTTP访问',
  `file_system_http_access_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'HTTP访问URL',
  `file_system_http_service_id` int NULL DEFAULT NULL COMMENT '绑定的服务ID',
  `file_system_upload_start_time` datetime(6) NULL DEFAULT NULL COMMENT '上传开始时间',
  `file_system_upload_complete_time` datetime(6) NULL DEFAULT NULL COMMENT '上传完成时间',
  `file_system_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `file_system_group_id` int NULL DEFAULT NULL COMMENT '文件分组ID',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`file_system_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文件系统管理' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `file_system_group`;
CREATE TABLE `file_system_group`  (
  `file_system_group_id` int NOT NULL AUTO_INCREMENT COMMENT '分组ID',
  `file_system_group_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组名称',
  `file_system_group_parent_id` int NULL DEFAULT NULL COMMENT '父分组ID',
  `file_system_group_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组路径',
  `file_system_group_full_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组完整路径',
  `file_system_group_level` int NULL DEFAULT NULL COMMENT '分组层级',
  `file_system_group_tree_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组树路径',
  `file_system_group_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组描述',
  `file_system_group_is_default` char(1) NULL DEFAULT NULL COMMENT '是否默认分组',
  `file_system_group_sort` int NULL DEFAULT NULL COMMENT '分组排序',
  `file_system_group_status` int NULL DEFAULT NULL COMMENT '分组状态',
  `file_system_group_icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组图标',
  `file_system_group_color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组颜色',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`file_system_group_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文件系统分组管理' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `file_system_part`;
CREATE TABLE `file_system_part`  (
  `file_system_part_id` int NOT NULL AUTO_INCREMENT COMMENT '分片ID',
  `file_system_part_file_id` int NULL DEFAULT NULL COMMENT '文件ID',
  `file_system_part_chunk_number` int NULL DEFAULT NULL COMMENT '分片序号(从0开始)',
  `file_system_part_chunk_size` bigint NULL DEFAULT NULL COMMENT '分片大小(字节)',
  `file_system_part_chunk_md5` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分片MD5值',
  `file_system_part_chunk_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分片文件路径',
  `file_system_part_chunk_status` int NULL DEFAULT NULL COMMENT '分片状态：0-待处理，1-处理中，2-已完成，3-处理失败',
  `file_system_part_upload_time` datetime(6) NULL DEFAULT NULL COMMENT '上传时间',
  `file_system_part_process_time` datetime(6) NULL DEFAULT NULL COMMENT '处理时间',
  `file_system_part_error_message` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误信息',
  `file_system_part_retry_count` int NULL DEFAULT NULL COMMENT '重试次数',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`file_system_part_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文件系统分片管理' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `file_system_setting`;
CREATE TABLE `file_system_setting`  (
  `file_system_setting_id` int NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `file_system_setting_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置名称',
  `file_system_setting_chunk_upload_enabled` char(1) NULL DEFAULT NULL COMMENT '是否启用分片上传',
  `file_system_setting_chunk_size_mb` int NULL DEFAULT NULL COMMENT '分片大小(MB)',
  `file_system_setting_merge_task_limit` int NULL DEFAULT NULL COMMENT '合并任务数量限制',
  `file_system_setting_manual_merge_enabled` char(1) NULL DEFAULT NULL COMMENT '是否支持手动合并功能（不影响自动合并）',
  `file_system_setting_download_enabled` char(1) NULL DEFAULT NULL COMMENT '是否启用文件下载',
  `file_system_setting_http_access_enabled` char(1) NULL DEFAULT NULL COMMENT '是否启用HTTP访问',
  `file_system_setting_http_access_domain` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'HTTP访问域名',
  `file_system_setting_allowed_file_types` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件类型白名单(逗号分隔)',
  `file_system_setting_max_file_size_mb` int NULL DEFAULT NULL COMMENT '单个文件最大大小(MB)',
  `file_system_setting_storage_root_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件存储根路径',
  `file_system_setting_temp_storage_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '临时文件存储路径',
  `file_system_setting_file_retention_days` int NULL DEFAULT NULL COMMENT '文件保留天数(0表示永久保留)',
  `file_system_setting_enabled` char(1) NULL DEFAULT NULL COMMENT '是否启用',
  `file_system_setting_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`file_system_setting_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文件系统配置管理' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_acme_account`;
CREATE TABLE `monitor_acme_account`  (
  `acme_account_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `acme_account_email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账号邮箱',
  `acme_account_server` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'ACME 服务端点',
  `acme_account_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账户状态',
  `acme_account_private_key_pem` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账户私钥（PEM）',
  `acme_account_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`acme_account_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'ACME 账户信息' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_acme_certificate`;
CREATE TABLE `monitor_acme_certificate`  (
  `acme_cert_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `acme_account_id` int NULL DEFAULT NULL COMMENT '账户ID',
  `acme_cert_primary_domain` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '主域名',
  `acme_cert_san` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备用域名(SAN)，逗号分隔',
  `acme_cert_challenge_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '挑战类型（HTTP-01/DNS-01）',
  `acme_cert_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '证书状态',
  `acme_cert_not_before` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '有效期起',
  `acme_cert_not_after` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '有效期止',
  `acme_cert_renew_at` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '计划续签日期',
  `acme_cert_chain_pem` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '证书PEM（含链）',
  `acme_cert_key_pem` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '私钥PEM',
  `acme_cert_last_error` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最近错误',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`acme_cert_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'ACME 证书信息' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_acme_order`;
CREATE TABLE `monitor_acme_order`  (
  `acme_order_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `acme_account_id` int NULL DEFAULT NULL COMMENT '账户ID',
  `acme_cert_id` int NULL DEFAULT NULL COMMENT '证书ID',
  `acme_order_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '订单URL',
  `acme_authz_urls` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '授权URL列表(JSON)',
  `acme_order_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '状态',
  `acme_certificate_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最终证书URL',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`acme_order_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'ACME 订单信息' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_application`;
CREATE TABLE `monitor_application`  (
  `monitor_id` int NOT NULL AUTO_INCREMENT COMMENT '',
  `monitor_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '监控名称',
  `monitor_application_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属应用',
  `monitor_prometheus_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Prometheus地址',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '监控名称' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_arthas_session`;
CREATE TABLE `monitor_arthas_session`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '会话ID（Arthas生成的唯一标识）',
  `consumer_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '消费者ID（用于拉取结果）',
  `job_id` int NULL DEFAULT NULL COMMENT '任务ID（用于中断任务）',
  `node_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '节点ID',
  `http_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Arthas HTTP API地址',
  `command` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执行的命令',
  `command_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '命令类型（如：trace、thread、jvm等）',
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '会话状态：RUNNING-运行中, COMPLETED-已完成, INTERRUPTED-已中断, EXPIRED-已过期',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `expire_time` datetime(6) NULL DEFAULT NULL COMMENT '过期时间（超过此时间自动清理）',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注信息',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Arthas Session 记录表' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_arthas_tunnel_config`;
CREATE TABLE `monitor_arthas_tunnel_config`  (
  `monitor_arthas_tunnel_config_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `monitor_arthas_tunnel_config_server_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务器ID',
  `monitor_arthas_tunnel_config_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Tunnel 地址（例如 ws://host:port/ws）',
  `monitor_arthas_tunnel_config_http` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'HTTP 请求地址（可选），用于 HTTP 方式连接 Arthas',
  `monitor_arthas_tunnel_config_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '认证用户名（可选）',
  `monitor_arthas_tunnel_config_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '认证密码（可选）',
  PRIMARY KEY (`monitor_arthas_tunnel_config_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Arthas Tunnel 独立配置实体' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_job`;
CREATE TABLE `monitor_job`  (
  `job_id` int NOT NULL AUTO_INCREMENT COMMENT '任务',
  `job_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `job_schedule_time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '时间配置',
  `job_author` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '负责人',
  `job_alarm_email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '报警邮件',
  `monitor_id` int NULL DEFAULT NULL COMMENT '对应的应用ID',
  `job_application_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '对应的应用名称',
  `job_application_active` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '环境',
  `job_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `job_glue_updatetime` datetime NULL DEFAULT NULL COMMENT 'Glue最后更新时间',
  `job_glue_source` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '源码',
  `job_glue_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Glue类型（例如：脚本、JAR等）',
  `job_fail_retry` int NULL DEFAULT NULL COMMENT '失败重试次数',
  `job_execute_timeout` int NULL DEFAULT NULL COMMENT '超时时间',
  `job_execute_misfire_strategy` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '失效后策略',
  `job_execute_bean` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'bean名称',
  `job_execute_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执行参数',
  `job_execute_router` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执行路由策略',
  `job_trigger_last_time` bigint NULL DEFAULT NULL COMMENT '任务触发的最后一次时间',
  `job_trigger_next_time` bigint NULL DEFAULT NULL COMMENT '任务触发的下一次时间',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`job_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '任务' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_job_log`;
CREATE TABLE `monitor_job_log`  (
  `job_log_id` int NOT NULL AUTO_INCREMENT COMMENT '任务日志',
  `job_log_trigger_time` datetime NULL DEFAULT NULL COMMENT '触发时间',
  `job_log_trigger_date` date NULL DEFAULT NULL COMMENT '触发时间',
  `job_log_trigger_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '触发状态',
  `job_log_trigger_bean` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执行对应的标识',
  `job_log_cost` decimal(20,6) NULL DEFAULT NULL COMMENT '耗时',
  `job_log_trigger_msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '触发消息',
  `job_log_app` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '触发应用',
  `job_log_trigger_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '触发地址',
  `job_log_trigger_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '触发参数',
  `job_log_profile` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '环境',
  `job_log_trigger_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '触发类型',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`job_log_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '任务日志' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_skywalking_config`;
CREATE TABLE `monitor_skywalking_config`  (
  `skywalking_config_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `skywalking_config_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置名称',
  `skywalking_config_host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务地址',
  `skywalking_config_port` int NULL DEFAULT NULL COMMENT '端口',
  `skywalking_config_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名',
  `skywalking_config_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
  `skywalking_config_status` int NULL DEFAULT NULL COMMENT '状态 0:禁用 1:启用',
  `skywalking_config_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `skywalking_config_use_https` int NULL DEFAULT NULL COMMENT '是否使用HTTPS 0:否 1:是',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`skywalking_config_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'SkyWalking 配置信息' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sync_connection`;
CREATE TABLE `monitor_sync_connection`  (
  `sync_connection_id` bigint NOT NULL AUTO_INCREMENT COMMENT '连线ID',
  `sync_task_id` bigint NULL DEFAULT NULL COMMENT '关联任务ID',
  `source_node_id` bigint NULL DEFAULT NULL COMMENT '源节点ID',
  `source_node_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '源节点Key(前端标识)',
  `source_handle` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '源节点输出端口',
  `target_node_id` bigint NULL DEFAULT NULL COMMENT '目标节点ID',
  `target_node_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标节点Key(前端标识)',
  `target_handle` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标节点输入端口',
  `connection_label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '连线标签',
  `sync_connection_create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sync_connection_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '同步节点连线实体类' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sync_node`;
CREATE TABLE `monitor_sync_node`  (
  `sync_node_id` bigint NOT NULL AUTO_INCREMENT COMMENT '节点ID',
  `sync_task_id` bigint NULL DEFAULT NULL COMMENT '关联任务ID',
  `sync_node_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '节点名称',
  `sync_node_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '节点唯一标识(前端生成,用于连线)',
  `sync_node_config` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置参数JSON',
  `sync_node_position` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '前端位置信息JSON(x,y坐标)',
  `sync_node_order` int NULL DEFAULT NULL COMMENT '节点顺序',
  `sync_node_enabled` int NULL DEFAULT NULL COMMENT '是否启用: 0否 1是',
  `sync_node_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '节点描述',
  `sync_node_create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `sync_node_update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sync_node_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '同步节点实体类' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sync_task`;
CREATE TABLE `monitor_sync_task`  (
  `sync_task_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `sync_task_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务名称',
  `sync_task_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务描述',
  `sync_task_batch_size` int NULL DEFAULT NULL COMMENT '批次大小',
  `sync_task_consume_timeout` bigint NULL DEFAULT NULL COMMENT '消费超时时间(毫秒)',
  `sync_task_retry_count` int NULL DEFAULT NULL COMMENT '重试次数',
  `sync_task_retry_interval` bigint NULL DEFAULT NULL COMMENT '重试间隔(毫秒)',
  `sync_task_sync_interval` bigint NULL DEFAULT NULL COMMENT '同步间隔(毫秒)',
  `sync_task_ack_enabled` int NULL DEFAULT NULL COMMENT '是否启用ACK: 0否 1是',
  `sync_task_transaction_enabled` int NULL DEFAULT NULL COMMENT '是否启用事务: 0否 1是',
  `sync_task_cron` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'CRON表达式(定时执行)',
  `sync_task_layout` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '前端连线布局JSON',
  `sync_task_last_run_time` datetime(6) NULL DEFAULT NULL COMMENT '最后执行时间',
  `sync_task_last_run_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后执行状态',
  `sync_task_run_count` bigint NULL DEFAULT NULL COMMENT '总执行次数',
  `sync_task_success_count` bigint NULL DEFAULT NULL COMMENT '成功执行次数',
  `sync_task_fail_count` bigint NULL DEFAULT NULL COMMENT '失败执行次数',
  `sync_task_create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `sync_task_update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sync_task_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '同步任务实体类' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sync_task_log`;
CREATE TABLE `monitor_sync_task_log`  (
  `sync_log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `sync_task_id` bigint NULL DEFAULT NULL COMMENT '关联任务ID',
  `sync_log_read_count` bigint NULL DEFAULT NULL COMMENT '读取数量',
  `sync_log_write_count` bigint NULL DEFAULT NULL COMMENT '写入数量',
  `sync_log_success_count` bigint NULL DEFAULT NULL COMMENT '成功数量',
  `sync_log_fail_count` bigint NULL DEFAULT NULL COMMENT '失败数量',
  `sync_log_retry_count` bigint NULL DEFAULT NULL COMMENT '重试数量',
  `sync_log_dead_letter_count` bigint NULL DEFAULT NULL COMMENT '死信数量',
  `sync_log_filter_count` bigint NULL DEFAULT NULL COMMENT '过滤数量',
  `sync_log_start_time` datetime(6) NULL DEFAULT NULL COMMENT '开始时间',
  `sync_log_end_time` datetime(6) NULL DEFAULT NULL COMMENT '结束时间',
  `sync_log_cost` bigint NULL DEFAULT NULL COMMENT '耗时(毫秒)',
  `sync_log_avg_process_time` double NULL DEFAULT NULL COMMENT '平均处理时间(毫秒)',
  `sync_log_stack_trace` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '详细堆栈(错误时)',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sync_log_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '同步任务执行日志实体类' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_alert_push_config`;
CREATE TABLE `monitor_sys_gen_alert_push_config`  (
  `monitor_sys_gen_alert_push_config_id` int NOT NULL AUTO_INCREMENT COMMENT '每个不同告警类型可配置多个通道（除 WEB 外），支持账号、密码、主账号、Webhook 等。',
  `monitor_sys_gen_server_id` int NULL DEFAULT NULL COMMENT '服务器ID，空或0表示全局',
  `monitor_sys_gen_alert_push_config_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '告警类型',
  `monitor_sys_gen_alert_push_config_channel` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '通知通道（EMAIL/SMS/WEBHOOK/DINGTALK/WECHAT 等）',
  `monitor_sys_gen_alert_push_config_template_id` int NULL DEFAULT NULL COMMENT '模板ID（优先于本条配置项）',
  `monitor_sys_gen_alert_push_config_enabled` char(1) NULL DEFAULT NULL COMMENT '是否启用',
  `monitor_sys_gen_alert_push_config_endpoint` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标地址（Webhook URL、邮箱、手机号等）',
  `monitor_sys_gen_alert_push_config_main_account` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '主账号',
  `monitor_sys_gen_alert_push_config_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名',
  `monitor_sys_gen_alert_push_config_password` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
  `monitor_sys_gen_alert_push_config_token` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '令牌/密钥',
  `monitor_sys_gen_alert_push_config_extra` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '扩展参数（JSON）',
  `monitor_sys_gen_alert_push_config_remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_alert_push_config_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '告警推送配置' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_config`;
CREATE TABLE `monitor_sys_gen_config`  (
  `monitor_sys_gen_config_id` int NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `monitor_sys_gen_config_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置键',
  `monitor_sys_gen_config_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置值',
  `monitor_sys_gen_config_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置描述',
  `monitor_sys_gen_config_env` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '环境',
  `monitor_sys_gen_config_status` int NULL DEFAULT NULL COMMENT '配置状态',
  `monitor_sys_gen_config_app` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属应用',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_config_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '配置管理实体' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_config_push_history`;
CREATE TABLE `monitor_sys_gen_config_push_history`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '历史ID',
  `monitor_sys_gen_config_id` int NULL DEFAULT NULL COMMENT '配置ID',
  `monitor_sys_gen_config_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置键',
  `monitor_sys_gen_config_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置值',
  `monitor_sys_gen_server_id` int NULL DEFAULT NULL COMMENT '服务器ID',
  `monitor_sys_gen_server_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务器名称',
  `push_success` int NULL DEFAULT NULL COMMENT '是否成功',
  `error_message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误信息',
  `push_time` datetime(6) NULL DEFAULT NULL COMMENT '推送时间',
  `operator` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '配置推送历史实体' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_file_system_setting`;
CREATE TABLE `monitor_sys_gen_file_system_setting`  (
  `monitor_sys_gen_file_system_setting_id` int NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_file_system_setting_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文件系统配置管理' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_global_setting`;
CREATE TABLE `monitor_sys_gen_global_setting`  (
  `monitor_sys_gen_global_setting_id` int NOT NULL AUTO_INCREMENT COMMENT '全局配置ID',
  `monitor_sys_gen_global_setting_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置名称',
  `monitor_sys_gen_global_setting_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置描述',
  `monitor_sys_gen_global_setting_dispatcher_enabled` int NULL DEFAULT NULL COMMENT '全局调度器总开关 0:关闭 1:开启',
  `monitor_sys_gen_global_setting_status_summary_enabled` int NULL DEFAULT NULL COMMENT '服务器状态汇总推送开关 0:关闭 1:开启',
  `monitor_sys_gen_global_setting_performance_trends_enabled` int NULL DEFAULT NULL COMMENT '性能趋势推送开关 0:关闭 1:开启',
  `monitor_sys_gen_global_setting_connection_stats_enabled` int NULL DEFAULT NULL COMMENT '连接统计推送开关 0:关闭 1:开启',
  `monitor_sys_gen_global_setting_health_status_enabled` int NULL DEFAULT NULL COMMENT '健康状态推送开关 0:关闭 1:开启',
  `monitor_sys_gen_global_setting_global_alert_enabled` int NULL DEFAULT NULL COMMENT '全局告警处理开关 0:关闭 1:开启',
  `monitor_sys_gen_global_setting_cleanup_enabled` int NULL DEFAULT NULL COMMENT '无效任务清理开关 0:关闭 1:开启',
  `monitor_sys_gen_global_setting_config_refresh_enabled` int NULL DEFAULT NULL COMMENT '配置刷新开关 0:关闭 1:开启',
  `monitor_sys_gen_global_setting_file_upload_check_enabled` int NULL DEFAULT NULL COMMENT '文件上传检查开关 0:关闭 1:开启',
  `monitor_sys_gen_global_setting_data_cleanup_enabled` int NULL DEFAULT NULL COMMENT '数据清理开关 0:关闭 1:开启',
  `monitor_sys_gen_global_setting_backup_enabled` int NULL DEFAULT NULL COMMENT '备份任务开关 0:关闭 1:开启',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_global_setting_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '全局配置设置实体类' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_message_push_template`;
CREATE TABLE `monitor_sys_gen_message_push_template`  (
  `monitor_sys_gen_message_push_template_id` int NOT NULL AUTO_INCREMENT COMMENT '消息推送模板（SPI 推送所需的账号/密钥/Endpoint 等统一配置）',
  `monitor_sys_gen_message_push_template_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板名称',
  `monitor_sys_gen_message_push_template_channel` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '通知通道',
  `monitor_sys_gen_message_push_template_enabled` char(1) NULL DEFAULT NULL COMMENT '是否启用',
  `monitor_sys_gen_message_push_template_endpoint` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标地址（Webhook URL、邮箱、手机号等）',
  `monitor_sys_gen_message_push_template_main_account` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '主账号',
  `monitor_sys_gen_message_push_template_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名',
  `monitor_sys_gen_message_push_template_password` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
  `monitor_sys_gen_message_push_template_token` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '令牌/密钥',
  `monitor_sys_gen_message_push_template_extra` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '扩展参数（JSON）',
  `monitor_sys_gen_message_push_template_remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_message_push_template_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息推送模板（SPI 推送所需的账号/密钥/Endpoint 等统一配置）' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_script`;
CREATE TABLE `monitor_sys_gen_script`  (
  `monitor_sys_gen_script_id` int NOT NULL AUTO_INCREMENT COMMENT '脚本ID',
  `monitor_sys_gen_script_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本名称',
  `monitor_sys_gen_script_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本描述',
  `monitor_sys_gen_script_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本内容',
  `monitor_sys_gen_script_parameters` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '默认参数(JSON格式)',
  `monitor_sys_gen_script_timeout` int NULL DEFAULT NULL COMMENT '执行超时时间(秒)',
  `monitor_sys_gen_script_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本状态',
  `monitor_sys_gen_script_tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本标签(逗号分隔)',
  `monitor_sys_gen_script_category` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本分类',
  `monitor_sys_gen_script_version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本版本',
  `monitor_sys_gen_script_author` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本作者',
  `monitor_sys_gen_script_last_execute_time` datetime(6) NULL DEFAULT NULL COMMENT '最后执行时间',
  `monitor_sys_gen_script_execute_count` int NULL DEFAULT NULL COMMENT '执行次数',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_script_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '脚本管理实体' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_script_category`;
CREATE TABLE `monitor_sys_gen_script_category`  (
  `monitor_sys_gen_script_category_id` int NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `monitor_sys_gen_script_category_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类名称',
  `monitor_sys_gen_script_category_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类描述',
  `monitor_sys_gen_script_category_parent_id` int NULL DEFAULT NULL COMMENT '父分类ID',
  `monitor_sys_gen_script_category_sort` int NULL DEFAULT NULL COMMENT '排序',
  `monitor_sys_gen_script_category_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '状态',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_script_category_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '脚本分类实体' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_script_execution`;
CREATE TABLE `monitor_sys_gen_script_execution`  (
  `monitor_sys_gen_script_execution_id` bigint NOT NULL AUTO_INCREMENT COMMENT '执行记录ID',
  `monitor_sys_gen_script_id` int NULL DEFAULT NULL COMMENT '脚本ID',
  `monitor_sys_gen_script_execution_parameters` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执行参数',
  `monitor_sys_gen_script_execution_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执行状态',
  `monitor_sys_gen_script_execution_start_time` datetime(6) NULL DEFAULT NULL COMMENT '开始执行时间',
  `monitor_sys_gen_script_execution_end_time` datetime(6) NULL DEFAULT NULL COMMENT '结束执行时间',
  `monitor_sys_gen_script_execution_duration` bigint NULL DEFAULT NULL COMMENT '执行耗时(毫秒)',
  `monitor_sys_gen_script_execution_exit_code` int NULL DEFAULT NULL COMMENT '退出码',
  `monitor_sys_gen_script_execution_stdout` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标准输出',
  `monitor_sys_gen_script_execution_stderr` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误输出',
  `monitor_sys_gen_script_execution_error_message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误信息',
  `monitor_sys_gen_script_execution_process_id` bigint NULL DEFAULT NULL COMMENT '进程ID',
  `monitor_sys_gen_script_execution_task_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务ID',
  `monitor_sys_gen_script_execution_server_id` int NULL DEFAULT NULL COMMENT '执行服务器ID',
  `monitor_sys_gen_script_execution_server_ids` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执行服务器ID列表（逗号分隔）',
  `monitor_sys_gen_script_execution_server_names` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执行服务器名称列表（逗号分隔）',
  `monitor_sys_gen_script_execution_method` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执行方式',
  `monitor_sys_gen_script_execution_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本内容快照',
  `monitor_sys_gen_script_execution_script_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本名称',
  `monitor_sys_gen_script_execution_script_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本类型',
  `monitor_sys_gen_script_execution_user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执行人',
  `monitor_sys_gen_script_execution_trigger_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '触发类型',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_script_execution_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '脚本执行历史实体' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_script_upload_record`;
CREATE TABLE `monitor_sys_gen_script_upload_record`  (
  `monitor_sys_gen_script_upload_record_id` int NOT NULL AUTO_INCREMENT COMMENT '示例: 1',
  `monitor_sys_gen_script_id` int NULL DEFAULT NULL COMMENT '脚本ID',
  `monitor_sys_gen_script_upload_server_id` int NULL DEFAULT NULL COMMENT '服务器ID',
  `monitor_sys_gen_script_upload_node_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '节点ID',
  `monitor_sys_gen_script_upload_file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件名',
  `monitor_sys_gen_script_upload_file_size` bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
  `monitor_sys_gen_script_upload_error_message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误信息',
  `monitor_sys_gen_script_upload_script_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本名称',
  `monitor_sys_gen_script_upload_script_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本描述',
  `monitor_sys_gen_script_upload_ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '实际IP（REMOTE 为服务器IP，NODE 为节点IP）',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_script_upload_record_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '脚本上传记录' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_serial`;
CREATE TABLE `monitor_sys_gen_serial`  (
  `monitor_sys_gen_serial_id` int NOT NULL AUTO_INCREMENT COMMENT '串口配置管理',
  `monitor_sys_gen_serial_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '串口名称',
  `monitor_sys_gen_serial_port` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '串口端口',
  `monitor_sys_gen_serial_baud_rate` int NULL DEFAULT NULL COMMENT '波特率',
  `monitor_sys_gen_serial_data_bits` int NULL DEFAULT NULL COMMENT '数据位',
  `monitor_sys_gen_serial_stop_bits` int NULL DEFAULT NULL COMMENT '停止位',
  `monitor_sys_gen_serial_parity` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '校验位',
  `monitor_sys_gen_serial_flow_control` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '流控制',
  `monitor_sys_gen_serial_timeout` int NULL DEFAULT NULL COMMENT '超时时间(毫秒)',
  `monitor_sys_gen_serial_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `gen_id` int NULL DEFAULT NULL COMMENT '生成器id',
  PRIMARY KEY (`monitor_sys_gen_serial_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '串口配置管理' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_server`;
CREATE TABLE `monitor_sys_gen_server`  (
  `monitor_sys_gen_server_id` int NOT NULL AUTO_INCREMENT COMMENT '服务器配置ID',
  `monitor_sys_gen_server_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务器名称',
  `monitor_sys_gen_server_host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务器主机地址',
  `monitor_sys_gen_server_port` int NULL DEFAULT NULL COMMENT '服务器端口',
  `monitor_sys_gen_server_protocol` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '连接协议',
  `monitor_sys_gen_server_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名',
  `monitor_sys_gen_server_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
  `monitor_sys_gen_server_private_key` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '私钥文件路径',
  `monitor_sys_gen_server_timeout` int NULL DEFAULT NULL COMMENT '连接超时时间(毫秒)',
  `monitor_sys_gen_server_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务器描述',
  `monitor_sys_gen_server_status` int NULL DEFAULT NULL COMMENT '服务器状态',
  `monitor_sys_gen_server_tags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务器标签',
  `monitor_sys_gen_server_connection_status` int NULL DEFAULT NULL COMMENT '连接状态',
  `monitor_sys_gen_server_last_connect_time` datetime(6) NULL DEFAULT NULL COMMENT '最后连接时间',
  `monitor_sys_gen_server_connection_error` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '连接失败原因',
  `monitor_sys_gen_server_proxy_id` int NULL DEFAULT NULL COMMENT '代理ID',
  `monitor_sys_gen_server_is_local` int NULL DEFAULT NULL COMMENT '是否本地服务器',
  `monitor_sys_gen_server_ip_addresses` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务器IP地址列表',
  `monitor_sys_gen_server_os_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作系统类型',
  `monitor_sys_gen_server_os_version` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作系统版本',
  `monitor_sys_gen_server_os_arch` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作系统架构',
  `monitor_sys_gen_server_latency` bigint NULL DEFAULT NULL COMMENT '网络延迟(毫秒)',
  `monitor_sys_gen_server_group_id` int NULL DEFAULT NULL COMMENT '服务器分组ID',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_server_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '服务器配置实体类' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_server_alert_history`;
CREATE TABLE `monitor_sys_gen_server_alert_history`  (
  `monitor_sys_gen_server_alert_history_id` bigint NOT NULL AUTO_INCREMENT COMMENT '告警历史ID',
  `monitor_sys_gen_server_alert_history_server_id` int NULL DEFAULT NULL COMMENT '服务器ID',
  `monitor_sys_gen_server_alert_history_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '告警类型',
  `monitor_sys_gen_server_alert_history_level` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '告警级别',
  `monitor_sys_gen_server_alert_history_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '告警状态',
  `monitor_sys_gen_server_alert_history_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '告警标题',
  `monitor_sys_gen_server_alert_history_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '告警内容',
  `monitor_sys_gen_server_alert_history_current_value` decimal(20,6) NULL DEFAULT NULL COMMENT '当前值',
  `monitor_sys_gen_server_alert_history_threshold_value` decimal(20,6) NULL DEFAULT NULL COMMENT '阈值',
  `monitor_sys_gen_server_alert_history_unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '单位',
  `monitor_sys_gen_server_alert_history_trigger_time` datetime(6) NULL DEFAULT NULL COMMENT '触发时间',
  `monitor_sys_gen_server_alert_history_recovery_time` datetime(6) NULL DEFAULT NULL COMMENT '恢复时间',
  `monitor_sys_gen_server_alert_history_silence_until` datetime(6) NULL DEFAULT NULL COMMENT '静默截止时间',
  `monitor_sys_gen_server_alert_history_fingerprint` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '告警指纹（用于去重）',
  `monitor_sys_gen_server_alert_history_notification_sent` char(1) NULL DEFAULT NULL COMMENT '是否已发送通知',
  `monitor_sys_gen_server_alert_history_notification_method` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '通知方式',
  `monitor_sys_gen_server_alert_history_notification_address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '通知地址',
  `monitor_sys_gen_server_alert_history_notification_time` datetime(6) NULL DEFAULT NULL COMMENT '通知发送时间',
  `monitor_sys_gen_server_alert_history_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `monitor_sys_gen_server_alert_history_ignore_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '忽略原因',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_server_alert_history_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '服务器告警历史实体类' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_server_component`;
CREATE TABLE `monitor_sys_gen_server_component`  (
  `monitor_sys_gen_server_component_id` bigint NOT NULL AUTO_INCREMENT COMMENT '组件配置ID',
  `monitor_sys_gen_server_id` int NULL DEFAULT NULL COMMENT '关联服务器ID',
  `monitor_sys_gen_server_component_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件名称',
  `monitor_sys_gen_server_component_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件标题',
  `monitor_sys_gen_server_component_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件类型',
  `monitor_sys_gen_server_component_expression_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '表达式类型',
  `monitor_sys_gen_server_component_expression` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '查询表达式',
  `monitor_sys_gen_server_component_position` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件位置信息',
  `monitor_sys_gen_server_component_chart_config` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图表配置',
  `monitor_sys_gen_server_component_refresh_interval` int NULL DEFAULT NULL COMMENT '刷新间隔(秒)',
  `monitor_sys_gen_server_component_enabled` int NULL DEFAULT NULL COMMENT '是否启用',
  `monitor_sys_gen_server_component_sort_order` int NULL DEFAULT NULL COMMENT '排序序号',
  `monitor_sys_gen_server_component_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件描述',
  `monitor_sys_gen_server_component_show_title` char(1) NULL DEFAULT NULL COMMENT '是否显示标题',
  `monitor_sys_gen_server_component_create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `monitor_sys_gen_server_component_update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_server_component_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '服务器组件配置实体类 (基于MonitorSysGenServerDetailComponent的简化版本)' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_server_component_layout`;
CREATE TABLE `monitor_sys_gen_server_component_layout`  (
  `monitor_sys_gen_server_component_layout_id` bigint NOT NULL AUTO_INCREMENT COMMENT '服务器组件布局配置实体类',
  `monitor_sys_gen_server_id` int NULL DEFAULT NULL COMMENT '关联服务器ID',
  `monitor_sys_gen_server_component_id` bigint NULL DEFAULT NULL COMMENT '关联组件ID',
  `monitor_sys_gen_server_component_layout_x` int NULL DEFAULT NULL COMMENT '布局X坐标',
  `monitor_sys_gen_server_component_layout_y` int NULL DEFAULT NULL COMMENT '布局Y坐标',
  `monitor_sys_gen_server_component_layout_w` int NULL DEFAULT NULL COMMENT '布局宽度',
  `monitor_sys_gen_server_component_layout_h` int NULL DEFAULT NULL COMMENT '布局高度',
  `monitor_sys_gen_server_component_layout_z_index` int NULL DEFAULT NULL COMMENT '布局层级',
  `monitor_sys_gen_server_component_layout_movable` char(1) NULL DEFAULT NULL COMMENT '是否可移动',
  `monitor_sys_gen_server_component_layout_resizable` char(1) NULL DEFAULT NULL COMMENT '是否可调整大小',
  `monitor_sys_gen_server_component_layout_status` int NULL DEFAULT NULL COMMENT '布局状态',
  `monitor_sys_gen_server_component_layout_sort_order` int NULL DEFAULT NULL COMMENT '排序顺序',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_server_component_layout_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '服务器组件布局配置实体类' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_server_group`;
CREATE TABLE `monitor_sys_gen_server_group`  (
  `monitor_sys_gen_server_group_id` int NOT NULL AUTO_INCREMENT COMMENT '分组ID',
  `monitor_sys_gen_server_group_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组名称',
  `monitor_sys_gen_server_group_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组描述',
  `monitor_sys_gen_server_group_color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组颜色(十六进制颜色值)',
  `monitor_sys_gen_server_group_icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组图标',
  `monitor_sys_gen_server_group_status` int NULL DEFAULT NULL COMMENT '分组状态 0:禁用 1:启用',
  `monitor_sys_gen_server_group_is_default` int NULL DEFAULT NULL COMMENT '是否默认分组 0:否 1:是',
  `monitor_sys_gen_server_group_sort` int NULL DEFAULT NULL COMMENT '排序号',
  `monitor_sys_gen_server_group_remark` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_server_group_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '服务器分组实体类' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_server_layout`;
CREATE TABLE `monitor_sys_gen_server_layout`  (
  `monitor_sys_gen_server_layout_id` int NOT NULL AUTO_INCREMENT COMMENT '服务器布局配置实体类',
  `monitor_sys_gen_server_id` int NULL DEFAULT NULL COMMENT '关联服务器ID',
  `monitor_sys_gen_server_layout_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '布局名称',
  `monitor_sys_gen_server_layout_config` varchar(10000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '布局配置',
  `monitor_sys_gen_server_layout_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '布局类型',
  `monitor_sys_gen_server_layout_default` int NULL DEFAULT NULL COMMENT '是否为默认布局',
  `monitor_sys_gen_server_layout_status` int NULL DEFAULT NULL COMMENT '布局状态',
  `monitor_sys_gen_server_layout_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '布局描述',
  `monitor_sys_gen_server_layout_version` int NULL DEFAULT NULL COMMENT '布局版本',
  `monitor_sys_gen_server_layout_sort` int NULL DEFAULT NULL COMMENT '布局排序',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_server_layout_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '服务器布局配置实体类' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_server_logs`;
CREATE TABLE `monitor_sys_gen_server_logs`  (
  `monitor_sys_gen_server_log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '服务器日志实体类',
  `monitor_sys_gen_server_id` int NULL DEFAULT NULL COMMENT '关联服务器ID',
  `monitor_sys_gen_server_log_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志内容',
  `monitor_sys_gen_server_log_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志级别(DEBUG,INFO,WARN,ERROR,FATAL)',
  `monitor_sys_gen_server_log_source` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志来源(SYSTEM,APPLICATION,SECURITY,ACCESS)',
  `monitor_sys_gen_server_log_category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志分类',
  `monitor_sys_gen_server_log_thread` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '线程名称',
  `monitor_sys_gen_server_log_logger` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志记录器名称',
  `monitor_sys_gen_server_log_file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志文件路径',
  `monitor_sys_gen_server_log_line_number` int NULL DEFAULT NULL COMMENT '日志行号',
  `monitor_sys_gen_server_log_exception_stack` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '异常堆栈信息',
  `monitor_sys_gen_server_log_create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `monitor_sys_gen_server_log_server_time` datetime(6) NULL DEFAULT NULL COMMENT '服务器时间',
  `monitor_sys_gen_server_log_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务器IP',
  `monitor_sys_gen_server_log_hostname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '主机名',
  `monitor_sys_gen_server_log_process_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '进程ID',
  `monitor_sys_gen_server_log_tags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志标签(JSON格式)',
  `monitor_sys_gen_server_log_extra_data` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '额外数据(JSON格式)',
  `monitor_sys_gen_server_log_status` int NULL DEFAULT NULL COMMENT '状态(0:删除 1:正常)',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_server_log_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '服务器日志实体类' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_server_log_config`;
CREATE TABLE `monitor_sys_gen_server_log_config`  (
  `monitor_sys_gen_server_log_config_id` bigint NOT NULL AUTO_INCREMENT COMMENT '服务器日志配置实体类',
  `monitor_sys_gen_server_id` int NULL DEFAULT NULL COMMENT '关联服务器ID',
  `monitor_sys_gen_server_log_retention_days` int NULL DEFAULT NULL COMMENT '日志保留天数',
  `monitor_sys_gen_server_log_enabled` int NULL DEFAULT NULL COMMENT '是否启用日志收集(0:否 1:是)',
  `monitor_sys_gen_server_log_level_filter` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志级别过滤器',
  `monitor_sys_gen_server_log_source_filter` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志来源过滤器',
  `monitor_sys_gen_server_log_max_size_mb` int NULL DEFAULT NULL COMMENT '单个日志文件最大大小(MB)',
  `monitor_sys_gen_server_log_max_files` int NULL DEFAULT NULL COMMENT '最大日志文件数量',
  `monitor_sys_gen_server_log_collection_interval` int NULL DEFAULT NULL COMMENT '日志收集间隔(秒)',
  `monitor_sys_gen_server_log_batch_size` int NULL DEFAULT NULL COMMENT '批量处理大小',
  `monitor_sys_gen_server_log_compression_enabled` int NULL DEFAULT NULL COMMENT '是否启用压缩(0:否 1:是)',
  `monitor_sys_gen_server_log_real_time_enabled` int NULL DEFAULT NULL COMMENT '是否启用实时日志(0:否 1:是)',
  `monitor_sys_gen_server_log_file_patterns` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志文件匹配模式(JSON格式)',
  `monitor_sys_gen_server_log_exclude_patterns` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '排除模式(JSON格式)',
  `monitor_sys_gen_server_log_custom_parsers` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '自定义解析器配置(JSON格式)',
  `monitor_sys_gen_server_log_alert_rules` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '告警规则配置(JSON格式)',
  `monitor_sys_gen_server_log_create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `monitor_sys_gen_server_log_update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `monitor_sys_gen_server_log_config_status` int NULL DEFAULT NULL COMMENT '配置状态(0:禁用 1:启用)',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_server_log_config_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '服务器日志配置实体类' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_server_log_statistics`;
CREATE TABLE `monitor_sys_gen_server_log_statistics`  (
  `monitor_sys_gen_server_log_stat_id` bigint NOT NULL AUTO_INCREMENT COMMENT '服务器日志统计实体类',
  `monitor_sys_gen_server_id` int NULL DEFAULT NULL COMMENT '关联服务器ID',
  `monitor_sys_gen_server_log_stat_date` date NULL DEFAULT NULL COMMENT '统计日期',
  `monitor_sys_gen_server_log_stat_hour` int NULL DEFAULT NULL COMMENT '统计小时(0-23)',
  `monitor_sys_gen_server_log_total_count` bigint NULL DEFAULT NULL COMMENT '总日志数量',
  `monitor_sys_gen_server_log_debug_count` bigint NULL DEFAULT NULL COMMENT 'DEBUG级别数量',
  `monitor_sys_gen_server_log_info_count` bigint NULL DEFAULT NULL COMMENT 'INFO级别数量',
  `monitor_sys_gen_server_log_warn_count` bigint NULL DEFAULT NULL COMMENT 'WARN级别数量',
  `monitor_sys_gen_server_log_error_count` bigint NULL DEFAULT NULL COMMENT 'ERROR级别数量',
  `monitor_sys_gen_server_log_fatal_count` bigint NULL DEFAULT NULL COMMENT 'FATAL级别数量',
  `monitor_sys_gen_server_log_size_bytes` bigint NULL DEFAULT NULL COMMENT '日志大小(字节)',
  `monitor_sys_gen_server_log_stat_create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `monitor_sys_gen_server_log_stat_update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_server_log_stat_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '服务器日志统计实体类' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_server_metrics`;
CREATE TABLE `monitor_sys_gen_server_metrics`  (
  `monitor_sys_gen_server_metrics_id` bigint NOT NULL AUTO_INCREMENT COMMENT '服务器指标数据实体类',
  `monitor_sys_gen_server_id` int NULL DEFAULT NULL COMMENT '服务器ID',
  `monitor_sys_gen_server_metrics_collect_time` datetime(6) NULL DEFAULT NULL COMMENT '抓取时间',
  `monitor_sys_gen_server_metrics_cpu_usage` decimal(20,6) NULL DEFAULT NULL COMMENT 'CPU使用率',
  `monitor_sys_gen_server_metrics_cpu_cores` int NULL DEFAULT NULL COMMENT 'CPU核心数',
  `monitor_sys_gen_server_metrics_cpu_load_1m` decimal(20,6) NULL DEFAULT NULL COMMENT 'CPU负载1分钟',
  `monitor_sys_gen_server_metrics_cpu_load_5m` decimal(20,6) NULL DEFAULT NULL COMMENT 'CPU负载5分钟',
  `monitor_sys_gen_server_metrics_cpu_load_15m` decimal(20,6) NULL DEFAULT NULL COMMENT 'CPU负载15分钟',
  `monitor_sys_gen_server_metrics_memory_total` bigint NULL DEFAULT NULL COMMENT '内存总量',
  `monitor_sys_gen_server_metrics_memory_used` bigint NULL DEFAULT NULL COMMENT '内存使用量',
  `monitor_sys_gen_server_metrics_memory_free` bigint NULL DEFAULT NULL COMMENT '内存可用量',
  `monitor_sys_gen_server_metrics_memory_usage` decimal(20,6) NULL DEFAULT NULL COMMENT '内存使用率',
  `monitor_sys_gen_server_metrics_disk_total` bigint NULL DEFAULT NULL COMMENT '磁盘总量',
  `monitor_sys_gen_server_metrics_disk_used` bigint NULL DEFAULT NULL COMMENT '磁盘使用量',
  `monitor_sys_gen_server_metrics_disk_free` bigint NULL DEFAULT NULL COMMENT '磁盘可用量',
  `monitor_sys_gen_server_metrics_disk_usage` decimal(20,6) NULL DEFAULT NULL COMMENT '磁盘使用率',
  `monitor_sys_gen_server_metrics_uptime` bigint NULL DEFAULT NULL COMMENT '系统运行时间',
  `monitor_sys_gen_server_metrics_process_count` int NULL DEFAULT NULL COMMENT '进程数量',
  `monitor_sys_gen_server_metrics_status` int NULL DEFAULT NULL COMMENT '连接状态',
  `monitor_sys_gen_server_metrics_response_time` bigint NULL DEFAULT NULL COMMENT '响应时间',
  `monitor_sys_gen_server_metrics_os_info` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作系统信息',
  `monitor_sys_gen_server_metrics_extra_info` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '扩展信息',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_server_metrics_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '服务器指标数据实体类' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_server_setting`;
CREATE TABLE `monitor_sys_gen_server_setting`  (
  `monitor_sys_gen_server_setting_id` int NOT NULL AUTO_INCREMENT COMMENT '服务器配置设置ID',
  `monitor_sys_gen_server_id` int NULL DEFAULT NULL COMMENT '服务器ID',
  `monitor_sys_gen_server_setting_report_enabled` int NULL DEFAULT NULL COMMENT '是否需要上报',
  `monitor_sys_gen_server_setting_data_report_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据上报方式',
  `monitor_sys_gen_server_setting_prometheus_host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Prometheus服务器地址',
  `monitor_sys_gen_server_setting_prometheus_port` int NULL DEFAULT NULL COMMENT 'Prometheus服务器端口',
  `monitor_sys_gen_server_setting_metrics_retention_days` int NULL DEFAULT NULL COMMENT '指标存储时间(天)',
  `monitor_sys_gen_server_setting_cpu_retention_days` int NULL DEFAULT NULL COMMENT 'CPU指标存储时间(天)',
  `monitor_sys_gen_server_setting_memory_retention_days` int NULL DEFAULT NULL COMMENT '内存指标存储时间(天)',
  `monitor_sys_gen_server_setting_disk_retention_days` int NULL DEFAULT NULL COMMENT '磁盘指标存储时间(天)',
  `monitor_sys_gen_server_setting_network_retention_days` int NULL DEFAULT NULL COMMENT '网络指标存储时间(天)',
  `monitor_sys_gen_server_setting_monitor_interval` int NULL DEFAULT NULL COMMENT '监控间隔(秒)',
  `monitor_sys_gen_server_setting_monitor_enabled` int NULL DEFAULT NULL COMMENT '是否启用监控',
  `monitor_sys_gen_server_setting_cpu_alert_threshold` decimal(20,6) NULL DEFAULT NULL COMMENT 'CPU告警阈值 (百分比)',
  `monitor_sys_gen_server_setting_memory_alert_threshold` decimal(20,6) NULL DEFAULT NULL COMMENT '内存告警阈值 (百分比)',
  `monitor_sys_gen_server_setting_disk_alert_threshold` decimal(20,6) NULL DEFAULT NULL COMMENT '磁盘告警阈值 (百分比)',
  `monitor_sys_gen_server_setting_network_alert_threshold` decimal(20,6) NULL DEFAULT NULL COMMENT '网络告警阈值 (Mbps)',
  `monitor_sys_gen_server_setting_response_time_alert_threshold` bigint NULL DEFAULT NULL COMMENT '响应时间告警阈值 (毫秒)',
  `monitor_sys_gen_server_setting_alert_enabled` int NULL DEFAULT NULL COMMENT '是否启用告警',
  `monitor_sys_gen_server_setting_alert_notification_method` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '告警通知方式',
  `monitor_sys_gen_server_setting_alert_notification_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '告警通知地址',
  `monitor_sys_gen_server_setting_alert_silence_duration` int NULL DEFAULT NULL COMMENT '告警静默时间 (分钟)',
  `monitor_sys_gen_server_setting_auto_recovery_enabled` int NULL DEFAULT NULL COMMENT '是否启用自动恢复通知',
  `monitor_sys_gen_server_setting_data_collection_frequency` int NULL DEFAULT NULL COMMENT '数据收集频率 (秒)',
  `monitor_sys_gen_server_setting_performance_suggestion_enabled` int NULL DEFAULT NULL COMMENT '是否启用性能优化建议',
  `monitor_sys_gen_server_setting_custom_tags` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '自定义标签 (JSON格式)',
  `monitor_sys_gen_server_setting_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置描述',
  `monitor_sys_gen_server_setting_status` int NULL DEFAULT NULL COMMENT '配置状态',
  `monitor_sys_gen_server_setting_docker_enabled` int NULL DEFAULT NULL COMMENT '是否支持Docker',
  `monitor_sys_gen_server_setting_docker_monitor_enabled` int NULL DEFAULT NULL COMMENT '是否启用Docker监控',
  `monitor_sys_gen_server_setting_docker_api_version` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Docker API版本',
  `monitor_sys_gen_server_setting_docker_connection_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Docker连接方式',
  `monitor_sys_gen_server_setting_docker_host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Docker TCP连接地址',
  `monitor_sys_gen_server_setting_docker_port` int NULL DEFAULT NULL COMMENT 'Docker TCP连接端口',
  `monitor_sys_gen_server_setting_docker_socket_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Docker Socket路径',
  `monitor_sys_gen_server_setting_docker_tls_enabled` int NULL DEFAULT NULL COMMENT 'Docker TLS验证',
  `monitor_sys_gen_server_setting_docker_cert_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Docker证书路径',
  `monitor_sys_gen_server_setting_docker_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Docker API 基本认证用户名（可选）',
  `monitor_sys_gen_server_setting_docker_password` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Docker API 基本认证密码（可选）',
  `monitor_sys_gen_server_setting_docker_connect_timeout_millis` int NULL DEFAULT NULL COMMENT 'Docker API 连接超时时间（毫秒，可选，默认30000）',
  `monitor_sys_gen_server_setting_docker_stats_interval` int NULL DEFAULT NULL COMMENT 'Docker状态推送间隔(秒)',
  `monitor_sys_gen_server_setting_proxy_enabled` int NULL DEFAULT NULL COMMENT '是否使用代理',
  `monitor_sys_gen_server_setting_proxy_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '代理类型',
  `monitor_sys_gen_server_setting_proxy_host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '代理服务器地址',
  `monitor_sys_gen_server_setting_proxy_port` int NULL DEFAULT NULL COMMENT '代理服务器端口',
  `monitor_sys_gen_server_setting_proxy_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '代理用户名',
  `monitor_sys_gen_server_setting_proxy_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '代理密码',
  `monitor_sys_gen_server_setting_process_monitor_enabled` int NULL DEFAULT NULL COMMENT '是否启用进程监控',
  `monitor_sys_gen_server_setting_service_monitor_enabled` int NULL DEFAULT NULL COMMENT '是否启用服务监控',
  `monitor_sys_gen_server_setting_port_monitor_enabled` int NULL DEFAULT NULL COMMENT '是否启用端口监控',
  `monitor_sys_gen_server_setting_monitor_ports` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '监控的端口列表 (逗号分隔)',
  `monitor_sys_gen_server_setting_file_management_mode` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件管理方式(NONE:不启用, SSH:SSH模式, API:API模式, NODE:NODE模式, LOCAL:本地模式)',
  `monitor_sys_gen_server_setting_file_management_api_config` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件管理API配置(JSON格式)',
  `monitor_sys_gen_server_setting_file_management_node_client` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '选择的NODE客户端ID(仅在NODE模式下使用)',
  `monitor_sys_gen_server_setting_file_management_enabled` int NULL DEFAULT NULL COMMENT '是否启用文件管理',
  `monitor_sys_gen_server_setting_file_management_timeout` int NULL DEFAULT NULL COMMENT '文件管理操作超时时间 (秒)',
  `monitor_sys_gen_server_setting_file_management_max_retries` int NULL DEFAULT NULL COMMENT '文件管理最大重试次数',
  `monitor_sys_gen_server_setting_client_health_check_interval` int NULL DEFAULT NULL COMMENT '客户端健康状态检查间隔 (秒)',
  `monitor_sys_gen_server_setting_client_health_timeout` int NULL DEFAULT NULL COMMENT '客户端健康状态超时时间 (秒)',
  `monitor_sys_gen_server_setting_port_check_interval` int NULL DEFAULT NULL COMMENT '端口检查间隔 (秒)',
  `monitor_sys_gen_server_setting_online_check_enabled` int NULL DEFAULT NULL COMMENT '是否启用在线状态检查',
  `monitor_sys_gen_server_setting_online_check_interval` int NULL DEFAULT NULL COMMENT '在线状态检查间隔 (秒)',
  `monitor_sys_gen_server_setting_online_check_timeout` int NULL DEFAULT NULL COMMENT '在线状态检查超时时间(ms)',
  `monitor_sys_gen_server_setting_latency_check_enabled` int NULL DEFAULT NULL COMMENT '是否启用延迟检测',
  `monitor_sys_gen_server_setting_latency_check_interval` int NULL DEFAULT NULL COMMENT '延迟检测间隔 (秒)',
  `monitor_sys_gen_server_setting_log_cleanup_enabled` int NULL DEFAULT NULL COMMENT '是否启用日志清理',
  `monitor_sys_gen_server_setting_temp_file_cleanup_enabled` int NULL DEFAULT NULL COMMENT '是否启用临时文件清理',
  `monitor_sys_gen_server_setting_temp_file_retention_hours` int NULL DEFAULT NULL COMMENT '临时文件保留时间 (小时)',
  `monitor_sys_gen_server_setting_websocket_cleanup_enabled` int NULL DEFAULT NULL COMMENT '是否启用WebSocket清理',
  `monitor_sys_gen_server_setting_websocket_cleanup_interval` int NULL DEFAULT NULL COMMENT 'WebSocket清理间隔 (分钟)',
  `monitor_sys_gen_server_setting_prometheus_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Prometheus服务器URL',
  `monitor_sys_gen_server_setting_prometheus_query_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Prometheus查询路径',
  `monitor_sys_gen_server_setting_prometheus_auth_enabled` int NULL DEFAULT NULL COMMENT '是否启用Prometheus认证',
  `monitor_sys_gen_server_setting_prometheus_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Prometheus用户名',
  `monitor_sys_gen_server_setting_prometheus_password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Prometheus密码',
  `monitor_sys_gen_server_setting_prometheus_timeout` int NULL DEFAULT NULL COMMENT 'Prometheus连接超时时间 (秒)',
  `monitor_sys_gen_server_setting_prometheus_labels` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Prometheus标签 (JSON格式)',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_server_setting_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '服务器配置设置实体类' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_server_setting_history`;
CREATE TABLE `monitor_sys_gen_server_setting_history`  (
  `monitor_sys_gen_server_setting_history_id` bigint NOT NULL AUTO_INCREMENT COMMENT '历史记录ID',
  `monitor_sys_gen_server_id` int NULL DEFAULT NULL COMMENT '服务器ID',
  `monitor_sys_gen_server_setting_id` bigint NULL DEFAULT NULL COMMENT '配置ID',
  `change_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '变更描述',
  `change_user` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '变更用户',
  `change_time` datetime(6) NULL DEFAULT NULL COMMENT '变更时间',
  `setting_snapshot` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置快照（JSON格式）',
  `changed_fields` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '变更字段列表（JSON格式）',
  `old_values` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '变更前的值（JSON格式）',
  `new_values` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '变更后的值（JSON格式）',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_server_setting_history_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '服务器配置变更历史实体类' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `node_system`;
CREATE TABLE `node_system`  (
  `node_system_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '节点ID - MD5(IP+PORT)',
  `node_system_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '节点名称',
  `node_system_application_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '应用名称',
  `node_system_ip_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `node_system_port` int NULL DEFAULT NULL COMMENT '端口号',
  `node_system_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '节点状态：ONLINE-在线, OFFLINE-离线, CONNECTING-连接中, ERROR-异常, MAINTENANCE-维护中',
  `node_system_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '节点类型',
  `node_system_version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务版本',
  `node_system_healthy` char(1) NULL DEFAULT NULL COMMENT '是否健康',
  `node_system_first_discovered_time` datetime(6) NULL DEFAULT NULL COMMENT '首次发现时间',
  `node_system_last_online_time` datetime(6) NULL DEFAULT NULL COMMENT '最后在线时间',
  `node_system_last_offline_time` datetime(6) NULL DEFAULT NULL COMMENT '最后离线时间',
  `node_system_last_heartbeat_time` datetime(6) NULL DEFAULT NULL COMMENT '最后心跳时间',
  `node_system_total_online_duration` bigint NULL DEFAULT NULL COMMENT '总在线时长（秒）',
  `node_system_connection_count` int NULL DEFAULT NULL COMMENT '连接次数',
  `node_system_avg_response_time` double NULL DEFAULT NULL COMMENT '平均响应时间（毫秒）',
  `node_system_max_response_time` bigint NULL DEFAULT NULL COMMENT '最大响应时间（毫秒）',
  `node_system_min_response_time` bigint NULL DEFAULT NULL COMMENT '最小响应时间（毫秒）',
  `node_system_avg_cpu_usage` double NULL DEFAULT NULL COMMENT '平均CPU使用率',
  `node_system_avg_memory_usage` double NULL DEFAULT NULL COMMENT '平均内存使用率',
  `node_system_avg_disk_usage` double NULL DEFAULT NULL COMMENT '平均磁盘使用率',
  `node_system_avg_network_latency` double NULL DEFAULT NULL COMMENT '平均网络延迟（毫秒）',
  `node_system_error_count` int NULL DEFAULT NULL COMMENT '错误次数',
  `node_system_last_error_time` datetime(6) NULL DEFAULT NULL COMMENT '最后错误时间',
  `node_system_last_error_message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后错误信息',
  `node_system_metadata` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '扩展元数据（JSON格式）',
  `node_system_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注信息',
  `node_system_monitor_enabled` char(1) NULL DEFAULT NULL COMMENT '是否启用监控',
  `node_system_monitor_interval` int NULL DEFAULT NULL COMMENT '监控间隔（秒）',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`node_system_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '节点系统表 - 用于记录节点历史数据和统计信息' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `system_data_email_history`;
CREATE TABLE `system_data_email_history`  (
  `system_data_email_history_id` int NOT NULL AUTO_INCREMENT COMMENT '记录邮件的详细信息，包括邮件内容、状态、标签等。',
  `system_data_email_history_setting_id` int NULL DEFAULT NULL COMMENT '关联的数据源配置ID',
  `system_data_email_history_message_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮件唯一标识符',
  `system_data_email_history_subject` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮件主题',
  `system_data_email_history_from` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发件人',
  `system_data_email_history_to` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收件人',
  `system_data_email_history_cc` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '抄送人',
  `system_data_email_history_bcc` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密送人',
  `system_data_email_history_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮件内容（纯文本）',
  `system_data_email_history_content_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮件内容类型',
  `system_data_email_history_html_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮件内容（HTML）',
  `system_data_email_history_sent_date` datetime(6) NULL DEFAULT NULL COMMENT '邮件发送时间',
  `system_data_email_history_received_date` datetime(6) NULL DEFAULT NULL COMMENT '邮件接收时间',
  `system_data_email_history_size` bigint NULL DEFAULT NULL COMMENT '邮件大小（字节）',
  `system_data_email_history_is_read` char(1) NULL DEFAULT NULL COMMENT '是否已读',
  `system_data_email_history_is_starred` char(1) NULL DEFAULT NULL COMMENT '是否收藏',
  `system_data_email_history_is_important` char(1) NULL DEFAULT NULL COMMENT '是否重要',
  `system_data_email_history_is_deleted` char(1) NULL DEFAULT NULL COMMENT '是否已删除',
  `system_data_email_history_labels` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮件标签（多个标签用逗号分隔）',
  `system_data_email_history_folder` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮件文件夹',
  `system_data_email_history_priority` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮件优先级',
  `system_data_email_history_attachment_count` int NULL DEFAULT NULL COMMENT '附件数量',
  `system_data_email_history_attachments` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '附件信息（JSON格式）',
  `system_data_email_history_headers` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮件头信息（JSON格式）',
  `system_data_email_history_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮件类型',
  `system_data_email_history_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮件状态',
  `system_data_email_history_remark` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `system_data_email_history_sync_status` int NULL DEFAULT NULL COMMENT '同步状态：0-未同步，1-已同步，2-同步失败',
  `system_data_email_history_sync_time` datetime(6) NULL DEFAULT NULL COMMENT '同步时间',
  `system_data_email_history_error_message` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误信息',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`system_data_email_history_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '邮件历史记录' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `system_data_field_comment`;
CREATE TABLE `system_data_field_comment`  (
  `system_data_field_comment_id` int NOT NULL AUTO_INCREMENT COMMENT '记录特定数据源(`systemDataFieldCommentSettingId`)下某个节点(`systemDataFieldCommentNodePath`)或具体的库/表/列的注释。',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`system_data_field_comment_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '数据管理-字段注释' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `system_data_setting`;
CREATE TABLE `system_data_setting`  (
  `system_data_setting_id` int NOT NULL AUTO_INCREMENT COMMENT '记录各类数据源的基础连接信息与控制台能力配置。',
  `system_data_setting_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `system_data_setting_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '类型：JDBC/REDIS/ZK 等',
  `system_data_setting_server` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '直连URL（优先）',
  `system_data_setting_host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '主机名',
  `system_data_setting_port` int NULL DEFAULT NULL COMMENT '端口',
  `system_data_setting_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名',
  `system_data_setting_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
  `system_data_setting_database` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据库名称',
  `system_data_setting_policy` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '策略',
  `system_data_setting_auth_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '认证方式',
  `system_data_setting_icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标URL',
  `system_data_setting_console_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '控制台展示类型：TABLE/GRAPH/FILE',
  `system_data_setting_chart_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图表类型',
  `system_data_setting_timeout_ms` int NULL DEFAULT NULL COMMENT '超时毫秒',
  `system_data_setting_mode` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模式：REMOTE/FILE',
  `system_data_setting_enabled` char(1) NULL DEFAULT NULL COMMENT '是否启用',
  `system_data_setting_config` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '控制台权限/个性化配置(JSON)',
  `system_data_setting_protocol` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '协议',
  `system_data_setting_driver_class` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'JDBC 驱动类名',
  `system_data_setting_driver_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'JDBC 驱动文件路径',
  `system_data_setting_image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '远程图片地址',
  `system_data_setting_backup_status` int NULL DEFAULT NULL COMMENT '备份状态;0:未启动; 1:已启动',
  `system_data_setting_idle` char(1) NULL DEFAULT NULL COMMENT '是否支持监听',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`system_data_setting_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '数据管理-客户端配置' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `system_data_setting_item`;
CREATE TABLE `system_data_setting_item`  (
  `system_data_setting_item_id` int NOT NULL AUTO_INCREMENT COMMENT '记录某数据源的扩展键值配置。',
  `system_data_setting_item_setting_id` int NULL DEFAULT NULL COMMENT '数据源ID',
  `system_data_setting_item_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置项名称',
  `system_data_setting_item_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置项值',
  `system_data_setting_item_order` int NULL DEFAULT NULL COMMENT '排序号',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`system_data_setting_item_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '数据管理-配置项' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_file_share`;
CREATE TABLE `monitor_sys_file_share`  (
  `share_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分享ID（UUID）',
  PRIMARY KEY (`share_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文件分享记录实体' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `system_server`;
CREATE TABLE `system_server`  (
  `system_server_id` int NOT NULL AUTO_INCREMENT COMMENT '系统服务器ID',
  `system_server_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '系统服务器名称',
  `system_server_host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '系统服务器主机',
  `system_server_port` int NULL DEFAULT NULL COMMENT '系统服务器端口',
  `system_server_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '系统服务器类型',
  `system_server_context_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '系统服务器上下文路径',
  `system_server_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '系统服务器启动状态',
  `system_server_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '系统服务器描述',
  `system_server_config` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '系统服务器配置',
  `system_server_auto_start` char(1) NULL DEFAULT NULL COMMENT '系统服务器是否自动启动',
  `system_server_max_connections` int NULL DEFAULT NULL COMMENT '系统服务器最大连接数',
  `system_server_timeout` int NULL DEFAULT NULL COMMENT '系统服务器超时时间',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`system_server_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统服务器配置实体类' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `system_server_log`;
CREATE TABLE `system_server_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '对应数据表：systemserverLog（按需求指定的表名）',
  `server_id` int NULL DEFAULT NULL COMMENT '系统服务器ID',
  `filter_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '过滤器类型（SPI类型或类名）',
  `process_status` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '处理状态（例如：IP限流、黑名单拦截、白名单通过等）',
  `client_ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户端IP',
  `client_geo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户端地理位置信息',
  `access_time` datetime(6) NULL DEFAULT NULL COMMENT '访问时间',
  `duration_ms` bigint NULL DEFAULT NULL COMMENT '处理时长(毫秒)',
  `store_time` datetime(6) NULL DEFAULT NULL COMMENT '存储时间',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '服务管理-过滤器处理日志' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `system_server_setting`;
CREATE TABLE `system_server_setting`  (
  `system_server_setting_id` int NOT NULL AUTO_INCREMENT COMMENT '系统服务器配置ID',
  `system_server_setting_https_enabled` char(1) NULL DEFAULT NULL COMMENT '是否启用HTTPS',
  `system_server_setting_https_pem_key_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'PEM私钥密码',
  `system_server_setting_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '系统服务器配置名称',
  `system_server_setting_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '系统服务器配置类型',
  `system_server_setting_server_id` int NULL DEFAULT NULL COMMENT '关联的服务器ID',
  `system_server_setting_order` int NULL DEFAULT NULL COMMENT '排序字段',
  `system_server_setting_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置描述',
  `system_server_setting_enabled` char(1) NULL DEFAULT NULL COMMENT '配置状态',
  `system_server_setting_class_name` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置类名',
  `system_server_setting_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置版本',
  `system_server_setting_config` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置JSON数据',
  `system_server_setting_filter_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '启动的时候生成',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`system_server_setting_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统服务器配置表实体类' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `system_server_setting_address_rate_limit`;
CREATE TABLE `system_server_setting_address_rate_limit`  (
  `system_server_setting_address_rate_limit_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `address_rate_limit_server_id` int NULL DEFAULT NULL COMMENT '服务器ID',
  `address_rate_limit_address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地址(接口/路径)',
  `address_rate_limit_qps` int NULL DEFAULT NULL COMMENT '每秒请求阈值',
  `address_rate_limit_enabled` char(1) NULL DEFAULT NULL COMMENT '是否启用',
  `address_rate_limit_setting_id` int NULL DEFAULT NULL COMMENT '关联的系统配置ID(SystemServerSettingId)',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`system_server_setting_address_rate_limit_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AddressRateLimitServletFilter 专用配置表' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `system_server_setting_file_storage`;
CREATE TABLE `system_server_setting_file_storage`  (
  `system_server_setting_file_storage_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `file_storage_server_id` int NULL DEFAULT NULL COMMENT '服务器ID',
  `file_storage_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '存储类型: LOCAL/S3/MINIO/OSS',
  `file_storage_base_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '本地根路径',
  `file_storage_endpoint` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '对象存储 Endpoint',
  `file_storage_bucket` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Bucket',
  `file_storage_access_key` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'AccessKey',
  `file_storage_secret_key` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SecretKey',
  `file_storage_region` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Region',
  `file_storage_enabled` char(1) NULL DEFAULT NULL COMMENT '是否启用',
  `file_storage_connection_timeout` bigint NULL DEFAULT NULL COMMENT '连接超时',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`system_server_setting_file_storage_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'FileStorageServletFilter 专用配置表' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `system_server_setting_ip_rate_limit`;
CREATE TABLE `system_server_setting_ip_rate_limit`  (
  `system_server_setting_ip_rate_limit_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `ip_rate_limit_server_id` int NULL DEFAULT NULL COMMENT '服务器ID',
  `ip_rate_limit_ip` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'IP地址(支持CIDR)',
  `ip_rate_limit_qps` int NULL DEFAULT NULL COMMENT '每秒请求阈值',
  `ip_rate_limit_enabled` char(1) NULL DEFAULT NULL COMMENT '是否启用',
  `ip_rate_limit_setting_id` int NULL DEFAULT NULL COMMENT '关联的系统配置ID(SystemServerSettingId)',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`system_server_setting_ip_rate_limit_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'IPRateLimitServletFilter 专用配置表' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `system_server_setting_item`;
CREATE TABLE `system_server_setting_item`  (
  `system_server_setting_item_id` int NOT NULL AUTO_INCREMENT COMMENT '系统服务器配置项ID',
  `system_server_setting_item_setting_id` int NULL DEFAULT NULL COMMENT '关联的配置ID',
  `system_server_setting_item_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置项名称',
  `system_server_setting_item_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置项值',
  `system_server_setting_item_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置项描述',
  `system_server_setting_item_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置项类型',
  `system_server_setting_item_required` char(1) NULL DEFAULT NULL COMMENT '是否必填',
  `system_server_setting_item_default_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '默认值',
  `system_server_setting_item_validation_rule` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '验证规则',
  `system_server_setting_item_order` int NULL DEFAULT NULL COMMENT '排序字段',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`system_server_setting_item_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统服务器配置项详情表实体类' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `system_server_setting_preview_extension`;
CREATE TABLE `system_server_setting_preview_extension`  (
  `system_server_setting_preview_extension_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `preview_extension_server_id` int NULL DEFAULT NULL COMMENT '服务器ID',
  `preview_extension_disabled` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '禁用预览的扩展名列表（逗号分隔）',
  `preview_extension_allowed` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '允许预览的扩展名列表（逗号分隔）',
  `preview_extension_whitelist_mode` char(1) NULL DEFAULT NULL COMMENT '是否启用白名单模式',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`system_server_setting_preview_extension_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '预览扩展名配置表' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `system_server_setting_service_discovery`;
CREATE TABLE `system_server_setting_service_discovery`  (
  `system_server_setting_service_discovery_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `service_discovery_server_id` int NULL DEFAULT NULL COMMENT '服务器ID',
  `service_discovery_mode` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务发现模式: MONITOR/SPRING/TABLE/HAZELCAST',
  `service_discovery_balance` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '默认负载均衡策略',
  `service_discovery_bean_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Spring容器中的ServiceDiscovery Bean名称',
  `service_discovery_enabled` char(1) NULL DEFAULT NULL COMMENT '是否启用',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`system_server_setting_service_discovery_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'ServiceDiscoveryServletFilter 专用配置表' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `system_server_setting_service_discovery_mapping`;
CREATE TABLE `system_server_setting_service_discovery_mapping`  (
  `system_server_setting_service_discovery_mapping_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `service_discovery_server_id` int NULL DEFAULT NULL COMMENT '服务器ID',
  `service_discovery_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务名称',
  `service_discovery_address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务地址',
  `service_discovery_weight` int NULL DEFAULT NULL COMMENT '服务权重',
  `service_discovery_enabled` char(1) NULL DEFAULT NULL COMMENT '是否启用',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`system_server_setting_service_discovery_mapping_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'ServiceDiscovery 默认模式映射明细表' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `system_soft`;
CREATE TABLE `system_soft`  (
  `system_soft_id` int NOT NULL AUTO_INCREMENT COMMENT '系统软件ID',
  `system_soft_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '软件名称',
  `system_soft_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '软件代码',
  `system_soft_category` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '软件分类',
  `system_soft_icon` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '软件图标',
  `system_soft_tags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '软件标签',
  `system_soft_desc` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '软件描述',
  `system_soft_registry_id` int NULL DEFAULT NULL COMMENT '镜像仓库ID',
  `system_soft_docker_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Docker镜像名称',
  `system_soft_default_install_method` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '默认安装方式',
  `system_soft_default_install_params` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '默认安装参数',
  `system_soft_status` int NULL DEFAULT NULL COMMENT '软件状态',
  `system_soft_is_official` int NULL DEFAULT NULL COMMENT '是否官方软件',
  `system_soft_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '软件备注',
  `system_soft_star_count` int NULL DEFAULT NULL COMMENT 'Star 数',
  `system_soft_pull_count` int NULL DEFAULT NULL COMMENT 'Pull 数',
  `system_soft_ext_field1` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '扩展字段1',
  `system_soft_ext_field2` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '扩展字段2',
  `system_soft_ext_field3` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '扩展字段3',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`system_soft_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统软件信息实体类' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `system_soft_container`;
CREATE TABLE `system_soft_container`  (
  `system_soft_container_id` int NOT NULL AUTO_INCREMENT COMMENT '系统软件容器ID',
  `system_soft_id` int NULL DEFAULT NULL COMMENT '关联的软件ID',
  `system_soft_version_id` int NULL DEFAULT NULL COMMENT '关联的软件版本ID',
  `system_server_id` int NULL DEFAULT NULL COMMENT '关联的服务器ID',
  `system_soft_image_id` int NULL DEFAULT NULL COMMENT '关联的镜像ID',
  `system_soft_container_docker_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Docker容器ID',
  `system_soft_container_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '容器名称',
  `system_soft_container_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '容器镜像',
  `system_soft_container_image_tag` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '容器镜像标签',
  `system_soft_container_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '容器状态',
  `system_soft_container_ports` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '容器端口映射',
  `system_soft_container_env` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '容器环境变量',
  `system_soft_container_volumes` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '容器数据卷',
  `system_soft_container_networks` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '容器网络',
  `system_soft_container_command` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '容器启动命令',
  `system_soft_container_args` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '容器启动参数',
  `system_soft_container_created_time` datetime(6) NULL DEFAULT NULL COMMENT '容器创建时间',
  `system_soft_container_started_time` datetime(6) NULL DEFAULT NULL COMMENT '容器启动时间',
  `system_soft_container_finished_time` datetime(6) NULL DEFAULT NULL COMMENT '容器结束时间',
  `system_soft_container_restart_count` int NULL DEFAULT NULL COMMENT '容器重启次数',
  `system_soft_container_config` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '容器配置信息',
  `system_soft_container_health_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '容器健康状态',
  `system_soft_container_auto_restart` int NULL DEFAULT NULL COMMENT '是否自动重启',
  `system_soft_container_cpu_limit` double NULL DEFAULT NULL COMMENT 'CPU使用限制',
  `system_soft_container_memory_limit` bigint NULL DEFAULT NULL COMMENT '内存使用限制',
  `system_soft_container_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '容器备注',
  `system_soft_container_ext_field1` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '扩展字段1',
  `system_soft_container_ext_field2` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '扩展字段2',
  `system_soft_container_ext_field3` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '扩展字段3',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`system_soft_container_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统软件容器信息实体类' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `system_soft_container_stats`;
CREATE TABLE `system_soft_container_stats`  (
  `system_soft_container_stats_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `system_soft_container_stats_container_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '容器ID',
  `system_soft_container_stats_container_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '容器名称',
  `system_soft_container_stats_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '状态',
  `system_soft_container_stats_started_at` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '启动时间',
  `system_soft_container_stats_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '镜像',
  `system_soft_container_stats_command` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '命令',
  `system_soft_container_stats_created` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建时间',
  `system_soft_container_stats_ports` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '端口映射',
  `system_soft_container_stats_labels` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签',
  `system_soft_container_stats_status_detail` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '状态详情',
  `system_soft_container_stats_record_time` datetime(6) NULL DEFAULT NULL COMMENT '记录创建时间',
  `system_soft_container_stats_disk_read` bigint NULL DEFAULT NULL COMMENT '磁盘读取字节数',
  `system_soft_container_stats_disk_write` bigint NULL DEFAULT NULL COMMENT '磁盘写入字节数',
  `system_soft_container_stats_cpu_percent` double NULL DEFAULT NULL COMMENT 'CPU使用率 (%)',
  `system_soft_container_stats_memory_usage` bigint NULL DEFAULT NULL COMMENT '内存使用字节数',
  `system_soft_container_stats_memory_limit` bigint NULL DEFAULT NULL COMMENT '内存限制字节数',
  `system_soft_container_stats_memory_percent` double NULL DEFAULT NULL COMMENT '内存使用率 (%)',
  `system_soft_container_stats_network_rx_bytes` bigint NULL DEFAULT NULL COMMENT '网络接收字节数',
  `system_soft_container_stats_network_tx_bytes` bigint NULL DEFAULT NULL COMMENT '网络发送字节数',
  `system_soft_container_id` int NULL DEFAULT NULL COMMENT '容器关联ID',
  `system_soft_image_id` int NULL DEFAULT NULL COMMENT '镜像关联ID',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`system_soft_container_stats_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '容器统计信息实体类' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `system_soft_image`;
CREATE TABLE `system_soft_image`  (
  `system_soft_image_id` int NOT NULL AUTO_INCREMENT COMMENT '系统软件镜像ID',
  `system_soft_id` int NULL DEFAULT NULL COMMENT '关联的软件ID',
  `system_soft_image_server_id` int NULL DEFAULT NULL COMMENT '关联的服务器ID',
  `system_soft_image_server_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务器名称',
  `system_soft_image_image_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Docker镜像ID',
  `system_soft_image_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '镜像名称',
  `system_soft_image_tag` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '镜像标签',
  `system_soft_image_full_name` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '镜像完整名称',
  `system_soft_image_repository_id` int NULL DEFAULT NULL COMMENT '镜像仓库地址ID',
  `system_soft_image_size` bigint NULL DEFAULT NULL COMMENT '镜像大小',
  `system_soft_image_created` datetime(6) NULL DEFAULT NULL COMMENT '镜像创建时间',
  `system_soft_image_architecture` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '镜像架构',
  `system_soft_image_os_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作系统类型',
  `system_soft_image_digest` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '镜像摘要',
  `system_soft_image_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '镜像状态',
  `system_soft_image_is_official` int NULL DEFAULT NULL COMMENT '是否官方镜像',
  `system_soft_image_description` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '镜像描述',
  `system_soft_image_tag_count` int NULL DEFAULT NULL COMMENT '标签数量',
  `system_soft_image_pull_count` bigint NULL DEFAULT NULL COMMENT '拉取次数',
  `system_soft_image_star_count` bigint NULL DEFAULT NULL COMMENT '星标数量',
  `system_soft_image_last_pulled` datetime(6) NULL DEFAULT NULL COMMENT '最后拉取时间',
  `system_soft_image_last_pushed` datetime(6) NULL DEFAULT NULL COMMENT '最后推送时间',
  `system_soft_image_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '镜像备注',
  `system_soft_image_ext_field1` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '扩展字段1',
  `system_soft_image_ext_field2` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '扩展字段2',
  `system_soft_image_ext_field3` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '扩展字段3',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`system_soft_image_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统软件镜像信息实体类' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `system_soft_record`;
CREATE TABLE `system_soft_record`  (
  `system_soft_record_id` int NOT NULL AUTO_INCREMENT COMMENT '系统软件记录ID',
  `system_soft_id` int NULL DEFAULT NULL COMMENT '关联的软件ID',
  `system_soft_version_id` int NULL DEFAULT NULL COMMENT '关联的软件版本ID',
  `system_server_id` int NULL DEFAULT NULL COMMENT '关联的服务器ID',
  `system_soft_record_operation_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作类型',
  `system_soft_record_message` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作消息',
  `system_soft_record_method` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作方法',
  `system_soft_record_env_params` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '环境参数',
  `system_soft_record_params` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作参数',
  `system_soft_record_time` datetime(6) NULL DEFAULT NULL COMMENT '记录时间',
  `system_soft_record_status` int NULL DEFAULT NULL COMMENT '操作状态',
  `system_soft_record_user` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作用户',
  `system_soft_record_container_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联的容器ID',
  `system_soft_record_start_time` datetime(6) NULL DEFAULT NULL COMMENT '操作开始时间',
  `system_soft_record_end_time` datetime(6) NULL DEFAULT NULL COMMENT '操作结束时间',
  `system_soft_record_duration` bigint NULL DEFAULT NULL COMMENT '操作耗时',
  `system_soft_record_error_message` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误信息',
  `system_soft_record_result` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作结果',
  `system_soft_record_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '记录备注',
  `system_soft_record_ext_field1` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '扩展字段1',
  `system_soft_record_ext_field2` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '扩展字段2',
  `system_soft_record_ext_field3` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '扩展字段3',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`system_soft_record_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统软件操作记录实体类' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `system_soft_registry`;
CREATE TABLE `system_soft_registry`  (
  `system_soft_registry_id` int NOT NULL AUTO_INCREMENT COMMENT '系统软件镜像仓库ID',
  `system_soft_registry_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '仓库名称',
  `system_soft_registry_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '仓库类型',
  `system_soft_registry_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '仓库地址',
  `system_soft_registry_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '仓库用户名',
  `system_soft_registry_password` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '仓库密码',
  `system_soft_registry_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '仓库邮箱',
  `system_soft_registry_active` int NULL DEFAULT NULL COMMENT '是否激活，激活的仓库用于软件搜索',
  `system_soft_registry_ssl_enabled` int NULL DEFAULT NULL COMMENT '是否启用SSL',
  `system_soft_registry_support_sync` int NULL DEFAULT NULL COMMENT '是否支持同步',
  `system_soft_registry_timeout` int NULL DEFAULT NULL COMMENT '连接超时时间(秒)',
  `system_soft_registry_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '仓库描述',
  `system_soft_registry_config` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '仓库配置信息',
  `system_soft_registry_last_connect_time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后连接时间',
  `system_soft_registry_connect_status` int NULL DEFAULT NULL COMMENT '连接状态',
  `system_soft_registry_error_message` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '连接错误信息',
  `system_soft_registry_sort` int NULL DEFAULT NULL COMMENT '排序号',
  `system_soft_registry_status` int NULL DEFAULT NULL COMMENT '状态(0:禁用, 1:启用)',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`system_soft_registry_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统软件镜像仓库信息实体类' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `system_soft_version`;
CREATE TABLE `system_soft_version`  (
  `system_soft_version_id` int NOT NULL AUTO_INCREMENT COMMENT '系统软件版本ID',
  `system_soft_id` int NULL DEFAULT NULL COMMENT '关联的软件ID',
  `system_soft_version_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '版本名称',
  `sys_soft_version_digest` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '摘要',
  `system_soft_version_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '版本号',
  `system_soft_version_download_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '版本下载地址',
  `system_soft_version_image_tag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '镜像标签',
  `system_soft_version_install_template` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '安装模板',
  `system_soft_version_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '版本描述',
  `system_soft_version_size` bigint NULL DEFAULT NULL COMMENT '版本大小',
  `system_soft_version_status` int NULL DEFAULT NULL COMMENT '版本状态',
  `system_soft_version_is_latest` int NULL DEFAULT NULL COMMENT '是否为最新版本',
  `system_soft_version_is_stable` int NULL DEFAULT NULL COMMENT '是否为稳定版本',
  `system_soft_version_release_time` datetime(6) NULL DEFAULT NULL COMMENT '发布时间',
  `system_soft_version_min_requirements` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最小系统要求',
  `system_soft_version_recommended_requirements` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '推荐系统要求',
  `system_soft_version_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '版本备注',
  `system_soft_version_ext_field1` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '扩展字段1',
  `system_soft_version_ext_field2` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '扩展字段2',
  `system_soft_version_ext_field3` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '扩展字段3',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`system_soft_version_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '' ROW_FORMAT = DYNAMIC;


DROP TABLE IF EXISTS monitor_sys_gen_file_system;
CREATE TABLE monitor_sys_gen_file_system  (
  monitor_sys_gen_file_system_id int NOT NULL AUTO_INCREMENT COMMENT '文件系统ID',
  monitor_sys_gen_file_system_name varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件名称',
  monitor_sys_gen_file_system_original_name varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件原始名称',
  monitor_sys_gen_file_system_path varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件路径',
  monitor_sys_gen_file_system_size bigint NULL DEFAULT NULL COMMENT '文件大小(字节)',
  monitor_sys_gen_file_system_md5 varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件MD5',
  monitor_sys_gen_file_system_type varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件类型',
  monitor_sys_gen_file_system_extension varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件扩展名',
  monitor_sys_gen_file_system_status int NULL DEFAULT NULL COMMENT '文件状态 0:待合并 1:正常 2:合并失败 3:已删除',
  monitor_sys_gen_file_system_is_chunk int NULL DEFAULT NULL COMMENT '是否分片上传 0:否 1:是',
  monitor_sys_gen_file_system_chunk_total int NULL DEFAULT NULL COMMENT '分片总数',
  monitor_sys_gen_file_system_chunk_uploaded int NULL DEFAULT NULL COMMENT '已上传分片数',
  monitor_sys_gen_file_system_description varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件描述',
  monitor_sys_gen_file_system_upload_user varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '上传用户',
  monitor_sys_gen_file_system_download_count int NULL DEFAULT NULL COMMENT '下载次数',
  monitor_sys_gen_file_system_last_download_time datetime(6) NULL DEFAULT NULL COMMENT '最后下载时间',
  monitor_sys_gen_file_system_http_access int NULL DEFAULT NULL COMMENT '是否允许HTTP访问 0:否 1:是',
  monitor_sys_gen_file_system_http_url varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'HTTP访问URL',
  monitor_sys_gen_file_system_remark varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (monitor_sys_gen_file_system_id) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文件系统管理' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS monitor_sys_gen_file_system_part;
CREATE TABLE monitor_sys_gen_file_system_part  (
  monitor_sys_gen_file_system_part_id int NOT NULL AUTO_INCREMENT COMMENT '分片ID',
  monitor_sys_gen_file_system_id int NULL DEFAULT NULL COMMENT '文件系统ID',
  monitor_sys_gen_file_system_part_index int NULL DEFAULT NULL COMMENT '分片序号(从1开始)',
  monitor_sys_gen_file_system_part_size bigint NULL DEFAULT NULL COMMENT '分片大小(字节)',
  monitor_sys_gen_file_system_part_md5 varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分片MD5',
  monitor_sys_gen_file_system_part_path varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分片文件路径',
  monitor_sys_gen_file_system_part_status int NULL DEFAULT NULL COMMENT '分片状态 0:待上传 1:处理中 2:已完成 3:上传失败',
  monitor_sys_gen_file_system_part_start_time datetime(6) NULL DEFAULT NULL COMMENT '上传开始时间',
  monitor_sys_gen_file_system_part_end_time datetime(6) NULL DEFAULT NULL COMMENT '上传完成时间',
  monitor_sys_gen_file_system_part_error_msg varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误信息',
  monitor_sys_gen_file_system_part_retry_count int NULL DEFAULT NULL COMMENT '重试次数',
  monitor_sys_gen_file_system_part_remark varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (monitor_sys_gen_file_system_part_id) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文件系统分片管理' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
