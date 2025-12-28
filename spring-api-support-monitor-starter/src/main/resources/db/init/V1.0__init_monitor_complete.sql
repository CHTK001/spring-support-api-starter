-- Monitor System Schema
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `device_metrics`;
CREATE TABLE `device_metrics`  (
  `device_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `device_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `ip_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `port` int NULL DEFAULT NULL,
  `os_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `os_version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `os_arch` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `hostname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `cpu_usage` double NULL DEFAULT NULL,
  `cpu_cores` int NULL DEFAULT NULL,
  `cpu_frequency` bigint NULL DEFAULT NULL,
  `memory_usage` double NULL DEFAULT NULL,
  `total_memory` bigint NULL DEFAULT NULL,
  `used_memory` bigint NULL DEFAULT NULL,
  `available_memory` bigint NULL DEFAULT NULL,
  `disk_usage` double NULL DEFAULT NULL,
  `total_disk` bigint NULL DEFAULT NULL,
  `used_disk` bigint NULL DEFAULT NULL,
  `available_disk` bigint NULL DEFAULT NULL,
  `network_in_bytes` bigint NULL DEFAULT NULL,
  `network_out_bytes` bigint NULL DEFAULT NULL,
  `network_in_packets` bigint NULL DEFAULT NULL,
  `network_out_packets` bigint NULL DEFAULT NULL,
  `load_average` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `uptime` bigint NULL DEFAULT NULL,
  `process_count` int NULL DEFAULT NULL,
  `thread_count` int NULL DEFAULT NULL,
  `temperature` double NULL DEFAULT NULL,
  `online` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `collect_time` datetime(6) NULL DEFAULT NULL,
  `extra_data` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '设备数据' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `file_storage`;
CREATE TABLE `file_storage`  (
  `file_storage_id` int NOT NULL AUTO_INCREMENT,
  `file_storage_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '文件存储路径',
  `file_storage_desc` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '存储名称',
  `file_storage_type` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '实现类型',
  `file_storage_user` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '账号',
  `file_storage_password` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '密码',
  `file_storage_icon` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '图表',
  `file_storage_endpoint` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '远程访问端点',
  `file_storage_bucket` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT 'bucket',
  `file_storage_protocol_id` int NULL DEFAULT NULL COMMENT '服务器协议, ',
  `file_storage_status` int NULL DEFAULT NULL COMMENT '状态,0:停用',
  `file_storage_cookie` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL COMMENT 'cookie',
  PRIMARY KEY (`file_storage_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_bin COMMENT = '文件存储' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `file_storage_log`;
CREATE TABLE `file_storage_log`  (
  `file_storage_log_id` int NOT NULL AUTO_INCREMENT,
  `file_storage_id` int NULL DEFAULT NULL,
  `file_storage_log_address` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '请求客户端地址',
  `file_storage_log_url` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '请求文件地址',
  PRIMARY KEY (`file_storage_log_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_bin COMMENT = '文件存储日志' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `file_storage_protocol`;
CREATE TABLE `file_storage_protocol`  (
  `file_storage_protocol_id` int NOT NULL AUTO_INCREMENT,
  `file_storage_protocol_port` int NULL DEFAULT NULL COMMENT '开启远程服务器端口',
  `file_storage_protocol_desc` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '服务器说明',
  `file_storage_protocol_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '服务器协议, HTTP',
  `file_storage_protocol_host` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '开启远程服务器主机',
  `file_storage_protocol_status` int NULL DEFAULT NULL COMMENT '远程服务器状态, 0:未开启',
  `file_storage_protocol_plugins` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '插件列表，多个逗号分隔',
  `file_storage_protocol_setting` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '配置列表，多个逗号分隔',
  `file_storage_protocol_plugin_open` int NULL DEFAULT NULL COMMENT '是否开启插件；0:不开启',
  `file_storage_protocol_setting_open` int NULL DEFAULT NULL COMMENT '是否开启配置；0:不开启',
  `file_storage_protocol_rang_open` int NULL DEFAULT NULL COMMENT '是否开启Range；0:不开启',
  `file_storage_protocol_ua_open` int NULL DEFAULT NULL COMMENT '是否开启UA；0:不开启',
  `file_storage_protocol_download_ua` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT 'UA',
  `file_storage_protocol_ua` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT 'UA',
  `file_storage_protocol_preview_or_download` int NULL DEFAULT NULL COMMENT '0: 全部支持,1:预览, 2:下载',
  `file_storage_protocol_watermark_open` int NULL DEFAULT NULL COMMENT '是否开启水印；0:不开启, 1: 下载水印,2:预览水印,3: 两者',
  `file_storage_protocol_transfer_cache_open` int NULL DEFAULT NULL COMMENT '是否转化数据缓存；0:不开启',
  `file_storage_protocol_transfer_cache_time` int NULL DEFAULT NULL COMMENT '转化数据缓存时间(s)',
  `file_storage_protocol_transfer_cache_path` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '转化数据缓存路径',
  `file_storage_protocol_remote_open` int NULL DEFAULT NULL COMMENT '是否开启远程服务器；0:不开启',
  `file_storage_protocol_watermark_content` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '水印内容',
  `file_storage_protocol_watermark_way` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '水印放置方式, NORMAL: 正常, TILE:平铺',
  `file_storage_protocol_watermark_x` int NULL DEFAULT NULL COMMENT '水印x轴位置',
  `file_storage_protocol_watermark_y` int NULL DEFAULT NULL COMMENT '水印y轴位置',
  `file_storage_protocol_watermark_height` int NULL DEFAULT NULL COMMENT '水印高度',
  `file_storage_protocol_watermark_width` int NULL DEFAULT NULL COMMENT '水印宽度',
  `file_storage_protocol_watermark_alpha` int NULL DEFAULT NULL COMMENT '水印alpha',
  `file_storage_protocol_watermark_color` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '水印颜色',
  PRIMARY KEY (`file_storage_protocol_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_bin COMMENT = '文件存储服务器协议' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `file_system`;
CREATE TABLE `file_system`  (
  `file_system_id` int NOT NULL AUTO_INCREMENT COMMENT '文件ID',
  `file_system_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件名称',
  `file_system_original_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件原始名称',
  `file_system_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件路径',
  `file_system_size` bigint NULL DEFAULT NULL COMMENT '文件大小(字节)',
  `file_system_md5` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件MD5值',
  `file_system_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件类型',
  `file_system_extension` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件扩展名',
  `file_system_chunk_total` int NULL DEFAULT NULL COMMENT '分片总数',
  `file_system_chunk_uploaded` int NULL DEFAULT NULL COMMENT '已上传分片数',
  `file_system_status` int NULL DEFAULT NULL COMMENT '文件状态：0-待合并，1-合并中，2-已完成，3-合并失败',
  `file_system_http_access_enabled` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否启用HTTP访问',
  `file_system_http_access_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'HTTP访问URL',
  `file_system_http_service_id` int NULL DEFAULT NULL COMMENT '绑定的服务ID',
  `file_system_upload_start_time` datetime(6) NULL DEFAULT NULL COMMENT '上传开始时间',
  `file_system_upload_complete_time` datetime(6) NULL DEFAULT NULL COMMENT '上传完成时间',
  `file_system_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `file_system_group_id` int NULL DEFAULT NULL COMMENT '文件分组ID',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`file_system_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文件系统' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `file_system_group`;
CREATE TABLE `file_system_group`  (
  `file_system_group_id` int NOT NULL AUTO_INCREMENT COMMENT '分组ID',
  `file_system_group_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组名称',
  `file_system_group_parent_id` int NULL DEFAULT NULL COMMENT '父分组ID',
  `file_system_group_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组路径',
  `file_system_group_full_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组完整路径',
  `file_system_group_level` int NULL DEFAULT NULL COMMENT '分组层级',
  `file_system_group_tree_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组树路径',
  `file_system_group_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组描述',
  `file_system_group_is_default` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否默认分组',
  `file_system_group_sort` int NULL DEFAULT NULL COMMENT '分组排序',
  `file_system_group_status` int NULL DEFAULT NULL COMMENT '分组状态',
  `file_system_group_icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组图标',
  `file_system_group_color` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组颜色',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`file_system_group_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `file_system_part`;
CREATE TABLE `file_system_part`  (
  `file_system_part_id` int NOT NULL AUTO_INCREMENT COMMENT '分片ID',
  `file_system_part_file_id` int NULL DEFAULT NULL COMMENT '文件ID',
  `file_system_part_chunk_number` int NULL DEFAULT NULL COMMENT '分片序号(从0开始)',
  `file_system_part_chunk_size` bigint NULL DEFAULT NULL COMMENT '分片大小(字节)',
  `file_system_part_chunk_md5` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分片MD5值',
  `file_system_part_chunk_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分片文件路径',
  `file_system_part_chunk_status` int NULL DEFAULT NULL COMMENT '分片状态：0-待处理，1-处理中，2-已完成，3-处理失败',
  `file_system_part_upload_time` datetime(6) NULL DEFAULT NULL COMMENT '上传时间',
  `file_system_part_process_time` datetime(6) NULL DEFAULT NULL COMMENT '处理时间',
  `file_system_part_error_message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误信息',
  `file_system_part_retry_count` int NULL DEFAULT NULL COMMENT '重试次数',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`file_system_part_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `file_system_setting`;
CREATE TABLE `file_system_setting`  (
  `file_system_setting_id` int NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `file_system_setting_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置名称',
  `file_system_setting_chunk_upload_enabled` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否启用分片上传',
  `file_system_setting_chunk_size_mb` int NULL DEFAULT NULL COMMENT '分片大小(MB)',
  `file_system_setting_merge_task_limit` int NULL DEFAULT NULL COMMENT '合并任务数量限制',
  `file_system_setting_manual_merge_enabled` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否支持手动合并功能',
  `file_system_setting_download_enabled` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否启用文件下载',
  `file_system_setting_http_access_enabled` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否启用HTTP访问',
  `file_system_setting_http_access_domain` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'HTTP访问域名',
  `file_system_setting_allowed_file_types` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件类型白名单(逗号分隔)',
  `file_system_setting_max_file_size_mb` int NULL DEFAULT NULL COMMENT '单个文件最大大小(MB)',
  `file_system_setting_storage_root_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件存储根路径',
  `file_system_setting_temp_storage_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '临时文件存储路径',
  `file_system_setting_file_retention_days` int NULL DEFAULT NULL COMMENT '文件保留天数(0表示永久保留)',
  `file_system_setting_enabled` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否启用',
  `file_system_setting_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`file_system_setting_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_acme_account`;
CREATE TABLE `monitor_acme_account`  (
  `acme_account_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `acme_account_email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账号邮箱',
  `acme_account_server` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'ACME 服务端点',
  `acme_account_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账户状态',
  `acme_account_private_key_pem` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账户私钥（PEM）',
  `acme_account_eab_kid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'EAB Key ID (ZeroSSL 等需要)',
  `acme_account_eab_hmac_key` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'EAB HMAC Key (Base64 编码)',
  `acme_account_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`acme_account_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

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
  `acme_cert_chain_pem` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '证书PEM（含链）',
  `acme_cert_key_pem` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '私钥PEM',
  `acme_cert_last_error` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最近错误',
  `acme_cert_deploy_mode` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'manual' COMMENT '部署模式：local(本机自动)/manual(手动)',
  `acme_cert_webroot_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '本机模式webroot路径',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`acme_cert_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_acme_order`;
CREATE TABLE `monitor_acme_order`  (
  `acme_order_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `acme_account_id` int NULL DEFAULT NULL COMMENT '账户ID',
  `acme_cert_id` int NULL DEFAULT NULL COMMENT '证书ID',
  `acme_order_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '订单URL',
  `acme_authz_urls` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '授权URL列表(JSON)',
  `acme_order_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '状态',
  `acme_certificate_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最终证书URL',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`acme_order_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_application`;
CREATE TABLE `monitor_application`  (
  `monitor_id` int NOT NULL AUTO_INCREMENT,
  `monitor_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '监控名称',
  `monitor_application_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属应用',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  `monitor_prometheus_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Prometheus地址',
  PRIMARY KEY (`monitor_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '应用管理' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_arthas_session`;
CREATE TABLE `monitor_arthas_session`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `consumer_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `job_id` int NULL DEFAULT NULL,
  `node_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `http_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `command` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `command_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `update_time` datetime(6) NULL DEFAULT NULL,
  `expire_time` datetime(6) NULL DEFAULT NULL,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_arthas_tunnel_config`;
CREATE TABLE `monitor_arthas_tunnel_config`  (
  `monitor_arthas_tunnel_config_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `monitor_arthas_tunnel_config_server_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务器ID',
  `monitor_arthas_tunnel_config_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Tunnel 地址（例如 ws://host:port/ws）',
  `monitor_arthas_tunnel_config_http` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'HTTP 请求地址（可选），用于 HTTP 方式连接 Arthas',
  `monitor_arthas_tunnel_config_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '认证用户名（可选）',
  `monitor_arthas_tunnel_config_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '认证密码（可选）',
  PRIMARY KEY (`monitor_arthas_tunnel_config_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_job`;
CREATE TABLE `monitor_job`  (
  `job_id` int NOT NULL AUTO_INCREMENT,
  `job_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `job_author` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '负责人',
  `job_alarm_email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '报警邮件，多个邮件地址则逗号分隔',
  `job_schedule_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务类型, cron, fixed',
  `job_schedule_time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置时间',
  `job_trigger_status` int NULL DEFAULT 0 COMMENT '状态,0: 未启用； 1: 正在运行',
  `monitor_id` int NULL DEFAULT NULL COMMENT '对应的应用ID',
  `job_application_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '对应的应用名称',
  `job_application_active` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '环境',
  `job_execute_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模式, spring, xxl-job',
  `job_execute_bean` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'bean名称',
  `job_execute_route` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执行路由策略',
  `job_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `job_glue_updatetime` date NULL DEFAULT NULL COMMENT 'Glue最后更新时间',
  `job_glue_source` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '源码',
  `job_glue_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Glue类型（例如：脚本、JAR等）',
  `job_fail_retry` int NULL DEFAULT NULL COMMENT '失败重试次数',
  `job_execute_timeout` int NULL DEFAULT NULL COMMENT '超时时间',
  `job_execute_misfire_strategy` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '失效后策略',
  `job_execute_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执行参数',
  `job_execute_router` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执行路由策略',
  `job_trigger_last_time` bigint NULL DEFAULT NULL COMMENT '任务触发的最后一次时间',
  `job_trigger_next_time` bigint NULL DEFAULT NULL COMMENT '任务触发的下一次时间',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`job_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '调度任务' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_job_log`;
CREATE TABLE `monitor_job_log`  (
  `job_log_id` int NOT NULL AUTO_INCREMENT,
  `job_log_trigger_date` date NULL DEFAULT NULL COMMENT '触发时间',
  `job_log_trigger_time` datetime NULL DEFAULT NULL COMMENT '触发时间',
  `job_log_trigger_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '触发状态',
  `job_log_execute_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户端执行任务的结果',
  `job_log_trigger_msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '触发消息',
  `job_log_app` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '触发应用',
  `job_log_trigger_address` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '触发地址',
  `job_log_trigger_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '触发参数',
  `job_log_trigger_bean` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执行对应的标识',
  `job_log_profile` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '环境',
  `job_log_trigger_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '触发类型',
  `job_log_cost` decimal(10, 2) NULL DEFAULT NULL COMMENT '耗时',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`job_log_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1670 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '调度任务日志' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_log`;
CREATE TABLE `monitor_log`  (
  `log_id` int NOT NULL AUTO_INCREMENT,
  `log_module_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模块',
  `log_command_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '命令',
  `log_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '内容',
  `log_appname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '应用',
  `log_profile` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '环境',
  `log_host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地址',
  `log_port` int NULL DEFAULT NULL COMMENT '端口',
  `log_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '响应码',
  `log_msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误消息',
  `tenant_id` int NULL DEFAULT 0 COMMENT '租户ID',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  `sys_tenant_id` int NULL DEFAULT 0 COMMENT '租户ID',
  PRIMARY KEY (`log_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1653 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统日志' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_mqtt_server`;
CREATE TABLE `monitor_mqtt_server`  (
  `monitor_mqtt_server_id` int NOT NULL AUTO_INCREMENT,
  `monitor_mqtt_server_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `monitor_mqtt_server_host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地址',
  `monitor_mqtt_server_port` int NULL DEFAULT NULL COMMENT '端口',
  `monitor_mqtt_server_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账号',
  `monitor_mqtt_server_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
  `monitor_mqtt_server_version` int NULL DEFAULT NULL COMMENT '版本号',
  `monitor_mqtt_server_status` int NULL DEFAULT NULL COMMENT '是否启动,0:未启动',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_mqtt_server_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_nginx_config`;
CREATE TABLE `monitor_nginx_config`  (
  `monitor_nginx_config_id` int NOT NULL AUTO_INCREMENT,
  `monitor_nginx_config_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置名称',
  `monitor_nginx_config_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置文件路径',
  `monitor_nginx_config_worker_processes` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '#允许进程数量，\\n建议设置为cpu核心数或者auto自动检测，注意Windows服务器上虽然可以启动多个processes，但是实际只会用其中一个worker_processes  auto,',
  `monitor_nginx_config_access_log` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'access_log日志目录',
  `monitor_nginx_config_error_log` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '#error日志存放位置\\n error_log  logs/error.log, \\n#error_log  logs/error.log  notice, \\n#error_log  logs/error.log  info,',
  `monitor_nginx_config_pid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '#pid        logs/nginx.pid,#工作模式及连接数上限',
  `monitor_nginx_config_multipart` int NULL DEFAULT NULL COMMENT '是否多文件存储',
  `monitor_nginx_config_nginx_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'nginx脚本路径；服务方式无需配置',
  `monitor_nginx_config_type` int NULL DEFAULT NULL COMMENT '配置类型,0:文件；1:服务',
  `monitor_nginx_config_service_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务名称',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  `monitor_nginx_config_run_user` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '运行用户',
  PRIMARY KEY (`monitor_nginx_config_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'nginx配置项' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_nginx_event`;
CREATE TABLE `monitor_nginx_event`  (
  `monitor_nginx_event_id` int NOT NULL AUTO_INCREMENT,
  `monitor_nginx_event_worker_connections` int NULL DEFAULT NULL COMMENT '工作连接数上限',
  `monitor_nginx_event_use` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '事件模型',
  `monitor_nginx_event_multi_accept` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否允许多连接',
  `monitor_nginx_event_accept_mutex` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '接受互斥锁',
  `monitor_nginx_config_id` int NULL DEFAULT NULL COMMENT '所属配置',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_nginx_event_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'nginx事件' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_nginx_http`;
CREATE TABLE `monitor_nginx_http`  (
  `monitor_nginx_http_id` int NOT NULL AUTO_INCREMENT,
  `monitor_nginx_http_keepalive_timeout` int NULL DEFAULT 65 COMMENT '长连接超时时间(秒)',
  `monitor_nginx_http_sendfile` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'on' COMMENT '高效文件传输模式',
  `monitor_nginx_http_access_log` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '访问日志路径',
  `monitor_nginx_http_server_names_hash_bucket_size` int NULL DEFAULT 128 COMMENT '服务器名hash表大小',
  `monitor_nginx_http_server_tokens` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'off' COMMENT '隐藏nginx版本号',
  `monitor_nginx_http_ignore_invalid_headers` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'on' COMMENT '忽略不合法请求头',
  `monitor_nginx_http_server_name_in_redirect` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'off' COMMENT '重定向时使用server_name',
  `monitor_nginx_http_client_body_buffer_size` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '8m' COMMENT '客户端请求体大小',
  `monitor_nginx_http_tcp_nopush` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'on' COMMENT 'TCP推送设置',
  `monitor_nginx_http_tcp_nodelay` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'TCP延迟设置',
  `monitor_nginx_http_error_log` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误日志',
  `monitor_nginx_http_log_format` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '自定义日志格式',
  `monitor_nginx_http_charset` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '编码',
  `monitor_nginx_http_gzip` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'on' COMMENT 'gzip压缩开关',
  `monitor_nginx_http_gzip_disable` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '禁用gzip压缩',
  `monitor_nginx_http_gzip_min_length` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最小压缩大小',
  `monitor_nginx_http_gzip_buffers` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '压缩缓冲区',
  `monitor_nginx_http_gzip_http_version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '压缩版本',
  `monitor_nginx_http_gzip_comp_level` int NULL DEFAULT NULL COMMENT '压缩等级',
  `monitor_nginx_http_gzip_vary` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'gzip vary头开关',
  `monitor_nginx_http_gzip_types` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '压缩类型',
  `monitor_nginx_http_ssl_certificate` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SSL证书路径',
  `monitor_nginx_http_ssl_certificate_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SSL私钥路径',
  `monitor_nginx_http_ssl_protocols` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SSL协议',
  `monitor_nginx_http_ssl_session_timeout` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SSL会话超时',
  `monitor_nginx_http_ssl_ciphers` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SSL加密算法',
  `monitor_nginx_http_ssl_prefer_server_ciphers` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务器加密优先',
  `monitor_nginx_http_include` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '包含的配置文件',
  `monitor_nginx_http_limit_req_zone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '限流配置',
  `monitor_nginx_http_proxy_cache_path` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '缓存路径配置',
  `monitor_nginx_config_id` int NULL DEFAULT NULL COMMENT '所属配置',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  `monitor_nginx_http_log_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志名称',
  PRIMARY KEY (`monitor_nginx_http_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_nginx_http_server`;
CREATE TABLE `monitor_nginx_http_server`  (
  `monitor_nginx_http_server_id` int NOT NULL AUTO_INCREMENT,
  `monitor_nginx_http_id` int NULL DEFAULT NULL COMMENT 'HTTP配置ID',
  `monitor_nginx_http_server_port` int NULL DEFAULT NULL COMMENT '监听端口',
  `monitor_nginx_http_server_root` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '根目录',
  `monitor_nginx_http_server_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务器名称',
  `monitor_nginx_http_server_charset` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '编码',
  `monitor_nginx_http_server_access_log` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '访问日志',
  `monitor_nginx_http_server_return` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '重定向配置',
  `monitor_nginx_http_server_ssl` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SSL端口',
  `monitor_nginx_http_server_udp` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'UDP端口',
  `monitor_nginx_http_server_error_page` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误页面',
  `monitor_nginx_http_server_ssl_certificate` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SSL证书',
  `monitor_nginx_http_server_ssl_certificate_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SSL证书密钥',
  `monitor_nginx_http_server_ssl_session_timeout` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SSL会话超时',
  `monitor_nginx_http_server_ssl_ciphers` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SSL加密算法',
  `monitor_nginx_http_server_ssl_protocols` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SSL协议',
  `monitor_nginx_http_server_ssl_prefer_server_ciphers` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SSL加密优先级',
  `monitor_nginx_http_server_proxy_read_timeout` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '代理读取超时',
  `monitor_nginx_http_server_proxy_send_timeout` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '代理发送超时',
  `monitor_nginx_http_server_proxy_connect_timeout` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '代理连接超时',
  `monitor_nginx_http_server_client_max_body_size` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户端最大请求体',
  `monitor_nginx_http_server_client_body_buffer_size` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户端请求体缓冲',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_nginx_http_server_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 44 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'nginx http服务配置' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_nginx_http_server_location`;
CREATE TABLE `monitor_nginx_http_server_location`  (
  `monitor_nginx_http_server_location_id` int NOT NULL AUTO_INCREMENT,
  `monitor_nginx_http_server_id` int NULL DEFAULT NULL COMMENT '服务器配置ID',
  `monitor_nginx_http_server_location_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地址名称',
  `monitor_nginx_http_server_location_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `monitor_nginx_http_server_location_proxy_pass` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '代理地址',
  `monitor_nginx_http_server_location_proxy_cache` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '代理缓存',
  `monitor_nginx_http_server_location_proxy_cache_valid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '缓存有效期',
  `monitor_nginx_http_server_location_proxy_cache_methods` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '缓存方法',
  `monitor_nginx_http_server_location_client_max_body_size` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户端最大请求体',
  `monitor_nginx_http_server_location_client_body_buffer_size` double NULL DEFAULT NULL COMMENT '客户端请求体缓冲',
  `monitor_nginx_http_server_location_proxy_read_timeout` int NULL DEFAULT NULL COMMENT '代理读取超时',
  `monitor_nginx_http_server_location_proxy_send_timeout` int NULL DEFAULT NULL COMMENT '代理发送超时',
  `monitor_nginx_http_server_location_proxy_connect_timeout` int NULL DEFAULT NULL COMMENT '代理连接超时',
  `monitor_nginx_http_server_location_root` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '根目录',
  `monitor_nginx_http_server_location_index` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '首页索引文件',
  `monitor_nginx_http_server_location_try_files` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'try_files配置',
  `monitor_nginx_http_server_location_condition` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'location条件配置',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_nginx_http_server_location_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'nginx location配置' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_nginx_http_server_location_header`;
CREATE TABLE `monitor_nginx_http_server_location_header`  (
  `monitor_nginx_http_server_location_header_id` int NOT NULL AUTO_INCREMENT,
  `monitor_nginx_http_server_location_id` int NULL DEFAULT NULL COMMENT 'monitor_nginx_http_server_location表ID',
  `monitor_nginx_http_server_location_header_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `monitor_nginx_http_server_location_header_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '值',
  `monitor_nginx_http_server_location_header_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '类型, add, set',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_nginx_http_server_location_header_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 517 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'nginx消息头' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_nginx_upstream`;
CREATE TABLE `monitor_nginx_upstream`  (
  `monitor_nginx_upstream_id` int NOT NULL AUTO_INCREMENT,
  `monitor_nginx_upstream_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `monitor_nginx_upstream_server` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务',
  `monitor_nginx_upstream_max_fails` int NULL DEFAULT NULL COMMENT '失败次数',
  `monitor_nginx_upstream_weight` int NULL DEFAULT NULL COMMENT '权重',
  `monitor_nginx_upstream_type` int NULL DEFAULT NULL COMMENT '0:http',
  `monitor_nginx_upstream_mode` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '负载均衡模式',
  `monitor_nginx_upstream_udp` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'UDP服务配置',
  `monitor_nginx_server_id` int NULL DEFAULT NULL COMMENT '所属服务',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_nginx_upstream_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'nginx负载均衡配置' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_protection`;
CREATE TABLE `monitor_protection`  (
  `monitor_protection_id` int NOT NULL AUTO_INCREMENT,
  `monitor_protection_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `monitor_protection_pid` int NULL DEFAULT NULL COMMENT '监听的PID',
  `monitor_protection_status` int NULL DEFAULT NULL COMMENT '是否禁用, 0:启用',
  `monitor_protection_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `monitor_protection_shell` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '启动脚本',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_protection_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_proxy`;
CREATE TABLE `monitor_proxy`  (
  `proxy_id` int NOT NULL AUTO_INCREMENT,
  `proxy_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `proxy_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `proxy_host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0.0.0.0' COMMENT '启动地址',
  `proxy_port` int NULL DEFAULT NULL COMMENT '端口',
  `proxy_protocol` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '协议',
  `proxy_status` int NULL DEFAULT NULL COMMENT '0: 未开启',
  `proxy_version` int NULL DEFAULT NULL COMMENT '版本',
  PRIMARY KEY (`proxy_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '代理管理' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_proxy_log`;
CREATE TABLE `monitor_proxy_log`  (
  `monitor_proxy_log_id` int NOT NULL AUTO_INCREMENT,
  `monitor_proxy_log_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '类型； LIMIT, WHITE, BLACK',
  `monitor_proxy_log_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误码',
  `monitor_proxy_log_cost` decimal(10, 2) NULL DEFAULT NULL COMMENT '耗时',
  `monitor_proxy_log_msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '原因',
  `monitor_proxy_log_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地址',
  `monitor_proxy_log_server_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务 ID',
  `monitor_proxy_log_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户端地址',
  `monitor_proxy_log_address_geo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户端地址',
  `monitor_proxy_log_date` int NULL DEFAULT NULL COMMENT '发生时间(day)',
  `monitor_proxy_log_hour` int NULL DEFAULT NULL COMMENT '发生时间(hour)',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  `monitor_proxy_log_address_country` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户端地址地理位置（国家）',
  `monitor_proxy_log_address_province` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户端地址地理位置（省份）',
  `monitor_proxy_log_qps` bigint NULL DEFAULT NULL COMMENT 'QPS值',
  `monitor_proxy_log_browser` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '浏览器类型',
  `monitor_proxy_log_os` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作系统',
  `monitor_proxy_log_device` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备类型',
  `monitor_proxy_log_fingerprint` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '浏览器指纹',
  `monitor_proxy_log_user_agent` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'User-Agent',
  PRIMARY KEY (`monitor_proxy_log_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 108 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '代理日志' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_proxy_plugin`;
CREATE TABLE `monitor_proxy_plugin`  (
  `proxy_plugin_id` int NOT NULL AUTO_INCREMENT,
  `proxy_plugin_spi` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件实现',
  `proxy_id` int NULL DEFAULT NULL COMMENT '代理ID',
  `proxy_plugin_sort` int NULL DEFAULT NULL COMMENT '优先级',
  `proxy_plugin_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '插件名称',
  PRIMARY KEY (`proxy_plugin_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '代理-插件关系表' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_proxy_plugin_config`;
CREATE TABLE `monitor_proxy_plugin_config`  (
  `proxy_config_id` int NOT NULL AUTO_INCREMENT,
  `proxy_config_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置名称',
  `proxy_config_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置值',
  `proxy_plugin_id` int NULL DEFAULT NULL COMMENT '插件ID',
  `proxy_id` int NULL DEFAULT NULL COMMENT '代理ID',
  PRIMARY KEY (`proxy_config_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '代理 - 组件- 配置' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_proxy_plugin_limit`;
CREATE TABLE `monitor_proxy_plugin_limit`  (
  `proxy_config_limit_id` int NOT NULL AUTO_INCREMENT,
  `proxy_config_limit_path_or_ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '限流路径',
  `proxy_config_limit_per_seconds` int NULL DEFAULT NULL COMMENT '每秒限流次数',
  `proxy_config_limit_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '类型,  PATH, IP',
  `proxy_config_limit_disabled` int NULL DEFAULT NULL COMMENT '是否禁用；0: 启用',
  `proxy_plugin_id` int NULL DEFAULT NULL COMMENT '插件ID',
  `proxy_id` int NULL DEFAULT NULL COMMENT '代理ID',
  PRIMARY KEY (`proxy_config_limit_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '代理 - 组件- 限流' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_proxy_plugin_list`;
CREATE TABLE `monitor_proxy_plugin_list`  (
  `proxy_config_list_id` int NOT NULL AUTO_INCREMENT,
  `proxy_config_list` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '黑白名单',
  `proxy_config_list_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '类型,  WHITE, BLACK',
  `proxy_config_list_disabled` int NULL DEFAULT NULL COMMENT '是否禁用；0: 启用',
  `proxy_plugin_id` int NULL DEFAULT NULL COMMENT '插件ID',
  `proxy_id` int NULL DEFAULT NULL COMMENT '代理ID',
  PRIMARY KEY (`proxy_config_list_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '黑白名单' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_proxy_plugin_statistic_service_discovery`;
CREATE TABLE `monitor_proxy_plugin_statistic_service_discovery`  (
  `proxy_statistic_id` int NOT NULL AUTO_INCREMENT,
  `proxy_statistic_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '名称说明',
  `proxy_statistic_url` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '地址前缀',
  `proxy_statistic_protocol` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '协议',
  `proxy_statistic_hostname` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '地址',
  `proxy_statistic_weight` int NULL DEFAULT NULL COMMENT '权重',
  `proxy_id` int NULL DEFAULT NULL COMMENT '代理ID',
  `proxy_statistic_status` int NULL DEFAULT NULL COMMENT '状态,0:禁用',
  PRIMARY KEY (`proxy_statistic_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_bin COMMENT = '静态IP' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen`;
CREATE TABLE `monitor_sys_gen`  (
  `gen_id` int NOT NULL AUTO_INCREMENT,
  `gen_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据库名称说明',
  `gen_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据库类型',
  `gen_user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据库用户名',
  `gen_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据库密码',
  `gen_host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据库地址',
  `gen_port` int NULL DEFAULT NULL COMMENT '数据库端口',
  `gen_timeout` int NULL DEFAULT NULL COMMENT '超时时间',
  `gen_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据库说明',
  `gen_driver` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据库驱动地址',
  `gen_driver_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据库驱动下载地址',
  `gen_database` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据库名称',
  `gen_database_file` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件数据库数据目录',
  `gen_uid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件数据库ID',
  `gen_backup_status` int NULL DEFAULT NULL COMMENT '备份状态,0:未开启',
  `gen_jdbc_custom_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT 'JDBC类型',
  `gen_jdbc_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT 'JDBC类型',
  `gen_driver_file` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '驱动文件路径',
  `gen_driver_remote_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '驱动文件下载目录',
  `gen_backup_period` int NULL DEFAULT 720 COMMENT '备份时间分钟(默认720)',
  `gen_backup_event` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备份事件',
  `gen_spilt` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '是否分屏',
  PRIMARY KEY (`gen_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '代码生成器管理' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_alert_config`;
CREATE TABLE `monitor_sys_gen_alert_config`  (
  `monitor_sys_gen_alert_config_id` int NOT NULL AUTO_INCREMENT COMMENT '告警配置ID',
  `monitor_sys_gen_alert_config_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置名称',
  `monitor_sys_gen_alert_config_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置描述',
  `monitor_sys_gen_alert_config_enabled` int NULL DEFAULT NULL COMMENT '是否启用',
  `monitor_sys_gen_alert_config_is_default` int NULL DEFAULT NULL COMMENT '是否为默认配置',
  `monitor_sys_gen_alert_config_cpu_threshold` decimal(11, 2) NULL DEFAULT NULL COMMENT 'CPU告警阈值 (百分比)',
  `monitor_sys_gen_alert_config_cpu_critical_threshold` decimal(11, 2) NULL DEFAULT NULL COMMENT 'CPU严重告警阈值 (百分比)',
  `monitor_sys_gen_alert_config_memory_threshold` decimal(11, 2) NULL DEFAULT NULL COMMENT '内存告警阈值 (百分比)',
  `monitor_sys_gen_alert_config_memory_critical_threshold` decimal(11, 2) NULL DEFAULT NULL COMMENT '内存严重告警阈值 (百分比)',
  `monitor_sys_gen_alert_config_disk_threshold` decimal(11, 2) NULL DEFAULT NULL COMMENT '磁盘告警阈值 (百分比)',
  `monitor_sys_gen_alert_config_disk_critical_threshold` decimal(11, 2) NULL DEFAULT NULL COMMENT '磁盘严重告警阈值 (百分比)',
  `monitor_sys_gen_alert_config_network_threshold` decimal(11, 2) NULL DEFAULT NULL COMMENT '网络告警阈值 (Mbps)',
  `monitor_sys_gen_alert_config_network_critical_threshold` decimal(11, 2) NULL DEFAULT NULL COMMENT '网络严重告警阈值 (Mbps)',
  `monitor_sys_gen_alert_config_response_time_threshold` bigint NULL DEFAULT NULL COMMENT '响应时间告警阈值 (毫秒)',
  `monitor_sys_gen_alert_config_response_time_critical_threshold` bigint NULL DEFAULT NULL COMMENT '响应时间严重告警阈值 (毫秒)',
  `monitor_sys_gen_alert_config_latency_threshold` bigint NULL DEFAULT NULL COMMENT '延迟告警阈值 (毫秒)',
  `monitor_sys_gen_alert_config_latency_critical_threshold` bigint NULL DEFAULT NULL COMMENT '延迟严重告警阈值 (毫秒)',
  `monitor_sys_gen_alert_config_temperature_threshold` decimal(11, 2) NULL DEFAULT NULL COMMENT '温度告警阈值 (摄氏度)',
  `monitor_sys_gen_alert_config_temperature_critical_threshold` decimal(11, 2) NULL DEFAULT NULL COMMENT '温度严重告警阈值 (摄氏度)',
  `monitor_sys_gen_alert_config_silence_duration` int NULL DEFAULT NULL COMMENT '告警静默时长 (分钟)',
  `monitor_sys_gen_alert_config_detection_interval` int NULL DEFAULT NULL COMMENT '告警检测间隔 (秒)',
  `monitor_sys_gen_alert_config_recovery_notification_enabled` int NULL DEFAULT NULL COMMENT '是否启用恢复通知',
  `monitor_sys_gen_alert_config_notification_method` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '告警通知方式',
  `monitor_sys_gen_alert_config_notification_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '告警通知地址',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_alert_config_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_alert_push_config`;
CREATE TABLE `monitor_sys_gen_alert_push_config`  (
  `monitor_sys_gen_alert_push_config_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `monitor_sys_gen_server_id` int NULL DEFAULT NULL COMMENT '服务器ID，空或0表示全局',
  `monitor_sys_gen_alert_push_config_type` varchar(11) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '告警类型',
  `monitor_sys_gen_alert_push_config_channel` varchar(11) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '通知通道（EMAIL/SMS/WEBHOOK/DINGTALK/WECHAT 等）',
  `monitor_sys_gen_alert_push_config_template_id` int NULL DEFAULT NULL COMMENT '模板ID（优先于本条配置项）',
  `monitor_sys_gen_alert_push_config_enabled` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否启用',
  `monitor_sys_gen_alert_push_config_endpoint` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标地址（Webhook URL、邮箱、手机号等）',
  `monitor_sys_gen_alert_push_config_main_account` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '主账号',
  `monitor_sys_gen_alert_push_config_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名',
  `monitor_sys_gen_alert_push_config_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
  `monitor_sys_gen_alert_push_config_token` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '令牌/密钥',
  `monitor_sys_gen_alert_push_config_extra` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '扩展参数（JSON）',
  `monitor_sys_gen_alert_push_config_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_alert_push_config_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_backup`;
CREATE TABLE `monitor_sys_gen_backup`  (
  `monitor_sys_gen_backup_id` int NOT NULL AUTO_INCREMENT,
  `monitor_sys_gen_backup_day` int NULL DEFAULT NULL COMMENT '多少天备份一次',
  `monitor_sys_gen_backup_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备份地址',
  `monitor_sys_gen_backup_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备份结果',
  `monitor_sys_gen_backup_retry` int NULL DEFAULT NULL COMMENT '失败后重试次数',
  `monitor_sys_gen_backup_enable` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否开启',
  `monitor_sys_gen_backup_timeout` int NULL DEFAULT NULL COMMENT '超时时间(min), 默认30分钟',
  `monitor_sys_gen_backup_retention_days` int NULL DEFAULT NULL COMMENT '保存天数(day), 默认90天',
  `monitor_sys_gen_id` int NULL DEFAULT NULL COMMENT '数据源ID',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_backup_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_backup_log`;
CREATE TABLE `monitor_sys_gen_backup_log`  (
  `monitor_sys_gen_backup_log_id` int NOT NULL AUTO_INCREMENT,
  `monitor_sys_gen_backup_log_time` datetime(6) NULL DEFAULT NULL COMMENT '备份时间',
  `monitor_sys_gen_backup_log_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备份状态',
  `monitor_sys_gen_backup_log_day` date NULL DEFAULT NULL COMMENT '备份日期',
  `monitor_sys_gen_backup_log_error_message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误原因',
  `monitor_sys_gen_backup_id` int NULL DEFAULT NULL COMMENT '备份ID',
  `monitor_sys_gen_id` int NULL DEFAULT NULL COMMENT '数据源ID',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_backup_log_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_column`;
CREATE TABLE `monitor_sys_gen_column`  (
  `col_id` int NOT NULL AUTO_INCREMENT,
  `tab_id` int NULL DEFAULT NULL COMMENT '表ID',
  `col_column_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '列名称',
  `col_column_comment` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '列描述',
  `col_column_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '列类型',
  `col_column_length` int NULL DEFAULT NULL COMMENT '列长度',
  `col_column_decimal` int NULL DEFAULT NULL COMMENT '小数点长度',
  `col_java_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'JAVA类型',
  `col_java_field` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'JAVA字段名',
  `col_is_pk` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否主键（1是）',
  `col_is_increment` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否自增（1是）',
  `col_is_required` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否必填（1是）',
  `col_is_insert` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否为插入字段（1是）',
  `col_is_edit` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否编辑字段（1是）',
  `col_is_list` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否列表字段（1是）',
  `col_is_query` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否查询字段（1是）',
  `col_query_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '查询方式（等于、不等于、大于、小于、范围）',
  `col_html_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）',
  `col_dict_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典类型',
  `col_sort` int NULL DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`col_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 52 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '生成器字段' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_file_system`;
CREATE TABLE `monitor_sys_gen_file_system`  (
  `monitor_sys_gen_file_system_id` int NOT NULL AUTO_INCREMENT COMMENT '文件系统ID',
  `monitor_sys_gen_file_system_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件名称',
  `monitor_sys_gen_file_system_original_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件原始名称',
  `monitor_sys_gen_file_system_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件路径',
  `monitor_sys_gen_file_system_size` bigint NULL DEFAULT NULL COMMENT '文件大小(字节)',
  `monitor_sys_gen_file_system_md5` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件MD5',
  `monitor_sys_gen_file_system_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件类型',
  `monitor_sys_gen_file_system_extension` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件扩展名',
  `monitor_sys_gen_file_system_status` int NULL DEFAULT NULL COMMENT '文件状态 0:待合并 1:正常 2:合并失败 3:已删除',
  `monitor_sys_gen_file_system_is_chunk` int NULL DEFAULT NULL COMMENT '是否分片上传 0:否 1:是',
  `monitor_sys_gen_file_system_chunk_total` int NULL DEFAULT NULL COMMENT '分片总数',
  `monitor_sys_gen_file_system_chunk_uploaded` int NULL DEFAULT NULL COMMENT '已上传分片数',
  `monitor_sys_gen_file_system_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件描述',
  `monitor_sys_gen_file_system_upload_user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '上传用户',
  `monitor_sys_gen_file_system_download_count` int NULL DEFAULT NULL COMMENT '下载次数',
  `monitor_sys_gen_file_system_last_download_time` datetime(6) NULL DEFAULT NULL COMMENT '最后下载时间',
  `monitor_sys_gen_file_system_http_access` int NULL DEFAULT NULL COMMENT '是否允许HTTP访问 0:否 1:是',
  `monitor_sys_gen_file_system_http_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'HTTP访问URL',
  `monitor_sys_gen_file_system_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_file_system_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_file_system_part`;
CREATE TABLE `monitor_sys_gen_file_system_part`  (
  `monitor_sys_gen_file_system_part_id` int NOT NULL AUTO_INCREMENT COMMENT '分片ID',
  `monitor_sys_gen_file_system_id` int NULL DEFAULT NULL COMMENT '文件系统ID',
  `monitor_sys_gen_file_system_part_index` int NULL DEFAULT NULL COMMENT '分片序号(从1开始)',
  `monitor_sys_gen_file_system_part_size` bigint NULL DEFAULT NULL COMMENT '分片大小(字节)',
  `monitor_sys_gen_file_system_part_md5` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分片MD5',
  `monitor_sys_gen_file_system_part_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分片文件路径',
  `monitor_sys_gen_file_system_part_status` int NULL DEFAULT NULL COMMENT '分片状态 0:待上传 1:处理中 2:已完成 3:上传失败',
  `monitor_sys_gen_file_system_part_start_time` datetime(6) NULL DEFAULT NULL COMMENT '上传开始时间',
  `monitor_sys_gen_file_system_part_end_time` datetime(6) NULL DEFAULT NULL COMMENT '上传完成时间',
  `monitor_sys_gen_file_system_part_error_msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误信息',
  `monitor_sys_gen_file_system_part_retry_count` int NULL DEFAULT NULL COMMENT '重试次数',
  `monitor_sys_gen_file_system_part_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_file_system_part_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_file_system_setting`;
CREATE TABLE `monitor_sys_gen_file_system_setting`  (
  `monitor_sys_gen_file_system_setting_id` int NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `monitor_sys_gen_file_system_setting_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置键',
  `monitor_sys_gen_file_system_setting_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置值',
  `monitor_sys_gen_file_system_setting_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置类型 string/number/boolean/json',
  `monitor_sys_gen_file_system_setting_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置名称',
  `monitor_sys_gen_file_system_setting_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置描述',
  `monitor_sys_gen_file_system_setting_group` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置分组',
  `monitor_sys_gen_file_system_setting_status` int NULL DEFAULT NULL COMMENT '是否启用 0:禁用 1:启用',
  `monitor_sys_gen_file_system_setting_sort` int NULL DEFAULT NULL COMMENT '排序号',
  `monitor_sys_gen_file_system_setting_default_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '默认值',
  `monitor_sys_gen_file_system_setting_required` int NULL DEFAULT NULL COMMENT '是否必填 0:否 1:是',
  `monitor_sys_gen_file_system_setting_validation` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '验证规则',
  `monitor_sys_gen_file_system_setting_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_file_system_setting_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_global_setting`;
CREATE TABLE `monitor_sys_gen_global_setting`  (
  `monitor_sys_gen_global_setting_id` int NOT NULL DEFAULT 1 COMMENT '全局配置ID（固定为1）',
  `monitor_sys_gen_global_setting_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '默认全局配置' COMMENT '配置名称',
  `monitor_sys_gen_global_setting_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '系统默认全局配置' COMMENT '配置描述',
  `monitor_sys_gen_global_setting_dispatcher_enabled` tinyint(1) NULL DEFAULT 1 COMMENT '全局调度器总开关 0:关闭 1:开启',
  `monitor_sys_gen_global_setting_status_summary_enabled` tinyint(1) NULL DEFAULT 1 COMMENT '服务器状态汇总推送开关 0:关闭 1:开启',
  `monitor_sys_gen_global_setting_performance_trends_enabled` tinyint(1) NULL DEFAULT 1 COMMENT '性能趋势推送开关 0:关闭 1:开启',
  `monitor_sys_gen_global_setting_connection_stats_enabled` tinyint(1) NULL DEFAULT 1 COMMENT '连接统计推送开关 0:关闭 1:开启',
  `monitor_sys_gen_global_setting_health_status_enabled` tinyint(1) NULL DEFAULT 1 COMMENT '健康状态推送开关 0:关闭 1:开启',
  `monitor_sys_gen_global_setting_global_alert_enabled` tinyint(1) NULL DEFAULT 1 COMMENT '全局告警处理开关 0:关闭 1:开启',
  `monitor_sys_gen_global_setting_cleanup_enabled` tinyint(1) NULL DEFAULT 1 COMMENT '无效任务清理开关 0:关闭 1:开启',
  `monitor_sys_gen_global_setting_config_refresh_enabled` tinyint(1) NULL DEFAULT 1 COMMENT '配置刷新开关 0:关闭 1:开启',
  `monitor_sys_gen_global_setting_file_upload_check_enabled` tinyint(1) NULL DEFAULT 1 COMMENT '文件上传检查开关 0:关闭 1:开启',
  `monitor_sys_gen_global_setting_data_cleanup_enabled` tinyint(1) NULL DEFAULT 1 COMMENT '数据清理开关 0:关闭 1:开启',
  `monitor_sys_gen_global_setting_backup_enabled` tinyint(1) NULL DEFAULT 1 COMMENT '备份任务开关 0:关闭 1:开启',
  `create_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`monitor_sys_gen_global_setting_id`) USING BTREE,
  CONSTRAINT `chk_single_record` CHECK (`monitor_sys_gen_global_setting_id` = 1)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '全局配置设置表（单例模式，只允许一条记录）' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `monitor_sys_gen_message_push_template`;
CREATE TABLE `monitor_sys_gen_message_push_template`  (
  `monitor_sys_gen_message_push_template_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `monitor_sys_gen_message_push_template_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板名称',
  `monitor_sys_gen_message_push_template_channel` varchar(11) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '通知通道',
  `monitor_sys_gen_message_push_template_enabled` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否启用',
  `monitor_sys_gen_message_push_template_endpoint` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标地址（Webhook URL、邮箱、手机号等）',
  `monitor_sys_gen_message_push_template_main_account` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '主账号',
  `monitor_sys_gen_message_push_template_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名',
  `monitor_sys_gen_message_push_template_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
  `monitor_sys_gen_message_push_template_token` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '令牌/密钥',
  `monitor_sys_gen_message_push_template_extra` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '扩展参数（JSON）',
  `monitor_sys_gen_message_push_template_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_message_push_template_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_prometheus_config`;
CREATE TABLE `monitor_sys_gen_prometheus_config`  (
  `monitor_sys_gen_prometheus_config_id` int NOT NULL AUTO_INCREMENT,
  `monitor_sys_gen_prometheus_config_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `monitor_sys_gen_prometheus_config_postion` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '位置',
  `monitor_sys_gen_prometheus_config_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标题',
  `monitor_sys_gen_prometheus_config_ql` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '表达式',
  `monitor_sys_gen_prometheus_config_chart_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图表类型',
  `monitor_sys_gen_prometheus_config_chart_config` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图表配置',
  `monitor_sys_gen_prometheus_config_tip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '说明',
  `monitor_sys_gen_prometheus_config_enable` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否开启',
  `monitor_sys_gen_prometheus_config_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '类型',
  `monitor_sys_gen_id` int NULL DEFAULT NULL COMMENT '所属genId',
  `monitor_sys_gen_prometheus_config_share` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否共享',
  `monitor_sys_gen_prometheus_config_value_unit` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据单位',
  `monitor_sys_gen_prometheus_config_origin` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '共享来源',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_prometheus_config_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_remark`;
CREATE TABLE `monitor_sys_gen_remark`  (
  `remark_id` int NOT NULL AUTO_INCREMENT,
  `remark_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `gen_id` int NULL DEFAULT NULL COMMENT 'gen_id',
  `remark_table` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '表',
  `remark_database` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据库',
  `remark_column` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字段',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`remark_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字段备注' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_script`;
CREATE TABLE `monitor_sys_gen_script`  (
  `monitor_sys_gen_script_id` int NOT NULL AUTO_INCREMENT COMMENT '脚本ID',
  `monitor_sys_gen_script_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本名称',
  `monitor_sys_gen_script_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本类型',
  `monitor_sys_gen_script_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本描述',
  `monitor_sys_gen_script_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本内容',
  `monitor_sys_gen_script_parameters` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '默认参数(JSON格式)',
  `monitor_sys_gen_script_timeout` int NULL DEFAULT NULL COMMENT '执行超时时间(秒)',
  `monitor_sys_gen_script_status` varchar(11) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '脚本状态',
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
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_script_category`;
CREATE TABLE `monitor_sys_gen_script_category`  (
  `monitor_sys_gen_script_category_id` int NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `monitor_sys_gen_script_category_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类名称',
  `monitor_sys_gen_script_category_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类描述',
  `monitor_sys_gen_script_category_parent_id` int NULL DEFAULT NULL COMMENT '父分类ID',
  `monitor_sys_gen_script_category_sort` int NULL DEFAULT NULL COMMENT '排序',
  `monitor_sys_gen_script_category_status` varchar(11) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '状态',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_script_category_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_script_execution`;
CREATE TABLE `monitor_sys_gen_script_execution`  (
  `monitor_sys_gen_script_execution_id` bigint NOT NULL AUTO_INCREMENT COMMENT '执行记录ID',
  `monitor_sys_gen_script_id` int NULL DEFAULT NULL COMMENT '脚本ID',
  `monitor_sys_gen_script_execution_parameters` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执行参数',
  `monitor_sys_gen_script_execution_status` varchar(11) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '执行状态',
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
  `monitor_sys_gen_script_execution_trigger_type` varchar(11) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '触发类型',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_script_execution_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_script_upload_record`;
CREATE TABLE `monitor_sys_gen_script_upload_record`  (
  `monitor_sys_gen_script_upload_record_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `monitor_sys_gen_script_id` int NULL DEFAULT NULL COMMENT '脚本ID',
  `monitor_sys_gen_script_upload_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '上传类型：REMOTE/NODE',
  `monitor_sys_gen_script_upload_server_id` int NULL DEFAULT NULL COMMENT '服务器ID',
  `monitor_sys_gen_script_upload_node_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '节点ID',
  `monitor_sys_gen_script_upload_target_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标路径',
  `monitor_sys_gen_script_upload_file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件名',
  `monitor_sys_gen_script_upload_file_size` bigint NULL DEFAULT NULL COMMENT '文件大小（字节）',
  `monitor_sys_gen_script_upload_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '上传状态：SUCCESS/FAILED',
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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_serial`;
CREATE TABLE `monitor_sys_gen_serial`  (
  `monitor_sys_gen_serial_id` int NOT NULL AUTO_INCREMENT COMMENT '串口配置ID',
  `monitor_sys_gen_serial_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '串口名称',
  `monitor_sys_gen_serial_port` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '串口端口',
  `monitor_sys_gen_serial_baud_rate` int NULL DEFAULT NULL COMMENT '波特率',
  `monitor_sys_gen_serial_data_bits` int NULL DEFAULT NULL COMMENT '数据位',
  `monitor_sys_gen_serial_stop_bits` int NULL DEFAULT NULL COMMENT '停止位',
  `monitor_sys_gen_serial_parity` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '校验位',
  `monitor_sys_gen_serial_flow_control` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '流控制',
  `monitor_sys_gen_serial_timeout` int NULL DEFAULT NULL COMMENT '超时时间(毫秒)',
  `monitor_sys_gen_serial_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `monitor_sys_gen_serial_status` int NULL DEFAULT NULL COMMENT '状态,0:停用',
  `gen_id` int NULL DEFAULT NULL COMMENT '生成器id',
  PRIMARY KEY (`monitor_sys_gen_serial_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_server`;
CREATE TABLE `monitor_sys_gen_server`  (
  `monitor_sys_gen_server_id` int NOT NULL AUTO_INCREMENT COMMENT '服务器配置ID',
  `monitor_sys_gen_server_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务器名称',
  `monitor_sys_gen_server_host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务器主机地址',
  `monitor_sys_gen_server_port` int NULL DEFAULT NULL COMMENT '服务器端口',
  `monitor_sys_gen_server_protocol` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '连接协议',
  `monitor_sys_gen_server_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名',
  `monitor_sys_gen_server_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
  `monitor_sys_gen_server_private_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '私钥文件路径',
  `monitor_sys_gen_server_timeout` int NULL DEFAULT NULL COMMENT '连接超时时间(毫秒)',
  `monitor_sys_gen_server_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务器描述',
  `monitor_sys_gen_server_status` int NULL DEFAULT NULL COMMENT '服务器状态',
  `monitor_sys_gen_server_tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务器标签',
  `monitor_sys_gen_server_connection_status` int NULL DEFAULT NULL COMMENT '连接状态',
  `monitor_sys_gen_server_last_connect_time` datetime(6) NULL DEFAULT NULL COMMENT '最后连接时间',
  `monitor_sys_gen_server_connection_error` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '连接失败原因',
  `monitor_sys_gen_server_proxy_id` int NULL DEFAULT NULL COMMENT '代理ID',
  `monitor_sys_gen_server_is_local` int NULL DEFAULT NULL COMMENT '是否本地服务器',
  `monitor_sys_gen_server_ip_addresses` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务器IP地址列表',
  `monitor_sys_gen_server_os_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作系统类型',
  `monitor_sys_gen_server_os_version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作系统版本',
  `monitor_sys_gen_server_os_arch` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作系统架构',
  `monitor_sys_gen_server_latency` bigint NULL DEFAULT NULL COMMENT '网络延迟(毫秒)',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  `monitor_sys_gen_server_group_id` int NULL DEFAULT NULL COMMENT '服务器分组ID',
  PRIMARY KEY (`monitor_sys_gen_server_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_server_alert_history`;
CREATE TABLE `monitor_sys_gen_server_alert_history`  (
  `monitor_sys_gen_server_alert_history_id` bigint NOT NULL AUTO_INCREMENT COMMENT '告警历史ID',
  `monitor_sys_gen_server_alert_history_server_id` int NULL DEFAULT NULL COMMENT '服务器ID',
  `monitor_sys_gen_server_alert_history_type` varchar(11) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '告警类型',
  `monitor_sys_gen_server_alert_history_level` varchar(11) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '告警级别',
  `monitor_sys_gen_server_alert_history_status` varchar(11) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '告警状态',
  `monitor_sys_gen_server_alert_history_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '告警标题',
  `monitor_sys_gen_server_alert_history_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '告警内容',
  `monitor_sys_gen_server_alert_history_current_value` decimal(11, 2) NULL DEFAULT NULL COMMENT '当前值',
  `monitor_sys_gen_server_alert_history_threshold_value` decimal(11, 2) NULL DEFAULT NULL COMMENT '阈值',
  `monitor_sys_gen_server_alert_history_unit` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '单位',
  `monitor_sys_gen_server_alert_history_trigger_time` datetime(6) NULL DEFAULT NULL COMMENT '触发时间',
  `monitor_sys_gen_server_alert_history_recovery_time` datetime(6) NULL DEFAULT NULL COMMENT '恢复时间',
  `monitor_sys_gen_server_alert_history_silence_until` datetime(6) NULL DEFAULT NULL COMMENT '静默截止时间',
  `monitor_sys_gen_server_alert_history_fingerprint` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '告警指纹（用于去重）',
  `monitor_sys_gen_server_alert_history_notification_sent` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否已发送通知',
  `monitor_sys_gen_server_alert_history_notification_method` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '通知方式',
  `monitor_sys_gen_server_alert_history_notification_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '通知地址',
  `monitor_sys_gen_server_alert_history_notification_time` datetime(6) NULL DEFAULT NULL COMMENT '通知发送时间',
  `monitor_sys_gen_server_alert_history_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `monitor_sys_gen_server_alert_history_ignore_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '忽略原因',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_server_alert_history_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1307 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_server_component`;
CREATE TABLE `monitor_sys_gen_server_component`  (
  `monitor_sys_gen_server_component_id` bigint NOT NULL AUTO_INCREMENT COMMENT '组件配置ID',
  `monitor_sys_gen_server_id` int NULL DEFAULT NULL COMMENT '关联服务器ID',
  `monitor_sys_gen_server_component_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件名称',
  `monitor_sys_gen_server_component_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件标题',
  `monitor_sys_gen_server_component_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件类型',
  `monitor_sys_gen_server_component_expression_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '表达式类型',
  `monitor_sys_gen_server_component_expression` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '查询表达式',
  `monitor_sys_gen_server_component_position` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件位置信息',
  `monitor_sys_gen_server_component_chart_config` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图表配置',
  `monitor_sys_gen_server_component_refresh_interval` int NULL DEFAULT NULL COMMENT '刷新间隔(秒)',
  `monitor_sys_gen_server_component_enabled` int NULL DEFAULT NULL COMMENT '是否启用',
  `monitor_sys_gen_server_component_sort_order` int NULL DEFAULT NULL COMMENT '排序序号',
  `monitor_sys_gen_server_component_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件描述',
  `monitor_sys_gen_server_component_create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `monitor_sys_gen_server_component_update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  `monitor_sys_gen_server_component_show_title` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否显示标题',
  PRIMARY KEY (`monitor_sys_gen_server_component_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_server_component_layout`;
CREATE TABLE `monitor_sys_gen_server_component_layout`  (
  `monitor_sys_gen_server_component_layout_id` bigint NOT NULL AUTO_INCREMENT COMMENT '组件布局配置ID',
  `monitor_sys_gen_server_id` int NULL DEFAULT NULL COMMENT '关联服务器ID',
  `monitor_sys_gen_server_component_id` bigint NULL DEFAULT NULL COMMENT '关联组件ID',
  `monitor_sys_gen_server_component_layout_x` int NULL DEFAULT NULL COMMENT '布局X坐标',
  `monitor_sys_gen_server_component_layout_y` int NULL DEFAULT NULL COMMENT '布局Y坐标',
  `monitor_sys_gen_server_component_layout_w` int NULL DEFAULT NULL COMMENT '布局宽度',
  `monitor_sys_gen_server_component_layout_h` int NULL DEFAULT NULL COMMENT '布局高度',
  `monitor_sys_gen_server_component_layout_z_index` int NULL DEFAULT NULL COMMENT '布局层级',
  `monitor_sys_gen_server_component_layout_movable` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否可移动',
  `monitor_sys_gen_server_component_layout_resizable` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否可调整大小',
  `monitor_sys_gen_server_component_layout_status` int NULL DEFAULT NULL COMMENT '布局状态',
  `monitor_sys_gen_server_component_layout_sort_order` int NULL DEFAULT NULL COMMENT '排序顺序',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_server_component_layout_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_server_connection_status`;
CREATE TABLE `monitor_sys_gen_server_connection_status`  (
  `monitor_sys_gen_server_id` int NOT NULL AUTO_INCREMENT COMMENT '服务器ID',
  `monitor_sys_gen_server_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务器名称',
  `monitor_sys_gen_server_host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务器主机地址',
  `monitor_sys_gen_server_port` int NULL DEFAULT NULL COMMENT '服务器端口',
  `monitor_sys_gen_server_protocol` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '连接协议',
  `monitor_sys_gen_server_connection_status` int NULL DEFAULT NULL COMMENT '连接状态 0:离线 1:在线 2:连接中 3:连接失败',
  `monitor_sys_gen_server_last_connect_time` datetime(6) NULL DEFAULT NULL COMMENT '最后连接时间',
  `monitor_sys_gen_server_connection_error` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '连接失败原因',
  `monitor_sys_gen_server_response_time` bigint NULL DEFAULT NULL COMMENT '连接响应时间(毫秒)',
  `monitor_sys_gen_server_test_time` datetime(6) NULL DEFAULT NULL COMMENT '连接测试时间',
  `monitor_sys_gen_server_monitor_enabled` int NULL DEFAULT NULL COMMENT '是否启用监控',
  `monitor_sys_gen_server_status` int NULL DEFAULT NULL COMMENT '服务器状态',
  PRIMARY KEY (`monitor_sys_gen_server_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_server_file_upload_queue`;
CREATE TABLE `monitor_sys_gen_server_file_upload_queue`  (
  `monitor_sys_gen_server_file_upload_queue_id` bigint NOT NULL AUTO_INCREMENT COMMENT '队列ID',
  `monitor_sys_gen_server_file_upload_task_id` bigint NULL DEFAULT NULL COMMENT '任务ID',
  `monitor_sys_gen_server_id` int NULL DEFAULT NULL COMMENT '服务器ID',
  `monitor_sys_gen_server_file_upload_queue_priority` int NULL DEFAULT NULL COMMENT '队列优先级(1-10，数字越小优先级越高)',
  `monitor_sys_gen_server_file_upload_queue_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '队列状态(PENDING:待处理 PROCESSING:处理中 COMPLETED:已完成 FAILED:失败)',
  `monitor_sys_gen_server_file_upload_queue_retry_count` int NULL DEFAULT NULL COMMENT '重试次数',
  `monitor_sys_gen_server_file_upload_queue_create_time` datetime(6) NULL DEFAULT NULL COMMENT '入队时间',
  `monitor_sys_gen_server_file_upload_queue_process_time` datetime(6) NULL DEFAULT NULL COMMENT '开始处理时间',
  `monitor_sys_gen_server_file_upload_queue_complete_time` datetime(6) NULL DEFAULT NULL COMMENT '完成时间',
  `monitor_sys_gen_server_file_upload_queue_error_message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误信息',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_server_file_upload_queue_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_server_file_upload_records`;
CREATE TABLE `monitor_sys_gen_server_file_upload_records`  (
  `monitor_sys_gen_server_file_upload_record_id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `monitor_sys_gen_server_file_upload_task_id` bigint NULL DEFAULT NULL COMMENT '关联任务ID',
  `monitor_sys_gen_server_id` int NULL DEFAULT NULL COMMENT '目标服务器ID',
  `monitor_sys_gen_file_upload_target_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标路径',
  `monitor_sys_gen_file_upload_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '上传状态(PENDING:待上传 UPLOADING:上传中 COMPLETED:已完成 FAILED:失败 CANCELLED:已取消)',
  `monitor_sys_gen_file_upload_progress` int NULL DEFAULT NULL COMMENT '上传进度(0-100)',
  `monitor_sys_gen_file_upload_speed` bigint NULL DEFAULT NULL COMMENT '上传速度(字节/秒)',
  `monitor_sys_gen_file_upload_transferred_bytes` bigint NULL DEFAULT NULL COMMENT '已传输字节数',
  `monitor_sys_gen_file_upload_total_bytes` bigint NULL DEFAULT NULL COMMENT '总字节数',
  `monitor_sys_gen_file_upload_start_time` datetime(6) NULL DEFAULT NULL COMMENT '开始上传时间',
  `monitor_sys_gen_file_upload_end_time` datetime(6) NULL DEFAULT NULL COMMENT '结束时间',
  `monitor_sys_gen_file_upload_duration_ms` bigint NULL DEFAULT NULL COMMENT '上传耗时(毫秒)',
  `monitor_sys_gen_file_upload_error_message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误信息',
  `monitor_sys_gen_file_upload_error_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误代码',
  `monitor_sys_gen_file_upload_retry_count` int NULL DEFAULT NULL COMMENT '重试次数',
  `monitor_sys_gen_file_upload_file_md5_remote` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '远程文件MD5值',
  `monitor_sys_gen_file_upload_verification_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '验证状态(PENDING:待验证 PASSED:通过 FAILED:失败)',
  `monitor_sys_gen_file_upload_backup_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备份文件路径',
  `monitor_sys_gen_file_upload_connection_info` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '连接信息(JSON格式)',
  `monitor_sys_gen_file_upload_extra_data` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '额外数据(JSON格式)',
  `monitor_sys_gen_file_upload_create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `monitor_sys_gen_file_upload_update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `monitor_sys_gen_file_upload_record_status` int NULL DEFAULT NULL COMMENT '记录状态(0:删除 1:正常)',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_server_file_upload_record_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_server_file_upload_tasks`;
CREATE TABLE `monitor_sys_gen_server_file_upload_tasks`  (
  `monitor_sys_gen_server_file_upload_task_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `monitor_sys_gen_server_file_upload_task_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务名称',
  `monitor_sys_gen_server_file_upload_task_file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件名称',
  `monitor_sys_gen_server_file_upload_task_file_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件路径',
  `monitor_sys_gen_server_file_upload_task_file_size` bigint NULL DEFAULT NULL COMMENT '文件大小(字节)',
  `monitor_sys_gen_server_file_upload_task_file_md5` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件MD5值',
  `monitor_sys_gen_file_upload_target_server_ids` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标服务器ID列表(JSON格式)',
  `monitor_sys_gen_file_upload_target_paths` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标路径列表(JSON格式)',
  `monitor_sys_gen_file_upload_mode` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '上传模式(REALTIME:实时 SCHEDULED:定时)',
  `monitor_sys_gen_file_upload_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '上传类型(SINGLE:单文件 BATCH:批量)',
  `monitor_sys_gen_file_upload_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务状态(PENDING:待处理 PROCESSING:处理中 COMPLETED:已完成 FAILED:失败 CANCELLED:已取消)',
  `monitor_sys_gen_file_upload_scheduled_time` datetime(6) NULL DEFAULT NULL COMMENT '定时执行时间',
  `monitor_sys_gen_file_upload_priority` int NULL DEFAULT NULL COMMENT '优先级(1-10，数字越小优先级越高)',
  `monitor_sys_gen_file_upload_retry_count` int NULL DEFAULT NULL COMMENT '重试次数',
  `monitor_sys_gen_file_upload_max_retry` int NULL DEFAULT NULL COMMENT '最大重试次数',
  `monitor_sys_gen_file_upload_timeout_seconds` int NULL DEFAULT NULL COMMENT '超时时间(秒)',
  `monitor_sys_gen_file_upload_overwrite_enabled` int NULL DEFAULT NULL COMMENT '是否覆盖已存在文件(0:否 1:是)',
  `monitor_sys_gen_file_upload_backup_enabled` int NULL DEFAULT NULL COMMENT '是否备份原文件(0:否 1:是)',
  `monitor_sys_gen_file_upload_compress_enabled` int NULL DEFAULT NULL COMMENT '是否压缩传输(0:否 1:是)',
  `monitor_sys_gen_file_upload_verify_enabled` int NULL DEFAULT NULL COMMENT '是否验证文件完整性(0:否 1:是)',
  `monitor_sys_gen_file_upload_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务描述',
  `monitor_sys_gen_file_upload_tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签(JSON格式)',
  `monitor_sys_gen_file_upload_create_user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建用户',
  `monitor_sys_gen_file_upload_create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `monitor_sys_gen_file_upload_update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `monitor_sys_gen_file_upload_start_time` datetime(6) NULL DEFAULT NULL COMMENT '开始执行时间',
  `monitor_sys_gen_file_upload_end_time` datetime(6) NULL DEFAULT NULL COMMENT '结束时间',
  `monitor_sys_gen_file_upload_task_status` int NULL DEFAULT NULL COMMENT '记录状态(0:删除 1:正常)',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_server_file_upload_task_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_server_group`;
CREATE TABLE `monitor_sys_gen_server_group`  (
  `monitor_sys_gen_server_group_id` int NOT NULL AUTO_INCREMENT COMMENT '分组ID',
  `monitor_sys_gen_server_group_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组名称',
  `monitor_sys_gen_server_group_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组描述',
  `monitor_sys_gen_server_group_color` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组颜色(十六进制颜色值)',
  `monitor_sys_gen_server_group_icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组图标',
  `monitor_sys_gen_server_group_status` int NULL DEFAULT NULL COMMENT '分组状态 0:禁用 1:启用',
  `monitor_sys_gen_server_group_is_default` int NULL DEFAULT NULL COMMENT '是否默认分组 0:否 1:是',
  `monitor_sys_gen_server_group_sort` int NULL DEFAULT NULL COMMENT '排序号',
  `monitor_sys_gen_server_group_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_server_group_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_server_layout`;
CREATE TABLE `monitor_sys_gen_server_layout`  (
  `monitor_sys_gen_server_layout_id` int NOT NULL AUTO_INCREMENT COMMENT '布局配置ID',
  `monitor_sys_gen_server_id` int NULL DEFAULT NULL COMMENT '关联服务器ID',
  `monitor_sys_gen_server_layout_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '布局名称',
  `monitor_sys_gen_server_layout_config` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '布局配置',
  `monitor_sys_gen_server_layout_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '布局类型',
  `monitor_sys_gen_server_layout_default` int NULL DEFAULT NULL COMMENT '是否为默认布局',
  `monitor_sys_gen_server_layout_status` int NULL DEFAULT NULL COMMENT '布局状态',
  `monitor_sys_gen_server_layout_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '布局描述',
  `monitor_sys_gen_server_layout_version` int NULL DEFAULT NULL COMMENT '布局版本',
  `monitor_sys_gen_server_layout_sort` int NULL DEFAULT NULL COMMENT '布局排序',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_server_layout_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_server_log_statistics`;
CREATE TABLE `monitor_sys_gen_server_log_statistics`  (
  `monitor_sys_gen_server_log_stat_id` bigint NOT NULL AUTO_INCREMENT COMMENT '统计ID',
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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_server_logs`;
CREATE TABLE `monitor_sys_gen_server_logs`  (
  `monitor_sys_gen_server_log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `monitor_sys_gen_server_id` int NULL DEFAULT NULL COMMENT '关联服务器ID',
  `monitor_sys_gen_server_log_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志内容',
  `monitor_sys_gen_server_log_level` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志级别(DEBUG,INFO,WARN,ERROR,FATAL)',
  `monitor_sys_gen_server_log_source` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志来源(SYSTEM,APPLICATION,SECURITY,ACCESS)',
  `monitor_sys_gen_server_log_category` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志分类',
  `monitor_sys_gen_server_log_thread` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '线程名称',
  `monitor_sys_gen_server_log_logger` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志记录器名称',
  `monitor_sys_gen_server_log_file_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志文件路径',
  `monitor_sys_gen_server_log_line_number` int NULL DEFAULT NULL COMMENT '日志行号',
  `monitor_sys_gen_server_log_exception_stack` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '异常堆栈信息',
  `monitor_sys_gen_server_log_create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `monitor_sys_gen_server_log_server_time` datetime(6) NULL DEFAULT NULL COMMENT '服务器时间',
  `monitor_sys_gen_server_log_ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务器IP',
  `monitor_sys_gen_server_log_hostname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '主机名',
  `monitor_sys_gen_server_log_process_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '进程ID',
  `monitor_sys_gen_server_log_tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志标签(JSON格式)',
  `monitor_sys_gen_server_log_extra_data` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '额外数据(JSON格式)',
  `monitor_sys_gen_server_log_status` int NULL DEFAULT NULL COMMENT '状态(0:删除 1:正常)',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_server_log_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14167 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_server_metrics`;
CREATE TABLE `monitor_sys_gen_server_metrics`  (
  `monitor_sys_gen_server_metrics_id` bigint NOT NULL AUTO_INCREMENT COMMENT '指标数据ID',
  `monitor_sys_gen_server_id` int NULL DEFAULT NULL COMMENT '服务器ID',
  `monitor_sys_gen_server_metrics_collect_time` datetime(6) NULL DEFAULT NULL COMMENT '抓取时间',
  `monitor_sys_gen_server_metrics_cpu_usage` decimal(11, 2) NULL DEFAULT NULL COMMENT 'CPU使用率',
  `monitor_sys_gen_server_metrics_cpu_cores` int NULL DEFAULT NULL COMMENT 'CPU核心数',
  `monitor_sys_gen_server_metrics_cpu_load_1m` decimal(11, 2) NULL DEFAULT NULL COMMENT 'CPU负载1分钟',
  `monitor_sys_gen_server_metrics_cpu_load_5m` decimal(11, 2) NULL DEFAULT NULL COMMENT 'CPU负载5分钟',
  `monitor_sys_gen_server_metrics_cpu_load_15m` decimal(11, 2) NULL DEFAULT NULL COMMENT 'CPU负载15分钟',
  `monitor_sys_gen_server_metrics_memory_total` bigint NULL DEFAULT NULL COMMENT '内存总量',
  `monitor_sys_gen_server_metrics_memory_used` bigint NULL DEFAULT NULL COMMENT '内存使用量',
  `monitor_sys_gen_server_metrics_memory_free` bigint NULL DEFAULT NULL COMMENT '内存可用量',
  `monitor_sys_gen_server_metrics_memory_usage` decimal(11, 2) NULL DEFAULT NULL COMMENT '内存使用率',
  `monitor_sys_gen_server_metrics_disk_total` bigint NULL DEFAULT NULL COMMENT '磁盘总量',
  `monitor_sys_gen_server_metrics_disk_used` bigint NULL DEFAULT NULL COMMENT '磁盘使用量',
  `monitor_sys_gen_server_metrics_disk_free` bigint NULL DEFAULT NULL COMMENT '磁盘可用量',
  `monitor_sys_gen_server_metrics_disk_usage` decimal(11, 2) NULL DEFAULT NULL COMMENT '磁盘使用率',
  `monitor_sys_gen_server_metrics_network_in` bigint NULL DEFAULT NULL COMMENT '网络入流量',
  `monitor_sys_gen_server_metrics_network_out` bigint NULL DEFAULT NULL COMMENT '网络出流量',
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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_server_script_executions`;
CREATE TABLE `monitor_sys_gen_server_script_executions`  (
  `monitor_sys_gen_server_script_execution_id` bigint NOT NULL AUTO_INCREMENT COMMENT '执行记录ID',
  `monitor_sys_gen_server_script_id` bigint NULL DEFAULT NULL COMMENT '脚本ID',
  `monitor_sys_gen_server_id` int NULL DEFAULT NULL COMMENT '服务器ID',
  `monitor_sys_gen_server_script_execution_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执行名称',
  `monitor_sys_gen_server_script_execution_parameters` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执行参数(JSON格式)',
  `monitor_sys_gen_server_script_execution_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执行状态(PENDING:待执行 RUNNING:执行中 COMPLETED:已完成 FAILED:失败 CANCELLED:已取消 TIMEOUT:超时)',
  `monitor_sys_gen_server_script_execution_mode` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执行模式(SYNC:同步 ASYNC:异步)',
  `monitor_sys_gen_server_script_execution_priority` int NULL DEFAULT NULL COMMENT '执行优先级(1-10，数字越小优先级越高)',
  `monitor_sys_gen_server_script_execution_start_time` datetime(6) NULL DEFAULT NULL COMMENT '开始执行时间',
  `monitor_sys_gen_server_script_execution_end_time` datetime(6) NULL DEFAULT NULL COMMENT '结束时间',
  `monitor_sys_gen_server_script_execution_duration_ms` bigint NULL DEFAULT NULL COMMENT '执行耗时(毫秒)',
  `monitor_sys_gen_server_script_execution_exit_code` int NULL DEFAULT NULL COMMENT '退出代码',
  `monitor_sys_gen_server_script_execution_output` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执行输出',
  `monitor_sys_gen_server_script_execution_error_output` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误输出',
  `monitor_sys_gen_server_script_execution_working_directory` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '工作目录',
  `monitor_sys_gen_server_script_execution_environment_vars` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '环境变量(JSON格式)',
  `monitor_sys_gen_server_script_execution_process_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '进程ID',
  `monitor_sys_gen_server_script_execution_trigger_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '触发类型(MANUAL:手动 SCHEDULED:定时 API:API调用 EVENT:事件触发)',
  `monitor_sys_gen_server_script_execution_trigger_user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '触发用户',
  `monitor_sys_gen_server_script_execution_retry_count` int NULL DEFAULT NULL COMMENT '重试次数',
  `monitor_sys_gen_server_script_execution_max_retry` int NULL DEFAULT NULL COMMENT '最大重试次数',
  `monitor_sys_gen_server_script_execution_parent_id` bigint NULL DEFAULT NULL COMMENT '父执行记录ID(重试时使用)',
  `monitor_sys_gen_server_script_execution_scheduled_time` datetime(6) NULL DEFAULT NULL COMMENT '计划执行时间',
  `monitor_sys_gen_server_script_execution_timeout_seconds` int NULL DEFAULT NULL COMMENT '超时时间(秒)',
  `monitor_sys_gen_server_script_execution_tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执行标签(JSON格式)',
  `monitor_sys_gen_server_script_execution_metadata` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执行元数据(JSON格式)',
  `monitor_sys_gen_server_script_execution_create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `monitor_sys_gen_server_script_execution_update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `monitor_sys_gen_server_script_execution_record_status` int NULL DEFAULT NULL COMMENT '记录状态(0:删除 1:正常)',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_server_script_execution_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_server_scripts`;
CREATE TABLE `monitor_sys_gen_server_scripts`  (
  `monitor_sys_gen_server_script_id` bigint NOT NULL AUTO_INCREMENT COMMENT '脚本ID',
  `monitor_sys_gen_server_script_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本名称',
  `monitor_sys_gen_server_script_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本描述',
  `monitor_sys_gen_server_script_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本内容',
  `monitor_sys_gen_server_script_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本类型(SHELL:Shell脚本 PYTHON:Python脚本 POWERSHELL:PowerShell脚本 BATCH:批处理脚本)',
  `monitor_sys_gen_server_script_language` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本语言',
  `monitor_sys_gen_server_script_version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本版本',
  `monitor_sys_gen_server_script_category` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本分类',
  `monitor_sys_gen_server_script_tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本标签(JSON格式)',
  `monitor_sys_gen_server_script_parameters` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本参数定义(JSON格式)',
  `monitor_sys_gen_server_script_timeout_seconds` int NULL DEFAULT NULL COMMENT '执行超时时间(秒)',
  `monitor_sys_gen_server_script_retry_count` int NULL DEFAULT NULL COMMENT '重试次数',
  `monitor_sys_gen_server_script_is_template` int NULL DEFAULT NULL COMMENT '是否为模板(0:否 1:是)',
  `monitor_sys_gen_server_script_is_public` int NULL DEFAULT NULL COMMENT '是否公开(0:否 1:是)',
  `monitor_sys_gen_server_script_execution_mode` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执行模式(SYNC:同步 ASYNC:异步)',
  `monitor_sys_gen_server_script_working_directory` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '工作目录',
  `monitor_sys_gen_server_script_environment_vars` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '环境变量(JSON格式)',
  `monitor_sys_gen_server_script_dependencies` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '依赖项(JSON格式)',
  `monitor_sys_gen_server_script_validation_rules` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '验证规则(JSON格式)',
  `monitor_sys_gen_server_script_create_user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建用户',
  `monitor_sys_gen_server_script_create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `monitor_sys_gen_server_script_update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `monitor_sys_gen_server_script_last_execution_time` datetime(6) NULL DEFAULT NULL COMMENT '最后执行时间',
  `monitor_sys_gen_server_script_execution_count` int NULL DEFAULT NULL COMMENT '执行次数',
  `monitor_sys_gen_server_script_success_count` int NULL DEFAULT NULL COMMENT '成功次数',
  `monitor_sys_gen_server_script_status` int NULL DEFAULT NULL COMMENT '脚本状态(0:禁用 1:启用)',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_server_script_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_server_setting`;
CREATE TABLE `monitor_sys_gen_server_setting`  (
  `monitor_sys_gen_server_setting_id` int NOT NULL AUTO_INCREMENT COMMENT '服务器配置设置ID',
  `monitor_sys_gen_server_id` int NULL DEFAULT NULL COMMENT '服务器ID',
  `monitor_sys_gen_server_setting_health_check_enabled` int NULL DEFAULT NULL COMMENT '健康检查开关',
  `monitor_sys_gen_server_setting_metrics_collection_enabled` int NULL DEFAULT NULL COMMENT '指标采集开关',
  `monitor_sys_gen_server_setting_report_enabled` int NULL DEFAULT NULL COMMENT '是否需要上报',
  `monitor_sys_gen_server_setting_data_report_method` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据上报方式',
  `monitor_sys_gen_server_setting_prometheus_host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Prometheus服务器地址',
  `monitor_sys_gen_server_setting_prometheus_port` int NULL DEFAULT NULL COMMENT 'Prometheus服务器端口',
  `monitor_sys_gen_server_setting_metrics_retention_days` int NULL DEFAULT NULL COMMENT '指标存储时间(天)',
  `monitor_sys_gen_server_setting_cpu_retention_days` int NULL DEFAULT NULL COMMENT 'CPU指标存储时间(天)',
  `monitor_sys_gen_server_setting_memory_retention_days` int NULL DEFAULT NULL COMMENT '内存指标存储时间(天)',
  `monitor_sys_gen_server_setting_disk_retention_days` int NULL DEFAULT NULL COMMENT '磁盘指标存储时间(天)',
  `monitor_sys_gen_server_setting_network_retention_days` int NULL DEFAULT NULL COMMENT '网络指标存储时间(天)',
  `monitor_sys_gen_server_setting_monitor_interval` int NULL DEFAULT NULL COMMENT '监控间隔(秒)',
  `monitor_sys_gen_server_setting_monitor_enabled` int NULL DEFAULT NULL COMMENT '是否启用监控',
  `monitor_sys_gen_server_setting_cpu_alert_threshold` decimal(11, 2) NULL DEFAULT NULL COMMENT 'CPU告警阈值 (百分比)',
  `monitor_sys_gen_server_setting_memory_alert_threshold` decimal(11, 2) NULL DEFAULT NULL COMMENT '内存告警阈值 (百分比)',
  `monitor_sys_gen_server_setting_disk_alert_threshold` decimal(11, 2) NULL DEFAULT NULL COMMENT '磁盘告警阈值 (百分比)',
  `monitor_sys_gen_server_setting_network_alert_threshold` decimal(11, 2) NULL DEFAULT NULL COMMENT '网络告警阈值 (Mbps)',
  `monitor_sys_gen_server_setting_response_time_alert_threshold` bigint NULL DEFAULT NULL COMMENT '响应时间告警阈值 (毫秒)',
  `monitor_sys_gen_server_setting_alert_enabled` int NULL DEFAULT NULL COMMENT '是否启用告警',
  `monitor_sys_gen_server_setting_alert_notification_method` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '告警通知方式',
  `monitor_sys_gen_server_setting_alert_notification_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '告警通知地址',
  `monitor_sys_gen_server_setting_alert_silence_duration` int NULL DEFAULT NULL COMMENT '告警静默时间 (分钟)',
  `monitor_sys_gen_server_setting_auto_recovery_enabled` int NULL DEFAULT NULL COMMENT '是否启用自动恢复通知',
  `monitor_sys_gen_server_setting_data_collection_frequency` int NULL DEFAULT NULL COMMENT '数据收集频率 (秒)',
  `monitor_sys_gen_server_setting_performance_suggestion_enabled` int NULL DEFAULT NULL COMMENT '是否启用性能优化建议',
  `monitor_sys_gen_server_setting_custom_tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '自定义标签 (JSON格式)',
  `monitor_sys_gen_server_setting_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置描述',
  `monitor_sys_gen_server_setting_status` int NULL DEFAULT NULL COMMENT '配置状态',
  `monitor_sys_gen_server_setting_docker_enabled` int NULL DEFAULT NULL COMMENT '是否支持Docker',
  `monitor_sys_gen_server_setting_docker_monitor_enabled` int NULL DEFAULT NULL COMMENT '是否启用Docker监控',
  `monitor_sys_gen_server_setting_docker_api_version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Docker API版本',
  `monitor_sys_gen_server_setting_docker_connection_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Docker连接方式',
  `monitor_sys_gen_server_setting_docker_host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Docker TCP连接地址',
  `monitor_sys_gen_server_setting_docker_port` int NULL DEFAULT NULL COMMENT 'Docker TCP连接端口',
  `monitor_sys_gen_server_setting_docker_socket_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Docker Socket路径',
  `monitor_sys_gen_server_setting_docker_tls_enabled` int NULL DEFAULT NULL COMMENT 'Docker TLS验证',
  `monitor_sys_gen_server_setting_docker_cert_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Docker证书路径',
  `monitor_sys_gen_server_setting_proxy_enabled` int NULL DEFAULT NULL COMMENT '是否使用代理',
  `monitor_sys_gen_server_setting_proxy_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '代理类型',
  `monitor_sys_gen_server_setting_proxy_host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '代理服务器地址',
  `monitor_sys_gen_server_setting_proxy_port` int NULL DEFAULT NULL COMMENT '代理服务器端口',
  `monitor_sys_gen_server_setting_proxy_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '代理用户名',
  `monitor_sys_gen_server_setting_proxy_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '代理密码',
  `monitor_sys_gen_server_setting_process_monitor_enabled` int NULL DEFAULT NULL COMMENT '是否启用进程监控',
  `monitor_sys_gen_server_setting_service_monitor_enabled` int NULL DEFAULT NULL COMMENT '是否启用服务监控',
  `monitor_sys_gen_server_setting_port_monitor_enabled` int NULL DEFAULT NULL COMMENT '是否启用端口监控',
  `monitor_sys_gen_server_setting_monitor_ports` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '监控的端口列表 (逗号分隔)',
  `monitor_sys_gen_server_setting_connection_timeout` int NULL DEFAULT NULL COMMENT '连接超时时间 (秒)',
  `monitor_sys_gen_server_setting_read_timeout` int NULL DEFAULT NULL COMMENT '读取超时时间 (秒)',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  `monitor_sys_gen_server_setting_prometheus_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Prometheus服务器完整URL',
  `monitor_sys_gen_server_setting_prometheus_query_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Prometheus查询路径',
  `monitor_sys_gen_server_setting_prometheus_auth_enabled` int NULL DEFAULT NULL COMMENT 'Prometheus认证是否启用',
  `monitor_sys_gen_server_setting_prometheus_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Prometheus认证用户名',
  `monitor_sys_gen_server_setting_prometheus_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Prometheus认证密码',
  `monitor_sys_gen_server_setting_prometheus_timeout` int NULL DEFAULT NULL COMMENT 'Prometheus查询超时时间(秒)',
  `monitor_sys_gen_server_setting_prometheus_labels` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Prometheus标签过滤器',
  `monitor_sys_gen_server_setting_port_check_interval` int NULL DEFAULT NULL COMMENT '端口检测间隔时间(秒)',
  `monitor_sys_gen_server_setting_online_check_enabled` int NULL DEFAULT NULL COMMENT '是否启用在线状态检测',
  `monitor_sys_gen_server_setting_online_check_interval` int NULL DEFAULT NULL COMMENT '在线状态检测间隔时间(秒)',
  `monitor_sys_gen_server_setting_latency_check_enabled` int NULL DEFAULT NULL COMMENT '是否启用延迟检测',
  `monitor_sys_gen_server_setting_latency_check_interval` int NULL DEFAULT NULL COMMENT '延迟检测间隔时间(秒)',
  `monitor_sys_gen_server_setting_docker_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Docker API 基本认证用户名（可选）',
  `monitor_sys_gen_server_setting_docker_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Docker API 基本认证密码（可选）',
  `monitor_sys_gen_server_setting_docker_connect_timeout_millis` int NULL DEFAULT NULL COMMENT 'Docker API 连接超时时间（毫秒，可选，默认30000）',
  `monitor_sys_gen_server_setting_docker_stats_interval` int NULL DEFAULT NULL COMMENT 'Docker状态推送间隔(秒)',
  `monitor_sys_gen_server_setting_file_management_mode` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件管理方式(NONE:不启用, SSH:SSH模式, API:API模式, NODE:NODE模式, LOCAL:本地模式)',
  `monitor_sys_gen_server_setting_file_management_api_config` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件管理API配置(JSON格式)',
  `monitor_sys_gen_server_setting_file_management_node_client` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '选择的NODE客户端ID(仅在NODE模式下使用)',
  `monitor_sys_gen_server_setting_file_management_enabled` int NULL DEFAULT NULL COMMENT '是否启用文件管理',
  `monitor_sys_gen_server_setting_file_management_timeout` int NULL DEFAULT NULL COMMENT '文件管理操作超时时间 (秒)',
  `monitor_sys_gen_server_setting_file_management_max_retries` int NULL DEFAULT NULL COMMENT '文件管理最大重试次数',
  `monitor_sys_gen_server_setting_client_health_check_interval` int NULL DEFAULT NULL COMMENT '客户端健康状态检查间隔 (秒)',
  `monitor_sys_gen_server_setting_client_health_timeout` int NULL DEFAULT NULL COMMENT '客户端健康状态超时时间 (秒)',
  `monitor_sys_gen_server_setting_online_check_timeout` int NULL DEFAULT NULL COMMENT '在线状态检查超时时间(ms)',
  PRIMARY KEY (`monitor_sys_gen_server_setting_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_server_setting_backup`;
CREATE TABLE `monitor_sys_gen_server_setting_backup`  (
  `monitor_sys_gen_server_setting_backup_id` bigint NOT NULL AUTO_INCREMENT COMMENT '备份ID',
  `backup_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备份名称',
  `backup_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备份描述',
  `backup_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备份类型: MANUAL/AUTO/SCHEDULED',
  `server_ids` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备份的服务器ID列表（JSON格式）',
  `backup_scope` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备份范围: ALL/SELECTED',
  `backup_data` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备份数据（JSON格式）',
  `backup_format` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备份格式: JSON/YAML',
  `server_count` int NULL DEFAULT NULL COMMENT '备份的服务器数量',
  `backup_size` bigint NULL DEFAULT NULL COMMENT '备份数据大小（字节）',
  `backup_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备份状态: SUCCESS/FAILED/PARTIAL',
  `backup_error` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备份错误信息',
  `create_user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建用户',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_server_setting_backup_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_server_setting_history`;
CREATE TABLE `monitor_sys_gen_server_setting_history`  (
  `monitor_sys_gen_server_setting_history_id` bigint NOT NULL AUTO_INCREMENT COMMENT '历史记录ID',
  `monitor_sys_gen_server_id` int NULL DEFAULT NULL COMMENT '服务器ID',
  `monitor_sys_gen_server_setting_id` bigint NULL DEFAULT NULL COMMENT '配置ID',
  `change_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '变更类型: CREATE/UPDATE/DELETE',
  `change_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '变更描述',
  `change_user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '变更用户',
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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_shell`;
CREATE TABLE `monitor_sys_gen_shell`  (
  `shell_id` int NOT NULL AUTO_INCREMENT,
  `shell_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本名称',
  `shell_script_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本路径',
  `shell_log_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志路径',
  `shell_args` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本参数',
  `shell_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '说明',
  `shell_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本内容',
  `gen_id` int NULL DEFAULT NULL COMMENT 'gen表ID',
  `shell_status` int NULL DEFAULT 0 COMMENT '状态,0: 待开启, 1:开启',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`shell_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'SHELL管理' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_table`;
CREATE TABLE `monitor_sys_gen_table`  (
  `tab_id` int NOT NULL AUTO_INCREMENT,
  `tab_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '表名称',
  `tab_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `gen_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属数据源名称, 用于处理内部数据源',
  `gen_id` int NULL DEFAULT NULL COMMENT '所属数据源',
  `tab_class_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '实体类名称',
  `tab_package_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '生成包路径',
  `tab_module_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '生成模块名',
  `tab_business_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '生成业务名',
  `tab_function_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '生成功能名',
  `tab_function_author` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '生成功能作者',
  `tab_gen_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '生成代码方式（0zip压缩包 1自定义路径）',
  `tab_gen_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '生成路径（不填默认项目路径）',
  `tab_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `tab_tpl_category` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '使用的模板（crud单表操作 tree树表操作 sub主子表操作）',
  PRIMARY KEY (`tab_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '生成器表' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_template`;
CREATE TABLE `monitor_sys_gen_template`  (
  `template_id` int NOT NULL AUTO_INCREMENT,
  `template_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板名称',
  `template_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板类型',
  `gen_id` int NULL DEFAULT NULL COMMENT '工具ID',
  `template_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目录',
  `template_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`template_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '生成器模板' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `node_system`;
CREATE TABLE `node_system`  (
  `node_system_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '节点ID - MD5(IP+PORT)',
  `node_system_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '节点名称',
  `node_system_application_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '应用名称',
  `node_system_ip_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `node_system_port` int NULL DEFAULT NULL COMMENT '端口号',
  `node_system_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '节点状态：ONLINE-在线, OFFLINE-离线, CONNECTING-连接中, ERROR-异常, MAINTENANCE-维护中',
  `node_system_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '节点类型',
  `node_system_version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务版本',
  `node_system_healthy` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否健康',
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
  `node_system_monitor_enabled` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否启用监控',
  `node_system_monitor_interval` int NULL DEFAULT NULL COMMENT '监控间隔（秒）',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `process_list`;
CREATE TABLE `process_list`  (
  `process_list_id` int NOT NULL AUTO_INCREMENT COMMENT '进程列表ID',
  `process_list_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '进程列表名称',
  `process_list_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '进程列表描述',
  `process_list_start_command` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '启动命令',
  `process_list_stop_command` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '停止命令',
  `process_list_restart_command` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '重启命令',
  `process_list_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '进程列表状态,0:停用,1:启用',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`process_list_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `server_info`;
CREATE TABLE `server_info`  (
  `ssh_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SSH ID',
  `install_id` int NULL DEFAULT NULL COMMENT '安装记录ID',
  `install_status` int NULL DEFAULT NULL COMMENT '安装状态',
  `install_status_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '安装状态描述',
  `run_status` int NULL DEFAULT NULL COMMENT '运行状态',
  `run_status_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '运行状态描述',
  `install_time` bigint NULL DEFAULT NULL COMMENT '安装时间',
  `install_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '安装路径',
  `install_version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '安装版本',
  `server_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务器名称',
  `server_host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务器主机地址',
  `host_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '主机名',
  `os_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作系统'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `server_info_builder`;
CREATE TABLE `server_info_builder`  (
  `ssh_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `install_id` int NULL DEFAULT NULL,
  `install_status` int NULL DEFAULT NULL,
  `install_status_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `run_status` int NULL DEFAULT NULL,
  `run_status_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `install_time` bigint NULL DEFAULT NULL,
  `install_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `install_version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `server_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `server_host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `host_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `os_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `soft_service`;
CREATE TABLE `soft_service`  (
  `soft_service_id` int NOT NULL AUTO_INCREMENT,
  `soft_service_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `soft_service_logo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'logo',
  `soft_service_category` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类',
  `soft_service_os` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作系统',
  `soft_service_install_command` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '安装命令',
  `soft_service_install_command_type` int NULL DEFAULT NULL COMMENT '安装命令类型（0:单行命令，1:脚本）',
  `soft_service_install_success_flag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '安装命令成功标识，多个标识用逗号分隔，支持通配符',
  `soft_service_install_failure_flag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '安装命令失败标识，多个标识用逗号分隔，支持通配符，优先级高于成功标识',
  `soft_service_uninstall_command` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '卸载命令',
  `soft_service_uninstall_command_type` int NULL DEFAULT NULL COMMENT '卸载命令类型（0:单行命令，1:脚本）',
  `soft_service_uninstall_success_flag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '卸载命令成功标识，多个标识用逗号分隔，支持通配符',
  `soft_service_uninstall_failure_flag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '卸载命令失败标识，多个标识用逗号分隔，支持通配符，优先级高于成功标识',
  `soft_service_start_command` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '启动命令',
  `soft_service_start_command_type` int NULL DEFAULT NULL COMMENT '启动命令类型（0:单行命令，1:脚本）',
  `soft_service_start_success_flag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '启动命令成功标识，多个标识用逗号分隔，支持通配符',
  `soft_service_start_failure_flag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '启动命令失败标识，多个标识用逗号分隔，支持通配符，优先级高于成功标识',
  `soft_service_stop_command` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '停止命令',
  `soft_service_stop_command_type` int NULL DEFAULT NULL COMMENT '停止命令类型（0:单行命令，1:脚本）',
  `soft_service_stop_success_flag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '停止命令成功标识，多个标识用逗号分隔，支持通配符',
  `soft_service_stop_failure_flag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '停止命令失败标识，多个标识用逗号分隔，支持通配符，优先级高于成功标识',
  `soft_service_restart_command` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '重启命令',
  `soft_service_restart_command_type` int NULL DEFAULT NULL COMMENT '重启命令类型（0:单行命令，1:脚本）',
  `soft_service_restart_success_flag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '重启命令成功标识，多个标识用逗号分隔，支持通配符',
  `soft_service_restart_failure_flag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '重启命令失败标识，多个标识用逗号分隔，支持通配符，优先级高于成功标识',
  `soft_service_status_check_command` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '服务状态检查命令',
  `soft_service_status_check_command_type` int NULL DEFAULT NULL COMMENT '服务状态检查命令类型（0:单行命令，1:脚本）',
  `soft_service_status_check_success_flag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务状态检查成功标识，多个标识用逗号分隔，支持通配符',
  `soft_service_status_check_failure_flag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务状态检查失败标识，多个标识用逗号分隔，支持通配符，优先级高于成功标识',
  `soft_service_installed` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否安装',
  `soft_service_log_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志路径',
  `soft_service_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `soft_service_version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '版本号',
  `soft_service_download_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '下载地址',
  `soft_service_tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签',
  `install_count` int NULL DEFAULT NULL COMMENT '安装次数',
  `favorite_count` int NULL DEFAULT NULL COMMENT '收藏次数',
  `soft_service_default_port` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '默认端口',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`soft_service_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `soft_service_install`;
CREATE TABLE `soft_service_install`  (
  `install_id` int NOT NULL AUTO_INCREMENT COMMENT '安装记录ID',
  `ssh_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SSH ID，关联MachineSshModel的ID',
  `soft_service_id` int NULL DEFAULT NULL COMMENT '软件服务ID',
  `install_status` int NULL DEFAULT NULL COMMENT '安装状态：0-未安装，1-安装中，2-安装成功，3-安装失败',
  `install_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '安装路径',
  `install_log_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '安装日志路径',
  `service_log_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务日志路径',
  `install_version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '安装版本',
  `install_time` bigint NULL DEFAULT NULL COMMENT '安装时间',
  `install_last_start_time` bigint NULL DEFAULT NULL COMMENT '最后一次启动时间',
  `install_last_stop_time` bigint NULL DEFAULT NULL COMMENT '最后一次停止时间',
  `install_run_status` int NULL DEFAULT NULL COMMENT '运行状态：0-未运行，1-运行中',
  `install_config_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置文件目录',
  `install_last_check_time` bigint NULL DEFAULT NULL COMMENT '最后一次状态检查时间',
  `command_timeout` int NULL DEFAULT NULL COMMENT '命令执行超时时间（毫秒），如果未设置，则使用默认超时时间',
  `install_command_timeout` int NULL DEFAULT NULL COMMENT '安装命令执行超时时间（毫秒），如果未设置，则使用通用超时时间',
  `uninstall_command_timeout` int NULL DEFAULT NULL COMMENT '卸载命令执行超时时间（毫秒），如果未设置，则使用通用超时时间',
  `start_command_timeout` int NULL DEFAULT NULL COMMENT '启动命令执行超时时间（毫秒），如果未设置，则使用通用超时时间',
  `stop_command_timeout` int NULL DEFAULT NULL COMMENT '停止命令执行超时时间（毫秒），如果未设置，则使用通用超时时间',
  `restart_command_timeout` int NULL DEFAULT NULL COMMENT '重启命令执行超时时间（毫秒），如果未设置，则使用通用超时时间',
  `status_check_command_timeout` int NULL DEFAULT NULL COMMENT '状态检查命令执行超时时间（毫秒），如果未设置，则使用通用超时时间',
  `soft_service_install_command` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '安装命令',
  `soft_service_install_command_type` int NULL DEFAULT NULL COMMENT '安装命令类型（0:单行命令，1:脚本）',
  `soft_service_uninstall_command` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '卸载命令',
  `soft_service_uninstall_command_type` int NULL DEFAULT NULL COMMENT '卸载命令类型（0:单行命令，1:脚本）',
  `soft_service_start_command` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '启动命令',
  `soft_service_start_command_type` int NULL DEFAULT NULL COMMENT '启动命令类型（0:单行命令，1:脚本）',
  `soft_service_stop_command` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '停止命令',
  `soft_service_stop_command_type` int NULL DEFAULT NULL COMMENT '停止命令类型（0:单行命令，1:脚本）',
  `soft_service_restart_command` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '重启命令',
  `soft_service_restart_command_type` int NULL DEFAULT NULL COMMENT '重启命令类型（0:单行命令，1:脚本）',
  `soft_service_status_check_command` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '服务状态检查命令',
  `soft_service_status_check_command_type` int NULL DEFAULT NULL COMMENT '服务状态检查命令类型（0:单行命令，1:脚本）',
  `soft_service_status_check_success_flag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务状态检查成功标识，多个标识用逗号分隔，支持通配符',
  `soft_service_status_check_failure_flag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务状态检查失败标识，多个标识用逗号分隔，支持通配符，优先级高于成功标识',
  `soft_service_install_success_flag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '软件服务安装命令成功标识，多个标识用逗号分隔，支持通配符',
  `soft_service_install_failure_flag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '软件服务安装命令失败标识，多个标识用逗号分隔，支持通配符，优先级高于成功标识',
  `soft_service_uninstall_success_flag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '软件服务卸载命令成功标识，多个标识用逗号分隔，支持通配符',
  `soft_service_uninstall_failure_flag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '软件服务卸载命令失败标识，多个标识用逗号分隔，支持通配符，优先级高于成功标识',
  `soft_service_start_success_flag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '软件服务启动命令成功标识，多个标识用逗号分隔，支持通配符',
  `soft_service_start_failure_flag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '软件服务启动命令失败标识，多个标识用逗号分隔，支持通配符，优先级高于成功标识',
  `soft_service_stop_success_flag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '软件服务停止命令成功标识，多个标识用逗号分隔，支持通配符',
  `soft_service_stop_failure_flag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '软件服务停止命令失败标识，多个标识用逗号分隔，支持通配符，优先级高于成功标识',
  `soft_service_restart_success_flag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '软件服务重启命令成功标识，多个标识用逗号分隔，支持通配符',
  `soft_service_restart_failure_flag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '软件服务重启命令失败标识，多个标识用逗号分隔，支持通配符，优先级高于成功标识',
  `service_config` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '服务配置项，JSON格式存储',
  `install_path_custom` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '自定义安装路径',
  `install_port` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '安装端口',
  `install_default_port` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '默认端口',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`install_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `soft_service_log`;
CREATE TABLE `soft_service_log`  (
  `soft_service_log_id` int NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `install_id` int NULL DEFAULT NULL COMMENT '安装ID',
  `soft_service_id` int NULL DEFAULT NULL COMMENT '软件服务ID',
  `ssh_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SSH ID',
  `log_type` int NULL DEFAULT NULL COMMENT '日志类型：1-安装，2-卸载，3-启动，4-停止，5-重启，6-状态检查',
  `log_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志内容',
  `log_status` int NULL DEFAULT NULL COMMENT '日志状态：0-进行中，1-成功，2-失败',
  `log_time` bigint NULL DEFAULT NULL COMMENT '日志时间',
  `log_file_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志文件路径',
  `log_operation_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志操作类型：install, uninstall, start, stop, restart, status',
  `log_duration` bigint NULL DEFAULT NULL COMMENT '日志执行耗时（毫秒）',
  `log_command` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志执行命令',
  `log_error` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志执行错误信息',
  `log_end_time` bigint NULL DEFAULT NULL COMMENT '日志结束时间',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`soft_service_log_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `soft_service_version`;
CREATE TABLE `soft_service_version`  (
  `soft_service_id` int NOT NULL AUTO_INCREMENT COMMENT '软件服务ID',
  `version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '版本号',
  `download_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '下载地址',
  `release_time` datetime(6) NULL DEFAULT NULL COMMENT '发布时间',
  `version_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '版本描述',
  `is_current` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否为当前版本',
  `is_installable` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否可以安装，根据安装脚本是否存在来决定',
  `install_command` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '安装命令',
  PRIMARY KEY (`soft_service_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `soft_service_version_builder`;
CREATE TABLE `soft_service_version_builder`  (
  `soft_service_id` int NOT NULL AUTO_INCREMENT,
  `version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `download_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `release_time` datetime(6) NULL DEFAULT NULL,
  `version_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `is_current` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `is_installable` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `install_command` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`soft_service_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `system_data_backup_record`;
CREATE TABLE `system_data_backup_record`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '记录ID',
  `setting_id` int NULL DEFAULT NULL COMMENT '配置ID',
  `event` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '事件类型',
  `timestamp` bigint NULL DEFAULT NULL COMMENT '时间戳',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称（文件名）',
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备份文件路径/地址',
  `size` bigint NULL DEFAULT NULL COMMENT '大小（字节），未知为-1',
  `message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述/消息'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `system_data_email_history`;
CREATE TABLE `system_data_email_history`  (
  `system_data_email_history_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `system_data_email_history_setting_id` int NULL DEFAULT NULL COMMENT '关联的数据源配置ID',
  `system_data_email_history_message_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮件唯一标识符',
  `system_data_email_history_subject` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮件主题',
  `system_data_email_history_from` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发件人',
  `system_data_email_history_to` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收件人',
  `system_data_email_history_cc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '抄送人',
  `system_data_email_history_bcc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密送人',
  `system_data_email_history_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮件内容（纯文本）',
  `system_data_email_history_content_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮件内容类型',
  `system_data_email_history_html_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮件内容（HTML）',
  `system_data_email_history_sent_date` datetime(6) NULL DEFAULT NULL COMMENT '邮件发送时间',
  `system_data_email_history_received_date` datetime(6) NULL DEFAULT NULL COMMENT '邮件接收时间',
  `system_data_email_history_size` bigint NULL DEFAULT NULL COMMENT '邮件大小（字节）',
  `system_data_email_history_is_read` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否已读',
  `system_data_email_history_is_starred` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否收藏',
  `system_data_email_history_is_important` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否重要',
  `system_data_email_history_is_deleted` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否已删除',
  `system_data_email_history_labels` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮件标签（多个标签用逗号分隔）',
  `system_data_email_history_folder` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮件文件夹',
  `system_data_email_history_priority` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮件优先级',
  `system_data_email_history_attachment_count` int NULL DEFAULT NULL COMMENT '附件数量',
  `system_data_email_history_attachments` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '附件信息（JSON格式）',
  `system_data_email_history_headers` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮件头信息（JSON格式）',
  `system_data_email_history_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮件类型',
  `system_data_email_history_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮件状态',
  `system_data_email_history_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `system_data_email_history_sync_status` int NULL DEFAULT NULL COMMENT '同步状态：0-未同步，1-已同步，2-同步失败',
  `system_data_email_history_sync_time` datetime(6) NULL DEFAULT NULL COMMENT '同步时间',
  `system_data_email_history_error_message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误信息',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`system_data_email_history_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `system_data_field_comment`;
CREATE TABLE `system_data_field_comment`  (
  `system_data_field_comment_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `system_data_field_comment_setting_id` int NULL DEFAULT NULL COMMENT '数据源配置ID',
  `system_data_field_comment_node_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '节点路径（唯一键之一）',
  `system_data_field_comment_database_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '库/Database 名称',
  `system_data_field_comment_table_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '表名',
  `system_data_field_comment_column_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '列名',
  `system_data_field_comment_comment` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '注释内容',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`system_data_field_comment_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `system_data_log_record`;
CREATE TABLE `system_data_log_record`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '记录ID',
  `setting_id` int NULL DEFAULT NULL COMMENT '配置ID',
  `level` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志级别',
  `timestamp` bigint NULL DEFAULT NULL COMMENT '时间戳',
  `message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '内容',
  `from` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `system_data_setting`;
CREATE TABLE `system_data_setting`  (
  `system_data_setting_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `system_data_setting_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `system_data_setting_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '类型：JDBC/REDIS/ZK 等',
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
  `system_data_setting_enabled` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否启用',
  `system_data_setting_config` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '控制台权限/个性化配置(JSON)',
  `system_data_setting_protocol` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '协议',
  `system_data_setting_driver_class` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'JDBC 驱动类名',
  `system_data_setting_driver_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'JDBC 驱动文件路径',
  `system_data_setting_image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '远程图片地址',
  `system_data_setting_backup_status` int NULL DEFAULT NULL COMMENT '备份状态,0:未启动, 1:已启动',
  `system_data_setting_idle` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否支持监听',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`system_data_setting_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `system_data_setting_item`;
CREATE TABLE `system_data_setting_item`  (
  `system_data_setting_item_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `system_server`;
CREATE TABLE `system_server`  (
  `system_server_id` int NOT NULL AUTO_INCREMENT COMMENT '系统服务器ID',
  `system_server_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '系统服务器名称',
  `system_server_host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '系统服务器主机',
  `system_server_port` int NULL DEFAULT NULL COMMENT '系统服务器端口',
  `system_server_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '系统服务器类型',
  `system_server_context_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '系统服务器上下文路径',
  `system_server_status` varchar(11) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '系统服务器启动状态',
  `system_server_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '系统服务器描述',
  `system_server_config` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '系统服务器配置',
  `system_server_auto_start` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '系统服务器是否自动启动',
  `system_server_max_connections` int NULL DEFAULT NULL COMMENT '系统服务器最大连接数',
  `system_server_timeout` int NULL DEFAULT NULL COMMENT '系统服务器超时时间',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`system_server_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `system_server_log`;
CREATE TABLE `system_server_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `server_id` int NULL DEFAULT NULL COMMENT '系统服务器ID',
  `filter_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '过滤器类型（SPI类型或类名）',
  `process_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '处理状态（例如：IP限流、黑名单拦截、白名单通过等）',
  `client_ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户端IP',
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
) ENGINE = InnoDB AUTO_INCREMENT = 190 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `system_server_setting`;
CREATE TABLE `system_server_setting`  (
  `system_server_setting_id` int NOT NULL AUTO_INCREMENT COMMENT '系统服务器配置ID',
  `system_server_setting_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '系统服务器配置名称',
  `system_server_setting_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '系统服务器配置类型',
  `system_server_setting_server_id` int NULL DEFAULT NULL COMMENT '关联的服务器ID',
  `system_server_setting_order` int NULL DEFAULT NULL COMMENT '排序字段',
  `system_server_setting_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置描述',
  `system_server_setting_enabled` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置状态',
  `system_server_setting_class_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置类名',
  `system_server_setting_version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置版本',
  `system_server_setting_config` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置JSON数据',
  `system_server_setting_filter_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '启动的时候生成',
  `system_server_setting_https_enabled` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否启用HTTPS',
  `system_server_setting_https_cert_type` varchar(11) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '证书类型: PEM/PFX/JKS',
  `system_server_setting_https_pem_cert` longblob NULL COMMENT 'PEM证书(BLOB)',
  `system_server_setting_https_pem_key` longblob NULL COMMENT 'PEM私钥(BLOB)',
  `system_server_setting_https_pem_key_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'PEM私钥密码',
  `system_server_setting_https_keystore` longblob NULL COMMENT 'Keystore容器(BLOB)',
  `system_server_setting_https_keystore_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Keystore密码',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`system_server_setting_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `system_server_setting_address_rate_limit`;
CREATE TABLE `system_server_setting_address_rate_limit`  (
  `system_server_setting_address_rate_limit_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `address_rate_limit_server_id` int NULL DEFAULT NULL COMMENT '服务器ID',
  `address_rate_limit_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地址(接口/路径)',
  `address_rate_limit_qps` int NULL DEFAULT NULL COMMENT '每秒请求阈值',
  `address_rate_limit_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '类型: RATE_LIMIT/WHITELIST/BLACKLIST',
  `address_rate_limit_enabled` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否启用',
  `address_rate_limit_setting_id` int NULL DEFAULT NULL COMMENT '关联的系统配置ID(SystemServerSettingId)',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`system_server_setting_address_rate_limit_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `system_server_setting_file_storage`;
CREATE TABLE `system_server_setting_file_storage`  (
  `system_server_setting_file_storage_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `file_storage_server_id` int NULL DEFAULT NULL COMMENT '服务器ID',
  `file_storage_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '存储类型: LOCAL/S3/MINIO/OSS',
  `file_storage_base_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '本地根路径',
  `file_storage_endpoint` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '对象存储 Endpoint',
  `file_storage_bucket` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Bucket',
  `file_storage_access_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'AccessKey',
  `file_storage_secret_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SecretKey',
  `file_storage_region` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Region',
  `file_storage_enabled` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否启用',
  `file_storage_connection_timeout` bigint NULL DEFAULT NULL COMMENT '连接超时',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`system_server_setting_file_storage_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `system_server_setting_ip_rate_limit`;
CREATE TABLE `system_server_setting_ip_rate_limit`  (
  `system_server_setting_ip_rate_limit_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `ip_rate_limit_server_id` int NULL DEFAULT NULL COMMENT '服务器ID',
  `ip_rate_limit_ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'IP地址(支持CIDR)',
  `ip_rate_limit_qps` int NULL DEFAULT NULL COMMENT '每秒请求阈值',
  `ip_rate_limit_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '类型: RATE_LIMIT/WHITELIST/BLACKLIST',
  `ip_rate_limit_enabled` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否启用',
  `ip_rate_limit_setting_id` int NULL DEFAULT NULL COMMENT '关联的系统配置ID(SystemServerSettingId)',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`system_server_setting_ip_rate_limit_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `system_server_setting_item`;
CREATE TABLE `system_server_setting_item`  (
  `system_server_setting_item_id` int NOT NULL AUTO_INCREMENT COMMENT '系统服务器配置项ID',
  `system_server_setting_item_setting_id` int NULL DEFAULT NULL COMMENT '关联的配置ID',
  `system_server_setting_item_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置项名称',
  `system_server_setting_item_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置项值',
  `system_server_setting_item_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置项描述',
  `system_server_setting_item_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置项类型',
  `system_server_setting_item_required` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否必填',
  `system_server_setting_item_default_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '默认值',
  `system_server_setting_item_validation_rule` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '验证规则',
  `system_server_setting_item_order` int NULL DEFAULT NULL COMMENT '排序字段',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`system_server_setting_item_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 115 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `system_server_setting_service_discovery`;
CREATE TABLE `system_server_setting_service_discovery`  (
  `system_server_setting_service_discovery_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `service_discovery_server_id` int NULL DEFAULT NULL COMMENT '服务器ID',
  `service_discovery_mode` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务发现模式: MONITOR/SPRING/TABLE/HAZELCAST',
  `service_discovery_balance` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '默认负载均衡策略',
  `service_discovery_bean_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Spring容器中的ServiceDiscovery Bean名称',
  `service_discovery_enabled` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否启用',
  `service_discovery_monitor_host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `service_discovery_monitor_port` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`system_server_setting_service_discovery_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `system_server_setting_service_discovery_mapping`;
CREATE TABLE `system_server_setting_service_discovery_mapping`  (
  `system_server_setting_service_discovery_mapping_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `service_discovery_server_id` int NULL DEFAULT NULL COMMENT '服务器ID',
  `service_discovery_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务名称',
  `service_discovery_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务地址',
  `service_discovery_weight` int NULL DEFAULT NULL COMMENT '服务权重',
  `service_discovery_enabled` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否启用',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`system_server_setting_service_discovery_mapping_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `system_soft`;
CREATE TABLE `system_soft`  (
  `system_soft_id` int NOT NULL AUTO_INCREMENT COMMENT '系统软件ID',
  `system_soft_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '软件名称',
  `system_soft_code` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '软件代码',
  `system_soft_category` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '软件分类',
  `system_soft_icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '软件图标',
  `system_soft_tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '软件标签',
  `system_soft_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '软件描述',
  `system_soft_registry_id` int NULL DEFAULT NULL COMMENT '镜像仓库ID',
  `system_soft_docker_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Docker镜像名称',
  `system_soft_default_install_method` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '默认安装方式',
  `system_soft_default_install_params` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '默认安装参数',
  `system_soft_status` int NULL DEFAULT NULL COMMENT '软件状态',
  `system_soft_is_official` int NULL DEFAULT NULL COMMENT '是否官方软件',
  `system_soft_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '软件备注',
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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `system_soft_container`;
CREATE TABLE `system_soft_container`  (
  `system_soft_container_id` int NOT NULL AUTO_INCREMENT COMMENT '系统软件容器ID',
  `system_soft_id` int NULL DEFAULT NULL COMMENT '关联的软件ID',
  `system_soft_version_id` int NULL DEFAULT NULL COMMENT '关联的软件版本ID',
  `system_server_id` int NULL DEFAULT NULL COMMENT '关联的服务器ID',
  `system_soft_image_id` int NULL DEFAULT NULL COMMENT '关联的镜像ID',
  `system_soft_container_docker_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Docker容器ID',
  `system_soft_container_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '容器名称',
  `system_soft_container_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '容器镜像',
  `system_soft_container_image_tag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '容器镜像标签',
  `system_soft_container_status` varchar(11) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '容器状态',
  `system_soft_container_ports` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '容器端口映射',
  `system_soft_container_env` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '容器环境变量',
  `system_soft_container_volumes` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '容器数据卷',
  `system_soft_container_networks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '容器网络',
  `system_soft_container_command` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '容器启动命令',
  `system_soft_container_args` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '容器启动参数',
  `system_soft_container_created_time` datetime(6) NULL DEFAULT NULL COMMENT '容器创建时间',
  `system_soft_container_started_time` datetime(6) NULL DEFAULT NULL COMMENT '容器启动时间',
  `system_soft_container_finished_time` datetime(6) NULL DEFAULT NULL COMMENT '容器结束时间',
  `system_soft_container_restart_count` int NULL DEFAULT NULL COMMENT '容器重启次数',
  `system_soft_container_config` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '容器配置信息',
  `system_soft_container_health_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '容器健康状态',
  `system_soft_container_auto_restart` int NULL DEFAULT NULL COMMENT '是否自动重启',
  `system_soft_container_cpu_limit` double NULL DEFAULT NULL COMMENT 'CPU使用限制',
  `system_soft_container_memory_limit` bigint NULL DEFAULT NULL COMMENT '内存使用限制',
  `system_soft_container_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '容器备注',
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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `system_soft_image`;
CREATE TABLE `system_soft_image`  (
  `system_soft_image_id` int NOT NULL AUTO_INCREMENT COMMENT '系统软件镜像ID',
  `system_soft_id` int NULL DEFAULT NULL COMMENT '关联的软件ID',
  `system_soft_image_server_id` int NULL DEFAULT NULL COMMENT '关联的服务器ID',
  `system_soft_image_server_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务器名称',
  `system_soft_image_image_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Docker镜像ID',
  `system_soft_image_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '镜像名称',
  `system_soft_image_tag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '镜像标签',
  `system_soft_image_full_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '镜像完整名称',
  `system_soft_image_repository_id` int NULL DEFAULT NULL COMMENT '镜像仓库地址ID',
  `system_soft_image_size` bigint NULL DEFAULT NULL COMMENT '镜像大小',
  `system_soft_image_created` datetime(6) NULL DEFAULT NULL COMMENT '镜像创建时间',
  `system_soft_image_architecture` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '镜像架构',
  `system_soft_image_os_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作系统类型',
  `system_soft_image_digest` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '镜像摘要',
  `system_soft_image_status` varchar(11) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '镜像状态',
  `system_soft_image_is_official` int NULL DEFAULT NULL COMMENT '是否官方镜像',
  `system_soft_image_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '镜像描述',
  `system_soft_image_tag_count` int NULL DEFAULT NULL COMMENT '标签数量',
  `system_soft_image_pull_count` bigint NULL DEFAULT NULL COMMENT '拉取次数',
  `system_soft_image_star_count` bigint NULL DEFAULT NULL COMMENT '星标数量',
  `system_soft_image_last_pulled` datetime(6) NULL DEFAULT NULL COMMENT '最后拉取时间',
  `system_soft_image_last_pushed` datetime(6) NULL DEFAULT NULL COMMENT '最后推送时间',
  `system_soft_image_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '镜像备注',
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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `system_soft_record`;
CREATE TABLE `system_soft_record`  (
  `system_soft_record_id` int NOT NULL AUTO_INCREMENT COMMENT '系统软件记录ID',
  `system_soft_id` int NULL DEFAULT NULL COMMENT '关联的软件ID',
  `system_soft_version_id` int NULL DEFAULT NULL COMMENT '关联的软件版本ID',
  `system_server_id` int NULL DEFAULT NULL COMMENT '关联的服务器ID',
  `system_soft_record_operation_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作类型',
  `system_soft_record_message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作消息',
  `system_soft_record_method` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作方法',
  `system_soft_record_env_params` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '环境参数',
  `system_soft_record_params` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作参数',
  `system_soft_record_time` datetime(6) NULL DEFAULT NULL COMMENT '记录时间',
  `system_soft_record_status` int NULL DEFAULT NULL COMMENT '操作状态',
  `system_soft_record_user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作用户',
  `system_soft_record_container_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联的容器ID',
  `system_soft_record_start_time` datetime(6) NULL DEFAULT NULL COMMENT '操作开始时间',
  `system_soft_record_end_time` datetime(6) NULL DEFAULT NULL COMMENT '操作结束时间',
  `system_soft_record_duration` bigint NULL DEFAULT NULL COMMENT '操作耗时',
  `system_soft_record_error_message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误信息',
  `system_soft_record_result` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作结果',
  `system_soft_record_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '记录备注',
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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `system_soft_registry`;
CREATE TABLE `system_soft_registry`  (
  `system_soft_registry_id` int NOT NULL AUTO_INCREMENT COMMENT '系统软件镜像仓库ID',
  `system_soft_registry_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '仓库名称',
  `system_soft_registry_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '仓库类型',
  `system_soft_registry_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '仓库地址',
  `system_soft_registry_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '仓库用户名',
  `system_soft_registry_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '仓库密码',
  `system_soft_registry_email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '仓库邮箱',
  `system_soft_registry_server_id` int NULL DEFAULT NULL COMMENT '绑定服务器ID',
  `system_soft_registry_ssl_enabled` int NULL DEFAULT NULL COMMENT '是否启用SSL',
  `system_soft_registry_support_sync` int NULL DEFAULT NULL COMMENT '是否支持同步',
  `system_soft_registry_timeout` int NULL DEFAULT NULL COMMENT '连接超时时间(秒)',
  `system_soft_registry_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '仓库描述',
  `system_soft_registry_config` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '仓库配置信息',
  `system_soft_registry_last_connect_time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后连接时间',
  `system_soft_registry_connect_status` int NULL DEFAULT NULL COMMENT '连接状态',
  `system_soft_registry_error_message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '连接错误信息',
  `system_soft_registry_sort` int NULL DEFAULT NULL COMMENT '排序号',
  `system_soft_registry_status` int NULL DEFAULT NULL COMMENT '状态(0:禁用, 1:启用)',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  `system_soft_registry_active` int NULL DEFAULT NULL COMMENT '是否激活，激活的仓库用于软件搜索',
  PRIMARY KEY (`system_soft_registry_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'docker软件库' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'docker软件版本' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `maintenance_group`;
CREATE TABLE `maintenance_group`
(
    `maintenance_group_id`     int(11)                                                       NOT NULL AUTO_INCREMENT COMMENT '维护组ID',
    `maintenance_group_name`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '维护组名称',
    `maintenance_group_desc`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '维护组描述',
    `maintenance_group_status` int(11)                                                       NULL DEFAULT 1 COMMENT '状态,0:停用,1:启用',
    `create_time`              datetime                                                      NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`              datetime                                                      NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`                varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '创建者',
    `update_by`                varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`maintenance_group_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '维护组表'
  ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `maintenance_host`;
CREATE TABLE `maintenance_host`
(
    `maintenance_host_id`       int(11)                                                       NOT NULL AUTO_INCREMENT COMMENT '维护主机ID',
    `maintenance_group_id`      int(11)                                                       NULL DEFAULT NULL COMMENT '维护组ID',
    `maintenance_host_address`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '主机地址',
    `maintenance_host_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账号',
    `maintenance_host_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
    `maintenance_host_port`     int(11)                                                       NULL DEFAULT 22 COMMENT '端口',
    `maintenance_host_status`   int(11)                                                       NULL DEFAULT 1 COMMENT '状态,0:停用,1:启用',
    `create_time`               datetime                                                      NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`               datetime                                                      NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`                 varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '创建者',
    `update_by`                 varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`maintenance_host_id`) USING BTREE,
    INDEX `idx_group_id` (`maintenance_group_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '维护主机表'
  ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `maintenance_script`;
CREATE TABLE `maintenance_script`
(
    `maintenance_script_id`      int(11)                                                       NOT NULL AUTO_INCREMENT COMMENT '脚本ID',
    `maintenance_group_id`       int(11)                                                       NULL DEFAULT NULL COMMENT '维护组ID',
    `maintenance_script_name`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本名称',
    `maintenance_script_path`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本路径',
    `maintenance_script_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci         NULL COMMENT '脚本内容',
    `maintenance_script_desc`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本描述',
    `maintenance_script_status`  int(11)                                                       NULL DEFAULT 1 COMMENT '状态,0:停用,1:启用',
    `create_time`                datetime                                                      NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`                datetime                                                      NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`                  varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '创建者',
    `update_by`                  varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`maintenance_script_id`) USING BTREE,
    INDEX `idx_group_id` (`maintenance_group_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '维护脚本表'
  ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `maintenance_file`;
CREATE TABLE `maintenance_file`
(
    `maintenance_file_id`          int(11)                                                       NOT NULL AUTO_INCREMENT COMMENT '文件ID',
    `maintenance_group_id`         int(11)                                                       NULL DEFAULT NULL COMMENT '维护组ID',
    `maintenance_file_name`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件名称',
    `maintenance_file_path`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件路径',
    `maintenance_file_type`        varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '文件类型',
    `maintenance_file_size`        bigint(20)                                                    NULL DEFAULT NULL COMMENT '文件大小',
    `maintenance_file_is_extract`  int(11)                                                       NULL DEFAULT 0 COMMENT '是否解压,0:否,1:是',
    `maintenance_file_is_override` int(11)                                                       NULL DEFAULT 0 COMMENT '是否覆盖,0:否,1:是',
    `maintenance_file_status`      int(11)                                                       NULL DEFAULT 1 COMMENT '状态,0:停用,1:启用',
    `create_time`                  datetime                                                      NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`                  datetime                                                      NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`                    varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '创建者',
    `update_by`                    varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`maintenance_file_id`) USING BTREE,
    INDEX `idx_group_id` (`maintenance_group_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '维护文件表'
  ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `maintenance_task`;
CREATE TABLE `maintenance_task`
(
    `maintenance_task_id`     int(11)                                                      NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `maintenance_group_id`    int(11)                                                      NULL DEFAULT NULL COMMENT '维护组ID',
    `maintenance_host_id`     int(11)                                                      NULL DEFAULT NULL COMMENT '维护主机ID',
    `maintenance_script_id`   int(11)                                                      NULL DEFAULT NULL COMMENT '脚本ID',
    `maintenance_file_id`     int(11)                                                      NULL DEFAULT NULL COMMENT '文件ID',
    `maintenance_task_type`   varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务类型,SCRIPT:脚本,FILE:文件',
    `maintenance_task_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务状态,PENDING:待执行,RUNNING:执行中,SUCCESS:成功,FAILED:失败',
    `maintenance_task_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci        NULL COMMENT '任务结果',
    `maintenance_task_error`  text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci        NULL COMMENT '错误信息',
    `create_time`             datetime                                                     NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`             datetime                                                     NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`               varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建者',
    `update_by`               varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`maintenance_task_id`) USING BTREE,
    INDEX `idx_group_id` (`maintenance_group_id`) USING BTREE,
    INDEX `idx_host_id` (`maintenance_host_id`) USING BTREE,
    INDEX `idx_script_id` (`maintenance_script_id`) USING BTREE,
    INDEX `idx_file_id` (`maintenance_file_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '维护任务表'
  ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `monitor_sync_task`;
CREATE TABLE `monitor_sync_task` (
  `sync_task_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `sync_task_name` VARCHAR(200) NOT NULL COMMENT '任务名称',
  `sync_task_desc` VARCHAR(500) DEFAULT NULL COMMENT '任务描述',
  `sync_task_status` VARCHAR(20) DEFAULT 'STOPPED' COMMENT '任务状态: STOPPED/RUNNING/ERROR',
  `sync_task_enabled` TINYINT DEFAULT 1 COMMENT '是否启用: 0-否 1-是',
  `sync_task_batch_size` INT DEFAULT 1000 COMMENT '批处理大小',
  `sync_task_consume_timeout` INT DEFAULT 30000 COMMENT '消费超时时间(ms)',
  `sync_task_retry_count` INT DEFAULT 3 COMMENT '重试次数',
  `sync_task_retry_interval` INT DEFAULT 1000 COMMENT '重试间隔(ms)',
  `sync_task_sync_interval` INT DEFAULT 0 COMMENT '同步间隔(ms)，0表示实时',
  `sync_task_ack_enabled` TINYINT DEFAULT 0 COMMENT '是否启用ACK: 0-否 1-是',
  `sync_task_transaction_enabled` TINYINT DEFAULT 0 COMMENT '是否启用事务: 0-否 1-是',
  `sync_task_cron` VARCHAR(100) DEFAULT NULL COMMENT 'Cron表达式',
  `sync_task_layout` TEXT COMMENT '设计器布局信息(JSON)',
  `sync_task_last_run_time` DATETIME DEFAULT NULL COMMENT '最后运行时间',
  `sync_task_last_run_status` VARCHAR(20) DEFAULT NULL COMMENT '最后运行状态',
  `sync_task_run_count` BIGINT DEFAULT 0 COMMENT '总运行次数',
  `sync_task_success_count` BIGINT DEFAULT 0 COMMENT '成功次数',
  `sync_task_fail_count` BIGINT DEFAULT 0 COMMENT '失败次数',
  `sync_task_create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `sync_task_update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
  `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`sync_task_id`),
  KEY `idx_task_name` (`sync_task_name`),
  KEY `idx_task_status` (`sync_task_status`),
  KEY `idx_create_time` (`sync_task_create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='同步任务表';

DROP TABLE IF EXISTS `monitor_sync_node`;
CREATE TABLE `monitor_sync_node` (
  `sync_node_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '节点ID',
  `sync_task_id` BIGINT NOT NULL COMMENT '关联任务ID',
  `sync_node_type` VARCHAR(20) NOT NULL COMMENT '节点类型: INPUT/OUTPUT/FILTER/DATA_CENTER',
  `sync_node_spi_name` VARCHAR(100) NOT NULL COMMENT 'SPI名称',
  `sync_node_name` VARCHAR(200) DEFAULT NULL COMMENT '节点显示名称',
  `sync_node_key` VARCHAR(100) NOT NULL COMMENT '节点唯一标识(前端生成)',
  `sync_node_config` TEXT COMMENT '节点配置(JSON)',
  `sync_node_position` VARCHAR(100) DEFAULT NULL COMMENT '节点位置(JSON: {x,y})',
  `sync_node_order` INT DEFAULT 0 COMMENT '节点顺序',
  `sync_node_enabled` TINYINT DEFAULT 1 COMMENT '是否启用: 0-否 1-是',
  `sync_node_desc` VARCHAR(500) DEFAULT NULL COMMENT '节点描述',
  `sync_node_create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `sync_node_update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`sync_node_id`),
  KEY `idx_task_id` (`sync_task_id`),
  KEY `idx_node_key` (`sync_node_key`),
  KEY `idx_node_type` (`sync_node_type`),
  CONSTRAINT `fk_node_task` FOREIGN KEY (`sync_task_id`) REFERENCES `monitor_sync_task` (`sync_task_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='同步节点表';

DROP TABLE IF EXISTS `monitor_sync_connection`;
CREATE TABLE `monitor_sync_connection` (
  `sync_connection_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '连线ID',
  `sync_task_id` BIGINT NOT NULL COMMENT '关联任务ID',
  `source_node_id` BIGINT DEFAULT NULL COMMENT '源节点ID',
  `source_node_key` VARCHAR(100) NOT NULL COMMENT '源节点Key',
  `source_handle` VARCHAR(50) DEFAULT 'output' COMMENT '源节点端口',
  `target_node_id` BIGINT DEFAULT NULL COMMENT '目标节点ID',
  `target_node_key` VARCHAR(100) NOT NULL COMMENT '目标节点Key',
  `target_handle` VARCHAR(50) DEFAULT 'input' COMMENT '目标节点端口',
  `connection_type` VARCHAR(20) DEFAULT 'DATA' COMMENT '连线类型: DATA/CONTROL',
  `connection_label` VARCHAR(100) DEFAULT NULL COMMENT '连线标签',
  `sync_connection_create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`sync_connection_id`),
  KEY `idx_task_id` (`sync_task_id`),
  KEY `idx_source_key` (`source_node_key`),
  KEY `idx_target_key` (`target_node_key`),
  CONSTRAINT `fk_conn_task` FOREIGN KEY (`sync_task_id`) REFERENCES `monitor_sync_task` (`sync_task_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='同步连线表';

DROP TABLE IF EXISTS `monitor_sync_task_log`;
CREATE TABLE `monitor_sync_task_log` (
  `sync_log_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `sync_task_id` BIGINT NOT NULL COMMENT '关联任务ID',
  `sync_log_status` VARCHAR(20) DEFAULT 'RUNNING' COMMENT '执行状态: RUNNING/SUCCESS/FAIL/TIMEOUT',
  `sync_log_trigger_type` VARCHAR(20) DEFAULT 'MANUAL' COMMENT '触发类型: MANUAL/SCHEDULE/API',
  `sync_log_read_count` BIGINT DEFAULT 0 COMMENT '读取数量',
  `sync_log_write_count` BIGINT DEFAULT 0 COMMENT '写入数量',
  `sync_log_success_count` BIGINT DEFAULT 0 COMMENT '成功数量',
  `sync_log_fail_count` BIGINT DEFAULT 0 COMMENT '失败数量',
  `sync_log_retry_count` BIGINT DEFAULT 0 COMMENT '重试数量',
  `sync_log_dead_letter_count` BIGINT DEFAULT 0 COMMENT '死信数量',
  `sync_log_filter_count` BIGINT DEFAULT 0 COMMENT '过滤数量',
  `sync_log_start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
  `sync_log_end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
  `sync_log_cost` BIGINT DEFAULT 0 COMMENT '耗时(毫秒)',
  `sync_log_avg_process_time` DOUBLE DEFAULT 0 COMMENT '平均处理时间(毫秒)',
  `sync_log_throughput` DOUBLE DEFAULT 0 COMMENT '吞吐量(条/秒)',
  `sync_log_message` TEXT COMMENT '执行消息/错误信息',
  `sync_log_stack_trace` TEXT COMMENT '详细堆栈(错误时)',
  `sync_log_create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`sync_log_id`),
  KEY `idx_task_id` (`sync_task_id`),
  KEY `idx_log_status` (`sync_log_status`),
  KEY `idx_start_time` (`sync_log_start_time`),
  KEY `idx_create_time` (`sync_log_create_time`),
  CONSTRAINT `fk_log_task` FOREIGN KEY (`sync_task_id`) REFERENCES `monitor_sync_task` (`sync_task_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='同步任务执行日志表';

DROP TABLE IF EXISTS `monitor_sync_task_log_detail`;
CREATE TABLE `monitor_sync_task_log_detail` (
  `log_detail_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '详细日志ID',
  `sync_log_id` BIGINT NOT NULL COMMENT '关联执行日志ID',
  `sync_task_id` BIGINT NOT NULL COMMENT '关联任务ID',
  `log_time` DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT '日志时间(精确到毫秒)',
  `log_level` VARCHAR(10) DEFAULT 'INFO' COMMENT '日志级别: DEBUG/INFO/WARN/ERROR',
  `log_node_key` VARCHAR(100) DEFAULT NULL COMMENT '相关节点Key',
  `log_message` TEXT COMMENT '日志消息',
  `log_data` TEXT COMMENT '日志数据(JSON)',
  PRIMARY KEY (`log_detail_id`),
  KEY `idx_sync_log_id` (`sync_log_id`),
  KEY `idx_task_id` (`sync_task_id`),
  KEY `idx_log_time` (`log_time`),
  KEY `idx_log_level` (`log_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='同步任务实时日志详情表';

SET FOREIGN_KEY_CHECKS = 1;
-- ==================== 缺失表补充 ====================

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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'SkyWalking配置信息' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_file_share`;
CREATE TABLE `monitor_sys_file_share`  (
  `share_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分享ID(UUID)',
  `server_id` bigint NULL DEFAULT NULL COMMENT '服务器ID',
  `bucket` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '存储桶',
  `file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件路径',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件名',
  `share_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分享类型：SHARE-普通分享，FLASH-闪图',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '过期时间',
  `view_count` int NULL DEFAULT 0 COMMENT '查看次数',
  `max_view_count` int NULL DEFAULT -1 COMMENT '最大查看次数，-1表示无限制',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `is_deleted` int NULL DEFAULT 0 COMMENT '是否已删除：0-否，1-是',
  PRIMARY KEY (`share_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文件分享记录' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_config`;
CREATE TABLE `monitor_sys_gen_config`  (
  `monitor_sys_gen_config_id` int NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `monitor_sys_gen_config_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置键',
  `monitor_sys_gen_config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '配置值',
  `monitor_sys_gen_config_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置描述',
  `monitor_sys_gen_config_env` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '环境',
  `monitor_sys_gen_config_status` int NULL DEFAULT NULL COMMENT '配置状态 0-禁用 1-启用',
  `monitor_sys_gen_config_app` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属应用',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_config_id`) USING BTREE,
  UNIQUE INDEX `uk_config_key_env`(`monitor_sys_gen_config_key`, `monitor_sys_gen_config_env`, `monitor_sys_gen_config_app`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '配置管理' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_config_push_history`;
CREATE TABLE `monitor_sys_gen_config_push_history`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '历史ID',
  `monitor_sys_gen_config_id` int NULL DEFAULT NULL COMMENT '配置ID',
  `monitor_sys_gen_config_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置键',
  `monitor_sys_gen_config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '配置值',
  `monitor_sys_gen_server_id` int NULL DEFAULT NULL COMMENT '服务器ID',
  `monitor_sys_gen_server_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务器名称',
  `push_success` int NULL DEFAULT NULL COMMENT '是否成功 0-失败 1-成功',
  `error_message` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误信息',
  `push_time` datetime(6) NULL DEFAULT NULL COMMENT '推送时间',
  `operator` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作人',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_config_id`(`monitor_sys_gen_config_id`) USING BTREE,
  INDEX `idx_server_id`(`monitor_sys_gen_server_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '配置推送历史' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `monitor_sys_gen_server_log_config`;
CREATE TABLE `monitor_sys_gen_server_log_config`  (
  `monitor_sys_gen_server_log_config_id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `monitor_sys_gen_server_id` int NOT NULL COMMENT '关联服务器ID',
  `monitor_sys_gen_server_log_retention_days` int NULL DEFAULT 30 COMMENT '日志保留天数',
  `monitor_sys_gen_server_log_enabled` int NULL DEFAULT 1 COMMENT '是否启用日志收集(0:否 1:是)',
  `monitor_sys_gen_server_log_level_filter` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志级别过滤器',
  `monitor_sys_gen_server_log_source_filter` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志来源过滤器',
  `monitor_sys_gen_server_log_max_size_mb` int NULL DEFAULT 100 COMMENT '单个日志文件最大大小(MB)',
  `monitor_sys_gen_server_log_max_files` int NULL DEFAULT 10 COMMENT '最大日志文件数量',
  `monitor_sys_gen_server_log_collection_interval` int NULL DEFAULT 60 COMMENT '日志收集间隔(秒)',
  `monitor_sys_gen_server_log_batch_size` int NULL DEFAULT 1000 COMMENT '批量处理大小',
  `monitor_sys_gen_server_log_compression_enabled` int NULL DEFAULT 1 COMMENT '是否启用压缩(0:否 1:是)',
  `monitor_sys_gen_server_log_real_time_enabled` int NULL DEFAULT 0 COMMENT '是否启用实时日志(0:否 1:是)',
  `monitor_sys_gen_server_log_file_patterns` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '日志文件匹配模式(JSON格式)',
  `monitor_sys_gen_server_log_exclude_patterns` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '排除模式(JSON格式)',
  `monitor_sys_gen_server_log_custom_parsers` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '自定义解析器配置(JSON格式)',
  `monitor_sys_gen_server_log_alert_rules` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '告警规则配置(JSON格式)',
  `monitor_sys_gen_server_log_create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `monitor_sys_gen_server_log_update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `monitor_sys_gen_server_log_config_status` int NULL DEFAULT 1 COMMENT '配置状态(0:禁用 1:启用)',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_server_log_config_id`) USING BTREE,
  INDEX `idx_server_id`(`monitor_sys_gen_server_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '服务器日志配置' ROW_FORMAT = DYNAMIC;

DROP TABLE IF EXISTS `system_server_setting_preview_extension`;
CREATE TABLE `system_server_setting_preview_extension`  (
  `system_server_setting_preview_extension_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `preview_extension_server_id` int NOT NULL COMMENT '所属服务器ID',
  `preview_extension_disabled` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '禁用预览的扩展名列表（逗号分隔）',
  `preview_extension_allowed` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '允许预览的扩展名列表（逗号分隔）',
  `preview_extension_whitelist_mode` tinyint(1) NULL DEFAULT 0 COMMENT '是否启用白名单模式',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`system_server_setting_preview_extension_id`) USING BTREE,
  INDEX `idx_server_id`(`preview_extension_server_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '预览扩展名配置' ROW_FORMAT = DYNAMIC;

-- ==================== 爬虫任务表 ====================
DROP TABLE IF EXISTS `monitor_spider_task`;
CREATE TABLE `monitor_spider_task` (
  `spider_task_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `spider_task_name` varchar(200) NOT NULL COMMENT '任务名称',
  `spider_task_url` varchar(500) NOT NULL COMMENT '目标URL',
  `spider_task_desc` varchar(500) DEFAULT NULL COMMENT '任务描述',
  `spider_task_url_pattern` varchar(500) DEFAULT NULL COMMENT 'URL模式',
  `spider_task_extract_rules` text COMMENT '提取规则JSON',
  `spider_task_selector_type` varchar(20) DEFAULT 'XPATH' COMMENT '选择器类型',
  `spider_task_thread_num` int DEFAULT '1' COMMENT '线程数',
  `spider_task_sleep_time` int DEFAULT '1000' COMMENT '爬取间隔(毫秒)',
  `spider_task_timeout` int DEFAULT '30' COMMENT '超时时间(秒)',
  `spider_task_retry_times` int DEFAULT '3' COMMENT '重试次数',
  `spider_task_headers` text COMMENT '请求头配置(JSON)',
  `spider_task_cookies` text COMMENT 'Cookie配置',
  `spider_task_user_agent` varchar(500) DEFAULT NULL COMMENT 'User-Agent配置',
  `spider_task_proxy` varchar(255) DEFAULT NULL COMMENT '代理配置',
  `spider_task_schedule_type` varchar(20) DEFAULT 'NONE' COMMENT '调度类型: NONE-一次性, CRON-定时',
  `spider_task_cron` varchar(50) DEFAULT NULL COMMENT 'CRON表达式',
  `spider_task_pipeline_type` varchar(20) DEFAULT 'JSON' COMMENT '存储类型: JSON/DATABASE/FILE',
  `spider_task_pipeline_config` text COMMENT '存储配置(JSON)',
  `spider_task_status` tinyint DEFAULT '1' COMMENT '任务状态: 0-停用, 1-启用',
  `spider_task_run_status` varchar(20) DEFAULT 'IDLE' COMMENT '运行状态: IDLE/RUNNING/STOPPED/ERROR',
  `spider_task_last_run_time` datetime DEFAULT NULL COMMENT '最后运行时间',
  `spider_task_next_run_time` datetime DEFAULT NULL COMMENT '下次运行时间',
  `spider_task_total_success` bigint DEFAULT '0' COMMENT '总成功数',
  `spider_task_total_fail` bigint DEFAULT '0' COMMENT '总失败数',
  `spider_task_job_id` int DEFAULT NULL COMMENT '关联的Job ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`spider_task_id`),
  KEY `idx_status` (`spider_task_status`),
  KEY `idx_run_status` (`spider_task_run_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='爬虫任务表';

DROP TABLE IF EXISTS `monitor_spider_data`;
CREATE TABLE `monitor_spider_data` (
  `spider_data_id` bigint NOT NULL AUTO_INCREMENT COMMENT '数据ID',
  `spider_task_id` bigint NOT NULL COMMENT '任务ID',
  `spider_data_content` json COMMENT '爬取的数据内容',
  `spider_data_url` varchar(500) DEFAULT NULL COMMENT '数据来源URL',
  `spider_data_hash` varchar(64) DEFAULT NULL COMMENT '数据哈希(用于去重)',
  `crawl_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '爬取时间',
  PRIMARY KEY (`spider_data_id`),
  KEY `idx_task_id` (`spider_task_id`),
  KEY `idx_crawl_time` (`crawl_time`),
  UNIQUE KEY `uk_hash` (`spider_data_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='爬虫数据表';

DROP TABLE IF EXISTS `monitor_spider_log`;
CREATE TABLE `monitor_spider_log` (
  `spider_log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `spider_task_id` bigint NOT NULL COMMENT '任务ID',
  `spider_log_level` varchar(20) DEFAULT 'INFO' COMMENT '日志级别: INFO/WARN/ERROR',
  `spider_log_message` text COMMENT '日志内容',
  `spider_log_url` varchar(500) DEFAULT NULL COMMENT '相关URL',
  `spider_log_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '日志时间',
  PRIMARY KEY (`spider_log_id`),
  KEY `idx_task_id` (`spider_task_id`),
  KEY `idx_log_time` (`spider_log_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='爬虫日志表';

-- ==================== 系统软件操作日志表 ====================
DROP TABLE IF EXISTS `system_soft_operation_log`;
CREATE TABLE `system_soft_operation_log` (
  `log_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `operation_id` VARCHAR(64) DEFAULT NULL COMMENT '操作ID（UUID，用于关联同一操作的多条日志）',
  `operation_type` VARCHAR(50) DEFAULT NULL COMMENT '操作类型（PULL_IMAGE/CREATE_CONTAINER/START_CONTAINER/STOP_CONTAINER/REMOVE_CONTAINER等）',
  `operation_target` VARCHAR(255) DEFAULT NULL COMMENT '操作目标（镜像名/容器名）',
  `container_id` INT DEFAULT NULL COMMENT '关联的容器ID',
  `image_id` INT DEFAULT NULL COMMENT '关联的镜像ID',
  `soft_id` INT DEFAULT NULL COMMENT '关联的软件ID',
  `server_id` INT DEFAULT NULL COMMENT '关联的服务器ID',
  `record_id` INT DEFAULT NULL COMMENT '关联的操作记录ID',
  `log_level` VARCHAR(20) DEFAULT 'INFO' COMMENT '日志级别（INFO/WARN/ERROR/DEBUG）',
  `log_message` VARCHAR(5000) DEFAULT NULL COMMENT '日志消息',
  `log_detail` TEXT DEFAULT NULL COMMENT '日志详情（JSON格式，存储进度、状态等详细信息）',
  `progress` INT DEFAULT 0 COMMENT '操作进度（0-100）',
  `status` VARCHAR(20) DEFAULT NULL COMMENT '操作状态（PENDING/RUNNING/SUCCESS/FAILED/CANCELLED）',
  `docker_status` VARCHAR(100) DEFAULT NULL COMMENT 'Docker响应状态',
  `docker_progress` VARCHAR(500) DEFAULT NULL COMMENT 'Docker响应进度信息',
  `error_message` VARCHAR(2000) DEFAULT NULL COMMENT '错误信息',
  `error_stack` TEXT DEFAULT NULL COMMENT '错误堆栈',
  `log_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '日志时间',
  `start_time` DATETIME DEFAULT NULL COMMENT '操作开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '操作结束时间',
  `duration` BIGINT DEFAULT NULL COMMENT '操作耗时（毫秒）',
  `operator` VARCHAR(100) DEFAULT NULL COMMENT '操作用户',
  `client_ip` VARCHAR(50) DEFAULT NULL COMMENT '客户端IP',
  `ext_data` TEXT DEFAULT NULL COMMENT '扩展字段（JSON格式）',
  `create_name` VARCHAR(255) DEFAULT NULL COMMENT '创建人姓名',
  `create_by` INT DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_name` VARCHAR(255) DEFAULT NULL COMMENT '更新人姓名',
  `update_by` INT DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`log_id`),
  INDEX `idx_operation_id` (`operation_id`),
  INDEX `idx_operation_type` (`operation_type`),
  INDEX `idx_container_id` (`container_id`),
  INDEX `idx_image_id` (`image_id`),
  INDEX `idx_server_id` (`server_id`),
  INDEX `idx_record_id` (`record_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_log_time` (`log_time`),
  INDEX `idx_log_level` (`log_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统软件操作日志表';

SET FOREIGN_KEY_CHECKS = 1;
