package com.chua.starter.system.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
* 刷新令牌请求
*
* @版本 1.0.0
* @since 2024/8/15
* @author CH
 */
@Data
@Schema(description = "刷新token")
public class RefreshToken {

    /**
    * 刷新令牌
     */
    @Schema(description = "刷新token")
    /** Refresh令牌 */
    private String refreshToken;
}
