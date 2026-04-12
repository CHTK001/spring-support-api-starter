package com.chua.starter.monitor.pojo.spider;

import lombok.Data;

/**
 * Spider AI 分析响应。
 */
@Data
public class SpiderAiAnalyzeResponse {

    private Boolean available;

    private String model;

    private String goal;

    private String pageDigest;

    private String selectorDigest;

    private String analysis;
}
