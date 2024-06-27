/*
 Navicat Premium Data Transfer

 Source Server         : 172.16.2.226
 Source Server Type    : MySQL
 Source Server Version : 80036 (8.0.36)
 Source Host           : 172.16.2.226:3306
 Source Schema         : monitor

 Target Server Type    : MySQL
 Target Server Version : 80036 (8.0.36)
 File Encoding         : 65001

 Date: 26/06/2024 16:35:04
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for monitor_app
-- ----------------------------
DROP TABLE IF EXISTS `monitor_app`;
CREATE TABLE `monitor_app`  (
  `monitor_id` int NOT NULL AUTO_INCREMENT,
  `monitor_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '监控名称',
  `monitor_appname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属应用',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '应用管理' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_app
-- ----------------------------
INSERT INTO `monitor_app` VALUES (1, '单点登录', 'oss', NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for monitor_config
-- ----------------------------
DROP TABLE IF EXISTS `monitor_config`;
CREATE TABLE `monitor_config`  (
  `config_id` int NOT NULL AUTO_INCREMENT,
  `config_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `config_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '值',
  `config_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `config_profile` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '环境',
  `config_appname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属应用',
  `config_status` int NULL DEFAULT NULL COMMENT '0: 未开启；',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`config_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '配置中心' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_config
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_job
-- ----------------------------
DROP TABLE IF EXISTS `monitor_job`;
CREATE TABLE `monitor_job`  (
  `job_id` int NOT NULL AUTO_INCREMENT,
  `job_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `job_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务类型; cron, fixed',
  `job_conf` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '时间配置',
  `job_status` int NULL DEFAULT 0 COMMENT '状态;0: 未启用； 1: 正在运行',
  `job_app` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '对应的应用ID',
  `job_profile` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '环境',
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
  `job_trigger_last_time` int NULL DEFAULT NULL COMMENT '任务触发的最后一次时间',
  `job_trigger_next_time` int NULL DEFAULT NULL COMMENT '任务触发的下一次时间',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`job_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '调度任务' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_job
-- ----------------------------
INSERT INTO `monitor_job` VALUES (1, '测试', 'FIXED', '1', 0, 'dev', 'oss', NULL, '测试', NULL, NULL, NULL, NULL, 'BEAN', NULL, 10, NULL, NULL, 'FIRST', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

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
  `start_date` datetime NULL DEFAULT NULL,
  `end_date` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`job_log_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3909 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '调度任务日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of monitor_job_log
-- ----------------------------
INSERT INTO `monitor_job_log` VALUES (3905, '2024-06-12', '2024-06-12 10:19:08', 'S9999C0000', 'PADDING', '', 'dev', '', '', '测试', 'oss', 'jobconf_trigger_type_manual', NULL, NULL);
INSERT INTO `monitor_job_log` VALUES (3906, '2024-06-12', '2024-06-12 10:19:09', 'S9999C0000', 'PADDING', '', 'dev', '', '', '测试', 'oss', 'jobconf_trigger_type_manual', NULL, NULL);
INSERT INTO `monitor_job_log` VALUES (3907, '2024-06-12', '2024-06-12 10:19:43', 'S9999C0000', 'PADDING', '', 'dev', '', '', '测试', 'oss', 'jobconf_trigger_type_manual', NULL, NULL);
INSERT INTO `monitor_job_log` VALUES (3908, '2024-06-12', '2024-06-12 10:19:50', 'S9999C0000', 'PADDING', '', 'dev', '', '', '测试', 'oss', 'jobconf_trigger_type_manual', NULL, NULL);

-- ----------------------------
-- Table structure for monitor_limit
-- ----------------------------
DROP TABLE IF EXISTS `monitor_limit`;
CREATE TABLE `monitor_limit`  (
  `limit_id` int NOT NULL AUTO_INCREMENT,
  `limit_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '限流名称',
  `limit_mapping` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '限流地址',
  `limit_permits` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '每秒次数',
  `limit_status` int NULL DEFAULT NULL COMMENT '是否开启; 0: 不开启',
  `limit_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '限流实现方式, guava',
  `limit_resolver` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '限流模式,URL, REMOTE',
  `limit_profile` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '环境',
  `limit_app` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '限流应用',
  `limit_remote_list` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'REMOTE模式下的黑名单',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`limit_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '限流管理' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_limit
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
  PRIMARY KEY (`log_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of monitor_log
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_mybatis
-- ----------------------------
DROP TABLE IF EXISTS `monitor_mybatis`;
CREATE TABLE `monitor_mybatis`  (
  `monitor_mybatis_id` int NOT NULL AUTO_INCREMENT,
  `monitor_mybatis_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `monitor_mybatis_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `monitor_mybatis_sql` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'sql',
  `monitor_mybatis_sql_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'sql类型, xml, sql',
  `monitor_mybatis_model_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'model',
  `monitor_mybatis_status` int NULL DEFAULT NULL COMMENT '0: 未开启；',
  `monitor_mybatis_mapper_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'mapper',
  `monitor_mybatis_profile` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '环境',
  `monitor_appname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '应用',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_mybatis_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'mybatis管理' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_mybatis
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_patch
-- ----------------------------
DROP TABLE IF EXISTS `monitor_patch`;
CREATE TABLE `monitor_patch`  (
  `monitor_patch_id` int NOT NULL AUTO_INCREMENT,
  `monitor_patch_profile` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '补丁所属环境',
  `monitor_patch_app` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '补丁所属应用',
  `monitor_patch_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '补丁名称',
  `monitor_patch_chinese_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '中文名称',
  `monitor_patch_pack` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '补丁包路径',
  `monitor_patch_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `monitor_patch_version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '版本',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
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
  `project_id` int NOT NULL AUTO_INCREMENT,
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
  `version_id` int NOT NULL AUTO_INCREMENT,
  `project_id` int NULL DEFAULT NULL COMMENT '版本ID',
  `version_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `version_run_script` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '启动脚本名称',
  `version_stop_script` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '停止脚本名称',
  `version_log` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志名称',
  `version_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '版本说明',
  `version_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '版本编号',
  `version_status` int NULL DEFAULT NULL COMMENT '状态; 0:未启动',
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
  `proxy_id` int NOT NULL AUTO_INCREMENT,
  `proxy_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `proxy_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `proxy_host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0.0.0.0' COMMENT '启动地址',
  `proxy_port` int NULL DEFAULT NULL COMMENT '端口',
  `proxy_protocol` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '协议',
  `proxy_status` int NULL DEFAULT NULL COMMENT '0: 未开启',
  `proxy_version` int NULL DEFAULT NULL COMMENT '版本',
  PRIMARY KEY (`proxy_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '代理管理' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of monitor_proxy
-- ----------------------------
INSERT INTO `monitor_proxy` VALUES (5, '这是一个测试代理', NULL, '127.0.0.1', 12345, 'proxy', 0, NULL);

-- ----------------------------
-- Table structure for monitor_proxy_config
-- ----------------------------
DROP TABLE IF EXISTS `monitor_proxy_config`;
CREATE TABLE `monitor_proxy_config`  (
  `config_id` int NOT NULL AUTO_INCREMENT,
  `config_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '配置名称',
  `config_value` varchar(4096) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '配置值',
  `config_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '配置描述',
  `proxy_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '代理ID',
  PRIMARY KEY (`config_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '代理配置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of monitor_proxy_config
-- ----------------------------
INSERT INTO `monitor_proxy_config` VALUES (4, 'balance', 'WEIGHT', '负载均衡', '5');
INSERT INTO `monitor_proxy_config` VALUES (8, 'open-log', 'true', '开启日志', '5');
INSERT INTO `monitor_proxy_config` VALUES (9, 'serviceDiscovery', 'NACOS', '发现服务', '5');
INSERT INTO `monitor_proxy_config` VALUES (10, 'open-limit', 'true', '开启限流', '5');

-- ----------------------------
-- Table structure for monitor_proxy_limit
-- ----------------------------
DROP TABLE IF EXISTS `monitor_proxy_limit`;
CREATE TABLE `monitor_proxy_limit`  (
  `limit_id` int NOT NULL AUTO_INCREMENT,
  `limit_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '限流ip地址',
  `limit_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '限流url地址',
  `limit_black` int NULL DEFAULT 0 COMMENT '是否黑名单； 0：否',
  `limit_disable` int NULL DEFAULT 0 COMMENT '是否开启； 0：未开启',
  `limit_permits_per_second` int NULL DEFAULT NULL COMMENT '每秒生产令牌数',
  `proxy_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '代理ID',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`limit_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '代理限流器' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of monitor_proxy_limit
-- ----------------------------
INSERT INTO `monitor_proxy_limit` VALUES (1, '192.168.110.8', '/', 1, 1, 1, '5', NULL, '2024-06-24 16:46:19');

-- ----------------------------
-- Table structure for monitor_proxy_limit_log
-- ----------------------------
DROP TABLE IF EXISTS `monitor_proxy_limit_log`;
CREATE TABLE `monitor_proxy_limit_log`  (
  `limit_log_id` int NOT NULL AUTO_INCREMENT,
  `limit_log_server_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '日志对应的代理',
  `limit_log_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '请求地址',
  `limit_log_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '类型;deny;allow',
  `limit_log_address_geo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT 'geo',
  `limit_log_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '请求端地址',
  `create_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_time_min` bigint NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`limit_log_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 150 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '限流日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of monitor_proxy_limit_log
-- ----------------------------
INSERT INTO `monitor_proxy_limit_log` VALUES (107, '5', '/test', 'allow', '局域网', '192.168.110.8', '2024-06-26 11:16:20', 1719371760000);
INSERT INTO `monitor_proxy_limit_log` VALUES (108, '5', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-06-26 11:16:22', 1719371760000);
INSERT INTO `monitor_proxy_limit_log` VALUES (109, '5', '/test', 'allow', '局域网', '192.168.110.8', '2024-06-26 11:16:37', 1719371760000);
INSERT INTO `monitor_proxy_limit_log` VALUES (110, '5', '/test', 'allow', '局域网', '192.168.110.8', '2024-06-26 11:16:37', 1719371760000);
INSERT INTO `monitor_proxy_limit_log` VALUES (111, '5', '/test', 'allow', '局域网', '192.168.110.8', '2024-06-26 11:16:37', 1719371760000);
INSERT INTO `monitor_proxy_limit_log` VALUES (112, '5', '/test', 'allow', '局域网', '192.168.110.8', '2024-06-26 11:16:37', 1719371760000);
INSERT INTO `monitor_proxy_limit_log` VALUES (113, '5', '/test', 'allow', '局域网', '192.168.110.8', '2024-06-26 11:16:38', 1719371760000);
INSERT INTO `monitor_proxy_limit_log` VALUES (114, '5', '/test', 'allow', '局域网', '192.168.110.8', '2024-06-26 11:16:38', 1719371760000);
INSERT INTO `monitor_proxy_limit_log` VALUES (115, '5', '/favicon.ico', 'allow', '局域网', '192.168.110.8', '2024-06-26 11:16:39', 1719371760000);
INSERT INTO `monitor_proxy_limit_log` VALUES (116, '5', '/test', 'deny', '局域网', '192.168.110.8', '2024-06-26 11:29:34', 1719372540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (117, '5', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-06-26 11:29:34', 1719372540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (118, '5', '/test', 'deny', '局域网', '192.168.110.8', '2024-06-26 11:29:39', 1719372540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (119, '5', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-06-26 11:29:39', 1719372540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (120, '5', '/test', 'deny', '局域网', '192.168.110.8', '2024-06-26 11:29:39', 1719372540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (121, '5', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-06-26 11:29:39', 1719372540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (122, '5', '/test', 'deny', '局域网', '192.168.110.8', '2024-06-26 11:29:39', 1719372540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (123, '5', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-06-26 11:29:39', 1719372540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (124, '5', '/test', 'deny', '局域网', '192.168.110.8', '2024-06-26 11:29:39', 1719372540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (125, '5', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-06-26 11:29:39', 1719372540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (126, '5', '/test', 'deny', '局域网', '192.168.110.8', '2024-06-26 11:29:42', 1719372540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (127, '5', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-06-26 11:29:43', 1719372540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (128, '5', '/test', 'deny', '局域网', '192.168.110.8', '2024-06-26 11:29:43', 1719372540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (129, '5', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-06-26 11:29:43', 1719372540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (130, '5', '/test', 'deny', '局域网', '192.168.110.8', '2024-06-26 11:29:43', 1719372540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (131, '5', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-06-26 11:29:43', 1719372540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (132, '5', '/test', 'deny', '局域网', '192.168.110.8', '2024-06-26 11:29:43', 1719372540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (133, '5', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-06-26 11:29:43', 1719372540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (134, '5', '/test', 'deny', '局域网', '192.168.110.8', '2024-06-26 11:29:43', 1719372540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (135, '5', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-06-26 11:29:43', 1719372540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (136, '5', '/test', 'deny', '局域网', '192.168.110.8', '2024-06-26 11:29:43', 1719372540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (137, '5', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-06-26 11:29:43', 1719372540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (138, '5', '/test', 'deny', '局域网', '192.168.110.8', '2024-06-26 11:29:43', 1719372540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (139, '5', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-06-26 11:29:43', 1719372540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (140, '5', '/test', 'deny', '局域网', '192.168.110.8', '2024-06-26 11:29:43', 1719372540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (141, '5', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-06-26 11:29:44', 1719372540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (142, '5', '/test', 'deny', '局域网', '192.168.110.8', '2024-06-26 11:29:44', 1719372540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (143, '5', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-06-26 11:29:44', 1719372540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (144, '5', '/test', 'deny', '局域网', '192.168.110.8', '2024-06-26 11:29:44', 1719372540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (145, '5', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-06-26 11:29:44', 1719372540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (146, '5', '/test', 'deny', '局域网', '192.168.110.8', '2024-06-26 11:29:44', 1719372540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (147, '5', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-06-26 11:29:44', 1719372540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (148, '5', '/test', 'deny', '局域网', '192.168.110.8', '2024-06-26 11:29:44', 1719372540000);
INSERT INTO `monitor_proxy_limit_log` VALUES (149, '5', '/favicon.ico', 'deny', '局域网', '192.168.110.8', '2024-06-26 11:29:44', 1719372540000);

-- ----------------------------
-- Table structure for monitor_proxy_plugin
-- ----------------------------
DROP TABLE IF EXISTS `monitor_proxy_plugin`;
CREATE TABLE `monitor_proxy_plugin`  (
  `plugin_id` int NOT NULL AUTO_INCREMENT,
  `proxy_id` int NULL DEFAULT NULL COMMENT '代理ID',
  `plugin_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '插件描述',
  `plugin_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '插件名称',
  `create_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `plugin_sort` int NULL DEFAULT NULL COMMENT '优先级',
  PRIMARY KEY (`plugin_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 81 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '过滤器' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of monitor_proxy_plugin
-- ----------------------------
INSERT INTO `monitor_proxy_plugin` VALUES (79, 5, '动态路由', 'ASYNCHTTPROUTINGGATEWAYFILTER', NULL, 0);
INSERT INTO `monitor_proxy_plugin` VALUES (80, 5, 'ws动态路由', 'ASYNCWEBSOCKETROUTINGGATEWAYFILTER', NULL, 1);

-- ----------------------------
-- Table structure for monitor_proxy_plugin_config
-- ----------------------------
DROP TABLE IF EXISTS `monitor_proxy_plugin_config`;
CREATE TABLE `monitor_proxy_plugin_config`  (
  `plugin_config_id` int NOT NULL AUTO_INCREMENT,
  `proxy_id` int NULL DEFAULT NULL COMMENT '代理ID',
  `plugin_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '插件名字',
  `plugin_sort` int NULL DEFAULT NULL COMMENT '插件优先级',
  `plugin_config_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '配置名称',
  `plugin_config_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '配置',
  `plugin_config_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '配置类型',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`plugin_config_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '过滤器配置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of monitor_proxy_plugin_config
-- ----------------------------
INSERT INTO `monitor_proxy_plugin_config` VALUES (1, 5, 'LIMIT', 0, 'limit', '1', NULL, '2024-06-18 17:32:26');
INSERT INTO `monitor_proxy_plugin_config` VALUES (2, 5, 'LIMIT', 0, 'permitsPerSecond', '0', 'NUMBER', '2024-06-19 09:01:33');
INSERT INTO `monitor_proxy_plugin_config` VALUES (3, 5, 'LIMIT', 2, 'permitsPerSecond', '0', 'NUMBER', NULL);

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
  `gen_port` int NULL DEFAULT NULL COMMENT '数据库端口\"',
  `gen_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据库说明',
  `gen_driver` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据库驱动地址',
  `gen_driver_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据库驱动下载地址',
  `gen_database` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据库名称',
  `gen_database_file` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件数据库数据目录',
  `gen_uid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件数据库数据目录',
  PRIMARY KEY (`gen_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '代码生成器管理' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_sys_gen
-- ----------------------------
INSERT INTO `monitor_sys_gen` VALUES (1, '本地mysql', 'JDBC', 'root', 'Q3BONUo0c0FFbStnbG5rM2NPUmg2QT09', '127.0.0.1', 3306, NULL, 'com.mysql.cj.jdbc.Driver', NULL, 'monitor', NULL, 'a1U09DGg2DwXaAb7');
INSERT INTO `monitor_sys_gen` VALUES (5, '测试', 'SHELL', 'root', 'bUxOUU1URWNpaWd3WDhlVjJqRDNOUT09', '192.168.110.100', 22, NULL, NULL, NULL, NULL, NULL, 'UW2lRgctX27PRvJK');

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
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '生成器字段' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_sys_gen_column
-- ----------------------------
INSERT INTO `monitor_sys_gen_column` VALUES (6, 2, 'config_id', NULL, 'INT', 10, 0, 'Integer', 'configId', NULL, '0', '0', '1', '1', '1', '1', 'EQ', 'input', NULL, NULL);
INSERT INTO `monitor_sys_gen_column` VALUES (7, 2, 'config_name', '配置名称', 'VARCHAR', 255, 0, ' String', 'configName', NULL, '0', '1', '1', '1', '1', '1', 'LIKE', 'input', NULL, NULL);
INSERT INTO `monitor_sys_gen_column` VALUES (8, 2, 'config_value', '配置值', 'VARCHAR', 4096, 0, ' String', 'configValue', NULL, '0', '1', '1', '1', '1', '1', 'EQ', 'input', NULL, NULL);
INSERT INTO `monitor_sys_gen_column` VALUES (9, 2, 'config_desc', '配置描述', 'VARCHAR', 255, 0, ' String', 'configDesc', NULL, '0', '1', '1', '1', '1', '1', 'EQ', 'input', NULL, NULL);
INSERT INTO `monitor_sys_gen_column` VALUES (10, 2, 'proxy_id', '代理ID', 'VARCHAR', 255, 0, ' String', 'proxyId', NULL, '0', '1', '1', '1', '1', '1', 'EQ', 'input', NULL, NULL);

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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字段备注' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_sys_gen_remark
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
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '生成器表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_sys_gen_table
-- ----------------------------
INSERT INTO `monitor_sys_gen_table` VALUES (2, 'monitor_proxy_config', '代理配置', '本地mysql', 1, 'MonitorProxyConfig', NULL, 'com', 'proxyConfig', NULL, NULL, NULL, NULL, NULL, NULL);

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
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '生成器模板' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of monitor_sys_gen_template
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_terminal
-- ----------------------------
DROP TABLE IF EXISTS `monitor_terminal`;
CREATE TABLE `monitor_terminal`  (
  `terminal_id` int NOT NULL AUTO_INCREMENT,
  `terminal_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '名称',
  `terminal_host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '地址',
  `terminal_port` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '端口',
  `terminal_user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '账号',
  `terminal_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '密码',
  `terminal_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '描述',
  `terminal_status` int NULL DEFAULT NULL COMMENT '状态;0:未启动',
  PRIMARY KEY (`terminal_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '终端管理' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of monitor_terminal
-- ----------------------------
INSERT INTO `monitor_terminal` VALUES (1, '测试', '192.168.110.100', '22', 'root', 'boren1818', NULL, 1);
INSERT INTO `monitor_terminal` VALUES (2, '36.137.212.233', '36.137.212.233', '52176', 'root', '6sVhqzx85T', NULL, 0);

-- ----------------------------
-- Table structure for monitor_terminal_base
-- ----------------------------
DROP TABLE IF EXISTS `monitor_terminal_base`;
CREATE TABLE `monitor_terminal_base`  (
  `base_id` int NOT NULL AUTO_INCREMENT,
  `base_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '名称',
  `terminal_id` int NULL DEFAULT NULL COMMENT '终端ID',
  `base_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '描述',
  `base_value` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '值',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`base_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 69 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '终端基本信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of monitor_terminal_base
-- ----------------------------
INSERT INTO `monitor_terminal_base` VALUES (1, 'public-ifconfig', 1, '公网IP', '', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `monitor_terminal_base` VALUES (2, 'public-ifconfig', 2, '公网IP', '36.137.212.233', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `monitor_terminal_base` VALUES (3, 'release', 2, '系统', '', NULL, NULL, NULL, NULL, NULL, NULL);
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
INSERT INTO `monitor_terminal_base` VALUES (14, 'cpu-core-num', 1, 'cpu core数', '4', NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for monitor_terminal_project
-- ----------------------------
DROP TABLE IF EXISTS `monitor_terminal_project`;
CREATE TABLE `monitor_terminal_project`  (
  `terminal_project_id` int NOT NULL AUTO_INCREMENT,
  `terminal_project_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '项目名称',
  `terminal_project_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '项目描述',
  `terminal_project_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '项目地址',
  `terminal_project_start_script` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '项目启动脚本',
  `terminal_project_end_script` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '项目停止脚本',
  `terminal_project_log` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '日志文件地址',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建地址',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新地址',
  PRIMARY KEY (`terminal_project_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '项目管理' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of monitor_terminal_project
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
