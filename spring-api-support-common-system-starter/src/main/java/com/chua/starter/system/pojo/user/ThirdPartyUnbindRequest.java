package com.chua.starter.system.pojo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 涓夋柟解绑请求
 *
 * @author CH
 * @since 2024/9/6
 */
@Data
@Schema(description = "涓夋柟解绑请求")
public class ThirdPartyUnbindRequest {

    /**
     * 登录类型
     */
    @Schema(description = "登录类型")
    private String loginType;
}