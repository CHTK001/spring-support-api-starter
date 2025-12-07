package com.chua.starter.system.pojo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 涓夋柟瑙ｇ粦璇锋眰
 *
 * @author CH
 * @since 2024/9/6
 */
@Data
@Schema(description = "涓夋柟瑙ｇ粦璇锋眰")
public class ThirdPartyUnbindRequest {

    /**
     * 鐧诲綍绫诲瀷
     */
    @Schema(description = "鐧诲綍绫诲瀷")
    private String loginType;
}