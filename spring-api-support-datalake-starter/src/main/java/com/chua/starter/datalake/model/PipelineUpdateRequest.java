package com.chua.starter.datalake.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Pipeline 更新请求
 */
@Data
public class PipelineUpdateRequest {

    private String id;
    private String name;
    private String version;
    private String description;
    private List<Map<String, Object>> nodes;
    private Map<String, Map<String, Object>> sinks;
}
