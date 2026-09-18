package com.chua.starter.oauth.sso.support.protocol;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.network.http.HttpHeader;
import com.chua.common.support.network.http.HttpMethod;
import com.chua.common.support.network.server.Server;
import com.chua.common.support.network.server.ServerSetting;
import com.chua.common.support.network.server.SyncServerListener;
import com.chua.common.support.network.server.filter.UrlMappingServerFilter;
import com.chua.common.support.network.server.request.AbstractServerRequest;
import com.chua.common.support.network.server.response.AbstractServerResponse;
import com.chua.common.support.network.server.response.ServerResponse;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.kcp.support.server.KcpServer;
import com.chua.rsocket.support.server.RSocketServer;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import com.chua.starter.oauth.sso.support.properties.OauthProtocolServersProperties;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.AUTH_ROUTES;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.DEFAULT_CONTEXT_PATH;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.DEFAULT_HOST;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.DEFAULT_PORT;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.DEFAULT_PROTOCOL;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.DEFAULT_TYPE;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.KCP_REQ_PREFIX;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.KCP_RESP_PREFIX;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.KCP_SEPARATOR;
import static com.chua.starter.oauth.sso.support.protocol.OauthProtocolConstants.LOCALHOST;

/**
 * OAuth 认证服务多协议 服务端 管理器
 *
 * <p>根据 {@code plugin.oauth.protocol-server.protocols[]} 配置启动多个基于 common-starter
 * {@code network.Server} 的协议 Server(HTTP/KCP/WS/UDP 等,可插拔扩展),
 * 把 OAuth 认证协议入口以多协议暴露,统一路由到 {@link OauthProtocolHandler}。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@Slf4j
public class OauthProtocolServerManager implements AutoCloseable {

    /**
     * 已启动的 服务端 映射(名称 -> 服务端)
     */
    private final Map<String, Server> serverMap = new ConcurrentHashMap<>();

    /** 属性 */
    private final OauthProtocolServersProperties properties;
    /** 认证服务器属性 */
    private final AuthServerProperties authServerProperties;
    /** Logincheck */
    private final LoginCheck loginCheck;

    /**
    * 创建 oauth协议服务端管理器 实例
    * @param properties 属性
    * @param authServerProperties 认证服务端属性
    * @param loginCheck login检查
     */
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
     * 启动所有启用的协议 服务端
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
     * 启动单个协议 服务端
     * @param config 配置
     */
    private void startServer(OauthProtocolServersProperties.ProtocolServer config) throws Exception {
        var setting = ServerSetting.defaults();
        // 并发承载压测:放大并发信号量,避免 autoConfig 按 CPU 核数收敛到较小上限(2核→32)
        // 默认 0 表示不启用并发限制;设为较大值以充分暴露真实认证瓶颈,而非协议层限流
        setting.setMaxConcurrency(0);
        setting.setHost(config.getHost());
        setting.setPort(config.getPort());
        setting.setProtocol(config.getProtocol());
 // 强制使用阻塞式过滤链:响应式链中 urlmapping服务端过滤器 异步执行 处理器,
        // 存在响应竞态(响应体未写入时响应已返回)。阻塞式链同步执行,保证响应完整。
        setting.setReactor(false);
        // contextPath 使用根路径,路由注册完整路径(/login 等),避免 JDK HttpServer context 前缀不匹配
        setting.setContextPath(DEFAULT_CONTEXT_PATH);

        var server = createServer(config.getType(), setting);
        // 注册 OAuth 认证路由(独立 UrlMappingServerFilter,避免依赖 Server 内部 urlMappingFilter 的初始化顺序)
        var handler = new OauthProtocolHandler(authServerProperties, loginCheck);
        // TCP 帧协议(TcpServer 接口):帧格式为 topic:body(如 login:{...} 或 verify:{...}),
 // 无 topic 前缀时默认 login,兼容旧版纯 主体 帧
        if (server instanceof com.chua.common.support.network.tcp.TcpServer tcpServer) {
            tcpServer.setHandler(request -> {
                String frame = new String(request, StandardCharsets.UTF_8);
                String topic = "login";
                String frameBody = frame;
                int colon = frame.indexOf(':');
                if (colon > 0 && !frame.startsWith("{\"") && !frame.startsWith("{")) {
                    topic = frame.substring(0, colon);
                    frameBody = frame.substring(colon + 1);
                }
                var req = new MessageServerRequest(topic, frameBody);
                var resp = new MessageServerResponse();
                try {
                    handler.handle(req, resp);
                } catch (Exception e) {
                    log.error("[OauthProtocolServer] TCP 认证异常 topic={}", topic, e);
                    resp.setBody(com.chua.common.support.lang.json.Json.toJson(
                            ReturnResult.error("认证处理异常")).getBytes(StandardCharsets.UTF_8));
                }
                return resp.getBody();
            });
            tcpServer.start();
            serverMap.put(config.getName(), server);
            log.info("[OauthProtocolServer] 已启动: {} -> {}:{} [{}], 协议: {}",
                    config.getName(), config.getHost(), config.getPort(),
                    "SUCCESS", config.getProtocol());
            return;
        }
        // KCP 为消息型协议(topic:payload),不走 filter 链路,单独适配
        if (server instanceof KcpServer kcpServer) {
            startKcpServer(kcpServer, handler, config.getAuthRoutes());
            serverMap.put(config.getName(), server);
            log.info("[OauthProtocolServer] 已启动: {} -> {}:{} [{}], 协议: {}",
                    config.getName(), config.getHost(), config.getPort(),
                    "SUCCESS", config.getProtocol());
            return;
        }
        // RSocket 为消息型协议(request-response),不走 filter 链路,单独适配
        if (server instanceof RSocketServer rsocketServer) {
            startRsocketServer(rsocketServer, handler, config.getAuthRoutes());
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
 // 兼容 客户端 地址 含 /OAuth 前缀的调用
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
     * <p>KCP 使用 topic:payload 模型,topic 对应认证路径(login/logout/verify 等),
     * payload 为 {@code {"data":"认证参数JSON或密文"}},认证结果通过 {@code send(clientId, topic, body)} 回传。</p>
     *
     * @param kcpServer KCP 服务器
     * @param handler   OAuth 认证处理器
     * @param routes    认证路由(topic)列表,为空时使用默认路由
     */
    private void startKcpServer(KcpServer kcpServer, OauthProtocolHandler handler, List<String> routes) {
        kcpServer.addListener(new SyncServerListener() {
            @Override
            /** on消息 */
            public void onMessage(String clientId, String topic, Object message) {
                if (message == null) {
                    return;
                }
                // 支持 req/<topic> 请求-响应协议:req/login:requestId|payload
                var rawTopic = topic;
                var requestId = "";
                var rawMessage = String.valueOf(message);
                if (topic.startsWith(KCP_REQ_PREFIX)) {
                    rawTopic = topic.substring(KCP_REQ_PREFIX.length());
                    int separatorIndex = rawMessage.indexOf(KCP_SEPARATOR);
                    if (separatorIndex > 0) {
                        requestId = rawMessage.substring(0, separatorIndex);
                        rawMessage = rawMessage.substring(separatorIndex + 1);
                    }
                }
                final var finalTopic = rawTopic;
                final var finalMessage = rawMessage;
                // 仅处理配置的认证 topic,其余消息忽略
                var routeTopics = routes != null && !routes.isEmpty()
                        ? routes : Arrays.asList(AUTH_ROUTES);
                if (routeTopics.stream().noneMatch(r -> r.equals("/" + finalTopic) || r.equals(finalTopic))) {
                    return;
                }
                var request = new MessageServerRequest(finalTopic, finalMessage);
                var response = new MessageServerResponse();
                try {
                    handler.handle(request, response);
                } catch (Exception e) {
                    log.error("[OauthProtocolServer] KCP 认证处理异常: topic={}", topic, e);
                    response.setBody(com.chua.common.support.lang.json.Json.toJson(
                            ReturnResult.error("服务处理异常")));
                }
                if (response.getBody() != null && clientId != null) {
                    var respTopic = rawTopic.startsWith("/") ? rawTopic.substring(1) : rawTopic;
                    var responseText = new String(response.getBody(), StandardCharsets.UTF_8);
 // 请求-响应协议回传 reqid|主体,普通消息直接回传 主体
                    kcpServer.send(clientId,
                            KCP_RESP_PREFIX + respTopic,
                            requestId.isEmpty() ? responseText : requestId + KCP_SEPARATOR + responseText);
                }
            }
        });
        kcpServer.start();
    }

    /**
     * 启动 r套接字 认证适配(消息型协议)
     *
     * <p>RSocket 使用 request-response 模型,topic 对应认证路径(login/logout/verify 等),
     * payload 为 {@code {"data":"认证参数JSON或密文"}},认证结果作为响应体返回。</p>
     *
     * @param rsocketServer r套接字 服务器
     * @param handler       OAuth 认证处理器
     * @param routes        认证路由(topic)列表,为空时使用默认路由
     */
    private void startRsocketServer(RSocketServer rsocketServer, OauthProtocolHandler handler, List<String> routes) {
        var routeTopics = routes != null && !routes.isEmpty()
                ? routes : Arrays.asList(AUTH_ROUTES);
        for (var topic : routeTopics) {
            // RSocket 客户端 route 不带前导斜杠(如 login),这里统一注册两种形式
            var rawTopic = topic.startsWith("/") ? topic.substring(1) : topic;
            rsocketServer.onRequest(rawTopic, (request, response) -> {
                try {
                    handler.handle(request, response);
                } catch (Exception e) {
                    log.error("[OauthProtocolServer] RSocket 认证处理异常: topic={}", topic, e);
                    response.setBody(com.chua.common.support.lang.json.Json.toJson(
                            ReturnResult.error("服务处理异常")));
                }
            });
        }
        rsocketServer.start();
    }

    /**
     * 创建 服务端 实例(支持 SPI 类型 与默认 jdk)
     * @param type 类型
     * @param setting setting
     * @return 创建服务端的结果
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
     * 默认 HTTP 服务端 配置
     * @return 默认http服务端的结果
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
     * 停止所有 服务端
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
     * <p>继承 {@link AbstractServerRequest},仅需实现协议特定的数据获取方法;
     * 将 {@code topic:payload} 报文的 topic 映射为请求路径,payload 作为请求体,
     * 使 {@link OauthProtocolHandler} 无需感知底层传输协议。</p>
     *
     * @since 4.0.0.42
     * @author CH
     */
    private static class MessageServerRequest extends AbstractServerRequest {

        /** Topic */
        private final String topic;
        /** 请求体 */
        private final byte[] body;

        MessageServerRequest(String topic, String body) {
            this.topic = topic;
            this.body = body != null ? body.getBytes(StandardCharsets.UTF_8) : new byte[0];
        }

        @Override
        /** 读取主体 */
        protected byte[] readBody() {
            return body;
        }

        @Override
        /** 获取头部 */
        public HttpHeader getHeaders() {
            return HttpHeader.create();
        }

        @Override
        /** 获取头部 */
        public String getHeader(String name) {
            return null;
        }

        @Override
        /** 获取Uri */
        public String getUri() {
            return "/" + topic;
        }

        @Override
        /** 获取路径 */
        public String getPath() {
            return "/" + topic;
        }

        @Override
        /** 获取方法 */
        public HttpMethod getMethod() {
            return HttpMethod.POST;
        }

        @Override
        /** 获取远程地址 */
        public String getRemoteAddress() {
            return LOCALHOST;
        }

        @Override
        /** 获取远程端口 */
        public int getRemotePort() {
            return 0;
        }
    }

    /**
     * 消息型协议(KCP)的轻量响应适配
     *
     * <p>继承 {@link AbstractServerResponse},仅实现协议特定的输出方法;
     * 捕获 {@link OauthProtocolHandler} 写入的响应体,由 KCP 适配层回传客户端。</p>
     *
     * @since 4.0.0.42
     * @author CH
     */
    private static class MessageServerResponse extends AbstractServerResponse {

        @Override
        /** 获取输出流 */
        public OutputStream getOutputStream() {
            return new ByteArrayOutputStream();
        }

        @Override
        /** 写入Raw */
        public void writeRaw(byte[] bytes) {
            this.body = bytes;
        }
    }
}
