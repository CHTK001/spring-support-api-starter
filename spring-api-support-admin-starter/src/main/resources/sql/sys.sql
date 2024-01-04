/*
 Navicat Premium Data Transfer

 Source Server         : 127.0.0.1
 Source Server Type    : MySQL
 Source Server Version : 80033
 Source Host           : 127.0.0.1:3306
 Source Schema         : sys

 Target Server Type    : MySQL
 Target Server Version : 80033
 File Encoding         : 65001

 Date: 04/01/2024 10:29:13
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`  (
  `dept_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dept_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '部门名称',
  `dept_marker` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '部门备注',
  `dept_parent_id` bigint NULL DEFAULT NULL COMMENT '父节点id',
  `dept_tree_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '父节点id路径',
  `dept_sort` int NULL DEFAULT NULL COMMENT '显示顺序',
  `dept_status` int NULL DEFAULT 1 COMMENT '状态(1:正常;0:禁用)',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `update_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`dept_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '部门表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dept
-- ----------------------------
INSERT INTO `sys_dept` VALUES (1, '测试', NULL, 0, '0', 1, 1, '系统管理员', '1', '2023-08-11 11:21:26.620000', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_dict
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict`  (
  `dict_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dict_type_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典类型ID',
  `dict_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典项名称',
  `dict_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典项值',
  `dict_sort` int NULL DEFAULT NULL COMMENT '排序',
  `dict_status` int NULL DEFAULT 1 COMMENT '状态(1:正常;0:禁用)',
  `dict_sys` int NULL DEFAULT 0 COMMENT '状态(1:系统字典;0:非系统字典)',
  `dict_defaulted` int NULL DEFAULT 1 COMMENT '是否默认(1:是;0:否)',
  `dict_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字典数据表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict
-- ----------------------------

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type`  (
  `dict_type_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dict_type_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '类型名称',
  `dict_type_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '类型编码',
  `dict_type_sort` int NULL DEFAULT NULL COMMENT '类型顺序',
  `dict_type_status` int NULL DEFAULT 0 COMMENT '状态(0:正常;1:禁用)',
  `dict_type_sys` int NULL DEFAULT 0 COMMENT '状态(1:系统字典;0:非系统字典)',
  `dict_type_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `update_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`dict_type_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字典类型表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES (1, '性别', 'XB', NULL, 0, 0, NULL, NULL, NULL, '2023-07-20 09:10:12.651000', '2023-07-20 09:10:30.027000', NULL, NULL);

-- ----------------------------
-- Table structure for sys_logger
-- ----------------------------
DROP TABLE IF EXISTS `sys_logger`;
CREATE TABLE `sys_logger`  (
  `log_id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `log_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志编号',
  `log_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志名称',
  `log_cost` int NULL DEFAULT NULL COMMENT '耗时',
  `log_mapping` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志请求地址',
  `log_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志内容',
  `log_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求地址',
  `log_address_position` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求地址位置',
  `log_watch` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '请求链路',
  `log_param` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '查询参数',
  `log_action` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '动作',
  `log_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '状态; 1:成功;',
  `log_day` date NULL DEFAULT NULL COMMENT '时间(天)',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `update_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`log_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统日志信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_logger
-- ----------------------------
INSERT INTO `sys_logger` VALUES (17, '9dc7f116de064a9a89d91b2fc14d7cf8', '登录', 2129, '/api/system/v2/users/login', '2023-08-14 16:21:01 null查询了模块登录', '192.168.110.6', '', '<strong class=\"node-details__name collapse-handle\">Request headers</strong><br />cookie: JSESSIONID=AC8AB2BF1EBDB9740710DDA3FBB57CF3; CHAT2DB.LOCALE=en-us<br />accept-language: zh-CN,zh;q=0.9<br />accept-encoding: gzip, deflate, br<br />referer: http://127.0.0.1:5173/<br />sec-fetch-dest: empty<br />sec-fetch-mode: cors<br />sec-fetch-site: same-origin<br />origin: http://127.0.0.1:5173<br />sec-ch-ua-platform: \"Windows\"<br />user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36<br />sec-ch-ua-mobile: ?0<br />content-type: application/json<br />accept: application/json, text/plain, */*<br />sec-ch-ua: \"Not.A/Brand\";v=\"8\", \"Chromium\";v=\"114\", \"Google Chrome\";v=\"114\"<br />content-length: 94<br />connection: close<br />host: localhost:18170<br /><strong class=\"node-details__name collapse-handle\">Request parameter</strong>', 'loginData:{\"password\":\"e10adc3949ba59abbe56e057f20f883e\",\"username\":\"admin\",\"verifyCode\":\"嘴放实深\"}\r\nrequest:null\r\nresponse:null\r\nbindingResult:null\r\n', '查询', '1', '2023-08-14', NULL, NULL, '2023-08-14 16:21:00.000000', '2023-08-14 16:21:01.083000', NULL, NULL);
INSERT INTO `sys_logger` VALUES (18, '6a0955853bf24d1bb9086b241b76df8a', '登录', 2054, '/api/system/v2/users/login', '2023-08-14 16:21:13 null查询了模块登录', '192.168.110.6', '', '<strong class=\"node-details__name collapse-handle\">Request headers</strong><br />cookie: JSESSIONID=AC8AB2BF1EBDB9740710DDA3FBB57CF3; CHAT2DB.LOCALE=en-us<br />accept-language: zh-CN,zh;q=0.9<br />accept-encoding: gzip, deflate, br<br />referer: http://127.0.0.1:5173/<br />sec-fetch-dest: empty<br />sec-fetch-mode: cors<br />sec-fetch-site: same-origin<br />origin: http://127.0.0.1:5173<br />sec-ch-ua-platform: \"Windows\"<br />user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36<br />sec-ch-ua-mobile: ?0<br />content-type: application/json<br />accept: application/json, text/plain, */*<br />sec-ch-ua: \"Not.A/Brand\";v=\"8\", \"Chromium\";v=\"114\", \"Google Chrome\";v=\"114\"<br />content-length: 88<br />connection: close<br />host: localhost:18170<br /><strong class=\"node-details__name collapse-handle\">Request parameter</strong>', 'loginData:{\"password\":\"e10adc3949ba59abbe56e057f20f883e\",\"username\":\"admin\",\"verifyCode\":\"Gm9fH3\"}\r\nrequest:null\r\nresponse:null\r\nbindingResult:null\r\n', '查询', '1', '2023-08-14', NULL, NULL, '2023-08-14 16:21:13.000000', '2023-08-14 16:21:13.066000', NULL, NULL);
INSERT INTO `sys_logger` VALUES (19, '041e7aa83e3f4772ba5d5f711de5c460', '登录', 11034, '/api/system/v2/users/login', '2023-08-14 16:21:42 null查询了模块登录', '192.168.110.6', '', '<strong class=\"node-details__name collapse-handle\">Request headers</strong><br />cookie: JSESSIONID=AC8AB2BF1EBDB9740710DDA3FBB57CF3; CHAT2DB.LOCALE=en-us<br />accept-language: zh-CN,zh;q=0.9<br />accept-encoding: gzip, deflate, br<br />referer: http://127.0.0.1:5173/<br />sec-fetch-dest: empty<br />sec-fetch-mode: cors<br />sec-fetch-site: same-origin<br />origin: http://127.0.0.1:5173<br />sec-ch-ua-platform: \"Windows\"<br />user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36<br />sec-ch-ua-mobile: ?0<br />content-type: application/json<br />accept: application/json, text/plain, */*<br />sec-ch-ua: \"Not.A/Brand\";v=\"8\", \"Chromium\";v=\"114\", \"Google Chrome\";v=\"114\"<br />content-length: 88<br />connection: close<br />host: localhost:18170<br /><strong class=\"node-details__name collapse-handle\">Request parameter</strong>', 'loginData:{\"password\":\"e10adc3949ba59abbe56e057f20f883e\",\"username\":\"admin\",\"verifyCode\":\"XmBbsB\"}\r\nrequest:null\r\nresponse:null\r\nbindingResult:null\r\n', '查询', '1', '2023-08-14', NULL, NULL, '2023-08-14 16:21:42.000000', '2023-08-14 16:21:42.934000', NULL, NULL);
INSERT INTO `sys_logger` VALUES (20, 'b2de7fc1f90945fd8aa64481556f0f9b', '登录', 55, '/api/system/v2/users/login', '2023-08-14 16:21:48 null查询了模块登录', '192.168.110.6', '', '<strong class=\"node-details__name collapse-handle\">Request headers</strong><br />cookie: JSESSIONID=AC8AB2BF1EBDB9740710DDA3FBB57CF3; CHAT2DB.LOCALE=en-us<br />accept-language: zh-CN,zh;q=0.9<br />accept-encoding: gzip, deflate, br<br />referer: http://127.0.0.1:5173/<br />sec-fetch-dest: empty<br />sec-fetch-mode: cors<br />sec-fetch-site: same-origin<br />origin: http://127.0.0.1:5173<br />sec-ch-ua-platform: \"Windows\"<br />user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36<br />sec-ch-ua-mobile: ?0<br />content-type: application/json<br />accept: application/json, text/plain, */*<br />sec-ch-ua: \"Not.A/Brand\";v=\"8\", \"Chromium\";v=\"114\", \"Google Chrome\";v=\"114\"<br />content-length: 83<br />connection: close<br />host: localhost:18170<br /><strong class=\"node-details__name collapse-handle\">Request parameter</strong>', 'loginData:{\"password\":\"e10adc3949ba59abbe56e057f20f883e\",\"username\":\"admin\",\"verifyCode\":\"5\"}\r\nrequest:null\r\nresponse:null\r\nbindingResult:null\r\n', '查询', '1', '2023-08-14', NULL, NULL, '2023-08-14 16:21:48.000000', '2023-08-14 16:21:48.966000', NULL, NULL);
INSERT INTO `sys_logger` VALUES (21, '519685d3679a4e1f8f638ad1f8cd0b5c', '登录', 3506, '/api/system/v2/users/login', '2023-08-17 17:00:23 null查询了模块登录', '192.168.110.6', '', '<strong class=\"node-details__name collapse-handle\">Request headers</strong><br />cookie: JSESSIONID=9426F7C0FB907403CF8EC2D60ADDB642; CHAT2DB.LOCALE=en-us<br />accept-language: zh-CN,zh;q=0.9<br />accept-encoding: gzip, deflate, br<br />referer: http://127.0.0.1:5173/<br />sec-fetch-dest: empty<br />sec-fetch-mode: cors<br />sec-fetch-site: same-origin<br />origin: http://127.0.0.1:5173<br />sec-ch-ua-platform: \"Windows\"<br />user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36<br />sec-ch-ua-mobile: ?0<br />content-type: application/json<br />accept: application/json, text/plain, */*<br />sec-ch-ua: \"Not.A/Brand\";v=\"8\", \"Chromium\";v=\"114\", \"Google Chrome\";v=\"114\"<br />content-length: 94<br />connection: close<br />host: localhost:18170<br /><strong class=\"node-details__name collapse-handle\">Request parameter</strong>', 'loginData:{\"password\":\"e10adc3949ba59abbe56e057f20f883e\",\"username\":\"admin\",\"verifyCode\":\"整共们写\"}\r\nrequest:null\r\nresponse:null\r\nbindingResult:null\r\n', '查询', '1', '2023-08-17', NULL, NULL, '2023-08-17 17:00:23.000000', '2023-08-17 17:00:23.439000', NULL, NULL);
INSERT INTO `sys_logger` VALUES (22, 'cf975cd95cd34a6dbf38501699ce71f9', '登录', 3210, '/api/system/v2/users/login', '2023-09-08 14:42:01 null查询了模块登录', '192.168.110.6', '', '<strong class=\"node-details__name collapse-handle\">Request headers</strong><br />cookie: JSESSIONID=5F9E4451AD3A703D025718CF07A11A9D<br />accept-language: zh-CN,zh;q=0.9<br />accept-encoding: gzip, deflate, br<br />referer: http://localhost:5173/<br />sec-fetch-dest: empty<br />sec-fetch-mode: cors<br />sec-fetch-site: same-origin<br />origin: http://localhost:5173<br />sec-ch-ua-platform: \"Windows\"<br />user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36<br />sec-ch-ua-mobile: ?0<br />content-type: application/json<br />accept: application/json, text/plain, */*<br />sec-ch-ua: \"Not.A/Brand\";v=\"8\", \"Chromium\";v=\"114\", \"Google Chrome\";v=\"114\"<br />content-length: 88<br />connection: close<br />host: localhost:18170<br /><strong class=\"node-details__name collapse-handle\">Request parameter</strong>', 'loginData:{\"password\":\"e10adc3949ba59abbe56e057f20f883e\",\"username\":\"admin\",\"verifyCode\":\"qMCRm2\"}\r\nrequest:null\r\nresponse:null\r\nbindingResult:null\r\n', '查询', '1', '2023-09-08', NULL, NULL, '2023-09-08 14:42:01.000000', '2023-09-08 14:42:01.976000', NULL, NULL);
INSERT INTO `sys_logger` VALUES (23, '9ddd0626ad3d497ebb708467eb83b164', '登录', 50, '/api/system/v2/users/login', '2023-09-08 14:43:08 null查询了模块登录', '192.168.110.6', '', '<strong class=\"node-details__name collapse-handle\">Request headers</strong><br />cookie: JSESSIONID=02BF152919D1D08FAF6B14CC126676E3; Idea-2fc98548=731679cb-41c8-44aa-b7ca-9e9d85452b4e<br />accept-language: zh-CN,zh;q=0.9<br />accept-encoding: gzip, deflate, br<br />referer: http://localhost:5173/<br />sec-fetch-dest: empty<br />sec-fetch-mode: cors<br />sec-fetch-site: same-origin<br />origin: http://localhost:5173<br />sec-ch-ua-platform: \"Windows\"<br />user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36<br />sec-ch-ua-mobile: ?0<br />content-type: application/json<br />accept: application/json, text/plain, */*<br />sec-ch-ua: \"Not.A/Brand\";v=\"8\", \"Chromium\";v=\"114\", \"Google Chrome\";v=\"114\"<br />content-length: 84<br />connection: close<br />host: localhost:18170<br /><strong class=\"node-details__name collapse-handle\">Request parameter</strong>', 'loginData:{\"password\":\"e10adc3949ba59abbe56e057f20f883e\",\"username\":\"admin\",\"verifyCode\":\"21\"}\r\nrequest:null\r\nresponse:null\r\nbindingResult:null\r\n', '查询', '1', '2023-09-08', NULL, NULL, '2023-09-08 14:43:08.000000', '2023-09-08 14:43:08.139000', NULL, NULL);

-- ----------------------------
-- Table structure for sys_mapping
-- ----------------------------
DROP TABLE IF EXISTS `sys_mapping`;
CREATE TABLE `sys_mapping`  (
  `map_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `map_mapping` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Mapping: 请求地址, Bean: 文件地址',
  `map_method` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '方法',
  `map_handler_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Mapping:类, Bean: 接口类型',
  `map_handler_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Mapping:方法名, Bean: Bean名称',
  `map_application_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '应用名称',
  `map_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'MAPPING' COMMENT '类型;MAPPING,BEAN',
  `map_application_status` int NULL DEFAULT 0 COMMENT '是否禁用;0:启用',
  `map_marker` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  PRIMARY KEY (`map_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '映射地址信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_mapping
-- ----------------------------
INSERT INTO `sys_mapping` VALUES ('0058590895c2ffda8944ce57ae82f5be', '/v2/task/createTask', 'POST', 'com.chua.starter.system.support.controller.SysTaskController', 'createTask', 'system', 'MAPPING', 0, NULL);
INSERT INTO `sys_mapping` VALUES ('009099d6ce1ca5d56d083baaca541b02', '/v2/task/deleteByTaskId', 'DELETE', 'com.chua.starter.system.support.controller.SysTaskController', 'deleteByTaskId', 'system', 'MAPPING', 0, '删除任务');
INSERT INTO `sys_mapping` VALUES ('0437b2ec8b5c9d5ab407fe43a7070638', '/v1/log/near', 'GET', 'com.chua.starter.system.support.controller.SysLogController', 'near', 'system', 'MAPPING', 0, NULL);
INSERT INTO `sys_mapping` VALUES ('0459fd9de8538db1f7a66b62cf6b9311', '/v2/dept/save', 'POST', 'com.chua.starter.system.support.controller.SysDeptController', 'save', 'system', 'MAPPING', 0, '新增组织机构');
INSERT INTO `sys_mapping` VALUES ('0667f8e507be0d5a918bc70a3fbe10a5', '/v2/library/save', 'POST', 'com.chua.starter.system.support.controller.SysLibraryController', 'save', 'system', 'MAPPING', 0, '创建识别库');
INSERT INTO `sys_mapping` VALUES ('07e22cf020a41544470b49ae9d8a582c', '/v2/users/save', 'POST', 'com.chua.starter.system.support.controller.SysUserController', 'save', 'system', 'MAPPING', 0, '新增用户');
INSERT INTO `sys_mapping` VALUES ('0aad09a7f147949f48e2214dc89449cf', '/oss/release/page', 'GET', 'com.chua.starter.oss.support.provider.OssProvider', 'page', 'system', 'MAPPING', 0, NULL);
INSERT INTO `sys_mapping` VALUES ('0abbd15cb63ea3fac781464d8e264e09', '/v2/users/me', 'GET', 'com.chua.starter.system.support.controller.SysUserController', 'getUserLoginInfo', 'system', 'MAPPING', 0, '获取当前登录用户信息');
INSERT INTO `sys_mapping` VALUES ('0b395ddf3a84007b8d3b12e0a52e4a20', '/v1/captcha.jpg', 'GET', 'com.chua.starter.common.support.provider.CaptchaProvider', 'captcha', 'system', 'MAPPING', 0, NULL);
INSERT INTO `sys_mapping` VALUES ('0b7d075c89042c13e6dc3b51d676c299', '/v2/dept/batchDelete', 'DELETE', 'com.chua.starter.system.support.controller.SysDeptController', 'batchDelete', 'system', 'MAPPING', 0, '批量删除组织机构');
INSERT INTO `sys_mapping` VALUES ('0d1ff6657c3f62f01b43f516265e7085', '/oss/release/listObjects', 'GET', 'com.chua.starter.oss.support.provider.OssProvider', 'listObjects', 'system', 'MAPPING', 0, NULL);
INSERT INTO `sys_mapping` VALUES ('0da96fb3859b43ae626ee93977f71225', '/oss/release/preview', 'POST', 'com.chua.starter.oss.support.provider.OssProvider', 'preview', 'system', 'MAPPING', 0, NULL);
INSERT INTO `sys_mapping` VALUES ('1090d5d74f64e2ef4506077d19522b0d', '/error', '', 'org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController', 'errorHtml', 'system', 'MAPPING', 0, NULL);
INSERT INTO `sys_mapping` VALUES ('11e3eeb9c3cee1172b868e832f558805', '/v2/dict/update', 'PUT', 'com.chua.starter.system.support.controller.SysDictController', 'update', 'system', 'MAPPING', 0, '修改字典');
INSERT INTO `sys_mapping` VALUES ('1708b36ae873c7cc6f136963709f49c0', '/v2/users/update', 'PUT', 'com.chua.starter.system.support.controller.SysUserController', 'update', 'system', 'MAPPING', 0, '更新用户');
INSERT INTO `sys_mapping` VALUES ('1c23027ba3d12408c348d86d245cc84b', '/v2/library/page', 'GET', 'com.chua.starter.system.support.controller.SysLibraryController', 'page', 'system', 'MAPPING', 0, '分页查询识别库');
INSERT INTO `sys_mapping` VALUES ('1c74a2cd004b1d4e78cb991c159487cc', '/v1/mapping/page', 'GET', 'com.chua.starter.common.api.support.RequestMappingHandlerMappingController', 'page', 'system', 'MAPPING', 0, NULL);
INSERT INTO `sys_mapping` VALUES ('2014f6d03b4667915f60c874bcdf9ef8', '/gitee/bind/loginCodeType', 'GET', 'com.chua.starter.system.support.third.controller.GiteeThirdPartyProvider', 'gitee', 'system', 'MAPPING', 0, NULL);
INSERT INTO `sys_mapping` VALUES ('24adbe459718778828dcb4aa1cb951ea', '/v1/captcha', 'GET', 'com.chua.starter.common.support.provider.CaptchaProvider', 'captchaBase64', 'system', 'MAPPING', 0, NULL);
INSERT INTO `sys_mapping` VALUES ('294581ab3b5ed8bd9dfd08828412bb2b', '/v2/dept/tree', 'GET', 'com.chua.starter.system.support.controller.SysDeptController', 'tree', 'system', 'MAPPING', 0, '机构树');
INSERT INTO `sys_mapping` VALUES ('2b5aa7d1ef09b622d95f9742eabef734', '/oss/release/preview', 'GET', 'com.chua.starter.oss.support.provider.OssProvider', 'preview', 'system', 'MAPPING', 0, NULL);
INSERT INTO `sys_mapping` VALUES ('2d2863bb1b32b61e06ccdd7d052721a4', '/v2/dict/page', 'GET', 'com.chua.starter.system.support.controller.SysDictController', 'page', 'system', 'MAPPING', 0, '字典列表');
INSERT INTO `sys_mapping` VALUES ('2d7e667599da89065ec5fea99d3a6029', '/v2/role/list', 'GET', 'com.chua.starter.system.support.controller.SysRoleController', 'list', 'system', 'MAPPING', 0, '角色列表');
INSERT INTO `sys_mapping` VALUES ('309698e0387a1808afcc84de16c650e8', '/v2/library/mapping', 'PUT', 'com.chua.starter.system.support.controller.SysLibraryController', 'update', 'system', 'MAPPING', 0, '更新识别库映射关系');
INSERT INTO `sys_mapping` VALUES ('30b76e9c721b9521029ee4fe2a8a1922', '/swagger-resources', 'GET', 'springfox.documentation.swagger.web.ApiResourceController', 'swaggerResources', 'system', 'MAPPING', 0, NULL);
INSERT INTO `sys_mapping` VALUES ('31614bed97bf0d3ff5255a19c275fcd7', '/v2/role/update', 'PUT', 'com.chua.starter.system.support.controller.SysRoleController', 'update', 'system', 'MAPPING', 0, '修改角色');
INSERT INTO `sys_mapping` VALUES ('334e61e3c2b2dd005507e297bf1d05ca', '/v2/menus/update', 'PUT', 'com.chua.starter.system.support.controller.SysMenuController', 'update', 'system', 'MAPPING', 0, '更新菜单');
INSERT INTO `sys_mapping` VALUES ('33f02ec1c0061f90e795815348a2928f', '/v2/role/page', 'GET', 'com.chua.starter.system.support.controller.SysRoleController', 'page', 'system', 'MAPPING', 0, '角色分页');
INSERT INTO `sys_mapping` VALUES ('33f79483446b8ecd8bf77e51d1e1c792', '/v2/task/page', 'GET', 'com.chua.starter.system.support.controller.SysTaskController', 'page', 'system', 'MAPPING', 0, NULL);
INSERT INTO `sys_mapping` VALUES ('35518309d9d486508036ade9380734bc', '/v2/menus/my/**', 'GET', 'com.chua.starter.system.support.controller.SysMenuController', 'myMenu', 'system', 'MAPPING', 0, '获取我的菜单');
INSERT INTO `sys_mapping` VALUES ('367804e9e4d3767eb2295a201f7b54ea', '/v1/mapping/update', 'PUT', 'com.chua.starter.common.api.support.RequestMappingHandlerMappingController', 'update', 'system', 'MAPPING', 0, NULL);
INSERT INTO `sys_mapping` VALUES ('38761ab1e9fc7ee00726ccb9ee010170', '/gitee/bind/user', 'GET', 'com.chua.starter.system.support.third.controller.GiteeThirdPartyProvider', 'thirdIndex', 'system', 'MAPPING', 0, NULL);
INSERT INTO `sys_mapping` VALUES ('3a62313bcd5d3d28ad5ff787e5bd16f5', '/error', '', 'org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController', 'error', 'system', 'MAPPING', 0, NULL);
INSERT INTO `sys_mapping` VALUES ('3fcf6051f1a217c3a13985eafed5d20b', '/v2/oshi/space', 'GET', 'com.chua.starter.system.support.controller.SysOshiController', 'space', 'system', 'MAPPING', 0, '磁盘空间');
INSERT INTO `sys_mapping` VALUES ('451f14ad832887e64367c89905be28a8', '/v2/menus/tree', 'GET', 'com.chua.starter.system.support.controller.SysMenuController', 'tree', 'system', 'MAPPING', 0, '获取菜单');
INSERT INTO `sys_mapping` VALUES ('4f5c0af1768281ae56cd2cfa13b90ad3', '/v2/dept/update', 'PUT', 'com.chua.starter.system.support.controller.SysDeptController', 'update', 'system', 'MAPPING', 0, '更新组织机构');
INSERT INTO `sys_mapping` VALUES ('53503942d21fd91951ee12ed59811399', '/v1/mapping/save', 'POST', 'com.chua.starter.common.api.support.RequestMappingHandlerMappingController', 'save', 'system', 'MAPPING', 0, NULL);
INSERT INTO `sys_mapping` VALUES ('556f194a7e91cd24c1ab569bdf6ca094', '/v2/dict/save', 'POST', 'com.chua.starter.system.support.controller.SysDictController', 'save', 'system', 'MAPPING', 0, '新增字典');
INSERT INTO `sys_mapping` VALUES ('5aa6d002ecfef012f01bdea528d42465', '/v2/task/runTask', 'POST', 'com.chua.starter.system.support.controller.SysTaskController', 'runTask', 'system', 'MAPPING', 0, '开启任务');
INSERT INTO `sys_mapping` VALUES ('5ccf88308525791cb4435d30174d3a85', '/oss/release/delete/{id}', 'GET', 'com.chua.starter.oss.support.provider.OssProvider', 'delete', 'system', 'MAPPING', 0, NULL);
INSERT INTO `sys_mapping` VALUES ('607e90d649831f2333069819462aa983', '/v2/task/subscribe/{taskTid}', 'GET', 'com.chua.starter.system.support.controller.SysTaskController', 'subscribe', 'system', 'MAPPING', 0, NULL);
INSERT INTO `sys_mapping` VALUES ('60c9f5978aaeca22a27d6965e2323161', '/v2/role/save', 'POST', 'com.chua.starter.system.support.controller.SysRoleController', 'save', 'system', 'MAPPING', 0, '新增角色');
INSERT INTO `sys_mapping` VALUES ('65fe2d37d7a926f19813bec61a98dcb0', '/v2/users/page', 'GET', 'com.chua.starter.system.support.controller.SysUserController', 'getUserPage', 'system', 'MAPPING', 0, '用户分页列表');
INSERT INTO `sys_mapping` VALUES ('69524a105a78966bea0c018725132f4d', '/v2/users/loginCode', 'GET', 'com.chua.starter.system.support.controller.SysUserController', 'loginCode', 'system', 'MAPPING', 0, '登录码');
INSERT INTO `sys_mapping` VALUES ('6e37d0968056730859ac81f09d124d5e', '/v1/mapping/delete', 'PUT', 'com.chua.starter.common.api.support.RequestMappingHandlerMappingController', 'delete', 'system', 'MAPPING', 0, NULL);
INSERT INTO `sys_mapping` VALUES ('755eba66d968d4b9618588f5b0b73f99', '/v2/menus/list', 'GET', 'com.chua.starter.system.support.controller.SysMenuController', 'list', 'system', 'MAPPING', 0, '获取菜单');
INSERT INTO `sys_mapping` VALUES ('776f6116627fda05f7a0c6108166bdfb', '/v2/dict/delete', 'DELETE', 'com.chua.starter.system.support.controller.SysDictController', 'delete', 'system', 'MAPPING', 0, '删除字典');
INSERT INTO `sys_mapping` VALUES ('7ae839bdc01dad659f937672a0b09108', '/v2/role/getRole', 'GET', 'com.chua.starter.system.support.controller.SysRoleController', 'updateRole', 'system', 'MAPPING', 0, '修改角色数据');
INSERT INTO `sys_mapping` VALUES ('7b8b3b15a51c9959d53aa5aa3d070943', '/v2/dept/delete', 'DELETE', 'com.chua.starter.system.support.controller.SysDeptController', 'delete', 'system', 'MAPPING', 0, '删除组织机构');
INSERT INTO `sys_mapping` VALUES ('7c6b74bd1bb72efa2f436de54ec386c1', '/v2/users/resetOwner', 'GET', 'com.chua.starter.system.support.controller.SysUserController', 'resetOwner', 'system', 'MAPPING', 0, '重置用户密碼');
INSERT INTO `sys_mapping` VALUES ('7c93cbf82fbfa3ff86b59ab37ce9b87c', '/v2/users/login', 'POST', 'com.chua.starter.system.support.controller.SysUserController', 'login', 'system', 'MAPPING', 0, '登录');
INSERT INTO `sys_mapping` VALUES ('7f2c74202a72729ad603b8b25f139efb', '/oss/release/preview/{bucket}/{path}/**', 'GET', 'com.chua.starter.oss.support.provider.OssProvider', 'preview', 'system', 'MAPPING', 0, NULL);
INSERT INTO `sys_mapping` VALUES ('81db455bf06c1284ce681ce733a9e22e', '/oss/release/options', 'GET', 'com.chua.starter.oss.support.provider.OssProvider', 'option', 'system', 'MAPPING', 0, NULL);
INSERT INTO `sys_mapping` VALUES ('8344f3c7fdbdfcfb33ebcf466b038609', '/oss/release/upload', 'POST', 'com.chua.starter.oss.support.provider.OssProvider', 'saveOss', 'system', 'MAPPING', 0, NULL);
INSERT INTO `sys_mapping` VALUES ('83607c34464582949c86f11e008abc99', '/v1/log/mylog', 'GET', 'com.chua.starter.system.support.controller.SysLogController', 'mylog', 'system', 'MAPPING', 0, NULL);
INSERT INTO `sys_mapping` VALUES ('87ed89a7719e81839ee989f50e67950d', '/v2/role/batchDelete', 'DELETE', 'com.chua.starter.system.support.controller.SysRoleController', 'batchDelete', 'system', 'MAPPING', 0, '批量删除角色');
INSERT INTO `sys_mapping` VALUES ('969ff3502a5ea5f93f9184ec43150b71', '/v2/menus/delete', 'DELETE', 'com.chua.starter.system.support.controller.SysMenuController', 'delete', 'system', 'MAPPING', 0, '删除菜单');
INSERT INTO `sys_mapping` VALUES ('9b01202a732176a563fe1d4181d4f87b', '/oss/release/save', 'POST', 'com.chua.starter.oss.support.provider.OssProvider', 'save', 'system', 'MAPPING', 0, NULL);
INSERT INTO `sys_mapping` VALUES ('9d1f4b09f3adcc8ff0b7dc1c263b7c44', '/oss/release/deleteObject', 'GET', 'com.chua.starter.oss.support.provider.OssProvider', 'deleteObject', 'system', 'MAPPING', 0, NULL);
INSERT INTO `sys_mapping` VALUES ('a093b0dfcccc88f41e746d6c33fb16bb', '/v2/task/pauseTask', 'POST', 'com.chua.starter.system.support.controller.SysTaskController', 'pauseTask', 'system', 'MAPPING', 0, '暂停任务');
INSERT INTO `sys_mapping` VALUES ('a3f1416979cfbd8dd821fb9c16cce1b1', '/v2/role/delete', 'DELETE', 'com.chua.starter.system.support.controller.SysRoleController', 'delete', 'system', 'MAPPING', 0, '删除角色');
INSERT INTO `sys_mapping` VALUES ('a5e9b8cb2ea64be7a5372f13c85d4204', '/v2/task/getTask', 'GET', 'com.chua.starter.system.support.controller.SysTaskController', 'getTask', 'system', 'MAPPING', 0, NULL);
INSERT INTO `sys_mapping` VALUES ('a69504ddc24e8e134c53402393a2358d', '/v2/task/options', 'GET', 'com.chua.starter.system.support.controller.SysTaskController', 'options', 'system', 'MAPPING', 0, NULL);
INSERT INTO `sys_mapping` VALUES ('a7116a6f00691cb3a89539ca8f805091', '/v2/api-docs', 'GET', 'springfox.documentation.swagger2.web.Swagger2ControllerWebMvc', 'getDocumentation', 'system', 'MAPPING', 0, NULL);
INSERT INTO `sys_mapping` VALUES ('b39792314e97836509b6c1f46f1c9702', '/swagger-resources/configuration/ui', 'GET', 'springfox.documentation.swagger.web.ApiResourceController', 'uiConfiguration', 'system', 'MAPPING', 0, NULL);
INSERT INTO `sys_mapping` VALUES ('b592d1f43f25c90da6e3e4d9de015a66', '/v2/users/grid', 'POST', 'com.chua.starter.system.support.controller.SysUserController', 'grid', 'system', 'MAPPING', 0, '保存用户首页布局');
INSERT INTO `sys_mapping` VALUES ('c0490da8fe3b08248090293d88bcd338', '/v2/dept/list', 'GET', 'com.chua.starter.system.support.controller.SysDeptController', 'list', 'system', 'MAPPING', 0, '机构列表');
INSERT INTO `sys_mapping` VALUES ('c27d577d50e24c5af268e23e7a40220b', '/v2/task/subscribe', 'GET', 'com.chua.starter.system.support.controller.SysTaskController', 'subscribe', 'system', 'MAPPING', 0, NULL);
INSERT INTO `sys_mapping` VALUES ('c67fc7aed8eabb08d5a67eb605e8aadf', '/v2/dict/type/save', 'POST', 'com.chua.starter.system.support.controller.SysDictTypeController', 'save', 'system', 'MAPPING', 0, '新增字典类型');
INSERT INTO `sys_mapping` VALUES ('c7f5129064cf872d6e4eaf2225f7ce14', '/v2/users/logout', 'DELETE', 'com.chua.starter.system.support.controller.SysUserController', 'logout', 'system', 'MAPPING', 0, '注销');
INSERT INTO `sys_mapping` VALUES ('c8704547fe37641fb7a6de1e746bdf0d', '/swagger-resources/configuration/security', 'GET', 'springfox.documentation.swagger.web.ApiResourceController', 'securityConfiguration', 'system', 'MAPPING', 0, NULL);
INSERT INTO `sys_mapping` VALUES ('c9eafad8a821e65f893d8b6970cdd7da', '/v2/library/getMapping', 'GET', 'com.chua.starter.system.support.controller.SysLibraryController', 'getMapping', 'system', 'MAPPING', 0, '获取识别库映射关系');
INSERT INTO `sys_mapping` VALUES ('ca13697ada12b4dede0b3c9f490ec71b', '/v2/users/reset', 'GET', 'com.chua.starter.system.support.controller.SysUserController', 'reset', 'system', 'MAPPING', 0, '重置用户密碼');
INSERT INTO `sys_mapping` VALUES ('cc81cb45d40154b542221cafd0015d9f', '/v3/api-docs', 'GET', 'springfox.documentation.oas.web.OpenApiControllerWebMvc', 'getDocumentation', 'system', 'MAPPING', 0, NULL);
INSERT INTO `sys_mapping` VALUES ('cd0f1d33099dd6995ea936296cf2acee', '/v2/users/delete', 'DELETE', 'com.chua.starter.system.support.controller.SysUserController', 'delete', 'system', 'MAPPING', 0, '删除用户');
INSERT INTO `sys_mapping` VALUES ('cd1c6918351bde5a14a45031cc647dd4', '/v2/task/update', 'PUT', 'com.chua.starter.system.support.controller.SysTaskController', 'update', 'system', 'MAPPING', 0, NULL);
INSERT INTO `sys_mapping` VALUES ('ce6431f6a1d95f3eeb2ebbcea4f733e6', '/v2/dict/type/list', 'GET', 'com.chua.starter.system.support.controller.SysDictTypeController', 'list', 'system', 'MAPPING', 0, '字典类型列表');
INSERT INTO `sys_mapping` VALUES ('d6ecab6066a44f16966686457299d2df', '/v2/library/update', 'PUT', 'com.chua.starter.system.support.controller.SysLibraryController', 'update', 'system', 'MAPPING', 0, '更新识别库');
INSERT INTO `sys_mapping` VALUES ('d9d789f1d73af11f10de6371c70a7d78', '/options/s2d3ff', 'OPTIONS', 'com.chua.starter.common.support.provider.OptionsProvider', 'options', 'system', 'MAPPING', 0, NULL);
INSERT INTO `sys_mapping` VALUES ('db9f3d7ac77ee820372789a96e5a57a8', '/v2/dept/page', 'GET', 'com.chua.starter.system.support.controller.SysDeptController', 'page', 'system', 'MAPPING', 0, '机构分页列表');
INSERT INTO `sys_mapping` VALUES ('deedf2576562b7592b7ad60652b3a562', '/v2/library/delete', 'DELETE', 'com.chua.starter.system.support.controller.SysLibraryController', 'delete', 'system', 'MAPPING', 0, '删除识别库');
INSERT INTO `sys_mapping` VALUES ('e482b3c92049c56cc377dbc3943cad1a', '/v2/task/getTaskStatus', 'GET', 'com.chua.starter.system.support.controller.SysTaskController', 'getTaskStatus', 'system', 'MAPPING', 0, NULL);
INSERT INTO `sys_mapping` VALUES ('eb495e76cacd739762ca7d077ff97d11', '/v2/dict/type/update', 'PUT', 'com.chua.starter.system.support.controller.SysDictTypeController', 'update', 'system', 'MAPPING', 0, '修改字典类型');
INSERT INTO `sys_mapping` VALUES ('eb930e090fc9751c3caa0745bfd51198', '/v2/role/updateRole', 'POST', 'com.chua.starter.system.support.controller.SysRoleController', 'updateRole', 'system', 'MAPPING', 0, '修改角色数据');
INSERT INTO `sys_mapping` VALUES ('ec11a8a8439a440f319e4e3b689db46e', '/oss/release/listObjects', 'POST', 'com.chua.starter.oss.support.provider.OssProvider', 'listDeepObjects', 'system', 'MAPPING', 0, NULL);
INSERT INTO `sys_mapping` VALUES ('ecb77b38f22b2dddc283cce2668bffd4', '/v2/task/pageFile', 'GET', 'com.chua.starter.system.support.controller.SysTaskController', 'pageFile', 'system', 'MAPPING', 0, NULL);
INSERT INTO `sys_mapping` VALUES ('f287008d0c16a40ac5e66f6555a6810e', '/oss/release/update', 'POST', 'com.chua.starter.oss.support.provider.OssProvider', 'updateById', 'system', 'MAPPING', 0, NULL);
INSERT INTO `sys_mapping` VALUES ('f5be627a65c32a1814c1f3a487edce30', '/v1/log/page', 'GET', 'com.chua.starter.system.support.controller.SysLogController', 'page', 'system', 'MAPPING', 0, NULL);
INSERT INTO `sys_mapping` VALUES ('f83f77220364e83c4961730ef2706d59', '/v2/dict/type/delete', 'DELETE', 'com.chua.starter.system.support.controller.SysDictTypeController', 'delete', 'system', 'MAPPING', 0, '删除字典类型');
INSERT INTO `sys_mapping` VALUES ('f92270d273378a5a7a30a1375ab63dc9', '/v2/menus/save', 'POST', 'com.chua.starter.system.support.controller.SysMenuController', 'save', 'system', 'MAPPING', 0, '新增菜单');

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `menu_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `menu_parent_id` bigint NULL DEFAULT NULL COMMENT '父菜单ID',
  `menu_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单名称',
  `menu_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单类型(menu-菜单；link-外链；button-按钮权限)',
  `menu_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '路由路径(浏览器地址栏路径)',
  `menu_component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件路径(vue页面完整路径，省略.vue后缀)',
  `menu_tag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签',
  `menu_color` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '颜色',
  `menu_perm` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '权限标识',
  `menu_visible` int NULL DEFAULT 1 COMMENT '显示状态(1:显示;0:隐藏), 任会返回给前端',
  `menu_status` int NULL DEFAULT 1 COMMENT '显示状态(1:显示;0:隐藏), 不返回给前端',
  `menu_sort` int NULL DEFAULT NULL COMMENT '排序',
  `menu_icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单图标',
  `menu_redirect` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '跳转路径',
  `menu_tree_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '父节点路径，以英文逗号(,)分割',
  `menu_affix` int NULL DEFAULT 0,
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `update_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`menu_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 37 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜单实体对象' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, 0, '首页', 'menu', '/home', NULL, NULL, NULL, NULL, 1, 1, NULL, 'el-icon-eleme-filled', NULL, '0', 0, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (2, 1, '控制台', 'menu', '/dashboard', 'home', NULL, NULL, NULL, 1, 1, NULL, 'el-icon-menu', NULL, '0,1', 1, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (3, 0, '系统管理', 'menu', '/setting', NULL, NULL, NULL, NULL, 1, 1, NULL, 'el-icon-setting', NULL, '0', 0, NULL, NULL, NULL, '2023-07-19 19:23:29.816000', NULL, NULL);
INSERT INTO `sys_menu` VALUES (4, 3, '用户管理', 'menu', '/setting/user', 'setting/user', NULL, NULL, NULL, 1, 1, 2, 'el-icon-user-filled', NULL, '0,3', 0, NULL, NULL, NULL, '2023-07-23 11:09:59.947000', '系统管理员', NULL);
INSERT INTO `sys_menu` VALUES (5, 3, '菜单管理', 'menu', '/setting/menu', 'setting/menu', NULL, NULL, NULL, 1, 1, 100, 'el-icon-fold', NULL, '0,3', 0, NULL, NULL, NULL, '2023-07-20 14:38:51.853000', NULL, NULL);
INSERT INTO `sys_menu` VALUES (6, 0, '其它', 'menu', '/other', '', NULL, NULL, NULL, 1, 1, 1, 'el-icon-add-location', NULL, '', 0, NULL, NULL, '2023-07-19 17:09:51.356000', '2023-07-19 19:23:07.348000', NULL, NULL);
INSERT INTO `sys_menu` VALUES (7, 6, '外部链接', 'link', 'http://www.baidu.com', '', NULL, NULL, NULL, 1, 1, 1, 'el-icon-aim', NULL, '', 0, NULL, NULL, '2023-07-19 17:09:59.980000', '2023-07-19 17:18:35.454000', NULL, NULL);
INSERT INTO `sys_menu` VALUES (8, 6, '内部链接', 'iframe', 'http://www.baidu.com', '', NULL, NULL, NULL, 1, 1, 2, 'el-icon-add-location', NULL, '', 0, NULL, NULL, '2023-07-19 17:17:18.884000', '2023-07-19 17:19:23.222000', NULL, NULL);
INSERT INTO `sys_menu` VALUES (9, 3, '字典管理', 'menu', '/setting/dic', '/setting/dic', '', '#A74E4E', NULL, 1, 1, NULL, 'el-icon-collection', NULL, '0,3', 0, NULL, NULL, '2023-07-19 19:24:06.447000', '2023-07-20 09:52:47.754000', NULL, NULL);
INSERT INTO `sys_menu` VALUES (10, 3, '角色管理', 'menu', '/setting/role', '/setting/role', '', NULL, NULL, 1, 1, 3, 'el-icon-notebook', NULL, '0,3', 0, NULL, NULL, '2023-07-20 09:49:50.586000', '2023-07-21 09:14:19.321000', NULL, NULL);
INSERT INTO `sys_menu` VALUES (11, 3, '操作日志', 'menu', '/setting/log', '/setting/log', NULL, NULL, NULL, 1, 1, 99, 'el-icon-warning', NULL, '0,3', 0, NULL, NULL, '2023-07-20 14:38:59.874000', '2023-07-20 14:40:09.108000', NULL, NULL);
INSERT INTO `sys_menu` VALUES (12, 3, '部门管理', 'menu', '/setting/dept', '/setting/dept', '', NULL, NULL, 1, 1, 1, 'sc-icon-organization', NULL, '0,3', 0, NULL, NULL, '2023-07-21 09:13:54.152000', '2023-07-21 10:44:25.501000', NULL, NULL);
INSERT INTO `sys_menu` VALUES (13, 3, 'OSS管理', 'menu', '/setting/oss', '/setting/oss', '', NULL, NULL, 1, 1, 1002, 'sc-icon-file-manager', NULL, '0,3', 0, '系统管理员', '1', '2023-07-23 11:05:59.249000', '2023-07-31 15:16:39.763000', '系统管理员', '1');
INSERT INTO `sys_menu` VALUES (14, 3, '任务管理', 'menu', '/setting/task', 'setting/task', '', NULL, NULL, 1, 1, 1001, 'el-icon-alarm-clock', NULL, '0,3', 0, '系统管理员', '1', '2023-07-23 11:08:10.395000', '2023-08-14 11:45:59.468000', '系统管理员', '1');
INSERT INTO `sys_menu` VALUES (15, 6, '空白模板', 'menu', '/template/layout/blank', '/template/layout/blank.vue', NULL, NULL, NULL, 1, 1, NULL, 'sc-icon-template', NULL, '', 0, '系统管理员', '1', '2023-07-28 15:27:40.503000', '2023-07-28 15:48:47.482000', '系统管理员', '1');
INSERT INTO `sys_menu` VALUES (16, 1, '账号信息', 'menu', '/usercenter', '/usercenter', NULL, NULL, NULL, 0, 1, NULL, 'el-icon-avatar', NULL, '0,1', 0, '系统管理员', '1', '2023-07-31 08:37:42.304000', '2023-07-31 08:38:18.873000', '系统管理员', '1');
INSERT INTO `sys_menu` VALUES (17, 0, '机器学习', 'menu', '/learning', '/learning', '', NULL, NULL, 1, 1, NULL, 'sc-icon-people-face', NULL, '', 0, '系统管理员', '1', '2023-07-31 15:14:23.790000', '2023-08-14 11:14:58.193000', '系统管理员', '1');
INSERT INTO `sys_menu` VALUES (18, 17, '人脸比对1:1', 'menu', '/learning/faceCompare', '/learning/faceCompare', '', NULL, NULL, 1, 1, NULL, 'sc-icon-face-compare', NULL, '', 0, '系统管理员', '1', '2023-07-31 15:14:44.121000', '2023-07-31 15:31:05.122000', '系统管理员', '1');
INSERT INTO `sys_menu` VALUES (19, 17, '图像比对1:1', 'menu', '/learning/imageCompare', '/learning/imageCompare', '', NULL, NULL, 1, 1, NULL, 'sc-icon-image-compare', NULL, '', 0, '系统管理员', '1', '2023-07-31 15:16:59.334000', '2023-07-31 15:29:17.901000', '系统管理员', '1');
INSERT INTO `sys_menu` VALUES (20, 17, 'OCR识别', 'menu', '/learning/ocr', '/learning/ocr', '', NULL, NULL, 1, 1, NULL, 'sc-icon-image-reg', NULL, '', 0, '系统管理员', '1', '2023-07-31 15:28:47.536000', '2023-07-31 15:30:42.470000', '系统管理员', '1');
INSERT INTO `sys_menu` VALUES (21, 17, '人脸检测', 'menu', '/learning/faceDetection', '/learning/faceDetection', NULL, NULL, NULL, 1, 1, NULL, 'sc-icon-face-detection', NULL, '', 0, '系统管理员', '1', '2023-08-01 14:39:31.813000', '2023-08-01 14:46:20.276000', '系统管理员', '1');
INSERT INTO `sys_menu` VALUES (22, 17, '底库', 'menu', '/learning/library/:indexName/:libType', '/learning/library', NULL, NULL, NULL, 0, 1, NULL, 'sc-icon-face-reg-library', NULL, '', 0, '系统管理员', '1', '2023-08-02 13:24:18.930000', '2023-08-03 15:11:58.106000', '系统管理员', '1');
INSERT INTO `sys_menu` VALUES (23, 17, '底库管理', 'menu', '/setting/library', '/setting/library', NULL, NULL, NULL, 1, 1, NULL, 'sc-icon-face-reg-library', NULL, '', 0, '系统管理员', '1', '2023-08-03 13:06:40.846000', '2023-08-10 11:18:36.120000', '系统管理员', '1');
INSERT INTO `sys_menu` VALUES (24, 17, 'GAN', 'menu', '/learning/gan', '/learning/gan', NULL, NULL, NULL, 1, 1, NULL, 'sc-icon-sc', NULL, '', 0, '系统管理员', '1', '2023-08-08 10:19:00.123000', '2023-08-10 11:18:45.435000', '系统管理员', '1');
INSERT INTO `sys_menu` VALUES (26, 0, '调度管理', 'menu', '/scheduler', '/scheduler', 'NEW', NULL, NULL, 1, 1, NULL, 'sc-icon-scheduler', NULL, '', 0, '系统管理员', '1', '2023-08-08 14:45:21.839000', '2023-08-09 13:39:29.964000', '系统管理员', '1');
INSERT INTO `sys_menu` VALUES (27, 26, '调度任务管理', 'menu', '/scheduler/jobinfo', '/scheduler/jobinfo', NULL, NULL, '任务管理', 1, 1, NULL, 'el-icon-alarm-clock', NULL, '', 0, '系统管理员', '1', '2023-08-08 14:57:43.683000', '2023-08-14 13:33:55.373000', '系统管理员', '1');
INSERT INTO `sys_menu` VALUES (28, 26, '调度日志', 'menu', '/scheduler/joblog/:jobGroup/:jobId', '/scheduler/joblog', NULL, NULL, NULL, 1, 1, NULL, 'el-icon-warning', NULL, '', 0, '系统管理员', '1', '2023-08-08 15:53:41.485000', '2023-08-09 13:39:41.037000', '系统管理员', '1');
INSERT INTO `sys_menu` VALUES (29, 26, '执行器管理', 'menu', '/scheduler/jobgroup', '/scheduler/jobgroup', NULL, NULL, NULL, 1, 1, NULL, 'el-icon-takeaway-box', NULL, '', 0, '系统管理员', '1', '2023-08-09 13:38:58.388000', '2023-08-09 13:40:06.860000', '系统管理员', '1');
INSERT INTO `sys_menu` VALUES (30, 17, '人脸1:N', 'menu', '/learning/faceCompareN', '/learning/faceCompareN', NULL, NULL, NULL, 1, 1, NULL, 'sc-icon-face-detection', NULL, '', 0, '系统管理员', '1', '2023-08-10 11:17:50.071000', '2023-08-10 11:18:19.043000', '系统管理员', '1');
INSERT INTO `sys_menu` VALUES (32, 0, '配置中心', 'menu', '/config', '/config', NULL, NULL, NULL, 1, 1, NULL, 'sc-icon-mapping-v2', NULL, '', 0, '系统管理员', '1', '2023-08-14 11:13:17.369000', '2023-08-14 11:13:32.454000', '系统管理员', '1');
INSERT INTO `sys_menu` VALUES (34, 32, '统一配置', 'menu', '/config/config', '/config/config', NULL, NULL, NULL, 1, 1, NULL, 'el-icon-grid', NULL, '', 0, '系统管理员', '1', '2023-08-14 11:13:41.480000', '2023-08-14 11:14:17.265000', '系统管理员', '1');
INSERT INTO `sys_menu` VALUES (35, 32, '统一脚本', 'menu', '/config/bean', '/config/bean', NULL, NULL, NULL, 1, 1, NULL, '', NULL, '', 0, '系统管理员', '1', '2023-08-15 16:13:18.060000', '2023-08-15 16:13:32.286000', '系统管理员', '1');
INSERT INTO `sys_menu` VALUES (36, 32, '统一日志', 'menu', '/config/log', '/config/log', NULL, NULL, NULL, 1, 1, NULL, 'el-icon-warning', NULL, '', 0, '系统管理员', '1', '2023-08-17 15:07:45.201000', '2023-08-17 15:08:04.393000', '系统管理员', '1');
INSERT INTO `sys_menu` VALUES (37, 32, '统一链路', 'menu', '/config/trace', '/config/trace', NULL, NULL, NULL, 1, 1, NULL, 'sc-icon-trace', NULL, '', 0, '系统管理员', '1', '2023-08-18 14:28:13.602000', '2023-08-18 14:29:56.839000', '系统管理员', '1');

-- ----------------------------
-- Table structure for sys_oss
-- ----------------------------
DROP TABLE IF EXISTS `sys_oss`;
CREATE TABLE `sys_oss`  (
  `oss_id` int NOT NULL AUTO_INCREMENT COMMENT 'ossID',
  `oss_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'oss路径',
  `oss_status` int NULL DEFAULT 1 COMMENT 'oss是否禁用; 0:禁用; 1:开启',
  `oss_bucket` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'bucket',
  `oss_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'local' COMMENT 'oss类型',
  `oss_app_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'appKey',
  `oss_app_secret` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'appSecret',
  `oss_buffer` int NULL DEFAULT 4096 COMMENT '传输字节',
  `oss_covering` bit(1) NULL DEFAULT b'1' COMMENT '重名覆盖',
  `oss_name_strategy` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'original' COMMENT '命名策略',
  `oss_reject_strategy` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'null' COMMENT '拒绝策略',
  `oss_properties` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '额外参数',
  `oss_plugins` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '插件,分隔',
  PRIMARY KEY (`oss_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_oss
-- ----------------------------
INSERT INTO `sys_oss` VALUES (1, './avatar', 1, 'avatar', 'local', NULL, NULL, 4096, b'1', 'user', 'null', NULL, NULL);
INSERT INTO `sys_oss` VALUES (2, './task', 1, 'task', 'local', NULL, NULL, 4096, b'1', 'TID', 'null', NULL, '');
INSERT INTO `sys_oss` VALUES (3, './face', 1, 'face', 'LOCAL', NULL, NULL, 4096, b'1', 'original', 'null', NULL, '');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `role_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色名称',
  `role_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色编码',
  `role_sort` int NULL DEFAULT 1 COMMENT '显示顺序',
  `role_status` int NULL DEFAULT 1 COMMENT '角色状态(1-正常；0-停用)',
  `role_sys` int NULL DEFAULT 0 COMMENT '是否系统角色; 0:非系统角色',
  `role_data_scope` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '1' COMMENT '数据权限; 1: 全部可见<br/>2: 本人可见<br/>3: 所在部门可见<br/>4: 所在部门及子级可见<br/>5: 选择的部门可见<br/>6: 自定义<br/>',
  `role_data_rule` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `role_dashboard` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '控制台样式',
  `role_grid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `update_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '系统管理员', 'ADMIN', 0, 1, 1, '1', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_role` VALUES (2, '运维', 'OPS', 0, 1, 1, '2', '', '0', 'ver,welcome', NULL, NULL, '2023-07-20 10:06:14.782000', '2023-07-20 10:44:54.408000', 'asdas', NULL);
INSERT INTO `sys_role` VALUES (3, '测试', 'CS', 1, 1, 0, '1', NULL, NULL, NULL, 'admin', 'a433c16b8b475be5174fbe9e1110cba0', '2023-07-21 20:27:04.142000', '2023-07-21 20:29:33.612000', 'admin', NULL);

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `role_id` bigint NULL DEFAULT NULL,
  `menu_id` bigint NULL DEFAULT NULL,
  INDEX `index_role_id`(`role_id` DESC) USING BTREE,
  INDEX `index_menu_id`(`menu_id` DESC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------

-- ----------------------------
-- Table structure for sys_task
-- ----------------------------
DROP TABLE IF EXISTS `sys_task`;
CREATE TABLE `sys_task`  (
  `task_id` int NOT NULL AUTO_INCREMENT COMMENT '表ID',
  `task_tid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务ID(系统生成的唯一标识)',
  `task_expire` int NULL DEFAULT 86400 COMMENT '任务执行的超时时间(s)',
  `task_delete` int NULL DEFAULT 1 COMMENT '任务过期记录是否删除; 1: 删除; 0: 不删除',
  `task_delete_list` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务删除列表多个,分割',
  `task_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务类型',
  `task_cid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务的实现Bean',
  `task_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务名称',
  `task_business` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '业务ID',
  `task_offset` int NULL DEFAULT NULL COMMENT '游标',
  `task_total` bigint NULL DEFAULT 0 COMMENT '总量',
  `task_current` bigint NULL DEFAULT 0 COMMENT '处理量',
  `task_status` int NULL DEFAULT 0 COMMENT '是否完成; 0: 未完成; 1:完成;2: 暂停; 3: 进行中',
  `task_notice_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'export' COMMENT '通知类型;导出: export, 信息: report',
  `task_version` int NULL DEFAULT NULL COMMENT '版本',
  `task_params` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务参数',
  `task_cost` bigint NULL DEFAULT 0 COMMENT '任务耗时',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `update_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`task_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_task
-- ----------------------------

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户编号',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名',
  `user_real_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户真实姓名',
  `user_marker` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户备注',
  `user_nickname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `user_gender` int NULL DEFAULT NULL COMMENT '性别(1:男;2:女)',
  `user_sys` int NULL DEFAULT NULL COMMENT ' 是否系统账号(1:是)',
  `user_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
  `user_register_ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '注册IP',
  `user_register_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '注册地址',
  `dept_id` bigint NULL DEFAULT NULL COMMENT '部门ID',
  `user_avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户头像',
  `user_mobile` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系方式',
  `user_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '住址',
  `user_status` int NULL DEFAULT 1 COMMENT '用户状态(1:正常;0:禁用)',
  `user_email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户邮箱',
  `user_deleted` int NULL DEFAULT 0 COMMENT '逻辑删除标识(0:未删除;1:已删除)',
  `user_salt` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '盐',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `update_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, NULL, 'admin', '系统管理员', '系统管理员000', '系统管理员', 1, 1, 'ce8485d03f09fdcf2c6b11bf068ce15d', NULL, NULL, 1, 'https://oss.youlai.tech/youlai-boot/2023/05/16/811270ef31f548af9cffc026dfc3777b.gif', '17621210366', NULL, 1, '', 0, 'gQD1AdXX', NULL, NULL, '2019-10-10 13:41:22.000000', '2023-07-22 20:32:09.562000', '系统管理员', NULL);
INSERT INTO `sys_user` VALUES (2, '879863248681046016', '1', '1', NULL, NULL, NULL, 0, 'ce8485d03f09fdcf2c6b11bf068ce15d', '192.168.0.108', '局域网', NULL, '/api/system/oss/release/preview/avatar/a433c16b8b475be5174fbe9e1110cba0.jpg', NULL, NULL, 1, NULL, 1, 'gQD1AdXX', 'admin', 'a433c16b8b475be5174fbe9e1110cba0', '2023-07-21 20:23:31.189000', '2023-07-21 20:43:53.536000', 'admin', NULL);
INSERT INTO `sys_user` VALUES (3, '879863554085359616', 'test', '测试', NULL, NULL, NULL, 0, 'ce8485d03f09fdcf2c6b11bf068ce15d', '192.168.0.108', '局域网', 1, '/api/system/oss/release/preview/avatar/a433c16b8b475be5174fbe9e1110cba0.jpg', NULL, NULL, 1, NULL, 0, 'gQD1AdXX', 'admin', 'a433c16b8b475be5174fbe9e1110cba0', '2023-07-21 20:24:44.000000', '2023-08-11 11:22:14.780000', '系统管理员', '1');
INSERT INTO `sys_user` VALUES (4, '879863731886493696', 'asd', 'asdas', '2300000000000000000000000000', NULL, 0, 0, 'ce8485d03f09fdcf2c6b11bf068ce15d', '192.168.0.108', '局域网', 18, '/api/system/oss/release/preview/avatar/a433c16b8b475be5174fbe9e1110cba0.jpeg', '12', NULL, 1, '12', 0, 'gQD1AdXX', 'admin', 'a433c16b8b475be5174fbe9e1110cba0', '2023-07-21 20:25:26.391000', '2023-07-22 23:02:07.714000', 'asdas', NULL);

-- ----------------------------
-- Table structure for sys_user_grid
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_grid`;
CREATE TABLE `sys_user_grid`  (
  `grid` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `layout` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '布局',
  `copms_list` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件',
  PRIMARY KEY (`grid`) USING BTREE,
  INDEX `index_user_id`(`user_id` DESC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_grid
-- ----------------------------
INSERT INTO `sys_user_grid` VALUES (1, 12, '[12,6,6]', '[[\"ver\"],[\"welcome\"],[]]');
INSERT INTO `sys_user_grid` VALUES (2, 1, '[24,16,8]', '[[\"space\"],[],[]]');

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `user_id` bigint NULL DEFAULT NULL,
  `role_id` bigint NULL DEFAULT NULL,
  INDEX `index_user_id`(`user_id` DESC) USING BTREE,
  INDEX `index_role_id`(`role_id` DESC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (10, 2);
INSERT INTO `sys_user_role` VALUES (10, 2);
INSERT INTO `sys_user_role` VALUES (10, 2);
INSERT INTO `sys_user_role` VALUES (3, 3);

-- ----------------------------
-- Table structure for sys_user_third
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_third`;
CREATE TABLE `sys_user_third`  (
  `user_id` bigint NOT NULL AUTO_INCREMENT,
  `login_code_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `login_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `login_uuid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `login_avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `login_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `update_time` datetime(6) NULL DEFAULT NULL,
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `update_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_third
-- ----------------------------

-- ----------------------------
-- Procedure structure for waterDetail
-- ----------------------------
DROP PROCEDURE IF EXISTS `waterDetail`;
delimiter ;;
CREATE PROCEDURE `waterDetail`(in tableName varchar(30), in sid varchar(20))
BEGIN
	 
	 SET @sqlStr = CONCAT_WS(' ', 'SELECT t.id, t.sid, t.item, t.val, t.unit, t.dt, t.index FROM ', tableName, 
	 ' t JOIN ( SELECT r.sid, r.item, max( r.dt ) dt  FROM ', tableName, 
	 ' r GROUP BY r.sid, r.item ) s ON s.sid = t.sid AND s.item = t.item AND s.dt = t.dt WHERE 1 = 1 AND t.sid = ', sid);  

	 

END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for waterDetail111
-- ----------------------------
DROP PROCEDURE IF EXISTS `waterDetail111`;
delimiter ;;
CREATE PROCEDURE `waterDetail111`()
BEGIN
	#Routine body goes here...

END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
