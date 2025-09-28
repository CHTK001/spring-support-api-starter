/*
 Navicat Premium Dump SQL

 Source Server         : 172.16.0.40
 Source Server Type    : MySQL
 Source Server Version : 80027 (8.0.27)
 Source Host           : 172.16.0.40:3306
 Source Schema         : service

 Target Server Type    : MySQL
 Target Server Version : 80027 (8.0.27)
 File Encoding         : 65001

 Date: 03/09/2025 09:28:50
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_bank_bins
-- ----------------------------
DROP TABLE IF EXISTS `sys_bank_bins`;
CREATE TABLE `sys_bank_bins`
(
    `sys_blank_bin_id`           int                                                           NOT NULL AUTO_INCREMENT,
    `sys_blank_bin_bank_name`    varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci        NOT NULL DEFAULT '',
    `sys_blank_bin_bank_name_en` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci        NULL     DEFAULT NULL,
    `sys_blank_bin_card_name`    varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci        NULL     DEFAULT NULL,
    `sys_blank_bin_card_type`    varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci        NULL     DEFAULT NULL,
    `sys_blank_bin_bin`          varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci        NOT NULL DEFAULT '',
    `sys_blank_bin_n_length`     tinyint                                                       NULL     DEFAULT NULL,
    `sys_blank_bin_bin_length`   tinyint(1)                                                    NULL     DEFAULT NULL,
    `sys_blank_bin_issue_id`     int                                                           NULL     DEFAULT NULL,
    `create_name`                varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT '创建人姓名',
    `create_by`                  int                                                           NULL     DEFAULT NULL COMMENT '创建人',
    `create_time`                datetime                                                      NULL     DEFAULT NULL COMMENT '创建时间',
    `update_time`                datetime                                                      NULL     DEFAULT NULL COMMENT '更新时间',
    `update_name`                varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT '更新人姓名',
    `update_by`                  int                                                           NULL     DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`sys_blank_bin_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 2379
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '银行BIN对照表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_bank_bins
-- ----------------------------
INSERT INTO `sys_bank_bins`
VALUES (1, '邮政储蓄银行', 'PSBC', '绿卡通', '绿卡通', '621096', 19, 6, 1000000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2, '邮政储蓄银行', 'PSBC', '绿卡通', '绿卡通', '621098', 19, 6, 1000000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (3, '邮政储蓄银行', 'PSBC', '绿卡银联标准卡', '绿卡银联标准卡', '622150', 19, 6, 1000000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (4, '邮政储蓄银行', 'PSBC', '绿卡银联标准卡', '绿卡银联标准卡', '622151', 19, 6, 1000000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (5, '邮政储蓄银行', 'PSBC', '绿卡专用卡', '绿卡专用卡', '622181', 19, 6, 1000000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (6, '邮政储蓄银行', 'PSBC', '绿卡银联标准卡', '绿卡银联标准卡', '622188', 19, 6, 1000000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (7, '邮政储蓄银行', 'PSBC', '绿卡银联标准卡', '绿卡银联标准卡', '622199', 19, 6, 1000000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (8, '邮政储蓄银行', 'PSBC', '绿卡(银联卡)', '绿卡(银联卡)', '955100', 19, 6, 1000000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (9, '邮政储蓄银行', 'PSBC', '绿卡VIP卡', '绿卡VIP卡', '621095', 19, 6, 1000000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (10, '邮政储蓄银行', 'PSBC', '银联标准卡', '银联标准卡', '620062', 19, 6, 1000000, NULL, NULL, NULL, NULL, NULL,
        NULL);

-- ----------------------------
-- Table structure for sys_dict
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict`
(
    `sys_dict_id`        int                                                           NOT NULL AUTO_INCREMENT,
    `sys_dict_name`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典名称',
    `sys_dict_status`    int                                                           NULL DEFAULT NULL COMMENT '状态; 0:隐藏',
    `sys_dict_in_system` int                                                           NULL DEFAULT NULL COMMENT '是否内置; 0:非内置',
    `sys_dict_code`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典编码, 唯一',
    `sys_dict_remark`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
    `sys_dict_i18n`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典i18n',
    `sys_dict_pid`       int                                                           NULL DEFAULT NULL COMMENT 'PID',
    `sys_dict_sort`      int                                                           NULL DEFAULT NULL COMMENT '排序字段',
    `create_name`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by`          int                                                           NULL DEFAULT NULL COMMENT '创建人',
    `create_time`        datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`        datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `update_name`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by`          int                                                           NULL DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`sys_dict_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 8
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '字典表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict
-- ----------------------------
INSERT INTO `sys_dict`
VALUES (1, '厂家', 1, 0, 'CHANG_JIA', '三方厂家', '', 0, NULL, 'sa', NULL, '2024-08-31 20:29:00', '2024-08-31 20:35:57',
        'sa', 1);
INSERT INTO `sys_dict`
VALUES (2, '模板类型', 1, 0, 'MU_BAN_LEI_XING', '', '', 0, NULL, 'sa', NULL, '2024-09-01 18:16:46',
        '2024-09-09 09:33:56', 'sa', 1);
INSERT INTO `sys_dict`
VALUES (3, '短信', 1, 0, 'DUAN_XIN', '', '', 2, NULL, 'sa', 1, '2024-09-07 09:07:09', '2024-09-09 09:23:22', 'sa', 1);
INSERT INTO `sys_dict`
VALUES (4, '微信', 1, 0, 'WEI_XIN', NULL, NULL, 2, NULL, 'sa', 1, '2024-09-09 10:07:50', NULL, NULL, NULL);
INSERT INTO `sys_dict`
VALUES (5, '组件功能', 1, 0, 'ZU_JIAN_GONG_NENG', '', '', 0, NULL, 'sa', 1, '2024-10-24 17:17:50', NULL, NULL, NULL);
INSERT INTO `sys_dict`
VALUES (6, '项目功能', 1, 0, 'XIANG_MU_GONG_NENG', '', '', 0, NULL, 'sa', 1, '2025-01-24 15:50:58', NULL, NULL, NULL);
INSERT INTO `sys_dict`
VALUES (7, '设备类型', 1, 0, 'SHE_BEI_LEI_XING', '', '', 0, NULL, 'sa', 1, '2025-02-11 16:20:16', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_dict_item
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_item`;
CREATE TABLE `sys_dict_item`
(
    `sys_dict_item_id`       int                                                           NOT NULL AUTO_INCREMENT,
    `sys_dict_id`            int                                                           NULL DEFAULT NULL COMMENT '字典ID',
    `sys_dict_item_icon`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标',
    `sys_dict_item_name`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典子项名称',
    `sys_dict_item_remark`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
    `sys_dict_item_status`   int                                                           NULL DEFAULT NULL COMMENT '状态;0: 隐藏',
    `sys_dict_item_sort`     int                                                           NULL DEFAULT NULL COMMENT '优先级',
    `sys_dict_item_i18n`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典i18n',
    `sys_dict_item_code`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '编码',
    `sys_dict_item_delete`   int                                                           NULL DEFAULT NULL COMMENT '是否删除0:未删除',
    `sys_dict_item_property` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '可配置项',
    `create_name`            varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by`              int                                                           NULL DEFAULT NULL COMMENT '创建人',
    `create_time`            datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`            datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `update_name`            varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by`              int                                                           NULL DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`sys_dict_item_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 41
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '字典项子项'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict_item
-- ----------------------------
INSERT INTO `sys_dict_item`
VALUES (1, 1, NULL, '海康云曜', '海康云曜', 1, 1, '', 'HAI_KANG_YUN_YAO', 0, NULL, 'sa', NULL, '2024-09-01 10:02:02',
        '2024-09-01 10:08:45', 'sa', 1);
INSERT INTO `sys_dict_item`
VALUES (2, 1, NULL, '阿里云', '阿里云', 1, 1, NULL, 'ALIYUN', 0, NULL, 'sa', NULL, '2024-09-01 10:10:03', NULL, NULL,
        NULL);
INSERT INTO `sys_dict_item`
VALUES (3, 1, NULL, '百度云', '百度云', 1, 1, NULL, 'BDYUN', 0, NULL, 'sa', NULL, '2024-09-01 10:10:13', NULL, NULL,
        NULL);
INSERT INTO `sys_dict_item`
VALUES (4, 2, NULL, '短信', '', 1, 1, '', 'SMS', 0, NULL, 'sa', 1, '2024-09-03 13:35:36', '2024-09-09 10:04:58', 'sa',
        1);
INSERT INTO `sys_dict_item`
VALUES (5, 3, NULL, '短信通知', '', 1, 2, '', 'DUAN_XIN_TONG_ZHI', 0, NULL, 'sa', 1, '2024-09-03 13:48:51',
        '2024-09-05 16:13:21', 'sa', 1);
INSERT INTO `sys_dict_item`
VALUES (6, 2, NULL, '站内', '', 1, 2, '', 'WEB', 0, NULL, 'sa', 1, '2024-09-03 13:54:31', '2024-09-03 13:55:18', 'sa',
        1);
INSERT INTO `sys_dict_item`
VALUES (7, 2, NULL, '微信', NULL, 1, 2, NULL, 'WECHAT', 0, NULL, 'sa', 1, '2024-09-03 13:55:13', '2024-09-05 15:54:08',
        'sa', 1);
INSERT INTO `sys_dict_item`
VALUES (8, 3, NULL, '验证码', '', 1, 1, '', 'YAN_ZHENG_MA', 0, NULL, 'sa', 1, '2024-09-09 09:24:59',
        '2024-09-09 09:31:22', 'sa', 1);
INSERT INTO `sys_dict_item`
VALUES (9, 4, NULL, '微信通知', NULL, 1, NULL, NULL, 'WEI_XIN_TONG_ZHI', 0, NULL, 'sa', 1, '2024-09-09 10:08:38', NULL,
        NULL, NULL);
INSERT INTO `sys_dict_item`
VALUES (10, 1, NULL, '讯飞', '', 1, 1, '', 'XUN_FEI', 0, 'sysProjectAppId,sysProjectAppSecret', 'sa', 1,
        '2024-10-24 15:07:24', '2025-02-17 10:38:56', 'sa', 1);
INSERT INTO `sys_dict_item`
VALUES (11, 5, NULL, '天气', '', 1, 1, '', 'TIAN_QI', 0, NULL, 'sa', 1, '2024-10-24 17:18:02', NULL, NULL, NULL);
INSERT INTO `sys_dict_item`
VALUES (12, 5, NULL, '时间', '', 1, 1, '', 'SHI_JIAN', 0, NULL, 'sa', 1, '2024-10-28 09:31:55', NULL, NULL, NULL);
INSERT INTO `sys_dict_item`
VALUES (13, 6, 'fa-solid:sms', '短信', NULL, 1, 1, NULL, 'DUAN_XIN', 0, NULL, 'sa', 1, '2025-01-24 15:51:14',
        '2025-01-24 15:51:23', 'sa', 1);
INSERT INTO `sys_dict_item`
VALUES (14, 6, 'ep:video-camera', '摄像头', '', 1, 1, '', 'SHE_XIANG_TOU', 1, NULL, 'sa', 1, '2025-01-24 15:51:33',
        '2025-01-24 15:54:21', 'sa', 1);
INSERT INTO `sys_dict_item`
VALUES (15, 6, 'humbleicons:eye', '人像识别', '', 1, 1, '', 'REN_XIANG_SHI_BIE', 0, NULL, 'sa', 1,
        '2025-01-25 18:55:49', NULL, NULL, NULL);
INSERT INTO `sys_dict_item`
VALUES (16, 6, 'humbleicons:mail', '邮件', '', 1, 1, '', 'YOU_JIAN', 0, NULL, 'sa', 1, '2025-01-27 13:47:56', NULL,
        NULL, NULL);
INSERT INTO `sys_dict_item`
VALUES (17, 6, 'pixelarticons:device-tv-smart', '设备', '', 1, 1, '', 'SHE_BEI', 0, NULL, 'sa', 1,
        '2025-01-27 13:48:31', '2025-02-06 13:36:28', 'sa', 1);
INSERT INTO `sys_dict_item`
VALUES (18, 1, 'bi:pc-display-horizontal', '内部系统', '', 1, 1, '', 'NEI_BU_XI_TONG', 0, NULL, 'sa', 1,
        '2025-01-27 13:49:49', '2025-02-17 10:32:02', 'sa', 1);
INSERT INTO `sys_dict_item`
VALUES (19, 1, NULL, '海康安防', '海康安防', 1, 1, '', 'HAI_KANG_AN_FANG', 0,
        'sysProjectAppId,sysProjectAppSecret,sysProjectEndpoint', 'sa', 1, '2025-02-05 14:43:32', '2025-02-05 14:43:40',
        'sa', 1);
INSERT INTO `sys_dict_item`
VALUES (20, 6, 'ri:robot-2-line', '大语言', '', 1, 1, '', 'DA_YU_YAN', 0, 'sysProjectAppKey,sysProjectEndpoint', 'sa',
        1, '2025-02-07 14:47:07', '2025-02-17 10:34:02', 'sa', 1);
INSERT INTO `sys_dict_item`
VALUES (21, 1, NULL, 'deepseek', '', 1, 1, '', 'deepseek', 0, NULL, 'sa', 1, '2025-02-08 14:28:56',
        '2025-02-17 10:31:59', 'sa', 1);
INSERT INTO `sys_dict_item`
VALUES (22, 1, NULL, 'kimi', '', 1, 1, '', 'kimi', 0, NULL, 'sa', 1, '2025-02-08 16:33:03', '2025-02-17 10:31:57', 'sa',
        1);
INSERT INTO `sys_dict_item`
VALUES (23, 1, NULL, 'ollama', '', 1, 1, '', 'ollama', 0, NULL, 'sa', 1, '2025-02-10 10:16:25', '2025-02-17 10:04:54',
        'sa', 1);
INSERT INTO `sys_dict_item`
VALUES (24, 7, 'mingcute:computer-camera-fill', '摄像头', '', 1, 1, '', 'CAMERA', 0, NULL, 'sa', 1,
        '2025-02-11 16:37:27', '2025-02-11 16:46:19', 'sa', 1);
INSERT INTO `sys_dict_item`
VALUES (25, 1, NULL, 'OpenAi', '', 1, 1, '', 'openai', 0, NULL, 'sa', 1, '2025-02-17 09:34:25', '2025-02-17 10:06:38',
        'sa', 1);
INSERT INTO `sys_dict_item`
VALUES (26, 1, NULL, '豆包', '', 1, 1, '', 'DOUBAO', 0, NULL, 'sa', 1, '2025-02-20 14:39:27', '2025-02-20 14:39:56',
        'sa', 1);
INSERT INTO `sys_dict_item`
VALUES (27, 6, 'ri:inbox-unarchive-line', '文生图', '', 1, 1, '', 'WEN_SHENG_TU', 0, NULL, 'sa', 1,
        '2025-02-26 10:36:37', '2025-02-28 11:24:54', 'sa', 1);
INSERT INTO `sys_dict_item`
VALUES (28, 1, NULL, '硅基流动', '', 1, 1, '', 'SILICONFLOW', 0, NULL, 'sa', 1, '2025-02-27 16:20:06',
        '2025-02-27 16:20:44', 'sa', 1);
INSERT INTO `sys_dict_item`
VALUES (29, 1, NULL, 'OpenRouter', '', 1, 1, '', 'OPEN_ROUTER', 0, NULL, 'sa', 1, '2025-02-28 13:57:56', NULL, NULL,
        NULL);
INSERT INTO `sys_dict_item`
VALUES (30, 1, NULL, '智谱清言', '', 1, 1, '', 'ZHIPU', 0, NULL, 'sa', 1, '2025-02-28 15:28:05', NULL, NULL, NULL);
INSERT INTO `sys_dict_item`
VALUES (31, 1, NULL, '无问芯穹', '', 1, 1, '', 'WU_WEN_XIN_QIONG', 0, NULL, 'sa', 1, '2025-03-01 21:28:23', NULL, NULL,
        NULL);
INSERT INTO `sys_dict_item`
VALUES (32, 1, NULL, 'Cloudflare', '', 1, 1, '', 'Cloudflare', 0, NULL, 'sa', 1, '2025-03-01 22:34:45', NULL, NULL,
        NULL);
INSERT INTO `sys_dict_item`
VALUES (33, 6, 'ep:video-camera', '文生视频', '', 1, 1, '', 'WEN_SHENG_SHI_PIN', 0, NULL, 'sa', 1,
        '2025-03-04 14:32:30', '2025-03-04 14:34:29', 'sa', 1);
INSERT INTO `sys_dict_item`
VALUES (34, 1, NULL, '腾讯', '', 1, 1, '', 'TENCENT', 0, NULL, 'sa', 1, '2025-03-07 15:38:26', NULL, NULL, NULL);
INSERT INTO `sys_dict_item`
VALUES (35, 1, 'ep:camera-filled', '海康云眸', '', 1, 1, '', 'HAI_KANG_YUN_MOU', 0, NULL, 'sa', 1,
        '2025-03-17 08:45:39', NULL, NULL, NULL);
INSERT INTO `sys_dict_item`
VALUES (36, 7, 'ep:credit-card', '门禁', '', 1, 1, '', 'MEN_JIN', 0, NULL, 'sa', 1, '2025-03-17 10:51:36',
        '2025-03-17 10:51:48', 'sa', 1);
INSERT INTO `sys_dict_item`
VALUES (37, 5, NULL, '日历', '', 1, 1, '', 'RI_LI', 0, NULL, 'sa', 1, '2025-03-18 15:03:56', NULL, NULL, NULL);
INSERT INTO `sys_dict_item`
VALUES (38, 1, NULL, '佐糖', '', 1, 1, '', 'ZUO_TANG', 0, NULL, 'sa', 1, '2025-03-20 13:41:08', NULL, NULL, NULL);
INSERT INTO `sys_dict_item`
VALUES (39, 6, 'simple-icons:davinciresolve', '超分辨率', '', 1, 1, '', 'RESOLUTION', 0, NULL, 'sa', 1,
        '2025-03-20 13:44:02', '2025-03-20 13:45:14', 'sa', 1);
INSERT INTO `sys_dict_item`
VALUES (40, 6, 'mingcute:color-picker-fill', '图像上色', '', 1, 1, '', 'COLORIZATION', 0, NULL, 'sa', 1,
        '2025-03-21 10:29:53', '2025-03-21 10:30:05', 'sa', 1);

-- ----------------------------
-- Table structure for sys_dict_item_property
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_item_property`;
CREATE TABLE `sys_dict_item_property`
(
    `sys_dict_item_property_id`            int                                                           NOT NULL AUTO_INCREMENT,
    `sys_dict_item_id`                     int                                                           NULL DEFAULT NULL COMMENT '字典子项ID',
    `sys_dict_item_property_group`         varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '配置项分组',
    `sys_dict_item_property_name`          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置项',
    `sys_dict_item_property_value`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置值',
    `sys_dict_item_property_selected_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '选中类型',
    `create_name`                          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by`                            int                                                           NULL DEFAULT NULL COMMENT '创建人',
    `create_time`                          datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`                          datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `update_name`                          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by`                            int                                                           NULL DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`sys_dict_item_property_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 33
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '字典配置项'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict_item_property
-- ----------------------------
INSERT INTO `sys_dict_item_property`
VALUES (14, 20, 'DA_YU_YAN', 'sysProjectAppKey', 'true', 'Boolean', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_dict_item_property`
VALUES (19, 20, 'DA_YU_YAN', 'sysProjectEndpoint', 'true', 'Boolean', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_dict_item_property`
VALUES (21, 10, 'DA_YU_YAN', 'sysProjectAppId', 'true', 'Boolean', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_dict_item_property`
VALUES (22, 10, 'DA_YU_YAN', 'sysProjectAppSecret', 'true', 'Boolean', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_dict_item_property`
VALUES (23, 23, 'DA_YU_YAN', 'sysProjectAppKey', 'false', 'Boolean', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_dict_item_property`
VALUES (25, 19, 'DEVICE', 'sysProjectAppId', 'true', 'Boolean', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_dict_item_property`
VALUES (26, 19, 'DEVICE', 'sysProjectAppSecret', 'true', 'Boolean', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_dict_item_property`
VALUES (27, 19, 'DEVICE', 'sysProjectEndpoint', 'true', 'Boolean', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_dict_item_property`
VALUES (29, 27, 'WEN_SHENG_TU', 'sysProjectAppKey', 'true', 'Boolean', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_dict_item_property`
VALUES (30, 27, 'WEN_SHENG_TU', 'sysProjectEndpoint', 'true', 'Boolean', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_dict_item_property`
VALUES (31, 34, 'DA_YU_YAN', 'sysProjectAppSecret', 'true', 'Boolean', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_dict_item_property`
VALUES (32, 34, 'DA_YU_YAN', 'sysProjectAppId', 'true', 'Boolean', NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log`
(
    `sys_log_id`       int                                                           NOT NULL AUTO_INCREMENT,
    `sys_log_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账号',
    `sys_log_address`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求IP',
    `sys_log_url`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求地址',
    `sys_log_param`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求参数',
    `sys_log_from`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求模块',
    `sys_log_status`   int                                                           NULL DEFAULT NULL COMMENT '请求状态; 0: 失败; 1: 成功',
    `sys_log_cost`     int                                                           NULL DEFAULT NULL COMMENT '耗时',
    `create_name`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by`        int                                                           NULL DEFAULT NULL COMMENT '创建人',
    `create_time`      datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`      datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `update_name`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by`        int                                                           NULL DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`sys_log_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统日志表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_log
-- ----------------------------

-- ----------------------------
-- Table structure for sys_setting_group
-- ----------------------------
DROP TABLE IF EXISTS `sys_setting_group`;
CREATE TABLE `sys_setting_group` (
  `sys_setting_group_id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `sys_setting_group_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `sys_setting_group_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '唯一编码',
  `sys_setting_group_icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标',
  `sys_setting_group_enable` tinyint(1) NULL DEFAULT NULL COMMENT '是否启用',
  `sys_setting_group_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `sys_setting_group_sort` int NULL DEFAULT NULL COMMENT '排序',
  `sys_setting_group_use_project_interface` tinyint(1) NULL DEFAULT NULL COMMENT '是否使用项目组接口',
  `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_by` int NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`sys_setting_group_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '配置组' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_setting_group
-- ----------------------------

-- ----------------------------
-- Table structure for sys_setting
-- ----------------------------
DROP TABLE IF EXISTS `sys_setting`;
CREATE TABLE `sys_setting`
(
    `sys_setting_id`         int                                                           NOT NULL AUTO_INCREMENT,
    `sys_setting_sort`       int                                                           NULL DEFAULT NULL COMMENT '优先级',
    `sys_setting_name`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置名称',
    `sys_setting_value`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置值',
    `sys_setting_status`     int                                                           NULL DEFAULT NULL COMMENT '是否启用; 0:不启用',
    `sys_setting_value_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置类型; 用于前端组件判断; e.g.BOOL：布尔',
    `sys_setting_group`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置所属分组',
    `sys_setting_collect`    int                                                           NULL DEFAULT NULL COMMENT '是否是多个配置集合;0:单个',
    `sys_setting_in_system`  int                                                           NULL DEFAULT NULL COMMENT '是否系统配置; 0:非系统配置',
    `sys_setting_app_inner`  int                                                           NULL DEFAULT NULL COMMENT '配置是否作用域系统启动',
    `sys_setting_remark`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置备注',
    `sys_setting_config`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置参数',
    `sys_setting_admin`      int                                                           NULL DEFAULT NULL COMMENT '是否只有管理员生效',
    `sys_setting_version`    int                                                           NULL DEFAULT NULL COMMENT '版本',
    `create_name`            varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by`              int                                                           NULL DEFAULT NULL COMMENT '创建人',
    `create_time`            datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`            datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `update_name`            varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by`              int                                                           NULL DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`sys_setting_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 43
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统全局配置'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_setting
-- ----------------------------
INSERT INTO `sys_setting`
VALUES (1, 4, 'CheckCodeOpen', 'false', 1, 'Boolean', 'default', 0, 1, 0, '验证码开关', NULL, NULL, 15, NULL, NULL,
        NULL, '2025-02-19 14:19:05', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (2, 0, 'SystemName', '后台管理系统', 1, 'String', 'default', 0, 1, 0, '系统标题', NULL, NULL, 15, NULL, NULL,
        NULL, '2025-02-19 14:19:05', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (3, 5, 'SlidingBlockOpen', 'false', 1, 'Boolean', 'default', 0, 1, 0, '滑动验证码', NULL, NULL, 15, NULL, NULL,
        NULL, '2025-02-19 14:19:05', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (4, 9, 'CrashPageOpen', 'false', 1, 'Boolean', 'config', 0, 1, 0, '调试模式崩溃当前页', NULL, NULL, 26, 'sa',
        NULL, '2024-08-18 11:22:26', '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (5, 10, 'CodecResponseOpen', 'false', 1, 'Boolean', 'config', 0, 1, 0, '返回加密', NULL, NULL, 26, 'sa', NULL,
        '2024-08-18 11:31:57', '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (6, 5, 'WatermarkOpen', 'false', 1, 'Boolean', 'config', 0, 1, 0, '水印', NULL, NULL, 26, 'sa', NULL,
        '2024-08-19 19:53:55', '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (7, 11, 'LoopDebuggerOpen', 'false', 1, 'Boolean', 'config', 0, 1, 0, '循环debugger', NULL, NULL, 26, 'sa', NULL,
        '2024-08-18 10:52:15', '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (8, 4, 'Version', '1', 1, 'Number', 'config', 0, 1, 0, '系统版本', 'fe7db08a-1001-4e7b-95ab-776a569a85c0', NULL,
        123, NULL, NULL, NULL, '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (9, 2, 'SocketUrl', 'http://172.16.2.226:29181', 1, 'Array', 'config', 0, 1, 0, 'socket.io服务器地址', NULL, 1,
        26, NULL, NULL, NULL, '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (10, 12, 'SocketOpen', 'false', 1, 'Boolean', 'config', 0, 1, 0, 'socket.io是否开启', NULL, 1, 26, NULL, NULL,
        NULL, '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (11, 13, 'SocketPath', '/socket.io', 1, 'String', 'config', 0, 1, 0, 'socket.io路径', NULL, NULL, NULL, NULL,
        NULL, NULL, '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (12, 6, 'WatermarkColor', '#409EFF', 1, 'Color', 'config', 0, 1, 0, '水印颜色', NULL, NULL, 26, NULL, NULL, NULL,
        '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (13, 8, 'WatermarkAlpha', '0.35', 1, 'Number', 'config', 0, 1, 0, '水印透明度', NULL, NULL, 26, NULL, NULL, NULL,
        '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (14, 7, 'WatermarkRotate', '-30', 1, 'Number', 'config', 0, 1, 0, '水印角度', NULL, NULL, 26, NULL, NULL, NULL,
        '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (15, 14, 'CodecRequestOpen', 'false', 1, 'Boolean', 'config', 0, 1, 0, '请求加密', NULL, NULL, 26, NULL, NULL,
        NULL, '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (16, 3, 'CodecRequestKey', '0123456789abcdef', 1, 'String', 'config', 0, 1, 1, '请求加密密钥', NULL, NULL, 26,
        NULL, NULL, NULL, '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (17, 2, 'Host', 'smtp.163.com', 1, 'String', 'smtp', 1, 1, 0, '服务器配置', NULL, NULL, NULL, NULL, NULL, NULL,
        '2024-11-27 13:12:49', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (18, 3, 'Port', '25', 1, 'Number', 'smtp', 1, 1, 0, '服务器端口', NULL, NULL, NULL, NULL, NULL, NULL,
        '2024-11-27 13:12:49', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (19, 4, 'Password', 'CVIRRMZQPYLQIQMW_C', 1, 'Password', 'smtp', 1, 1, 0, '授权码', NULL, NULL, NULL, NULL, NULL,
        NULL, '2024-11-27 13:12:49', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (20, 1, 'From', 'yemengzhidiandi@163.com', 1, 'Mail', 'smtp', 1, 1, 0, '发送账号', NULL, NULL, NULL, NULL, NULL,
        NULL, '2024-11-27 13:12:49', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (21, 1, 'AppId', 'wx8cd1bb691119e3f7', 1, 'String', 'weixin', 1, 1, 0, '微信AppId', NULL, NULL, NULL, NULL, NULL,
        NULL, '2024-09-05 16:32:37', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (22, 2, 'AppSecret', 'abff9e5a7541a5db21a819b087ecbf10', 1, 'String', 'weixin', 1, 1, 0, '微信AppSecret', NULL,
        NULL, NULL, NULL, NULL, NULL, '2024-09-05 16:32:37', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (23, 1, 'AppId', 'f0d999a2e91e9a4edcec329fe7e6b3f1fa8b7063f8a618c3c7a2e0d0a13d5943', 1, 'String', 'gitee', 1, 1,
        0, 'GiteeAppId', NULL, NULL, NULL, NULL, NULL, NULL, '2024-09-05 16:49:41', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (24, 2, 'AppSecret', '158ced91fe30cfa06798c638d2b477d54e57b623a7e869c42d34d3125e88f1b7', 1, 'String', 'gitee', 1,
        1, 0, 'GiteeAppSecret', NULL, NULL, NULL, NULL, NULL, NULL, '2024-09-05 16:49:41', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (25, 3, 'Gitee', 'true', 1, 'Boolean', 'sso', 1, 1, 0, 'Gitee登录', NULL, NULL, NULL, NULL, NULL, NULL,
        '2025-02-18 16:11:53', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (26, 1, 'AppId', 'LTAI5tKQQnvCS3yEjGxjHmCd', 1, 'String', 'sms', 1, 1, 0, '短信AppId', NULL, NULL, NULL, NULL,
        NULL, NULL, '2024-09-10 10:25:22', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (27, 2, 'AppSecret', 'Pm45AutVaPLqWCOUNeZ7gEQBTYJQAhh', 1, 'String', 'sms', 1, 1, 0, '短信AppSecret', NULL, NULL,
        NULL, NULL, NULL, NULL, '2024-09-10 10:25:22', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (28, 3, 'AppSign', '犀鸟校园', 1, 'String', 'sms', 1, 1, 0, '短信签名', NULL, NULL, NULL, NULL, NULL, NULL,
        '2024-09-10 10:25:22', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (29, 4, 'AppType', 'ALIYUN', 1, 'Dict', 'sms', 1, 1, 0, '短信厂商', '1', NULL, NULL, NULL, NULL, NULL,
        '2024-09-10 10:25:22', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (30, 1, 'AppKey', 'YfuzeuBcnvSluLjHkviR:WFjoVbqyzcqfttbxnsZu', 1, 'AppKey', 'llm', 1, 1, 0, '密钥', NULL, NULL,
        NULL, NULL, NULL, NULL, '2024-10-24 15:07:36', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (31, 2, 'AppType', 'XUN_FEI', 1, 'Dict', 'llm', 1, 1, 0, '语言模型厂商', '1', NULL, NULL, NULL, NULL, NULL,
        '2024-10-24 15:07:36', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (32, 3, 'AppId', '7d9dec86', 1, 'String', 'llm', 1, 1, 0, '模型AppId', NULL, NULL, NULL, NULL, NULL, NULL,
        '2024-10-24 15:07:36', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (33, 4, 'AppSecret', 'NTZiODE3MjI3ZGZhMmRjM2FjYTk5ZWJk', 1, 'AppSecret', 'llm', 1, 1, 0, '模型AppSecret', NULL,
        NULL, NULL, NULL, NULL, NULL, '2024-10-24 15:07:36', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (34, 6, 'CheckTotpCodeOpen', 'false', 1, 'Boolean', 'default', NULL, 0, NULL, '开启totp', NULL, NULL, NULL, 'sa',
        1, '2024-12-08 11:56:39', '2025-02-19 14:19:05', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (35, 1, 'WebrtcOpen', 'true', 1, 'Boolean', 'webrtc', NULL, NULL, NULL, '是否开启', NULL, NULL, NULL, NULL, NULL,
        NULL, '2024-12-11 10:26:15', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (36, 2, 'WebrtcUrl', 'http://172.16.2.226:29182', 1, 'String', 'webrtc', NULL, NULL, NULL, 'webrtc服务器地址',
        NULL, NULL, NULL, NULL, NULL, NULL, '2024-12-11 10:26:15', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (37, 3, 'WebrtcContext', '/webrtc', 1, 'String', 'webrtc', NULL, NULL, NULL, '路径', NULL, NULL, NULL, NULL,
        NULL, NULL, '2024-12-11 10:26:15', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (38, 1, 'OssAddress', 'http://127.0.0.01:19180', 1, 'String', 'config', NULL, 0, NULL, '资源地址', NULL, NULL,
        NULL, 'sa', 1, '2025-02-10 16:10:37', '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (39, 0, 'ApiAddress', NULL, 1, 'String', 'config', NULL, 0, NULL, '接口地址', NULL, NULL, NULL, 'sa', 1,
        '2025-02-17 08:48:56', '2025-02-19 11:10:48', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (40, 2, 'OpenTenantLogin', 'true', 1, 'Boolean', 'default', NULL, 0, NULL, '租户登录', NULL, NULL, NULL, 'sa', 1,
        '2025-02-19 11:14:15', '2025-02-19 14:19:05', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (41, 3, 'OpenTenantLoginRequire', 'false', 1, 'Boolean', 'default', NULL, 0, NULL, '填写租户', NULL, NULL, NULL,
        'sa', 1, '2025-02-19 14:18:26', '2025-02-19 14:19:05', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (42, 1, 'OpenBaseLogin', 'true', 1, 'Boolean', 'default', NULL, 0, NULL, '基础登录', NULL, NULL, NULL, 'sa', 1,
        '2025-02-19 14:18:52', '2025-02-19 14:19:05', 'sa', 1);

-- ----------------------------
-- Table structure for sys_limit_configuration
-- ----------------------------
DROP TABLE IF EXISTS `sys_limit_configuration`;
CREATE TABLE `sys_limit_configuration`
(
    `sys_limit_configuration_id`           bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `sys_limit_path`                       varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '接口路径',
    `sys_limit_name`                       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '限流规则名称',
    `sys_limit_for_period`                 int                                                           NULL DEFAULT NULL COMMENT '每个周期的许可数量',
    `sys_limit_refresh_period_seconds`     bigint                                                        NULL DEFAULT NULL COMMENT '限制刷新周期（秒）',
    `sys_limit_timeout_duration_millis`    bigint                                                        NULL DEFAULT NULL COMMENT '获取许可的超时时间（毫秒）',
    `sys_limit_dimension`                  varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '限流维度（GLOBAL, IP, USER, API）',
    `sys_limit_key_expression`             varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '自定义键表达式（SpEL）',
    `sys_limit_fallback_method`            varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '降级方法名称',
    `sys_limit_message`                    varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误消息',
    `sys_limit_status`                     int                                                           NULL DEFAULT NULL COMMENT '是否启用; 0: 禁用, 1: 启用',
    `sys_limit_description`                varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述信息',
    `sys_limit_sort`                       int                                                           NULL DEFAULT NULL COMMENT '排序',
    `create_name`                          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by`                            int                                                           NULL DEFAULT NULL COMMENT '创建人',
    `create_time`                          datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`                          datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `update_name`                          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by`                            int                                                           NULL DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`sys_limit_configuration_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '限流配置表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_limit_configuration
-- ----------------------------

-- ----------------------------
-- Table structure for sys_limit_record
-- ----------------------------
DROP TABLE IF EXISTS `sys_limit_record`;
CREATE TABLE `sys_limit_record`
(
    `sys_limit_record_id`           bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `sys_limit_configuration_id`    bigint                                                        NULL DEFAULT NULL COMMENT '限流配置ID',
    `sys_limit_path`                varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '接口路径',
    `sys_limit_name`                varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '限流规则名称',
    `sys_limit_dimension`           varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '限流维度（GLOBAL, IP, USER, API）',
    `sys_limit_key`                 varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '限流键值（如IP地址、用户ID等）',
    `sys_limit_time`                datetime                                                      NULL DEFAULT NULL COMMENT '限流时间',
    `sys_user_id`                   bigint                                                        NULL DEFAULT NULL COMMENT '用户ID',
    `sys_user_name`                 varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名',
    `client_ip`                     varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '客户端IP',
    `request_url`                   varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求URL',
    `request_method`                varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '请求方法',
    `user_agent`                    varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户代理',
    `create_name`                   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by`                     int                                                           NULL DEFAULT NULL COMMENT '创建人',
    `create_time`                   datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`                   datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `update_name`                   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by`                     int                                                           NULL DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`sys_limit_record_id`) USING BTREE,
    INDEX `idx_sys_limit_configuration_id` (`sys_limit_configuration_id`),
    INDEX `idx_sys_limit_time` (`sys_limit_time`),
    INDEX `idx_sys_user_id` (`sys_user_id`),
    INDEX `idx_client_ip` (`client_ip`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '限流记录表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_limit_record
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
