/*
 Navicat Premium Data Transfer

 Source Server         : 172.16.2.226
 Source Server Type    : MySQL
 Source Server Version : 80033
 Source Host           : 172.16.2.226:3306
 Source Schema         : sys

 Target Server Type    : MySQL
 Target Server Version : 80033
 File Encoding         : 65001

 Date: 17/01/2024 15:25:09
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
  `tenant_id` int NULL DEFAULT 0 COMMENT '租户ID',
  PRIMARY KEY (`dept_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '部门表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dept
-- ----------------------------

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
  `tenant_id` int NULL DEFAULT NULL,
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_by` int NULL DEFAULT NULL,
  `create_time` date NULL DEFAULT NULL,
  `update_time` date NULL DEFAULT NULL,
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `update_by` int NULL DEFAULT NULL,
  PRIMARY KEY (`dict_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字典数据表' ROW_FORMAT = DYNAMIC;

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
  `tenant_id` int NULL DEFAULT 0,
  PRIMARY KEY (`dict_type_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字典类型表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------

-- ----------------------------
-- Table structure for sys_file_storage
-- ----------------------------
DROP TABLE IF EXISTS `sys_file_storage`;
CREATE TABLE `sys_file_storage`  (
  `fs_id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `fs_bucket` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'bucket',
  `fs_domain` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '前端访问地址',
  `fs_storage_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件存储位置',
  `fs_endpoint` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '三方服务器地址',
  `fs_access_key_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'ak',
  `fs_access_key_secret` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'sk',
  `fs_status` int NULL DEFAULT 1 COMMENT '状态(1:正常;0:禁用)',
  `tenant_id` int NULL DEFAULT 0 COMMENT '租户ID',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` date NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` date NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  `fs_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `fs_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'OSS类型',
  `fs_filter` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片滤镜',
  `fs_plugin` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '插件; 文件上传时预处理',
  `fs_named` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件存储命名方式',
  PRIMARY KEY (`fs_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'OSS' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_file_storage
-- ----------------------------

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
  `tenant_id` int NULL DEFAULT 0 COMMENT '租户ID',
  PRIMARY KEY (`menu_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 48 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜单实体对象' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------

-- ----------------------------
-- Table structure for sys_operate_logger
-- ----------------------------
DROP TABLE IF EXISTS `sys_operate_logger`;
CREATE TABLE `sys_operate_logger`  (
  `log_id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `log_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志编号',
  `log_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志名称',
  `log_cost` int NULL DEFAULT NULL COMMENT '耗时',
  `log_mapping` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志请求地址',
  `log_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志内容',
  `client_ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求地址',
  `client_ip_position` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求地址位置',
  `log_watch` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '请求链路',
  `log_param` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '查询参数',
  `log_module` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作模块',
  `log_status` int NULL DEFAULT NULL COMMENT '状态; 1:成功;',
  `log_day` date NULL DEFAULT NULL COMMENT '时间(天)',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `update_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `tenant_id` int NULL DEFAULT 0 COMMENT '租户ID',
  PRIMARY KEY (`log_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 143 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统日志信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_operate_logger
-- ----------------------------

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
  `tenant_id` int NULL DEFAULT 0,
  PRIMARY KEY (`role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role
-- ----------------------------

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `role_id` bigint NULL DEFAULT NULL,
  `menu_id` bigint NULL DEFAULT NULL,
  `tenant_id` int NULL DEFAULT NULL,
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_by` int NULL DEFAULT NULL,
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `update_by` int NULL DEFAULT NULL,
  INDEX `index_role_id`(`role_id` DESC) USING BTREE,
  INDEX `index_menu_id`(`menu_id` DESC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色菜单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------

-- ----------------------------
-- Table structure for sys_setting
-- ----------------------------
DROP TABLE IF EXISTS `sys_setting`;
CREATE TABLE `sys_setting`  (
  `setting_id` int NOT NULL AUTO_INCREMENT,
  `setting_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设置名称',
  `setting_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设置值',
  `setting_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设置描述',
  `setting_enable` int NULL DEFAULT NULL COMMENT '是否开启; 0:不开启',
  `setting_version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设置版本',
  `setting_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '参数类型',
  PRIMARY KEY (`setting_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统设置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_setting
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
  `tenant_id` int NULL DEFAULT 0 COMMENT '租户ID',
  PRIMARY KEY (`user_id`) USING BTREE,
  INDEX `index_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, NULL, 'admin', '系统管理员', '系统管理员000', '系统管理员', 1, 1, '8512935d6d726a53c13dc9d5cf6f72c7', NULL, NULL, 1, 'https://oss.youlai.tech/youlai-boot/2023/05/16/811270ef31f548af9cffc026dfc3777b.gif', '17621210366', NULL, 1, '', 0, 'krdpgdbsinu1zt', NULL, NULL, '2019-10-10 13:41:22.000000', '2023-07-22 20:32:09.562000', '系统管理员', NULL, 0);

-- ----------------------------
-- Table structure for sys_user_grid
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_grid`;
CREATE TABLE `sys_user_grid`  (
  `grid` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `layout` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '布局',
  `copms_list` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件',
  `tenant_id` int NULL DEFAULT NULL,
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_by` int NULL DEFAULT NULL,
  `create_time` date NULL DEFAULT NULL,
  `update_time` date NULL DEFAULT NULL,
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `update_by` int NULL DEFAULT NULL,
  PRIMARY KEY (`grid`) USING BTREE,
  INDEX `index_user_id`(`user_id` DESC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户主页布局' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_grid
-- ----------------------------

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `user_id` bigint NULL DEFAULT NULL,
  `role_id` bigint NULL DEFAULT NULL,
  `tenant_id` int NULL DEFAULT NULL,
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_by` int NULL DEFAULT NULL,
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `update_by` int NULL DEFAULT NULL,
  INDEX `index_user_id`(`user_id` DESC) USING BTREE,
  INDEX `index_role_id`(`role_id` DESC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_role
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
