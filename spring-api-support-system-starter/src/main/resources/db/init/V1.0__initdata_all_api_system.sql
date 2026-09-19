-- =====================================================================
-- V1.0__initdata_all_api_system.sql - spring-api-support-system-starter 全量种子（合并自 initdata 文件 + init 内嵌种子）
-- 通道: INITDATA 虚拟线程异步；全部幂等（INSERT IGNORE / WHERE NOT EXISTS）。
-- =====================================================================

-- ---------------------------------------------------------------------
-- 来源: V1.0__initdata_system.sql
-- ---------------------------------------------------------------------
-- =====================================================================
-- 系统管理模块初始化数据（异步执行，不阻塞启动）
-- 由 V1.0__init_system.sql 建表后，本脚本补充种子数据：
--   默认角色、系统菜单、角色-菜单关系、默认系统账号、部门、头像框
-- 幂等：全量 H2 模式（sys_dept/sys_menu/sys_avatar_frame/sys_user 等
-- 建表时 DROP+CREATE 清空），MySQL 模式 sys_role/sys_menu/sys_role_menu
-- 保留历史数据，INSERT IGNORE 靠主键幂等，重复执行安全
-- 兼容 MySQL 8 / H2(MySQL模式)
-- =====================================================================

-- =====================================================
-- 1. 默认角色
-- =====================================================
INSERT IGNORE INTO `sys_role` (`sys_role_id`, `sys_role_name`, `sys_role_code`, `sys_role_in_system`, `sys_role_status`, `sys_role_sort`, `sys_role_remark`, `sys_role_hidden`) VALUES
(1, '超级管理员', 'SUPER_ADMIN', 1, 1, 1, '系统超级管理员，拥有所有权限', 0),
(2, '管理员', 'ADMIN', 1, 1, 2, '系统管理员', 0),
(3, '运维', 'OPS', 1, 1, 3, '运维人员', 0),
(4, '普通用户', 'USER', 0, 1, 4, '普通用户', 0),
(5, '部门负责人', 'DEPT_LEADER', 1, 1, 5, '负责部门及子部门账号管理', 0),
(6, '游客', 'GUEST', 1, 1, 6, '游客账号', 0);

-- =====================================================
-- 2. 系统基础菜单与权限
-- 仅保留当前 system 应用实际依赖的 system/common-service 基线能力
-- =====================================================
-- 菜单与角色菜单绑定：与线上 system_db 逐行对齐（含任务派发/调度中心/消息渠道/发送日志入口）
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
(1, 'system', '/manage', NULL, '系统管理', NULL, 0, '0,1', '/manage', 'ep:setting', 0, 2, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, '', NULL, NULL),
(2, 'menu', '/manage/menu', NULL, '菜单管理', NULL, 1, '0,1,2', 'system/menu/index', 'ep:menu', 0, 1, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(3, 'setting', '/manage/setting', '', '系统配置', '', 1, '0,1,3', 'system/setting/index', 'ep:setting', 0, 999, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(4, 'role', '/manage/role', NULL, '角色管理', NULL, 1, '0,1,4', 'system/role/index', 'ri:shield-user-line', 0, 2, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, '', NULL, NULL),
(5, '', '', '', '新增', 'sys:role:add', 4, '0,1,4', '', '', 3, 1, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), NULL, NULL, '', NULL, NULL),
(6, '', '', '', '删除', 'sys:role:delete', 4, '0,1,4', '', '', 3, 1, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), NULL, NULL, '', NULL, NULL),
(7, '', '', '', '修改', 'sys:role:update', 4, '0,1,4', '', '', 3, 1, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), NULL, NULL, '', NULL, NULL),
(8, 'user', '/manage/user', '', '用户管理', '', 1, '0,1,8', 'system/user/index', 'ri:user-settings-line', 0, 3, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, '', NULL, NULL),
(9, 'userLog', '/manage/log/login', '', '登录日志', '', 1, '0,1,9', 'system/userlog/index', 'ep:alarm-clock', 0, 998, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, '', NULL, NULL),
(11, 'dept', '/manage/dept', '', '机构管理', '', 1, '0,1,11', 'system/dept/index', 'ep:star', 0, 5, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, '', NULL, NULL),
(12, '', '', '', '删除', 'sys:login:delete', 8, '0,1,8', '', '', 3, 1, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), NULL, NULL, '', NULL, NULL),
(13, '', '', '', '编辑', 'sys:login:update', 8, '0,1,8', '', '', 3, 1, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), NULL, NULL, '', NULL, NULL),
(14, '', '', '', '新增', 'sys:login:add', 8, '0,1,8,14', '', '', 3, 1, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, '', NULL, NULL),
(15, 'dict', '/manage/dict', '', '字典管理', '', 1, '0,1,15', 'system/dict/index', 'humbleicons:dashboard', 0, 6, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, '', NULL, NULL),
(18, 'sysLog', '/manage/syslog', '', '系统日志', '', 1, '0,1,18', 'system/syslog/index', 'ep:list', 0, 999, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, '', NULL, NULL),
(19, '', '', '', '删除', 'sys:log:delete', 18, '0,1,18,19', '', '', 3, 1, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), NULL, NULL, '', NULL, NULL),
(20, 'online', '/manage/online', '', '在线用户', '', 1, '0,1,20', 'system/online/index', 'ep:monitor', 0, 7, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(30, 'post', '/manage/post', NULL, '岗位管理', NULL, 1, '0,1,30', 'system/post/index', 'ri:briefcase-4-line', 0, 4, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(31, 'tenant', '/manage/tenant', NULL, '租户管理', NULL, 1, '0,1,31', 'system/tenant/index', 'ri:community-line', 0, 8, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(32, 'announcement', '/manage/announcement', NULL, '通知公告', NULL, 1, '0,1,32', 'system/announcement/index', 'ep:bell', 0, 9, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(33, 'feedback', '/manage/feedback', NULL, '意见反馈', NULL, 1, '0,1,33', 'system/feedback/index', 'ep:chat-dot-round', 0, 10, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(34, 'ip', '/manage/ip', NULL, 'IP 黑白名单', NULL, 1, '0,1,34', 'system/ip/index', 'ep:position', 0, 11, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(35, 'cache', '/manage/cache', NULL, '缓存监控', NULL, 1, '0,1,35', 'system/cache/index', 'ep:coin', 0, 12, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(36, 'errorlog', '/manage/errorlog', NULL, '错误日志', NULL, 1, '0,1,36', 'system/errorlog/index', 'ep:warning', 0, 13, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(37, 'message', '/manage/message', NULL, '站内消息', NULL, 1, '0,1,37', 'system/message/index', 'ep:message', 0, 14, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(38, 'monitor', '/manage/monitor', NULL, '服务监控', NULL, 1, '0,1,38', 'system/monitor/index', 'ep:odometer', 0, 15, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(39, 'file', '/manage/file', NULL, '文件管理', NULL, 1, '0,1,39', 'filesystem/list/index', 'ep:folder', 0, 20, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(40, 'filegroup', '/manage/filegroup', NULL, '文件分组', NULL, 1, '0,1,40', 'filesystem/group/index', 'ep:files', 0, 21, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(41, 'filestorage', '/manage/filestorage', NULL, '存储配置', NULL, 1, '0,1,41', 'filesystem/filestorage/index', 'ep:box', 0, 22, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(42, 'filesetting', '/manage/filesetting', NULL, '文件设置', NULL, 1, '0,1,42', 'filesystem/setting/index', 'ep:setting', 0, 23, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(43, 'msgtemplate', '/manage/msgtemplate', NULL, '消息模板', NULL, 1, '0,1,43', 'message/template/index', 'ep:document', 0, 24, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(44, 'taskdispatch', '/manage/taskdispatch', '', '任务派发', '', 1, '0,1,44', 'task/dispatch/index', 'ep:promotion', 0, 25, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(45, 'scheduler', '/manage/scheduler', '', '调度中心', '', 1, '0,1,45', 'task/scheduler/index', 'ep:timer', 0, 26, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(46, 'msgprovider', '/manage/msgprovider', '', '消息渠道', '', 1, '0,1,46', 'message/provider/index', 'ep:connection', 0, 27, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(47, 'msglog', '/manage/msglog', '', '发送日志', '', 1, '0,1,47', 'message/log/index', 'ep:tickets', 0, 28, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL);

INSERT IGNORE INTO `sys_role_menu` (`sys_role_menu_id`, `sys_role_id`, `sys_menu_id`) VALUES
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
(162, 2, 19),
(163, 2, 43),
(164, 2, 42),
(165, 2, 41),
(166, 2, 40),
(167, 2, 39),
(168, 2, 38),
(169, 2, 37),
(170, 2, 36),
(171, 2, 35),
(172, 2, 34),
(173, 2, 33),
(174, 2, 32),
(175, 2, 31),
(176, 2, 30),
(177, 2, 44),
(178, 2, 45),
(179, 2, 46),
(180, 2, 47);

-- =====================================================
-- 3. 默认系统账号
-- 账号: sa / admin / ops
-- 密码: 123456 (统一算法 md5(明文 + salt)，见 PasswordUtils)
-- =====================================================
INSERT IGNORE INTO `sys_user` (`sys_user_id`, `sys_user_username`, `sys_user_nickname`, `sys_user_password`, `sys_user_salt`, `sys_user_delete`, `sys_user_status`, `sys_user_in_system`) VALUES
(1, 'sa', '超级管理员', 'ea48576f30be1669971699c09ad05c94', '123456', 0, 1, 1),
(2, 'admin', '管理员', 'ea48576f30be1669971699c09ad05c94', '123456', 0, 1, 1),
(3, 'ops', '运维', 'ea48576f30be1669971699c09ad05c94', '123456', 0, 1, 1);

-- =====================================================
-- 4. 默认账号角色关联
-- =====================================================
INSERT IGNORE INTO `sys_user_role` (`sys_user_id`, `sys_role_id`) VALUES
(1, 1),
(2, 2),
(3, 3);

-- =====================================================
-- 5. 默认组织机构
-- =====================================================
INSERT IGNORE INTO `sys_dept` (`sys_dept_id`, `sys_dept_name`, `sys_dept_pid`, `sys_dept_tree_id`, `sys_dept_code`, `sys_dept_sort`, `sys_dept_status`) VALUES
(1, '总部', 0, '0,1', 'ROOT', 1, 0);

-- =====================================================
-- 6. 头像框种子（常规 2-7、生肖 201-212、神兽 221-226、生日 100）
-- =====================================================
INSERT IGNORE INTO `sys_avatar_frame` (`sys_avatar_frame_id`, `sys_avatar_frame_name`, `sys_avatar_frame_type`, `sys_avatar_frame_color`, `sys_avatar_frame_desc`, `sys_avatar_frame_acquire`, `sys_avatar_frame_sort`) VALUES
(1, '经典', 'base', 'transparent', '默认无边框', 'default', 0);

-- 常规框：只有渐变色，前端按「渐变 + 径向遮罩挖孔」画素环
INSERT IGNORE INTO `sys_avatar_frame` (`sys_avatar_frame_id`, `sys_avatar_frame_name`, `sys_avatar_frame_type`, `sys_avatar_frame_color`, `sys_avatar_frame_desc`, `sys_avatar_frame_acquire`, `sys_avatar_frame_sort`) VALUES
(2, '素环', 'regular', 'linear-gradient(135deg, #f2f5f8 0%, #b9c2cc 35%, #ffffff 55%, #8e97a1 100%)', '常规款·银白素环', 'default', 20),
(3, '墨玉', 'regular', 'linear-gradient(135deg, #3a3f47 0%, #6b7480 35%, #1b1e23 60%, #59616c 100%)', '常规款·墨玉深灰', 'default', 21),
(4, '鎏金', 'regular', 'linear-gradient(135deg, #fff3c4 0%, #d4a531 30%, #a9772a 55%, #f7e59b 100%)', '常规款·鎏金暖阳', 'default', 22),
(5, '青瓷', 'regular', 'linear-gradient(135deg, #e2f5ee 0%, #6fbfa9 35%, #38796d 65%, #bfe6d8 100%)', '常规款·青瓷淡翠', 'default', 23),
(6, '绛朱', 'regular', 'linear-gradient(135deg, #ffd9c9 0%, #d9534a 35%, #9b241c 65%, #ffb3a0 100%)', '常规款·绛朱丹霞', 'default', 24),
(7, '星辉', 'regular', 'linear-gradient(135deg, #dfe4ff 0%, #7c6ff0 30%, #34296e 60%, #a8b6ff 100%)', '常规款·星辉蓝紫', 'default', 25);

INSERT IGNORE INTO `sys_avatar_frame` (`sys_avatar_frame_id`, `sys_avatar_frame_name`, `sys_avatar_frame_type`, `sys_avatar_frame_image`, `sys_avatar_frame_desc`, `sys_avatar_frame_acquire`, `sys_avatar_frame_sort`) VALUES
(201, '子鼠', 'zodiac', '/assets/avatar-frames/zodiac-rat.svg', '暗珐琅描金·生肖鼠', 'default', 1),
(202, '丑牛', 'zodiac', '/assets/avatar-frames/zodiac-ox.svg', '暗珐琅描金·生肖牛', 'default', 2),
(203, '寅虎', 'zodiac', '/assets/avatar-frames/zodiac-tiger.svg', '暗珐琅描金·生肖虎', 'default', 3),
(204, '卯兔', 'zodiac', '/assets/avatar-frames/zodiac-rabbit.svg', '暗珐琅描金·生肖兔', 'default', 4),
(205, '辰龙', 'zodiac', '/assets/avatar-frames/zodiac-dragon.svg', '暗珐琅描金·生肖龙', 'default', 5),
(206, '巳蛇', 'zodiac', '/assets/avatar-frames/zodiac-snake.svg', '暗珐琅描金·生肖蛇', 'default', 6),
(207, '午马', 'zodiac', '/assets/avatar-frames/zodiac-horse.svg', '暗珐琅描金·生肖马', 'default', 7),
(208, '未羊', 'zodiac', '/assets/avatar-frames/zodiac-goat.svg', '暗珐琅描金·生肖羊', 'default', 8),
(209, '申猴', 'zodiac', '/assets/avatar-frames/zodiac-monkey.svg', '暗珐琅描金·生肖猴', 'default', 9),
(210, '酉鸡', 'zodiac', '/assets/avatar-frames/zodiac-rooster.svg', '暗珐琅描金·生肖鸡', 'default', 10),
(211, '戌狗', 'zodiac', '/assets/avatar-frames/zodiac-dog.svg', '暗珐琅描金·生肖狗', 'default', 11),
(212, '亥猪', 'zodiac', '/assets/avatar-frames/zodiac-pig.svg', '暗珐琅描金·生肖猪', 'default', 12),
(221, '狮鹫', 'beast', '/assets/avatar-frames/beast-griffin.svg', '暗珐琅描金·西方神兽狮鹫', 'limit', 13),
(222, '独角兽', 'beast', '/assets/avatar-frames/beast-unicorn.svg', '暗珐琅描金·西方神兽独角兽', 'limit', 14),
(223, '凤凰', 'beast', '/assets/avatar-frames/beast-phoenix.svg', '暗珐琅描金·神兽凤凰', 'limit', 15),
(224, '麒麟', 'beast', '/assets/avatar-frames/beast-qilin.svg', '暗珐琅描金·神兽麒麟', 'limit', 16),
(225, '九尾狐', 'beast', '/assets/avatar-frames/beast-fox.svg', '暗珐琅描金·神兽九尾狐', 'limit', 17),
(226, '天马', 'beast', '/assets/avatar-frames/beast-pegasus.svg', '暗珐琅描金·西方神兽天马', 'limit', 18);

INSERT IGNORE INTO `sys_avatar_frame` (`sys_avatar_frame_id`, `sys_avatar_frame_name`, `sys_avatar_frame_type`, `sys_avatar_frame_image`, `sys_avatar_frame_desc`, `sys_avatar_frame_acquire`, `sys_avatar_frame_sort`, `sys_avatar_frame_status`) VALUES
(100, '生日快乐', 'birthday', '/assets/avatar-frames/birthday.svg', '生日专属限定头像框', 'birthday', 99, 1);

