-- =====================================================================
-- V1.0__init_all_api_monitor.sql - spring-api-support-monitor-starter 全量 DDL（合并自 2 个文件）
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
    `sys_setting_value` varchar(255) NULL DEFAULT NULL COMMENT '配置值 - 配置项的值',
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
-- 来源: V1.0__init_monitor_complete.sql
-- ---------------------------------------------------------------------
-- 监控中心完整表结构
-- 包含 ACME 证书管理、服务器/组件布局管理、Docker 软件库与软件仓库

SET NAMES utf8;

SET FOREIGN_KEY_CHECKS = 0;

-- ACME 账户表

CREATE TABLE IF NOT EXISTS `monitor_acme_account` (
  `acme_account_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `acme_account_email` varchar(255) NULL DEFAULT NULL COMMENT '账号邮箱',
  `acme_account_server` varchar(255) NULL DEFAULT NULL COMMENT 'ACME 服务端点',
  `acme_account_provider_type` varchar(32) NOT NULL DEFAULT 'acme' COMMENT '证书服务商类型(acme=ACME协议/acme4j、cas=阿里云CAS)（并入自 V1.2__add_acme_account_provider_type.sql）',
  `acme_account_status` varchar(255) NULL DEFAULT NULL COMMENT '账户状态',
  `acme_account_private_key_pem` text NULL DEFAULT NULL COMMENT '账户私钥（PEM）',
  `acme_account_eab_kid` varchar(255) NULL DEFAULT NULL COMMENT 'EAB Key ID (ZeroSSL 等需要)',
  `acme_account_eab_hmac_key` varchar(512) NULL DEFAULT NULL COMMENT 'EAB HMAC Key (Base64 编码)',
  `acme_account_remark` varchar(255) NULL DEFAULT NULL COMMENT '备注',
  `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`acme_account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ACME 账户表';

-- ACME 证书表

CREATE TABLE IF NOT EXISTS `monitor_acme_certificate` (
  `acme_cert_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `acme_account_id` int NULL DEFAULT NULL COMMENT '账户ID',
  `acme_cert_primary_domain` varchar(255) NULL DEFAULT NULL COMMENT '主域名',
  `acme_cert_san` varchar(255) NULL DEFAULT NULL COMMENT '备用域名(SAN)，逗号分隔',
  `acme_cert_challenge_type` varchar(255) NULL DEFAULT NULL COMMENT '挑战类型（HTTP-01/DNS-01）',
  `acme_cert_status` varchar(255) NULL DEFAULT NULL COMMENT '证书状态',
  `acme_cert_not_before` varchar(255) NULL DEFAULT NULL COMMENT '有效期起',
  `acme_cert_not_after` varchar(255) NULL DEFAULT NULL COMMENT '有效期止',
  `acme_cert_renew_at` varchar(255) NULL DEFAULT NULL COMMENT '计划续签日期',
  `acme_cert_chain_pem` text NULL COMMENT '证书PEM（含链）',
  `acme_cert_key_pem` text NULL COMMENT '私钥PEM',
  `acme_cert_last_error` varchar(255) NULL DEFAULT NULL COMMENT '最近错误',
  `acme_cert_deploy_mode` varchar(20) NULL DEFAULT 'manual' COMMENT '部署模式：local(本机自动)/manual(手动)',
  `acme_cert_webroot_path` varchar(255) NULL DEFAULT NULL COMMENT '本机模式webroot路径',
  `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`acme_cert_id`),
  KEY `monitor_acme_certificate_idx_account_id` (`acme_account_id`),
  KEY `monitor_acme_certificate_idx_domain` (`acme_cert_primary_domain`),
  KEY `monitor_acme_certificate_idx_status` (`acme_cert_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ACME 证书表';

-- ACME 订单表

CREATE TABLE IF NOT EXISTS `monitor_acme_order` (
  `acme_order_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `acme_account_id` int NULL DEFAULT NULL COMMENT '账户ID',
  `acme_cert_id` int NULL DEFAULT NULL COMMENT '证书ID',
  `acme_order_url` varchar(255) NULL DEFAULT NULL COMMENT '订单URL',
  `acme_authz_urls` text NULL COMMENT '授权URL列表(JSON)',
  `acme_order_status` varchar(255) NULL DEFAULT NULL COMMENT '状态',
  `acme_certificate_url` varchar(255) NULL DEFAULT NULL COMMENT '最终证书URL',
  `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`acme_order_id`),
  KEY `monitor_acme_order_idx_account_id` (`acme_account_id`),
  KEY `monitor_acme_order_idx_cert_id` (`acme_cert_id`),
  KEY `monitor_acme_order_idx_status` (`acme_order_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ACME 订单表';

-- 服务器布局表

CREATE TABLE IF NOT EXISTS `monitor_sys_gen_server_layout` (
  `monitor_sys_gen_server_layout_id` int NOT NULL AUTO_INCREMENT COMMENT '布局ID',
  `monitor_sys_gen_server_id` int NULL DEFAULT NULL COMMENT '关联服务器ID',
  `monitor_sys_gen_server_layout_name` varchar(255) NULL DEFAULT NULL COMMENT '布局名称',
  `monitor_sys_gen_server_layout_config` text NULL DEFAULT NULL COMMENT '布局配置 JSON',
  `monitor_sys_gen_server_layout_type` varchar(32) NULL DEFAULT NULL COMMENT '布局类型（default/generated/copied/custom）',
  `monitor_sys_gen_server_layout_default` int NULL DEFAULT 0 COMMENT '是否默认布局（0/1）',
  `monitor_sys_gen_server_layout_status` int NULL DEFAULT 1 COMMENT '布局状态（0禁用/1启用）',
  `monitor_sys_gen_server_layout_description` varchar(512) NULL DEFAULT NULL COMMENT '布局描述',
  `monitor_sys_gen_server_layout_version` int NULL DEFAULT 1 COMMENT '布局版本号',
  `monitor_sys_gen_server_layout_sort` int NULL DEFAULT 0 COMMENT '布局排序',
  `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_server_layout_id`),
  KEY `monitor_sys_gen_server_layout_idx_server_id` (`monitor_sys_gen_server_id`),
  KEY `monitor_sys_gen_server_layout_idx_type` (`monitor_sys_gen_server_layout_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务器布局表';

-- 服务器组件布局表

CREATE TABLE IF NOT EXISTS `monitor_sys_gen_server_component_layout` (
  `monitor_sys_gen_server_component_layout_id` int NOT NULL AUTO_INCREMENT COMMENT '组件布局ID',
  `monitor_sys_gen_server_id` int NULL DEFAULT NULL COMMENT '关联服务器ID',
  `monitor_sys_gen_server_component_id` int NULL DEFAULT NULL COMMENT '关联组件ID',
  `monitor_sys_gen_server_component_layout_x` int NULL DEFAULT 0 COMMENT 'X 坐标',
  `monitor_sys_gen_server_component_layout_y` int NULL DEFAULT 0 COMMENT 'Y 坐标',
  `monitor_sys_gen_server_component_layout_w` int NULL DEFAULT 0 COMMENT '宽度',
  `monitor_sys_gen_server_component_layout_h` int NULL DEFAULT 0 COMMENT '高度',
  `monitor_sys_gen_server_component_layout_z_index` int NULL DEFAULT 0 COMMENT '层级',
  `monitor_sys_gen_server_component_layout_movable` int NULL DEFAULT 1 COMMENT '是否可移动（0/1）',
  `monitor_sys_gen_server_component_layout_resizable` int NULL DEFAULT 1 COMMENT '是否可调整大小（0/1）',
  `monitor_sys_gen_server_component_layout_status` int NULL DEFAULT 1 COMMENT '布局状态（0禁用/1启用）',
  `monitor_sys_gen_server_component_layout_sort_order` int NULL DEFAULT 0 COMMENT '排序顺序',
  `monitor_sys_gen_server_component_layout_chart_config` text NULL DEFAULT NULL COMMENT '组件图表配置 JSON',
  `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`monitor_sys_gen_server_component_layout_id`),
  KEY `monitor_sys_gen_server_component_layout_idx_server_id` (`monitor_sys_gen_server_id`),
  KEY `monitor_sys_gen_server_component_layout_idx_component_id` (`monitor_sys_gen_server_component_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务器组件布局表';

-- 被监控服务器主机表

CREATE TABLE IF NOT EXISTS `monitor_server` (
  `server_id` int NOT NULL AUTO_INCREMENT COMMENT '服务器ID',
  `server_name` varchar(255) NULL DEFAULT NULL COMMENT '服务器名称',
  `server_ip` varchar(64) NULL DEFAULT NULL COMMENT '服务器IP',
  `server_host` varchar(255) NULL DEFAULT NULL COMMENT '主机名',
  `server_port` int NULL DEFAULT NULL COMMENT '端口',
  `server_os` varchar(64) NULL DEFAULT NULL COMMENT '操作系统',
  `server_group` varchar(128) NULL DEFAULT NULL COMMENT '分组',
  `server_account` varchar(128) NULL DEFAULT NULL COMMENT '登录账号',
  `server_status` int NULL DEFAULT 1 COMMENT '状态（0禁用/1启用）',
  `server_remark` varchar(1024) NULL DEFAULT NULL COMMENT '备注',
  `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`server_id`),
  KEY `monitor_server_idx_name` (`server_name`),
  KEY `monitor_server_idx_ip` (`server_ip`),
  KEY `monitor_server_idx_group` (`server_group`),
  KEY `monitor_server_idx_status` (`server_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='被监控服务器主机表';

-- ============================================================================
-- 老库幂等补列段：monitor_acme_account.provider_type（并入自 V1.2__add_acme_account_provider_type.sql）
-- 新库全量 DDL 已含该列；老库缺列时此处补齐（ADD COLUMN IF NOT EXISTS 为 MySQL/H2 通用写法）
-- ============================================================================
-- 操作日志表（并入自 V1.3__add_operation_log.sql）
CREATE TABLE IF NOT EXISTS ms_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    operator VARCHAR(64) NOT NULL COMMENT '操作用户',
    operation VARCHAR(32) NOT NULL COMMENT '操作类型',
    target_module VARCHAR(32) NOT NULL COMMENT '目标模块',
    target_id VARCHAR(64) COMMENT '目标对象ID',
    description TEXT COMMENT '操作描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_operator (operator),
    INDEX idx_target (target_module, target_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- =====================================================================
-- 监控模块核心业务表（并入自 V1.1__init_ms_monitor.sql）
-- 资产、采集任务、解析规则、网关、全局配置；对应 pages/monitor 前端模块
-- =====================================================================

-- 监控资产表
CREATE TABLE IF NOT EXISTS `ms_monitor` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `monitor_name` varchar(128) NOT NULL COMMENT '资产名称',
  `monitor_host` varchar(256) NOT NULL COMMENT '主机地址',
  `monitor_port` int NOT NULL DEFAULT 22 COMMENT '端口',
  `service_type` varchar(32) NOT NULL DEFAULT 'SERVER' COMMENT '服务类型(SERVER/REDIS/MYSQL/PG/ORACLE/MSSQL)',
  `server_type` varchar(32) NULL DEFAULT NULL COMMENT '系统类型(LINUX/WINDOWS/MACOS)',
  `monitor_group` varchar(64) NULL DEFAULT NULL COMMENT '分组',
  `monitor_enabled` tinyint NOT NULL DEFAULT 1 COMMENT '启用状态(1启用/0禁用)',
  `monitor_description` varchar(512) NULL DEFAULT NULL COMMENT '备注说明',
  `ms_server_host_id` bigint NULL DEFAULT NULL COMMENT '关联服务器主机ID(ms_server_host.id)',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除(0未删/1已删)',
  `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `ms_monitor_idx_service_type` (`service_type`),
  KEY `ms_monitor_idx_monitor_group` (`monitor_group`),
  KEY `ms_monitor_idx_monitor_enabled` (`monitor_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='监控资产表';

-- 采集任务表
CREATE TABLE IF NOT EXISTS `ms_monitor_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_name` varchar(128) NOT NULL COMMENT '任务名称',
  `monitor_id` bigint NOT NULL COMMENT '关联监控资产ID',
  `task_type` varchar(32) NOT NULL DEFAULT 'HEALTH' COMMENT '任务类型(HEALTH/COLLECT/ALERT/CUSTOM)',
  `task_cron` varchar(64) NULL DEFAULT NULL COMMENT 'Cron 表达式',
  `task_enabled` tinyint NOT NULL DEFAULT 1 COMMENT '启用状态(1启用/0禁用)',
  `task_config` text NULL COMMENT '任务配置(JSON)',
  `task_status` varchar(32) NOT NULL DEFAULT 'STOPPED' COMMENT '运行状态(RUNNING/STOPPED/ERROR)',
  `task_description` varchar(512) NULL DEFAULT NULL COMMENT '备注说明',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除(0未删/1已删)',
  `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `ms_monitor_task_idx_monitor_id` (`monitor_id`),
  KEY `ms_monitor_task_idx_task_type` (`task_type`),
  KEY `ms_monitor_task_idx_task_status` (`task_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采集任务表';

-- 指标解析规则表
CREATE TABLE IF NOT EXISTS `ms_monitor_metric_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `metric_name` varchar(128) NOT NULL COMMENT '指标名称',
  `task_id` bigint NOT NULL COMMENT '关联采集任务ID',
  `metric_type` varchar(32) NOT NULL COMMENT '指标类型(CPU/MEMORY/DISK/NETWORK/JVM/REDIS)',
  `match_pattern` varchar(512) NULL DEFAULT NULL COMMENT '匹配模式(正则)',
  `extract_rule` varchar(512) NULL DEFAULT NULL COMMENT '提取规则(JSON)',
  `unit` varchar(32) NULL DEFAULT NULL COMMENT '单位(%)',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除(0未删/1已删)',
  `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `ms_monitor_metric_config_idx_task_id` (`task_id`),
  KEY `ms_monitor_metric_config_idx_metric_type` (`metric_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='指标解析规则表';

-- 监控网关表
CREATE TABLE IF NOT EXISTS `ms_monitor_gateway` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `gateway_name` varchar(128) NOT NULL COMMENT '网关名称',
  `monitor_id` bigint NOT NULL COMMENT '关联监控资产ID',
  `gateway_host` varchar(256) NOT NULL COMMENT '网关主机',
  `gateway_port` int NOT NULL COMMENT '网关端口',
  `protocol` varchar(32) NOT NULL DEFAULT 'TCP' COMMENT '协议(TCP/UDP/HTTP)',
  `auth_enabled` tinyint NOT NULL DEFAULT 0 COMMENT '认证开关(1启用/0禁用)',
  `gateway_status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '网关状态(ACTIVE/STANDBY/ERROR)',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除(0未删/1已删)',
  `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `ms_monitor_gateway_idx_monitor_id` (`monitor_id`),
  KEY `ms_monitor_gateway_idx_gateway_status` (`gateway_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='监控网关表';

-- 全局配置表
CREATE TABLE IF NOT EXISTS `ms_monitor_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` varchar(128) NOT NULL COMMENT '配置键',
  `config_value` text NULL COMMENT '配置值',
  `config_type` varchar(32) NOT NULL DEFAULT 'STRING' COMMENT '值类型(STRING/NUMBER/BOOLEAN/JSON)',
  `config_group` varchar(64) NULL DEFAULT NULL COMMENT '配置分组',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除(0未删/1已删)',
  `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `ms_monitor_config_uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='全局配置表';

SET FOREIGN_KEY_CHECKS = 1;

