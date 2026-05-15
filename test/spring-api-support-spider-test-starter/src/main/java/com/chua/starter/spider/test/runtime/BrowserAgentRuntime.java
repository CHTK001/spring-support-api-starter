package com.chua.starter.spider.test.runtime;

import com.chua.starter.ai.support.chat.ChatClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * BROWSER_AGENT 运行时 — AI Agent 循环
 * <p>AI 自主操作浏览器：看页面 → 决策 → 操作（click/type/navigate/extract）→ 循环，
 * 遇到处理不了的登录/CAPTCHA 等情况交人工处理。</p>
 *
 * @author CH
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BrowserAgentRuntime {

    private static final Pattern JSON_BLOCK = Pattern.compile(
            "(?s)\\{[^{}]*+(?:\\{[^{}]*+\\}[^{}]*+)*+\\}");

    private final ObjectProvider<ChatClient> chatClientProvider;
    private final ObjectMapper objectMapper;

    /**
     * 执行 AI Agent 循环
     *
     * @param goal            目标描述
     * @param maxSteps        最大步数
     * @param fallbackStrategy 回落策略: AUTO_THEN_HUMAN / HUMAN_ONLY
     * @param extractConfig   提取配置（含 entryUrl 等）
     * @return 执行结果
     */
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

        Playwright playwright = Playwright.create();
        try {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(false)
                    .setArgs(List.of("--no-sandbox",
                            "--disable-blink-features=AutomationControlled")));
            BrowserContext browserContext = browser.newContext(new Browser.NewContextOptions()
                    .setViewportSize(1280, 720));
            Page page = browserContext.newPage();
            page.setDefaultTimeout(15_000);
            page.setDefaultNavigationTimeout(30_000);

            // 导航到初始 URL
            if (initialUrl != null && !initialUrl.isEmpty()) {
                try {
                    page.navigate(initialUrl);
                    page.waitForLoadState(LoadState.DOMCONTENTLOADED);
                    log.info("已导航到初始 URL: {}", initialUrl);
                } catch (Exception e) {
                    log.warn("导航到 {} 失败: {}", initialUrl, e.getMessage());
                }
            }

            // Agent 循环
            List<Map<String, Object>> actionHistory = new ArrayList<>();
            Map<String, Object> extractedData = new HashMap<>();

            for (int step = 0; step < maxSteps; step++) {
                log.info("BROWSER_AGENT step {}/{}", step + 1, maxSteps);

                // 1. 捕获当前页面状态
                String pageState = capturePageState(page);
                String interactableHtml = captureInteractableElements(page);

                // 2. 构建 AI prompt
                String prompt = buildAgentPrompt(goal, pageState, interactableHtml,
                        actionHistory, step, maxSteps, extractedData, fallbackStrategy);

                // 3. 调用 AI 决策
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

                // 4. 解析决策
                Map<String, Object> decision = parseJsonResponse(aiResponse);
                if (decision == null || decision.isEmpty()) {
                    log.warn("无法解析 AI 决策，重试: {}", aiResponse);
                    continue;
                }

                String actionType = (String) decision.getOrDefault("actionType", "");
                String actionDesc = (String) decision.getOrDefault("description", "");

                // 5. 执行决策
                String execResult = executeDecision(page, decision, extractedData);
                log.info("  → 动作: {} | 结果: {}", actionType, execResult);

                // 记录历史
                Map<String, Object> entry = new HashMap<>();
                entry.put("step", step + 1);
                entry.put("action", actionType);
                entry.put("description", actionDesc);
                entry.put("result", execResult);
                actionHistory.add(entry);

                // 6. 检查终止状态
                if ("goal_achieved".equals(actionType)) {
                    String summary = (String) decision.getOrDefault("summary", "目标已完成");
                    // 尝试合并 AI 返回的提取数据
                    @SuppressWarnings("unchecked")
                    Map<String, Object> aiData = (Map<String, Object>)
                            decision.get("extractedData");
                    if (aiData != null) {
                        extractedData.putAll(aiData);
                    }
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
            try {
                playwright.close();
            } catch (Exception ignored) {
            }
        }
    }

    // ==================== 页面状态捕获 ====================

    private String capturePageState(Page page) {
        try {
            Map<String, Object> state = new HashMap<>();
            state.put("url", page.url());
            state.put("title", page.title());

            String bodyText = page.locator("body").textContent();
            if (bodyText != null) {
                bodyText = bodyText.replaceAll("\\s+", " ").trim();
                if (bodyText.length() > 3000) {
                    bodyText = bodyText.substring(0, 3000) + "...";
                }
            }
            state.put("visibleText", bodyText != null ? bodyText : "");

            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(state);
        } catch (Exception e) {
            log.warn("捕获页面状态失败: {}", e.getMessage());
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    private String captureInteractableElements(Page page) {
        try {
            List<Map<String, String>> elements = new ArrayList<>();

            // 链接
            for (Locator el : page.locator("a[href]").all()) {
                String text = el.textContent();
                String href = el.getAttribute("href");
                if (text != null) text = text.replaceAll("\\s+", " ").trim();
                if (text != null && !text.isEmpty() && text.length() < 100) {
                    Map<String, String> e = new HashMap<>();
                    e.put("type", "link");
                    e.put("text", text);
                    e.put("href", href != null ? href : "");
                    elements.add(e);
                }
            }

            // 按钮
            for (Locator el : page.locator("button, input[type=submit], input[type=button]").all()) {
                String text = el.textContent();
                String val = el.getAttribute("value");
                String label = (text != null && !text.isBlank()) ? text : (val != null ? val : "");
                label = label.replaceAll("\\s+", " ").trim();
                if (!label.isEmpty() && label.length() < 80) {
                    Map<String, String> e = new HashMap<>();
                    e.put("type", "button");
                    e.put("text", label);
                    elements.add(e);
                }
            }

            // 输入框
            for (Locator el : page.locator("input:not([type=hidden]):not([type=submit]):not([type=button])").all()) {
                String name = el.getAttribute("name");
                String placeholder = el.getAttribute("placeholder");
                String type = el.getAttribute("type");
                if (name != null || placeholder != null) {
                    Map<String, String> e = new HashMap<>();
                    e.put("type", "input");
                    e.put("inputType", type != null ? type : "text");
                    e.put("name", name != null ? name : "");
                    e.put("placeholder", placeholder != null ? placeholder : "");
                    elements.add(e);
                }
            }

            // 下拉框
            for (Locator el : page.locator("select").all()) {
                String name = el.getAttribute("name");
                if (name != null) {
                    Map<String, String> e = new HashMap<>();
                    e.put("type", "select");
                    e.put("name", name);
                    elements.add(e);
                }
            }

            if (elements.isEmpty()) {
                return "无可交互元素";
            }

            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                    elements.subList(0, Math.min(elements.size(), 50)));
        } catch (Exception e) {
            log.warn("提取可交互元素失败: {}", e.getMessage());
            return "提取失败: " + e.getMessage();
        }
    }

    // ==================== Prompt 构建 ====================

    private String buildAgentPrompt(String goal, String pageState, String interactableElements,
                                     List<Map<String, Object>> actionHistory,
                                     int step, int maxSteps,
                                     Map<String, Object> extractedData,
                                     String fallbackStrategy) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个网页自动化浏览器代理 (Web Browser Agent)。\n\n");
        sb.append("## 目标\n").append(goal).append("\n\n");
        sb.append("## 当前页面状态\n").append(pageState).append("\n\n");
        sb.append("## 页面可交互元素\n").append(interactableElements).append("\n\n");
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

        sb.append("## 可用工具\n\n");
        sb.append("1. **navigate** — 导航到新 URL\n");
        sb.append("   {\"actionType\": \"navigate\", \"url\": \"https://...\", \"description\": \"描述\"}\n\n");
        sb.append("2. **click** — 点击元素（使用 CSS 选择器）\n");
        sb.append("   {\"actionType\": \"click\", \"selector\": \"CSS选择器\", \"description\": \"描述\"}\n\n");
        sb.append("3. **fill** — 在输入框中输入文本\n");
        sb.append("   {\"actionType\": \"fill\", \"selector\": \"CSS选择器\", \"value\": \"文本\", \"description\": \"描述\"}\n\n");
        sb.append("4. **select** — 选择下拉选项\n");
        sb.append("   {\"actionType\": \"select\", \"selector\": \"CSS选择器\", \"value\": \"选项值\", \"description\": \"描述\"}\n\n");
        sb.append("5. **extract** — 从当前页面提取数据\n");
        sb.append("   {\"actionType\": \"extract\", \"fields\": [{\"key\": \"fieldName\", \"description\": \"字段描述\"}], \"description\": \"描述\"}\n\n");
        sb.append("6. **wait** — 等待（默认 2 秒）\n");
        sb.append("   {\"actionType\": \"wait\", \"ms\": 2000, \"description\": \"描述\"}\n\n");
        sb.append("7. **scroll** — 滚动页面\n");
        sb.append("   {\"actionType\": \"scroll\", \"direction\": \"down\", \"description\": \"描述\"}\n\n");
        sb.append("8. **goal_achieved** — 目标已完成，结束执行\n");
        sb.append("   {\"actionType\": \"goal_achieved\", \"summary\": \"完成摘要\", \"extractedData\": {\"field\": \"value\"}, \"description\": \"描述\"}\n\n");
        sb.append("9. **manual_required** — 需要人工介入（如遇到验证码/登录等无法自动处理的情况）\n");
        sb.append("   {\"actionType\": \"manual_required\", \"reason\": \"原因\", \"taskGuide\": \"人工操作指引\", \"formFields\": [{\"fieldKey\": \"code\", \"label\": \"验证码\", \"component\": \"input\"}], \"description\": \"描述\"}\n\n");

        sb.append("## 回落策略\n");
        sb.append("  当前策略: ").append(fallbackStrategy).append("\n");
        sb.append("  - AUTO_THEN_HUMAN: 尽量自动处理，处理不了再交人工\n");
        sb.append("  - HUMAN_ONLY: 遇到如验证码、登录等情况直接交人工\n\n");

        sb.append("## 要求\n");
        sb.append("- 每次只执行一个操作\n");
        sb.append("- 操作失败时尝试其他方法，不要重复同一个失败操作\n");
        sb.append("- 提取数据时，尽量提取准确的结构化数据\n");
        sb.append("- 始终以 JSON 格式返回决策，不要包含其他文本\n");

        return sb.toString();
    }

    // ==================== 决策执行 ====================

    @SuppressWarnings("unchecked")
    private String executeDecision(Page page, Map<String, Object> decision,
                                    Map<String, Object> extractedData) {
        String actionType = (String) decision.getOrDefault("actionType", "");
        try {
            switch (actionType) {
                case "navigate": {
                    String url = (String) decision.get("url");
                    if (url == null || url.isBlank()) return "缺少 url 参数";
                    page.navigate(url);
                    page.waitForLoadState(LoadState.DOMCONTENTLOADED);
                    return "已导航到: " + url;
                }
                case "click": {
                    String selector = (String) decision.get("selector");
                    if (selector == null || selector.isBlank()) return "缺少 selector 参数";
                    page.locator(selector).first().click();
                    page.waitForTimeout(500);
                    return "已点击: " + selector;
                }
                case "fill": {
                    String selector = (String) decision.get("selector");
                    String value = (String) decision.get("value");
                    if (selector == null || selector.isBlank()) return "缺少 selector 参数";
                    page.locator(selector).first().fill(value != null ? value : "");
                    return "已填写: " + selector + " = " + (value != null ? value : "");
                }
                case "select": {
                    String selector = (String) decision.get("selector");
                    String value = (String) decision.get("value");
                    if (selector == null || selector.isBlank()) return "缺少 selector 参数";
                    page.locator(selector).first().selectOption(value != null ? value : "");
                    return "已选择: " + selector + " = " + value;
                }
                case "extract": {
                    return doExtract(page, decision, extractedData);
                }
                case "wait": {
                    Object msObj = decision.get("ms");
                    long ms = msObj instanceof Number ? ((Number) msObj).longValue() : 2000;
                    page.waitForTimeout(ms);
                    return "已等待 " + ms + "ms";
                }
                case "scroll": {
                    String dir = (String) decision.getOrDefault("direction", "down");
                    if ("down".equals(dir)) {
                        page.evaluate("window.scrollBy(0, window.innerHeight)");
                    } else {
                        page.evaluate("window.scrollBy(0, -window.innerHeight)");
                    }
                    page.waitForTimeout(300);
                    return "已滚动: " + dir;
                }
                default:
                    return "未知操作: " + actionType;
            }
        } catch (Exception e) {
            log.warn("执行动作 {} 失败: {}", actionType, e.getMessage());
            return "失败: " + e.getMessage();
        }
    }

    @SuppressWarnings("unchecked")
    private String doExtract(Page page, Map<String, Object> decision,
                              Map<String, Object> extractedData) {
        Object fieldsObj = decision.get("fields");
        List<Map<String, Object>> fields;
        if (fieldsObj instanceof List) {
            fields = (List<Map<String, Object>>) fieldsObj;
        } else {
            // 提取全文
            String bodyText = page.locator("body").textContent();
            extractedData.put("_pageText", bodyText != null ? bodyText.trim() : "");
            return "已提取页面全文";
        }

        int count = 0;
        for (Map<String, Object> field : fields) {
            String key = (String) field.getOrDefault("key", "");
            if (key.isEmpty()) continue;

            String desc = (String) field.getOrDefault("description", "");
            String selector = (String) field.getOrDefault("selector", "");

            if (!selector.isEmpty()) {
                try {
                    String val = page.locator(selector).first().textContent();
                    if (val != null) extractedData.put(key, val.trim());
                    count++;
                } catch (Exception ignored) {
                }
            } else {
                extractedData.put(key, "【通过 AI 分析提取】" + desc);
                count++;
            }
        }

        return "已提取 " + count + " 个字段";
    }

    // ==================== 人工任务构建 ====================

    @SuppressWarnings("unchecked")
    private BrowserAgentResult buildManualResult(Map<String, Object> decision,
                                                  Map<String, Object> extractedData,
                                                  List<Map<String, Object>> actionHistory) {
        String reason = (String) decision.getOrDefault("reason", "需要人工处理");
        String taskGuide = (String) decision.getOrDefault("taskGuide", "请按页面提示操作");
        String taskType = (String) decision.getOrDefault("taskType", "BROWSER_AGENT_MANUAL");

        Object fieldsObj = decision.get("formFields");
        List<Map<String, Object>> formFields = new ArrayList<>();
        if (fieldsObj instanceof List) {
            formFields = (List<Map<String, Object>>) fieldsObj;
        }

        return BrowserAgentResult.manualRequired(
                reason, extractedData, actionHistory,
                taskType, taskGuide, formFields);
    }

    // ==================== JSON 工具 ====================

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJsonResponse(String response) {
        // 尝试从文本中提取 JSON 块
        Matcher matcher = JSON_BLOCK.matcher(response);
        while (matcher.find()) {
            String json = matcher.group();
            try {
                return objectMapper.readValue(json, Map.class);
            } catch (Exception ignored) {
            }
        }
        // 尝试直接解析整个响应
        try {
            return objectMapper.readValue(response, Map.class);
        } catch (Exception e) {
            log.warn("JSON 解析失败: {}", e.getMessage());
            return null;
        }
    }
}
