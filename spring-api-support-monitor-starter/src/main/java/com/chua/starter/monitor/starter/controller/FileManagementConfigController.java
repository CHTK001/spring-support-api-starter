package com.chua.starter.monitor.starter.controller;

import com.chua.common.support.annotations.OnRouterEvent;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.protocol.request.Request;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.starter.enums.FileManagementMode;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerSettingService;
import com.chua.starter.monitor.starter.utils.MonitorSysGenServerSettingUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件管理配置控制器 简化的文件管理配置API
 *
 * @author CH
 * @since 2024/12/26
 */
@Slf4j
@RestController
@RequestMapping("/api/file-management-config")
@RequiredArgsConstructor
@Tag(name = "文件管理配置", description = "文件管理配置相关接口")
public class FileManagementConfigController {

    private final MonitorSysGenServerService serverService;
    private final MonitorSysGenServerSettingService serverSettingService;

    /**
     * 获取文件管理配置
     */
    @GetMapping("/{serverId}")
    @Operation(summary = "获取文件管理配置", description = "根据服务器ID获取文件管理配置信息")
    public ReturnResult<Map<String, Object>> getFileManagementConfig(
            @Parameter(description = "服务器ID") @PathVariable Integer serverId) {
        try {
            // 获取服务器信息
            MonitorSysGenServer server = serverService.getById(serverId);
            if (server == null) {
                return ReturnResult.error("服务器不存在");
            }

            // 获取服务器配置
            ReturnResult<MonitorSysGenServerSetting> settingResult = serverSettingService.getOrCreateSetting(serverId);
            if (!settingResult.isOk()) {
                return ReturnResult.error("获取服务器配置失败");
            }

            MonitorSysGenServerSetting setting = settingResult.getData();
            FileManagementMode mode = MonitorSysGenServerSettingUtils.getFileManagementModeEnum(setting);

            Map<String, Object> config = new HashMap<>();
            config.put("serverId", serverId);
            config.put("serverName", server.getMonitorSysGenServerName());
            config.put("isLocalServer", server.getMonitorSysGenServerIsLocal() == 1);

            // 文件管理配置
            config.put("enabled", MonitorSysGenServerSettingUtils.isFileManagementEnabled(setting));
            config.put("mode", mode.getCode());
            config.put("modeName", mode.getName());
            config.put("modeDescription", mode.getDescription());

            // API配置（如果是API模式）
            if (mode == FileManagementMode.API) {
                config.put("apiConfig", MonitorSysGenServerSettingUtils.getFileManagementApiConfigObject(setting));
            }

            // NODE客户端（如果是NODE模式）
            if (mode == FileManagementMode.NODE) {
                config.put("nodeClient", setting.getMonitorSysGenServerSettingFileManagementNodeClient());
            }

            // 通用配置
            config.put("timeout", MonitorSysGenServerSettingUtils.getFileManagementTimeoutOrDefault(setting));
            config.put("maxRetries", MonitorSysGenServerSettingUtils.getFileManagementMaxRetriesOrDefault(setting));

            // 支持的操作
            config.put("supportedOperations", mode.getSupportedOperations());
            config.put("capabilities", getCapabilities(mode));

            return ReturnResult.ok(config);
        } catch (Exception e) {
            log.error("获取文件管理配置失败: serverId={}", serverId, e);
            return ReturnResult.error("获取配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取可用的文件管理模式
     */
    @GetMapping("/modes")
    @Operation(summary = "获取可用的文件管理模式", description = "获取系统支持的所有文件管理模式")
    public ReturnResult<Map<String, Object>> getAvailableModes(
            @Parameter(description = "服务器ID") @RequestParam(required = false) Integer serverId) {
        try {
            Map<String, Object> result = new HashMap<>();

            boolean isLocalServer = false;
            if (serverId != null) {
                MonitorSysGenServer serverInfo = serverService.getById(serverId);
                if (serverInfo != null) {
                    isLocalServer = serverInfo.getMonitorSysGenServerIsLocal() == 1;
                }
            }

            // 构建可用模式列表
            Map<String, Object> modes = new HashMap<>();
            for (FileManagementMode mode : FileManagementMode.values()) {
                // 本地模式只对本地服务器可用
                if (mode == FileManagementMode.LOCAL && !isLocalServer) {
                    continue;
                }

                Map<String, Object> modeInfo = new HashMap<>();
                modeInfo.put("code", mode.getCode());
                modeInfo.put("name", mode.getName());
                modeInfo.put("description", mode.getDescription());
                modeInfo.put("enabled", mode.supportsFileManagement());
                modeInfo.put("requiresApiConfig", mode.requiresManualApiConfig());
                modeInfo.put("requiresNodeClient", mode.requiresClientOnline());
                modeInfo.put("requiresSshConfig", mode.requiresSshConnection());
                modeInfo.put("defaultTimeout", mode.getDefaultTimeout());
                modeInfo.put("defaultMaxRetries", mode.getDefaultMaxRetries());

                modes.put(mode.getCode(), modeInfo);
            }

            result.put("modes", modes);
            result.put("isLocalServer", isLocalServer);

            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("获取可用文件管理模式失败", e);
            return ReturnResult.error("获取模式失败: " + e.getMessage());
        }
    }

    /**
     * 测试文件管理连接
     */
    @PostMapping("/{serverId}/test")
    @Operation(summary = "测试文件管理连接", description = "测试指定服务器的文件管理连接是否正常")
    public ReturnResult<Boolean> testConnection(@Parameter(description = "服务器ID") @PathVariable Integer serverId) {
        try {
            return serverSettingService.testFileManagementConnection(serverId);
        } catch (Exception e) {
            log.error("测试文件管理连接失败: serverId={}", serverId, e);
            return ReturnResult.error("测试连接失败: " + e.getMessage());
        }
    }

    /**
     * 获取NODE客户端列表
     */
    @GetMapping("/{serverId}/node-clients")
    @Operation(summary = "获取NODE客户端列表", description = "获取指定服务器可用的NODE客户端列表")
    public ReturnResult<Object> getNodeClients(@Parameter(description = "服务器ID") @PathVariable Integer serverId) {
        try {
            // TODO: 实现获取NODE客户端列表
            log.info("获取NODE客户端列表: serverId={}", serverId);
            return ReturnResult.ok("NODE客户端列表功能待实现");
        } catch (Exception e) {
            log.error("获取NODE客户端列表失败: serverId={}", serverId, e);
            return ReturnResult.error("获取客户端列表失败: " + e.getMessage());
        }
    }

    /**
     * 验证文件管理配置
     */
    @PostMapping("/{serverId}/validate")
    @Operation(summary = "验证文件管理配置", description = "验证指定服务器的文件管理配置是否正确")
    public ReturnResult<Map<String, Object>> validateConfig(
            @Parameter(description = "服务器ID") @PathVariable Integer serverId, @RequestBody Map<String, Object> config) {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("valid", true);
            result.put("errors", new String[0]);
            result.put("warnings", new String[0]);

            String mode = (String) config.get("mode");
            if (StringUtils.isEmpty(mode)) {
                result.put("valid", false);
                result.put("errors", new String[]{"文件管理模式不能为空"});
                return ReturnResult.ok(result);
            }

            FileManagementMode fileMode = FileManagementMode.fromCode(mode);

            // 根据模式验证配置
            switch (fileMode) {
                case API:
                    validateApiConfig(config, result);
                    break;
                case NODE:
                    validateNodeConfig(config, result);
                    break;
                case SSH:
                    validateSshConfig(serverId, result);
                    break;
                case LOCAL:
                    validateLocalConfig(serverId, result);
                    break;
                default:
                    break;
            }

            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("验证文件管理配置失败: serverId={}", serverId, e);
            return ReturnResult.error("验证配置失败: " + e.getMessage());
        }
    }

    /**
     * WebSocket事件处理
     */
    @OnRouterEvent("file-management-config")
    public void handleWebSocketEvent(Request request) {
        try {
            String action = (String) request.getParameter("action");
            Integer serverId = Integer.valueOf((String) request.getParameter("serverId"));

            switch (action) {
                case "test-connection":
                    handleTestConnection(request, serverId);
                    break;
                case "get-node-clients":
                    handleGetNodeClients(request, serverId);
                    break;
                default:
                    log.warn("未知的WebSocket事件: {}", action);
                    break;
            }
        } catch (Exception e) {
            log.error("处理WebSocket事件失败", e);
        }
    }

    // 私有方法
    private Map<String, Object> getCapabilities(FileManagementMode mode) {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("supportsRealtimeOperations", mode.supportsRealtimeOperations());
        capabilities.put("supportsLargeFileTransfer", mode.supportsLargeFileTransfer());
        capabilities.put("supportsPermissionManagement", mode.supportsPermissionManagement());
        capabilities.put("supportsArchiveOperations", mode.supportsArchiveOperations());
        capabilities.put("supportsFileSearch", mode.supportsFileSearch());
        capabilities.put("supportsFilePreview", mode.supportsFilePreview());
        return capabilities;
    }

    private void validateApiConfig(Map<String, Object> config, Map<String, Object> result) {
        // TODO: 实现API配置验证
    }

    private void validateNodeConfig(Map<String, Object> config, Map<String, Object> result) {
        // TODO: 实现NODE配置验证
    }

    private void validateSshConfig(Integer serverId, Map<String, Object> result) {
        // TODO: 实现SSH配置验证
    }

    private void validateLocalConfig(Integer serverId, Map<String, Object> result) {
        // TODO: 实现本地配置验证
    }

    private void handleTestConnection(Request request, Integer serverId) {
        // TODO: 实现WebSocket测试连接
    }

    private void handleGetNodeClients(Request request, Integer serverId) {
        // TODO: 实现WebSocket获取NODE客户端
    }
}
