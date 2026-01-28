package com.chua.starter.monitor.service.data;

import com.chua.starter.common.support.log.RestTemplateTraceIdInterceptor;
import com.chua.starter.monitor.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.pojo.ServerMetricsData;
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
        this.restTemplate = createRestTemplate();
    }

    /**
     * 创建配置好的RestTemplate
     */
    private RestTemplate createRestTemplate() {
        RestTemplate template = new RestTemplate();
        template.setInterceptors(Collections.singletonList(new RestTemplateTraceIdInterceptor()));
        // 设置超时时间
        org.springframework.http.client.HttpComponentsClientHttpRequestFactory factory =
            new org.springframework.http.client.HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(10000); // 10秒连接超时
        factory.setReadTimeout(30000);    // 30秒读取超时
        template.setRequestFactory(factory);

        return template;
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
        if (serverId == null || setting == null) {
            log.warn("参数不能为空: serverId={}, setting={}", serverId, setting);
            return null;
        }

        try {
            PrometheusConnection connection = getOrCreateConnection(serverId, setting);
            if (!connection.isConnected()) {
                log.warn("Prometheus连接未建立，服务器ID: {}, 错误: {}", serverId, connection.getErrorMessage());
                return createDefaultMetrics(serverId);
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

            // 设置服务器状态
            metricsData.setStatus(1); // 在线状态

            // 验证数据完整性
            if (!metricsData.isValid()) {
                log.warn("收集到的指标数据无效: serverId={}", serverId);
                return createDefaultMetrics(serverId);
            }

            log.debug("成功收集Prometheus指标数据: serverId={}", serverId);
            return metricsData;
        } catch (Exception e) {
            log.error("从Prometheus收集指标数据失败，服务器ID: {}", serverId, e);
            return createDefaultMetrics(serverId);
        }
    }

    /**
     * 创建默认指标数据
     */
    private ServerMetricsData createDefaultMetrics(Integer serverId) {
        ServerMetricsData defaultData = ServerMetricsData.createDefault(serverId);
        defaultData.setStatus(0); // 离线状态
        return defaultData;
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

        try {
            // 设置连接参数
            String baseUrl = setting.getMonitorSysGenServerSettingPrometheusUrl();
            String queryPath = setting.getMonitorSysGenServerSettingPrometheusQueryPath();

            // 验证必要参数
            if (!org.springframework.util.StringUtils.hasText(baseUrl)) {
                connection.setErrorMessage("Prometheus服务器地址不能为空");
                connection.setConnected(false);
                return connection;
            }

            if (!org.springframework.util.StringUtils.hasText(queryPath)) {
                queryPath = "/api/v1/query"; // 默认查询路径
            }

            connection.setBaseUrl(baseUrl);
            connection.setQueryPath(queryPath);
            connection.setAuthEnabled(setting.getMonitorSysGenServerSettingPrometheusAuthEnabled() == 1);
            connection.setUsername(setting.getMonitorSysGenServerSettingPrometheusUsername());
            connection.setPassword(setting.getMonitorSysGenServerSettingPrometheusPassword());
            connection.setTimeout(setting.getMonitorSysGenServerSettingPrometheusTimeout() != null ?
                setting.getMonitorSysGenServerSettingPrometheusTimeout() : 30);
            connection.setLabels(parseLabels(setting.getMonitorSysGenServerSettingPrometheusLabels()));

            // 测试连接
            boolean connected = testConnection(connection);
            connection.setConnected(connected);
            connection.setLastConnectTime(LocalDateTime.now());

            if (connected) {
                log.info("Prometheus连接建立成功: {}", connection.getBaseUrl());
                connection.setErrorMessage(null);
            } else {
                log.warn("Prometheus连接建立失败: {}, 错误: {}", connection.getBaseUrl(), connection.getErrorMessage());
            }

        } catch (Exception e) {
            log.error("创建Prometheus连接失败", e);
            connection.setConnected(false);
            connection.setErrorMessage("创建连接异常: " + e.getMessage());
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
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, HttpMethod.GET, entity,
                new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
            );

            boolean isSuccess = response.getStatusCode() == HttpStatus.OK;
            if (!isSuccess) {
                connection.setErrorMessage("HTTP状态码: " + response.getStatusCode());
            }
            return isSuccess;
        } catch (Exception e) {
            String errorMsg;
            if (e.getCause() instanceof java.net.ConnectException) {
                errorMsg = "连接被拒绝: " + e.getMessage();
                log.warn("Prometheus连接测试失败: {}, {}", connection.getBaseUrl(), errorMsg);
            } else if (e.getCause() instanceof java.net.SocketTimeoutException) {
                errorMsg = "连接超时: " + e.getMessage();
                log.warn("Prometheus连接测试失败: {}, {}", connection.getBaseUrl(), errorMsg);
            } else {
                errorMsg = "连接异常: " + e.getMessage();
                log.error("测试Prometheus连接失败: {}, {}", connection.getBaseUrl(), errorMsg, e);
            }
            connection.setErrorMessage(errorMsg);
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
                metricsData.setNetworkInBytes(networkIn);
            }

            // 网络发送字节数
            String txQuery = "sum(rate(node_network_transmit_bytes_total[5m]))";
            Double networkOut = executeQuery(connection, txQuery);
            if (networkOut != null) {
                metricsData.setNetworkOutBytes(networkOut);
            }

            // 网络接收包数
            String rxPacketsQuery = "sum(rate(node_network_receive_packets_total[5m]))";
            Double networkInPackets = executeQuery(connection, rxPacketsQuery);
            if (networkInPackets != null) {
                metricsData.setNetworkInPackets(networkInPackets);
            }

            // 网络发送包数
            String txPacketsQuery = "sum(rate(node_network_transmit_packets_total[5m]))";
            Double networkOutPackets = executeQuery(connection, txPacketsQuery);
            if (networkOutPackets != null) {
                metricsData.setNetworkOutPackets(networkOutPackets);
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
    @SuppressWarnings("unchecked")
    private Double executeQuery(PrometheusConnection connection, String query) {
        try {
            String url = connection.getFullUrl() + "?query=" +
                java.net.URLEncoder.encode(query, "UTF-8");

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
                url = connection.getFullUrl() + "?query=" +
                    java.net.URLEncoder.encode(query, "UTF-8");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            if (connection.isAuthEnabled() && StringUtils.hasText(connection.getUsername())) {
                String auth = connection.getUsername() + ":" + connection.getPassword();
                String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
                headers.set("Authorization", "Basic " + encodedAuth);
            }

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, HttpMethod.GET, entity,
                new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                Object dataObj = body.get("data");
                if (dataObj instanceof Map) {
                    Map<String, Object> data = (Map<String, Object>) dataObj;
                    Object resultObj = data.get("result");
                    if (resultObj instanceof List) {
                        List<Map<String, Object>> result = (List<Map<String, Object>>) resultObj;
                        if (!result.isEmpty()) {
                            Map<String, Object> firstResult = result.get(0);
                            Object valueObj = firstResult.get("value");
                            if (valueObj instanceof List) {
                                List<Object> value = (List<Object>) valueObj;
                                if (value.size() > 1) {
                                    return Double.parseDouble(value.get(1).toString());
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("执行Prometheus查询失败: query={}", query, e);
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

    /**
     * 测试Prometheus连接（供外部调用）
     *
     * @param serverId 服务器ID
     * @param setting  服务器配置
     * @return 连接测试结果
     */
    public boolean testPrometheusConnection(Integer serverId, MonitorSysGenServerSetting setting) {
        try {
            PrometheusConnection connection = createConnection(setting);
            boolean result = connection.isConnected();

            if (result) {
                // 如果连接成功，缓存连接
                connectionCache.put(serverId, connection);
                log.info("Prometheus连接测试成功: serverId={}, url={}", serverId, connection.getBaseUrl());
            } else {
                log.warn("Prometheus连接测试失败: serverId={}, url={}, error={}",
                    serverId, connection.getBaseUrl(), connection.getErrorMessage());
            }

            return result;
        } catch (Exception e) {
            log.error("Prometheus连接测试异常: serverId={}", serverId, e);
            return false;
        }
    }

    /**
     * 获取连接状态信息
     *
     * @param serverId 服务器ID
     * @return 连接状态信息
     */
    public Map<String, Object> getConnectionStatus(Integer serverId) {
        Map<String, Object> status = new HashMap<>();
        PrometheusConnection connection = connectionCache.get(serverId);

        if (connection != null) {
            status.put("connected", connection.isConnected());
            status.put("baseUrl", connection.getBaseUrl());
            status.put("lastConnectTime", connection.getLastConnectTime());
            status.put("errorMessage", connection.getErrorMessage());
        } else {
            status.put("connected", false);
            status.put("errorMessage", "连接未建立");
        }

        return status;
    }
}
