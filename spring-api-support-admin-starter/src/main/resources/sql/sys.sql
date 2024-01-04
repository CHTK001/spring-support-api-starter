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

 Date: 04/01/2024 10:55:42
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
INSERT INTO `sys_role` VALUES (3, '测试', 'CS', 1, 1, 0, '1', NULL, NULL, NULL, 'admin', NULL, '2023-07-21 20:27:04.142000', '2023-07-21 20:29:33.612000', 'admin', NULL);

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `role_id` bigint NULL DEFAULT NULL,
  `menu_id` bigint NULL DEFAULT NULL,
  INDEX `index_role_id`(`role_id` DESC) USING BTREE,
  INDEX `index_menu_id`(`menu_id` DESC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色菜单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_menu
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
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, NULL, 'admin', '系统管理员', '系统管理员000', '系统管理员', 1, 1, 'ce8485d03f09fdcf2c6b11bf068ce15d', NULL, NULL, 1, 'https://oss.youlai.tech/youlai-boot/2023/05/16/811270ef31f548af9cffc026dfc3777b.gif', '17621210366', NULL, 1, '', 0, 'gQD1AdXX', NULL, NULL, '2019-10-10 13:41:22.000000', '2023-07-22 20:32:09.562000', '系统管理员', NULL);
INSERT INTO `sys_user` VALUES (2, '879863248681046016', '1', '1', NULL, NULL, NULL, 0, 'ce8485d03f09fdcf2c6b11bf068ce15d', '192.168.0.108', '局域网', NULL, '/api/system/oss/release/preview/avatar/a433c16b8b475be5174fbe9e1110cba0.jpg', NULL, NULL, 1, NULL, 1, 'gQD1AdXX', 'admin', NULL, '2023-07-21 20:23:31.189000', '2023-07-21 20:43:53.536000', 'admin', NULL);
INSERT INTO `sys_user` VALUES (3, '879863554085359616', 'test', '测试', NULL, NULL, NULL, 0, 'ce8485d03f09fdcf2c6b11bf068ce15d', '192.168.0.108', '局域网', 1, '/api/system/oss/release/preview/avatar/a433c16b8b475be5174fbe9e1110cba0.jpg', NULL, NULL, 1, NULL, 0, 'gQD1AdXX', 'admin', NULL, '2023-07-21 20:24:44.000000', '2023-08-11 11:22:14.780000', '系统管理员', '1');
INSERT INTO `sys_user` VALUES (4, '879863731886493696', 'asd', 'asdas', '2300000000000000000000000000', NULL, 0, 0, 'ce8485d03f09fdcf2c6b11bf068ce15d', '192.168.0.108', '局域网', 18, '/api/system/oss/release/preview/avatar/a433c16b8b475be5174fbe9e1110cba0.jpeg', '12', NULL, 1, '12', 0, 'gQD1AdXX', 'admin', NULL, '2023-07-21 20:25:26.391000', '2023-07-22 23:02:07.714000', 'asdas', NULL);

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
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户主页布局' ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户角色表' ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '三方登录' ROW_FORMAT = DYNAMIC;

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
