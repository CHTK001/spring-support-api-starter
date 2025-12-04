/*
 * 系统节假日增量脚本
 *
 * @author CH
 * @since 2024/12/04
 * @version 1.1
 */

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_holiday
-- ----------------------------
DROP TABLE IF EXISTS `sys_holiday`;
CREATE TABLE `sys_holiday`
(
    `sys_holiday_id`         int                                                           NOT NULL AUTO_INCREMENT COMMENT '节假日ID',
    `sys_holiday_year`       int                                                           NULL     DEFAULT NULL COMMENT '节假日年份',
    `sys_holiday_date`       date                                                          NULL     DEFAULT NULL COMMENT '节假日日期',
    `sys_holiday_name`       varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT '节假日名称',
    `sys_holiday_is_holiday` tinyint(1)                                                    NULL     DEFAULT 1 COMMENT '是否节假日: 1-是, 0-否(调休工作日)',
    `create_by`              int                                                           NULL     DEFAULT NULL COMMENT '创建人',
    `create_name`            varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT '创建人姓名',
    `create_time`            datetime                                                      NULL     DEFAULT NULL COMMENT '创建时间',
    `update_by`              int                                                           NULL     DEFAULT NULL COMMENT '更新人',
    `update_name`            varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT '更新人姓名',
    `update_time`            datetime                                                      NULL     DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`sys_holiday_id`) USING BTREE,
    INDEX `idx_year` (`sys_holiday_year`) USING BTREE COMMENT '年份索引',
    INDEX `idx_date` (`sys_holiday_date`) USING BTREE COMMENT '日期索引',
    UNIQUE INDEX `uk_date` (`sys_holiday_date`) USING BTREE COMMENT '日期唯一索引'
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '节假日表'
  ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
