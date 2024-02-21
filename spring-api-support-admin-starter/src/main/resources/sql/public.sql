/*
 Navicat Premium Data Transfer

 Source Server         : 127.0.0.1
 Source Server Type    : PostgreSQL
 Source Server Version : 160002 (160002)
 Source Host           : localhost:5432
 Source Catalog        : system
 Source Schema         : public

 Target Server Type    : PostgreSQL
 Target Server Version : 160002 (160002)
 File Encoding         : 65001

 Date: 21/02/2024 16:17:43
*/


-- ----------------------------
-- Sequence structure for sys_dept_dept_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."sys_dept_dept_id_seq";
CREATE SEQUENCE "public"."sys_dept_dept_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for sys_dict_dict_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."sys_dict_dict_id_seq";
CREATE SEQUENCE "public"."sys_dict_dict_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for sys_dict_type_dict_type_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."sys_dict_type_dict_type_id_seq";
CREATE SEQUENCE "public"."sys_dict_type_dict_type_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for sys_file_storage_fs_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."sys_file_storage_fs_id_seq";
CREATE SEQUENCE "public"."sys_file_storage_fs_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for sys_menu_menu_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."sys_menu_menu_id_seq";
CREATE SEQUENCE "public"."sys_menu_menu_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for sys_message_config_config_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."sys_message_config_config_id_seq";
CREATE SEQUENCE "public"."sys_message_config_config_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for sys_message_template_config_message_config_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."sys_message_template_config_message_config_id_seq";
CREATE SEQUENCE "public"."sys_message_template_config_message_config_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for sys_message_template_message_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."sys_message_template_message_id_seq";
CREATE SEQUENCE "public"."sys_message_template_message_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for sys_operate_logger_log_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."sys_operate_logger_log_id_seq";
CREATE SEQUENCE "public"."sys_operate_logger_log_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for sys_role_menu_role_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."sys_role_menu_role_id_seq";
CREATE SEQUENCE "public"."sys_role_menu_role_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for sys_role_role_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."sys_role_role_id_seq";
CREATE SEQUENCE "public"."sys_role_role_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for sys_setting_setting_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."sys_setting_setting_id_seq";
CREATE SEQUENCE "public"."sys_setting_setting_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for sys_user_setting_grid_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."sys_user_setting_grid_seq";
CREATE SEQUENCE "public"."sys_user_setting_grid_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for sys_user_tripartite_bind_tripartite_bind_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."sys_user_tripartite_bind_tripartite_bind_id_seq";
CREATE SEQUENCE "public"."sys_user_tripartite_bind_tripartite_bind_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for sys_user_user_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."sys_user_user_id_seq";
CREATE SEQUENCE "public"."sys_user_user_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_dept";
CREATE TABLE "public"."sys_dept" (
  "dept_id" int4 NOT NULL DEFAULT nextval('sys_dept_dept_id_seq'::regclass),
  "dept_name" varchar(255) COLLATE "pg_catalog"."default",
  "dept_marker" varchar(255) COLLATE "pg_catalog"."default",
  "dept_parent_id" int4,
  "dept_tree_path" varchar(255) COLLATE "pg_catalog"."default",
  "dept_sort" int4,
  "dept_status" int4,
  "tenant_id" int4 DEFAULT 0,
  "create_name" varchar(255) COLLATE "pg_catalog"."default",
  "create_by" int4,
  "create_time" timestamp(6),
  "update_time" timestamp(6),
  "update_name" varchar(255) COLLATE "pg_catalog"."default",
  "update_by" int4
)
;

-- ----------------------------
-- Records of sys_dept
-- ----------------------------

-- ----------------------------
-- Table structure for sys_dict
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_dict";
CREATE TABLE "public"."sys_dict" (
  "dict_id" int4 NOT NULL DEFAULT nextval('sys_dict_dict_id_seq'::regclass),
  "dict_type_id" varchar(255) COLLATE "pg_catalog"."default",
  "dict_name" varchar(255) COLLATE "pg_catalog"."default",
  "dict_value" varchar(255) COLLATE "pg_catalog"."default",
  "dict_sort" int4,
  "dict_status" int4,
  "dict_sys" int4,
  "dict_defaulted" int4,
  "dict_remark" varchar(255) COLLATE "pg_catalog"."default",
  "tenant_id" int4 DEFAULT 0,
  "create_name" varchar(255) COLLATE "pg_catalog"."default",
  "create_by" int4,
  "create_time" timestamp(6),
  "update_time" timestamp(6),
  "update_name" varchar(255) COLLATE "pg_catalog"."default",
  "update_by" int4
)
;

-- ----------------------------
-- Records of sys_dict
-- ----------------------------

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_dict_type";
CREATE TABLE "public"."sys_dict_type" (
  "dict_type_id" int4 NOT NULL DEFAULT nextval('sys_dict_type_dict_type_id_seq'::regclass),
  "dict_type_name" varchar(255) COLLATE "pg_catalog"."default",
  "dict_type_code" varchar(255) COLLATE "pg_catalog"."default",
  "dict_type_sort" int4,
  "dict_type_status" int4,
  "dict_type_sys" int4,
  "dict_type_remark" varchar(255) COLLATE "pg_catalog"."default",
  "tenant_id" int4 DEFAULT 0,
  "create_name" varchar(255) COLLATE "pg_catalog"."default",
  "create_by" int4,
  "create_time" timestamp(6),
  "update_time" timestamp(6),
  "update_name" varchar(255) COLLATE "pg_catalog"."default",
  "update_by" int4
)
;

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------

-- ----------------------------
-- Table structure for sys_file_storage
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_file_storage";
CREATE TABLE "public"."sys_file_storage" (
  "fs_id" int4 NOT NULL DEFAULT nextval('sys_file_storage_fs_id_seq'::regclass),
  "fs_name" varchar(255) COLLATE "pg_catalog"."default",
  "fs_bucket" varchar(255) COLLATE "pg_catalog"."default",
  "fs_type" varchar(255) COLLATE "pg_catalog"."default",
  "fs_filter" varchar(255) COLLATE "pg_catalog"."default",
  "fs_plugin" varchar(255) COLLATE "pg_catalog"."default",
  "fs_named" varchar(255) COLLATE "pg_catalog"."default",
  "fs_endpoint" varchar(255) COLLATE "pg_catalog"."default",
  "fs_domain" varchar(255) COLLATE "pg_catalog"."default",
  "fs_storage_path" varchar(255) COLLATE "pg_catalog"."default",
  "fs_access_key_id" varchar(255) COLLATE "pg_catalog"."default",
  "fs_access_key_secret" varchar(255) COLLATE "pg_catalog"."default",
  "fs_status" int4,
  "tenant_id" int4 DEFAULT 0,
  "create_name" varchar(255) COLLATE "pg_catalog"."default",
  "create_by" int4,
  "create_time" timestamp(6),
  "update_time" timestamp(6),
  "update_name" varchar(255) COLLATE "pg_catalog"."default",
  "update_by" int4
)
;

-- ----------------------------
-- Records of sys_file_storage
-- ----------------------------

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_menu";
CREATE TABLE "public"."sys_menu" (
  "menu_id" int4 NOT NULL DEFAULT nextval('sys_menu_menu_id_seq'::regclass),
  "menu_parent_id" int4,
  "menu_name" varchar(255) COLLATE "pg_catalog"."default",
  "menu_type" varchar(255) COLLATE "pg_catalog"."default",
  "menu_path" varchar(255) COLLATE "pg_catalog"."default",
  "menu_component" varchar(255) COLLATE "pg_catalog"."default",
  "menu_tag" varchar(255) COLLATE "pg_catalog"."default",
  "menu_color" varchar(255) COLLATE "pg_catalog"."default",
  "menu_perm" varchar(255) COLLATE "pg_catalog"."default",
  "menu_visible" int4,
  "menu_status" int4,
  "menu_sort" int4,
  "menu_icon" varchar(255) COLLATE "pg_catalog"."default",
  "menu_redirect" varchar(255) COLLATE "pg_catalog"."default",
  "menu_tree_path" varchar(255) COLLATE "pg_catalog"."default",
  "menu_affix" int4,
  "create_name" varchar(255) COLLATE "pg_catalog"."default",
  "create_by" varchar(255) COLLATE "pg_catalog"."default",
  "create_time" date,
  "update_time" date,
  "update_name" varchar(255) COLLATE "pg_catalog"."default",
  "update_by" varchar(255) COLLATE "pg_catalog"."default",
  "tenant_id" int4
)
;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO "public"."sys_menu" VALUES (1, 0, '首页', 'menu', '/home', NULL, NULL, NULL, NULL, 1, 1, NULL, 'el-icon-eleme-filled', NULL, '0', 0, NULL, NULL, NULL, NULL, NULL, NULL, 0);
INSERT INTO "public"."sys_menu" VALUES (2, 1, '控制台', 'menu', '/dashboard', 'home', NULL, NULL, NULL, 1, 1, NULL, 'el-icon-menu', NULL, '0,1', 1, NULL, NULL, NULL, NULL, NULL, NULL, 0);
INSERT INTO "public"."sys_menu" VALUES (3, 0, '系统管理', 'menu', '/setting', NULL, NULL, NULL, NULL, 1, 1, NULL, 'el-icon-setting', NULL, '0', 0, NULL, NULL, NULL, '2023-07-19', NULL, NULL, 0);
INSERT INTO "public"."sys_menu" VALUES (4, 3, '用户管理', 'menu', '/setting/user', 'setting/user', NULL, NULL, NULL, 1, 1, 2, 'el-icon-user-filled', NULL, '0,3', 0, NULL, NULL, NULL, '2023-07-23', '系统管理员', NULL, 0);
INSERT INTO "public"."sys_menu" VALUES (5, 3, '菜单管理', 'menu', '/setting/menu', 'setting/menu', NULL, NULL, NULL, 1, 1, 100, 'el-icon-fold', NULL, '0,3', 0, NULL, NULL, NULL, '2023-07-20', NULL, NULL, 0);
INSERT INTO "public"."sys_menu" VALUES (7, 6, '外部链接', 'link', 'http://www.baidu.com', '', NULL, NULL, NULL, 1, 1, 1, 'el-icon-aim', NULL, '', 0, NULL, NULL, '2023-07-19', '2023-07-19', NULL, NULL, 0);
INSERT INTO "public"."sys_menu" VALUES (8, 6, '内部链接', 'iframe', 'http://www.baidu.com', '', NULL, NULL, NULL, 1, 1, 2, 'el-icon-add-location', NULL, '', 0, NULL, NULL, '2023-07-19', '2023-07-19', NULL, NULL, 0);
INSERT INTO "public"."sys_menu" VALUES (9, 3, '字典管理', 'menu', '/setting/dic', '/setting/dic', '', '#A74E4E', NULL, 1, 1, NULL, 'el-icon-collection', NULL, '0,3', 0, NULL, NULL, '2023-07-19', '2023-07-20', NULL, NULL, 0);
INSERT INTO "public"."sys_menu" VALUES (10, 3, '角色管理', 'menu', '/setting/role', '/setting/role', '', NULL, NULL, 1, 1, 3, 'el-icon-notebook', NULL, '0,3', 0, NULL, NULL, '2023-07-20', '2023-07-21', NULL, NULL, 0);
INSERT INTO "public"."sys_menu" VALUES (11, 3, '操作日志', 'menu', '/setting/log', '/setting/log', NULL, NULL, NULL, 1, 1, 101, 'el-icon-warning', NULL, '0,3', 0, NULL, NULL, '2023-07-20', '2023-07-20', NULL, NULL, 0);
INSERT INTO "public"."sys_menu" VALUES (12, 3, '部门管理', 'menu', '/setting/dept', '/setting/dept', '', NULL, NULL, 1, 1, 1, 'sc-icon-organization', NULL, '0,3', 0, NULL, NULL, '2023-07-21', '2023-07-21', NULL, NULL, 0);
INSERT INTO "public"."sys_menu" VALUES (15, 6, '空白模板', 'menu', '/template/layout/blank', '/template/layout/blank.vue', NULL, NULL, NULL, 1, 1, NULL, 'sc-icon-template', NULL, '', 0, '系统管理员', '1', '2023-07-28', '2023-07-28', '系统管理员', '1', 0);
INSERT INTO "public"."sys_menu" VALUES (16, 1, '账号信息', 'menu', '/usercenter', '/usercenter', NULL, NULL, NULL, 0, 1, NULL, 'el-icon-avatar', NULL, '0,1', 0, '系统管理员', '1', '2023-07-31', '2023-07-31', '系统管理员', '1', 0);
INSERT INTO "public"."sys_menu" VALUES (18, 17, '人脸比对1:1', 'menu', '/learning/faceCompare', '/learning/faceCompare', '', NULL, NULL, 1, 1, NULL, 'sc-icon-face-compare', NULL, '', 0, '系统管理员', '1', '2023-07-31', '2023-07-31', '系统管理员', '1', 0);
INSERT INTO "public"."sys_menu" VALUES (19, 17, '图像比对1:1', 'menu', '/learning/imageCompare', '/learning/imageCompare', '', NULL, NULL, 1, 1, NULL, 'sc-icon-image-compare', NULL, '', 0, '系统管理员', '1', '2023-07-31', '2023-07-31', '系统管理员', '1', 0);
INSERT INTO "public"."sys_menu" VALUES (20, 17, 'OCR识别', 'menu', '/learning/ocr', '/learning/ocr', '', NULL, NULL, 1, 1, NULL, 'sc-icon-image-reg', NULL, '', 0, '系统管理员', '1', '2023-07-31', '2023-07-31', '系统管理员', '1', 0);
INSERT INTO "public"."sys_menu" VALUES (21, 17, '人脸检测', 'menu', '/learning/faceDetection', '/learning/faceDetection', NULL, NULL, NULL, 1, 1, NULL, 'sc-icon-face-detection', NULL, '', 0, '系统管理员', '1', '2023-08-01', '2023-08-01', '系统管理员', '1', 0);
INSERT INTO "public"."sys_menu" VALUES (22, 17, '底库', 'menu', '/learning/library/:indexName/:libType', '/learning/library', NULL, NULL, NULL, 0, 1, NULL, 'sc-icon-face-reg-library', NULL, '', 0, '系统管理员', '1', '2023-08-02', '2023-08-03', '系统管理员', '1', 0);
INSERT INTO "public"."sys_menu" VALUES (23, 17, '底库管理', 'menu', '/setting/library', '/setting/library', NULL, NULL, NULL, 1, 1, NULL, 'sc-icon-face-reg-library', NULL, '', 0, '系统管理员', '1', '2023-08-03', '2023-08-10', '系统管理员', '1', 0);
INSERT INTO "public"."sys_menu" VALUES (24, 17, 'GAN', 'menu', '/learning/gan', '/learning/gan', NULL, NULL, NULL, 1, 1, NULL, 'sc-icon-sc', NULL, '', 0, '系统管理员', '1', '2023-08-08', '2023-08-10', '系统管理员', '1', 0);
INSERT INTO "public"."sys_menu" VALUES (27, 26, '调度任务管理', 'menu', '/scheduler/jobinfo', '/scheduler/jobinfo', NULL, NULL, '任务管理', 1, 1, NULL, 'el-icon-alarm-clock', NULL, '', 0, '系统管理员', '1', '2023-08-08', '2023-08-14', '系统管理员', '1', 0);
INSERT INTO "public"."sys_menu" VALUES (28, 26, '调度日志', 'menu', '/scheduler/joblog/:jobGroup/:jobId', '/scheduler/joblog', NULL, NULL, NULL, 1, 1, NULL, 'el-icon-warning', NULL, '', 0, '系统管理员', '1', '2023-08-08', '2023-08-09', '系统管理员', '1', 0);
INSERT INTO "public"."sys_menu" VALUES (29, 26, '执行器管理', 'menu', '/scheduler/jobgroup', '/scheduler/jobgroup', NULL, NULL, NULL, 1, 1, NULL, 'el-icon-takeaway-box', NULL, '', 0, '系统管理员', '1', '2023-08-09', '2023-08-09', '系统管理员', '1', 0);
INSERT INTO "public"."sys_menu" VALUES (30, 17, '人脸1:N', 'menu', '/learning/faceCompareN', '/learning/faceCompareN', NULL, NULL, NULL, 1, 1, NULL, 'sc-icon-face-detection', NULL, '', 0, '系统管理员', '1', '2023-08-10', '2023-08-10', '系统管理员', '1', 0);
INSERT INTO "public"."sys_menu" VALUES (34, 32, '统一配置', 'menu', '/config/config', '/config/config', NULL, NULL, NULL, 1, 1, NULL, 'el-icon-grid', NULL, '', 0, '系统管理员', '1', '2023-08-14', '2023-08-14', '系统管理员', '1', 0);
INSERT INTO "public"."sys_menu" VALUES (35, 32, '统一脚本', 'menu', '/config/bean', '/config/bean', NULL, NULL, NULL, 1, 1, NULL, '', NULL, '', 0, '系统管理员', '1', '2023-08-15', '2023-08-15', '系统管理员', '1', 0);
INSERT INTO "public"."sys_menu" VALUES (36, 32, '统一日志', 'menu', '/config/log', '/config/log', NULL, NULL, NULL, 1, 1, NULL, 'el-icon-warning', NULL, '', 0, '系统管理员', '1', '2023-08-17', '2023-08-17', '系统管理员', '1', 0);
INSERT INTO "public"."sys_menu" VALUES (37, 32, '统一链路', 'menu', '/config/trace', '/config/trace', NULL, NULL, NULL, 1, 1, NULL, 'sc-icon-trace', NULL, '', 0, '系统管理员', '1', '2023-08-18', '2023-08-18', '系统管理员', '1', 0);
INSERT INTO "public"."sys_menu" VALUES (49, 3, 'OSS', 'menu', '/setting/oss', '/setting/oss', NULL, NULL, NULL, 1, 1, 101, 'el-icon-mostly-cloudy', NULL, '0,3', 0, NULL, NULL, NULL, NULL, NULL, NULL, 0);
INSERT INTO "public"."sys_menu" VALUES (50, 3, '系统设置', 'menu', '/setting/setting', '/setting/setting', NULL, NULL, NULL, 1, 1, 102, 'el-icon-setting', NULL, '0,3', 0, NULL, NULL, NULL, NULL, NULL, NULL, 0);
INSERT INTO "public"."sys_menu" VALUES (51, 3, '推送模板', 'menu', '/setting/message', '/setting/message', NULL, NULL, NULL, 1, 1, NULL, 'el-icon-message', NULL, '0,3', 0, NULL, NULL, NULL, NULL, NULL, NULL, 0);
INSERT INTO "public"."sys_menu" VALUES (52, 1, '推送模板配置', 'menu', '/setting/message/setting', '/setting/message/setting', NULL, NULL, NULL, 0, 1, NULL, 'el-icon-setting', NULL, '0,1', 0, NULL, NULL, NULL, NULL, NULL, NULL, 0);

-- ----------------------------
-- Table structure for sys_message_config
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_message_config";
CREATE TABLE "public"."sys_message_config" (
  "config_id" int4 NOT NULL DEFAULT nextval('sys_message_config_config_id_seq'::regclass),
  "config_name" varchar(255) COLLATE "pg_catalog"."default",
  "config_type" varchar(255) COLLATE "pg_catalog"."default",
  "config_access_key" varchar(255) COLLATE "pg_catalog"."default",
  "config_secret_key" varchar(255) COLLATE "pg_catalog"."default",
  "config_endpoint" varchar(255) COLLATE "pg_catalog"."default",
  "config_ecname" varchar(255) COLLATE "pg_catalog"."default",
  "config_mapper_template" varchar(255) COLLATE "pg_catalog"."default",
  "config_sms_type" varchar(255) COLLATE "pg_catalog"."default",
  "tenant_id" int4 DEFAULT 0,
  "create_name" varchar(255) COLLATE "pg_catalog"."default",
  "create_by" int4,
  "create_time" timestamp(6),
  "update_time" timestamp(6),
  "update_name" varchar(255) COLLATE "pg_catalog"."default",
  "update_by" int4
)
;

-- ----------------------------
-- Records of sys_message_config
-- ----------------------------

-- ----------------------------
-- Table structure for sys_message_template
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_message_template";
CREATE TABLE "public"."sys_message_template" (
  "message_id" int4 NOT NULL DEFAULT nextval('sys_message_template_message_id_seq'::regclass),
  "message_name" varchar(255) COLLATE "pg_catalog"."default",
  "message_code" varchar(255) COLLATE "pg_catalog"."default",
  "message_config_id" int4,
  "message_type" varchar(255) COLLATE "pg_catalog"."default",
  "message_template" varchar(255) COLLATE "pg_catalog"."default",
  "message_sender" varchar(255) COLLATE "pg_catalog"."default",
  "tenant_id" int4 DEFAULT 0,
  "create_name" varchar(255) COLLATE "pg_catalog"."default",
  "create_by" int4,
  "create_time" timestamp(6),
  "update_time" timestamp(6),
  "update_name" varchar(255) COLLATE "pg_catalog"."default",
  "update_by" int4
)
;

-- ----------------------------
-- Records of sys_message_template
-- ----------------------------

-- ----------------------------
-- Table structure for sys_message_template_config
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_message_template_config";
CREATE TABLE "public"."sys_message_template_config" (
  "message_config_id" int4 NOT NULL DEFAULT nextval('sys_message_template_config_message_config_id_seq'::regclass),
  "message_id" int4,
  "config_id" int4
)
;

-- ----------------------------
-- Records of sys_message_template_config
-- ----------------------------

-- ----------------------------
-- Table structure for sys_operate_logger
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_operate_logger";
CREATE TABLE "public"."sys_operate_logger" (
  "log_id" int4 NOT NULL DEFAULT nextval('sys_operate_logger_log_id_seq'::regclass),
  "log_code" varchar(255) COLLATE "pg_catalog"."default",
  "log_name" varchar(255) COLLATE "pg_catalog"."default",
  "log_cost" int4,
  "log_mapping" varchar(255) COLLATE "pg_catalog"."default",
  "log_content" varchar(255) COLLATE "pg_catalog"."default",
  "client_ip" varchar(255) COLLATE "pg_catalog"."default",
  "client_ip_position" varchar(255) COLLATE "pg_catalog"."default",
  "log_watch" varchar(255) COLLATE "pg_catalog"."default",
  "log_param" varchar(255) COLLATE "pg_catalog"."default",
  "log_module" varchar(255) COLLATE "pg_catalog"."default",
  "log_status" int4,
  "log_day" date,
  "tenant_id" int4 DEFAULT 0,
  "create_name" varchar(255) COLLATE "pg_catalog"."default",
  "create_by" int4,
  "create_time" timestamp(6),
  "update_time" timestamp(6),
  "update_name" varchar(255) COLLATE "pg_catalog"."default",
  "update_by" int4
)
;

-- ----------------------------
-- Records of sys_operate_logger
-- ----------------------------

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_role";
CREATE TABLE "public"."sys_role" (
  "role_id" int4 NOT NULL DEFAULT nextval('sys_role_role_id_seq'::regclass),
  "role_name" varchar(255) COLLATE "pg_catalog"."default",
  "role_code" varchar(255) COLLATE "pg_catalog"."default",
  "role_sort" int4,
  "role_status" int4,
  "role_sys" int4,
  "role_data_scope" varchar(255) COLLATE "pg_catalog"."default",
  "role_data_rule" varchar(255) COLLATE "pg_catalog"."default",
  "role_dashboard" varchar(255) COLLATE "pg_catalog"."default",
  "role_grid" varchar(255) COLLATE "pg_catalog"."default",
  "tenant_id" int4 DEFAULT 0,
  "create_name" varchar(255) COLLATE "pg_catalog"."default",
  "create_by" int4,
  "create_time" timestamp(6),
  "update_time" timestamp(6),
  "update_name" varchar(255) COLLATE "pg_catalog"."default",
  "update_by" int4
)
;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO "public"."sys_role" VALUES (1, '系统超级管理员', 'ADMIN', 1, 1, 1, '1', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0);
INSERT INTO "public"."sys_role" VALUES (2, '运维', 'OPS', 1, 1, 1, '1', NULL, NULL, NULL, 0, '系统管理员', NULL, '2024-01-30 16:30:38', NULL, NULL, 0);

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_role_menu";
CREATE TABLE "public"."sys_role_menu" (
  "role_id" int4 NOT NULL DEFAULT nextval('sys_role_menu_role_id_seq'::regclass),
  "menu_id" int4
)
;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------

-- ----------------------------
-- Table structure for sys_setting
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_setting";
CREATE TABLE "public"."sys_setting" (
  "setting_id" int4 NOT NULL DEFAULT nextval('sys_setting_setting_id_seq'::regclass),
  "setting_name" varchar(255) COLLATE "pg_catalog"."default",
  "setting_type" varchar(255) COLLATE "pg_catalog"."default",
  "setting_value" varchar(255) COLLATE "pg_catalog"."default",
  "setting_desc" varchar(255) COLLATE "pg_catalog"."default",
  "setting_enable" int4,
  "setting_version" varchar(255) COLLATE "pg_catalog"."default"
)
;

-- ----------------------------
-- Records of sys_setting
-- ----------------------------
INSERT INTO "public"."sys_setting" VALUES (1, 'task_dx', 'false', '短信推送', NULL, NULL, NULL);
INSERT INTO "public"."sys_setting" VALUES (2, 'task_wx', 'true', '微信推送', NULL, NULL, NULL);
INSERT INTO "public"."sys_setting" VALUES (3, 'task_zn', 'true', '站内推送', NULL, NULL, NULL);
INSERT INTO "public"."sys_setting" VALUES (4, 'task_yj', 'true', '邮件推送', NULL, NULL, NULL);
INSERT INTO "public"."sys_setting" VALUES (5, 'codec_open', 'false', '开启', NULL, NULL, NULL);
INSERT INTO "public"."sys_setting" VALUES (6, 'initial_socket', 'true', '开启', NULL, NULL, NULL);
INSERT INTO "public"."sys_setting" VALUES (7, 'initial_debug', 'false', '开启', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_user";
CREATE TABLE "public"."sys_user" (
  "user_id" int4 NOT NULL DEFAULT nextval('sys_user_user_id_seq'::regclass),
  "user_code" varchar(255) COLLATE "pg_catalog"."default",
  "username" varchar(255) COLLATE "pg_catalog"."default",
  "user_real_name" varchar(255) COLLATE "pg_catalog"."default",
  "user_marker" varchar(255) COLLATE "pg_catalog"."default",
  "user_nickname" varchar(255) COLLATE "pg_catalog"."default",
  "user_gender" int4,
  "user_sys" int4,
  "user_password" varchar(255) COLLATE "pg_catalog"."default",
  "user_register_ip" varchar(255) COLLATE "pg_catalog"."default",
  "user_register_address" varchar(255) COLLATE "pg_catalog"."default",
  "dept_id" int4,
  "user_avatar" varchar(255) COLLATE "pg_catalog"."default",
  "user_mobile" varchar(255) COLLATE "pg_catalog"."default",
  "user_address" varchar(255) COLLATE "pg_catalog"."default",
  "user_status" int4,
  "user_email" varchar(255) COLLATE "pg_catalog"."default",
  "user_deleted" int4,
  "user_salt" varchar(255) COLLATE "pg_catalog"."default",
  "tenant_id" int4 DEFAULT 0,
  "create_name" varchar(255) COLLATE "pg_catalog"."default",
  "create_by" int4,
  "create_time" timestamp(6),
  "update_time" timestamp(6),
  "update_name" varchar(255) COLLATE "pg_catalog"."default",
  "update_by" int4
)
;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO "public"."sys_user" VALUES (1, NULL, 'admin', '系统管理员', '系统管理员0001', '系统管理员', 2, 1, '8512935d6d726a53c13dc9d5cf6f72c7', NULL, NULL, 1, 'https://oss.youlai.tech/youlai-boot/2023/05/16/811270ef31f548af9cffc026dfc3777b.gif', NULL, NULL, 1, NULL, 0, 'krdpgdbsinu1zt', 0, NULL, NULL, '2019-10-10 13:41:22', '2024-01-30 13:19:34', NULL, NULL);

-- ----------------------------
-- Table structure for sys_user_setting
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_user_setting";
CREATE TABLE "public"."sys_user_setting" (
  "grid" int4 NOT NULL DEFAULT nextval('sys_user_setting_grid_seq'::regclass),
  "user_id" int4,
  "layout" varchar(255) COLLATE "pg_catalog"."default",
  "copms_list" varchar(255) COLLATE "pg_catalog"."default",
  "tenant_id" int4 DEFAULT 0,
  "create_name" varchar(255) COLLATE "pg_catalog"."default",
  "create_by" int4,
  "create_time" timestamp(6),
  "update_time" timestamp(6),
  "update_name" varchar(255) COLLATE "pg_catalog"."default",
  "update_by" int4
)
;

-- ----------------------------
-- Records of sys_user_setting
-- ----------------------------
INSERT INTO "public"."sys_user_setting" VALUES (3, 1, '[24,16,8]', '[[null],[],[]]', NULL, '系统管理员', NULL, '2024-01-17 00:00:00', '2024-01-24 00:00:00', '系统管理员', NULL);

-- ----------------------------
-- Table structure for sys_user_tripartite_bind
-- ----------------------------
DROP TABLE IF EXISTS "public"."sys_user_tripartite_bind";
CREATE TABLE "public"."sys_user_tripartite_bind" (
  "tripartite_bind_id" int4 NOT NULL DEFAULT nextval('sys_user_tripartite_bind_tripartite_bind_id_seq'::regclass),
  "user_id" int4,
  "login_code_type" varchar(255) COLLATE "pg_catalog"."default",
  "login_username" varchar(255) COLLATE "pg_catalog"."default",
  "login_uuid" varchar(255) COLLATE "pg_catalog"."default",
  "login_avatar" varchar(255) COLLATE "pg_catalog"."default",
  "login_name" varchar(255) COLLATE "pg_catalog"."default",
  "tenant_id" int4 DEFAULT 0,
  "create_name" varchar(255) COLLATE "pg_catalog"."default",
  "create_by" int4,
  "create_time" timestamp(6),
  "update_time" timestamp(6),
  "update_name" varchar(255) COLLATE "pg_catalog"."default",
  "update_by" int4
)
;

-- ----------------------------
-- Records of sys_user_tripartite_bind
-- ----------------------------
INSERT INTO "public"."sys_user_tripartite_bind" VALUES (6, 1, 'gitee', 'achtk', '7588800', 'https://gitee.com/assets/no_portrait.png', 'ACHTK', 0, 'admin', 1, '2024-01-30 14:55:35', NULL, NULL, NULL);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."sys_dept_dept_id_seq"
OWNED BY "public"."sys_dept"."dept_id";
SELECT setval('"public"."sys_dept_dept_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."sys_dict_dict_id_seq"
OWNED BY "public"."sys_dict"."dict_id";
SELECT setval('"public"."sys_dict_dict_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."sys_dict_type_dict_type_id_seq"
OWNED BY "public"."sys_dict_type"."dict_type_id";
SELECT setval('"public"."sys_dict_type_dict_type_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."sys_file_storage_fs_id_seq"
OWNED BY "public"."sys_file_storage"."fs_id";
SELECT setval('"public"."sys_file_storage_fs_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."sys_menu_menu_id_seq"
OWNED BY "public"."sys_menu"."menu_id";
SELECT setval('"public"."sys_menu_menu_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."sys_message_config_config_id_seq"
OWNED BY "public"."sys_message_config"."config_id";
SELECT setval('"public"."sys_message_config_config_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."sys_message_template_config_message_config_id_seq"
OWNED BY "public"."sys_message_template_config"."message_config_id";
SELECT setval('"public"."sys_message_template_config_message_config_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."sys_message_template_message_id_seq"
OWNED BY "public"."sys_message_template"."message_id";
SELECT setval('"public"."sys_message_template_message_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."sys_operate_logger_log_id_seq"
OWNED BY "public"."sys_operate_logger"."log_id";
SELECT setval('"public"."sys_operate_logger_log_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."sys_role_menu_role_id_seq"
OWNED BY "public"."sys_role_menu"."role_id";
SELECT setval('"public"."sys_role_menu_role_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."sys_role_role_id_seq"
OWNED BY "public"."sys_role"."role_id";
SELECT setval('"public"."sys_role_role_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."sys_setting_setting_id_seq"
OWNED BY "public"."sys_setting"."setting_id";
SELECT setval('"public"."sys_setting_setting_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."sys_user_setting_grid_seq"
OWNED BY "public"."sys_user_setting"."grid";
SELECT setval('"public"."sys_user_setting_grid_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."sys_user_tripartite_bind_tripartite_bind_id_seq"
OWNED BY "public"."sys_user_tripartite_bind"."tripartite_bind_id";
SELECT setval('"public"."sys_user_tripartite_bind_tripartite_bind_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."sys_user_user_id_seq"
OWNED BY "public"."sys_user"."user_id";
SELECT setval('"public"."sys_user_user_id_seq"', 1, false);

-- ----------------------------
-- Primary Key structure for table sys_dept
-- ----------------------------
ALTER TABLE "public"."sys_dept" ADD CONSTRAINT "sys_dept_pkey" PRIMARY KEY ("dept_id");

-- ----------------------------
-- Primary Key structure for table sys_dict
-- ----------------------------
ALTER TABLE "public"."sys_dict" ADD CONSTRAINT "sys_dict_pkey" PRIMARY KEY ("dict_id");

-- ----------------------------
-- Primary Key structure for table sys_dict_type
-- ----------------------------
ALTER TABLE "public"."sys_dict_type" ADD CONSTRAINT "sys_dict_type_pkey" PRIMARY KEY ("dict_type_id");

-- ----------------------------
-- Primary Key structure for table sys_file_storage
-- ----------------------------
ALTER TABLE "public"."sys_file_storage" ADD CONSTRAINT "sys_file_storage_pkey" PRIMARY KEY ("fs_id");

-- ----------------------------
-- Primary Key structure for table sys_menu
-- ----------------------------
ALTER TABLE "public"."sys_menu" ADD CONSTRAINT "sys_menu_pkey" PRIMARY KEY ("menu_id");

-- ----------------------------
-- Primary Key structure for table sys_message_config
-- ----------------------------
ALTER TABLE "public"."sys_message_config" ADD CONSTRAINT "sys_message_config_pkey" PRIMARY KEY ("config_id");

-- ----------------------------
-- Primary Key structure for table sys_message_template
-- ----------------------------
ALTER TABLE "public"."sys_message_template" ADD CONSTRAINT "sys_message_template_pkey" PRIMARY KEY ("message_id");

-- ----------------------------
-- Primary Key structure for table sys_message_template_config
-- ----------------------------
ALTER TABLE "public"."sys_message_template_config" ADD CONSTRAINT "sys_message_template_config_pkey" PRIMARY KEY ("message_config_id");

-- ----------------------------
-- Primary Key structure for table sys_operate_logger
-- ----------------------------
ALTER TABLE "public"."sys_operate_logger" ADD CONSTRAINT "sys_operate_logger_pkey" PRIMARY KEY ("log_id");

-- ----------------------------
-- Primary Key structure for table sys_role
-- ----------------------------
ALTER TABLE "public"."sys_role" ADD CONSTRAINT "sys_role_pkey" PRIMARY KEY ("role_id");

-- ----------------------------
-- Primary Key structure for table sys_role_menu
-- ----------------------------
ALTER TABLE "public"."sys_role_menu" ADD CONSTRAINT "sys_role_menu_pkey" PRIMARY KEY ("role_id");

-- ----------------------------
-- Primary Key structure for table sys_setting
-- ----------------------------
ALTER TABLE "public"."sys_setting" ADD CONSTRAINT "sys_setting_pkey" PRIMARY KEY ("setting_id");

-- ----------------------------
-- Primary Key structure for table sys_user
-- ----------------------------
ALTER TABLE "public"."sys_user" ADD CONSTRAINT "sys_user_pkey" PRIMARY KEY ("user_id");

-- ----------------------------
-- Primary Key structure for table sys_user_setting
-- ----------------------------
ALTER TABLE "public"."sys_user_setting" ADD CONSTRAINT "sys_user_setting_pkey" PRIMARY KEY ("grid");

-- ----------------------------
-- Primary Key structure for table sys_user_tripartite_bind
-- ----------------------------
ALTER TABLE "public"."sys_user_tripartite_bind" ADD CONSTRAINT "sys_user_tripartite_bind_pkey" PRIMARY KEY ("tripartite_bind_id");
