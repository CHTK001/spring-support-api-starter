-- 文件系统相关表结构
-- 创建时间: 2024/12/30

-- 1. 文件系统主表
CREATE TABLE IF NOT EXISTS `file_system` (
    `file_system_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '文件ID',
    `file_system_name` varchar(255) DEFAULT NULL COMMENT '文件名称',
    `file_system_original_name` varchar(255) DEFAULT NULL COMMENT '文件原始名称',
    `file_system_path` varchar(500) DEFAULT NULL COMMENT '文件路径',
    `file_system_size` bigint(20) DEFAULT NULL COMMENT '文件大小(字节)',
    `file_system_md5` varchar(32) DEFAULT NULL COMMENT '文件MD5值',
    `file_system_type` varchar(100) DEFAULT NULL COMMENT '文件类型',
    `file_system_extension` varchar(10) DEFAULT NULL COMMENT '文件扩展名',
    `file_system_chunk_total` int(11) DEFAULT NULL COMMENT '分片总数',
    `file_system_chunk_uploaded` int(11) DEFAULT 0 COMMENT '已上传分片数',
    `file_system_status` int(11) DEFAULT 0 COMMENT '文件状态：0-待合并，1-合并中，2-已完成，3-合并失败',
    `file_system_http_access_enabled` tinyint(1) DEFAULT 0 COMMENT '是否启用HTTP访问',
    `file_system_http_access_url` varchar(500) DEFAULT NULL COMMENT 'HTTP访问URL',
    `file_system_upload_start_time` datetime DEFAULT NULL COMMENT '上传开始时间',
    `file_system_upload_complete_time` datetime DEFAULT NULL COMMENT '上传完成时间',
    `file_system_remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`file_system_id`),
    UNIQUE KEY `uk_file_system_md5` (`file_system_md5`),
    KEY `idx_file_system_status` (`file_system_status`),
    KEY `idx_file_system_type` (`file_system_type`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_file_system_http_access` (`file_system_http_access_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件系统管理表';

-- 2. 文件系统分片表
CREATE TABLE IF NOT EXISTS `file_system_part` (
    `file_system_part_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '分片ID',
    `file_system_part_file_id` int(11) NOT NULL COMMENT '文件ID',
    `file_system_part_chunk_number` int(11) NOT NULL COMMENT '分片序号(从0开始)',
    `file_system_part_chunk_size` bigint(20) DEFAULT NULL COMMENT '分片大小(字节)',
    `file_system_part_chunk_md5` varchar(32) DEFAULT NULL COMMENT '分片MD5值',
    `file_system_part_chunk_path` varchar(500) DEFAULT NULL COMMENT '分片文件路径',
    `file_system_part_chunk_status` int(11) DEFAULT 0 COMMENT '分片状态：0-待处理，1-处理中，2-已完成，3-处理失败',
    `file_system_part_upload_time` datetime DEFAULT NULL COMMENT '上传时间',
    `file_system_part_process_time` datetime DEFAULT NULL COMMENT '处理时间',
    `file_system_part_error_message` varchar(1000) DEFAULT NULL COMMENT '错误信息',
    `file_system_part_retry_count` int(11) DEFAULT 0 COMMENT '重试次数',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`file_system_part_id`),
    UNIQUE KEY `uk_file_system_part_chunk` (`file_system_part_file_id`, `file_system_part_chunk_number`),
    KEY `idx_file_system_part_file_id` (`file_system_part_file_id`),
    KEY `idx_file_system_part_chunk_status` (`file_system_part_chunk_status`),
    KEY `idx_file_system_part_upload_time` (`file_system_part_upload_time`),
    CONSTRAINT `fk_file_system_part_file_id` FOREIGN KEY (`file_system_part_file_id`) REFERENCES `file_system` (`file_system_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件系统分片管理表';

-- 3. 文件系统配置表
CREATE TABLE IF NOT EXISTS `file_system_setting` (
    `setting_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `setting_name` varchar(100) DEFAULT NULL COMMENT '配置名称',
    `chunk_upload_enabled` tinyint(1) DEFAULT 1 COMMENT '是否启用分片上传',
    `chunk_size_mb` int(11) DEFAULT 5 COMMENT '分片大小(MB)',
    `merge_task_limit` int(11) DEFAULT 1 COMMENT '合并任务数量限制',
    `manual_merge_enabled` tinyint(1) DEFAULT 1 COMMENT '是否开启手动合并',
    `download_enabled` tinyint(1) DEFAULT 1 COMMENT '是否启用文件下载',
    `http_access_enabled` tinyint(1) DEFAULT 0 COMMENT '是否启用HTTP访问',
    `http_access_domain` varchar(255) DEFAULT NULL COMMENT 'HTTP访问域名',
    `allowed_file_types` varchar(1000) DEFAULT NULL COMMENT '文件类型白名单(逗号分隔)',
    `max_file_size_mb` int(11) DEFAULT 1024 COMMENT '单个文件最大大小(MB)',
    `storage_root_path` varchar(500) DEFAULT NULL COMMENT '文件存储根路径',
    `temp_storage_path` varchar(500) DEFAULT NULL COMMENT '临时文件存储路径',
    `file_retention_days` int(11) DEFAULT 0 COMMENT '文件保留天数(0表示永久保留)',
    `enabled` tinyint(1) DEFAULT 0 COMMENT '是否启用',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`setting_id`),
    UNIQUE KEY `uk_setting_name` (`setting_name`),
    KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件系统配置管理表';

-- 插入默认配置
INSERT INTO `file_system_setting` (
    `setting_name`, `chunk_upload_enabled`, `chunk_size_mb`, `merge_task_limit`, 
    `manual_merge_enabled`, `download_enabled`, `http_access_enabled`, 
    `max_file_size_mb`, `file_retention_days`, `enabled`, `remark`
) VALUES (
    '默认配置', 1, 5, 1, 1, 1, 0, 1024, 0, 1, '系统默认文件管理配置'
) ON DUPLICATE KEY UPDATE 
    `setting_name` = VALUES(`setting_name`);

-- 创建索引优化查询性能
-- 文件系统主表索引
CREATE INDEX IF NOT EXISTS `idx_file_system_composite` ON `file_system` (`file_status`, `create_time`);
CREATE INDEX IF NOT EXISTS `idx_file_system_size` ON `file_system` (`file_size`);

-- 文件系统分片表索引
CREATE INDEX IF NOT EXISTS `idx_file_system_part_composite` ON `file_system_part` (`file_id`, `chunk_status`);
CREATE INDEX IF NOT EXISTS `idx_file_system_part_retry` ON `file_system_part` (`chunk_status`, `retry_count`);

-- 创建视图用于统计查询
CREATE OR REPLACE VIEW `v_file_system_statistics` AS
SELECT 
    COUNT(*) as total_files,
    SUM(CASE WHEN file_status = 0 THEN 1 ELSE 0 END) as pending_files,
    SUM(CASE WHEN file_status = 1 THEN 1 ELSE 0 END) as merging_files,
    SUM(CASE WHEN file_status = 2 THEN 1 ELSE 0 END) as completed_files,
    SUM(CASE WHEN file_status = 3 THEN 1 ELSE 0 END) as failed_files,
    SUM(CASE WHEN file_status = 2 THEN file_size ELSE 0 END) as total_size,
    AVG(CASE WHEN file_status = 2 THEN file_size ELSE NULL END) as avg_file_size,
    COUNT(CASE WHEN DATE(create_time) = CURDATE() THEN 1 ELSE NULL END) as today_files
FROM `file_system`;

-- 创建存储过程用于清理过期文件
DELIMITER $$
CREATE PROCEDURE IF NOT EXISTS `sp_cleanup_expired_files`(IN retention_days INT)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE file_id_var INT;
    DECLARE file_path_var VARCHAR(500);
    
    -- 声明游标
    DECLARE file_cursor CURSOR FOR 
        SELECT file_id, file_path 
        FROM file_system 
        WHERE file_retention_days > 0 
        AND DATEDIFF(NOW(), create_time) > retention_days;
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    -- 开始事务
    START TRANSACTION;
    
    -- 打开游标
    OPEN file_cursor;
    
    read_loop: LOOP
        FETCH file_cursor INTO file_id_var, file_path_var;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- 删除分片记录
        DELETE FROM file_system_part WHERE file_id = file_id_var;
        
        -- 删除文件记录
        DELETE FROM file_system WHERE file_id = file_id_var;
        
    END LOOP;
    
    -- 关闭游标
    CLOSE file_cursor;
    
    -- 提交事务
    COMMIT;
    
    -- 返回删除的文件数量
    SELECT ROW_COUNT() as deleted_count;
END$$
DELIMITER ;

-- 创建触发器用于自动更新统计信息
DELIMITER $$
CREATE TRIGGER IF NOT EXISTS `tr_file_system_after_insert` 
AFTER INSERT ON `file_system`
FOR EACH ROW
BEGIN
    -- 可以在这里添加统计信息更新逻辑
    -- 例如：更新缓存、发送通知等
    INSERT INTO `file_system_log` (file_id, operation, operation_time, remark)
    VALUES (NEW.file_id, 'INSERT', NOW(), CONCAT('文件创建: ', NEW.file_original_name));
END$$

CREATE TRIGGER IF NOT EXISTS `tr_file_system_after_update` 
AFTER UPDATE ON `file_system`
FOR EACH ROW
BEGIN
    -- 记录状态变更
    IF OLD.file_status != NEW.file_status THEN
        INSERT INTO `file_system_log` (file_id, operation, operation_time, remark)
        VALUES (NEW.file_id, 'STATUS_CHANGE', NOW(), 
                CONCAT('状态变更: ', OLD.file_status, ' -> ', NEW.file_status));
    END IF;
END$$
DELIMITER ;

-- 创建文件系统日志表（可选）
CREATE TABLE IF NOT EXISTS `file_system_log` (
    `log_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `file_id` int(11) DEFAULT NULL COMMENT '文件ID',
    `operation` varchar(50) DEFAULT NULL COMMENT '操作类型',
    `operation_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`log_id`),
    KEY `idx_file_id` (`file_id`),
    KEY `idx_operation_time` (`operation_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件系统操作日志表';
