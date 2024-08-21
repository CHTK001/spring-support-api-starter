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

 Date: 21/08/2024 10:24:28
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
  `sys_dict_item_sort` int(11) NULL DEFAULT NULL COMMENT '优先级',
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
  `sys_menu_extra_icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '右侧菜单图标',
  `sys_menu_type` int(11) NULL DEFAULT NULL COMMENT '菜单 0 按钮 1',
  `sys_menu_sort` int(11) NULL DEFAULT NULL COMMENT '优先级',
  `sys_menu_hidden` int(11) NULL DEFAULT NULL COMMENT '是否隐藏；0:显示',
  `sys_menu_show_parent` int(11) NULL DEFAULT NULL COMMENT '当一个子级菜单是否隐藏父级菜单；0:显示',
  `sys_menu_hidden_tag` int(11) NULL DEFAULT NULL COMMENT '是否在标签栏隐藏；0:否',
  `sys_menu_fixed_tag` int(11) NULL DEFAULT NULL COMMENT '是否固定标签；0:否',
  `sys_menu_redirect` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '路由重定向',
  `sys_menu_enter_transition` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '进场动画',
  `sys_menu_leave_transition` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '离场动画',
  `sys_menu_active_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单激活',
  `sys_menu_frame_src` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链接地址',
  `sys_menu_keep_alive` int(11) NULL DEFAULT NULL COMMENT '是否缓存；0:否',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  `sys_menu_role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属权限',
  PRIMARY KEY (`sys_menu_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, 'system', '/system', NULL, '系统管理', NULL, 0, '0,1', NULL, 'ep:setting', NULL, 0, 1, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2024-08-19 11:23:52', 'sa', NULL, NULL);
INSERT INTO `sys_menu` VALUES (2, 'menu', '/system/menu', NULL, '菜单管理', NULL, 1, '0,1,2', 'manage/menu/index', 'ep:menu', NULL, 0, 1, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2024-08-19 11:23:52', 'sa', NULL, NULL);
INSERT INTO `sys_menu` VALUES (3, 'setting', '/system/setting', NULL, '系统配置', NULL, 1, '0,1,3', 'manage/setting/index', 'ep:setting', NULL, 0, 2, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2024-08-19 11:23:52', 'sa', NULL, NULL);
INSERT INTO `sys_menu` VALUES (4, 'role', '/system/role', NULL, '角色管理', NULL, 1, '0,1,4', 'manage/role/index', 'ep:user', NULL, 0, 1, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2024-08-19 11:36:18', 'sa', NULL, NULL);
INSERT INTO `sys_menu` VALUES (5, '', '', '', '新增', 'sys:role:add', 4, '0,1,4', '', '', NULL, 3, 1, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-19 15:58:58', NULL, NULL, NULL, '');
INSERT INTO `sys_menu` VALUES (6, NULL, '', '', '删除', 'sys:role:delete', 4, '0,1,4', '', '', NULL, 3, 1, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-19 16:15:03', NULL, NULL, NULL, '');
INSERT INTO `sys_menu` VALUES (7, '', '', '', '修改', 'sys:role:update', 4, '0,1,4', '', '', NULL, 3, 1, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-19 16:15:41', NULL, NULL, NULL, '');
INSERT INTO `sys_menu` VALUES (8, '/system/user', '/system/user', '', '用户管理', '', 1, '0,1,8', '/manage/user/index', 'ep:user-filled', NULL, 0, 1, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-20 15:51:07', '2024-08-20 16:29:51', 'sa', NULL, '');

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
) ENGINE = InnoDB AUTO_INCREMENT = 44 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色 - 菜单关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES (36, 2, 1);
INSERT INTO `sys_role_menu` VALUES (37, 2, 2);
INSERT INTO `sys_role_menu` VALUES (38, 2, 4);
INSERT INTO `sys_role_menu` VALUES (39, 2, 5);
INSERT INTO `sys_role_menu` VALUES (40, 2, 6);
INSERT INTO `sys_role_menu` VALUES (41, 2, 7);
INSERT INTO `sys_role_menu` VALUES (42, 2, 8);
INSERT INTO `sys_role_menu` VALUES (43, 2, 3);

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
  `sys_setting_config` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置参数',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_setting_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统设置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_setting
-- ----------------------------
INSERT INTO `sys_setting` VALUES (1, 'openVerifyCode', 'false', 1, 'bool', 'default', NULL, 1, 0, '验证码开关', NULL, NULL, NULL, NULL, '2024-08-16 13:25:38', 'sa', NULL);
INSERT INTO `sys_setting` VALUES (2, 'systemName', '后台管理系统', 1, 'string', 'default', NULL, 1, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_setting` VALUES (3, 'openLoopDebugger', 'false', 1, 'bool', 'codec', NULL, 1, NULL, '开启循环debugger', NULL, 'sa', NULL, '2024-08-18 10:52:15', '2024-08-18 10:52:47', 'sa', NULL);
INSERT INTO `sys_setting` VALUES (4, 'openLoopRedirect', 'false', 1, 'bool', 'codec', NULL, 1, NULL, '当开启调试模式自动崩溃当前页', NULL, 'sa', NULL, '2024-08-18 11:22:26', '2024-08-18 11:23:15', 'sa', NULL);
INSERT INTO `sys_setting` VALUES (5, 'enable', 'false', 1, 'bool', 'codec', NULL, 1, NULL, '是否开启返回加密', NULL, 'sa', NULL, '2024-08-18 11:31:57', '2024-08-18 11:32:06', 'sa', NULL);
INSERT INTO `sys_setting` VALUES (6, 'openLoopWatermark', 'false', 1, 'bool', 'codec', NULL, 1, NULL, '是否开启缓存!', '{\"watermarkColor\" :\"#409EFF\", \"globalAlpha\": 0.15,}', 'sa', NULL, '2024-08-19 19:53:55', '2024-08-19 19:57:22', 'sa', NULL);
INSERT INTO `sys_setting` VALUES (7, 'openVcode', 'true', 1, 'bool', 'default', NULL, 1, NULL, '滑动验证码', NULL, NULL, NULL, NULL, '2024-08-21 09:55:09', 'sa', NULL);

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
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'sa', '超级管理员', '4852bee906775d75337bfca410ba8e7d', '123456', 0, 1, NULL, NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_user` VALUES (2, 'test', '测试', 'df4e826a775613408212096887eb3bb7', '8u7p0vil02t7f1', 0, 0, '', NULL, 2, 0, NULL, '', NULL, '172.16.2.226', '172.16.2.226', 'sa', NULL, '2024-08-20 17:06:18', '2024-08-21 08:41:20', 'sa', NULL);

-- ----------------------------
-- Table structure for sys_user_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_log`;
CREATE TABLE `sys_user_log`  (
  `sys_log_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_log_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账号',
  `sys_log_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求地址',
  `sys_log_ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求IP',
  `sys_log_isp` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '运营商',
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
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_log
-- ----------------------------

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
