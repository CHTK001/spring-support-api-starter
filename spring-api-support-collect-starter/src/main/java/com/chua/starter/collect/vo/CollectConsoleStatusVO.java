package com.chua.starter.collect.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 控制台登录状态出参。
 *
 * @author CH
 * @since 4.0.0.42
 */
@Data
@Schema(description = "采集控制台登录状态")
public class CollectConsoleStatusVO {

    /**
     * 是否开启登录校验。
     */
    @Schema(description = "是否开启登录校验")
    private Boolean authEnabled;

    /**
     * 当前会话是否已登录。
     */
    @Schema(description = "当前会话是否已登录")
    private Boolean authenticated;

    /**
     * 已登录的账号。
     */
    @Schema(description = "已登录账号")
    private String username;
}
