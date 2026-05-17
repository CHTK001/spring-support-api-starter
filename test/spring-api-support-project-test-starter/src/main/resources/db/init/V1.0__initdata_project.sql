-- =====================================================
-- 项目管理测试数据
-- 版本: V1.0
-- =====================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 测试项目 (sys_project)
-- =====================================================
INSERT INTO `sys_project` (`sys_project_id`, `sys_project_name`, `sys_project_group`, `sys_project_code`, `sys_project_app_id`, `sys_project_app_secret`, `sys_project_delete`, `sys_project_status`, `sys_project_sort`, `sys_project_endpoint`, `sys_project_remark`, `sys_project_function`, `create_name`, `create_by`, `create_time`)
VALUES
(1, '测试项目-短信服务', '消息服务', 'test-sms', 'test-app-id', 'test-app-secret', 0, 1, 1, 'local://localhost', '短信服务测试项目', '1,2,3', 'sa', 1, NOW()),
(2, '测试项目-邮件服务', '消息服务', 'test-email', 'test-email-id', 'test-email-secret', 0, 1, 2, NULL, '邮件服务测试项目', '4,5', 'sa', 1, NOW()),
(3, '本地远程部署测试', '远程部署', 'test-deploy', NULL, NULL, 0, 1, 3, 'local://localhost?osType=linux', '本地远程部署功能测试', '6', 'sa', 1, NOW()),
(4, '阿里云短信', '云服务', 'ali-sms', 'LTAI5tKQQnvCS3yEjGxjHmCd', 'Pm45AutVaPLqWCOUNeZ7gEQBTYJQAh', 0, 1, 4, NULL, '阿里云短信服务', '14', 'sa', 1, NOW());

-- =====================================================
-- 测试默认项目 (sys_project_default)
-- =====================================================
INSERT INTO `sys_project_default` (`sys_project_default_id`, `sys_project_default_type`, `sys_project_id`)
VALUES
(1, '1', 1),
(2, '2', 1),
(3, '4', 2),
(4, '5', 2),
(5, '6', 3),
(6, '14', 4);

-- =====================================================
-- 测试密钥 (sys_secret)
-- =====================================================
INSERT INTO `sys_secret` (`sys_secret_id`, `sys_secret_group`, `sys_secret_code`, `sys_secret_app_id`, `sys_secret_app_secret`, `sys_secret_sign`, `sys_secret_delete`, `sys_secret_status`, `sys_secret_function`, `create_name`, `create_by`, `create_time`)
VALUES
(1, '阿里系', 'ALI', 'LTAI5tKQQnvCS3yEjGxjHmCd', 'Pm45AutVaPLqWCOUNeZ7gEQBTYJQAh', '测试签名', 0, 1, 'SMS', 'sa', 1, NOW()),
(2, '腾讯系', 'TXY', 'tx-app-id', 'tx-app-secret', '腾讯签名', 0, 1, 'SMS', 'sa', 1, NOW()),
(3, '邮件服务', 'EMAIL', 'email-app-id', 'email-app-secret', NULL, 0, 1, 'EMAIL', 'sa', 1, NOW());

-- =====================================================
-- 测试短信模板 (sys_sms_template)
-- =====================================================
INSERT INTO `sys_sms_template` (`sys_sms_template_id`, `sys_project_id`, `sys_sms_template_name`, `sys_sms_template_code`, `sys_sms_template_content`, `sys_sms_template_delete`, `sys_sms_template_status`, `create_name`, `create_by`, `create_time`)
VALUES
(1, 1, '验证码模板', 'SMS_001', '您的验证码是${code}，有效期5分钟', 0, 1, 'sa', 1, NOW()),
(2, 1, '通知模板', 'SMS_002', '您好，${message}', 0, 1, 'sa', 1, NOW());

-- =====================================================
-- 测试邮件模板 (sys_email_template)
-- =====================================================
INSERT INTO `sys_email_template` (`sys_email_template_id`, `sys_project_id`, `sys_email_template_name`, `sys_email_template_code`, `sys_email_template_content`, `sys_email_template_delete`, `sys_email_template_status`, `create_name`, `create_by`, `create_time`)
VALUES
(1, 2, '测试邮件模板', 'EMAIL_001', '<p><strong>测试邮件内容</strong></p>', 0, 1, 'sa', 1, NOW()),
(2, 2, '通知模板', 'EMAIL_002', '<p>您好，${content}</p>', 0, 1, 'sa', 1, NOW());

SET FOREIGN_KEY_CHECKS = 1;
