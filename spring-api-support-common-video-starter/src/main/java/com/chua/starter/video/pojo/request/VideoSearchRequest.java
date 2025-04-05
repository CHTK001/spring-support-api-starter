package com.chua.starter.video.pojo.request;

import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.video.entity.VideoInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.annotation.PostConstruct;
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

    /**
     * 地区列表，多个地区用逗号分隔
     */
    @ApiModelProperty(value = "地区列表，多个地区用逗号分隔")
    private String districts;

    /**
     * 语言列表，多个语言用逗号分隔
     */
    @ApiModelProperty(value = "语言列表，多个语言用逗号分隔")
    private String languages;

    /**
     * 视频类型列表，多个类型用逗号分隔
     */
    @ApiModelProperty(value = "视频类型列表，多个类型用逗号分隔")
    private String videoTypes;

    /**
     * 视频分类
     */
    @ApiModelProperty(value = "视频分类")
    private String category;


    /**
     * 年份
     */
    @ApiModelProperty(value = "年份")
    private String years;

    /**
     * 判断字段值是否为"全部"
     *
     * @param value 字段值
     * @return 是否为全部
     */
    public boolean isAll(String value) {
        return value == null || "全部".equals(value);
    }

    /**
     * 重写getDistricts方法，处理"全部"的情况
     */
    public String getDistricts() {
        return isAll(districts) ? null : districts;
    }

    /**
     * 重写getLanguages方法，处理"全部"的情况
     */
    public String getLanguages() {
        return isAll(languages) ? null : languages;
    }

    /**
     * 重写getVideoTypes方法，处理"全部"的情况
     */
    public String getVideoTypes() {
        return isAll(videoTypes) ? null : videoTypes;
    }

    /**
     * 重写getYears方法，处理"全部"的情况
     */
    public String getYears() {
        return isAll(years) ? null : years;
    }
    /**
     * 初始化处理请求参数
     */
    @PostConstruct
    public void init() {

        // 处理视频分类
        if (!isAll(category)) {
            // 可以根据category设置其他相关字段
        }

        // 处理排序方式
        String sort = getOrder();
        if (sort != null && !sort.isEmpty()) {
            // 设置排序字段和排序方式
            switch (sort) {
                case "recommend":
                    // 推荐排序
                    this.setOrder("video_score desc");
                    break;
                case "newest":
                    // 最新排序
                    this.setOrder("video_publish_date desc");
                    break;
                case "hottest":
                    // 最热排序
                    this.setOrder("video_views desc");
                    break;
                default:
                    // 默认排序
                    this.setOrder("video_score desc");
                    break;
            }
        }
    }
}