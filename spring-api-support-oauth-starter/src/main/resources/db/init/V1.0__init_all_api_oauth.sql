-- =====================================================================
-- V1.0__init_all_api_oauth.sql - spring-api-support-oauth-starter 全量 DDL（合并自 2 个文件）
-- 通道: INIT 同步建表；老库缺列/缺表由 information_schema + PREPARE 段幂等补齐。
-- 种子数据见 V1.0__initdata_all.sql（虚拟线程异步，不阻塞启动）。
-- =====================================================================

-- ---------------------------------------------------------------------
-- 来源: V0.0.0__h2_compat.sql
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_menu` (
    `sys_menu_id` INT NOT NULL AUTO_INCREMENT COMMENT '菜单ID - 主键自增',
    `sys_menu_name` varchar(255)  NULL DEFAULT NULL COMMENT '菜单名称(路由名) - 用于路由匹配的唯一名称',
    `sys_menu_path` varchar(255)  NULL DEFAULT NULL COMMENT '菜单路径 - 前端路由路径，如"/manage/user"',
    `sys_menu_i18n` varchar(255)  NULL DEFAULT NULL COMMENT '国际化key - 用于多语言显示',
    `sys_menu_title` varchar(255)  NULL DEFAULT NULL COMMENT '菜单标题 - 显示在界面上的名称',
    `sys_menu_perm` varchar(255)  NULL DEFAULT NULL COMMENT '权限标识 - 如"sys:user:add"',
    `sys_menu_pid` INT NULL DEFAULT NULL COMMENT '父菜单ID - 上级菜单的ID，顶级为0',
    `sys_menu_tree_id` varchar(255)  NULL DEFAULT NULL COMMENT '菜单路径链 - 从根节点到当前节点的ID路径',
    `sys_menu_component` varchar(255)  NULL DEFAULT NULL COMMENT '菜单组件路径 - Vue组件的路径，如"/manage/user/index"',
    `sys_menu_icon` varchar(255)  NULL DEFAULT NULL COMMENT '菜单图标 - 图标名称，如"ep:user"',
    `sys_menu_extra_icon` varchar(255)  NULL DEFAULT NULL COMMENT '右侧额外图标 - 菜单右侧显示的图标',
    `sys_menu_type` INT NULL DEFAULT NULL COMMENT '菜单类型 - 0:菜单, 1:iframe, 2:外链, 3:按钮',
    `sys_menu_sort` INT NULL DEFAULT NULL COMMENT '排序优先级 - 数值越小越靠前',
    `sys_menu_hidden` INT NULL DEFAULT NULL COMMENT '是否隐藏 - 0:显示, 1:隐藏',
    `sys_menu_show_parent` INT NULL DEFAULT NULL COMMENT '是否显示父级菜单 - 0:显示, 1:隐藏父级',
    `sys_menu_hidden_tag` INT NULL DEFAULT NULL COMMENT '是否在标签栏隐藏 - 0:显示, 1:隐藏',
`sys_menu_fixed_tag` INT NULL DEFAULT NULL COMMENT '是否固定标签 - 0:不固定, 1:固定在标签栏',
    `sys_menu_is_tag` tinyint NULL DEFAULT 0 COMMENT '是否开启菜单徽标 - 0:否 1:是',
    `sys_menu_tag_type` varchar(20)  NULL DEFAULT NULL COMMENT '徽标类型 - danger/success/info/warning/primary',
    `sys_menu_tag_name` varchar(50)  NULL DEFAULT NULL COMMENT '徽标名称 - 留空则显示圆点徽标',
    `sys_menu_redirect` varchar(255)  NULL DEFAULT NULL COMMENT '路由重定向地址 - 访问该菜单时跳转的地址',
    `sys_menu_enter_transition` varchar(255)  NULL DEFAULT NULL COMMENT '进场动画 - 页面进入时的动画效果',
    `sys_menu_leave_transition` varchar(255)  NULL DEFAULT NULL COMMENT '离场动画 - 页面离开时的动画效果',
    `sys_menu_active_path` varchar(255)  NULL DEFAULT NULL COMMENT '菜单激活路径 - 当匹配该路径时激活此菜单',
    `sys_menu_frame_src` varchar(255)  NULL DEFAULT NULL COMMENT '内嵌链接地址 - iframe或外链的URL',
    `sys_menu_keep_alive` INT NULL DEFAULT NULL COMMENT '是否缓存页面 - 0:不缓存, 1:缓存(keep-alive)',
    `sys_menu_role` varchar(255)  NULL DEFAULT NULL COMMENT '所属角色编码 - 可访问该菜单的角色编码，逗号分隔',
    `sys_menu_home` INT NULL DEFAULT NULL COMMENT '是否只显示在主页 - 0:否, 1:是',
    `sys_menu_engine` INT NULL DEFAULT NULL COMMENT '是否engine布局主页 - 0:否, 1:是',
    `create_name` varchar(255)  NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` INT NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255)  NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` INT NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_menu_id` DESC),
    INDEX `sys_menu_idx_menu_pid` (`sys_menu_pid`),
    INDEX `sys_menu_idx_menu_perm` (`sys_menu_perm`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '菜单表 - 存储系统菜单和按钮权限信息';

CREATE TABLE IF NOT EXISTS sys_role_menu (sys_role_menu_id BIGINT AUTO_INCREMENT PRIMARY KEY, sys_role_id BIGINT, sys_menu_id BIGINT);

CREATE TABLE IF NOT EXISTS `sys_user_log` (
    `sys_log_id` INT NOT NULL AUTO_INCREMENT COMMENT '日志ID - 主键自增',
    `sys_log_username` varchar(255)  NULL DEFAULT NULL COMMENT '登录账号 - 用户登录使用的账号',
    `sys_log_address` text  NULL COMMENT '登录地点 - 根据IP解析的地理位置',
    `sys_log_ip` varchar(255)  NULL DEFAULT NULL COMMENT '登录IP - 用户登录时的IP地址',
    `sys_log_isp` varchar(255)  NULL DEFAULT NULL COMMENT '运营商 - 网络运营商信息',
    `sys_log_system` varchar(255)  NULL DEFAULT NULL COMMENT '操作系统 - 用户使用的操作系统',
    `sys_log_ua` text  NULL COMMENT 'User-Agent - 浏览器完整的UA字符串',
    `sys_log_browser` varchar(255)  NULL DEFAULT NULL COMMENT '浏览器 - 浏览器类型和版本',
    `sys_log_url` text  NULL COMMENT '请求地址 - 登录的API地址',
    `sys_log_app_name` varchar(255)  NULL DEFAULT NULL COMMENT '应用名称 - 请求来源应用标识',
    `sys_log_param` text  NULL COMMENT '请求参数 - 登录请求的参数(脱敏)',
    `sys_log_cost` INT NULL DEFAULT NULL COMMENT '耗时(毫秒) - 登录操作耗时',
    `sys_log_from` varchar(255)  NULL DEFAULT NULL COMMENT '操作类型 - LOGIN:登录, LOGOUT:登出',
    `sys_log_status` INT NULL DEFAULT NULL COMMENT '操作状态 - 0:失败, 1:成功',
    `sys_log_message` text  NULL COMMENT '响应消息 - 登录/登出结果描述',
    `sys_log_fingerprint` varchar(255)  NULL DEFAULT NULL COMMENT '浏览器指纹 - 用于设备识别的指纹',
    `sys_log_login_type` varchar(255)  NULL DEFAULT NULL COMMENT '登录方式 - WEB:网页, APP:应用, GITEE:Gitee等',
    `sys_dept_id` INT NULL DEFAULT NULL COMMENT '所属机构ID - 用户所属的部门ID',
    `create_name` varchar(255)  NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` INT NULL DEFAULT NULL COMMENT '创建人ID(用户ID)',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255)  NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` INT NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_log_id`),
    INDEX `sys_user_log_idx_username` (`sys_log_username`),
    INDEX `sys_user_log_idx_from` (`sys_log_from`),
    INDEX `sys_user_log_idx_create_time` (`create_time`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '用户登录日志表 - 记录用户登录登出操作';

CREATE TABLE IF NOT EXISTS `sys_setting` (
    `sys_setting_id` int NOT NULL AUTO_INCREMENT COMMENT '配置ID - 主键自增',
    `sys_setting_name` varchar(255) NULL DEFAULT NULL COMMENT '配置名称 - 配置项的键名',
    `sys_setting_code` varchar(255) NULL DEFAULT NULL COMMENT '配置编码 - 唯一标识码（如 login.title）',
    `sys_setting_value` text NULL COMMENT '配置值 - 配置项的值',
    `sys_setting_value_type` varchar(255) NULL DEFAULT NULL COMMENT '配置类型 - Boolean/String/Number/Color/Dict等，用于前端组件判断',
    `sys_setting_group` varchar(255) NULL DEFAULT NULL COMMENT '所属分组 - 配置项所属的分组编码',
    `sys_setting_status` int NULL DEFAULT 1 COMMENT '是否启用 - 0:禁用, 1:启用',
    `sys_setting_sort` int NULL DEFAULT 0 COMMENT '排序号 - 配置项在分组中的显示顺序',
    `sys_setting_collect` int NULL DEFAULT 0 COMMENT '是否多值集合 - 0:单个值, 1:多个值的集合',
    `sys_setting_in_system` int NULL DEFAULT 0 COMMENT '是否系统配置 - 0:非系统配置, 1:系统内置配置',
    `sys_setting_app_inner` int NULL DEFAULT 0 COMMENT '是否应用启动配置 - 0:否, 1:是(影响应用启动)',
    `sys_setting_admin` int NULL DEFAULT 0 COMMENT '是否管理员专属 - 0:所有人可见, 1:仅管理员可见',
    `sys_setting_version` int NULL DEFAULT 1 COMMENT '配置版本 - 用于配置变更追踪',
    `sys_setting_config` varchar(255) NULL DEFAULT NULL COMMENT '配置参数 - 附加的配置参数(如关联字典ID)',
    `sys_setting_remark` varchar(255) NULL DEFAULT NULL COMMENT '配置备注 - 配置项用途说明',
    `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    `sys_setting_is_sensitive` int NULL DEFAULT 0 COMMENT '是否敏感配置: 0-否 1-是',
    `sys_setting_validate` varchar(512) NULL DEFAULT NULL COMMENT '校验规则(正则表达式, 可为空)',
    PRIMARY KEY (`sys_setting_id`),
    UNIQUE INDEX `sys_setting_uk_setting_name_group` (`sys_setting_name`, `sys_setting_group`),
    -- code 允许重复（大量内置项为空串），唯一性由 name + group 保证，故此处只建普通索引
    INDEX `sys_setting_idx_setting_code` (`sys_setting_code`),
    INDEX `sys_setting_idx_setting_group` (`sys_setting_group`),
    INDEX `sys_setting_idx_setting_status` (`sys_setting_status`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci 
COMMENT = '系统全局配置表 - 存储系统全局配置参数';

CREATE TABLE IF NOT EXISTS `sys_setting_group` (
    `sys_setting_group_id` int NOT NULL AUTO_INCREMENT COMMENT '分组ID - 主键自增',
    `sys_setting_group_name` varchar(255) NULL DEFAULT NULL COMMENT '分组名称 - 配置分组的显示名称',
    `sys_setting_group_code` varchar(255) NULL DEFAULT NULL COMMENT '分组编码 - 配置分组的唯一标识码',
    `sys_setting_group_icon` varchar(255) NULL DEFAULT NULL COMMENT '分组图标 - 图标类名或URL',
    `sys_setting_group_enable` TINYINT NULL DEFAULT 1 COMMENT '是否启用 - 0:禁用, 1:启用',
    `sys_setting_group_remark` varchar(255) NULL DEFAULT NULL COMMENT '分组描述 - 分组用途说明',
    `sys_setting_group_sort` int NULL DEFAULT 0 COMMENT '排序号 - 分组在列表中的显示顺序',
    `sys_setting_group_use_project_interface` TINYINT NULL DEFAULT 0 COMMENT '是否使用项目组接口 - 0:否, 1:是',
    `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_setting_group_id`),
    UNIQUE INDEX `sys_setting_group_uk_group_code` (`sys_setting_group_code`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci 
COMMENT = '系统配置分组表 - 对系统配置项进行分组管理';

CREATE TABLE IF NOT EXISTS `sys_file_system_group` (
    `sys_file_system_group_id` int NOT NULL AUTO_INCREMENT COMMENT '分组ID - 主键自增',
    `sys_file_system_group_name` varchar(100) NULL DEFAULT NULL COMMENT '分组名称 - 文件分组的显示名称',
    `sys_file_system_group_parent_id` int NULL DEFAULT 0 COMMENT '父分组ID - 支持多级分组，0表示顶级分组',
    `sys_file_system_group_path` varchar(255) NULL DEFAULT NULL COMMENT '分组路径 - 分组的短路径标识',
    `sys_file_system_group_full_path` varchar(500) NULL DEFAULT NULL COMMENT '完整路径 - 从根到当前分组的完整路径',
    `sys_file_system_group_level` int NULL DEFAULT 1 COMMENT '分组层级 - 分组在树中的层级深度',
    `sys_file_system_group_tree_path` varchar(500) NULL DEFAULT NULL COMMENT '树路径 - 分组ID组成的树路径，用于快速查询',
    `sys_file_system_group_description` varchar(500) NULL DEFAULT NULL COMMENT '分组描述 - 分组用途说明',
    `sys_file_system_group_is_default` TINYINT NULL DEFAULT 0 COMMENT '是否默认分组 - 0:否, 1:是(新文件默认归入)',
    `sys_file_system_group_sort` int NULL DEFAULT 0 COMMENT '排序号 - 分组在列表中的显示顺序',
    `sys_file_system_group_status` int NULL DEFAULT 1 COMMENT '分组状态 - 0:禁用, 1:启用',
    `sys_file_system_group_icon` varchar(100) NULL DEFAULT NULL COMMENT '分组图标 - 图标类名',
    `sys_file_system_group_color` varchar(20) NULL DEFAULT NULL COMMENT '分组颜色 - 十六进制颜色值',
    `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_file_system_group_id`),
    INDEX `sys_file_system_group_idx_parent_id` (`sys_file_system_group_parent_id`),
    INDEX `sys_file_system_group_idx_status` (`sys_file_system_group_status`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci 
COMMENT = '文件系统分组表 - 对上传的文件进行分组管理';

CREATE TABLE IF NOT EXISTS sys_job_cluster (
    sys_job_cluster_id              VARCHAR(64)  NOT NULL,
    sys_job_cluster_leader_node     VARCHAR(255),
    sys_job_cluster_lease_until_ms  BIGINT       DEFAULT 0,
    sys_job_cluster_version         BIGINT       DEFAULT 0,
    create_name                     VARCHAR(64),
    create_by                       INT,
    create_time                     DATETIME,
    update_name                     VARCHAR(64),
    update_by                       INT,
    update_time                     DATETIME,
    PRIMARY KEY (sys_job_cluster_id)
);


-- ---------------------------------------------------------------------
-- 来源: V1.0__init_oauth_complete.sql
-- ---------------------------------------------------------------------
/*
 * OAuth认证模块 - 数据库初始化脚本
 * 
 * 包含表:
 *   - app_key: AppKey应用密钥表
 *   - app_id: AppId应用标识表
 *   - sys_app_key: OAuth应用密钥表(AK/SK) - 兼容旧版本
 *   - sys_token_blacklist: Token黑名单表
 *
 * 注意: 此脚本只包含OAuth专用表，不包含基础系统表（sys_user, sys_role等）
 * 基础系统表请先执行 spring-api-support-system-starter 模块的初始化脚本
 *
 * @author System
 * @since 2025-01-01
 * @version 1.0
 */

SET NAMES utf8;

SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 1. AppKey应用密钥表 (app_key)
-- 用途: 存储AppKey相关信息，用于API访问认证
-- =====================================================

CREATE TABLE IF NOT EXISTS `app_key` (
    `app_key_id` int NOT NULL AUTO_INCREMENT COMMENT 'AppKey ID - 主键自增',
    `app_key_value` varchar(100) NOT NULL COMMENT 'AppKey值 - 应用访问密钥，类似用户名，全局唯一',
    `app_key_name` varchar(100) NULL DEFAULT NULL COMMENT 'AppKey名称 - 应用的显示名称',
    `app_key_secret` varchar(200) NOT NULL COMMENT 'AppSecret - 应用访问密钥，类似密码（需加密存储）',
    `app_key_description` varchar(500) NULL DEFAULT NULL COMMENT '应用描述 - 应用的详细说明',
    `app_key_ip_whitelist` varchar(1000) NULL DEFAULT NULL COMMENT 'IP白名单 - 允许访问的IP地址列表，逗号分隔，如"192.168.1.1,10.0.0.1"',
    `app_key_status` int NULL DEFAULT 1 COMMENT '应用状态 - 0:禁用, 1:启用',
    `app_key_valid_days` int NOT NULL DEFAULT 30 COMMENT '有效天数，0表示不限时',
    `app_key_expire_time` bigint NULL DEFAULT NULL COMMENT '过期时间 - Unix时间戳(毫秒)，NULL表示永不过期',
    `app_key_last_use_time` bigint NULL DEFAULT NULL COMMENT '最后使用时间 - Unix时间戳(毫秒)，最近一次使用该密钥的时间',
    `app_key_use_count` bigint NULL DEFAULT 0 COMMENT '使用次数 - 该密钥累计被调用的次数',
    `user_id` int NULL DEFAULT NULL COMMENT '关联用户ID - 该AppKey所属的用户ID',
    `app_key_scope` varchar(500) NULL DEFAULT NULL COMMENT '允许的权限范围 - 多个scope用逗号分隔，如"read,write,admin"',
    `app_key_grant_types` varchar(500) NULL DEFAULT NULL COMMENT '允许的授权类型 - 多个用逗号分隔，如"client_credentials,password,authorization_code,refresh_token"',
    `app_key_redirect_uris` varchar(1000) NULL DEFAULT NULL COMMENT '允许的回调地址 - 多个用逗号分隔，用于authorization_code模式的redirect_uri校验',
    `app_key_remark` varchar(500) NULL DEFAULT NULL COMMENT '备注信息',
    `delete_flag` int NULL DEFAULT 0 COMMENT '删除标记 - 0:未删除, 1:已删除',
    `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`app_key_id`),
    UNIQUE INDEX `app_key_uk_app_key_value` (`app_key_value`),
    INDEX `app_key_idx_status` (`app_key_status`),
    INDEX `app_key_idx_expire_time` (`app_key_expire_time`),
    INDEX `app_key_idx_user_id` (`user_id`),
    INDEX `app_key_idx_delete_flag` (`delete_flag`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci 
COMMENT = 'AppKey应用密钥表 - 存储AppKey相关信息，用于API访问认证';

-- =====================================================
-- 2. AppId应用标识表 (app_id)
-- 用途: 存储AppId相关信息，用于应用标识和关联
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
    `app_id_scope` varchar(500) NULL DEFAULT NULL COMMENT '允许的权限范围 - 多个scope用逗号分隔，如"read,write,admin"',
    `app_id_grant_types` varchar(500) NULL DEFAULT NULL COMMENT '允许的授权类型 - 多个用逗号分隔，如"client_credentials,password,authorization_code,refresh_token"',
    `app_id_redirect_uris` varchar(1000) NULL DEFAULT NULL COMMENT '允许的回调地址 - 多个用逗号分隔，用于authorization_code模式的redirect_uri校验',
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
COMMENT = 'AppId应用标识表 - 存储AppId相关信息，与app_key表是并列关系';

-- =====================================================
-- 3. OAuth应用密钥表 (sys_app_key) - 兼容旧版本
-- 用途: 存储第三方应用的AK/SK密钥对，用于OAuth认证
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
    `sys_app_key_use_count` int NULL DEFAULT 0 COMMENT '使用次数',
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
    INDEX `sys_app_key_idx_status` (`sys_app_key_status`),
    INDEX `sys_app_key_idx_user_id` (`sys_user_id`),
    INDEX `sys_app_key_idx_delete` (`sys_app_key_delete`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci 
COMMENT = 'OAuth应用密钥表 - 存储第三方应用的AK/SK，用于API访问认证';

-- =====================================================
-- 4. Token黑名单表 (sys_token_blacklist)
-- 用途: 存储已失效的Token，用于登出、踢出、密码修改等场景
-- =====================================================

CREATE TABLE IF NOT EXISTS `sys_token_blacklist` (
    `sys_blacklist_id` bigint NOT NULL AUTO_INCREMENT COMMENT '黑名单ID - 主键自增',
    `sys_token_value` varchar(500) NOT NULL COMMENT 'Token值 - 完整的JWT Token字符串',
    `sys_token_hash` varchar(64) NOT NULL COMMENT 'Token哈希值 - Token的SHA-256哈希，用于快速查询匹配',
    `sys_user_id` varchar(255) NULL DEFAULT NULL COMMENT '用户ID - 该Token所属的用户ID',
    `sys_username` varchar(255) NULL DEFAULT NULL COMMENT '用户名 - 该Token所属的用户名',
    `sys_blacklist_reason` varchar(255) NULL DEFAULT NULL COMMENT '加入黑名单原因 - LOGOUT:用户登出, KICKED:被管理员踢出, EXPIRED:Token过期, REVOKED:主动撤销, PASSWORD_CHANGED:密码修改, SECURITY:安全原因',
    `sys_blacklist_source` varchar(100) NULL DEFAULT NULL COMMENT '操作来源 - USER:用户主动操作, ADMIN:管理员操作, SYSTEM:系统自动处理',
    `sys_expire_time` datetime NOT NULL COMMENT 'Token过期时间 - 与原Token的过期时间一致，用于定期清理',
    `sys_client_ip` varchar(50) NULL DEFAULT NULL COMMENT '客户端IP - 执行操作时的客户端IP地址',
    `sys_user_agent` varchar(500) NULL DEFAULT NULL COMMENT 'User-Agent - 执行操作时的浏览器UA信息',
    `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 - 加入黑名单的时间',
    PRIMARY KEY (`sys_blacklist_id`),
    INDEX `sys_token_blacklist_idx_token_hash` (`sys_token_hash`),
    INDEX `sys_token_blacklist_idx_user_id` (`sys_user_id`),
    INDEX `sys_token_blacklist_idx_expire_time` (`sys_expire_time`),
    INDEX `sys_token_blacklist_idx_reason` (`sys_blacklist_reason`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci 
COMMENT = 'Token黑名单表 - 存储已失效的Token，防止失效Token继续使用';

-- =====================================================
-- 5. OAuth追踪日志表 (sys_oauth_trace_log)
-- 用途: 记录OAuth认证请求的完整链路追踪信息
-- =====================================================

CREATE TABLE IF NOT EXISTS `sys_oauth_trace_log` (
    `sys_trace_log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID - 主键自增',
    `sys_trace_id` varchar(64) NOT NULL COMMENT '追踪ID - 贯穿客户端和服务端的唯一标识',
    `sys_operation_type` varchar(50) NULL DEFAULT NULL COMMENT '操作类型 - AUTH:认证, LOGIN:登录, LOGOUT:登出, REFRESH:刷新令牌, UPGRADE:升级令牌',
    `sys_client_app_name` varchar(100) NULL DEFAULT NULL COMMENT '客户端应用名 - 发起请求的应用名称',
    `sys_client_ip` varchar(50) NULL DEFAULT NULL COMMENT '客户端IP - 发起请求的客户端IP地址',
    `sys_user_id` varchar(255) NULL DEFAULT NULL COMMENT '用户ID - 认证成功后的用户ID',
    `sys_username` varchar(255) NULL DEFAULT NULL COMMENT '用户名 - 认证成功后的用户名',
    `sys_request_params` text NULL COMMENT '请求参数 - JSON格式，敏感字段已脱敏',
    `sys_response_code` int NULL DEFAULT NULL COMMENT '响应码 - HTTP响应状态码或业务码',
    `sys_success` TINYINT NULL DEFAULT 0 COMMENT '是否成功 - 0:失败, 1:成功',
    `sys_total_cost_ms` bigint NULL DEFAULT NULL COMMENT '总耗时 - 从客户端发起到服务端响应的总耗时(毫秒)',
    `sys_spans_detail` text NULL COMMENT 'Span详情 - JSON数组，记录各节点的名称和耗时',
    `sys_error_message` varchar(1000) NULL DEFAULT NULL COMMENT '错误信息 - 失败时的错误描述',
    `sys_request_uri` varchar(500) NULL DEFAULT NULL COMMENT '请求URI - 请求的接口路径',
    `sys_access_key` varchar(100) NULL DEFAULT NULL COMMENT 'AccessKey - 使用的应用密钥(已脱敏)',
    `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 - 日志记录时间',
    PRIMARY KEY (`sys_trace_log_id`),
    INDEX `sys_oauth_trace_log_idx_trace_id` (`sys_trace_id`),
    INDEX `sys_oauth_trace_log_idx_client_app` (`sys_client_app_name`),
    INDEX `sys_oauth_trace_log_idx_user_id` (`sys_user_id`),
    INDEX `sys_oauth_trace_log_idx_create_time` (`create_time`),
    INDEX `sys_oauth_trace_log_idx_success` (`sys_success`),
    INDEX `sys_oauth_trace_log_idx_operation_type` (`sys_operation_type`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci
COMMENT = 'OAuth追踪日志表 - 记录认证请求的完整链路追踪信息，用于问题排查和性能分析';

-- =====================================================
-- 6. 浏览器指纹与用户映射表 (sys_fingerprint)
-- 用途: 记录用户登录时的浏览器指纹和IP，支持通过指纹/IP反查用户身份
-- =====================================================

CREATE TABLE IF NOT EXISTS `sys_fingerprint` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `fingerprint` varchar(128) NOT NULL COMMENT '浏览器指纹（SHA-256哈希）',
    `user_id` varchar(64) NOT NULL COMMENT '用户ID',
    `username` varchar(100) NULL DEFAULT NULL COMMENT '用户名',
    `ip` varchar(64) NULL DEFAULT NULL COMMENT '登录IP',
    `login_type` varchar(50) NULL DEFAULT NULL COMMENT '登录类型（WEB/MINI/GITEE等）',
    `user_agent` varchar(500) NULL DEFAULT NULL COMMENT 'User-Agent',
    `browser` varchar(50) NULL DEFAULT NULL COMMENT '浏览器',
    `os` varchar(50) NULL DEFAULT NULL COMMENT '操作系统',
    `login_time` bigint NULL DEFAULT NULL COMMENT '登录时间戳（毫秒）',
    `last_login_time` bigint NULL DEFAULT NULL COMMENT '最后在线时间戳（毫秒）',
    `token_count` int NULL DEFAULT 1 COMMENT '当前在线Token数',
    `status` int NULL DEFAULT 1 COMMENT '状态：0-离线，1-在线',
    `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `sys_fingerprint_uk_fingerprint` (`fingerprint`),
    INDEX `sys_fingerprint_idx_user_id` (`user_id`),
    INDEX `sys_fingerprint_idx_ip` (`ip`),
    INDEX `sys_fingerprint_idx_login_time` (`login_time`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci
COMMENT = '浏览器指纹与用户映射表 - 支持通过指纹/IP反查用户身份';

-- =====================================================
-- 定期清理任务说明
-- =====================================================
-- 建议配置定时任务定期清理已过期的黑名单记录：
-- DELETE FROM sys_token_blacklist WHERE sys_expire_time < NOW() - INTERVAL 7 DAY;
-- 
-- 建议配置定时任务定期清理追踪日志（保留30天）：
-- DELETE FROM sys_oauth_trace_log WHERE create_time < NOW() - INTERVAL 30 DAY;
-- 
-- 或者使用事件调度器(Event Scheduler)：
-- CREATE EVENT IF NOT EXISTS cleanup_expired_blacklist
-- ON SCHEDULE EVERY 1 DAY
-- DO DELETE FROM sys_token_blacklist WHERE sys_expire_time < NOW() - INTERVAL 7 DAY;
--
-- CREATE EVENT IF NOT EXISTS cleanup_trace_logs
-- ON SCHEDULE EVERY 1 DAY
-- DO DELETE FROM sys_oauth_trace_log WHERE create_time < NOW() - INTERVAL 30 DAY;

SET FOREIGN_KEY_CHECKS = 1;

