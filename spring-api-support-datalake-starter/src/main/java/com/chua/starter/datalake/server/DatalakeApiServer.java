package com.chua.starter.datalake.server;

import com.chua.common.support.network.protocol.server.ProtocolServer;
import com.chua.common.support.network.protocol.ServerSetting;
import com.chua.starter.datalake.bridge.DatalakeHttpBridge;
import com.chua.starter.datalake.service.DatalakeDataService;
import com.chua.starter.datalake.service.DatalakeMonitorService;
import com.chua.starter.datalake.service.PipelineService;

import java.util.concurrent.TimeUnit;

/**
 * 数据中台 API 服务器 — 基于 ProtocolServer 的纯 Java 启动入口
 */
public class DatalakeApiServer {

    private final int port;
    private final String contextPath;
    private ProtocolServer server;

    public DatalakeApiServer(int port, String contextPath) {
        this.port = port;
        this.contextPath = contextPath;
    }

    public DatalakeApiServer() {
        this(58081, "/v2/datalake");
    }

    public void start() throws Exception {
        // 诊断: 检查 NettyHttpProtocolServer 是否可加载
        try {
            Class<?> clazz = Class.forName("com.chua.netty.support.protocol.NettyHttpProtocolServer");
            System.out.println("[DIAG] Class.forName SUCCESS: " + clazz.getName());
        } catch (Throwable e) {
            System.out.println("[DIAG] Class.forName FAILED: " + e);
        }

        // 诊断: 列出 META-INF/extensions 资源
        try {
            var urls = Thread.currentThread().getContextClassLoader()
                    .getResources("META-INF/extensions/com.chua.common.support.network.protocol.server.ProtocolServer");
            while (urls.hasMoreElements()) {
                System.out.println("[DIAG] Extensions URL: " + urls.nextElement());
            }
        } catch (Exception e) {
            System.out.println("[DIAG] getResources FAILED: " + e);
        }

        ServerSetting setting = ServerSetting.builder()
                .port(port)
                .host("0.0.0.0")
                .contextPath(contextPath)
                .allowOrigin(true)
                .addDefaultMapping(true)
                .build();

        server = ProtocolServer.create("http", setting);
        System.out.println("[DIAG] Server class: " + server.getClass().getName());

        // 实例化 Service 层
        PipelineService pipelineService = new PipelineService();
        DatalakeDataService dataService = new DatalakeDataService();
        DatalakeMonitorService monitorService = new DatalakeMonitorService(pipelineService, dataService);

        // 注册路由
        DatalakeHttpBridge bridge = new DatalakeHttpBridge(pipelineService, dataService, monitorService);
        bridge.register(server);

        server.start();
        System.out.println("[DatalakeApiServer] Started on http://0.0.0.0:" + port + contextPath);
    }

    public void stop() {
        if (server != null) {
            server.closeQuietly();
            System.out.println("[DatalakeApiServer] Stopped");
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 58081;
        String contextPath = "/v2/datalake";

        for (String arg : args) {
            if (arg.startsWith("--port=")) {
                port = Integer.parseInt(arg.substring(7));
            } else if (arg.startsWith("--context-path=")) {
                contextPath = arg.substring(15);
            }
        }

        DatalakeApiServer apiServer = new DatalakeApiServer(port, contextPath);
        apiServer.start();

        Runtime.getRuntime().addShutdownHook(new Thread(apiServer::stop));

        // 阻塞主线程
        Thread.currentThread().join();
    }
}
