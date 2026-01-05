/*
 * OAuth认证模块 - 数据库初始化脚本
 * 
 * 包含表:
 *   - app_key: AppKey应用密钥表
 *   - app_id: AppId应用标识表
 *   - sys_app_key: OAuth应用密钥表(AK/SK) - 兼容旧版本
 *   - sys_token_blacklist: Token黑名单表
 *
 * 注意: 此脚本只包含OAuth专用表，不包含基础系统表（sys_user, sys_role等）
 * 基础系统表请先执行 spring-api-support-system-starter 模块的初始化脚本
 *
 * @author System
 * @since 2025-01-01
 * @version 1.0
 */

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 1. AppKey应用密钥表 (app_key)
-- 用途: 存储AppKey相关信息，用于API访问认证
-- =====================================================
DROP TABLE IF EXISTS `app_key`;
CREATE TABLE `app_key` (
    `app_key_id` int NOT NULL AUTO_INCREMENT COMMENT 'AppKey ID - 主键自增',
    `app_key_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'AppKey值 - 应用访问密钥，类似用户名，全局唯一',
    `app_key_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'AppKey名称 - 应用的显示名称',
    `app_key_secret` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'AppSecret - 应用访问密钥，类似密码（需加密存储）',
    `app_key_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '应用描述 - 应用的详细说明',
    `app_key_ip_whitelist` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'IP白名单 - 允许访问的IP地址列表，逗号分隔，如"192.168.1.1,10.0.0.1"',
    `app_key_status` int NULL DEFAULT 1 COMMENT '应用状态 - 0:禁用, 1:启用',
    `app_key_expire_time` bigint NULL DEFAULT NULL COMMENT '过期时间 - Unix时间戳(毫秒)，NULL表示永不过期',
    `app_key_last_use_time` bigint NULL DEFAULT NULL COMMENT '最后使用时间 - Unix时间戳(毫秒)，最近一次使用该密钥的时间',
    `app_key_use_count` bigint NULL DEFAULT 0 COMMENT '使用次数 - 该密钥累计被调用的次数',
    `user_id` int NULL DEFAULT NULL COMMENT '关联用户ID - 该AppKey所属的用户ID',
    `app_key_scope` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '允许的权限范围 - 多个scope用逗号分隔，如"read,write,admin"',
    `app_key_grant_types` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '允许的授权类型 - 多个用逗号分隔，如"client_credentials,password,authorization_code,refresh_token"',
    `app_key_redirect_uris` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '允许的回调地址 - 多个用逗号分隔，用于authorization_code模式的redirect_uri校验',
    `app_key_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注信息',
    `delete_flag` int NULL DEFAULT 0 COMMENT '删除标记 - 0:未删除, 1:已删除',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`app_key_id`) USING BTREE,
    UNIQUE INDEX `uk_app_key_value` (`app_key_value`) USING BTREE COMMENT 'AppKey值唯一索引 - 确保AppKey不重复',
    INDEX `idx_status` (`app_key_status`) USING BTREE COMMENT '状态索引 - 用于快速查询启用的应用',
    INDEX `idx_expire_time` (`app_key_expire_time`) USING BTREE COMMENT '过期时间索引 - 用于清理过期密钥',
    INDEX `idx_user_id` (`user_id`) USING BTREE COMMENT '用户ID索引 - 用于查询某用户的所有AppKey',
    INDEX `idx_delete_flag` (`delete_flag`) USING BTREE COMMENT '删除标记索引 - 用于软删除查询'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = 'AppKey应用密钥表 - 存储AppKey相关信息，用于API访问认证' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 2. AppId应用标识表 (app_id)
-- 用途: 存储AppId相关信息，用于应用标识和关联
-- =====================================================
DROP TABLE IF EXISTS `app_id`;
CREATE TABLE `app_id` (
    `app_id_id` int NOT NULL AUTO_INCREMENT COMMENT 'AppId ID - 主键自增',
    `app_id_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'AppId值 - 应用唯一标识符，全局唯一',
    `app_id_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'AppId名称 - 应用的显示名称',
    `app_id_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '应用描述 - 应用的详细说明',
    `app_id_status` int NULL DEFAULT 1 COMMENT '应用状态 - 0:禁用, 1:启用',
    `app_id_expire_time` bigint NULL DEFAULT NULL COMMENT '过期时间 - Unix时间戳(毫秒)，NULL表示永不过期',
    `app_id_last_use_time` bigint NULL DEFAULT NULL COMMENT '最后使用时间 - Unix时间戳(毫秒)，最近一次使用该AppId的时间',
    `app_id_use_count` bigint NULL DEFAULT 0 COMMENT '使用次数 - 该AppId累计被调用的次数',
    `user_id` int NULL DEFAULT NULL COMMENT '关联用户ID - 该AppId所属的用户ID',
    `app_id_scope` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '允许的权限范围 - 多个scope用逗号分隔，如"read,write,admin"',
    `app_id_grant_types` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '允许的授权类型 - 多个用逗号分隔，如"client_credentials,password,authorization_code,refresh_token"',
    `app_id_redirect_uris` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '允许的回调地址 - 多个用逗号分隔，用于authorization_code模式的redirect_uri校验',
    `app_id_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注信息',
    `delete_flag` int NULL DEFAULT 0 COMMENT '删除标记 - 0:未删除, 1:已删除',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`app_id_id`) USING BTREE,
    UNIQUE INDEX `uk_app_id_value` (`app_id_value`) USING BTREE COMMENT 'AppId值唯一索引 - 确保AppId不重复',
    INDEX `idx_status` (`app_id_status`) USING BTREE COMMENT '状态索引 - 用于快速查询启用的应用',
    INDEX `idx_expire_time` (`app_id_expire_time`) USING BTREE COMMENT '过期时间索引 - 用于清理过期AppId',
    INDEX `idx_user_id` (`user_id`) USING BTREE COMMENT '用户ID索引 - 用于查询某用户的所有AppId',
    INDEX `idx_delete_flag` (`delete_flag`) USING BTREE COMMENT '删除标记索引 - 用于软删除查询'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = 'AppId应用标识表 - 存储AppId相关信息，与app_key表是并列关系' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 3. OAuth应用密钥表 (sys_app_key) - 兼容旧版本
-- 用途: 存储第三方应用的AK/SK密钥对，用于OAuth认证
-- =====================================================
DROP TABLE IF EXISTS `sys_app_key`;
CREATE TABLE `sys_app_key` (
    `sys_app_key_id` int NOT NULL AUTO_INCREMENT COMMENT '应用ID - 主键自增',
    `sys_app_key_app_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'AppKey - 应用访问密钥，类似用户名',
    `sys_app_key_app_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '应用名称 - 应用的显示名称',
    `sys_app_key_secret` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'AppSecret - 应用访问密钥，类似密码（需加密存储）',
    `sys_app_key_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '应用描述 - 应用的详细说明',
    `sys_app_key_whitelist` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'IP白名单 - 允许访问的IP地址列表，逗号分隔，如"192.168.1.1,10.0.0.1"',
    `sys_app_key_status` int NULL DEFAULT 1 COMMENT '应用状态 - 0:禁用, 1:启用',
    `sys_app_key_expire_time` bigint NULL DEFAULT NULL COMMENT '过期时间 - Unix时间戳(毫秒)，NULL表示永不过期',
    `sys_app_key_last_use` datetime NULL DEFAULT NULL COMMENT '最后使用时间 - 最近一次使用该密钥的时间',
    `sys_app_key_use_count` bigint NULL DEFAULT 0 COMMENT '使用次数 - 该密钥累计被调用的次数',
    `sys_app_key_enable_fingerprint` int NULL DEFAULT 0 COMMENT '是否启用指纹检测 - 0:不启用, 1:启用，启用后认证时会验证客户端指纹',
    `sys_app_key_fingerprint_whitelist` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '允许的指纹列表 - 多个指纹用逗号分隔，用于指纹白名单验证',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`sys_app_key_id`) USING BTREE,
    UNIQUE INDEX `uk_app_key` (`sys_app_key_app_key`) USING BTREE COMMENT 'AppKey唯一索引 - 确保AppKey不重复',
    INDEX `idx_status` (`sys_app_key_status`) USING BTREE COMMENT '状态索引 - 用于快速查询启用的应用',
    INDEX `idx_expire_time` (`sys_app_key_expire_time`) USING BTREE COMMENT '过期时间索引 - 用于清理过期密钥'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = 'OAuth应用密钥表 - 存储第三方应用的AK/SK，用于API访问认证' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 4. Token黑名单表 (sys_token_blacklist)
-- 用途: 存储已失效的Token，用于登出、踢出、密码修改等场景
-- =====================================================
DROP TABLE IF EXISTS `sys_token_blacklist`;
CREATE TABLE `sys_token_blacklist` (
    `sys_blacklist_id` bigint NOT NULL AUTO_INCREMENT COMMENT '黑名单ID - 主键自增',
    `sys_token_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Token值 - 完整的JWT Token字符串',
    `sys_token_hash` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Token哈希值 - Token的SHA-256哈希，用于快速查询匹配',
    `sys_user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户ID - 该Token所属的用户ID',
    `sys_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名 - 该Token所属的用户名',
    `sys_blacklist_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '加入黑名单原因 - LOGOUT:用户登出, KICKED:被管理员踢出, EXPIRED:Token过期, REVOKED:主动撤销, PASSWORD_CHANGED:密码修改, SECURITY:安全原因',
    `sys_blacklist_source` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作来源 - USER:用户主动操作, ADMIN:管理员操作, SYSTEM:系统自动处理',
    `sys_expire_time` datetime NOT NULL COMMENT 'Token过期时间 - 与原Token的过期时间一致，用于定期清理',
    `sys_client_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户端IP - 执行操作时的客户端IP地址',
    `sys_user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'User-Agent - 执行操作时的浏览器UA信息',
    `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 - 加入黑名单的时间',
    PRIMARY KEY (`sys_blacklist_id`) USING BTREE,
    INDEX `idx_token_hash` (`sys_token_hash`) USING BTREE COMMENT 'Token哈希索引 - 用于快速验证Token是否在黑名单',
    INDEX `idx_user_id` (`sys_user_id`) USING BTREE COMMENT '用户ID索引 - 用于查询某用户的所有黑名单Token',
    INDEX `idx_expire_time` (`sys_expire_time`) USING BTREE COMMENT '过期时间索引 - 用于定期清理已过期的黑名单记录',
    INDEX `idx_reason` (`sys_blacklist_reason`) USING BTREE COMMENT '原因索引 - 用于统计分析'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = 'Token黑名单表 - 存储已失效的Token，防止失效Token继续使用' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 5. OAuth追踪日志表 (sys_oauth_trace_log)
-- 用途: 记录OAuth认证请求的完整链路追踪信息
-- =====================================================
DROP TABLE IF EXISTS `sys_oauth_trace_log`;
CREATE TABLE `sys_oauth_trace_log` (
    `sys_trace_log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID - 主键自增',
    `sys_trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '追踪ID - 贯穿客户端和服务端的唯一标识',
    `sys_operation_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作类型 - AUTH:认证, LOGIN:登录, LOGOUT:登出, REFRESH:刷新令牌, UPGRADE:升级令牌',
    `sys_client_app_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户端应用名 - 发起请求的应用名称',
    `sys_client_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户端IP - 发起请求的客户端IP地址',
    `sys_user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户ID - 认证成功后的用户ID',
    `sys_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名 - 认证成功后的用户名',
    `sys_request_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '请求参数 - JSON格式，敏感字段已脱敏',
    `sys_response_code` int NULL DEFAULT NULL COMMENT '响应码 - HTTP响应状态码或业务码',
    `sys_success` tinyint(1) NULL DEFAULT 0 COMMENT '是否成功 - 0:失败, 1:成功',
    `sys_total_cost_ms` bigint NULL DEFAULT NULL COMMENT '总耗时 - 从客户端发起到服务端响应的总耗时(毫秒)',
    `sys_spans_detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'Span详情 - JSON数组，记录各节点的名称和耗时',
    `sys_error_message` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误信息 - 失败时的错误描述',
    `sys_request_uri` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求URI - 请求的接口路径',
    `sys_access_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'AccessKey - 使用的应用密钥(已脱敏)',
    `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 - 日志记录时间',
    PRIMARY KEY (`sys_trace_log_id`) USING BTREE,
    INDEX `idx_trace_id` (`sys_trace_id`) USING BTREE COMMENT '追踪ID索引 - 用于关联查询完整链路',
    INDEX `idx_client_app` (`sys_client_app_name`) USING BTREE COMMENT '客户端应用索引 - 用于按应用统计',
    INDEX `idx_user_id` (`sys_user_id`) USING BTREE COMMENT '用户ID索引 - 用于查询用户认证历史',
    INDEX `idx_create_time` (`create_time`) USING BTREE COMMENT '创建时间索引 - 用于时间范围查询和清理',
    INDEX `idx_success` (`sys_success`) USING BTREE COMMENT '成功状态索引 - 用于统计成功/失败率',
    INDEX `idx_operation_type` (`sys_operation_type`) USING BTREE COMMENT '操作类型索引 - 用于按类型统计'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = 'OAuth追踪日志表 - 记录认证请求的完整链路追踪信息，用于问题排查和性能分析' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 定期清理任务说明
-- =====================================================
-- 建议配置定时任务定期清理已过期的黑名单记录：
-- DELETE FROM sys_token_blacklist WHERE sys_expire_time < NOW() - INTERVAL 7 DAY;
-- 
-- 建议配置定时任务定期清理追踪日志（保留30天）：
-- DELETE FROM sys_oauth_trace_log WHERE create_time < NOW() - INTERVAL 30 DAY;
-- 
-- 或者使用事件调度器(Event Scheduler)：
-- CREATE EVENT IF NOT EXISTS cleanup_expired_blacklist
-- ON SCHEDULE EVERY 1 DAY
-- DO DELETE FROM sys_token_blacklist WHERE sys_expire_time < NOW() - INTERVAL 7 DAY;
--
-- CREATE EVENT IF NOT EXISTS cleanup_trace_logs
-- ON SCHEDULE EVERY 1 DAY
-- DO DELETE FROM sys_oauth_trace_log WHERE create_time < NOW() - INTERVAL 30 DAY;

SET FOREIGN_KEY_CHECKS = 1;
