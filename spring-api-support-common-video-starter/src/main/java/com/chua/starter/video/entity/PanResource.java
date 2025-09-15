package com.chua.starter.video.entity;

import com.chua.starter.video.enums.PanType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 网盘资源实体类
 * 用于封装网盘搜索结果
 *
 * @author CH
 * @version 1.0
 * @since 2024/12/19
 */
@ApiModel(description = "网盘资源")
@Schema(description = "网盘资源")
@Data
public class PanResource {

    /**
     * 资源标题
     */
    @ApiModelProperty(value = "资源标题")
    @Schema(description = "资源标题")
    private String title;

    /**
     * 资源链接
     */
    @ApiModelProperty(value = "资源链接")
    @Schema(description = "资源链接")
    private String url;

    /**
     * 网盘类型
     */
    @ApiModelProperty(value = "网盘类型")
    @Schema(description = "网盘类型")
    private PanType panType;

    /**
     * 资源大小
     */
    @ApiModelProperty(value = "资源大小")
    @Schema(description = "资源大小")
    private String size;

    /**
     * 资源描述
     */
    @ApiModelProperty(value = "资源描述")
    @Schema(description = "资源描述")
    private String description;

    /**
     * 发布时间
     */
    @ApiModelProperty(value = "发布时间")
    @Schema(description = "发布时间")
    private LocalDateTime publishTime;

    /**
     * 来源插件
     */
    @ApiModelProperty(value = "来源插件")
    @Schema(description = "来源插件")
    private String source;

    /**
     * 资源评分/优先级
     */
    @ApiModelProperty(value = "资源评分")
    @Schema(description = "资源评分")
    private Double score;

    /**
     * 提取码（如果需要）
     */
    @ApiModelProperty(value = "提取码")
    @Schema(description = "提取码")
    private String extractCode;

    /**
     * 是否有效
     */
    @ApiModelProperty(value = "是否有效")
    @Schema(description = "是否有效")
    private Boolean valid;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 构造函数
     */
    public PanResource() {
        this.createTime = LocalDateTime.now();
        this.valid = true;
    }

    /**
     * 构造函数
     *
     * @param title   资源标题
     * @param url     资源链接
     * @param panType 网盘类型
     */
    public PanResource(String title, String url, PanType panType) {
        this();
        this.title = title;
        this.url = url;
        this.panType = panType;
    }

    /**
     * 构造函数
     *
     * @param title   资源标题
     * @param url     资源链接
     * @param panType 网盘类型
     * @param size    资源大小
     * @param source  来源插件
     */
    public PanResource(String title, String url, PanType panType, String size, String source) {
        this(title, url, panType);
        this.size = size;
        this.source = source;
    }
}