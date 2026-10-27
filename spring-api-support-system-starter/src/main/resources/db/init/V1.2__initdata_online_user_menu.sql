-- =====================================================================
-- 监控管理 → 在线用户（仅管理员）菜单与角色授权 —— 补丁迁移
--
-- 背景：在线用户页面读取 oauth 服务端在线名单，属运维能力，只对管理员
-- 及以上开放（角色 SUPER_ADMIN / ADMIN）。菜单按 hotsport 相同的补丁
-- 方式独立落文件：老库执行一次、新库 INSERT IGNORE 空转，两侧结果一致。
--
-- 权限约定：
--   - sys_menu_role = 'SUPER_ADMIN,ADMIN'：接口层菜单归属（仅管理员）
--   - sys_role_menu 只给角色 1(SUPER_ADMIN) / 2(ADMIN) 绑定，其他角色不可见
--   - 后端 OnlineUserController 再以 @Permission(role=ADMIN/SUPER_ADMIN)
--     与方法内 isAdmin 二次校验，杜绝伪造请求拉取在线名单
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
(126, 'online', '/monitor/online', NULL, '在线用户', NULL, 100, '0,100,126', 'system/online/index', 'ep:user', 1, 15, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL);

-- 在线用户(126)：仅 SUPER_ADMIN(1) / ADMIN(2) 可见，与监控管理子菜单角色范围一致。
INSERT IGNORE INTO `sys_role_menu` (`sys_role_menu_id`, `sys_role_id`, `sys_menu_id`) VALUES
(248, 1, 126),
(249, 2, 126);