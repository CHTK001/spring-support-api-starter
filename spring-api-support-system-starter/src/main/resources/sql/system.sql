/*
 Navicat Premium Data Transfer

 Source Server         : 172.16.0.40
 Source Server Type    : MySQL
 Source Server Version : 80016 (8.0.16)
 Source Host           : 172.16.0.40:3306
 Source Schema         : system

 Target Server Type    : MySQL
 Target Server Version : 80016 (8.0.16)
 File Encoding         : 65001

 Date: 18/08/2024 13:29:03
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`  (
  `sys_dept_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_dept_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '机构名称',
  `sys_dept_pid` int(11) NULL DEFAULT NULL COMMENT '父机构',
  `sys_dept_tree_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '机构链路',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_dept_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '组织机构' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dept
-- ----------------------------

-- ----------------------------
-- Table structure for sys_dict
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict`  (
  `sys_dict_id` int(11) NOT NULL,
  `sys_dict_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典名称',
  `sys_dict_status` int(11) NULL DEFAULT NULL COMMENT '状态; 0:隐藏',
  `sys_dict_in_system` int(11) NULL DEFAULT NULL COMMENT '是否内置; 0:非内置',
  `sys_dict_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典编码, 唯一',
  `sys_dict_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_dict_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict
-- ----------------------------

-- ----------------------------
-- Table structure for sys_dict_item
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_item`;
CREATE TABLE `sys_dict_item`  (
  `sys_dict_item_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_dict_id` int(11) NULL DEFAULT NULL COMMENT '字典ID',
  `sys_dict_item_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典子项名称',
  `sys_dict_item_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `sys_dict_item_status` int(11) NULL DEFAULT NULL COMMENT '状态;0: 隐藏',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_dict_item_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字典项子项' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict_item
-- ----------------------------

-- ----------------------------
-- Table structure for sys_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log`  (
  `sys_log_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_log_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账号',
  `sys_log_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求IP',
  `sys_log_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求地址',
  `sys_log_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求参数',
  `sys_log_from` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求模块',
  `sys_log_status` int(11) NULL DEFAULT NULL COMMENT '请求状态; 0: 失败; 1: 成功',
  `sys_log_cost` int(11) NULL DEFAULT NULL COMMENT '耗时',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_log_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_log
-- ----------------------------

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `sys_menu_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_menu_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单名称',
  `sys_menu_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单路径',
  `sys_menu_i18n` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '国际化',
  `sys_menu_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标题',
  `sys_menu_perm` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '权限',
  `sys_menu_pid` int(11) NULL DEFAULT NULL COMMENT '父菜单',
  `sys_menu_tree_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单路径',
  `sys_menu_component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单组件',
  `sys_menu_icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单图标',
  `sys_menu_type` int(11) NULL DEFAULT NULL COMMENT '菜单 0 按钮 1',
  `sys_menu_sort` int(11) NULL DEFAULT NULL COMMENT '优先级',
  `sys_menu_hidden` int(11) NULL DEFAULT NULL COMMENT '是否隐藏；0:显示',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_menu_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, 'system', '/system', NULL, '系统管理', NULL, 0, '0', NULL, 'line-md:cog', 0, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (2, 'menu', '/system/menu', NULL, '菜单管理', NULL, 1, '0,1', 'manage/menu/index', 'line-md:close-to-menu-alt-transition', 0, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (3, 'setting', '/system/setting', NULL, '系统配置', NULL, 1, '0,1', 'manage/setting/index', 'line-md:cog-filled-loop', 0, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (4, 'role', '/system/role', NULL, '角色管理', NULL, 1, '0,1', 'manage/role/index', NULL, 0, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `sys_role_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_role_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色名称',
  `sys_role_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色编码; 唯一',
  `sys_role_in_system` int(11) NULL DEFAULT NULL COMMENT '是否系统角色; 0：非系统角色',
  `sys_role_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'SUPER_ADMIN', 1, '超级管理员', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_role` VALUES (2, '管理员', 'ADMIN', 1, '超级管理员', NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `sys_role_menu_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_role_id` int(11) NULL DEFAULT NULL COMMENT '角色ID',
  `sys_menu_id` int(11) NULL DEFAULT NULL COMMENT '菜单ID',
  PRIMARY KEY (`sys_role_menu_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色 - 菜单关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------

-- ----------------------------
-- Table structure for sys_setting
-- ----------------------------
DROP TABLE IF EXISTS `sys_setting`;
CREATE TABLE `sys_setting`  (
  `sys_setting_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_setting_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置名称',
  `sys_setting_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置值',
  `sys_setting_status` int(11) NULL DEFAULT NULL COMMENT '是否启用; 0:不启用',
  `sys_setting_value_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置类型; 用于前端组件判断; e.g.BOOL：布尔',
  `sys_setting_group` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置所属分组',
  `sys_setting_from_app` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置所属来源',
  `sys_setting_in_system` int(11) NULL DEFAULT NULL COMMENT '是否系统配置; 0:非系统配置',
  `sys_setting_app_inner` int(11) NULL DEFAULT NULL COMMENT '配置是否作用域系统启动',
  `sys_setting_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置备注',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_setting_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统设置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_setting
-- ----------------------------
INSERT INTO `sys_setting` VALUES (1, 'openVerifyCode', 'true', 1, 'bool', 'default', NULL, 1, 0, '验证码开关', NULL, NULL, NULL, '2024-08-16 13:25:38', 'sa', NULL);
INSERT INTO `sys_setting` VALUES (2, 'systemName', '后台管理系统', 1, 'string', 'default', NULL, 1, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_setting` VALUES (3, 'openLoopDebugger', 'false', 1, 'bool', 'codec', NULL, 1, NULL, '开启循环debugger', 'sa', NULL, '2024-08-18 10:52:15', '2024-08-18 10:52:47', 'sa', NULL);
INSERT INTO `sys_setting` VALUES (4, 'openLoopRedirect', 'false', 1, 'bool', 'codec', NULL, 1, NULL, '当开启调试模式自动崩溃当前页', 'sa', NULL, '2024-08-18 11:22:26', '2024-08-18 11:23:15', 'sa', NULL);
INSERT INTO `sys_setting` VALUES (5, 'enable', 'false', 1, 'bool', 'codec', NULL, 1, NULL, '是否开启返回加密', 'sa', NULL, '2024-08-18 11:31:57', '2024-08-18 11:32:06', 'sa', NULL);
INSERT INTO `sys_setting` VALUES (6, 'openStorage', 'true', 1, 'bool', 'codec', NULL, 1, NULL, '开启缓存加密', 'sa', NULL, '2024-08-18 12:10:58', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `sys_user_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_user_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账号',
  `sys_user_nickname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `sys_user_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
  `sys_user_salt` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '盐',
  `sys_user_delete` int(11) NULL DEFAULT NULL COMMENT '是否删除; 0: 未删除',
  `sys_user_status` int(11) NULL DEFAULT NULL COMMENT '是否启用；0：禁用',
  `sys_user_phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `sys_user_email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `sys_user_sex` int(11) NULL DEFAULT NULL COMMENT '性别; 0:女;1:男; 2:其他',
  `sys_user_in_system` int(11) NULL DEFAULT NULL COMMENT '是否系统账号; 0: 非系统账号',
  `sys_user_card` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证',
  `sys_user_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `sys_user_last_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后一次登录地点',
  `sys_user_last_ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后一次登录IP',
  `sys_user_register_ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '注册地址',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'sa', '超级管理员', '4852bee906775d75337bfca410ba8e7d', '123456', 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_user_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_log`;
CREATE TABLE `sys_user_log`  (
  `sys_log_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_log_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账号',
  `sys_log_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求IP',
  `sys_log_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求地址',
  `sys_log_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求参数',
  `sys_log_cost` int(11) NULL DEFAULT NULL COMMENT '耗时',
  `sys_log_from` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求模块',
  `sys_log_status` int(11) NULL DEFAULT NULL COMMENT '请求状态; 0: 失败; 1: 成功',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_log_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_log
-- ----------------------------
INSERT INTO `sys_user_log` VALUES (2, 'sa', '192.168.0.108', '/system/api/v2/user/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"BhHV6Z\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]', NULL, 'LOGIN', 1, NULL, NULL, '2024-08-18 12:38:01', '2024-08-18 12:38:01', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (3, 'sa', '192.168.0.108', '/system/api/v2/user/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"jbq4yE\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]', NULL, 'LOGIN', 1, NULL, NULL, '2024-08-18 12:42:20', '2024-08-18 12:42:20', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (4, 'sa', '192.168.0.108', '/system/api/v2/user/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"GTPFke\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]', NULL, 'LOGIN', 1, NULL, NULL, '2024-08-18 12:43:00', '2024-08-18 12:43:00', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (5, 'sa', '192.168.0.108', '/system/api/v2/user/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"M7ydPF\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]', NULL, 'LOGIN', 1, NULL, NULL, '2024-08-18 12:44:11', '2024-08-18 12:44:11', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (6, 'sa', '192.168.0.108', '/system/api/v2/user/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"8mrfc5\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]', NULL, 'LOGIN', 1, NULL, NULL, '2024-08-18 12:51:16', '2024-08-18 12:51:16', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (7, 'sa', '192.168.0.108', '/system/api/v2/user/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"32NVqv\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]', NULL, 'LOGIN', 1, NULL, NULL, '2024-08-18 12:52:47', '2024-08-18 12:52:47', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (8, 'sa', '192.168.0.108', '/system/api/v2/user/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"t8SM9h\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]', NULL, 'LOGIN', 1, NULL, NULL, '2024-08-18 13:01:52', '2024-08-18 13:01:52', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (9, 'sa', '192.168.0.108', '/system/api/v2/user/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"tH7Jyh\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]', NULL, 'LOGIN', 1, NULL, NULL, '2024-08-18 13:04:10', '2024-08-18 13:04:10', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (10, 'sa', '192.168.0.108', '/system/api/v2/user/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"yAZSCt\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]', NULL, 'LOGIN', 1, NULL, NULL, '2024-08-18 13:06:07', '2024-08-18 13:06:07', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (11, 'sa', '192.168.0.108', '/system/api/v2/user/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"mQZUGu\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]', NULL, 'LOGIN', 1, NULL, NULL, '2024-08-18 13:08:39', '2024-08-18 13:08:39', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (12, 'sa', '192.168.0.108', '/system/api/v2/user/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"fFGcTC\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]', NULL, 'LOGIN', 1, NULL, NULL, '2024-08-18 13:10:11', '2024-08-18 13:10:11', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (13, 'sa', '192.168.0.108', '/system/api/v2/user/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"3DJ2Bw\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]', NULL, 'LOGIN', 1, NULL, NULL, '2024-08-18 13:11:05', '2024-08-18 13:11:05', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (14, 'sa', '192.168.0.108', '/system/api/v2/user/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"RnvC93\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]', NULL, 'LOGIN', 1, NULL, NULL, '2024-08-18 13:14:08', '2024-08-18 13:14:08', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (15, 'sa', '192.168.0.108', '/system/api/v2/user/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"MyAnFQ\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]', NULL, 'LOGIN', 1, NULL, NULL, '2024-08-18 13:16:36', '2024-08-18 13:16:36', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (16, 'sa', '192.168.0.108', '/system/api/v2/role/delete', '[]', NULL, '删除', 1, NULL, NULL, '2024-08-18 13:17:13', '2024-08-18 13:17:13', NULL, NULL);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `sys_user_role_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_user_id` int(11) NULL DEFAULT NULL COMMENT '用户ID',
  `sys_role_id` int(11) NULL DEFAULT NULL COMMENT '角色ID',
  PRIMARY KEY (`sys_user_role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户 - 角色关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1, 1);

SET FOREIGN_KEY_CHECKS = 1;
