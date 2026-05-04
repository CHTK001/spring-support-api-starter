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
    preset_remark LONGTEXT NULL,
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
    binding_remark LONGTEXT NULL,
    create_name VARCHAR(255) NULL,
    create_by INT NULL,
    create_time TIMESTAMP NULL,
    update_name VARCHAR(255) NULL,
    update_by INT NULL,
    update_time TIMESTAMP NULL
);

-- 项目容器服务编排表
CREATE TABLE IF NOT EXISTS sys_project_container_service_topology (
    sys_project_container_service_topology_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    sys_project_id INT NULL,
    topology_name VARCHAR(255) NULL,
    editor_nodes_json LONGTEXT NULL,
    editor_edges_json LONGTEXT NULL,
    imported_projects_json LONGTEXT NULL,
    node_config_json LONGTEXT NULL,
    topology_delete INT DEFAULT 0,
    topology_status INT DEFAULT 1,
    topology_remark VARCHAR(500) NULL,
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

-- 升级现有 Linux 软件预设为 system/docker 双模式结构
UPDATE sys_project_container_software_preset
SET preset_remark = '{"description":"Linux NGINX 双模式预设","serviceName":"nginx","modeConfigFields":{"SYSTEM":[{"fieldKey":"workerProcesses","fieldLabel":"worker_processes","componentType":"input","groupName":"system","requiredFlag":false,"defaultValue":"auto","description":"Nginx worker 进程数"}],"DOCKER":[{"fieldKey":"workerProcesses","fieldLabel":"worker_processes","componentType":"input","groupName":"docker","requiredFlag":false,"defaultValue":"auto","description":"Nginx worker 进程数"}]},"modeScripts":{"SYSTEM":{"installScript":"#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get update\n  sudo apt-get install -y nginx\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum install -y epel-release || true\n  sudo yum install -y nginx\nelse\n  echo unsupported package manager\n  exit 1\nfi","startScript":"#!/bin/bash\nset -e\nsudo systemctl start nginx || sudo service nginx start","stopScript":"#!/bin/bash\nset -e\nsudo systemctl stop nginx || sudo service nginx stop","restartScript":"#!/bin/bash\nset -e\nsudo systemctl restart nginx || { sudo service nginx stop; sudo service nginx start; }","statusScript":"#!/bin/bash\nsudo systemctl status nginx --no-pager || ps -ef | grep nginx | grep -v grep","uninstallScript":"#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get remove -y nginx\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum remove -y nginx\nfi"},"DOCKER":{"installScript":"#!/bin/bash\nset -e\ndocker pull ${DOCKER_IMAGE:-nginx:1.24-alpine}\ndocker rm -f ${SERVICE_NAME:-nginx} >/dev/null 2>&1 || true\ndocker run -d --name ${SERVICE_NAME:-nginx} -p 80:80 -p 443:443 ${DOCKER_IMAGE:-nginx:1.24-alpine}","startScript":"#!/bin/bash\nset -e\ndocker start ${SERVICE_NAME:-nginx}","stopScript":"#!/bin/bash\nset -e\ndocker stop ${SERVICE_NAME:-nginx}","restartScript":"#!/bin/bash\nset -e\ndocker restart ${SERVICE_NAME:-nginx}","statusScript":"#!/bin/bash\ndocker ps -a --filter name=${SERVICE_NAME:-nginx}","uninstallScript":"#!/bin/bash\nset -e\ndocker rm -f ${SERVICE_NAME:-nginx} >/dev/null 2>&1 || true\ndocker rmi ${DOCKER_IMAGE:-nginx:1.24-alpine} >/dev/null 2>&1 || true"}},"dockerDefaults":{"image":"nginx:1.24-alpine","scriptMode":"script","dockerfileContent":"FROM nginx:1.24-alpine\nCOPY ./nginx.conf /etc/nginx/nginx.conf"}}'
WHERE sys_project_container_software_preset_id = 1;

UPDATE sys_project_container_software_preset
SET preset_remark = '{"description":"Linux MySQL 双模式预设","serviceName":"mysqld","modeConfigFields":{"SYSTEM":[{"fieldKey":"rootPassword","fieldLabel":"root 密码","componentType":"password","groupName":"system","requiredFlag":false,"defaultValue":"root123456","description":"初始化 root 密码"}],"DOCKER":[{"fieldKey":"rootPassword","fieldLabel":"MYSQL_ROOT_PASSWORD","componentType":"password","groupName":"docker","requiredFlag":true,"defaultValue":"root123456","description":"Docker MySQL root 密码"}]},"modeScripts":{"SYSTEM":{"installScript":"#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get update\n  sudo DEBIAN_FRONTEND=noninteractive apt-get install -y mysql-server\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum install -y mysql-server || sudo yum install -y mariadb-server\nelse\n  echo unsupported package manager\n  exit 1\nfi","startScript":"#!/bin/bash\nset -e\nsudo systemctl start mysqld || sudo systemctl start mysql || sudo service mysqld start || sudo service mysql start","stopScript":"#!/bin/bash\nset -e\nsudo systemctl stop mysqld || sudo systemctl stop mysql || sudo service mysqld stop || sudo service mysql stop","restartScript":"#!/bin/bash\nset -e\nsudo systemctl restart mysqld || sudo systemctl restart mysql || { sudo service mysqld stop; sudo service mysqld start; } || { sudo service mysql stop; sudo service mysql start; }","statusScript":"#!/bin/bash\nsudo systemctl status mysqld --no-pager || sudo systemctl status mysql --no-pager || ps -ef | grep mysql | grep -v grep","uninstallScript":"#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get remove -y mysql-server\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum remove -y mysql-server mariadb-server\nfi"},"DOCKER":{"installScript":"#!/bin/bash\nset -e\ndocker pull ${DOCKER_IMAGE:-mysql:8.0}\ndocker rm -f ${SERVICE_NAME:-mysql8} >/dev/null 2>&1 || true\ndocker run -d --name ${SERVICE_NAME:-mysql8} -e MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD:-root123456} -p 3306:3306 ${DOCKER_IMAGE:-mysql:8.0}","startScript":"#!/bin/bash\nset -e\ndocker start ${SERVICE_NAME:-mysql8}","stopScript":"#!/bin/bash\nset -e\ndocker stop ${SERVICE_NAME:-mysql8}","restartScript":"#!/bin/bash\nset -e\ndocker restart ${SERVICE_NAME:-mysql8}","statusScript":"#!/bin/bash\ndocker ps -a --filter name=${SERVICE_NAME:-mysql8}","uninstallScript":"#!/bin/bash\nset -e\ndocker rm -f ${SERVICE_NAME:-mysql8} >/dev/null 2>&1 || true\ndocker rmi ${DOCKER_IMAGE:-mysql:8.0} >/dev/null 2>&1 || true"}},"dockerDefaults":{"image":"mysql:8.0","scriptMode":"script","dockerfileContent":"FROM mysql:8.0\nENV MYSQL_ROOT_PASSWORD=root123456"}}'
WHERE sys_project_container_software_preset_id = 2;

UPDATE sys_project_container_software_preset
SET preset_remark = '{"description":"Linux Redis 双模式预设","serviceName":"redis","modeConfigFields":{"SYSTEM":[{"fieldKey":"requirepass","fieldLabel":"requirepass","componentType":"password","groupName":"system","requiredFlag":false,"defaultValue":"","description":"Redis 访问密码"}],"DOCKER":[{"fieldKey":"requirepass","fieldLabel":"REDIS_PASSWORD","componentType":"password","groupName":"docker","requiredFlag":false,"defaultValue":"","description":"Redis 容器密码"}]},"modeScripts":{"SYSTEM":{"installScript":"#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get update\n  sudo apt-get install -y redis-server\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum install -y redis\nelse\n  echo unsupported package manager\n  exit 1\nfi","startScript":"#!/bin/bash\nset -e\nsudo systemctl start redis || sudo systemctl start redis-server || sudo service redis start || sudo service redis-server start","stopScript":"#!/bin/bash\nset -e\nsudo systemctl stop redis || sudo systemctl stop redis-server || sudo service redis stop || sudo service redis-server stop","restartScript":"#!/bin/bash\nset -e\nsudo systemctl restart redis || sudo systemctl restart redis-server || { sudo service redis stop; sudo service redis start; }","statusScript":"#!/bin/bash\nsudo systemctl status redis --no-pager || sudo systemctl status redis-server --no-pager || ps -ef | grep redis-server | grep -v grep","uninstallScript":"#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get remove -y redis-server\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum remove -y redis\nfi"},"DOCKER":{"installScript":"#!/bin/bash\nset -e\ndocker pull ${DOCKER_IMAGE:-redis:7.0}\ndocker rm -f ${SERVICE_NAME:-redis7} >/dev/null 2>&1 || true\ndocker run -d --name ${SERVICE_NAME:-redis7} -p 6379:6379 ${DOCKER_IMAGE:-redis:7.0} redis-server --appendonly yes","startScript":"#!/bin/bash\nset -e\ndocker start ${SERVICE_NAME:-redis7}","stopScript":"#!/bin/bash\nset -e\ndocker stop ${SERVICE_NAME:-redis7}","restartScript":"#!/bin/bash\nset -e\ndocker restart ${SERVICE_NAME:-redis7}","statusScript":"#!/bin/bash\ndocker ps -a --filter name=${SERVICE_NAME:-redis7}","uninstallScript":"#!/bin/bash\nset -e\ndocker rm -f ${SERVICE_NAME:-redis7} >/dev/null 2>&1 || true\ndocker rmi ${DOCKER_IMAGE:-redis:7.0} >/dev/null 2>&1 || true"}},"dockerDefaults":{"image":"redis:7.0","scriptMode":"script","dockerfileContent":"FROM redis:7.0\nCMD [\"redis-server\",\"--appendonly\",\"yes\"]"}}'
WHERE sys_project_container_software_preset_id = 3;

UPDATE sys_project_container_software_preset
SET preset_remark = '{"description":"Linux Node.js 环境预设，仅支持 system 安装","serviceName":"node","supportedModes":["SYSTEM"],"modeConfigFields":{"SYSTEM":[{"fieldKey":"nodeVersion","fieldLabel":"Node 版本","componentType":"input","groupName":"system","requiredFlag":false,"defaultValue":"20","description":"Node.js 版本"}]},"modeScripts":{"SYSTEM":{"installScript":"#!/bin/bash\nset -e\ncurl -fsSL https://rpm.nodesource.com/setup_20.x | sudo bash - 2>/dev/null || true\ncurl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash - 2>/dev/null || true\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get install -y nodejs\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum install -y nodejs\nelse\n  echo unsupported package manager\n  exit 1\nfi","startScript":"#!/bin/bash\necho Node.js 是运行时环境，请在具体项目脚本中定义启动命令","stopScript":"#!/bin/bash\necho Node.js 是运行时环境，请在具体项目脚本中定义停止命令","restartScript":"#!/bin/bash\necho Node.js 是运行时环境，请在具体项目脚本中定义重启命令","statusScript":"#!/bin/bash\nnode -v && npm -v","uninstallScript":"#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get remove -y nodejs\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum remove -y nodejs\nfi"}}}'
WHERE sys_project_container_software_preset_id = 4;

UPDATE sys_project_container_software_preset
SET preset_remark = '{"description":"Linux Python 双模式预设","serviceName":"python3","modeConfigFields":{"SYSTEM":[{"fieldKey":"pythonVersion","fieldLabel":"Python 版本","componentType":"input","groupName":"system","requiredFlag":false,"defaultValue":"3.11","description":"Python 版本"}],"DOCKER":[{"fieldKey":"pythonVersion","fieldLabel":"Python 版本","componentType":"input","groupName":"docker","requiredFlag":false,"defaultValue":"3.11","description":"Python 容器版本"}]},"modeScripts":{"SYSTEM":{"installScript":"#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get update\n  sudo apt-get install -y python3 python3-pip python3-venv\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum install -y python3 python3-pip\nelse\n  echo unsupported package manager\n  exit 1\nfi","startScript":"#!/bin/bash\necho Python 为运行时环境，请在具体项目脚本中定义启动命令","stopScript":"#!/bin/bash\necho Python 为运行时环境，请在具体项目脚本中定义停止命令","restartScript":"#!/bin/bash\necho Python 为运行时环境，请在具体项目脚本中定义重启命令","statusScript":"#!/bin/bash\npython3 --version && pip3 --version","uninstallScript":"#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get remove -y python3 python3-pip python3-venv\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum remove -y python3 python3-pip\nfi"},"DOCKER":{"installScript":"#!/bin/bash\nset -e\ndocker pull ${DOCKER_IMAGE:-python:3.11-slim}","startScript":"#!/bin/bash\necho Python Docker 作为运行时基础镜像，不执行常驻服务启动","stopScript":"#!/bin/bash\necho Python Docker 作为运行时基础镜像，无常驻服务可停止","restartScript":"#!/bin/bash\necho Python Docker 作为运行时基础镜像，无常驻服务可重启","statusScript":"#!/bin/bash\ndocker images | grep -E \"python\\s+3.11-slim|python\\s+3.11\" || true","uninstallScript":"#!/bin/bash\nset -e\ndocker rmi ${DOCKER_IMAGE:-python:3.11-slim} >/dev/null 2>&1 || true"}},"dockerDefaults":{"image":"python:3.11-slim","scriptMode":"script","dockerfileContent":"FROM python:3.11-slim\nWORKDIR /app"}}'
WHERE sys_project_container_software_preset_id = 5;

UPDATE sys_project_container_software_preset
SET preset_remark = '{"description":"Linux Maven 双模式预设","serviceName":"mvn","modeConfigFields":{"SYSTEM":[{"fieldKey":"mavenVersion","fieldLabel":"Maven 版本","componentType":"input","groupName":"system","requiredFlag":false,"defaultValue":"3.9","description":"Maven 版本"}],"DOCKER":[{"fieldKey":"mavenVersion","fieldLabel":"Maven 版本","componentType":"input","groupName":"docker","requiredFlag":false,"defaultValue":"3.9","description":"Maven 镜像版本"}]},"modeScripts":{"SYSTEM":{"installScript":"#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get update\n  sudo apt-get install -y maven\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum install -y maven\nelse\n  echo unsupported package manager\n  exit 1\nfi","startScript":"#!/bin/bash\necho Maven 为构建工具，无常驻服务，无需启动","stopScript":"#!/bin/bash\necho Maven 为构建工具，无常驻服务，无需停止","restartScript":"#!/bin/bash\necho Maven 为构建工具，无常驻服务，无需重启","statusScript":"#!/bin/bash\nmvn -version","uninstallScript":"#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get remove -y maven\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum remove -y maven\nfi"},"DOCKER":{"installScript":"#!/bin/bash\nset -e\ndocker pull ${DOCKER_IMAGE:-maven:3.9-eclipse-temurin-17}","startScript":"#!/bin/bash\necho Maven Docker 作为构建镜像，无常驻服务可启动","stopScript":"#!/bin/bash\necho Maven Docker 作为构建镜像，无常驻服务可停止","restartScript":"#!/bin/bash\necho Maven Docker 作为构建镜像，无常驻服务可重启","statusScript":"#!/bin/bash\ndocker images | grep -E \"maven\\s+3.9\" || true","uninstallScript":"#!/bin/bash\nset -e\ndocker rmi ${DOCKER_IMAGE:-maven:3.9-eclipse-temurin-17} >/dev/null 2>&1 || true"}},"dockerDefaults":{"image":"maven:3.9-eclipse-temurin-17","scriptMode":"script","dockerfileContent":"FROM maven:3.9-eclipse-temurin-17\nWORKDIR /workspace"}}'
WHERE sys_project_container_software_preset_id = 6;

UPDATE sys_project_container_software_preset
SET preset_remark = '{"description":"Linux Docker Engine 双模式预设","serviceName":"docker","modeConfigFields":{"SYSTEM":[],"DOCKER":[]},"modeScripts":{"SYSTEM":{"installScript":"#!/bin/bash\nset -e\ncurl -fsSL https://get.docker.com | sh\nsudo usermod -aG docker ${USER:-root} || true","startScript":"#!/bin/bash\nset -e\nsudo systemctl start docker || sudo service docker start","stopScript":"#!/bin/bash\nset -e\nsudo systemctl stop docker || sudo service docker stop","restartScript":"#!/bin/bash\nset -e\nsudo systemctl restart docker || { sudo service docker stop; sudo service docker start; }","statusScript":"#!/bin/bash\nsudo systemctl status docker --no-pager || ps -ef | grep dockerd | grep -v grep","uninstallScript":"#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get remove -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum remove -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin\nfi"},"DOCKER":{"installScript":"#!/bin/bash\necho Docker Engine 本身不支持再以 Docker 模式安装","startScript":"#!/bin/bash\necho Docker Engine 本身不支持再以 Docker 模式启动","stopScript":"#!/bin/bash\necho Docker Engine 本身不支持再以 Docker 模式停止","restartScript":"#!/bin/bash\necho Docker Engine 本身不支持再以 Docker 模式重启","statusScript":"#!/bin/bash\necho Docker Engine 本身不支持再以 Docker 模式检测","uninstallScript":"#!/bin/bash\necho Docker Engine 本身不支持再以 Docker 模式卸载"}},"dockerDefaults":{"image":"docker:24-dind","scriptMode":"script","dockerfileContent":"FROM docker:24-dind"}}'
WHERE sys_project_container_software_preset_id = 10;

UPDATE sys_project_container_software_preset
SET preset_remark = '{"description":"Linux Guacamole 双模式预设","serviceName":"guacd","modeConfigFields":{"SYSTEM":[],"DOCKER":[]},"modeScripts":{"SYSTEM":{"installScript":"#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get update\n  sudo apt-get install -y tomcat9 guacd wget\nelif command -v yum >/dev/null 2>&1 || command -v dnf >/dev/null 2>&1; then\n  PM=$(command -v dnf >/dev/null 2>&1 && echo dnf || echo yum)\n  sudo $PM install -y tomcat guacd wget\nelse\n  echo unsupported package manager\n  exit 1\nfi","startScript":"#!/bin/bash\nset -e\nsudo systemctl start guacd || true\nsudo systemctl start tomcat9 || sudo systemctl start tomcat || true","stopScript":"#!/bin/bash\nset -e\nsudo systemctl stop tomcat9 || sudo systemctl stop tomcat || true\nsudo systemctl stop guacd || true","restartScript":"#!/bin/bash\nset -e\nsudo systemctl restart guacd || true\nsudo systemctl restart tomcat9 || sudo systemctl restart tomcat || true","statusScript":"#!/bin/bash\nsudo systemctl is-active guacd || true\nsudo systemctl is-active tomcat9 || sudo systemctl is-active tomcat || true","uninstallScript":"#!/bin/bash\nset -e\nsudo systemctl stop tomcat9 || sudo systemctl stop tomcat || true\nsudo systemctl stop guacd || true\nsudo rm -f /var/lib/tomcat9/webapps/guacamole.war /var/lib/tomcat/webapps/guacamole.war || true"},"DOCKER":{"installScript":"#!/bin/bash\nset -e\ndocker pull ${DOCKER_IMAGE:-guacamole/guacamole:1.5.5}\ndocker rm -f ${SERVICE_NAME:-guacamole} >/dev/null 2>&1 || true\ndocker run -d --name ${SERVICE_NAME:-guacamole} -p 8080:8080 ${DOCKER_IMAGE:-guacamole/guacamole:1.5.5}","startScript":"#!/bin/bash\nset -e\ndocker start ${SERVICE_NAME:-guacamole}","stopScript":"#!/bin/bash\nset -e\ndocker stop ${SERVICE_NAME:-guacamole}","restartScript":"#!/bin/bash\nset -e\ndocker restart ${SERVICE_NAME:-guacamole}","statusScript":"#!/bin/bash\ndocker ps -a --filter name=${SERVICE_NAME:-guacamole}","uninstallScript":"#!/bin/bash\nset -e\ndocker rm -f ${SERVICE_NAME:-guacamole} >/dev/null 2>&1 || true\ndocker rmi ${DOCKER_IMAGE:-guacamole/guacamole:1.5.5} >/dev/null 2>&1 || true"}},"dockerDefaults":{"image":"guacamole/guacamole:1.5.5","scriptMode":"script","dockerfileContent":"FROM guacamole/guacamole:1.5.5"}}'
WHERE sys_project_container_software_preset_id = 36;

-- Java 多版本预设（system 支持多版本；docker 非必需但保留基础镜像能力）
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
SELECT 40, 'java', 'OpenJDK', 'language', '8', 'mdi:language-java', 'openjdk', 'linux', 'amd64', '/usr/lib/jvm', '',
       '#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get update\n  sudo apt-get install -y openjdk-8-jdk\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum install -y java-1.8.0-openjdk-devel\nelse\n  echo unsupported package manager\n  exit 1\nfi',
       '#!/bin/bash\necho Java 运行时无常驻服务，无需启动',
       '#!/bin/bash\necho Java 运行时无常驻服务，无需停止',
       '#!/bin/bash\necho Java 运行时无常驻服务，无需重启',
       '#!/bin/bash\njava -version',
       '#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get remove -y openjdk-8-jdk\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum remove -y java-1.8.0-openjdk-devel\nfi',
       1, 0, 1, 200,
       '{"description":"OpenJDK 8 环境预设，仅支持 system 多版本安装","serviceName":"java","supportedModes":["SYSTEM"],"modeConfigFields":{"SYSTEM":[{"fieldKey":"javaHome","fieldLabel":"JAVA_HOME","componentType":"input","groupName":"system","requiredFlag":false,"defaultValue":"/usr/lib/jvm/java-8-openjdk","description":"Java 安装目录"}]},"modeScripts":{"SYSTEM":{"installScript":"#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get update\n  sudo apt-get install -y openjdk-8-jdk\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum install -y java-1.8.0-openjdk-devel\nelse\n  echo unsupported package manager\n  exit 1\nfi","startScript":"#!/bin/bash\necho Java 运行时无常驻服务，无需启动","stopScript":"#!/bin/bash\necho Java 运行时无常驻服务，无需停止","restartScript":"#!/bin/bash\necho Java 运行时无常驻服务，无需重启","statusScript":"#!/bin/bash\njava -version","uninstallScript":"#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get remove -y openjdk-8-jdk\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum remove -y java-1.8.0-openjdk-devel\nfi"}}}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE sys_project_container_software_preset_id = 40
);

INSERT INTO sys_project_container_software_preset (
    sys_project_container_software_preset_id, software_code, software_name, software_category, software_version,
    software_icon, software_vendor, os_type, software_arch, install_path, expose_ports,
    install_script, start_script, stop_script, restart_script, status_script, uninstall_script,
    preset_builtin, preset_delete, preset_status, preset_sort, preset_remark
)
SELECT 41, 'java', 'OpenJDK', 'language', '11', 'mdi:language-java', 'openjdk', 'linux', 'amd64', '/usr/lib/jvm', '',
       '#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get update\n  sudo apt-get install -y openjdk-11-jdk\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum install -y java-11-openjdk-devel\nelse\n  echo unsupported package manager\n  exit 1\nfi',
       '#!/bin/bash\necho Java 运行时无常驻服务，无需启动',
       '#!/bin/bash\necho Java 运行时无常驻服务，无需停止',
       '#!/bin/bash\necho Java 运行时无常驻服务，无需重启',
       '#!/bin/bash\njava -version',
       '#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get remove -y openjdk-11-jdk\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum remove -y java-11-openjdk-devel\nfi',
       1, 0, 1, 210,
       '{"description":"OpenJDK 11 环境预设，仅支持 system 多版本安装","serviceName":"java","supportedModes":["SYSTEM"],"modeConfigFields":{"SYSTEM":[{"fieldKey":"javaHome","fieldLabel":"JAVA_HOME","componentType":"input","groupName":"system","requiredFlag":false,"defaultValue":"/usr/lib/jvm/java-11-openjdk","description":"Java 安装目录"}]},"modeScripts":{"SYSTEM":{"installScript":"#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get update\n  sudo apt-get install -y openjdk-11-jdk\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum install -y java-11-openjdk-devel\nelse\n  echo unsupported package manager\n  exit 1\nfi","startScript":"#!/bin/bash\necho Java 运行时无常驻服务，无需启动","stopScript":"#!/bin/bash\necho Java 运行时无常驻服务，无需停止","restartScript":"#!/bin/bash\necho Java 运行时无常驻服务，无需重启","statusScript":"#!/bin/bash\njava -version","uninstallScript":"#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get remove -y openjdk-11-jdk\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum remove -y java-11-openjdk-devel\nfi"}}}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE sys_project_container_software_preset_id = 41
  );

INSERT INTO sys_project_container_software_preset (
    sys_project_container_software_preset_id, software_code, software_name, software_category, software_version,
    software_icon, software_vendor, os_type, software_arch, install_path, expose_ports,
    install_script, start_script, stop_script, restart_script, status_script, uninstall_script,
    preset_builtin, preset_delete, preset_status, preset_sort, preset_remark
)
SELECT 42, 'java', 'OpenJDK', 'language', '17', 'mdi:language-java', 'openjdk', 'linux', 'amd64', '/usr/lib/jvm', '',
       '#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get update\n  sudo apt-get install -y openjdk-17-jdk\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum install -y java-17-openjdk-devel\nelse\n  echo unsupported package manager\n  exit 1\nfi',
       '#!/bin/bash\necho Java 运行时无常驻服务，无需启动',
       '#!/bin/bash\necho Java 运行时无常驻服务，无需停止',
       '#!/bin/bash\necho Java 运行时无常驻服务，无需重启',
       '#!/bin/bash\njava -version',
       '#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get remove -y openjdk-17-jdk\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum remove -y java-17-openjdk-devel\nfi',
       1, 0, 1, 220,
       '{"description":"OpenJDK 17 环境预设，仅支持 system 多版本安装","serviceName":"java","supportedModes":["SYSTEM"],"modeConfigFields":{"SYSTEM":[{"fieldKey":"javaHome","fieldLabel":"JAVA_HOME","componentType":"input","groupName":"system","requiredFlag":false,"defaultValue":"/usr/lib/jvm/java-17-openjdk","description":"Java 安装目录"}]},"modeScripts":{"SYSTEM":{"installScript":"#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get update\n  sudo apt-get install -y openjdk-17-jdk\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum install -y java-17-openjdk-devel\nelse\n  echo unsupported package manager\n  exit 1\nfi","startScript":"#!/bin/bash\necho Java 运行时无常驻服务，无需启动","stopScript":"#!/bin/bash\necho Java 运行时无常驻服务，无需停止","restartScript":"#!/bin/bash\necho Java 运行时无常驻服务，无需重启","statusScript":"#!/bin/bash\njava -version","uninstallScript":"#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get remove -y openjdk-17-jdk\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum remove -y java-17-openjdk-devel\nfi"}}}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE sys_project_container_software_preset_id = 42
);

INSERT INTO sys_project_container_software_preset (
    sys_project_container_software_preset_id, software_code, software_name, software_category, software_version,
    software_icon, software_vendor, os_type, software_arch, install_path, expose_ports,
    install_script, start_script, stop_script, restart_script, status_script, uninstall_script,
    preset_builtin, preset_delete, preset_status, preset_sort, preset_remark
)
SELECT 43, 'java', 'OpenJDK', 'language', '21', 'mdi:language-java', 'openjdk', 'linux', 'amd64', '/usr/lib/jvm', '',
       '#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get update\n  sudo apt-get install -y openjdk-21-jdk\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum install -y java-21-openjdk-devel\nelse\n  echo unsupported package manager\n  exit 1\nfi',
       '#!/bin/bash\necho Java 运行时无常驻服务，无需启动',
       '#!/bin/bash\necho Java 运行时无常驻服务，无需停止',
       '#!/bin/bash\necho Java 运行时无常驻服务，无需重启',
       '#!/bin/bash\njava -version',
       '#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get remove -y openjdk-21-jdk\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum remove -y java-21-openjdk-devel\nfi',
       1, 0, 1, 230,
       '{"description":"OpenJDK 21 环境预设，仅支持 system 多版本安装","serviceName":"java","supportedModes":["SYSTEM"],"modeConfigFields":{"SYSTEM":[{"fieldKey":"javaHome","fieldLabel":"JAVA_HOME","componentType":"input","groupName":"system","requiredFlag":false,"defaultValue":"/usr/lib/jvm/java-21-openjdk","description":"Java 安装目录"}]},"modeScripts":{"SYSTEM":{"installScript":"#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get update\n  sudo apt-get install -y openjdk-21-jdk\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum install -y java-21-openjdk-devel\nelse\n  echo unsupported package manager\n  exit 1\nfi","startScript":"#!/bin/bash\necho Java 运行时无常驻服务，无需启动","stopScript":"#!/bin/bash\necho Java 运行时无常驻服务，无需停止","restartScript":"#!/bin/bash\necho Java 运行时无常驻服务，无需重启","statusScript":"#!/bin/bash\njava -version","uninstallScript":"#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get remove -y openjdk-21-jdk\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum remove -y java-21-openjdk-devel\nfi"}}}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE sys_project_container_software_preset_id = 43
);

-- Docker Engine 预设（核心容器运行时）
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
SELECT 10, 'docker', 'Docker Engine', 'ops', '24', 'mdi:docker', 'docker', 'linux', 'amd64', '/var/lib/docker', '2375,2376',
       '#!/bin/bash\nset -e\ncurl -fsSL https://get.docker.com | sh\nsudo usermod -aG docker ${USER:-root} || true',
       '#!/bin/bash\nset -e\nsudo systemctl start docker || sudo service docker start',
       '#!/bin/bash\nset -e\nsudo systemctl stop docker || sudo service docker stop',
       '#!/bin/bash\nset -e\nsudo systemctl restart docker || { sudo service docker stop; sudo service docker start; }',
       '#!/bin/bash\nsudo systemctl status docker --no-pager || ps -ef | grep dockerd | grep -v grep',
       '#!/bin/bash\nset -e\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get remove -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin\nelif command -v yum >/dev/null 2>&1; then\n  sudo yum remove -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin\nfi',
       1, 0, 1, 100, 'Linux Docker Engine 预设，提供容器运行时环境'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE sys_project_container_software_preset_id = 10
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
SELECT 36, 'guacamole', 'Apache Guacamole', 'ops', '1.5.5', 'mdi:remote-desktop', 'apache', 'linux', 'amd64', '/etc/guacamole', '8080',
       '#!/bin/bash\nset -euo pipefail\nGUAC_VERSION=${GUAC_VERSION:-1.5.5}\nGUAC_HOME=${GUAC_HOME:-/etc/guacamole}\nGUAC_USER=${GUAC_USER:-guacadmin}\nGUAC_PASSWORD=${GUAC_PASSWORD:-guacadmin}\necho [guacamole] installing\nif command -v apt-get >/dev/null 2>&1; then\n  sudo apt-get update\n  sudo apt-get install -y tomcat9 guacd wget || sudo apt-get install -y tomcat9 wget\n  TOMCAT_SERVICE=tomcat9\n  TOMCAT_WEBAPPS=/var/lib/tomcat9/webapps\nelif command -v yum >/dev/null 2>&1 || command -v dnf >/dev/null 2>&1; then\n  PM=$(command -v dnf >/dev/null 2>&1 && echo dnf || echo yum)\n  sudo $PM install -y epel-release || true\n  sudo $PM install -y tomcat guacd wget || sudo $PM install -y tomcat wget\n  TOMCAT_SERVICE=tomcat\n  TOMCAT_WEBAPPS=/var/lib/tomcat/webapps\nelse\n  echo unsupported package manager\n  exit 1\nfi\nsudo mkdir -p \"$GUAC_HOME\" \"$TOMCAT_WEBAPPS\"\nsudo wget -q -O \"$TOMCAT_WEBAPPS/guacamole.war\" \"https://archive.apache.org/dist/guacamole/$GUAC_VERSION/binary/guacamole-$GUAC_VERSION.war\"\nsudo tee \"$GUAC_HOME/guacamole.properties\" >/dev/null <<EOF\nguacd-hostname: localhost\nguacd-port: 4822\nEOF\nsudo tee \"$GUAC_HOME/user-mapping.xml\" >/dev/null <<EOF\n<user-mapping>\n  <authorize username=\"$GUAC_USER\" password=\"$GUAC_PASSWORD\">\n  </authorize>\n</user-mapping>\nEOF\nsudo mkdir -p /usr/share/tomcat9 /var/lib/tomcat9 /usr/share/tomcat /var/lib/tomcat\nsudo ln -sfn \"$GUAC_HOME\" /usr/share/tomcat9/.guacamole || true\nsudo ln -sfn \"$GUAC_HOME\" /var/lib/tomcat9/.guacamole || true\nsudo ln -sfn \"$GUAC_HOME\" /usr/share/tomcat/.guacamole || true\nsudo ln -sfn \"$GUAC_HOME\" /var/lib/tomcat/.guacamole || true\nsudo systemctl enable guacd || true\nsudo systemctl enable \"$TOMCAT_SERVICE\" || true\nsudo systemctl restart guacd || sudo service guacd restart || true\nsudo systemctl restart \"$TOMCAT_SERVICE\" || sudo service \"$TOMCAT_SERVICE\" restart\necho [guacamole] install finished',
       '#!/bin/bash\nset -e\nsudo systemctl start guacd || sudo service guacd start || true\nsudo systemctl start tomcat9 || sudo systemctl start tomcat || sudo service tomcat9 start || sudo service tomcat start',
       '#!/bin/bash\nset -e\nsudo systemctl stop tomcat9 || sudo systemctl stop tomcat || sudo service tomcat9 stop || sudo service tomcat stop\nsudo systemctl stop guacd || sudo service guacd stop || true',
       '#!/bin/bash\nset -e\nsudo systemctl restart guacd || sudo service guacd restart || true\nsudo systemctl restart tomcat9 || sudo systemctl restart tomcat || sudo service tomcat9 restart || sudo service tomcat restart',
       '#!/bin/bash\n(sudo systemctl is-active guacd || true) && (sudo systemctl is-active tomcat9 || sudo systemctl is-active tomcat || ps -ef | grep ''[t]omcat'')',
       '#!/bin/bash\nset -e\nsudo systemctl stop tomcat9 || sudo systemctl stop tomcat || true\nsudo systemctl stop guacd || true\nsudo rm -f /var/lib/tomcat9/webapps/guacamole.war /var/lib/tomcat/webapps/guacamole.war\necho [guacamole] webapp removed, config kept: /etc/guacamole',
       1, 0, 1, 150, 'Linux Guacamole 预设'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE software_code = 'guacamole'
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
