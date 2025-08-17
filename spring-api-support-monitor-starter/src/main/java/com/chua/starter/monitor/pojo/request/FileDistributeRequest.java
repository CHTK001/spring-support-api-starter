package com.chua.starter.monitor.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 文件分发请求
 */
@Data
@Schema(description = "文件分发请求")
public class FileDistributeRequest {

    /**
     * 源服务器ID
     */
    @Schema(description = "源服务器ID", required = true)
    private Integer sourceServerId;

    /**
     * 源文件绝对路径
     */
    @Schema(description = "源文件绝对路径", required = true)
    private String sourceFilePath;

    /**
     * 目标类型：SERVER 或 NODE
     */
    @Schema(description = "目标类型：SERVER 或 NODE", required = true, example = "SERVER")
    private String targetType;

    /**
     * 目标ID列表：当 targetType=SERVER 时为服务器ID；当 targetType=NODE 时为节点ID
     */
    @Schema(description = "目标ID列表：SERVER为服务器ID，NODE为节点ID", required = true)
    private List<String> targetIds;

    /**
     * 目标目录
     */
    @Schema(description = "目标目录", required = true)
    private String targetDir;

    /**
     * 是否覆盖
     */
    @Schema(description = "是否覆盖")
    private Boolean overwrite;
}

