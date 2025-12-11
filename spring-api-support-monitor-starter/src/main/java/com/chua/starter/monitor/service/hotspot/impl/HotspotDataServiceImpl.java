package com.chua.starter.monitor.service.hotspot.impl;

import com.chua.common.support.json.Json;
import com.chua.starter.monitor.pojo.HotspotReportData;
import com.chua.starter.monitor.service.hotspot.HotspotDataService;
import com.chua.sync.support.client.SyncClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

/**
 * Hotspot 数据处理服务实现
 * 处理 Hotspot Agent 上报的监控数据
 *
 * @author CH
 * @since 2024/12/11
 * @version 4.0.0.34
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HotspotDataServiceImpl implements HotspotDataService {

    /**
     * Hotspot 事件主题前缀
     */
    private static final String HOTSPOT_TOPIC_PREFIX = "hotspot/";

    /**
     * 同步客户端（可选注入）
     */
    @Autowired(required = false)
    private SyncClient syncClient;

    /**
     * 链路追踪数据缓存（按应用名称分组）
     */
    private final Map<String, List<HotspotReportData.TraceData>> traceDataCache = new ConcurrentHashMap<>();

    /**
     * SQL 监控数据缓存
     */
    private final Map<String, List<HotspotReportData.SqlData>> sqlDataCache = new ConcurrentHashMap<>();

    /**
     * 日志数据缓存
     */
    private final Map<String, List<HotspotReportData.LogData>> logDataCache = new ConcurrentHashMap<>();

    /**
     * 异常数据缓存
     */
    private final Map<String, List<HotspotReportData.ExceptionData>> exceptionDataCache = new ConcurrentHashMap<>();

    /**
     * 服务实例数据缓存
     */
    private final Map<String, List<HotspotReportData.ServerData>> serverDataCache = new ConcurrentHashMap<>();

    /**
     * 缓存最大容量
     */
    private static final int MAX_CACHE_SIZE = 1000;

    @Override
    public boolean handleReport(HotspotReportData reportData) {
        if (reportData == null || reportData.getReportType() == null) {
            log.warn("收到无效的 Hotspot 上报数据");
            return false;
        }

        String appName = reportData.getAppName();
        String reportType = reportData.getReportType().toUpperCase();
        Object data = reportData.getReportData();

        log.debug("处理 Hotspot 上报数据: appName={}, type={}", appName, reportType);

        try {
            return switch (reportType) {
                case "TRACE" -> handleTraceReport(appName, data);
                case "SQL" -> handleSqlReport(appName, data);
                case "LOG" -> handleLogReport(appName, data);
                case "EXCEPTION" -> handleExceptionReport(appName, data);
                case "SERVER" -> handleServerReport(appName, data);
                default -> {
                    log.warn("未知的上报类型: {}", reportType);
                    yield false;
                }
            };
        } catch (Exception e) {
            log.error("处理 Hotspot 上报数据失败: appName={}, type={}", appName, reportType, e);
            return false;
        }
    }

    /**
     * 处理链路追踪上报
     */
    private boolean handleTraceReport(String appName, Object data) {
        if (data == null) {
            return false;
        }
        HotspotReportData.TraceData traceData = Json.fromJson(Json.toJson(data), HotspotReportData.TraceData.class);
        return handleTraceData(appName, traceData);
    }

    /**
     * 处理 SQL 上报
     */
    private boolean handleSqlReport(String appName, Object data) {
        if (data == null) {
            return false;
        }
        HotspotReportData.SqlData sqlData = Json.fromJson(Json.toJson(data), HotspotReportData.SqlData.class);
        return handleSqlData(appName, sqlData);
    }

    /**
     * 处理日志上报
     */
    private boolean handleLogReport(String appName, Object data) {
        if (data == null) {
            return false;
        }
        HotspotReportData.LogData logData = Json.fromJson(Json.toJson(data), HotspotReportData.LogData.class);
        return handleLogData(appName, logData);
    }

    /**
     * 处理异常上报
     */
    private boolean handleExceptionReport(String appName, Object data) {
        if (data == null) {
            return false;
        }
        HotspotReportData.ExceptionData exceptionData = Json.fromJson(Json.toJson(data), HotspotReportData.ExceptionData.class);
        return handleExceptionData(appName, exceptionData);
    }

    /**
     * 处理服务实例上报
     */
    private boolean handleServerReport(String appName, Object data) {
        if (data == null) {
            return false;
        }
        HotspotReportData.ServerData serverData = Json.fromJson(Json.toJson(data), HotspotReportData.ServerData.class);
        return handleServerData(appName, serverData);
    }

    @Override
    public boolean handleTraceData(String appName, HotspotReportData.TraceData traceData) {
        if (traceData == null) {
            return false;
        }

        List<HotspotReportData.TraceData> list = traceDataCache.computeIfAbsent(
                appName, k -> new CopyOnWriteArrayList<>());

        // 限制缓存大小
        if (list.size() >= MAX_CACHE_SIZE) {
            list.remove(0);
        }

        list.add(traceData);
        log.debug("链路追踪数据已缓存: appName={}, linkId={}", appName, traceData.getLinkId());
        
        // 推送事件
        publishEvent(appName, "trace", traceData);
        return true;
    }

    @Override
    public boolean handleSqlData(String appName, HotspotReportData.SqlData sqlData) {
        if (sqlData == null) {
            return false;
        }

        List<HotspotReportData.SqlData> list = sqlDataCache.computeIfAbsent(
                appName, k -> new CopyOnWriteArrayList<>());

        if (list.size() >= MAX_CACHE_SIZE) {
            list.remove(0);
        }

        list.add(sqlData);
        log.debug("SQL 监控数据已缓存: appName={}, database={}", appName, sqlData.getDatabase());
        
        // 推送事件
        publishEvent(appName, "sql", sqlData);
        return true;
    }

    @Override
    public boolean handleLogData(String appName, HotspotReportData.LogData logData) {
        if (logData == null) {
            return false;
        }

        List<HotspotReportData.LogData> list = logDataCache.computeIfAbsent(
                appName, k -> new CopyOnWriteArrayList<>());

        if (list.size() >= MAX_CACHE_SIZE) {
            list.remove(0);
        }

        list.add(logData);
        log.debug("日志数据已缓存: appName={}, level={}", appName, logData.getLevel());
        
        // 推送事件
        publishEvent(appName, "log", logData);
        return true;
    }

    @Override
    public boolean handleExceptionData(String appName, HotspotReportData.ExceptionData exceptionData) {
        if (exceptionData == null) {
            return false;
        }

        List<HotspotReportData.ExceptionData> list = exceptionDataCache.computeIfAbsent(
                appName, k -> new CopyOnWriteArrayList<>());

        if (list.size() >= MAX_CACHE_SIZE) {
            list.remove(0);
        }

        list.add(exceptionData);
        log.warn("异常数据已缓存: appName={}, type={}", appName, exceptionData.getExceptionType());
        
        // 推送事件
        publishEvent(appName, "exception", exceptionData);
        return true;
    }

    @Override
    public boolean handleServerData(String appName, HotspotReportData.ServerData serverData) {
        if (serverData == null) {
            return false;
        }

        List<HotspotReportData.ServerData> list = serverDataCache.computeIfAbsent(
                appName, k -> new CopyOnWriteArrayList<>());

        if (list.size() >= MAX_CACHE_SIZE) {
            list.remove(0);
        }

        list.add(serverData);
        log.debug("服务实例数据已缓存: appName={}, serviceName={}", appName, serverData.getName());
        
        // 推送事件
        publishEvent(appName, "server", serverData);
        return true;
    }

    /**
     * 获取链路追踪数据
     *
     * @param appName 应用名称
     * @return 链路追踪数据列表
     */
    public List<HotspotReportData.TraceData> getTraceData(String appName) {
        return traceDataCache.getOrDefault(appName, List.of());
    }

    /**
     * 获取 SQL 监控数据
     *
     * @param appName 应用名称
     * @return SQL 数据列表
     */
    public List<HotspotReportData.SqlData> getSqlData(String appName) {
        return sqlDataCache.getOrDefault(appName, List.of());
    }

    /**
     * 获取日志数据
     *
     * @param appName 应用名称
     * @return 日志数据列表
     */
    public List<HotspotReportData.LogData> getLogData(String appName) {
        return logDataCache.getOrDefault(appName, List.of());
    }

    /**
     * 获取异常数据
     *
     * @param appName 应用名称
     * @return 异常数据列表
     */
    public List<HotspotReportData.ExceptionData> getExceptionData(String appName) {
        return exceptionDataCache.getOrDefault(appName, List.of());
    }

    /**
     * 获取服务实例数据
     *
     * @param appName 应用名称
     * @return 服务实例数据列表
     */
    public List<HotspotReportData.ServerData> getServerData(String appName) {
        return serverDataCache.getOrDefault(appName, List.of());
    }

    /**
     * 获取所有已注册的应用名称
     *
     * @return 应用名称列表
     */
    public List<String> getAllAppNames() {
        return traceDataCache.keySet().stream().toList();
    }

    /**
     * 清除指定应用的缓存
     *
     * @param appName 应用名称
     */
    public void clearCache(String appName) {
        traceDataCache.remove(appName);
        sqlDataCache.remove(appName);
        logDataCache.remove(appName);
        exceptionDataCache.remove(appName);
        serverDataCache.remove(appName);
        log.info("已清除应用 {} 的缓存数据", appName);
    }

    // ==================== 事件推送 ====================

    /**
     * 推送 Hotspot 事件到 SyncClient
     *
     * @param appName   应用名称
     * @param eventType 事件类型（trace/sql/log/exception/server）
     * @param data      事件数据
     */
    private void publishEvent(String appName, String eventType, Object data) {
        if (syncClient == null || !syncClient.isConnected()) {
            log.debug("SyncClient 未连接，跳过事件推送: appName={}, eventType={}", appName, eventType);
            return;
        }

        try {
            String topic = HOTSPOT_TOPIC_PREFIX + eventType;
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("appName", appName);
            eventData.put("eventType", eventType);
            eventData.put("data", data);
            eventData.put("timestamp", System.currentTimeMillis());

            syncClient.publish(topic, eventData);
            log.debug("Hotspot 事件已推送: topic={}, appName={}", topic, appName);
        } catch (Exception e) {
            log.error("推送 Hotspot 事件失败: appName={}, eventType={}", appName, eventType, e);
        }
    }
}
