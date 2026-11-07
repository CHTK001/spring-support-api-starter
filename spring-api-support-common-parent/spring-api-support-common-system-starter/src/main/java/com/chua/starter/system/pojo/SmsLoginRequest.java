package com.chua.starter.system.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.Objects;

/**
 * 短信验证码登录请求
 *
 * @param phone 手机号
 * @param code 验证码
 *
 * @since 2024/12/11
 * @author CH
 */
@Schema(description = "短信验证码登录请求")
public record SmsLoginRequest(
        @NotBlank(message = "手机号不能为空") @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确") @Schema(description = "手机号",
            example = "13800138000") String phone,
        @NotBlank(message = "验证码不能为空") @Pattern(regexp = "^\\d{4,6}$", message = "验证码格式不正确") @Schema(description = "验证码",
            example = "123456") String code
) {

    /**
     * 规范构造器：手机号与验证码为空值敌对。
     *
     * <p>value class 前置条件——组件必须非空，与组件上的 {@code @NotBlank} 校验声明保持一致。</p>
     *
     * @param phone 手机号
     * @param code  验证码
     */
    public SmsLoginRequest {
        phone = Objects.requireNonNull(phone, "phone 不能为 null");
        code = Objects.requireNonNull(code, "code 不能为 null");
    }
}
