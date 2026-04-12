package com.chua.starter.monitor.service.spider;

import com.chua.common.support.core.utils.StringUtils;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.deeplearning.support.ml.bigmodel.BigModelResponse;
import com.chua.starter.monitor.pojo.spider.SpiderAiAnalyzeRequest;
import com.chua.starter.monitor.pojo.spider.SpiderAiAnalyzeResponse;
import com.chua.starter.monitor.pojo.spider.SpiderAiStatus;
import com.chua.starter.monitor.properties.SpiderProperties;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.project.pojo.bigmodel.BigModelRequest;
import com.chua.starter.project.service.BigModelService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Spider AI 分析服务。
 */
@Slf4j
public class SpiderAiService {

    private final BigModelService bigModelService;
    private final SpiderProperties spiderProperties;

    public SpiderAiService(BigModelService bigModelService, SpiderProperties spiderProperties) {
        this.bigModelService = bigModelService;
        this.spiderProperties = spiderProperties;
    }

    public SpiderAiStatus status() {
        SpiderAiStatus status = new SpiderAiStatus();
        SpiderProperties.AiConfig aiConfig = spiderProperties.getAi();
        status.setEnabled(aiConfig != null && aiConfig.isEnabled());
        status.setDefaultModel(aiConfig == null ? null : aiConfig.getDefaultModel());

        if (aiConfig == null || !aiConfig.isEnabled()) {
            status.setAvailable(false);
            status.setMessage("Spider AI 已关闭，请开启 plugin.monitor.spider.ai.enabled");
            status.setSupportedModels(List.of());
            return status;
        }

        try {
            ReturnResult<String[]> result = bigModelService.models();
            List<String> models = result != null && result.getData() != null
                    ? Arrays.asList(result.getData())
                    : List.of();
            status.setSupportedModels(models);
            status.setAvailable(true);
            status.setMessage(models.isEmpty() ? "已启用，但未读取到模型列表" : "Spider AI 已就绪");
            return status;
        } catch (Exception e) {
            log.warn("[Spider][AI] 读取模型状态失败: {}", e.getMessage());
            status.setAvailable(false);
            status.setSupportedModels(List.of());
            status.setMessage("Spider AI 当前不可用: " + e.getMessage());
            return status;
        }
    }

    public SpiderAiAnalyzeResponse analyzePage(SpiderAiAnalyzeRequest request) {
        SpiderProperties.AiConfig aiConfig = requireAiEnabled();
        if (request == null) {
            throw new IllegalArgumentException("分析请求不能为空");
        }

        Document document = resolveDocument(request);
        String pageDigest = buildPageDigest(document, request, aiConfig);
        String selectorDigest = buildSelectorDigest(document, request, aiConfig);
        String goal = resolveGoal(request);
        String model = StringUtils.isNotEmpty(request.getModel())
                ? request.getModel()
                : aiConfig.getDefaultModel();

        BigModelRequest bigModelRequest = BigModelRequest.builder()
                .model(StringUtils.isEmpty(model) ? null : model)
                .system(resolveSystemPrompt(aiConfig))
                .user(buildUserPrompt(request, goal, pageDigest, selectorDigest))
                .tokens(aiConfig.getMaxTokens())
                .temperature(aiConfig.getTemperature())
                .build();

        ReturnResult<BigModelResponse> result = bigModelService.call(new Query<>(), bigModelRequest);
        if (result == null) {
            throw new IllegalStateException("AI 分析失败: 未返回结果");
        }
        if (!result.isOk()) {
            throw new IllegalStateException("AI 分析失败: " + result.getMsg());
        }

        BigModelResponse response = result.getData();
        SpiderAiAnalyzeResponse analyzeResponse = new SpiderAiAnalyzeResponse();
        analyzeResponse.setAvailable(true);
        analyzeResponse.setModel(StringUtils.isNotEmpty(model) ? model : aiConfig.getDefaultModel());
        analyzeResponse.setGoal(goal);
        analyzeResponse.setPageDigest(pageDigest);
        analyzeResponse.setSelectorDigest(selectorDigest);
        analyzeResponse.setAnalysis(resolveOutput(response));
        return analyzeResponse;
    }

    private SpiderProperties.AiConfig requireAiEnabled() {
        SpiderProperties.AiConfig aiConfig = spiderProperties.getAi();
        if (aiConfig == null || !aiConfig.isEnabled()) {
            throw new IllegalStateException("Spider AI 未启用");
        }
        return aiConfig;
    }

    private String resolveSystemPrompt(SpiderProperties.AiConfig aiConfig) {
        return StringUtils.defaultString(aiConfig.getSystemPrompt(),
                "你是资深网页爬虫分析助手，请输出可直接落地的抓取建议。");
    }

    private String resolveGoal(SpiderAiAnalyzeRequest request) {
        if (StringUtils.isNotEmpty(request.getGoal())) {
            return request.getGoal();
        }
        if (StringUtils.isNotEmpty(request.getQuestion())) {
            return request.getQuestion();
        }
        return "请分析当前页面的抓取策略，并给出字段建议和稳定选择器建议。";
    }

    private String buildUserPrompt(
            SpiderAiAnalyzeRequest request,
            String goal,
            String pageDigest,
            String selectorDigest) {
        StringBuilder builder = new StringBuilder(4096);
        builder.append("分析目标:\n").append(goal).append("\n\n");
        builder.append("页面信息:\n");
        if (StringUtils.isNotEmpty(request.getUrl())) {
            builder.append("- URL: ").append(request.getUrl()).append("\n");
        }
        if (StringUtils.isNotEmpty(request.getTitle())) {
            builder.append("- Title: ").append(request.getTitle()).append("\n");
        }
        if (StringUtils.isNotEmpty(request.getSelector())) {
            builder.append("- 当前选择器: ").append(request.getSelector()).append("\n");
            builder.append("- 选择器类型: ")
                    .append(StringUtils.isBlank(request.getSelectorType()) ? "XPATH" : request.getSelectorType())
                    .append("\n");
        }
        builder.append("\n页面摘要:\n").append(pageDigest).append("\n\n");
        if (StringUtils.isNotEmpty(selectorDigest)) {
            builder.append("选择器摘要:\n").append(selectorDigest).append("\n\n");
        }
        builder.append("请按下面结构输出:\n")
                .append("1. 页面用途判断\n")
                .append("2. 建议抓取字段清单（字段名、含义、优先级）\n")
                .append("3. 推荐选择器（优先 CSS，其次 XPath，每个字段至少给一个）\n")
                .append("4. 稳定性风险与反爬/动态渲染判断\n")
                .append("5. 数据清洗与去重建议\n")
                .append("6. 下一步实验建议\n");
        return builder.toString();
    }

    private Document resolveDocument(SpiderAiAnalyzeRequest request) {
        if (StringUtils.isNotEmpty(request.getHtml())) {
            return Jsoup.parse(limit(request.getHtml(), spiderProperties.getAi().getMaxHtmlChars()),
                    StringUtils.defaultString(request.getUrl(), ""));
        }
        if (StringUtils.isEmpty(request.getUrl())) {
            throw new IllegalArgumentException("url 或 html 至少提供一个");
        }
        try {
            return Jsoup.connect(request.getUrl())
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(Math.max(1, spiderProperties.getRequestTimeoutSeconds()) * 1000)
                    .get();
        } catch (Exception e) {
            throw new IllegalStateException("加载页面失败: " + e.getMessage(), e);
        }
    }

    private String buildPageDigest(
            Document document,
            SpiderAiAnalyzeRequest request,
            SpiderProperties.AiConfig aiConfig) {
        StringBuilder builder = new StringBuilder(4096);
        builder.append("title=").append(document.title()).append('\n');
        builder.append("baseUrl=").append(document.baseUri()).append('\n');

        Element body = document.body();
        if (body == null) {
            builder.append("body=empty");
            return builder.toString();
        }

        builder.append("headings=").append(joinTexts(document.select("h1, h2, h3"), 8, 80)).append('\n');
        builder.append("links=").append(joinTexts(document.select("a[href]"), 10, 80)).append('\n');
        builder.append("tables=").append(document.select("table").size()).append('\n');
        builder.append("forms=").append(document.select("form").size()).append('\n');
        builder.append("lists=").append(document.select("ul, ol").size()).append('\n');
        builder.append("images=").append(document.select("img").size()).append('\n');

        List<String> nodeSummaries = new ArrayList<>();
        Elements elements = body.select("*");
        int maxNodeCount = Math.max(1, aiConfig.getMaxNodeCount());
        for (Element element : elements) {
            if (nodeSummaries.size() >= maxNodeCount) {
                break;
            }
            String text = StringUtils.isBlank(element.ownText()) ? element.text() : element.ownText();
            if (StringUtils.isBlank(text) && element.attributes().size() == 0) {
                continue;
            }
            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("tag", element.tagName());
            if (StringUtils.isNotEmpty(element.id())) {
                summary.put("id", element.id());
            }
            if (StringUtils.isNotEmpty(element.className())) {
                summary.put("class", limit(element.className(), 80));
            }
            if (StringUtils.isNotEmpty(text)) {
                summary.put("text", limit(text, 120));
            }
            if (element.hasAttr("href")) {
                summary.put("href", limit(element.attr("href"), 120));
            }
            nodeSummaries.add(summary.toString());
        }
        builder.append("domSamples=").append(String.join("\n", nodeSummaries)).append('\n');

        String html = StringUtils.isNotEmpty(request.getHtml()) ? request.getHtml() : document.html();
        builder.append("htmlSnippet=\n").append(limit(html, aiConfig.getMaxHtmlChars()));
        return builder.toString();
    }

    private String buildSelectorDigest(
            Document document,
            SpiderAiAnalyzeRequest request,
            SpiderProperties.AiConfig aiConfig) {
        if (request == null || StringUtils.isEmpty(request.getSelector())) {
            return "";
        }
        Elements elements;
        try {
            if ("CSS".equalsIgnoreCase(request.getSelectorType())) {
                elements = document.select(request.getSelector());
            } else {
                elements = document.selectXpath(request.getSelector());
            }
        } catch (Exception e) {
            return "selectorError=" + e.getMessage();
        }

        StringBuilder builder = new StringBuilder(1024);
        builder.append("matchCount=").append(elements.size()).append('\n');
        int limit = Math.max(1, aiConfig.getMaxSelectorMatches());
        for (int i = 0; i < elements.size() && i < limit; i++) {
            Element element = elements.get(i);
            builder.append("- ")
                    .append(element.tagName())
                    .append(" | text=")
                    .append(limit(StringUtils.isBlank(element.ownText()) ? element.text() : element.ownText(), 120))
                    .append(" | outerHtml=")
                    .append(limit(element.outerHtml(), 180))
                    .append('\n');
        }
        if (elements.size() > limit) {
            builder.append("...truncated=").append(elements.size() - limit);
        }
        return builder.toString();
    }

    private String joinTexts(Elements elements, int maxItems, int maxLength) {
        List<String> result = new ArrayList<>();
        for (Element element : elements) {
            if (result.size() >= maxItems) {
                break;
            }
            String value = StringUtils.isBlank(element.ownText()) ? element.text() : element.ownText();
            if (StringUtils.isBlank(value)) {
                continue;
            }
            result.add(limit(value, maxLength));
        }
        return result.isEmpty() ? "-" : String.join(" | ", result);
    }

    private String resolveOutput(BigModelResponse response) {
        if (response == null) {
            return "";
        }
        String output = response.getOutput();
        return StringUtils.isBlank(output) ? String.valueOf(response) : output;
    }

    private String limit(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, Math.max(0, maxLength)) + "...";
    }
}
