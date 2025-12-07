package com.chua.starter.service.result;

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

    @Schema(description = "鍒锋柊token")
    private String refreshToken;
    @Schema(description = "鐢ㄦ埛淇℃伅")
    private UserInfoVO userInfo;
    @Schema(description = "杩囨湡鏃堕棿(鍗曚綅锛氭绉?")
    private Long expires;

}
