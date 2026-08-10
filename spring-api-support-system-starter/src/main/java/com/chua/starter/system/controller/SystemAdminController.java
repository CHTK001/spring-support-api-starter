package com.chua.starter.system.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.system.properties.SystemProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 系统管理接口
 *
 * <p>提供系统级的运维管理能力,涵盖:</p>
 * <ul>
 *   <li>调试模式开关</li>
 *   <li>运行状态查看(JVM/内存/CPU)</li>
 *   <li>系统信息(版本/启动时间/环境)</li>
 *   <li>日志级别动态调整</li>
 * </ul>
 *
 * @author CH
 * @since 4.0.0.42
 */
@RestController
@RequestMapping("/system/admin")
@Slf4j
@Tag(name = "系统管理", description = "系统级管理接口")
@RequiredArgsConstructor
public class SystemAdminController {

    private final SystemProperties systemProperties;
    private final LoggingSystem loggingSystem;

    private static volatile boolean debugMode = false;
    private static final LocalDateTime START_TIME = LocalDateTime.now();

    // ==================== 调试模式 ====================

    @GetMapping("/debug/status")
    @Operation(summary = "获取调试模式状态")
    public ReturnResult<Map<String, Object>> getDebugStatus() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("debugMode", debugMode);
        result.put("systemLogLevel", getLogLevel("com.chua.starter.system"));
        return ReturnResult.ok(result);
    }

    @PostMapping("/debug/enable")
    @Operation(summary = "启用调试模式")
    public ReturnResult<String> enableDebug() {
        debugMode = true;
        setLogLevel("com.chua.starter.system", LogLevel.DEBUG);
        log.info("系统管理: 调试模式已启用");
        return ReturnResult.ok("调试模式已启用");
    }

    @PostMapping("/debug/disable")
    @Operation(summary = "关闭调试模式")
    public ReturnResult<String> disableDebug() {
        debugMode = false;
        setLogLevel("com.chua.starter.system", LogLevel.INFO);
        log.info("系统管理: 调试模式已关闭");
        return ReturnResult.ok("调试模式已关闭");
    }

    // ==================== 服务器状态 ====================

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

    // ==================== 系统信息 ====================

    @GetMapping("/info")
    @Operation(summary = "获取系统信息(版本/启动时间/Java 运行时)")
    public ReturnResult<Map<String, Object>> getSystemInfo() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("startTime", START_TIME.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        info.put("uptimeMs", Duration.between(START_TIME, LocalDateTime.now()).toMillis());
        info.put("jvmName", runtime.getVmName());
        info.put("jvmVersion", runtime.getVmVersion());
        info.put("jvmVendor", runtime.getVmVendor());
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("osName", System.getProperty("os.name"));
        info.put("osArch", System.getProperty("os.arch"));
        info.put("userHome", System.getProperty("user.home"));
        info.put("userDir", System.getProperty("user.dir"));
        info.put("processors", Runtime.getRuntime().availableProcessors());
        return ReturnResult.ok(info);
    }

    // ==================== JVM 指标 ====================

    @GetMapping("/metrics/jvm")
    @Operation(summary = "获取 JVM 运行时指标(堆/非堆/线程/CPU)")
    public ReturnResult<Map<String, Object>> getJvmMetrics() {
        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        Runtime runtime = Runtime.getRuntime();
        ThreadMXBean threads = ManagementFactory.getThreadMXBean();

        Map<String, Object> metrics = new LinkedHashMap<>();

        Map<String, Object> heap = new LinkedHashMap<>();
        heap.put("init", memory.getHeapMemoryUsage().getInit());
        heap.put("used", memory.getHeapMemoryUsage().getUsed());
        heap.put("committed", memory.getHeapMemoryUsage().getCommitted());
        heap.put("max", memory.getHeapMemoryUsage().getMax());
        metrics.put("heap", heap);

        Map<String, Object> nonHeap = new LinkedHashMap<>();
        nonHeap.put("init", memory.getNonHeapMemoryUsage().getInit());
        nonHeap.put("used", memory.getNonHeapMemoryUsage().getUsed());
        nonHeap.put("committed", memory.getNonHeapMemoryUsage().getCommitted());
        nonHeap.put("max", memory.getNonHeapMemoryUsage().getMax());
        metrics.put("nonHeap", nonHeap);

        metrics.put("totalMemory", runtime.totalMemory());
        metrics.put("freeMemory", runtime.freeMemory());
        metrics.put("maxMemory", runtime.maxMemory());
        metrics.put("availableProcessors", runtime.availableProcessors());
        metrics.put("systemLoadAverage", os.getSystemLoadAverage());
        metrics.put("threadCount", threads.getTotalStartedThreadCount());
        metrics.put("daemonThreadCount", threads.getDaemonThreadCount());
        metrics.put("peakThreadCount", threads.getPeakThreadCount());
        return ReturnResult.ok(metrics);
    }

    // ==================== 系统属性 ====================

    @GetMapping("/properties")
    @Operation(summary = "获取系统属性(过滤敏感字段)")
    public ReturnResult<Map<String, String>> getSystemProperties(
            @RequestParam(defaultValue = "java,spring,os,user") String prefix) {
        Properties props = System.getProperties();
        Map<String, String> filtered = new LinkedHashMap<>();
        String[] prefixes = prefix.split(",");
        for (String name : props.stringPropertyNames()) {
            for (String p : prefixes) {
                String trimmed = p.trim();
                if (!trimmed.isEmpty() && name.toLowerCase().startsWith(trimmed.toLowerCase())) {
                    filtered.put(name, maskIfSensitive(name, props.getProperty(name)));
                    break;
                }
            }
        }
        return ReturnResult.ok(filtered);
    }

    private static String maskIfSensitive(String key, String value) {
        if (value == null) {
            return null;
        }
        String lower = key.toLowerCase();
        if (lower.contains("password") || lower.contains("secret") || lower.contains("token")
                || lower.contains("credential") || lower.contains("key")) {
            return "***";
        }
        return value;
    }

    // ==================== GC 触发 ====================

    @PostMapping("/gc")
    @Operation(summary = "建议 JVM 进行 GC")
    public ReturnResult<Map<String, Object>> suggestGc() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        long before = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
        System.gc();
        long after = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("before", before);
        result.put("after", after);
        result.put("freed", before - after);
        return ReturnResult.ok(result);
    }

    // ==================== 临时文件清理 ====================

    @PostMapping("/temp/clear")
    @Operation(summary = "清理临时文件目录(默认 java.io.tmpdir)")
    public ReturnResult<Map<String, Object>> clearTemp(
            @RequestParam(required = false) String path,
            @RequestParam(defaultValue = "7") int olderThanDays) {
        File root = new File(path != null && !path.isEmpty() ? path : System.getProperty("java.io.tmpdir"));
        if (!root.exists() || !root.isDirectory()) {
            return ReturnResult.error("目录不存在: " + root.getAbsolutePath());
        }
        long cutoff = System.currentTimeMillis() - Duration.ofDays(olderThanDays).toMillis();
        int removed = 0;
        long freed = 0;
        File[] files = root.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile() && f.lastModified() < cutoff) {
                    long len = f.length();
                    if (f.delete()) {
                        removed++;
                        freed += len;
                    }
                }
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("path", root.getAbsolutePath());
        result.put("removedFiles", removed);
        result.put("freedBytes", freed);
        return ReturnResult.ok(result);
    }

    // ==================== 日志级别 ====================

    @PostMapping("/log/{level}")
    @Operation(summary = "动态设置日志级别(DEBUG/INFO/WARN/ERROR)")
    public ReturnResult<String> setLogLevelByPath(
            @PathVariable String level,
            @RequestParam(defaultValue = "com.chua.starter.system") String logger) {
        LogLevel logLevel;
        try {
            logLevel = LogLevel.valueOf(level.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ReturnResult.error("无效日志级别: " + level);
        }
        setLogLevel(logger, logLevel);
        log.info("日志级别调整: {} -> {}", logger, logLevel);
        return ReturnResult.ok("已设置 " + logger + " -> " + logLevel);
    }

    private void setLogLevel(String loggerName, LogLevel level) {
        if (loggingSystem == null) {
            log.warn("LoggingSystem 未配置,无法调整日志级别");
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
