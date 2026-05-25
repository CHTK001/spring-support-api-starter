-- Check current state
SELECT COUNT(*) AS cnt FROM sys_ai_global_setting WHERE sys_ai_global_setting_group = 'runtime';

-- Delete old runtime settings
DELETE FROM sys_ai_global_setting WHERE sys_ai_global_setting_group = 'runtime';

-- Insert correct runtime settings
INSERT INTO sys_ai_global_setting (sys_ai_global_setting_group, sys_ai_global_setting_field, sys_ai_global_setting_value, sys_ai_global_setting_value_type, sys_ai_global_setting_description, sys_ai_global_setting_sort, sys_ai_global_setting_status) VALUES
('runtime', 'provider', 'zhipu', 'STRING', 'LLM提供商', 1, 1),
('runtime', 'factory', 'default', 'STRING', 'ChatClient工厂', 2, 1),
('runtime', 'provider_base_url', 'https://open.bigmodel.cn/api/paas/v4/', 'STRING', 'API基础URL', 3, 1),
('runtime', 'provider_api_key', 'acb7372b94c3428bb75c03f695fcb2ee.p82pPKkxRovK8dqz', 'STRING', 'API密钥', 4, 1),
('runtime', 'default_model', 'glm-4-flash', 'STRING', '默认模型', 5, 1);

-- Verify
SELECT sys_ai_global_setting_field, sys_ai_global_setting_value FROM sys_ai_global_setting WHERE sys_ai_global_setting_group = 'runtime';
