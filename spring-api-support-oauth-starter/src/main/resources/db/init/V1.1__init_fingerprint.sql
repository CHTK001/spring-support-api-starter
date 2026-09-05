-- =====================================================
-- 6. 指纹/IP映射表 (sys_fingerprint)
-- 用途: 记录用户登录时的浏览器指纹和IP，支持通过指纹/IP反查用户
-- =====================================================
DROP TABLE IF EXISTS `sys_fingerprint`;
CREATE TABLE `sys_fingerprint` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `fingerprint` varchar(128) COLLATE utf8mb4_general_ci NOT NULL COMMENT '浏览器指纹（SHA-256哈希）',
    `user_id` varchar(64) COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户ID',
    `username` varchar(100) COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户名',
    `ip` varchar(64) COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '登录IP',
    `login_type` varchar(50) COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '登录类型（WEB/MINI/GITEE等）',
    `user_agent` varchar(500) COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'User-Agent',
    `browser` varchar(50) COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '浏览器',
    `os` varchar(50) COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作系统',
    `login_time` bigint NULL DEFAULT NULL COMMENT '登录时间戳（毫秒）',
    `last_login_time` bigint NULL DEFAULT NULL COMMENT '最后在线时间戳（毫秒）',
    `token_count` int NULL DEFAULT 1 COMMENT '当前在线Token数',
    `status` int NULL DEFAULT 1 COMMENT '状态：0-离线，1-在线',
    `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_fingerprint` (`fingerprint`) USING BTREE COMMENT '指纹唯一索引',
    INDEX `idx_user_id` (`user_id`) USING BTREE COMMENT '用户ID索引',
    INDEX `idx_ip` (`ip`) USING BTREE COMMENT 'IP索引',
    INDEX `idx_login_time` (`login_time`) USING BTREE COMMENT '登录时间索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci
COMMENT = '浏览器指纹与用户映射表 - 支持通过指纹/IP反查用户身份' ROW_FORMAT = DYNAMIC;
