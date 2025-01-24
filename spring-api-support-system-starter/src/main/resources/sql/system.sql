/*
 Navicat Premium Dump SQL

 Source Server         : 172.16.0.40
 Source Server Type    : MySQL
 Source Server Version : 80016 (8.0.16)
 Source Host           : 172.16.0.40:3306
 Source Schema         : system

 Target Server Type    : MySQL
 Target Server Version : 80016 (8.0.16)
 File Encoding         : 65001

 Date: 24/01/2025 10:04:06
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_bank_bins
-- ----------------------------
DROP TABLE IF EXISTS `sys_bank_bins`;
CREATE TABLE `sys_bank_bins`
(
    `sys_blank_bin_id`           int(11)                                                       NOT NULL AUTO_INCREMENT,
    `sys_blank_bin_bank_name`    varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci        NOT NULL DEFAULT '',
    `sys_blank_bin_bank_name_en` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci        NULL     DEFAULT NULL,
    `sys_blank_bin_card_name`    varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci        NULL     DEFAULT NULL,
    `sys_blank_bin_card_type`    varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci        NULL     DEFAULT NULL,
    `sys_blank_bin_bin`          varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci        NOT NULL DEFAULT '',
    `sys_blank_bin_n_length`     tinyint(4)                                                    NULL     DEFAULT NULL,
    `sys_blank_bin_bin_length`   tinyint(1)                                                    NULL     DEFAULT NULL,
    `sys_blank_bin_issue_id`     int(11)                                                       NULL     DEFAULT NULL,
    `create_name`                varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT '创建人姓名',
    `create_by`                  int(11)                                                       NULL     DEFAULT NULL COMMENT '创建人',
    `create_time`                datetime                                                      NULL     DEFAULT NULL COMMENT '创建时间',
    `update_time`                datetime                                                      NULL     DEFAULT NULL COMMENT '更新时间',
    `update_name`                varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT '更新人姓名',
    `update_by`                  int(11)                                                       NULL     DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`sys_blank_bin_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 2379
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '银行BIN对照表'
  ROW_FORMAT = Dynamic;

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
INSERT INTO `sys_bank_bins`
VALUES (11, '邮政储蓄银行', 'PSBC', '中职学生资助卡', '中职学生资助卡', '621285', 19, 6, 1000000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (12, '邮政储蓄银行', 'PSBC', 'IC绿卡通VIP卡', 'IC绿卡通VIP卡', '621798', 19, 6, 1000000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (13, '邮政储蓄银行', 'PSBC', 'IC绿卡通', 'IC绿卡通', '621799', 19, 6, 1000000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (14, '邮政储蓄银行', 'PSBC', 'IC联名卡', 'IC联名卡', '621797', 19, 6, 1000000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (15, '邮政储蓄银行', 'PSBC', 'IC预付费卡', 'IC预付费卡', '620529', 19, 6, 1000000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (16, '邮政储蓄银行', 'PSBC', '绿卡储蓄卡(银联卡)', '绿卡储蓄卡(银联卡)', '62215049', 19, 8, 1004900, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (17, '邮政储蓄银行', 'PSBC', '绿卡储蓄卡(银联卡)', '绿卡储蓄卡(银联卡)', '62215050', 19, 8, 1004900, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (18, '邮政储蓄银行', 'PSBC', '绿卡储蓄卡(银联卡)', '绿卡储蓄卡(银联卡)', '62215051', 19, 8, 1004900, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (19, '邮政储蓄银行', 'PSBC', '绿卡储蓄卡(银联卡)', '绿卡储蓄卡(银联卡)', '62218850', 19, 8, 1004900, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (20, '邮政储蓄银行', 'PSBC', '绿卡储蓄卡(银联卡)', '绿卡储蓄卡(银联卡)', '62218851', 19, 8, 1004900, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (21, '邮政储蓄银行', 'PSBC', '绿卡储蓄卡(银联卡)', '绿卡储蓄卡(银联卡)', '62218849', 19, 8, 1004900, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (22, '邮政储蓄银行', 'PSBC', '武警军人保障卡', '武警军人保障卡', '621622', 19, 6, 1009999, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (23, '邮政储蓄银行', 'PSBC', '福农卡', '福农卡', '621599', 19, 6, 1009999, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (24, '邮政储蓄银行', 'PSBC', '普通高中学生资助卡', '普通高中学生资助卡', '621674', 19, 6, 1009999, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (25, '邮政储蓄银行', 'PSBC', '中国旅游卡（普卡）', '中国旅游卡（普卡）', '623218', 19, 6, 1009999, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (26, '邮政储蓄银行', 'PSBC', '中国旅游卡（金卡）', '中国旅游卡（金卡）', '623219', 19, 6, 1009999, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (27, '中国工商银行', 'ICBC', '牡丹运通卡金卡', '牡丹运通卡金卡', '370246', 15, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (28, '中国工商银行', 'ICBC', '牡丹运通卡金卡', '牡丹运通卡金卡', '370248', 15, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (29, '中国工商银行', 'ICBC', '牡丹运通卡金卡', '牡丹运通卡金卡', '370249', 15, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (30, '中国工商银行', 'ICBC', '牡丹VISA卡(单位卡)', '牡丹VISA卡(单位卡)', '427010', 16, 6, 1020000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (31, '中国工商银行', 'ICBC', '牡丹VISA信用卡', '牡丹VISA信用卡', '427018', 16, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (32, '中国工商银行', 'ICBC', '牡丹VISA卡(单位卡)', '牡丹VISA卡(单位卡)', '427019', 16, 6, 1020000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (33, '中国工商银行', 'ICBC', '牡丹VISA信用卡', '牡丹VISA信用卡', '427020', 16, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (34, '中国工商银行', 'ICBC', '牡丹VISA信用卡', '牡丹VISA信用卡', '427029', 16, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (35, '中国工商银行', 'ICBC', '牡丹VISA信用卡', '牡丹VISA信用卡', '427030', 16, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (36, '中国工商银行', 'ICBC', '牡丹VISA信用卡', '牡丹VISA信用卡', '427039', 16, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (37, '中国工商银行', 'ICBC', '牡丹运通卡普通卡', '牡丹运通卡普通卡', '370247', 15, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (38, '中国工商银行', 'ICBC', '牡丹VISA信用卡', '牡丹VISA信用卡', '438125', 16, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (39, '中国工商银行', 'ICBC', '牡丹VISA白金卡', '牡丹VISA白金卡', '438126', 16, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (40, '中国工商银行', 'ICBC', '牡丹贷记卡(银联卡)', '牡丹贷记卡(银联卡)', '451804', 16, 6, 1020000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (41, '中国工商银行', 'ICBC', '牡丹贷记卡(银联卡)', '牡丹贷记卡(银联卡)', '451810', 16, 6, 1020000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (42, '中国工商银行', 'ICBC', '牡丹贷记卡(银联卡)', '牡丹贷记卡(银联卡)', '451811', 16, 6, 1020000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (43, '中国工商银行', 'ICBC', '牡丹信用卡(银联卡)', '牡丹信用卡(银联卡)', '45806', 16, 5, 1020000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (44, '中国工商银行', 'ICBC', '牡丹贷记卡(银联卡)', '牡丹贷记卡(银联卡)', '458071', 16, 6, 1020000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (45, '中国工商银行', 'ICBC', '牡丹欧元卡', '牡丹欧元卡', '489734', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (46, '中国工商银行', 'ICBC', '牡丹欧元卡', '牡丹欧元卡', '489735', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (47, '中国工商银行', 'ICBC', '牡丹欧元卡', '牡丹欧元卡', '489736', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (48, '中国工商银行', 'ICBC', '牡丹万事达国际借记卡', '牡丹万事达国际借记卡', '510529', 16, 6, 1020000, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (49, '中国工商银行', 'ICBC', '牡丹VISA信用卡', '牡丹VISA信用卡', '427062', 16, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (50, '中国工商银行', 'ICBC', '海航信用卡', '海航信用卡', '524091', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (51, '中国工商银行', 'ICBC', '牡丹VISA信用卡', '牡丹VISA信用卡', '427064', 16, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (52, '中国工商银行', 'ICBC', '牡丹万事达信用卡', '牡丹万事达信用卡', '530970', 16, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (53, '中国工商银行', 'ICBC', '牡丹信用卡(银联卡)', '牡丹信用卡(银联卡)', '53098', 16, 5, 1020000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (54, '中国工商银行', 'ICBC', '牡丹万事达信用卡', '牡丹万事达信用卡', '530990', 16, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (55, '中国工商银行', 'ICBC', '牡丹万事达信用卡', '牡丹万事达信用卡', '558360', 16, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (56, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620200', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (57, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620302', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (58, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620402', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (59, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620403', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (60, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620404', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (61, '中国工商银行', 'ICBC', '牡丹万事达白金卡', '牡丹万事达白金卡', '524047', 16, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (62, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620406', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (63, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620407', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (64, '中国工商银行', 'ICBC', '海航信用卡个人普卡', '海航信用卡个人普卡', '525498', 16, 6, 1020000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (65, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620409', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (66, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620410', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (67, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620411', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (68, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620412', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (69, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620502', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (70, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620503', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (71, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620405', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (72, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620408', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (73, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620512', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (74, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620602', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (75, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620604', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (76, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620607', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (77, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620611', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (78, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620612', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (79, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620704', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (80, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620706', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (81, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620707', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (82, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620708', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (83, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620709', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (84, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620710', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (85, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620609', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (86, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620712', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (87, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620713', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (88, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620714', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (89, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620802', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (90, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620711', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (91, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620904', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (92, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620905', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (93, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621001', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (94, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '620902', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (95, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621103', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (96, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621105', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (97, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621106', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (98, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621107', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (99, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621102', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (100, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621203', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (101, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621204', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (102, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621205', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (103, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621206', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (104, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621207', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (105, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621208', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (106, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621209', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (107, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621210', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (108, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621302', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (109, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621303', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (110, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621202', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (111, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621305', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (112, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621306', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (113, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621307', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (114, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621309', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (115, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621311', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (116, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621313', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (117, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621211', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (118, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621315', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (119, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621304', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (120, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621402', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (121, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621404', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (122, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621405', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (123, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621406', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (124, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621407', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (125, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621408', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (126, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621409', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (127, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621410', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (128, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621502', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (129, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621317', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (130, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621511', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (131, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621602', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (132, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621603', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (133, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621604', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (134, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621605', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (135, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621608', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (136, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621609', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (137, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621610', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (138, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621611', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (139, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621612', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (140, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621613', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (141, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621614', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (142, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621615', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (143, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621616', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (144, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621617', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (145, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621607', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (146, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621606', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (147, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621804', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (148, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621807', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (149, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621813', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (150, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621814', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (151, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621817', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (152, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621901', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (153, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621904', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (154, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621905', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (155, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621906', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (156, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621907', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (157, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621908', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (158, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621909', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (159, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621910', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (160, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621911', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (161, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621912', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (162, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621913', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (163, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621915', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (164, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622002', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (165, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621903', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (166, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622004', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (167, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622005', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (168, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622006', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (169, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622007', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (170, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622008', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (171, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622010', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (172, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622011', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (173, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622012', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (174, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '621914', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (175, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622015', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (176, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622016', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (177, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622003', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (178, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622018', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (179, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622019', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (180, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622020', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (181, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622102', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (182, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622103', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (183, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622104', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (184, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622105', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (185, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622013', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (186, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622111', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (187, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622114', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (188, '中国工商银行', 'ICBC', '灵通卡', '灵通卡', '622200', 19, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (189, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622017', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (190, '中国工商银行', 'ICBC', 'E时代卡', 'E时代卡', '622202', 19, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (191, '中国工商银行', 'ICBC', 'E时代卡', 'E时代卡', '622203', 19, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (192, '中国工商银行', 'ICBC', '理财金卡', '理财金卡', '622208', 19, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (193, '中国工商银行', 'ICBC', '准贷记卡(个普)', '准贷记卡(个普)', '622210', 16, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (194, '中国工商银行', 'ICBC', '准贷记卡(个普)', '准贷记卡(个普)', '622211', 16, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (195, '中国工商银行', 'ICBC', '准贷记卡(个普)', '准贷记卡(个普)', '622212', 16, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (196, '中国工商银行', 'ICBC', '准贷记卡(个普)', '准贷记卡(个普)', '622213', 16, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (197, '中国工商银行', 'ICBC', '准贷记卡(个普)', '准贷记卡(个普)', '622214', 16, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (198, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622110', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (199, '中国工商银行', 'ICBC', '准贷记卡(商普)', '准贷记卡(商普)', '622220', 16, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (200, '中国工商银行', 'ICBC', '牡丹卡(商务卡)', '牡丹卡(商务卡)', '622223', 16, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (201, '中国工商银行', 'ICBC', '准贷记卡(商金)', '准贷记卡(商金)', '622225', 16, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (202, '中国工商银行', 'ICBC', '牡丹卡(商务卡)', '牡丹卡(商务卡)', '622229', 16, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (203, '中国工商银行', 'ICBC', '贷记卡(个普)', '贷记卡(个普)', '622230', 16, 6, 1020000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (204, '中国工商银行', 'ICBC', '牡丹卡(个人卡)', '牡丹卡(个人卡)', '622231', 16, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (205, '中国工商银行', 'ICBC', '牡丹卡(个人卡)', '牡丹卡(个人卡)', '622232', 16, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (206, '中国工商银行', 'ICBC', '牡丹卡(个人卡)', '牡丹卡(个人卡)', '622233', 16, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (207, '中国工商银行', 'ICBC', '牡丹卡(个人卡)', '牡丹卡(个人卡)', '622234', 16, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (208, '中国工商银行', 'ICBC', '贷记卡(个金)', '贷记卡(个金)', '622235', 16, 6, 1020000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (209, '中国工商银行', 'ICBC', '牡丹交通卡', '牡丹交通卡', '622237', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (210, '中国工商银行', 'ICBC', '准贷记卡(个金)', '准贷记卡(个金)', '622215', 16, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (211, '中国工商银行', 'ICBC', '牡丹交通卡', '牡丹交通卡', '622239', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (212, '中国工商银行', 'ICBC', '贷记卡(商普)', '贷记卡(商普)', '622240', 16, 6, 1020000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (213, '中国工商银行', 'ICBC', '贷记卡(商金)', '贷记卡(商金)', '622245', 16, 6, 1020000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (214, '中国工商银行', 'ICBC', '牡丹卡(商务卡)', '牡丹卡(商务卡)', '622224', 16, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (215, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622303', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (216, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622304', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (217, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622305', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (218, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622306', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (219, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622307', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (220, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622308', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (221, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622309', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (222, '中国工商银行', 'ICBC', '牡丹交通卡', '牡丹交通卡', '622238', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (223, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622314', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (224, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622315', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (225, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622317', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (226, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622302', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (227, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622402', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (228, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622403', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (229, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622404', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (230, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622313', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (231, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622504', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (232, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622505', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (233, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622509', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (234, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622513', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (235, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622517', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (236, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622502', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (237, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622604', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (238, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622605', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (239, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622606', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (240, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622510', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (241, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622703', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (242, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622715', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (243, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622806', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (244, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622902', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (245, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622903', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (246, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622706', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (247, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '623002', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (248, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '623006', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (249, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '623008', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (250, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '623011', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (251, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '623012', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (252, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '622904', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (253, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '623015', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (254, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '623100', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (255, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '623202', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (256, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '623301', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (257, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '623400', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (258, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '623500', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (259, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '623602', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (260, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '623803', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (261, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '623901', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (262, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '623014', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (263, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '624100', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (264, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '624200', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (265, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '624301', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (266, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '624402', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (267, '中国工商银行', 'ICBC', '牡丹贷记卡', '牡丹贷记卡', '62451804', 16, 8, 1020000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (268, '中国工商银行', 'ICBC', '牡丹贷记卡', '牡丹贷记卡', '62451810', 16, 8, 1020000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (269, '中国工商银行', 'ICBC', '牡丹贷记卡', '牡丹贷记卡', '62451811', 16, 8, 1020000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (270, '中国工商银行', 'ICBC', '牡丹信用卡', '牡丹信用卡', '6245806', 16, 7, 1020000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (271, '中国工商银行', 'ICBC', '牡丹贷记卡', '牡丹贷记卡', '62458071', 16, 8, 1020000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (272, '中国工商银行', 'ICBC', '牡丹信用卡', '牡丹信用卡', '6253098', 16, 7, 1020000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (273, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '623700', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (274, '中国工商银行', 'ICBC', '中央预算单位公务卡', '中央预算单位公务卡', '628288', 16, 6, 1020000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (275, '中国工商银行', 'ICBC', '牡丹灵通卡', '牡丹灵通卡', '624000', 18, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (276, '中国工商银行', 'ICBC', '牡丹灵通卡(银联卡)', '牡丹灵通卡(银联卡)', '9558', 19, 4, 1020000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (277, '中国工商银行', 'ICBC', '财政预算单位公务卡', '财政预算单位公务卡', '628286', 16, 6, 1020000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (278, '中国工商银行', 'ICBC', '牡丹卡白金卡', '牡丹卡白金卡', '622206', 16, 6, 1020000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (279, '中国工商银行', 'ICBC', '牡丹卡普卡', '牡丹卡普卡', '621225', 19, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (280, '中国工商银行', 'ICBC', '国航知音牡丹信用卡', '国航知音牡丹信用卡', '526836', 16, 6, 1020000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (281, '中国工商银行', 'ICBC', '国航知音牡丹信用卡', '国航知音牡丹信用卡', '513685', 16, 6, 1020000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (282, '中国工商银行', 'ICBC', '国航知音牡丹信用卡', '国航知音牡丹信用卡', '543098', 16, 6, 1020000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (283, '中国工商银行', 'ICBC', '国航知音牡丹信用卡', '国航知音牡丹信用卡', '458441', 16, 6, 1020000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (284, '中国工商银行', 'ICBC', '银联标准卡', '银联标准卡', '620058', 19, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (285, '中国工商银行', 'ICBC', '中职学生资助卡', '中职学生资助卡', '621281', 19, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (286, '中国工商银行', 'ICBC', '专用信用消费卡', '专用信用消费卡', '622246', 16, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (287, '中国工商银行', 'ICBC', '牡丹社会保障卡', '牡丹社会保障卡', '900000', 19, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (288, '中国工商银行', 'ICBC', '牡丹东航联名卡', '牡丹东航联名卡', '544210', 16, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (289, '中国工商银行', 'ICBC', '牡丹东航联名卡', '牡丹东航联名卡', '548943', 16, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (290, '中国工商银行', 'ICBC', '牡丹运通白金卡', '牡丹运通白金卡', '370267', 15, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (291, '中国工商银行', 'ICBC', '福农灵通卡', '福农灵通卡', '621558', 19, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (292, '中国工商银行', 'ICBC', '福农灵通卡', '福农灵通卡', '621559', 19, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (293, '中国工商银行', 'ICBC', '灵通卡', '灵通卡', '621722', 19, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (294, '中国工商银行', 'ICBC', '灵通卡', '灵通卡', '621723', 19, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (295, '中国工商银行', 'ICBC', '中国旅行卡', '中国旅行卡', '620086', 19, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (296, '中国工商银行', 'ICBC', '牡丹卡普卡', '牡丹卡普卡', '621226', 19, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (297, '中国工商银行', 'ICBC', '国际借记卡', '国际借记卡', '402791', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (298, '中国工商银行', 'ICBC', '国际借记卡', '国际借记卡', '427028', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (299, '中国工商银行', 'ICBC', '国际借记卡', '国际借记卡', '427038', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (300, '中国工商银行', 'ICBC', '国际借记卡', '国际借记卡', '548259', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (301, '中国工商银行', 'ICBC', '牡丹JCB信用卡', '牡丹JCB信用卡', '356879', 16, 6, 1020000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (302, '中国工商银行', 'ICBC', '牡丹JCB信用卡', '牡丹JCB信用卡', '356880', 16, 6, 1020000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (303, '中国工商银行', 'ICBC', '牡丹JCB信用卡', '牡丹JCB信用卡', '356881', 16, 6, 1020000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (304, '中国工商银行', 'ICBC', '牡丹JCB信用卡', '牡丹JCB信用卡', '356882', 16, 6, 1020000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (305, '中国工商银行', 'ICBC', '牡丹多币种卡', '牡丹多币种卡', '528856', 16, 6, 1020000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (306, '中国工商银行', 'ICBC', '武警军人保障卡', '武警军人保障卡', '621618', 19, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (307, '中国工商银行', 'ICBC', '预付芯片卡', '预付芯片卡', '620516', 19, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (308, '中国工商银行', 'ICBC', '理财金账户金卡', '理财金账户金卡', '621227', 19, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (309, '中国工商银行', 'ICBC', '工银财富卡', '工银财富卡', '621288', 19, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (310, '中国工商银行', 'ICBC', '灵通卡', '灵通卡', '621721', 19, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (311, '中国工商银行', 'ICBC', '牡丹宁波市民卡', '牡丹宁波市民卡', '900010', 19, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (312, '中国工商银行', 'ICBC', '中国旅游卡', '中国旅游卡', '625330', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (313, '中国工商银行', 'ICBC', '中国旅游卡', '中国旅游卡', '625331', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (314, '中国工商银行', 'ICBC', '中国旅游卡', '中国旅游卡', '625332', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (315, '中国工商银行', 'ICBC', '借记卡', '借记卡', '623062', 19, 6, 1020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (316, '中国工商银行', 'ICBC', '借贷合一卡', '借贷合一卡', '622236', 16, 6, 1020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (317, '中国工商银行', 'ICBC', '牡丹百夫长信用卡', '牡丹百夫长信用卡', '374738', 15, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (318, '中国工商银行', 'ICBC', '牡丹百夫长信用卡', '牡丹百夫长信用卡', '374739', 15, 6, 1020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (319, '中国工商银行', 'ICBC', '普通高中学生资助卡', '普通高中学生资助卡', '621670', 19, 6, 1020000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (320, '中国工商银行', 'ICBC', '牡丹多币种卡', '牡丹多币种卡', '524374', 16, 6, 1020000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (321, '中国工商银行', 'ICBC', '牡丹多币种卡', '牡丹多币种卡', '550213', 16, 6, 1020000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (322, '中国工商银行布鲁塞尔分行', 'ICBC', '贷记卡', '贷记卡', '625929', 16, 6, 1020056, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (323, '中国工商银行布鲁塞尔分行', 'ICBC', '借记卡', '借记卡', '621376', 16, 6, 1020056, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (324, '中国工商银行布鲁塞尔分行', 'ICBC', '预付卡', '预付卡', '620054', 16, 6, 1020056, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (325, '中国工商银行布鲁塞尔分行', 'ICBC', '预付卡', '预付卡', '620142', 16, 6, 1020056, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (326, '中国工商银行（巴西）', 'ICBC', '借记卡', '借记卡', '621423', 16, 6, 1020076, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (327, '中国工商银行（巴西）', 'ICBC', '贷记卡', '贷记卡', '625927', 16, 6, 1020076, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (328, '中国工商银行金边分行', 'ICBC', '借记卡', '借记卡', '621428', 16, 6, 1020116, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (329, '中国工商银行金边分行', 'ICBC', '信用卡', '信用卡', '625939', 16, 6, 1020116, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (330, '中国工商银行金边分行', 'ICBC', '借记卡', '借记卡', '621434', 16, 6, 1020116, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (331, '中国工商银行金边分行', 'ICBC', '信用卡', '信用卡', '625987', 16, 6, 1020116, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (332, '中国工商银行加拿大分行', 'ICBC', '贷记卡', '贷记卡', '625930', 16, 6, 1020124, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (333, '中国工商银行加拿大分行', 'ICBC', '借记卡', '借记卡', '621761', 16, 6, 1020124, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (334, '中国工商银行加拿大分行', 'ICBC', '借记卡', '借记卡', '621749', 16, 6, 1020124, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (335, '中国工商银行加拿大分行', 'ICBC', '预付卡', '预付卡', '620184', 16, 6, 1020124, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (336, '中国工商银行巴黎分行', 'ICBC', '借记卡', '借记卡', '621300', 16, 6, 1020250, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (337, '中国工商银行巴黎分行', 'ICBC', '借记卡', '借记卡', '621378', 16, 6, 1020250, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (338, '中国工商银行巴黎分行', 'ICBC', '贷记卡', '贷记卡', '625114', 16, 6, 1020250, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (339, '中国工商银行法兰克福分行', 'ICBC', '贷记卡', '贷记卡', '622159', 16, 6, 1020276, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (340, '中国工商银行法兰克福分行', 'ICBC', '借记卡', '借记卡', '621720', 19, 6, 1020276, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (341, '中国工商银行法兰克福分行', 'ICBC', '贷记卡', '贷记卡', '625021', 16, 6, 1020276, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (342, '中国工商银行法兰克福分行', 'ICBC', '贷记卡', '贷记卡', '625022', 16, 6, 1020276, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (343, '中国工商银行法兰克福分行', 'ICBC', '贷记卡', '贷记卡', '625932', 16, 6, 1020276, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (344, '中国工商银行法兰克福分行', 'ICBC', '借记卡', '借记卡', '621379', 19, 6, 1020276, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (345, '中国工商银行法兰克福分行', 'ICBC', '预付卡', '预付卡', '620114', 19, 6, 1020276, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (346, '中国工商银行法兰克福分行', 'ICBC', '预付卡', '预付卡', '620146', 19, 6, 1020276, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (347, '中国工商银行(亚洲)', 'ICBC', 'ICBC(Asia) Credit', 'ICBC(Asia) Credit', '622889', 16, 6, 1020344, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (348, '中国工商银行(亚洲)', 'ICBC', 'ICBC Credit Card', 'ICBC Credit Card', '625900', 16, 6, 1020344, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (349, '中国工商银行(亚洲)', 'ICBC', 'CNYEasylinkCard', 'CNYEasylinkCard', '622944', 16, 6, 1020344, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (350, '中国工商银行(亚洲)', 'ICBC', 'EliteClubATMCard', 'EliteClubATMCard', '622949', 16, 6, 1020344, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (351, '中国工商银行(亚洲)', 'ICBC', '港币信用卡', '港币信用卡', '625915', 16, 6, 1020344, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (352, '中国工商银行(亚洲)', 'ICBC', '港币信用卡', '港币信用卡', '625916', 16, 6, 1020344, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (353, '中国工商银行(亚洲)', 'ICBC', '工银亚洲预付卡', '工银亚洲预付卡', '620030', 16, 6, 1020344, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (354, '中国工商银行(亚洲)', 'ICBC', '预付卡', '预付卡', '620050', 16, 6, 1020344, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (355, '中国工商银行印尼分行', 'ICBC', '印尼盾复合卡', '印尼盾复合卡', '622171', 16, 6, 1020360, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (356, '中国工商银行印尼分行', 'ICBC', '借记卡', '借记卡', '621240', 19, 6, 1020360, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (357, '中国工商银行印尼分行', 'ICBC', '借记卡', '借记卡', '621724', 19, 6, 1020360, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (358, '中国工商银行印尼分行', 'ICBC', '贷记卡', '贷记卡', '625931', 16, 6, 1020360, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (359, '中国工商银行（印度尼西亚）', 'ICBC', '借记卡', '借记卡', '621762', 19, 6, 1020360, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (360, '中国工商银行米兰分行', 'ICBC', '贷记卡', '贷记卡', '625113', 16, 6, 1020380, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (361, '中国工商银行米兰分行', 'ICBC', '借记卡', '借记卡', '621371', 16, 6, 1020380, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (362, '中国工商银行米兰分行', 'ICBC', '预付卡', '预付卡', '620143', 16, 6, 1020380, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (363, '中国工商银行米兰分行', 'ICBC', '预付卡', '预付卡', '620149', 16, 6, 1020380, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (364, '工行东京分行', 'ICBC', '工行东京借记卡', '工行东京借记卡', '621730', 16, 6, 1020392, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (365, '中国工商银行阿拉木图子行', 'ICBC', '贷记卡', '贷记卡', '625928', 16, 6, 1020398, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (366, '中国工商银行阿拉木图子行', 'ICBC', '借记卡', '借记卡', '621414', 19, 6, 1020398, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (367, '中国工商银行阿拉木图子行', 'ICBC', '贷记卡', '贷记卡', '625914', 16, 6, 1020398, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (368, '中国工商银行阿拉木图子行', 'ICBC', '借记卡', '借记卡', '621375', 19, 6, 1020398, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (369, '中国工商银行阿拉木图子行', 'ICBC', '预付卡', '预付卡', '620187', 19, 6, 1020398, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (370, '工行首尔', 'ICBC', '借记卡', '借记卡', '621734', 16, 6, 1020410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (371, '中国工商银行万象分行', 'ICBC', '借记卡', '借记卡', '621433', 16, 6, 1020418, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (372, '中国工商银行万象分行', 'ICBC', '贷记卡', '贷记卡', '625986', 16, 6, 1020418, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (373, '中国工商银行卢森堡分行', 'ICBC', '借记卡', '借记卡', '621370', 16, 6, 1020442, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (374, '中国工商银行卢森堡分行', 'ICBC', '贷记卡', '贷记卡', '625925', 16, 6, 1020442, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (375, '中国工商银行澳门分行', 'ICBC', 'E时代卡', 'E时代卡', '622926', 19, 6, 1020446, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (376, '中国工商银行澳门分行', 'ICBC', 'E时代卡', 'E时代卡', '622927', 19, 6, 1020446, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (377, '中国工商银行澳门分行', 'ICBC', 'E时代卡', 'E时代卡', '622928', 19, 6, 1020446, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (378, '中国工商银行澳门分行', 'ICBC', '理财金账户', '理财金账户', '622929', 19, 6, 1020446, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (379, '中国工商银行澳门分行', 'ICBC', '理财金账户', '理财金账户', '622930', 19, 6, 1020446, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (380, '中国工商银行澳门分行', 'ICBC', '理财金账户', '理财金账户', '622931', 19, 6, 1020446, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (381, '中国工商银行澳门分行', 'ICBC', '借记卡', '借记卡', '621733', 19, 6, 1020446, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (382, '中国工商银行澳门分行', 'ICBC', '借记卡', '借记卡', '621732', 19, 6, 1020446, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (383, '中国工商银行澳门分行', 'ICBC', '预付卡', '预付卡', '620124', 16, 6, 1020446, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (384, '中国工商银行澳门分行', 'ICBC', '预付卡', '预付卡', '620183', 16, 6, 1020446, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (385, '工行马来西亚', 'ICBC', '贷记卡', '贷记卡', '625921', 16, 6, 1020458, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (386, '工行马来西亚', 'ICBC', '借记卡', '借记卡', '621764', 16, 6, 1020458, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (387, '中国工商银行阿姆斯特丹', 'ICBC', '贷记卡', '贷记卡', '625926', 16, 6, 1020528, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (388, '中国工商银行阿姆斯特丹', 'ICBC', '借记卡', '借记卡', '621372', 19, 6, 1020528, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (389, '中国工商银行卡拉奇分行', 'ICBC', '借记卡', '借记卡', '621464', 16, 6, 1020586, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (390, '中国工商银行卡拉奇分行', 'ICBC', '贷记卡', '贷记卡', '625942', 16, 6, 1020586, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (391, '中国工商银行新加坡分行', 'ICBC', '贷记卡', '贷记卡', '622158', 16, 6, 1020702, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (392, '中国工商银行新加坡分行', 'ICBC', '贷记卡', '贷记卡', '625917', 16, 6, 1020702, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (393, '中国工商银行新加坡分行', 'ICBC', '借记卡', '借记卡', '621765', 16, 6, 1020702, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (394, '中国工商银行新加坡分行', 'ICBC', '预付卡', '预付卡', '620094', 16, 6, 1020702, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (395, '中国工商银行新加坡分行', 'ICBC', '预付卡', '预付卡', '620186', 16, 6, 1020702, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (396, '工行河内', 'ICBC', '贷记卡', '贷记卡', '625922', 16, 6, 1020704, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (397, '工银河内', 'ICBC', '借记卡', '借记卡', '621369', 19, 6, 1020704, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (398, '工银河内', 'ICBC', '工银越南盾借记卡', '工银越南盾借记卡', '621763', 19, 6, 1020704, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (399, '工银河内', 'ICBC', '工银越南盾信用卡', '工银越南盾信用卡', '625934', 16, 6, 1020704, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (400, '工银河内', 'ICBC', '预付卡', '预付卡', '620046', 19, 6, 1020704, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (401, '中国工商银行马德里分行', 'ICBC', '借记卡', '借记卡', '621750', 16, 6, 1020724, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (402, '中国工商银行马德里分行', 'ICBC', '贷记卡', '贷记卡', '625933', 16, 6, 1020724, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (403, '中国工商银行马德里分行', 'ICBC', '借记卡', '借记卡', '621377', 16, 6, 1020724, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (404, '中国工商银行马德里分行', 'ICBC', '预付卡', '预付卡', '620148', 16, 6, 1020724, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (405, '中国工商银行马德里分行', 'ICBC', '预付卡', '预付卡', '620185', 16, 6, 1020724, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (406, '工银泰国', 'ICBC', '贷记卡', '贷记卡', '625920', 16, 6, 1020764, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (407, '工银泰国', 'ICBC', '借记卡', '借记卡', '621367', 16, 6, 1020764, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (408, '中国工商银行伦敦子行', 'ICBC', '贷记卡', '贷记卡', '625924', 16, 6, 1020826, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (409, '中国工商银行伦敦子行', 'ICBC', '借记卡', '借记卡', '621374', 16, 6, 1020826, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (410, '中国工商银行伦敦子行', 'ICBC', '工银伦敦借记卡', '工银伦敦借记卡', '621731', 16, 6, 1020826, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (411, '中国工商银行伦敦子行', 'ICBC', '借记卡', '借记卡', '621781', 16, 6, 1020826, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (412, '农业银行', 'ABC', '金穗借记卡', '金穗借记卡', '103', 19, 3, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (413, '农业银行', 'ABC', '金穗贷记卡(银联卡)', '金穗贷记卡(银联卡)', '403361', 16, 6, 1030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (414, '农业银行', 'ABC', '金穗贷记卡(银联卡)', '金穗贷记卡(银联卡)', '404117', 16, 6, 1030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (415, '农业银行', 'ABC', '金穗贷记卡(银联卡)', '金穗贷记卡(银联卡)', '404118', 16, 6, 1030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (416, '农业银行', 'ABC', '金穗贷记卡(银联卡)', '金穗贷记卡(银联卡)', '404119', 16, 6, 1030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (417, '农业银行', 'ABC', '金穗贷记卡(银联卡)', '金穗贷记卡(银联卡)', '404120', 16, 6, 1030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (418, '农业银行', 'ABC', '金穗贷记卡(银联卡)', '金穗贷记卡(银联卡)', '404121', 16, 6, 1030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (419, '农业银行', 'ABC', 'VISA白金卡', 'VISA白金卡', '463758', 16, 6, 1030000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (420, '农业银行', 'ABC', '金穗贷记卡(银联卡)', '金穗贷记卡(银联卡)', '519412', 16, 6, 1030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (421, '农业银行', 'ABC', '金穗贷记卡(银联卡)', '金穗贷记卡(银联卡)', '519413', 16, 6, 1030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (422, '农业银行', 'ABC', '金穗贷记卡(银联卡)', '金穗贷记卡(银联卡)', '520082', 16, 6, 1030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (423, '农业银行', 'ABC', '金穗贷记卡(银联卡)', '金穗贷记卡(银联卡)', '520083', 16, 6, 1030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (424, '农业银行', 'ABC', '金穗信用卡(银联卡)', '金穗信用卡(银联卡)', '53591', 16, 5, 1030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (425, '农业银行', 'ABC', '金穗贷记卡', '金穗贷记卡', '552599', 16, 6, 1030000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (426, '农业银行', 'ABC', '金穗贷记卡', '金穗贷记卡', '558730', 16, 6, 1030000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (427, '农业银行', 'ABC', '金穗信用卡(银联卡)', '金穗信用卡(银联卡)', '49102', 16, 5, 1030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (428, '农业银行', 'ABC', '万事达白金卡', '万事达白金卡', '514027', 16, 6, 1030000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (429, '农业银行', 'ABC', '金穗贷记卡', '金穗贷记卡', '622836', 16, 6, 1030000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (430, '农业银行', 'ABC', '金穗贷记卡', '金穗贷记卡', '622837', 16, 6, 1030000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (431, '农业银行', 'ABC', '金穗惠农卡', '金穗惠农卡', '622841', 19, 6, 1030000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (432, '农业银行', 'ABC', '金穗旅游卡(银联卡)', '金穗旅游卡(银联卡)', '622824', 19, 6, 1030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (433, '农业银行', 'ABC', '复合介质金穗通宝卡', '复合介质金穗通宝卡', '622826', 19, 6, 1030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (434, '农业银行', 'ABC', '金穗公务卡', '金穗公务卡', '628268', 16, 6, 1030000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (435, '农业银行', 'ABC', '金穗信用卡', '金穗信用卡', '6349102', 16, 7, 1030000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (436, '农业银行', 'ABC', '金穗信用卡', '金穗信用卡', '6353591', 16, 7, 1030000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (437, '农业银行', 'ABC', '借记卡(银联卡)', '借记卡(银联卡)', '95595', 19, 5, 1030000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (438, '农业银行', 'ABC', '借记卡(银联卡)', '借记卡(银联卡)', '95596', 19, 5, 1030000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (439, '农业银行', 'ABC', '借记卡(银联卡)', '借记卡(银联卡)', '95597', 19, 5, 1030000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (440, '农业银行', 'ABC', '借记卡(银联卡)', '借记卡(银联卡)', '95598', 19, 5, 1030000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (441, '农业银行', 'ABC', '借记卡(银联卡)', '借记卡(银联卡)', '95599', 19, 5, 1030000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (442, '农业银行', 'ABC', '金穗通宝卡(银联卡)', '金穗通宝卡(银联卡)', '622848', 19, 6, 1030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (443, '农业银行', 'ABC', '银联标准卡', '银联标准卡', '620059', 19, 6, 1030000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (444, '农业银行', 'ABC', '中职学生资助卡', '中职学生资助卡', '621282', 19, 6, 1030000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (445, '农业银行', 'ABC', '退役金卡', '退役金卡', '622828', 19, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (446, '农业银行', 'ABC', '银联IC卡金卡', '银联IC卡金卡', '625996', 16, 6, 1030000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (447, '农业银行', 'ABC', '银联IC卡白金卡', '银联IC卡白金卡', '625998', 16, 6, 1030000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (448, '农业银行', 'ABC', '金穗社保卡(银联卡)', '金穗社保卡(银联卡)', '622823', 19, 6, 1030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (449, '农业银行', 'ABC', '专用惠农卡', '专用惠农卡', '621336', 19, 6, 1030000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (450, '农业银行', 'ABC', '武警军人保障卡', '武警军人保障卡', '621619', 19, 6, 1030000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (451, '农业银行', 'ABC', '金穗校园卡(银联卡)', '金穗校园卡(银联卡)', '622821', 19, 6, 1030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (452, '农业银行', 'ABC', '金穗星座卡(银联卡)', '金穗星座卡(银联卡)', '622822', 19, 6, 1030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (453, '农业银行', 'ABC', '金穗青年卡(银联卡)', '金穗青年卡(银联卡)', '622825', 19, 6, 1030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (454, '农业银行', 'ABC', '金穗海通卡', '金穗海通卡', '622827', 19, 6, 1030000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (455, '农业银行', 'ABC', '金穗通宝卡(银联卡)', '金穗通宝卡(银联卡)', '622845', 19, 6, 1030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (456, '农业银行', 'ABC', '金穗通宝钻石卡', '金穗通宝钻石卡', '622849', 19, 6, 1030000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (457, '农业银行', 'ABC', '掌尚钱包', '掌尚钱包', '623018', 19, 6, 1030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (458, '农业银行', 'ABC', '银联预算单位公务卡金卡', '银联预算单位公务卡金卡', '625997', 16, 6, 1030000, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (459, '农业银行', 'ABC', '中国旅游卡', '中国旅游卡', '623206', 19, 6, 1030000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (460, '农业银行', 'ABC', '普通高中学生资助卡', '普通高中学生资助卡', '621671', 19, 6, 1030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (461, '农业银行', 'ABC', '金穗通宝卡(银联卡)', '金穗通宝卡(银联卡)', '622840', 19, 6, 1030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (462, '农业银行', 'ABC', '金穗通宝银卡', '金穗通宝银卡', '622843', 19, 6, 1030000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (463, '农业银行', 'ABC', '金穗通宝卡(银联卡)', '金穗通宝卡(银联卡)', '622844', 19, 6, 1030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (464, '农业银行', 'ABC', '金穗通宝卡', '金穗通宝卡', '622846', 19, 6, 1030000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (465, '农业银行', 'ABC', '金穗通宝卡(银联卡)', '金穗通宝卡(银联卡)', '622847', 19, 6, 1030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (466, '农业银行', 'ABC', '金穗通商卡', '金穗通商卡', '622820', 16, 6, 1030001, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (467, '农业银行', 'ABC', '金穗通商卡', '金穗通商卡', '622830', 16, 6, 1030001, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (468, '农业银行', 'ABC', '银联白金卡', '银联白金卡', '622838', 16, 6, 1030001, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (469, '农业银行', 'ABC', '中国旅游卡', '中国旅游卡', '625336', 16, 6, 1030001, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (470, '农业银行', 'ABC', 'IC普卡', 'IC普卡', '625826', 16, 6, 1030001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (471, '农业银行', 'ABC', 'IC金卡', 'IC金卡', '625827', 16, 6, 1030001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (472, '农业银行', 'ABC', '欧元卡', '欧元卡', '544243', 16, 6, 1030001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (473, '农业银行', 'ABC', '澳元卡', '澳元卡', '548478', 16, 6, 1030001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (474, '农业银行', 'ABC', '银联IC公务卡', '银联IC公务卡', '628269', 16, 6, 1030001, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (475, '宁波市农业银行', '(NULL)', '市民卡B卡', '市民卡B卡', '620501', 19, 6, 1033320, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (476, '中国银行', 'BOC', '联名卡', '联名卡', '621660', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (477, '中国银行', 'BOC', '个人普卡', '个人普卡', '621661', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (478, '中国银行', 'BOC', '个人金卡', '个人金卡', '621662', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (479, '中国银行', 'BOC', '员工普卡', '员工普卡', '621663', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (480, '中国银行', 'BOC', '员工金卡', '员工金卡', '621665', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (481, '中国银行', 'BOC', '理财普卡', '理财普卡', '621667', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (482, '中国银行', 'BOC', '理财金卡', '理财金卡', '621668', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (483, '中国银行', 'BOC', '理财银卡', '理财银卡', '621669', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (484, '中国银行', 'BOC', '理财白金卡', '理财白金卡', '621666', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (485, '中国银行', 'BOC', '中行金融IC卡白金卡', '中行金融IC卡白金卡', '625908', 16, 6, 1040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (486, '中国银行', 'BOC', '中行金融IC卡普卡', '中行金融IC卡普卡', '625910', 16, 6, 1040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (487, '中国银行', 'BOC', '中行金融IC卡金卡', '中行金融IC卡金卡', '625909', 16, 6, 1040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (488, '中国银行', 'BOC', '中银JCB卡金卡', '中银JCB卡金卡', '356833', 16, 6, 1040000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (489, '中国银行', 'BOC', '中银JCB卡普卡', '中银JCB卡普卡', '356835', 16, 6, 1040000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (490, '中国银行', 'BOC', '员工普卡', '员工普卡', '409665', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (491, '中国银行', 'BOC', '个人普卡', '个人普卡', '409666', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (492, '中国银行', 'BOC', '中银威士信用卡员', '中银威士信用卡员', '409668', 16, 6, 1040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (493, '中国银行', 'BOC', '中银威士信用卡员', '中银威士信用卡员', '409669', 16, 6, 1040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (494, '中国银行', 'BOC', '个人白金卡', '个人白金卡', '409670', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (495, '中国银行', 'BOC', '中银威士信用卡', '中银威士信用卡', '409671', 16, 6, 1040000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (496, '中国银行', 'BOC', '长城公务卡', '长城公务卡', '409672', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (497, '中国银行', 'BOC', '长城电子借记卡', '长城电子借记卡', '456351', 19, 6, 1040000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (498, '中国银行', 'BOC', '中银万事达信用卡', '中银万事达信用卡', '512315', 16, 6, 1040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (499, '中国银行', 'BOC', '中银万事达信用卡', '中银万事达信用卡', '512316', 16, 6, 1040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (500, '中国银行', 'BOC', '中银万事达信用卡', '中银万事达信用卡', '512411', 16, 6, 1040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (501, '中国银行', 'BOC', '中银万事达信用卡', '中银万事达信用卡', '512412', 16, 6, 1040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (502, '中国银行', 'BOC', '中银万事达信用卡', '中银万事达信用卡', '514957', 16, 6, 1040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (503, '中国银行', 'BOC', '中银威士信用卡员', '中银威士信用卡员', '409667', 16, 6, 1040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (504, '中国银行', 'BOC', '长城万事达信用卡', '长城万事达信用卡', '518378', 16, 6, 1040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (505, '中国银行', 'BOC', '长城万事达信用卡', '长城万事达信用卡', '518379', 16, 6, 1040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (506, '中国银行', 'BOC', '长城万事达信用卡', '长城万事达信用卡', '518474', 16, 6, 1040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (507, '中国银行', 'BOC', '长城万事达信用卡', '长城万事达信用卡', '518475', 16, 6, 1040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (508, '中国银行', 'BOC', '长城万事达信用卡', '长城万事达信用卡', '518476', 16, 6, 1040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (509, '中国银行', 'BOC', '中银奥运信用卡', '中银奥运信用卡', '438088', 16, 6, 1040000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (510, '中国银行', 'BOC', '长城信用卡', '长城信用卡', '524865', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (511, '中国银行', 'BOC', '长城信用卡', '长城信用卡', '525745', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (512, '中国银行', 'BOC', '长城信用卡', '长城信用卡', '525746', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (513, '中国银行', 'BOC', '长城万事达信用卡', '长城万事达信用卡', '547766', 16, 6, 1040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (514, '中国银行', 'BOC', '长城公务卡', '长城公务卡', '552742', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (515, '中国银行', 'BOC', '长城公务卡', '长城公务卡', '553131', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (516, '中国银行', 'BOC', '中银万事达信用卡', '中银万事达信用卡', '558868', 16, 6, 1040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (517, '中国银行', 'BOC', '中银万事达信用卡', '中银万事达信用卡', '514958', 16, 6, 1040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (518, '中国银行', 'BOC', '长城人民币信用卡', '长城人民币信用卡', '622752', 16, 6, 1040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (519, '中国银行', 'BOC', '长城人民币信用卡', '长城人民币信用卡', '622753', 16, 6, 1040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (520, '中国银行', 'BOC', '长城人民币信用卡', '长城人民币信用卡', '622755', 16, 6, 1040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (521, '中国银行', 'BOC', '长城信用卡', '长城信用卡', '524864', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (522, '中国银行', 'BOC', '长城人民币信用卡', '长城人民币信用卡', '622757', 16, 6, 1040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (523, '中国银行', 'BOC', '长城人民币信用卡', '长城人民币信用卡', '622758', 16, 6, 1040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (524, '中国银行', 'BOC', '长城信用卡', '长城信用卡', '622759', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (525, '中国银行', 'BOC', '银联单币贷记卡', '银联单币贷记卡', '622760', 16, 6, 1040000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (526, '中国银行', 'BOC', '长城信用卡', '长城信用卡', '622761', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (527, '中国银行', 'BOC', '长城信用卡', '长城信用卡', '622762', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (528, '中国银行', 'BOC', '长城信用卡', '长城信用卡', '622763', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (529, '中国银行', 'BOC', '长城电子借记卡', '长城电子借记卡', '601382', 19, 6, 1040000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (530, '中国银行', 'BOC', '长城人民币信用卡', '长城人民币信用卡', '622756', 16, 6, 1040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (531, '中国银行', 'BOC', '银联标准公务卡', '银联标准公务卡', '628388', 16, 6, 1040000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (532, '中国银行', 'BOC', '一卡双账户普卡', '一卡双账户普卡', '621256', 19, 6, 1040000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (533, '中国银行', 'BOC', '财互通卡', '财互通卡', '621212', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (534, '中国银行', 'BOC', '电子现金卡', '电子现金卡', '620514', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (535, '中国银行', 'BOC', '长城人民币信用卡', '长城人民币信用卡', '622754', 16, 6, 1040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (536, '中国银行', 'BOC', '长城单位信用卡普卡', '长城单位信用卡普卡', '622764', 16, 6, 1040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (537, '中国银行', 'BOC', '中银女性主题信用卡', '中银女性主题信用卡', '518377', 16, 6, 1040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (538, '中国银行', 'BOC', '长城单位信用卡金卡', '长城单位信用卡金卡', '622765', 16, 6, 1040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (539, '中国银行', 'BOC', '白金卡', '白金卡', '622788', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (540, '中国银行', 'BOC', '中职学生资助卡', '中职学生资助卡', '621283', 19, 6, 1040000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (541, '中国银行', 'BOC', '银联标准卡', '银联标准卡', '620061', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (542, '中国银行', 'BOC', '金融IC卡', '金融IC卡', '621725', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (543, '中国银行', 'BOC', '长城社会保障卡', '长城社会保障卡', '620040', 19, 6, 1040000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (544, '中国银行', 'BOC', '世界卡', '世界卡', '558869', 16, 6, 1040000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (545, '中国银行', 'BOC', '社保联名卡', '社保联名卡', '621330', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (546, '中国银行', 'BOC', '社保联名卡', '社保联名卡', '621331', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (547, '中国银行', 'BOC', '医保联名卡', '医保联名卡', '621332', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (548, '中国银行', 'BOC', '医保联名卡', '医保联名卡', '621333', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (549, '中国银行', 'BOC', '公司借记卡', '公司借记卡', '621297', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (550, '中国银行', 'BOC', '银联美运顶级卡', '银联美运顶级卡', '377677', 15, 6, 1040000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (551, '中国银行', 'BOC', '长城福农借记卡金卡', '长城福农借记卡金卡', '621568', 19, 6, 1040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (552, '中国银行', 'BOC', '长城福农借记卡普卡', '长城福农借记卡普卡', '621569', 19, 6, 1040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (553, '中国银行', 'BOC', '中行金融IC卡普卡', '中行金融IC卡普卡', '625905', 16, 6, 1040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (554, '中国银行', 'BOC', '中行金融IC卡金卡', '中行金融IC卡金卡', '625906', 16, 6, 1040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (555, '中国银行', 'BOC', '中行金融IC卡白金卡', '中行金融IC卡白金卡', '625907', 16, 6, 1040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (556, '中国银行', 'BOC', '长城银联公务IC卡白金卡', '长城银联公务IC卡白金卡', '628313', 16, 6, 1040000, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (557, '中国银行', 'BOC', '中银旅游信用卡', '中银旅游信用卡', '625333', 16, 6, 1040000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (558, '中国银行', 'BOC', '普通高中学生资助卡', '普通高中学生资助卡', '621672', 19, 6, 1040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (559, '中国银行', 'BOC', '长城银联公务IC卡金卡', '长城银联公务IC卡金卡', '628312', 16, 6, 1040000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (560, '中国银行', 'BOC', '中国旅游卡', '中国旅游卡', '623208', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (561, '中国银行', 'BOC', '武警军人保障卡', '武警军人保障卡', '621620', 19, 6, 1040000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (562, '中国银行', 'BOC', '社保联名借记IC卡', '社保联名借记IC卡', '621756', 19, 6, 1040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (563, '中国银行', 'BOC', '社保联名借记IC卡', '社保联名借记IC卡', '621757', 19, 6, 1040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (564, '中国银行', 'BOC', '医保联名借记IC卡', '医保联名借记IC卡', '621758', 19, 6, 1040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (565, '中国银行', 'BOC', '医保联名借记IC卡', '医保联名借记IC卡', '621759', 19, 6, 1040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (566, '中国银行', 'BOC', '借记IC个人普卡', '借记IC个人普卡', '621785', 19, 6, 1040000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (567, '中国银行', 'BOC', '借记IC个人金卡', '借记IC个人金卡', '621786', 19, 6, 1040000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (568, '中国银行', 'BOC', '借记IC个人普卡', '借记IC个人普卡', '621787', 19, 6, 1040000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (569, '中国银行', 'BOC', '借记IC白金卡', '借记IC白金卡', '621788', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (570, '中国银行', 'BOC', '借记IC钻石卡', '借记IC钻石卡', '621789', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (571, '中国银行', 'BOC', '借记IC联名卡', '借记IC联名卡', '621790', 19, 6, 1040000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (572, '中国银行（澳大利亚）', 'BOC', '预付卡', '预付卡', '620025', 16, 6, 1040036, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (573, '中国银行（澳大利亚）', 'BOC', '预付卡', '预付卡', '620026', 16, 6, 1040036, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (574, '中国银行（澳大利亚）', 'BOC', '借记卡', '借记卡', '621293', 16, 6, 1040036, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (575, '中国银行（澳大利亚）', 'BOC', '借记卡', '借记卡', '621294', 16, 6, 1040036, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (576, '中国银行（澳大利亚）', 'BOC', '借记卡', '借记卡', '621342', 16, 6, 1040036, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (577, '中国银行（澳大利亚）', 'BOC', '借记卡', '借记卡', '621343', 16, 6, 1040036, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (578, '中国银行（澳大利亚）', 'BOC', '借记卡', '借记卡', '621364', 16, 6, 1040036, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (579, '中国银行（澳大利亚）', 'BOC', '借记卡', '借记卡', '621394', 16, 6, 1040036, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (580, '中国银行金边分行', 'BOC', '借记卡', '借记卡', '621648', 16, 6, 1040116, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (581, '中国银行雅加达分行', 'BOC', '借记卡', '借记卡', '621248', 16, 6, 1040360, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (582, '中银东京分行', 'BOC', '借记卡普卡', '借记卡普卡', '621215', 16, 6, 1040392, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (583, '中国银行首尔分行', 'BOC', '借记卡', '借记卡', '621249', 16, 6, 1040410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (584, '中国银行澳门分行', 'BOC', '银联港币卡', '银联港币卡', '622273', 19, 6, 1040446, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (585, '中国银行澳门分行', 'BOC', '银联澳门币卡', '银联澳门币卡', '622274', 19, 6, 1040446, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (586, '中国银行澳门分行', 'BOC', '人民币信用卡', '人民币信用卡', '622750', 16, 6, 1040446, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (587, '中国银行澳门分行', 'BOC', '人民币信用卡', '人民币信用卡', '622751', 16, 6, 1040446, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (588, '中国银行澳门分行', 'BOC', '中银卡', '中银卡', '622771', 19, 6, 1040446, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (589, '中国银行澳门分行', 'BOC', '中银卡', '中银卡', '622772', 19, 6, 1040446, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (590, '中国银行澳门分行', 'BOC', '中银卡', '中银卡', '622770', 19, 6, 1040446, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (591, '中国银行澳门分行', 'BOC', '中银银联双币商务卡', '中银银联双币商务卡', '625145', 16, 6, 1040446, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (592, '中国银行澳门分行', 'BOC', '预付卡', '预付卡', '620531', 19, 6, 1040446, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (593, '中国银行澳门分行', 'BOC', '澳门中国银行银联预付卡', '澳门中国银行银联预付卡', '620210', 16, 6, 1040446,
        NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (594, '中国银行澳门分行', 'BOC', '澳门中国银行银联预付卡', '澳门中国银行银联预付卡', '620211', 16, 6, 1040446,
        NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (595, '中国银行澳门分行', 'BOC', '熊猫卡', '熊猫卡', '622479', 16, 6, 1040446, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (596, '中国银行澳门分行', 'BOC', '财富卡', '财富卡', '622480', 16, 6, 1040446, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (597, '中国银行(马来西亚)', 'BOC', '预付卡', '预付卡', '620019', 16, 6, 1040458, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (598, '中国银行(马来西亚)', 'BOC', '预付卡', '预付卡', '620035', 16, 6, 1040458, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (599, '中国银行马尼拉分行', 'BOC', '双币种借记卡', '双币种借记卡', '621231', 16, 6, 1040608, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (600, '中行新加坡分行', 'BOC', 'BOCCUPPLATINUMCARD', 'BOCCUPPLATINUMCARD', '622789', 16, 6, 1040702, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (601, '中国银行胡志明分行', 'BOC', '借记卡', '借记卡', '621638', 16, 6, 1040704, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (602, '中国银行曼谷分行', 'BOC', '借记卡', '借记卡', '621334', 16, 6, 1040764, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (603, '中国银行曼谷分行', 'BOC', '长城信用卡环球通', '长城信用卡环球通', '625140', 16, 6, 1040764, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (604, '中国银行曼谷分行', 'BOC', '借记卡', '借记卡', '621395', 16, 6, 1040764, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (605, '中国银行', 'BOC', '长城宁波市民卡', '长城宁波市民卡', '620513', 19, 6, 1043320, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (606, '建设银行', 'CCB', '龙卡信用卡', '龙卡信用卡', '5453242', 18, 7, 1050000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (607, '建设银行', 'CCB', '龙卡信用卡', '龙卡信用卡', '5491031', 18, 7, 1050000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (608, '建设银行', 'CCB', '龙卡信用卡', '龙卡信用卡', '5544033', 18, 7, 1050000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (609, '建设银行', 'CCB', '龙卡准贷记卡', '龙卡准贷记卡', '622725', 16, 6, 1050000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (610, '建设银行', 'CCB', '龙卡准贷记卡金卡', '龙卡准贷记卡金卡', '622728', 16, 6, 1050000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (611, '建设银行', 'CCB', '中职学生资助卡', '中职学生资助卡', '621284', 19, 6, 1050000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (612, '建设银行', 'CCB', '乐当家银卡VISA', '乐当家银卡VISA', '421349', 16, 6, 1050000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (613, '建设银行', 'CCB', '乐当家金卡VISA', '乐当家金卡VISA', '434061', 16, 6, 1050000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (614, '建设银行', 'CCB', '乐当家白金卡', '乐当家白金卡', '434062', 16, 6, 1050000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (615, '建设银行', 'CCB', '龙卡普通卡VISA', '龙卡普通卡VISA', '436728', 16, 6, 1050000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (616, '建设银行', 'CCB', '龙卡储蓄卡', '龙卡储蓄卡', '436742', 19, 6, 1050000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (617, '建设银行', 'CCB', 'VISA准贷记卡(银联卡)', 'VISA准贷记卡(银联卡)', '453242', 16, 6, 1050000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (618, '建设银行', 'CCB', 'VISA准贷记金卡', 'VISA准贷记金卡', '491031', 16, 6, 1050000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (619, '建设银行', 'CCB', '乐当家', '乐当家', '524094', 16, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (620, '建设银行', 'CCB', '乐当家', '乐当家', '526410', 16, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (621, '建设银行', 'CCB', 'MASTER准贷记卡', 'MASTER准贷记卡', '53242', 16, 5, 1050000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (622, '建设银行', 'CCB', '乐当家', '乐当家', '53243', 16, 5, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (623, '建设银行', 'CCB', '准贷记金卡', '准贷记金卡', '544033', 16, 6, 1050000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (624, '建设银行', 'CCB', '乐当家白金卡', '乐当家白金卡', '552245', 16, 6, 1050000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (625, '建设银行', 'CCB', '金融复合IC卡', '金融复合IC卡', '589970', 19, 6, 1050000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (626, '建设银行', 'CCB', '银联标准卡', '银联标准卡', '620060', 19, 6, 1050000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (627, '建设银行', 'CCB', '银联理财钻石卡', '银联理财钻石卡', '621080', 16, 6, 1050000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (628, '建设银行', 'CCB', '金融IC卡', '金融IC卡', '621081', 19, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (629, '建设银行', 'CCB', '建行陆港通龙卡', '建行陆港通龙卡', '621082', 16, 6, 1050000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (630, '建设银行', 'CCB', '理财白金卡', '理财白金卡', '621466', 16, 6, 1050000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (631, '建设银行', 'CCB', '社保IC卡', '社保IC卡', '621467', 19, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (632, '建设银行', 'CCB', '财富卡私人银行卡', '财富卡私人银行卡', '621488', 16, 6, 1050000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (633, '建设银行', 'CCB', '理财金卡', '理财金卡', '621499', 16, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (634, '建设银行', 'CCB', '福农卡', '福农卡', '621598', 19, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (635, '建设银行', 'CCB', '武警军人保障卡', '武警军人保障卡', '621621', 19, 6, 1050000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (636, '建设银行', 'CCB', '龙卡通', '龙卡通', '621700', 19, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (637, '建设银行', 'CCB', '银联储蓄卡', '银联储蓄卡', '622280', 19, 6, 1050000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (638, '建设银行', 'CCB', '龙卡储蓄卡(银联卡)', '龙卡储蓄卡(银联卡)', '622700', 19, 6, 1050000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (639, '建设银行', 'CCB', '准贷记卡', '准贷记卡', '622707', 16, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (640, '建设银行', 'CCB', '理财白金卡', '理财白金卡', '622966', 16, 6, 1050000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (641, '建设银行', 'CCB', '理财金卡', '理财金卡', '622988', 16, 6, 1050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (642, '建设银行', 'CCB', '准贷记卡普卡', '准贷记卡普卡', '625955', 16, 6, 1050000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (643, '建设银行', 'CCB', '准贷记卡金卡', '准贷记卡金卡', '625956', 16, 6, 1050000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (644, '建设银行', 'CCB', '龙卡信用卡', '龙卡信用卡', '553242', 18, 6, 1050000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (645, '建设银行', 'CCB', '中国旅游卡', '中国旅游卡', '625362', 16, 6, 1050001, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (646, '建设银行', 'CCB', '中国旅游卡', '中国旅游卡', '625363', 16, 6, 1050001, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (647, '建设银行', 'CCB', '龙卡IC公务卡', '龙卡IC公务卡', '628316', 16, 6, 1050001, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (648, '建设银行', 'CCB', '龙卡IC公务卡', '龙卡IC公务卡', '628317', 16, 6, 1050001, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (649, '建设银行', 'CCB', '中国旅游卡', '中国旅游卡', '623211', 19, 6, 1050001, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (650, '建设银行', 'CCB', '龙卡储蓄卡', '龙卡储蓄卡', '436742193', 19, 9, 1053930, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (651, '建设银行', 'CCB', '银联储蓄卡', '银联储蓄卡', '622280193', 19, 9, 1053930, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (652, '建设银行', 'CCB', '龙卡JCB金卡', '龙卡JCB金卡', '356896', 16, 6, 1059999, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (653, '建设银行', 'CCB', '龙卡JCB白金卡', '龙卡JCB白金卡', '356899', 16, 6, 1059999, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (654, '建设银行', 'CCB', '龙卡JCB普卡', '龙卡JCB普卡', '356895', 16, 6, 1059999, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (655, '建设银行', 'CCB', '龙卡贷记卡公司卡', '龙卡贷记卡公司卡', '436718', 16, 6, 1059999, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (656, '建设银行', 'CCB', '龙卡贷记卡', '龙卡贷记卡', '436738', 16, 6, 1059999, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (657, '建设银行', 'CCB', '龙卡国际普通卡VISA', '龙卡国际普通卡VISA', '436745', 16, 6, 1059999, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (658, '建设银行', 'CCB', '龙卡国际金卡VISA', '龙卡国际金卡VISA', '436748', 16, 6, 1059999, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (659, '建设银行', 'CCB', 'VISA白金信用卡', 'VISA白金信用卡', '489592', 16, 6, 1059999, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (660, '建设银行', 'CCB', '龙卡国际白金卡', '龙卡国际白金卡', '531693', 16, 6, 1059999, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (661, '建设银行', 'CCB', '龙卡国际普通卡MASTER', '龙卡国际普通卡MASTER', '532450', 16, 6, 1059999, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (662, '建设银行', 'CCB', '龙卡国际金卡MASTER', '龙卡国际金卡MASTER', '532458', 16, 6, 1059999, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (663, '建设银行', 'CCB', '龙卡万事达金卡', '龙卡万事达金卡', '544887', 16, 6, 1059999, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (664, '建设银行', 'CCB', '龙卡贷记卡', '龙卡贷记卡', '552801', 16, 6, 1059999, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (665, '建设银行', 'CCB', '龙卡万事达白金卡', '龙卡万事达白金卡', '557080', 16, 6, 1059999, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (666, '建设银行', 'CCB', '龙卡贷记卡', '龙卡贷记卡', '558895', 16, 6, 1059999, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (667, '建设银行', 'CCB', '龙卡万事达信用卡', '龙卡万事达信用卡', '559051', 16, 6, 1059999, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (668, '建设银行', 'CCB', '龙卡人民币信用卡', '龙卡人民币信用卡', '622166', 16, 6, 1059999, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (669, '建设银行', 'CCB', '龙卡人民币信用金卡', '龙卡人民币信用金卡', '622168', 16, 6, 1059999, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (670, '建设银行', 'CCB', '龙卡人民币白金卡', '龙卡人民币白金卡', '622708', 16, 6, 1059999, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (671, '建设银行', 'CCB', '龙卡IC信用卡普卡', '龙卡IC信用卡普卡', '625964', 16, 6, 1059999, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (672, '建设银行', 'CCB', '龙卡IC信用卡金卡', '龙卡IC信用卡金卡', '625965', 16, 6, 1059999, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (673, '建设银行', 'CCB', '龙卡IC信用卡白金卡', '龙卡IC信用卡白金卡', '625966', 16, 6, 1059999, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (674, '建设银行', 'CCB', '龙卡银联公务卡普卡', '龙卡银联公务卡普卡', '628266', 16, 6, 1059999, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (675, '建设银行', 'CCB', '龙卡银联公务卡金卡', '龙卡银联公务卡金卡', '628366', 16, 6, 1059999, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (676, '交通银行', 'COMM', '交行预付卡', '交行预付卡', '620021', 19, 6, 3010000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (677, '交通银行', 'COMM', '世博预付IC卡', '世博预付IC卡', '620521', 19, 6, 3010000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (678, '交通银行', 'COMM', '太平洋互连卡', '太平洋互连卡', '0', 17, 8, 3010000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (679, '交通银行', 'COMM', '太平洋信用卡', '太平洋信用卡', '0', 16, 7, 3010000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (680, '交通银行', 'COMM', '太平洋信用卡', '太平洋信用卡', '0', 16, 7, 3010000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (681, '交通银行', 'COMM', '太平洋万事顺卡', '太平洋万事顺卡', '0', 17, 8, 3010000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (682, '交通银行', 'COMM', '太平洋互连卡(银联卡)', '太平洋互连卡(银联卡)', '405512', 17, 6, 3010000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (683, '交通银行', 'COMM', '太平洋白金信用卡', '太平洋白金信用卡', '434910', 16, 6, 3010000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (684, '交通银行', 'COMM', '太平洋双币贷记卡', '太平洋双币贷记卡', '458123', 16, 6, 3010000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (685, '交通银行', 'COMM', '太平洋双币贷记卡', '太平洋双币贷记卡', '458124', 16, 6, 3010000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (686, '交通银行', 'COMM', '太平洋信用卡', '太平洋信用卡', '49104', 16, 5, 3010000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (687, '交通银行', 'COMM', '太平洋双币贷记卡', '太平洋双币贷记卡', '520169', 16, 6, 3010000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (688, '交通银行', 'COMM', '太平洋白金信用卡', '太平洋白金信用卡', '522964', 16, 6, 3010000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (689, '交通银行', 'COMM', '太平洋信用卡', '太平洋信用卡', '53783', 16, 5, 3010000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (690, '交通银行', 'COMM', '太平洋双币贷记卡', '太平洋双币贷记卡', '552853', 16, 6, 3010000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (691, '交通银行', 'COMM', '太平洋万事顺卡', '太平洋万事顺卡', '601428', 17, 6, 3010000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (692, '交通银行', 'COMM', '太平洋人民币贷记卡', '太平洋人民币贷记卡', '622250', 16, 6, 3010000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (693, '交通银行', 'COMM', '太平洋人民币贷记卡', '太平洋人民币贷记卡', '622251', 16, 6, 3010000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (694, '交通银行', 'COMM', '太平洋双币贷记卡', '太平洋双币贷记卡', '521899', 16, 6, 3010000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (695, '交通银行', 'COMM', '太平洋准贷记卡', '太平洋准贷记卡', '622254', 16, 6, 3010000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (696, '交通银行', 'COMM', '太平洋准贷记卡', '太平洋准贷记卡', '622255', 16, 6, 3010000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (697, '交通银行', 'COMM', '太平洋准贷记卡', '太平洋准贷记卡', '622256', 16, 6, 3010000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (698, '交通银行', 'COMM', '太平洋准贷记卡', '太平洋准贷记卡', '622257', 16, 6, 3010000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (699, '交通银行', 'COMM', '太平洋借记卡', '太平洋借记卡', '622258', 17, 6, 3010000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (700, '交通银行', 'COMM', '太平洋借记卡', '太平洋借记卡', '622259', 17, 6, 3010000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (701, '交通银行', 'COMM', '太平洋人民币贷记卡', '太平洋人民币贷记卡', '622253', 16, 6, 3010000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (702, '交通银行', 'COMM', '太平洋借记卡', '太平洋借记卡', '622261', 19, 6, 3010000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (703, '交通银行', 'COMM', '太平洋MORE卡', '太平洋MORE卡', '622284', 16, 6, 3010000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (704, '交通银行', 'COMM', '白金卡', '白金卡', '622656', 16, 6, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (705, '交通银行', 'COMM', '交通银行公务卡普卡', '交通银行公务卡普卡', '628216', 16, 6, 3010000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (706, '交通银行', 'COMM', '太平洋人民币贷记卡', '太平洋人民币贷记卡', '622252', 16, 6, 3010000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (707, '交通银行', 'COMM', '太平洋互连卡', '太平洋互连卡', '66405512', 17, 8, 3010000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (708, '交通银行', 'COMM', '太平洋信用卡', '太平洋信用卡', '6649104', 16, 7, 3010000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (709, '交通银行', 'COMM', '太平洋借记卡', '太平洋借记卡', '622260', 19, 6, 3010000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (710, '交通银行', 'COMM', '太平洋万事顺卡', '太平洋万事顺卡', '66601428', 17, 8, 3010000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (711, '交通银行', 'COMM', '太平洋贷记卡(银联卡)', '太平洋贷记卡(银联卡)', '955590', 16, 6, 3010000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (712, '交通银行', 'COMM', '太平洋贷记卡(银联卡)', '太平洋贷记卡(银联卡)', '955591', 16, 6, 3010000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (713, '交通银行', 'COMM', '太平洋贷记卡(银联卡)', '太平洋贷记卡(银联卡)', '955592', 16, 6, 3010000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (714, '交通银行', 'COMM', '太平洋贷记卡(银联卡)', '太平洋贷记卡(银联卡)', '955593', 16, 6, 3010000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (715, '交通银行', 'COMM', '太平洋信用卡', '太平洋信用卡', '6653783', 16, 7, 3010000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (716, '交通银行', 'COMM', '交通银行公务卡金卡', '交通银行公务卡金卡', '628218', 16, 6, 3010000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (717, '交通银行', 'COMM', '交银IC卡', '交银IC卡', '622262', 19, 6, 3010000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (718, '交通银行香港分行', 'COMM', '银联人民币卡', '银联人民币卡', '621002', 19, 6, 3010344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (719, '交通银行香港分行', 'COMM', '交通银行港币借记卡', '交通银行港币借记卡', '621069', 19, 6, 3010344, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (720, '交通银行香港分行', 'COMM', '港币礼物卡', '港币礼物卡', '620013', 16, 6, 3010344, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (721, '交通银行香港分行', 'COMM', '双币种信用卡', '双币种信用卡', '625028', 16, 6, 3010344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (722, '交通银行香港分行', 'COMM', '双币种信用卡', '双币种信用卡', '625029', 16, 6, 3010344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (723, '交通银行香港分行', 'COMM', '双币卡', '双币卡', '621436', 19, 6, 3010344, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (724, '交通银行澳门分行', 'COMM', '银联借记卡', '银联借记卡', '621335', 19, 6, 3010446, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (725, '中信银行', 'CITIC', '中信借记卡', '中信借记卡', '433670', 16, 6, 3020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (726, '中信银行', 'CITIC', '中信借记卡', '中信借记卡', '433680', 16, 6, 3020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (727, '中信银行', 'CITIC', '中信国际借记卡', '中信国际借记卡', '442729', 16, 6, 3020000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (728, '中信银行', 'CITIC', '中信国际借记卡', '中信国际借记卡', '442730', 16, 6, 3020000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (729, '中信银行', 'CITIC', '中国旅行卡', '中国旅行卡', '620082', 16, 6, 3020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (730, '中信银行', 'CITIC', '中信借记卡(银联卡)', '中信借记卡(银联卡)', '622690', 16, 6, 3020000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (731, '中信银行', 'CITIC', '中信借记卡(银联卡)', '中信借记卡(银联卡)', '622691', 16, 6, 3020000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (732, '中信银行', 'CITIC', '中信贵宾卡(银联卡)', '中信贵宾卡(银联卡)', '622692', 16, 6, 3020000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (733, '中信银行', 'CITIC', '中信理财宝金卡', '中信理财宝金卡', '622696', 16, 6, 3020000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (734, '中信银行', 'CITIC', '中信理财宝白金卡', '中信理财宝白金卡', '622698', 16, 6, 3020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (735, '中信银行', 'CITIC', '中信钻石卡', '中信钻石卡', '622998', 16, 6, 3020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (736, '中信银行', 'CITIC', '中信钻石卡', '中信钻石卡', '622999', 16, 6, 3020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (737, '中信银行', 'CITIC', '中信借记卡', '中信借记卡', '433671', 16, 6, 3020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (738, '中信银行', 'CITIC', '中信理财宝(银联卡)', '中信理财宝(银联卡)', '968807', 16, 6, 3020000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (739, '中信银行', 'CITIC', '中信理财宝(银联卡)', '中信理财宝(银联卡)', '968808', 16, 6, 3020000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (740, '中信银行', 'CITIC', '中信理财宝(银联卡)', '中信理财宝(银联卡)', '968809', 16, 6, 3020000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (741, '中信银行', 'CITIC', '借记卡', '借记卡', '621771', 16, 6, 3020000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (742, '中信银行', 'CITIC', '理财宝IC卡', '理财宝IC卡', '621767', 16, 6, 3020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (743, '中信银行', 'CITIC', '理财宝IC卡', '理财宝IC卡', '621768', 16, 6, 3020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (744, '中信银行', 'CITIC', '理财宝IC卡', '理财宝IC卡', '621770', 16, 6, 3020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (745, '中信银行', 'CITIC', '理财宝IC卡', '理财宝IC卡', '621772', 16, 6, 3020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (746, '中信银行', 'CITIC', '理财宝IC卡', '理财宝IC卡', '621773', 16, 6, 3020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (747, '中国光大银行', 'CEB', '阳光卡', '阳光卡', '303', 16, 3, 3030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (748, '中国光大银行', 'CITIC', '阳光商旅信用卡', '阳光商旅信用卡', '356837', 16, 6, 3030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (749, '中国光大银行', 'CITIC', '阳光商旅信用卡', '阳光商旅信用卡', '356838', 16, 6, 3030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (750, '中国光大银行', 'CITIC', '阳光商旅信用卡', '阳光商旅信用卡', '486497', 16, 6, 3030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (751, '中国光大银行', 'CEB', '阳光旅行卡', '阳光旅行卡', '620085', 16, 6, 3030000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (752, '中国光大银行', 'CEB', '阳光卡(银联卡)', '阳光卡(银联卡)', '622660', 16, 6, 3030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (753, '中国光大银行', 'CEB', '阳光卡(银联卡)', '阳光卡(银联卡)', '622662', 16, 6, 3030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (754, '中国光大银行', 'CEB', '阳光卡(银联卡)', '阳光卡(银联卡)', '622663', 16, 6, 3030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (755, '中国光大银行', 'CEB', '阳光卡(银联卡)', '阳光卡(银联卡)', '622664', 16, 6, 3030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (756, '中国光大银行', 'CEB', '阳光卡(银联卡)', '阳光卡(银联卡)', '622665', 16, 6, 3030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (757, '中国光大银行', 'CEB', '阳光卡(银联卡)', '阳光卡(银联卡)', '622666', 16, 6, 3030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (758, '中国光大银行', 'CEB', '阳光卡(银联卡)', '阳光卡(银联卡)', '622667', 16, 6, 3030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (759, '中国光大银行', 'CEB', '阳光卡(银联卡)', '阳光卡(银联卡)', '622669', 16, 6, 3030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (760, '中国光大银行', 'CEB', '阳光卡(银联卡)', '阳光卡(银联卡)', '622670', 16, 6, 3030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (761, '中国光大银行', 'CEB', '阳光卡(银联卡)', '阳光卡(银联卡)', '622671', 16, 6, 3030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (762, '中国光大银行', 'CEB', '阳光卡(银联卡)', '阳光卡(银联卡)', '622672', 16, 6, 3030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (763, '中国光大银行', 'CEB', '阳光卡(银联卡)', '阳光卡(银联卡)', '622668', 16, 6, 3030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (764, '中国光大银行', 'CEB', '阳光卡(银联卡)', '阳光卡(银联卡)', '622661', 16, 6, 3030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (765, '中国光大银行', 'CEB', '阳光卡(银联卡)', '阳光卡(银联卡)', '622674', 16, 6, 3030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (766, '中国光大银行', 'CEB', '阳光卡(银联卡)', '阳光卡(银联卡)', '90030', 16, 5, 3030000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (767, '中国光大银行', 'CEB', '阳光卡(银联卡)', '阳光卡(银联卡)', '622673', 16, 6, 3030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (768, '中国光大银行', 'CEB', '借记卡普卡', '借记卡普卡', '620518', 16, 6, 3030000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (769, '中国光大银行', 'CEB', '社会保障IC卡', '社会保障IC卡', '621489', 16, 6, 3030000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (770, '中国光大银行', 'CEB', 'IC借记卡普卡', 'IC借记卡普卡', '621492', 16, 6, 3030000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (771, '中国光大银行', 'CEB', '手机支付卡', '手机支付卡', '620535', 19, 6, 3030000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (772, '华夏银行', 'HXBANK', '华夏卡(银联卡)', '华夏卡(银联卡)', '622630', 16, 6, 3040000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (773, '华夏银行', 'HXBANK', '华夏至尊金卡(银联卡)', '华夏至尊金卡(银联卡)', '622631', 16, 6, 3040000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (774, '华夏银行', 'HXBANK', '华夏丽人卡(银联卡)', '华夏丽人卡(银联卡)', '622632', 16, 6, 3040000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (775, '华夏银行', 'HXBANK', '华夏万通卡', '华夏万通卡', '622633', 16, 6, 3040000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (776, '华夏银行', 'HXBANK', '华夏卡(银联卡)', '华夏卡(银联卡)', '999999', 16, 6, 3040000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (777, '华夏银行', 'HXBANK', '华夏白金卡', '华夏白金卡', '621222', 16, 6, 3040000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (778, '华夏银行', 'HXBANK', '华夏普卡', '华夏普卡', '623020', 16, 6, 3040000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (779, '华夏银行', 'HXBANK', '华夏金卡', '华夏金卡', '623021', 16, 6, 3040000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (780, '华夏银行', 'HXBANK', '华夏白金卡', '华夏白金卡', '623022', 16, 6, 3040000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (781, '华夏银行', 'HXBANK', '华夏钻石卡', '华夏钻石卡', '623023', 16, 6, 3040000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (782, '中国民生银行', 'CMBC', '民生借记卡(银联卡)', '民生借记卡(银联卡)', '622615', 16, 6, 3050000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (783, '中国民生银行', 'CMBC', '民生银联借记卡－金卡', '民生银联借记卡－金卡', '622616', 16, 6, 3050000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (784, '中国民生银行', 'CMBC', '钻石卡', '钻石卡', '622618', 16, 6, 3050000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (785, '中国民生银行', 'CMBC', '民生借记卡(银联卡)', '民生借记卡(银联卡)', '622622', 16, 6, 3050000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (786, '中国民生银行', 'CMBC', '民生借记卡(银联卡)', '民生借记卡(银联卡)', '622617', 16, 6, 3050000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (787, '中国民生银行', 'CMBC', '民生借记卡(银联卡)', '民生借记卡(银联卡)', '622619', 16, 6, 3050000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (788, '中国民生银行', 'CMBC', '民生借记卡', '民生借记卡', '415599', 16, 6, 3050000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (789, '中国民生银行', 'CMBC', '民生国际卡', '民生国际卡', '421393', 16, 6, 3050000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (790, '中国民生银行', 'CMBC', '民生国际卡(银卡)', '民生国际卡(银卡)', '421865', 16, 6, 3050000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (791, '中国民生银行', 'CMBC', '民生国际卡(欧元卡)', '民生国际卡(欧元卡)', '427570', 16, 6, 3050000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (792, '中国民生银行', 'CMBC', '民生国际卡(澳元卡)', '民生国际卡(澳元卡)', '427571', 16, 6, 3050000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (793, '中国民生银行', 'CMBC', '民生国际卡', '民生国际卡', '472067', 16, 6, 3050000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (794, '中国民生银行', 'CMBC', '民生国际卡', '民生国际卡', '472068', 16, 6, 3050000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (795, '中国民生银行', 'CMBC', '薪资理财卡', '薪资理财卡', '622620', 16, 6, 3050000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (796, '中国民生银行', 'CMBC', '民生MasterCard', '民生MasterCard', '545392', 16, 6, 3050001, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (797, '中国民生银行', 'CMBC', '民生MasterCard', '民生MasterCard', '545393', 16, 6, 3050001, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (798, '中国民生银行', 'CMBC', '民生MasterCard', '民生MasterCard', '545431', 16, 6, 3050001, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (799, '中国民生银行', 'CMBC', '民生MasterCard', '民生MasterCard', '545447', 16, 6, 3050001, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (800, '中国民生银行', 'CMBC', '民生JCB信用卡', '民生JCB信用卡', '356859', 16, 6, 3050001, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (801, '中国民生银行', 'CMBC', '民生JCB金卡', '民生JCB金卡', '356857', 16, 6, 3050001, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (802, '中国民生银行', 'CMBC', '民生贷记卡(银联卡)', '民生贷记卡(银联卡)', '407405', 16, 6, 3050001, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (803, '中国民生银行', 'CMBC', '民生贷记卡(银联卡)', '民生贷记卡(银联卡)', '421869', 16, 6, 3050001, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (804, '中国民生银行', 'CMBC', '民生贷记卡(银联卡)', '民生贷记卡(银联卡)', '421870', 16, 6, 3050001, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (805, '中国民生银行', 'CMBC', '民生贷记卡(银联卡)', '民生贷记卡(银联卡)', '421871', 16, 6, 3050001, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (806, '中国民生银行', 'CMBC', '民生贷记卡(银联卡)', '民生贷记卡(银联卡)', '512466', 16, 6, 3050001, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (807, '中国民生银行', 'CMBC', '民生JCB普卡', '民生JCB普卡', '356856', 16, 6, 3050001, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (808, '中国民生银行', 'CMBC', '民生贷记卡(银联卡)', '民生贷记卡(银联卡)', '528948', 16, 6, 3050001, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (809, '中国民生银行', 'CMBC', '民生贷记卡(银联卡)', '民生贷记卡(银联卡)', '552288', 16, 6, 3050001, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (810, '中国民生银行', 'CMBC', '民生信用卡(银联卡)', '民生信用卡(银联卡)', '622600', 16, 6, 3050001, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (811, '中国民生银行', 'CMBC', '民生信用卡(银联卡)', '民生信用卡(银联卡)', '622601', 16, 6, 3050001, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (812, '中国民生银行', 'CMBC', '民生银联白金信用卡', '民生银联白金信用卡', '622602', 16, 6, 3050001, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (813, '中国民生银行', 'CMBC', '民生贷记卡(银联卡)', '民生贷记卡(银联卡)', '517636', 16, 6, 3050001, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (814, '中国民生银行', 'CMBC', '民生银联个人白金卡', '民生银联个人白金卡', '622621', 16, 6, 3050001, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (815, '中国民生银行', 'CMBC', '公务卡金卡', '公务卡金卡', '628258', 16, 6, 3050001, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (816, '中国民生银行', 'CMBC', '民生贷记卡(银联卡)', '民生贷记卡(银联卡)', '556610', 16, 6, 3050001, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (817, '中国民生银行', 'CMBC', '民生银联商务信用卡', '民生银联商务信用卡', '622603', 16, 6, 3050001, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (818, '中国民生银行', 'CMBC', '民VISA无限卡', '民VISA无限卡', '464580', 16, 6, 3050001, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (819, '中国民生银行', 'CMBC', '民生VISA商务白金卡', '民生VISA商务白金卡', '464581', 16, 6, 3050001, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (820, '中国民生银行', 'CMBC', '民生万事达钛金卡', '民生万事达钛金卡', '523952', 16, 6, 3050001, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (821, '中国民生银行', 'CMBC', '民生万事达世界卡', '民生万事达世界卡', '545217', 16, 6, 3050001, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (822, '中国民生银行', 'CMBC', '民生万事达白金公务卡', '民生万事达白金公务卡', '553161', 16, 6, 3050001, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (823, '中国民生银行', 'CMBC', '民生JCB白金卡', '民生JCB白金卡', '356858', 16, 6, 3050001, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (824, '中国民生银行', 'CMBC', '银联标准金卡', '银联标准金卡', '622623', 16, 6, 3050001, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (825, '中国民生银行', 'CMBC', '银联芯片金卡', '银联芯片金卡', '625912', 16, 6, 3050001, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (826, '中国民生银行', 'CMBC', '银联芯片白金卡', '银联芯片白金卡', '625913', 16, 6, 3050001, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (827, '中国民生银行', 'CMBC', '银联芯片普卡', '银联芯片普卡', '625911', 16, 6, 3050001, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (828, '中国民生银行', 'CMBC', '民生运通双币标准信用卡白金卡', '民生运通双币标准信用卡白金卡', '377155', 15, 6,
        3050001, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (829, '中国民生银行', 'CMBC', '民生运通双币信用卡普卡', '民生运通双币信用卡普卡', '377152', 15, 6, 3050001, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (830, '中国民生银行', 'CMBC', '民生运通双币信用卡金卡', '民生运通双币信用卡金卡', '377153', 15, 6, 3050001, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (831, '中国民生银行', 'CMBC', '民生运通双币信用卡钻石卡', '民生运通双币信用卡钻石卡', '377158', 15, 6, 3050001,
        NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (832, '广东发展银行', 'GDB', '广发VISA信用卡', '广发VISA信用卡', '406365', 16, 6, 3060000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (833, '广东发展银行', 'GDB', '广发VISA信用卡', '广发VISA信用卡', '406366', 16, 6, 3060000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (834, '广东发展银行', 'GDB', '广发信用卡', '广发信用卡', '428911', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (835, '广东发展银行', 'GDB', '广发信用卡', '广发信用卡', '436768', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (836, '广东发展银行', 'GDB', '广发信用卡', '广发信用卡', '436769', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (837, '广东发展银行', 'GDB', '广发信用卡', '广发信用卡', '436770', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (838, '广东发展银行', 'GDB', '广发VISA信用卡', '广发VISA信用卡', '487013', 16, 6, 3060000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (839, '广东发展银行', 'GDB', '广发信用卡', '广发信用卡', '491032', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (840, '广东发展银行', 'GDB', '广发信用卡', '广发信用卡', '491033', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (841, '广东发展银行', 'GDB', '广发信用卡', '广发信用卡', '491034', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (842, '广东发展银行', 'GDB', '广发信用卡', '广发信用卡', '491035', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (843, '广东发展银行', 'GDB', '广发信用卡', '广发信用卡', '491036', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (844, '广东发展银行', 'GDB', '广发信用卡', '广发信用卡', '491037', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (845, '广东发展银行', 'GDB', '广发信用卡', '广发信用卡', '491038', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (846, '广东发展银行', 'GDB', '广发信用卡', '广发信用卡', '436771', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (847, '广东发展银行', 'GDB', '广发信用卡', '广发信用卡', '518364', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (848, '广东发展银行', 'GDB', '广发万事达信用卡', '广发万事达信用卡', '520152', 16, 6, 3060000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (849, '广东发展银行', 'GDB', '广发万事达信用卡', '广发万事达信用卡', '520382', 16, 6, 3060000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (850, '广东发展银行', 'GDB', '广发信用卡', '广发信用卡', '541709', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (851, '广东发展银行', 'GDB', '广发信用卡', '广发信用卡', '541710', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (852, '广东发展银行', 'GDB', '广发信用卡', '广发信用卡', '548844', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (853, '广东发展银行', 'GDB', '广发万事达信用卡', '广发万事达信用卡', '552794', 16, 6, 3060000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (854, '广东发展银行', 'GDB', '广发信用卡', '广发信用卡', '493427', 16, 6, 3060000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (855, '广东发展银行', 'GDB', '广发银联标准金卡', '广发银联标准金卡', '622555', 16, 6, 3060000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (856, '广东发展银行', 'GDB', '广发银联标准普卡', '广发银联标准普卡', '622556', 16, 6, 3060000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (857, '广东发展银行', 'GDB', '广发银联标准真情金卡', '广发银联标准真情金卡', '622557', 16, 6, 3060000, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (858, '广东发展银行', 'GDB', '广发银联标准白金卡', '广发银联标准白金卡', '622558', 16, 6, 3060000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (859, '广东发展银行', 'GDB', '广发银联标准真情普卡', '广发银联标准真情普卡', '622559', 16, 6, 3060000, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (860, '广东发展银行', 'GDB', '广发真情白金卡', '广发真情白金卡', '622560', 16, 6, 3060000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (861, '广东发展银行', 'GDB', '广发理财通卡', '广发理财通卡', '622568', 19, 6, 3060000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (862, '广东发展银行', 'GDB', '广发万事达信用卡', '广发万事达信用卡', '528931', 16, 6, 3060000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (863, '广东发展银行', 'GDB', '广发万事达信用卡', '广发万事达信用卡', '685800', 19, 6, 3060000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (864, '广东发展银行', 'GDB', '广发理财通', '广发理财通', '6858000', 19, 7, 3060000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (865, '广东发展银行', 'GDB', '广发理财通', '广发理财通', '6858001', 19, 7, 3060000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (866, '广东发展银行', 'GDB', '广发理财通', '广发理财通', '6858009', 19, 7, 3060000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (867, '广东发展银行', 'GDB', '广发理财通(银联卡)', '广发理财通(银联卡)', '9111', 19, 4, 3060000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (868, '广东发展银行', 'GDB', '广发万事达信用卡', '广发万事达信用卡', '558894', 16, 6, 3060000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (869, '广东发展银行', 'GDB', '银联标准金卡', '银联标准金卡', '625072', 16, 6, 3060000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (870, '广东发展银行', 'GDB', '银联标准普卡', '银联标准普卡', '625071', 16, 6, 3060000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (871, '广东发展银行', 'GDB', '银联公务金卡', '银联公务金卡', '628260', 16, 6, 3060000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (872, '广东发展银行', 'GDB', '银联公务普卡', '银联公务普卡', '628259', 16, 6, 3060000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (873, '广东发展银行', 'GDB', '理财通卡', '理财通卡', '621462', 19, 6, 3060000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (874, '广东发展银行', 'GDB', '银联真情普卡', '银联真情普卡', '625805', 16, 6, 3060000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (875, '广东发展银行', 'GDB', '银联真情金卡', '银联真情金卡', '625806', 16, 6, 3060000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (876, '广东发展银行', 'GDB', '银联真情白金卡', '银联真情白金卡', '625807', 16, 6, 3060000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (877, '广东发展银行', 'GDB', '银联标准普卡', '银联标准普卡', '625808', 16, 6, 3060000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (878, '广东发展银行', 'GDB', '银联标准金卡', '银联标准金卡', '625809', 16, 6, 3060000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (879, '广东发展银行', 'GDB', '银联标准白金卡', '银联标准白金卡', '625810', 16, 6, 3060000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (880, '深圳发展银行', '(NULL)', '沃尔玛百分卡', '沃尔玛百分卡', '435744', 16, 6, 3070000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (881, '深圳发展银行', '(NULL)', '沃尔玛百分卡', '沃尔玛百分卡', '435745', 16, 6, 3070000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (882, '深圳发展银行', '(NULL)', 'VISA白金卡', 'VISA白金卡', '483536', 16, 6, 3070000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (883, '深圳发展银行', '(NULL)', '人民币信用卡金卡', '人民币信用卡金卡', '622525', 16, 6, 3070000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (884, '深圳发展银行', '(NULL)', '人民币信用卡普卡', '人民币信用卡普卡', '622526', 16, 6, 3070000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (885, '深圳发展银行', '(NULL)', '发展信用卡(银联卡)', '发展信用卡(银联卡)', '998801', 16, 6, 3070000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (886, '深圳发展银行', '(NULL)', '发展信用卡(银联卡)', '发展信用卡(银联卡)', '998802', 16, 6, 3070000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (887, '深圳发展银行', '(NULL)', '发展借记卡', '发展借记卡', '412963', 16, 6, 3070010, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (888, '深圳发展银行', '(NULL)', '国际借记卡', '国际借记卡', '415752', 16, 6, 3070010, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (889, '深圳发展银行', '(NULL)', '国际借记卡', '国际借记卡', '415753', 16, 6, 3070010, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (890, '深圳发展银行', '(NULL)', '聚财卡金卡', '聚财卡金卡', '622535', 16, 6, 3070010, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (891, '深圳发展银行', '(NULL)', '聚财卡VIP金卡', '聚财卡VIP金卡', '622536', 16, 6, 3070010, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (892, '深圳发展银行', '(NULL)', '发展卡(银联卡)', '发展卡(银联卡)', '622538', 16, 6, 3070010, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (893, '深圳发展银行', '(NULL)', '聚财卡白金卡和钻石卡', '聚财卡白金卡和钻石卡', '622539', 16, 6, 3070010, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (894, '深圳发展银行', '(NULL)', '发展借记卡(银联卡)', '发展借记卡(银联卡)', '998800', 16, 6, 3070010, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (895, '深圳发展银行', '(NULL)', '发展借记卡', '发展借记卡', '412962', 16, 6, 3070010, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (896, '深圳发展银行', '(NULL)', '聚财卡钻石卡', '聚财卡钻石卡', '622983', 16, 6, 3070010, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (897, '深圳发展银行', '(NULL)', '公益预付卡', '公益预付卡', '620010', 16, 6, 3070010, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (898, '招商银行', 'CMB', '招商银行信用卡', '招商银行信用卡', '356885', 16, 6, 3080000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (899, '招商银行', 'CMB', '招商银行信用卡', '招商银行信用卡', '356886', 16, 6, 3080000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (900, '招商银行', 'CMB', '招商银行信用卡', '招商银行信用卡', '356887', 16, 6, 3080000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (901, '招商银行', 'CMB', '招商银行信用卡', '招商银行信用卡', '356888', 16, 6, 3080000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (902, '招商银行', 'CMB', '招商银行信用卡', '招商银行信用卡', '356890', 16, 6, 3080000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (903, '招商银行', 'CMB', '两地一卡通', '两地一卡通', '402658', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (904, '招商银行', 'CMB', '招行国际卡(银联卡)', '招行国际卡(银联卡)', '410062', 16, 6, 3080000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (905, '招商银行', 'CMB', '招商银行信用卡', '招商银行信用卡', '439188', 16, 6, 3080000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (906, '招商银行', 'CMB', 'VISA商务信用卡', 'VISA商务信用卡', '439227', 16, 6, 3080000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (907, '招商银行', 'CMB', '招行国际卡(银联卡)', '招行国际卡(银联卡)', '468203', 16, 6, 3080000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (908, '招商银行', 'CMB', '招商银行信用卡', '招商银行信用卡', '479228', 16, 6, 3080000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (909, '招商银行', 'CMB', '招商银行信用卡', '招商银行信用卡', '479229', 16, 6, 3080000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (910, '招商银行', 'CMB', '招行国际卡(银联卡)', '招行国际卡(银联卡)', '512425', 16, 6, 3080000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (911, '招商银行', 'CMB', '世纪金花联名信用卡', '世纪金花联名信用卡', '521302', 16, 6, 3080000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (912, '招商银行', 'CMB', '招行国际卡(银联卡)', '招行国际卡(银联卡)', '524011', 16, 6, 3080000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (913, '招商银行', 'CMB', '招商银行信用卡', '招商银行信用卡', '356889', 16, 6, 3080000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (914, '招商银行', 'CMB', '万事达信用卡', '万事达信用卡', '545620', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (915, '招商银行', 'CMB', '万事达信用卡', '万事达信用卡', '545621', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (916, '招商银行', 'CMB', '万事达信用卡', '万事达信用卡', '545947', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (917, '招商银行', 'CMB', '万事达信用卡', '万事达信用卡', '545948', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (918, '招商银行', 'CMB', '招商银行信用卡', '招商银行信用卡', '552534', 16, 6, 3080000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (919, '招商银行', 'CMB', '招商银行信用卡', '招商银行信用卡', '552587', 16, 6, 3080000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (920, '招商银行', 'CMB', '招商银行信用卡', '招商银行信用卡', '622575', 16, 6, 3080000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (921, '招商银行', 'CMB', '招商银行信用卡', '招商银行信用卡', '622576', 16, 6, 3080000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (922, '招商银行', 'CMB', '招商银行信用卡', '招商银行信用卡', '622577', 16, 6, 3080000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (923, '招商银行', 'CMB', '招商银行信用卡', '招商银行信用卡', '622578', 16, 6, 3080000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (924, '招商银行', 'CMB', '招商银行信用卡', '招商银行信用卡', '622579', 16, 6, 3080000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (925, '招商银行', 'CMB', '一卡通(银联卡)', '一卡通(银联卡)', '622580', 16, 6, 3080000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (926, '招商银行', 'CMB', '万事达信用卡', '万事达信用卡', '545619', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (927, '招商银行', 'CMB', '招商银行信用卡', '招商银行信用卡', '622581', 16, 6, 3080000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (928, '招商银行', 'CMB', '招商银行信用卡', '招商银行信用卡', '622582', 16, 6, 3080000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (929, '招商银行', 'CMB', '一卡通(银联卡)', '一卡通(银联卡)', '622588', 16, 6, 3080000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (930, '招商银行', 'CMB', '公司卡(银联卡)', '公司卡(银联卡)', '622598', 16, 6, 3080000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (931, '招商银行', 'CMB', '金卡', '金卡', '622609', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (932, '招商银行', 'CMB', '招行一卡通', '招行一卡通', '690755', 15, 6, 3080000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (933, '招商银行', 'CMB', '招行一卡通', '招行一卡通', '690755', 18, 6, 3080000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (934, '招商银行', 'CMB', '一卡通(银联卡)', '一卡通(银联卡)', '95555', 16, 5, 3080000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (935, '招商银行', 'CMB', '万事达信用卡', '万事达信用卡', '545623', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (936, '招商银行', 'CMB', '金葵花卡', '金葵花卡', '621286', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (937, '招商银行', 'CMB', '电子现金卡', '电子现金卡', '620520', 19, 6, 3080000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (938, '招商银行', 'CMB', '银联IC普卡', '银联IC普卡', '621483', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (939, '招商银行', 'CMB', '银联IC金卡', '银联IC金卡', '621485', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (940, '招商银行', 'CMB', '银联金葵花IC卡', '银联金葵花IC卡', '621486', 16, 6, 3080000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (941, '招商银行', 'CMB', 'IC公务卡', 'IC公务卡', '628290', 16, 6, 3080000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (942, '招商银行', 'CMB', '美国运通绿卡', '美国运通绿卡', '370285', 15, 6, 3080010, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (943, '招商银行', 'CMB', '美国运通金卡', '美国运通金卡', '370286', 15, 6, 3080010, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (944, '招商银行', 'CMB', '美国运通商务绿卡', '美国运通商务绿卡', '370287', 15, 6, 3080010, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (945, '招商银行', 'CMB', '美国运通商务金卡', '美国运通商务金卡', '370289', 15, 6, 3080010, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (946, '招商银行', 'CMB', 'VISA信用卡', 'VISA信用卡', '439225', 16, 6, 3080010, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (947, '招商银行', 'CMB', 'MASTER信用卡', 'MASTER信用卡', '518710', 16, 6, 3080010, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (948, '招商银行', 'CMB', 'MASTER信用金卡', 'MASTER信用金卡', '518718', 16, 6, 3080010, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (949, '招商银行', 'CMB', '银联标准公务卡(金卡)', '银联标准公务卡(金卡)', '628362', 16, 6, 3080010, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (950, '招商银行', 'CMB', 'VISA信用卡', 'VISA信用卡', '439226', 16, 6, 3080010, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (951, '招商银行', 'CMB', '银联标准财政公务卡', '银联标准财政公务卡', '628262', 16, 6, 3080010, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (952, '招商银行', 'CMB', '芯片IC信用卡', '芯片IC信用卡', '625802', 16, 6, 3080010, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (953, '招商银行', 'CMB', '芯片IC信用卡', '芯片IC信用卡', '625803', 16, 6, 3080010, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (954, '招商银行', 'CMB', '香港一卡通', '香港一卡通', '621299', 16, 6, 3080344, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (955, '兴业银行', 'CIB', '兴业智能卡', '兴业智能卡', '438589', 18, 6, 3090000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (956, '兴业银行', 'CIB', '兴业卡', '兴业卡', '90592', 16, 5, 3090000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (957, '兴业银行', 'CIB', '兴业卡(银联卡)', '兴业卡(银联卡)', '966666', 18, 6, 3090000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (958, '兴业银行', 'CIB', '兴业卡(银联标准卡)', '兴业卡(银联标准卡)', '622909', 18, 6, 3090000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (959, '兴业银行', 'CIB', '兴业智能卡(银联卡)', '兴业智能卡(银联卡)', '438588', 18, 6, 3090000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (960, '兴业银行', 'CIB', '兴业自然人生理财卡', '兴业自然人生理财卡', '622908', 18, 6, 3090000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (961, '兴业银行', 'CIB', 'visa标准双币个人普卡', 'visa标准双币个人普卡', '461982', 16, 6, 3090010, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (962, '兴业银行', 'CIB', 'VISA商务普卡', 'VISA商务普卡', '486493', 16, 6, 3090010, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (963, '兴业银行', 'CIB', 'VISA商务金卡', 'VISA商务金卡', '486494', 16, 6, 3090010, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (964, '兴业银行', 'CIB', 'VISA运动白金信用卡', 'VISA运动白金信用卡', '486861', 16, 6, 3090010, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (965, '兴业银行', 'CIB', '万事达信用卡(银联卡)', '万事达信用卡(银联卡)', '523036', 16, 6, 3090010, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (966, '兴业银行', 'CIB', 'VISA信用卡(银联卡)', 'VISA信用卡(银联卡)', '451289', 16, 6, 3090010, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (967, '兴业银行', 'CIB', '加菲猫信用卡', '加菲猫信用卡', '527414', 16, 6, 3090010, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (968, '兴业银行', 'CIB', '个人白金卡', '个人白金卡', '528057', 16, 6, 3090010, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (969, '兴业银行', 'CIB', '银联信用卡(银联卡)', '银联信用卡(银联卡)', '622901', 16, 6, 3090010, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (970, '兴业银行', 'CIB', '银联信用卡(银联卡)', '银联信用卡(银联卡)', '622902', 16, 6, 3090010, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (971, '兴业银行', 'CIB', '银联白金信用卡', '银联白金信用卡', '622922', 16, 6, 3090010, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (972, '兴业银行', 'CIB', '银联标准公务卡', '银联标准公务卡', '628212', 16, 6, 3090010, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (973, '兴业银行', 'CIB', 'VISA信用卡(银联卡)', 'VISA信用卡(银联卡)', '451290', 16, 6, 3090010, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (974, '兴业银行', 'CIB', '万事达信用卡(银联卡)', '万事达信用卡(银联卡)', '524070', 16, 6, 3090010, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (975, '兴业银行', 'CIB', '银联标准贷记普卡', '银联标准贷记普卡', '625084', 16, 6, 3090010, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (976, '兴业银行', 'CIB', '银联标准贷记金卡', '银联标准贷记金卡', '625085', 16, 6, 3090010, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (977, '兴业银行', 'CIB', '银联标准贷记金卡', '银联标准贷记金卡', '625086', 16, 6, 3090010, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (978, '兴业银行', 'CIB', '银联标准贷记金卡', '银联标准贷记金卡', '625087', 16, 6, 3090010, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (979, '兴业银行', 'CIB', '兴业信用卡', '兴业信用卡', '548738', 16, 6, 3090010, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (980, '兴业银行', 'CIB', '兴业信用卡', '兴业信用卡', '549633', 16, 6, 3090010, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (981, '兴业银行', 'CIB', '兴业信用卡', '兴业信用卡', '552398', 16, 6, 3090010, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (982, '兴业银行', 'CIB', '银联标准贷记普卡', '银联标准贷记普卡', '625082', 16, 6, 3090010, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (983, '兴业银行', 'CIB', '银联标准贷记普卡', '银联标准贷记普卡', '625083', 16, 6, 3090010, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (984, '兴业银行', 'CIB', '兴业芯片普卡', '兴业芯片普卡', '625960', 16, 6, 3090010, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (985, '兴业银行', 'CIB', '兴业芯片金卡', '兴业芯片金卡', '625961', 16, 6, 3090010, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (986, '兴业银行', 'CIB', '兴业芯片白金卡', '兴业芯片白金卡', '625962', 16, 6, 3090010, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (987, '兴业银行', 'CIB', '兴业芯片钻石卡', '兴业芯片钻石卡', '625963', 16, 6, 3090010, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (988, '浦东发展银行', 'SPDB', '浦发JCB金卡', '浦发JCB金卡', '356851', 16, 6, 3100000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (989, '浦东发展银行', 'SPDB', '浦发JCB白金卡', '浦发JCB白金卡', '356852', 16, 6, 3100000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (990, '浦东发展银行', 'SPDB', '信用卡VISA普通', '信用卡VISA普通', '404738', 16, 6, 3100000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (991, '浦东发展银行', 'SPDB', '信用卡VISA金卡', '信用卡VISA金卡', '404739', 16, 6, 3100000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (992, '浦东发展银行', 'SPDB', '浦发银行VISA年青卡', '浦发银行VISA年青卡', '456418', 16, 6, 3100000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (993, '浦东发展银行', 'SPDB', 'VISA白金信用卡', 'VISA白金信用卡', '498451', 16, 6, 3100000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (994, '浦东发展银行', 'SPDB', '浦发万事达白金卡', '浦发万事达白金卡', '515672', 16, 6, 3100000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (995, '浦东发展银行', 'SPDB', '浦发JCB普卡', '浦发JCB普卡', '356850', 16, 6, 3100000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (996, '浦东发展银行', 'SPDB', '浦发万事达金卡', '浦发万事达金卡', '517650', 16, 6, 3100000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (997, '浦东发展银行', 'SPDB', '浦发万事达普卡', '浦发万事达普卡', '525998', 16, 6, 3100000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (998, '浦东发展银行', 'SPDB', '浦发单币卡', '浦发单币卡', '622177', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (999, '浦东发展银行', 'SPDB', '浦发银联单币麦兜普卡', '浦发银联单币麦兜普卡', '622277', 16, 6, 3100000, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1000, '浦东发展银行', 'SPDB', '东方轻松理财卡', '东方轻松理财卡', '622516', 16, 6, 3100000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1001, '浦东发展银行', 'SPDB', '东方-轻松理财卡普卡', '东方-轻松理财卡普卡', '622517', 16, 6, 3100000, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1002, '浦东发展银行', 'SPDB', '东方轻松理财卡', '东方轻松理财卡', '622518', 16, 6, 3100000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1003, '浦东发展银行', 'SPDB', '东方轻松理财智业金卡', '东方轻松理财智业金卡', '622520', 16, 6, 3100000, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1004, '浦东发展银行', 'SPDB', '东方卡(银联卡)', '东方卡(银联卡)', '622521', 16, 6, 3100000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1005, '浦东发展银行', 'SPDB', '东方卡(银联卡)', '东方卡(银联卡)', '622522', 16, 6, 3100000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1006, '浦东发展银行', 'SPDB', '东方卡(银联卡)', '东方卡(银联卡)', '622523', 16, 6, 3100000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1007, '浦东发展银行', 'SPDB', '公务卡金卡', '公务卡金卡', '628222', 16, 6, 3100000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1008, '浦东发展银行', 'SPDB', '东方卡', '东方卡', '84301', 16, 5, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1009, '浦东发展银行', 'SPDB', '东方卡', '东方卡', '84336', 16, 5, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1010, '浦东发展银行', 'SPDB', '东方卡', '东方卡', '622500', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1011, '浦东发展银行', 'SPDB', '东方卡', '东方卡', '84373', 16, 5, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1012, '浦东发展银行', 'SPDB', '公务卡普卡', '公务卡普卡', '628221', 16, 6, 3100000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1013, '浦东发展银行', 'SPDB', '东方卡', '东方卡', '84385', 16, 5, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1014, '浦东发展银行', 'SPDB', '东方卡', '东方卡', '84390', 16, 5, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1015, '浦东发展银行', 'SPDB', '东方卡', '东方卡', '87000', 16, 5, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1016, '浦东发展银行', 'SPDB', '东方卡', '东方卡', '87010', 16, 5, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1017, '浦东发展银行', 'SPDB', '东方卡', '东方卡', '87030', 16, 5, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1018, '浦东发展银行', 'SPDB', '东方卡', '东方卡', '87040', 16, 5, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1019, '浦东发展银行', 'SPDB', '东方卡', '东方卡', '84380', 16, 5, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1020, '浦东发展银行', 'SPDB', '东方卡', '东方卡', '984301', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1021, '浦东发展银行', 'SPDB', '东方卡', '东方卡', '984303', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1022, '浦东发展银行', 'SPDB', '东方卡', '东方卡', '84361', 16, 5, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1023, '浦东发展银行', 'SPDB', '东方卡', '东方卡', '87050', 16, 5, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1024, '浦东发展银行', 'SPDB', '浦发单币卡', '浦发单币卡', '622176', 16, 6, 3100000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1025, '浦东发展银行', 'SPDB', '浦发联名信用卡', '浦发联名信用卡', '622276', 16, 6, 3100000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1026, '浦东发展银行', 'SPDB', '浦发银联白金卡', '浦发银联白金卡', '622228', 16, 6, 3100000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1027, '浦东发展银行', 'SPDB', '轻松理财普卡', '轻松理财普卡', '621352', 16, 6, 3100000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1028, '浦东发展银行', 'SPDB', '轻松理财金卡（复合卡）', '轻松理财金卡（复合卡）', '621793', 16, 6, 3100000, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1029, '浦东发展银行', 'SPDB', '轻松理财白金卡（复合卡）', '轻松理财白金卡（复合卡）', '621795', 16, 6, 3100000,
        NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1030, '浦东发展银行', 'SPDB', '轻松理财钻石卡（复合卡）', '轻松理财钻石卡（复合卡）', '621796', 16, 6, 3100000,
        NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1031, '浦东发展银行', 'SPDB', '移动联名卡', '移动联名卡', '621351', 16, 6, 3100000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1032, '浦东发展银行', 'SPDB', '轻松理财消贷易卡', '轻松理财消贷易卡', '621390', 16, 6, 3100000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1033, '浦东发展银行', 'SPDB', '轻松理财普卡（复合卡）', '轻松理财普卡（复合卡）', '621792', 16, 6, 3100000, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1034, '浦东发展银行', 'SPDB', '贷记卡', '贷记卡', '625957', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1035, '浦东发展银行', 'SPDB', '贷记卡', '贷记卡', '625958', 16, 6, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1036, '浦东发展银行', 'SPDB', '东方借记卡（复合卡）', '东方借记卡（复合卡）', '621791', 16, 6, 3100000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1037, '浦东发展银行', 'SPDB', '东方卡', '东方卡', '84342', 16, 5, 3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1038, '浦东发展银行', 'SPDB', '电子现金卡（IC卡）', '电子现金卡（IC卡）', '620530', 19, 6, 3100000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1039, '浦东发展银行', 'SPDB', '移动浦发联名卡', '移动浦发联名卡', '625993', 16, 6, 3100000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1040, '浦东发展银行', 'SPDB', '东方-标准准贷记卡', '东方-标准准贷记卡', '622519', 16, 6, 3100000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1041, '浦东发展银行', 'SPDB', '中国移动浦发银行联名手机卡', '中国移动浦发银行联名手机卡', '625831', 16, 6,
        3100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1042, '恒丰银行', 'EGBANK', '九州IC卡', '九州IC卡', '623078', 19, 6, 3110000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1043, '恒丰银行', 'EGBANK', '九州借记卡(银联卡)', '九州借记卡(银联卡)', '622384', 17, 6, 3114560, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1044, '恒丰银行', 'EGBANK', '九州借记卡(银联卡)', '九州借记卡(银联卡)', '940034', 17, 6, 3114560, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1045, '天津市商业银行', '(NULL)', '津卡', '津卡', '6091201', 18, 7, 3131100, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1046, '天津市商业银行', '(NULL)', '银联卡(银联卡)', '银联卡(银联卡)', '940015', 18, 6, 3131100, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1047, '齐鲁银行', 'QLBANK', '齐鲁卡(银联卡)', '齐鲁卡(银联卡)', '940008', 19, 6, 3134500, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1048, '齐鲁银行', 'QLBANK', '齐鲁卡(银联卡)', '齐鲁卡(银联卡)', '622379', 19, 6, 3134510, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1049, '烟台商业银行', '(NULL)', '金通卡', '金通卡', '622886', 16, 6, 3134560, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1050, '潍坊银行', 'BANKWF', '鸢都卡(银联卡)', '鸢都卡(银联卡)', '622391', 16, 6, 3134580, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1051, '潍坊银行', 'BANKWF', '鸳都卡(银联卡)', '鸳都卡(银联卡)', '940072', 16, 6, 3134580, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1052, '临沂商业银行', '(NULL)', '沂蒙卡(银联卡)', '沂蒙卡(银联卡)', '622359', 19, 6, 3134730, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1053, '临沂商业银行', '(NULL)', '沂蒙卡(银联卡)', '沂蒙卡(银联卡)', '940066', 19, 6, 3134730, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1054, '日照市商业银行', '(NULL)', '黄海卡', '黄海卡', '622857', 19, 6, 3134732, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1055, '日照市商业银行', '(NULL)', '黄海卡(银联卡)', '黄海卡(银联卡)', '940065', 19, 6, 3134732, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1056, '浙商银行', 'CZBANK', '商卡', '商卡', '621019', 19, 6, 3160000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1057, '浙商银行', 'CZBANK', '商卡', '商卡', '622309', 19, 6, 3160000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1058, '浙商银行', 'CZBANK', '商卡', '商卡', '6223091100', 19, 10, 3161100, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1059, '浙商银行', 'CZBANK', '商卡', '商卡', '6223092900', 19, 10, 3162900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1060, '浙商银行', 'CZBANK', '商卡(银联卡)', '商卡(银联卡)', '6223093310', 19, 10, 3163310, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1061, '浙商银行', 'CZBANK', '商卡(银联卡)', '商卡(银联卡)', '6223093320', 19, 10, 3163320, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1062, '浙商银行', 'CZBANK', '商卡(银联卡)', '商卡(银联卡)', '6223093330', 19, 10, 3163330, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1063, '浙商银行', 'CZBANK', '商卡', '商卡', '6223093370', 19, 10, 3163370, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1064, '浙商银行', 'CZBANK', '商卡(银联卡)', '商卡(银联卡)', '6223093380', 19, 10, 3163380, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1065, '浙商银行', 'CZBANK', '商卡(银联卡)', '商卡(银联卡)', '6223096510', 19, 10, 3166510, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1066, '浙商银行', 'CZBANK', '商卡', '商卡', '6223097910', 19, 10, 3167910, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1067, '渤海银行', 'BOHAIB', '浩瀚金卡', '浩瀚金卡', '621268', 16, 6, 3170000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1068, '渤海银行', 'BOHAIB', '渤海银行公司借记卡', '渤海银行公司借记卡', '622684', 16, 6, 3170000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1069, '渤海银行', 'BOHAIB', '渤海银行借记卡', '渤海银行借记卡', '622884', 16, 6, 3170000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1070, '渤海银行', 'BOHAIB', '金融IC卡', '金融IC卡', '621453', 16, 6, 3170000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1071, '花旗银行(中国)有限公司', '(NULL)', '借记卡普卡', '借记卡普卡', '621062', 16, 6, 3190001, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1072, '花旗银行(中国)有限公司', '(NULL)', '借记卡高端卡', '借记卡高端卡', '621063', 16, 6, 3190001, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1073, '花旗中国', '(NULL)', '花旗礼享卡', '花旗礼享卡', '625076', 16, 6, 3190002, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1074, '花旗中国', '(NULL)', '花旗礼享卡', '花旗礼享卡', '625077', 16, 6, 3190002, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1075, '花旗中国', '(NULL)', '花旗礼享卡', '花旗礼享卡', '625074', 16, 6, 3190003, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1076, '花旗中国', '(NULL)', '花旗礼享卡', '花旗礼享卡', '625075', 16, 6, 3190003, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1077, '东亚银行', 'HKBEA', '紫荆卡', '紫荆卡', '622933', 19, 6, 3200000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1078, '东亚银行', 'HKBEA', '显卓理财卡', '显卓理财卡', '622938', 19, 6, 3200000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1079, '东亚银行', 'HKBEA', '借记卡', '借记卡', '623031', 19, 6, 3200000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1080, '汇丰银(中国)有限公司', '(NULL)', '汇丰中国卓越理财卡', '汇丰中国卓越理财卡', '622946', 16, 6, 3210000,
        NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1081, '渣打银行中国有限公司', '(NULL)', '渣打银行智通借记卡', '渣打银行智通借记卡', '622942', 16, 6, 3220000,
        NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1082, '渣打银行中国有限公司', '(NULL)', '渣打银行白金借记卡', '渣打银行白金借记卡', '622994', 16, 6, 3220000,
        NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1083, '星展银行', '(NULL)', '星展银行借记卡', '星展银行借记卡', '621016', 19, 6, 3240000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1084, '星展银行', '(NULL)', '星展银行借记卡', '星展银行借记卡', '621015', 19, 6, 3240000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1085, '恒生银行', '(NULL)', '恒生通财卡', '恒生通财卡', '622950', 16, 6, 3260000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1086, '恒生银行', '(NULL)', '恒生优越通财卡', '恒生优越通财卡', '622951', 16, 6, 3260000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1087, '友利银行(中国)有限公司', '(NULL)', '友利借记卡', '友利借记卡', '621060', 19, 6, 3270000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1088, '新韩银行', '(NULL)', '新韩卡', '新韩卡', '621072', 19, 6, 3280000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1089, '韩亚银行（中国）', '(NULL)', '韩亚卡', '韩亚卡', '621201', 16, 6, 3290000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1090, '华侨银行（中国）', '(NULL)', '卓锦借记卡', '卓锦借记卡', '621077', 16, 6, 3300000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1091, '永亨银行（中国）有限公司', '(NULL)', '永亨卡', '永亨卡', '621298', 18, 6, 3310000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1092, '南洋商业银行（中国）', '(NULL)', '借记卡', '借记卡', '621213', 19, 6, 3320000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1093, '南洋商业银行（中国）', '(NULL)', '财互通卡', '财互通卡', '621289', 19, 6, 3320000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1094, '南洋商业银行（中国）', '(NULL)', '财互通卡', '财互通卡', '621290', 19, 6, 3320000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1095, '南洋商业银行（中国）', '(NULL)', '财互通卡', '财互通卡', '621291', 19, 6, 3320000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1096, '南洋商业银行（中国）', '(NULL)', '财互通卡', '财互通卡', '621292', 19, 6, 3320000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1097, '法国兴业银行（中国）', '(NULL)', '法兴标准借记卡', '法兴标准借记卡', '621245', 16, 6, 3330001, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1098, '大华银行（中国）', '(NULL)', '尊享理财卡', '尊享理财卡', '621328', 19, 6, 3340000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1099, '大新银行（中国）', '(NULL)', '借记卡', '借记卡', '621277', 16, 6, 3350000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1100, '企业银行（中国）', '(NULL)', '瑞卡', '瑞卡', '621651', 19, 6, 3360000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1101, '上海银行', 'SHBANK', '单位借记卡', '单位借记卡', '621050', 18, 6, 4012900, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1102, '上海银行', 'SHBANK', '综合保险卡', '综合保险卡', '622172', 18, 6, 4012900, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1103, '上海银行', 'SHBANK', '申卡社保副卡(有折)', '申卡社保副卡(有折)', '622985', 18, 6, 4012900, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1104, '上海银行', 'SHBANK', '申卡社保副卡(无折)', '申卡社保副卡(无折)', '622987', 18, 6, 4012900, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1105, '上海银行', 'SHBANK', '首发纪念版IC卡', '首发纪念版IC卡', '620522', 18, 6, 4012900, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1106, '上海银行', 'SHBANK', '白金IC借记卡', '白金IC借记卡', '622267', 18, 6, 4012900, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1107, '上海银行', 'SHBANK', '慧通白金卡(配折)', '慧通白金卡(配折)', '622278', 18, 6, 4012900, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1108, '上海银行', 'SHBANK', '慧通白金卡(不配折)', '慧通白金卡(不配折)', '622279', 18, 6, 4012900, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1109, '上海银行', 'SHBANK', '申卡(银联卡)', '申卡(银联卡)', '622468', 18, 6, 4012900, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1110, '上海银行', 'SHBANK', '申卡借记卡', '申卡借记卡', '622892', 18, 6, 4012900, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1111, '上海银行', 'SHBANK', '银联申卡(银联卡)', '银联申卡(银联卡)', '940021', 18, 6, 4012900, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1112, '上海银行', 'SHBANK', '申卡贷记卡', '申卡贷记卡', '356827', 16, 6, 4012902, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1113, '上海银行', 'SHBANK', '申卡贷记卡', '申卡贷记卡', '356828', 16, 6, 4012902, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1114, '上海银行', 'SHBANK', 'J分期付款信用卡', 'J分期付款信用卡', '356830', 16, 6, 4012902, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1115, '上海银行', 'SHBANK', '申卡贷记卡', '申卡贷记卡', '402673', 16, 6, 4012902, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1116, '上海银行', 'SHBANK', '申卡贷记卡', '申卡贷记卡', '402674', 16, 6, 4012902, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1117, '上海银行', 'SHBANK', '上海申卡IC', '上海申卡IC', '438600', 16, 6, 4012902, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1118, '上海银行', 'SHBANK', '申卡贷记卡', '申卡贷记卡', '486466', 16, 6, 4012902, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1119, '上海银行', 'SHBANK', '申卡贷记卡普通卡', '申卡贷记卡普通卡', '519498', 16, 6, 4012902, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1120, '上海银行', 'SHBANK', '申卡贷记卡金卡', '申卡贷记卡金卡', '520131', 16, 6, 4012902, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1121, '上海银行', 'SHBANK', '万事达白金卡', '万事达白金卡', '524031', 16, 6, 4012902, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1122, '上海银行', 'SHBANK', '万事达星运卡', '万事达星运卡', '548838', 16, 6, 4012902, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1123, '上海银行', 'SHBANK', '申卡贷记卡金卡', '申卡贷记卡金卡', '622148', 16, 6, 4012902, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1124, '上海银行', 'SHBANK', '申卡贷记卡普通卡', '申卡贷记卡普通卡', '622149', 16, 6, 4012902, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1125, '上海银行', 'SHBANK', '安融卡', '安融卡', '622268', 16, 6, 4012902, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1126, '上海银行', 'SHBANK', '分期付款信用卡', '分期付款信用卡', '356829', 16, 6, 4012902, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1127, '上海银行', 'SHBANK', '信用卡', '信用卡', '622300', 16, 6, 4012902, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1128, '上海银行', 'SHBANK', '个人公务卡', '个人公务卡', '628230', 16, 6, 4012902, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1129, '上海银行', 'SHBANK', '安融卡', '安融卡', '622269', 16, 6, 4012902, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1130, '上海银行', 'SHBANK', '上海银行银联白金卡', '上海银行银联白金卡', '625099', 16, 6, 4012902, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1131, '上海银行', 'SHBANK', '贷记IC卡', '贷记IC卡', '625953', 16, 6, 4012902, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1132, '厦门银行', '(NULL)', '银鹭借记卡(银联卡)', '银鹭借记卡(银联卡)', '622393', 16, 6, 4023930, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1133, '厦门银行', '(NULL)', '银鹭卡', '银鹭卡', '6886592', 18, 7, 4023930, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1134, '厦门银行', '(NULL)', '银联卡(银联卡)', '银联卡(银联卡)', '940023', 16, 6, 4023930, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1135, '厦门银行', '(NULL)', '凤凰花卡', '凤凰花卡', '623019', 19, 6, 4023930, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1136, '厦门银行', '(NULL)', '凤凰花卡', '凤凰花卡', '621600', 19, 6, 4023930, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1137, '北京银行', 'BJBANK', '京卡借记卡', '京卡借记卡', '421317', 16, 6, 4031000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1138, '北京银行', 'BJBANK', '京卡贵宾白金卡', '京卡贵宾白金卡', '422161', 16, 6, 4031000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1139, '北京银行', 'BJBANK', '京卡(银联卡)', '京卡(银联卡)', '602969', 16, 6, 4031000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1140, '北京银行', 'BJBANK', '京卡贵宾金卡', '京卡贵宾金卡', '422160', 16, 6, 4031000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1141, '北京银行', 'BJBANK', '京卡借记卡', '京卡借记卡', '621030', 16, 6, 4031000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1142, '北京银行', 'BJBANK', '京卡', '京卡', '621420', 16, 6, 4031000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1143, '北京银行', 'BJBANK', '京卡', '京卡', '621468', 16, 6, 4031000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1144, '福建海峡银行', 'FJHXBC', '榕城卡(银联卡)', '榕城卡(银联卡)', '622388', 16, 6, 4053910, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1145, '福建海峡银行', 'FJHXBC', '福州市民卡', '福州市民卡', '621267', 18, 6, 4053910, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1146, '福建海峡银行', 'FJHXBC', '福州市民卡', '福州市民卡', '620043', 18, 6, 4053910, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1147, '福建海峡银行', 'FJHXBC', '海福卡（IC卡）', '海福卡（IC卡）', '623063', 18, 6, 4053910, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1148, '吉林银行', 'JLBANK', '君子兰一卡通(银联卡)', '君子兰一卡通(银联卡)', '622865', 19, 6, 4062410, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1149, '吉林银行', 'JLBANK', '君子兰卡(银联卡)', '君子兰卡(银联卡)', '940012', 16, 6, 4062410, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1150, '吉林银行', 'JLBANK', '长白山金融IC卡', '长白山金融IC卡', '623131', 19, 6, 4062410, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1151, '吉林银行', 'JLBANK', '信用卡', '信用卡', '622178', 16, 6, 4062418, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1152, '吉林银行', 'JLBANK', '信用卡', '信用卡', '622179', 16, 6, 4062418, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1153, '吉林银行', 'JLBANK', '公务卡', '公务卡', '628358', 16, 6, 4062418, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1154, '镇江市商业银行', '(NULL)', '金山灵通卡(银联卡)', '金山灵通卡(银联卡)', '622394', 16, 6, 4073140, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1155, '镇江市商业银行', '(NULL)', '金山灵通卡(银联卡)', '金山灵通卡(银联卡)', '940025', 16, 6, 4073140, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1156, '宁波银行', 'NBBANK', '银联标准卡', '银联标准卡', '621279', 16, 6, 4083320, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1157, '宁波银行', 'NBBANK', '汇通借记卡', '汇通借记卡', '622281', 16, 6, 4083320, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1158, '宁波银行', 'NBBANK', '汇通卡(银联卡)', '汇通卡(银联卡)', '622316', 16, 6, 4083320, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1159, '宁波银行', 'NBBANK', '明州卡', '明州卡', '940022', 16, 6, 4083320, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1160, '宁波银行', 'NBBANK', '汇通借记卡', '汇通借记卡', '621418', 19, 6, 4083320, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1161, '宁波银行', 'NBBANK', '汇通国际卡银联双币卡', '汇通国际卡银联双币卡', '512431', 16, 6, 4083329, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1162, '宁波银行', 'NBBANK', '汇通国际卡银联双币卡', '汇通国际卡银联双币卡', '520194', 16, 6, 4083329, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1163, '宁波银行', 'NBBANK', '汇通贷记卡', '汇通贷记卡', '622282', 16, 6, 4083329, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1164, '宁波银行', 'NBBANK', '汇通卡(银联卡)', '汇通卡(银联卡)', '622318', 16, 6, 4083329, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1165, '平安银行', 'SPABANK', '新磁条借记卡', '新磁条借记卡', '621626', 19, 6, 4100000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1166, '平安银行', 'SPABANK', '平安银行IC借记卡', '平安银行IC借记卡', '623058', 19, 6, 4100000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1167, '平安银行', 'SPABANK', '万事顺卡', '万事顺卡', '602907', 16, 6, 4105840, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1168, '平安银行', 'SPABANK', '平安银行借记卡', '平安银行借记卡', '622986', 16, 6, 4105840, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1169, '平安银行', 'SPABANK', '平安银行借记卡', '平安银行借记卡', '622989', 16, 6, 4105840, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1170, '平安银行', 'SPABANK', '万事顺借记卡', '万事顺借记卡', '622298', 16, 6, 4105840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1171, '焦作市商业银行', '(NULL)', '月季借记卡(银联卡)', '月季借记卡(银联卡)', '622338', 19, 6, 4115010, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1172, '焦作市商业银行', '(NULL)', '月季城市通(银联卡)', '月季城市通(银联卡)', '940032', 16, 6, 4115010, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1173, '温州银行', 'WZCB', '金鹿卡', '金鹿卡', '621977', 16, 6, 4123330, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1174, '广州银行', 'GCB', '羊城借记卡', '羊城借记卡', '603445', 19, 6, 4135810, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1175, '广州银行', 'GCB', '羊城借记卡(银联卡)', '羊城借记卡(银联卡)', '622467', 19, 6, 4135810, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1176, '广州银行', 'GCB', '羊城借记卡(银联卡)', '羊城借记卡(银联卡)', '940016', 19, 6, 4135810, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1177, '广州银行', 'GCB', '金融IC借记卡', '金融IC借记卡', '621463', 19, 6, 4135810, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1178, '汉口银行', 'HKB', '九通卡(银联卡)', '九通卡(银联卡)', '990027', 18, 6, 4145210, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1179, '汉口银行', 'HKB', '九通卡', '九通卡', '622325', 16, 6, 4145210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1180, '汉口银行', 'HKB', '借记卡', '借记卡', '623105', 16, 6, 4145210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1181, '汉口银行', 'HKB', '借记卡', '借记卡', '623029', 16, 6, 4145210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1182, '龙江银行', 'DAQINGB', '金鹤卡', '金鹤卡', '622475', 16, 6, 4162640, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1183, '盛京银行', 'SJBANK', '玫瑰卡', '玫瑰卡', '621244', 16, 6, 4170000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1184, '盛京银行', 'SJBANK', '玫瑰IC卡', '玫瑰IC卡', '623108', 16, 6, 4170001, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1185, '盛京银行', 'SJBANK', '玫瑰IC卡', '玫瑰IC卡', '623081', 16, 6, 4170001, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1186, '盛京银行', 'SJBANK', '玫瑰卡', '玫瑰卡', '566666', 18, 6, 4172210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1187, '盛京银行', 'SJBANK', '玫瑰卡', '玫瑰卡', '622455', 19, 6, 4172210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1188, '盛京银行', 'SJBANK', '玫瑰卡(银联卡)', '玫瑰卡(银联卡)', '940039', 19, 6, 4172210, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1189, '盛京银行', 'SJBANK', '玫瑰卡(银联卡)', '玫瑰卡(银联卡)', '622466', 16, 6, 4172211, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1190, '盛京银行', 'SJBANK', '盛京银行公务卡', '盛京银行公务卡', '628285', 16, 6, 4172211, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1191, '洛阳银行', '(NULL)', '都市一卡通(银联卡)', '都市一卡通(银联卡)', '622420', 17, 6, 4184930, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1192, '洛阳银行', '(NULL)', '都市一卡通(银联卡)', '都市一卡通(银联卡)', '940041', 17, 6, 4184930, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1193, '辽阳银行', '(NULL)', '新兴卡(银联卡)', '新兴卡(银联卡)', '622399', 17, 6, 4192310, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1194, '辽阳银行股份有限公司', '(NULL)', '新兴卡(银联卡)', '新兴卡(银联卡)', '940043', 17, 6, 4192310, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1195, '大连银行', 'DLB', '北方明珠卡', '北方明珠卡', '603708', 17, 6, 4202220, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1196, '大连银行', 'DLB', '人民币借记卡', '人民币借记卡', '622993', 19, 6, 4202220, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1197, '大连银行', 'DLB', '大连市社会保障卡', '大连市社会保障卡', '623069', 19, 6, 4202220, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1198, '大连银行', 'DLB', '金融IC借记卡', '金融IC借记卡', '623070', 19, 6, 4202220, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1199, '大连银行', 'DLB', '借记IC卡', '借记IC卡', '623172', 19, 6, 4202220, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1200, '大连银行', 'DLB', '借记IC卡', '借记IC卡', '623173', 19, 6, 4202220, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1201, '大连银行', 'DLB', '大连市商业银行贷记卡', '大连市商业银行贷记卡', '622383', 16, 6, 4202221, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1202, '大连银行', 'DLB', '大连市商业银行贷记卡', '大连市商业银行贷记卡', '622385', 16, 6, 4202221, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1203, '大连银行', 'DLB', '银联标准公务卡', '银联标准公务卡', '628299', 16, 6, 4202221, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1204, '苏州市商业银行', '(NULL)', '姑苏卡', '姑苏卡', '603506', 19, 6, 4213050, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1205, '河北银行', 'BHB', '如意借记卡(银联卡)', '如意借记卡(银联卡)', '622498', 19, 6, 4221210, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1206, '河北银行', 'BHB', '如意借记卡(银联卡)', '如意借记卡(银联卡)', '622499', 19, 6, 4221210, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1207, '河北银行', 'BHB', '借记IC卡', '借记IC卡', '623000', 19, 6, 4221210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1208, '河北银行', 'BHB', '如意卡(银联卡)', '如意卡(银联卡)', '940046', 19, 6, 4221210, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1209, '杭州商业银行', '(NULL)', '西湖卡', '西湖卡', '603367', 18, 6, 4233310, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1210, '杭州商业银行', '(NULL)', '西湖卡', '西湖卡', '622878', 18, 6, 4233310, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1211, '南京银行', 'NJCB', '梅花贷记卡(银联卡)', '梅花贷记卡(银联卡)', '622303', 16, 6, 4240001, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1212, '南京银行', 'NJCB', '梅花信用卡公务卡', '梅花信用卡公务卡', '628242', 16, 6, 4240001, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1213, '南京银行', 'NJCB', '梅花信用卡商务卡', '梅花信用卡商务卡', '622595', 16, 6, 4240001, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1214, '南京银行', 'NJCB', '梅花借记卡(银联卡)', '梅花借记卡(银联卡)', '622305', 16, 6, 4243010, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1215, '南京银行', 'NJCB', '白金卡', '白金卡', '621259', 16, 6, 4243010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1216, '南京银行', 'NJCB', '商务卡', '商务卡', '622596', 16, 6, 4243010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1217, '东莞市商业银行', '(NULL)', '万顺通卡(银联卡)', '万顺通卡(银联卡)', '622333', 16, 6, 4256020, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1218, '东莞市商业银行', '(NULL)', '万顺通卡(银联卡)', '万顺通卡(银联卡)', '940050', 16, 6, 4256020, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1219, '东莞市商业银行', '(NULL)', '万顺通借记卡', '万顺通借记卡', '621439', 19, 6, 4256020, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1220, '东莞市商业银行', '(NULL)', '社会保障卡', '社会保障卡', '623010', 19, 6, 4256020, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1221, '金华银行', 'JHBANK', '双龙借记卡', '双龙借记卡', '622449', 16, 6, 4263380, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1222, '金华银行', 'JHBANK', '双龙卡(银联卡)', '双龙卡(银联卡)', '940051', 16, 6, 4263380, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1223, '金华银行', 'JHBANK', '双龙贷记卡(银联卡)', '双龙贷记卡(银联卡)', '622450', 16, 6, 4263380, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1224, '金华银行', 'JHBANK', '公务卡', '公务卡', '628204', 16, 6, 4263380, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1225, '乌鲁木齐市商业银行', 'URMQCCB', '雪莲贷记卡(银联卡)', '雪莲贷记卡(银联卡)', '622476', 16, 6, 4270001,
        NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1226, '乌鲁木齐市商业银行', 'URMQCCB', '雪莲借记IC卡', '雪莲借记IC卡', '621751', 19, 6, 4270001, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1227, '乌鲁木齐市商业银行', 'URMQCCB', '乌鲁木齐市公务卡', '乌鲁木齐市公务卡', '628278', 16, 6, 4270001, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1228, '乌鲁木齐市商业银行', 'URMQCCB', '雪莲借记卡(银联卡)', '雪莲借记卡(银联卡)', '622143', 19, 6, 4278810,
        NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1229, '乌鲁木齐市商业银行', 'URMQCCB', '雪莲卡(银联卡)', '雪莲卡(银联卡)', '940001', 19, 6, 4278810, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1230, '乌鲁木齐市商业银行', 'URMQCCB', '雪莲借记IC卡', '雪莲借记IC卡', '621754', 19, 6, 4278810, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1231, '绍兴银行', 'SXCB', '兰花卡(银联卡)', '兰花卡(银联卡)', '622486', 16, 6, 4283370, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1232, '绍兴银行', 'SXCB', '兰花卡', '兰花卡', '603602', 18, 6, 4283370, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1233, '绍兴银行', 'SXCB', '兰花IC借记卡', '兰花IC借记卡', '623026', 16, 6, 4283371, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1234, '绍兴银行', 'SXCB', '社保IC借记卡', '社保IC借记卡', '623086', 16, 6, 4283371, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1235, '绍兴银行', 'SXCB', '兰花公务卡', '兰花公务卡', '628291', 16, 6, 4283379, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1236, '成都商业银行', '(NULL)', '锦程卡金卡', '锦程卡金卡', '622152', 19, 6, 4296510, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1237, '成都商业银行', '(NULL)', '锦程卡定活一卡通', '锦程卡定活一卡通', '622154', 19, 6, 4296510, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1238, '成都商业银行', '(NULL)', '锦程力诚联名卡', '锦程力诚联名卡', '622996', 19, 6, 4296510, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1239, '成都商业银行', '(NULL)', '锦程力诚联名卡', '锦程力诚联名卡', '622997', 19, 6, 4296510, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1240, '成都商业银行', '(NULL)', '锦程卡(银联卡)', '锦程卡(银联卡)', '940027', 19, 6, 4296510, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1241, '成都商业银行', '(NULL)', '锦程卡定活一卡通金卡', '锦程卡定活一卡通金卡', '622153', 19, 6, 4296510, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1242, '成都商业银行', '(NULL)', '锦程卡(银联卡)', '锦程卡(银联卡)', '622135', 19, 6, 4296510, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1243, '成都商业银行', '(NULL)', '芙蓉锦程天府通卡', '芙蓉锦程天府通卡', '621482', 19, 6, 4296510, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1244, '成都商业银行', '(NULL)', '芙蓉锦程福农卡', '芙蓉锦程福农卡', '621532', 19, 6, 4296510, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1245, '抚顺银行', 'FSCB', '绿叶卡(银联卡)', '绿叶卡(银联卡)', '622442', 17, 6, 4302240, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1246, '抚顺银行', 'FSCB', '启运卡', '启运卡', '622442', 19, 6, 4302240, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1247, '抚顺银行', 'FSCB', '绿叶卡(银联卡)', '绿叶卡(银联卡)', '940053', 18, 6, 4302240, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1248, '抚顺银行', 'FSCB', '借记IC卡', '借记IC卡', '623099', 19, 6, 4302248, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1249, '宜昌市商业银行', '(NULL)', '三峡卡(银联卡)', '三峡卡(银联卡)', '940055', 17, 6, 4325210, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1250, '宜昌市商业银行', '(NULL)', '信用卡(银联卡)', '信用卡(银联卡)', '622397', 16, 6, 4325261, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1251, '葫芦岛市商业银行', '(NULL)', '一通卡', '一通卡', '622398', 16, 6, 4332350, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1252, '葫芦岛市商业银行', '(NULL)', '一卡通(银联卡)', '一卡通(银联卡)', '940054', 16, 6, 4332350, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1253, '天津市商业银行', '(NULL)', '津卡', '津卡', '622331', 18, 6, 4341100, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1254, '天津市商业银行', '(NULL)', '津卡贷记卡(银联卡)', '津卡贷记卡(银联卡)', '622426', 16, 6, 4341100, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1255, '天津银行', 'TCCB', '商务卡', '商务卡', '628205', 16, 6, 4341101, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1256, '郑州银行', 'ZZBANK', '世纪一卡通(银联卡)', '世纪一卡通(银联卡)', '622421', 19, 6, 4354910, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1257, '郑州银行', 'ZZBANK', '世纪一卡通', '世纪一卡通', '940056', 17, 6, 4354910, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1258, '郑州银行', 'ZZBANK', '世纪一卡通', '世纪一卡通', '96828', 16, 5, 4354910, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1259, '宁夏银行', 'NXBANK', '宁夏银行公务卡', '宁夏银行公务卡', '628214', 16, 6, 4360010, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1260, '宁夏银行', 'NXBANK', '宁夏银行福农贷记卡', '宁夏银行福农贷记卡', '625529', 16, 6, 4360010, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1261, '宁夏银行', 'NXBANK', '如意卡(银联卡)', '如意卡(银联卡)', '622428', 16, 6, 4368710, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1262, '宁夏银行', 'NXBANK', '宁夏银行福农借记卡', '宁夏银行福农借记卡', '621529', 19, 6, 4368710, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1263, '宁夏银行', 'NXBANK', '如意借记卡', '如意借记卡', '622429', 19, 6, 4368710, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1264, '宁夏银行', 'NXBANK', '如意IC卡', '如意IC卡', '621417', 19, 6, 4368719, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1265, '宁夏银行', 'NXBANK', '宁夏银行如意借记卡', '宁夏银行如意借记卡', '623089', 19, 6, 4368719, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1266, '宁夏银行', 'NXBANK', '中国旅游卡', '中国旅游卡', '623200', 19, 6, 4368719, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1267, '珠海华润银行', '(NULL)', '万事顺卡', '万事顺卡', '622363', 19, 6, 4375850, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1268, '珠海华润银行', '(NULL)', '万事顺卡(银联卡)', '万事顺卡(银联卡)', '940048', 19, 6, 4375850, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1269, '齐商银行', 'ZBCB', '金达借记卡(银联卡)', '金达借记卡(银联卡)', '622311', 17, 6, 4384530, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1270, '齐商银行', 'ZBCB', '金达卡(银联卡)', '金达卡(银联卡)', '940057', 17, 6, 4384530, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1271, '锦州银行', 'BOJZ', '7777卡', '7777卡', '622990', 17, 6, 4392270, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1272, '锦州银行', 'BOJZ', '万通卡(银联卡)', '万通卡(银联卡)', '940003', 17, 6, 4392270, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1273, '徽商银行', 'HSBANK', '黄山卡', '黄山卡', '622877', 19, 6, 4403600, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1274, '徽商银行', 'HSBANK', '黄山卡', '黄山卡', '622879', 19, 6, 4403600, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1275, '徽商银行', 'HSBANK', '黄山卡', '黄山卡', '603601', 17, 6, 4403610, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1276, '徽商银行', 'HSBANK', '黄山卡(银联卡)', '黄山卡(银联卡)', '622137', 17, 6, 4403620, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1277, '徽商银行', 'HSBANK', '黄山卡(银联卡)', '黄山卡(银联卡)', '622327', 17, 6, 4403650, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1278, '徽商银行', 'HSBANK', '黄山卡(银联卡)', '黄山卡(银联卡)', '622340', 17, 6, 4403660, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1279, '徽商银行', 'HSBANK', '黄山卡(银联卡)', '黄山卡(银联卡)', '622366', 17, 6, 4403680, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1280, '重庆银行', 'CQBANK', '长江卡', '长江卡', '9896', 16, 4, 4416530, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1281, '重庆银行', 'CQBANK', '长江卡(银联卡)', '长江卡(银联卡)', '622134', 16, 6, 4416530, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1282, '重庆银行', 'CQBANK', '长江卡(银联卡)', '长江卡(银联卡)', '940018', 16, 6, 4416530, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1283, '重庆银行', 'CQBANK', '长江卡', '长江卡', '623016', 16, 6, 4416900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1284, '哈尔滨银行', '(NULL)', '福农借记卡', '福农借记卡', '621577', 19, 6, 4422610, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1285, '哈尔滨银行', '(NULL)', '丁香一卡通(银联卡)', '丁香一卡通(银联卡)', '940049', 18, 6, 4422610, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1286, '哈尔滨银行', '(NULL)', '丁香卡', '丁香卡', '622425', 19, 6, 4422610, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1287, '哈尔滨银行', '(NULL)', '丁香借记卡(银联卡)', '丁香借记卡(银联卡)', '622425', 17, 6, 4422610, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1288, '贵阳银行', '(NULL)', '甲秀银联借记卡', '甲秀银联借记卡', '622133', 19, 6, 4437010, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1289, '贵阳银行', '(NULL)', '甲秀卡', '甲秀卡', '888', 16, 3, 4437010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1290, '贵阳银行', '(NULL)', '一卡通', '一卡通', '621735', 19, 6, 4437010, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1291, '西安银行', 'XABANK', '福瑞卡', '福瑞卡', '622136', 18, 6, 4447910, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1292, '西安银行', 'XABANK', '金丝路卡', '金丝路卡', '622981', 18, 6, 4447910, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1293, '无锡市商业银行', '(NULL)', '太湖卡', '太湖卡', '60326500', 16, 8, 4453020, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1294, '无锡市商业银行', '(NULL)', '太湖卡', '太湖卡', '60326513', 18, 8, 4453020, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1295, '无锡市商业银行', '(NULL)', '太湖金保卡(银联卡)', '太湖金保卡(银联卡)', '622485', 18, 6, 4453020, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1296, '丹东银行', 'BODD', '银杏卡(银联卡)', '银杏卡(银联卡)', '622415', 16, 6, 4462260, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1297, '丹东银行', 'BODD', '银杏卡(银联卡)', '银杏卡(银联卡)', '940060', 16, 6, 4462260, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1298, '丹东银行', 'BODD', '借记IC卡', '借记IC卡', '623098', 19, 6, 4462262, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1299, '兰州银行', 'LZYH', '敦煌国际卡(银联卡)', '敦煌国际卡(银联卡)', '622139', 16, 6, 4478210, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1300, '兰州银行', 'LZYH', '敦煌卡', '敦煌卡', '940040', 16, 6, 4478210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1301, '兰州银行', 'LZYH', '敦煌卡', '敦煌卡', '621242', 19, 6, 4478210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1302, '兰州银行', 'LZYH', '敦煌卡', '敦煌卡', '621538', 19, 6, 4478210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1303, '兰州银行', 'LZYH', '敦煌金融IC卡', '敦煌金融IC卡', '621496', 19, 6, 4478210, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1304, '南昌银行', 'NCB', '南昌银行借记卡', '南昌银行借记卡', '621269', 16, 6, 4484210, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1305, '南昌银行', 'NCB', '金瑞卡', '金瑞卡', '622275', 16, 6, 4484210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1306, '南昌银行', 'NCB', '金瑞卡(银联卡)', '金瑞卡(银联卡)', '940006', 17, 6, 4484210, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1307, '晋商银行', 'JSB', '晋龙一卡通', '晋龙一卡通', '621216', 19, 6, 4491610, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1308, '晋商银行', 'JSB', '晋龙一卡通', '晋龙一卡通', '622465', 17, 6, 4491610, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1309, '晋商银行', 'JSB', '晋龙卡(银联卡)', '晋龙卡(银联卡)', '940031', 17, 6, 4491610, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1310, '青岛银行', 'QDCCB', '金桥通卡', '金桥通卡', '621252', 16, 6, 4504520, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1311, '青岛银行', 'QDCCB', '金桥卡(银联卡)', '金桥卡(银联卡)', '622146', 16, 6, 4504520, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1312, '青岛银行', 'QDCCB', '金桥卡(银联卡)', '金桥卡(银联卡)', '940061', 16, 6, 4504520, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1313, '青岛银行', 'QDCCB', '金桥卡', '金桥卡', '621419', 19, 6, 4504520, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1314, '青岛银行', 'QDCCB', '借记IC卡', '借记IC卡', '623170', 19, 6, 4504520, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1315, '吉林银行', 'JLBANK', '雾凇卡(银联卡)', '雾凇卡(银联卡)', '622440', 16, 6, 4512420, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1316, '吉林银行', 'JLBANK', '雾凇卡(银联卡)', '雾凇卡(银联卡)', '940047', 16, 6, 4512420, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1317, '南通商业银行', '(NULL)', '金桥卡', '金桥卡', '69580', 18, 5, 4523060, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1318, '南通商业银行', '(NULL)', '金桥卡(银联卡)', '金桥卡(银联卡)', '940017', 18, 6, 4523060, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1319, '南通商业银行', '(NULL)', '金桥卡(银联卡)', '金桥卡(银联卡)', '622418', 16, 6, 4523060, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1320, '九江银行', 'JJBANK', '庐山卡(银联卡)', '庐山卡(银联卡)', '622307', 19, 6, 4544240, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1321, '九江银行', 'JJBANK', '庐山卡', '庐山卡', '622162', 19, 6, 4544240, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1322, '鞍山银行', 'ASCB', '千山卡(银联卡)', '千山卡(银联卡)', '622413', 16, 6, 4562230, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1323, '鞍山银行', 'ASCB', '千山卡(银联卡)', '千山卡(银联卡)', '940002', 16, 6, 4562230, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1324, '秦皇岛市商业银行', '(NULL)', '秦卡', '秦卡', '621237', 19, 6, 4571260, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1325, '秦皇岛市商业银行', '(NULL)', '秦卡', '秦卡', '62249802', 19, 8, 4571260, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1326, '秦皇岛市商业银行', '(NULL)', '秦卡', '秦卡', '94004602', 19, 8, 4571260, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1327, '秦皇岛市商业银行', '(NULL)', '秦卡-IC卡', '秦卡-IC卡', '623003', 19, 6, 4571261, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1328, '青海银行', 'BOQH', '三江银行卡(银联卡)', '三江银行卡(银联卡)', '622310', 17, 6, 4588510, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1329, '青海银行', 'BOQH', '三江卡', '三江卡', '940068', 17, 6, 4588510, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1330, '台州银行', 'TZCB', '大唐贷记卡', '大唐贷记卡', '622321', 16, 6, 4593450, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1331, '台州银行', 'TZCB', '大唐准贷记卡', '大唐准贷记卡', '625001', 16, 6, 4593450, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1332, '台州银行', 'TZCB', '大唐卡(银联卡)', '大唐卡(银联卡)', '622427', 16, 6, 4593451, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1333, '台州银行', 'TZCB', '大唐卡', '大唐卡', '940069', 17, 6, 4593451, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1334, '台州银行', 'TZCB', '借记卡', '借记卡', '623039', 19, 6, 4593451, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1335, '台州银行', 'TZCB', '公务卡', '公务卡', '628273', 16, 6, 4593451, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1336, '盐城商行', '(NULL)', '金鹤卡(银联卡)', '金鹤卡(银联卡)', '940070', 16, 6, 4603110, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1337, '长沙银行', 'CSCB', '芙蓉卡', '芙蓉卡', '694301', 18, 6, 4615510, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1338, '长沙银行', 'CSCB', '芙蓉卡(银联卡)', '芙蓉卡(银联卡)', '940071', 19, 6, 4615510, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1339, '长沙银行', 'CSCB', '芙蓉卡(银联卡)', '芙蓉卡(银联卡)', '622368', 19, 6, 4615510, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1340, '长沙银行', 'CSCB', '芙蓉金融IC卡', '芙蓉金融IC卡', '621446', 19, 6, 4615510, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1341, '长沙银行', 'CSCB', '市民卡', '市民卡', '625901', 16, 6, 4615511, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1342, '长沙银行', 'CSCB', '芙蓉贷记卡', '芙蓉贷记卡', '622898', 16, 6, 4615511, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1343, '长沙银行', 'CSCB', '芙蓉贷记卡', '芙蓉贷记卡', '622900', 16, 6, 4615511, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1344, '长沙银行', 'CSCB', '公务卡钻石卡', '公务卡钻石卡', '628281', 16, 6, 4615511, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1345, '长沙银行', 'CSCB', '公务卡金卡', '公务卡金卡', '628282', 16, 6, 4615511, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1346, '长沙银行', 'CSCB', '银联标准钻石卡', '银联标准钻石卡', '622806', 16, 6, 4615511, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1347, '长沙银行', 'CSCB', '公务卡普卡', '公务卡普卡', '628283', 16, 6, 4615511, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1348, '长沙银行', 'CSCB', '市民卡', '市民卡', '620519', 19, 6, 4615511, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1349, '赣州银行', 'GZB', '长征卡', '长征卡', '622967', 19, 6, 4634280, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1350, '赣州银行', 'GZB', '长征卡(银联卡)', '长征卡(银联卡)', '940073', 19, 6, 4634280, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1351, '泉州银行', '(NULL)', '海峡银联卡(银联卡)', '海峡银联卡(银联卡)', '622370', 19, 6, 4643970, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1352, '泉州银行', '(NULL)', '海峡储蓄卡', '海峡储蓄卡', '683970', 18, 6, 4643970, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1353, '泉州银行', '(NULL)', '海峡银联卡(银联卡)', '海峡银联卡(银联卡)', '940074', 18, 6, 4643970, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1354, '泉州银行', '(NULL)', '海峡卡', '海峡卡', '621437', 19, 6, 4643970, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1355, '泉州银行', '(NULL)', '公务卡', '公务卡', '628319', 16, 6, 4643970, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1356, '营口市商业银行', '(NULL)', '辽河一卡通(银联卡)', '辽河一卡通(银联卡)', '622400', 17, 6, 4652280, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1357, '昆明商业银行', '(NULL)', '春城卡(银联卡)', '春城卡(银联卡)', '990871', 18, 6, 4667310, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1358, '昆明商业银行', '(NULL)', '春城卡(银联卡)', '春城卡(银联卡)', '622308', 17, 6, 4667310, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1359, '昆明商业银行', '(NULL)', '富滇IC卡（复合卡）', '富滇IC卡（复合卡）', '621415', 19, 6, 4667310, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1360, '阜新银行', 'FXCB', '金通卡(银联卡)', '金通卡(银联卡)', '622126', 18, 6, 4672290, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1361, '嘉兴市商业银行', '(NULL)', '南湖借记卡(银联卡)', '南湖借记卡(银联卡)', '622132', 16, 6, 4703350, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1362, '廊坊银行', 'LANGFB', '白金卡', '白金卡', '621340', 16, 6, 4721460, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1363, '廊坊银行', 'LANGFB', '金卡', '金卡', '621341', 16, 6, 4721460, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1364, '廊坊银行', 'LANGFB', '银星卡(银联卡)', '银星卡(银联卡)', '622140', 16, 6, 4721460, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1365, '泰隆城市信用社', '(NULL)', '泰隆卡(银联卡)', '泰隆卡(银联卡)', '622141', 16, 6, 4733450, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1366, '内蒙古银行', 'H3CB', '百灵卡(银联卡)', '百灵卡(银联卡)', '622147', 19, 6, 4741910, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1367, '内蒙古银行', 'H3CB', '成吉思汗卡', '成吉思汗卡', '621633', 19, 6, 4741910, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1368, '湖州市商业银行', 'HZCCB', '百合卡', '百合卡', '622301', 19, 6, 4753360, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1369, '沧州银行', '(NULL)', '狮城卡', '狮城卡', '621266', 19, 6, 4761430, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1370, '沧州银行', '(NULL)', '狮城卡', '狮城卡', '62249804', 19, 8, 4761430, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1371, '沧州银行', '(NULL)', '狮城卡', '狮城卡', '94004604', 19, 8, 4761430, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1372, '沧州银行', '(NULL)', '狮城卡', '狮城卡', '621422', 19, 6, 4761431, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1373, '南宁市商业银行', '(NULL)', '桂花卡(银联卡)', '桂花卡(银联卡)', '622335', 16, 6, 4786110, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1374, '包商银行', 'BSB', '雄鹰卡(银联卡)', '雄鹰卡(银联卡)', '622336', 17, 6, 4791920, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1375, '包商银行', 'BSB', '包头市商业银行借记卡', '包头市商业银行借记卡', '622165', 16, 6, 4791921, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1376, '包商银行', 'BSB', '雄鹰贷记卡', '雄鹰贷记卡', '622315', 16, 6, 4791921, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1377, '包商银行', 'BSB', '贷记卡', '贷记卡', '625950', 16, 6, 4791921, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1378, '包商银行', 'BSB', '包商银行内蒙古自治区公务卡', '包商银行内蒙古自治区公务卡', '628295', 16, 6, 4791921,
        NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1379, '包商银行', 'BSB', '借记卡', '借记卡', '621760', 17, 6, 4791922, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1380, '连云港市商业银行', '(NULL)', '金猴神通借记卡', '金猴神通借记卡', '622337', 16, 6, 4803070, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1381, '威海商业银行', '(NULL)', '通达卡(银联卡)', '通达卡(银联卡)', '622411', 16, 6, 4814650, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1382, '威海市商业银行', 'WHCCB', '通达借记IC卡', '通达借记IC卡', '623102', 16, 6, 4814659, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1383, '攀枝花市商业银行', '(NULL)', '攀枝花卡(银联卡)', '攀枝花卡(银联卡)', '622342', 19, 6, 4836560, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1384, '绵阳市商业银行', '(NULL)', '科技城卡(银联卡)', '科技城卡(银联卡)', '622367', 19, 6, 4856590, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1385, '泸州市商业银行', '(NULL)', '酒城卡(银联卡)', '酒城卡(银联卡)', '622392', 19, 6, 4866570, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1386, '大同市商业银行', '(NULL)', '云冈卡(银联卡)', '云冈卡(银联卡)', '622395', 19, 6, 4871620, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1387, '三门峡银行', 'SCCB', '天鹅卡(银联卡)', '天鹅卡(银联卡)', '622441', 16, 6, 4885050, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1388, '广东南粤银行', '(NULL)', '南珠卡(银联卡)', '南珠卡(银联卡)', '622448', 16, 6, 4895910, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1389, '张家口市商业银行', 'ZJKCCB', '如意借记卡', '如意借记卡', '622982', 19, 6, 4901380, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1390, '张家口市商业银行', 'ZJKCCB', '好运IC借记卡', '好运IC借记卡', '621413', 19, 6, 4901381, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1391, '桂林市商业银行', '(NULL)', '漓江卡(银联卡)', '漓江卡(银联卡)', '622856', 17, 6, 4916170, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1392, '龙江银行', 'DAQINGB', '福农借记卡', '福农借记卡', '621037', 19, 6, 4922600, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1393, '龙江银行', 'DAQINGB', '联名借记卡', '联名借记卡', '621097', 19, 6, 4922600, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1394, '龙江银行', 'DAQINGB', '福农借记卡', '福农借记卡', '621588', 19, 6, 4922600, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1395, '龙江银行', 'DAQINGB', '中国旅游卡', '中国旅游卡', '62321601', 19, 8, 4922600, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1396, '龙江银行', 'DAQINGB', '玉兔卡(银联卡)', '玉兔卡(银联卡)', '622860', 16, 6, 4922690, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1397, '江苏长江商业银行', '(NULL)', '长江卡', '长江卡', '622870', 16, 6, 4933120, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1398, '徐州市商业银行', '(NULL)', '彭城借记卡(银联卡)', '彭城借记卡(银联卡)', '622866', 16, 6, 4943030, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1399, '柳州银行', '(NULL)', '龙城卡', '龙城卡', '622292', 18, 6, 4956140, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1400, '柳州银行', '(NULL)', '龙城卡VIP卡', '龙城卡VIP卡', '622880', 16, 6, 4956140, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1401, '柳州银行', '(NULL)', '龙城致富卡', '龙城致富卡', '622881', 16, 6, 4956140, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1402, '柳州银行', '(NULL)', '龙城卡', '龙城卡', '622291', 18, 6, 4956140, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1403, '柳州银行', '(NULL)', '龙城IC卡', '龙城IC卡', '621412', 18, 6, 4956140, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1404, '南充市商业银行', 'CGNB', '熊猫团团卡', '熊猫团团卡', '622897', 19, 6, 4966730, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1405, '莱商银行', 'LSBANK', '银联标准卡', '银联标准卡', '628279', 16, 6, 4974634, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1406, '莱芜银行', '(NULL)', '金凤卡', '金凤卡', '622864', 16, 6, 4974634, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1407, '莱商银行', 'LSBANK', '借记IC卡', '借记IC卡', '621403', 19, 6, 4974790, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1408, '德阳银行', '(NULL)', '锦程卡定活一卡通', '锦程卡定活一卡通', '622561', 19, 6, 4986580, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1409, '德阳银行', '(NULL)', '锦程卡定活一卡通金卡', '锦程卡定活一卡通金卡', '622562', 19, 6, 4986580, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1410, '德阳银行', '(NULL)', '锦程卡定活一卡通', '锦程卡定活一卡通', '622563', 19, 6, 4986580, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1411, '唐山市商业银行', '(NULL)', '唐山市城通卡', '唐山市城通卡', '622167', 19, 6, 4991240, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1412, '六盘水商行', '(NULL)', '凉都卡', '凉都卡', '622508', 16, 6, 5007020, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1413, '曲靖市商业银行', '(NULL)', '珠江源卡', '珠江源卡', '622777', 16, 6, 5027360, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1414, '曲靖市商业银行', '(NULL)', '珠江源卡', '珠江源卡', '621497', 16, 6, 5027361, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1415, '晋城银行', '(NULL)', '珠联璧合卡', '珠联璧合卡', '622532', 19, 6, 5031680, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1416, '东莞商行', '(NULL)', '恒通贷记卡', '恒通贷记卡', '622888', 16, 6, 5056020, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1417, '温州银行', 'WZCB', '金鹿信用卡', '金鹿信用卡', '622868', 16, 6, 5063330, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1418, '温州银行', 'WZCB', '金鹿信用卡', '金鹿信用卡', '622899', 16, 6, 5063330, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1419, '温州银行', 'WZCB', '金鹿公务卡', '金鹿公务卡', '628255', 16, 6, 5063330, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1420, '汉口银行', 'HKB', '汉口银行贷记卡', '汉口银行贷记卡', '622566', 16, 6, 5075210, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1421, '汉口银行', 'HKB', '汉口银行贷记卡', '汉口银行贷记卡', '622567', 16, 6, 5075210, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1422, '汉口银行', 'HKB', '九通香港旅游贷记普卡', '九通香港旅游贷记普卡', '622625', 16, 6, 5075210, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1423, '汉口银行', 'HKB', '九通香港旅游贷记金卡', '九通香港旅游贷记金卡', '622626', 16, 6, 5075210, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1424, '汉口银行', 'HKB', '贷记卡', '贷记卡', '625946', 16, 6, 5075210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1425, '汉口银行', 'HKB', '九通公务卡', '九通公务卡', '628200', 16, 6, 5075210, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1426, '江苏银行', 'JSBANK', '聚宝借记卡', '聚宝借记卡', '621076', 19, 6, 5083000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1427, '江苏银行', 'JSBANK', '月季卡', '月季卡', '504923', 16, 6, 5083000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1428, '江苏银行', 'JSBANK', '紫金卡', '紫金卡', '622173', 19, 6, 5083000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1429, '江苏银行', 'JSBANK', '绿扬卡(银联卡)', '绿扬卡(银联卡)', '622422', 16, 6, 5083000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1430, '江苏银行', 'JSBANK', '月季卡(银联卡)', '月季卡(银联卡)', '622447', 16, 6, 5083000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1431, '江苏银行', 'JSBANK', '九州借记卡(银联卡)', '九州借记卡(银联卡)', '622131', 19, 6, 5083000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1432, '江苏银行', 'JSBANK', '月季卡(银联卡)', '月季卡(银联卡)', '940076', 16, 6, 5083000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1433, '江苏银行', 'JSBANK', '聚宝惠民福农卡', '聚宝惠民福农卡', '621579', 19, 6, 5083000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1434, '江苏银行', 'JSBANK', '江苏银行聚宝IC借记卡', '江苏银行聚宝IC借记卡', '622876', 19, 6, 5083000, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1435, '平安银行', 'SPABANK', '白金信用卡', '白金信用卡', '531659', 16, 6, 5105840, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1436, '平安银行', 'SPABANK', '白金信用卡', '白金信用卡', '622157', 16, 6, 5105840, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1437, '平安银行', 'SPABANK', '平安银行信用卡', '平安银行信用卡', '528020', 16, 6, 5105840, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1438, '平安银行', 'SPABANK', '平安银行信用卡', '平安银行信用卡', '622155', 16, 6, 5105840, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1439, '平安银行', 'SPABANK', '平安银行信用卡', '平安银行信用卡', '622156', 16, 6, 5105840, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1440, '平安银行', 'SPABANK', '平安银行信用卡', '平安银行信用卡', '526855', 16, 6, 5105840, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1441, '平安银行', 'SPABANK', '信用卡', '信用卡', '356869', 16, 6, 5105840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1442, '平安银行', 'SPABANK', '信用卡', '信用卡', '356868', 16, 6, 5105840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1443, '平安银行', 'SPABANK', '平安中国旅游信用卡', '平安中国旅游信用卡', '625360', 16, 6, 5105840, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1444, '平安银行', 'SPABANK', '平安中国旅游白金信用卡', '平安中国旅游白金信用卡', '625361', 16, 6, 5105840, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1445, '平安银行', 'SPABANK', '公务卡', '公务卡', '628296', 16, 6, 5105840, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1446, '长治市商业银行', '(NULL)', '长治商行银联晋龙卡', '长治商行银联晋龙卡', '622962', 17, 6, 5121660, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1447, '承德市商业银行', '(NULL)', '热河卡', '热河卡', '622936', 19, 6, 5131410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1448, '德州银行', 'DZBANK', '长河借记卡', '长河借记卡', '622937', 19, 6, 5154680, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1449, '遵义市商业银行', 'ZYCBANK', '社保卡', '社保卡', '621460', 19, 6, 5167030, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1450, '遵义市商业银行', 'ZYCBANK', '尊卡', '尊卡', '622939', 17, 6, 5167030, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1451, '邯郸市商业银行', '(NULL)', '邯银卡', '邯银卡', '622960', 18, 6, 5171270, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1452, '安顺市商业银行', '(NULL)', '黄果树福农卡', '黄果树福农卡', '621591', 16, 6, 5197110, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1453, '安顺市商业银行', '(NULL)', '黄果树借记卡', '黄果树借记卡', '622961', 16, 6, 5197117, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1454, '江苏银行', 'JSBANK', '紫金信用卡(公务卡)', '紫金信用卡(公务卡)', '628210', 16, 6, 5213000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1455, '江苏银行', 'JSBANK', '紫金信用卡', '紫金信用卡', '622283', 16, 6, 5213000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1456, '江苏银行', 'JSBANK', '天翼联名信用卡', '天翼联名信用卡', '625902', 16, 6, 5213000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1457, '平凉市商业银行', '(NULL)', '广成卡', '广成卡', '621010', 16, 6, 5238333, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1458, '玉溪市商业银行', 'YXCCB', '红塔卡', '红塔卡', '622980', 19, 6, 5247410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1459, '浙江民泰商业银行', 'MTBANK', '金融IC卡', '金融IC卡', '621726', 19, 6, 5253450, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1460, '浙江民泰商业银行', 'MTBANK', '民泰借记卡', '民泰借记卡', '621088', 19, 6, 5253450, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1461, '浙江民泰商业银行', 'MTBANK', '金融IC卡C卡', '金融IC卡C卡', '620517', 19, 6, 5253450, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1462, '浙江民泰商业银行', 'MTBANK', '银联标准普卡金卡', '银联标准普卡金卡', '622740', 16, 6, 5253454, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1463, '浙江民泰商业银行', 'MTBANK', '商惠通', '商惠通', '625036', 16, 6, 5253454, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1464, '上饶市商业银行', '(NULL)', '三清山卡', '三清山卡', '621014', 16, 6, 5264330, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1465, '东营银行', '(NULL)', '胜利卡', '胜利卡', '621004', 18, 6, 5274550, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1466, '泰安市商业银行', 'TACCB', '岱宗卡', '岱宗卡', '622972', 19, 6, 5284630, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1467, '泰安市商业银行', 'TACCB', '市民一卡通', '市民一卡通', '623196', 19, 6, 5284630, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1468, '浙江稠州商业银行', 'CZCB', '义卡', '义卡', '621028', 16, 6, 5303380, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1469, '浙江稠州商业银行', 'CZCB', '公务卡', '公务卡', '628250', 16, 6, 5303387, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1470, '乌海银行', '(NULL)', '狮城借记卡', '狮城借记卡', '622973', 19, 6, 5311930, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1471, '自贡市商业银行', 'ZGCCB', '锦程卡', '锦程卡', '621070', 19, 6, 5326560, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1472, '龙江银行', 'DAQINGB', '万通卡', '万通卡', '622977', 19, 6, 5332740, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1473, '鄂尔多斯银行', 'ORBANK', '天骄卡', '天骄卡', '622978', 19, 6, 5342050, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1474, '鄂尔多斯银行', 'ORBANK', '天骄公务卡', '天骄公务卡', '628253', 16, 6, 5342050, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1475, '鹤壁银行', '(NULL)', '鹤卡', '鹤卡', '622979', 19, 6, 5354970, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1476, '许昌市商业银行', '(NULL)', '连城卡', '连城卡', '621035', 19, 6, 5365030, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1477, '济宁银行', 'JNBANK', '儒商卡', '儒商卡', '621200', 19, 6, 5374610, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1478, '铁岭银行', '(NULL)', '龙凤卡', '龙凤卡', '621038', 19, 6, 5392330, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1479, '乐山市商业银行', 'LSCCB', '大福卡', '大福卡', '621086', 19, 6, 5406650, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1480, '长安银行', '(NULL)', '长长卡', '长长卡', '621296', 19, 6, 5417900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1481, '长安银行', '(NULL)', '借记IC卡', '借记IC卡', '621448', 19, 6, 5417900, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1482, '宝鸡商行', '(NULL)', '姜炎卡', '姜炎卡', '621044', 19, 6, 5417930, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1483, '重庆三峡银行', 'CCQTGB', '财富人生卡', '财富人生卡', '622945', 16, 6, 5426900, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1484, '重庆三峡银行', 'CCQTGB', '借记卡', '借记卡', '621755', 16, 6, 5426900, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1485, '石嘴山银行', 'SZSBK', '麒麟借记卡', '麒麟借记卡', '622940', 19, 6, 5438720, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1486, '石嘴山银行', 'SZSBK', '麒麟公务卡', '麒麟公务卡', '628355', 16, 6, 5438729, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1487, '盘锦市商业银行', '(NULL)', '鹤卡', '鹤卡', '621089', 19, 6, 5442320, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1488, '昆仑银行', 'KLB', '瑞卡', '瑞卡', '621029', 19, 6, 5478820, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1489, '昆仑银行', 'KLB', '金融IC卡', '金融IC卡', '621766', 19, 6, 5478820, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1490, '平顶山银行', 'BOP', '佛泉卡', '佛泉卡', '621071', 19, 6, 5484950, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1491, '朝阳银行', 'BOCY', '鑫鑫通卡', '鑫鑫通卡', '621074', 19, 6, 5492340, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1492, '朝阳银行', 'BOCY', '红山卡', '红山卡', '623030', 19, 6, 5492341, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1493, '宁波东海银行', '(NULL)', '绿叶卡', '绿叶卡', '621345', 18, 6, 5503320, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1494, '遂宁市商业银行', '(NULL)', '锦程卡', '锦程卡', '621090', 19, 6, 5516620, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1495, '保定银行', '(NULL)', '直隶卡', '直隶卡', '621091', 19, 6, 5521340, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1496, '邢台银行', 'XTB', '金牛卡', '金牛卡', '621238', 19, 6, 5541310, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1497, '凉山州商业银行', '(NULL)', '锦程卡', '锦程卡', '621057', 19, 6, 5556840, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1498, '漯河商行', '(NULL)', '福卡', '福卡', '621075', 19, 6, 5565040, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1499, '漯河商行', '(NULL)', '福源卡', '福源卡', '623037', 19, 6, 5565040, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1500, '达州市商业银行', '(NULL)', '锦程卡', '锦程卡', '621233', 19, 6, 5576750, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1501, '新乡市商业银行', '(NULL)', '新卡', '新卡', '621235', 18, 6, 5584980, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1502, '晋中市商业银行', 'JZBANK', '九州方圆借记卡', '九州方圆借记卡', '621223', 19, 6, 5591750, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1503, '晋中市商业银行', 'JZBANK', '九州方圆卡', '九州方圆卡', '621780', 19, 6, 5591750, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1504, '驻马店市商业银行', '(NULL)', '驿站卡', '驿站卡', '621221', 19, 6, 5605110, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1505, '衡水市商业银行', '(NULL)', '金鼎卡', '金鼎卡', '621239', 19, 6, 5611480, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1506, '周口银行', 'BOZK', '如愿卡', '如愿卡', '621271', 19, 6, 5625080, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1507, '阳泉市商业银行', '(NULL)', '金鼎卡', '金鼎卡', '621272', 16, 6, 5631650, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1508, '阳泉市商业银行', '(NULL)', '金鼎卡', '金鼎卡', '621738', 16, 6, 5631650, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1509, '宜宾市商业银行', 'YBCCB', '锦程卡', '锦程卡', '621273', 19, 6, 5646710, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1510, '库尔勒市商业银行', 'KORLABANK', '孔雀胡杨卡', '孔雀胡杨卡', '621263', 18, 6, 5658880, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1511, '雅安市商业银行', '(NULL)', '锦城卡', '锦城卡', '621325', 19, 6, 5666770, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1512, '商丘商行', '(NULL)', '百汇卡', '百汇卡', '621337', 19, 6, 5675060, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1513, '安阳银行', 'AYCB', '安鼎卡', '安鼎卡', '621327', 19, 6, 5684960, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1514, '信阳银行', 'XYBANK', '信阳卡', '信阳卡', '621753', 19, 6, 5695150, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1515, '华融湘江银行', 'HRXJB', '华融卡', '华融卡', '621366', 19, 6, 5705500, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1516, '华融湘江银行', 'HRXJB', '华融卡', '华融卡', '621388', 19, 6, 5705500, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1517, '营口沿海银行', '(NULL)', '祥云借记卡', '祥云借记卡', '621348', 19, 6, 5722280, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1518, '景德镇商业银行', '(NULL)', '瓷都卡', '瓷都卡', '621359', 18, 6, 5734220, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1519, '湖北银行', 'HBC', '金牛卡', '金牛卡', '621217', 18, 6, 5755200, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1520, '湖北银行', 'HBC', '汉江卡', '汉江卡', '622959', 19, 6, 5755200, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1521, '湖北银行', 'HBC', '借记卡', '借记卡', '621270', 18, 6, 5755200, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1522, '湖北银行', 'HBC', '三峡卡', '三峡卡', '622396', 17, 6, 5755200, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1523, '湖北银行', 'HBC', '至尊卡', '至尊卡', '622511', 17, 6, 5755200, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1524, '湖北银行', 'HBC', '金融IC卡', '金融IC卡', '623076', 19, 6, 5755201, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1525, '西藏银行', '(NULL)', '借记IC卡', '借记IC卡', '621391', 19, 6, 5767700, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1526, '新疆汇和银行', '(NULL)', '汇和卡', '汇和卡', '621339', 19, 6, 5778981, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1527, '广东华兴银行', '(NULL)', '借记卡', '借记卡', '621469', 19, 6, 5785800, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1528, '广东华兴银行', '(NULL)', '华兴银联公司卡', '华兴银联公司卡', '621625', 19, 6, 5785800, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1529, '濮阳银行', '(NULL)', '龙翔卡', '龙翔卡', '621601', 19, 6, 5795020, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1530, '宁波通商银行', '(NULL)', '借记卡', '借记卡', '621655', 19, 6, 5803320, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1531, '甘肃银行', '(NULL)', '神舟兴陇借记卡', '神舟兴陇借记卡', '621636', 19, 6, 5818200, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1532, '平安银行', 'SPABANK', '一账通借贷合一钻石卡', '一账通借贷合一钻石卡', '627069', 16, 6, 6105840, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1533, '平安银行', 'SPABANK', '一账通借贷合一白金卡', '一账通借贷合一白金卡', '627068', 16, 6, 6105840, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1534, '平安银行', 'SPABANK', '一账通借贷合一卡普卡', '一账通借贷合一卡普卡', '627066', 16, 6, 6105840, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1535, '平安银行', 'SPABANK', '一账通借贷合一卡金卡', '一账通借贷合一卡金卡', '627067', 16, 6, 6105840, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1536, '盛京银行', 'SJBANK', '医保卡', '医保卡', '622955', 16, 6, 804172210, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1537, '上海农商银行', '(NULL)', '如意卡(银联卡)', '如意卡(银联卡)', '622478', 16, 6, 14012900, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1538, '上海农商银行', '(NULL)', '如意卡(银联卡)', '如意卡(银联卡)', '940013', 16, 6, 14012900, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1539, '上海农商银行', '(NULL)', '鑫通卡', '鑫通卡', '621495', 16, 6, 14012900, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1540, '昆山农信社', '(NULL)', '江通卡(银联卡)', '江通卡(银联卡)', '622443', 19, 6, 14023052, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1541, '昆山农信社', '(NULL)', '银联汇通卡(银联卡)', '银联汇通卡(银联卡)', '940029', 19, 6, 14023052, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1542, '常熟市农村商业银行', '(NULL)', '粒金贷记卡(银联卡)', '粒金贷记卡(银联卡)', '622462', 16, 6, 14030001,
        NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1543, '常熟市农村商业银行', '(NULL)', '公务卡', '公务卡', '628272', 16, 6, 14030001, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1544, '常熟市农村商业银行', '(NULL)', '粒金准贷卡', '粒金准贷卡', '625101', 16, 6, 14030001, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1545, '常熟农村商业银行', 'CSRCB', '粒金卡', '粒金卡', '603694', 19, 6, 14033055, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1546, '常熟农村商业银行', 'CSRCB', '粒金借记卡(银联卡)', '粒金借记卡(银联卡)', '622323', 19, 6, 14033055, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1547, '常熟农村商业银行', 'CSRCB', '粒金卡(银联卡)', '粒金卡(银联卡)', '9400301', 19, 7, 14033055, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1548, '深圳农村商业银行', 'SRCB', '信通卡(银联卡)', '信通卡(银联卡)', '622128', 16, 6, 14045840, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1549, '深圳农村商业银行', 'SRCB', '信通商务卡(银联卡)', '信通商务卡(银联卡)', '622129', 16, 6, 14045840, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1550, '深圳农村商业银行', 'SRCB', '信通卡', '信通卡', '623035', 16, 6, 14045840, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1551, '广州农村商业银行', '(NULL)', '麒麟卡', '麒麟卡', '909810', 18, 6, 14055810, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1552, '广州农村商业银行', '(NULL)', '麒麟卡(银联卡)', '麒麟卡(银联卡)', '940035', 18, 6, 14055810, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1553, '广州农村商业银行', '(NULL)', '福农太阳卡', '福农太阳卡', '621522', 18, 6, 14055810, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1554, '广州农村商业银行', '(NULL)', '麒麟储蓄卡', '麒麟储蓄卡', '622439', 18, 6, 14055810, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1555, '广东南海农村商业银行', '(NULL)', '盛通卡', '盛通卡', '622271', 18, 6, 14075882, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1556, '广东南海农村商业银行', '(NULL)', '盛通卡(银联卡)', '盛通卡(银联卡)', '940037', 18, 6, 14075882, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1557, '佛山顺德农村商业银行', '(NULL)', '恒通卡(银联卡)', '恒通卡(银联卡)', '940038', 16, 6, 14085883, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1558, '佛山顺德农村商业银行', '(NULL)', '恒通卡', '恒通卡', '985262', 16, 6, 14085883, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1559, '佛山顺德农村商业银行', '(NULL)', '恒通卡(银联卡)', '恒通卡(银联卡)', '622322', 16, 6, 14085883, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1560, '昆明农联社', '(NULL)', '金碧卡', '金碧卡', '0', 18, 6, 14097310, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1561, '昆明农联社', '(NULL)', '金碧白金卡', '金碧白金卡', '621017', 19, 6, 14097310, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1562, '昆明农联社', '(NULL)', '金碧一卡通(银联卡)', '金碧一卡通(银联卡)', '622369', 16, 6, 14097310, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1563, '昆明农联社', '(NULL)', '银联卡(银联卡)', '银联卡(银联卡)', '940042', 18, 6, 14097310, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1564, '湖北农信社', '(NULL)', '信通卡', '信通卡', '622412', 16, 6, 14105200, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1565, '湖北农信', '(NULL)', '福农小康卡', '福农小康卡', '621523', 16, 6, 14105200, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1566, '湖北农信社', '(NULL)', '福卡IC借记卡', '福卡IC借记卡', '623055', 16, 6, 14105200, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1567, '湖北农信社', '(NULL)', '福卡(VIP卡)', '福卡(VIP卡)', '621013', 16, 6, 14105200, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1568, '武汉农信', '(NULL)', '信通卡(银联卡)', '信通卡(银联卡)', '940044', 17, 6, 14105210, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1569, '徐州市郊农村信用合作联社', '(NULL)', '信通卡(银联卡)', '信通卡(银联卡)', '622312', 16, 6, 14113030, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1570, '江阴农村商业银行', '(NULL)', '暨阳公务卡', '暨阳公务卡', '628381', 16, 6, 14123020, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1571, '江阴市农村商业银行', '(NULL)', '合作贷记卡(银联卡)', '合作贷记卡(银联卡)', '622481', 16, 6, 14123020,
        NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1572, '江阴农村商业银行', '(NULL)', '合作借记卡', '合作借记卡', '622341', 16, 6, 14123022, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1573, '江阴农村商业银行', '(NULL)', '合作卡(银联卡)', '合作卡(银联卡)', '940058', 16, 6, 14123022, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1574, '重庆农村商业银行股份有限公司', '(NULL)', '信合平安卡', '信合平安卡', '622867', 16, 6, 14136530, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1575, '重庆农村商业银行股份有限公司', '(NULL)', '信合希望卡', '信合希望卡', '622885', 16, 6, 14136530, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1576, '重庆农村商业银行股份有限公司', '(NULL)', '信合一卡通(银联卡)', '信合一卡通(银联卡)', '940020', 16, 6,
        14136530, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1577, '重庆农村商业银行', 'CRCBANK', '江渝借记卡VIP卡', '江渝借记卡VIP卡', '621258', 16, 6, 14136900, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1578, '重庆农村商业银行', 'CRCBANK', '江渝乡情福农卡', '江渝乡情福农卡', '621528', 16, 6, 14136900, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1579, '重庆农村商业银行', 'CRCBANK', '江渝IC借记卡', '江渝IC借记卡', '621465', 16, 6, 14136900, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1580, '山东农村信用联合社', '(NULL)', '信通卡', '信通卡', '900105', 16, 6, 14144500, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1581, '山东农村信用联合社', '(NULL)', '信通卡', '信通卡', '900205', 16, 6, 14144500, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1582, '山东农村信用联合社', '(NULL)', '信通卡', '信通卡', '622319', 16, 6, 14144500, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1583, '山东省农村信用社联合社', '(NULL)', '泰山福农卡', '泰山福农卡', '621521', 16, 6, 14144500, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1584, '山东省农村信用社联合社', '(NULL)', 'VIP卡', 'VIP卡', '621690', 16, 6, 14144500, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1585, '青岛农信', '(NULL)', '信通卡', '信通卡', '62231902', 16, 8, 14144520, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1586, '青岛农信', '(NULL)', '信通卡', '信通卡', '90010502', 16, 8, 14144520, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1587, '青岛农信', '(NULL)', '信通卡', '信通卡', '90020502', 16, 8, 14144520, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1588, '东莞农村商业银行', 'DRCBCL', '信通卡(银联卡)', '信通卡(银联卡)', '622328', 19, 6, 14156020, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1589, '东莞农村商业银行', 'DRCBCL', '信通卡(银联卡)', '信通卡(银联卡)', '940062', 19, 6, 14156020, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1590, '东莞农村商业银行', 'DRCBCL', '信通信用卡', '信通信用卡', '625288', 16, 6, 14156020, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1591, '东莞农村商业银行', 'DRCBCL', '信通借记卡', '信通借记卡', '623038', 19, 6, 14156020, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1592, '东莞农村商业银行', 'DRCBCL', '贷记IC卡', '贷记IC卡', '625888', 16, 6, 14156020, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1593, '张家港农村商业银行', 'ZRCBANK', '一卡通(银联卡)', '一卡通(银联卡)', '622332', 17, 6, 14163056, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1594, '张家港农村商业银行', 'ZRCBANK', '一卡通(银联卡)', '一卡通(银联卡)', '940063', 17, 6, 14163056, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1595, '福建省农村信用社联合社', '(NULL)', '万通(借记)卡', '万通(借记)卡', '622127', 19, 6, 14173900, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1596, '福建省农村信用社联合社', '(NULL)', '万通(借记)卡', '万通(借记)卡', '622184', 19, 6, 14173900, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1597, '福建省农村信用社联合社', '(NULL)', '社保卡', '社保卡', '621701', 19, 6, 14173900, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1598, '福建省农村信用社联合社', '(NULL)', '福建海峡旅游卡', '福建海峡旅游卡', '621251', 19, 6, 14173900, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1599, '福建省农村信用社联合社', '(NULL)', '福万通福农卡', '福万通福农卡', '621589', 19, 6, 14173900, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1600, '福建省农村信用社联合社', '(NULL)', '借记卡', '借记卡', '623036', 19, 6, 14173900, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1601, '北京农村商业银行', 'BJRCB', '中国旅行卡', '中国旅行卡', '620088', 19, 6, 14181000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1602, '北京农村商业银行', 'BJRCB', '惠通卡', '惠通卡', '621068', 19, 6, 14181000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1603, '北京农村商业银行', 'BJRCB', '信通卡', '信通卡', '622138', 19, 6, 14181000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1604, '北京农村商业银行', 'BJRCB', '惠通卡', '惠通卡', '621066', 19, 6, 14181000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1605, '北京农村商业银行', 'BJRCB', '凤凰福农卡', '凤凰福农卡', '621560', 19, 6, 14181000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1606, '北京农商行', '(NULL)', '凤凰福农卡', '凤凰福农卡', '625526', 16, 6, 14181001, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1607, '北京农商行', '(NULL)', '凤凰标准卡', '凤凰标准卡', '625186', 16, 6, 14181001, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1608, '北京农商行', '(NULL)', '凤凰公务卡', '凤凰公务卡', '628336', 16, 6, 14181001, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1609, '天津农村商业银行', '(NULL)', '吉祥商联IC卡', '吉祥商联IC卡', '622531', 19, 6, 14191100, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1610, '天津农村商业银行', '(NULL)', '信通借记卡(银联卡)', '信通借记卡(银联卡)', '622329', 19, 6, 14191100, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1611, '鄞州农村合作银行', '(NULL)', '蜜蜂借记卡(银联卡)', '蜜蜂借记卡(银联卡)', '622339', 16, 6, 14203320, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1612, '宁波鄞州农村合作银行', '(NULL)', '蜜蜂电子钱包(IC)', '蜜蜂电子钱包(IC)', '620500', 16, 6, 14203323, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1613, '宁波鄞州农村合作银行', '(NULL)', '蜜蜂IC借记卡', '蜜蜂IC借记卡', '621024', 16, 6, 14203323, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1614, '宁波鄞州农村合作银行', '(NULL)', '蜜蜂贷记IC卡', '蜜蜂贷记IC卡', '622289', 16, 6, 14203323, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1615, '宁波鄞州农村合作银行', '(NULL)', '蜜蜂贷记卡', '蜜蜂贷记卡', '622389', 16, 6, 14203323, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1616, '宁波鄞州农村合作银行', '(NULL)', '公务卡', '公务卡', '628300', 16, 6, 14203323, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1617, '佛山市三水区农村信用合作社', '(NULL)', '信通卡(银联卡)', '信通卡(银联卡)', '622343', 19, 6, 14215881,
        NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1618, '成都农村商业银行', '(NULL)', '福农卡', '福农卡', '625516', 16, 6, 14226510, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1619, '成都农村商业银行', '(NULL)', '福农卡', '福农卡', '621516', 19, 6, 14226510, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1620, '成都农村商业银行', '(NULL)', '天府借记卡(银联卡)', '天府借记卡(银联卡)', '622345', 19, 6, 14226510, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1621, '江苏农信社', '(NULL)', '圆鼎卡(银联卡)', '圆鼎卡(银联卡)', '622452', 19, 6, 14243000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1622, '江苏农信社', '(NULL)', '圆鼎卡(银联卡)', '圆鼎卡(银联卡)', '622324', 19, 6, 14243000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1623, '江苏省农村信用社联合社', '(NULL)', '福农卡', '福农卡', '621578', 19, 6, 14243000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1624, '江苏省农村信用社联合社', '(NULL)', '圆鼎借记IC卡', '圆鼎借记IC卡', '623066', 19, 6, 14243001, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1625, '江门新会农村商业银行', '(NULL)', '信通卡(银联卡)', '信通卡(银联卡)', '622470', 19, 6, 14255890, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1626, '吴江农商行', '(NULL)', '垂虹贷记卡', '垂虹贷记卡', '622648', 16, 6, 14283054, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1627, '吴江农商行', '(NULL)', '银联标准公务卡', '银联标准公务卡', '628248', 16, 6, 14283054, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1628, '吴江农商行', '(NULL)', '垂虹卡(银联卡)', '垂虹卡(银联卡)', '622488', 16, 6, 14283054, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1629, '浙江省农村信用社联合社', 'ZJNX', '丰收贷记卡', '丰收贷记卡', '622288', 16, 6, 14293300, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1630, '浙江省农村信用社联合社', 'ZJNX', '丰收卡(银联卡)', '丰收卡(银联卡)', '622858', 19, 6, 14293300, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1631, '浙江省农村信用社联合社', 'ZJNX', '丰收小额贷款卡', '丰收小额贷款卡', '621058', 19, 6, 14293300, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1632, '浙江省农村信用社联合社', 'ZJNX', '丰收福农卡', '丰收福农卡', '621527', 19, 6, 14293300, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1633, '浙江省农村信用社联合社', 'ZJNX', '银联标准公务卡', '银联标准公务卡', '628280', 16, 6, 14293300, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1634, '苏州银行', 'BOSZ', '新苏卡(银联卡)', '新苏卡(银联卡)', '622855', 19, 6, 14303050, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1635, '苏州银行', 'BOSZ', '新苏卡', '新苏卡', '621461', 19, 6, 14303050, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1636, '珠海农村商业银行', '(NULL)', '信通卡(银联卡)', '信通卡(银联卡)', '622859', 19, 6, 14315850, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1637, '太仓农村商业银行', '(NULL)', '郑和卡(银联卡)', '郑和卡(银联卡)', '622869', 19, 6, 14333051, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1638, '尧都区农村信用合作社联社', '(NULL)', '天河卡', '天河卡', '622882', 19, 6, 14341770, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1639, '贵州省农村信用社联合社', '(NULL)', '信合卡', '信合卡', '622893', 19, 6, 14367000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1640, '贵州省农村信用社联合社', '(NULL)', '信合福农卡', '信合福农卡', '621590', 19, 6, 14367000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1641, '无锡农村商业银行', 'WRCB', '金阿福', '金阿福', '622895', 16, 6, 14373020, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1642, '湖南省农村信用社联合社', '(NULL)', '福祥借记卡', '福祥借记卡', '622169', 19, 6, 14385500, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1643, '湖南省农村信用社联合社', '(NULL)', '福祥卡', '福祥卡', '621519', 19, 6, 14385500, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1644, '湖南省农村信用社联合社', '(NULL)', '福祥卡', '福祥卡', '621539', 19, 6, 14385500, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1645, '江西农信联合社', '(NULL)', '百福卡', '百福卡', '622681', 19, 6, 14394200, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1646, '江西农信联合社', '(NULL)', '百福卡', '百福卡', '622682', 19, 6, 14394200, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1647, '江西农信联合社', '(NULL)', '百福卡', '百福卡', '622683', 19, 6, 14394200, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1648, '河南省农村信用社联合社', '(NULL)', '金燕卡', '金燕卡', '622991', 18, 6, 14404900, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1649, '河南省农村信用社联合社', '(NULL)', '金燕快货通福农卡', '金燕快货通福农卡', '621585', 18, 6, 14404900,
        NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1650, '河南省农村信用社联合社', '(NULL)', '借记卡', '借记卡', '623013', 18, 6, 14404900, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1651, '河北省农村信用社联合社', '(NULL)', '信通卡', '信通卡', '621021', 19, 6, 14411200, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1652, '河北省农村信用社联合社', '(NULL)', '信通卡(银联卡)', '信通卡(银联卡)', '622358', 19, 6, 14411200, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1653, '河北省农村信用社联合社', '(NULL)', '借记卡', '借记卡', '623025', 19, 6, 14411200, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1654, '陕西省农村信用社联合社', '(NULL)', '陕西信合富秦卡', '陕西信合富秦卡', '622506', 19, 6, 14427900, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1655, '陕西省农村信用社联合社', '(NULL)', '富秦家乐福农卡', '富秦家乐福农卡', '621566', 19, 6, 14427900, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1656, '陕西省农村信用社联合社', '(NULL)', '富秦卡', '富秦卡', '623027', 19, 6, 14427900, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1657, '陕西省农村信用社联合社', '(NULL)', '社会保障卡（陕西信合）', '社会保障卡（陕西信合）', '623028', 19, 6,
        14427900, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1658, '广西农村信用社联合社', '(NULL)', '桂盛卡', '桂盛卡', '622992', 19, 6, 14436100, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1659, '新疆维吾尔自治区农村信用社联合', '(NULL)', '玉卡', '玉卡', '621008', 16, 6, 14448800, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1660, '新疆农村信用社联合社', '(NULL)', '福农卡', '福农卡', '621525', 16, 6, 14448800, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1661, '吉林农信联合社', '(NULL)', '吉卡', '吉卡', '622935', 19, 6, 14452400, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1662, '吉林农信联合社', '(NULL)', '吉林农信银联标准吉卡福农借记卡', '吉林农信银联标准吉卡福农借记卡', '621531',
        19, 6, 14452400, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1663, '吉林省农村信用社联合社', '(NULL)', '借记IC卡', '借记IC卡', '623181', 19, 6, 14452402, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1664, '黄河农村商业银行', '(NULL)', '黄河卡', '黄河卡', '622947', 19, 6, 14468700, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1665, '黄河农村商业银行', '(NULL)', '黄河富农卡福农卡', '黄河富农卡福农卡', '621561', 19, 6, 14468700, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1666, '安徽省农村信用社联合社', '(NULL)', '金农卡', '金农卡', '622953', 19, 6, 14473600, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1667, '安徽省农村信用社联合社', '(NULL)', '金农易贷福农卡', '金农易贷福农卡', '621526', 19, 6, 14473600, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1668, '海南省农村信用社联合社', '(NULL)', '大海卡', '大海卡', '621036', 19, 6, 14486400, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1669, '海南省农村信用社联合社', '(NULL)', '大海福农卡', '大海福农卡', '621536', 19, 6, 14486400, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1670, '海南省农村信用社联合社', '(NULL)', '金融IC借记卡', '金融IC借记卡', '621458', 19, 6, 14486400, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1671, '青海省农村信用社联合社', '(NULL)', '紫丁香借记卡', '紫丁香借记卡', '621065', 16, 6, 14498500, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1672, '青海省农村信用社联合社', '(NULL)', '紫丁香福农卡', '紫丁香福农卡', '621517', 16, 6, 14498500, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1673, '青海省农村信用社联合社', '(NULL)', '青海省公务卡', '青海省公务卡', '628289', 16, 6, 14498501, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1674, '广东省农村信用社联合社', 'GDRCC', '信通卡(银联卡)', '信通卡(银联卡)', '622477', 19, 6, 14505800, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1675, '广东省农村信用社联合社', 'GDRCC', '信通白金卡', '信通白金卡', '622509', 19, 6, 14505800, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1676, '广东省农村信用社联合社', 'GDRCC', '信通金卡', '信通金卡', '622510', 19, 6, 14505800, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1677, '广东省农村信用社联合社', 'GDRCC', '信通卡(银联卡)', '信通卡(银联卡)', '622302', 16, 6, 14505800, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1678, '广东省农村信用社联合社', 'GDRCC', '信通卡(银联卡)', '信通卡(银联卡)', '622362', 19, 6, 14505800, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1679, '广东省农村信用社联合社', 'GDRCC', '珠江平安卡', '珠江平安卡', '621018', 19, 6, 14505800, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1680, '广东省农村信用社联合社', 'GDRCC', '珠江平安福农卡', '珠江平安福农卡', '621518', 19, 6, 14505800, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1681, '内蒙古自治区农村信用社联合式', '(NULL)', '信合金牛卡', '信合金牛卡', '622976', 19, 6, 14511900, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1682, '内蒙古自治区农村信用社联合式', '(NULL)', '金牛福农卡', '金牛福农卡', '621533', 19, 6, 14511900, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1683, '内蒙古自治区农村信用社联合式', '(NULL)', '白金卡', '白金卡', '621362', 19, 6, 14511900, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1684, '四川省农村信用社联合社', '(NULL)', '蜀信卡', '蜀信卡', '621033', 19, 6, 14526500, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1685, '四川省农村信用社联合社', '(NULL)', '蜀信贵宾卡', '蜀信贵宾卡', '621099', 19, 6, 14526500, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1686, '四川省农村信用社联合社', '(NULL)', '蜀信福农卡', '蜀信福农卡', '621530', 19, 6, 14526500, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1687, '四川省农村信用社联合社', '(NULL)', '蜀信卡', '蜀信卡', '621457', 19, 6, 14526500, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1688, '四川省农村信用社联合社', '(NULL)', '蜀信社保卡', '蜀信社保卡', '621459', 19, 6, 14526500, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1689, '甘肃省农村信用社联合社', '(NULL)', '飞天卡', '飞天卡', '621061', 19, 6, 14538200, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1690, '甘肃省农村信用社联合社', '(NULL)', '福农卡', '福农卡', '621520', 19, 6, 14538200, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1691, '辽宁省农村信用社联合社', '(NULL)', '金信卡', '金信卡', '621449', 19, 6, 14540001, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1692, '辽宁省农村信用社联合社', '(NULL)', '金信卡', '金信卡', '621026', 19, 6, 14542200, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1693, '山西省农村信用社联合社', '(NULL)', '关帝银行卡', '关帝银行卡', '622968', 19, 6, 14551600, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1694, '山西省农村信用社', '(NULL)', '信合通', '信合通', '621280', 19, 6, 14551600, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1695, '山西省农村信用社联合社', '(NULL)', '信合通', '信合通', '621580', 19, 6, 14551600, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1696, '天津滨海农村商业银行', '(NULL)', '四海通卡', '四海通卡', '621073', 19, 6, 14561100, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1697, '黑龙江省农村信用社联合社', '(NULL)', '鹤卡', '鹤卡', '621228', 19, 6, 14572600, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1698, '黑龙江省农村信用社联合社', '(NULL)', '丰收时贷福农卡', '丰收时贷福农卡', '621557', 19, 6, 14572600, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1699, '武汉农村商业银行', 'WHRCB', '汉卡', '汉卡', '621361', 19, 6, 14595210, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1700, '武汉农村商业银行', 'WHRCB', '汉卡', '汉卡', '623033', 19, 6, 14595210, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1701, '江南农村商业银行', '(NULL)', '阳湖卡(银联卡)', '阳湖卡(银联卡)', '622891', 19, 6, 14603040, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1702, '江南农村商业银行', '(NULL)', '天天红火卡', '天天红火卡', '621363', 19, 6, 14603040, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1703, '大邑交银兴民村镇银行', '(NULL)', '借记卡', '借记卡', '621056801', 19, 9, 15006518, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1704, '湖北嘉鱼吴江村镇银行', '(NULL)', '垂虹卡', '垂虹卡', '622995', 16, 6, 15015363, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1705, '青岛即墨北农商村镇银行', '(NULL)', '凤凰卡', '凤凰卡', '6229756114', 19, 10, 15024521, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1706, '湖北仙桃北农商村镇银行', '(NULL)', '凤凰卡', '凤凰卡', '6229756115', 19, 10, 15025371, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1707, '句容茅山村镇银行', '(NULL)', '暨阳卡', '暨阳卡', '62105913', 16, 8, 15033142, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1708, '兴化苏南村镇银行', '(NULL)', '暨阳卡', '暨阳卡', '62105916', 16, 8, 15033161, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1709, '海口苏南村镇银行', '(NULL)', '暨阳卡', '暨阳卡', '62105915', 16, 8, 15036410, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1710, '双流诚民村镇银行', '(NULL)', '暨阳卡', '暨阳卡', '62105901', 16, 8, 15036512, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1711, '宣汉诚民村镇银行', '(NULL)', '暨阳卡', '暨阳卡', '62105900', 16, 8, 15036753, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1712, '福建建瓯石狮村镇银行', '(NULL)', '玉竹卡', '玉竹卡', '621053', 19, 6, 15044015, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1713, '恩施常农商村镇银行', '(NULL)', '恩施村镇银行借记卡', '恩施村镇银行借记卡', '621260002', 19, 9, 15055411,
        NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1714, '咸丰常农商村镇银行', '(NULL)', '借记卡', '借记卡', '621260001', 19, 9, 15055416, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1715, '浙江乐清联合村镇银行', '(NULL)', '联合卡', '联合卡', '621092003', 19, 9, 15083333, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1716, '浙江嘉善联合村镇银行', '(NULL)', '联合卡', '联合卡', '621092002', 19, 9, 15083351, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1717, '浙江长兴联合村镇银行', '(NULL)', '联合卡', '联合卡', '621092001', 19, 9, 15083362, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1718, '浙江平湖工银村镇银行', '(NULL)', '金平卡', '金平卡', '621230', 19, 6, 15103352, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1719, '重庆璧山工银村镇银行', '(NULL)', '翡翠卡', '翡翠卡', '621229', 19, 6, 15106919, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1720, '北京密云汇丰村镇银行', '(NULL)', '借记卡', '借记卡', '621250004', 16, 9, 15111027, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1721, '福建永安汇丰村镇银行', '(NULL)', '借记卡', '借记卡', '621250003', 16, 9, 15113961, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1722, '湖北随州曾都汇丰村镇银行', '(NULL)', '借记卡', '借记卡', '621250001', 16, 9, 15115270, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1723, '广东恩平汇丰村镇银行', '(NULL)', '借记卡', '借记卡', '621250005', 16, 9, 15115893, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1724, '重庆大足汇丰村镇银行', '(NULL)', '借记卡', '借记卡', '621250002', 16, 9, 15116917, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1725, '江苏沭阳东吴村镇银行', '(NULL)', '新苏借记卡', '新苏借记卡', '621241001', 19, 9, 15123181, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1726, '重庆农村商业银行', 'CRCBANK', '银联标准贷记卡', '银联标准贷记卡', '622218', 16, 6, 15136900, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1727, '重庆农村商业银行', 'CRCBANK', '公务卡', '公务卡', '628267', 16, 6, 15136900, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1728, '鄂尔多斯市东胜蒙银村镇银行', '(NULL)', '龙源腾借记卡', '龙源腾借记卡', '621346003', 19, 9, 15142050,
        NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1729, '方大村镇银行', '(NULL)', '胡杨卡神州卡', '胡杨卡神州卡', '621346002', 19, 9, 15142080, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1730, '深圳龙岗鼎业村镇银行', '(NULL)', '鼎业卡', '鼎业卡', '621346001', 19, 9, 15145840, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1731, '中山小榄村镇银行', 'XLBANK', '菊卡', '菊卡', '621326763', 18, 9, 15156030, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1732, '江苏邗江民泰村镇银行', '(NULL)', '金荷花借记卡', '金荷花借记卡', '621338001', 19, 9, 15173120, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1733, '天津静海新华村镇银行', '(NULL)', '新华卡', '新华卡', '621353008', 19, 9, 15181123, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1734, '安徽当涂新华村镇银行', '(NULL)', '新华卡', '新华卡', '621353002', 19, 9, 15183651, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1735, '安徽当涂新华村镇银行', '(NULL)', '新华卡', '新华卡', '621353102', 19, 9, 15183651, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1736, '安徽和县新华村镇银行', '(NULL)', '新华卡', '新华卡', '621353005', 19, 9, 15183653, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1737, '望江新华村镇银行', '(NULL)', '新华卡', '新华卡', '621353007', 19, 9, 15183687, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1738, '郎溪新华村镇银行', '(NULL)', '新华卡', '新华卡', '621353003', 19, 9, 15183772, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1739, '郎溪新华村镇银行', '(NULL)', '新华卡', '新华卡', '621353103', 19, 9, 15183772, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1740, '广州番禹新华村镇银行', '(NULL)', '新华卡', '新华卡', '621353001', 19, 9, 15185810, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1741, '广州番禹新华村镇银行', '(NULL)', '新华卡', '新华卡', '621353101', 19, 9, 15185810, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1742, '沂水中银富登村镇银行', '(NULL)', '借记卡', '借记卡', '621356004', 19, 9, 15194737, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1743, '谷城中银富登村镇银行', '(NULL)', '借记卡', '借记卡', '621356007', 19, 9, 15195284, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1744, '老河口中银富登村镇银行', '(NULL)', '中银富登村镇银行借记卡', '中银富登村镇银行借记卡', '621356009', 19,
        9, 15195287, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1745, '枣阳中银富登村镇银行', '(NULL)', '借记卡', '借记卡', '621356008', 19, 9, 15195288, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1746, '京山中银富登村镇银行', '(NULL)', '京山富登借记卡', '京山富登借记卡', '621356002', 19, 9, 15195321, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1747, '蕲春中银富登村镇银行', '(NULL)', '中银富登借记卡', '中银富登借记卡', '621356001', 19, 9, 15195338, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1748, '潜江中银富登村镇银行', '(NULL)', '中银富登村镇银行借记卡', '中银富登村镇银行借记卡', '621356010', 19, 9,
        15195375, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1749, '北京顺义银座村镇银行', '(NULL)', '大唐卡', '大唐卡', '621347002', 16, 9, 15201000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1750, '浙江三门银座村镇银行', '(NULL)', '大唐卡', '大唐卡', '621347005', 16, 9, 15203457, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1751, '江西赣州银座村镇银行', '(NULL)', '大唐卡', '大唐卡', '621347003', 16, 9, 15204280, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1752, '深圳福田银座村镇银行', '(NULL)', '大唐卡', '大唐卡', '621347001', 16, 9, 15205840, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1753, '重庆渝北银座村镇银行', '(NULL)', '大唐卡', '大唐卡', '621347006', 16, 9, 15206900, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1754, '重庆黔江银座村镇银行', '(NULL)', '大唐卡', '大唐卡', '621347007', 16, 9, 15206925, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1755, '北京怀柔融兴村镇银行', '(NULL)', '融兴普惠卡', '融兴普惠卡', '621350010', 19, 9, 15211000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1756, '榆树融兴村镇银行', '(NULL)', '融兴普惠卡', '融兴普惠卡', '621350431', 19, 9, 15212411, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1757, '拜泉融兴村镇银行', '(NULL)', '融兴普惠卡', '融兴普惠卡', '621350013', 19, 9, 15212652, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1758, '新安融兴村镇银行', '(NULL)', '融兴普惠卡', '融兴普惠卡', '621350004', 19, 9, 15214933, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1759, '深圳宝安融兴村镇银行', '(NULL)', '融兴普惠卡', '融兴普惠卡', '621350755', 19, 9, 15215840, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1760, '海南保亭融兴村镇银行', '(NULL)', '融兴普惠卡', '融兴普惠卡', '621350017', 19, 9, 15216437, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1761, '会宁会师村镇银行', '(NULL)', '会师普惠卡', '会师普惠卡', '621350943', 19, 9, 15218242, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1762, '南阳村镇银行', '(NULL)', '玉都卡', '玉都卡', '621392', 18, 6, 15265130, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1763, '上海松江民生村镇银行', '(NULL)', '借记卡', '借记卡', '621399001', 16, 9, 15272900, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1764, '浙江建德湖商村镇银行', '(NULL)', '湖商卡', '湖商卡', '621365001', 19, 9, 15283315, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1765, '晋中市榆次融信村镇银行', '(NULL)', '魏榆卡', '魏榆卡', '621481', 19, 6, 15301750, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1766, '宝生村镇银行', '(NULL)', '宝生村镇银行一卡通', '宝生村镇银行一卡通', '621623001', 19, 9, 15335840, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1767, '广州花都稠州村镇银行', '(NULL)', '义卡借记卡', '义卡借记卡', '621627007', 19, 9, 15355810, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1768, '文昌国民村镇银行', '(NULL)', '赀业卡', '赀业卡', '621650002', 19, 9, 15386423, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1769, '琼海国民村镇银行', '(NULL)', '椰卡', '椰卡', '621650001', 19, 9, 15386424, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1770, '鹤山珠江村镇银行', '(NULL)', '珠江太阳卡', '珠江太阳卡', '62163101', 18, 8, 15405895, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1771, '大武口石银村镇银行', '(NULL)', '麒麟借记卡', '麒麟借记卡', '621653006', 19, 9, 15438720, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1772, '广元贵商村镇银行', '(NULL)', '利卡', '利卡', '62308299', 19, 8, 15446610, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1773, '昆山鹿城村镇银行', '(NULL)', '鹿城卡', '鹿城卡', '621355001', 16, 9, 15483052, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1774, '东营莱商村镇银行', '(NULL)', '绿洲卡', '绿洲卡', '621396', 16, 6, 15514550, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1775, '湖北咸安武农商村镇银行', '(NULL)', '汉卡', '汉卡', '621676001', 19, 9, 15535360, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1776, '湖北赤壁武弄商村镇银行', '(NULL)', '汉卡', '汉卡', '621676002', 19, 9, 15535367, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1777, '广州增城长江村镇银行', '(NULL)', '汉卡', '汉卡', '621676003', 19, 9, 15535813, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1778, '济南长清沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682101', 19, 9, 15584510, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1779, '济南槐荫沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682102', 19, 9, 15584513, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1780, '泰安沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682106', 19, 9, 15584630, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1781, '宁阳沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682103', 19, 9, 15584631, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1782, '东平沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682105', 19, 9, 15584633, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1783, '聊城东昌沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682110', 19, 9, 15584710, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1784, '临清沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682111', 19, 9, 15584712, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1785, '阳谷沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682109', 19, 9, 15584713, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1786, '茌平沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682108', 19, 9, 15584715, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1787, '日照沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682107', 19, 9, 15584770, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1788, '长沙星沙沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682202', 19, 9, 15585511, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1789, '宁乡沪农商行村镇银行', '(NULL)', '借记卡', '借记卡', '621682201', 19, 9, 15585514, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1790, '醴陵沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682203', 19, 9, 15585525, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1791, '衡阳沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682205', 19, 9, 15585541, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1792, '澧县沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682209', 19, 9, 15585583, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1793, '临澧沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682208', 19, 9, 15585584, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1794, '石门沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682210', 19, 9, 15585586, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1795, '慈利沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682213', 19, 9, 15585591, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1796, '涟源沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682211', 19, 9, 15585623, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1797, '双峰沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682212', 19, 9, 15585624, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1798, '桂阳沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682207', 19, 9, 15585634, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1799, '永兴沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682206', 19, 9, 15585635, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1800, '嵩明沪农商村镇银行', '(NULL)', '借记卡', '借记卡', '621682302', 19, 9, 15587317, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1801, '东亚银行', 'HKBEA', 'cup credit card', 'cup credit card', '622372', 16, 6, 25020344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1802, '东亚银行', 'HKBEA', '电子网络人民币卡', '电子网络人民币卡', '622365', 17, 6, 25020344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1803, '东亚银行', 'HKBEA', '人民币信用卡(银联卡)', '人民币信用卡(银联卡)', '622471', 16, 6, 25020344, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1804, '东亚银行', 'HKBEA', '银联借记卡', '银联借记卡', '622943', 19, 6, 25020344, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1805, '东亚银行', 'HKBEA', '人民币信用卡金卡', '人民币信用卡金卡', '622472', 16, 6, 25020344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1806, '东亚银行', 'HKBEA', '银联借记卡', '银联借记卡', '621411', 19, 6, 25020446, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1807, '花旗银行', '(NULL)', '花旗人民币信用卡', '花旗人民币信用卡', '622371', 16, 6, 25030344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1808, '花旗银行', '(NULL)', '双币卡', '双币卡', '625091', 16, 6, 25030344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1809, '大新银行', '(NULL)', '信用卡(普通卡)', '信用卡(普通卡)', '622293', 16, 6, 25040344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1810, '大新银行', '(NULL)', '商务信用卡', '商务信用卡', '622295', 16, 6, 25040344, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1811, '大新银行', '(NULL)', '商务信用卡', '商务信用卡', '622296', 16, 6, 25040344, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1812, '大新银行', '(NULL)', '预付卡(普通卡)', '预付卡(普通卡)', '622297', 16, 6, 25040344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1813, '大新银行', '(NULL)', '人民币信用卡', '人民币信用卡', '622373', 16, 6, 25040344, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1814, '大新银行', '(NULL)', '人民币借记卡(银联卡)', '人民币借记卡(银联卡)', '622375', 17, 6, 25040344, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1815, '大新银行', '(NULL)', '大新人民币信用卡金卡', '大新人民币信用卡金卡', '622451', 16, 6, 25040344, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1816, '大新银行', '(NULL)', '大新港币信用卡(金卡)', '大新港币信用卡(金卡)', '622294', 16, 6, 25040344, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1817, '大新银行', '(NULL)', '贷记卡', '贷记卡', '625940', 16, 6, 25040344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1818, '大新银行', '(NULL)', '借记卡(银联卡)', '借记卡(银联卡)', '622489', 17, 6, 25040344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1819, '永亨银行', '(NULL)', '永亨尊贵理财卡', '永亨尊贵理财卡', '622871', 16, 6, 25060344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1820, '永亨银行', '(NULL)', '永亨贵宾理财卡', '永亨贵宾理财卡', '622958', 16, 6, 25060344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1821, '永亨银行', '(NULL)', '永亨贵宾理财卡', '永亨贵宾理财卡', '622963', 16, 6, 25060344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1822, '永亨银行', '(NULL)', '永亨贵宾理财卡', '永亨贵宾理财卡', '622957', 16, 6, 25060344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1823, '永亨银行', '(NULL)', '港币贷记卡', '港币贷记卡', '622798', 16, 6, 25060344, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1824, '永亨银行', '(NULL)', '永亨银联白金卡', '永亨银联白金卡', '625010', 16, 6, 25060344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1825, '建设银行', 'CCB', '人民币信用卡', '人民币信用卡', '622381', 16, 6, 25070344, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1826, '建设银行', 'CCB', '银联卡', '银联卡', '622675', 16, 6, 25070344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1827, '建设银行', 'CCB', '银联卡', '银联卡', '622676', 16, 6, 25070344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1828, '建设银行', 'CCB', '银联卡', '银联卡', '622677', 16, 6, 25070344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1829, '建设银行', 'CCB', '人民币卡(银联卡)', '人民币卡(银联卡)', '622382', 16, 6, 25070344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1830, '建设银行', 'CCB', '借记卡', '借记卡', '621487', 16, 6, 25070344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1831, '建设银行', 'CCB', '建行陆港通龙卡', '建行陆港通龙卡', '621083', 16, 6, 25070344, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1832, '星展银行', '(NULL)', '银联人民币银行卡', '银联人民币银行卡', '622487', 16, 6, 25080344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1833, '星展银行', '(NULL)', '银联人民币银行卡', '银联人民币银行卡', '622487', 17, 6, 25080344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1834, '星展银行', '(NULL)', '银联人民币银行卡', '银联人民币银行卡', '622490', 16, 6, 25080344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1835, '星展银行', '(NULL)', '银联人民币银行卡', '银联人民币银行卡', '622490', 17, 6, 25080344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1836, '星展银行', '(NULL)', '银联银行卡', '银联银行卡', '622491', 16, 6, 25080344, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1837, '星展银行', '(NULL)', '银联港币银行卡', '银联港币银行卡', '622491', 17, 6, 25080344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1838, '星展银行', '(NULL)', '银联银行卡', '银联银行卡', '622492', 16, 6, 25080344, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1839, '星展银行', '(NULL)', '银联港币银行卡', '银联港币银行卡', '622492', 17, 6, 25080344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1840, '星展银行', '(NULL)', '借记卡', '借记卡', '621744', 17, 6, 25080344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1841, '星展银行', '(NULL)', '借记卡', '借记卡', '621745', 17, 6, 25080344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1842, '星展银行', '(NULL)', '借记卡', '借记卡', '621746', 17, 6, 25080344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1843, '星展银行', '(NULL)', '借记卡', '借记卡', '621747', 17, 6, 25080344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1844, '上海商业银行', '(NULL)', '上银卡', '上银卡', '621034', 16, 6, 25090344, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1845, '上海商业银行', '(NULL)', '人民币信用卡(银联卡)', '人民币信用卡(银联卡)', '622386', 16, 6, 25090344, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1846, '上海商业银行', '(NULL)', '上银卡ShacomCard', '上银卡ShacomCard', '622952', 16, 6, 25090344, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1847, '永隆银行', '(NULL)', '永隆人民币信用卡', '永隆人民币信用卡', '622387', 16, 6, 25100344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1848, '永隆银行', '(NULL)', '永隆人民币信用卡', '永隆人民币信用卡', '622423', 16, 6, 25100344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1849, '永隆银行', '(NULL)', '永隆港币卡', '永隆港币卡', '622971', 17, 6, 25100344, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1850, '永隆银行', '(NULL)', '永隆人民币卡', '永隆人民币卡', '622970', 17, 6, 25100344, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1851, '永隆银行', '(NULL)', '永隆双币卡', '永隆双币卡', '625062', 16, 6, 25100344, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1852, '永隆银行', '(NULL)', '永隆双币卡', '永隆双币卡', '625063', 16, 6, 25100344, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1853, '香港上海汇丰银行', '(NULL)', '人民币卡(银联卡)', '人民币卡(银联卡)', '622360', 16, 6, 25120344, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1854, '香港上海汇丰银行', '(NULL)', '人民币金卡(银联卡)', '人民币金卡(银联卡)', '622361', 16, 6, 25120344, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1855, '香港上海汇丰银行', '(NULL)', '银联卡', '银联卡', '625034', 16, 6, 25120344, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1856, '香港上海汇丰银行', '(NULL)', '汇丰银联双币卡', '汇丰银联双币卡', '625096', 16, 6, 25120344, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1857, '香港上海汇丰银行', '(NULL)', '汇丰银联双币钻石卡', '汇丰银联双币钻石卡', '625098', 16, 6, 25120344, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1858, '香港上海汇丰银行', '(NULL)', 'ATMCard', 'ATMCard', '622406', 17, 6, 25130344, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1859, '香港上海汇丰银行', '(NULL)', 'ATMCard', 'ATMCard', '622407', 19, 6, 25130344, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1860, '香港上海汇丰银行', '(NULL)', '借记卡', '借记卡', '621442', 17, 6, 25130344, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1861, '香港上海汇丰银行', '(NULL)', '借记卡', '借记卡', '621443', 19, 6, 25130344, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1862, '恒生银行', '(NULL)', '港币贷记白金卡', '港币贷记白金卡', '625026', 16, 6, 25140344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1863, '恒生银行', '(NULL)', '港币贷记普卡', '港币贷记普卡', '625024', 16, 6, 25140344, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1864, '恒生银行', '(NULL)', '恒生人民币信用卡', '恒生人民币信用卡', '622376', 16, 6, 25140344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1865, '恒生银行', '(NULL)', '恒生人民币白金卡', '恒生人民币白金卡', '622378', 16, 6, 25140344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1866, '恒生银行', '(NULL)', '恒生人民币金卡', '恒生人民币金卡', '622377', 16, 6, 25140344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1867, '恒生银行', '(NULL)', '银联人民币钻石商务卡', '银联人民币钻石商务卡', '625092', 16, 6, 25140344, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1868, '恒生银行', '(NULL)', '恒生银行港卡借记卡', '恒生银行港卡借记卡', '622409', 19, 6, 25150344, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1869, '恒生银行', '(NULL)', '恒生银行港卡借记卡', '恒生银行港卡借记卡', '622410', 17, 6, 25150344, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1870, '恒生银行', '(NULL)', '港币借记卡（普卡）', '港币借记卡（普卡）', '621440', 17, 6, 25150344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1871, '恒生银行', '(NULL)', '港币借记卡（金卡）', '港币借记卡（金卡）', '621441', 19, 6, 25150344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1872, '中信嘉华银行', '(NULL)', '人民币信用卡金卡', '人民币信用卡金卡', '622453', 16, 6, 25160344, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1873, '中信嘉华银行', '(NULL)', '信用卡普通卡', '信用卡普通卡', '622456', 16, 6, 25160344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1874, '中信嘉华银行', '(NULL)', '人民币借记卡(银联卡)', '人民币借记卡(银联卡)', '622459', 17, 6, 25160344, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1875, '创兴银行', '(NULL)', '银联贺礼卡(创兴银行)', '银联贺礼卡(创兴银行)', '622272', 16, 6, 25170344, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1876, '创兴银行', '(NULL)', '港币借记卡', '港币借记卡', '622463', 19, 6, 25170344, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1877, '创兴银行', '(NULL)', '人民币提款卡', '人民币提款卡', '621087', 19, 6, 25170344, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1878, '创兴银行', '(NULL)', '银联双币信用卡', '银联双币信用卡', '625008', 16, 6, 25170344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1879, '创兴银行', '(NULL)', '银联双币信用卡', '银联双币信用卡', '625009', 16, 6, 25170344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1880, '中银信用卡(国际)有限公司', 'BOC', '商务金卡', '商务金卡', '625055', 16, 6, 25180344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1881, '中银信用卡(国际)有限公司', 'BOC', '中银银联双币信用卡', '中银银联双币信用卡', '625040', 16, 6, 25180344,
        NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1882, '中银信用卡(国际)有限公司', 'BOC', '中银银联双币信用卡', '中银银联双币信用卡', '625042', 16, 6, 25180344,
        NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1883, '中银信用卡(国际)有限公司', 'BOC', '澳门币贷记卡', '澳门币贷记卡', '625141', 16, 6, 25180446, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1884, '中银信用卡(国际)有限公司', 'BOC', '澳门币贷记卡', '澳门币贷记卡', '625143', 16, 6, 25180446, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1885, '中国银行（香港）', 'BOC', '接触式晶片借记卡', '接触式晶片借记卡', '621741', 19, 6, 25190344, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1886, '中国银行（香港）', 'BOC', '接触式银联双币预制晶片借记卡', '接触式银联双币预制晶片借记卡', '623040', 16, 6,
        25190344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1887, '中国银行（香港）', 'BOC', '中国银行银联预付卡', '中国银行银联预付卡', '620202', 16, 6, 25190344, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1888, '中国银行（香港）', 'BOC', '中国银行银联预付卡', '中国银行银联预付卡', '620203', 16, 6, 25190344, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1889, '南洋商业银行', '(NULL)', '银联港币卡', '银联港币卡', '621042', 19, 6, 25200344, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1890, '南洋商业银行', '(NULL)', '银联双币信用卡', '银联双币信用卡', '625046', 16, 6, 25200344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1891, '南洋商业银行', '(NULL)', '银联双币信用卡', '银联双币信用卡', '625044', 16, 6, 25200344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1892, '南洋商业银行', '(NULL)', '双币商务卡', '双币商务卡', '625058', 16, 6, 25200344, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1893, '南洋商业银行', '(NULL)', '接触式晶片借记卡', '接触式晶片借记卡', '621743', 19, 6, 25200344, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1894, '南洋商业银行', '(NULL)', '接触式银联双币预制晶片借记卡', '接触式银联双币预制晶片借记卡', '623041', 16, 6,
        25200344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1895, '南洋商业银行', '(NULL)', '南洋商业银行银联预付卡', '南洋商业银行银联预付卡', '620208', 16, 6, 25200344,
        NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1896, '南洋商业银行', '(NULL)', '南洋商业银行银联预付卡', '南洋商业银行银联预付卡', '620209', 16, 6, 25200344,
        NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1897, '集友银行', '(NULL)', '银联港币卡', '银联港币卡', '621043', 19, 6, 25210344, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1898, '集友银行', '(NULL)', '银联双币信用卡', '银联双币信用卡', '625048', 16, 6, 25210344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1899, '集友银行', '(NULL)', '银联双币信用卡', '银联双币信用卡', '625053', 16, 6, 25210344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1900, '集友银行', '(NULL)', '双币商务卡', '双币商务卡', '625060', 16, 6, 25210344, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1901, '集友银行', '(NULL)', '接触式晶片借记卡', '接触式晶片借记卡', '621742', 19, 6, 25210344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1902, '集友银行', '(NULL)', '接触式银联双币预制晶片借记卡', '接触式银联双币预制晶片借记卡', '623042', 16, 6,
        25210344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1903, '集友银行', '(NULL)', '集友银行银联预付卡', '集友银行银联预付卡', '620206', 16, 6, 25210344, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1904, '集友银行', '(NULL)', '集友银行银联预付卡', '集友银行银联预付卡', '620207', 16, 6, 25210344, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1905, 'AEON信贷财务亚洲有限公司', '(NULL)', 'AEONJUSCO银联卡', 'AEONJUSCO银联卡', '622493', 16, 6, 25230344,
        NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1906, '大丰银行', '(NULL)', '银联双币白金卡', '银联双币白金卡', '625198', 16, 6, 25250446, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1907, '大丰银行', '(NULL)', '银联双币金卡', '银联双币金卡', '625196', 16, 6, 25250446, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1908, '大丰银行', '(NULL)', '港币借记卡', '港币借记卡', '622547', 19, 6, 25250446, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1909, '大丰银行', '(NULL)', '澳门币借记卡', '澳门币借记卡', '622548', 19, 6, 25250446, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1910, '大丰银行', '(NULL)', '人民币借记卡', '人民币借记卡', '622546', 19, 6, 25250446, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1911, '澳门大丰银行', '(NULL)', '中银银联双币商务卡', '中银银联双币商务卡', '625147', 16, 6, 25250446, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1912, '大丰银行', '(NULL)', '大丰预付卡', '大丰预付卡', '620072', 19, 6, 25250446, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1913, '大丰银行', '(NULL)', '大丰银行预付卡', '大丰银行预付卡', '620204', 16, 6, 25250446, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1914, '大丰银行', '(NULL)', '大丰银行预付卡', '大丰银行预付卡', '620205', 16, 6, 25250446, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1915, 'AEON信贷财务亚洲有限公司', '(NULL)', 'AEON银联礼品卡', 'AEON银联礼品卡', '621064', 16, 6, 25260344, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1916, 'AEON信贷财务亚洲有限公司', '(NULL)', 'AEON银联礼品卡', 'AEON银联礼品卡', '622941', 16, 6, 25260344, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1917, 'AEON信贷财务亚洲有限公司', '(NULL)', 'AEON银联礼品卡', 'AEON银联礼品卡', '622974', 16, 6, 25260344, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1918, '中国建设银行（澳门）', 'CCB', '扣款卡', '扣款卡', '621084', 16, 6, 25270446, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1919, '渣打银行香港有限公司', '(NULL)', '港币借记卡', '港币借记卡', '622948', 16, 6, 25280344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1920, '渣打银行（香港）', '(NULL)', '银联标准卡', '银联标准卡', '621740', 16, 6, 25280344, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1921, '渣打银行香港有限公司', '(NULL)', '双币信用卡', '双币信用卡', '622482', 16, 6, 25280344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1922, '渣打银行香港有限公司', '(NULL)', '双币信用卡', '双币信用卡', '622483', 16, 6, 25280344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1923, '渣打银行香港有限公司', '(NULL)', '双币信用卡', '双币信用卡', '622484', 16, 6, 25280344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1924, '中国银盛', '(NULL)', '中国银盛预付卡', '中国银盛预付卡', '620070', 16, 6, 25290344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1925, '中国银盛', '(NULL)', '中国银盛预付卡', '中国银盛预付卡', '620068', 16, 6, 25300344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1926, '中国建设银行（亚洲）', 'CCB', '预付卡', '预付卡', '620107', 16, 6, 25330344, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1927, 'Travelex', '(NULL)', 'Travelex港币卡', 'Travelex港币卡', '622495', 16, 6, 26040344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1928, 'Travelex', '(NULL)', 'Travelex美元卡', 'Travelex美元卡', '622496', 16, 6, 26040344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1929, 'Travelex', '(NULL)', 'CashPassportCounsume', 'CashPassportCounsume', '620152', 16, 6, 26040344, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1930, 'Travelex', '(NULL)', 'CashPassportCounsume', 'CashPassportCounsume', '620153', 16, 6, 26040344, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1931, '新加坡大华银行', '(NULL)', 'UOBCUPCARD', 'UOBCUPCARD', '622433', 16, 6, 26070702, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1932, '澳门永亨银行', '(NULL)', '人民币卡', '人民币卡', '622861', 16, 6, 26080446, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1933, '澳门永亨银行', '(NULL)', '港币借记卡', '港币借记卡', '622932', 16, 6, 26080446, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1934, '澳门永亨银行', '(NULL)', '澳门币借记卡', '澳门币借记卡', '622862', 16, 6, 26080446, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1935, '澳门永亨银行', '(NULL)', '澳门币贷记卡', '澳门币贷记卡', '622775', 16, 6, 26080446, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1936, '澳门永亨银行', '(NULL)', '港币贷记卡', '港币贷记卡', '622785', 16, 6, 26080446, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1937, '日本三井住友卡公司', '(NULL)', 'MITSUISUMITOMOGINREN', 'MITSUISUMITOMOGINREN', '622920', 16, 6, 26110392,
        NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1938, '澳门国际银行', '(NULL)', '人民币卡', '人民币卡', '622434', 19, 6, 26220446, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1939, '澳门国际银行', '(NULL)', '澳门币卡', '澳门币卡', '622436', 19, 6, 26220446, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1940, '澳门国际银行', '(NULL)', '港币卡', '港币卡', '622435', 19, 6, 26220446, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1941, '大西洋银行', '(NULL)', '财运卡', '财运卡', '621232', 19, 6, 26230446, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1942, '大西洋银行', '(NULL)', '澳门币卡', '澳门币卡', '622432', 19, 6, 26230446, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1943, '大西洋银行', '(NULL)', '财运卡', '财运卡', '621247', 19, 6, 26230446, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (1944, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60110', 16, 5, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1945, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601100', 16, 6, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1946, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601101', 16, 6, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1947, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60112', 16, 5, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1948, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60112010', 16, 8, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1949, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60112011', 16, 8, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1950, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60112012', 16, 8, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1951, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60112089', 16, 8, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1952, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601121', 16, 6, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1953, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601123', 16, 6, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1954, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601124', 16, 6, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1955, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601125', 16, 6, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1956, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601126', 16, 6, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1957, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601127', 16, 6, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1958, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601128', 16, 6, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1959, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '6011290', 16, 7, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1960, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '6011291', 16, 7, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1961, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '6011292', 16, 7, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1962, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '6011293', 16, 7, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1963, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60112013', 16, 8, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1964, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '6011295', 16, 7, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1965, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601122', 16, 6, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1966, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '6011297', 16, 7, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1967, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60112980', 16, 8, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1968, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60112981', 16, 8, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1969, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60112986', 16, 8, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1970, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60112987', 16, 8, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1971, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60112988', 16, 8, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1972, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60112989', 16, 8, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1973, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60112990', 16, 8, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1974, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60112991', 16, 8, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1975, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60112992', 16, 8, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1976, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60112993', 16, 8, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1977, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '6011294', 16, 7, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1978, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '6011296', 16, 7, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1979, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60112996', 16, 8, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1980, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60112997', 16, 8, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1981, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60113', 16, 5, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1982, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '6011300', 16, 7, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1983, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60113080', 16, 8, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1984, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60113081', 16, 8, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1985, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60113089', 16, 8, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1986, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601131', 16, 6, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1987, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601136', 16, 6, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1988, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601137', 16, 6, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1989, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601138', 16, 6, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1990, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '6011390', 16, 7, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1991, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60112995', 16, 8, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1992, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '6011392', 16, 7, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1993, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '6011393', 16, 7, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1994, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60113940', 16, 8, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1995, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60113941', 16, 8, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1996, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60113943', 16, 8, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1997, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60113944', 16, 8, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1998, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60113945', 16, 8, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (1999, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60113946', 16, 8, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2000, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60113984', 16, 8, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2001, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60113985', 16, 8, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2002, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60113986', 16, 8, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2003, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60113988', 16, 8, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2004, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60112994', 16, 8, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2005, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '6011391', 16, 7, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2006, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60114', 16, 5, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2007, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601140', 16, 6, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2008, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601142', 16, 6, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2009, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601143', 16, 6, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2010, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601144', 16, 6, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2011, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601145', 16, 6, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2012, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601146', 16, 6, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2013, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601147', 16, 6, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2014, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601148', 16, 6, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2015, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601149', 16, 6, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2016, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601174', 16, 6, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2017, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60113989', 16, 8, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2018, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601178', 16, 6, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2019, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '6011399', 16, 7, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2020, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601186', 16, 6, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2021, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601187', 16, 6, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2022, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601188', 16, 6, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2023, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601189', 16, 6, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2024, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '60119', 16, 5, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2025, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '644', 16, 3, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2026, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '65', 16, 2, 26290840, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2027, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '6506', 16, 4, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2028, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '6507', 16, 4, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2029, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '6508', 16, 4, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2030, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601177', 16, 6, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2031, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '601179', 16, 6, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2032, 'Discover Financial S', '(NULL)', '发现卡', '发现卡', '6509', 16, 4, 26290840, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2033, '澳门商业银行', '(NULL)', '银联人民币卡', '银联人民币卡', '621253', 19, 6, 26320446, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2034, '澳门商业银行', '(NULL)', '银联澳门币卡', '银联澳门币卡', '621254', 19, 6, 26320446, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2035, '澳门商业银行', '(NULL)', '银联港币卡', '银联港币卡', '621255', 19, 6, 26320446, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2036, '澳门商业银行', '(NULL)', '双币种普卡', '双币种普卡', '625014', 16, 6, 26320446, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2037, '澳门商业银行', '(NULL)', '双币种白金卡', '双币种白金卡', '625016', 16, 6, 26320446, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2038, '哈萨克斯坦国民储蓄银行', '(NULL)', 'HalykbankClassic', 'HalykbankClassic', '622549', 16, 6, 26330398,
        NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2039, '哈萨克斯坦国民储蓄银行', '(NULL)', 'HalykbankGolden', 'HalykbankGolden', '622550', 16, 6, 26330398, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2040, 'Bangkok Bank Pcl', '(NULL)', '贷记卡', '贷记卡', '622354', 16, 6, 26350764, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2041, '中国工商银行（澳门）', 'ICBC', '普卡', '普卡', '625017', 16, 6, 26470446, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2042, '中国工商银行（澳门）', 'ICBC', '金卡', '金卡', '625018', 16, 6, 26470446, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2043, '中国工商银行（澳门）', 'ICBC', '白金卡', '白金卡', '625019', 16, 6, 26470446, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2044, '可汗银行', '(NULL)', '借记卡', '借记卡', '621224', 16, 6, 26530496, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2045, '可汗银行', '(NULL)', '银联蒙图借记卡', '银联蒙图借记卡', '622954', 16, 6, 26530496, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2046, '越南Vietcombank', '(NULL)', '借记卡', '借记卡', '621295', 16, 6, 26550704, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2047, '越南Vietcombank', '(NULL)', '贷记卡', '贷记卡', '625124', 16, 6, 26550704, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2048, '越南Vietcombank', '(NULL)', '贷记卡', '贷记卡', '625154', 16, 6, 26550704, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2049, '蒙古郭勒姆特银行', '(NULL)', 'Golomt Unionpay', 'Golomt Unionpay', '621049', 16, 6, 26620496, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2050, '蒙古郭勒姆特银行', '(NULL)', '贷记卡', '贷记卡', '622444', 16, 6, 26620496, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2051, '蒙古郭勒姆特银行', '(NULL)', '借记卡', '借记卡', '622414', 16, 6, 26620496, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2052, 'BC卡公司', '(NULL)', 'BC-CUPGiftCard', 'BC-CUPGiftCard', '620011', 16, 6, 26630410, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2053, 'BC卡公司', '(NULL)', 'BC-CUPGiftCard', 'BC-CUPGiftCard', '620027', 16, 6, 26630410, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2054, 'BC卡公司', '(NULL)', 'BC-CUPGiftCard', 'BC-CUPGiftCard', '620031', 16, 6, 26630410, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2055, 'BC卡公司', '(NULL)', 'BC-CUPGiftCard', 'BC-CUPGiftCard', '620039', 16, 6, 26630410, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2056, 'BC卡公司', '(NULL)', 'BC-CUPGiftCard', 'BC-CUPGiftCard', '620103', 16, 6, 26630410, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2057, 'BC卡公司', '(NULL)', 'BC-CUPGiftCard', 'BC-CUPGiftCard', '620106', 16, 6, 26630410, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2058, 'BC卡公司', '(NULL)', 'BC-CUPGiftCard', 'BC-CUPGiftCard', '620120', 16, 6, 26630410, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2059, 'BC卡公司', '(NULL)', 'BC-CUPGiftCard', 'BC-CUPGiftCard', '620123', 16, 6, 26630410, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2060, 'BC卡公司', '(NULL)', 'BC-CUPGiftCard', 'BC-CUPGiftCard', '620125', 16, 6, 26630410, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2061, 'BC卡公司', '(NULL)', 'BC-CUPGiftCard', 'BC-CUPGiftCard', '620220', 16, 6, 26630410, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2062, 'BC卡公司', '(NULL)', 'BC-CUPGiftCard', 'BC-CUPGiftCard', '620278', 16, 6, 26630410, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2063, 'BC卡公司', '(NULL)', 'BC-CUPGiftCard', 'BC-CUPGiftCard', '620812', 16, 6, 26630410, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2064, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '621006', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2065, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '621011', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2066, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '621012', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2067, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '621020', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2068, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '621023', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2069, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '621025', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2070, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '621027', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2071, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '621031', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2072, 'BC卡公司', '(NULL)', 'BC-CUPGiftCard', 'BC-CUPGiftCard', '620132', 16, 6, 26630410, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2073, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '621039', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2074, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '621078', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2075, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '621220', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2076, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625003', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2077, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '621003', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2078, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625011', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2079, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625012', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2080, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625020', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2081, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625023', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2082, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625025', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2083, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625027', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2084, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625031', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2085, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '621032', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2086, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625039', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2087, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625078', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2088, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625079', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2089, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625103', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2090, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625106', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2091, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625006', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2092, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625112', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2093, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625120', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2094, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625123', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2095, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625125', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2096, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625127', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2097, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625131', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2098, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625032', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2099, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625139', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2100, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625178', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2101, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625179', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2102, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625220', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2103, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625320', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2104, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625111', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2105, 'BC卡公司', '(NULL)', '中国通卡', '中国通卡', '625132', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2106, '莫斯科人民储蓄银行', '(NULL)', 'cup-unioncard', 'cup-unioncard', '621040', 16, 6, 26690643, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2107, '丝绸之路银行', '(NULL)', 'Classic/Gold', 'Classic/Gold', '621045', 16, 6, 26700860, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2108, '俄罗斯远东商业银行', '(NULL)', '借记卡', '借记卡', '621264', 16, 6, 26780643, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2109, 'CSC', '(NULL)', '贷记卡', '贷记卡', '622356', 16, 6, 26790422, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2110, 'CSC', '(NULL)', 'CSC借记卡', 'CSC借记卡', '621234', 16, 6, 26790422, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2111, 'Allied Bank', '(NULL)', '贷记卡', '贷记卡', '622145', 16, 6, 26930608, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2112, 'Allied Bank', '(NULL)', '贷记卡', '贷记卡', '625013', 16, 6, 26930608, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2113, '日本三菱信用卡公司', '(NULL)', '贷记卡', '贷记卡', '622130', 16, 6, 27090392, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2114, 'Baiduri Bank Berhad', '(NULL)', '借记卡', '借记卡', '621257', 16, 6, 27130096, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2115, '越南西贡商业银行', '(NULL)', '借记卡', '借记卡', '621055', 16, 6, 27200704, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2116, '越南西贡商业银行', '(NULL)', '预付卡', '预付卡', '620009', 16, 6, 27200704, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2117, '越南西贡商业银行', '(NULL)', '贷记卡', '贷记卡', '625002', 16, 6, 27200704, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2118, '菲律宾BDO', '(NULL)', '银联卡', '银联卡', '625033', 16, 6, 27240608, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2119, '菲律宾BDO', '(NULL)', '银联卡', '银联卡', '625035', 16, 6, 27240608, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2120, '菲律宾RCBC', '(NULL)', '贷记卡', '贷记卡', '625007', 16, 6, 27250608, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2121, '新加坡星网电子付款私人有限公司', '(NULL)', '预付卡', '预付卡', '620015', 16, 6, 27520702, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2122, 'Royal Bank Open Stoc', '(NULL)', '预付卡', '预付卡', '620024', 16, 6, 27550031, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2123, 'Royal Bank Open Stoc', '(NULL)', '贷记卡', '贷记卡', '625004', 16, 6, 27550031, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2124, 'Royal Bank Open Stoc', '(NULL)', '借记卡', '借记卡', '621344', 19, 6, 27550031, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2125, '乌兹别克斯坦INFINBANK', '(NULL)', '借记卡', '借记卡', '621349', 16, 6, 27650860, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2126, 'BCEL', '(NULL)', '借记卡', '借记卡', '621354', 16, 6, 27710418, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2127, '澳门通股份有限公司', '(NULL)', '双币闪付卡', '双币闪付卡', '620532', 16, 6, 28020446, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2128, '韩国乐天', '(NULL)', '贷记卡', '贷记卡', '625904', 16, 6, 28030410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2129, '巴基斯坦FAYSAL BANK', '(NULL)', '借记卡', '借记卡', '621645', 16, 6, 28040586, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2130, '俄罗斯ORIENT EXPRESS BA', '(NULL)', '信用卡', '信用卡', '625104', 16, 6, 28450643, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2131, '俄罗斯ORIENT EXPRESS BA', '(NULL)', '借记卡', '借记卡', '621647', 16, 6, 28450643, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2132, 'Mongolia Trade Devel', '(NULL)', '普卡/金卡', '普卡/金卡', '621642', 16, 6, 28530496, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2133, 'Krung Thaj Bank Publ', '(NULL)', '借记卡', '借记卡', '621654', 16, 6, 28550764, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2134, '韩国KB', '(NULL)', '贷记卡', '贷记卡', '625804', 16, 6, 28590410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2135, '韩国三星卡公司', '(NULL)', '三星卡', '三星卡', '625814', 16, 6, 28660410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2136, '韩国三星卡公司', '(NULL)', '三星卡', '三星卡', '625817', 16, 6, 28660410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2137, 'Commercial Bank of D', '(NULL)', 'PrepaidCard', 'PrepaidCard', '620079', 16, 6, 28790784, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2138, 'Commercial Bank of D', '(NULL)', 'PrepaidCard', 'PrepaidCard', '620091', 16, 6, 28790784, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2139, 'The Bancorp Bank', '(NULL)', 'UnionPay Travel Card', 'UnionPay Travel Card', '620105', 16, 6, 28880840,
        NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2140, '巴基斯坦HabibBank', '(NULL)', '借记卡', '借记卡', '621657', 16, 6, 28990586, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2141, 'The Mauritius Commer', '(NULL)', '预付卡', '预付卡', '620129', 16, 6, 29170480, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2142, '中国银行香港有限公司', 'BOC', '人民币信用卡金卡', '人民币信用卡金卡', '622346', 16, 6, 47980344, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2143, '中国银行香港有限公司', 'BOC', '信用卡普通卡', '信用卡普通卡', '622347', 16, 6, 47980344, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2144, '中国银行香港有限公司', 'BOC', '中银卡(人民币)', '中银卡(人民币)', '622348', 16, 6, 47980344, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2145, '南洋商业银行', '(NULL)', '人民币信用卡金卡', '人民币信用卡金卡', '622349', 16, 6, 47980344, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2146, '南洋商业银行', '(NULL)', '信用卡普通卡', '信用卡普通卡', '622350', 16, 6, 47980344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2147, '集友银行', '(NULL)', '人民币信用卡金卡', '人民币信用卡金卡', '622352', 16, 6, 47980344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2148, '集友银行', '(NULL)', '信用卡普通卡', '信用卡普通卡', '622353', 16, 6, 47980344, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2149, '集友银行', '(NULL)', '中银卡', '中银卡', '622355', 16, 6, 47980344, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2150, '中国银行(香港)', '(NULL)', '银联港币借记卡', '银联港币借记卡', '621041', 19, 6, 47980344, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2151, '南洋商业银行', '(NULL)', '中银卡(人民币)', '中银卡(人民币)', '622351', 16, 6, 47980344, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2152, '中银通商务支付有限公司', '(NULL)', '预付卡', '预付卡', '620048', 16, 6, 48080000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2153, '中银通商务支付有限公司', '(NULL)', '预付卡', '预付卡', '620515', 16, 6, 48080000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2154, '中银通商务支付有限公司', '(NULL)', '预付卡', '预付卡', '920000', 16, 6, 48080000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2155, '中银通支付', '(NULL)', '安徽合肥通卡', '安徽合肥通卡', '921000', 19, 6, 48080001, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2156, '中银通商务支付有限公司', '(NULL)', '铁路卡', '铁路卡', '620038', 19, 6, 48100000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2157, '中国邮政储蓄银行', 'PSBC', '银联标准白金卡', '银联标准白金卡', '622812', 16, 6, 61000000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2158, '中国邮政储蓄银行', 'PSBC', '银联标准贷记卡', '银联标准贷记卡', '622810', 16, 6, 61000000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2159, '中国邮政储蓄银行', 'PSBC', '银联标准贷记卡', '银联标准贷记卡', '622811', 16, 6, 61000000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2160, '中国邮政储蓄银行', 'PSBC', '银联标准公务卡', '银联标准公务卡', '628310', 16, 6, 61000000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2161, '中国邮政储蓄银行', 'PSBC', '上海购物信用卡', '上海购物信用卡', '625919', 16, 6, 61000000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2162, '中信银行', 'CITIC', '中信贷记卡银联卡', '中信贷记卡银联卡', '376968', 15, 6, 63020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2163, '中信银行', 'CITIC', '中信贷记卡银联卡', '中信贷记卡银联卡', '376969', 15, 6, 63020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2164, '中信银行', 'CITIC', '中信贷记卡', '中信贷记卡', '400360', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2165, '中信银行', 'CITIC', '中信贷记卡', '中信贷记卡', '403391', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2166, '中信银行', 'CITIC', '中信贷记卡', '中信贷记卡', '403392', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2167, '中信银行', 'CITIC', '中信贷记卡银联卡', '中信贷记卡银联卡', '376966', 15, 6, 63020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2168, '中信银行', 'CITIC', '中信贷记卡', '中信贷记卡', '404158', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2169, '中信银行', 'CITIC', '中信贷记卡', '中信贷记卡', '404159', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2170, '中信银行', 'CITIC', '中信贷记卡', '中信贷记卡', '404171', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2171, '中信银行', 'CITIC', '中信贷记卡', '中信贷记卡', '404172', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2172, '中信银行', 'CITIC', '中信贷记卡', '中信贷记卡', '404173', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2173, '中信银行', 'CITIC', '中信贷记卡', '中信贷记卡', '404174', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2174, '中信银行', 'CITIC', '中信贷记卡', '中信贷记卡', '404157', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2175, '中信银行', 'CITIC', '中信贷记卡', '中信贷记卡', '433667', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2176, '中信银行', 'CITIC', '中信贷记卡', '中信贷记卡', '433668', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2177, '中信银行', 'CITIC', '中信贷记卡', '中信贷记卡', '433669', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2178, '中信银行', 'CITIC', '中信贷记卡', '中信贷记卡', '514906', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2179, '中信银行', 'CITIC', '中信贷记卡', '中信贷记卡', '403393', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2180, '中信银行', 'CITIC', '中信贷记卡', '中信贷记卡', '520108', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2181, '中信银行', 'CITIC', '中信贷记卡', '中信贷记卡', '433666', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2182, '中信银行', 'CITIC', '中信贷记卡', '中信贷记卡', '558916', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2183, '中信银行', 'CITIC', '中信银联标准贷记卡', '中信银联标准贷记卡', '622678', 16, 6, 63020000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2184, '中信银行', 'CITIC', '中信银联标准贷记卡', '中信银联标准贷记卡', '622679', 16, 6, 63020000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2185, '中信银行', 'CITIC', '中信银联标准贷记卡', '中信银联标准贷记卡', '622680', 16, 6, 63020000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2186, '中信银行', 'CITIC', '中信银联标准贷记卡', '中信银联标准贷记卡', '622688', 16, 6, 63020000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2187, '中信银行', 'CITIC', '中信银联标准贷记卡', '中信银联标准贷记卡', '622689', 16, 6, 63020000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2188, '中信银行', 'CITIC', '中信银联公务卡', '中信银联公务卡', '628206', 16, 6, 63020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2189, '中信银行', 'CITIC', '中信贷记卡', '中信贷记卡', '556617', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2190, '中信银行', 'CITIC', '中信银联公务卡', '中信银联公务卡', '628209', 16, 6, 63020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2191, '中信银行', 'CITIC', '中信贷记卡', '中信贷记卡', '518212', 16, 6, 63020000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2192, '中信银行', 'CITIC', '中信银联公务卡', '中信银联公务卡', '628208', 16, 6, 63020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2193, '中信银行', 'CITIC', '中信JCB美元卡', '中信JCB美元卡', '356390', 16, 6, 63020000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2194, '中信银行', 'CITIC', '中信JCB美元卡', '中信JCB美元卡', '356391', 16, 6, 63020000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2195, '中信银行', 'CITIC', '中信JCB美元卡', '中信JCB美元卡', '356392', 16, 6, 63020000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2196, '中信银行', 'CITIC', '中信银联IC卡普卡', '中信银联IC卡普卡', '622916', 16, 6, 63020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2197, '中信银行', 'CITIC', '中信银联IC卡金卡', '中信银联IC卡金卡', '622918', 16, 6, 63020000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2198, '中信银行', 'CITIC', '中信银联IC卡白金卡', '中信银联IC卡白金卡', '622919', 16, 6, 63020000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2199, '中国光大银行', 'CEB', '存贷合一白金卡', '存贷合一白金卡', '622657', 16, 6, 63030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2200, '中国光大银行', 'CEB', '存贷合一卡普卡', '存贷合一卡普卡', '622685', 16, 6, 63030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2201, '中国光大银行', 'CEB', '理财信用卡', '理财信用卡', '622659', 16, 6, 63030000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2202, '中国光大银行', 'CEB', '存贷合一钻石卡', '存贷合一钻石卡', '622687', 16, 6, 63030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2203, '中国光大银行', 'CEB', '存贷合一IC卡', '存贷合一IC卡', '625978', 16, 6, 63030000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2204, '中国光大银行', 'CEB', '存贷合一IC卡', '存贷合一IC卡', '625980', 16, 6, 63030000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2205, '中国光大银行', 'CEB', '存贷合一IC卡', '存贷合一IC卡', '625981', 16, 6, 63030000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2206, '中国光大银行', 'CEB', '存贷合一IC卡', '存贷合一IC卡', '625979', 16, 6, 63030000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2207, '中国光大银行', 'CEB', '阳光商旅信用卡', '阳光商旅信用卡', '356839', 16, 6, 63030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2208, '中国光大银行', 'CEB', '阳光商旅信用卡', '阳光商旅信用卡', '356840', 16, 6, 63030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2209, '中国光大银行', 'CEB', '阳光信用卡(银联', '阳光信用卡(银联', '406252', 16, 6, 63030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2210, '中国光大银行', 'CEB', '阳光信用卡(银联', '阳光信用卡(银联', '406254', 16, 6, 63030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2211, '中国光大银行', 'CEB', '阳光商旅信用卡', '阳光商旅信用卡', '425862', 16, 6, 63030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2212, '中国光大银行', 'CEB', '阳光白金信用卡', '阳光白金信用卡', '481699', 16, 6, 63030000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2213, '中国光大银行', 'CEB', '安邦俱乐部信用卡', '安邦俱乐部信用卡', '524090', 16, 6, 63030000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2214, '中国光大银行', 'CEB', '足球锦标赛纪念卡', '足球锦标赛纪念卡', '543159', 16, 6, 63030000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2215, '中国光大银行', 'CEB', '光大银行联名公务卡', '光大银行联名公务卡', '622161', 16, 6, 63030000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2216, '中国光大银行', 'CEB', '积分卡', '积分卡', '622570', 16, 6, 63030000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2217, '中国光大银行', 'CEB', '炎黄卡普卡', '炎黄卡普卡', '622650', 16, 6, 63030000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2218, '中国光大银行', 'CEB', '炎黄卡白金卡', '炎黄卡白金卡', '622655', 16, 6, 63030000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2219, '中国光大银行', 'CEB', '炎黄卡金卡', '炎黄卡金卡', '622658', 16, 6, 63030000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2220, '中国光大银行', 'CEB', '贷记IC卡', '贷记IC卡', '625975', 16, 6, 63030000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2221, '中国光大银行', 'CEB', '贷记IC卡', '贷记IC卡', '625977', 16, 6, 63030000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2222, '中国光大银行', 'CEB', '银联公务卡', '银联公务卡', '628201', 16, 6, 63030000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2223, '中国光大银行', 'CEB', '银联公务卡', '银联公务卡', '628202', 16, 6, 63030000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2224, '中国光大银行', 'CEB', '银联贷记IC旅游卡', '银联贷记IC旅游卡', '625339', 16, 6, 63030000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2225, '中国光大银行', 'CEB', '贷记IC卡', '贷记IC卡', '625976', 16, 6, 63030000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2226, '华夏银行', 'HXBANK', '华夏万事达信用卡', '华夏万事达信用卡', '523959', 16, 6, 63040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2227, '华夏银行', 'HXBANK', '万事达信用卡金卡', '万事达信用卡金卡', '528709', 16, 6, 63040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2228, '华夏银行', 'HXBANK', '万事达普卡', '万事达普卡', '539867', 16, 6, 63040000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2229, '华夏银行', 'HXBANK', '万事达普卡', '万事达普卡', '539868', 16, 6, 63040000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2230, '华夏银行', 'HXBANK', '华夏信用卡金卡', '华夏信用卡金卡', '622637', 16, 6, 63040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2231, '华夏银行', 'HXBANK', '华夏白金卡', '华夏白金卡', '622638', 16, 6, 63040000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2232, '华夏银行', 'HXBANK', '华夏公务信用卡', '华夏公务信用卡', '628318', 16, 6, 63040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2233, '华夏银行', 'HXBANK', '万事达信用卡金卡', '万事达信用卡金卡', '528708', 16, 6, 63040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2234, '华夏银行', 'HXBANK', '华夏信用卡普卡', '华夏信用卡普卡', '622636', 16, 6, 63040000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2235, '华夏银行', 'HXBANK', '华夏标准金融IC信用卡', '华夏标准金融IC信用卡', '625967', 16, 6, 63040000, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2236, '华夏银行', 'HXBANK', '华夏标准金融IC信用卡', '华夏标准金融IC信用卡', '625968', 16, 6, 63040000, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2237, '华夏银行', 'HXBANK', '华夏标准金融IC信用卡', '华夏标准金融IC信用卡', '625969', 16, 6, 63040000, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2238, '浦发银行信用卡中心', '(NULL)', '移动浦发借贷合一联名卡', '移动浦发借贷合一联名卡', '625971', 16, 6,
        63100000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2239, '浦发银行信用卡中心', '(NULL)', '贷记卡', '贷记卡', '625970', 16, 6, 63100000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2240, '浦发银行信用卡中心', '(NULL)', '浦发私人银行信用卡', '浦发私人银行信用卡', '377187', 15, 6, 63100000,
        NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2241, '东亚银行', 'HKBEA', '东亚银行普卡', '东亚银行普卡', '622265', 16, 6, 63200000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2242, '东亚银行', 'HKBEA', '东亚银行金卡', '东亚银行金卡', '622266', 16, 6, 63200000, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2243, '东亚银行', 'HKBEA', '百家网点纪念版IC贷记卡', '百家网点纪念版IC贷记卡', '625972', 16, 6, 63200000, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2244, '东亚银行', 'HKBEA', '百家网点纪念版IC贷记卡', '百家网点纪念版IC贷记卡', '625973', 16, 6, 63200000, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2245, '南洋商业银行', '(NULL)', '银联个人白金信用卡', '银联个人白金信用卡', '625093', 16, 6, 63320000, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2246, '南洋商业银行', '(NULL)', '银联商务白金信用卡', '银联商务白金信用卡', '625095', 16, 6, 63320000, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2247, '北京银行', 'BJBANK', '万事达双币金卡', '万事达双币金卡', '522001', 16, 6, 64031000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2248, '北京银行', 'BJBANK', '银联标准贷记卡', '银联标准贷记卡', '622163', 16, 6, 64031000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2249, '北京银行', 'BJBANK', '银联标准贷记卡', '银联标准贷记卡', '622853', 16, 6, 64031000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2250, '北京银行', 'BJBANK', '银联标准公务卡', '银联标准公务卡', '628203', 16, 6, 64031000, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2251, '北京银行', 'BJBANK', '北京银行中荷人寿联名卡', '北京银行中荷人寿联名卡', '622851', 16, 6, 64031000, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2252, '北京银行', 'BJBANK', '尊尚白金卡', '尊尚白金卡', '622852', 16, 6, 64031000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2253, '宁波银行', 'NBBANK', '汇通贷记卡', '汇通贷记卡', '625903', 16, 6, 64083300, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2254, '宁波银行', 'NBBANK', '汇通白金卡', '汇通白金卡', '622778', 16, 6, 64083320, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2255, '宁波银行', 'NBBANK', '汇通公务卡', '汇通公务卡', '628207', 16, 6, 64083320, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2256, '齐鲁银行', 'QLBANK', '泉城公务卡', '泉城公务卡', '628379', 16, 6, 64094510, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2257, '广州银行', 'GCB', '银联标准公务卡', '银联标准公务卡', '628367', 16, 6, 64135810, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2258, '广州银行', 'GCB', '广州银行信用卡', '广州银行信用卡', '625050', 16, 6, 64135810, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2259, '龙江银行', 'DAQINGB', '金鹤卡', '金鹤卡', '622644', 16, 6, 64162640, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2260, '龙江银行', 'DAQINGB', '公务卡', '公务卡', '628333', 16, 6, 64162640, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2261, '河北银行', 'BHB', '如意贷记卡', '如意贷记卡', '622921', 16, 6, 64221210, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2262, '河北银行', 'BHB', '如意贷记卡', '如意贷记卡', '628321', 16, 6, 64221210, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2263, '杭州市商业银行', '(NULL)', '西湖卡IC卡', '西湖卡IC卡', '603367', 19, 6, 64233310, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2264, '杭州市商业银行', '(NULL)', '西湖贷记卡', '西湖贷记卡', '622286', 16, 6, 64233311, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2265, '杭州市商业银行', '(NULL)', '西湖贷记卡', '西湖贷记卡', '628236', 16, 6, 64233311, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2266, '南京银行', 'NJCB', '借记IC卡', '借记IC卡', '621777', 16, 6, 64243010, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2267, '成都市商业银行', '(NULL)', '银联标准公务卡', '银联标准公务卡', '628228', 16, 6, 64296510, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2268, '成都市商业银行', '(NULL)', '银联标准卡', '银联标准卡', '622813', 16, 6, 64296510, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2269, '成都市商业银行', '(NULL)', '银联标准卡', '银联标准卡', '622818', 16, 6, 64296510, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2270, '临商银行', 'LSBC', '公务卡', '公务卡', '628359', 16, 6, 64314730, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2271, '珠海华润银行', '(NULL)', '公务信用卡', '公务信用卡', '628270', 16, 6, 64375850, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2272, '齐商银行', 'ZBCB', '金达公务卡', '金达公务卡', '628311', 16, 6, 64384530, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2273, '锦州银行', 'BOJZ', '公务卡', '公务卡', '628261', 16, 6, 64392270, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2274, '徽商银行', 'HSBANK', '银联标准公务卡', '银联标准公务卡', '628251', 16, 6, 64403600, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2275, '徽商银行', 'HSBANK', '借记卡', '借记卡', '621775', 19, 6, 64403600, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2276, '徽商银行', 'HSBANK', '贷记卡', '贷记卡', '622651', 16, 6, 64403600, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2277, '徽商银行', 'HSBANK', '徽商银行中国旅游卡（安徽）', '徽商银行中国旅游卡（安徽）', '623203', 19, 6, 64403600,
        NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2278, '徽商银行', 'HSBANK', '贷记IC卡', '贷记IC卡', '625828', 16, 6, 64403600, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2279, '重庆银行', 'CQBANK', '银联标准卡', '银联标准卡', '622613', 16, 6, 64416910, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2280, '重庆银行', 'CQBANK', '银联标准公务卡', '银联标准公务卡', '628220', 16, 6, 64416910, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2281, '哈尔滨商行', '(NULL)', '丁香贷记卡', '丁香贷记卡', '622809', 16, 6, 64422610, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2282, '哈尔滨商行', '(NULL)', '哈尔滨银行公务卡', '哈尔滨银行公务卡', '628224', 16, 6, 64422610, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2283, '哈尔滨银行', '(NULL)', '福农准贷记卡', '福农准贷记卡', '625577', 16, 6, 64422610, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2284, '哈尔滨银行', '(NULL)', '金融IC借记卡', '金融IC借记卡', '621752', 19, 6, 64422611, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2285, '贵阳银行', '(NULL)', '甲秀公务卡', '甲秀公务卡', '628213', 16, 6, 64437010, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2286, '兰州银行', 'LZYH', '敦煌公务卡', '敦煌公务卡', '628263', 16, 6, 64478210, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2287, '南昌银行', 'NCB', '银联标准公务卡', '银联标准公务卡', '628305', 16, 6, 64484210, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2288, '青岛银行', 'QDCCB', '公务卡', '公务卡', '628239', 16, 6, 64504520, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2289, '九江银行', 'JJBANK', '庐山公务卡', '庐山公务卡', '628238', 16, 6, 64544240, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2290, '日照银行', '(NULL)', '黄海公务卡', '黄海公务卡', '628257', 16, 6, 64554770, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2291, '青海银行', 'BOQH', '三江贷记卡', '三江贷记卡', '622817', 16, 6, 64588510, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2292, '青海银行', 'BOQH', '三江贷记卡(公务卡)', '三江贷记卡(公务卡)', '628287', 16, 6, 64588510, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2293, '青海银行', 'BOQH', '三江贷记IC卡', '三江贷记IC卡', '625959', 16, 6, 64588510, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2294, '青海银行', 'BOQH', '中国旅游卡', '中国旅游卡', '62536601', 16, 8, 64588510, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2295, '潍坊银行', 'BANKWF', '鸢都公务卡', '鸢都公务卡', '628391', 16, 6, 64624580, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2296, '赣州银行', 'GZB', '长征公务卡', '长征公务卡', '628233', 16, 6, 64634280, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2297, '富滇银行', 'FDB', '富滇公务卡', '富滇公务卡', '628231', 16, 6, 64667310, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2298, '浙江泰隆商业银行', 'ZJTLCB', '泰隆公务卡(单位卡)', '泰隆公务卡(单位卡)', '628275', 16, 6, 64733450, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2299, '浙江泰隆商业银行', 'ZJTLCB', '泰隆尊尚白金卡、钻石卡', '泰隆尊尚白金卡、钻石卡', '622565', 16, 6, 64733450,
        NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2300, '浙江泰隆商业银行', 'ZJTLCB', '泰隆信用卡', '泰隆信用卡', '622287', 16, 6, 64733450, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2301, '浙江泰隆商业银行', 'ZJTLCB', '融易通', '融易通', '622717', 16, 6, 64733450, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2302, '内蒙古银行', 'H3CB', '银联标准公务卡', '银联标准公务卡', '628252', 16, 6, 64741910, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2303, '湖州银行', '(NULL)', '公务卡', '公务卡', '628306', 16, 6, 64753360, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2304, '广西北部湾银行', 'BGB', '银联标准公务卡', '银联标准公务卡', '628227', 16, 6, 64786110, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2305, '广西北部湾银行', 'BGB', 'IC借记卡', 'IC借记卡', '623001', 16, 6, 64786110, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2306, '威海市商业银行', 'WHCCB', '通达公务卡', '通达公务卡', '628234', 16, 6, 64814650, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2307, '广东南粤银行', '(NULL)', '湛江市民卡', '湛江市民卡', '621727', 19, 6, 64895910, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2308, '广东南粤银行', '(NULL)', '公务卡', '公务卡', '628237', 16, 6, 64895919, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2309, '桂林银行', 'GLBANK', '漓江公务卡', '漓江公务卡', '628219', 16, 6, 64916170, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2310, '桂林银行', 'GLBANK', '漓江卡', '漓江卡', '621456', 17, 6, 64916170, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2311, '桂林银行', 'GLBANK', '福农IC卡', '福农IC卡', '621562', 19, 6, 64916170, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2312, '龙江银行', 'DAQINGB', '玉兔贷记卡', '玉兔贷记卡', '622270', 16, 6, 64922690, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2313, '龙江银行', 'DAQINGB', '玉兔贷记卡(公务卡)', '玉兔贷记卡(公务卡)', '628368', 16, 6, 64922690, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2314, '龙江银行', 'DAQINGB', '福农准贷记卡', '福农准贷记卡', '625588', 16, 6, 64922690, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2315, '龙江银行', 'DAQINGB', '联名贷记卡', '联名贷记卡', '625090', 16, 6, 64922690, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2316, '柳州银行', '(NULL)', '龙城公务卡', '龙城公务卡', '628293', 16, 6, 64956140, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2317, '上海农商银行贷记卡', '(NULL)', '鑫卡', '鑫卡', '622611', 16, 6, 65012900, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2318, '上海农商银行贷记卡', '(NULL)', '商务卡', '商务卡', '622722', 16, 6, 65012900, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2319, '上海农商银行贷记卡', '(NULL)', '银联标准公务卡', '银联标准公务卡', '628211', 16, 6, 65012900, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2320, '上海农商银行贷记卡', '(NULL)', '福农卡', '福农卡', '625500', 16, 6, 65012900, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2321, '上海农商银行贷记卡', '(NULL)', '鑫通卡', '鑫通卡', '625989', 16, 6, 65012900, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2322, '广州农村商业银行', '(NULL)', '太阳信用卡', '太阳信用卡', '625080', 16, 6, 65055810, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2323, '广州农村商业银行', '(NULL)', '公务卡', '公务卡', '628235', 16, 6, 65055810, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2324, '佛山顺德农村商业银行', '(NULL)', '恒通贷记卡（公务卡）', '恒通贷记卡（公务卡）', '628322', 16, 6, 65085883,
        NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2325, '佛山顺德农村商业银行', '(NULL)', '恒通贷记卡', '恒通贷记卡', '625088', 16, 6, 65085883, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2326, '云南省农村信用社', 'YNRCC', '金碧贷记卡', '金碧贷记卡', '622469', 16, 6, 65097300, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2327, '云南省农村信用社', 'YNRCC', '金碧公务卡', '金碧公务卡', '628307', 16, 6, 65097300, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2328, '承德银行', 'BOCD', '热河公务卡', '热河公务卡', '628229', 16, 6, 65131410, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2329, '德州银行', 'DZBANK', '长河公务卡', '长河公务卡', '628397', 16, 6, 65154680, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2330, '福建省农村信用社联合社', '(NULL)', '万通贷记卡', '万通贷记卡', '628232', 16, 6, 65173900, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2331, '福建省农村信用社联合社', '(NULL)', '万通贷记卡', '万通贷记卡', '622802', 16, 6, 65173900, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2332, '福建省农村信用社联合社', '(NULL)', '福建海峡旅游卡', '福建海峡旅游卡', '622290', 16, 6, 65173900, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2333, '天津农村商业银行', '(NULL)', '吉祥信用卡', '吉祥信用卡', '622829', 16, 6, 65191100, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2334, '天津农村商业银行', '(NULL)', '吉祥信用卡', '吉祥信用卡', '628301', 16, 6, 65191100, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2335, '成都农村商业银行', '(NULL)', '天府贷记卡', '天府贷记卡', '622808', 16, 6, 65226510, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2336, '成都农村商业银行', '(NULL)', '天府公务卡', '天府公务卡', '628308', 16, 6, 65226510, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2337, '成都农村商业银行', '(NULL)', '天府借记卡', '天府借记卡', '623088', 19, 6, 65226510, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2338, '江苏省农村信用社联合社', '(NULL)', '圆鼎贷记卡', '圆鼎贷记卡', '622815', 16, 6, 65243000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2339, '江苏省农村信用社联合社', '(NULL)', '圆鼎贷记卡', '圆鼎贷记卡', '622816', 16, 6, 65243000, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2340, '江苏省农村信用社联合社', '(NULL)', '银联标准公务卡', '银联标准公务卡', '628226', 16, 6, 65243000, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2341, '上饶银行', 'SRBANK', '三清山公务卡', '三清山公务卡', '628223', 16, 6, 65264330, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2342, '上饶银行', 'SRBANK', '三清山IC卡', '三清山IC卡', '621416', 16, 6, 65264331, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2343, '东营银行', '(NULL)', '财政公务卡', '财政公务卡', '628217', 16, 6, 65274550, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2344, '临汾市尧都区农村信用合作联社', '(NULL)', '天河贷记公务卡', '天河贷记公务卡', '628382', 16, 6, 65341770,
        NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2345, '临汾市尧都区农村信用合作联社', '(NULL)', '天河贷记卡', '天河贷记卡', '625158', 16, 6, 65341770, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2346, '无锡农村商业银行', 'WRCB', '银联标准公务卡', '银联标准公务卡', '628369', 16, 6, 65373020, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2347, '无锡农村商业银行', 'WRCB', '金阿福贷记卡', '金阿福贷记卡', '622569', 16, 6, 65373020, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2348, '湖南农村信用社联合社', '(NULL)', '福祥贷记卡', '福祥贷记卡', '622906', 16, 6, 65385500, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2349, '湖南农村信用社联合社', '(NULL)', '福祥公务卡', '福祥公务卡', '628386', 16, 6, 65385500, NULL, NULL, NULL,
        NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2350, '湖南农信', '(NULL)', '福农卡', '福农卡', '625519', 16, 6, 65385500, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2351, '湖南农信', '(NULL)', '福祥贷记卡（福农卡）', '福祥贷记卡（福农卡）', '625506', 16, 6, 65385500, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2352, '江西省农村信用社联合社', '(NULL)', '百福福农卡', '百福福农卡', '621592', 16, 6, 65394200, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2353, '江西省农村信用社联合社', '(NULL)', '百福公务卡', '百福公务卡', '628392', 16, 6, 65394200, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2354, '安徽省农村信用社', 'ARCU', '金农卡', '金农卡', '621778', 19, 6, 65473600, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2355, '邢台银行', 'XTB', '金牛市民卡', '金牛市民卡', '620528', 19, 6, 65541310, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2356, '商丘市商业银行', '(NULL)', '百汇卡', '百汇卡', '621748', 19, 6, 65675060, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2357, '商丘市商业银行', '(NULL)', '公务卡', '公务卡', '628271', 16, 6, 65675061, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2358, '华融湘江银行', 'HRXJB', '华融湘江银行华融公务卡普卡', '华融湘江银行华融公务卡普卡', '628328', 16, 6,
        65705500, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2359, '北京银行', 'BJBANK', '借记IC卡', '借记IC卡', '623111', 16, 6, 4031000, NULL, NULL, NULL, NULL, NULL,
        NULL);
INSERT INTO `sys_bank_bins`
VALUES (2360, '阜新银行', 'FXCB', '借记IC卡', '借记IC卡', '623166', 18, 6, 4672299, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2361, '衡水市商业银行', '(NULL)', '借记IC卡', '借记IC卡', '623068', 19, 6, 5611481, NULL, NULL, NULL, NULL,
        NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2362, '湖南省农村信用社联合社', '(NULL)', '福祥借记IC卡', '福祥借记IC卡', '623090', 19, 6, 14385500, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2363, '重庆南川石银村镇银行', '(NULL)', '麒麟借记卡', '麒麟借记卡', '621653004', 19, 9, 15436900, NULL, NULL,
        NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2364, 'BC卡公司', '(NULL)', '贷记卡', '贷记卡', '625244', 16, 6, 26630410, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2365, '澳门BDA', '(NULL)', '汇业卡', '汇业卡', '621274', 19, 6, 27860446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2366, '澳门BDA', '(NULL)', '汇业卡', '汇业卡', '621324', 19, 6, 27860446, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2368, '邮政储蓄银行', 'PSBC', '绿卡通', '绿卡通', '622180', 19, 6, 1000000, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2369, '建设银行', 'CCB', NULL, NULL, '623668', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2370, '农业银行', NULL, NULL, NULL, '623052', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2371, '中国工商银行', NULL, NULL, NULL, '621472', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2372, '山西农信社', NULL, NULL, NULL, '623051', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2373, '中国民生银行', NULL, NULL, NULL, '621691', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2374, '邮政储蓄银行', NULL, NULL, NULL, '621582', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2375, '河南农信社', NULL, NULL, NULL, '623059', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2376, '建设银行', 'CCB', NULL, NULL, '623094', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2377, '郑州银行', NULL, NULL, NULL, '623531', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_bank_bins`
VALUES (2378, '建设银行', 'CCB', NULL, NULL, '621534', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`
(
    `sys_dept_id`        int(11)                                                       NOT NULL AUTO_INCREMENT,
    `sys_dept_name`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '机构名称',
    `sys_dept_pid`       int(11)                                                       NULL DEFAULT NULL COMMENT '父机构',
    `sys_dept_tree_id`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '机构链路',
    `sys_dept_code`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组织编码',
    `sys_dept_principal` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '部门负责人',
    `sys_dept_contact`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '负责人联系方式',
    `sys_dept_sort`      int(11)                                                       NULL DEFAULT NULL COMMENT '优先级',
    `create_name`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by`          int(11)                                                       NULL DEFAULT NULL COMMENT '创建人',
    `create_time`        datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`        datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `update_name`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by`          int(11)                                                       NULL DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`sys_dept_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '组织机构'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dept
-- ----------------------------

-- ----------------------------
-- Table structure for sys_dict
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict`
(
    `sys_dict_id`        int(11)                                                       NOT NULL AUTO_INCREMENT,
    `sys_dict_name`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典名称',
    `sys_dict_status`    int(11)                                                       NULL DEFAULT NULL COMMENT '状态; 0:隐藏',
    `sys_dict_in_system` int(11)                                                       NULL DEFAULT NULL COMMENT '是否内置; 0:非内置',
    `sys_dict_code`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典编码, 唯一',
    `sys_dict_remark`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
    `sys_dict_i18n`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典i18n',
    `sys_dict_pid`       int(11)                                                       NULL DEFAULT NULL COMMENT 'PID',
    `create_name`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by`          int(11)                                                       NULL DEFAULT NULL COMMENT '创建人',
    `create_time`        datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`        datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `update_name`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by`          int(11)                                                       NULL DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`sys_dict_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 6
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '字典表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict
-- ----------------------------
INSERT INTO `sys_dict`
VALUES (1, '厂家', 1, 0, 'CHANG_JIA', '三方厂家', '', 0, 'sa', NULL, '2024-08-31 20:29:00', '2024-08-31 20:35:57', 'sa',
        1);
INSERT INTO `sys_dict`
VALUES (2, '模板类型', 1, 0, 'MU_BAN_LEI_XING', '', '', 0, 'sa', NULL, '2024-09-01 18:16:46', '2024-09-09 09:33:56',
        'sa', 1);
INSERT INTO `sys_dict`
VALUES (3, '短信', 1, 0, 'DUAN_XIN', '', '', 2, 'sa', 1, '2024-09-07 09:07:09', '2024-09-09 09:23:22', 'sa', 1);
INSERT INTO `sys_dict`
VALUES (4, '微信', 1, 0, 'WEI_XIN', NULL, NULL, 2, 'sa', 1, '2024-09-09 10:07:50', NULL, NULL, NULL);
INSERT INTO `sys_dict`
VALUES (5, '组件功能', 1, 0, 'ZU_JIAN_GONG_NENG', '', '', 0, 'sa', 1, '2024-10-24 17:17:50', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_dict_item
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_item`;
CREATE TABLE `sys_dict_item`
(
    `sys_dict_item_id`     int(11)                                                       NOT NULL AUTO_INCREMENT,
    `sys_dict_id`          int(11)                                                       NULL DEFAULT NULL COMMENT '字典ID',
    `sys_dict_item_name`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典子项名称',
    `sys_dict_item_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
    `sys_dict_item_status` int(11)                                                       NULL DEFAULT NULL COMMENT '状态;0: 隐藏',
    `sys_dict_item_sort`   int(11)                                                       NULL DEFAULT NULL COMMENT '优先级',
    `sys_dict_item_i18n`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典i18n',
    `sys_dict_item_code`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '编码',
    `sys_dict_item_delete` int(11)                                                       NULL DEFAULT NULL COMMENT '是否删除0:未删除',
    `create_name`          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by`            int(11)                                                       NULL DEFAULT NULL COMMENT '创建人',
    `create_time`          datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`          datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `update_name`          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by`            int(11)                                                       NULL DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`sys_dict_item_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 13
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '字典项子项'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict_item
-- ----------------------------
INSERT INTO `sys_dict_item`
VALUES (1, 1, '海康云耀', '海康云耀', 1, 1, '', 'HAIKANGYUNYAO', 0, 'sa', NULL, '2024-09-01 10:02:02',
        '2024-09-01 10:08:45', 'sa', 1);
INSERT INTO `sys_dict_item`
VALUES (2, 1, '阿里云', '阿里云', 1, 1, NULL, 'ALIYUN', 0, 'sa', NULL, '2024-09-01 10:10:03', NULL, NULL, NULL);
INSERT INTO `sys_dict_item`
VALUES (3, 1, '百度云', '百度云', 1, 1, NULL, 'BDYUN', 0, 'sa', NULL, '2024-09-01 10:10:13', NULL, NULL, NULL);
INSERT INTO `sys_dict_item`
VALUES (4, 2, '短信', '', 1, 1, '', 'SMS', 0, 'sa', 1, '2024-09-03 13:35:36', '2024-09-09 10:04:58', 'sa', 1);
INSERT INTO `sys_dict_item`
VALUES (5, 3, '短信通知', '', 1, 2, '', 'DUAN_XIN_TONG_ZHI', 0, 'sa', 1, '2024-09-03 13:48:51', '2024-09-05 16:13:21',
        'sa', 1);
INSERT INTO `sys_dict_item`
VALUES (6, 2, '站内', '', 1, 2, '', 'WEB', 0, 'sa', 1, '2024-09-03 13:54:31', '2024-09-03 13:55:18', 'sa', 1);
INSERT INTO `sys_dict_item`
VALUES (7, 2, '微信', NULL, 1, 2, NULL, 'WECHAT', 0, 'sa', 1, '2024-09-03 13:55:13', '2024-09-05 15:54:08', 'sa', 1);
INSERT INTO `sys_dict_item`
VALUES (8, 3, '验证码', '', 1, 1, '', 'YAN_ZHENG_MA', 0, 'sa', 1, '2024-09-09 09:24:59', '2024-09-09 09:31:22', 'sa',
        1);
INSERT INTO `sys_dict_item`
VALUES (9, 4, '微信通知', NULL, 1, NULL, NULL, 'WEI_XIN_TONG_ZHI', 0, 'sa', 1, '2024-09-09 10:08:38', NULL, NULL, NULL);
INSERT INTO `sys_dict_item`
VALUES (10, 1, '讯飞', '', 1, 1, '', 'XUN_FEI', 0, 'sa', 1, '2024-10-24 15:07:24', NULL, NULL, NULL);
INSERT INTO `sys_dict_item`
VALUES (11, 5, '天气', '', 1, 1, '', 'TIAN_QI', 0, 'sa', 1, '2024-10-24 17:18:02', NULL, NULL, NULL);
INSERT INTO `sys_dict_item`
VALUES (12, 5, '时间', '', 1, 1, '', 'SHI_JIAN', 0, 'sa', 1, '2024-10-28 09:31:55', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log`
(
    `sys_log_id`       int(11)                                                       NOT NULL AUTO_INCREMENT,
    `sys_log_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账号',
    `sys_log_address`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求IP',
    `sys_log_url`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求地址',
    `sys_log_param`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求参数',
    `sys_log_from`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求模块',
    `sys_log_status`   int(11)                                                       NULL DEFAULT NULL COMMENT '请求状态; 0: 失败; 1: 成功',
    `sys_log_cost`     int(11)                                                       NULL DEFAULT NULL COMMENT '耗时',
    `create_name`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by`        int(11)                                                       NULL DEFAULT NULL COMMENT '创建人',
    `create_time`      datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`      datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `update_name`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by`        int(11)                                                       NULL DEFAULT NULL COMMENT '更新人',
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
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`
(
    `sys_menu_id`               int(11)                                                       NOT NULL AUTO_INCREMENT,
    `sys_menu_name`             varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单名称',
    `sys_menu_path`             varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单路径',
    `sys_menu_i18n`             varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '国际化',
    `sys_menu_title`            varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标题',
    `sys_menu_perm`             varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '权限',
    `sys_menu_pid`              int(11)                                                       NULL DEFAULT NULL COMMENT '父菜单',
    `sys_menu_tree_id`          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单路径',
    `sys_menu_component`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单组件',
    `sys_menu_icon`             varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单图标',
    `sys_menu_extra_icon`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '右侧菜单图标',
    `sys_menu_type`             int(11)                                                       NULL DEFAULT NULL COMMENT '菜单 0 按钮 1',
    `sys_menu_sort`             int(11)                                                       NULL DEFAULT NULL COMMENT '优先级',
    `sys_menu_hidden`           int(11)                                                       NULL DEFAULT NULL COMMENT '是否隐藏；0:显示',
    `sys_menu_show_parent`      int(11)                                                       NULL DEFAULT NULL COMMENT '当一个子级菜单是否隐藏父级菜单；0:显示',
    `sys_menu_hidden_tag`       int(11)                                                       NULL DEFAULT NULL COMMENT '是否在标签栏隐藏；0:否',
    `sys_menu_fixed_tag`        int(11)                                                       NULL DEFAULT NULL COMMENT '是否固定标签；0:否',
    `sys_menu_redirect`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '路由重定向',
    `sys_menu_enter_transition` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '进场动画',
    `sys_menu_leave_transition` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '离场动画',
    `sys_menu_active_path`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单激活',
    `sys_menu_frame_src`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链接地址',
    `sys_menu_keep_alive`       int(11)                                                       NULL DEFAULT NULL COMMENT '是否缓存；0:否',
    `create_name`               varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by`                 int(11)                                                       NULL DEFAULT NULL COMMENT '创建人',
    `create_time`               datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`               datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `update_name`               varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by`                 int(11)                                                       NULL DEFAULT NULL COMMENT '更新人',
    `sys_menu_role`             varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属权限',
    `sys_menu_home`             int(11)                                                       NULL DEFAULT NULL COMMENT '是否只显示在主页； 0: 否',
    `sys_menu_engine`           int(11)                                                       NULL DEFAULT NULL COMMENT '是否engine布局主页；0: 否',
    PRIMARY KEY (`sys_menu_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 26
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜单'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu`
VALUES (1, 'system', '/manage', NULL, '系统管理', NULL, 0, '0,1', NULL, 'ep:setting', NULL, 0, 1, 0, 1, NULL, NULL,
        NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2024-08-19 11:23:52', 'sa', 1, '', NULL, NULL);
INSERT INTO `sys_menu`
VALUES (2, 'menu', '/manage/menu', NULL, '菜单管理', NULL, 1, '0,1,2', '/manage/menu/index', 'ep:menu', NULL, 0, 1, 0,
        1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2024-08-19 11:23:52', 'sa', 1, '', NULL,
        NULL);
INSERT INTO `sys_menu`
VALUES (3, 'setting', '/manage/setting', NULL, '系统配置', NULL, 1, '0,1,3', '/manage/setting/index', 'ep:setting',
        NULL, 0, 999, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2024-08-19 11:23:52',
        'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL);
INSERT INTO `sys_menu`
VALUES (4, 'role', '/manage/role', NULL, '角色管理', NULL, 1, '0,1,4', '/manage/role/index', 'ep:user', NULL, 0, 2, 0,
        1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2024-08-19 11:36:18', 'sa', 1, '', NULL,
        NULL);
INSERT INTO `sys_menu`
VALUES (5, '', '', '', '新增', 'sys:role:add', 4, '0,1,4', '', '', NULL, 3, 1, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa',
        NULL, '2024-08-19 15:58:58', NULL, NULL, NULL, '', NULL, NULL);
INSERT INTO `sys_menu`
VALUES (6, NULL, '', '', '删除', 'sys:role:delete', 4, '0,1,4', '', '', NULL, 3, 1, 0, 1, 0, 0, '', '', '', '', '', 0,
        'sa', NULL, '2024-08-19 16:15:03', NULL, NULL, NULL, '', NULL, NULL);
INSERT INTO `sys_menu`
VALUES (7, '', '', '', '修改', 'sys:role:update', 4, '0,1,4', '', '', NULL, 3, 1, 0, 1, 0, 0, '', '', '', '', '', 0,
        'sa', NULL, '2024-08-19 16:15:41', NULL, NULL, NULL, '', NULL, NULL);
INSERT INTO `sys_menu`
VALUES (8, 'user', '/manage/user', '', '用户管理', '', 1, '0,1,8', '/manage/user/index', 'ep:user-filled', NULL, 0, 3,
        0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-20 15:51:07', '2024-08-20 16:29:51', 'sa', 1, '', NULL,
        NULL);
INSERT INTO `sys_menu`
VALUES (9, 'userLog', '/manage/log/user', '', '登录日志', '', 1, '0,1,9', '/manage/log/user/index', 'ep:alarm-clock',
        NULL, 0, 998, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-21 15:46:26', '2024-08-21 16:12:12', 'sa',
        NULL, '', NULL, NULL);
INSERT INTO `sys_menu`
VALUES (11, 'dept', '/manage/dept', '', '机构管理', '', 1, '0,1,11', '/manage/dept/index', 'ep:star', NULL, 0, 5, 0, 1,
        0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-21 16:25:12', '2024-08-21 16:25:29', 'sa', 1, '', NULL, NULL);
INSERT INTO `sys_menu`
VALUES (12, '', '', '', '删除', 'sys:user:delete', 8, '0,1,8', '', '', NULL, 3, 1, 0, 1, 0, 0, '', '', '', '', '', 0,
        'sa', NULL, '2024-08-23 13:29:45', NULL, NULL, NULL, '', NULL, NULL);
INSERT INTO `sys_menu`
VALUES (13, '', '', '', '编辑', 'sys:user:update', 8, '0,1,8', '', '', NULL, 3, 1, 0, 1, 0, 0, '', '', '', '', '', 0,
        'sa', NULL, '2024-08-23 13:29:56', NULL, NULL, NULL, '', NULL, NULL);
INSERT INTO `sys_menu`
VALUES (14, '', '', '', '新增', 'sys:user:add', 8, '0,1,8,14', '', '', NULL, 3, 1, 0, 1, 0, 0, '', '', '', '', '', 0,
        'sa', NULL, '2024-08-23 13:30:07', '2024-08-23 13:30:39', 'sa', NULL, '', NULL, NULL);
INSERT INTO `sys_menu`
VALUES (15, 'dict', '/manage/dict', '', '字典管理', '', 1, '0,1,15', '/manage/dict/index', 'humbleicons:dashboard',
        NULL, 0, 996, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-27 08:41:06', '2024-09-02 16:00:14', 'sa',
        NULL, '', NULL, NULL);
INSERT INTO `sys_menu`
VALUES (16, 'secret', '/manage/secret', '', '密钥管理', '', 1, '0,1,16', '/manage/secret/index', 'ep:lock', NULL, 0,
        997, 1, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-08-30 11:26:23', '2024-08-30 11:26:57', 'sa', NULL,
        '', NULL, NULL);
INSERT INTO `sys_menu`
VALUES (17, 'demo', '/demo/index', '', '自定义弹框组件', '', 18, '0,18,17', '/demo/index', 'ep:alarm-clock', NULL, 0, 1,
        0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-09-02 09:54:43', '2024-09-02 15:09:18', 'sa', NULL, '',
        NULL, NULL);
INSERT INTO `sys_menu`
VALUES (20, 'template', '/manage/template', '', '模板管理', '', 1, '0,1,20', '/manage/template/index', 'ep:cellphone',
        NULL, 0, 997, 0, 1, 0, 0, '', '', '', '', '', 0, 'sa', NULL, '2024-09-02 15:59:48', '2024-09-02 16:00:05', 'sa',
        NULL, '', NULL, NULL);
INSERT INTO `sys_menu`
VALUES (24, 'sfc', '/manage/sfc', '', '组件管理', NULL, 1, '0,1,24', '/manage/sfc/index', 'ri:compasses-2-fill', NULL,
        0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'sa', 1, '2024-10-24 16:34:53',
        '2024-10-24 16:35:51', 'sa', 1, '', NULL, NULL);

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`
(
    `sys_role_id`        int(11)                                                       NOT NULL AUTO_INCREMENT,
    `sys_role_name`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色名称',
    `sys_role_code`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色编码; 唯一',
    `sys_role_in_system` int(11)                                                       NULL DEFAULT NULL COMMENT '是否系统角色; 0：非系统角色',
    `sys_role_status`    int(11)                                                       NULL DEFAULT NULL COMMENT '是否启用; 0：不启用',
    `sys_role_sort`      int(11)                                                       NULL DEFAULT NULL COMMENT '优先级',
    `sys_role_remark`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
    `sys_role_hidden`    int(11)                                                       NULL DEFAULT 0 COMMENT '是否显示; 0:显示',
    `create_name`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by`          int(11)                                                       NULL DEFAULT NULL COMMENT '创建人',
    `create_time`        datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`        datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `update_name`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by`          int(11)                                                       NULL DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`sys_role_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 4
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role`
VALUES (1, '超级管理员', 'SUPER_ADMIN', 1, 1, 1, '超级管理员', 0, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_role`
VALUES (2, '管理员', 'ADMIN', 1, 1, 1, '超级管理员', 0, NULL, NULL, NULL, '2024-08-21 13:49:40', 'sa', NULL);
INSERT INTO `sys_role`
VALUES (3, '运维', 'OPS', 1, 1, 1, '运维', 0, NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`
(
    `sys_role_menu_id` int(11) NOT NULL AUTO_INCREMENT,
    `sys_role_id`      int(11) NULL DEFAULT NULL COMMENT '角色ID',
    `sys_menu_id`      int(11) NULL DEFAULT NULL COMMENT '菜单ID',
    PRIMARY KEY (`sys_role_menu_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 98
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色 - 菜单关系表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu`
VALUES (44, 3, 2);
INSERT INTO `sys_role_menu`
VALUES (45, 3, 4);
INSERT INTO `sys_role_menu`
VALUES (46, 3, 5);
INSERT INTO `sys_role_menu`
VALUES (47, 3, 6);
INSERT INTO `sys_role_menu`
VALUES (48, 3, 7);
INSERT INTO `sys_role_menu`
VALUES (49, 3, 8);
INSERT INTO `sys_role_menu`
VALUES (82, 2, 4);
INSERT INTO `sys_role_menu`
VALUES (83, 2, 5);
INSERT INTO `sys_role_menu`
VALUES (84, 2, 6);
INSERT INTO `sys_role_menu`
VALUES (85, 2, 7);
INSERT INTO `sys_role_menu`
VALUES (86, 2, 8);
INSERT INTO `sys_role_menu`
VALUES (87, 2, 12);
INSERT INTO `sys_role_menu`
VALUES (88, 2, 13);
INSERT INTO `sys_role_menu`
VALUES (89, 2, 14);
INSERT INTO `sys_role_menu`
VALUES (90, 2, 1);
INSERT INTO `sys_role_menu`
VALUES (91, 2, 2);
INSERT INTO `sys_role_menu`
VALUES (92, 2, 11);
INSERT INTO `sys_role_menu`
VALUES (93, 2, 15);
INSERT INTO `sys_role_menu`
VALUES (94, 2, 16);
INSERT INTO `sys_role_menu`
VALUES (95, 2, 20);
INSERT INTO `sys_role_menu`
VALUES (96, 2, 9);
INSERT INTO `sys_role_menu`
VALUES (97, 2, 3);

-- ----------------------------
-- Table structure for sys_secret
-- ----------------------------
DROP TABLE IF EXISTS `sys_secret`;
CREATE TABLE `sys_secret`
(
    `sys_secret_id`           int(11)                                                       NOT NULL AUTO_INCREMENT,
    `sys_secret_group`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分组',
    `sys_secret_code`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '编码, 可自行作为唯一标识',
    `sys_secret_app_id`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'appid',
    `sys_secret_app_secret`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'appsecret',
    `sys_secret_sign`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '签名',
    `sys_secret_delete`       int(11)                                                       NULL DEFAULT NULL COMMENT '是否删除0:未删除',
    `sys_secret_status`       int(11)                                                       NULL DEFAULT NULL COMMENT '是否启用0:未启用',
    `sys_secret_cdn`          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'cdn',
    `sys_secret_endpoint`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '端点',
    `sys_secret_api_address`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'api地址',
    `sys_secret_dict_item_id` int(11)                                                       NULL DEFAULT NULL COMMENT '字典项ID(字典1)',
    `sys_secret_remark`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
    `sys_secret_function`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '功能',
    `create_name`             varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by`               int(11)                                                       NULL DEFAULT NULL COMMENT '创建人',
    `create_time`             datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`             datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `update_name`             varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by`               int(11)                                                       NULL DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`sys_secret_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 2
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '密钥'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_secret
-- ----------------------------
INSERT INTO `sys_secret`
VALUES (1, '阿里系', 'ALI', 'LTAI5tKQQnvCS3yEjGxjHmCd', 'Pm45AutVaPLqWCOUNeZ7gEQBTYJQAh', '犀鸟校园', 0, 1, NULL, NULL,
        NULL, 2, NULL, 'SMS', 'sa', 1, '2024-09-06 13:32:21', '2024-09-06 13:36:12', 'sa', 1);

-- ----------------------------
-- Table structure for sys_setting
-- ----------------------------
DROP TABLE IF EXISTS `sys_setting`;
CREATE TABLE `sys_setting`
(
    `sys_setting_id`         int(11)                                                       NOT NULL AUTO_INCREMENT,
    `sys_setting_sort`       int(11)                                                       NULL DEFAULT NULL COMMENT '优先级',
    `sys_setting_name`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置名称',
    `sys_setting_value`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置值',
    `sys_setting_status`     int(11)                                                       NULL DEFAULT NULL COMMENT '是否启用; 0:不启用',
    `sys_setting_value_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置类型; 用于前端组件判断; e.g.BOOL：布尔',
    `sys_setting_group`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置所属分组',
    `sys_setting_collect`    int(255)                                                      NULL DEFAULT NULL COMMENT '是否是多个配置集合;0:单个',
    `sys_setting_in_system`  int(11)                                                       NULL DEFAULT NULL COMMENT '是否系统配置; 0:非系统配置',
    `sys_setting_app_inner`  int(11)                                                       NULL DEFAULT NULL COMMENT '配置是否作用域系统启动',
    `sys_setting_remark`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置备注',
    `sys_setting_config`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置参数',
    `sys_setting_admin`      int(11)                                                       NULL DEFAULT NULL COMMENT '是否只有管理员生效',
    `sys_setting_version`    int(11)                                                       NULL DEFAULT NULL COMMENT '版本',
    `create_name`            varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by`              int(11)                                                       NULL DEFAULT NULL COMMENT '创建人',
    `create_time`            datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`            datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `update_name`            varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by`              int(11)                                                       NULL DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`sys_setting_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 38
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统全局配置'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_setting
-- ----------------------------
INSERT INTO `sys_setting`
VALUES (1, 2, 'CheckCodeOpen', 'false', 1, 'Boolean', 'default', 0, 1, 0, '验证码开关', NULL, NULL, 9, NULL, NULL, NULL,
        '2024-12-08 13:47:54', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (2, 1, 'SystemName', '后台管理系统', 1, 'String', 'default', 0, 1, 0, '系统标题', NULL, NULL, 9, NULL, NULL,
        NULL, '2024-12-08 13:47:54', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (3, 3, 'SlidingBlockOpen', 'false', 1, 'Boolean', 'default', 0, 1, 0, '滑动验证码', NULL, NULL, 9, NULL, NULL,
        NULL, '2024-12-08 13:47:54', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (4, 1, 'CrashPageOpen', 'false', 1, 'Boolean', 'config', 0, 1, 0, '调试模式崩溃当前页', NULL, NULL, 19, 'sa',
        NULL, '2024-08-18 11:22:26', '2024-12-11 11:09:18', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (5, 2, 'CodecResponseOpen', 'false', 1, 'Boolean', 'config', 0, 1, 0, '返回加密', NULL, NULL, 19, 'sa', NULL,
        '2024-08-18 11:31:57', '2024-12-11 11:09:18', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (6, 7, 'WatermarkOpen', 'false', 1, 'Boolean', 'config', 0, 1, 0, '水印', NULL, NULL, 19, 'sa', NULL,
        '2024-08-19 19:53:55', '2024-12-11 11:09:18', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (7, 4, 'LoopDebuggerOpen', 'false', 1, 'Boolean', 'config', 0, 1, 0, '循环debugger', NULL, NULL, 19, 'sa', NULL,
        '2024-08-18 10:52:15', '2024-12-11 11:09:18', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (8, 5, 'Version', '1', 1, 'Number', 'config', 0, 1, 0, '系统版本', '25e82ffb-f4d4-41f0-9082-6d978b661e37', NULL,
        95, NULL, NULL, NULL, '2024-12-11 11:09:18', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (9, 7, 'SocketUrl', 'http://172.16.2.226:29181', 1, 'Array', 'config', 0, 1, 0, 'socket.io服务器地址', NULL, 1,
        19, NULL, NULL, NULL, '2024-12-11 11:09:18', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (10, 6, 'SocketOpen', 'true', 1, 'Boolean', 'config', 0, 1, 0, 'socket.io是否开启', NULL, 1, 19, NULL, NULL,
        NULL, '2024-12-11 11:09:18', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (11, 8, 'SocketPath', '/socket.io', 1, 'String', 'config', 0, 1, 0, 'socket.io路径', NULL, NULL, NULL, NULL,
        NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_setting`
VALUES (12, 8, 'WatermarkColor', '#409EFF', 1, 'Color', 'config', 0, 1, 0, '水印颜色', NULL, NULL, 19, NULL, NULL, NULL,
        '2024-12-11 11:09:18', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (13, 9, 'WatermarkAlpha', '0.35', 1, 'Number', 'config', 0, 1, 0, '水印透明度', NULL, NULL, 19, NULL, NULL, NULL,
        '2024-12-11 11:09:18', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (14, 10, 'WatermarkRotate', '-30', 1, 'Number', 'config', 0, 1, 0, '水印角度', NULL, NULL, 19, NULL, NULL, NULL,
        '2024-12-11 11:09:18', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (15, 11, 'CodecRequestOpen', 'false', 1, 'Boolean', 'config', 0, 1, 0, '请求加密', NULL, NULL, 19, NULL, NULL,
        NULL, '2024-12-11 11:09:18', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (16, 11, 'CodecRequestKey', '0123456789abcdef', 1, 'String', 'config', 0, 1, 1, '请求加密密钥', NULL, NULL, 19,
        NULL, NULL, NULL, '2024-12-11 11:09:18', 'sa', 1);
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
        '2024-12-08 13:40:57', 'sa', 1);
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
VALUES (34, 5, 'CheckTotpCodeOpen', 'false', 1, 'Boolean', 'default', NULL, 0, NULL, '开启totp', NULL, NULL, NULL, 'sa',
        1, '2024-12-08 11:56:39', '2024-12-08 13:47:54', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (35, 1, 'WebrtcOpen', 'true', 1, 'Boolean', 'webrtc', NULL, NULL, NULL, '是否开启', NULL, NULL, NULL, NULL, NULL,
        NULL, '2024-12-11 10:26:15', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (36, 2, 'WebrtcUrl', 'http://172.16.2.226:29182', 1, 'String', 'webrtc', NULL, NULL, NULL, 'webrtc服务器地址',
        NULL, NULL, NULL, NULL, NULL, NULL, '2024-12-11 10:26:15', 'sa', 1);
INSERT INTO `sys_setting`
VALUES (37, 3, 'WebrtcContext', '/webrtc', 1, 'String', 'webrtc', NULL, NULL, NULL, '路径', NULL, NULL, NULL, NULL,
        NULL, NULL, '2024-12-11 10:26:15', 'sa', 1);

-- ----------------------------
-- Table structure for sys_sfc
-- ----------------------------
DROP TABLE IF EXISTS `sys_sfc`;
CREATE TABLE `sys_sfc`
(
    `sys_sfc_id`           int(11)                                                       NOT NULL AUTO_INCREMENT,
    `sys_sfc_name`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件名称',
    `sys_sfc_chinese_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件名称',
    `sys_sfc_function`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件功能, 多个逗号分隔',
    `sys_sfc_icon`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件图标',
    `sys_sfc_category`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类；HOME',
    `sys_sfc_path`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件存放路径',
    `sys_sfc_status`       int(11)                                                       NULL DEFAULT NULL COMMENT '组件状态; 0:未启用',
    `sys_sfc_desc`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件描述',
    `sys_sfc_proxy`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '代理地址',
    `sys_sfc_type`         int(11)                                                       NULL DEFAULT NULL COMMENT '组件类型; 0:文件; 1:文本',
    `sys_sfc_content`      longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci     NULL COMMENT '组件文本',
    `sys_sfc_model_cache`  longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci     NULL COMMENT '模块缓存',
    `sys_sfc_delay`        int(11)                                                       NULL DEFAULT NULL COMMENT '延迟时间; 2000',
    `sys_sfc_version`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '版本',
    `create_name`          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by`            int(11)                                                       NULL DEFAULT NULL COMMENT '创建人',
    `create_time`          datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`          datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `update_name`          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by`            int(11)                                                       NULL DEFAULT NULL COMMENT '更新人',
    `sys_sfc_timeout`      int(11)                                                       NULL DEFAULT NULL COMMENT '超时时间; 3000',
    PRIMARY KEY (`sys_sfc_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 6
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '组件库'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_sfc
-- ----------------------------
INSERT INTO `sys_sfc`
VALUES (2, 'time', '时间组件', '时间', 'ep:timer', 'HOME', NULL, 1, NULL, NULL, 1,
        '<template>\n  <el-card shadow=\"hover\" header=\"时钟\" class=\"item-background\">\n    <div class=\"time\">\n      <h2>{{ time }}</h2>\n      <p>{{ day }}</p>\n    </div>\n  </el-card>\n</template>\n\n<script>\nimport { dateFormat } from \"@/utils/date\";\nexport default {\n  title: \"时钟\",\n  icon: \"ep:clock\",\n  description: \"演示部件效果\",\n  data() {\n    return {\n      time: \"\",\n      day: \"\"\n    };\n  },\n  mounted() {\n    this.showTime();\n    setInterval(() => {\n      this.showTime();\n    }, 1000);\n	this.$emit(\"loaded\", true);\n  },\n  methods: {\n    showTime() {\n      this.time = dateFormat(new Date(), \"hh:mm:ss\");\n      this.day = dateFormat(new Date(), \"yyyy年MM月dd日\");\n    }\n  }\n};\n</script>\n\n<style scoped>\n.item-background {\n  background: linear-gradient(to right, #8e54e9, #4776e6);\n  color: #fff;\n}\n.time h2 {\n  font-size: 40px;\n}\n.time p {\n  font-size: 14px;\n  margin-top: 13px;\n  opacity: 0.7;\n}\n</style>\n',
        '[{\"name\":\"Date\",\"value\":\"import * as Date from \\\"@/utils/date\\\";\"}]', NULL, '1.0.0', 'sa', 1,
        '2024-10-25 09:26:42', '2024-10-25 09:27:12', 'sa', 1, NULL);
INSERT INTO `sys_sfc`
VALUES (3, 'weather', '天气', '天气', 'meteocons:clear-day', 'HOME', '.\\sfc\\3\\weather.vue', 1, NULL, NULL, 0, 'null',
        'null', NULL, '1.0.0', 'sa', 1, '2024-10-28 09:32:27', '2024-10-28 13:36:56', 'sa', 1, NULL);
INSERT INTO `sys_sfc`
VALUES (4, 'local-weather', '本地天气组件', '天气', 'ep:basketball', 'HOME', '@repo/module/weather/index.vue', 1, NULL,
        NULL, 3, NULL, '[]', NULL, '1.0.0', 'sa', 1, '2024-11-24 10:09:56', '2024-11-24 10:10:49', 'sa', 1, NULL);
INSERT INTO `sys_sfc`
VALUES (5, 'frame', 'test', '天气', 'ep:trophy-base', 'HOME',
        'https://blog.csdn.net/m0_64494670/article/details/135143478', 1, NULL, NULL, 2, NULL, '[]', NULL, '1.0.0',
        'sa', 1, '2024-11-24 12:55:03', '2024-11-24 13:21:45', 'sa', 1, NULL);

-- ----------------------------
-- Table structure for sys_sfc_install
-- ----------------------------
DROP TABLE IF EXISTS `sys_sfc_install`;
CREATE TABLE `sys_sfc_install`
(
    `sys_sfc_install_id` int(11)                                                       NOT NULL AUTO_INCREMENT,
    `user_id`            int(11)                                                       NULL DEFAULT NULL COMMENT '安裝的用户ID',
    `sys_sfc_id`         int(11)                                                       NULL DEFAULT NULL COMMENT '组件ID',
    `create_name`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by`          int(11)                                                       NULL DEFAULT NULL COMMENT '创建人',
    `create_time`        datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`        datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `update_name`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by`          int(11)                                                       NULL DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`sys_sfc_install_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 5
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户安装的组件'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_sfc_install
-- ----------------------------
INSERT INTO `sys_sfc_install`
VALUES (1, 1, 2, 'sa', 1, '2024-10-25 09:33:10', NULL, NULL, NULL);
INSERT INTO `sys_sfc_install`
VALUES (2, 1, 3, 'sa', 1, '2024-10-29 10:46:53', NULL, NULL, NULL);
INSERT INTO `sys_sfc_install`
VALUES (3, 1, 4, 'sa', 1, '2024-11-24 10:16:03', NULL, NULL, NULL);
INSERT INTO `sys_sfc_install`
VALUES (4, 1, 5, 'sa', 1, '2024-11-24 12:55:07', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_sms_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_sms_log`;
CREATE TABLE `sys_sms_log`
(
    `sys_sms_log_id`          int(11)                                                       NOT NULL AUTO_INCREMENT,
    `sys_sms_log_target`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '接收人',
    `sys_sms_log_template_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板ID',
    `sys_sms_log_param`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '参数',
    `sys_sms_cost`            decimal(10, 2)                                                NULL DEFAULT NULL COMMENT '耗时时间',
    `sys_sms_code`            varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误码',
    `sys_sms_message`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误信息',
    `sys_sms_app_type`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '短信厂家',
    `sys_sms_app_id`          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '短信密钥',
    `sys_sms_app_sign`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '签名',
    `create_name`             varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by`               int(11)                                                       NULL DEFAULT NULL COMMENT '创建人',
    `create_time`             datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`             datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `update_name`             varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by`               int(11)                                                       NULL DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`sys_sms_log_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '短信下发日志表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_sms_log
-- ----------------------------

-- ----------------------------
-- Table structure for sys_task_sso
-- ----------------------------
DROP TABLE IF EXISTS `sys_task_sso`;
CREATE TABLE `sys_task_sso`
(
    `sys_task_sso_id`         int(11)                                                       NOT NULL AUTO_INCREMENT,
    `sys_task_sso_seq`        int(11)                                                       NULL DEFAULT NULL COMMENT '任务序号',
    `sys_task_sso_tid`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务ID',
    `sys_task_sso_status`     int(11)                                                       NULL DEFAULT NULL COMMENT '任务状态; 0: 未开始; 1: 正在传输; 2: 完成; -1: 处理异常',
    `sys_task_sso_name`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务名称',
    `sys_task_sso_number`     int(11)                                                       NULL DEFAULT NULL COMMENT '分片数量',
    `sys_task_sso_chuck`      bigint(20)                                                    NULL DEFAULT NULL COMMENT '当前文件大小',
    `sys_task_sso_summary`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务描述',
    `sys_task_sso_total`      bigint(20)                                                    NULL DEFAULT NULL COMMENT '任务总大小',
    `sys_task_sso_md5`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件md5',
    `sys_task_sso_local_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '子任务本地路径',
    `sys_task_sso_version`    int(11)                                                       NULL DEFAULT NULL COMMENT '任务锁',
    `sys_task_sso_type`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'sso类型',
    `sys_task_bucket`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '存储桶',
    `create_name`             varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by`               int(11)                                                       NULL DEFAULT NULL COMMENT '创建人',
    `create_time`             datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`             datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `update_name`             varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by`               int(11)                                                       NULL DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`sys_task_sso_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = 'sso任务表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_task_sso
-- ----------------------------

-- ----------------------------
-- Table structure for sys_task_sso_item
-- ----------------------------
DROP TABLE IF EXISTS `sys_task_sso_item`;
CREATE TABLE `sys_task_sso_item`
(
    `sys_task_sso_item_id`         int(11)                                                       NOT NULL AUTO_INCREMENT,
    `sys_task_sso_item_seq`        int(11)                                                       NULL DEFAULT NULL COMMENT '任务序号',
    `sys_task_sso_id`              varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务ID',
    `sys_task_sso_item_status`     int(11)                                                       NULL DEFAULT NULL COMMENT '任务状态; 0: 未开始; 1: 正在传输; 2: 完成; -1: 处理异常',
    `sys_task_sso_item_version`    int(11)                                                       NULL DEFAULT NULL COMMENT '任务锁',
    `sys_task_sso_item_md5`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件md5',
    `sys_task_sso_item_local_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '子任务本地路径',
    `create_name`                  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by`                    int(11)                                                       NULL DEFAULT NULL COMMENT '创建人',
    `create_time`                  datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`                  datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `update_name`                  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by`                    int(11)                                                       NULL DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`sys_task_sso_item_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = 'sso任务表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_task_sso_item
-- ----------------------------

-- ----------------------------
-- Table structure for sys_template
-- ----------------------------
DROP TABLE IF EXISTS `sys_template`;
CREATE TABLE `sys_template`
(
    `sys_template_id`                int(11)                                                       NOT NULL AUTO_INCREMENT,
    `sys_dict_item_id1`              int(11)                                                       NULL DEFAULT NULL COMMENT '适用厂家类型, 字典(dictId = 1)子项',
    `sys_dict_item_id2`              int(11)                                                       NULL DEFAULT NULL COMMENT '类别字典表(dictId=2)',
    `sys_dict_item_id3`              int(11)                                                       NULL DEFAULT NULL COMMENT '子类别字典表(dictId=3)',
    `sys_template_sort`              int(11)                                                       NULL DEFAULT NULL COMMENT '模板优先级',
    `sys_template_name`              varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
    `sys_template_code`              varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '唯一编码',
    `sys_template_content`           text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci         NULL COMMENT '模板内容',
    `sys_template_delete`            int(11)                                                       NULL DEFAULT NULL COMMENT '是否删除0:未删除',
    `sys_template_status`            int(11)                                                       NULL DEFAULT NULL COMMENT '是否启用:0:未启用',
    `sys_template_type`              int(11)                                                       NULL DEFAULT NULL COMMENT '模板类型;0:系统;1:用户;2:其它',
    `sys_template_default`           int(11)                                                       NULL DEFAULT NULL COMMENT '是否默认:0:未启用',
    `sys_template_approve`           int(11)                                                       NULL DEFAULT NULL COMMENT '审批状态;0:未审批; 1: 审批中; 2:已通过; 3: 未通过',
    `sys_template_approve_reject`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '拒绝原因',
    `sys_template_approve_create_by` int(11)                                                       NULL DEFAULT NULL COMMENT '审批人',
    `sys_template_disabled`          int(11)                                                       NULL DEFAULT NULL COMMENT '模板编辑状态;0:可编辑; 1: 不可编辑;',
    `sys_template_origin`            varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源',
    `sys_template_origin_uuid`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源uuid',
    `sys_secret_id`                  int(11)                                                       NULL DEFAULT NULL COMMENT '密钥ID',
    `create_name`                    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by`                      int(11)                                                       NULL DEFAULT NULL COMMENT '创建人',
    `create_time`                    datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`                    datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `update_name`                    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by`                      int(11)                                                       NULL DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`sys_template_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '模板管理'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_template
-- ----------------------------

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`
(
    `sys_user_id`           int(11)                                                       NOT NULL AUTO_INCREMENT,
    `sys_user_username`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账号',
    `sys_user_nickname`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
    `sys_user_password`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
    `sys_user_salt`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '盐',
    `sys_user_delete`       int(11)                                                       NULL DEFAULT NULL COMMENT '是否删除; 0: 未删除',
    `sys_user_status`       int(11)                                                       NULL DEFAULT NULL COMMENT '是否启用；0：禁用',
    `sys_user_phone`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
    `sys_user_email`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
    `sys_user_sex`          int(11)                                                       NULL DEFAULT NULL COMMENT '性别; 0:女;1:男; 2:其他',
    `sys_user_full_address` varchar(400) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '居住地址',
    `sys_user_in_system`    int(11)                                                       NULL DEFAULT NULL COMMENT '是否系统账号; 0: 非系统账号',
    `sys_user_avatar`       text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci         NULL COMMENT '头像',
    `sys_user_card`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证',
    `sys_dept_id`           int(11)                                                       NULL DEFAULT NULL COMMENT '部门ID',
    `sys_user_remark`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
    `sys_user_last_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后一次登录地点',
    `sys_user_last_ip`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后一次登录IP',
    `sys_user_register_ip`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '注册地址',
    `sys_user_login_count`  int(11)                                                       NULL DEFAULT NULL COMMENT '登录次数',
    `sys_client_id`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户端ID',
    `sys_user_totp_code`    varchar(600) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'topt',
    `create_name`           varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by`             int(11)                                                       NULL DEFAULT NULL COMMENT '创建人',
    `create_time`           datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`           datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `update_name`           varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by`             int(11)                                                       NULL DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`sys_user_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 3
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user`
VALUES (1, 'sa', '超级管理员', '53006d111477a9b5b16c4b38ac8fad69', '123456', 0, 1, '12345678901', 'test@qq.com', NULL,
        NULL, 1,
        'data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/4gHYSUNDX1BST0ZJTEUAAQEAAAHIAAAAAAQwAABtbnRyUkdCIFhZWiAH4AABAAEAAAAAAABhY3NwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAQAA9tYAAQAAAADTLQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAlkZXNjAAAA8AAAACRyWFlaAAABFAAAABRnWFlaAAABKAAAABRiWFlaAAABPAAAABR3dHB0AAABUAAAABRyVFJDAAABZAAAAChnVFJDAAABZAAAAChiVFJDAAABZAAAAChjcHJ0AAABjAAAADxtbHVjAAAAAAAAAAEAAAAMZW5VUwAAAAgAAAAcAHMAUgBHAEJYWVogAAAAAAAAb6IAADj1AAADkFhZWiAAAAAAAABimQAAt4UAABjaWFlaIAAAAAAAACSgAAAPhAAAts9YWVogAAAAAAAA9tYAAQAAAADTLXBhcmEAAAAAAAQAAAACZmYAAPKnAAANWQAAE9AAAApbAAAAAAAAAABtbHVjAAAAAAAAAAEAAAAMZW5VUwAAACAAAAAcAEcAbwBvAGcAbABlACAASQBuAGMALgAgADIAMAAxADb/2wBDAAEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQH/2wBDAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQH/wAARCAA+AGADAREAAhEBAxEB/8QAHgAAAgICAwEBAAAAAAAAAAAABgcEBQIIAAMJAQr/xAA7EAABAwIFAgQDBgQFBQAAAAABAgMEBREABhIhMQdBEyJRYQgUcQkVIzKBkUKhseEWJDND0SVSwfDx/8QAHQEAAgIDAQEBAAAAAAAAAAAABQYDBAECBwAICf/EADoRAAEDAgQEBQEHAwQCAwAAAAECAxEEIQAFEjEGQVFhBxMicYGRFDKhscHR8CNCUhUzYvEI4RYkU//aAAwDAQACEQMRAD8A/e+oLHCbjv8A+3tifUJib4DqS4IITIP5/XGBUu1rcX3t7/tiRIQRMjuJt/OZvvjGp0WiPfp2k/z4xwLcTc/1T/bG0I6j6/8AvGqluHfl2/gxiVqNz35ttzjOhPT8TjULMcj3/wCrYxBcUrdO/a1tvT6884wQgb2588ZSpSjAE3gwNvn/AL/THeQbWI3I3Hb/AJ/a9j9MRSkkwZA5/r/N+2JiXEkCLfE/zpv8Tj6lG99O1xv7fr+vHa2IyuCR9D/O+J0pUQCfxjr2xmotI/MQP1P/AIxga1XEn+dNv2xIdAsYH86i+MglC0FSPMN/W3f3/e9rcY8SsGFW6yBsfj8se8pKkhSZVfse9rftBxxMcK3Vt7WI/t9ceStSe/v/AD85xt5CSJ536ST0P899sfA22hVyQbdj239zvtax25xhSlKk3jnvG/PlOMhKUERBAO8bXuB/IvhL5h+ILpdl+ZUKXNzJGYqkBC/EjKCSUuJCiEklfJKfTjgHBqm4bzmqQ081SKUw4RpUJ9QMX+6eRn8MDn89yynK23KhKHEAym0g/WN9/nDRq2aaJSKY/VJlRgIjR0a3FrmMNoCbHfWpekbep+hwJDD2rSW1pM7lCgB9QMWKusp6OlcqXlo0ITqPrG155ncdsDtO6jZaqtNbqsKdFkRXQShbEth5BsArZaFFKjYjYX5Hrtv9kekgIUe6Ukg/It+ONMvrKbMqVNWytsIWCoBSkTYTeSDz6YjwM8UytyS3ClMR0x16XQ8+2jXbY6dXhg/mHGra/tjVLLqdeppwkQRDaoHuYMDczP7Yqt19LUPLZSoJLaylRKhBiOsWnv1M4I5+Z8v0xsuTKrTGEJT5i7OiNAbeq3Rb63xKxTVdRAap3Vk7BLLiiepsPy+MTvOUTA/qOsgC93UAfiexnAZJ6x9PoTqGpGZKC0pxQQjxK3TkalHsnW+L34Ftz6YtP5TmDIQpymqEhZASCw6mSdrEXJuLYiRX5ekEpfYtefOb7bGYP854vV9RMpfIqqBrtG+WbSkl01WD4diCQSvxtG4va5HN/pCrLK1CglVNUBShIHkO7GL/AHe/KcSprKNweYmopykWJ89uPayrG0Hp2vjV3P8A8c3S/IMp+NKZkVQMGxfpr/zTKgCQVJcjx30FItckKItv9eicPeE+d8RNIcpqhhkrE6HtKFAG+y3UHnaRvaxgYVc443y/KNWtC3Up5tSsR7pSoG29zH4Y11rH2tXw/wBPkJjyIVRLmopP+YIKSNiFf5E6SLcGxvh7Z/8AHHjDSCKqlg7elJEG82qL9j398IFR448NNOFtxp7UDG6rkTy8s+2Bt77ZT4eIb5hN0usvOpF7MvOOXt3sinKPbf8Afk4rVv8A47cWUyS65V0mkW/s35D/AHzH7DECfHvhwufZ2makq3sVm3SzW07X/PA1Xvto+g8iBMh0uk15FRWytuOoKfUUPG2klKaaDe3a4t353zQeAHEXmIcqKmkLSVArkoAKADMHzj+V+/Keq8bMnU1oZZqA4tOlBSFGFGejc9T+UzjSHqd9rvVBlup03L0qtQMwSHFOQZrzE1LTbZDukKJaaF/O2f8AUSNjbY46VlHgdRJrWHqtth6iQmHWkLaKlKBAOxXvCuRMkdScKOZ+K1f9geapnX2qtRlpa0OAJSdW86eo/uHtEY8hs7/FF1mzXXZ2Yf8AFAcl1Nep1el0qVcm9x81fuf6k847VTcGcN0VKhhui8ttpJ0glOwj/gOUcj9bY5nXcTZ5XJStFYDUrPrUZMk9gvt1527Mj4qftks7w+j2Y6E0h2FMlUxsMSW5KEPghonWhYd1Aqve4tc4454i+FOR8PZbV1SKpAeKPNQ3BB9XqgekRA2i4/NpyPxBzzi95rJlUzrTNQQ0t5Kh6Qn0kkgk7227zscKX4KvtpM8J6W0LKM5hypzYTKnXJMmS29IcHy7KfOpTwUTdone25PfC74a+HuTcTUDOuqbFQpBUpMHVGmeSTYx++GTirOsy4CytLDSnn0NrKQtSz6tkx6o7f8Au4xtNG+2Qz1OnONwKEEKjOlLhadZSFEbefS7Y3JA39PpbpNB4KZHUGraVUoCkEpkIOqZTedM2n3645s34wZsWnXTSFtSQVJMga4IufVvuTcx3jAV1U+0x6l9QqXIhl6oUUvg/ixpnhqTdNti26SOb82225Fm3h3wgyTI6hDiks1QTfQ43IPPmO0fphcrvFPPc5ZUgpfp0KJGtKwIO3IkzG9oERyxoX1S+Ibqlmd2gymeouZ6f92y2pKgxVpCBJDbpXoc0KNwb234G174tcW8GZdVLoE02WU6Qh5JV5bIEjXPTpaTtznGWeNK8UjiXK99K0Nqv5igSqLTzuPm4w35vx6dVHOm03JLWYK540llhtNSFRX8wjwmloKkueJqBUVXO25HbjE9V4c5Uttqr+x04W20gBvygNR0De1z6fxthbofE/OWGn6AVD7iXHV/1i6dSfUrmTOxm4FgL4m9Pfjeznl7KqKNWKa7mF8w5Edc+ouIkPKL0fwg6Vur1XQTrFwSFe+ANFwClOYqqWqlymRqSoMtkpSNJnSEgQJ2ty7Ya2vFKrRQ/ZXaUVJKVoLjmlaiVpCZkkGRP59L6lZo6k1etVWoVJVQlMGXLekBlLyglnxFlWhIBsAknTtsAD7W61AZYZaSdehtKCo7qIAEmRf3A5EjqOY1zj9ZUOVCSpJcWpwIBgJJMxbkJj88AOVevCcs55cjyJ33jIQ0QY0hwuH8yhcpWQB6bk++wwgZxm6a2uVkgXocSdXpMK3AAsexvfl1syZZlFVS0Kc4U0pwWGlWxgSYM73t7YMKN1xlws1t1KTEQuFInoecS5oLTbWkJOxNgLpv9TuBbBSoylJy4BVSptQZ9JBIKjqJt3PWOvScWsrz5Sq5INMlWh0SlQBG0Qb3A6fMY3vztnLpbnPo3Us5tVmlQ65FQww1T2EaFrJjPazcJsPMylPI3INtsIOU12Z5dmzdCEO1NMtRUp5ZlIhQj3EK6xFrY6nmblDmOVLqFpapnwlIDaQAY0G9p7H3MT18tal1/fyzPWqIwioNNuAoSopUkhKgdgoi/G/B+vI6DX1n2pkNJHlrAhWkQb9YtO0deffltI/5Na4CoqQmCJNuoEW6f9740V67dasndXcqVCVl1Mi8CImK543PiNo8JWn8NBI1JP6cnHzB4q+L3DfFdN9my5S/NLCW061AiUoifuJtItfbft9LeHvhpnXDlQH8xLUeaXEAQDpUoqGyjfSZMdicVPwfyolDS1PnEIQuIpO6gCfwVXG/F7i23txgN4O8fZLwk+Hc5WfL8ko9KkpOrSocwq1/zxH4tcPZvniFMMAKYLoKUlJMEqF7K7XIA588b35Qd++8sdRcy5Yeaady7FeluqccSFFSXord0gKbUd3Ra19j73Ha6Xxd4Zcps4qKJ8NvlKlUxU6gSrWgCx0k2nbvyBjl6/CjOUO5UhbaPs75T5ydJukhZ/ygbC5E2MjpC6O5lzBn/IrlbqdTjuPplLas4+hK9KdYtpW7q/h2sONvQ4JcH+LVDV5Z5ubVAXVBcA+akQm8C5JBFudzPcYo8UeHj+V5qKWiYKaQIkhKCfVYH7sC4PKbYzoNVldQMwS8vUiZGS/Q3gmaHXmwhQACzoJWkHynaxPubYo1Hi7Q5hmaaWhqdAZfSFlSxGnUCRNhFwd95G84q1XhtU5bQKq8wpypuraUWAhBlJIgah6ryCYgW98BXWTN8nIGbKblWPJQmbMiIdSW1hTeoNsqUboJBuXL/m59d8WuLfF2ly55igZrE+c8w2oFLoUgEtoJ1aTE+oWJ2k8sDeEfC419M/Wv0xLSH3LKRCtJWvTY9Y6R84usndXolTacoTkr/q8Rh1b61uAINm1EaSTzdCv4ibkdsW+G/E7J8zQaZVSn7e02pbq1OoSkgJJ3MSYB5nlvjbPuBHcoWipLCvsbrqG0BDfqBKkpJMAxOocuW07qHNPXWDl+vx6NOfWXp76kMlKjp7q5sQQQPUW4wLzDxayqizJmiU/Jec0WcBTYKjUQCOXM78sMtF4cO11AqupmNKGmwtUovftaT1+s7nCnlVJc7qU/Vob7Talt7OuLSBYLKrXJT6jvhFzLi+iHGDtcw8m6YCvMT/kroZ3PxPQYLOZXUt8Ooow2vy0q+5oVN0gEXHPfmbiOuDGrZ7rDMVxr59lSWkFKgl0KsASSAAs7c8cX4w3O8foVTKL9UhQS2S3DiZHMDc9SdrD3wDy3gtIq6dbdMsKdcGs6CLmdjA6DmZNzith9XanS8lzlypRXTEvXeZbUpTilEO8IClXsNVrp5IAt3gy7xBySky93Mq50KDS7w4nVurlBv6RfoOmGXPuB82frmsty5ASp1oKSShWkGECZBA/umd+oFzgTrmZVzaCzWaepSG3W1vAKuF2SNRuLg3+vfjjE2ccd5cvK05lldShKnW1rALqCRFxYEEEj3/dUy7guqazVVFmDKlOocQhxQQoJ9RvBgzzO+3viCOgdRjxZ8Jhh5pmU64pQQiyTqUpQJGnjc+vrf1/IGt8Ui24wdE+hM3F/SJP3gZm/KelsfdxyaqVUNhSnEpIkA2ATaPiLfpibR+mFbytDSxFckp8NJB8MWBTpsRun0B7WA/bEJ8Squtc/pqcbSSD6VCBEmfvHbvM/nbeyYpUS5R/aUj/JIIsT1I2tfeMXuWK1m7LtPr+XIgnqazIhcWSUFIGlS2nPPdQJ3ZHY7/ynqeNsxU5TPNZo8whghTrSXIS4AD6ViDNyOY2GxwOrKNlwp/8ArpSpv7oIAKY+bEb9OvZKSaf1SyLXTBpb9dRSDqdU0wtCY5UpQPCtJ4URsMOlN4jrqqGW83VSr2DYcgzG9goST3uZwP8AsNEtX9eiaqHP81plXPnIPvGxAvuAXdFcv58l5jzBVpVZq1E3U/rWtKfmbNpNiUocvf8ALvbgi+JzxwtumedYzdQfS2pRUlwhalhO5OnfrfpjGYUlE4y229QtLbSIShSZSkHkADcdLme84Ls3Zbrua82wanKqMuU/DQWEPLOpR0BtFwdPB8MH1PB9+eHxRzR5t9dTmL77yFrQ2txyVBAUoADoAEgC3Q+2+X0OVU1GsNUtO2CZLSQYUbzaeszzuTe+KenZHrjOZXloqMqIpa0IKwbFepSk6TZBvqBsdhfV2JBxPReKubsJU4xmFS24pJ1rQ7BII5noASBHLabYqHK8qzJzy6qkY8tI1JC0EjVuIF7yBF+57XfU7oXVqjNoVVfkSY5jsoc8ZQHmu0oa76Dub+wI/lNS+K1csuuLq3al4KKg4pYK2zJmPuxHcXvveSNPQZfStLZQyyhpUApAhJG4t8W2sbjC6rvSivSGFO02tTEv7kPMqOojnktH19Nt7nbE9P4q5g5WErrHlORJlwaom/P3mfbczio7luVEFKaSnUn/ABCZH8j8rExgTy30qzd4s8T61UXEpJA8ZVwRZF/9v1J7jg772wVd8X6xYDZzF0FAgpLgk8/r2xPSZfkiCSpimQtN0iCCD23ExvtvecNvJPTpxma1Hq01T1NUu77cgEsqOoDzgI91diLE2xUrPE+veoHA3XOkT/shwQogKvH0F1c4tgjTpypyrbQWmA5BAeIOpA9Ox3HKxG4B74KK9lqmATYLM1luL4bjcdCbhF1IUkBCSk83H79+cAGPEviFRQgV1X5KSJZ8wFKUz6hH4fG4uBFUZRkjtQpaG6XzUqClOgHUoi4mJA2O/tY49C6XBjGPGXPbGkNN/MlKR+fQnX2t+YK5PptY7fIuY8TLdbe8pyXgSGfVbSCQmeZMcwN/jDF/rD739ZZGlqxMC0CN4i2n5PMHElWX8qT5Ll2yWFJOnyJ5Nz9Nrg/2F8RUfGtZSUbQdcioBhYk2EAc/kb/ALYJs8QsutBsKBXBnbmI/ePx64Gj07yzFfdlNsfihRcZuhFibe+9rX4/+XkcfNuQlx4wqy4PXeL7D4HTcAiXa3Lg4S6TqUqTtN/cxbnbEeRluiVVwx5scKWRb/TQDYWHJH02sb+1rY1e4rUFeZQOnTy9Z35zBvz29++JFs5e8jz6edQEDaxiduVxPaDigfyTFbUtilsWAulZCAnykWt5B3Fh+o2G2PJ41r6VClOOnS6DN1bRcftPzE3EZm20pCEI+9zJAkE3n/sT84Z2QOluSE052ZmJoich26LBv8pUokeYjtbgW398JldxxmH2xCWHP6Cp1yTvI25b9pscC0USWkErkT369R379dxjud6R5RqVYdfgsKKG1pebJCNlNqK08dthxvjLXiFW0jixUOwwpOgAE87fNj1j622pGWXHVJV93TKYInnF+ZsPruMd+dcsIqjEanSGklDLKY7QCRfShJAv32Fzzb3wXyzjnyw4tpw6VkqWSTefqB/L741rWwnbkLc+u3Xa/OAY3jABTcgZWpDZjzWlB1KVEjSn81gOTe+43wdZ4ybUsVLTpLyrbyBI/O5se0dMB0vkANMk+dP4foBHX6YGKdlOiKl1RMhrS0tahEslIJGlFr323IVx+vtJW5ynQiobWfNcGpVzBPqHXaImZt+NleVuICHXp1OeqJNzzHxHWYjFpA6eZVaYW5WUKTD1nWU6QqxJI7je1yfTbb1rUnF1clQbS5K5tcnt+0TbaTidikpUOpccmE78rWn68gP1wLVLpfkCqOuimpcUGbrauUXuk6k8E3Gwvf0vxg7R8T5slaitSYUIMg2BH15i9/fbBJ2my5IS4yVa1mFT/wAt9j0n/wB4d9L6p5SboM2PIag/NOCzZWshadyfKNe+3t72wiPcH16KhtQdcWjnCRB+iRYk+3zilWZrTsNBDRQ4VoSSlKpBVGxA537z74jZZzXSnng6GGXWlJGmxUU/1HqNu2AGcZBVJWpPmLbIVJ2G89Rta0fPQQUdcpTfmJaIUQbRcQI6xzgQeW2JOZM0luoU0wqMuQ0pweKlpKlJ0lK+fOCOP3+uJsn4eNUhxtb6QsjSFkiZkX2P1E8vbAtx51dUlSypA1/dPMQYHebCAeWIVZzxEgy/HNACHEpT+FpXq7E7a+5735vhwyfgss1SGnq5Kk3MKIiJH/Ec/ggYZmKtx1xNM0lQkbp67He0XBj2t0FV9XYzJdD+WlRS6CGVuJWnxVEbBBLhvc7Dbt3w71nANHXtNpp69oqau6EESkBRJkaLCN/nFOtVUsGC2twkwPSCRvHP8BymBiliZ7+blpVUJf3EhSlaYslYb8RBNw5ZWskKTY3B4UDthXzHgtmlV5dK2mttdxpOoJIAkSI2Ij4iOgOpdrd1F1Kd4I2FoN5/PphkV7qfQ8s0GnSYM+LPlPPttOpYd1LShS2kknSU7AKP0sfe6n/8Ifr6hxt1hVOhKSsFaQASJIEmb+mOfPFGmqqpFQTqWRFwNhvNo+ef0g4pa91KgzjTnI7zZkOsoUmOlZ8Ra1IsUgXJv+tvc2wSy/gcopKxavQhoEFZAgAEXNu1/wAutiszd9xxphtlxZXYqSJAMXJg3v8AyQBi/p+YqUiAJ9ZQzGf0nUxKJSsWSdyNRIIJta//ABgL/ojhUGqQl0apC2xqBvEbAbG1ue2LIJpm0rcQQ5IkmQdxael+nzcyk6l1Ip7EqpKhxW5CG1KIU0VKS2AlO5IWCLX3Nzz6DD0nht0U9KHSUFSbJULm5ItF5iAN+Q6YJO1j9Q2wQ2spa36EAnva8Db9sKis9V5FRadYhpcUCSAhs33Fxa2q9xuOe2GDKeF26d9C32QRuoqTAue0ctvmb7WG64LaJTSklMcjMj57fqOyQoHVrMcDM05iRFnCMFhIUsWQEm+9yeB37Hk98dFq+FstXQsususJcVJKEn1SZtBHO/tftixR1AUtRWyBIgIP9v1PLp8RbG1jnTnKrlYeQt2qBtuS822lCgPw0uEJCh80Bew7A78EWGOFv8VZy0zqQKU6UAnVMkkA/wD5T+OOdUSWV1zCVl0oUoaxYk2vEqjlESOu+H1lXJmWoMZtLXzxShNxrCCq4G1z8wfTm5OEGv4hzeufV532cBRBISVddv8AbE7x7csHK3NVZfUuN0qP6aQANYTO5EwNQ/HvuL1dZzPMolShxaS0yWVOhDnzaU6tPm/JpS9uSE8kbDm+2GfIQEIW48SXCJTo2kx96SnryH6YCHNa2tq2I8tA8yFSSCZCjySQTbtud5wxPuqhV+kmsVNmQKn5QfAbaMfSUk/xOtqvcD/bG19742rc5rmK/Sgo0FNrqB3ETAPtvhwZzFeX1aV6dSkp5AEbzzA59R8csJ7OsSBUkxGm2lNGmOpcZJQhOpSFagF6Dci/N79xa3JTJc8rqJ19wLKxWAoWCokgKABiRa3TDIxmLdWdb7SjFxpSj8pSO/8ABhIdQssP5ua+9pchuPLgtJjsCO4822UNoDaSoNoQNWlpJJ0kgk79y45LxB/ptSmnS2p1t5ZcXrCFHUo6jGomx1GRIvtiF5dPVVCG1IUGlAaoSjUR6RtMTcjfbA1kfp6mS4pNUnOPtNNqUlKHn3CFJQpQIDmhI3AOxxpxNxY6iBSshBUsJJWhCbagN0lRO/4/GBr1JR0z61ISuFEASEyASQJ9UT/L4a+WslUNdchz3XJinaa/+Cm90HT5QFAvWtYnYg+nvherOIcxRQuUSQyGqxseaf7hMGQQjsOY9+sSqdilHmtJJWr1JKwIBV1uY+J+YGGlnjKdNzJ40h1x9jXcaGiUJAN9rIWB3IvheyzM6jJW0pYShYTtrudxzIJwVay9nNKMPVVnDBOgW25THMnlt9MJWbkWkUGBU/kVvrXKZcDpeJNiQkXSS4s9h/2/1JeaXP6rM3aMVKEANLSBogSJURIASOtukfBfLqSkCHmClUIGgGEnpe5tP688IXLUKBArIb0vOJU6tRuEquS5fhS7W39v57POc1zwoytlKElKERI0/wBpB+6D03wOLaKSrFOykeWoydQEySOk/wCV77e2OVaVQVzqmgxJSXEtO+ZDUcC4Qo/m8YKt+nqO+0GWIzB9FM4t5qFrR6dbh5i0eWBH8jAwrWKx5ICbb+5BuIEW5SJ59sf/2Q==',
        NULL, NULL, NULL, NULL, '192.168.110.79', NULL, 54, NULL,
        'b2fe2a1217dd70078efbaaf4f969ed3d37a5ea2eb1577006922e541f9e00ee6ab8d2d590ea5b4dcddccfa04ad65985e1a9f565dd9f017170d6911e5991dba0f4',
        NULL, NULL, NULL, '2024-08-28 10:09:26', 'sa', 1);
INSERT INTO `sys_user`
VALUES (2, 'test', '测试', '2cc08c3aa427d1753bd23df1e4136527', 'fqweet9i62w9se', 0, 1, '', NULL, 2, NULL, 0, NULL, NULL,
        1, '', NULL, '172.16.2.226', '172.16.2.226', NULL, NULL, NULL, 'sa', NULL, '2024-08-20 17:06:18',
        '2024-08-21 08:41:20', 'sa', NULL);

-- ----------------------------
-- Table structure for sys_user_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_config`;
CREATE TABLE `sys_user_config`
(
    `sys_user_config_id`         int(11)                                                       NOT NULL,
    `sys_user_config_wx_openid`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信openid',
    `sys_user_config_wx_unionid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信unionid',
    `sys_user_config_gitee_id`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'giteeID',
    `create_name`                varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by`                  int(11)                                                       NULL DEFAULT NULL COMMENT '创建人',
    `create_time`                datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`                datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `update_name`                varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by`                  int(11)                                                       NULL DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`sys_user_config_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户信息 - 用户设置表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_config
-- ----------------------------

-- ----------------------------
-- Table structure for sys_user_layout
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_layout`;
CREATE TABLE `sys_user_layout`
(
    `sys_layout_id`        int(11)                                                       NOT NULL AUTO_INCREMENT,
    `sys_layout_layout`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '布局',
    `sys_layout_grid`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '布局网格',
    `sys_layout_component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件',
    `sys_layout_user_id`   int(11)                                                       NULL DEFAULT NULL COMMENT '所属用户',
    `create_name`          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by`            int(11)                                                       NULL DEFAULT NULL COMMENT '创建人',
    `create_time`          datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`          datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `update_name`          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by`            int(11)                                                       NULL DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`sys_layout_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 2
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户布局'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_layout
-- ----------------------------
INSERT INTO `sys_user_layout`
VALUES (1, '[{\"x\":0,\"y\":0,\"w\":9,\"h\":4,\"id\":4}]', '[]', '[{\"x\":0,\"y\":0,\"w\":9,\"h\":4,\"id\":4}]', 1,
        'sa', NULL, '2024-08-29 17:20:12', '2024-08-29 17:20:50', 'sa', 1);

-- ----------------------------
-- Table structure for sys_user_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_log`;
CREATE TABLE `sys_user_log`
(
    `sys_log_id`         int(11)                                                       NOT NULL AUTO_INCREMENT,
    `sys_log_username`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账号',
    `sys_log_address`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求地址',
    `sys_log_ip`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求IP',
    `sys_log_isp`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '运营商',
    `sys_log_system`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '系统',
    `sys_log_ua`         text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci         NULL COMMENT 'ua',
    `sys_log_browser`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '浏览器',
    `sys_log_url`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求地址',
    `sys_log_param`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求参数',
    `sys_log_cost`       int(11)                                                       NULL DEFAULT NULL COMMENT '耗时',
    `sys_log_from`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求模块',
    `sys_log_status`     int(11)                                                       NULL DEFAULT NULL COMMENT '请求状态; 0: 失败; 1: 成功',
    `sys_log_login_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '登录方式',
    `create_name`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by`          int(11)                                                       NULL DEFAULT NULL COMMENT '创建人',
    `create_time`        datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`        datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `update_name`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by`          int(11)                                                       NULL DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`sys_log_id`) USING BTREE,
    INDEX `index_sys_log_from` (`sys_log_from` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 57
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统日志表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_log
-- ----------------------------
INSERT INTO `sys_user_log`
VALUES (1, 'sa', NULL, '172.16.2.226', NULL, 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 Edg/123.0.0.0',
        'CHROME12', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        251, 'LOGIN', 1, 'WEB', NULL, 1, '2024-09-12 14:59:28', '2024-09-12 14:59:28', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (2, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 Edg/123.0.0.0',
        'CHROME12', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        353, 'LOGIN', 1, 'WEB', NULL, 1, '2024-09-13 16:37:39', '2024-09-13 16:37:39', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (3, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 Edg/123.0.0.0',
        'CHROME12', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        110, 'LOGIN', 1, 'WEB', NULL, 1, '2024-09-13 16:38:30', '2024-09-13 16:38:30', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (4, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 Edg/123.0.0.0',
        'CHROME12', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        102, 'LOGIN', 1, 'WEB', NULL, 1, '2024-09-13 16:41:11', '2024-09-13 16:41:11', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (5, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 Edg/123.0.0.0',
        'CHROME12', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        98, 'LOGIN', 1, 'WEB', NULL, 1, '2024-09-13 16:43:10', '2024-09-13 16:43:10', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (6, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 Edg/123.0.0.0',
        'CHROME12', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        637, 'LOGIN', 1, 'WEB', NULL, 1, '2024-09-26 15:10:34', '2024-09-26 15:10:34', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (7, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36',
        'CHROME12', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        521, 'LOGIN', 1, 'WEB', NULL, 1, '2024-10-24 15:03:58', '2024-10-24 15:03:58', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (8, 'sa', NULL, '172.16.2.226', NULL, 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36',
        'CHROME12', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        159, 'LOGIN', 1, 'WEB', NULL, 1, '2024-10-24 16:54:17', '2024-10-24 16:54:17', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (9, 'sa', NULL, '172.16.2.226', NULL, 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        1457, 'LOGIN', 1, 'WEB', NULL, 1, '2024-10-28 08:44:11', '2024-10-28 08:44:11', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (10, 'sa', NULL, '172.16.2.226', NULL, 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        171, 'LOGIN', 1, 'WEB', NULL, 1, '2024-11-26 09:01:01', '2024-11-26 09:01:01', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (11, 'sa', NULL, '172.16.2.226', NULL, 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        51, 'LOGIN', 1, 'WEB', NULL, 1, '2024-11-26 13:29:28', '2024-11-26 13:29:28', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (12, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        219, 'LOGIN', 1, 'WEB', NULL, 1, '2024-11-28 15:30:32', '2024-11-28 15:30:32', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (13, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        66, 'LOGIN', 1, 'WEB', NULL, 1, '2024-11-28 15:30:58', '2024-11-28 15:30:58', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (14, 'sa', '局域网', '172.16.2.226', '对方和您在同一内部网', 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        46, 'LOGIN', 1, 'WEB', NULL, 1, '2024-11-28 15:41:11', '2024-11-28 15:41:11', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (15, 'sa', NULL, '172.16.2.226', NULL, 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        189, 'LOGIN', 1, 'WEB', NULL, 1, '2024-11-29 09:29:42', '2024-11-29 09:29:42', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (16, 'sa', NULL, '172.16.2.226', NULL, 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        70, 'LOGIN', 1, 'WEB', NULL, 1, '2024-11-29 09:30:33', '2024-11-29 09:30:33', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (17, 'sa', NULL, '172.16.2.226', NULL, 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"48TWVM\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        69, 'LOGIN', 1, 'WEB', NULL, 1, '2024-11-29 09:31:22', '2024-11-29 09:31:22', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (18, 'sa', NULL, '172.16.2.226', NULL, 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"TTcEEP\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        263, 'LOGIN', 1, 'WEB', NULL, 1, '2024-12-08 11:05:54', '2024-12-08 11:05:54', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (19, 'sa', NULL, '172.16.2.226', NULL, 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"kp6vrd\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        247, 'LOGIN', 1, 'WEB', NULL, 1, '2024-12-08 12:44:18', '2024-12-08 12:44:18', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (20, 'sa', NULL, '172.16.2.226', NULL, 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/logout', '[]', 345, 'LOGIN', 1, '', NULL, 1, '2024-12-08 12:44:46',
        '2024-12-08 12:44:46', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (21, 'sa', NULL, '172.16.2.226', NULL, 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        82, 'LOGIN', 1, 'WEB', NULL, 1, '2024-12-08 12:45:28', '2024-12-08 12:45:28', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (22, 'sa', NULL, '172.16.2.226', NULL, 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/logout', '[]', 122, 'LOGIN', 1, '', NULL, 1, '2024-12-08 12:45:32',
        '2024-12-08 12:45:32', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (23, 'sa', NULL, '172.16.2.226', NULL, 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        74, 'LOGIN', 1, 'WEB', NULL, 1, '2024-12-08 12:46:09', '2024-12-08 12:46:09', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (24, 'sa', NULL, '172.16.2.226', NULL, 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        84, 'LOGIN', 1, 'WEB', NULL, 1, '2024-12-08 12:47:09', '2024-12-08 12:47:09', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (25, 'sa', NULL, '172.16.2.226', NULL, 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/logout', '[]', 144, 'LOGIN', 1, '', NULL, 1, '2024-12-08 12:47:16',
        '2024-12-08 12:47:16', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (26, 'sa', NULL, '172.16.2.226', NULL, 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        52, 'LOGIN', 1, 'WEB', NULL, 1, '2024-12-08 12:47:18', '2024-12-08 12:47:18', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (27, 'sa', NULL, '172.16.2.226', NULL, 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/logout', '[]', 125, 'LOGIN', 1, '', NULL, 1, '2024-12-08 12:47:21',
        '2024-12-08 12:47:21', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (28, 'sa', NULL, '192.168.110.79', NULL, 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        9541, 'LOGIN', 1, 'WEB', NULL, 1, '2024-12-08 13:39:19', '2024-12-08 13:39:19', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (29, 'sa', NULL, '192.168.110.79', NULL, 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        71, 'LOGIN', 1, 'WEB', NULL, 1, '2024-12-08 13:39:35', '2024-12-08 13:39:35', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (30, 'sa', NULL, '192.168.110.79', NULL, 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/logout', '[]', 108, 'LOGIN', 1, '', NULL, 1, '2024-12-08 13:39:57',
        '2024-12-08 13:39:57', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (31, 'sa', NULL, '192.168.110.79', NULL, 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        92, 'LOGIN', 1, 'WEB', NULL, 1, '2024-12-08 13:40:01', '2024-12-08 13:40:01', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (32, 'sa', NULL, '192.168.110.79', NULL, 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/logout', '[]', 103, 'LOGIN', 1, '', NULL, 1, '2024-12-08 13:40:04',
        '2024-12-08 13:40:04', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (33, 'sa', NULL, '192.168.110.79', NULL, 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        4853, 'LOGIN', 1, 'WEB', NULL, 1, '2024-12-08 13:40:16', '2024-12-08 13:40:16', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (34, 'sa', '局域网', '192.168.110.79', '对方和您在同一内部网', 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/logout', '[]', 180, 'LOGIN', 1, '', NULL, 1, '2024-12-08 13:47:57',
        '2024-12-08 13:47:57', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (35, 'sa', '局域网', '192.168.110.79', '对方和您在同一内部网', 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        15, 'LOGIN', 1, 'WEB', NULL, 1, '2024-12-08 13:48:01', '2024-12-08 13:48:01', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (36, 'sa', '局域网', '192.168.110.79', '对方和您在同一内部网', 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        1903, 'LOGIN', 1, 'WEB', NULL, 1, '2024-12-08 13:48:59', '2024-12-08 13:48:59', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (37, 'sa', '局域网', '192.168.110.79', '对方和您在同一内部网', 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        1, 'LOGIN', 1, 'WEB', NULL, 1, '2024-12-08 13:49:56', '2024-12-08 13:49:56', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (38, 'sa', '局域网', '192.168.110.79', '对方和您在同一内部网', 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        1, 'LOGIN', 1, 'WEB', NULL, 1, '2024-12-08 13:50:52', '2024-12-08 13:50:52', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (39, 'sa', '局域网', '192.168.110.79', '对方和您在同一内部网', 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        4269, 'LOGIN', 1, 'WEB', NULL, 1, '2024-12-08 13:56:21', '2024-12-08 13:56:21', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (40, 'sa', '局域网', '192.168.110.79', '对方和您在同一内部网', 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        3207, 'LOGIN', 1, 'WEB', NULL, 1, '2024-12-08 13:56:44', '2024-12-08 13:56:44', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (41, 'sa', NULL, '192.168.110.79', NULL, 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        0, 'LOGIN', 1, 'WEB', NULL, 1, '2024-12-08 13:58:33', '2024-12-08 13:58:33', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (42, 'sa', NULL, '192.168.110.79', NULL, 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        0, 'LOGIN', 1, 'WEB', NULL, 1, '2024-12-08 13:59:04', '2024-12-08 13:59:04', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (43, 'sa', NULL, '192.168.110.79', NULL, 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"111111\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        19, 'LOGIN', 1, 'WEB', NULL, 1, '2024-12-08 13:59:32', '2024-12-08 13:59:32', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (44, 'sa', NULL, '192.168.110.79', NULL, 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/logout', '[]', 0, 'LOGIN', 1, '', NULL, 1, '2024-12-08 13:59:32',
        '2024-12-08 13:59:32', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (45, 'sa', NULL, '192.168.110.79', NULL, 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"222222\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        42515, 'LOGIN', 1, 'WEB', NULL, 1, '2024-12-08 14:00:24', '2024-12-08 14:00:24', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (46, NULL, '局域网', '192.168.110.79', '对方和您在同一内部网', 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"111111\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        4195, 'LOGIN', 1, 'WEB', NULL, NULL, '2024-12-08 14:04:25', '2024-12-08 14:04:25', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (47, NULL, '局域网', '192.168.110.79', '对方和您在同一内部网', 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"222222\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        2, 'LOGIN', 1, 'WEB', NULL, NULL, '2024-12-08 14:04:30', '2024-12-08 14:04:30', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (48, NULL, '局域网', '192.168.110.79', '对方和您在同一内部网', 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"290800\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        1, 'LOGIN', 1, 'WEB', NULL, NULL, '2024-12-08 14:04:45', '2024-12-08 14:04:45', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (49, NULL, '局域网', '192.168.110.79', '对方和您在同一内部网', 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"290800\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        1, 'LOGIN', 1, 'WEB', NULL, NULL, '2024-12-08 14:05:00', '2024-12-08 14:05:00', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (50, 'sa', '局域网', '192.168.110.79', '对方和您在同一内部网', 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"198888\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        174, 'LOGIN', 1, 'WEB', NULL, 1, '2024-12-08 14:05:05', '2024-12-08 14:05:05', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (51, 'sa', '局域网', '192.168.110.79', '对方和您在同一内部网', 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/logout', '[]', 117, 'LOGIN', 1, '', NULL, 1, '2024-12-08 14:06:04',
        '2024-12-08 14:06:04', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (52, 'sa', '局域网', '192.168.110.79', '对方和您在同一内部网', 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"882493\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        1, 'LOGIN', 1, 'WEB', NULL, 1, '2024-12-08 14:07:28', '2024-12-08 14:07:28', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (53, 'sa', '局域网', '192.168.110.79', '对方和您在同一内部网', 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"2a5e0dbbf1c2c94c16c008f59cffb129\",\n    \"verifyCodeKey\": \"405495\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        59, 'LOGIN', 1, 'WEB', NULL, 1, '2024-12-08 14:07:41', '2024-12-08 14:07:41', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (54, 'sa', '局域网', '192.168.110.79', '对方和您在同一内部网', 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/logout', '[]', 141, 'LOGIN', 1, '', NULL, 1, '2024-12-08 14:08:15',
        '2024-12-08 14:08:15', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (55, 'sa', NULL, '192.168.110.79', NULL, 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/logout', '[]', 4001, 'LOGIN', 1, '', NULL, 1, '2025-01-24 09:20:06',
        '2025-01-24 09:20:06', NULL, NULL);
INSERT INTO `sys_user_log`
VALUES (56, 'sa', '', '192.168.110.79', '', 'Windows 10',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36',
        'CHROME13', '/system/api/v2/user/login',
        '[\n  {\n    \"username\": \"sa\",\n    \"password\": \"e6e061838856bf47e1de730719fb2609\",\n    \"verifyCodeKey\": \"233982\",\n    \"verifyCodeUlid\": \"\",\n    \"loginType\": \"SYSTEM\"\n  }\n]',
        2560, 'LOGIN', 1, 'WEB', NULL, 1, '2025-01-24 09:53:02', '2025-01-24 09:53:02', NULL, NULL);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`
(
    `sys_user_role_id` int(11) NOT NULL AUTO_INCREMENT,
    `sys_user_id`      int(11) NULL DEFAULT NULL COMMENT '用户ID',
    `sys_role_id`      int(11) NULL DEFAULT NULL COMMENT '角色ID',
    PRIMARY KEY (`sys_user_role_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 6
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户 - 角色关系表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role`
VALUES (1, 1, 1);

-- ----------------------------
-- Table structure for sys_user_third_party
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_third_party`;
CREATE TABLE `sys_user_third_party`
(
    `sys_user_third_party_id`    int(11)                                                       NOT NULL AUTO_INCREMENT,
    `sys_user_third_party_type`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '三方类型. gitee',
    `sys_user_third_party_name`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '三方信息名称',
    `sys_user_third_party_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '三方信息',
    `sys_user_id`                int(11)                                                       NULL DEFAULT NULL COMMENT '当前系统用户ID',
    `sys_user_third_party_uid`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '三方用户ID',
    `create_name`                varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by`                  int(11)                                                       NULL DEFAULT NULL COMMENT '创建人',
    `create_time`                datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`                datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `update_name`                varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by`                  int(11)                                                       NULL DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`sys_user_third_party_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户三方绑定表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_third_party
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
