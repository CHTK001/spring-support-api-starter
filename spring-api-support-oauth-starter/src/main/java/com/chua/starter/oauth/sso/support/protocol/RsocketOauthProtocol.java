package com.chua.starter.oauth.sso.support.protocol;

import com.chua.common.support.core.annotation.Extension;
import com.chua.common.support.core.annotation.Spi;
import com.chua.common.support.text.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.oauth.client.support.entity.AuthRequest;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.condition.OnBeanCondition;
import com.chua.starter.oauth.sso.support.information.LoginAuthInformation;
import com.chua.starter.oauth.sso.support.information.LogoutAuthInformation;
import com.chua.starter.oauth.sso.support.information.OauthAuthInformation;
import com.chua.starter.oauth.sso.support.information.RsocketAuthInformation;
import com.chua.starter.oauth.sso.support.information.TemporaryTokenAuthInformation;
import com.chua.starter.oauth.sso.support.information.UpgradeAuthInformation;
import com.chua.starter.oauth.sso.support.parser.OauthAuthorization;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import io.rsocket.Payload;
import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketServer;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.Map;

/**
 * RSocket 协议实现
 * <p>
 * 提供基于 RSocket 的 OAuth 认证服务
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2025-01-05
 */
@Component
@Extension("rsocket")
@Spi("rsocket")
@Slf4j
@Conditional(OnBeanCondition.class)
@RequiredArgsConstructor
public class RsocketOauthProtocol implements OauthProtocol, InitializingBean, DisposableBean {

    private final AuthServerProperties authServerProperties;
    private final LoginCheck loginCheck;
    private final ApplicationContext applicationContext;

    private Disposable serverDisposable;

    @Override
    public void afterPropertiesSet() throws Exception {
        initializeRsocketServer();
    }

    /**
     * 初始化 RSocket 服务器
     */
    private void initializeRsocketServer() {
        if (!authServerProperties.getProtocols().contains("rsocket")) {
            log.info("[OAuth][Armeria] 协议未启用，跳过 ProtocolServer 启动");
            return;
        }
        try {
            int port = authServerProperties.getRsocketPort() != null ?
                    authServerProperties.getRsocketPort() : 7000;

            SocketAcceptor acceptor = SocketAcceptor.forRequestResponse(
                    payload -> handleRequest(payload)
            );

            RSocketServer server = RSocketServer.create(acceptor)
                    .payloadDecoder(PayloadDecoder.ZERO_COPY);

            this.serverDisposable = server.bind(TcpServerTransport.create(port))
                    .block();

            log.info("RSocket OAuth 服务器已启动，端口: {}", port);
        } catch (Exception e) {
            log.error("RSocket OAuth 服务器启动失败", e);
        }
    }

    /**
     * 处理请求
     *
     * @param payload RSocket 负载
     * @return 响应
     */
    private Mono<Payload> handleRequest(Payload payload) {
        // 共享的 rsocketResponse，用于收集响应头（如 x-oauth-response-serial）
        RsocketAuthInformation.RsocketResponse sharedResponse = new RsocketAuthInformation.RsocketResponse();
        try {
            // metadata 是 JSON，需要解析出 route 字段
            String metadataRaw = payload.getMetadataUtf8();
            String route = metadataRaw;
            try {
                var metaObj = com.chua.common.support.text.json.Json.getJsonObject(metadataRaw);
                if (metaObj != null && metaObj.containsKey("route")) {
                    route = "/" + metaObj.getString("route");
                }
            } catch (Exception ignored) {
                // 非 JSON 格式则直接用原始值
            }
            String body = payload.getDataUtf8();

            log.debug("RSocket 请求: route={}, bodyLength={}", route, body != null ? body.length() : 0);

            // 根据路由分发请求
            ReturnResult<String> result;
            switch (route) {
                case "/oauth":
                    result = handleOauth(body, payload, sharedResponse);
                    break;
                case "/login":
                    result = handleLogin(body, payload, sharedResponse);
                    break;
                case "/logout":
                    result = handleLogout(body, payload, sharedResponse);
                    break;
                case "/upgrade":
                    result = handleUpgrade(body, payload, sharedResponse);
                    break;
                case "/temporary-token":
                    result = handleTemporaryToken(body, payload, sharedResponse);
                    break;
                default:
                    result = ReturnResult.error("不支持的路由: " + route);
            }

            String responseBody = Json.toJson(result);
            // 把响应头（如 x-oauth-response-serial）放到 metadata 里传给客户端
            Map<String, String> responseHeaders = sharedResponse.getAllHeaders();
            if (!responseHeaders.isEmpty()) {
                String responseMeta = Json.toJson(responseHeaders);
                return Mono.just(DefaultPayload.create(responseBody, responseMeta));
            }
            return Mono.just(DefaultPayload.create(responseBody));

        } catch (Exception e) {
            log.error("RSocket 请求处理失败", e);
            ReturnResult<String> errorResult = ReturnResult.error("请求处理失败: " + e.getMessage());
            return Mono.just(DefaultPayload.create(Json.toJson(errorResult)));
        }
    }

    /**
     * 处理 OAuth 认证请求
     */
    private ReturnResult<String> handleOauth(String body, Payload payload, RsocketAuthInformation.RsocketResponse sharedResponse) {
        try {
            AuthRequest authRequest = Json.fromJson(body, AuthRequest.class);

            // 创建适配器请求/响应对象
            RsocketAuthInformation.RsocketRequest rsocketRequest = createRsocketRequest(payload);

            // 创建认证信息
            OauthAuthInformation authInformation = new OauthAuthInformation(
                    authServerProperties,
                    authRequest.getData(),
                    rsocketRequest,
                    sharedResponse,
                    applicationContext,
                    loginCheck);

            // 验证签名
            if (!authInformation.isSuccessful()) {
                return ReturnResult.illegal(authInformation.errorMessage());
            }

            // 执行认证
            OauthAuthorization authorization = authInformation.authorization();
            return authorization.authentication();

        } catch (Exception e) {
            log.error("OAuth 认证失败", e);
            return ReturnResult.error("OAuth认证失败: " + e.getMessage());
        }
    }

    /**
     * 处理登录请求
     */
    private ReturnResult<String> handleLogin(String body, Payload payload, RsocketAuthInformation.RsocketResponse sharedResponse) {
        try {
            AuthRequest authRequest = Json.fromJson(body, AuthRequest.class);
            if (authRequest == null) authRequest = new AuthRequest();

            RsocketAuthInformation.RsocketRequest rsocketRequest = createRsocketRequest(payload);

            // RsocketRequest 实现了 HttpServletRequest，可直接使用 HTTP 构造函数
            LoginAuthInformation authInformation = new LoginAuthInformation(
                    authServerProperties, authRequest.getData(), (jakarta.servlet.http.HttpServletRequest) rsocketRequest,
                    (jakarta.servlet.http.HttpServletResponse) sharedResponse, applicationContext, loginCheck);

            if (!authInformation.isSuccessful()) {
                return ReturnResult.illegal(authInformation.errorMessage());
            }

            return authInformation.authorization().authentication();
        } catch (Exception e) {
            log.error("[RSocket] 登录失败", e);
            return ReturnResult.error("登录失败: " + e.getMessage());
        }
    }

    /**
     * 处理登出请求
     */
    private ReturnResult<String> handleLogout(String body, Payload payload, RsocketAuthInformation.RsocketResponse sharedResponse) {
        try {
            AuthRequest authRequest = Json.fromJson(body, AuthRequest.class);
            if (authRequest == null) authRequest = new AuthRequest();

            RsocketAuthInformation.RsocketRequest rsocketRequest = createRsocketRequest(payload);

            LogoutAuthInformation authInformation = new LogoutAuthInformation(
                    authServerProperties, authRequest.getData(), (jakarta.servlet.http.HttpServletRequest) rsocketRequest,
                    (jakarta.servlet.http.HttpServletResponse) sharedResponse, applicationContext, loginCheck);

            if (!authInformation.isSuccessful()) {
                return ReturnResult.illegal(authInformation.errorMessage());
            }

            return authInformation.authorization().authentication();
        } catch (Exception e) {
            log.error("[RSocket] 登出失败", e);
            return ReturnResult.error("登出失败: " + e.getMessage());
        }
    }

    /**
     * 处理升级请求（token 刷新/升级）
     */
    private ReturnResult<String> handleUpgrade(String body, Payload payload, RsocketAuthInformation.RsocketResponse sharedResponse) {
        try {
            AuthRequest authRequest = Json.fromJson(body, AuthRequest.class);
            if (authRequest == null) authRequest = new AuthRequest();

            RsocketAuthInformation.RsocketRequest rsocketRequest = createRsocketRequest(payload);

            UpgradeAuthInformation authInformation = new UpgradeAuthInformation(
                    authServerProperties, authRequest.getData(), (jakarta.servlet.http.HttpServletRequest) rsocketRequest,
                    (jakarta.servlet.http.HttpServletResponse) sharedResponse, applicationContext, loginCheck);

            if (!authInformation.isSuccessful()) {
                return ReturnResult.illegal(authInformation.errorMessage());
            }

            return authInformation.authorization().authentication();
        } catch (Exception e) {
            log.error("[RSocket] Token 升级失败", e);
            return ReturnResult.error("Token升级失败: " + e.getMessage());
        }
    }

    /**
     * 处理临时令牌请求
     */
    private ReturnResult<String> handleTemporaryToken(String body, Payload payload, RsocketAuthInformation.RsocketResponse sharedResponse) {
        try {
            AuthRequest authRequest = Json.fromJson(body, AuthRequest.class);
            if (authRequest == null) authRequest = new AuthRequest();

            RsocketAuthInformation.RsocketRequest rsocketRequest = createRsocketRequest(payload);

            TemporaryTokenAuthInformation authInformation = new TemporaryTokenAuthInformation(
                    authServerProperties, authRequest.getData(), (jakarta.servlet.http.HttpServletRequest) rsocketRequest,
                    (jakarta.servlet.http.HttpServletResponse) sharedResponse, applicationContext, loginCheck);

            if (!authInformation.isSuccessful()) {
                return ReturnResult.illegal(authInformation.errorMessage());
            }

            return authInformation.authorization().authentication();
        } catch (Exception e) {
            log.error("[RSocket] 临时令牌生成失败", e);
            return ReturnResult.error("临时令牌生成失败: " + e.getMessage());
        }
    }

    /**
     * 从 RSocket Payload 创建 RsocketRequest 适配器
     *
     * @param payload RSocket 负载
     * @return RsocketRequest 适配器对象
     */
    private RsocketAuthInformation.RsocketRequest createRsocketRequest(Payload payload) {
        // 获取客户端地址（从连接信息中获取）
        InetSocketAddress clientAddress = null;
        try {
            // RSocket 连接信息需要通过其他方式获取
            // 这里先使用默认值
            clientAddress = new InetSocketAddress("127.0.0.1", 0);
        } catch (Exception e) {
            log.warn("无法获取客户端地址", e);
        }

        RsocketAuthInformation.RsocketRequest rsocketRequest = new RsocketAuthInformation.RsocketRequest(clientAddress);

        // 从 Payload 的 metadata 中提取 Header
        // RSocket 的 metadata 通常包含路由和元数据信息
        String metadata = payload.getMetadataUtf8();
        if (metadata != null && !metadata.isEmpty()) {
            try {
                var metadataObj = com.chua.common.support.text.json.Json.getJsonObject(metadata);
                if (metadataObj != null) {
                    // 客户端发送的 metadata 是平铺 JSON，所有 x-oauth-* 字段直接作为 header
                    metadataObj.forEach((key, value) -> {
                        if (key != null && !key.equals("route") && value != null) {
                            rsocketRequest.setHeader(key, value.toString());
                        }
                    });
                    // 兼容旧格式：如果有 headers 子对象
                    if (metadataObj.containsKey("headers")) {
                        var headers = metadataObj.getJSONObject("headers");
                        if (headers != null) {
                            headers.forEach((key, value) -> {
                                if (value != null) {
                                    rsocketRequest.setHeader(key, value.toString());
                                }
                            });
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("解析 RSocket metadata 失败", e);
            }
        }

        // 设置请求体（原始 body，用于签名验证）
        String body = payload.getDataUtf8();
        rsocketRequest.setBody(body);
        
        // 将原始 body 存储到 attribute 中，供签名验证器使用
        rsocketRequest.setAttribute("requestBody", body);

        return rsocketRequest;
    }

    @Override
    public void destroy() throws Exception {
        shutdown();
    }

    /**
     * 关闭服务器
     */
    public void shutdown() {
        if (serverDisposable != null) {
            try {
                serverDisposable.dispose();
                log.info("RSocket OAuth 服务器已关闭");
            } catch (Exception e) {
                log.error("关闭 RSocket OAuth 服务器失败", e);
            }
        }
    }
}

