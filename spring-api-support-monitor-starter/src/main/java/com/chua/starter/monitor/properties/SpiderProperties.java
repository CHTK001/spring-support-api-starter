package com.chua.starter.monitor.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 爬虫模块配置
 *
 * @author CH
 * @since 2026/03/27
 */
@Data
@ConfigurationProperties(prefix = SpiderProperties.PRE)
public class SpiderProperties {

    public static final String PRE = "plugin.monitor.spider";

    /**
     * 是否启用爬虫模块
     */
    private boolean enabled = true;

    /**
     * 启动时自动初始化数据库表
     */
    private boolean autoCreateTable = false;

    /**
     * 预览/解析页面时的请求超时时间（秒）
     */
    private int requestTimeoutSeconds = 30;

    /**
     * 可视化 DOM 解析最大深度
     */
    private int previewMaxDepth = 10;

    /**
     * 每层最多保留的子节点数量
     */
    private int previewMaxChildren = 50;

    /**
     * 文本预览最大长度
     */
    private int previewTextLimit = 200;

    /**
     * spider 轻页面认证配置
     */
    private WebAuthConfig webAuth = new WebAuthConfig();

    /**
     * spider AI 配置
     */
    private AiConfig ai = new AiConfig();

    @Data
    public static class WebAuthConfig {

        /**
         * 认证模式：embedded / none
         */
        private String mode = "embedded";

        /**
         * embedded 模式用户名
         */
        private String username = "admin";

        /**
         * embedded 模式密码
         */
        private String password = "admin123";

        /**
         * Session 超时时间（秒）
         */
        private Integer sessionTimeout = 3600;
    }

    @Data
    public static class AiConfig {

        /**
         * 是否启用 Spider AI 分析
         */
        private boolean enabled = true;

        /**
         * 默认使用的模型编码，为空时使用全局默认模型
         */
        private String defaultModel;

        /**
         * 单次分析的最大 tokens
         */
        private Integer maxTokens = 1800;

        /**
         * 模型温度
         */
        private Double temperature = 0.2D;

        /**
         * 参与分析的 HTML 最大字符数
         */
        private Integer maxHtmlChars = 12000;

        /**
         * 参与分析的 DOM 节点摘要上限
         */
        private Integer maxNodeCount = 40;

        /**
         * 选择器命中摘要上限
         */
        private Integer maxSelectorMatches = 8;

        /**
         * Spider AI 的系统提示词
         */
        private String systemPrompt = "你是资深网页爬虫分析助手。请基于页面结构、HTML片段和当前选择器，"
                + "给出适合工程落地的提取方案。输出中文，优先说明页面用途、建议抓取字段、"
                + "推荐CSS/XPath、稳定性风险、清洗规则和下一步实验建议。";
    }
}
