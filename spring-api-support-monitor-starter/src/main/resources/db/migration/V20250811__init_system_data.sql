-- SystemData 系列表

CREATE TABLE IF NOT EXISTS system_data_setting (
    system_data_setting_id           INT AUTO_INCREMENT PRIMARY KEY,
    system_data_setting_name         VARCHAR(255) NOT NULL COMMENT '名称',
    system_data_setting_type         VARCHAR(64)  NOT NULL COMMENT '类型: JDBC/REDIS/ZOOKEEPER/ES/MONGO/...（SPI）',
    system_data_setting_server       VARCHAR(255)          COMMENT '地址或连接串',
    system_data_setting_host         VARCHAR(255)          COMMENT '主机',
    system_data_setting_port         INT                   COMMENT '端口',
    system_data_setting_username     VARCHAR(255)          COMMENT '用户名',
    system_data_setting_password     VARCHAR(255)          COMMENT '密码',
    system_data_setting_auth_type    VARCHAR(64)           COMMENT '认证类型: BASIC/ACL/KEY/...',
    system_data_setting_icon         VARCHAR(512)          COMMENT '图片地址',
    system_data_setting_console_type VARCHAR(32)           COMMENT '控制台类型: TABLE/GRAPH/FILE',
    system_data_setting_timeout_ms   INT DEFAULT 30000     COMMENT '超时时间(ms)',
    system_data_setting_is_file      TINYINT(1) DEFAULT 0  COMMENT '是否文件数据',
    system_data_setting_enabled      TINYINT(1) DEFAULT 1  COMMENT '是否启用',
    system_data_setting_config       TEXT                  COMMENT '扩展JSON配置',
    create_time                      DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time                      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='数据管理-客户端配置表';

CREATE TABLE IF NOT EXISTS system_data_setting_item (
    system_data_setting_item_id      INT AUTO_INCREMENT PRIMARY KEY,
    system_data_setting_item_setting_id INT NOT NULL,
    system_data_setting_item_name    VARCHAR(255) NOT NULL,
    system_data_setting_item_value   TEXT,
    system_data_setting_item_order   INT DEFAULT 1,
    create_time                      DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time                      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_setting(system_data_setting_item_setting_id)
) COMMENT='数据管理-配置项表';


