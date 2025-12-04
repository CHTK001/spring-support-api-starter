/*
 Navicat Premium Dump SQL

 Source Server         : 172.16.0.40
 Source Server Type    : MySQL
 Source Server Version : 80027 (8.0.27)
 Source Host           : 172.16.0.40:3306
 Source Schema         : video

 Target Server Type    : MySQL
 Target Server Version : 80027 (8.0.27)
 File Encoding         : 65001

 Date: 15/09/2025 09:20:10
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for video_download
-- ----------------------------
DROP TABLE IF EXISTS `video_download`;
CREATE TABLE `video_download`
(
    `video_download_id`         varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci   NOT NULL COMMENT '下载ID',
    `video_id`                  int                                                            NOT NULL COMMENT '视频ID',
    `video_download_url`        varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '下载地址',
    `video_download_name`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '下载名称',
    `video_download_type`       varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci   NULL DEFAULT NULL COMMENT '下载类型',
    `video_download_quality`    varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci   NULL DEFAULT NULL COMMENT '视频质量(标清/高清/超清)',
    `video_download_platform`   varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '下载平台',
    `video_download_size`       varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci   NULL DEFAULT NULL COMMENT '文件大小',
    `video_download_magnetic`   text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci          NULL COMMENT '磁力链接',
    `video_download_status`     tinyint                                                        NULL DEFAULT 1 COMMENT '下载状态(1:可用,0:不可用)',
    `video_download_count`      int                                                            NULL DEFAULT 0 COMMENT '下载次数',
    `video_download_share_time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci   NULL DEFAULT NULL COMMENT '分享时间',
    `create_name`               varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by`                 int                                                            NULL DEFAULT NULL COMMENT '创建人',
    `create_time`               datetime                                                       NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`               datetime                                                       NULL DEFAULT NULL COMMENT '更新时间',
    `update_name`               varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by`                 int                                                            NULL DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`video_download_id`) USING BTREE,
    INDEX `idx_video_id` (`video_id` ASC) USING BTREE,
    INDEX `idx_download_status` (`video_download_status` ASC) USING BTREE,
    INDEX `idx_download_quality` (`video_download_quality` ASC) USING BTREE,
    CONSTRAINT `video_download_ibfk_1` FOREIGN KEY (`video_id`) REFERENCES `video_info` (`video_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '视频下载链接表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of video_download
-- ----------------------------

-- ----------------------------
-- Table structure for video_info
-- ----------------------------
DROP TABLE IF EXISTS `video_info`;
CREATE TABLE `video_info`
(
    `video_id`           int                                                            NOT NULL AUTO_INCREMENT COMMENT '视频唯一标识ID',
    `video_title`        varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '视频标题',
    `video_name`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '视频名称',
    `video_alias_name`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '视频别名',
    `video_score`        decimal(3, 1)                                                  NULL DEFAULT NULL COMMENT '视频评分',
    `video_year`         int                                                            NULL DEFAULT NULL COMMENT '视频年份',
    `video_platform`     varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '视频所属平台',
    `video_language`     varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci   NULL DEFAULT NULL COMMENT '视频语言',
    `video_quality`      varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci   NULL DEFAULT NULL COMMENT '视频清晰度',
    `video_thumbnail`    varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '视频缩略图URL',
    `video_cover`        varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '视频封面URL',
    `video_url`          varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '视频播放地址',
    `video_views`        decimal(15, 0)                                                 NULL DEFAULT NULL COMMENT '视频观看次数',
    `video_likes`        int                                                            NULL DEFAULT NULL COMMENT '视频点赞数',
    `video_status`       int                                                            NULL DEFAULT 0 COMMENT '视频状态是否禁用;0:启用',
    `video_duration`     int                                                            NULL DEFAULT NULL COMMENT '视频时长(分)',
    `video_version`      int                                                            NULL DEFAULT 1 COMMENT '视频版本',
    `video_dou_ban_id`   varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci   NULL DEFAULT NULL COMMENT '豆瓣视频ID',
    `video_publish_date` datetime                                                       NULL DEFAULT NULL COMMENT '视频发布日期',
    `video_category`     varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci   NULL DEFAULT NULL COMMENT '视频种类;MV;TV',
    `video_type`         varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '视频类型',
    `video_release`      varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci   NULL DEFAULT NULL COMMENT '视频上映日期',
    `video_district`     varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '视频地区',
    `video_size`         varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci   NULL DEFAULT NULL COMMENT '视频文件大小',
    `video_author`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '视频作者',
    `video_director`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '视频导演',
    `video_writer`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '视频编剧',
    `video_actor`        text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci          NULL COMMENT '视频主演',
    `video_description`  text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci          NULL COMMENT '视频描述内容',
    `create_name`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by`          int                                                            NULL DEFAULT NULL COMMENT '创建人',
    `create_time`        datetime                                                       NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`        datetime                                                       NULL DEFAULT NULL COMMENT '更新时间',
    `update_name`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by`          int                                                            NULL DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`video_id`) USING BTREE,
    INDEX `idx_video_title` (`video_title` ASC) USING BTREE,
    INDEX `idx_video_year` (`video_year` ASC) USING BTREE,
    INDEX `idx_video_category` (`video_category` ASC) USING BTREE,
    INDEX `idx_video_status` (`video_status` ASC) USING BTREE,
    INDEX `idx_create_time` (`create_time` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '视频信息表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of video_info
-- ----------------------------

-- ----------------------------
-- Table structure for video_keyword
-- ----------------------------
DROP TABLE IF EXISTS `video_keyword`;
CREATE TABLE `video_keyword`
(
    `video_keyword_id`      varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '关键词ID',
    `video_keyword_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关键词内容',
    `video_keyword_hot`     int                                                           NULL DEFAULT 0 COMMENT '关键词热度',
    `video_keyword_sort`    int                                                           NULL DEFAULT 0 COMMENT '关键词排序',
    `video_keyword_status`  int                                                           NULL DEFAULT 1 COMMENT '关键词状态：0-禁用，1-启用',
    `create_name`           varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by`             int                                                           NULL DEFAULT NULL COMMENT '创建人',
    `create_time`           datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`           datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `update_name`           varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by`             int                                                           NULL DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`video_keyword_id`) USING BTREE,
    UNIQUE INDEX `uk_keyword_content` (`video_keyword_content` ASC) USING BTREE,
    INDEX `idx_keyword_hot` (`video_keyword_hot` ASC) USING BTREE,
    INDEX `idx_keyword_sort` (`video_keyword_sort` ASC) USING BTREE,
    INDEX `idx_keyword_status` (`video_keyword_status` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '视频热门搜索关键词表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of video_keyword
-- ----------------------------
INSERT INTO `video_keyword`
VALUES ('1', '热门电影', 100, 1, 1, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `video_keyword`
VALUES ('2', '最新电视剧', 95, 2, 1, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `video_keyword`
VALUES ('3', '经典动漫', 90, 3, 1, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `video_keyword`
VALUES ('4', '纪录片', 85, 4, 1, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `video_keyword`
VALUES ('5', '综艺节目', 80, 5, 1, NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for video_mark
-- ----------------------------
DROP TABLE IF EXISTS `video_mark`;
CREATE TABLE `video_mark`
(
    `video_mark_id`     int                                                           NOT NULL AUTO_INCREMENT COMMENT '评分ID',
    `video_id`          int                                                           NOT NULL COMMENT '视频ID',
    `video_mark_type`   varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '评分类型',
    `video_mark_score`  decimal(3, 1)                                                 NULL DEFAULT NULL COMMENT '评分',
    `video_mark_people` int                                                           NULL DEFAULT 0 COMMENT '评分人数',
    `create_name`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by`         int                                                           NULL DEFAULT NULL COMMENT '创建人',
    `create_time`       datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`       datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `update_name`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by`         int                                                           NULL DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`video_mark_id`) USING BTREE,
    INDEX `idx_video_id` (`video_id` ASC) USING BTREE,
    INDEX `idx_mark_type` (`video_mark_type` ASC) USING BTREE,
    INDEX `idx_mark_score` (`video_mark_score` ASC) USING BTREE,
    CONSTRAINT `video_mark_ibfk_1` FOREIGN KEY (`video_id`) REFERENCES `video_info` (`video_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '视频评分表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of video_mark
-- ----------------------------

-- ----------------------------
-- Table structure for video_sync_config
-- ----------------------------
DROP TABLE IF EXISTS `video_sync_config`;
CREATE TABLE `video_sync_config`
(
    `video_sync_config_id`      varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '配置ID',
    `video_sync_config_name`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '同步名称',
    `video_sync_config_type`    varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '同步方式',
    `video_sync_config_url`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '同步地址',
    `video_sync_config_key`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '同步密钥',
    `video_sync_config_cookie`  text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci         NULL COMMENT '同步Cookie',
    `video_sync_config_extra`   text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci         NULL COMMENT '额外参数(JSON格式)',
    `video_sync_config_enabled` tinyint(1)                                                    NULL DEFAULT 1 COMMENT '是否启用',
    `video_sync_config_source`  varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '同步来源(用于SPI实现类型)',
    `create_name`               varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by`                 int                                                           NULL DEFAULT NULL COMMENT '创建人',
    `create_time`               datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_time`               datetime                                                      NULL DEFAULT NULL COMMENT '更新时间',
    `update_name`               varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by`                 int                                                           NULL DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`video_sync_config_id`) USING BTREE,
    INDEX `idx_config_name` (`video_sync_config_name` ASC) USING BTREE,
    INDEX `idx_config_type` (`video_sync_config_type` ASC) USING BTREE,
    INDEX `idx_config_enabled` (`video_sync_config_enabled` ASC) USING BTREE,
    INDEX `idx_config_source` (`video_sync_config_source` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '视频同步配置表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of video_sync_config
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
