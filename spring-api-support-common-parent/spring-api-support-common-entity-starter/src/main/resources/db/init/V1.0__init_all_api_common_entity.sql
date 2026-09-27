-- =====================================================================
-- V1.0__init_all_api_common_entity.sql - spring-api-support-common-entity-starter 全量建表（列定义由实体注解生成）
-- 通道: INIT 表结构；全部 CREATE TABLE IF NOT EXISTS，可重复执行。
-- 方言: MySQL 8 / H2 MySQL 模式通用子集。
-- =====================================================================

CREATE TABLE IF NOT EXISTS `sys_device` (
    `sys_device_id` int NOT NULL AUTO_INCREMENT,
    `sys_project_id` int NULL DEFAULT NULL,
    `sys_dept_id` int NULL DEFAULT NULL,
    `sys_device_name` varchar(255) NULL DEFAULT NULL,
    `sys_device_serial_number` varchar(255) NULL DEFAULT NULL,
    `sys_device_org_code` varchar(255) NULL DEFAULT NULL,
    `sys_device_org_name` varchar(255) NULL DEFAULT NULL,
    `sys_device_channels` varchar(255) NULL DEFAULT NULL,
    `sys_device_main_subtype` varchar(255) NULL DEFAULT NULL,
    `sys_device_sub_subtype` varchar(255) NULL DEFAULT NULL,
    `sys_device_status` int NULL DEFAULT NULL,
    `sys_device_online` int NULL DEFAULT NULL,
    `sys_device_position` varchar(255) NULL DEFAULT NULL,
    `sys_device_resource_type` varchar(255) NULL DEFAULT NULL,
    `sys_device_description` varchar(1000) NULL DEFAULT NULL,
    `sys_device_version` varchar(255) NULL DEFAULT NULL,
    `sys_device_validate_code` varchar(255) NULL DEFAULT NULL,
    `sys_device_type` varchar(255) NULL DEFAULT NULL,
    `sys_device_account` varchar(255) NULL DEFAULT NULL,
    `sys_device_password` varchar(255) NULL DEFAULT NULL,
    `sys_device_rtsp` varchar(255) NULL DEFAULT NULL,
    `sys_device_rtsp_webrtc` varchar(255) NULL DEFAULT NULL,
    `sys_device_net_address` varchar(255) NULL DEFAULT NULL,
    `sys_device_net_physical_address` varchar(255) NULL DEFAULT NULL,
    `sys_device_latitude` decimal(18, 4) NULL DEFAULT NULL,
    `sys_device_longitude` decimal(18, 4) NULL DEFAULT NULL,
    `sys_device_owner` varchar(255) NULL DEFAULT NULL,
    `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_device_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `sys_device_camera_record` (
    `sys_device_camera_record_id` int NOT NULL AUTO_INCREMENT,
    `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_device_camera_record_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `sys_device_card_event` (
    `sys_device_card_event_id` int NOT NULL AUTO_INCREMENT,
    `sys_device_card_event_time` datetime NULL DEFAULT NULL,
    `sys_device_serial_number` varchar(255) NULL DEFAULT NULL,
    `sys_device_card_event_person_involved` varchar(255) NULL DEFAULT NULL,
    `sys_device_card_event_person_involved_name` varchar(255) NULL DEFAULT NULL,
    `sys_device_card_event_major_name` varchar(255) NULL DEFAULT NULL,
    `sys_device_card_event_minor_name` varchar(255) NULL DEFAULT NULL,
    `sys_device_card_event_message_id` varchar(255) NULL DEFAULT NULL,
    `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_device_card_event_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `sys_device_channel` (
    `sys_device_channel_id` int NOT NULL AUTO_INCREMENT,
    `sys_device_channel_name` varchar(255) NULL DEFAULT NULL,
    `sys_device_channel_no` int NULL DEFAULT NULL,
    `sys_device_serial_number` varchar(255) NULL DEFAULT NULL,
    `sys_device_channel_type` varchar(255) NULL DEFAULT NULL,
    `sys_device_channel_status` int NULL DEFAULT NULL,
    `sys_device_channel_use` int NULL DEFAULT NULL,
    `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_device_channel_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `sys_device_message_event` (
    `sys_device_message_event_id` int NOT NULL AUTO_INCREMENT,
    `sys_device_message_event_type` varchar(255) NULL DEFAULT NULL,
    `sys_device_message_event_event_id` varchar(255) NULL DEFAULT NULL,
    `sys_device_message_event_data_time` datetime NULL DEFAULT NULL,
    `sys_device_message_event_employee_no` varchar(255) NULL DEFAULT NULL,
    `sys_device_message_event_employee_name` varchar(255) NULL DEFAULT NULL,
    `sys_device_message_event_card_no` varchar(255) NULL DEFAULT NULL,
    `sys_device_serial_number` varchar(255) NULL DEFAULT NULL,
    `sys_device_name` varchar(255) NULL DEFAULT NULL,
    `sys_device_message_event_channel_no` varchar(255) NULL DEFAULT NULL,
    `sys_device_message_event_location` varchar(255) NULL DEFAULT NULL,
    `sys_device_message_event_channel_name` varchar(255) NULL DEFAULT NULL,
    `sys_device_message_event_card_reader_kind` varchar(255) NULL DEFAULT NULL,
    `sys_device_message_event_card_reader_no` int NULL DEFAULT NULL,
    `sys_device_message_event_door_no` varchar(255) NULL DEFAULT NULL,
    `sys_device_message_event_event_type` varchar(255) NULL DEFAULT NULL,
    `sys_device_message_event_event_type_name` varchar(255) NULL DEFAULT NULL,
    `sys_device_message_event_event_code` varchar(255) NULL DEFAULT NULL,
    `sys_device_message_event_event_description` varchar(1000) NULL DEFAULT NULL,
    `sys_device_message_event_event_source` varchar(255) NULL DEFAULT NULL,
    `sys_device_message_event_event_picture_url` varchar(255) NULL DEFAULT NULL,
    `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_device_message_event_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `sys_device_org` (
    `sys_device_org_id` varchar(255) NOT NULL,
    `sys_project_id` int NULL DEFAULT NULL,
    `sys_device_org_name` varchar(255) NULL DEFAULT NULL,
    `sys_device_org_pid` varchar(255) NULL DEFAULT NULL,
    `sys_device_org_tree_path` varchar(255) NULL DEFAULT NULL,
    `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_device_org_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `sys_devops_project` (
    `project_id` bigint NOT NULL AUTO_INCREMENT,
    `project_name` varchar(255) NULL DEFAULT NULL,
    `project_description` varchar(1000) NULL DEFAULT NULL,
    `project_icon` varchar(255) NULL DEFAULT NULL,
    `server_os` varchar(255) NULL DEFAULT NULL,
    `sort_order` int NULL DEFAULT NULL,
    `status` int NULL DEFAULT NULL,
    `server_type` varchar(255) NULL DEFAULT NULL,
    `server_host` varchar(255) NULL DEFAULT NULL,
    `server_port` int NULL DEFAULT NULL,
    `server_username` varchar(255) NULL DEFAULT NULL,
    `server_password` varchar(255) NULL DEFAULT NULL,
    `server_private_key` varchar(255) NULL DEFAULT NULL,
    `server_connect_timeout` int NULL DEFAULT NULL,
    `server_enabled` int NULL DEFAULT NULL,
    `server_activated_at` datetime NULL DEFAULT NULL,
    `log_path` varchar(255) NULL DEFAULT NULL,
    `config_path` varchar(1000) NULL DEFAULT NULL,
    `lib_path` varchar(255) NULL DEFAULT NULL,
    `root_path` varchar(255) NULL DEFAULT NULL,
    `start_script` varchar(255) NULL DEFAULT NULL,
    `stop_script` varchar(255) NULL DEFAULT NULL,
    `restart_script` varchar(255) NULL DEFAULT NULL,
    `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`project_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `sys_email_log` (
    `sys_email_log_id` int NOT NULL AUTO_INCREMENT,
    `sys_email_log_target` varchar(255) NULL DEFAULT NULL,
    `sys_email_log_template_id` varchar(255) NULL DEFAULT NULL,
    `sys_email_log_param` varchar(255) NULL DEFAULT NULL,
    `sys_email_cost` decimal(18, 4) NULL DEFAULT NULL,
    `sys_email_code` varchar(255) NULL DEFAULT NULL,
    `sys_email_message` varchar(255) NULL DEFAULT NULL,
    `sys_email_log_content` varchar(1000) NULL DEFAULT NULL,
    `sys_email_app_type` varchar(255) NULL DEFAULT NULL,
    `sys_email_app_id` varchar(255) NULL DEFAULT NULL,
    `sys_email_app_sign` varchar(255) NULL DEFAULT NULL,
    `sys_project_id` int NULL DEFAULT NULL,
    `sys_email_read` int NULL DEFAULT NULL,
    `create_name` varchar(255) NULL DEFAULT NULL,
    `create_by` int NULL DEFAULT NULL,
    `create_time` datetime NULL DEFAULT NULL,
    `update_time` datetime NULL DEFAULT NULL,
    `update_name` varchar(255) NULL DEFAULT NULL,
    `update_by` int NULL DEFAULT NULL,
    PRIMARY KEY (`sys_email_log_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `sys_email_template` (
    `sys_email_template_id` int NOT NULL AUTO_INCREMENT,
    `sys_project_id` int NULL DEFAULT NULL,
    `sys_project_type` int NULL DEFAULT NULL,
    `sys_email_template_sort` int NULL DEFAULT NULL,
    `sys_email_template_category` int NULL DEFAULT NULL,
    `sys_email_template_name` varchar(255) NULL DEFAULT NULL,
    `sys_email_template_callback_url` varchar(255) NULL DEFAULT NULL,
    `sys_email_template_code` varchar(255) NULL DEFAULT NULL,
    `sys_email_template_content` varchar(1000) NULL DEFAULT NULL,
    `sys_email_template_delete` int NULL DEFAULT NULL,
    `sys_email_template_status` int NULL DEFAULT NULL,
    `sys_email_template_type` int NULL DEFAULT NULL,
    `sys_email_template_default` int NULL DEFAULT NULL,
    `sys_email_template_approve` int NULL DEFAULT NULL,
    `sys_email_template_approve_reject` varchar(255) NULL DEFAULT NULL,
    `sys_email_template_approve_create_by` int NULL DEFAULT NULL,
    `sys_email_template_disabled` int NULL DEFAULT NULL,
    `sys_email_template_origin` varchar(255) NULL DEFAULT NULL,
    `sys_email_template_origin_uuid` varchar(255) NULL DEFAULT NULL,
    `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_email_template_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `sys_message_history` (
    `sys_message_history_id` int NOT NULL AUTO_INCREMENT,
    `sys_message_history_message_id` int NULL DEFAULT NULL,
    `sys_message_history_title` varchar(255) NULL DEFAULT NULL,
    `sys_message_history_content` varchar(1000) NULL DEFAULT NULL,
    `sys_message_history_type` varchar(255) NULL DEFAULT NULL,
    `sys_message_history_level` varchar(255) NULL DEFAULT NULL,
    `sys_message_history_sender_id` int NULL DEFAULT NULL,
    `sys_message_history_sender_name` varchar(255) NULL DEFAULT NULL,
    `sys_message_history_receiver_id` int NULL DEFAULT NULL,
    `sys_message_history_receiver_name` varchar(255) NULL DEFAULT NULL,
    `sys_message_history_send_time` datetime NULL DEFAULT NULL,
    `sys_message_history_read_time` datetime NULL DEFAULT NULL,
    `sys_message_history_biz_type` varchar(255) NULL DEFAULT NULL,
    `sys_message_history_biz_id` varchar(255) NULL DEFAULT NULL,
    `sys_message_history_url` varchar(255) NULL DEFAULT NULL,
    `sys_message_history_extra` varchar(1000) NULL DEFAULT NULL,
    `history` text NULL DEFAULT NULL,
    `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_message_history_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `sys_project` (
    `sys_project_id` int NOT NULL AUTO_INCREMENT,
    `sys_project_name` varchar(255) NULL DEFAULT NULL,
    `sys_project_icon` varchar(255) NULL DEFAULT NULL,
    `sys_project_group` varchar(255) NULL DEFAULT NULL,
    `sys_project_code` varchar(255) NULL DEFAULT NULL,
    `sys_project_app_id` varchar(255) NULL DEFAULT NULL,
    `sys_project_app_secret` varchar(255) NULL DEFAULT NULL,
    `sys_project_sign` varchar(255) NULL DEFAULT NULL,
    `sys_project_app_key` varchar(255) NULL DEFAULT NULL,
    `sys_project_sign_code` varchar(255) NULL DEFAULT NULL,
    `sys_project_delete` int NULL DEFAULT NULL,
    `sys_project_status` int NULL DEFAULT NULL,
    `sys_project_cdn` varchar(255) NULL DEFAULT NULL,
    `sys_project_endpoint` varchar(255) NULL DEFAULT NULL,
    `sys_project_local_path` varchar(255) NULL DEFAULT NULL,
    `sys_project_api_address` varchar(255) NULL DEFAULT NULL,
    `sys_project_location_temp_path` varchar(255) NULL DEFAULT NULL,
    `sys_project_location_oss_address` varchar(255) NULL DEFAULT NULL,
    `sys_project_vender` varchar(255) NULL DEFAULT NULL,
    `sys_project_sort` int NULL DEFAULT NULL,
    `sys_project_remark` varchar(1000) NULL DEFAULT NULL,
    `sys_project_function` varchar(255) NULL DEFAULT NULL,
    `sys_project_smtp_host` varchar(255) NULL DEFAULT NULL,
    `sys_project_smtp_port` int NULL DEFAULT NULL,
    `sys_project_smtp_password` varchar(255) NULL DEFAULT NULL,
    `sys_project_smtp_from` varchar(255) NULL DEFAULT NULL,
    `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_project_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `sys_project_default` (
    `sys_project_default_id` int NOT NULL AUTO_INCREMENT,
    `sys_project_default_type` varchar(255) NULL DEFAULT NULL,
    `sys_project_id` int NULL DEFAULT NULL,
    PRIMARY KEY (`sys_project_default_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `sys_secret` (
    `sys_secret_id` int NOT NULL AUTO_INCREMENT,
    `sys_project_id` int NULL DEFAULT NULL,
    `sys_secret_key` varchar(255) NULL DEFAULT NULL,
    `sys_secret_value` varchar(255) NULL DEFAULT NULL,
    `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_secret_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `sys_sms_template` (
    `sys_sms_template_id` int NOT NULL AUTO_INCREMENT,
    `sys_project_id` int NULL DEFAULT NULL,
    `sys_project_type` int NULL DEFAULT NULL,
    `sys_sms_template_sort` int NULL DEFAULT NULL,
    `sys_sms_template_category` int NULL DEFAULT NULL,
    `sys_sms_template_name` varchar(255) NULL DEFAULT NULL,
    `sys_sms_template_code` varchar(255) NULL DEFAULT NULL,
    `sys_sms_template_content` varchar(1000) NULL DEFAULT NULL,
    `sys_sms_template_delete` int NULL DEFAULT NULL,
    `sys_sms_template_status` int NULL DEFAULT NULL,
    `sys_sms_template_type` int NULL DEFAULT NULL,
    `sys_sms_template_default` int NULL DEFAULT NULL,
    `sys_sms_template_approve` int NULL DEFAULT NULL,
    `sys_sms_template_approve_reject` varchar(255) NULL DEFAULT NULL,
    `sys_sms_template_approve_create_by` int NULL DEFAULT NULL,
    `sys_sms_template_disabled` int NULL DEFAULT NULL,
    `sys_sms_template_origin` varchar(255) NULL DEFAULT NULL,
    `sys_sms_template_origin_uuid` varchar(255) NULL DEFAULT NULL,
    `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_sms_template_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci;

-- =====================================================================
-- 归口补齐：实体类在本模块（common-entity-starter），但 DDL 此前散落在
-- 未被部署的模块里，导致全新库缺表、相关接口 500。
--   sys_message_template / sys_message_log
--       原在 spring-support-message-starter（在本 reactor，但不是
--       spring-api-support-system-starter 的依赖，不进 lib/，
--       其 db/init SQL 永远不会被迁移器扫到）
--   app_id / sys_app_key
--       原在 spring-api-support-oauth-starter（同样不在 lib/）
--   sys_dept_principal_rel / sys_user_config / sys_user_layout /
--   sys_user_third_party / sys_user_avatar_frame
--       原在 V1.0__init_all_api_system.sql，用户域下沉到
--       common-system-starter 时被漏掉
-- 规约：建表必须随实体代码一起归口，否则嵌入本模块的宿主起不来。
-- 内容与原出处逐字一致，此处只做位置归口，不改结构。
-- =====================================================================

-- =====================================================
-- sys_dept_principal_rel
-- =====================================================
CREATE TABLE IF NOT EXISTS `sys_dept_principal_rel` (
    `sys_dept_principal_rel_id` INT NOT NULL AUTO_INCREMENT COMMENT '关系ID - 主键自增',
    `sys_dept_id` INT NOT NULL COMMENT '部门ID - 关联组织机构表',
    `sys_user_id` INT NOT NULL COMMENT '负责人用户ID - 关联用户表',
    PRIMARY KEY (`sys_dept_principal_rel_id`),
    UNIQUE INDEX `sys_dept_principal_rel_uk_dept_user` (`sys_dept_id`, `sys_user_id`),
    INDEX `sys_dept_principal_rel_idx_principal_user_id` (`sys_user_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '部门负责人关系表 - 存储部门负责人的多对多关系';

-- =====================================================
-- sys_user_config
-- =====================================================
CREATE TABLE IF NOT EXISTS `sys_user_config` (
    `sys_user_config_id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `sys_user_id` INT NOT NULL COMMENT '用户ID - 关联用户表',
    `sys_user_config_key` varchar(255) NULL DEFAULT NULL COMMENT '配置键',
    `sys_user_config_value` varchar(255) NULL DEFAULT NULL COMMENT '配置值',
    `sys_user_config_type` int NULL DEFAULT NULL COMMENT '配置类型',
    `sys_user_config_wx_openid` varchar(255) NULL DEFAULT NULL COMMENT '微信 openid',
    `sys_user_config_wx_unionid` varchar(255) NULL DEFAULT NULL COMMENT '微信 unionid',
    `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_user_config_id`),
    INDEX `sys_user_config_idx_user` (`sys_user_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '用户配置表 - 存储用户的个性化配置';

-- =====================================================
-- sys_user_layout
-- =====================================================
CREATE TABLE IF NOT EXISTS `sys_user_layout` (
    `sys_layout_id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `sys_layout_layout` text NULL COMMENT '布局内容',
    `sys_layout_grid` text NULL COMMENT '栅格内容',
    `sys_layout_component` text NULL COMMENT '组件内容',
    `sys_layout_user_id` INT NULL DEFAULT NULL COMMENT '用户ID',
    `sys_layout_preference` text NULL COMMENT '偏好设置',
    `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_layout_id`),
    INDEX `sys_user_layout_idx_user` (`sys_layout_user_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '用户布局表 - 存储用户的个性化布局';

-- =====================================================
-- sys_user_third_party
-- =====================================================
CREATE TABLE IF NOT EXISTS `sys_user_third_party`  (
    `sys_user_third_party_id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `sys_user_id` INT NOT NULL COMMENT '用户ID - 关联用户表',
    `sys_user_third_party_type` varchar(64) NULL DEFAULT NULL COMMENT '第三方类型 - WECHAT/GITHUB/QQ等',
    `sys_user_third_party_name` varchar(255) NULL DEFAULT NULL COMMENT '第三方名称',
    `sys_user_third_party_value` varchar(255) NULL DEFAULT NULL COMMENT '第三方标识值',
    `sys_user_third_party_uid` varchar(128) NULL DEFAULT NULL COMMENT '第三方 uid',
    `sys_user_third_party_open_id` varchar(128) NULL DEFAULT NULL COMMENT '第三方唯一标识',
    `sys_user_third_party_union_id` varchar(128) NULL DEFAULT NULL COMMENT '第三方unionId',
    `sys_user_third_party_nick_name` varchar(255) NULL DEFAULT NULL COMMENT '第三方昵称',
    `sys_user_third_party_avatar` varchar(512) NULL DEFAULT NULL COMMENT '第三方头像',
    `sys_user_third_party_status` int NULL DEFAULT 1 COMMENT '状态 - 0:未绑定, 1:已绑定, 2:已解绑',
    `sys_user_third_party_bind_time` datetime NULL DEFAULT NULL COMMENT '绑定时间',
    `sys_user_third_party_unbind_time` datetime NULL DEFAULT NULL COMMENT '解绑时间',
    `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_user_third_party_id`),
    INDEX `sys_user_third_party_idx_user` (`sys_user_id`),
    INDEX `sys_user_third_party_idx_open_id` (`sys_user_third_party_open_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '用户第三方绑定表';

-- =====================================================
-- sys_user_avatar_frame
-- =====================================================
CREATE TABLE IF NOT EXISTS `sys_user_avatar_frame` (
    `sys_user_avatar_frame_id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `sys_user_id` INT NULL DEFAULT NULL COMMENT '用户ID - 关联用户表',
    `sys_avatar_frame_id` INT NULL DEFAULT NULL COMMENT '头像框ID - 关联头像框配置',
    `acquire_time` datetime NULL DEFAULT NULL COMMENT '获得时间',
    `expire_time` datetime NULL DEFAULT NULL COMMENT '过期时间',
    `sys_user_avatar_frame_name` varchar(255) NULL DEFAULT NULL COMMENT '头像框名称',
    `sys_user_avatar_frame_code` varchar(255) NULL DEFAULT NULL COMMENT '头像框编码',
    `sys_user_avatar_frame_url` varchar(512) NULL DEFAULT NULL COMMENT '头像框资源地址',
    `sys_user_avatar_frame_status` int NULL DEFAULT 1 COMMENT '状态 - 0:禁用, 1:启用',
    `sys_user_avatar_frame_sort` int NULL DEFAULT 0 COMMENT '排序',
    `sys_user_avatar_frame_price` int NULL DEFAULT 0 COMMENT '价格 - 0:免费',
    `sys_user_avatar_frame_remark` varchar(512) NULL DEFAULT NULL COMMENT '备注',
    `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_user_avatar_frame_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '用户头像框表';

-- =====================================================
-- sys_message_template
-- =====================================================
CREATE TABLE IF NOT EXISTS `sys_message_template` (

    `sys_message_template_id` int NOT NULL AUTO_INCREMENT COMMENT '模板ID - 主键自增',
    `sys_message_provider_id` int NULL DEFAULT NULL COMMENT '提供商ID - 关联消息提供商',
    `sys_message_template_code` varchar(255) NULL DEFAULT NULL COMMENT '模板编码 - 唯一标识',
    `sys_message_template_name` varchar(255) NULL DEFAULT NULL COMMENT '模板名称',
    `sys_message_template_title` varchar(255) NULL DEFAULT NULL COMMENT '模板标题(支持变量)',
    `sys_message_template_type` varchar(64) NULL DEFAULT NULL COMMENT '模板类型 - EMAIL/SMS/WECHAT/DINGDING/FEISHU',
    `sys_message_channel` varchar(64) NULL DEFAULT NULL COMMENT '消息渠道',
    `sys_message_external_code` varchar(255) NULL DEFAULT NULL COMMENT '外部渠道模板编码',
    `sys_message_param_schema` varchar(255) NULL DEFAULT NULL COMMENT '参数结构定义(JSON Schema)',
    `sys_message_template_category` int NULL DEFAULT NULL COMMENT '模板分类',
    `sys_message_template_content` text NULL COMMENT '模板内容 - 模板内容，变量用${var}表示',
    `sys_message_template_callback_url` varchar(512) NULL DEFAULT NULL COMMENT '回调地址 - 邮件状态回调URL',
    `sys_message_template_delete` int NULL DEFAULT 0 COMMENT '是否删除 - 0:未删除, 1:已删除',
    `sys_message_template_status` int NULL DEFAULT 1 COMMENT '是否启用 - 0:禁用, 1:启用',
    `sys_message_template_sort` int NULL DEFAULT 0 COMMENT '模板优先级',
    `sys_message_template_default` int NULL DEFAULT 0 COMMENT '是否默认 - 0:否, 1:是',
    `sys_message_template_remark` varchar(512) NULL DEFAULT NULL COMMENT '备注说明',
    `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_message_template_id`),
    KEY `idx_template_provider` (`sys_message_provider_id`),
    KEY `idx_template_code` (`sys_message_template_code`),
    KEY `idx_template_status` (`sys_message_template_status`)
) ENGINE=InnoDB;

-- =====================================================
-- sys_message_log
-- =====================================================
CREATE TABLE IF NOT EXISTS `sys_message_log` (

    `sys_message_log_id` int NOT NULL AUTO_INCREMENT COMMENT '日志ID - 主键自增',
    `sys_message_log_target` varchar(255) NULL DEFAULT NULL COMMENT '接收人 - 手机号/邮箱/用户ID等',
    `sys_message_template_id` int NULL DEFAULT NULL COMMENT '模板ID - 使用的模板',
    `sys_message_provider_id` int NULL DEFAULT NULL COMMENT '提供商ID - 使用的消息提供商',
    `sys_message_log_channel` varchar(64) NULL DEFAULT NULL COMMENT '渠道标识 - 实际发送渠道',
    `sys_message_log_content` longtext NULL COMMENT '发送内容 - 最终发送的消息内容',
    `sys_message_log_param` longtext NULL COMMENT '模板参数 - JSON格式的模板变量',
    `sys_message_cost` decimal(10, 2) NULL DEFAULT NULL COMMENT '耗时 - 发送耗时(毫秒)',
    `sys_message_code` varchar(64) NULL DEFAULT NULL COMMENT '返回码 - SUCCESS/FAILURE',
    `sys_message_message` text NULL COMMENT '返回消息 - 错误信息',
    `sys_message_log_subject` varchar(512) NULL DEFAULT NULL COMMENT '消息主题 - 邮件标题等',
    `sys_message_log_message_id` varchar(255) NULL DEFAULT NULL COMMENT '消息ID - 服务商返回的消息标识',
    `sys_message_batch_id` varchar(64) NULL DEFAULT NULL COMMENT '批次号 - 同一次发送请求的接收人共享',
    `sys_message_track_token` varchar(64) NULL DEFAULT NULL COMMENT '追踪标识 - 邮件阅读追踪token，用于回写已读状态',
    `sys_message_read_status` int NULL DEFAULT 0 COMMENT '已读状态 - 0:未读, 1:已读',
    `sys_message_read_time` datetime NULL DEFAULT NULL COMMENT '已读时间 - 收件人打开邮件触发追踪的时间',
    `sys_message_log_delete` int NULL DEFAULT 0 COMMENT '是否删除 - 0:未删除, 1:已删除',
    `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_message_log_id`),
    KEY `idx_log_target` (`sys_message_log_target`),
    KEY `idx_log_template` (`sys_message_template_id`),
    KEY `idx_log_provider` (`sys_message_provider_id`),
    KEY `idx_log_create_time` (`create_time`),
    KEY `idx_log_track_token` (`sys_message_track_token`),
    KEY `idx_log_batch` (`sys_message_batch_id`)
) ENGINE=InnoDB;

-- =====================================================
-- app_id
-- =====================================================
CREATE TABLE IF NOT EXISTS `app_id` (
    `app_id_id` int NOT NULL AUTO_INCREMENT COMMENT 'AppId ID - 主键自增',
    `app_id_value` varchar(100) NOT NULL COMMENT 'AppId值 - 应用唯一标识符，全局唯一',
    `app_id_name` varchar(100) NULL DEFAULT NULL COMMENT 'AppId名称 - 应用的显示名称',
    `app_id_description` varchar(500) NULL DEFAULT NULL COMMENT '应用描述 - 应用的详细说明',
    `app_id_status` int NULL DEFAULT 1 COMMENT '应用状态 - 0:禁用, 1:启用',
    `app_id_expire_time` bigint NULL DEFAULT NULL COMMENT '过期时间 - Unix时间戳(毫秒)，NULL表示永不过期',
    `app_id_last_use_time` bigint NULL DEFAULT NULL COMMENT '最后使用时间 - Unix时间戳(毫秒)，最近一次使用该AppId的时间',
    `app_id_use_count` bigint NULL DEFAULT 0 COMMENT '使用次数 - 该AppId累计被调用的次数',
    `user_id` int NULL DEFAULT NULL COMMENT '关联用户ID - 该AppId所属的用户ID',
    `app_id_scope` varchar(500) NULL DEFAULT NULL COMMENT '允许的权限范围 - 多个scope用逗号分隔',
    `app_id_grant_types` varchar(500) NULL DEFAULT NULL COMMENT '允许的授权类型 - 多个用逗号分隔',
    `app_id_redirect_uris` varchar(1000) NULL DEFAULT NULL COMMENT '允许的回调地址 - 多个用逗号分隔',
    `app_id_remark` varchar(500) NULL DEFAULT NULL COMMENT '备注信息',
    `delete_flag` int NULL DEFAULT 0 COMMENT '删除标记 - 0:未删除, 1:已删除',
    `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`app_id_id`),
    UNIQUE INDEX `app_id_uk_app_id_value` (`app_id_value`),
    INDEX `app_id_idx_status` (`app_id_status`),
    INDEX `app_id_idx_expire_time` (`app_id_expire_time`),
    INDEX `app_id_idx_user_id` (`user_id`),
    INDEX `app_id_idx_delete_flag` (`delete_flag`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci
COMMENT = 'AppId应用标识表 - 存储AppId相关信息';

-- =====================================================
-- sys_app_key
-- =====================================================
CREATE TABLE IF NOT EXISTS `sys_app_key` (
    `sys_app_key_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `sys_app_key_app_id` varchar(64) NULL DEFAULT NULL COMMENT '应用ID',
    `app_key` varchar(100) NOT NULL COMMENT 'AppKey - 应用访问密钥',
    `sys_app_key_secret` varchar(100) NULL DEFAULT NULL COMMENT 'AppSecret - 应用访问密钥（需加密存储）',
    `sys_app_key_name` varchar(100) NULL DEFAULT NULL COMMENT '应用名称',
    `sys_user_id` int NULL DEFAULT NULL COMMENT '用户ID',
    `sys_app_key_status` int NULL DEFAULT 0 COMMENT '状态：0-禁用，1-启用',
    `sys_app_key_delete` int NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    `sys_app_key_valid_days` int NOT NULL DEFAULT 30 COMMENT '有效天数，0表示不限时',
    `sys_app_key_expire_time` bigint NULL DEFAULT NULL COMMENT '过期时间 - Unix时间戳(秒)',
    `sys_app_key_ip_whitelist` varchar(1000) NULL DEFAULT NULL COMMENT 'IP白名单',
    `sys_app_key_remark` varchar(500) NULL DEFAULT NULL COMMENT '备注',
    `sys_app_key_last_used_time` bigint NULL DEFAULT NULL COMMENT '最后使用时间',
    `sys_app_key_use_count` bigint NULL DEFAULT NULL COMMENT '使用次数',
    `sys_app_key_scope` varchar(500) NULL DEFAULT NULL COMMENT '允许的权限范围，多个用逗号分隔',
    `sys_app_key_grant_types` varchar(500) NULL DEFAULT NULL COMMENT '允许的授权类型，多个用逗号分隔',
    `sys_app_key_redirect_uris` varchar(1000) NULL DEFAULT NULL COMMENT '允许的回调地址，多个用逗号分隔',
    `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_app_key_id`),
    UNIQUE INDEX `sys_app_key_uk_app_key` (`app_key`),
    INDEX `sys_app_key_idx_app_id` (`sys_app_key_app_id`),
    INDEX `sys_app_key_idx_user_id` (`sys_user_id`),
    INDEX `sys_app_key_idx_status` (`sys_app_key_status`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci
COMMENT = 'AppKey应用密钥表 - 存储第三方应用的AK/SK密钥对';
