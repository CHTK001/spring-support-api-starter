package com.chua.starter.system.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 鐧诲綍鍝嶅簲瀵硅薄
 *
 */
@Schema(description = "鐧诲綍鍝嶅簲瀵硅薄")
@Data
@Builder
public class LoginResult {

    @Schema(description = "璁块棶token")
    private String accessToken;

    @Schema(description = "token 绫诲瀷", example = "Bearer")
    private String tokenType;

    @Schema(description = "")
    private String refreshToken;
    @Schema(description = "")
    private UserInfoVO userInfo;
    @Schema(description = "过期时间(单位：毫秒)")
    private Long expires;

}
