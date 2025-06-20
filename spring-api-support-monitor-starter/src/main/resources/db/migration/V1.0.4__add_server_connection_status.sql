-- 为monitor_sys_gen_server表添加连接状态相关字段
-- 检查字段是否存在，如果不存在则添加

-- 添加连接状态字段
ALTER TABLE monitor_sys_gen_server 
    ADD COLUMN IF NOT EXISTS monitor_sys_gen_server_connection_status INT DEFAULT 0 COMMENT '连接状态 0:离线 1:在线 2:连接中 3:连接失败';

-- 添加最后连接时间字段  
ALTER TABLE monitor_sys_gen_server 
    ADD COLUMN IF NOT EXISTS monitor_sys_gen_server_last_connect_time DATETIME COMMENT '最后连接时间';

-- 添加连接错误信息字段
ALTER TABLE monitor_sys_gen_server 
    ADD COLUMN IF NOT EXISTS monitor_sys_gen_server_connection_error TEXT COMMENT '连接错误信息';

-- 为连接状态字段添加索引以提高查询性能
CREATE INDEX IF NOT EXISTS idx_server_connection_status ON monitor_sys_gen_server(monitor_sys_gen_server_connection_status);

-- 为最后连接时间字段添加索引
CREATE INDEX IF NOT EXISTS idx_server_last_connect_time ON monitor_sys_gen_server(monitor_sys_gen_server_last_connect_time);
