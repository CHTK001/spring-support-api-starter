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

 Date: 28/08/2024 09:03:32
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
  `sys_dept_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组织编码',
  `sys_dept_principal` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '部门负责人',
  `sys_dept_contact` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '负责人联系方式',
  `sys_dept_sort` int(11) NULL DEFAULT NULL COMMENT '优先级',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_dept_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '组织机构' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dept
-- ----------------------------

-- ----------------------------
-- Table structure for sys_dict
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict`  (
  `sys_dict_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_dict_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典名称',
  `sys_dict_status` int(11) NULL DEFAULT NULL COMMENT '状态; 0:隐藏',
  `sys_dict_in_system` int(11) NULL DEFAULT NULL COMMENT '是否内置; 0:非内置',
  `sys_dict_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典编码, 唯一',
  `sys_dict_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `sys_dict_i18n` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典i18n',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_dict_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字典表' ROW_FORMAT = Dynamic;

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
  `sys_dict_item_i18n` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典i18n',
  `sys_dict_item_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '编码',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_dict_item_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字典项子项' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, 'system', '/system', NULL, '系统管理', NULL, 0, '0,1', NULL, 'ep:setting', NULL, 0, 1, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2024-08-19 11:23:52', 'sa', NULL, NULL);
INSERT INTO `sys_menu` VALUES (2, 'menu', '/system/menu', NULL, '菜单管理', NULL, 1, '0,1,2', 'manage/menu/index', 'ep:menu', NULL, 0, 1, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2024-08-19 11:23:52', 'sa', NULL, NULL);
INSERT INTO `sys_menu` VALUES (3, 'setting', '/system/setting', NULL, '系统配置', NULL, 1, '0,1,3', 'manage/setting/index', 'ep:setting', NULL, 0, 999, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2024-08-19 11:23:52', 'sa', NULL, NULL);
INSERT INTO `sys_menu` VALUES (4, 'role', '/system/role', NULL, '角色管理', NULL, 1, '0,1,4', 'manage/role/index', 'ep:user', NULL, 0, 2, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2024-08-19 11:36:18', 'sa', NULL, NULL);
INSERT INTO `sys_menu` VALUES (5, '', '', '', '新增', 'sys:role:add', 4, '0,1,4', '', '', NULL, 3, 1, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-19 15:58:58', NULL, NULL, NULL, '');
INSERT INTO `sys_menu` VALUES (6, NULL, '', '', '删除', 'sys:role:delete', 4, '0,1,4', '', '', NULL, 3, 1, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-19 16:15:03', NULL, NULL, NULL, '');
INSERT INTO `sys_menu` VALUES (7, '', '', '', '修改', 'sys:role:update', 4, '0,1,4', '', '', NULL, 3, 1, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-19 16:15:41', NULL, NULL, NULL, '');
INSERT INTO `sys_menu` VALUES (8, 'user', '/system/user', '', '用户管理', '', 1, '0,1,8', '/manage/user/index', 'ep:user-filled', NULL, 0, 3, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-20 15:51:07', '2024-08-20 16:29:51', 'sa', NULL, '');
INSERT INTO `sys_menu` VALUES (9, 'userLog', '/manage/log/user', '', '登录日志', '', 1, '0,1,9', '/manage/log/user/index', 'ep:alarm-clock', NULL, 0, 998, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-21 15:46:26', '2024-08-21 16:12:12', 'sa', NULL, '');
INSERT INTO `sys_menu` VALUES (11, 'dept', '/system/dept', '', '机构管理', '', 1, '0,1,11', 'manage/dept/index', 'ep:star', NULL, 0, 5, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-21 16:25:12', '2024-08-21 16:25:29', 'sa', NULL, '');
INSERT INTO `sys_menu` VALUES (12, '', '', '', '删除', 'sys:user:delete', 8, '0,1,8', '', '', NULL, 3, 1, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-23 13:29:45', NULL, NULL, NULL, '');
INSERT INTO `sys_menu` VALUES (13, '', '', '', '编辑', 'sys:user:update', 8, '0,1,8', '', '', NULL, 3, 1, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-23 13:29:56', NULL, NULL, NULL, '');
INSERT INTO `sys_menu` VALUES (14, '', '', '', '新增', 'sys:user:add', 8, '0,1,8,14', '', '', NULL, 3, 1, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-23 13:30:07', '2024-08-23 13:30:39', 'sa', NULL, '');
INSERT INTO `sys_menu` VALUES (15, 'dict', '/manage/dict', '', '字典管理', '', 1, '0,1', '/manage/dict/index', 'humbleicons:dashboard', NULL, 0, 1, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-27 08:41:06', NULL, NULL, NULL, '');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `sys_role_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_role_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色名称',
  `sys_role_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色编码; 唯一',
  `sys_role_in_system` int(11) NULL DEFAULT NULL COMMENT '是否系统角色; 0：非系统角色',
  `sys_role_status` int(11) NULL DEFAULT NULL COMMENT '是否启用; 0：不启用',
  `sys_role_sort` int(11) NULL DEFAULT NULL COMMENT '优先级',
  `sys_role_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'SUPER_ADMIN', 1, 1, 1, '超级管理员', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_role` VALUES (2, '管理员', 'ADMIN', 1, 1, 1, '超级管理员', NULL, NULL, NULL, '2024-08-21 13:49:40', 'sa', NULL);
INSERT INTO `sys_role` VALUES (3, '运维', 'OPS', 1, 1, 1, '运维', NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `sys_role_menu_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_role_id` int(11) NULL DEFAULT NULL COMMENT '角色ID',
  `sys_menu_id` int(11) NULL DEFAULT NULL COMMENT '菜单ID',
  PRIMARY KEY (`sys_role_menu_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 50 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色 - 菜单关系表' ROW_FORMAT = Dynamic;

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
INSERT INTO `sys_role_menu` VALUES (44, 3, 2);
INSERT INTO `sys_role_menu` VALUES (45, 3, 4);
INSERT INTO `sys_role_menu` VALUES (46, 3, 5);
INSERT INTO `sys_role_menu` VALUES (47, 3, 6);
INSERT INTO `sys_role_menu` VALUES (48, 3, 7);
INSERT INTO `sys_role_menu` VALUES (49, 3, 8);

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
  `sys_setting_admin` int(11) NULL DEFAULT NULL COMMENT '是否只有管理员生效',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_setting_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统设置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_setting
-- ----------------------------
INSERT INTO `sys_setting` VALUES (1, 'openVerifyCode', 'false', 1, 'bool', 'default', NULL, 1, 0, '验证码开关', NULL, NULL, NULL, NULL, NULL, '2024-08-16 13:25:38', 'sa', NULL);
INSERT INTO `sys_setting` VALUES (2, 'systemName', '后台管理系统', 1, 'string', 'default', NULL, 1, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_setting` VALUES (3, 'openVcode', 'true', 1, 'bool', 'default', NULL, 1, NULL, '滑动验证码', NULL, NULL, NULL, NULL, NULL, '2024-08-21 09:55:09', 'sa', NULL);
INSERT INTO `sys_setting` VALUES (4, 'openLoopRedirect', 'false', 1, 'bool', 'config', NULL, 1, NULL, '当开启调试模式自动崩溃当前页', NULL, NULL, 'sa', NULL, '2024-08-18 11:22:26', '2024-08-18 11:23:15', 'sa', NULL);
INSERT INTO `sys_setting` VALUES (5, 'openCodec', 'false', 1, 'bool', 'config', NULL, 1, NULL, '是否开启返回加密', NULL, NULL, 'sa', NULL, '2024-08-18 11:31:57', '2024-08-18 11:32:06', 'sa', NULL);
INSERT INTO `sys_setting` VALUES (6, 'openLoopWatermark', 'false', 1, 'bool', 'config', NULL, 1, NULL, '是否开启缓存!', '{\"watermarkColor\" :\"#409EFF\", \"globalAlpha\": 0.15,}', NULL, 'sa', NULL, '2024-08-19 19:53:55', '2024-08-19 19:57:22', 'sa', NULL);
INSERT INTO `sys_setting` VALUES (7, 'openLoopDebugger', 'false', 1, 'bool', 'config', NULL, 1, NULL, '开启循环debugger', NULL, NULL, 'sa', NULL, '2024-08-18 10:52:15', '2024-08-18 10:52:47', 'sa', NULL);
INSERT INTO `sys_setting` VALUES (8, 'version', '1', 1, 'number', 'config', NULL, 1, NULL, '系统版本', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_setting` VALUES (9, 'socket_url', 'http://127.0.0.1:31256', 1, 'array', 'config', NULL, 1, NULL, 'socket服务器地址', NULL, 1, NULL, NULL, NULL, '2024-08-26 15:55:21', 'sa', NULL);
INSERT INTO `sys_setting` VALUES (10, 'openSocket', 'true', 1, 'bool', 'config', NULL, 1, NULL, 'socket是否开启', NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL);

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
  `sys_user_full_address` varchar(400) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '居住地址',
  `sys_user_in_system` int(11) NULL DEFAULT NULL COMMENT '是否系统账号; 0: 非系统账号',
  `sys_user_avatar` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '头像',
  `sys_user_card` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证',
  `sys_dept_id` int(11) NULL DEFAULT NULL COMMENT '部门ID',
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
INSERT INTO `sys_user` VALUES (1, 'sa', '超级管理员', '4852bee906775d75337bfca410ba8e7d', '123456', 0, 1, '12345678901', 'test@qq.com', NULL, NULL, 1, 'data:image/jpeg;base64,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', NULL, NULL, NULL, NULL, '192.168.30.38', NULL, NULL, NULL, NULL, '2024-08-26 14:14:15', 'sa', NULL);
INSERT INTO `sys_user` VALUES (2, 'test', '测试', '2cc08c3aa427d1753bd23df1e4136527', 'fqweet9i62w9se', 0, 1, '', NULL, 2, NULL, 0, NULL, NULL, 1, '', NULL, '172.16.2.226', '172.16.2.226', 'sa', NULL, '2024-08-20 17:06:18', '2024-08-21 08:41:20', 'sa', NULL);

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
  `sys_log_system` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '系统',
  `sys_log_browser` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '浏览器',
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
  PRIMARY KEY (`sys_log_id`) USING BTREE,
  INDEX `index_sys_log_from`(`sys_log_from` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统日志表' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户 - 角色关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1, 1);

SET FOREIGN_KEY_CHECKS = 1;
