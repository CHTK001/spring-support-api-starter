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
 * 提供： - 查询节点端 Arthas 配置（是否启用、tunnel 地址） - 查询/设置服务器级别的 Arthas tunnel
 * 地址（保存在服务器自定义标签中）
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

    @Operation(summary = "获取服务器 Arthas Tunnel 配置", description = "包含地址与认证信息")
    @GetMapping("/tunnel-config")
    public ReturnResult<ArthasTunnelConfig> getTunnelConfig(@RequestParam Integer serverId) {
        try {
            return ReturnResult.ok(arthasService.getServerTunnelConfig(serverId));
        } catch (Exception e) {
            log.error("获取服务器 Tunnel 配置失败: serverId={}", serverId, e);
            return ReturnResult.error("获取服务器 Tunnel 配置失败: " + e.getMessage());
        }
    }

    @Operation(summary = "设置服务器 Arthas Tunnel 配置", description = "包含地址与认证信息")
    @PostMapping("/tunnel-config/set")
    public ReturnResult<Boolean> setTunnelConfig(@RequestParam Integer serverId,
            @RequestParam(required = false) String address, @RequestParam(required = false) String username,
            @RequestParam(required = false) String password) {
        try {
            ArthasTunnelConfig cfg = new ArthasTunnelConfig();
            cfg.setAddress(address);
            cfg.setUsername(username);
            cfg.setPassword(password);
            return ReturnResult.ok(arthasService.setServerTunnelConfig(serverId, cfg));
        } catch (Exception e) {
            log.error("设置服务器 Tunnel 配置失败: serverId={}", serverId, e);
            return ReturnResult.error("设置服务器 Tunnel 配置失败: " + e.getMessage());
        }
    }
}
