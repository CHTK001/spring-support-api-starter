-- 运维项目管理表
CREATE TABLE IF NOT EXISTS sys_devops_project (
    project_id            BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    project_name          VARCHAR(100)  NOT NULL                COMMENT '项目名称',
    project_description   TEXT          NULL                    COMMENT '项目描述',
    project_icon          VARCHAR(255)  NULL                    COMMENT '项目图标URL',
    server_os             VARCHAR(10)   NULL DEFAULT 'unknown'  COMMENT '服务器OS类型',
    sort_order            INT           NOT NULL DEFAULT 0      COMMENT '排序',
    status                TINYINT       NOT NULL DEFAULT 1      COMMENT '状态：1启用 0禁用',
    server_type           VARCHAR(10)   NULL                    COMMENT '连接类型：SSH/WINRM/LOCAL',
    server_host           VARCHAR(512)  NULL                    COMMENT '主机地址',
    server_port           INT           NULL                    COMMENT '端口号',
    server_username       VARCHAR(512)  NULL                    COMMENT '用户名',
    server_password       VARCHAR(1024) NULL                    COMMENT '密码',
    server_private_key    TEXT          NULL                    COMMENT 'SSH私钥',
    server_connect_timeout INT          NOT NULL DEFAULT 30     COMMENT '连接超时（秒）',
    server_enabled        TINYINT       NOT NULL DEFAULT 0      COMMENT '服务器信息是否激活',
    server_activated_at   DATETIME      NULL                    COMMENT '最近激活时间',
    log_path              VARCHAR(512)  NULL                    COMMENT '日志路径',
    config_path           VARCHAR(512)  NULL                    COMMENT '配置文件路径',
    lib_path              VARCHAR(512)  NULL                    COMMENT '依赖目录路径',
    root_path             VARCHAR(512)  NULL                    COMMENT '项目根目录',
    start_script          TEXT          NULL                    COMMENT '启动脚本',
    stop_script           TEXT          NULL                    COMMENT '停止脚本',
    restart_script        TEXT          NULL                    COMMENT '重启脚本',
    create_time           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by             VARCHAR(64)   NULL,
    update_by             VARCHAR(64)   NULL
);

-- 服务器主机表（避免测试后端在新库初始化时因 server_host 缺失而启动失败）
CREATE TABLE IF NOT EXISTS server_host (
    server_id             INT           NOT NULL AUTO_INCREMENT PRIMARY KEY,
    server_name           VARCHAR(255)  NULL,
    server_code           VARCHAR(255)  NULL,
    server_type           VARCHAR(64)   NULL,
    server_os_type        VARCHAR(64)   NULL,
    server_architecture   VARCHAR(64)   NULL,
    server_host           VARCHAR(255)  NULL,
    server_public_ip      VARCHAR(255)  NULL,
    server_port           INT           NULL,
    server_username       VARCHAR(255)  NULL,
    server_password       VARCHAR(2048) NULL,
    server_private_key    LONGTEXT      NULL,
    server_base_directory VARCHAR(1024) NULL,
    server_tags           VARCHAR(1024) NULL,
    server_enabled        TINYINT(1)    DEFAULT 1,
    server_description    VARCHAR(1024) NULL,
    server_metadata_json  LONGTEXT      NULL,
    create_name           VARCHAR(255)  NULL,
    create_by             INT           NULL,
    create_time           DATETIME      NULL,
    update_time           DATETIME      NULL,
    update_name           VARCHAR(255)  NULL,
    update_by             INT           NULL
);

-- 系统配置分组表（用于系统配置初始化）
CREATE TABLE IF NOT EXISTS sys_setting_group (
    sys_setting_group_id INT AUTO_INCREMENT PRIMARY KEY,
    sys_setting_group_name VARCHAR(255),
    sys_setting_group_code VARCHAR(255),
    sys_setting_group_icon VARCHAR(255),
    sys_setting_group_enable BOOLEAN,
    sys_setting_group_remark VARCHAR(1000),
    sys_setting_group_sort INT,
    sys_setting_group_use_project_interface BOOLEAN,
    create_name VARCHAR(255),
    create_by INT,
    create_time TIMESTAMP,
    update_name VARCHAR(255),
    update_by INT,
    update_time TIMESTAMP
);

-- 系统配置表（用于系统配置初始化）
CREATE TABLE IF NOT EXISTS sys_setting (
    sys_setting_id INT AUTO_INCREMENT PRIMARY KEY,
    sys_setting_name VARCHAR(255),
    sys_setting_value VARCHAR(255),
    sys_setting_config VARCHAR(255),
    sys_setting_remark VARCHAR(255),
    sys_setting_sort INT,
    sys_setting_version INT,
    sys_setting_status INT,
    sys_setting_admin INT,
    sys_setting_value_type VARCHAR(255),
    sys_setting_group VARCHAR(255),
    sys_setting_collect INT,
    sys_setting_in_system INT,
    sys_setting_app_inner INT,
    create_name VARCHAR(255),
    create_by INT,
    create_time TIMESTAMP,
    update_name VARCHAR(255),
    update_by INT,
    update_time TIMESTAMP
);

-- 项目管理基础表（用于项目管理页面远程项目解析）
CREATE TABLE IF NOT EXISTS sys_project (
    sys_project_id INT AUTO_INCREMENT PRIMARY KEY,
    sys_project_name VARCHAR(255),
    sys_project_icon TEXT,
    sys_project_group VARCHAR(255),
    sys_project_code VARCHAR(255),
    sys_project_app_id VARCHAR(255),
    sys_project_app_secret VARCHAR(255),
    sys_project_sign VARCHAR(255),
    sys_project_app_key VARCHAR(255),
    sys_project_sign_code VARCHAR(255),
    sys_project_delete INT DEFAULT 0,
    sys_project_status INT DEFAULT 1,
    sys_project_sort INT DEFAULT 0,
    sys_project_cdn VARCHAR(255),
    sys_project_endpoint VARCHAR(255),
    sys_project_local_path VARCHAR(255),
    sys_project_api_address VARCHAR(255),
    sys_project_location_temp_path VARCHAR(255),
    sys_project_location_oss_address VARCHAR(255),
    sys_project_vender VARCHAR(64),
    sys_project_remark VARCHAR(255),
    sys_project_function VARCHAR(255),
    sys_project_smtp_host VARCHAR(255),
    sys_project_smtp_port INT,
    sys_project_smtp_password VARCHAR(255),
    sys_project_smtp_from VARCHAR(255),
    create_name VARCHAR(255),
    create_by INT,
    create_time TIMESTAMP,
    update_time TIMESTAMP,
    update_name VARCHAR(255),
    update_by INT
);

-- 项目默认配置表（兼容 SysProjectMapper 子查询）
CREATE TABLE IF NOT EXISTS sys_project_default (
    sys_project_default_id INT AUTO_INCREMENT PRIMARY KEY,
    sys_project_default_type VARCHAR(255),
    sys_project_id INT
);

-- 项目容器软件预设表
CREATE TABLE IF NOT EXISTS sys_project_container_software_preset (
    sys_project_container_software_preset_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    software_code VARCHAR(128) NULL,
    software_name VARCHAR(255) NULL,
    software_category VARCHAR(64) NULL,
    software_version VARCHAR(64) NULL,
    software_icon VARCHAR(255) NULL,
    software_vendor VARCHAR(128) NULL,
    os_type VARCHAR(32) NULL,
    software_arch VARCHAR(32) NULL,
    install_path VARCHAR(255) NULL,
    expose_ports VARCHAR(255) NULL,
    install_script LONGTEXT NULL,
    start_script LONGTEXT NULL,
    stop_script LONGTEXT NULL,
    restart_script LONGTEXT NULL,
    status_script LONGTEXT NULL,
    uninstall_script LONGTEXT NULL,
    preset_builtin INT DEFAULT 1,
    preset_delete INT DEFAULT 0,
    preset_status INT DEFAULT 1,
    preset_sort INT DEFAULT 0,
    preset_remark VARCHAR(500) NULL,
    create_name VARCHAR(255) NULL,
    create_by INT NULL,
    create_time TIMESTAMP NULL,
    update_name VARCHAR(255) NULL,
    update_by INT NULL,
    update_time TIMESTAMP NULL
);

-- 项目容器软件关联表
CREATE TABLE IF NOT EXISTS sys_project_container_software_binding (
    sys_project_container_software_binding_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    sys_project_id INT NULL,
    sys_project_container_software_preset_id INT NULL,
    software_code VARCHAR(128) NULL,
    software_name VARCHAR(255) NULL,
    software_category VARCHAR(64) NULL,
    software_version VARCHAR(64) NULL,
    software_icon VARCHAR(255) NULL,
    software_vendor VARCHAR(128) NULL,
    os_type VARCHAR(32) NULL,
    software_arch VARCHAR(32) NULL,
    install_path VARCHAR(255) NULL,
    expose_ports VARCHAR(255) NULL,
    install_script LONGTEXT NULL,
    start_script LONGTEXT NULL,
    stop_script LONGTEXT NULL,
    restart_script LONGTEXT NULL,
    status_script LONGTEXT NULL,
    uninstall_script LONGTEXT NULL,
    install_status VARCHAR(32) NULL,
    runtime_status VARCHAR(32) NULL,
    binding_custom INT DEFAULT 0,
    binding_delete INT DEFAULT 0,
    binding_status INT DEFAULT 1,
    binding_sort INT DEFAULT 0,
    binding_remark VARCHAR(500) NULL,
    create_name VARCHAR(255) NULL,
    create_by INT NULL,
    create_time TIMESTAMP NULL,
    update_name VARCHAR(255) NULL,
    update_by INT NULL,
    update_time TIMESTAMP NULL
);

-- 预置：用于远程项目解析的测试项目（endpoint -> sysProjectId）
INSERT INTO sys_project (
    sys_project_name,
    sys_project_code,
    sys_project_app_id,
    sys_project_app_secret,
    sys_project_status,
    sys_project_delete,
    sys_project_endpoint,
    sys_project_vender,
    sys_project_function,
    create_name,
    create_time,
    update_name,
    update_time
)
SELECT
    '测试nginx',
    'panel-test-nginx-001',
    'root',
    'Yaq.K11qiRenWen>a<6XsJlbEN',
    1,
    0,
    'ssh://8.139.4.229:22',
    'panel-test',
    '16',
    'panel-test',
    CURRENT_TIMESTAMP,
    'panel-test',
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_project
    WHERE sys_project_endpoint = 'ssh://8.139.4.229:22'
);

-- 预置：容器软件基础预设
INSERT INTO sys_project_container_software_preset (
    sys_project_container_software_preset_id,
    software_code,
    software_name,
    software_category,
    software_version,
    software_icon,
    software_vendor,
    os_type,
    software_arch,
    install_path,
    expose_ports,
    install_script,
    start_script,
    stop_script,
    restart_script,
    status_script,
    uninstall_script,
    preset_builtin,
    preset_delete,
    preset_status,
    preset_sort,
    preset_remark
)
SELECT 1, 'nginx', 'NGINX', 'middleware', '1.24', 'mdi:web', 'nginx.org', 'linux', 'amd64', '/etc/nginx', '80,443',
       '#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get update\n  sudo apt-get install -y nginx\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum install -y epel-release || true\n  sudo yum install -y nginx\nelse\n  echo unsupported package manager\n  exit 1\nfi',
       '#!/bin/bash\nset -e\nsudo systemctl start nginx || sudo service nginx start',
       '#!/bin/bash\nset -e\nsudo systemctl stop nginx || sudo service nginx stop',
       '#!/bin/bash\nset -e\nsudo systemctl restart nginx || { sudo service nginx stop; sudo service nginx start; }',
       '#!/bin/bash\nsudo systemctl status nginx --no-pager || ps -ef | grep nginx | grep -v grep',
       '#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get remove -y nginx\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum remove -y nginx\nfi',
       1, 0, 1, 10, 'Linux NGINX 预设'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE sys_project_container_software_preset_id = 1
);

INSERT INTO sys_project_container_software_preset (
    sys_project_container_software_preset_id,
    software_code,
    software_name,
    software_category,
    software_version,
    software_icon,
    software_vendor,
    os_type,
    software_arch,
    install_path,
    expose_ports,
    install_script,
    start_script,
    stop_script,
    restart_script,
    status_script,
    uninstall_script,
    preset_builtin,
    preset_delete,
    preset_status,
    preset_sort,
    preset_remark
)
SELECT 2, 'mysql', 'MySQL Community', 'database', '8.0', 'mdi:database', 'oracle', 'linux', 'amd64', '/var/lib/mysql', '3306',
       '#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get update\n  sudo DEBIAN_FRONTEND=noninteractive apt-get install -y mysql-server\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum install -y mysql-server || sudo yum install -y mariadb-server\nelse\n  echo unsupported package manager\n  exit 1\nfi',
       '#!/bin/bash\nset -e\nsudo systemctl start mysqld || sudo systemctl start mysql || sudo service mysqld start || sudo service mysql start',
       '#!/bin/bash\nset -e\nsudo systemctl stop mysqld || sudo systemctl stop mysql || sudo service mysqld stop || sudo service mysql stop',
       '#!/bin/bash\nset -e\nsudo systemctl restart mysqld || sudo systemctl restart mysql || { sudo service mysqld stop; sudo service mysqld start; } || { sudo service mysql stop; sudo service mysql start; }',
       '#!/bin/bash\nsudo systemctl status mysqld --no-pager || sudo systemctl status mysql --no-pager || ps -ef | grep mysql | grep -v grep',
       '#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get remove -y mysql-server\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum remove -y mysql-server mariadb-server\nfi',
       1, 0, 1, 20, 'Linux MySQL 预设'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE sys_project_container_software_preset_id = 2
);

INSERT INTO sys_project_container_software_preset (
    sys_project_container_software_preset_id,
    software_code,
    software_name,
    software_category,
    software_version,
    software_icon,
    software_vendor,
    os_type,
    software_arch,
    install_path,
    expose_ports,
    install_script,
    start_script,
    stop_script,
    restart_script,
    status_script,
    uninstall_script,
    preset_builtin,
    preset_delete,
    preset_status,
    preset_sort,
    preset_remark
)
SELECT 3, 'redis', 'Redis', 'database', '7.0', 'mdi:database-outline', 'redis.io', 'linux', 'amd64', '/etc/redis', '6379',
       '#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get update\n  sudo apt-get install -y redis-server\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum install -y redis\nelse\n  echo unsupported package manager\n  exit 1\nfi',
       '#!/bin/bash\nset -e\nsudo systemctl start redis || sudo systemctl start redis-server || sudo service redis start || sudo service redis-server start',
       '#!/bin/bash\nset -e\nsudo systemctl stop redis || sudo systemctl stop redis-server || sudo service redis stop || sudo service redis-server stop',
       '#!/bin/bash\nset -e\nsudo systemctl restart redis || sudo systemctl restart redis-server || { sudo service redis stop; sudo service redis start; }',
       '#!/bin/bash\nsudo systemctl status redis --no-pager || sudo systemctl status redis-server --no-pager || ps -ef | grep redis-server | grep -v grep',
       '#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get remove -y redis-server\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum remove -y redis\nfi',
       1, 0, 1, 30, 'Linux Redis 预设'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE sys_project_container_software_preset_id = 3
);

INSERT INTO sys_project_container_software_preset (
    sys_project_container_software_preset_id,
    software_code,
    software_name,
    software_category,
    software_version,
    software_icon,
    software_vendor,
    os_type,
    software_arch,
    install_path,
    expose_ports,
    install_script,
    start_script,
    stop_script,
    restart_script,
    status_script,
    uninstall_script,
    preset_builtin,
    preset_delete,
    preset_status,
    preset_sort,
    preset_remark
)
SELECT 4, 'nodejs', 'Node.js', 'language', '20', 'mdi:nodejs', 'nodejs.org', 'linux', 'amd64', '/usr/local/node', '3000',
       '#!/bin/bash\nset -e\ncurl -fsSL https://rpm.nodesource.com/setup_20.x | sudo bash - 2>/dev/null || true\ncurl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash - 2>/dev/null || true\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get install -y nodejs\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum install -y nodejs\nelse\n  echo unsupported package manager\n  exit 1\nfi',
       '#!/bin/bash\necho Node.js 是运行时环境，请在具体项目脚本中定义启动命令',
       '#!/bin/bash\necho Node.js 是运行时环境，请在具体项目脚本中定义停止命令',
       '#!/bin/bash\necho Node.js 是运行时环境，请在具体项目脚本中定义重启命令',
       '#!/bin/bash\nnode -v && npm -v',
       '#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get remove -y nodejs\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum remove -y nodejs\nfi',
       1, 0, 1, 40, 'Linux Node.js 20 预设'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE sys_project_container_software_preset_id = 4
);

INSERT INTO sys_project_container_software_preset (
    sys_project_container_software_preset_id,
    software_code,
    software_name,
    software_category,
    software_version,
    software_icon,
    software_vendor,
    os_type,
    software_arch,
    install_path,
    expose_ports,
    install_script,
    start_script,
    stop_script,
    restart_script,
    status_script,
    uninstall_script,
    preset_builtin,
    preset_delete,
    preset_status,
    preset_sort,
    preset_remark
)
SELECT 5, 'python', 'Python', 'language', '3.11', 'mdi:language-python', 'python.org', 'linux', 'amd64', '/usr/bin', '',
       '#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get update\n  sudo apt-get install -y python3 python3-pip python3-venv\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum install -y python3 python3-pip\nelse\n  echo unsupported package manager\n  exit 1\nfi',
       '#!/bin/bash\necho Python 为运行时环境，请在具体项目脚本中定义启动命令',
       '#!/bin/bash\necho Python 为运行时环境，请在具体项目脚本中定义停止命令',
       '#!/bin/bash\necho Python 为运行时环境，请在具体项目脚本中定义重启命令',
       '#!/bin/bash\npython3 --version && pip3 --version',
       '#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get remove -y python3 python3-pip python3-venv\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum remove -y python3 python3-pip\nfi',
       1, 0, 1, 50, 'Linux Python 3.11 预设'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE sys_project_container_software_preset_id = 5
);

INSERT INTO sys_project_container_software_preset (
    sys_project_container_software_preset_id,
    software_code,
    software_name,
    software_category,
    software_version,
    software_icon,
    software_vendor,
    os_type,
    software_arch,
    install_path,
    expose_ports,
    install_script,
    start_script,
    stop_script,
    restart_script,
    status_script,
    uninstall_script,
    preset_builtin,
    preset_delete,
    preset_status,
    preset_sort,
    preset_remark
)
SELECT 6, 'maven', 'Maven', 'devops', '3.9', 'mdi:apache-kafka', 'apache', 'linux', 'amd64', '/opt/maven', '',
       '#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get update\n  sudo apt-get install -y maven\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum install -y maven\nelse\n  echo unsupported package manager\n  exit 1\nfi',
       '#!/bin/bash\necho Maven 为构建工具，无常驻服务，无需启动',
       '#!/bin/bash\necho Maven 为构建工具，无常驻服务，无需停止',
       '#!/bin/bash\necho Maven 为构建工具，无常驻服务，无需重启',
       '#!/bin/bash\nmvn -version',
       '#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get remove -y maven\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum remove -y maven\nfi',
       1, 0, 1, 60, 'Linux Maven 预设'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE sys_project_container_software_preset_id = 6
);

INSERT INTO sys_project_container_software_preset (
    sys_project_container_software_preset_id,
    software_code,
    software_name,
    software_category,
    software_version,
    software_icon,
    software_vendor,
    os_type,
    software_arch,
    install_path,
    expose_ports,
    install_script,
    start_script,
    stop_script,
    restart_script,
    status_script,
    uninstall_script,
    preset_builtin,
    preset_delete,
    preset_status,
    preset_sort,
    preset_remark
)
SELECT 7, 'docker-compose', 'Docker Compose', 'ops', 'v2', 'mdi:docker', 'docker', 'linux', 'amd64', '/usr/local/bin', '',
       '#!/bin/bash\nset -e\nsudo mkdir -p /usr/local/lib/docker/cli-plugins\nsudo curl -SL https://github.com/docker/compose/releases/download/v2.27.0/docker-compose-linux-x86_64 -o /usr/local/lib/docker/cli-plugins/docker-compose\nsudo chmod +x /usr/local/lib/docker/cli-plugins/docker-compose',
       '#!/bin/bash\necho Docker Compose 为命令行工具，无常驻服务，无需启动',
       '#!/bin/bash\necho Docker Compose 为命令行工具，无常驻服务，无需停止',
       '#!/bin/bash\necho Docker Compose 为命令行工具，无常驻服务，无需重启',
       '#!/bin/bash\ndocker compose version',
       '#!/bin/bash\nset -e\nsudo rm -f /usr/local/lib/docker/cli-plugins/docker-compose',
       1, 0, 1, 70, 'Linux Docker Compose 预设'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE sys_project_container_software_preset_id = 7
);

INSERT INTO sys_project_container_software_preset (
    sys_project_container_software_preset_id,
    software_code,
    software_name,
    software_category,
    software_version,
    software_icon,
    software_vendor,
    os_type,
    software_arch,
    install_path,
    expose_ports,
    install_script,
    start_script,
    stop_script,
    restart_script,
    status_script,
    uninstall_script,
    preset_builtin,
    preset_delete,
    preset_status,
    preset_sort,
    preset_remark
)
SELECT 8, 'git', 'Git for Windows', 'devops', '2.x', 'mdi:git', 'git-scm', 'windows', 'amd64', 'C:/Program Files/Git', '',
       '$gitUrl = \"https://github.com/git-for-windows/git/releases/download/v2.45.1.windows.1/Git-2.45.1-64-bit.exe\"\n$temp = Join-Path $env:TEMP \"git-installer.exe\"\nInvoke-WebRequest -Uri $gitUrl -OutFile $temp\nStart-Process -FilePath $temp -ArgumentList \"/VERYSILENT\",\"/NORESTART\" -Wait',
       'Write-Output \"Git for Windows 为命令行工具，无常驻服务，无需启动\"',
       'Write-Output \"Git for Windows 为命令行工具，无常驻服务，无需停止\"',
       'Write-Output \"Git for Windows 为命令行工具，无常驻服务，无需重启\"',
       'git --version',
       '$gitUninstall = \"C:/Program Files/Git/unins000.exe\"\nif (Test-Path $gitUninstall) { Start-Process -FilePath $gitUninstall -ArgumentList \"/VERYSILENT\",\"/NORESTART\" -Wait }',
       1, 0, 1, 80, 'Windows Git 预设'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE sys_project_container_software_preset_id = 8
);

INSERT INTO sys_project_container_software_preset (
    sys_project_container_software_preset_id,
    software_code,
    software_name,
    software_category,
    software_version,
    software_icon,
    software_vendor,
    os_type,
    software_arch,
    install_path,
    expose_ports,
    install_script,
    start_script,
    stop_script,
    restart_script,
    status_script,
    uninstall_script,
    preset_builtin,
    preset_delete,
    preset_status,
    preset_sort,
    preset_remark
)
SELECT 9, 'git', 'Git for macOS', 'devops', '2.x', 'mdi:git', 'git-scm', 'macos', 'arm64', '/usr/bin', '',
       '/bin/bash -lc \"if command -v brew >/dev/null 2>&1; then brew install git; else xcode-select --install || true; fi\"',
       'echo Git for macOS 为命令行工具，无常驻服务，无需启动',
       'echo Git for macOS 为命令行工具，无常驻服务，无需停止',
       'echo Git for macOS 为命令行工具，无常驻服务，无需重启',
       'git --version',
       '/bin/bash -lc \"if command -v brew >/dev/null 2>&1; then brew uninstall git; fi\"',
       1, 0, 1, 90, 'macOS Git 预设'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE sys_project_container_software_preset_id = 9
);

INSERT INTO sys_project_container_software_preset (
    sys_project_container_software_preset_id,
    software_code,
    software_name,
    software_category,
    software_version,
    software_icon,
    software_vendor,
    os_type,
    software_arch,
    install_path,
    expose_ports,
    install_script,
    start_script,
    stop_script,
    restart_script,
    status_script,
    uninstall_script,
    preset_builtin,
    preset_delete,
    preset_status,
    preset_sort,
    preset_remark
)
SELECT 4, 'nodejs', 'Node.js', 'language', '20', 'mdi:nodejs', 'nodejs.org', 'linux', 'amd64', '/usr/local/node', '3000',
       '#!/bin/bash\nset -e\ncurl -fsSL https://rpm.nodesource.com/setup_20.x | sudo bash - 2>/dev/null || true\ncurl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash - 2>/dev/null || true\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get install -y nodejs\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum install -y nodejs\nelse\n  echo unsupported package manager\n  exit 1\nfi',
       '#!/bin/bash\necho Node.js 是运行时环境，请在具体项目脚本中定义启动命令',
       '#!/bin/bash\necho Node.js 是运行时环境，请在具体项目脚本中定义停止命令',
       '#!/bin/bash\necho Node.js 是运行时环境，请在具体项目脚本中定义重启命令',
       '#!/bin/bash\nnode -v && npm -v',
       '#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get remove -y nodejs\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum remove -y nodejs\nfi',
       1, 0, 1, 40, 'Linux Node.js 20 预设'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE sys_project_container_software_preset_id = 4
);

INSERT INTO sys_project_container_software_preset (
    sys_project_container_software_preset_id,
    software_code,
    software_name,
    software_category,
    software_version,
    software_icon,
    software_vendor,
    os_type,
    software_arch,
    install_path,
    expose_ports,
    install_script,
    start_script,
    stop_script,
    restart_script,
    status_script,
    uninstall_script,
    preset_builtin,
    preset_delete,
    preset_status,
    preset_sort,
    preset_remark
)
SELECT 5, 'python', 'Python', 'language', '3.11', 'mdi:language-python', 'python.org', 'linux', 'amd64', '/usr/bin', '',
       '#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get update\n  sudo apt-get install -y python3 python3-pip python3-venv\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum install -y python3 python3-pip\nelse\n  echo unsupported package manager\n  exit 1\nfi',
       '#!/bin/bash\necho Python 为运行时环境，请在具体项目脚本中定义启动命令',
       '#!/bin/bash\necho Python 为运行时环境，请在具体项目脚本中定义停止命令',
       '#!/bin/bash\necho Python 为运行时环境，请在具体项目脚本中定义重启命令',
       '#!/bin/bash\npython3 --version && pip3 --version',
       '#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get remove -y python3 python3-pip python3-venv\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum remove -y python3 python3-pip\nfi',
       1, 0, 1, 50, 'Linux Python 3.11 预设'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE sys_project_container_software_preset_id = 5
);

INSERT INTO sys_project_container_software_preset (
    sys_project_container_software_preset_id,
    software_code,
    software_name,
    software_category,
    software_version,
    software_icon,
    software_vendor,
    os_type,
    software_arch,
    install_path,
    expose_ports,
    install_script,
    start_script,
    stop_script,
    restart_script,
    status_script,
    uninstall_script,
    preset_builtin,
    preset_delete,
    preset_status,
    preset_sort,
    preset_remark
)
SELECT 6, 'maven', 'Maven', 'devops', '3.9', 'mdi:apache-kafka', 'apache', 'linux', 'amd64', '/opt/maven', '',
       '#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get update\n  sudo apt-get install -y maven\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum install -y maven\nelse\n  echo unsupported package manager\n  exit 1\nfi',
       '#!/bin/bash\necho Maven 为构建工具，无常驻服务，无需启动',
       '#!/bin/bash\necho Maven 为构建工具，无常驻服务，无需停止',
       '#!/bin/bash\necho Maven 为构建工具，无常驻服务，无需重启',
       '#!/bin/bash\nmvn -version',
       '#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get remove -y maven\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum remove -y maven\nfi',
       1, 0, 1, 60, 'Linux Maven 预设'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE sys_project_container_software_preset_id = 6
);

INSERT INTO sys_project_container_software_preset (
    sys_project_container_software_preset_id,
    software_code,
    software_name,
    software_category,
    software_version,
    software_icon,
    software_vendor,
    os_type,
    software_arch,
    install_path,
    expose_ports,
    install_script,
    start_script,
    stop_script,
    restart_script,
    status_script,
    uninstall_script,
    preset_builtin,
    preset_delete,
    preset_status,
    preset_sort,
    preset_remark
)
SELECT 7, 'docker-compose', 'Docker Compose', 'ops', 'v2', 'mdi:docker', 'docker', 'linux', 'amd64', '/usr/local/bin', '',
       '#!/bin/bash\nset -e\nsudo mkdir -p /usr/local/lib/docker/cli-plugins\nsudo curl -SL https://github.com/docker/compose/releases/download/v2.27.0/docker-compose-linux-x86_64 -o /usr/local/lib/docker/cli-plugins/docker-compose\nsudo chmod +x /usr/local/lib/docker/cli-plugins/docker-compose',
       '#!/bin/bash\necho Docker Compose 为命令行工具，无常驻服务，无需启动',
       '#!/bin/bash\necho Docker Compose 为命令行工具，无常驻服务，无需停止',
       '#!/bin/bash\necho Docker Compose 为命令行工具，无常驻服务，无需重启',
       '#!/bin/bash\ndocker compose version',
       '#!/bin/bash\nset -e\nsudo rm -f /usr/local/lib/docker/cli-plugins/docker-compose',
       1, 0, 1, 70, 'Linux Docker Compose 预设'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE sys_project_container_software_preset_id = 7
);

INSERT INTO sys_project_container_software_preset (
    sys_project_container_software_preset_id,
    software_code,
    software_name,
    software_category,
    software_version,
    software_icon,
    software_vendor,
    os_type,
    software_arch,
    install_path,
    expose_ports,
    install_script,
    start_script,
    stop_script,
    restart_script,
    status_script,
    uninstall_script,
    preset_builtin,
    preset_delete,
    preset_status,
    preset_sort,
    preset_remark
)
SELECT 8, 'git', 'Git for Windows', 'devops', '2.x', 'mdi:git', 'git-scm', 'windows', 'amd64', 'C:/Program Files/Git', '',
       '$gitUrl = \"https://github.com/git-for-windows/git/releases/download/v2.45.1.windows.1/Git-2.45.1-64-bit.exe\"\n$temp = Join-Path $env:TEMP \"git-installer.exe\"\nInvoke-WebRequest -Uri $gitUrl -OutFile $temp\nStart-Process -FilePath $temp -ArgumentList \"/VERYSILENT\",\"/NORESTART\" -Wait',
       'Write-Output \"Git for Windows 为命令行工具，无常驻服务，无需启动\"',
       'Write-Output \"Git for Windows 为命令行工具，无常驻服务，无需停止\"',
       'Write-Output \"Git for Windows 为命令行工具，无常驻服务，无需重启\"',
       'git --version',
       '$gitUninstall = \"C:/Program Files/Git/unins000.exe\"\nif (Test-Path $gitUninstall) { Start-Process -FilePath $gitUninstall -ArgumentList \"/VERYSILENT\",\"/NORESTART\" -Wait }',
       1, 0, 1, 80, 'Windows Git 预设'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE sys_project_container_software_preset_id = 8
);

INSERT INTO sys_project_container_software_preset (
    sys_project_container_software_preset_id,
    software_code,
    software_name,
    software_category,
    software_version,
    software_icon,
    software_vendor,
    os_type,
    software_arch,
    install_path,
    expose_ports,
    install_script,
    start_script,
    stop_script,
    restart_script,
    status_script,
    uninstall_script,
    preset_builtin,
    preset_delete,
    preset_status,
    preset_sort,
    preset_remark
)
SELECT 9, 'git', 'Git for macOS', 'devops', '2.x', 'mdi:git', 'git-scm', 'macos', 'arm64', '/usr/bin', '',
       '/bin/bash -lc \"if command -v brew >/dev/null 2>&1; then brew install git; else xcode-select --install || true; fi\"',
       'echo Git for macOS 为命令行工具，无常驻服务，无需启动',
       'echo Git for macOS 为命令行工具，无常驻服务，无需停止',
       'echo Git for macOS 为命令行工具，无常驻服务，无需重启',
       'git --version',
       '/bin/bash -lc \"if command -v brew >/dev/null 2>&1; then brew uninstall git; fi\"',
       1, 0, 1, 90, 'macOS Git 预设'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE sys_project_container_software_preset_id = 9
);
