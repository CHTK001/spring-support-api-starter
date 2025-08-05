package com.chua.starter.monitor.service;

import com.chua.starter.monitor.starter.config.MetricsThresholdConfig;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerMetrics;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 服务器健康评分服务
 * 基于多个指标计算服务器的综合健康评分
 *
 * @author CH
 * @since 2024/12/30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServerHealthScoreService {

    private final MetricsThresholdConfig thresholdConfig;

    /**
     * 健康评分结果
     */
    @Data
    public static class HealthScoreResult {
        private int totalScore;           // 总分（0-100）
        private String healthLevel;       // 健康等级：excellent, good, warning, critical
        private String healthDescription; // 健康描述
        private Map<String, MetricScore> metricScores; // 各指标得分
        private String[] recommendations; // 优化建议
        private LocalDateTime calculateTime; // 计算时间

        public HealthScoreResult() {
            this.metricScores = new HashMap<>();
            this.calculateTime = LocalDateTime.now();
        }
    }

    /**
     * 单项指标评分
     */
    @Data
    public static class MetricScore {
        private String metricName;    // 指标名称
        private double value;         // 当前值
        private int score;           // 得分（0-100）
        private double weight;       // 权重
        private String status;       // 状态：excellent, good, warning, critical
        private String description;  // 描述
    }

    /**
     * 指标权重配置
     */
    private static final Map<String, Double> METRIC_WEIGHTS = Map.of(
        "cpu", 0.25,      // CPU使用率权重25%
        "memory", 0.25,   // 内存使用率权重25%
        "disk", 0.20,     // 磁盘使用率权重20%
        "network", 0.15,  // 网络使用率权重15%
        "response", 0.10, // 响应时间权重10%
        "uptime", 0.05    // 运行时间权重5%
    );

    /**
     * 计算服务器健康评分
     *
     * @param metrics 服务器指标数据
     * @return 健康评分结果
     */
    public HealthScoreResult calculateHealthScore(MonitorSysGenServerMetrics metrics) {
        HealthScoreResult result = new HealthScoreResult();
        
        try {
            // 计算各项指标得分
            calculateCpuScore(metrics, result);
            calculateMemoryScore(metrics, result);
            calculateDiskScore(metrics, result);
            calculateNetworkScore(metrics, result);
            calculateResponseScore(metrics, result);
            calculateUptimeScore(metrics, result);
            
            // 计算总分
            calculateTotalScore(result);
            
            // 确定健康等级和描述
            determineHealthLevel(result);
            
            // 生成优化建议
            generateRecommendations(result);
            
            log.debug("服务器 {} 健康评分计算完成: 总分 {}, 等级 {}", 
                    metrics.getMonitorSysGenServerId(), result.getTotalScore(), result.getHealthLevel());
            
        } catch (Exception e) {
            log.error("计算服务器健康评分失败: {}", e.getMessage(), e);
            // 返回默认评分
            result.setTotalScore(0);
            result.setHealthLevel("unknown");
            result.setHealthDescription("评分计算失败");
        }
        
        return result;
    }

    /**
     * 计算CPU得分
     */
    private void calculateCpuScore(MonitorSysGenServerMetrics metrics, HealthScoreResult result) {
        MetricScore cpuScore = new MetricScore();
        cpuScore.setMetricName("CPU使用率");
        cpuScore.setWeight(METRIC_WEIGHTS.get("cpu"));
        
        BigDecimal cpuUsage = metrics.getMonitorSysGenServerMetricsCpuUsage();
        if (cpuUsage != null) {
            double value = cpuUsage.doubleValue();
            cpuScore.setValue(value);
            
            MetricsThresholdConfig.ThresholdLevel threshold = thresholdConfig.getCpu();
            cpuScore.setScore(calculateMetricScore(value, threshold));
            cpuScore.setStatus(threshold.getColorLevel(value));
            
            if (value < threshold.getNormal()) {
                cpuScore.setDescription("CPU使用率正常");
            } else if (value < threshold.getWarning()) {
                cpuScore.setDescription("CPU使用率偏高，建议关注");
            } else if (value < threshold.getCritical()) {
                cpuScore.setDescription("CPU使用率过高，需要优化");
            } else {
                cpuScore.setDescription("CPU使用率严重过高，急需处理");
            }
        } else {
            cpuScore.setValue(0);
            cpuScore.setScore(0);
            cpuScore.setStatus("unknown");
            cpuScore.setDescription("CPU数据不可用");
        }
        
        result.getMetricScores().put("cpu", cpuScore);
    }

    /**
     * 计算内存得分
     */
    private void calculateMemoryScore(MonitorSysGenServerMetrics metrics, HealthScoreResult result) {
        MetricScore memoryScore = new MetricScore();
        memoryScore.setMetricName("内存使用率");
        memoryScore.setWeight(METRIC_WEIGHTS.get("memory"));
        
        BigDecimal memoryUsage = metrics.getMonitorSysGenServerMetricsMemoryUsage();
        if (memoryUsage != null) {
            double value = memoryUsage.doubleValue();
            memoryScore.setValue(value);
            
            MetricsThresholdConfig.ThresholdLevel threshold = thresholdConfig.getMemory();
            memoryScore.setScore(calculateMetricScore(value, threshold));
            memoryScore.setStatus(threshold.getColorLevel(value));
            
            if (value < threshold.getNormal()) {
                memoryScore.setDescription("内存使用率正常");
            } else if (value < threshold.getWarning()) {
                memoryScore.setDescription("内存使用率偏高，建议关注");
            } else if (value < threshold.getCritical()) {
                memoryScore.setDescription("内存使用率过高，可能影响性能");
            } else {
                memoryScore.setDescription("内存使用率严重过高，系统可能不稳定");
            }
        } else {
            memoryScore.setValue(0);
            memoryScore.setScore(0);
            memoryScore.setStatus("unknown");
            memoryScore.setDescription("内存数据不可用");
        }
        
        result.getMetricScores().put("memory", memoryScore);
    }

    /**
     * 计算磁盘得分
     */
    private void calculateDiskScore(MonitorSysGenServerMetrics metrics, HealthScoreResult result) {
        MetricScore diskScore = new MetricScore();
        diskScore.setMetricName("磁盘使用率");
        diskScore.setWeight(METRIC_WEIGHTS.get("disk"));
        
        BigDecimal diskUsage = metrics.getMonitorSysGenServerMetricsDiskUsage();
        if (diskUsage != null) {
            double value = diskUsage.doubleValue();
            diskScore.setValue(value);
            
            MetricsThresholdConfig.ThresholdLevel threshold = thresholdConfig.getDisk();
            diskScore.setScore(calculateMetricScore(value, threshold));
            diskScore.setStatus(threshold.getColorLevel(value));
            
            if (value < threshold.getNormal()) {
                diskScore.setDescription("磁盘空间充足");
            } else if (value < threshold.getWarning()) {
                diskScore.setDescription("磁盘空间偏紧，建议清理");
            } else if (value < threshold.getCritical()) {
                diskScore.setDescription("磁盘空间不足，需要扩容");
            } else {
                diskScore.setDescription("磁盘空间严重不足，系统可能无法正常运行");
            }
        } else {
            diskScore.setValue(0);
            diskScore.setScore(0);
            diskScore.setStatus("unknown");
            diskScore.setDescription("磁盘数据不可用");
        }
        
        result.getMetricScores().put("disk", diskScore);
    }

    /**
     * 计算网络得分
     */
    private void calculateNetworkScore(MonitorSysGenServerMetrics metrics, HealthScoreResult result) {
        MetricScore networkScore = new MetricScore();
        networkScore.setMetricName("网络状态");
        networkScore.setWeight(METRIC_WEIGHTS.get("network"));
        
        // 网络评分基于网络连通性和流量状态
        Long networkIn = metrics.getMonitorSysGenServerMetricsNetworkIn();
        Long networkOut = metrics.getMonitorSysGenServerMetricsNetworkOut();
        
        if (networkIn != null && networkOut != null) {
            // 简单的网络健康评估：如果有网络流量则认为网络正常
            double value = (networkIn > 0 || networkOut > 0) ? 10.0 : 0.0; // 假设值
            networkScore.setValue(value);
            networkScore.setScore(networkIn >= 0 && networkOut >= 0 ? 100 : 0);
            networkScore.setStatus("normal");
            networkScore.setDescription("网络连接正常");
        } else {
            networkScore.setValue(0);
            networkScore.setScore(0);
            networkScore.setStatus("unknown");
            networkScore.setDescription("网络数据不可用");
        }
        
        result.getMetricScores().put("network", networkScore);
    }

    /**
     * 计算响应时间得分
     */
    private void calculateResponseScore(MonitorSysGenServerMetrics metrics, HealthScoreResult result) {
        MetricScore responseScore = new MetricScore();
        responseScore.setMetricName("响应时间");
        responseScore.setWeight(METRIC_WEIGHTS.get("response"));
        
        Long responseTime = metrics.getMonitorSysGenServerMetricsResponseTime();
        if (responseTime != null) {
            double value = responseTime.doubleValue();
            responseScore.setValue(value);
            
            // 响应时间评分：<100ms=100分，100-500ms=80分，500-1000ms=60分，>1000ms=40分
            int score;
            String status;
            String description;
            
            if (value < 100) {
                score = 100;
                status = "excellent";
                description = "响应时间优秀";
            } else if (value < 500) {
                score = 80;
                status = "good";
                description = "响应时间良好";
            } else if (value < 1000) {
                score = 60;
                status = "warning";
                description = "响应时间偏慢";
            } else {
                score = 40;
                status = "critical";
                description = "响应时间过慢";
            }
            
            responseScore.setScore(score);
            responseScore.setStatus(status);
            responseScore.setDescription(description);
        } else {
            responseScore.setValue(0);
            responseScore.setScore(0);
            responseScore.setStatus("unknown");
            responseScore.setDescription("响应时间数据不可用");
        }
        
        result.getMetricScores().put("response", responseScore);
    }

    /**
     * 计算运行时间得分
     */
    private void calculateUptimeScore(MonitorSysGenServerMetrics metrics, HealthScoreResult result) {
        MetricScore uptimeScore = new MetricScore();
        uptimeScore.setMetricName("运行时间");
        uptimeScore.setWeight(METRIC_WEIGHTS.get("uptime"));
        
        Long uptime = metrics.getMonitorSysGenServerMetricsUptime();
        if (uptime != null) {
            double value = uptime.doubleValue();
            uptimeScore.setValue(value);
            
            // 运行时间评分：>30天=100分，7-30天=80分，1-7天=60分，<1天=40分
            int score;
            String status = "good";
            String description;
            
            long days = (long) (value / 86400L);
            
            if (days >= 30) {
                score = 100;
                description = "系统运行稳定";
            } else if (days >= 7) {
                score = 80;
                description = "系统运行良好";
            } else if (days >= 1) {
                score = 60;
                description = "系统最近重启";
            } else {
                score = 40;
                description = "系统刚刚启动";
            }
            
            uptimeScore.setScore(score);
            uptimeScore.setStatus(status);
            uptimeScore.setDescription(description);
        } else {
            uptimeScore.setValue(0);
            uptimeScore.setScore(50); // 默认中等分数
            uptimeScore.setStatus("unknown");
            uptimeScore.setDescription("运行时间数据不可用");
        }
        
        result.getMetricScores().put("uptime", uptimeScore);
    }

    /**
     * 根据阈值计算指标得分
     */
    private int calculateMetricScore(double value, MetricsThresholdConfig.ThresholdLevel threshold) {
        if (value < threshold.getNormal()) {
            return 100; // 优秀
        } else if (value < threshold.getWarning()) {
            // 线性插值：normal到warning之间，分数从100降到80
            double ratio = (value - threshold.getNormal()) / (threshold.getWarning() - threshold.getNormal());
            return (int) (100 - ratio * 20);
        } else if (value < threshold.getCritical()) {
            // 线性插值：warning到critical之间，分数从80降到60
            double ratio = (value - threshold.getWarning()) / (threshold.getCritical() - threshold.getWarning());
            return (int) (80 - ratio * 20);
        } else {
            // 超过critical阈值，分数为40-60之间，根据超出程度决定
            double excessRatio = Math.min((value - threshold.getCritical()) / threshold.getCritical(), 1.0);
            return (int) (60 - excessRatio * 20);
        }
    }

    /**
     * 计算总分
     */
    private void calculateTotalScore(HealthScoreResult result) {
        double totalScore = 0.0;
        double totalWeight = 0.0;
        
        for (MetricScore metricScore : result.getMetricScores().values()) {
            totalScore += metricScore.getScore() * metricScore.getWeight();
            totalWeight += metricScore.getWeight();
        }
        
        if (totalWeight > 0) {
            result.setTotalScore((int) Math.round(totalScore / totalWeight));
        } else {
            result.setTotalScore(0);
        }
    }

    /**
     * 确定健康等级和描述
     */
    private void determineHealthLevel(HealthScoreResult result) {
        int score = result.getTotalScore();
        
        if (score >= 90) {
            result.setHealthLevel("excellent");
            result.setHealthDescription("服务器运行状态优秀，各项指标表现良好");
        } else if (score >= 75) {
            result.setHealthLevel("good");
            result.setHealthDescription("服务器运行状态良好，整体表现正常");
        } else if (score >= 60) {
            result.setHealthLevel("warning");
            result.setHealthDescription("服务器运行状态一般，部分指标需要关注");
        } else if (score >= 40) {
            result.setHealthLevel("critical");
            result.setHealthDescription("服务器运行状态较差，需要及时处理");
        } else {
            result.setHealthLevel("critical");
            result.setHealthDescription("服务器运行状态严重异常，急需处理");
        }
    }

    /**
     * 生成优化建议
     */
    private void generateRecommendations(HealthScoreResult result) {
        java.util.List<String> recommendations = new java.util.ArrayList<>();
        
        for (MetricScore metricScore : result.getMetricScores().values()) {
            if ("critical".equals(metricScore.getStatus())) {
                switch (metricScore.getMetricName()) {
                    case "CPU使用率":
                        recommendations.add("优化CPU密集型进程，考虑升级CPU或增加服务器节点");
                        break;
                    case "内存使用率":
                        recommendations.add("释放内存资源，考虑增加内存容量或优化内存使用");
                        break;
                    case "磁盘使用率":
                        recommendations.add("清理磁盘空间，删除不必要的文件或扩展存储容量");
                        break;
                    case "响应时间":
                        recommendations.add("检查网络连接和服务器负载，优化应用程序性能");
                        break;
                }
            } else if ("warning".equals(metricScore.getStatus())) {
                switch (metricScore.getMetricName()) {
                    case "CPU使用率":
                        recommendations.add("监控CPU使用趋势，准备性能优化方案");
                        break;
                    case "内存使用率":
                        recommendations.add("监控内存使用趋势，考虑内存优化");
                        break;
                    case "磁盘使用率":
                        recommendations.add("定期清理磁盘空间，制定存储扩容计划");
                        break;
                }
            }
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("服务器运行状态良好，建议继续保持当前配置");
        }
        
        result.setRecommendations(recommendations.toArray(new String[0]));
    }
}
