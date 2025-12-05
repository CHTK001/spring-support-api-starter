package com.chua.starter.monitor.controller;


import com.chua.common.support.discovery.Discovery;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.pojo.ClientHealthStatus;
import com.chua.starter.monitor.service.ClientHealthService;
import com.chua.sync.support.pojo.ClientInfo;
import com.chua.sync.support.server.SyncServer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 监控应用控制器
 */
@RestController
@RequestMapping("v1/app")
@Tag(name = "节点接口")
@RequiredArgsConstructor
public class NodeController {

    private final ClientHealthService clientHealthService;

    @Autowired(required = false)
    private SyncServer syncServer;
    /**
     * 获取所有健康的NODE客户端列表 用于文件管理器选择节点模式时调用
     *
     * @return NODE客户端列表
     */
    @ResponseBody
    @Operation(summary = "获取所有健康的NODE客户端", description = "获取所有在线且健康的NODE客户端列表，用于文件管理功能")
    @GetMapping("nodes/healthy")
    public ReturnResult<List<ClientHealthStatus>> getHealthyNodes() {
        try {
            return clientHealthService.getAllHealthyClients();
        } catch (Exception e) {
            return ReturnResult.error("获取健康节点列表失败: " + e.getMessage());
        }
    }

    /**
     * 检查指定节点的健康状态
     *
     * @param clientIp   客户端IP
     * @param clientPort 客户端端口
     * @return 健康状态
     */
    @ResponseBody
    @Operation(summary = "检查节点健康状态", description = "检查指定IP和端口的NODE客户端健康状态")
    @GetMapping("nodes/health")
    public ReturnResult<ClientHealthStatus> getNodeHealth(@RequestParam("clientIp") String clientIp,
                                                          @RequestParam("clientPort") Integer clientPort) {
        try {
            return clientHealthService.getClientHealth(clientIp, clientPort);
        } catch (Exception e) {
            return ReturnResult.error("获取节点健康状态失败: " + e.getMessage());
        }
    }

    /**
     * 批量检查节点在线状态
     *
     * @param clientAddresses 客户端地址列表（格式：ip:port）
     * @return 在线状态映射
     */
    @ResponseBody
    @Operation(summary = "批量检查节点在线状态", description = "批量检查多个NODE客户端的在线状态")
    @PostMapping("nodes/batch-check")
    public ReturnResult<Map<String, Boolean>> batchCheckNodeStatus(@RequestBody List<String> clientAddresses) {
        try {
            return clientHealthService.batchCheckOnlineStatus(clientAddresses);
        } catch (Exception e) {
            return ReturnResult.error("批量检查节点状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取节点健康状态统计信息
     *
     * @return 统计信息
     */
    @ResponseBody
    @Operation(summary = "获取节点健康统计", description = "获取所有NODE客户端的健康状态统计信息")
    @GetMapping("nodes/statistics")
    public ReturnResult<Object> getNodeStatistics() {
        try {
            return clientHealthService.getHealthStatistics();
        } catch (Exception e) {
            return ReturnResult.error("获取节点统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取在线监控节点列表（用于文件管理器） 直接返回在线的 Discovery 节点数据
     *
     * @return 在线节点列表
     */
    @ResponseBody
    @Operation(summary = "获取在线监控节点", description = "获取所有在线的监控节点列表，用于文件管理器选择节点模式")
    @GetMapping("nodes/online")
    public ReturnResult<List<Discovery>> getOnlineNodes() {
        try {
            // 从 SyncServer 获取所有在线客户端
            List<Discovery> onlineNodes = getOnlineClientsAsDiscovery();
            return ReturnResult.ok(onlineNodes);
        } catch (Exception e) {
            return ReturnResult.error("获取在线监控节点失败: " + e.getMessage());
        }
    }

    /**
     * 根据应用名称获取在线监控节点
     *
     * @param applicationName 应用名称（可选）
     * @return 在线节点列表
     */
    @ResponseBody
    @Operation(summary = "根据应用名称获取在线节点", description = "根据应用名称过滤获取在线的监控节点")
    @GetMapping("nodes/online/by-app")
    public ReturnResult<List<Discovery>> getOnlineNodesByApp(
            @RequestParam(value = "applicationName", required = false) String applicationName) {
        try {
            List<Discovery> allNodes = getOnlineClientsAsDiscovery();

            List<Discovery> filteredNodes;
            if (applicationName != null && !applicationName.trim().isEmpty()) {
                // 根据应用名称过滤节点
                filteredNodes = allNodes.stream().filter(discovery -> {
                    Map<String, String> metadata = discovery.getMetadata();
                    if (metadata == null) {
                        return false;
                    }
                    String appName = metadata.get("applicationName");
                    return applicationName.equals(appName);
                }).collect(java.util.stream.Collectors.toList());
            } else {
                filteredNodes = allNodes;
            }

            return ReturnResult.ok(filteredNodes);
        } catch (Exception e) {
            return ReturnResult.error("获取在线监控节点失败: " + e.getMessage());
        }
    }

    /**
     * 获取可用的节点名称列表（用于文件管理器配置） 返回所有在线监控节点的应用名称，去重后供前端选择
     *
     * @return 节点名称列表
     */
    @ResponseBody
    @Operation(summary = "获取可用节点名称", description = "获取所有在线监控节点的应用名称列表，用于NODE模式文件管理配置")
    @GetMapping("nodes/names")
    public ReturnResult<List<String>> getAvailableNodeNames() {
        try {
            List<Discovery> discoveries = getOnlineClientsAsDiscovery();
            if (discoveries.isEmpty()) {
                return ReturnResult.ok(new ArrayList<>());
            }

            // 提取应用名称并去重
            Set<String> nodeNames = discoveries.stream().map(discovery -> {
                Map<String, String> metadata = discovery.getMetadata();
                return metadata != null ? metadata.get("applicationName") : null;
            }).filter(Objects::nonNull).collect(java.util.stream.Collectors.toSet());

            List<String> sortedNames = new ArrayList<>(nodeNames);
            sortedNames.sort(String::compareTo);

            return ReturnResult.ok(sortedNames);
        } catch (Exception e) {
            return ReturnResult.error("获取节点名称列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定节点名称下的所有实例信息
     *
     * @param nodeName 节点名称
     * @return 节点实例列表
     */
    @ResponseBody
    @Operation(summary = "获取节点实例信息", description = "获取指定节点名称下的所有在线实例信息")
    @GetMapping("nodes/instances")
    public ReturnResult<List<Discovery>> getNodeInstances(@RequestParam("nodeName") String nodeName) {
        try {
            if (nodeName == null || nodeName.trim().isEmpty()) {
                return ReturnResult.error("节点名称不能为空");
            }

            List<Discovery> discoveries = getOnlineClientsAsDiscovery();
            if (discoveries.isEmpty()) {
                return ReturnResult.ok(new ArrayList<>());
            }

            // 过滤出匹配节点名称的实例
            List<Discovery> instances = discoveries.stream().filter(discovery -> {
                Map<String, String> metadata = discovery.getMetadata();
                if (metadata == null) {
                    return false;
                }
                String applicationName = metadata.get("applicationName");
                return nodeName.equals(applicationName);
            }).collect(java.util.stream.Collectors.toList());

            return ReturnResult.ok(instances);
        } catch (Exception e) {
            return ReturnResult.error("获取节点实例信息失败: " + e.getMessage());
        }
    }

    /**
     * 检查服务器关联的NODE客户端健康状态
     *
     * @param serverId 服务器ID
     * @return 是否健康
     */
    @ResponseBody
    @Operation(summary = "检查服务器NODE客户端健康状态", description = "检查指定服务器关联的NODE客户端是否健康")
    @GetMapping("nodes/server/{serverId}/health")
    public ReturnResult<Boolean> checkServerNodeHealth(@PathVariable("serverId") Integer serverId) {
        try {
            return clientHealthService.isServerClientHealthy(serverId);
        } catch (Exception e) {
            return ReturnResult.error("检查服务器节点健康状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取服务器关联的NODE客户端详细健康状态
     *
     * @param serverId 服务器ID
     * @return 客户端健康状态详情
     */
    @ResponseBody
    @Operation(summary = "获取服务器NODE客户端详细状态", description = "获取指定服务器关联的NODE客户端详细健康状态信息")
    @GetMapping("nodes/server/{serverId}/detail")
    public ReturnResult<ClientHealthStatus> getServerNodeHealthDetail(@PathVariable("serverId") Integer serverId) {
        try {
            return clientHealthService.getServerClientHealth(serverId);
        } catch (Exception e) {
            return ReturnResult.error("获取服务器节点详细状态失败: " + e.getMessage());
        }
    }

    /**
     * 从 SyncServer 获取所有在线客户端并转换为 Discovery 列表
     */
    private List<Discovery> getOnlineClientsAsDiscovery() {
        if (syncServer == null) {
            return new ArrayList<>();
        }

        List<Discovery> result = new ArrayList<>();
        for (Map.Entry<String, ClientInfo> entry : syncServer.getAllClients().entrySet()) {
            ClientInfo clientInfo = entry.getValue();

            Discovery discovery = new Discovery();
            discovery.setServerId(entry.getKey());
            discovery.setHost(clientInfo.getIpAddress());
            discovery.setPort(clientInfo.getPort());

            Map<String, String> metadata = new HashMap<>();
            metadata.put("applicationName", clientInfo.getAppName());
            metadata.put("instanceId", clientInfo.getInstanceId());
            metadata.put("hostname", clientInfo.getHostname());
            if (clientInfo.getMetadata() != null) {
                clientInfo.getMetadata().forEach((k, v) -> metadata.put(k, String.valueOf(v)));
            }
            discovery.setMetadata(metadata);

            result.add(discovery);
        }
        return result;
    }
}
