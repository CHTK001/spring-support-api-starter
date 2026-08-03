package com.chua.starter.oauth.server.support;

/**
 * OAuth 认证服务器标记类
 *
 * <p>仅作为标记存在，用于让 oauth-client-starter 的 {@code AuthFilter} 自动配置
 * （通过 {@code @ConditionalOnMissingClass("com.chua.starter.oauth.server.support.SsoServer")} 判断）
 * 在认证服务器应用上不注册，避免认证协议入口（/login、/verify、/upgrade 等）被客户端认证过滤器二次拦截。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
public class SsoServer {
}
