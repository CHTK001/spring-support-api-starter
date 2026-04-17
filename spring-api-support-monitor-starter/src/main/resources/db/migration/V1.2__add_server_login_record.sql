-- 服务器登录记录表
CREATE TABLE IF NOT EXISTS `monitor_sys_gen_server_login_record` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `server_id` INT DEFAULT NULL COMMENT '服务器ID',
    `server_name` VARCHAR(128) DEFAULT NULL COMMENT '服务器名称',
    `host` VARCHAR(128) DEFAULT NULL COMMENT '服务器主机地址',
    `username` VARCHAR(64) DEFAULT NULL COMMENT '登录用户名',
    `login_type` VARCHAR(32) DEFAULT NULL COMMENT '登录方式（SSH/TELNET/RDP/VNC等）',
    `login_ip` VARCHAR(64) DEFAULT NULL COMMENT '登录IP地址',
    `status` VARCHAR(16) DEFAULT NULL COMMENT '登录状态（SUCCESS/FAILED）',
    `login_time` DATETIME DEFAULT NULL COMMENT '登录时间',
    `logout_time` DATETIME DEFAULT NULL COMMENT '登出时间',
    `duration` BIGINT DEFAULT NULL COMMENT '登录耗时（毫秒）',
    `message` VARCHAR(512) DEFAULT NULL COMMENT '登录结果描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_server_id` (`server_id`),
    INDEX `idx_login_time` (`login_time`),
    INDEX `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务器登录记录表';