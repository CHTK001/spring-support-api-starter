-- 预览扩展名配置表
-- 用于配置 ViewServletFilter 的扩展名白名单/黑名单
-- @author CH
-- @since 2024/12/08

CREATE TABLE IF NOT EXISTS `system_server_setting_preview_extension` (
    `system_server_setting_preview_extension_id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `preview_extension_server_id` INT(11) NOT NULL COMMENT '所属服务器ID',
    `preview_extension_disabled` VARCHAR(2000) DEFAULT NULL COMMENT '禁用预览的扩展名列表（逗号分隔）',
    `preview_extension_allowed` VARCHAR(2000) DEFAULT NULL COMMENT '允许预览的扩展名列表（逗号分隔）',
    `preview_extension_whitelist_mode` TINYINT(1) DEFAULT 0 COMMENT '是否启用白名单模式（0=黑名单，1=白名单）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`system_server_setting_preview_extension_id`),
    UNIQUE KEY `uk_server_id` (`preview_extension_server_id`),
    KEY `idx_server_id` (`preview_extension_server_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='预览扩展名配置表';
