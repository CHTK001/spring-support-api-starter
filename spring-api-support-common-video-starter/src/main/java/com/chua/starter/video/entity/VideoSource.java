package com.chua.starter.video.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chua.common.support.utils.ArrayUtils;
import com.chua.starter.mybatis.pojo.SysBase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import static com.chua.common.support.sql.SortCondition.SortDirection.ASC;

/**
 * @author CH
 * @since 2025/9/16 13:46
 */

/**
 * 视频来源
 */
@ApiModel(description = "视频来源")
@Schema(description = "视频来源")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "video_source")
public class VideoSource extends SysBase {
    private static final String DEFAULT_VIDEO_SOURCE_TYPE = "MV,TV,AC";
    /**
     * 排序
     * MV: 电影
     * TV: 电视
     * AC: 动漫
     * DOC: 纪录片
     * VAR: 综艺
     * MUSIC: 音乐
     * SP: 体育
     */
    private static final String[] SORTED_ORDER = {
            "MV", "TV", "AC", "DOC", "VAR", "MUSIC", "SP"
    };

    @TableId(value = "video_source_id", type = IdType.AUTO)
    @ApiModelProperty(value = "")
    @Schema(description = "")
    private Integer videoSourceId;

    /**
     * 名称
     */
    @TableField(value = "video_source_name")
    @ApiModelProperty(value = "名称")
    @Schema(description = "名称")
    private String videoSourceName;

    /**
     * 平台
     */
    @TableField(value = "video_source_platform")
    @ApiModelProperty(value = "平台")
    @Schema(description = "平台")
    private String videoSourcePlatform;

    /**
     * 地址
     */
    @TableField(value = "video_source_url")
    @ApiModelProperty(value = "地址")
    @Schema(description = "地址")
    private String videoSourceUrl;

    /**
     * 是否开启;0:关闭
     */
    @TableField(value = "video_source_enable")
    @ApiModelProperty(value = "是否开启;0:关闭")
    @Schema(description = "是否开启;0:关闭")
    private Integer videoSourceEnable;

    /**
     * 令牌
     */
    @TableField(value = "video_source_token")
    @ApiModelProperty(value = "令牌")
    @Schema(description = "令牌")
    private String videoSourceToken;

    /**
     * 最大结果数
     */
    @TableField(value = "video_source_max_resource")
    @ApiModelProperty(value = "最大结果数")
    @Schema(description = "最大结果数")
    private Integer videoSourceMaxResource;

    /**
     * user agent
     */
    @TableField(value = "video_source_user_agent")
    @ApiModelProperty(value = "user agent")
    @Schema(description = "user agent")
    private String videoSourceUserAgent;

    /**
     * cookie
     */
    @TableField(value = "video_source_cookie")
    @ApiModelProperty(value = "cookie")
    @Schema(description = "cookie")
    private String videoSourceCookie;

    /**
     * 图标
     */
    @TableField(value = "video_source_icon")
    @ApiModelProperty(value = "图标")
    @Schema(description = "图标")
    private String videoSourceIcon;

    /**
     * 封面后缀
     */
    @TableField(value = "video_source_cover_suffix")
    @ApiModelProperty(value = "封面后缀")
    private String videoSourceCoverSuffix;
    /**
     * 最小年份
     */
    @TableField(value = "video_source_min_year")
    @ApiModelProperty(value = "最小年份")
    private Integer videoSourceMinYear;
    /**
     * 类型
     */
    @TableField(value = "video_source_type")
    @ApiModelProperty(value = "类型;MV, TV, AC")
    private String videoSourceType;

    /**
     * 同步数量
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "同步数量")
    private int synCount;

    /**
     * 获取视频类型
     *
     * @return 视频类型
     */
    public String getVideoSourceType() {
        return StringUtils.defaultIfBlank(videoSourceType, DEFAULT_VIDEO_SOURCE_TYPE);
    }

    /**
     * 获取视频类型列表
     *
     * @return 视频类型列表
     */
    public String[] getVideoSourceTypeForList() {
        return ArrayUtils.sort(StringUtils.split(getVideoSourceType(), ","), SORTED_ORDER, ASC);
    }

    /**
     * 获取视频类型列表
     *
     * @return 视频类型列表
     */
    public String[] getVideoSourceTypeForListAfter(String offsetElement) {
        return ArrayUtils.filterAfter(StringUtils.split(getVideoSourceType(), ","), SORTED_ORDER, offsetElement);
    }
}