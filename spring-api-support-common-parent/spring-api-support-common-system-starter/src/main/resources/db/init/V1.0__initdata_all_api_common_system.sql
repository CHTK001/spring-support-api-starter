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
-- 策略配置归一：sys_ip_blackwhite / sys_rate_limit_rule / sys_account_blackwhite
--               → sys_policy_config（统一策略配置表，一行 = 一条配置）
--
-- 三张旧表由 sys_policy_config 取代，数据按 type 判别列归位：
--   sys_ip_blackwhite      → type='IP'         biz_key=sys_ip_ip
--   sys_rate_limit_rule    → type='RATE_LIMIT' biz_key=target, dimension=dimension
--   sys_account_blackwhite → type='ACCOUNT'    biz_key=account
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
INSERT IGNORE INTO `sys_policy_config`
(`type`, `dimension`, `list_type`, `biz_key`, `status`, `sort`, `remark`, `create_name`, `create_by`, `create_time`, `update_name`, `update_by`, `update_time`)
SELECT 'IP', '',
       CASE WHEN `sys_ip_type` = 1 THEN 1 ELSE 2 END,
       `sys_ip_ip`,
       CASE WHEN `sys_ip_status` = 0 THEN 1 ELSE 0 END,
       0, `sys_ip_remark`,
       `create_name`, `create_by`, `create_time`, `update_name`, `update_by`, `update_time`
FROM `sys_ip_blackwhite`
WHERE `sys_ip_ip` IS NOT NULL;

-- 限流规则 → type='RATE_LIMIT'
INSERT IGNORE INTO `sys_policy_config`
(`type`, `dimension`, `list_type`, `biz_key`, `status`, `qps`, `burst_size`, `sort`, `remark`, `create_name`, `create_by`, `create_time`, `update_name`, `update_by`, `update_time`)
SELECT 'RATE_LIMIT', `dimension`, 0, `target`, COALESCE(`enabled`, 1), `qps`, `burst_size`, 0, `remark`,
       `create_name`, `create_by`, `create_time`, `update_name`, `update_by`, `update_time`
FROM `sys_rate_limit_rule`
WHERE `target` IS NOT NULL;

-- 账号黑白名单 → type='ACCOUNT'（type/status 与目标列同向，直搬）
INSERT IGNORE INTO `sys_policy_config`
(`type`, `dimension`, `list_type`, `biz_key`, `status`, `sort`, `remark`, `create_name`, `create_by`, `create_time`, `update_name`, `update_by`, `update_time`)
SELECT 'ACCOUNT', '', COALESCE(`type`, 1), `account`, COALESCE(`status`, 1), 0, `remark`,
       `create_name`, `create_by`, `create_time`, `update_name`, `update_by`, `update_time`
FROM `sys_account_blackwhite`
WHERE `account` IS NOT NULL;

-- 数据搬运完成后销毁遗留表。此后 sys_policy_config 是唯一的策略配置来源。
DROP TABLE IF EXISTS `sys_ip_blackwhite`;
DROP TABLE IF EXISTS `sys_rate_limit_rule`;
DROP TABLE IF EXISTS `sys_account_blackwhite`;
