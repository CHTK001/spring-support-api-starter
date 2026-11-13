-- =====================================================================
-- V1.0__initdata_all_api_common_system.sql - spring-api-support-common-system-starter 全量种子（合并自 initdata 文件 + init 内嵌种子）
-- 通道: INITDATA 虚拟线程异步；全部幂等（INSERT IGNORE / WHERE NOT EXISTS）。
-- =====================================================================

-- ---------------------------------------------------------------------
-- 来源: 用户域种子自 spring-api-support-system-starter 下沉
-- 建表与账号种子必须随 SysUser/SysRole/SysDept 的代码一起归口，
-- 否则嵌入本模块的宿主（如采集调度平台）没有可登录账号。
-- 账号: sa / admin / ops，口令 admin123，摘要算法 md5(明文 + salt) 见 PasswordUtils。
-- 部署时必须改密。
-- ---------------------------------------------------------------------

INSERT IGNORE INTO `sys_dept` (`sys_dept_id`, `sys_dept_name`, `sys_dept_pid`, `sys_dept_tree_id`, `sys_dept_code`, `sys_dept_sort`, `sys_dept_status`) VALUES
(1, '总部', 0, '0,1', 'ROOT', 1, 0);

INSERT IGNORE INTO `sys_role` (`sys_role_id`, `sys_role_name`, `sys_role_code`, `sys_role_in_system`, `sys_role_status`, `sys_role_sort`, `sys_role_remark`, `sys_role_hidden`) VALUES
(1, '超级管理员', 'SUPER_ADMIN', 1, 1, 1, '系统超级管理员，拥有所有权限', 0),
(2, '管理员', 'ADMIN', 1, 1, 2, '系统管理员', 0),
(3, '运维', 'OPS', 1, 1, 3, '运维人员', 0),
(4, '普通用户', 'USER', 0, 1, 4, '普通用户', 0),
(5, '部门负责人', 'DEPT_LEADER', 1, 1, 5, '负责部门及子部门账号管理', 0),
(6, '游客', 'GUEST', 1, 1, 6, '游客账号', 0);

INSERT IGNORE INTO `sys_user` (`sys_user_id`, `sys_user_username`, `sys_user_nickname`, `sys_user_password`, `sys_user_salt`, `sys_user_delete`, `sys_user_status`, `sys_user_in_system`, `sys_dept_id`) VALUES
(1, 'sa', '超级管理员', 'c466f189d277fc0e6702ec3fe5ea18d7', '123456', 0, 1, 1, 1),
(2, 'admin', '管理员', 'c466f189d277fc0e6702ec3fe5ea18d7', '123456', 0, 1, 1, 1),
(3, 'ops', '运维', 'c466f189d277fc0e6702ec3fe5ea18d7', '123456', 0, 1, 1, 1);

INSERT IGNORE INTO `sys_user_role` (`sys_user_role_id`, `sys_user_id`, `sys_role_id`) VALUES
(1, 1, 1),
(2, 2, 2),
(3, 3, 3);

-- ---------------------------------------------------------------------
-- 策略配置归一：sys_firewall_ip_blackwhite / sys_firewall_rate_limit_rule / sys_firewall_account_blackwhite
--               → sys_firewall_config（统一策略配置表，一行 = 一条配置）
--
-- 三张旧表由 sys_firewall_config 取代，数据按 type 判别列归位：
--   sys_firewall_ip_blackwhite      → type='IP'         biz_key=sys_ip_ip
--   sys_firewall_rate_limit_rule    → type='RATE_LIMIT' biz_key=target, dimension=dimension
--   sys_firewall_account_blackwhite → type='ACCOUNT'    biz_key=account
--
-- 【语义归一，务必注意】IP 表与账号表的取值方向是相反的，不能直接搬：
--   IP     : sys_ip_type   0=白名单 1=黑名单   → list_type 2=白名单 1=黑名单
--            sys_ip_status 0=启用   1=禁用     → status   1=启用   0=禁用
--   ACCOUNT: type          1=黑名单 2=白名单   → list_type 同向，直搬
--            status        1=启用   0=禁用     → status   同向，直搬
-- 漏掉这一步会让黑白名单与启停状态整体反转，且不报错。
--
-- 说明：
-- 1) 迁移与建表分属 init / initdata 两个 Flyway 通道，init 仍会建这三张空表，
--    以保证下面的 INSERT..SELECT 在全新库中也不会因表不存在而报错。
-- 2) 迁移完成后由本文件末尾统一 DROP，全新库最终不残留这三张表。
-- 3) 开发期数据库截断重建，本段等价于「一次性搬迁」；不需要、也不应为此
--    新增 V1.x 增量脚本。改表结构一律改 V1.0__init_all 的 CREATE TABLE。
-- ---------------------------------------------------------------------

-- IP 黑白名单 → type='IP'（type/status 需反向归一）
INSERT IGNORE INTO `sys_firewall_config`
(`type`, `dimension`, `list_type`, `biz_key`, `status`, `sort`, `remark`, `create_name`, `create_by`, `create_time`, `update_name`, `update_by`, `update_time`)
SELECT 'IP', '',
       CASE WHEN `sys_ip_type` = 1 THEN 1 ELSE 2 END,
       `sys_ip_ip`,
       CASE WHEN `sys_ip_status` = 0 THEN 1 ELSE 0 END,
       0, `sys_ip_remark`,
       `create_name`, `create_by`, `create_time`, `update_name`, `update_by`, `update_time`
FROM `sys_firewall_ip_blackwhite`
WHERE `sys_ip_ip` IS NOT NULL;

-- 限流规则 → type='RATE_LIMIT'
INSERT IGNORE INTO `sys_firewall_config`
(`type`, `dimension`, `list_type`, `biz_key`, `status`, `qps`, `burst_size`, `sort`, `remark`, `create_name`, `create_by`, `create_time`, `update_name`, `update_by`, `update_time`)
SELECT 'RATE_LIMIT', `dimension`, 0, `target`, COALESCE(`enabled`, 1), `qps`, `burst_size`, 0, `remark`,
       `create_name`, `create_by`, `create_time`, `update_name`, `update_by`, `update_time`
FROM `sys_firewall_rate_limit_rule`
WHERE `target` IS NOT NULL;

-- 账号黑白名单 → type='ACCOUNT'（type/status 与目标列同向，直搬）
INSERT IGNORE INTO `sys_firewall_config`
(`type`, `dimension`, `list_type`, `biz_key`, `status`, `sort`, `remark`, `create_name`, `create_by`, `create_time`, `update_name`, `update_by`, `update_time`)
SELECT 'ACCOUNT', '', COALESCE(`type`, 1), `account`, COALESCE(`status`, 1), 0, `remark`,
       `create_name`, `create_by`, `create_time`, `update_name`, `update_by`, `update_time`
FROM `sys_firewall_account_blackwhite`
WHERE `account` IS NOT NULL;

-- 数据搬运完成后销毁遗留表。此后 sys_firewall_config 是唯一的策略配置来源。
DROP TABLE IF EXISTS `sys_firewall_ip_blackwhite`;
DROP TABLE IF EXISTS `sys_firewall_rate_limit_rule`;
DROP TABLE IF EXISTS `sys_firewall_account_blackwhite`;

-- ============================================================
-- V6.3 壁纸目录种子数据（内置 6 套）
--
-- 全部是 CSS 渐变 / 图案，不依赖图片资源：
-- 一套位图壁纸动辄数 MB，塞进发行包不划算；渐变的任意分辨率都不糊，
-- 也方便部署侧后续在库里换成照片（kind 改 image、填 url 即可）。
--
-- 幂等：按 code 判重，重复执行不会插入第二份。
-- 与前端 builtinWallpapers.ts 的兜底清单保持一致。
-- ============================================================

INSERT INTO `sys_wallpaper` (`sys_wallpaper_code`, `sys_wallpaper_name`, `sys_wallpaper_group`, `sys_wallpaper_kind`, `sys_wallpaper_css`, `sys_wallpaper_css_dark`, `sys_wallpaper_tile`, `sys_wallpaper_opacity`, `sys_wallpaper_blur`, `sys_wallpaper_dim`, `sys_wallpaper_sort`, `sys_wallpaper_status`)
SELECT 'paper', '素纸', '素雅', 'css', 'repeating-linear-gradient(0deg, rgba(120,96,64,0.045) 0 1px, transparent 1px 28px), linear-gradient(180deg, #fbf8f2 0%, #f3ede1 100%)', 'repeating-linear-gradient(0deg, rgba(255,255,255,0.05) 0 1px, transparent 1px 28px), linear-gradient(180deg, #1b1a18 0%, #121110 100%)', 'none', 1.00, 0, 0.00, 10, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_wallpaper` WHERE `sys_wallpaper_code` = 'paper');

INSERT INTO `sys_wallpaper` (`sys_wallpaper_code`, `sys_wallpaper_name`, `sys_wallpaper_group`, `sys_wallpaper_kind`, `sys_wallpaper_css`, `sys_wallpaper_css_dark`, `sys_wallpaper_tile`, `sys_wallpaper_opacity`, `sys_wallpaper_blur`, `sys_wallpaper_dim`, `sys_wallpaper_sort`, `sys_wallpaper_status`)
SELECT 'bamboo', '青竹', '雅致', 'css', 'repeating-linear-gradient(94deg, rgba(74,124,89,0.10) 0 2px, transparent 2px 46px), radial-gradient(120% 90% at 12% 0%, #eaf4ea 0%, #dcecdf 45%, #cfe4d5 100%)', 'repeating-linear-gradient(94deg, rgba(140,200,160,0.10) 0 2px, transparent 2px 46px), radial-gradient(120% 90% at 12% 0%, #10201a 0%, #0c1712 55%, #091210 100%)', 'none', 1.00, 0, 0.00, 20, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_wallpaper` WHERE `sys_wallpaper_code` = 'bamboo');

INSERT INTO `sys_wallpaper` (`sys_wallpaper_code`, `sys_wallpaper_name`, `sys_wallpaper_group`, `sys_wallpaper_kind`, `sys_wallpaper_css`, `sys_wallpaper_css_dark`, `sys_wallpaper_tile`, `sys_wallpaper_opacity`, `sys_wallpaper_blur`, `sys_wallpaper_dim`, `sys_wallpaper_sort`, `sys_wallpaper_status`)
SELECT 'distant-hills', '远山', '雅致', 'css', 'radial-gradient(120% 62% at 18% 100%, rgba(109,93,252,0.16) 0%, transparent 62%), radial-gradient(90% 50% at 82% 100%, rgba(47,191,113,0.14) 0%, transparent 60%), linear-gradient(180deg, #f6f7fb 0%, #eef1f8 60%, #e7ebf6 100%)', 'radial-gradient(120% 62% at 18% 100%, rgba(125,109,255,0.20) 0%, transparent 62%), radial-gradient(90% 50% at 82% 100%, rgba(47,191,113,0.16) 0%, transparent 60%), linear-gradient(180deg, #14151f 0%, #101119 60%, #0c0d14 100%)', 'none', 1.00, 0, 0.00, 30, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_wallpaper` WHERE `sys_wallpaper_code` = 'distant-hills');

INSERT INTO `sys_wallpaper` (`sys_wallpaper_code`, `sys_wallpaper_name`, `sys_wallpaper_group`, `sys_wallpaper_kind`, `sys_wallpaper_css`, `sys_wallpaper_css_dark`, `sys_wallpaper_tile`, `sys_wallpaper_opacity`, `sys_wallpaper_blur`, `sys_wallpaper_dim`, `sys_wallpaper_sort`, `sys_wallpaper_status`)
SELECT 'ink', '墨痕', '雅致', 'css', 'radial-gradient(42% 34% at 24% 18%, rgba(31,36,48,0.10) 0%, transparent 70%), radial-gradient(38% 30% at 78% 76%, rgba(31,36,48,0.08) 0%, transparent 70%), linear-gradient(160deg, #f7f7f9 0%, #eceef3 100%)', 'radial-gradient(42% 34% at 24% 18%, rgba(255,255,255,0.06) 0%, transparent 70%), radial-gradient(38% 30% at 78% 76%, rgba(255,255,255,0.05) 0%, transparent 70%), linear-gradient(160deg, #1a1b22 0%, #111219 100%)', 'none', 1.00, 0, 0.00, 40, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_wallpaper` WHERE `sys_wallpaper_code` = 'ink');

INSERT INTO `sys_wallpaper` (`sys_wallpaper_code`, `sys_wallpaper_name`, `sys_wallpaper_group`, `sys_wallpaper_kind`, `sys_wallpaper_css`, `sys_wallpaper_css_dark`, `sys_wallpaper_tile`, `sys_wallpaper_opacity`, `sys_wallpaper_blur`, `sys_wallpaper_dim`, `sys_wallpaper_sort`, `sys_wallpaper_status`)
SELECT 'warm-dusk', '暖霞', '暖色', 'css', 'radial-gradient(90% 70% at 80% 0%, rgba(245,165,36,0.20) 0%, transparent 60%), radial-gradient(80% 60% at 10% 10%, rgba(240,74,93,0.14) 0%, transparent 58%), linear-gradient(180deg, #fdf7f2 0%, #f7eef0 100%)', 'radial-gradient(90% 70% at 80% 0%, rgba(245,165,36,0.16) 0%, transparent 60%), radial-gradient(80% 60% at 10% 10%, rgba(240,74,93,0.12) 0%, transparent 58%), linear-gradient(180deg, #1f1913 0%, #161014 100%)', 'none', 1.00, 0, 0.00, 50, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_wallpaper` WHERE `sys_wallpaper_code` = 'warm-dusk');

INSERT INTO `sys_wallpaper` (`sys_wallpaper_code`, `sys_wallpaper_name`, `sys_wallpaper_group`, `sys_wallpaper_kind`, `sys_wallpaper_css`, `sys_wallpaper_css_dark`, `sys_wallpaper_tile`, `sys_wallpaper_opacity`, `sys_wallpaper_blur`, `sys_wallpaper_dim`, `sys_wallpaper_sort`, `sys_wallpaper_status`)
SELECT 'starry', '星野', '夜色', 'css', 'radial-gradient(circle at 22% 28%, rgba(255,255,255,0.92) 0 1.2px, transparent 1.8px), radial-gradient(circle at 68% 62%, rgba(255,255,255,0.70) 0 1px, transparent 1.6px), radial-gradient(120% 90% at 50% 0%, #2b3a5e 0%, #182238 60%, #0b0f1a 100%)', 'radial-gradient(circle at 22% 28%, rgba(255,255,255,0.92) 0 1.2px, transparent 1.8px), radial-gradient(circle at 68% 62%, rgba(255,255,255,0.70) 0 1px, transparent 1.6px), radial-gradient(120% 90% at 50% 0%, #1a2440 0%, #0e1526 60%, #070a12 100%)', 'none', 0.95, 0, 0.25, 60, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_wallpaper` WHERE `sys_wallpaper_code` = 'starry');

-- ---------------------------------------------------------------------
-- 在线图片壁纸（需要联网）
-- 前端会监听网络状态：离线时这两条在面板里隐藏、图层暂时撤下，联网后自动恢复。
-- 用 Unsplash 固定图片 id，避免每次加载随机换图。
-- ---------------------------------------------------------------------
INSERT INTO `sys_wallpaper` (`sys_wallpaper_code`, `sys_wallpaper_name`, `sys_wallpaper_group`, `sys_wallpaper_kind`, `sys_wallpaper_css`, `sys_wallpaper_css_dark`, `sys_wallpaper_url`, `sys_wallpaper_tile`, `sys_wallpaper_opacity`, `sys_wallpaper_blur`, `sys_wallpaper_dim`, `sys_wallpaper_sort`, `sys_wallpaper_status`)
SELECT 'online-lake', '远山湖', '在线', 'image', NULL, NULL, 'https://images.unsplash.com/photo-1501785888041-af3ef285b470?w=1920&q=80&auto=format&fit=crop', 'cover', 1.00, 0, 0.15, 70, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_wallpaper` WHERE `sys_wallpaper_code` = 'online-lake');

INSERT INTO `sys_wallpaper` (`sys_wallpaper_code`, `sys_wallpaper_name`, `sys_wallpaper_group`, `sys_wallpaper_kind`, `sys_wallpaper_css`, `sys_wallpaper_css_dark`, `sys_wallpaper_url`, `sys_wallpaper_tile`, `sys_wallpaper_opacity`, `sys_wallpaper_blur`, `sys_wallpaper_dim`, `sys_wallpaper_sort`, `sys_wallpaper_status`)
SELECT 'online-forest', '雾林', '在线', 'image', NULL, NULL, 'https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=1920&q=80&auto=format&fit=crop', 'cover', 1.00, 0, 0.15, 80, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_wallpaper` WHERE `sys_wallpaper_code` = 'online-forest');

INSERT INTO `sys_wallpaper` (`sys_wallpaper_code`, `sys_wallpaper_name`, `sys_wallpaper_group`, `sys_wallpaper_kind`, `sys_wallpaper_css`, `sys_wallpaper_css_dark`, `sys_wallpaper_url`, `sys_wallpaper_tile`, `sys_wallpaper_opacity`, `sys_wallpaper_blur`, `sys_wallpaper_dim`, `sys_wallpaper_sort`, `sys_wallpaper_status`)
SELECT 'online-snow', '雪峰', '在线', 'image', NULL, NULL, 'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=1920&q=80&auto=format&fit=crop', 'cover', 1.00, 0, 0.15, 90, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_wallpaper` WHERE `sys_wallpaper_code` = 'online-snow');

-- ---------------------------------------------------------------------
-- 饰品：头像挂件（字符）+ 昵称牌（CSS 背景）
-- 只放「不依赖图片资源」的种子，部署后即可佩戴；后续限定款再补 image。
-- ---------------------------------------------------------------------
INSERT INTO `sys_ornament` (`sys_ornament_code`, `sys_ornament_name`, `sys_ornament_slot`, `sys_ornament_kind`, `sys_ornament_glyph`, `sys_ornament_css`, `sys_ornament_color`, `sys_ornament_sort`, `sys_ornament_status`)
SELECT 'avatar-star', '星芒', 'avatar', 'glyph', '★', NULL, '#f59e0b', 10, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_ornament` WHERE `sys_ornament_code` = 'avatar-star');

INSERT INTO `sys_ornament` (`sys_ornament_code`, `sys_ornament_name`, `sys_ornament_slot`, `sys_ornament_kind`, `sys_ornament_glyph`, `sys_ornament_css`, `sys_ornament_color`, `sys_ornament_sort`, `sys_ornament_status`)
SELECT 'avatar-sakura', '樱瓣', 'avatar', 'glyph', '❀', NULL, '#f472b6', 20, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_ornament` WHERE `sys_ornament_code` = 'avatar-sakura');

INSERT INTO `sys_ornament` (`sys_ornament_code`, `sys_ornament_name`, `sys_ornament_slot`, `sys_ornament_kind`, `sys_ornament_glyph`, `sys_ornament_css`, `sys_ornament_color`, `sys_ornament_sort`, `sys_ornament_status`)
SELECT 'avatar-spark', '星辉', 'avatar', 'glyph', '✦', NULL, '#38bdf8', 30, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_ornament` WHERE `sys_ornament_code` = 'avatar-spark');

INSERT INTO `sys_ornament` (`sys_ornament_code`, `sys_ornament_name`, `sys_ornament_slot`, `sys_ornament_kind`, `sys_ornament_glyph`, `sys_ornament_css`, `sys_ornament_color`, `sys_ornament_sort`, `sys_ornament_status`)
SELECT 'avatar-moon', '月华', 'avatar', 'glyph', '☾', NULL, '#818cf8', 40, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_ornament` WHERE `sys_ornament_code` = 'avatar-moon');

INSERT INTO `sys_ornament` (`sys_ornament_code`, `sys_ornament_name`, `sys_ornament_slot`, `sys_ornament_kind`, `sys_ornament_glyph`, `sys_ornament_css`, `sys_ornament_color`, `sys_ornament_sort`, `sys_ornament_status`)
SELECT 'plate-gold', '鎏金', 'nameplate', 'css', NULL, 'linear-gradient(135deg, rgba(245,158,11,0.30), rgba(245,158,11,0.08))', NULL, 110, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_ornament` WHERE `sys_ornament_code` = 'plate-gold');

INSERT INTO `sys_ornament` (`sys_ornament_code`, `sys_ornament_name`, `sys_ornament_slot`, `sys_ornament_kind`, `sys_ornament_glyph`, `sys_ornament_css`, `sys_ornament_color`, `sys_ornament_sort`, `sys_ornament_status`)
SELECT 'plate-ocean', '海盐', 'nameplate', 'css', NULL, 'linear-gradient(135deg, rgba(56,189,248,0.28), rgba(56,189,248,0.06))', NULL, 120, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_ornament` WHERE `sys_ornament_code` = 'plate-ocean');

INSERT INTO `sys_ornament` (`sys_ornament_code`, `sys_ornament_name`, `sys_ornament_slot`, `sys_ornament_kind`, `sys_ornament_glyph`, `sys_ornament_css`, `sys_ornament_color`, `sys_ornament_sort`, `sys_ornament_status`)
SELECT 'plate-rose', '玫瑰', 'nameplate', 'css', NULL, 'linear-gradient(135deg, rgba(244,114,182,0.28), rgba(244,114,182,0.06))', NULL, 130, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_ornament` WHERE `sys_ornament_code` = 'plate-rose');

INSERT INTO `sys_ornament` (`sys_ornament_code`, `sys_ornament_name`, `sys_ornament_slot`, `sys_ornament_kind`, `sys_ornament_glyph`, `sys_ornament_css`, `sys_ornament_color`, `sys_ornament_sort`, `sys_ornament_status`)
SELECT 'plate-ink', '墨玉', 'nameplate', 'css', NULL, 'linear-gradient(135deg, rgba(148,163,184,0.30), rgba(148,163,184,0.06))', NULL, 140, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_ornament` WHERE `sys_ornament_code` = 'plate-ink');

