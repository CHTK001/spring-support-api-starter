package com.chua.starter.monitor.pojo.node;

import com.chua.starter.monitor.pojo.node.NodeApiInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 节点API执行请求
 *
 * @author CH
 * @since 2024/12/30
 */
@Data
@Schema(description = "节点API执行请求")
public class NodeApiExecuteRequest {

    @Schema(description = "节点ID")
    private String nodeId;

    @Schema(description = "节点地址")
    private String nodeAddress;

    @Schema(description = "上下文路径")
    private String contextPath;

    @Schema(description = "API信息")
    private NodeApiInfo api;

    @Schema(description = "路径参数")
    private Map<String, String> pathParams;

    @Schema(description = "查询参数")
    private Map<String, String> queryParams;

    @Schema(description = "请求体")
    private String requestBody;

    @Schema(description = "请求头")
    private Map<String, String> headers;

    @Schema(description = "超时时间(秒)")
    private Integer timeout = 30;

    public NodeApiExecuteRequest() {}

    public NodeApiExecuteRequest(String nodeId, String nodeAddress, NodeApiInfo api) {
        this.nodeId = nodeId;
        this.nodeAddress = nodeAddress;
        this.api = api;
    }
}
