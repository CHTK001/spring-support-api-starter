package com.chua.starter.system.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 短信验证码请求
 *
 * @author CH
 * @since 2024/12/11
 */
@Data
@Schema(description = "短信验证码请求")
public class SmsCodeRequest {

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    /**
     * 业务类型
     * <p>login-登录, register-注册, reset-重置密码</p>
     */
    @NotBlank(message = "业务类型不能为空")
    @Schema(description = "业务类型(login/register/reset)", example = "login")
    private String bizType;
}
