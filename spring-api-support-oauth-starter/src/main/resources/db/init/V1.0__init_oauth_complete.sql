/*
 * OAuth认证模块 - 数据库初始化脚本
 * 
 * 包含表:
 *   - sys_app_key: OAuth应用密钥表(AK/SK)
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
-- 1. OAuth应用密钥表 (sys_app_key)
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
-- 2. Token黑名单表 (sys_token_blacklist)
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
-- 定期清理任务说明
-- =====================================================
-- 建议配置定时任务定期清理已过期的黑名单记录：
-- DELETE FROM sys_token_blacklist WHERE sys_expire_time < NOW() - INTERVAL 7 DAY;
-- 
-- 或者使用事件调度器(Event Scheduler)：
-- CREATE EVENT IF NOT EXISTS cleanup_expired_blacklist
-- ON SCHEDULE EVERY 1 DAY
-- DO DELETE FROM sys_token_blacklist WHERE sys_expire_time < NOW() - INTERVAL 7 DAY;

SET FOREIGN_KEY_CHECKS = 1;
