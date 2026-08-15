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
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import com.chua.starter.oauth.sso.support.token.TokenResolver;
import com.chua.starter.oauth.sso.support.trace.ServerTraceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

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
 * @author CH
 * @since 4.0.0.42
 */
@Slf4j
@RequiredArgsConstructor
public class OauthProtocolHandler implements ServerHandler {

    private final AuthServerProperties authServerProperties;
    private final LoginCheck loginCheck;

    @Override
    public void handle(ServerRequest request, ServerResponse response) throws Exception {
        var path = normalize(request.getPath());
        try {
            if (path.endsWith("/login")) handleLogin(request, response);
            else if (path.endsWith("/verify") || path.endsWith("/oauth")) handleVerify(request, response);
            else if (path.endsWith("/upgrade")) handleUpgrade(request, response);
            else if (path.endsWith("/logout")) handleLogout(request, response);
            else if (path.endsWith("/temporary-token")) handleTemporaryToken(request, response);
            else writeError(response, "不支持的认证路径: " + path);
        } catch (Exception e) {
            log.error("[OauthProtocolHandler] 处理失败: path={}, error={}", path, e.getMessage(), e);
            writeError(response, "处理异常: " + e.getMessage());
        } finally {
            ServerTraceContext.clear();
        }
    }

    /**
     * 归一化认证路径(兼容 client address 含 /oauth 前缀的调用)
     */
    private String normalize(String path) {
        if (path != null && path.startsWith("/oauth/")) {
            return path.substring("/oauth".length());
        }
        return path;
    }

    // ==================== 登录 ====================

    private void handleLogin(ServerRequest request, ServerResponse response) {
        var decoder = decode(request);
        if (!decoder.isSuccessful()) {
            writeError(response, "解析失败!");
            return;
        }
        var result = loginCheck.doLogin(
                resolveAddress(request, decoder),
                decoder.getString("x-oauth-username"),
                decoder.getString("x-oauth-password"),
                decoder.getString("x-oauth-auth-type"),
                decoder.getBodyData().get("x-oauth-ext"));
        if (!ReturnCode.OK.getCode().equals(result.getCode())) {
            writeError(response, result.getMsg());
            return;
        }
        var loginResult = result.getData();
        // 写 Cookie(协议响应头)
        if (loginResult != null && loginResult.getToken() != null) {
            response.setHeader("Set-Cookie", authServerProperties.getCookieName() + "="
                    + loginResult.getToken() + "; Path=/; HttpOnly");
        }
        writeResult(response, decoder, result);
    }

    // ==================== 鉴权 ====================

    private void handleVerify(ServerRequest request, ServerResponse response) {
        var decoder = decode(request);
        if (!decoder.isSuccessful()) {
            writeError(response, "解析失败!");
            return;
        }
        writeResult(response, decoder, resolveTokenResolver().resolve(
                decoder.getString("x-oauth-cookie"), resolveToken(request, decoder)));
    }

    // ==================== 升级 ====================

    private void handleUpgrade(ServerRequest request, ServerResponse response) {
        var decoder = decode(request);
        if (!decoder.isSuccessful()) {
            writeError(response, "解析失败!");
            return;
        }
        var resolver = resolveTokenResolver();
        var token = resolveToken(request, decoder);
        var refreshToken = decoder.getString("x-oauth-refresh-token");
        var upgradeType = decoder.getString("x-oauth-upgrade-type");
        writeResult(response, decoder, switch (upgradeType == null ? "" : upgradeType.toUpperCase()) {
            case "TIMESTAMP" -> resolver.upgradeForTimestamp(token, refreshToken);
            case "VERSION" -> resolver.upgradeForVersion(token, refreshToken);
            default -> resolver.upgradeForRefresh(token, refreshToken);
        });
    }

    // ==================== 登出 ====================

    private void handleLogout(ServerRequest request, ServerResponse response) {
        var decoder = decode(request);
        if (!decoder.isSuccessful()) {
            writeError(response, "解析失败!");
            return;
        }
        var resolver = resolveTokenResolver();
        var uid = decoder.getString("x-oauth-uid");
        var token = resolveToken(request, decoder);
        if (StringUtils.isNotBlank(uid)) {
            var type = StringUtils.isNotBlank(decoder.getString("x-oauth-logout-type"))
                    ? LogoutType.valueOf(decoder.getString("x-oauth-logout-type")) : LogoutType.LOGOUT;
            resolver.logout(uid, type);
        } else if (StringUtils.isNotBlank(token)) {
            resolver.logout(token);
        }
        writeResult(response, decoder, ReturnResult.ok("登出成功"));
    }

    // ==================== 临时令牌 ====================

    private void handleTemporaryToken(ServerRequest request, ServerResponse response) {
        var decoder = decode(request);
        if (!decoder.isSuccessful()) {
            writeError(response, "解析失败!");
            return;
        }
        var resolver = resolveTokenResolver();
        var source = resolver.resolve(null, decoder.getString("x-oauth-source-token"));
        if (!ReturnCode.OK.getCode().equals(source.getCode())) {
            writeResult(response, decoder, source);
            return;
        }
        // 临时令牌:基于源用户创建短期令牌
        var userResult = new com.chua.starter.oauth.client.support.user.UserResult();
        org.springframework.beans.BeanUtils.copyProperties(source.getData(), userResult);
        writeResult(response, decoder, resolver.createToken(
                resolveAddress(request, decoder), userResult, "TEMPORARY"));
    }

    // ==================== 工具方法 ====================

    /**
     * 解码报文体(取外层 data 字段,再解析/解密认证参数)
     */
    private OauthProtocolDecoder decode(ServerRequest request) {
        var headers = request.getHeaders();
        var bodyString = request.getBodyString();
        // 协议请求体格式: {"data":"<认证参数JSON/密文>"}
        var wrapper = bodyString != null && !bodyString.isBlank()
                ? com.chua.common.support.lang.json.Json.getJsonObject(bodyString) : null;
        var data = wrapper != null && wrapper.get("data") != null
                ? String.valueOf(wrapper.get("data")) : bodyString;
        return new OauthProtocolDecoder(
                authServerProperties,
                data,
                header(headers, "x-oauth-uuid"),
                header(headers, "x-oauth-serial"),
                header(headers, "x-oauth-sign"),
                header(headers, "x-oauth-ak"),
                header(headers, "x-oauth-encode"));
    }

    /**
     * 大小写不敏感读取请求头(common HttpClient 可能首字母大写,如 X-oauth-uuid)
     */
    private String header(HttpHeader headers, String name) {
        if (headers == null) return null;
        var value = headers.get(name);
        if (value != null) return value;
        return headers.toMap().entrySet().stream()
                .filter(e -> e.getKey() != null && e.getKey().equalsIgnoreCase(name))
                .map(Map.Entry::getValue)
                .findFirst().orElse(null);
    }

    /**
     * 从请求体/请求头解析 token
     */
    private String resolveToken(ServerRequest request, OauthProtocolDecoder decoder) {
        var token = decoder.getString("x-oauth-token");
        if (StringUtils.isNotBlank(token)) return token;
        var authorization = header(request.getHeaders(), "Authorization");
        return authorization != null && authorization.startsWith("Bearer ")
                ? authorization.substring("Bearer ".length()) : null;
    }

    /**
     * 解析客户端地址(优先请求头 x-oauth-param-address)
     */
    private String resolveAddress(ServerRequest request, OauthProtocolDecoder decoder) {
        var address = header(request.getHeaders(), "x-oauth-param-address");
        return decoder.resolveClientAddress(address != null ? address : request.getRemoteAddress());
    }

    /**
     * 解析 TokenResolver(与 LoginCheck 一致)
     */
    private TokenResolver resolveTokenResolver() {
        return loginCheck.resolveTokenResolver(authServerProperties.getTokenManagement());
    }

    /**
     * 写入结果(加密/明文由请求 x-oauth-encode 决定)
     * <p>与原 LoginAuthorization 一致:外层 ReturnResult JSON 明文,data 字段加密。</p>
     */
    private void writeResult(ServerResponse response, OauthProtocolDecoder decoder, ReturnResult<?> result) {
        response.setContentType("application/json");
        String body;
        if (decoder != null && decoder.isEncode() && result.getData() != null) {
            // 加密 data 字段,外层 ReturnResult 保持明文
            var newKey = com.chua.common.support.utils.IdUtils.createSimpleUuid();
            response.setHeader("x-oauth-response-serial", newKey);
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
     */
    private void writeError(ServerResponse response, String message) {
        writeResult(response, null, ReturnResult.error(message));
    }
}
