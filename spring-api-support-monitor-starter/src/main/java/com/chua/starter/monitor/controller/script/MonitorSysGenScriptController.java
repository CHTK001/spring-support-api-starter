package com.chua.starter.monitor.controller.script;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenScript;
import com.chua.starter.monitor.entity.MonitorSysGenScriptExecution;
import com.chua.starter.monitor.pojo.ScriptManagementDTO;
import com.chua.starter.monitor.service.MonitorSysGenScriptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 脚本管理控制器
 *
 * @author CH
 * @since 2024/12/19
 */
@Slf4j
@RestController
@RequestMapping("script")
@RequiredArgsConstructor
@Validated
@Tag(name = "脚本管理", description = "脚本管理相关接口")
public class MonitorSysGenScriptController {

    private final MonitorSysGenScriptService monitorSysGenScriptService;

    /**
     * 分页查询脚本列表
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询脚本列表", description = "根据条件分页查询脚本列表")
    public ReturnResult<IPage<MonitorSysGenScript>> getScriptPage(
            @RequestBody ScriptManagementDTO.ScriptQueryDTO queryDTO) {
        try {
            IPage<MonitorSysGenScript> page = monitorSysGenScriptService.getScriptPage(queryDTO);
            return ReturnResult.ok(page);
        } catch (Exception e) {
            log.error("查询脚本列表失败", e);
            return ReturnResult.error("查询脚本列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取脚本详情
     */
    @GetMapping("/{scriptId}")
    @Operation(summary = "获取脚本详情", description = "根据脚本ID获取脚本详细信息")
    public ReturnResult<MonitorSysGenScript> getScriptById(
            @Parameter(description = "脚本ID") @PathVariable @NotNull Integer scriptId) {
        try {
            MonitorSysGenScript script = monitorSysGenScriptService.getScriptById(scriptId);
            if (script == null) {
                return ReturnResult.error("脚本不存在");
            }
            return ReturnResult.ok(script);
        } catch (Exception e) {
            log.error("获取脚本详情失败: scriptId={}", scriptId, e);
            return ReturnResult.error("获取脚本详情失败: " + e.getMessage());
        }
    }

    /**
     * 创建脚本
     */
    @PostMapping
    @Operation(summary = "创建脚本", description = "创建新的脚本")
    public ReturnResult<MonitorSysGenScript> createScript(@Valid @RequestBody MonitorSysGenScript script) {
        try {
            MonitorSysGenScript result = monitorSysGenScriptService.createScript(script);
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("创建脚本失败", e);
            return ReturnResult.error("创建脚本失败: " + e.getMessage());
        }
    }

    /**
     * 更新脚本
     */
    @PutMapping
    @Operation(summary = "更新脚本", description = "更新现有脚本信息")
    public ReturnResult<MonitorSysGenScript> updateScript(@Valid @RequestBody MonitorSysGenScript script) {
        try {
            MonitorSysGenScript result = monitorSysGenScriptService.updateScript(script);
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("更新脚本失败", e);
            return ReturnResult.error("更新脚本失败: " + e.getMessage());
        }
    }

    /**
     * 删除脚本
     */
    @DeleteMapping("/{scriptId}")
    @Operation(summary = "删除脚本", description = "根据脚本ID删除脚本")
    public ReturnResult<Boolean> deleteScript(
            @Parameter(description = "脚本ID") @PathVariable @NotNull Integer scriptId) {
        try {
            boolean result = monitorSysGenScriptService.deleteScript(scriptId);
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("删除脚本失败: scriptId={}", scriptId, e);
            return ReturnResult.error("删除脚本失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除脚本
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除脚本", description = "根据脚本ID列表批量删除脚本")
    public ReturnResult<Boolean> batchDeleteScripts(
            @Parameter(description = "脚本ID列表") @RequestBody List<Integer> scriptIds) {
        try {
            boolean result = monitorSysGenScriptService.batchDeleteScripts(scriptIds);
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("批量删除脚本失败: scriptIds={}", scriptIds, e);
            return ReturnResult.error("批量删除脚本失败: " + e.getMessage());
        }
    }

    /**
     * 更新脚本状态
     */
    @PutMapping("/{scriptId}/status")
    @Operation(summary = "更新脚本状态", description = "启用或禁用脚本")
    public ReturnResult<Boolean> updateScriptStatus(
            @Parameter(description = "脚本ID") @PathVariable @NotNull Integer scriptId,
            @Parameter(description = "脚本状态") @RequestParam @NotNull Integer status) {
        try {
            boolean result = monitorSysGenScriptService.updateScriptStatus(scriptId, status);
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("更新脚本状态失败: scriptId={}, status={}", scriptId, status, e);
            return ReturnResult.error("更新脚本状态失败: " + e.getMessage());
        }
    }

    /**
     * 复制脚本
     */
    @PostMapping("/{scriptId}/copy")
    @Operation(summary = "复制脚本", description = "复制现有脚本并创建新脚本")
    public ReturnResult<MonitorSysGenScript> copyScript(
            @Parameter(description = "脚本ID") @PathVariable @NotNull Integer scriptId,
            @Parameter(description = "新脚本名称") @RequestParam @NotNull String newScriptName) {
        try {
            MonitorSysGenScript result = monitorSysGenScriptService.copyScript(scriptId, newScriptName);
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("复制脚本失败: scriptId={}, newScriptName={}", scriptId, newScriptName, e);
            return ReturnResult.error("复制脚本失败: " + e.getMessage());
        }
    }

    /**
     * 检查脚本名称是否可用
     */
    @GetMapping("/check-name")
    @Operation(summary = "检查脚本名称", description = "检查脚本名称是否可用")
    public ReturnResult<Boolean> checkScriptName(
            @Parameter(description = "脚本名称") @RequestParam @NotNull String scriptName,
            @Parameter(description = "排除的脚本ID") @RequestParam(required = false) Integer excludeId) {
        try {
            boolean available = monitorSysGenScriptService.isScriptNameAvailable(scriptName, excludeId);
            return ReturnResult.ok(available);
        } catch (Exception e) {
            log.error("检查脚本名称失败: scriptName={}, excludeId={}", scriptName, excludeId, e);
            return ReturnResult.error("检查脚本名称失败: " + e.getMessage());
        }
    }

    /**
     * 执行脚本
     */
    @PostMapping("/{scriptId}/execute")
    @Operation(summary = "执行脚本", description = "执行指定的脚本")
    public ReturnResult<MonitorSysGenScriptExecution> executeScript(
            @Parameter(description = "脚本ID") @PathVariable @NotNull Integer scriptId,
            @RequestBody ScriptManagementDTO.ScriptExecuteRequestDTO requestDTO) {
        try {
            MonitorSysGenScriptExecution result = monitorSysGenScriptService.executeScript(scriptId, requestDTO);
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("执行脚本失败: scriptId={}", scriptId, e);
            return ReturnResult.error("执行脚本失败: " + e.getMessage());
        }
    }

    /**
     * 停止脚本执行
     */
    @PostMapping("/execution/{executionId}/stop")
    @Operation(summary = "停止脚本执行", description = "停止正在执行的脚本")
    public ReturnResult<Boolean> stopScriptExecution(
            @Parameter(description = "执行记录ID") @PathVariable @NotNull Long executionId) {
        try {
            boolean result = monitorSysGenScriptService.stopScriptExecution(executionId);
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("停止脚本执行失败: executionId={}", executionId, e);
            return ReturnResult.error("停止脚本执行失败: " + e.getMessage());
        }
    }

    /**
     * 获取脚本执行历史（支持多条件筛选）
     */
    @GetMapping("/execution/history")
    @Operation(summary = "获取脚本执行历史", description = "分页查询脚本执行历史记录，支持脚本ID、服务器ID、状态、触发类型、时间范围筛选")
    public ReturnResult<IPage<MonitorSysGenScriptExecution>> getExecutionHistory(
            @Parameter(description = "脚本ID") @RequestParam(required = false) Integer scriptId,
            @Parameter(description = "服务器ID") @RequestParam(required = false) Integer serverId,
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "触发类型") @RequestParam(required = false) String triggerType,
            @Parameter(description = "触发用户") @RequestParam(required = false) String triggerUser,
            @Parameter(description = "开始时间") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime endTime,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            IPage<MonitorSysGenScriptExecution> page = monitorSysGenScriptService.getExecutionHistory(
                    scriptId, serverId, status, triggerType, triggerUser, startTime, endTime, pageNum, pageSize);
            return ReturnResult.ok(page);
        } catch (Exception e) {
            log.error("获取脚本执行历史失败: scriptId={}", scriptId, e);
            return ReturnResult.error("获取脚本执行历史失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有脚本执行历史（不分页）
     */
    @GetMapping("/execution/history/all")
    @Operation(summary = "获取所有脚本执行历史", description = "不分页地获取执行历史，支持同分页接口的筛选条件")
    public ReturnResult<java.util.List<MonitorSysGenScriptExecution>> getAllExecutionHistory(
            @Parameter(description = "脚本ID") @RequestParam(required = false) Integer scriptId,
            @Parameter(description = "服务器ID") @RequestParam(required = false) Integer serverId,
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "触发类型") @RequestParam(required = false) String triggerType,
            @Parameter(description = "触发用户") @RequestParam(required = false) String triggerUser,
            @Parameter(description = "开始时间") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime endTime) {
        try {
            java.util.List<MonitorSysGenScriptExecution> list = monitorSysGenScriptService.getAllExecutionHistory(
                    scriptId, serverId, status, triggerType, triggerUser, startTime, endTime);
            return ReturnResult.ok(list);
        } catch (Exception e) {
            log.error("获取所有脚本执行历史失败", e);
            return ReturnResult.error("获取所有脚本执行历史失败: " + e.getMessage());
        }
    }

    /**
     * 清理过期执行记录
     */
    @DeleteMapping("/execution/clean")
    @Operation(summary = "清理过期执行记录", description = "清理早于指定天数的执行记录（不包含运行中）")
    public ReturnResult<Integer> cleanExpiredExecutions(@Parameter(description = "保留天数") @RequestParam(defaultValue = "30") Integer days) {
        try {
            int count = monitorSysGenScriptService.cleanExpiredExecutions(days);
            return ReturnResult.ok(count);
        } catch (Exception e) {
            log.error("清理执行记录失败", e);
            return ReturnResult.error("清理执行记录失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除执行记录
     */
    @DeleteMapping("/execution/batch")
    @Operation(summary = "批量删除执行记录", description = "批量删除选中的执行记录（不包含运行中）")
    public ReturnResult<Boolean> batchDeleteExecutions(@RequestBody java.util.List<Long> executionIds) {
        try {
            boolean ok = monitorSysGenScriptService.batchDeleteExecutions(executionIds);
            return ReturnResult.ok(ok);
        } catch (Exception e) {
            log.error("批量删除执行记录失败", e);
            return ReturnResult.error("批量删除执行记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取执行详情
     */
    @GetMapping("/execution/{executionId}")
    @Operation(summary = "获取执行详情", description = "根据执行记录ID获取执行详情")
    public ReturnResult<MonitorSysGenScriptExecution> getExecutionDetail(
            @Parameter(description = "执行记录ID") @PathVariable @NotNull Long executionId) {
        try {
            MonitorSysGenScriptExecution execution = monitorSysGenScriptService.getExecutionDetail(executionId);
            if (execution == null) {
                return ReturnResult.error("执行记录不存在");
            }
            return ReturnResult.ok(execution);
        } catch (Exception e) {
            log.error("获取执行详情失败: executionId={}", executionId, e);
            return ReturnResult.error("获取执行详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取正在运行的执行记录
     */
    @GetMapping("/execution/running")
    @Operation(summary = "获取正在运行的执行记录", description = "获取所有正在运行的脚本执行记录")
    public ReturnResult<List<MonitorSysGenScriptExecution>> getRunningExecutions() {
        try {
            List<MonitorSysGenScriptExecution> executions = monitorSysGenScriptService.getRunningExecutions();
            return ReturnResult.ok(executions);
        } catch (Exception e) {
            log.error("获取正在运行的执行记录失败", e);
            return ReturnResult.error("获取正在运行的执行记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取脚本统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取脚本统计信息", description = "获取脚本的统计信息")
    public ReturnResult<Map<String, Object>> getScriptStatistics() {
        try {
            Map<String, Object> statistics = monitorSysGenScriptService.getScriptStatistics();
            return ReturnResult.ok(statistics);
        } catch (Exception e) {
            log.error("获取脚本统计信息失败", e);
            return ReturnResult.error("获取脚本统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取最近执行的脚本
     */
    @GetMapping("/recent")
    @Operation(summary = "获取最近执行的脚本", description = "获取最近执行的脚本列表")
    public ReturnResult<List<MonitorSysGenScript>> getRecentScripts(
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<MonitorSysGenScript> scripts = monitorSysGenScriptService.getRecentScripts(limit);
            return ReturnResult.ok(scripts);
        } catch (Exception e) {
            log.error("获取最近执行的脚本失败", e);
            return ReturnResult.error("获取最近执行的脚本失败: " + e.getMessage());
        }
    }

    /**
     * 获取热门脚本
     */
    @GetMapping("/popular")
    @Operation(summary = "获取热门脚本", description = "获取执行次数最多的脚本列表")
    public ReturnResult<List<MonitorSysGenScript>> getPopularScripts(
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<MonitorSysGenScript> scripts = monitorSysGenScriptService.getPopularScripts(limit);
            return ReturnResult.ok(scripts);
        } catch (Exception e) {
            log.error("获取热门脚本失败", e);
            return ReturnResult.error("获取热门脚本失败: " + e.getMessage());
        }
    }

    /**
     * 导出脚本
     */
    @GetMapping("/{scriptId}/export")
    @Operation(summary = "导出脚本", description = "导出脚本配置为JSON格式")
    public ReturnResult<Map<String, Object>> exportScript(
            @Parameter(description = "脚本ID") @PathVariable @NotNull Integer scriptId) {
        try {
            Map<String, Object> exportData = monitorSysGenScriptService.exportScript(scriptId);
            return ReturnResult.ok(exportData);
        } catch (Exception e) {
            log.error("导出脚本失败: scriptId={}", scriptId, e);
            return ReturnResult.error("导出脚本失败: " + e.getMessage());
        }
    }

    /**
     * 导入脚本
     */
    @PostMapping("/import")
    @Operation(summary = "导入脚本", description = "从JSON数据导入脚本")
    public ReturnResult<MonitorSysGenScript> importScript(@RequestBody Map<String, Object> scriptData) {
        try {
            MonitorSysGenScript script = monitorSysGenScriptService.importScript(scriptData);
            return ReturnResult.ok(script);
        } catch (Exception e) {
            log.error("导入脚本失败", e);
            return ReturnResult.error("导入脚本失败: " + e.getMessage());
        }
    }

    /**
     * 获取支持的脚本类型
     */
    @GetMapping("/types")
    @Operation(summary = "获取支持的脚本类型", description = "获取系统支持的所有脚本类型")
    public ReturnResult<List<MonitorSysGenScript.ScriptType>> getSupportedScriptTypes() {
        try {
            List<MonitorSysGenScript.ScriptType> types = monitorSysGenScriptService.getSupportedScriptTypes();
            return ReturnResult.ok(types);
        } catch (Exception e) {
            log.error("获取支持的脚本类型失败", e);
            return ReturnResult.error("获取支持的脚本类型失败: " + e.getMessage());
        }
    }
}
