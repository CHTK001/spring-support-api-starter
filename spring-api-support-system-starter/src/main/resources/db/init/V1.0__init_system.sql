/*
 * 系统管理模块 - 数据库初始化脚本
 * 
 * 包含表:
 *   - sys_dept: 组织机构表
 *   - sys_role: 角色表
 *   - sys_role_menu: 角色-菜单关系表
 *   - sys_menu: 菜单表
 *   - sys_user: 用户表
 *   - sys_user_role: 用户-角色关系表
 *   - sys_user_config: 用户配置表(第三方绑定)
 *   - sys_user_layout: 用户布局配置表
 *   - sys_user_log: 用户登录日志表
 *   - sys_log: 系统操作日志表
 *   - sys_sfc: 组件库表
 *   - sys_sfc_install: 用户安装组件表
 *
 * @author System
 * @since 2025-01-01
 * @version 1.0
 */

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 1. 组织机构表 (sys_dept)
-- 用途: 存储企业/系统的组织架构信息，支持树形结构
-- =====================================================
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept` (
    `sys_dept_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '机构ID - 主键自增',
    `sys_dept_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '机构名称 - 组织/部门的显示名称',
    `sys_dept_pid` int(11) NULL DEFAULT NULL COMMENT '父机构ID - 上级组织的ID，顶级为0',
    `sys_dept_tree_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '机构链路 - 从根节点到当前节点的ID路径，如"0,1,2"',
    `sys_dept_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组织编码 - 机构的唯一业务编码',
    `sys_dept_principal` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '部门负责人 - 该部门的负责人姓名',
    `sys_dept_contact` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '负责人联系方式 - 负责人的电话或其他联系方式',
    `sys_dept_sort` int(11) NULL DEFAULT NULL COMMENT '排序优先级 - 数值越小越靠前',
    `sys_dept_status` int(11) NULL DEFAULT NULL COMMENT '状态 - 0:启用, 1:禁用',
    `sys_dept_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注 - 附加说明信息',
    `sys_dept_data_permission` int(11) NULL DEFAULT NULL COMMENT '数据权限类型 - 1:全部可见, 2:本人可见, 3:所在部门可见, 4:所在部门及子级可见, 5:选择的部门可见',
    `sys_dept_data_permission_dept_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据权限可见部门ID列表 - 当数据权限类型为5时，存储可见的部门ID，逗号分隔',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_dept_id`) USING BTREE,
    INDEX `idx_dept_pid` (`sys_dept_pid`) USING BTREE COMMENT '父机构索引',
    INDEX `idx_dept_code` (`sys_dept_code`) USING BTREE COMMENT '机构编码索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '组织机构表 - 存储系统的部门/组织架构信息' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 2. 角色表 (sys_role)
-- 用途: 存储系统角色信息，用于权限控制
-- =====================================================
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `sys_role_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '角色ID - 主键自增',
    `sys_role_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色名称 - 角色的显示名称，如"管理员"',
    `sys_role_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色编码 - 角色的唯一标识码，如"ADMIN"',
    `sys_role_in_system` int(11) NULL DEFAULT NULL COMMENT '是否系统角色 - 0:非系统角色, 1:系统内置角色(不可删除)',
    `sys_role_status` int(11) NULL DEFAULT NULL COMMENT '是否启用 - 0:禁用, 1:启用',
    `sys_role_sort` int(11) NULL DEFAULT NULL COMMENT '排序优先级 - 数值越小越靠前',
    `sys_role_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注 - 角色的说明描述',
    `sys_role_hidden` int(11) NULL DEFAULT 0 COMMENT '是否隐藏 - 0:显示, 1:隐藏(前端不展示)',
    `sys_role_readable` int(11) NULL DEFAULT NULL COMMENT '角色可读权限位 - 二进制位表示的读权限',
    `sys_role_writeable` int(11) NULL DEFAULT NULL COMMENT '角色可写权限位 - 二进制位表示的写权限',
    `sys_role_executable` int(11) NULL DEFAULT NULL COMMENT '角色可执行权限位 - 二进制位表示的执行权限',
    `sys_role_board_card` int(11) NULL DEFAULT NULL COMMENT '角色可看板类型 - 该角色可查看的看板类型',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_role_id`) USING BTREE,
    UNIQUE INDEX `uk_role_code` (`sys_role_code`) USING BTREE COMMENT '角色编码唯一索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色表 - 存储系统角色信息，用于RBAC权限控制' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 3. 菜单表 (sys_menu)
-- 用途: 存储系统菜单/权限点信息，支持树形结构
-- =====================================================
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
    `sys_menu_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '菜单ID - 主键自增',
    `sys_menu_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单名称(路由名) - 用于路由匹配的唯一名称',
    `sys_menu_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单路径 - 前端路由路径，如"/manage/user"',
    `sys_menu_i18n` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '国际化key - 用于多语言显示',
    `sys_menu_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单标题 - 显示在界面上的名称',
    `sys_menu_perm` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '权限标识 - 如"sys:user:add"',
    `sys_menu_pid` int(11) NULL DEFAULT NULL COMMENT '父菜单ID - 上级菜单的ID，顶级为0',
    `sys_menu_tree_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单路径链 - 从根节点到当前节点的ID路径',
    `sys_menu_component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单组件路径 - Vue组件的路径，如"/manage/user/index"',
    `sys_menu_icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单图标 - 图标名称，如"ep:user"',
    `sys_menu_extra_icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '右侧额外图标 - 菜单右侧显示的图标',
    `sys_menu_type` int(11) NULL DEFAULT NULL COMMENT '菜单类型 - 0:菜单, 1:iframe, 2:外链, 3:按钮',
    `sys_menu_sort` int(11) NULL DEFAULT NULL COMMENT '排序优先级 - 数值越小越靠前',
    `sys_menu_hidden` int(11) NULL DEFAULT NULL COMMENT '是否隐藏 - 0:显示, 1:隐藏',
    `sys_menu_show_parent` int(11) NULL DEFAULT NULL COMMENT '是否显示父级菜单 - 0:显示, 1:隐藏父级',
    `sys_menu_hidden_tag` int(11) NULL DEFAULT NULL COMMENT '是否在标签栏隐藏 - 0:显示, 1:隐藏',
    `sys_menu_fixed_tag` int(11) NULL DEFAULT NULL COMMENT '是否固定标签 - 0:不固定, 1:固定在标签栏',
    `sys_menu_redirect` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '路由重定向地址 - 访问该菜单时跳转的地址',
    `sys_menu_enter_transition` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '进场动画 - 页面进入时的动画效果',
    `sys_menu_leave_transition` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '离场动画 - 页面离开时的动画效果',
    `sys_menu_active_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单激活路径 - 当匹配该路径时激活此菜单',
    `sys_menu_frame_src` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '内嵌链接地址 - iframe或外链的URL',
    `sys_menu_keep_alive` int(11) NULL DEFAULT NULL COMMENT '是否缓存页面 - 0:不缓存, 1:缓存(keep-alive)',
    `sys_menu_role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属角色编码 - 可访问该菜单的角色编码，逗号分隔',
    `sys_menu_home` int(11) NULL DEFAULT NULL COMMENT '是否只显示在主页 - 0:否, 1:是',
    `sys_menu_engine` int(11) NULL DEFAULT NULL COMMENT '是否engine布局主页 - 0:否, 1:是',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_menu_id` DESC) USING BTREE,
    INDEX `idx_menu_pid` (`sys_menu_pid`) USING BTREE COMMENT '父菜单索引',
    INDEX `idx_menu_perm` (`sys_menu_perm`) USING BTREE COMMENT '权限标识索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜单表 - 存储系统菜单和按钮权限信息' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 4. 角色-菜单关系表 (sys_role_menu)
-- 用途: 存储角色与菜单的多对多关系
-- =====================================================
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
    `sys_role_menu_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '关系ID - 主键自增',
    `sys_role_id` int(11) NULL DEFAULT NULL COMMENT '角色ID - 关联角色表',
    `sys_menu_id` int(11) NULL DEFAULT NULL COMMENT '菜单ID - 关联菜单表',
    PRIMARY KEY (`sys_role_menu_id`) USING BTREE,
    INDEX `idx_role_id` (`sys_role_id`) USING BTREE COMMENT '角色ID索引',
    INDEX `idx_menu_id` (`sys_menu_id`) USING BTREE COMMENT '菜单ID索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色-菜单关系表 - 存储角色与菜单的多对多映射关系' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 5. 用户表 (sys_user)
-- 用途: 存储系统用户账号信息
-- =====================================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `sys_user_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '用户ID - 主键自增',
    `sys_user_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户账号 - 登录使用的唯一账号名',
    `sys_user_nickname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户昵称 - 显示名称',
    `sys_user_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户密码 - 加密后的密码',
    `sys_user_salt` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码盐值 - 用于密码加密的随机字符串',
    `sys_user_delete` int(11) NULL DEFAULT NULL COMMENT '是否删除 - 0:未删除, 1:已删除(逻辑删除)',
    `sys_user_status` int(11) NULL DEFAULT NULL COMMENT '用户状态 - 0:禁用, 1:启用',
    `sys_user_phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号码 - 用户的手机号',
    `sys_user_email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '电子邮箱 - 用户的邮箱地址',
    `sys_user_sex` int(11) NULL DEFAULT NULL COMMENT '用户性别 - 0:女, 1:男, 2:其他',
    `sys_user_full_address` varchar(400) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '居住地址 - 用户的详细地址',
    `sys_user_in_system` int(11) NULL DEFAULT NULL COMMENT '是否系统账号 - 0:普通账号, 1:系统内置账号(不可删除)',
    `sys_user_avatar` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '用户头像 - 头像图片的URL或Base64',
    `sys_user_card` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证号 - 用户的身份证号码',
    `sys_dept_id` int(11) NULL DEFAULT NULL COMMENT '所属部门ID - 关联组织机构表',
    `sys_user_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户备注 - 附加说明信息',
    `sys_user_last_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后登录地点 - 最近一次登录的地理位置',
    `sys_user_register_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '注册地点 - 用户注册时的地理位置',
    `sys_user_last_ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后登录IP - 最近一次登录的IP地址',
    `sys_user_register_ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '注册IP - 用户注册时的IP地址',
    `sys_user_login_count` int(11) NULL DEFAULT NULL COMMENT '登录次数 - 累计登录次数',
    `sys_client_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户端ID - 绑定的客户端标识',
    `sys_user_totp_code` varchar(600) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'TOTP密钥 - 两步验证的加密密钥(Base32编码)',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_user_id`) USING BTREE,
    UNIQUE INDEX `uk_username` (`sys_user_username`) USING BTREE COMMENT '用户名唯一索引',
    INDEX `idx_dept_id` (`sys_dept_id`) USING BTREE COMMENT '部门ID索引',
    INDEX `idx_phone` (`sys_user_phone`) USING BTREE COMMENT '手机号索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表 - 存储系统用户账号基本信息' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 6. 用户-角色关系表 (sys_user_role)
-- 用途: 存储用户与角色的多对多关系
-- =====================================================
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
    `sys_user_role_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '关系ID - 主键自增',
    `sys_user_id` int(11) NULL DEFAULT NULL COMMENT '用户ID - 关联用户表',
    `sys_role_id` int(11) NULL DEFAULT NULL COMMENT '角色ID - 关联角色表',
    PRIMARY KEY (`sys_user_role_id`) USING BTREE,
    INDEX `idx_user_id` (`sys_user_id`) USING BTREE COMMENT '用户ID索引',
    INDEX `idx_role_id` (`sys_role_id`) USING BTREE COMMENT '角色ID索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户-角色关系表 - 存储用户与角色的多对多映射关系' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 7. 用户配置表 (sys_user_config)
-- 用途: 存储用户的第三方账号绑定信息
-- =====================================================
DROP TABLE IF EXISTS `sys_user_config`;
CREATE TABLE `sys_user_config` (
    `sys_user_config_id` int(11) NOT NULL COMMENT '用户配置ID - 与用户ID一致(一对一)',
    `sys_user_config_wx_openid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信OpenID - 微信登录的OpenID',
    `sys_user_config_wx_unionid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信UnionID - 微信开放平台的UnionID',
    `sys_user_config_gitee_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Gitee用户ID - Gitee登录绑定的用户ID',
    `sys_user_config_github_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'GitHub用户ID - GitHub登录绑定的用户ID',
    `sys_user_config_microsoft_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微软账号ID - Microsoft登录绑定的用户ID',
    `sys_user_config_google_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Google账号ID - Google登录绑定的用户ID',
    `sys_user_config_qq_openid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'QQ OpenID - QQ登录的OpenID',
    `sys_user_config_dingtalk_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '钉钉用户ID - 钉钉登录绑定的用户ID',
    `sys_user_config_feishu_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '飞书用户ID - 飞书登录绑定的用户ID',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_user_config_id`) USING BTREE,
    INDEX `idx_wx_openid` (`sys_user_config_wx_openid`) USING BTREE COMMENT '微信OpenID索引',
    INDEX `idx_gitee_id` (`sys_user_config_gitee_id`) USING BTREE COMMENT 'Gitee ID索引',
    INDEX `idx_microsoft_id` (`sys_user_config_microsoft_id`) USING BTREE COMMENT '微软账号索引'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户配置表 - 存储用户第三方账号绑定信息' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 8. 用户布局配置表 (sys_user_layout)
-- 用途: 存储用户个性化的界面布局配置
-- =====================================================
DROP TABLE IF EXISTS `sys_user_layout`;
CREATE TABLE `sys_user_layout` (
    `sys_layout_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '布局ID - 主键自增',
    `sys_layout_layout` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '布局配置 - JSON格式的网格布局信息',
    `sys_layout_grid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '网格配置 - JSON格式的网格参数',
    `sys_layout_component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件配置 - JSON格式的组件列表',
    `sys_layout_user_id` int(11) NULL DEFAULT NULL COMMENT '所属用户ID - 关联用户表',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_layout_id`) USING BTREE,
    UNIQUE INDEX `uk_user_id` (`sys_layout_user_id`) USING BTREE COMMENT '用户ID唯一索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户布局配置表 - 存储用户个性化的界面布局' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 9. 用户登录日志表 (sys_user_log)
-- 用途: 记录用户登录登出操作日志
-- =====================================================
DROP TABLE IF EXISTS `sys_user_log`;
CREATE TABLE `sys_user_log` (
    `sys_log_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '日志ID - 主键自增',
    `sys_log_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '登录账号 - 用户登录使用的账号',
    `sys_log_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '登录地点 - 根据IP解析的地理位置',
    `sys_log_ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '登录IP - 用户登录时的IP地址',
    `sys_log_isp` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '运营商 - 网络运营商信息',
    `sys_log_system` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作系统 - 用户使用的操作系统',
    `sys_log_ua` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'User-Agent - 浏览器完整的UA字符串',
    `sys_log_browser` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '浏览器 - 浏览器类型和版本',
    `sys_log_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求地址 - 登录的API地址',
    `sys_log_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求参数 - 登录请求的参数(脱敏)',
    `sys_log_cost` int(11) NULL DEFAULT NULL COMMENT '耗时(毫秒) - 登录操作耗时',
    `sys_log_from` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作类型 - LOGIN:登录, LOGOUT:登出',
    `sys_log_status` int(11) NULL DEFAULT NULL COMMENT '操作状态 - 0:失败, 1:成功',
    `sys_log_fingerprint` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '浏览器指纹 - 用于设备识别的指纹',
    `sys_log_login_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '登录方式 - WEB:网页, APP:应用, GITEE:Gitee等',
    `sys_dept_id` int(11) NULL DEFAULT NULL COMMENT '所属机构ID - 用户所属的部门ID',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人ID(用户ID)',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_log_id`) USING BTREE,
    INDEX `idx_username` (`sys_log_username`) USING BTREE COMMENT '用户名索引',
    INDEX `idx_from` (`sys_log_from`) USING BTREE COMMENT '操作类型索引',
    INDEX `idx_create_time` (`create_time`) USING BTREE COMMENT '创建时间索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户登录日志表 - 记录用户登录登出操作' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 10. 系统操作日志表 (sys_log)
-- 用途: 记录系统通用操作日志
-- =====================================================
DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log` (
    `sys_log_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '日志ID - 主键自增',
    `sys_log_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作账号 - 执行操作的用户账号',
    `sys_log_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求IP - 发起请求的IP地址',
    `sys_log_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求地址 - API接口路径',
    `sys_log_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求参数 - 接口请求参数',
    `sys_log_from` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求模块 - 功能模块名称',
    `sys_log_status` int(11) NULL DEFAULT NULL COMMENT '请求状态 - 0:失败, 1:成功',
    `sys_log_cost` int(11) NULL DEFAULT NULL COMMENT '耗时(毫秒) - 接口响应时间',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_log_id`) USING BTREE,
    INDEX `idx_username` (`sys_log_username`) USING BTREE COMMENT '用户名索引',
    INDEX `idx_create_time` (`create_time`) USING BTREE COMMENT '创建时间索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统操作日志表 - 记录系统通用操作日志' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 11. 组件库表 (sys_sfc)
-- 用途: 存储可配置的前端组件信息
-- =====================================================
DROP TABLE IF EXISTS `sys_sfc`;
CREATE TABLE `sys_sfc` (
    `sys_sfc_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '组件ID - 主键自增',
    `sys_sfc_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件名称(英文) - 组件的唯一标识名',
    `sys_sfc_chinese_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件名称(中文) - 组件的显示名称',
    `sys_sfc_function` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件功能 - 多个功能用逗号分隔',
    `sys_sfc_icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件图标 - 图标名称',
    `sys_sfc_category` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件分类 - 如HOME表示首页组件',
    `sys_sfc_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件路径 - 组件文件存放路径或URL',
    `sys_sfc_status` int(11) NULL DEFAULT NULL COMMENT '组件状态 - 0:未启用, 1:已启用',
    `sys_sfc_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件描述 - 组件功能说明',
    `sys_sfc_proxy` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '代理地址 - 组件数据接口的代理地址',
    `sys_sfc_type` int(11) NULL DEFAULT NULL COMMENT '组件类型 - 0:文件, 1:文本, 2:外链, 3:本地',
    `sys_sfc_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '组件内容 - 当类型为文本时存储Vue组件代码',
    `sys_sfc_model_cache` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '模块缓存 - JSON格式的依赖模块配置',
    `sys_sfc_delay` int(11) NULL DEFAULT NULL COMMENT '延迟时间(毫秒) - 组件加载延迟',
    `sys_sfc_version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件版本 - 版本号如"1.0.0"',
    `sys_sfc_timeout` int(11) NULL DEFAULT NULL COMMENT '超时时间(毫秒) - 组件加载超时设置',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_sfc_id`) USING BTREE,
    INDEX `idx_category` (`sys_sfc_category`) USING BTREE COMMENT '分类索引',
    INDEX `idx_status` (`sys_sfc_status`) USING BTREE COMMENT '状态索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '组件库表 - 存储可配置的前端组件信息' ROW_FORMAT = Dynamic;

-- =====================================================
-- 12. 用户安装组件表 (sys_sfc_install)
-- 用途: 存储用户已安装的组件列表
-- =====================================================
DROP TABLE IF EXISTS `sys_sfc_install`;
CREATE TABLE `sys_sfc_install` (
    `sys_sfc_install_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '安装记录ID - 主键自增',
    `user_id` int(11) NULL DEFAULT NULL COMMENT '用户ID - 安装组件的用户',
    `sys_sfc_id` int(11) NULL DEFAULT NULL COMMENT '组件ID - 被安装的组件',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int(11) NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间(安装时间)',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int(11) NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_sfc_install_id`) USING BTREE,
    INDEX `idx_user_id` (`user_id`) USING BTREE COMMENT '用户ID索引',
    INDEX `idx_sfc_id` (`sys_sfc_id`) USING BTREE COMMENT '组件ID索引',
    UNIQUE INDEX `uk_user_sfc` (`user_id`, `sys_sfc_id`) USING BTREE COMMENT '用户-组件唯一索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户安装组件表 - 存储用户已安装的组件列表' ROW_FORMAT = Dynamic;

-- =====================================================
-- 初始数据 - 默认角色
-- =====================================================
INSERT INTO `sys_role` (`sys_role_id`, `sys_role_name`, `sys_role_code`, `sys_role_in_system`, `sys_role_status`, `sys_role_sort`, `sys_role_remark`, `sys_role_hidden`) VALUES 
(1, '超级管理员', 'SUPER_ADMIN', 1, 1, 1, '系统超级管理员，拥有所有权限', 0),
(2, '管理员', 'ADMIN', 1, 1, 2, '系统管理员', 0),
(3, '运维', 'OPS', 1, 1, 3, '运维人员', 0),
(4, '普通用户', 'USER', 0, 1, 4, '普通用户', 0);

-- =====================================================
-- 初始数据 - 默认超级管理员账号
-- 账号: sa  密码: 123456 (MD5加盐加密)
-- =====================================================
INSERT INTO `sys_user` (`sys_user_id`, `sys_user_username`, `sys_user_nickname`, `sys_user_password`, `sys_user_salt`, `sys_user_delete`, `sys_user_status`, `sys_user_in_system`) VALUES 
(1, 'sa', '超级管理员', '53006d111477a9b5b16c4b38ac8fad69', '123456', 0, 1, 1);

-- =====================================================
-- 初始数据 - 超级管理员角色关联
-- =====================================================
INSERT INTO `sys_user_role` (`sys_user_id`, `sys_role_id`) VALUES (1, 1);

-- =====================================================
-- 初始数据 - 默认组织机构
-- =====================================================
INSERT INTO `sys_dept` (`sys_dept_id`, `sys_dept_name`, `sys_dept_pid`, `sys_dept_tree_id`, `sys_dept_code`, `sys_dept_sort`, `sys_dept_status`) VALUES 
(1, '总部', 0, '0,1', 'ROOT', 1, 0);

SET FOREIGN_KEY_CHECKS = 1;
