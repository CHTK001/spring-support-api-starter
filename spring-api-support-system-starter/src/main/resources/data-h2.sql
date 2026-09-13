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




-- =====================================================
-- 1. 银行BIN对照表 (sys_bank_bins)
-- 用途: 根据银行卡号前缀识别发卡银行和卡类型
-- =====================================================
DROP TABLE IF EXISTS sys_bank_bins;
CREATE TABLE sys_bank_bins (
    sys_blank_bin_id int NOT NULL AUTO_INCREMENT COMMENT 'BIN记录ID - 主键自增',
    sys_blank_bin_bank_name varchar(20)   NOT NULL DEFAULT '' COMMENT '银行名称 - 发卡银行中文名称',
    sys_blank_bin_bank_name_en varchar(20)   NULL DEFAULT NULL COMMENT '银行英文简称 - 如ICBC、CCB等',
    sys_blank_bin_card_name varchar(20)   NULL DEFAULT NULL COMMENT '卡片名称 - 银行卡产品名称',
    sys_blank_bin_card_type varchar(20)   NULL DEFAULT NULL COMMENT '卡片类型 - 借记卡/信用卡等',
    sys_blank_bin_bin varchar(20)   NOT NULL DEFAULT '' COMMENT 'BIN号 - 银行卡号前6位标识码',
    sys_blank_bin_n_length tinyint NULL DEFAULT NULL COMMENT '卡号长度 - 完整银行卡号的位数',
    sys_blank_bin_bin_length tinyint NULL DEFAULT NULL COMMENT 'BIN长度 - BIN号的位数',
    sys_blank_bin_issue_id int NULL DEFAULT NULL COMMENT '发卡机构ID - 发卡银行的标识ID',
    create_name varchar(255)   NULL DEFAULT NULL COMMENT '创建人姓名',
    create_by int NULL DEFAULT NULL COMMENT '创建人ID',
    create_time datetime NULL DEFAULT NULL COMMENT '创建时间',
    update_time datetime NULL DEFAULT NULL COMMENT '更新时间',
    update_name varchar(255)   NULL DEFAULT NULL COMMENT '更新人姓名',
    update_by int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (sys_blank_bin_id) USING BTREE,
    INDEX idx_bin (sys_blank_bin_bin) USING BTREE COMMENT 'BIN号索引 - 用于快速匹配银行卡',
    INDEX idx_bank_name (sys_blank_bin_bank_name) USING BTREE COMMENT '银行名称索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci 
COMMENT = '银行BIN对照表 - 根据银行卡前缀识别发卡银行' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 3. 字典表 (sys_dict)
-- 用途: 系统数据字典，管理各类枚举值和下拉选项
-- =====================================================
DROP TABLE IF EXISTS sys_dict;
CREATE TABLE sys_dict (
    sys_dict_id int NOT NULL AUTO_INCREMENT COMMENT '字典ID - 主键自增',
    sys_dict_name varchar(255)   NULL DEFAULT NULL COMMENT '字典名称 - 字典的显示名称',
    sys_dict_code varchar(255)   NULL DEFAULT NULL COMMENT '字典编码 - 字典的唯一标识码，用于程序调用',
    sys_dict_pid int NULL DEFAULT 0 COMMENT '父字典ID - 支持字典层级结构，0表示顶级字典',
    sys_dict_status int NULL DEFAULT 1 COMMENT '字典状态 - 0:隐藏/禁用, 1:显示/启用',
    sys_dict_in_system int NULL DEFAULT 0 COMMENT '是否内置 - 0:非内置(可删除), 1:内置(不可删除)',
    sys_dict_i18n varchar(255)   NULL DEFAULT NULL COMMENT '国际化标识 - 用于多语言支持的i18n键',
    sys_dict_sort int NULL DEFAULT 0 COMMENT '排序号 - 字典在列表中的显示顺序',
    sys_dict_remark varchar(255)   NULL DEFAULT NULL COMMENT '字典描述 - 字典用途说明',
    create_name varchar(255)   NULL DEFAULT NULL COMMENT '创建人姓名',
    create_by int NULL DEFAULT NULL COMMENT '创建人ID',
    create_time datetime NULL DEFAULT NULL COMMENT '创建时间',
    update_time datetime NULL DEFAULT NULL COMMENT '更新时间',
    update_name varchar(255)   NULL DEFAULT NULL COMMENT '更新人姓名',
    update_by int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (sys_dict_id) USING BTREE,
    UNIQUE INDEX uk_dict_code (sys_dict_code) USING BTREE COMMENT '字典编码唯一索引',
    INDEX idx_dict_pid (sys_dict_pid) USING BTREE COMMENT '父字典索引',
    INDEX idx_dict_status (sys_dict_status) USING BTREE COMMENT '状态索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci 
COMMENT = '字典表 - 系统数据字典，管理各类枚举值' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 4. 字典子项表 (sys_dict_item)
-- 用途: 字典的具体选项值
-- =====================================================
DROP TABLE IF EXISTS sys_dict_item;
CREATE TABLE sys_dict_item (
    sys_dict_item_id int NOT NULL AUTO_INCREMENT COMMENT '字典项ID - 主键自增',
    sys_dict_id int NULL DEFAULT NULL COMMENT '所属字典ID - 关联sys_dict表',
    sys_dict_item_name varchar(255)   NULL DEFAULT NULL COMMENT '字典项名称 - 选项的显示文本',
    sys_dict_item_code varchar(255)   NULL DEFAULT NULL COMMENT '字典项编码 - 选项的值/代码',
    sys_dict_item_icon varchar(255)   NULL DEFAULT NULL COMMENT '图标 - 选项的图标类名或URL',
    sys_dict_item_status int NULL DEFAULT 1 COMMENT '字典项状态 - 0:隐藏/禁用, 1:显示/启用',
    sys_dict_item_sort int NULL DEFAULT 0 COMMENT '排序号 - 选项在列表中的显示顺序',
    sys_dict_item_i18n varchar(255)   NULL DEFAULT NULL COMMENT '国际化标识 - 用于多语言支持的i18n键',
    sys_dict_item_delete int NULL DEFAULT 0 COMMENT '是否删除 - 0:未删除, 1:已删除(软删除)',
    sys_dict_item_property varchar(255)   NULL DEFAULT NULL COMMENT '可配置项 - 该字典项支持的扩展配置字段列表',
    sys_dict_item_remark varchar(255)   NULL DEFAULT NULL COMMENT '字典项描述 - 选项用途说明',
    create_name varchar(255)   NULL DEFAULT NULL COMMENT '创建人姓名',
    create_by int NULL DEFAULT NULL COMMENT '创建人ID',
    create_time datetime NULL DEFAULT NULL COMMENT '创建时间',
    update_time datetime NULL DEFAULT NULL COMMENT '更新时间',
    update_name varchar(255)   NULL DEFAULT NULL COMMENT '更新人姓名',
    update_by int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (sys_dict_item_id) USING BTREE,
    INDEX idx_dict_id (sys_dict_id) USING BTREE COMMENT '字典ID索引',
    INDEX idx_item_code (sys_dict_item_code) USING BTREE COMMENT '字典项编码索引',
    INDEX idx_item_status (sys_dict_item_status) USING BTREE COMMENT '状态索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci 
COMMENT = '字典子项表 - 字典的具体选项值' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 5. 字典配置项表 (sys_dict_item_property)
-- 用途: 字典项的扩展属性配置
-- =====================================================
DROP TABLE IF EXISTS sys_dict_item_property;
CREATE TABLE sys_dict_item_property (
    sys_dict_item_property_id int NOT NULL AUTO_INCREMENT COMMENT '配置项ID - 主键自增',
    sys_dict_item_id int NULL DEFAULT NULL COMMENT '字典项ID - 关联sys_dict_item表',
    sys_dict_item_property_group varchar(20)   NULL DEFAULT NULL COMMENT '配置项分组 - 属性所属的业务分组',
    sys_dict_item_property_name varchar(255)   NULL DEFAULT NULL COMMENT '配置项名称 - 属性的键名',
    sys_dict_item_property_value varchar(255)   NULL DEFAULT NULL COMMENT '配置项值 - 属性的值',
    sys_dict_item_property_selected_type varchar(255)   NULL DEFAULT NULL COMMENT '值类型 - Boolean/String/Number等',
    create_name varchar(255)   NULL DEFAULT NULL COMMENT '创建人姓名',
    create_by int NULL DEFAULT NULL COMMENT '创建人ID',
    create_time datetime NULL DEFAULT NULL COMMENT '创建时间',
    update_time datetime NULL DEFAULT NULL COMMENT '更新时间',
    update_name varchar(255)   NULL DEFAULT NULL COMMENT '更新人姓名',
    update_by int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (sys_dict_item_property_id) USING BTREE,
    INDEX idx_dict_item_id (sys_dict_item_id) USING BTREE COMMENT '字典项ID索引',
    INDEX idx_property_group (sys_dict_item_property_group) USING BTREE COMMENT '分组索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci 
COMMENT = '字典配置项表 - 字典项的扩展属性配置' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 6. 系统操作日志表 (sys_log)
-- 用途: 记录系统操作日志，用于审计和问题追踪
-- =====================================================
DROP TABLE IF EXISTS sys_log;
CREATE TABLE sys_log (
    sys_log_id int NOT NULL AUTO_INCREMENT COMMENT '日志ID - 主键自增',
    sys_log_username varchar(255)   NULL DEFAULT NULL COMMENT '操作账号 - 执行操作的用户账号',
    sys_log_address varchar(255)   NULL DEFAULT NULL COMMENT '请求IP - 客户端IP地址',
    sys_log_url varchar(255)   NULL DEFAULT NULL COMMENT '请求地址 - API接口路径',
    sys_log_param varchar(255)   NULL DEFAULT NULL COMMENT '请求参数 - 接口请求参数(脱敏后)',
    sys_log_from varchar(255)   NULL DEFAULT NULL COMMENT '请求模块 - 所属功能模块名称',
    sys_log_status int NULL DEFAULT NULL COMMENT '请求状态 - 0:失败, 1:成功',
    sys_log_cost int NULL DEFAULT NULL COMMENT '耗时 - 请求处理耗时(毫秒)',
    create_name varchar(255)   NULL DEFAULT NULL COMMENT '创建人姓名',
    create_by int NULL DEFAULT NULL COMMENT '创建人ID',
    create_time datetime NULL DEFAULT NULL COMMENT '创建时间',
    update_time datetime NULL DEFAULT NULL COMMENT '更新时间',
    update_name varchar(255)   NULL DEFAULT NULL COMMENT '更新人姓名',
    update_by int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (sys_log_id) USING BTREE,
    INDEX idx_username (sys_log_username) USING BTREE COMMENT '用户名索引',
    INDEX idx_status (sys_log_status) USING BTREE COMMENT '状态索引',
    INDEX idx_create_time (create_time) USING BTREE COMMENT '创建时间索引',
    INDEX idx_url (sys_log_url) USING BTREE COMMENT '请求地址索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci 
COMMENT = '系统操作日志表 - 记录API请求日志用于审计' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 7. 系统配置分组表 (sys_setting_group)
-- 用途: 对系统配置项进行分组管理
-- =====================================================
DROP TABLE IF EXISTS sys_setting_group;
CREATE TABLE sys_setting_group (
    sys_setting_group_id int NOT NULL AUTO_INCREMENT COMMENT '分组ID - 主键自增',
    sys_setting_group_name varchar(255)   NULL DEFAULT NULL COMMENT '分组名称 - 配置分组的显示名称',
    sys_setting_group_code varchar(255)   NULL DEFAULT NULL COMMENT '分组编码 - 配置分组的唯一标识码',
    sys_setting_group_icon varchar(255)   NULL DEFAULT NULL COMMENT '分组图标 - 图标类名或URL',
    sys_setting_group_enable tinyint NULL DEFAULT 1 COMMENT '是否启用 - 0:禁用, 1:启用',
    sys_setting_group_remark varchar(255)   NULL DEFAULT NULL COMMENT '分组描述 - 分组用途说明',
    sys_setting_group_sort int NULL DEFAULT 0 COMMENT '排序号 - 分组在列表中的显示顺序',
    sys_setting_group_use_project_interface tinyint NULL DEFAULT 0 COMMENT '是否使用项目组接口 - 0:否, 1:是',
    create_name varchar(255)   NULL DEFAULT NULL COMMENT '创建人姓名',
    create_by int NULL DEFAULT NULL COMMENT '创建人ID',
    create_time datetime NULL DEFAULT NULL COMMENT '创建时间',
    update_time datetime NULL DEFAULT NULL COMMENT '更新时间',
    update_name varchar(255)   NULL DEFAULT NULL COMMENT '更新人姓名',
    update_by int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (sys_setting_group_id) USING BTREE,
    UNIQUE INDEX uk_group_code (sys_setting_group_code) USING BTREE COMMENT '分组编码唯一索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci 
COMMENT = '系统配置分组表 - 对系统配置项进行分组管理' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 8. 系统全局配置表 (sys_setting)
-- 用途: 存储系统全局配置参数
-- =====================================================
DROP TABLE IF EXISTS sys_setting;
CREATE TABLE sys_setting (
    sys_setting_id int NOT NULL AUTO_INCREMENT COMMENT '配置ID - 主键自增',
    sys_setting_name varchar(255)   NULL DEFAULT NULL COMMENT '配置名称 - 配置项的键名',
    sys_setting_code varchar(255)   NULL DEFAULT NULL COMMENT '配置编码 - 唯一标识码（如 login.title）',
    sys_setting_value varchar(255)   NULL DEFAULT NULL COMMENT '配置值 - 配置项的值',
    sys_setting_value_type varchar(255)   NULL DEFAULT NULL COMMENT '配置类型 - Boolean/String/Number/Color/Dict等，用于前端组件判断',
    sys_setting_group varchar(255)   NULL DEFAULT NULL COMMENT '所属分组 - 配置项所属的分组编码',
    sys_setting_status int NULL DEFAULT 1 COMMENT '是否启用 - 0:禁用, 1:启用',
    sys_setting_sort int NULL DEFAULT 0 COMMENT '排序号 - 配置项在分组中的显示顺序',
    sys_setting_collect int NULL DEFAULT 0 COMMENT '是否多值集合 - 0:单个值, 1:多个值的集合',
    sys_setting_in_system int NULL DEFAULT 0 COMMENT '是否系统配置 - 0:非系统配置, 1:系统内置配置',
    sys_setting_app_inner int NULL DEFAULT 0 COMMENT '是否应用启动配置 - 0:否, 1:是(影响应用启动)',
    sys_setting_admin int NULL DEFAULT 0 COMMENT '是否管理员专属 - 0:所有人可见, 1:仅管理员可见',
    sys_setting_version int NULL DEFAULT 1 COMMENT '配置版本 - 用于配置变更追踪',
    sys_setting_config varchar(255)   NULL DEFAULT NULL COMMENT '配置参数 - 附加的配置参数(如关联字典ID)',
    sys_setting_remark varchar(255)   NULL DEFAULT NULL COMMENT '配置备注 - 配置项用途说明',
    create_name varchar(255)   NULL DEFAULT NULL COMMENT '创建人姓名',
    create_by int NULL DEFAULT NULL COMMENT '创建人ID',
    create_time datetime NULL DEFAULT NULL COMMENT '创建时间',
    update_time datetime NULL DEFAULT NULL COMMENT '更新时间',
    update_name varchar(255)   NULL DEFAULT NULL COMMENT '更新人姓名',
    update_by int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (sys_setting_id) USING BTREE,
    UNIQUE INDEX uk_setting_name_group (sys_setting_name, sys_setting_group) USING BTREE COMMENT '配置名和分组联合唯一索引',
    UNIQUE INDEX uk_setting_code (sys_setting_code) USING BTREE COMMENT '配置编码唯一索引',
    INDEX idx_setting_group (sys_setting_group) USING BTREE COMMENT '分组索引',
    INDEX idx_setting_status (sys_setting_status) USING BTREE COMMENT '状态索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci 
COMMENT = '系统全局配置表 - 存储系统全局配置参数' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 10. 限流配置表 (sys_limit_configuration)
-- 用途: 配置API接口的限流规则
-- =====================================================
DROP TABLE IF EXISTS sys_limit_configuration;
CREATE TABLE sys_limit_configuration (
    sys_limit_configuration_id bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID - 主键自增',
    sys_limit_name varchar(255)   NULL DEFAULT NULL COMMENT '限流规则名称 - 规则的显示名称',
    sys_limit_path varchar(500)   NULL DEFAULT NULL COMMENT '接口路径 - 需要限流的API路径，支持Ant风格匹配',
    sys_limit_for_period int NULL DEFAULT 10 COMMENT '周期许可数 - 每个刷新周期内允许的最大请求数',
    sys_limit_refresh_period_seconds bigint NULL DEFAULT 1 COMMENT '刷新周期 - 限流计数器的刷新周期(秒)',
    sys_limit_timeout_duration_millis bigint NULL DEFAULT 0 COMMENT '超时时间 - 获取许可的超时时间(毫秒)，0表示立即返回',
    sys_limit_dimension varchar(50)   NULL DEFAULT 'GLOBAL' COMMENT '限流维度 - GLOBAL:全局, IP:按IP, USER:按用户, API:按接口',
    sys_limit_key_expression varchar(500)   NULL DEFAULT NULL COMMENT '自定义键表达式 - SpEL表达式，用于提取限流键',
    sys_limit_fallback_method varchar(255)   NULL DEFAULT NULL COMMENT '降级方法 - 限流触发后的降级处理方法名',
    sys_limit_message varchar(500)   NULL DEFAULT '请求过于频繁，请稍后再试' COMMENT '错误消息 - 限流触发时返回给客户端的提示信息',
    sys_limit_status int NULL DEFAULT 1 COMMENT '是否启用 - 0:禁用, 1:启用',
    sys_limit_description varchar(500)   NULL DEFAULT NULL COMMENT '规则描述 - 限流规则的详细说明',
    sys_limit_sort int NULL DEFAULT 0 COMMENT '排序号 - 规则优先级，数字越小优先级越高',
    create_name varchar(255)   NULL DEFAULT NULL COMMENT '创建人姓名',
    create_by int NULL DEFAULT NULL COMMENT '创建人ID',
    create_time datetime NULL DEFAULT NULL COMMENT '创建时间',
    update_time datetime NULL DEFAULT NULL COMMENT '更新时间',
    update_name varchar(255)   NULL DEFAULT NULL COMMENT '更新人姓名',
    update_by int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (sys_limit_configuration_id) USING BTREE,
    INDEX idx_limit_path (sys_limit_path(255)) USING BTREE COMMENT '接口路径索引',
    INDEX idx_limit_status (sys_limit_status) USING BTREE COMMENT '状态索引',
    INDEX idx_limit_dimension (sys_limit_dimension) USING BTREE COMMENT '限流维度索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci 
COMMENT = '限流配置表 - 配置API接口的限流规则' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 11. 限流记录表 (sys_limit_record)
-- 用途: 记录限流触发的详细信息
-- =====================================================
DROP TABLE IF EXISTS sys_limit_record;
CREATE TABLE sys_limit_record (
    sys_limit_record_id bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID - 主键自增',
    sys_limit_configuration_id bigint NULL DEFAULT NULL COMMENT '限流配置ID - 关联sys_limit_configuration表',
    sys_limit_path varchar(500)   NULL DEFAULT NULL COMMENT '接口路径 - 被限流的API路径',
    sys_limit_name varchar(255)   NULL DEFAULT NULL COMMENT '限流规则名称',
    sys_limit_dimension varchar(50)   NULL DEFAULT NULL COMMENT '限流维度 - GLOBAL/IP/USER/API',
    sys_limit_key varchar(255)   NULL DEFAULT NULL COMMENT '限流键值 - 具体的限流标识(如IP地址、用户ID)',
    sys_limit_time datetime NULL DEFAULT NULL COMMENT '限流时间 - 触发限流的时间',
    sys_user_id bigint NULL DEFAULT NULL COMMENT '用户ID - 被限流的用户ID',
    sys_user_name varchar(255)   NULL DEFAULT NULL COMMENT '用户名 - 被限流的用户名',
    client_ip varchar(50)   NULL DEFAULT NULL COMMENT '客户端IP - 请求的客户端IP地址',
    request_url varchar(500)   NULL DEFAULT NULL COMMENT '请求URL - 完整的请求URL',
    request_method varchar(10)   NULL DEFAULT NULL COMMENT '请求方法 - GET/POST/PUT/DELETE等',
    user_agent varchar(500)   NULL DEFAULT NULL COMMENT 'User-Agent - 客户端浏览器UA信息',
    create_name varchar(255)   NULL DEFAULT NULL COMMENT '创建人姓名',
    create_by int NULL DEFAULT NULL COMMENT '创建人ID',
    create_time datetime NULL DEFAULT NULL COMMENT '创建时间',
    update_time datetime NULL DEFAULT NULL COMMENT '更新时间',
    update_name varchar(255)   NULL DEFAULT NULL COMMENT '更新人姓名',
    update_by int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (sys_limit_record_id) USING BTREE,
    INDEX idx_configuration_id (sys_limit_configuration_id) USING BTREE COMMENT '配置ID索引',
    INDEX idx_limit_time (sys_limit_time) USING BTREE COMMENT '限流时间索引',
    INDEX idx_user_id (sys_user_id) USING BTREE COMMENT '用户ID索引',
    INDEX idx_client_ip (client_ip) USING BTREE COMMENT '客户端IP索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci 
COMMENT = '限流记录表 - 记录限流触发的详细信息' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 12. 节假日表 (sys_holiday)
-- 用途: 存储法定节假日和调休工作日信息
-- =====================================================
DROP TABLE IF EXISTS sys_holiday;
CREATE TABLE sys_holiday (
    sys_holiday_id int NOT NULL AUTO_INCREMENT COMMENT '节假日ID - 主键自增',
    sys_holiday_year int NULL DEFAULT NULL COMMENT '节假日年份 - 如2024、2025',
    sys_holiday_date date NULL DEFAULT NULL COMMENT '节假日日期 - 具体日期',
    sys_holiday_name varchar(100)   NULL DEFAULT NULL COMMENT '节假日名称 - 如春节、国庆节、调休',
    sys_holiday_is_holiday tinyint NULL DEFAULT 1 COMMENT '是否节假日 - 1:节假日休息, 0:调休工作日',
    create_by int NULL DEFAULT NULL COMMENT '创建人ID',
    create_name varchar(255)   NULL DEFAULT NULL COMMENT '创建人姓名',
    create_time datetime NULL DEFAULT NULL COMMENT '创建时间',
    update_by int NULL DEFAULT NULL COMMENT '更新人ID',
    update_name varchar(255)   NULL DEFAULT NULL COMMENT '更新人姓名',
    update_time datetime NULL DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (sys_holiday_id) USING BTREE,
    UNIQUE INDEX uk_date (sys_holiday_date) USING BTREE COMMENT '日期唯一索引 - 确保每个日期只有一条记录',
    INDEX idx_year (sys_holiday_year) USING BTREE COMMENT '年份索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci 
COMMENT = '节假日表 - 存储法定节假日和调休工作日' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 15. 文件系统分组表 (sys_file_system_group)
-- 用途: 对上传的文件进行分组管理
-- =====================================================
DROP TABLE IF EXISTS sys_file_system_group;
CREATE TABLE sys_file_system_group (
    sys_file_system_group_id int NOT NULL AUTO_INCREMENT COMMENT '分组ID - 主键自增',
    sys_file_system_group_name varchar(100)   NULL DEFAULT NULL COMMENT '分组名称 - 文件分组的显示名称',
    sys_file_system_group_parent_id int NULL DEFAULT 0 COMMENT '父分组ID - 支持多级分组，0表示顶级分组',
    sys_file_system_group_path varchar(255)   NULL DEFAULT NULL COMMENT '分组路径 - 分组的短路径标识',
    sys_file_system_group_full_path varchar(500)   NULL DEFAULT NULL COMMENT '完整路径 - 从根到当前分组的完整路径',
    sys_file_system_group_level int NULL DEFAULT 1 COMMENT '分组层级 - 分组在树中的层级深度',
    sys_file_system_group_tree_path varchar(500)   NULL DEFAULT NULL COMMENT '树路径 - 分组ID组成的树路径，用于快速查询',
    sys_file_system_group_description varchar(500)   NULL DEFAULT NULL COMMENT '分组描述 - 分组用途说明',
    sys_file_system_group_is_default tinyint NULL DEFAULT 0 COMMENT '是否默认分组 - 0:否, 1:是(新文件默认归入)',
    sys_file_system_group_sort int NULL DEFAULT 0 COMMENT '排序号 - 分组在列表中的显示顺序',
    sys_file_system_group_status int NULL DEFAULT 1 COMMENT '分组状态 - 0:禁用, 1:启用',
    sys_file_system_group_icon varchar(100)   NULL DEFAULT NULL COMMENT '分组图标 - 图标类名',
    sys_file_system_group_color varchar(20)   NULL DEFAULT NULL COMMENT '分组颜色 - 十六进制颜色值',
    create_name varchar(255)   NULL DEFAULT NULL COMMENT '创建人姓名',
    create_by int NULL DEFAULT NULL COMMENT '创建人ID',
    create_time datetime NULL DEFAULT NULL COMMENT '创建时间',
    update_time datetime NULL DEFAULT NULL COMMENT '更新时间',
    update_name varchar(255)   NULL DEFAULT NULL COMMENT '更新人姓名',
    update_by int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (sys_file_system_group_id) USING BTREE,
    INDEX idx_parent_id (sys_file_system_group_parent_id) USING BTREE COMMENT '父分组索引',
    INDEX idx_status (sys_file_system_group_status) USING BTREE COMMENT '状态索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci 
COMMENT = '文件系统分组表 - 对上传的文件进行分组管理' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 16. 文件系统配置表 (sys_file_system_setting)
-- 用途: 配置文件上传和存储的参数
-- =====================================================
DROP TABLE IF EXISTS sys_file_system_setting;
CREATE TABLE sys_file_system_setting (
    sys_file_system_setting_id int NOT NULL AUTO_INCREMENT COMMENT '配置ID - 主键自增',
    sys_file_system_setting_name varchar(100)   NULL DEFAULT NULL COMMENT '配置名称 - 配置方案的名称',
    sys_file_system_setting_chunk_enabled tinyint NULL DEFAULT 1 COMMENT '是否启用分片上传 - 0:否, 1:是',
    sys_file_system_setting_chunk_size_mb int NULL DEFAULT 100 COMMENT '分片大小 - 每个分片的大小(MB)',
    sys_file_system_setting_auto_merge_enabled tinyint NULL DEFAULT 1 COMMENT '是否自动合并 - 0:否, 1:是(分片上传完成后自动合并)',
    sys_file_system_setting_merge_task_limit int NULL DEFAULT 10 COMMENT '合并任务限制 - 同时进行的合并任务最大数量',
    sys_file_system_setting_manual_merge_enabled tinyint NULL DEFAULT 1 COMMENT '是否支持手动合并 - 0:否, 1:是',
    sys_file_system_setting_http_server_enabled tinyint NULL DEFAULT 0 COMMENT '是否开启HTTP服务 - 0:否, 1:是',
    sys_file_system_setting_http_server_port int NULL DEFAULT 8888 COMMENT 'HTTP服务端口',
    sys_file_system_setting_http_server_host varchar(100)   NULL DEFAULT '0.0.0.0' COMMENT 'HTTP服务主机',
    sys_file_system_setting_http_server_context varchar(100)   NULL DEFAULT '/file' COMMENT 'HTTP上下文路径',
    sys_file_system_setting_download_enabled tinyint NULL DEFAULT 1 COMMENT '是否启用下载 - 0:否, 1:是',
    sys_file_system_setting_preview_enabled tinyint NULL DEFAULT 1 COMMENT '是否启用预览 - 0:否, 1:是',
    sys_file_system_setting_http_access_domain varchar(255)   NULL DEFAULT NULL COMMENT 'HTTP访问域名 - 文件访问的域名前缀',
    sys_file_system_setting_allowed_types varchar(1000)   NULL DEFAULT NULL COMMENT '允许的文件类型 - 文件扩展名白名单，逗号分隔',
    sys_file_system_setting_max_file_size_mb int NULL DEFAULT 1024 COMMENT '单文件最大大小 - 单个文件的最大大小(MB)',
    sys_file_system_setting_storage_root_path varchar(500)   NULL DEFAULT NULL COMMENT '存储根路径 - 文件存储的根目录路径',
    sys_file_system_setting_temp_path varchar(500)   NULL DEFAULT NULL COMMENT '临时文件路径 - 分片上传的临时存储目录',
    sys_file_system_setting_retention_days int NULL DEFAULT 0 COMMENT '文件保留天数 - 0表示永久保留',
    sys_file_system_setting_enabled tinyint NULL DEFAULT 1 COMMENT '是否启用 - 0:禁用, 1:启用',
    sys_file_system_setting_default_storage_type varchar(50)   NULL DEFAULT 'local' COMMENT '默认存储类型 - local:本地, oss:阿里云OSS, cos:腾讯云COS等',
    sys_file_system_setting_remark varchar(500)   NULL DEFAULT NULL COMMENT '配置备注',
    create_name varchar(255)   NULL DEFAULT NULL COMMENT '创建人姓名',
    create_by int NULL DEFAULT NULL COMMENT '创建人ID',
    create_time datetime NULL DEFAULT NULL COMMENT '创建时间',
    update_time datetime NULL DEFAULT NULL COMMENT '更新时间',
    update_name varchar(255)   NULL DEFAULT NULL COMMENT '更新人姓名',
    update_by int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (sys_file_system_setting_id) USING BTREE,
    INDEX idx_enabled (sys_file_system_setting_enabled) USING BTREE COMMENT '启用状态索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci 
COMMENT = '文件系统配置表 - 配置文件上传和存储的参数' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 17. 文件系统主表 (sys_file_system)
-- 用途: 存储上传文件的元数据信息
-- =====================================================
DROP TABLE IF EXISTS sys_file_system;
CREATE TABLE sys_file_system (
    sys_file_system_id int NOT NULL AUTO_INCREMENT COMMENT '文件ID - 主键自增',
    sys_file_system_tid varchar(64)   NULL DEFAULT NULL COMMENT '任务唯一标识 - 上传任务的唯一ID，用于断点续传',
    sys_file_system_bucket varchar(255)   NULL DEFAULT NULL COMMENT '存储桶 - 对象存储的桶名称',
    sys_file_system_name varchar(255)   NULL DEFAULT NULL COMMENT '文件名称 - 存储后的文件名(通常是UUID)',
    sys_file_system_original_name varchar(255)   NULL DEFAULT NULL COMMENT '原始文件名 - 用户上传时的原始文件名',
    sys_file_system_type varchar(50)   NULL DEFAULT NULL COMMENT '存储类型 - local/oss/cos/minio等',
    sys_file_system_extension varchar(20)   NULL DEFAULT NULL COMMENT '文件扩展名 - 如jpg、pdf、docx',
    sys_file_system_path varchar(500)   NULL DEFAULT NULL COMMENT '文件路径 - 文件在存储中的相对路径',
    sys_file_system_size bigint NULL DEFAULT NULL COMMENT '文件大小 - 文件大小(字节)',
    sys_file_system_md5 varchar(32)   NULL DEFAULT NULL COMMENT '文件MD5 - 文件的MD5哈希值，用于秒传和完整性校验',
    sys_file_system_chunk_total int NULL DEFAULT NULL COMMENT '分片总数 - 分片上传时的总分片数',
    sys_file_system_chunk_uploaded int NULL DEFAULT 0 COMMENT '已上传分片数 - 当前已完成上传的分片数量',
    sys_file_system_chunk_size bigint NULL DEFAULT NULL COMMENT '分片大小 - 每个分片的大小(字节)',
    sys_file_system_version int NULL DEFAULT 0 COMMENT '版本号 - 文件版本，用于乐观锁',
    sys_file_system_status int NULL DEFAULT 0 COMMENT '文件状态 - 0:上传中, 1:上传完成, 2:合并中, 3:已完成, -1:上传失败',
    sys_file_system_http_enabled tinyint NULL DEFAULT 0 COMMENT '是否启用HTTP访问 - 0:否, 1:是',
    sys_file_system_http_url varchar(500)   NULL DEFAULT NULL COMMENT 'HTTP访问URL - 文件的完整访问地址',
    sys_file_system_service_id int NULL DEFAULT NULL COMMENT '绑定服务ID - 关联的业务服务ID',
    sys_file_system_group_id int NULL DEFAULT NULL COMMENT '分组ID - 关联sys_file_system_group表',
    sys_file_system_upload_start_time datetime NULL DEFAULT NULL COMMENT '上传开始时间',
    sys_file_system_upload_complete_time datetime NULL DEFAULT NULL COMMENT '上传完成时间',
    sys_file_system_local_path varchar(500)   NULL DEFAULT NULL COMMENT '本地临时路径 - 分片上传时的临时存储路径',
    sys_file_system_summary varchar(500)   NULL DEFAULT NULL COMMENT '文件描述 - 文件的备注说明',
    create_name varchar(255)   NULL DEFAULT NULL COMMENT '创建人姓名',
    create_by int NULL DEFAULT NULL COMMENT '创建人ID',
    create_time datetime NULL DEFAULT NULL COMMENT '创建时间',
    update_time datetime NULL DEFAULT NULL COMMENT '更新时间',
    update_name varchar(255)   NULL DEFAULT NULL COMMENT '更新人姓名',
    update_by int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (sys_file_system_id) USING BTREE,
    UNIQUE INDEX uk_tid (sys_file_system_tid) USING BTREE COMMENT '任务ID唯一索引',
    INDEX idx_status (sys_file_system_status) USING BTREE COMMENT '状态索引',
    INDEX idx_group_id (sys_file_system_group_id) USING BTREE COMMENT '分组ID索引',
    INDEX idx_create_time (create_time) USING BTREE COMMENT '创建时间索引',
    INDEX idx_md5 (sys_file_system_md5) USING BTREE COMMENT 'MD5索引 - 用于秒传查询'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci 
COMMENT = '文件系统主表 - 存储上传文件的元数据信息' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 18. 文件系统分片表 (sys_file_system_part)
-- 用途: 存储分片上传的每个分片信息
-- =====================================================
DROP TABLE IF EXISTS sys_file_system_part;
CREATE TABLE sys_file_system_part (
    sys_file_system_part_id int NOT NULL AUTO_INCREMENT COMMENT '分片ID - 主键自增',
    sys_file_system_part_file_id int NOT NULL COMMENT '文件ID - 关联sys_file_system表',
    sys_file_system_part_seq int NOT NULL COMMENT '分片序号 - 分片在文件中的顺序号，从1开始',
    sys_file_system_part_size bigint NULL DEFAULT NULL COMMENT '分片大小 - 该分片的大小(字节)',
    sys_file_system_part_md5 varchar(32)   NULL DEFAULT NULL COMMENT '分片MD5 - 该分片的MD5哈希值',
    sys_file_system_part_local_path varchar(500)   NULL DEFAULT NULL COMMENT '本地存储路径 - 分片在本地的临时存储路径',
    sys_file_system_part_version int NULL DEFAULT 0 COMMENT '版本号 - 用于乐观锁',
    sys_file_system_part_status int NULL DEFAULT 0 COMMENT '分片状态 - 0:等待上传, 1:上传中, 2:上传完成, -1:上传失败',
    sys_file_system_part_upload_time datetime NULL DEFAULT NULL COMMENT '上传时间 - 分片上传完成的时间',
    sys_file_system_part_process_time datetime NULL DEFAULT NULL COMMENT '处理时间 - 分片被处理(合并)的时间',
    sys_file_system_part_error_message varchar(1000)   NULL DEFAULT NULL COMMENT '错误信息 - 上传失败时的错误消息',
    sys_file_system_part_retry_count int NULL DEFAULT 0 COMMENT '重试次数 - 上传失败后的重试次数',
    create_name varchar(255)   NULL DEFAULT NULL COMMENT '创建人姓名',
    create_by int NULL DEFAULT NULL COMMENT '创建人ID',
    create_time datetime NULL DEFAULT NULL COMMENT '创建时间',
    update_time datetime NULL DEFAULT NULL COMMENT '更新时间',
    update_name varchar(255)   NULL DEFAULT NULL COMMENT '更新人姓名',
    update_by int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (sys_file_system_part_id) USING BTREE,
    UNIQUE INDEX uk_file_seq (sys_file_system_part_file_id, sys_file_system_part_seq) USING BTREE COMMENT '文件和序号联合唯一索引',
    INDEX idx_file_id (sys_file_system_part_file_id) USING BTREE COMMENT '文件ID索引',
    INDEX idx_status (sys_file_system_part_status) USING BTREE COMMENT '状态索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci 
COMMENT = '文件系统分片表 - 存储分片上传的每个分片信息' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 19. 请求追踪原始记录表 (sys_request_trace_raw)
-- 用途: 持久化注解命中的请求原始事件，用于热点/刷屏/爬虫分析
-- =====================================================
DROP TABLE IF EXISTS sys_request_trace_raw;
CREATE TABLE sys_request_trace_raw (
    sys_request_trace_raw_id bigint NOT NULL AUTO_INCREMENT COMMENT '原始记录ID - 主键自增',
    sys_request_trace_time datetime NULL DEFAULT NULL COMMENT '请求时间',
    sys_request_trace_trace_id varchar(64)   NULL DEFAULT NULL COMMENT '追踪ID',
    sys_request_trace_strategy_code varchar(128)   NULL DEFAULT NULL COMMENT '策略编码',
    sys_request_trace_method varchar(16)   NULL DEFAULT NULL COMMENT '请求方法',
    sys_request_trace_path varchar(500)   NULL DEFAULT NULL COMMENT '请求路径',
    sys_request_trace_route varchar(500)   NULL DEFAULT NULL COMMENT '路由模板',
    sys_request_trace_query text   NULL COMMENT '查询参数',
    sys_request_trace_url varchar(1000)   NULL DEFAULT NULL COMMENT '请求地址',
    sys_request_trace_status int NULL DEFAULT NULL COMMENT '响应状态码',
    sys_request_trace_duration_ms bigint NULL DEFAULT NULL COMMENT '耗时毫秒',
    sys_request_trace_client_ip varchar(64)   NULL DEFAULT NULL COMMENT '客户端IP',
    sys_request_trace_browser varchar(64)   NULL DEFAULT NULL COMMENT '浏览器',
    sys_request_trace_user_agent varchar(1024)   NULL DEFAULT NULL COMMENT '浏览器标识',
    sys_request_trace_app_name varchar(255)   NULL DEFAULT NULL COMMENT '应用名称',
    sys_request_trace_user_id varchar(64)   NULL DEFAULT NULL COMMENT '用户ID',
    sys_request_trace_username varchar(255)   NULL DEFAULT NULL COMMENT '用户名',
    sys_request_trace_fingerprint varchar(255)   NULL DEFAULT NULL COMMENT '客户端指纹',
    sys_request_trace_token_hash varchar(255)   NULL DEFAULT NULL COMMENT '令牌摘要',
    sys_request_trace_signals varchar(255)   NULL DEFAULT NULL COMMENT '命中信号',
    create_name varchar(255)   NULL DEFAULT NULL COMMENT '创建人姓名',
    create_by int NULL DEFAULT NULL COMMENT '创建人ID',
    create_time datetime NULL DEFAULT NULL COMMENT '创建时间',
    update_time datetime NULL DEFAULT NULL COMMENT '更新时间',
    update_name varchar(255)   NULL DEFAULT NULL COMMENT '更新人姓名',
    update_by int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (sys_request_trace_raw_id) USING BTREE,
    INDEX idx_request_time (sys_request_trace_time) USING BTREE COMMENT '请求时间索引',
    INDEX idx_trace_id (sys_request_trace_trace_id) USING BTREE COMMENT '追踪ID索引',
    INDEX idx_strategy_code (sys_request_trace_strategy_code) USING BTREE COMMENT '策略编码索引',
    INDEX idx_client_ip (sys_request_trace_client_ip) USING BTREE COMMENT '客户端IP索引',
    INDEX idx_user_id (sys_request_trace_user_id) USING BTREE COMMENT '用户ID索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci
COMMENT = '请求追踪原始记录表 - 持久化行为分析原始事件' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 20. 请求追踪行为分析结果表 (sys_request_trace_behavior)
-- 用途: 持久化AI/规则分析后的风险结果，用于审计与处置
-- =====================================================
DROP TABLE IF EXISTS sys_request_trace_behavior;
CREATE TABLE sys_request_trace_behavior (
    sys_request_trace_behavior_id bigint NOT NULL AUTO_INCREMENT COMMENT '行为结果ID - 主键自增',
    sys_request_trace_request_time datetime NULL DEFAULT NULL COMMENT '请求时间',
    sys_request_trace_analyzed_at datetime NULL DEFAULT NULL COMMENT '分析时间',
    sys_request_trace_trace_id varchar(64)   NULL DEFAULT NULL COMMENT '追踪ID',
    sys_request_trace_strategy_code varchar(128)   NULL DEFAULT NULL COMMENT '策略编码',
    sys_request_trace_route varchar(500)   NULL DEFAULT NULL COMMENT '路由',
    sys_request_trace_url varchar(1000)   NULL DEFAULT NULL COMMENT '请求地址',
    sys_request_trace_status int NULL DEFAULT NULL COMMENT '响应状态码',
    sys_request_trace_duration_ms bigint NULL DEFAULT NULL COMMENT '耗时毫秒',
    sys_request_trace_client_ip varchar(64)   NULL DEFAULT NULL COMMENT '客户端IP',
    sys_request_trace_browser varchar(64)   NULL DEFAULT NULL COMMENT '浏览器',
    sys_request_trace_user_id varchar(64)   NULL DEFAULT NULL COMMENT '用户ID',
    sys_request_trace_username varchar(255)   NULL DEFAULT NULL COMMENT '用户名',
    sys_request_trace_fingerprint varchar(255)   NULL DEFAULT NULL COMMENT '客户端指纹',
    sys_request_trace_user_agent varchar(1024)   NULL DEFAULT NULL COMMENT '浏览器标识',
    sys_request_trace_signals varchar(255)   NULL DEFAULT NULL COMMENT '命中信号',
    sys_request_trace_related_request_count bigint NULL DEFAULT NULL COMMENT '关联请求数量',
    sys_request_trace_distinct_path_count bigint NULL DEFAULT NULL COMMENT '扫描路径数量',
    sys_request_trace_risk_level varchar(32)   NULL DEFAULT NULL COMMENT '风险等级',
    sys_request_trace_summary text   NULL COMMENT '规则摘要',
    sys_request_trace_ai_summary text   NULL COMMENT 'AI摘要',
    sys_request_trace_extra text   NULL COMMENT '扩展信息',
    create_name varchar(255)   NULL DEFAULT NULL COMMENT '创建人姓名',
    create_by int NULL DEFAULT NULL COMMENT '创建人ID',
    create_time datetime NULL DEFAULT NULL COMMENT '创建时间',
    update_time datetime NULL DEFAULT NULL COMMENT '更新时间',
    update_name varchar(255)   NULL DEFAULT NULL COMMENT '更新人姓名',
    update_by int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (sys_request_trace_behavior_id) USING BTREE,
    INDEX idx_behavior_request_time (sys_request_trace_request_time) USING BTREE COMMENT '请求时间索引',
    INDEX idx_behavior_analyzed_at (sys_request_trace_analyzed_at) USING BTREE COMMENT '分析时间索引',
    INDEX idx_behavior_trace_id (sys_request_trace_trace_id) USING BTREE COMMENT '追踪ID索引',
    INDEX idx_behavior_strategy (sys_request_trace_strategy_code) USING BTREE COMMENT '策略编码索引',
    INDEX idx_behavior_risk_level (sys_request_trace_risk_level) USING BTREE COMMENT '风险等级索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci
COMMENT = '请求追踪行为分析结果表 - 持久化风险分析结论' ROW_FORMAT = DYNAMIC;




/*
 * 系统管理模块 - 数据库初始化脚本
 * 
 * 包含表:
 *   - sys_dept: 组织机构表
 *   - sys_dept_principal_rel: 部门负责人关系表
 *   - sys_role: 角色表
 *   - sys_role_menu: 角色-菜单关系表
 *   - sys_menu: 菜单表
 *   - sys_mini_menu: 小程序菜单卡片表
 *   - sys_feedback: 系统反馈表
 *   - sys_user: 用户表
 *   - sys_user_role: 用户-角色关系表
 *   - sys_user_config: 用户配置表(第三方绑定)
 *   - sys_user_layout: 用户布局配置表
 *   - sys_user_log: 用户登录日志表
 *   - sys_sms_log: 短信发送日志表
 *   - sys_log: 系统操作日志表
 *   - sys_sfc: 组件库表
 *   - sys_sfc_install: 用户安装组件表
 *   - sys_user_third_party: 用户第三方绑定表
 *   - sys_wechat_user: 微信用户表
 *   - sys_announcement: 系统公告表
 *   - sys_avatar_frame: 头像框定义表
 *   - sys_user_avatar_frame: 用户头像框关联表
 *
 * 说明: 本脚本已合并原 V1.1(sys_mini_menu)/V1.2(sys_feedback)/V1.3(avatar)/V1.4(sys_menu 徽标列) 增量内容。
 *
 * @author System
 * @since 2025-01-01
 * @version 1.0
 */




-- =====================================================
-- 1. 组织机构表 (sys_dept)
-- 用途: 存储企业/系统的组织架构信息，支持树形结构
-- =====================================================
DROP TABLE IF EXISTS sys_dept;
CREATE TABLE sys_dept (
    sys_dept_id int NOT NULL AUTO_INCREMENT COMMENT '机构ID - 主键自增',
    sys_dept_name varchar(255)  NULL DEFAULT NULL COMMENT '机构名称 - 组织/部门的显示名称',
    sys_dept_pid int NULL DEFAULT NULL COMMENT '父机构ID - 上级组织的ID，顶级为0',
    sys_dept_tree_id varchar(255)  NULL DEFAULT NULL COMMENT '机构链路 - 从根节点到当前节点的ID路径，如"0,1,2"',
    sys_dept_code varchar(255)  NULL DEFAULT NULL COMMENT '组织编码 - 机构的唯一业务编码',
    sys_dept_principal varchar(255)  NULL DEFAULT NULL COMMENT '部门负责人 - 该部门的负责人姓名',
    sys_dept_contact varchar(255)  NULL DEFAULT NULL COMMENT '负责人联系方式 - 负责人的电话或其他联系方式',
    sys_dept_sort int NULL DEFAULT NULL COMMENT '排序优先级 - 数值越小越靠前',
    sys_dept_status int NULL DEFAULT NULL COMMENT '状态 - 0:启用, 1:禁用',
    sys_dept_remark varchar(255)  NULL DEFAULT NULL COMMENT '备注 - 附加说明信息',
    sys_dept_data_permission int NULL DEFAULT NULL COMMENT '数据权限类型 - 1:全部可见, 2:本人可见, 3:所在部门可见, 4:所在部门及子级可见, 5:选择的部门可见',
    sys_dept_data_permission_dept_id varchar(255)  NULL DEFAULT NULL COMMENT '数据权限可见部门ID列表 - 当数据权限类型为5时，存储可见的部门ID，逗号分隔',
    create_name varchar(255)  NULL DEFAULT NULL COMMENT '创建人姓名',
    create_by int NULL DEFAULT NULL COMMENT '创建人ID',
    create_time datetime NULL DEFAULT NULL COMMENT '创建时间',
    update_time datetime NULL DEFAULT NULL COMMENT '更新时间',
    update_name varchar(255)  NULL DEFAULT NULL COMMENT '更新人姓名',
    update_by int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (sys_dept_id) USING BTREE,
    INDEX idx_dept_pid (sys_dept_pid) USING BTREE COMMENT '父机构索引',
    INDEX idx_dept_code (sys_dept_code) USING BTREE COMMENT '机构编码索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '组织机构表 - 存储系统的部门/组织架构信息' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 2. 部门负责人关系表 (sys_dept_principal_rel)
-- 用途: 存储部门与负责人的多对多关系
-- =====================================================
DROP TABLE IF EXISTS sys_dept_principal_rel;
CREATE TABLE sys_dept_principal_rel (
    sys_dept_principal_rel_id int NOT NULL AUTO_INCREMENT COMMENT '关系ID - 主键自增',
    sys_dept_id int NOT NULL COMMENT '部门ID - 关联组织机构表',
    sys_user_id int NOT NULL COMMENT '负责人用户ID - 关联用户表',
    PRIMARY KEY (sys_dept_principal_rel_id) USING BTREE,
    UNIQUE INDEX uk_dept_user (sys_dept_id, sys_user_id) USING BTREE COMMENT '部门负责人唯一索引',
    INDEX idx_principal_user_id (sys_user_id) USING BTREE COMMENT '负责人用户索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '部门负责人关系表 - 存储部门负责人的多对多关系' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 3. 角色表 (sys_role)
-- 用途: 存储系统角色信息，用于权限控制
-- =====================================================
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    sys_role_id int NOT NULL AUTO_INCREMENT COMMENT '角色ID - 主键自增',
    sys_role_name varchar(255)  NULL DEFAULT NULL COMMENT '角色名称 - 角色的显示名称，如"管理员"',
    sys_role_code varchar(255)  NULL DEFAULT NULL COMMENT '角色编码 - 角色的唯一标识码，如"ADMIN"',
    sys_role_in_system int NULL DEFAULT NULL COMMENT '是否系统角色 - 0:非系统角色, 1:系统内置角色(不可删除)',
    sys_role_status int NULL DEFAULT NULL COMMENT '是否启用 - 0:禁用, 1:启用',
    sys_role_sort int NULL DEFAULT NULL COMMENT '排序优先级 - 数值越小越靠前',
    sys_role_remark varchar(255)  NULL DEFAULT NULL COMMENT '备注 - 角色的说明描述',
    sys_role_hidden int NULL DEFAULT 0 COMMENT '是否隐藏 - 0:显示, 1:隐藏(前端不展示)',
    sys_role_readable int NULL DEFAULT NULL COMMENT '角色可读权限位 - 二进制位表示的读权限',
    sys_role_writeable int NULL DEFAULT NULL COMMENT '角色可写权限位 - 二进制位表示的写权限',
    sys_role_executable int NULL DEFAULT NULL COMMENT '角色可执行权限位 - 二进制位表示的执行权限',
    sys_role_board_card int NULL DEFAULT NULL COMMENT '角色可看板类型 - 该角色可查看的看板类型',
    create_name varchar(255)  NULL DEFAULT NULL COMMENT '创建人姓名',
    create_by int NULL DEFAULT NULL COMMENT '创建人ID',
    create_time datetime NULL DEFAULT NULL COMMENT '创建时间',
    update_time datetime NULL DEFAULT NULL COMMENT '更新时间',
    update_name varchar(255)  NULL DEFAULT NULL COMMENT '更新人姓名',
    update_by int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (sys_role_id) USING BTREE,
    UNIQUE INDEX uk_role_code (sys_role_code) USING BTREE COMMENT '角色编码唯一索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '角色表 - 存储系统角色信息，用于RBAC权限控制' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 4. 菜单表 (sys_menu)
-- 用途: 存储系统菜单/权限点信息，支持树形结构
-- =====================================================
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    sys_menu_id int NOT NULL AUTO_INCREMENT COMMENT '菜单ID - 主键自增',
    sys_menu_name varchar(255)  NULL DEFAULT NULL COMMENT '菜单名称(路由名) - 用于路由匹配的唯一名称',
    sys_menu_path varchar(255)  NULL DEFAULT NULL COMMENT '菜单路径 - 前端路由路径，如"/manage/user"',
    sys_menu_i18n varchar(255)  NULL DEFAULT NULL COMMENT '国际化key - 用于多语言显示',
    sys_menu_title varchar(255)  NULL DEFAULT NULL COMMENT '菜单标题 - 显示在界面上的名称',
    sys_menu_perm varchar(255)  NULL DEFAULT NULL COMMENT '权限标识 - 如"sys:user:add"',
    sys_menu_pid int NULL DEFAULT NULL COMMENT '父菜单ID - 上级菜单的ID，顶级为0',
    sys_menu_tree_id varchar(255)  NULL DEFAULT NULL COMMENT '菜单路径链 - 从根节点到当前节点的ID路径',
    sys_menu_component varchar(255)  NULL DEFAULT NULL COMMENT '菜单组件路径 - Vue组件的路径，如"/manage/user/index"',
    sys_menu_icon varchar(255)  NULL DEFAULT NULL COMMENT '菜单图标 - 图标名称，如"ep:user"',
    sys_menu_extra_icon varchar(255)  NULL DEFAULT NULL COMMENT '右侧额外图标 - 菜单右侧显示的图标',
    sys_menu_type int NULL DEFAULT NULL COMMENT '菜单类型 - 0:菜单, 1:iframe, 2:外链, 3:按钮',
    sys_menu_sort int NULL DEFAULT NULL COMMENT '排序优先级 - 数值越小越靠前',
    sys_menu_hidden int NULL DEFAULT NULL COMMENT '是否隐藏 - 0:显示, 1:隐藏',
    sys_menu_show_parent int NULL DEFAULT NULL COMMENT '是否显示父级菜单 - 0:显示, 1:隐藏父级',
    sys_menu_hidden_tag int NULL DEFAULT NULL COMMENT '是否在标签栏隐藏 - 0:显示, 1:隐藏',
sys_menu_fixed_tag int NULL DEFAULT NULL COMMENT '是否固定标签 - 0:不固定, 1:固定在标签栏',
    sys_menu_is_tag tinyint NULL DEFAULT 0 COMMENT '是否开启菜单徽标 - 0:否 1:是',
    sys_menu_tag_type varchar(20)  NULL DEFAULT NULL COMMENT '徽标类型 - danger/success/info/warning/primary',
    sys_menu_tag_name varchar(50)  NULL DEFAULT NULL COMMENT '徽标名称 - 留空则显示圆点徽标',
    sys_menu_redirect varchar(255)  NULL DEFAULT NULL COMMENT '路由重定向地址 - 访问该菜单时跳转的地址',
    sys_menu_enter_transition varchar(255)  NULL DEFAULT NULL COMMENT '进场动画 - 页面进入时的动画效果',
    sys_menu_leave_transition varchar(255)  NULL DEFAULT NULL COMMENT '离场动画 - 页面离开时的动画效果',
    sys_menu_active_path varchar(255)  NULL DEFAULT NULL COMMENT '菜单激活路径 - 当匹配该路径时激活此菜单',
    sys_menu_frame_src varchar(255)  NULL DEFAULT NULL COMMENT '内嵌链接地址 - iframe或外链的URL',
    sys_menu_keep_alive int NULL DEFAULT NULL COMMENT '是否缓存页面 - 0:不缓存, 1:缓存(keep-alive)',
    sys_menu_role varchar(255)  NULL DEFAULT NULL COMMENT '所属角色编码 - 可访问该菜单的角色编码，逗号分隔',
    sys_menu_home int NULL DEFAULT NULL COMMENT '是否只显示在主页 - 0:否, 1:是',
    sys_menu_engine int NULL DEFAULT NULL COMMENT '是否engine布局主页 - 0:否, 1:是',
    create_name varchar(255)  NULL DEFAULT NULL COMMENT '创建人姓名',
    create_by int NULL DEFAULT NULL COMMENT '创建人ID',
    create_time datetime NULL DEFAULT NULL COMMENT '创建时间',
    update_time datetime NULL DEFAULT NULL COMMENT '更新时间',
    update_name varchar(255)  NULL DEFAULT NULL COMMENT '更新人姓名',
    update_by int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (sys_menu_id DESC) USING BTREE,
    INDEX idx_menu_pid (sys_menu_pid) USING BTREE COMMENT '父菜单索引',
    INDEX idx_menu_perm (sys_menu_perm) USING BTREE COMMENT '权限标识索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '菜单表 - 存储系统菜单和按钮权限信息' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 5. 小程序菜单卡片表 (sys_mini_menu)
-- 用途: 存储小程序首页卡片配置，与PC菜单完全分离
-- =====================================================
DROP TABLE IF EXISTS sys_mini_menu;
CREATE TABLE sys_mini_menu (
    sys_mini_menu_id int NOT NULL AUTO_INCREMENT COMMENT '小程序菜单ID - 主键自增',
    sys_mini_menu_title varchar(255)  NULL DEFAULT NULL COMMENT '卡片标题',
    sys_mini_menu_subtitle varchar(255)  NULL DEFAULT NULL COMMENT '卡片副标题',
    sys_mini_menu_category varchar(128)  NULL DEFAULT NULL COMMENT '卡片分组',
    sys_mini_menu_path varchar(255)  NULL DEFAULT NULL COMMENT '小程序页面路径',
    sys_mini_menu_jump_mode int NULL DEFAULT 0 COMMENT '跳转模式 - 0:普通页面, 1:tabBar页面, 2:webview',
    sys_mini_menu_icon varchar(255)  NULL DEFAULT NULL COMMENT '卡片图标',
    sys_mini_menu_cover varchar(255)  NULL DEFAULT NULL COMMENT '卡片封面/背景',
    sys_mini_menu_badge varchar(64)  NULL DEFAULT NULL COMMENT '角标文案',
    sys_mini_menu_badge_type varchar(64)  NULL DEFAULT NULL COMMENT '角标样式',
    sys_mini_menu_perm varchar(255)  NULL DEFAULT NULL COMMENT '权限标识',
    sys_mini_menu_role varchar(255)  NULL DEFAULT NULL COMMENT '所属角色编码',
    sys_mini_menu_sort int NULL DEFAULT 1 COMMENT '排序优先级',
    sys_mini_menu_keep_alive int NULL DEFAULT 0 COMMENT '是否缓存 - 0:否, 1:是',
    sys_mini_menu_hidden int NULL DEFAULT 0 COMMENT '是否隐藏 - 0:显示, 1:隐藏',
    sys_mini_menu_description varchar(255)  NULL DEFAULT NULL COMMENT '卡片说明',
    create_name varchar(255)  NULL DEFAULT NULL COMMENT '创建人姓名',
    create_by int NULL DEFAULT NULL COMMENT '创建人ID',
    create_time datetime NULL DEFAULT NULL COMMENT '创建时间',
    update_time datetime NULL DEFAULT NULL COMMENT '更新时间',
    update_name varchar(255)  NULL DEFAULT NULL COMMENT '更新人姓名',
    update_by int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (sys_mini_menu_id) USING BTREE,
    INDEX idx_mini_menu_sort (sys_mini_menu_sort) USING BTREE COMMENT '排序索引',
    INDEX idx_mini_menu_perm (sys_mini_menu_perm) USING BTREE COMMENT '权限标识索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '小程序菜单卡片表 - 存储小程序首页卡片与权限配置' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 6. 角色-菜单关系表 (sys_role_menu)
-- 用途: 存储角色与菜单的多对多关系
-- =====================================================
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
    sys_role_menu_id int NOT NULL AUTO_INCREMENT COMMENT '关系ID - 主键自增',
    sys_role_id int NULL DEFAULT NULL COMMENT '角色ID - 关联角色表',
    sys_menu_id int NULL DEFAULT NULL COMMENT '菜单ID - 关联菜单表',
    PRIMARY KEY (sys_role_menu_id) USING BTREE,
    INDEX idx_role_id (sys_role_id) USING BTREE COMMENT '角色ID索引',
    INDEX idx_menu_id (sys_menu_id) USING BTREE COMMENT '菜单ID索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '角色-菜单关系表 - 存储角色与菜单的多对多映射关系' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 7. 系统反馈表 (sys_feedback)
-- 用途: 存储帮助与反馈、异常上报等用户反馈内容
-- =====================================================
DROP TABLE IF EXISTS sys_feedback;
CREATE TABLE sys_feedback (
    sys_feedback_id int NOT NULL AUTO_INCREMENT COMMENT '反馈ID - 主键自增',
    sys_feedback_type varchar(255)  NULL DEFAULT NULL COMMENT '反馈类型 - 如BUG、SUGGESTION、OTHER',
    sys_feedback_images text  NULL COMMENT '反馈图片 - 多个图片地址逗号分隔',
    sys_feedback_content text  NULL COMMENT '反馈内容 - 用户提交的问题描述或建议',
    sys_feedback_status int NULL DEFAULT 0 COMMENT '反馈状态 - 0:未处理, 1:已处理',
    sys_feedback_deal_by int NULL DEFAULT NULL COMMENT '处理人ID - 关联系统用户',
    sys_feedback_deal_name varchar(255)  NULL DEFAULT NULL COMMENT '处理人名称',
    sys_feedback_recover_content text  NULL COMMENT '回复内容 - 管理员处理回复',
    sys_feedback_recover_time datetime NULL DEFAULT NULL COMMENT '回复时间 - 处理反馈的时间',
sys_feedback_recover_images text  NULL COMMENT '回复图片 - 管理员回复附图',
    sys_feedback_user_agent varchar(500)  NULL DEFAULT NULL COMMENT '客户端类型(UserAgent)',
    sys_feedback_platform varchar(50)  NULL DEFAULT NULL COMMENT '平台类型(预留)',
    create_name varchar(255)  NULL DEFAULT NULL COMMENT '创建人姓名',
    create_by int NULL DEFAULT NULL COMMENT '创建人ID',
    create_time datetime NULL DEFAULT NULL COMMENT '创建时间',
    update_time datetime NULL DEFAULT NULL COMMENT '更新时间',
    update_name varchar(255)  NULL DEFAULT NULL COMMENT '更新人姓名',
    update_by int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (sys_feedback_id) USING BTREE,
    INDEX idx_feedback_type (sys_feedback_type) USING BTREE COMMENT '反馈类型索引',
    INDEX idx_feedback_status (sys_feedback_status) USING BTREE COMMENT '反馈状态索引',
    INDEX idx_feedback_create_time (create_time) USING BTREE COMMENT '创建时间索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '系统反馈表 - 存储帮助与反馈和异常上报信息' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 8. 用户表 (sys_user)
-- 用途: 存储系统用户账号信息
-- =====================================================
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    sys_user_id int NOT NULL AUTO_INCREMENT COMMENT '用户ID - 主键自增',
    sys_user_username varchar(255)  NULL DEFAULT NULL COMMENT '用户账号 - 登录使用的唯一账号名',
    sys_user_nickname varchar(255)  NULL DEFAULT NULL COMMENT '用户昵称 - 显示名称',
    sys_user_password varchar(255)  NULL DEFAULT NULL COMMENT '用户密码 - 加密后的密码',
    sys_user_salt varchar(255)  NULL DEFAULT NULL COMMENT '密码盐值 - 用于密码加密的随机字符串',
    sys_user_delete int NULL DEFAULT NULL COMMENT '是否删除 - 0:未删除, 1:已删除(逻辑删除)',
    sys_user_status int NULL DEFAULT NULL COMMENT '用户状态 - 0:禁用, 1:启用',
    sys_user_phone varchar(255)  NULL DEFAULT NULL COMMENT '手机号码 - 用户的手机号',
    sys_user_email varchar(255)  NULL DEFAULT NULL COMMENT '电子邮箱 - 用户的邮箱地址',
    sys_user_sex int NULL DEFAULT NULL COMMENT '用户性别 - 0:女, 1:男, 2:其他',
    sys_user_full_address varchar(400)  NULL DEFAULT NULL COMMENT '居住地址 - 用户的详细地址',
    sys_user_in_system int NULL DEFAULT NULL COMMENT '是否系统账号 - 0:普通账号, 1:系统内置账号(不可删除)',
    sys_user_avatar text  NULL COMMENT '用户头像 - 头像图片的URL或Base64',
    sys_user_card varchar(255)  NULL DEFAULT NULL COMMENT '身份证号 - 用户的身份证号码',
    sys_dept_id int NULL DEFAULT NULL COMMENT '所属部门ID - 关联组织机构表',
    sys_user_remark varchar(255)  NULL DEFAULT NULL COMMENT '用户备注 - 附加说明信息',
    sys_user_last_address varchar(255)  NULL DEFAULT NULL COMMENT '最后登录地点 - 最近一次登录的地理位置',
    sys_user_register_address varchar(255)  NULL DEFAULT NULL COMMENT '注册地点 - 用户注册时的地理位置',
    sys_user_last_ip varchar(255)  NULL DEFAULT NULL COMMENT '最后登录IP - 最近一次登录的IP地址',
    sys_user_register_ip varchar(255)  NULL DEFAULT NULL COMMENT '注册IP - 用户注册时的IP地址',
    sys_user_login_count int NULL DEFAULT NULL COMMENT '登录次数 - 累计登录次数',
    sys_client_id varchar(255)  NULL DEFAULT NULL COMMENT '客户端ID - 绑定的客户端标识',
    sys_user_totp_code varchar(600)  NULL DEFAULT NULL COMMENT 'TOTP密钥 - 两步验证的加密密钥(Base32编码)',
    sys_user_birthday varchar(20)  NULL DEFAULT NULL COMMENT '生日 - 用户出生日期，用于生日头像框等个性化功能',
    sys_user_avatar_frame int NULL DEFAULT NULL COMMENT '头像框ID - 用户选择的头像框样式ID，0为默认',
    sys_user_avatar_frame_show int NULL DEFAULT 0 COMMENT '是否显示头像框 - 0:不显示 1:显示',
    create_name varchar(255)  NULL DEFAULT NULL COMMENT '创建人姓名',
    create_by int NULL DEFAULT NULL COMMENT '创建人ID',
    create_time datetime NULL DEFAULT NULL COMMENT '创建时间',
    update_time datetime NULL DEFAULT NULL COMMENT '更新时间',
    update_name varchar(255)  NULL DEFAULT NULL COMMENT '更新人姓名',
    update_by int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (sys_user_id) USING BTREE,
    UNIQUE INDEX uk_username (sys_user_username) USING BTREE COMMENT '用户名唯一索引',
    INDEX idx_dept_id (sys_dept_id) USING BTREE COMMENT '部门ID索引',
    INDEX idx_phone (sys_user_phone) USING BTREE COMMENT '手机号索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '用户表 - 存储系统用户账号基本信息' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 9. 用户-角色关系表 (sys_user_role)
-- 用途: 存储用户与角色的多对多关系
-- =====================================================
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    sys_user_role_id int NOT NULL AUTO_INCREMENT COMMENT '关系ID - 主键自增',
    sys_user_id int NULL DEFAULT NULL COMMENT '用户ID - 关联用户表',
    sys_role_id int NULL DEFAULT NULL COMMENT '角色ID - 关联角色表',
    PRIMARY KEY (sys_user_role_id) USING BTREE,
    INDEX idx_user_id (sys_user_id) USING BTREE COMMENT '用户ID索引',
    INDEX idx_role_id (sys_role_id) USING BTREE COMMENT '角色ID索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '用户-角色关系表 - 存储用户与角色的多对多映射关系' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 10. 用户配置表 (sys_user_config)
-- 用途: 存储用户的第三方账号绑定信息
-- =====================================================
DROP TABLE IF EXISTS sys_user_config;
CREATE TABLE sys_user_config (
    sys_user_config_id int NOT NULL COMMENT '用户配置ID - 与用户ID一致(一对一)',
    sys_user_config_wx_openid varchar(255)  NULL DEFAULT NULL COMMENT '微信OpenID - 微信登录的OpenID',
    sys_user_config_wx_unionid varchar(255)  NULL DEFAULT NULL COMMENT '微信UnionID - 微信开放平台的UnionID',
    sys_user_config_gitee_id varchar(255)  NULL DEFAULT NULL COMMENT 'Gitee用户ID - Gitee登录绑定的用户ID',
    sys_user_config_github_id varchar(255)  NULL DEFAULT NULL COMMENT 'GitHub用户ID - GitHub登录绑定的用户ID',
    sys_user_config_microsoft_id varchar(255)  NULL DEFAULT NULL COMMENT '微软账号ID - Microsoft登录绑定的用户ID',
    sys_user_config_google_id varchar(255)  NULL DEFAULT NULL COMMENT 'Google账号ID - Google登录绑定的用户ID',
    sys_user_config_qq_openid varchar(255)  NULL DEFAULT NULL COMMENT 'QQ OpenID - QQ登录的OpenID',
    sys_user_config_dingtalk_id varchar(255)  NULL DEFAULT NULL COMMENT '钉钉用户ID - 钉钉登录绑定的用户ID',
    sys_user_config_feishu_id varchar(255)  NULL DEFAULT NULL COMMENT '飞书用户ID - 飞书登录绑定的用户ID',
    create_name varchar(255)  NULL DEFAULT NULL COMMENT '创建人姓名',
    create_by int NULL DEFAULT NULL COMMENT '创建人ID',
    create_time datetime NULL DEFAULT NULL COMMENT '创建时间',
    update_time datetime NULL DEFAULT NULL COMMENT '更新时间',
    update_name varchar(255)  NULL DEFAULT NULL COMMENT '更新人姓名',
    update_by int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (sys_user_config_id) USING BTREE,
    INDEX idx_wx_openid (sys_user_config_wx_openid) USING BTREE COMMENT '微信OpenID索引',
    INDEX idx_gitee_id (sys_user_config_gitee_id) USING BTREE COMMENT 'Gitee ID索引',
    INDEX idx_microsoft_id (sys_user_config_microsoft_id) USING BTREE COMMENT '微软账号索引'
) ENGINE = InnoDB CHARACTER SET = utf8mb4  COMMENT = '用户配置表 - 存储用户第三方账号绑定信息' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 8. 用户布局配置表 (sys_user_layout)
-- 用途: 存储用户个性化的界面布局配置
-- =====================================================
DROP TABLE IF EXISTS sys_user_layout;
CREATE TABLE sys_user_layout (
    sys_layout_id int NOT NULL AUTO_INCREMENT COMMENT '布局ID - 主键自增',
    sys_layout_layout varchar(255)  NULL DEFAULT NULL COMMENT '布局配置 - JSON格式的网格布局信息',
    sys_layout_grid varchar(255)  NULL DEFAULT NULL COMMENT '网格配置 - JSON格式的网格参数',
    sys_layout_component varchar(255)  NULL DEFAULT NULL COMMENT '组件配置 - JSON格式的组件列表',
    sys_layout_user_id int NULL DEFAULT NULL COMMENT '所属用户ID - 关联用户表',
    sys_layout_preference text  NULL COMMENT '用户偏好配置 - lay-setting 配置（JSON格式）',
    create_name varchar(255)  NULL DEFAULT NULL COMMENT '创建人姓名',
    create_by int NULL DEFAULT NULL COMMENT '创建人ID',
    create_time datetime NULL DEFAULT NULL COMMENT '创建时间',
    update_time datetime NULL DEFAULT NULL COMMENT '更新时间',
    update_name varchar(255)  NULL DEFAULT NULL COMMENT '更新人姓名',
    update_by int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (sys_layout_id) USING BTREE,
    UNIQUE INDEX uk_user_id (sys_layout_user_id) USING BTREE COMMENT '用户ID唯一索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '用户布局配置表 - 存储用户个性化的界面布局' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 9. 用户登录日志表 (sys_user_log)
-- 用途: 记录用户登录登出操作日志
-- =====================================================
DROP TABLE IF EXISTS sys_user_log;
CREATE TABLE sys_user_log (
    sys_log_id int NOT NULL AUTO_INCREMENT COMMENT '日志ID - 主键自增',
    sys_log_username varchar(255)  NULL DEFAULT NULL COMMENT '登录账号 - 用户登录使用的账号',
    sys_log_address text  NULL COMMENT '登录地点 - 根据IP解析的地理位置',
    sys_log_ip varchar(255)  NULL DEFAULT NULL COMMENT '登录IP - 用户登录时的IP地址',
    sys_log_isp varchar(255)  NULL DEFAULT NULL COMMENT '运营商 - 网络运营商信息',
    sys_log_system varchar(255)  NULL DEFAULT NULL COMMENT '操作系统 - 用户使用的操作系统',
    sys_log_ua text  NULL COMMENT 'User-Agent - 浏览器完整的UA字符串',
    sys_log_browser varchar(255)  NULL DEFAULT NULL COMMENT '浏览器 - 浏览器类型和版本',
    sys_log_url text  NULL COMMENT '请求地址 - 登录的API地址',
    sys_log_app_name varchar(255)  NULL DEFAULT NULL COMMENT '应用名称 - 请求来源应用标识',
    sys_log_param text  NULL COMMENT '请求参数 - 登录请求的参数(脱敏)',
    sys_log_cost int NULL DEFAULT NULL COMMENT '耗时(毫秒) - 登录操作耗时',
    sys_log_from varchar(255)  NULL DEFAULT NULL COMMENT '操作类型 - LOGIN:登录, LOGOUT:登出',
    sys_log_status int NULL DEFAULT NULL COMMENT '操作状态 - 0:失败, 1:成功',
    sys_log_message text  NULL COMMENT '响应消息 - 登录/登出结果描述',
    sys_log_fingerprint varchar(255)  NULL DEFAULT NULL COMMENT '浏览器指纹 - 用于设备识别的指纹',
    sys_log_login_type varchar(255)  NULL DEFAULT NULL COMMENT '登录方式 - WEB:网页, APP:应用, GITEE:Gitee等',
    sys_dept_id int NULL DEFAULT NULL COMMENT '所属机构ID - 用户所属的部门ID',
    create_name varchar(255)  NULL DEFAULT NULL COMMENT '创建人姓名',
    create_by int NULL DEFAULT NULL COMMENT '创建人ID(用户ID)',
    create_time datetime NULL DEFAULT NULL COMMENT '创建时间',
    update_time datetime NULL DEFAULT NULL COMMENT '更新时间',
    update_name varchar(255)  NULL DEFAULT NULL COMMENT '更新人姓名',
    update_by int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (sys_log_id) USING BTREE,
    INDEX idx_username (sys_log_username) USING BTREE COMMENT '用户名索引',
    INDEX idx_from (sys_log_from) USING BTREE COMMENT '操作类型索引',
    INDEX idx_create_time (create_time) USING BTREE COMMENT '创建时间索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '用户登录日志表 - 记录用户登录登出操作' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 10. 短信日志表 (sys_sms_log)
-- 用途: 记录短信发送历史
-- =====================================================
DROP TABLE IF EXISTS sys_sms_log;
CREATE TABLE sys_sms_log (
    sys_sms_log_id int NOT NULL AUTO_INCREMENT COMMENT '日志ID - 主键自增',
    sys_sms_log_target varchar(255)  NULL DEFAULT NULL COMMENT '接收人 - 手机号码',
    sys_sms_log_template_id varchar(255)  NULL DEFAULT NULL COMMENT '模板ID - 使用的模板编码',
    sys_sms_log_param varchar(255)  NULL DEFAULT NULL COMMENT '模板参数 - JSON格式的模板变量',
    sys_sms_log_content longtext  NULL COMMENT '发送内容 - 最终发送的短信内容',
    sys_sms_cost decimal(10, 2) NULL DEFAULT NULL COMMENT '耗时 - 发送耗时(毫秒)',
    sys_sms_code varchar(255)  NULL DEFAULT NULL COMMENT '返回码 - 短信平台返回的状态码',
    sys_sms_message text  NULL COMMENT '返回消息 - 短信平台返回的消息',
    sys_sms_app_type varchar(255)  NULL DEFAULT NULL COMMENT '短信厂家 - 使用的短信服务商',
    sys_sms_app_id varchar(255)  NULL DEFAULT NULL COMMENT '短信AppID',
    sys_sms_app_sign varchar(255)  NULL DEFAULT NULL COMMENT '短信签名',
    sys_project_id int NULL DEFAULT NULL COMMENT '项目ID - 关联sys_project表',
    create_name varchar(255)  NULL DEFAULT NULL COMMENT '创建人姓名',
    create_by int NULL DEFAULT NULL COMMENT '创建人ID',
    create_time datetime NULL DEFAULT NULL COMMENT '创建时间',
    update_time datetime NULL DEFAULT NULL COMMENT '更新时间',
    update_name varchar(255)  NULL DEFAULT NULL COMMENT '更新人姓名',
    update_by int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (sys_sms_log_id) USING BTREE,
    INDEX idx_target (sys_sms_log_target) USING BTREE COMMENT '接收人索引',
    INDEX idx_project_id (sys_project_id) USING BTREE COMMENT '项目ID索引',
    INDEX idx_create_time (create_time) USING BTREE COMMENT '创建时间索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '短信日志表 - 记录短信发送历史' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 11. 系统操作日志表 (sys_log)
-- 用途: 记录系统通用操作日志
-- =====================================================
DROP TABLE IF EXISTS sys_log;
CREATE TABLE sys_log (
    sys_log_id int NOT NULL AUTO_INCREMENT COMMENT '日志ID - 主键自增',
    sys_log_username varchar(255)  NULL DEFAULT NULL COMMENT '操作账号 - 执行操作的用户账号',
    sys_log_address varchar(255)  NULL DEFAULT NULL COMMENT '请求IP - 发起请求的IP地址',
    sys_log_url varchar(255)  NULL DEFAULT NULL COMMENT '请求地址 - API接口路径',
    sys_log_param varchar(255)  NULL DEFAULT NULL COMMENT '请求参数 - 接口请求参数',
    sys_log_from varchar(255)  NULL DEFAULT NULL COMMENT '请求模块 - 功能模块名称',
    sys_log_status int NULL DEFAULT NULL COMMENT '请求状态 - 0:失败, 1:成功',
    sys_log_cost int NULL DEFAULT NULL COMMENT '耗时(毫秒) - 接口响应时间',
    create_name varchar(255)  NULL DEFAULT NULL COMMENT '创建人姓名',
    create_by int NULL DEFAULT NULL COMMENT '创建人ID',
    create_time datetime NULL DEFAULT NULL COMMENT '创建时间',
    update_time datetime NULL DEFAULT NULL COMMENT '更新时间',
    update_name varchar(255)  NULL DEFAULT NULL COMMENT '更新人姓名',
    update_by int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (sys_log_id) USING BTREE,
    INDEX idx_username (sys_log_username) USING BTREE COMMENT '用户名索引',
    INDEX idx_create_time (create_time) USING BTREE COMMENT '创建时间索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '系统操作日志表 - 记录系统通用操作日志' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 12. 组件库表 (sys_sfc)
-- 用途: 存储可配置的前端组件信息
-- =====================================================
DROP TABLE IF EXISTS sys_sfc;
CREATE TABLE sys_sfc (
    sys_sfc_id int NOT NULL AUTO_INCREMENT COMMENT '组件ID - 主键自增',
    sys_sfc_name varchar(255)  NULL DEFAULT NULL COMMENT '组件名称(英文) - 组件的唯一标识名',
    sys_sfc_chinese_name varchar(255)  NULL DEFAULT NULL COMMENT '组件名称(中文) - 组件的显示名称',
    sys_sfc_function varchar(255)  NULL DEFAULT NULL COMMENT '组件功能 - 多个功能用逗号分隔',
    sys_sfc_icon varchar(255)  NULL DEFAULT NULL COMMENT '组件图标 - 图标名称',
    sys_sfc_category varchar(255)  NULL DEFAULT NULL COMMENT '组件分类 - 如HOME表示首页组件',
    sys_sfc_path varchar(255)  NULL DEFAULT NULL COMMENT '组件路径 - 组件文件存放路径或URL',
    sys_sfc_status int NULL DEFAULT NULL COMMENT '组件状态 - 0:未启用, 1:已启用',
    sys_sfc_desc varchar(255)  NULL DEFAULT NULL COMMENT '组件描述 - 组件功能说明',
    sys_sfc_proxy varchar(255)  NULL DEFAULT NULL COMMENT '代理地址 - 组件数据接口的代理地址',
    sys_sfc_type int NULL DEFAULT NULL COMMENT '组件类型 - 0:文件, 1:文本, 2:外链, 3:本地',
    sys_sfc_content longtext  NULL COMMENT '组件内容 - 当类型为文本时存储Vue组件代码',
    sys_sfc_model_cache longtext  NULL COMMENT '模块缓存 - JSON格式的依赖模块配置',
    sys_sfc_delay int NULL DEFAULT NULL COMMENT '延迟时间(毫秒) - 组件加载延迟',
    sys_sfc_version varchar(255)  NULL DEFAULT NULL COMMENT '组件版本 - 版本号如"1.0.0"',
    sys_sfc_timeout int NULL DEFAULT NULL COMMENT '超时时间(毫秒) - 组件加载超时设置',
    create_name varchar(255)  NULL DEFAULT NULL COMMENT '创建人姓名',
    create_by int NULL DEFAULT NULL COMMENT '创建人ID',
    create_time datetime NULL DEFAULT NULL COMMENT '创建时间',
    update_time datetime NULL DEFAULT NULL COMMENT '更新时间',
    update_name varchar(255)  NULL DEFAULT NULL COMMENT '更新人姓名',
    update_by int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (sys_sfc_id) USING BTREE,
    INDEX idx_category (sys_sfc_category) USING BTREE COMMENT '分类索引',
    INDEX idx_status (sys_sfc_status) USING BTREE COMMENT '状态索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '组件库表 - 存储可配置的前端组件信息' ROW_FORMAT = Dynamic;

-- =====================================================
-- 13. 用户安装组件表 (sys_sfc_install)
-- 用途: 存储用户已安装的组件列表
-- =====================================================
DROP TABLE IF EXISTS sys_sfc_install;
CREATE TABLE sys_sfc_install (
    sys_sfc_install_id int NOT NULL AUTO_INCREMENT COMMENT '安装记录ID - 主键自增',
    user_id int NULL DEFAULT NULL COMMENT '用户ID - 安装组件的用户',
    sys_sfc_id int NULL DEFAULT NULL COMMENT '组件ID - 被安装的组件',
    create_name varchar(255)  NULL DEFAULT NULL COMMENT '创建人姓名',
    create_by int NULL DEFAULT NULL COMMENT '创建人ID',
    create_time datetime NULL DEFAULT NULL COMMENT '创建时间(安装时间)',
    update_time datetime NULL DEFAULT NULL COMMENT '更新时间',
    update_name varchar(255)  NULL DEFAULT NULL COMMENT '更新人姓名',
    update_by int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (sys_sfc_install_id) USING BTREE,
    INDEX idx_user_id (user_id) USING BTREE COMMENT '用户ID索引',
    INDEX idx_sfc_id (sys_sfc_id) USING BTREE COMMENT '组件ID索引',
    UNIQUE INDEX uk_user_sfc (user_id, sys_sfc_id) USING BTREE COMMENT '用户-组件唯一索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '用户安装组件表 - 存储用户已安装的组件列表' ROW_FORMAT = Dynamic;

-- =====================================================
-- 14. 用户第三方绑定表 (sys_user_third_party)
-- 用途: 存储用户与第三方平台的绑定关系
-- =====================================================
DROP TABLE IF EXISTS sys_user_third_party;
CREATE TABLE sys_user_third_party  (
    sys_user_third_party_id int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    sys_user_third_party_type varchar(255)  NULL DEFAULT NULL COMMENT '三方类型，如: gitee、wx_mini_app 等',
    sys_user_third_party_name varchar(255)  NULL DEFAULT NULL COMMENT '第三方信息名称',
    sys_user_third_party_value varchar(255)  NULL DEFAULT NULL COMMENT '第三方信息值',
    sys_user_id int NULL DEFAULT NULL COMMENT '当前系统用户ID',
    sys_user_third_party_uid varchar(255)  NULL DEFAULT NULL COMMENT '第三方用户ID',
    create_name varchar(255)  NULL DEFAULT NULL COMMENT '创建人姓名',
    create_by int NULL DEFAULT NULL COMMENT '创建人ID',
    create_time datetime NULL DEFAULT NULL COMMENT '创建时间',
    update_time datetime NULL DEFAULT NULL COMMENT '更新时间',
    update_name varchar(255)  NULL DEFAULT NULL COMMENT '更新人姓名',
    update_by int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (sys_user_third_party_id) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '用户三方绑定表 - 存储用户与第三方平台的绑定关系' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 15. 微信用户表 (sys_wechat_user)
-- 用途: 存储通过微信体系接入的用户基础信息
-- =====================================================
DROP TABLE IF EXISTS sys_wechat_user;
CREATE TABLE sys_wechat_user (
    sys_wechat_user_id int NOT NULL AUTO_INCREMENT COMMENT '主键ID - 主键自增',
    sys_wechat_open_id varchar(255)  NULL DEFAULT NULL COMMENT '微信 openid - 微信登录的OpenID',
    sys_wechat_union_id varchar(255)  NULL DEFAULT NULL COMMENT '微信 unionid - 微信开放平台的UnionID',
    sys_wechat_nickname varchar(255)  NULL DEFAULT NULL COMMENT '昵称 - 微信用户昵称',
    sys_wechat_avatar varchar(4000)  NULL DEFAULT NULL COMMENT '头像 - 微信用户头像URL',
    sys_user_id int NULL DEFAULT NULL COMMENT '绑定的系统用户ID - 关联用户表',
    create_name varchar(255)  NULL DEFAULT NULL COMMENT '创建人姓名',
    create_by int NULL DEFAULT NULL COMMENT '创建人ID',
    create_time datetime NULL DEFAULT NULL COMMENT '创建时间',
    update_time datetime NULL DEFAULT NULL COMMENT '更新时间',
    update_name varchar(255)  NULL DEFAULT NULL COMMENT '更新人姓名',
    update_by int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (sys_wechat_user_id) USING BTREE,
    INDEX idx_user_id (sys_user_id) USING BTREE COMMENT '系统用户ID索引',
    INDEX idx_open_id (sys_wechat_open_id) USING BTREE COMMENT '微信OpenID索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '微信用户表 - 存储通过微信体系接入的用户基础信息' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 初始数据 - 默认角色
-- =====================================================
INSERT INTO sys_role (sys_role_id, sys_role_name, sys_role_code, sys_role_in_system, sys_role_status, sys_role_sort, sys_role_remark, sys_role_hidden) VALUES 
(1, '超级管理员', 'SUPER_ADMIN', 1, 1, 1, '系统超级管理员，拥有所有权限', 0),
(2, '管理员', 'ADMIN', 1, 1, 2, '系统管理员', 0),
(3, '运维', 'OPS', 1, 1, 3, '运维人员', 0),
(4, '普通用户', 'USER', 0, 1, 4, '普通用户', 0),
(5, '部门负责人', 'DEPT_LEADER', 1, 1, 5, '负责部门及子部门账号管理', 0),
(6, '游客', 'GUEST', 1, 1, 6, '游客账号', 0);

-- =====================================================
-- 初始数据 - 系统基础菜单与权限
-- 仅保留当前 system 应用实际依赖的 system/common-service 基线能力
-- =====================================================
INSERT INTO sys_menu (
    sys_menu_id, sys_menu_name, sys_menu_path, sys_menu_i18n, sys_menu_title, sys_menu_perm,
    sys_menu_pid, sys_menu_tree_id, sys_menu_component, sys_menu_icon, sys_menu_type, sys_menu_sort,
    sys_menu_hidden, sys_menu_show_parent, sys_menu_hidden_tag, sys_menu_fixed_tag, sys_menu_redirect,
    sys_menu_keep_alive, create_name, create_by, create_time, update_time, update_name, update_by,
    sys_menu_role, sys_menu_home, sys_menu_engine
) VALUES
(1, 'system', '/manage', NULL, '系统管理', NULL, 0, '0,1', '/manage', 'ep:setting', 0, 2, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, '', NULL, NULL),
(2, 'menu', '/manage/menu', NULL, '菜单管理', NULL, 1, '0,1,2', '/manage/menu/index', 'ep:menu', 0, 1, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(3, 'setting', '/manage/setting', '', '系统配置', '', 1, '0,1,3', '@pages/common/setting/index.vue', 'ep:setting', 0, 999, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(4, 'role', '/manage/role', NULL, '角色管理', NULL, 1, '0,1,4', '/manage/role/index', 'ri:shield-user-line', 0, 2, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, '', NULL, NULL),
(5, '', '', '', '新增', 'sys:role:add', 4, '0,1,4', '', '', 3, 1, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NULL, NULL, NULL, '', NULL, NULL),
(6, '', '', '', '删除', 'sys:role:delete', 4, '0,1,4', '', '', 3, 1, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NULL, NULL, NULL, '', NULL, NULL),
(7, '', '', '', '修改', 'sys:role:update', 4, '0,1,4', '', '', 3, 1, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NULL, NULL, NULL, '', NULL, NULL),
(8, 'login', '/manage/login', '', '用户管理', '', 1, '0,1,8', '/manage/login/index', 'ri:user-settings-line', 0, 3, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, '', NULL, NULL),
(9, 'userLog', '/manage/log/login', '', '登录日志', '', 1, '0,1,9', '/manage/log/login/index', 'ep:alarm-clock', 0, 998, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, '', NULL, NULL),
(11, 'dept', '/manage/dept', '', '机构管理', '', 1, '0,1,11', '/manage/dept/index', 'ep:star', 0, 5, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, '', NULL, NULL),
(12, '', '', '', '删除', 'sys:login:delete', 8, '0,1,8', '', '', 3, 1, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NULL, NULL, NULL, '', NULL, NULL),
(13, '', '', '', '编辑', 'sys:login:update', 8, '0,1,8', '', '', 3, 1, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NULL, NULL, NULL, '', NULL, NULL),
(14, '', '', '', '新增', 'sys:login:add', 8, '0,1,8,14', '', '', 3, 1, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, '', NULL, NULL),
(15, 'dict', '/manage/dict', '', '字典管理', '', 1, '0,1,15', '/manage/dict/index', 'humbleicons:dashboard', 0, 6, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, '', NULL, NULL),
(18, 'sysLog', '/manage/syslog', '', '系统日志', '', 1, '0,1,18', 'system/syslog/index', 'ep:list', 0, 999, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, '', NULL, NULL),
(19, '', '', '', '删除', 'sys:log:delete', 18, '0,1,18,19', '', '', 3, 1, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NULL, NULL, NULL, '', NULL, NULL);

INSERT INTO sys_role_menu (sys_role_menu_id, sys_role_id, sys_menu_id) VALUES
(82, 2, 4),
(83, 2, 5),
(84, 2, 6),
(85, 2, 7),
(86, 2, 8),
(87, 2, 12),
(88, 2, 13),
(89, 2, 14),
(90, 2, 1),
(91, 2, 2),
(92, 2, 11),
(93, 2, 15),
(96, 2, 9),
(97, 2, 3),
(131, 3, 2),
(132, 3, 4),
(133, 3, 5),
(134, 3, 6),
(135, 3, 7),
(136, 3, 8),
(137, 3, 12),
(138, 3, 13),
(139, 3, 14),
(140, 3, 9),
(141, 3, 11),
(142, 3, 15),
(143, 3, 1),
(161, 2, 18),
(162, 2, 19);

-- =====================================================
-- 初始数据 - 默认系统账号
-- 账号: sa / admin / ops
-- 密码: 123456 (前端先 MD5，再由后端执行 MD5(md5(password)+salt))
-- =====================================================
INSERT INTO sys_user (sys_user_id, sys_user_username, sys_user_nickname, sys_user_password, sys_user_salt, sys_user_delete, sys_user_status, sys_user_in_system) VALUES
(1, 'sa', '超级管理员', '5f1d7a84db00d2fce00b31a7fc73224f', '123456', 0, 1, 1),
(2, 'admin', '管理员', '5f1d7a84db00d2fce00b31a7fc73224f', '123456', 0, 1, 1),
(3, 'ops', '运维', '5f1d7a84db00d2fce00b31a7fc73224f', '123456', 0, 1, 1);

-- =====================================================
-- 初始数据 - 默认账号角色关联
-- =====================================================
INSERT INTO sys_user_role (sys_user_id, sys_role_id) VALUES
(1, 1),
(2, 2),
(3, 3);

-- =====================================================
-- 初始数据 - 默认组织机构
-- =====================================================
INSERT INTO sys_dept (sys_dept_id, sys_dept_name, sys_dept_pid, sys_dept_tree_id, sys_dept_code, sys_dept_sort, sys_dept_status) VALUES 
(1, '总部', 0, '0,1', 'ROOT', 1, 0);


-- ============================================
-- 11. 系统公告表 (sys_announcement)
-- ============================================
DROP TABLE IF EXISTS sys_announcement;
CREATE TABLE sys_announcement (
    sys_announcement_id int NOT NULL AUTO_INCREMENT COMMENT '公告ID - 主键自增',
    sys_announcement_title varchar(200)  NOT NULL COMMENT '公告标题',
    sys_announcement_content text  NULL COMMENT '公告内容(HTML) - 支持富文本',
    sys_announcement_level varchar(20)  NULL DEFAULT 'info' COMMENT '级别 - info:信息 warning:警告 important:重要 emergency:紧急',
    sys_announcement_position varchar(20)  NULL DEFAULT 'default' COMMENT '展示位置 - default:Tabs下方 fixed-top:固定顶部',
    sys_announcement_type varchar(20)  NULL DEFAULT 'global' COMMENT '类型 - global:全局 role:指定角色',
    sys_announcement_target_roles varchar(1000)  NULL DEFAULT NULL COMMENT '目标角色ID列表(JSON数组) - type=role时生效',
    sys_announcement_status int NULL DEFAULT 0 COMMENT '状态 - 0:草稿 1:已发布 2:已下架',
    sys_announcement_sort int NULL DEFAULT 0 COMMENT '排序权重 - 越大越靠前',
    sys_announcement_publish_time datetime NULL DEFAULT NULL COMMENT '发布时间',
    sys_announcement_expire_time datetime NULL DEFAULT NULL COMMENT '过期时间 - 空=永不过期',
    sys_announcement_target_url varchar(500)  NULL DEFAULT NULL COMMENT '跳转链接 - 点击公告跳转',
    sys_announcement_publisher_name varchar(100)  NULL DEFAULT NULL COMMENT '发布人',
    create_name varchar(255)  NULL DEFAULT NULL COMMENT '创建人姓名',
    create_by int NULL DEFAULT NULL COMMENT '创建人ID',
    create_time datetime NULL DEFAULT NULL COMMENT '创建时间',
    update_time datetime NULL DEFAULT NULL COMMENT '更新时间',
    update_name varchar(255)  NULL DEFAULT NULL COMMENT '更新人姓名',
    update_by int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (sys_announcement_id) USING BTREE,
    INDEX idx_announcement_status (sys_announcement_status) USING BTREE COMMENT '公告状态索引',
    INDEX idx_announcement_publish_time (sys_announcement_publish_time) USING BTREE COMMENT '发布时间索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '系统公告表 - 存储系统公告和通知' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 12. 头像框定义表 (sys_avatar_frame)
-- 用途: 存储头像框定义（渐变框 id=1-22、图片框 id=23-28、生日框 id=100）
-- =====================================================
DROP TABLE IF EXISTS sys_avatar_frame;
CREATE TABLE sys_avatar_frame (
    sys_avatar_frame_id int NOT NULL AUTO_INCREMENT COMMENT '头像框ID',
    sys_avatar_frame_name varchar(100) NOT NULL COMMENT '头像框名称',
    sys_avatar_frame_type varchar(50) DEFAULT 'base' COMMENT '类型: base-基础 birthday-生日 event-活动',
    sys_avatar_frame_image varchar(500) DEFAULT NULL COMMENT '头像框图片URL',
    sys_avatar_frame_color varchar(200) DEFAULT NULL COMMENT '渐变背景色(css)',
    sys_avatar_frame_desc varchar(500) DEFAULT NULL COMMENT '描述',
    sys_avatar_frame_acquire varchar(50) DEFAULT 'default' COMMENT '获取途径: default-默认 birthday-生日 achievement-成就',
    sys_avatar_frame_sort int DEFAULT 0 COMMENT '排序',
    sys_avatar_frame_status int DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    create_name varchar(255) DEFAULT NULL COMMENT '创建人姓名',
    create_by int DEFAULT NULL COMMENT '创建人',
    create_time datetime DEFAULT NULL COMMENT '创建时间',
    update_time datetime DEFAULT NULL COMMENT '更新时间',
    update_name varchar(255) DEFAULT NULL COMMENT '更新人姓名',
    update_by int DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (sys_avatar_frame_id) USING BTREE,
    KEY idx_type (sys_avatar_frame_type) USING BTREE,
    KEY idx_status (sys_avatar_frame_status) USING BTREE
) ;

-- =====================================================
-- 13. 用户头像框关联表 (sys_user_avatar_frame)
-- 用途: 记录用户已获取的头像框
-- =====================================================
DROP TABLE IF EXISTS sys_user_avatar_frame;
CREATE TABLE sys_user_avatar_frame (
    sys_user_avatar_frame_id int NOT NULL AUTO_INCREMENT COMMENT '用户头像框ID',
    sys_user_id int NOT NULL COMMENT '用户ID',
    sys_avatar_frame_id int NOT NULL COMMENT '头像框ID',
    acquire_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '获取时间',
    expire_time datetime DEFAULT NULL COMMENT '过期时间(NULL=永久)',
    create_name varchar(255) DEFAULT NULL COMMENT '创建人姓名',
    create_by int DEFAULT NULL COMMENT '创建人',
    create_time datetime DEFAULT NULL COMMENT '创建时间',
    update_time datetime DEFAULT NULL COMMENT '更新时间',
    update_name varchar(255) DEFAULT NULL COMMENT '更新人姓名',
    update_by int DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (sys_user_avatar_frame_id) USING BTREE,
    UNIQUE KEY uk_user_frame (sys_user_id, sys_avatar_frame_id) USING BTREE,
    KEY idx_user_id (sys_user_id) USING BTREE,
    KEY idx_frame_id (sys_avatar_frame_id) USING BTREE
) ;

-- =====================================================
-- 初始数据 - 头像框种子（渐变 1-22、图片 23-28、生日 100）
-- =====================================================
INSERT INTO sys_avatar_frame (sys_avatar_frame_id, sys_avatar_frame_name, sys_avatar_frame_type, sys_avatar_frame_color, sys_avatar_frame_desc, sys_avatar_frame_acquire, sys_avatar_frame_sort) VALUES
(1, '经典', 'base', 'transparent', '默认无边框', 'default', 0),
(2, '金色', 'base', 'linear-gradient(135deg, #f6d365, #fda085)', '温暖金色渐变', 'default', 1),
(3, '蓝色', 'base', 'linear-gradient(135deg, #667eea, #764ba2)', '深邃蓝紫渐变', 'default', 2),
(4, '红色', 'base', 'linear-gradient(135deg, #f093fb, #f5576c)', '热情红粉渐变', 'default', 3),
(5, '绿色', 'base', 'linear-gradient(135deg, #4facfe, #00f2fe)', '清新蓝绿渐变', 'default', 4),
(6, '紫色', 'base', 'linear-gradient(135deg, #a18cd1, #fbc2eb)', '梦幻紫粉渐变', 'default', 5),
(7, '橙色', 'base', 'linear-gradient(135deg, #fa709a, #fee140)', '活力橙黄渐变', 'default', 6),
(8, '暗夜', 'base', 'linear-gradient(135deg, #2c3e50, #3498db)', '沉稳暗夜渐变', 'default', 7),
(9, '糖果', 'base', 'linear-gradient(135deg, #f6d365, #fda085)', '甜美糖果渐变', 'default', 8),
(10, '樱粉', 'base', 'linear-gradient(135deg, #ff9a9e, #fecfef)', '浪漫樱粉渐变', 'default', 9),
(11, '薄荷', 'base', 'linear-gradient(135deg, #a1c4fd, #c2e9fb)', '清爽薄荷渐变', 'default', 10),
(12, '暮霭', 'base', 'linear-gradient(135deg, #d4a4fc, #ff9a9e)', '温柔暮霭渐变', 'default', 11),
(13, '柠檬', 'base', 'linear-gradient(135deg, #ffdfba, #feedb0)', '明快柠檬渐变', 'default', 12),
(14, '星空', 'base', 'linear-gradient(135deg, #0f2027, #203a43, #2c5364)', '深邃星空渐变', 'default', 13),
(15, '森林', 'base', 'linear-gradient(135deg, #134e5e, #71b280)', '自然森林渐变', 'default', 14),
(16, '熔岩', 'base', 'linear-gradient(135deg, #ff512f, #dd2476)', '炽热熔岩渐变', 'default', 15),
(17, '极光', 'base', 'linear-gradient(135deg, #00c9ff, #92fe9d)', '缥缈极光渐变', 'default', 16),
(18, '红枫', 'base', 'linear-gradient(135deg, #b31217, #e52d27)', '热烈红枫渐变', 'default', 17),
(19, '海洋', 'base', 'linear-gradient(135deg, #2193b0, #6dd5ed)', '蔚蓝海洋渐变', 'default', 18),
(20, '祖母绿', 'base', 'linear-gradient(135deg, #11998e, #38ef7d)', '剔透祖母绿渐变', 'default', 19),
(21, '晚霞', 'base', 'linear-gradient(135deg, #ff9966, #ff5e62)', '绚烂晚霞渐变', 'default', 20),
(22, '阿凡达', 'base', 'linear-gradient(135deg, #1488cc, #2b32b2)', '科幻阿凡达渐变', 'default', 21);

INSERT INTO sys_avatar_frame (sys_avatar_frame_id, sys_avatar_frame_name, sys_avatar_frame_type, sys_avatar_frame_image, sys_avatar_frame_desc, sys_avatar_frame_acquire, sys_avatar_frame_sort) VALUES
(23, '鎏金', 'base', '/assets/avatar-frames/frame-gold.png', '金色图腾环', 'default', 22),
(24, '皇室蓝', 'base', '/assets/avatar-frames/frame-royal.png', '经典皇室蓝环', 'default', 23),
(25, '玫瑰金', 'base', '/assets/avatar-frames/frame-rose.png', '优雅玫瑰金环', 'default', 24),
(26, '焰光', 'base', '/assets/avatar-frames/frame-flame.apng', '火焰呼吸动画(APNG)', 'default', 25),
(27, '晶蓝', 'base', '/assets/avatar-frames/frame-azure.apng', '晶蓝呼吸动画(APNG)', 'default', 26),
(28, '玄黑', 'base', '/assets/avatar-frames/frame-onyx.png', '幽黑玄金环', 'default', 27),
(29, '像素风', 'base', '/assets/avatar-frames/frame-pixel.png', '复古像素风边框', 'default', 28);

INSERT INTO sys_avatar_frame (sys_avatar_frame_id, sys_avatar_frame_name, sys_avatar_frame_type, sys_avatar_frame_image, sys_avatar_frame_desc, sys_avatar_frame_acquire, sys_avatar_frame_sort, sys_avatar_frame_status) VALUES
(100, '生日快乐', 'birthday', '/assets/avatar-frames/birthday.png', '生日专属限定头像框', 'birthday', 99, 1);


-- =====================================================
-- Service 模块初始化数据脚本
-- 版本: V1.0
-- 说明: 包含系统字典、字典项、系统配置、文件系统分组等基础数据
-- 执行方式: 异步执行（虚拟线程）
-- =====================================================



-- =====================================================
-- 初始数据 - 字典数据
-- =====================================================
INSERT INTO sys_dict VALUES (1, '厂家', 'CHANG_JIA', 0, 1, 0, '', NULL, '三方厂家', 'sa', NULL, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_dict VALUES (2, '模板类型', 'MU_BAN_LEI_XING', 0, 1, 0, '', NULL, '', 'sa', NULL, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_dict VALUES (3, '短信', 'DUAN_XIN', 2, 1, 0, '', NULL, '', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_dict VALUES (4, '微信', 'WEI_XIN', 2, 1, 0, NULL, NULL, NULL, 'sa', 1, NOW(), NULL, NULL, NULL);
INSERT INTO sys_dict VALUES (5, '组件功能', 'ZU_JIAN_GONG_NENG', 0, 1, 0, '', NULL, '', 'sa', 1, NOW(), NULL, NULL, NULL);
INSERT INTO sys_dict VALUES (6, '项目功能', 'XIANG_MU_GONG_NENG', 0, 1, 0, '', NULL, '', 'sa', 1, NOW(), NULL, NULL, NULL);
INSERT INTO sys_dict VALUES (7, '设备类型', 'SHE_BEI_LEI_XING', 0, 1, 0, '', NULL, '', 'sa', 1, NOW(), NULL, NULL, NULL);

-- =====================================================
-- 初始数据 - 字典项数据
-- =====================================================
INSERT INTO sys_dict_item VALUES (1, 1, '海康云曜', 'HAI_KANG_YUN_YAO', NULL, 1, 1, '', 0, NULL, '海康云曜', 'sa', NULL, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_dict_item VALUES (2, 1, '阿里云', 'ALIYUN', NULL, 1, 1, NULL, 0, NULL, '阿里云', 'sa', NULL, NOW(), NULL, NULL, NULL);
INSERT INTO sys_dict_item VALUES (3, 1, '百度云', 'BDYUN', NULL, 1, 1, NULL, 0, NULL, '百度云', 'sa', NULL, NOW(), NULL, NULL, NULL);
INSERT INTO sys_dict_item VALUES (4, 2, '短信', 'SMS', NULL, 1, 1, '', 0, NULL, '', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_dict_item VALUES (5, 3, '短信通知', 'DUAN_XIN_TONG_ZHI', NULL, 1, 2, '', 0, NULL, '', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_dict_item VALUES (6, 2, '站内', 'WEB', NULL, 1, 2, '', 0, NULL, '', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_dict_item VALUES (7, 2, '微信', 'WECHAT', NULL, 1, 2, NULL, 0, NULL, NULL, 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_dict_item VALUES (8, 3, '验证码', 'YAN_ZHENG_MA', NULL, 1, 1, '', 0, NULL, '', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_dict_item VALUES (9, 4, '微信通知', 'WEI_XIN_TONG_ZHI', NULL, 1, NULL, NULL, 0, NULL, NULL, 'sa', 1, NOW(), NULL, NULL, NULL);
INSERT INTO sys_dict_item VALUES (10, 1, '讯飞', 'XUN_FEI', NULL, 1, 1, '', 0, 'sysProjectAppId,sysProjectAppSecret', '', 'sa', 1, NOW(), NOW(), 'sa', 1);

-- =====================================================
-- 初始数据 - 系统配置
-- =====================================================
INSERT INTO sys_setting_group VALUES (1, '默认配置', 'default', 'ep:setting', 1, '系统默认配置', 1, 0, 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_setting_group VALUES (2, '系统配置', 'config', 'ep:tools', 1, '系统基础运行配置', 2, 0, 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_setting_group VALUES (3, '单点登录', 'sso', 'ri:shield-user-line', 1, '第三方登录总开关', 3, 0, 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_setting_group VALUES (4, 'Gitee登录', 'gitee', 'simple-icons:gitee', 1, 'Gitee OAuth配置', 4, 0, 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_setting_group VALUES (5, 'GitHub登录', 'github', 'simple-icons:github', 1, 'GitHub OAuth配置', 5, 0, 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_setting_group VALUES (6, 'Google登录', 'google', 'simple-icons:google', 1, 'Google OAuth配置', 6, 0, 'sa', 1, NOW(), NOW(), 'sa', 1);

INSERT INTO sys_setting VALUES (1, 'CheckCodeOpen', '', 'false', 'Boolean', 'default', 1, 4, 0, 1, 0, NULL, 15, NULL, '验证码开关', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (2, 'SystemName', '', '后台管理系统', 'String', 'default', 1, 0, 0, 1, 0, NULL, 15, NULL, '系统标题', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (3, 'SlidingBlockOpen', '', 'true', 'Boolean', 'default', 1, 5, 0, 1, 0, NULL, 15, NULL, '滑动验证码', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (17, 'OpenSmsLogin', '', 'true', 'Boolean', 'default', 1, 6, 0, 1, 0, NULL, 15, NULL, '短信登录开关', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (30, 'CheckTotpOpen', 'login.otp', 'false', 'Boolean', 'default', 1, 7, 0, 1, 0, NULL, 15, NULL, 'OTP动态口令', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (31, 'BrandName', 'login.brandName', '后台管理系统', 'String', 'default', 1, 1, 0, 1, 0, NULL, 15, NULL, '品牌名称', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (32, 'Welcome', 'login.welcome', '欢迎登录', 'String', 'default', 1, 2, 0, 1, 0, NULL, 15, NULL, '欢迎语', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (33, 'Description', 'login.description', '系统管理平台', 'String', 'default', 1, 3, 0, 1, 0, NULL, 15, NULL, '系统描述', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (34, 'LogoUrl', 'login.logoUrl', '/assets/logo.png', 'String', 'default', 1, 8, 0, 1, 0, NULL, 15, NULL, '品牌Logo', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (35, 'ImageUrl', 'login.imageUrl', '/assets/login/science.png', 'String', 'default', 1, 9, 0, 1, 0, NULL, 15, NULL, '主视觉图片', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (36, 'AvatarUrl', 'login.avatarUrl', '', 'String', 'default', 1, 10, 0, 1, 0, NULL, 15, NULL, '默认头像', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (37, 'BeiAnHao', 'login.beiAnHao', '', 'String', 'default', 1, 11, 0, 1, 0, NULL, 15, NULL, '备案号', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (38, 'FeatureTags', 'login.featureTags', '[{"icon":"DataAnalysis","text":"高效管理"},{"icon":"Brush","text":"现代设计"},{"icon":"Lock","text":"安全可靠"}]', 'String', 'default', 1, 12, 0, 1, 0, NULL, 15, NULL, '功能特性标签', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (5, 'CodecResponseOpen', '', 'false', 'Boolean', 'config', 1, 10, 0, 1, 0, NULL, 26, NULL, '返回加密', 'sa', NULL, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (6, 'WatermarkOpen', '', 'false', 'Boolean', 'config', 1, 5, 0, 1, 0, NULL, 26, NULL, '水印', 'sa', NULL, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (8, 'Version', '', '1', 'Number', 'config', 1, 4, 0, 1, 0, NULL, 123, 'fe7db08a-1001-4e7b-95ab-776a569a85c0', '系统版本', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (9, 'SocketUrl', '', 'http://127.0.0.1:29181', 'Array', 'config', 1, 2, 0, 1, 0, 1, 26, NULL, 'socket.io服务器地址', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (10, 'SocketOpen', '', 'false', 'Boolean', 'config', 1, 12, 0, 1, 0, 1, 26, NULL, 'socket.io是否开启', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (11, 'SocketPath', '', '/socket.io', 'String', 'config', 1, 13, 0, 1, 0, NULL, NULL, NULL, 'socket.io路径', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (12, 'WatermarkColor', '', '#409EFF', 'Color', 'config', 1, 6, 0, 1, 0, NULL, 26, NULL, '水印颜色', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (13, 'WatermarkAlpha', '', '0.35', 'Number', 'config', 1, 8, 0, 1, 0, NULL, 26, NULL, '水印透明度', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (14, 'WatermarkRotate', '', '-30', 'Number', 'config', 1, 7, 0, 1, 0, NULL, 26, NULL, '水印角度', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (15, 'CodecRequestOpen', '', 'false', 'Boolean', 'config', 1, 14, 0, 1, 0, NULL, 26, NULL, '请求加密', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (16, 'CodecRequestKey', '', '0123456789abcdef', 'String', 'config', 1, 3, 0, 1, 1, NULL, 26, NULL, '请求加密密钥', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (18, 'Gitee', '', 'false', 'Boolean', 'sso', 1, 1, 0, 1, 0, NULL, 1, NULL, 'Gitee登录开关', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (19, 'Github', '', 'false', 'Boolean', 'sso', 1, 2, 0, 1, 0, NULL, 1, NULL, 'GitHub登录开关', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (20, 'Google', '', 'false', 'Boolean', 'sso', 1, 3, 0, 1, 0, NULL, 1, NULL, 'Google登录开关', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (21, 'clientId', '', '', 'String', 'gitee', 1, 1, 0, 1, 0, NULL, 1, NULL, 'Gitee Client ID', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (22, 'clientSecret', '', '', 'Password', 'gitee', 1, 2, 0, 1, 0, NULL, 1, NULL, 'Gitee Client Secret', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (23, 'redirectUri', '', 'http://127.0.0.1:18170/system/api/v2/third/login/gitee/callback', 'String', 'gitee', 1, 3, 0, 1, 0, NULL, 1, NULL, 'Gitee回调地址', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (24, 'clientId', '', '', 'String', 'github', 1, 1, 0, 1, 0, NULL, 1, NULL, 'GitHub Client ID', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (25, 'clientSecret', '', '', 'Password', 'github', 1, 2, 0, 1, 0, NULL, 1, NULL, 'GitHub Client Secret', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (26, 'redirectUri', '', 'http://127.0.0.1:18170/system/api/v2/third/login/github/callback', 'String', 'github', 1, 3, 0, 1, 0, NULL, 1, NULL, 'GitHub回调地址', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (27, 'clientId', '', '', 'String', 'google', 1, 1, 0, 1, 0, NULL, 1, NULL, 'Google Client ID', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (28, 'clientSecret', '', '', 'Password', 'google', 1, 2, 0, 1, 0, NULL, 1, NULL, 'Google Client Secret', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (29, 'redirectUri', '', 'http://127.0.0.1:18170/system/api/v2/third/login/google/callback', 'String', 'google', 1, 3, 0, 1, 0, NULL, 1, NULL, 'Google回调地址', 'sa', 1, NOW(), NOW(), 'sa', 1);

-- =====================================================
-- 初始数据 - 文件系统分组
-- =====================================================
INSERT INTO sys_file_system_group VALUES (1, '默认分组', 0, 'default', '/default', 1, 'default', NULL, 1, 1, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO sys_file_system_group VALUES (2, '图片', 0, 'images', '/images', 1, 'images', NULL, 0, 2, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO sys_file_system_group VALUES (3, '视频', 0, 'videos', '/videos', 1, 'videos', NULL, 0, 3, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO sys_file_system_group VALUES (4, '文档', 0, 'documents', '/documents', 1, 'documents', NULL, 0, 4, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO sys_file_system_group VALUES (5, '音频', 0, 'audios', '/audios', 1, 'audios', NULL, 0, 5, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

-- =====================================================
-- 初始数据 - 文件系统配置
-- =====================================================
INSERT INTO sys_file_system_setting VALUES (1, '默认配置', 1, 100, 1, 10, 1, 0, 8888, '0.0.0.0', '/file', 1, 1, NULL, NULL, 1024, NULL, NULL, 0, 1, 'local', NULL, NULL, NULL, NULL, NULL, NULL, NULL);




