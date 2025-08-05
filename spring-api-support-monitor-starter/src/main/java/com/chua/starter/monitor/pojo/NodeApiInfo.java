package com.chua.starter.monitor.pojo;

import com.chua.starter.monitor.starter.pojo.NodeApiParameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 节点API信息
 *
 * @author CH
 * @since 2024/12/30
 */
@Data
@Schema(description = "节点API信息")
public class NodeApiInfo {

    @Schema(description = "API路径")
    private String path;

    @Schema(description = "HTTP方法")
    private String method;

    @Schema(description = "API摘要")
    private String summary;

    @Schema(description = "API描述")
    private String description;

    @Schema(description = "参数列表")
    private List<NodeApiParameter> parameters;

    @Schema(description = "请求体")
    private Map<String, Object> requestBody;

    @Schema(description = "响应信息")
    private Map<String, Object> responses;

    public NodeApiInfo() {}

    public NodeApiInfo(String path, String method) {
        this.path = path;
        this.method = method;
    }

    public NodeApiInfo(String path, String method, String summary) {
        this.path = path;
        this.method = method;
        this.summary = summary;
    }
}
