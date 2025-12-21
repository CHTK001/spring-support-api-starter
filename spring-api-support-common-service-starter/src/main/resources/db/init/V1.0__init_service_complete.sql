/*
 * 通用服务模块 - 数据库初始化脚本
 * 
 * 包含表:
 *   - sys_bank_bins: 银行BIN对照表
 *   - sys_city: 行政区划表
 *   - sys_dict: 字典表
 *   - sys_dict_item: 字典子项表
 *   - sys_dict_item_property: 字典配置项表
 *   - sys_log: 系统操作日志表
 *   - sys_setting_group: 系统配置分组表
 *   - sys_setting: 系统全局配置表
 *   - sys_setting_history: 系统配置变更历史表
 *   - sys_limit_configuration: 限流配置表
 *   - sys_limit_record: 限流记录表
 *   - sys_message: 系统消息表
 *   - sys_message_history: 系统消息历史表
 *   - sys_holiday: 节假日表
 *   - sys_file_system_group: 文件系统分组表
 *   - sys_file_system_setting: 文件系统配置表
 *   - sys_file_system: 文件系统主表
 *   - sys_file_system_part: 文件系统分片表
 *
 * 提供系统通用的字典管理、配置管理、日志记录、消息通知、限流控制、文件管理等功能
 *
 * @author System
 * @since 2025-01-01
 * @version 1.0
 */

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 1. 银行BIN对照表 (sys_bank_bins)
-- 用途: 根据银行卡号前缀识别发卡银行和卡类型
-- =====================================================
DROP TABLE IF EXISTS `sys_bank_bins`;
CREATE TABLE `sys_bank_bins` (
    `sys_blank_bin_id` int NOT NULL AUTO_INCREMENT COMMENT 'BIN记录ID - 主键自增',
    `sys_blank_bin_bank_name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '银行名称 - 发卡银行中文名称',
    `sys_blank_bin_bank_name_en` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '银行英文简称 - 如ICBC、CCB等',
    `sys_blank_bin_card_name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '卡片名称 - 银行卡产品名称',
    `sys_blank_bin_card_type` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '卡片类型 - 借记卡/信用卡等',
    `sys_blank_bin_bin` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT 'BIN号 - 银行卡号前6位标识码',
    `sys_blank_bin_n_length` tinyint NULL DEFAULT NULL COMMENT '卡号长度 - 完整银行卡号的位数',
    `sys_blank_bin_bin_length` tinyint(1) NULL DEFAULT NULL COMMENT 'BIN长度 - BIN号的位数',
    `sys_blank_bin_issue_id` int NULL DEFAULT NULL COMMENT '发卡机构ID - 发卡银行的标识ID',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_blank_bin_id`) USING BTREE,
    INDEX `idx_bin` (`sys_blank_bin_bin`) USING BTREE COMMENT 'BIN号索引 - 用于快速匹配银行卡',
    INDEX `idx_bank_name` (`sys_blank_bin_bank_name`) USING BTREE COMMENT '银行名称索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci 
COMMENT = '银行BIN对照表 - 根据银行卡前缀识别发卡银行' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 3. 字典表 (sys_dict)
-- 用途: 系统数据字典，管理各类枚举值和下拉选项
-- =====================================================
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict` (
    `sys_dict_id` int NOT NULL AUTO_INCREMENT COMMENT '字典ID - 主键自增',
    `sys_dict_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典名称 - 字典的显示名称',
    `sys_dict_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典编码 - 字典的唯一标识码，用于程序调用',
    `sys_dict_pid` int NULL DEFAULT 0 COMMENT '父字典ID - 支持字典层级结构，0表示顶级字典',
    `sys_dict_status` int NULL DEFAULT 1 COMMENT '字典状态 - 0:隐藏/禁用, 1:显示/启用',
    `sys_dict_in_system` int NULL DEFAULT 0 COMMENT '是否内置 - 0:非内置(可删除), 1:内置(不可删除)',
    `sys_dict_i18n` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '国际化标识 - 用于多语言支持的i18n键',
    `sys_dict_sort` int NULL DEFAULT 0 COMMENT '排序号 - 字典在列表中的显示顺序',
    `sys_dict_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典描述 - 字典用途说明',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_dict_id`) USING BTREE,
    UNIQUE INDEX `uk_dict_code` (`sys_dict_code`) USING BTREE COMMENT '字典编码唯一索引',
    INDEX `idx_dict_pid` (`sys_dict_pid`) USING BTREE COMMENT '父字典索引',
    INDEX `idx_dict_status` (`sys_dict_status`) USING BTREE COMMENT '状态索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = '字典表 - 系统数据字典，管理各类枚举值' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 4. 字典子项表 (sys_dict_item)
-- 用途: 字典的具体选项值
-- =====================================================
DROP TABLE IF EXISTS `sys_dict_item`;
CREATE TABLE `sys_dict_item` (
    `sys_dict_item_id` int NOT NULL AUTO_INCREMENT COMMENT '字典项ID - 主键自增',
    `sys_dict_id` int NULL DEFAULT NULL COMMENT '所属字典ID - 关联sys_dict表',
    `sys_dict_item_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典项名称 - 选项的显示文本',
    `sys_dict_item_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典项编码 - 选项的值/代码',
    `sys_dict_item_icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标 - 选项的图标类名或URL',
    `sys_dict_item_status` int NULL DEFAULT 1 COMMENT '字典项状态 - 0:隐藏/禁用, 1:显示/启用',
    `sys_dict_item_sort` int NULL DEFAULT 0 COMMENT '排序号 - 选项在列表中的显示顺序',
    `sys_dict_item_i18n` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '国际化标识 - 用于多语言支持的i18n键',
    `sys_dict_item_delete` int NULL DEFAULT 0 COMMENT '是否删除 - 0:未删除, 1:已删除(软删除)',
    `sys_dict_item_property` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '可配置项 - 该字典项支持的扩展配置字段列表',
    `sys_dict_item_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典项描述 - 选项用途说明',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_dict_item_id`) USING BTREE,
    INDEX `idx_dict_id` (`sys_dict_id`) USING BTREE COMMENT '字典ID索引',
    INDEX `idx_item_code` (`sys_dict_item_code`) USING BTREE COMMENT '字典项编码索引',
    INDEX `idx_item_status` (`sys_dict_item_status`) USING BTREE COMMENT '状态索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = '字典子项表 - 字典的具体选项值' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 5. 字典配置项表 (sys_dict_item_property)
-- 用途: 字典项的扩展属性配置
-- =====================================================
DROP TABLE IF EXISTS `sys_dict_item_property`;
CREATE TABLE `sys_dict_item_property` (
    `sys_dict_item_property_id` int NOT NULL AUTO_INCREMENT COMMENT '配置项ID - 主键自增',
    `sys_dict_item_id` int NULL DEFAULT NULL COMMENT '字典项ID - 关联sys_dict_item表',
    `sys_dict_item_property_group` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置项分组 - 属性所属的业务分组',
    `sys_dict_item_property_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置项名称 - 属性的键名',
    `sys_dict_item_property_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置项值 - 属性的值',
    `sys_dict_item_property_selected_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '值类型 - Boolean/String/Number等',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_dict_item_property_id`) USING BTREE,
    INDEX `idx_dict_item_id` (`sys_dict_item_id`) USING BTREE COMMENT '字典项ID索引',
    INDEX `idx_property_group` (`sys_dict_item_property_group`) USING BTREE COMMENT '分组索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = '字典配置项表 - 字典项的扩展属性配置' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 6. 系统操作日志表 (sys_log)
-- 用途: 记录系统操作日志，用于审计和问题追踪
-- =====================================================
DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log` (
    `sys_log_id` int NOT NULL AUTO_INCREMENT COMMENT '日志ID - 主键自增',
    `sys_log_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作账号 - 执行操作的用户账号',
    `sys_log_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求IP - 客户端IP地址',
    `sys_log_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求地址 - API接口路径',
    `sys_log_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求参数 - 接口请求参数(脱敏后)',
    `sys_log_from` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求模块 - 所属功能模块名称',
    `sys_log_status` int NULL DEFAULT NULL COMMENT '请求状态 - 0:失败, 1:成功',
    `sys_log_cost` int NULL DEFAULT NULL COMMENT '耗时 - 请求处理耗时(毫秒)',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_log_id`) USING BTREE,
    INDEX `idx_username` (`sys_log_username`) USING BTREE COMMENT '用户名索引',
    INDEX `idx_status` (`sys_log_status`) USING BTREE COMMENT '状态索引',
    INDEX `idx_create_time` (`create_time`) USING BTREE COMMENT '创建时间索引',
    INDEX `idx_url` (`sys_log_url`) USING BTREE COMMENT '请求地址索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = '系统操作日志表 - 记录API请求日志用于审计' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 7. 系统配置分组表 (sys_setting_group)
-- 用途: 对系统配置项进行分组管理
-- =====================================================
DROP TABLE IF EXISTS `sys_setting_group`;
CREATE TABLE `sys_setting_group` (
    `sys_setting_group_id` int NOT NULL AUTO_INCREMENT COMMENT '分组ID - 主键自增',
    `sys_setting_group_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组名称 - 配置分组的显示名称',
    `sys_setting_group_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组编码 - 配置分组的唯一标识码',
    `sys_setting_group_icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组图标 - 图标类名或URL',
    `sys_setting_group_enable` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用 - 0:禁用, 1:启用',
    `sys_setting_group_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组描述 - 分组用途说明',
    `sys_setting_group_sort` int NULL DEFAULT 0 COMMENT '排序号 - 分组在列表中的显示顺序',
    `sys_setting_group_use_project_interface` tinyint(1) NULL DEFAULT 0 COMMENT '是否使用项目组接口 - 0:否, 1:是',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_setting_group_id`) USING BTREE,
    UNIQUE INDEX `uk_group_code` (`sys_setting_group_code`) USING BTREE COMMENT '分组编码唯一索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = '系统配置分组表 - 对系统配置项进行分组管理' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 8. 系统全局配置表 (sys_setting)
-- 用途: 存储系统全局配置参数
-- =====================================================
DROP TABLE IF EXISTS `sys_setting`;
CREATE TABLE `sys_setting` (
    `sys_setting_id` int NOT NULL AUTO_INCREMENT COMMENT '配置ID - 主键自增',
    `sys_setting_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置名称 - 配置项的键名',
    `sys_setting_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置值 - 配置项的值',
    `sys_setting_value_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置类型 - Boolean/String/Number/Color/Dict等，用于前端组件判断',
    `sys_setting_group` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属分组 - 配置项所属的分组编码',
    `sys_setting_status` int NULL DEFAULT 1 COMMENT '是否启用 - 0:禁用, 1:启用',
    `sys_setting_sort` int NULL DEFAULT 0 COMMENT '排序号 - 配置项在分组中的显示顺序',
    `sys_setting_collect` int NULL DEFAULT 0 COMMENT '是否多值集合 - 0:单个值, 1:多个值的集合',
    `sys_setting_in_system` int NULL DEFAULT 0 COMMENT '是否系统配置 - 0:非系统配置, 1:系统内置配置',
    `sys_setting_app_inner` int NULL DEFAULT 0 COMMENT '是否应用启动配置 - 0:否, 1:是(影响应用启动)',
    `sys_setting_admin` int NULL DEFAULT 0 COMMENT '是否管理员专属 - 0:所有人可见, 1:仅管理员可见',
    `sys_setting_version` int NULL DEFAULT 1 COMMENT '配置版本 - 用于配置变更追踪',
    `sys_setting_config` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置参数 - 附加的配置参数(如关联字典ID)',
    `sys_setting_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置备注 - 配置项用途说明',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_setting_id`) USING BTREE,
    UNIQUE INDEX `uk_setting_name_group` (`sys_setting_name`, `sys_setting_group`) USING BTREE COMMENT '配置名和分组联合唯一索引',
    INDEX `idx_setting_group` (`sys_setting_group`) USING BTREE COMMENT '分组索引',
    INDEX `idx_setting_status` (`sys_setting_status`) USING BTREE COMMENT '状态索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = '系统全局配置表 - 存储系统全局配置参数' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 10. 限流配置表 (sys_limit_configuration)
-- 用途: 配置API接口的限流规则
-- =====================================================
DROP TABLE IF EXISTS `sys_limit_configuration`;
CREATE TABLE `sys_limit_configuration` (
    `sys_limit_configuration_id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID - 主键自增',
    `sys_limit_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '限流规则名称 - 规则的显示名称',
    `sys_limit_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '接口路径 - 需要限流的API路径，支持Ant风格匹配',
    `sys_limit_for_period` int NULL DEFAULT 10 COMMENT '周期许可数 - 每个刷新周期内允许的最大请求数',
    `sys_limit_refresh_period_seconds` bigint NULL DEFAULT 1 COMMENT '刷新周期 - 限流计数器的刷新周期(秒)',
    `sys_limit_timeout_duration_millis` bigint NULL DEFAULT 0 COMMENT '超时时间 - 获取许可的超时时间(毫秒)，0表示立即返回',
    `sys_limit_dimension` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'GLOBAL' COMMENT '限流维度 - GLOBAL:全局, IP:按IP, USER:按用户, API:按接口',
    `sys_limit_key_expression` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '自定义键表达式 - SpEL表达式，用于提取限流键',
    `sys_limit_fallback_method` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '降级方法 - 限流触发后的降级处理方法名',
    `sys_limit_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '请求过于频繁，请稍后再试' COMMENT '错误消息 - 限流触发时返回给客户端的提示信息',
    `sys_limit_status` int NULL DEFAULT 1 COMMENT '是否启用 - 0:禁用, 1:启用',
    `sys_limit_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '规则描述 - 限流规则的详细说明',
    `sys_limit_sort` int NULL DEFAULT 0 COMMENT '排序号 - 规则优先级，数字越小优先级越高',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_limit_configuration_id`) USING BTREE,
    INDEX `idx_limit_path` (`sys_limit_path`(255)) USING BTREE COMMENT '接口路径索引',
    INDEX `idx_limit_status` (`sys_limit_status`) USING BTREE COMMENT '状态索引',
    INDEX `idx_limit_dimension` (`sys_limit_dimension`) USING BTREE COMMENT '限流维度索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = '限流配置表 - 配置API接口的限流规则' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 11. 限流记录表 (sys_limit_record)
-- 用途: 记录限流触发的详细信息
-- =====================================================
DROP TABLE IF EXISTS `sys_limit_record`;
CREATE TABLE `sys_limit_record` (
    `sys_limit_record_id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID - 主键自增',
    `sys_limit_configuration_id` bigint NULL DEFAULT NULL COMMENT '限流配置ID - 关联sys_limit_configuration表',
    `sys_limit_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '接口路径 - 被限流的API路径',
    `sys_limit_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '限流规则名称',
    `sys_limit_dimension` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '限流维度 - GLOBAL/IP/USER/API',
    `sys_limit_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '限流键值 - 具体的限流标识(如IP地址、用户ID)',
    `sys_limit_time` datetime NULL DEFAULT NULL COMMENT '限流时间 - 触发限流的时间',
    `sys_user_id` bigint NULL DEFAULT NULL COMMENT '用户ID - 被限流的用户ID',
    `sys_user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名 - 被限流的用户名',
    `client_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户端IP - 请求的客户端IP地址',
    `request_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求URL - 完整的请求URL',
    `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求方法 - GET/POST/PUT/DELETE等',
    `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'User-Agent - 客户端浏览器UA信息',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_limit_record_id`) USING BTREE,
    INDEX `idx_configuration_id` (`sys_limit_configuration_id`) USING BTREE COMMENT '配置ID索引',
    INDEX `idx_limit_time` (`sys_limit_time`) USING BTREE COMMENT '限流时间索引',
    INDEX `idx_user_id` (`sys_user_id`) USING BTREE COMMENT '用户ID索引',
    INDEX `idx_client_ip` (`client_ip`) USING BTREE COMMENT '客户端IP索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = '限流记录表 - 记录限流触发的详细信息' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 12. 系统消息表 (sys_message)
-- 用途: 存储系统消息、通知、公告等
-- =====================================================
DROP TABLE IF EXISTS `sys_message`;
CREATE TABLE `sys_message` (
    `sys_message_id` int NOT NULL AUTO_INCREMENT COMMENT '消息ID - 主键自增',
    `sys_message_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息标题 - 消息的简短标题',
    `sys_message_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '消息内容 - 消息的详细内容，支持富文本',
    `sys_message_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'system' COMMENT '消息类型 - system:系统通知, notice:公告, message:私信, warning:警告, info:信息',
    `sys_message_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'normal' COMMENT '消息级别 - low:低, normal:普通, high:高, urgent:紧急',
    `sys_message_sender_id` int NULL DEFAULT 0 COMMENT '发送者ID - 0表示系统自动发送',
    `sys_message_sender_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发送者名称',
    `sys_message_receiver_id` int NULL DEFAULT 0 COMMENT '接收者ID - 0表示广播给所有人',
    `sys_message_receiver_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '接收者名称',
    `sys_message_read` tinyint NULL DEFAULT 0 COMMENT '是否已读 - 0:未读, 1:已读',
    `sys_message_read_time` datetime NULL DEFAULT NULL COMMENT '阅读时间 - 消息被阅读的时间',
    `sys_message_send_time` datetime NULL DEFAULT NULL COMMENT '发送时间 - 消息发送的时间',
    `sys_message_expire_time` datetime NULL DEFAULT NULL COMMENT '过期时间 - 消息过期时间，NULL表示永不过期',
    `sys_message_biz_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '业务类型 - 关联的业务类型标识',
    `sys_message_biz_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '业务ID - 关联的业务数据ID',
    `sys_message_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '跳转链接 - 点击消息后跳转的URL',
    `sys_message_extra` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '扩展数据 - 其他扩展数据(JSON格式)',
    `sys_message_status` tinyint NULL DEFAULT 1 COMMENT '消息状态 - 0:草稿, 1:已发送, 2:已撤回',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`sys_message_id`) USING BTREE,
    INDEX `idx_receiver_id` (`sys_message_receiver_id`) USING BTREE COMMENT '接收者索引',
    INDEX `idx_sender_id` (`sys_message_sender_id`) USING BTREE COMMENT '发送者索引',
    INDEX `idx_type` (`sys_message_type`) USING BTREE COMMENT '消息类型索引',
    INDEX `idx_status` (`sys_message_status`) USING BTREE COMMENT '状态索引',
    INDEX `idx_read` (`sys_message_read`) USING BTREE COMMENT '已读状态索引',
    INDEX `idx_send_time` (`sys_message_send_time`) USING BTREE COMMENT '发送时间索引',
    INDEX `idx_expire_time` (`sys_message_expire_time`) USING BTREE COMMENT '过期时间索引',
    INDEX `idx_biz` (`sys_message_biz_type`, `sys_message_biz_id`) USING BTREE COMMENT '业务关联索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = '系统消息表 - 存储系统消息、通知、公告' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 13. 系统消息历史表 (sys_message_history)
-- 用途: 归档已读的历史消息
-- =====================================================
DROP TABLE IF EXISTS `sys_message_history`;
CREATE TABLE `sys_message_history` (
    `sys_message_history_id` int NOT NULL AUTO_INCREMENT COMMENT '历史记录ID - 主键自增',
    `sys_message_history_message_id` int NULL DEFAULT NULL COMMENT '原消息ID - 关联原sys_message表的消息ID',
    `sys_message_history_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '消息标题',
    `sys_message_history_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '消息内容',
    `sys_message_history_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '消息类型',
    `sys_message_history_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '消息级别',
    `sys_message_history_sender_id` int NULL DEFAULT NULL COMMENT '发送者ID',
    `sys_message_history_sender_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发送者名称',
    `sys_message_history_receiver_id` int NULL DEFAULT NULL COMMENT '接收者ID',
    `sys_message_history_receiver_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '接收者名称',
    `sys_message_history_send_time` datetime NULL DEFAULT NULL COMMENT '发送时间',
    `sys_message_history_read_time` datetime NULL DEFAULT NULL COMMENT '阅读时间',
    `sys_message_history_biz_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '业务类型',
    `sys_message_history_biz_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '业务ID',
    `sys_message_history_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '跳转链接',
    `sys_message_history_extra` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '扩展数据',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`sys_message_history_id`) USING BTREE,
    INDEX `idx_receiver_id` (`sys_message_history_receiver_id`) USING BTREE COMMENT '接收者索引',
    INDEX `idx_read_time` (`sys_message_history_read_time`) USING BTREE COMMENT '阅读时间索引',
    INDEX `idx_message_id` (`sys_message_history_message_id`) USING BTREE COMMENT '原消息ID索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = '系统消息历史表 - 归档已读的历史消息' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 14. 节假日表 (sys_holiday)
-- 用途: 存储法定节假日和调休工作日信息
-- =====================================================
DROP TABLE IF EXISTS `sys_holiday`;
CREATE TABLE `sys_holiday` (
    `sys_holiday_id` int NOT NULL AUTO_INCREMENT COMMENT '节假日ID - 主键自增',
    `sys_holiday_year` int NULL DEFAULT NULL COMMENT '节假日年份 - 如2024、2025',
    `sys_holiday_date` date NULL DEFAULT NULL COMMENT '节假日日期 - 具体日期',
    `sys_holiday_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '节假日名称 - 如春节、国庆节、调休',
    `sys_holiday_is_holiday` tinyint(1) NULL DEFAULT 1 COMMENT '是否节假日 - 1:节假日休息, 0:调休工作日',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`sys_holiday_id`) USING BTREE,
    UNIQUE INDEX `uk_date` (`sys_holiday_date`) USING BTREE COMMENT '日期唯一索引 - 确保每个日期只有一条记录',
    INDEX `idx_year` (`sys_holiday_year`) USING BTREE COMMENT '年份索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = '节假日表 - 存储法定节假日和调休工作日' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 15. 文件系统分组表 (sys_file_system_group)
-- 用途: 对上传的文件进行分组管理
-- =====================================================
DROP TABLE IF EXISTS `sys_file_system_group`;
CREATE TABLE `sys_file_system_group` (
    `sys_file_system_group_id` int NOT NULL AUTO_INCREMENT COMMENT '分组ID - 主键自增',
    `sys_file_system_group_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组名称 - 文件分组的显示名称',
    `sys_file_system_group_parent_id` int NULL DEFAULT 0 COMMENT '父分组ID - 支持多级分组，0表示顶级分组',
    `sys_file_system_group_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组路径 - 分组的短路径标识',
    `sys_file_system_group_full_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '完整路径 - 从根到当前分组的完整路径',
    `sys_file_system_group_level` int NULL DEFAULT 1 COMMENT '分组层级 - 分组在树中的层级深度',
    `sys_file_system_group_tree_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '树路径 - 分组ID组成的树路径，用于快速查询',
    `sys_file_system_group_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组描述 - 分组用途说明',
    `sys_file_system_group_is_default` tinyint(1) NULL DEFAULT 0 COMMENT '是否默认分组 - 0:否, 1:是(新文件默认归入)',
    `sys_file_system_group_sort` int NULL DEFAULT 0 COMMENT '排序号 - 分组在列表中的显示顺序',
    `sys_file_system_group_status` int NULL DEFAULT 1 COMMENT '分组状态 - 0:禁用, 1:启用',
    `sys_file_system_group_icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组图标 - 图标类名',
    `sys_file_system_group_color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组颜色 - 十六进制颜色值',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_file_system_group_id`) USING BTREE,
    INDEX `idx_parent_id` (`sys_file_system_group_parent_id`) USING BTREE COMMENT '父分组索引',
    INDEX `idx_status` (`sys_file_system_group_status`) USING BTREE COMMENT '状态索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = '文件系统分组表 - 对上传的文件进行分组管理' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 16. 文件系统配置表 (sys_file_system_setting)
-- 用途: 配置文件上传和存储的参数
-- =====================================================
DROP TABLE IF EXISTS `sys_file_system_setting`;
CREATE TABLE `sys_file_system_setting` (
    `sys_file_system_setting_id` int NOT NULL AUTO_INCREMENT COMMENT '配置ID - 主键自增',
    `sys_file_system_setting_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置名称 - 配置方案的名称',
    `sys_file_system_setting_chunk_enabled` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用分片上传 - 0:否, 1:是',
    `sys_file_system_setting_chunk_size_mb` int NULL DEFAULT 100 COMMENT '分片大小 - 每个分片的大小(MB)',
    `sys_file_system_setting_auto_merge_enabled` tinyint(1) NULL DEFAULT 1 COMMENT '是否自动合并 - 0:否, 1:是(分片上传完成后自动合并)',
    `sys_file_system_setting_merge_task_limit` int NULL DEFAULT 10 COMMENT '合并任务限制 - 同时进行的合并任务最大数量',
    `sys_file_system_setting_manual_merge_enabled` tinyint(1) NULL DEFAULT 1 COMMENT '是否支持手动合并 - 0:否, 1:是',
    `sys_file_system_setting_http_server_enabled` tinyint(1) NULL DEFAULT 0 COMMENT '是否开启HTTP服务 - 0:否, 1:是',
    `sys_file_system_setting_http_server_port` int NULL DEFAULT 8888 COMMENT 'HTTP服务端口',
    `sys_file_system_setting_http_server_host` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0.0.0.0' COMMENT 'HTTP服务主机',
    `sys_file_system_setting_http_server_context` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '/file' COMMENT 'HTTP上下文路径',
    `sys_file_system_setting_download_enabled` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用下载 - 0:否, 1:是',
    `sys_file_system_setting_preview_enabled` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用预览 - 0:否, 1:是',
    `sys_file_system_setting_http_access_domain` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'HTTP访问域名 - 文件访问的域名前缀',
    `sys_file_system_setting_allowed_types` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '允许的文件类型 - 文件扩展名白名单，逗号分隔',
    `sys_file_system_setting_max_file_size_mb` int NULL DEFAULT 1024 COMMENT '单文件最大大小 - 单个文件的最大大小(MB)',
    `sys_file_system_setting_storage_root_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '存储根路径 - 文件存储的根目录路径',
    `sys_file_system_setting_temp_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '临时文件路径 - 分片上传的临时存储目录',
    `sys_file_system_setting_retention_days` int NULL DEFAULT 0 COMMENT '文件保留天数 - 0表示永久保留',
    `sys_file_system_setting_enabled` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用 - 0:禁用, 1:启用',
    `sys_file_system_setting_default_storage_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'local' COMMENT '默认存储类型 - local:本地, oss:阿里云OSS, cos:腾讯云COS等',
    `sys_file_system_setting_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置备注',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_file_system_setting_id`) USING BTREE,
    INDEX `idx_enabled` (`sys_file_system_setting_enabled`) USING BTREE COMMENT '启用状态索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = '文件系统配置表 - 配置文件上传和存储的参数' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 17. 文件系统主表 (sys_file_system)
-- 用途: 存储上传文件的元数据信息
-- =====================================================
DROP TABLE IF EXISTS `sys_file_system`;
CREATE TABLE `sys_file_system` (
    `sys_file_system_id` int NOT NULL AUTO_INCREMENT COMMENT '文件ID - 主键自增',
    `sys_file_system_tid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务唯一标识 - 上传任务的唯一ID，用于断点续传',
    `sys_file_system_bucket` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '存储桶 - 对象存储的桶名称',
    `sys_file_system_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件名称 - 存储后的文件名(通常是UUID)',
    `sys_file_system_original_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '原始文件名 - 用户上传时的原始文件名',
    `sys_file_system_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '存储类型 - local/oss/cos/minio等',
    `sys_file_system_extension` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件扩展名 - 如jpg、pdf、docx',
    `sys_file_system_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件路径 - 文件在存储中的相对路径',
    `sys_file_system_size` bigint NULL DEFAULT NULL COMMENT '文件大小 - 文件大小(字节)',
    `sys_file_system_md5` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件MD5 - 文件的MD5哈希值，用于秒传和完整性校验',
    `sys_file_system_chunk_total` int NULL DEFAULT NULL COMMENT '分片总数 - 分片上传时的总分片数',
    `sys_file_system_chunk_uploaded` int NULL DEFAULT 0 COMMENT '已上传分片数 - 当前已完成上传的分片数量',
    `sys_file_system_chunk_size` bigint NULL DEFAULT NULL COMMENT '分片大小 - 每个分片的大小(字节)',
    `sys_file_system_version` int NULL DEFAULT 0 COMMENT '版本号 - 文件版本，用于乐观锁',
    `sys_file_system_status` int NULL DEFAULT 0 COMMENT '文件状态 - 0:上传中, 1:上传完成, 2:合并中, 3:已完成, -1:上传失败',
    `sys_file_system_http_enabled` tinyint(1) NULL DEFAULT 0 COMMENT '是否启用HTTP访问 - 0:否, 1:是',
    `sys_file_system_http_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'HTTP访问URL - 文件的完整访问地址',
    `sys_file_system_service_id` int NULL DEFAULT NULL COMMENT '绑定服务ID - 关联的业务服务ID',
    `sys_file_system_group_id` int NULL DEFAULT NULL COMMENT '分组ID - 关联sys_file_system_group表',
    `sys_file_system_upload_start_time` datetime NULL DEFAULT NULL COMMENT '上传开始时间',
    `sys_file_system_upload_complete_time` datetime NULL DEFAULT NULL COMMENT '上传完成时间',
    `sys_file_system_local_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '本地临时路径 - 分片上传时的临时存储路径',
    `sys_file_system_summary` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件描述 - 文件的备注说明',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_file_system_id`) USING BTREE,
    UNIQUE INDEX `uk_tid` (`sys_file_system_tid`) USING BTREE COMMENT '任务ID唯一索引',
    INDEX `idx_status` (`sys_file_system_status`) USING BTREE COMMENT '状态索引',
    INDEX `idx_group_id` (`sys_file_system_group_id`) USING BTREE COMMENT '分组ID索引',
    INDEX `idx_create_time` (`create_time`) USING BTREE COMMENT '创建时间索引',
    INDEX `idx_md5` (`sys_file_system_md5`) USING BTREE COMMENT 'MD5索引 - 用于秒传查询'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = '文件系统主表 - 存储上传文件的元数据信息' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 18. 文件系统分片表 (sys_file_system_part)
-- 用途: 存储分片上传的每个分片信息
-- =====================================================
DROP TABLE IF EXISTS `sys_file_system_part`;
CREATE TABLE `sys_file_system_part` (
    `sys_file_system_part_id` int NOT NULL AUTO_INCREMENT COMMENT '分片ID - 主键自增',
    `sys_file_system_part_file_id` int NOT NULL COMMENT '文件ID - 关联sys_file_system表',
    `sys_file_system_part_seq` int NOT NULL COMMENT '分片序号 - 分片在文件中的顺序号，从1开始',
    `sys_file_system_part_size` bigint NULL DEFAULT NULL COMMENT '分片大小 - 该分片的大小(字节)',
    `sys_file_system_part_md5` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分片MD5 - 该分片的MD5哈希值',
    `sys_file_system_part_local_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '本地存储路径 - 分片在本地的临时存储路径',
    `sys_file_system_part_version` int NULL DEFAULT 0 COMMENT '版本号 - 用于乐观锁',
    `sys_file_system_part_status` int NULL DEFAULT 0 COMMENT '分片状态 - 0:等待上传, 1:上传中, 2:上传完成, -1:上传失败',
    `sys_file_system_part_upload_time` datetime NULL DEFAULT NULL COMMENT '上传时间 - 分片上传完成的时间',
    `sys_file_system_part_process_time` datetime NULL DEFAULT NULL COMMENT '处理时间 - 分片被处理(合并)的时间',
    `sys_file_system_part_error_message` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误信息 - 上传失败时的错误消息',
    `sys_file_system_part_retry_count` int NULL DEFAULT 0 COMMENT '重试次数 - 上传失败后的重试次数',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_file_system_part_id`) USING BTREE,
    UNIQUE INDEX `uk_file_seq` (`sys_file_system_part_file_id`, `sys_file_system_part_seq`) USING BTREE COMMENT '文件和序号联合唯一索引',
    INDEX `idx_file_id` (`sys_file_system_part_file_id`) USING BTREE COMMENT '文件ID索引',
    INDEX `idx_status` (`sys_file_system_part_status`) USING BTREE COMMENT '状态索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = '文件系统分片表 - 存储分片上传的每个分片信息' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
