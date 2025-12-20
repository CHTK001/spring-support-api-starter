/*
 * 视频管理模块 - 数据库初始化脚本
 * 
 * 包含表:
 *   - video_info: 视频信息表
 *   - video_download: 视频下载记录表
 *   - video_keyword: 视频关键词表
 *   - video_mark: 视频标记表
 *   - video_sync_config: 视频同步配置表
 *
 * 用于视频内容管理、下载追踪和同步配置
 *
 * @author System
 * @since 2025-01-01
 * @version 1.0
 */

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 1. 视频信息表 (video_info)
-- 用途: 存储视频基本信息和元数据
-- =====================================================
DROP TABLE IF EXISTS `video_info`;
CREATE TABLE `video_info` (
    `video_info_id` bigint NOT NULL AUTO_INCREMENT COMMENT '视频ID - 主键自增',
    `video_info_title` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '视频标题 - 视频的显示标题',
    `video_info_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '视频描述 - 视频的详细介绍说明',
    `video_info_cover` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图片 - 视频封面图URL地址',
    `video_info_url` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '视频URL - 视频播放地址或源地址',
    `video_info_local_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '本地路径 - 视频在本地存储的文件路径',
    `video_info_source` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '视频来源 - 来源平台标识(YOUTUBE/BILIBILI/TIKTOK/LOCAL等)',
    `video_info_source_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源ID - 视频在来源平台的原始ID',
    `video_info_source_url` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源URL - 视频在来源平台的原始页面地址',
    `video_info_author` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '作者/UP主 - 视频创作者名称',
    `video_info_author_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '作者ID - 创作者在来源平台的ID',
    `video_info_duration` int NULL DEFAULT 0 COMMENT '视频时长 - 视频时长(秒)',
    `video_info_duration_text` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '时长文本 - 格式化的时长显示(如"10:30")',
    `video_info_width` int NULL DEFAULT 0 COMMENT '视频宽度 - 视频分辨率宽度(像素)',
    `video_info_height` int NULL DEFAULT 0 COMMENT '视频高度 - 视频分辨率高度(像素)',
    `video_info_resolution` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分辨率标识 - 如"1080P"、"4K"等',
    `video_info_file_size` bigint NULL DEFAULT 0 COMMENT '文件大小 - 视频文件大小(字节)',
    `video_info_format` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '视频格式 - 文件格式(MP4/AVI/MKV等)',
    `video_info_codec` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '视频编码 - 视频编码格式(H.264/H.265/VP9等)',
    `video_info_bitrate` int NULL DEFAULT 0 COMMENT '视频码率 - 视频码率(kbps)',
    `video_info_fps` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '帧率 - 视频帧率(FPS)',
    `video_info_view_count` bigint NULL DEFAULT 0 COMMENT '播放量 - 视频播放次数',
    `video_info_like_count` bigint NULL DEFAULT 0 COMMENT '点赞数 - 视频获得的点赞数量',
    `video_info_comment_count` bigint NULL DEFAULT 0 COMMENT '评论数 - 视频评论数量',
    `video_info_share_count` bigint NULL DEFAULT 0 COMMENT '分享数 - 视频被分享的次数',
    `video_info_publish_time` datetime NULL DEFAULT NULL COMMENT '发布时间 - 视频在来源平台的发布时间',
    `video_info_category` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类 - 视频所属分类',
    `video_info_tags` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签 - 视频标签，逗号分隔',
    `video_info_language` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '语言 - 视频语言(zh/en/ja等)',
    `video_info_subtitle` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字幕文件 - 字幕文件URL或路径，多个用逗号分隔',
    `video_info_is_downloaded` int NULL DEFAULT 0 COMMENT '是否已下载 - 0:未下载, 1:已下载',
    `video_info_download_time` datetime NULL DEFAULT NULL COMMENT '下载时间 - 视频下载完成的时间',
    `video_info_is_favorite` int NULL DEFAULT 0 COMMENT '是否收藏 - 0:未收藏, 1:已收藏',
    `video_info_watch_progress` int NULL DEFAULT 0 COMMENT '观看进度 - 已观看的时长(秒)',
    `video_info_watch_count` int NULL DEFAULT 0 COMMENT '本地播放次数 - 在本系统中的播放次数',
    `video_info_last_watch_time` datetime NULL DEFAULT NULL COMMENT '最后观看时间 - 最近一次播放的时间',
    `video_info_status` int NULL DEFAULT 1 COMMENT '视频状态 - 0:已删除, 1:正常, 2:处理中, 3:下载中, 4:下载失败',
    `video_info_extra` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '扩展信息 - 其他扩展数据(JSON格式)',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`video_info_id`) USING BTREE,
    UNIQUE INDEX `uk_source_video` (`video_info_source`, `video_info_source_id`) USING BTREE COMMENT '来源视频唯一索引 - 防止同一来源的视频重复录入',
    INDEX `idx_source` (`video_info_source`) USING BTREE COMMENT '来源平台索引',
    INDEX `idx_author` (`video_info_author`) USING BTREE COMMENT '作者索引',
    INDEX `idx_status` (`video_info_status`) USING BTREE COMMENT '状态索引',
    INDEX `idx_publish_time` (`video_info_publish_time`) USING BTREE COMMENT '发布时间索引',
    INDEX `idx_is_downloaded` (`video_info_is_downloaded`) USING BTREE COMMENT '下载状态索引',
    INDEX `idx_is_favorite` (`video_info_is_favorite`) USING BTREE COMMENT '收藏状态索引',
    FULLTEXT INDEX `ft_title_desc` (`video_info_title`, `video_info_description`) COMMENT '标题描述全文索引 - 用于内容搜索'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = '视频信息表 - 存储视频基本信息和元数据' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 2. 视频下载记录表 (video_download)
-- 用途: 记录视频下载任务和进度
-- =====================================================
DROP TABLE IF EXISTS `video_download`;
CREATE TABLE `video_download` (
    `video_download_id` bigint NOT NULL AUTO_INCREMENT COMMENT '下载记录ID - 主键自增',
    `video_info_id` bigint NULL DEFAULT NULL COMMENT '视频ID - 关联video_info表',
    `video_download_url` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '下载URL - 视频下载地址',
    `video_download_save_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '保存路径 - 本地保存路径',
    `video_download_file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件名 - 保存的文件名',
    `video_download_total_size` bigint NULL DEFAULT 0 COMMENT '总大小 - 文件总大小(字节)',
    `video_download_downloaded_size` bigint NULL DEFAULT 0 COMMENT '已下载大小 - 已下载的大小(字节)',
    `video_download_progress` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '下载进度 - 下载完成百分比(0-100)',
    `video_download_speed` bigint NULL DEFAULT 0 COMMENT '下载速度 - 当前下载速度(字节/秒)',
    `video_download_avg_speed` bigint NULL DEFAULT 0 COMMENT '平均速度 - 平均下载速度(字节/秒)',
    `video_download_status` int NULL DEFAULT 0 COMMENT '下载状态 - 0:等待中, 1:下载中, 2:已完成, 3:已暂停, 4:下载失败, 5:已取消',
    `video_download_error_msg` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误信息 - 下载失败时的错误消息',
    `video_download_retry_count` int NULL DEFAULT 0 COMMENT '重试次数 - 下载失败后的重试次数',
    `video_download_max_retry` int NULL DEFAULT 3 COMMENT '最大重试次数 - 允许的最大重试次数',
    `video_download_priority` int NULL DEFAULT 5 COMMENT '下载优先级 - 1-10，数字越小优先级越高',
    `video_download_start_time` datetime NULL DEFAULT NULL COMMENT '开始时间 - 下载开始的时间',
    `video_download_end_time` datetime NULL DEFAULT NULL COMMENT '完成时间 - 下载完成的时间',
    `video_download_elapsed_time` int NULL DEFAULT 0 COMMENT '耗时 - 下载耗时(秒)',
    `video_download_quality` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '下载质量 - 选择的画质(如"1080P"、"720P")',
    `video_download_format` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'MP4' COMMENT '下载格式 - 下载的文件格式',
    `video_download_with_audio` int NULL DEFAULT 1 COMMENT '包含音频 - 0:仅视频, 1:包含音频',
    `video_download_with_subtitle` int NULL DEFAULT 0 COMMENT '包含字幕 - 0:不含字幕, 1:含字幕',
    `video_download_proxy` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '使用代理 - 下载使用的代理地址',
    `video_download_extra` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '扩展信息 - 其他下载相关信息(JSON格式)',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`video_download_id`) USING BTREE,
    INDEX `idx_video_id` (`video_info_id`) USING BTREE COMMENT '视频ID索引',
    INDEX `idx_status` (`video_download_status`) USING BTREE COMMENT '下载状态索引',
    INDEX `idx_priority` (`video_download_priority`, `video_download_status`) USING BTREE COMMENT '优先级状态复合索引 - 用于下载队列调度',
    INDEX `idx_create_time` (`create_time`) USING BTREE COMMENT '创建时间索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = '视频下载记录表 - 记录视频下载任务和进度' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 3. 视频关键词表 (video_keyword)
-- 用途: 存储用于视频搜索和订阅的关键词
-- =====================================================
DROP TABLE IF EXISTS `video_keyword`;
CREATE TABLE `video_keyword` (
    `video_keyword_id` int NOT NULL AUTO_INCREMENT COMMENT '关键词ID - 主键自增',
    `video_keyword_text` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '关键词 - 搜索关键词文本',
    `video_keyword_source` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '适用来源 - 适用的视频平台(YOUTUBE/BILIBILI等)，NULL表示所有平台',
    `video_keyword_category` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关键词分类 - 关键词的业务分类',
    `video_keyword_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关键词描述 - 关键词用途说明',
    `video_keyword_priority` int NULL DEFAULT 5 COMMENT '优先级 - 搜索优先级(1-10)，数字越小优先级越高',
    `video_keyword_auto_download` int NULL DEFAULT 0 COMMENT '自动下载 - 0:不自动下载, 1:搜索到后自动下载',
    `video_keyword_download_quality` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '1080P' COMMENT '下载画质 - 自动下载时的画质设置',
    `video_keyword_max_results` int NULL DEFAULT 50 COMMENT '最大结果数 - 每次搜索返回的最大视频数量',
    `video_keyword_min_duration` int NULL DEFAULT 0 COMMENT '最小时长 - 过滤视频的最小时长(秒)',
    `video_keyword_max_duration` int NULL DEFAULT 0 COMMENT '最大时长 - 过滤视频的最大时长(秒)，0表示不限',
    `video_keyword_min_views` bigint NULL DEFAULT 0 COMMENT '最小播放量 - 过滤视频的最小播放量',
    `video_keyword_filter_config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '过滤配置 - 其他过滤条件(JSON格式)',
    `video_keyword_last_search` datetime NULL DEFAULT NULL COMMENT '最后搜索时间 - 最近一次使用该关键词搜索的时间',
    `video_keyword_search_count` int NULL DEFAULT 0 COMMENT '搜索次数 - 该关键词被使用的搜索次数',
    `video_keyword_hit_count` int NULL DEFAULT 0 COMMENT '命中数量 - 该关键词搜索到的视频总数',
    `video_keyword_status` int NULL DEFAULT 1 COMMENT '关键词状态 - 0:禁用, 1:启用',
    `video_keyword_is_subscription` int NULL DEFAULT 0 COMMENT '是否订阅 - 0:普通关键词, 1:订阅关键词(定期自动搜索)',
    `video_keyword_subscription_interval` int NULL DEFAULT 24 COMMENT '订阅间隔 - 订阅关键词的搜索间隔(小时)',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`video_keyword_id`) USING BTREE,
    UNIQUE INDEX `uk_keyword_source` (`video_keyword_text`, `video_keyword_source`) USING BTREE COMMENT '关键词来源唯一索引',
    INDEX `idx_source` (`video_keyword_source`) USING BTREE COMMENT '来源平台索引',
    INDEX `idx_status` (`video_keyword_status`) USING BTREE COMMENT '状态索引',
    INDEX `idx_is_subscription` (`video_keyword_is_subscription`, `video_keyword_status`) USING BTREE COMMENT '订阅状态复合索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = '视频关键词表 - 存储视频搜索和订阅的关键词' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 4. 视频标记表 (video_mark)
-- 用途: 存储视频的时间标记和书签
-- =====================================================
DROP TABLE IF EXISTS `video_mark`;
CREATE TABLE `video_mark` (
    `video_mark_id` bigint NOT NULL AUTO_INCREMENT COMMENT '标记ID - 主键自增',
    `video_info_id` bigint NOT NULL COMMENT '视频ID - 关联video_info表',
    `video_mark_time` int NOT NULL COMMENT '标记时间点 - 视频中的时间位置(秒)',
    `video_mark_time_text` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '时间文本 - 格式化的时间显示(如"05:30")',
    `video_mark_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标记标题 - 标记的简短标题',
    `video_mark_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '标记内容 - 标记的详细说明或笔记',
    `video_mark_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'BOOKMARK' COMMENT '标记类型 - BOOKMARK:书签, NOTE:笔记, HIGHLIGHT:高亮, CHAPTER:章节',
    `video_mark_color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '#1890ff' COMMENT '标记颜色 - 标记显示的颜色(十六进制)',
    `video_mark_thumbnail` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '截图 - 标记时间点的视频截图URL',
    `video_mark_duration` int NULL DEFAULT 0 COMMENT '标记时长 - 标记覆盖的时间长度(秒)，用于章节类型',
    `video_mark_sort` int NULL DEFAULT 0 COMMENT '排序号 - 标记在列表中的显示顺序',
    `video_mark_is_public` int NULL DEFAULT 0 COMMENT '是否公开 - 0:私有标记, 1:公开标记(可分享)',
    `video_mark_status` int NULL DEFAULT 1 COMMENT '标记状态 - 0:已删除, 1:正常',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`video_mark_id`) USING BTREE,
    INDEX `idx_video_id` (`video_info_id`) USING BTREE COMMENT '视频ID索引',
    INDEX `idx_mark_type` (`video_mark_type`) USING BTREE COMMENT '标记类型索引',
    INDEX `idx_mark_time` (`video_info_id`, `video_mark_time`) USING BTREE COMMENT '视频时间复合索引 - 用于按时间顺序获取标记',
    INDEX `idx_create_by` (`create_by`) USING BTREE COMMENT '创建人索引 - 用于获取用户的所有标记'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = '视频标记表 - 存储视频的时间标记和书签' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 5. 视频同步配置表 (video_sync_config)
-- 用途: 配置视频来源平台的同步参数
-- =====================================================
DROP TABLE IF EXISTS `video_sync_config`;
CREATE TABLE `video_sync_config` (
    `video_sync_config_id` int NOT NULL AUTO_INCREMENT COMMENT '配置ID - 主键自增',
    `video_sync_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置名称 - 同步任务的显示名称',
    `video_sync_source` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '来源平台 - 视频平台标识(YOUTUBE/BILIBILI/TIKTOK等)',
    `video_sync_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'CHANNEL' COMMENT '同步类型 - CHANNEL:频道, PLAYLIST:播放列表, USER:用户, KEYWORD:关键词',
    `video_sync_target_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '同步目标ID - 频道ID/播放列表ID/用户ID等',
    `video_sync_target_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '同步目标名称 - 频道名/播放列表名等',
    `video_sync_target_url` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '同步目标URL - 同步来源的页面地址',
    `video_sync_keyword` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '搜索关键词 - 当同步类型为KEYWORD时使用',
    `video_sync_interval` int NULL DEFAULT 24 COMMENT '同步间隔 - 自动同步的时间间隔(小时)',
    `video_sync_last_time` datetime NULL DEFAULT NULL COMMENT '最后同步时间 - 最近一次同步的时间',
    `video_sync_next_time` datetime NULL DEFAULT NULL COMMENT '下次同步时间 - 计划的下次同步时间',
    `video_sync_count` int NULL DEFAULT 0 COMMENT '同步次数 - 累计同步执行的次数',
    `video_sync_video_count` int NULL DEFAULT 0 COMMENT '视频数量 - 累计同步到的视频数量',
    `video_sync_auto_download` int NULL DEFAULT 0 COMMENT '自动下载 - 0:仅同步信息, 1:自动下载视频',
    `video_sync_download_quality` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '1080P' COMMENT '下载画质 - 自动下载时的画质设置',
    `video_sync_max_videos` int NULL DEFAULT 100 COMMENT '最大视频数 - 每次同步获取的最大视频数量',
    `video_sync_filter_config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '过滤配置 - 视频过滤条件(JSON格式)',
    `video_sync_proxy` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '代理设置 - 同步使用的代理地址',
    `video_sync_cookie` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'Cookie - 平台登录Cookie，用于获取会员内容',
    `video_sync_api_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'API密钥 - 平台API密钥(如YouTube Data API)',
    `video_sync_status` int NULL DEFAULT 1 COMMENT '配置状态 - 0:禁用, 1:启用, 2:同步中, 3:同步失败',
    `video_sync_error_msg` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误信息 - 最近一次同步失败的错误消息',
    `video_sync_extra` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '扩展配置 - 其他配置信息(JSON格式)',
    `create_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
    `create_by` int NULL DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
    `update_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
    `update_by` int NULL DEFAULT NULL COMMENT '更新人ID',
    PRIMARY KEY (`video_sync_config_id`) USING BTREE,
    INDEX `idx_source` (`video_sync_source`) USING BTREE COMMENT '来源平台索引',
    INDEX `idx_sync_type` (`video_sync_type`) USING BTREE COMMENT '同步类型索引',
    INDEX `idx_status` (`video_sync_status`) USING BTREE COMMENT '配置状态索引',
    INDEX `idx_next_time` (`video_sync_next_time`, `video_sync_status`) USING BTREE COMMENT '下次同步时间索引 - 用于调度同步任务'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci 
COMMENT = '视频同步配置表 - 配置视频来源平台的同步参数' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
