-- =====================================================================
-- V1.0__initdata_all.sql - spring-api-support-common-signin-starter 全量种子（合并自 initdata 文件 + init 内嵌种子）
-- 通道: INITDATA 虚拟线程异步；全部幂等（INSERT IGNORE / WHERE NOT EXISTS）。
-- =====================================================================

-- ---------------------------------------------------------------------
-- 来源: V1.0__initdata_signin.sql
-- ---------------------------------------------------------------------
-- 默认积分规则
INSERT IGNORE INTO `sign_in_points_rule` (`type`, `points`, `description`, `status`) VALUES
('DAILY_SIGN', 10, '每日签到基础积分', 1),
('STREAK_BONUS_3', 5, '连续签到3天额外奖励', 1),
('STREAK_BONUS_7', 10, '连续签到7天额外奖励', 1),
('STREAK_BONUS_30', 50, '连续签到30天额外奖励', 1);

