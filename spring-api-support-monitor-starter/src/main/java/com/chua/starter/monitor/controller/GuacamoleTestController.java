package com.chua.starter.monitor.controller;

import com.chua.starter.monitor.starter.guacamole.GuacamoleConnectionManager;
import com.chua.starter.monitor.starter.guacamole.GuacamoleTunnel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Guacamole测试控制器
 *
 * @author CH
 * @since 2024/12/18
 */
@RestController
@RequestMapping("/api/guacamole")
@Api(tags = "Guacamole测试接口")
@Tag(name = "Guacamole测试接口", description = "用于测试Guacamole连接功能")
@RequiredArgsConstructor
@Slf4j
public class GuacamoleTestController {

    private final GuacamoleConnectionManager guacamoleConnectionManager;

    /**
     * 获取连接状态
     */
    @GetMapping("/status")
    @Operation(summary = "获取连接状态")
    @ApiOperation("获取连接状态")
    public Map<String, Object> getConnectionStatus() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<Integer, GuacamoleTunnel> connections = guacamoleConnectionManager.getAllConnections();
            
            result.put("success", true);
            result.put("connectionCount", guacamoleConnectionManager.getConnectionCount());
            result.put("stats", guacamoleConnectionManager.getConnectionStats());
            
            Map<Integer, Map<String, Object>> connectionDetails = new HashMap<>();
            for (Map.Entry<Integer, GuacamoleTunnel> entry : connections.entrySet()) {
                Integer serverId = entry.getKey();
                GuacamoleTunnel tunnel = entry.getValue();
                
                Map<String, Object> detail = new HashMap<>();
                detail.put("serverId", serverId);
                detail.put("connected", tunnel.isConnected());
                detail.put("info", tunnel.getConnectionInfo());
                
                connectionDetails.put(serverId, detail);
            }
            result.put("connections", connectionDetails);
            
        } catch (Exception e) {
            log.error("获取连接状态失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 检查服务器连接
     */
    @GetMapping("/connection/{serverId}")
    @Operation(summary = "检查服务器连接")
    @ApiOperation("检查服务器连接")
    public Map<String, Object> checkConnection(@PathVariable Integer serverId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean hasConnection = guacamoleConnectionManager.hasConnection(serverId);
            GuacamoleTunnel tunnel = guacamoleConnectionManager.getConnection(serverId);
            
            result.put("success", true);
            result.put("serverId", serverId);
            result.put("hasConnection", hasConnection);
            
            if (tunnel != null) {
                result.put("connected", tunnel.isConnected());
                result.put("info", tunnel.getConnectionInfo());
            } else {
                result.put("connected", false);
                result.put("info", "连接不存在");
            }
            
        } catch (Exception e) {
            log.error("检查服务器连接失败: serverId={}, error={}", serverId, e.getMessage(), e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 关闭服务器连接
     */
    @DeleteMapping("/connection/{serverId}")
    @Operation(summary = "关闭服务器连接")
    @ApiOperation("关闭服务器连接")
    public Map<String, Object> closeConnection(@PathVariable Integer serverId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            guacamoleConnectionManager.closeConnection(serverId);
            
            result.put("success", true);
            result.put("serverId", serverId);
            result.put("message", "连接已关闭");
            
        } catch (Exception e) {
            log.error("关闭服务器连接失败: serverId={}, error={}", serverId, e.getMessage(), e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 关闭所有连接
     */
    @DeleteMapping("/connections")
    @Operation(summary = "关闭所有连接")
    @ApiOperation("关闭所有连接")
    public Map<String, Object> closeAllConnections() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            int connectionCount = guacamoleConnectionManager.getConnectionCount();
            guacamoleConnectionManager.closeAllConnections();
            
            result.put("success", true);
            result.put("message", "所有连接已关闭");
            result.put("closedCount", connectionCount);
            
        } catch (Exception e) {
            log.error("关闭所有连接失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 发送测试数据
     */
    @PostMapping("/test-data/{serverId}")
    @Operation(summary = "发送测试数据")
    @ApiOperation("发送测试数据")
    public Map<String, Object> sendTestData(@PathVariable Integer serverId, 
                                           @RequestBody Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String testData = data.get("data") != null ? data.get("data").toString() : "test";
            boolean sent = guacamoleConnectionManager.sendData(serverId, testData);
            
            result.put("success", sent);
            result.put("serverId", serverId);
            result.put("data", testData);
            result.put("message", sent ? "数据发送成功" : "数据发送失败");
            
        } catch (Exception e) {
            log.error("发送测试数据失败: serverId={}, error={}", serverId, e.getMessage(), e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取WebSocket端点信息
     */
    @GetMapping("/endpoints")
    @Operation(summary = "获取WebSocket端点信息")
    @ApiOperation("获取WebSocket端点信息")
    public Map<String, Object> getWebSocketEndpoints() {
        Map<String, Object> result = new HashMap<>();
        
        result.put("success", true);
        result.put("endpoints", Map.of(
            "rdp", "/websocket/rdp",
            "vnc", "/websocket/vnc"
        ));
        result.put("description", "WebSocket连接端点");
        result.put("usage", Map.of(
            "connect", "发送 {\"action\": \"connect\", \"serverId\": 1} 建立连接",
            "disconnect", "发送 {\"action\": \"disconnect\"} 断开连接",
            "mouse", "发送 {\"action\": \"mouse\", \"type\": \"move\", \"x\": 100, \"y\": 100} 鼠标事件",
            "keyboard", "发送 {\"action\": \"keyboard\", \"type\": \"down\", \"keyCode\": 13} 键盘事件"
        ));
        
        return result;
    }
}
