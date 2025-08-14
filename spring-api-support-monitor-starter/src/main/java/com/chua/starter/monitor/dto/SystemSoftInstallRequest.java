package com.chua.starter.monitor.dto;

import lombok.Data;

/**
 * 安装请求（JSON Body）
 */
@Data
public class SystemSoftInstallRequest {
    private Integer systemSoftId;
    private Integer systemSoftVersionId;
    private Integer[] serverIds;
    private String method; // DOCKER_CLI/COMPOSE/SWARM（可选，覆盖默认）
    private String params; // JSON 文本（可选，覆盖默认）
}

