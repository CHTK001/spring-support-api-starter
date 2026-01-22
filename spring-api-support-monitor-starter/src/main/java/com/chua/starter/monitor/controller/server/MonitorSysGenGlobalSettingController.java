package com.chua.starter.monitor.controller.server;

import com.chua.common.support.core.annotation.OnRouterEvent;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenGlobalSetting;
import com.chua.starter.monitor.service.server.MonitorSysGenGlobalSettingService;
import com.chua.starter.monitor.task.manager.ServerTaskManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 全局配置设置Controller
 *
 * @author CH
 * @since 2024/12/10
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/v1/monitor/global-setting")
@RequiredArgsConstructor
@Api(tags = "全局配置管理")
@Tag(name = "全局配置管理", description = "全局配置管理相关接口")
public class MonitorSysGenGlobalSettingController {

    private final MonitorSysGenGlobalSettingService globalSettingService;
    private final ServerTaskManager serverTaskManager;

    /**
     * 获取全局配置
     *
     * @return 全局配置
     */
    @GetMapping
    @OnRouterEvent("获取全局配置")
    @ApiOperation(value = "获取全局配置", notes = "获取系统全局配置信息")
    @Operation(summary = "获取全局配置", description = "获取系统全局配置信息")
    public ReturnResult<MonitorSysGenGlobalSetting> getGlobalSetting() {
        try {
            return globalSettingService.getGlobalSetting();
        } catch (Exception e) {
            log.error("获取全局配置失败: {}", e.getMessage(), e);
            return ReturnResult.error("获取全局配置失败: " + e.getMessage());
        }
    }

    /**
     * 保存或更新全局配置
     *
     * @param setting 全局配置
     * @return 操作结果
     */
    @PostMapping
    @OnRouterEvent("保存或更新全局配置")
    @ApiOperation(value = "保存或更新全局配置", notes = "保存或更新系统全局配置")
    @Operation(summary = "保存或更新全局配置", description = "保存或更新系统全局配置")
    public ReturnResult<MonitorSysGenGlobalSetting> saveOrUpdateGlobalSetting(
            @RequestBody MonitorSysGenGlobalSetting setting) {
        try {
            ReturnResult<MonitorSysGenGlobalSetting> result = globalSettingService.saveOrUpdateGlobalSetting(setting);
            
            if (result.isOk()) {
                // 配置更新后，刷新任务管理器配置
                serverTaskManager.refreshGlobalConfig();
                log.info("全局配置已更新，任务管理器配置已刷新");
            }
            
            return result;
        } catch (Exception e) {
            log.error("保存或更新全局配置失败: {}", e.getMessage(), e);
            return ReturnResult.error("保存或更新全局配置失败: " + e.getMessage());
        }
    }

    /**
     * 检查全局调度器是否启用
     *
     * @return true:启用 false:禁用
     */
    @GetMapping("/dispatcher-enabled")
    @OnRouterEvent("检查全局调度器是否启用")
    @ApiOperation(value = "检查全局调度器是否启用", notes = "检查全局调度器是否启用")
    @Operation(summary = "检查全局调度器是否启用", description = "检查全局调度器是否启用")
    public ReturnResult<Boolean> isGlobalDispatcherEnabled() {
        try {
            boolean enabled = globalSettingService.isGlobalDispatcherEnabled();
            return ReturnResult.ok(enabled);
        } catch (Exception e) {
            log.error("检查全局调度器状态失败: {}", e.getMessage(), e);
            return ReturnResult.error("检查全局调度器状态失败: " + e.getMessage());
        }
    }

    /**
     * 切换全局调度器开关
     *
     * @param enabled true:启用 false:禁用
     * @return 操作结果
     */
    @PutMapping("/dispatcher-enabled")
    @OnRouterEvent("切换全局调度器开关")
    @ApiOperation(value = "切换全局调度器开关", notes = "切换全局调度器开关")
    @Operation(summary = "切换全局调度器开关", description = "切换全局调度器开关")
    public ReturnResult<Boolean> toggleGlobalDispatcher(@RequestParam Boolean enabled) {
        try {
            ReturnResult<MonitorSysGenGlobalSetting> settingResult = globalSettingService.getGlobalSetting();
            if (!settingResult.isOk() || settingResult.getData() == null) {
                return ReturnResult.error("获取全局配置失败");
            }

            MonitorSysGenGlobalSetting setting = settingResult.getData();
            setting.setMonitorSysGenGlobalSettingDispatcherEnabled(enabled ? 1 : 0);
            
            ReturnResult<MonitorSysGenGlobalSetting> updateResult = 
                globalSettingService.saveOrUpdateGlobalSetting(setting);
            
            if (updateResult.isOk()) {
                // 刷新任务管理器配置
                serverTaskManager.refreshGlobalConfig();
                log.info("全局调度器开关已切换为: {}", enabled ? "启用" : "禁用");
                return ReturnResult.ok(enabled);
            }
            
            return ReturnResult.error("切换全局调度器开关失败");
        } catch (Exception e) {
            log.error("切换全局调度器开关失败: {}", e.getMessage(), e);
            return ReturnResult.error("切换全局调度器开关失败: " + e.getMessage());
        }
    }
}
