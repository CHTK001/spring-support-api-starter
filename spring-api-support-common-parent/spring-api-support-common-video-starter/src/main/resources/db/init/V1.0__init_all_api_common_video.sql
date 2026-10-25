-- ============================================================
-- 视频本地索引表（MySQL 版）
--
-- 资源站（MacCMS）目录同步落库后的查询表：列表/筛选/排序/分页直接读本表，
-- 详情快照（线路 + 下载）以 JSON 存在 video_payload，避免再拆子表。
-- 唯一键 video_origin_id 保证同一资源站条目只保留一行。
-- ============================================================

CREATE TABLE IF NOT EXISTS video_info (
    video_info_id       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    video_origin_id     VARCHAR(64)  NOT NULL COMMENT '资源站条目ID（MacCMS vod_id）',
    video_source        VARCHAR(32)  NULL DEFAULT 'maccms' COMMENT '来源标识',
    video_name          VARCHAR(255) NULL COMMENT '名称',
    video_alias_name    VARCHAR(255) NULL COMMENT '别名',
    video_category      VARCHAR(32)  NULL COMMENT '分类码 movie/tv/anime/variety/documentary',
    video_type_name     VARCHAR(64)  NULL COMMENT '资源站分类名（电影/电视剧等）',
    video_year          INT          NULL COMMENT '年份',
    video_district      VARCHAR(64)  NULL COMMENT '地区',
    video_language      VARCHAR(32)  NULL COMMENT '语言',
    video_tags          VARCHAR(255) NULL COMMENT '标签（逗号分隔）',
    video_score         DECIMAL(4,1) NULL COMMENT '评分',
    video_views         DECIMAL(18,0) NULL COMMENT '播放量',
    video_cover         VARCHAR(512) NULL COMMENT '封面地址',
    video_actor         VARCHAR(512) NULL COMMENT '主演',
    video_director      VARCHAR(255) NULL COMMENT '导演',
    video_description   TEXT         NULL COMMENT '简介',
    video_remark        VARCHAR(128) NULL COMMENT '更新状态（更新至N集 / HD国语）',
    video_quality       VARCHAR(32)  NULL COMMENT '清晰度',
    video_publish_time  DATETIME     NULL COMMENT '资源站更新时间',
    video_payload       MEDIUMTEXT   NULL COMMENT '详情快照 JSON（线路/剧集/下载）',
    video_status        INT          NULL DEFAULT 1 COMMENT '1 上架 0 下架',
    video_sync_time     DATETIME     NULL COMMENT '最近同步时间',
    create_time         DATETIME     NULL,
    update_time         DATETIME     NULL,
    PRIMARY KEY (video_info_id),
    UNIQUE KEY uk_video_info_origin (video_origin_id),
    KEY idx_video_info_cat_time (video_category, video_publish_time),
    KEY idx_video_info_score (video_score),
    KEY idx_video_info_year (video_year)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '视频本地索引';
