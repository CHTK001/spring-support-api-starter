package com.chua.starter.system.pojo.verify;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.Objects;

/**
 * 验证码校验请求
 *
 * @param channel 验证渠道
 * @param code    用户提交的验证码
 * @author CH
 * @since 4.0.0.50
 */
@Schema(description = "验证码校验请求")
public record VerifyCodeVerifyRequest(
        @NotBlank(message = "验证渠道不能为空") @Schema(description = "验证渠道(EMAIL/SMS)", example = "EMAIL") String channel,
        @NotBlank(message = "验证码不能为空") @Pattern(regexp = "^\\d{4,8}$", message = "验证码为4-8位数字") @Schema(description = "验证码",
            example = "123456") String code
) {
    /**
     * 规范构造器：校验必填组件非空。
     *
     * <p>value class 前置条件——规范构造器必须空值敌对。</p>
     *
     * @param channel 验证渠道
     * @param code    用户提交的验证码
     */
    public VerifyCodeVerifyRequest {
        channel = Objects.requireNonNull(channel, "channel 不能为 null");
        code = Objects.requireNonNull(code, "code 不能为 null");
    }
}
