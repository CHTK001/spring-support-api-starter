package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.system.properties.SystemProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 系统管理控制器
 * <p>
 * 提供系统服务的管理功能，包括：
 * - 调试模式开关
 * - 服务器状态查看
 * - 配置信息查询
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/11
 */
@RestController
@RequestMapping("/system/admin")
@Slf4j
@Tag(name = "系统管理", description = "系统服务管理接口")
@RequiredArgsConstructor
public class SystemAdminController {

    private final SystemProperties systemProperties;
    
    /**
     * 日志系统（可能不存在）
     */
    private final LoggingSystem loggingSystem;

    /**
     * 调试模式状态
     * -- GETTER --
     *  是否为调试模式
     */
    @Getter
    private static volatile boolean debugMode = false;

    // ==================== 调试模式 ====================

    /**
     * 获取调试模式状态
     */
    @GetMapping("/debug/status")
    @Operation(summary = "获取调试模式状态")
    public ReturnResult<Map<String, Object>> getDebugStatus() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("debugMode", debugMode);
        result.put("systemLogLevel", getLogLevel("com.chua.starter.system"));
        return ReturnResult.ok(result);
    }

    /**
     * 开启调试模式
     */
    @PostMapping("/debug/enable")
    @Operation(summary = "开启调试模式")
    public ReturnResult<String> enableDebug() {
        debugMode = true;
        setLogLevel("com.chua.starter.system", LogLevel.DEBUG);
        log.info("【系统管理】调试模式已开启");
        return ReturnResult.ok("调试模式已开启");
    }

    /**
     * 关闭调试模式
     */
    @PostMapping("/debug/disable")
    @Operation(summary = "关闭调试模式")
    public ReturnResult<String> disableDebug() {
        debugMode = false;
        setLogLevel("com.chua.starter.system", LogLevel.INFO);
        log.info("【系统管理】调试模式已关闭");
        return ReturnResult.ok("调试模式已关闭");
    }

    // ==================== 服务器状态 ====================

    /**
     * 获取服务器状态
     */
    @GetMapping("/status")
    @Operation(summary = "获取服务器状态")
    public ReturnResult<Map<String, Object>> getServerStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("debugMode", debugMode);
        status.put("enable", systemProperties.isEnable());
        status.put("cacheEnabled", systemProperties.getCache().isEnable());
        status.put("cacheExpire", systemProperties.getCache().getExpire());
        status.put("logEnabled", systemProperties.getLog().isEnable());
        status.put("logAsync", systemProperties.getLog().isAsync());
        status.put("serverTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return ReturnResult.ok(status);
    }

    // ==================== 辅助方法 ====================

    private void setLogLevel(String loggerName, LogLevel level) {
        if (loggingSystem == null) {
            log.warn("LoggingSystem 不可用，无法设置日志级别");
            return;
        }
        try {
            loggingSystem.setLogLevel(loggerName, level);
        } catch (Exception e) {
            log.warn("设置日志级别失败: {} -> {}", loggerName, level);
        }
    }

    private String getLogLevel(String loggerName) {
        if (loggingSystem == null) {
            return "UNAVAILABLE";
        }
        try {
            var config = loggingSystem.getLoggerConfiguration(loggerName);
            return config != null && config.getEffectiveLevel() != null 
                    ? config.getEffectiveLevel().name() : "UNKNOWN";
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }
}


