package com.chua.starter.system.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 刷新token请求
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/8/15
 */
@Data
@Schema(description = "刷新token")
public class RefreshToken {

    /**
     * 刷新token
     */
    @Schema(description = "刷新token")
    private String refreshToken;
}
