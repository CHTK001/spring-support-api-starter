package com.chua.starter.system.pojo.verify;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

/**
 * 邮箱服务器连通性探测结果
 *
 * @param smtpSuccess       发信通道是否握手并认证成功
 * @param smtpMessage       发信通道探测说明（失败时为服务端原因）
 * @param smtpCost            发信通道探测耗时(毫秒)
 * @param imapSuccess       收信通道是否握手并认证成功
 * @param imapMessage       收信通道探测说明（失败时为服务端原因）
 * @param imapCost            收信通道探测耗时(毫秒)
 * @param emailVerifyEnable 邮箱验证码能力是否可用（发信通道配置齐备即为可用）
 * @author CH
 * @since 4.0.0.50
 */
@Schema(description = "邮箱服务器连通性探测结果")
public record EmailConnectivityVO(
        @Schema(description = "SMTP 是否连通") boolean smtpSuccess,
        @Schema(description = "SMTP 探测说明") String smtpMessage,
        @Schema(description = "SMTP 探测耗时(毫秒)") long smtpCost,
        @Schema(description = "IMAP 是否连通") boolean imapSuccess,
        @Schema(description = "IMAP 探测说明") String imapMessage,
        @Schema(description = "IMAP 探测耗时(毫秒)") long imapCost,
        @Schema(description = "邮箱验证码能力是否可用") boolean emailVerifyEnable) {

    /**
     * 规范构造器：探测说明为空值敌对。
     *
     * <p>value class 前置条件——引用组件必须非空；两个通道的探测说明在
     * {@code EmailProbe} 的所有分支上都是非空字面量，故可安全拒绝 null。</p>
     *
     * @param smtpSuccess       发信通道是否握手并认证成功
     * @param smtpMessage       发信通道探测说明（失败时为服务端原因）
     * @param smtpCost          发信通道探测耗时(毫秒)
     * @param imapSuccess       收信通道是否握手并认证成功
     * @param imapMessage       收信通道探测说明（失败时为服务端原因）
     * @param imapCost          收信通道探测耗时(毫秒)
     * @param emailVerifyEnable 邮箱验证码能力是否可用
     */
    public EmailConnectivityVO {
        smtpMessage = Objects.requireNonNull(smtpMessage, "smtpMessage 不能为 null");
        imapMessage = Objects.requireNonNull(imapMessage, "imapMessage 不能为 null");
    }
}
