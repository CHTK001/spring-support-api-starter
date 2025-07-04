package com.chua.starter.monitor.starter.controller;

import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerSettingService;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.mybatis.utils.PageResultUtils;
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
 * 管理服务器的监控配置和运行时设置，包括：
 * - 监控配置：启用/禁用监控、数据收集频率、数据保留策略
 * - 数据上报：上报方式选择（NONE/LOCAL/API/PROMETHEUS）、上报配置
 * - 告警配置：告警阈值设置、通知方式配置
 * - 高级配置：连接超时、重试策略、性能优化参数
 * - 配置管理：导入/导出、模板应用、批量配置
 *
 * 与基础服务器管理的区别：
 * - MonitorSysGenServerController：管理服务器基本信息（连接信息、认证等）
 * - MonitorSysGenServerSettingController：管理服务器运行时配置（监控、告警等）
 *
 * 主要功能：
 * - 指标管理：配置数据收集和上报方式
 * - 告警管理：设置监控阈值和通知规则
 * - 配置模板：预设配置模板的应用和管理
 *
 * @author CH
 * @since 2024/12/26
 */
@RestController
@RequestMapping("v1/gen/server/setting")
@Tag(name = "服务器配置设置", description = "服务器监控配置和运行时设置管理API")
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
     * 根据服务器ID获取或创建配置
     *
     * 获取指定服务器的配置设置，如果不存在则自动创建默认配置。
     * 包含的配置项：
     * - 基础监控配置：启用状态、数据收集频率、数据保留时间
     * - 数据上报配置：上报方式、上报地址、认证信息
     * - 告警配置：告警阈值、通知方式、联系人信息
     * - 高级配置：连接超时、Docker监控、日志监控等
     *
     * 支持两个路径：
     * - /server/{serverId}: 仅获取现有配置
     * - /server/{serverId}/or-create: 获取配置，不存在时创建默认配置
     *
     * @param serverId 服务器ID
     * @return 服务器配置设置，包含完整的监控和告警配置
     */
    @ResponseBody
    @Operation(summary = "获取或创建服务器配置", description = "获取服务器的监控配置，不存在时自动创建默认配置")
    @GetMapping({"server/{serverId}", "server/{serverId}/or-create"})
    public ReturnResult<MonitorSysGenServerSetting> getByServerId(@PathVariable("serverId") Integer serverId) {
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
    @PostMapping({"save", "save-or-update"})
    public ReturnResult<Boolean> saveOrUpdate(@Validated @RequestBody MonitorSysGenServerSetting setting, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ReturnResult.error(bindingResult.getFieldError().getDefaultMessage());
        }
        return monitorSysGenServerSettingService.saveOrUpdateSetting(setting);
    }

    /**
     * 更新服务器配置
     *
     * @param setting 服务器配置
     * @param bindingResult 验证结果
     * @return 操作结果
     */
    @ResponseBody
    @Operation(summary = "更新服务器配置")
    @PutMapping("update")
    public ReturnResult<Boolean> update(@Validated @RequestBody MonitorSysGenServerSetting setting, BindingResult bindingResult) {
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
    @PostMapping({"batch/save", "batch-save-or-update"})
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
     * 根据ID删除配置
     *
     * @param id 配置ID
     * @return 操作结果
     */
    @ResponseBody
    @Operation(summary = "根据ID删除配置")
    @DeleteMapping("delete")
    public ReturnResult<Boolean> deleteById(@RequestParam Integer id) {
        return monitorSysGenServerSettingService.deleteById(id);
    }

    /**
     * 批量删除配置
     *
     * @param ids 配置ID列表
     * @return 操作结果
     */
    @ResponseBody
    @Operation(summary = "批量删除配置")
    @DeleteMapping("batch-delete")
    public ReturnResult<Boolean> batchDelete(@RequestBody List<Integer> ids) {
        return monitorSysGenServerSettingService.batchDeleteByIds(ids);
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
     * 批量更新监控启用状态
     *
     * @param requestBody 请求体，包含serverIds和enabled
     * @return 操作结果
     */
    @ResponseBody
    @Operation(summary = "批量更新监控启用状态")
    @PutMapping("batch-update-monitor-enabled")
    public ReturnResult<Boolean> batchUpdateMonitorEnabled(@RequestBody Map<String, Object> requestBody) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> serverIds = (List<Integer>) requestBody.get("serverIds");
            Boolean enabled = (Boolean) requestBody.get("enabled");

            if (serverIds == null || enabled == null) {
                return ReturnResult.error("serverIds和enabled参数不能为空");
            }

            return monitorSysGenServerSettingService.batchUpdateMonitorEnabled(serverIds, enabled);
        } catch (Exception e) {
            return ReturnResult.error("参数格式错误: " + e.getMessage());
        }
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
    @GetMapping({"template/{templateType}", "template"})
    public ReturnResult<MonitorSysGenServerSetting> getTemplate(@PathVariable(required = false) String templateType,
                                                                @RequestParam(required = false) String templateTypeParam) {
        String type = templateType != null ? templateType : templateTypeParam;
        if (type == null) {
            type = "default";
        }
        return monitorSysGenServerSettingService.getSettingTemplate(type);
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
    @PostMapping({"server/{serverId}/test/alert", "test-alert/{serverId}"})
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
    @PostMapping({"server/{serverId}/test/report", "test-report/{serverId}"})
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

    /**
     * 应用配置模板到多个服务器（通用接口）
     *
     * @param requestBody 请求体，包含templateType和serverIds
     * @return 操作结果
     */
    @ResponseBody
    @Operation(summary = "应用配置模板到多个服务器")
    @PostMapping("apply-template")
    public ReturnResult<Boolean> applyTemplateGeneric(@RequestBody Map<String, Object> requestBody) {
        try {
            String templateType = (String) requestBody.get("templateType");
            @SuppressWarnings("unchecked")
            List<Integer> serverIds = (List<Integer>) requestBody.get("serverIds");

            if (templateType == null || serverIds == null) {
                return ReturnResult.error("templateType和serverIds参数不能为空");
            }

            return monitorSysGenServerSettingService.applyTemplateToServers(templateType, serverIds);
        } catch (Exception e) {
            return ReturnResult.error("参数格式错误: " + e.getMessage());
        }
    }
}
