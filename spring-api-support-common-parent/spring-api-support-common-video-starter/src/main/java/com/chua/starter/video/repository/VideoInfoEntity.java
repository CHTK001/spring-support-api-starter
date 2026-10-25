package com.chua.starter.video.repository;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 视频本地索引实体（{@code video_info}）。
 *
 * <p>资源站目录同步落库后的查询载体：列表、筛选、排序、分页只读本表的
 * 标量列；详情（播放线路 + 下载资源）原样存在 {@code video_payload}，
 * 避免为线路/剧集/下载再拆三张子表。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Data
@TableName("video_info")
public class VideoInfoEntity {

    /**
     * 主键。
     */
    @TableId(value = "video_info_id", type = IdType.AUTO)
    private Long videoInfoId;

    /**
     * 资源站条目 ID（MacCMS vod_id），唯一键。
     */
    @TableField("video_origin_id")
    private String videoOriginId;

    /**
     * 来源标识。
     */
    @TableField("video_source")
    private String videoSource;

    /**
     * 名称。
     */
    @TableField("video_name")
    private String videoName;

    /**
     * 别名。
     */
    @TableField("video_alias_name")
    private String videoAliasName;

    /**
     * 分类码（movie/tv/anime/variety/documentary）。
     */
    @TableField("video_category")
    private String videoCategory;

    /**
     * 资源站分类名（电影/电视剧/动漫…）。
     */
    @TableField("video_type_name")
    private String videoTypeName;

    /**
     * 年份。
     */
    @TableField("video_year")
    private Integer videoYear;

    /**
     * 地区。
     */
    @TableField("video_district")
    private String videoDistrict;

    /**
     * 语言。
     */
    @TableField("video_language")
    private String videoLanguage;

    /**
     * 标签（逗号分隔）。
     */
    @TableField("video_tags")
    private String videoTags;

    /**
     * 评分。
     */
    @TableField("video_score")
    private BigDecimal videoScore;

    /**
     * 播放量。
     */
    @TableField("video_views")
    private BigDecimal videoViews;

    /**
     * 封面地址。
     */
    @TableField("video_cover")
    private String videoCover;

    /**
     * 主演。
     */
    @TableField("video_actor")
    private String videoActor;

    /**
     * 导演。
     */
    @TableField("video_director")
    private String videoDirector;

    /**
     * 简介。
     */
    @TableField("video_description")
    private String videoDescription;

    /**
     * 更新状态（更新至 N 集 / HD 国语）。
     */
    @TableField("video_remark")
    private String videoRemark;

    /**
     * 清晰度。
     */
    @TableField("video_quality")
    private String videoQuality;

    /**
     * 资源站更新时间。
     */
    @TableField("video_publish_time")
    private LocalDateTime videoPublishTime;

    /**
     * 详情快照 JSON（线路 / 剧集 / 下载）。
     */
    @TableField("video_payload")
    private String videoPayload;

    /**
     * 上架状态：1 上架，0 下架。
     */
    @TableField("video_status")
    private Integer videoStatus;

    /**
     * 最近同步时间。
     */
    @TableField("video_sync_time")
    private LocalDateTime videoSyncTime;

    /**
     * 创建时间。
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间。
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}
