package com.chua.starter.monitor.starter.service;

import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.starter.pojo.ServerMetricsData;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Prometheus数据收集服务
 * 用于从Prometheus服务器收集监控指标数据
 *
 * @author CH
 * @since 2024/12/30
 */
@Slf4j
@Service
public class PrometheusDataCollectorService {

    private final RestTemplate restTemplate;
    private final Map<Integer, PrometheusConnection> connectionCache = new ConcurrentHashMap<>();

    public PrometheusDataCollectorService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Prometheus连接信息
     */
    @Data
    public static class PrometheusConnection {
        private String baseUrl;
        private String queryPath;
        private boolean authEnabled;
        private String username;
        private String password;
        private int timeout;
        private Map<String, String> labels;
        private LocalDateTime lastConnectTime;
        private boolean connected;
        private String errorMessage;

        public String getFullUrl() {
            return baseUrl + queryPath;
        }
    }

    /**
     * 从Prometheus收集服务器指标数据
     *
     * @param serverId 服务器ID
     * @param setting  服务器配置
     * @return 指标数据
     */
    public ServerMetricsData collectMetrics(Integer serverId, MonitorSysGenServerSetting setting) {
        try {
            PrometheusConnection connection = getOrCreateConnection(serverId, setting);
            if (!connection.isConnected()) {
                log.warn("Prometheus连接未建立，服务器ID: {}", serverId);
                return null;
            }

            ServerMetricsData metricsData = new ServerMetricsData();
            metricsData.setServerId(serverId);
            metricsData.setCollectTime(LocalDateTime.now());

            // 收集各项指标
            collectCpuMetrics(connection, metricsData);
            collectMemoryMetrics(connection, metricsData);
            collectDiskMetrics(connection, metricsData);
            collectNetworkMetrics(connection, metricsData);
            collectSystemMetrics(connection, metricsData);

            return metricsData;
        } catch (Exception e) {
            log.error("从Prometheus收集指标数据失败，服务器ID: {}", serverId, e);
            return null;
        }
    }

    /**
     * 获取或创建Prometheus连接
     */
    private PrometheusConnection getOrCreateConnection(Integer serverId, MonitorSysGenServerSetting setting) {
        PrometheusConnection connection = connectionCache.get(serverId);
        
        if (connection == null || needsReconnection(connection, setting)) {
            connection = createConnection(setting);
            connectionCache.put(serverId, connection);
        }
        
        return connection;
    }

    /**
     * 创建Prometheus连接
     */
    private PrometheusConnection createConnection(MonitorSysGenServerSetting setting) {
        PrometheusConnection connection = new PrometheusConnection();
        
        connection.setBaseUrl(setting.getMonitorSysGenServerSettingPrometheusUrl());
        connection.setQueryPath(setting.getMonitorSysGenServerSettingPrometheusQueryPath());
        connection.setAuthEnabled(setting.getMonitorSysGenServerSettingPrometheusAuthEnabled() == 1);
        connection.setUsername(setting.getMonitorSysGenServerSettingPrometheusUsername());
        connection.setPassword(setting.getMonitorSysGenServerSettingPrometheusPassword());
        connection.setTimeout(setting.getMonitorSysGenServerSettingPrometheusTimeout());
        connection.setLabels(parseLabels(setting.getMonitorSysGenServerSettingPrometheusLabels()));
        
        // 测试连接
        boolean connected = testConnection(connection);
        connection.setConnected(connected);
        connection.setLastConnectTime(LocalDateTime.now());
        
        if (connected) {
            log.info("Prometheus连接建立成功: {}", connection.getBaseUrl());
        } else {
            log.warn("Prometheus连接建立失败: {}", connection.getBaseUrl());
        }
        
        return connection;
    }

    /**
     * 测试Prometheus连接
     */
    public boolean testConnection(PrometheusConnection connection) {
        try {
            String testQuery = "up";
            String url = connection.getFullUrl() + "?query=" + testQuery;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            if (connection.isAuthEnabled() && StringUtils.hasText(connection.getUsername())) {
                String auth = connection.getUsername() + ":" + connection.getPassword();
                String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
                headers.set("Authorization", "Basic " + encodedAuth);
            }
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.error("测试Prometheus连接失败", e);
            return false;
        }
    }

    /**
     * 收集CPU指标
     */
    private void collectCpuMetrics(PrometheusConnection connection, ServerMetricsData metricsData) {
        try {
            // CPU使用率查询
            String cpuQuery = "100 - (avg(rate(node_cpu_seconds_total{mode=\"idle\"}[5m])) * 100)";
            Double cpuUsage = executeQuery(connection, cpuQuery);
            if (cpuUsage != null) {
                metricsData.setCpuUsage(cpuUsage);
            }

            // CPU核心数查询
            String coresQuery = "count(count(node_cpu_seconds_total) by (cpu))";
            Double cores = executeQuery(connection, coresQuery);
            if (cores != null) {
                metricsData.setCpuCores(cores.intValue());
            }

            // 系统负载查询
            String load1Query = "node_load1";
            Double load1 = executeQuery(connection, load1Query);
            if (load1 != null) {
                metricsData.setLoad1m(load1);
            }

            String load5Query = "node_load5";
            Double load5 = executeQuery(connection, load5Query);
            if (load5 != null) {
                metricsData.setLoad5m(load5);
            }

            String load15Query = "node_load15";
            Double load15 = executeQuery(connection, load15Query);
            if (load15 != null) {
                metricsData.setLoad15m(load15);
            }

        } catch (Exception e) {
            log.error("收集CPU指标失败", e);
        }
    }

    /**
     * 收集内存指标
     */
    private void collectMemoryMetrics(PrometheusConnection connection, ServerMetricsData metricsData) {
        try {
            // 总内存
            String totalQuery = "node_memory_MemTotal_bytes";
            Double totalMemory = executeQuery(connection, totalQuery);
            if (totalMemory != null) {
                metricsData.setMemoryTotal(totalMemory.longValue());
            }

            // 可用内存
            String availableQuery = "node_memory_MemAvailable_bytes";
            Double availableMemory = executeQuery(connection, availableQuery);
            if (availableMemory != null) {
                metricsData.setMemoryFree(availableMemory.longValue());
                if (totalMemory != null) {
                    long usedMemory = totalMemory.longValue() - availableMemory.longValue();
                    metricsData.setMemoryUsed(usedMemory);
                    metricsData.setMemoryUsage((double) usedMemory / totalMemory * 100);
                }
            }

        } catch (Exception e) {
            log.error("收集内存指标失败", e);
        }
    }

    /**
     * 收集磁盘指标
     */
    private void collectDiskMetrics(PrometheusConnection connection, ServerMetricsData metricsData) {
        try {
            // 磁盘总空间
            String totalQuery = "sum(node_filesystem_size_bytes{fstype!=\"tmpfs\"})";
            Double totalDisk = executeQuery(connection, totalQuery);
            if (totalDisk != null) {
                metricsData.setDiskTotal(totalDisk.longValue());
            }

            // 磁盘可用空间
            String availableQuery = "sum(node_filesystem_avail_bytes{fstype!=\"tmpfs\"})";
            Double availableDisk = executeQuery(connection, availableQuery);
            if (availableDisk != null) {
                metricsData.setDiskFree(availableDisk.longValue());
                if (totalDisk != null) {
                    long usedDisk = totalDisk.longValue() - availableDisk.longValue();
                    metricsData.setDiskUsed(usedDisk);
                    metricsData.setDiskUsage((double) usedDisk / totalDisk * 100);
                }
            }

        } catch (Exception e) {
            log.error("收集磁盘指标失败", e);
        }
    }

    /**
     * 收集网络指标
     */
    private void collectNetworkMetrics(PrometheusConnection connection, ServerMetricsData metricsData) {
        try {
            // 网络接收字节数
            String rxQuery = "sum(rate(node_network_receive_bytes_total[5m]))";
            Double networkIn = executeQuery(connection, rxQuery);
            if (networkIn != null) {
                metricsData.setNetworkIn(networkIn.longValue());
            }

            // 网络发送字节数
            String txQuery = "sum(rate(node_network_transmit_bytes_total[5m]))";
            Double networkOut = executeQuery(connection, txQuery);
            if (networkOut != null) {
                metricsData.setNetworkOut(networkOut.longValue());
            }

        } catch (Exception e) {
            log.error("收集网络指标失败", e);
        }
    }

    /**
     * 收集系统指标
     */
    private void collectSystemMetrics(PrometheusConnection connection, ServerMetricsData metricsData) {
        try {
            // 系统运行时间
            String uptimeQuery = "node_time_seconds - node_boot_time_seconds";
            Double uptime = executeQuery(connection, uptimeQuery);
            if (uptime != null) {
                metricsData.setUptime(uptime.longValue());
            }

            // 进程数量
            String processQuery = "node_processes_pids";
            Double processCount = executeQuery(connection, processQuery);
            if (processCount != null) {
                metricsData.setProcessCount(processCount.intValue());
            }

        } catch (Exception e) {
            log.error("收集系统指标失败", e);
        }
    }

    /**
     * 执行Prometheus查询
     */
    private Double executeQuery(PrometheusConnection connection, String query) {
        try {
            String url = connection.getFullUrl() + "?query=" + query;
            
            // 添加标签过滤
            if (connection.getLabels() != null && !connection.getLabels().isEmpty()) {
                StringBuilder labelFilter = new StringBuilder();
                for (Map.Entry<String, String> entry : connection.getLabels().entrySet()) {
                    if (labelFilter.length() > 0) {
                        labelFilter.append(",");
                    }
                    labelFilter.append(entry.getKey()).append("=\"").append(entry.getValue()).append("\"");
                }
                query = query.replace("}", "," + labelFilter + "}");
                url = connection.getFullUrl() + "?query=" + query;
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            if (connection.isAuthEnabled() && StringUtils.hasText(connection.getUsername())) {
                String auth = connection.getUsername() + ":" + connection.getPassword();
                String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
                headers.set("Authorization", "Basic " + encodedAuth);
            }
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                Map<String, Object> data = (Map<String, Object>) body.get("data");
                if (data != null) {
                    List<Map<String, Object>> result = (List<Map<String, Object>>) data.get("result");
                    if (result != null && !result.isEmpty()) {
                        Map<String, Object> firstResult = result.get(0);
                        List<Object> value = (List<Object>) firstResult.get("value");
                        if (value != null && value.size() > 1) {
                            return Double.parseDouble(value.get(1).toString());
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("执行Prometheus查询失败: {}", query, e);
        }
        
        return null;
    }

    /**
     * 解析标签字符串
     */
    private Map<String, String> parseLabels(String labelsStr) {
        Map<String, String> labels = new HashMap<>();
        if (StringUtils.hasText(labelsStr)) {
            String[] pairs = labelsStr.split(",");
            for (String pair : pairs) {
                String[] kv = pair.split("=");
                if (kv.length == 2) {
                    labels.put(kv[0].trim(), kv[1].trim());
                }
            }
        }
        return labels;
    }

    /**
     * 检查是否需要重新连接
     */
    private boolean needsReconnection(PrometheusConnection connection, MonitorSysGenServerSetting setting) {
        return !Objects.equals(connection.getBaseUrl(), setting.getMonitorSysGenServerSettingPrometheusUrl()) ||
               !Objects.equals(connection.getQueryPath(), setting.getMonitorSysGenServerSettingPrometheusQueryPath()) ||
               connection.isAuthEnabled() != (setting.getMonitorSysGenServerSettingPrometheusAuthEnabled() == 1) ||
               !Objects.equals(connection.getUsername(), setting.getMonitorSysGenServerSettingPrometheusUsername());
    }

    /**
     * 清除连接缓存
     */
    public void clearConnectionCache(Integer serverId) {
        connectionCache.remove(serverId);
    }

    /**
     * 清除所有连接缓存
     */
    public void clearAllConnectionCache() {
        connectionCache.clear();
    }
}
