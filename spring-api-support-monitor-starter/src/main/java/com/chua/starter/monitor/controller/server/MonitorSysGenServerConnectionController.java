package com.chua.starter.monitor.controller.server;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenServerConnectionStatus;
import com.chua.starter.monitor.pojo.MonitorSysGenServerConnectionCheckResult;
import com.chua.starter.monitor.pojo.MonitorSysGenServerConnectionHealthReport;
import com.chua.starter.monitor.pojo.MonitorSysGenServerConnectionStatistics;
import com.chua.starter.monitor.service.server.MonitorSysGenServerConnectionStatusService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 服务器连接状态管理控制器
 *
 * @author CH
 * @since 2024/12/20
 */
@RestController
@RequestMapping("/v1/server/connection")
@Api(tags = "服务器连接状态管理")
@Tag(name = "服务器连接状态管理")
@Slf4j
@RequiredArgsConstructor
public class MonitorSysGenServerConnectionController {

    private final MonitorSysGenServerConnectionStatusService connectionStatusService;

    /**
     * 测试单个服务器连接
     *
     * @param id 服务器ID
     * @return 连接测试结果
     */
    @PostMapping("/test/{id}")
    @Operation(summary = "测试单个服务器连接")
    @ApiOperation("测试单个服务器连接")
    public ReturnResult<MonitorSysGenServerConnectionStatus> testServerConnection(
            @PathVariable("id") @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer id) {
        
        log.info("开始测试服务器连接: serverId={}", id);
        return connectionStatusService.testServerConnection(id);
    }

    /**
     * 批量测试服务器连接
     *
     * @param serverIds 服务器ID列表
     * @return 批量连接测试结果
     */
    @PostMapping("/batch-test")
    @Operation(summary = "批量测试服务器连接")
    @ApiOperation("批量测试服务器连接")
    public ReturnResult<List<MonitorSysGenServerConnectionStatus>> batchTestServerConnection(
            @RequestBody @Parameter(description = "服务器ID列表") @ApiParam("服务器ID列表") List<Integer> serverIds) {
        
        log.info("开始批量测试服务器连接: serverIds={}", serverIds);
        return connectionStatusService.batchTestServerConnection(serverIds);
    }

    /**
     * 获取所有服务器连接状态
     *
     * @return 所有服务器连接状态
     */
    @GetMapping("/status/all")
    @Operation(summary = "获取所有服务器连接状态")
    @ApiOperation("获取所有服务器连接状态")
    public ReturnResult<List<MonitorSysGenServerConnectionStatus>> getAllServerConnectionStatus() {
        return connectionStatusService.getAllServerConnectionStatus();
    }

    /**
     * 根据服务器ID获取连接状态
     *
     * @param id 服务器ID
     * @return 服务器连接状态
     */
    @GetMapping("/status/{id}")
    @Operation(summary = "根据服务器ID获取连接状态")
    @ApiOperation("根据服务器ID获取连接状态")
    public ReturnResult<MonitorSysGenServerConnectionStatus> getServerConnectionStatus(
            @PathVariable("id") @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer id) {
        
        return connectionStatusService.getServerConnectionStatus(id);
    }

    /**
     * 根据连接状态获取服务器列表
     *
     * @param status 连接状态 (0:离线 1:在线 2:连接中 3:连接失败)
     * @return 服务器连接状态列表
     */
    @GetMapping("/status/by-status/{status}")
    @Operation(summary = "根据连接状态获取服务器列表")
    @ApiOperation("根据连接状态获取服务器列表")
    public ReturnResult<List<MonitorSysGenServerConnectionStatus>> getServersByConnectionStatus(
            @PathVariable("status") @Parameter(description = "连接状态") @ApiParam("连接状态") Integer status) {
        
        return connectionStatusService.getServersByConnectionStatus(status);
    }

    /**
     * 更新服务器连接状态
     *
     * @param id               服务器ID
     * @param connectionStatus 连接状态
     * @param connectionError  连接错误信息
     * @return 更新结果
     */
    @PutMapping("/status/{id}")
    @Operation(summary = "更新服务器连接状态")
    @ApiOperation("更新服务器连接状态")
    public ReturnResult<Boolean> updateServerConnectionStatus(
            @PathVariable("id") @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer id,
            @RequestParam("status") @Parameter(description = "连接状态") @ApiParam("连接状态") Integer connectionStatus,
            @RequestParam(value = "error", required = false) @Parameter(description = "连接错误信息") @ApiParam("连接错误信息") String connectionError) {
        
        log.info("更新服务器连接状态: serverId={}, status={}, error={}", id, connectionStatus, connectionError);
        return connectionStatusService.updateServerConnectionStatus(id, connectionStatus, connectionError);
    }

    /**
     * 批量更新服务器连接状态
     *
     * @param request 批量更新请求
     * @return 更新结果
     */
    @PutMapping("/status/batch")
    @Operation(summary = "批量更新服务器连接状态")
    @ApiOperation("批量更新服务器连接状态")
    public ReturnResult<Boolean> batchUpdateServerConnectionStatus(
            @RequestBody @Parameter(description = "批量更新请求") @ApiParam("批量更新请求") BatchUpdateRequest request) {
        
        log.info("批量更新服务器连接状态: serverIds={}, status={}", request.getServerIds(), request.getConnectionStatus());
        return connectionStatusService.batchUpdateServerConnectionStatus(request.getServerIds(), request.getConnectionStatus());
    }

    /**
     * 获取连接状态统计
     *
     * @return 连接状态统计
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取连接状态统计")
    @ApiOperation("获取连接状态统计")
    public ReturnResult<MonitorSysGenServerConnectionStatistics> getConnectionStatusStatistics() {
        return connectionStatusService.getConnectionStatusStatistics();
    }

    /**
     * 获取长时间未连接的服务器
     *
     * @param minutes 分钟数，默认60分钟
     * @return 长时间未连接的服务器列表
     */
    @GetMapping("/long-time-no-connect")
    @Operation(summary = "获取长时间未连接的服务器")
    @ApiOperation("获取长时间未连接的服务器")
    public ReturnResult<List<MonitorSysGenServerConnectionStatus>> getLongTimeNoConnectServers(
            @RequestParam(value = "minutes", defaultValue = "60") @Parameter(description = "分钟数") @ApiParam("分钟数") Integer minutes) {
        
        return connectionStatusService.getLongTimeNoConnectServers(minutes);
    }

    /**
     * 检查所有服务器连接状态
     *
     * @return 检查结果
     */
    @PostMapping("/check-all")
    @Operation(summary = "检查所有服务器连接状态")
    @ApiOperation("检查所有服务器连接状态")
    public ReturnResult<MonitorSysGenServerConnectionCheckResult> checkAllServerConnections() {
        log.info("开始检查所有服务器连接状态");
        return connectionStatusService.checkAllServerConnections();
    }

    /**
     * 获取服务器连接健康度报告
     *
     * @return 健康度报告
     */
    @GetMapping("/health-report")
    @Operation(summary = "获取服务器连接健康度报告")
    @ApiOperation("获取服务器连接健康度报告")
    public ReturnResult<MonitorSysGenServerConnectionHealthReport> getServerConnectionHealthReport() {
        return connectionStatusService.getServerConnectionHealthReport();
    }

    /**
     * 批量更新请求对象
     */
    public static class BatchUpdateRequest {
        private List<Integer> serverIds;
        private Integer connectionStatus;

        public List<Integer> getServerIds() {
            return serverIds;
        }

        public void setServerIds(List<Integer> serverIds) {
            this.serverIds = serverIds;
        }

        public Integer getConnectionStatus() {
            return connectionStatus;
        }

        public void setConnectionStatus(Integer connectionStatus) {
            this.connectionStatus = connectionStatus;
        }
    }
}
