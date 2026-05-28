package com.chua.starter.datalake.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Pipeline 创建请求
 */
@Data
public class PipelineCreateRequest {

    private String id;
    private String name;
    private String version = "1.0.0";
    private String description;
    private List<Map<String, Object>> nodes;
    private Map<String, Map<String, Object>> sinks;
}
