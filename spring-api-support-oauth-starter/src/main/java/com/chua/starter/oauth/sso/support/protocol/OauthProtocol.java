package com.chua.starter.oauth.sso.support.protocol;

/**
* OAuth 认证协议接口。
*
* <p>各协议实现（HTTP / Armeria / KCP / RSocket …）通过 {@code @Spi}（如 "http"、"armeria"）
* 注册扩展名，由 {@link com.chua.common.support.spi.ServiceProvider} 按名称分发，
* 业务侧无需硬编码 switch 选择实现。</p>
*
* @author CH
* @since 4.0.0.42
 */
public interface OauthProtocol {
}