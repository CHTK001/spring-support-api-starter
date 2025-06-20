-- 创建服务器日志管理相关表

-- 服务器日志表
CREATE TABLE IF NOT EXISTS monitor_sys_gen_server_logs (
    monitor_sys_gen_server_log_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    monitor_sys_gen_server_id INT NOT NULL COMMENT '关联服务器ID',
    monitor_sys_gen_server_log_content TEXT COMMENT '日志内容',
    monitor_sys_gen_server_log_level VARCHAR(20) DEFAULT 'INFO' COMMENT '日志级别(DEBUG,INFO,WARN,ERROR,FATAL)',
    monitor_sys_gen_server_log_source VARCHAR(50) COMMENT '日志来源(SYSTEM,APPLICATION,SECURITY,ACCESS)',
    monitor_sys_gen_server_log_category VARCHAR(50) COMMENT '日志分类',
    monitor_sys_gen_server_log_thread VARCHAR(100) COMMENT '线程名称',
    monitor_sys_gen_server_log_logger VARCHAR(200) COMMENT '日志记录器名称',
    monitor_sys_gen_server_log_file_path VARCHAR(500) COMMENT '日志文件路径',
    monitor_sys_gen_server_log_line_number INT COMMENT '日志行号',
    monitor_sys_gen_server_log_exception_stack TEXT COMMENT '异常堆栈信息',
    monitor_sys_gen_server_log_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    monitor_sys_gen_server_log_server_time DATETIME COMMENT '服务器时间',
    monitor_sys_gen_server_log_ip VARCHAR(50) COMMENT '服务器IP',
    monitor_sys_gen_server_log_hostname VARCHAR(100) COMMENT '主机名',
    monitor_sys_gen_server_log_process_id VARCHAR(20) COMMENT '进程ID',
    monitor_sys_gen_server_log_tags VARCHAR(500) COMMENT '日志标签(JSON格式)',
    monitor_sys_gen_server_log_extra_data TEXT COMMENT '额外数据(JSON格式)',
    monitor_sys_gen_server_log_status TINYINT DEFAULT 1 COMMENT '状态(0:删除 1:正常)',
    
    INDEX idx_server_id (monitor_sys_gen_server_id),
    INDEX idx_log_level (monitor_sys_gen_server_log_level),
    INDEX idx_log_source (monitor_sys_gen_server_log_source),
    INDEX idx_create_time (monitor_sys_gen_server_log_create_time),
    INDEX idx_server_time (monitor_sys_gen_server_log_server_time),
    INDEX idx_status (monitor_sys_gen_server_log_status),
    INDEX idx_server_level_time (monitor_sys_gen_server_id, monitor_sys_gen_server_log_level, monitor_sys_gen_server_log_create_time),
    
    FOREIGN KEY (monitor_sys_gen_server_id) REFERENCES monitor_sys_gen_server(monitor_sys_gen_server_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务器日志表';

-- 服务器日志配置表
CREATE TABLE IF NOT EXISTS monitor_sys_gen_server_log_config (
    monitor_sys_gen_server_log_config_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配置ID',
    monitor_sys_gen_server_id INT NOT NULL COMMENT '关联服务器ID',
    monitor_sys_gen_server_log_retention_days INT DEFAULT 30 COMMENT '日志保留天数',
    monitor_sys_gen_server_log_enabled TINYINT DEFAULT 1 COMMENT '是否启用日志收集(0:否 1:是)',
    monitor_sys_gen_server_log_level_filter VARCHAR(100) DEFAULT 'INFO,WARN,ERROR,FATAL' COMMENT '日志级别过滤器',
    monitor_sys_gen_server_log_source_filter VARCHAR(200) COMMENT '日志来源过滤器',
    monitor_sys_gen_server_log_max_size_mb INT DEFAULT 100 COMMENT '单个日志文件最大大小(MB)',
    monitor_sys_gen_server_log_max_files INT DEFAULT 10 COMMENT '最大日志文件数量',
    monitor_sys_gen_server_log_collection_interval INT DEFAULT 60 COMMENT '日志收集间隔(秒)',
    monitor_sys_gen_server_log_batch_size INT DEFAULT 1000 COMMENT '批量处理大小',
    monitor_sys_gen_server_log_compression_enabled TINYINT DEFAULT 1 COMMENT '是否启用压缩(0:否 1:是)',
    monitor_sys_gen_server_log_real_time_enabled TINYINT DEFAULT 0 COMMENT '是否启用实时日志(0:否 1:是)',
    monitor_sys_gen_server_log_file_patterns TEXT COMMENT '日志文件匹配模式(JSON格式)',
    monitor_sys_gen_server_log_exclude_patterns TEXT COMMENT '排除模式(JSON格式)',
    monitor_sys_gen_server_log_custom_parsers TEXT COMMENT '自定义解析器配置(JSON格式)',
    monitor_sys_gen_server_log_alert_rules TEXT COMMENT '告警规则配置(JSON格式)',
    monitor_sys_gen_server_log_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    monitor_sys_gen_server_log_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    monitor_sys_gen_server_log_config_status TINYINT DEFAULT 1 COMMENT '配置状态(0:禁用 1:启用)',
    
    UNIQUE KEY uk_server_id (monitor_sys_gen_server_id),
    INDEX idx_enabled (monitor_sys_gen_server_log_enabled),
    INDEX idx_config_status (monitor_sys_gen_server_log_config_status),
    INDEX idx_update_time (monitor_sys_gen_server_log_update_time),
    
    FOREIGN KEY (monitor_sys_gen_server_id) REFERENCES monitor_sys_gen_server(monitor_sys_gen_server_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务器日志配置表';

-- 服务器日志统计表
CREATE TABLE IF NOT EXISTS monitor_sys_gen_server_log_statistics (
    monitor_sys_gen_server_log_stat_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '统计ID',
    monitor_sys_gen_server_id INT NOT NULL COMMENT '关联服务器ID',
    monitor_sys_gen_server_log_stat_date DATE NOT NULL COMMENT '统计日期',
    monitor_sys_gen_server_log_stat_hour TINYINT COMMENT '统计小时(0-23)',
    monitor_sys_gen_server_log_total_count BIGINT DEFAULT 0 COMMENT '总日志数量',
    monitor_sys_gen_server_log_debug_count BIGINT DEFAULT 0 COMMENT 'DEBUG级别数量',
    monitor_sys_gen_server_log_info_count BIGINT DEFAULT 0 COMMENT 'INFO级别数量',
    monitor_sys_gen_server_log_warn_count BIGINT DEFAULT 0 COMMENT 'WARN级别数量',
    monitor_sys_gen_server_log_error_count BIGINT DEFAULT 0 COMMENT 'ERROR级别数量',
    monitor_sys_gen_server_log_fatal_count BIGINT DEFAULT 0 COMMENT 'FATAL级别数量',
    monitor_sys_gen_server_log_size_bytes BIGINT DEFAULT 0 COMMENT '日志大小(字节)',
    monitor_sys_gen_server_log_stat_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    monitor_sys_gen_server_log_stat_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_server_date_hour (monitor_sys_gen_server_id, monitor_sys_gen_server_log_stat_date, monitor_sys_gen_server_log_stat_hour),
    INDEX idx_server_id (monitor_sys_gen_server_id),
    INDEX idx_stat_date (monitor_sys_gen_server_log_stat_date),
    INDEX idx_create_time (monitor_sys_gen_server_log_stat_create_time),
    
    FOREIGN KEY (monitor_sys_gen_server_id) REFERENCES monitor_sys_gen_server(monitor_sys_gen_server_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务器日志统计表';

-- 为现有服务器创建默认日志配置
INSERT IGNORE INTO monitor_sys_gen_server_log_config (
    monitor_sys_gen_server_id,
    monitor_sys_gen_server_log_retention_days,
    monitor_sys_gen_server_log_enabled,
    monitor_sys_gen_server_log_level_filter,
    monitor_sys_gen_server_log_collection_interval,
    monitor_sys_gen_server_log_batch_size
)
SELECT 
    monitor_sys_gen_server_id,
    30,
    1,
    'INFO,WARN,ERROR,FATAL',
    60,
    1000
FROM monitor_sys_gen_server 
WHERE monitor_sys_gen_server_status = 1;
