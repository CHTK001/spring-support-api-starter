-- 为脚本执行历史表添加任务ID字段
-- 用于内存任务管理

-- 添加任务ID字段
ALTER TABLE monitor_sys_gen_script_execution 
ADD COLUMN monitor_sys_gen_script_execution_task_id VARCHAR(64) COMMENT '任务ID（用于内存任务管理）';

-- 添加任务ID索引，提高查询性能
CREATE INDEX idx_script_execution_task_id 
ON monitor_sys_gen_script_execution(monitor_sys_gen_script_execution_task_id);

-- 添加复合索引，优化按脚本ID和状态查询
CREATE INDEX idx_script_execution_script_status 
ON monitor_sys_gen_script_execution(monitor_sys_gen_script_id, monitor_sys_gen_script_execution_status);

-- 添加时间范围查询索引
CREATE INDEX idx_script_execution_time_range 
ON monitor_sys_gen_script_execution(monitor_sys_gen_script_execution_start_time, monitor_sys_gen_script_execution_end_time);

-- 添加进程ID索引，用于进程管理
CREATE INDEX idx_script_execution_process_id 
ON monitor_sys_gen_script_execution(monitor_sys_gen_script_execution_process_id);
