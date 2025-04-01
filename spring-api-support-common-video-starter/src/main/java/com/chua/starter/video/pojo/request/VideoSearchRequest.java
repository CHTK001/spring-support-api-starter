package com.chua.starter.video.pojo.request;

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
public class VideoSearchRequest implements Serializable {

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

    /**
     * 页码
     */
    @ApiModelProperty(value = "页码", example = "1")
    private Integer page = 1;

    /**
     * 每页大小
     */
    @ApiModelProperty(value = "每页大小", example = "10")
    private Integer pageSize = 10;
}