package com.chua.starter.video.pojo.request;

import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.video.entity.VideoInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 视频搜索请求
 *
 * @author CH
 * @since 2024/6/21
 */
@Data
@ApiModel(description = "视频搜索请求")
public class VideoSearchRequest extends Query<VideoInfo> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关键词
     */
    @ApiModelProperty(value = "关键词")
    private String keyword;

    /**
     * 选中的视频类型ID
     */
    @ApiModelProperty(value = "选中的视频类型ID")
    private String selectedType;

    /**
     * 平台ID
     */
    @ApiModelProperty(value = "平台ID")
    private String platformId;
}