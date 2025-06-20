-- 创建服务器脚本管理相关表

-- 服务器脚本表
CREATE TABLE IF NOT EXISTS monitor_sys_gen_server_scripts (
    monitor_sys_gen_server_script_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '脚本ID',
    monitor_sys_gen_server_script_name VARCHAR(255) NOT NULL COMMENT '脚本名称',
    monitor_sys_gen_server_script_description TEXT COMMENT '脚本描述',
    monitor_sys_gen_server_script_content LONGTEXT NOT NULL COMMENT '脚本内容',
    monitor_sys_gen_server_script_type VARCHAR(50) DEFAULT 'SHELL' COMMENT '脚本类型(SHELL:Shell脚本 PYTHON:Python脚本 POWERSHELL:PowerShell脚本 BATCH:批处理脚本)',
    monitor_sys_gen_server_script_language VARCHAR(50) DEFAULT 'bash' COMMENT '脚本语言',
    monitor_sys_gen_server_script_version VARCHAR(50) DEFAULT '1.0.0' COMMENT '脚本版本',
    monitor_sys_gen_server_script_category VARCHAR(100) COMMENT '脚本分类',
    monitor_sys_gen_server_script_tags TEXT COMMENT '脚本标签(JSON格式)',
    monitor_sys_gen_server_script_parameters TEXT COMMENT '脚本参数定义(JSON格式)',
    monitor_sys_gen_server_script_timeout_seconds INT DEFAULT 300 COMMENT '执行超时时间(秒)',
    monitor_sys_gen_server_script_retry_count INT DEFAULT 0 COMMENT '重试次数',
    monitor_sys_gen_server_script_is_template TINYINT DEFAULT 0 COMMENT '是否为模板(0:否 1:是)',
    monitor_sys_gen_server_script_is_public TINYINT DEFAULT 0 COMMENT '是否公开(0:否 1:是)',
    monitor_sys_gen_server_script_execution_mode VARCHAR(20) DEFAULT 'SYNC' COMMENT '执行模式(SYNC:同步 ASYNC:异步)',
    monitor_sys_gen_server_script_working_directory VARCHAR(500) COMMENT '工作目录',
    monitor_sys_gen_server_script_environment_vars TEXT COMMENT '环境变量(JSON格式)',
    monitor_sys_gen_server_script_dependencies TEXT COMMENT '依赖项(JSON格式)',
    monitor_sys_gen_server_script_validation_rules TEXT COMMENT '验证规则(JSON格式)',
    monitor_sys_gen_server_script_create_user VARCHAR(100) COMMENT '创建用户',
    monitor_sys_gen_server_script_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    monitor_sys_gen_server_script_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    monitor_sys_gen_server_script_last_execution_time DATETIME COMMENT '最后执行时间',
    monitor_sys_gen_server_script_execution_count INT DEFAULT 0 COMMENT '执行次数',
    monitor_sys_gen_server_script_success_count INT DEFAULT 0 COMMENT '成功次数',
    monitor_sys_gen_server_script_status TINYINT DEFAULT 1 COMMENT '脚本状态(0:禁用 1:启用)',
    
    INDEX idx_script_name (monitor_sys_gen_server_script_name),
    INDEX idx_script_type (monitor_sys_gen_server_script_type),
    INDEX idx_script_category (monitor_sys_gen_server_script_category),
    INDEX idx_create_user (monitor_sys_gen_server_script_create_user),
    INDEX idx_create_time (monitor_sys_gen_server_script_create_time),
    INDEX idx_is_template (monitor_sys_gen_server_script_is_template),
    INDEX idx_is_public (monitor_sys_gen_server_script_is_public),
    INDEX idx_status (monitor_sys_gen_server_script_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务器脚本表';

-- 服务器脚本执行记录表
CREATE TABLE IF NOT EXISTS monitor_sys_gen_server_script_executions (
    monitor_sys_gen_server_script_execution_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '执行记录ID',
    monitor_sys_gen_server_script_id BIGINT NOT NULL COMMENT '脚本ID',
    monitor_sys_gen_server_id INT NOT NULL COMMENT '服务器ID',
    monitor_sys_gen_server_script_execution_name VARCHAR(255) COMMENT '执行名称',
    monitor_sys_gen_server_script_execution_parameters TEXT COMMENT '执行参数(JSON格式)',
    monitor_sys_gen_server_script_execution_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '执行状态(PENDING:待执行 RUNNING:执行中 COMPLETED:已完成 FAILED:失败 CANCELLED:已取消 TIMEOUT:超时)',
    monitor_sys_gen_server_script_execution_mode VARCHAR(20) DEFAULT 'SYNC' COMMENT '执行模式(SYNC:同步 ASYNC:异步)',
    monitor_sys_gen_server_script_execution_priority INT DEFAULT 5 COMMENT '执行优先级(1-10，数字越小优先级越高)',
    monitor_sys_gen_server_script_execution_start_time DATETIME COMMENT '开始执行时间',
    monitor_sys_gen_server_script_execution_end_time DATETIME COMMENT '结束时间',
    monitor_sys_gen_server_script_execution_duration_ms BIGINT COMMENT '执行耗时(毫秒)',
    monitor_sys_gen_server_script_execution_exit_code INT COMMENT '退出代码',
    monitor_sys_gen_server_script_execution_output LONGTEXT COMMENT '执行输出',
    monitor_sys_gen_server_script_execution_error_output LONGTEXT COMMENT '错误输出',
    monitor_sys_gen_server_script_execution_working_directory VARCHAR(500) COMMENT '工作目录',
    monitor_sys_gen_server_script_execution_environment_vars TEXT COMMENT '环境变量(JSON格式)',
    monitor_sys_gen_server_script_execution_process_id VARCHAR(50) COMMENT '进程ID',
    monitor_sys_gen_server_script_execution_trigger_type VARCHAR(50) DEFAULT 'MANUAL' COMMENT '触发类型(MANUAL:手动 SCHEDULED:定时 API:API调用 EVENT:事件触发)',
    monitor_sys_gen_server_script_execution_trigger_user VARCHAR(100) COMMENT '触发用户',
    monitor_sys_gen_server_script_execution_retry_count INT DEFAULT 0 COMMENT '重试次数',
    monitor_sys_gen_server_script_execution_max_retry INT DEFAULT 0 COMMENT '最大重试次数',
    monitor_sys_gen_server_script_execution_parent_id BIGINT COMMENT '父执行记录ID(重试时使用)',
    monitor_sys_gen_server_script_execution_scheduled_time DATETIME COMMENT '计划执行时间',
    monitor_sys_gen_server_script_execution_timeout_seconds INT COMMENT '超时时间(秒)',
    monitor_sys_gen_server_script_execution_tags TEXT COMMENT '执行标签(JSON格式)',
    monitor_sys_gen_server_script_execution_metadata TEXT COMMENT '执行元数据(JSON格式)',
    monitor_sys_gen_server_script_execution_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    monitor_sys_gen_server_script_execution_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    monitor_sys_gen_server_script_execution_record_status TINYINT DEFAULT 1 COMMENT '记录状态(0:删除 1:正常)',
    
    INDEX idx_script_id (monitor_sys_gen_server_script_id),
    INDEX idx_server_id (monitor_sys_gen_server_id),
    INDEX idx_execution_status (monitor_sys_gen_server_script_execution_status),
    INDEX idx_execution_mode (monitor_sys_gen_server_script_execution_mode),
    INDEX idx_trigger_type (monitor_sys_gen_server_script_execution_trigger_type),
    INDEX idx_trigger_user (monitor_sys_gen_server_script_execution_trigger_user),
    INDEX idx_start_time (monitor_sys_gen_server_script_execution_start_time),
    INDEX idx_create_time (monitor_sys_gen_server_script_execution_create_time),
    INDEX idx_scheduled_time (monitor_sys_gen_server_script_execution_scheduled_time),
    INDEX idx_priority (monitor_sys_gen_server_script_execution_priority),
    INDEX idx_parent_id (monitor_sys_gen_server_script_execution_parent_id),
    INDEX idx_record_status (monitor_sys_gen_server_script_execution_record_status),
    INDEX idx_script_server (monitor_sys_gen_server_script_id, monitor_sys_gen_server_id),
    
    FOREIGN KEY (monitor_sys_gen_server_script_id) REFERENCES monitor_sys_gen_server_scripts(monitor_sys_gen_server_script_id) ON DELETE CASCADE,
    FOREIGN KEY (monitor_sys_gen_server_id) REFERENCES monitor_sys_gen_server(monitor_sys_gen_server_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务器脚本执行记录表';

-- 服务器脚本统计表
CREATE TABLE IF NOT EXISTS monitor_sys_gen_server_script_statistics (
    monitor_sys_gen_server_script_stat_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '统计ID',
    monitor_sys_gen_server_script_id BIGINT COMMENT '脚本ID(NULL表示全局统计)',
    monitor_sys_gen_server_id INT COMMENT '服务器ID(NULL表示全局统计)',
    monitor_sys_gen_server_script_stat_date DATE NOT NULL COMMENT '统计日期',
    monitor_sys_gen_server_script_stat_hour TINYINT COMMENT '统计小时(0-23，NULL表示日统计)',
    monitor_sys_gen_server_script_execution_count INT DEFAULT 0 COMMENT '执行总数',
    monitor_sys_gen_server_script_success_count INT DEFAULT 0 COMMENT '成功执行数',
    monitor_sys_gen_server_script_failed_count INT DEFAULT 0 COMMENT '失败执行数',
    monitor_sys_gen_server_script_timeout_count INT DEFAULT 0 COMMENT '超时执行数',
    monitor_sys_gen_server_script_cancelled_count INT DEFAULT 0 COMMENT '取消执行数',
    monitor_sys_gen_server_script_total_duration_ms BIGINT DEFAULT 0 COMMENT '总执行时间(毫秒)',
    monitor_sys_gen_server_script_avg_duration_ms BIGINT DEFAULT 0 COMMENT '平均执行时间(毫秒)',
    monitor_sys_gen_server_script_max_duration_ms BIGINT DEFAULT 0 COMMENT '最大执行时间(毫秒)',
    monitor_sys_gen_server_script_min_duration_ms BIGINT DEFAULT 0 COMMENT '最小执行时间(毫秒)',
    monitor_sys_gen_server_script_retry_total_count INT DEFAULT 0 COMMENT '总重试次数',
    monitor_sys_gen_server_script_stat_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    monitor_sys_gen_server_script_stat_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_script_server_date_hour (monitor_sys_gen_server_script_id, monitor_sys_gen_server_id, monitor_sys_gen_server_script_stat_date, monitor_sys_gen_server_script_stat_hour),
    INDEX idx_script_id (monitor_sys_gen_server_script_id),
    INDEX idx_server_id (monitor_sys_gen_server_id),
    INDEX idx_stat_date (monitor_sys_gen_server_script_stat_date),
    INDEX idx_create_time (monitor_sys_gen_server_script_stat_create_time),
    
    FOREIGN KEY (monitor_sys_gen_server_script_id) REFERENCES monitor_sys_gen_server_scripts(monitor_sys_gen_server_script_id) ON DELETE CASCADE,
    FOREIGN KEY (monitor_sys_gen_server_id) REFERENCES monitor_sys_gen_server(monitor_sys_gen_server_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务器脚本统计表';

-- 服务器脚本调度表
CREATE TABLE IF NOT EXISTS monitor_sys_gen_server_script_schedules (
    monitor_sys_gen_server_script_schedule_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '调度ID',
    monitor_sys_gen_server_script_id BIGINT NOT NULL COMMENT '脚本ID',
    monitor_sys_gen_server_ids TEXT COMMENT '目标服务器ID列表(JSON格式)',
    monitor_sys_gen_server_script_schedule_name VARCHAR(255) NOT NULL COMMENT '调度名称',
    monitor_sys_gen_server_script_schedule_description TEXT COMMENT '调度描述',
    monitor_sys_gen_server_script_schedule_cron VARCHAR(100) COMMENT 'Cron表达式',
    monitor_sys_gen_server_script_schedule_type VARCHAR(20) DEFAULT 'CRON' COMMENT '调度类型(CRON:Cron表达式 INTERVAL:固定间隔 ONCE:一次性)',
    monitor_sys_gen_server_script_schedule_interval_seconds INT COMMENT '间隔时间(秒)',
    monitor_sys_gen_server_script_schedule_start_time DATETIME COMMENT '开始时间',
    monitor_sys_gen_server_script_schedule_end_time DATETIME COMMENT '结束时间',
    monitor_sys_gen_server_script_schedule_parameters TEXT COMMENT '调度参数(JSON格式)',
    monitor_sys_gen_server_script_schedule_enabled TINYINT DEFAULT 1 COMMENT '是否启用(0:否 1:是)',
    monitor_sys_gen_server_script_schedule_max_instances INT DEFAULT 1 COMMENT '最大并发实例数',
    monitor_sys_gen_server_script_schedule_timeout_seconds INT DEFAULT 300 COMMENT '超时时间(秒)',
    monitor_sys_gen_server_script_schedule_retry_count INT DEFAULT 0 COMMENT '重试次数',
    monitor_sys_gen_server_script_schedule_last_execution_time DATETIME COMMENT '最后执行时间',
    monitor_sys_gen_server_script_schedule_next_execution_time DATETIME COMMENT '下次执行时间',
    monitor_sys_gen_server_script_schedule_execution_count INT DEFAULT 0 COMMENT '执行次数',
    monitor_sys_gen_server_script_schedule_success_count INT DEFAULT 0 COMMENT '成功次数',
    monitor_sys_gen_server_script_schedule_create_user VARCHAR(100) COMMENT '创建用户',
    monitor_sys_gen_server_script_schedule_create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    monitor_sys_gen_server_script_schedule_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    monitor_sys_gen_server_script_schedule_status TINYINT DEFAULT 1 COMMENT '调度状态(0:禁用 1:启用)',
    
    INDEX idx_script_id (monitor_sys_gen_server_script_id),
    INDEX idx_schedule_name (monitor_sys_gen_server_script_schedule_name),
    INDEX idx_schedule_type (monitor_sys_gen_server_script_schedule_type),
    INDEX idx_enabled (monitor_sys_gen_server_script_schedule_enabled),
    INDEX idx_next_execution_time (monitor_sys_gen_server_script_schedule_next_execution_time),
    INDEX idx_create_user (monitor_sys_gen_server_script_schedule_create_user),
    INDEX idx_create_time (monitor_sys_gen_server_script_schedule_create_time),
    INDEX idx_status (monitor_sys_gen_server_script_schedule_status),
    
    FOREIGN KEY (monitor_sys_gen_server_script_id) REFERENCES monitor_sys_gen_server_scripts(monitor_sys_gen_server_script_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务器脚本调度表';

-- 插入一些示例脚本模板
INSERT IGNORE INTO monitor_sys_gen_server_scripts (
    monitor_sys_gen_server_script_name,
    monitor_sys_gen_server_script_description,
    monitor_sys_gen_server_script_content,
    monitor_sys_gen_server_script_type,
    monitor_sys_gen_server_script_language,
    monitor_sys_gen_server_script_category,
    monitor_sys_gen_server_script_is_template,
    monitor_sys_gen_server_script_is_public,
    monitor_sys_gen_server_script_create_user
) VALUES 
(
    '系统信息检查',
    '检查服务器基本系统信息',
    '#!/bin/bash\necho "=== 系统信息 ==="\nuname -a\necho "=== CPU信息 ==="\nlscpu\necho "=== 内存信息 ==="\nfree -h\necho "=== 磁盘信息 ==="\ndf -h',
    'SHELL',
    'bash',
    '系统监控',
    1,
    1,
    'system'
),
(
    '进程检查',
    '检查指定进程是否运行',
    '#!/bin/bash\nPROCESS_NAME=${1:-"nginx"}\nif pgrep -x "$PROCESS_NAME" > /dev/null; then\n    echo "进程 $PROCESS_NAME 正在运行"\n    exit 0\nelse\n    echo "进程 $PROCESS_NAME 未运行"\n    exit 1\nfi',
    'SHELL',
    'bash',
    '进程管理',
    1,
    1,
    'system'
),
(
    '日志清理',
    '清理指定目录下的旧日志文件',
    '#!/bin/bash\nLOG_DIR=${1:-"/var/log"}\nDAYS=${2:-"7"}\necho "清理 $LOG_DIR 目录下 $DAYS 天前的日志文件"\nfind "$LOG_DIR" -name "*.log" -type f -mtime +$DAYS -delete\necho "清理完成"',
    'SHELL',
    'bash',
    '日志管理',
    1,
    1,
    'system'
);
