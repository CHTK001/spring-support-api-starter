-- 租户表
CREATE TABLE IF NOT EXISTS `sys_tenant` (
    `sys_tenant_id` INT NOT NULL AUTO_INCREMENT COMMENT '租户ID',
    `sys_tenant_gid` VARCHAR(64) DEFAULT NULL COMMENT '租户全局唯一ID',
    `sys_tenant_code` VARCHAR(64) DEFAULT NULL COMMENT '租户编码',
    `sys_tenant_name` VARCHAR(128) NOT NULL COMMENT '租户名称',
    `sys_tenant_username` VARCHAR(64) DEFAULT NULL COMMENT '租户账号',
    `sys_tenant_password` VARCHAR(256) DEFAULT NULL COMMENT '租户密码',
    `sys_tenant_phone` VARCHAR(32) DEFAULT NULL COMMENT '联系电话',
    `sys_tenant_email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    `sys_tenant_avatar` VARCHAR(512) DEFAULT NULL COMMENT '头像',
    `sys_tenant_status` TINYINT DEFAULT 0 COMMENT '状态: 0-启用, 1-禁用',
    `sys_tenant_delete` TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`sys_tenant_id`),
    UNIQUE KEY `uk_tenant_code` (`sys_tenant_code`),
    UNIQUE KEY `uk_tenant_gid` (`sys_tenant_gid`),
    KEY `idx_tenant_name` (`sys_tenant_name`),
    KEY `idx_tenant_status` (`sys_tenant_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户表';

-- 服务表
CREATE TABLE IF NOT EXISTS `sys_service` (
    `sys_service_id` INT NOT NULL AUTO_INCREMENT COMMENT '服务ID',
    `sys_service_code` VARCHAR(64) DEFAULT NULL COMMENT '服务编码',
    `sys_service_name` VARCHAR(128) NOT NULL COMMENT '服务名称',
    `sys_service_desc` VARCHAR(512) DEFAULT NULL COMMENT '服务描述',
    `sys_service_icon` VARCHAR(256) DEFAULT NULL COMMENT '服务图标',
    `sys_service_sort` INT DEFAULT 0 COMMENT '排序',
    `sys_service_status` TINYINT DEFAULT 0 COMMENT '状态: 0-启用, 1-禁用',
    `sys_service_delete` TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`sys_service_id`),
    UNIQUE KEY `uk_service_code` (`sys_service_code`),
    KEY `idx_service_name` (`sys_service_name`),
    KEY `idx_service_status` (`sys_service_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务表';

-- 服务模块表
CREATE TABLE IF NOT EXISTS `sys_service_module` (
    `sys_service_module_id` INT NOT NULL AUTO_INCREMENT COMMENT '服务模块ID',
    `sys_service_id` INT NOT NULL COMMENT '服务ID',
    `sys_service_module_code` VARCHAR(64) DEFAULT NULL COMMENT '模块编码',
    `sys_service_module_name` VARCHAR(128) NOT NULL COMMENT '模块名称',
    `sys_service_module_desc` VARCHAR(512) DEFAULT NULL COMMENT '模块描述',
    `sys_service_module_sort` INT DEFAULT 0 COMMENT '排序',
    `sys_service_module_status` TINYINT DEFAULT 0 COMMENT '状态: 0-启用, 1-禁用',
    `sys_service_module_delete` TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`sys_service_module_id`),
    KEY `idx_service_id` (`sys_service_id`),
    KEY `idx_module_code` (`sys_service_module_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务模块表';

-- 服务模块关联项表 (服务与菜单的关联)
CREATE TABLE IF NOT EXISTS `sys_service_module_item` (
    `sys_service_module_item_id` INT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    `sys_service_id` INT NOT NULL COMMENT '服务ID',
    `sys_service_module_id` INT DEFAULT NULL COMMENT '服务模块ID',
    `sys_menu_id` INT NOT NULL COMMENT '菜单ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`sys_service_module_item_id`),
    KEY `idx_service_id` (`sys_service_id`),
    KEY `idx_module_id` (`sys_service_module_id`),
    KEY `idx_menu_id` (`sys_menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务模块关联项表';

-- 租户服务关联表
CREATE TABLE IF NOT EXISTS `sys_tenant_service` (
    `sys_tenant_service_id` INT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    `sys_tenant_id` INT NOT NULL COMMENT '租户ID',
    `sys_service_id` INT NOT NULL COMMENT '服务ID',
    `sys_tenant_service_valid_time` DATE DEFAULT NULL COMMENT '服务到期时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`sys_tenant_service_id`),
    KEY `idx_tenant_id` (`sys_tenant_id`),
    KEY `idx_service_id` (`sys_service_id`),
    UNIQUE KEY `uk_tenant_service` (`sys_tenant_id`, `sys_service_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户服务关联表';
