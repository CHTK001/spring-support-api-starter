-- 应用密钥表（AK/SK管理）
CREATE TABLE IF NOT EXISTS sys_app_key
(
    sys_app_key_id              INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    sys_app_key_app_id          VARCHAR(255)  NOT NULL COMMENT '应用ID（AccessKey/AppId）',
    sys_app_key_secret          VARCHAR(255)  NOT NULL COMMENT '应用密钥（SecretKey）',
    sys_app_key_name            VARCHAR(255)  NULL COMMENT '应用名称',
    sys_user_id                 INT           NULL COMMENT '关联用户ID',
    sys_app_key_status          INT DEFAULT 0 NULL COMMENT '状态（0：启用，1：禁用）',
    sys_app_key_delete          INT DEFAULT 0 NULL COMMENT '是否删除（0：正常，1：已删除）',
    sys_app_key_expire_time     BIGINT        NULL COMMENT '过期时间（Unix时间戳，为空表示永久有效）',
    sys_app_key_ip_whitelist    VARCHAR(1000) NULL COMMENT 'IP白名单（多个IP用逗号分隔）',
    sys_app_key_remark          VARCHAR(500)  NULL COMMENT '备注',
    sys_app_key_last_used_time  BIGINT        NULL COMMENT '最后使用时间',
    sys_app_key_use_count       INT DEFAULT 0 NULL COMMENT '使用次数',
    create_name                 VARCHAR(255)  NULL COMMENT '创建人姓名',
    create_by                   INT           NULL COMMENT '创建人',
    create_time                 DATETIME      NULL COMMENT '创建时间',
    update_time                 DATETIME      NULL COMMENT '更新时间',
    update_name                 VARCHAR(255)  NULL COMMENT '更新人姓名',
    update_by                   INT           NULL COMMENT '更新人',
    UNIQUE KEY uk_app_id (sys_app_key_app_id),
    INDEX idx_user_id (sys_user_id),
    INDEX idx_status (sys_app_key_status)
) COMMENT '应用密钥表（AK/SK管理）' ROW_FORMAT = DYNAMIC;

