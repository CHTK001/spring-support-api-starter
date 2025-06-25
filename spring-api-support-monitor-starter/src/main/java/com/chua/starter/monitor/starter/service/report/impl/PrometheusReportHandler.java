package com.chua.starter.monitor.starter.service.report.impl;

import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerMetrics;
import com.chua.starter.monitor.starter.service.report.ReportHandler;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.common.support.json.Json;
import com.chua.starter.monitor.starter.message.ServerWebSocketMessage;
import com.chua.common.support.lang.code.ReturnResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Prometheus上报处理器
 * 用于定时查询Prometheus数据，向前端下发但不存储到指标表
 * 
 * @author CH
 * @since 2024/12/25
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PrometheusReportHandler implements ReportHandler {

    private final SocketSessionTemplate socketSessionTemplate;
    private final RestTemplate restTemplate = new RestTemplate();
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    private final Map<Integer, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    @Override
    public String getReportType() {
        return "PROMETHEUS";
    }

    @Override
    public boolean supports(MonitorSysGenServer server) {
        return "PROMETHEUS".equals(server.getMonitorSysGenServerDataReportMethod())
                && StringUtils.hasText(server.getMonitorSysGenServerPrometheusHost())
                && server.getMonitorSysGenServerPrometheusPort() != null;
    }

    @Override
    public ReturnResult<MonitorSysGenServerMetrics> handleReport(MonitorSysGenServer server, Map<String, Object> reportData) {
        try {
            log.debug("开始查询Prometheus数据: serverId={}", server.getMonitorSysGenServerId());
            
            // 查询Prometheus数据
            Map<String, Object> prometheusData = queryPrometheusData(server);
            
            // 直接通过WebSocket推送到前端，不存储到数据库
            broadcastPrometheusData(server.getMonitorSysGenServerId(), prometheusData);
            
            log.debug("Prometheus数据查询完成: serverId={}", server.getMonitorSysGenServerId());
            
            // 返回null表示不需要存储到指标表
            return ReturnResult.success(null);
            
        } catch (Exception e) {
            log.error("查询Prometheus数据失败: serverId={}", server.getMonitorSysGenServerId(), e);
            return ReturnResult.error("Prometheus数据查询失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> startDataCollection(MonitorSysGenServer server) {
        try {
            Integer serverId = server.getMonitorSysGenServerId();
            
            // 停止已存在的任务
            stopDataCollection(serverId);
            
            // 获取监控间隔，默认30秒
            long interval = server.getMonitorSysGenServerMonitorInterval() != null ? 
                server.getMonitorSysGenServerMonitorInterval() : getDefaultMonitorInterval();
            
            // 启动定时查询任务
            ScheduledFuture<?> task = scheduler.scheduleAtFixedRate(() -> {
                try {
                    handleReport(server, null);
                } catch (Exception e) {
                    log.error("Prometheus数据查询任务执行失败: serverId={}", serverId, e);
                }
            }, 0, interval, TimeUnit.SECONDS);
            
            scheduledTasks.put(serverId, task);
            log.info("Prometheus数据查询任务启动成功: serverId={}, interval={}s", serverId, interval);
            
            return ReturnResult.success(true);
            
        } catch (Exception e) {
            log.error("启动Prometheus数据查询任务失败: serverId={}", server.getMonitorSysGenServerId(), e);
            return ReturnResult.error("启动Prometheus数据查询失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> stopDataCollection(Integer serverId) {
        try {
            ScheduledFuture<?> task = scheduledTasks.remove(serverId);
            if (task != null && !task.isCancelled()) {
                task.cancel(true);
                log.info("Prometheus数据查询任务已停止: serverId={}", serverId);
            }
            return ReturnResult.success(true);
            
        } catch (Exception e) {
            log.error("停止Prometheus数据查询任务失败: serverId={}", serverId, e);
            return ReturnResult.error("停止Prometheus数据查询失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> validateServerConfig(MonitorSysGenServer server) {
        try {
            // 检查Prometheus服务器地址
            if (!StringUtils.hasText(server.getMonitorSysGenServerPrometheusHost())) {
                return ReturnResult.error("Prometheus服务器地址不能为空");
            }
            
            // 检查Prometheus端口
            if (server.getMonitorSysGenServerPrometheusPort() == null || server.getMonitorSysGenServerPrometheusPort() <= 0) {
                return ReturnResult.error("Prometheus服务器端口配置无效");
            }
            
            // 检查监控间隔
            if (server.getMonitorSysGenServerMonitorInterval() != null && server.getMonitorSysGenServerMonitorInterval() < 10) {
                return ReturnResult.error("Prometheus查询间隔不能小于10秒");
            }
            
            // 测试连接Prometheus服务器
            try {
                String prometheusUrl = buildPrometheusUrl(server, "up");
                ResponseEntity<String> response = restTemplate.getForEntity(prometheusUrl, String.class);
                if (!response.getStatusCode().is2xxSuccessful()) {
                    return ReturnResult.error("无法连接到Prometheus服务器");
                }
            } catch (Exception e) {
                return ReturnResult.error("Prometheus服务器连接测试失败: " + e.getMessage());
            }
            
            return ReturnResult.success(true);
            
        } catch (Exception e) {
            log.error("验证Prometheus服务器配置失败: serverId={}", server.getMonitorSysGenServerId(), e);
            return ReturnResult.error("验证配置失败: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "Prometheus上报 - 定时查询Prometheus数据并推送到前端";
    }

    @Override
    public int getPriority() {
        return 30; // Prometheus上报优先级较低
    }

    @Override
    public boolean requiresScheduledTask() {
        return true;
    }

    @Override
    public int getDefaultMonitorInterval() {
        return 30;
    }

    /**
     * 查询Prometheus数据
     * 
     * @param server 服务器配置
     * @return Prometheus数据
     */
    private Map<String, Object> queryPrometheusData(MonitorSysGenServer server) {
        Map<String, Object> data = new HashMap<>();
        
        try {
            // 查询CPU使用率
            String cpuQuery = "100 - (avg by (instance) (irate(node_cpu_seconds_total{mode=\"idle\"}[5m])) * 100)";
            Map<String, Object> cpuData = queryPrometheusMetric(server, cpuQuery);
            data.put("cpu", cpuData);
            
            // 查询内存使用率
            String memoryQuery = "(1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100";
            Map<String, Object> memoryData = queryPrometheusMetric(server, memoryQuery);
            data.put("memory", memoryData);
            
            // 查询磁盘使用率
            String diskQuery = "100 - ((node_filesystem_avail_bytes{mountpoint=\"/\"} / node_filesystem_size_bytes{mountpoint=\"/\"}) * 100)";
            Map<String, Object> diskData = queryPrometheusMetric(server, diskQuery);
            data.put("disk", diskData);
            
            // 查询网络流量
            String networkQuery = "irate(node_network_receive_bytes_total{device!=\"lo\"}[5m])";
            Map<String, Object> networkData = queryPrometheusMetric(server, networkQuery);
            data.put("network", networkData);
            
            // 查询系统负载
            String loadQuery = "node_load1";
            Map<String, Object> loadData = queryPrometheusMetric(server, loadQuery);
            data.put("load", loadData);
            
            data.put("timestamp", System.currentTimeMillis());
            data.put("collectTime", LocalDateTime.now());
            
            log.debug("Prometheus数据查询完成: serverId={}, dataKeys={}", 
                    server.getMonitorSysGenServerId(), data.keySet());
            
        } catch (Exception e) {
            log.error("查询Prometheus数据失败", e);
            throw new RuntimeException("查询Prometheus数据失败", e);
        }
        
        return data;
    }

    /**
     * 查询单个Prometheus指标
     * 
     * @param server 服务器配置
     * @param query 查询语句
     * @return 查询结果
     */
    private Map<String, Object> queryPrometheusMetric(MonitorSysGenServer server, String query) {
        try {
            String url = buildPrometheusUrl(server, query);
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> body = response.getBody();
                return body;
            } else {
                log.warn("Prometheus查询返回异常状态: {}", response.getStatusCode());
                return new HashMap<>();
            }
            
        } catch (Exception e) {
            log.error("查询Prometheus指标失败: query={}", query, e);
            return new HashMap<>();
        }
    }

    /**
     * 构建Prometheus查询URL
     *
     * @param server 服务器配置
     * @param query 查询语句
     * @return 查询URL
     */
    private String buildPrometheusUrl(MonitorSysGenServer server, String query) {
        return String.format("http://%s:%d/api/v1/query?query=%s",
                server.getMonitorSysGenServerPrometheusHost(),
                server.getMonitorSysGenServerPrometheusPort(),
                query);
    }

    /**
     * 广播Prometheus数据到前端
     */
    private void broadcastPrometheusData(Integer serverId, Map<String, Object> prometheusData) {
        try {
            ServerWebSocketMessage message = ServerWebSocketMessage.createServerMetricsMessage(
                    serverId, "Server-" + serverId, prometheusData);
            String messageJson = Json.toJson(message);
            socketSessionTemplate.send("gen/server", messageJson);
            log.debug("Prometheus数据已广播: serverId={}", serverId);
        } catch (Exception e) {
            log.error("广播Prometheus数据失败: serverId={}", serverId, e);
        }
    }
}
