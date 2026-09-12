package com.chua.starter.oauth.sso.support.protocol;

/**
* OAuth 认证协议常量
*
* <p>集中管理多协议 Server 的默认端口、认证路由路径、请求/响应头名称、内容类型等,
* 避免在 {@link OauthProtocolServerManager} 与 {@link OauthProtocolHandler} 中散落硬编码值。</p>
*
* @since 4.0.0.42
* @author CH
 */
public final class OauthProtocolConstants {

    /** 创建 oauth协议常量 实例 */
    private OauthProtocolConstants() {
    }

    /**
    * 默认 HTTP 服务端 类型(对应 通用 network.服务端 的 SPI 键)
     */
    public static final String DEFAULT_TYPE = "jdk";

    /**
    * 默认协议
     */
    public static final String DEFAULT_PROTOCOL = "http";

    /**
    * 默认监听主机
     */
    public static final String DEFAULT_HOST = "0.0.0.0";

    /**
    * 默认监听端口
     */
    public static final int DEFAULT_PORT = 19180;

    /**
    * 默认上下文路径
     */
    public static final String DEFAULT_CONTEXT_PATH = "/";

    /**
    * 消息型协议(KCP)的本机回环地址
     */
    public static final String LOCALHOST = "127.0.0.1";

    /**
    * 统一内容类型
     */
    public static final String CONTENT_TYPE_JSON = "application/json";

    /**
    * 客户端 地址 的 /OAuth 兼容前缀
     */
    public static final String OAUTH_PREFIX = "/oauth";

    /**
    * 登录路径
     */
    public static final String PATH_LOGIN = "/login";

    /**
    * 登出路径
     */
    public static final String PATH_LOGOUT = "/logout";

    /**
    * 令牌升级路径
     */
    public static final String PATH_UPGRADE = "/upgrade";

    /**
    * 临时令牌路径
     */
    public static final String PATH_TEMPORARY_TOKEN = "/temporary-token";

    /**
    * 令牌鉴权路径
     */
    public static final String PATH_VERIFY = "/verify";

    /**
    * 兼容鉴权路径(/OAuth)
     */
    public static final String PATH_OAUTH = "/oauth";

    /**
    * 认证路由路径集合(与 {@link OauthProtocolHandler} 支持的路径一致)
     */
    public static final String[] AUTH_ROUTES = {
            PATH_LOGIN, PATH_LOGOUT, PATH_UPGRADE, PATH_TEMPORARY_TOKEN, PATH_VERIFY, PATH_OAUTH
    };

    /**
    * 认证请求唯一标识头
     */
    public static final String HEADER_UUID = "x-oauth-uuid";

    /**
    * 认证请求序列头
     */
    public static final String HEADER_SERIAL = "x-oauth-serial";

    /**
    * 认证请求签名头
     */
    public static final String HEADER_SIGN = "x-oauth-sign";

    /**
    * 认证请求 访问密钥 头
     */
    public static final String HEADER_ACCESS_KEY = "x-oauth-ak";

    /**
    * 认证请求加密标记头
     */
    public static final String HEADER_ENCODE = "x-oauth-encode";

    /**
    * 客户端地址头
     */
    public static final String HEADER_ADDRESS = "x-oauth-param-address";

    /**
    * 认证响应序列头
     */
    public static final String HEADER_RESPONSE_SERIAL = "x-oauth-response-serial";

    /**
    * 授权头(用于解析 Bearer 令牌)
     */
    public static final String HEADER_AUTHORIZATION = "Authorization";

    /**
    * Bearer 令牌 前缀
     */
    public static final String AUTH_SCHEME_BEARER = "Bearer ";

    /**
    * 请求体数据包装字段
     */
    public static final String FIELD_DATA = "data";

    /**
    * 用户名
     */
    public static final String FIELD_USERNAME = "x-oauth-username";

    /**
    * 密码
     */
    public static final String FIELD_PASSWORD = "x-oauth-password";

    /**
    * 认证类型(web/tenant/wechat/sms 等)
     */
    public static final String FIELD_AUTH_TYPE = "x-oauth-auth-type";

    /**
    * 扩展参数
     */
    public static final String FIELD_EXT = "x-oauth-ext";

    /**
    * 令牌 值
     */
    public static final String FIELD_TOKEN = "x-oauth-token";

    /**
    * Cookie 值
     */
    public static final String FIELD_COOKIE = "x-oauth-cookie";

    /**
    * 用户唯一标识
     */
    public static final String FIELD_UID = "x-oauth-uid";

    /**
    * 刷新令牌
     */
    public static final String FIELD_REFRESH_TOKEN = "x-oauth-refresh-token";

    /**
    * 升级类型(时间戳 / 版本 / REFRESH)
     */
    public static final String FIELD_UPGRADE_TYPE = "x-oauth-upgrade-type";

    /**
    * 登出类型
     */
    public static final String FIELD_LOGOUT_TYPE = "x-oauth-logout-type";

    /**
    * 源令牌(用于生成临时令牌)
     */
    public static final String FIELD_SOURCE_TOKEN = "x-oauth-source-token";

    /**
    * KCP 请求-响应协议请求前缀
     */
    public static final String KCP_REQ_PREFIX = "req/";

    /**
    * KCP 请求-响应协议响应前缀
     */
    public static final String KCP_RESP_PREFIX = "resp/";

    /**
    * KCP 请求-响应协议分隔符
     */
    public static final String KCP_SEPARATOR = "|";
}
