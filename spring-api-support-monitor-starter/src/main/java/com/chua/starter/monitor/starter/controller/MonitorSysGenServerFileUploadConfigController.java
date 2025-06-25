package com.chua.starter.monitor.starter.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.pojo.MonitorSysGenServerFileUploadConfig;
import com.chua.starter.monitor.starter.pojo.MonitorSysGenServerFileUploadBatchResult;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerFileUploadConfigService;
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
 * 服务器文件上传配置管理控制器
 *
 * @author CH
 * @since 2024/12/20
 */
@Slf4j
@RestController
@RequestMapping("/api/monitor/server/file-upload/config")
@RequiredArgsConstructor
@Api(tags = "服务器文件上传配置管理")
@Tag(name = "服务器文件上传配置管理", description = "服务器文件上传配置管理相关接口")
public class MonitorSysGenServerFileUploadConfigController {

    private final MonitorSysGenServerFileUploadConfigService configService;

    /**
     * 获取全局上传配置
     */
    @GetMapping("/global")
    @Operation(summary = "获取全局上传配置")
    @ApiOperation("获取全局上传配置")
    public ReturnResult<MonitorSysGenServerFileUploadConfig> getGlobalUploadConfig() {
        return configService.getGlobalUploadConfig();
    }

    /**
     * 更新全局上传配置
     */
    @PutMapping("/global")
    @Operation(summary = "更新全局上传配置")
    @ApiOperation("更新全局上传配置")
    public ReturnResult<Boolean> updateGlobalUploadConfig(
            @RequestBody @Parameter(description = "配置信息") @ApiParam("配置信息") MonitorSysGenServerFileUploadConfig config) {
        return configService.updateGlobalUploadConfig(config);
    }

    /**
     * 获取服务器上传配置
     */
    @GetMapping("/server/{serverId}")
    @Operation(summary = "获取服务器上传配置")
    @ApiOperation("获取服务器上传配置")
    public ReturnResult<MonitorSysGenServerFileUploadConfig> getServerUploadConfig(
            @PathVariable @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId) {
        return configService.getServerUploadConfig(serverId);
    }

    /**
     * 更新服务器上传配置
     */
    @PutMapping("/server/{serverId}")
    @Operation(summary = "更新服务器上传配置")
    @ApiOperation("更新服务器上传配置")
    public ReturnResult<Boolean> updateServerUploadConfig(
            @PathVariable @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId,
            @RequestBody @Parameter(description = "配置信息") @ApiParam("配置信息") MonitorSysGenServerFileUploadConfig config) {
        return configService.updateServerUploadConfig(serverId, config);
    }

    /**
     * 删除服务器上传配置
     */
    @DeleteMapping("/server/{serverId}")
    @Operation(summary = "删除服务器上传配置")
    @ApiOperation("删除服务器上传配置")
    public ReturnResult<Boolean> deleteServerUploadConfig(
            @PathVariable @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId) {
        return configService.deleteServerUploadConfig(serverId);
    }

    /**
     * 获取有效的上传配置
     */
    @GetMapping("/effective/{serverId}")
    @Operation(summary = "获取有效的上传配置")
    @ApiOperation("获取有效的上传配置")
    public ReturnResult<MonitorSysGenServerFileUploadConfig> getEffectiveUploadConfig(
            @PathVariable @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId) {
        return configService.getEffectiveUploadConfig(serverId);
    }

    /**
     * 验证上传配置
     */
    @PostMapping("/validate")
    @Operation(summary = "验证上传配置")
    @ApiOperation("验证上传配置")
    public ReturnResult<Map<String, Object>> validateUploadConfig(
            @RequestBody @Parameter(description = "配置信息") @ApiParam("配置信息") MonitorSysGenServerFileUploadConfig config) {
        return configService.validateUploadConfig(config);
    }

    /**
     * 获取默认上传配置
     */
    @GetMapping("/default")
    @Operation(summary = "获取默认上传配置")
    @ApiOperation("获取默认上传配置")
    public ReturnResult<MonitorSysGenServerFileUploadConfig> getDefaultUploadConfig() {
        return configService.getDefaultUploadConfig();
    }

    /**
     * 重置服务器配置为默认值
     */
    @PostMapping("/server/{serverId}/reset")
    @Operation(summary = "重置服务器配置为默认值")
    @ApiOperation("重置服务器配置为默认值")
    public ReturnResult<Boolean> resetServerConfigToDefault(
            @PathVariable @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId) {
        return configService.resetServerConfigToDefault(serverId);
    }

    /**
     * 批量更新服务器配置
     */
    @PutMapping("/batch")
    @Operation(summary = "批量更新服务器配置")
    @ApiOperation("批量更新服务器配置")
    public ReturnResult<MonitorSysGenServerFileUploadBatchResult> batchUpdateServerConfigs(
            @RequestBody @Parameter(description = "服务器配置列表") @ApiParam("服务器配置列表")
            List<MonitorSysGenServerFileUploadConfig> serverConfigs) {
        return configService.batchUpdateServerConfigs(serverConfigs);
    }

    /**
     * 导出配置
     */
    @GetMapping("/export")
    @Operation(summary = "导出配置")
    @ApiOperation("导出配置")
    public ReturnResult<String> exportConfig(
            @RequestParam(required = false) @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId) {
        return configService.exportConfig(serverId);
    }

    /**
     * 导入配置
     */
    @PostMapping("/import")
    @Operation(summary = "导入配置")
    @ApiOperation("导入配置")
    public ReturnResult<Boolean> importConfig(
            @RequestParam @Parameter(description = "配置数据") @ApiParam("配置数据") String configData,
            @RequestParam(required = false) @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId) {
        return configService.importConfig(configData, serverId);
    }

    /**
     * 获取配置历史
     */
    @GetMapping("/history")
    @Operation(summary = "获取配置历史")
    @ApiOperation("获取配置历史")
    public ReturnResult<List<Map<String, Object>>> getConfigHistory(
            @RequestParam(required = false) @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId,
            @RequestParam(defaultValue = "10") @Parameter(description = "返回数量限制") @ApiParam("返回数量限制") Integer limit) {
        return configService.getConfigHistory(serverId, limit);
    }

    /**
     * 回滚配置到指定版本
     */
    @PostMapping("/rollback")
    @Operation(summary = "回滚配置到指定版本")
    @ApiOperation("回滚配置到指定版本")
    public ReturnResult<Boolean> rollbackConfig(
            @RequestParam(required = false) @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId,
            @RequestParam @Parameter(description = "版本ID") @ApiParam("版本ID") Long versionId) {
        return configService.rollbackConfig(serverId, versionId);
    }

    /**
     * 获取配置模板
     */
    @GetMapping("/templates")
    @Operation(summary = "获取配置模板")
    @ApiOperation("获取配置模板")
    public ReturnResult<List<Map<String, Object>>> getConfigTemplates() {
        return configService.getConfigTemplates();
    }

    /**
     * 创建配置模板
     */
    @PostMapping("/templates")
    @Operation(summary = "创建配置模板")
    @ApiOperation("创建配置模板")
    public ReturnResult<Boolean> createConfigTemplate(
            @RequestParam @Parameter(description = "模板名称") @ApiParam("模板名称") String templateName,
            @RequestBody @Parameter(description = "配置信息") @ApiParam("配置信息") Map<String, Object> config) {
        return configService.createConfigTemplate(templateName, config);
    }

    /**
     * 应用配置模板
     */
    @PostMapping("/server/{serverId}/apply-template")
    @Operation(summary = "应用配置模板")
    @ApiOperation("应用配置模板")
    public ReturnResult<Boolean> applyConfigTemplate(
            @PathVariable @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId,
            @RequestParam @Parameter(description = "模板ID") @ApiParam("模板ID") Long templateId) {
        return configService.applyConfigTemplate(serverId, templateId);
    }

    /**
     * 删除配置模板
     */
    @DeleteMapping("/templates/{templateId}")
    @Operation(summary = "删除配置模板")
    @ApiOperation("删除配置模板")
    public ReturnResult<Boolean> deleteConfigTemplate(
            @PathVariable @Parameter(description = "模板ID") @ApiParam("模板ID") Long templateId) {
        return configService.deleteConfigTemplate(templateId);
    }

    /**
     * 检查配置兼容性
     */
    @PostMapping("/server/{serverId}/compatibility")
    @Operation(summary = "检查配置兼容性")
    @ApiOperation("检查配置兼容性")
    public ReturnResult<Map<String, Object>> checkConfigCompatibility(
            @PathVariable @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId,
            @RequestBody @Parameter(description = "配置信息") @ApiParam("配置信息") Map<String, Object> config) {
        return configService.checkConfigCompatibility(serverId, config);
    }

    /**
     * 获取配置建议
     */
    @GetMapping("/server/{serverId}/recommendations")
    @Operation(summary = "获取配置建议")
    @ApiOperation("获取配置建议")
    public ReturnResult<List<Map<String, Object>>> getConfigRecommendations(
            @PathVariable @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId) {
        return configService.getConfigRecommendations(serverId);
    }

    /**
     * 优化配置
     */
    @PostMapping("/server/{serverId}/optimize")
    @Operation(summary = "优化配置")
    @ApiOperation("优化配置")
    public ReturnResult<Map<String, Object>> optimizeConfig(
            @PathVariable @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId) {
        return configService.optimizeConfig(serverId);
    }

    /**
     * 测试配置
     */
    @PostMapping("/server/{serverId}/test")
    @Operation(summary = "测试配置")
    @ApiOperation("测试配置")
    public ReturnResult<Map<String, Object>> testConfig(
            @PathVariable @Parameter(description = "服务器ID") @ApiParam("服务器ID") Integer serverId,
            @RequestBody @Parameter(description = "配置信息") @ApiParam("配置信息") Map<String, Object> config) {
        return configService.testConfig(serverId, config);
    }

    /**
     * 获取配置使用统计
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取配置使用统计")
    @ApiOperation("获取配置使用统计")
    public ReturnResult<Map<String, Object>> getConfigUsageStatistics() {
        return configService.getConfigUsageStatistics();
    }

    /**
     * 清理无效配置
     */
    @PostMapping("/cleanup")
    @Operation(summary = "清理无效配置")
    @ApiOperation("清理无效配置")
    public ReturnResult<Map<String, Object>> cleanupInvalidConfigs() {
        return configService.cleanupInvalidConfigs();
    }
}
