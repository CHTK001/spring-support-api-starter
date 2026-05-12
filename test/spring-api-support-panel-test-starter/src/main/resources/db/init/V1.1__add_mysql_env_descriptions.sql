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
