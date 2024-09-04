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

 Date: 04/09/2024 10:41:22
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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '组织机构' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict
-- ----------------------------
INSERT INTO `sys_dict` VALUES (1, '三方厂家', 1, 0, 'SFCJ', '三方厂家', '', 'sa', NULL, '2024-08-31 20:29:00', '2024-08-31 20:35:57', 'sa', NULL);
INSERT INTO `sys_dict` VALUES (2, '模板类型', 1, 0, 'MBLX', '', '', 'sa', NULL, '2024-09-01 18:16:46', NULL, NULL, NULL);

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
  `sys_dict_item_delete` int(11) NULL DEFAULT NULL COMMENT '是否删除0:未删除',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_dict_item_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字典项子项' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict_item
-- ----------------------------
INSERT INTO `sys_dict_item` VALUES (1, 1, '海康云耀', '海康云耀', 1, 1, '', 'HKYY', 0, 'sa', NULL, '2024-09-01 10:02:02', '2024-09-01 10:08:45', 'sa', NULL);
INSERT INTO `sys_dict_item` VALUES (2, 1, '阿里云', '阿里云', 1, 1, NULL, 'ALIYUN', 0, 'sa', NULL, '2024-09-01 10:10:03', NULL, NULL, NULL);
INSERT INTO `sys_dict_item` VALUES (3, 1, '百度云', '百度云', 1, 1, NULL, 'BDYUN', 0, 'sa', NULL, '2024-09-01 10:10:13', NULL, NULL, NULL);
INSERT INTO `sys_dict_item` VALUES (4, 2, '邮件', '', 1, 1, '', 'EMAIL', 0, 'sa', 1, '2024-09-03 13:35:36', NULL, NULL, NULL);
INSERT INTO `sys_dict_item` VALUES (5, 2, '短信', '', 1, 1, '', 'SMS', 0, 'sa', 1, '2024-09-03 13:48:51', NULL, NULL, NULL);
INSERT INTO `sys_dict_item` VALUES (6, 2, '站内', '', 1, 1, '', 'WEB', 0, 'sa', 1, '2024-09-03 13:54:31', '2024-09-03 13:55:18', 'sa', 1);
INSERT INTO `sys_dict_item` VALUES (7, 2, '微信', NULL, 1, NULL, NULL, 'WX', 0, 'sa', 1, '2024-09-03 13:55:13', NULL, NULL, NULL);

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
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, 'system', '/system', NULL, '系统管理', NULL, 0, '0,1', NULL, 'ep:setting', NULL, 0, 1, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2024-08-19 11:23:52', 'sa', NULL, NULL);
INSERT INTO `sys_menu` VALUES (2, 'menu', '/system/menu', NULL, '菜单管理', NULL, 1, '0,1,2', 'manage/menu/index', 'ep:menu', NULL, 0, 1, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2024-08-19 11:23:52', 'sa', NULL, NULL);
INSERT INTO `sys_menu` VALUES (3, 'setting', '/system/setting', NULL, '系统配置', NULL, 1, '0,1,3', 'manage/setting/index', 'ep:setting', NULL, 0, 999, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2024-08-19 11:23:52', 'sa', NULL, 'SUPER_ADMIN,ADMIN');
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
INSERT INTO `sys_menu` VALUES (15, 'dict', '/manage/dict', '', '字典管理', '', 1, '0,1,15', '/manage/dict/index', 'humbleicons:dashboard', NULL, 0, 996, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-27 08:41:06', '2024-09-02 16:00:14', 'sa', NULL, '');
INSERT INTO `sys_menu` VALUES (16, 'secret', '/manage/secret', '', '密钥管理', '', 1, '0,1,16', '/manage/secret/index', 'ep:lock', NULL, 0, 997, 1, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-30 11:26:23', '2024-08-30 11:26:57', 'sa', NULL, '');
INSERT INTO `sys_menu` VALUES (17, 'demo', '/demo/index', '', '自定义弹框组件', '', 18, '0,18,17', '/demo/index', 'ep:alarm-clock', NULL, 0, 1, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-09-02 09:54:43', '2024-09-02 15:09:18', 'sa', NULL, '');
INSERT INTO `sys_menu` VALUES (18, 'demo', '/demo', '', '测试demo', '', 0, '0,18', '', 'ep:add-location', NULL, 0, 1, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-09-02 15:08:38', '2024-09-02 15:08:45', 'sa', NULL, '');
INSERT INTO `sys_menu` VALUES (20, 'template', '/manage/template', '', '模板管理', '', 1, '0,1,20', '/manage/template/index', 'ep:cellphone', NULL, 0, 997, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-09-02 15:59:48', '2024-09-02 16:00:05', 'sa', NULL, '');

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
-- Table structure for sys_secret
-- ----------------------------
DROP TABLE IF EXISTS `sys_secret`;
CREATE TABLE `sys_secret`  (
  `sys_secret_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_secret_group` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组',
  `sys_secret_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '编码, 可自行作为唯一标识',
  `sys_secret_app_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'appid',
  `sys_secret_app_secret` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'appsecret',
  `sys_secret_delete` int(11) NULL DEFAULT NULL COMMENT '是否删除0:未删除',
  `sys_secret_status` int(11) NULL DEFAULT NULL COMMENT '是否启用0:未启用',
  `sys_secret_cdn` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'cdn',
  `sys_secret_endpoint` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '端点',
  `sys_secret_api_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'api地址',
  `sys_secret_dict_item_id` int(11) NULL DEFAULT NULL COMMENT '字典项ID(字典1)',
  `sys_secret_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_secret_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '密钥' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_secret
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
  `sys_setting_config` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置参数',
  `sys_setting_admin` int(11) NULL DEFAULT NULL COMMENT '是否只有管理员生效',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_setting_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统全局配置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_setting
-- ----------------------------
INSERT INTO `sys_setting` VALUES (1, 'CheckCodeOpen', 'false', 1, 'Boolean', 'default', NULL, 1, 0, '验证码开关', NULL, NULL, NULL, NULL, NULL, '2024-09-04 09:47:01', 'sa', 1);
INSERT INTO `sys_setting` VALUES (2, 'SystemName', '后台管理系统', 1, 'String', 'default', NULL, 1, 0, '系统标题', NULL, NULL, NULL, NULL, NULL, '2024-09-04 09:47:01', 'sa', 1);
INSERT INTO `sys_setting` VALUES (3, 'SlidingBlockOpen', 'true', 1, 'Boolean', 'default', NULL, 1, 0, '滑动验证码', NULL, NULL, NULL, NULL, NULL, '2024-09-04 09:47:01', 'sa', 1);
INSERT INTO `sys_setting` VALUES (4, 'CrashPageOpen', 'false', 1, 'Boolean', 'config', NULL, 1, 0, '开启调试模式崩溃当前页', NULL, NULL, 'sa', NULL, '2024-08-18 11:22:26', '2024-09-04 09:41:02', 'sa', 1);
INSERT INTO `sys_setting` VALUES (5, 'CodecResponseOpen', 'false', 1, 'Boolean', 'config', NULL, 1, 0, '返回加密', NULL, NULL, 'sa', NULL, '2024-08-18 11:31:57', '2024-09-04 09:41:02', 'sa', 1);
INSERT INTO `sys_setting` VALUES (6, 'WatermarkOpen', 'false', 1, 'Boolean', 'config', NULL, 1, 0, '开启水印', NULL, NULL, 'sa', NULL, '2024-08-19 19:53:55', '2024-09-04 09:41:02', 'sa', 1);
INSERT INTO `sys_setting` VALUES (7, 'LoopDebuggerOpen', 'false', 1, 'Boolean', 'config', NULL, 1, 0, '开启循环debugger', NULL, NULL, 'sa', NULL, '2024-08-18 10:52:15', '2024-09-04 09:41:02', 'sa', 1);
INSERT INTO `sys_setting` VALUES (8, 'Version', '1', 1, 'Number', 'config', NULL, 1, 0, '系统版本', NULL, NULL, NULL, NULL, NULL, '2024-09-04 09:41:02', 'sa', 1);
INSERT INTO `sys_setting` VALUES (9, 'SocketUrl', 'http://127.0.0.1:31256', 1, 'Array', 'config', NULL, 1, 0, 'socket服务器地址', NULL, 1, NULL, NULL, NULL, '2024-09-04 09:41:02', 'sa', 1);
INSERT INTO `sys_setting` VALUES (10, 'SocketOpen', 'false', 1, 'Boolean', 'config', NULL, 1, 0, 'socket是否开启', NULL, 1, NULL, NULL, NULL, '2024-09-04 09:41:02', 'sa', 1);
INSERT INTO `sys_setting` VALUES (12, 'WatermarkColor', '#409EFF', 1, 'Color', 'config', NULL, 1, 0, '水印颜色', NULL, NULL, NULL, NULL, NULL, '2024-09-04 09:41:02', 'sa', 1);
INSERT INTO `sys_setting` VALUES (13, 'WatermarkAlpha', '0.15', 1, 'Number', 'config', NULL, 1, 0, '水印透明度', NULL, NULL, NULL, NULL, NULL, '2024-09-04 09:41:02', 'sa', 1);
INSERT INTO `sys_setting` VALUES (14, 'WatermarkRotate', '-30', 1, 'Number', 'config', NULL, 1, 0, '水印角度', NULL, NULL, NULL, NULL, NULL, '2024-09-04 09:41:02', 'sa', 1);
INSERT INTO `sys_setting` VALUES (15, 'CodecRequestOpen', 'true', 1, 'Boolean', 'config', NULL, 1, 0, '请求加密', NULL, NULL, NULL, NULL, NULL, '2024-09-04 09:41:02', 'sa', 1);
INSERT INTO `sys_setting` VALUES (16, 'CodecRequestKey', '0123456789abcdef', 1, 'String', 'config', NULL, 1, 1, '请求加密密钥', NULL, NULL, NULL, NULL, NULL, '2024-09-04 09:41:02', 'sa', 1);

-- ----------------------------
-- Table structure for sys_template
-- ----------------------------
DROP TABLE IF EXISTS `sys_template`;
CREATE TABLE `sys_template`  (
  `sys_template_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_template_manufacturer_id` int(11) NULL DEFAULT NULL COMMENT '厂家类型, 字典(dictId = 1)子项',
  `sys_template_category` int(11) NULL DEFAULT NULL COMMENT '模板类型, 字典(dictId = 2)子项',
  `sys_template_sort` int(11) NULL DEFAULT NULL COMMENT '模板优先级',
  `sys_template_group_id` int(11) NULL DEFAULT NULL COMMENT '分组ID',
  `sys_template_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `sys_template_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '唯一编码',
  `sys_template_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '模板内容',
  `sys_template_delete` int(11) NULL DEFAULT NULL COMMENT '是否删除0:未删除',
  `sys_template_status` int(11) NULL DEFAULT NULL COMMENT '是否启用:0:未启用',
  `sys_template_type` int(11) NULL DEFAULT NULL COMMENT '模板类型;0:系统;1:用户;2:其它',
  `sys_template_default` int(11) NULL DEFAULT NULL COMMENT '是否默认:0:未启用',
  `sys_template_approve` int(11) NULL DEFAULT NULL COMMENT '审批状态;0:未审批; 1: 审批中; 2:已通过; 3: 未通过',
  `sys_template_approve_reject` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '拒绝原因',
  `sys_template_approve_create_by` int(11) NULL DEFAULT NULL COMMENT '审批人',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_template_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '模板管理' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_template
-- ----------------------------
INSERT INTO `sys_template` VALUES (1, 2, 5, 1, 1, '验证码', 'SMS_CODE_2_0000', NULL, 0, 1, 0, NULL, 2, NULL, NULL, 'sa', 1, '2024-09-02 19:53:49', '2024-09-02 20:04:01', 'sa', 1);

-- ----------------------------
-- Table structure for sys_template_group
-- ----------------------------
DROP TABLE IF EXISTS `sys_template_group`;
CREATE TABLE `sys_template_group`  (
  `sys_template_group_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_template_group_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板名称',
  `sys_template_group_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板唯一编码',
  `sys_template_group_delete` int(11) NULL DEFAULT NULL COMMENT '是否删除;0:未删除',
  `sys_template_group_sort` int(11) NULL DEFAULT NULL COMMENT '模板优先级',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_template_group_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '模板分组' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_template_group
-- ----------------------------
INSERT INTO `sys_template_group` VALUES (1, '验证码', 'YZM', 0, 1, 'sa', NULL, '2024-09-02 16:18:15', '2024-09-03 09:54:41', 'sa', 1);
INSERT INTO `sys_template_group` VALUES (2, '邮件', 'EMAIL', 0, 1, 'sa', NULL, '2024-09-02 16:32:18', '2024-09-02 16:34:08', 'sa', NULL);
INSERT INTO `sys_template_group` VALUES (3, '站内消息', 'ZNXX', 1, 1, 'sa', 1, '2024-09-02 16:34:21', NULL, NULL, NULL);

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
INSERT INTO `sys_user` VALUES (1, 'sa', '超级管理员', '4852bee906775d75337bfca410ba8e7d', '123456', 0, 1, '12345678901', 'test@qq.com', NULL, NULL, 1, 'data:image/jpeg;base64,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', NULL, NULL, NULL, NULL, '172.16.3.66', NULL, NULL, NULL, NULL, '2024-08-28 10:09:26', 'sa', NULL);
INSERT INTO `sys_user` VALUES (2, 'test', '测试', '2cc08c3aa427d1753bd23df1e4136527', 'fqweet9i62w9se', 0, 1, '', NULL, 2, NULL, 0, NULL, NULL, 1, '', NULL, '172.16.2.226', '172.16.2.226', 'sa', NULL, '2024-08-20 17:06:18', '2024-08-21 08:41:20', 'sa', NULL);

-- ----------------------------
-- Table structure for sys_user_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_config`;
CREATE TABLE `sys_user_config`  (
  `sys_user_config_id` int(11) NOT NULL,
  `sys_user_config_wx_openid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信openid',
  `sys_user_config_wx_unionid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信unionid',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_user_config_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户信息 - 用户设置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_config
-- ----------------------------

-- ----------------------------
-- Table structure for sys_user_layout
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_layout`;
CREATE TABLE `sys_user_layout`  (
  `sys_layout_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_layout_layout` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '布局',
  `sys_layout_grid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '布局网格',
  `sys_layout_component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件',
  `sys_layout_user_id` int(11) NULL DEFAULT NULL COMMENT '所属用户',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_layout_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户布局' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_layout
-- ----------------------------
INSERT INTO `sys_user_layout` VALUES (1, '[18,6]', '[]', '[[\"/src/views/home/components/weather.vue\"],[\"/src/views/home/components/time.vue\"]]', 1, 'sa', NULL, '2024-08-29 17:20:12', '2024-08-29 17:20:50', 'sa', NULL);

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
  `sys_log_ua` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'ua',
  `sys_log_browser` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '浏览器',
  `sys_log_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求地址',
  `sys_log_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求参数',
  `sys_log_cost` int(11) NULL DEFAULT NULL COMMENT '耗时',
  `sys_log_from` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求模块',
  `sys_log_status` int(11) NULL DEFAULT NULL COMMENT '请求状态; 0: 失败; 1: 成功',
  `sys_log_login_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '登录方式',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_log_id`) USING BTREE,
  INDEX `index_sys_log_from`(`sys_log_from` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_log
-- ----------------------------
INSERT INTO `sys_user_log` VALUES (1, 'sa', NULL, '172.16.3.66', NULL, 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36', 'CHROME12', '/system/api/v2/user/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]', 591, 'LOGIN', 1, 'WEB', NULL, 1, '2024-09-03 19:08:36', '2024-09-03 19:08:36', NULL, NULL);

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
