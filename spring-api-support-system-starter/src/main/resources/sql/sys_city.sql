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

 Date: 24/01/2025 10:04:39
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_city
-- ----------------------------
DROP TABLE IF EXISTS `sys_city`;
CREATE TABLE `sys_city`
(
    `sys_city_code`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    `sys_city_name`         text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci         NULL COMMENT '区划名称',
    `sys_city_level`        int(11)                                                       NULL DEFAULT NULL COMMENT '区划级别，1省，2地级市（），3县级市（区），4乡镇（街道），5村（社区、居委会）',
    `sys_city_parent_code`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    `sys_city_tree_path`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '路径',
    `sys_city_short_name`   varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '简称',
    `sys_city_merger_name`  text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci         NULL,
    `sys_city_pinyin`       text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci         NULL,
    `sys_city_lng`          double                                                        NULL DEFAULT NULL COMMENT '经度',
    `sys_city_lat`          double                                                        NULL DEFAULT NULL COMMENT '纬度',
    `sys_city_sort`         int(11)                                                       NULL DEFAULT NULL COMMENT '排序',
    `sys_city_area_code`    varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '地区区号',
    `sys_city_car_plate`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '车牌',
    `sys_city_car_plate_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '车牌号',
    PRIMARY KEY (`sys_city_code`) USING BTREE,
    UNIQUE INDEX `city_code_uindex` (`sys_city_code` ASC) USING BTREE,
    INDEX `city_pcode_index` (`sys_city_parent_code` ASC) USING BTREE,
    INDEX `city_level_index` (`sys_city_level` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
