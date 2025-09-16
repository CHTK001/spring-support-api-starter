package com.chua.starter.video.pojo;

import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.video.entity.VideoInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author CH
 * @since 2025/9/15 21:04
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(title = "视频搜索")
public class VideoSearch extends Query<VideoInfo> {

    /**
     * 关键词
     */
    @Schema(title = "关键词")
    private String keyword;

    /**
     * 年份
     */
    @Schema(title = "年份")
    private Integer year;
    /**
     * 分类
     */
    @Schema(title = "分类")
    private String category;
    /**
     * 视频类型
     */
    @Schema(title = "视频类型")
    private String videoType;
    /**
     * 平台
     */
    @Schema(title = "平台")
    private String platform;

}
