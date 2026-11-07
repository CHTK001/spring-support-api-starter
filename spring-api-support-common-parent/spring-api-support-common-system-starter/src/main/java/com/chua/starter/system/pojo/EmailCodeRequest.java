package com.chua.starter.system.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Objects;

/**
 * 邮箱验证码请求
 *
 * @param email   邮箱
 * @param bizType 业务类型 <p>login-登录, bind-绑定, reset-重置密码</p>
 *
 * @since 4.0.0.50
 * @author CH
 */
@Schema(description = "邮箱验证码请求")
public record EmailCodeRequest(
        @NotBlank(message = "邮箱不能为空") @Email(message = "邮箱格式不正确") @Schema(description = "邮箱", example = "user@example.com") String email,
        @NotBlank(message = "业务类型不能为空") @Schema(description = "业务类型(login/bind/reset)", example = "login") String bizType
) {

    /**
     * 规范构造器：邮箱与业务类型为空值敌对。
     *
     * <p>value class 前置条件——组件必须非空，与组件上的 {@code @NotBlank} 校验声明保持一致。</p>
     *
     * @param email   邮箱
     * @param bizType 业务类型
     */
    public EmailCodeRequest {
        email = Objects.requireNonNull(email, "email 不能为 null");
        bizType = Objects.requireNonNull(bizType, "bizType 不能为 null");
    }
}
