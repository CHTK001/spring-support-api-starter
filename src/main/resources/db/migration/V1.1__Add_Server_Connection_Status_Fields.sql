-- 添加服务器连接状态相关字段
-- 作者: CH
-- 日期: 2024/12/20
-- 描述: 为monitor_sys_gen_server表添加连接状态管理字段

-- 添加连接状态字段
ALTER TABLE monitor_sys_gen_server 
ADD COLUMN monitor_sys_gen_server_connection_status INT DEFAULT 0 COMMENT '连接状态 0:离线 1:在线 2:连接中 3:连接失败';

-- 添加最后连接时间字段
ALTER TABLE monitor_sys_gen_server 
ADD COLUMN monitor_sys_gen_server_last_connect_time DATETIME COMMENT '最后连接时间';

-- 添加连接失败原因字段
ALTER TABLE monitor_sys_gen_server 
ADD COLUMN monitor_sys_gen_server_connection_error VARCHAR(1000) COMMENT '连接失败原因';

-- 为连接状态字段添加索引，便于查询
CREATE INDEX idx_monitor_sys_gen_server_connection_status 
ON monitor_sys_gen_server(monitor_sys_gen_server_connection_status);

-- 为最后连接时间字段添加索引，便于按时间排序
CREATE INDEX idx_monitor_sys_gen_server_last_connect_time 
ON monitor_sys_gen_server(monitor_sys_gen_server_last_connect_time);

-- 更新现有数据，将所有服务器的连接状态设置为离线
UPDATE monitor_sys_gen_server 
SET monitor_sys_gen_server_connection_status = 0 
WHERE monitor_sys_gen_server_connection_status IS NULL;
