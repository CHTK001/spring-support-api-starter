ALTER TABLE sys_ai_module
    ADD COLUMN IF NOT EXISTS `sys_ai_module_mapping_name` VARCHAR(128) NULL DEFAULT NULL
    COMMENT 'OpenAI 兼容映射名称，如将 deepseek-chat 映射为 gpt-3.5-turbo'
    AFTER `sys_ai_module_api_expose`;
