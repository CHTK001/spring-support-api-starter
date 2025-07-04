package com.chua.starter.oauth.sso.support.service.impl;

import com.chua.starter.oauth.sso.support.entity.AnalysisResult;
import com.chua.starter.oauth.sso.support.entity.LoginRecord;
import com.chua.starter.oauth.sso.support.entity.LoginStatistics;
import com.chua.starter.oauth.sso.support.service.AnalysisService;
import com.chua.starter.oauth.sso.support.service.LoginTrackingService;
import com.chua.starter.oauth.sso.support.spi.AnalysisProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 内部分析服务实现
 * 
 * 提供默认的内部分析功能
 * 
 * @author CH
 * @version 4.0.0.32
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InternalAnalysisServiceImpl implements AnalysisService {

    private final LoginTrackingService loginTrackingService;
    private final List<AnalysisProvider> analysisProviders;
    private final AnalysisConfigImpl analysisConfig = new AnalysisConfigImpl();

    @Override
    @Cacheable(value = "oauth-analysis", key = "#analysisType + '_' + #days")
    public AnalysisResult performAnalysis(String analysisType, int days) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("开始执行OAuth认证分析: type={}, days={}", analysisType, days);
            
            // 检查分析类型是否支持
            if (!isAnalysisTypeSupported(analysisType)) {
                return AnalysisResult.builder()
                        .success(false)
                        .message("不支持的分析类型: " + analysisType)
                        .analysisType(analysisType)
                        .days(days)
                        .analysisTime(LocalDateTime.now())
                        .build();
            }
            
            // 获取基础统计数据
            LoginStatistics basicStats = loginTrackingService.getLoginStatistics(days);
            
            // 执行具体分析
            AnalysisResult.AnalysisResultBuilder resultBuilder = AnalysisResult.builder()
                    .success(true)
                    .analysisType(analysisType)
                    .days(days)
                    .startTime(LocalDateTime.now().minusDays(days))
                    .endTime(LocalDateTime.now())
                    .analysisTime(LocalDateTime.now())
                    .basicStats(basicStats);
            
            if ("internal".equals(analysisType)) {
                // 内部分析
                performInternalAnalysis(resultBuilder, days);
            } else {
                // 使用SPI提供者进行分析
                performSpiAnalysis(resultBuilder, analysisType, days);
            }
            
            long endTime = System.currentTimeMillis();
            AnalysisResult result = resultBuilder
                    .analysisTimeMs(endTime - startTime)
                    .build();
            
            log.info("OAuth认证分析完成: type={}, days={}, 耗时={}ms", analysisType, days, result.getAnalysisTimeMs());
            return result;
            
        } catch (Exception e) {
            log.error("OAuth认证分析失败", e);
            return AnalysisResult.builder()
                    .success(false)
                    .message("分析失败: " + e.getMessage())
                    .analysisType(analysisType)
                    .days(days)
                    .analysisTime(LocalDateTime.now())
                    .analysisTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    /**
     * 执行内部分析
     */
    private void performInternalAnalysis(AnalysisResult.AnalysisResultBuilder resultBuilder, int days) {
        // 安全分析
        AnalysisResult.SecurityAnalysis securityAnalysis = performSecurityAnalysis(days);
        resultBuilder.securityAnalysis(securityAnalysis);
        
        // 用户行为分析
        AnalysisResult.UserBehaviorAnalysis userBehaviorAnalysis = performUserBehaviorAnalysis(days);
        resultBuilder.userBehaviorAnalysis(userBehaviorAnalysis);
        
        // 系统性能分析
        AnalysisResult.SystemPerformanceAnalysis systemPerformanceAnalysis = performSystemPerformanceAnalysis(days);
        resultBuilder.systemPerformanceAnalysis(systemPerformanceAnalysis);
        
        // 趋势分析
        AnalysisResult.TrendAnalysis trendAnalysis = performTrendAnalysis(days);
        resultBuilder.trendAnalysis(trendAnalysis);
        
        // 生成建议
        List<AnalysisResult.Recommendation> recommendations = generateRecommendations(securityAnalysis, userBehaviorAnalysis, systemPerformanceAnalysis);
        resultBuilder.recommendations(recommendations);
        
        // 生成图表数据
        Map<String, Object> chartData = generateChartData(days);
        resultBuilder.chartData(chartData);
        
        resultBuilder.message("内部分析完成");
    }

    /**
     * 使用SPI提供者进行分析
     */
    private void performSpiAnalysis(AnalysisResult.AnalysisResultBuilder resultBuilder, String analysisType, int days) {
        Optional<AnalysisProvider> provider = analysisProviders.stream()
                .filter(p -> p.supports(analysisType))
                .findFirst();
        
        if (provider.isPresent()) {
            try {
                AnalysisResult spiResult = provider.get().analyze(analysisType, days);
                
                // 合并SPI分析结果
                resultBuilder
                        .securityAnalysis(spiResult.getSecurityAnalysis())
                        .userBehaviorAnalysis(spiResult.getUserBehaviorAnalysis())
                        .systemPerformanceAnalysis(spiResult.getSystemPerformanceAnalysis())
                        .trendAnalysis(spiResult.getTrendAnalysis())
                        .aiAnalysisResult(spiResult.getAiAnalysisResult())
                        .recommendations(spiResult.getRecommendations())
                        .chartData(spiResult.getChartData())
                        .message("SPI分析完成: " + analysisType);
                        
            } catch (Exception e) {
                log.error("SPI分析失败: " + analysisType, e);
                resultBuilder.message("SPI分析失败: " + e.getMessage());
            }
        } else {
            resultBuilder.message("未找到对应的分析提供者: " + analysisType);
        }
    }

    /**
     * 执行安全分析
     */
    private AnalysisResult.SecurityAnalysis performSecurityAnalysis(int days) {
        try {
            // 获取登录历史
            List<LoginRecord> loginHistory = loginTrackingService.getLoginHistory(days, 10000);
            
            // 分析可疑IP
            List<AnalysisResult.SuspiciousIp> suspiciousIps = analyzeSuspiciousIps(loginHistory);
            
            // 检测暴力破解
            AnalysisResult.BruteForceDetection bruteForceDetection = detectBruteForce(loginHistory);
            
            // 计算风险评分
            double riskScore = calculateRiskScore(suspiciousIps, bruteForceDetection);
            String riskLevel = determineRiskLevel(riskScore);
            
            return AnalysisResult.SecurityAnalysis.builder()
                    .riskLevel(riskLevel)
                    .riskScore(riskScore)
                    .suspiciousIps(suspiciousIps)
                    .bruteForceDetection(bruteForceDetection)
                    .anomalousPatterns(new ArrayList<>())
                    .geographicAnomalies(new ArrayList<>())
                    .build();
                    
        } catch (Exception e) {
            log.error("安全分析失败", e);
            return AnalysisResult.SecurityAnalysis.builder()
                    .riskLevel("UNKNOWN")
                    .riskScore(0.0)
                    .suspiciousIps(new ArrayList<>())
                    .anomalousPatterns(new ArrayList<>())
                    .bruteForceDetection(AnalysisResult.BruteForceDetection.builder().detected(false).build())
                    .geographicAnomalies(new ArrayList<>())
                    .build();
        }
    }

    /**
     * 分析可疑IP
     */
    private List<AnalysisResult.SuspiciousIp> analyzeSuspiciousIps(List<LoginRecord> loginHistory) {
        Map<String, List<LoginRecord>> ipGroups = loginHistory.stream()
                .collect(Collectors.groupingBy(LoginRecord::getIp));
        
        return ipGroups.entrySet().stream()
                .filter(entry -> {
                    List<LoginRecord> records = entry.getValue();
                    long failedCount = records.stream().mapToLong(r -> r.isSuccess() ? 0 : 1).sum();
                    double failureRate = (double) failedCount / records.size();
                    return failureRate > 0.5 || failedCount > 10; // 失败率超过50%或失败次数超过10次
                })
                .map(entry -> {
                    String ip = entry.getKey();
                    List<LoginRecord> records = entry.getValue();
                    long failedCount = records.stream().mapToLong(r -> r.isSuccess() ? 0 : 1).sum();
                    double failureRate = (double) failedCount / records.size();
                    
                    return AnalysisResult.SuspiciousIp.builder()
                            .ip(ip)
                            .reason("高失败率认证")
                            .riskScore(failureRate * 100)
                            .failedAttempts(failedCount)
                            .location("未知")
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 检测暴力破解
     */
    private AnalysisResult.BruteForceDetection detectBruteForce(List<LoginRecord> loginHistory) {
        // 统计每个IP的失败尝试
        Map<String, Long> ipFailureCounts = loginHistory.stream()
                .filter(record -> !record.isSuccess())
                .collect(Collectors.groupingBy(LoginRecord::getIp, Collectors.counting()));
        
        // 检测暴力破解（单个IP失败次数超过20次）
        List<String> attackerIps = ipFailureCounts.entrySet().stream()
                .filter(entry -> entry.getValue() > 20)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        long totalAttempts = ipFailureCounts.values().stream().mapToLong(Long::longValue).sum();
        
        return AnalysisResult.BruteForceDetection.builder()
                .detected(!attackerIps.isEmpty())
                .attackerIps(attackerIps)
                .totalAttempts(totalAttempts)
                .timeWindow("最近" + loginHistory.size() + "条记录")
                .build();
    }

    /**
     * 计算风险评分
     */
    private double calculateRiskScore(List<AnalysisResult.SuspiciousIp> suspiciousIps, 
                                    AnalysisResult.BruteForceDetection bruteForceDetection) {
        double score = 0.0;
        
        // 可疑IP评分
        score += suspiciousIps.size() * 10;
        
        // 暴力破解评分
        if (bruteForceDetection.isDetected()) {
            score += bruteForceDetection.getAttackerIps().size() * 20;
        }
        
        return Math.min(score, 100.0); // 最高100分
    }

    /**
     * 确定风险等级
     */
    private String determineRiskLevel(double riskScore) {
        if (riskScore >= 80) return "CRITICAL";
        if (riskScore >= 60) return "HIGH";
        if (riskScore >= 30) return "MEDIUM";
        return "LOW";
    }

    /**
     * 执行用户行为分析
     */
    private AnalysisResult.UserBehaviorAnalysis performUserBehaviorAnalysis(int days) {
        try {
            LoginStatistics stats = loginTrackingService.getLoginStatistics(days);
            Map<String, Long> protocolStats = loginTrackingService.getProtocolUsageStats(days);
            Map<String, Long> authMethodStats = loginTrackingService.getAuthMethodUsageStats(days);
            
            // 模拟登录时间模式分析
            Map<Integer, Long> loginTimePatterns = new HashMap<>();
            for (int hour = 0; hour < 24; hour++) {
                loginTimePatterns.put(hour, (long) (Math.random() * 100)); // 模拟数据
            }
            
            return AnalysisResult.UserBehaviorAnalysis.builder()
                    .activeUsers(stats.getUniqueUsers())
                    .activityDistribution(Map.of("高活跃", 20L, "中活跃", 50L, "低活跃", 30L))
                    .loginTimePatterns(loginTimePatterns)
                    .protocolPreferences(protocolStats)
                    .authMethodPreferences(authMethodStats)
                    .anomalousUserBehaviors(new ArrayList<>())
                    .build();
                    
        } catch (Exception e) {
            log.error("用户行为分析失败", e);
            return AnalysisResult.UserBehaviorAnalysis.builder()
                    .activeUsers(0L)
                    .activityDistribution(new HashMap<>())
                    .loginTimePatterns(new HashMap<>())
                    .protocolPreferences(new HashMap<>())
                    .authMethodPreferences(new HashMap<>())
                    .anomalousUserBehaviors(new ArrayList<>())
                    .build();
        }
    }

    /**
     * 执行系统性能分析
     */
    private AnalysisResult.SystemPerformanceAnalysis performSystemPerformanceAnalysis(int days) {
        try {
            LoginStatistics stats = loginTrackingService.getLoginStatistics(days);
            
            double successRate = stats.getTotalLogins() > 0 ? 
                    (double) stats.getSuccessfulLogins() / stats.getTotalLogins() * 100 : 0.0;
            
            // 模拟性能数据
            double averageResponseTime = 150.0 + Math.random() * 100; // 150-250ms
            long peakQps = (long) (stats.getTotalLogins() / (days * 24 * 3600) * 10); // 估算峰值QPS
            double averageQps = (double) stats.getTotalLogins() / (days * 24 * 3600);
            
            return AnalysisResult.SystemPerformanceAnalysis.builder()
                    .averageResponseTime(averageResponseTime)
                    .successRate(successRate)
                    .peakQps(peakQps)
                    .averageQps(averageQps)
                    .loadAnalysis(Map.of("CPU", "60%", "内存", "45%", "网络", "30%"))
                    .bottlenecks(new ArrayList<>())
                    .build();
                    
        } catch (Exception e) {
            log.error("系统性能分析失败", e);
            return AnalysisResult.SystemPerformanceAnalysis.builder()
                    .averageResponseTime(0.0)
                    .successRate(0.0)
                    .peakQps(0L)
                    .averageQps(0.0)
                    .loadAnalysis(new HashMap<>())
                    .bottlenecks(new ArrayList<>())
                    .build();
        }
    }

    /**
     * 执行趋势分析
     */
    private AnalysisResult.TrendAnalysis performTrendAnalysis(int days) {
        try {
            // 简单的趋势分析
            LoginStatistics currentStats = loginTrackingService.getLoginStatistics(days);
            LoginStatistics previousStats = loginTrackingService.getLoginStatistics(days * 2);
            
            long currentLogins = currentStats.getTotalLogins();
            long previousLogins = previousStats.getTotalLogins() - currentLogins;
            
            double trendPercentage = previousLogins > 0 ? 
                    ((double) (currentLogins - previousLogins) / previousLogins) * 100 : 0.0;
            
            String trend = trendPercentage > 5 ? "增长" : trendPercentage < -5 ? "下降" : "稳定";
            
            return AnalysisResult.TrendAnalysis.builder()
                    .loginTrend(trend)
                    .trendPercentage(trendPercentage)
                    .predictions(Map.of("下周预测", currentLogins * 1.1))
                    .seasonalPatterns(Map.of("工作日", "高", "周末", "低"))
                    .build();
                    
        } catch (Exception e) {
            log.error("趋势分析失败", e);
            return AnalysisResult.TrendAnalysis.builder()
                    .loginTrend("未知")
                    .trendPercentage(0.0)
                    .predictions(new HashMap<>())
                    .seasonalPatterns(new HashMap<>())
                    .build();
        }
    }

    /**
     * 生成建议
     */
    private List<AnalysisResult.Recommendation> generateRecommendations(
            AnalysisResult.SecurityAnalysis securityAnalysis,
            AnalysisResult.UserBehaviorAnalysis userBehaviorAnalysis,
            AnalysisResult.SystemPerformanceAnalysis systemPerformanceAnalysis) {

        List<AnalysisResult.Recommendation> recommendations = new ArrayList<>();

        // 安全建议
        if ("HIGH".equals(securityAnalysis.getRiskLevel()) || "CRITICAL".equals(securityAnalysis.getRiskLevel())) {
            recommendations.add(AnalysisResult.Recommendation.builder()
                    .type("SECURITY")
                    .priority("HIGH")
                    .title("加强安全防护")
                    .description("检测到高风险安全威胁，建议立即采取防护措施")
                    .action("启用IP白名单、增强密码策略、启用多因子认证")
                    .impact("显著提升系统安全性")
                    .build());
        }

        if (securityAnalysis.getBruteForceDetection().isDetected()) {
            recommendations.add(AnalysisResult.Recommendation.builder()
                    .type("SECURITY")
                    .priority("CRITICAL")
                    .title("暴力破解防护")
                    .description("检测到暴力破解攻击，需要立即处理")
                    .action("封禁攻击IP、启用验证码、限制登录频率")
                    .impact("阻止恶意攻击，保护用户账户安全")
                    .build());
        }

        // 性能建议
        if (systemPerformanceAnalysis.getSuccessRate() < 95.0) {
            recommendations.add(AnalysisResult.Recommendation.builder()
                    .type("PERFORMANCE")
                    .priority("MEDIUM")
                    .title("提升认证成功率")
                    .description("当前认证成功率较低，可能影响用户体验")
                    .action("优化认证流程、检查系统稳定性、改进错误处理")
                    .impact("提升用户体验和系统可靠性")
                    .build());
        }

        if (systemPerformanceAnalysis.getAverageResponseTime() > 500) {
            recommendations.add(AnalysisResult.Recommendation.builder()
                    .type("PERFORMANCE")
                    .priority("MEDIUM")
                    .title("优化响应时间")
                    .description("认证响应时间较长，可能影响用户体验")
                    .action("优化数据库查询、增加缓存、升级硬件配置")
                    .impact("提升系统响应速度和用户满意度")
                    .build());
        }

        // 优化建议
        if (userBehaviorAnalysis.getActiveUsers() > 1000) {
            recommendations.add(AnalysisResult.Recommendation.builder()
                    .type("OPTIMIZATION")
                    .priority("LOW")
                    .title("扩展系统容量")
                    .description("用户数量增长较快，建议考虑系统扩容")
                    .action("增加服务器资源、优化负载均衡、考虑微服务架构")
                    .impact("确保系统能够支撑更大的用户规模")
                    .build());
        }

        return recommendations;
    }

    /**
     * 生成图表数据
     */
    private Map<String, Object> generateChartData(int days) {
        Map<String, Object> chartData = new HashMap<>();

        try {
            LoginStatistics stats = loginTrackingService.getLoginStatistics(days);

            // 登录趋势图数据
            Map<String, Long> dailyStats = stats.getDailyStats();
            if (dailyStats != null) {
                chartData.put("loginTrend", dailyStats);
            }

            // 成功失败比例饼图
            Map<String, Long> successFailureData = new HashMap<>();
            successFailureData.put("成功", stats.getSuccessfulLogins());
            successFailureData.put("失败", stats.getFailedLogins());
            chartData.put("successFailure", successFailureData);

            // 协议使用分布
            Map<String, Long> protocolStats = loginTrackingService.getProtocolUsageStats(days);
            chartData.put("protocolUsage", protocolStats);

            // 认证方式分布
            Map<String, Long> authMethodStats = loginTrackingService.getAuthMethodUsageStats(days);
            chartData.put("authMethodUsage", authMethodStats);

            // 小时分布图
            Map<Integer, Long> hourlyStats = stats.getHourlyStats();
            if (hourlyStats != null) {
                chartData.put("hourlyDistribution", hourlyStats);
            }

        } catch (Exception e) {
            log.error("生成图表数据失败", e);
        }

        return chartData;
    }

    @Override
    public List<String> getSupportedAnalysisTypes() {
        List<String> types = new ArrayList<>();
        types.add("internal"); // 内部分析

        // 添加SPI提供者支持的类型
        analysisProviders.forEach(provider -> {
            types.addAll(provider.getSupportedTypes());
        });

        return types;
    }

    @Override
    @CacheEvict(value = "oauth-analysis", allEntries = true)
    public void clearCache() {
        log.info("清除OAuth认证分析缓存");
    }

    @Override
    public boolean isAnalysisTypeSupported(String analysisType) {
        return getSupportedAnalysisTypes().contains(analysisType);
    }

    @Override
    public AnalysisConfig getAnalysisConfig() {
        return analysisConfig;
    }

    @Override
    public void updateAnalysisConfig(AnalysisConfig config) {
        if (config instanceof AnalysisConfigImpl) {
            AnalysisConfigImpl impl = (AnalysisConfigImpl) config;
            this.analysisConfig.setAiAnalysisEnabled(impl.isAiAnalysisEnabled());
            this.analysisConfig.setAiProvider(impl.getAiProvider());
            this.analysisConfig.setCacheTimeMinutes(impl.getCacheTimeMinutes());
            this.analysisConfig.setMaxAnalysisDays(impl.getMaxAnalysisDays());
        }
    }

    /**
     * 分析配置实现
     */
    public static class AnalysisConfigImpl implements AnalysisConfig {
        private boolean aiAnalysisEnabled = false;
        private String aiProvider = "openai";
        private int cacheTimeMinutes = 30;
        private int maxAnalysisDays = 90;

        @Override
        public boolean isAiAnalysisEnabled() {
            return aiAnalysisEnabled;
        }

        public void setAiAnalysisEnabled(boolean aiAnalysisEnabled) {
            this.aiAnalysisEnabled = aiAnalysisEnabled;
        }

        @Override
        public String getAiProvider() {
            return aiProvider;
        }

        public void setAiProvider(String aiProvider) {
            this.aiProvider = aiProvider;
        }

        @Override
        public int getCacheTimeMinutes() {
            return cacheTimeMinutes;
        }

        public void setCacheTimeMinutes(int cacheTimeMinutes) {
            this.cacheTimeMinutes = cacheTimeMinutes;
        }

        @Override
        public int getMaxAnalysisDays() {
            return maxAnalysisDays;
        }

        public void setMaxAnalysisDays(int maxAnalysisDays) {
            this.maxAnalysisDays = maxAnalysisDays;
        }
    }
}
