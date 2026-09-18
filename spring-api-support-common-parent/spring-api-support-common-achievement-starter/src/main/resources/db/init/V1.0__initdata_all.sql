-- =====================================================================
-- V1.0__initdata_all.sql - spring-api-support-common-achievement-starter 全量种子（合并自 initdata 文件 + init 内嵌种子）
-- 通道: INITDATA 虚拟线程异步；全部幂等（INSERT IGNORE / WHERE NOT EXISTS）。
-- =====================================================================

-- ---------------------------------------------------------------------
-- 来源: V1.0__initdata_achievement.sql
-- ---------------------------------------------------------------------
-- 登录成就
INSERT IGNORE INTO `achievement_tier` (`type`, `level`, `name`, `badge`, `description`, `rule`, `sort`) VALUES
('login', 1, '初次登录', '🌱', '第一次登录系统', 'loginCount >= 1', 1),
('login', 2, '登录达人 Ⅰ', '🏅', '累计登录 7 天', 'loginCount >= 7', 2),
('login', 3, '登录达人 Ⅱ', '🎖️', '累计登录 30 天', 'loginCount >= 30', 3),
('login', 4, '登录达人 Ⅲ', '🏆', '累计登录 100 天', 'loginCount >= 100', 4);

-- 签到成就
INSERT IGNORE INTO `achievement_tier` (`type`, `level`, `name`, `badge`, `description`, `rule`, `sort`) VALUES
('sign', 1, '签到新秀', '📌', '累计签到 3 天', 'signInCount >= 3', 1),
('sign', 2, '签到达人', '📅', '累计签到 7 天', 'signInCount >= 7', 2),
('sign', 3, '签到精英', '🔥', '累计签到 30 天', 'signInCount >= 30', 3),
('sign', 4, '签到大师', '💎', '累计签到 100 天', 'signInCount >= 100', 4);

-- 菜单成就
INSERT IGNORE INTO `achievement_tier` (`type`, `level`, `name`, `badge`, `description`, `rule`, `sort`) VALUES
('menu', 1, '菜单探索者', '🔍', '累计浏览菜单 10 次', 'menuVisitedCount >= 10', 1),
('menu', 2, '菜单达人', '📋', '累计浏览菜单 50 次', 'menuVisitedCount >= 50', 2),
('menu', 3, '菜单专家', '🗂️', '累计浏览菜单 200 次', 'menuVisitedCount >= 200', 3);

-- 消息成就
INSERT IGNORE INTO `achievement_tier` (`type`, `level`, `name`, `badge`, `description`, `rule`, `sort`) VALUES
('message', 1, '初识消息', '💬', '累计发送 10 条消息', 'messageSentCount >= 10', 1),
('message', 2, '消息达人', '📣', '累计发送 100 条消息', 'messageSentCount >= 100', 2),
('message', 3, '消息高手', '🎙️', '累计发送 1000 条消息', 'messageSentCount >= 1000', 3);

-- 头像框成就
INSERT IGNORE INTO `achievement_tier` (`type`, `level`, `name`, `badge`, `description`, `rule`, `sort`) VALUES
('frame', 1, '头像收集者', '🖼️', '拥有 3 个头像框', 'frameOwnedCount >= 3', 1),
('frame', 2, '头像收藏家', '🎨', '拥有 10 个头像框', 'frameOwnedCount >= 10', 2),
('frame', 3, '头像大师', '👑', '拥有 30 个头像框', 'frameOwnedCount >= 30', 3);

