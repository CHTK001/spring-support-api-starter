package com.chua.starter.monitor.arthas;

import com.chua.common.support.lang.code.ReturnResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Arthas 管理接口
 *
 * 提供： - 查询节点端 Arthas 配置（是否启用、tunnel 地址） - 查询/设置服务器级别的 Arthas tunnel
 * 地址（保存在服务器自定义标签中）
 * 
 * @author CH
 * @since 2023-04-05
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("v1/arthas")
@Tag(name = "Arthas 管理", description = "Arthas / Tunnel 统一管理接口")
public class ArthasController {

    private final ArthasService arthasService;

    /**
     * 查询节点 Arthas 配置
     * 
     * @param nodeId 节点ID，用于标识特定的节点客户端
     *               示例: "node-001"
     * @return 节点的 Arthas 配置信息，包括是否启用、tunnel 地址等
     */
    @Operation(summary = "查询节点 Arthas 配置", description = "从节点客户端映射获取 Arthas 配置")
    @GetMapping("/client-config")
    public ReturnResult<Map<String, Object>> getClientConfig(
            @Parameter(description = "节点ID，用于标识特定的节点客户端。示例: node-001") 
            @RequestParam String nodeId) {
        try {
            return ReturnResult.ok(arthasService.getNodeArthasConfig(nodeId));
        } catch (Exception e) {
            log.error("获取节点 Arthas 配置失败: nodeId={}", nodeId, e);
            return ReturnResult.error("获取节点 Arthas 配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取服务器 Arthas Tunnel 配置
     * 
     * @param serverId 服务器ID，用于标识特定的服务器
     *                 示例: "server-001"
     * @return 服务器的 Arthas Tunnel 配置信息，包括地址与认证信息
     */
    @Operation(summary = "获取服务器 Arthas Tunnel 配置", description = "包含地址与认证信息")
    @GetMapping("/tunnel-config")
    public ReturnResult<ArthasTunnelConfig> getTunnelConfig(
            @Parameter(description = "服务器ID，用于标识特定的服务器。示例: server-001") 
            @RequestParam String serverId) {
        try {
            return ReturnResult.ok(arthasService.getServerTunnelConfig(serverId));
        } catch (Exception e) {
            log.error("获取服务器 Tunnel 配置失败: serverId={}", serverId, e);
            return ReturnResult.error("获取服务器 Tunnel 配置失败: " + e.getMessage());
        }
    }

    /**
     * 设置服务器 Arthas Tunnel 配置
     *
     * @param serverId 服务器ID，用于标识特定的服务器
     *                 示例: "server-001"
     * @param address  Tunnel 服务器地址，例如：ws://arthas-tunnel:7777/ws
     *                 示例: "ws://arthas-tunnel:7777/ws"
     * @param http     HTTP 请求地址（可选），用于 HTTP 方式连接 Arthas
     *                 示例: "http://arthas-tunnel:8080"
     * @param username 认证用户名（可选）
     *                 示例: "admin"
     * @param password 认证密码（可选）
     *                 示例: "password123"
     * @return 设置结果，true表示设置成功，false表示设置失败
     * @author CH
     * @since 2023-04-05
     */
    @Operation(summary = "设置服务器 Arthas Tunnel 配置", description = "包含地址与认证信息")
    @PostMapping("/tunnel-config/set")
    public ReturnResult<Boolean> setTunnelConfig(
            @Parameter(description = "服务器ID，用于标识特定的服务器。示例: server-001") 
            @RequestParam String serverId,
            @Parameter(description = "Tunnel 服务器地址，例如：ws://arthas-tunnel:7777/ws。示例: ws://arthas-tunnel:7777/ws") 
            @RequestParam(required = false) String address,
            @Parameter(description = "HTTP 请求地址（可选），用于 HTTP 方式连接 Arthas。示例: http://arthas-tunnel:8080") 
            @RequestParam(required = false) String http,
            @Parameter(description = "认证用户名（可选）。示例: admin") 
            @RequestParam(required = false) String username,
            @Parameter(description = "认证密码（可选）。示例: password123") 
            @RequestParam(required = false) String password) {
        try {
            ArthasTunnelConfig cfg = ArthasTunnelConfig.builder()
                    .arthasTunnelConfigAddress(address)
                    .arthasTunnelConfigHttp(http)
                    .arthasTunnelConfigUsername(username)
                    .arthasTunnelConfigPassword(password)
                    .build();
            return ReturnResult.ok(arthasService.setServerTunnelConfig(serverId, cfg));
        } catch (Exception e) {
            log.error("设置服务器 Tunnel 配置失败: serverId={}", serverId, e);
            return ReturnResult.error("设置服务器 Tunnel 配置失败: " + e.getMessage());
        }
    }
}
