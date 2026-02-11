package com.chua.starter.oauth.sso.support.protocol;

import com.chua.common.support.core.annotation.Extension;
import com.chua.common.support.core.annotation.Spi;
import com.chua.common.support.base.collection.Options;
import com.chua.common.support.core.spi.ServiceProvider;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.network.protocol.Protocol;
import com.chua.common.support.network.protocol.ProtocolSetting;
import com.chua.common.support.network.protocol.request.ServletRequest;
import com.chua.common.support.network.protocol.request.ServletResponse;
import com.chua.common.support.network.protocol.server.ProtocolServer;
import com.chua.common.support.text.json.Json;
import com.chua.starter.oauth.client.support.entity.AuthRequest;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.condition.OnBeanCondition;
import com.chua.starter.oauth.sso.support.information.LoginAuthInformation;
import com.chua.starter.oauth.sso.support.information.LogoutAuthInformation;
import com.chua.starter.oauth.sso.support.information.OauthAuthInformation;
import com.chua.starter.oauth.sso.support.information.ProtocolServerAuthInformation;
import com.chua.starter.oauth.sso.support.information.TemporaryTokenAuthInformation;
import com.chua.starter.oauth.sso.support.information.UpgradeAuthInformation;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Armeria 协议
 *
 * @author CH
 */
@Component
@Extension("armeria")
@Spi("armeria")
@Slf4j
@Conditional(OnBeanCondition.class)
@RequiredArgsConstructor
@Tag(name = "认证服务", description = "提供Armeria协议的认证服务接口")
public class ArmeriaOauthProtocol implements OauthProtocol, InitializingBean {

    final AuthServerProperties authServerProperties;

    final LoginCheck loginCheck;

    final ApplicationContext applicationContext;

    @Value("${plugin.auth.server.context-path:}")
    private String contextPath;

    private ProtocolServer protocolServer;

    @Override
    public void afterPropertiesSet() throws Exception {
        // 检查是否启用 armeria 协议
        if (!authServerProperties.getProtocols().contains("armeria")) {
            log.info("[OAuth][Armeria] 协议未启用，跳过 ProtocolServer 启动");
            return;
        }
        var protocolSetting = ProtocolSetting.builder()
                .port(authServerProperties.getArmeriaPort() != null ? authServerProperties.getArmeriaPort() : 19180)
                // 统一由 Armeria 底层根据 contextPath 处理路由前缀，业务侧仅注册相对路径
                .options(Options.of("contextPath", contextPath))
                .build();
        try {
            var protocol = ServiceProvider.of(Protocol.class).getNewExtension("armeria", protocolSetting);
            if (protocol == null) {
                log.warn("[OAuth][Armeria] 未找到 Armeria Protocol 实现，跳过 ProtocolServer 启动");
                return;
            }


            protocolServer = protocol.createServer(protocolSetting);
            registerProtocolServerRoutes(protocolServer);
            protocolServer.start();

            log.info("[OAuth][Armeria] ProtocolServer 启动成功，端口: {}", protocolSetting.getPort());
        } catch (Exception e) {
            log.error("[OAuth][Armeria] ProtocolServer 启动失败", e);
        }
    }

    /**
     * 注册 ProtocolServer 路由
     *
     * @param server ProtocolServer 实例
     */
    private void registerProtocolServerRoutes(ProtocolServer server) {
        // 登录端点
        server.post("/login", (request, response) -> {
            try {
                String body = extractBody(request);
                AuthRequest authRequest = Json.fromJson(body, AuthRequest.class);
                if (authRequest == null) {
                    authRequest = new AuthRequest();
                }

                var protocolRequest = new ProtocolServerAuthInformation.ProtocolServerRequest(request);
                var protocolResponse = new ProtocolServerAuthInformation.ProtocolServerResponse(response);

                var authInformation = new LoginAuthInformation(
                        authServerProperties,
                        authRequest.getData(),
                        protocolRequest,
                        protocolResponse,
                        applicationContext,
                        loginCheck);

                if (!authInformation.isSuccessful()) {
                    writeResponse(response, ReturnResult.illegal(authInformation.errorMessage()));
                    return;
                }

                var result = authInformation.authorization().authentication();
                writeResponse(response, result);
            } catch (Exception e) {
                log.error("[OAuth][Armeria] 登录处理异常", e);
                writeResponse(response, ReturnResult.error("登录失败: " + e.getMessage()));
            }
        });

        // 登出端点
        server.post("/logout", (request, response) -> {
            try {
                String body = extractBody(request);
                AuthRequest authRequest = Json.fromJson(body, AuthRequest.class);
                if (authRequest == null) {
                    authRequest = new AuthRequest();
                }

                var protocolRequest = new ProtocolServerAuthInformation.ProtocolServerRequest(request);
                var protocolResponse = new ProtocolServerAuthInformation.ProtocolServerResponse(response);

                var authInformation = new LogoutAuthInformation(
                        authServerProperties,
                        authRequest.getData(),
                        protocolRequest,
                        protocolResponse,
                        applicationContext,
                        loginCheck);

                if (!authInformation.isSuccessful()) {
                    writeResponse(response, ReturnResult.illegal(authInformation.errorMessage()));
                    return;
                }

                var result = authInformation.authorization().authentication();
                writeResponse(response, result);
            } catch (Exception e) {
                log.error("[OAuth][Armeria] 登出处理异常", e);
                writeResponse(response, ReturnResult.error("登出失败: " + e.getMessage()));
            }
        });

        // 鉴权端点
        server.post("/oauth", (request, response) -> {
            try {
                String body = extractBody(request);
                AuthRequest authRequest = Json.fromJson(body, AuthRequest.class);
                if (authRequest == null) {
                    authRequest = new AuthRequest();
                }

                var protocolRequest = new ProtocolServerAuthInformation.ProtocolServerRequest(request);
                var protocolResponse = new ProtocolServerAuthInformation.ProtocolServerResponse(response);

                var authInformation = new OauthAuthInformation(
                        authServerProperties,
                        authRequest.getData(),
                        protocolRequest,
                        protocolResponse,
                        applicationContext,
                        loginCheck);

                if (!authInformation.isSuccessful()) {
                    writeResponse(response, ReturnResult.illegal(authInformation.errorMessage()));
                    return;
                }

                var result = authInformation.authorization().authentication();
                writeResponse(response, result);
            } catch (Exception e) {
                log.error("[OAuth][Armeria] 鉴权处理异常", e);
                writeResponse(response, ReturnResult.error("鉴权失败: " + e.getMessage()));
            }
        });

        // 升级端点
        server.post("/upgrade", (request, response) -> {
            try {
                String body = extractBody(request);
                AuthRequest authRequest = Json.fromJson(body, AuthRequest.class);
                if (authRequest == null) {
                    authRequest = new AuthRequest();
                }

                var protocolRequest = new ProtocolServerAuthInformation.ProtocolServerRequest(request);
                var protocolResponse = new ProtocolServerAuthInformation.ProtocolServerResponse(response);

                var authInformation = new UpgradeAuthInformation(
                        authServerProperties,
                        authRequest.getData(),
                        protocolRequest,
                        protocolResponse,
                        applicationContext,
                        loginCheck);

                if (!authInformation.isSuccessful()) {
                    writeResponse(response, ReturnResult.illegal(authInformation.errorMessage()));
                    return;
                }

                var result = authInformation.authorization().authentication();
                writeResponse(response, result);
            } catch (Exception e) {
                log.error("[OAuth][Armeria] 令牌升级处理异常", e);
                writeResponse(response, ReturnResult.error("令牌升级失败: " + e.getMessage()));
            }
        });

        // 临时令牌端点
        server.post("/temporary-token", (request, response) -> {
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
                    writeResponse(response, ReturnResult.illegal(authInformation.errorMessage()));
                    return;
                }

                var result = authInformation.authorization().authentication();
                writeResponse(response, result);
            } catch (Exception e) {
                log.error("[OAuth][Armeria] 临时令牌处理异常", e);
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
            log.error("[OAuth][Armeria] 提取请求体失败", e);
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
            log.error("[OAuth][Armeria] 写入响应失败", e);
        }
    }
}
