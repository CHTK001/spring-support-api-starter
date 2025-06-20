-- 创建文件上传管理相关表

-- 文件上传任务表
CREATE TABLE IF NOT EXISTS monitor_sys_gen_file_upload_tasks (
    monitor_sys_gen_file_upload_task_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '任务ID',
    monitor_sys_gen_file_upload_task_name VARCHAR(255) NOT NULL COMMENT '任务名称',
    monitor_sys_gen_file_upload_file_name VARCHAR(255) NOT NULL COMMENT '文件名称',
    monitor_sys_gen_file_upload_file_path VARCHAR(1000) NOT NULL COMMENT '文件路径',
    monitor_sys_gen_file_upload_file_size BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
    monitor_sys_gen_file_upload_file_md5 VARCHAR(32) COMMENT '文件MD5值',
    monitor_sys_gen_file_upload_target_server_ids TEXT COMMENT '目标服务器ID列表(JSON格式)',
    monitor_sys_gen_file_upload_target_paths TEXT COMMENT '目标路径列表(JSON格式)',
    monitor_sys_gen_file_upload_mode VARCHAR(20) DEFAULT 'REALTIME' COMMENT '上传模式(REALTIME:实时 SCHEDULED:定时)',
    monitor_sys_gen_file_upload_type VARCHAR(20) DEFAULT 'SINGLE' COMMENT '上传类型(SINGLE:单文件 BATCH:批量)',
    monitor_sys_gen_file_upload_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '任务状态(PENDING:待处理 PROCESSING:处理中 COMPLETED:已完成 FAILED:失败 CANCELLED:已取消)',
    monitor_sys_gen_file_upload_scheduled_time DATETIME COMMENT '定时执行时间',
    monitor_sys_gen_file_upload_priority INT DEFAULT 5 COMMENT '优先级(1-10，数字越小优先级越高)',
    monitor_sys_gen_file_upload_retry_count INT DEFAULT 0 COMMENT '重试次数',
    monitor_sys_gen_file_upload_max_retry INT DEFAULT 3 COMMENT '最大重试次数',
    monitor_sys_gen_file_upload_timeout_seconds INT DEFAULT 300 COMMENT '超时时间(秒)',
    monitor_sys_gen_file_upload_overwrite_enabled TINYINT DEFAULT 0 COMMENT '是否覆盖已存在文件(0:否 1:是)',
    monitor_sys_gen_file_upload_backup_enabled TINYINT DEFAULT 1 COMMENT '是否备份原文件(0:否 1:是)',
    monitor_sys_gen_file_upload_compress_enabled TINYINT DEFAULT 0 COMMENT '是否压缩传输(0:否 1:是)',
    monitor_sys_gen_file_upload_verify_enabled TINYINT DEFAULT 1 COMMENT '是否验证文件完整性(0:否 1:是)',
    monitor_sys_gen_file_upload_description TEXT COMMENT '任务描述',
    monitor_sys_gen_file_upload_tags VARCHAR(500) COMMENT '标签(JSON格式)',
    monitor_sys_gen_file_upload_create_user VARCHAR(100) COMMENT '创建用户',
    monitor_sys_gen_file_upload_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    monitor_sys_gen_file_upload_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    monitor_sys_gen_file_upload_start_time DATETIME COMMENT '开始执行时间',
    monitor_sys_gen_file_upload_end_time DATETIME COMMENT '结束时间',
    monitor_sys_gen_file_upload_task_status TINYINT DEFAULT 1 COMMENT '记录状态(0:删除 1:正常)',
    
    INDEX idx_task_name (monitor_sys_gen_file_upload_task_name),
    INDEX idx_status (monitor_sys_gen_file_upload_status),
    INDEX idx_mode (monitor_sys_gen_file_upload_mode),
    INDEX idx_type (monitor_sys_gen_file_upload_type),
    INDEX idx_scheduled_time (monitor_sys_gen_file_upload_scheduled_time),
    INDEX idx_priority (monitor_sys_gen_file_upload_priority),
    INDEX idx_create_time (monitor_sys_gen_file_upload_create_time),
    INDEX idx_create_user (monitor_sys_gen_file_upload_create_user),
    INDEX idx_task_status (monitor_sys_gen_file_upload_task_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件上传任务表';

-- 文件上传记录表
CREATE TABLE IF NOT EXISTS monitor_sys_gen_file_upload_records (
    monitor_sys_gen_file_upload_record_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    monitor_sys_gen_file_upload_task_id BIGINT NOT NULL COMMENT '关联任务ID',
    monitor_sys_gen_server_id INT NOT NULL COMMENT '目标服务器ID',
    monitor_sys_gen_file_upload_target_path VARCHAR(1000) COMMENT '目标路径',
    monitor_sys_gen_file_upload_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '上传状态(PENDING:待上传 UPLOADING:上传中 COMPLETED:已完成 FAILED:失败 CANCELLED:已取消)',
    monitor_sys_gen_file_upload_progress INT DEFAULT 0 COMMENT '上传进度(0-100)',
    monitor_sys_gen_file_upload_speed BIGINT DEFAULT 0 COMMENT '上传速度(字节/秒)',
    monitor_sys_gen_file_upload_transferred_bytes BIGINT DEFAULT 0 COMMENT '已传输字节数',
    monitor_sys_gen_file_upload_total_bytes BIGINT DEFAULT 0 COMMENT '总字节数',
    monitor_sys_gen_file_upload_start_time DATETIME COMMENT '开始上传时间',
    monitor_sys_gen_file_upload_end_time DATETIME COMMENT '结束时间',
    monitor_sys_gen_file_upload_duration_ms BIGINT COMMENT '上传耗时(毫秒)',
    monitor_sys_gen_file_upload_error_message TEXT COMMENT '错误信息',
    monitor_sys_gen_file_upload_error_code VARCHAR(50) COMMENT '错误代码',
    monitor_sys_gen_file_upload_retry_count INT DEFAULT 0 COMMENT '重试次数',
    monitor_sys_gen_file_upload_file_md5_remote VARCHAR(32) COMMENT '远程文件MD5值',
    monitor_sys_gen_file_upload_verification_status VARCHAR(20) COMMENT '验证状态(PENDING:待验证 PASSED:通过 FAILED:失败)',
    monitor_sys_gen_file_upload_backup_path VARCHAR(1000) COMMENT '备份文件路径',
    monitor_sys_gen_file_upload_connection_info TEXT COMMENT '连接信息(JSON格式)',
    monitor_sys_gen_file_upload_extra_data TEXT COMMENT '额外数据(JSON格式)',
    monitor_sys_gen_file_upload_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    monitor_sys_gen_file_upload_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    monitor_sys_gen_file_upload_record_status TINYINT DEFAULT 1 COMMENT '记录状态(0:删除 1:正常)',
    
    INDEX idx_task_id (monitor_sys_gen_file_upload_task_id),
    INDEX idx_server_id (monitor_sys_gen_server_id),
    INDEX idx_status (monitor_sys_gen_file_upload_status),
    INDEX idx_progress (monitor_sys_gen_file_upload_progress),
    INDEX idx_start_time (monitor_sys_gen_file_upload_start_time),
    INDEX idx_create_time (monitor_sys_gen_file_upload_create_time),
    INDEX idx_record_status (monitor_sys_gen_file_upload_record_status),
    INDEX idx_task_server (monitor_sys_gen_file_upload_task_id, monitor_sys_gen_server_id),
    
    FOREIGN KEY (monitor_sys_gen_file_upload_task_id) REFERENCES monitor_sys_gen_file_upload_tasks(monitor_sys_gen_file_upload_task_id) ON DELETE CASCADE,
    FOREIGN KEY (monitor_sys_gen_server_id) REFERENCES monitor_sys_gen_server(monitor_sys_gen_server_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件上传记录表';

-- 文件上传统计表
CREATE TABLE IF NOT EXISTS monitor_sys_gen_file_upload_statistics (
    monitor_sys_gen_file_upload_stat_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '统计ID',
    monitor_sys_gen_server_id INT COMMENT '服务器ID(NULL表示全局统计)',
    monitor_sys_gen_file_upload_stat_date DATE NOT NULL COMMENT '统计日期',
    monitor_sys_gen_file_upload_stat_hour TINYINT COMMENT '统计小时(0-23，NULL表示日统计)',
    monitor_sys_gen_file_upload_task_count INT DEFAULT 0 COMMENT '任务总数',
    monitor_sys_gen_file_upload_success_count INT DEFAULT 0 COMMENT '成功任务数',
    monitor_sys_gen_file_upload_failed_count INT DEFAULT 0 COMMENT '失败任务数',
    monitor_sys_gen_file_upload_cancelled_count INT DEFAULT 0 COMMENT '取消任务数',
    monitor_sys_gen_file_upload_total_size_bytes BIGINT DEFAULT 0 COMMENT '总上传大小(字节)',
    monitor_sys_gen_file_upload_total_duration_ms BIGINT DEFAULT 0 COMMENT '总耗时(毫秒)',
    monitor_sys_gen_file_upload_avg_speed_bps BIGINT DEFAULT 0 COMMENT '平均上传速度(字节/秒)',
    monitor_sys_gen_file_upload_max_speed_bps BIGINT DEFAULT 0 COMMENT '最大上传速度(字节/秒)',
    monitor_sys_gen_file_upload_min_speed_bps BIGINT DEFAULT 0 COMMENT '最小上传速度(字节/秒)',
    monitor_sys_gen_file_upload_retry_total_count INT DEFAULT 0 COMMENT '总重试次数',
    monitor_sys_gen_file_upload_stat_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    monitor_sys_gen_file_upload_stat_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_server_date_hour (monitor_sys_gen_server_id, monitor_sys_gen_file_upload_stat_date, monitor_sys_gen_file_upload_stat_hour),
    INDEX idx_server_id (monitor_sys_gen_server_id),
    INDEX idx_stat_date (monitor_sys_gen_file_upload_stat_date),
    INDEX idx_create_time (monitor_sys_gen_file_upload_stat_create_time),
    
    FOREIGN KEY (monitor_sys_gen_server_id) REFERENCES monitor_sys_gen_server(monitor_sys_gen_server_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件上传统计表';

-- 文件上传配置表
CREATE TABLE IF NOT EXISTS monitor_sys_gen_file_upload_config (
    monitor_sys_gen_file_upload_config_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配置ID',
    monitor_sys_gen_server_id INT COMMENT '服务器ID(NULL表示全局配置)',
    monitor_sys_gen_file_upload_max_concurrent INT DEFAULT 5 COMMENT '最大并发上传数',
    monitor_sys_gen_file_upload_max_file_size_mb INT DEFAULT 1024 COMMENT '最大文件大小(MB)',
    monitor_sys_gen_file_upload_allowed_extensions TEXT COMMENT '允许的文件扩展名(JSON格式)',
    monitor_sys_gen_file_upload_forbidden_extensions TEXT COMMENT '禁止的文件扩展名(JSON格式)',
    monitor_sys_gen_file_upload_default_timeout INT DEFAULT 300 COMMENT '默认超时时间(秒)',
    monitor_sys_gen_file_upload_default_retry INT DEFAULT 3 COMMENT '默认重试次数',
    monitor_sys_gen_file_upload_chunk_size_kb INT DEFAULT 1024 COMMENT '分块大小(KB)',
    monitor_sys_gen_file_upload_verify_enabled TINYINT DEFAULT 1 COMMENT '是否启用文件验证(0:否 1:是)',
    monitor_sys_gen_file_upload_compress_enabled TINYINT DEFAULT 0 COMMENT '是否启用压缩传输(0:否 1:是)',
    monitor_sys_gen_file_upload_backup_enabled TINYINT DEFAULT 1 COMMENT '是否启用备份(0:否 1:是)',
    monitor_sys_gen_file_upload_cleanup_days INT DEFAULT 30 COMMENT '清理天数',
    monitor_sys_gen_file_upload_notification_enabled TINYINT DEFAULT 1 COMMENT '是否启用通知(0:否 1:是)',
    monitor_sys_gen_file_upload_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    monitor_sys_gen_file_upload_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    monitor_sys_gen_file_upload_config_status TINYINT DEFAULT 1 COMMENT '配置状态(0:禁用 1:启用)',
    
    UNIQUE KEY uk_server_id (monitor_sys_gen_server_id),
    INDEX idx_config_status (monitor_sys_gen_file_upload_config_status),
    INDEX idx_update_time (monitor_sys_gen_file_upload_update_time),
    
    FOREIGN KEY (monitor_sys_gen_server_id) REFERENCES monitor_sys_gen_server(monitor_sys_gen_server_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件上传配置表';

-- 插入默认全局配置
INSERT IGNORE INTO monitor_sys_gen_file_upload_config (
    monitor_sys_gen_server_id,
    monitor_sys_gen_file_upload_max_concurrent,
    monitor_sys_gen_file_upload_max_file_size_mb,
    monitor_sys_gen_file_upload_default_timeout,
    monitor_sys_gen_file_upload_default_retry,
    monitor_sys_gen_file_upload_chunk_size_kb
) VALUES (
    NULL,
    5,
    1024,
    300,
    3,
    1024
);
