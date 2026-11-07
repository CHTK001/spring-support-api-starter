package com.chua.starter.system.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.Objects;

/**
 * 邮箱验证码登录请求
 *
 * @param email 邮箱
 * @param code  验证码
 *
 * @since 4.0.0.50
 * @author CH
 */
@Schema(description = "邮箱验证码登录请求")
public record EmailLoginRequest(
        @NotBlank(message = "邮箱不能为空") @Email(message = "邮箱格式不正确") @Schema(description = "邮箱", example = "user@example.com") String email,
        @NotBlank(message = "验证码不能为空") @Pattern(regexp = "^\\d{4,6}$", message = "验证码格式不正确") @Schema(description = "验证码",
            example = "123456") String code
) {

    /**
     * 规范构造器：邮箱与验证码为空值敌对。
     *
     * <p>value class 前置条件——组件必须非空，与组件上的 {@code @NotBlank} 校验声明保持一致。</p>
     *
     * @param email 邮箱
     * @param code  验证码
     */
    public EmailLoginRequest {
        email = Objects.requireNonNull(email, "email 不能为 null");
        code = Objects.requireNonNull(code, "code 不能为 null");
    }
}
