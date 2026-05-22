package com.chua.starter.project.pojo.bigmodel;

import lombok.Builder;
import lombok.Data;

/**
 * BigModelRequest 存根 — 在缺少 spring-api-support-common-project-starter 时的替换实现
 */
@Data
@Builder
public class BigModelRequest {
    private String model;
    private String system;
    private String user;
    @Builder.Default
    private Integer tokens = 2048;
    @Builder.Default
    private Double temperature = 0.3D;
}
