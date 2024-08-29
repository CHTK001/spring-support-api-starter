package com.chua.starter.system.pojo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户布局
 * @author CH
 * @since 2024/8/29
 */
@Data
@Schema(description = "用户布局")
public class UserLayout {

    /**
     * 布局网格
     */
    @Schema(description = "布局网格")
    private String[] grid;

    /**
     * 布局
     */
    @Schema(description = "布局")
    private String[] layout;

    /**
     * 主题
     */
    @Schema(description = "主题")
    private String theme;
    /**
     * 颜色
     */
    @Schema(description = "颜色")
    private String color;

    /**
     * 组件列表
     */
    @Schema(description = "组件列表")
    private String[] component;
}
