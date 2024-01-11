package com.chua.starter.admin.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 登录信息
 *
 * @author CH
 */
@Schema(description = "登录信息")
@Data
public class LoginData {

    @Schema(title = "账号")
    @NotNull(message = "账号不能为空")
    private String username;
    @Schema(title = "密码")
    @NotNull(message = "密码不能为空")
    private String password;
    @Schema(title = "校验码")
    @NotNull(message = "校验码不能为空")
    private String verifyCode;
}
