-- 操作日志表
CREATE TABLE IF NOT EXISTS ms_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    operator VARCHAR(64) NOT NULL COMMENT '操作用户',
    operation VARCHAR(32) NOT NULL COMMENT '操作类型',
    target_module VARCHAR(32) NOT NULL COMMENT '目标模块',
    target_id VARCHAR(64) COMMENT '目标对象ID',
    description TEXT COMMENT '操作描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_operator (operator),
    INDEX idx_target (target_module, target_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';
