-- SysToken表结构
CREATE TABLE IF NOT EXISTS `sys_token` (
    `sys_token_id` INT(11) NOT NULL AUTO_INCREMENT COMMENT 'Token ID',
    `sys_token_app_key` VARCHAR(64) NOT NULL COMMENT 'AppKey（应用标识）',
    `sys_token_app_secret` VARCHAR(128) NOT NULL COMMENT 'AppSecret（应用密钥）',
    `sys_token_app_name` VARCHAR(255) DEFAULT NULL COMMENT '应用名称',
    `sys_token_user_id` VARCHAR(64) DEFAULT NULL COMMENT '关联用户ID',
    `sys_token_status` TINYINT(1) DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
    `sys_token_expire_time` DATETIME DEFAULT NULL COMMENT '过期时间',
    `sys_token_create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `sys_token_update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `sys_token_remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`sys_token_id`),
    UNIQUE KEY `uk_app_key` (`sys_token_app_key`),
    KEY `idx_user_id` (`sys_token_user_id`),
    KEY `idx_status` (`sys_token_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AppKey Token表';

-- 示例数据
INSERT INTO `sys_token` (`sys_token_app_key`, `sys_token_app_secret`, `sys_token_app_name`, `sys_token_user_id`, `sys_token_status`, `sys_token_remark`)
VALUES ('test_app_key', 'test_app_secret_123456', '测试应用', '1', 1, '测试用AppKey');
