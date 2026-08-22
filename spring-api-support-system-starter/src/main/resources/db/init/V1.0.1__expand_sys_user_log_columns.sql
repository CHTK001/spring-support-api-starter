-- =====================================================
-- sys_user_log 表字段扩容
-- 用途：将 varchar(255) 字段升级为 TEXT，解决登录参数/响应消息/地址/URL 超长被截断导致的"乱码"问题
-- 执行：Flyway 自动应用（V1.0 之后）
-- =====================================================

-- sys_log_param: 登录请求参数（含验证码/账号/tenantId 等 JSON，>255 字符很常见）
ALTER TABLE `sys_user_log` MODIFY COLUMN `sys_log_param` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '请求参数 - 登录请求的参数(脱敏)';

-- sys_log_message: 响应消息（异常堆栈/JSON 响应体可能 >255 字符）
ALTER TABLE `sys_user_log` MODIFY COLUMN `sys_log_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '响应消息 - 登录/登出结果描述';

-- sys_log_address: 地理位置（IP 解析可能含省市+运营商拼接）
ALTER TABLE `sys_user_log` MODIFY COLUMN `sys_log_address` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '登录地点 - 根据IP解析的地理位置';

-- sys_log_url: 请求 URL（带 query string 可能 >255）
ALTER TABLE `sys_user_log` MODIFY COLUMN `sys_log_url` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '请求地址 - 登录的API地址';
