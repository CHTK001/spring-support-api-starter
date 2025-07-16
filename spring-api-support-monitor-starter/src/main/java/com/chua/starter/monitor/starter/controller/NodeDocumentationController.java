package com.chua.starter.monitor.starter.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.pojo.NodeApiExecuteRequest;
import com.chua.starter.monitor.starter.pojo.NodeApiGroup;
import com.chua.starter.monitor.starter.service.NodeDocumentationService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 节点文档控制器
 *
 * @author CH
 * @since 2024/12/30
 */
@RestController
@RequestMapping("/v1/monitor/nodes")
@Api(tags = "节点文档管理")
@Tag(name = "节点文档管理")
@RequiredArgsConstructor
@Slf4j
public class NodeDocumentationController {

    private final NodeDocumentationService nodeDocumentationService;

    /**
     * 获取节点API文档
     *
     * @param nodeId      节点ID
     * @param nodeAddress 节点地址
     * @param contextPath 上下文路径
     * @return API文档列表
     */
    @GetMapping("/api-docs")
    @Operation(summary = "获取节点API文档", description = "获取指定节点的API文档信息")
    public ReturnResult<List<NodeApiGroup>> getNodeApiDocs(
            @Parameter(name = "节点ID") @RequestParam String nodeId,
            @Parameter(name = "节点地址") @RequestParam String nodeAddress,
            @Parameter(name = "上下文路径") @RequestParam(required = false, defaultValue = "") String contextPath) {
        try {
            log.info("获取节点API文档: nodeId={}, nodeAddress={}, contextPath={}", nodeId, nodeAddress, contextPath);
            return nodeDocumentationService.getNodeApiDocs(nodeId, nodeAddress, contextPath);
        } catch (Exception e) {
            log.error("获取节点API文档失败: nodeId={}, nodeAddress={}", nodeId, nodeAddress, e);
            return ReturnResult.error("获取API文档失败: " + e.getMessage());
        }
    }

    /**
     * 执行节点API
     *
     * @param request 执行请求
     * @return 执行结果
     */
    @PostMapping("/execute-api")
    @Operation(summary = "执行节点API", description = "执行指定节点的API接口")
    public ReturnResult<Map<String, Object>> executeNodeApi(@RequestBody NodeApiExecuteRequest request) {
        try {
            log.info("执行节点API: nodeId={}, path={}, method={}", 
                    request.getNodeId(), request.getApi().getPath(), request.getApi().getMethod());
            return nodeDocumentationService.executeNodeApi(request);
        } catch (Exception e) {
            log.error("执行节点API失败: nodeId={}, path={}", 
                    request.getNodeId(), request.getApi().getPath(), e);
            return ReturnResult.error("执行API失败: " + e.getMessage());
        }
    }

    /**
     * 获取节点健康状态
     *
     * @param nodeId 节点ID
     * @return 健康状态
     */
    @GetMapping("/{nodeId}/health")
    @Operation(summary = "获取节点健康状态", description = "获取指定节点的健康状态信息")
    public ReturnResult<Map<String, Object>> getNodeHealth(
            @Parameter(name = "节点ID") @PathVariable String nodeId) {
        try {
            log.info("获取节点健康状态: nodeId={}", nodeId);
            return nodeDocumentationService.getNodeHealth(nodeId);
        } catch (Exception e) {
            log.error("获取节点健康状态失败: nodeId={}", nodeId, e);
            return ReturnResult.error("获取健康状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取节点基本信息
     *
     * @param nodeId 节点ID
     * @return 节点信息
     */
    @GetMapping("/{nodeId}/info")
    @Operation(summary = "获取节点基本信息", description = "获取指定节点的基本信息")
    public ReturnResult<Map<String, Object>> getNodeInfo(
            @Parameter(name = "节点ID") @PathVariable String nodeId) {
        try {
            log.info("获取节点基本信息: nodeId={}", nodeId);
            return nodeDocumentationService.getNodeInfo(nodeId);
        } catch (Exception e) {
            log.error("获取节点基本信息失败: nodeId={}", nodeId, e);
            return ReturnResult.error("获取节点信息失败: " + e.getMessage());
        }
    }
}
