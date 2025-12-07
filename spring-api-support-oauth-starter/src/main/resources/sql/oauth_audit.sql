-- =============================================
-- OAuth 审计日志与Token黑名单 初始化SQL
-- 版本: 4.0.0
-- 作者: CH
-- 更新时间: 2024/12/07
-- =============================================

-- 1. 审计日志表增强（为现有 sys_user_log 添加索引）
-- 创建用户名索引，加速按用户查询
CREATE INDEX IF NOT EXISTS idx_sys_log_username ON sys_user_log (sys_log_username);

-- 创建创建时间索引，加速按时间范围查询
CREATE INDEX IF NOT EXISTS idx_sys_log_create_time ON sys_user_log (create_time);

-- 创建状态索引，加速按登录状态查询
CREATE INDEX IF NOT EXISTS idx_sys_log_status ON sys_user_log (sys_log_status);

-- 创建登录类型索引
CREATE INDEX IF NOT EXISTS idx_sys_log_login_type ON sys_user_log (sys_log_login_type);

-- 2. Token黑名单表
CREATE TABLE IF NOT EXISTS sys_token_blacklist
(
    sys_blacklist_id      BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    sys_token_value       VARCHAR(500)  NOT NULL COMMENT 'Token值',
    sys_token_hash        VARCHAR(64)   NOT NULL COMMENT 'Token哈希值(用于快速查询)',
    sys_user_id           VARCHAR(255)  NULL COMMENT '用户ID',
    sys_username          VARCHAR(255)  NULL COMMENT '用户名',
    sys_blacklist_reason  VARCHAR(255)  NULL COMMENT '加入黑名单原因: LOGOUT-登出, KICKED-被踢出, EXPIRED-过期, REVOKED-撤销',
    sys_blacklist_source  VARCHAR(100)  NULL COMMENT '来源: USER-用户主动, ADMIN-管理员操作, SYSTEM-系统自动',
    sys_expire_time       DATETIME      NOT NULL COMMENT '过期时间(与原Token过期时间一致)',
    sys_client_ip         VARCHAR(50)   NULL COMMENT '操作时的客户端IP',
    sys_user_agent        VARCHAR(500)  NULL COMMENT '操作时的User-Agent',
    create_time           DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_token_hash (sys_token_hash),
    INDEX idx_user_id (sys_user_id),
    INDEX idx_expire_time (sys_expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
  COMMENT='Token黑名单表' ROW_FORMAT=DYNAMIC;

-- 3. 审计日志统计视图（可选，用于统计分析）
CREATE OR REPLACE VIEW v_user_login_stats AS
SELECT 
    sys_log_username AS username,
    COUNT(*) AS total_count,
    SUM(CASE WHEN sys_log_status = 1 THEN 1 ELSE 0 END) AS success_count,
    SUM(CASE WHEN sys_log_status = 0 THEN 1 ELSE 0 END) AS fail_count,
    MAX(create_time) AS last_login_time,
    COUNT(DISTINCT sys_log_ip) AS unique_ip_count
FROM sys_user_log
WHERE sys_log_from = 'LOGIN'
GROUP BY sys_log_username;

-- 4. 清理过期Token黑名单的存储过程
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS sp_clean_expired_token_blacklist()
BEGIN
    DELETE FROM sys_token_blacklist WHERE sys_expire_time < NOW();
    SELECT ROW_COUNT() AS deleted_count;
END //
DELIMITER ;

-- 5. 创建定时清理事件（每天凌晨2点执行）
-- 注意：需要先开启事件调度器 SET GLOBAL event_scheduler = ON;
CREATE EVENT IF NOT EXISTS evt_clean_token_blacklist
ON SCHEDULE EVERY 1 DAY
STARTS CURRENT_DATE + INTERVAL 1 DAY + INTERVAL 2 HOUR
DO CALL sp_clean_expired_token_blacklist();
