package com.chua.starter.oauth.sso.support.protocol;

import com.chua.common.support.annotations.Extension;
import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.oauth.client.support.entity.AuthRequest;
import com.chua.starter.oauth.client.support.enums.LogoutType;
import com.chua.starter.oauth.client.support.user.LoginResult;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.condition.OnBeanCondition;
import com.chua.starter.oauth.sso.support.information.LoginAuthInformation;
import com.chua.starter.oauth.sso.support.information.LogoutAuthInformation;
import com.chua.starter.oauth.sso.support.information.OauthAuthInformation;
import com.chua.starter.oauth.sso.support.information.UpgradeAuthInformation;
import com.chua.starter.oauth.sso.support.parser.LoginAuthorization;
import com.chua.starter.oauth.sso.support.parser.LogoutAuthorization;
import com.chua.starter.oauth.sso.support.parser.OauthAuthorization;
import com.chua.starter.oauth.sso.support.parser.UpgradeAuthorization;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import com.linecorp.armeria.common.HttpData;
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
                    authServerProperties.getArmeriaPort() : 9090;
            
            ServerBuilder builder = Server.builder();
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
     * @param builder ServerBuilder
     */
    private void registerOAuthEndpoints(ServerBuilder builder) {
        String contextPath = authServerProperties.getContextPath();
        
        // OAuth 认证端点
        builder.service(contextPath + "/oauth", (ctx, req) -> {
            try {
                String body = req.aggregate().join().contentUtf8();
                Map<String, Object> params = Json.fromJson(body, Map.class);
                
                OauthAuthorization authorization = applicationContext.getBean(OauthAuthorization.class);
                OauthAuthInformation information = authorization.parser(params);
                
                ReturnResult<UserResult> result = new ReturnResult<>();
                result.setCode(String.valueOf(information.getCode()));
                result.setMsg(information.getMessage());
                result.setData(information.getUserResult());
                
                return HttpResponse.of(HttpStatus.OK, MediaType.JSON_UTF_8, Json.toJson(result));
            } catch (Exception e) {
                log.error("OAuth 认证失败", e);
                return HttpResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        });
        
        // 登录端点
        builder.service(contextPath + "/login", (ctx, req) -> {
            try {
                String body = req.aggregate().join().contentUtf8();
                AuthRequest authRequest = Json.fromJson(body, AuthRequest.class);
                
                LoginAuthorization authorization = applicationContext.getBean(LoginAuthorization.class);
                LoginAuthInformation information = authorization.parser(authRequest);
                
                LoginResult loginResult = new LoginResult();
                loginResult.setCode(information.getCode());
                loginResult.setMessage(information.getMessage());
                loginResult.setToken(information.getToken());
                loginResult.setRefreshToken(information.getRefreshToken());
                loginResult.setUserResume(information.getUserResume());
                
                return HttpResponse.of(HttpStatus.OK, MediaType.JSON_UTF_8, Json.toJson(loginResult));
            } catch (Exception e) {
                log.error("登录失败", e);
                return HttpResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        });
        
        // 登出端点
        builder.service(contextPath + "/logout", (ctx, req) -> {
            try {
                String body = req.aggregate().join().contentUtf8();
                Map<String, Object> params = Json.fromJson(body, Map.class);
                
                LogoutAuthorization authorization = applicationContext.getBean(LogoutAuthorization.class);
                LogoutAuthInformation information = authorization.parser(
                        (String) params.get("uid"),
                        LogoutType.valueOf((String) params.get("logoutType"))
                );
                
                ReturnResult<Void> result = new ReturnResult<>();
                result.setCode(String.valueOf(information.getCode()));
                result.setMsg(information.getMessage());
                
                return HttpResponse.of(HttpStatus.OK, MediaType.JSON_UTF_8, Json.toJson(result));
            } catch (Exception e) {
                log.error("登出失败", e);
                return HttpResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        });
        
        // 升级端点
        builder.service(contextPath + "/upgrade", (ctx, req) -> {
            try {
                String body = req.aggregate().join().contentUtf8();
                Map<String, Object> params = Json.fromJson(body, Map.class);
                
                UpgradeAuthorization authorization = applicationContext.getBean(UpgradeAuthorization.class);
                UpgradeAuthInformation information = authorization.parser(params);
                
                ReturnResult<UserResult> result = new ReturnResult<>();
                result.setCode(String.valueOf(information.getCode()));
                result.setMsg(information.getMessage());
                result.setData(information.getUserResult());
                
                return HttpResponse.of(HttpStatus.OK, MediaType.JSON_UTF_8, Json.toJson(result));
            } catch (Exception e) {
                log.error("升级失败", e);
                return HttpResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        });
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
