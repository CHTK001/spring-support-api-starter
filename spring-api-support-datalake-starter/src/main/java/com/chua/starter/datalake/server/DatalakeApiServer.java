package com.chua.starter.datalake.server;

import com.chua.common.support.core.spi.ServiceProvider;
import com.chua.common.support.metric.MetricsReporter;
import com.chua.common.support.network.protocol.server.AbstractTopicProtocolServer;
import com.chua.common.support.network.protocol.server.ProtocolServer;
import com.chua.common.support.network.protocol.ServerSetting;
import com.chua.common.support.network.protocol.filter.ServletFilter;
import com.chua.common.support.text.json.Json;
import com.chua.common.support.text.json.JsonObject;
import com.chua.datalake.support.common.BackpressureBuffer;
import com.chua.datalake.support.common.LocalDeduplicator;
import com.chua.datalake.support.engine.DataSourcePipelineManager;
import com.chua.datalake.support.engine.ScGraph;
import com.chua.datalake.support.engine.ScGraphEngine;
import com.chua.datalake.support.engine.ScGraphNode;
import com.chua.datalake.support.envelope.DataEnvelope;
import com.chua.datalake.support.model.CollectData;
import com.chua.datalake.support.spi.auth.RateLimiter;
import com.chua.datalake.support.spi.auth.TokenService;
import com.chua.datalake.support.spi.collection.Collector;
import com.chua.datalake.support.spi.pipeline.*;
import com.chua.datalake.support.spi.storage.DataSink;
import com.chua.datalake.support.spi.storage.DataSource;
import com.chua.datalake.support.spi.storage.FilePipelineDataSource;
import com.chua.datalake.support.spi.storage.PipelineDataSource;
import com.chua.datalake.support.spi.subscription.Subscriber;
import com.chua.starter.datalake.bridge.DatalakeHttpBridge;
import com.chua.starter.datalake.collection.HttpCollector;
import com.chua.starter.datalake.model.MonitorStats;
import com.chua.starter.datalake.server.auth.AuthServletFilter;
import com.chua.starter.datalake.server.auth.FileTokenService;
import com.chua.starter.datalake.server.auth.LocalRateLimiter;
import com.chua.starter.datalake.service.DatalakeDataService;
import com.chua.starter.datalake.service.DatalakeMonitorService;
import com.chua.starter.datalake.service.DatalakeReporter;
import com.chua.starter.datalake.service.PipelineService;
import com.chua.starter.datalake.subscription.HttpSubscriber;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 数据中台 API 服务器 — 基于 ProtocolServer 的纯 Java 启动入口
 *
 * <p><b>端口分配表 (58081-58086):</b></p>
 * <ul>
 *   <li><b>58081</b> — HTTP API 主端口，REST 路由注册 + 静态文件 SPA 服务 + HTTP 采集/订阅共享此端口</li>
 *   <li><b>58082</b> — WebSocket 端口，实时推送监控数据（MonitorStats）到前端仪表盘</li>
 *   <li><b>58083</b> — TCP 采集端口 (TcpCollector)，接收 TCP socket 客户端上报的逐行数据</li>
 *   <li><b>58084</b> — UDP 采集端口 (UdpCollector)，接收 UDP 数据报格式的数据接入</li>
 *   <li><b>58085</b> — MQ 订阅端口 (MqSubscriber)，简易内存消息队列，支持 PUB/SUB/POLL 协议</li>
 *   <li><b>58086</b> — TCP 订阅端口 (TcpSubscriber)，长连接推送通道，服务端主动 PUSH 数据到客户端</li>
 * </ul>
 *
 * @author CH
 */
public class DatalakeApiServer {

    /** HTTP API 服务端口（默认 58081，与 HTTP 采集/订阅共享） */
    private final int port;
    /** REST API 上下文路径（默认 /v2/datalake） */
    private final String contextPath;
    /** WebSocket 服务端口（默认 58082，port + 1） */
    private final int wsPort;
    /** HTTP 协议服务器实例（ProtocolServer，smart-http/Netty 封装） */
    private ProtocolServer server;
    /** WebSocket 主题协议服务器实例，用于广播监控数据到前端 */
    private AbstractTopicProtocolServer wsServer;
    /** WS 监控数据推送定时器，每 5 秒广播 MonitorStats 到所有 WS 客户端 */
    private ScheduledExecutorService wsPushScheduler;
    /** 指标上报器，定期采集并上报管线/系统 Metrics */
    private DatalakeReporter reporter;

    /**
     * 完整构造函数
     *
     * @param port        HTTP API 服务端口（58081）
     * @param contextPath REST API 上下文路径（/v2/datalake）
     * @param wsPort      WebSocket 服务端口（58082）
     */
    public DatalakeApiServer(int port, String contextPath, int wsPort) {
        this.port = port;
        this.contextPath = contextPath;
        this.wsPort = wsPort;
    }

    /**
     * 快捷构造函数，WebSocket 端口自动设为 port + 1
     *
     * @param port        HTTP API 服务端口
     * @param contextPath REST API 上下文路径
     */
    public DatalakeApiServer(int port, String contextPath) {
        this(port, contextPath, port + 1);
    }

    /**
     * 无参构造函数，使用默认值：port=58081, contextPath=/v2/datalake, wsPort=58082
     */
    public DatalakeApiServer() {
        this(58081, "/v2/datalake", 58082);
    }

    /**
     * 启动数据中台服务器
     * <ol>
     *   <li>创建 HTTP 服务器（58081），注册 SPI 发现的采集器/订阅器路由</li>
     *   <li>加载 SPI 组件：DataSink、Collector、Subscriber</li>
     *   <li>初始化默认管线 pipeline-management</li>
     *   <li>添加静态文件过滤器（DatalakeStaticFileFilter）用于 SPA 前端</li>
     *   <li>启动 WebSocket 服务器（58082），定时推送监控数据</li>
     * </ol>
     */
    @SuppressWarnings("rawtypes")
    public void start() throws Exception {
        // ── HTTP 服务器 ──
        ServerSetting httpSetting = ServerSetting.builder()
                .port(port)
                .host("0.0.0.0")
                .contextPath(contextPath)
                .allowOrigin(true)
                .addDefaultMapping(true)
                .build();

        server = ProtocolServer.create("http", httpSetting);

        // ── 实例化核心服务 ──
        DefaultParserRegistry parserRegistry = new DefaultParserRegistry();
        ScGraphEngine engine = new ScGraphEngine(parserRegistry);
        LocalDeduplicator deduplicator = new LocalDeduplicator();
        BackpressureBuffer buffer = new BackpressureBuffer();
        PipelineDataSource dataSource = new FilePipelineDataSource("data/pipelines");
        DataSourcePipelineManager pipelineManager = new DataSourcePipelineManager(dataSource, engine, deduplicator, buffer);

        // 注册默认 Sink
        var sinkProvider = ServiceProvider.of(DataSink.class);
        for (String name : sinkProvider.list().keySet()) {
            DataSink instance = sinkProvider.getExtension(name);
            if (instance != null) {
                pipelineManager.registerSink(name, instance);
                System.out.println("[DatalakeApiServer] 注册 sink: " + name);
            }
        }

        PipelineService pipelineService = new PipelineService(pipelineManager);
        DatalakeDataService dataService = new DatalakeDataService();
        DatalakeMonitorService monitorService = new DatalakeMonitorService(pipelineService, dataService);

        // ── 启动定时上报（MetricsReporter） ──
        MetricsReporter metricsReporter = MetricsReporter.getDefault();
        reporter = new DatalakeReporter(metricsReporter, pipelineService, monitorService, buffer);
        reporter.start();

        Map<String, List<Map<String, String>>> spiCatalog = buildSpiCatalog();

        // ── 注册 HTTP 路由 ──
        DatalakeHttpBridge bridge = new DatalakeHttpBridge(pipelineService, dataService, monitorService, contextPath, spiCatalog);
        bridge.register(server);

        initDefaultPipeline(pipelineManager);

        // ── 自动发现并启动采集器（Collector SPI） ──
        var collectorProvider = ServiceProvider.of(Collector.class);
        for (String name : collectorProvider.list().keySet()) {
            Collector collector = collectorProvider.getExtension(name);
            if (collector == null) continue;

            // 设置采集回调: CollectData → DataEnvelope → Pipeline
            String finalProtocol = collector.protocol();
            collector.setHandler(data -> handleCollectData(data, pipelineManager));

            if ("HTTP".equals(collector.protocol()) && collector instanceof HttpCollector httpCol) {
                // HTTP 采集共享 58081 端口，使用统一 collect(String) API 解析请求体
                httpCol.start(collector.defaultPort());
                startedCollectors.add(httpCol);
                server.post("/collect", (req, resp) -> {
                    byte[] body = req.getBody();
                    if (body == null || body.length == 0) {
                        writeJsonResponse(resp, 400, "Invalid request body");
                        return;
                    }
                    String json = new String(body, java.nio.charset.StandardCharsets.UTF_8);
                    httpCol.collect(json);
                    writeJsonResponse(resp, 200, Map.of("status", "ok", "protocol", "HTTP"));
                });
                System.out.println("[DatalakeApiServer] 注册 HTTP 采集路由: POST " + contextPath + "/collect");
            } else {
                // TCP/UDP 采集器启动独立端口
                try {
                    collector.start(collector.defaultPort());
                    System.out.println("[DatalakeApiServer] 采集器已启动: " + name
                            + " (" + collector.protocol() + ":" + collector.defaultPort() + ")");
                } catch (Exception e) {
                    System.err.println("[DatalakeApiServer] 采集器启动失败: " + name + " — " + e.getMessage());
                }
                startedCollectors.add(collector);
            }
        }

        // ── 自动发现并启动订阅器（Subscriber SPI） ──
        var subscriberProvider = ServiceProvider.of(Subscriber.class);
        for (String name : subscriberProvider.list().keySet()) {
            Subscriber subscriber = subscriberProvider.getExtension(name);
            if (subscriber == null) continue;

            if ("HTTP".equals(subscriber.protocol()) && subscriber instanceof HttpSubscriber httpSub) {
                // HTTP 订阅共享 58081 端口，注册轮询/注册路由
                httpSub.start(subscriber.defaultPort());
                server.post("/subscribe/register", (req, resp) -> {
                    JsonObject body = parseBody(req);
                    if (body == null) {
                        writeJsonResponse(resp, 400, "Invalid request body");
                        return;
                    }
                    String clientId = body.getString("clientId");
                    String pipelineId = body.getString("pipelineId");
                    Object topics = body.get("topics");
                    if (clientId != null && topics instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<String> topicList = (List<String>) topics;
                        httpSub.subscribe(clientId, pipelineId, topicList);
                    }
                    writeJsonResponse(resp, 200, Map.of("status", "ok"));
                });
                server.get("/subscribe/poll", (req, resp) -> {
                    String clientId = req.getParameter("clientId");
                    int maxBatch = 10;
                    String batchStr = req.getParameter("maxBatch");
                    if (batchStr != null) try { maxBatch = Integer.parseInt(batchStr); } catch (NumberFormatException ignored) {}

                    java.util.List<DataEnvelope> messages = clientId != null
                            ? httpSub.poll(clientId, maxBatch)
                            : java.util.Collections.emptyList();
                    writeJsonResponse(resp, 200, Map.of("messages", messages));
                });
                System.out.println("[DatalakeApiServer] 注册 HTTP 订阅路由: POST " + contextPath + "/subscribe/register"
                        + ", GET " + contextPath + "/subscribe/poll");
                startedSubscribers.add(httpSub);
            } else {
                // MQ/TCP 订阅器启动独立端口
                try {
                    subscriber.start(subscriber.defaultPort());
                    System.out.println("[DatalakeApiServer] 订阅器已启动: " + name
                            + " (" + subscriber.protocol() + ":" + subscriber.defaultPort() + ")");
                } catch (Exception e) {
                    System.err.println("[DatalakeApiServer] 订阅器启动失败: " + name + " — " + e.getMessage());
                }
                startedSubscribers.add(subscriber);
            }
        }

        // ── 初始化令牌服务和限流器 ──
        final TokenService tokenService = initTokenService();
        final RateLimiter rateLimiter = initRateLimiter();
        System.out.println("[DatalakeApiServer] 令牌服务: " + tokenService.getClass().getSimpleName()
                + " | 限流器: " + rateLimiter.getClass().getSimpleName() + " (默认 100 req/s)");

        // ── 注册鉴权过滤器（优先级 90，在 MappingServletFilter 之前运行） ──
        boolean authEnabled = isAuthEnabled();
        System.out.println("[DatalakeApiServer] Token 鉴权: " + (authEnabled ? "已启用" : "已禁用")
                + " (系统属性: datalake.auth.enabled=" + authEnabled + ")");
        if (authEnabled) {
            AuthServletFilter authFilter = new AuthServletFilter(tokenService, rateLimiter);
            server.addFilter(authFilter);
            System.out.println("[DatalakeApiServer] 鉴权过滤器已注册 (优先级: 90)");
        }

        // ── 注册令牌管理路由（免鉴权，AuthServletFilter 已跳过 /auth/token） ──
        final String authBase = contextPath + "/auth";
        server.post(authBase + "/token", (req, resp) -> {
            JsonObject body = parseBody(req);
            String description = body != null ? body.getString("description") : null;
            String newToken = tokenService.createToken(description);
            writeJsonResponse(resp, 200, Map.of("code", 0, "data", Map.of("token", newToken)));
        });
        server.delete(authBase + "/token", (req, resp) -> {
            String token = req.getParameter("token");
            if (token == null || token.isEmpty()) {
                writeJsonResponse(resp, 400, Map.of("code", 400, "message", "参数 token 不能为空"));
                return;
            }
            boolean revoked = tokenService.revokeToken(token);
            writeJsonResponse(resp, revoked ? 200 : 404, Map.of("code", revoked ? 0 : 404, "message", revoked ? "已吊销" : "令牌不存在"));
        });
        server.get(authBase + "/token", (req, resp) -> {
            var list = tokenService.listTokens();
            writeJsonResponse(resp, 200, Map.of("code", 0, "data", list));
        });
        System.out.println("[DatalakeApiServer] 令牌管理路由已注册: POST/DELETE/GET " + authBase + "/token");

        // ── 注册令牌校验路由（供外部系统校验 token 有效性） ──
        server.get(authBase + "/token/check", (req, resp) -> {
            String token = req.getParameter("token");
            boolean valid = tokenService.validateToken(token);
            writeJsonResponse(resp, 200, Map.of("code", 0, "data", Map.of("valid", valid)));
        });
        System.out.println("[DatalakeApiServer] 令牌校验路由已注册: GET " + authBase + "/token/check");

        // ── 添加静态文件过滤器（SPA 前端） ──
        DatalakeStaticFileFilter staticFilter = new DatalakeStaticFileFilter(
                new File("datalake-ui"), contextPath);
        server.addFilter(staticFilter);
        System.out.println("[DatalakeApiServer] 静态文件过滤器已注册 (baseDir: datalake-ui)");

        server.start();
        System.out.println("[DatalakeApiServer] HTTP 启动: http://0.0.0.0:" + port + contextPath);

        // ── WebSocket 服务器 ──
        ServerSetting wsSetting = ServerSetting.builder()
                .port(wsPort)
                .host("0.0.0.0")
                .contextPath(contextPath)
                .build();

        ProtocolServer rawWs = ProtocolServer.create("websocket", wsSetting);
        if (rawWs instanceof AbstractTopicProtocolServer topicWs) {
            wsServer = topicWs;
            wsServer.start();
            System.out.println("[DatalakeApiServer] WebSocket 启动: ws://0.0.0.0:" + wsPort + contextPath + "/ws");

            // 每 5 秒推送监控数据到所有 WS 客户端
            wsPushScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "ws-monitor-push");
                t.setDaemon(true);
                return t;
            });
            wsPushScheduler.scheduleAtFixedRate(() -> {
                try {
                    if (wsServer.getConnectionCount() == 0) return;
                    MonitorStats stats = monitorService.stats();
                    Map<String, Object> tpPayload = new LinkedHashMap<>();
                    tpPayload.put("data", monitorService.throughput());
                    Map<String, Object> errPayload = new LinkedHashMap<>();
                    errPayload.put("data", monitorService.errors());
                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("type", "monitor_update");
                    payload.put("stats", stats);
                    payload.put("throughput", tpPayload);
                    payload.put("errors", errPayload);
                    wsServer.broadcast(Json.toJson(payload));
                } catch (Exception ignored) {
                    // WS push failures are non-critical
                }
            }, 5, 5, TimeUnit.SECONDS);
        } else {
            System.out.println("[DatalakeApiServer] 警告: WebSocket 服务器不可用，跳过 WS 启动");
        }
    }

    /**
     * 初始化默认管线（pipeline-management）
     * <p>包含完整的 8 节点链路: source → parse → clean → standardize → transform → filter → router → sink</p>
     * <p>仅在管线不存在时创建，幂等操作</p>
     *
     * @param pipelineManager 管线管理器
     */
    private void initDefaultPipeline(DataSourcePipelineManager pipelineManager) {
        if (pipelineManager.getById("pipeline-management") != null) return;

        ScGraph graph = new ScGraph();
        graph.setId("pipeline-management");
        graph.setName("默认数据管线");
        graph.setVersion("1.0.0");
        graph.setDescription("数据中台默认管线 — 包含完整的数据接入 → 解析 → 清洗 → 标准化 → 转换 → 过滤 → 路由 → 输出链路");

        List<ScGraphNode> nodes = new ArrayList<>();

        ScGraphNode source = new ScGraphNode();
        source.setType("source");
        source.setSpi("http-source");
        source.setConfig(Map.of("protocol", "http"));
        nodes.add(source);

        ScGraphNode parse = new ScGraphNode();
        parse.setType("parse");
        parse.setSpi("json-parser");
        parse.setConfig(Map.of("strict", false, "rootPath", "$.data"));
        nodes.add(parse);

        ScGraphNode clean = new ScGraphNode();
        clean.setType("clean");
        clean.setSpi("default-cleaner");
        clean.setConfig(Map.of("removeNulls", true, "removeDuplicates", true, "trimStrings", true));
        nodes.add(clean);

        ScGraphNode standardize = new ScGraphNode();
        standardize.setType("standardize");
        standardize.setSpi("default-standardizer");
        standardize.setConfig(Map.of("dateFormat", "yyyy-MM-dd'T'HH:mm:ss", "timezone", "Asia/Shanghai"));
        nodes.add(standardize);

        ScGraphNode transform = new ScGraphNode();
        transform.setType("transform");
        transform.setSpi("default-transformer");
        transform.setConfig(Map.of("fieldMapping", Map.of(), "addFields", Map.of("source", "datalake")));
        nodes.add(transform);

        ScGraphNode filter = new ScGraphNode();
        filter.setType("filter");
        filter.setSpi("default-filter");
        filter.setConfig(Map.of("rules", List.of()));
        nodes.add(filter);

        ScGraphNode router = new ScGraphNode();
        router.setType("router");
        router.setSpi("condition-router");
        router.setConfig(Map.of("field", "value"));

        var cond1 = new ScGraphNode.RouteCondition();
        cond1.setWhen("value > 100");
        cond1.setTarget("alert-sink");
        var cond2 = new ScGraphNode.RouteCondition();
        cond2.setWhen("value <= 100");
        cond2.setTarget("normal-sink");
        router.setConditions(List.of(cond1, cond2));
        nodes.add(router);

        ScGraphNode sink = new ScGraphNode();
        sink.setType("sink");
        sink.setSpi("log-sink");
        sink.setConfig(Map.of("level", "info"));
        nodes.add(sink);

        graph.setNodes(nodes);

        Map<String, Map<String, Object>> sinkConfigs = new HashMap<>();
        sinkConfigs.put("normal-sink", Map.of("enabled", true, "type", "log", "level", "info"));
        sinkConfigs.put("alert-sink", Map.of("enabled", true, "type", "alert", "threshold", 100));
        sinkConfigs.put("storage-sink", Map.of("enabled", true, "type", "file", "path", "data/output"));
        graph.setSinks(sinkConfigs);

        pipelineManager.create(graph);
        System.out.println("[DatalakeApiServer] 默认管线初始化完成: pipeline-management (8节点: source→parse→clean→standardize→transform→filter→router→sink)");
    }

    /**
     * 构建 SPI 服务目录
     * <p>遍历所有管线节点类型（source/sink/filter/parse/clean/standardize/transform/enrich/aggregate），
     * 收集各 SPI 接口的可用实现列表，供前端动态渲染节点选择面板</p>
     *
     * @return SPI 目录 Map，key=节点类型，value=可用实现列表
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Map<String, List<Map<String, String>>> buildSpiCatalog() {
        Map<String, List<Map<String, String>>> catalog = new LinkedHashMap<>();
        java.util.function.BiConsumer<String, Class<?>> collect = (nodeType, spiClass) -> {
            var provider = (com.chua.common.support.core.spi.ServiceProvider)
                    com.chua.common.support.core.spi.ServiceProvider.of((Class) spiClass);
            Map<String, ?> spiMap = provider.list();
            List<Map<String, String>> items = new ArrayList<>();
            for (String name : spiMap.keySet()) {
                Map<String, String> entry = new LinkedHashMap<>();
                entry.put("name", name);
                items.add(entry);
            }
            catalog.put(nodeType, items);
        };
        collect.accept("source", com.chua.datalake.support.spi.storage.DataSource.class);
        collect.accept("sink", DataSink.class);
        collect.accept("filter", com.chua.datalake.support.spi.pipeline.DataFilter.class);
        collect.accept("parse", com.chua.datalake.support.spi.pipeline.DataParser.class);
        collect.accept("clean", com.chua.datalake.support.spi.pipeline.DataCleaner.class);
        collect.accept("standardize", com.chua.datalake.support.spi.pipeline.DataStandardizer.class);
        collect.accept("transform", com.chua.datalake.support.spi.pipeline.DataParser.class);
        collect.accept("enrich", com.chua.datalake.support.spi.pipeline.DataParser.class);
        collect.accept("aggregate", com.chua.datalake.support.spi.pipeline.DataParser.class);
        return catalog;
    }

    // ==================== 鉴权 & 限流初始化 ====================

    /**
     * 初始化令牌服务 — 优先 SPI 加载，回退直接实例化
     */
    private static TokenService initTokenService() {
        TokenService service;
        try {
            service = ServiceProvider.of(TokenService.class).getExtension("file-token-service");
        } catch (Exception e) {
            service = new FileTokenService();
        }
        if (service instanceof FileTokenService fts) {
            fts.init();
        }
        return service;
    }

    /**
     * 初始化限流器 — 优先 SPI 加载，回退直接实例化
     */
    private static RateLimiter initRateLimiter() {
        try {
            return ServiceProvider.of(RateLimiter.class).getExtension("local-rate-limiter");
        } catch (Exception e) {
            return new LocalRateLimiter();
        }
    }

    /**
     * 检查 Token 鉴权是否启用
     * <p>通过系统属性 {@code datalake.auth.enabled} 控制，默认 {@code true}。
     * 测试时可设置 {@code -Ddatalake.auth.enabled=false} 关闭鉴权。</p>
     *
     * @return true 启用鉴权（默认），false 关闭
     */
    private static boolean isAuthEnabled() {
        String prop = System.getProperty("datalake.auth.enabled");
        return prop == null || Boolean.parseBoolean(prop);
    }

    // ==================== 采集数据处理 ====================

    /** 已启动的采集器列表（用于 stop 时优雅关闭） */
    private final List<Collector> startedCollectors = new ArrayList<>();
    /** 已启动的订阅器列表（用于 stop 时优雅关闭） */
    private final List<Subscriber> startedSubscribers = new ArrayList<>();

    /**
     * 采集数据回调处理
     * <p>Collector 采集到数据后回调此方法，将 CollectData 转换为 DataEnvelope，
     * 提交给对应的 DataSourcePipelineManager 执行管线处理，
     * 管线执行完成后将结果推送到所有匹配的订阅器。</p>
     *
     * @param data            采集到的原始数据
     * @param pipelineManager 管线管理器
     */
    private void handleCollectData(CollectData data, DataSourcePipelineManager pipelineManager) {
        // 转换为 DataEnvelope
        DataEnvelope envelope = new DataEnvelope();
        if (data.getTopic() != null) envelope.addTopic(data.getTopic());
        if (data.getPipelineId() != null) envelope.setPipelineId(data.getPipelineId());
        if (data.getPayload() != null) {
            byte[] raw = data.getPayload() instanceof String s
                    ? s.getBytes(StandardCharsets.UTF_8)
                    : com.chua.common.support.text.json.Json.toJson(data.getPayload()).getBytes(StandardCharsets.UTF_8);
            envelope.setRawPayload(raw);
        }
        envelope.setFormatType(data.getFormatType());

        // 提交到管线
        String pipelineId = data.getPipelineId();
        if (pipelineId != null && pipelineManager.getById(pipelineId) != null) {
            try {
                pipelineManager.execute(pipelineId, envelope);
            } catch (Exception e) {
                System.err.println("[DatalakeApiServer] 采集数据处理失败: pipeline=" + pipelineId + " — " + e.getMessage());
                return;
            }
        }

        // 管线执行完成后，推送到所有匹配的订阅器
        for (Subscriber subscriber : startedSubscribers) {
            try {
                subscriber.push(null, envelope);
            } catch (Exception e) {
                System.err.println("[DatalakeApiServer] 订阅推送失败: " + subscriber.protocol() + " — " + e.getMessage());
            }
        }
    }

    // ==================== HTTP 工具方法 ====================

    /**
     * 解析 HTTP 请求体为 JSON 对象
     *
     * @param request HTTP Servlet 请求
     * @return 解析后的 JsonObject，请求体为空或解析失败时返回 null
     */
    private com.chua.common.support.text.json.JsonObject parseBody(com.chua.common.support.network.protocol.request.ServletRequest request) {
        try {
            byte[] body = request.getBody();
            if (body == null || body.length == 0) return null;
            return com.chua.common.support.text.json.JsonObject.parse(new String(body, java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 写入 JSON 格式的 HTTP 响应
     *
     * @param response HTTP Servlet 响应
     * @param status   HTTP 状态码（200/400/500 等）
     * @param data     响应数据对象，会被序列化为 JSON 字符串
     */
    private void writeJsonResponse(com.chua.common.support.network.protocol.request.ServletResponse response,
                                   int status, Object data) {
        response.setStatusCode(status);
        response.setContentType("application/json;charset=UTF-8");
        response.setBodyString(com.chua.common.support.text.json.Json.toJson(data));
    }

    /**
     * 优雅关闭服务器
     * <p>停止顺序：采集器 → 订阅器 → 指标上报器 → WS 推送定时器 → WebSocket → HTTP</p>
     */
    public void stop() {
        // 停止采集器
        for (Collector collector : startedCollectors) {
            try { collector.stop(); } catch (Exception ignored) {}
        }
        // 停止订阅器
        for (Subscriber subscriber : startedSubscribers) {
            try { subscriber.stop(); } catch (Exception ignored) {}
        }
        if (reporter != null) {
            reporter.stop();
        }
        if (wsPushScheduler != null) {
            wsPushScheduler.shutdown();
        }
        if (wsServer != null) {
            wsServer.closeQuietly();
            System.out.println("[DatalakeApiServer] WebSocket 已停止");
        }
        if (server != null) {
            server.closeQuietly();
            System.out.println("[DatalakeApiServer] HTTP 已停止");
        }
    }

    /**
     * 启动入口
     * <p>支持命令行参数覆盖默认配置：</p>
     * <ul>
     *   <li>{@code --port=58081} — HTTP API 端口</li>
     *   <li>{@code --ws-port=58082} — WebSocket 端口</li>
     *   <li>{@code --context-path=/v2/datalake} — REST 上下文路径</li>
     * </ul>
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) throws Exception {
        int port = 58081;
        int wsPort = 58082;
        String contextPath = "/v2/datalake";

        for (String arg : args) {
            if (arg.startsWith("--port=")) {
                port = Integer.parseInt(arg.substring(7));
            } else if (arg.startsWith("--ws-port=")) {
                wsPort = Integer.parseInt(arg.substring(10));
            } else if (arg.startsWith("--context-path=")) {
                contextPath = arg.substring(15);
            }
        }

        DatalakeApiServer apiServer = new DatalakeApiServer(port, contextPath, wsPort);
        apiServer.start();

        Runtime.getRuntime().addShutdownHook(new Thread(apiServer::stop));

        Thread.currentThread().join();
    }
}
