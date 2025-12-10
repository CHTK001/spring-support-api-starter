/*
 Navicat Premium Dump SQL

 Source Server Type    : MySQL
 Source Server Version : 80027 (8.0.27)
 Target Server Type    : MySQL
 Target Server Version : 80027 (8.0.27)
 File Encoding         : 65001

 Date: 10/12/2024 13:30:00
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_file_system_group
-- ----------------------------
DROP TABLE IF EXISTS `sys_file_system_group`;
CREATE TABLE `sys_file_system_group`
(
    `sys_file_system_group_id`          int                                                           NOT NULL AUTO_INCREMENT COMMENT '分组ID',
    `sys_file_system_group_name`        varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT '分组名称',
    `sys_file_system_group_parent_id`   int                                                           NULL     DEFAULT 0 COMMENT '父分组ID',
    `sys_file_system_group_path`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT '分组路径',
    `sys_file_system_group_full_path`   varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT '分组完整路径',
    `sys_file_system_group_level`       int                                                           NULL     DEFAULT 1 COMMENT '分组层级',
    `sys_file_system_group_tree_path`   varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT '分组树路径',
    `sys_file_system_group_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT '分组描述',
    `sys_file_system_group_is_default`  tinyint(1)                                                    NULL     DEFAULT 0 COMMENT '是否默认分组',
    `sys_file_system_group_sort`        int                                                           NULL     DEFAULT 0 COMMENT '分组排序',
    `sys_file_system_group_status`      int                                                           NULL     DEFAULT 1 COMMENT '分组状态',
    `sys_file_system_group_icon`        varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT '分组图标',
    `sys_file_system_group_color`       varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL     DEFAULT NULL COMMENT '分组颜色',
    `create_name`                       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT '创建人姓名',
    `create_by`                         int                                                           NULL     DEFAULT NULL COMMENT '创建人',
    `create_time`                       datetime                                                      NULL     DEFAULT NULL COMMENT '创建时间',
    `update_time`                       datetime                                                      NULL     DEFAULT NULL COMMENT '更新时间',
    `update_name`                       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT '更新人姓名',
    `update_by`                         int                                                           NULL     DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`sys_file_system_group_id`) USING BTREE,
    INDEX `idx_parent_id` (`sys_file_system_group_parent_id`) USING BTREE,
    INDEX `idx_status` (`sys_file_system_group_status`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '文件系统分组表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_file_system_group
-- ----------------------------
INSERT INTO `sys_file_system_group` VALUES (1, '默认分组', 0, 'default', '/default', 1, 'default', NULL, 1, 1, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_file_system_group` VALUES (2, '图片', 0, 'images', '/images', 1, 'images', NULL, 0, 2, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_file_system_group` VALUES (3, '视频', 0, 'videos', '/videos', 1, 'videos', NULL, 0, 3, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_file_system_group` VALUES (4, '文档', 0, 'documents', '/documents', 1, 'documents', NULL, 0, 4, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_file_system_group` VALUES (5, '音频', 0, 'audios', '/audios', 1, 'audios', NULL, 0, 5, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_file_system_setting
-- ----------------------------
DROP TABLE IF EXISTS `sys_file_system_setting`;
CREATE TABLE `sys_file_system_setting`
(
    `sys_file_system_setting_id`                   int                                                           NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `sys_file_system_setting_name`                 varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT '配置名称',
    `sys_file_system_setting_chunk_enabled`        tinyint(1)                                                    NULL     DEFAULT 1 COMMENT '是否启用分片上传',
    `sys_file_system_setting_chunk_size_mb`        int                                                           NULL     DEFAULT 100 COMMENT '分片大小MB',
    `sys_file_system_setting_auto_merge_enabled`   tinyint(1)                                                    NULL     DEFAULT 1 COMMENT '是否开启自动合并',
    `sys_file_system_setting_merge_task_limit`     int                                                           NULL     DEFAULT 10 COMMENT '合并任务数量限制',
    `sys_file_system_setting_manual_merge_enabled` tinyint(1)                                                    NULL     DEFAULT 1 COMMENT '是否支持手动合并',
    `sys_file_system_setting_http_server_enabled`  tinyint(1)                                                    NULL     DEFAULT 0 COMMENT '是否开启HTTP服务',
    `sys_file_system_setting_http_server_port`     int                                                           NULL     DEFAULT 8888 COMMENT 'HTTP服务端口',
    `sys_file_system_setting_http_server_host`     varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT '0.0.0.0' COMMENT 'HTTP服务主机',
    `sys_file_system_setting_http_server_context`  varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT '/file' COMMENT 'HTTP上下文路径',
    `sys_file_system_setting_download_enabled`     tinyint(1)                                                    NULL     DEFAULT 1 COMMENT '是否启用下载',
    `sys_file_system_setting_preview_enabled`      tinyint(1)                                                    NULL     DEFAULT 1 COMMENT '是否启用预览',
    `sys_file_system_setting_http_access_domain`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT 'HTTP访问域名',
    `sys_file_system_setting_allowed_types`        varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL    DEFAULT NULL COMMENT '文件类型白名单',
    `sys_file_system_setting_max_file_size_mb`     int                                                           NULL     DEFAULT 1024 COMMENT '单文件最大MB',
    `sys_file_system_setting_storage_root_path`    varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT '存储根路径',
    `sys_file_system_setting_temp_path`            varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT '临时文件路径',
    `sys_file_system_setting_retention_days`       int                                                           NULL     DEFAULT 0 COMMENT '文件保留天数',
    `sys_file_system_setting_enabled`              tinyint(1)                                                    NULL     DEFAULT 1 COMMENT '是否启用',
    `sys_file_system_setting_default_storage_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL     DEFAULT 'local' COMMENT '默认存储类型',
    `sys_file_system_setting_remark`               varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT '备注',
    `create_name`                                  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT '创建人姓名',
    `create_by`                                    int                                                           NULL     DEFAULT NULL COMMENT '创建人',
    `create_time`                                  datetime                                                      NULL     DEFAULT NULL COMMENT '创建时间',
    `update_time`                                  datetime                                                      NULL     DEFAULT NULL COMMENT '更新时间',
    `update_name`                                  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT '更新人姓名',
    `update_by`                                    int                                                           NULL     DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`sys_file_system_setting_id`) USING BTREE,
    INDEX `idx_enabled` (`sys_file_system_setting_enabled`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '文件系统配置表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_file_system_setting
-- ----------------------------
INSERT INTO `sys_file_system_setting` VALUES (1, '默认配置', 1, 100, 1, 10, 1, 0, 8888, '0.0.0.0', '/file', 1, 1, NULL, NULL, 1024, NULL, NULL, 0, 1, 'local', NULL, NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_file_system
-- ----------------------------
DROP TABLE IF EXISTS `sys_file_system`;
CREATE TABLE `sys_file_system`
(
    `sys_file_system_id`                  int                                                           NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `sys_file_system_tid`                 varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL     DEFAULT NULL COMMENT '任务唯一标识',
    `sys_file_system_bucket`              varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT '存储桶',
    `sys_file_system_name`                varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT '文件名称',
    `sys_file_system_original_name`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT '原始名称',
    `sys_file_system_type`                varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL     DEFAULT NULL COMMENT '存储类型',
    `sys_file_system_extension`           varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL     DEFAULT NULL COMMENT '扩展名',
    `sys_file_system_path`                varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT '文件路径',
    `sys_file_system_size`                bigint                                                        NULL     DEFAULT NULL COMMENT '文件大小',
    `sys_file_system_md5`                 varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL     DEFAULT NULL COMMENT '文件MD5',
    `sys_file_system_chunk_total`         int                                                           NULL     DEFAULT NULL COMMENT '分片数量',
    `sys_file_system_chunk_uploaded`      int                                                           NULL     DEFAULT 0 COMMENT '已上传分片数',
    `sys_file_system_chunk_size`          bigint                                                        NULL     DEFAULT NULL COMMENT '分片大小',
    `sys_file_system_version`             int                                                           NULL     DEFAULT 0 COMMENT '版本号',
    `sys_file_system_status`              int                                                           NULL     DEFAULT 0 COMMENT '状态',
    `sys_file_system_http_enabled`        tinyint(1)                                                    NULL     DEFAULT 0 COMMENT '是否启用HTTP',
    `sys_file_system_http_url`            varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT 'HTTP访问URL',
    `sys_file_system_service_id`          int                                                           NULL     DEFAULT NULL COMMENT '绑定服务ID',
    `sys_file_system_group_id`            int                                                           NULL     DEFAULT NULL COMMENT '分组ID',
    `sys_file_system_upload_start_time`   datetime                                                      NULL     DEFAULT NULL COMMENT '上传开始时间',
    `sys_file_system_upload_complete_time` datetime                                                     NULL     DEFAULT NULL COMMENT '上传完成时间',
    `sys_file_system_local_path`          varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT '本地临时路径',
    `sys_file_system_summary`             varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT '描述',
    `create_name`                         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT '创建人姓名',
    `create_by`                           int                                                           NULL     DEFAULT NULL COMMENT '创建人',
    `create_time`                         datetime                                                      NULL     DEFAULT NULL COMMENT '创建时间',
    `update_time`                         datetime                                                      NULL     DEFAULT NULL COMMENT '更新时间',
    `update_name`                         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT '更新人姓名',
    `update_by`                           int                                                           NULL     DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`sys_file_system_id`) USING BTREE,
    UNIQUE INDEX `uk_tid` (`sys_file_system_tid`) USING BTREE,
    INDEX `idx_status` (`sys_file_system_status`) USING BTREE,
    INDEX `idx_group_id` (`sys_file_system_group_id`) USING BTREE,
    INDEX `idx_create_time` (`create_time`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '文件系统主表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_file_system
-- ----------------------------

-- ----------------------------
-- Table structure for sys_file_system_part
-- ----------------------------
DROP TABLE IF EXISTS `sys_file_system_part`;
CREATE TABLE `sys_file_system_part`
(
    `sys_file_system_part_id`            int                                                           NOT NULL AUTO_INCREMENT COMMENT '分片ID',
    `sys_file_system_part_file_id`       int                                                           NOT NULL COMMENT '文件ID',
    `sys_file_system_part_seq`           int                                                           NOT NULL COMMENT '分片序号',
    `sys_file_system_part_size`          bigint                                                        NULL     DEFAULT NULL COMMENT '分片大小',
    `sys_file_system_part_md5`           varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL     DEFAULT NULL COMMENT '分片MD5',
    `sys_file_system_part_local_path`    varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT '本地存储路径',
    `sys_file_system_part_version`       int                                                           NULL     DEFAULT 0 COMMENT '版本号',
    `sys_file_system_part_status`        int                                                           NULL     DEFAULT 0 COMMENT '状态',
    `sys_file_system_part_upload_time`   datetime                                                      NULL     DEFAULT NULL COMMENT '上传时间',
    `sys_file_system_part_process_time`  datetime                                                      NULL     DEFAULT NULL COMMENT '处理时间',
    `sys_file_system_part_error_message` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL    DEFAULT NULL COMMENT '错误信息',
    `sys_file_system_part_retry_count`   int                                                           NULL     DEFAULT 0 COMMENT '重试次数',
    `create_name`                        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT '创建人姓名',
    `create_by`                          int                                                           NULL     DEFAULT NULL COMMENT '创建人',
    `create_time`                        datetime                                                      NULL     DEFAULT NULL COMMENT '创建时间',
    `update_time`                        datetime                                                      NULL     DEFAULT NULL COMMENT '更新时间',
    `update_name`                        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT '更新人姓名',
    `update_by`                          int                                                           NULL     DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`sys_file_system_part_id`) USING BTREE,
    INDEX `idx_file_id` (`sys_file_system_part_file_id`) USING BTREE,
    INDEX `idx_status` (`sys_file_system_part_status`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '文件系统分片表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_file_system_part
-- ----------------------------

-- ----------------------------
-- Drop old SysTaskSso tables
-- ----------------------------
DROP TABLE IF EXISTS `sys_task_sso_item`;
DROP TABLE IF EXISTS `sys_task_sso`;

SET FOREIGN_KEY_CHECKS = 1;
