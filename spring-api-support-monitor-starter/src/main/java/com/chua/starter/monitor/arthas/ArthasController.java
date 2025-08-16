package com.chua.starter.monitor.arthas;

import com.chua.common.support.lang.code.ReturnResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Arthas 管理接口
 *
 * 提供：
 * - 查询节点端 Arthas 配置（是否启用、tunnel 地址）
 * - 查询/设置服务器级别的 Arthas tunnel 地址（保存在服务器自定义标签中）
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("v1/arthas")
@Tag(name = "Arthas 管理", description = "Arthas / Tunnel 统一管理接口")
public class ArthasController {

    private final ArthasService arthasService;

    @Operation(summary = "查询节点 Arthas 配置", description = "从节点客户端映射获取 Arthas 配置")
    @GetMapping("/client-config")
    public ReturnResult<Map<String, Object>> getClientConfig(@RequestParam String nodeId) {
        try {
            return ReturnResult.ok(arthasService.getNodeArthasConfig(nodeId));
        } catch (Exception e) {
            log.error("获取节点 Arthas 配置失败: nodeId={}", nodeId, e);
            return ReturnResult.error("获取节点 Arthas 配置失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取服务器 Tunnel 地址", description = "从服务器设置自定义标签读取 arthasTunnelAddress")
    @GetMapping("/tunnel-address")
    public ReturnResult<String> getTunnelAddress(@RequestParam Integer serverId) {
        try {
            return ReturnResult.ok(arthasService.getServerTunnelAddress(serverId));
        } catch (Exception e) {
            log.error("获取服务器 Tunnel 地址失败: serverId={}", serverId, e);
            return ReturnResult.error("获取服务器 Tunnel 地址失败: " + e.getMessage());
        }
    }

    @Operation(summary = "设置服务器 Tunnel 地址", description = "写入服务器设置自定义标签 arthasTunnelAddress")
    @PostMapping("/tunnel-address/set")
    public ReturnResult<Boolean> setTunnelAddress(@RequestParam Integer serverId, @RequestParam String tunnelAddress) {
        try {
            return ReturnResult.ok(arthasService.setServerTunnelAddress(serverId, tunnelAddress));
        } catch (Exception e) {
            log.error("设置服务器 Tunnel 地址失败: serverId={}, tunnelAddress={}", serverId, tunnelAddress, e);
            return ReturnResult.error("设置服务器 Tunnel 地址失败: " + e.getMessage());
        }
    }
}


