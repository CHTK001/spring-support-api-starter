package com.chua.starter.monitor.pojo.spider;

import lombok.Data;

import java.util.List;

/**
 * Spider AI 状态。
 */
@Data
public class SpiderAiStatus {

    private Boolean enabled;

    private Boolean available;

    private String defaultModel;

    private List<String> supportedModels;

    private String message;
}
