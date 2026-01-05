package com.chua.starter.oauth.sso.support.protocol;

import com.chua.common.support.annotations.Extension;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.oauth.client.support.user.LoginResult;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.condition.OnBeanCondition;
import com.chua.starter.oauth.sso.support.core.OAuth2CoreService;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import com.chua.starter.oauth.sso.support.token.TokenResolver;
import com.chua.starter.system.service.SysAppKeyService;
import com.chua.starter.system.service.SysAppIdService;
import com.chua.starter.entity.system.SysAppId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
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
    private final SysAppKeyService sysAppKeyService;
    private final SysAppIdService sysAppIdService;
    private final OAuth2CoreService oAuth2CoreService;

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("OAuth 2.0 协议服务已启动");
    }

    // ==================== 授权端点 (Authorization Endpoint) ====================

    /**
     * OAuth 2.0 授权端点
     * <p>
     * 支持 authorization_code 授权流程，包括 PKCE 扩展。
     * 用户需要先登录，然后调用此端点获取授权码。
     * </p>
     */
    @GetMapping("/authorize")
    @Operation(summary = "授权端点", description = "获取授权码，支持 PKCE")
    public void authorize(
            @Parameter(description = "响应类型") @RequestParam("response_type") String responseType,
            @Parameter(description = "客户端ID") @RequestParam("client_id") String clientId,
            @Parameter(description = "回调地址") @RequestParam("redirect_uri") String redirectUri,
            @Parameter(description = "权限范围") @RequestParam(value = "scope", required = false) String scope,
            @Parameter(description = "状态码") @RequestParam(value = "state", required = false) String state,
            @Parameter(description = "PKCE code_challenge") @RequestParam(value = "code_challenge", required = false) String codeChallenge,
            @Parameter(description = "PKCE 方法") @RequestParam(value = "code_challenge_method", required = false) String codeChallengeMethod,
            @RequestHeader(value = "x-oauth-token", required = false) String token,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        log.info("OAuth2 授权请求: clientId={}, responseType={}, scope={}", clientId, responseType, scope);

        // 验证 response_type
        if (!"code".equals(responseType)) {
            sendAuthorizeError(response, redirectUri, state, "unsupported_response_type", "只支持 response_type=code");
            return;
        }

        // 验证客户端存在（先尝试 AppId，再尝试 AppKey）
        SysAppId appId = sysAppIdService.getByAppIdValue(clientId);
        SysAppKey appKey = null;
        if (appId == null) {
            appKey = sysAppKeyService.getByAppKey(clientId);
            if (appKey == null) {
                sendAuthorizeError(response, redirectUri, state, "invalid_client", "客户端不存在");
                return;
            }
        }

        // 验证 redirect_uri
        if (!oAuth2CoreService.isRedirectUriValid(clientId, redirectUri)) {
            sendAuthorizeError(response, redirectUri, state, "invalid_request", "无效的回调地址");
            return;
        }

        // 验证 grant_type
        if (!oAuth2CoreService.isGrantTypeAllowed(clientId, "authorization_code")) {
            sendAuthorizeError(response, redirectUri, state, "unauthorized_client", "客户端未授权使用 authorization_code 模式");
            return;
        }

        // 验证 scope
        OAuth2CoreService.ScopeValidationResult scopeResult = oAuth2CoreService.validateScope(clientId, scope);
        if (!scopeResult.valid()) {
            sendAuthorizeError(response, redirectUri, state, "invalid_scope", scopeResult.errorMessage());
            return;
        }

        // 验证用户已登录
        TokenResolver tokenResolver = getTokenResolver();
        if (tokenResolver == null || StringUtils.isBlank(token)) {
            sendAuthorizeError(response, redirectUri, state, "access_denied", "用户未登录");
            return;
        }

        ReturnResult<UserResume> userResult = tokenResolver.resolve(null, token);
        if (!userResult.isOk() || userResult.getData() == null) {
            sendAuthorizeError(response, redirectUri, state, "access_denied", "用户未登录或令牌已过期");
            return;
        }

        UserResume user = userResult.getData();

        // 生成授权码
        String code = oAuth2CoreService.generateAuthorizationCode(
                clientId, redirectUri, scope, state,
                codeChallenge, codeChallengeMethod,
                user.getUserId(), user.getUsername());

        // 重定向到回调地址
        String redirectUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("code", code)
                .queryParamIfPresent("state", Optional.ofNullable(state))
                .build().toUriString();

        log.info("OAuth2 授权成功: clientId={}, userId={}", clientId, user.getUserId());
        response.sendRedirect(redirectUrl);
    }

    /**
     * 发送授权错误重定向
     */
    private void sendAuthorizeError(HttpServletResponse response, String redirectUri,
                                    String state, String error, String description) throws Exception {
        if (StringUtils.isBlank(redirectUri)) {
            response.setStatus(400);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"" + error + "\",\"error_description\":\"" + description + "\"}");
            return;
        }

        String redirectUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("error", error)
                .queryParam("error_description", URLEncoder.encode(description, StandardCharsets.UTF_8))
                .queryParamIfPresent("state", Optional.ofNullable(state))
                .build().toUriString();

        response.sendRedirect(redirectUrl);
    }

    // ==================== 令牌端点 (Token Endpoint) ====================

    /**
     * OAuth 2.0 Token端点
     */
    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "令牌端点", description = "获取访问令牌")
    public Map<String, Object> token(
            @Parameter(description = "授权类型") @RequestParam("grant_type") String grantType,
            @Parameter(description = "客户端ID") @RequestParam(value = "client_id", required = false) String clientId,
            @Parameter(description = "客户端密钥") @RequestParam(value = "client_secret", required = false) String clientSecret,
            @Parameter(description = "用户名") @RequestParam(value = "username", required = false) String username,
            @Parameter(description = "密码") @RequestParam(value = "password", required = false) String password,
            @Parameter(description = "刷新令牌") @RequestParam(value = "refresh_token", required = false) String refreshToken,
            @Parameter(description = "权限范围") @RequestParam(value = "scope", required = false) String scope,
            @Parameter(description = "授权码") @RequestParam(value = "code", required = false) String code,
            @Parameter(description = "回调地址") @RequestParam(value = "redirect_uri", required = false) String redirectUri,
            @Parameter(description = "PKCE code_verifier") @RequestParam(value = "code_verifier", required = false) String codeVerifier,
            HttpServletRequest request,
            HttpServletResponse response) {

        log.info("OAuth2 Token请求 - grant_type: {}, client_id: {}", grantType, clientId);

        // 从 Authorization头解析Basic认证
        if (StringUtils.isBlank(clientId) || StringUtils.isBlank(clientSecret)) {
            String[] credentials = extractBasicCredentials(request);
            if (credentials != null) {
                clientId = credentials[0];
                clientSecret = credentials[1];
            }
        }

        // 验证授权类型是否允许
        if (!oAuth2CoreService.isGrantTypeAllowed(clientId, grantType)) {
            response.setStatus(400);
            return buildErrorResponse("unauthorized_client", "客户端未授权使用此授权类型: " + grantType);
        }

        // authorization_code 模式不需要验证 client_secret（如果使用 PKCE）
        if (!"authorization_code".equalsIgnoreCase(grantType)) {
            // 验证客户端凭据
            if (!validateClientCredentials(clientId, clientSecret)) {
                response.setStatus(401);
                return buildErrorResponse("invalid_client", "客户端认证失败");
            }
        }

        // 验证 scope
        if (StringUtils.isNotBlank(scope)) {
            OAuth2CoreService.ScopeValidationResult scopeResult = oAuth2CoreService.validateScope(clientId, scope);
            if (!scopeResult.valid()) {
                response.setStatus(400);
                return buildErrorResponse("invalid_scope", scopeResult.errorMessage());
            }
        }

        // 根据 grant_type处理不同的授权模式
        return switch (grantType.toLowerCase()) {
            case "authorization_code" -> handleAuthorizationCode(code, redirectUri, codeVerifier, clientId, clientSecret, scope, response);
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
     * 处理授权码模式
     */
    private Map<String, Object> handleAuthorizationCode(String code, String redirectUri, String codeVerifier,
                                                        String clientId, String clientSecret, String scope,
                                                        HttpServletResponse response) {
        log.debug("处理authorization_code授权 - clientId: {}", clientId);

        if (StringUtils.isBlank(code)) {
            response.setStatus(400);
            return buildErrorResponse("invalid_request", "缺少授权码");
        }

        if (StringUtils.isBlank(redirectUri)) {
            response.setStatus(400);
            return buildErrorResponse("invalid_request", "缺少回调地址");
        }

        // 验证并消费授权码
        OAuth2CoreService.AuthorizationCodeData codeData = oAuth2CoreService.validateAndConsumeCode(
                code, clientId, redirectUri, codeVerifier);

        if (codeData == null) {
            response.setStatus(400);
            return buildErrorResponse("invalid_grant", "授权码无效或已过期");
        }

        // 如果没有使用 PKCE，需要验证 client_secret
        if (StringUtils.isBlank(codeVerifier) && !validateClientCredentials(clientId, clientSecret)) {
            response.setStatus(401);
            return buildErrorResponse("invalid_client", "客户端认证失败");
        }

        // 为用户创建 Token
        TokenResolver tokenResolver = getTokenResolver();
        if (tokenResolver == null) {
            response.setStatus(500);
            return buildErrorResponse("server_error", "Token管理器不可用");
        }

        UserResult userResult = new UserResult();
        userResult.setUserId(codeData.userId());
        userResult.setUsername(codeData.username());
        userResult.setUid(codeData.userId());
        userResult.setLoginType("AUTHORIZATION_CODE");

        Map<String, Object> ext = new HashMap<>();
        ext.put("scope", codeData.scope());
        ext.put("clientId", clientId);
        ext.put("loginTime", System.currentTimeMillis());
        userResult.setExt(ext);

        ReturnResult<LoginResult> result = tokenResolver.createToken(null, userResult, "AUTHORIZATION_CODE");
        if (!result.isOk()) {
            response.setStatus(500);
            return buildErrorResponse("server_error", result.getMsg());
        }

        return buildTokenResponse(result.getData(), codeData.scope());
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
                "authorization_code",
                "client_credentials",
                "password",
                "refresh_token"
        ));

        metadata.put("authorization_endpoint", baseUrl + "/oauth2/authorize");
        metadata.put("code_challenge_methods_supported", Arrays.asList("S256", "plain"));

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

    /**
     * 验证客户端凭据
     * <p>
     * 先尝试通过 AppId 验证，如果不存在，再尝试通过 AppKey 验证
     * </p>
     *
     * @param clientId     客户端ID (可能是 appId 或 appKey)
     * @param clientSecret 客户端密钥
     * @return 是否验证通过
     */
    private boolean validateClientCredentials(String clientId, String clientSecret) {
        if (StringUtils.isBlank(clientId) || StringUtils.isBlank(clientSecret)) {
            return false;
        }
        
        try {
            // 先尝试通过 AppId 验证
            SysAppId appId = sysAppIdService.getByAppIdValue(clientId);
            if (appId != null) {
                // 验证 AppId 状态和过期时间
                if (appId.getAppIdStatus() != null && appId.getAppIdStatus() != 1) {
                    log.warn("AppId已禁用: clientId={}", clientId);
                    return false;
                }
                if (appId.getAppIdExpireTime() != null) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime > appId.getAppIdExpireTime()) {
                        log.warn("AppId已过期: clientId={}", clientId);
                        return false;
                    }
                }
                // AppId 验证通过，更新使用信息
                sysAppIdService.updateUseInfo(clientId);
                log.debug("OAuth2客户端验证通过（AppId）: clientId={}", clientId);
                return true;
            }
            
            // 如果 AppId 不存在，尝试通过 AppKey 验证
            boolean verified = sysAppKeyService.verify(clientId, clientSecret);
            if (verified) {
                // 验证通过后更新使用信息
                sysAppKeyService.updateUseInfoByAppKey(clientId);
                log.debug("OAuth2客户端验证通过（AppKey）: clientId={}", clientId);
            } else {
                log.warn("OAuth2客户端验证失败: clientId={}", clientId);
            }
            return verified;
        } catch (Exception e) {
            log.error("OAuth2客户端验证异常: clientId={}, error={}", clientId, e.getMessage());
            return false;
        }
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
