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

 Date: 30/07/2024 17:23:44
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
  `file_storage_cookie` text CHARACTER SET utf8 COLLATE utf8_bin NULL COMMENT 'cookie',
  PRIMARY KEY (`file_storage_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = '文件存储' ROW_FORMAT = Dynamic;

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
INSERT INTO `file_storage_protocol` VALUES (2, 8184, '测试', 'HTTP', '0.0.0.0', 1, '', 'SHAPE,GRAY,ORIENT,FORMAT,QUALITY,INTERLACE,ROTATE,BLUR,THUMBNAIL,BRIGHT', 0, 1, 0, 0, NULL, 'PC;OSS', 0);

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
INSERT INTO `monitor_proxy_config` VALUES (9, 'serviceDiscovery', 'NACOS', '静态代理', '5');
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
) ENGINE = InnoDB AUTO_INCREMENT = 2224 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '限流日志' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_proxy_limit_log
-- ----------------------------
INSERT INTO `monitor_proxy_limit_log` VALUES (150, '5', 'IP限流', '/', 'allow', '本机地址', '127.0.0.1', '2024-07-30 15:17:43', 1722323820000);
INSERT INTO `monitor_proxy_limit_log` VALUES (151, '5', '黑名单', '/', 'allow', '本机地址', '127.0.0.1', '2024-07-30 15:17:44', 1722323820000);
INSERT INTO `monitor_proxy_limit_log` VALUES (152, '5', 'IP限流', '/', 'allow', '本机地址', '127.0.0.1', '2024-07-30 15:24:38', 1722324240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (153, '5', '黑名单', '/', 'allow', '本机地址', '127.0.0.1', '2024-07-30 15:24:39', 1722324240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (154, '5', 'IP限流', '/favicon.ico', 'allow', '本机地址', '127.0.0.1', '2024-07-30 15:24:42', 1722324240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (155, '5', '黑名单', '/favicon.ico', 'allow', '本机地址', '127.0.0.1', '2024-07-30 15:24:42', 1722324240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (156, '5', '黑名单', '/', 'allow', '本机地址', '127.0.0.1', '2024-07-30 15:29:45', 1722324540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (157, '5', 'IP限流', '/', 'allow', '本机地址', '127.0.0.1', '2024-07-30 15:29:45', 1722324540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (158, '5', '黑名单', '/favicon.ico', 'allow', '本机地址', '127.0.0.1', '2024-07-30 15:29:47', 1722324540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (159, '5', 'IP限流', '/favicon.ico', 'allow', '本机地址', '127.0.0.1', '2024-07-30 15:29:47', 1722324540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (160, '5', '黑名单', '/', 'allow', '本机地址', '127.0.0.1', '2024-07-30 15:29:47', 1722324540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (161, '5', 'IP限流', '/', 'allow', '本机地址', '127.0.0.1', '2024-07-30 15:29:47', 1722324540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (162, '5', 'IP限流', '/favicon.ico', 'allow', '本机地址', '127.0.0.1', '2024-07-30 15:29:49', 1722324540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (163, '5', '黑名单', '/favicon.ico', 'allow', '本机地址', '127.0.0.1', '2024-07-30 15:29:49', 1722324540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (164, '5', '黑名单', '/', 'allow', '本机地址', '127.0.0.1', '2024-07-30 15:31:46', 1722324660000);
INSERT INTO `monitor_proxy_limit_log` VALUES (165, '5', 'IP限流', '/', 'allow', '本机地址', '127.0.0.1', '2024-07-30 15:31:46', 1722324660000);
INSERT INTO `monitor_proxy_limit_log` VALUES (166, '5', '黑名单', '/favicon.ico', 'allow', '本机地址', '127.0.0.1', '2024-07-30 15:31:48', 1722324660000);
INSERT INTO `monitor_proxy_limit_log` VALUES (167, '5', 'IP限流', '/favicon.ico', 'allow', '本机地址', '127.0.0.1', '2024-07-30 15:31:48', 1722324660000);
INSERT INTO `monitor_proxy_limit_log` VALUES (168, '5', '黑名单', '/', 'allow', '本机地址', '127.0.0.1', '2024-07-30 15:32:02', 1722324720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (169, '5', 'IP限流', '/', 'allow', '本机地址', '127.0.0.1', '2024-07-30 15:32:02', 1722324720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (170, '5', 'IP限流', '/', 'allow', '本机地址', '127.0.0.1', '2024-07-30 15:32:22', 1722324720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (171, '5', '黑名单', '/', 'allow', '本机地址', '127.0.0.1', '2024-07-30 15:32:22', 1722324720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (172, '5', 'IP限流', '/favicon.ico', 'allow', '本机地址', '127.0.0.1', '2024-07-30 15:32:24', 1722324720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (173, '5', '黑名单', '/favicon.ico', 'allow', '本机地址', '127.0.0.1', '2024-07-30 15:32:24', 1722324720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (174, '5', '黑名单', '/', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:32:27', 1722324720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (175, '5', 'IP限流', '/', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:32:27', 1722324720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (176, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:32:33', 1722324720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (177, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:32:33', 1722324720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (178, '5', '黑名单', '/', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:32:42', 1722324720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (179, '5', 'IP限流', '/', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:32:42', 1722324720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (180, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:32:49', 1722324720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (181, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:32:49', 1722324720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (182, '5', '黑名单', '/', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:44:43', 1722325440000);
INSERT INTO `monitor_proxy_limit_log` VALUES (183, '5', '白名单', '/', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:44:46', 1722325440000);
INSERT INTO `monitor_proxy_limit_log` VALUES (184, '5', 'IP限流', '/', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:44:46', 1722325440000);
INSERT INTO `monitor_proxy_limit_log` VALUES (185, '5', '地址限流', '/', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:44:46', 1722325440000);
INSERT INTO `monitor_proxy_limit_log` VALUES (186, '5', '黑名单', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:46:20', 1722325560000);
INSERT INTO `monitor_proxy_limit_log` VALUES (187, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:46:20', 1722325560000);
INSERT INTO `monitor_proxy_limit_log` VALUES (188, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:46:20', 1722325560000);
INSERT INTO `monitor_proxy_limit_log` VALUES (189, '5', '白名单', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:46:20', 1722325560000);
INSERT INTO `monitor_proxy_limit_log` VALUES (190, '5', '黑名单', '/', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:46:56', 1722325560000);
INSERT INTO `monitor_proxy_limit_log` VALUES (191, '5', '黑名单', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:47:03', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (192, '5', '黑名单', '/', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:47:05', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (193, '5', '黑名单', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:47:05', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (194, '5', '黑名单', '/', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:47:06', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (195, '5', '黑名单', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:47:06', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (196, '5', '黑名单', '/', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:47:06', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (197, '5', '黑名单', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:47:06', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (198, '5', '黑名单', '/', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:47:06', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (199, '5', '黑名单', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:47:06', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (200, '5', '黑名单', '/', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:47:06', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (201, '5', '黑名单', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:47:06', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (202, '5', '黑名单', '/', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:47:06', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (203, '5', '黑名单', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:47:06', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (204, '5', '黑名单', '/', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:47:06', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (205, '5', '黑名单', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:47:06', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (206, '5', '黑名单', '/', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:47:08', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (207, '5', '黑名单', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:47:08', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (208, '5', '黑名单', '/', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:47:08', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (209, '5', '黑名单', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:47:08', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (210, '5', '黑名单', '/', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:47:08', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (211, '5', '黑名单', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:47:08', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (212, '5', '黑名单', '/', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:47:13', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (213, '5', '黑名单', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:47:13', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (214, '5', '黑名单', '/', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:47:13', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (215, '5', '黑名单', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:47:13', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (216, '5', '黑名单', '/', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:47:13', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (217, '5', '黑名单', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:47:13', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (218, '5', '黑名单', '/', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:47:13', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (219, '5', '黑名单', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:47:13', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (220, '5', '黑名单', '/', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:47:13', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (221, '5', '黑名单', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:47:13', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (222, '5', '黑名单', '/', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:47:36', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (223, '5', '白名单', '/', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:47:36', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (224, '5', '白名单', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:47:36', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (225, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:47:36', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (226, '5', '黑名单', '/', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:47:37', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (227, '5', '白名单', '/', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:47:37', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (228, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:47:37', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (229, '5', '白名单', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:47:37', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (230, '5', '白名单', '/', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:47:37', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (231, '5', '黑名单', '/', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:47:37', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (232, '5', '白名单', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:47:37', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (233, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:47:37', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (234, '5', '黑名单', '/', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:47:37', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (235, '5', '白名单', '/', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:47:37', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (236, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:47:37', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (237, '5', '白名单', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:47:37', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (238, '5', '黑名单', '/', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:47:48', 1722325620000);
INSERT INTO `monitor_proxy_limit_log` VALUES (239, '5', '白名单', '/', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:48:35', 1722325680000);
INSERT INTO `monitor_proxy_limit_log` VALUES (240, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:48:35', 1722325680000);
INSERT INTO `monitor_proxy_limit_log` VALUES (241, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:48:35', 1722325680000);
INSERT INTO `monitor_proxy_limit_log` VALUES (242, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:48:35', 1722325680000);
INSERT INTO `monitor_proxy_limit_log` VALUES (243, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:48:35', 1722325680000);
INSERT INTO `monitor_proxy_limit_log` VALUES (244, '5', '白名单', '/', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:48:37', 1722325680000);
INSERT INTO `monitor_proxy_limit_log` VALUES (245, '5', '黑名单', '/', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:48:37', 1722325680000);
INSERT INTO `monitor_proxy_limit_log` VALUES (246, '5', 'IP限流', '/', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:48:37', 1722325680000);
INSERT INTO `monitor_proxy_limit_log` VALUES (247, '5', '地址限流', '/', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:48:37', 1722325680000);
INSERT INTO `monitor_proxy_limit_log` VALUES (248, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:48:38', 1722325680000);
INSERT INTO `monitor_proxy_limit_log` VALUES (249, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:48:38', 1722325680000);
INSERT INTO `monitor_proxy_limit_log` VALUES (250, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:48:38', 1722325680000);
INSERT INTO `monitor_proxy_limit_log` VALUES (251, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:48:38', 1722325680000);
INSERT INTO `monitor_proxy_limit_log` VALUES (252, '5', '黑名单', '/', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:48:46', 1722325680000);
INSERT INTO `monitor_proxy_limit_log` VALUES (253, '5', '白名单', '/', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:48:46', 1722325680000);
INSERT INTO `monitor_proxy_limit_log` VALUES (254, '5', 'IP限流', '/', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:48:52', 1722325680000);
INSERT INTO `monitor_proxy_limit_log` VALUES (255, '5', '地址限流', '/', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:48:56', 1722325680000);
INSERT INTO `monitor_proxy_limit_log` VALUES (256, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:49:06', 1722325740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (257, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:49:06', 1722325740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (258, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:49:09', 1722325740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (259, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:49:09', 1722325740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (260, '5', '地址限流', '/', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:49:11', 1722325740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (261, '5', '黑名单', '/', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:49:11', 1722325740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (262, '5', 'IP限流', '/', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:49:11', 1722325740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (263, '5', '白名单', '/', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:49:11', 1722325740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (264, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:49:13', 1722325740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (265, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:49:13', 1722325740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (266, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:49:13', 1722325740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (267, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:49:13', 1722325740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (268, '5', 'IP限流', '/', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:49:14', 1722325740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (269, '5', '白名单', '/', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:49:14', 1722325740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (270, '5', '黑名单', '/', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:49:14', 1722325740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (271, '5', '地址限流', '/', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:49:14', 1722325740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (272, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:49:16', 1722325740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (273, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:49:16', 1722325740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (274, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:49:16', 1722325740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (275, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:49:16', 1722325740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (276, '5', '黑名单', '/', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:49:54', 1722325740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (277, '5', '黑名单', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:49:54', 1722325740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (278, '5', '黑名单', '/', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:49:55', 1722325740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (279, '5', '黑名单', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:49:55', 1722325740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (280, '5', '黑名单', '/', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:49:55', 1722325740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (281, '5', '黑名单', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:49:55', 1722325740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (282, '5', '黑名单', '/', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:49:55', 1722325740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (283, '5', '黑名单', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:49:55', 1722325740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (284, '5', '黑名单', '/', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:49:55', 1722325740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (285, '5', '黑名单', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:49:55', 1722325740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (286, '5', '黑名单', '/', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:49:56', 1722325740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (287, '5', '黑名单', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:49:56', 1722325740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (288, '5', '黑名单', '/', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:49:56', 1722325740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (289, '5', '黑名单', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:49:56', 1722325740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (290, '5', '黑名单', '/', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:49:56', 1722325740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (291, '5', '黑名单', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:49:56', 1722325740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (292, '5', '黑名单', '/', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:49:56', 1722325740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (293, '5', '黑名单', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:49:56', 1722325740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (294, '5', '黑名单', '/', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:49:56', 1722325740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (295, '5', '黑名单', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-07-30 15:49:56', 1722325740000);
INSERT INTO `monitor_proxy_limit_log` VALUES (296, '5', 'IP限流', '/', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:51:58', 1722325860000);
INSERT INTO `monitor_proxy_limit_log` VALUES (297, '5', '黑名单', '/', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:51:58', 1722325860000);
INSERT INTO `monitor_proxy_limit_log` VALUES (298, '5', '白名单', '/', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:51:58', 1722325860000);
INSERT INTO `monitor_proxy_limit_log` VALUES (299, '5', '地址限流', '/', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:51:58', 1722325860000);
INSERT INTO `monitor_proxy_limit_log` VALUES (300, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:52:00', 1722325920000);
INSERT INTO `monitor_proxy_limit_log` VALUES (301, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:52:00', 1722325920000);
INSERT INTO `monitor_proxy_limit_log` VALUES (302, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:52:00', 1722325920000);
INSERT INTO `monitor_proxy_limit_log` VALUES (303, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 15:52:00', 1722325920000);
INSERT INTO `monitor_proxy_limit_log` VALUES (304, '5', '地址限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:46:04', 1722329160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (305, '5', '黑名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:46:04', 1722329160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (306, '5', '白名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:46:04', 1722329160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (307, '5', 'IP限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:46:04', 1722329160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (308, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:46:07', 1722329160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (309, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:46:07', 1722329160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (310, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:46:07', 1722329160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (311, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:46:07', 1722329160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (312, '5', '白名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:46:18', 1722329160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (313, '5', 'IP限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:46:18', 1722329160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (314, '5', '地址限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:46:18', 1722329160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (315, '5', '黑名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:46:18', 1722329160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (316, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:46:26', 1722329160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (317, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:46:26', 1722329160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (318, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:46:26', 1722329160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (319, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:46:26', 1722329160000);
INSERT INTO `monitor_proxy_limit_log` VALUES (320, '5', '地址限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:48:23', 1722329280000);
INSERT INTO `monitor_proxy_limit_log` VALUES (321, '5', '白名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:48:23', 1722329280000);
INSERT INTO `monitor_proxy_limit_log` VALUES (322, '5', 'IP限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:48:23', 1722329280000);
INSERT INTO `monitor_proxy_limit_log` VALUES (323, '5', '黑名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:48:23', 1722329280000);
INSERT INTO `monitor_proxy_limit_log` VALUES (324, '5', '地址限流', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:48:23', 1722329280000);
INSERT INTO `monitor_proxy_limit_log` VALUES (325, '5', '黑名单', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:48:23', 1722329280000);
INSERT INTO `monitor_proxy_limit_log` VALUES (326, '5', '白名单', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:48:23', 1722329280000);
INSERT INTO `monitor_proxy_limit_log` VALUES (327, '5', 'IP限流', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:48:23', 1722329280000);
INSERT INTO `monitor_proxy_limit_log` VALUES (328, '5', 'IP限流', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:48:23', 1722329280000);
INSERT INTO `monitor_proxy_limit_log` VALUES (329, '5', '黑名单', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:48:23', 1722329280000);
INSERT INTO `monitor_proxy_limit_log` VALUES (330, '5', '黑名单', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:48:23', 1722329280000);
INSERT INTO `monitor_proxy_limit_log` VALUES (331, '5', '白名单', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:48:23', 1722329280000);
INSERT INTO `monitor_proxy_limit_log` VALUES (332, '5', '白名单', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:48:23', 1722329280000);
INSERT INTO `monitor_proxy_limit_log` VALUES (333, '5', '地址限流', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:48:23', 1722329280000);
INSERT INTO `monitor_proxy_limit_log` VALUES (334, '5', '地址限流', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:48:23', 1722329280000);
INSERT INTO `monitor_proxy_limit_log` VALUES (335, '5', 'IP限流', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:48:23', 1722329280000);
INSERT INTO `monitor_proxy_limit_log` VALUES (336, '5', '地址限流', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:48:36', 1722329280000);
INSERT INTO `monitor_proxy_limit_log` VALUES (337, '5', 'IP限流', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:48:36', 1722329280000);
INSERT INTO `monitor_proxy_limit_log` VALUES (338, '5', '白名单', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:48:36', 1722329280000);
INSERT INTO `monitor_proxy_limit_log` VALUES (339, '5', '黑名单', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:48:36', 1722329280000);
INSERT INTO `monitor_proxy_limit_log` VALUES (340, '5', '地址限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:48:43', 1722329280000);
INSERT INTO `monitor_proxy_limit_log` VALUES (341, '5', 'IP限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:48:43', 1722329280000);
INSERT INTO `monitor_proxy_limit_log` VALUES (342, '5', '黑名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:48:43', 1722329280000);
INSERT INTO `monitor_proxy_limit_log` VALUES (343, '5', '白名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:48:43', 1722329280000);
INSERT INTO `monitor_proxy_limit_log` VALUES (344, '5', '黑名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:49:14', 1722329340000);
INSERT INTO `monitor_proxy_limit_log` VALUES (345, '5', '白名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:49:14', 1722329340000);
INSERT INTO `monitor_proxy_limit_log` VALUES (346, '5', 'IP限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:49:14', 1722329340000);
INSERT INTO `monitor_proxy_limit_log` VALUES (347, '5', '地址限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:49:14', 1722329340000);
INSERT INTO `monitor_proxy_limit_log` VALUES (348, '5', '地址限流', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:50:05', 1722329400000);
INSERT INTO `monitor_proxy_limit_log` VALUES (349, '5', '黑名单', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:50:05', 1722329400000);
INSERT INTO `monitor_proxy_limit_log` VALUES (350, '5', '黑名单', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:50:05', 1722329400000);
INSERT INTO `monitor_proxy_limit_log` VALUES (351, '5', '地址限流', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:50:05', 1722329400000);
INSERT INTO `monitor_proxy_limit_log` VALUES (352, '5', 'IP限流', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:50:05', 1722329400000);
INSERT INTO `monitor_proxy_limit_log` VALUES (353, '5', '白名单', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:50:05', 1722329400000);
INSERT INTO `monitor_proxy_limit_log` VALUES (354, '5', '白名单', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:50:05', 1722329400000);
INSERT INTO `monitor_proxy_limit_log` VALUES (355, '5', 'IP限流', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:50:05', 1722329400000);
INSERT INTO `monitor_proxy_limit_log` VALUES (356, '5', '黑名单', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:50:05', 1722329400000);
INSERT INTO `monitor_proxy_limit_log` VALUES (357, '5', '地址限流', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:50:05', 1722329400000);
INSERT INTO `monitor_proxy_limit_log` VALUES (358, '5', 'IP限流', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:50:05', 1722329400000);
INSERT INTO `monitor_proxy_limit_log` VALUES (359, '5', '白名单', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:50:05', 1722329400000);
INSERT INTO `monitor_proxy_limit_log` VALUES (360, '5', '黑名单', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:50:25', 1722329400000);
INSERT INTO `monitor_proxy_limit_log` VALUES (361, '5', '白名单', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:50:25', 1722329400000);
INSERT INTO `monitor_proxy_limit_log` VALUES (362, '5', '地址限流', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:50:25', 1722329400000);
INSERT INTO `monitor_proxy_limit_log` VALUES (363, '5', 'IP限流', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:50:25', 1722329400000);
INSERT INTO `monitor_proxy_limit_log` VALUES (364, '5', 'IP限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:50:26', 1722329400000);
INSERT INTO `monitor_proxy_limit_log` VALUES (365, '5', '地址限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:50:26', 1722329400000);
INSERT INTO `monitor_proxy_limit_log` VALUES (366, '5', '黑名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:50:26', 1722329400000);
INSERT INTO `monitor_proxy_limit_log` VALUES (367, '5', '白名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:50:26', 1722329400000);
INSERT INTO `monitor_proxy_limit_log` VALUES (368, '5', '地址限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:51:23', 1722329460000);
INSERT INTO `monitor_proxy_limit_log` VALUES (369, '5', '白名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:51:23', 1722329460000);
INSERT INTO `monitor_proxy_limit_log` VALUES (370, '5', '黑名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:51:23', 1722329460000);
INSERT INTO `monitor_proxy_limit_log` VALUES (371, '5', 'IP限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:51:23', 1722329460000);
INSERT INTO `monitor_proxy_limit_log` VALUES (372, '5', '黑名单', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:51:30', 1722329460000);
INSERT INTO `monitor_proxy_limit_log` VALUES (373, '5', '白名单', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:51:30', 1722329460000);
INSERT INTO `monitor_proxy_limit_log` VALUES (374, '5', 'IP限流', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:51:30', 1722329460000);
INSERT INTO `monitor_proxy_limit_log` VALUES (375, '5', '地址限流', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:51:30', 1722329460000);
INSERT INTO `monitor_proxy_limit_log` VALUES (376, '5', '黑名单', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:51:30', 1722329460000);
INSERT INTO `monitor_proxy_limit_log` VALUES (377, '5', 'IP限流', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:51:30', 1722329460000);
INSERT INTO `monitor_proxy_limit_log` VALUES (378, '5', '白名单', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:51:30', 1722329460000);
INSERT INTO `monitor_proxy_limit_log` VALUES (379, '5', 'IP限流', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:51:30', 1722329460000);
INSERT INTO `monitor_proxy_limit_log` VALUES (380, '5', '地址限流', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:51:30', 1722329460000);
INSERT INTO `monitor_proxy_limit_log` VALUES (381, '5', '白名单', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:51:30', 1722329460000);
INSERT INTO `monitor_proxy_limit_log` VALUES (382, '5', '黑名单', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:51:30', 1722329460000);
INSERT INTO `monitor_proxy_limit_log` VALUES (383, '5', '地址限流', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:51:30', 1722329460000);
INSERT INTO `monitor_proxy_limit_log` VALUES (384, '5', '白名单', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:51:30', 1722329460000);
INSERT INTO `monitor_proxy_limit_log` VALUES (385, '5', '黑名单', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:51:30', 1722329460000);
INSERT INTO `monitor_proxy_limit_log` VALUES (386, '5', 'IP限流', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:51:30', 1722329460000);
INSERT INTO `monitor_proxy_limit_log` VALUES (387, '5', '地址限流', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:51:30', 1722329460000);
INSERT INTO `monitor_proxy_limit_log` VALUES (388, '5', '地址限流', '/zxb/school/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:51:31', 1722329460000);
INSERT INTO `monitor_proxy_limit_log` VALUES (389, '5', '黑名单', '/zxb/school/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:51:31', 1722329460000);
INSERT INTO `monitor_proxy_limit_log` VALUES (390, '5', '白名单', '/zxb/school/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:51:31', 1722329460000);
INSERT INTO `monitor_proxy_limit_log` VALUES (391, '5', 'IP限流', '/zxb/school/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:51:31', 1722329460000);
INSERT INTO `monitor_proxy_limit_log` VALUES (392, '5', '黑名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:52:05', 1722329520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (393, '5', 'IP限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:52:05', 1722329520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (394, '5', '白名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:52:05', 1722329520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (395, '5', '地址限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:52:05', 1722329520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (396, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:52:05', 1722329520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (397, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:52:05', 1722329520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (398, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:52:05', 1722329520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (399, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:52:05', 1722329520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (400, '5', '黑名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:52:12', 1722329520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (401, '5', 'IP限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:52:12', 1722329520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (402, '5', '白名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:52:12', 1722329520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (403, '5', '地址限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:52:12', 1722329520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (404, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:52:28', 1722329520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (405, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:52:28', 1722329520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (406, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:52:28', 1722329520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (407, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:52:28', 1722329520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (408, '5', '黑名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:52:32', 1722329520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (409, '5', '地址限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:52:32', 1722329520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (410, '5', '白名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:52:32', 1722329520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (411, '5', 'IP限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:52:32', 1722329520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (412, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:52:32', 1722329520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (413, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:52:32', 1722329520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (414, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:52:32', 1722329520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (415, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:52:32', 1722329520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (416, '5', '黑名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:52:35', 1722329520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (417, '5', '地址限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:52:35', 1722329520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (418, '5', '白名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:52:35', 1722329520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (419, '5', 'IP限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:52:35', 1722329520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (420, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:52:47', 1722329520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (421, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:52:47', 1722329520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (422, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:52:47', 1722329520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (423, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:52:47', 1722329520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (424, '5', 'IP限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:52:57', 1722329520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (425, '5', '白名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:52:57', 1722329520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (426, '5', '地址限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:52:57', 1722329520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (427, '5', '黑名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:52:57', 1722329520000);
INSERT INTO `monitor_proxy_limit_log` VALUES (428, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:53:27', 1722329580000);
INSERT INTO `monitor_proxy_limit_log` VALUES (429, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:53:27', 1722329580000);
INSERT INTO `monitor_proxy_limit_log` VALUES (430, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:53:27', 1722329580000);
INSERT INTO `monitor_proxy_limit_log` VALUES (431, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:53:27', 1722329580000);
INSERT INTO `monitor_proxy_limit_log` VALUES (432, '5', 'IP限流', '/zxb/school/', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:53:32', 1722329580000);
INSERT INTO `monitor_proxy_limit_log` VALUES (433, '5', '白名单', '/zxb/school/', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:53:32', 1722329580000);
INSERT INTO `monitor_proxy_limit_log` VALUES (434, '5', '地址限流', '/zxb/school/', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:53:32', 1722329580000);
INSERT INTO `monitor_proxy_limit_log` VALUES (435, '5', '黑名单', '/zxb/school/', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:53:32', 1722329580000);
INSERT INTO `monitor_proxy_limit_log` VALUES (436, '5', '地址限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:54:15', 1722329640000);
INSERT INTO `monitor_proxy_limit_log` VALUES (437, '5', '白名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:54:15', 1722329640000);
INSERT INTO `monitor_proxy_limit_log` VALUES (438, '5', '黑名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:54:15', 1722329640000);
INSERT INTO `monitor_proxy_limit_log` VALUES (439, '5', 'IP限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:54:15', 1722329640000);
INSERT INTO `monitor_proxy_limit_log` VALUES (440, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:54:29', 1722329640000);
INSERT INTO `monitor_proxy_limit_log` VALUES (441, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:54:29', 1722329640000);
INSERT INTO `monitor_proxy_limit_log` VALUES (442, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:54:29', 1722329640000);
INSERT INTO `monitor_proxy_limit_log` VALUES (443, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:54:29', 1722329640000);
INSERT INTO `monitor_proxy_limit_log` VALUES (444, '5', '白名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:54:35', 1722329640000);
INSERT INTO `monitor_proxy_limit_log` VALUES (445, '5', '黑名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:54:35', 1722329640000);
INSERT INTO `monitor_proxy_limit_log` VALUES (446, '5', '地址限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:54:35', 1722329640000);
INSERT INTO `monitor_proxy_limit_log` VALUES (447, '5', 'IP限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:54:35', 1722329640000);
INSERT INTO `monitor_proxy_limit_log` VALUES (448, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:55:04', 1722329700000);
INSERT INTO `monitor_proxy_limit_log` VALUES (449, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:55:04', 1722329700000);
INSERT INTO `monitor_proxy_limit_log` VALUES (450, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:55:04', 1722329700000);
INSERT INTO `monitor_proxy_limit_log` VALUES (451, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:55:04', 1722329700000);
INSERT INTO `monitor_proxy_limit_log` VALUES (452, '5', '黑名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:55:29', 1722329700000);
INSERT INTO `monitor_proxy_limit_log` VALUES (453, '5', '白名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:55:29', 1722329700000);
INSERT INTO `monitor_proxy_limit_log` VALUES (454, '5', '地址限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:55:29', 1722329700000);
INSERT INTO `monitor_proxy_limit_log` VALUES (455, '5', 'IP限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:55:29', 1722329700000);
INSERT INTO `monitor_proxy_limit_log` VALUES (456, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:55:32', 1722329700000);
INSERT INTO `monitor_proxy_limit_log` VALUES (457, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:55:32', 1722329700000);
INSERT INTO `monitor_proxy_limit_log` VALUES (458, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:55:32', 1722329700000);
INSERT INTO `monitor_proxy_limit_log` VALUES (459, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:55:32', 1722329700000);
INSERT INTO `monitor_proxy_limit_log` VALUES (460, '5', '白名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:55:43', 1722329700000);
INSERT INTO `monitor_proxy_limit_log` VALUES (461, '5', 'IP限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:55:43', 1722329700000);
INSERT INTO `monitor_proxy_limit_log` VALUES (462, '5', '地址限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:55:43', 1722329700000);
INSERT INTO `monitor_proxy_limit_log` VALUES (463, '5', '黑名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:55:43', 1722329700000);
INSERT INTO `monitor_proxy_limit_log` VALUES (464, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:55:48', 1722329700000);
INSERT INTO `monitor_proxy_limit_log` VALUES (465, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:55:48', 1722329700000);
INSERT INTO `monitor_proxy_limit_log` VALUES (466, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:55:48', 1722329700000);
INSERT INTO `monitor_proxy_limit_log` VALUES (467, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:55:48', 1722329700000);
INSERT INTO `monitor_proxy_limit_log` VALUES (468, '5', '地址限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:55:52', 1722329700000);
INSERT INTO `monitor_proxy_limit_log` VALUES (469, '5', 'IP限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:55:52', 1722329700000);
INSERT INTO `monitor_proxy_limit_log` VALUES (470, '5', '黑名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:55:52', 1722329700000);
INSERT INTO `monitor_proxy_limit_log` VALUES (471, '5', '白名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:55:52', 1722329700000);
INSERT INTO `monitor_proxy_limit_log` VALUES (472, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:56:13', 1722329760000);
INSERT INTO `monitor_proxy_limit_log` VALUES (473, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:56:13', 1722329760000);
INSERT INTO `monitor_proxy_limit_log` VALUES (474, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:56:13', 1722329760000);
INSERT INTO `monitor_proxy_limit_log` VALUES (475, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:56:13', 1722329760000);
INSERT INTO `monitor_proxy_limit_log` VALUES (476, '5', '地址限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:56:25', 1722329760000);
INSERT INTO `monitor_proxy_limit_log` VALUES (477, '5', '白名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:56:25', 1722329760000);
INSERT INTO `monitor_proxy_limit_log` VALUES (478, '5', '黑名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:56:25', 1722329760000);
INSERT INTO `monitor_proxy_limit_log` VALUES (479, '5', 'IP限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:56:25', 1722329760000);
INSERT INTO `monitor_proxy_limit_log` VALUES (480, '5', '地址限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:56:57', 1722329760000);
INSERT INTO `monitor_proxy_limit_log` VALUES (481, '5', '白名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:56:57', 1722329760000);
INSERT INTO `monitor_proxy_limit_log` VALUES (482, '5', 'IP限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:56:57', 1722329760000);
INSERT INTO `monitor_proxy_limit_log` VALUES (483, '5', '黑名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:56:57', 1722329760000);
INSERT INTO `monitor_proxy_limit_log` VALUES (484, '5', '黑名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:57:50', 1722329820000);
INSERT INTO `monitor_proxy_limit_log` VALUES (485, '5', 'IP限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:57:50', 1722329820000);
INSERT INTO `monitor_proxy_limit_log` VALUES (486, '5', '地址限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:57:50', 1722329820000);
INSERT INTO `monitor_proxy_limit_log` VALUES (487, '5', '白名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:57:50', 1722329820000);
INSERT INTO `monitor_proxy_limit_log` VALUES (488, '5', '地址限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:58:29', 1722329880000);
INSERT INTO `monitor_proxy_limit_log` VALUES (489, '5', '白名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:58:29', 1722329880000);
INSERT INTO `monitor_proxy_limit_log` VALUES (490, '5', 'IP限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:58:29', 1722329880000);
INSERT INTO `monitor_proxy_limit_log` VALUES (491, '5', '黑名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:58:29', 1722329880000);
INSERT INTO `monitor_proxy_limit_log` VALUES (492, '5', '地址限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:58:50', 1722329880000);
INSERT INTO `monitor_proxy_limit_log` VALUES (493, '5', 'IP限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:58:50', 1722329880000);
INSERT INTO `monitor_proxy_limit_log` VALUES (494, '5', '黑名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:58:50', 1722329880000);
INSERT INTO `monitor_proxy_limit_log` VALUES (495, '5', '白名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 16:58:50', 1722329880000);
INSERT INTO `monitor_proxy_limit_log` VALUES (496, '5', '黑名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:00:58', 1722330000000);
INSERT INTO `monitor_proxy_limit_log` VALUES (497, '5', 'IP限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:00:58', 1722330000000);
INSERT INTO `monitor_proxy_limit_log` VALUES (498, '5', '白名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:00:58', 1722330000000);
INSERT INTO `monitor_proxy_limit_log` VALUES (499, '5', '地址限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:00:58', 1722330000000);
INSERT INTO `monitor_proxy_limit_log` VALUES (500, '5', '白名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:01:14', 1722330060000);
INSERT INTO `monitor_proxy_limit_log` VALUES (501, '5', 'IP限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:01:14', 1722330060000);
INSERT INTO `monitor_proxy_limit_log` VALUES (502, '5', '黑名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:01:14', 1722330060000);
INSERT INTO `monitor_proxy_limit_log` VALUES (503, '5', '地址限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:01:14', 1722330060000);
INSERT INTO `monitor_proxy_limit_log` VALUES (504, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:01:20', 1722330060000);
INSERT INTO `monitor_proxy_limit_log` VALUES (505, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:01:20', 1722330060000);
INSERT INTO `monitor_proxy_limit_log` VALUES (506, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:01:20', 1722330060000);
INSERT INTO `monitor_proxy_limit_log` VALUES (507, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:01:20', 1722330060000);
INSERT INTO `monitor_proxy_limit_log` VALUES (508, '5', '黑名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:01:25', 1722330060000);
INSERT INTO `monitor_proxy_limit_log` VALUES (509, '5', '白名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:01:25', 1722330060000);
INSERT INTO `monitor_proxy_limit_log` VALUES (510, '5', '地址限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:01:25', 1722330060000);
INSERT INTO `monitor_proxy_limit_log` VALUES (511, '5', 'IP限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:01:25', 1722330060000);
INSERT INTO `monitor_proxy_limit_log` VALUES (512, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:01:59', 1722330060000);
INSERT INTO `monitor_proxy_limit_log` VALUES (513, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:01:59', 1722330060000);
INSERT INTO `monitor_proxy_limit_log` VALUES (514, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:01:59', 1722330060000);
INSERT INTO `monitor_proxy_limit_log` VALUES (515, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:01:59', 1722330060000);
INSERT INTO `monitor_proxy_limit_log` VALUES (516, '5', '地址限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:02:01', 1722330120000);
INSERT INTO `monitor_proxy_limit_log` VALUES (517, '5', '白名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:02:01', 1722330120000);
INSERT INTO `monitor_proxy_limit_log` VALUES (518, '5', '黑名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:02:01', 1722330120000);
INSERT INTO `monitor_proxy_limit_log` VALUES (519, '5', 'IP限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:02:01', 1722330120000);
INSERT INTO `monitor_proxy_limit_log` VALUES (520, '5', 'IP限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:02:11', 1722330120000);
INSERT INTO `monitor_proxy_limit_log` VALUES (521, '5', '白名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:02:11', 1722330120000);
INSERT INTO `monitor_proxy_limit_log` VALUES (522, '5', '黑名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:02:11', 1722330120000);
INSERT INTO `monitor_proxy_limit_log` VALUES (523, '5', '地址限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:02:11', 1722330120000);
INSERT INTO `monitor_proxy_limit_log` VALUES (524, '5', '黑名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:02:17', 1722330120000);
INSERT INTO `monitor_proxy_limit_log` VALUES (525, '5', '白名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:02:17', 1722330120000);
INSERT INTO `monitor_proxy_limit_log` VALUES (526, '5', 'IP限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:02:17', 1722330120000);
INSERT INTO `monitor_proxy_limit_log` VALUES (527, '5', '地址限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:02:17', 1722330120000);
INSERT INTO `monitor_proxy_limit_log` VALUES (528, '5', '白名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:04:29', 1722330240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (529, '5', '地址限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:04:29', 1722330240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (530, '5', 'IP限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:04:29', 1722330240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (531, '5', '黑名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:04:29', 1722330240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (532, '5', '黑名单', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:04:31', 1722330240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (533, '5', '地址限流', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:04:31', 1722330240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (534, '5', 'IP限流', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:04:31', 1722330240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (535, '5', '白名单', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:04:31', 1722330240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (536, '5', 'IP限流', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:04:31', 1722330240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (537, '5', 'IP限流', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:04:31', 1722330240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (538, '5', '白名单', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:04:31', 1722330240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (539, '5', '黑名单', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:04:31', 1722330240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (540, '5', '黑名单', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:04:31', 1722330240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (541, '5', '白名单', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:04:31', 1722330240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (542, '5', '地址限流', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:04:31', 1722330240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (543, '5', '地址限流', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:04:31', 1722330240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (544, '5', 'IP限流', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:04:31', 1722330240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (545, '5', '白名单', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:04:31', 1722330240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (546, '5', '地址限流', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:04:31', 1722330240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (547, '5', '黑名单', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:04:31', 1722330240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (548, '5', '黑名单', '/zxb/school/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:04:32', 1722330240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (549, '5', 'IP限流', '/zxb/school/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:04:32', 1722330240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (550, '5', '白名单', '/zxb/school/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:04:32', 1722330240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (551, '5', '地址限流', '/zxb/school/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:04:32', 1722330240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (552, '5', '白名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:04:46', 1722330240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (553, '5', '地址限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:04:46', 1722330240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (554, '5', 'IP限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:04:46', 1722330240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (555, '5', '黑名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:04:46', 1722330240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (556, '5', '黑名单', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:04:52', 1722330240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (557, '5', '黑名单', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:04:52', 1722330240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (558, '5', '地址限流', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:04:52', 1722330240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (559, '5', '白名单', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:04:52', 1722330240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (560, '5', 'IP限流', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:04:52', 1722330240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (561, '5', '白名单', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:04:52', 1722330240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (562, '5', '地址限流', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:04:52', 1722330240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (563, '5', '白名单', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:04:52', 1722330240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (564, '5', '地址限流', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:04:52', 1722330240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (565, '5', '黑名单', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:04:52', 1722330240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (566, '5', 'IP限流', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:04:52', 1722330240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (567, '5', 'IP限流', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:04:52', 1722330240000);
INSERT INTO `monitor_proxy_limit_log` VALUES (568, '5', 'IP限流', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:36', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (569, '5', '白名单', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:36', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (570, '5', '地址限流', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:36', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (571, '5', '黑名单', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:36', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (572, '5', 'IP限流', '/zxb/school/assets/antd.5ee291b2.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:39', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (573, '5', '白名单', '/zxb/school/assets/antd.5ee291b2.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:39', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (574, '5', 'IP限流', '/zxb/school/assets/components.b0153bba.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:39', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (575, '5', '黑名单', '/zxb/school/assets/antd.5ee291b2.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:39', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (576, '5', '白名单', '/zxb/school/assets/components.b0153bba.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:39', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (577, '5', '地址限流', '/zxb/school/assets/components.b0153bba.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:39', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (578, '5', '黑名单', '/zxb/school/assets/components.b0153bba.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:39', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (579, '5', '地址限流', '/zxb/school/assets/antd.5ee291b2.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:39', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (580, '5', 'IP限流', '/zxb/school/assets/index.5c0e3cc4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:40', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (581, '5', '黑名单', '/zxb/school/assets/index.2318c6a3.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:40', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (582, '5', '地址限流', '/zxb/school/assets/index.5c0e3cc4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:40', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (583, '5', '地址限流', '/zxb/school/assets/index.2318c6a3.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:40', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (584, '5', '黑名单', '/zxb/school/assets/index.5c0e3cc4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:40', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (585, '5', '白名单', '/zxb/school/assets/index.5c0e3cc4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:40', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (586, '5', 'IP限流', '/zxb/school/assets/index.2318c6a3.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:40', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (587, '5', '白名单', '/zxb/school/assets/index.2318c6a3.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:40', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (588, '5', '白名单', '/zxb/school/assets/index.f80df554.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:40', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (589, '5', '地址限流', '/zxb/school/assets/index.f80df554.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:40', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (590, '5', 'IP限流', '/zxb/school/assets/index.f80df554.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:40', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (591, '5', '黑名单', '/zxb/school/assets/index.f80df554.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:40', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (592, '5', '白名单', '/zxb/school/assets/loginBg.4fcda629.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:40', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (593, '5', '黑名单', '/zxb/school/assets/loginBg.4fcda629.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:40', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (594, '5', '地址限流', '/zxb/school/assets/loginBg.4fcda629.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:40', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (595, '5', 'IP限流', '/zxb/school/assets/loginBg.4fcda629.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:40', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (596, '5', '白名单', '/zxb/school/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:40', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (597, '5', 'IP限流', '/zxb/school/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:40', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (598, '5', '黑名单', '/zxb/school/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:40', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (599, '5', '地址限流', '/zxb/school/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:40', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (600, '5', 'IP限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:42', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (601, '5', '白名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:42', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (602, '5', '地址限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:42', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (603, '5', '黑名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:42', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (604, '5', '地址限流', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:42', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (605, '5', '黑名单', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:42', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (606, '5', 'IP限流', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:42', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (607, '5', 'IP限流', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:42', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (608, '5', '白名单', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:42', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (609, '5', '白名单', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:42', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (610, '5', '地址限流', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:42', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (611, '5', '黑名单', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:42', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (612, '5', '白名单', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:42', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (613, '5', '地址限流', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:42', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (614, '5', '黑名单', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:42', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (615, '5', '黑名单', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:42', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (616, '5', 'IP限流', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:42', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (617, '5', '白名单', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:42', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (618, '5', '地址限流', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:42', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (619, '5', 'IP限流', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:42', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (620, '5', '地址限流', '/zxb/school/bundle.tracing.min.js.map', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:42', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (621, '5', '白名单', '/zxb/school/bundle.tracing.min.js.map', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:42', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (622, '5', '黑名单', '/zxb/school/bundle.tracing.min.js.map', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:42', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (623, '5', 'IP限流', '/zxb/school/bundle.tracing.min.js.map', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:42', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (624, '5', '黑名单', '/zxb/school/assets/components.b0153bba.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:43', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (625, '5', '白名单', '/zxb/school/assets/components.b0153bba.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:43', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (626, '5', '黑名单', '/zxb/school/assets/antd.5ee291b2.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:43', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (627, '5', 'IP限流', '/zxb/school/assets/antd.5ee291b2.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:43', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (628, '5', '地址限流', '/zxb/school/assets/antd.5ee291b2.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:43', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (629, '5', '白名单', '/zxb/school/assets/antd.5ee291b2.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:43', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (630, '5', 'IP限流', '/zxb/school/assets/components.b0153bba.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:43', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (631, '5', '地址限流', '/zxb/school/assets/components.b0153bba.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:43', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (632, '5', '白名单', '/zxb/school/assets/index.5c0e3cc4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:43', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (633, '5', '地址限流', '/zxb/school/assets/index.2318c6a3.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:43', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (634, '5', '地址限流', '/zxb/school/assets/index.5c0e3cc4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:43', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (635, '5', '黑名单', '/zxb/school/assets/index.2318c6a3.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:43', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (636, '5', 'IP限流', '/zxb/school/assets/index.f80df554.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:43', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (637, '5', 'IP限流', '/zxb/school/assets/index.2318c6a3.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:43', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (638, '5', '黑名单', '/zxb/school/assets/index.5c0e3cc4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:43', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (639, '5', 'IP限流', '/zxb/school/assets/index.5c0e3cc4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:43', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (640, '5', '黑名单', '/zxb/school/assets/index.f80df554.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:43', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (641, '5', '白名单', '/zxb/school/assets/index.2318c6a3.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:43', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (642, '5', '地址限流', '/zxb/school/assets/index.f80df554.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:43', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (643, '5', '白名单', '/zxb/school/assets/index.f80df554.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:43', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (644, '5', '黑名单', '/zxb/school/assets/loginBg.4fcda629.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:43', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (645, '5', '地址限流', '/zxb/school/assets/loginBg.4fcda629.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:43', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (646, '5', '白名单', '/zxb/school/assets/loginBg.4fcda629.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:43', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (647, '5', 'IP限流', '/zxb/school/assets/loginBg.4fcda629.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:43', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (648, '5', '黑名单', '/zxb/school/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:43', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (649, '5', 'IP限流', '/zxb/school/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:43', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (650, '5', '白名单', '/zxb/school/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:43', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (651, '5', '地址限流', '/zxb/school/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:43', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (652, '5', 'IP限流', '/zxb/school/assets/school.694a1ada.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:56', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (653, '5', '白名单', '/zxb/school/assets/school.694a1ada.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:56', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (654, '5', '地址限流', '/zxb/school/assets/school.694a1ada.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:56', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (655, '5', '黑名单', '/zxb/school/assets/school.694a1ada.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:56', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (656, '5', '黑名单', '/zxb/school/assets/index.66049ef6.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (657, '5', 'IP限流', '/zxb/school/assets/index.66049ef6.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (658, '5', 'IP限流', '/zxb/school/assets/index.24e39ce6.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (659, '5', '白名单', '/zxb/school/assets/index.24e39ce6.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (660, '5', 'IP限流', '/zxb/school/assets/index.715e18aa.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (661, '5', '白名单', '/zxb/school/assets/index.715e18aa.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (662, '5', '黑名单', '/zxb/school/assets/index.715e18aa.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (663, '5', '地址限流', '/zxb/school/assets/index.715e18aa.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (664, '5', '地址限流', '/zxb/school/assets/index.66049ef6.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (665, '5', '白名单', '/zxb/school/assets/index.66049ef6.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (666, '5', '白名单', '/zxb/school/assets/index.c74f04a0.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (667, '5', 'IP限流', '/zxb/school/assets/index.c74f04a0.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (668, '5', '地址限流', '/zxb/school/assets/index.24e39ce6.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (669, '5', '白名单', '/zxb/school/assets/index.a974acfa.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (670, '5', 'IP限流', '/zxb/school/assets/index.a974acfa.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (671, '5', '黑名单', '/zxb/school/assets/index.c74f04a0.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (672, '5', '黑名单', '/zxb/school/assets/index.24e39ce6.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (673, '5', '地址限流', '/zxb/school/assets/index.a974acfa.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (674, '5', '黑名单', '/zxb/school/assets/index.a974acfa.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (675, '5', '地址限流', '/zxb/school/assets/index.c74f04a0.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (676, '5', '黑名单', '/zxb/school/assets/exportjs.f18efa7b.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (677, '5', '地址限流', '/zxb/school/assets/exportjs.f18efa7b.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (678, '5', '白名单', '/zxb/school/assets/exportjs.f18efa7b.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (679, '5', 'IP限流', '/zxb/school/assets/exportjs.f18efa7b.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (680, '5', '黑名单', '/zxb/school/assets/dispositionUrl.8959524c.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (681, '5', '白名单', '/zxb/school/assets/dispositionUrl.8959524c.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (682, '5', 'IP限流', '/zxb/school/assets/dispositionUrl.8959524c.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (683, '5', '地址限流', '/zxb/school/assets/dispositionUrl.8959524c.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (684, '5', '黑名单', '/zxb/school/assets/index.f971040c.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (685, '5', '白名单', '/zxb/school/assets/index.f971040c.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (686, '5', 'IP限流', '/zxb/school/assets/index.f971040c.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (687, '5', '地址限流', '/zxb/school/assets/index.f971040c.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (688, '5', '白名单', '/zxb/school/assets/exportjsxls.64d2fc39.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (689, '5', '黑名单', '/zxb/school/assets/exportjsxls.64d2fc39.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (690, '5', 'IP限流', '/zxb/school/assets/exportjsxls.64d2fc39.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (691, '5', 'IP限流', '/zxb/school/assets/use-search.c9c11a05.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (692, '5', '地址限流', '/zxb/school/assets/exportjsxls.64d2fc39.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (693, '5', '白名单', '/zxb/school/assets/use-search.c9c11a05.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (694, '5', '地址限流', '/zxb/school/assets/use-search.c9c11a05.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (695, '5', '黑名单', '/zxb/school/assets/use-search.c9c11a05.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (696, '5', '白名单', '/zxb/school/assets/index.a75202a9.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (697, '5', 'IP限流', '/zxb/school/assets/index.a75202a9.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (698, '5', '地址限流', '/zxb/school/assets/index.a75202a9.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (699, '5', '黑名单', '/zxb/school/assets/index.a75202a9.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:58', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (700, '5', '黑名单', '/zxb/school/assets/school.694a1ada.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:59', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (701, '5', 'IP限流', '/zxb/school/assets/school.694a1ada.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:59', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (702, '5', '白名单', '/zxb/school/assets/school.694a1ada.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:59', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (703, '5', '地址限流', '/zxb/school/assets/school.694a1ada.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:05:59', 1722330300000);
INSERT INTO `monitor_proxy_limit_log` VALUES (704, '5', '地址限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:06:37', 1722330360000);
INSERT INTO `monitor_proxy_limit_log` VALUES (705, '5', 'IP限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:06:37', 1722330360000);
INSERT INTO `monitor_proxy_limit_log` VALUES (706, '5', '黑名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:06:37', 1722330360000);
INSERT INTO `monitor_proxy_limit_log` VALUES (707, '5', '白名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:06:37', 1722330360000);
INSERT INTO `monitor_proxy_limit_log` VALUES (708, '5', 'IP限流', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:06:37', 1722330360000);
INSERT INTO `monitor_proxy_limit_log` VALUES (709, '5', '地址限流', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:06:37', 1722330360000);
INSERT INTO `monitor_proxy_limit_log` VALUES (710, '5', '白名单', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:06:37', 1722330360000);
INSERT INTO `monitor_proxy_limit_log` VALUES (711, '5', '黑名单', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:06:37', 1722330360000);
INSERT INTO `monitor_proxy_limit_log` VALUES (712, '5', 'IP限流', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:06:37', 1722330360000);
INSERT INTO `monitor_proxy_limit_log` VALUES (713, '5', '地址限流', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:06:37', 1722330360000);
INSERT INTO `monitor_proxy_limit_log` VALUES (714, '5', '黑名单', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:06:37', 1722330360000);
INSERT INTO `monitor_proxy_limit_log` VALUES (715, '5', '黑名单', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:06:37', 1722330360000);
INSERT INTO `monitor_proxy_limit_log` VALUES (716, '5', '白名单', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:06:37', 1722330360000);
INSERT INTO `monitor_proxy_limit_log` VALUES (717, '5', 'IP限流', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:06:37', 1722330360000);
INSERT INTO `monitor_proxy_limit_log` VALUES (718, '5', '白名单', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:06:37', 1722330360000);
INSERT INTO `monitor_proxy_limit_log` VALUES (719, '5', '地址限流', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:06:37', 1722330360000);
INSERT INTO `monitor_proxy_limit_log` VALUES (720, '5', '黑名单', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:06:37', 1722330360000);
INSERT INTO `monitor_proxy_limit_log` VALUES (721, '5', '白名单', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:06:37', 1722330360000);
INSERT INTO `monitor_proxy_limit_log` VALUES (722, '5', '地址限流', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:06:37', 1722330360000);
INSERT INTO `monitor_proxy_limit_log` VALUES (723, '5', 'IP限流', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:06:37', 1722330360000);
INSERT INTO `monitor_proxy_limit_log` VALUES (724, '5', '黑名单', '/zxb/school/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:06:37', 1722330360000);
INSERT INTO `monitor_proxy_limit_log` VALUES (725, '5', 'IP限流', '/zxb/school/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:06:37', 1722330360000);
INSERT INTO `monitor_proxy_limit_log` VALUES (726, '5', '地址限流', '/zxb/school/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:06:37', 1722330360000);
INSERT INTO `monitor_proxy_limit_log` VALUES (727, '5', '白名单', '/zxb/school/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:06:37', 1722330360000);
INSERT INTO `monitor_proxy_limit_log` VALUES (728, '5', '白名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:06:43', 1722330360000);
INSERT INTO `monitor_proxy_limit_log` VALUES (729, '5', '地址限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:06:43', 1722330360000);
INSERT INTO `monitor_proxy_limit_log` VALUES (730, '5', '黑名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:06:43', 1722330360000);
INSERT INTO `monitor_proxy_limit_log` VALUES (731, '5', 'IP限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:06:43', 1722330360000);
INSERT INTO `monitor_proxy_limit_log` VALUES (732, '5', '黑名单', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:07:33', 1722330420000);
INSERT INTO `monitor_proxy_limit_log` VALUES (733, '5', '地址限流', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:07:33', 1722330420000);
INSERT INTO `monitor_proxy_limit_log` VALUES (734, '5', '地址限流', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:07:33', 1722330420000);
INSERT INTO `monitor_proxy_limit_log` VALUES (735, '5', '白名单', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:07:33', 1722330420000);
INSERT INTO `monitor_proxy_limit_log` VALUES (736, '5', '白名单', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:07:33', 1722330420000);
INSERT INTO `monitor_proxy_limit_log` VALUES (737, '5', '白名单', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:07:33', 1722330420000);
INSERT INTO `monitor_proxy_limit_log` VALUES (738, '5', '地址限流', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:07:33', 1722330420000);
INSERT INTO `monitor_proxy_limit_log` VALUES (739, '5', 'IP限流', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:07:33', 1722330420000);
INSERT INTO `monitor_proxy_limit_log` VALUES (740, '5', 'IP限流', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:07:33', 1722330420000);
INSERT INTO `monitor_proxy_limit_log` VALUES (741, '5', '黑名单', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:07:33', 1722330420000);
INSERT INTO `monitor_proxy_limit_log` VALUES (742, '5', 'IP限流', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:07:33', 1722330420000);
INSERT INTO `monitor_proxy_limit_log` VALUES (743, '5', '黑名单', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:07:33', 1722330420000);
INSERT INTO `monitor_proxy_limit_log` VALUES (744, '5', '白名单', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:07:35', 1722330420000);
INSERT INTO `monitor_proxy_limit_log` VALUES (745, '5', 'IP限流', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:07:35', 1722330420000);
INSERT INTO `monitor_proxy_limit_log` VALUES (746, '5', '地址限流', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:07:35', 1722330420000);
INSERT INTO `monitor_proxy_limit_log` VALUES (747, '5', '黑名单', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:07:35', 1722330420000);
INSERT INTO `monitor_proxy_limit_log` VALUES (748, '5', '白名单', '/zxb/school/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:07:37', 1722330420000);
INSERT INTO `monitor_proxy_limit_log` VALUES (749, '5', 'IP限流', '/zxb/school/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:07:37', 1722330420000);
INSERT INTO `monitor_proxy_limit_log` VALUES (750, '5', '地址限流', '/zxb/school/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:07:37', 1722330420000);
INSERT INTO `monitor_proxy_limit_log` VALUES (751, '5', '黑名单', '/zxb/school/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:07:37', 1722330420000);
INSERT INTO `monitor_proxy_limit_log` VALUES (752, '5', '黑名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:37', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (753, '5', 'IP限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:37', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (754, '5', '地址限流', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:37', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (755, '5', '白名单', '/zxb/school', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:37', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (756, '5', 'IP限流', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:37', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (757, '5', '白名单', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:37', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (758, '5', '地址限流', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:37', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (759, '5', '黑名单', '/zxb/school/bundle.tracing.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:37', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (760, '5', '地址限流', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:37', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (761, '5', 'IP限流', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:37', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (762, '5', '白名单', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:37', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (763, '5', '黑名单', '/zxb/school/lib/base64/base64.min.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:37', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (764, '5', '白名单', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:37', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (765, '5', '黑名单', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:37', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (766, '5', '地址限流', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:37', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (767, '5', '白名单', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:37', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (768, '5', 'IP限流', '/zxb/school/assets/index.2560c1d4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:37', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (769, '5', 'IP限流', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:37', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (770, '5', '地址限流', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:37', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (771, '5', '黑名单', '/zxb/school/assets/index.1698ba38.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:37', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (772, '5', '黑名单', '/zxb/school/bundle.tracing.min.js.map', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:37', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (773, '5', '白名单', '/zxb/school/bundle.tracing.min.js.map', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:37', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (774, '5', 'IP限流', '/zxb/school/bundle.tracing.min.js.map', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:37', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (775, '5', '地址限流', '/zxb/school/bundle.tracing.min.js.map', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:37', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (776, '5', 'IP限流', '/zxb/school/assets/antd.5ee291b2.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (777, '5', '地址限流', '/zxb/school/assets/antd.5ee291b2.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (778, '5', 'IP限流', '/zxb/school/assets/components.b0153bba.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (779, '5', '白名单', '/zxb/school/assets/antd.5ee291b2.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (780, '5', '黑名单', '/zxb/school/assets/components.b0153bba.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (781, '5', '地址限流', '/zxb/school/assets/components.b0153bba.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (782, '5', '黑名单', '/zxb/school/assets/antd.5ee291b2.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (783, '5', '白名单', '/zxb/school/assets/components.b0153bba.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (784, '5', '黑名单', '/zxb/school/assets/index.715e18aa.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (785, '5', '白名单', '/zxb/school/assets/index.66049ef6.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (786, '5', '地址限流', '/zxb/school/assets/index.715e18aa.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (787, '5', 'IP限流', '/zxb/school/assets/index.715e18aa.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (788, '5', '白名单', '/zxb/school/assets/index.715e18aa.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (789, '5', 'IP限流', '/zxb/school/assets/index.66049ef6.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (790, '5', '地址限流', '/zxb/school/assets/index.66049ef6.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (791, '5', '黑名单', '/zxb/school/assets/index.66049ef6.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (792, '5', 'IP限流', '/zxb/school/assets/index.24e39ce6.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (793, '5', '白名单', '/zxb/school/assets/index.c74f04a0.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (794, '5', 'IP限流', '/zxb/school/assets/index.c74f04a0.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (795, '5', '黑名单', '/zxb/school/assets/index.c74f04a0.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (796, '5', '白名单', '/zxb/school/assets/index.24e39ce6.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (797, '5', '地址限流', '/zxb/school/assets/index.c74f04a0.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (798, '5', '黑名单', '/zxb/school/assets/index.a974acfa.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (799, '5', '黑名单', '/zxb/school/assets/index.24e39ce6.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (800, '5', '地址限流', '/zxb/school/assets/index.24e39ce6.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (801, '5', 'IP限流', '/zxb/school/assets/index.a974acfa.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (802, '5', '白名单', '/zxb/school/assets/index.a974acfa.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (803, '5', '地址限流', '/zxb/school/assets/index.a974acfa.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (804, '5', '黑名单', '/zxb/school/assets/exportjs.f18efa7b.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (805, '5', '地址限流', '/zxb/school/assets/exportjs.f18efa7b.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (806, '5', 'IP限流', '/zxb/school/assets/exportjs.f18efa7b.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (807, '5', '白名单', '/zxb/school/assets/exportjs.f18efa7b.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (808, '5', '黑名单', '/zxb/school/assets/dispositionUrl.8959524c.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (809, '5', '白名单', '/zxb/school/assets/dispositionUrl.8959524c.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (810, '5', '地址限流', '/zxb/school/assets/dispositionUrl.8959524c.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (811, '5', 'IP限流', '/zxb/school/assets/dispositionUrl.8959524c.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (812, '5', '白名单', '/zxb/school/assets/index.f971040c.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (813, '5', 'IP限流', '/zxb/school/assets/exportjsxls.64d2fc39.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (814, '5', '黑名单', '/zxb/school/assets/index.f971040c.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (815, '5', '地址限流', '/zxb/school/assets/index.f971040c.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (816, '5', '黑名单', '/zxb/school/assets/exportjsxls.64d2fc39.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (817, '5', 'IP限流', '/zxb/school/assets/index.f971040c.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (818, '5', '白名单', '/zxb/school/assets/exportjsxls.64d2fc39.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (819, '5', '地址限流', '/zxb/school/assets/exportjsxls.64d2fc39.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (820, '5', '黑名单', '/zxb/school/assets/use-search.c9c11a05.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (821, '5', '地址限流', '/zxb/school/assets/use-search.c9c11a05.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (822, '5', '白名单', '/zxb/school/assets/use-search.c9c11a05.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (823, '5', 'IP限流', '/zxb/school/assets/use-search.c9c11a05.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (824, '5', '白名单', '/zxb/school/assets/index.a75202a9.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (825, '5', 'IP限流', '/zxb/school/assets/index.a75202a9.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (826, '5', '地址限流', '/zxb/school/assets/index.a75202a9.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (827, '5', '黑名单', '/zxb/school/assets/index.a75202a9.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:38', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (828, '5', '黑名单', '/zxb/school/assets/school.694a1ada.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:39', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (829, '5', 'IP限流', '/zxb/school/assets/school.694a1ada.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:39', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (830, '5', '白名单', '/zxb/school/assets/school.694a1ada.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:39', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (831, '5', '地址限流', '/zxb/school/assets/school.694a1ada.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:39', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (832, '5', '地址限流', '/zxb/school/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:39', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (833, '5', '白名单', '/zxb/school/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:39', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (834, '5', 'IP限流', '/zxb/school/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:39', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (835, '5', '黑名单', '/zxb/school/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:39', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (836, '5', '白名单', '/zxb/school/assets/index.99bad2d0.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:40', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (837, '5', '地址限流', '/zxb/school/assets/index.99bad2d0.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:40', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (838, '5', '黑名单', '/zxb/school/assets/index.47d09ddf.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:40', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (839, '5', '白名单', '/zxb/school/assets/index.7935c2e8.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:40', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (840, '5', '白名单', '/zxb/school/assets/selectList.04affd17.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:40', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (841, '5', '黑名单', '/zxb/school/assets/index.7935c2e8.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:40', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (842, '5', '白名单', '/zxb/school/assets/index.47d09ddf.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:40', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (843, '5', 'IP限流', '/zxb/school/assets/index.99bad2d0.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:40', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (844, '5', '黑名单', '/zxb/school/assets/index.99bad2d0.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:40', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (845, '5', 'IP限流', '/zxb/school/assets/selectList.04affd17.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:40', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (846, '5', '黑名单', '/zxb/school/assets/exportFile.6e01bc24.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:40', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (847, '5', 'IP限流', '/zxb/school/assets/index.7935c2e8.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:40', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (848, '5', '白名单', '/zxb/school/assets/exportFile.6e01bc24.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:40', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (849, '5', 'IP限流', '/zxb/school/assets/index.47d09ddf.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:40', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (850, '5', '黑名单', '/zxb/school/assets/selectList.04affd17.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:40', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (851, '5', 'IP限流', '/zxb/school/assets/exportFile.6e01bc24.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:40', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (852, '5', '地址限流', '/zxb/school/assets/index.7935c2e8.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:40', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (853, '5', '地址限流', '/zxb/school/assets/selectList.04affd17.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:40', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (854, '5', '地址限流', '/zxb/school/assets/exportFile.6e01bc24.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:40', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (855, '5', '地址限流', '/zxb/school/assets/index.47d09ddf.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:40', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (856, '5', 'IP限流', '/zxb/school/assets/school.694a1ada.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:40', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (857, '5', '地址限流', '/zxb/school/assets/school.694a1ada.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:40', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (858, '5', '黑名单', '/zxb/school/assets/school.694a1ada.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:40', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (859, '5', '白名单', '/zxb/school/assets/school.694a1ada.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:40', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (860, '5', '白名单', '/zxb/school/assets/index.57c15cf2.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:42', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (861, '5', '白名单', '/zxb/school/assets/index.2eb83e71.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:42', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (862, '5', 'IP限流', '/zxb/school/assets/index.2eb83e71.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:42', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (863, '5', 'IP限流', '/zxb/school/assets/index.57c15cf2.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:42', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (864, '5', '地址限流', '/zxb/school/assets/index.57c15cf2.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:42', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (865, '5', '地址限流', '/zxb/school/assets/index.2eb83e71.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:42', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (866, '5', '黑名单', '/zxb/school/assets/index.2eb83e71.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:42', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (867, '5', '黑名单', '/zxb/school/assets/index.57c15cf2.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:42', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (868, '5', 'IP限流', '/zxb/school/assets/school.694a1ada.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:42', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (869, '5', '白名单', '/zxb/school/assets/school.694a1ada.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:42', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (870, '5', '地址限流', '/zxb/school/assets/school.694a1ada.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:42', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (871, '5', '黑名单', '/zxb/school/assets/school.694a1ada.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:42', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (872, '5', '黑名单', '/zxb/school/assets/index.c221fd6f.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:43', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (873, '5', '地址限流', '/zxb/school/assets/index.c221fd6f.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:43', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (874, '5', '地址限流', '/zxb/school/assets/index.c42027e4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:43', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (875, '5', '白名单', '/zxb/school/assets/index.c221fd6f.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:43', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (876, '5', 'IP限流', '/zxb/school/assets/index.c221fd6f.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:43', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (877, '5', '白名单', '/zxb/school/assets/index.c42027e4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:43', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (878, '5', 'IP限流', '/zxb/school/assets/index.c42027e4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:43', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (879, '5', '黑名单', '/zxb/school/assets/index.c42027e4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:43', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (880, '5', '黑名单', '/zxb/school/assets/marquee.vue_vue_type_style_index_0_scoped_true_lang.ad761336.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:43', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (881, '5', '地址限流', '/zxb/school/assets/marquee.vue_vue_type_style_index_0_scoped_true_lang.ad761336.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:43', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (882, '5', '白名单', '/zxb/school/assets/marquee.vue_vue_type_style_index_0_scoped_true_lang.ad761336.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:43', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (883, '5', 'IP限流', '/zxb/school/assets/marquee.vue_vue_type_style_index_0_scoped_true_lang.ad761336.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:43', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (884, '5', 'IP限流', '/zxb/school/assets/index.c9ee3ed4.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:43', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (885, '5', '黑名单', '/zxb/school/assets/index.c9ee3ed4.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:43', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (886, '5', '地址限流', '/zxb/school/assets/index.c9ee3ed4.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:43', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (887, '5', '黑名单', '/zxb/school/assets/index.8f890ceb.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:43', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (888, '5', '白名单', '/zxb/school/assets/index.8f890ceb.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:43', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (889, '5', '白名单', '/zxb/school/assets/index.c9ee3ed4.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:43', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (890, '5', '地址限流', '/zxb/school/assets/index.8f890ceb.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:43', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (891, '5', 'IP限流', '/zxb/school/assets/index.8f890ceb.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:43', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (892, '5', '白名单', '/zxb/school/assets/index.11feb1a9.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:43', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (893, '5', 'IP限流', '/zxb/school/assets/index.11feb1a9.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:43', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (894, '5', '地址限流', '/zxb/school/assets/index.11feb1a9.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:43', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (895, '5', '黑名单', '/zxb/school/assets/index.11feb1a9.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:43', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (896, '5', 'IP限流', '/zxb/school/assets/index.3f9a1760.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:43', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (897, '5', '白名单', '/zxb/school/assets/index.3f9a1760.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:43', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (898, '5', '黑名单', '/zxb/school/assets/index.3f9a1760.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:43', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (899, '5', '黑名单', '/zxb/school/assets/marquee.b08a5074.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:43', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (900, '5', '白名单', '/zxb/school/assets/marquee.b08a5074.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:43', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (901, '5', '地址限流', '/zxb/school/assets/index.3f9a1760.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:43', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (902, '5', 'IP限流', '/zxb/school/assets/marquee.b08a5074.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:43', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (903, '5', '地址限流', '/zxb/school/assets/marquee.b08a5074.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:43', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (904, '5', '黑名单', '/zxb/school/assets/school.694a1ada.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:44', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (905, '5', 'IP限流', '/zxb/school/assets/school.694a1ada.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:44', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (906, '5', '地址限流', '/zxb/school/assets/school.694a1ada.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:44', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (907, '5', '白名单', '/zxb/school/assets/school.694a1ada.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:44', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (908, '5', 'IP限流', '/zxb/school/assets/school.694a1ada.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:46', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (909, '5', '白名单', '/zxb/school/assets/school.694a1ada.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:46', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (910, '5', '地址限流', '/zxb/school/assets/school.694a1ada.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:46', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (911, '5', '黑名单', '/zxb/school/assets/school.694a1ada.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:09:46', 1722330540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (912, '5', '黑名单', '/baidu', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:10:42', 1722330600000);
INSERT INTO `monitor_proxy_limit_log` VALUES (913, '5', 'IP限流', '/baidu', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:10:42', 1722330600000);
INSERT INTO `monitor_proxy_limit_log` VALUES (914, '5', '地址限流', '/baidu', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:10:42', 1722330600000);
INSERT INTO `monitor_proxy_limit_log` VALUES (915, '5', '白名单', '/baidu', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:10:42', 1722330600000);
INSERT INTO `monitor_proxy_limit_log` VALUES (916, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:10:42', 1722330600000);
INSERT INTO `monitor_proxy_limit_log` VALUES (917, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:10:42', 1722330600000);
INSERT INTO `monitor_proxy_limit_log` VALUES (918, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:10:42', 1722330600000);
INSERT INTO `monitor_proxy_limit_log` VALUES (919, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:10:42', 1722330600000);
INSERT INTO `monitor_proxy_limit_log` VALUES (920, '5', 'IP限流', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:11:00', 1722330600000);
INSERT INTO `monitor_proxy_limit_log` VALUES (921, '5', '地址限流', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:11:00', 1722330600000);
INSERT INTO `monitor_proxy_limit_log` VALUES (922, '5', '黑名单', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:11:00', 1722330600000);
INSERT INTO `monitor_proxy_limit_log` VALUES (923, '5', '白名单', '/baidu/s?wd=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:11:00', 1722330600000);
INSERT INTO `monitor_proxy_limit_log` VALUES (924, '5', 'IP限流', '/sugrec?&prod=pc_his&from=pc_web&json=1&sid=60271_60360_60492_60501_60474_60475_60522_60552&hisdata=&_t=1722330661171&req=2&bs=1&csor=0', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:11:01', 1722330660000);
INSERT INTO `monitor_proxy_limit_log` VALUES (925, '5', '地址限流', '/sugrec?&prod=pc_his&from=pc_web&json=1&sid=60271_60360_60492_60501_60474_60475_60522_60552&hisdata=&_t=1722330661171&req=2&bs=1&csor=0', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:11:01', 1722330660000);
INSERT INTO `monitor_proxy_limit_log` VALUES (926, '5', '黑名单', '/sugrec?&prod=pc_his&from=pc_web&json=1&sid=60271_60360_60492_60501_60474_60475_60522_60552&hisdata=&_t=1722330661171&req=2&bs=1&csor=0', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:11:01', 1722330660000);
INSERT INTO `monitor_proxy_limit_log` VALUES (927, '5', '白名单', '/sugrec?&prod=pc_his&from=pc_web&json=1&sid=60271_60360_60492_60501_60474_60475_60522_60552&hisdata=&_t=1722330661171&req=2&bs=1&csor=0', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:11:01', 1722330660000);
INSERT INTO `monitor_proxy_limit_log` VALUES (928, '5', '黑名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=1&req=2&bs=1&csor=1&sugmode=2&hot_launch=0&cb=jQuery110205771704855766557_1722330660786&_=1722330660787', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:06', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (929, '5', '白名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=1&req=2&bs=1&csor=1&sugmode=2&hot_launch=0&cb=jQuery110205771704855766557_1722330660786&_=1722330660787', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:06', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (930, '5', '地址限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=1&req=2&bs=1&csor=1&sugmode=2&hot_launch=0&cb=jQuery110205771704855766557_1722330660786&_=1722330660787', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:06', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (931, '5', 'IP限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=1&req=2&bs=1&csor=1&sugmode=2&hot_launch=0&cb=jQuery110205771704855766557_1722330660786&_=1722330660787', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:06', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (932, '5', '黑名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=1&req=2&bs=1&csor=0&pwd=1&sugmode=2&hot_launch=0&cb=jQuery110205771704855766557_1722330660786&_=1722330660788', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:06', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (933, '5', 'IP限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=1&req=2&bs=1&csor=0&pwd=1&sugmode=2&hot_launch=0&cb=jQuery110205771704855766557_1722330660786&_=1722330660788', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:06', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (934, '5', '白名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=1&req=2&bs=1&csor=0&pwd=1&sugmode=2&hot_launch=0&cb=jQuery110205771704855766557_1722330660786&_=1722330660788', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:06', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (935, '5', '地址限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=1&req=2&bs=1&csor=0&pwd=1&sugmode=2&hot_launch=0&cb=jQuery110205771704855766557_1722330660786&_=1722330660788', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:06', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (936, '5', '黑名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=s&req=2&bs=1&csor=1&pwd=1&cb=jQuery110205771704855766557_1722330660786&_=1722330660789', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:07', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (937, '5', '地址限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=s&req=2&bs=1&csor=1&pwd=1&cb=jQuery110205771704855766557_1722330660786&_=1722330660789', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:07', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (938, '5', 'IP限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=s&req=2&bs=1&csor=1&pwd=1&cb=jQuery110205771704855766557_1722330660786&_=1722330660789', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:07', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (939, '5', '白名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=s&req=2&bs=1&csor=1&pwd=1&cb=jQuery110205771704855766557_1722330660786&_=1722330660789', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:07', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (940, '5', '地址限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=so&req=2&bs=1&csor=2&pwd=s&cb=jQuery110205771704855766557_1722330660786&_=1722330660790', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:07', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (941, '5', '黑名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=so&req=2&bs=1&csor=2&pwd=s&cb=jQuery110205771704855766557_1722330660786&_=1722330660790', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:07', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (942, '5', 'IP限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=so&req=2&bs=1&csor=2&pwd=s&cb=jQuery110205771704855766557_1722330660786&_=1722330660790', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:07', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (943, '5', '白名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=so&req=2&bs=1&csor=2&pwd=s&cb=jQuery110205771704855766557_1722330660786&_=1722330660790', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:07', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (944, '5', '白名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=sou&req=2&bs=1&csor=3&pwd=so&cb=jQuery110205771704855766557_1722330660786&_=1722330660791', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:08', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (945, '5', '黑名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=sou&req=2&bs=1&csor=3&pwd=so&cb=jQuery110205771704855766557_1722330660786&_=1722330660791', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:08', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (946, '5', '地址限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=sou&req=2&bs=1&csor=3&pwd=so&cb=jQuery110205771704855766557_1722330660786&_=1722330660791', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:08', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (947, '5', 'IP限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=sou&req=2&bs=1&csor=3&pwd=so&cb=jQuery110205771704855766557_1722330660786&_=1722330660791', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:08', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (948, '5', '白名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=soug&req=2&bs=1&csor=4&pwd=sou&cb=jQuery110205771704855766557_1722330660786&_=1722330660792', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:08', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (949, '5', '地址限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=soug&req=2&bs=1&csor=4&pwd=sou&cb=jQuery110205771704855766557_1722330660786&_=1722330660792', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:08', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (950, '5', 'IP限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=soug&req=2&bs=1&csor=4&pwd=sou&cb=jQuery110205771704855766557_1722330660786&_=1722330660792', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:08', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (951, '5', '黑名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=soug&req=2&bs=1&csor=4&pwd=sou&cb=jQuery110205771704855766557_1722330660786&_=1722330660792', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:08', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (952, '5', '白名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=sou&req=2&bs=1&csor=3&pwd=soug&cb=jQuery110205771704855766557_1722330660786&_=1722330660793', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:08', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (953, '5', 'IP限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=sou&req=2&bs=1&csor=3&pwd=soug&cb=jQuery110205771704855766557_1722330660786&_=1722330660793', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:08', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (954, '5', '地址限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=sou&req=2&bs=1&csor=3&pwd=soug&cb=jQuery110205771704855766557_1722330660786&_=1722330660793', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:08', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (955, '5', '黑名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=sou&req=2&bs=1&csor=3&pwd=soug&cb=jQuery110205771704855766557_1722330660786&_=1722330660793', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:08', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (956, '5', 'IP限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=so&req=2&bs=1&csor=2&pwd=sou&cb=jQuery110205771704855766557_1722330660786&_=1722330660794', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:08', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (957, '5', '白名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=so&req=2&bs=1&csor=2&pwd=sou&cb=jQuery110205771704855766557_1722330660786&_=1722330660794', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:08', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (958, '5', '黑名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=so&req=2&bs=1&csor=2&pwd=sou&cb=jQuery110205771704855766557_1722330660786&_=1722330660794', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:08', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (959, '5', '地址限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=so&req=2&bs=1&csor=2&pwd=sou&cb=jQuery110205771704855766557_1722330660786&_=1722330660794', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:08', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (960, '5', '黑名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=s&req=2&bs=1&csor=1&pwd=so&cb=jQuery110205771704855766557_1722330660786&_=1722330660795', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:08', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (961, '5', '白名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=s&req=2&bs=1&csor=1&pwd=so&cb=jQuery110205771704855766557_1722330660786&_=1722330660795', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:08', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (962, '5', '地址限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=s&req=2&bs=1&csor=1&pwd=so&cb=jQuery110205771704855766557_1722330660786&_=1722330660795', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:08', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (963, '5', 'IP限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=s&req=2&bs=1&csor=1&pwd=so&cb=jQuery110205771704855766557_1722330660786&_=1722330660795', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:08', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (964, '5', '地址限流', '/sugrec?&prod=pc_his&from=pc_web&json=1&sid=60271_60360_60492_60501_60474_60475_60522_60552&hisdata=&_t=1722330728403&req=2&bs=1&sc=eb&csor=0&pwd=s', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:08', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (965, '5', 'IP限流', '/sugrec?&prod=pc_his&from=pc_web&json=1&sid=60271_60360_60492_60501_60474_60475_60522_60552&hisdata=&_t=1722330728403&req=2&bs=1&sc=eb&csor=0&pwd=s', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:08', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (966, '5', '白名单', '/sugrec?&prod=pc_his&from=pc_web&json=1&sid=60271_60360_60492_60501_60474_60475_60522_60552&hisdata=&_t=1722330728403&req=2&bs=1&sc=eb&csor=0&pwd=s', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:08', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (967, '5', '黑名单', '/sugrec?&prod=pc_his&from=pc_web&json=1&sid=60271_60360_60492_60501_60474_60475_60522_60552&hisdata=&_t=1722330728403&req=2&bs=1&sc=eb&csor=0&pwd=s', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:08', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (968, '5', '黑名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=s&req=2&bs=1&csor=1&cb=jQuery110205771704855766557_1722330660786&_=1722330660796', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:09', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (969, '5', 'IP限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=s&req=2&bs=1&csor=1&cb=jQuery110205771704855766557_1722330660786&_=1722330660796', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:09', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (970, '5', '地址限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=s&req=2&bs=1&csor=1&cb=jQuery110205771704855766557_1722330660786&_=1722330660796', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:09', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (971, '5', '白名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=s&req=2&bs=1&csor=1&cb=jQuery110205771704855766557_1722330660786&_=1722330660796', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:09', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (972, '5', '黑名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=so&req=2&bs=1&csor=2&pwd=s&cb=jQuery110205771704855766557_1722330660786&_=1722330660797', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:09', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (973, '5', '地址限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=so&req=2&bs=1&csor=2&pwd=s&cb=jQuery110205771704855766557_1722330660786&_=1722330660797', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:09', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (974, '5', '白名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=so&req=2&bs=1&csor=2&pwd=s&cb=jQuery110205771704855766557_1722330660786&_=1722330660797', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:09', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (975, '5', 'IP限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=so&req=2&bs=1&csor=2&pwd=s&cb=jQuery110205771704855766557_1722330660786&_=1722330660797', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:09', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (976, '5', '白名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=sou&req=2&bs=1&csor=3&pwd=so&cb=jQuery110205771704855766557_1722330660786&_=1722330660798', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:09', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (977, '5', '黑名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=sou&req=2&bs=1&csor=3&pwd=so&cb=jQuery110205771704855766557_1722330660786&_=1722330660798', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:09', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (978, '5', '地址限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=sou&req=2&bs=1&csor=3&pwd=so&cb=jQuery110205771704855766557_1722330660786&_=1722330660798', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:09', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (979, '5', 'IP限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=sou&req=2&bs=1&csor=3&pwd=so&cb=jQuery110205771704855766557_1722330660786&_=1722330660798', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:09', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (980, '5', '白名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=sou%27g&req=2&bs=1&csor=5&pwd=sou&cb=jQuery110205771704855766557_1722330660786&_=1722330660799', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:09', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (981, '5', '地址限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=sou%27g&req=2&bs=1&csor=5&pwd=sou&cb=jQuery110205771704855766557_1722330660786&_=1722330660799', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:09', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (982, '5', '黑名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=sou%27g&req=2&bs=1&csor=5&pwd=sou&cb=jQuery110205771704855766557_1722330660786&_=1722330660799', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:09', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (983, '5', 'IP限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=sou%27g&req=2&bs=1&csor=5&pwd=sou&cb=jQuery110205771704855766557_1722330660786&_=1722330660799', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:09', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (984, '5', '白名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=sou%27go&req=2&bs=1&csor=6&pwd=sou%27g&cb=jQuery110205771704855766557_1722330660786&_=1722330660800', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:10', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (985, '5', '黑名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=sou%27go&req=2&bs=1&csor=6&pwd=sou%27g&cb=jQuery110205771704855766557_1722330660786&_=1722330660800', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:10', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (986, '5', '地址限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=sou%27go&req=2&bs=1&csor=6&pwd=sou%27g&cb=jQuery110205771704855766557_1722330660786&_=1722330660800', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:10', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (987, '5', 'IP限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=sou%27go&req=2&bs=1&csor=6&pwd=sou%27g&cb=jQuery110205771704855766557_1722330660786&_=1722330660800', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:10', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (988, '5', '黑名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=sou%27gou&req=2&bs=1&csor=7&pwd=sou%27go&cb=jQuery110205771704855766557_1722330660786&_=1722330660801', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:10', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (989, '5', 'IP限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=sou%27gou&req=2&bs=1&csor=7&pwd=sou%27go&cb=jQuery110205771704855766557_1722330660786&_=1722330660801', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:10', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (990, '5', '白名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=sou%27gou&req=2&bs=1&csor=7&pwd=sou%27go&cb=jQuery110205771704855766557_1722330660786&_=1722330660801', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:10', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (991, '5', '地址限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=sou%27gou&req=2&bs=1&csor=7&pwd=sou%27go&cb=jQuery110205771704855766557_1722330660786&_=1722330660801', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:10', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (992, '5', '黑名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=%E6%90%9C%E7%8B%97&req=2&bs=1&csor=2&pwd=sou%27gou&cb=jQuery110205771704855766557_1722330660786&_=1722330660802', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:10', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (993, '5', 'IP限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=%E6%90%9C%E7%8B%97&req=2&bs=1&csor=2&pwd=sou%27gou&cb=jQuery110205771704855766557_1722330660786&_=1722330660802', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:10', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (994, '5', '地址限流', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=%E6%90%9C%E7%8B%97&req=2&bs=1&csor=2&pwd=sou%27gou&cb=jQuery110205771704855766557_1722330660786&_=1722330660802', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:10', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (995, '5', '白名单', '/sugrec?pre=1&p=3&ie=utf-8&json=1&prod=pc&from=pc_web&sugsid=60271,60360,60492,60501,60474,60475,60522,60552&wd=%E6%90%9C%E7%8B%97&req=2&bs=1&csor=2&pwd=sou%27gou&cb=jQuery110205771704855766557_1722330660786&_=1722330660802', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:10', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (996, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:10', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (997, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:10', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (998, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:10', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (999, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:10', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1000, '5', '黑名单', '/sugrec?&prod=pc_his&from=pc_web&json=1&sid=60271_60360_60492_60501_60474_60475_60522_60552&hisdata=&_t=1722330739396&bs=1&csor=0', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:19', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1001, '5', '地址限流', '/sugrec?&prod=pc_his&from=pc_web&json=1&sid=60271_60360_60492_60501_60474_60475_60522_60552&hisdata=&_t=1722330739396&bs=1&csor=0', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:19', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1002, '5', '白名单', '/sugrec?&prod=pc_his&from=pc_web&json=1&sid=60271_60360_60492_60501_60474_60475_60522_60552&hisdata=&_t=1722330739396&bs=1&csor=0', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:19', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1003, '5', 'IP限流', '/sugrec?&prod=pc_his&from=pc_web&json=1&sid=60271_60360_60492_60501_60474_60475_60522_60552&hisdata=&_t=1722330739396&bs=1&csor=0', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:12:19', 1722330720000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1004, '5', '地址限流', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:26', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1005, '5', '白名单', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:26', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1006, '5', 'IP限流', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:26', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1007, '5', '黑名单', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:26', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1008, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:26', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1009, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:26', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1010, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:26', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1011, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:26', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1012, '5', '黑名单', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:29', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1013, '5', '地址限流', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:29', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1014, '5', '白名单', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:29', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1015, '5', 'IP限流', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:29', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1016, '5', '白名单', '/rp/VeYpfzSZtJYcjpVvfwiIaM1Zx2k.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1017, '5', '黑名单', '/rp/VeYpfzSZtJYcjpVvfwiIaM1Zx2k.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1018, '5', '地址限流', '/rp/VeYpfzSZtJYcjpVvfwiIaM1Zx2k.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1019, '5', 'IP限流', '/rp/VeYpfzSZtJYcjpVvfwiIaM1Zx2k.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1020, '5', '地址限流', '/rp/Xjqk15Ijg0OcYgnUt2a7NDG5S6Q.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1021, '5', 'IP限流', '/rp/Xjqk15Ijg0OcYgnUt2a7NDG5S6Q.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1022, '5', '黑名单', '/rp/Xjqk15Ijg0OcYgnUt2a7NDG5S6Q.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1023, '5', '白名单', '/rp/Xjqk15Ijg0OcYgnUt2a7NDG5S6Q.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1024, '5', '黑名单', '/rp/y_8Q_2aCPV7xMwmnkTxgDurroYc.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1025, '5', 'IP限流', '/rp/y_8Q_2aCPV7xMwmnkTxgDurroYc.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1026, '5', '地址限流', '/rp/y_8Q_2aCPV7xMwmnkTxgDurroYc.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1027, '5', '白名单', '/rp/y_8Q_2aCPV7xMwmnkTxgDurroYc.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1028, '5', '白名单', '/rp/B6jGHby7hXuEC7enS8xiNSUwqXw.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1029, '5', 'IP限流', '/rp/B6jGHby7hXuEC7enS8xiNSUwqXw.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1030, '5', '黑名单', '/rp/B6jGHby7hXuEC7enS8xiNSUwqXw.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1031, '5', '地址限流', '/rp/B6jGHby7hXuEC7enS8xiNSUwqXw.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1032, '5', 'IP限流', '/rp/2NggrJSqjy5oZmsCGtiHDPUSMyM.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1033, '5', '黑名单', '/rp/2NggrJSqjy5oZmsCGtiHDPUSMyM.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1034, '5', '白名单', '/rp/2NggrJSqjy5oZmsCGtiHDPUSMyM.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1035, '5', '地址限流', '/rp/2NggrJSqjy5oZmsCGtiHDPUSMyM.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1036, '5', '白名单', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1037, '5', '黑名单', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1038, '5', 'IP限流', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1039, '5', '白名单', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1040, '5', '黑名单', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1041, '5', '地址限流', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1042, '5', '地址限流', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1043, '5', 'IP限流', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1044, '5', '白名单', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1045, '5', '黑名单', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1046, '5', 'IP限流', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1047, '5', '地址限流', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1048, '5', '黑名单', '/fd/ls/l?IG=3D657E4FE11D437EB7FD4EECC92B19E6&Type=Event.ClientInst&DATA=[{%22T%22:%22Info%22,%22FID%22:%22CI%22,%22Name%22:%22ClientDimNotSent%22,%22Text%22:%221%22}]', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1049, '5', '地址限流', '/fd/ls/l?IG=3D657E4FE11D437EB7FD4EECC92B19E6&Type=Event.ClientInst&DATA=[{%22T%22:%22Info%22,%22FID%22:%22CI%22,%22Name%22:%22ClientDimNotSent%22,%22Text%22:%221%22}]', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1050, '5', 'IP限流', '/fd/ls/l?IG=3D657E4FE11D437EB7FD4EECC92B19E6&Type=Event.ClientInst&DATA=[{%22T%22:%22Info%22,%22FID%22:%22CI%22,%22Name%22:%22ClientDimNotSent%22,%22Text%22:%221%22}]', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1051, '5', '白名单', '/fd/ls/l?IG=3D657E4FE11D437EB7FD4EECC92B19E6&Type=Event.ClientInst&DATA=[{%22T%22:%22Info%22,%22FID%22:%22CI%22,%22Name%22:%22ClientDimNotSent%22,%22Text%22:%221%22}]', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1052, '5', 'IP限流', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1053, '5', '地址限流', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1054, '5', '地址限流', '/sa/simg/favicon-trans-bg-blue-mg.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1055, '5', '黑名单', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1056, '5', '黑名单', '/sa/simg/favicon-trans-bg-blue-mg.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1057, '5', '白名单', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1058, '5', '黑名单', '/rp/D_AxtJbuCgxOywpK7e7rYVBOBYw.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1059, '5', '白名单', '/sa/simg/favicon-trans-bg-blue-mg.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1060, '5', '地址限流', '/rp/D_AxtJbuCgxOywpK7e7rYVBOBYw.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1061, '5', 'IP限流', '/sa/simg/favicon-trans-bg-blue-mg.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1062, '5', '白名单', '/rp/D_AxtJbuCgxOywpK7e7rYVBOBYw.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1063, '5', 'IP限流', '/rp/D_AxtJbuCgxOywpK7e7rYVBOBYw.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1064, '5', '地址限流', '/fd/ls/l?IG=3D657E4FE11D437EB7FD4EECC92B19E6&Type=Event.ClientInst&DATA=[{%22T%22:%22CI.ClientInst%22,%22FID%22:%22CI%22,%22Name%22:%22max%20errors%20reached%22}]', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1065, '5', '黑名单', '/fd/ls/l?IG=3D657E4FE11D437EB7FD4EECC92B19E6&Type=Event.ClientInst&DATA=[{%22T%22:%22CI.ClientInst%22,%22FID%22:%22CI%22,%22Name%22:%22max%20errors%20reached%22}]', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1066, '5', '白名单', '/fd/ls/l?IG=3D657E4FE11D437EB7FD4EECC92B19E6&Type=Event.ClientInst&DATA=[{%22T%22:%22CI.ClientInst%22,%22FID%22:%22CI%22,%22Name%22:%22max%20errors%20reached%22}]', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1067, '5', 'IP限流', '/fd/ls/l?IG=3D657E4FE11D437EB7FD4EECC92B19E6&Type=Event.ClientInst&DATA=[{%22T%22:%22CI.ClientInst%22,%22FID%22:%22CI%22,%22Name%22:%22max%20errors%20reached%22}]', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:30', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1068, '5', '白名单', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:52', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1069, '5', '黑名单', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:52', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1070, '5', '地址限流', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:52', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1071, '5', 'IP限流', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:14:52', 1722330840000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1072, '5', '地址限流', '/rp/VeYpfzSZtJYcjpVvfwiIaM1Zx2k.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1073, '5', '黑名单', '/rp/y_8Q_2aCPV7xMwmnkTxgDurroYc.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1074, '5', '地址限流', '/rp/Xjqk15Ijg0OcYgnUt2a7NDG5S6Q.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1075, '5', 'IP限流', '/rp/y_8Q_2aCPV7xMwmnkTxgDurroYc.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1076, '5', '白名单', '/rp/VeYpfzSZtJYcjpVvfwiIaM1Zx2k.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1077, '5', '白名单', '/rp/y_8Q_2aCPV7xMwmnkTxgDurroYc.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1078, '5', 'IP限流', '/rp/Xjqk15Ijg0OcYgnUt2a7NDG5S6Q.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1079, '5', '黑名单', '/rp/VeYpfzSZtJYcjpVvfwiIaM1Zx2k.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1080, '5', '黑名单', '/rp/Xjqk15Ijg0OcYgnUt2a7NDG5S6Q.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1081, '5', '地址限流', '/rp/y_8Q_2aCPV7xMwmnkTxgDurroYc.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1082, '5', 'IP限流', '/rp/VeYpfzSZtJYcjpVvfwiIaM1Zx2k.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1083, '5', '白名单', '/rp/Xjqk15Ijg0OcYgnUt2a7NDG5S6Q.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1084, '5', 'IP限流', '/rp/B6jGHby7hXuEC7enS8xiNSUwqXw.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1085, '5', '地址限流', '/rp/B6jGHby7hXuEC7enS8xiNSUwqXw.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1086, '5', '黑名单', '/rp/B6jGHby7hXuEC7enS8xiNSUwqXw.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1087, '5', '白名单', '/rp/B6jGHby7hXuEC7enS8xiNSUwqXw.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1088, '5', '地址限流', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1089, '5', '白名单', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1090, '5', 'IP限流', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1091, '5', '黑名单', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1092, '5', 'IP限流', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1093, '5', '黑名单', '/rp/2NggrJSqjy5oZmsCGtiHDPUSMyM.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1094, '5', '白名单', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1095, '5', '地址限流', '/rp/2NggrJSqjy5oZmsCGtiHDPUSMyM.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1096, '5', '白名单', '/rp/2NggrJSqjy5oZmsCGtiHDPUSMyM.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1097, '5', '黑名单', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1098, '5', 'IP限流', '/rp/2NggrJSqjy5oZmsCGtiHDPUSMyM.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1099, '5', '白名单', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1100, '5', '地址限流', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1101, '5', '黑名单', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1102, '5', 'IP限流', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1103, '5', '地址限流', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1104, '5', 'IP限流', '/fd/ls/l?IG=0F3C31E9C739436BAA62D2B2DEBAF5BF&Type=Event.ClientInst&DATA=[{%22T%22:%22Info%22,%22FID%22:%22CI%22,%22Name%22:%22ClientDimNotSent%22,%22Text%22:%221%22}]', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1105, '5', '地址限流', '/fd/ls/l?IG=0F3C31E9C739436BAA62D2B2DEBAF5BF&Type=Event.ClientInst&DATA=[{%22T%22:%22Info%22,%22FID%22:%22CI%22,%22Name%22:%22ClientDimNotSent%22,%22Text%22:%221%22}]', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1106, '5', '黑名单', '/fd/ls/l?IG=0F3C31E9C739436BAA62D2B2DEBAF5BF&Type=Event.ClientInst&DATA=[{%22T%22:%22Info%22,%22FID%22:%22CI%22,%22Name%22:%22ClientDimNotSent%22,%22Text%22:%221%22}]', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1107, '5', '白名单', '/fd/ls/l?IG=0F3C31E9C739436BAA62D2B2DEBAF5BF&Type=Event.ClientInst&DATA=[{%22T%22:%22Info%22,%22FID%22:%22CI%22,%22Name%22:%22ClientDimNotSent%22,%22Text%22:%221%22}]', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1108, '5', 'IP限流', '/sa/simg/favicon-trans-bg-blue-mg.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1109, '5', '白名单', '/sa/simg/favicon-trans-bg-blue-mg.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1110, '5', '地址限流', '/sa/simg/favicon-trans-bg-blue-mg.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1111, '5', '黑名单', '/sa/simg/favicon-trans-bg-blue-mg.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1112, '5', '地址限流', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1113, '5', '黑名单', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1114, '5', 'IP限流', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1115, '5', '白名单', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1116, '5', '地址限流', '/rp/D_AxtJbuCgxOywpK7e7rYVBOBYw.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1117, '5', '白名单', '/rp/D_AxtJbuCgxOywpK7e7rYVBOBYw.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1118, '5', '黑名单', '/rp/D_AxtJbuCgxOywpK7e7rYVBOBYw.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1119, '5', 'IP限流', '/rp/D_AxtJbuCgxOywpK7e7rYVBOBYw.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1120, '5', '白名单', '/fd/ls/l?IG=0F3C31E9C739436BAA62D2B2DEBAF5BF&Type=Event.ClientInst&DATA=[{%22T%22:%22CI.ClientInst%22,%22FID%22:%22CI%22,%22Name%22:%22max%20errors%20reached%22}]', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1121, '5', '地址限流', '/fd/ls/l?IG=0F3C31E9C739436BAA62D2B2DEBAF5BF&Type=Event.ClientInst&DATA=[{%22T%22:%22CI.ClientInst%22,%22FID%22:%22CI%22,%22Name%22:%22max%20errors%20reached%22}]', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1122, '5', '黑名单', '/fd/ls/l?IG=0F3C31E9C739436BAA62D2B2DEBAF5BF&Type=Event.ClientInst&DATA=[{%22T%22:%22CI.ClientInst%22,%22FID%22:%22CI%22,%22Name%22:%22max%20errors%20reached%22}]', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1123, '5', 'IP限流', '/fd/ls/l?IG=0F3C31E9C739436BAA62D2B2DEBAF5BF&Type=Event.ClientInst&DATA=[{%22T%22:%22CI.ClientInst%22,%22FID%22:%22CI%22,%22Name%22:%22max%20errors%20reached%22}]', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:11', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1124, '5', 'IP限流', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:22', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1125, '5', '黑名单', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:22', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1126, '5', '白名单', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:22', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1127, '5', '地址限流', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:22', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1128, '5', 'IP限流', '/rp/TD_sPVBFVV86Fvh3NzYbmw9Mg44.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1129, '5', '白名单', '/rp/TD_sPVBFVV86Fvh3NzYbmw9Mg44.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1130, '5', '黑名单', '/rp/TD_sPVBFVV86Fvh3NzYbmw9Mg44.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1131, '5', '地址限流', '/rp/TD_sPVBFVV86Fvh3NzYbmw9Mg44.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1132, '5', '白名单', '/rp/YS1JJXnGSAyUSUTw4kMvsXA8XS4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1133, '5', '地址限流', '/rp/YS1JJXnGSAyUSUTw4kMvsXA8XS4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1134, '5', 'IP限流', '/rp/YS1JJXnGSAyUSUTw4kMvsXA8XS4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1135, '5', '黑名单', '/rp/YS1JJXnGSAyUSUTw4kMvsXA8XS4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1136, '5', '白名单', '/rp/wrJhxhCpyGMxLd3Se183wJVtEJU.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1137, '5', '地址限流', '/rp/wrJhxhCpyGMxLd3Se183wJVtEJU.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1138, '5', 'IP限流', '/rp/wrJhxhCpyGMxLd3Se183wJVtEJU.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1139, '5', '黑名单', '/rp/wrJhxhCpyGMxLd3Se183wJVtEJU.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1140, '5', '白名单', '/rp/Ezx484olZzBW4AC3oMzDpfvnnWs.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1141, '5', '地址限流', '/rp/Ezx484olZzBW4AC3oMzDpfvnnWs.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1142, '5', '白名单', '/rp/kQIUI14fAfVogUZAT3QH5qfaUnI.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1143, '5', '地址限流', '/rp/kQIUI14fAfVogUZAT3QH5qfaUnI.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1144, '5', 'IP限流', '/rp/kQIUI14fAfVogUZAT3QH5qfaUnI.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1145, '5', 'IP限流', '/rp/Ezx484olZzBW4AC3oMzDpfvnnWs.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1146, '5', '黑名单', '/rp/kQIUI14fAfVogUZAT3QH5qfaUnI.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1147, '5', '黑名单', '/rp/Ezx484olZzBW4AC3oMzDpfvnnWs.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1148, '5', '地址限流', '/rp/3cF03ygAW-3-ZH6h8qMEk1jaW0I.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1149, '5', '白名单', '/rp/3cF03ygAW-3-ZH6h8qMEk1jaW0I.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1150, '5', '黑名单', '/rp/3cF03ygAW-3-ZH6h8qMEk1jaW0I.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1151, '5', 'IP限流', '/rp/3cF03ygAW-3-ZH6h8qMEk1jaW0I.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1152, '5', '黑名单', '/rp/rwGSTGjkr48OTf1xQlHHyhMgS60.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1153, '5', 'IP限流', '/rp/rwGSTGjkr48OTf1xQlHHyhMgS60.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1154, '5', '白名单', '/rp/rwGSTGjkr48OTf1xQlHHyhMgS60.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1155, '5', '地址限流', '/rp/rwGSTGjkr48OTf1xQlHHyhMgS60.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1156, '5', 'IP限流', '/rp/Hgrb7jMDFa47DinximwgoHwr5Fc.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1157, '5', '白名单', '/rp/Hgrb7jMDFa47DinximwgoHwr5Fc.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1158, '5', '地址限流', '/rp/Hgrb7jMDFa47DinximwgoHwr5Fc.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1159, '5', '黑名单', '/rp/Hgrb7jMDFa47DinximwgoHwr5Fc.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1160, '5', '白名单', '/rp/n9BHk0WmVm-KoCKnBvQBaU3dJgo.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1161, '5', 'IP限流', '/rp/6EH3ObNTQqdAV-1WU_DjWmJS48A.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1162, '5', '黑名单', '/rp/nsL2sJnecOjUJFYx9nryDWwzz1w.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1163, '5', '白名单', '/rp/6EH3ObNTQqdAV-1WU_DjWmJS48A.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1164, '5', '白名单', '/rp/nsL2sJnecOjUJFYx9nryDWwzz1w.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1165, '5', '地址限流', '/rp/n9BHk0WmVm-KoCKnBvQBaU3dJgo.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1166, '5', '黑名单', '/rp/6EH3ObNTQqdAV-1WU_DjWmJS48A.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1167, '5', 'IP限流', '/rp/nsL2sJnecOjUJFYx9nryDWwzz1w.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1168, '5', '黑名单', '/rp/n9BHk0WmVm-KoCKnBvQBaU3dJgo.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1169, '5', '地址限流', '/rp/6EH3ObNTQqdAV-1WU_DjWmJS48A.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1170, '5', '地址限流', '/rp/nsL2sJnecOjUJFYx9nryDWwzz1w.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1171, '5', 'IP限流', '/rp/n9BHk0WmVm-KoCKnBvQBaU3dJgo.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1172, '5', '白名单', '/rp/3sj96d6X08Y5ULsNDwa2qjAbdq0.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1173, '5', '白名单', '/rp/_4mUz6cgbn3nbHhOA3c2g6vP9uY.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1174, '5', '黑名单', '/rp/3sj96d6X08Y5ULsNDwa2qjAbdq0.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1175, '5', 'IP限流', '/rp/RqJwi9iqZ24YLvU44XOE8XBmj_c.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1176, '5', '地址限流', '/rp/_4mUz6cgbn3nbHhOA3c2g6vP9uY.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1177, '5', '黑名单', '/rp/VJKJYU5pCjcN3AtsCiobFnft7fw.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1178, '5', '地址限流', '/rp/RqJwi9iqZ24YLvU44XOE8XBmj_c.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1179, '5', '黑名单', '/rp/RqJwi9iqZ24YLvU44XOE8XBmj_c.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1180, '5', '白名单', '/rp/RqJwi9iqZ24YLvU44XOE8XBmj_c.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1181, '5', 'IP限流', '/rp/_4mUz6cgbn3nbHhOA3c2g6vP9uY.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1182, '5', 'IP限流', '/rp/3sj96d6X08Y5ULsNDwa2qjAbdq0.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1183, '5', '白名单', '/rp/VJKJYU5pCjcN3AtsCiobFnft7fw.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1184, '5', '白名单', '/rp/KhpjzaqQvA3ITNHlsQNlgqqyB44.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1185, '5', 'IP限流', '/rp/VJKJYU5pCjcN3AtsCiobFnft7fw.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1186, '5', '地址限流', '/rp/VJKJYU5pCjcN3AtsCiobFnft7fw.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1187, '5', '黑名单', '/rp/KhpjzaqQvA3ITNHlsQNlgqqyB44.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1188, '5', '地址限流', '/rp/KhpjzaqQvA3ITNHlsQNlgqqyB44.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1189, '5', '黑名单', '/rp/_4mUz6cgbn3nbHhOA3c2g6vP9uY.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1190, '5', '地址限流', '/rp/3sj96d6X08Y5ULsNDwa2qjAbdq0.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1191, '5', 'IP限流', '/rp/KhpjzaqQvA3ITNHlsQNlgqqyB44.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1192, '5', '黑名单', '/rp/3RDOsE0o5vwi2J3RCYs_hH4vnBg.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1193, '5', '地址限流', '/rp/3RDOsE0o5vwi2J3RCYs_hH4vnBg.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1194, '5', '白名单', '/rp/3RDOsE0o5vwi2J3RCYs_hH4vnBg.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1195, '5', 'IP限流', '/rp/3RDOsE0o5vwi2J3RCYs_hH4vnBg.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1196, '5', '白名单', '/rp/QmywCm2TUAJpuofoMzbA5Xuq0YQ.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1197, '5', '黑名单', '/rp/QmywCm2TUAJpuofoMzbA5Xuq0YQ.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1198, '5', '白名单', '/rp/s0DWQSJdiTBCA4KEyk6uw1W3FbE.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1199, '5', 'IP限流', '/rp/QmywCm2TUAJpuofoMzbA5Xuq0YQ.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1200, '5', '地址限流', '/rp/s0DWQSJdiTBCA4KEyk6uw1W3FbE.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1201, '5', '地址限流', '/rp/QmywCm2TUAJpuofoMzbA5Xuq0YQ.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1202, '5', '黑名单', '/rp/YY6W6pDJRXxL-NjtyPCn0v-ZrcU.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1203, '5', 'IP限流', '/rp/s0DWQSJdiTBCA4KEyk6uw1W3FbE.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1204, '5', '白名单', '/rp/YY6W6pDJRXxL-NjtyPCn0v-ZrcU.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1205, '5', '黑名单', '/rp/s0DWQSJdiTBCA4KEyk6uw1W3FbE.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1206, '5', '地址限流', '/rp/YY6W6pDJRXxL-NjtyPCn0v-ZrcU.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1207, '5', '白名单', '/rp/tZme7v3I4Mu9-8ncmZdYbzJWIFk.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1208, '5', 'IP限流', '/rp/YY6W6pDJRXxL-NjtyPCn0v-ZrcU.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1209, '5', '白名单', '/rp/smvkiJczx2J4SQXFhD0znUhnHzo.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1210, '5', '黑名单', '/rp/smvkiJczx2J4SQXFhD0znUhnHzo.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1211, '5', '黑名单', '/rp/tZme7v3I4Mu9-8ncmZdYbzJWIFk.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1212, '5', '地址限流', '/rp/tZme7v3I4Mu9-8ncmZdYbzJWIFk.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1213, '5', '地址限流', '/rp/smvkiJczx2J4SQXFhD0znUhnHzo.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1214, '5', 'IP限流', '/rp/tZme7v3I4Mu9-8ncmZdYbzJWIFk.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1215, '5', '黑名单', '/rp/MNJSwXCFZCCBLvir3T1Qzw5bH2I.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1216, '5', 'IP限流', '/rp/smvkiJczx2J4SQXFhD0znUhnHzo.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1217, '5', '地址限流', '/rp/MNJSwXCFZCCBLvir3T1Qzw5bH2I.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1218, '5', 'IP限流', '/rp/MNJSwXCFZCCBLvir3T1Qzw5bH2I.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1219, '5', '白名单', '/rp/MNJSwXCFZCCBLvir3T1Qzw5bH2I.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1220, '5', '黑名单', '/rp/lqNzuXAg0UXgJVBhddp9UAWgmUo.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1221, '5', 'IP限流', '/rp/jg_YvabqQ9B3LtTj6sogJSHLjhY.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1222, '5', 'IP限流', '/rp/lqNzuXAg0UXgJVBhddp9UAWgmUo.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1223, '5', '地址限流', '/rp/lqNzuXAg0UXgJVBhddp9UAWgmUo.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1224, '5', '地址限流', '/rp/jg_YvabqQ9B3LtTj6sogJSHLjhY.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1225, '5', '白名单', '/rp/M9icSw9krUVbe0_w7aODPg3Yzug.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1226, '5', 'IP限流', '/rp/M9icSw9krUVbe0_w7aODPg3Yzug.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1227, '5', '黑名单', '/rp/jg_YvabqQ9B3LtTj6sogJSHLjhY.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1228, '5', '白名单', '/rp/jg_YvabqQ9B3LtTj6sogJSHLjhY.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1229, '5', '白名单', '/rp/lqNzuXAg0UXgJVBhddp9UAWgmUo.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1230, '5', '地址限流', '/rp/M9icSw9krUVbe0_w7aODPg3Yzug.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1231, '5', '黑名单', '/rp/M9icSw9krUVbe0_w7aODPg3Yzug.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1232, '5', '黑名单', '/rp/iR5ql4fLHGSyNAZe9hKidJmeriw.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1233, '5', 'IP限流', '/rp/a7W3zEI02qE99ymyv0ldLsEWCa8.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1234, '5', '白名单', '/rp/a7W3zEI02qE99ymyv0ldLsEWCa8.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1235, '5', '黑名单', '/rp/a7W3zEI02qE99ymyv0ldLsEWCa8.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1236, '5', 'IP限流', '/rp/iR5ql4fLHGSyNAZe9hKidJmeriw.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1237, '5', '白名单', '/rp/iR5ql4fLHGSyNAZe9hKidJmeriw.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1238, '5', '地址限流', '/rp/a7W3zEI02qE99ymyv0ldLsEWCa8.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1239, '5', '地址限流', '/rp/iR5ql4fLHGSyNAZe9hKidJmeriw.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1240, '5', '地址限流', '/rp/kFAqShRrnkQMbH6NYLBYoJ3lq9s.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1241, '5', '黑名单', '/rp/kFAqShRrnkQMbH6NYLBYoJ3lq9s.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1242, '5', 'IP限流', '/rp/kFAqShRrnkQMbH6NYLBYoJ3lq9s.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1243, '5', '白名单', '/rp/kFAqShRrnkQMbH6NYLBYoJ3lq9s.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1244, '5', '地址限流', '/rp/GMw-emKtBI4niBEd1QYct2VdwMA.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1245, '5', '白名单', '/rp/GMw-emKtBI4niBEd1QYct2VdwMA.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1246, '5', 'IP限流', '/rp/GMw-emKtBI4niBEd1QYct2VdwMA.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1247, '5', '黑名单', '/rp/GMw-emKtBI4niBEd1QYct2VdwMA.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1248, '5', '黑名单', '/rp/T0alytyTRnhWDuOgX_RPliEUL0g.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1249, '5', '白名单', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1250, '5', '地址限流', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1251, '5', 'IP限流', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1252, '5', '白名单', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1253, '5', 'IP限流', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1254, '5', '黑名单', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1255, '5', '地址限流', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1256, '5', 'IP限流', '/rp/T0alytyTRnhWDuOgX_RPliEUL0g.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1257, '5', '黑名单', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1258, '5', '白名单', '/rp/T0alytyTRnhWDuOgX_RPliEUL0g.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1259, '5', '地址限流', '/rp/T0alytyTRnhWDuOgX_RPliEUL0g.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1260, '5', '黑名单', '/rp/9Z_VGwmfEjJgLeE8Le77khSS-Os.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1261, '5', '白名单', '/rp/9Z_VGwmfEjJgLeE8Le77khSS-Os.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1262, '5', 'IP限流', '/rp/9Z_VGwmfEjJgLeE8Le77khSS-Os.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1263, '5', '地址限流', '/rp/9Z_VGwmfEjJgLeE8Le77khSS-Os.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1264, '5', '黑名单', '/rp/1anO1uIGPV0y5mnejL1lzwvLYwM.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1265, '5', '白名单', '/rp/jd_KiktC3sAWYJsOn4s4PoSvu18.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1266, '5', 'IP限流', '/rp/1anO1uIGPV0y5mnejL1lzwvLYwM.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1267, '5', '黑名单', '/rp/jd_KiktC3sAWYJsOn4s4PoSvu18.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1268, '5', '白名单', '/rp/1anO1uIGPV0y5mnejL1lzwvLYwM.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1269, '5', '地址限流', '/rp/jd_KiktC3sAWYJsOn4s4PoSvu18.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1270, '5', 'IP限流', '/rp/jd_KiktC3sAWYJsOn4s4PoSvu18.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1271, '5', '地址限流', '/rp/1anO1uIGPV0y5mnejL1lzwvLYwM.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1272, '5', 'IP限流', '/rp/OVhMHAhkq7bCatxdkWpknGOSppw.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1273, '5', 'IP限流', '/rp/o2XE53Emfk-rLc4XqBpGz3uySZ4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1274, '5', '黑名单', '/rp/o2XE53Emfk-rLc4XqBpGz3uySZ4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1275, '5', '黑名单', '/rp/opcLbWzzKDXAt7OOso4_av7nD1U.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1276, '5', '黑名单', '/rp/OVhMHAhkq7bCatxdkWpknGOSppw.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1277, '5', '白名单', '/rp/OVhMHAhkq7bCatxdkWpknGOSppw.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1278, '5', '白名单', '/rp/o2XE53Emfk-rLc4XqBpGz3uySZ4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1279, '5', '地址限流', '/rp/o2XE53Emfk-rLc4XqBpGz3uySZ4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1280, '5', '地址限流', '/rp/opcLbWzzKDXAt7OOso4_av7nD1U.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1281, '5', '地址限流', '/rp/OVhMHAhkq7bCatxdkWpknGOSppw.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1282, '5', 'IP限流', '/rp/opcLbWzzKDXAt7OOso4_av7nD1U.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1283, '5', '黑名单', '/rp/dGVKyFljVlqrEyAUqxWukFvLG1s.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1284, '5', '白名单', '/rp/opcLbWzzKDXAt7OOso4_av7nD1U.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1285, '5', '白名单', '/rp/dGVKyFljVlqrEyAUqxWukFvLG1s.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1286, '5', 'IP限流', '/rp/dGVKyFljVlqrEyAUqxWukFvLG1s.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1287, '5', '地址限流', '/rp/-kOKVlBm-bVq20_zFZBlq1udLhY.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1288, '5', '白名单', '/rp/YWD73Yj3ji4VFtib0dNF5jmlTBA.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1289, '5', 'IP限流', '/rp/YWD73Yj3ji4VFtib0dNF5jmlTBA.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1290, '5', '地址限流', '/rp/dGVKyFljVlqrEyAUqxWukFvLG1s.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1291, '5', '白名单', '/rp/-kOKVlBm-bVq20_zFZBlq1udLhY.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1292, '5', '黑名单', '/rp/-kOKVlBm-bVq20_zFZBlq1udLhY.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1293, '5', 'IP限流', '/rp/-kOKVlBm-bVq20_zFZBlq1udLhY.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1294, '5', '地址限流', '/rp/YWD73Yj3ji4VFtib0dNF5jmlTBA.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1295, '5', '黑名单', '/rp/YWD73Yj3ji4VFtib0dNF5jmlTBA.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1296, '5', '黑名单', '/rp/P4nVm9cQ78FaxD0VO8pzAuFypbk.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1297, '5', 'IP限流', '/rp/M-pga2Unw7h6nQLr2nCrEJG-zbs.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1298, '5', '白名单', '/rp/bOxaSr1x0aPO6OQeE_BCcQoEGgU.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1299, '5', '黑名单', '/rp/bOxaSr1x0aPO6OQeE_BCcQoEGgU.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1300, '5', '白名单', '/rp/M-pga2Unw7h6nQLr2nCrEJG-zbs.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1301, '5', '黑名单', '/rp/M-pga2Unw7h6nQLr2nCrEJG-zbs.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1302, '5', '地址限流', '/rp/M-pga2Unw7h6nQLr2nCrEJG-zbs.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1303, '5', 'IP限流', '/rp/P4nVm9cQ78FaxD0VO8pzAuFypbk.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1304, '5', '白名单', '/rp/P4nVm9cQ78FaxD0VO8pzAuFypbk.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1305, '5', '地址限流', '/rp/P4nVm9cQ78FaxD0VO8pzAuFypbk.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1306, '5', '地址限流', '/rp/bOxaSr1x0aPO6OQeE_BCcQoEGgU.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1307, '5', 'IP限流', '/rp/bOxaSr1x0aPO6OQeE_BCcQoEGgU.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1308, '5', 'IP限流', '/rp/ebjPhN8QpuVT6ajlwVTAk2vZIJ0.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1309, '5', '白名单', '/rp/ebjPhN8QpuVT6ajlwVTAk2vZIJ0.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1310, '5', '黑名单', '/rp/iUcoezJYOt0ROQ8xiYOm_7e96aA.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1311, '5', '地址限流', '/rp/ebjPhN8QpuVT6ajlwVTAk2vZIJ0.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1312, '5', '黑名单', '/rp/EpR1iHlQbv86VKrI0rWd8XuDGXg.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1313, '5', '黑名单', '/rp/ebjPhN8QpuVT6ajlwVTAk2vZIJ0.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1314, '5', '白名单', '/rp/iUcoezJYOt0ROQ8xiYOm_7e96aA.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1315, '5', 'IP限流', '/rp/iUcoezJYOt0ROQ8xiYOm_7e96aA.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1316, '5', '地址限流', '/rp/iUcoezJYOt0ROQ8xiYOm_7e96aA.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1317, '5', '白名单', '/rp/EpR1iHlQbv86VKrI0rWd8XuDGXg.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1318, '5', 'IP限流', '/rp/EpR1iHlQbv86VKrI0rWd8XuDGXg.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1319, '5', '地址限流', '/rp/EpR1iHlQbv86VKrI0rWd8XuDGXg.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:29', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1320, '5', 'IP限流', '/rp/ihYUI2GVpPjr_SdswXuJ_mTxxwI.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1321, '5', '黑名单', '/rp/bLU1ReHrMf2wB0ecO-FzNTOryRY.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1322, '5', '白名单', '/rp/vJG6QkR9vyUmM05ksDfTtldkbJI.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1323, '5', '白名单', '/rp/ihYUI2GVpPjr_SdswXuJ_mTxxwI.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1324, '5', 'IP限流', '/rp/vJG6QkR9vyUmM05ksDfTtldkbJI.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1325, '5', '地址限流', '/rp/vJG6QkR9vyUmM05ksDfTtldkbJI.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1326, '5', '白名单', '/rp/bLU1ReHrMf2wB0ecO-FzNTOryRY.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1327, '5', '黑名单', '/rp/ihYUI2GVpPjr_SdswXuJ_mTxxwI.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1328, '5', '黑名单', '/rp/vJG6QkR9vyUmM05ksDfTtldkbJI.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1329, '5', '地址限流', '/rp/ihYUI2GVpPjr_SdswXuJ_mTxxwI.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1330, '5', '黑名单', '/rp/yHfkgBT96zLCsI5DopEUGPkimIg.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1331, '5', 'IP限流', '/rp/bLU1ReHrMf2wB0ecO-FzNTOryRY.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1332, '5', '地址限流', '/rp/bLU1ReHrMf2wB0ecO-FzNTOryRY.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1333, '5', '黑名单', '/rp/9UL79UZBVqHPlftz_XNXvE6lrUc.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1334, '5', '黑名单', '/rp/9Z7TZcFuApTnbjn4WeglGvmfTC4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1335, '5', '白名单', '/rp/yHfkgBT96zLCsI5DopEUGPkimIg.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1336, '5', 'IP限流', '/rp/9UL79UZBVqHPlftz_XNXvE6lrUc.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1337, '5', '白名单', '/rp/9Z7TZcFuApTnbjn4WeglGvmfTC4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1338, '5', 'IP限流', '/rp/yHfkgBT96zLCsI5DopEUGPkimIg.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1339, '5', '地址限流', '/rp/yHfkgBT96zLCsI5DopEUGPkimIg.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1340, '5', '白名单', '/rp/9UL79UZBVqHPlftz_XNXvE6lrUc.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1341, '5', '地址限流', '/rp/9Z7TZcFuApTnbjn4WeglGvmfTC4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1342, '5', 'IP限流', '/rp/9Z7TZcFuApTnbjn4WeglGvmfTC4.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1343, '5', '地址限流', '/rp/9UL79UZBVqHPlftz_XNXvE6lrUc.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1344, '5', '黑名单', '/rp/fYVBfPjqhjimiAgnc-MTWjGV7n0.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1345, '5', '黑名单', '/rp/A25Fg1rEIKPwxL8yrniQVteLJ6g.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1346, '5', '地址限流', '/rp/fYVBfPjqhjimiAgnc-MTWjGV7n0.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1347, '5', '白名单', '/rp/fYVBfPjqhjimiAgnc-MTWjGV7n0.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1348, '5', 'IP限流', '/rp/A25Fg1rEIKPwxL8yrniQVteLJ6g.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1349, '5', 'IP限流', '/rp/fYVBfPjqhjimiAgnc-MTWjGV7n0.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1350, '5', '白名单', '/rp/A25Fg1rEIKPwxL8yrniQVteLJ6g.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1351, '5', '地址限流', '/rp/A25Fg1rEIKPwxL8yrniQVteLJ6g.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1352, '5', 'IP限流', '/rp/PDNPMcxYiRw8vBHI9m8SgpZGC5o.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1353, '5', '地址限流', '/rp/Ol6hyXzjqQCVAvKS5C7iK0e2VrA.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1354, '5', '黑名单', '/rp/PDNPMcxYiRw8vBHI9m8SgpZGC5o.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1355, '5', '白名单', '/rp/wW3Q74DesGWZzmnySeEOqLWAbT8.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1356, '5', '地址限流', '/rp/PDNPMcxYiRw8vBHI9m8SgpZGC5o.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1357, '5', '地址限流', '/rp/wW3Q74DesGWZzmnySeEOqLWAbT8.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1358, '5', '黑名单', '/rp/Ol6hyXzjqQCVAvKS5C7iK0e2VrA.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1359, '5', 'IP限流', '/rp/wW3Q74DesGWZzmnySeEOqLWAbT8.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1360, '5', '黑名单', '/rp/O89tudWt_fWCE0DGzBeEfW9jWFY.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1361, '5', '白名单', '/rp/O89tudWt_fWCE0DGzBeEfW9jWFY.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1362, '5', '黑名单', '/rp/wW3Q74DesGWZzmnySeEOqLWAbT8.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1363, '5', 'IP限流', '/rp/O89tudWt_fWCE0DGzBeEfW9jWFY.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1364, '5', '白名单', '/rp/Ol6hyXzjqQCVAvKS5C7iK0e2VrA.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1365, '5', '地址限流', '/rp/O89tudWt_fWCE0DGzBeEfW9jWFY.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1366, '5', '白名单', '/rp/PDNPMcxYiRw8vBHI9m8SgpZGC5o.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1367, '5', 'IP限流', '/rp/Ol6hyXzjqQCVAvKS5C7iK0e2VrA.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1368, '5', 'IP限流', '/rp/yS1hb12h5cPMQsXtTHTldZJ3n_s.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1369, '5', '白名单', '/rp/yS1hb12h5cPMQsXtTHTldZJ3n_s.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1370, '5', '地址限流', '/rp/pSw5XHjG0MDi96hEijQMuZZiLZU.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1371, '5', '白名单', '/rp/pSw5XHjG0MDi96hEijQMuZZiLZU.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1372, '5', '黑名单', '/rp/yS1hb12h5cPMQsXtTHTldZJ3n_s.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1373, '5', '黑名单', '/rp/pSw5XHjG0MDi96hEijQMuZZiLZU.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1374, '5', '地址限流', '/rp/yS1hb12h5cPMQsXtTHTldZJ3n_s.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1375, '5', '白名单', '/rp/t6bXI1ycFrvir_-AR5eBmZoeDFQ.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1376, '5', '黑名单', '/rp/t6bXI1ycFrvir_-AR5eBmZoeDFQ.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1377, '5', 'IP限流', '/rp/pSw5XHjG0MDi96hEijQMuZZiLZU.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1378, '5', 'IP限流', '/rp/t6bXI1ycFrvir_-AR5eBmZoeDFQ.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1379, '5', '地址限流', '/rp/t6bXI1ycFrvir_-AR5eBmZoeDFQ.js', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1380, '5', '黑名单', '/fd/ls/l?IG=A4D2DBE4923C41AFB08486CF5A6EEE07&Type=Event.ClientInst&DATA=[{%22T%22:%22CI.BM%22,%22FID%22:%22CI%22,%22Name%22:%22HV%22}]', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1381, '5', '白名单', '/fd/ls/l?IG=A4D2DBE4923C41AFB08486CF5A6EEE07&Type=Event.ClientInst&DATA=[{%22T%22:%22CI.BM%22,%22FID%22:%22CI%22,%22Name%22:%22HV%22}]', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1382, '5', '地址限流', '/fd/ls/l?IG=A4D2DBE4923C41AFB08486CF5A6EEE07&Type=Event.ClientInst&DATA=[{%22T%22:%22CI.BM%22,%22FID%22:%22CI%22,%22Name%22:%22HV%22}]', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1383, '5', 'IP限流', '/fd/ls/l?IG=A4D2DBE4923C41AFB08486CF5A6EEE07&Type=Event.ClientInst&DATA=[{%22T%22:%22CI.BM%22,%22FID%22:%22CI%22,%22Name%22:%22HV%22}]', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1384, '5', 'IP限流', '/rp/2NggrJSqjy5oZmsCGtiHDPUSMyM.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1385, '5', '白名单', '/rp/2NggrJSqjy5oZmsCGtiHDPUSMyM.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1386, '5', '黑名单', '/rp/2NggrJSqjy5oZmsCGtiHDPUSMyM.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1387, '5', '地址限流', '/rp/2NggrJSqjy5oZmsCGtiHDPUSMyM.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1388, '5', '白名单', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1389, '5', '地址限流', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1390, '5', 'IP限流', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1391, '5', '黑名单', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1392, '5', '白名单', '/fd/ls/l?IG=A4D2DBE4923C41AFB08486CF5A6EEE07&Type=Event.ClientInst&DATA=[{%22T%22:%22CI.ClientInst%22,%22FID%22:%22CI%22,%22Name%22:%22max%20errors%20reached%22}]', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1393, '5', 'IP限流', '/fd/ls/l?IG=A4D2DBE4923C41AFB08486CF5A6EEE07&Type=Event.ClientInst&DATA=[{%22T%22:%22CI.ClientInst%22,%22FID%22:%22CI%22,%22Name%22:%22max%20errors%20reached%22}]', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1394, '5', '黑名单', '/fd/ls/l?IG=A4D2DBE4923C41AFB08486CF5A6EEE07&Type=Event.ClientInst&DATA=[{%22T%22:%22CI.ClientInst%22,%22FID%22:%22CI%22,%22Name%22:%22max%20errors%20reached%22}]', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1395, '5', '地址限流', '/fd/ls/l?IG=A4D2DBE4923C41AFB08486CF5A6EEE07&Type=Event.ClientInst&DATA=[{%22T%22:%22CI.ClientInst%22,%22FID%22:%22CI%22,%22Name%22:%22max%20errors%20reached%22}]', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1396, '5', '黑名单', '/sa/simg/favicon-trans-bg-blue-mg.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1397, '5', 'IP限流', '/sa/simg/favicon-trans-bg-blue-mg.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1398, '5', '白名单', '/sa/simg/favicon-trans-bg-blue-mg.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1399, '5', '地址限流', '/sa/simg/favicon-trans-bg-blue-mg.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:30', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1400, '5', '地址限流', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:44', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1401, '5', 'IP限流', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:44', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1402, '5', '白名单', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:44', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1403, '5', '黑名单', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:44', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1404, '5', '地址限流', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:44', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1405, '5', '黑名单', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:44', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1406, '5', 'IP限流', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:44', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1407, '5', '白名单', '/fd/ls/lsp.aspx?', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:44', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1408, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:45', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1409, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:45', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1410, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:45', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1411, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:45', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1412, '5', '黑名单', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:46', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1413, '5', '地址限流', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:46', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1414, '5', 'IP限流', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:46', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1415, '5', '白名单', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:46', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1416, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:46', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1417, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:46', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1418, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:46', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1419, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:46', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1420, '5', '地址限流', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:47', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1421, '5', '白名单', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:47', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1422, '5', 'IP限流', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:47', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1423, '5', '黑名单', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:47', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1424, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:47', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1425, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:47', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1426, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:47', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1427, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:47', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1428, '5', '白名单', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:48', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1429, '5', 'IP限流', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:48', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1430, '5', '地址限流', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:48', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1431, '5', '黑名单', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:48', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1432, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:48', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1433, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:48', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1434, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:48', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1435, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:48', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1436, '5', 'IP限流', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:49', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1437, '5', '地址限流', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:49', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1438, '5', '白名单', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:49', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1439, '5', '黑名单', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:49', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1440, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:49', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1441, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:49', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1442, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:49', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1443, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:49', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1444, '5', '白名单', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:50', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1445, '5', '黑名单', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:50', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1446, '5', '地址限流', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:50', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1447, '5', 'IP限流', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:50', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1448, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:50', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1449, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:50', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1450, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:50', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1451, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:50', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1452, '5', '地址限流', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:50', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1453, '5', '白名单', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:50', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1454, '5', 'IP限流', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:50', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1455, '5', '黑名单', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:50', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1456, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:51', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1457, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:51', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1458, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:51', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1459, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:51', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1460, '5', '白名单', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:52', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1461, '5', '地址限流', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:52', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1462, '5', 'IP限流', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:52', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1463, '5', '黑名单', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:52', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1464, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:52', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1465, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:52', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1466, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:52', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1467, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:52', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1468, '5', 'IP限流', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:58', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1469, '5', '白名单', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:58', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1470, '5', '地址限流', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:58', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1471, '5', '黑名单', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:15:58', 1722330900000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1472, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:02', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1473, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:02', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1474, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:02', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1475, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:02', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1476, '5', '白名单', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:03', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1477, '5', '地址限流', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:03', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1478, '5', '黑名单', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:03', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1479, '5', 'IP限流', '/baidu/search?pglt=43&q=%E6%90%9C%E7%8B%97', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:03', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1480, '5', '白名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:39', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1481, '5', 'IP限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:39', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1482, '5', '地址限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:39', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1483, '5', '黑名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:39', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1484, '5', '地址限流', '/web/img/logo115_50.png?v=5', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:44', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1485, '5', '黑名单', '/web/img/logo115_50.png?v=5', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:44', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1486, '5', 'IP限流', '/web/img/logo115_50.png?v=5', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:44', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1487, '5', '白名单', '/web/img/logo115_50.png?v=5', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:44', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1488, '5', '白名单', '/img/fav.ico?272ba438-b20f-43d0-b168-47ea7c855bed', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:44', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1489, '5', '白名单', '/approve?uuid=272ba438-b20f-43d0-b168-47ea7c855bed&token=3740671188F69544999C87EAF5319AEA9AFF840566A8AF7B', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:44', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1490, '5', '地址限流', '/img/fav.ico?272ba438-b20f-43d0-b168-47ea7c855bed', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:44', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1491, '5', '黑名单', '/approve?uuid=272ba438-b20f-43d0-b168-47ea7c855bed&token=3740671188F69544999C87EAF5319AEA9AFF840566A8AF7B', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:44', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1492, '5', 'IP限流', '/img/fav.ico?272ba438-b20f-43d0-b168-47ea7c855bed', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:44', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1493, '5', '黑名单', '/img/fav.ico?272ba438-b20f-43d0-b168-47ea7c855bed', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:44', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1494, '5', '地址限流', '/approve?uuid=272ba438-b20f-43d0-b168-47ea7c855bed&token=3740671188F69544999C87EAF5319AEA9AFF840566A8AF7B', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:44', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1495, '5', 'IP限流', '/approve?uuid=272ba438-b20f-43d0-b168-47ea7c855bed&token=3740671188F69544999C87EAF5319AEA9AFF840566A8AF7B', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:44', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1496, '5', '黑名单', '/images/logo/new/favicon.ico?nv=1&v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:44', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1497, '5', '白名单', '/images/logo/new/favicon.ico?nv=1&v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:44', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1498, '5', '地址限流', '/images/logo/new/favicon.ico?nv=1&v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:44', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1499, '5', 'IP限流', '/images/logo/new/favicon.ico?nv=1&v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:44', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1500, '5', '黑名单', '/sug/css/suggestion.min.v.5.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:44', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1501, '5', 'IP限流', '/sug/css/suggestion.min.v.5.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:44', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1502, '5', '地址限流', '/sug/css/suggestion.min.v.5.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:44', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1503, '5', '白名单', '/sug/css/suggestion.min.v.5.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:44', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1504, '5', 'IP限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:45', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1505, '5', '地址限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:45', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1506, '5', '白名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:45', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1507, '5', '黑名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:45', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1508, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:45', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1509, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:45', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1510, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:45', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1511, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:45', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1512, '5', '白名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:46', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1513, '5', '黑名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:46', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1514, '5', 'IP限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:46', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1515, '5', '地址限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:46', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1516, '5', '地址限流', '/web/img/logo115_50.png?v=5', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:47', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1517, '5', '黑名单', '/web/img/logo115_50.png?v=5', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:47', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1518, '5', 'IP限流', '/web/img/logo115_50.png?v=5', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:47', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1519, '5', '白名单', '/web/img/logo115_50.png?v=5', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:47', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1520, '5', '黑名单', '/img/fav.ico?fcbeeac8-235b-46d8-9ee9-40402323daf2', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:47', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1521, '5', 'IP限流', '/img/fav.ico?fcbeeac8-235b-46d8-9ee9-40402323daf2', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:47', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1522, '5', '地址限流', '/img/fav.ico?fcbeeac8-235b-46d8-9ee9-40402323daf2', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:47', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1523, '5', '白名单', '/img/fav.ico?fcbeeac8-235b-46d8-9ee9-40402323daf2', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:47', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1524, '5', '白名单', '/approve?uuid=fcbeeac8-235b-46d8-9ee9-40402323daf2&token=3740725788F69544999C87EAF5319AEA9AFF840566A8AF7F', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:47', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1525, '5', 'IP限流', '/approve?uuid=fcbeeac8-235b-46d8-9ee9-40402323daf2&token=3740725788F69544999C87EAF5319AEA9AFF840566A8AF7F', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:47', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1526, '5', '地址限流', '/approve?uuid=fcbeeac8-235b-46d8-9ee9-40402323daf2&token=3740725788F69544999C87EAF5319AEA9AFF840566A8AF7F', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:47', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1527, '5', '黑名单', '/approve?uuid=fcbeeac8-235b-46d8-9ee9-40402323daf2&token=3740725788F69544999C87EAF5319AEA9AFF840566A8AF7F', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:47', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1528, '5', '白名单', '/images/logo/new/favicon.ico?nv=1&v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:48', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1529, '5', 'IP限流', '/images/logo/new/favicon.ico?nv=1&v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:48', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1530, '5', '地址限流', '/images/logo/new/favicon.ico?nv=1&v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:48', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1531, '5', '黑名单', '/images/logo/new/favicon.ico?nv=1&v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:48', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1532, '5', '黑名单', '/sug/css/suggestion.min.v.5.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:48', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1533, '5', '白名单', '/sug/css/suggestion.min.v.5.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:48', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1534, '5', '地址限流', '/sug/css/suggestion.min.v.5.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:48', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1535, '5', 'IP限流', '/sug/css/suggestion.min.v.5.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:48', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1536, '5', '地址限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:49', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1537, '5', 'IP限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:49', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1538, '5', '黑名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:49', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1539, '5', '白名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:49', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1540, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:49', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1541, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:49', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1542, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:49', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1543, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:49', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1544, '5', '白名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:50', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1545, '5', '黑名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:50', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1546, '5', 'IP限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:50', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1547, '5', '地址限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:50', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1548, '5', '黑名单', '/web/img/logo115_50.png?v=5', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:50', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1549, '5', '地址限流', '/web/img/logo115_50.png?v=5', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:50', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1550, '5', 'IP限流', '/web/img/logo115_50.png?v=5', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:50', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1551, '5', '白名单', '/web/img/logo115_50.png?v=5', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:50', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1552, '5', '地址限流', '/approve?uuid=23f0e7c6-75c2-43b2-b985-59d0be55bd1f&token=37407C1988F69544999C87EAF5319AEA9AFF840566A8AF82', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:51', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1553, '5', '黑名单', '/approve?uuid=23f0e7c6-75c2-43b2-b985-59d0be55bd1f&token=37407C1988F69544999C87EAF5319AEA9AFF840566A8AF82', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:51', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1554, '5', 'IP限流', '/approve?uuid=23f0e7c6-75c2-43b2-b985-59d0be55bd1f&token=37407C1988F69544999C87EAF5319AEA9AFF840566A8AF82', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:51', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1555, '5', '白名单', '/img/fav.ico?23f0e7c6-75c2-43b2-b985-59d0be55bd1f', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:51', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1556, '5', 'IP限流', '/img/fav.ico?23f0e7c6-75c2-43b2-b985-59d0be55bd1f', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:51', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1557, '5', '黑名单', '/img/fav.ico?23f0e7c6-75c2-43b2-b985-59d0be55bd1f', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:51', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1558, '5', '地址限流', '/img/fav.ico?23f0e7c6-75c2-43b2-b985-59d0be55bd1f', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:51', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1559, '5', '白名单', '/approve?uuid=23f0e7c6-75c2-43b2-b985-59d0be55bd1f&token=37407C1988F69544999C87EAF5319AEA9AFF840566A8AF82', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:51', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1560, '5', '地址限流', '/images/logo/new/favicon.ico?nv=1&v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:51', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1561, '5', '白名单', '/images/logo/new/favicon.ico?nv=1&v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:51', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1562, '5', '黑名单', '/images/logo/new/favicon.ico?nv=1&v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:51', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1563, '5', 'IP限流', '/images/logo/new/favicon.ico?nv=1&v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:51', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1564, '5', 'IP限流', '/sug/css/suggestion.min.v.5.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:51', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1565, '5', '白名单', '/sug/css/suggestion.min.v.5.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:51', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1566, '5', '地址限流', '/sug/css/suggestion.min.v.5.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:51', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1567, '5', '黑名单', '/sug/css/suggestion.min.v.5.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:16:51', 1722330960000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1568, '5', '白名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:17:06', 1722331020000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1569, '5', 'IP限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:17:06', 1722331020000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1570, '5', '地址限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:17:06', 1722331020000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1571, '5', '黑名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:17:06', 1722331020000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1572, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:17:46', 1722331020000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1573, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:17:46', 1722331020000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1574, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:17:46', 1722331020000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1575, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:17:46', 1722331020000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1576, '5', '地址限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:04', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1577, '5', '黑名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:04', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1578, '5', 'IP限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:04', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1579, '5', '白名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:04', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1580, '5', '地址限流', '/web/img/logo115_50.png?v=5', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:11', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1581, '5', '黑名单', '/web/img/logo115_50.png?v=5', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:11', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1582, '5', 'IP限流', '/web/img/logo115_50.png?v=5', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:11', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1583, '5', '白名单', '/web/img/logo115_50.png?v=5', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:11', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1584, '5', '黑名单', '/img/fav.ico?c11e8aef-f072-45ea-9f1d-c868d5a00abc', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:11', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1585, '5', '地址限流', '/img/fav.ico?c11e8aef-f072-45ea-9f1d-c868d5a00abc', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:11', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1586, '5', '白名单', '/img/fav.ico?c11e8aef-f072-45ea-9f1d-c868d5a00abc', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:11', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1587, '5', 'IP限流', '/img/fav.ico?c11e8aef-f072-45ea-9f1d-c868d5a00abc', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:11', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1588, '5', '地址限流', '/approve?uuid=c11e8aef-f072-45ea-9f1d-c868d5a00abc&token=43621D4F88F69544999C87EAF5319AEA9AFF840566A8AFD3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:11', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1589, '5', '白名单', '/approve?uuid=c11e8aef-f072-45ea-9f1d-c868d5a00abc&token=43621D4F88F69544999C87EAF5319AEA9AFF840566A8AFD3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:11', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1590, '5', 'IP限流', '/approve?uuid=c11e8aef-f072-45ea-9f1d-c868d5a00abc&token=43621D4F88F69544999C87EAF5319AEA9AFF840566A8AFD3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:11', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1591, '5', '黑名单', '/approve?uuid=c11e8aef-f072-45ea-9f1d-c868d5a00abc&token=43621D4F88F69544999C87EAF5319AEA9AFF840566A8AFD3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:11', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1592, '5', 'IP限流', '/images/logo/new/favicon.ico?nv=1&v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:11', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1593, '5', '黑名单', '/images/logo/new/favicon.ico?nv=1&v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:11', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1594, '5', '白名单', '/images/logo/new/favicon.ico?nv=1&v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:11', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1595, '5', '地址限流', '/images/logo/new/favicon.ico?nv=1&v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:11', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1596, '5', 'IP限流', '/sug/css/suggestion.min.v.5.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:11', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1597, '5', '地址限流', '/sug/css/suggestion.min.v.5.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:11', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1598, '5', '白名单', '/sug/css/suggestion.min.v.5.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:11', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1599, '5', '黑名单', '/sug/css/suggestion.min.v.5.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:11', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1600, '5', '地址限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:30', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1601, '5', '黑名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:30', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1602, '5', '白名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:30', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1603, '5', 'IP限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:30', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1604, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:30', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1605, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:30', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1606, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:30', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1607, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:30', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1608, '5', '地址限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:30', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1609, '5', 'IP限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:30', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1610, '5', '黑名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:30', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1611, '5', '白名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:30', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1612, '5', '黑名单', '/web/img/logo115_50.png?v=5', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1613, '5', '白名单', '/web/img/logo115_50.png?v=5', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1614, '5', 'IP限流', '/web/img/logo115_50.png?v=5', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1615, '5', '地址限流', '/web/img/logo115_50.png?v=5', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1616, '5', '白名单', '/img/fav.ico?0e8f8dd2-eb57-4c67-9bc9-33226bca6fbb', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1617, '5', '黑名单', '/img/fav.ico?0e8f8dd2-eb57-4c67-9bc9-33226bca6fbb', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1618, '5', '地址限流', '/img/fav.ico?0e8f8dd2-eb57-4c67-9bc9-33226bca6fbb', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1619, '5', 'IP限流', '/img/fav.ico?0e8f8dd2-eb57-4c67-9bc9-33226bca6fbb', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1620, '5', '黑名单', '/approve?uuid=0e8f8dd2-eb57-4c67-9bc9-33226bca6fbb&token=436260A188F69544999C87EAF5319AEA9AFF840566A8AFE7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1621, '5', 'IP限流', '/approve?uuid=0e8f8dd2-eb57-4c67-9bc9-33226bca6fbb&token=436260A188F69544999C87EAF5319AEA9AFF840566A8AFE7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1622, '5', '白名单', '/approve?uuid=0e8f8dd2-eb57-4c67-9bc9-33226bca6fbb&token=436260A188F69544999C87EAF5319AEA9AFF840566A8AFE7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1623, '5', '地址限流', '/approve?uuid=0e8f8dd2-eb57-4c67-9bc9-33226bca6fbb&token=436260A188F69544999C87EAF5319AEA9AFF840566A8AFE7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1624, '5', 'IP限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1625, '5', '地址限流', '/images/logo/new/favicon.ico?nv=1&v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1626, '5', '黑名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1627, '5', '地址限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1628, '5', '白名单', '/images/logo/new/favicon.ico?nv=1&v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1629, '5', 'IP限流', '/images/logo/new/favicon.ico?nv=1&v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1630, '5', '黑名单', '/images/logo/new/favicon.ico?nv=1&v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1631, '5', '白名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1632, '5', 'IP限流', '/sug/css/suggestion.min.v.5.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1633, '5', '黑名单', '/sug/css/suggestion.min.v.5.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1634, '5', '白名单', '/sug/css/suggestion.min.v.5.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1635, '5', '地址限流', '/sug/css/suggestion.min.v.5.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1636, '5', '白名单', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1637, '5', 'IP限流', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1638, '5', '黑名单', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1639, '5', '地址限流', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1640, '5', '白名单', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1641, '5', '黑名单', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1642, '5', 'IP限流', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1643, '5', 'IP限流', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1644, '5', '黑名单', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1645, '5', '地址限流', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1646, '5', '地址限流', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1647, '5', '黑名单', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1648, '5', '白名单', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1649, '5', '地址限流', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1650, '5', '黑名单', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1651, '5', 'IP限流', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1652, '5', 'IP限流', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1653, '5', '地址限流', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1654, '5', '白名单', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1655, '5', '白名单', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1656, '5', 'IP限流', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1657, '5', '黑名单', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1658, '5', '地址限流', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1659, '5', '白名单', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:31', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1660, '5', '黑名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:32', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1661, '5', '地址限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:32', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1662, '5', '白名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:32', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1663, '5', 'IP限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:32', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1664, '5', '地址限流', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:32', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1665, '5', '白名单', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:32', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1666, '5', '黑名单', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:32', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1667, '5', '白名单', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:32', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1668, '5', '黑名单', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:32', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1669, '5', 'IP限流', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:32', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1670, '5', '地址限流', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:32', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1671, '5', 'IP限流', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:32', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1672, '5', '白名单', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:32', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1673, '5', '黑名单', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:32', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1674, '5', 'IP限流', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:32', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1675, '5', '地址限流', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:32', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1676, '5', '黑名单', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:32', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1677, '5', '白名单', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:32', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1678, '5', 'IP限流', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:32', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1679, '5', '地址限流', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:32', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1680, '5', '地址限流', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:32', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1681, '5', 'IP限流', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:32', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1682, '5', '白名单', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:32', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1683, '5', '黑名单', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:32', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1684, '5', 'IP限流', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:32', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1685, '5', '白名单', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:32', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1686, '5', '黑名单', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:32', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1687, '5', '地址限流', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:32', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1688, '5', '白名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:36', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1689, '5', 'IP限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:36', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1690, '5', '地址限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:36', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1691, '5', '黑名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:36', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1692, '5', '黑名单', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:36', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1693, '5', '地址限流', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:36', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1694, '5', '白名单', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:36', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1695, '5', 'IP限流', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:36', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1696, '5', '黑名单', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:36', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1697, '5', '白名单', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:36', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1698, '5', '地址限流', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:36', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1699, '5', 'IP限流', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:36', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1700, '5', '黑名单', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:36', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1701, '5', '黑名单', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:36', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1702, '5', 'IP限流', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:36', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1703, '5', '地址限流', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:36', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1704, '5', '地址限流', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:36', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1705, '5', '地址限流', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:36', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1706, '5', '白名单', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:36', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1707, '5', '白名单', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:36', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1708, '5', 'IP限流', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:36', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1709, '5', '白名单', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:36', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1710, '5', 'IP限流', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:36', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1711, '5', '黑名单', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:36', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1712, '5', '地址限流', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:36', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1713, '5', 'IP限流', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:36', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1714, '5', '黑名单', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:36', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1715, '5', '白名单', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:36', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1716, '5', '黑名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:37', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1717, '5', '地址限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:37', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1718, '5', '白名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:37', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1719, '5', 'IP限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:37', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1720, '5', 'IP限流', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:37', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1721, '5', '白名单', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:37', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1722, '5', '黑名单', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:37', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1723, '5', '地址限流', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:37', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1724, '5', '白名单', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:37', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1725, '5', 'IP限流', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:37', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1726, '5', '地址限流', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:37', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1727, '5', '黑名单', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:37', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1728, '5', '黑名单', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:37', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1729, '5', 'IP限流', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:37', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1730, '5', '黑名单', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:37', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1731, '5', '白名单', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:37', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1732, '5', '黑名单', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:37', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1733, '5', '地址限流', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:37', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1734, '5', 'IP限流', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:37', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1735, '5', '地址限流', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:37', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1736, '5', '地址限流', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:37', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1737, '5', '白名单', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:37', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1738, '5', 'IP限流', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:37', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1739, '5', '白名单', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:37', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1740, '5', '黑名单', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:37', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1741, '5', '白名单', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:37', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1742, '5', '地址限流', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:37', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1743, '5', 'IP限流', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:37', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1744, '5', '白名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1745, '5', '黑名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1746, '5', '地址限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1747, '5', 'IP限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1748, '5', 'IP限流', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1749, '5', '黑名单', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1750, '5', '地址限流', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1751, '5', '白名单', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1752, '5', '黑名单', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1753, '5', '白名单', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1754, '5', '地址限流', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1755, '5', '白名单', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1756, '5', 'IP限流', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1757, '5', 'IP限流', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1758, '5', '黑名单', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1759, '5', '地址限流', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1760, '5', '白名单', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1761, '5', 'IP限流', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1762, '5', '黑名单', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1763, '5', '地址限流', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1764, '5', 'IP限流', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1765, '5', '白名单', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1766, '5', '地址限流', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1767, '5', '黑名单', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1768, '5', '地址限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1769, '5', 'IP限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1770, '5', '白名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1771, '5', '黑名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1772, '5', '地址限流', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1773, '5', '白名单', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1774, '5', 'IP限流', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1775, '5', '黑名单', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1776, '5', 'IP限流', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1777, '5', '黑名单', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1778, '5', '黑名单', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1779, '5', '白名单', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1780, '5', '地址限流', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1781, '5', 'IP限流', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1782, '5', '黑名单', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1783, '5', 'IP限流', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1784, '5', '白名单', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1785, '5', '地址限流', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1786, '5', '地址限流', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1787, '5', '黑名单', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1788, '5', '白名单', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1789, '5', 'IP限流', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1790, '5', '白名单', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1791, '5', 'IP限流', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1792, '5', '地址限流', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1793, '5', '地址限流', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1794, '5', '白名单', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1795, '5', '黑名单', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1796, '5', '黑名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1797, '5', '白名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1798, '5', '地址限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1799, '5', 'IP限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1800, '5', '地址限流', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1801, '5', '白名单', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1802, '5', '黑名单', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1803, '5', '黑名单', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1804, '5', '黑名单', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1805, '5', 'IP限流', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1806, '5', 'IP限流', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1807, '5', '白名单', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1808, '5', '白名单', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1809, '5', '地址限流', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1810, '5', 'IP限流', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1811, '5', '地址限流', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1812, '5', '白名单', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1813, '5', '白名单', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1814, '5', 'IP限流', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1815, '5', '黑名单', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1816, '5', 'IP限流', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1817, '5', '地址限流', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1818, '5', '地址限流', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1819, '5', '黑名单', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1820, '5', '白名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1821, '5', '地址限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1822, '5', 'IP限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1823, '5', '黑名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1824, '5', '黑名单', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1825, '5', '地址限流', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1826, '5', '白名单', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1827, '5', 'IP限流', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:38', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1828, '5', '地址限流', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1829, '5', 'IP限流', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1830, '5', '黑名单', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1831, '5', '白名单', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1832, '5', '黑名单', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1833, '5', '白名单', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1834, '5', '地址限流', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1835, '5', '黑名单', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1836, '5', 'IP限流', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1837, '5', '白名单', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1838, '5', 'IP限流', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1839, '5', '地址限流', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1840, '5', '地址限流', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1841, '5', '黑名单', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1842, '5', 'IP限流', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1843, '5', '地址限流', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1844, '5', '黑名单', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1845, '5', '白名单', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1846, '5', '白名单', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1847, '5', 'IP限流', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1848, '5', '黑名单', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1849, '5', '白名单', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1850, '5', 'IP限流', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1851, '5', '地址限流', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1852, '5', '黑名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1853, '5', '地址限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1854, '5', '白名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1855, '5', 'IP限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1856, '5', '地址限流', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1857, '5', 'IP限流', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1858, '5', '黑名单', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1859, '5', '白名单', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1860, '5', '黑名单', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1861, '5', '地址限流', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1862, '5', 'IP限流', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1863, '5', '白名单', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1864, '5', '白名单', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1865, '5', '黑名单', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1866, '5', 'IP限流', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1867, '5', '地址限流', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1868, '5', '黑名单', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1869, '5', '地址限流', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1870, '5', 'IP限流', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1871, '5', '白名单', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1872, '5', '白名单', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1873, '5', '黑名单', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1874, '5', 'IP限流', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1875, '5', '地址限流', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1876, '5', 'IP限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1877, '5', '黑名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1878, '5', '白名单', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1879, '5', '白名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1880, '5', '地址限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1881, '5', '地址限流', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1882, '5', '黑名单', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1883, '5', 'IP限流', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:39', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1884, '5', '白名单', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1885, '5', 'IP限流', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1886, '5', '黑名单', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1887, '5', '地址限流', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1888, '5', '地址限流', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1889, '5', '黑名单', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1890, '5', '白名单', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1891, '5', 'IP限流', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1892, '5', '白名单', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1893, '5', '黑名单', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1894, '5', 'IP限流', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1895, '5', '地址限流', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1896, '5', '黑名单', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1897, '5', '地址限流', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1898, '5', '白名单', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1899, '5', 'IP限流', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1900, '5', '地址限流', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1901, '5', 'IP限流', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1902, '5', '黑名单', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1903, '5', '白名单', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1904, '5', 'IP限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1905, '5', '黑名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1906, '5', '地址限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1907, '5', '白名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1908, '5', '白名单', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1909, '5', '黑名单', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1910, '5', '地址限流', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1911, '5', 'IP限流', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1912, '5', 'IP限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1913, '5', '黑名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1914, '5', '地址限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1915, '5', '白名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1916, '5', '黑名单', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1917, '5', 'IP限流', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1918, '5', '白名单', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1919, '5', '黑名单', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1920, '5', '地址限流', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1921, '5', 'IP限流', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1922, '5', '地址限流', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1923, '5', '黑名单', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1924, '5', '白名单', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1925, '5', '白名单', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1926, '5', 'IP限流', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1927, '5', '黑名单', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1928, '5', '白名单', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1929, '5', '黑名单', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1930, '5', '地址限流', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1931, '5', 'IP限流', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1932, '5', '地址限流', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1933, '5', '地址限流', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1934, '5', 'IP限流', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1935, '5', '白名单', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1936, '5', '白名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1937, '5', '地址限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1938, '5', '黑名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1939, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1940, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1941, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1942, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1943, '5', 'IP限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1944, '5', 'IP限流', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1945, '5', '黑名单', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1946, '5', '白名单', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1947, '5', '黑名单', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1948, '5', '地址限流', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1949, '5', 'IP限流', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1950, '5', '黑名单', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1951, '5', '地址限流', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1952, '5', '白名单', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1953, '5', '白名单', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1954, '5', 'IP限流', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1955, '5', '黑名单', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1956, '5', '地址限流', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1957, '5', '地址限流', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1958, '5', '黑名单', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1959, '5', '地址限流', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1960, '5', '白名单', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1961, '5', '白名单', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1962, '5', 'IP限流', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1963, '5', 'IP限流', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1964, '5', '白名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1965, '5', '黑名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1966, '5', '地址限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1967, '5', 'IP限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1968, '5', 'IP限流', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1969, '5', '黑名单', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1970, '5', 'IP限流', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1971, '5', '黑名单', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1972, '5', '黑名单', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1973, '5', 'IP限流', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1974, '5', '地址限流', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1975, '5', '白名单', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1976, '5', '白名单', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1977, '5', '地址限流', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1978, '5', '地址限流', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1979, '5', '白名单', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1980, '5', '地址限流', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1981, '5', 'IP限流', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1982, '5', '黑名单', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1983, '5', '地址限流', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1984, '5', '白名单', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1985, '5', 'IP限流', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1986, '5', '白名单', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1987, '5', '黑名单', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1988, '5', '白名单', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1989, '5', 'IP限流', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1990, '5', '黑名单', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1991, '5', '地址限流', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:40', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1992, '5', '白名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:41', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1993, '5', '黑名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:41', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1994, '5', '地址限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:41', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1995, '5', 'IP限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:41', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1996, '5', '白名单', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:41', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1997, '5', '白名单', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:41', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1998, '5', '黑名单', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:41', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (1999, '5', 'IP限流', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:41', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2000, '5', '地址限流', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:41', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2001, '5', '地址限流', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:41', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2002, '5', '黑名单', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:41', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2003, '5', '地址限流', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:41', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2004, '5', 'IP限流', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:41', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2005, '5', '黑名单', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:41', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2006, '5', 'IP限流', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:41', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2007, '5', '白名单', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:41', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2008, '5', '白名单', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:41', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2009, '5', '黑名单', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:41', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2010, '5', '地址限流', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:41', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2011, '5', 'IP限流', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:41', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2012, '5', 'IP限流', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:41', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2013, '5', '白名单', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:41', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2014, '5', '黑名单', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:41', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2015, '5', '地址限流', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:41', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2016, '5', '地址限流', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:41', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2017, '5', '白名单', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:41', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2018, '5', '黑名单', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:41', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2019, '5', 'IP限流', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:41', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2020, '5', '白名单', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:43', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2021, '5', '地址限流', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:43', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2022, '5', '黑名单', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:43', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2023, '5', '地址限流', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:43', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2024, '5', 'IP限流', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:43', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2025, '5', '白名单', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:43', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2026, '5', 'IP限流', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:43', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2027, '5', '黑名单', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:43', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2028, '5', '白名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:44', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2029, '5', '地址限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:44', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2030, '5', 'IP限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:44', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2031, '5', '黑名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:44', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2032, '5', '黑名单', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:44', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2033, '5', '黑名单', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:44', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2034, '5', 'IP限流', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:44', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2035, '5', '白名单', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:44', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2036, '5', '地址限流', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:44', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2037, '5', '白名单', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:44', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2038, '5', 'IP限流', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:44', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2039, '5', '地址限流', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:44', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2040, '5', '黑名单', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:44', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2041, '5', '白名单', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:44', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2042, '5', 'IP限流', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:44', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2043, '5', '白名单', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:44', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2044, '5', '地址限流', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:44', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2045, '5', '黑名单', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:44', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2046, '5', 'IP限流', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:44', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2047, '5', '地址限流', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:44', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2048, '5', 'IP限流', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:44', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2049, '5', '白名单', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:44', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2050, '5', '黑名单', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:44', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2051, '5', '地址限流', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:44', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2052, '5', 'IP限流', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:44', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2053, '5', '地址限流', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:44', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2054, '5', '白名单', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:44', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2055, '5', '黑名单', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:18:44', 1722331080000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2056, '5', 'IP限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:19:10', 1722331140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2057, '5', '白名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:19:10', 1722331140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2058, '5', '黑名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:19:10', 1722331140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2059, '5', '地址限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:19:10', 1722331140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2060, '5', '地址限流', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:19:14', 1722331140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2061, '5', '黑名单', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:19:14', 1722331140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2062, '5', '白名单', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:19:14', 1722331140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2063, '5', '地址限流', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:19:14', 1722331140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2064, '5', '黑名单', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:19:14', 1722331140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2065, '5', '白名单', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:19:14', 1722331140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2066, '5', 'IP限流', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:19:14', 1722331140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2067, '5', 'IP限流', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:19:14', 1722331140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2068, '5', 'IP限流', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:19:14', 1722331140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2069, '5', '黑名单', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:19:14', 1722331140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2070, '5', '白名单', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:19:14', 1722331140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2071, '5', '地址限流', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:19:14', 1722331140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2072, '5', 'IP限流', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:19:14', 1722331140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2073, '5', '白名单', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:19:14', 1722331140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2074, '5', '黑名单', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:19:14', 1722331140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2075, '5', '地址限流', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:19:14', 1722331140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2076, '5', '地址限流', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:19:14', 1722331140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2077, '5', '黑名单', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:19:14', 1722331140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2078, '5', 'IP限流', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:19:14', 1722331140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2079, '5', '白名单', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:19:14', 1722331140000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2080, '5', '地址限流', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:20:12', 1722331200000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2081, '5', '白名单', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:20:12', 1722331200000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2082, '5', 'IP限流', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:20:12', 1722331200000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2083, '5', '黑名单', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:20:12', 1722331200000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2084, '5', 'IP限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:45', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2085, '5', '黑名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:45', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2086, '5', '地址限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:45', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2087, '5', '白名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:45', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2088, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:46', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2089, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:46', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2090, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:46', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2091, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:46', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2092, '5', '白名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:46', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2093, '5', '地址限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:46', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2094, '5', '黑名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:46', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2095, '5', 'IP限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:46', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2096, '5', '白名单', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:47', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2097, '5', 'IP限流', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:47', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2098, '5', '地址限流', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:47', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2099, '5', '黑名单', '/baidu/static/css/anti.min.css?v=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:47', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2100, '5', '地址限流', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:47', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2101, '5', 'IP限流', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:47', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2102, '5', '白名单', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:47', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2103, '5', '黑名单', '/baidu/newstatic/css/verify.css', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:47', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2104, '5', '黑名单', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:47', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2105, '5', '地址限流', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:47', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2106, '5', '白名单', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:47', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2107, '5', '白名单', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:47', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2108, '5', 'IP限流', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:47', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2109, '5', 'IP限流', '/baidu/static/js/antispider.min.js?v=3', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:47', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2110, '5', '黑名单', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:47', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2111, '5', 'IP限流', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:47', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2112, '5', '黑名单', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:47', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2113, '5', '地址限流', '/baidu/newstatic/js/main.min.js?v=0.0.1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:47', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2114, '5', '白名单', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:47', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2115, '5', '地址限流', '/baidu/static/images/logo_180x60.png', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:47', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2116, '5', '白名单', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:47', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2117, '5', 'IP限流', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:47', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2118, '5', '地址限流', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:47', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2119, '5', '黑名单', '/baidu/newstatic/js/index.min.js?v=0.1.7', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:47', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2120, '5', '黑名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:47', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2121, '5', '白名单', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:47', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2122, '5', '地址限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:47', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2123, '5', 'IP限流', '/baidu/web?query=1', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:47', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2124, '5', 'IP限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:47', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2125, '5', '白名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:47', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2126, '5', '地址限流', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:47', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2127, '5', '黑名单', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-07-30 17:21:47', 1722331260000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2128, '5', '地址限流', '/system/api/doc.html', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2129, '5', '白名单', '/system/api/doc.html', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2130, '5', '黑名单', '/system/api/doc.html', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2131, '5', 'IP限流', '/system/api/doc.html', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2132, '5', '白名单', '/system/api/webjars/css/app.56b4fde5.css', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2133, '5', 'IP限流', '/system/api/webjars/css/app.56b4fde5.css', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2134, '5', '黑名单', '/system/api/webjars/css/app.56b4fde5.css', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2135, '5', '地址限流', '/system/api/webjars/css/app.56b4fde5.css', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2136, '5', '白名单', '/system/api/webjars/css/chunk-vendors.46fab8e1.css', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2137, '5', '黑名单', '/system/api/webjars/js/app.58a914a0.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2138, '5', '黑名单', '/system/api/webjars/css/chunk-vendors.46fab8e1.css', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2139, '5', '地址限流', '/system/api/webjars/css/chunk-vendors.46fab8e1.css', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2140, '5', 'IP限流', '/system/api/webjars/css/chunk-vendors.46fab8e1.css', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2141, '5', 'IP限流', '/system/api/webjars/js/app.58a914a0.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2142, '5', '白名单', '/system/api/webjars/js/app.58a914a0.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2143, '5', 'IP限流', '/system/api/webjars/js/chunk-vendors.c45f9828.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2144, '5', '黑名单', '/system/api/webjars/js/chunk-vendors.c45f9828.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2145, '5', '白名单', '/system/api/webjars/js/chunk-vendors.c45f9828.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2146, '5', '地址限流', '/system/api/webjars/js/app.58a914a0.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2147, '5', '地址限流', '/system/api/webjars/js/chunk-vendors.c45f9828.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2148, '5', '白名单', '/system/api/webjars/css/chunk-183657a2.8ed15ee7.css', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2149, '5', 'IP限流', '/system/api/webjars/css/chunk-183657a2.8ed15ee7.css', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2150, '5', '地址限流', '/system/api/webjars/css/chunk-183657a2.8ed15ee7.css', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2151, '5', '黑名单', '/system/api/webjars/css/chunk-183657a2.8ed15ee7.css', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2152, '5', '黑名单', '/system/api/webjars/js/chunk-0c58d94d.6bf494e7.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2153, '5', '地址限流', '/system/api/webjars/js/chunk-0c58d94d.6bf494e7.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2154, '5', 'IP限流', '/system/api/webjars/js/chunk-0c58d94d.6bf494e7.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2155, '5', '白名单', '/system/api/webjars/js/chunk-0c58d94d.6bf494e7.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2156, '5', '黑名单', '/system/api/webjars/js/chunk-0fd67716.d57e2c41.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2157, '5', '地址限流', '/system/api/webjars/js/chunk-0fd67716.d57e2c41.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2158, '5', 'IP限流', '/system/api/webjars/js/chunk-0fd67716.d57e2c41.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2159, '5', '白名单', '/system/api/webjars/js/chunk-0fd67716.d57e2c41.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2160, '5', 'IP限流', '/system/api/webjars/js/chunk-183657a2.2ce62f61.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2161, '5', '地址限流', '/system/api/webjars/js/chunk-183657a2.2ce62f61.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2162, '5', '黑名单', '/system/api/webjars/js/chunk-183657a2.2ce62f61.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2163, '5', '白名单', '/system/api/webjars/js/chunk-183657a2.2ce62f61.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2164, '5', '白名单', '/system/api/webjars/js/chunk-214218f0.92c7a521.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2165, '5', '地址限流', '/system/api/webjars/js/chunk-214218f0.92c7a521.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2166, '5', 'IP限流', '/system/api/webjars/js/chunk-214218f0.92c7a521.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2167, '5', '黑名单', '/system/api/webjars/js/chunk-214218f0.92c7a521.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2168, '5', 'IP限流', '/system/api/webjars/js/chunk-2d0af44e.81b7dfc1.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2169, '5', '黑名单', '/system/api/webjars/js/chunk-2d0af44e.81b7dfc1.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2170, '5', 'IP限流', '/system/api/webjars/js/chunk-2d0bd799.d162a2d5.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2171, '5', '白名单', '/system/api/webjars/js/chunk-2d0af44e.81b7dfc1.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2172, '5', '黑名单', '/system/api/webjars/js/chunk-2d0bd799.d162a2d5.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2173, '5', '地址限流', '/system/api/webjars/js/chunk-2d0af44e.81b7dfc1.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2174, '5', '地址限流', '/system/api/webjars/js/chunk-2d0bd799.d162a2d5.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2175, '5', '白名单', '/system/api/webjars/js/chunk-2d0bd799.d162a2d5.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2176, '5', '地址限流', '/system/api/webjars/js/chunk-2d0da532.b56aa8db.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2177, '5', '黑名单', '/system/api/webjars/js/chunk-2d0da532.b56aa8db.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2178, '5', '白名单', '/system/api/webjars/js/chunk-2d0da532.b56aa8db.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2179, '5', 'IP限流', '/system/api/webjars/js/chunk-2d0da532.b56aa8db.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2180, '5', 'IP限流', '/system/api/webjars/js/chunk-2d22269d.bb355f65.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2181, '5', '黑名单', '/system/api/webjars/js/chunk-2d22269d.bb355f65.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2182, '5', '白名单', '/system/api/webjars/js/chunk-2d22269d.bb355f65.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2183, '5', '地址限流', '/system/api/webjars/js/chunk-2d22269d.bb355f65.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2184, '5', '黑名单', '/system/api/webjars/js/chunk-3b888a65.8737ce4f.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2185, '5', '白名单', '/system/api/webjars/js/chunk-3b888a65.8737ce4f.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2186, '5', '地址限流', '/system/api/webjars/js/chunk-3b888a65.8737ce4f.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2187, '5', 'IP限流', '/system/api/webjars/js/chunk-3b888a65.8737ce4f.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2188, '5', '白名单', '/system/api/webjars/js/chunk-589faee0.08c2fba5.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2189, '5', '黑名单', '/system/api/webjars/js/chunk-589faee0.08c2fba5.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2190, '5', '地址限流', '/system/api/webjars/js/chunk-589faee0.08c2fba5.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2191, '5', '黑名单', '/system/api/webjars/js/chunk-735c675c.76398496.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2192, '5', 'IP限流', '/system/api/webjars/js/chunk-735c675c.76398496.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2193, '5', '地址限流', '/system/api/webjars/js/chunk-735c675c.76398496.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2194, '5', 'IP限流', '/system/api/webjars/js/chunk-589faee0.08c2fba5.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2195, '5', '白名单', '/system/api/webjars/js/chunk-735c675c.76398496.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2196, '5', '地址限流', '/system/api/webjars/js/chunk-adb9e944.9e6584b5.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2197, '5', '黑名单', '/system/api/webjars/js/chunk-adb9e944.9e6584b5.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2198, '5', 'IP限流', '/system/api/webjars/js/chunk-adb9e944.9e6584b5.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2199, '5', '白名单', '/system/api/webjars/js/chunk-adb9e944.9e6584b5.js', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:42', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2200, '5', '地址限流', '/system/api/favicon.ico', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:43', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2201, '5', '白名单', '/system/api/favicon.ico', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:43', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2202, '5', '黑名单', '/system/api/favicon.ico', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:43', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2203, '5', 'IP限流', '/system/api/favicon.ico', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:43', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2204, '5', 'IP限流', '/system/api/swagger-resources/configuration/ui', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:43', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2205, '5', '地址限流', '/system/api/swagger-resources/configuration/ui', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:43', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2206, '5', '白名单', '/system/api/swagger-resources/configuration/ui', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:43', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2207, '5', '黑名单', '/system/api/swagger-resources/configuration/ui', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:43', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2208, '5', 'IP限流', '/system/api/swagger-resources', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:43', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2209, '5', '黑名单', '/system/api/swagger-resources', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:43', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2210, '5', '白名单', '/system/api/swagger-resources', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:43', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2211, '5', '地址限流', '/system/api/swagger-resources', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:43', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2212, '5', '黑名单', '/system/api/v2/api-docs?group=biz', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:43', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2213, '5', 'IP限流', '/system/api/v2/api-docs?group=biz', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:43', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2214, '5', '地址限流', '/system/api/v2/api-docs?group=biz', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:43', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2215, '5', '白名单', '/system/api/v2/api-docs?group=biz', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:22:43', 1722331320000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2216, '5', 'IP限流', '/system/api/doc.html', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:23:17', 1722331380000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2217, '5', '黑名单', '/system/api/doc.html', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:23:17', 1722331380000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2218, '5', '白名单', '/system/api/doc.html', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:23:17', 1722331380000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2219, '5', '地址限流', '/system/api/doc.html', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:23:17', 1722331380000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2220, '5', '地址限流', '/favicon.ico', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:23:20', 1722331380000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2221, '5', '黑名单', '/favicon.ico', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:23:20', 1722331380000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2222, '5', '白名单', '/favicon.ico', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:23:20', 1722331380000);
INSERT INTO `monitor_proxy_limit_log` VALUES (2223, '5', 'IP限流', '/favicon.ico', 'allow', '本机地址', '127.0.0.1', '2024-07-30 17:23:20', 1722331380000);

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
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = '静态IP' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of monitor_proxy_statistic_service_discovery
-- ----------------------------
INSERT INTO `monitor_proxy_statistic_service_discovery` VALUES (2, '测试', '/system/api', 'http', 'https://www.qirenit.com', 12, 5, 1);
INSERT INTO `monitor_proxy_statistic_service_discovery` VALUES (3, '百度', '/baidu/', 'http', 'https://www.baidu.com', 1, 5, 1);
INSERT INTO `monitor_proxy_statistic_service_discovery` VALUES (4, '搜狗', '/baidu/', 'http', 'https://www.sogou.com', 1, 5, 1);

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
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '代码生成器管理' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_sys_gen
-- ----------------------------
INSERT INTO `monitor_sys_gen` VALUES (1, '本地 - MYSQL', 'JDBC', 'root', 'OHFOL3NPS3J0VXB6eGg5N1g5RVludz09', '172.16.0.40', 3306, '测试', 'com.mysql.cj.jdbc.Driver', NULL, 'monitor', NULL, 'OtPRaynDelFeVkVl', 0, 'JDBC', 'MYSQL', NULL, NULL, NULL, 'CREATE,DELETE');
INSERT INTO `monitor_sys_gen` VALUES (5, '测试', 'SHELL', 'root', 'bUxOUU1URWNpaWd3WDhlVjJqRDNOUT09', '192.168.110.100', 22, NULL, NULL, NULL, NULL, NULL, 'UW2lRgctX27PRvJK', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `monitor_sys_gen` VALUES (8, '11', 'REDIS', '', NULL, '172.16.0.40', 16379, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '', NULL, NULL, NULL, NULL);
INSERT INTO `monitor_sys_gen` VALUES (9, 'Sqlite', 'JDBC', '', 'Nnl5WXFhZXVCVkFoVWlDYzZ1dmVmZz09', '127.0.0.1', 3306, NULL, 'org.sqlite.JDBC', NULL, '', 'F:\\workspace\\utils-support-parent-starter\\gen\\9\\data\\1.db', 'OjndF5PYVMmD1BOW', NULL, 'FILE', 'SQLITE', NULL, NULL, 720, '');
INSERT INTO `monitor_sys_gen` VALUES (10, 'H2', 'JDBC', '', NULL, '127.0.0.1', 3306, NULL, 'org.h2.Driver', NULL, NULL, 'F:\\workspace\\utils-support-parent-starter\\gen\\10\\data\\1.db', NULL, NULL, 'FILE', 'H2', 'F:\\workspace\\utils-support-parent-starter\\gen\\10\\driver\\h2-2.1.214.jar', NULL, 720, '');
INSERT INTO `monitor_sys_gen` VALUES (11, '112.124.44.21', 'INFLUXDB', 'root', 'ZE9FZGpLeDlkOVJERTBCZE5sczdPUT09', '112.124.44.21', 8086, NULL, NULL, NULL, 'smartiot', NULL, 'CL32rLzJtPBLkNcS', NULL, NULL, '', NULL, NULL, 720, NULL);

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
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '生成器字段' ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字段备注' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_sys_gen_remark
-- ----------------------------

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
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '生成器表' ROW_FORMAT = DYNAMIC;

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
