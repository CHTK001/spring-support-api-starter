-- 添加浏览器和请求头相关字段
ALTER TABLE monitor_proxy_log
    ADD COLUMN IF NOT EXISTS monitor_proxy_log_browser VARCHAR (255) COMMENT '浏览器类型',
    ADD COLUMN IF NOT EXISTS monitor_proxy_log_os VARCHAR (255) COMMENT '操作系统',
    ADD COLUMN IF NOT EXISTS monitor_proxy_log_device VARCHAR (255) COMMENT '设备类型',
    ADD COLUMN IF NOT EXISTS monitor_proxy_log_fingerprint VARCHAR (255) COMMENT '浏览器指纹',
    ADD COLUMN IF NOT EXISTS monitor_proxy_log_user_agent VARCHAR (1000) COMMENT 'User-Agent';

-- 创建索引以提高查询性能
CREATE INDEX IF NOT EXISTS idx_monitor_proxy_log_browser ON monitor_proxy_log(monitor_proxy_log_browser);
CREATE INDEX IF NOT EXISTS idx_monitor_proxy_log_os ON monitor_proxy_log(monitor_proxy_log_os);
CREATE INDEX IF NOT EXISTS idx_monitor_proxy_log_fingerprint ON monitor_proxy_log(monitor_proxy_log_fingerprint); 