package com.chua.starter.system.support;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 常见爬虫 / 自动化客户端的 User-Agent 特征预设。
 *
 * <p><b>为什么给预设而不是让管理员手打</b>：UA 里的判别特征是行业惯例
 * （{@code Googlebot}、{@code HeadlessChrome}、{@code python-requests}），
 * 管理员不一定知道某个采集器叫什么。让他从零写正则，多半写成一条过宽的规则
 * （比如直接拦 {@code Mozilla}），把正常浏览器和搜索引擎全拦掉。</p>
 *
 * <p>预设只是「一键填入」，最终仍是一条普通规则，管理员可以增删改。
 * 全部按<b>子串</b>匹配（{@link HeaderRule#MATCH_CONTAINS}），避免正则误伤。</p>
 *
 * @author CH
 * @since 4.0.0.43
 */
public final class HeaderRulePresets {

    /**
     * 全部预设（分组）
     */
    private static final Map<String, List<String>> GROUPS = new LinkedHashMap<>();

    static {
        GROUPS.put("搜索引擎爬虫", List.of(
                "Googlebot", "Google-InspectionTool", "bingbot", "Baiduspider", "YandexBot",
                "Sogou", "360Spider", "DuckDuckBot", "Applebot", "PetalBot", "Bytespider"));
        GROUPS.put("SEO / 内容抓取", List.of(
                "AhrefsBot", "SemrushBot", "MJ12bot", "DotBot", "Scrapy",
                "python-requests", "aiohttp", "curl", "Wget", "Go-http-client", "okhttp"));
        GROUPS.put("无头浏览器 / 自动化框架", List.of(
                "HeadlessChrome", "PhantomJS", "Puppeteer", "Playwright", "Selenium",
                "webdriver", "Chrome-Lighthouse", "Electron"));
        GROUPS.put("接口调试 / 压测", List.of(
                "PostmanRuntime", "Apifox", "insomnia", "HTTPie", "Swagger-UI",
                "OpenAPI", "Java-http-client", "axios", "node-fetch", "undici"));
    }

    private HeaderRulePresets() {
    }

    /**
     * 全部预设（分组）。
     *
     * @return 分组名 → 特征列表
     */
    public static Map<String, List<String>> all() {
        return GROUPS;
    }

    /**
     * 展平后的全部特征。
     *
     * <p>「一键填入」用这个：管理员点一下就有内容，再自己删不要的。</p>
     *
     * @return 特征列表
     */
    public static List<String> flat() {
        return GROUPS.values().stream().flatMap(List::stream).distinct().toList();
    }
}
