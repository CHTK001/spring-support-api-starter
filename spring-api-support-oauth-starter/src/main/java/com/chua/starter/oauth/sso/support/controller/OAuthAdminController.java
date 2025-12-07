package com.chua.starter.oauth.sso.support.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * OAuth 管理控制器
 * <p>
 * 提供 OAuth 认证服务的管理功能，包括：
 * - 调试模式开关
 * - 最近请求查看
 * - 在线用户统计
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2025-12-07
 */
@RestController
@RequestMapping("/oauth/admin")
@Slf4j
@Tag(name = "OAuth管理", description = "OAuth认证服务管理接口")
public class OAuthAdminController {

    private final AuthServerProperties authServerProperties;
    
    /**
     * 日志系统（可能不存在）
     */
    private final LoggingSystem loggingSystem;

    public OAuthAdminController(AuthServerProperties authServerProperties,
                                @Autowired(required = false) LoggingSystem loggingSystem) {
        this.authServerProperties = authServerProperties;
        this.loggingSystem = loggingSystem;
    }

    /**
     * 最近请求记录（最多保留100条）
     */
    private static final Deque<RequestRecord> RECENT_REQUESTS = new ConcurrentLinkedDeque<>();
    private static final int MAX_RECORDS = 100;

    /**
     * 调试模式状态
     */
    private static volatile boolean debugMode = false;

    /**
     * 请求记录
     */
    public record RequestRecord(
            String time,
            String type,
            String clientIp,
            String username,
            boolean success,
            String message,
            long costMs
    ) {}

    // ==================== 调试模式 ====================

    /**
     * 获取调试模式状态
     */
    @GetMapping("/debug/status")
    @Operation(summary = "获取调试模式状态")
    public ReturnResult<Map<String, Object>> getDebugStatus() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("debugMode", debugMode);
        result.put("oauthLogLevel", getLogLevel("com.chua.starter.oauth"));
        result.put("authInfoLogLevel", getLogLevel("com.chua.starter.oauth.sso.support.information"));
        return ReturnResult.ok(result);
    }

    /**
     * 开启调试模式
     */
    @PostMapping("/debug/enable")
    @Operation(summary = "开启调试模式")
    public ReturnResult<String> enableDebug() {
        debugMode = true;
        setLogLevel("com.chua.starter.oauth", LogLevel.DEBUG);
        setLogLevel("com.chua.starter.oauth.sso", LogLevel.DEBUG);
        setLogLevel("com.chua.starter.oauth.sso.support.information", LogLevel.DEBUG);
        setLogLevel("com.chua.starter.oauth.sso.support.protocol", LogLevel.DEBUG);
        setLogLevel("com.chua.starter.oauth.sso.support.token", LogLevel.DEBUG);
        log.info("【OAuth管理】调试模式已开启");
        return ReturnResult.ok("调试模式已开启");
    }

    /**
     * 关闭调试模式
     */
    @PostMapping("/debug/disable")
    @Operation(summary = "关闭调试模式")
    public ReturnResult<String> disableDebug() {
        debugMode = false;
        setLogLevel("com.chua.starter.oauth", LogLevel.INFO);
        setLogLevel("com.chua.starter.oauth.sso", LogLevel.INFO);
        setLogLevel("com.chua.starter.oauth.sso.support.information", LogLevel.INFO);
        setLogLevel("com.chua.starter.oauth.sso.support.protocol", LogLevel.INFO);
        setLogLevel("com.chua.starter.oauth.sso.support.token", LogLevel.INFO);
        log.info("【OAuth管理】调试模式已关闭");
        return ReturnResult.ok("调试模式已关闭");
    }

    // ==================== 最近请求 ====================

    /**
     * 获取最近请求记录
     */
    @GetMapping("/requests")
    @Operation(summary = "获取最近请求记录")
    public ReturnResult<List<RequestRecord>> getRecentRequests(
            @RequestParam(defaultValue = "20") int limit) {
        List<RequestRecord> result = new ArrayList<>();
        int count = 0;
        for (RequestRecord record : RECENT_REQUESTS) {
            if (count >= limit) break;
            result.add(record);
            count++;
        }
        return ReturnResult.ok(result);
    }

    /**
     * 清空请求记录
     */
    @DeleteMapping("/requests")
    @Operation(summary = "清空请求记录")
    public ReturnResult<String> clearRequests() {
        RECENT_REQUESTS.clear();
        log.info("【OAuth管理】请求记录已清空");
        return ReturnResult.ok("请求记录已清空");
    }

    /**
     * 记录请求（供内部调用）
     */
    public static void recordRequest(String type, String clientIp, String username, 
                                     boolean success, String message, long costMs) {
        RequestRecord record = new RequestRecord(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")),
                type,
                clientIp,
                username,
                success,
                message,
                costMs
        );
        RECENT_REQUESTS.addFirst(record);
        // 保持最大记录数
        while (RECENT_REQUESTS.size() > MAX_RECORDS) {
            RECENT_REQUESTS.removeLast();
        }
    }

    /**
     * 是否为调试模式
     */
    public static boolean isDebugMode() {
        return debugMode;
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
        status.put("signVerify", authServerProperties.isSignVerify());
        status.put("renew", authServerProperties.isRenew());
        status.put("onlineMode", authServerProperties.getOnline().name());
        status.put("maxOnlineCount", authServerProperties.getMaxOnlineCount());
        status.put("tokenExpire", authServerProperties.getExpire());
        status.put("refreshTokenExpire", authServerProperties.getRefreshExpire());
        status.put("recentRequestCount", RECENT_REQUESTS.size());
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
