-- ============================================================
-- 种子数据
-- ============================================================

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

-- Nginx 软件预设（DOCKER 模式专用，卷映射和环境变量完整预配置）
INSERT INTO sys_project_container_software_preset (
    software_code, service_name, software_name, software_category, preset_type, software_version,
    os_type, install_path, expose_ports, has_port_mapping,
    install_script, start_script, stop_script, restart_script, status_script, uninstall_script,
    preset_builtin, preset_delete, preset_status, preset_sort,
    preset_remark,
    docker_env_vars
)
SELECT
    'nginx', 'nginx', 'Nginx', 'webserver', 'SOFTWARE', '1.24',
    'linux', '/usr/local/nginx', '80,443', 1,
    '', '', '', '', '', '',
    1, 0, 1, 0,
    '{"description":"高性能 Web 服务器及反向代理","serviceName":"nginx","supportedModes":["DOCKER"],"dockerDefaults":{"image":"nginx","tags":["1.24","1.25","alpine","latest","alpine-perl"],"scriptMode":"script","env":{"TZ":"Asia/Shanghai"},"volumeMappings":[{"hostPath":"/data/nginx/html","containerPath":"/usr/share/nginx/html"},{"hostPath":"/data/nginx/conf","containerPath":"/etc/nginx/conf.d"},{"hostPath":"/data/nginx/logs","containerPath":"/var/log/nginx"}]},"configFields":[{"fieldKey":"nginx_root","fieldLabel":"站点根目录","fieldType":"text","defaultValue":"/usr/share/nginx/html","placeholder":"容器内站点文件路径"},{"fieldKey":"nginx_server_name","fieldLabel":"服务器名称","fieldType":"text","defaultValue":"localhost","placeholder":"server_name 指令值"}]}',
    '{"TZ":"Asia/Shanghai"}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE software_code = 'nginx'
);

-- Apache Guacamole 预设（DOCKERFILE 类型，仅 docker 模式）
INSERT INTO sys_project_container_software_preset (
    software_code, service_name, software_name, software_category, preset_type, software_version,
    os_type, expose_ports, has_port_mapping,
    install_script,
    preset_builtin, preset_delete, preset_status, preset_sort,
    preset_remark,
    docker_env_vars
)
SELECT
    'guacamole', 'guacamole', 'Apache Guacamole', 'remote', 'DOCKERFILE', '1.5.5',
    'linux', '8080', 1,
    '',
    1, 0, 1, 1,
    '{"description":"Apache Guacamole — 无客户端远程桌面网关，支持 RDP/VNC/SSH","serviceName":"guacamole","supportedModes":["DOCKER"],"dockerDefaults":{"image":"guacamole/guacamole","tags":["1.5.5","latest"],"scriptMode":"dockerfile","dockerfileContent":"FROM guacamole/guacamole:1.5.5\\nEXPOSE 8080\\nCMD [\\"/opt/guacamole/bin/start.sh\\"]","env":{"TZ":"Asia/Shanghai","GUACD_HOSTNAME":"guacd","MYSQL_HOSTNAME":"mysql","MYSQL_PORT":"3306","MYSQL_DATABASE":"guacamole_db","MYSQL_USER":"guacamole_user","MYSQL_PASSWORD":"guacamole_pass"},"volumeMappings":[{"hostPath":"/data/guacamole/config","containerPath":"/config"},{"hostPath":"/data/guacamole/recordings","containerPath":"/recordings"}]}}',
    '{"TZ":"Asia/Shanghai"}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE software_code = 'guacamole'
);

-- ============================================================
-- 常用软件预设（Docker 模式）
-- 配置相同，通过 tags 支持多版本
-- ============================================================

-- MySQL 数据库
INSERT INTO sys_project_container_software_preset (
    software_code, service_name, software_name, software_category, preset_type, software_version,
    software_icon, os_type, expose_ports, has_port_mapping,
    install_script,
    preset_builtin, preset_delete, preset_status, preset_sort,
    preset_remark,
    docker_env_vars
)
SELECT
    'mysql', 'mysql', 'MySQL', 'database', 'SOFTWARE', '8.0',
    'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/mysql.svg',
    'linux', '3306', 1,
    '',
    1, 0, 1, 2,
    '{"description":"MySQL 关系型数据库","serviceName":"mysql","supportedModes":["DOCKER"],"dockerDefaults":{"image":"mysql:8.0","tags":["5.7","8.0","8.4","9.0","latest"],"scriptMode":"script","env":{"MYSQL_ROOT_PASSWORD":"root123456","MYSQL_DATABASE":"myapp","MYSQL_USER":"appuser","MYSQL_PASSWORD":"apppass","TZ":"Asia/Shanghai","character_set_server":"utf8mb4","collation_server":"utf8mb4_unicode_ci","lower_case_table_names":"1","max_connections":"200"},"volumeMappings":[{"hostPath":"/data/mysql/data","containerPath":"/var/lib/mysql"},{"hostPath":"/data/mysql/conf","containerPath":"/etc/mysql/conf.d"},{"hostPath":"/data/mysql/init","containerPath":"/docker-entrypoint-initdb.d"}]}}',
    '{"TZ":"Asia/Shanghai","MYSQL_ROOT_PASSWORD":"root123456"}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE software_code = 'mysql'
);

-- Redis 缓存
INSERT INTO sys_project_container_software_preset (
    software_code, service_name, software_name, software_category, preset_type, software_version,
    software_icon, os_type, expose_ports, has_port_mapping,
    install_script,
    preset_builtin, preset_delete, preset_status, preset_sort,
    preset_remark,
    docker_env_vars
)
SELECT
    'redis', 'redis', 'Redis', 'middleware', 'SOFTWARE', '7-alpine',
    'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/redis.svg',
    'linux', '6379', 1,
    '',
    1, 0, 1, 3,
    '{"description":"Redis 内存缓存数据库","serviceName":"redis","supportedModes":["DOCKER"],"dockerDefaults":{"image":"redis","tags":["7-alpine","7","8-alpine","8","alpine","latest"],"scriptMode":"script","env":{"TZ":"Asia/Shanghai"},"volumeMappings":[{"hostPath":"/data/redis/data","containerPath":"/data"},{"hostPath":"/data/redis/conf","containerPath":"/etc/redis"}]}}',
    '{"TZ":"Asia/Shanghai"}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE software_code = 'redis'
);

-- Redis Stack（含高级功能）
INSERT INTO sys_project_container_software_preset (
    software_code, service_name, software_name, software_category, preset_type, software_version,
    software_icon, os_type, expose_ports, has_port_mapping,
    install_script,
    preset_builtin, preset_delete, preset_status, preset_sort,
    preset_remark,
    docker_env_vars
)
SELECT
    'redis-stack', 'redis-stack', 'Redis Stack', 'middleware', 'SOFTWARE', 'latest',
    'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/redis.svg',
    'linux', '6379,8001', 1,
    '',
    1, 0, 1, 4,
    '{"description":"Redis Stack — 集成 RedisJSON、RedisSearch、RedisTimeSeries、RedisBloom 等模块","serviceName":"redis-stack","supportedModes":["DOCKER"],"dockerDefaults":{"image":"redis/redis-stack-server","tags":["latest"],"scriptMode":"script","env":{"REDIS_ARGS":"--requirepass redis123","TZ":"Asia/Shanghai"},"volumeMappings":[{"hostPath":"/data/redis-stack/data","containerPath":"/data"}]}}',
    '{"TZ":"Asia/Shanghai"}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE software_code = 'redis-stack'
);

-- MongoDB
INSERT INTO sys_project_container_software_preset (
    software_code, service_name, software_name, software_category, preset_type, software_version,
    software_icon, os_type, expose_ports, has_port_mapping,
    install_script,
    preset_builtin, preset_delete, preset_status, preset_sort,
    preset_remark,
    docker_env_vars
)
SELECT
    'mongodb', 'mongodb', 'MongoDB', 'database', 'SOFTWARE', '7',
    'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/mongodb.svg',
    'linux', '27017', 1,
    '',
    1, 0, 1, 5,
    '{"description":"MongoDB 文档数据库","serviceName":"mongodb","supportedModes":["DOCKER"],"dockerDefaults":{"image":"mongo:7","tags":["7","7.0","6","latest"],"scriptMode":"script","env":{"MONGO_INITDB_ROOT_USERNAME":"admin","MONGO_INITDB_ROOT_PASSWORD":"admin123","TZ":"Asia/Shanghai"},"volumeMappings":[{"hostPath":"/data/mongodb/data","containerPath":"/data/db"}]}}',
    '{"TZ":"Asia/Shanghai","MONGO_INITDB_ROOT_USERNAME":"admin","MONGO_INITDB_ROOT_PASSWORD":"admin123"}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE software_code = 'mongodb'
);

-- PostgreSQL
INSERT INTO sys_project_container_software_preset (
    software_code, service_name, software_name, software_category, preset_type, software_version,
    software_icon, os_type, expose_ports, has_port_mapping,
    install_script,
    preset_builtin, preset_delete, preset_status, preset_sort,
    preset_remark,
    docker_env_vars
)
SELECT
    'postgresql', 'postgresql', 'PostgreSQL', 'database', 'SOFTWARE', '16',
    'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/postgresql.svg',
    'linux', '5432', 1,
    '',
    1, 0, 1, 6,
    '{"description":"PostgreSQL 关系型数据库","serviceName":"postgresql","supportedModes":["DOCKER"],"dockerDefaults":{"image":"postgres:16","tags":["16","15","14","13","latest"],"scriptMode":"script","env":{"POSTGRES_PASSWORD":"postgres123","POSTGRES_DB":"myapp","TZ":"Asia/Shanghai"},"volumeMappings":[{"hostPath":"/data/postgresql/data","containerPath":"/var/lib/postgresql/data"},{"hostPath":"/data/postgresql/init","containerPath":"/docker-entrypoint-initdb.d"}]}}',
    '{"TZ":"Asia/Shanghai","POSTGRES_PASSWORD":"postgres123"}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE software_code = 'postgresql'
);

-- MariaDB
INSERT INTO sys_project_container_software_preset (
    software_code, service_name, software_name, software_category, preset_type, software_version,
    software_icon, os_type, expose_ports, has_port_mapping,
    install_script,
    preset_builtin, preset_delete, preset_status, preset_sort,
    preset_remark,
    docker_env_vars
)
SELECT
    'mariadb', 'mariadb', 'MariaDB', 'database', 'SOFTWARE', '11',
    'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/mariadb.svg',
    'linux', '3306', 1,
    '',
    1, 0, 1, 7,
    '{"description":"MariaDB 关系型数据库（MySQL 兼容）","serviceName":"mariadb","supportedModes":["DOCKER"],"dockerDefaults":{"image":"mariadb:11","tags":["11","10.11","10.6","latest"],"scriptMode":"script","env":{"MYSQL_ROOT_PASSWORD":"root123456","MYSQL_DATABASE":"myapp","TZ":"Asia/Shanghai","character_set_server":"utf8mb4","collation_server":"utf8mb4_unicode_ci","lower_case_table_names":"1"},"volumeMappings":[{"hostPath":"/data/mariadb/data","containerPath":"/var/lib/mysql"},{"hostPath":"/data/mariadb/conf","containerPath":"/etc/mysql/conf.d"}]}}',
    '{"TZ":"Asia/Shanghai","MYSQL_ROOT_PASSWORD":"root123456"}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE software_code = 'mariadb'
);

-- ZooKeeper
INSERT INTO sys_project_container_software_preset (
    software_code, service_name, software_name, software_category, preset_type, software_version,
    software_icon, os_type, expose_ports, has_port_mapping,
    install_script,
    preset_builtin, preset_delete, preset_status, preset_sort,
    preset_remark,
    docker_env_vars
)
SELECT
    'zookeeper', 'zookeeper', 'ZooKeeper', 'middleware', 'SOFTWARE', '3.9',
    'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/apachezookeeper.svg',
    'linux', '2181', 1,
    '',
    1, 0, 1, 8,
    '{"description":"Apache ZooKeeper — 分布式协调服务","serviceName":"zookeeper","supportedModes":["DOCKER"],"dockerDefaults":{"image":"bitnami/zookeeper","tags":["3.9","3.8","latest"],"scriptMode":"script","env":{"ALLOW_ANONYMOUS_LOGIN":"yes","ZOO_MY_ID":"1","TZ":"Asia/Shanghai"},"volumeMappings":[{"hostPath":"/data/zookeeper/data","containerPath":"/bitnami/zookeeper"}]}}',
    '{"TZ":"Asia/Shanghai","ALLOW_ANONYMOUS_LOGIN":"yes"}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE software_code = 'zookeeper'
);

-- Apache Kafka（Dockerfile 构建，需依赖 ZooKeeper）
INSERT INTO sys_project_container_software_preset (
    software_code, service_name, software_name, software_category, preset_type, software_version,
    software_icon, os_type, expose_ports, has_port_mapping,
    install_script,
    preset_builtin, preset_delete, preset_status, preset_sort,
    preset_remark,
    docker_env_vars
)
SELECT
    'kafka', 'kafka', 'Apache Kafka', 'middleware', 'DOCKERFILE', '7',
    'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/apachekafka.svg',
    'linux', '9092', 1,
    '',
    1, 0, 1, 9,
    '{"description":"Apache Kafka — 分布式消息队列系统（Dockerfile 构建，运行时需连接 ZooKeeper）","serviceName":"kafka","supportedModes":["DOCKER"],"dockerDefaults":{"image":"confluentinc/cp-kafka","tags":["7","7.6","latest"],"scriptMode":"dockerfile","dockerfileContent":"FROM confluentinc/cp-kafka:7\\nEXPOSE 9092\\nCMD [\\"/etc/confluent/docker/run\\"]","env":{"KAFKA_BROKER_ID":"1","KAFKA_ZOOKEEPER_CONNECT":"zookeeper:2181","KAFKA_ADVERTISED_LISTENERS":"PLAINTEXT://localhost:9092","TZ":"Asia/Shanghai"},"volumeMappings":[{"hostPath":"/data/kafka/data","containerPath":"/var/lib/kafka/data"}]}}',
    '{"TZ":"Asia/Shanghai","KAFKA_BROKER_ID":"1"}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE software_code = 'kafka'
);

-- Nacos 注册中心
INSERT INTO sys_project_container_software_preset (
    software_code, service_name, software_name, software_category, preset_type, software_version,
    software_icon, os_type, expose_ports, has_port_mapping,
    install_script,
    preset_builtin, preset_delete, preset_status, preset_sort,
    preset_remark,
    docker_env_vars
)
SELECT
    'nacos', 'nacos', 'Nacos', 'middleware', 'SOFTWARE', '2.3',
    'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/nacos.svg',
    'linux', '8848,9848', 1,
    '',
    1, 0, 1, 10,
    '{"description":"Nacos — 动态服务发现、配置管理和服务管理平台","serviceName":"nacos","supportedModes":["DOCKER"],"dockerDefaults":{"image":"nacos/nacos-server","tags":["v2.3","v2.2","latest"],"scriptMode":"script","env":{"MODE":"standalone","TZ":"Asia/Shanghai"},"volumeMappings":[{"hostPath":"/data/nacos/logs","containerPath":"/home/nacos/logs"}]}}',
    '{"TZ":"Asia/Shanghai","MODE":"standalone"}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE software_code = 'nacos'
);

-- MinIO 对象存储
INSERT INTO sys_project_container_software_preset (
    software_code, service_name, software_name, software_category, preset_type, software_version,
    software_icon, os_type, expose_ports, has_port_mapping,
    install_script,
    preset_builtin, preset_delete, preset_status, preset_sort,
    preset_remark,
    docker_env_vars
)
SELECT
    'minio', 'minio', 'MinIO', 'middleware', 'SOFTWARE', 'latest',
    'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/minio.svg',
    'linux', '9000,9001', 1,
    '',
    1, 0, 1, 11,
    '{"description":"MinIO — 高性能 S3 兼容对象存储","serviceName":"minio","supportedModes":["DOCKER"],"dockerDefaults":{"image":"minio/minio","tags":["latest"],"scriptMode":"script","env":{"MINIO_ROOT_USER":"minioadmin","MINIO_ROOT_PASSWORD":"minioadmin","TZ":"Asia/Shanghai"},"volumeMappings":[{"hostPath":"/data/minio/data","containerPath":"/data"}]}}',
    '{"TZ":"Asia/Shanghai","MINIO_ROOT_USER":"minioadmin","MINIO_ROOT_PASSWORD":"minioadmin"}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE software_code = 'minio'
);

-- RabbitMQ 消息队列
INSERT INTO sys_project_container_software_preset (
    software_code, service_name, software_name, software_category, preset_type, software_version,
    software_icon, os_type, expose_ports, has_port_mapping,
    install_script,
    preset_builtin, preset_delete, preset_status, preset_sort,
    preset_remark,
    docker_env_vars
)
SELECT
    'rabbitmq', 'rabbitmq', 'RabbitMQ', 'middleware', 'SOFTWARE', '3-management',
    'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/rabbitmq.svg',
    'linux', '5672,15672', 1,
    '',
    1, 0, 1, 12,
    '{"description":"RabbitMQ — 高级消息队列协议（AMQP）消息代理","serviceName":"rabbitmq","supportedModes":["DOCKER"],"dockerDefaults":{"image":"rabbitmq","tags":["3-management","3.13-management","3.12-management","latest"],"scriptMode":"script","env":{"RABBITMQ_DEFAULT_USER":"admin","RABBITMQ_DEFAULT_PASS":"admin123","TZ":"Asia/Shanghai"},"volumeMappings":[{"hostPath":"/data/rabbitmq/data","containerPath":"/var/lib/rabbitmq"}]}}',
    '{"TZ":"Asia/Shanghai","RABBITMQ_DEFAULT_USER":"admin","RABBITMQ_DEFAULT_PASS":"admin123"}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE software_code = 'rabbitmq'
);

-- Elasticsearch
INSERT INTO sys_project_container_software_preset (
    software_code, service_name, software_name, software_category, preset_type, software_version,
    software_icon, os_type, expose_ports, has_port_mapping,
    install_script,
    preset_builtin, preset_delete, preset_status, preset_sort,
    preset_remark,
    docker_env_vars
)
SELECT
    'elasticsearch', 'elasticsearch', 'Elasticsearch', 'middleware', 'SOFTWARE', '8',
    'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/elasticsearch.svg',
    'linux', '9200,9300', 1,
    '',
    1, 0, 1, 13,
    '{"description":"Elasticsearch — 分布式搜索和分析引擎","serviceName":"elasticsearch","supportedModes":["DOCKER"],"dockerDefaults":{"image":"elasticsearch","tags":["8","8.14","8.13","7","latest"],"scriptMode":"script","env":{"discovery.type":"single-node","ES_JAVA_OPTS":"-Xms512m -Xmx512m","TZ":"Asia/Shanghai"},"volumeMappings":[{"hostPath":"/data/elasticsearch/data","containerPath":"/usr/share/elasticsearch/data"}]}}',
    '{"TZ":"Asia/Shanghai","discovery.type":"single-node","ES_JAVA_OPTS":"-Xms512m -Xmx512m"}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE software_code = 'elasticsearch'
);

-- ============================================================
-- 绑定数据
-- ============================================================

-- 为测试项目绑定 Nginx 预设
INSERT INTO sys_project_container_software_binding (
    sys_project_id, sys_project_container_software_preset_id,
    software_code, software_name, service_name, software_category, software_version, software_icon,
    expose_ports, has_port_mapping, install_status, runtime_status,
    binding_delete, binding_status, create_name, create_time
)
SELECT
    1, p.sys_project_container_software_preset_id,
    p.software_code, p.software_name, p.service_name, p.software_category, p.software_version, p.software_icon,
    p.expose_ports, p.has_port_mapping,
    'NOT_INSTALLED', 'STOPPED',
    0, 1, 'panel-test', CURRENT_TIMESTAMP
FROM sys_project_container_software_preset p
WHERE p.software_code = 'nginx'
  AND NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_binding b
    WHERE b.sys_project_id = 1 AND b.software_code = 'nginx'
);
