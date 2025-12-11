package com.chua.starter.auth.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 登录响应对象
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/8/15
 */
@Schema(description = "登录响应对象")
@Data
@Builder
public class LoginResult {

    /**
     * 访问token
     */
    @Schema(description = "访问token")
    private String accessToken;

    /**
     * token 类型
     */
    @Schema(description = "token 类型", example = "Bearer")
    private String tokenType;

    /**
     * 刷新token
     */
    @Schema(description = "刷新token")
    private String refreshToken;

    /**
     * 用户信息
     */
    @Schema(description = "用户信息")
    private UserInfoVO userInfo;

    /**
     * 过期时间(单位：毫秒)
     */
    @Schema(description = "过期时间(单位：毫秒)")
    private Long expires;
}
