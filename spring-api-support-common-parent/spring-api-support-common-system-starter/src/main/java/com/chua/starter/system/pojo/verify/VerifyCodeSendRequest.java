package com.chua.starter.system.pojo.verify;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.Objects;

/**
 * 验证码下发请求
 *
 * <p>投递目标由服务端从当前登录账号的档案里取（邮箱取 sys_user_email，手机号取 sys_user_phone），
 * 不接受入参指定，避免把验证码发往他人地址。</p>
 *
 * @param channel 验证渠道
 * @author CH
 * @since 4.0.0.50
 */
@Schema(description = "验证码下发请求")
public record VerifyCodeSendRequest(
        @NotBlank(message = "验证渠道不能为空") @Schema(description = "验证渠道(EMAIL/SMS)", example = "EMAIL") String channel
) {

    /**
     * 规范构造器：验证渠道为空值敌对。
     *
     * <p>value class 前置条件——组件必须非空，与组件上的 {@code @NotBlank} 校验声明保持一致。</p>
     *
     * @param channel 验证渠道
     */
    public VerifyCodeSendRequest {
        channel = Objects.requireNonNull(channel, "channel 不能为 null");
    }
}
