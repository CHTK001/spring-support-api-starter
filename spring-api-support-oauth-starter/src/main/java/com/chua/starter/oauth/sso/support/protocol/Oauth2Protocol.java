package com.chua.starter.oauth.sso.support.protocol;

import com.chua.common.support.annotations.Extension;
import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.oauth.client.support.user.LoginResult;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.condition.OnBeanCondition;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import com.chua.starter.oauth.sso.support.token.TokenResolver;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * OAuth 2.0 标准协议服务端实现
 * <p>
 * 提供符合 RFC 6749 规范的 OAuth 2.0 认证服务端点，包括：
 * - Token端点：支持 client_credentials、password、refresh_token 授权模式
 * - Revoke端点：撤销令牌 (RFC 7009)
 * - Introspect端点：令牌内省 (RFC 7662)
 * - UserInfo端点：获取用户信息
 * - 服务发现端点：OAuth服务器元数据
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2025-12-15
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc6749">RFC 6749 - OAuth 2.0</a>
 */
@Controller
@Extension("oauth2")
@Slf4j
@Conditional(OnBeanCondition.class)
@RequestMapping("/oauth2")
@RequiredArgsConstructor
@Tag(name = "OAuth 2.0认证服务", description = "提供符合RFC 6749规范的OAuth 2.0认证服务接口")
public class Oauth2Protocol implements Protocol, InitializingBean {

    private final AuthServerProperties authServerProperties;
    private final LoginCheck loginCheck;
    private final ApplicationContext applicationContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("OAuth 2.0 协议服务已启动");
    }

    /**
     * OAuth 2.0 Token端点
     * <p>
     * 支持的授权类型（grant_type）：
     * - client_credentials: 客户端凭据模式
     * - password: 资源所有者密码凭据模式
     * - refresh_token: 刷新令牌模式
     * </p>
     */
    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> token(
            @RequestParam("grant_type") String grantType,
            @RequestParam(value = "client_id", required = false) String clientId,
            @RequestParam(value = "client_secret", required = false) String clientSecret,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "refresh_token", required = false) String refreshToken,
            @RequestParam(value = "scope", required = false) String scope,
            HttpServletRequest request,
            HttpServletResponse response) {

        log.info("OAuth2 Token请求 - grant_type: {}, client_id: {}", grantType, clientId);

        // 从Authorization头解析Basic认证
        if (StringUtils.isBlank(clientId) || StringUtils.isBlank(clientSecret)) {
            String[] credentials = extractBasicCredentials(request);
            if (credentials != null) {
                clientId = credentials[0];
                clientSecret = credentials[1];
            }
        }

        // 验证客户端凭据
        if (!validateClientCredentials(clientId, clientSecret)) {
            response.setStatus(401);
            return buildErrorResponse("invalid_client", "客户端认证失败");
        }

        // 根据grant_type处理不同的授权模式
        return switch (grantType.toLowerCase()) {
            case "client_credentials" -> handleClientCredentials(clientId, scope, response);
            case "password" -> handlePasswordGrant(username, password, scope, request, response);
            case "refresh_token" -> handleRefreshToken(refreshToken, scope, response);
            default -> {
                response.setStatus(400);
                yield buildErrorResponse("unsupported_grant_type", "不支持的授权类型: " + grantType);
            }
        };
    }

    /**
     * 处理客户端凭据模式
     */
    private Map<String, Object> handleClientCredentials(String clientId, String scope, HttpServletResponse response) {
        log.debug("处理client_credentials授权 - clientId: {}", clientId);

        TokenResolver tokenResolver = getTokenResolver();
        if (tokenResolver == null) {
            response.setStatus(500);
            return buildErrorResponse("server_error", "Token管理器不可用");
        }

        // 为客户端创建Token
        UserResult userResult = new UserResult();
        userResult.setUserId("client:" + clientId);
        userResult.setUsername(clientId);
        userResult.setUid(clientId);
        userResult.setLoginType("CLIENT_CREDENTIALS");

        Map<String, Object> ext = new HashMap<>();
        ext.put("scope", scope);
        ext.put("clientId", clientId);
        ext.put("loginTime", System.currentTimeMillis());
        userResult.setExt(ext);

        ReturnResult<LoginResult> result = tokenResolver.createToken(null, userResult, "CLIENT_CREDENTIALS");
        if (!result.isOk()) {
            response.setStatus(500);
            return buildErrorResponse("server_error", result.getMsg());
        }

        return buildTokenResponse(result.getData(), scope);
    }

    /**
     * 处理资源所有者密码凭据模式
     */
    private Map<String, Object> handlePasswordGrant(String username, String password,
                                                    String scope, HttpServletRequest request,
                                                    HttpServletResponse response) {
        log.debug("处理password授权 - username: {}", username);

        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            response.setStatus(400);
            return buildErrorResponse("invalid_request", "用户名和密码不能为空");
        }

        String clientAddress = request.getRemoteAddr();
        Map<String, String> ext = new HashMap<>();
        ext.put("address", clientAddress);
        ext.put("loginType", "PASSWORD");

        // 使用LoginCheck进行登录验证
        ReturnResult<LoginResult> result = loginCheck.doLogin(clientAddress, username, password, "PASSWORD", ext);

        if (!result.isOk()) {
            response.setStatus(400);
            return buildErrorResponse("invalid_grant", result.getMsg());
        }

        return buildTokenResponse(result.getData(), scope);
    }

    /**
     * 处理刷新令牌模式
     */
    private Map<String, Object> handleRefreshToken(String refreshToken, String scope, HttpServletResponse response) {
        log.debug("处理refresh_token授权");

        if (StringUtils.isBlank(refreshToken)) {
            response.setStatus(400);
            return buildErrorResponse("invalid_request", "refresh_token不能为空");
        }

        TokenResolver tokenResolver = getTokenResolver();
        if (tokenResolver == null) {
            response.setStatus(500);
            return buildErrorResponse("server_error", "Token管理器不可用");
        }

        ReturnResult<UserResume> result = tokenResolver.upgradeForRefresh(null, refreshToken);
        if (!result.isOk()) {
            response.setStatus(400);
            return buildErrorResponse("invalid_grant", "refresh_token无效或已过期");
        }

        UserResume userResume = result.getData();

        Map<String, Object> tokenResponse = new LinkedHashMap<>();
        // UserResult extends UserResume，包含token字段
        if (userResume instanceof UserResult userResult) {
            tokenResponse.put("access_token", userResult.getToken());
            tokenResponse.put("token_type", "Bearer");
            tokenResponse.put("expires_in", userResume.getExpireTime());
            if (userResult.getRefreshToken() != null) {
                tokenResponse.put("refresh_token", userResult.getRefreshToken());
            }
        } else {
            response.setStatus(500);
            return buildErrorResponse("server_error", "Token刷新失败");
        }
        if (StringUtils.isNotBlank(scope)) {
            tokenResponse.put("scope", scope);
        }

        return tokenResponse;
    }

    /**
     * 令牌撤销端点 (RFC 7009)
     */
    @PostMapping(value = "/revoke", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public void revoke(
            @RequestParam("token") String token,
            @RequestParam(value = "token_type_hint", required = false) String tokenTypeHint,
            HttpServletRequest request,
            HttpServletResponse response) {

        log.info("OAuth2 Revoke请求 - tokenTypeHint: {}", tokenTypeHint);

        // 验证客户端凭据
        String[] credentials = extractBasicCredentials(request);
        if (credentials == null || !validateClientCredentials(credentials[0], credentials[1])) {
            response.setStatus(401);
            return;
        }

        TokenResolver tokenResolver = getTokenResolver();
        if (tokenResolver != null) {
            tokenResolver.logout(token);
        }

        // RFC 7009: 无论成功与否都返回200
        response.setStatus(200);
    }

    /**
     * 令牌内省端点 (RFC 7662)
     */
    @PostMapping(value = "/introspect", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> introspect(
            @RequestParam("token") String token,
            @RequestParam(value = "token_type_hint", required = false) String tokenTypeHint,
            HttpServletRequest request,
            HttpServletResponse response) {

        log.debug("OAuth2 Introspect请求");

        // 验证客户端凭据
        String[] credentials = extractBasicCredentials(request);
        if (credentials == null || !validateClientCredentials(credentials[0], credentials[1])) {
            response.setStatus(401);
            return Map.of("active", false);
        }

        TokenResolver tokenResolver = getTokenResolver();
        if (tokenResolver == null) {
            return Map.of("active", false);
        }

        ReturnResult<UserResume> result = tokenResolver.resolve(null, token);
        if (!result.isOk() || result.getData() == null) {
            return Map.of("active", false);
        }

        UserResume userResume = result.getData();

        Map<String, Object> introspectResponse = new LinkedHashMap<>();
        introspectResponse.put("active", true);
        introspectResponse.put("sub", userResume.getUserId());
        introspectResponse.put("username", userResume.getUsername());
        introspectResponse.put("token_type", "Bearer");
        if (userResume.getExpireTime() != null) {
            introspectResponse.put("exp", System.currentTimeMillis() / 1000 + userResume.getExpireTime());
        }
        introspectResponse.put("iat", System.currentTimeMillis() / 1000);

        return introspectResponse;
    }

    /**
     * 用户信息端点
     */
    @GetMapping(value = "/userinfo", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> userinfo(HttpServletRequest request, HttpServletResponse response) {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.isBlank(authHeader) || !authHeader.startsWith("Bearer ")) {
            response.setStatus(401);
            return Map.of("error", "invalid_token", "error_description", "缺少或无效的访问令牌");
        }

        String token = authHeader.substring(7);

        TokenResolver tokenResolver = getTokenResolver();
        if (tokenResolver == null) {
            response.setStatus(500);
            return Map.of("error", "server_error");
        }

        ReturnResult<UserResume> result = tokenResolver.resolve(null, token);
        if (!result.isOk() || result.getData() == null) {
            response.setStatus(401);
            return Map.of("error", "invalid_token", "error_description", "令牌无效或已过期");
        }

        UserResume userResume = result.getData();

        Map<String, Object> userInfo = new LinkedHashMap<>();
        userInfo.put("sub", userResume.getUserId());
        userInfo.put("name", userResume.getNickName());
        userInfo.put("preferred_username", userResume.getUsername());

        if (userResume.getExt() != null) {
            Map<String, Object> ext = userResume.getExt();
            if (ext.containsKey("email")) {
                userInfo.put("email", ext.get("email"));
            }
            if (ext.containsKey("phone")) {
                userInfo.put("phone_number", ext.get("phone"));
            }
        }

        return userInfo;
    }

    /**
     * OAuth 2.0 服务发现端点 (RFC 8414)
     */
    @GetMapping(value = "/.well-known/oauth-authorization-server", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> authorizationServerMetadata(HttpServletRequest request) {
        String baseUrl = getBaseUrl(request);

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("issuer", baseUrl);
        metadata.put("token_endpoint", baseUrl + "/oauth2/token");
        metadata.put("revocation_endpoint", baseUrl + "/oauth2/revoke");
        metadata.put("introspection_endpoint", baseUrl + "/oauth2/introspect");
        metadata.put("userinfo_endpoint", baseUrl + "/oauth2/userinfo");

        metadata.put("grant_types_supported", Arrays.asList(
                "client_credentials",
                "password",
                "refresh_token"
        ));

        metadata.put("token_endpoint_auth_methods_supported", Arrays.asList(
                "client_secret_basic",
                "client_secret_post"
        ));

        metadata.put("response_types_supported", Collections.singletonList("token"));
        metadata.put("scopes_supported", Arrays.asList("read", "write", "admin"));

        return metadata;
    }

    // ==================== 辅助方法 ====================

    private TokenResolver getTokenResolver() {
        return ServiceProvider.of(TokenResolver.class)
                .getExtension(authServerProperties.getTokenManagement());
    }

    private boolean validateClientCredentials(String clientId, String clientSecret) {
        return StringUtils.isNotBlank(clientId) && StringUtils.isNotBlank(clientSecret);
    }

    private String[] extractBasicCredentials(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.isBlank(authHeader) || !authHeader.startsWith("Basic ")) {
            return null;
        }

        try {
            String base64Credentials = authHeader.substring(6);
            byte[] decodedBytes = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(decodedBytes, StandardCharsets.UTF_8);
            String[] parts = credentials.split(":", 2);
            if (parts.length == 2) {
                return parts;
            }
        } catch (Exception e) {
            log.debug("解析Basic认证头失败", e);
        }
        return null;
    }

    private Map<String, Object> buildTokenResponse(LoginResult loginResult, String scope) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("access_token", loginResult.getToken());
        response.put("token_type", "Bearer");

        UserResume userResume = loginResult.getUserResume();
        if (userResume != null && userResume.getExpireTime() != null) {
            response.put("expires_in", userResume.getExpireTime());
        } else {
            response.put("expires_in", authServerProperties.getExpire());
        }

        if (loginResult.getRefreshToken() != null) {
            response.put("refresh_token", loginResult.getRefreshToken());
        }

        if (StringUtils.isNotBlank(scope)) {
            response.put("scope", scope);
        }

        return response;
    }

    private Map<String, Object> buildErrorResponse(String error, String description) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("error", error);
        response.put("error_description", description);
        return response;
    }

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);

        if (("http".equals(scheme) && serverPort != 80) ||
                ("https".equals(scheme) && serverPort != 443)) {
            url.append(":").append(serverPort);
        }

        url.append(contextPath);
        return url.toString();
    }
}
