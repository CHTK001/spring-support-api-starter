-- =====================================================
-- Project 模块初始化数据脚本
-- 版本: V1.0
-- 说明: 包含邮件日志/模板、项目配置、默认功能、密钥等基础数据
-- 执行方式: 异步执行（虚拟线程）
-- =====================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 初始数据 - 邮件日志表 (sys_email_log)
-- =====================================================
INSERT INTO `sys_email_log` VALUES (6, 'yemengzhidiandi@163.com', '1', '{}', '<p><strong>测试</strong></p>', 2392.00, '00000', NULL, 'NEI_BU_XI_TONG', NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_email_log` VALUES (7, 'yemengzhidiandi@163.com', '1', '{}', '<p><strong>测试</strong></p>', 1348.00, 'SUCCESS', NULL, 'NEI_BU_XI_TONG', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, 2);
INSERT INTO `sys_email_log` VALUES (8, 'yemengzhidiandi@163.com', '1', NULL, '<p><strong>测试</strong></p>', 893.00, 'SUCCESS', NULL, 'NEI_BU_XI_TONG', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, 2);
INSERT INTO `sys_email_log` VALUES (9, 'request.getParams()', '1', NULL, '<p><strong>测试</strong></p>', 40.00, 'FAILURE', NULL, 'NEI_BU_XI_TONG', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, 2);
INSERT INTO `sys_email_log` VALUES (10, 'yemengzhidiandi@163.com', '1', NULL, '<p><strong>测试</strong></p>', 917.00, 'SUCCESS', NULL, 'NEI_BU_XI_TONG', NULL, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, 2);

-- =====================================================
-- 初始数据 - 邮件模板 (sys_email_template)
-- =====================================================
INSERT INTO `sys_email_template` VALUES (1, 2, NULL, 1, 4, '测试', '111', '<p><strong>测试</strong></p>', NULL, 1, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 'http://127.0.0.1:18170/system/api/v2/project/template/email/callback', 'sa', 1, '2025-01-27 20:00:31', '2025-01-27 20:04:46', 'sa', 1);

-- =====================================================
-- 初始数据 - 默认功能映射 (sys_project_default)
-- =====================================================
INSERT INTO `sys_project_default` VALUES (30, '6', 3);
INSERT INTO `sys_project_default` VALUES (33, '16', 2);
INSERT INTO `sys_project_default` VALUES (37, '14', 4);
INSERT INTO `sys_project_default` VALUES (46, '13', 1);
INSERT INTO `sys_project_default` VALUES (47, '20', 16);

-- =====================================================
-- 初始数据 - 密钥 (sys_secret)
-- =====================================================
INSERT INTO `sys_secret` VALUES (1, '阿里系', 'ALI', 'LTAI5tKQQnvCS3yEjGxjHmCd', 'Pm45AutVaPLqWCOUNeZ7gEQBTYJQAh', '犀鸟校园', 0, 1, NULL, NULL, NULL, 2, NULL, 'SMS', 'sa', 1, '2024-09-06 13:32:21', '2024-09-06 13:36:12', 'sa', 1);

SET FOREIGN_KEY_CHECKS = 1;

