package com.chua.starter.system.pojo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 第三方解绑
 * @author CH
 * @since 2024/9/6
 */
@Data
@Schema(description = "第三方解绑")
public class ThirdPartyUnbindRequest {

    /**
     * 解绑类型
     */
    @Schema(description = "解绑类型")
    private String loginType;
}
