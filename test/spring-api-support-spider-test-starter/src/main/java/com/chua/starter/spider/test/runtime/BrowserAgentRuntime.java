package com.chua.starter.spider.test.runtime;

import com.chua.starter.ai.support.chat.ChatClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * BROWSER_AGENT 运行时 — AI Agent 循环（基于 Chrome 内置 MCP Server）
 * <p>AI 通过 MCP 协议 + CDP 命令操控浏览器，使用 document.querySelector 等
 * 原生 DOM API 实现稳定的元素交互。</p>
 *
 * @author CH
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BrowserAgentRuntime {

    private final ObjectProvider<ChatClient> chatClientProvider;
    private final ObjectMapper objectMapper;

    @Value("${spider.browser.channel:}")
    private String browserChannel;

    @Value("${spider.browser.mcp-port:9222}")
    private int mcpCdpPort;

    /** 启用 WebMCP 实验特性（chrome://flags/#enable-webmcp-testing），默认关闭 */
    @Value("${spider.browser.webmcp:false}")
    private boolean webMcpEnabled;

    public BrowserAgentResult execute(String goal, int maxSteps,
                                       String fallbackStrategy,
                                       Map<String, Object> extractConfig) {
        ChatClient chatClient = chatClientProvider.getIfAvailable();
        if (chatClient == null) {
            log.warn("ChatClient 不可用，BROWSER_AGENT 无法运行");
            return BrowserAgentResult.failed("AI 服务不可用，请检查 AI 配置");
        }

        String initialUrl = extractConfig != null
                ? (String) extractConfig.get("entryUrl") : null;

        Playwright playwright = null;
        McpClient mcpClient = null;
        try {
            // ==================== 1. 启动 Chrome（启用 CDP 调试端口） ====================
            playwright = Playwright.create();
            BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                    .setHeadless(false);
            if (browserChannel != null && !browserChannel.isBlank()) {
                launchOptions.setChannel(browserChannel);
                log.info("使用系统浏览器: channel={}", browserChannel);
            }

            // Chrome 启动参数 — 增强反检测
            List<String> chromeArgs = new ArrayList<>(List.of(
                    "--no-sandbox",
                    "--disable-blink-features=AutomationControlled",
                    "--disable-search-engine-choice-screen",
                    "--disable-automation",
                    "--disable-features=ChromeWhatsNewUI,ChromeLabs,PrivacySandboxSettings4,OptimizationGuideModelDownloading",
                    "--disable-sync",
                    "--disable-default-apps",
                    "--disable-background-networking",
                    "--disable-background-timer-throttling",
                    "--disable-backgrounding-occluded-windows",
                    "--disable-breakpad",
                    "--disable-component-extensions-with-background-pages",
                    "--disable-field-trial-config",
                    "--disable-ipc-flooding-protection",
                    "--disable-prompt-on-repost",
                    "--disable-renderer-backgrounding",
                    "--no-first-run",
                    "--no-default-browser-check",
                    "--password-store=basic",
                    "--use-mock-keychain",
                    "--hide-scrollbars",
                    "--mute-audio",
                    "--lang=zh-CN",
                    "--remote-debugging-port=" + mcpCdpPort,
                    "--remote-allow-origins=*",
                    // DNS 修复 — 使用系统 DNS 而非 Chrome 内置 DNS
                    "--disable-features=AsyncDns",
                    "--enable-features=UseDnsHttpsSvcb",
                    "--dns-prefetch-disable"
            ));
            // WebMCP 实验特性（需 Chrome 146+ Canary + chrome://flags/#enable-webmcp-testing）
            // 目前仅有极少实验站点支持，不影响 BROWSER_AGENT 的 DOM 操作流程
            if (webMcpEnabled) {
                chromeArgs.add("--enable-features=WebMCPTesting");
                log.info("WebMCP 实验特性已启用（仅对实现 WebMCP API 的站点有效）");
            }

            launchOptions.setArgs(chromeArgs);

            Browser browser = playwright.chromium().launch(launchOptions);
            log.info("Chrome 已启动, CDP 端口={}", mcpCdpPort);

            // ==================== 2. 连接 CDP ====================
            mcpClient = new McpClient(mcpCdpPort, objectMapper);
            boolean cdpConnected = false;
            Exception lastCdpError = null;
            for (int retry = 0; retry < 5; retry++) {
                if (retry > 0) {
                    log.info("CDP 连接重试 {}/5...", retry + 1);
                    try { Thread.sleep(2000); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
                }
                try {
                    mcpClient.connect();
                    cdpConnected = true;
                    break;
                } catch (Exception e) {
                    lastCdpError = e;
                    log.warn("CDP 连接尝试 {}/5 失败: {}", retry + 1, e.getMessage());
                }
            }
            if (!cdpConnected) {
                String errorMsg = lastCdpError != null ? lastCdpError.getMessage() : "未知错误";
                log.error("CDP 连接失败（已重试 5 次）: {}", errorMsg, lastCdpError);
                return BrowserAgentResult.failed("Chrome CDP 连接失败: " + errorMsg);
            }

            try { Thread.sleep(1500); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }

            // ==================== 3. 导航到初始 URL ====================
            if (initialUrl != null && !initialUrl.isEmpty()) {
                try {
                    mcpClient.pageNavigate(initialUrl);
                    log.info("已导航到初始 URL: {}", initialUrl);
                    try { Thread.sleep(2000); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
                    // 注入 stealth JS
                    injectStealthScripts(mcpClient);
                    try { Thread.sleep(500); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
                } catch (Exception e) {
                    log.warn("导航到 {} 失败: {}", initialUrl, e.getMessage());
                }
            }

            // ==================== 4. Agent 循环 ====================
            List<Map<String, Object>> actionHistory = new ArrayList<>();
            Map<String, Object> extractedData = new HashMap<>();

            for (int step = 0; step < maxSteps; step++) {
                log.info("BROWSER_AGENT step {}/{}", step + 1, maxSteps);

                String pageState = capturePageStateViaMcp(mcpClient);
                String prompt = buildAgentPrompt(goal, pageState,
                        actionHistory, step, maxSteps, extractedData, fallbackStrategy);

                String aiResponse;
                try {
                    aiResponse = chatClient.chatSync(prompt);
                    log.debug("AI 响应: {}", aiResponse);
                } catch (Exception e) {
                    log.error("AI 决策调用失败: {}", e.getMessage());
                    return BrowserAgentResult.failed("AI 决策失败: " + e.getMessage(),
                            extractedData, actionHistory);
                }

                if (aiResponse == null || aiResponse.isBlank()) {
                    return BrowserAgentResult.failed("AI 返回空响应", extractedData, actionHistory);
                }

                Map<String, Object> decision = parseJsonResponse(aiResponse);
                if (decision == null || decision.isEmpty()) {
                    log.warn("无法解析 AI 决策，重试: {}", aiResponse);
                    continue;
                }

                String actionType = (String) decision.getOrDefault("actionType", "");
                String actionDesc = (String) decision.getOrDefault("description", "");

                String execResult = executeDecisionViaMcp(mcpClient, decision, extractedData);
                log.info("  → 动作: {} | 结果: {}", actionType, execResult);

                Map<String, Object> entry = new HashMap<>();
                entry.put("step", step + 1);
                entry.put("action", actionType);
                entry.put("description", actionDesc);
                entry.put("result", execResult);
                actionHistory.add(entry);

                if ("goal_achieved".equals(actionType)) {
                    String summary = (String) decision.getOrDefault("summary", "目标已完成");
                    @SuppressWarnings("unchecked")
                    Map<String, Object> aiData = (Map<String, Object>) decision.get("extractedData");
                    if (aiData != null) extractedData.putAll(aiData);
                    return BrowserAgentResult.success(summary, extractedData, actionHistory);
                }

                if ("manual_required".equals(actionType)) {
                    return buildManualResult(decision, extractedData, actionHistory);
                }
            }

            return BrowserAgentResult.failed(
                    "已达最大步数(" + maxSteps + ")，目标未完成", extractedData, actionHistory);

        } catch (Exception e) {
            log.error("BROWSER_AGENT 执行异常", e);
            return BrowserAgentResult.failed("BROWSER_AGENT 异常: " + e.getMessage());
        } finally {
            if (mcpClient != null) {
                try { mcpClient.close(); } catch (Exception ignored) {}
            }
            if (playwright != null) {
                try { playwright.close(); } catch (Exception ignored) {}
            }
        }
    }

    // ==================== Stealth 反检测 ====================

    /** 注入 stealth JS，隐藏自动化痕迹 */
    private void injectStealthScripts(McpClient mcpClient) {
        String stealthJs = """
                (() => {
                  // 覆盖 webdriver 属性
                  Object.defineProperty(navigator, 'webdriver', { get: () => false });
                  // 覆盖 chrome.runtime
                  if (window.chrome) {
                    Object.defineProperty(window.chrome, 'runtime', {
                      get: () => ({ connect: () => {}, sendMessage: () => {} })
                    });
                  }
                  // 覆盖 permissions
                  if (navigator.permissions) {
                    const origQuery = navigator.permissions.query;
                    navigator.permissions.query = (desc) => (
                      desc.name === 'notifications'
                        ? Promise.resolve({ state: 'granted', onchange: null })
                        : origQuery.call(navigator.permissions, desc)
                    );
                  }
                  // 覆盖 plugins 长度
                  Object.defineProperty(navigator, 'plugins', {
                    get: () => [1, 2, 3, 4, 5]
                  });
                  // 覆盖 languages
                  Object.defineProperty(navigator, 'languages', {
                    get: () => ['zh-CN', 'zh', 'en']
                  });
                })();
                """;
        try {
            mcpClient.evaluate(stealthJs);
            log.info("Stealth JS 已注入");
        } catch (Exception e) {
            log.warn("Stealth JS 注入失败: {}", e.getMessage());
        }
    }

    // ==================== 页面状态捕获 ====================

    /** 通过 MCP / CDP 获取完整的页面状态描述 */
    private String capturePageStateViaMcp(McpClient mcpClient) {
        try {
            String url = mcpClient.getCurrentUrl();
            String title = mcpClient.getPageTitle();
            String interactable = mcpClient.getInteractableElements();
            String bodyText = mcpClient.getPageText();
            if (bodyText != null && bodyText.length() > 2000) {
                bodyText = bodyText.substring(0, 2000) + "...";
            }

            // 获取截图（仅描述大小，不含实际 base64 数据以节省 token）
            String screenshotInfo = "";
            try {
                String ss = mcpClient.takeScreenshot();
                if (ss != null && !ss.isEmpty()) {
                    int kb = ss.length() * 3 / 4 / 1024;
                    screenshotInfo = "\n截图: 可用 (" + kb + "KB PNG base64)";
                }
            } catch (Exception ignored) {}

            return String.format("""
                    === 页面信息 ===
                    URL: %s
                    标题: %s
                    %s

                    === 可交互元素（共N个） ===
                    %s

                    === 页面可见文本 ===
                    %s
                    """,
                    url != null ? url : "（未知）",
                    title != null ? title : "（未知）",
                    screenshotInfo,
                    interactable != null ? interactable : "（无）",
                    bodyText != null ? bodyText : "（无）");
        } catch (Exception e) {
            log.warn("MCP 页面状态捕获失败: {}", e.getMessage());
            return "页面状态获取失败: " + e.getMessage();
        }
    }

    // ==================== Prompt ====================

    private String buildAgentPrompt(String goal, String pageState,
                                     List<Map<String, Object>> actionHistory,
                                     int step, int maxSteps,
                                     Map<String, Object> extractedData,
                                     String fallbackStrategy) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个网页自动化浏览器代理 (Web Browser Agent)。\n\n");
        sb.append("## 目标\n").append(goal).append("\n\n");
        sb.append("## 当前页面状态\n").append(pageState).append("\n\n");

        sb.append("## 已提取数据\n");
        if (extractedData.isEmpty()) {
            sb.append("（暂无）\n");
        } else {
            try {
                sb.append(objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(extractedData));
            } catch (Exception e) {
                sb.append(extractedData);
            }
        }
        sb.append("\n\n");

        if (!actionHistory.isEmpty()) {
            sb.append("## 已执行操作历史\n");
            for (Map<String, Object> h : actionHistory) {
                sb.append("  - Step ").append(h.get("step"))
                        .append(": [").append(h.get("action")).append("] ")
                        .append(h.get("description")).append(" → ")
                        .append(h.get("result")).append("\n");
            }
            sb.append("\n");
        }

        sb.append("## 当前进度\n");
        sb.append("  第 ").append(step + 1).append(" / 共 ").append(maxSteps).append(" 步\n\n");

        sb.append("""
                ## 可用工具

                1. **navigate** — 导航到新 URL
                   {"actionType": "navigate", "url": "https://gitee.com/explore", "description": "跳转到 Gitee 探索页"}

                2. **click** — 点击元素（使用可交互元素中的 selector）
                   {"actionType": "click", "selector": "a.project-link", "description": "点击项目链接"}

                3. **fill** — 在输入框中输入文本
                   {"actionType": "fill", "selector": "#search-input", "value": "java", "description": "搜索框输入 java"}

                4. **select** — 选择下拉选项
                   {"actionType": "select", "selector": "select.sort-by", "value": "stars", "description": "选择按星标排序"}

                5. **extract** — 从当前页面提取数据。使用 fields 数组指定要提取的字段，每个 field 用 key 作为字段名，description 作为提取指引（不需要提供 CSS 选择器，AI 会自动从页面文本分析）。如果不传 fields 则提取全部页面文本。
                   {"actionType": "extract", "fields": [
                     {"key": "项目名称", "description": "项目标题文本"},
                     {"key": "所属组织", "description": "项目所属组织/用户"},
                     {"key": "星标数", "description": "star 数量"},
                     {"key": "编程语言", "description": "主要编程语言"},
                     {"key": "项目描述", "description": "项目描述文本"}
                   ], "description": "提取项目列表信息"}

                6. **wait** — 等待（默认 2 秒）
                   {"actionType": "wait", "ms": 2000, "description": "等待页面加载"}

                7. **scroll** — 滚动页面
                   {"actionType": "scroll", "direction": "down", "description": "向下滚动加载更多"}

                8. **screenshot** — 获取页面截图（base64 PNG，可查看页面视觉状态）
                   {"actionType": "screenshot", "description": "截取当前页面状态"}

                9. **goal_achieved** — 目标已完成，结束执行
                   {"actionType": "goal_achieved", "summary": "成功采集了 xx 个项目的信息", "extractedData": {"项目名称": "spring-boot", "星标数": "50k"}, "description": "目标完成"}

                10. **manual_required** — 需要人工介入
                    {"actionType": "manual_required", "reason": "遇到验证码拦截", "taskGuide": "请手动完成滑块验证", "formFields": [], "description": "需要人工处理验证码"}
                """);

        sb.append("## 回落策略\n");
        sb.append("  当前策略: ").append(fallbackStrategy).append("\n");
        sb.append("  - AUTO_THEN_HUMAN: 尽量自动处理，处理不了再交人工\n");
        sb.append("  - HUMAN_ONLY: 直接交人工\n\n");

        sb.append("""
                ## 要求（必须遵守）
                - 每次只执行一个操作
                - 优先使用可交互元素中的 selector（id / name / data-testid 更稳定）
                - 操作失败时尝试其他方法，不要重复同一个失败操作
                - 提取数据时尽量提取准确的结构化数据
                ***重要: 你必须只输出一个 JSON 对象，不要包含任何其他文字、思考过程或解释！***
                ***JSON 格式必须严格匹配上面可用工具的格式！***
                ***如果你无法操作，就使用 goal_achieved 或 manual_required 结束。***
                ***不要解释你的决定，不要输出除了 JSON 之外的任何内容！***
                """);

        return sb.toString();
    }

    // ==================== 决策执行 ====================

    @SuppressWarnings("unchecked")
    private String executeDecisionViaMcp(McpClient mcpClient,
                                          Map<String, Object> decision,
                                          Map<String, Object> extractedData) {
        String actionType = (String) decision.getOrDefault("actionType", "");
        try {
            switch (actionType) {
                case "navigate": {
                    String url = (String) decision.get("url");
                    if (url == null || url.isBlank()) return "缺少 url 参数";
                    mcpClient.pageNavigate(url);
                    randomDelay(1500, 500);
                    injectStealthScripts(mcpClient);
                    return "已导航到: " + url;
                }
                case "click": {
                    String selector = (String) decision.get("selector");
                    if (selector == null || selector.isBlank()) return "缺少 selector 参数";
                    mcpClient.clickSelector(selector);
                    randomDelay(500, 300);
                    return "已点击: " + selector;
                }
                case "fill": {
                    String selector = (String) decision.get("selector");
                    String value = (String) decision.get("value");
                    if (selector == null || selector.isBlank()) return "缺少 selector 参数";
                    mcpClient.fillInput(selector, value != null ? value : "");
                    randomDelay(300, 200);
                    return "已填写: " + selector + " = " + (value != null ? value : "");
                }
                case "select": {
                    String selector = (String) decision.get("selector");
                    String value = (String) decision.get("value");
                    if (selector == null || selector.isBlank()) return "缺少 selector 参数";
                    mcpClient.selectOption(selector, value != null ? value : "");
                    return "已选择: " + selector + " = " + value;
                }
                case "extract": {
                    return doExtract(mcpClient, decision, extractedData);
                }
                case "screenshot": {
                    String ss = mcpClient.takeScreenshot();
                    if (ss != null && !ss.isEmpty()) {
                        int kb = ss.length() * 3 / 4 / 1024;
                        extractedData.put("_lastScreenshot", "data:image/png;base64," + ss);
                        return "截图已获取 (" + kb + "KB PNG)";
                    }
                    return "截图为空";
                }
                case "wait": {
                    Object msObj = decision.get("ms");
                    long ms = msObj instanceof Number ? ((Number) msObj).longValue() : 2000;
                    try { Thread.sleep(ms); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
                    return "已等待 " + ms + "ms";
                }
                case "scroll": {
                    String dir = (String) decision.getOrDefault("direction", "down");
                    String amount = "window.innerHeight";
                    // 随机滚动量，避免被检测
                    if (ThreadLocalRandom.current().nextBoolean()) {
                        amount = "Math.floor(window.innerHeight * 0." + (ThreadLocalRandom.current().nextInt(5, 9)) + ")";
                    }
                    String script = "window.scrollBy(0, " +
                            ("down".equals(dir) ? amount : "-" + amount) + ")";
                    mcpClient.evaluate(script);
                    randomDelay(400, 300);
                    return "已滚动: " + dir;
                }
                case "goal_achieved":
                case "manual_required":
                    return "无需浏览器操作";
                default:
                    return "未知操作: " + actionType;
            }
        } catch (Exception e) {
            log.warn("执行动作 {} 失败: {}", actionType, e.getMessage());
            return "失败: " + e.getMessage();
        }
    }

    /** 随机延迟（基础 + 随机增量），模拟人类操作 */
    private void randomDelay(int baseMs, int randomMs) {
        try {
            Thread.sleep(baseMs + ThreadLocalRandom.current().nextInt(randomMs));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @SuppressWarnings("unchecked")
    private String doExtract(McpClient mcpClient, Map<String, Object> decision,
                              Map<String, Object> extractedData) {
        Object fieldsObj = decision.get("fields");
        List<Map<String, Object>> fields;
        if (fieldsObj instanceof List) {
            fields = (List<Map<String, Object>>) fieldsObj;
        } else {
            try {
                String bodyText = mcpClient.getPageText();
                extractedData.put("_pageText", bodyText != null ? bodyText.trim() : "");
                return "已提取页面全文";
            } catch (Exception e) {
                return "提取失败: " + e.getMessage();
            }
        }

        int count = 0;
        for (Map<String, Object> field : fields) {
            String key = (String) field.getOrDefault("key", "");
            if (key.isEmpty()) continue;
            String desc = (String) field.getOrDefault("description", "");
            String selector = (String) field.getOrDefault("selector", "");

            if (!selector.isEmpty()) {
                try {
                    String script = "(() => { const el = document.querySelector('" +
                            selector.replace("'", "\\'") +
                            "'); return el ? el.textContent.trim() : ''; })()";
                    String val = mcpClient.evaluate(script);
                    extractedData.put(key, val != null ? val.trim() : "");
                    count++;
                } catch (Exception ignored) {}
            } else {
                extractedData.put(key, "【AI 分析】" + desc);
                count++;
            }
        }
        return "已提取 " + count + " 个字段";
    }

    // ==================== 人工任务 ====================

    @SuppressWarnings("unchecked")
    private BrowserAgentResult buildManualResult(Map<String, Object> decision,
                                                  Map<String, Object> extractedData,
                                                  List<Map<String, Object>> actionHistory) {
        String reason = (String) decision.getOrDefault("reason", "需要人工处理");
        String taskGuide = (String) decision.getOrDefault("taskGuide", "请按页面提示操作");
        String taskType = (String) decision.getOrDefault("taskType", "BROWSER_AGENT_MANUAL");
        Object fieldsObj = decision.get("formFields");
        List<Map<String, Object>> formFields = new ArrayList<>();
        if (fieldsObj instanceof List) formFields = (List<Map<String, Object>>) fieldsObj;

        return BrowserAgentResult.manualRequired(
                reason, extractedData, actionHistory, taskType, taskGuide, formFields);
    }

    // ==================== JSON 解析 ====================

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJsonResponse(String response) {
        if (response == null || response.isBlank()) return null;

        // 移除 Qwen 模型的 <think>...</think> 思考标签
        String cleaned = response.replaceAll("(?s)<think>.*?</think>", "").trim();
        // 移除 markdown 代码块标记
        cleaned = cleaned.replaceAll("```(?:json)?\\s*", "").trim();
        // 移除多余的尾部 ```
        if (cleaned.endsWith("```")) cleaned = cleaned.substring(0, cleaned.length() - 3).trim();

        // 直接解析完整响应
        try { return objectMapper.readValue(cleaned, Map.class); } catch (Exception ignored) {}

        // 查找 JSON 对象的起止位置
        int start = cleaned.indexOf('{');
        if (start < 0) return null;
        int depth = 0;
        boolean inString = false;
        for (int i = start; i < cleaned.length(); i++) {
            char c = cleaned.charAt(i);
            if (inString) {
                if (c == '\\') { i++; continue; }
                if (c == '"') inString = false;
                continue;
            }
            if (c == '"') { inString = true; continue; }
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) {
                    try { return objectMapper.readValue(cleaned.substring(start, i + 1), Map.class); }
                    catch (Exception e) { return null; }
                }
            }
        }
        return null;
    }
}
