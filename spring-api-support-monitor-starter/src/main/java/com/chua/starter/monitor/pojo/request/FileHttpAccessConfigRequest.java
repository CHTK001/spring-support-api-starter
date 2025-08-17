package com.chua.starter.monitor.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文件HTTP访问配置请求
 */
@Data
@Schema(description = "文件HTTP访问配置请求")
public class FileHttpAccessConfigRequest {

    /**
     * 已有服务ID（可选）
     */
    @Schema(description = "已有服务ID（可选）")
    private Integer serviceId;

    /**
     * 端口（必填）
     */
    @Schema(description = "端口（必填）")
    private Integer port;

    /**
     * 服务名称（可选）
     */
    @Schema(description = "服务名称（可选）")
    private String name;

    /**
     * 主机（默认0.0.0.0）
     */
    @Schema(description = "主机（默认0.0.0.0）")
    private String host;

    /**
     * 类型（默认HTTP）
     */
    @Schema(description = "类型（默认HTTP）")
    private String type;

    /**
     * 上下文路径（默认/）
     */
    @Schema(description = "上下文路径（默认/）")
    private String contextPath;
}

