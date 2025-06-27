-- 创建服务器配置历史表
-- 作者: CH
-- 日期: 2024/12/27

CREATE TABLE monitor_sys_gen_server_setting_history
(
    monitor_sys_gen_server_setting_history_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '历史记录ID',
    monitor_sys_gen_server_id                 INT         NOT NULL COMMENT '服务器ID',
    monitor_sys_gen_server_setting_id         BIGINT COMMENT '配置ID',

    -- 变更信息
    change_type                               VARCHAR(20) NOT NULL COMMENT '变更类型: CREATE/UPDATE/DELETE',
    change_description                        TEXT COMMENT '变更描述',
    change_user                               VARCHAR(100) COMMENT '变更用户',
    change_time                               DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',

    -- 配置快照（JSON格式存储完整配置）
    setting_snapshot                          JSON COMMENT '配置快照',

    -- 变更前后对比
    changed_fields                            JSON COMMENT '变更字段列表',
    old_values                                JSON COMMENT '变更前的值',
    new_values                                JSON COMMENT '变更后的值',

    -- 基础字段
    create_time                               DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time                               DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_server_id (monitor_sys_gen_server_id),
    INDEX idx_setting_id (monitor_sys_gen_server_setting_id),
    INDEX idx_change_time (change_time),
    INDEX idx_change_type (change_type)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='服务器配置变更历史表';

-- 创建配置备份表
CREATE TABLE monitor_sys_gen_server_setting_backup
(
    monitor_sys_gen_server_setting_backup_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '备份ID',
    backup_name                              VARCHAR(200) NOT NULL COMMENT '备份名称',
    backup_description                       TEXT COMMENT '备份描述',
    backup_type                              VARCHAR(20)  NOT NULL DEFAULT 'MANUAL' COMMENT '备份类型: MANUAL/AUTO/SCHEDULED',

    -- 备份范围
    server_ids                               JSON COMMENT '备份的服务器ID列表',
    backup_scope                             VARCHAR(20)  NOT NULL DEFAULT 'SELECTED' COMMENT '备份范围: ALL/SELECTED',

    -- 备份数据
    backup_data                              LONGTEXT     NOT NULL COMMENT '备份数据(JSON格式)',
    backup_format                            VARCHAR(10)  NOT NULL DEFAULT 'JSON' COMMENT '备份格式: JSON/YAML',

    -- 备份统计
    server_count                             INT                   DEFAULT 0 COMMENT '备份的服务器数量',
    backup_size                              BIGINT                DEFAULT 0 COMMENT '备份数据大小(字节)',

    -- 备份状态
    backup_status                            VARCHAR(20)  NOT NULL DEFAULT 'SUCCESS' COMMENT '备份状态: SUCCESS/FAILED/PARTIAL',
    backup_error                             TEXT COMMENT '备份错误信息',

    -- 基础字段
    create_user                              VARCHAR(100) COMMENT '创建用户',
    create_time                              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time                              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_backup_name (backup_name),
    INDEX idx_backup_type (backup_type),
    INDEX idx_backup_status (backup_status),
    INDEX idx_create_time (create_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='服务器配置备份表';

-- 插入示例备份记录
INSERT INTO monitor_sys_gen_server_setting_backup (backup_name,
                                                   backup_description,
                                                   backup_type,
                                                   backup_scope,
                                                   backup_data,
                                                   server_count,
                                                   create_user)
VALUES ('系统初始化备份',
        '系统初始化时的默认配置备份',
        'AUTO',
        'ALL',
        '[]',
        0,
        'system');
