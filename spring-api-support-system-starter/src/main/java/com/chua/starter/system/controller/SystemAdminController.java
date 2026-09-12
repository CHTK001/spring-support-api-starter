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
 * @since 4.0.0.42
 * @author CH
 */
@RestController
@RequestMapping("/system/admin")
@Slf4j
@Tag(name = "系统管理", description = "系统级管理接口")
@RequiredArgsConstructor
public class SystemAdminController {

    /**
     * 系统服务配置属性
     */
    private final SystemProperties systemProperties;

    /**
     * 日志系统
     */
    private final LoggingSystem loggingSystem;

    /**
     * 调试模式开关状态
     */
    private static volatile boolean debugMode = false;

    /**
     * 服务启动时间
     */
    private static final LocalDateTime START_TIME = LocalDateTime.now();

    /**
     * 系统默认日志包名
     */
    private static final String SYSTEM_LOG_PACKAGE = "com.chua.starter.system";

    /**
     * 调试模式提示信息
     */
    private static final String MSG_DEBUG_ENABLED = "调试模式已启用";

    /**
     * 关闭调试模式提示信息
     */
    private static final String MSG_DEBUG_DISABLED = "调试模式已关闭";

    /**
     * 敏感信息掩码
     */
    private static final String SENSITIVE_MASK = "***";

    // ==================== 调试模式 ====================

    /**
     * 获取调试模式状态。
     *
     * @return 调试模式状态信息
     */
    @GetMapping("/debug/status")
    @Operation(summary = "获取调试模式状态")
    public ReturnResult<Map<String, Object>> getDebugStatus() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("debugMode", debugMode);
        result.put("systemLogLevel", getLogLevel(SYSTEM_LOG_PACKAGE));
        return ReturnResult.ok(result);
    }

    /**
     * 启用调试模式。
     *
     * @return 启用成功提示
     */
    @PostMapping("/debug/enable")
    @Operation(summary = "启用调试模式")
    public ReturnResult<String> enableDebug() {
        debugMode = true;
        setLogLevel(SYSTEM_LOG_PACKAGE, LogLevel.DEBUG);
        log.info("系统管理: 调试模式已启用");
        return ReturnResult.ok(MSG_DEBUG_ENABLED);
    }

    /**
     * 关闭调试模式。
     *
     * @return 关闭成功提示
     */
    @PostMapping("/debug/disable")
    @Operation(summary = "关闭调试模式")
    public ReturnResult<String> disableDebug() {
        debugMode = false;
        setLogLevel(SYSTEM_LOG_PACKAGE, LogLevel.INFO);
        log.info("系统管理: 调试模式已关闭");
        return ReturnResult.ok(MSG_DEBUG_DISABLED);
    }

    // ==================== 服务器状态 ====================

    /**
     * 获取服务器运行状态。
     *
     * @return 服务器状态信息
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

    // ==================== 系统信息 ====================

    /**
     * 获取系统信息 (版本/启动时间/Java 运行时)。
     *
     * @return 系统信息
     */
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

    /**
     * 获取 JVM 运行时指标 (堆/非堆/线程/CPU)。
     *
     * @return JVM 指标信息
     */
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

    /**
     * 获取系统属性, 支持按前缀过滤并掩码敏感字段。
     *
     * @param prefix 属性前缀, 逗号分隔
     * @return 过滤后的系统属性
     */
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

    /**
     * 对敏感属性值进行掩码处理。
     *
     * @param key   属性名
     * @param value 属性值
     * @return 掩码后的属性值, 敏感字段返回 "***"
     */
    private static String maskIfSensitive(String key, String value) {
        if (value == null) {
            return null;
        }
        String lower = key.toLowerCase();
        if (lower.contains("password") || lower.contains("secret") || lower.contains("token")
                || lower.contains("credential") || lower.contains("key")) {
            return SENSITIVE_MASK;
        }
        return value;
    }

    // ==================== GC 触发 ====================

    /**
     * 建议 JVM 进行垃圾回收, 并返回回收前后堆内存变化。
     *
     * @return GC 前后堆内存信息
     */
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

    /**
     * 清理临时文件目录中超过指定天数的文件。
     *
     * @param path          临时文件目录, 为空时使用系统临时目录
     * @param olderThanDays 超过的天数
     * @return 清理结果信息
     */
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

    /**
     * 动态设置日志级别。
     *
     * @param level  日志级别
     * @param logger 目标日志
     * @return 设置结果提示
     */
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

    /**
     * 设置指定日志的日志级别。
     *
     * @param loggerName 日志名称
     * @param level      日志级别
     */
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

    /**
     * 获取指定日志的有效级别。
     *
     * @param loggerName 日志名称
     * @return 日志级别名称, 不可用时返回 UNKNOWN 或 不可用
     */
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
