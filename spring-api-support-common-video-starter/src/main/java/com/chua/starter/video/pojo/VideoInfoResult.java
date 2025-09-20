package com.chua.starter.video.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.chua.starter.video.entity.VideoDownload;
import com.chua.starter.video.entity.VideoInfo;
import com.chua.starter.video.entity.VideoMark;
import com.chua.starter.video.entity.VideoPlayAddress;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author CH
 * @since 2025/9/15 9:43
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "视频信息分页结果")
public class VideoInfoResult extends VideoInfo {


    /**
     * 视频标签
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "视频标签")
    @Schema(description = "视频标签")
    private List<VideoMark> videoMarkList;

    /**
     * 视频下载地址
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "视频下载地址")
    @Schema(description = "视频下载地址")
    private List<VideoDownload> downloadList;
    /**
     * 播放地址
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "播放地址")
    @Schema(description = "播放地址")
    private List<VideoPlayAddress> playAddressesList;

    /**
     * 下载地址
     */
    private String downloadUrls;

}
