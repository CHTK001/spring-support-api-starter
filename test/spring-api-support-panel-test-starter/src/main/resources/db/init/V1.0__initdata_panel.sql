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
    '{"description":"Apache Guacamole — 无客户端远程桌面网关，支持 RDP/VNC/SSH","serviceName":"guacamole","supportedModes":["DOCKER"],"dockerDefaults":{"image":"guacamole/guacamole","tags":["1.5.5","latest"],"scriptMode":"dockerfile","dockerfileContent":"FROM guacamole/guacamole:1.5.5\\nUSER root\\nRUN sed -i ''/if \\\\[ -r .*setclasspath.sh \\\\]; then/,/^  fi$/c\\\\  . \\"$CATALINA_HOME\\"/bin/setclasspath.sh'' /usr/local/tomcat/bin/catalina.sh && sed -i ''s/\\\\[ -x/\\\\[ -f/g; s/\\\\[ ! -x/\\\\[ ! -f/g'' /usr/local/tomcat/bin/setclasspath.sh\\nUSER guacamole\\nEXPOSE 8080\\nCMD [\\"/opt/guacamole/bin/start.sh\\"]","dockerSecurityOpts":["seccomp=unconfined"],"env":{"TZ":"Asia/Shanghai","GUACD_HOSTNAME":"guacd","MYSQL_HOSTNAME":"mysql","MYSQL_PORT":"3306","MYSQL_DATABASE":"guacamole_db","MYSQL_USER":"guacamole_user","MYSQL_PASSWORD":"guacamole_pass"},"volumeMappings":[{"hostPath":"/data/guacamole/config","containerPath":"/config"},{"hostPath":"/data/guacamole/recordings","containerPath":"/recordings"}]}}',
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
    '{"description":"MySQL 关系型数据库","serviceName":"mysql","supportedModes":["DOCKER"],"dockerDefaults":{"image":"mysql:8.0","tags":["5.7","8.0","8.4","9.0","latest"],"scriptMode":"script","env":{"MYSQL_ROOT_PASSWORD":"root123456","MYSQL_DATABASE":"myapp","MYSQL_USER":"appuser","MYSQL_PASSWORD":"apppass","TZ":"Asia/Shanghai","character_set_server":"utf8mb4","collation_server":"utf8mb4_unicode_ci","lower_case_table_names":"1","max_connections":"200"},"envDescriptions":{"MYSQL_ROOT_PASSWORD":"MySQL root 用户密码","MYSQL_DATABASE":"初始化时自动创建的数据库","MYSQL_USER":"初始化时创建的普通用户","MYSQL_PASSWORD":"普通用户的密码","TZ":"容器时区设置","character_set_server":"服务器字符集编码","collation_server":"服务器排序规则","lower_case_table_names":"表名大小写敏感（0=敏感，1=不敏感）","max_connections":"最大并发连接数"},"volumeMappings":[{"hostPath":"/data/mysql/data","containerPath":"/var/lib/mysql"},{"hostPath":"/data/mysql/conf","containerPath":"/etc/mysql/conf.d"},{"hostPath":"/data/mysql/init","containerPath":"/docker-entrypoint-initdb.d"}]}}',
    '{"TZ":"Asia/Shanghai","MYSQL_ROOT_PASSWORD":"root123456"}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE software_code = 'mysql'
);

-- MySQL 5（兼容旧系统）
INSERT INTO sys_project_container_software_preset (
    software_code, service_name, software_name, software_category, preset_type, software_version,
    software_icon, os_type, expose_ports, has_port_mapping,
    install_script,
    preset_builtin, preset_delete, preset_status, preset_sort,
    preset_remark,
    docker_env_vars
)
SELECT
    'mysql5', 'mysql5', 'MySQL 5', 'database', 'SOFTWARE', '5.7',
    'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/mysql.svg',
    'linux', '3308:3306', 1,
    '',
    1, 0, 1, 2,
    '{"description":"MySQL 5.7 关系型数据库（兼容旧系统）","serviceName":"mysql5","supportedModes":["DOCKER"],"dockerSecurityOpts":["seccomp=unconfined"],"port":"3308:3306","dockerDefaults":{"image":"mysql:5.7","tags":["5.7"],"scriptMode":"script","env":{"MYSQL_ROOT_PASSWORD":"root123456","MYSQL_DATABASE":"myapp","TZ":"Asia/Shanghai","character_set_server":"utf8mb4","collation_server":"utf8mb4_unicode_ci","lower_case_table_names":"1"},"envDescriptions":{"MYSQL_ROOT_PASSWORD":"MySQL root 用户密码","MYSQL_DATABASE":"初始化时自动创建的数据库","TZ":"容器时区设置","character_set_server":"服务器字符集编码","collation_server":"服务器排序规则","lower_case_table_names":"表名大小写敏感（0=敏感，1=不敏感）"},"volumeMappings":[{"hostPath":"/data/mysql5/data","containerPath":"/var/lib/mysql"},{"hostPath":"/data/mysql5/conf","containerPath":"/etc/mysql/conf.d"},{"hostPath":"/data/mysql5/init","containerPath":"/docker-entrypoint-initdb.d"}]}}',
    '{"TZ":"Asia/Shanghai","MYSQL_ROOT_PASSWORD":"root123456"}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE software_code = 'mysql5'
);

-- MySQL 9（最新版）
INSERT INTO sys_project_container_software_preset (
    software_code, service_name, software_name, software_category, preset_type, software_version,
    software_icon, os_type, expose_ports, has_port_mapping,
    install_script,
    preset_builtin, preset_delete, preset_status, preset_sort,
    preset_remark,
    docker_env_vars
)
SELECT
    'mysql9', 'mysql9', 'MySQL 9', 'database', 'SOFTWARE', '9.0',
    'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/mysql.svg',
    'linux', '3309:3306', 1,
    '',
    1, 0, 1, 2,
    '{"description":"MySQL 9.0 关系型数据库（最新版）","serviceName":"mysql9","supportedModes":["DOCKER"],"dockerSecurityOpts":["seccomp=unconfined"],"port":"3309:3306","dockerDefaults":{"image":"mysql:9.0","tags":["9.0","9","latest"],"scriptMode":"script","env":{"MYSQL_ROOT_PASSWORD":"root123456","MYSQL_DATABASE":"myapp","MYSQL_USER":"appuser","MYSQL_PASSWORD":"apppass","TZ":"Asia/Shanghai","character_set_server":"utf8mb4","collation_server":"utf8mb4_unicode_ci","lower_case_table_names":"1","max_connections":"500"},"envDescriptions":{"MYSQL_ROOT_PASSWORD":"MySQL root 用户密码","MYSQL_DATABASE":"初始化时自动创建的数据库","MYSQL_USER":"初始化时创建的普通用户","MYSQL_PASSWORD":"普通用户的密码","TZ":"容器时区设置","character_set_server":"服务器字符集编码","collation_server":"服务器排序规则","lower_case_table_names":"表名大小写敏感（0=敏感，1=不敏感）","max_connections":"最大并发连接数"},"volumeMappings":[{"hostPath":"/data/mysql9/data","containerPath":"/var/lib/mysql"},{"hostPath":"/data/mysql9/conf","containerPath":"/etc/mysql/conf.d"},{"hostPath":"/data/mysql9/init","containerPath":"/docker-entrypoint-initdb.d"}]}}',
    '{"TZ":"Asia/Shanghai","MYSQL_ROOT_PASSWORD":"root123456"}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE software_code = 'mysql9'
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
    '{"description":"Apache ZooKeeper — 分布式协调服务","serviceName":"zookeeper","supportedModes":["DOCKER"],"dockerDefaults":{"image":"zookeeper","tags":["3.9","3.8","latest"],"scriptMode":"script","env":{"ZOO_MY_ID":"1","TZ":"Asia/Shanghai"},"volumeMappings":[{"hostPath":"/data/zookeeper/data","containerPath":"/data"},{"hostPath":"/data/zookeeper/logs","containerPath":"/datalog"}]}}',
    '{"TZ":"Asia/Shanghai","ZOO_MY_ID":"1"}'
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
    'FROM confluentinc/cp-kafka:latest\nEXPOSE 9092\nCMD ["/etc/confluent/docker/run"]',
    1, 0, 1, 9,
    '{"description":"Apache Kafka — 分布式消息队列系统（Dockerfile 构建，运行时需连接 ZooKeeper）","serviceName":"kafka","supportedModes":["DOCKER"],"dockerDefaults":{"image":"confluentinc/cp-kafka","tags":["latest","7.7","7.6"],"scriptMode":"dockerfile","dockerfileContent":"FROM confluentinc/cp-kafka:latest\\nEXPOSE 9092\\nCMD [\\"/etc/confluent/docker/run\\"]","env":{"KAFKA_BROKER_ID":"1","KAFKA_ZOOKEEPER_CONNECT":"zookeeper:2181","KAFKA_ADVERTISED_LISTENERS":"PLAINTEXT://localhost:9092","TZ":"Asia/Shanghai"},"volumeMappings":[{"hostPath":"/data/kafka/data","containerPath":"/var/lib/kafka/data"}]}}',
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
    'nacos', 'nacos', 'Nacos', 'middleware', 'DOCKERFILE', '3.2.1',
    'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/nacos.svg',
    'linux', '8848,9848', 1,
    'FROM nacos/nacos-server:latest\nUSER root\nRUN sed -i -E ''/UseConcMarkSweepGC|UseCMSCompactAtFullCollection|CMSInitiatingOccupancyFraction|CMSParallelRemarkEnabled|CMSClassUnloadingEnabled|SoftRefLRUPolicyMSPerMB|PrintGCDetails|PrintGCDateStamps|PrintGCTimeStamps|UseGCLogFileRotation|NumberOfGCLogFiles|GCLogFileSize|SurvivorRatio/d'' /home/nacos/bin/docker-startup.sh\nEXPOSE 8848 9848\nCMD ["bash", "/home/nacos/bin/docker-startup.sh", "-m", "standalone"]',
    1, 0, 1, 10,
    '{"description":"Nacos — 动态服务发现、配置管理和服务管理平台","serviceName":"nacos","dockerSecurityOpts":["seccomp=unconfined"],"supportedModes":["DOCKER"],"dockerDefaults":{"image":"nacos/nacos-server","tags":["v2.3","v2.2","latest"],"scriptMode":"dockerfile","dockerfileContent":"FROM nacos/nacos-server:latest\\nUSER root\\nRUN sed -i -E ''/UseConcMarkSweepGC|UseCMSCompactAtFullCollection|CMSInitiatingOccupancyFraction|CMSParallelRemarkEnabled|CMSClassUnloadingEnabled|SoftRefLRUPolicyMSPerMB|PrintGCDetails|PrintGCDateStamps|PrintGCTimeStamps|UseGCLogFileRotation|NumberOfGCLogFiles|GCLogFileSize|SurvivorRatio/d'' /home/nacos/bin/docker-startup.sh\\nEXPOSE 8848 9848\\nCMD [\\"bash\\", \\"/home/nacos/bin/docker-startup.sh\\", \\"-m\\", \\"standalone\\"]","env":{"MODE":"standalone","TZ":"Asia/Shanghai","JAVA_OPT":"-Xms256m -Xmx512m","NACOS_AUTH_TOKEN":"SecretKey012345678901234567890123456789012345678901234567890123456789","NACOS_AUTH_IDENTITY_KEY":"serverIdentity","NACOS_AUTH_IDENTITY_VALUE":"security"},"volumeMappings":[{"hostPath":"/data/nacos/logs","containerPath":"/home/nacos/logs"}]}}',
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
    'linux', '9000:9000,9001:9001', 1,
    '',
    1, 0, 1, 11,
    '{"description":"MinIO — 高性能 S3 兼容对象存储","serviceName":"minio","supportedModes":["DOCKER"],"dockerDefaults":{"image":"minio/minio","tags":["latest"],"scriptMode":"script","command":"server /data --console-address=:9001","env":{"MINIO_ROOT_USER":"minioadmin","MINIO_ROOT_PASSWORD":"minioadmin","TZ":"Asia/Shanghai"},"volumeMappings":[{"hostPath":"/data/minio/data","containerPath":"/data"}]}}',
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
    '{"description":"Elasticsearch — 分布式搜索和分析引擎","serviceName":"elasticsearch","supportedModes":["DOCKER"],"dockerDefaults":{"image":"elasticsearch:8.14.0","tags":["8","8.14","8.13","7","latest"],"scriptMode":"script","env":{"discovery.type":"single-node","ES_JAVA_OPTS":"-Xms512m -Xmx512m","TZ":"Asia/Shanghai"},"volumeMappings":[{"hostPath":"/data/elasticsearch/data","containerPath":"/usr/share/elasticsearch/data"}]}}',
    '{"TZ":"Asia/Shanghai","discovery.type":"single-node","ES_JAVA_OPTS":"-Xms512m -Xmx512m"}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE software_code = 'elasticsearch'
);

-- SkyWalking APM（DOCKERFILE 构建，需 MySQL 存储，内置 MySQL JDBC 驱动）
INSERT INTO sys_project_container_software_preset (
    software_code, service_name, software_name, software_category, preset_type, software_version,
    software_icon, os_type, expose_ports, has_port_mapping,
    install_script,
    preset_builtin, preset_delete, preset_status, preset_sort,
    preset_remark,
    docker_env_vars
)
SELECT
    'skywalking', 'skywalking', 'SkyWalking', 'monitoring', 'DOCKERFILE', '9.7.0',
    'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/apacheskywalking.svg',
    'linux', '11800,12800', 1,
    'FROM apache/skywalking-oap-server:9.7.0\nRUN wget -q -O /skywalking/ext-libs/mysql-connector-j-8.0.33.jar https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar\nEXPOSE 11800 12800\nCMD ["bash", "/skywalking/bin/oapService.sh"]',
    1, 0, 1, 14,
    '{"description":"Apache SkyWalking — 分布式 APM 系统（Dockerfile 构建，需连接 MySQL 作为存储后端）","serviceName":"skywalking","dockerSecurityOpts":["seccomp=unconfined"],"supportedModes":["DOCKER"],"dockerDefaults":{"image":"apache/skywalking-oap-server","tags":["9.7.0","latest"],"scriptMode":"dockerfile","dockerfileContent":"FROM apache/skywalking-oap-server:9.7.0\\nRUN wget -q -O /skywalking/ext-libs/mysql-connector-j-8.0.33.jar https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar\\nEXPOSE 11800 12800\\nCMD [\\"bash\\", \\"/skywalking/bin/oapService.sh\\"]","env":{"SW_STORAGE":"mysql","SW_JDBC_URL":"jdbc:mysql://172.16.0.40:3307/skywalking?useSSL=false&rewriteBatchedStatements=true&serverTimezone=Asia/Shanghai","SW_DATA_SOURCE_USER":"root","SW_DATA_SOURCE_PASSWORD":"root123456","TZ":"Asia/Shanghai"},"volumeMappings":[{"hostPath":"/data/skywalking/logs","containerPath":"/skywalking/logs"}],"envDescriptions":{"SW_STORAGE":"存储类型（mysql/elasticsearch）","SW_JDBC_URL":"MySQL JDBC 连接地址","SW_DATA_SOURCE_USER":"MySQL 用户名","SW_DATA_SOURCE_PASSWORD":"MySQL 密码","TZ":"时区"}}}',
    '{"TZ":"Asia/Shanghai","SW_STORAGE":"mysql"}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE software_code = 'skywalking'
);

-- 更新已有 SkyWalking 预设（兼容已初始化的数据库）
UPDATE sys_project_container_software_preset
SET install_script = 'FROM apache/skywalking-oap-server:9.7.0\nRUN wget -q -O /skywalking/ext-libs/mysql-connector-j-8.0.33.jar https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar\nEXPOSE 11800 12800\nCMD ["bash", "/skywalking/bin/oapService.sh"]',
    preset_remark = '{"description":"Apache SkyWalking — 分布式 APM 系统（Dockerfile 构建，需连接 MySQL 作为存储后端）","serviceName":"skywalking","dockerSecurityOpts":["seccomp=unconfined"],"supportedModes":["DOCKER"],"dockerDefaults":{"image":"apache/skywalking-oap-server","tags":["9.7.0","latest"],"scriptMode":"dockerfile","dockerfileContent":"FROM apache/skywalking-oap-server:9.7.0\\nRUN wget -q -O /skywalking/ext-libs/mysql-connector-j-8.0.33.jar https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar\\nEXPOSE 11800 12800\\nCMD [\\"bash\\", \\"/skywalking/bin/oapService.sh\\"]","env":{"SW_STORAGE":"mysql","SW_JDBC_URL":"jdbc:mysql://172.16.0.40:3307/skywalking?useSSL=false&rewriteBatchedStatements=true&serverTimezone=Asia/Shanghai","SW_DATA_SOURCE_USER":"root","SW_DATA_SOURCE_PASSWORD":"root123456","TZ":"Asia/Shanghai"},"volumeMappings":[{"hostPath":"/data/skywalking/logs","containerPath":"/skywalking/logs"}],"envDescriptions":{"SW_STORAGE":"存储类型（mysql/elasticsearch）","SW_JDBC_URL":"MySQL JDBC 连接地址","SW_DATA_SOURCE_USER":"MySQL 用户名","SW_DATA_SOURCE_PASSWORD":"MySQL 密码","TZ":"时区"}}}'
WHERE software_code = 'skywalking';

-- ============================================================
-- 新增常用软件预设（Docker 模式）
-- ============================================================

-- Grafana 监控可视化
INSERT INTO sys_project_container_software_preset (
    software_code, service_name, software_name, software_category, preset_type, software_version,
    software_icon, os_type, expose_ports, has_port_mapping,
    install_script,
    preset_builtin, preset_delete, preset_status, preset_sort,
    preset_remark,
    docker_env_vars
)
SELECT
    'grafana', 'grafana', 'Grafana', 'monitoring', 'SOFTWARE', '11',
    'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/grafana.svg',
    'linux', '3000', 1,
    '',
    1, 0, 1, 15,
    '{"description":"Grafana — 监控分析及可视化平台","serviceName":"grafana","supportedModes":["DOCKER"],"dockerDefaults":{"image":"grafana/grafana","tags":["latest","11.0","10.4","10.3"],"scriptMode":"script","env":{"GF_SECURITY_ADMIN_USER":"admin","GF_SECURITY_ADMIN_PASSWORD":"admin123","TZ":"Asia/Shanghai"},"envDescriptions":{"GF_SECURITY_ADMIN_USER":"管理员用户名","GF_SECURITY_ADMIN_PASSWORD":"管理员密码","TZ":"容器时区设置"},"volumeMappings":[{"hostPath":"/data/grafana/data","containerPath":"/var/lib/grafana"},{"hostPath":"/data/grafana/plugins","containerPath":"/var/lib/grafana/plugins"},{"hostPath":"/data/grafana/config","containerPath":"/etc/grafana"}]}}',
    '{"TZ":"Asia/Shanghai","GF_SECURITY_ADMIN_USER":"admin","GF_SECURITY_ADMIN_PASSWORD":"admin123"}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE software_code = 'grafana'
);

-- Prometheus 监控系统
INSERT INTO sys_project_container_software_preset (
    software_code, service_name, software_name, software_category, preset_type, software_version,
    software_icon, os_type, expose_ports, has_port_mapping,
    install_script,
    preset_builtin, preset_delete, preset_status, preset_sort,
    preset_remark,
    docker_env_vars
)
SELECT
    'prometheus', 'prometheus', 'Prometheus', 'monitoring', 'SOFTWARE', 'v2',
    'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/prometheus.svg',
    'linux', '9090', 1,
    '',
    1, 0, 1, 16,
    '{"description":"Prometheus — 开源系统监控和警报工具","serviceName":"prometheus","supportedModes":["DOCKER"],"dockerDefaults":{"image":"prom/prometheus","tags":["latest","v2.53","v2.52","v2.51"],"scriptMode":"script","env":{"TZ":"Asia/Shanghai"},"volumeMappings":[{"hostPath":"/data/prometheus/data","containerPath":"/prometheus"},{"hostPath":"/data/prometheus/conf","containerPath":"/etc/prometheus"}]}}',
    '{"TZ":"Asia/Shanghai"}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE software_code = 'prometheus'
);

-- Apache Tomcat
INSERT INTO sys_project_container_software_preset (
    software_code, service_name, software_name, software_category, preset_type, software_version,
    software_icon, os_type, expose_ports, has_port_mapping,
    install_script,
    preset_builtin, preset_delete, preset_status, preset_sort,
    preset_remark,
    docker_env_vars
)
SELECT
    'tomcat', 'tomcat', 'Tomcat', 'webserver', 'SOFTWARE', '11',
    'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/apachetomcat.svg',
    'linux', '8080', 1,
    '',
    1, 0, 1, 17,
    '{"description":"Apache Tomcat — Java Servlet/JSP 容器","serviceName":"tomcat","supportedModes":["DOCKER"],"dockerDefaults":{"image":"tomcat","tags":["11","10","9","latest"],"scriptMode":"script","env":{"TZ":"Asia/Shanghai"},"volumeMappings":[{"hostPath":"/data/tomcat/webapps","containerPath":"/usr/local/tomcat/webapps"},{"hostPath":"/data/tomcat/logs","containerPath":"/usr/local/tomcat/logs"}]}}',
    '{"TZ":"Asia/Shanghai"}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE software_code = 'tomcat'
);

-- Jenkins CI/CD
INSERT INTO sys_project_container_software_preset (
    software_code, service_name, software_name, software_category, preset_type, software_version,
    software_icon, os_type, expose_ports, has_port_mapping,
    install_script,
    preset_builtin, preset_delete, preset_status, preset_sort,
    preset_remark,
    docker_env_vars
)
SELECT
    'jenkins', 'jenkins', 'Jenkins', 'devops', 'SOFTWARE', 'lts',
    'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/jenkins.svg',
    'linux', '8080,50000', 1,
    '',
    1, 0, 1, 18,
    '{"description":"Jenkins — 开源 CI/CD 自动化服务器","serviceName":"jenkins","supportedModes":["DOCKER"],"dockerSecurityOpts":["seccomp=unconfined"],"dockerDefaults":{"image":"jenkins/jenkins","tags":["lts","latest","2.462","2.452"],"scriptMode":"script","env":{"TZ":"Asia/Shanghai"},"volumeMappings":[{"hostPath":"/data/jenkins/home","containerPath":"/var/jenkins_home"}]}}',
    '{"TZ":"Asia/Shanghai"}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE software_code = 'jenkins'
);

-- WordPress CMS
INSERT INTO sys_project_container_software_preset (
    software_code, service_name, software_name, software_category, preset_type, software_version,
    software_icon, os_type, expose_ports, has_port_mapping,
    install_script,
    preset_builtin, preset_delete, preset_status, preset_sort,
    preset_remark,
    docker_env_vars
)
SELECT
    'wordpress', 'wordpress', 'WordPress', 'cms', 'SOFTWARE', '6',
    'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/wordpress.svg',
    'linux', '8080', 1,
    '',
    1, 0, 1, 19,
    '{"description":"WordPress — 开源内容管理系统（CMS）","serviceName":"wordpress","supportedModes":["DOCKER"],"dockerDefaults":{"image":"wordpress","tags":["latest","6.5","6.4","6.3"],"scriptMode":"script","env":{"WORDPRESS_DB_HOST":"mysql:3306","WORDPRESS_DB_USER":"wordpress","WORDPRESS_DB_PASSWORD":"wordpress","WORDPRESS_DB_NAME":"wordpress","TZ":"Asia/Shanghai"},"envDescriptions":{"WORDPRESS_DB_HOST":"MySQL 数据库地址","WORDPRESS_DB_USER":"数据库用户名","WORDPRESS_DB_PASSWORD":"数据库密码","WORDPRESS_DB_NAME":"数据库名称","TZ":"时区"},"volumeMappings":[{"hostPath":"/data/wordpress/html","containerPath":"/var/www/html"}]}}',
    '{"TZ":"Asia/Shanghai"}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE software_code = 'wordpress'
);

-- phpMyAdmin
INSERT INTO sys_project_container_software_preset (
    software_code, service_name, software_name, software_category, preset_type, software_version,
    software_icon, os_type, expose_ports, has_port_mapping,
    install_script,
    preset_builtin, preset_delete, preset_status, preset_sort,
    preset_remark,
    docker_env_vars
)
SELECT
    'phpmyadmin', 'phpmyadmin', 'phpMyAdmin', 'database', 'SOFTWARE', 'latest',
    'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/phpmyadmin.svg',
    'linux', '8080', 1,
    '',
    1, 0, 1, 20,
    '{"description":"phpMyAdmin — 基于 Web 的 MySQL 数据库管理工具","serviceName":"phpmyadmin","supportedModes":["DOCKER"],"dockerDefaults":{"image":"phpmyadmin","tags":["latest","5.2"],"scriptMode":"script","env":{"PMA_HOST":"mysql","PMA_PORT":"3306","UPLOAD_LIMIT":"64M","TZ":"Asia/Shanghai"},"envDescriptions":{"PMA_HOST":"MySQL 数据库主机地址","PMA_PORT":"MySQL 数据库端口","UPLOAD_LIMIT":"上传文件大小限制","TZ":"时区"}}}',
    '{"TZ":"Asia/Shanghai","PMA_HOST":"mysql"}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE software_code = 'phpmyadmin'
);

-- Consul 服务发现
INSERT INTO sys_project_container_software_preset (
    software_code, service_name, software_name, software_category, preset_type, software_version,
    software_icon, os_type, expose_ports, has_port_mapping,
    install_script,
    preset_builtin, preset_delete, preset_status, preset_sort,
    preset_remark,
    docker_env_vars
)
SELECT
    'consul', 'consul', 'Consul', 'middleware', 'SOFTWARE', '1.18',
    'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/consul.svg',
    'linux', '8500', 1,
    '',
    1, 0, 1, 21,
    '{"description":"HashiCorp Consul — 服务发现和配置管理","serviceName":"consul","supportedModes":["DOCKER"],"dockerDefaults":{"image":"hashicorp/consul","tags":["latest","1.18","1.17","1.16"],"scriptMode":"script","command":"agent -dev -client=0.0.0.0","env":{"TZ":"Asia/Shanghai"},"volumeMappings":[{"hostPath":"/data/consul/data","containerPath":"/consul/data"}]}}',
    '{"TZ":"Asia/Shanghai"}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE software_code = 'consul'
);

-- EMQX MQTT 消息服务器
INSERT INTO sys_project_container_software_preset (
    software_code, service_name, software_name, software_category, preset_type, software_version,
    software_icon, os_type, expose_ports, has_port_mapping,
    install_script,
    preset_builtin, preset_delete, preset_status, preset_sort,
    preset_remark,
    docker_env_vars
)
SELECT
    'emqx', 'emqx', 'EMQX', 'middleware', 'SOFTWARE', '5',
    'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/emqx.svg',
    'linux', '1883,8083,8084,18083', 1,
    '',
    1, 0, 1, 22,
    '{"description":"EMQX — 大规模分布式 MQTT 消息服务器","serviceName":"emqx","supportedModes":["DOCKER"],"dockerDefaults":{"image":"emqx/emqx","tags":["latest","5.6","5.5","5.4"],"scriptMode":"script","env":{"TZ":"Asia/Shanghai"},"volumeMappings":[{"hostPath":"/data/emqx/data","containerPath":"/opt/emqx/data"},{"hostPath":"/data/emqx/log","containerPath":"/opt/emqx/log"}]}}',
    '{"TZ":"Asia/Shanghai"}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE software_code = 'emqx'
);

-- Portainer Docker 管理面板
INSERT INTO sys_project_container_software_preset (
    software_code, service_name, software_name, software_category, preset_type, software_version,
    software_icon, os_type, expose_ports, has_port_mapping,
    install_script,
    preset_builtin, preset_delete, preset_status, preset_sort,
    preset_remark,
    docker_env_vars
)
SELECT
    'portainer', 'portainer', 'Portainer', 'devops', 'SOFTWARE', '2',
    'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/docker.svg',
    'linux', '9000,9443', 1,
    '',
    1, 0, 1, 23,
    '{"description":"Portainer — 轻量级 Docker 容器管理面板","serviceName":"portainer","supportedModes":["DOCKER"],"dockerDefaults":{"image":"portainer/portainer-ce","tags":["latest","2.21","2.20","2.19"],"scriptMode":"script","command":"-H unix:///var/run/docker.sock","env":{"TZ":"Asia/Shanghai"},"volumeMappings":[{"hostPath":"/var/run/docker.sock","containerPath":"/var/run/docker.sock"},{"hostPath":"/data/portainer/data","containerPath":"/data"}]}}',
    '{"TZ":"Asia/Shanghai"}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE software_code = 'portainer'
);

-- Nginx Proxy Manager
INSERT INTO sys_project_container_software_preset (
    software_code, service_name, software_name, software_category, preset_type, software_version,
    software_icon, os_type, expose_ports, has_port_mapping,
    install_script,
    preset_builtin, preset_delete, preset_status, preset_sort,
    preset_remark,
    docker_env_vars
)
SELECT
    'nginx-proxy-manager', 'nginx-proxy-manager', 'Nginx Proxy Manager', 'webserver', 'SOFTWARE', '2',
    'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/nginx.svg',
    'linux', '80,81,443', 1,
    '',
    1, 0, 1, 24,
    '{"description":"Nginx Proxy Manager — Web 界面反向代理管理，支持 SSL 证书自动续签","serviceName":"nginx-proxy-manager","supportedModes":["DOCKER"],"dockerDefaults":{"image":"jc21/nginx-proxy-manager","tags":["latest","2.11","2.10","2.9"],"scriptMode":"script","env":{"TZ":"Asia/Shanghai"},"volumeMappings":[{"hostPath":"/data/nginx-proxy-manager/data","containerPath":"/data"},{"hostPath":"/data/nginx-proxy-manager/letsencrypt","containerPath":"/etc/letsencrypt"}]}}',
    '{"TZ":"Asia/Shanghai"}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE software_code = 'nginx-proxy-manager'
);

-- Netdata 实时监控
INSERT INTO sys_project_container_software_preset (
    software_code, service_name, software_name, software_category, preset_type, software_version,
    software_icon, os_type, expose_ports, has_port_mapping,
    install_script,
    preset_builtin, preset_delete, preset_status, preset_sort,
    preset_remark,
    docker_env_vars
)
SELECT
    'netdata', 'netdata', 'Netdata', 'monitoring', 'SOFTWARE', 'v2',
    'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/netdata.svg',
    'linux', '19999', 1,
    '',
    1, 0, 1, 25,
    '{"description":"Netdata — 实时系统性能监控平台，300+ 指标开箱即用","serviceName":"netdata","supportedModes":["DOCKER"],"dockerDefaults":{"image":"netdata/netdata","tags":["latest","v2.0","v1.46"],"scriptMode":"script","env":{"TZ":"Asia/Shanghai"},"volumeMappings":[{"hostPath":"/data/netdata/config","containerPath":"/etc/netdata"}]}}',
    '{"TZ":"Asia/Shanghai"}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE software_code = 'netdata'
);

-- Uptime Kuma 服务监控
INSERT INTO sys_project_container_software_preset (
    software_code, service_name, software_name, software_category, preset_type, software_version,
    software_icon, os_type, expose_ports, has_port_mapping,
    install_script,
    preset_builtin, preset_delete, preset_status, preset_sort,
    preset_remark,
    docker_env_vars
)
SELECT
    'uptime-kuma', 'uptime-kuma', 'Uptime Kuma', 'monitoring', 'SOFTWARE', '1',
    'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/uptimekuma.svg',
    'linux', '3001', 1,
    '',
    1, 0, 1, 26,
    '{"description":"Uptime Kuma — 服务可用性监控面板，支持 HTTP/TCP/Ping 等多种协议和告警通知","serviceName":"uptime-kuma","supportedModes":["DOCKER"],"dockerDefaults":{"image":"louislam/uptime-kuma","tags":["latest","1"],"scriptMode":"script","env":{"TZ":"Asia/Shanghai"},"volumeMappings":[{"hostPath":"/data/uptime-kuma/data","containerPath":"/app/data"}]}}',
    '{"TZ":"Asia/Shanghai"}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE software_code = 'uptime-kuma'
);

-- RocketMQ 消息队列
INSERT INTO sys_project_container_software_preset (
    software_code, service_name, software_name, software_category, preset_type, software_version,
    software_icon, os_type, expose_ports, has_port_mapping,
    install_script,
    preset_builtin, preset_delete, preset_status, preset_sort,
    preset_remark,
    docker_env_vars
)
SELECT
    'rocketmq', 'rocketmq', 'RocketMQ', 'middleware', 'SOFTWARE', '5.3',
    'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/apacherocketmq.svg',
    'linux', '9876,10911,10909', 1,
    '',
    1, 0, 1, 27,
    '{"description":"Apache RocketMQ — 分布式消息队列（阿里系），单容器同时启动 NameServer + Broker","serviceName":"rocketmq","supportedModes":["DOCKER"],"dockerDefaults":{"image":"apache/rocketmq","tags":["5.3.0","5.2.0","5.1.0","latest"],"scriptMode":"script","command":"sh -c \"mqnamesrv & sleep 3 && mqbroker -n localhost:9876 & wait\"","env":{"TZ":"Asia/Shanghai","JAVA_OPT_EXT":"-Xms512m -Xmx512m -Xmn256m"},"volumeMappings":[{"hostPath":"/data/rocketmq/logs","containerPath":"/home/rocketmq/logs"},{"hostPath":"/data/rocketmq/store","containerPath":"/home/rocketmq/store"}]}}',
    '{"TZ":"Asia/Shanghai"}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE software_code = 'rocketmq'
);

-- OnlyOffice 文档服务器
INSERT INTO sys_project_container_software_preset (
    software_code, service_name, software_name, software_category, preset_type, software_version,
    software_icon, os_type, expose_ports, has_port_mapping,
    install_script,
    preset_builtin, preset_delete, preset_status, preset_sort,
    preset_remark,
    docker_env_vars
)
SELECT
    'onlyoffice', 'onlyoffice', 'OnlyOffice', 'office', 'SOFTWARE', '8.1',
    'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/onlyoffice.svg',
    'linux', '8080:80', 1,
    '',
    1, 0, 1, 28,
    '{"description":"OnlyOffice Document Server — 在线文档编辑服务，支持 Word/Excel/PPT 协作编辑和预览","serviceName":"onlyoffice","supportedModes":["DOCKER"],"dockerDefaults":{"image":"onlyoffice/documentserver","tags":["latest","8.1","8.0","7.5"],"scriptMode":"script","env":{"TZ":"Asia/Shanghai"},"volumeMappings":[{"hostPath":"/data/onlyoffice/data","containerPath":"/var/www/onlyoffice/Data"},{"hostPath":"/data/onlyoffice/logs","containerPath":"/var/log/onlyoffice"},{"hostPath":"/data/onlyoffice/lib","containerPath":"/var/lib/onlyoffice"}]}}',
    '{"TZ":"Asia/Shanghai"}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE software_code = 'onlyoffice'
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

-- ============================================================
-- 为已有的 MySQL 预设补充 envDescriptions
-- ============================================================
UPDATE sys_project_container_software_preset
SET preset_remark = JSON_SET(
    preset_remark,
    '$.dockerDefaults.envDescriptions',
    JSON_OBJECT(
        'MYSQL_ROOT_PASSWORD', 'MySQL root 用户密码',
        'MYSQL_DATABASE', '初始化时自动创建的数据库',
        'MYSQL_USER', '初始化时创建的普通用户',
        'MYSQL_PASSWORD', '普通用户的密码',
        'TZ', '容器时区设置',
        'character_set_server', '服务器字符集编码',
        'collation_server', '服务器排序规则',
        'lower_case_table_names', '表名大小写敏感（0=敏感，1=不敏感）',
        'max_connections', '最大并发连接数'
    )
)
WHERE software_code = 'mysql'
  AND preset_remark IS NOT NULL
  AND NOT JSON_CONTAINS_PATH(preset_remark, 'one', '$.dockerDefaults.envDescriptions');

-- ============================================================
-- 清除其他模块遗留的失败迁移记录，避免启动时重试导致崩溃
-- ============================================================
UPDATE sys_database_version SET sys_database_version_success = 'true' WHERE sys_database_version_version = '1.7';

-- ============================================================
-- 镜像仓库种子数据（面板仓库管理）
-- ============================================================
INSERT INTO system_soft_registry (
    system_soft_registry_name, system_soft_registry_type, system_soft_registry_url,
    system_soft_registry_ssl_enabled, system_soft_registry_support_sync,
    system_soft_registry_timeout, system_soft_registry_sort,
    system_soft_registry_status, system_soft_registry_active,
    system_soft_registry_description,
    create_name, create_time
)
SELECT
    '阿里云容器镜像服务', 'aliyun', 'https://registry.cn-hangzhou.aliyuncs.com',
    1, 1, 30, 1,
    1, 1,
    '阿里云容器镜像服务（中国区），加速国内 Docker 镜像拉取',
    'panel-test', CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM system_soft_registry WHERE system_soft_registry_type = 'aliyun'
);

INSERT INTO system_soft_registry (
    system_soft_registry_name, system_soft_registry_type, system_soft_registry_url,
    system_soft_registry_ssl_enabled, system_soft_registry_support_sync,
    system_soft_registry_timeout, system_soft_registry_sort,
    system_soft_registry_status, system_soft_registry_active,
    system_soft_registry_description,
    create_name, create_time
)
SELECT
    'DaoCloud 镜像加速', 'docker_hub', 'https://docker.m.daocloud.io/',
    1, 0, 30, 2,
    1, 0,
    'DaoCloud Docker Hub 镜像代理（国内加速）',
    'panel-test', CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM system_soft_registry WHERE system_soft_registry_url LIKE '%daocloud%'
);

INSERT INTO system_soft_registry (
    system_soft_registry_name, system_soft_registry_type, system_soft_registry_url,
    system_soft_registry_ssl_enabled, system_soft_registry_support_sync,
    system_soft_registry_timeout, system_soft_registry_sort,
    system_soft_registry_status, system_soft_registry_active,
    system_soft_registry_description,
    create_name, create_time
)
SELECT
    '网易 163 镜像加速', 'docker_hub', 'https://hub-mirror.c.163.com/',
    1, 0, 30, 3,
    1, 0,
    '网易 Docker Hub 镜像代理（国内加速）',
    'panel-test', CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM system_soft_registry WHERE system_soft_registry_url LIKE '%163.com%'
);

INSERT INTO system_soft_registry (
    system_soft_registry_name, system_soft_registry_type, system_soft_registry_url,
    system_soft_registry_ssl_enabled, system_soft_registry_support_sync,
    system_soft_registry_timeout, system_soft_registry_sort,
    system_soft_registry_status, system_soft_registry_active,
    system_soft_registry_description,
    create_name, create_time
)
SELECT
    '百度镜像加速', 'docker_hub', 'https://mirror.baidubce.com/',
    1, 0, 30, 4,
    1, 0,
    '百度 Docker Hub 镜像代理（国内加速）',
    'panel-test', CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM system_soft_registry WHERE system_soft_registry_url LIKE '%baidubce%'
);

-- ============================================================
-- 修复调度器 job_group 列缺失（如果已存在则跳过）
-- ============================================================
SET @job_group_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_job' AND COLUMN_NAME = 'job_group');
SET @alter_sql = IF(@job_group_exists = 0,
    'ALTER TABLE sys_job ADD COLUMN job_group VARCHAR(255) NULL DEFAULT NULL COMMENT ''任务分组'' AFTER job_name',
    'SELECT 1');
PREPARE stmt FROM @alter_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================================
-- 新增常用软件预设（2026-05-15 补充）
-- ============================================================

-- Seata 分布式事务
INSERT INTO sys_project_container_software_preset (
    software_code, service_name, software_name, software_category, preset_type, software_version,
    software_icon, os_type, expose_ports, has_port_mapping,
    install_script,
    preset_builtin, preset_delete, preset_status, preset_sort,
    preset_remark,
    docker_env_vars
)
SELECT
    'seata', 'seata', 'Seata', 'middleware', 'SOFTWARE', '2.2',
    'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/seata.svg',
    'linux', '8091,7091', 1,
    '',
    1, 0, 1, 160,
    '{"description":"Seata — 阿里巴巴开源分布式事务解决方案，支持 AT/TCC/SAGA/XA 四种模式","serviceName":"seata","supportedModes":["DOCKER"],"dockerDefaults":{"image":"seataio/seata-server","tags":["2.2","2.1","2.0","latest"],"scriptMode":"script","env":{"SEATA_IP":"","SEATA_PORT":"8091","STORE_MODE":"file","TZ":"Asia/Shanghai"},"volumeMappings":[{"hostPath":"/data/seata/config","containerPath":"/root/seata-config"},{"hostPath":"/data/seata/logs","containerPath":"/home/seata/logs"}],"command":"sh -c \"seata-server.sh -h $SEATA_IP -p $SEATA_PORT -m $STORE_MODE\""}}',
    '{"TZ":"Asia/Shanghai","STORE_MODE":"file"}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE software_code = 'seata'
);

-- Sentinel 流量控制
INSERT INTO sys_project_container_software_preset (
    software_code, service_name, software_name, software_category, preset_type, software_version,
    software_icon, os_type, expose_ports, has_port_mapping,
    install_script,
    preset_builtin, preset_delete, preset_status, preset_sort,
    preset_remark,
    docker_env_vars
)
SELECT
    'sentinel', 'sentinel', 'Sentinel', 'middleware', 'SOFTWARE', '1.8',
    'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/sentinel.svg',
    'linux', '8858,8719', 1,
    '',
    1, 0, 1, 170,
    '{"description":"Sentinel — 阿里巴巴开源流量控制、熔断降级、系统负载保护中间件","serviceName":"sentinel","supportedModes":["DOCKER"],"dockerDefaults":{"image":"bladex/sentinel-dashboard","tags":["1.8.8","1.8.7","latest"],"scriptMode":"script","env":{"TZ":"Asia/Shanghai","JAVA_OPTS":"-Dserver.port=8858 -Dcsp.sentinel.api.port=8719 -Dproject.name=sentinel-dashboard"},"volumeMappings":[{"hostPath":"/data/sentinel/logs","containerPath":"/root/logs/csp"}],"command":"sh -c \\"java \\$JAVA_OPTS -jar /app/sentinel-dashboard.jar\\""}}',
    '{"TZ":"Asia/Shanghai"}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE software_code = 'sentinel'
);

-- SQL Server（MSSQL）
INSERT INTO sys_project_container_software_preset (
    software_code, service_name, software_name, software_category, preset_type, software_version,
    software_icon, os_type, expose_ports, has_port_mapping,
    install_script,
    preset_builtin, preset_delete, preset_status, preset_sort,
    preset_remark,
    docker_env_vars
)
SELECT
    'mssql', 'mssql', 'SQL Server', 'database', 'SOFTWARE', '2022',
    'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/microsoftsqlserver.svg',
    'linux', '1433', 1,
    '',
    1, 0, 1, 180,
    '{"description":"Microsoft SQL Server 2022 — 企业级关系型数据库（Linux 容器版）","serviceName":"mssql","supportedModes":["DOCKER"],"dockerDefaults":{"image":"mcr.microsoft.com/mssql/server","tags":["2022-latest","2022-CU14-ubuntu","2019-latest","latest"],"scriptMode":"script","env":{"ACCEPT_EULA":"Y","MSSQL_SA_PASSWORD":"SaPassword123!","MSSQL_PID":"developer","TZ":"Asia/Shanghai"},"volumeMappings":[{"hostPath":"/data/mssql/data","containerPath":"/var/opt/mssql"},{"hostPath":"/data/mssql/logs","containerPath":"/var/opt/mssql/log"},{"hostPath":"/data/mssql/secrets","containerPath":"/var/opt/mssql/secrets"}]}}',
    '{"ACCEPT_EULA":"Y","MSSQL_SA_PASSWORD":"SaPassword123!","MSSQL_PID":"developer","TZ":"Asia/Shanghai"}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE software_code = 'mssql'
);

-- Oracle XE（免费版）
INSERT INTO sys_project_container_software_preset (
    software_code, service_name, software_name, software_category, preset_type, software_version,
    software_icon, os_type, expose_ports, has_port_mapping,
    install_script,
    preset_builtin, preset_delete, preset_status, preset_sort,
    preset_remark,
    docker_env_vars
)
SELECT
    'oracle', 'oracle', 'Oracle XE', 'database', 'SOFTWARE', '21c',
    'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/oracle.svg',
    'linux', '1521,5500', 1,
    '',
    1, 0, 1, 190,
    '{"description":"Oracle Database Express Edition (XE) — 免费开发版关系型数据库，内置 JSON/Graph/SQL 多模型支持","serviceName":"oracle","supportedModes":["DOCKER"],"dockerDefaults":{"image":"gvenzl/oracle-xe","tags":["21-slim","21-full","18-slim","latest"],"scriptMode":"script","env":{"ORACLE_PASSWORD":"oracle","ORACLE_DATABASE":"XE","APP_USER":"app_user","APP_USER_PASSWORD":"app_pass","TZ":"Asia/Shanghai"},"volumeMappings":[{"hostPath":"/data/oracle/oradata","containerPath":"/opt/oracle/oradata"},{"hostPath":"/data/oracle/scripts","containerPath":"/opt/oracle/scripts/startup"}]}}',
    '{"ORACLE_PASSWORD":"oracle","ORACLE_DATABASE":"XE","TZ":"Asia/Shanghai"}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE software_code = 'oracle'
);

-- Neo4j 图数据库
INSERT INTO sys_project_container_software_preset (
    software_code, service_name, software_name, software_category, preset_type, software_version,
    software_icon, os_type, expose_ports, has_port_mapping,
    install_script,
    preset_builtin, preset_delete, preset_status, preset_sort,
    preset_remark,
    docker_env_vars
)
SELECT
    'neo4j', 'neo4j', 'Neo4j', 'database', 'SOFTWARE', '5-community',
    'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/neo4j.svg',
    'linux', '7474,7687', 1,
    '',
    1, 0, 1, 200,
    '{"description":"Neo4j — 业界领先的图数据库管理系统，原生图存储和处理，支持 Cypher 查询语言","serviceName":"neo4j","supportedModes":["DOCKER"],"dockerDefaults":{"image":"neo4j","tags":["5-community","5-enterprise","4-community","latest"],"scriptMode":"script","env":{"NEO4J_AUTH":"neo4j/neo4j123","NEO4J_PLUGINS":"[\"apoc\"]","NEO4J_dbms_memory_pagecache_size":"512m","NEO4J_dbms_memory_heap_max__size":"1G","TZ":"Asia/Shanghai"},"volumeMappings":[{"hostPath":"/data/neo4j/data","containerPath":"/data"},{"hostPath":"/data/neo4j/logs","containerPath":"/logs"},{"hostPath":"/data/neo4j/import","containerPath":"/var/lib/neo4j/import"},{"hostPath":"/data/neo4j/plugins","containerPath":"/plugins"}]}}',
    '{"NEO4J_AUTH":"neo4j/neo4j123","TZ":"Asia/Shanghai"}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE software_code = 'neo4j'
);

-- InfluxDB 时序数据库
INSERT INTO sys_project_container_software_preset (
    software_code, service_name, software_name, software_category, preset_type, software_version,
    software_icon, os_type, expose_ports, has_port_mapping,
    install_script,
    preset_builtin, preset_delete, preset_status, preset_sort,
    preset_remark,
    docker_env_vars
)
SELECT
    'influxdb', 'influxdb', 'InfluxDB', 'database', 'SOFTWARE', '2',
    'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/influxdb.svg',
    'linux', '8086', 1,
    '',
    1, 0, 1, 210,
    '{"description":"InfluxDB — 高性能时序数据库，专为指标、事件和实时分析设计，支持 SQL 和 Flux 查询","serviceName":"influxdb","supportedModes":["DOCKER"],"dockerDefaults":{"image":"influxdb","tags":["2","2.7","1.11","latest"],"scriptMode":"script","env":{"DOCKER_INFLUXDB_INIT_MODE":"setup","DOCKER_INFLUXDB_INIT_USERNAME":"admin","DOCKER_INFLUXDB_INIT_PASSWORD":"admin123456","DOCKER_INFLUXDB_INIT_ORG":"myorg","DOCKER_INFLUXDB_INIT_BUCKET":"default","TZ":"Asia/Shanghai"},"volumeMappings":[{"hostPath":"/data/influxdb/data","containerPath":"/var/lib/influxdb2"},{"hostPath":"/data/influxdb/config","containerPath":"/etc/influxdb2"}]}}',
    '{"DOCKER_INFLUXDB_INIT_MODE":"setup","DOCKER_INFLUXDB_INIT_USERNAME":"admin","DOCKER_INFLUXDB_INIT_PASSWORD":"admin123456","DOCKER_INFLUXDB_INIT_ORG":"myorg","DOCKER_INFLUXDB_INIT_BUCKET":"default","TZ":"Asia/Shanghai"}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE software_code = 'influxdb'
);

-- TDengine 时序数据库
INSERT INTO sys_project_container_software_preset (
    software_code, service_name, software_name, software_category, preset_type, software_version,
    software_icon, os_type, expose_ports, has_port_mapping,
    install_script,
    preset_builtin, preset_delete, preset_status, preset_sort,
    preset_remark,
    docker_env_vars
)
SELECT
    'tdengine', 'tdengine', 'TDengine', 'database', 'SOFTWARE', '3.3',
    'https://cdn.jsdelivr.net/npm/simple-icons@latest/icons/tdengine.svg',
    'linux', '6030,6041,6043,6044', 1,
    '',
    1, 0, 1, 220,
    '{"description":"TDengine — 高性能国产时序数据库，专为物联网/车联网/工业互联网设计，支持 SQL 查询","serviceName":"tdengine","supportedModes":["DOCKER"],"dockerDefaults":{"image":"tdengine/tdengine","tags":["3.3","3.2","3.1","latest"],"scriptMode":"script","env":{"TAOS_MULTIPLE_CPU":"0","TZ":"Asia/Shanghai"},"volumeMappings":[{"hostPath":"/data/tdengine/data","containerPath":"/var/lib/taos"},{"hostPath":"/data/tdengine/logs","containerPath":"/var/log/taos"}]}}',
    '{"TZ":"Asia/Shanghai"}'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_container_software_preset WHERE software_code = 'tdengine'
);
