-- =====================================================================
-- V1.0__init_all_api_common_system.sql - spring-api-support-common-system-starter 全量 DDL（合并自 1 个文件）
-- 通道: INIT 同步建表；老库缺列/缺表由 information_schema + PREPARE 段幂等补齐。
-- 种子数据见 V1.0__initdata_all.sql（虚拟线程异步，不阻塞启动）。
-- =====================================================================

-- ---------------------------------------------------------------------
-- 来源: V1.0__init_sys_error_log.sql
-- ---------------------------------------------------------------------
/*
 * 前端异常上报日志表 - 数据库初始化脚本
 *
 * 包含表:
 *   - sys_error_log: 前端异常上报日志表, 承接 monitor worker 批量上报的运行时错误
 *
 * 使用 CREATE TABLE IF NOT EXISTS 保证脚本幂等, 已存在的库不会重复建表。
 *
 * @author CH
 * @since 2026-08-08
 */
CREATE TABLE IF NOT EXISTS `sys_error_log` (
  `sys_error_log_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '异常日志ID',
  `sys_error_key` varchar(512) DEFAULT NULL COMMENT '去重指纹(message+source+行列号归一化)',
  `sys_error_source` varchar(32) DEFAULT NULL COMMENT '异常来源: js:JS运行时错误 rejection:未处理Promise拒绝 resource:资源加载失败 manual:手动捕获',
  `sys_error_message` varchar(2000) DEFAULT NULL COMMENT '异常消息',
  `sys_error_stack` text DEFAULT NULL COMMENT '异常堆栈',
  `sys_error_filename` varchar(512) DEFAULT NULL COMMENT '出错文件URL',
  `sys_error_lineno` INT DEFAULT NULL COMMENT '出错行号',
  `sys_error_colno` INT DEFAULT NULL COMMENT '出错列号',
  `sys_error_resource_url` varchar(512) DEFAULT NULL COMMENT '资源加载失败的目标URL',
  `sys_error_tag_name` varchar(32) DEFAULT NULL COMMENT '资源元素标签(如IMG/SCRIPT/LINK)',
  `sys_error_first_at` BIGINT DEFAULT NULL COMMENT '首次发生时间(毫秒时间戳)',
  `sys_error_count` INT DEFAULT 1 COMMENT '去重窗口内累计发生次数',
  `sys_error_report_time` BIGINT DEFAULT NULL COMMENT '客户端上报时间(毫秒时间戳)',
  `create_name` varchar(255) DEFAULT NULL COMMENT '创建人姓名',
  `create_by` INT DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) DEFAULT NULL COMMENT '更新人姓名',
  `update_by` INT DEFAULT NULL COMMENT '更新人',
  `sys_error_status` int NULL DEFAULT 0 COMMENT '处理状态: 0-未处理 1-已处理 2-已忽略',
  PRIMARY KEY (`sys_error_log_id`),
  KEY `sys_error_log_idx_error_log_source` (`sys_error_source`),
  KEY `sys_error_log_idx_error_log_key` (`sys_error_key`),
  KEY `sys_error_log_idx_error_log_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='前端异常上报日志表';

-- ---------------------------------------------------------------------
-- 来源: SysUserTripartiteBind 实体（第三方账号绑定），此前无对应建表脚本
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_user_tripartite_bind` (
  `user_id` int NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `sys_tenant_id` int NULL DEFAULT NULL COMMENT '租户ID',
  `login_code_type` varchar(255) NULL DEFAULT NULL COMMENT '登录类型',
  `login_username` varchar(255) NULL DEFAULT NULL COMMENT '登录账号',
  `login_uuid` varchar(255) NULL DEFAULT NULL COMMENT '三方唯一标识',
  `login_avatar` varchar(255) NULL DEFAULT NULL COMMENT '头像',
  `login_name` varchar(255) NULL DEFAULT NULL COMMENT '昵称',
  `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户第三方绑定';

-- ---------------------------------------------------------------------
-- 来源: 用户域建表自 spring-api-support-system-starter 下沉
-- 本模块已承载 SysUser/SysRole/SysDept/SysUserRole 的实体、Mapper 与登录服务，
-- 建表必须随代码归口；否则任何只嵌入本模块的宿主（如采集调度平台）
-- 在 /v2/user/login 上会因缺表直接失败。
-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS `sys_dept` (
    `sys_dept_id` INT NOT NULL AUTO_INCREMENT COMMENT '机构ID - 主键自增',
    `sys_dept_name` varchar(255)  NULL DEFAULT NULL COMMENT '机构名称 - 组织/部门的显示名称',
    `sys_dept_pid` INT NULL DEFAULT NULL COMMENT '父机构ID - 上级组织的ID，顶级为0',
    `sys_dept_tree_id` varchar(255)  NULL DEFAULT NULL COMMENT '机构链路 - 从根节点到当前节点的ID路径',
    `sys_dept_code` varchar(255)  NULL DEFAULT NULL COMMENT '组织编码 - 机构的唯一业务编码',
    `sys_dept_principal` varchar(255)  NULL DEFAULT NULL COMMENT '部门负责人 - 该部门的负责人姓名',
    `sys_dept_contact` varchar(255)  NULL DEFAULT NULL COMMENT '负责人联系方式 - 负责人的电话或其他联系方式',
    `sys_dept_sort` INT NULL DEFAULT NULL COMMENT '排序优先级 - 数值越小越靠前',
    `sys_dept_status` INT NULL DEFAULT NULL COMMENT '状态 - 0:启用, 1:禁用',
    `sys_dept_remark` varchar(255)  NULL DEFAULT NULL COMMENT '备注 - 附加说明信息',
    `sys_dept_data_permission` INT NULL DEFAULT NULL COMMENT '数据权限类型 - 1:全部可见, 2:本人可见, 3:所在部门可见, 4:所在部门及子级可见, 5:选择的部门可见',
    `sys_dept_data_permission_dept_id` varchar(255)  NULL DEFAULT NULL COMMENT '数据权限可见部门ID列表 - 当数据权限类型为5时，存储可见的部门ID，逗号分隔',
    `create_name` varchar(255)  NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` INT NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255)  NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` INT NULL DEFAULT NULL COMMENT '更新人ID',
    `sys_dept_data_permission_dept_ids` varchar(1024) NULL DEFAULT NULL COMMENT '自定义数据权限可见的部门ID列表(JSON数组)',
    PRIMARY KEY (`sys_dept_id`),
    INDEX `sys_dept_idx_dept_pid` (`sys_dept_pid`),
    INDEX `sys_dept_idx_dept_code` (`sys_dept_code`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '组织机构表 - 存储系统的部门/组织架构信息';

CREATE TABLE IF NOT EXISTS `sys_role` (
    `sys_role_id` INT NOT NULL AUTO_INCREMENT COMMENT '角色ID - 主键自增',
    `sys_role_name` varchar(255)  NULL DEFAULT NULL COMMENT '角色名称 - 角色的显示名称',
    `sys_role_code` varchar(255)  NULL DEFAULT NULL COMMENT '角色编码 - 角色的唯一标识码，如 ADMIN',
    `sys_role_in_system` INT NULL DEFAULT NULL COMMENT '是否系统角色 - 0:非系统角色, 1:系统内置角色(不可删除)',
    `sys_role_status` INT NULL DEFAULT NULL COMMENT '是否启用 - 0:禁用, 1:启用',
    `sys_role_sort` INT NULL DEFAULT NULL COMMENT '排序优先级 - 数值越小越靠前',
    `sys_role_remark` varchar(255)  NULL DEFAULT NULL COMMENT '备注 - 角色的说明描述',
    `sys_role_hidden` INT NULL DEFAULT 0 COMMENT '是否隐藏 - 0:显示, 1:隐藏(前端不展示)',
    `sys_role_readable` INT NULL DEFAULT NULL COMMENT '角色可读权限位 - 二进制位表示的读权限',
    `sys_role_writeable` INT NULL DEFAULT NULL COMMENT '角色可写权限位 - 二进制位表示的写权限',
    `sys_role_executable` INT NULL DEFAULT NULL COMMENT '角色可执行权限位 - 二进制位表示的执行权限',
    `sys_role_board_card` INT NULL DEFAULT NULL COMMENT '角色可看板类型 - 该角色可查看的看板类型',
    `create_name` varchar(255)  NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` INT NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255)  NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` INT NULL DEFAULT NULL COMMENT '更新人ID',
    `sys_role_data_scope` int NULL DEFAULT 1 COMMENT '数据权限范围: 1-全部数据 2-本部门数据 3-本部门及以下 4-仅本人数据 5-自定义部门数据',
    `sys_role_data_scope_dept_ids` varchar(512) NULL DEFAULT NULL COMMENT '自定义数据权限部门ID列表(逗号分隔)',
    `sys_role_readonly` int NULL DEFAULT 0 COMMENT '是否只读角色: 0-否 1-是',
    PRIMARY KEY (`sys_role_id`),
    UNIQUE INDEX `sys_role_uk_role_code` (`sys_role_code`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '角色表 - 存储系统角色信息，用于RBAC权限控制';

CREATE TABLE IF NOT EXISTS `sys_user` (
    `sys_user_id` INT NOT NULL AUTO_INCREMENT COMMENT '用户ID - 主键自增',
    `sys_user_username` varchar(255)  NULL DEFAULT NULL COMMENT '用户账号 - 登录使用的唯一账号名',
    `sys_user_nickname` varchar(255)  NULL DEFAULT NULL COMMENT '用户昵称 - 显示名称',
    `sys_user_password` varchar(255)  NULL DEFAULT NULL COMMENT '用户密码 - 加密后的密码',
    `sys_user_salt` varchar(255)  NULL DEFAULT NULL COMMENT '密码盐值 - 用于密码加密的随机字符串',
    `sys_user_delete` INT NOT NULL DEFAULT 0 COMMENT '是否删除 - 0:未删除, 非0:已删除(逻辑删除,值为本行主键,墓碑行互不冲突)',
    `sys_user_status` INT NULL DEFAULT NULL COMMENT '用户状态 - 0:禁用, 1:启用',
    `sys_user_phone` varchar(255)  NULL DEFAULT NULL COMMENT '手机号码 - 用户的手机号',
    `sys_user_email` varchar(255)  NULL DEFAULT NULL COMMENT '电子邮箱 - 用户的邮箱地址',
    `sys_user_sex` INT NULL DEFAULT NULL COMMENT '用户性别 - 0:女, 1:男, 2:其他',
    `sys_user_full_address` varchar(400)  NULL DEFAULT NULL COMMENT '居住地址 - 用户的详细地址',
    `sys_user_in_system` INT NULL DEFAULT NULL COMMENT '是否系统账号 - 0:普通账号, 1:系统内置账号(不可删除)',
    `sys_user_avatar` text  NULL COMMENT '用户头像 - 头像图片的URL或Base64',
    `sys_user_card` varchar(255)  NULL DEFAULT NULL COMMENT '身份证号 - 用户的身份证号码',
    `sys_dept_id` INT NULL DEFAULT NULL COMMENT '所属部门ID - 关联组织机构表',
    `sys_user_remark` varchar(255)  NULL DEFAULT NULL COMMENT '用户备注 - 附加说明信息',
    `sys_user_last_address` varchar(255)  NULL DEFAULT NULL COMMENT '最后登录地点 - 最近一次登录的地理位置',
    `sys_user_register_address` varchar(255)  NULL DEFAULT NULL COMMENT '注册地点 - 用户注册时的地理位置',
    `sys_user_last_ip` varchar(255)  NULL DEFAULT NULL COMMENT '最后登录IP - 最近一次登录的IP地址',
    `sys_user_last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间 - 最近一次登录的时间',
    `sys_user_register_ip` varchar(255)  NULL DEFAULT NULL COMMENT '注册IP - 用户注册时的IP地址',
    `sys_user_login_count` INT NULL DEFAULT NULL COMMENT '登录次数 - 累计登录次数',
    `sys_client_id` varchar(255)  NULL DEFAULT NULL COMMENT '客户端ID - 绑定的客户端标识',
    `sys_user_totp_code` varchar(600)  NULL DEFAULT NULL COMMENT 'TOTP密钥 - 两步验证的加密密钥(Base32编码)',
    `sys_user_birthday` varchar(20)  NULL DEFAULT NULL COMMENT '生日 - 用户出生日期，用于生日头像框等个性化功能',
    `sys_user_avatar_frame` INT NULL DEFAULT NULL COMMENT '头像框ID - 用户选择的头像框样式ID，0为默认',
    `sys_user_avatar_frame_show` INT NULL DEFAULT 0 COMMENT '是否显示头像框 - 0:不显示 1:显示',
    `create_name` varchar(255)  NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` INT NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255)  NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` INT NULL DEFAULT NULL COMMENT '更新人ID',
    `sys_user_password_update_time` datetime NULL DEFAULT NULL COMMENT '密码修改时间',
    `sys_user_must_change_password` int NULL DEFAULT 0 COMMENT '是否强制修改密码: 0-否 1-是',
    `sys_user_email_verified` INT NULL DEFAULT 0 COMMENT '邮箱是否已验证: 0-未验证 1-已验证',
    `sys_user_email_verify_time` datetime NULL DEFAULT NULL COMMENT '邮箱验证通过时间',
    PRIMARY KEY (`sys_user_id`),
    UNIQUE INDEX `sys_user_uk_username` (`sys_user_username`, `sys_user_delete`),
    INDEX `sys_user_idx_dept_id` (`sys_dept_id`),
    INDEX `sys_user_idx_phone` (`sys_user_phone`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '用户表 - 存储系统用户账号基本信息';

-- =====================================================
-- 角色-菜单关系表 (sys_role_menu)
-- 说明: 归属 spring-api-support-common-system-starter，与 SysRoleMenuMapper 同模块；
--       V1.0__initdata_all_api_system.sql 存在 sys_role_menu 种子数据，
--       缺表会导致启动期 "Table SYS_ROLE_MENU not found" 而整轮初始化中断
-- =====================================================

CREATE TABLE IF NOT EXISTS `sys_role_menu` (
    `sys_role_menu_id` INT NOT NULL AUTO_INCREMENT COMMENT '关联ID - 自增主键',
    `sys_role_id` INT NULL DEFAULT NULL COMMENT '角色ID - 关联角色表',
    `sys_menu_id` INT NULL DEFAULT NULL COMMENT '菜单ID - 关联菜单表',
    PRIMARY KEY (`sys_role_menu_id`),
    INDEX `sys_role_menu_idx_role_id` (`sys_role_id`),
    INDEX `sys_role_menu_idx_menu_id` (`sys_menu_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COMMENT = '角色-菜单关系表 - 存储角色拥有的菜单权限';

CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `sys_user_role_id` INT NOT NULL AUTO_INCREMENT COMMENT '关系ID - 主键自增',
    `sys_user_id` INT NULL DEFAULT NULL COMMENT '用户ID - 关联用户表',
    `sys_role_id` INT NULL DEFAULT NULL COMMENT '角色ID - 关联角色表',
    PRIMARY KEY (`sys_user_role_id`),
    INDEX `sys_user_role_idx_user_id` (`sys_user_id`),
    INDEX `sys_user_role_idx_role_id` (`sys_role_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '用户-角色关系表 - 存储用户与角色的多对多映射关系';

-- =====================================================
-- 用户登录日志表 (sys_user_log)
-- 来源: 建表自 spring-api-support-system-starter 下沉。
-- 本模块承载 SysUserLogMapper 与 SysUserLogServiceImpl（登录/登出审计写入），
-- 建表必须随代码归口；否则只嵌入本模块的宿主（如采集调度平台）每次登录都抛
-- BadSqlGrammarException: Table "SYS_USER_LOG" not found。
-- =====================================================

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

-- =====================================================================
-- 以下表的 Mapper / Service / Controller 位于 spring-api-support-common-system-starter，
-- 建表语句随 Mapper 所在模块归置，避免只引入本模块时缺表。
-- =====================================================================

CREATE TABLE IF NOT EXISTS `sys_announcement_read` (
    `sys_announcement_read_id` bigint NOT NULL AUTO_INCREMENT,
    `sys_announcement_id` int NULL DEFAULT NULL,
    `sys_user_id` int NULL DEFAULT NULL,
    `sys_user_name` varchar(255) NULL DEFAULT NULL COMMENT '阅读人姓名 - 冗余存储，避免列表查询关联用户表',
    `read_time` datetime NULL DEFAULT NULL,
    `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_announcement_read_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci;

-- ---------------------------------------------------------------------
-- sys_policy_config：统一策略配置表（取代 sys_ip_blackwhite / sys_rate_limit_rule /
-- sys_account_blackwhite 三张表）。一行 = 一条策略配置。
--
-- 存储约定：type 判别列 + 通用宽列 + config JSON 兜底。
--   type      策略类型，取值见 Java 侧 PolicyConfigType 常量
--   biz_key   业务键：IP 值 / 账号 / dimension:target / 灰度键 / 功能键
--   dimension 限流维度 interface/ip/account；其余 type 统一填空串 ''（不可为 NULL，
--             否则 MySQL 唯一索引把 NULL 视为互不相等，唯一约束会失效）
--   list_type 名单方向 1黑名单 2白名单；非名单类统一填 0
--   config    类型特有字段的 JSON 兜底（灰度权重、命中策略、扩展参数等）
--
-- 唯一键取 (type, dimension, list_type, biz_key)：限流靠 dimension 区分同 target 的不同维度，
-- 黑白名单靠 list_type 允许同一 IP/账号同时存在于黑名单与白名单。
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_policy_config` (
    `sys_policy_config_id` bigint NOT NULL AUTO_INCREMENT COMMENT '统一策略配置ID',
    `type` varchar(32) NOT NULL COMMENT '策略类型: IP/RATE_LIMIT/ACCOUNT/GRAY/FEATURE',
    `dimension` varchar(20) NOT NULL DEFAULT '' COMMENT '限流维度: interface/ip/account; 其它类型填空串',
    `list_type` int NOT NULL DEFAULT 0 COMMENT '名单方向: 1黑名单 2白名单; 其它类型填0',
    `biz_key` varchar(255) NOT NULL COMMENT '业务键: IP值/账号/dimension:target/灰度键/功能键',
    `biz_value` varchar(1000) NULL DEFAULT NULL COMMENT '宽列值: 灰度目标/命中对象等',
    `status` int NOT NULL DEFAULT 1 COMMENT '状态: 1启用 0禁用',
    `qps` int NULL DEFAULT NULL COMMENT 'QPS: 仅 RATE_LIMIT 使用',
    `burst_size` int NULL DEFAULT NULL COMMENT '突发容量: 仅 RATE_LIMIT 使用',
    `weight` int NULL DEFAULT NULL COMMENT '权重/百分比: 仅 GRAY 使用',
    `sort` int NOT NULL DEFAULT 0 COMMENT '排序值, 越小越靠前',
    `config` text NULL COMMENT '类型特有配置(JSON)',
    `remark` varchar(1000) NULL DEFAULT NULL COMMENT '备注',
    `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`sys_policy_config_id`),
    -- 不写前缀长度 biz_key(191)：H2 的解析器不认识 MySQL 的前缀索引语法，
    -- 会直接抛 parseIndexColumnList 导致整份 init 脚本建表中止。
    -- 前缀长度本是 MySQL 5.6 的索引长度变通，MySQL 8 默认 DYNAMIC 行格式下
    -- 唯一索引上限 3072 字节，本键四列合计约 1232 字节，整列索引足够。
    -- 去掉后 MySQL 8 与 H2 都能建出这张表。
    UNIQUE KEY `sys_policy_config_uk_biz` (`type`, `dimension`, `list_type`, `biz_key`),
    KEY `sys_policy_config_idx_type_status` (`type`, `status`),
    KEY `sys_policy_config_idx_type_sort` (`type`, `sort`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '统一策略配置表';

-- ---------------------------------------------------------------------
-- 【遗留表 · 仅供一次性数据迁移使用】
--
-- 下面三张表已被 sys_policy_config 取代。之所以仍在此建表，是因为 Flyway 的 init 与
-- initdata 是两个独立通道：init 跑完才开始跑 initdata，而「搬数据」必须发生在
-- 「删表」之前。若 init 不建这三张表，initdata 里的 INSERT..SELECT 会因表不存在而报错，
-- 且无法在纯 SQL 里做条件执行。
--
-- 因此：init 建空表 → initdata 搬数据 → initdata 末尾 DROP TABLE IF EXISTS。
-- 全新库最终不会残留这三张表；历史库数据会被完整搬进 sys_policy_config。
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_ip_blackwhite` (
    `sys_ip_id` int NOT NULL AUTO_INCREMENT,
    `sys_ip_type` int NULL DEFAULT NULL,
    `sys_ip_ip` varchar(255) NULL DEFAULT NULL,
    `sys_ip_status` int NULL DEFAULT NULL,
    `sys_ip_remark` varchar(1000) NULL DEFAULT NULL,
    `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_ip_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '遗留表, 迁移后删除';

CREATE TABLE IF NOT EXISTS `sys_rate_limit_rule` (
    `sys_rate_limit_rule_id` bigint NOT NULL AUTO_INCREMENT COMMENT '限流规则ID',
    `dimension` varchar(20) NOT NULL COMMENT '限流维度: interface/ip/account',
    `target` varchar(500) NOT NULL COMMENT '匹配目标: 接口路径/IP/账号',
    `qps` int NULL DEFAULT 100 COMMENT '每秒允许请求数(QPS)',
    `burst_size` int NULL DEFAULT 200 COMMENT '突发容量',
    `enabled` tinyint NULL DEFAULT 1 COMMENT '是否启用: 1启用 0禁用',
    `remark` varchar(255) NULL DEFAULT NULL COMMENT '备注',
    `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`sys_rate_limit_rule_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '遗留表, 迁移后删除';

CREATE TABLE IF NOT EXISTS `sys_account_blackwhite` (
    `sys_account_bw_id` bigint NOT NULL AUTO_INCREMENT COMMENT '账号黑白名单ID',
    `account` varchar(100) NOT NULL COMMENT '账号',
    `type` int NOT NULL COMMENT '类型: 1黑名单 2白名单',
    `status` int NULL DEFAULT 1 COMMENT '状态: 1启用 0禁用',
    `remark` varchar(255) NULL DEFAULT NULL COMMENT '备注',
    `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`sys_account_bw_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '遗留表, 迁移后删除';

CREATE TABLE IF NOT EXISTS `sys_message` (
    `sys_message_id` int NOT NULL AUTO_INCREMENT,
    `sys_message_title` varchar(255) NULL DEFAULT NULL,
    `sys_message_content` varchar(1000) NULL DEFAULT NULL,
    `sys_message_type` varchar(255) NULL DEFAULT NULL,
    `sys_message_level` varchar(255) NULL DEFAULT NULL,
    `sys_message_channel` varchar(32) NULL DEFAULT 'PLATFORM' COMMENT '渠道类型: PLATFORM/EMAIL/SMS',
    `sys_message_sender_id` int NULL DEFAULT NULL,
    `sys_message_sender_name` varchar(255) NULL DEFAULT NULL,
    `sys_message_receiver_id` int NULL DEFAULT NULL,
    `sys_message_receiver_name` varchar(255) NULL DEFAULT NULL,
    `sys_message_read` int NULL DEFAULT NULL,
    `sys_message_read_time` datetime NULL DEFAULT NULL,
    `sys_message_send_time` datetime NULL DEFAULT NULL,
    `sys_message_expire_time` datetime NULL DEFAULT NULL,
    `sys_message_biz_type` varchar(255) NULL DEFAULT NULL,
    `sys_message_biz_id` varchar(255) NULL DEFAULT NULL,
    `sys_message_url` varchar(255) NULL DEFAULT NULL,
    `sys_message_extra` varchar(1000) NULL DEFAULT NULL,
    `sys_message_status` int NULL DEFAULT NULL,
    `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_message_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `sys_message_recipient` (
    `sys_message_recipient_id` bigint NOT NULL AUTO_INCREMENT,
    `sys_message_id` bigint NULL DEFAULT NULL COMMENT '关联站内消息ID',
    `recipient_type` varchar(16) NULL DEFAULT NULL COMMENT '接收人类型: USER/EMAIL/PHONE',
    `recipient_address` varchar(255) NULL DEFAULT NULL COMMENT '接收人地址',
    `delivery_status` varchar(16) NULL DEFAULT NULL COMMENT '送达状态: SENT/FAILED',
    `read_status` varchar(16) NULL DEFAULT 'UNREAD' COMMENT '已读状态: READ/UNREAD',
    `read_time` datetime NULL DEFAULT NULL COMMENT '阅读时间',
    `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_message_recipient_id`),
    KEY `idx_message_recipient_msg` (`sys_message_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `sys_post` (
    `sys_post_id` bigint NOT NULL AUTO_INCREMENT,
    `sys_post_name` varchar(255) NULL DEFAULT NULL,
    `sys_post_code` varchar(255) NULL DEFAULT NULL,
    `sys_post_sort` int NULL DEFAULT NULL,
    `sys_post_status` int NULL DEFAULT NULL,
    `remark` varchar(1000) NULL DEFAULT NULL,
    `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_post_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `sys_user_post` (
    `sys_user_post_id` bigint NOT NULL AUTO_INCREMENT,
    `sys_user_id` int NULL DEFAULT NULL,
    `sys_post_id` bigint NULL DEFAULT NULL,
    PRIMARY KEY (`sys_user_post_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci;

-- =====================================================================
-- 以下表的 Mapper 位于本模块，建表语句随 Mapper 归置。
-- =====================================================================

CREATE TABLE IF NOT EXISTS `sys_user_account` (
    `sys_account_id` int NOT NULL AUTO_INCREMENT,
    `sys_account_type` varchar(255) NULL DEFAULT NULL,
    `sys_account_openid` varchar(255) NULL DEFAULT NULL,
    `sys_account_unionid` varchar(255) NULL DEFAULT NULL,
    `sys_account_name` varchar(255) NULL DEFAULT NULL,
    `sys_account_avatar` varchar(255) NULL DEFAULT NULL,
    `sys_account_phone` varchar(255) NULL DEFAULT NULL,
    `sys_account_email` varchar(255) NULL DEFAULT NULL,
    `sys_account_forbidden` tinyint NULL DEFAULT NULL,
    `sys_account_login_count` int NULL DEFAULT NULL,
    `sys_account_last_ip` varchar(255) NULL DEFAULT NULL,
    `sys_account_last_ip_address` varchar(255) NULL DEFAULT NULL,
    `sys_account_user_id` int NULL DEFAULT NULL,
    `create_name` varchar(255) NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_account_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci;

-- =====================================================================
-- 以下表的 Mapper / Service / 定时任务位于 spring-api-support-common-system-starter，
-- 建表语句随代码归置；否则只嵌入本模块的宿主会因缺表直接失败。
-- =====================================================================

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
    -- SysMenu.sysMenuRemind 映射本列；此前只在实体里有、DDL 里漏了，
    -- 导致按实体字段全量 select 时报 Column "SYS_MENU_REMIND" not found，
    -- 菜单接口直接 500。以 init 脚本为唯一建表来源后必须补上。
    `sys_menu_remind` INT NULL DEFAULT 0 COMMENT '是否脱离菜单控制 - 0:作为菜单处理, 1:只注册路由不作菜单',
    `sys_menu_position` varchar(20) NOT NULL DEFAULT 'standard' COMMENT '菜单位置 - standard:标准(侧边栏) top:顶部(顶栏按钮, 强制 remind=1)',
    `sys_menu_show_type` varchar(20) NOT NULL DEFAULT 'ALWAYS' COMMENT '动态显示条件类型: ALWAYS/CLASS/SPEL/SWITCH/PROPERTY',
    `sys_menu_show_value` varchar(500) NULL DEFAULT NULL COMMENT '条件值: 类全名/SpEL表达式/配置键',
    `sys_menu_show_switch` INT NULL DEFAULT 1 COMMENT '开关型条件放行值: 0否 1是',
    `sys_menu_show_key` varchar(255) NULL DEFAULT NULL COMMENT '开关型条件联动的功能键(sys_policy_config type=FEATURE 的 biz_key)',
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

-- ---------------------------------------------------------------------
-- 动态显示条件四列（sys_menu_show_type / _value / _switch / _key）由上面的
-- CREATE TABLE IF NOT EXISTS `sys_menu` 一次性建出，本文件不写任何
-- ALTER TABLE ... ADD COLUMN。
--
-- 为什么没有兼容 ALTER：开发期数据库是截断重建的，initdata 跑完即全新态，
-- Flyway 也按 sys_database_version 跳过已应用的 V1.0，写 ALTER 只会成为
-- 永不执行的死代码，还会破坏「init 只含建表」这一不变量。
--
-- 改表结构的正确做法：直接改这里的 CREATE TABLE，删库重建；
-- 不要新增 V1.x__add_*.sql 增量脚本。
-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS `sys_mini_menu` (
    `sys_mini_menu_id` INT NOT NULL AUTO_INCREMENT COMMENT '小程序菜单ID - 主键自增',
    `sys_mini_menu_title` varchar(255)  NULL DEFAULT NULL COMMENT '卡片标题',
    `sys_mini_menu_subtitle` varchar(255)  NULL DEFAULT NULL COMMENT '卡片副标题',
    `sys_mini_menu_category` varchar(128)  NULL DEFAULT NULL COMMENT '卡片分组',
    `sys_mini_menu_path` varchar(255)  NULL DEFAULT NULL COMMENT '小程序页面路径',
    `sys_mini_menu_jump_mode` INT NULL DEFAULT 0 COMMENT '跳转模式 - 0:普通页面, 1:tabBar页面, 2:webview',
    `sys_mini_menu_icon` varchar(255)  NULL DEFAULT NULL COMMENT '卡片图标',
    `sys_mini_menu_cover` varchar(255)  NULL DEFAULT NULL COMMENT '卡片封面/背景',
    `sys_mini_menu_badge` varchar(64)  NULL DEFAULT NULL COMMENT '角标文案',
    `sys_mini_menu_badge_type` varchar(64)  NULL DEFAULT NULL COMMENT '角标样式',
    `sys_mini_menu_perm` varchar(255)  NULL DEFAULT NULL COMMENT '权限标识',
    `sys_mini_menu_role` varchar(255)  NULL DEFAULT NULL COMMENT '所属角色编码',
    `sys_mini_menu_sort` INT NULL DEFAULT 1 COMMENT '排序优先级',
    `sys_mini_menu_keep_alive` INT NULL DEFAULT 0 COMMENT '是否缓存 - 0:否, 1:是',
    `sys_mini_menu_hidden` INT NULL DEFAULT 0 COMMENT '是否隐藏 - 0:显示, 1:隐藏',
    `sys_mini_menu_description` varchar(255)  NULL DEFAULT NULL COMMENT '卡片说明',
    `create_name` varchar(255)  NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` INT NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255)  NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` INT NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_mini_menu_id`),
    INDEX `sys_mini_menu_idx_mini_menu_sort` (`sys_mini_menu_sort`),
    INDEX `sys_mini_menu_idx_mini_menu_perm` (`sys_mini_menu_perm`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '小程序菜单卡片表 - 存储小程序首页卡片与权限配置';

CREATE TABLE IF NOT EXISTS `sys_feedback` (
    `sys_feedback_id` INT NOT NULL AUTO_INCREMENT COMMENT '反馈ID - 主键自增',
    `sys_feedback_type` varchar(255)  NULL DEFAULT NULL COMMENT '反馈类型 - 如BUG、SUGGESTION、OTHER',
    `sys_feedback_images` text  NULL COMMENT '反馈图片 - 多个图片地址逗号分隔',
    `sys_feedback_content` text  NULL COMMENT '反馈内容 - 用户提交的问题描述或建议',
    `sys_feedback_status` INT NULL DEFAULT 0 COMMENT '反馈状态 - 0:未处理, 1:已处理',
    `sys_feedback_deal_by` INT NULL DEFAULT NULL COMMENT '处理人ID - 关联系统用户',
    `sys_feedback_deal_name` varchar(255)  NULL DEFAULT NULL COMMENT '处理人名称',
    `sys_feedback_recover_content` text  NULL COMMENT '回复内容 - 管理员处理回复',
    `sys_feedback_recover_time` datetime NULL DEFAULT NULL COMMENT '回复时间 - 处理反馈的时间',
`sys_feedback_recover_images` text  NULL COMMENT '回复图片 - 管理员回复附图',
    `sys_feedback_user_agent` varchar(500)  NULL DEFAULT NULL COMMENT '客户端类型(UserAgent)',
    `sys_feedback_platform` varchar(50)  NULL DEFAULT NULL COMMENT '平台类型(预留)',
    `create_name` varchar(255)  NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` INT NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255)  NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` INT NULL DEFAULT NULL COMMENT '更新人ID',
    `sys_feedback_priority` int NULL DEFAULT 1 COMMENT '优先级: 0-低 1-中 2-高 3-紧急',
    PRIMARY KEY (`sys_feedback_id`),
    INDEX `sys_feedback_idx_feedback_type` (`sys_feedback_type`),
    INDEX `sys_feedback_idx_feedback_status` (`sys_feedback_status`),
    INDEX `sys_feedback_idx_feedback_create_time` (`create_time`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '系统反馈表 - 存储帮助与反馈和异常上报信息';

CREATE TABLE IF NOT EXISTS `sys_sms_log` (
    `sys_sms_log_id` int NOT NULL AUTO_INCREMENT COMMENT '日志ID - 主键自增',
    `sys_sms_log_target` varchar(255)  NULL DEFAULT NULL COMMENT '接收人 - 手机号码',
    `sys_sms_log_template_id` varchar(255)  NULL DEFAULT NULL COMMENT '模板ID - 使用的模板编码',
    `sys_sms_log_param` varchar(255)  NULL DEFAULT NULL COMMENT '模板参数 - JSON格式的模板变量',
    `sys_sms_log_content` longtext  NULL COMMENT '发送内容 - 最终发送的短信内容',
    `sys_sms_cost` decimal(10, 2) NULL DEFAULT NULL COMMENT '耗时 - 发送耗时(毫秒)',
    `sys_sms_code` varchar(255)  NULL DEFAULT NULL COMMENT '返回码 - 短信平台返回的状态码',
    `sys_sms_message` text  NULL COMMENT '返回消息 - 短信平台返回的消息',
    `sys_sms_app_type` varchar(255)  NULL DEFAULT NULL COMMENT '短信厂家 - 使用的短信服务商',
    `sys_sms_app_id` varchar(255)  NULL DEFAULT NULL COMMENT '短信AppID',
    `sys_sms_app_sign` varchar(255)  NULL DEFAULT NULL COMMENT '短信签名',
    `sys_project_id` int NULL DEFAULT NULL COMMENT '项目ID - 关联sys_project表',
    `create_name` varchar(255)  NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255)  NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_sms_log_id`),
    INDEX `sys_sms_log_idx_target` (`sys_sms_log_target`),
    INDEX `sys_sms_log_idx_project_id` (`sys_project_id`),
    INDEX `sys_sms_log_idx_create_time` (`create_time`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '短信日志表 - 记录短信发送历史';

CREATE TABLE IF NOT EXISTS `sys_wechat_user` (
    `sys_wechat_user_id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID - 主键自增',
    `sys_wechat_open_id` varchar(255)  NULL DEFAULT NULL COMMENT '微信 openid - 微信登录的OpenID',
    `sys_wechat_union_id` varchar(255)  NULL DEFAULT NULL COMMENT '微信 unionid - 微信开放平台的UnionID',
    `sys_wechat_nickname` varchar(255)  NULL DEFAULT NULL COMMENT '昵称 - 微信用户昵称',
    `sys_wechat_avatar` varchar(4000)  NULL DEFAULT NULL COMMENT '头像 - 微信用户头像URL',
    `sys_user_id` INT NULL DEFAULT NULL COMMENT '绑定的系统用户ID - 关联用户表',
    `create_name` varchar(255)  NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` INT NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255)  NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` INT NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_wechat_user_id`),
    INDEX `sys_wechat_user_idx_user_id` (`sys_user_id`),
    INDEX `sys_wechat_user_idx_open_id` (`sys_wechat_open_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '微信用户表 - 存储通过微信体系接入的用户基础信息';

CREATE TABLE IF NOT EXISTS `sys_announcement` (
    `sys_announcement_id` INT NOT NULL AUTO_INCREMENT COMMENT '公告ID - 主键自增',
    `sys_announcement_title` varchar(200)  NOT NULL COMMENT '公告标题',
    `sys_announcement_content` text  NULL COMMENT '公告内容(HTML) - 支持富文本',
    `sys_announcement_level` varchar(20)  NULL DEFAULT 'info' COMMENT '级别 - info:信息 warning:警告 important:重要 emergency:紧急',
    `sys_announcement_position` varchar(20)  NULL DEFAULT 'default' COMMENT '展示位置 - default:Tabs下方 fixed-top:固定顶部',
    `sys_announcement_type` varchar(20)  NULL DEFAULT 'global' COMMENT '类型 - global:全局 role:指定角色',
    `sys_announcement_target_roles` varchar(1000)  NULL DEFAULT NULL COMMENT '目标角色ID列表(JSON数组) - type=role时生效',
    `sys_announcement_status` INT NULL DEFAULT 0 COMMENT '状态 - 0:草稿 1:已发布 2:已下架',
    `sys_announcement_sort` INT NULL DEFAULT 0 COMMENT '排序权重 - 越大越靠前',
    `sys_announcement_publish_time` datetime NULL DEFAULT NULL COMMENT '发布时间',
    `sys_announcement_expire_time` datetime NULL DEFAULT NULL COMMENT '过期时间 - 空=永不过期',
    `sys_announcement_target_url` varchar(500)  NULL DEFAULT NULL COMMENT '跳转链接 - 点击公告跳转',
    `sys_announcement_publisher_name` varchar(100)  NULL DEFAULT NULL COMMENT '发布人',
    `create_name` varchar(255)  NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` INT NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255)  NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` INT NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_announcement_id`),
    INDEX `sys_announcement_idx_announcement_status` (`sys_announcement_status`),
    INDEX `sys_announcement_idx_announcement_publish_time` (`sys_announcement_publish_time`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '系统公告表 - 存储系统公告和通知';

CREATE TABLE IF NOT EXISTS `sys_avatar_frame` (
    `sys_avatar_frame_id` INT NOT NULL AUTO_INCREMENT COMMENT '头像框ID',
    `sys_avatar_frame_name` varchar(100) NOT NULL COMMENT '头像框名称',
    `sys_avatar_frame_type` varchar(50) DEFAULT 'base' COMMENT '类型: base-基础 birthday-生日 event-活动',
    `sys_avatar_frame_image` varchar(500) DEFAULT NULL COMMENT '头像框图片URL',
    `sys_avatar_frame_color` varchar(200) DEFAULT NULL COMMENT '渐变背景色(css)',
    `sys_avatar_frame_desc` varchar(500) DEFAULT NULL COMMENT '描述',
    `sys_avatar_frame_acquire` varchar(50) DEFAULT 'default' COMMENT '获取途径: default-默认 birthday-生日 achievement-成就',
    `sys_avatar_frame_sort` INT DEFAULT 0 COMMENT '排序',
    `sys_avatar_frame_status` INT DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    `create_name` varchar(255) DEFAULT NULL COMMENT '创建人姓名',
    `create_by` INT DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) DEFAULT NULL COMMENT '更新人姓名',
    `update_by` INT DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`sys_avatar_frame_id`),
    KEY `sys_avatar_frame_idx_type` (`sys_avatar_frame_type`),
    KEY `sys_avatar_frame_idx_status` (`sys_avatar_frame_status`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='头像框定义表';
