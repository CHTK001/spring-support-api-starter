-- 迁移文件管理模式字段从数字格式到字符串格式
-- 这个脚本将现有的数字值转换为对应的字符串代码

-- 备份当前数据（可选）
-- CREATE TABLE monitor_sys_gen_server_setting_backup AS SELECT * FROM monitor_sys_gen_server_setting;

-- 更新文件管理模式字段
UPDATE monitor_sys_gen_server_setting
SET monitor_sys_gen_server_setting_file_management_mode =
        CASE
            WHEN monitor_sys_gen_server_setting_file_management_mode = '0' THEN 'NONE'
            WHEN monitor_sys_gen_server_setting_file_management_mode = '1' THEN 'SSH'
            WHEN monitor_sys_gen_server_setting_file_management_mode = '2' THEN 'API'
            WHEN monitor_sys_gen_server_setting_file_management_mode = '3' THEN 'NODE'
            WHEN monitor_sys_gen_server_setting_file_management_mode = '4' THEN 'LOCAL'
            ELSE 'NONE'
            END
WHERE monitor_sys_gen_server_setting_file_management_mode IN ('0', '1', '2', '3', '4')
   OR monitor_sys_gen_server_setting_file_management_mode REGEXP '^[0-9]+$';

-- 确保所有 NULL 值设置为默认值
UPDATE monitor_sys_gen_server_setting
SET monitor_sys_gen_server_setting_file_management_mode = 'NONE'
WHERE monitor_sys_gen_server_setting_file_management_mode IS NULL;

-- 验证迁移结果
-- SELECT 
--     monitor_sys_gen_server_setting_file_management_mode,
--     COUNT(*) as count
-- FROM monitor_sys_gen_server_setting 
-- GROUP BY monitor_sys_gen_server_setting_file_management_mode
-- ORDER BY monitor_sys_gen_server_setting_file_management_mode;
