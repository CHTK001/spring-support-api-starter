/*
 * 项目管理模块 - 数据库初始化脚本
 * 
 * 包含表:
 *   - sys_project: 项目管理表
 *   - sys_project_default: 项目默认配置表
 *   - sys_secret: 密钥管理表
 *   - sys_sms_template: 短信模板表
 *   - sys_sms_log: 短信日志表
 *   - sys_email_template: 邮件模板表
 *   - sys_email_log: 邮件日志表
 *
 * 用于项目配置管理、消息发送等基础功能
 *
 * @author System
 * @since 2025-01-01
 * @version 1.0
 */

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 1. 项目管理表 (sys_project)
-- 用途: 管理各类第三方项目/服务的配置信息
-- =====================================================
DROP TABLE IF EXISTS `sys_project`;
CREATE TABLE `sys_project` (
    `sys_project_id` int NOT NULL AUTO_INCREMENT COMMENT '项目ID - 主键自增',
    `sys_project_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '项目名称 - 项目的显示名称',
    `sys_project_icon` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '项目图标 - 项目图标URL地址',
    `sys_project_group` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '项目分组 - 项目所属分组，用于分类管理',
    `sys_project_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '项目编码 - 项目唯一标识码',
    `sys_project_app_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'AppID - 应用标识ID，用于API认证',
    `sys_project_app_secret` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'AppSecret - 应用密钥，用于API认证',
    `sys_project_app_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'AppKey - 应用访问密钥',
    `sys_project_sign` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '签名 - 短信/接口签名',
    `sys_project_delete` int NULL DEFAULT 0 COMMENT '是否删除 - 0:未删除, 1:已删除',
    `sys_project_status` int NULL DEFAULT 1 COMMENT '是否启用 - 0:禁用, 1:启用',
    `sys_project_sign_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '项目标识编码 - 海康等平台的项目标识',
    `sys_project_sort` int NULL DEFAULT 0 COMMENT '排序号 - 项目在列表中的显示顺序',
    `sys_project_cdn` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'CDN地址 - 内容分发网络地址',
    `sys_project_endpoint` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '端点地址 - API服务端点URL',
    `sys_project_local_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '本地路径 - 本地文件存储路径',
    `sys_project_api_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'API地址 - 接口服务基础地址',
    `sys_project_vender` int NULL DEFAULT NULL COMMENT '厂家 - 关联字典表ID=1的厂家类型',
    `sys_project_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '项目描述 - 项目用途说明',
    `sys_project_function` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '功能类型 - 关联字典表ID=6的功能类型，多个用逗号分隔',
    `sys_project_smtp_host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SMTP主机 - 邮件服务器主机地址',
    `sys_project_smtp_port` int NULL DEFAULT NULL COMMENT 'SMTP端口 - 邮件服务器端口号',
    `sys_project_smtp_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SMTP密码 - 邮件服务器授权码',
    `sys_project_smtp_from` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SMTP发送账号 - 发件人邮箱地址',
    `sys_project_location_temp_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '临时文件路径 - 临时文件存储目录',
    `sys_project_location_oss_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'OSS地址 - 对象存储服务地址',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_project_id`) USING BTREE,
    UNIQUE INDEX `uk_project_code` (`sys_project_code`) USING BTREE COMMENT '项目编码唯一索引',
    INDEX `idx_project_status` (`sys_project_status`) USING BTREE COMMENT '状态索引',
    INDEX `idx_project_vender` (`sys_project_vender`) USING BTREE COMMENT '厂家索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = '项目管理表 - 管理各类第三方项目/服务的配置信息' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 2. 项目默认配置表 (sys_project_default)
-- 用途: 配置各功能类型的默认项目
-- =====================================================
DROP TABLE IF EXISTS `sys_project_default`;
CREATE TABLE `sys_project_default` (
    `sys_project_default_id` int NOT NULL AUTO_INCREMENT COMMENT '配置ID - 主键自增',
    `sys_project_default_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '功能类型 - 关联字典表ID=6的功能类型',
    `sys_project_id` int NULL DEFAULT NULL COMMENT '项目ID - 关联sys_project表，该功能类型的默认项目',
    PRIMARY KEY (`sys_project_default_id`) USING BTREE,
    UNIQUE INDEX `uk_default_type` (`sys_project_default_type`) USING BTREE COMMENT '功能类型唯一索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = '项目默认配置表 - 配置各功能类型的默认项目' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 3. 密钥管理表 (sys_secret)
-- 用途: 独立管理各类服务的密钥信息
-- =====================================================
DROP TABLE IF EXISTS `sys_secret`;
CREATE TABLE `sys_secret` (
    `sys_secret_id` int NOT NULL AUTO_INCREMENT COMMENT '密钥ID - 主键自增',
    `sys_secret_group` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密钥分组 - 密钥所属分组',
    `sys_secret_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密钥编码 - 密钥唯一标识码',
    `sys_secret_app_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'AppID',
    `sys_secret_app_secret` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'AppSecret',
    `sys_secret_sign` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '签名',
    `sys_secret_delete` int NULL DEFAULT 0 COMMENT '是否删除 - 0:未删除, 1:已删除',
    `sys_secret_status` int NULL DEFAULT 1 COMMENT '是否启用 - 0:禁用, 1:启用',
    `sys_secret_cdn` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'CDN地址',
    `sys_secret_endpoint` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '端点地址',
    `sys_secret_api_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'API地址',
    `sys_secret_dict_item_id` int NULL DEFAULT NULL COMMENT '字典项ID - 关联字典表ID=1',
    `sys_secret_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
    `sys_secret_function` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '功能类型 - SMS/EMAIL等',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_secret_id`) USING BTREE,
    INDEX `idx_secret_code` (`sys_secret_code`) USING BTREE COMMENT '密钥编码索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = '密钥管理表 - 独立管理各类服务的密钥信息' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 4. 短信模板表 (sys_sms_template)
-- 用途: 管理短信发送模板
-- =====================================================
DROP TABLE IF EXISTS `sys_sms_template`;
CREATE TABLE `sys_sms_template` (
    `sys_sms_template_id` int NOT NULL AUTO_INCREMENT COMMENT '模板ID - 主键自增',
    `sys_project_id` int NULL DEFAULT NULL COMMENT '项目ID - 关联sys_project表',
    `sys_project_type` int NULL DEFAULT NULL COMMENT '类别 - 关联字典表ID=6',
    `sys_sms_template_sort` int NULL DEFAULT 0 COMMENT '模板优先级',
    `sys_sms_template_category` int NULL DEFAULT NULL COMMENT '子类型 - 关联字典表ID=2(短信通知/验证码等)',
    `sys_sms_template_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板名称',
    `sys_sms_template_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板编码 - 短信平台的模板ID',
    `sys_sms_template_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '模板内容 - 短信模板内容，变量用${var}表示',
    `sys_sms_template_delete` int NULL DEFAULT 0 COMMENT '是否删除 - 0:未删除, 1:已删除',
    `sys_sms_template_status` int NULL DEFAULT 1 COMMENT '是否启用 - 0:禁用, 1:启用',
    `sys_sms_template_type` int NULL DEFAULT 0 COMMENT '模板类型 - 0:系统, 1:用户, 2:其它',
    `sys_sms_template_default` int NULL DEFAULT 0 COMMENT '是否默认 - 0:否, 1:是',
    `sys_sms_template_approve` int NULL DEFAULT 0 COMMENT '审批状态 - 0:未审批, 1:审批中, 2:已通过, 3:未通过',
    `sys_sms_template_approve_reject` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '拒绝原因',
    `sys_sms_template_approve_create_by` int NULL DEFAULT NULL COMMENT '审批人ID',
    `sys_sms_template_disabled` int NULL DEFAULT 0 COMMENT '编辑状态 - 0:可编辑, 1:不可编辑',
    `sys_sms_template_origin` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源',
    `sys_sms_template_origin_uuid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源UUID',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_sms_template_id`) USING BTREE,
    INDEX `idx_project_id` (`sys_project_id`) USING BTREE COMMENT '项目ID索引',
    INDEX `idx_template_code` (`sys_sms_template_code`) USING BTREE COMMENT '模板编码索引',
    INDEX `idx_template_status` (`sys_sms_template_status`) USING BTREE COMMENT '状态索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = '短信模板表 - 管理短信发送模板' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 5. 短信日志表 (sys_sms_log)
-- 用途: 记录短信发送历史
-- =====================================================
DROP TABLE IF EXISTS `sys_sms_log`;
CREATE TABLE `sys_sms_log` (
    `sys_sms_log_id` int NOT NULL AUTO_INCREMENT COMMENT '日志ID - 主键自增',
    `sys_sms_log_target` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '接收人 - 手机号码',
    `sys_sms_log_template_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板ID - 使用的模板编码',
    `sys_sms_log_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板参数 - JSON格式的模板变量',
    `sys_sms_log_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '发送内容 - 最终发送的短信内容',
    `sys_sms_cost` decimal(10, 2) NULL DEFAULT NULL COMMENT '耗时 - 发送耗时(毫秒)',
    `sys_sms_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '返回码 - 短信平台返回的状态码',
    `sys_sms_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '返回消息 - 短信平台返回的消息',
    `sys_sms_app_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '短信厂家 - 使用的短信服务商',
    `sys_sms_app_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '短信AppID',
    `sys_sms_app_sign` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '短信签名',
    `sys_project_id` int NULL DEFAULT NULL COMMENT '项目ID - 关联sys_project表',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_sms_log_id`) USING BTREE,
    INDEX `idx_target` (`sys_sms_log_target`) USING BTREE COMMENT '接收人索引',
    INDEX `idx_project_id` (`sys_project_id`) USING BTREE COMMENT '项目ID索引',
    INDEX `idx_create_time` (`create_time`) USING BTREE COMMENT '创建时间索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = '短信日志表 - 记录短信发送历史' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 6. 邮件模板表 (sys_email_template)
-- 用途: 管理邮件发送模板
-- =====================================================
DROP TABLE IF EXISTS `sys_email_template`;
CREATE TABLE `sys_email_template` (
    `sys_email_template_id` int NOT NULL AUTO_INCREMENT COMMENT '模板ID - 主键自增',
    `sys_project_id` int NULL DEFAULT NULL COMMENT '项目ID - 关联sys_project表',
    `sys_project_type` int NULL DEFAULT NULL COMMENT '类别 - 关联字典表ID=6',
    `sys_email_template_sort` int NULL DEFAULT 0 COMMENT '模板优先级',
    `sys_email_template_category` int NULL DEFAULT NULL COMMENT '子类型 - 关联字典表ID=2',
    `sys_email_template_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板名称',
    `sys_email_template_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板编码 - 模板唯一标识',
    `sys_email_template_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '模板内容 - HTML格式的邮件模板',
    `sys_email_template_delete` int NULL DEFAULT 0 COMMENT '是否删除 - 0:未删除, 1:已删除',
    `sys_email_template_status` int NULL DEFAULT 1 COMMENT '是否启用 - 0:禁用, 1:启用',
    `sys_email_template_type` int NULL DEFAULT 0 COMMENT '模板类型 - 0:系统, 1:用户, 2:其它',
    `sys_email_template_default` int NULL DEFAULT 0 COMMENT '是否默认 - 0:否, 1:是',
    `sys_email_template_approve` int NULL DEFAULT 0 COMMENT '审批状态 - 0:未审批, 1:审批中, 2:已通过, 3:未通过',
    `sys_email_template_approve_reject` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '拒绝原因',
    `sys_email_template_approve_create_by` int NULL DEFAULT NULL COMMENT '审批人ID',
    `sys_email_template_disabled` int NULL DEFAULT 0 COMMENT '编辑状态 - 0:可编辑, 1:不可编辑',
    `sys_email_template_origin` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源',
    `sys_email_template_origin_uuid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源UUID',
    `sys_email_template_callback_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '回调地址 - 邮件状态回调URL',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_email_template_id`) USING BTREE,
    INDEX `idx_project_id` (`sys_project_id`) USING BTREE COMMENT '项目ID索引',
    INDEX `idx_template_code` (`sys_email_template_code`) USING BTREE COMMENT '模板编码索引',
    INDEX `idx_template_status` (`sys_email_template_status`) USING BTREE COMMENT '状态索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = '邮件模板表 - 管理邮件发送模板' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 7. 邮件日志表 (sys_email_log)
-- 用途: 记录邮件发送历史
-- =====================================================
DROP TABLE IF EXISTS `sys_email_log`;
CREATE TABLE `sys_email_log` (
    `sys_email_log_id` int NOT NULL AUTO_INCREMENT COMMENT '日志ID - 主键自增',
    `sys_email_log_target` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '接收人 - 收件人邮箱地址',
    `sys_email_log_template_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板ID',
    `sys_email_log_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板参数 - JSON格式的模板变量',
    `sys_email_log_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '发送内容 - 最终发送的邮件内容',
    `sys_email_cost` decimal(10, 2) NULL DEFAULT NULL COMMENT '耗时 - 发送耗时(毫秒)',
    `sys_email_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '返回码 - SUCCESS/FAILURE',
    `sys_email_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '返回消息 - 错误信息',
    `sys_email_app_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮件厂家',
    `sys_email_app_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮件密钥',
    `sys_email_app_sign` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '签名',
    `sys_email_read` int NULL DEFAULT 0 COMMENT '是否已读 - 0:未读, 1:已读',
    `sys_project_id` int NULL DEFAULT NULL COMMENT '项目ID - 关联sys_project表',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_email_log_id`) USING BTREE,
    INDEX `idx_target` (`sys_email_log_target`) USING BTREE COMMENT '接收人索引',
    INDEX `idx_project_id` (`sys_project_id`) USING BTREE COMMENT '项目ID索引',
    INDEX `idx_create_time` (`create_time`) USING BTREE COMMENT '创建时间索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = '邮件日志表 - 记录邮件发送历史' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;

