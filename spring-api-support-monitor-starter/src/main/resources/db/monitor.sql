/*
 Navicat Premium Data Transfer

 Source Server         : 172.16.0.40
 Source Server Type    : MySQL
 Source Server Version : 80016 (8.0.16)
 Source Host           : 172.16.0.40:3306
 Source Schema         : monitor

 Target Server Type    : MySQL
 Target Server Version : 80016 (8.0.16)
 File Encoding         : 65001

 Date: 01/08/2024 15:50:09
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for file_storage
-- ----------------------------
DROP TABLE IF EXISTS `file_storage`;
CREATE TABLE `file_storage`  (
  `file_storage_id` int(11) NOT NULL AUTO_INCREMENT,
  `file_storage_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '文件存储路径',
  `file_storage_desc` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '存储名称',
  `file_storage_type` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '实现类型',
  `file_storage_user` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '账号',
  `file_storage_password` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '密码',
  `file_storage_icon` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '图表',
  `file_storage_endpoint` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '远程访问端点',
  `file_storage_bucket` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'bucket',
  `file_storage_protocol_id` int(255) NULL DEFAULT NULL COMMENT '服务器协议; ',
  `file_storage_status` int(11) NULL DEFAULT NULL COMMENT '状态;0:停用',
  `file_storage_cookie` longtext CHARACTER SET utf8 COLLATE utf8_bin NULL COMMENT 'cookie',
  PRIMARY KEY (`file_storage_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = '文件存储' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of file_storage
-- ----------------------------
INSERT INTO `file_storage` VALUES (3, '本地磁盘测试', NULL, 'FILESYSTEM', NULL, NULL, 'el-icon-apple', 'Z://', 'test', 2, 1, NULL);
INSERT INTO `file_storage` VALUES (6, '本地FTP1', NULL, 'FTP', NULL, NULL, 'sc-icon-clipboard', 'ftp://127.0.0.1:21', 'oss', 2, 1, NULL);
INSERT INTO `file_storage` VALUES (7, 'SFTP', NULL, 'SFTP', 'root', 'q^EgK8quQ5!zgfRb7.18', 'el-icon-add-location', 'sftp://112.124.44.21:22', 'home', 2, 1, NULL);
INSERT INTO `file_storage` VALUES (9, '七牛', NULL, 'QINIU', '3vZ1Mi8Y_DBfjbGSJcKnztini_j_KcqBxnxi-JaW', '-m9bkUk7tqMi41M8Z4Ehl2GuVJSu9krzYEX4QN-u', 'sc-icon-bell', 'https://cdn.qirenit.com/', 'qiren-zxb', 2, 1, NULL);

-- ----------------------------
-- Table structure for file_storage_log
-- ----------------------------
DROP TABLE IF EXISTS `file_storage_log`;
CREATE TABLE `file_storage_log`  (
  `file_storage_log_id` int(11) NOT NULL AUTO_INCREMENT,
  `file_storage_id` int(11) NULL DEFAULT NULL,
  `file_storage_log_address` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '请求客户端地址',
  `file_storage_log_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '请求文件地址',
  PRIMARY KEY (`file_storage_log_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = '文件存储日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of file_storage_log
-- ----------------------------

-- ----------------------------
-- Table structure for file_storage_protocol
-- ----------------------------
DROP TABLE IF EXISTS `file_storage_protocol`;
CREATE TABLE `file_storage_protocol`  (
  `file_storage_protocol_id` int(255) NOT NULL AUTO_INCREMENT,
  `file_storage_protocol_port` int(11) NULL DEFAULT NULL COMMENT '开启远程服务器端口',
  `file_storage_protocol_desc` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '服务器说明',
  `file_storage_protocol_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '服务器协议; HTTP',
  `file_storage_protocol_host` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '开启远程服务器主机',
  `file_storage_protocol_status` int(11) NULL DEFAULT NULL COMMENT '远程服务器状态; 0:未开启',
  `file_storage_protocol_plugins` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '插件列表，多个逗号分隔',
  `file_storage_protocol_setting` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '配置列表，多个逗号分隔',
  `file_storage_protocol_plugin_open` int(11) NULL DEFAULT NULL COMMENT '是否开启插件；0:不开启',
  `file_storage_protocol_setting_open` int(11) NULL DEFAULT NULL COMMENT '是否开启配置；0:不开启',
  `file_storage_protocol_rang_open` int(11) NULL DEFAULT NULL COMMENT '是否开启Range；0:不开启',
  `file_storage_protocol_ua_open` int(11) NULL DEFAULT NULL COMMENT '是否开启UA；0:不开启',
  `file_storage_protocol_download_ua` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'UA',
  `file_storage_protocol_ua` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'UA',
  `file_storage_protocol_preview_or_download` int(11) NULL DEFAULT NULL COMMENT '0: 全部支持;1:预览; 2:下载',
  PRIMARY KEY (`file_storage_protocol_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = '文件存储服务器协议' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of file_storage_protocol
-- ----------------------------
INSERT INTO `file_storage_protocol` VALUES (2, 8184, '测试', 'HTTP', '0.0.0.0', 1, 'SEPIATONE', 'SHAPE,GRAY,ORIENT,FORMAT,QUALITY,INTERLACE,ROTATE,BLUR,THUMBNAIL,BRIGHT', 1, 1, 0, 0, NULL, 'PC;OSS', 0);

-- ----------------------------
-- Table structure for monitor_app
-- ----------------------------
DROP TABLE IF EXISTS `monitor_app`;
CREATE TABLE `monitor_app`  (
  `monitor_id` int(11) NOT NULL AUTO_INCREMENT,
  `monitor_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '监控名称',
  `monitor_appname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属应用',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '应用管理' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_app
-- ----------------------------
INSERT INTO `monitor_app` VALUES (1, '单点登录', 'oss', NULL, NULL, '2024-07-18 15:40:30', '2024-07-18 15:40:30', '2', NULL);

-- ----------------------------
-- Table structure for monitor_config
-- ----------------------------
DROP TABLE IF EXISTS `monitor_config`;
CREATE TABLE `monitor_config`  (
  `config_id` int(11) NOT NULL AUTO_INCREMENT,
  `config_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `config_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '值',
  `config_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `config_profile` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '环境',
  `config_appname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属应用',
  `config_status` int(11) NULL DEFAULT NULL COMMENT '0: 未开启；',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`config_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '配置中心' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_config
-- ----------------------------
INSERT INTO `monitor_config` VALUES (12, 'test', '123', NULL, 'test', 'oss', 1, NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for monitor_job
-- ----------------------------
DROP TABLE IF EXISTS `monitor_job`;
CREATE TABLE `monitor_job`  (
  `job_id` int(11) NOT NULL AUTO_INCREMENT,
  `job_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `job_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务类型; cron, fixed',
  `job_conf` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '时间配置',
  `job_status` int(11) NULL DEFAULT 0 COMMENT '状态;0: 未启用； 1: 正在运行',
  `job_app` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '对应的应用ID',
  `job_profile` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '环境',
  `job_execute_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模式; spring, xxl-job',
  `job_execute_bean` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'bean名称',
  `job_execute_route` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执行路由策略',
  `job_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `job_glue_updatetime` date NULL DEFAULT NULL COMMENT 'Glue最后更新时间',
  `job_glue_source` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '源码',
  `job_glue_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Glue类型（例如：脚本、JAR等）',
  `job_fail_retry` int(11) NULL DEFAULT NULL COMMENT '失败重试次数',
  `job_execute_timeout` int(11) NULL DEFAULT NULL COMMENT '超时时间',
  `job_execute_misfire_strategy` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '失效后策略',
  `job_execute_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执行参数',
  `job_execute_router` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执行路由策略',
  `job_trigger_last_time` bigint(20) NULL DEFAULT NULL COMMENT '任务触发的最后一次时间',
  `job_trigger_next_time` bigint(20) NULL DEFAULT NULL COMMENT '任务触发的下一次时间',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`job_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '调度任务' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_job
-- ----------------------------
INSERT INTO `monitor_job` VALUES (1, '测试', 'FIXED', '1', 0, 'dev', 'oss', NULL, '测试', NULL, NULL, NULL, NULL, 'BEAN', NULL, 10, NULL, NULL, 'FIRST', 0, 0, NULL, NULL, NULL, '2024-07-18 16:32:59', NULL, NULL);

-- ----------------------------
-- Table structure for monitor_job_log
-- ----------------------------
DROP TABLE IF EXISTS `monitor_job_log`;
CREATE TABLE `monitor_job_log`  (
  `job_log_id` int(11) NOT NULL AUTO_INCREMENT,
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
  `start_date` datetime NULL DEFAULT NULL,
  `end_date` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`job_log_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '调度任务日志' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_job_log
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_limit
-- ----------------------------
DROP TABLE IF EXISTS `monitor_limit`;
CREATE TABLE `monitor_limit`  (
  `limit_id` int(11) NOT NULL AUTO_INCREMENT,
  `limit_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '限流名称',
  `limit_mapping` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '限流地址',
  `limit_permits` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '每秒次数',
  `limit_status` int(11) NULL DEFAULT NULL COMMENT '是否开启; 0: 不开启',
  `limit_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '限流实现方式, guava',
  `limit_resolver` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '限流模式,URL, REMOTE',
  `limit_profile` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '环境',
  `limit_app` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '限流应用',
  `limit_remote_list` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'REMOTE模式下的黑名单',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`limit_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '限流管理' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_limit
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_log
-- ----------------------------
DROP TABLE IF EXISTS `monitor_log`;
CREATE TABLE `monitor_log`  (
  `log_id` int(11) NOT NULL AUTO_INCREMENT,
  `log_module_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模块',
  `log_command_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '命令',
  `log_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '内容',
  `log_appname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '应用',
  `log_profile` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '环境',
  `log_host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地址',
  `log_port` int(11) NULL DEFAULT NULL COMMENT '端口',
  `log_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '响应码',
  `log_msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误消息',
  `tenant_id` int(11) NULL DEFAULT 0 COMMENT '租户ID',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`log_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统日志' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_log
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_mybatis
-- ----------------------------
DROP TABLE IF EXISTS `monitor_mybatis`;
CREATE TABLE `monitor_mybatis`  (
  `monitor_mybatis_id` int(11) NOT NULL AUTO_INCREMENT,
  `monitor_mybatis_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `monitor_mybatis_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `monitor_mybatis_sql` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'sql',
  `monitor_mybatis_sql_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'sql类型, xml, sql',
  `monitor_mybatis_model_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'model',
  `monitor_mybatis_status` int(11) NULL DEFAULT NULL COMMENT '0: 未开启；',
  `monitor_mybatis_mapper_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'mapper',
  `monitor_mybatis_profile` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '环境',
  `monitor_appname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '应用',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_mybatis_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'mybatis管理' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_mybatis
-- ----------------------------
INSERT INTO `monitor_mybatis` VALUES (2, 'test', NULL, 'SELECT * FROM sys_user limit 2', 'sql', 'com.chua.starter.sso.impl.SysUser', 1, 'com.chua.starter.sso.mapper.SysUserMapper', 'test', 'oss', NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for monitor_patch
-- ----------------------------
DROP TABLE IF EXISTS `monitor_patch`;
CREATE TABLE `monitor_patch`  (
  `monitor_patch_id` int(11) NOT NULL AUTO_INCREMENT,
  `monitor_patch_profile` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '补丁所属环境',
  `monitor_patch_app` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '补丁所属应用',
  `monitor_patch_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '补丁名称',
  `monitor_patch_chinese_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '中文名称',
  `monitor_patch_pack` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '补丁包路径',
  `monitor_patch_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `monitor_patch_version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '版本',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_patch_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'hotswap包管理' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_patch
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_project
-- ----------------------------
DROP TABLE IF EXISTS `monitor_project`;
CREATE TABLE `monitor_project`  (
  `project_id` int(11) NOT NULL AUTO_INCREMENT,
  `project_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '版本名称',
  `project_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `project_control_user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账号',
  `project_control_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
  `project_control_host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '主机',
  `project_control_port` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '端口',
  `project_project_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '项目位置',
  `project_control_uid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'uid',
  PRIMARY KEY (`project_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '版本管理' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_project
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_project_version
-- ----------------------------
DROP TABLE IF EXISTS `monitor_project_version`;
CREATE TABLE `monitor_project_version`  (
  `version_id` int(11) NOT NULL AUTO_INCREMENT,
  `project_id` int(11) NULL DEFAULT NULL COMMENT '版本ID',
  `version_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `version_run_script` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '启动脚本名称',
  `version_stop_script` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '停止脚本名称',
  `version_log` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志名称',
  `version_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '版本说明',
  `version_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '版本编号',
  `version_status` int(11) NULL DEFAULT NULL COMMENT '状态; 0:未启动',
  `version_log_path_position` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '版本状态;0: 未启动',
  PRIMARY KEY (`version_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '版本控制-子版本' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_project_version
-- ----------------------------
INSERT INTO `monitor_project_version` VALUES (2, 2, 'attendance', 'start.sh', NULL, '/zxb2/attendance/start.log', NULL, '01hqq7ts3b8qhamn6z6phn4rsb', 0, '1');

-- ----------------------------
-- Table structure for monitor_proxy
-- ----------------------------
DROP TABLE IF EXISTS `monitor_proxy`;
CREATE TABLE `monitor_proxy`  (
  `proxy_id` int(11) NOT NULL AUTO_INCREMENT,
  `proxy_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `proxy_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `proxy_host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0.0.0.0' COMMENT '启动地址',
  `proxy_port` int(11) NULL DEFAULT NULL COMMENT '端口',
  `proxy_protocol` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '协议',
  `proxy_status` int(11) NULL DEFAULT NULL COMMENT '0: 未开启',
  `proxy_version` int(11) NULL DEFAULT NULL COMMENT '版本',
  PRIMARY KEY (`proxy_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '代理管理' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_proxy
-- ----------------------------
INSERT INTO `monitor_proxy` VALUES (5, '这是一个测试代理', NULL, '127.0.0.1', 12345, 'proxy', 1, NULL);

-- ----------------------------
-- Table structure for monitor_proxy_config
-- ----------------------------
DROP TABLE IF EXISTS `monitor_proxy_config`;
CREATE TABLE `monitor_proxy_config`  (
  `config_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `config_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '配置名称',
  `config_value` varchar(4096) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '配置值',
  `config_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '配置描述',
  `proxy_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '代理ID',
  PRIMARY KEY (`config_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '代理配置' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_proxy_config
-- ----------------------------
INSERT INTO `monitor_proxy_config` VALUES (4, 'balance', 'WEIGHT', '负载均衡', '5');
INSERT INTO `monitor_proxy_config` VALUES (8, 'open-log', 'true', '开启日志', '5');
INSERT INTO `monitor_proxy_config` VALUES (9, 'serviceDiscovery', 'STATISTIC', '静态代理', '5');
INSERT INTO `monitor_proxy_config` VALUES (10, 'open-limit', 'true', '开启限流', '5');
INSERT INTO `monitor_proxy_config` VALUES (12, 'open-black', 'true', '开启黑名单', '5');
INSERT INTO `monitor_proxy_config` VALUES (13, 'open-white', 'true', '开启白名单', '5');
INSERT INTO `monitor_proxy_config` VALUES (14, 'open-ip-limit', 'true', '开启IP限流', '5');
INSERT INTO `monitor_proxy_config` VALUES (15, 'open-url-limit', 'true', '开启地址限流', '5');

-- ----------------------------
-- Table structure for monitor_proxy_limit
-- ----------------------------
DROP TABLE IF EXISTS `monitor_proxy_limit`;
CREATE TABLE `monitor_proxy_limit`  (
  `limit_id` int(11) NOT NULL AUTO_INCREMENT,
  `limit_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '限流ip地址',
  `limit _type` int(11) NULL DEFAULT NULL COMMENT '限流类型; 0: url限流; 1: ip限流',
  `limit_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '限流url地址',
  `limit_disable` int(11) NULL DEFAULT 0 COMMENT '是否开启； 0：未开启',
  `limit_permits_per_second` int(11) NULL DEFAULT NULL COMMENT '每秒生产令牌数',
  `proxy_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '代理ID',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`limit_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '代理限流器' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_proxy_limit
-- ----------------------------
INSERT INTO `monitor_proxy_limit` VALUES (1, '192.168.110.8', 0, '/', 1, 2, '5', NULL, '2024-07-30 10:19:50');
INSERT INTO `monitor_proxy_limit` VALUES (6, '1.2.1.2', 1, NULL, 1, 1, '5', '2024-07-30 14:42:26', '2024-07-30 15:02:35');

-- ----------------------------
-- Table structure for monitor_proxy_limit_list
-- ----------------------------
DROP TABLE IF EXISTS `monitor_proxy_limit_list`;
CREATE TABLE `monitor_proxy_limit_list`  (
  `list_id` int(11) NOT NULL AUTO_INCREMENT,
  `list_type` int(11) NULL DEFAULT NULL COMMENT '类型;0:白名单; 1:黑名单',
  `list_status` int(11) NULL DEFAULT NULL COMMENT '是否开启; 0:不开启',
  `list_ip` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT 'IP',
  `proxy_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '代理ID',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`list_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = '名单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of monitor_proxy_limit_list
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_proxy_limit_log
-- ----------------------------
DROP TABLE IF EXISTS `monitor_proxy_limit_log`;
CREATE TABLE `monitor_proxy_limit_log`  (
  `limit_log_id` int(11) NOT NULL AUTO_INCREMENT,
  `limit_log_server_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '日志对应的代理',
  `limit_log_from` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '来源模块',
  `limit_log_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '请求地址',
  `limit_log_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '类型;deny;allow',
  `limit_log_address_geo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT 'geo',
  `limit_log_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '请求端地址',
  `create_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_time_min` bigint(20) NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`limit_log_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3328 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '限流日志' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_proxy_limit_log
-- ----------------------------
INSERT INTO `monitor_proxy_limit_log` VALUES (2312, '5', '地址限流', '/system/api/doc.html', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:27:41', 1722331620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2313, '5', '黑名单', '/system/api/doc.html', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:27:41', 1722331620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2314, '5', 'IP限流', '/system/api/doc.html', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:27:41', 1722331620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2315, '5', '白名单', '/system/api/doc.html', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:27:41', 1722331620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2316, '5', '白名单', '/system/api/doc.html', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2317, '5', 'IP限流', '/system/api/doc.html', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2318, '5', '地址限流', '/system/api/doc.html', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2319, '5', '黑名单', '/system/api/doc.html', 'deny', '局域网', '192.168.110.8', '2024-07-31 09:03:26', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2320, '5', '地址限流', '/system/api/webjars/css/app.56b4fde5.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2321, '5', 'IP限流', '/system/api/webjars/css/app.56b4fde5.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2322, '5', '黑名单', '/system/api/webjars/css/app.56b4fde5.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2323, '5', '白名单', '/system/api/webjars/css/app.56b4fde5.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2324, '5', '白名单', '/system/api/webjars/js/chunk-vendors.c45f9828.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2325, '5', '黑名单', '/system/api/webjars/css/chunk-vendors.46fab8e1.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2326, '5', 'IP限流', '/system/api/webjars/css/chunk-vendors.46fab8e1.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2327, '5', '黑名单', '/system/api/webjars/js/chunk-vendors.c45f9828.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2328, '5', 'IP限流', '/system/api/webjars/js/chunk-vendors.c45f9828.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2329, '5', '地址限流', '/system/api/webjars/js/chunk-vendors.c45f9828.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2330, '5', '黑名单', '/system/api/webjars/js/app.58a914a0.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2331, '5', '白名单', '/system/api/webjars/js/app.58a914a0.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2332, '5', 'IP限流', '/system/api/webjars/js/app.58a914a0.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2333, '5', '地址限流', '/system/api/webjars/css/chunk-vendors.46fab8e1.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2334, '5', '地址限流', '/system/api/webjars/js/app.58a914a0.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2335, '5', '黑名单', '/system/api/webjars/css/chunk-183657a2.8ed15ee7.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2336, '5', '白名单', '/system/api/webjars/css/chunk-183657a2.8ed15ee7.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2337, '5', 'IP限流', '/system/api/webjars/css/chunk-183657a2.8ed15ee7.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2338, '5', '地址限流', '/system/api/webjars/css/chunk-183657a2.8ed15ee7.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2339, '5', '白名单', '/system/api/webjars/css/chunk-vendors.46fab8e1.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2340, '5', '黑名单', '/system/api/webjars/js/chunk-0c58d94d.6bf494e7.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2341, '5', '白名单', '/system/api/webjars/js/chunk-0c58d94d.6bf494e7.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2342, '5', '地址限流', '/system/api/webjars/js/chunk-0c58d94d.6bf494e7.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2343, '5', 'IP限流', '/system/api/webjars/js/chunk-0c58d94d.6bf494e7.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2344, '5', 'IP限流', '/system/api/webjars/js/chunk-0fd67716.d57e2c41.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2345, '5', '地址限流', '/system/api/webjars/js/chunk-0fd67716.d57e2c41.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2346, '5', '黑名单', '/system/api/webjars/js/chunk-0fd67716.d57e2c41.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2347, '5', '白名单', '/system/api/webjars/js/chunk-0fd67716.d57e2c41.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2348, '5', 'IP限流', '/system/api/webjars/js/chunk-183657a2.2ce62f61.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2349, '5', '黑名单', '/system/api/webjars/js/chunk-214218f0.92c7a521.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2350, '5', '地址限流', '/system/api/webjars/js/chunk-214218f0.92c7a521.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2351, '5', '白名单', '/system/api/webjars/js/chunk-214218f0.92c7a521.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2352, '5', '黑名单', '/system/api/webjars/js/chunk-183657a2.2ce62f61.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2353, '5', '白名单', '/system/api/webjars/js/chunk-183657a2.2ce62f61.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2354, '5', 'IP限流', '/system/api/webjars/js/chunk-214218f0.92c7a521.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2355, '5', '地址限流', '/system/api/webjars/js/chunk-183657a2.2ce62f61.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2356, '5', 'IP限流', '/system/api/webjars/js/chunk-2d0af44e.81b7dfc1.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2357, '5', '地址限流', '/system/api/webjars/js/chunk-2d0af44e.81b7dfc1.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2358, '5', '白名单', '/system/api/webjars/js/chunk-2d0af44e.81b7dfc1.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2359, '5', '黑名单', '/system/api/webjars/js/chunk-2d0af44e.81b7dfc1.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2360, '5', '黑名单', '/system/api/webjars/js/chunk-2d0bd799.d162a2d5.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2361, '5', '地址限流', '/system/api/webjars/js/chunk-2d0bd799.d162a2d5.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2362, '5', 'IP限流', '/system/api/webjars/js/chunk-2d0bd799.d162a2d5.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2363, '5', '白名单', '/system/api/webjars/js/chunk-2d0bd799.d162a2d5.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2364, '5', '白名单', '/system/api/webjars/js/chunk-2d0da532.b56aa8db.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2365, '5', 'IP限流', '/system/api/webjars/js/chunk-2d0da532.b56aa8db.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2366, '5', '黑名单', '/system/api/webjars/js/chunk-2d22269d.bb355f65.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2367, '5', '地址限流', '/system/api/webjars/js/chunk-2d22269d.bb355f65.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2368, '5', '地址限流', '/system/api/webjars/js/chunk-2d0da532.b56aa8db.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2369, '5', '黑名单', '/system/api/webjars/js/chunk-2d0da532.b56aa8db.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2370, '5', 'IP限流', '/system/api/webjars/js/chunk-2d22269d.bb355f65.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2371, '5', '黑名单', '/system/api/webjars/js/chunk-3b888a65.8737ce4f.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2372, '5', '白名单', '/system/api/webjars/js/chunk-2d22269d.bb355f65.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2373, '5', 'IP限流', '/system/api/webjars/js/chunk-3b888a65.8737ce4f.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2374, '5', '地址限流', '/system/api/webjars/js/chunk-3b888a65.8737ce4f.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2375, '5', '黑名单', '/system/api/webjars/js/chunk-589faee0.08c2fba5.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2376, '5', '白名单', '/system/api/webjars/js/chunk-3b888a65.8737ce4f.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2377, '5', '白名单', '/system/api/webjars/js/chunk-589faee0.08c2fba5.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2378, '5', 'IP限流', '/system/api/webjars/js/chunk-589faee0.08c2fba5.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2379, '5', '地址限流', '/system/api/webjars/js/chunk-589faee0.08c2fba5.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2380, '5', 'IP限流', '/system/api/webjars/js/chunk-735c675c.76398496.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2381, '5', '黑名单', '/system/api/webjars/js/chunk-735c675c.76398496.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2382, '5', '白名单', '/system/api/webjars/js/chunk-adb9e944.9e6584b5.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2383, '5', 'IP限流', '/system/api/webjars/js/chunk-adb9e944.9e6584b5.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2384, '5', '地址限流', '/system/api/webjars/js/chunk-adb9e944.9e6584b5.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2385, '5', '白名单', '/system/api/webjars/js/chunk-735c675c.76398496.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2386, '5', '地址限流', '/system/api/webjars/js/chunk-735c675c.76398496.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2387, '5', '黑名单', '/system/api/webjars/js/chunk-adb9e944.9e6584b5.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:09', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2388, '5', '黑名单', '/system/api/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:11', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2389, '5', '地址限流', '/system/api/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:11', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2390, '5', '白名单', '/system/api/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:11', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2391, '5', 'IP限流', '/system/api/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:11', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2392, '5', 'IP限流', '/system/api/swagger-resources/configuration/ui', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:11', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2393, '5', '黑名单', '/system/api/swagger-resources/configuration/ui', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:11', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2394, '5', '地址限流', '/system/api/swagger-resources/configuration/ui', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:11', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2395, '5', '地址限流', '/system/api/swagger-resources', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:11', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2396, '5', '黑名单', '/system/api/swagger-resources', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:11', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2397, '5', '白名单', '/system/api/swagger-resources/configuration/ui', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:11', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2398, '5', '白名单', '/system/api/swagger-resources', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:11', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2399, '5', 'IP限流', '/system/api/swagger-resources', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:11', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2400, '5', 'IP限流', '/system/api/v2/api-docs?group=biz', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:11', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2401, '5', '黑名单', '/system/api/v2/api-docs?group=biz', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:11', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2402, '5', '白名单', '/system/api/v2/api-docs?group=biz', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:11', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2403, '5', '地址限流', '/system/api/v2/api-docs?group=biz', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:29:11', 1722331740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2404, '5', '白名单', '/', 'allow', '本机地址', '127.0.0.1', '2024-07-31 10:14:42', 1722392040000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2405, '5', '黑名单', '/', 'allow', '本机地址', '127.0.0.1', '2024-07-31 10:14:42', 1722392040000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2406, '5', '地址限流', '/', 'allow', '本机地址', '127.0.0.1', '2024-07-31 10:14:42', 1722392040000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2407, '5', 'IP限流', '/', 'allow', '本机地址', '127.0.0.1', '2024-07-31 10:14:42', 1722392040000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2408, '5', 'IP限流', '/favicon.ico', 'allow', '本机地址', '127.0.0.1', '2024-07-31 10:14:42', 1722392040000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2409, '5', '地址限流', '/favicon.ico', 'allow', '本机地址', '127.0.0.1', '2024-07-31 10:14:42', 1722392040000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2410, '5', '白名单', '/favicon.ico', 'allow', '本机地址', '127.0.0.1', '2024-07-31 10:14:42', 1722392040000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2411, '5', '黑名单', '/favicon.ico', 'allow', '本机地址', '127.0.0.1', '2024-07-31 10:14:42', 1722392040000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2412, '5', 'IP限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:14:52', 1722392040000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2413, '5', '白名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:14:52', 1722392040000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2414, '5', '地址限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:14:52', 1722392040000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2415, '5', '黑名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:14:52', 1722392040000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2416, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:14:52', 1722392040000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2417, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:14:52', 1722392040000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2418, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:14:52', 1722392040000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2419, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:14:52', 1722392040000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2420, '5', 'IP限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:16:30', 1722392160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2421, '5', '白名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:16:30', 1722392160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2422, '5', '黑名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:16:30', 1722392160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2423, '5', '地址限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:16:30', 1722392160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2424, '5', '地址限流', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:16:31', 1722392160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2425, '5', '黑名单', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:16:31', 1722392160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2426, '5', '白名单', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:16:31', 1722392160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2427, '5', 'IP限流', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:16:31', 1722392160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2428, '5', 'IP限流', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:16:31', 1722392160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2429, '5', '白名单', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:16:31', 1722392160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2430, '5', '地址限流', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:16:31', 1722392160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2431, '5', '黑名单', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:16:31', 1722392160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2432, '5', '黑名单', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:16:31', 1722392160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2433, '5', '白名单', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:16:31', 1722392160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2434, '5', 'IP限流', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:16:31', 1722392160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2435, '5', '黑名单', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:16:31', 1722392160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2436, '5', '地址限流', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:16:31', 1722392160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2437, '5', 'IP限流', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:16:31', 1722392160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2438, '5', '白名单', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:16:31', 1722392160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2439, '5', '地址限流', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:16:31', 1722392160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2440, '5', '白名单', '/zxb/school/assets/components.b0153bba.css', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:16:31', 1722392160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2441, '5', '黑名单', '/zxb/school/assets/components.b0153bba.css', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:16:31', 1722392160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2442, '5', '地址限流', '/zxb/school/assets/components.b0153bba.css', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:16:31', 1722392160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2443, '5', 'IP限流', '/zxb/school/assets/components.b0153bba.css', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:16:31', 1722392160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2444, '5', '黑名单', '/zxb/school/assets/antd.5ee291b2.css', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:16:31', 1722392160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2445, '5', 'IP限流', '/zxb/school/assets/antd.5ee291b2.css', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:16:31', 1722392160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2446, '5', '地址限流', '/zxb/school/assets/antd.5ee291b2.css', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:16:31', 1722392160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2447, '5', '白名单', '/zxb/school/assets/antd.5ee291b2.css', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:16:31', 1722392160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2448, '5', '白名单', '/zxb/school/assets/school.694a1ada.png', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:16:32', 1722392160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2449, '5', 'IP限流', '/zxb/school/assets/school.694a1ada.png', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:16:32', 1722392160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2450, '5', '地址限流', '/zxb/school/assets/school.694a1ada.png', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:16:32', 1722392160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2451, '5', '黑名单', '/zxb/school/assets/school.694a1ada.png', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:16:32', 1722392160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2452, '5', '黑名单', '/zxb/school/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:16:32', 1722392160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2453, '5', '地址限流', '/zxb/school/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:16:32', 1722392160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2454, '5', '白名单', '/zxb/school/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:16:32', 1722392160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2455, '5', 'IP限流', '/zxb/school/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:16:32', 1722392160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2456, '5', '黑名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:16:44', 1722392160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2457, '5', '白名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:16:44', 1722392160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2458, '5', 'IP限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:16:44', 1722392160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2459, '5', '地址限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:16:44', 1722392160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2460, '5', '黑名单', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:44:10', 1722393840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2461, '5', 'IP限流', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:44:10', 1722393840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2462, '5', '地址限流', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:44:10', 1722393840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2463, '5', '白名单', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:44:10', 1722393840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2464, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:44:58', 1722393840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2465, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:44:58', 1722393840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2466, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:44:58', 1722393840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2467, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:44:58', 1722393840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2468, '5', 'IP限流', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:00', 1722393840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2469, '5', '白名单', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:00', 1722393840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2470, '5', '地址限流', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:00', 1722393840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2471, '5', '黑名单', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:00', 1722393840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2472, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:05', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2473, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:05', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2474, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:05', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2475, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:05', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2476, '5', 'IP限流', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:06', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2477, '5', '黑名单', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:06', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2478, '5', '白名单', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:06', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2479, '5', '地址限流', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:06', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2480, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:06', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2481, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:06', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2482, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:06', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2483, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:06', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2484, '5', '黑名单', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:06', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2485, '5', '白名单', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:06', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2486, '5', 'IP限流', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:06', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2487, '5', '地址限流', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:06', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2488, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:06', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2489, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:06', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2490, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:06', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2491, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:06', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2492, '5', '地址限流', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:06', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2493, '5', '白名单', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:06', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2494, '5', '黑名单', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:06', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2495, '5', 'IP限流', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:06', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2496, '5', '地址限流', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:07', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2497, '5', '白名单', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:07', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2498, '5', 'IP限流', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:07', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2499, '5', '黑名单', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:07', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2500, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:07', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2501, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:07', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2502, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:07', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2503, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:07', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2504, '5', '白名单', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:17', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2505, '5', '地址限流', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:17', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2506, '5', 'IP限流', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:17', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2507, '5', '黑名单', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:17', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2508, '5', 'IP限流', '/sugrec?&prod=pc_his&from=pc_web&json=1&sid=60274_60467_60492_60502_60551&hisdata=&_t=1722393918499&req=2&bs=1&csor=0', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:19', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2509, '5', '黑名单', '/sugrec?&prod=pc_his&from=pc_web&json=1&sid=60274_60467_60492_60502_60551&hisdata=&_t=1722393918499&req=2&bs=1&csor=0', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:19', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2510, '5', '地址限流', '/sugrec?&prod=pc_his&from=pc_web&json=1&sid=60274_60467_60492_60502_60551&hisdata=&_t=1722393918499&req=2&bs=1&csor=0', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:19', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2511, '5', '白名单', '/sugrec?&prod=pc_his&from=pc_web&json=1&sid=60274_60467_60492_60502_60551&hisdata=&_t=1722393918499&req=2&bs=1&csor=0', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:19', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2512, '5', '白名单', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:19', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2513, '5', 'IP限流', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:19', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2514, '5', '地址限流', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:19', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2515, '5', '黑名单', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:19', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2516, '5', '白名单', '/sugrec?&prod=pc_his&from=pc_web&json=1&sid=60274_60467_60492_60502_60551&hisdata=&_t=1722393920211&req=2&bs=1&csor=0', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:20', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2517, '5', '地址限流', '/sugrec?&prod=pc_his&from=pc_web&json=1&sid=60274_60467_60492_60502_60551&hisdata=&_t=1722393920211&req=2&bs=1&csor=0', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:20', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2518, '5', 'IP限流', '/sugrec?&prod=pc_his&from=pc_web&json=1&sid=60274_60467_60492_60502_60551&hisdata=&_t=1722393920211&req=2&bs=1&csor=0', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:20', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2519, '5', '黑名单', '/sugrec?&prod=pc_his&from=pc_web&json=1&sid=60274_60467_60492_60502_60551&hisdata=&_t=1722393920211&req=2&bs=1&csor=0', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:20', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2520, '5', 'IP限流', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:22', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2521, '5', '白名单', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:22', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2522, '5', '黑名单', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:22', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2523, '5', '地址限流', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:22', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2524, '5', '白名单', '/sugrec?&prod=pc_his&from=pc_web&json=1&sid=60274_60467_60492_60502_60551&hisdata=&_t=1722393922760&req=2&bs=1&csor=0', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:23', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2525, '5', 'IP限流', '/sugrec?&prod=pc_his&from=pc_web&json=1&sid=60274_60467_60492_60502_60551&hisdata=&_t=1722393922760&req=2&bs=1&csor=0', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:23', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2526, '5', '黑名单', '/sugrec?&prod=pc_his&from=pc_web&json=1&sid=60274_60467_60492_60502_60551&hisdata=&_t=1722393922760&req=2&bs=1&csor=0', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:23', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2527, '5', '地址限流', '/sugrec?&prod=pc_his&from=pc_web&json=1&sid=60274_60467_60492_60502_60551&hisdata=&_t=1722393922760&req=2&bs=1&csor=0', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:45:23', 1722393900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2528, '5', '黑名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=1&req=2&bs=1&csor=1&sugmode=2&hot_launch=0&cb=jQuery110201299900919930772_1722393922512&_=1722393922513', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:55', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2529, '5', 'IP限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=1&req=2&bs=1&csor=1&sugmode=2&hot_launch=0&cb=jQuery110201299900919930772_1722393922512&_=1722393922513', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:55', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2530, '5', '地址限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=1&req=2&bs=1&csor=1&sugmode=2&hot_launch=0&cb=jQuery110201299900919930772_1722393922512&_=1722393922513', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:55', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2531, '5', '白名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=1&req=2&bs=1&csor=1&sugmode=2&hot_launch=0&cb=jQuery110201299900919930772_1722393922512&_=1722393922513', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:55', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2532, '5', '黑名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=1&req=2&bs=1&csor=0&pwd=1&sugmode=2&hot_launch=0&cb=jQuery110201299900919930772_1722393922512&_=1722393922514', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:55', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2533, '5', '地址限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=1&req=2&bs=1&csor=0&pwd=1&sugmode=2&hot_launch=0&cb=jQuery110201299900919930772_1722393922512&_=1722393922514', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:55', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2534, '5', 'IP限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=1&req=2&bs=1&csor=0&pwd=1&sugmode=2&hot_launch=0&cb=jQuery110201299900919930772_1722393922512&_=1722393922514', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:55', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2535, '5', '白名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=1&req=2&bs=1&csor=0&pwd=1&sugmode=2&hot_launch=0&cb=jQuery110201299900919930772_1722393922512&_=1722393922514', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:55', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2536, '5', '地址限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=n&req=2&bs=1&csor=1&pwd=1&cb=jQuery110201299900919930772_1722393922512&_=1722393922515', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:56', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2537, '5', '黑名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=n&req=2&bs=1&csor=1&pwd=1&cb=jQuery110201299900919930772_1722393922512&_=1722393922515', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:56', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2538, '5', 'IP限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=n&req=2&bs=1&csor=1&pwd=1&cb=jQuery110201299900919930772_1722393922512&_=1722393922515', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:56', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2539, '5', '白名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=n&req=2&bs=1&csor=1&pwd=1&cb=jQuery110201299900919930772_1722393922512&_=1722393922515', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:56', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2540, '5', 'IP限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=ng&req=2&bs=1&csor=2&pwd=n&cb=jQuery110201299900919930772_1722393922512&_=1722393922516', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:56', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2541, '5', '黑名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=ng&req=2&bs=1&csor=2&pwd=n&cb=jQuery110201299900919930772_1722393922512&_=1722393922516', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:56', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2542, '5', '地址限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=ng&req=2&bs=1&csor=2&pwd=n&cb=jQuery110201299900919930772_1722393922512&_=1722393922516', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:56', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2543, '5', '白名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=ng&req=2&bs=1&csor=2&pwd=n&cb=jQuery110201299900919930772_1722393922512&_=1722393922516', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:56', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2544, '5', '黑名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=ngi&req=2&bs=1&csor=3&pwd=ng&cb=jQuery110201299900919930772_1722393922512&_=1722393922517', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:56', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2545, '5', '地址限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=ngi&req=2&bs=1&csor=3&pwd=ng&cb=jQuery110201299900919930772_1722393922512&_=1722393922517', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:56', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2546, '5', 'IP限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=ngi&req=2&bs=1&csor=3&pwd=ng&cb=jQuery110201299900919930772_1722393922512&_=1722393922517', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:56', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2547, '5', '白名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=ngi&req=2&bs=1&csor=3&pwd=ng&cb=jQuery110201299900919930772_1722393922512&_=1722393922517', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:56', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2548, '5', '白名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=ngin&req=2&bs=1&csor=4&pwd=ngi&cb=jQuery110201299900919930772_1722393922512&_=1722393922518', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:56', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2549, '5', '地址限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=ngin&req=2&bs=1&csor=4&pwd=ngi&cb=jQuery110201299900919930772_1722393922512&_=1722393922518', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:56', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2550, '5', 'IP限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=ngin&req=2&bs=1&csor=4&pwd=ngi&cb=jQuery110201299900919930772_1722393922512&_=1722393922518', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:56', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2551, '5', '黑名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=ngin&req=2&bs=1&csor=4&pwd=ngi&cb=jQuery110201299900919930772_1722393922512&_=1722393922518', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:56', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2552, '5', '黑名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx&req=2&bs=1&csor=5&pwd=ngin&cb=jQuery110201299900919930772_1722393922512&_=1722393922519', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:57', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2553, '5', 'IP限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx&req=2&bs=1&csor=5&pwd=ngin&cb=jQuery110201299900919930772_1722393922512&_=1722393922519', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:57', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2554, '5', '地址限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx&req=2&bs=1&csor=5&pwd=ngin&cb=jQuery110201299900919930772_1722393922512&_=1722393922519', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:57', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2555, '5', '白名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx&req=2&bs=1&csor=5&pwd=ngin&cb=jQuery110201299900919930772_1722393922512&_=1722393922519', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:57', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2556, '5', '白名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20&req=2&bs=1&csor=6&pwd=nginx&cb=jQuery110201299900919930772_1722393922512&_=1722393922520', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:57', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2557, '5', '黑名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20&req=2&bs=1&csor=6&pwd=nginx&cb=jQuery110201299900919930772_1722393922512&_=1722393922520', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:57', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2558, '5', 'IP限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20&req=2&bs=1&csor=6&pwd=nginx&cb=jQuery110201299900919930772_1722393922512&_=1722393922520', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:57', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2559, '5', '地址限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20&req=2&bs=1&csor=6&pwd=nginx&cb=jQuery110201299900919930772_1722393922512&_=1722393922520', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:57', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2560, '5', '地址限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20d&req=2&bs=1&csor=7&pwd=nginx%20&cb=jQuery110201299900919930772_1722393922512&_=1722393922521', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:57', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2561, '5', 'IP限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20d&req=2&bs=1&csor=7&pwd=nginx%20&cb=jQuery110201299900919930772_1722393922512&_=1722393922521', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:57', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2562, '5', '黑名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20d&req=2&bs=1&csor=7&pwd=nginx%20&cb=jQuery110201299900919930772_1722393922512&_=1722393922521', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:57', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2563, '5', '白名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20d&req=2&bs=1&csor=7&pwd=nginx%20&cb=jQuery110201299900919930772_1722393922512&_=1722393922521', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:57', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2564, '5', '地址限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20du&req=2&bs=1&csor=8&pwd=nginx%20d&cb=jQuery110201299900919930772_1722393922512&_=1722393922522', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:58', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2565, '5', 'IP限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20du&req=2&bs=1&csor=8&pwd=nginx%20d&cb=jQuery110201299900919930772_1722393922512&_=1722393922522', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:58', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2566, '5', '黑名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20du&req=2&bs=1&csor=8&pwd=nginx%20d&cb=jQuery110201299900919930772_1722393922512&_=1722393922522', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:58', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2567, '5', '白名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20du&req=2&bs=1&csor=8&pwd=nginx%20d&cb=jQuery110201299900919930772_1722393922512&_=1722393922522', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:58', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2568, '5', '地址限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20duo&req=2&bs=1&csor=9&pwd=nginx%20du&cb=jQuery110201299900919930772_1722393922512&_=1722393922523', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:58', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2569, '5', '黑名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20duo&req=2&bs=1&csor=9&pwd=nginx%20du&cb=jQuery110201299900919930772_1722393922512&_=1722393922523', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:58', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2570, '5', 'IP限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20duo&req=2&bs=1&csor=9&pwd=nginx%20du&cb=jQuery110201299900919930772_1722393922512&_=1722393922523', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:58', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2571, '5', '白名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20duo&req=2&bs=1&csor=9&pwd=nginx%20du&cb=jQuery110201299900919930772_1722393922512&_=1722393922523', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:58', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2572, '5', '黑名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20du&req=2&bs=1&csor=8&pwd=nginx%20duo&cb=jQuery110201299900919930772_1722393922512&_=1722393922524', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:58', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2573, '5', '白名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20du&req=2&bs=1&csor=8&pwd=nginx%20duo&cb=jQuery110201299900919930772_1722393922512&_=1722393922524', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:58', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2574, '5', '地址限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20du&req=2&bs=1&csor=8&pwd=nginx%20duo&cb=jQuery110201299900919930772_1722393922512&_=1722393922524', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:58', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2575, '5', 'IP限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20du&req=2&bs=1&csor=8&pwd=nginx%20duo&cb=jQuery110201299900919930772_1722393922512&_=1722393922524', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:58', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2576, '5', '白名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20d&req=2&bs=1&csor=7&pwd=nginx%20du&cb=jQuery110201299900919930772_1722393922512&_=1722393922525', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:58', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2577, '5', '黑名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20d&req=2&bs=1&csor=7&pwd=nginx%20du&cb=jQuery110201299900919930772_1722393922512&_=1722393922525', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:58', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2578, '5', '地址限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20d&req=2&bs=1&csor=7&pwd=nginx%20du&cb=jQuery110201299900919930772_1722393922512&_=1722393922525', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:58', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2579, '5', 'IP限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20d&req=2&bs=1&csor=7&pwd=nginx%20du&cb=jQuery110201299900919930772_1722393922512&_=1722393922525', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:58', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2580, '5', '地址限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20&req=2&bs=1&csor=6&pwd=nginx%20d&cb=jQuery110201299900919930772_1722393922512&_=1722393922526', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:58', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2581, '5', 'IP限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20&req=2&bs=1&csor=6&pwd=nginx%20d&cb=jQuery110201299900919930772_1722393922512&_=1722393922526', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:58', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2582, '5', '白名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20&req=2&bs=1&csor=6&pwd=nginx%20d&cb=jQuery110201299900919930772_1722393922512&_=1722393922526', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:58', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2583, '5', '黑名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20&req=2&bs=1&csor=6&pwd=nginx%20d&cb=jQuery110201299900919930772_1722393922512&_=1722393922526', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:58', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2584, '5', '黑名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20d&req=2&bs=1&csor=7&pwd=nginx%20&cb=jQuery110201299900919930772_1722393922512&_=1722393922527', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:59', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2585, '5', '白名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20d&req=2&bs=1&csor=7&pwd=nginx%20&cb=jQuery110201299900919930772_1722393922512&_=1722393922527', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:59', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2586, '5', 'IP限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20d&req=2&bs=1&csor=7&pwd=nginx%20&cb=jQuery110201299900919930772_1722393922512&_=1722393922527', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:59', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2587, '5', '地址限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20d&req=2&bs=1&csor=7&pwd=nginx%20&cb=jQuery110201299900919930772_1722393922512&_=1722393922527', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:59', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2588, '5', '白名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20du&req=2&bs=1&csor=8&pwd=nginx%20d&cb=jQuery110201299900919930772_1722393922512&_=1722393922528', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:59', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2589, '5', 'IP限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20du&req=2&bs=1&csor=8&pwd=nginx%20d&cb=jQuery110201299900919930772_1722393922512&_=1722393922528', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:59', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2590, '5', '黑名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20du&req=2&bs=1&csor=8&pwd=nginx%20d&cb=jQuery110201299900919930772_1722393922512&_=1722393922528', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:59', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2591, '5', '地址限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20du&req=2&bs=1&csor=8&pwd=nginx%20d&cb=jQuery110201299900919930772_1722393922512&_=1722393922528', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:59', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2592, '5', '黑名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20duo&req=2&bs=1&csor=9&pwd=nginx%20du&cb=jQuery110201299900919930772_1722393922512&_=1722393922529', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:59', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2593, '5', '地址限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20duo&req=2&bs=1&csor=9&pwd=nginx%20du&cb=jQuery110201299900919930772_1722393922512&_=1722393922529', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:59', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2594, '5', 'IP限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20duo&req=2&bs=1&csor=9&pwd=nginx%20du&cb=jQuery110201299900919930772_1722393922512&_=1722393922529', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:59', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2595, '5', '白名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20duo&req=2&bs=1&csor=9&pwd=nginx%20du&cb=jQuery110201299900919930772_1722393922512&_=1722393922529', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:59', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2596, '5', 'IP限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20duo%27g&req=2&bs=1&csor=11&pwd=nginx%20duo&cb=jQuery110201299900919930772_1722393922512&_=1722393922530', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:59', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2597, '5', '地址限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20duo%27g&req=2&bs=1&csor=11&pwd=nginx%20duo&cb=jQuery110201299900919930772_1722393922512&_=1722393922530', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:59', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2598, '5', '白名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20duo%27g&req=2&bs=1&csor=11&pwd=nginx%20duo&cb=jQuery110201299900919930772_1722393922512&_=1722393922530', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:59', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2599, '5', '黑名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20duo%27g&req=2&bs=1&csor=11&pwd=nginx%20duo&cb=jQuery110201299900919930772_1722393922512&_=1722393922530', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:59', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2600, '5', 'IP限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20duo%27ge&req=2&bs=1&csor=12&pwd=nginx%20duo%27g&cb=jQuery110201299900919930772_1722393922512&_=1722393922531', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:59', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2601, '5', '白名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20duo%27ge&req=2&bs=1&csor=12&pwd=nginx%20duo%27g&cb=jQuery110201299900919930772_1722393922512&_=1722393922531', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:59', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2602, '5', '地址限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20duo%27ge&req=2&bs=1&csor=12&pwd=nginx%20duo%27g&cb=jQuery110201299900919930772_1722393922512&_=1722393922531', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:59', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2603, '5', '黑名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20duo%27ge&req=2&bs=1&csor=12&pwd=nginx%20duo%27g&cb=jQuery110201299900919930772_1722393922512&_=1722393922531', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:59', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2604, '5', 'IP限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20%E5%A4%9A%E4%B8%AA&req=2&bs=1&csor=8&pwd=nginx%20duo%27ge&cb=jQuery110201299900919930772_1722393922512&_=1722393922532', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:59', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2605, '5', '黑名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20%E5%A4%9A%E4%B8%AA&req=2&bs=1&csor=8&pwd=nginx%20duo%27ge&cb=jQuery110201299900919930772_1722393922512&_=1722393922532', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:59', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2606, '5', '白名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20%E5%A4%9A%E4%B8%AA&req=2&bs=1&csor=8&pwd=nginx%20duo%27ge&cb=jQuery110201299900919930772_1722393922512&_=1722393922532', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:59', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2607, '5', '地址限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20%E5%A4%9A%E4%B8%AA&req=2&bs=1&csor=8&pwd=nginx%20duo%27ge&cb=jQuery110201299900919930772_1722393922512&_=1722393922532', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:52:59', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2608, '5', 'IP限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20%E5%A4%9A%E4%B8%AAd&req=2&bs=1&csor=9&pwd=nginx%20%E5%A4%9A%E4%B8%AA&cb=jQuery110201299900919930772_1722393922512&_=1722393922533', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:53:00', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2609, '5', '白名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20%E5%A4%9A%E4%B8%AAd&req=2&bs=1&csor=9&pwd=nginx%20%E5%A4%9A%E4%B8%AA&cb=jQuery110201299900919930772_1722393922512&_=1722393922533', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:53:00', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2610, '5', '地址限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20%E5%A4%9A%E4%B8%AAd&req=2&bs=1&csor=9&pwd=nginx%20%E5%A4%9A%E4%B8%AA&cb=jQuery110201299900919930772_1722393922512&_=1722393922533', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:53:00', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2611, '5', '黑名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20%E5%A4%9A%E4%B8%AAd&req=2&bs=1&csor=9&pwd=nginx%20%E5%A4%9A%E4%B8%AA&cb=jQuery110201299900919930772_1722393922512&_=1722393922533', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:53:00', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2612, '5', '黑名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20%E5%A4%9A%E4%B8%AAdi&req=2&bs=1&csor=10&pwd=nginx%20%E5%A4%9A%E4%B8%AAd&cb=jQuery110201299900919930772_1722393922512&_=1722393922534', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:53:00', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2613, '5', 'IP限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20%E5%A4%9A%E4%B8%AAdi&req=2&bs=1&csor=10&pwd=nginx%20%E5%A4%9A%E4%B8%AAd&cb=jQuery110201299900919930772_1722393922512&_=1722393922534', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:53:00', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2614, '5', '白名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20%E5%A4%9A%E4%B8%AAdi&req=2&bs=1&csor=10&pwd=nginx%20%E5%A4%9A%E4%B8%AAd&cb=jQuery110201299900919930772_1722393922512&_=1722393922534', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:53:00', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2615, '5', '地址限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20%E5%A4%9A%E4%B8%AAdi&req=2&bs=1&csor=10&pwd=nginx%20%E5%A4%9A%E4%B8%AAd&cb=jQuery110201299900919930772_1722393922512&_=1722393922534', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:53:00', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2616, '5', '黑名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20%E5%A4%9A%E4%B8%AAdi%27z&req=2&bs=1&csor=12&pwd=nginx%20%E5%A4%9A%E4%B8%AAdi&cb=jQuery110201299900919930772_1722393922512&_=1722393922535', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:53:00', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2617, '5', 'IP限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20%E5%A4%9A%E4%B8%AAdi%27z&req=2&bs=1&csor=12&pwd=nginx%20%E5%A4%9A%E4%B8%AAdi&cb=jQuery110201299900919930772_1722393922512&_=1722393922535', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:53:00', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2618, '5', '白名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20%E5%A4%9A%E4%B8%AAdi%27z&req=2&bs=1&csor=12&pwd=nginx%20%E5%A4%9A%E4%B8%AAdi&cb=jQuery110201299900919930772_1722393922512&_=1722393922535', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:53:00', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2619, '5', '地址限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20%E5%A4%9A%E4%B8%AAdi%27z&req=2&bs=1&csor=12&pwd=nginx%20%E5%A4%9A%E4%B8%AAdi&cb=jQuery110201299900919930772_1722393922512&_=1722393922535', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:53:00', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2620, '5', '地址限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20%E5%A4%9A%E4%B8%AAdi%27zh&req=2&bs=1&csor=13&pwd=nginx%20%E5%A4%9A%E4%B8%AAdi%27z&cb=jQuery110201299900919930772_1722393922512&_=1722393922536', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:53:00', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2621, '5', '白名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20%E5%A4%9A%E4%B8%AAdi%27zh&req=2&bs=1&csor=13&pwd=nginx%20%E5%A4%9A%E4%B8%AAdi%27z&cb=jQuery110201299900919930772_1722393922512&_=1722393922536', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:53:00', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2622, '5', 'IP限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20%E5%A4%9A%E4%B8%AAdi%27zh&req=2&bs=1&csor=13&pwd=nginx%20%E5%A4%9A%E4%B8%AAdi%27z&cb=jQuery110201299900919930772_1722393922512&_=1722393922536', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:53:00', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2623, '5', '黑名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20%E5%A4%9A%E4%B8%AAdi%27zh&req=2&bs=1&csor=13&pwd=nginx%20%E5%A4%9A%E4%B8%AAdi%27z&cb=jQuery110201299900919930772_1722393922512&_=1722393922536', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:53:00', 1722394320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2624, '5', '地址限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20%E5%A4%9A%E4%B8%AAdi%27zhi&req=2&bs=1&csor=14&pwd=nginx%20%E5%A4%9A%E4%B8%AAdi%27zh&cb=jQuery110201299900919930772_1722393922512&_=1722393922537', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:53:00', 1722394380000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2625, '5', '白名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20%E5%A4%9A%E4%B8%AAdi%27zhi&req=2&bs=1&csor=14&pwd=nginx%20%E5%A4%9A%E4%B8%AAdi%27zh&cb=jQuery110201299900919930772_1722393922512&_=1722393922537', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:53:00', 1722394380000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2626, '5', '黑名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20%E5%A4%9A%E4%B8%AAdi%27zhi&req=2&bs=1&csor=14&pwd=nginx%20%E5%A4%9A%E4%B8%AAdi%27zh&cb=jQuery110201299900919930772_1722393922512&_=1722393922537', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:53:00', 1722394380000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2627, '5', 'IP限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60274,60467,60492,60502,60551&wd=nginx%20%E5%A4%9A%E4%B8%AAdi%27zhi&req=2&bs=1&csor=14&pwd=nginx%20%E5%A4%9A%E4%B8%AAdi%27zh&cb=jQuery110201299900919930772_1722393922512&_=1722393922537', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:53:00', 1722394380000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2628, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:53:04', 1722394380000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2629, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:53:04', 1722394380000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2630, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:53:04', 1722394380000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2631, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-31 10:53:04', 1722394380000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2632, '5', '黑名单', '/', 'allow', '局域网', '192.168.110.100', '2024-07-31 11:08:25', 1722395280000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2633, '5', '白名单', '/', 'allow', '局域网', '192.168.110.100', '2024-07-31 11:08:25', 1722395280000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2634, '5', 'IP限流', '/', 'allow', '局域网', '192.168.110.100', '2024-07-31 11:08:25', 1722395280000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2635, '5', '地址限流', '/', 'allow', '局域网', '192.168.110.100', '2024-07-31 11:08:25', 1722395280000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2636, '5', 'IP限流', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.100', '2024-07-31 11:08:36', 1722395280000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2637, '5', '黑名单', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.100', '2024-07-31 11:08:36', 1722395280000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2638, '5', '白名单', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.100', '2024-07-31 11:08:36', 1722395280000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2639, '5', '地址限流', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.100', '2024-07-31 11:08:36', 1722395280000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2640, '5', '白名单', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.100', '2024-07-31 11:08:52', 1722395280000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2641, '5', '黑名单', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.100', '2024-07-31 11:08:52', 1722395280000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2642, '5', 'IP限流', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.100', '2024-07-31 11:08:52', 1722395280000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2643, '5', '地址限流', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.100', '2024-07-31 11:08:52', 1722395280000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2644, '5', '黑名单', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.100', '2024-07-31 11:10:05', 1722395400000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2645, '5', '白名单', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.100', '2024-07-31 11:10:05', 1722395400000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2646, '5', '地址限流', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.100', '2024-07-31 11:10:05', 1722395400000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2647, '5', 'IP限流', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.100', '2024-07-31 11:10:05', 1722395400000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2648, '5', '白名单', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.100', '2024-07-31 11:11:32', 1722395460000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2649, '5', '地址限流', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.100', '2024-07-31 11:11:32', 1722395460000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2650, '5', '黑名单', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.100', '2024-07-31 11:11:32', 1722395460000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2651, '5', 'IP限流', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.100', '2024-07-31 11:11:32', 1722395460000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2652, '5', 'IP限流', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.100', '2024-07-31 11:11:57', 1722395460000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2653, '5', '白名单', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.100', '2024-07-31 11:11:57', 1722395460000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2654, '5', '黑名单', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.100', '2024-07-31 11:11:57', 1722395460000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2655, '5', '地址限流', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.100', '2024-07-31 11:11:57', 1722395460000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2656, '5', '黑名单', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.100', '2024-07-31 11:12:07', 1722395520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2657, '5', '地址限流', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.100', '2024-07-31 11:12:07', 1722395520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2658, '5', '白名单', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.100', '2024-07-31 11:12:07', 1722395520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2659, '5', 'IP限流', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.100', '2024-07-31 11:12:07', 1722395520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2660, '5', '白名单', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.100', '2024-07-31 11:12:29', 1722395520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2661, '5', '黑名单', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.100', '2024-07-31 11:12:29', 1722395520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2662, '5', 'IP限流', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.100', '2024-07-31 11:12:29', 1722395520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2663, '5', '地址限流', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.100', '2024-07-31 11:12:29', 1722395520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2664, '5', 'IP限流', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.100', '2024-07-31 11:15:05', 1722395700000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2665, '5', '黑名单', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.100', '2024-07-31 11:15:05', 1722395700000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2666, '5', '地址限流', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.100', '2024-07-31 11:15:05', 1722395700000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2667, '5', '白名单', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.100', '2024-07-31 11:15:05', 1722395700000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2668, '5', '黑名单', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.100', '2024-07-31 11:22:20', 1722396120000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2669, '5', '白名单', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:22:21', 1722396120000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2670, '5', 'IP限流', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:22:22', 1722396120000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2671, '5', '地址限流', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:22:23', 1722396120000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2672, '5', '黑名单', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:28:43', 1722396480000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2673, '5', '白名单', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:28:53', 1722396480000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2674, '5', 'IP限流', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:28:55', 1722396480000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2675, '5', '地址限流', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:28:55', 1722396480000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2676, '5', '地址限流', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2677, '5', '白名单', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2678, '5', 'IP限流', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2679, '5', '黑名单', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2680, '5', '白名单', '//zxb/css/reset.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2681, '5', '黑名单', '//zxb/css/reset.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2682, '5', 'IP限流', '//zxb/css/reset.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2683, '5', '地址限流', '//zxb/css/reset.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2684, '5', '白名单', '//zxb/js/jquery-1.11.0.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2685, '5', '白名单', '//zxb/css/buttons.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2686, '5', '地址限流', '//zxb/js/jquery-1.11.0.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2687, '5', '黑名单', '//zxb/css/buttons.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2688, '5', 'IP限流', '//zxb/css/buttons.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2689, '5', 'IP限流', '//zxb/js/jquery-1.11.0.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2690, '5', '地址限流', '//zxb/css/buttons.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2691, '5', '黑名单', '//zxb/js/jquery-1.11.0.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2692, '5', 'IP限流', '//zxb/css/gr-animate.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2693, '5', '黑名单', '//zxb/css/gr-animate.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2694, '5', '白名单', '//zxb/js/protect.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2695, '5', 'IP限流', '//zxb/css/swiper.min.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2696, '5', '白名单', '//zxb/css/style.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2697, '5', 'IP限流', '//zxb/css/style.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2698, '5', '白名单', '//zxb/css/gr-animate.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2699, '5', '地址限流', '//zxb/css/gr-animate.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2700, '5', '地址限流', '//zxb/css/swiper.min.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2701, '5', '白名单', '//zxb/css/swiper.min.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2702, '5', '地址限流', '//zxb/css/style.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2703, '5', '黑名单', '//zxb/css/swiper.min.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2704, '5', '黑名单', '//zxb/student/img/qrkjlogo.png', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2705, '5', '白名单', '//zxb/student/img/qrkjlogo.png', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2706, '5', 'IP限流', '//zxb/student/img/qrkjlogo.png', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2707, '5', '地址限流', '//zxb/student/img/qrkjlogo.png', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2708, '5', '黑名单', '//zxb/js/protect.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2709, '5', '地址限流', '//zxb/js/protect.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2710, '5', 'IP限流', '//zxb/js/protect.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2711, '5', '黑名单', '//zxb/css/style.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2712, '5', '黑名单', '//zxb/student/img/trtysj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2713, '5', '地址限流', '//zxb/student/img/trtysj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2714, '5', 'IP限流', '//zxb/student/img/trtysj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2715, '5', '白名单', '//zxb/student/img/trtysj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2716, '5', '地址限流', '//zxb/student/img/trtzhcs.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2717, '5', '黑名单', '//zxb/student/img/trtzhcs.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2718, '5', 'IP限流', '//zxb/student/img/trtzhcs.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2719, '5', '白名单', '//zxb/student/img/trtzhcs.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2720, '5', 'IP限流', '//zxb/student/img/kltrj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2721, '5', '白名单', '//zxb/student/img/kltrj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2722, '5', '黑名单', '//zxb/student/img/kltrj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2723, '5', '地址限流', '//zxb/student/img/kltrj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2724, '5', '白名单', '//zxb/student/img/ldsc.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2725, '5', '地址限流', '//zxb/student/img/ldsc.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2726, '5', 'IP限流', '//zxb/student/img/ldsc.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2727, '5', '黑名单', '//zxb/student/img/ldsc.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2728, '5', '白名单', '//zxb/js/jquery.countTo.min.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2729, '5', '黑名单', '//zxb/js/jquery.countTo.min.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2730, '5', 'IP限流', '//zxb/js/jquery.countTo.min.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2731, '5', '地址限流', '//zxb/js/jquery.countTo.min.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2732, '5', 'IP限流', '//zxb/js/waypoints.min.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2733, '5', '白名单', '//zxb/js/waypoints.min.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2734, '5', '黑名单', '//zxb/js/waypoints.min.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2735, '5', '地址限流', '//zxb/js/waypoints.min.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2736, '5', '白名单', '//zxb/js/jishuqi.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2737, '5', '地址限流', '//zxb/js/jishuqi.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2738, '5', 'IP限流', '//zxb/js/jishuqi.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2739, '5', '黑名单', '//zxb/js/jishuqi.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2740, '5', '地址限流', '//zxb/js/plugin.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2741, '5', '黑名单', '//zxb/js/plugin.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2742, '5', '白名单', '//zxb/js/plugin.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2743, '5', 'IP限流', '//zxb/js/plugin.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2744, '5', 'IP限流', '//zxb/js/index.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2745, '5', '白名单', '//zxb/js/index.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2746, '5', '地址限流', '//zxb/js/index.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2747, '5', '黑名单', '//zxb/js/index.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2748, '5', '白名单', '//zxb/js/page.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2749, '5', '黑名单', '//zxb/js/page.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2750, '5', 'IP限流', '//zxb/js/page.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2751, '5', '地址限流', '//zxb/js/page.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2752, '5', 'IP限流', '//zxb/js/box.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2753, '5', '白名单', '//zxb/js/box.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2754, '5', '地址限流', '//zxb/js/box.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2755, '5', '黑名单', '//zxb/js/box.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2756, '5', '黑名单', '//zxb/js/phone.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2757, '5', '白名单', '//zxb/js/phone.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2758, '5', '地址限流', '//zxb/js/phone.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2759, '5', 'IP限流', '//zxb/js/phone.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2760, '5', '地址限流', '//zxb/js/plugin2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2761, '5', '黑名单', '//zxb/js/plugin2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2762, '5', '白名单', '//zxb/js/plugin2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2763, '5', 'IP限流', '//zxb/js/plugin2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2764, '5', '白名单', '//zxb/js/index2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2765, '5', '黑名单', '//zxb/js/index2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2766, '5', 'IP限流', '//zxb/js/index2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2767, '5', '地址限流', '//zxb/js/index2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2768, '5', '地址限流', '//zxb/js/page2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2769, '5', 'IP限流', '//zxb/js/page2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2770, '5', '白名单', '//zxb/js/page2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2771, '5', '黑名单', '//zxb/js/page2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:31', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2772, '5', '白名单', '//zxb/js/box2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:32', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2773, '5', '黑名单', '//zxb/js/box2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:32', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2774, '5', '地址限流', '//zxb/js/box2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:32', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2775, '5', 'IP限流', '//zxb/js/box2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:32', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2776, '5', '黑名单', '//zxb/js/phone2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:32', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2777, '5', '白名单', '//zxb/js/phone2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:32', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2778, '5', '地址限流', '//zxb/js/phone2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:32', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2779, '5', 'IP限流', '//zxb/js/phone2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:32', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2780, '5', 'IP限流', '//zxb/js/wow.min2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:32', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2781, '5', '黑名单', '//zxb/js/wow.min2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:32', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2782, '5', '地址限流', '//zxb/js/wow.min2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:32', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2783, '5', '白名单', '//zxb/js/wow.min2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:32', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2784, '5', '黑名单', '//zxb/js/SmoothScroll2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:32', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2785, '5', 'IP限流', '//zxb/js/SmoothScroll2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:32', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2786, '5', '地址限流', '//zxb/js/SmoothScroll2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:32', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2787, '5', '白名单', '//zxb/js/SmoothScroll2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:32', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2788, '5', '黑名单', '//zxb/js/jquery.SuperSlide.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:32', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2789, '5', 'IP限流', '//zxb/js/jquery.SuperSlide.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:32', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2790, '5', '白名单', '//zxb/js/jquery.SuperSlide.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:32', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2791, '5', '地址限流', '//zxb/js/jquery.SuperSlide.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:34:32', 1722396840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2792, '5', '地址限流', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2793, '5', 'IP限流', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2794, '5', '黑名单', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2795, '5', '白名单', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2796, '5', 'IP限流', '//zxb/css/reset.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2797, '5', '黑名单', '//zxb/css/reset.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2798, '5', '地址限流', '//zxb/css/reset.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2799, '5', '白名单', '//zxb/css/reset.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2800, '5', '地址限流', '//zxb/css/gr-animate.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2801, '5', 'IP限流', '//zxb/css/buttons.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2802, '5', '白名单', '//zxb/css/buttons.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2803, '5', '白名单', '//zxb/css/gr-animate.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2804, '5', '地址限流', '//zxb/css/buttons.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2805, '5', '黑名单', '//zxb/css/buttons.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2806, '5', '黑名单', '//zxb/css/gr-animate.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2807, '5', 'IP限流', '//zxb/css/gr-animate.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2808, '5', '白名单', '//zxb/css/style.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2809, '5', '黑名单', '//zxb/js/jquery-1.11.0.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2810, '5', '白名单', '//zxb/js/jquery-1.11.0.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2811, '5', '地址限流', '//zxb/css/style.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2812, '5', '黑名单', '//zxb/css/swiper.min.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2813, '5', 'IP限流', '//zxb/css/style.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2814, '5', '地址限流', '//zxb/js/jquery-1.11.0.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2815, '5', 'IP限流', '//zxb/js/jquery-1.11.0.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2816, '5', '白名单', '//zxb/css/swiper.min.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2817, '5', '黑名单', '//zxb/css/style.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2818, '5', 'IP限流', '//zxb/css/swiper.min.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2819, '5', '地址限流', '//zxb/css/swiper.min.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2820, '5', 'IP限流', '//zxb/js/protect.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2821, '5', '黑名单', '//zxb/student/img/qrkjlogo.png', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2822, '5', '黑名单', '//zxb/js/protect.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2823, '5', 'IP限流', '//zxb/student/img/trtysj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2824, '5', '地址限流', '//zxb/student/img/qrkjlogo.png', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2825, '5', '地址限流', '//zxb/student/img/trtysj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2826, '5', '白名单', '//zxb/student/img/qrkjlogo.png', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2827, '5', '白名单', '//zxb/student/img/trtysj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2828, '5', '白名单', '//zxb/js/protect.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2829, '5', '地址限流', '//zxb/js/protect.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2830, '5', '黑名单', '//zxb/student/img/trtysj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2831, '5', 'IP限流', '//zxb/student/img/qrkjlogo.png', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2832, '5', '黑名单', '//zxb/student/img/trtzhcs.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2833, '5', 'IP限流', '//zxb/student/img/kltrj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2834, '5', '地址限流', '//zxb/student/img/kltrj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2835, '5', '地址限流', '//zxb/student/img/ldsc.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2836, '5', 'IP限流', '//zxb/student/img/ldsc.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2837, '5', '黑名单', '//zxb/student/img/kltrj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2838, '5', 'IP限流', '//zxb/student/img/trtzhcs.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2839, '5', '白名单', '//zxb/student/img/trtzhcs.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2840, '5', '白名单', '//zxb/student/img/ldsc.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2841, '5', '黑名单', '//zxb/student/img/ldsc.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2842, '5', '地址限流', '//zxb/student/img/trtzhcs.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2843, '5', '白名单', '//zxb/student/img/kltrj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2844, '5', '黑名单', '//zxb/js/jishuqi.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2845, '5', '白名单', '//zxb/js/jishuqi.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2846, '5', 'IP限流', '//zxb/js/jquery.countTo.min.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2847, '5', '黑名单', '//zxb/js/jquery.countTo.min.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2848, '5', '地址限流', '//zxb/js/jishuqi.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2849, '5', 'IP限流', '//zxb/js/jishuqi.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2850, '5', '白名单', '//zxb/js/waypoints.min.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2851, '5', '地址限流', '//zxb/js/waypoints.min.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2852, '5', '白名单', '//zxb/js/jquery.countTo.min.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2853, '5', '地址限流', '//zxb/js/jquery.countTo.min.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2854, '5', 'IP限流', '//zxb/js/waypoints.min.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2855, '5', '黑名单', '//zxb/js/waypoints.min.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2856, '5', '地址限流', '//zxb/js/plugin.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2857, '5', '地址限流', '//zxb/js/index.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2858, '5', '黑名单', '//zxb/js/plugin.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2859, '5', 'IP限流', '//zxb/js/plugin.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2860, '5', '白名单', '//zxb/js/page.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2861, '5', 'IP限流', '//zxb/js/index.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2862, '5', '白名单', '//zxb/js/index.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2863, '5', '白名单', '//zxb/js/plugin.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2864, '5', '黑名单', '//zxb/js/page.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2865, '5', '地址限流', '//zxb/js/page.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2866, '5', 'IP限流', '//zxb/js/page.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2867, '5', '黑名单', '//zxb/js/index.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2868, '5', '地址限流', '//zxb/js/phone.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2869, '5', '黑名单', '//zxb/js/box.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2870, '5', 'IP限流', '//zxb/js/box.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2871, '5', '白名单', '//zxb/js/phone.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2872, '5', '黑名单', '//zxb/js/phone.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2873, '5', '白名单', '//zxb/js/box.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2874, '5', '地址限流', '//zxb/js/box.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2875, '5', 'IP限流', '//zxb/js/phone.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2876, '5', '白名单', '//zxb/js/index2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2877, '5', 'IP限流', '//zxb/js/index2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2878, '5', '地址限流', '//zxb/js/plugin2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2879, '5', '白名单', '//zxb/js/plugin2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2880, '5', '黑名单', '//zxb/js/index2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2881, '5', '黑名单', '//zxb/js/plugin2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2882, '5', 'IP限流', '//zxb/js/plugin2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2883, '5', '地址限流', '//zxb/js/index2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2884, '5', '白名单', '//zxb/js/page2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2885, '5', '黑名单', '//zxb/js/phone2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2886, '5', 'IP限流', '//zxb/js/box2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2887, '5', 'IP限流', '//zxb/js/page2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2888, '5', '黑名单', '//zxb/js/page2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2889, '5', '地址限流', '//zxb/js/phone2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2890, '5', '白名单', '//zxb/js/box2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2891, '5', '地址限流', '//zxb/js/box2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2892, '5', '黑名单', '//zxb/js/box2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2893, '5', 'IP限流', '//zxb/js/phone2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2894, '5', '地址限流', '//zxb/js/page2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2895, '5', '白名单', '//zxb/js/phone2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2896, '5', '地址限流', '//zxb/js/wow.min2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2897, '5', '白名单', '//zxb/js/wow.min2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2898, '5', '地址限流', '//zxb/js/jquery.SuperSlide.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2899, '5', '黑名单', '//zxb/js/SmoothScroll2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2900, '5', 'IP限流', '//zxb/js/jquery.SuperSlide.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2901, '5', '白名单', '//zxb/js/SmoothScroll2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2902, '5', '黑名单', '//zxb/js/wow.min2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2903, '5', 'IP限流', '//zxb/js/SmoothScroll2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2904, '5', '黑名单', '//zxb/js/jquery.SuperSlide.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2905, '5', 'IP限流', '//zxb/js/wow.min2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2906, '5', '白名单', '//zxb/js/jquery.SuperSlide.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2907, '5', '地址限流', '//zxb/js/SmoothScroll2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2908, '5', '白名单', '//zxb/student/img/ldsc2.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2909, '5', '黑名单', '//zxb/student/img/ldsc2.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2910, '5', '地址限流', '//zxb/student/img/zhzw.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2911, '5', '地址限流', '//zxb/student/img/zhwl.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2912, '5', 'IP限流', '//zxb/student/img/ldsc2.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2913, '5', '黑名单', '//zxb/student/img/zhwl.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2914, '5', '白名单', '//zxb/student/img/zhwl.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2915, '5', '黑名单', '//zxb/student/img/zhzw.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2916, '5', '白名单', '//zxb/student/img/zhzw.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2917, '5', 'IP限流', '//zxb/student/img/zhwl.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2918, '5', '地址限流', '//zxb/student/img/ldsc2.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2919, '5', 'IP限流', '//zxb/student/img/zhzw.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2920, '5', '白名单', '//zxb/student/img/zhcs.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2921, '5', 'IP限流', '//zxb/student/img/zhcs.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2922, '5', '黑名单', '//zxb/student/img/zhcs.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2923, '5', '地址限流', '//zxb/student/img/zhcs.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2924, '5', '黑名单', '//zxb/student/img/rjzd.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2925, '5', '地址限流', '//zxb/student/img/rjzd.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2926, '5', '白名单', '//zxb/student/img/zhyj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2927, '5', 'IP限流', '//zxb/student/img/rjzd.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2928, '5', '地址限流', '//zxb/student/img/zhyj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2929, '5', '白名单', '//zxb/student/img/rjzd.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2930, '5', '黑名单', '//zxb/student/img/zhyj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2931, '5', 'IP限流', '//zxb/student/img/zhyj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:35:26', 1722396900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2932, '5', '地址限流', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2933, '5', '白名单', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2934, '5', '黑名单', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2935, '5', 'IP限流', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2936, '5', '地址限流', '//zxb/css/buttons.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2937, '5', '地址限流', '//zxb/css/reset.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2938, '5', '白名单', '//zxb/css/gr-animate.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2939, '5', 'IP限流', '//zxb/css/buttons.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2940, '5', 'IP限流', '//zxb/css/gr-animate.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2941, '5', '白名单', '//zxb/css/buttons.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2942, '5', '黑名单', '//zxb/css/buttons.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2943, '5', '地址限流', '//zxb/css/gr-animate.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2944, '5', '黑名单', '//zxb/css/reset.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2945, '5', '黑名单', '//zxb/css/gr-animate.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2946, '5', '白名单', '//zxb/css/reset.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2947, '5', '白名单', '//zxb/css/swiper.min.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2948, '5', '地址限流', '//zxb/js/jquery-1.11.0.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2949, '5', 'IP限流', '//zxb/css/reset.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2950, '5', 'IP限流', '//zxb/js/jquery-1.11.0.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2951, '5', '黑名单', '//zxb/css/swiper.min.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2952, '5', '地址限流', '//zxb/css/swiper.min.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2953, '5', '黑名单', '//zxb/css/style.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2954, '5', 'IP限流', '//zxb/css/style.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2955, '5', 'IP限流', '//zxb/css/swiper.min.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2956, '5', '地址限流', '//zxb/css/style.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2957, '5', '白名单', '//zxb/css/style.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2958, '5', '黑名单', '//zxb/js/jquery-1.11.0.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2959, '5', '白名单', '//zxb/js/jquery-1.11.0.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2960, '5', '白名单', '//zxb/js/protect.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2961, '5', '黑名单', '//zxb/js/protect.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2962, '5', '地址限流', '//zxb/js/protect.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2963, '5', 'IP限流', '//zxb/js/protect.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2964, '5', '地址限流', '//zxb/student/img/trtysj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2965, '5', '黑名单', '//zxb/student/img/trtysj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2966, '5', '地址限流', '//zxb/student/img/qrkjlogo.png', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2967, '5', '白名单', '//zxb/student/img/trtysj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2968, '5', 'IP限流', '//zxb/student/img/trtysj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2969, '5', '黑名单', '//zxb/student/img/qrkjlogo.png', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2970, '5', 'IP限流', '//zxb/student/img/qrkjlogo.png', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2971, '5', '白名单', '//zxb/student/img/qrkjlogo.png', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2972, '5', '黑名单', '//zxb/student/img/kltrj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2973, '5', 'IP限流', '//zxb/js/jishuqi.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2974, '5', '白名单', '//zxb/student/img/trtzhcs.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2975, '5', '地址限流', '//zxb/js/jishuqi.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2976, '5', '地址限流', '//zxb/js/jquery.countTo.min.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2977, '5', '黑名单', '//zxb/js/jishuqi.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2978, '5', '黑名单', '//zxb/js/waypoints.min.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2979, '5', '黑名单', '//zxb/js/jquery.countTo.min.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2980, '5', '白名单', '//zxb/student/img/ldsc.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2981, '5', 'IP限流', '//zxb/js/jquery.countTo.min.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2982, '5', '黑名单', '//zxb/student/img/ldsc.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2983, '5', '地址限流', '//zxb/student/img/kltrj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2984, '5', '白名单', '//zxb/js/waypoints.min.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2985, '5', '黑名单', '//zxb/student/img/trtzhcs.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2986, '5', '地址限流', '//zxb/js/waypoints.min.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2987, '5', 'IP限流', '//zxb/student/img/trtzhcs.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2988, '5', '白名单', '//zxb/student/img/kltrj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2989, '5', '地址限流', '//zxb/student/img/trtzhcs.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2990, '5', '白名单', '//zxb/js/jquery.countTo.min.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2991, '5', 'IP限流', '//zxb/student/img/ldsc.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2992, '5', 'IP限流', '//zxb/student/img/kltrj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2993, '5', '地址限流', '//zxb/student/img/ldsc.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2994, '5', 'IP限流', '//zxb/js/waypoints.min.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2995, '5', '白名单', '//zxb/js/jishuqi.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2996, '5', '白名单', '//zxb/js/index.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2997, '5', '黑名单', '//zxb/js/index.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2998, '5', 'IP限流', '//zxb/js/plugin.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2999, '5', '黑名单', '//zxb/js/plugin.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3000, '5', '地址限流', '//zxb/js/index.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3001, '5', '白名单', '//zxb/js/page.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3002, '5', '地址限流', '//zxb/js/plugin.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3003, '5', 'IP限流', '//zxb/js/page.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3004, '5', '白名单', '//zxb/js/box.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3005, '5', '黑名单', '//zxb/js/phone.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3006, '5', '白名单', '//zxb/js/plugin.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3007, '5', '白名单', '//zxb/js/phone.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3008, '5', '地址限流', '//zxb/js/page.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3009, '5', '地址限流', '//zxb/js/box.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3010, '5', '黑名单', '//zxb/js/page.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3011, '5', 'IP限流', '//zxb/js/phone.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3012, '5', '黑名单', '//zxb/js/box.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3013, '5', 'IP限流', '//zxb/js/index.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3014, '5', 'IP限流', '//zxb/js/box.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3015, '5', '地址限流', '//zxb/js/phone.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3016, '5', 'IP限流', '//zxb/js/index2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3017, '5', '白名单', '//zxb/js/index2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3018, '5', '白名单', '//zxb/js/page2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3019, '5', '黑名单', '//zxb/js/box2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3020, '5', '地址限流', '//zxb/js/index2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3021, '5', '黑名单', '//zxb/js/page2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3022, '5', '白名单', '//zxb/js/plugin2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3023, '5', '地址限流', '//zxb/js/plugin2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3024, '5', '黑名单', '//zxb/js/index2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3025, '5', '地址限流', '//zxb/js/page2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3026, '5', 'IP限流', '//zxb/js/box2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3027, '5', '地址限流', '//zxb/js/box2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3028, '5', '白名单', '//zxb/js/box2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3029, '5', '地址限流', '//zxb/js/phone2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3030, '5', 'IP限流', '//zxb/js/plugin2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3031, '5', '黑名单', '//zxb/js/plugin2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3032, '5', 'IP限流', '//zxb/js/phone2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3033, '5', '黑名单', '//zxb/js/phone2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3034, '5', '白名单', '//zxb/js/phone2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3035, '5', 'IP限流', '//zxb/js/page2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3036, '5', '黑名单', '//zxb/js/jquery.SuperSlide.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3037, '5', '白名单', '//zxb/js/jquery.SuperSlide.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3038, '5', '地址限流', '//zxb/js/SmoothScroll2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3039, '5', '地址限流', '//zxb/js/jquery.SuperSlide.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3040, '5', '白名单', '//zxb/js/SmoothScroll2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3041, '5', '白名单', '//zxb/js/wow.min2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3042, '5', 'IP限流', '//zxb/js/jquery.SuperSlide.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3043, '5', '黑名单', '//zxb/js/SmoothScroll2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3044, '5', '白名单', '//zxb/student/img/ldsc2.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3045, '5', '黑名单', '//zxb/js/wow.min2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3046, '5', 'IP限流', '//zxb/js/SmoothScroll2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3047, '5', '地址限流', '//zxb/js/wow.min2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3048, '5', '地址限流', '//zxb/student/img/ldsc2.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3049, '5', 'IP限流', '//zxb/student/img/ldsc2.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3050, '5', 'IP限流', '//zxb/student/img/zhzw.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3051, '5', '地址限流', '//zxb/student/img/zhwl.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3052, '5', '黑名单', '//zxb/student/img/zhwl.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3053, '5', 'IP限流', '//zxb/student/img/zhwl.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3054, '5', '黑名单', '//zxb/student/img/ldsc2.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3055, '5', 'IP限流', '//zxb/js/wow.min2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3056, '5', '地址限流', '//zxb/student/img/zhzw.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3057, '5', '白名单', '//zxb/student/img/zhzw.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3058, '5', '白名单', '//zxb/student/img/zhwl.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3059, '5', '黑名单', '//zxb/student/img/zhzw.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:03', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3060, '5', 'IP限流', '//zxb/student/img/zhcs.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:04', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3061, '5', '白名单', '//zxb/student/img/zhyj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:04', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3062, '5', '地址限流', '//zxb/student/img/zhcs.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:04', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3063, '5', '黑名单', '//zxb/student/img/zhcs.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:04', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3064, '5', '白名单', '//zxb/student/img/zhcs.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:04', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3065, '5', '黑名单', '//zxb/student/img/zhyj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:04', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3066, '5', '地址限流', '//zxb/student/img/zhyj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:04', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3067, '5', '白名单', '//zxb/student/img/rjzd.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:04', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3068, '5', '地址限流', '//zxb/student/img/rjzd.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:04', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3069, '5', 'IP限流', '//zxb/student/img/rjzd.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:04', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3070, '5', 'IP限流', '//zxb/student/img/zhyj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:04', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3071, '5', '黑名单', '//zxb/student/img/rjzd.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:04', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3072, '5', '黑名单', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:12', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3073, '5', '白名单', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:22', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3074, '5', 'IP限流', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:22', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3075, '5', '地址限流', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:22', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3076, '5', 'IP限流', '//zxb/css/gr-animate.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3077, '5', '黑名单', '//zxb/css/gr-animate.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3078, '5', '白名单', '//zxb/css/gr-animate.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3079, '5', '地址限流', '//zxb/css/reset.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3080, '5', 'IP限流', '//zxb/css/reset.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3081, '5', '地址限流', '//zxb/css/gr-animate.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3082, '5', '白名单', '//zxb/css/reset.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3083, '5', 'IP限流', '//zxb/css/buttons.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3084, '5', '地址限流', '//zxb/css/buttons.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3085, '5', '黑名单', '//zxb/css/reset.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3086, '5', '白名单', '//zxb/css/buttons.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3087, '5', '黑名单', '//zxb/css/buttons.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3088, '5', '白名单', '//zxb/css/swiper.min.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3089, '5', 'IP限流', '//zxb/css/swiper.min.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3090, '5', '黑名单', '//zxb/css/swiper.min.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3091, '5', '地址限流', '//zxb/css/swiper.min.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3092, '5', 'IP限流', '//zxb/css/style.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3093, '5', '白名单', '//zxb/js/jquery-1.11.0.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3094, '5', '白名单', '//zxb/css/style.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3095, '5', '黑名单', '//zxb/css/style.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3096, '5', '地址限流', '//zxb/js/jquery-1.11.0.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3097, '5', '地址限流', '//zxb/css/style.css', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3098, '5', '黑名单', '//zxb/js/jquery-1.11.0.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3099, '5', 'IP限流', '//zxb/js/jquery-1.11.0.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3100, '5', '地址限流', '//zxb/student/img/qrkjlogo.png', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3101, '5', '地址限流', '//zxb/js/protect.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3102, '5', '白名单', '//zxb/student/img/qrkjlogo.png', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3103, '5', '黑名单', '//zxb/js/protect.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3104, '5', '白名单', '//zxb/student/img/trtysj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3105, '5', '地址限流', '//zxb/student/img/trtysj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3106, '5', '黑名单', '//zxb/student/img/qrkjlogo.png', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3107, '5', 'IP限流', '//zxb/student/img/trtysj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3108, '5', 'IP限流', '//zxb/js/protect.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3109, '5', '白名单', '//zxb/js/protect.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3110, '5', '黑名单', '//zxb/student/img/trtysj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3111, '5', 'IP限流', '//zxb/student/img/qrkjlogo.png', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3112, '5', 'IP限流', '//zxb/student/img/ldsc.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3113, '5', '黑名单', '//zxb/student/img/ldsc.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3114, '5', '地址限流', '//zxb/student/img/ldsc.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3115, '5', '地址限流', '//zxb/student/img/trtzhcs.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3116, '5', 'IP限流', '//zxb/student/img/trtzhcs.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3117, '5', '白名单', '//zxb/student/img/ldsc.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3118, '5', '黑名单', '//zxb/student/img/kltrj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3119, '5', '白名单', '//zxb/student/img/kltrj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3120, '5', '黑名单', '//zxb/student/img/trtzhcs.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3121, '5', 'IP限流', '//zxb/student/img/kltrj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3122, '5', '白名单', '//zxb/student/img/trtzhcs.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3123, '5', '地址限流', '//zxb/student/img/kltrj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3124, '5', '白名单', '//zxb/js/jquery.countTo.min.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3125, '5', 'IP限流', '//zxb/js/jishuqi.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3126, '5', '地址限流', '//zxb/js/waypoints.min.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3127, '5', '黑名单', '//zxb/js/jishuqi.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3128, '5', '黑名单', '//zxb/js/jquery.countTo.min.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3129, '5', '白名单', '//zxb/js/jishuqi.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3130, '5', '黑名单', '//zxb/js/waypoints.min.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3131, '5', 'IP限流', '//zxb/js/waypoints.min.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3132, '5', '白名单', '//zxb/js/waypoints.min.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3133, '5', 'IP限流', '//zxb/js/jquery.countTo.min.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3134, '5', '地址限流', '//zxb/js/jishuqi.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3135, '5', '地址限流', '//zxb/js/jquery.countTo.min.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3136, '5', '地址限流', '//zxb/js/index.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3137, '5', '黑名单', '//zxb/js/index.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3138, '5', 'IP限流', '//zxb/js/page.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3139, '5', '白名单', '//zxb/js/index.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3140, '5', '地址限流', '//zxb/js/page.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3141, '5', '白名单', '//zxb/js/plugin.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3142, '5', 'IP限流', '//zxb/js/index.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3143, '5', '地址限流', '//zxb/js/plugin.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3144, '5', '黑名单', '//zxb/js/plugin.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3145, '5', 'IP限流', '//zxb/js/plugin.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3146, '5', '黑名单', '//zxb/js/page.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3147, '5', '白名单', '//zxb/js/page.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3148, '5', '白名单', '//zxb/js/box.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3149, '5', 'IP限流', '//zxb/js/phone.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3150, '5', '黑名单', '//zxb/js/box.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3151, '5', '地址限流', '//zxb/js/phone.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3152, '5', '黑名单', '//zxb/js/phone.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3153, '5', '地址限流', '//zxb/js/box.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3154, '5', 'IP限流', '//zxb/js/box.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3155, '5', '白名单', '//zxb/js/phone.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3156, '5', '黑名单', '//zxb/js/index2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3157, '5', '白名单', '//zxb/js/index2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3158, '5', '黑名单', '//zxb/js/plugin2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3159, '5', 'IP限流', '//zxb/js/plugin2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3160, '5', 'IP限流', '//zxb/js/index2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3161, '5', '白名单', '//zxb/js/plugin2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3162, '5', '地址限流', '//zxb/js/plugin2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3163, '5', '地址限流', '//zxb/js/index2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3164, '5', '白名单', '//zxb/js/box2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3165, '5', '黑名单', '//zxb/js/phone2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3166, '5', '白名单', '//zxb/js/page2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3167, '5', '地址限流', '//zxb/js/page2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3168, '5', '黑名单', '//zxb/js/page2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3169, '5', '地址限流', '//zxb/js/phone2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3170, '5', '白名单', '//zxb/js/phone2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3171, '5', '黑名单', '//zxb/js/box2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3172, '5', 'IP限流', '//zxb/js/box2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3173, '5', 'IP限流', '//zxb/js/page2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3174, '5', 'IP限流', '//zxb/js/phone2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3175, '5', '地址限流', '//zxb/js/box2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3176, '5', '地址限流', '//zxb/js/jquery.SuperSlide.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3177, '5', '地址限流', '//zxb/js/wow.min2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3178, '5', '黑名单', '//zxb/js/jquery.SuperSlide.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3179, '5', '黑名单', '//zxb/js/SmoothScroll2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3180, '5', 'IP限流', '//zxb/js/wow.min2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3181, '5', '黑名单', '//zxb/js/wow.min2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3182, '5', '白名单', '//zxb/js/wow.min2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3183, '5', 'IP限流', '//zxb/js/SmoothScroll2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3184, '5', '白名单', '//zxb/js/SmoothScroll2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3185, '5', 'IP限流', '//zxb/js/jquery.SuperSlide.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3186, '5', '地址限流', '//zxb/js/SmoothScroll2.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3187, '5', '白名单', '//zxb/js/jquery.SuperSlide.js', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3188, '5', 'IP限流', '//zxb/student/img/zhzw.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3189, '5', '黑名单', '//zxb/student/img/zhwl.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3190, '5', 'IP限流', '//zxb/student/img/ldsc2.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3191, '5', '白名单', '//zxb/student/img/ldsc2.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3192, '5', 'IP限流', '//zxb/student/img/zhwl.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3193, '5', '地址限流', '//zxb/student/img/zhwl.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3194, '5', '黑名单', '//zxb/student/img/ldsc2.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3195, '5', '黑名单', '//zxb/student/img/zhzw.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3196, '5', '白名单', '//zxb/student/img/zhwl.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3197, '5', '白名单', '//zxb/student/img/zhzw.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3198, '5', '地址限流', '//zxb/student/img/ldsc2.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3199, '5', '地址限流', '//zxb/student/img/zhzw.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3200, '5', '白名单', '//zxb/student/img/zhcs.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3201, '5', '地址限流', '//zxb/student/img/zhcs.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3202, '5', 'IP限流', '//zxb/student/img/zhcs.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3203, '5', '黑名单', '//zxb/student/img/zhcs.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3204, '5', '黑名单', '//zxb/student/img/zhyj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3205, '5', '地址限流', '//zxb/student/img/zhyj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3206, '5', '白名单', '//zxb/student/img/zhyj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3207, '5', 'IP限流', '//zxb/student/img/zhyj.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3208, '5', 'IP限流', '//zxb/student/img/rjzd.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3209, '5', '白名单', '//zxb/student/img/rjzd.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3210, '5', '地址限流', '//zxb/student/img/rjzd.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3211, '5', '黑名单', '//zxb/student/img/rjzd.jpg', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:34', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3212, '5', '黑名单', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:47', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3213, '5', '白名单', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:47', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3214, '5', '地址限流', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:47', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3215, '5', 'IP限流', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:36:47', 1722396960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3216, '5', '白名单', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:37:04', 1722397020000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3217, '5', '地址限流', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:37:04', 1722397020000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3218, '5', 'IP限流', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:37:04', 1722397020000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3219, '5', '黑名单', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:37:04', 1722397020000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3220, '5', 'IP限流', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:37:26', 1722397020000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3221, '5', '白名单', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:37:26', 1722397020000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3222, '5', '黑名单', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:37:26', 1722397020000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3223, '5', '地址限流', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:37:26', 1722397020000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3224, '5', '地址限流', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:37:37', 1722397020000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3225, '5', '白名单', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:37:37', 1722397020000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3226, '5', 'IP限流', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:37:37', 1722397020000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3227, '5', '黑名单', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:37:37', 1722397020000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3228, '5', 'IP限流', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:37:45', 1722397020000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3229, '5', '白名单', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:37:45', 1722397020000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3230, '5', '地址限流', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:37:45', 1722397020000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3231, '5', '黑名单', '//baidu/s?wd=1', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:37:45', 1722397020000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3232, '5', '白名单', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:37:58', 1722397020000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3233, '5', 'IP限流', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:37:58', 1722397020000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3234, '5', '地址限流', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:37:58', 1722397020000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3235, '5', '黑名单', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:37:58', 1722397020000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3236, '5', 'IP限流', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:38:14', 1722397080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3237, '5', '白名单', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:38:14', 1722397080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3238, '5', '地址限流', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:38:14', 1722397080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3239, '5', '黑名单', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:38:14', 1722397080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3240, '5', '黑名单', '//zxb/school/', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:38:28', 1722397080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3241, '5', 'IP限流', '//zxb/school/', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:38:28', 1722397080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3242, '5', '地址限流', '//zxb/school/', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:38:28', 1722397080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3243, '5', '白名单', '//zxb/school/', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:38:28', 1722397080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3244, '5', 'IP限流', '/test1/zxb/school/', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:02', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3245, '5', '地址限流', '/test1/zxb/school/', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:02', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3246, '5', '黑名单', '/test1/zxb/school/', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:02', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3247, '5', '白名单', '/test1/zxb/school/', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:02', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3248, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:04', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3249, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:04', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3250, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:04', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3251, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:04', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3252, '5', '白名单', '/zxb/school/', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:08', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3253, '5', 'IP限流', '/zxb/school/', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:08', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3254, '5', '地址限流', '/zxb/school/', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:08', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3255, '5', '黑名单', '/zxb/school/', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:08', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3256, '5', 'IP限流', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:08', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3257, '5', '白名单', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:08', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3258, '5', 'IP限流', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:08', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3259, '5', '地址限流', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:08', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3260, '5', '地址限流', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:08', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3261, '5', '白名单', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:08', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3262, '5', '黑名单', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:08', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3263, '5', '白名单', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:08', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3264, '5', '地址限流', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:08', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3265, '5', '黑名单', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:08', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3266, '5', '黑名单', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:08', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3267, '5', 'IP限流', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:08', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3268, '5', '黑名单', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:08', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3269, '5', '地址限流', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:08', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3270, '5', 'IP限流', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:08', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3271, '5', '白名单', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:08', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3272, '5', 'IP限流', '/zxb/school/bundle.tracing.min.js.map', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:08', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3273, '5', '黑名单', '/zxb/school/bundle.tracing.min.js.map', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:08', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3274, '5', '地址限流', '/zxb/school/bundle.tracing.min.js.map', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:08', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3275, '5', '白名单', '/zxb/school/bundle.tracing.min.js.map', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:08', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3276, '5', '白名单', '/zxb/school/assets/components.b0153bba.css', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:09', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3277, '5', 'IP限流', '/zxb/school/assets/antd.5ee291b2.css', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:09', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3278, '5', '地址限流', '/zxb/school/assets/antd.5ee291b2.css', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:09', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3279, '5', '地址限流', '/zxb/school/assets/components.b0153bba.css', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:09', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3280, '5', 'IP限流', '/zxb/school/assets/components.b0153bba.css', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:09', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3281, '5', '黑名单', '/zxb/school/assets/components.b0153bba.css', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:09', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3282, '5', '黑名单', '/zxb/school/assets/antd.5ee291b2.css', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:09', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3283, '5', '白名单', '/zxb/school/assets/antd.5ee291b2.css', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:09', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3284, '5', '白名单', '/zxb/school/assets/school.694a1ada.png', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:10', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3285, '5', '黑名单', '/zxb/school/assets/school.694a1ada.png', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:10', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3286, '5', 'IP限流', '/zxb/school/assets/school.694a1ada.png', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:10', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3287, '5', '地址限流', '/zxb/school/assets/school.694a1ada.png', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:10', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3288, '5', 'IP限流', '/test1/zxb/school/', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:12', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3289, '5', '地址限流', '/test1/zxb/school/', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:12', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3290, '5', '黑名单', '/test1/zxb/school/', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:12', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3291, '5', '白名单', '/test1/zxb/school/', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:12', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3292, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:12', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3293, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:12', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3294, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:12', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3295, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-31 11:39:12', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3296, '5', 'IP限流', '//zxb/school/', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:39:12', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3297, '5', '黑名单', '//zxb/school/', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:39:12', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3298, '5', '地址限流', '//zxb/school/', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:39:12', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3299, '5', '白名单', '//zxb/school/', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:39:12', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3300, '5', '白名单', '//zxb/school/', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:39:17', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3301, '5', 'IP限流', '//zxb/school/', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:39:17', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3302, '5', '黑名单', '//zxb/school/', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:39:17', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3303, '5', '地址限流', '//zxb/school/', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:39:17', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3304, '5', '白名单', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:39:21', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3305, '5', '黑名单', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:39:21', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3306, '5', '地址限流', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:39:21', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3307, '5', 'IP限流', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:39:21', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3308, '5', '白名单', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:39:30', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3309, '5', '黑名单', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:39:30', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3310, '5', '地址限流', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:39:50', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3311, '5', 'IP限流', '//zxb/school', 'allow', '局域网', '192.168.110.1', '2024-07-31 11:39:50', 1722397140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3312, '5', '黑名单', '//baidu/s?wd=1', 'allow', '杭州市', '183.129.243.2', '2024-07-31 11:44:22', 1722397440000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3313, '5', '白名单', '//baidu/s?wd=1', 'allow', '杭州市', '183.129.243.2', '2024-07-31 11:44:22', 1722397440000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3314, '5', '地址限流', '//baidu/s?wd=1', 'allow', '杭州市', '183.129.243.2', '2024-07-31 11:44:22', 1722397440000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3315, '5', 'IP限流', '//baidu/s?wd=1', 'allow', '杭州市', '183.129.243.2', '2024-07-31 11:44:22', 1722397440000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3316, '5', 'IP限流', '//baidu/s?wd=1', 'allow', '杭州市', '183.129.243.2', '2024-07-31 11:44:32', 1722397440000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3317, '5', '黑名单', '//baidu/s?wd=1', 'allow', '杭州市', '183.129.243.2', '2024-07-31 11:44:32', 1722397440000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3318, '5', '白名单', '//baidu/s?wd=1', 'allow', '杭州市', '183.129.243.2', '2024-07-31 11:44:32', 1722397440000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3319, '5', '地址限流', '//baidu/s?wd=1', 'allow', '杭州市', '183.129.243.2', '2024-07-31 11:44:32', 1722397440000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3320, '5', 'IP限流', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-31 16:52:37', 1722415920000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3321, '5', '黑名单', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-31 16:52:37', 1722415920000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3322, '5', '地址限流', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-31 16:52:37', 1722415920000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3323, '5', '白名单', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-31 16:52:37', 1722415920000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3324, '5', 'IP限流', '/sugrec?&prod=pc_his&from=pc_web&json=1&sid=60278_60469_60492_60500_60515_60525_60566&hisdata=&_t=1722415958399&req=2&bs=1&csor=0', 'allow', '局域网', '192.168.110.8', '2024-07-31 16:52:38', 1722415920000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3325, '5', '黑名单', '/sugrec?&prod=pc_his&from=pc_web&json=1&sid=60278_60469_60492_60500_60515_60525_60566&hisdata=&_t=1722415958399&req=2&bs=1&csor=0', 'allow', '局域网', '192.168.110.8', '2024-07-31 16:52:38', 1722415920000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3326, '5', '白名单', '/sugrec?&prod=pc_his&from=pc_web&json=1&sid=60278_60469_60492_60500_60515_60525_60566&hisdata=&_t=1722415958399&req=2&bs=1&csor=0', 'allow', '局域网', '192.168.110.8', '2024-07-31 16:52:38', 1722415920000);
INSERT INTO `monitor_proxy_limit_log` VALUES (3327, '5', '地址限流', '/sugrec?&prod=pc_his&from=pc_web&json=1&sid=60278_60469_60492_60500_60515_60525_60566&hisdata=&_t=1722415958399&req=2&bs=1&csor=0', 'allow', '局域网', '192.168.110.8', '2024-07-31 16:52:38', 1722415920000);

-- ----------------------------
-- Table structure for monitor_proxy_plugin
-- ----------------------------
DROP TABLE IF EXISTS `monitor_proxy_plugin`;
CREATE TABLE `monitor_proxy_plugin`  (
  `plugin_id` int(11) NOT NULL AUTO_INCREMENT,
  `proxy_id` int(11) NULL DEFAULT NULL COMMENT '代理ID',
  `plugin_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '插件描述',
  `plugin_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '插件名称',
  `create_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `plugin_sort` int(11) NULL DEFAULT NULL COMMENT '优先级',
  PRIMARY KEY (`plugin_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 91 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '过滤器' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_proxy_plugin
-- ----------------------------
INSERT INTO `monitor_proxy_plugin` VALUES (89, 5, 'HTTP动态路由', 'ASYNCHTTPROUTINGGATEWAY', NULL, 0);
INSERT INTO `monitor_proxy_plugin` VALUES (90, 5, 'WS动态路由', 'ASYNCWEBSOCKETROUTINGGATEWAY', NULL, 1);

-- ----------------------------
-- Table structure for monitor_proxy_plugin_config
-- ----------------------------
DROP TABLE IF EXISTS `monitor_proxy_plugin_config`;
CREATE TABLE `monitor_proxy_plugin_config`  (
  `plugin_config_id` int(11) NOT NULL AUTO_INCREMENT,
  `proxy_id` int(11) NULL DEFAULT NULL COMMENT '代理ID',
  `plugin_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '插件名字',
  `plugin_sort` int(11) NULL DEFAULT NULL COMMENT '插件优先级',
  `plugin_config_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '配置名称',
  `plugin_config_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '配置',
  `plugin_config_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '配置类型',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`plugin_config_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '过滤器配置' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_proxy_plugin_config
-- ----------------------------
INSERT INTO `monitor_proxy_plugin_config` VALUES (1, 5, 'LIMIT', 0, 'limit', '1', NULL, '2024-06-18 17:32:26');
INSERT INTO `monitor_proxy_plugin_config` VALUES (2, 5, 'LIMIT', 0, 'permitsPerSecond', '0', 'NUMBER', '2024-06-19 09:01:33');
INSERT INTO `monitor_proxy_plugin_config` VALUES (3, 5, 'LIMIT', 2, 'permitsPerSecond', '0', 'NUMBER', NULL);

-- ----------------------------
-- Table structure for monitor_proxy_statistic_service_discovery
-- ----------------------------
DROP TABLE IF EXISTS `monitor_proxy_statistic_service_discovery`;
CREATE TABLE `monitor_proxy_statistic_service_discovery`  (
  `proxy_statistic_id` int(11) NOT NULL AUTO_INCREMENT,
  `proxy_statistic_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '名称说明',
  `proxy_statistic_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '地址前缀',
  `proxy_statistic_protocol` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '协议',
  `proxy_statistic_hostname` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '地址',
  `proxy_statistic_weight` int(11) NULL DEFAULT NULL COMMENT '权重',
  `proxy_id` int(11) NULL DEFAULT NULL COMMENT '代理ID',
  `proxy_statistic_status` int(11) NULL DEFAULT NULL COMMENT '状态;0:禁用',
  PRIMARY KEY (`proxy_statistic_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = '静态IP' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of monitor_proxy_statistic_service_discovery
-- ----------------------------
INSERT INTO `monitor_proxy_statistic_service_discovery` VALUES (2, '测试', '/system/api', 'http', 'https://www.qirenit.com', 12, 5, 1);
INSERT INTO `monitor_proxy_statistic_service_discovery` VALUES (3, '百度', '/baidu/', 'http', 'https://www.baidu.com/', 1, 5, 1);
INSERT INTO `monitor_proxy_statistic_service_discovery` VALUES (5, 'school', '/zxb/school', 'http', 'https://www.qirenit.com', 1, 5, 1);

-- ----------------------------
-- Table structure for monitor_sys_gen
-- ----------------------------
DROP TABLE IF EXISTS `monitor_sys_gen`;
CREATE TABLE `monitor_sys_gen`  (
  `gen_id` int(11) NOT NULL AUTO_INCREMENT,
  `gen_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据库名称说明',
  `gen_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据库类型',
  `gen_user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据库用户名',
  `gen_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据库密码',
  `gen_host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据库地址',
  `gen_port` int(11) NULL DEFAULT NULL COMMENT '数据库端口',
  `gen_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据库说明',
  `gen_driver` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据库驱动地址',
  `gen_driver_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据库驱动下载地址',
  `gen_database` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据库名称',
  `gen_database_file` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件数据库数据目录',
  `gen_uid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件数据库ID',
  `gen_backup_status` int(11) NULL DEFAULT NULL COMMENT '备份状态;0:未开启',
  `gen_jdbc_custom_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT 'JDBC类型',
  `gen_jdbc_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT 'JDBC类型',
  `gen_driver_file` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '驱动文件路径',
  `gen_driver_remote_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '驱动文件下载目录',
  `gen_backup_period` int(11) NULL DEFAULT 720 COMMENT '备份时间分钟(默认720)',
  `gen_backup_event` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备份事件',
  PRIMARY KEY (`gen_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '代码生成器管理' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_sys_gen
-- ----------------------------
INSERT INTO `monitor_sys_gen` VALUES (1, '本地 - MYSQL', 'JDBC', 'root', 'OHFOL3NPS3J0VXB6eGg5N1g5RVludz09', '172.16.0.40', 3306, '测试', 'com.mysql.cj.jdbc.Driver', NULL, 'monitor', NULL, 'OtPRaynDelFeVkVl', 0, 'JDBC', 'MYSQL', NULL, NULL, NULL, 'CREATE,DELETE');
INSERT INTO `monitor_sys_gen` VALUES (5, '测试', 'SHELL', 'root', 'bUxOUU1URWNpaWd3WDhlVjJqRDNOUT09', '192.168.110.100', 22, NULL, NULL, NULL, NULL, NULL, 'UW2lRgctX27PRvJK', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `monitor_sys_gen` VALUES (8, '11', 'REDIS', '', NULL, '172.16.0.40', 16379, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '', NULL, NULL, NULL, NULL);
INSERT INTO `monitor_sys_gen` VALUES (9, 'Sqlite', 'JDBC', '', 'Nnl5WXFhZXVCVkFoVWlDYzZ1dmVmZz09', '127.0.0.1', 3306, NULL, 'org.sqlite.JDBC', NULL, '', 'F:\\workspace\\utils-support-parent-starter\\gen\\9\\data\\1.db', 'OjndF5PYVMmD1BOW', NULL, 'FILE', 'SQLITE', NULL, NULL, 720, '');
INSERT INTO `monitor_sys_gen` VALUES (10, 'H2', 'JDBC', '', NULL, '127.0.0.1', 3306, NULL, 'org.h2.Driver', NULL, NULL, 'F:\\workspace\\utils-support-parent-starter\\gen\\10\\data\\1.db', NULL, NULL, 'FILE', 'H2', 'F:\\workspace\\utils-support-parent-starter\\gen\\10\\driver\\h2-2.1.214.jar', NULL, 720, '');
INSERT INTO `monitor_sys_gen` VALUES (11, '112.124.44.21', 'INFLUXDB', 'root', 'ZE9FZGpLeDlkOVJERTBCZE5sczdPUT09', '112.124.44.21', 8086, NULL, NULL, NULL, 'smartiot', NULL, 'CL32rLzJtPBLkNcS', NULL, NULL, '', NULL, NULL, 720, NULL);
INSERT INTO `monitor_sys_gen` VALUES (12, 'postgres', 'JDBC', 'postgres', 'OWR5VVZCb244U2xiNFZTYnp5NTBHdz09', '127.0.0.1', 5432, NULL, 'org.postgresql.Driver', NULL, 'postgres', NULL, 'f5LAJ20FBAjtNVfz', 1, 'JDBC', 'POSTGRES', NULL, NULL, 720, '');

-- ----------------------------
-- Table structure for monitor_sys_gen_column
-- ----------------------------
DROP TABLE IF EXISTS `monitor_sys_gen_column`;
CREATE TABLE `monitor_sys_gen_column`  (
  `col_id` int(11) NOT NULL AUTO_INCREMENT,
  `tab_id` int(11) NULL DEFAULT NULL COMMENT '表ID',
  `col_column_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '列名称',
  `col_column_comment` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '列描述',
  `col_column_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '列类型',
  `col_column_length` int(11) NULL DEFAULT NULL COMMENT '列长度',
  `col_column_decimal` int(11) NULL DEFAULT NULL COMMENT '小数点长度',
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
  `col_sort` int(11) NULL DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`col_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '生成器字段' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_sys_gen_column
-- ----------------------------
INSERT INTO `monitor_sys_gen_column` VALUES (6, 2, 'config_id', '主键', 'INT', 10, 0, 'Integer', 'configId', NULL, '0', '0', '1', '1', '1', '1', 'EQ', 'input', NULL, NULL);
INSERT INTO `monitor_sys_gen_column` VALUES (7, 2, 'config_name', '配置名称', 'VARCHAR', 255, 0, ' String', 'configName', NULL, '0', '1', '1', '1', '1', '1', 'LIKE', 'input', NULL, NULL);
INSERT INTO `monitor_sys_gen_column` VALUES (8, 2, 'config_value', '配置值', 'VARCHAR', 4096, 0, ' String', 'configValue', NULL, '0', '1', '1', '1', '1', '1', 'EQ', 'input', NULL, NULL);
INSERT INTO `monitor_sys_gen_column` VALUES (9, 2, 'config_desc', '配置描述', 'VARCHAR', 255, 0, ' String', 'configDesc', NULL, '0', '1', '1', '1', '1', '1', 'EQ', 'input', NULL, NULL);
INSERT INTO `monitor_sys_gen_column` VALUES (10, 2, 'proxy_id', '代理ID', 'VARCHAR', 255, 0, ' String', 'proxyId', NULL, '0', '1', '1', '1', '1', '1', 'EQ', 'input', NULL, NULL);

-- ----------------------------
-- Table structure for monitor_sys_gen_remark
-- ----------------------------
DROP TABLE IF EXISTS `monitor_sys_gen_remark`;
CREATE TABLE `monitor_sys_gen_remark`  (
  `remark_id` int(11) NOT NULL AUTO_INCREMENT,
  `remark_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `gen_id` int(11) NULL DEFAULT NULL COMMENT 'gen_id',
  `remark_table` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '表',
  `remark_database` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据库',
  `remark_column` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字段',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`remark_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字段备注' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_sys_gen_remark
-- ----------------------------
INSERT INTO `monitor_sys_gen_remark` VALUES (1, '温度', 11, 'live_data_1772504991081914369', 'smartiot', 'A191', NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for monitor_sys_gen_shell
-- ----------------------------
DROP TABLE IF EXISTS `monitor_sys_gen_shell`;
CREATE TABLE `monitor_sys_gen_shell`  (
  `shell_id` int(11) NOT NULL AUTO_INCREMENT,
  `shell_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本名称',
  `shell_script_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本路径',
  `shell_log_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志路径',
  `shell_args` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本参数',
  `shell_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '说明',
  `shell_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本内容',
  `gen_id` int(11) NULL DEFAULT NULL COMMENT 'gen表ID',
  `shell_status` int(11) NULL DEFAULT 0 COMMENT '状态;0: 待开启; 1:开启',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
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
  `tab_id` int(11) NOT NULL AUTO_INCREMENT,
  `tab_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '表名称',
  `tab_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `gen_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属数据源名称, 用于处理内部数据源',
  `gen_id` int(11) NULL DEFAULT NULL COMMENT '所属数据源',
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
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '生成器表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_sys_gen_table
-- ----------------------------
INSERT INTO `monitor_sys_gen_table` VALUES (2, 'monitor_proxy_config', '代理配置', '本地mysql', 1, 'MonitorProxyConfig', NULL, 'com', 'proxyConfig', NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for monitor_sys_gen_template
-- ----------------------------
DROP TABLE IF EXISTS `monitor_sys_gen_template`;
CREATE TABLE `monitor_sys_gen_template`  (
  `template_id` int(11) NOT NULL AUTO_INCREMENT,
  `template_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板名称',
  `template_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板类型',
  `gen_id` int(11) NULL DEFAULT NULL COMMENT '工具ID',
  `template_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目录',
  `template_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`template_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '生成器模板' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_sys_gen_template
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_terminal
-- ----------------------------
DROP TABLE IF EXISTS `monitor_terminal`;
CREATE TABLE `monitor_terminal`  (
  `terminal_id` int(11) NOT NULL AUTO_INCREMENT,
  `terminal_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '名称',
  `terminal_host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '地址',
  `terminal_port` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '端口',
  `terminal_user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '账号',
  `terminal_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '密码',
  `terminal_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '描述',
  `terminal_status` int(11) NULL DEFAULT NULL COMMENT '状态;0:未启动1:已启动; 2:启动中',
  PRIMARY KEY (`terminal_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '终端管理' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_terminal
-- ----------------------------
INSERT INTO `monitor_terminal` VALUES (1, '测试', '192.168.110.100', '22', 'root', 'boren1818', NULL, 0);
INSERT INTO `monitor_terminal` VALUES (2, '36.137.212.233', '36.137.212.233', '52176', 'root', '6sVhqzx85T', NULL, 0);
INSERT INTO `monitor_terminal` VALUES (4, '1', '120.26.74.94', '22', 'root', 'Qrkj1234.', NULL, 0);
INSERT INTO `monitor_terminal` VALUES (5, '8.139.4.229', '8.139.4.229', '22', 'root', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for monitor_terminal_base
-- ----------------------------
DROP TABLE IF EXISTS `monitor_terminal_base`;
CREATE TABLE `monitor_terminal_base`  (
  `base_id` int(11) NOT NULL AUTO_INCREMENT,
  `base_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '名称',
  `terminal_id` int(11) NULL DEFAULT NULL COMMENT '终端ID',
  `base_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '描述',
  `base_value` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '值',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`base_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 80 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '终端基本信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_terminal_base
-- ----------------------------
INSERT INTO `monitor_terminal_base` VALUES (1, 'public-ifconfig', 1, '公网IP', '', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `monitor_terminal_base` VALUES (2, 'public-ifconfig', 2, '公网IP', '36.137.212.233', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `monitor_terminal_base` VALUES (3, 'release', 2, '系统', 'Linux zhdsj 4.15.0-142-generic #146-Ubuntu SMP Tue Apr 13 01:11:19 UTC 2021 x86_64 x86_64 x86_64 GNU/Linux', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `monitor_terminal_base` VALUES (4, 'ulimit', 2, '最大连接数', '65535', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `monitor_terminal_base` VALUES (5, 'mem-total', 2, '最大内存', '15.48', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `monitor_terminal_base` VALUES (6, 'cpu-model', 2, 'cpu型号', 'Intel(R) Xeon(R) Gold 6278C CPU @ 2.60GHz', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `monitor_terminal_base` VALUES (7, 'cpu-num', 2, 'cpu数', '4', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `monitor_terminal_base` VALUES (8, 'cpu-core-num', 2, 'cpu core数', '2', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `monitor_terminal_base` VALUES (9, 'release', 1, '系统信息', 'CentOS Linux release 7.9.2009 (Core)', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `monitor_terminal_base` VALUES (10, 'ulimit', 1, '最大连接数', '65536', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `monitor_terminal_base` VALUES (11, 'mem-total', 1, '最大内存', '31.21', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `monitor_terminal_base` VALUES (12, 'cpu-model', 1, 'cpu型号', 'Intel(R) Xeon(R) CPU E3-1231 v3 @ 3.40GHz', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `monitor_terminal_base` VALUES (13, 'cpu-num', 1, 'cpu数', '8', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `monitor_terminal_base` VALUES (70, 'public-ifconfig', 4, '公网IP', '', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `monitor_terminal_base` VALUES (71, 'release', 4, '系统信息', '', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `monitor_terminal_base` VALUES (72, 'ulimit', 4, '最大连接数', '0', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `monitor_terminal_base` VALUES (73, 'mem-total', 4, '最大内存', '0.0', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `monitor_terminal_base` VALUES (74, 'cpu-model', 4, 'cpu型号', '', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `monitor_terminal_base` VALUES (75, 'cpu-num', 4, 'cpu数', 'null', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `monitor_terminal_base` VALUES (76, 'cpu-core-num', 4, 'cpu core数', 'null', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `monitor_terminal_base` VALUES (77, 'ipaddress', 4, 'ip地址名称', '', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `monitor_terminal_base` VALUES (79, 'cpu-core-num', 1, 'cpu core数', '4', NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for monitor_terminal_project
-- ----------------------------
DROP TABLE IF EXISTS `monitor_terminal_project`;
CREATE TABLE `monitor_terminal_project`  (
  `terminal_project_id` int(11) NOT NULL AUTO_INCREMENT,
  `terminal_project_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '项目名称',
  `terminal_project_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '项目描述',
  `terminal_project_status` int(11) NULL DEFAULT NULL COMMENT '是否开启;0：关闭',
  `terminal_project_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '项目地址',
  `terminal_project_start_script` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '项目启动脚本',
  `terminal_project_end_script` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '项目停止脚本',
  `terminal_id` int(11) NULL DEFAULT NULL COMMENT '终端ID',
  `terminal_project_log` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '日志文件地址',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建地址',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新地址',
  PRIMARY KEY (`terminal_project_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '项目管理' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_terminal_project
-- ----------------------------
INSERT INTO `monitor_terminal_project` VALUES (1, '测试', '', NULL, '/home/zxb2/teacher', 'start.sh', 'stop.sh', 1, 'start.log', NULL, '2024-06-27 09:38:46');

SET FOREIGN_KEY_CHECKS = 1;
