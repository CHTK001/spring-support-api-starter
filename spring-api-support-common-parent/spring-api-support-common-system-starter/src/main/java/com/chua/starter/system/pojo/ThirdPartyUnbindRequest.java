package com.chua.starter.system.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 第三方解绑请求
 *
   * @版本 1.0.0
 * @since 2024/9/6
 * @author CH
 */
@Data
@Schema(description = "第三方解绑")
public class ThirdPartyUnbindRequest {

    /**
     * 解绑类型
     */
    @Schema(description = "解绑类型")
    /** Login类型 */
    private String loginType;
}
