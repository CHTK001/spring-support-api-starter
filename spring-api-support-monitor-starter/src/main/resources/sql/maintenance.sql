-- ----------------------------
-- Table structure for maintenance_group
-- ----------------------------
DROP TABLE IF EXISTS `maintenance_group`;
CREATE TABLE `maintenance_group`
(
    `maintenance_group_id`     int(11)                                                       NOT NULL AUTO_INCREMENT COMMENT '维护组ID',
    `maintenance_group_name`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '维护组名称',
    `maintenance_group_desc`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '维护组描述',
    `maintenance_group_status` int(11)                                                       NULL DEFAULT 1 COMMENT '状态,0:停用,1:启用',
    `create_time`              datetime                                                      NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`              datetime                                                      NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`                varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '创建者',
    `update_by`                varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`maintenance_group_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '维护组表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for maintenance_host
-- ----------------------------
DROP TABLE IF EXISTS `maintenance_host`;
CREATE TABLE `maintenance_host`
(
    `maintenance_host_id`       int(11)                                                       NOT NULL AUTO_INCREMENT COMMENT '维护主机ID',
    `maintenance_group_id`      int(11)                                                       NULL DEFAULT NULL COMMENT '维护组ID',
    `maintenance_host_address`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '主机地址',
    `maintenance_host_username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账号',
    `maintenance_host_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
    `maintenance_host_port`     int(11)                                                       NULL DEFAULT 22 COMMENT '端口',
    `maintenance_host_status`   int(11)                                                       NULL DEFAULT 1 COMMENT '状态,0:停用,1:启用',
    `create_time`               datetime                                                      NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`               datetime                                                      NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`                 varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '创建者',
    `update_by`                 varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`maintenance_host_id`) USING BTREE,
    INDEX `idx_group_id` (`maintenance_group_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '维护主机表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for maintenance_script
-- ----------------------------
DROP TABLE IF EXISTS `maintenance_script`;
CREATE TABLE `maintenance_script`
(
    `maintenance_script_id`      int(11)                                                       NOT NULL AUTO_INCREMENT COMMENT '脚本ID',
    `maintenance_group_id`       int(11)                                                       NULL DEFAULT NULL COMMENT '维护组ID',
    `maintenance_script_name`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本名称',
    `maintenance_script_path`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本路径',
    `maintenance_script_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci         NULL COMMENT '脚本内容',
    `maintenance_script_desc`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '脚本描述',
    `maintenance_script_status`  int(11)                                                       NULL DEFAULT 1 COMMENT '状态,0:停用,1:启用',
    `create_time`                datetime                                                      NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`                datetime                                                      NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`                  varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '创建者',
    `update_by`                  varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`maintenance_script_id`) USING BTREE,
    INDEX `idx_group_id` (`maintenance_group_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '维护脚本表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for maintenance_file
-- ----------------------------
DROP TABLE IF EXISTS `maintenance_file`;
CREATE TABLE `maintenance_file`
(
    `maintenance_file_id`          int(11)                                                       NOT NULL AUTO_INCREMENT COMMENT '文件ID',
    `maintenance_group_id`         int(11)                                                       NULL DEFAULT NULL COMMENT '维护组ID',
    `maintenance_file_name`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件名称',
    `maintenance_file_path`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件路径',
    `maintenance_file_type`        varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '文件类型',
    `maintenance_file_size`        bigint(20)                                                    NULL DEFAULT NULL COMMENT '文件大小',
    `maintenance_file_is_extract`  int(11)                                                       NULL DEFAULT 0 COMMENT '是否解压,0:否,1:是',
    `maintenance_file_is_override` int(11)                                                       NULL DEFAULT 0 COMMENT '是否覆盖,0:否,1:是',
    `maintenance_file_status`      int(11)                                                       NULL DEFAULT 1 COMMENT '状态,0:停用,1:启用',
    `create_time`                  datetime                                                      NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`                  datetime                                                      NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`                    varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '创建者',
    `update_by`                    varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`maintenance_file_id`) USING BTREE,
    INDEX `idx_group_id` (`maintenance_group_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '维护文件表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for maintenance_task
-- ----------------------------
DROP TABLE IF EXISTS `maintenance_task`;
CREATE TABLE `maintenance_task`
(
    `maintenance_task_id`     int(11)                                                      NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `maintenance_group_id`    int(11)                                                      NULL DEFAULT NULL COMMENT '维护组ID',
    `maintenance_host_id`     int(11)                                                      NULL DEFAULT NULL COMMENT '维护主机ID',
    `maintenance_script_id`   int(11)                                                      NULL DEFAULT NULL COMMENT '脚本ID',
    `maintenance_file_id`     int(11)                                                      NULL DEFAULT NULL COMMENT '文件ID',
    `maintenance_task_type`   varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务类型,SCRIPT:脚本,FILE:文件',
    `maintenance_task_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务状态,PENDING:待执行,RUNNING:执行中,SUCCESS:成功,FAILED:失败',
    `maintenance_task_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci        NULL COMMENT '任务结果',
    `maintenance_task_error`  text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci        NULL COMMENT '错误信息',
    `create_time`             datetime                                                     NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`             datetime                                                     NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`               varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建者',
    `update_by`               varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`maintenance_task_id`) USING BTREE,
    INDEX `idx_group_id` (`maintenance_group_id`) USING BTREE,
    INDEX `idx_host_id` (`maintenance_host_id`) USING BTREE,
    INDEX `idx_script_id` (`maintenance_script_id`) USING BTREE,
    INDEX `idx_file_id` (`maintenance_file_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '维护任务表'
  ROW_FORMAT = Dynamic;