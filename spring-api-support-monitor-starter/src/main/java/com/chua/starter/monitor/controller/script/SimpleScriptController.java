package com.chua.starter.monitor.controller.script;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenScript;
import com.chua.starter.monitor.pojo.ScriptManagementDTO;
import com.chua.starter.monitor.service.script.MonitorSysGenScriptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 简化版脚本管理控制器
 * 只保留核心的CRUD功能
 *
 * @author CH
 * @since 2024-12-11
 * @version 2.0
 */
@Slf4j
@RestController
@RequestMapping("script")
@RequiredArgsConstructor
@Validated
@Tag(name = "脚本管理", description = "简化版脚本管理接口 - 只保留核心CRUD功能")
public class SimpleScriptController {

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
            @Parameter(description = "脚本状态") @RequestParam @NotNull String status) {
        try {
            // 将字符串状态转换为整数
            Integer statusValue = "ENABLED".equals(status) ? 1 : 0;
            boolean result = monitorSysGenScriptService.updateScriptStatus(scriptId, statusValue);
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
            @Parameter(description = "新脚本名称") @RequestParam(required = false) String newScriptName) {
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
}
