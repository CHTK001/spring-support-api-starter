-- ============================================================
-- 视频平台 H2 专用 DDL（通用 MySQL DDL 在 H2 下不执行）
-- 由 H2SchemaConfiguration 在 h2 profile 下执行
-- 创建/补全视频配置数据库（sys_setting）所需表
-- ============================================================

CREATE TABLE IF NOT EXISTS sys_setting (
    sys_setting_id int NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    sys_setting_name varchar(255) NULL COMMENT '配置名称',
    sys_setting_code varchar(255) NULL COMMENT '配置编码',
    sys_setting_value text NULL COMMENT '配置值',
    sys_setting_default_value text NULL COMMENT '默认值',
    sys_setting_value_type varchar(255) NULL COMMENT '配置类型',
    sys_setting_group varchar(255) NULL COMMENT '所属分组',
    sys_setting_status int NULL DEFAULT 1 COMMENT '是否启用',
    sys_setting_sort int NULL DEFAULT 0 COMMENT '排序号',
    sys_setting_collect int NULL DEFAULT 0 COMMENT '是否多值集合',
    sys_setting_in_system int NULL DEFAULT 0 COMMENT '是否系统配置',
    sys_setting_app_inner int NULL DEFAULT 0 COMMENT '是否应用启动配置',
    sys_setting_admin int NULL DEFAULT 0 COMMENT '是否管理员专属',
    sys_setting_version int NULL DEFAULT 1 COMMENT '配置版本',
    sys_setting_config varchar(255) NULL COMMENT '配置参数',
    sys_setting_remark varchar(255) NULL COMMENT '配置备注',
    create_name varchar(255) NULL,
    create_by int NULL,
    create_time datetime NULL,
    update_time datetime NULL,
    update_name varchar(255) NULL,
    update_by int NULL,
    sys_setting_is_sensitive int NULL DEFAULT 0 COMMENT '是否敏感配置',
    sys_setting_validate varchar(512) NULL COMMENT '校验规则',
    PRIMARY KEY (sys_setting_id)
);

-- 兜底：若 sys_setting 已由其它 starter 的旧 DDL 建表，补齐缺失列
ALTER TABLE sys_setting ADD COLUMN IF NOT EXISTS sys_setting_default_value text NULL;
ALTER TABLE sys_setting ADD COLUMN IF NOT EXISTS sys_setting_is_sensitive int NULL DEFAULT 0;
ALTER TABLE sys_setting ADD COLUMN IF NOT EXISTS sys_setting_validate varchar(512) NULL;
ALTER TABLE sys_setting ADD COLUMN IF NOT EXISTS sys_setting_admin int NULL DEFAULT 0;
ALTER TABLE sys_setting ADD COLUMN IF NOT EXISTS sys_setting_app_inner int NULL DEFAULT 0;
ALTER TABLE sys_setting ADD COLUMN IF NOT EXISTS sys_setting_collect int NULL DEFAULT 0;
ALTER TABLE sys_setting ADD COLUMN IF NOT EXISTS sys_setting_in_system int NULL DEFAULT 0;
ALTER TABLE sys_setting ADD COLUMN IF NOT EXISTS sys_setting_value_type varchar(255) NULL;

-- 兜底：值列放宽为 TEXT（直播目录整段 JSON），兼容被旧 DDL 建成的 varchar(255)
ALTER TABLE sys_setting ALTER COLUMN sys_setting_value SET DATA TYPE TEXT;
ALTER TABLE sys_setting ALTER COLUMN sys_setting_default_value SET DATA TYPE TEXT;

CREATE UNIQUE INDEX IF NOT EXISTS sys_setting_uk_setting_name_group ON sys_setting (sys_setting_name, sys_setting_group);
CREATE INDEX IF NOT EXISTS sys_setting_idx_setting_code ON sys_setting (sys_setting_code);
CREATE INDEX IF NOT EXISTS sys_setting_idx_setting_group ON sys_setting (sys_setting_group);

-- ============================================================
-- job-starter 表（sys_job / sys_job_log / sys_job_cluster）
-- H2 语法：索引独立创建，不使用 MySQL 内联 KEY
-- ============================================================

CREATE TABLE IF NOT EXISTS sys_job (
    sys_job_id                       NUMERIC(19, 0) NOT NULL,
    sys_job_no                       VARCHAR(64)    NOT NULL,
    sys_job_name                     VARCHAR(255),
    sys_job_author                   VARCHAR(64),
    sys_job_alarm_email              VARCHAR(255),
    sys_job_group                    VARCHAR(64)    DEFAULT 'default',
    sys_job_schedule_type            VARCHAR(50)    DEFAULT 'CRON',
    sys_job_schedule_time            VARCHAR(255),
    sys_job_trigger_status           INT            DEFAULT 0,
    sys_job_desc                     VARCHAR(500),
    sys_job_glue_updatetime          TIMESTAMP,
    sys_job_glue_source              VARCHAR(4000),
    sys_job_glue_type                VARCHAR(50)    DEFAULT 'BEAN',
    sys_job_dispatch_mode            VARCHAR(20)    DEFAULT 'LOCAL',
    sys_job_remote_executor_address  VARCHAR(500),
    sys_job_storage_mode             VARCHAR(20)    DEFAULT 'DATABASE',
    sys_job_fail_retry               INT            DEFAULT 3,
    sys_job_retry_interval           INT            DEFAULT 0,
    sys_job_execute_timeout          INT            DEFAULT 0,
    sys_job_execute_bean             VARCHAR(255),
    sys_job_execute_param            VARCHAR(512),
    sys_job_exception_callback_bean  VARCHAR(255),
    sys_job_retry_callback_bean      VARCHAR(255),
    sys_job_execute_misfire_strategy VARCHAR(50)    DEFAULT 'DO_NOTHING',
    sys_job_trigger_last_time        NUMERIC(19, 0) DEFAULT 0,
    sys_job_trigger_next_time        NUMERIC(19, 0) DEFAULT 0,
    sys_job_is_deleted               INT            DEFAULT 0,
    sys_job_version                  NUMERIC(19, 0) DEFAULT 0,
    create_name                      VARCHAR(64),
    create_by                        INT,
    create_time                      TIMESTAMP,
    update_name                      VARCHAR(64),
    update_by                        INT,
    update_time                      TIMESTAMP,
    PRIMARY KEY (sys_job_id),
    CONSTRAINT uk_sys_job_no UNIQUE (sys_job_no)
);
CREATE INDEX IF NOT EXISTS idx_sys_job_trigger_status ON sys_job (sys_job_trigger_status);
CREATE INDEX IF NOT EXISTS idx_sys_job_trigger_next_time ON sys_job (sys_job_trigger_next_time);

CREATE TABLE IF NOT EXISTS sys_job_log (
    sys_job_log_id              NUMERIC(19, 0) NOT NULL,
    sys_job_log_no              VARCHAR(64)    NOT NULL,
    sys_job_id                  NUMERIC(19, 0),
    sys_job_no                  VARCHAR(64),
    sys_job_log_app             VARCHAR(255),
    sys_job_log_trigger_bean    VARCHAR(255),
    sys_job_log_trigger_type    VARCHAR(50),
    sys_job_log_profile         VARCHAR(50),
    sys_job_log_trigger_time    TIMESTAMP,
    sys_job_log_trigger_date    DATE,
    sys_job_log_trigger_code    VARCHAR(20),
    sys_job_log_execute_code    VARCHAR(20)    DEFAULT 'PADDING',
    sys_job_log_cost            DECIMAL(10, 2),
    sys_job_log_trigger_msg     VARCHAR(4000),
    sys_job_log_trigger_param   VARCHAR(512),
    sys_job_log_trigger_address VARCHAR(255),
    sys_job_log_node_id         VARCHAR(64),
    sys_job_log_file_path       VARCHAR(500),
    sys_job_log_is_deleted      INT            DEFAULT 0,
    create_name                 VARCHAR(64),
    create_by                   INT,
    create_time                 TIMESTAMP,
    update_name                 VARCHAR(64),
    update_by                   INT,
    update_time                 TIMESTAMP,
    PRIMARY KEY (sys_job_log_id),
    CONSTRAINT uk_sys_job_log_no UNIQUE (sys_job_log_no)
);
CREATE INDEX IF NOT EXISTS idx_sys_job_log_job_id ON sys_job_log (sys_job_id);
CREATE INDEX IF NOT EXISTS idx_sys_job_log_node_id ON sys_job_log (sys_job_log_node_id);
CREATE INDEX IF NOT EXISTS idx_sys_job_log_trigger_date ON sys_job_log (sys_job_log_trigger_date);

CREATE TABLE IF NOT EXISTS sys_job_cluster (
    sys_job_cluster_id              VARCHAR(64)    NOT NULL,
    sys_job_cluster_leader_node     VARCHAR(64),
    sys_job_cluster_lease_until_ms  NUMERIC(19, 0) DEFAULT 0,
    sys_job_cluster_version         NUMERIC(19, 0) DEFAULT 0,
    create_name                     VARCHAR(64),
    create_by                       INT,
    create_time                     TIMESTAMP,
    update_name                     VARCHAR(64),
    update_by                       INT,
    update_time                     TIMESTAMP,
    PRIMARY KEY (sys_job_cluster_id)
);
