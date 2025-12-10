/*
 Navicat Premium Dump SQL

 Source Server         : 172.16.0.40
 Source Server Type    : MySQL
 Source Server Version : 80044 (8.0.44)
 Source Host           : 172.16.0.40:3306
 Source Schema         : report

 Target Server Type    : MySQL
 Target Server Version : 80044 (8.0.44)
 File Encoding         : 65001

 Date: 10/12/2025 17:25:24
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for device_metrics
-- ----------------------------
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

-- ----------------------------
-- Records of device_metrics
-- ----------------------------

-- ----------------------------
-- Table structure for file_storage
-- ----------------------------
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
  `file_storage_protocol_id` int NULL DEFAULT NULL COMMENT '服务器协议; ',
  `file_storage_status` int NULL DEFAULT NULL COMMENT '状态;0:停用',
  `file_storage_cookie` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL COMMENT 'cookie',
  PRIMARY KEY (`file_storage_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_bin COMMENT = '文件存储' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of file_storage
-- ----------------------------
INSERT INTO `file_storage` VALUES (3, '本地磁盘测试(F)', NULL, 'FILESYSTEM', NULL, NULL, 'ri:u-disk-line', 'F://', 'workspace', 2, 1, NULL);
INSERT INTO `file_storage` VALUES (6, '本地FTP1', NULL, 'FTP', NULL, NULL, 'pixelarticons:align-right', 'ftp://127.0.0.1:21', 'oss', 2, 1, NULL);
INSERT INTO `file_storage` VALUES (7, 'SFTP', NULL, 'SFTP', 'root', 'q^EgK8quQ5!zgfRb7.18', 'pixelarticons:add-row', 'sftp://112.124.44.21:22', 'home', 2, 0, NULL);
INSERT INTO `file_storage` VALUES (22, '本地磁盘测试(Z)', NULL, 'FILESYSTEM', NULL, NULL, 'simple-icons:files', 'D://', 'test', 2, 1, NULL);

-- ----------------------------
-- Table structure for file_storage_log
-- ----------------------------
DROP TABLE IF EXISTS `file_storage_log`;
CREATE TABLE `file_storage_log`  (
  `file_storage_log_id` int NOT NULL AUTO_INCREMENT,
  `file_storage_id` int NULL DEFAULT NULL,
  `file_storage_log_address` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '请求客户端地址',
  `file_storage_log_url` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '请求文件地址',
  PRIMARY KEY (`file_storage_log_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_bin COMMENT = '文件存储日志' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of file_storage_log
-- ----------------------------

-- ----------------------------
-- Table structure for file_storage_protocol
-- ----------------------------
DROP TABLE IF EXISTS `file_storage_protocol`;
CREATE TABLE `file_storage_protocol`  (
  `file_storage_protocol_id` int NOT NULL AUTO_INCREMENT,
  `file_storage_protocol_port` int NULL DEFAULT NULL COMMENT '开启远程服务器端口',
  `file_storage_protocol_desc` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '服务器说明',
  `file_storage_protocol_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '服务器协议; HTTP',
  `file_storage_protocol_host` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '开启远程服务器主机',
  `file_storage_protocol_status` int NULL DEFAULT NULL COMMENT '远程服务器状态; 0:未开启',
  `file_storage_protocol_plugins` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '插件列表，多个逗号分隔',
  `file_storage_protocol_setting` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '配置列表，多个逗号分隔',
  `file_storage_protocol_plugin_open` int NULL DEFAULT NULL COMMENT '是否开启插件；0:不开启',
  `file_storage_protocol_setting_open` int NULL DEFAULT NULL COMMENT '是否开启配置；0:不开启',
  `file_storage_protocol_rang_open` int NULL DEFAULT NULL COMMENT '是否开启Range；0:不开启',
  `file_storage_protocol_ua_open` int NULL DEFAULT NULL COMMENT '是否开启UA；0:不开启',
  `file_storage_protocol_download_ua` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT 'UA',
  `file_storage_protocol_ua` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT 'UA',
  `file_storage_protocol_preview_or_download` int NULL DEFAULT NULL COMMENT '0: 全部支持;1:预览; 2:下载',
  `file_storage_protocol_watermark_open` int NULL DEFAULT NULL COMMENT '是否开启水印；0:不开启; 1: 下载水印;2:预览水印;3: 两者',
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

-- ----------------------------
-- Records of file_storage_protocol
-- ----------------------------
INSERT INTO `file_storage_protocol` VALUES (2, 8184, '测试', 'HTTP', '192.168.110.79', 1, 'GRAY', 'FORMAT,QUALITY,INTERLACE,ROTATE,BLUR,THUMBNAIL,BRIGHT,ORIENT,GRAY,SHAPE,CAN', 1, 1, 0, 0, NULL, 'PC;OSS', 0, 0, 1, NULL, NULL, 1, '11111111', 'TILE', NULL, NULL, 100, 100, 66, 'rgba(0, 0, 0, 0.52)');

-- ----------------------------
-- Table structure for file_system
-- ----------------------------
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

-- ----------------------------
-- Records of file_system
-- ----------------------------
INSERT INTO `file_system` VALUES (1, '0ddf905d079409d89373ea965b624717_1764559382906.jpg', '0ddf905d079409d89373ea965b624717.jpg', 'C:\\Users\\yemen\\AppData\\Local\\Temp\\\\filesystem\\default\\2025\\12\\01\\0ddf905d079409d89373ea965b624717_1764559382906.jpg', 90583, 'e97ad1783ca0b0b35760665b9cc3c0a5', 'image', 'jpg', 1, 1, 2, '0', NULL, NULL, '2025-12-01 11:23:02.922141', '2025-12-01 11:26:09.120387', NULL, 1, 'admin', 1, '2025-12-01 11:23:02.954616', '2025-12-01 11:26:10.000000', NULL, NULL);

-- ----------------------------
-- Table structure for file_system_group
-- ----------------------------
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

-- ----------------------------
-- Records of file_system_group
-- ----------------------------
INSERT INTO `file_system_group` VALUES (1, 'default', NULL, 'default', 'default', 1, '1', '默认分组', '1', 999, 1, 'ri:folder-line', '#909399', 'admin', 1, '2025-12-01 11:23:02.817441', '2025-12-01 11:23:02.876585', 'admin', 1);

-- ----------------------------
-- Table structure for file_system_part
-- ----------------------------
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

-- ----------------------------
-- Records of file_system_part
-- ----------------------------

-- ----------------------------
-- Table structure for file_system_setting
-- ----------------------------
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

-- ----------------------------
-- Records of file_system_setting
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_acme_account
-- ----------------------------
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
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_acme_account
-- ----------------------------
INSERT INTO `monitor_acme_account` VALUES (1, '784412312@qq.com', 'https://acme-v02.api.letsencrypt.org/directory', NULL, NULL, '', 'admin', 1, '2025-12-09 22:03:58.000000', '2025-12-09 23:31:30.826479', 'admin', 1);
INSERT INTO `monitor_acme_account` VALUES (2, '784412312@qq.com', 'https://acme.zerossl.com/v2/DV90', NULL, NULL, '', 'admin', 1, '2025-12-09 22:03:58.000000', '2025-12-09 23:29:28.000000', 'admin', 1);
INSERT INTO `monitor_acme_account` VALUES (3, '784412312@qq.com', 'https://acme.hi.cn/directory', NULL, NULL, '', 'admin', 1, '2025-12-09 23:58:14.949304', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for monitor_acme_certificate
-- ----------------------------
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
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_acme_certificate
-- ----------------------------
INSERT INTO `monitor_acme_certificate` VALUES (1, 1, 'www.qirenit.com', '', 'HTTP-01', 'failed', NULL, NULL, NULL, NULL, NULL, NULL, 'admin', 1, '2025-12-09 22:57:19.095428', '2025-12-09 22:57:30.151232', 'admin', 1);
INSERT INTO `monitor_acme_certificate` VALUES (2, 1, 'www.qirenit.com', '', 'HTTP-01', 'failed', NULL, NULL, NULL, NULL, NULL, NULL, 'admin', 1, '2025-12-09 22:59:00.432038', '2025-12-09 22:59:12.416402', 'admin', 1);
INSERT INTO `monitor_acme_certificate` VALUES (3, 1, 'www.qiren.com', '', 'HTTP-01', 'failed', NULL, NULL, NULL, NULL, NULL, NULL, 'admin', 1, '2025-12-09 23:04:25.609432', '2025-12-09 23:04:36.331971', 'admin', 1);
INSERT INTO `monitor_acme_certificate` VALUES (4, 1, 'www.qiren.com', '', 'HTTP-01', 'failed', NULL, NULL, NULL, NULL, NULL, NULL, 'admin', 1, '2025-12-09 23:07:13.367629', '2025-12-09 23:08:32.544861', 'admin', 1);
INSERT INTO `monitor_acme_certificate` VALUES (5, 1, 'www.qirenit.com', '', 'HTTP-01', 'failed', NULL, NULL, NULL, NULL, NULL, NULL, 'admin', 1, '2025-12-09 23:10:52.056922', '2025-12-09 23:11:17.622365', 'admin', 1);
INSERT INTO `monitor_acme_certificate` VALUES (6, 1, 'www.qirenit.com', '', 'HTTP-01', 'failed', NULL, NULL, NULL, NULL, NULL, '连接 ACME 服务器失败: Network error', 'admin', 1, '2025-12-09 23:24:24.056524', '2025-12-09 23:24:43.873291', 'admin', 1);
INSERT INTO `monitor_acme_certificate` VALUES (7, 1, 'www.qirenit.com', '', 'HTTP-01', 'failed', NULL, NULL, NULL, NULL, NULL, '连接 ACME 服务器失败: Network error', 'admin', 1, '2025-12-09 23:29:34.932807', '2025-12-09 23:29:45.685162', 'admin', 1);
INSERT INTO `monitor_acme_certificate` VALUES (8, 1, 'www.qirenit.com', '', 'HTTP-01', 'failed', NULL, NULL, NULL, NULL, NULL, '连接 ACME 服务器失败: Network error', 'admin', 1, '2025-12-09 23:31:53.176125', '2025-12-09 23:32:46.232197', 'admin', 1);
INSERT INTO `monitor_acme_certificate` VALUES (9, 1, 'www.qirenit.com', '', 'HTTP-01', 'failed', NULL, NULL, NULL, NULL, NULL, '连接 ACME 服务器失败: Network error', 'admin', 1, '2025-12-09 23:32:32.196632', '2025-12-09 23:32:46.203671', 'admin', 1);
INSERT INTO `monitor_acme_certificate` VALUES (10, 1, 'www.qirenit.com', '', 'HTTP-01', 'failed', NULL, NULL, NULL, NULL, NULL, '连接 ACME 服务器失败: Network error', 'admin', 1, '2025-12-09 23:32:48.691996', '2025-12-09 23:33:23.315563', 'admin', 1);
INSERT INTO `monitor_acme_certificate` VALUES (11, 3, 'www.qirenit.com', '', 'HTTP-01', 'failed', NULL, NULL, NULL, NULL, NULL, '连接 ACME 服务器失败: Network error', 'admin', 1, '2025-12-10 00:02:09.653355', '2025-12-10 00:02:17.534829', 'admin', 1);
INSERT INTO `monitor_acme_certificate` VALUES (12, 3, 'www.qirenit.com', '', 'HTTP-01', 'failed', NULL, NULL, NULL, NULL, NULL, 'SSL/TLS 连接失败: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target', 'admin', 1, '2025-12-10 00:07:43.790521', '2025-12-10 00:07:44.263531', 'admin', 1);
INSERT INTO `monitor_acme_certificate` VALUES (13, 3, 'www.qirenit.com', '', 'HTTP-01', 'failed', NULL, NULL, NULL, NULL, NULL, '无法连接到 ACME 服务器，请检查网络连接或防火墙设置', 'admin', 1, '2025-12-10 00:13:07.833853', '2025-12-10 00:13:16.880896', 'admin', 1);
INSERT INTO `monitor_acme_certificate` VALUES (14, 3, 'www.qirenit.com', '', 'DNS-01', 'failed', NULL, NULL, NULL, NULL, NULL, 'SSL/TLS 连接失败: No name matching acme.hi.cn found', 'admin', 1, '2025-12-10 00:16:42.782477', '2025-12-10 00:16:43.385720', 'admin', 1);
INSERT INTO `monitor_acme_certificate` VALUES (15, 1, 'www.qirenit.com', '', 'HTTP-01', 'failed', NULL, NULL, NULL, NULL, NULL, '网络 I/O 错误: HTTP connect timed out', 'admin', 1, '2025-12-10 00:17:01.852728', '2025-12-10 00:17:16.903229', 'admin', 1);

-- ----------------------------
-- Table structure for monitor_acme_order
-- ----------------------------
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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_acme_order
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_application
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_application
-- ----------------------------
INSERT INTO `monitor_application` VALUES (2, ' 平台端', 'system', 'sa', 1, '2024-09-13 10:45:07', '2025-12-08 20:26:29', 'admin', 1, NULL);

-- ----------------------------
-- Table structure for monitor_arthas_session
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_arthas_session
-- ----------------------------
INSERT INTO `monitor_arthas_session` VALUES (1, '7b81a815-49ae-4c60-875f-0dae1b235ceb', '40d92acd085b4141aa7275ffb27c126a_1', 2, '142adf753597ef435919e95bd13ea388', 'http://127.0.0.1:8563/api', 'trace com.chua.starter.monitor.arthas.ArthasCommandController exec -n 10', 'trace', 'INTERRUPTED', '2025-12-04 01:34:03.176337', '2025-12-04 01:34:04.260733', '2025-12-04 02:34:03.176337', '最新状态: state=SUCCEEDED, sessionId=7b81a815-49ae-4c60-875f-0dae1b235ceb');

-- ----------------------------
-- Table structure for monitor_arthas_tunnel_config
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_arthas_tunnel_config
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_job
-- ----------------------------
DROP TABLE IF EXISTS `monitor_job`;
CREATE TABLE `monitor_job`  (
  `job_id` int NOT NULL AUTO_INCREMENT,
  `job_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `job_author` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '负责人',
  `job_alarm_email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '报警邮件，多个邮件地址则逗号分隔',
  `job_schedule_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务类型; cron, fixed',
  `job_schedule_time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置时间',
  `job_trigger_status` int NULL DEFAULT 0 COMMENT '状态;0: 未启用； 1: 正在运行',
  `monitor_id` int NULL DEFAULT NULL COMMENT '对应的应用ID',
  `job_application_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '对应的应用名称',
  `job_application_active` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '环境',
  `job_execute_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模式; spring, xxl-job',
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

-- ----------------------------
-- Records of monitor_job
-- ----------------------------
INSERT INTO `monitor_job` VALUES (1, '测试', 'sa', NULL, 'CRON', '0 * * * * ?', 1, 2, 'system', 'dev', NULL, 'test', NULL, NULL, NULL, NULL, 'BEAN', NULL, 10, 'DO_NOTHING', NULL, 'FIRST', 1765290540000, 1765290600000, NULL, NULL, NULL, '2025-12-09 22:28:56', 'admin', 1);

-- ----------------------------
-- Table structure for monitor_job_log
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_job_log
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_log
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_log
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_mqtt_server
-- ----------------------------
DROP TABLE IF EXISTS `monitor_mqtt_server`;
CREATE TABLE `monitor_mqtt_server`  (
  `monitor_mqtt_server_id` int NOT NULL AUTO_INCREMENT,
  `monitor_mqtt_server_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `monitor_mqtt_server_host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地址',
  `monitor_mqtt_server_port` int NULL DEFAULT NULL COMMENT '端口',
  `monitor_mqtt_server_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账号',
  `monitor_mqtt_server_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
  `monitor_mqtt_server_version` int NULL DEFAULT NULL COMMENT '版本号',
  `monitor_mqtt_server_status` int NULL DEFAULT NULL COMMENT '是否启动;0:未启动',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_mqtt_server_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_mqtt_server
-- ----------------------------
INSERT INTO `monitor_mqtt_server` VALUES (1, '测试', '0.0.0.0', 12121, NULL, NULL, NULL, 1, 'sa', 1, '2025-01-10 19:55:14', '2025-01-10 20:18:26', 'sa', 1);

-- ----------------------------
-- Table structure for monitor_nginx_config
-- ----------------------------
DROP TABLE IF EXISTS `monitor_nginx_config`;
CREATE TABLE `monitor_nginx_config`  (
  `monitor_nginx_config_id` int NOT NULL AUTO_INCREMENT,
  `monitor_nginx_config_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置名称',
  `monitor_nginx_config_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置文件路径',
  `monitor_nginx_config_worker_processes` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '#允许进程数量，\\n建议设置为cpu核心数或者auto自动检测，注意Windows服务器上虽然可以启动多个processes，但是实际只会用其中一个worker_processes  auto;',
  `monitor_nginx_config_access_log` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'access_log日志目录',
  `monitor_nginx_config_error_log` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '#error日志存放位置\\n error_log  logs/error.log; \\n#error_log  logs/error.log  notice; \\n#error_log  logs/error.log  info;',
  `monitor_nginx_config_pid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '#pid        logs/nginx.pid;#工作模式及连接数上限',
  `monitor_nginx_config_multipart` int NULL DEFAULT NULL COMMENT '是否多文件存储',
  `monitor_nginx_config_nginx_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'nginx脚本路径；服务方式无需配置',
  `monitor_nginx_config_type` int NULL DEFAULT NULL COMMENT '配置类型;0:文件；1:服务',
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

-- ----------------------------
-- Records of monitor_nginx_config
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_nginx_event
-- ----------------------------
DROP TABLE IF EXISTS `monitor_nginx_event`;
CREATE TABLE `monitor_nginx_event`  (
  `monitor_nginx_event_id` int NOT NULL AUTO_INCREMENT,
  `monitor_nginx_event_worker_connections` int NULL DEFAULT NULL COMMENT ' Nginx 的工作模式及连接数上限 worker_connections  1024;',
  `monitor_nginx_event_use` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'use epoll;指定使用哪种事件模型。Nginx 支持多种事件模型，如 epoll（Linux）、kqueue（BSD）、select 和 poll 等。通常，Nginx 会根据操作系统自动选择最佳的事件模型，但也可以手动指定',
  `monitor_nginx_event_multi_accept` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'multi_accept on; 设置是否允许服务器在单个监听事件中接受多个连接。这可以减少 I/O 等待时间，提高性能。',
  `monitor_nginx_event_accept_mutex` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'accept_mutex on;在某些情况下，可以设置为 on 来允许多个工作进程同时监听相同的端口。默认情况下，它是关闭的，以避免多个进程间的端口竞争。',
  `monitor_nginx_config_id` int NULL DEFAULT NULL COMMENT '所属配置',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_nginx_event_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'nginx事件' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_nginx_event
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_nginx_http
-- ----------------------------
DROP TABLE IF EXISTS `monitor_nginx_http`;
CREATE TABLE `monitor_nginx_http`  (
  `monitor_nginx_http_id` int NOT NULL AUTO_INCREMENT,
  `monitor_nginx_http_keepalive_timeout` int NULL DEFAULT 65 COMMENT '长连接超时时间，单位是秒',
  `monitor_nginx_http_sendfile` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'on' COMMENT '开启高效文件传输模式，sendfile指令指定nginx是否调用sendfile函数来输出文件，对于普通应用设为 on，如果用来进行下载等应用磁盘IO重负载应用，可设置为off，以平衡磁盘与网络I/O处理速度，降低系统的负载。注意：如果图片显示不正常把这个改成off.sendfile指令指定 nginx 是否调用sendfile 函数（zero copy 方式）来输出文件，对于普通应用，必须设为on。如果用来进行下载等应用磁盘IO重负载应用，可设置为off，以平衡磁盘与网络IO处理速度，降低系统uptime',
  `monitor_nginx_http_access_log` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用了log_format指令设置了日志格式之后，需要用access_log指令指定日志文件的存放路径记录了哪些用户，哪些页面以及用户浏览器、ip和其他的访问信息',
  `monitor_nginx_http_server_names_hash_bucket_size` int NULL DEFAULT 128 COMMENT '服务器名字的hash表大小',
  `monitor_nginx_http_server_tokens` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'off' COMMENT '隐藏ngnix版本号',
  `monitor_nginx_http_ignore_invalid_headers` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'on' COMMENT '忽略不合法的请求头',
  `monitor_nginx_http_server_name_in_redirect` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'off' COMMENT '让 nginx 在处理自己内部重定向时不默认使用  server_name设置中的第一个域名',
  `monitor_nginx_http_client_body_buffer_size` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '8m' COMMENT '客户端请求体的大小',
  `monitor_nginx_http_tcp_nopush` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'on' COMMENT '告诉nginx在一个数据包里发送所有头文件，而不一个接一个的发送。',
  `monitor_nginx_http_tcp_nodelay` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'tcp_nodelay off 会增加通信的延时，但是会提高带宽利用率。在高延时、数据量大的通信场景中应该会有不错的效果tcp_nodelay on，会增加小包的数量，但是可以提高响应速度。在及时性高的通信场景中应该会有不错的效果',
  `monitor_nginx_http_error_log` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误日志',
  `monitor_nginx_http_log_format` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '自定义日志格式',
  `monitor_nginx_http_charset` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '编码',
  `monitor_nginx_http_gzip` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'on' COMMENT 'gzip模块设置，使用 gzip 压缩可以降低网站带宽消耗，同时提升访问速度',
  `monitor_nginx_http_gzip_disable` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '禁用gzip压缩',
  `monitor_nginx_http_gzip_min_length` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最小压缩大小',
  `monitor_nginx_http_gzip_buffers` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '压缩缓冲区4 16k',
  `monitor_nginx_http_gzip_http_version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'gzip_http_version 1.0;        #压缩版本',
  `monitor_nginx_http_gzip_comp_level` int NULL DEFAULT NULL COMMENT 'gzip_comp_level 2;            #压缩等级',
  `monitor_nginx_http_gzip_vary` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'gzip_vary on;                 #是否开启gzip vary头',
  `monitor_nginx_http_gzip_types` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '#gzip_types   text/plain text/css text/xml text/javascript application/json application/x-javascript application/xml application/xml+rss;#压缩类型',
  `monitor_nginx_http_ssl_certificate` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'ssl_certificate     	cert/yphtoy.com.pem;   #加密证书路径',
  `monitor_nginx_http_ssl_certificate_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'ssl_certificate_key	cert/yphtoy.com.key;       #加密私钥路径',
  `monitor_nginx_http_ssl_protocols` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'ssl_protocols		TLSv1 TLSv1.1 TLSv1.2;     #加密协议',
  `monitor_nginx_http_ssl_session_timeout` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'ssl_session_timeout	10m;                       #加密访问缓存过期时间',
  `monitor_nginx_http_ssl_ciphers` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'ssl_ciphers		HIGH:!aNULL:!MD5;              #加密算法',
  `monitor_nginx_http_ssl_prefer_server_ciphers` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'ssl_prefer_server_ciphers on;	               #是否由服务器决定采用哪种加密算法',
  `monitor_nginx_http_include` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '# 包含其他的配置文件include  /etc/nginx/conf.d/*.conf;include  /etc/nginx/sites-enabled/*;',
  `monitor_nginx_http_limit_req_zone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '限流；limit_req_zone $binary_remote_addr zone=mylimit:10m rate=1r/s;',
  `monitor_nginx_http_proxy_cache_path` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '缓存； proxy_cache_path /data/nginx/cache levels=1:2 keys_zone=my_cache:10m;max_size=10g inactive=60m use_temp_path=off; # 定义缓存路径、目录结构、缓存区名称和大小、最大缓存大小、非活动数据清理时间，以及是否使用临时路径  ',
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

-- ----------------------------
-- Records of monitor_nginx_http
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_nginx_http_server
-- ----------------------------
DROP TABLE IF EXISTS `monitor_nginx_http_server`;
CREATE TABLE `monitor_nginx_http_server`  (
  `monitor_nginx_http_server_id` int NOT NULL AUTO_INCREMENT,
  `monitor_nginx_http_id` int NULL DEFAULT NULL COMMENT 'monitor_nginx_http表ID',
  `monitor_nginx_http_server_port` int NULL DEFAULT NULL COMMENT 'listen 80',
  `monitor_nginx_http_server_root` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT ' root /usr/share/nginx/html;',
  `monitor_nginx_http_server_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'server_name kuntu.com;',
  `monitor_nginx_http_server_charset` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '编码',
  `monitor_nginx_http_server_access_log` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT ' #access_log  logs/host.access.log  main;',
  `monitor_nginx_http_server_return` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '# 301重定向跳转到HTTPS接口;return 301 https://$server_name$request_uri;',
  `monitor_nginx_http_server_ssl` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'listen 443 ssl;',
  `monitor_nginx_http_server_udp` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'listen 53 udp;',
  `monitor_nginx_http_server_error_page` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'error_page 500 502 503 504 /50x.html;',
  `monitor_nginx_http_server_ssl_certificate` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'ssl证书; ssl_certificate /root/.acme.sh/zjedu-ai.com_ecc/fullchain.cer',
  `monitor_nginx_http_server_ssl_certificate_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'ssl证书密钥; ssl_certificate_key /root/.acme.sh/zjedu-ai.com_ecc/private.key',
  `monitor_nginx_http_server_ssl_session_timeout` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'ssl会话缓存; ssl_session_timeout 5m',
  `monitor_nginx_http_server_ssl_ciphers` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'ssl加密算法; ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:ECDHE:ECDH:AES:HIGH:!NULL:!aNULL:!MD5:!ADH:!RC4;',
  `monitor_nginx_http_server_ssl_protocols` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'ssl加密协议; ssl_protocols TLSv1 TLSv1.1 TLSv1.2;',
  `monitor_nginx_http_server_ssl_prefer_server_ciphers` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'ssl加密优先级; ssl_prefer_server_ciphers on;',
  `monitor_nginx_http_server_proxy_read_timeout` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'proxy_read_timeout 60s',
  `monitor_nginx_http_server_proxy_send_timeout` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'proxy_send_timeout 60s',
  `monitor_nginx_http_server_proxy_connect_timeout` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'proxy_connect_timeout 60s',
  `monitor_nginx_http_server_client_max_body_size` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'client_max_body_size 100m',
  `monitor_nginx_http_server_client_body_buffer_size` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'client_body_buffer_size 100m',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_nginx_http_server_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 44 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'nginx http服务配置' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_nginx_http_server
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_nginx_http_server_location
-- ----------------------------
DROP TABLE IF EXISTS `monitor_nginx_http_server_location`;
CREATE TABLE `monitor_nginx_http_server_location`  (
  `monitor_nginx_http_server_location_id` int NOT NULL AUTO_INCREMENT,
  `monitor_nginx_http_server_id` int NULL DEFAULT NULL COMMENT 'monitor_nginx_http_server表ID',
  `monitor_nginx_http_server_location_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地址名称；location ',
  `monitor_nginx_http_server_location_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `monitor_nginx_http_server_location_proxy_pass` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'proxy_pass http://192.168.247.129:8080;',
  `monitor_nginx_http_server_location_proxy_cache` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'proxy_cache my_cache;  # 启用名为my_cache的缓存',
  `monitor_nginx_http_server_location_proxy_cache_valid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'proxy_cache_valid 200 302 10m;  # 对HTTP 200和302响应缓存10分钟\\nproxy_cache_valid 404      1m;  # 对HTTP 404响应缓存1分钟 ',
  `monitor_nginx_http_server_location_proxy_cache_methods` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'proxy_cache_methods GET HEAD;  # 对哪些方法进行缓存',
  `monitor_nginx_http_server_location_client_max_body_size` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'client_max_body_size       10m;   #允许客户端请求的最大单文件字节数',
  `monitor_nginx_http_server_location_client_body_buffer_size` double NULL DEFAULT NULL COMMENT 'client_body_buffer_size    128k;  #缓冲区代理缓冲用户端请求的最大字节数',
  `monitor_nginx_http_server_location_proxy_read_timeout` int NULL DEFAULT NULL COMMENT 'proxy_read_timeout 1000;',
  `monitor_nginx_http_server_location_proxy_send_timeout` int NULL DEFAULT NULL COMMENT 'proxy_send_timeout 300;',
  `monitor_nginx_http_server_location_proxy_connect_timeout` int NULL DEFAULT NULL COMMENT 'nginx跟后端服务器连接超时时间(代理连接超时)',
  `monitor_nginx_http_server_location_root` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'root   html;',
  `monitor_nginx_http_server_location_index` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '#定义首页索引文件的名称\\n# index  index.html index.htm;',
  `monitor_nginx_http_server_location_try_files` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '三.总的来说,index的优先级比try_files高,请求会先去找index配置,这里最后一个参数必须存在\\ntry_files $uri $uri/ /index.html',
  `monitor_nginx_http_server_location_condition` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'if ($request_method = \'OPTIONS\') {\\nreturn 204;\\n }',
  `monitor_nginx_http_server_location_limit_req` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '限流； limit_req zone=mylimit burst=5 nodelay;;',
  `monitor_nginx_http_server_location_alias` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'alias /data/nginx/www/;',
  `monitor_nginx_http_server_location_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'proxy: 代理\r\nlocal: 本地',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_nginx_http_server_location_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 560 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '地址解析;\\nwebsocket:\\nlocation /ws {  # 匹配以/ws开头的请求\\nproxy_pass http://websocket_backend;  # 转发请求到WebSocket后端服务器  \\nproxy_http_version 1.1;  # 代理时使用的HTTP版本  \\nproxy_set_header Upgrade $http_upgrade;  # 传递Upgrade头部以支持WebSocket  \\nproxy_set_header Connection upgrade;  # 设置Connection头部为upgrade以支持WebSocket  \\nproxy_set_header Host $host;  # 传递Host头部  \\n # 其他可能的配置，如处理WebSocket特有的超时、缓冲等  \\n }' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_nginx_http_server_location
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_nginx_http_server_location_header
-- ----------------------------
DROP TABLE IF EXISTS `monitor_nginx_http_server_location_header`;
CREATE TABLE `monitor_nginx_http_server_location_header`  (
  `monitor_nginx_http_server_location_header_id` int NOT NULL AUTO_INCREMENT,
  `monitor_nginx_http_server_location_id` int NULL DEFAULT NULL COMMENT 'monitor_nginx_http_server_location表ID',
  `monitor_nginx_http_server_location_header_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `monitor_nginx_http_server_location_header_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '值',
  `monitor_nginx_http_server_location_header_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '类型; add, set',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_nginx_http_server_location_header_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 517 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'nginx消息头' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_nginx_http_server_location_header
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_nginx_upstream
-- ----------------------------
DROP TABLE IF EXISTS `monitor_nginx_upstream`;
CREATE TABLE `monitor_nginx_upstream`  (
  `monitor_nginx_upstream_id` int NOT NULL AUTO_INCREMENT,
  `monitor_nginx_upstream_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `monitor_nginx_upstream_server` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务',
  `monitor_nginx_upstream_max_fails` int NULL DEFAULT NULL COMMENT '失败次数',
  `monitor_nginx_upstream_weight` int NULL DEFAULT NULL COMMENT '权重',
  `monitor_nginx_upstream_type` int NULL DEFAULT NULL COMMENT '0:http',
  `monitor_nginx_upstream_mode` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模式；\\nip_hash： IP 哈希策略根据客户端的 IP 地址进行哈希运算，将相同的请求分配给同一个后端服务器。这种策略适用于需要保持会话（Session）的场景，因为同一个客户端的请求会被发送到同一个服务器，从而避免了会话信息的丢失；\\nleast_conn：最少连接策略将新的请求分配给当前连接数最少的后端服务器。这种策略可以确保每个后端服务器的负载相对均衡，避免某个服务器过载而其他服务器空闲的情况。',
  `monitor_nginx_upstream_udp` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'udp; server 192.168.1.13:53 udp; ',
  `monitor_nginx_server_id` int NULL DEFAULT NULL COMMENT '所属服务',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_nginx_upstream_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '# 负载均衡\\nupstream api_upstream\\n{\\nserver 127.0.0.1:8080 max_fails=3 weight=1;\\nserver 127.0.0.1:8081 max_fails=3 weight=1;\\n}' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_nginx_upstream
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_protection
-- ----------------------------
DROP TABLE IF EXISTS `monitor_protection`;
CREATE TABLE `monitor_protection`  (
  `monitor_protection_id` int NOT NULL AUTO_INCREMENT,
  `monitor_protection_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `monitor_protection_pid` int NULL DEFAULT NULL COMMENT '监听的PID',
  `monitor_protection_status` int NULL DEFAULT NULL COMMENT '是否禁用; 0:启用',
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

-- ----------------------------
-- Records of monitor_protection
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_proxy
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_proxy
-- ----------------------------
INSERT INTO `monitor_proxy` VALUES (5, '这是一个测试代理', NULL, '127.0.0.1', 12345, 'http-proxy', 1, NULL);
INSERT INTO `monitor_proxy` VALUES (8, 'vnc', NULL, '127.0.0.1', 15901, 'websockify', 0, NULL);
INSERT INTO `monitor_proxy` VALUES (9, 'tcp', NULL, '127.0.0.1', 13306, 'tcp-proxy', 0, NULL);

-- ----------------------------
-- Table structure for monitor_proxy_log
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_proxy_log
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_proxy_plugin
-- ----------------------------
DROP TABLE IF EXISTS `monitor_proxy_plugin`;
CREATE TABLE `monitor_proxy_plugin`  (
  `proxy_plugin_id` int NOT NULL AUTO_INCREMENT,
  `proxy_plugin_spi` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件实现',
  `proxy_id` int NULL DEFAULT NULL COMMENT '代理ID',
  `proxy_plugin_sort` int NULL DEFAULT NULL COMMENT '优先级',
  `proxy_plugin_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '插件名称',
  PRIMARY KEY (`proxy_plugin_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '代理-插件关系表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_proxy_plugin
-- ----------------------------
INSERT INTO `monitor_proxy_plugin` VALUES (12, 'ip-limit', 5, NULL, 'IP限流');
INSERT INTO `monitor_proxy_plugin` VALUES (14, 'http-proxy', 5, NULL, 'Http代理');
INSERT INTO `monitor_proxy_plugin` VALUES (15, 'tcp-proxy', 9, NULL, 'Tcp代理');
INSERT INTO `monitor_proxy_plugin` VALUES (16, 'empty', 8, NULL, 'WebSockify代理');

-- ----------------------------
-- Table structure for monitor_proxy_plugin_config
-- ----------------------------
DROP TABLE IF EXISTS `monitor_proxy_plugin_config`;
CREATE TABLE `monitor_proxy_plugin_config`  (
  `proxy_config_id` int NOT NULL AUTO_INCREMENT,
  `proxy_config_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置名称',
  `proxy_config_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置值',
  `proxy_plugin_id` int NULL DEFAULT NULL COMMENT '插件ID',
  `proxy_id` int NULL DEFAULT NULL COMMENT '代理ID',
  PRIMARY KEY (`proxy_config_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '代理 - 组件- 配置' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_proxy_plugin_config
-- ----------------------------
INSERT INTO `monitor_proxy_plugin_config` VALUES (1, 'serviceDiscovery', 'NACOS', 0, 5);
INSERT INTO `monitor_proxy_plugin_config` VALUES (2, 'balance', 'WEIGHT', 0, 5);
INSERT INTO `monitor_proxy_plugin_config` VALUES (3, 'open-log', 'true', 0, 5);
INSERT INTO `monitor_proxy_plugin_config` VALUES (4, 'serviceDiscovery', 'STATISTIC', 0, 9);
INSERT INTO `monitor_proxy_plugin_config` VALUES (5, 'open-log', 'true', 0, 9);
INSERT INTO `monitor_proxy_plugin_config` VALUES (6, 'balance', 'POLLING', 0, 9);
INSERT INTO `monitor_proxy_plugin_config` VALUES (7, 'balance', 'POLLING', 0, 8);
INSERT INTO `monitor_proxy_plugin_config` VALUES (8, 'serviceDiscovery', 'STATISTIC', 0, 8);

-- ----------------------------
-- Table structure for monitor_proxy_plugin_limit
-- ----------------------------
DROP TABLE IF EXISTS `monitor_proxy_plugin_limit`;
CREATE TABLE `monitor_proxy_plugin_limit`  (
  `proxy_config_limit_id` int NOT NULL AUTO_INCREMENT,
  `proxy_config_limit_path_or_ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '限流路径',
  `proxy_config_limit_per_seconds` int NULL DEFAULT NULL COMMENT '每秒限流次数',
  `proxy_config_limit_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '类型;  PATH; IP',
  `proxy_config_limit_disabled` int NULL DEFAULT NULL COMMENT '是否禁用；0: 启用',
  `proxy_plugin_id` int NULL DEFAULT NULL COMMENT '插件ID',
  `proxy_id` int NULL DEFAULT NULL COMMENT '代理ID',
  PRIMARY KEY (`proxy_config_limit_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '代理 - 组件- 限流' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_proxy_plugin_limit
-- ----------------------------
INSERT INTO `monitor_proxy_plugin_limit` VALUES (1, '/', 1, 'PATH', 1, NULL, 5);
INSERT INTO `monitor_proxy_plugin_limit` VALUES (3, '1.1.1.1', 1, 'IP', 1, NULL, 5);

-- ----------------------------
-- Table structure for monitor_proxy_plugin_list
-- ----------------------------
DROP TABLE IF EXISTS `monitor_proxy_plugin_list`;
CREATE TABLE `monitor_proxy_plugin_list`  (
  `proxy_config_list_id` int NOT NULL AUTO_INCREMENT,
  `proxy_config_list` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '黑白名单',
  `proxy_config_list_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '类型;  WHITE; BLACK',
  `proxy_config_list_disabled` int NULL DEFAULT NULL COMMENT '是否禁用；0: 启用',
  `proxy_plugin_id` int NULL DEFAULT NULL COMMENT '插件ID',
  `proxy_id` int NULL DEFAULT NULL COMMENT '代理ID',
  PRIMARY KEY (`proxy_config_list_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '黑白名单' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_proxy_plugin_list
-- ----------------------------
INSERT INTO `monitor_proxy_plugin_list` VALUES (1, '1.1.1.12', 'BLACK', 1, NULL, 5);

-- ----------------------------
-- Table structure for monitor_proxy_plugin_statistic_service_discovery
-- ----------------------------
DROP TABLE IF EXISTS `monitor_proxy_plugin_statistic_service_discovery`;
CREATE TABLE `monitor_proxy_plugin_statistic_service_discovery`  (
  `proxy_statistic_id` int NOT NULL AUTO_INCREMENT,
  `proxy_statistic_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '名称说明',
  `proxy_statistic_url` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '地址前缀',
  `proxy_statistic_protocol` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '协议',
  `proxy_statistic_hostname` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '地址',
  `proxy_statistic_weight` int NULL DEFAULT NULL COMMENT '权重',
  `proxy_id` int NULL DEFAULT NULL COMMENT '代理ID',
  `proxy_statistic_status` int NULL DEFAULT NULL COMMENT '状态;0:禁用',
  PRIMARY KEY (`proxy_statistic_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_bin COMMENT = '静态IP' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_proxy_plugin_statistic_service_discovery
-- ----------------------------
INSERT INTO `monitor_proxy_plugin_statistic_service_discovery` VALUES (3, '百度', '/baidu/', 'http', 'https://www.baidu.com/', 1, 5, 1);
INSERT INTO `monitor_proxy_plugin_statistic_service_discovery` VALUES (5, 'school', '/zxb/school', 'http', 'https://www.qirenit.com', 1, 5, 1);
INSERT INTO `monitor_proxy_plugin_statistic_service_discovery` VALUES (7, 'mysql', '', 'tcp', '172.16.0.40:3306', 1, 9, 1);
INSERT INTO `monitor_proxy_plugin_statistic_service_discovery` VALUES (8, 'vnc', '', 'websockify', '172.16.3.66:5900', 1, 8, 1);

-- ----------------------------
-- Table structure for monitor_sys_gen
-- ----------------------------
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
  `gen_backup_status` int NULL DEFAULT NULL COMMENT '备份状态;0:未开启',
  `gen_jdbc_custom_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT 'JDBC类型',
  `gen_jdbc_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT 'JDBC类型',
  `gen_driver_file` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '驱动文件路径',
  `gen_driver_remote_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '驱动文件下载目录',
  `gen_backup_period` int NULL DEFAULT 720 COMMENT '备份时间分钟(默认720)',
  `gen_backup_event` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备份事件',
  `gen_spilt` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '是否分屏',
  PRIMARY KEY (`gen_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '代码生成器管理' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_sys_gen
-- ----------------------------
INSERT INTO `monitor_sys_gen` VALUES (1, '本地 - MYSQL', 'JDBC', 'root', 'OHFOL3NPS3J0VXB6eGg5N1g5RVludz09', '172.16.0.40', 3306, NULL, '测试1', 'com.mysql.cj.jdbc.Driver', NULL, 'monitor', NULL, 'OtPRaynDelFeVkVl', 1, 'JDBC', 'MYSQL', NULL, NULL, NULL, 'CREATE,DELETE', NULL);
INSERT INTO `monitor_sys_gen` VALUES (5, '测试', 'SHELL', 'root', 'boren1818', '192.168.110.100', 22, NULL, NULL, NULL, NULL, NULL, NULL, 'UW2lRgctX27PRvJK', 0, 'URL', '', NULL, '192.168.110.100:22', NULL, NULL, NULL);
INSERT INTO `monitor_sys_gen` VALUES (9, 'Sqlite', 'JDBC', '', 'Nnl5WXFhZXVCVkFoVWlDYzZ1dmVmZz09', '127.0.0.1', 3306, NULL, '233', 'org.sqlite.JDBC', NULL, '', 'F:\\workspace\\spring-support-api-starter\\gen\\9\\data\\index', 'OjndF5PYVMmD1BOW', NULL, 'FILE', 'SQLITE', NULL, NULL, 720, '', NULL);
INSERT INTO `monitor_sys_gen` VALUES (10, 'H2', 'JDBC', '', NULL, '127.0.0.1', 3306, NULL, NULL, 'org.h2.Driver', NULL, NULL, 'F:\\workspace\\utils-support-parent-starter\\gen\\10\\data\\1.db', NULL, NULL, 'FILE', 'H2', 'F:\\workspace\\utils-support-parent-starter\\gen\\10\\driver\\h2-2.1.214.jar', NULL, 720, '', NULL);
INSERT INTO `monitor_sys_gen` VALUES (11, '112.124.44.21', 'INFLUXDB', 'root', 'ZE9FZGpLeDlkOVJERTBCZE5sczdPUT09', '112.124.44.21', 8086, NULL, NULL, NULL, NULL, 'smartiot', NULL, 'CL32rLzJtPBLkNcS', NULL, NULL, '', NULL, NULL, 720, NULL, NULL);
INSERT INTO `monitor_sys_gen` VALUES (12, 'postgres', 'JDBC', 'postgres', 'root@', '127.0.0.1', 5432, NULL, NULL, 'org.postgresql.Driver', NULL, 'postgres', NULL, 'f5LAJ20FBAjtNVfz', 0, 'URL', 'POSTGRES', NULL, '127.0.0.1:5432', 720, 'UPDATE,DELETE,CREATE', NULL);
INSERT INTO `monitor_sys_gen` VALUES (15, 'zk', 'ZOOKEEPER', NULL, NULL, '172.16.2.226', 2181, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'URL', '', NULL, '172.16.2.226:2181', 720, NULL, NULL);
INSERT INTO `monitor_sys_gen` VALUES (16, 'redis', 'REDIS', '', '', '172.16.0.40', 16379, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'URL', '', NULL, '172.16.0.40:16379', 720, NULL, NULL);
INSERT INTO `monitor_sys_gen` VALUES (17, 'file', 'JDBC', NULL, NULL, NULL, NULL, NULL, '1', 'com.chua.calcite.Driver', NULL, NULL, 'F:\\workspace\\spring-support-api-starter\\gen\\17\\data\\fm_user_form_data.xlsx', NULL, 0, 'FILE', 'CALCITE', NULL, 'null:null', 720, NULL, NULL);
INSERT INTO `monitor_sys_gen` VALUES (19, 'mongodb', 'MONGODB', 'mongodb', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '', NULL, 'undefined:undefined', 720, '', NULL);
INSERT INTO `monitor_sys_gen` VALUES (20, 'webrtc', 'WEBRTC', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '', NULL, 'undefined:undefined', 720, '', NULL);
INSERT INTO `monitor_sys_gen` VALUES (21, '测试vnc', 'VNC', 'chenhua', 'chenhua', '172.16.2.226', 15901, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 'URL', '', NULL, '172.16.2.226:15901', 720, '', NULL);

-- ----------------------------
-- Table structure for monitor_sys_gen_alert_config
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_sys_gen_alert_config
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_sys_gen_alert_push_config
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_sys_gen_alert_push_config
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_sys_gen_backup
-- ----------------------------
DROP TABLE IF EXISTS `monitor_sys_gen_backup`;
CREATE TABLE `monitor_sys_gen_backup`  (
  `monitor_sys_gen_backup_id` int NOT NULL AUTO_INCREMENT,
  `monitor_sys_gen_backup_day` int NULL DEFAULT NULL COMMENT '多少天备份一次',
  `monitor_sys_gen_backup_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备份地址',
  `monitor_sys_gen_backup_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备份结果',
  `monitor_sys_gen_backup_retry` int NULL DEFAULT NULL COMMENT '失败后重试次数',
  `monitor_sys_gen_backup_enable` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否开启',
  `monitor_sys_gen_backup_timeout` int NULL DEFAULT NULL COMMENT '超时时间(min); 默认30分钟',
  `monitor_sys_gen_backup_retention_days` int NULL DEFAULT NULL COMMENT '保存天数(day); 默认90天',
  `monitor_sys_gen_id` int NULL DEFAULT NULL COMMENT '数据源ID',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_backup_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_sys_gen_backup
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_sys_gen_backup_log
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_sys_gen_backup_log
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_sys_gen_column
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_sys_gen_column
-- ----------------------------
INSERT INTO `monitor_sys_gen_column` VALUES (24, 6, 'file_storage_protocol_id', NULL, 'INT', 10, 0, 'Integer', 'fileStorageProtocolId', NULL, '0', '0', '1', '1', '1', '1', 'EQ', 'input', NULL, NULL);
INSERT INTO `monitor_sys_gen_column` VALUES (25, 6, 'file_storage_protocol_port', '开启远程服务器端口', 'INT', 10, 0, 'Integer', 'fileStorageProtocolPort', NULL, '0', '1', '1', '1', '1', '1', 'EQ', 'input', NULL, NULL);
INSERT INTO `monitor_sys_gen_column` VALUES (26, 6, 'file_storage_protocol_desc', '服务器说明', 'VARCHAR', 255, 0, ' String', 'fileStorageProtocolDesc', NULL, '0', '1', '1', '1', '1', '1', 'EQ', 'input', NULL, NULL);
INSERT INTO `monitor_sys_gen_column` VALUES (27, 6, 'file_storage_protocol_name', '服务器协议; HTTP', 'VARCHAR', 255, 0, ' String', 'fileStorageProtocolName', NULL, '0', '1', '1', '1', '1', '1', 'LIKE', 'input', NULL, NULL);
INSERT INTO `monitor_sys_gen_column` VALUES (28, 6, 'file_storage_protocol_host', '开启远程服务器主机', 'VARCHAR', 255, 0, ' String', 'fileStorageProtocolHost', NULL, '0', '1', '1', '1', '1', '1', 'EQ', 'input', NULL, NULL);
INSERT INTO `monitor_sys_gen_column` VALUES (29, 6, 'file_storage_protocol_status', '远程服务器状态; 0:未开启', 'INT', 10, 0, 'Integer', 'fileStorageProtocolStatus', NULL, '0', '1', '1', '1', '1', '1', 'EQ', 'radio', NULL, NULL);
INSERT INTO `monitor_sys_gen_column` VALUES (30, 6, 'file_storage_protocol_plugins', '插件列表，多个逗号分隔', 'VARCHAR', 255, 0, ' String', 'fileStorageProtocolPlugins', NULL, '0', '1', '1', '1', '1', '1', 'EQ', 'input', NULL, NULL);
INSERT INTO `monitor_sys_gen_column` VALUES (31, 6, 'file_storage_protocol_setting', '配置列表，多个逗号分隔', 'VARCHAR', 255, 0, ' String', 'fileStorageProtocolSetting', NULL, '0', '1', '1', '1', '1', '1', 'EQ', 'input', NULL, NULL);
INSERT INTO `monitor_sys_gen_column` VALUES (32, 6, 'file_storage_protocol_plugin_open', '是否开启插件；0:不开启', 'INT', 10, 0, 'Integer', 'fileStorageProtocolPluginOpen', NULL, '0', '1', '1', '1', '1', '1', 'EQ', 'input', NULL, NULL);
INSERT INTO `monitor_sys_gen_column` VALUES (33, 6, 'file_storage_protocol_setting_open', '是否开启配置；0:不开启', 'INT', 10, 0, 'Integer', 'fileStorageProtocolSettingOpen', NULL, '0', '1', '1', '1', '1', '1', 'EQ', 'input', NULL, NULL);
INSERT INTO `monitor_sys_gen_column` VALUES (34, 6, 'file_storage_protocol_rang_open', '是否开启Range；0:不开启', 'INT', 10, 0, 'Integer', 'fileStorageProtocolRangOpen', NULL, '0', '1', '1', '1', '1', '1', 'EQ', 'input', NULL, NULL);
INSERT INTO `monitor_sys_gen_column` VALUES (35, 6, 'file_storage_protocol_ua_open', '是否开启UA；0:不开启', 'INT', 10, 0, 'Integer', 'fileStorageProtocolUaOpen', NULL, '0', '1', '1', '1', '1', '1', 'EQ', 'input', NULL, NULL);
INSERT INTO `monitor_sys_gen_column` VALUES (36, 6, 'file_storage_protocol_download_ua', 'UA', 'VARCHAR', 255, 0, ' String', 'fileStorageProtocolDownloadUa', NULL, '0', '1', '1', '1', '1', '1', 'EQ', 'input', NULL, NULL);
INSERT INTO `monitor_sys_gen_column` VALUES (37, 6, 'file_storage_protocol_ua', 'UA', 'VARCHAR', 255, 0, ' String', 'fileStorageProtocolUa', NULL, '0', '1', '1', '1', '1', '1', 'EQ', 'input', NULL, NULL);
INSERT INTO `monitor_sys_gen_column` VALUES (38, 6, 'file_storage_protocol_preview_or_download', '0: 全部支持;1:预览; 2:下载', 'INT', 10, 0, 'Integer', 'fileStorageProtocolPreviewOrDownload', NULL, '0', '1', '1', '1', '1', '1', 'EQ', 'input', NULL, NULL);
INSERT INTO `monitor_sys_gen_column` VALUES (39, 7, 'file_storage_log_id', NULL, 'INT', 10, 0, 'Integer', 'fileStorageLogId', NULL, '0', '0', '1', '1', '1', '1', 'EQ', 'input', NULL, NULL);
INSERT INTO `monitor_sys_gen_column` VALUES (40, 7, 'file_storage_id', NULL, 'INT', 10, 0, 'Integer', 'fileStorageId', NULL, '0', '1', '1', '1', '1', '1', 'EQ', 'input', NULL, NULL);
INSERT INTO `monitor_sys_gen_column` VALUES (41, 7, 'file_storage_log_address', '请求客户端地址', 'VARCHAR', 255, 0, ' String', 'fileStorageLogAddress', NULL, '0', '1', '1', '1', '1', '1', 'EQ', 'input', NULL, NULL);
INSERT INTO `monitor_sys_gen_column` VALUES (42, 7, 'file_storage_log_url', '请求文件地址', 'VARCHAR', 255, 0, ' String', 'fileStorageLogUrl', NULL, '0', '1', '1', '1', '1', '1', 'EQ', 'input', NULL, NULL);
INSERT INTO `monitor_sys_gen_column` VALUES (43, 8, 'limit_id', NULL, 'INT', 10, 0, 'Integer', 'limitId', NULL, '0', '0', '1', '1', '1', '1', 'EQ', 'input', NULL, NULL);
INSERT INTO `monitor_sys_gen_column` VALUES (44, 8, 'limit_address', '限流ip地址', 'VARCHAR', 255, 0, ' String', 'limitAddress', NULL, '0', '1', '1', '1', '1', '1', 'EQ', 'input', NULL, NULL);
INSERT INTO `monitor_sys_gen_column` VALUES (45, 8, 'limit _type', '限流类型; 0: url限流; 1: ip限流', 'INT', 10, 0, 'Integer', 'limit Type', NULL, '0', '1', '1', '1', '1', '1', 'EQ', 'select', NULL, NULL);
INSERT INTO `monitor_sys_gen_column` VALUES (46, 8, 'limit_url', '限流url地址', 'VARCHAR', 255, 0, ' String', 'limitUrl', NULL, '0', '1', '1', '1', '1', '1', 'EQ', 'input', NULL, NULL);
INSERT INTO `monitor_sys_gen_column` VALUES (47, 8, 'limit_disable', '是否开启； 0：未开启', 'INT', 10, 0, 'Integer', 'limitDisable', NULL, '0', '1', '1', '1', '1', '1', 'EQ', 'input', NULL, NULL);
INSERT INTO `monitor_sys_gen_column` VALUES (48, 8, 'limit_permits_per_second', '每秒生产令牌数', 'INT', 10, 0, 'Integer', 'limitPermitsPerSecond', NULL, '0', '1', '1', '1', '1', '1', 'EQ', 'input', NULL, NULL);
INSERT INTO `monitor_sys_gen_column` VALUES (49, 8, 'proxy_id', '代理ID', 'VARCHAR', 255, 0, ' String', 'proxyId', NULL, '0', '1', '1', '1', '1', '1', 'EQ', 'input', NULL, NULL);
INSERT INTO `monitor_sys_gen_column` VALUES (50, 8, 'create_time', '创建时间', 'DATETIME', 19, 0, 'LocalDateTime', 'createTime', NULL, '0', '1', NULL, NULL, NULL, NULL, 'EQ', 'datetime', NULL, NULL);
INSERT INTO `monitor_sys_gen_column` VALUES (51, 8, 'update_time', '更新时间', 'DATETIME', 19, 0, 'LocalDateTime', 'updateTime', NULL, '0', '1', NULL, NULL, NULL, NULL, 'EQ', 'datetime', NULL, NULL);

-- ----------------------------
-- Table structure for monitor_sys_gen_file_system
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_sys_gen_file_system
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_sys_gen_file_system_part
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_sys_gen_file_system_part
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_sys_gen_file_system_setting
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_sys_gen_file_system_setting
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_sys_gen_global_setting
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_sys_gen_global_setting
-- ----------------------------
INSERT INTO `monitor_sys_gen_global_setting` VALUES (1, '默认全局配置', '系统默认全局配置，控制所有全局任务的开关', 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, NULL, NULL, '2025-12-10 17:24:11', NULL, NULL, '2025-12-10 17:24:11');

-- ----------------------------
-- Table structure for monitor_sys_gen_message_push_template
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_sys_gen_message_push_template
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_sys_gen_prometheus_config
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_sys_gen_prometheus_config
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_sys_gen_remark
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_sys_gen_remark
-- ----------------------------
INSERT INTO `monitor_sys_gen_remark` VALUES (1, '温度', 11, 'live_data_1772504991081914369', 'smartiot', 'A191', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `monitor_sys_gen_remark` VALUES (2, '姓名', 1, 'demo', NULL, 'name', 'sa', 1, '2024-09-27 14:05:26', '2024-09-28 10:32:06', 'sa', 1);
INSERT INTO `monitor_sys_gen_remark` VALUES (3, '年龄', 1, 'demo', NULL, 'age', 'sa', 1, '2024-09-27 14:08:10', NULL, NULL, NULL);
INSERT INTO `monitor_sys_gen_remark` VALUES (4, '测试', 1, 'demo', NULL, 'demo', 'sa', 1, '2024-09-27 14:08:36', NULL, NULL, NULL);
INSERT INTO `monitor_sys_gen_remark` VALUES (5, 'test', 1, 'demo', NULL, 'test', 'sa', 1, '2024-09-27 14:11:46', NULL, NULL, NULL);
INSERT INTO `monitor_sys_gen_remark` VALUES (6, '主键', 1, 'demo', NULL, 'id', 'sa', 1, '2024-09-27 21:48:24', '2024-09-27 21:49:22', 'sa', 1);
INSERT INTO `monitor_sys_gen_remark` VALUES (7, '主键', 9, 'webdav', NULL, 'id', 'sa', 1, '2024-09-28 15:26:52', NULL, NULL, NULL);
INSERT INTO `monitor_sys_gen_remark` VALUES (8, '11', 11, 'live_data_1583007929632772097', NULL, 'A014', 'sa', 1, '2024-09-29 17:13:11', NULL, NULL, NULL);
INSERT INTO `monitor_sys_gen_remark` VALUES (9, '风向', 11, 'live_data_1583270114191671298', NULL, 'A078', 'sa', 1, '2024-09-29 17:29:10', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for monitor_sys_gen_script
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_sys_gen_script
-- ----------------------------
INSERT INTO `monitor_sys_gen_script` VALUES (1, '测试', 'SHELL', '', '#!/bin/bash\n\n# 脚本描述：请添加脚本描述\n# 作者：系统管理员\n# 创建时间：2025/12/10\n\necho \"开始执行脚本...\"\n\n# 在这里添加你的脚本内容\n\necho \"脚本执行完成\"', NULL, 300, 'ENABLED', NULL, NULL, '1.0.0', NULL, NULL, 0, 'admin', 1, '2025-12-10 09:05:55.186488', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for monitor_sys_gen_script_category
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_sys_gen_script_category
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_sys_gen_script_execution
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_sys_gen_script_execution
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_sys_gen_script_upload_record
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_sys_gen_script_upload_record
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_sys_gen_serial
-- ----------------------------
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
  `monitor_sys_gen_serial_status` int NULL DEFAULT NULL COMMENT '状态;0:停用',
  `gen_id` int NULL DEFAULT NULL COMMENT '生成器id',
  PRIMARY KEY (`monitor_sys_gen_serial_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_sys_gen_serial
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_sys_gen_server
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_sys_gen_server
-- ----------------------------
INSERT INTO `monitor_sys_gen_server` VALUES (1, '192.168.110.179', '192.168.110.179', 3389, 'RDP', 'Administrator', '', '', 30000, '', 1, '', 1, '2025-12-10 11:16:08.055860', '连接测试失败', NULL, 1, '[\"192.168.110.79\",\"0.0.0.0\",\"172.16.2.226\",\"172.27.64.1\"]', 'Windows 11', '10.0', 'amd64', 1, 'sa', 1, '2025-07-02 14:57:49.000000', '2025-12-10 00:28:36.817691', 'admin', 1, NULL);
INSERT INTO `monitor_sys_gen_server` VALUES (2, '192.168.110.100', '192.168.110.100', 22, 'SSH', 'root', 'qiren1818', '', 30000, '', 1, '', 1, '2025-12-10 11:16:17.166516', '连接测试失败', NULL, 0, '[]', 'CentOS', '22.04 LTS', 'x86_64', 1, 'sa', 1, '2025-07-02 14:58:25.000000', '2025-12-10 00:38:14.737494', 'admin', 1, 1);

-- ----------------------------
-- Table structure for monitor_sys_gen_server_alert_history
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_sys_gen_server_alert_history
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_sys_gen_server_component
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_sys_gen_server_component
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_sys_gen_server_component_layout
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_sys_gen_server_component_layout
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_sys_gen_server_connection_status
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_sys_gen_server_connection_status
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_sys_gen_server_file_upload_queue
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_sys_gen_server_file_upload_queue
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_sys_gen_server_file_upload_records
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_sys_gen_server_file_upload_records
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_sys_gen_server_file_upload_tasks
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_sys_gen_server_file_upload_tasks
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_sys_gen_server_group
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_sys_gen_server_group
-- ----------------------------
INSERT INTO `monitor_sys_gen_server_group` VALUES (1, '1', '', '#409eff', 'ri:folder-line', 1, 0, 0, '', 'admin', 1, '2025-12-10 00:38:08.057728', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for monitor_sys_gen_server_layout
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_sys_gen_server_layout
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_sys_gen_server_log_statistics
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_sys_gen_server_log_statistics
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_sys_gen_server_logs
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_sys_gen_server_logs
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_sys_gen_server_metrics
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_sys_gen_server_metrics
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_sys_gen_server_script_executions
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_sys_gen_server_script_executions
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_sys_gen_server_scripts
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_sys_gen_server_scripts
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_sys_gen_server_setting
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_sys_gen_server_setting
-- ----------------------------
INSERT INTO `monitor_sys_gen_server_setting` VALUES (1, 1, 0, 0, 0, 'LOCAL', NULL, NULL, 30, NULL, NULL, NULL, NULL, 60, 0, 80.00, 85.00, 90.00, 100.00, 5000, 0, 'EMAIL', '', 30, 0, 30, 1, NULL, NULL, 1, 0, 0, '1.40', 'SHELL', NULL, NULL, NULL, 0, NULL, 0, 'GUACAMOLE', 'localhost', 4822, NULL, NULL, NULL, NULL, 0, NULL, 30, 60, 'sa', 1, '2025-07-02 14:57:49.000000', '2025-12-10 11:16:46.723058', 'admin', 1, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, 0, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `monitor_sys_gen_server_setting` VALUES (2, 2, 0, 0, 0, 'API', 'localhost', 9090, 30, NULL, NULL, NULL, NULL, 60, 0, 80.00, 85.00, 90.00, 100.00, 5000, 0, 'EMAIL', '', 30, 0, 30, 1, NULL, NULL, 1, 0, 0, '1.40', 'SHELL', '192.168.110.100', 2376, NULL, 0, NULL, 0, 'GUACAMOLE', 'localhost', 4822, NULL, NULL, NULL, NULL, 0, NULL, 30, 60, 'sa', 1, '2025-07-02 14:58:25.000000', '2025-12-10 11:19:42.876569', 'admin', 1, 'http://localhost:9090', '/api/v1/query', 0, '', '', 30, '', 60, 0, 30, 0, 60, '', '', 30000, NULL, 'SSH', '', '', 0, 60, 3, 30, 10, NULL);

-- ----------------------------
-- Table structure for monitor_sys_gen_server_setting_backup
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_sys_gen_server_setting_backup
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_sys_gen_server_setting_history
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_sys_gen_server_setting_history
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_sys_gen_shell
-- ----------------------------
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
  `shell_status` int NULL DEFAULT 0 COMMENT '状态;0: 待开启; 1:开启',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`shell_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'SHELL管理' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_sys_gen_shell
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_sys_gen_table
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_sys_gen_table
-- ----------------------------
INSERT INTO `monitor_sys_gen_table` VALUES (6, 'file_storage_protocol', '文件存储服务器协议', '本地 - MYSQL', 1, 'FileStorageProtocol', NULL, 'com', 'storageProtocol', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `monitor_sys_gen_table` VALUES (7, 'file_storage_log', '文件存储日志', '本地 - MYSQL', 1, 'FileStorageLog', NULL, 'com', 'storageLog', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `monitor_sys_gen_table` VALUES (8, 'monitor_proxy_limit', '代理限流器', '本地 - MYSQL', 1, 'MonitorProxyLimit', NULL, 'com', 'proxyLimit', NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for monitor_sys_gen_template
-- ----------------------------
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

-- ----------------------------
-- Records of monitor_sys_gen_template
-- ----------------------------

-- ----------------------------
-- Table structure for node_system
-- ----------------------------
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

-- ----------------------------
-- Records of node_system
-- ----------------------------
INSERT INTO `node_system` VALUES ('142adf753597ef435919e95bd13ea388', '192.168.110.82:19170', 'monitor', '192.168.110.82', 19170, 'OFFLINE', 'monitor', 'unknown', '0', '2025-12-01 02:16:14.179128', '2025-12-01 02:16:14.179128', '2025-12-08 01:16:53.000000', '2025-12-01 02:16:14.179128', NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'admin', 1, '2025-12-01 02:16:14.179128', '2025-12-08 01:16:53.000000', NULL, NULL);
INSERT INTO `node_system` VALUES ('142adf753597ef435919e95bd13ea388', '192.168.110.82:19170', 'monitor', '192.168.110.82', 19170, 'OFFLINE', 'monitor', 'unknown', '0', '2025-12-01 02:16:14.179128', '2025-12-01 02:16:14.179128', '2025-12-08 01:16:53.000000', '2025-12-01 02:16:14.179128', NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'admin', 1, '2025-12-01 02:16:14.179128', '2025-12-08 01:16:53.000000', NULL, NULL);
INSERT INTO `node_system` VALUES ('0296853f19f29e7d095a9cb7486843aa', '172.16.0.40:19180', 'oss', '172.16.0.40', 19180, 'OFFLINE', 'monitor', 'unknown', '0', '2025-12-01 02:16:14.346871', '2025-12-01 02:16:14.346871', '2025-12-08 01:16:53.000000', '2025-12-01 02:16:14.346871', NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'admin', 1, '2025-12-01 02:16:14.346871', '2025-12-08 01:16:53.000000', NULL, NULL);
INSERT INTO `node_system` VALUES ('0296853f19f29e7d095a9cb7486843aa', '172.16.0.40:19180', 'oss', '172.16.0.40', 19180, 'OFFLINE', 'monitor', 'unknown', '0', '2025-12-01 02:16:14.355945', '2025-12-01 02:16:14.355945', '2025-12-08 01:16:53.000000', '2025-12-01 02:16:14.355945', NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'admin', 1, '2025-12-01 02:16:14.355945', '2025-12-08 01:16:53.000000', NULL, NULL);
INSERT INTO `node_system` VALUES ('032b7a5e137faa56be7f903536cacf35', 'DESKTOP-SPEA8V6', 'system', '192.168.1.11', 0, 'OFFLINE', 'monitor', '21.0.9', '0', '2025-12-08 01:16:52.676109', '2025-12-08 20:36:57.001739', '2025-12-08 20:58:11.000000', '2025-12-08 20:36:57.001739', NULL, 118, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'admin', 1, '2025-12-08 01:16:52.676109', '2025-12-08 20:58:11.000000', 'admin', 1);
INSERT INTO `node_system` VALUES ('e3830f999cc499cff647901aaa903d48', 'null:0', 'unknown', NULL, 0, 'OFFLINE', 'monitor', 'unknown', '0', '2025-12-08 20:58:10.103764', '2025-12-08 21:09:31.511299', '2025-12-08 21:25:00.000000', '2025-12-08 21:09:31.511299', NULL, 19, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'admin', 1, '2025-12-08 20:58:10.103764', '2025-12-08 21:25:00.000000', 'admin', 1);
INSERT INTO `node_system` VALUES ('83cdc8b1c4ccc3e3af040c81f32451f8', 'DESKTOP-SPEA8V6', 'system', '192.168.1.11', 18170, 'OFFLINE', 'monitor', '21.0.9', '0', '2025-12-08 21:24:59.306318', '2025-12-08 22:00:47.693315', '2025-12-08 22:47:48.000000', '2025-12-08 22:00:47.693315', NULL, 30, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'admin', 1, '2025-12-08 21:24:59.306318', '2025-12-08 22:47:48.000000', 'admin', 1);
INSERT INTO `node_system` VALUES ('ee03f94c8313872514797ee88ecc01dc', 'DESKTOP-SPEA8V6', 'system', '172.16.3.66', 18170, 'ONLINE', 'monitor', '21.0.9', '1', '2025-12-08 22:47:47.264096', '2025-12-09 23:36:24.846978', NULL, '2025-12-09 23:36:24.846978', NULL, 38, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'admin', 1, '2025-12-08 22:47:47.264096', '2025-12-09 23:36:24.848979', 'admin', 1);

-- ----------------------------
-- Table structure for process_list
-- ----------------------------
DROP TABLE IF EXISTS `process_list`;
CREATE TABLE `process_list`  (
  `process_list_id` int NOT NULL AUTO_INCREMENT COMMENT '进程列表ID',
  `process_list_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '进程列表名称',
  `process_list_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '进程列表描述',
  `process_list_start_command` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '启动命令',
  `process_list_stop_command` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '停止命令',
  `process_list_restart_command` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '重启命令',
  `process_list_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '进程列表状态;0:停用;1:启用',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`process_list_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of process_list
-- ----------------------------

-- ----------------------------
-- Table structure for server_info
-- ----------------------------
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

-- ----------------------------
-- Records of server_info
-- ----------------------------

-- ----------------------------
-- Table structure for server_info_builder
-- ----------------------------
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

-- ----------------------------
-- Records of server_info_builder
-- ----------------------------

-- ----------------------------
-- Table structure for soft_service
-- ----------------------------
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

-- ----------------------------
-- Records of soft_service
-- ----------------------------

-- ----------------------------
-- Table structure for soft_service_install
-- ----------------------------
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

-- ----------------------------
-- Records of soft_service_install
-- ----------------------------

-- ----------------------------
-- Table structure for soft_service_log
-- ----------------------------
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

-- ----------------------------
-- Records of soft_service_log
-- ----------------------------

-- ----------------------------
-- Table structure for soft_service_version
-- ----------------------------
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

-- ----------------------------
-- Records of soft_service_version
-- ----------------------------

-- ----------------------------
-- Table structure for soft_service_version_builder
-- ----------------------------
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

-- ----------------------------
-- Records of soft_service_version_builder
-- ----------------------------

-- ----------------------------
-- Table structure for sys_ai_group
-- ----------------------------
DROP TABLE IF EXISTS `sys_ai_group`;
CREATE TABLE `sys_ai_group`  (
  `sys_ai_group_id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `sys_ai_group_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `sys_project_id` int NULL DEFAULT NULL COMMENT '项目ID',
  `sys_user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账号ID',
  `sys_ai_group_color` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组颜色',
  `sys_ai_group_default` int NULL DEFAULT NULL COMMENT '是否默认分组',
  `sys_ai_group_sort` int NULL DEFAULT NULL COMMENT '分组顺序',
  `sys_ai_group_config` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组配置',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_ai_group_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '分组' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_ai_group
-- ----------------------------

-- ----------------------------
-- Table structure for sys_ai_group_message
-- ----------------------------
DROP TABLE IF EXISTS `sys_ai_group_message`;
CREATE TABLE `sys_ai_group_message`  (
  `sys_ai_group_message_id` int NOT NULL AUTO_INCREMENT,
  `sys_ai_group_id` int NULL DEFAULT NULL COMMENT '分组ID',
  `sys_ai_group_message_role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色',
  `sys_ai_group_message_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '聊天内容',
  `sys_ai_group_message_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '消息类型',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_ai_group_message_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '聊天内容' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_ai_group_message
-- ----------------------------

-- ----------------------------
-- Table structure for sys_ai_module
-- ----------------------------
DROP TABLE IF EXISTS `sys_ai_module`;
CREATE TABLE `sys_ai_module`  (
  `sys_ai_module_id` int NOT NULL AUTO_INCREMENT,
  `sys_ai_module_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模型名称',
  `sys_ai_module_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模型编码',
  `sys_project_id` int NULL DEFAULT NULL COMMENT '项目ID',
  `sys_ai_module_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '说明',
  `sys_ai_module_status` int NULL DEFAULT NULL COMMENT '是否禁用;0:禁用, 1:可用',
  `sys_ai_module_sort` int NULL DEFAULT NULL COMMENT '优先级',
  `sys_ai_module_manufacturers` int NULL DEFAULT NULL COMMENT '厂商(字典表1)',
  `sys_ai_module_role_setting` int NULL DEFAULT NULL COMMENT '是否存在角色设置;0:不存在',
  `sys_ai_module_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模型类型',
  `sys_ai_module_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模型单独地址',
  `sys_ai_module_version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模型版本',
  `sys_ai_module_vlm` int NULL DEFAULT NULL COMMENT '是否是vlm模型;0:否',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_ai_module_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 37 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'ai模型分类表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_ai_module
-- ----------------------------
INSERT INTO `sys_ai_module` VALUES (1, 'Spark Lite', 'Spark Lite', 6, NULL, 1, NULL, 10, 0, 'LLM', NULL, 'lite', 0, 'sa', 1, '2025-02-07 16:38:03', '2025-02-08 11:31:17', 'sa', 1);
INSERT INTO `sys_ai_module` VALUES (2, 'deepseek-chat', 'deepseek-chat', 7, NULL, 1, 0, 21, 0, 'LLM', NULL, NULL, 0, 'sa', 1, '2025-02-08 15:20:14', NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (3, 'moonshot-v1-8k', 'moonshot-v1-8k', 8, NULL, 1, 0, 22, 0, 'LLM', NULL, NULL, 0, 'sa', 1, '2025-02-08 16:40:10', NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (4, 'deepseek-r1:1.5b', 'deepseek-r1:1.5b', 9, NULL, 1, 0, 23, 1, 'LLM', NULL, NULL, 0, 'sa', 1, '2025-02-10 10:17:12', '2025-02-10 11:17:10', 'sa', 1);
INSERT INTO `sys_ai_module` VALUES (5, 'gpt-4o-mini', 'gpt-4o-mini', 11, NULL, 1, 0, 25, 0, 'LLM', NULL, NULL, 0, 'sa', 1, '2025-02-17 09:35:14', NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (6, 'bot-20250220142651-gh6tp', 'bot-20250220142651-gh6tp', 12, NULL, 1, 0, 26, 0, 'LLM', NULL, NULL, 0, 'sa', 1, '2025-02-20 14:43:43', NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (7, 'wanx-v1', 'wanx-v1', 13, NULL, 1, 0, 2, 0, 'VINCENT', NULL, NULL, 0, 'sa', 1, '2025-02-26 11:07:15', NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (8, 'black-forest-labs/FLUX.1-schnell', 'black-forest-labs/FLUX.1-schnell', 14, 'FLUX.1 [schnell] 是一个 120 亿参数的 Rectified Flow Transformer 模型，能够根据文本描述生成图像。该模型采用潜在对抗扩散蒸馏技术训练，可以在1到4步内生成高质量图像。它具有最先进的输出质量和具有竞争力的提示跟随能力，匹配封闭源替代方案的性能。该模型在 Apache 2.0 许可下发布，可用于个人、科学和商业目的', 1, 0, 28, 0, 'VINCENT', NULL, 'v1.0', 0, 'sa', 1, '2025-02-27 16:54:44', '2025-02-28 08:39:31', 'sa', 1);
INSERT INTO `sys_ai_module` VALUES (9, 'stabilityai/stable-diffusion-xl-base-1.0', 'stabilityai/stable-diffusion-xl-base-1.0', 14, 'Stable Diffusion XL（SDXL）是一个基于潜在扩散的文本到图像生成模型，使用了两个固定的预训练文本编码器（OpenCLIP-ViT/G 和 CLIP-ViT/L）。SDXL 由基础模型和精炼模型组成，可以单独使用基础模型或结合两者以获得更好的效果。该模型在生成艺术作品、设计和创意工具应用等方面表现出色，但仅限于研究目的使用', 1, 0, 28, 0, 'VINCENT', NULL, 'v1.0', 0, 'sa', 1, '2025-02-28 10:10:11', '2025-02-28 10:11:41', 'sa', 1);
INSERT INTO `sys_ai_module` VALUES (10, 'stabilityai/stable-diffusion-3-5-large', 'stabilityai/stable-diffusion-3-5-large', 14, 'Stable Diffusion 3.5 Large 是 Stable Diffusion 系列中最强大的基础模型，拥有 80 亿参数。该模型具有卓越的图像质量和更好的提示遵循能力，特别适合在 1 兆像素分辨率下进行专业用途', 1, 2, 28, 0, 'VINCENT', NULL, 'v3.5', 0, 'sa', 1, '2025-02-28 10:44:14', '2025-02-28 10:45:09', 'sa', 1);
INSERT INTO `sys_ai_module` VALUES (11, 'deepseek-ai/DeepSeek-R1-Distill-Qwen-7B', 'deepseek-ai/DeepSeek-R1-Distill-Qwen-7B', 15, 'DeepSeek-R1-Distill-Qwen-7B 是基于 Qwen2.5-Math-7B 通过知识蒸馏得到的模型。该模型使用 DeepSeek-R1 生成的 80 万个精选样本进行微调，展现出优秀的推理能力。在多个基准测试中表现出色，其中在 MATH-500 上达到了 92.8% 的准确率，在 AIME 2024 上达到了 55.5% 的通过率，在 CodeForces 上获得了 1189 的评分，作为 7B 规模的模型展示了较强的数学和编程能力\n', 1, 1, 28, 0, 'LLM', NULL, 'v1.0', 0, 'sa', 1, '2025-02-28 11:30:14', NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (12, 'Qwen/Qwen2.5-Coder-7B-Instruct', 'Qwen/Qwen2.5-Coder-7B-Instruct', 15, 'Qwen2.5-Coder-7B-Instruct 是阿里云发布的代码特定大语言模型系列的最新版本。该模型在 Qwen2.5 的基础上，通过 5.5 万亿个 tokens 的训练，显著提升了代码生成、推理和修复能力。它不仅增强了编码能力，还保持了数学和通用能力的优势。模型为代码智能体等实际应用提供了更全面的基础', 1, 1, 28, 0, 'LLM', NULL, 'v1.0', 0, 'sa', 1, '2025-02-28 13:43:38', NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (13, 'deepseek-ai/DeepSeek-R1-Distill-Qwen-7B', 'deepseek-ai/DeepSeek-R1-Distill-Qwen-7B', 14, 'DeepSeek-R1-Distill-Qwen-7B 是基于 Qwen2.5-Math-7B 通过知识蒸馏得到的模型。该模型使用 DeepSeek-R1 生成的 80 万个精选样本进行微调，展现出优秀的推理能力。在多个基准测试中表现出色，其中在 MATH-500 上达到了 92.8% 的准确率，在 AIME 2024 上达到了 55.5% 的通过率，在 CodeForces 上获得了 1189 的评分，作为 7B 规模的模型展示了较强的数学和编程能力', 1, 1, 28, 0, 'LLM', 'https://api.siliconflow.cn/v1/chat/completions', 'v1.0', 0, 'sa', 1, '2025-02-28 15:21:14', '2025-02-28 15:21:58', 'sa', 1);
INSERT INTO `sys_ai_module` VALUES (14, 'cogview-3-flash', 'cogview-3-flash', 17, 'CogView-3-Flash是智谱AI推出的免费图像生成模型，能够根据用户指令生成符合要求且美学评分更高的图像。CogView-3-Flash主要应用于艺术创作、设计参考、游戏开发、虚拟现实等领域，帮助用户快速实现从文本到图像的转换需求。', 1, 1, 30, 0, 'VINCENT', NULL, 'v1.0', 0, 'sa', 1, '2025-02-28 15:31:07', '2025-02-28 15:44:18', 'sa', 1);
INSERT INTO `sys_ai_module` VALUES (15, 'glm-4-flash', 'glm-4-flash', 17, NULL, 1, 1, 30, 0, 'LLM', NULL, 'v1.0', 0, 'sa', 1, '2025-02-28 15:53:45', NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (16, 'Kwai-Kolors/Kolors', 'Kwai-Kolors/Kolors', 14, 'Kolors 是由快手 Kolors 团队开发的基于潜在扩散的大规模文本到图像生成模型。该模型通过数十亿文本-图像对的训练，在视觉质量、复杂语义准确性以及中英文字符渲染方面展现出显著优势。它不仅支持中英文输入，在理解和生成中文特定内容方面也表现出色', 1, 0, 28, 0, 'VINCENT', NULL, NULL, 0, 'sa', 1, '2025-03-01 17:47:46', NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (17, 'moonshotai/moonlight-16b-a3b-instruct:free', 'moonshotai/moonlight-16b-a3b-instruct:free', 16, 'Moonlight-16B-A3B-Instruct 是由 Moonshot AI 开发的一个具有 160 亿参数的混合专家（MoE）语言模型。它针对指令跟随任务进行了优化，每次推理有 30 亿激活参数。该模型在英语、编码、数学和中文基准测试中，在每 FLOP 的性能方面推进了帕累托前沿。它在性能上优于类似的模型，如 Llama3-3B 和 Deepseek-v2-Lite，同时通过与 Hugging Face 集成以及与流行的推理引擎（如 vLLM12）兼容，保持了高效的部署能力。', 1, 1, 29, 0, 'LLM', NULL, NULL, 0, 'sa', 1, '2025-03-01 19:19:15', NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (18, 'cognitivecomputations/dolphin3.0-r1-mistral-24b:free', 'cognitivecomputations/dolphin3.0-r1-mistral-24b:free', 16, 'Dolphin 3.0 R1 是 Dolphin 系列指令微调模型的下一代产品。旨在成为终极通用本地模型，支持编程、数学、智能代理、函数调用和一般用例。R1 版本经过了 3 个周期的训练，使用来自 Dolphin-R1 数据集的 80 万个推理轨迹进行推理。', 1, 0, 29, 0, 'LLM', NULL, NULL, 0, 'sa', 1, '2025-03-01 19:25:55', NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (19, 'deepseek-r1', 'deepseek-r1', 18, NULL, 1, 0, 31, 0, 'LLM', 'https://cloud.infini-ai.com/maas/v1/chat/completions', NULL, 0, 'sa', 1, '2025-03-01 21:30:36', '2025-03-01 21:30:58', 'sa', 1);
INSERT INTO `sys_ai_module` VALUES (20, 'deepseek-r1-distill-qwen-32b', 'deepseek-r1-distill-qwen-32b', 18, NULL, 1, 0, 31, 0, 'LLM', 'https://cloud.infini-ai.com/maas/v1/chat/completions', NULL, 0, 'sa', 1, '2025-03-01 21:35:34', NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (21, 'deepseek-v3', 'deepseek-v3', 18, NULL, 1, 0, 31, 0, 'LLM', 'https://cloud.infini-ai.com/maas/v1/chat/completions', NULL, 0, 'sa', 1, '2025-03-01 21:37:42', NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (22, '@cf/meta/llama-3.1-8b-instruct', '@cf/meta/llama-3.1-8b-instruct', 11, NULL, 1, 0, 25, 0, 'LLM', NULL, NULL, 0, 'sa', 1, '2025-03-01 22:36:47', NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (23, '@cf/deepseek-ai/deepseek-math-7b-instruct', '@cf/deepseek-ai/deepseek-math-7b-instruct', 11, NULL, 1, 0, 25, 0, 'LLM', NULL, NULL, 0, 'sa', 1, '2025-03-01 22:54:41', NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (24, '@cf/stabilityai/stable-diffusion-xl-base-1.0', '@cf/stabilityai/stable-diffusion-xl-base-1.0', 11, NULL, 1, 0, 32, 0, 'VINCENT', 'https://api.cloudflare.com/client/v4/accounts/f0a671943ec8c499a69ae2b1857f66ae/ai/run/@cf/stabilityai/stable-diffusion-xl-base-1.0', NULL, 0, 'sa', 1, '2025-03-01 23:20:09', '2025-03-02 00:37:09', 'sa', 1);
INSERT INTO `sys_ai_module` VALUES (25, '@cf/black-forest-labs/flux-1-schnell', '@cf/black-forest-labs/flux-1-schnell', 11, NULL, 1, 0, 32, 0, 'VINCENT', 'https://api.cloudflare.com/client/v4/accounts/f0a671943ec8c499a69ae2b1857f66ae/ai/run/@cf/black-forest-labs/flux-1-schnell', NULL, 0, 'sa', 1, '2025-03-02 02:03:48', '2025-03-02 02:41:22', 'sa', 1);
INSERT INTO `sys_ai_module` VALUES (26, '@cf/lykon/dreamshaper-8-lcm', '@cf/lykon/dreamshaper-8-lcm', 11, NULL, 1, 0, 32, 0, 'VINCENT', 'https://api.cloudflare.com/client/v4/accounts/f0a671943ec8c499a69ae2b1857f66ae/ai/run/@cf/lykon/dreamshaper-8-lcm', NULL, 0, 'sa', 1, '2025-03-02 02:03:48', '2025-03-02 02:41:22', 'sa', 1);
INSERT INTO `sys_ai_module` VALUES (27, '@cf/bytedance/stable-diffusion-xl-lightning', '@cf/bytedance/stable-diffusion-xl-lightning', 11, NULL, 1, 0, 32, 0, 'VINCENT', 'https://api.cloudflare.com/client/v4/accounts/f0a671943ec8c499a69ae2b1857f66ae/ai/run/@cf/bytedance/stable-diffusion-xl-lightning', NULL, 0, 'sa', 1, '2025-03-02 02:03:48', '2025-03-02 02:41:22', 'sa', 1);
INSERT INTO `sys_ai_module` VALUES (28, '@cf/runwayml/stable-diffusion-v1-5-img2img', '@cf/runwayml/stable-diffusion-v1-5-img2img', 11, NULL, 1, 0, 32, 0, 'VINCENT', 'https://api.cloudflare.com/client/v4/accounts/f0a671943ec8c499a69ae2b1857f66ae/ai/run/@cf/runwayml/stable-diffusion-v1-5-img2img', NULL, 0, 'sa', 1, '2025-03-02 02:03:48', '2025-03-02 02:41:22', 'sa', 1);
INSERT INTO `sys_ai_module` VALUES (29, '@cf/qwen/qwen1.5-14b-chat-awq', '@cf/qwen/qwen1.5-14b-chat-awq', 11, NULL, 1, 0, 25, 0, 'LLM', NULL, NULL, 0, 'sa', 1, '2025-03-02 03:03:52', NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (30, 'glm-4v-flash', 'glm-4v-flash', 17, NULL, 1, 0, 30, 0, 'LLM', NULL, NULL, 1, 'sa', 1, '2025-03-02 12:35:46', NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (31, 'cogvideox-flash', 'cogvideox-flash', 17, NULL, 1, 1, 30, 0, 'VIDEO', NULL, NULL, 0, 'sa', 1, '2025-03-04 14:35:25', '2025-03-04 15:16:15', 'sa', 1);
INSERT INTO `sys_ai_module` VALUES (32, 'hunyuan-lite', 'hunyuan-lite', 19, NULL, 1, 0, 34, 0, 'LLM', NULL, NULL, 0, 'sa', 1, '2025-03-07 15:57:20', NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (33, 'TextToImageLite', 'TextToImageLite', 19, NULL, 1, 1, 34, 0, 'VINCENT', NULL, NULL, 0, 'sa', 1, '2025-03-07 16:30:43', NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (34, 'HunYuanImage', 'HunYuanImage', 19, NULL, 1, 1, 34, 0, 'VINCENT', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (35, '佐糖', '佐糖', 21, NULL, 1, 0, 38, 0, 'RESOLUTION', NULL, NULL, 0, 'sa', 1, '2025-03-20 13:52:27', NULL, NULL, NULL);
INSERT INTO `sys_ai_module` VALUES (36, '佐糖', '佐糖', 21, NULL, 1, 0, 38, 0, 'COLORIZATION', NULL, 'v1.0.0', 0, 'sa', 1, '2025-03-21 10:35:11', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_ai_vincent_setting
-- ----------------------------
DROP TABLE IF EXISTS `sys_ai_vincent_setting`;
CREATE TABLE `sys_ai_vincent_setting`  (
  `sys_ai_vincent_id` int NOT NULL AUTO_INCREMENT,
  `sys_ai_module_id` int NULL DEFAULT NULL COMMENT '所属模块模型ID',
  `sys_ai_vincent_support_async` int NULL DEFAULT NULL COMMENT '是否支持异步任务;0:不支持',
  `sys_ai_vincent_support_ref_image` int NULL DEFAULT NULL COMMENT '支持模板',
  `sys_ai_vincent_support_audio` int NULL DEFAULT NULL COMMENT '支持ai音效',
  `sys_ai_vincent_supported_style` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支持的风格',
  `sys_ai_vincent_supported_number` int NULL DEFAULT NULL COMMENT '支持输出张数',
  `sys_ai_vincent_supported_size` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支持的尺寸',
  `sys_ai_vincent_supported_max_size` int NULL DEFAULT NULL COMMENT '支持的最大尺寸',
  `sys_ai_vincent_supported_fps` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支持的FPS',
  `sys_ai_vincent_supported_quality` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支持的视频质量',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_ai_vincent_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文生图模型参数' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_ai_vincent_setting
-- ----------------------------
INSERT INTO `sys_ai_vincent_setting` VALUES (1, 7, 1, 1, NULL, '<auto>,<photography>,<portrait>,<3d cartoon>,<anime>,<oil painting>,<watercolor>,<sketch>,<chinese painting>,<flat illustration>', 2, '1024*1024,720*1280,768*1152,1280*720', NULL, NULL, NULL, 'sa', 1, '2025-02-26 11:07:15', '2025-02-26 13:17:00', 'sa', 1);
INSERT INTO `sys_ai_vincent_setting` VALUES (2, 8, 0, NULL, NULL, '<auto>', 1, '1024*1024,720*1280,768*1152,1280*720', NULL, NULL, NULL, 'sa', 1, '2025-02-28 08:41:43', '2025-02-28 08:49:03', 'sa', 1);
INSERT INTO `sys_ai_vincent_setting` VALUES (3, 9, 0, NULL, NULL, '<auto>', 1, '1024*1024,720*1280,768*1152,1280*720', NULL, NULL, NULL, 'sa', 1, '2025-02-28 10:21:17', '2025-02-28 10:27:56', 'sa', 1);
INSERT INTO `sys_ai_vincent_setting` VALUES (4, 10, 0, NULL, NULL, '<auto>', 1, '1024*1024,720*1280,768*1152,1280*720,512*1024,576*1024,1024*576', NULL, NULL, NULL, 'sa', 1, '2025-02-28 10:45:02', '2025-02-28 10:48:08', 'sa', 1);
INSERT INTO `sys_ai_vincent_setting` VALUES (5, 14, 0, 0, NULL, '', 1, '1024*1024,720*1280,768*1152,1280*720,512*1024,576*1024,1024*576', NULL, NULL, NULL, 'sa', 1, '2025-02-28 15:31:27', '2025-03-18 14:22:12', 'sa', 1);
INSERT INTO `sys_ai_vincent_setting` VALUES (6, 16, 0, NULL, NULL, '<auto>', NULL, '1024*1024,720*1280,768*1152,1280*720,512*1024,576*1024,1024*576', NULL, NULL, NULL, 'sa', 1, '2025-03-01 17:48:04', NULL, NULL, NULL);
INSERT INTO `sys_ai_vincent_setting` VALUES (7, 24, 0, NULL, NULL, '<auto>', NULL, '1024*1024,720*1280,768*1152', NULL, NULL, NULL, 'sa', 1, '2025-03-01 23:20:20', NULL, NULL, NULL);
INSERT INTO `sys_ai_vincent_setting` VALUES (8, 25, 0, NULL, NULL, '<auto>', NULL, '1024*1024,720*1280', NULL, NULL, NULL, 'sa', 1, '2025-03-02 02:04:06', '2025-03-02 02:08:14', 'sa', 1);
INSERT INTO `sys_ai_vincent_setting` VALUES (9, 27, 0, NULL, NULL, '<auto>', NULL, '1024*1024,720*1280', NULL, NULL, NULL, 'sa', 1, '2025-03-02 02:51:20', NULL, NULL, NULL);
INSERT INTO `sys_ai_vincent_setting` VALUES (10, 26, 0, NULL, NULL, '<auto>', NULL, '1024*1024', NULL, NULL, NULL, 'sa', 1, '2025-03-02 02:52:04', NULL, NULL, NULL);
INSERT INTO `sys_ai_vincent_setting` VALUES (11, 28, 0, NULL, NULL, '<auto>', NULL, '1024*1024,720*1280,768*1152', NULL, NULL, NULL, 'sa', 1, '2025-03-02 02:58:13', NULL, NULL, NULL);
INSERT INTO `sys_ai_vincent_setting` VALUES (12, 31, 1, 1, 1, '<auto>', 1, '1024*1024', NULL, '', '', 'sa', 1, '2025-03-04 15:14:35', '2025-03-04 15:15:21', 'sa', 1);
INSERT INTO `sys_ai_vincent_setting` VALUES (13, 33, 1, 0, NULL, '<auto>', 1, '1024*1024,720*1280,768*1152', NULL, NULL, NULL, 'sa', 1, '2025-03-07 16:36:31', NULL, NULL, NULL);
INSERT INTO `sys_ai_vincent_setting` VALUES (14, 34, 1, 0, NULL, NULL, 1, '1024*1024', NULL, NULL, NULL, 'sa', 1, '2025-03-07 20:21:12', NULL, NULL, NULL);
INSERT INTO `sys_ai_vincent_setting` VALUES (15, 35, NULL, NULL, NULL, NULL, NULL, '2,4', 2048, NULL, NULL, 'sa', 1, '2025-03-21 09:46:39', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_ai_vincent_style
-- ----------------------------
DROP TABLE IF EXISTS `sys_ai_vincent_style`;
CREATE TABLE `sys_ai_vincent_style`  (
  `sys_ai_vincent_style_id` int NOT NULL AUTO_INCREMENT,
  `sys_ai_module_id` int NULL DEFAULT NULL COMMENT '所属模型',
  `sys_ai_vincent_style_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '风格名称',
  `sys_ai_vincent_style_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '风格编码',
  `sys_ai_vincent_style_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '风格地址',
  PRIMARY KEY (`sys_ai_vincent_style_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 55 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文生图风格' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_ai_vincent_style
-- ----------------------------
INSERT INTO `sys_ai_vincent_style` VALUES (1, 34, '日漫动画', 'riman', 'https://qcloudimg.tencent-cloud.cn/image/document/1899ad3c41b71cbf67564e5e9a3de27d.png');
INSERT INTO `sys_ai_vincent_style` VALUES (2, 34, '水墨画', 'shuimo', 'https://qcloudimg.tencent-cloud.cn/image/document/567818d24ede579ebb47a90dd8749f39.png');
INSERT INTO `sys_ai_vincent_style` VALUES (3, 34, '莫奈', 'minai', 'https://qcloudimg.tencent-cloud.cn/image/document/60f02a8510ed6d55475608f25d6bdc83.jpg');
INSERT INTO `sys_ai_vincent_style` VALUES (4, 34, '扁平插画', 'bianping', 'https://qcloudimg.tencent-cloud.cn/image/document/57dc6b150d8695479203f4887783f1d6.jpg');
INSERT INTO `sys_ai_vincent_style` VALUES (5, 34, '像素插画', 'xiangsu', 'https://qcloudimg.tencent-cloud.cn/image/document/74be9d474c6ba9e8f37468d76f40a4ce.png');
INSERT INTO `sys_ai_vincent_style` VALUES (6, 34, '儿童绘本', 'ertonghuiben', 'https://qcloudimg.tencent-cloud.cn/image/document/28dc717b5f32e810bf53dc10f02948d7.png');
INSERT INTO `sys_ai_vincent_style` VALUES (7, 34, '3D 渲染', '3dxuanran', 'https://qcloudimg.tencent-cloud.cn/image/document/19fa3321dacd17cf7689fd47f8629005.jpg');
INSERT INTO `sys_ai_vincent_style` VALUES (8, 34, '漫画', 'manhua', 'https://qcloudimg.tencent-cloud.cn/image/document/869e471bf04942eb8e0d36da0a4556bf.png');
INSERT INTO `sys_ai_vincent_style` VALUES (9, 34, '黑白漫画', 'heibaimanhua', 'https://qcloudimg.tencent-cloud.cn/image/document/120f20965200a8bd2f838d1a7fe26d9a.png');
INSERT INTO `sys_ai_vincent_style` VALUES (10, 34, '写实', 'xieshi', 'https://qcloudimg.tencent-cloud.cn/image/document/19c066e3ee8f25069b8834a93f337df3.jpg');
INSERT INTO `sys_ai_vincent_style` VALUES (11, 34, '动漫', 'dongman', 'https://qcloudimg.tencent-cloud.cn/image/document/20271b79b5a5aa8ff49ec07bb488edc9.png');
INSERT INTO `sys_ai_vincent_style` VALUES (12, 34, '毕加索', 'bijiasuo', 'https://qcloudimg.tencent-cloud.cn/image/document/dc45004da5225aebb89af48fadc8731c.png');
INSERT INTO `sys_ai_vincent_style` VALUES (13, 34, '赛博朋克', 'saibopengke', 'https://qcloudimg.tencent-cloud.cn/image/document/3e633ceff357c72a1b07db07199669c6.png');
INSERT INTO `sys_ai_vincent_style` VALUES (14, 34, '油画', 'youhua', 'https://qcloudimg.tencent-cloud.cn/image/document/ee58c19eb93298d4175c1d0f88490a94.png');
INSERT INTO `sys_ai_vincent_style` VALUES (15, 34, '马赛克', 'masaike', 'https://qcloudimg.tencent-cloud.cn/image/document/112378e0f4ee1d78c9f306daa8fbff85.png');
INSERT INTO `sys_ai_vincent_style` VALUES (16, 34, '青花瓷', 'qinghuaci', 'https://qcloudimg.tencent-cloud.cn/image/document/d82963705b50cb90ee66bd060177c71b.png');
INSERT INTO `sys_ai_vincent_style` VALUES (17, 34, '新年剪纸画', 'xinnianjianzhi', 'https://qcloudimg.tencent-cloud.cn/image/document/324589129d651b05d23ca336be075cd7.png');
INSERT INTO `sys_ai_vincent_style` VALUES (18, 34, '新年花艺', 'xinnianhuayi', 'https://qcloudimg.tencent-cloud.cn/image/document/92e1acb426ef3268a6b17255d1d6d12b.png');
INSERT INTO `sys_ai_vincent_style` VALUES (19, 7, '默认', '<auto>', 'https://img.alicdn.com/imgextra/i4/O1CN01RzKicz1W0YWzYkWcK_!!6000000002726-2-tps-132-104.png');
INSERT INTO `sys_ai_vincent_style` VALUES (20, 7, '水彩', '<watercolor>', 'https://img.alicdn.com/imgextra/i1/O1CN01M5D6fB1PVVzdwM375_!!6000000001846-2-tps-132-104.png');
INSERT INTO `sys_ai_vincent_style` VALUES (21, 7, '摄影', '<photography>', 'https://img.alicdn.com/imgextra/i1/O1CN01M5D6fB1PVVzdwM375_!!6000000001846-2-tps-132-104.png');
INSERT INTO `sys_ai_vincent_style` VALUES (22, 7, '人像写真', '<portrait>', 'https://img.alicdn.com/imgextra/i1/O1CN01M5D6fB1PVVzdwM375_!!6000000001846-2-tps-132-104.png');
INSERT INTO `sys_ai_vincent_style` VALUES (23, 7, '油画', '<oil painting>', 'https://img.alicdn.com/imgextra/i4/O1CN01wuUOm31wLmtuwafhG_!!6000000006292-2-tps-132-104.png');
INSERT INTO `sys_ai_vincent_style` VALUES (24, 7, '中国画', '<chinese painting>', 'https://img.alicdn.com/imgextra/i3/O1CN01tN84xj1oI6nEhfmL1_!!6000000005201-2-tps-132-104.png');
INSERT INTO `sys_ai_vincent_style` VALUES (25, 7, '扁平插画', '<flat illustration>', 'https://img.alicdn.com/imgextra/i2/O1CN01XMadUe29wy3eouOgg_!!6000000008133-2-tps-132-104.png');
INSERT INTO `sys_ai_vincent_style` VALUES (26, 7, '二次元', '<anime>', 'https://img.alicdn.com/imgextra/i4/O1CN01RIdXyT1Qoa62pyko6_!!6000000002023-2-tps-132-104.png');
INSERT INTO `sys_ai_vincent_style` VALUES (27, 7, '素描', '<sketch>', 'https://img.alicdn.com/imgextra/i1/O1CN017OBYOc1YWjaZbcRpu_!!6000000003067-2-tps-132-104.png');
INSERT INTO `sys_ai_vincent_style` VALUES (28, 7, '3D卡通', '<3d cartoon>', 'https://img.alicdn.com/imgextra/i1/O1CN01W1aZZv1vqBZYXLLrn_!!6000000006223-2-tps-132-104.png');
INSERT INTO `sys_ai_vincent_style` VALUES (29, 33, '默认', '000', 'https://img.alicdn.com/imgextra/i4/O1CN01RzKicz1W0YWzYkWcK_!!6000000002726-2-tps-132-104.png');
INSERT INTO `sys_ai_vincent_style` VALUES (30, 33, '水墨画', '101', 'https://img.alicdn.com/imgextra/i4/O1CN01wuUOm31wLmtuwafhG_!!6000000006292-2-tps-132-104.png');
INSERT INTO `sys_ai_vincent_style` VALUES (31, 33, '概念艺术', '102', 'https://n.sinaimg.cn/sinacn11/720/w1920h1200/20180821/fe95-hhzsnea0733811.jpg');
INSERT INTO `sys_ai_vincent_style` VALUES (32, 33, '油画1', '103', 'https://qcloudimg.tencent-cloud.cn/image/document/ee58c19eb93298d4175c1d0f88490a94.png');
INSERT INTO `sys_ai_vincent_style` VALUES (33, 33, '油画2（梵高）', '118', 'https://5b0988e595225.cdn.sohucs.com/images/20180806/db2a2d66d4bc4924b28b8b8541f5bdf3.jpg');
INSERT INTO `sys_ai_vincent_style` VALUES (34, 33, '水彩画', '104', 'https://img.lucydraw.com/data/upload/d/file/qita/shuicaihua/2015-09-11/1441930227892565.jpg');
INSERT INTO `sys_ai_vincent_style` VALUES (35, 33, '像素画', '105', 'https://img.zcool.cn/community/01958b5c9b0fa9a801214168ab22f7.jpg?x-oss-process=image/auto-orient,1/resize,m_lfit,w_1280,limit_1/sharpen,100');
INSERT INTO `sys_ai_vincent_style` VALUES (36, 33, '厚涂风格', '106', 'https://tse1-mm.cn.bing.net/th/id/OIP-C.wXRGpsVC77SQlqwDAS7W0AHaMr?rs=1&pid=ImgDetMain');
INSERT INTO `sys_ai_vincent_style` VALUES (37, 33, '插图', '107', 'https://img.zcool.cn/community/031crpgdb64vh4s9c28me0s3839.png');
INSERT INTO `sys_ai_vincent_style` VALUES (38, 33, '剪纸风格', '108', 'https://tse4-mm.cn.bing.net/th/id/OIP-C.1AAnOtU3pli62YtDhrkSlQHaJ1?rs=1&pid=ImgDetMain');
INSERT INTO `sys_ai_vincent_style` VALUES (39, 33, '印象派1（莫奈）', '109', 'https://n.sinaimg.cn/sinacn11/75/w2048h1227/20180402/18be-fyssmme6384393.jpg');
INSERT INTO `sys_ai_vincent_style` VALUES (40, 33, '印象派2', '119', 'https://tse2-mm.cn.bing.net/th/id/OIP-C.kj_tF2vSCBW074ywVVkLbQHaF6?rs=1&pid=ImgDetMain');
INSERT INTO `sys_ai_vincent_style` VALUES (41, 33, '2.5D', '110', NULL);
INSERT INTO `sys_ai_vincent_style` VALUES (42, 33, '古典肖像画', '111', 'https://tse1-mm.cn.bing.net/th/id/OIP-C.xIpdK_hc45tsFmKOPD5WZAHaKa?rs=1&pid=ImgDetMain');
INSERT INTO `sys_ai_vincent_style` VALUES (43, 33, '黑白素描画', '112', 'https://c-ssl.duitang.com/uploads/item/201705/28/20170528024853_rz5te.jpeg');
INSERT INTO `sys_ai_vincent_style` VALUES (44, 33, '赛博朋克', '113', 'https://qcloudimg.tencent-cloud.cn/image/document/3e633ceff357c72a1b07db07199669c6.png');
INSERT INTO `sys_ai_vincent_style` VALUES (45, 33, '科幻风格', '114', 'https://img.cgmol.com/excellentwork/20190514/164066_20190514110224xsedrgxjpo9tpq5.jpg');
INSERT INTO `sys_ai_vincent_style` VALUES (46, 33, '暗黑风格', '115', 'https://img.3dmgame.com/UploadFiles/201108/20110802091135913.jpg');
INSERT INTO `sys_ai_vincent_style` VALUES (47, 33, '3D', '116', 'https://qcloudimg.tencent-cloud.cn/image/document/19fa3321dacd17cf7689fd47f8629005.jpg');
INSERT INTO `sys_ai_vincent_style` VALUES (48, 33, '蒸汽波', '117', 'https://pic2.zhimg.com/v2-21e09b4d3f1d2fb50f3d1d63bbef6bbc_720w.jpg?source=172ae18b');
INSERT INTO `sys_ai_vincent_style` VALUES (49, 33, '日系动漫', '201', 'https://qcloudimg.tencent-cloud.cn/image/document/1899ad3c41b71cbf67564e5e9a3de27d.png');
INSERT INTO `sys_ai_vincent_style` VALUES (50, 33, '怪兽风格', '202', 'https://img.redocn.com/sheji/20230908/shouhuifenggeguaiwuchatu_13096310.jpg');
INSERT INTO `sys_ai_vincent_style` VALUES (51, 33, '唯美古风', '203', 'https://ss2.baidu.com/6ONYsjip0QIZ8tyhnq/it/u=1287230597,3924216573&fm=173&app=25&f=JPEG?w=640&h=1139&s=71B432D19110E3C610BDACD8030090B3');
INSERT INTO `sys_ai_vincent_style` VALUES (52, 33, '复古动漫', '204', 'https://c-ssl.duitang.com/uploads/item/202002/04/20200204083412_A5dKK.jpeg');
INSERT INTO `sys_ai_vincent_style` VALUES (53, 33, '游戏卡通手绘', '301', 'https://img.tukuppt.com/png_preview/00/24/81/BfoO7Ynm9x.jpg!/fw/780');
INSERT INTO `sys_ai_vincent_style` VALUES (54, 33, '通用写实风格', '401', 'https://qcloudimg.tencent-cloud.cn/image/document/19c066e3ee8f25069b8834a93f337df3.jpg');

-- ----------------------------
-- Table structure for sys_ai_vincent_task
-- ----------------------------
DROP TABLE IF EXISTS `sys_ai_vincent_task`;
CREATE TABLE `sys_ai_vincent_task`  (
  `sys_ai_vincent_task_id` int NOT NULL AUTO_INCREMENT,
  `sys_ai_vincent_task_uid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求ID',
  `sys_ai_vincent_task_status` int NULL DEFAULT NULL COMMENT '状态;0:未完成; 1: 完成; 2:失败',
  `sys_project_id` int NULL DEFAULT NULL COMMENT '项目ID',
  `sys_ai_module_id` int NULL DEFAULT NULL COMMENT '模型id',
  `sys_ai_vincent_task_condition` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '参数',
  `sys_ai_vincent_task_url` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '图片地址',
  `sys_ai_vincent_task_local_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片本地地址',
  `sys_ai_vincent_task_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '类型',
  `sys_ai_vincent_task_request_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求ID',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_ai_vincent_task_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 258 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文生图任务' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_ai_vincent_task
-- ----------------------------

-- ----------------------------
-- Table structure for sys_ai_vincent_template
-- ----------------------------
DROP TABLE IF EXISTS `sys_ai_vincent_template`;
CREATE TABLE `sys_ai_vincent_template`  (
  `sys_ai_vincent_template_id` int NOT NULL AUTO_INCREMENT,
  `sys_ai_module_id` int NULL DEFAULT NULL,
  `sys_ai_vincent_template_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `sys_ai_vincent_template_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '类型',
  `sys_ai_vincent_template_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地址',
  `sys_ai_vincent_template_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `sys_ai_vincent_template_sort` int NULL DEFAULT NULL COMMENT '优先级',
  `sys_ai_vincent_template_model_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模型ID',
  `sys_ai_vincent_template_model_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模型名称',
  `sys_ai_vincent_template_category` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类',
  `sys_ai_vincent_template_train_model` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '训练模型',
  `sys_ai_vincent_template_recommend_prompt` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '正向关键词',
  `sys_ai_vincent_template_resolutions` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分辨率',
  `sys_ai_vincent_template_word` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关键词',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_ai_vincent_template_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文生图模板' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_ai_vincent_template
-- ----------------------------
INSERT INTO `sys_ai_vincent_template` VALUES (1, 7, '四格漫画', 'sigemanhua', 'https://wanx.alicdn.com/model/sigemanhua.png', '擅长卡通风格小漫画', 1, '301-2.1-t2i-plus', 'sigemanhua', 'combining_images', 'wanx2.1-t2i-plus', '小男孩在树林里玩耍, 两只北极熊在冰川上温馨地交谈, 猫咪试图偷吃鱼，却被主人及时抓住, 机器人好奇地涂上睫毛膏，结果短路烧焦。, 小狗追逐蝴蝶，结果一头撞上大树', '1024*1024', '描述你希望展现的漫画剧情与风格', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_ai_vincent_template` VALUES (2, 7, '情侣头像', 'qinglvtouxiang', 'https://wanx.alicdn.com/model/qinglvtouxiang.png', '擅长清新可爱的情侣头像', 2, '303-2.1-t2i-plus', 'qinglvtouxiang', 'combining_images', 'wanx2.1-t2i-plus', '针织娃娃相对而望，甜蜜的情侣头像, 一对白色毛毡小鸟站在树枝上，轻声细语, 一对情侣戴动物帽子相互微笑，黄色卡通背景, 两只毛毡小狗在绿草地上欢快地追逐嬉戏, 一对白兔子耳朵缠绕在一起，构成情侣头像', '1440*720', '描述你想要的头像内容与风格', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_ai_vincent_template` VALUES (3, 7, '图标印花', 'tubiaoyinhua', 'https://wanx.alicdn.com/model/tubiaoyinhua.png', '擅长设计和展示印花效果', 3, '302-2.1-t2i-plus', 'tubiaoyinhua', 'combining_images', 'wanx2.1-t2i-plus', '面包店的菱形logo应用在招牌上, 健身房的logo设计应用于健身T恤, 花店的logo展示在漂亮的橱窗中, 茶馆的logo应用在精美的茶杯上, 服装店的logo别致地印在衣物标签上', '1248*832', '描述你希望设计的Logo印花样式与贴图效果', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_ai_vincent_template` VALUES (4, 7, '电影分镜', 'dianyingfenjing', 'https://wanx.alicdn.com/model/dianyingfenjing.png', '擅长电影质感画面', 4, '300-2.1-t2i-plus', 'dianyingfenjing', 'combining_images', 'wanx2.1-t2i-plus', '两个人在雪地里一起堆雪人，欢乐无比。, 蒙古草原上的骏马飞驰，场景壮丽。, 圣诞老人点燃烟花，场面充满欢乐。, 火箭从发射升空，壮观地进入太空。, 两人在篝火旁载歌载舞，气氛热烈。', '1072*1424', '描述你希望展现的电影故事主题与画面内容', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_ai_vincent_template` VALUES (5, 7, '新年请神', 'qingshen', 'https://wanx.alicdn.com/model/qingshen.png', '擅长人像', 5, '304-2.1-t2i-plus', 'qingshen', 'style', 'wanx2.1-t2i-plus', '一个女人，双手托着笔记本电脑, 一个女孩捧着鲜花, 一个女孩拿着一杯珍珠奶茶, 一个女孩拿着红包, 一个女孩捧着榴莲，背景里飘着榴莲', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_ai_vincent_template` VALUES (9, 7, '四格漫画2', 'sigemanhua', 'https://wanx.alicdn.com/model/sigemanhua.png', '擅长卡通风格小漫画', 1, '301-2.1-t2i-plus', 'sigemanhua', 'combining_images', 'wanx2.1-t2i-plus', '小男孩在树林里玩耍, 两只北极熊在冰川上温馨地交谈, 猫咪试图偷吃鱼，却被主人及时抓住, 机器人好奇地涂上睫毛膏，结果短路烧焦。, 小狗追逐蝴蝶，结果一头撞上大树', '1024*1024', '描述你希望展现的漫画剧情与风格', 'sa', 1, '2025-03-04 11:08:14', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_bank_bins
-- ----------------------------
DROP TABLE IF EXISTS `sys_bank_bins`;
CREATE TABLE `sys_bank_bins`  (
  `sys_blank_bin_id` int NOT NULL AUTO_INCREMENT,
  `sys_blank_bin_bank_name` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '',
  `sys_blank_bin_bank_name_en` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `sys_blank_bin_card_name` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `sys_blank_bin_card_type` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `sys_blank_bin_bin` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '',
  `sys_blank_bin_n_length` tinyint NULL DEFAULT NULL,
  `sys_blank_bin_bin_length` tinyint(1) NULL DEFAULT NULL,
  `sys_blank_bin_issue_id` int NULL DEFAULT NULL,
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_blank_bin_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2379 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '银行BIN对照表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_bank_bins
-- ----------------------------
INSERT INTO `sys_bank_bins` VALUES (1, '邮政储蓄银行', 'PSBC', '绿卡通', '绿卡通', '621096', 19, 6, 1000000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (2, '邮政储蓄银行', 'PSBC', '绿卡通', '绿卡通', '621098', 19, 6, 1000000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (3, '邮政储蓄银行', 'PSBC', '绿卡银联标准卡', '绿卡银联标准卡', '622150', 19, 6, 1000000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (4, '邮政储蓄银行', 'PSBC', '绿卡银联标准卡', '绿卡银联标准卡', '622151', 19, 6, 1000000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (5, '邮政储蓄银行', 'PSBC', '绿卡专用卡', '绿卡专用卡', '622181', 19, 6, 1000000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (6, '邮政储蓄银行', 'PSBC', '绿卡银联标准卡', '绿卡银联标准卡', '622188', 19, 6, 1000000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (7, '邮政储蓄银行', 'PSBC', '绿卡银联标准卡', '绿卡银联标准卡', '622199', 19, 6, 1000000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (8, '邮政储蓄银行', 'PSBC', '绿卡(银联卡)', '绿卡(银联卡)', '955100', 19, 6, 1000000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (9, '邮政储蓄银行', 'PSBC', '绿卡VIP卡', '绿卡VIP卡', '621095', 19, 6, 1000000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins` VALUES (10, '邮政储蓄银行', 'PSBC', '银联标准卡', '银联标准卡', '620062', 19, 6, 1000000, NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_database_version
-- ----------------------------
DROP TABLE IF EXISTS `sys_database_version`;
CREATE TABLE `sys_database_version`  (
  `sys_database_version_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `sys_database_version_version` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本版本号',
  `sys_database_version_script_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '脚本文件名',
  `sys_database_version_script_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本类型：INIT-初始化，ADD-增量',
  `sys_database_version_description` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本描述信息',
  `sys_database_version_checksum` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本文件MD5校验和',
  `sys_database_version_executed_on` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '脚本执行时间',
  `sys_database_version_success` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执行状态：true-成功，false-失败',
  PRIMARY KEY (`sys_database_version_id`) USING BTREE,
  UNIQUE INDEX `uk_script_name_version`(`sys_database_version_script_name` ASC, `sys_database_version_version` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_database_version
-- ----------------------------
INSERT INTO `sys_database_version` VALUES (1, '1.0.0', 'BASELINE', NULL, 'Initial baseline', 'baseline', '2025-12-04 10:12:03', 'true');
INSERT INTO `sys_database_version` VALUES (2, '1.0', 'V1.0__init_project.sql', 'INIT', 'init project', 'eefccce12d689ad47d4f69f2421ceebf', '2025-12-04 10:13:15', 'true');
INSERT INTO `sys_database_version` VALUES (3, '1.0', 'V1.0__init_service.sql', 'INIT', 'init service', '1fac9fc27f94b1d1a319715bd44f3477', '2025-12-04 10:13:18', 'true');
INSERT INTO `sys_database_version` VALUES (4, '1.0', 'V1.0__init_video.sql', 'INIT', 'init video', '65b0c7a031882cb7a9f51bc8935e5506', '2025-12-04 10:13:19', 'true');
INSERT INTO `sys_database_version` VALUES (5, '1.0', 'V1.0__add_project.sql', 'ADD', 'add project', '0b6a6dc200a546f89c24fbcb9935b126', '2025-12-04 10:13:19', 'true');
INSERT INTO `sys_database_version` VALUES (6, '1.1', 'V1.1__add_sys_message.sql', 'ADD', 'add sys message', 'a8f4d554ae204840a6a2b283de1a762c', '2025-12-04 13:38:31', 'true');
INSERT INTO `sys_database_version` VALUES (7, '1.2', 'V1.2__add_sys_holiday.sql', 'ADD', 'add sys holiday', '764c78365d73a27ddde4b1011f70003f', '2025-12-04 14:58:13', 'true');
INSERT INTO `sys_database_version` VALUES (8, '1.3', 'V1.3__add_sys_file_system.sql', 'ADD', 'add sys file system', 'ce7545f38cb87ae5a59bdc04c9f84e79', '2025-12-10 15:21:34', 'true');

-- ----------------------------
-- Table structure for sys_device
-- ----------------------------
DROP TABLE IF EXISTS `sys_device`;
CREATE TABLE `sys_device`  (
  `sys_device_id` int NOT NULL AUTO_INCREMENT,
  `sys_project_id` int NULL DEFAULT NULL COMMENT '项目ID',
  `sys_device_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `sys_device_serial_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '序列号',
  `sys_device_org_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组织编码',
  `sys_device_org_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组织名称',
  `sys_device_channel_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '管道名称',
  `sys_device_channel_number` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '管道号',
  `sys_device_channel_count` int NULL DEFAULT NULL COMMENT '管道数量',
  `sys_device_status` int NULL DEFAULT NULL COMMENT '是否禁用; 0:启用',
  `sys_device_position` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '位置',
  `sys_device_version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '版本',
  `sys_device_resource_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '资源类型',
  `sys_device_account` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账号',
  `sys_device_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
  `sys_device_rtsp` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '推流地址;存在则使用该地址播放视像头',
  `sys_device_rtsp_webrtc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'rtsp推理webrtc地址; 采用webrtc解析rtsp',
  `sys_device_channels` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '管道',
  `sys_device_owner` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属者唯一编码',
  `sys_device_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `sys_device_online` int NULL DEFAULT NULL COMMENT '是否在线; 0: 离线； 1:在线',
  `sys_device_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备类型',
  `sys_device_latitude` decimal(10, 2) NULL DEFAULT NULL COMMENT '纬度',
  `sys_device_longitude` decimal(10, 2) NULL DEFAULT NULL COMMENT '经度',
  `sys_device_net_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '网络地址',
  `sys_device_net_physical_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '网络地址物理地址',
  `sys_device_validate_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '验证码',
  `sys_device_main_subtype` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '主码流编码',
  `sys_device_sub_subtype` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '子码流编码',
  `sys_dept_id` int NULL DEFAULT NULL COMMENT '机构ID',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_device_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 421 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '摄像头设备管理' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_device
-- ----------------------------

-- ----------------------------
-- Table structure for sys_device_card_event
-- ----------------------------
DROP TABLE IF EXISTS `sys_device_card_event`;
CREATE TABLE `sys_device_card_event`  (
  `sys_device_card_event_id` int NOT NULL,
  `sys_device_card_event_time` datetime NULL DEFAULT NULL COMMENT '时间',
  `sys_device_serial_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '序列号',
  `sys_device_card_event_person_involved` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '涉事人ID',
  `sys_device_card_event_person_involved_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '涉事人姓名',
  `sys_device_card_event_major_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '主事件类型',
  `sys_device_card_event_minor_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '次事件类型',
  `sys_device_card_event_message_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '消息ID',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_device_card_event_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '门禁事件' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_device_card_event
-- ----------------------------

-- ----------------------------
-- Table structure for sys_device_channel
-- ----------------------------
DROP TABLE IF EXISTS `sys_device_channel`;
CREATE TABLE `sys_device_channel`  (
  `sys_device_channel_id` int NOT NULL AUTO_INCREMENT,
  `sys_device_channel_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '通道名称',
  `sys_device_channel_no` int NULL DEFAULT NULL COMMENT '通道号',
  `sys_device_serial_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备序列号',
  `sys_device_channel_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '通道类型，10300:视频通道，10302：报警输入',
  `sys_device_channel_status` int NULL DEFAULT NULL COMMENT '通道状态，0：离线，1：在线，-1：设备未上报',
  `sys_device_channel_use` int NULL DEFAULT NULL COMMENT '通道启用状态：0-禁用；1-启用',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_device_channel_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '设备管道' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_device_channel
-- ----------------------------

-- ----------------------------
-- Table structure for sys_device_message_event
-- ----------------------------
DROP TABLE IF EXISTS `sys_device_message_event`;
CREATE TABLE `sys_device_message_event`  (
  `sys_device_message_event_id` int NOT NULL AUTO_INCREMENT,
  `sys_device_message_event_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '事件类型',
  `sys_device_message_event_event_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '事件ID',
  `sys_device_message_event_data_time` datetime NULL DEFAULT NULL COMMENT '事件时间',
  `sys_device_message_event_employee_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '人员ID',
  `sys_device_message_event_employee_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '姓名',
  `sys_device_message_event_card_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '卡号',
  `sys_device_serial_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备序列号',
  `sys_device_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备名称',
  `sys_device_message_event_channel_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '触发报警的设备通道号',
  `sys_device_message_event_channel_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '触发报警的设备通道名称',
  `sys_device_message_event_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '触发设备通道位置',
  `sys_device_message_event_card_reader_kind` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '读卡器类型：1-IC读卡器，2-身份证读卡器，3-二维码读卡器	',
  `sys_device_message_event_card_reader_no` int NULL DEFAULT NULL COMMENT '读卡器编号',
  `sys_device_message_event_door_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '门编号',
  `sys_device_message_event_event_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '事件消息类型',
  `sys_device_message_event_event_type_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '事件消息类型名称',
  `sys_device_message_event_event_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '事件码',
  `sys_device_message_event_event_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '事件描述',
  `sys_device_message_event_event_source` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '事件来源(目前暂未使用)',
  `sys_device_message_event_event_picture_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片URL',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_device_message_event_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6906 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '事件' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_device_message_event
-- ----------------------------

-- ----------------------------
-- Table structure for sys_device_org
-- ----------------------------
DROP TABLE IF EXISTS `sys_device_org`;
CREATE TABLE `sys_device_org`  (
  `sys_device_org_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '组织ID',
  `sys_device_org_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组织名称',
  `sys_device_org_pid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '上级组织',
  `sys_device_org_tree_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '路径',
  `sys_project_id` int NULL DEFAULT NULL COMMENT '项目ID',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_device_org_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '设备组织' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_device_org
-- ----------------------------

-- ----------------------------
-- Table structure for sys_device_record
-- ----------------------------
DROP TABLE IF EXISTS `sys_device_record`;
CREATE TABLE `sys_device_record`  (
  `sys_camera_record_id` int NOT NULL AUTO_INCREMENT,
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_camera_record_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '摄像头记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_device_record
-- ----------------------------

-- ----------------------------
-- Table structure for sys_dict
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict`  (
  `sys_dict_id` int NOT NULL AUTO_INCREMENT,
  `sys_dict_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典名称',
  `sys_dict_status` int NULL DEFAULT NULL COMMENT '状态; 0:隐藏',
  `sys_dict_in_system` int NULL DEFAULT NULL COMMENT '是否内置; 0:非内置',
  `sys_dict_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典编码, 唯一',
  `sys_dict_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `sys_dict_i18n` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典i18n',
  `sys_dict_pid` int NULL DEFAULT NULL COMMENT 'PID',
  `sys_dict_sort` int NULL DEFAULT NULL COMMENT '排序字段',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_dict_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字典表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict
-- ----------------------------
INSERT INTO `sys_dict` VALUES (1, '厂家', 1, 0, 'CHANG_JIA', '三方厂家', '', 0, NULL, 'sa', NULL, '2024-08-31 20:29:00', '2024-08-31 20:35:57', 'sa', 1);
INSERT INTO `sys_dict` VALUES (2, '模板类型', 1, 0, 'MU_BAN_LEI_XING', '', '', 0, NULL, 'sa', NULL, '2024-09-01 18:16:46', '2024-09-09 09:33:56', 'sa', 1);
INSERT INTO `sys_dict` VALUES (3, '短信', 1, 0, 'DUAN_XIN', '', '', 2, NULL, 'sa', 1, '2024-09-07 09:07:09', '2024-09-09 09:23:22', 'sa', 1);
INSERT INTO `sys_dict` VALUES (4, '微信', 1, 0, 'WEI_XIN', NULL, NULL, 2, NULL, 'sa', 1, '2024-09-09 10:07:50', NULL, NULL, NULL);
INSERT INTO `sys_dict` VALUES (5, '组件功能', 1, 0, 'ZU_JIAN_GONG_NENG', '', '', 0, NULL, 'sa', 1, '2024-10-24 17:17:50', NULL, NULL, NULL);
INSERT INTO `sys_dict` VALUES (6, '项目功能', 1, 0, 'XIANG_MU_GONG_NENG', '', '', 0, NULL, 'sa', 1, '2025-01-24 15:50:58', NULL, NULL, NULL);
INSERT INTO `sys_dict` VALUES (7, '设备类型', 1, 0, 'SHE_BEI_LEI_XING', '', '', 0, NULL, 'sa', 1, '2025-02-11 16:20:16', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_dict_item
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_item`;
CREATE TABLE `sys_dict_item`  (
  `sys_dict_item_id` int NOT NULL AUTO_INCREMENT,
  `sys_dict_id` int NULL DEFAULT NULL COMMENT '字典ID',
  `sys_dict_item_icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标',
  `sys_dict_item_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典子项名称',
  `sys_dict_item_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `sys_dict_item_status` int NULL DEFAULT NULL COMMENT '状态;0: 隐藏',
  `sys_dict_item_sort` int NULL DEFAULT NULL COMMENT '优先级',
  `sys_dict_item_i18n` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典i18n',
  `sys_dict_item_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '编码',
  `sys_dict_item_delete` int NULL DEFAULT NULL COMMENT '是否删除0:未删除',
  `sys_dict_item_property` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '可配置项',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_dict_item_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 41 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字典项子项' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict_item
-- ----------------------------
INSERT INTO `sys_dict_item` VALUES (1, 1, NULL, '海康云曜', '海康云曜', 1, 1, '', 'HAI_KANG_YUN_YAO', 0, NULL, 'sa', NULL, '2024-09-01 10:02:02', '2024-09-01 10:08:45', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (2, 1, NULL, '阿里云', '阿里云', 1, 1, NULL, 'ALIYUN', 0, NULL, 'sa', NULL, '2024-09-01 10:10:03', NULL, NULL, NULL);
INSERT INTO `sys_dict_item` VALUES (3, 1, NULL, '百度云', '百度云', 1, 1, NULL, 'BDYUN', 0, NULL, 'sa', NULL, '2024-09-01 10:10:13', NULL, NULL, NULL);
INSERT INTO `sys_dict_item` VALUES (4, 2, NULL, '短信', '', 1, 1, '', 'SMS', 0, NULL, 'sa', 1, '2024-09-03 13:35:36', '2024-09-09 10:04:58', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (5, 3, NULL, '短信通知', '', 1, 2, '', 'DUAN_XIN_TONG_ZHI', 0, NULL, 'sa', 1, '2024-09-03 13:48:51', '2024-09-05 16:13:21', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (6, 2, NULL, '站内', '', 1, 2, '', 'WEB', 0, NULL, 'sa', 1, '2024-09-03 13:54:31', '2024-09-03 13:55:18', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (7, 2, NULL, '微信', NULL, 1, 2, NULL, 'WECHAT', 0, NULL, 'sa', 1, '2024-09-03 13:55:13', '2024-09-05 15:54:08', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (8, 3, NULL, '验证码', '', 1, 1, '', 'YAN_ZHENG_MA', 0, NULL, 'sa', 1, '2024-09-09 09:24:59', '2024-09-09 09:31:22', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (9, 4, NULL, '微信通知', NULL, 1, NULL, NULL, 'WEI_XIN_TONG_ZHI', 0, NULL, 'sa', 1, '2024-09-09 10:08:38', NULL, NULL, NULL);
INSERT INTO `sys_dict_item` VALUES (10, 1, NULL, '讯飞', '', 1, 1, '', 'XUN_FEI', 0, 'sysProjectAppId,sysProjectAppSecret', 'sa', 1, '2024-10-24 15:07:24', '2025-02-17 10:38:56', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (11, 5, NULL, '天气', '', 1, 1, '', 'TIAN_QI', 0, NULL, 'sa', 1, '2024-10-24 17:18:02', NULL, NULL, NULL);
INSERT INTO `sys_dict_item` VALUES (12, 5, NULL, '时间', '', 1, 1, '', 'SHI_JIAN', 0, NULL, 'sa', 1, '2024-10-28 09:31:55', NULL, NULL, NULL);
INSERT INTO `sys_dict_item` VALUES (13, 6, 'fa-solid:sms', '短信', NULL, 1, 1, NULL, 'DUAN_XIN', 0, NULL, 'sa', 1, '2025-01-24 15:51:14', '2025-01-24 15:51:23', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (14, 6, 'ep:video-camera', '摄像头', '', 1, 1, '', 'SHE_XIANG_TOU', 1, NULL, 'sa', 1, '2025-01-24 15:51:33', '2025-01-24 15:54:21', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (15, 6, 'humbleicons:eye', '人像识别', '', 1, 1, '', 'REN_XIANG_SHI_BIE', 0, NULL, 'sa', 1, '2025-01-25 18:55:49', NULL, NULL, NULL);
INSERT INTO `sys_dict_item` VALUES (16, 6, 'humbleicons:mail', '邮件', '', 1, 1, '', 'YOU_JIAN', 0, NULL, 'sa', 1, '2025-01-27 13:47:56', NULL, NULL, NULL);
INSERT INTO `sys_dict_item` VALUES (17, 6, 'pixelarticons:device-tv-smart', '设备', '', 1, 1, '', 'SHE_BEI', 0, NULL, 'sa', 1, '2025-01-27 13:48:31', '2025-02-06 13:36:28', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (18, 1, 'bi:pc-display-horizontal', '内部系统', '', 1, 1, '', 'NEI_BU_XI_TONG', 0, NULL, 'sa', 1, '2025-01-27 13:49:49', '2025-02-17 10:32:02', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (19, 1, NULL, '海康安防', '海康安防', 1, 1, '', 'HAI_KANG_AN_FANG', 0, 'sysProjectAppId,sysProjectAppSecret,sysProjectEndpoint', 'sa', 1, '2025-02-05 14:43:32', '2025-02-05 14:43:40', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (20, 6, 'ri:robot-2-line', '大语言', '', 1, 1, '', 'DA_YU_YAN', 0, 'sysProjectAppKey,sysProjectEndpoint', 'sa', 1, '2025-02-07 14:47:07', '2025-02-17 10:34:02', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (21, 1, NULL, 'deepseek', '', 1, 1, '', 'deepseek', 0, NULL, 'sa', 1, '2025-02-08 14:28:56', '2025-02-17 10:31:59', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (22, 1, NULL, 'kimi', '', 1, 1, '', 'kimi', 0, NULL, 'sa', 1, '2025-02-08 16:33:03', '2025-02-17 10:31:57', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (23, 1, NULL, 'ollama', '', 1, 1, '', 'ollama', 0, NULL, 'sa', 1, '2025-02-10 10:16:25', '2025-02-17 10:04:54', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (24, 7, 'mingcute:computer-camera-fill', '摄像头', '', 1, 1, '', 'CAMERA', 0, NULL, 'sa', 1, '2025-02-11 16:37:27', '2025-02-11 16:46:19', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (25, 1, NULL, 'OpenAi', '', 1, 1, '', 'openai', 0, NULL, 'sa', 1, '2025-02-17 09:34:25', '2025-02-17 10:06:38', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (26, 1, NULL, '豆包', '', 1, 1, '', 'DOUBAO', 0, NULL, 'sa', 1, '2025-02-20 14:39:27', '2025-02-20 14:39:56', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (27, 6, 'ri:inbox-unarchive-line', '文生图', '', 1, 1, '', 'WEN_SHENG_TU', 0, NULL, 'sa', 1, '2025-02-26 10:36:37', '2025-02-28 11:24:54', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (28, 1, NULL, '硅基流动', '', 1, 1, '', 'SILICONFLOW', 0, NULL, 'sa', 1, '2025-02-27 16:20:06', '2025-02-27 16:20:44', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (29, 1, NULL, 'OpenRouter', '', 1, 1, '', 'OPEN_ROUTER', 0, NULL, 'sa', 1, '2025-02-28 13:57:56', NULL, NULL, NULL);
INSERT INTO `sys_dict_item` VALUES (30, 1, NULL, '智谱清言', '', 1, 1, '', 'ZHIPU', 0, NULL, 'sa', 1, '2025-02-28 15:28:05', NULL, NULL, NULL);
INSERT INTO `sys_dict_item` VALUES (31, 1, NULL, '无问芯穹', '', 1, 1, '', 'WU_WEN_XIN_QIONG', 0, NULL, 'sa', 1, '2025-03-01 21:28:23', NULL, NULL, NULL);
INSERT INTO `sys_dict_item` VALUES (32, 1, NULL, 'Cloudflare', '', 1, 1, '', 'Cloudflare', 0, NULL, 'sa', 1, '2025-03-01 22:34:45', NULL, NULL, NULL);
INSERT INTO `sys_dict_item` VALUES (33, 6, 'ep:video-camera', '文生视频', '', 1, 1, '', 'WEN_SHENG_SHI_PIN', 0, NULL, 'sa', 1, '2025-03-04 14:32:30', '2025-03-04 14:34:29', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (34, 1, NULL, '腾讯', '', 1, 1, '', 'TENCENT', 0, NULL, 'sa', 1, '2025-03-07 15:38:26', NULL, NULL, NULL);
INSERT INTO `sys_dict_item` VALUES (35, 1, 'ep:camera-filled', '海康云眸', '', 1, 1, '', 'HAI_KANG_YUN_MOU', 0, NULL, 'sa', 1, '2025-03-17 08:45:39', NULL, NULL, NULL);
INSERT INTO `sys_dict_item` VALUES (36, 7, 'ep:credit-card', '门禁', '', 1, 1, '', 'MEN_JIN', 0, NULL, 'sa', 1, '2025-03-17 10:51:36', '2025-03-17 10:51:48', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (37, 5, NULL, '日历', '', 1, 1, '', 'RI_LI', 0, NULL, 'sa', 1, '2025-03-18 15:03:56', NULL, NULL, NULL);
INSERT INTO `sys_dict_item` VALUES (38, 1, NULL, '佐糖', '', 1, 1, '', 'ZUO_TANG', 0, NULL, 'sa', 1, '2025-03-20 13:41:08', NULL, NULL, NULL);
INSERT INTO `sys_dict_item` VALUES (39, 6, 'simple-icons:davinciresolve', '超分辨率', '', 1, 1, '', 'RESOLUTION', 0, NULL, 'sa', 1, '2025-03-20 13:44:02', '2025-03-20 13:45:14', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (40, 6, 'mingcute:color-picker-fill', '图像上色', '', 1, 1, '', 'COLORIZATION', 0, NULL, 'sa', 1, '2025-03-21 10:29:53', '2025-03-21 10:30:05', 'sa', 1);

-- ----------------------------
-- Table structure for sys_dict_item_property
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_item_property`;
CREATE TABLE `sys_dict_item_property`  (
  `sys_dict_item_property_id` int NOT NULL AUTO_INCREMENT,
  `sys_dict_item_id` int NULL DEFAULT NULL COMMENT '字典子项ID',
  `sys_dict_item_property_group` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置项分组',
  `sys_dict_item_property_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置项',
  `sys_dict_item_property_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置值',
  `sys_dict_item_property_selected_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '选中类型',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_dict_item_property_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 33 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字典配置项' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict_item_property
-- ----------------------------
INSERT INTO `sys_dict_item_property` VALUES (14, 20, 'DA_YU_YAN', 'sysProjectAppKey', 'true', 'Boolean', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_dict_item_property` VALUES (19, 20, 'DA_YU_YAN', 'sysProjectEndpoint', 'true', 'Boolean', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_dict_item_property` VALUES (21, 10, 'DA_YU_YAN', 'sysProjectAppId', 'true', 'Boolean', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_dict_item_property` VALUES (22, 10, 'DA_YU_YAN', 'sysProjectAppSecret', 'true', 'Boolean', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_dict_item_property` VALUES (23, 23, 'DA_YU_YAN', 'sysProjectAppKey', 'false', 'Boolean', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_dict_item_property` VALUES (25, 19, 'DEVICE', 'sysProjectAppId', 'true', 'Boolean', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_dict_item_property` VALUES (26, 19, 'DEVICE', 'sysProjectAppSecret', 'true', 'Boolean', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_dict_item_property` VALUES (27, 19, 'DEVICE', 'sysProjectEndpoint', 'true', 'Boolean', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_dict_item_property` VALUES (29, 27, 'WEN_SHENG_TU', 'sysProjectAppKey', 'true', 'Boolean', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_dict_item_property` VALUES (30, 27, 'WEN_SHENG_TU', 'sysProjectEndpoint', 'true', 'Boolean', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_dict_item_property` VALUES (31, 34, 'DA_YU_YAN', 'sysProjectAppSecret', 'true', 'Boolean', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_dict_item_property` VALUES (32, 34, 'DA_YU_YAN', 'sysProjectAppId', 'true', 'Boolean', NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_email_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_email_log`;
CREATE TABLE `sys_email_log`  (
  `sys_email_log_id` int NOT NULL AUTO_INCREMENT,
  `sys_email_log_target` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '接收人',
  `sys_email_log_template_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板ID',
  `sys_email_log_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '参数',
  `sys_email_log_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '发送的模板内容',
  `sys_email_cost` decimal(10, 2) NULL DEFAULT NULL COMMENT '耗时时间',
  `sys_email_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误码',
  `sys_email_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '错误信息',
  `sys_email_app_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮件厂家',
  `sys_email_app_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮件密钥',
  `sys_email_app_sign` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '签名',
  `sys_email_read` int NULL DEFAULT NULL COMMENT '是否已读;0:未读',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  `sys_project_id` int NULL DEFAULT NULL COMMENT '项目ID',
  PRIMARY KEY (`sys_email_log_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '邮件下发日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_email_log
-- ----------------------------
INSERT INTO `sys_email_log` VALUES (6, 'yemengzhidiandi@163.com', '1', '{}', '<p><strong>测试</strong></p>', 2392.00, '00000', NULL, 'NEI_BU_XI_TONG', NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_email_log` VALUES (7, 'yemengzhidiandi@163.com', '1', '{}', '<p><strong>测试</strong></p>', 1348.00, 'SUCCESS', NULL, 'NEI_BU_XI_TONG', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, 2);
INSERT INTO `sys_email_log` VALUES (8, 'yemengzhidiandi@163.com', '1', NULL, '<p><strong>测试</strong></p>', 893.00, 'SUCCESS', NULL, 'NEI_BU_XI_TONG', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, 2);
INSERT INTO `sys_email_log` VALUES (9, 'request.getParams()', '1', NULL, '<p><strong>测试</strong></p>', 40.00, 'FAILURE', NULL, 'NEI_BU_XI_TONG', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, 2);
INSERT INTO `sys_email_log` VALUES (10, 'yemengzhidiandi@163.com', '1', NULL, '<p><strong>测试</strong></p>', 917.00, 'SUCCESS', NULL, 'NEI_BU_XI_TONG', NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, 2);

-- ----------------------------
-- Table structure for sys_email_template
-- ----------------------------
DROP TABLE IF EXISTS `sys_email_template`;
CREATE TABLE `sys_email_template`  (
  `sys_email_template_id` int NOT NULL AUTO_INCREMENT,
  `sys_project_id` int NULL DEFAULT NULL COMMENT '项目ID',
  `sys_project_type` int NULL DEFAULT NULL COMMENT '类别字典表(dictId=6)',
  `sys_email_template_sort` int NULL DEFAULT NULL COMMENT '模板优先级',
  `sys_email_template_category` int NULL DEFAULT NULL COMMENT '子类型; (dictId = 2)',
  `sys_email_template_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `sys_email_template_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '唯一编码',
  `sys_email_template_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '模板内容',
  `sys_email_template_delete` int NULL DEFAULT NULL COMMENT '是否删除0:未删除',
  `sys_email_template_status` int NULL DEFAULT NULL COMMENT '是否启用:0:未启用',
  `sys_email_template_type` int NULL DEFAULT NULL COMMENT '模板类型;0:系统;1:用户;2:其它',
  `sys_email_template_default` int NULL DEFAULT NULL COMMENT '是否默认:0:未启用',
  `sys_email_template_approve` int NULL DEFAULT NULL COMMENT '审批状态;0:未审批; 1: 审批中; 2:已通过; 3: 未通过',
  `sys_email_template_approve_reject` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '拒绝原因',
  `sys_email_template_approve_create_by` int NULL DEFAULT NULL COMMENT '审批人',
  `sys_email_template_disabled` int NULL DEFAULT NULL COMMENT '模板编辑状态;0:可编辑; 1: 不可编辑;',
  `sys_email_template_origin` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源',
  `sys_email_template_origin_uuid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源uuid',
  `sys_email_template_callback_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '回调地址',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_email_template_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '邮件模板' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_email_template
-- ----------------------------
INSERT INTO `sys_email_template` VALUES (1, 2, NULL, 1, 4, '测试', '111', '<p><strong>测试</strong></p>', NULL, 1, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 'http://127.0.0.1:18170/system/api/v2/project/template/email/callback', 'sa', 1, '2025-01-27 20:00:31', '2025-01-27 20:04:46', 'sa', 1);

-- ----------------------------
-- Table structure for sys_file_system
-- ----------------------------
DROP TABLE IF EXISTS `sys_file_system`;
CREATE TABLE `sys_file_system`  (
  `sys_file_system_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `sys_file_system_tid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务唯一标识',
  `sys_file_system_bucket` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '存储桶',
  `sys_file_system_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件名称',
  `sys_file_system_original_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '原始名称',
  `sys_file_system_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '存储类型',
  `sys_file_system_extension` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '扩展名',
  `sys_file_system_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件路径',
  `sys_file_system_size` bigint NULL DEFAULT NULL COMMENT '文件大小',
  `sys_file_system_md5` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件MD5',
  `sys_file_system_chunk_total` int NULL DEFAULT NULL COMMENT '分片数量',
  `sys_file_system_chunk_uploaded` int NULL DEFAULT 0 COMMENT '已上传分片数',
  `sys_file_system_chunk_size` bigint NULL DEFAULT NULL COMMENT '分片大小',
  `sys_file_system_version` int NULL DEFAULT 0 COMMENT '版本号',
  `sys_file_system_status` int NULL DEFAULT 0 COMMENT '状态',
  `sys_file_system_http_enabled` tinyint(1) NULL DEFAULT 0 COMMENT '是否启用HTTP',
  `sys_file_system_http_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'HTTP访问URL',
  `sys_file_system_service_id` int NULL DEFAULT NULL COMMENT '绑定服务ID',
  `sys_file_system_group_id` int NULL DEFAULT NULL COMMENT '分组ID',
  `sys_file_system_upload_start_time` datetime NULL DEFAULT NULL COMMENT '上传开始时间',
  `sys_file_system_upload_complete_time` datetime NULL DEFAULT NULL COMMENT '上传完成时间',
  `sys_file_system_local_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '本地临时路径',
  `sys_file_system_summary` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_file_system_id`) USING BTREE,
  UNIQUE INDEX `uk_tid`(`sys_file_system_tid` ASC) USING BTREE,
  INDEX `idx_status`(`sys_file_system_status` ASC) USING BTREE,
  INDEX `idx_group_id`(`sys_file_system_group_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文件系统主表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_file_system
-- ----------------------------

-- ----------------------------
-- Table structure for sys_file_system_group
-- ----------------------------
DROP TABLE IF EXISTS `sys_file_system_group`;
CREATE TABLE `sys_file_system_group`  (
  `sys_file_system_group_id` int NOT NULL AUTO_INCREMENT COMMENT '分组ID',
  `sys_file_system_group_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组名称',
  `sys_file_system_group_parent_id` int NULL DEFAULT 0 COMMENT '父分组ID',
  `sys_file_system_group_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组路径',
  `sys_file_system_group_full_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组完整路径',
  `sys_file_system_group_level` int NULL DEFAULT 1 COMMENT '分组层级',
  `sys_file_system_group_tree_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组树路径',
  `sys_file_system_group_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组描述',
  `sys_file_system_group_is_default` tinyint(1) NULL DEFAULT 0 COMMENT '是否默认分组',
  `sys_file_system_group_sort` int NULL DEFAULT 0 COMMENT '分组排序',
  `sys_file_system_group_status` int NULL DEFAULT 1 COMMENT '分组状态',
  `sys_file_system_group_icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组图标',
  `sys_file_system_group_color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组颜色',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_file_system_group_id`) USING BTREE,
  INDEX `idx_parent_id`(`sys_file_system_group_parent_id` ASC) USING BTREE,
  INDEX `idx_status`(`sys_file_system_group_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文件系统分组表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_file_system_group
-- ----------------------------
INSERT INTO `sys_file_system_group` VALUES (1, '默认分组', 0, 'default', '/default', 1, 'default', NULL, 1, 1, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_file_system_group` VALUES (2, '图片', 0, 'images', '/images', 1, 'images', NULL, 0, 2, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_file_system_group` VALUES (3, '视频', 0, 'videos', '/videos', 1, 'videos', NULL, 0, 3, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_file_system_group` VALUES (4, '文档', 0, 'documents', '/documents', 1, 'documents', NULL, 0, 4, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_file_system_group` VALUES (5, '音频', 0, 'audios', '/audios', 1, 'audios', NULL, 0, 5, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_file_system_part
-- ----------------------------
DROP TABLE IF EXISTS `sys_file_system_part`;
CREATE TABLE `sys_file_system_part`  (
  `sys_file_system_part_id` int NOT NULL AUTO_INCREMENT COMMENT '分片ID',
  `sys_file_system_part_file_id` int NOT NULL COMMENT '文件ID',
  `sys_file_system_part_seq` int NOT NULL COMMENT '分片序号',
  `sys_file_system_part_size` bigint NULL DEFAULT NULL COMMENT '分片大小',
  `sys_file_system_part_md5` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分片MD5',
  `sys_file_system_part_local_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '本地存储路径',
  `sys_file_system_part_version` int NULL DEFAULT 0 COMMENT '版本号',
  `sys_file_system_part_status` int NULL DEFAULT 0 COMMENT '状态',
  `sys_file_system_part_upload_time` datetime NULL DEFAULT NULL COMMENT '上传时间',
  `sys_file_system_part_process_time` datetime NULL DEFAULT NULL COMMENT '处理时间',
  `sys_file_system_part_error_message` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误信息',
  `sys_file_system_part_retry_count` int NULL DEFAULT 0 COMMENT '重试次数',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_file_system_part_id`) USING BTREE,
  INDEX `idx_file_id`(`sys_file_system_part_file_id` ASC) USING BTREE,
  INDEX `idx_status`(`sys_file_system_part_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文件系统分片表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_file_system_part
-- ----------------------------

-- ----------------------------
-- Table structure for sys_file_system_setting
-- ----------------------------
DROP TABLE IF EXISTS `sys_file_system_setting`;
CREATE TABLE `sys_file_system_setting`  (
  `sys_file_system_setting_id` int NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `sys_file_system_setting_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置名称',
  `sys_file_system_setting_chunk_enabled` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用分片上传',
  `sys_file_system_setting_chunk_size_mb` int NULL DEFAULT 100 COMMENT '分片大小MB',
  `sys_file_system_setting_auto_merge_enabled` tinyint(1) NULL DEFAULT 1 COMMENT '是否开启自动合并',
  `sys_file_system_setting_merge_task_limit` int NULL DEFAULT 10 COMMENT '合并任务数量限制',
  `sys_file_system_setting_manual_merge_enabled` tinyint(1) NULL DEFAULT 1 COMMENT '是否支持手动合并',
  `sys_file_system_setting_http_server_enabled` tinyint(1) NULL DEFAULT 0 COMMENT '是否开启HTTP服务',
  `sys_file_system_setting_http_server_port` int NULL DEFAULT 8888 COMMENT 'HTTP服务端口',
  `sys_file_system_setting_http_server_host` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0.0.0.0' COMMENT 'HTTP服务主机',
  `sys_file_system_setting_http_server_context` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '/file' COMMENT 'HTTP上下文路径',
  `sys_file_system_setting_download_enabled` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用下载',
  `sys_file_system_setting_preview_enabled` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用预览',
  `sys_file_system_setting_http_access_domain` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'HTTP访问域名',
  `sys_file_system_setting_allowed_types` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件类型白名单',
  `sys_file_system_setting_max_file_size_mb` int NULL DEFAULT 1024 COMMENT '单文件最大MB',
  `sys_file_system_setting_storage_root_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '存储根路径',
  `sys_file_system_setting_temp_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '临时文件路径',
  `sys_file_system_setting_retention_days` int NULL DEFAULT 0 COMMENT '文件保留天数',
  `sys_file_system_setting_enabled` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用',
  `sys_file_system_setting_default_storage_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'local' COMMENT '默认存储类型',
  `sys_file_system_setting_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_file_system_setting_id`) USING BTREE,
  INDEX `idx_enabled`(`sys_file_system_setting_enabled` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文件系统配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_file_system_setting
-- ----------------------------
INSERT INTO `sys_file_system_setting` VALUES (1, '默认配置', 1, 100, 1, 10, 1, 0, 8888, '0.0.0.0', '/file', 1, 1, NULL, NULL, 1024, NULL, NULL, 0, 1, 'local', NULL, NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_holiday
-- ----------------------------
DROP TABLE IF EXISTS `sys_holiday`;
CREATE TABLE `sys_holiday`  (
  `sys_holiday_id` int NOT NULL AUTO_INCREMENT COMMENT '节假日ID',
  `sys_holiday_year` int NULL DEFAULT NULL COMMENT '节假日年份',
  `sys_holiday_date` date NULL DEFAULT NULL COMMENT '节假日日期',
  `sys_holiday_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '节假日名称',
  `sys_holiday_is_holiday` tinyint(1) NULL DEFAULT 1 COMMENT '是否节假日: 1-是, 0-否(调休工作日)',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`sys_holiday_id`) USING BTREE,
  UNIQUE INDEX `uk_date`(`sys_holiday_date` ASC) USING BTREE COMMENT '日期唯一索引',
  INDEX `idx_year`(`sys_holiday_year` ASC) USING BTREE COMMENT '年份索引',
  INDEX `idx_date`(`sys_holiday_date` ASC) USING BTREE COMMENT '日期索引'
) ENGINE = InnoDB AUTO_INCREMENT = 34 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '节假日表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_holiday
-- ----------------------------
INSERT INTO `sys_holiday` VALUES (1, 2025, '2025-01-01', '元旦', 1, 1, 'admin', '2025-12-04 14:58:56', NULL, NULL, NULL);
INSERT INTO `sys_holiday` VALUES (2, 2025, '2025-01-26', '春节前补班', 0, 1, 'admin', '2025-12-04 14:58:56', NULL, NULL, NULL);
INSERT INTO `sys_holiday` VALUES (3, 2025, '2025-01-28', '除夕', 1, 1, 'admin', '2025-12-04 14:58:56', NULL, NULL, NULL);
INSERT INTO `sys_holiday` VALUES (4, 2025, '2025-01-29', '初一', 1, 1, 'admin', '2025-12-04 14:58:56', NULL, NULL, NULL);
INSERT INTO `sys_holiday` VALUES (5, 2025, '2025-01-30', '初二', 1, 1, 'admin', '2025-12-04 14:58:56', NULL, NULL, NULL);
INSERT INTO `sys_holiday` VALUES (6, 2025, '2025-01-31', '初三', 1, 1, 'admin', '2025-12-04 14:58:56', NULL, NULL, NULL);
INSERT INTO `sys_holiday` VALUES (7, 2025, '2025-02-01', '初四', 1, 1, 'admin', '2025-12-04 14:58:56', NULL, NULL, NULL);
INSERT INTO `sys_holiday` VALUES (8, 2025, '2025-02-02', '初五', 1, 1, 'admin', '2025-12-04 14:58:56', NULL, NULL, NULL);
INSERT INTO `sys_holiday` VALUES (9, 2025, '2025-02-03', '初六', 1, 1, 'admin', '2025-12-04 14:58:56', NULL, NULL, NULL);
INSERT INTO `sys_holiday` VALUES (10, 2025, '2025-02-04', '初七', 1, 1, 'admin', '2025-12-04 14:58:56', NULL, NULL, NULL);
INSERT INTO `sys_holiday` VALUES (11, 2025, '2025-02-08', '春节后补班', 0, 1, 'admin', '2025-12-04 14:58:56', NULL, NULL, NULL);
INSERT INTO `sys_holiday` VALUES (12, 2025, '2025-04-04', '清明节', 1, 1, 'admin', '2025-12-04 14:58:56', NULL, NULL, NULL);
INSERT INTO `sys_holiday` VALUES (13, 2025, '2025-04-05', '清明节', 1, 1, 'admin', '2025-12-04 14:58:56', NULL, NULL, NULL);
INSERT INTO `sys_holiday` VALUES (14, 2025, '2025-04-06', '清明节', 1, 1, 'admin', '2025-12-04 14:58:56', NULL, NULL, NULL);
INSERT INTO `sys_holiday` VALUES (15, 2025, '2025-04-27', '劳动节前补班', 0, 1, 'admin', '2025-12-04 14:58:56', NULL, NULL, NULL);
INSERT INTO `sys_holiday` VALUES (16, 2025, '2025-05-01', '劳动节', 1, 1, 'admin', '2025-12-04 14:58:56', NULL, NULL, NULL);
INSERT INTO `sys_holiday` VALUES (17, 2025, '2025-05-02', '劳动节', 1, 1, 'admin', '2025-12-04 14:58:56', NULL, NULL, NULL);
INSERT INTO `sys_holiday` VALUES (18, 2025, '2025-05-03', '劳动节', 1, 1, 'admin', '2025-12-04 14:58:56', NULL, NULL, NULL);
INSERT INTO `sys_holiday` VALUES (19, 2025, '2025-05-04', '劳动节', 1, 1, 'admin', '2025-12-04 14:58:56', NULL, NULL, NULL);
INSERT INTO `sys_holiday` VALUES (20, 2025, '2025-05-05', '劳动节', 1, 1, 'admin', '2025-12-04 14:58:56', NULL, NULL, NULL);
INSERT INTO `sys_holiday` VALUES (21, 2025, '2025-05-31', '端午节', 1, 1, 'admin', '2025-12-04 14:58:56', NULL, NULL, NULL);
INSERT INTO `sys_holiday` VALUES (22, 2025, '2025-06-01', '端午节', 1, 1, 'admin', '2025-12-04 14:58:56', NULL, NULL, NULL);
INSERT INTO `sys_holiday` VALUES (23, 2025, '2025-06-02', '端午节', 1, 1, 'admin', '2025-12-04 14:58:56', NULL, NULL, NULL);
INSERT INTO `sys_holiday` VALUES (24, 2025, '2025-09-28', '国庆节前补班', 0, 1, 'admin', '2025-12-04 14:58:56', NULL, NULL, NULL);
INSERT INTO `sys_holiday` VALUES (25, 2025, '2025-10-01', '国庆节', 1, 1, 'admin', '2025-12-04 14:58:56', NULL, NULL, NULL);
INSERT INTO `sys_holiday` VALUES (26, 2025, '2025-10-02', '国庆节', 1, 1, 'admin', '2025-12-04 14:58:56', NULL, NULL, NULL);
INSERT INTO `sys_holiday` VALUES (27, 2025, '2025-10-03', '国庆节', 1, 1, 'admin', '2025-12-04 14:58:56', NULL, NULL, NULL);
INSERT INTO `sys_holiday` VALUES (28, 2025, '2025-10-04', '国庆节', 1, 1, 'admin', '2025-12-04 14:58:56', NULL, NULL, NULL);
INSERT INTO `sys_holiday` VALUES (29, 2025, '2025-10-05', '国庆节', 1, 1, 'admin', '2025-12-04 14:58:56', NULL, NULL, NULL);
INSERT INTO `sys_holiday` VALUES (30, 2025, '2025-10-06', '中秋节', 1, 1, 'admin', '2025-12-04 14:58:56', NULL, NULL, NULL);
INSERT INTO `sys_holiday` VALUES (31, 2025, '2025-10-07', '国庆节', 1, 1, 'admin', '2025-12-04 14:58:56', NULL, NULL, NULL);
INSERT INTO `sys_holiday` VALUES (32, 2025, '2025-10-08', '国庆节', 1, 1, 'admin', '2025-12-04 14:58:56', NULL, NULL, NULL);
INSERT INTO `sys_holiday` VALUES (33, 2025, '2025-10-11', '国庆节后补班', 0, 1, 'admin', '2025-12-04 14:58:56', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_limit_configuration
-- ----------------------------
DROP TABLE IF EXISTS `sys_limit_configuration`;
CREATE TABLE `sys_limit_configuration`  (
  `sys_limit_configuration_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `sys_limit_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '接口路径',
  `sys_limit_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '限流规则名称',
  `sys_limit_for_period` int NULL DEFAULT NULL COMMENT '每个周期的许可数量',
  `sys_limit_refresh_period_seconds` bigint NULL DEFAULT NULL COMMENT '限制刷新周期（秒）',
  `sys_limit_timeout_duration_millis` bigint NULL DEFAULT NULL COMMENT '获取许可的超时时间（毫秒）',
  `sys_limit_dimension` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '限流维度（GLOBAL, IP, USER, API）',
  `sys_limit_key_expression` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '自定义键表达式（SpEL）',
  `sys_limit_fallback_method` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '降级方法名称',
  `sys_limit_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误消息',
  `sys_limit_status` int NULL DEFAULT NULL COMMENT '是否启用; 0: 禁用, 1: 启用',
  `sys_limit_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述信息',
  `sys_limit_sort` int NULL DEFAULT NULL COMMENT '排序',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_limit_configuration_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '限流配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_limit_configuration
-- ----------------------------

-- ----------------------------
-- Table structure for sys_limit_record
-- ----------------------------
DROP TABLE IF EXISTS `sys_limit_record`;
CREATE TABLE `sys_limit_record`  (
  `sys_limit_record_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `sys_limit_configuration_id` bigint NULL DEFAULT NULL COMMENT '限流配置ID',
  `sys_limit_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '接口路径',
  `sys_limit_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '限流规则名称',
  `sys_limit_dimension` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '限流维度（GLOBAL, IP, USER, API）',
  `sys_limit_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '限流键值（如IP地址、用户ID等）',
  `sys_limit_time` datetime NULL DEFAULT NULL COMMENT '限流时间',
  `sys_user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `sys_user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名',
  `client_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户端IP',
  `request_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求URL',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求方法',
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户代理',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_limit_record_id`) USING BTREE,
  INDEX `idx_sys_limit_configuration_id`(`sys_limit_configuration_id` ASC) USING BTREE,
  INDEX `idx_sys_limit_time`(`sys_limit_time` ASC) USING BTREE,
  INDEX `idx_sys_user_id`(`sys_user_id` ASC) USING BTREE,
  INDEX `idx_client_ip`(`client_ip` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '限流记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_limit_record
-- ----------------------------

-- ----------------------------
-- Table structure for sys_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log`  (
  `sys_log_id` int NOT NULL AUTO_INCREMENT,
  `sys_log_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账号',
  `sys_log_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求IP',
  `sys_log_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求地址',
  `sys_log_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求参数',
  `sys_log_from` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求模块',
  `sys_log_status` int NULL DEFAULT NULL COMMENT '请求状态; 0: 失败; 1: 成功',
  `sys_log_cost` int NULL DEFAULT NULL COMMENT '耗时',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_log_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_log
-- ----------------------------

-- ----------------------------
-- Table structure for sys_message
-- ----------------------------
DROP TABLE IF EXISTS `sys_message`;
CREATE TABLE `sys_message`  (
  `sys_message_id` int NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `sys_message_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息标题',
  `sys_message_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '消息内容',
  `sys_message_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'system' COMMENT '消息类型: system-系统通知, notice-公告, message-私信, warning-警告, info-信息',
  `sys_message_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'normal' COMMENT '消息级别: low-低, normal-普通, high-高, urgent-紧急',
  `sys_message_sender_id` int NULL DEFAULT 0 COMMENT '发送者ID，0表示系统发送',
  `sys_message_sender_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发送者名称',
  `sys_message_receiver_id` int NULL DEFAULT 0 COMMENT '接收者ID，0表示广播消息',
  `sys_message_receiver_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '接收者名称',
  `sys_message_read` tinyint NULL DEFAULT 0 COMMENT '是否已读: 0-未读, 1-已读',
  `sys_message_read_time` datetime NULL DEFAULT NULL COMMENT '阅读时间',
  `sys_message_send_time` datetime NULL DEFAULT NULL COMMENT '发送时间',
  `sys_message_expire_time` datetime NULL DEFAULT NULL COMMENT '过期时间，为空表示永不过期',
  `sys_message_biz_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '业务类型',
  `sys_message_biz_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '业务ID',
  `sys_message_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '跳转链接',
  `sys_message_extra` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '扩展数据（JSON格式）',
  `sys_message_status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-草稿, 1-已发送, 2-已撤回',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`sys_message_id`) USING BTREE,
  INDEX `idx_receiver_id`(`sys_message_receiver_id` ASC) USING BTREE COMMENT '接收者索引',
  INDEX `idx_sender_id`(`sys_message_sender_id` ASC) USING BTREE COMMENT '发送者索引',
  INDEX `idx_type`(`sys_message_type` ASC) USING BTREE COMMENT '类型索引',
  INDEX `idx_status`(`sys_message_status` ASC) USING BTREE COMMENT '状态索引',
  INDEX `idx_read`(`sys_message_read` ASC) USING BTREE COMMENT '已读状态索引',
  INDEX `idx_send_time`(`sys_message_send_time` ASC) USING BTREE COMMENT '发送时间索引',
  INDEX `idx_expire_time`(`sys_message_expire_time` ASC) USING BTREE COMMENT '过期时间索引',
  INDEX `idx_biz`(`sys_message_biz_type` ASC, `sys_message_biz_id` ASC) USING BTREE COMMENT '业务关联索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统消息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_message
-- ----------------------------

-- ----------------------------
-- Table structure for sys_message_history
-- ----------------------------
DROP TABLE IF EXISTS `sys_message_history`;
CREATE TABLE `sys_message_history`  (
  `sys_message_history_id` int NOT NULL AUTO_INCREMENT COMMENT '历史记录ID',
  `sys_message_history_message_id` int NULL DEFAULT NULL COMMENT '原消息ID',
  `sys_message_history_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '消息标题',
  `sys_message_history_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '消息内容',
  `sys_message_history_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '消息类型',
  `sys_message_history_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '消息级别',
  `sys_message_history_sender_id` int NULL DEFAULT NULL COMMENT '发送者ID',
  `sys_message_history_sender_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发送者名称',
  `sys_message_history_receiver_id` int NULL DEFAULT NULL COMMENT '接收者ID',
  `sys_message_history_receiver_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '接收者名称',
  `sys_message_history_send_time` datetime NULL DEFAULT NULL COMMENT '发送时间',
  `sys_message_history_read_time` datetime NULL DEFAULT NULL COMMENT '阅读时间',
  `sys_message_history_biz_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '业务类型',
  `sys_message_history_biz_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '业务ID',
  `sys_message_history_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '跳转链接',
  `sys_message_history_extra` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '扩展数据',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`sys_message_history_id`) USING BTREE,
  INDEX `idx_receiver_id`(`sys_message_history_receiver_id` ASC) USING BTREE COMMENT '接收者索引',
  INDEX `idx_read_time`(`sys_message_history_read_time` ASC) USING BTREE COMMENT '阅读时间索引',
  INDEX `idx_message_id`(`sys_message_history_message_id` ASC) USING BTREE COMMENT '原消息ID索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统消息历史记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_message_history
-- ----------------------------

-- ----------------------------
-- Table structure for sys_project
-- ----------------------------
DROP TABLE IF EXISTS `sys_project`;
CREATE TABLE `sys_project`  (
  `sys_project_id` int NOT NULL AUTO_INCREMENT,
  `sys_project_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '项目名称',
  `sys_project_icon` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '图标',
  `sys_project_group` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组',
  `sys_project_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '编码, 可自行作为唯一标识',
  `sys_project_app_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'appid',
  `sys_project_app_secret` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'appsecret',
  `sys_project_app_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'appkey',
  `sys_project_sign` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '签名',
  `sys_project_delete` int NULL DEFAULT NULL COMMENT '是否删除0:未删除',
  `sys_project_status` int NULL DEFAULT NULL COMMENT '是否启用0:未启用',
  `sys_project_sign_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '项目标识编码',
  `sys_project_sort` int NULL DEFAULT NULL COMMENT '优先级',
  `sys_project_cdn` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'cdn',
  `sys_project_endpoint` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '端点',
  `sys_project_local_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '本地路径',
  `sys_project_api_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'api地址',
  `sys_project_vender` int NULL DEFAULT NULL COMMENT '厂家(字典1)',
  `sys_project_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `sys_project_function` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '功能; 字典表6',
  `sys_project_smtp_host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'smtp主机',
  `sys_project_smtp_port` int NULL DEFAULT NULL COMMENT 'smtp端口',
  `sys_project_smtp_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'smtp密码',
  `sys_project_smtp_from` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'smtp主题账号',
  `sys_project_location_temp_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '临时文件路径',
  `sys_project_location_oss_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'oss地址',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_project_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '项目管理' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_project
-- ----------------------------
INSERT INTO `sys_project` VALUES (1, '阿里', 'https://img1.baidu.com/it/u=2383102008,2259360939&fm=253&fmt=auto?w=800&h=800', '阿里系', 'ALI', 'LTAI5tKQQnvCS3yEjGxjHmCd', 'Pm45AutVaPLqWCOUNeZ7gEQBTYJQAh', '', '犀鸟校园', 0, 1, NULL, 2, NULL, NULL, NULL, NULL, 2, NULL, '13,15', NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2024-09-06 13:32:21', '2024-09-06 13:36:12', 'sa', 1);
INSERT INTO `sys_project` VALUES (2, '内部系统', NULL, NULL, '01jjk500nsgh4tgmrm9e0t7bmh', NULL, NULL, NULL, NULL, 0, 1, NULL, 3, NULL, NULL, NULL, NULL, 18, NULL, '16', 'smtp.163.com', 25, 'CVIRRMZQPYLQIQMW', 'yemengzhidiandi@163.com', NULL, NULL, 'sa', 1, '2025-01-27 13:50:49', '2025-01-27 13:56:48', 'sa', 1);
INSERT INTO `sys_project` VALUES (4, '海康安防', 'https://open.hikvision.com/fileserver/opsimgs/综合安防.png', NULL, '01jk9zrhcnzsxrm71ybxh85abj', '23031179', 'yKeMbw5czqVuwRnxYHlJ', NULL, '', 0, 1, NULL, 3, NULL, '112.124.44.21:443', NULL, NULL, 19, NULL, '17', NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-05 10:41:53', '2025-02-05 10:44:07', 'sa', 1);
INSERT INTO `sys_project` VALUES (5, '海康云曜', 'https://static.hikyun.com/portal/vportalimages/brand-logo.png', NULL, '01jkczk8ns06derb1m0t6aptb5', '16643603898674415', 'VQYaHTWVmatXVdCbrKZJ', NULL, '651515588253424', 0, 1, '1664360485020781', 3, NULL, NULL, NULL, NULL, 1, NULL, '17', NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-06 14:36:44', '2025-02-06 14:37:46', 'sa', 1);
INSERT INTO `sys_project` VALUES (6, '讯飞', 'https://www.xfyun.cn/static/img/layout/header/logo.svg', NULL, '01jkfjbnn8tvqj03938qkvb2ga', '7d9dec86', 'NTZiODE3MjI3ZGZhMmRjM2FjYTk5ZWJk', '4f810aad8b96224fc2aae62d8d440123', '', 0, 1, NULL, 3, NULL, 'http://spark-api.xf-yun.com/v1.1/chat', NULL, NULL, 10, NULL, '20', NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-07 14:43:07', '2025-02-07 14:47:17', 'sa', 1);
INSERT INTO `sys_project` VALUES (7, 'deepseek', 'https://gimg3.baidu.com/search/src=http%3A%2F%2Fgips0.baidu.com%2Fit%2Fu%3D501085362%2C3268784142%26fm%3D3030%26app%3D3030%26f%3DJPEG%3Fw%3D338%26h%3D225%26s%3D46F088725076FF8208BF9FDD020080AC&refer=http%3A%2F%2Fwww.baidu.com&app=2021&size=f242,150&n=0&g=0n&q=100&fmt=auto?sec=1739120400&t=07d69eb6c8aa160d9c89849bb067310c', NULL, '01jkj3yxzmzppaj8c3afphentc', NULL, NULL, 'EWSNmypiPVPCzRVlcIdKsyNLGJNuw8CTSR+8IcKrheTl1rqvC2QFB8YUaJ8MaP7v', NULL, 0, 1, NULL, 3, NULL, 'https://api.deepseek.com/chat/completions', NULL, NULL, 21, NULL, '20', NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-08 14:29:12', '2025-02-08 15:02:56', 'sa', 1);
INSERT INTO `sys_project` VALUES (8, 'kimi', 'https://platform.moonshot.cn/logo.png', NULL, '01jkjb2jc4grwqtv0vn9tjjayw', NULL, NULL, 'sk-2KiwJ1D0DLOpdb6cozbY5R0H8ydum7YYcDIONGetw4bu6jsy', NULL, 0, 1, NULL, 3, NULL, 'https://api.moonshot.cn/v1/chat/completions', NULL, NULL, 22, NULL, '20', NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-08 16:33:32', '2025-02-08 16:33:43', 'sa', 1);
INSERT INTO `sys_project` VALUES (9, 'ollama', 'https://ollama.com/public/ollama.png', NULL, '01jkpta36f0yds272z1g81qs5r', NULL, NULL, NULL, NULL, 0, 1, NULL, 3, NULL, 'http://127.0.0.1:11434/v1/chat/completions', NULL, NULL, 23, NULL, '20', NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-10 10:16:45', '2025-02-10 10:40:30', 'sa', 1);
INSERT INTO `sys_project` VALUES (10, '测试', NULL, NULL, '01jkszw2aj1ht2pyr971fv2bz0', '28282971', 'q4S7VR7AXTcSpLuKsMlF', '', NULL, 0, 1, NULL, 3, NULL, '39.170.78.27:1443', NULL, NULL, 19, NULL, '17', NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-11 15:51:40', '2025-02-11 15:52:37', 'sa', 1);
INSERT INTO `sys_project` VALUES (11, 'Cloudflare', 'https://developers.cloudflare.com/_astro/logo.p_ySeMR1.svg', NULL, '01jm8rp81pxz21ys3a7jtjw17w', NULL, NULL, 'ac4ctLVeplrjya5eT5ax024mgnT8hz1U-G-JF5ZR', NULL, 0, 1, NULL, 3, NULL, 'https://api.cloudflare.com/client/v4/accounts/f0a671943ec8c499a69ae2b1857f66ae/ai/v1', NULL, NULL, 32, NULL, '20,27', NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-17 09:34:45', '2025-02-17 09:36:25', 'sa', 1);
INSERT INTO `sys_project` VALUES (12, '豆包', 'https://lf-flow-web-cdn.doubao.com/obj/flow-doubao/samantha/logo-icon-white-bg.png', NULL, '01jmh1ctzzqxk5pgxc5mv50h81', NULL, NULL, 'd3eec490-7aaf-468c-9e08-71021807460e1', NULL, 0, 1, NULL, 3, NULL, NULL, NULL, NULL, 26, NULL, '20', NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-20 14:40:50', '2025-02-20 14:41:05', 'sa', 1);
INSERT INTO `sys_project` VALUES (13, '阿里百炼', 'https://gw.alicdn.com/imgextra/i1/O1CN01gAwVtW296IdlNcfkW_!!6000000008018-2-tps-264-104.png', NULL, '01jn01sjfvgm9a9v2qjbm5ywtn', NULL, NULL, 'sk-8e1cd4c8176b494b97c361b5a1d78ea3', NULL, 0, 1, NULL, 3, NULL, NULL, NULL, NULL, 2, NULL, '27', NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-26 10:36:23', '2025-02-26 10:39:46', 'sa', 1);
INSERT INTO `sys_project` VALUES (14, '硅基流动', 'https://framerusercontent.com/images/4li2PjWxZJmoGkzXRMJWU1rJmI.svg', NULL, '01jn3803z7e41x7fjzvj539j20', NULL, NULL, 'sk-fqfxjiyhpadgvdlkousbnztmuttiegwmgaymubnaxofwoxhx', NULL, 0, 1, NULL, 3, NULL, 'https://api.siliconflow.cn/v1/images/generations', NULL, NULL, 28, NULL, '27,20', NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-27 16:22:33', '2025-02-27 16:23:53', 'sa', 1);
INSERT INTO `sys_project` VALUES (16, 'openrouter', ' https://openrouter.ai/favicon.ico', NULL, '01jn5j39e1h4yqnw4qs801jzqn', NULL, NULL, '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', NULL, 0, 1, NULL, 3, NULL, 'https://openrouter.ai/api/v1/chat/completions', NULL, NULL, 29, NULL, '20', NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-28 13:57:31', '2025-03-25 14:11:37', 'sa', 1);
INSERT INTO `sys_project` VALUES (17, '智谱清言', 'https://bigmodel.cn/img/icons/android-chrome-192x192.png', NULL, '01jn5qbe49btdqzc2fe17rjysf', NULL, NULL, '29ed238f4660416cb2efe39aaaf71ed5.S2I2AfOu75HQwZ30', NULL, 0, 1, NULL, 3, NULL, NULL, NULL, NULL, 30, NULL, '27,20,33', NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-28 15:29:21', '2025-02-28 15:29:36', 'sa', 1);
INSERT INTO `sys_project` VALUES (18, '无问芯穹', 'https://content.cloud.infini-ai.com/platform-web-prod/assets/svg/logo_new.ceae5ff6.svg', NULL, '01jn8yb4ak2amap9qsc3amj4js', NULL, NULL, 'sk-daulbrd77wpadaw6', NULL, 0, 1, NULL, 3, NULL, NULL, NULL, NULL, 31, NULL, '20', NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-03-01 21:29:14', NULL, NULL, NULL);
INSERT INTO `sys_project` VALUES (19, '腾讯', 'https://pic.rmb.bdstatic.com/bjh/news/87715fa6e1758590fc4b076538d704ad.png', NULL, '01jnqrnw0z28t5bhkf6a2qkc2d', 'AKIDOgokSxEI8pIYaP1RedPxKTSEn8GAtPp1', '1rUt9E7mETWMyQhlbEetwpV0KXXKtEMJ', 'sk-RBopn00LV7WB5KMQHdDAaV256zyjjQGuVA9DftZrUWTFsM0w', NULL, 0, 1, NULL, 1, NULL, NULL, NULL, NULL, 34, NULL, '20,27', NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-03-07 15:38:51', '2025-03-07 15:42:17', 'sa', 1);
INSERT INTO `sys_project` VALUES (20, '海康云眸', ' https://pic.hik-cloud.com/favicon.ico', NULL, '01jpgs1jm6tbvdp6tq9zfw59ss', 'e431e02c369b4adbad8f72c1eafbe8f7', '492c2864b59d459ebdb64795119e062b', NULL, NULL, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, 35, NULL, '17', NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-03-17 08:46:16', '2025-03-17 08:51:09', 'sa', 1);
INSERT INTO `sys_project` VALUES (21, '佐糖', 'https://qncdn.aoscdn.com/astro/picwish/_astro/logo-zh.Bv2rokLa.svg', NULL, '01jps1ab36ykbgd6mkygprmcmd', NULL, NULL, NULL, NULL, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, 38, NULL, '39,40', NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-03-20 13:44:47', '2025-03-21 10:30:34', 'sa', 1);

-- ----------------------------
-- Table structure for sys_project_default
-- ----------------------------
DROP TABLE IF EXISTS `sys_project_default`;
CREATE TABLE `sys_project_default`  (
  `sys_project_default_id` int NOT NULL AUTO_INCREMENT,
  `sys_project_default_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '类型； 字典表6;',
  `sys_project_id` int NULL DEFAULT NULL COMMENT '项目ID',
  PRIMARY KEY (`sys_project_default_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 48 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '默认功能 <---->对象的项目; e.g. 短信默认采用那个项目配置' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_project_default
-- ----------------------------
INSERT INTO `sys_project_default` VALUES (30, '6', 3);
INSERT INTO `sys_project_default` VALUES (33, '16', 2);
INSERT INTO `sys_project_default` VALUES (37, '14', 4);
INSERT INTO `sys_project_default` VALUES (46, '13', 1);
INSERT INTO `sys_project_default` VALUES (47, '20', 16);

-- ----------------------------
-- Table structure for sys_secret
-- ----------------------------
DROP TABLE IF EXISTS `sys_secret`;
CREATE TABLE `sys_secret`  (
  `sys_secret_id` int NOT NULL AUTO_INCREMENT,
  `sys_secret_group` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组',
  `sys_secret_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '编码, 可自行作为唯一标识',
  `sys_secret_app_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'appid',
  `sys_secret_app_secret` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'appsecret',
  `sys_secret_sign` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '签名',
  `sys_secret_delete` int NULL DEFAULT NULL COMMENT '是否删除0:未删除',
  `sys_secret_status` int NULL DEFAULT NULL COMMENT '是否启用0:未启用',
  `sys_secret_cdn` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'cdn',
  `sys_secret_endpoint` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '端点',
  `sys_secret_api_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'api地址',
  `sys_secret_dict_item_id` int NULL DEFAULT NULL COMMENT '字典项ID(字典1)',
  `sys_secret_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `sys_secret_function` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '功能',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_secret_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '密钥' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_secret
-- ----------------------------
INSERT INTO `sys_secret` VALUES (1, '阿里系', 'ALI', 'LTAI5tKQQnvCS3yEjGxjHmCd', 'Pm45AutVaPLqWCOUNeZ7gEQBTYJQAh', '犀鸟校园', 0, 1, NULL, NULL, NULL, 2, NULL, 'SMS', 'sa', 1, '2024-09-06 13:32:21', '2024-09-06 13:36:12', 'sa', 1);

-- ----------------------------
-- Table structure for sys_setting
-- ----------------------------
DROP TABLE IF EXISTS `sys_setting`;
CREATE TABLE `sys_setting`  (
  `sys_setting_id` int NOT NULL AUTO_INCREMENT,
  `sys_setting_sort` int NULL DEFAULT NULL COMMENT '优先级',
  `sys_setting_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置名称',
  `sys_setting_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置值',
  `sys_setting_status` int NULL DEFAULT NULL COMMENT '是否启用; 0:不启用',
  `sys_setting_value_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置类型; 用于前端组件判断; e.g.BOOL：布尔',
  `sys_setting_group` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置所属分组',
  `sys_setting_collect` int NULL DEFAULT NULL COMMENT '是否是多个配置集合;0:单个',
  `sys_setting_in_system` int NULL DEFAULT NULL COMMENT '是否系统配置; 0:非系统配置',
  `sys_setting_app_inner` int NULL DEFAULT NULL COMMENT '配置是否作用域系统启动',
  `sys_setting_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置备注',
  `sys_setting_config` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置参数',
  `sys_setting_admin` int NULL DEFAULT NULL COMMENT '是否只有管理员生效',
  `sys_setting_version` int NULL DEFAULT NULL COMMENT '版本',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_setting_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 43 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统全局配置' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_setting
-- ----------------------------
INSERT INTO `sys_setting` VALUES (1, 4, 'CheckCodeOpen', 'false', 1, 'Boolean', 'default', 0, 1, 0, '验证码开关', NULL, NULL, 15, NULL, NULL, NULL, '2025-02-19 14:19:05', 'sa', 1);
INSERT INTO `sys_setting` VALUES (2, 0, 'SystemName', '后台管理系统', 1, 'String', 'default', 0, 1, 0, '系统标题', NULL, NULL, 15, NULL, NULL, NULL, '2025-02-19 14:19:05', 'sa', 1);
INSERT INTO `sys_setting` VALUES (3, 5, 'SlidingBlockOpen', 'false', 1, 'Boolean', 'default', 0, 1, 0, '滑动验证码', NULL, NULL, 15, NULL, NULL, NULL, '2025-02-19 14:19:05', 'sa', 1);
INSERT INTO `sys_setting` VALUES (4, 9, 'CrashPageOpen', 'false', 1, 'Boolean', 'config', 0, 1, 0, '调试模式崩溃当前页', NULL, NULL, 26, 'sa', NULL, '2024-08-18 11:22:26', '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting` VALUES (5, 10, 'CodecResponseOpen', 'false', 1, 'Boolean', 'config', 0, 1, 0, '返回加密', NULL, NULL, 26, 'sa', NULL, '2024-08-18 11:31:57', '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting` VALUES (6, 5, 'WatermarkOpen', 'false', 1, 'Boolean', 'config', 0, 1, 0, '水印', NULL, NULL, 26, 'sa', NULL, '2024-08-19 19:53:55', '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting` VALUES (7, 11, 'LoopDebuggerOpen', 'false', 1, 'Boolean', 'config', 0, 1, 0, '循环debugger', NULL, NULL, 26, 'sa', NULL, '2024-08-18 10:52:15', '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting` VALUES (8, 4, 'Version', '1', 1, 'Number', 'config', 0, 1, 0, '系统版本', 'fe7db08a-1001-4e7b-95ab-776a569a85c0', NULL, 123, NULL, NULL, NULL, '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting` VALUES (9, 2, 'SocketUrl', 'http://172.16.2.226:29181', 1, 'Array', 'config', 0, 1, 0, 'socket.io服务器地址', NULL, 1, 26, NULL, NULL, NULL, '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting` VALUES (10, 12, 'SocketOpen', 'false', 1, 'Boolean', 'config', 0, 1, 0, 'socket.io是否开启', NULL, 1, 26, NULL, NULL, NULL, '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting` VALUES (11, 13, 'SocketPath', '/socket.io', 1, 'String', 'config', 0, 1, 0, 'socket.io路径', NULL, NULL, NULL, NULL, NULL, NULL, '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting` VALUES (12, 6, 'WatermarkColor', '#409EFF', 1, 'Color', 'config', 0, 1, 0, '水印颜色', NULL, NULL, 26, NULL, NULL, NULL, '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting` VALUES (13, 8, 'WatermarkAlpha', '0.35', 1, 'Number', 'config', 0, 1, 0, '水印透明度', NULL, NULL, 26, NULL, NULL, NULL, '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting` VALUES (14, 7, 'WatermarkRotate', '-30', 1, 'Number', 'config', 0, 1, 0, '水印角度', NULL, NULL, 26, NULL, NULL, NULL, '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting` VALUES (15, 14, 'CodecRequestOpen', 'false', 1, 'Boolean', 'config', 0, 1, 0, '请求加密', NULL, NULL, 26, NULL, NULL, NULL, '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting` VALUES (16, 3, 'CodecRequestKey', '0123456789abcdef', 1, 'String', 'config', 0, 1, 1, '请求加密密钥', NULL, NULL, 26, NULL, NULL, NULL, '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting` VALUES (17, 2, 'Host', 'smtp.163.com', 1, 'String', 'smtp', 1, 1, 0, '服务器配置', NULL, NULL, NULL, NULL, NULL, NULL, '2024-11-27 13:12:49', 'sa', 1);
INSERT INTO `sys_setting` VALUES (18, 3, 'Port', '25', 1, 'Number', 'smtp', 1, 1, 0, '服务器端口', NULL, NULL, NULL, NULL, NULL, NULL, '2024-11-27 13:12:49', 'sa', 1);
INSERT INTO `sys_setting` VALUES (19, 4, 'Password', 'CVIRRMZQPYLQIQMW_C', 1, 'Password', 'smtp', 1, 1, 0, '授权码', NULL, NULL, NULL, NULL, NULL, NULL, '2024-11-27 13:12:49', 'sa', 1);
INSERT INTO `sys_setting` VALUES (20, 1, 'From', 'yemengzhidiandi@163.com', 1, 'Mail', 'smtp', 1, 1, 0, '发送账号', NULL, NULL, NULL, NULL, NULL, NULL, '2024-11-27 13:12:49', 'sa', 1);
INSERT INTO `sys_setting` VALUES (21, 1, 'AppId', 'wx8cd1bb691119e3f7', 1, 'String', 'weixin', 1, 1, 0, '微信AppId', NULL, NULL, NULL, NULL, NULL, NULL, '2024-09-05 16:32:37', 'sa', 1);
INSERT INTO `sys_setting` VALUES (22, 2, 'AppSecret', 'abff9e5a7541a5db21a819b087ecbf10', 1, 'String', 'weixin', 1, 1, 0, '微信AppSecret', NULL, NULL, NULL, NULL, NULL, NULL, '2024-09-05 16:32:37', 'sa', 1);
INSERT INTO `sys_setting` VALUES (23, 1, 'AppId', 'f0d999a2e91e9a4edcec329fe7e6b3f1fa8b7063f8a618c3c7a2e0d0a13d5943', 1, 'String', 'gitee', 1, 1, 0, 'GiteeAppId', NULL, NULL, NULL, NULL, NULL, NULL, '2024-09-05 16:49:41', 'sa', 1);
INSERT INTO `sys_setting` VALUES (24, 2, 'AppSecret', '158ced91fe30cfa06798c638d2b477d54e57b623a7e869c42d34d3125e88f1b7', 1, 'String', 'gitee', 1, 1, 0, 'GiteeAppSecret', NULL, NULL, NULL, NULL, NULL, NULL, '2024-09-05 16:49:41', 'sa', 1);
INSERT INTO `sys_setting` VALUES (25, 3, 'Gitee', 'true', 1, 'Boolean', 'sso', 1, 1, 0, 'Gitee登录', NULL, NULL, NULL, NULL, NULL, NULL, '2025-02-18 16:11:53', 'sa', 1);
INSERT INTO `sys_setting` VALUES (26, 1, 'AppId', 'LTAI5tKQQnvCS3yEjGxjHmCd', 1, 'String', 'sms', 1, 1, 0, '短信AppId', NULL, NULL, NULL, NULL, NULL, NULL, '2024-09-10 10:25:22', 'sa', 1);
INSERT INTO `sys_setting` VALUES (27, 2, 'AppSecret', 'Pm45AutVaPLqWCOUNeZ7gEQBTYJQAhh', 1, 'String', 'sms', 1, 1, 0, '短信AppSecret', NULL, NULL, NULL, NULL, NULL, NULL, '2024-09-10 10:25:22', 'sa', 1);
INSERT INTO `sys_setting` VALUES (28, 3, 'AppSign', '犀鸟校园', 1, 'String', 'sms', 1, 1, 0, '短信签名', NULL, NULL, NULL, NULL, NULL, NULL, '2024-09-10 10:25:22', 'sa', 1);
INSERT INTO `sys_setting` VALUES (29, 4, 'AppType', 'ALIYUN', 1, 'Dict', 'sms', 1, 1, 0, '短信厂商', '1', NULL, NULL, NULL, NULL, NULL, '2024-09-10 10:25:22', 'sa', 1);
INSERT INTO `sys_setting` VALUES (30, 1, 'AppKey', 'YfuzeuBcnvSluLjHkviR:WFjoVbqyzcqfttbxnsZu', 1, 'AppKey', 'llm', 1, 1, 0, '密钥', NULL, NULL, NULL, NULL, NULL, NULL, '2024-10-24 15:07:36', 'sa', 1);
INSERT INTO `sys_setting` VALUES (31, 2, 'AppType', 'XUN_FEI', 1, 'Dict', 'llm', 1, 1, 0, '语言模型厂商', '1', NULL, NULL, NULL, NULL, NULL, '2024-10-24 15:07:36', 'sa', 1);
INSERT INTO `sys_setting` VALUES (32, 3, 'AppId', '7d9dec86', 1, 'String', 'llm', 1, 1, 0, '模型AppId', NULL, NULL, NULL, NULL, NULL, NULL, '2024-10-24 15:07:36', 'sa', 1);
INSERT INTO `sys_setting` VALUES (33, 4, 'AppSecret', 'NTZiODE3MjI3ZGZhMmRjM2FjYTk5ZWJk', 1, 'AppSecret', 'llm', 1, 1, 0, '模型AppSecret', NULL, NULL, NULL, NULL, NULL, NULL, '2024-10-24 15:07:36', 'sa', 1);
INSERT INTO `sys_setting` VALUES (34, 6, 'CheckTotpCodeOpen', 'false', 1, 'Boolean', 'default', NULL, 0, NULL, '开启totp', NULL, NULL, NULL, 'sa', 1, '2024-12-08 11:56:39', '2025-02-19 14:19:05', 'sa', 1);
INSERT INTO `sys_setting` VALUES (35, 1, 'WebrtcOpen', 'true', 1, 'Boolean', 'webrtc', NULL, NULL, NULL, '是否开启', NULL, NULL, NULL, NULL, NULL, NULL, '2024-12-11 10:26:15', 'sa', 1);
INSERT INTO `sys_setting` VALUES (36, 2, 'WebrtcUrl', 'http://172.16.2.226:29182', 1, 'String', 'webrtc', NULL, NULL, NULL, 'webrtc服务器地址', NULL, NULL, NULL, NULL, NULL, NULL, '2024-12-11 10:26:15', 'sa', 1);
INSERT INTO `sys_setting` VALUES (37, 3, 'WebrtcContext', '/webrtc', 1, 'String', 'webrtc', NULL, NULL, NULL, '路径', NULL, NULL, NULL, NULL, NULL, NULL, '2024-12-11 10:26:15', 'sa', 1);
INSERT INTO `sys_setting` VALUES (38, 1, 'OssAddress', 'http://127.0.0.01:19180', 1, 'String', 'config', NULL, 0, NULL, '资源地址', NULL, NULL, NULL, 'sa', 1, '2025-02-10 16:10:37', '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting` VALUES (39, 0, 'ApiAddress', NULL, 1, 'String', 'config', NULL, 0, NULL, '接口地址', NULL, NULL, NULL, 'sa', 1, '2025-02-17 08:48:56', '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting` VALUES (40, 2, 'OpenTenantLogin', 'true', 1, 'Boolean', 'default', NULL, 0, NULL, '租户登录', NULL, NULL, NULL, 'sa', 1, '2025-02-19 11:14:15', '2025-02-19 14:19:05', 'sa', 1);
INSERT INTO `sys_setting` VALUES (41, 3, 'OpenTenantLoginRequire', 'false', 1, 'Boolean', 'default', NULL, 0, NULL, '填写租户', NULL, NULL, NULL, 'sa', 1, '2025-02-19 14:18:26', '2025-02-19 14:19:05', 'sa', 1);
INSERT INTO `sys_setting` VALUES (42, 1, 'OpenBaseLogin', 'true', 1, 'Boolean', 'default', NULL, 0, NULL, '基础登录', NULL, NULL, NULL, 'sa', 1, '2025-02-19 14:18:52', '2025-02-19 14:19:05', 'sa', 1);

-- ----------------------------
-- Table structure for sys_setting_group
-- ----------------------------
DROP TABLE IF EXISTS `sys_setting_group`;
CREATE TABLE `sys_setting_group`  (
  `sys_setting_group_id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `sys_setting_group_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `sys_setting_group_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '唯一编码',
  `sys_setting_group_icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标',
  `sys_setting_group_enable` tinyint(1) NULL DEFAULT NULL COMMENT '是否启用',
  `sys_setting_group_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `sys_setting_group_sort` int NULL DEFAULT NULL COMMENT '排序',
  `sys_setting_group_use_project_interface` tinyint(1) NULL DEFAULT NULL COMMENT '是否使用项目组接口',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_setting_group_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '配置组' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_setting_group
-- ----------------------------
INSERT INTO `sys_setting_group` VALUES (1, '系统设置', 'config', 'ri:export-line', 1, '系统设置, 用于设置系统水印，加解密等', 1, NULL, 'admin', 1, '2025-09-10 17:23:50', '2025-09-10 19:46:25', 'admin', 1);
INSERT INTO `sys_setting_group` VALUES (2, '微信设置', 'weixin', 'simple-icons:wechat', 1, '', 2, NULL, 'admin', 1, '2025-09-10 17:26:29', '2025-09-10 19:46:25', 'admin', 1);
INSERT INTO `sys_setting_group` VALUES (3, 'Gitee设置', 'gitee', 'simple-icons:gitee', 1, '', 6, NULL, 'admin', 1, '2025-09-10 17:27:01', '2025-09-10 19:46:25', 'admin', 1);
INSERT INTO `sys_setting_group` VALUES (4, '邮件设置', 'email', 'bi:mailbox2', 1, '', 3, NULL, 'admin', 1, '2025-09-10 17:27:32', '2025-09-10 19:46:25', 'admin', 1);
INSERT INTO `sys_setting_group` VALUES (5, '短信设置', 'sms', 'ri:phone-find-line', 1, '', 4, NULL, 'admin', 1, '2025-09-10 17:29:10', '2025-09-10 19:46:25', 'admin', 1);
INSERT INTO `sys_setting_group` VALUES (6, '大语言模型设置', 'llm', 'ri:login-box-fill', 1, '', 5, NULL, 'admin', 1, '2025-09-10 17:29:57', '2025-09-10 19:46:25', 'admin', 1);
INSERT INTO `sys_setting_group` VALUES (7, 'SSO设置', 'sso', 'ri:login-box-fill', 1, '', 7, NULL, 'admin', 1, '2025-09-10 17:30:49', '2025-09-10 19:46:25', 'admin', 1);

-- ----------------------------
-- Table structure for sys_sms_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_sms_log`;
CREATE TABLE `sys_sms_log`  (
  `sys_sms_log_id` int NOT NULL AUTO_INCREMENT,
  `sys_sms_log_target` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '接收人',
  `sys_sms_log_template_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板ID',
  `sys_sms_log_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '参数',
  `sys_sms_log_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '发送的模板内容',
  `sys_sms_cost` decimal(10, 2) NULL DEFAULT NULL COMMENT '耗时时间',
  `sys_sms_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误码',
  `sys_sms_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '错误信息',
  `sys_sms_app_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '短信厂家',
  `sys_sms_app_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '短信密钥',
  `sys_sms_app_sign` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '签名',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  `sys_project_id` int NULL DEFAULT NULL COMMENT '项目ID',
  PRIMARY KEY (`sys_sms_log_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '短信下发日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_sms_log
-- ----------------------------

-- ----------------------------
-- Table structure for sys_sms_template
-- ----------------------------
DROP TABLE IF EXISTS `sys_sms_template`;
CREATE TABLE `sys_sms_template`  (
  `sys_sms_template_id` int NOT NULL AUTO_INCREMENT,
  `sys_project_id` int NULL DEFAULT NULL COMMENT '项目ID',
  `sys_project_type` int NULL DEFAULT NULL COMMENT '类别字典表(dictId=6)',
  `sys_sms_template_sort` int NULL DEFAULT NULL COMMENT '模板优先级',
  `sys_sms_template_category` int NULL DEFAULT NULL COMMENT '子类型; (dictId = 2)',
  `sys_sms_template_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `sys_sms_template_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '唯一编码',
  `sys_sms_template_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '模板内容',
  `sys_sms_template_delete` int NULL DEFAULT NULL COMMENT '是否删除0:未删除',
  `sys_sms_template_status` int NULL DEFAULT NULL COMMENT '是否启用:0:未启用',
  `sys_sms_template_type` int NULL DEFAULT NULL COMMENT '模板类型;0:系统;1:用户;2:其它',
  `sys_sms_template_default` int NULL DEFAULT NULL COMMENT '是否默认:0:未启用',
  `sys_sms_template_approve` int NULL DEFAULT NULL COMMENT '审批状态;0:未审批; 1: 审批中; 2:已通过; 3: 未通过',
  `sys_sms_template_approve_reject` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '拒绝原因',
  `sys_sms_template_approve_create_by` int NULL DEFAULT NULL COMMENT '审批人',
  `sys_sms_template_disabled` int NULL DEFAULT NULL COMMENT '模板编辑状态;0:可编辑; 1: 不可编辑;',
  `sys_sms_template_origin` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源',
  `sys_sms_template_origin_uuid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源uuid',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_sms_template_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 37 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '模板管理' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_sms_template
-- ----------------------------

-- ----------------------------
-- Table structure for system_data_backup_record
-- ----------------------------
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

-- ----------------------------
-- Records of system_data_backup_record
-- ----------------------------

-- ----------------------------
-- Table structure for system_data_email_history
-- ----------------------------
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

-- ----------------------------
-- Records of system_data_email_history
-- ----------------------------

-- ----------------------------
-- Table structure for system_data_field_comment
-- ----------------------------
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

-- ----------------------------
-- Records of system_data_field_comment
-- ----------------------------

-- ----------------------------
-- Table structure for system_data_log_record
-- ----------------------------
DROP TABLE IF EXISTS `system_data_log_record`;
CREATE TABLE `system_data_log_record`  (
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '记录ID',
  `setting_id` int NULL DEFAULT NULL COMMENT '配置ID',
  `level` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志级别',
  `timestamp` bigint NULL DEFAULT NULL COMMENT '时间戳',
  `message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '内容',
  `from` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of system_data_log_record
-- ----------------------------

-- ----------------------------
-- Table structure for system_data_setting
-- ----------------------------
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
  `system_data_setting_backup_status` int NULL DEFAULT NULL COMMENT '备份状态;0:未启动; 1:已启动',
  `system_data_setting_idle` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否支持监听',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`system_data_setting_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of system_data_setting
-- ----------------------------
INSERT INTO `system_data_setting` VALUES (1, '172.16.0.40', 'JDBC', 'jdbc:mysql://172.16.0.40:3306/report?useUnicode=true&characterEncoding=UTF-8', '172.16.0.40', 3306, 'root', '0IG6HL98tdPqAF0TXH82vw==', 'report', NULL, NULL, '', NULL, '', 600000, 'REMOTE', '1', '{\"useUnicode\":\"true\",\"characterEncoding\":\"UTF-8\"}', 'Mysql', 'com.mysql.cj.jdbc.Driver', '', '', NULL, NULL, 'admin', 1, '2025-11-30 22:14:08.000000', '2025-11-30 23:19:11.304181', 'admin', 1);
INSERT INTO `system_data_setting` VALUES (2, 'redis_172.16.0.40', 'REDIS', 'redis://172.16.0.40:16379', '172.16.0.40', 16379, '', '', '', NULL, NULL, NULL, NULL, NULL, 600000, 'REMOTE', '1', '', 'Redis', '', '', NULL, NULL, NULL, 'admin', 1, '2025-12-01 01:51:17.753893', NULL, NULL, NULL);
INSERT INTO `system_data_setting` VALUES (3, 'zookeeper', 'ZOOKEEPER', '112.124.44.21:2181', '112.124.44.21', 2181, '', '', '', NULL, NULL, NULL, NULL, NULL, 600000, 'REMOTE', '1', '', 'Zookeeper', '', '', NULL, NULL, NULL, 'admin', 1, '2025-12-03 22:51:44.667547', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for system_data_setting_item
-- ----------------------------
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

-- ----------------------------
-- Records of system_data_setting_item
-- ----------------------------

-- ----------------------------
-- Table structure for system_server
-- ----------------------------
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

-- ----------------------------
-- Records of system_server
-- ----------------------------
INSERT INTO `system_server` VALUES (1, '文件存储', '0.0.0.0', 8081, 'HTTP', '', 'STOPPED', '', NULL, '0', NULL, NULL, 'admin', 1, '2025-12-04 01:44:50.112539', '2025-12-09 19:43:01.770813', 'admin', 1);
INSERT INTO `system_server` VALUES (2, '测试代理', '0.0.0.0', 38080, 'HTTP-PROXY', '', 'STOPPED', '', NULL, '0', NULL, NULL, 'admin', 1, '2025-12-08 02:15:00.084891', '2025-12-09 22:02:17.878694', 'admin', 1);

-- ----------------------------
-- Table structure for system_server_log
-- ----------------------------
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

-- ----------------------------
-- Records of system_server_log
-- ----------------------------

-- ----------------------------
-- Table structure for system_server_setting
-- ----------------------------
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

-- ----------------------------
-- Records of system_server_setting
-- ----------------------------
INSERT INTO `system_server_setting` VALUES (1, '映射Servlet过滤器', 'MAPPING', 1, 1, '映射Servlet过滤器', '1', NULL, NULL, NULL, 'b7a05b57-653e-4bce-8239-6e4ad1aecf93', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'admin', 1, '2025-12-04 01:51:28.553666', '2025-12-08 23:59:09.424935', 'admin', 1);
INSERT INTO `system_server_setting` VALUES (3, '文件存储', 'FILESTORAGE', 1, 2, '文件存储', '1', NULL, NULL, NULL, '3ce36bef-72b0-4048-b162-8f999af48837', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'admin', 1, '2025-12-08 02:01:27.583550', '2025-12-08 23:59:09.429939', 'admin', 1);
INSERT INTO `system_server_setting` VALUES (6, '服务发现过滤器', 'SERVICEDISCOVERY', 2, 1, '服务发现过滤器', '1', NULL, NULL, NULL, 'dd837c82-4806-4551-aa49-4f600c38ecb0', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'admin', 1, '2025-12-08 02:15:33.280744', '2025-12-09 22:00:35.349715', 'admin', 1);

-- ----------------------------
-- Table structure for system_server_setting_address_rate_limit
-- ----------------------------
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

-- ----------------------------
-- Records of system_server_setting_address_rate_limit
-- ----------------------------

-- ----------------------------
-- Table structure for system_server_setting_file_storage
-- ----------------------------
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

-- ----------------------------
-- Records of system_server_setting_file_storage
-- ----------------------------
INSERT INTO `system_server_setting_file_storage` VALUES (1, 1, 'FILESYSTEM', 'file', 'H:\\', '', '', '', '', '1', NULL, 'admin', 1, '2025-12-04 01:52:32.000000', '2025-12-08 15:39:59.992776', 'admin', 1);

-- ----------------------------
-- Table structure for system_server_setting_ip_rate_limit
-- ----------------------------
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

-- ----------------------------
-- Records of system_server_setting_ip_rate_limit
-- ----------------------------

-- ----------------------------
-- Table structure for system_server_setting_item
-- ----------------------------
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

-- ----------------------------
-- Records of system_server_setting_item
-- ----------------------------
INSERT INTO `system_server_setting_item` VALUES (104, 3, 'openDownload', 'true', NULL, NULL, NULL, NULL, NULL, 1, 'admin', 1, '2025-12-08 15:39:58.856016', NULL, NULL, NULL);
INSERT INTO `system_server_setting_item` VALUES (105, 3, 'openPreview', 'true', NULL, NULL, NULL, NULL, NULL, 1, 'admin', 1, '2025-12-08 15:39:58.922104', NULL, NULL, NULL);
INSERT INTO `system_server_setting_item` VALUES (106, 3, 'openPlugin', 'true', NULL, NULL, NULL, NULL, NULL, 1, 'admin', 1, '2025-12-08 15:39:58.974496', NULL, NULL, NULL);
INSERT INTO `system_server_setting_item` VALUES (107, 3, 'openSetting', 'true', NULL, NULL, NULL, NULL, NULL, 1, 'admin', 1, '2025-12-08 15:39:59.036820', NULL, NULL, NULL);
INSERT INTO `system_server_setting_item` VALUES (108, 3, 'openRange', 'false', NULL, NULL, NULL, NULL, NULL, 1, 'admin', 1, '2025-12-08 15:39:59.097594', NULL, NULL, NULL);
INSERT INTO `system_server_setting_item` VALUES (109, 3, 'openWatermark', 'false', NULL, NULL, NULL, NULL, NULL, 1, 'admin', 1, '2025-12-08 15:39:59.156532', NULL, NULL, NULL);
INSERT INTO `system_server_setting_item` VALUES (110, 3, 'openWebjars', 'true', NULL, NULL, NULL, NULL, NULL, 1, 'admin', 1, '2025-12-08 15:39:59.227593', NULL, NULL, NULL);
INSERT INTO `system_server_setting_item` VALUES (111, 3, 'openRemoteFile', 'false', NULL, NULL, NULL, NULL, NULL, 1, 'admin', 1, '2025-12-08 15:39:59.293633', NULL, NULL, NULL);
INSERT INTO `system_server_setting_item` VALUES (112, 3, 'plugins', '[MINECRAFT]', NULL, NULL, NULL, NULL, NULL, 1, 'admin', 1, '2025-12-08 15:39:59.381837', NULL, NULL, NULL);
INSERT INTO `system_server_setting_item` VALUES (113, 3, 'settings', '[]', NULL, NULL, NULL, NULL, NULL, 1, 'admin', 1, '2025-12-08 15:39:59.438624', NULL, NULL, NULL);
INSERT INTO `system_server_setting_item` VALUES (114, 3, 'formatCacheTimeMinutes', '1440', NULL, NULL, NULL, NULL, NULL, 1, 'admin', 1, '2025-12-08 15:39:59.498266', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for system_server_setting_service_discovery
-- ----------------------------
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

-- ----------------------------
-- Records of system_server_setting_service_discovery
-- ----------------------------
INSERT INTO `system_server_setting_service_discovery` VALUES (1, 2, 'MONITOR', 'weight', 'NACOS', '1', NULL, NULL, 'admin', 1, '2025-12-08 23:29:40.000000', '2025-12-09 21:52:34.791479', 'admin', 1);

-- ----------------------------
-- Table structure for system_server_setting_service_discovery_mapping
-- ----------------------------
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

-- ----------------------------
-- Records of system_server_setting_service_discovery_mapping
-- ----------------------------
INSERT INTO `system_server_setting_service_discovery_mapping` VALUES (1, 2, '/bd/', 'https://www.baidu.com/', 1, '1', 'admin', 1, '2025-12-08 23:29:40.000000', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for system_soft
-- ----------------------------
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

-- ----------------------------
-- Records of system_soft
-- ----------------------------

-- ----------------------------
-- Table structure for system_soft_container
-- ----------------------------
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

-- ----------------------------
-- Records of system_soft_container
-- ----------------------------

-- ----------------------------
-- Table structure for system_soft_container_stats
-- ----------------------------
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

-- ----------------------------
-- Records of system_soft_container_stats
-- ----------------------------

-- ----------------------------
-- Table structure for system_soft_image
-- ----------------------------
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

-- ----------------------------
-- Records of system_soft_image
-- ----------------------------

-- ----------------------------
-- Table structure for system_soft_record
-- ----------------------------
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

-- ----------------------------
-- Records of system_soft_record
-- ----------------------------

-- ----------------------------
-- Table structure for system_soft_registry
-- ----------------------------
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

-- ----------------------------
-- Records of system_soft_registry
-- ----------------------------
INSERT INTO `system_soft_registry` VALUES (2, '默认', 'docker_hub', 'https://registry.cn-hangzhou.aliyuncs.com', '', '', '', NULL, NULL, 1, NULL, '', NULL, '2025-12-01 11:27:58', 1, NULL, NULL, 0, 'admin', 1, '2025-11-30 22:08:13.000000', '2025-12-04 22:00:06.253524', 'admin', 1, 1);
INSERT INTO `system_soft_registry` VALUES (3, 'github', 'github', 'https://api.github.com', '', '', '', NULL, NULL, 1, NULL, '', NULL, '2025-12-04 16:34:13', 1, NULL, NULL, 1, 'admin', 1, '2025-12-01 19:52:10.854316', '2025-12-04 16:34:13.000000', NULL, NULL, 1);

-- ----------------------------
-- Table structure for system_soft_version
-- ----------------------------
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

-- ----------------------------
-- Records of system_soft_version
-- ----------------------------

-- ----------------------------
-- Table structure for video_download
-- ----------------------------
DROP TABLE IF EXISTS `video_download`;
CREATE TABLE `video_download`  (
  `video_download_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '下载ID',
  `video_id` int NOT NULL COMMENT '视频ID',
  `video_download_url` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '下载地址',
  `video_download_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '下载名称',
  `video_download_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '下载类型',
  `video_download_quality` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '视频质量(标清/高清/超清)',
  `video_download_platform` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '下载平台',
  `video_download_size` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件大小',
  `video_download_magnetic` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '磁力链接',
  `video_download_status` tinyint NULL DEFAULT 1 COMMENT '下载状态(1:可用,0:不可用)',
  `video_download_count` int NULL DEFAULT 0 COMMENT '下载次数',
  `video_download_share_time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分享时间',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`video_download_id`) USING BTREE,
  INDEX `idx_video_id`(`video_id` ASC) USING BTREE,
  INDEX `idx_download_status`(`video_download_status` ASC) USING BTREE,
  INDEX `idx_download_quality`(`video_download_quality` ASC) USING BTREE,
  CONSTRAINT `video_download_ibfk_1` FOREIGN KEY (`video_id`) REFERENCES `video_info` (`video_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '视频下载链接表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of video_download
-- ----------------------------

-- ----------------------------
-- Table structure for video_info
-- ----------------------------
DROP TABLE IF EXISTS `video_info`;
CREATE TABLE `video_info`  (
  `video_id` int NOT NULL AUTO_INCREMENT COMMENT '视频唯一标识ID',
  `video_title` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '视频标题',
  `video_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '视频名称',
  `video_alias_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '视频别名',
  `video_score` decimal(3, 1) NULL DEFAULT NULL COMMENT '视频评分',
  `video_year` int NULL DEFAULT NULL COMMENT '视频年份',
  `video_platform` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '视频所属平台',
  `video_language` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '视频语言',
  `video_quality` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '视频清晰度',
  `video_thumbnail` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '视频缩略图URL',
  `video_cover` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '视频封面URL',
  `video_url` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '视频播放地址',
  `video_views` decimal(15, 0) NULL DEFAULT NULL COMMENT '视频观看次数',
  `video_likes` int NULL DEFAULT NULL COMMENT '视频点赞数',
  `video_status` int NULL DEFAULT 0 COMMENT '视频状态是否禁用;0:启用',
  `video_duration` int NULL DEFAULT NULL COMMENT '视频时长(分)',
  `video_version` int NULL DEFAULT 1 COMMENT '视频版本',
  `video_dou_ban_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '豆瓣视频ID',
  `video_publish_date` datetime NULL DEFAULT NULL COMMENT '视频发布日期',
  `video_category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '视频种类;MV;TV',
  `video_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '视频类型',
  `video_release` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '视频上映日期',
  `video_district` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '视频地区',
  `video_size` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '视频文件大小',
  `video_author` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '视频作者',
  `video_director` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '视频导演',
  `video_writer` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '视频编剧',
  `video_actor` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '视频主演',
  `video_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '视频描述内容',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`video_id`) USING BTREE,
  INDEX `idx_video_title`(`video_title` ASC) USING BTREE,
  INDEX `idx_video_year`(`video_year` ASC) USING BTREE,
  INDEX `idx_video_category`(`video_category` ASC) USING BTREE,
  INDEX `idx_video_status`(`video_status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '视频信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of video_info
-- ----------------------------

-- ----------------------------
-- Table structure for video_keyword
-- ----------------------------
DROP TABLE IF EXISTS `video_keyword`;
CREATE TABLE `video_keyword`  (
  `video_keyword_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关键词ID',
  `video_keyword_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关键词内容',
  `video_keyword_hot` int NULL DEFAULT 0 COMMENT '关键词热度',
  `video_keyword_sort` int NULL DEFAULT 0 COMMENT '关键词排序',
  `video_keyword_status` int NULL DEFAULT 1 COMMENT '关键词状态：0-禁用，1-启用',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`video_keyword_id`) USING BTREE,
  UNIQUE INDEX `uk_keyword_content`(`video_keyword_content` ASC) USING BTREE,
  INDEX `idx_keyword_hot`(`video_keyword_hot` ASC) USING BTREE,
  INDEX `idx_keyword_sort`(`video_keyword_sort` ASC) USING BTREE,
  INDEX `idx_keyword_status`(`video_keyword_status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '视频热门搜索关键词表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of video_keyword
-- ----------------------------
INSERT INTO `video_keyword` VALUES ('1', '热门电影', 100, 1, 1, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `video_keyword` VALUES ('2', '最新电视剧', 95, 2, 1, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `video_keyword` VALUES ('3', '经典动漫', 90, 3, 1, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `video_keyword` VALUES ('4', '纪录片', 85, 4, 1, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `video_keyword` VALUES ('5', '综艺节目', 80, 5, 1, NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for video_mark
-- ----------------------------
DROP TABLE IF EXISTS `video_mark`;
CREATE TABLE `video_mark`  (
  `video_mark_id` int NOT NULL AUTO_INCREMENT COMMENT '评分ID',
  `video_id` int NOT NULL COMMENT '视频ID',
  `video_mark_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '评分类型',
  `video_mark_score` decimal(3, 1) NULL DEFAULT NULL COMMENT '评分',
  `video_mark_people` int NULL DEFAULT 0 COMMENT '评分人数',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`video_mark_id`) USING BTREE,
  INDEX `idx_video_id`(`video_id` ASC) USING BTREE,
  INDEX `idx_mark_type`(`video_mark_type` ASC) USING BTREE,
  INDEX `idx_mark_score`(`video_mark_score` ASC) USING BTREE,
  CONSTRAINT `video_mark_ibfk_1` FOREIGN KEY (`video_id`) REFERENCES `video_info` (`video_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '视频评分表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of video_mark
-- ----------------------------

-- ----------------------------
-- Table structure for video_sync_config
-- ----------------------------
DROP TABLE IF EXISTS `video_sync_config`;
CREATE TABLE `video_sync_config`  (
  `video_sync_config_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置ID',
  `video_sync_config_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '同步名称',
  `video_sync_config_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '同步方式',
  `video_sync_config_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '同步地址',
  `video_sync_config_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '同步密钥',
  `video_sync_config_cookie` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '同步Cookie',
  `video_sync_config_extra` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '额外参数(JSON格式)',
  `video_sync_config_enabled` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用',
  `video_sync_config_source` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '同步来源(用于SPI实现类型)',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`video_sync_config_id`) USING BTREE,
  INDEX `idx_config_name`(`video_sync_config_name` ASC) USING BTREE,
  INDEX `idx_config_type`(`video_sync_config_type` ASC) USING BTREE,
  INDEX `idx_config_enabled`(`video_sync_config_enabled` ASC) USING BTREE,
  INDEX `idx_config_source`(`video_sync_config_source` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '视频同步配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of video_sync_config
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
