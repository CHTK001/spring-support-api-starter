INSERT INTO sys_ai_module (sys_ai_module_name, sys_ai_module_code, sys_ai_module_type, sys_ai_module_vendor, sys_ai_module_status, sys_ai_module_sort, sys_ai_module_vlm, sys_ai_module_support_think, sys_ai_module_concurrency, sys_ai_module_input_price, sys_ai_module_output_price, sys_ai_module_remark)
SELECT * FROM (
  SELECT 'KAT-Coder-Exp-72B-1010' AS col1, 'KAT-Coder-Exp-72B-1010' AS col2, 'CHAT' AS col3, 'baishan' AS col4, 1 AS col5, 10 AS col6, 0 AS col7, 0 AS col8, 1 AS col9, 0.0000 AS col10, 0.0000 AS col11, '白山智算' AS col12 UNION ALL
  SELECT 'DeepSeek-R1-0528-Qwen3-8B', 'DeepSeek-R1-0528-Qwen3-8B', 'CHAT', 'baishan', 1, 20, 0, 1, 1, 0.0000, 0.0000, '白山智算' UNION ALL
  SELECT 'DeepSeek-V4-Pro', 'deepseek-v4-pro', 'CHAT', 'baishan', 1, 30, 0, 0, 1, 0.0000, 0.0000, 'Infer AI' UNION ALL
  SELECT 'DeepSeek-V4-Flash-L', 'deepseek-v4-flash-l', 'CHAT', 'sixthsense', 1, 201, 0, 0, 5, 0.0000, 0.0000, 'Sixthsense LLM' UNION ALL
  SELECT 'GLM-4.7', 'GLM-4.7', 'CHAT', 'zai', 1, 186, 0, 0, 1, 0.6000, 2.2000, 'Z.AI' UNION ALL
  SELECT 'GLM-4.7-Flash', 'GLM-4.7-Flash', 'CHAT', 'zai', 1, 187, 0, 0, 1, 0.0000, 0.0000, 'Z.AI' UNION ALL
  SELECT 'GLM-4V-Flash', 'GLM-4V-Flash', 'CHAT', 'zai', 1, 190, 1, 0, 10, 0.0000, 0.0000, 'Z.AI' UNION ALL
  SELECT 'GLM-5.1', 'GLM-5.1', 'CHAT', 'zai', 1, 191, 0, 1, 10, 2.0000, 8.0000, 'Z.AI'
) seed
WHERE NOT EXISTS (SELECT 1 FROM sys_ai_module WHERE sys_ai_module_code = seed.col2);
