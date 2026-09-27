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
-- sys_role 种子已下沉至 spring-api-support-common-system-starter

-- =====================================================
-- 2. 系统基础菜单与权限
-- 仅保留当前 system 应用实际依赖的 system/common-service 基线能力
-- =====================================================
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
(1, 'system', '/manage', NULL, '系统管理', NULL, 0, '0,1', '/manage', 'ep:setting', 0, 10, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, '', NULL, NULL),
(2, 'menu', '/manage/menu', NULL, '菜单管理', NULL, 1, '0,1,2', 'system/menu/index', 'ep:menu', 0, 30, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(3, 'setting', '/manage/setting', '', '配置管理', '', 1, '0,1,3', 'system/setting/index', 'ep:setting', 0, 80, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(4, 'role', '/manage/role', NULL, '角色管理', NULL, 1, '0,1,4', 'system/role/index', 'ri:shield-user-line', 0, 20, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, '', NULL, NULL),
(5, '', '', '', '新增', 'sys:role:add', 4, '0,1,4', '', '', 3, 1, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), NULL, NULL, '', NULL, NULL),
(6, '', '', '', '删除', 'sys:role:delete', 4, '0,1,4', '', '', 3, 1, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), NULL, NULL, '', NULL, NULL),
(7, '', '', '', '修改', 'sys:role:update', 4, '0,1,4', '', '', 3, 1, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), NULL, NULL, '', NULL, NULL),
(8, 'user', '/manage/user', '', '用户管理', '', 1, '0,1,8', 'system/user/index', 'ri:user-settings-line', 0, 10, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, '', NULL, NULL),
(9, 'userLog', '/manage/log/login', '', '登录日志', '', 1, '0,1,9', 'system/userlog/index', 'ep:alarm-clock', 0, 150, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, '', NULL, NULL),
(11, 'dept', '/manage/dept', '', '机构管理', '', 1, '0,1,11', 'system/dept/index', 'ep:star', 0, 40, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, '', NULL, NULL),
(12, '', '', '', '删除', 'sys:login:delete', 8, '0,1,8', '', '', 3, 1, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), NULL, NULL, '', NULL, NULL),
(13, '', '', '', '编辑', 'sys:login:update', 8, '0,1,8', '', '', 3, 1, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), NULL, NULL, '', NULL, NULL),
(14, '', '', '', '新增', 'sys:login:add', 8, '0,1,8,14', '', '', 3, 1, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, '', NULL, NULL),
(15, 'dict', '/manage/dict', '', '字典管理', '', 1, '0,1,15', 'system/dict/index', 'humbleicons:dashboard', 0, 70, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, '', NULL, NULL),
(18, 'sysLog', '/manage/syslog', '', '系统日志', '', 1, '0,1,18', 'system/syslog/index', 'ep:list', 0, 160, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, '', NULL, NULL),
(19, '', '', '', '删除', 'sys:log:delete', 18, '0,1,18,19', '', '', 3, 1, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), NULL, NULL, '', NULL, NULL),
(20, 'feedback', '/manage/feedback', NULL, '反馈管理', NULL, 1, '0,1,20', 'system/feedback/index', 'ri:feedback-line', 0, 110, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(41, 'filestorage', '/manage/filestorage', NULL, '文件管理', NULL, 1, '0,1,41', 'filesystem/filestorage/index', 'ep:box', 0, 140, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(42, 'post', '/manage/post', NULL, '岗位管理', NULL, 1, '0,1,42', 'system/post/index', 'ep:postcard', 0, 50, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(43, 'message', '/manage/message', NULL, '消息管理', NULL, 1, '0,1,43', 'system/message/index', 'ep:message', 0, 90, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(44, 'announcement', '/manage/announcement', NULL, '公告管理', NULL, 1, '0,1,44', 'system/announcement/index', 'ep:bell', 0, 100, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(56, '', '', '', '新增', 'sys:menu:add', 2, '0,1,2', '', '', 3, 1, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), NULL, NULL, '', NULL, NULL),
(57, '', '', '', '编辑', 'sys:menu:update', 2, '0,1,2', '', '', 3, 2, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), NULL, NULL, '', NULL, NULL),
(58, '', '', '', '删除', 'sys:menu:delete', 2, '0,1,2', '', '', 3, 3, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), NULL, NULL, '', NULL, NULL),
(59, '', '', '', '新增按钮', 'sys:menu:button:add', 2, '0,1,2', '', '', 3, 4, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), NULL, NULL, '', NULL, NULL),
(60, '', '', '', '删除按钮', 'sys:menu:button:delete', 2, '0,1,2', '', '', 3, 5, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), NULL, NULL, '', NULL, NULL),
(100, 'monitor', '/monitor', NULL, '监控管理', NULL, 0, '0,100', '/monitor', 'ep:odometer', 0, 20, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(101, 'redis', '/monitor/redis', NULL, 'Redis监控', NULL, 100, '0,100,101', 'system/monitor/redis/index', 'ep:coin', 0, 20, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(102, 'cache', '/monitor/cache', NULL, '缓存监控', NULL, 100, '0,100,102', 'system/monitor/cache/index', 'ep:files', 0, 30, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(103, 'server', '/monitor/server', NULL, '监控大盘', NULL, 100, '0,100,103', 'system/monitor/server/index', 'ep:cpu', 0, 10, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(104, 'policy', '/monitor/policy', NULL, '策略任务', NULL, 100, '0,100,104', 'system/monitor/policy/index', 'ep:set-up', 0, 40, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL);

-- ---------------------------------------------------------------------
-- 调度（@pages/job）。单独一条 INSERT：条件四列是后续新增的，
-- 不必回头给上面 30 条历史菜单补默认值（缺失时后端按 ALWAYS 处理）。
--
-- 归属：调度挂在「系统管理(1)」分组下（pid=1），不再作为顶级菜单。
--
-- 只保留一条「调度管理」，它本身就是调度功能的唯一入口：
-- 宿主没有调度模块时这条菜单不显示，因此不存在「点得进、点不开」的僵尸菜单。
-- 早期版本拆过 105 总览 + 106/107/108 三个明细页，总览页的每个面板都带直达
-- 明细的入口，任一明细缺失就落 404；既然明细不再下发，总览页也就没有存在价值，
-- 故合并为单条，避免留下无人可达的页面。
--
-- component 字段的写法由前端 core 的 resolveRouteComponent 决定：
-- 它把 sys_menu_component 拼成 `views/{component}.vue` 去查页面映射表，
-- 所以这里既不能带 views/ 前缀，也不能带 .vue 后缀，否则拼出
-- `views/views/job/console/index.vue` 这种双前缀而回落 404 兜底页。
-- 映射表由 @pages/job 在包内 registerMenuViewModules 注册（见其 src/index.ts）。
--
-- show_type=CLASS 表示「类在 classpath 才显示」，条件值必须是**真实存在**的
-- 类全名，否则菜单恒不显示。job-starter 里的调度启动类是
-- com.chua.starter.job.JobSchedulerApplication；
-- 注意历史上这里写的是 com.chua.starter.scheduler.SchedulerApplication，
-- 该类在本工程不存在（包名是 job 而非 scheduler），条件永远为假 —— 这正是
-- 「菜单点进去 404 / 干脆不显示」的根因之一。
-- ---------------------------------------------------------------------
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
    `sys_menu_engine`,
    `sys_menu_show_type`,
    `sys_menu_show_value`,
    `sys_menu_show_switch`,
    `sys_menu_show_key`
) VALUES
(105, 'jobConsole', '/job/console', NULL, '调度管理', NULL, 1, '0,1,105', 'job/console/index', 'ep:timer', 0, 85, 0, 0, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL, 'CLASS', 'com.chua.starter.job.JobSchedulerApplication', 1, NULL);

INSERT IGNORE INTO `sys_role_menu` (`sys_role_menu_id`, `sys_role_id`, `sys_menu_id`) VALUES
(164, 1, 20),
(200, 1, 100),
(201, 1, 101),
(202, 1, 102),
(203, 1, 103),
(204, 1, 104),
(210, 1, 105),
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
(162, 2, 19),
(163, 2, 20),
(165, 2, 18),
(166, 2, 41),
(169, 2, 42),
(170, 2, 43),
(171, 2, 44),
(205, 2, 100),
(206, 2, 101),
(207, 2, 102),
(208, 2, 103),
(209, 2, 104),
(211, 2, 105),
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
(143, 3, 1);

-- =====================================================
-- 3. 默认系统账号
-- 账号: sa / admin / ops
-- 密码: admin123 (统一算法 md5(明文 + salt)，见 PasswordUtils)
-- =====================================================
-- sys_user 种子已下沉至 spring-api-support-common-system-starter

-- =====================================================
-- 4. 默认账号角色关联
-- =====================================================
-- sys_user_role 种子已下沉至 spring-api-support-common-system-starter

-- =====================================================
-- 5. 默认组织机构
-- =====================================================
-- sys_dept 种子已下沉至 spring-api-support-common-system-starter

-- =====================================================
-- 6. 头像框种子（id=1 透明占位 + 常规 101-106 + 十二生肖 201-212）
-- 静态金属珐琅环（环不动）；生肖动物主体 APNG 动画，常规框静态
INSERT IGNORE INTO `sys_avatar_frame` (`sys_avatar_frame_id`, `sys_avatar_frame_name`, `sys_avatar_frame_type`, `sys_avatar_frame_color`, `sys_avatar_frame_desc`, `sys_avatar_frame_acquire`, `sys_avatar_frame_sort`) VALUES
(1, '经典', 'base', 'transparent', '不佩戴头像框', 'default', 0);

INSERT IGNORE INTO `sys_avatar_frame` (`sys_avatar_frame_id`, `sys_avatar_frame_name`, `sys_avatar_frame_type`, `sys_avatar_frame_image`, `sys_avatar_frame_color`, `sys_avatar_frame_desc`, `sys_avatar_frame_acquire`, `sys_avatar_frame_sort`) VALUES
(101, '鎏金', 'regular', '/assets/avatar-frames/reg-gold.apng', '#C99A3E', '鎏金珐琅环·顶嵌钻石', 'default', 101),
(102, '银辉', 'regular', '/assets/avatar-frames/reg-silver.apng', '#A6B0BE', '银辉珐琅环·繁星点缀', 'default', 102),
(103, '玫瑰金', 'regular', '/assets/avatar-frames/reg-rose.apng', '#D99690', '玫瑰金珐琅环·心语', 'default', 103),
(104, '宝蓝', 'regular', '/assets/avatar-frames/reg-sapphire.apng', '#3A50B0', '宝蓝珐琅环·蓝宝石镶嵌', 'default', 104),
(105, '翡翠', 'regular', '/assets/avatar-frames/reg-emerald.apng', '#2E8C68', '翡翠珐琅环·嫩叶', 'default', 105),
(106, '紫晶', 'regular', '/assets/avatar-frames/reg-amethyst.apng', '#7C54B2', '紫晶珐琅环·紫水晶', 'default', 106),
(201, '子鼠', 'zodiac', '/assets/avatar-frames/zodiac-rat.apng', '#92A2B4', '灵鼠绕框奔跑', 'default', 201),
(202, '丑牛', 'zodiac', '/assets/avatar-frames/zodiac-ox.apng', '#AC8058', '憨厚丑牛轻轻晃动', 'default', 202),
(203, '寅虎', 'zodiac', '/assets/avatar-frames/zodiac-tiger.apng', '#DE9E56', '萌虎扑跳', 'default', 203),
(204, '卯兔', 'zodiac', '/assets/avatar-frames/zodiac-rabbit.apng', '#E6C0D4', '玉兔蹦跳', 'default', 204),
(205, '辰龙', 'zodiac', '/assets/avatar-frames/zodiac-dragon.apng', '#AC4032', '祥龙螺旋盘绕', 'default', 205),
(206, '巳蛇', 'zodiac', '/assets/avatar-frames/zodiac-snake.apng', '#5C9E6C', '灵蛇盘绕', 'default', 206),
(207, '午马', 'zodiac', '/assets/avatar-frames/zodiac-horse.apng', '#9C724E', '骏马点头小跑', 'default', 207),
(208, '未羊', 'zodiac', '/assets/avatar-frames/zodiac-goat.apng', '#C4B6A4', '吉羊轻轻顶撞', 'default', 208),
(209, '申猴', 'zodiac', '/assets/avatar-frames/zodiac-monkey.apng', '#926640', '顽猴悬挂荡悠', 'default', 209),
(210, '酉鸡', 'zodiac', '/assets/avatar-frames/zodiac-rooster.apng', '#D8BE98', '金鸡反复啄食', 'default', 210),
(211, '戌狗', 'zodiac', '/assets/avatar-frames/zodiac-dog.apng', '#B88C60', '忠犬绕圈追逐', 'default', 211),
(212, '亥猪', 'zodiac', '/assets/avatar-frames/zodiac-pig.apng', '#DE9CB0', '福猪上下慢晃', 'default', 212);

-- 根目录 data-h2.sql 的系统重复种子已由本模块与 common-service/common-system canonical 覆盖；旧乱码方言专用 SQL 不重复执行。
