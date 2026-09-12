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
@RequiredArgsConstructor
public class OauthProtocolHandler implements ServerHandler {

    /** 认证服务器属性 */
    private final AuthServerProperties authServerProperties;
    /** Logincheck */
    private final LoginCheck loginCheck;

    @Override
    /** 处理 */
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
        var result = loginCheck.doLogin(
                resolveAddress(request, decoder),
                decoder.getString(FIELD_USERNAME),
                decoder.getString(FIELD_PASSWORD),
                decoder.getString(FIELD_AUTH_TYPE),
                decoder.getBodyData().get(FIELD_EXT));
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
        writeResult(response, decoder, resolveTokenResolver().resolve(
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
        var resolver = resolveTokenResolver();
        var token = resolveToken(request, decoder);
        var refreshToken = decoder.getString(FIELD_REFRESH_TOKEN);
        var upgradeType = UpgradeType.getUpgradeType(decoder.getString(FIELD_UPGRADE_TYPE));
        writeResult(response, decoder, switch (upgradeType) {
            case TIMESTAMP -> resolver.upgradeForTimestamp(token, refreshToken);
            case VERSION -> resolver.upgradeForVersion(token, refreshToken);
            case REFRESH -> resolver.upgradeForRefresh(token, refreshToken);
        });
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
        var resolver = resolveTokenResolver();
        var uid = decoder.getString(FIELD_UID);
        var token = resolveToken(request, decoder);
        if (StringUtils.isNotBlank(uid)) {
            var type = StringUtils.isNotBlank(decoder.getString(FIELD_LOGOUT_TYPE))
                    ? LogoutType.valueOf(decoder.getString(FIELD_LOGOUT_TYPE)) : LogoutType.LOGOUT;
            resolver.logout(uid, type);
        } else if (StringUtils.isNotBlank(token)) {
            resolver.logout(token);
        }
        writeResult(response, decoder, ReturnResult.ok("登出成功"));
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
        var resolver = resolveTokenResolver();
        var source = resolver.resolve(null, decoder.getString(FIELD_SOURCE_TOKEN));
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
    * 解析客户端地址(优先请求头 x-OAuth-参数-地址)
    * @param request 请求
    * @param decoder 解码器
    * @return resolve地址的结果
     */
    private String resolveAddress(ServerRequest request, OauthProtocolDecoder decoder) {
        var address = header(request.getHeaders(), HEADER_ADDRESS);
        return decoder.resolveClientAddress(address != null ? address : request.getRemoteAddress());
    }

    /**
    * 解析 令牌解析器(与 login检查 一致)
    * @return resolve令牌解析器的结果
     */
    private TokenResolver resolveTokenResolver() {
        return loginCheck.resolveTokenResolver(authServerProperties.getTokenManagement());
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
