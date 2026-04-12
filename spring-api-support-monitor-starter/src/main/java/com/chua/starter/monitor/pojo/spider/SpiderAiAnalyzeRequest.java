package com.chua.starter.monitor.pojo.spider;

import lombok.Data;

/**
 * Spider AI 分析请求。
 */
@Data
public class SpiderAiAnalyzeRequest {

    private String url;

    private String title;

    private String html;

    private String selector;

    private String selectorType;

    private String goal;

    private String question;

    private String model;
}
