package com.chua.starter.collect.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 控制台登录入参。
 *
 * @author CH
 * @since 4.0.0.42
 */
@Data
@Schema(description = "采集控制台登录入参")
public class CollectLoginDTO {

    /**
     * 登录账号。
     */
    @Schema(description = "登录账号")
    private String username;

    /**
     * 登录密码。
     */
    @Schema(description = "登录密码")
    private String password;
}
