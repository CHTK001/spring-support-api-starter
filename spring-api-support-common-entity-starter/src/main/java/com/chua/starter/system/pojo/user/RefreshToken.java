package com.chua.starter.system.pojo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author CH
 * @since 2024/8/15
 */
@Data
@Schema(description = "刷新token")
public class RefreshToken {

    @Schema(description = "刷新token")
    private String refreshToken;
}
