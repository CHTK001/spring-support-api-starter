package com.chua.starter.service.pojo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author CH
 * @since 2024/8/15
 */
@Data
@Schema(description = "鍒锋柊token")
public class RefreshToken {

    @Schema(description = "鍒锋柊token")
    private String refreshToken;
}
