package com.chua.starter.monitor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 安装请求（JSON Body）
 *
 * @author CH
 * @since 2025/08/14
 */
@Data
@Schema(name = "SystemSoftInstallRequest", description = "软件安装请求体")
public class SystemSoftInstallRequest {
    @Schema(description = "软件ID", example = "1")
    private Integer systemSoftId;

    @Schema(description = "软件版本ID", example = "10")
    private Integer systemSoftVersionId;

    @Schema(description = "服务器ID集合", example = "[1,2,3]")
    private Integer[] serverIds;

    @Schema(description = "安装方式（DOCKER_CLI/COMPOSE/SWARM）", example = "DOCKER_CLI")
    private String method; // 可选，覆盖默认

    @Schema(description = "安装参数（JSON文本）", example = "{\"env\":[\"TZ=Asia/Shanghai\"],\"ports\":[\"8080:80\"]}")
    private String params; // 可选，覆盖默认
}
