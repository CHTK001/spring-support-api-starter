/*
 Navicat Premium Dump SQL

 Source Server         : 172.16.0.40
 Source Server Type    : MySQL
 Source Server Version : 80027 (8.0.27)
 Source Host           : 172.16.0.40:3306
 Source Schema         : project

 Target Server Type    : MySQL
 Target Server Version : 80027 (8.0.27)
 File Encoding         : 65001

 Date: 04/09/2025 22:46:44
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_ai_group
-- ----------------------------
DROP TABLE IF EXISTS `sys_ai_group`;
CREATE TABLE `sys_ai_group`
(
    `sys_ai_group_id`      int                                                           NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `sys_ai_group_name`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
    `sys_project_id`       int                                                           NULL DEFAULT NULL COMMENT '项目ID',
    `sys_user_id`          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账号ID',
    `sys_ai_group_color`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组颜色',
    `sys_ai_group_default` int                                                           NULL DEFAULT NULL COMMENT '是否默认分组',
    `sys_ai_group_sort`    int                                                           NULL DEFAULT NULL COMMENT '分组顺序',
    `sys_ai_group_config`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组配置',
    `create_name`          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by`            int                                                           NULL DEFAULT NULL COMMENT '创建人',
    `create_time`          datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`          datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `update_name`          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by`            int                                                           NULL DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`sys_ai_group_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '分组'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_ai_group_message
-- ----------------------------
DROP TABLE IF EXISTS `sys_ai_group_message`;
CREATE TABLE `sys_ai_group_message`
(
    `sys_ai_group_message_id`      int                                                           NOT NULL AUTO_INCREMENT,
    `sys_ai_group_id`              int                                                           NULL DEFAULT NULL COMMENT '分组ID',
    `sys_ai_group_message_role`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色',
    `sys_ai_group_message_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci     NULL COMMENT '聊天内容',
    `sys_ai_group_message_type`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '消息类型',
    `create_name`                  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by`                    int                                                           NULL DEFAULT NULL COMMENT '创建人',
    `create_time`                  datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`                  datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `update_name`                  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by`                    int                                                           NULL DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`sys_ai_group_message_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '聊天内容'
  ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
