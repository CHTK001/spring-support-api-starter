-- =====================================================================
-- 监控管理 → 系统指标（@pages/hotsport）菜单与角色授权 —— 补丁迁移
--
-- 背景：这批菜单最初是直接追加进 V1.0__initdata_all_api_system.sql 的。
-- 全新安装的库由 V1.0 一并写入，没有问题；但在 V1.0 发布之前就已经初始
-- 化过的库，其版本表里已有 V1.0 的成功执行记录，data-scan-mode=ONCE 下
-- 不会再跑一遍 V1.0，于是老库里永远没有这 15 条菜单 —— 表现为侧边栏没有
-- 「系统指标」，只能 URL 直达且 404。
--
-- 因此这里用独立文件补一次：FlywayLike 的 ONCE 语义只跳过「版本表里有成功
-- 记录」的脚本，新文件名没有记录，老库会把本文件执行一次；新库则在 V1.0
-- 之后执行本文件，因全程 INSERT IGNORE 而整体空转，两侧结果一致。
--
-- 详细约定（component 写法、路由来源、角色范围）见 V1.0 里同名段落说明。
-- =====================================================================

INSERT IGNORE INTO `sys_menu` (
    `sys_menu_id`,
    `sys_menu_name`,
    `sys_menu_path`,
    `sys_menu_i18n`,
    `sys_menu_title`,
    `sys_menu_perm`,
    `sys_menu_pid`,
    `sys_menu_tree_id`,
    `sys_menu_component`,
    `sys_menu_icon`,
    `sys_menu_type`,
    `sys_menu_sort`,
    `sys_menu_hidden`,
    `sys_menu_show_parent`,
    `sys_menu_hidden_tag`,
    `sys_menu_fixed_tag`,
    `sys_menu_redirect`,
    `sys_menu_keep_alive`,
    `create_name`,
    `create_by`,
    `create_time`,
    `update_time`,
    `update_name`,
    `update_by`,
    `sys_menu_role`,
    `sys_menu_home`,
    `sys_menu_engine`
) VALUES
(111, 'hotsport', '/hotsport', NULL, '系统指标', NULL, 100, '0,100,111', '/hotsport', 'ep:data-line', 0, 5, 0, 1, 0, 0, '/hotsport/dashboard', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(112, 'dashboard', '/hotsport/dashboard', NULL, '总览看板', NULL, 111, '0,100,111,112', 'hotsport/dashboard/index', 'ep:data-line', 1, 10, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(113, 'traces', '/hotsport/traces', NULL, '调用链路', NULL, 111, '0,100,111,113', 'hotsport/traces/index', 'ep:document', 1, 20, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(114, 'dependencies', '/hotsport/dependencies', NULL, '依赖拓扑', NULL, 111, '0,100,111,114', 'hotsport/dependencies/index', 'ep:share', 1, 30, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(115, 'topology', '/hotsport/topology', NULL, '系统拓扑', NULL, 111, '0,100,111,115', 'hotsport/topology/index', 'ep:connection', 1, 40, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(116, 'services', '/hotsport/services', NULL, '服务列表', NULL, 111, '0,100,111,116', 'hotsport/services/index', 'ep:list', 1, 50, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(117, 'endpoints', '/hotsport/endpoints', NULL, '端点分析', NULL, 111, '0,100,111,117', 'hotsport/endpoints/index', 'ep:aim', 1, 60, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(118, 'errors', '/hotsport/errors', NULL, '错误排行', NULL, 111, '0,100,111,118', 'hotsport/errors/index', 'ep:circle-close', 1, 70, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(119, 'leaks', '/hotsport/leaks', NULL, '资源泄漏', NULL, 111, '0,100,111,119', 'hotsport/leaks/index', 'ep:warning', 1, 80, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(120, 'jvm', '/hotsport/jvm', NULL, 'JVM 指标', NULL, 111, '0,100,111,120', 'hotsport/jvm/index', 'ep:cpu', 1, 90, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(121, 'logs', '/hotsport/logs', NULL, '日志', NULL, 111, '0,100,111,121', 'hotsport/logs/index', 'ep:tickets', 1, 100, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(122, 'alerts', '/hotsport/alerts', NULL, '告警中心', NULL, 111, '0,100,111,122', 'hotsport/alerts/index', 'ep:bell', 1, 110, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(123, 'agent', '/hotsport/agent', NULL, 'Agent 中心', NULL, 111, '0,100,111,123', 'hotsport/agent/index', 'ep:monitor', 1, 120, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(124, 'injector', '/hotsport/injector', NULL, '数据注入器', NULL, 111, '0,100,111,124', 'hotsport/injector/index', 'ep:magic-stick', 1, 130, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(125, 'settings', '/hotsport/settings', NULL, '接口设置', NULL, 111, '0,100,111,125', 'hotsport/settings/index', 'ep:setting', 1, 140, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL);

-- 系统指标(111) 与 14 个叶子(112-125)：SUPER_ADMIN(1) / ADMIN(2) 可见，
-- 与 100-104 监控管理子菜单的角色范围保持一致。
INSERT IGNORE INTO `sys_role_menu` (`sys_role_menu_id`, `sys_role_id`, `sys_menu_id`) VALUES
(218, 1, 111),
(219, 1, 112),
(220, 1, 113),
(221, 1, 114),
(222, 1, 115),
(223, 1, 116),
(224, 1, 117),
(225, 1, 118),
(226, 1, 119),
(227, 1, 120),
(228, 1, 121),
(229, 1, 122),
(230, 1, 123),
(231, 1, 124),
(246, 1, 125),
(232, 2, 111),
(233, 2, 112),
(234, 2, 113),
(235, 2, 114),
(236, 2, 115),
(237, 2, 116),
(238, 2, 117),
(239, 2, 118),
(240, 2, 119),
(241, 2, 120),
(242, 2, 121),
(243, 2, 122),
(244, 2, 123),
(245, 2, 124),
(247, 2, 125);
