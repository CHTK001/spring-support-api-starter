package com.chua.starter.monitor.starter.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.ServerConnectionMessage;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerService;
import com.chua.starter.monitor.starter.service.ServerConnectionWebSocketService;
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
 * 服务器连接状态管理控制器
 *
 * @author CH
 * @since 2024/12/20
 */
@RestController
@RequestMapping("v1/server/connection")
@Tag(name = "服务器连接状态管理")
@Api(tags = "服务器连接状态管理")
@RequiredArgsConstructor
@Slf4j
public class ServerConnectionController {

    private final MonitorSysGenServerService monitorSysGenServerService;
    private final ServerConnectionWebSocketService serverConnectionWebSocketService;

    /**
     * 测试服务器连接
     *
     * @param id 服务器ID
     * @return 测试结果
     */
    @Operation(summary = "测试服务器连接")
    @PostMapping("test/{id}")
    public ReturnResult<Boolean> testConnection(@Parameter(name = "服务器ID") @PathVariable String id) {
        log.info("开始测试服务器连接: {}", id);
        ReturnResult<Boolean> result = monitorSysGenServerService.testConnection(id);
        
        // 发送WebSocket消息
        if (result.getData() != null) {
            try {
                ServerConnectionMessage message = ServerConnectionMessage.builder()
                        .messageType(ServerConnectionMessage.MessageType.CONNECTION_TEST_RESULT)
                        .serverId(Integer.valueOf(id))
                        .connectionStatus(result.getData() ? 1 : 0)
                        .statusDesc(result.getData() ? "连接成功" : "连接失败")
                        .errorMessage(result.getData() ? null : result.getMsg())
                        .timestamp(System.currentTimeMillis())
                        .build();
                
                serverConnectionWebSocketService.sendConnectionTestResult(message);
            } catch (Exception e) {
                log.error("发送连接测试结果WebSocket消息失败", e);
            }
        }
        
        return result;
    }

    /**
     * 批量测试服务器连接
     *
     * @param ids 服务器ID列表
     * @return 测试结果
     */
    @Operation(summary = "批量测试服务器连接")
    @PostMapping("batch-test")
    public ReturnResult<Map<String, Object>> batchTestConnection(@RequestBody List<String> ids) {
        log.info("开始批量测试服务器连接: {}", ids);
        return monitorSysGenServerService.batchTestConnection(ids);
    }

    /**
     * 更新服务器连接状态
     *
     * @param id 服务器ID
     * @param status 连接状态
     * @param errorMessage 错误信息
     * @return 更新结果
     */
    @Operation(summary = "更新服务器连接状态")
    @PutMapping("status/{id}")
    public ReturnResult<Boolean> updateConnectionStatus(
            @Parameter(name = "服务器ID") @PathVariable String id,
            @Parameter(name = "连接状态") @RequestParam Integer status,
            @Parameter(name = "错误信息") @RequestParam(required = false) String errorMessage) {
        log.info("更新服务器连接状态: id={}, status={}, error={}", id, status, errorMessage);
        return monitorSysGenServerService.updateConnectionStatus(id, status, errorMessage);
    }

    /**
     * 获取所有服务器连接状态
     *
     * @return 连接状态列表
     */
    @Operation(summary = "获取所有服务器连接状态")
    @GetMapping("status/all")
    public ReturnResult<List<Map<String, Object>>> getAllConnectionStatus() {
        return monitorSysGenServerService.getAllConnectionStatus();
    }

    /**
     * 获取单个服务器连接状态
     *
     * @param id 服务器ID
     * @return 连接状态
     */
    @Operation(summary = "获取单个服务器连接状态")
    @GetMapping("status/{id}")
    public ReturnResult<Map<String, Object>> getConnectionStatus(@Parameter(name = "服务器ID") @PathVariable String id) {
        return monitorSysGenServerService.getConnectionStatus(id);
    }

    /**
     * 手动触发连接状态检查
     *
     * @return 操作结果
     */
    @Operation(summary = "手动触发连接状态检查")
    @PostMapping("check")
    public ReturnResult<String> triggerConnectionCheck() {
        try {
            log.info("手动触发服务器连接状态检查");
            monitorSysGenServerService.scheduleConnectionCheck();
            return ReturnResult.ok("连接状态检查已触发");
        } catch (Exception e) {
            log.error("触发连接状态检查失败", e);
            return ReturnResult.error("触发检查失败: " + e.getMessage());
        }
    }

    /**
     * 广播所有服务器连接状态
     *
     * @return 操作结果
     */
    @Operation(summary = "广播所有服务器连接状态")
    @PostMapping("broadcast")
    public ReturnResult<String> broadcastAllServerStatus() {
        try {
            log.info("广播所有服务器连接状态");
            serverConnectionWebSocketService.broadcastAllServerStatus();
            return ReturnResult.ok("状态广播完成");
        } catch (Exception e) {
            log.error("广播服务器状态失败", e);
            return ReturnResult.error("广播失败: " + e.getMessage());
        }
    }
}
