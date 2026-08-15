package com.chua.starter.oauth.sso.support.protocol;

import com.chua.common.support.network.http.HttpMethod;
import com.chua.common.support.network.server.Server;
import com.chua.common.support.network.server.ServerSetting;
import com.chua.common.support.network.server.filter.UrlMappingServerFilter;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import com.chua.starter.oauth.sso.support.properties.OauthProtocolServersProperties;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
        setting.setContextPath("/");

        var server = createServer(config.getType(), setting);
        // 注册 OAuth 认证路由(独立 UrlMappingServerFilter,避免依赖 Server 内部 urlMappingFilter 的初始化顺序)
        var handler = new OauthProtocolHandler(authServerProperties, loginCheck);
        var urlMappingFilter = new UrlMappingServerFilter();
        for (var p : new String[]{"/login", "/logout", "/upgrade", "/temporary-token", "/verify", "/oauth"}) {
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
     * 创建 Server 实例(支持 SPI type 与默认 jdk)
     */
    private Server createServer(String type, ServerSetting setting) {
        var serverType = (type == null || type.isEmpty()) ? "jdk" : type;
        var server = ServiceProvider.of(Server.class).getNewExtension(serverType, setting);
        if (server == null) {
            log.warn("[OauthProtocolServer] 未找到 Server 类型: {}, 回退到 JDK HTTP Server", serverType);
            server = ServiceProvider.of(Server.class).getNewExtension("jdk", setting);
        }
        return server;
    }

    /**
     * 默认 HTTP Server 配置
     */
    private OauthProtocolServersProperties.ProtocolServer defaultHttpServer() {
        var config = new OauthProtocolServersProperties.ProtocolServer();
        config.setName("http");
        config.setType("jdk");
        config.setProtocol("http");
        config.setHost("0.0.0.0");
        config.setPort(19180);
        config.setEnable(true);
        config.setContextPath("/");
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
}
