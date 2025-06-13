package com.chua.starter.oauth.sso.support.protocol;

import com.chua.common.support.annotations.Extension;
import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.oauth.client.support.entity.AuthRequest;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.condition.OnBeanCondition;
import com.chua.starter.oauth.sso.support.information.AuthInformation;
import com.chua.starter.oauth.sso.support.parser.Authorization;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import com.chua.starter.oauth.sso.support.provider.LoginProvider;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.chua.common.support.lang.code.ReturnCode.OK;

/**
 * WebSocket协议实现
 * <p>
 * 提供基于WebSocket的认证服务，支持实时、持久的认证连接。
 * 相比HTTP协议，WebSocket协议具有以下优势：
 * </p>
 * <ul>
 *   <li>保持长连接，减少连接建立开销</li>
 *   <li>支持服务器推送，可实现实时会话状态更新</li>
 *   <li>减少请求头开销，提高传输效率</li>
 *   <li>适合需要实时认证状态更新的场景</li>
 * </ul>
 *
 * @author CH
 */
@Controller
@Extension("websocket")
@Slf4j
@Conditional(OnBeanCondition.class)
@EnableWebSocket
@RequiredArgsConstructor
@Tag(name = "WebSocket认证服务", description = "提供WebSocket协议的认证服务接口")
public class WebSocketProtocol extends TextWebSocketHandler implements Protocol, InitializingBean, WebSocketConfigurer {

    /**
     * 认证服务器配置
     */
    final AuthServerProperties authServerProperties;

    /**
     * 登录提供者
     */
    final LoginProvider loginProvider;

    /**
     * 登录检查
     */
    final LoginCheck loginCheck;

    /**
     * 应用上下文
     */
    final ApplicationContext applicationContext;
    /**
     * 活跃的WebSocket会话
     */
    private final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();
    /**
     * 上下文路径
     */
    @Value("${plugin.auth.server.context-path:}")
    private String contextPath;

    /**
     * 配置WebSocket处理器
     *
     * @param registry WebSocket处理器注册表
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册WebSocket处理器，路径为/ws/oauth
        registry.addHandler(this, contextPath + "/ws/oauth")
                .setAllowedOrigins("*"); // 允许所有来源的连接，生产环境应该限制
    }

    /**
     * 处理WebSocket连接建立
     *
     * @param session WebSocket会话
     * @throws Exception 异常信息
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.debug("WebSocket连接已建立: {}, 远程地址: {}", session.getId(), session.getRemoteAddress());
        activeSessions.put(session.getId(), session);
    }

    /**
     * 处理WebSocket文本消息
     *
     * @param session WebSocket会话
     * @param message 接收到的消息
     * @throws Exception 异常信息
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("收到WebSocket消息: {}", payload);

        // 处理心跳请求
        if ("PING".equals(payload)) {
            session.sendMessage(new TextMessage("PONG"));
            return;
        }

        try {
            // 解析请求
            AuthRequest request = Json.fromJson(payload, AuthRequest.class);
            String data = request.getData();

            // 获取客户端地址
            String address = session.getRemoteAddress().getAddress().getHostAddress();

            // 根据消息类型处理不同的请求
            Map<String, Object> requestData = Json.fromJson(data, Map.class);
            String type = (String) requestData.getOrDefault("type", "approve");

            ReturnResult<String> result;
            if ("upgrade".equals(type)) {
                result = handleUpgradeRequest(data, address);
            } else {
                result = handleApproveRequest(request, address);
            }

            // 发送响应
            session.sendMessage(new TextMessage(Json.toJson(result)));
        } catch (Exception e) {
            log.error("处理WebSocket消息异常", e);
            session.sendMessage(new TextMessage(Json.toJson(ReturnResult.noAuth())));
        }
    }

    /**
     * 处理认证请求
     *
     * @param request 认证请求
     * @param address 客户端地址
     * @return 认证结果
     */
    private ReturnResult<String> handleApproveRequest(AuthRequest request, String address) {
        String data = request.getData();
        AuthInformation authInformation = new AuthInformation(request.isEncipher(), data, null, authServerProperties, applicationContext, loginCheck);
        Authorization authorization = authInformation.resolve();

        if (!authorization.hasKey()) {
            return ReturnResult.noAuth();
        }

        if (!authorization.hasTokenOrCookie()) {
            return ReturnResult.noAuth();
        }

        ReturnResult<String> authentication = authorization.authentication(request.isEncipher());
        if (!OK.getCode().equals(authentication.getCode())) {
            return ReturnResult.noAuth();
        }

        return authentication;
    }

    /**
     * 处理升级请求
     *
     * @param data    请求数据
     * @param address 客户端地址
     * @return 升级结果
     */
    private ReturnResult<String> handleUpgradeRequest(String data, String address) {
        AuthInformation authInformation = new AuthInformation(true, data, null, authServerProperties, applicationContext, loginCheck);
        Authorization authorization = authInformation.resolve();

        if (!authorization.hasRefreshToken()) {
            return ReturnResult.noAuth();
        }

        if (!authorization.hasKey()) {
            return ReturnResult.noAuth();
        }

        if (!authorization.hasTokenOrCookie()) {
            return ReturnResult.noAuth();
        }

        ReturnResult<String> authentication = authorization.upgrade(address, authServerProperties.getCookieName());
        if (!OK.getCode().equals(authentication.getCode())) {
            return ReturnResult.noAuth();
        }

        return authentication;
    }

    /**
     * 处理WebSocket连接关闭
     *
     * @param session WebSocket会话
     * @param status  关闭状态
     * @throws Exception 异常信息
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.debug("WebSocket连接已关闭: {}, 状态: {}", session.getId(), status);
        activeSessions.remove(session.getId());
    }

    /**
     * 处理WebSocket传输错误
     *
     * @param session   WebSocket会话
     * @param exception 异常信息
     * @throws Exception 异常信息
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输错误: {}", session.getId(), exception);
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
        activeSessions.remove(session.getId());
    }

    /**
     * 向指定会话发送消息
     *
     * @param sessionId 会话ID
     * @param message   消息内容
     * @return 是否发送成功
     */
    public boolean sendMessage(String sessionId, String message) {
        WebSocketSession session = activeSessions.get(sessionId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
                return true;
            } catch (IOException e) {
                log.error("发送WebSocket消息失败: {}", sessionId, e);
            }
        }
        return false;
    }

    /**
     * 广播消息到所有活跃会话
     *
     * @param message 消息内容
     * @return 成功发送的会话数
     */
    public int broadcastMessage(String message) {
        int successCount = 0;
        for (WebSocketSession session : activeSessions.values()) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                    successCount++;
                } catch (IOException e) {
                    log.error("广播WebSocket消息失败: {}", session.getId(), e);
                }
            }
        }
        return successCount;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("WebSocket认证服务已初始化，路径: {}/ws/oauth", contextPath);
    }
} 