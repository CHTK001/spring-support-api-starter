package com.chua.starter.monitor.starter.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerLogConfig;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerLogConfigService;
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
import java.util.Map;

/**
 * 服务器日志配置管理控制器
 *
 * @author CH
 * @since 2024/12/20
 */
@RestController
@RequestMapping("/v1/server/log-config")
@Api(tags = "服务器日志配置管理")
@Tag(name = "服务器日志配置管理")
@Slf4j
@RequiredArgsConstructor
public class MonitorSysGenServerLogConfigController {

    private final MonitorSysGenServerLogConfigService configService;

    /**
     * 根据服务器ID获取日志配置
     *
     * @param serverId 服务器ID
     * @return 日志配置
     */
    @GetMapping("/server/{serverId}")
    @Operation(summary = "根据服务器ID获取日志配置")
    @ApiOperation("根据服务器ID获取日志配置")
    public ReturnResult<MonitorSysGenServerLogConfig> getConfigByServerId(
            @PathVariable("serverId") @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId) {

        return configService.getConfigByServerId(serverId);
    }

    /**
     * 保存或更新日志配置
     *
     * @param config 日志配置
     * @return 保存结果
     */
    @PostMapping("/save")
    @Operation(summary = "保存或更新日志配置")
    @ApiOperation("保存或更新日志配置")
    public ReturnResult<MonitorSysGenServerLogConfig> saveOrUpdateConfig(
            @RequestBody @Parameter(description = "日志配置") @ApiParam("日志配置") MonitorSysGenServerLogConfig config) {

        return configService.saveOrUpdateConfig(config);
    }

    /**
     * 获取所有启用的日志配置
     *
     * @return 启用的日志配置列表
     */
    @GetMapping("/enabled")
    @Operation(summary = "获取所有启用的日志配置")
    @ApiOperation("获取所有启用的日志配置")
    public ReturnResult<List<MonitorSysGenServerLogConfig>> getEnabledConfigs() {
        return configService.getEnabledConfigs();
    }

    /**
     * 获取启用实时日志的配置
     *
     * @return 启用实时日志的配置列表
     */
    @GetMapping("/realtime-enabled")
    @Operation(summary = "获取启用实时日志的配置")
    @ApiOperation("获取启用实时日志的配置")
    public ReturnResult<List<MonitorSysGenServerLogConfig>> getRealTimeEnabledConfigs() {
        return configService.getRealTimeEnabledConfigs();
    }

    /**
     * 根据保留天数获取配置
     *
     * @param retentionDays 保留天数
     * @return 配置列表
     */
    @GetMapping("/retention/{retentionDays}")
    @Operation(summary = "根据保留天数获取配置")
    @ApiOperation("根据保留天数获取配置")
    public ReturnResult<List<MonitorSysGenServerLogConfig>> getConfigsByRetentionDays(
            @PathVariable("retentionDays") @Parameter(description = "保留天数") @ApiParam("保留天数") Integer retentionDays) {

        return configService.getConfigsByRetentionDays(retentionDays);
    }

    /**
     * 获取需要清理的配置
     *
     * @return 需要清理的配置列表
     */
    @GetMapping("/cleanup")
    @Operation(summary = "获取需要清理的配置")
    @ApiOperation("获取需要清理的配置")
    public ReturnResult<List<MonitorSysGenServerLogConfig>> getConfigsForCleanup() {
        return configService.getConfigsForCleanup();
    }

    /**
     * 更新配置状态
     *
     * @param serverId 服务器ID
     * @param status   状态
     * @return 更新结果
     */
    @PutMapping("/server/{serverId}/status")
    @Operation(summary = "更新配置状态")
    @ApiOperation("更新配置状态")
    public ReturnResult<Boolean> updateConfigStatus(
            @PathVariable("serverId") @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId,
            @RequestParam("status") @Parameter(description = "状态") @ApiParam("状态") Integer status) {

        return configService.updateConfigStatus(serverId, status);
    }

    /**
     * 更新日志启用状态
     *
     * @param serverId 服务器ID
     * @param enabled  是否启用
     * @return 更新结果
     */
    @PutMapping("/server/{serverId}/enabled")
    @Operation(summary = "更新日志启用状态")
    @ApiOperation("更新日志启用状态")
    public ReturnResult<Boolean> updateLogEnabled(
            @PathVariable("serverId") @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId,
            @RequestParam("enabled") @Parameter(description = "是否启用") @ApiParam("是否启用") Integer enabled) {

        return configService.updateLogEnabled(serverId, enabled);
    }

    /**
     * 更新实时日志启用状态
     *
     * @param serverId 服务器ID
     * @param enabled  是否启用
     * @return 更新结果
     */
    @PutMapping("/server/{serverId}/realtime-enabled")
    @Operation(summary = "更新实时日志启用状态")
    @ApiOperation("更新实时日志启用状态")
    public ReturnResult<Boolean> updateRealTimeEnabled(
            @PathVariable("serverId") @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId,
            @RequestParam("enabled") @Parameter(description = "是否启用") @ApiParam("是否启用") Integer enabled) {

        return configService.updateRealTimeEnabled(serverId, enabled);
    }

    /**
     * 批量更新配置状态
     *
     * @param request 批量更新请求
     * @return 更新结果
     */
    @PutMapping("/batch/status")
    @Operation(summary = "批量更新配置状态")
    @ApiOperation("批量更新配置状态")
    public ReturnResult<Boolean> batchUpdateConfigStatus(
            @RequestBody @Parameter(description = "批量更新请求") @ApiParam("批量更新请求") BatchUpdateRequest request) {

        return configService.batchUpdateConfigStatus(request.getServerIds(), request.getStatus());
    }

    /**
     * 获取配置统计信息
     *
     * @return 统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取配置统计信息")
    @ApiOperation("获取配置统计信息")
    public ReturnResult<Map<String, Object>> getConfigStatistics() {
        return configService.getConfigStatistics();
    }

    /**
     * 根据收集间隔获取配置
     *
     * @param interval 收集间隔
     * @return 配置列表
     */
    @GetMapping("/interval/{interval}")
    @Operation(summary = "根据收集间隔获取配置")
    @ApiOperation("根据收集间隔获取配置")
    public ReturnResult<List<MonitorSysGenServerLogConfig>> getConfigsByCollectionInterval(
            @PathVariable("interval") @Parameter(description = "收集间隔") @ApiParam("收集间隔") Integer interval) {

        return configService.getConfigsByCollectionInterval(interval);
    }

    /**
     * 获取过期配置（长时间未更新）
     *
     * @param days 天数
     * @return 过期配置列表
     */
    @GetMapping("/expired/{days}")
    @Operation(summary = "获取过期配置")
    @ApiOperation("获取过期配置")
    public ReturnResult<List<MonitorSysGenServerLogConfig>> getExpiredConfigs(
            @PathVariable("days") @Parameter(description = "天数") @ApiParam("天数") Integer days) {

        return configService.getExpiredConfigs(days);
    }

    /**
     * 检查服务器是否有日志配置
     *
     * @param serverId 服务器ID
     * @return 是否存在配置
     */
    @GetMapping("/server/{serverId}/exists")
    @Operation(summary = "检查服务器是否有日志配置")
    @ApiOperation("检查服务器是否有日志配置")
    public ReturnResult<Boolean> hasConfigByServerId(
            @PathVariable("serverId") @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId) {

        return configService.hasConfigByServerId(serverId);
    }

    /**
     * 获取默认配置模板
     *
     * @return 默认配置
     */
    @GetMapping("/template/default")
    @Operation(summary = "获取默认配置模板")
    @ApiOperation("获取默认配置模板")
    public ReturnResult<MonitorSysGenServerLogConfig> getDefaultTemplate() {
        return configService.getDefaultTemplate();
    }

    /**
     * 为服务器创建默认配置
     *
     * @param serverId 服务器ID
     * @return 创建结果
     */
    @PostMapping("/server/{serverId}/default")
    @Operation(summary = "为服务器创建默认配置")
    @ApiOperation("为服务器创建默认配置")
    public ReturnResult<MonitorSysGenServerLogConfig> createDefaultConfig(
            @PathVariable("serverId") @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId) {

        return configService.createDefaultConfig(serverId);
    }

    /**
     * 复制配置到其他服务器
     *
     * @param sourceServerId  源服务器ID
     * @param targetServerIds 目标服务器ID列表
     * @return 复制结果
     */
    @PostMapping("/copy/{sourceServerId}")
    @Operation(summary = "复制配置到其他服务器")
    @ApiOperation("复制配置到其他服务器")
    public ReturnResult<Boolean> copyConfigToServers(
            @PathVariable("sourceServerId") @Parameter(description = "源服务器ID") @ApiParam("源服务器ID") Integer sourceServerId,
            @RequestBody @Parameter(description = "目标服务器ID列表") @ApiParam("目标服务器ID列表") List<Integer> targetServerIds) {

        return configService.copyConfigToServers(sourceServerId, targetServerIds);
    }

    /**
     * 验证配置有效性
     *
     * @param config 配置
     * @return 验证结果
     */
    @PostMapping("/validate")
    @Operation(summary = "验证配置有效性")
    @ApiOperation("验证配置有效性")
    public ReturnResult<Map<String, Object>> validateConfig(
            @RequestBody @Parameter(description = "配置") @ApiParam("配置") MonitorSysGenServerLogConfig config) {

        return configService.validateConfig(config);
    }

    /**
     * 重置配置为默认值
     *
     * @param serverId 服务器ID
     * @return 重置结果
     */
    @PostMapping("/server/{serverId}/reset")
    @Operation(summary = "重置配置为默认值")
    @ApiOperation("重置配置为默认值")
    public ReturnResult<MonitorSysGenServerLogConfig> resetConfigToDefault(
            @PathVariable("serverId") @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId) {

        return configService.resetConfigToDefault(serverId);
    }

    /**
     * 批量更新请求对象
     */
    public static class BatchUpdateRequest {
        private List<Integer> serverIds;
        private Integer status;

        public List<Integer> getServerIds() {
            return serverIds;
        }

        public void setServerIds(List<Integer> serverIds) {
            this.serverIds = serverIds;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }
    }
}
