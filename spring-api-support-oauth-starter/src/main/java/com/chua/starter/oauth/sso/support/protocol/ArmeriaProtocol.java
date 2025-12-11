package com.chua.starter.oauth.sso.support.protocol;

import com.chua.common.support.annotations.Extension;
import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.MapUtils;
import com.chua.starter.oauth.client.support.entity.AuthRequest;
import com.chua.starter.oauth.client.support.user.LoginResult;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.sso.support.token.TokenResolver;
import com.google.common.base.Strings;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.condition.OnBeanCondition;
import com.chua.starter.oauth.sso.support.information.ArmeriaAuthInformation;
import com.chua.starter.oauth.sso.support.information.LoginAuthInformation;
import com.chua.starter.oauth.sso.support.information.LogoutAuthInformation;
import com.chua.starter.oauth.sso.support.information.OauthAuthInformation;
import com.chua.starter.oauth.sso.support.information.TemporaryTokenAuthInformation;
import com.chua.starter.oauth.sso.support.parser.TemporaryTokenAuthorization;
import com.chua.starter.oauth.sso.support.information.UpgradeAuthInformation;
import com.chua.starter.oauth.sso.support.parser.LoginAuthorization;
import com.chua.starter.oauth.sso.support.parser.LogoutAuthorization;
import com.chua.starter.oauth.sso.support.parser.OauthAuthorization;
import com.chua.starter.oauth.sso.support.parser.UpgradeAuthorization;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.ServerBuilder;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Armeria 协议实现
 * 提供基于 Armeria 的高性能 OAuth 认证服务
 *
 * @author CH
 * @version 1.0.0
 * @since 2025-12-02
 */
@Component
@Extension("armeria")
@Slf4j
@Conditional(OnBeanCondition.class)
@RequiredArgsConstructor
@Tag(name = "认证服务", description = "提供Armeria协议的认证服务接口")
public class ArmeriaProtocol implements Protocol, InitializingBean {

    private final AuthServerProperties authServerProperties;
    private final LoginCheck loginCheck;
    private final ApplicationContext applicationContext;
    
    private Server server;

    @Override
    public void afterPropertiesSet() throws Exception {
        initializeArmeriaServer();
    }

    /**
     * 初始化 Armeria 服务器
     */
    private void initializeArmeriaServer() {
        try {
            int port = authServerProperties.getArmeriaPort() != null ? 
                    authServerProperties.getArmeriaPort() : 9091;
            
            ServerBuilder builder = Server.builder()
                    .maxNumConnections(1_000_000)  // 百万连接
                    .idleTimeoutMillis(30000);
            builder.http(port);

            // 注册 OAuth 认证接口
            registerOAuthEndpoints(builder);
            
            this.server = builder.build();
            server.start().join();
            
            log.info("Armeria OAuth 服务器已启动，端口: {}", port);
        } catch (Exception e) {
            log.error("Armeria OAuth 服务器启动失败", e);
        }
    }

    /**
     * 注册 OAuth 端点
     *
     * @param builder ServerBuilder 服务器构建器
     */
    private void registerOAuthEndpoints(ServerBuilder builder) {
        String contextPath = authServerProperties.getContextPath() != null 
                ? authServerProperties.getContextPath() : "";
        
        // OAuth 认证端点
        builder.service(contextPath + "/oauth", (ctx, req) -> {
            try {
                String body = req.aggregate().join().contentUtf8();
                AuthRequest authRequest = Json.fromJson(body, AuthRequest.class);
                
                // 创建适配器请求/响应对象
                ArmeriaAuthInformation.ArmeriaRequest tcpRequest = createTcpRequest(ctx, req);
                ArmeriaAuthInformation.ArmeriaResponse tcpResponse = new ArmeriaAuthInformation.ArmeriaResponse();
                
                // 创建认证信息
                OauthAuthInformation authInformation = new OauthAuthInformation(
                        authServerProperties,
                        authRequest.getData(),
                        tcpRequest,
                        tcpResponse,
                        applicationContext,
                        loginCheck);
                
                // 验证签名
                if (!authInformation.isSuccessful()) {
                    return HttpResponse.of(HttpStatus.OK, MediaType.JSON_UTF_8, 
                            Json.toJson(ReturnResult.illegal(authInformation.errorMessage())));
                }
                
                // 执行认证
                OauthAuthorization authorization = authInformation.authorization();
                ReturnResult<String> result = authorization.authentication();
                
                return HttpResponse.of(HttpStatus.OK, MediaType.JSON_UTF_8, Json.toJson(result));
            } catch (Exception e) {
                log.error("OAuth 认证失败", e);
                return HttpResponse.of(HttpStatus.OK, MediaType.JSON_UTF_8, 
                        Json.toJson(ReturnResult.error("OAuth认证失败: " + e.getMessage())));
            }
        });
        
        // 登录端点
        builder.service(contextPath + "/login", (ctx, req) -> {
            try {
                String body = req.aggregate().join().contentUtf8();
                AuthRequest authRequest = Json.fromJson(body, AuthRequest.class);
                
                // 创建适配器请求/响应对象
                ArmeriaAuthInformation.ArmeriaRequest tcpRequest = createTcpRequest(ctx, req);
                ArmeriaAuthInformation.ArmeriaResponse tcpResponse = new ArmeriaAuthInformation.ArmeriaResponse();
                
                // 创建登录认证信息
                LoginAuthInformation authInformation = new LoginAuthInformation(
                        authServerProperties,
                        authRequest.getData(),
                        tcpRequest,
                        tcpResponse,
                        applicationContext,
                        loginCheck);
                
                // 验证签名
                if (!authInformation.isSuccessful()) {
                    return HttpResponse.of(HttpStatus.OK, MediaType.JSON_UTF_8, 
                            Json.toJson(ReturnResult.illegal(authInformation.errorMessage())));
                }
                
                // 执行登录
                LoginAuthorization authorization = authInformation.authorization();
                ReturnResult<String> result = authorization.authentication();
                
                return HttpResponse.of(HttpStatus.OK, MediaType.JSON_UTF_8, Json.toJson(result));
            } catch (Exception e) {
                log.error("登录失败", e);
                return HttpResponse.of(HttpStatus.OK, MediaType.JSON_UTF_8, 
                        Json.toJson(ReturnResult.error("登录失败: " + e.getMessage())));
            }
        });
        
        // 登出端点
        builder.service(contextPath + "/logout", (ctx, req) -> {
            try {
                String body = req.aggregate().join().contentUtf8();
                AuthRequest authRequest = Json.fromJson(body, AuthRequest.class);
                
                // 创建适配器请求/响应对象
                ArmeriaAuthInformation.ArmeriaRequest tcpRequest = createTcpRequest(ctx, req);
                ArmeriaAuthInformation.ArmeriaResponse tcpResponse = new ArmeriaAuthInformation.ArmeriaResponse();
                
                // 创建登出认证信息
                LogoutAuthInformation authInformation = new LogoutAuthInformation(
                        authServerProperties,
                        authRequest.getData(),
                        tcpRequest,
                        tcpResponse,
                        applicationContext,
                        loginCheck);
                
                // 验证签名
                if (!authInformation.isSuccessful()) {
                    return HttpResponse.of(HttpStatus.OK, MediaType.JSON_UTF_8, 
                            Json.toJson(ReturnResult.illegal(authInformation.errorMessage())));
                }
                
                // 执行登出
                LogoutAuthorization authorization = authInformation.authorization();
                ReturnResult<String> result = authorization.authentication();
                
                return HttpResponse.of(HttpStatus.OK, MediaType.JSON_UTF_8, Json.toJson(result));
            } catch (Exception e) {
                log.error("登出失败", e);
                return HttpResponse.of(HttpStatus.OK, MediaType.JSON_UTF_8, 
                        Json.toJson(ReturnResult.error("登出失败: " + e.getMessage())));
            }
        });
        
        // 升级端点
        builder.service(contextPath + "/upgrade", (ctx, req) -> {
            try {
                String body = req.aggregate().join().contentUtf8();
                AuthRequest authRequest = Json.fromJson(body, AuthRequest.class);
                
                // 创建适配器请求/响应对象
                ArmeriaAuthInformation.ArmeriaRequest tcpRequest = createTcpRequest(ctx, req);
                ArmeriaAuthInformation.ArmeriaResponse tcpResponse = new ArmeriaAuthInformation.ArmeriaResponse();
                
                // 创建升级认证信息
                UpgradeAuthInformation authInformation = new UpgradeAuthInformation(
                        authServerProperties,
                        authRequest.getData(),
                        tcpRequest,
                        tcpResponse,
                        applicationContext,
                        loginCheck);
                
                // 验证签名
                if (!authInformation.isSuccessful()) {
                    return HttpResponse.of(HttpStatus.OK, MediaType.JSON_UTF_8, 
                            Json.toJson(ReturnResult.illegal(authInformation.errorMessage())));
                }
                
                // 执行升级
                UpgradeAuthorization authorization = authInformation.authorization();
                ReturnResult<String> result = authorization.authentication();
                
                return HttpResponse.of(HttpStatus.OK, MediaType.JSON_UTF_8, Json.toJson(result));
            } catch (Exception e) {
                log.error("升级失败", e);
                return HttpResponse.of(HttpStatus.OK, MediaType.JSON_UTF_8, 
                        Json.toJson(ReturnResult.error("升级失败: " + e.getMessage())));
            }
        });
        
        // 临时令牌端点
        builder.service(contextPath + "/temporary-token", (ctx, req) -> {
            try {
                String body = req.aggregate().join().contentUtf8();
                AuthRequest authRequest = Json.fromJson(body, AuthRequest.class);
                
                // 创建适配器请求/响应对象
                ArmeriaAuthInformation.ArmeriaRequest tcpRequest = createTcpRequest(ctx, req);
                ArmeriaAuthInformation.ArmeriaResponse tcpResponse = new ArmeriaAuthInformation.ArmeriaResponse();
                
                // 使用 TemporaryTokenAuthInformation 解析请求
                TemporaryTokenAuthInformation authInformation = new TemporaryTokenAuthInformation(
                        authServerProperties,
                        authRequest.getData(),
                        tcpRequest,
                        tcpResponse,
                        applicationContext,
                        loginCheck);
                
                // 验证签名
                if (!authInformation.isSuccessful()) {
                    return HttpResponse.of(HttpStatus.OK, MediaType.JSON_UTF_8, 
                            Json.toJson(ReturnResult.illegal(authInformation.errorMessage())));
                }
                
                // 使用 TemporaryTokenAuthorization 执行创建逻辑
                TemporaryTokenAuthorization authorization = authInformation.authorization();
                ReturnResult<String> result = authorization.authentication();
                
                return HttpResponse.of(HttpStatus.OK, MediaType.JSON_UTF_8, Json.toJson(result));
            } catch (Exception e) {
                log.error("临时令牌创建失败", e);
                return HttpResponse.of(HttpStatus.OK, MediaType.JSON_UTF_8, 
                        Json.toJson(ReturnResult.error("临时令牌创建失败: " + e.getMessage())));
            }
        });
    }

    /**
     * 从 Armeria 请求创建 TcpRequest 适配器
     *
     * @param ctx Armeria 服务上下文
     * @param req Armeria HTTP 请求
     * @return TcpRequest 适配器对象
     */
    private ArmeriaAuthInformation.ArmeriaRequest createTcpRequest(
            com.linecorp.armeria.server.ServiceRequestContext ctx, 
            com.linecorp.armeria.common.HttpRequest req) {
        // 获取客户端地址
        InetSocketAddress clientAddress = ctx.remoteAddress();
        ArmeriaAuthInformation.ArmeriaRequest tcpRequest = new ArmeriaAuthInformation.ArmeriaRequest(clientAddress);
        
        // 复制请求头到 TcpRequest
        req.headers().forEach(header -> tcpRequest.setHeader(header.getKey().toString(), header.getValue()));
        
        return tcpRequest;
    }

    /**
     * 关闭服务器
     */
    public void shutdown() {
        if (server != null) {
            try {
                server.stop().join();
                log.info("Armeria OAuth 服务器已关闭");
            } catch (Exception e) {
                log.error("关闭 Armeria OAuth 服务器失败", e);
            }
        }
    }
}
