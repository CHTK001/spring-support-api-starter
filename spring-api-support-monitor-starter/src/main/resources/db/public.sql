/*
 Navicat Premium Data Transfer

 Source Server         : 127.0.0.1
 Source Server Type    : PostgreSQL
 Source Server Version : 160002 (160002)
 Source Host           : localhost:5432
 Source Catalog        : monitor
 Source Schema         : public

 Target Server Type    : PostgreSQL
 Target Server Version : 160002 (160002)
 File Encoding         : 65001

 Date: 21/02/2024 16:18:00
*/


-- ----------------------------
-- Sequence structure for monitor_app_monitor_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."monitor_app_monitor_id_seq";
CREATE SEQUENCE "public"."monitor_app_monitor_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for monitor_config_config_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."monitor_config_config_id_seq";
CREATE SEQUENCE "public"."monitor_config_config_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for monitor_limit_limit_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."monitor_limit_limit_id_seq";
CREATE SEQUENCE "public"."monitor_limit_limit_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for monitor_mybatis_monitor_mybatis_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."monitor_mybatis_monitor_mybatis_id_seq";
CREATE SEQUENCE "public"."monitor_mybatis_monitor_mybatis_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for monitor_patch_monitor_patch_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."monitor_patch_monitor_patch_id_seq";
CREATE SEQUENCE "public"."monitor_patch_monitor_patch_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for monitor_sys_gen_column_col_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."monitor_sys_gen_column_col_id_seq";
CREATE SEQUENCE "public"."monitor_sys_gen_column_col_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for monitor_sys_gen_gen_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."monitor_sys_gen_gen_id_seq";
CREATE SEQUENCE "public"."monitor_sys_gen_gen_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for monitor_sys_gen_remark_remark_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."monitor_sys_gen_remark_remark_id_seq";
CREATE SEQUENCE "public"."monitor_sys_gen_remark_remark_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for monitor_sys_gen_shell_shell_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."monitor_sys_gen_shell_shell_id_seq";
CREATE SEQUENCE "public"."monitor_sys_gen_shell_shell_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for monitor_sys_gen_table_tab_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."monitor_sys_gen_table_tab_id_seq";
CREATE SEQUENCE "public"."monitor_sys_gen_table_tab_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for monitor_sys_gen_template_template_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."monitor_sys_gen_template_template_id_seq";
CREATE SEQUENCE "public"."monitor_sys_gen_template_template_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Table structure for monitor_app
-- ----------------------------
DROP TABLE IF EXISTS "public"."monitor_app";
CREATE TABLE "public"."monitor_app" (
  "monitor_id" int4 NOT NULL DEFAULT nextval('monitor_app_monitor_id_seq'::regclass),
  "monitor_name" varchar(255) COLLATE "pg_catalog"."default",
  "monitor_appname" varchar(255) COLLATE "pg_catalog"."default",
  "create_name" varchar(255) COLLATE "pg_catalog"."default",
  "create_by" int4,
  "create_time" timestamp(6),
  "update_time" timestamp(6),
  "update_name" varchar(255) COLLATE "pg_catalog"."default",
  "update_by" int4
)
;

-- ----------------------------
-- Records of monitor_app
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_config
-- ----------------------------
DROP TABLE IF EXISTS "public"."monitor_config";
CREATE TABLE "public"."monitor_config" (
  "config_id" int4 NOT NULL DEFAULT nextval('monitor_config_config_id_seq'::regclass),
  "config_name" varchar(255) COLLATE "pg_catalog"."default",
  "config_value" varchar(255) COLLATE "pg_catalog"."default",
  "config_desc" varchar(255) COLLATE "pg_catalog"."default",
  "config_profile" varchar(255) COLLATE "pg_catalog"."default",
  "config_appname" varchar(255) COLLATE "pg_catalog"."default",
  "config_status" int4,
  "create_name" varchar(255) COLLATE "pg_catalog"."default",
  "create_by" int4,
  "create_time" timestamp(6),
  "update_time" timestamp(6),
  "update_name" varchar(255) COLLATE "pg_catalog"."default",
  "update_by" int4
)
;

-- ----------------------------
-- Records of monitor_config
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_limit
-- ----------------------------
DROP TABLE IF EXISTS "public"."monitor_limit";
CREATE TABLE "public"."monitor_limit" (
  "limit_id" int4 NOT NULL DEFAULT nextval('monitor_limit_limit_id_seq'::regclass),
  "limit_name" varchar(255) COLLATE "pg_catalog"."default",
  "limit_mapping" varchar(255) COLLATE "pg_catalog"."default",
  "limit_permits" varchar(255) COLLATE "pg_catalog"."default",
  "limit_status" int4,
  "limit_type" varchar(255) COLLATE "pg_catalog"."default",
  "limit_resolver" varchar(255) COLLATE "pg_catalog"."default",
  "limit_profile" varchar(255) COLLATE "pg_catalog"."default",
  "limit_app" varchar(255) COLLATE "pg_catalog"."default",
  "limit_remote_list" varchar(255) COLLATE "pg_catalog"."default",
  "create_name" varchar(255) COLLATE "pg_catalog"."default",
  "create_by" int4,
  "create_time" timestamp(6),
  "update_time" timestamp(6),
  "update_name" varchar(255) COLLATE "pg_catalog"."default",
  "update_by" int4
)
;

-- ----------------------------
-- Records of monitor_limit
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_mybatis
-- ----------------------------
DROP TABLE IF EXISTS "public"."monitor_mybatis";
CREATE TABLE "public"."monitor_mybatis" (
  "monitor_mybatis_id" int4 NOT NULL DEFAULT nextval('monitor_mybatis_monitor_mybatis_id_seq'::regclass),
  "monitor_mybatis_name" varchar(255) COLLATE "pg_catalog"."default",
  "monitor_mybatis_desc" varchar(255) COLLATE "pg_catalog"."default",
  "monitor_mybatis_sql" varchar(255) COLLATE "pg_catalog"."default",
  "monitor_mybatis_sql_type" varchar(255) COLLATE "pg_catalog"."default",
  "monitor_mybatis_model_type" varchar(255) COLLATE "pg_catalog"."default",
  "monitor_mybatis_status" int4,
  "monitor_mybatis_mapper_type" varchar(255) COLLATE "pg_catalog"."default",
  "monitor_mybatis_profile" varchar(255) COLLATE "pg_catalog"."default",
  "monitor_appname" varchar(255) COLLATE "pg_catalog"."default",
  "create_name" varchar(255) COLLATE "pg_catalog"."default",
  "create_by" int4,
  "create_time" timestamp(6),
  "update_time" timestamp(6),
  "update_name" varchar(255) COLLATE "pg_catalog"."default",
  "update_by" int4
)
;

-- ----------------------------
-- Records of monitor_mybatis
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_patch
-- ----------------------------
DROP TABLE IF EXISTS "public"."monitor_patch";
CREATE TABLE "public"."monitor_patch" (
  "monitor_patch_id" int4 NOT NULL DEFAULT nextval('monitor_patch_monitor_patch_id_seq'::regclass),
  "monitor_patch_profile" varchar(255) COLLATE "pg_catalog"."default",
  "monitor_patch_app" varchar(255) COLLATE "pg_catalog"."default",
  "monitor_patch_name" varchar(255) COLLATE "pg_catalog"."default",
  "monitor_patch_chinese_name" varchar(255) COLLATE "pg_catalog"."default",
  "monitor_patch_pack" varchar(255) COLLATE "pg_catalog"."default",
  "monitor_patch_desc" varchar(255) COLLATE "pg_catalog"."default",
  "monitor_patch_version" varchar(255) COLLATE "pg_catalog"."default",
  "create_name" varchar(255) COLLATE "pg_catalog"."default",
  "create_by" int4,
  "create_time" timestamp(6),
  "update_time" timestamp(6),
  "update_name" varchar(255) COLLATE "pg_catalog"."default",
  "update_by" int4
)
;

-- ----------------------------
-- Records of monitor_patch
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_sys_gen
-- ----------------------------
DROP TABLE IF EXISTS "public"."monitor_sys_gen";
CREATE TABLE "public"."monitor_sys_gen" (
  "gen_id" int4 NOT NULL DEFAULT nextval('monitor_sys_gen_gen_id_seq'::regclass),
  "gen_name" varchar(255) COLLATE "pg_catalog"."default",
  "gen_type" varchar(255) COLLATE "pg_catalog"."default",
  "gen_user" varchar(255) COLLATE "pg_catalog"."default",
  "gen_password" varchar(255) COLLATE "pg_catalog"."default",
  "gen_host" varchar(255) COLLATE "pg_catalog"."default",
  "gen_port" int4,
  "gen_desc" varchar(255) COLLATE "pg_catalog"."default",
  "gen_driver" varchar(255) COLLATE "pg_catalog"."default",
  "gen_driver_url" varchar(255) COLLATE "pg_catalog"."default",
  "gen_database" varchar(255) COLLATE "pg_catalog"."default",
  "gen_database_file" varchar(255) COLLATE "pg_catalog"."default",
  "gen_uid" varchar(255) COLLATE "pg_catalog"."default"
)
;

-- ----------------------------
-- Records of monitor_sys_gen
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_sys_gen_column
-- ----------------------------
DROP TABLE IF EXISTS "public"."monitor_sys_gen_column";
CREATE TABLE "public"."monitor_sys_gen_column" (
  "col_id" int4 NOT NULL DEFAULT nextval('monitor_sys_gen_column_col_id_seq'::regclass),
  "tab_id" int4,
  "col_column_name" varchar(255) COLLATE "pg_catalog"."default",
  "col_column_comment" varchar(255) COLLATE "pg_catalog"."default",
  "col_column_type" varchar(255) COLLATE "pg_catalog"."default",
  "col_column_length" int4,
  "col_column_decimal" int4,
  "col_java_type" varchar(255) COLLATE "pg_catalog"."default",
  "col_java_field" varchar(255) COLLATE "pg_catalog"."default",
  "col_is_pk" varchar(255) COLLATE "pg_catalog"."default",
  "col_is_increment" varchar(255) COLLATE "pg_catalog"."default",
  "col_is_required" varchar(255) COLLATE "pg_catalog"."default",
  "col_is_insert" varchar(255) COLLATE "pg_catalog"."default",
  "col_is_edit" varchar(255) COLLATE "pg_catalog"."default",
  "col_is_list" varchar(255) COLLATE "pg_catalog"."default",
  "col_is_query" varchar(255) COLLATE "pg_catalog"."default",
  "col_query_type" varchar(255) COLLATE "pg_catalog"."default",
  "col_html_type" varchar(255) COLLATE "pg_catalog"."default",
  "col_dict_type" varchar(255) COLLATE "pg_catalog"."default",
  "col_sort" int4
)
;

-- ----------------------------
-- Records of monitor_sys_gen_column
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_sys_gen_remark
-- ----------------------------
DROP TABLE IF EXISTS "public"."monitor_sys_gen_remark";
CREATE TABLE "public"."monitor_sys_gen_remark" (
  "remark_id" int4 NOT NULL DEFAULT nextval('monitor_sys_gen_remark_remark_id_seq'::regclass),
  "remark_name" varchar(255) COLLATE "pg_catalog"."default",
  "gen_id" int4,
  "remark_table" varchar(255) COLLATE "pg_catalog"."default",
  "remark_database" varchar(255) COLLATE "pg_catalog"."default",
  "remark_column" varchar(255) COLLATE "pg_catalog"."default",
  "create_name" varchar(255) COLLATE "pg_catalog"."default",
  "create_by" int4,
  "create_time" timestamp(6),
  "update_time" timestamp(6),
  "update_name" varchar(255) COLLATE "pg_catalog"."default",
  "update_by" int4
)
;

-- ----------------------------
-- Records of monitor_sys_gen_remark
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_sys_gen_shell
-- ----------------------------
DROP TABLE IF EXISTS "public"."monitor_sys_gen_shell";
CREATE TABLE "public"."monitor_sys_gen_shell" (
  "shell_id" int4 NOT NULL DEFAULT nextval('monitor_sys_gen_shell_shell_id_seq'::regclass),
  "shell_name" varchar(255) COLLATE "pg_catalog"."default",
  "shell_script_path" varchar(255) COLLATE "pg_catalog"."default",
  "shell_log_path" varchar(255) COLLATE "pg_catalog"."default",
  "shell_args" varchar(255) COLLATE "pg_catalog"."default",
  "shell_desc" varchar(255) COLLATE "pg_catalog"."default",
  "shell_content" varchar(255) COLLATE "pg_catalog"."default",
  "gen_id" int4,
  "shell_status" int4 DEFAULT 0,
  "create_name" varchar(255) COLLATE "pg_catalog"."default",
  "create_by" int4,
  "create_time" timestamp(6),
  "update_time" timestamp(6),
  "update_name" varchar(255) COLLATE "pg_catalog"."default",
  "update_by" int4
)
;

-- ----------------------------
-- Records of monitor_sys_gen_shell
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_sys_gen_table
-- ----------------------------
DROP TABLE IF EXISTS "public"."monitor_sys_gen_table";
CREATE TABLE "public"."monitor_sys_gen_table" (
  "tab_id" int4 NOT NULL DEFAULT nextval('monitor_sys_gen_table_tab_id_seq'::regclass),
  "tab_name" varchar(255) COLLATE "pg_catalog"."default",
  "tab_desc" varchar(255) COLLATE "pg_catalog"."default",
  "gen_name" varchar(255) COLLATE "pg_catalog"."default",
  "gen_id" int4,
  "tab_class_name" varchar(255) COLLATE "pg_catalog"."default",
  "tab_package_name" varchar(255) COLLATE "pg_catalog"."default",
  "tab_module_name" varchar(255) COLLATE "pg_catalog"."default",
  "tab_business_name" varchar(255) COLLATE "pg_catalog"."default",
  "tab_function_name" varchar(255) COLLATE "pg_catalog"."default",
  "tab_function_author" varchar(255) COLLATE "pg_catalog"."default",
  "tab_gen_type" varchar(255) COLLATE "pg_catalog"."default",
  "tab_gen_path" varchar(255) COLLATE "pg_catalog"."default",
  "tab_remark" varchar(255) COLLATE "pg_catalog"."default",
  "tab_tpl_category" varchar(255) COLLATE "pg_catalog"."default"
)
;

-- ----------------------------
-- Records of monitor_sys_gen_table
-- ----------------------------

-- ----------------------------
-- Table structure for monitor_sys_gen_template
-- ----------------------------
DROP TABLE IF EXISTS "public"."monitor_sys_gen_template";
CREATE TABLE "public"."monitor_sys_gen_template" (
  "template_id" int4 NOT NULL DEFAULT nextval('monitor_sys_gen_template_template_id_seq'::regclass),
  "template_name" varchar(255) COLLATE "pg_catalog"."default",
  "template_type" varchar(255) COLLATE "pg_catalog"."default",
  "gen_id" int4,
  "template_path" varchar(255) COLLATE "pg_catalog"."default",
  "template_content" varchar(255) COLLATE "pg_catalog"."default",
  "create_name" varchar(255) COLLATE "pg_catalog"."default",
  "create_by" int4,
  "create_time" timestamp(6),
  "update_time" timestamp(6),
  "update_name" varchar(255) COLLATE "pg_catalog"."default",
  "update_by" int4
)
;

-- ----------------------------
-- Records of monitor_sys_gen_template
-- ----------------------------

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."monitor_app_monitor_id_seq"
OWNED BY "public"."monitor_app"."monitor_id";
SELECT setval('"public"."monitor_app_monitor_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."monitor_config_config_id_seq"
OWNED BY "public"."monitor_config"."config_id";
SELECT setval('"public"."monitor_config_config_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."monitor_limit_limit_id_seq"
OWNED BY "public"."monitor_limit"."limit_id";
SELECT setval('"public"."monitor_limit_limit_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."monitor_mybatis_monitor_mybatis_id_seq"
OWNED BY "public"."monitor_mybatis"."monitor_mybatis_id";
SELECT setval('"public"."monitor_mybatis_monitor_mybatis_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."monitor_patch_monitor_patch_id_seq"
OWNED BY "public"."monitor_patch"."monitor_patch_id";
SELECT setval('"public"."monitor_patch_monitor_patch_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."monitor_sys_gen_column_col_id_seq"
OWNED BY "public"."monitor_sys_gen_column"."col_id";
SELECT setval('"public"."monitor_sys_gen_column_col_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."monitor_sys_gen_gen_id_seq"
OWNED BY "public"."monitor_sys_gen"."gen_id";
SELECT setval('"public"."monitor_sys_gen_gen_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."monitor_sys_gen_remark_remark_id_seq"
OWNED BY "public"."monitor_sys_gen_remark"."remark_id";
SELECT setval('"public"."monitor_sys_gen_remark_remark_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."monitor_sys_gen_shell_shell_id_seq"
OWNED BY "public"."monitor_sys_gen_shell"."shell_id";
SELECT setval('"public"."monitor_sys_gen_shell_shell_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."monitor_sys_gen_table_tab_id_seq"
OWNED BY "public"."monitor_sys_gen_table"."tab_id";
SELECT setval('"public"."monitor_sys_gen_table_tab_id_seq"', 1, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."monitor_sys_gen_template_template_id_seq"
OWNED BY "public"."monitor_sys_gen_template"."template_id";
SELECT setval('"public"."monitor_sys_gen_template_template_id_seq"', 1, false);

-- ----------------------------
-- Primary Key structure for table monitor_app
-- ----------------------------
ALTER TABLE "public"."monitor_app" ADD CONSTRAINT "monitor_app_pkey" PRIMARY KEY ("monitor_id");

-- ----------------------------
-- Primary Key structure for table monitor_config
-- ----------------------------
ALTER TABLE "public"."monitor_config" ADD CONSTRAINT "monitor_config_pkey" PRIMARY KEY ("config_id");

-- ----------------------------
-- Primary Key structure for table monitor_limit
-- ----------------------------
ALTER TABLE "public"."monitor_limit" ADD CONSTRAINT "monitor_limit_pkey" PRIMARY KEY ("limit_id");

-- ----------------------------
-- Primary Key structure for table monitor_mybatis
-- ----------------------------
ALTER TABLE "public"."monitor_mybatis" ADD CONSTRAINT "monitor_mybatis_pkey" PRIMARY KEY ("monitor_mybatis_id");

-- ----------------------------
-- Primary Key structure for table monitor_patch
-- ----------------------------
ALTER TABLE "public"."monitor_patch" ADD CONSTRAINT "monitor_patch_pkey" PRIMARY KEY ("monitor_patch_id");

-- ----------------------------
-- Primary Key structure for table monitor_sys_gen
-- ----------------------------
ALTER TABLE "public"."monitor_sys_gen" ADD CONSTRAINT "monitor_sys_gen_pkey" PRIMARY KEY ("gen_id");

-- ----------------------------
-- Primary Key structure for table monitor_sys_gen_column
-- ----------------------------
ALTER TABLE "public"."monitor_sys_gen_column" ADD CONSTRAINT "monitor_sys_gen_column_pkey" PRIMARY KEY ("col_id");

-- ----------------------------
-- Primary Key structure for table monitor_sys_gen_remark
-- ----------------------------
ALTER TABLE "public"."monitor_sys_gen_remark" ADD CONSTRAINT "monitor_sys_gen_remark_pkey" PRIMARY KEY ("remark_id");

-- ----------------------------
-- Primary Key structure for table monitor_sys_gen_shell
-- ----------------------------
ALTER TABLE "public"."monitor_sys_gen_shell" ADD CONSTRAINT "monitor_sys_gen_shell_pkey" PRIMARY KEY ("shell_id");

-- ----------------------------
-- Primary Key structure for table monitor_sys_gen_table
-- ----------------------------
ALTER TABLE "public"."monitor_sys_gen_table" ADD CONSTRAINT "monitor_sys_gen_table_pkey" PRIMARY KEY ("tab_id");

-- ----------------------------
-- Primary Key structure for table monitor_sys_gen_template
-- ----------------------------
ALTER TABLE "public"."monitor_sys_gen_template" ADD CONSTRAINT "monitor_sys_gen_template_pkey" PRIMARY KEY ("template_id");
