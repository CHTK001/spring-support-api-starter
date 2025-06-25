package com.chua.starter.monitor.starter.service.report.impl;

import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerMetrics;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerMetricsService;
import com.chua.starter.monitor.starter.service.report.ReportHandler;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.json.Json;
import com.chua.starter.monitor.starter.message.ServerWebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * API上报处理器
 * 用于处理远程服务器通过API接口上报的数据
 * 
 * @author CH
 * @since 2024/12/25
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiReportHandler implements ReportHandler {

    private final MonitorSysGenServerMetricsService metricsService;
    private final SocketSessionTemplate socketSessionTemplate;

    @Override
    public String getReportType() {
        return "API";
    }

    @Override
    public boolean supports(MonitorSysGenServer server) {
        return "API".equals(server.getMonitorSysGenServerDataReportMethod())
                && server.getMonitorSysGenServerIsLocal() != null 
                && server.getMonitorSysGenServerIsLocal() == 0;
    }

    @Override
    public ReturnResult<MonitorSysGenServerMetrics> handleReport(MonitorSysGenServer server, Map<String, Object> reportData) {
        try {
            log.debug("开始处理API上报数据: serverId={}", server.getMonitorSysGenServerId());
            
            if (reportData == null || reportData.isEmpty()) {
                return ReturnResult.error("上报数据不能为空");
            }
            
            // 解析上报数据并转换为指标对象
            MonitorSysGenServerMetrics metrics = parseReportData(server, reportData);
            
            // 保存到数据库
            metricsService.save(metrics);
            
            // 通过WebSocket推送到前端
            broadcastServerMetrics(server.getMonitorSysGenServerId(), metrics);
            
            log.debug("API上报数据处理完成: serverId={}", server.getMonitorSysGenServerId());
            return ReturnResult.success(metrics);
            
        } catch (Exception e) {
            log.error("处理API上报数据失败: serverId={}", server.getMonitorSysGenServerId(), e);
            return ReturnResult.error("API上报数据处理失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> startDataCollection(MonitorSysGenServer server) {
        // API上报不需要主动启动数据收集任务，由客户端主动上报
        log.info("API上报方式不需要启动数据收集任务: serverId={}", server.getMonitorSysGenServerId());
        return ReturnResult.success(true);
    }

    @Override
    public ReturnResult<Boolean> stopDataCollection(Integer serverId) {
        // API上报不需要停止数据收集任务
        log.info("API上报方式不需要停止数据收集任务: serverId={}", serverId);
        return ReturnResult.success(true);
    }

    @Override
    public ReturnResult<Boolean> validateServerConfig(MonitorSysGenServer server) {
        try {
            // 检查是否为远程服务器
            if (server.getMonitorSysGenServerIsLocal() == null || server.getMonitorSysGenServerIsLocal() != 0) {
                return ReturnResult.error("本地服务器不支持API上报方式");
            }
            
            // 检查服务器地址
            if (!StringUtils.hasText(server.getMonitorSysGenServerHost())) {
                return ReturnResult.error("服务器地址不能为空");
            }
            
            // 检查端口
            if (server.getMonitorSysGenServerPort() == null || server.getMonitorSysGenServerPort() <= 0) {
                return ReturnResult.error("服务器端口配置无效");
            }
            
            return ReturnResult.success(true);
            
        } catch (Exception e) {
            log.error("验证API上报服务器配置失败: serverId={}", server.getMonitorSysGenServerId(), e);
            return ReturnResult.error("验证配置失败: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "API上报 - 远程服务器通过API接口主动上报数据";
    }

    @Override
    public int getPriority() {
        return 20; // API上报优先级中等
    }

    @Override
    public boolean requiresScheduledTask() {
        return false; // API上报不需要定时任务
    }

    @Override
    public int getDefaultMonitorInterval() {
        return 60; // API上报建议间隔较长
    }

    /**
     * 解析上报数据并转换为指标对象
     * 
     * @param server 服务器配置
     * @param reportData 上报数据
     * @return 指标对象
     */
    private MonitorSysGenServerMetrics parseReportData(MonitorSysGenServer server, Map<String, Object> reportData) {
        MonitorSysGenServerMetrics metrics = new MonitorSysGenServerMetrics();
        
        try {
            // 设置基本信息
            metrics.setMonitorSysGenServerId(server.getMonitorSysGenServerId());
            metrics.setMonitorSysGenServerMetricsCollectTime(LocalDateTime.now());
            
            // 解析CPU使用率
            Object cpuUsage = reportData.get("cpuUsage");
            if (cpuUsage != null) {
                metrics.setMonitorSysGenServerMetricsCpuUsage(parseBigDecimal(cpuUsage));
            }
            
            // 解析内存信息
            Object memoryTotal = reportData.get("memoryTotal");
            if (memoryTotal != null) {
                metrics.setMonitorSysGenServerMetricsMemoryTotal(parseLong(memoryTotal));
            }
            
            Object memoryUsed = reportData.get("memoryUsed");
            if (memoryUsed != null) {
                metrics.setMonitorSysGenServerMetricsMemoryUsed(parseLong(memoryUsed));
            }
            
            Object memoryUsage = reportData.get("memoryUsage");
            if (memoryUsage != null) {
                metrics.setMonitorSysGenServerMetricsMemoryUsage(parseBigDecimal(memoryUsage));
            }
            
            // 解析磁盘使用率
            Object diskUsage = reportData.get("diskUsage");
            if (diskUsage != null) {
                metrics.setMonitorSysGenServerMetricsDiskUsage(parseBigDecimal(diskUsage));
            }

            // 解析网络入流量
            Object networkIn = reportData.get("networkIn");
            if (networkIn != null) {
                metrics.setMonitorSysGenServerMetricsNetworkIn(parseLong(networkIn));
            }

            // 解析网络出流量
            Object networkOut = reportData.get("networkOut");
            if (networkOut != null) {
                metrics.setMonitorSysGenServerMetricsNetworkOut(parseLong(networkOut));
            }

            // 解析系统负载
            Object loadAverage = reportData.get("loadAverage");
            if (loadAverage != null) {
                // 如果是字符串格式的负载平均值，解析第一个值作为1分钟负载
                String loadStr = loadAverage.toString();
                if (loadStr.contains(" ")) {
                    String[] loads = loadStr.split("\\s+");
                    if (loads.length >= 1) {
                        metrics.setMonitorSysGenServerMetricsCpuLoad1m(parseBigDecimal(loads[0]));
                    }
                    if (loads.length >= 2) {
                        metrics.setMonitorSysGenServerMetricsCpuLoad5m(parseBigDecimal(loads[1]));
                    }
                    if (loads.length >= 3) {
                        metrics.setMonitorSysGenServerMetricsCpuLoad15m(parseBigDecimal(loads[2]));
                    }
                } else {
                    // 单个值作为1分钟负载
                    metrics.setMonitorSysGenServerMetricsCpuLoad1m(parseBigDecimal(loadAverage));
                }
            }

            // 解析单独的负载值
            Object load1m = reportData.get("load1m");
            if (load1m != null) {
                metrics.setMonitorSysGenServerMetricsCpuLoad1m(parseBigDecimal(load1m));
            }

            Object load5m = reportData.get("load5m");
            if (load5m != null) {
                metrics.setMonitorSysGenServerMetricsCpuLoad5m(parseBigDecimal(load5m));
            }

            Object load15m = reportData.get("load15m");
            if (load15m != null) {
                metrics.setMonitorSysGenServerMetricsCpuLoad15m(parseBigDecimal(load15m));
            }

            // 解析运行时间
            Object uptime = reportData.get("uptime");
            if (uptime != null) {
                metrics.setMonitorSysGenServerMetricsUptime(parseLong(uptime));
            }

            // 解析进程数量
            Object processCount = reportData.get("processCount");
            if (processCount != null) {
                metrics.setMonitorSysGenServerMetricsProcessCount(parseInteger(processCount));
            }

            log.debug("API上报数据解析完成: CPU={}%, Memory={}%, Load={}",
                    metrics.getMonitorSysGenServerMetricsCpuUsage(),
                    metrics.getMonitorSysGenServerMetricsMemoryUsage(),
                    metrics.getMonitorSysGenServerMetricsCpuLoad1m());
            
        } catch (Exception e) {
            log.error("解析API上报数据失败", e);
            throw new RuntimeException("解析API上报数据失败", e);
        }
        
        return metrics;
    }

    /**
     * 安全解析BigDecimal值
     */
    private BigDecimal parseBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            log.warn("无法解析BigDecimal值: {}", value);
            return null;
        }
    }

    /**
     * 安全解析Long值
     */
    private Long parseLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            log.warn("无法解析Long值: {}", value);
            return null;
        }
    }

    /**
     * 安全解析Integer值
     */
    private Integer parseInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            log.warn("无法解析Integer值: {}", value);
            return null;
        }
    }

    /**
     * 广播服务器指标数据到前端
     */
    private void broadcastServerMetrics(Integer serverId, MonitorSysGenServerMetrics metrics) {
        try {
            ServerWebSocketMessage message = ServerWebSocketMessage.createServerMetricsMessage(
                    serverId, "Server-" + serverId, metrics);
            String messageJson = Json.toJson(message);
            socketSessionTemplate.send("gen/server", messageJson);
            log.debug("服务器指标数据已广播: serverId={}", serverId);
        } catch (Exception e) {
            log.error("广播服务器指标数据失败: serverId={}", serverId, e);
        }
    }
}
