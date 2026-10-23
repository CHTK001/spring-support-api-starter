package com.chua.starter.oauth.sso.support.protocol;

import com.chua.common.support.lang.code.ReturnCode;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.lang.json.Json;
import com.chua.common.support.network.http.HttpHeader;
import com.chua.common.support.network.server.handler.ServerHandler;
import com.chua.common.support.network.server.request.ServerRequest;
import com.chua.common.support.network.server.response.ServerResponse;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.oauth.client.support.enums.LogoutType;
import com.chua.starter.oauth.client.support.enums.UpgradeType;
import com.chua.starter.oauth.client.support.user.LoginResult;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.core.AuthCoreService;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import com.chua.starter.oauth.sso.support.token.TokenResolver;
import com.chua.starter.oauth.sso.support.trace.ServerTraceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.client.support.user.UserResume;
import org.springframework.beans.BeanUtils;

import java.util.Map;

import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.AUTH_SCHEME_BEARER;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.CONTENT_TYPE_JSON;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.FIELD_AUTH_TYPE;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.FIELD_COOKIE;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.FIELD_DATA;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.FIELD_EXT;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.FIELD_LOGOUT_TYPE;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.FIELD_PASSWORD;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.FIELD_REFRESH_TOKEN;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.FIELD_SOURCE_TOKEN;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.FIELD_TOKEN;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.FIELD_UID;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.FIELD_UPGRADE_TYPE;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.FIELD_USERNAME;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.HEADER_ACCESS_KEY;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.HEADER_ADDRESS;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.HEADER_AUTHORIZATION;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.HEADER_ENCODE;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.HEADER_RESPONSE_SERIAL;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.HEADER_SERIAL;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.HEADER_SIGN;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.HEADER_UUID;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.OAUTH_PREFIX;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.PATH_LOGIN;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.PATH_LOGOUT;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.PATH_OAUTH;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.PATH_TEMPORARY_TOKEN;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.PATH_UPGRADE;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.PATH_VERIFY;

/**
 * OAuth 认证协议处理器
 *
 * <p>实现 common-starter 的 {@link ServerHandler},承载 OAuth 认证协议入口
 * (通过 {@code network.Server} 以 HTTP/KCP/WS/UDP 等多协议暴露)。把协议无关的
 * {@link ServerRequest} 解码后路由到下层认证逻辑(LoginCheck/TokenResolver),
 * 下层业务逻辑完全不变。</p>
 *
 * <p>支持路径:</p>
 * <ul>
 *     <li>/login           - 登录</li>
 *     <li>/verify /oauth   - 令牌鉴权</li>
 *     <li>/upgrade         - 令牌升级</li>
 *     <li>/logout          - 登出</li>
 *     <li>/temporary-token - 临时令牌</li>
 * </ul>
 *
 * @since 4.0.0.42
 * @author CH
 */
@Slf4j
public class OauthProtocolHandler implements ServerHandler {

    /**
     * 代入目标的 ext 键：客户端把已标记的目标身份放在该键下。
     *
     * <p>与客户端 {@code AuthClientExecute} 写入的键名必须一致，
     * 改名需两端同步，否则代入会静默退化为「换自己」。</p>
     */
    private static final String EXT_IMPERSONATE = "impersonate";

    /**
     * 认证服务器属性
    */
    private final AuthServerProperties authServerProperties;
    /**
     * Logincheck
    */
    private final LoginCheck loginCheck;

    /**
     * 是否强制校验请求签名。
     *
     * <p>HTTP 协议默认开启（客户端 {@code HttpProtocol} 会给每个请求带
     * {@code x-oauth-sign}）；TCP/KCP/RSocket 目前没有承载签名头，默认关闭，
     * 由监听地址（默认回环）与网络 ACL 兜底。</p>
     */
    private final boolean requireSign;

    /**
     * 认证核心服务（登录/升级/登出统一入口）
     */
    private final AuthCoreService authCoreService;

    /**
     * 创建 OAuth 协议处理器。
     *
     * @param authServerProperties 认证服务端属性
     * @param loginCheck           登录检查
     * @param authCoreService      认证核心服务
     * @param requireSign          是否强制校验请求签名
     */
    public OauthProtocolHandler(AuthServerProperties authServerProperties,
                                LoginCheck loginCheck,
                                AuthCoreService authCoreService,
                                boolean requireSign) {
        this.authServerProperties = authServerProperties;
        this.loginCheck = loginCheck;
        this.authCoreService = authCoreService;
        this.requireSign = requireSign;
    }

    @Override
    /**
     * 处理
    */
    public void handle(ServerRequest request, ServerResponse response) throws Exception {
        var path = normalize(request.getPath());
        try {
            if (path.endsWith(PATH_LOGIN)) {
                handleLogin(request, response);
            }
            else if (path.endsWith(PATH_VERIFY) || path.endsWith(PATH_OAUTH)) {
                handleVerify(request, response);
            }
            else if (path.endsWith(PATH_UPGRADE)) {
                handleUpgrade(request, response);
            }
            else if (path.endsWith(PATH_LOGOUT)) {
                handleLogout(request, response);
            }
            else if (path.endsWith(PATH_TEMPORARY_TOKEN)) {
                handleTemporaryToken(request, response);
            }
            else {
                writeError(response, "不支持的认证路径: " + path);
            }
        } catch (Exception e) {
            log.error("[OauthProtocolHandler] 处理失败: path={}, error={}", path, e.getMessage(), e);
            writeError(response, "处理异常: " + e.getMessage());
        } finally {
            ServerTraceContext.clear();
        }
    }

    /**
     * 归一化认证路径(兼容 客户端 地址 含 /OAuth 前缀的调用)
     * @param path 路径
     * @return normalize的结果
     */
    private String normalize(String path) {
        if (path != null && path.startsWith(OAUTH_PREFIX + "/")) {
            return path.substring(OAUTH_PREFIX.length());
        }
        return path;
    }

    // ==================== 登录 ====================

    /**
     * 处理Login
     *
     * @param request 请求
     * @param response 响应
     */
    private void handleLogin(ServerRequest request, ServerResponse response) {
        var decoder = decode(request);
        if (!decoder.isSuccessful()) {
            writeError(response, "解析失败!");
            return;
        }
        if (rejectIfSignInvalid(PATH_LOGIN, decoder, response)) {
            return;
        }
        var result = authCoreService.login(
                resolveAddress(request),
                decoder.getString(FIELD_USERNAME),
                decoder.getString(FIELD_PASSWORD),
                decoder.getString(FIELD_AUTH_TYPE),
                decoder.getBodyData().get(FIELD_EXT));
        if (!ReturnCode.OK.getCode().equals(result.getCode())) {
            writeError(response, result.getMsg());
            return;
        }
        var loginResult = result.getData();
        // 写 Cookie(协议响应头)：HttpOnly 防脚本读取，SameSite=Lax 降低跨站携带风险，
        // Secure 由配置控制（内网 HTTP 下强制 Secure 会让浏览器直接丢弃 Cookie）
        if (loginResult != null && loginResult.getToken() != null) {
            StringBuilder cookie = new StringBuilder(authServerProperties.getCookieName())
                    .append('=').append(loginResult.getToken())
                    .append("; Path=/; HttpOnly; SameSite=Lax");
            if (authServerProperties.isCookieSecure()) {
                cookie.append("; Secure");
            }
            response.setHeader("Set-Cookie", cookie.toString());
        }
        writeResult(response, decoder, result);
    }

    // ==================== 鉴权 ====================

    /**
     * 处理验证
     *
     * @param request 请求
     * @param response 响应
     */
    private void handleVerify(ServerRequest request, ServerResponse response) {
        var decoder = decode(request);
        if (!decoder.isSuccessful()) {
            writeError(response, "解析失败!");
            return;
        }
        writeResult(response, decoder, authCoreService.tokenResolver().resolve(
                decoder.getString(FIELD_COOKIE), resolveToken(request, decoder)));
    }

    // ==================== 升级 ====================

    /**
     * 处理Upgrade
     *
     * @param request 请求
     * @param response 响应
     */
    private void handleUpgrade(ServerRequest request, ServerResponse response) {
        var decoder = decode(request);
        if (!decoder.isSuccessful()) {
            writeError(response, "解析失败!");
            return;
        }
        if (rejectIfSignInvalid(PATH_UPGRADE, decoder, response)) {
            return;
        }
        var token = resolveToken(request, decoder);
        var refreshToken = decoder.getString(FIELD_REFRESH_TOKEN);
        writeResult(response, decoder, authCoreService.upgrade(
                decoder.getString(FIELD_UPGRADE_TYPE), token, refreshToken));
    }

    // ==================== 登出 ====================

    /**
     * 处理Logout
     *
     * @param request 请求
     * @param response 响应
     */
    private void handleLogout(ServerRequest request, ServerResponse response) {
        var decoder = decode(request);
        if (!decoder.isSuccessful()) {
            writeError(response, "解析失败!");
            return;
        }
        if (rejectIfSignInvalid(PATH_LOGOUT, decoder, response)) {
            return;
        }
        var uid = decoder.getString(FIELD_UID);
        var token = resolveToken(request, decoder);
        LogoutType type = LogoutType.LOGOUT;
        String rawType = decoder.getString(FIELD_LOGOUT_TYPE);
        if (StringUtils.isNotBlank(rawType)) {
            try {
                type = LogoutType.valueOf(rawType.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("[OauthProtocolHandler] 非法登出类型，按 LOGOUT 处理: {}", rawType);
            }
        }
        writeResult(response, decoder, authCoreService.logout(uid, type, token));
    }

    // ==================== 临时令牌 ====================

    /**
     * 处理temporary令牌
     *
     * @param request 请求
     * @param response 响应
     */
    private void handleTemporaryToken(ServerRequest request, ServerResponse response) {
        var decoder = decode(request);
        if (!decoder.isSuccessful()) {
            writeError(response, "解析失败!");
            return;
        }
        if (rejectIfSignInvalid(PATH_TEMPORARY_TOKEN, decoder, response)) {
            return;
        }
        var resolver = authCoreService.tokenResolver();
        var source = resolver.resolve(null, decoder.getString(FIELD_SOURCE_TOKEN));
        if (!ReturnCode.OK.getCode().equals(source.getCode())) {
            writeResult(response, decoder, source);
            return;
        }
        // 代入：ext 里带目标身份时按目标身份签发，否则沿用源用户（「换自己」的旧语义）。
        // 客户端已校验过调用者是管理员，这里再加一道「不得提升为管理员」的护栏——
        // 客户端一旦被绕过，提权就无法从服务端拦下。
        UserResume effective = resolveImpersonateTarget(source.getData(), decoder);
        if (effective == null) {
            writeError(response, "代入目标非法: 不得代入为管理员身份");
            return;
        }
        // 临时令牌:基于目标身份创建短期令牌
        // 需转成 UserResult：createToken 的形参是子类（携带 token 等字段）
        var userResult = new UserResult();
        BeanUtils.copyProperties(effective, userResult);
        writeResult(response, decoder, resolver.createToken(
                resolveAddress(request), userResult, "TEMPORARY"));
    }

    /**
     * 解析代入目标身份。
     *
     * <p>ext 里带 {@value #EXT_IMPERSONATE} 时用它作为目标身份，否则返回源用户
     * （保持「用 refreshToken/accessToken 换自己短期令牌」的既有语义不变）。</p>
     *
     * <p><b>为什么要把目标身份交给客户端决定</b>：代入是业务语义
     * （「管理员要看某个学生看到什么」），服务端不认识学生表也不应认识。
     * 客户端把已构造好的目标身份连同 {@code TemporaryToken} 标记一起送上来，
     * 服务端只做两件事——校验来源凭据有效、拒绝提权——然后签发。
     * 这与 static 协议下客户端本地签发的信任模型一致，且多了一道服务端护栏。</p>
     *
     * @param source  源令牌对应的用户
     * @param decoder 已解码的请求
     * @return 目标身份；入参非法或试图提权为管理员时返回 {@code null}
     */
    private UserResume resolveImpersonateTarget(UserResume source, OauthProtocolDecoder decoder) {
        Object ext = decoder.getBodyData().get(FIELD_EXT);
        if (!(ext instanceof Map<?, ?> extMap)) {
            return source;
        }
        Object raw = extMap.get(EXT_IMPERSONATE);
        if (raw == null) {
            return source;
        }
        UserResult target;
        try {
            target = Json.fromJson(String.valueOf(raw), UserResult.class);
        } catch (Exception e) {
            log.warn("代入目标解析失败: {}", e.getMessage());
            return null;
        }
        if (target == null || StringUtils.isBlank(target.getUserId())) {
            log.warn("代入目标为空或缺少 userId");
            return null;
        }
        if (AuthConstant.isAdmin(target.getRoles())) {
            log.warn("拒绝代入为管理员身份: userId={}", target.getUserId());
            return null;
        }
        return target;
    }

    // ==================== 工具方法 ====================

    /**
     * 强制校验请求签名。
     *
     * <p>MVC 入口的 {@code BaseAuthInformation} 一直有这层校验，协议服务器此前漏掉了：
     * {@code OauthProtocolDecoder#isSignValid()} 定义了却从没被调用，等于
     * {@code plugin.oauth.server.sign-verify} 对协议服务器完全无效。</p>
     *
     * <p>{@code /verify} 刻意不校验：令牌本身就是凭据，且调试时常用 curl 直接验 token；
     * {@code /login}、{@code /upgrade}、{@code /logout}、{@code /temporary-token}
     * 会签发/销毁凭据，必须带签名。</p>
     *
     * @param path     当前路径，仅用于日志
     * @param decoder  已解码的请求
     * @param response 响应
     * @return 已拒绝返回 {@code true}
     */
    private boolean rejectIfSignInvalid(String path, OauthProtocolDecoder decoder, ServerResponse response) {
        if (!requireSign || !authServerProperties.isSignVerify()) {
            return false;
        }
        if (decoder == null || !decoder.isSuccessful()) {
            return false;
        }
        if (decoder.isSignValid()) {
            return false;
        }
        log.warn("[OauthProtocolHandler] 签名校验失败，拒绝请求: path={}", path);
        writeError(response, "签名校验失败");
        return true;
    }

    /**
     * 解码报文体(取外层 数据 字段,再解析/解密认证参数)
     * @param request 请求
     * @return decode的结果
     */
    private OauthProtocolDecoder decode(ServerRequest request) {
        var headers = request.getHeaders();
        var requestBody = request.getBodyString();
        // 协议请求体格式: {"data":"<认证参数JSON/密文>"}
        var wrapper = requestBody != null && !requestBody.isBlank()
                ? com.chua.common.support.lang.json.Json.getJsonObject(requestBody) : null;
        var data = wrapper != null && wrapper.get(FIELD_DATA) != null
                ? String.valueOf(wrapper.get(FIELD_DATA)) : requestBody;
        return new OauthProtocolDecoder(
                authServerProperties,
                data,
                header(headers, HEADER_UUID),
                header(headers, HEADER_SERIAL),
                header(headers, HEADER_SIGN),
                header(headers, HEADER_ACCESS_KEY),
                header(headers, HEADER_ENCODE));
    }

    /**
     * 大小写不敏感读取请求头(通用 HTTP客户端 可能首字母大写,如 X-OAuth-uuid)
     * @param headers 头部
     * @param name 名称
     * @return 头部的结果
     */
    private String header(HttpHeader headers, String name) {
        if (headers == null) {
            return null;
        }
        var value = headers.get(name);
        if (value != null) {
            return value;
        }
        return headers.toMap().entrySet().stream()
                .filter(e -> e.getKey() != null && e.getKey().equalsIgnoreCase(name))
                .map(Map.Entry::getValue)
                .findFirst().orElse(null);
    }

    /**
     * 从请求体/请求头解析 令牌
     * @param request 请求
     * @param decoder 解码器
     * @return resolve令牌的结果
     */
    private String resolveToken(ServerRequest request, OauthProtocolDecoder decoder) {
        var token = decoder.getString(FIELD_TOKEN);
        if (StringUtils.isNotBlank(token)) {
            return token;
        }
        var authorization = header(request.getHeaders(), HEADER_AUTHORIZATION);
        return authorization != null && authorization.startsWith(AUTH_SCHEME_BEARER)
                ? authorization.substring(AUTH_SCHEME_BEARER.length()) : null;
    }

    /**
     * 解析可信客户端地址。
     *
     * <p>只采信传输层地址；只有当传输层地址是回环或配置在
     * {@code plugin.oauth.server.trusted-proxies} 中时，才采用
     * {@code X-Forwarded-For} 的最左端地址。请求体里的
     * {@code x-oauth-param-address} 由调用方任意填写，不参与安全判定。</p>
     *
     * @param request 请求
     * @return 可信客户端地址；消息型协议（无 IP 概念）返回 {@code null}
     */
    private String resolveAddress(ServerRequest request) {
        return ClientAddressResolver.resolve(
                header(request.getHeaders(), ClientAddressResolver.FORWARDED_FOR),
                request.getRemoteAddress(),
                authServerProperties.getTrustedProxies());
    }

    /**
     * 写入结果(加密/明文由请求 x-OAuth-encode 决定)
     * <p>与原 LoginAuthorization 一致:外层 ReturnResult JSON 明文,data 字段加密。</p>
     * @param response 响应
     * @param decoder 解码器
     * @param result 结果
     */
    private void writeResult(ServerResponse response, OauthProtocolDecoder decoder, ReturnResult<?> result) {
        response.setContentType(CONTENT_TYPE_JSON);
        String body;
        if (decoder != null && decoder.isEncode() && result.getData() != null) {
 // 加密 数据 字段,外层 返回结果 保持明文
            var newKey = com.chua.common.support.utils.IdUtils.createSimpleUuid();
            response.setHeader(HEADER_RESPONSE_SERIAL, newKey);
            var encrypted = com.chua.starter.common.support.algorithm.crypto.Codec
                    .build(decoder.encryption(), newKey)
                    .encodeHex(Json.toJson(result.getData()));
            var wrapper = new com.chua.common.support.lang.json.JsonObject();
            wrapper.put("code", result.getCode());
            wrapper.put("msg", result.getMsg());
            wrapper.put("data", encrypted);
            body = wrapper.toJSONString();
        } else {
            body = Json.toJson(result);
        }
        response.setBody(body);
        response.end();
    }

    /**
     * 写入失败结果
     * @param response 响应
     * @param message 消息
     */
    private void writeError(ServerResponse response, String message) {
        writeResult(response, null, ReturnResult.error(message));
    }
}
