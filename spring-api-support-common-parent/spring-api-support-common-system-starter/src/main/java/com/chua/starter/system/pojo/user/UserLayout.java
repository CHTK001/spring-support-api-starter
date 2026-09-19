package com.chua.starter.system.pojo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户布局
 *
 * @since 2024/8/29
 * @author CH
 */
@Data
@Schema(description = "用户布局")
public class UserLayout {

    /**
     * 布局网格
     */
    @Schema(description = "布局网格")
    /** Grid */
    private String grid;

    /**
     * 布局
     */
    @Schema(description = "布局")
    /** Layout */
    private String layout;

    /**
     * 主题
     */
    @Schema(description = "主题")
    /** Theme */
    private String theme;

    /**
     * 颜色
     */
    @Schema(description = "颜色")
    /** 颜色 */
    private String color;

    /**
     * 组件列表
     */
    @Schema(description = "组件列表")
    /** 组件 */
    private String component;

    /**
     * 用户偏好配置（lay-setting 配置，JSON格式）
     */
    @Schema(description = "用户偏好配置")
    /** Preference */
    private String preference;
}
