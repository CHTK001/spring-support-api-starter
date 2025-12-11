/*
 * OAuth认证服务初始化脚本
 * 
 * 注意：此脚本只包含OAuth专用表，不包含基础系统表（sys_user, sys_role, sys_dept等）
 * 基础系统表请使用 spring-api-support-system-starter 模块的 system.sql
 *
 * @author CH
 * @since 2024/12/07
 * @version 1.0
 */

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_app_key (OAuth专用)
-- 用于存储应用的AK/SK密钥
-- ----------------------------
DROP TABLE IF EXISTS `sys_app_key`;
CREATE TABLE `sys_app_key`
(
    `sys_app_key_id`          int                                                            NOT NULL AUTO_INCREMENT COMMENT '应用ID',
    `sys_app_key_app_key`     varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT 'AppKey',
    `sys_app_key_app_name`    varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL     DEFAULT NULL COMMENT '应用名称',
    `sys_app_key_secret`      varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT 'AppSecret',
    `sys_app_key_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL     DEFAULT NULL COMMENT '应用描述',
    `sys_app_key_whitelist`   varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT 'IP白名单',
    `sys_app_key_status`      int                                                            NULL     DEFAULT 1 COMMENT '状态; 0: 禁用, 1: 启用',
    `sys_app_key_expire_time` bigint                                                         NULL     DEFAULT NULL COMMENT '过期时间(时间戳)',
    `sys_app_key_last_use`    datetime                                                       NULL     DEFAULT NULL COMMENT '最后使用时间',
    `sys_app_key_use_count`   bigint                                                         NULL     DEFAULT 0 COMMENT '使用次数',
    `create_name`             varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL     DEFAULT NULL COMMENT '创建人姓名',
    `create_by`               int                                                            NULL     DEFAULT NULL COMMENT '创建人',
    `create_time`             datetime                                                       NULL     DEFAULT NULL COMMENT '创建时间',
    `update_time`             datetime                                                       NULL     DEFAULT NULL COMMENT '更新时间',
    `update_name`             varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL     DEFAULT NULL COMMENT '更新人姓名',
    `update_by`               int                                                            NULL     DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`sys_app_key_id`) USING BTREE,
    UNIQUE INDEX `uk_app_key` (`sys_app_key_app_key`) USING BTREE COMMENT 'AppKey唯一索引',
    INDEX `idx_status` (`sys_app_key_status`) USING BTREE COMMENT '状态索引'
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = 'OAuth应用密钥表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_token_blacklist (OAuth专用)
-- 用于存储已失效的Token黑名单
-- ----------------------------
DROP TABLE IF EXISTS `sys_token_blacklist`;
CREATE TABLE `sys_token_blacklist`
(
    `sys_blacklist_id`      bigint                                                          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `sys_token_value`       varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci   NOT NULL COMMENT 'Token值',
    `sys_token_hash`        varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci    NOT NULL COMMENT 'Token哈希值(用于快速查询)',
    `sys_user_id`           varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci   NULL     DEFAULT NULL COMMENT '用户ID',
    `sys_username`          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci   NULL     DEFAULT NULL COMMENT '用户名',
    `sys_blacklist_reason`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci   NULL     DEFAULT NULL COMMENT '加入黑名单原因: LOGOUT-登出, KICKED-被踢出, EXPIRED-过期, REVOKED-撤销, PASSWORD_CHANGED-密码变更, SECURITY-安全原因',
    `sys_blacklist_source`  varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci   NULL     DEFAULT NULL COMMENT '来源: USER-用户主动, ADMIN-管理员操作, SYSTEM-系统自动',
    `sys_expire_time`       datetime                                                        NOT NULL COMMENT '过期时间(与原Token过期时间一致)',
    `sys_client_ip`         varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci    NULL     DEFAULT NULL COMMENT '操作时的客户端IP',
    `sys_user_agent`        varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci   NULL     DEFAULT NULL COMMENT '操作时的User-Agent',
    `create_time`           datetime                                                        NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`sys_blacklist_id`) USING BTREE,
    INDEX `idx_token_hash` (`sys_token_hash`) USING BTREE COMMENT 'Token哈希索引',
    INDEX `idx_user_id` (`sys_user_id`) USING BTREE COMMENT '用户ID索引',
    INDEX `idx_expire_time` (`sys_expire_time`) USING BTREE COMMENT '过期时间索引',
    INDEX `idx_reason` (`sys_blacklist_reason`) USING BTREE COMMENT '原因索引'
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = 'OAuth Token黑名单表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- View for user login statistics (依赖sys_user_log表，需system模块先初始化)
-- ----------------------------
-- CREATE OR REPLACE VIEW `v_user_login_stats` AS
-- SELECT 
--     `sys_log_username` AS `username`,
--     COUNT(*) AS `total_count`,
--     SUM(CASE WHEN `sys_log_status` = 1 THEN 1 ELSE 0 END) AS `success_count`,
--     SUM(CASE WHEN `sys_log_status` = 0 THEN 1 ELSE 0 END) AS `fail_count`,
--     MAX(`create_time`) AS `last_login_time`,
--     COUNT(DISTINCT `sys_log_ip`) AS `unique_ip_count`
-- FROM `sys_user_log`
-- WHERE `sys_log_from` = 'LOGIN'
-- GROUP BY `sys_log_username`;

SET FOREIGN_KEY_CHECKS = 1;
