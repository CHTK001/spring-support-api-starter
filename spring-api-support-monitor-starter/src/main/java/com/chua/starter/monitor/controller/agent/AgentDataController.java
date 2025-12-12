package com.chua.starter.monitor.controller.agent;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.agent.AgentFileHandleDTO;
import com.chua.starter.monitor.pojo.agent.AgentServiceInstanceDTO;
import com.chua.starter.monitor.pojo.agent.AgentTraceDTO;
import com.chua.starter.monitor.service.agent.AgentDataStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Agent 数据查询控制器
 * <p>
 * 提供 Agent 上报数据的查询接口，包括链路追踪、服务拓扑、文件句柄等
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2025/01/17
 */
@Slf4j
@RestController
@RequestMapping("/v1/agent/data")
@RequiredArgsConstructor
@Tag(name = "Agent 数据查询", description = "Agent 上报数据的查询接口")
@ConditionalOnBean(AgentDataStorageService.class)
public class AgentDataController {

    private final AgentDataStorageService agentDataStorageService;

    // ==================== 链路追踪查询 ====================

    /**
     * 搜索历史链路追踪
     *
     * @param keyword   关键词
     * @param startTime 开始时间（毫秒时间戳）
     * @param endTime   结束时间（毫秒时间戳）
     * @param page      页码
     * @param pageSize  每页数量
     * @return 链路追踪列表
     */
    @GetMapping("/trace/search")
    @Operation(summary = "搜索历史链路追踪", description = "根据关键词和时间范围搜索链路追踪数据")
    public ReturnResult<List<AgentTraceDTO>> searchTracesForAgent(
            @Parameter(description = "关键词（支持 linkId、method、address 等）") 
            @RequestParam(required = false) String keyword,
            @Parameter(description = "开始时间（毫秒时间戳）") 
            @RequestParam(required = false) Long startTime,
            @Parameter(description = "结束时间（毫秒时间戳）") 
            @RequestParam(required = false) Long endTime,
            @Parameter(description = "页码") 
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") 
            @RequestParam(defaultValue = "20") Integer pageSize) {
        
        int offset = (page - 1) * pageSize;
        return agentDataStorageService.searchTracesForAgent(keyword, startTime, endTime, offset, pageSize);
    }

    /**
     * 获取完整链路详情
     *
     * @param linkId 链路ID
     * @return 完整链路信息
     */
    @GetMapping("/trace/{linkId}")
    @Operation(summary = "获取完整链路详情", description = "根据链路ID获取完整的链路追踪树")
    public ReturnResult<AgentTraceDTO> getTraceByLinkIdForAgent(
            @Parameter(description = "链路ID") @PathVariable String linkId) {
        return agentDataStorageService.getTraceByLinkIdForAgent(linkId);
    }

    // ==================== 服务拓扑查询（关系图谱）====================

    /**
     * 获取服务拓扑图数据
     *
     * @return 服务拓扑数据（节点和边）
     */
    @GetMapping("/topology")
    @Operation(summary = "获取服务拓扑图", description = "获取服务之间的调用关系，用于绘制关系图谱")
    public ReturnResult<Map<String, Object>> getServiceTopologyForAgent() {
        return agentDataStorageService.getServiceTopologyForAgent();
    }

    /**
     * 获取指定服务的调用关系
     *
     * @param serviceName 服务名称
     * @return 服务调用关系列表
     */
    @GetMapping("/topology/service")
    @Operation(summary = "获取服务调用关系", description = "获取指定服务的上下游调用关系")
    public ReturnResult<List<AgentServiceInstanceDTO>> getServiceRelationsForAgent(
            @Parameter(description = "服务名称或IP:端口") @RequestParam String serviceName) {
        return agentDataStorageService.getServiceRelationsForAgent(serviceName);
    }

    // ==================== 文件句柄查询 ====================

    /**
     * 获取指定节点的文件句柄列表
     *
     * @param ipAddress IP地址
     * @param port      端口
     * @return 文件句柄列表
     */
    @GetMapping("/fd/list")
    @Operation(summary = "获取节点文件句柄", description = "获取指定节点打开的文件句柄列表")
    public ReturnResult<List<AgentFileHandleDTO>> getFileHandlesForAgent(
            @Parameter(description = "节点IP地址") @RequestParam String ipAddress,
            @Parameter(description = "节点端口") @RequestParam Integer port) {
        return agentDataStorageService.getFileHandlesForAgent(ipAddress, port);
    }

    /**
     * 获取所有节点的文件句柄统计
     *
     * @return 文件句柄统计信息
     */
    @GetMapping("/fd/stats")
    @Operation(summary = "获取文件句柄统计", description = "获取所有节点的文件句柄统计信息")
    public ReturnResult<Map<String, Object>> getFileHandleStatsForAgent() {
        return agentDataStorageService.getFileHandleStatsForAgent();
    }
}
