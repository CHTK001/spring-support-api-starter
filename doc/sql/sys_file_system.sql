-- =====================================================
-- 文件系统管理表结构
-- 作者: CH
-- 创建时间: 2024/12/10
-- 版本: 1.0.0
-- =====================================================

-- -----------------------------------------------------
-- 删除旧表 (如果存在)
-- -----------------------------------------------------
DROP TABLE IF EXISTS `sys_file_system_part`;
DROP TABLE IF EXISTS `sys_file_system`;
DROP TABLE IF EXISTS `sys_file_system_setting`;
DROP TABLE IF EXISTS `sys_file_system_group`;

-- 删除旧的 SysTaskSso 相关表
DROP TABLE IF EXISTS `sys_task_sso_item`;
DROP TABLE IF EXISTS `sys_task_sso`;

-- -----------------------------------------------------
-- 1. 文件系统分组表
-- -----------------------------------------------------
CREATE TABLE `sys_file_system_group` (
    `sys_file_system_group_id` INT NOT NULL AUTO_INCREMENT COMMENT '分组ID',
    `sys_file_system_group_name` VARCHAR(100) NULL COMMENT '分组名称',
    `sys_file_system_group_parent_id` INT DEFAULT 0 COMMENT '父分组ID，顶级分组为0',
    `sys_file_system_group_path` VARCHAR(255) NULL COMMENT '分组路径',
    `sys_file_system_group_full_path` VARCHAR(500) NULL COMMENT '分组完整路径',
    `sys_file_system_group_level` INT DEFAULT 1 COMMENT '分组层级，从1开始',
    `sys_file_system_group_tree_path` VARCHAR(500) NULL COMMENT '分组树路径（ID链路）',
    `sys_file_system_group_description` VARCHAR(500) NULL COMMENT '分组描述',
    `sys_file_system_group_is_default` TINYINT(1) DEFAULT 0 COMMENT '是否默认分组',
    `sys_file_system_group_sort` INT DEFAULT 0 COMMENT '分组排序',
    `sys_file_system_group_status` INT DEFAULT 1 COMMENT '分组状态: 0-禁用, 1-启用',
    `sys_file_system_group_icon` VARCHAR(100) NULL COMMENT '分组图标',
    `sys_file_system_group_color` VARCHAR(20) NULL COMMENT '分组颜色',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) NULL COMMENT '创建人',
    `update_by` VARCHAR(64) NULL COMMENT '更新人',
    PRIMARY KEY (`sys_file_system_group_id`),
    INDEX `idx_parent_id` (`sys_file_system_group_parent_id`),
    INDEX `idx_status` (`sys_file_system_group_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件系统分组表';

-- -----------------------------------------------------
-- 2. 文件系统配置表
-- -----------------------------------------------------
CREATE TABLE `sys_file_system_setting` (
    `sys_file_system_setting_id` INT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `sys_file_system_setting_name` VARCHAR(100) NULL COMMENT '配置名称',
    `sys_file_system_setting_chunk_enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用分片上传',
    `sys_file_system_setting_chunk_size_mb` INT DEFAULT 100 COMMENT '分片大小(MB)',
    `sys_file_system_setting_auto_merge_enabled` TINYINT(1) DEFAULT 1 COMMENT '是否开启自动合并',
    `sys_file_system_setting_merge_task_limit` INT DEFAULT 10 COMMENT '合并任务数量限制',
    `sys_file_system_setting_manual_merge_enabled` TINYINT(1) DEFAULT 1 COMMENT '是否支持手动合并功能',
    `sys_file_system_setting_http_server_enabled` TINYINT(1) DEFAULT 0 COMMENT '是否开启HTTP服务',
    `sys_file_system_setting_http_server_port` INT DEFAULT 8888 COMMENT 'HTTP服务端口',
    `sys_file_system_setting_http_server_host` VARCHAR(100) DEFAULT '0.0.0.0' COMMENT 'HTTP服务主机',
    `sys_file_system_setting_http_server_context` VARCHAR(100) DEFAULT '/file' COMMENT 'HTTP服务上下文路径',
    `sys_file_system_setting_download_enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用文件下载',
    `sys_file_system_setting_preview_enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用文件预览',
    `sys_file_system_setting_http_access_domain` VARCHAR(255) NULL COMMENT 'HTTP访问域名',
    `sys_file_system_setting_allowed_types` VARCHAR(1000) NULL COMMENT '文件类型白名单(逗号分隔)',
    `sys_file_system_setting_max_file_size_mb` INT DEFAULT 1024 COMMENT '单个文件最大大小(MB)',
    `sys_file_system_setting_storage_root_path` VARCHAR(500) NULL COMMENT '文件存储根路径',
    `sys_file_system_setting_temp_path` VARCHAR(500) NULL COMMENT '临时文件存储路径',
    `sys_file_system_setting_retention_days` INT DEFAULT 0 COMMENT '文件保留天数(0表示永久保留)',
    `sys_file_system_setting_enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    `sys_file_system_setting_default_storage_type` VARCHAR(50) DEFAULT 'local' COMMENT '默认存储类型',
    `sys_file_system_setting_remark` VARCHAR(500) NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) NULL COMMENT '创建人',
    `update_by` VARCHAR(64) NULL COMMENT '更新人',
    PRIMARY KEY (`sys_file_system_setting_id`),
    INDEX `idx_enabled` (`sys_file_system_setting_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件系统配置表';

-- -----------------------------------------------------
-- 3. 文件系统主表
-- -----------------------------------------------------
CREATE TABLE `sys_file_system` (
    `sys_file_system_id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `sys_file_system_tid` VARCHAR(64) NULL COMMENT '任务唯一标识（ULID）',
    `sys_file_system_bucket` VARCHAR(255) NULL COMMENT '存储桶/存储位置',
    `sys_file_system_name` VARCHAR(255) NULL COMMENT '文件名称',
    `sys_file_system_original_name` VARCHAR(255) NULL COMMENT '文件原始名称',
    `sys_file_system_type` VARCHAR(50) NULL COMMENT '存储类型: local, oss, cos, s3等',
    `sys_file_system_extension` VARCHAR(20) NULL COMMENT '文件扩展名',
    `sys_file_system_path` VARCHAR(500) NULL COMMENT '文件路径',
    `sys_file_system_size` BIGINT NULL COMMENT '文件大小(字节)',
    `sys_file_system_md5` VARCHAR(32) NULL COMMENT '文件MD5值',
    `sys_file_system_chunk_total` INT NULL COMMENT '分片数量',
    `sys_file_system_chunk_uploaded` INT DEFAULT 0 COMMENT '已上传分片数',
    `sys_file_system_chunk_size` BIGINT NULL COMMENT '分片大小(字节)',
    `sys_file_system_version` INT DEFAULT 0 COMMENT '版本号(乐观锁)',
    `sys_file_system_status` INT DEFAULT 0 COMMENT '状态: 0-待上传, 1-上传中, 2-待合并, 3-合并中, 4-已完成, -1-处理异常',
    `sys_file_system_http_enabled` TINYINT(1) DEFAULT 0 COMMENT '是否启用HTTP访问',
    `sys_file_system_http_url` VARCHAR(500) NULL COMMENT 'HTTP访问URL',
    `sys_file_system_service_id` INT NULL COMMENT '绑定的服务ID',
    `sys_file_system_group_id` INT NULL COMMENT '文件分组ID',
    `sys_file_system_upload_start_time` DATETIME NULL COMMENT '上传开始时间',
    `sys_file_system_upload_complete_time` DATETIME NULL COMMENT '上传完成时间',
    `sys_file_system_local_path` VARCHAR(500) NULL COMMENT '本地临时文件路径',
    `sys_file_system_summary` VARCHAR(500) NULL COMMENT '描述/摘要',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) NULL COMMENT '创建人',
    `update_by` VARCHAR(64) NULL COMMENT '更新人',
    PRIMARY KEY (`sys_file_system_id`),
    UNIQUE INDEX `uk_tid` (`sys_file_system_tid`),
    INDEX `idx_status` (`sys_file_system_status`),
    INDEX `idx_group_id` (`sys_file_system_group_id`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件系统主表';

-- -----------------------------------------------------
-- 4. 文件系统分片表
-- -----------------------------------------------------
CREATE TABLE `sys_file_system_part` (
    `sys_file_system_part_id` INT NOT NULL AUTO_INCREMENT COMMENT '分片ID',
    `sys_file_system_part_file_id` INT NOT NULL COMMENT '文件ID',
    `sys_file_system_part_seq` INT NOT NULL COMMENT '分片序号(从1开始)',
    `sys_file_system_part_size` BIGINT NULL COMMENT '分片大小(字节)',
    `sys_file_system_part_md5` VARCHAR(32) NULL COMMENT '分片MD5值',
    `sys_file_system_part_local_path` VARCHAR(500) NULL COMMENT '分片本地存储路径',
    `sys_file_system_part_version` INT DEFAULT 0 COMMENT '版本号(乐观锁)',
    `sys_file_system_part_status` INT DEFAULT 0 COMMENT '状态: 0-待上传, 1-上传中, 2-已完成, -1-处理异常',
    `sys_file_system_part_upload_time` DATETIME NULL COMMENT '上传时间',
    `sys_file_system_part_process_time` DATETIME NULL COMMENT '处理时间',
    `sys_file_system_part_error_message` VARCHAR(1000) NULL COMMENT '错误信息',
    `sys_file_system_part_retry_count` INT DEFAULT 0 COMMENT '重试次数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) NULL COMMENT '创建人',
    `update_by` VARCHAR(64) NULL COMMENT '更新人',
    PRIMARY KEY (`sys_file_system_part_id`),
    INDEX `idx_file_id` (`sys_file_system_part_file_id`),
    INDEX `idx_status` (`sys_file_system_part_status`),
    CONSTRAINT `fk_part_file` FOREIGN KEY (`sys_file_system_part_file_id`) 
        REFERENCES `sys_file_system` (`sys_file_system_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件系统分片表';

-- -----------------------------------------------------
-- 初始化默认数据
-- -----------------------------------------------------

-- 插入默认配置
INSERT INTO `sys_file_system_setting` (
    `sys_file_system_setting_name`,
    `sys_file_system_setting_chunk_enabled`,
    `sys_file_system_setting_chunk_size_mb`,
    `sys_file_system_setting_auto_merge_enabled`,
    `sys_file_system_setting_merge_task_limit`,
    `sys_file_system_setting_manual_merge_enabled`,
    `sys_file_system_setting_http_server_enabled`,
    `sys_file_system_setting_http_server_port`,
    `sys_file_system_setting_http_server_host`,
    `sys_file_system_setting_http_server_context`,
    `sys_file_system_setting_download_enabled`,
    `sys_file_system_setting_preview_enabled`,
    `sys_file_system_setting_max_file_size_mb`,
    `sys_file_system_setting_retention_days`,
    `sys_file_system_setting_enabled`,
    `sys_file_system_setting_default_storage_type`
) VALUES (
    '默认配置',
    1, -- 启用分片上传
    100, -- 分片大小 100MB
    1, -- 启用自动合并
    10, -- 合并任务限制
    1, -- 支持手动合并
    0, -- 默认不开启HTTP服务
    8888, -- HTTP端口
    '0.0.0.0', -- HTTP主机
    '/file', -- 上下文路径
    1, -- 启用下载
    1, -- 启用预览
    1024, -- 最大文件大小 1GB
    0, -- 永久保留
    1, -- 启用
    'local' -- 本地存储
);

-- 插入默认分组
INSERT INTO `sys_file_system_group` (
    `sys_file_system_group_name`,
    `sys_file_system_group_parent_id`,
    `sys_file_system_group_path`,
    `sys_file_system_group_full_path`,
    `sys_file_system_group_level`,
    `sys_file_system_group_tree_path`,
    `sys_file_system_group_is_default`,
    `sys_file_system_group_sort`,
    `sys_file_system_group_status`
) VALUES 
('默认分组', 0, 'default', '/default', 1, 'default', 1, 1, 1),
('图片', 0, 'images', '/images', 1, 'images', 0, 2, 1),
('视频', 0, 'videos', '/videos', 1, 'videos', 0, 3, 1),
('文档', 0, 'documents', '/documents', 1, 'documents', 0, 4, 1),
('音频', 0, 'audios', '/audios', 1, 'audios', 0, 5, 1);
