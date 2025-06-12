-- 添加QPS和地理位置相关字段
ALTER TABLE monitor_proxy_log
    ADD COLUMN IF NOT EXISTS monitor_proxy_log_address_country VARCHAR (255) COMMENT '客户端地址地理位置（国家）',
    ADD COLUMN IF NOT EXISTS monitor_proxy_log_address_province VARCHAR (255) COMMENT '客户端地址地理位置（省份）',
    ADD COLUMN IF NOT EXISTS monitor_proxy_log_qps BIGINT COMMENT 'QPS值';

-- 更新monitor_proxy_log_address_geo字段注释
ALTER TABLE monitor_proxy_log
    MODIFY COLUMN monitor_proxy_log_address_geo VARCHAR(255) COMMENT '客户端地址地理位置（城市）';

-- 创建索引以提高查询性能
CREATE INDEX IF NOT EXISTS idx_monitor_proxy_log_qps ON monitor_proxy_log(monitor_proxy_log_qps);
CREATE INDEX IF NOT EXISTS idx_monitor_proxy_log_country ON monitor_proxy_log(monitor_proxy_log_address_country);
CREATE INDEX IF NOT EXISTS idx_monitor_proxy_log_province ON monitor_proxy_log(monitor_proxy_log_address_province); 