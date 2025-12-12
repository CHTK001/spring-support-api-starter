package com.chua.starter.monitor.controller.node;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.node.NodeUrlMappingDTO;
import com.chua.starter.monitor.pojo.node.NodeUrlQpsDTO;
import com.chua.starter.monitor.service.node.NodeUrlQpsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 节点 URL QPS 统计控制器
 * <p>
 * 提供节点 URL 映射查询、QPS 统计查询等功能
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2025/01/17
 */
@Slf4j
@RestController
@RequestMapping("/v1/node/url")
@RequiredArgsConstructor
@Tag(name = "节点 URL 统计管理", description = "节点 URL 映射和 QPS 统计相关接口")
public class NodeUrlQpsController {

    private final NodeUrlQpsService nodeUrlQpsService;

    /**
     * 获取节点的 URL 映射列表
     *
     * @param nodeUrl 节点URL（Base64编码）
     * @return URL 映射列表
     */
    @GetMapping("/mappings")
    @Operation(summary = "获取节点 URL 映射列表", description = "通过 actuator 获取节点的所有 URL 映射信息")
    public ReturnResult<List<NodeUrlMappingDTO>> getMappingsForUrl(
            @Parameter(description = "节点URL（Base64编码）", required = true)
            @RequestParam String nodeUrl) {

        log.info("[UrlQps] 获取 URL 映射列表: nodeUrl={}", nodeUrl);
        return nodeUrlQpsService.getNodeUrlMappings(nodeUrl);
    }

    /**
     * 获取节点的 URL QPS 统计
     *
     * @param nodeUrl 节点URL（Base64编码）
     * @return QPS 统计列表
     */
    @GetMapping("/qps")
    @Operation(summary = "获取节点 URL QPS 统计", description = "获取节点上报的 URL 请求统计数据")
    public ReturnResult<List<NodeUrlQpsDTO>> getQpsForUrl(
            @Parameter(description = "节点URL（Base64编码）", required = true)
            @RequestParam String nodeUrl) {

        log.info("[UrlQps] 获取 URL QPS 统计: nodeUrl={}", nodeUrl);
        return nodeUrlQpsService.getNodeUrlQps(nodeUrl);
    }

    /**
     * 获取节点的 QPS 汇总统计
     *
     * @param nodeUrl 节点URL（Base64编码）
     * @return QPS 汇总统计
     */
    @GetMapping("/qps/summary")
    @Operation(summary = "获取节点 QPS 汇总统计", description = "获取节点的整体 QPS 汇总信息")
    public ReturnResult<Map<String, Object>> getQpsSummaryForUrl(
            @Parameter(description = "节点URL（Base64编码）", required = true)
            @RequestParam String nodeUrl) {

        log.info("[UrlQps] 获取 QPS 汇总统计: nodeUrl={}", nodeUrl);
        return nodeUrlQpsService.getNodeQpsSummary(nodeUrl);
    }

    /**
     * 清除节点的 QPS 统计数据
     *
     * @param nodeUrl 节点URL（Base64编码）
     * @return 清除结果
     */
    @DeleteMapping("/qps/clear")
    @Operation(summary = "清除节点 QPS 数据", description = "清除指定节点的 QPS 统计数据缓存")
    public ReturnResult<Boolean> clearQpsDataForUrl(
            @Parameter(description = "节点URL（Base64编码）", required = true)
            @RequestParam String nodeUrl) {

        log.info("[UrlQps] 清除 QPS 数据: nodeUrl={}", nodeUrl);
        nodeUrlQpsService.clearNodeQpsData(nodeUrl);
        return ReturnResult.success(true);
    }
}
