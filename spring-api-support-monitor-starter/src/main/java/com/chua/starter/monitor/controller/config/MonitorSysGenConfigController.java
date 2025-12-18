package com.chua.starter.monitor.controller.config;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenConfig;
import com.chua.starter.monitor.pojo.ConfigManagementDTO;
import com.chua.starter.monitor.service.config.MonitorSysGenConfigService;
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
import java.util.Map;

/**
 * 配置管理控制器
 *
 * @author CH
 * @since 2024/12/16
 */
@Slf4j
@RestController
@RequestMapping("v1/config")
@RequiredArgsConstructor
@Validated
@Tag(name = "配置管理", description = "配置管理相关接口")
public class MonitorSysGenConfigController {

    private final MonitorSysGenConfigService configService;

    /**
     * 分页查询配置列表
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询配置列表", description = "根据条件分页查询配置列表")
    public ReturnResult<IPage<MonitorSysGenConfig>> getConfigPage(
            @RequestBody ConfigManagementDTO.ConfigQueryDTO queryDTO) {
        try {
            IPage<MonitorSysGenConfig> page = configService.getConfigPage(queryDTO);
            return ReturnResult.ok(page);
        } catch (Exception e) {
            log.error("查询配置列表失败", e);
            return ReturnResult.error("查询配置列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取配置详情
     */
    @GetMapping("/{configId}")
    @Operation(summary = "获取配置详情", description = "根据配置ID获取配置详细信息")
    public ReturnResult<MonitorSysGenConfig> getConfigById(
            @Parameter(description = "配置ID") @PathVariable @NotNull Integer configId) {
        try {
            MonitorSysGenConfig config = configService.getConfigById(configId);
            if (config == null) {
                return ReturnResult.error("配置不存在");
            }
            return ReturnResult.ok(config);
        } catch (Exception e) {
            log.error("获取配置详情失败: configId={}", configId, e);
            return ReturnResult.error("获取配置详情失败: " + e.getMessage());
        }
    }

    /**
     * 创建配置
     */
    @PostMapping
    @Operation(summary = "创建配置", description = "创建新的配置")
    public ReturnResult<MonitorSysGenConfig> createConfig(@Valid @RequestBody MonitorSysGenConfig config) {
        try {
            MonitorSysGenConfig result = configService.createConfig(config);
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("创建配置失败", e);
            return ReturnResult.error("创建配置失败: " + e.getMessage());
        }
    }

    /**
     * 更新配置
     */
    @PutMapping
    @Operation(summary = "更新配置", description = "更新现有配置信息")
    public ReturnResult<MonitorSysGenConfig> updateConfig(@Valid @RequestBody MonitorSysGenConfig config) {
        try {
            MonitorSysGenConfig result = configService.updateConfig(config);
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("更新配置失败", e);
            return ReturnResult.error("更新配置失败: " + e.getMessage());
        }
    }

    /**
     * 删除配置
     */
    @DeleteMapping("/{configId}")
    @Operation(summary = "删除配置", description = "根据配置ID删除配置")
    public ReturnResult<Boolean> deleteConfig(
            @Parameter(description = "配置ID") @PathVariable @NotNull Integer configId) {
        try {
            boolean result = configService.deleteConfig(configId);
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("删除配置失败: configId={}", configId, e);
            return ReturnResult.error("删除配置失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除配置
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除配置", description = "根据配置ID列表批量删除配置")
    public ReturnResult<Boolean> batchDeleteConfigs(
            @Parameter(description = "配置ID列表") @RequestBody List<Integer> configIds) {
        try {
            boolean result = configService.batchDeleteConfigs(configIds);
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("批量删除配置失败: configIds={}", configIds, e);
            return ReturnResult.error("批量删除配置失败: " + e.getMessage());
        }
    }

    /**
     * 更新配置状态
     */
    @PutMapping("/{configId}/status")
    @Operation(summary = "更新配置状态", description = "启用或禁用配置")
    public ReturnResult<Boolean> updateConfigStatus(
            @Parameter(description = "配置ID") @PathVariable @NotNull Integer configId,
            @Parameter(description = "配置状态") @RequestParam @NotNull Integer status) {
        try {
            boolean result = configService.updateConfigStatus(configId, status);
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("更新配置状态失败: configId={}, status={}", configId, status, e);
            return ReturnResult.error("更新配置状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有环境列表
     */
    @GetMapping("/envs")
    @Operation(summary = "获取环境列表", description = "获取所有可用的环境列表")
    public ReturnResult<List<String>> getAllEnvs() {
        try {
            List<String> envs = configService.getAllEnvs();
            return ReturnResult.ok(envs);
        } catch (Exception e) {
            log.error("获取环境列表失败", e);
            return ReturnResult.error("获取环境列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有应用列表
     */
    @GetMapping("/apps")
    @Operation(summary = "获取应用列表", description = "获取所有可用的应用列表")
    public ReturnResult<List<String>> getAllApps() {
        try {
            List<String> apps = configService.getAllApps();
            return ReturnResult.ok(apps);
        } catch (Exception e) {
            log.error("获取应用列表失败", e);
            return ReturnResult.error("获取应用列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取配置统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取配置统计信息", description = "获取配置的统计信息")
    public ReturnResult<Map<String, Object>> getConfigStatistics() {
        try {
            Map<String, Object> statistics = configService.getConfigStatistics();
            return ReturnResult.ok(statistics);
        } catch (Exception e) {
            log.error("获取配置统计信息失败", e);
            return ReturnResult.error("获取配置统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 检查配置键是否可用
     */
    @GetMapping("/check-key")
    @Operation(summary = "检查配置键", description = "检查配置键在指定环境下是否可用")
    public ReturnResult<Boolean> checkConfigKey(
            @Parameter(description = "配置键") @RequestParam @NotNull String configKey,
            @Parameter(description = "环境") @RequestParam(required = false) String env,
            @Parameter(description = "排除的配置ID") @RequestParam(required = false) Integer excludeId) {
        try {
            boolean available = configService.isConfigKeyAvailable(configKey, env, excludeId);
            return ReturnResult.ok(available);
        } catch (Exception e) {
            log.error("检查配置键失败: configKey={}, env={}", configKey, env, e);
            return ReturnResult.error("检查配置键失败: " + e.getMessage());
        }
    }

    /**
     * 下发配置到节点
     */
    @PostMapping("/push")
    @Operation(summary = "下发配置", description = "将配置下发到指定的服务器节点")
    public ReturnResult<ConfigManagementDTO.ConfigPushResult> pushConfigToNodes(
            @Valid @RequestBody ConfigManagementDTO.ConfigPushRequest request) {
        try {
            ConfigManagementDTO.ConfigPushResult result = configService.pushConfigToNodes(request);
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("下发配置失败", e);
            return ReturnResult.error("下发配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取配置下发历史
     */
    @PostMapping("/push-history")
    @Operation(summary = "获取下发历史", description = "获取配置的下发历史记录")
    public ReturnResult<IPage<ConfigManagementDTO.ConfigPushHistory>> getPushHistory(
            @RequestBody ConfigManagementDTO.PushHistoryQueryDTO queryDTO) {
        try {
            IPage<ConfigManagementDTO.ConfigPushHistory> history = configService.getPushHistory(queryDTO);
            return ReturnResult.ok(history);
        } catch (Exception e) {
            log.error("获取下发历史失败", e);
            return ReturnResult.error("获取下发历史失败: " + e.getMessage());
        }
    }

    /**
     * 从历史记录还原推送
     */
    @PostMapping("/push-history/{historyId}/repush")
    @Operation(summary = "从历史还原推送", description = "使用历史记录中的配置值重新推送到服务器")
    public ReturnResult<ConfigManagementDTO.ConfigPushResult> repushFromHistory(
            @Parameter(description = "历史ID") @PathVariable @NotNull Long historyId) {
        try {
            ConfigManagementDTO.ConfigPushResult result = configService.repushFromHistory(historyId);
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("从历史还原推送失败: historyId={}", historyId, e);
            return ReturnResult.error("从历史还原推送失败: " + e.getMessage());
        }
    }

    /**
     * 批量从历史记录还原推送
     */
    @PostMapping("/push-history/batch-repush")
    @Operation(summary = "批量从历史还原推送", description = "批量使用历史记录中的配置值重新推送到服务器")
    public ReturnResult<ConfigManagementDTO.ConfigPushResult> batchRepushFromHistory(
            @Parameter(description = "历史ID列表") @RequestBody List<Long> historyIds) {
        try {
            ConfigManagementDTO.ConfigPushResult result = configService.batchRepushFromHistory(historyIds);
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("批量从历史还原推送失败", e);
            return ReturnResult.error("批量从历史还原推送失败: " + e.getMessage());
        }
    }

    /**
     * 删除推送历史
     */
    @DeleteMapping("/push-history/{historyId}")
    @Operation(summary = "删除推送历史", description = "删除指定的推送历史记录")
    public ReturnResult<Boolean> deletePushHistory(
            @Parameter(description = "历史ID") @PathVariable @NotNull Long historyId) {
        try {
            boolean result = configService.deletePushHistory(historyId);
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("删除推送历史失败: historyId={}", historyId, e);
            return ReturnResult.error("删除推送历史失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除推送历史
     */
    @DeleteMapping("/push-history/batch")
    @Operation(summary = "批量删除推送历史", description = "批量删除推送历史记录")
    public ReturnResult<Boolean> batchDeletePushHistory(
            @Parameter(description = "历史ID列表") @RequestBody List<Long> historyIds) {
        try {
            boolean result = configService.batchDeletePushHistory(historyIds);
            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("批量删除推送历史失败", e);
            return ReturnResult.error("批量删除推送历史失败: " + e.getMessage());
        }
    }

    /**
     * 导出配置
     */
    @PostMapping("/export")
    @Operation(summary = "导出配置", description = "导出指定的配置列表")
    public ReturnResult<List<MonitorSysGenConfig>> exportConfigs(
            @Parameter(description = "配置ID列表") @RequestBody List<Integer> configIds) {
        try {
            List<MonitorSysGenConfig> configs = configService.exportConfigs(configIds);
            return ReturnResult.ok(configs);
        } catch (Exception e) {
            log.error("导出配置失败", e);
            return ReturnResult.error("导出配置失败: " + e.getMessage());
        }
    }

    /**
     * 导入配置
     */
    @PostMapping("/import")
    @Operation(summary = "导入配置", description = "从配置列表导入配置")
    public ReturnResult<Integer> importConfigs(
            @RequestBody List<MonitorSysGenConfig> configs) {
        try {
            int count = configService.importConfigs(configs);
            return ReturnResult.ok(count);
        } catch (Exception e) {
            log.error("导入配置失败", e);
            return ReturnResult.error("导入配置失败: " + e.getMessage());
        }
    }

    /**
     * 根据环境获取配置
     */
    @GetMapping("/by-env/{env}")
    @Operation(summary = "根据环境获取配置", description = "获取指定环境的所有配置")
    public ReturnResult<List<MonitorSysGenConfig>> getConfigsByEnv(
            @Parameter(description = "环境") @PathVariable String env) {
        try {
            List<MonitorSysGenConfig> configs = configService.getConfigsByEnv(env);
            return ReturnResult.ok(configs);
        } catch (Exception e) {
            log.error("获取环境配置失败: env={}", env, e);
            return ReturnResult.error("获取环境配置失败: " + e.getMessage());
        }
    }

    /**
     * 根据应用获取配置
     */
    @GetMapping("/by-app/{app}")
    @Operation(summary = "根据应用获取配置", description = "获取指定应用的所有配置")
    public ReturnResult<List<MonitorSysGenConfig>> getConfigsByApp(
            @Parameter(description = "应用名") @PathVariable String app) {
        try {
            List<MonitorSysGenConfig> configs = configService.getConfigsByApp(app);
            return ReturnResult.ok(configs);
        } catch (Exception e) {
            log.error("获取应用配置失败: app={}", app, e);
            return ReturnResult.error("获取应用配置失败: " + e.getMessage());
        }
    }
}
