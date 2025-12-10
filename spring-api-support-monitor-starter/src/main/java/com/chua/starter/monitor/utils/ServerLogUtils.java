package com.chua.starter.monitor.utils;

import com.chua.starter.monitor.entity.MonitorSysGenServerLog;
import com.chua.starter.monitor.service.server.MonitorSysGenServerLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * 服务器日志记录工具类
 * 用于记录服务器相关的操作日志
 *
 * @author CH
 * @since 2024/12/20
 */
@Component
@Slf4j
public class ServerLogUtils {

    private static MonitorSysGenServerLogService logService;

    @Autowired
    public void setLogService(MonitorSysGenServerLogService logService) {
        ServerLogUtils.logService = logService;
    }

    /**
     * 记录服务器操作日志
     *
     * @param serverId 服务器ID
     * @param level    日志级别
     * @param source   日志来源
     * @param category 日志分类
     * @param content  日志内容
     */
    public static void logServerOperation(Integer serverId, String level, String source, String category, String content) {
        logServerOperation(serverId, level, source, category, content, null, null, null);
    }

    /**
     * 记录服务器操作日志（带异常信息）
     *
     * @param serverId  服务器ID
     * @param level     日志级别
     * @param source    日志来源
     * @param category  日志分类
     * @param content   日志内容
     * @param exception 异常信息
     */
    public static void logServerOperation(Integer serverId, String level, String source, String category, String content, Throwable exception) {
        String exceptionStack = exception != null ? getStackTrace(exception) : null;
        logServerOperation(serverId, level, source, category, content, exceptionStack, null, null);
    }

    /**
     * 记录服务器操作日志（完整参数）
     *
     * @param serverId       服务器ID
     * @param level          日志级别
     * @param source         日志来源
     * @param category       日志分类
     * @param content        日志内容
     * @param exceptionStack 异常堆栈
     * @param extraData      额外数据
     * @param tags           标签
     */
    public static void logServerOperation(Integer serverId, String level, String source, String category, 
                                        String content, String exceptionStack, String extraData, String tags) {
        if (logService == null) {
            log.warn("ServerLogService未初始化，无法记录服务器日志");
            return;
        }

        // 异步记录日志，避免影响主业务流程
        CompletableFuture.runAsync(() -> {
            try {
                MonitorSysGenServerLog serverLog = new MonitorSysGenServerLog();
                serverLog.setMonitorSysGenServerId(serverId);
                serverLog.setMonitorSysGenServerLogLevel(level);
                serverLog.setMonitorSysGenServerLogSource(source);
                serverLog.setMonitorSysGenServerLogCategory(category);
                serverLog.setMonitorSysGenServerLogContent(content);
                serverLog.setMonitorSysGenServerLogExceptionStack(exceptionStack);
                serverLog.setMonitorSysGenServerLogExtraData(extraData);
                serverLog.setMonitorSysGenServerLogTags(tags);
                serverLog.setMonitorSysGenServerLogCreateTime(LocalDateTime.now());
                serverLog.setMonitorSysGenServerLogServerTime(LocalDateTime.now());
                serverLog.setMonitorSysGenServerLogStatus(1); // 正常状态

                // 设置线程信息
                Thread currentThread = Thread.currentThread();
                serverLog.setMonitorSysGenServerLogThread(currentThread.getName());

                // 设置主机信息
                try {
                    java.net.InetAddress localHost = java.net.InetAddress.getLocalHost();
                    serverLog.setMonitorSysGenServerLogIp(localHost.getHostAddress());
                    serverLog.setMonitorSysGenServerLogHostname(localHost.getHostName());
                } catch (Exception e) {
                    log.debug("获取主机信息失败", e);
                }

                // 设置进程ID
                try {
                    String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
                    String pid = processName.split("@")[0];
                    serverLog.setMonitorSysGenServerLogProcessId(pid);
                } catch (Exception e) {
                    log.debug("获取进程ID失败", e);
                }

                logService.save(serverLog);
                
            } catch (Exception e) {
                log.error("记录服务器日志失败: serverId={}, content={}", serverId, content, e);
            }
        });
    }

    /**
     * 记录服务器连接日志
     */
    public static void logServerConnect(Integer serverId, String protocol, String host, Integer port, boolean success, String message) {
        String level = success ? "INFO" : "ERROR";
        String content = String.format("服务器连接%s: %s://%s:%d - %s", 
                                      success ? "成功" : "失败", protocol, host, port, message);
        logServerOperation(serverId, level, "SYSTEM", "CONNECTION", content);
    }

    /**
     * 记录服务器断开连接日志
     */
    public static void logServerDisconnect(Integer serverId, String protocol, String reason) {
        String content = String.format("服务器断开连接: %s - %s", protocol, reason);
        logServerOperation(serverId, "INFO", "SYSTEM", "CONNECTION", content);
    }

    /**
     * 记录服务器命令执行日志
     */
    public static void logServerCommand(Integer serverId, String command, boolean success, String result, String error) {
        String level = success ? "INFO" : "ERROR";
        String content = String.format("执行命令: %s", command);
        String extraData = String.format("{\"command\":\"%s\",\"success\":%b,\"result\":\"%s\",\"error\":\"%s\"}", 
                                       command, success, result != null ? result : "", error != null ? error : "");
        logServerOperation(serverId, level, "APPLICATION", "COMMAND", content, null, extraData, null);
    }

    /**
     * 记录文件操作日志
     */
    public static void logFileOperation(Integer serverId, String operation, String filePath, boolean success, String message) {
        String level = success ? "INFO" : "ERROR";
        String content = String.format("文件%s%s: %s - %s", operation, success ? "成功" : "失败", filePath, message);
        logServerOperation(serverId, level, "APPLICATION", "FILE", content);
    }

    /**
     * 记录配置变更日志
     */
    public static void logConfigChange(Integer serverId, String configType, String oldValue, String newValue, String operator) {
        String content = String.format("配置变更: %s 由 [%s] 修改为 [%s], 操作人: %s", 
                                      configType, oldValue, newValue, operator);
        String extraData = String.format("{\"configType\":\"%s\",\"oldValue\":\"%s\",\"newValue\":\"%s\",\"operator\":\"%s\"}", 
                                       configType, oldValue, newValue, operator);
        logServerOperation(serverId, "INFO", "SYSTEM", "CONFIG", content, null, extraData, null);
    }

    /**
     * 记录监控操作日志
     */
    public static void logMonitorOperation(Integer serverId, String operation, String details) {
        String content = String.format("监控操作: %s - %s", operation, details);
        logServerOperation(serverId, "INFO", "SYSTEM", "MONITOR", content);
    }

    /**
     * 记录安全相关日志
     */
    public static void logSecurityEvent(Integer serverId, String event, String details, String level) {
        String content = String.format("安全事件: %s - %s", event, details);
        logServerOperation(serverId, level, "SECURITY", "SECURITY", content);
    }

    /**
     * 记录访问日志
     */
    public static void logAccess(Integer serverId, String user, String action, String resource, String ip) {
        String content = String.format("用户访问: %s 执行 %s 操作，资源: %s，IP: %s", user, action, resource, ip);
        String extraData = String.format("{\"login\":\"%s\",\"action\":\"%s\",\"resource\":\"%s\",\"ip\":\"%s\"}",
                                       user, action, resource, ip);
        logServerOperation(serverId, "INFO", "ACCESS", "ACCESS", content, null, extraData, null);
    }

    /**
     * 获取异常堆栈信息
     */
    private static String getStackTrace(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * 记录错误日志
     */
    public static void logError(Integer serverId, String message, Throwable exception) {
        logServerOperation(serverId, "ERROR", "SYSTEM", "ERROR", message, exception);
    }

    /**
     * 记录警告日志
     */
    public static void logWarning(Integer serverId, String message) {
        logServerOperation(serverId, "WARN", "SYSTEM", "WARNING", message);
    }

    /**
     * 记录信息日志
     */
    public static void logInfo(Integer serverId, String message) {
        logServerOperation(serverId, "INFO", "SYSTEM", "INFO", message);
    }

    /**
     * 记录调试日志
     */
    public static void logDebug(Integer serverId, String message) {
        logServerOperation(serverId, "DEBUG", "SYSTEM", "DEBUG", message);
    }
}
