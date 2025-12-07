package com.chua.starter.service.pojo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 鐢ㄦ埛甯冨眬
 * @author CH
 * @since 2024/8/29
 */
@Data
@Schema(description = "鐢ㄦ埛甯冨眬")
public class UserLayout {

    /**
     * 甯冨眬缃戞牸
     */
    @Schema(description = "甯冨眬缃戞牸")
    private String grid;

    /**
     * 甯冨眬
     */
    @Schema(description = "甯冨眬")
    private String layout;

    /**
     * 涓婚
     */
    @Schema(description = "涓婚")
    private String theme;
    /**
     * 棰滆壊
     */
    @Schema(description = "棰滆壊")
    private String color;

    /**
     * 缁勪欢鍒楄〃
     */
    @Schema(description = "缁勪欢鍒楄〃")
    private String component;
}
