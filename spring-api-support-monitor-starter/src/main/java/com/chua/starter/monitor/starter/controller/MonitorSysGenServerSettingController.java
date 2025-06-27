package com.chua.starter.monitor.starter.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.mybatis.utils.PageResultUtils;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 服务器配置设置控制器
 *
 * @author CH
 * @since 2024/12/26
 */
@RestController
@RequestMapping("v1/gen/server/setting")
@Tag(name = "服务器配置设置")
@RequiredArgsConstructor
public class MonitorSysGenServerSettingController {

    private final MonitorSysGenServerSettingService monitorSysGenServerSettingService;

    /**
     * 分页查询服务器配置设置
     *
     * @param page   分页参数
     * @param entity 查询条件
     * @return 分页结果
     */
    @ResponseBody
    @Operation(summary = "分页查询服务器配置设置")
    @GetMapping("page")
    public ReturnPageResult<MonitorSysGenServerSetting> page(Query<MonitorSysGenServerSetting> page, MonitorSysGenServerSetting entity) {
        return PageResultUtils.ok(monitorSysGenServerSettingService.pageFor(page.createPage(), entity));
    }

    /**
     * 根据服务器ID获取配置
     *
     * @param serverId 服务器ID
     * @return 服务器配置
     */
    @ResponseBody
    @Operation(summary = "根据服务器ID获取配置")
    @GetMapping("server/{serverId}")
    public ReturnResult<MonitorSysGenServerSetting> getByServerId(@PathVariable Integer serverId) {
        return monitorSysGenServerSettingService.getOrCreateSetting(serverId);
    }

    /**
     * 根据服务器ID列表批量获取配置
     *
     * @param serverIds 服务器ID列表
     * @return 服务器配置列表
     */
    @ResponseBody
    @Operation(summary = "批量获取服务器配置")
    @PostMapping("batch/get")
    public ReturnResult<List<MonitorSysGenServerSetting>> getByServerIds(@RequestBody List<Integer> serverIds) {
        return monitorSysGenServerSettingService.getByServerIds(serverIds);
    }

    /**
     * 保存或更新服务器配置
     *
     * @param setting 服务器配置
     * @param bindingResult 验证结果
     * @return 操作结果
     */
    @ResponseBody
    @Operation(summary = "保存或更新服务器配置")
    @PostMapping("save")
    public ReturnResult<Boolean> saveOrUpdate(@Validated @RequestBody MonitorSysGenServerSetting setting, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.error(bindingResult.getFieldError().getDefaultMessage());
        }
        return monitorSysGenServerSettingService.saveOrUpdateSetting(setting);
    }

    /**
     * 批量保存或更新服务器配置
     *
     * @param settings 服务器配置列表
     * @return 操作结果
     */
    @ResponseBody
    @Operation(summary = "批量保存或更新服务器配置")
    @PostMapping("batch/save")
    public ReturnResult<Boolean> batchSaveOrUpdate(@RequestBody List<MonitorSysGenServerSetting> settings) {
        return monitorSysGenServerSettingService.batchSaveOrUpdateSettings(settings);
    }

    /**
     * 根据服务器ID删除配置
     *
     * @param serverId 服务器ID
     * @return 操作结果
     */
    @ResponseBody
    @Operation(summary = "删除服务器配置")
    @DeleteMapping("server/{serverId}")
    public ReturnResult<Boolean> deleteByServerId(@PathVariable Integer serverId) {
        return monitorSysGenServerSettingService.deleteByServerId(serverId);
    }

    /**
     * 获取启用监控的服务器配置
     *
     * @return 启用监控的服务器配置列表
     */
    @ResponseBody
    @Operation(summary = "获取启用监控的服务器配置")
    @GetMapping("monitor/enabled")
    public ReturnResult<List<MonitorSysGenServerSetting>> getMonitorEnabledSettings() {
        return monitorSysGenServerSettingService.getMonitorEnabledSettings();
    }

    /**
     * 获取启用告警的服务器配置
     *
     * @return 启用告警的服务器配置列表
     */
    @ResponseBody
    @Operation(summary = "获取启用告警的服务器配置")
    @GetMapping("alert/enabled")
    public ReturnResult<List<MonitorSysGenServerSetting>> getAlertEnabledSettings() {
        return monitorSysGenServerSettingService.getAlertEnabledSettings();
    }

    /**
     * 获取启用上报的服务器配置
     *
     * @param reportMethod 上报方式
     * @return 启用上报的服务器配置列表
     */
    @ResponseBody
    @Operation(summary = "获取启用上报的服务器配置")
    @GetMapping("report/enabled")
    public ReturnResult<List<MonitorSysGenServerSetting>> getReportEnabledSettings(@RequestParam(required = false) String reportMethod) {
        return monitorSysGenServerSettingService.getReportEnabledSettings(reportMethod);
    }

    /**
     * 更新服务器配置状态
     *
     * @param serverId 服务器ID
     * @param status 状态
     * @return 操作结果
     */
    @ResponseBody
    @Operation(summary = "更新服务器配置状态")
    @PutMapping("server/{serverId}/status/{status}")
    public ReturnResult<Boolean> updateStatus(@PathVariable Integer serverId, @PathVariable Integer status) {
        return monitorSysGenServerSettingService.updateStatusByServerId(serverId, status);
    }

    /**
     * 获取配置统计信息
     *
     * @return 统计信息
     */
    @ResponseBody
    @Operation(summary = "获取配置统计信息")
    @GetMapping("statistics")
    public ReturnResult<Map<String, Object>> getStatistics() {
        return monitorSysGenServerSettingService.getSettingsStatistics();
    }

    /**
     * 创建默认配置
     *
     * @param serverId 服务器ID
     * @return 默认配置
     */
    @ResponseBody
    @Operation(summary = "创建默认配置")
    @PostMapping("server/{serverId}/default")
    public ReturnResult<MonitorSysGenServerSetting> createDefault(@PathVariable Integer serverId) {
        return monitorSysGenServerSettingService.createDefaultSetting(serverId);
    }

    /**
     * 复制配置到其他服务器
     *
     * @param sourceServerId 源服务器ID
     * @param targetServerIds 目标服务器ID列表
     * @return 操作结果
     */
    @ResponseBody
    @Operation(summary = "复制配置到其他服务器")
    @PostMapping("copy/{sourceServerId}")
    public ReturnResult<Boolean> copySetting(@PathVariable Integer sourceServerId, @RequestBody List<Integer> targetServerIds) {
        return monitorSysGenServerSettingService.copySettingToServers(sourceServerId, targetServerIds);
    }

    /**
     * 验证配置有效性
     *
     * @param setting 服务器配置
     * @return 验证结果
     */
    @ResponseBody
    @Operation(summary = "验证配置有效性")
    @PostMapping("validate")
    public ReturnResult<Boolean> validate(@RequestBody MonitorSysGenServerSetting setting) {
        return monitorSysGenServerSettingService.validateSetting(setting);
    }

    /**
     * 获取配置模板
     *
     * @param templateType 模板类型
     * @return 配置模板
     */
    @ResponseBody
    @Operation(summary = "获取配置模板")
    @GetMapping("template/{templateType}")
    public ReturnResult<MonitorSysGenServerSetting> getTemplate(@PathVariable String templateType) {
        return monitorSysGenServerSettingService.getSettingTemplate(templateType);
    }

    /**
     * 导出配置
     *
     * @param serverIds 服务器ID列表
     * @param format 导出格式
     * @return 导出数据
     */
    @ResponseBody
    @Operation(summary = "导出配置")
    @PostMapping("export")
    public ReturnResult<String> exportSettings(@RequestBody List<Integer> serverIds, @RequestParam(defaultValue = "json") String format) {
        return monitorSysGenServerSettingService.exportSettings(serverIds, format);
    }

    /**
     * 导入配置
     *
     * @param configData 配置数据
     * @param format 数据格式
     * @param overwrite 是否覆盖现有配置
     * @return 导入结果
     */
    @ResponseBody
    @Operation(summary = "导入配置")
    @PostMapping("import")
    public ReturnResult<Boolean> importSettings(@RequestParam String configData, 
                                               @RequestParam(defaultValue = "json") String format,
                                               @RequestParam(defaultValue = "false") boolean overwrite) {
        return monitorSysGenServerSettingService.importSettings(configData, format, overwrite);
    }

    /**
     * 重置配置为默认值
     *
     * @param serverId 服务器ID
     * @return 操作结果
     */
    @ResponseBody
    @Operation(summary = "重置配置为默认值")
    @PostMapping("server/{serverId}/reset")
    public ReturnResult<Boolean> resetToDefault(@PathVariable Integer serverId) {
        return monitorSysGenServerSettingService.resetToDefault(serverId);
    }

    /**
     * 获取配置变更历史
     *
     * @param serverId 服务器ID
     * @param limit 限制数量
     * @return 变更历史
     */
    @ResponseBody
    @Operation(summary = "获取配置变更历史")
    @GetMapping("server/{serverId}/history")
    public ReturnResult<List<Map<String, Object>>> getHistory(@PathVariable Integer serverId, @RequestParam(defaultValue = "10") Integer limit) {
        return monitorSysGenServerSettingService.getSettingHistory(serverId, limit);
    }

    /**
     * 测试告警配置
     *
     * @param serverId 服务器ID
     * @return 测试结果
     */
    @ResponseBody
    @Operation(summary = "测试告警配置")
    @PostMapping("server/{serverId}/test/alert")
    public ReturnResult<Boolean> testAlert(@PathVariable Integer serverId) {
        return monitorSysGenServerSettingService.testAlertConfiguration(serverId);
    }

    /**
     * 测试上报配置
     *
     * @param serverId 服务器ID
     * @return 测试结果
     */
    @ResponseBody
    @Operation(summary = "测试上报配置")
    @PostMapping("server/{serverId}/test/report")
    public ReturnResult<Boolean> testReport(@PathVariable Integer serverId) {
        return monitorSysGenServerSettingService.testReportConfiguration(serverId);
    }

    /**
     * 获取推荐配置
     *
     * @param serverId 服务器ID
     * @return 推荐配置
     */
    @ResponseBody
    @Operation(summary = "获取推荐配置")
    @GetMapping("server/{serverId}/recommended")
    public ReturnResult<MonitorSysGenServerSetting> getRecommended(@PathVariable Integer serverId) {
        return monitorSysGenServerSettingService.getRecommendedSetting(serverId);
    }

    /**
     * 应用配置模板到多个服务器
     *
     * @param templateType 模板类型
     * @param serverIds 服务器ID列表
     * @return 操作结果
     */
    @ResponseBody
    @Operation(summary = "应用配置模板到多个服务器")
    @PostMapping("template/{templateType}/apply")
    public ReturnResult<Boolean> applyTemplate(@PathVariable String templateType, @RequestBody List<Integer> serverIds) {
        return monitorSysGenServerSettingService.applyTemplateToServers(templateType, serverIds);
    }
}
