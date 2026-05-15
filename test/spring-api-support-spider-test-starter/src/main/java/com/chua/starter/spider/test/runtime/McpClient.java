package com.chua.starter.spider.test.runtime;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Chrome CDP 客户端 — 通过 WebSocket 直接连接 Chrome DevTools Protocol。
 * <p>不再依赖 Chrome 内置 MCP Server（DevToolsMCPServer），而是直接通过 CDP WebSocket
 * 发送 runtime_evaluate / Page.navigate 等命令，更稳定可靠。</p>
 *
 * @author CH
 */
@Slf4j
public class McpClient implements AutoCloseable {

    private static final int DEFAULT_TIMEOUT_SECONDS = 30;

    private final int cdpPort;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final AtomicInteger requestId = new AtomicInteger(0);
    private final Map<Integer, CompletableFuture<JsonNode>> pendingRequests = new ConcurrentHashMap<>();

    private WebSocket webSocket;
    private volatile boolean connected = false;

    public McpClient(int port, ObjectMapper objectMapper) {
        this(port, objectMapper, DEFAULT_TIMEOUT_SECONDS);
    }

    public McpClient(int port, ObjectMapper objectMapper, int timeoutSeconds) {
        this.cdpPort = port;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        log.info("CDP 客户端初始化, 端口: {}", port);
    }

    // ==================== 连接管理 ====================

    /**
     * 连接到 Chrome CDP — 自动发现或创建页面目标
     */
    public void connect() throws Exception {
        // 1. 获取页面 WebSocket URL（GET /json 获取页面列表）
        String pageWsUrl = getPageWebSocketUrl();
        log.info("CDP 页面 WebSocket URL: {}", pageWsUrl);

        // 2. 连接页面 WebSocket
        CompletableFuture<WebSocket> wsFuture = new CompletableFuture<>();
        httpClient.newWebSocketBuilder()
                .buildAsync(URI.create(pageWsUrl), new WebSocketListener(wsFuture))
                .whenComplete((ws, err) -> {
                    if (err != null) wsFuture.completeExceptionally(err);
                });

        this.webSocket = wsFuture.get(15, TimeUnit.SECONDS);
        this.connected = true;
        log.info("CDP 页面 WebSocket 已连接");

        // 3. 发送 CDP 初始化命令 — Page.enable + Runtime.enable
        try { sendCdpCommand("Page.enable", Map.of()); } catch (Exception ignored) {}
        try { sendCdpCommand("Runtime.enable", Map.of()); } catch (Exception ignored) {}
        log.info("CDP 页面已初始化");
    }

    /**
     * 获取可用页面 WebSocket URL。
     * 先尝试从 /json 获取已有页面，如果没有则通过浏览器 WebSocket 创建新页面。
     */
    private String getPageWebSocketUrl() throws Exception {
        // 先尝试从 /json 获取已有页面
        HttpRequest listRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + cdpPort + "/json"))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> listResponse = httpClient.send(listRequest,
                HttpResponse.BodyHandlers.ofString());

        if (listResponse.statusCode() == 200) {
            JsonNode pages = objectMapper.readTree(listResponse.body());
            if (pages.isArray() && pages.size() > 0) {
                // 取第一个可用页面的 WebSocket URL
                for (JsonNode page : pages) {
                    String wsUrl = page.path("webSocketDebuggerUrl").asText();
                    if (!wsUrl.isEmpty()) {
                        return wsUrl;
                    }
                }
            }
        }

        // 没有可用页面，通过浏览器 WebSocket 创建新页面
        String browserWsUrl = getBrowserWebSocketUrl();
        log.info("无可用页面，通过浏览器 WebSocket 创建新页面: {}", browserWsUrl);

        CompletableFuture<WebSocket> browserWsFuture = new CompletableFuture<>();
        httpClient.newWebSocketBuilder()
                .buildAsync(URI.create(browserWsUrl), new WebSocketListener(browserWsFuture))
                .whenComplete((ws, err) -> {
                    if (err != null) browserWsFuture.completeExceptionally(err);
                });

        WebSocket browserWs = browserWsFuture.get(15, TimeUnit.SECONDS);

        // 用 Target.createTarget 创建空白页
        int createId = requestId.incrementAndGet();
        String createCmd = objectMapper.writeValueAsString(Map.of(
                "id", createId,
                "method", "Target.createTarget",
                "params", Map.of("url", "about:blank")
        ));

        CompletableFuture<JsonNode> createFuture = new CompletableFuture<>();
        pendingRequests.put(createId, createFuture);
        browserWs.sendText(createCmd, true);
        JsonNode createResult = createFuture.get(10, TimeUnit.SECONDS);

        String targetId = createResult.path("result").path("targetId").asText();
        browserWs.sendClose(1000, "done");

        if (targetId.isEmpty()) {
            throw new RuntimeException("无法创建页面目标: " + createResult);
        }

        // 通过 /json 获取新页面的 WebSocket URL
        HttpRequest retryRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + cdpPort + "/json"))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> retryResponse = httpClient.send(retryRequest,
                HttpResponse.BodyHandlers.ofString());

        if (retryResponse.statusCode() == 200) {
            JsonNode retryPages = objectMapper.readTree(retryResponse.body());
            if (retryPages.isArray()) {
                for (JsonNode page : retryPages) {
                    if (targetId.equals(page.path("id").asText())) {
                        String wsUrl = page.path("webSocketDebuggerUrl").asText();
                        if (!wsUrl.isEmpty()) return wsUrl;
                    }
                }
            }
        }

        throw new RuntimeException("无法获取新创建页面的 WebSocket URL");
    }

    /**
     * 通过 CDP /json/version 获取浏览器级 WebSocket URL
     */
    private String getBrowserWebSocketUrl() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + cdpPort + "/json/version"))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("CDP 版本查询失败: HTTP " + response.statusCode());
        }

        JsonNode json = objectMapper.readTree(response.body());
        String wsUrl = json.path("webSocketDebuggerUrl").asText();
        if (wsUrl.isEmpty()) {
            throw new RuntimeException("无法获取 CDP WebSocket URL，响应: " + response.body());
        }
        return wsUrl;
    }

    // ==================== CDP 命令 ====================

    /**
     * 发送 CDP 命令并等待响应
     */
    private JsonNode sendCdpCommand(String method, Map<String, Object> params) throws Exception {
        if (!connected || webSocket == null) {
            throw new RuntimeException("CDP 未连接");
        }

        int id = requestId.incrementAndGet();

        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("id", id);
        body.put("method", method);
        if (params != null && !params.isEmpty()) {
            body.put("params", params);
        }

        String json = objectMapper.writeValueAsString(body);

        CompletableFuture<JsonNode> future = new CompletableFuture<>();
        pendingRequests.put(id, future);

        try {
            webSocket.sendText(json, true);
        } catch (Exception e) {
            pendingRequests.remove(id);
            throw new RuntimeException("CDP 发送失败: " + e.getMessage(), e);
        }

        return future.get(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    // ==================== 高级浏览器操作 ====================

    /** 导航到 URL */
    public void pageNavigate(String url) throws Exception {
        sendCdpCommand("Page.navigate", Map.of("url", url));
    }

    /** 点击元素（通过 CSS 选择器） */
    public void clickSelector(String selector) throws Exception {
        String escaped = selector.replace("'", "\\'");
        evaluate("document.querySelector('" + escaped + "').click()");
    }

    /** 填入输入框 */
    public void fillInput(String selector, String value) throws Exception {
        String escapedSel = selector.replace("'", "\\'");
        String escapedVal = value.replace("'", "\\'")
                .replace("\n", "\\n");
        evaluate("(() => {" +
                "  const el = document.querySelector('" + escapedSel + "');" +
                "  if (!el) return;" +
                "  const nativeInputValueSetter = Object.getOwnPropertyDescriptor(" +
                "    window.HTMLInputElement.prototype, 'value').set;" +
                "  nativeInputValueSetter.call(el, '" + escapedVal + "');" +
                "  el.dispatchEvent(new Event('input', { bubbles: true }));" +
                "  el.dispatchEvent(new Event('change', { bubbles: true }));" +
                "})()");
    }

    /** 选择下拉选项 */
    public void selectOption(String selector, String value) throws Exception {
        String escaped = selector.replace("'", "\\'");
        evaluate("(() => {" +
                "  const el = document.querySelector('" + escaped + "');" +
                "  if (!el) return;" +
                "  el.value = '" + value.replace("'", "\\'") + "';" +
                "  el.dispatchEvent(new Event('change', { bubbles: true }));" +
                "})()");
    }

    /** 获取页面内容文本 */
    public String getPageText() throws Exception {
        return evaluate("document.body.innerText");
    }

    /** 获取页面标题 */
    public String getPageTitle() throws Exception {
        return evaluate("document.title");
    }

    /** 获取可交互元素列表 */
    public String getInteractableElements() throws Exception {
        String json = evaluate("JSON.stringify(" +
                "  Array.from(document.querySelectorAll(" +
                "    'a[href], button, input:not([type=hidden]), select, textarea, [role=button], [tabindex]'" +
                "  )).slice(0, 100).map((el, i) => ({" +
                "    index: i," +
                "    tag: el.tagName.toLowerCase()," +
                "    type: el.type || ''," +
                "    text: (el.textContent || el.placeholder || el.getAttribute('aria-label') || '').trim().slice(0, 60)," +
                "    selector: el.id ? '#' + el.id : " +
                "      el.name ? '[name=\"' + el.name + '\"]' : " +
                "      el.getAttribute('data-testid') ? '[data-testid=\"' + el.getAttribute('data-testid') + '\"]' : " +
                "      el.tagName.toLowerCase() + (el.className ? '.' + el.className.split(' ').filter(Boolean).join('.') : '')," +
                "    href: el.href || ''," +
                "    value: el.value || ''" +
                "  }))" +
                ")");
        return json;
    }

    /** 获取当前 URL */
    public String getCurrentUrl() throws Exception {
        return evaluate("window.location.href");
    }

    /** 执行 JavaScript */
    public String evaluate(String script) throws Exception {
        JsonNode response = sendCdpCommand("Runtime.evaluate", Map.of(
                "expression", script,
                "returnByValue", true
        ));
        JsonNode result = response.path("result");
        if (result.has("value")) {
            JsonNode val = result.get("value");
            return val.isTextual() ? val.asText() : val.toString();
        }
        if (result.has("description")) {
            return result.path("description").asText();
        }
        return result.toString();
    }

    /** 截图 */
    public String takeScreenshot() throws Exception {
        JsonNode response = sendCdpCommand("Page.captureScreenshot", Map.of("format", "png"));
        return response.path("result").path("data").asText("");
    }

    // ==================== WebSocket 监听器 ====================

    private class WebSocketListener implements WebSocket.Listener {
        private final CompletableFuture<WebSocket> wsFuture;
        private final StringBuilder messageBuffer = new StringBuilder();

        WebSocketListener(CompletableFuture<WebSocket> wsFuture) {
            this.wsFuture = wsFuture;
        }

        @Override
        public void onOpen(WebSocket webSocket) {
            log.debug("CDP WebSocket 已打开");
            wsFuture.complete(webSocket);
            WebSocket.Listener.super.onOpen(webSocket);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            messageBuffer.append(data);
            if (last) {
                String message = messageBuffer.toString();
                messageBuffer.setLength(0);
                try {
                    JsonNode json = objectMapper.readTree(message);
                    if (json.has("id")) {
                        int id = json.get("id").asInt();
                        CompletableFuture<JsonNode> future = pendingRequests.remove(id);
                        if (future != null) {
                            JsonNode error = json.get("error");
                            if (error != null) {
                                future.completeExceptionally(
                                        new RuntimeException("CDP 错误: " + error.path("message").asText()));
                            } else {
                                future.complete(json);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("CDP 消息解析失败: {}", e.getMessage());
                }
            }
            return WebSocket.Listener.super.onText(webSocket, data, last);
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            log.error("CDP WebSocket 错误: {}", error.getMessage());
            wsFuture.completeExceptionally(error);
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            log.info("CDP WebSocket 已关闭: code={}, reason={}", statusCode, reason);
            connected = false;
            // 完成所有等待中的请求
            for (Map.Entry<Integer, CompletableFuture<JsonNode>> entry : pendingRequests.entrySet()) {
                entry.getValue().completeExceptionally(
                        new RuntimeException("CDP 连接已关闭: " + reason));
            }
            pendingRequests.clear();
            return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
        }
    }

    @Override
    public void close() {
        connected = false;
        if (webSocket != null) {
            webSocket.sendClose(1000, "client close");
            webSocket = null;
        }
        // 完成所有等待中的请求
        for (Map.Entry<Integer, CompletableFuture<JsonNode>> entry : pendingRequests.entrySet()) {
            entry.getValue().completeExceptionally(new RuntimeException("CDP 客户端已关闭"));
        }
        pendingRequests.clear();
        log.info("CDP 客户端已关闭");
    }
}
