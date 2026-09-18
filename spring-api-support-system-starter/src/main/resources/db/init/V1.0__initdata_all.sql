-- =====================================================================
-- V1.0__initdata_all.sql - spring-api-support-system-starter 全量种子（合并自 initdata 文件 + init 内嵌种子）
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
INSERT IGNORE INTO `sys_menu` (
    `sys_menu_id`, `sys_menu_name`, `sys_menu_path`, `sys_menu_i18n`, `sys_menu_title`, `sys_menu_perm`,
    `sys_menu_pid`, `sys_menu_tree_id`, `sys_menu_component`, `sys_menu_icon`, `sys_menu_type`, `sys_menu_sort`,
    `sys_menu_hidden`, `sys_menu_show_parent`, `sys_menu_hidden_tag`, `sys_menu_fixed_tag`, `sys_menu_redirect`,
    `sys_menu_keep_alive`, `create_name`, `create_by`, `create_time`, `update_time`, `update_name`, `update_by`,
    `sys_menu_role`, `sys_menu_home`, `sys_menu_engine`
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
(162, 2, 19);

-- =====================================================
-- 3. 默认系统账号
-- 账号: sa / admin / ops
-- 密码: 123456 (前端先 MD5，再由后端执行 MD5(md5(password)+salt))
-- =====================================================
INSERT IGNORE INTO `sys_user` (`sys_user_id`, `sys_user_username`, `sys_user_nickname`, `sys_user_password`, `sys_user_salt`, `sys_user_delete`, `sys_user_status`, `sys_user_in_system`) VALUES
(1, 'sa', '超级管理员', '5f1d7a84db00d2fce00b31a7fc73224f', '123456', 0, 1, 1),
(2, 'admin', '管理员', '5f1d7a84db00d2fce00b31a7fc73224f', '123456', 0, 1, 1),
(3, 'ops', '运维', '5f1d7a84db00d2fce00b31a7fc73224f', '123456', 0, 1, 1);

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
-- 6. 头像框种子（渐变 1-22、图片 23-29、生日 100）
-- =====================================================
INSERT IGNORE INTO `sys_avatar_frame` (`sys_avatar_frame_id`, `sys_avatar_frame_name`, `sys_avatar_frame_type`, `sys_avatar_frame_color`, `sys_avatar_frame_desc`, `sys_avatar_frame_acquire`, `sys_avatar_frame_sort`) VALUES
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

INSERT IGNORE INTO `sys_avatar_frame` (`sys_avatar_frame_id`, `sys_avatar_frame_name`, `sys_avatar_frame_type`, `sys_avatar_frame_image`, `sys_avatar_frame_desc`, `sys_avatar_frame_acquire`, `sys_avatar_frame_sort`) VALUES
(23, '鎏金', 'base', '/assets/avatar-frames/frame-gold.png', '金色图腾环', 'default', 22),
(24, '皇室蓝', 'base', '/assets/avatar-frames/frame-royal.png', '经典皇室蓝环', 'default', 23),
(25, '玫瑰金', 'base', '/assets/avatar-frames/frame-rose.png', '优雅玫瑰金环', 'default', 24),
(26, '焰光', 'base', '/assets/avatar-frames/frame-flame.apng', '火焰呼吸动画(APNG)', 'default', 25),
(27, '晶蓝', 'base', '/assets/avatar-frames/frame-azure.apng', '晶蓝呼吸动画(APNG)', 'default', 26),
(28, '玄黑', 'base', '/assets/avatar-frames/frame-onyx.png', '幽黑玄金环', 'default', 27),
(29, '像素风', 'base', '/assets/avatar-frames/frame-pixel.png', '复古像素风边框', 'default', 28);

INSERT IGNORE INTO `sys_avatar_frame` (`sys_avatar_frame_id`, `sys_avatar_frame_name`, `sys_avatar_frame_type`, `sys_avatar_frame_image`, `sys_avatar_frame_desc`, `sys_avatar_frame_acquire`, `sys_avatar_frame_sort`, `sys_avatar_frame_status`) VALUES
(100, '生日快乐', 'birthday', '/assets/avatar-frames/birthday.png', '生日专属限定头像框', 'birthday', 99, 1);

