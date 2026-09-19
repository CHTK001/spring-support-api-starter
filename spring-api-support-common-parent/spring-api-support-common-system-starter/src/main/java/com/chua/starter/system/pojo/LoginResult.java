package com.chua.starter.system.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 登录响应对象
 *
 * @since 2024/8/15
 * @author CH
 */
@Schema(description = "登录响应对象")
@Data
@Builder
public class LoginResult {

    /**
     * 访问令牌
     */
    @Schema(description = "访问token")
    /** Access令牌 */
    private String accessToken;

    /**
     * 令牌 类型
     */
    @Schema(description = "token 类型", example = "Bearer")
    /** 令牌类型 */
    private String tokenType;

    /**
     * 刷新令牌
     */
    @Schema(description = "刷新token")
    /** Refresh令牌 */
    private String refreshToken;

    /**
     * 用户信息
     */
    @Schema(description = "用户信息")
    /** 用户信息 */
    private UserInfoVO userInfo;

    /**
     * 过期时间(单位：毫秒)
     */
    @Schema(description = "过期时间(单位：毫秒)")
    /** Expires */
    private Long expires;
}
