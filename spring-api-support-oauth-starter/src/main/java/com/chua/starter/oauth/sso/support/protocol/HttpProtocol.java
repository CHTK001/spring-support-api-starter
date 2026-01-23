package com.chua.starter.oauth.sso.support.protocol;

import com.chua.common.support.core.annotation.Extension;
import com.chua.common.support.resilience.rate.RateLimiter;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.core.spi.ServiceProvider;
import com.chua.common.support.core.utils.MapUtils;
import com.chua.common.support.text.json.Json;
import com.chua.common.support.network.protocol.Protocol;
import com.chua.common.support.network.protocol.ProtocolSetting;
import com.chua.common.support.network.protocol.server.ProtocolServer;
import com.chua.starter.common.support.utils.CookieUtil;
import com.chua.starter.common.support.utils.RequestUtils;
import com.chua.starter.oauth.client.support.entity.AuthRequest;
import com.chua.starter.oauth.client.support.enums.LogoutType;
import com.chua.starter.oauth.client.support.user.LoginResult;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.sso.support.advice.AdviceView;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.condition.OnBeanCondition;
import com.chua.starter.oauth.sso.support.information.LoginAuthInformation;
import com.chua.starter.oauth.sso.support.information.LogoutAuthInformation;
import com.chua.starter.oauth.sso.support.information.OauthAuthInformation;
import com.chua.starter.oauth.sso.support.information.TemporaryTokenAuthInformation;
import com.chua.starter.oauth.sso.support.information.ProtocolServerAuthInformation;
import com.chua.starter.oauth.sso.support.parser.TemporaryTokenAuthorization;
import com.chua.starter.oauth.sso.support.information.UpgradeAuthInformation;
import com.chua.starter.oauth.sso.support.parser.LoginAuthorization;
import com.chua.starter.oauth.sso.support.parser.LogoutAuthorization;
import com.chua.starter.oauth.sso.support.parser.OauthAuthorization;
import com.chua.starter.oauth.sso.support.parser.UpgradeAuthorization;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import com.chua.starter.oauth.sso.support.token.TokenResolver;
import com.google.common.base.Strings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.chua.common.support.network.protocol.request.ServletRequest;
import com.chua.common.support.network.protocol.request.ServletResponse;
import com.chua.common.support.network.protocol.ProtocolSetting;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * http
 *
 * @author CH
 */
@Component
@Extension("http")
@com.chua.common.support.core.annotation.Spi("http")
@Slf4j
@Conditional(OnBeanCondition.class)
@RequiredArgsConstructor
@Tag(name = "认证服务", description = "提供HTTP协议的认证服务接口")
public class HttpProtocol implements Protocol, InitializingBean {

    final AuthServerProperties authServerProperties;

    final LoginCheck loginCheck;

    final ApplicationContext applicationContext;

    @Value("${plugin.auth.server.context-path:}")
    private String contextPath;
    
    private ProtocolServer protocolServer;

    @Override
    public void afterPropertiesSet() throws Exception {
        // 检查是否启用 http 协议
        if (!authServerProperties.getProtocols().contains("http")) {
            log.info("HTTP 协议未启用，跳过 ProtocolServer 启动");
            return;
        }
        
        try {
            // 获取 Protocol 实例
            var protocol = ServiceProvider.of(Protocol.class).getExtension("http");
            if (protocol == null) {
                log.warn("未找到 HTTP Protocol 实现，跳过 ProtocolServer 启动");
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
            
            log.info("[OAuth][HTTP] ProtocolServer 启动成功，端口: {}", protocolSetting.getPort());
        } catch (Exception e) {
            log.error("[OAuth][HTTP] ProtocolServer 启动失败", e);
        }
    }
    
    /**
     * 注册 ProtocolServer 路由
     *
     * @param server ProtocolServer 实例
     */
    private void registerProtocolServerRoutes(ProtocolServer server) {
        String path = contextPath != null && !contextPath.isEmpty() ? contextPath : "";
        
        // 登录端点
        server.post(path + "/login", (request, response) -> {
            try {
                String body = extractBody(request);
                AuthRequest authRequest = Json.fromJson(body, AuthRequest.class);
                if (authRequest == null) {
                    authRequest = new AuthRequest();
                }
                
                var protocolRequest = new ProtocolServerAuthInformation.ProtocolServerRequest(request);
                var protocolResponse = new ProtocolServerAuthInformation.ProtocolServerResponse(response);
                
                long startTime = System.currentTimeMillis();
                String clientIp = protocolRequest.getRemoteAddr();
                
                log.debug("[OAuth][HTTP] 登录请求开始，客户端IP: {}", clientIp);
                
                var authInformation = new LoginAuthInformation(
                        authServerProperties,
                        authRequest.getData(),
                        protocolRequest,
                        protocolResponse,
                        applicationContext,
                        loginCheck);
                
                if (!authInformation.isSuccessful()) {
                    long cost = System.currentTimeMillis() - startTime;
                    log.warn("[OAuth][HTTP] 登录请求解析失败: {}, 客户端IP: {}, 耗时: {}ms", 
                            authInformation.errorMessage(), clientIp, cost);
                    writeResponse(response, ReturnResult.illegal(authInformation.errorMessage()));
                    return;
                }
                
                var authorization = authInformation.authorization();
                var result = authorization.authentication();
                
                long cost = System.currentTimeMillis() - startTime;
                if (result.isFailure()) {
                    log.warn("[OAuth][HTTP] 登录认证失败: {}, 客户端IP: {}, 耗时: {}ms", 
                            result.getMsg(), clientIp, cost);
                } else {
                    log.info("[OAuth][HTTP] 登录认证成功, 客户端IP: {}, 耗时: {}ms", clientIp, cost);
                }
                
                writeResponse(response, result);
            } catch (Exception e) {
                log.error("[OAuth][HTTP] 登录处理异常", e);
                writeResponse(response, ReturnResult.error("登录失败: " + e.getMessage()));
            }
        });
        
        // 登出端点
        server.post(path + "/logout", (request, response) -> {
            try {
                String body = extractBody(request);
                AuthRequest authRequest = Json.fromJson(body, AuthRequest.class);
                if (authRequest == null) {
                    authRequest = new AuthRequest();
                }
                
                var protocolRequest = new ProtocolServerAuthInformation.ProtocolServerRequest(request);
                var protocolResponse = new ProtocolServerAuthInformation.ProtocolServerResponse(response);
                
                long startTime = System.currentTimeMillis();
                String clientIp = protocolRequest.getRemoteAddr();
                
                log.debug("[OAuth][HTTP] 登出请求开始，客户端IP: {}", clientIp);
                
                var authInformation = new LogoutAuthInformation(
                        authServerProperties,
                        authRequest.getData(),
                        protocolRequest,
                        protocolResponse,
                        applicationContext,
                        loginCheck);
                
                if (!authInformation.isSuccessful()) {
                    long cost = System.currentTimeMillis() - startTime;
                    log.warn("[OAuth][HTTP] 登出请求解析失败: {}, 客户端IP: {}, 耗时: {}ms", 
                            authInformation.errorMessage(), clientIp, cost);
                    writeResponse(response, ReturnResult.illegal(authInformation.errorMessage()));
                    return;
                }
                
                var authorization = authInformation.authorization();
                var result = authorization.authentication();
                
                long cost = System.currentTimeMillis() - startTime;
                if (result.isFailure()) {
                    log.warn("[OAuth][HTTP] 登出失败: {}, 客户端IP: {}, 耗时: {}ms", 
                            result.getMsg(), clientIp, cost);
                } else {
                    log.info("[OAuth][HTTP] 登出成功, 客户端IP: {}, 耗时: {}ms", clientIp, cost);
                }
                
                writeResponse(response, result);
            } catch (Exception e) {
                log.error("[OAuth][HTTP] 登出处理异常", e);
                writeResponse(response, ReturnResult.error("登出失败: " + e.getMessage()));
            }
        });
        
        // 鉴权端点
        server.post(path + "/oauth", (request, response) -> {
            try {
                String body = extractBody(request);
                AuthRequest authRequest = Json.fromJson(body, AuthRequest.class);
                if (authRequest == null) {
                    authRequest = new AuthRequest();
                }
                
                var protocolRequest = new ProtocolServerAuthInformation.ProtocolServerRequest(request);
                var protocolResponse = new ProtocolServerAuthInformation.ProtocolServerResponse(response);
                
                long startTime = System.currentTimeMillis();
                String clientIp = protocolRequest.getRemoteAddr();
                
                log.debug("[OAuth][HTTP] 鉴权请求开始，客户端IP: {}", clientIp);
                
                var authInformation = new OauthAuthInformation(
                        authServerProperties,
                        authRequest.getData(),
                        protocolRequest,
                        protocolResponse,
                        applicationContext,
                        loginCheck);
                
                if (!authInformation.isSuccessful()) {
                    long cost = System.currentTimeMillis() - startTime;
                    log.warn("[OAuth][HTTP] 鉴权请求解析失败: {}, 客户端IP: {}, 耗时: {}ms", 
                            authInformation.errorMessage(), clientIp, cost);
                    writeResponse(response, ReturnResult.illegal(authInformation.errorMessage()));
                    return;
                }
                
                var authorization = authInformation.authorization();
                var result = authorization.authentication();
                
                long cost = System.currentTimeMillis() - startTime;
                if (result.isFailure()) {
                    log.debug("[OAuth][HTTP] 鉴权失败: {}, 客户端IP: {}, 耗时: {}ms", result.getMsg(), clientIp, cost);
                } else {
                    log.debug("[OAuth][HTTP] 鉴权成功, 客户端IP: {}, 耗时: {}ms", clientIp, cost);
                }
                
                writeResponse(response, result);
            } catch (Exception e) {
                log.error("[OAuth][HTTP] 鉴权处理异常", e);
                writeResponse(response, ReturnResult.error("鉴权失败: " + e.getMessage()));
            }
        });
        
        // 升级端点
        server.post(path + "/upgrade", (request, response) -> {
            try {
                String body = extractBody(request);
                AuthRequest authRequest = Json.fromJson(body, AuthRequest.class);
                if (authRequest == null) {
                    authRequest = new AuthRequest();
                }
                
                var protocolRequest = new ProtocolServerAuthInformation.ProtocolServerRequest(request);
                var protocolResponse = new ProtocolServerAuthInformation.ProtocolServerResponse(response);
                
                long startTime = System.currentTimeMillis();
                String clientIp = protocolRequest.getRemoteAddr();
                
                log.debug("[OAuth][HTTP] 令牌升级请求开始，客户端IP: {}", clientIp);
                
                var authInformation = new UpgradeAuthInformation(
                        authServerProperties,
                        authRequest.getData(),
                        protocolRequest,
                        protocolResponse,
                        applicationContext,
                        loginCheck);
                
                if (!authInformation.isSuccessful()) {
                    long cost = System.currentTimeMillis() - startTime;
                    log.warn("[OAuth][HTTP] 令牌升级请求解析失败: {}, 客户端IP: {}, 耗时: {}ms", 
                            authInformation.errorMessage(), clientIp, cost);
                    writeResponse(response, ReturnResult.illegal(authInformation.errorMessage()));
                    return;
                }
                
                var authorization = authInformation.authorization();
                var result = authorization.authentication();
                
                long cost = System.currentTimeMillis() - startTime;
                if (result.isFailure()) {
                    log.warn("[OAuth][HTTP] 令牌升级失败: {}, 客户端IP: {}, 耗时: {}ms", result.getMsg(), clientIp, cost);
                } else {
                    log.info("[OAuth][HTTP] 令牌升级成功, 客户端IP: {}, 耗时: {}ms", clientIp, cost);
                }
                
                writeResponse(response, result);
            } catch (Exception e) {
                log.error("[OAuth][HTTP] 令牌升级处理异常", e);
                writeResponse(response, ReturnResult.error("令牌升级失败: " + e.getMessage()));
            }
        });
        
        // 临时令牌端点
        server.post(path + "/temporary-token", (request, response) -> {
            try {
                String body = extractBody(request);
                AuthRequest authRequest = Json.fromJson(body, AuthRequest.class);
                if (authRequest == null) {
                    authRequest = new AuthRequest();
                }
                
                var protocolRequest = new ProtocolServerAuthInformation.ProtocolServerRequest(request);
                var protocolResponse = new ProtocolServerAuthInformation.ProtocolServerResponse(response);
                
                var authInformation = new TemporaryTokenAuthInformation(
                        authServerProperties,
                        authRequest.getData(),
                        protocolRequest,
                        protocolResponse,
                        applicationContext,
                        loginCheck);
                
                if (!authInformation.isSuccessful()) {
                    log.warn("[OAuth][HTTP] 临时令牌请求解析失败: {}, 客户端IP: {}",
                            authInformation.errorMessage(), authInformation.getClientIp());
                    writeResponse(response, ReturnResult.illegal(authInformation.errorMessage()));
                    return;
                }
                
                var authorization = authInformation.authorization();
                var result = authorization.authentication();
                
                writeResponse(response, result);
            } catch (Exception e) {
                log.error("[OAuth][HTTP] 临时令牌处理异常", e);
                writeResponse(response, ReturnResult.error("临时令牌创建失败: " + e.getMessage()));
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
            log.error("[OAuth][HTTP] 提取请求体失败", e);
            return "";
        }
    }
    
    /**
     * 写入响应
     */
    private void writeResponse(ServletResponse response, ReturnResult<?> result) {
        try {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(200);
            response.setBodyString(Json.toJson(result));
        } catch (Exception e) {
            log.error("[OAuth][HTTP] 写入响应失败", e);
        }
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
        return "http";
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
