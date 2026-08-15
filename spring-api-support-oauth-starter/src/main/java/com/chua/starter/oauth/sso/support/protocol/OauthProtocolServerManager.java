package com.chua.starter.oauth.sso.support.protocol;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.network.http.HttpHeader;
import com.chua.common.support.network.http.HttpMethod;
import com.chua.common.support.network.server.Server;
import com.chua.common.support.network.server.ServerSetting;
import com.chua.common.support.network.server.SyncServerListener;
import com.chua.common.support.network.server.filter.UrlMappingServerFilter;
import com.chua.common.support.network.server.request.ServerRequest;
import com.chua.common.support.network.server.response.ServerResponse;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.kcp.support.server.KcpServer;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import com.chua.starter.oauth.sso.support.properties.OauthProtocolServersProperties;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.AUTH_ROUTES;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.CONTENT_TYPE_JSON;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.DEFAULT_CONTEXT_PATH;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.DEFAULT_HOST;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.DEFAULT_PORT;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.DEFAULT_PROTOCOL;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.DEFAULT_TYPE;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.LOCALHOST;

/**
 * OAuth 认证服务多协议 Server 管理器
 *
 * <p>根据 {@code plugin.oauth.protocol-server.protocols[]} 配置启动多个基于 common-starter
 * {@code network.Server} 的协议 Server(HTTP/KCP/WS/UDP 等,可插拔扩展),
 * 把 OAuth 认证协议入口以多协议暴露,统一路由到 {@link OauthProtocolHandler}。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Slf4j
public class OauthProtocolServerManager implements AutoCloseable {

    /**
     * 已启动的 Server 映射(name -> Server)
     */
    private final Map<String, Server> serverMap = new ConcurrentHashMap<>();

    private final OauthProtocolServersProperties properties;
    private final AuthServerProperties authServerProperties;
    private final LoginCheck loginCheck;

    public OauthProtocolServerManager(OauthProtocolServersProperties properties,
                                      AuthServerProperties authServerProperties,
                                      LoginCheck loginCheck) {
        this.properties = properties;
        this.authServerProperties = authServerProperties;
        this.loginCheck = loginCheck;
        log.info("[OauthProtocolServer] 配置绑定: protocols={}",
                properties != null && properties.getProtocols() != null
                        ? properties.getProtocols().size() : 0);
    }

    /**
     * 启动所有启用的协议 Server
     */
    public void startAll() {
        var protocols = properties.getProtocols();
        if (protocols == null || protocols.isEmpty()) {
            log.info("[OauthProtocolServer] 未配置 protocols,使用默认 HTTP Server");
            protocols = List.of(defaultHttpServer());
        }
        for (var config : protocols) {
            if (!config.isEnable()) {
                log.info("[OauthProtocolServer] 跳过未启用的协议: {}", config.getName());
                continue;
            }
            try {
                startServer(config);
            } catch (Exception e) {
                log.error("[OauthProtocolServer] 启动协议失败: {} - {}", config.getName(), e.getMessage(), e);
            }
        }
    }

    /**
     * 启动单个协议 Server
     */
    private void startServer(OauthProtocolServersProperties.ProtocolServer config) throws Exception {
        var setting = ServerSetting.defaults();
        setting.setHost(config.getHost());
        setting.setPort(config.getPort());
        setting.setProtocol(config.getProtocol());
        // contextPath 使用根路径,路由注册完整路径(/login 等),避免 JDK HttpServer context 前缀不匹配
        setting.setContextPath(DEFAULT_CONTEXT_PATH);

        var server = createServer(config.getType(), setting);
        // 注册 OAuth 认证路由(独立 UrlMappingServerFilter,避免依赖 Server 内部 urlMappingFilter 的初始化顺序)
        var handler = new OauthProtocolHandler(authServerProperties, loginCheck);
        // KCP 为消息型协议(topic:payload),不走 filter 链路,单独适配
        if (server instanceof KcpServer kcpServer) {
            startKcpServer(kcpServer, handler, config.getAuthRoutes());
            serverMap.put(config.getName(), server);
            log.info("[OauthProtocolServer] 已启动: {} -> {}:{} [{}], 协议: {}",
                    config.getName(), config.getHost(), config.getPort(),
                    "SUCCESS", config.getProtocol());
            return;
        }
        var urlMappingFilter = new UrlMappingServerFilter();
        var routes = config.getAuthRoutes() != null && !config.getAuthRoutes().isEmpty()
                ? config.getAuthRoutes() : Arrays.asList(AUTH_ROUTES);
        for (var p : routes) {
            urlMappingFilter.route(p, HttpMethod.POST, handler);
            // 兼容 client address 含 /oauth 前缀的调用
            urlMappingFilter.route("/oauth" + p, HttpMethod.POST, handler);
        }
        server.addFilter(urlMappingFilter);

        server.start();
        serverMap.put(config.getName(), server);
        log.info("[OauthProtocolServer] 已启动: {} -> {}:{} [{}], 协议: {}",
                config.getName(), config.getHost(), config.getPort(),
                "SUCCESS", config.getProtocol());
    }

    /**
     * 启动 KCP 认证适配(消息型协议)
     *
     * <p>KCP 报文格式为 {@code topic:payload},topic 对应认证路径(login/logout/verify 等),
     * payload 为 {@code {"data":"认证参数JSON或密文"}},认证结果通过 {@code send(clientId, topic, body)} 回传。</p>
     *
     * @param kcpServer KCP 服务器
     * @param handler   OAuth 认证处理器
     * @param routes    认证路由(topic)列表,为空时使用默认路由
     */
    private void startKcpServer(KcpServer kcpServer, OauthProtocolHandler handler, List<String> routes) {
        kcpServer.addListener(new SyncServerListener() {
            @Override
            public void onMessage(String clientId, String topic, Object message) {
                if (message == null) {
                    return;
                }
                // 仅处理配置的认证 topic,其余消息忽略
                var routeTopics = routes != null && !routes.isEmpty()
                        ? routes : Arrays.asList(AUTH_ROUTES);
                if (routeTopics.stream().noneMatch(r -> r.equals("/" + topic) || r.equals(topic))) {
                    return;
                }
                var body = String.valueOf(message);
                var request = new MessageServerRequest(topic, body);
                var response = new MessageServerResponse();
                try {
                    handler.handle(request, response);
                } catch (Exception e) {
                    log.error("[OauthProtocolServer] KCP 认证处理异常: topic={}", topic, e);
                    response.setBody(com.chua.common.support.lang.json.Json.toJson(
                            ReturnResult.error("服务处理异常")));
                }
                if (response.getBody() != null && clientId != null) {
                    kcpServer.send(clientId, topic, new String(response.getBody(), StandardCharsets.UTF_8));
                }
            }
        });
        kcpServer.start();
    }

    /**
     * 创建 Server 实例(支持 SPI type 与默认 jdk)
     */
    private Server createServer(String type, ServerSetting setting) {
        var serverType = (type == null || type.isEmpty()) ? DEFAULT_TYPE : type;
        var server = ServiceProvider.of(Server.class).getNewExtension(serverType, setting);
        if (server == null) {
            log.warn("[OauthProtocolServer] 未找到 Server 类型: {}, 回退到 {} Server", serverType, DEFAULT_TYPE);
            server = ServiceProvider.of(Server.class).getNewExtension(DEFAULT_TYPE, setting);
        }
        return server;
    }

    /**
     * 默认 HTTP Server 配置
     */
    private OauthProtocolServersProperties.ProtocolServer defaultHttpServer() {
        var config = new OauthProtocolServersProperties.ProtocolServer();
        config.setName(DEFAULT_PROTOCOL);
        config.setType(DEFAULT_TYPE);
        config.setProtocol(DEFAULT_PROTOCOL);
        config.setHost(DEFAULT_HOST);
        config.setPort(DEFAULT_PORT);
        config.setEnable(true);
        config.setContextPath(DEFAULT_CONTEXT_PATH);
        return config;
    }

    /**
     * 停止所有 Server
     */
    @Override
    public void close() {
        for (var entry : serverMap.entrySet()) {
            try {
                entry.getValue().stop();
                log.info("[OauthProtocolServer] 已停止: {}", entry.getKey());
            } catch (Exception e) {
                log.warn("[OauthProtocolServer] 停止失败: {} - {}", entry.getKey(), e.getMessage());
            }
        }
        serverMap.clear();
    }

    /**
     * 消息型协议(KCP)的轻量请求适配
     *
     * <p>将 {@code topic:payload} 报文的 topic 映射为请求路径,payload 作为请求体,
     * 使 {@link OauthProtocolHandler} 无需感知底层传输协议。</p>
     *
     * @author CH
     * @since 4.0.0.42
     */
    private static class MessageServerRequest implements ServerRequest {

        private final String topic;
        private final String body;
        private final Map<String, Object> attributes = new ConcurrentHashMap<>();

        MessageServerRequest(String topic, String body) {
            this.topic = topic;
            this.body = body;
        }

        @Override
        public String getUri() {
            return "/" + topic;
        }

        @Override
        public String getPath() {
            return "/" + topic;
        }

        @Override
        public HttpMethod getMethod() {
            return HttpMethod.POST;
        }

        @Override
        public String getHeader(String name) {
            return null;
        }

        @Override
        public HttpHeader getHeaders() {
            return HttpHeader.create();
        }

        @Override
        public Map<String, String> getParams() {
            return Map.of();
        }

        @Override
        public String getParam(String name) {
            return null;
        }

        @Override
        public String getContentType() {
            return CONTENT_TYPE_JSON;
        }

        @Override
        public long getContentLength() {
            return body != null ? body.getBytes(StandardCharsets.UTF_8).length : -1;
        }

        @Override
        public byte[] getBody() {
            return body != null ? body.getBytes(StandardCharsets.UTF_8) : new byte[0];
        }

        @Override
        public String getBodyString() {
            return body;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(getBody());
        }

        @Override
        public String getRemoteAddress() {
            return LOCALHOST;
        }

        @Override
        public int getRemotePort() {
            return 0;
        }

        @Override
        public Map<String, Object> getAttributes() {
            return attributes;
        }

        @Override
        public Object getAttribute(String name) {
            return attributes.get(name);
        }

        @Override
        public void setAttribute(String name, Object value) {
            attributes.put(name, value);
        }
    }

    /**
     * 消息型协议(KCP)的轻量响应适配
     *
     * <p>捕获 {@link OauthProtocolHandler} 写入的响应体,由 KCP 适配层回传客户端。</p>
     *
     * @author CH
     * @since 4.0.0.42
     */
    private static class MessageServerResponse implements ServerResponse {

        private int status = 200;
        private byte[] body;
        private boolean ended;

        @Override
        public ServerResponse setStatus(int statusCode) {
            this.status = statusCode;
            return this;
        }

        @Override
        public int getStatus() {
            return status;
        }

        @Override
        public ServerResponse setHeader(String name, String value) {
            return this;
        }

        @Override
        public String getHeader(String name) {
            return null;
        }

        @Override
        public HttpHeader getHeaders() {
            return HttpHeader.create();
        }

        @Override
        public ServerResponse setContentType(String contentType) {
            return this;
        }

        @Override
        public String getContentType() {
            return CONTENT_TYPE_JSON;
        }

        @Override
        public ServerResponse setBody(byte[] body) {
            this.body = body;
            return this;
        }

        @Override
        public ServerResponse setBody(String body) {
            this.body = body != null ? body.getBytes(StandardCharsets.UTF_8) : null;
            return this;
        }

        @Override
        public byte[] getBody() {
            return body;
        }

        @Override
        public OutputStream getOutputStream() {
            return new ByteArrayOutputStream();
        }

        @Override
        public ServerResponse sendRedirect(String location) {
            return this;
        }

        @Override
        public ServerResponse sendError(int statusCode, String message) {
            this.status = statusCode;
            this.body = message != null ? message.getBytes(StandardCharsets.UTF_8) : null;
            this.ended = true;
            return this;
        }

        @Override
        public void flush() {
        }

        @Override
        public boolean isCommitted() {
            return ended;
        }

        @Override
        public boolean isEnded() {
            return ended;
        }

        @Override
        public ServerResponse reset() {
            this.status = 200;
            this.body = null;
            this.ended = false;
            return this;
        }

        @Override
        public void end() {
            this.ended = true;
        }

        @Override
        public ServerResponse setResult(Object result) {
            if (result != null) {
                this.body = String.valueOf(result).getBytes(StandardCharsets.UTF_8);
            }
            return this;
        }

        @Override
        public Object getResult() {
            return body;
        }

        @Override
        public void writeRaw(byte[] bytes) {
            this.body = bytes;
        }
    }
}
