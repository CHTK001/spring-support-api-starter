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

 Date: 17/01/2024 09:52:12
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
INSERT INTO `sys_dept` VALUES (1, '测试', NULL, 0, '0', 1, 1, '系统管理员', '1', '2023-08-11 11:21:26.620000', NULL, NULL, NULL, 0);
INSERT INTO `sys_dept` VALUES (2, '子部门1', NULL, 1, '0,1', 1, 1, '系统管理员', NULL, '2024-01-09 15:46:41.543000', NULL, NULL, NULL, 0);
INSERT INTO `sys_dept` VALUES (3, '子部门2', NULL, 2, '0,1,2', 1, 1, '系统管理员', NULL, '2024-01-09 15:46:59.388000', NULL, NULL, NULL, 0);
INSERT INTO `sys_dept` VALUES (4, '子部门3', NULL, 1, '0,1', 1, 1, '系统管理员', NULL, '2024-01-09 15:47:12.118000', NULL, NULL, NULL, 0);
INSERT INTO `sys_dept` VALUES (5, '运维', NULL, 0, '0', 1, 1, '系统管理员', NULL, '2024-01-09 15:47:16.405000', NULL, NULL, NULL, 0);
INSERT INTO `sys_dept` VALUES (6, '子部门4', NULL, 2, '0,1,2', 1, 1, '系统管理员', NULL, '2024-01-09 15:47:29.621000', NULL, NULL, NULL, 0);
INSERT INTO `sys_dept` VALUES (7, '子部门555', NULL, 1, '0,1', 1, 1, '系统管理员', NULL, '2024-01-09 15:49:05.000000', '2024-01-09 15:49:16.299000', '系统管理员', NULL, 0);

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
INSERT INTO `sys_dict` VALUES (1, '3', '群众', '1', NULL, 1, 0, 1, '群众', NULL, '系统管理员', NULL, '2024-01-09', '2024-01-09', '系统管理员', NULL);
INSERT INTO `sys_dict` VALUES (2, '3', '党员', '2', NULL, 1, 0, 1, '党员', NULL, '系统管理员', NULL, '2024-01-09', '2024-01-09', '系统管理员', NULL);
INSERT INTO `sys_dict` VALUES (3, '1', '男', '1', NULL, 1, 0, 1, NULL, NULL, '系统管理员', NULL, '2024-01-09', NULL, NULL, NULL);
INSERT INTO `sys_dict` VALUES (4, '1', '女', '0', NULL, 1, 0, 1, NULL, NULL, '系统管理员', NULL, '2024-01-09', NULL, NULL, NULL);
INSERT INTO `sys_dict` VALUES (5, '1', '其他', '2', NULL, 1, 0, 1, NULL, NULL, '系统管理员', NULL, '2024-01-09', NULL, NULL, NULL);

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
INSERT INTO `sys_dict_type` VALUES (1, '性别', 'XB', NULL, 0, 0, NULL, NULL, NULL, '2023-07-20 09:10:12.651000', '2023-07-20 09:10:30.027000', NULL, NULL, 0);
INSERT INTO `sys_dict_type` VALUES (3, '人员类型', 'USER_TYPE', NULL, 0, 0, '人员类型', '系统管理员', NULL, '2024-01-09 15:45:32.270000', NULL, NULL, NULL, 0);

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
INSERT INTO `sys_file_storage` VALUES (22, 'filesystem', 'http://localhost:18170/admin/api', NULL, 'http://127.0.0.1:9000', 'minioadmin', 'minioadmin', 1, 0, '系统管理员', NULL, '2024-01-16', '2024-01-16', '系统管理员', NULL, 'minio', 'MINIO', 'SEPIATONE', NULL, NULL);
INSERT INTO `sys_file_storage` VALUES (23, 'test', 'http://192.168.110.6:18170/v1/file/test/preview/', NULL, 'Z://', NULL, NULL, 1, 0, '系统管理员', NULL, '2024-01-16', NULL, NULL, NULL, '测试', 'LOCAL', NULL, NULL, NULL);

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
INSERT INTO `sys_menu` VALUES (1, 0, '首页', 'menu', '/home', NULL, NULL, NULL, NULL, 1, 1, NULL, 'el-icon-eleme-filled', NULL, '0', 0, NULL, NULL, NULL, '2024-01-12 14:04:36.079000', '系统管理员', NULL, 0);
INSERT INTO `sys_menu` VALUES (2, 1, '控制台', 'menu', '/dashboard', 'home', NULL, NULL, NULL, 1, 1, NULL, 'el-icon-menu', NULL, '0,1', 1, NULL, NULL, NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (3, 0, '系统管理', 'menu', '/setting', NULL, NULL, NULL, NULL, 1, 1, NULL, 'el-icon-setting', NULL, '0', 0, NULL, NULL, NULL, '2023-07-19 19:23:29.816000', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (4, 3, '用户管理', 'menu', '/setting/user', 'setting/user', NULL, NULL, NULL, 1, 1, 2, 'el-icon-user-filled', NULL, '0,3', 0, NULL, NULL, NULL, '2023-07-23 11:09:59.947000', '系统管理员', NULL, 0);
INSERT INTO `sys_menu` VALUES (5, 3, '菜单管理', 'menu', '/setting/menu', 'setting/menu', NULL, NULL, NULL, 1, 1, 100, 'el-icon-fold', NULL, '0,3', 0, NULL, NULL, NULL, '2023-07-20 14:38:51.853000', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (6, 0, '其它', 'menu', '/other', '', NULL, NULL, NULL, 1, 1, 1, 'el-icon-add-location', NULL, '', 0, NULL, NULL, '2023-07-19 17:09:51.356000', '2023-07-19 19:23:07.348000', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (7, 6, '外部链接', 'link', 'http://www.baidu.com', '', NULL, NULL, NULL, 1, 1, 1, 'el-icon-aim', NULL, '', 0, NULL, NULL, '2023-07-19 17:09:59.980000', '2023-07-19 17:18:35.454000', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (8, 6, '内部链接', 'iframe', 'http://www.baidu.com', '', NULL, NULL, NULL, 1, 1, 2, 'el-icon-add-location', NULL, '', 0, NULL, NULL, '2023-07-19 17:17:18.884000', '2023-07-19 17:19:23.222000', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (9, 3, '字典管理', 'menu', '/setting/dic', '/setting/dic', '', '#A74E4E', NULL, 1, 1, NULL, 'el-icon-collection', NULL, '0,3', 0, NULL, NULL, '2023-07-19 19:24:06.447000', '2023-07-20 09:52:47.754000', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (10, 3, '角色管理', 'menu', '/setting/role', '/setting/role', '', NULL, NULL, 1, 1, 3, 'el-icon-notebook', NULL, '0,3', 0, NULL, NULL, '2023-07-20 09:49:50.586000', '2023-07-21 09:14:19.321000', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (11, 3, '操作日志', 'menu', '/setting/log', '/setting/log', NULL, NULL, NULL, 1, 1, 99, 'el-icon-warning', NULL, '0,3', 0, NULL, NULL, '2023-07-20 14:38:59.874000', '2023-07-20 14:40:09.108000', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (12, 3, '部门管理', 'menu', '/setting/dept', '/setting/dept', '', NULL, NULL, 1, 1, 1, 'sc-icon-organization', NULL, '0,3', 0, NULL, NULL, '2023-07-21 09:13:54.152000', '2023-07-21 10:44:25.501000', NULL, NULL, 0);
INSERT INTO `sys_menu` VALUES (15, 6, '空白模板', 'menu', '/template/layout/blank', '/template/layout/blank.vue', NULL, NULL, NULL, 1, 1, NULL, 'sc-icon-template', NULL, '', 0, '系统管理员', '1', '2023-07-28 15:27:40.503000', '2023-07-28 15:48:47.482000', '系统管理员', '1', 0);
INSERT INTO `sys_menu` VALUES (16, 1, '账号信息', 'menu', '/usercenter', '/usercenter', NULL, NULL, NULL, 0, 1, NULL, 'el-icon-avatar', NULL, '0,1', 0, '系统管理员', '1', '2023-07-31 08:37:42.304000', '2023-07-31 08:38:18.873000', '系统管理员', '1', 0);
INSERT INTO `sys_menu` VALUES (17, 0, '机器学习', 'menu', '/learning', '/learning', '', NULL, NULL, 1, 1, NULL, 'sc-icon-people-face', NULL, '', 0, '系统管理员', '1', '2023-07-31 15:14:23.790000', '2023-08-14 11:14:58.193000', '系统管理员', '1', 0);
INSERT INTO `sys_menu` VALUES (18, 17, '人脸比对1:1', 'menu', '/learning/faceCompare', '/learning/faceCompare', '', NULL, NULL, 1, 1, NULL, 'sc-icon-face-compare', NULL, '', 0, '系统管理员', '1', '2023-07-31 15:14:44.121000', '2023-07-31 15:31:05.122000', '系统管理员', '1', 0);
INSERT INTO `sys_menu` VALUES (19, 17, '图像比对1:1', 'menu', '/learning/imageCompare', '/learning/imageCompare', '', NULL, NULL, 1, 1, NULL, 'sc-icon-image-compare', NULL, '', 0, '系统管理员', '1', '2023-07-31 15:16:59.334000', '2023-07-31 15:29:17.901000', '系统管理员', '1', 0);
INSERT INTO `sys_menu` VALUES (20, 17, 'OCR识别', 'menu', '/learning/ocr', '/learning/ocr', '', NULL, NULL, 1, 1, NULL, 'sc-icon-image-reg', NULL, '', 0, '系统管理员', '1', '2023-07-31 15:28:47.536000', '2023-07-31 15:30:42.470000', '系统管理员', '1', 0);
INSERT INTO `sys_menu` VALUES (21, 17, '人脸检测', 'menu', '/learning/faceDetection', '/learning/faceDetection', NULL, NULL, NULL, 1, 1, NULL, 'sc-icon-face-detection', NULL, '', 0, '系统管理员', '1', '2023-08-01 14:39:31.813000', '2023-08-01 14:46:20.276000', '系统管理员', '1', 0);
INSERT INTO `sys_menu` VALUES (22, 17, '底库', 'menu', '/learning/library/:indexName/:libType', '/learning/library', NULL, NULL, NULL, 0, 1, NULL, 'sc-icon-face-reg-library', NULL, '', 0, '系统管理员', '1', '2023-08-02 13:24:18.930000', '2023-08-03 15:11:58.106000', '系统管理员', '1', 0);
INSERT INTO `sys_menu` VALUES (23, 17, '底库管理', 'menu', '/setting/library', '/setting/library', NULL, NULL, NULL, 1, 1, NULL, 'sc-icon-face-reg-library', NULL, '', 0, '系统管理员', '1', '2023-08-03 13:06:40.846000', '2023-08-10 11:18:36.120000', '系统管理员', '1', 0);
INSERT INTO `sys_menu` VALUES (24, 17, 'GAN', 'menu', '/learning/gan', '/learning/gan', NULL, NULL, NULL, 1, 1, NULL, 'sc-icon-sc', NULL, '', 0, '系统管理员', '1', '2023-08-08 10:19:00.123000', '2023-08-10 11:18:45.435000', '系统管理员', '1', 0);
INSERT INTO `sys_menu` VALUES (26, 0, '调度管理', 'menu', '/scheduler', '/scheduler', 'NEW', NULL, NULL, 1, 1, NULL, 'sc-icon-scheduler', NULL, '', 0, '系统管理员', '1', '2023-08-08 14:45:21.839000', '2023-08-09 13:39:29.964000', '系统管理员', '1', 0);
INSERT INTO `sys_menu` VALUES (27, 26, '调度任务管理', 'menu', '/scheduler/jobinfo', '/scheduler/jobinfo', NULL, NULL, '任务管理', 1, 1, NULL, 'el-icon-alarm-clock', NULL, '', 0, '系统管理员', '1', '2023-08-08 14:57:43.683000', '2023-08-14 13:33:55.373000', '系统管理员', '1', 0);
INSERT INTO `sys_menu` VALUES (28, 26, '调度日志', 'menu', '/scheduler/joblog/:jobGroup/:jobId', '/scheduler/joblog', NULL, NULL, NULL, 1, 1, NULL, 'el-icon-warning', NULL, '', 0, '系统管理员', '1', '2023-08-08 15:53:41.485000', '2023-08-09 13:39:41.037000', '系统管理员', '1', 0);
INSERT INTO `sys_menu` VALUES (29, 26, '执行器管理', 'menu', '/scheduler/jobgroup', '/scheduler/jobgroup', NULL, NULL, NULL, 1, 1, NULL, 'el-icon-takeaway-box', NULL, '', 0, '系统管理员', '1', '2023-08-09 13:38:58.388000', '2023-08-09 13:40:06.860000', '系统管理员', '1', 0);
INSERT INTO `sys_menu` VALUES (30, 17, '人脸1:N', 'menu', '/learning/faceCompareN', '/learning/faceCompareN', NULL, NULL, NULL, 1, 1, NULL, 'sc-icon-face-detection', NULL, '', 0, '系统管理员', '1', '2023-08-10 11:17:50.071000', '2023-08-10 11:18:19.043000', '系统管理员', '1', 0);
INSERT INTO `sys_menu` VALUES (32, 0, '配置中心', 'menu', '/config', '/config', NULL, NULL, NULL, 1, 1, NULL, 'sc-icon-mapping-v2', NULL, '', 0, '系统管理员', '1', '2023-08-14 11:13:17.369000', '2023-08-14 11:13:32.454000', '系统管理员', '1', 0);
INSERT INTO `sys_menu` VALUES (34, 32, '统一配置', 'menu', '/config/config', '/config/config', NULL, NULL, NULL, 1, 1, NULL, 'el-icon-grid', NULL, '', 0, '系统管理员', '1', '2023-08-14 11:13:41.480000', '2023-08-14 11:14:17.265000', '系统管理员', '1', 0);
INSERT INTO `sys_menu` VALUES (35, 32, '统一脚本', 'menu', '/config/bean', '/config/bean', NULL, NULL, NULL, 1, 1, NULL, '', NULL, '', 0, '系统管理员', '1', '2023-08-15 16:13:18.060000', '2023-08-15 16:13:32.286000', '系统管理员', '1', 0);
INSERT INTO `sys_menu` VALUES (36, 32, '统一日志', 'menu', '/config/log', '/config/log', NULL, NULL, NULL, 1, 1, NULL, 'el-icon-warning', NULL, '', 0, '系统管理员', '1', '2023-08-17 15:07:45.201000', '2023-08-17 15:08:04.393000', '系统管理员', '1', 0);
INSERT INTO `sys_menu` VALUES (37, 32, '统一链路', 'menu', '/config/trace', '/config/trace', NULL, NULL, NULL, 1, 1, NULL, 'sc-icon-trace', NULL, '', 0, '系统管理员', '1', '2023-08-18 14:28:13.602000', '2023-08-18 14:29:56.839000', '系统管理员', '1', 0);
INSERT INTO `sys_menu` VALUES (38, 3, 'OSS', NULL, '/setting/oss', '/setting/oss', NULL, NULL, NULL, 1, 1, 101, 'el-icon-reading', NULL, '0,3', 0, '系统管理员', NULL, '2024-01-11 16:48:38.869000', '2024-01-16 10:18:58.117000', '系统管理员', NULL, 0);
INSERT INTO `sys_menu` VALUES (47, 6, '图标', 'menu', '/vab/iconselect.vue', '/vab/iconselect.vue', NULL, NULL, NULL, 1, 1, NULL, 'el-icon-apple', NULL, '', 0, '系统管理员', NULL, '2024-01-14 09:53:38.137000', '2024-01-14 09:57:08.318000', '系统管理员', NULL, 0);

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
INSERT INTO `sys_operate_logger` VALUES (24, '01hkpr9t4gwg675axayryz0x1a', '登录', 0, '/admin/api/v2/users/login', '', '172.16.2.226', NULL, NULL, '[\n	{\n		\"password\":\"2768d78e45f5c38cf942d4b4aa829f1b\",\n		\"username\":\"ops\",\n		\"verifyCode\":\"屋斗七果\"\n	}\n]', '', 1, '2024-01-09', NULL, NULL, '2024-01-09 17:19:19.695000', '2024-01-09 17:19:19.798000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (25, '01hkprcxfenat8t8bfn9q00aeg', '登录', 0, '/admin/api/v2/users/login', '', '172.16.2.226', NULL, NULL, '[\n	{\n		\"password\":\"2768d78e45f5c38cf942d4b4aa829f1b\",\n		\"username\":\"ops\",\n		\"verifyCode\":\"wwfRTp\"\n	}\n]', '', 1, '2024-01-09', NULL, NULL, '2024-01-09 17:20:26.566000', '2024-01-09 17:21:01.429000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (26, '01hkrdca6ryzfakz7qvz6fex50', '登录', 0, '/admin/api/v2/users/login', '', '172.16.2.226', NULL, NULL, '[\n	{\n		\"password\":\"2768d78e45f5c38cf942d4b4aa829f1b\",\n		\"username\":\"ops\",\n		\"verifyCode\":\"wwfRTp\"\n	}\n]', '查询', 1, '2024-01-10', NULL, NULL, '2024-01-10 08:46:52.394000', '2024-01-10 08:46:58.603000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (27, '01hkrrgmq197ytbpppp4hxhnsf', '更新用户', 1, '/admin/api/v2/users/update', '', '192.168.30.53', NULL, NULL, '[\n	{\n		\"userId\":16,\n		\"userStatus\":0\n	}\n]', '修改', 1, '2024-01-10', NULL, NULL, '2024-01-10 12:01:30.512000', '2024-01-10 12:01:32.448000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (28, '01hkrrgqbc7625mwfrtpdvj3jw', '更新用户', 0, '/admin/api/v2/users/update', '', '192.168.30.53', NULL, NULL, '[\n	{\n		\"userId\":16,\n		\"userStatus\":1\n	}\n]', '修改', 1, '2024-01-10', NULL, NULL, '2024-01-10 12:01:35.084000', '2024-01-10 12:01:35.095000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (29, '01hkrrgrbve8zc9kt8c62a9h2g', '更新用户', 0, '/admin/api/v2/users/update', '', '192.168.30.53', NULL, NULL, '[\n	{\n		\"userId\":17,\n		\"userStatus\":1\n	}\n]', '修改', 1, '2024-01-10', NULL, NULL, '2024-01-10 12:01:36.123000', '2024-01-10 12:01:36.140000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (30, '01hkrrxtkavdh5zczxb9y7mkaw', '登录', 2, '/admin/api/v2/users/login', '', '192.168.30.53', NULL, NULL, '[\n	{\n		\"password\":\"4cb546d1362e658ee5b4c38d329bcd35\",\n		\"username\":\"test\",\n		\"verifyCode\":\"16\"\n	}\n]', '查询', 1, '2024-01-10', NULL, NULL, '2024-01-10 12:08:44.392000', '2024-01-10 12:08:44.401000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (31, '01hkrs031dgekeaxf797e5n0w8', '登录', 0, '/admin/api/v2/users/login', '', '192.168.30.53', NULL, NULL, '[\n	{\n		\"password\":\"e10adc3949ba59abbe56e057f20f883e\",\n		\"username\":\"admin\",\n		\"verifyCode\":\"12\"\n	}\n]', '查询', 1, '2024-01-10', NULL, NULL, '2024-01-10 12:09:58.573000', '2024-01-10 12:09:58.580000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (32, '01hkrz53tkspzdhgcamzptcm43', '登录', 1, '/admin/api/v2/users/login', '', '192.168.30.53', NULL, NULL, '[\n	{\n		\"password\":\"e10adc3949ba59abbe56e057f20f883e\",\n		\"username\":\"admin\",\n		\"verifyCode\":\"4\"\n	}\n]', '查询', 1, '2024-01-10', NULL, NULL, '2024-01-10 13:57:32.591000', '2024-01-10 13:57:34.888000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (33, '01hks5jnfszvzjsvbfm66015dq', '登录', 3, '/admin/api/v2/users/login', '', '172.16.2.226', NULL, NULL, '[\n	{\n		\"password\":\"e10adc3949ba59abbe56e057f20f883e\",\n		\"username\":\"admin\",\n		\"verifyCode\":\"NZfuSX\"\n	}\n]', '查询', 1, '2024-01-10', NULL, NULL, '2024-01-10 15:49:48.563000', '2024-01-10 15:49:50.274000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (34, '01hkv9b4r1hbgn2v5wp8srs3yv', '登录', 5, '/admin/api/v2/users/login', '', '172.16.2.226', NULL, NULL, '[\n	{\n		\"password\":\"e10adc3949ba59abbe56e057f20f883e\",\n		\"username\":\"admin\",\n		\"verifyCode\":\"团进去放\"\n	}\n]', '查询', 1, '2024-01-11', NULL, NULL, '2024-01-11 11:34:05.213000', '2024-01-11 11:34:06.916000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (35, '01hkv9bce86wbr4sam6savx3b8', '登录', 0, '/admin/api/v2/users/login', '', '172.16.2.226', NULL, NULL, '[\n	{\n		\"password\":\"e10adc3949ba59abbe56e057f20f883e\",\n		\"username\":\"admin\",\n		\"verifyCode\":\"整河倒直\"\n	}\n]', '查询', 1, '2024-01-11', NULL, NULL, '2024-01-11 11:34:14.728000', '2024-01-11 11:34:14.731000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (36, '01hkv9e3g3dd3drm3dzyvm4fex', '登录', 0, '/admin/api/v2/users/login', '', '172.16.2.226', NULL, NULL, '[\n	{\n		\"password\":\"e10adc3949ba59abbe56e057f20f883e\",\n		\"username\":\"admin\",\n		\"verifyCode\":\"-9\"\n	}\n]', '查询', 1, '2024-01-11', NULL, NULL, '2024-01-11 11:35:43.875000', '2024-01-11 11:35:43.880000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (37, '01hkv9gh2r2m68frwm1y1m6yfb', '登录', 0, '/admin/api/v2/users/login', '', '172.16.2.226', NULL, NULL, '[\n	{\n		\"password\":\"e10adc3949ba59abbe56e057f20f883e\",\n		\"username\":\"admin\",\n		\"verifyCode\":\"BamNQd\"\n	}\n]', '查询', 1, '2024-01-11', NULL, NULL, '2024-01-11 11:37:03.319000', '2024-01-11 11:37:03.325000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (38, '01hkv9ndxc5e7zbphaq7etr8f7', '登录', 0, '/admin/api/v2/users/login', '', '172.16.2.226', NULL, NULL, '[\n	{\n		\"password\":\"e10adc3949ba59abbe56e057f20f883e\",\n		\"username\":\"admin\",\n		\"verifyCode\":\"等村女才\"\n	}\n]', '查询', 1, '2024-01-11', NULL, NULL, '2024-01-11 11:39:43.916000', '2024-01-11 11:39:43.922000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (39, '01hkv9qjgqmy60p56rwv02n57x', '登录', 36, '/admin/api/v2/users/login', '', '172.16.2.226', NULL, NULL, '[\n	{\n		\"password\":\"e10adc3949ba59abbe56e057f20f883e\",\n		\"username\":\"admin\",\n		\"verifyCode\":\"TppArq\"\n	}\n]', '查询', 1, '2024-01-11', NULL, NULL, '2024-01-11 11:40:54.167000', '2024-01-11 11:40:54.171000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (40, '01hkv9rjfvc9pd7nmcgrqs2x6z', '登录', 0, '/admin/api/v2/users/login', '', '172.16.2.226', NULL, NULL, '[\n	{\n		\"password\":\"e10adc3949ba59abbe56e057f20f883e\",\n		\"username\":\"admin\",\n		\"verifyCode\":\"12\"\n	}\n]', '查询', 1, '2024-01-11', NULL, NULL, '2024-01-11 11:41:26.907000', '2024-01-11 11:41:26.909000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (41, '01hkv9sd90v3b1cafnvbenpwv0', '登录', 0, '/admin/api/v2/users/login', '', '172.16.2.226', NULL, NULL, '[\n	{\n		\"password\":\"e10adc3949ba59abbe56e057f20f883e\",\n		\"username\":\"admin\",\n		\"verifyCode\":\"志队法船\"\n	}\n]', '查询', 1, '2024-01-11', NULL, NULL, '2024-01-11 11:41:54.335000', '2024-01-11 11:41:54.338000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (42, '01hkv9swdp887vqb4p482w402r', '登录', 0, '/admin/api/v2/users/login', '', '172.16.2.226', NULL, NULL, '[\n	{\n		\"password\":\"e10adc3949ba59abbe56e057f20f883e\",\n		\"username\":\"admin\",\n		\"verifyCode\":\"-6\"\n	}\n]', '查询', 1, '2024-01-11', NULL, NULL, '2024-01-11 11:42:09.846000', '2024-01-11 11:42:09.848000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (43, '01hkvawefgnzz8304759skzjz8', '登录', 0, '/admin/api/v2/users/login', '', '192.168.30.53', NULL, NULL, '[\n	{\n		\"password\":\"123456\",\n		\"username\":\"admin\"\n	}\n]', '查询', 1, '2024-01-11', NULL, NULL, '2024-01-11 12:01:00.694000', '2024-01-11 12:01:02.561000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (44, '01hkvgsrks72ae07yrcpddcwr8', '修改角色数据', 0, '/admin/api/v2/role/getRole', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '修改', 0, '2024-01-11', NULL, NULL, '2024-01-11 13:44:24.318000', '2024-01-11 13:44:25.999000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (45, '01hkvgsysghx0952f3h0f1zvrv', '修改角色数据', 0, '/admin/api/v2/role/getRole', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '修改', 0, '2024-01-11', NULL, NULL, '2024-01-11 13:44:32.304000', '2024-01-11 13:44:32.308000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (46, '01hkvgtgqx3c4qsj130tr4s74c', '修改角色数据', 0, '/admin/api/v2/role/getRole', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '修改', 0, '2024-01-11', NULL, NULL, '2024-01-11 13:44:50.685000', '2024-01-11 13:44:50.688000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (47, '01hkvgv03zbhf2hme0z4k0v59t', '修改角色数据', 0, '/admin/api/v2/role/getRole', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '修改', 0, '2024-01-11', NULL, NULL, '2024-01-11 13:45:06.430000', '2024-01-11 13:45:06.433000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (48, '01hkvgwthv5px5ahp32pernq3b', '修改角色数据', 44, '/admin/api/v2/role/getRole', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '修改', 0, '2024-01-11', NULL, NULL, '2024-01-11 13:46:06.267000', '2024-01-11 13:46:06.270000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (49, '01hkvgxmrfp0ra033exqmj8nyp', '修改角色数据', 4, '/admin/api/v2/role/getRole', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '修改', 1, '2024-01-11', NULL, NULL, '2024-01-11 13:46:31.543000', '2024-01-11 13:46:33.142000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (50, '01hkvh7fq7de87pgcd6hh66ek9', '修改角色', 0, '/admin/api/v2/role/update', '', '172.16.2.226', NULL, NULL, '[\n	{\n		\"roleId\":3,\n		\"roleStatus\":0,\n		\"roleSys\":0,\n		\"updateName\":\"系统管理员\",\n		\"updateTime\":\"2024-01-11 13:51:55\"\n	}\n]', '修改', 1, '2024-01-11', NULL, NULL, '2024-01-11 13:51:55.617000', '2024-01-11 13:51:55.626000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (51, '01hkvh7g5fnxf68a17g6e3gsgw', '修改角色', 0, '/admin/api/v2/role/update', '', '172.16.2.226', NULL, NULL, '[\n	{\n		\"roleId\":3,\n		\"roleStatus\":1,\n		\"roleSys\":0,\n		\"updateName\":\"系统管理员\",\n		\"updateTime\":\"2024-01-11 13:51:56\"\n	}\n]', '修改', 1, '2024-01-11', NULL, NULL, '2024-01-11 13:51:56.079000', '2024-01-11 13:51:56.082000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (52, '01hkvh7tmmnv0cs10a1brzgk3q', '修改角色', 0, '/admin/api/v2/role/update', '', '172.16.2.226', NULL, NULL, '[\n	{\n		\"roleCode\":\"CS\",\n		\"roleId\":3,\n		\"roleName\":\"测试\",\n		\"roleSort\":1,\n		\"roleStatus\":0,\n		\"roleSys\":0,\n		\"updateName\":\"系统管理员\",\n		\"updateTime\":\"2024-01-11 13:52:06\"\n	}\n]', '修改', 1, '2024-01-11', NULL, NULL, '2024-01-11 13:52:06.804000', '2024-01-11 13:52:06.807000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (53, '01hkxjn9reya874xfq2apbdydp', '登录', 10, '/admin/api/v2/users/login', '', '172.16.2.226', NULL, NULL, '[\n	{\n		\"password\":\"e10adc3949ba59abbe56e057f20f883e\",\n		\"username\":\"admin\",\n		\"verifyCode\":\"6efCE8\"\n	}\n]', '查询', 1, '2024-01-12', NULL, NULL, '2024-01-12 08:55:24.179000', '2024-01-12 08:55:25.761000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (54, '01hkxs7kpc4twbq6h91qbgbkmj', '登录', 2, '/admin/api/v2/users/login', '', '172.16.2.226', NULL, NULL, '[\n	{\n		\"password\":\"e10adc3949ba59abbe56e057f20f883e\",\n		\"username\":\"admin\",\n		\"verifyCode\":\"学树照旧\"\n	}\n]', '查询', 1, '2024-01-12', NULL, NULL, '2024-01-12 10:50:15.577000', '2024-01-12 10:50:17.218000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (55, '01hkxsbg3bfrgcs4f514qe79da', '登录', 2, '/admin/api/v2/users/login', '', '172.16.2.226', NULL, NULL, '[\n	{\n		\"password\":\"e10adc3949ba59abbe56e057f20f883e\",\n		\"username\":\"admin\",\n		\"verifyCode\":\"阵未觉河\"\n	}\n]', '查询', 1, '2024-01-12', NULL, NULL, '2024-01-12 10:52:23.111000', '2024-01-12 10:52:24.622000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (56, '01hky1cfvhdb9jahpf7pxt1pac', '删除菜单', 0, '/admin/api/v2/menus/delete', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '删除', 1, '2024-01-12', NULL, NULL, '2024-01-12 13:12:44.478000', '2024-01-12 13:12:45.714000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (57, '01hky1cfvhds56yerazgkeq2ya', '删除菜单', 0, '/admin/api/v2/menus/delete', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '删除', 1, '2024-01-12', NULL, NULL, '2024-01-12 13:12:44.696000', '2024-01-12 13:12:45.714000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (58, '01hky1cfvhcxbjypca76r0x54h', '删除菜单', 0, '/admin/api/v2/menus/delete', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '删除', 1, '2024-01-12', NULL, NULL, '2024-01-12 13:12:45.055000', '2024-01-12 13:12:45.714000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (59, '01hky1cfvh3twdf6444npb2dvf', '删除菜单', 0, '/admin/api/v2/menus/delete', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '删除', 1, '2024-01-12', NULL, NULL, '2024-01-12 13:12:44.878000', '2024-01-12 13:12:45.714000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (60, '01hky1cfvhtvbstj0m810nndtt', '删除菜单', 0, '/admin/api/v2/menus/delete', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '删除', 1, '2024-01-12', NULL, NULL, '2024-01-12 13:12:44.064000', '2024-01-12 13:12:45.714000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (61, '01hky1ch241h4vxy8by1s6dnq9', '删除菜单', 0, '/admin/api/v2/menus/delete', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '删除', 1, '2024-01-12', NULL, NULL, '2024-01-12 13:12:46.915000', '2024-01-12 13:12:46.918000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (62, '01hky1ch6v272zm1c1s8qxnr4a', '删除菜单', 0, '/admin/api/v2/menus/delete', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '删除', 1, '2024-01-12', NULL, NULL, '2024-01-12 13:12:47.067000', '2024-01-12 13:12:47.069000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (63, '01hky1chc8ynt0g5kmx5shh523', '删除菜单', 0, '/admin/api/v2/menus/delete', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '删除', 1, '2024-01-12', NULL, NULL, '2024-01-12 13:12:47.240000', '2024-01-12 13:12:47.242000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (64, '01hky1chhbrm4g83bmcbrt6tvb', '删除菜单', 0, '/admin/api/v2/menus/delete', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '删除', 1, '2024-01-12', NULL, NULL, '2024-01-12 13:12:47.403000', '2024-01-12 13:12:47.405000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (65, '01hky1chpj5y4028e595qj6rx2', '删除菜单', 0, '/admin/api/v2/menus/delete', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '删除', 1, '2024-01-12', NULL, NULL, '2024-01-12 13:12:47.570000', '2024-01-12 13:12:47.572000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (66, '01hky1chvkb55bmy9b15fmsrc1', '删除菜单', 0, '/admin/api/v2/menus/delete', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '删除', 1, '2024-01-12', NULL, NULL, '2024-01-12 13:12:47.731000', '2024-01-12 13:12:47.734000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (67, '01hky1cjac9wvr8ftnfbjpb2a7', '删除菜单', 0, '/admin/api/v2/menus/delete', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '删除', 1, '2024-01-12', NULL, NULL, '2024-01-12 13:12:48.204000', '2024-01-12 13:12:48.205000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (68, '01hky8wt8f05sztbmknjmvdtf1', '修改OSS', 0, '/admin/api/v1/oss/update', '', '172.16.2.226', NULL, NULL, '[\n	{\n		\n	}\n]', '修改', 1, '2024-01-12', NULL, NULL, '2024-01-12 15:23:59.010000', '2024-01-12 15:24:00.672000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (69, '01hky8xj11an90afhsmedewheb', '修改OSS', 3, '/admin/api/v1/oss/update', '', '172.16.2.226', NULL, NULL, '[\n	{\n		\n	}\n]', '修改', 1, '2024-01-12', NULL, NULL, '2024-01-12 15:24:24.993000', '2024-01-12 15:24:25.005000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (70, '01hky8z462h9cj8687e5er5vxh', '修改OSS', 0, '/admin/api/v1/oss/update', '', '172.16.2.226', NULL, NULL, '[\n	{\n		\n	}\n]', '修改', 1, '2024-01-12', NULL, NULL, '2024-01-12 15:25:16.354000', '2024-01-12 15:25:16.357000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (71, '01hky8zbsj70cbe41950d817m9', '修改OSS', 0, '/admin/api/v1/oss/update', '', '172.16.2.226', NULL, NULL, '[\n	{\n		\n	}\n]', '修改', 1, '2024-01-12', NULL, NULL, '2024-01-12 15:25:24.146000', '2024-01-12 15:25:24.148000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (72, '01hky90qckwq47w3htkza8p9ax', '修改OSS', 0, '/admin/api/v1/oss/update', '', '172.16.2.226', NULL, NULL, '[\n	{\n		\n	}\n]', '修改', 1, '2024-01-12', NULL, NULL, '2024-01-12 15:26:08.787000', '2024-01-12 15:26:08.790000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (73, '01hky913dyggfcm9bp56e7ae4m', '修改OSS', 0, '/admin/api/v1/oss/update', '', '172.16.2.226', NULL, NULL, '[\n	{\n		\n	}\n]', '修改', 1, '2024-01-12', NULL, NULL, '2024-01-12 15:26:21.117000', '2024-01-12 15:26:21.120000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (74, '01hky91dwm1cts7a732s58bwzf', '修改OSS', 3, '/admin/api/v1/oss/update', '', '172.16.2.226', NULL, NULL, '[\n	{\n		\n	}\n]', '修改', 1, '2024-01-12', NULL, NULL, '2024-01-12 15:26:31.828000', '2024-01-12 15:26:31.835000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (75, '01hky93nxfcttwk1nhfr578epf', '修改OSS', 0, '/admin/api/v1/oss/update', '', '172.16.2.226', NULL, NULL, '[\n	{\n		\"createName\":\"系统管理员\",\n		\"createTime\":\"2024-01-12 00:00:00\",\n		\"fsBucket\":\"test\",\n		\"fsDomain\":\"http://localhost:18170/admin/api\",\n		\"fsId\":12,\n		\"fsName\":\"测试\",\n		\"fsStatus\":1,\n		\"fsType\":\"LOCAL\",\n		\"tenantId\":0,\n		\"updateName\":\"系统管理员\",\n		\"updateTime\":\"2024-01-12 15:27:43\"\n	}\n]', '修改', 1, '2024-01-12', NULL, NULL, '2024-01-12 15:27:44.016000', '2024-01-12 15:27:45.629000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (76, '01hky94vw6m26pw11fae4rs4ks', '修改OSS', 0, '/admin/api/v1/oss/update', '', '172.16.2.226', NULL, NULL, '[\n	{\n		\"createName\":\"系统管理员\",\n		\"createTime\":\"2024-01-12 00:00:00\",\n		\"fsBucket\":\"test\",\n		\"fsDomain\":\"http://localhost:18170/admin/api\",\n		\"fsId\":12,\n		\"fsName\":\"测试\",\n		\"fsStatus\":1,\n		\"fsType\":\"LOCAL\",\n		\"tenantId\":0,\n		\"updateName\":\"系统管理员\",\n		\"updateTime\":\"2024-01-12 15:28:24\"\n	}\n]', '修改', 1, '2024-01-12', NULL, NULL, '2024-01-12 15:28:24.453000', '2024-01-12 15:28:24.456000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (77, '01hky9yw0pjyzkfycp354tg74z', '删除OSS', 0, '/admin/api/v1/oss/delete', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '删除', 1, '2024-01-12', NULL, NULL, '2024-01-12 15:42:36.566000', '2024-01-12 15:42:36.568000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (78, '01hky9yyp2t1fmr0t6bhjzfq2n', '删除OSS', 0, '/admin/api/v1/oss/delete', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '删除', 1, '2024-01-12', NULL, NULL, '2024-01-12 15:42:39.298000', '2024-01-12 15:42:39.303000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (79, '01hkya0aawrsvxgedx79as2j6m', '删除OSS', 2, '/admin/api/v1/oss/delete', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '删除', 1, '2024-01-12', NULL, NULL, '2024-01-12 15:43:22.444000', '2024-01-12 15:43:24.067000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (80, '01hkya1700z37cf8gnkwbhs6x7', '删除OSS', 0, '/admin/api/v1/oss/delete', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '删除', 1, '2024-01-12', NULL, NULL, '2024-01-12 15:43:53.344000', '2024-01-12 15:43:53.347000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (81, '01hkya1nbw08sjy3gbchvn3rze', '删除OSS', 6, '/admin/api/v1/oss/delete', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '删除', 1, '2024-01-12', NULL, NULL, '2024-01-12 15:44:08.060000', '2024-01-12 15:44:08.067000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (82, '01hkya5jy31gch2nxg01mywax4', '删除OSS', 0, '/admin/api/v1/oss/delete', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '删除', 1, '2024-01-12', NULL, NULL, '2024-01-12 15:46:16.643000', '2024-01-12 15:46:16.646000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (83, '01hkya5wcy0tysen4wdyf5zkf3', '删除OSS', 0, '/admin/api/v1/oss/delete', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '删除', 1, '2024-01-12', NULL, NULL, '2024-01-12 15:46:26.334000', '2024-01-12 15:46:26.338000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (84, '01hkya762njmyb89b5z843z530', '删除OSS', 0, '/admin/api/v1/oss/delete', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '删除', 1, '2024-01-12', NULL, NULL, '2024-01-12 15:47:09.013000', '2024-01-12 15:47:09.014000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (85, '01hkybd87z4ashjfj5hhwnbcaq', '删除OSS', 0, '/admin/api/v1/oss/delete', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '删除', 1, '2024-01-12', NULL, NULL, '2024-01-12 16:07:54.786000', '2024-01-12 16:07:56.447000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (86, '01hkybdq4rg4yxnynd5788fhjx', '删除OSS', 0, '/admin/api/v1/oss/delete', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '删除', 1, '2024-01-12', NULL, NULL, '2024-01-12 16:08:11.672000', '2024-01-12 16:08:11.684000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (87, '01hkybfyzjmvw1jxar7rc1595k', '删除OSS', 0, '/admin/api/v1/oss/delete', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '删除', 1, '2024-01-12', NULL, NULL, '2024-01-12 16:09:25.234000', '2024-01-12 16:09:25.237000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (88, '01hkybg2j2whceygmzf08pv8sx', '删除OSS', 0, '/admin/api/v1/oss/delete', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '删除', 1, '2024-01-12', NULL, NULL, '2024-01-12 16:09:28.898000', '2024-01-12 16:09:28.901000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (89, '01hkybh4xmhrbdgw6jwr0hqgdr', '删除OSS', 0, '/admin/api/v1/oss/delete', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '删除', 1, '2024-01-12', NULL, NULL, '2024-01-12 16:10:04.084000', '2024-01-12 16:10:04.085000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (90, '01hkybme60tammz8eqtz6rqxp5', '删除OSS', 0, '/admin/api/v1/oss/delete', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '删除', 1, '2024-01-12', NULL, NULL, '2024-01-12 16:11:51.871000', '2024-01-12 16:11:51.875000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (91, '01hkyc0c9eperkpvywb884f2aw', '删除OSS', 0, '/admin/api/v1/oss/delete', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '删除', 1, '2024-01-12', NULL, NULL, '2024-01-12 16:18:23.150000', '2024-01-12 16:18:23.151000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (92, '01hkyc0egsr4r7ahcm36jykr5n', '删除OSS', 0, '/admin/api/v1/oss/delete', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '删除', 1, '2024-01-12', NULL, NULL, '2024-01-12 16:18:25.433000', '2024-01-12 16:18:25.435000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (93, '01hkyc0rxyf8rq5vbgytrdrvmg', '删除OSS', 0, '/admin/api/v1/oss/delete', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '删除', 1, '2024-01-12', NULL, NULL, '2024-01-12 16:18:36.094000', '2024-01-12 16:18:36.096000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (94, '01hkyc2kjxhhez7rjdvp3mehan', '删除OSS', 0, '/admin/api/v1/oss/delete', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '删除', 1, '2024-01-12', NULL, NULL, '2024-01-12 16:19:36.157000', '2024-01-12 16:19:36.159000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (95, '01hkyc2mph07zqjwkwxx9r3gd5', '删除OSS', 0, '/admin/api/v1/oss/delete', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '删除', 1, '2024-01-12', NULL, NULL, '2024-01-12 16:19:37.297000', '2024-01-12 16:19:37.298000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (96, '01hkyc2p4tph1e0vjn8056bq23', '删除OSS', 0, '/admin/api/v1/oss/delete', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '删除', 1, '2024-01-12', NULL, NULL, '2024-01-12 16:19:38.778000', '2024-01-12 16:19:38.779000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (97, '01hkyc33wwpjmn6jjpbag7hy8v', '删除OSS', 0, '/admin/api/v1/oss/delete', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '删除', 1, '2024-01-12', NULL, NULL, '2024-01-12 16:19:52.860000', '2024-01-12 16:19:52.861000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (98, '01hkyc34q2xryafcsstpb6ssw5', '删除OSS', 0, '/admin/api/v1/oss/delete', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '删除', 1, '2024-01-12', NULL, NULL, '2024-01-12 16:19:53.698000', '2024-01-12 16:19:53.700000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (99, '01hkyc34wxhqnsp1tpangjz5tz', '删除OSS', 0, '/admin/api/v1/oss/delete', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '删除', 1, '2024-01-12', NULL, NULL, '2024-01-12 16:19:53.885000', '2024-01-12 16:19:53.887000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (100, '01hkyc352108cw20y685afhmyw', '删除OSS', 0, '/admin/api/v1/oss/delete', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '删除', 1, '2024-01-12', NULL, NULL, '2024-01-12 16:19:54.049000', '2024-01-12 16:19:54.052000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (101, '01hkyc3vf6xwdqnkzv267h6gpt', '删除OSS', 0, '/admin/api/v1/oss/delete', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '删除', 1, '2024-01-12', NULL, NULL, '2024-01-12 16:20:16.998000', '2024-01-12 16:20:16.999000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (102, '01hkyc5wy5ydfgq6jz1ynpt78e', '删除OSS', 0, '/admin/api/v1/oss/delete', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '删除', 1, '2024-01-12', NULL, NULL, '2024-01-12 16:21:24.037000', '2024-01-12 16:21:24.039000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (103, '01hkyc8mjrrknvnwdf7j1ywe2d', '删除OSS', 0, '/admin/api/v1/oss/delete', '', '172.16.2.226', NULL, NULL, '[\n	\n]', '删除', 1, '2024-01-12', NULL, NULL, '2024-01-12 16:22:53.784000', '2024-01-12 16:22:53.788000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (104, '01hkycawec7v58eydzvm4hgr2y', '修改OSS', 0, '/admin/api/v1/oss/update', '', '172.16.2.226', NULL, NULL, '[\n	{\n		\"createName\":\"系统管理员\",\n		\"createTime\":\"2024-01-12 00:00:00\",\n		\"fsBucket\":\"test\",\n		\"fsDomain\":\"http://localhost:18170/admin/api\",\n		\"fsId\":1,\n		\"fsName\":\"测试\",\n		\"fsStatus\":1,\n		\"fsType\":\"LOCAL\",\n		\"tenantId\":0,\n		\"updateName\":\"系统管理员\",\n		\"updateTime\":\"2024-01-12 16:24:07\"\n	}\n]', '修改', 1, '2024-01-12', NULL, NULL, '2024-01-12 16:24:07.360000', '2024-01-12 16:24:07.373000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (105, '01hkycgnr3kmj31h3zef6zcp3z', '修改OSS', 0, '/admin/api/v1/oss/update', '', '172.16.2.226', NULL, NULL, '[\n	{\n		\"createName\":\"系统管理员\",\n		\"createTime\":\"2024-01-12 00:00:00\",\n		\"fsBucket\":\"test\",\n		\"fsDomain\":\"http://localhost:18170/admin/api\",\n		\"fsId\":1,\n		\"fsName\":\"测试\",\n		\"fsStatus\":1,\n		\"fsStoragePath\":\"/\",\n		\"fsType\":\"LOCAL\",\n		\"tenantId\":0,\n		\"updateName\":\"系统管理员\",\n		\"updateTime\":\"2024-01-12 16:27:17\"\n	}\n]', '修改', 1, '2024-01-12', NULL, NULL, '2024-01-12 16:27:17.122000', '2024-01-12 16:27:17.125000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (106, '01hkyy8w3me44dgjxn1dfpg83e', '登录', 3, '/admin/api/v2/users/login', '', '172.16.2.100', NULL, NULL, '[\n	{\n		\"password\":\"e10adc3949ba59abbe56e057f20f883e\",\n		\"username\":\"admin\",\n		\"verifyCode\":\"-2\"\n	}\n]', '查询', 1, '2024-01-12', NULL, NULL, '2024-01-12 21:37:33.203000', '2024-01-12 21:37:36.306000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (107, '01hkyyeh2n3mfsf86ta05es91k', '修改OSS', 0, '/admin/api/v1/oss/update', '', '172.16.2.100', NULL, NULL, '[\n	{\n		\"createName\":\"系统管理员\",\n		\"createTime\":\"2024-01-12 00:00:00\",\n		\"fsBucket\":\"test\",\n		\"fsDomain\":\"http://localhost:18170/admin/api\",\n		\"fsId\":1,\n		\"fsName\":\"测试\",\n		\"fsStatus\":1,\n		\"fsStoragePath\":\"Z://\",\n		\"fsType\":\"LOCAL\",\n		\"tenantId\":0,\n		\"updateName\":\"系统管理员\",\n		\"updateTime\":\"2024-01-12 00:00:00\"\n	}\n]', '修改', 1, '2024-01-12', NULL, NULL, '2024-01-12 21:40:41.160000', '2024-01-12 21:40:41.181000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (108, '01hkz02eaa3n4m276h3dadzecd', '删除OSS', 0, '/admin/api/v1/oss/delete', '', '192.168.0.108', NULL, NULL, '[\n	\n]', '删除', 1, '2024-01-12', NULL, NULL, '2024-01-12 22:09:02.282000', '2024-01-12 22:09:02.410000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (109, '01hkz02e9s02t7frpenhhbcybk', '删除OSS', 0, '/admin/api/v1/oss/delete', '', '192.168.0.108', NULL, NULL, '[\n	\n]', '删除', 1, '2024-01-12', NULL, NULL, '2024-01-12 22:09:02.262000', '2024-01-12 22:09:02.410000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (110, '01hm0vhk3pwv31dy375j7wvcwf', '登录', 2, '/admin/api/v2/users/login', '', '172.16.2.100', NULL, NULL, '[\n	{\n		\"password\":\"e10adc3949ba59abbe56e057f20f883e\",\n		\"username\":\"admin\",\n		\"verifyCode\":\"办名结站\"\n	}\n]', '查询', 1, '2024-01-13', NULL, NULL, '2024-01-13 15:28:22.766000', '2024-01-13 15:28:24.910000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (111, '01hm2tfmh0jts7c4069rfpmxdp', '登录', 2, '/admin/api/v2/users/login', '', '172.16.2.100', NULL, NULL, '[\n	{\n		\"password\":\"e10adc3949ba59abbe56e057f20f883e\",\n		\"username\":\"admin\",\n		\"verifyCode\":\"QhKJgJ\"\n	}\n]', '查询', 1, '2024-01-14', NULL, NULL, '2024-01-14 09:48:19.105000', '2024-01-14 09:48:21.104000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (112, '01hm5fya8vskg2f19zk65psk82', '修改OSS', 0, '/admin/api/v1/oss/update', '', '172.16.2.226', NULL, NULL, '[\n	{\n		\"createName\":\"系统管理员\",\n		\"createTime\":\"2024-01-12 00:00:00\",\n		\"fsBucket\":\"test\",\n		\"fsDomain\":\"http://localhost:18170/admin/api\",\n		\"fsEndpoint\":\"Z://\",\n		\"fsId\":13,\n		\"fsName\":\"测试\",\n		\"fsStatus\":1,\n		\"fsStoragePath\":\"Z://\",\n		\"fsType\":\"LOCAL\",\n		\"tenantId\":0,\n		\"updateName\":\"系统管理员\",\n		\"updateTime\":\"2024-01-15 10:41:49\"\n	}\n]', '修改', 1, '2024-01-15', NULL, NULL, '2024-01-15 10:41:49.084000', '2024-01-15 10:41:50.895000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (113, '01hm6hevtac4qnmqq6pwqzv5nr', '登录', 3, '/admin/api/v2/users/login', '', '172.16.2.100', NULL, NULL, '[\n  {\n    \"username\": \"admin\",\n    \"password\": \"e10adc3949ba59abbe56e057f20f883e\",\n    \"verifyCode\": \"友越主史\"\n  }\n]', '查询', 1, '2024-01-15', NULL, NULL, '2024-01-15 20:27:33.995000', '2024-01-15 20:27:36.206000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (114, '01hm6hmt26amjm353ttvgmnmzn', '登录', 5, '/admin/api/v2/users/login', '', '172.16.2.100', NULL, NULL, '[\n  {\n    \"username\": \"admin\",\n    \"password\": \"e10adc3949ba59abbe56e057f20f883e\",\n    \"verifyCode\": \"32\"\n  }\n]', '查询', 1, '2024-01-15', NULL, NULL, '2024-01-15 20:30:49.304000', '2024-01-15 20:30:51.022000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (115, '01hm6mntrf27z5j112ah8v4t5c', '修改OSS', 0, '/admin/api/v1/oss/update', '', '172.16.2.100', NULL, NULL, '[\n  {\n    \"fsId\": 13,\n    \"fsName\": \"测试\",\n    \"fsBucket\": \"test\",\n    \"fsType\": \"LOCAL\",\n    \"fsFilter\": \"GRAY\",\n    \"fsEndpoint\": \"Z://\",\n    \"fsDomain\": \"http://localhost:18170/admin/api\",\n    \"fsStoragePath\": \"Z://\",\n    \"fsStatus\": 1,\n    \"tenantId\": 0,\n    \"createName\": \"系统管理员\",\n    \"createTime\": {\n      \"date\": {\n        \"year\": 2024,\n        \"month\": 1,\n        \"day\": 12\n      },\n      \"time\": {\n        \"hour\": 0,\n        \"minute\": 0,\n        \"second\": 0,\n        \"nano\": 0\n      }\n    },\n    \"updateTime\": {\n      \"date\": {\n        \"year\": 2024,\n        \"month\": 1,\n        \"day\": 15\n      },\n      \"time\": {\n        \"hour\": 0,\n        \"minute\": 0,\n        \"second\": 0,\n        \"nano\": 0\n      }\n    },\n    \"updateName\": \"系统管理员\"\n  }\n]', '修改', 1, '2024-01-15', NULL, NULL, '2024-01-15 21:23:50.152000', '2024-01-15 21:23:50.201000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (116, '01hm6mv2x3cx23kjx3q5tkgwg9', '修改OSS', 56, '/admin/api/v1/oss/update', '', '172.16.2.100', NULL, NULL, '[\n  {\n    \"fsId\": 13,\n    \"fsName\": \"测试\",\n    \"fsBucket\": \"test\",\n    \"fsType\": \"LOCAL\",\n    \"fsFilter\": \"GRAY\",\n    \"fsEndpoint\": \"Z://\",\n    \"fsDomain\": \"http://localhost:18170/admin/api\",\n    \"fsStoragePath\": \"Z://\",\n    \"fsStatus\": 1,\n    \"tenantId\": 0,\n    \"createName\": \"系统管理员\",\n    \"createTime\": {\n      \"date\": {\n        \"year\": 2024,\n        \"month\": 1,\n        \"day\": 12\n      },\n      \"time\": {\n        \"hour\": 0,\n        \"minute\": 0,\n        \"second\": 0,\n        \"nano\": 0\n      }\n    },\n    \"updateTime\": {\n      \"date\": {\n        \"year\": 2024,\n        \"month\": 1,\n        \"day\": 15\n      },\n      \"time\": {\n        \"hour\": 0,\n        \"minute\": 0,\n        \"second\": 0,\n        \"nano\": 0\n      }\n    },\n    \"updateName\": \"系统管理员\"\n  }\n]', '修改', 1, '2024-01-15', NULL, NULL, '2024-01-15 21:26:42.338000', '2024-01-15 21:26:42.351000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (117, '01hm6mzghwv809es59t2f2rsye', '修改OSS', 142, '/admin/api/v1/oss/update', '', '172.16.2.100', NULL, NULL, '[\n  {\n    \"fsId\": 13,\n    \"fsName\": \"测试\",\n    \"fsBucket\": \"test\",\n    \"fsType\": \"LOCAL\",\n    \"fsFilter\": \"GRAY\",\n    \"fsEndpoint\": \"Z://\",\n    \"fsDomain\": \"http://localhost:18170/admin/api\",\n    \"fsStoragePath\": \"Z://\",\n    \"fsStatus\": 1,\n    \"tenantId\": 0,\n    \"createName\": \"系统管理员\",\n    \"createTime\": {\n      \"date\": {\n        \"year\": 2024,\n        \"month\": 1,\n        \"day\": 12\n      },\n      \"time\": {\n        \"hour\": 0,\n        \"minute\": 0,\n        \"second\": 0,\n        \"nano\": 0\n      }\n    },\n    \"updateTime\": {\n      \"date\": {\n        \"year\": 2024,\n        \"month\": 1,\n        \"day\": 15\n      },\n      \"time\": {\n        \"hour\": 0,\n        \"minute\": 0,\n        \"second\": 0,\n        \"nano\": 0\n      }\n    },\n    \"updateName\": \"系统管理员\"\n  }\n]', '修改', 1, '2024-01-15', NULL, NULL, '2024-01-15 21:29:07.388000', '2024-01-15 21:29:07.394000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (118, '01hm6n1wkv76491fa66sz0kdmh', '修改OSS', 0, '/admin/api/v1/oss/update', '', '172.16.2.100', NULL, NULL, '[\n  {\n    \"fsId\": 13,\n    \"fsName\": \"测试\",\n    \"fsBucket\": \"test\",\n    \"fsType\": \"LOCAL\",\n    \"fsFilter\": \"GRAY\",\n    \"fsEndpoint\": \"Z://\",\n    \"fsDomain\": \"http://localhost:18170/admin/api\",\n    \"fsStoragePath\": \"Z://\",\n    \"fsStatus\": 1,\n    \"tenantId\": 0,\n    \"createName\": \"系统管理员\",\n    \"createTime\": {\n      \"date\": {\n        \"year\": 2024,\n        \"month\": 1,\n        \"day\": 12\n      },\n      \"time\": {\n        \"hour\": 0,\n        \"minute\": 0,\n        \"second\": 0,\n        \"nano\": 0\n      }\n    },\n    \"updateTime\": {\n      \"date\": {\n        \"year\": 2024,\n        \"month\": 1,\n        \"day\": 15\n      },\n      \"time\": {\n        \"hour\": 0,\n        \"minute\": 0,\n        \"second\": 0,\n        \"nano\": 0\n      }\n    },\n    \"updateName\": \"系统管理员\"\n  }\n]', '修改', 1, '2024-01-15', NULL, NULL, '2024-01-15 21:30:23.566000', '2024-01-15 21:30:25.309000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (119, '01hm6n3phcr7eeea2537y8xras', '修改OSS', 0, '/admin/api/v1/oss/update', '', '172.16.2.100', NULL, NULL, '[\n  {\n    \"fsId\": 13,\n    \"fsName\": \"测试\",\n    \"fsBucket\": \"test\",\n    \"fsType\": \"LOCAL\",\n    \"fsFilter\": \"BIN\",\n    \"fsEndpoint\": \"Z://\",\n    \"fsDomain\": \"http://localhost:18170/admin/api\",\n    \"fsStoragePath\": \"Z://\",\n    \"fsStatus\": 1,\n    \"tenantId\": 0,\n    \"createName\": \"系统管理员\",\n    \"createTime\": {\n      \"date\": {\n        \"year\": 2024,\n        \"month\": 1,\n        \"day\": 12\n      },\n      \"time\": {\n        \"hour\": 0,\n        \"minute\": 0,\n        \"second\": 0,\n        \"nano\": 0\n      }\n    },\n    \"updateTime\": {\n      \"date\": {\n        \"year\": 2024,\n        \"month\": 1,\n        \"day\": 15\n      },\n      \"time\": {\n        \"hour\": 0,\n        \"minute\": 0,\n        \"second\": 0,\n        \"nano\": 0\n      }\n    },\n    \"updateName\": \"系统管理员\"\n  }\n]', '修改', 1, '2024-01-15', NULL, NULL, '2024-01-15 21:31:24.587000', '2024-01-15 21:31:24.612000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (120, '01hm6n3ywyjm8fve14xqrqdcra', '修改OSS', 0, '/admin/api/v1/oss/update', '', '172.16.2.100', NULL, NULL, '[\n  {\n    \"fsId\": 13,\n    \"fsName\": \"测试\",\n    \"fsBucket\": \"test\",\n    \"fsType\": \"LOCAL\",\n    \"fsFilter\": \"NEGATIVE\",\n    \"fsEndpoint\": \"Z://\",\n    \"fsDomain\": \"http://localhost:18170/admin/api\",\n    \"fsStoragePath\": \"Z://\",\n    \"fsStatus\": 1,\n    \"tenantId\": 0,\n    \"createName\": \"系统管理员\",\n    \"createTime\": {\n      \"date\": {\n        \"year\": 2024,\n        \"month\": 1,\n        \"day\": 12\n      },\n      \"time\": {\n        \"hour\": 0,\n        \"minute\": 0,\n        \"second\": 0,\n        \"nano\": 0\n      }\n    },\n    \"updateTime\": {\n      \"date\": {\n        \"year\": 2024,\n        \"month\": 1,\n        \"day\": 15\n      },\n      \"time\": {\n        \"hour\": 0,\n        \"minute\": 0,\n        \"second\": 0,\n        \"nano\": 0\n      }\n    },\n    \"updateName\": \"系统管理员\"\n  }\n]', '修改', 1, '2024-01-15', NULL, NULL, '2024-01-15 21:31:33.150000', '2024-01-15 21:31:33.158000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (121, '01hm6n450h9pz0jep9mqy9w92q', '修改OSS', 0, '/admin/api/v1/oss/update', '', '172.16.2.100', NULL, NULL, '[\n  {\n    \"fsId\": 13,\n    \"fsName\": \"测试\",\n    \"fsBucket\": \"test\",\n    \"fsType\": \"LOCAL\",\n    \"fsFilter\": \"USM\",\n    \"fsEndpoint\": \"Z://\",\n    \"fsDomain\": \"http://localhost:18170/admin/api\",\n    \"fsStoragePath\": \"Z://\",\n    \"fsStatus\": 1,\n    \"tenantId\": 0,\n    \"createName\": \"系统管理员\",\n    \"createTime\": {\n      \"date\": {\n        \"year\": 2024,\n        \"month\": 1,\n        \"day\": 12\n      },\n      \"time\": {\n        \"hour\": 0,\n        \"minute\": 0,\n        \"second\": 0,\n        \"nano\": 0\n      }\n    },\n    \"updateTime\": {\n      \"date\": {\n        \"year\": 2024,\n        \"month\": 1,\n        \"day\": 15\n      },\n      \"time\": {\n        \"hour\": 0,\n        \"minute\": 0,\n        \"second\": 0,\n        \"nano\": 0\n      }\n    },\n    \"updateName\": \"系统管理员\"\n  }\n]', '修改', 1, '2024-01-15', NULL, NULL, '2024-01-15 21:31:39.409000', '2024-01-15 21:31:39.416000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (122, '01hm6n4d0ysq6bvb4a3nvpp0yc', '修改OSS', 0, '/admin/api/v1/oss/update', '', '172.16.2.100', NULL, NULL, '[\n  {\n    \"fsId\": 13,\n    \"fsName\": \"测试\",\n    \"fsBucket\": \"test\",\n    \"fsType\": \"LOCAL\",\n    \"fsFilter\": \"SEPIATONE\",\n    \"fsEndpoint\": \"Z://\",\n    \"fsDomain\": \"http://localhost:18170/admin/api\",\n    \"fsStoragePath\": \"Z://\",\n    \"fsStatus\": 1,\n    \"tenantId\": 0,\n    \"createName\": \"系统管理员\",\n    \"createTime\": {\n      \"date\": {\n        \"year\": 2024,\n        \"month\": 1,\n        \"day\": 12\n      },\n      \"time\": {\n        \"hour\": 0,\n        \"minute\": 0,\n        \"second\": 0,\n        \"nano\": 0\n      }\n    },\n    \"updateTime\": {\n      \"date\": {\n        \"year\": 2024,\n        \"month\": 1,\n        \"day\": 15\n      },\n      \"time\": {\n        \"hour\": 0,\n        \"minute\": 0,\n        \"second\": 0,\n        \"nano\": 0\n      }\n    },\n    \"updateName\": \"系统管理员\"\n  }\n]', '修改', 1, '2024-01-15', NULL, NULL, '2024-01-15 21:31:47.614000', '2024-01-15 21:31:47.619000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (123, '01hm6n4gyxzp55g85g35q6jpk4', '修改OSS', 0, '/admin/api/v1/oss/update', '', '172.16.2.100', NULL, NULL, '[\n  {\n    \"fsId\": 13,\n    \"fsName\": \"测试\",\n    \"fsBucket\": \"test\",\n    \"fsType\": \"LOCAL\",\n    \"fsFilter\": \"FINDEDGE\",\n    \"fsEndpoint\": \"Z://\",\n    \"fsDomain\": \"http://localhost:18170/admin/api\",\n    \"fsStoragePath\": \"Z://\",\n    \"fsStatus\": 1,\n    \"tenantId\": 0,\n    \"createName\": \"系统管理员\",\n    \"createTime\": {\n      \"date\": {\n        \"year\": 2024,\n        \"month\": 1,\n        \"day\": 12\n      },\n      \"time\": {\n        \"hour\": 0,\n        \"minute\": 0,\n        \"second\": 0,\n        \"nano\": 0\n      }\n    },\n    \"updateTime\": {\n      \"date\": {\n        \"year\": 2024,\n        \"month\": 1,\n        \"day\": 15\n      },\n      \"time\": {\n        \"hour\": 0,\n        \"minute\": 0,\n        \"second\": 0,\n        \"nano\": 0\n      }\n    },\n    \"updateName\": \"系统管理员\"\n  }\n]', '修改', 1, '2024-01-15', NULL, NULL, '2024-01-15 21:31:51.644000', '2024-01-15 21:31:51.662000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (124, '01hm6n4p8x58v4n3c7522jkpxx', '修改OSS', 0, '/admin/api/v1/oss/update', '', '172.16.2.100', NULL, NULL, '[\n  {\n    \"fsId\": 13,\n    \"fsName\": \"测试\",\n    \"fsBucket\": \"test\",\n    \"fsType\": \"LOCAL\",\n    \"fsFilter\": \"ANTIALIASING\",\n    \"fsEndpoint\": \"Z://\",\n    \"fsDomain\": \"http://localhost:18170/admin/api\",\n    \"fsStoragePath\": \"Z://\",\n    \"fsStatus\": 1,\n    \"tenantId\": 0,\n    \"createName\": \"系统管理员\",\n    \"createTime\": {\n      \"date\": {\n        \"year\": 2024,\n        \"month\": 1,\n        \"day\": 12\n      },\n      \"time\": {\n        \"hour\": 0,\n        \"minute\": 0,\n        \"second\": 0,\n        \"nano\": 0\n      }\n    },\n    \"updateTime\": {\n      \"date\": {\n        \"year\": 2024,\n        \"month\": 1,\n        \"day\": 15\n      },\n      \"time\": {\n        \"hour\": 0,\n        \"minute\": 0,\n        \"second\": 0,\n        \"nano\": 0\n      }\n    },\n    \"updateName\": \"系统管理员\"\n  }\n]', '修改', 1, '2024-01-15', NULL, NULL, '2024-01-15 21:31:57.085000', '2024-01-15 21:31:57.089000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (125, '01hm6n4sx27vpjeshaxdr9tv7c', '修改OSS', 0, '/admin/api/v1/oss/update', '', '172.16.2.100', NULL, NULL, '[\n  {\n    \"fsId\": 13,\n    \"fsName\": \"测试\",\n    \"fsBucket\": \"test\",\n    \"fsType\": \"LOCAL\",\n    \"fsFilter\": \"SUDOKU\",\n    \"fsEndpoint\": \"Z://\",\n    \"fsDomain\": \"http://localhost:18170/admin/api\",\n    \"fsStoragePath\": \"Z://\",\n    \"fsStatus\": 1,\n    \"tenantId\": 0,\n    \"createName\": \"系统管理员\",\n    \"createTime\": {\n      \"date\": {\n        \"year\": 2024,\n        \"month\": 1,\n        \"day\": 12\n      },\n      \"time\": {\n        \"hour\": 0,\n        \"minute\": 0,\n        \"second\": 0,\n        \"nano\": 0\n      }\n    },\n    \"updateTime\": {\n      \"date\": {\n        \"year\": 2024,\n        \"month\": 1,\n        \"day\": 15\n      },\n      \"time\": {\n        \"hour\": 0,\n        \"minute\": 0,\n        \"second\": 0,\n        \"nano\": 0\n      }\n    },\n    \"updateName\": \"系统管理员\"\n  }\n]', '修改', 1, '2024-01-15', NULL, NULL, '2024-01-15 21:32:00.802000', '2024-01-15 21:32:00.814000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (126, '01hm6n5cv2qqxerhf9k7pfrxvh', '修改OSS', 0, '/admin/api/v1/oss/update', '', '172.16.2.100', NULL, NULL, '[\n  {\n    \"fsId\": 13,\n    \"fsName\": \"测试\",\n    \"fsBucket\": \"test\",\n    \"fsType\": \"LOCAL\",\n    \"fsFilter\": \"FINDEDGE\",\n    \"fsEndpoint\": \"Z://\",\n    \"fsDomain\": \"http://localhost:18170/admin/api\",\n    \"fsStoragePath\": \"Z://\",\n    \"fsStatus\": 1,\n    \"tenantId\": 0,\n    \"createName\": \"系统管理员\",\n    \"createTime\": {\n      \"date\": {\n        \"year\": 2024,\n        \"month\": 1,\n        \"day\": 12\n      },\n      \"time\": {\n        \"hour\": 0,\n        \"minute\": 0,\n        \"second\": 0,\n        \"nano\": 0\n      }\n    },\n    \"updateTime\": {\n      \"date\": {\n        \"year\": 2024,\n        \"month\": 1,\n        \"day\": 15\n      },\n      \"time\": {\n        \"hour\": 0,\n        \"minute\": 0,\n        \"second\": 0,\n        \"nano\": 0\n      }\n    },\n    \"updateName\": \"系统管理员\"\n  }\n]', '修改', 1, '2024-01-15', NULL, NULL, '2024-01-15 21:32:20.193000', '2024-01-15 21:32:20.204000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (127, '01hm80wf8kh3w9rdeq7tk23rz1', '登录', 2, '/admin/api/v2/users/login', '', '172.16.2.226', NULL, NULL, '[\n  {\n    \"username\": \"admin\",\n    \"password\": \"e10adc3949ba59abbe56e057f20f883e\",\n    \"verifyCode\": \"y4ChJ4\"\n  }\n]', '查询', 1, '2024-01-16', NULL, NULL, '2024-01-16 10:16:23.463000', '2024-01-16 10:16:25.136000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (128, '01hm80wtyf31rt45ajqmgb44xy', '登录', 2, '/admin/api/v2/users/login', '', '172.16.2.226', NULL, NULL, '[\n  {\n    \"username\": \"admin\",\n    \"password\": \"e10adc3949ba59abbe56e057f20f883e\",\n    \"verifyCode\": \"阳听更块\"\n  }\n]', '查询', 1, '2024-01-16', NULL, NULL, '2024-01-16 10:16:37.070000', '2024-01-16 10:16:37.080000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (129, '01hm80z2bnwjze4nrwkjnhrc0r', '登录', 2, '/admin/api/v2/users/login', '', '172.16.2.226', NULL, NULL, '[\n  {\n    \"username\": \"admin\",\n    \"password\": \"e10adc3949ba59abbe56e057f20f883e\",\n    \"verifyCode\": \"准得切您\"\n  }\n]', '查询', 1, '2024-01-16', NULL, NULL, '2024-01-16 10:17:48.720000', '2024-01-16 10:17:50.234000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (130, '01hm85cgdj4zr0hvrmwnrz845k', '修改OSS', 0, '/admin/api/v1/oss/update', '', '172.16.2.226', NULL, NULL, '[\n  {\n    \"fsId\": 13,\n    \"fsName\": \"测试\",\n    \"fsBucket\": \"test\",\n    \"fsType\": \"LOCAL\",\n    \"fsFilter\": \"\",\n    \"fsEndpoint\": \"Z://\",\n    \"fsDomain\": \"http://localhost:18170/admin/api\",\n    \"fsStoragePath\": \"Z://\",\n    \"fsStatus\": 1,\n    \"tenantId\": 0,\n    \"createName\": \"系统管理员\",\n    \"createTime\": {\n      \"date\": {\n        \"year\": 2024,\n        \"month\": 1,\n        \"day\": 12\n      },\n      \"time\": {\n        \"hour\": 0,\n        \"minute\": 0,\n        \"second\": 0,\n        \"nano\": 0\n      }\n    },\n    \"updateTime\": {\n      \"date\": {\n        \"year\": 2024,\n        \"month\": 1,\n        \"day\": 15\n      },\n      \"time\": {\n        \"hour\": 0,\n        \"minute\": 0,\n        \"second\": 0,\n        \"nano\": 0\n      }\n    },\n    \"updateName\": \"系统管理员\"\n  }\n]', '修改', 1, '2024-01-16', NULL, NULL, '2024-01-16 11:35:04.878000', '2024-01-16 11:35:04.908000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (131, '01hm8b4g4d1fgh7xy697tr5dpn', '删除OSS', 0, '/admin/api/v1/oss/delete', '', '172.16.2.226', NULL, NULL, '[]', '删除', 1, '2024-01-16', NULL, NULL, '2024-01-16 13:15:33.901000', '2024-01-16 13:15:33.911000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (132, '01hm8b7st1rhpnybcxnwzs56yc', '修改OSS', 1, '/admin/api/v1/oss/update', '', '172.16.2.226', NULL, NULL, '[\n  {\n    \"fsId\": 19,\n    \"fsName\": \"ftp\",\n    \"fsBucket\": \"ftp\",\n    \"fsType\": \"FTP\",\n    \"fsEndpoint\": \"127.0.0.1:21\",\n    \"fsDomain\": \"http://localhost:18170/admin/api/\",\n    \"fsAccessKeyId\": \"anonymous\",\n    \"fsAccessKeySecret\": \"anonymous\",\n    \"fsStatus\": 1,\n    \"tenantId\": 0,\n    \"createName\": \"系统管理员\",\n    \"createTime\": {\n      \"date\": {\n        \"year\": 2024,\n        \"month\": 1,\n        \"day\": 16\n      },\n      \"time\": {\n        \"hour\": 0,\n        \"minute\": 0,\n        \"second\": 0,\n        \"nano\": 0\n      }\n    },\n    \"updateTime\": {\n      \"date\": {\n        \"year\": 2024,\n        \"month\": 1,\n        \"day\": 16\n      },\n      \"time\": {\n        \"hour\": 13,\n        \"minute\": 17,\n        \"second\": 20,\n        \"nano\": 281000000\n      }\n    },\n    \"updateName\": \"系统管理员\"\n  }\n]', '修改', 1, '2024-01-16', NULL, NULL, '2024-01-16 13:17:22.112000', '2024-01-16 13:17:22.120000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (133, '01hm8dsgycg3z351m53n1tny8a', '删除OSS', 0, '/admin/api/v1/oss/delete', '', '172.16.2.226', NULL, NULL, '[]', '删除', 1, '2024-01-16', NULL, NULL, '2024-01-16 14:01:58.365000', '2024-01-16 14:02:00.057000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (134, '01hm8exyc18q8p83c5mdehq9cs', '修改OSS', 0, '/admin/api/v1/oss/update', '', '172.16.2.226', NULL, NULL, '[\n  {\n    \"fsId\": 22,\n    \"fsName\": \"minio\",\n    \"fsBucket\": \"filesystem\",\n    \"fsType\": \"MINIO\",\n    \"fsEndpoint\": \"http://127.0.0.1:9000\",\n    \"fsDomain\": \"http://localhost:18170/admin/api\",\n    \"fsAccessKeyId\": \"minioadmin\",\n    \"fsAccessKeySecret\": \"minioadmin\",\n    \"fsStatus\": 1,\n    \"tenantId\": 0,\n    \"createName\": \"系统管理员\",\n    \"createTime\": {\n      \"date\": {\n        \"year\": 2024,\n        \"month\": 1,\n        \"day\": 16\n      },\n      \"time\": {\n        \"hour\": 0,\n        \"minute\": 0,\n        \"second\": 0,\n        \"nano\": 0\n      }\n    },\n    \"updateTime\": {\n      \"date\": {\n        \"year\": 2024,\n        \"month\": 1,\n        \"day\": 16\n      },\n      \"time\": {\n        \"hour\": 14,\n        \"minute\": 21,\n        \"second\": 51,\n        \"nano\": 776000000\n      }\n    },\n    \"updateName\": \"系统管理员\"\n  }\n]', '修改', 1, '2024-01-16', NULL, NULL, '2024-01-16 14:21:51.808000', '2024-01-16 14:21:53.429000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (135, '01hm8f2vfv8mrxx5d9twqj9saa', '修改OSS', 0, '/admin/api/v1/oss/update', '', '172.16.2.226', NULL, NULL, '[\n  {\n    \"fsId\": 22,\n    \"fsName\": \"minio\",\n    \"fsBucket\": \"filesystem\",\n    \"fsType\": \"MINIO\",\n    \"fsFilter\": \"BIN\",\n    \"fsEndpoint\": \"http://127.0.0.1:9000\",\n    \"fsDomain\": \"http://localhost:18170/admin/api\",\n    \"fsAccessKeyId\": \"minioadmin\",\n    \"fsAccessKeySecret\": \"minioadmin\",\n    \"fsStatus\": 1,\n    \"tenantId\": 0,\n    \"createName\": \"系统管理员\",\n    \"createTime\": {\n      \"date\": {\n        \"year\": 2024,\n        \"month\": 1,\n        \"day\": 16\n      },\n      \"time\": {\n        \"hour\": 0,\n        \"minute\": 0,\n        \"second\": 0,\n        \"nano\": 0\n      }\n    },\n    \"updateTime\": {\n      \"date\": {\n        \"year\": 2024,\n        \"month\": 1,\n        \"day\": 16\n      },\n      \"time\": {\n        \"hour\": 14,\n        \"minute\": 24,\n        \"second\": 34,\n        \"nano\": 288000000\n      }\n    },\n    \"updateName\": \"系统管理员\"\n  }\n]', '修改', 1, '2024-01-16', NULL, NULL, '2024-01-16 14:24:34.299000', '2024-01-16 14:24:34.301000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (136, '01hm8n5ke04y09kb4bqccy72nh', '登录', 2, '/admin/api/v2/users/login', '', '172.16.2.226', NULL, NULL, '[\n  {\n    \"username\": \"admin\",\n    \"password\": \"e10adc3949ba59abbe56e057f20f883e\",\n    \"verifyCode\": \"63\"\n  }\n]', '查询', 1, '2024-01-16', NULL, NULL, '2024-01-16 16:10:54.323000', '2024-01-16 16:10:55.858000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (137, '01hm8n9vxq89bdt4ewhk8z7zn4', '修改OSS', 0, '/admin/api/v1/oss/update', '', '172.16.2.226', NULL, NULL, '[\n  {\n    \"fsId\": 22,\n    \"fsName\": \"minio\",\n    \"fsBucket\": \"filesystem\",\n    \"fsType\": \"MINIO\",\n    \"fsFilter\": \"SEPIATONE\",\n    \"fsEndpoint\": \"http://127.0.0.1:9000\",\n    \"fsDomain\": \"http://localhost:18170/admin/api\",\n    \"fsAccessKeyId\": \"minioadmin\",\n    \"fsAccessKeySecret\": \"minioadmin\",\n    \"fsStatus\": 1,\n    \"tenantId\": 0,\n    \"createName\": \"系统管理员\",\n    \"createTime\": {\n      \"date\": {\n        \"year\": 2024,\n        \"month\": 1,\n        \"day\": 16\n      },\n      \"time\": {\n        \"hour\": 0,\n        \"minute\": 0,\n        \"second\": 0,\n        \"nano\": 0\n      }\n    },\n    \"updateTime\": {\n      \"date\": {\n        \"year\": 2024,\n        \"month\": 1,\n        \"day\": 16\n      },\n      \"time\": {\n        \"hour\": 0,\n        \"minute\": 0,\n        \"second\": 0,\n        \"nano\": 0\n      }\n    },\n    \"updateName\": \"系统管理员\"\n  }\n]', '修改', 1, '2024-01-16', NULL, NULL, '2024-01-16 16:13:15.573000', '2024-01-16 16:13:15.577000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (138, '01hm8nbfznxbtsqy0k986c6h7r', '修改OSS', 0, '/admin/api/v1/oss/update', '', '172.16.2.226', NULL, NULL, '[\n  {\n    \"fsId\": 13,\n    \"fsName\": \"测试\",\n    \"fsBucket\": \"test\",\n    \"fsType\": \"LOCAL\",\n    \"fsFilter\": \"SUDOKU\",\n    \"fsEndpoint\": \"Z://\",\n    \"fsDomain\": \"http://localhost:18170/admin/api\",\n    \"fsStoragePath\": \"Z://\",\n    \"fsStatus\": 1,\n    \"tenantId\": 0,\n    \"createName\": \"系统管理员\",\n    \"createTime\": {\n      \"date\": {\n        \"year\": 2024,\n        \"month\": 1,\n        \"day\": 12\n      },\n      \"time\": {\n        \"hour\": 0,\n        \"minute\": 0,\n        \"second\": 0,\n        \"nano\": 0\n      }\n    },\n    \"updateTime\": {\n      \"date\": {\n        \"year\": 2024,\n        \"month\": 1,\n        \"day\": 15\n      },\n      \"time\": {\n        \"hour\": 0,\n        \"minute\": 0,\n        \"second\": 0,\n        \"nano\": 0\n      }\n    },\n    \"updateName\": \"系统管理员\"\n  }\n]', '修改', 1, '2024-01-16', NULL, NULL, '2024-01-16 16:14:08.885000', '2024-01-16 16:14:08.887000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (139, '01hm8ne1hrs92qrssbnfbt2txt', '修改OSS', 0, '/admin/api/v1/oss/update', '', '172.16.2.226', NULL, NULL, '[\n  {\n    \"fsId\": 13,\n    \"fsName\": \"测试\",\n    \"fsBucket\": \"test\",\n    \"fsType\": \"LOCAL\",\n    \"fsFilter\": \"FINDEDGE\",\n    \"fsEndpoint\": \"Z://\",\n    \"fsDomain\": \"http://localhost:18170/admin/api\",\n    \"fsStoragePath\": \"Z://\",\n    \"fsStatus\": 1,\n    \"tenantId\": 0,\n    \"createName\": \"系统管理员\",\n    \"createTime\": {\n      \"date\": {\n        \"year\": 2024,\n        \"month\": 1,\n        \"day\": 12\n      },\n      \"time\": {\n        \"hour\": 0,\n        \"minute\": 0,\n        \"second\": 0,\n        \"nano\": 0\n      }\n    },\n    \"updateTime\": {\n      \"date\": {\n        \"year\": 2024,\n        \"month\": 1,\n        \"day\": 15\n      },\n      \"time\": {\n        \"hour\": 0,\n        \"minute\": 0,\n        \"second\": 0,\n        \"nano\": 0\n      }\n    },\n    \"updateName\": \"系统管理员\"\n  }\n]', '修改', 1, '2024-01-16', NULL, NULL, '2024-01-16 16:15:32.408000', '2024-01-16 16:15:32.410000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (140, '01hm8nfr0q6yx7dadacgj8vr5k', '修改OSS', 0, '/admin/api/v1/oss/update', '', '172.16.2.226', NULL, NULL, '[\n  {\n    \"fsId\": 13,\n    \"fsName\": \"测试\",\n    \"fsBucket\": \"test\",\n    \"fsType\": \"LOCAL\",\n    \"fsFilter\": \"GRAY\",\n    \"fsEndpoint\": \"Z://\",\n    \"fsDomain\": \"http://localhost:18170/admin/api\",\n    \"fsStoragePath\": \"Z://\",\n    \"fsStatus\": 1,\n    \"tenantId\": 0,\n    \"createName\": \"系统管理员\",\n    \"createTime\": {\n      \"date\": {\n        \"year\": 2024,\n        \"month\": 1,\n        \"day\": 12\n      },\n      \"time\": {\n        \"hour\": 0,\n        \"minute\": 0,\n        \"second\": 0,\n        \"nano\": 0\n      }\n    },\n    \"updateTime\": {\n      \"date\": {\n        \"year\": 2024,\n        \"month\": 1,\n        \"day\": 15\n      },\n      \"time\": {\n        \"hour\": 0,\n        \"minute\": 0,\n        \"second\": 0,\n        \"nano\": 0\n      }\n    },\n    \"updateName\": \"系统管理员\"\n  }\n]', '修改', 1, '2024-01-16', NULL, NULL, '2024-01-16 16:16:28.183000', '2024-01-16 16:16:28.193000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (141, '01hm8rb2srq50bwx2paghxf2cr', '删除OSS', 0, '/admin/api/v1/oss/delete', '', '172.16.2.226', NULL, NULL, '[]', '删除', 1, '2024-01-16', NULL, NULL, '2024-01-16 17:06:21.112000', '2024-01-16 17:06:21.121000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (142, '01hm93x9ajxvfycwm7v800g88m', '登录', 3, '/admin/api/v2/users/login', '', '172.16.2.100', NULL, NULL, '[\n  {\n    \"username\": \"admin\",\n    \"password\": \"e10adc3949ba59abbe56e057f20f883e\",\n    \"verifyCode\": \"钱面吃研\"\n  }\n]', '查询', 1, '2024-01-16', NULL, NULL, '2024-01-16 20:28:29.818000', '2024-01-16 20:28:32.116000', NULL, NULL, 0);
INSERT INTO `sys_operate_logger` VALUES (143, '01hm941a6zkgbez5jg8asqt902', '登录', 3, '/admin/api/v2/users/login', '', '172.16.2.100', NULL, NULL, '[\n  {\n    \"username\": \"admin\",\n    \"password\": \"e10adc3949ba59abbe56e057f20f883e\",\n    \"verifyCode\": \"得问公师\"\n  }\n]', '查询', 1, '2024-01-16', NULL, NULL, '2024-01-16 20:30:36.143000', '2024-01-16 20:30:44.088000', NULL, NULL, 0);

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
INSERT INTO `sys_role` VALUES (1, '系统管理员', 'ADMIN', 0, 1, 1, '1', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0);
INSERT INTO `sys_role` VALUES (2, '运维', 'OPS', 0, 1, 1, '4', '', '0', 'ver,time,welcome', NULL, NULL, '2023-07-20 10:06:14.782000', '2023-07-20 10:44:54.408000', 'asdas', NULL, 0);
INSERT INTO `sys_role` VALUES (3, '测试', 'CS', 1, 0, 0, '1', NULL, NULL, NULL, 'admin', NULL, '2023-07-21 20:27:04.142000', '2024-01-11 13:52:06.800000', '系统管理员', NULL, 0);

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
INSERT INTO `sys_role_menu` VALUES (2, 11, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_role_menu` VALUES (2, 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_role_menu` VALUES (2, 12, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_role_menu` VALUES (2, 2, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_role_menu` VALUES (2, 3, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_role_menu` VALUES (2, 4, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_role_menu` VALUES (2, 16, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_role_menu` VALUES (2, 5, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_role_menu` VALUES (2, 9, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_role_menu` VALUES (2, 10, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_setting
-- ----------------------------
DROP TABLE IF EXISTS `sys_setting`;
CREATE TABLE `sys_setting`  (
  `setting_id` int NOT NULL,
  `setting_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设置名称',
  `setting_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设置值',
  `setting_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设置描述',
  `setting_enable` int NULL DEFAULT NULL COMMENT '是否开启; 0:不开启',
  `setting_version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设置版本',
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
INSERT INTO `sys_user` VALUES (15, '942121092219707392', 'ops', NULL, NULL, NULL, NULL, 0, '50e1f134c6cec076359303f0ba2c9575', '172.16.2.226', NULL, 1, '', NULL, NULL, 1, NULL, 1, 'gsnfcmcgvvk7dtuu', '系统管理员', NULL, '2024-01-09 15:34:00.551000', '2024-01-09 15:35:31.784000', '系统管理员', NULL, 0);
INSERT INTO `sys_user` VALUES (16, '942121578249887744', 'ops', '运维', NULL, NULL, NULL, 0, 'bff302a388fa57ba029787c0aea376e7', '172.16.2.226', NULL, 5, '', NULL, NULL, 1, NULL, 0, 'hy1x6x9lhj805m4r', '系统管理员', NULL, '2024-01-09 15:35:53.661000', '2024-01-10 12:01:34.845000', '系统管理员', NULL, 0);
INSERT INTO `sys_user` VALUES (17, '942128543132983296', 'test', '测试人员', NULL, NULL, NULL, 0, 'fd609c6fafe8efb268b67dd0d90677b5', '172.16.2.226', NULL, 1, '', '13312345678', NULL, 1, NULL, 0, 't31os91x56269sul', '系统管理员', NULL, '2024-01-09 16:03:34.220000', '2024-01-10 12:01:35.915000', '系统管理员', NULL, 0);

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
INSERT INTO `sys_user_grid` VALUES (1, 12, '[12,6,6]', '[[\"ver\"],[\"welcome\"],[]]', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_user_grid` VALUES (2, 1, '[24,16,8]', '[[\"space\"],[],[]]', NULL, NULL, NULL, NULL, NULL, NULL, NULL);

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
INSERT INTO `sys_user_role` VALUES (10, 2, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_user_role` VALUES (10, 2, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_user_role` VALUES (10, 2, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_user_role` VALUES (3, 3, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_user_role` VALUES (4, 2, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_user_role` VALUES (4, 3, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_user_role` VALUES (13, 2, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_user_role` VALUES (15, 2, NULL, NULL, NULL, NULL, NULL);

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
