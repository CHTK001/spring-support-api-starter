package com.chua.starter.oauth.sso.support.protocol;

import com.chua.common.support.core.annotation.Extension;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.core.spi.ServiceProvider;
import com.chua.common.support.core.utils.StringUtils;
import com.chua.common.support.text.json.Json;
import com.chua.common.support.network.protocol.Protocol;
import com.chua.common.support.network.protocol.ProtocolSetting;
import com.chua.common.support.network.protocol.server.ProtocolServer;
import com.chua.starter.oauth.client.support.user.LoginResult;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.condition.OnBeanCondition;
import com.chua.starter.oauth.sso.support.core.OAuth2CoreService;
import com.chua.starter.oauth.sso.support.information.ProtocolServerAuthInformation;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import com.chua.starter.oauth.sso.support.token.TokenResolver;
import com.chua.starter.system.service.SysAppKeyService;
import com.chua.starter.system.service.SysAppIdService;
import com.chua.starter.entity.system.SysAppId;
import com.chua.starter.entity.system.SysAppKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.chua.common.support.network.protocol.request.ServletRequest;
import com.chua.common.support.network.protocol.request.ServletResponse;
import com.chua.common.support.network.protocol.ProtocolSetting;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Base64;

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
@Component
@Extension("oauth2")
@Slf4j
@Conditional(OnBeanCondition.class)
@RequiredArgsConstructor
@Tag(name = "OAuth 2.0认证服务", description = "提供符合RFC 6749规范的OAuth 2.0认证服务接口")
public class Oauth2Protocol implements Protocol, InitializingBean {

    private final AuthServerProperties authServerProperties;
    private final LoginCheck loginCheck;
    private final ApplicationContext applicationContext;
    private final SysAppKeyService sysAppKeyService;
    private final SysAppIdService sysAppIdService;
    private final OAuth2CoreService oAuth2CoreService;
    
    private ProtocolServer protocolServer;

    @Override
    public void afterPropertiesSet() throws Exception {
        // 检查是否启用 oauth2 协议
        if (!authServerProperties.getProtocols().contains("oauth2")) {
            log.info("[OAuth][OAuth2] OAuth2 协议未启用，跳过 ProtocolServer 启动");
            return;
        }
        
        try {
            // 获取 Protocol 实例
            var protocol = ServiceProvider.of(Protocol.class).getExtension("http");
            if (protocol == null) {
                log.warn("[OAuth][OAuth2] 未找到 HTTP Protocol 实现，跳过 ProtocolServer 启动");
                return;
            }
            
            // 创建 ProtocolSetting
            var protocolSetting = ProtocolSetting.builder()
                    .port(authServerProperties.getArmeriaPort() != null ? 
                            authServerProperties.getArmeriaPort() : 8080)
                    .build();
            
            // 创建并启动 ProtocolServer
            protocolServer = protocol.createServer(protocolSetting);
            registerProtocolServerRoutes(protocolServer);
            protocolServer.start();
            
            log.info("[OAuth][OAuth2] ProtocolServer 启动成功，端口: {}", protocolSetting.getPort());
        } catch (Exception e) {
            log.error("[OAuth][OAuth2] ProtocolServer 启动失败", e);
        }
    }
    
    /**
     * 注册 ProtocolServer 路由
     *
     * @param server ProtocolServer 实例
     */
    private void registerProtocolServerRoutes(ProtocolServer server) {
        // OAuth2 授权端点 (GET)
        server.get("/oauth2/authorize", (request, response) -> {
            try {
                var protocolRequest = new ProtocolServerAuthInformation.ProtocolServerRequest(request);
                var protocolResponse = new ProtocolServerAuthInformation.ProtocolServerResponse(response);
                
                String responseType = protocolRequest.getParameter("response_type");
                String clientId = protocolRequest.getParameter("client_id");
                String redirectUri = protocolRequest.getParameter("redirect_uri");
                String scope = protocolRequest.getParameter("scope");
                String state = protocolRequest.getParameter("state");
                String codeChallenge = protocolRequest.getParameter("code_challenge");
                String codeChallengeMethod = protocolRequest.getParameter("code_challenge_method");
                String token = protocolRequest.getHeader("x-oauth-token");
                
                log.info("[OAuth][OAuth2] 授权请求: clientId={}, responseType={}, scope={}", clientId, responseType, scope);
                
                // 验证 response_type
                if (!"code".equals(responseType)) {
                    sendAuthorizeError(response, redirectUri, state, "unsupported_response_type", "只支持 response_type=code");
                    return;
                }
                
                // 验证客户端存在
                var appId = sysAppIdService.getByAppIdValue(clientId);
                var appKey = appId == null ? sysAppKeyService.getByAppKey(clientId) : null;
                if (appId == null && appKey == null) {
                    sendAuthorizeError(response, redirectUri, state, "invalid_client", "客户端不存在");
                    return;
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
                var scopeResult = oAuth2CoreService.validateScope(clientId, scope);
                if (!scopeResult.valid()) {
                    sendAuthorizeError(response, redirectUri, state, "invalid_scope", scopeResult.errorMessage());
                    return;
                }
                
                // 验证用户已登录
                var tokenResolver = getTokenResolver();
                if (tokenResolver == null || StringUtils.isBlank(token)) {
                    sendAuthorizeError(response, redirectUri, state, "access_denied", "用户未登录");
                    return;
                }
                
                var userResult = tokenResolver.resolve(null, token);
                if (!userResult.isOk() || userResult.getData() == null) {
                    sendAuthorizeError(response, redirectUri, state, "access_denied", "用户未登录或令牌已过期");
                    return;
                }
                
                var user = userResult.getData();
                
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
                
                log.info("[OAuth][OAuth2] 授权成功: clientId={}, userId={}", clientId, user.getUserId());
                protocolResponse.sendRedirect(redirectUrl);
            } catch (Exception e) {
                log.error("[OAuth][OAuth2] 授权处理异常", e);
                try {
                    response.setStatus(500);
                    response.setContentType("application/json;charset=UTF-8");
                    response.setBodyString(Json.toJson(Map.of("error", "server_error", "error_description", e.getMessage())));
                } catch (Exception ex) {
                    log.error("[OAuth][OAuth2] 写入错误响应失败", ex);
                }
            }
        });
        
        // OAuth2 Token 端点 (POST)
        server.post("/oauth2/token", (request, response) -> {
            try {
                var protocolRequest = new ProtocolServerAuthInformation.ProtocolServerRequest(request);
                var protocolResponse = new ProtocolServerAuthInformation.ProtocolServerResponse(response);
                
                String body = extractBody(request);
                Map<String, String> params = parseFormUrlEncoded(body);
                
                String grantType = params.get("grant_type");
                String clientId = params.get("client_id");
                String clientSecret = params.get("client_secret");
                String username = params.get("username");
                String password = params.get("password");
                String refreshToken = params.get("refresh_token");
                String scope = params.get("scope");
                String code = params.get("code");
                String redirectUri = params.get("redirect_uri");
                String codeVerifier = params.get("code_verifier");
                
                log.info("[OAuth][OAuth2] Token请求 - grant_type: {}, client_id: {}", grantType, clientId);
                
                // 从 Authorization头解析Basic认证
                if (StringUtils.isBlank(clientId) || StringUtils.isBlank(clientSecret)) {
                    String[] credentials = extractBasicCredentials(protocolRequest);
                    if (credentials != null) {
                        clientId = credentials[0];
                        clientSecret = credentials[1];
                    }
                }
                
                // 验证授权类型是否允许
                if (!oAuth2CoreService.isGrantTypeAllowed(clientId, grantType)) {
                    protocolResponse.setStatus(400);
                    writeJsonResponse(response, buildErrorResponse("unauthorized_client", "客户端未授权使用此授权类型: " + grantType));
                    return;
                }
                
                // authorization_code 模式不需要验证 client_secret（如果使用 PKCE）
                if (!"authorization_code".equalsIgnoreCase(grantType)) {
                    // 验证客户端凭据
                    if (!validateClientCredentials(clientId, clientSecret)) {
                        protocolResponse.setStatus(401);
                        writeJsonResponse(response, buildErrorResponse("invalid_client", "客户端认证失败"));
                        return;
                    }
                }
                
                // 验证 scope
                if (StringUtils.isNotBlank(scope)) {
                    var scopeResult = oAuth2CoreService.validateScope(clientId, scope);
                    if (!scopeResult.valid()) {
                        protocolResponse.setStatus(400);
                        writeJsonResponse(response, buildErrorResponse("invalid_scope", scopeResult.errorMessage()));
                        return;
                    }
                }
                
                // 根据 grant_type处理不同的授权模式
                Map<String, Object> result = switch (grantType.toLowerCase()) {
                    case "authorization_code" -> handleAuthorizationCode(code, redirectUri, codeVerifier, clientId, clientSecret, scope, protocolResponse);
                    case "client_credentials" -> handleClientCredentials(clientId, scope, protocolResponse);
                    case "password" -> handlePasswordGrant(username, password, scope, protocolRequest, protocolResponse);
                    case "refresh_token" -> handleRefreshToken(refreshToken, scope, protocolResponse);
                    default -> {
                        protocolResponse.setStatus(400);
                        yield buildErrorResponse("unsupported_grant_type", "不支持的授权类型: " + grantType);
                    }
                };
                
                writeJsonResponse(response, result);
            } catch (Exception e) {
                log.error("[OAuth][OAuth2] Token处理异常", e);
                try {
                    response.setStatus(500);
                    response.setContentType("application/json;charset=UTF-8");
                    response.setBodyString(Json.toJson(buildErrorResponse("server_error", e.getMessage())));
                } catch (Exception ex) {
                    log.error("[OAuth][OAuth2] 写入错误响应失败", ex);
                }
            }
        });
        
        // OAuth2 Revoke 端点 (POST)
        server.post("/oauth2/revoke", (request, response) -> {
            try {
                var protocolRequest = new ProtocolServerAuthInformation.ProtocolServerRequest(request);
                var protocolResponse = new ProtocolServerAuthInformation.ProtocolServerResponse(response);
                
                String body = extractBody(request);
                Map<String, String> params = parseFormUrlEncoded(body);
                String token = params.get("token");
                
                log.info("[OAuth][OAuth2] Revoke请求");
                
                // 验证客户端凭据
                String[] credentials = extractBasicCredentials(protocolRequest);
                if (credentials == null || !validateClientCredentials(credentials[0], credentials[1])) {
                    protocolResponse.setStatus(401);
                    return;
                }
                
                var tokenResolver = getTokenResolver();
                if (tokenResolver != null) {
                    tokenResolver.logout(token);
                }
                
                // RFC 7009: 无论成功与否都返回200
                protocolResponse.setStatus(200);
            } catch (Exception e) {
                log.error("[OAuth][OAuth2] Revoke处理异常", e);
            }
        });
        
        // OAuth2 Introspect 端点 (POST)
        server.post("/oauth2/introspect", (request, response) -> {
            try {
                var protocolRequest = new ProtocolServerAuthInformation.ProtocolServerRequest(request);
                var protocolResponse = new ProtocolServerAuthInformation.ProtocolServerResponse(response);
                
                String body = extractBody(request);
                Map<String, String> params = parseFormUrlEncoded(body);
                String token = params.get("token");
                
                log.debug("[OAuth][OAuth2] Introspect请求");
                
                // 验证客户端凭据
                String[] credentials = extractBasicCredentials(protocolRequest);
                if (credentials == null || !validateClientCredentials(credentials[0], credentials[1])) {
                    protocolResponse.setStatus(401);
                    writeJsonResponse(response, Map.of("active", false));
                    return;
                }
                
                var tokenResolver = getTokenResolver();
                if (tokenResolver == null) {
                    writeJsonResponse(response, Map.of("active", false));
                    return;
                }
                
                var result = tokenResolver.resolve(null, token);
                if (!result.isOk() || result.getData() == null) {
                    writeJsonResponse(response, Map.of("active", false));
                    return;
                }
                
                var userResume = result.getData();
                Map<String, Object> introspectResponse = new LinkedHashMap<>();
                introspectResponse.put("active", true);
                introspectResponse.put("sub", userResume.getUserId());
                introspectResponse.put("username", userResume.getUsername());
                introspectResponse.put("token_type", "Bearer");
                if (userResume.getExpireTime() != null) {
                    introspectResponse.put("exp", System.currentTimeMillis() / 1000 + userResume.getExpireTime());
                }
                introspectResponse.put("iat", System.currentTimeMillis() / 1000);
                
                writeJsonResponse(response, introspectResponse);
            } catch (Exception e) {
                log.error("[OAuth][OAuth2] Introspect处理异常", e);
                try {
                    response.setStatus(500);
                    response.setContentType("application/json;charset=UTF-8");
                    response.setBodyString(Json.toJson(Map.of("active", false)));
                } catch (Exception ex) {
                    log.error("[OAuth][OAuth2] 写入错误响应失败", ex);
                }
            }
        });
        
        // OAuth2 UserInfo 端点 (GET)
        server.get("/oauth2/userinfo", (request, response) -> {
            try {
                var protocolRequest = new ProtocolServerAuthInformation.ProtocolServerRequest(request);
                var protocolResponse = new ProtocolServerAuthInformation.ProtocolServerResponse(response);
                
                String authHeader = protocolRequest.getHeader("Authorization");
                if (StringUtils.isBlank(authHeader) || !authHeader.startsWith("Bearer ")) {
                    protocolResponse.setStatus(401);
                    writeJsonResponse(response, Map.of("error", "invalid_token", "error_description", "缺少或无效的访问令牌"));
                    return;
                }
                
                String token = authHeader.substring(7);
                
                var tokenResolver = getTokenResolver();
                if (tokenResolver == null) {
                    protocolResponse.setStatus(500);
                    writeJsonResponse(response, Map.of("error", "server_error"));
                    return;
                }
                
                var result = tokenResolver.resolve(null, token);
                if (!result.isOk() || result.getData() == null) {
                    protocolResponse.setStatus(401);
                    writeJsonResponse(response, Map.of("error", "invalid_token", "error_description", "令牌无效或已过期"));
                    return;
                }
                
                var userResume = result.getData();
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
                
                writeJsonResponse(response, userInfo);
            } catch (Exception e) {
                log.error("[OAuth][OAuth2] UserInfo处理异常", e);
                try {
                    response.setStatus(500);
                    response.setContentType("application/json;charset=UTF-8");
                    response.setBodyString(Json.toJson(Map.of("error", "server_error")));
                } catch (Exception ex) {
                    log.error("[OAuth][OAuth2] 写入错误响应失败", ex);
                }
            }
        });
        
        // OAuth2 服务发现端点 (GET)
        server.get("/oauth2/.well-known/oauth-authorization-server", (request, response) -> {
            try {
                var protocolRequest = new ProtocolServerAuthInformation.ProtocolServerRequest(request);
                var protocolResponse = new ProtocolServerAuthInformation.ProtocolServerResponse(response);
                
                String baseUrl = getBaseUrl(protocolRequest);
                
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
                
                writeJsonResponse(response, metadata);
            } catch (Exception e) {
                log.error("[OAuth][OAuth2] 服务发现处理异常", e);
                try {
                    response.setStatus(500);
                    response.setContentType("application/json;charset=UTF-8");
                    response.setBodyString(Json.toJson(Map.of("error", "server_error")));
                } catch (Exception ex) {
                    log.error("[OAuth][OAuth2] 写入错误响应失败", ex);
                }
            }
        });
    }
    
    /**
     * 从 ServletRequest 提取请求体
     */
    private String extractBody(ServletRequest request) {
        try {
            byte[] body = request.getBody();
            if (body == null || body.length == 0) {
                return "";
            }
            return new String(body, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("[OAuth][OAuth2] 提取请求体失败", e);
            return "";
        }
    }
    
    /**
     * 解析 form-urlencoded 格式的请求体
     */
    private Map<String, String> parseFormUrlEncoded(String body) {
        Map<String, String> params = new HashMap<>();
        if (StringUtils.isBlank(body)) {
            return params;
        }
        String[] pairs = body.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                try {
                    params.put(keyValue[0], URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8));
                } catch (Exception e) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return params;
    }
    
    /**
     * 写入 JSON 响应
     */
    private void writeJsonResponse(ServletResponse response, Object data) {
        try {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(200);
            response.setBodyString(Json.toJson(data));
        } catch (Exception e) {
            log.error("[OAuth][OAuth2] 写入响应失败", e);
        }
    }
    
    /**
     * 发送授权错误重定向
     */
    private void sendAuthorizeError(ServletResponse response, String redirectUri,
                                    String state, String error, String description) {
        try {
            if (StringUtils.isBlank(redirectUri)) {
                response.setStatus(400);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setBodyString("{\"error\":\"" + error + "\",\"error_description\":\"" + description + "\"}");
                return;
            }
        
            String redirectUrl = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("error", error)
                    .queryParam("error_description", URLEncoder.encode(description, StandardCharsets.UTF_8))
                    .queryParamIfPresent("state", Optional.ofNullable(state))
                    .build().toUriString();
            
            // 创建 ProtocolServerResponse 包装器以使用 sendRedirect 方法
            var protocolResponse = new ProtocolServerAuthInformation.ProtocolServerResponse(response);
            protocolResponse.sendRedirect(redirectUrl);
        } catch (Exception e) {
            log.error("[OAuth][OAuth2] 发送授权错误失败", e);
            response.setStatus(500);
            response.setBodyString("{\"error\":\"server_error\"}");
        }
    }

    // ==================== 辅助方法 ====================
    
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
    
    @Override
    public ProtocolSetting getProtocolSetting() {
        return ProtocolSetting.builder()
                .port(authServerProperties.getArmeriaPort() != null ? 
                        authServerProperties.getArmeriaPort() : 8080)
                .build();
    }
    
    @Override
    public String getProtocolName() {
        return "oauth2";
    }
    
    @Override
    public Class<? extends ProtocolServer> getServerType() {
        return ProtocolServer.class;
    }
    
    @Override
    public ProtocolServer createServer(ProtocolSetting protocolSetting) {
        var protocol = ServiceProvider.of(Protocol.class).getExtension("http");
        if (protocol == null) {
            throw new IllegalStateException("未找到 HTTP Protocol 实现");
        }
        return protocol.createServer(protocolSetting);
    }
    
    @Override
    public Class<? extends com.chua.common.support.network.protocol.client.BaseProtocolClient> getClientType() {
        return com.chua.common.support.network.protocol.client.BaseProtocolClient.class;
    }
    
    @Override
    public com.chua.common.support.network.protocol.client.ProtocolClient createClient(ProtocolSetting protocolSetting) {
        var protocol = ServiceProvider.of(Protocol.class).getExtension("http");
        if (protocol == null) {
            throw new IllegalStateException("未找到 HTTP Protocol 实现");
        }
        return protocol.createClient(protocolSetting);
    }
}
