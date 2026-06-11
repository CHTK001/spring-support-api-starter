-- ============================================================
-- V1.2__cleanup_ai_seed.sql
-- 清理测试/脏数据，保留正常生产种子
-- ============================================================

DELETE FROM `sys_ai_module`
WHERE `sys_ai_module_code` IN ('deepseek-v4-pro', 'claude-test')
   OR `sys_ai_module_vendor` IN ('infer-ai')
   OR `sys_ai_module_remark` LIKE '%Infer AI%';

DELETE FROM `sys_ai_manufacturer`
WHERE `sys_ai_manufacturer_code` IN ('infer-ai', 'claude-test')
   OR `sys_ai_manufacturer_name` LIKE '%Infer AI%';

UPDATE `sys_project`
SET
  `sys_project_name` = 'AI项目',
  `sys_project_code` = 'ai-project',
  `sys_project_status` = 1,
  `sys_project_delete` = 0,
  `sys_project_endpoint` = 'https://api.openai.com/v1/chat/completions',
  `sys_project_vender` = 'openai',
  `create_name` = 'system',
  `update_name` = 'system'
WHERE `sys_project_id` = 6;

INSERT INTO `sys_project` (
  `sys_project_id`,
  `sys_project_name`,
  `sys_project_code`,
  `sys_project_status`,
  `sys_project_delete`,
  `sys_project_endpoint`,
  `sys_project_vender`,
  `create_name`,
  `create_time`,
  `update_name`,
  `update_time`
)
SELECT
  6,
  'AI项目',
  'ai-project',
  1,
  0,
  'https://api.openai.com/v1/chat/completions',
  'openai',
  'system',
  CURRENT_TIMESTAMP,
  'system',
  CURRENT_TIMESTAMP
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_project` WHERE `sys_project_id` = 6
);
