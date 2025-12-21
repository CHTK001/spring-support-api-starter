/*
 Navicat Premium Dump SQL

 Source Server         : 172.16.0.40
 Source Server Type    : MySQL
 Source Server Version : 80016 (8.0.16)
 Source Host           : 172.16.0.40:3306
 Source Schema         : system

 Target Server Type    : MySQL
 Target Server Version : 80016 (8.0.16)
 File Encoding         : 65001

 Date: 27/03/2025 14:40:54
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
  `sys_dept_status` int(11) NULL DEFAULT NULL COMMENT '状态, 是否禁用, 0:启用',
  `sys_dept_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `sys_dept_data_permission` int(11) NULL DEFAULT NULL COMMENT '数据权限类型,1:全部可见,2:本人可见,3:所在部门可见,4:所在部门及子级可见,5:选择的部门可见',
  `sys_dept_data_permission_dept_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据权限可见的部门ID列表',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_dept_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '组织机构' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dept
-- ----------------------------
INSERT INTO `sys_dept` VALUES (1, '一级机构', 0, '0,1', 'ROOT', NULL, NULL, 1, 0, NULL, 5, '4,2,5', 'sa', 1, '2025-02-12 17:23:37', '2025-02-12 17:26:09', 'sa', 1);
INSERT INTO `sys_dept` VALUES (2, '二级机构', 1, '0,1,2', 'l2', NULL, NULL, 1, 0, NULL, NULL, NULL, 'sa', 1, '2025-02-12 17:30:03', '2025-02-13 10:29:52', 'sa', 1);
INSERT INTO `sys_dept` VALUES (4, '二级机构 - 2', 1, '0,1,4', 'l22', NULL, NULL, 0, 0, NULL, NULL, NULL, 'sa', 1, '2025-02-13 14:36:26', '2025-02-13 14:36:26', 'sa', 1);
INSERT INTO `sys_dept` VALUES (5, '三级机构', 2, '0,1,2,5', 'l3', NULL, NULL, 0, 0, NULL, NULL, NULL, 'sa', 1, '2025-02-13 14:36:39', '2025-02-13 14:36:39', 'sa', 1);

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
  `sys_log_status` int(11) NULL DEFAULT NULL COMMENT '请求状态, 0: 失败, 1: 成功',
  `sys_log_cost` int(11) NULL DEFAULT NULL COMMENT '耗时',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_log_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统日志表' ROW_FORMAT = DYNAMIC;

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
  `sys_menu_type` int(11) NULL DEFAULT NULL COMMENT '菜单:0 iframe:1 外链:2 按钮:3',
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
  `sys_menu_home` int(11) NULL DEFAULT NULL COMMENT '是否只显示在主页； 0: 否',
  `sys_menu_engine` int(11) NULL DEFAULT NULL COMMENT '是否engine布局主页；0: 否',
  PRIMARY KEY (`sys_menu_id` DESC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 42 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜单' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (41, 'aiColorization', '/ai/colorization', NULL, 'AI 图像上色', NULL, 37, '0,37', '/ai/colorization/index', 'mingcute:color-picker-fill', NULL, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-03-21 10:29:02', NULL, NULL, NULL, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (40, 'aiResolution', '/ai/resolution', NULL, 'AI 分辨率', NULL, 37, '0,37,40', '/ai/resolution/index', 'simple-icons:davinciresolve', NULL, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-03-18 16:59:22', '2025-03-20 13:08:36', 'sa', 1, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (39, 'aiVideo', '/ai/video', NULL, 'AI 文生视频', NULL, 37, '0,37,39', '/ai/video/index', 'ri:folder-video-line', NULL, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-28 16:24:52', '2025-03-21 10:42:06', 'sa', 1, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (38, 'aiVincent', '/ai/vincent', NULL, 'AI 文生图', NULL, 37, '0,37,38', '/ai/vincent/index', 'ri:picture-in-picture-exit-line', NULL, 0, 2, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-26 10:46:55', '2025-02-26 10:47:06', 'sa', 1, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (37, 'ai', '/ai', NULL, 'AI 管理', NULL, 0, '0,37', '/ai', 'ri:robot-2-line', NULL, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-26 10:23:48', '2025-02-26 10:24:22', 'sa', 1, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (36, 'aiLlm', '/ai/llm', NULL, 'AI 对话', NULL, 37, '0,37,36', '/ai/llm/index', 'ri:robot-2-line', NULL, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-25 17:19:27', '2025-02-25 17:20:27', 'sa', 1, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (35, 'serviceTenant', '/service/tenant', NULL, '租户管理', NULL, 32, '0,32', '/service/tenant/index', 'bi:people', NULL, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-19 15:44:30', NULL, NULL, NULL, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (34, 'serviceService', '/service/service', NULL, '服务管理', NULL, 32, '0,32', '/service/service/index', 'ri:customer-service-line', NULL, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-18 10:44:13', NULL, NULL, NULL, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (33, 'serviceModule', '/service/module', NULL, '模块管理', NULL, 32, '0,32,33', '/service/module/index', 'ri:sound-module-line', NULL, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-18 10:43:26', '2025-02-18 10:44:30', 'sa', 1, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (32, 'service', '/service', NULL, '服务管理', NULL, 0, '0,32', '/service', 'mingcute:service-fill', NULL, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-18 10:41:32', '2025-02-18 10:41:54', 'sa', 1, 'ADMIN,SUPER_ADMIN', NULL, NULL);
INSERT INTO `sys_menu` VALUES (31, NULL, NULL, NULL, '查询', 'sys:login:page', 8, '0,1,8', NULL, NULL, NULL, 3, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-02-13 15:48:07', NULL, NULL, NULL, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (30, 'device', '/manage/device', '', '设备管理', '', 1, '0,1,30', '/manage/device/index', 'pixelarticons:device-tv-smart', NULL, 0, 995, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', 1, '2025-02-10 15:39:45', '2025-02-13 10:36:09', 'sa', 1, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (29, 'llm-template', '/ai/llm', '', '模板管理-大语言', '', 37, '0,37,29', '/ai/llm/index', '', NULL, 0, 997, 1, 1, 0, 0, '', '', '', '', '', 0, 'sa', 1, '2025-02-07 15:14:04', '2025-02-13 10:33:47', 'sa', 1, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (28, 'device-template', '/manage/device/camera', '', '模板管理-设备', '', 1, '0,1,28', '/manage/template/device/index', '', NULL, 0, 997, 1, 1, 0, 0, '', '', '', '', '', 0, 'sa', 1, '2025-02-05 10:52:22', '2025-02-05 10:53:31', 'sa', 1, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (27, 'email-template', '/manage/email/template', '', '模板管理-邮件', '', 1, '0,1,27', '/manage/template/email/index', '', NULL, 0, 997, 1, 1, 0, 0, '', '', '', '', '', 0, 'sa', 1, '2025-01-27 19:17:19', '2025-01-27 19:17:44', 'sa', 1, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (26, 'project', '/manage/project', '', '项目管理', NULL, 1, '0,1,26', '/manage/project/index', 'simple-icons:saltproject', NULL, 0, 997, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2025-01-24 14:33:09', '2025-01-24 14:33:44', 'sa', 1, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (24, 'sfc', '/manage/sfc', '', '组件管理', NULL, 1, '0,1,24', '/manage/sfc/index', 'ri:compasses-2-fill', NULL, 0, 996, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2024-10-24 16:34:53', '2024-10-24 16:35:51', 'sa', 1, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (20, 'sms-template', '/manage/sms/template', '', '模板管理-短信', '', 1, '0,1,20', '/manage/template/sms/index', '', NULL, 0, 997, 1, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-09-02 15:59:48', '2024-09-02 16:00:05', 'sa', 1, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (17, 'demo', '/demo/index', '', '自定义弹框组件', '', 18, '0,18,17', '/demo/index', 'ep:alarm-clock', NULL, 0, 1, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-09-02 09:54:43', '2024-09-02 15:09:18', 'sa', NULL, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (16, 'secret', '/manage/secret', '', '密钥管理', '', 1, '0,1,16', '/manage/secret/index', 'ep:lock', NULL, 0, 997, 1, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-30 11:26:23', '2024-08-30 11:26:57', 'sa', NULL, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (15, 'dict', '/manage/dict', '', '字典管理', '', 1, '0,1,15', '/manage/dict/index', 'humbleicons:dashboard', NULL, 0, 6, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-27 08:41:06', '2024-09-02 16:00:14', 'sa', 1, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (14, '', '', '', '新增', 'sys:login:add', 8, '0,1,8,14', '', '', NULL, 3, 1, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-23 13:30:07', '2024-08-23 13:30:39', 'sa', NULL, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (13, '', '', '', '编辑', 'sys:login:update', 8, '0,1,8', '', '', NULL, 3, 1, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-23 13:29:56', NULL, NULL, NULL, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (12, '', '', '', '删除', 'sys:login:delete', 8, '0,1,8', '', '', NULL, 3, 1, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-23 13:29:45', NULL, NULL, NULL, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (11, 'dept', '/manage/dept', '', '机构管理', '', 1, '0,1,11', '/manage/dept/index', 'ep:star', NULL, 0, 5, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-21 16:25:12', '2024-08-21 16:25:29', 'sa', 1, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (9, 'userLog', '/manage/log/login', '', '登录日志', '', 1, '0,1,9', '/manage/log/login/index', 'ep:alarm-clock', NULL, 0, 998, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-21 15:46:26', '2024-08-21 16:12:12', 'sa', NULL, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (8, 'login', '/manage/login', '', '用户管理', '', 1, '0,1,8', '/manage/login/index', 'ep:login-filled', NULL, 0, 3, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-20 15:51:07', '2024-08-20 16:29:51', 'sa', 1, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (7, '', '', '', '修改', 'sys:role:update', 4, '0,1,4', '', '', NULL, 3, 1, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-19 16:15:41', NULL, NULL, NULL, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (6, NULL, '', '', '删除', 'sys:role:delete', 4, '0,1,4', '', '', NULL, 3, 1, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-19 16:15:03', NULL, NULL, NULL, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (5, '', '', '', '新增', 'sys:role:add', 4, '0,1,4', '', '', NULL, 3, 1, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-19 15:58:58', NULL, NULL, NULL, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (4, 'role', '/manage/role', NULL, '角色管理', NULL, 1, '0,1,4', '/manage/role/index', 'ep:login', NULL, 0, 2, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2024-08-19 11:36:18', 'sa', 1, '', NULL, NULL);
INSERT INTO `sys_menu` VALUES (3, 'setting', '/manage/setting', '', '系统配置', '', 1, '0,1,3', '@repo/pages/setting/index.vue', 'ep:setting', NULL, 0, 999, 0, 1, 0, 0, '', '', '', '', '', 0, NULL, NULL, NULL, '2024-08-19 11:23:52', 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL);
INSERT INTO `sys_menu` VALUES (2, 'menu', '/manage/menu', NULL, '菜单管理', NULL, 1, '0,1,2', '/manage/menu/index', 'ep:menu', NULL, 0, 1, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2024-08-19 11:23:52', 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1, 'system', '/manage', NULL, '系统管理', NULL, 0, '0,1', '/manage', 'ep:setting', NULL, 0, 2, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2024-08-19 11:23:52', 'sa', 1, '', NULL, NULL);

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `sys_role_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_role_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色名称',
  `sys_role_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色编码, 唯一',
  `sys_role_in_system` int(11) NULL DEFAULT NULL COMMENT '是否系统角色, 0：非系统角色',
  `sys_role_status` int(11) NULL DEFAULT NULL COMMENT '是否启用, 0：不启用',
  `sys_role_sort` int(11) NULL DEFAULT NULL COMMENT '优先级',
  `sys_role_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `sys_role_hidden` int(11) NULL DEFAULT 0 COMMENT '是否显示, 0:显示',
  `sys_role_readable` int(11) NULL DEFAULT NULL COMMENT '角色可读',
  `sys_role_writeable` int(11) NULL DEFAULT NULL COMMENT '角色可写',
  `sys_role_executable` int(11) NULL DEFAULT NULL COMMENT '角色可执行',
  `sys_role_board_card` int(11) NULL DEFAULT NULL COMMENT '角色可看板类型',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'SUPER_ADMIN', 1, 1, 1, '超级管理员', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_role` VALUES (2, '管理员', 'ADMIN', 1, 1, 1, '超级管理员', 0, NULL, NULL, NULL, 1, NULL, NULL, NULL, '2024-08-21 13:49:40', 'sa', 1);
INSERT INTO `sys_role` VALUES (3, '运维', 'OPS', 1, 1, 1, '运维', 0, NULL, NULL, NULL, 1, NULL, NULL, NULL, '2025-02-13 11:31:33', 'sa', 1);
INSERT INTO `sys_role` VALUES (4, '机构', 'DEPT', 1, 1, 1, '机构账号', 0, 1, 16, 256, NULL, 'sa', 1, '2025-02-13 15:19:04', '2025-02-13 15:19:23', 'sa', 1);

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `sys_role_menu_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_role_id` int(11) NULL DEFAULT NULL COMMENT '角色ID',
  `sys_menu_id` int(11) NULL DEFAULT NULL COMMENT '菜单ID',
  PRIMARY KEY (`sys_role_menu_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 189 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色 - 菜单关系表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES (82, 2, 4);
INSERT INTO `sys_role_menu` VALUES (83, 2, 5);
INSERT INTO `sys_role_menu` VALUES (84, 2, 6);
INSERT INTO `sys_role_menu` VALUES (85, 2, 7);
INSERT INTO `sys_role_menu` VALUES (86, 2, 8);
INSERT INTO `sys_role_menu` VALUES (87, 2, 12);
INSERT INTO `sys_role_menu` VALUES (88, 2, 13);
INSERT INTO `sys_role_menu` VALUES (89, 2, 14);
INSERT INTO `sys_role_menu` VALUES (90, 2, 1);
INSERT INTO `sys_role_menu` VALUES (91, 2, 2);
INSERT INTO `sys_role_menu` VALUES (92, 2, 11);
INSERT INTO `sys_role_menu` VALUES (93, 2, 15);
INSERT INTO `sys_role_menu` VALUES (94, 2, 16);
INSERT INTO `sys_role_menu` VALUES (95, 2, 20);
INSERT INTO `sys_role_menu` VALUES (96, 2, 9);
INSERT INTO `sys_role_menu` VALUES (97, 2, 3);
INSERT INTO `sys_role_menu` VALUES (131, 3, 2);
INSERT INTO `sys_role_menu` VALUES (132, 3, 4);
INSERT INTO `sys_role_menu` VALUES (133, 3, 5);
INSERT INTO `sys_role_menu` VALUES (134, 3, 6);
INSERT INTO `sys_role_menu` VALUES (135, 3, 7);
INSERT INTO `sys_role_menu` VALUES (136, 3, 8);
INSERT INTO `sys_role_menu` VALUES (137, 3, 12);
INSERT INTO `sys_role_menu` VALUES (138, 3, 13);
INSERT INTO `sys_role_menu` VALUES (139, 3, 14);
INSERT INTO `sys_role_menu` VALUES (140, 3, 9);
INSERT INTO `sys_role_menu` VALUES (141, 3, 11);
INSERT INTO `sys_role_menu` VALUES (142, 3, 15);
INSERT INTO `sys_role_menu` VALUES (143, 3, 26);
INSERT INTO `sys_role_menu` VALUES (166, 4, 20);
INSERT INTO `sys_role_menu` VALUES (167, 4, 24);
INSERT INTO `sys_role_menu` VALUES (168, 4, 26);
INSERT INTO `sys_role_menu` VALUES (169, 4, 27);
INSERT INTO `sys_role_menu` VALUES (170, 4, 28);
INSERT INTO `sys_role_menu` VALUES (171, 4, 29);
INSERT INTO `sys_role_menu` VALUES (172, 4, 30);
INSERT INTO `sys_role_menu` VALUES (173, 4, 1);
INSERT INTO `sys_role_menu` VALUES (174, 4, 2);
INSERT INTO `sys_role_menu` VALUES (175, 4, 4);
INSERT INTO `sys_role_menu` VALUES (176, 4, 5);
INSERT INTO `sys_role_menu` VALUES (177, 4, 6);
INSERT INTO `sys_role_menu` VALUES (178, 4, 7);
INSERT INTO `sys_role_menu` VALUES (179, 4, 8);
INSERT INTO `sys_role_menu` VALUES (180, 4, 12);
INSERT INTO `sys_role_menu` VALUES (181, 4, 13);
INSERT INTO `sys_role_menu` VALUES (182, 4, 14);
INSERT INTO `sys_role_menu` VALUES (183, 4, 31);
INSERT INTO `sys_role_menu` VALUES (184, 4, 11);
INSERT INTO `sys_role_menu` VALUES (185, 4, 15);
INSERT INTO `sys_role_menu` VALUES (186, 4, 16);
INSERT INTO `sys_role_menu` VALUES (187, 4, 9);
INSERT INTO `sys_role_menu` VALUES (188, 4, 3);

-- ----------------------------
-- Table structure for sys_sfc
-- ----------------------------
DROP TABLE IF EXISTS `sys_sfc`;
CREATE TABLE `sys_sfc`  (
  `sys_sfc_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_sfc_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件名称',
  `sys_sfc_chinese_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件名称',
  `sys_sfc_function` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件功能, 多个逗号分隔',
  `sys_sfc_icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件图标',
  `sys_sfc_category` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类；HOME',
  `sys_sfc_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件存放路径',
  `sys_sfc_status` int(11) NULL DEFAULT NULL COMMENT '组件状态, 0:未启用',
  `sys_sfc_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件描述',
  `sys_sfc_proxy` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '代理地址',
  `sys_sfc_type` int(11) NULL DEFAULT NULL COMMENT '组件类型, 0:文件, 1:文本',
  `sys_sfc_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '组件文本',
  `sys_sfc_model_cache` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '模块缓存',
  `sys_sfc_delay` int(11) NULL DEFAULT NULL COMMENT '延迟时间, 2000',
  `sys_sfc_version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '版本',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  `sys_sfc_timeout` int(11) NULL DEFAULT NULL COMMENT '超时时间, 3000',
  PRIMARY KEY (`sys_sfc_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '组件库' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_sfc
-- ----------------------------
INSERT INTO `sys_sfc` VALUES (2, 'time', '时间组件', '时间', 'ep:timer', 'HOME', NULL, 1, NULL, NULL, 1, '<template>\n  <el-card shadow=\"hover\" header=\"时钟\" class=\"item-background\">\n    <div class=\"time\">\n      <h2>{{ time }}</h2>\n      <p>{{ day }}</p>\n    </div>\n  </el-card>\n</template>\n\n<script>\nimport { dateFormat } from \"@/utils/date\";\nexport default {\n  title: \"时钟\",\n  icon: \"ep:clock\",\n  description: \"演示部件效果\",\n  data() {\n    return {\n      time: \"\",\n      day: \"\"\n    };\n  },\n  mounted() {\n    this.showTime();\n    setInterval(() => {\n      this.showTime();\n    }, 1000);\n	this.$emit(\"loaded\", true);\n  },\n  methods: {\n    showTime() {\n      this.time = dateFormat(new Date(), \"hh:mm:ss\");\n      this.day = dateFormat(new Date(), \"yyyy年MM月dd日\");\n    }\n  }\n};\n</script>\n\n<style scoped>\n.item-background {\n  background: linear-gradient(to right, #8e54e9, #4776e6);\n  color: #fff;\n}\n.time h2 {\n  font-size: 40px;\n}\n.time p {\n  font-size: 14px;\n  margin-top: 13px;\n  opacity: 0.7;\n}\n</style>\n', '[{\"name\":\"Date\",\"value\":\"import * as Date from \\\"@/utils/date\\\";\"}]', NULL, '1.0.0', 'sa', 1, '2024-10-25 09:26:42', '2024-10-25 09:27:12', 'sa', 1, NULL);
INSERT INTO `sys_sfc` VALUES (3, 'weather', '天气', '天气', 'meteocons:clear-day', 'HOME', '.\\sfc\\3\\weather.vue', 1, NULL, NULL, 0, 'null', 'null', NULL, '1.0.0', 'sa', 1, '2024-10-28 09:32:27', '2024-10-28 13:36:56', 'sa', 1, NULL);
INSERT INTO `sys_sfc` VALUES (4, 'local-weather', '本地天气组件', '天气', 'ep:basketball', 'HOME', '@repo/module/weather/index.vue', 1, NULL, NULL, 3, NULL, '[]', NULL, '1.0.0', 'sa', 1, '2024-11-24 10:09:56', '2024-11-24 10:10:49', 'sa', 1, NULL);
INSERT INTO `sys_sfc` VALUES (5, 'frame', 'test', '天气', 'ep:trophy-base', 'HOME', 'https://blog.csdn.net/m0_64494670/article/details/135143478', 1, NULL, NULL, 2, NULL, '[]', NULL, '1.0.0', 'sa', 1, '2024-11-24 12:55:03', '2024-11-24 13:21:45', 'sa', 1, NULL);
INSERT INTO `sys_sfc` VALUES (6, 'calendar', '日历', '日历', 'ep:calendar', 'HOME', '@repo/module/calendar/index.vue', 1, NULL, NULL, 3, '', '[]', NULL, '1.0.0', 'sa', 1, '2025-03-18 15:04:30', '2025-03-18 15:04:47', '', 0, NULL);
INSERT INTO `sys_sfc` VALUES (7, 'day', '当日天气', '天气', 'meteocons:sun-hot-fill', 'HOME', '@repo/module/day/index.vue', 1, NULL, NULL, 3, '', '[]', NULL, '1.0.0', '', 0, '2025-03-24 13:22:54', '2025-03-24 13:23:06', 'sa', 1, NULL);

-- ----------------------------
-- Table structure for sys_sfc_install
-- ----------------------------
DROP TABLE IF EXISTS `sys_sfc_install`;
CREATE TABLE `sys_sfc_install`  (
  `sys_sfc_install_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NULL DEFAULT NULL COMMENT '安裝的用户ID',
  `sys_sfc_id` int(11) NULL DEFAULT NULL COMMENT '组件ID',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_sfc_install_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户安装的组件' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_sfc_install
-- ----------------------------
INSERT INTO `sys_sfc_install` VALUES (1, 1, 2, 'sa', 1, '2024-10-25 09:33:10', NULL, NULL, NULL);
INSERT INTO `sys_sfc_install` VALUES (2, 1, 3, 'sa', 1, '2024-10-29 10:46:53', NULL, NULL, NULL);
INSERT INTO `sys_sfc_install` VALUES (3, 1, 4, 'sa', 1, '2024-11-24 10:16:03', NULL, NULL, NULL);
INSERT INTO `sys_sfc_install` VALUES (5, 1, 6, 'sa', 1, '2025-03-18 15:04:53', NULL, NULL, NULL);
INSERT INTO `sys_sfc_install` VALUES (6, 1, 5, 'sa', 1, '2025-03-18 15:04:57', NULL, NULL, NULL);

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
  `sys_user_delete` int(11) NULL DEFAULT NULL COMMENT '是否删除, 0: 未删除',
  `sys_user_status` int(11) NULL DEFAULT NULL COMMENT '是否启用；0：禁用',
  `sys_user_phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `sys_user_email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `sys_user_sex` int(11) NULL DEFAULT NULL COMMENT '性别, 0:女,1:男, 2:其他',
  `sys_user_full_address` varchar(400) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '居住地址',
  `sys_user_in_system` int(11) NULL DEFAULT NULL COMMENT '是否系统账号, 0: 非系统账号',
  `sys_user_avatar` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '头像',
  `sys_user_card` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证',
  `sys_dept_id` int(11) NULL DEFAULT NULL COMMENT '部门ID',
  `sys_user_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `sys_user_last_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后一次登录地点',
  `sys_user_register_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '注册地点',
  `sys_user_last_ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后一次登录IP',
  `sys_user_register_ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '注册地址',
  `sys_user_login_count` int(11) NULL DEFAULT NULL COMMENT '登录次数',
  `sys_client_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户端ID',
  `sys_user_totp_code` varchar(600) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'topt',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'sa', '超级管理员', '53006d111477a9b5b16c4b38ac8fad69', '123456', 0, 1, '12345678901', 'test@qq.com', NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, '192.168.110.79', NULL, 9, NULL, 'b2fe2a1217dd70078efbaaf4f969ed3d37a5ea2eb1577006922e541f9e00ee6ab8d2d590ea5b4dcddccfa04ad65985e1a9f565dd9f017170d6911e5991dba0f4', NULL, NULL, NULL, '2024-08-28 10:09:26', 'sa', 1);
INSERT INTO `sys_user` VALUES (2, 'test', '测试', '0a44d441a857bc12ce1e764af68bb068', 'i6iuq2o73anxs5', 0, 1, '1', NULL, 1, NULL, 1, NULL, NULL, 1, '', NULL, NULL, '172.16.2.226', '172.16.2.226', 16, NULL, NULL, 'sa', NULL, '2024-08-20 17:06:18', '2025-03-21 15:27:46', 'sa', 1);

-- ----------------------------
-- Table structure for sys_user_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_config`;
CREATE TABLE `sys_user_config`  (
  `sys_user_config_id` int(11) NOT NULL,
  `sys_user_config_wx_openid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信openid',
  `sys_user_config_wx_unionid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信unionid',
  `sys_user_config_gitee_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'giteeID',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_user_config_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户信息 - 用户设置表' ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户布局' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_layout
-- ----------------------------
INSERT INTO `sys_user_layout` VALUES (1, '[{\"x\":9,\"y\":0,\"w\":3,\"h\":1,\"i\":\"local-day\",\"id\":\"local-day\",\"static\":false,\"type\":1,\"moved\":false}]', '[]', '[[],[],[],{\"id\":\"local-day\"}]', 1, 'sa', NULL, '2024-08-29 17:20:12', '2025-03-26 15:23:56', 'sa', 1);

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
  `sys_log_ua` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'ua',
  `sys_log_browser` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '浏览器',
  `sys_log_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求地址',
  `sys_log_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求参数',
  `sys_log_cost` int(11) NULL DEFAULT NULL COMMENT '耗时',
  `sys_log_from` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求模块',
  `sys_log_status` int(11) NULL DEFAULT NULL COMMENT '请求状态, 0: 失败, 1: 成功',
  `sys_log_fingerprint` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '浏览器指纹',
  `sys_log_login_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '登录方式',
  `sys_dept_id` int(11) NULL DEFAULT NULL COMMENT '机构ID',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_log_id`) USING BTREE,
  INDEX `index_sys_log_from`(`sys_log_from` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 373 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_log
-- ----------------------------
INSERT INTO `sys_user_log` VALUES (193, 'test', '', '172.16.2.226', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 9991, 'LOGIN', 1, NULL, '', 1, NULL, 2, '2025-02-14 11:05:33', '2025-02-14 11:05:33', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (194, 'test', '', '172.16.2.226', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"test\",\n    \"password\": \"57ba172a6be125cca2f449826f9980ca\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]', 47148, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 2, '2025-02-14 11:06:11', '2025-02-14 11:06:11', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (195, 'test', '', '172.16.2.226', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"test\",\n    \"password\": \"57ba172a6be125cca2f449826f9980ca\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]', 3234, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 2, '2025-02-14 11:06:31', '2025-02-14 11:06:31', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (196, 'test', '', '172.16.2.226', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 30, 'LOGIN', 1, NULL, '', 1, NULL, 2, '2025-02-14 11:06:42', '2025-02-14 11:06:42', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (197, 'sa', '', '172.16.2.226', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]', 83, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-14 11:08:01', '2025-02-14 11:08:01', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (198, NULL, '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 11, 'LOGIN', 1, NULL, '', 1, NULL, NULL, '2025-02-17 15:56:36', '2025-02-17 15:56:36', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (199, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]', 167, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-17 15:56:44', '2025-02-17 15:56:44', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (200, NULL, '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 8, 'LOGIN', 1, NULL, '', 1, NULL, NULL, '2025-02-18 10:35:22', '2025-02-18 10:35:22', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (201, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]', 151, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-18 10:35:31', '2025-02-18 10:35:31', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (202, 'sa', NULL, '172.16.2.226', NULL, 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 488, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-18 16:13:58', '2025-02-18 16:13:58', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (203, 'sa', '', '172.16.2.226', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]', 417, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-18 16:15:09', '2025-02-18 16:15:09', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (204, 'sa', NULL, '172.16.2.226', NULL, 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 602, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-19 11:15:05', '2025-02-19 11:15:05', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (205, 'sa', '', '172.16.2.226', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]', 1311, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 11:21:29', '2025-02-19 11:21:29', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (206, 'sa', '', '172.16.2.226', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 48, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-19 11:21:32', '2025-02-19 11:21:32', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (207, 'sa', '', '172.16.2.226', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]', 87, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 11:24:42', '2025-02-19 11:24:42', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (208, 'sa', '', '172.16.2.226', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 37, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-19 11:24:44', '2025-02-19 11:24:44', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (209, 'sa', '', '172.16.2.226', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]', 543, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 13:19:35', '2025-02-19 13:19:35', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (210, 'sa', '', '172.16.2.226', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 68, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-19 13:19:38', '2025-02-19 13:19:38', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (211, 'sa', '', '172.16.2.226', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]', 121, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 13:22:54', '2025-02-19 13:22:54', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (212, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 198, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-19 13:31:48', '2025-02-19 13:31:48', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (213, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]', 132, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 13:33:57', '2025-02-19 13:33:57', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (214, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 31, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-19 13:34:02', '2025-02-19 13:34:02', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (215, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 0, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-19 13:34:15', '2025-02-19 13:34:15', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (216, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 0, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-19 13:34:18', '2025-02-19 13:34:18', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (217, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]', 4205, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 13:34:31', '2025-02-19 13:34:31', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (218, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 123, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-19 13:35:19', '2025-02-19 13:35:19', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (219, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]', 13008, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 13:36:31', '2025-02-19 13:36:31', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (220, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 30, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-19 13:37:04', '2025-02-19 13:37:04', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (221, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\",\n    \"accountType\": \"tenant\"\n  }\n]', 6937, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 13:37:14', '2025-02-19 13:37:14', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (222, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 37, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-19 13:37:48', '2025-02-19 13:37:48', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (223, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"accountType\": \"tenant\"\n  }\n]', 2945, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 13:37:55', '2025-02-19 13:37:55', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (224, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"accountType\": \"tenant\"\n  }\n]', 7846, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 13:38:06', '2025-02-19 13:38:06', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (225, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"accountType\": \"tenant\",\n    \"tenantId\": \"1\"\n  }\n]', 30, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 13:38:25', '2025-02-19 13:38:25', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (226, NULL, '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"accountType\": \"tenant\",\n    \"tenantId\": \"1\"\n  }\n]', 1122, 'LOGIN', 1, NULL, 'WEB', 1, NULL, NULL, '2025-02-19 13:39:50', '2025-02-19 13:39:50', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (227, NULL, '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"accountType\": \"tenant\",\n    \"tenantId\": \"1\"\n  }\n]', 6900, 'LOGIN', 1, NULL, 'WEB', 1, NULL, NULL, '2025-02-19 13:40:12', '2025-02-19 13:40:12', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (228, NULL, '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"accountType\": \"tenant\",\n    \"tenantId\": \"1\"\n  }\n]', 5839, 'LOGIN', 1, NULL, 'WEB', 1, NULL, NULL, '2025-02-19 13:43:30', '2025-02-19 13:43:30', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (229, NULL, '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"accountType\": \"tenant\",\n    \"tenantId\": \"1\"\n  }\n]', 2769, 'LOGIN', 1, NULL, 'WEB', 1, NULL, NULL, '2025-02-19 13:44:32', '2025-02-19 13:44:32', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (230, NULL, '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"accountType\": \"tenant\",\n    \"tenantId\": \"1\"\n  }\n]', 3315, 'LOGIN', 1, NULL, 'WEB', 1, NULL, NULL, '2025-02-19 13:44:40', '2025-02-19 13:44:40', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (231, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"1\"\n  }\n]', 3280, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 13:50:08', '2025-02-19 13:50:08', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (232, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 103, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-19 13:50:14', '2025-02-19 13:50:14', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (233, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 257, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 13:50:15', '2025-02-19 13:50:15', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (234, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 4266, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 14:04:49', '2025-02-19 14:04:49', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (235, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 92, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-19 14:04:52', '2025-02-19 14:04:52', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (236, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"accountType\": \"tenant\",\n    \"tenantId\": \"1\"\n  }\n]', 538, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 14:05:53', '2025-02-19 14:05:53', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (237, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"accountType\": \"tenant\",\n    \"tenantId\": \"1\"\n  }\n]', 15444, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 14:06:49', '2025-02-19 14:06:49', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (238, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"accountType\": \"tenant\",\n    \"tenantId\": \"1\"\n  }\n]', 18131, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 14:07:20', '2025-02-19 14:07:20', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (239, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"accountType\": \"tenant\",\n    \"tenantId\": \"1\"\n  }\n]', 18688, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 14:07:44', '2025-02-19 14:07:44', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (240, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 167, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-19 14:19:07', '2025-02-19 14:19:07', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (241, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"accountType\": \"tenant\",\n    \"tenantId\": \"\"\n  }\n]', 8130, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 14:20:08', '2025-02-19 14:20:08', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (242, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 2748, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-19 14:25:03', '2025-02-19 14:25:03', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (243, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 2355, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 14:25:05', '2025-02-19 14:25:05', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (244, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 93, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-19 14:26:28', '2025-02-19 14:26:28', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (245, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 308, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 14:26:30', '2025-02-19 14:26:30', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (246, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 294, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 14:30:14', '2025-02-19 14:30:14', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (247, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 699, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 14:44:59', '2025-02-19 14:44:59', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (248, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 2920, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 15:00:08', '2025-02-19 15:00:08', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (249, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 2252, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 15:17:27', '2025-02-19 15:17:27', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (250, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 315, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-19 15:34:16', '2025-02-19 15:34:16', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (251, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 331, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-19 15:34:17', '2025-02-19 15:34:17', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (252, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 3203, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-20 15:46:18', '2025-02-20 15:46:18', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (253, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 655, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-24 15:48:08', '2025-02-24 15:48:08', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (254, 'sa', '', '192.168.137.1', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 698, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-24 16:45:59', '2025-02-24 16:45:59', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (255, 'sa', '', '192.168.137.1', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 206, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-24 16:46:00', '2025-02-24 16:46:00', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (256, 'sa', '', '192.168.137.1', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 275, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-24 16:46:06', '2025-02-24 16:46:06', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (257, 'sa', '', '192.168.137.1', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 134, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-24 16:46:06', '2025-02-24 16:46:06', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (258, 'sa', '', '192.168.137.1', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 5357, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-24 16:46:28', '2025-02-24 16:46:28', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (259, 'sa', '', '192.168.137.1', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 48, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-24 16:46:29', '2025-02-24 16:46:29', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (260, 'sa', '', '192.168.137.1', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 276, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-24 16:46:38', '2025-02-24 16:46:38', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (261, 'sa', '', '192.168.137.1', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 73, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-24 16:46:38', '2025-02-24 16:46:38', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (262, 'sa', '', '192.168.137.1', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 274, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-24 16:47:00', '2025-02-24 16:47:00', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (263, 'sa', '', '192.168.137.1', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 56, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-24 16:47:09', '2025-02-24 16:47:09', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (264, 'sa', '', '192.168.137.1', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 60017, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-24 16:48:16', '2025-02-24 16:48:16', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (265, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 60016, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-24 17:00:10', '2025-02-24 17:00:10', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (266, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 10383, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-24 17:00:27', '2025-02-24 17:00:27', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (267, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 50, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-24 17:00:27', '2025-02-24 17:00:27', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (268, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 2351, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-24 17:01:09', '2025-02-24 17:01:09', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (269, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 46, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-24 17:01:09', '2025-02-24 17:01:09', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (270, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 12081, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-24 17:01:28', '2025-02-24 17:01:28', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (271, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 46, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-24 17:01:28', '2025-02-24 17:01:28', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (272, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 4257, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-24 17:01:44', '2025-02-24 17:01:44', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (273, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 47, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-24 17:01:44', '2025-02-24 17:01:44', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (274, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 7829, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-24 17:10:32', '2025-02-24 17:10:32', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (275, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 516, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-25 10:43:15', '2025-02-25 10:43:15', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (276, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 698, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-25 10:45:41', '2025-02-25 10:45:41', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (277, NULL, '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"accountType\": \"tenant\",\n    \"tenantId\": \"\"\n  }\n]', 2489, 'LOGIN', 1, NULL, 'WEB', 1, NULL, NULL, '2025-02-25 11:22:16', '2025-02-25 11:22:16', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (278, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 1170, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-25 11:23:16', '2025-02-25 11:23:16', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (279, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 391, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-25 11:25:56', '2025-02-25 11:25:56', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (280, 'sa', '', '192.168.137.1', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 928, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-25 13:08:28', '2025-02-25 13:08:28', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (281, 'sa', '', '192.168.137.1', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 1181, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-25 13:20:50', '2025-02-25 13:20:50', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (282, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 255, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-25 13:31:01', '2025-02-25 13:31:01', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (283, 'test', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 172, 'LOGIN', 1, NULL, '', 1, NULL, 20, '2025-02-25 14:17:59', '2025-02-25 14:17:59', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (284, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 1562, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-25 14:18:01', '2025-02-25 14:18:01', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (285, 'sa', '', '192.168.137.1', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 1142, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-25 14:48:21', '2025-02-25 14:48:21', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (286, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 2893, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-25 15:08:39', '2025-02-25 15:08:39', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (287, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 550, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-25 15:08:39', '2025-02-25 15:08:39', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (288, 'sa', '局域网', '192.168.137.1', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 257, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-25 15:14:18', '2025-02-25 15:14:18', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (289, 'test', NULL, '172.16.2.226', NULL, 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 659, 'LOGIN', 1, NULL, '', 1, NULL, 20, '2025-02-25 16:32:23', '2025-02-25 16:32:23', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (290, 'sa', '', '172.16.2.226', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 1037, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-25 16:32:25', '2025-02-25 16:32:25', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (291, 'sa', '', '172.16.2.226', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 216, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-25 16:51:27', '2025-02-25 16:51:27', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (292, 'sa', '', '172.16.2.226', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 47, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-02-25 16:59:39', '2025-02-25 16:59:39', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (293, 'sa', NULL, '172.16.2.226', NULL, 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 86, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-25 17:01:52', '2025-02-25 17:01:52', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (294, 'sa', NULL, '192.168.137.1', NULL, 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 291, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-26 17:27:06', '2025-02-26 17:27:06', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (295, 'sa', '', '192.168.137.1', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 523, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-02-27 09:15:56', '2025-02-27 09:15:56', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (296, 'sa', NULL, '192.168.110.79', NULL, 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 1279, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-04 14:00:16', '2025-03-04 14:00:16', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (297, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 40, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-03-04 14:00:17', '2025-03-04 14:00:17', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (298, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 51, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-04 14:00:26', '2025-03-04 14:00:26', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (299, 'sa', NULL, '192.168.137.1', NULL, 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 1967, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-05 13:18:16', '2025-03-05 13:18:16', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (300, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 202, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-03-07 15:36:04', '2025-03-07 15:36:04', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (301, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 90, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-07 15:36:06', '2025-03-07 15:36:06', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (302, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 59, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-07 15:37:46', '2025-03-07 15:37:46', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (303, NULL, '局域网', '192.168.110.79', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 18, 'LOGIN', 1, NULL, '', 1, NULL, NULL, '2025-03-18 08:42:11', '2025-03-18 08:42:11', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (304, 'sa', '局域网', '192.168.110.79', '对方和您在同一内部网', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 264, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-18 08:42:24', '2025-03-18 08:42:24', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (305, 'sa', NULL, '192.168.110.79', NULL, 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 1008, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 13:06:10', '2025-03-21 13:06:10', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (306, NULL, '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/logout', '[]', 1, 'LOGIN', 1, NULL, '', 1, NULL, NULL, '2025-03-21 13:06:11', '2025-03-21 13:06:11', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (307, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 61, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 13:06:27', '2025-03-21 13:06:27', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (308, NULL, '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/logout', '[]', 0, 'LOGIN', 1, NULL, '', 1, NULL, NULL, '2025-03-21 13:06:27', '2025-03-21 13:06:27', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (309, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 46, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-03-21 13:08:08', '2025-03-21 13:08:08', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (310, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 49, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 13:08:09', '2025-03-21 13:08:09', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (311, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 37, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-03-21 13:08:11', '2025-03-21 13:08:11', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (312, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 44, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 13:08:13', '2025-03-21 13:08:13', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (313, NULL, '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/logout', '[]', 0, 'LOGIN', 1, NULL, '', 1, NULL, NULL, '2025-03-21 13:08:13', '2025-03-21 13:08:13', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (314, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 38, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 13:08:24', '2025-03-21 13:08:24', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (315, NULL, '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/logout', '[]', 0, 'LOGIN', 1, NULL, '', 1, NULL, NULL, '2025-03-21 13:08:24', '2025-03-21 13:08:24', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (316, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 51, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 13:08:34', '2025-03-21 13:08:34', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (317, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 57, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 13:11:08', '2025-03-21 13:11:08', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (318, NULL, '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/logout', '[]', 0, 'LOGIN', 1, NULL, '', 1, NULL, NULL, '2025-03-21 13:11:08', '2025-03-21 13:11:08', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (319, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 57, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 13:21:55', '2025-03-21 13:21:55', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (320, NULL, '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/logout', '[]', 0, 'LOGIN', 1, NULL, '', 1, NULL, NULL, '2025-03-21 13:21:55', '2025-03-21 13:21:55', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (321, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 51, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 13:22:17', '2025-03-21 13:22:17', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (322, NULL, '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/logout', '[]', 0, 'LOGIN', 1, NULL, '', 1, NULL, NULL, '2025-03-21 13:22:47', '2025-03-21 13:22:47', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (323, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 38, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 13:23:18', '2025-03-21 13:23:18', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (324, NULL, '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/logout', '[]', 0, 'LOGIN', 1, NULL, '', 1, NULL, NULL, '2025-03-21 13:23:18', '2025-03-21 13:23:18', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (325, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 90, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 13:23:46', '2025-03-21 13:23:46', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (326, NULL, '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/logout', '[]', 1, 'LOGIN', 1, NULL, '', 1, NULL, NULL, '2025-03-21 13:23:46', '2025-03-21 13:23:46', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (327, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 43, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 13:23:53', '2025-03-21 13:23:53', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (328, NULL, '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/logout', '[]', 1, 'LOGIN', 1, NULL, '', 1, NULL, NULL, '2025-03-21 13:23:53', '2025-03-21 13:23:53', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (329, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 44, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 13:24:14', '2025-03-21 13:24:14', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (330, NULL, '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/logout', '[]', 0, 'LOGIN', 1, NULL, '', 1, NULL, NULL, '2025-03-21 13:24:27', '2025-03-21 13:24:27', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (331, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 2978, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 13:26:30', '2025-03-21 13:26:30', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (332, NULL, '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/logout', '[]', 1, 'LOGIN', 1, NULL, '', 1, NULL, NULL, '2025-03-21 13:27:00', '2025-03-21 13:27:00', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (333, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 265, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 13:28:37', '2025-03-21 13:28:37', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (334, NULL, '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/logout', '[]', 0, 'LOGIN', 1, NULL, '', 1, NULL, NULL, '2025-03-21 13:29:07', '2025-03-21 13:29:07', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (335, 'sa', NULL, '192.168.110.79', NULL, 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 9173, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 14:12:06', '2025-03-21 14:12:06', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (336, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 456, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 14:12:27', '2025-03-21 14:12:27', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (337, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 276, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 14:12:42', '2025-03-21 14:12:42', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (338, NULL, '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/logout', '[]', 1, 'LOGIN', 1, NULL, '', 1, NULL, NULL, '2025-03-21 14:13:12', '2025-03-21 14:13:12', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (339, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 1026, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 14:14:48', '2025-03-21 14:14:48', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (340, NULL, '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/logout', '[]', 1, 'LOGIN', 1, NULL, '', 1, NULL, NULL, '2025-03-21 14:14:55', '2025-03-21 14:14:55', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (341, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 69, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 14:15:02', '2025-03-21 14:15:02', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (342, NULL, '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) TraeCN/1.97.2 Chrome/128.0.6613.186 Electron/32.2.7 Safari/537.36', 'CHROME12', '/system/api/v2/login/logout', '[]', 0, 'LOGIN', 1, NULL, '', 1, NULL, NULL, '2025-03-21 14:15:02', '2025-03-21 14:15:02', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (343, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 37, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-03-21 14:15:12', '2025-03-21 14:15:12', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (344, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 45, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 14:15:13', '2025-03-21 14:15:13', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (345, 'sa', NULL, '192.168.110.79', NULL, 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 636, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 15:41:53', '2025-03-21 15:41:53', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (346, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 47, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 15:42:18', '2025-03-21 15:42:18', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (347, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 395, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-03-21 16:13:04', '2025-03-21 16:13:04', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (348, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 109, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-21 16:13:05', '2025-03-21 16:13:05', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (349, NULL, NULL, '192.168.110.79', NULL, 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 1133, 'LOGIN', 1, NULL, 'WEB', 1, NULL, NULL, '2025-03-24 13:12:19', '2025-03-24 13:12:19', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (350, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 4906, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-24 13:12:19', '2025-03-24 13:12:19', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (351, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 61, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-24 13:12:22', '2025-03-24 13:12:22', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (352, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 46, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-03-24 13:15:42', '2025-03-24 13:15:42', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (353, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 54, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-24 13:15:44', '2025-03-24 13:15:44', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (354, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 328, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-24 14:17:39', '2025-03-24 14:17:39', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (355, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 271, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-03-24 14:57:31', '2025-03-24 14:57:31', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (356, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 1709, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-24 14:57:36', '2025-03-24 14:57:36', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (357, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 110, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-03-24 16:10:12', '2025-03-24 16:10:12', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (358, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 1025, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-24 16:38:33', '2025-03-24 16:38:33', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (359, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 50, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-03-24 16:39:00', '2025-03-24 16:39:00', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (360, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 44, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-24 16:39:14', '2025-03-24 16:39:14', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (361, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 17, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-03-24 16:39:16', '2025-03-24 16:39:16', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (362, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 56, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-24 16:39:41', '2025-03-24 16:39:41', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (363, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 39, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-03-24 16:39:43', '2025-03-24 16:39:43', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (364, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 46, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-24 16:40:14', '2025-03-24 16:40:14', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (365, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 20, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-03-24 16:40:16', '2025-03-24 16:40:16', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (366, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 58, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-24 16:40:46', '2025-03-24 16:40:46', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (367, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 34, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-03-24 16:40:49', '2025-03-24 16:40:49', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (368, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 44, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-24 16:40:51', '2025-03-24 16:40:51', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (369, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/logout', '[]', 61, 'LOGIN', 1, NULL, '', 1, NULL, 1, '2025-03-24 16:44:31', '2025-03-24 16:44:31', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (370, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 68, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-24 16:45:21', '2025-03-24 16:45:21', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (371, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 1270, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-25 21:29:05', '2025-03-25 21:29:05', NULL, NULL);
INSERT INTO `sys_user_log` VALUES (372, 'sa', '', '192.168.110.79', '', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36 Edg/134.0.0.0', 'CHROME13', '/system/api/v2/login/login', '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"7be2228e651b0e93292a02f3582da66a\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"WEB\",\n    \"tenantId\": \"\"\n  }\n]', 1029, 'LOGIN', 1, NULL, 'WEB', 1, NULL, 1, '2025-03-26 15:23:44', '2025-03-26 15:23:44', NULL, NULL);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `sys_user_role_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_user_id` int(11) NULL DEFAULT NULL COMMENT '用户ID',
  `sys_role_id` int(11) NULL DEFAULT NULL COMMENT '角色ID',
  PRIMARY KEY (`sys_user_role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户 - 角色关系表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1, 1);
INSERT INTO `sys_user_role` VALUES (30, 2, 4);

-- ----------------------------
-- Table structure for sys_user_third_party
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_third_party`;
CREATE TABLE `sys_user_third_party`  (
  `sys_user_third_party_id` int(11) NOT NULL AUTO_INCREMENT,
  `sys_user_third_party_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '三方类型. gitee',
  `sys_user_third_party_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '三方信息名称',
  `sys_user_third_party_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '三方信息',
  `sys_user_id` int(11) NULL DEFAULT NULL COMMENT '当前系统用户ID',
  `sys_user_third_party_uid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '三方用户ID',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_user_third_party_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户三方绑定表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_third_party
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
