/*
 Navicat Premium Dump SQL

 Source Server         : LocalMySQL
 Source Server Type    : MySQL
 Source Server Version : 80027 (8.0.27)
 Source Host           : localhost:3306
 Source Schema         : project

 Target Server Type    : MySQL
 Target Server Version : 80027 (8.0.27)
 File Encoding         : 65001

 Date: 28/09/2025 15:00:00
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_token
-- ----------------------------
DROP TABLE IF EXISTS `sys_token`;
CREATE TABLE `sys_token`
(
    `sys_token_id`                bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '令牌ID',
    `sys_user_id`                 varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '用户ID',
    `sys_token_value`             varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '令牌值',
    `sys_token_duration`          bigint                                                        NULL DEFAULT NULL COMMENT '持续时间(秒)',
    `sys_token_whitelist_ips`     varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '白名单IP',
    `sys_token_enable_whitelist`  int                                                           NULL DEFAULT 0 COMMENT '是否开启白名单; 0: 关闭, 1: 开启',
    `sys_token_status`            int                                                           NULL DEFAULT 1 COMMENT '令牌状态; 0: 禁用, 1: 启用',
    `sys_token_description`       varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '描述信息',
    `sys_token_deleted`           int                                                           NULL DEFAULT 0 COMMENT '是否删除; 0: 未删除, 1: 已删除',
    `create_name`                 varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by`                   int                                                           NULL DEFAULT NULL COMMENT '创建人',
    `create_time`                 datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`                 datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `update_name`                 varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by`                   int                                                           NULL DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`sys_token_id`) USING BTREE,
    UNIQUE INDEX `uk_sys_token_value`(`sys_token_value` ASC) USING BTREE,
    INDEX `idx_sys_user_id`(`sys_user_id` ASC) USING BTREE,
    INDEX `idx_sys_token_status`(`sys_token_status` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统令牌表'
  ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;