-- 添加服务器管理新字段
-- 作者: CH
-- 日期: 2024/12/24
-- 描述: 添加本地服务器检测、多IP支持、Docker支持、操作系统信息等字段

-- 添加是否本地服务器字段
ALTER TABLE monitor_sys_gen_server 
ADD COLUMN monitor_sys_gen_server_is_local INT DEFAULT 0 COMMENT '是否本地服务器 0:否 1:是 (自动检测，不允许修改)';

-- 添加服务器IP地址列表字段
ALTER TABLE monitor_sys_gen_server 
ADD COLUMN monitor_sys_gen_server_ip_addresses TEXT COMMENT '服务器IP地址列表 (JSON格式，支持多个IP)';

-- 添加Docker支持字段
ALTER TABLE monitor_sys_gen_server 
ADD COLUMN monitor_sys_gen_server_docker_enabled INT DEFAULT 0 COMMENT '是否支持Docker 0:否 1:是';

-- 添加Docker连接方式字段
ALTER TABLE monitor_sys_gen_server 
ADD COLUMN monitor_sys_gen_server_docker_connection_type VARCHAR(20) COMMENT 'Docker连接方式 (SHELL:命令行, TCP:TCP连接)';

-- 添加Docker TCP连接地址字段
ALTER TABLE monitor_sys_gen_server 
ADD COLUMN monitor_sys_gen_server_docker_host VARCHAR(255) COMMENT 'Docker TCP连接地址';

-- 添加Docker TCP连接端口字段
ALTER TABLE monitor_sys_gen_server 
ADD COLUMN monitor_sys_gen_server_docker_port INT COMMENT 'Docker TCP连接端口';

-- 添加操作系统类型字段
ALTER TABLE monitor_sys_gen_server 
ADD COLUMN monitor_sys_gen_server_os_type VARCHAR(50) COMMENT '操作系统类型 (自动检测)';

-- 添加操作系统版本字段
ALTER TABLE monitor_sys_gen_server 
ADD COLUMN monitor_sys_gen_server_os_version VARCHAR(100) COMMENT '操作系统版本 (自动检测)';

-- 添加操作系统架构字段
ALTER TABLE monitor_sys_gen_server 
ADD COLUMN monitor_sys_gen_server_os_arch VARCHAR(50) COMMENT '操作系统架构 (自动检测)';

-- 为新字段添加索引
CREATE INDEX idx_monitor_sys_gen_server_is_local ON monitor_sys_gen_server(monitor_sys_gen_server_is_local);
CREATE INDEX idx_monitor_sys_gen_server_docker_enabled ON monitor_sys_gen_server(monitor_sys_gen_server_docker_enabled);
CREATE INDEX idx_monitor_sys_gen_server_os_type ON monitor_sys_gen_server(monitor_sys_gen_server_os_type);

-- 更新现有数据，检测本地服务器
UPDATE monitor_sys_gen_server 
SET monitor_sys_gen_server_is_local = CASE 
    WHEN monitor_sys_gen_server_host IN ('localhost', '127.0.0.1', '0.0.0.0', '::1') THEN 1 
    ELSE 0 
END,
monitor_sys_gen_server_ip_addresses = '[]'
WHERE monitor_sys_gen_server_ip_addresses IS NULL;

-- 为本地服务器设置默认Docker配置
UPDATE monitor_sys_gen_server 
SET monitor_sys_gen_server_docker_enabled = 0,
    monitor_sys_gen_server_docker_connection_type = 'SHELL'
WHERE monitor_sys_gen_server_is_local = 1 
  AND monitor_sys_gen_server_docker_enabled IS NULL;
