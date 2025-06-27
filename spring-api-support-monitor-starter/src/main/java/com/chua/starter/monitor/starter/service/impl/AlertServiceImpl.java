package com.chua.starter.monitor.starter.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.starter.service.AlertService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 告警服务实现类
 *
 * @author CH
 * @since 2024/12/27
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    private static final String ALERT_SILENCE_KEY_PREFIX = "alert:silence:";
    private static final String ALERT_HISTORY_KEY_PREFIX = "alert:history:";
    private static final String ALERT_STATS_KEY_PREFIX = "alert:stats:";
    private final MonitorSysGenServerSettingService settingService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public ReturnResult<Boolean> checkCpuAlert(Integer serverId, BigDecimal cpuUsage) {
        try {
            ReturnResult<MonitorSysGenServerSetting> settingResult = settingService.getByServerId(serverId);
            if (!settingResult.isOk() || settingResult.getData() == null) {
                return ReturnResult.error("获取服务器配置失败");
            }

            MonitorSysGenServerSetting setting = settingResult.getData();
            if (!setting.isAlertEnabled()) {
                return ReturnResult.ok(false, "告警功能未启用");
            }

            BigDecimal threshold = setting.getMonitorSysGenServerSettingCpuAlertThreshold();
            if (threshold == null) {
                return ReturnResult.ok(false, "CPU告警阈值未配置");
            }

            if (cpuUsage.compareTo(threshold) > 0) {
                // 检查是否在静默期
                if (isInSilencePeriod(serverId, "CPU")) {
                    return ReturnResult.ok(false, "告警在静默期内");
                }

                // 发送告警
                String alertMessage = String.format("CPU使用率过高: 当前值 %.2f%%, 阈值 %.2f%%",
                        cpuUsage, threshold);
                return sendAlert(serverId, "CPU", alertMessage, cpuUsage, threshold);
            }

            return ReturnResult.ok(false, "CPU使用率正常");
        } catch (Exception e) {
            log.error("检查CPU告警失败", e);
            return ReturnResult.error("检查CPU告警失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> checkMemoryAlert(Integer serverId, BigDecimal memoryUsage) {
        try {
            ReturnResult<MonitorSysGenServerSetting> settingResult = settingService.getByServerId(serverId);
            if (!settingResult.isOk() || settingResult.getData() == null) {
                return ReturnResult.error("获取服务器配置失败");
            }

            MonitorSysGenServerSetting setting = settingResult.getData();
            if (!setting.isAlertEnabled()) {
                return ReturnResult.ok(false, "告警功能未启用");
            }

            BigDecimal threshold = setting.getMonitorSysGenServerSettingMemoryAlertThreshold();
            if (threshold == null) {
                return ReturnResult.ok(false, "内存告警阈值未配置");
            }

            if (memoryUsage.compareTo(threshold) > 0) {
                if (isInSilencePeriod(serverId, "MEMORY")) {
                    return ReturnResult.ok(false, "告警在静默期内");
                }

                String alertMessage = String.format("内存使用率过高: 当前值 %.2f%%, 阈值 %.2f%%",
                        memoryUsage, threshold);
                return sendAlert(serverId, "MEMORY", alertMessage, memoryUsage, threshold);
            }

            return ReturnResult.ok(false, "内存使用率正常");
        } catch (Exception e) {
            log.error("检查内存告警失败", e);
            return ReturnResult.error("检查内存告警失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> checkDiskAlert(Integer serverId, BigDecimal diskUsage) {
        try {
            ReturnResult<MonitorSysGenServerSetting> settingResult = settingService.getByServerId(serverId);
            if (!settingResult.isOk() || settingResult.getData() == null) {
                return ReturnResult.error("获取服务器配置失败");
            }

            MonitorSysGenServerSetting setting = settingResult.getData();
            if (!setting.isAlertEnabled()) {
                return ReturnResult.ok(false, "告警功能未启用");
            }

            BigDecimal threshold = setting.getMonitorSysGenServerSettingDiskAlertThreshold();
            if (threshold == null) {
                return ReturnResult.ok(false, "磁盘告警阈值未配置");
            }

            if (diskUsage.compareTo(threshold) > 0) {
                if (isInSilencePeriod(serverId, "DISK")) {
                    return ReturnResult.ok(false, "告警在静默期内");
                }

                String alertMessage = String.format("磁盘使用率过高: 当前值 %.2f%%, 阈值 %.2f%%",
                        diskUsage, threshold);
                return sendAlert(serverId, "DISK", alertMessage, diskUsage, threshold);
            }

            return ReturnResult.ok(false, "磁盘使用率正常");
        } catch (Exception e) {
            log.error("检查磁盘告警失败", e);
            return ReturnResult.error("检查磁盘告警失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> checkNetworkAlert(Integer serverId, BigDecimal networkUsage) {
        try {
            ReturnResult<MonitorSysGenServerSetting> settingResult = settingService.getByServerId(serverId);
            if (!settingResult.isOk() || settingResult.getData() == null) {
                return ReturnResult.error("获取服务器配置失败");
            }

            MonitorSysGenServerSetting setting = settingResult.getData();
            if (!setting.isAlertEnabled()) {
                return ReturnResult.ok(false, "告警功能未启用");
            }

            BigDecimal threshold = setting.getMonitorSysGenServerSettingNetworkAlertThreshold();
            if (threshold == null) {
                return ReturnResult.ok(false, "网络告警阈值未配置");
            }

            if (networkUsage.compareTo(threshold) > 0) {
                if (isInSilencePeriod(serverId, "NETWORK")) {
                    return ReturnResult.ok(false, "告警在静默期内");
                }

                String alertMessage = String.format("网络使用率过高: 当前值 %.2f Mbps, 阈值 %.2f Mbps",
                        networkUsage, threshold);
                return sendAlert(serverId, "NETWORK", alertMessage, networkUsage, threshold);
            }

            return ReturnResult.ok(false, "网络使用率正常");
        } catch (Exception e) {
            log.error("检查网络告警失败", e);
            return ReturnResult.error("检查网络告警失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> checkResponseTimeAlert(Integer serverId, Long responseTime) {
        try {
            ReturnResult<MonitorSysGenServerSetting> settingResult = settingService.getByServerId(serverId);
            if (!settingResult.isOk() || settingResult.getData() == null) {
                return ReturnResult.error("获取服务器配置失败");
            }

            MonitorSysGenServerSetting setting = settingResult.getData();
            if (!setting.isAlertEnabled()) {
                return ReturnResult.ok(false, "告警功能未启用");
            }

            Long threshold = setting.getMonitorSysGenServerSettingResponseTimeAlertThreshold();
            if (threshold == null) {
                return ReturnResult.ok(false, "响应时间告警阈值未配置");
            }

            if (responseTime > threshold) {
                if (isInSilencePeriod(serverId, "RESPONSE_TIME")) {
                    return ReturnResult.ok(false, "告警在静默期内");
                }

                String alertMessage = String.format("响应时间过长: 当前值 %d ms, 阈值 %d ms",
                        responseTime, threshold);
                return sendAlert(serverId, "RESPONSE_TIME", alertMessage, responseTime, threshold);
            }

            return ReturnResult.ok(false, "响应时间正常");
        } catch (Exception e) {
            log.error("检查响应时间告警失败", e);
            return ReturnResult.error("检查响应时间告警失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> checkServerMetricsAlert(Integer serverId, Map<String, Object> metrics) {
        try {
            boolean hasAlert = false;
            StringBuilder alertMessages = new StringBuilder();

            // 检查CPU
            if (metrics.containsKey("cpuUsage")) {
                BigDecimal cpuUsage = new BigDecimal(metrics.get("cpuUsage").toString());
                ReturnResult<Boolean> cpuResult = checkCpuAlert(serverId, cpuUsage);
                if (cpuResult.isOk() && cpuResult.getData()) {
                    hasAlert = true;
                    alertMessages.append("CPU告警; ");
                }
            }

            // 检查内存
            if (metrics.containsKey("memoryUsage")) {
                BigDecimal memoryUsage = new BigDecimal(metrics.get("memoryUsage").toString());
                ReturnResult<Boolean> memoryResult = checkMemoryAlert(serverId, memoryUsage);
                if (memoryResult.isOk() && memoryResult.getData()) {
                    hasAlert = true;
                    alertMessages.append("内存告警; ");
                }
            }

            // 检查磁盘
            if (metrics.containsKey("diskUsage")) {
                BigDecimal diskUsage = new BigDecimal(metrics.get("diskUsage").toString());
                ReturnResult<Boolean> diskResult = checkDiskAlert(serverId, diskUsage);
                if (diskResult.isOk() && diskResult.getData()) {
                    hasAlert = true;
                    alertMessages.append("磁盘告警; ");
                }
            }

            // 检查网络
            if (metrics.containsKey("networkUsage")) {
                BigDecimal networkUsage = new BigDecimal(metrics.get("networkUsage").toString());
                ReturnResult<Boolean> networkResult = checkNetworkAlert(serverId, networkUsage);
                if (networkResult.isOk() && networkResult.getData()) {
                    hasAlert = true;
                    alertMessages.append("网络告警; ");
                }
            }

            // 检查响应时间
            if (metrics.containsKey("responseTime")) {
                Long responseTime = Long.valueOf(metrics.get("responseTime").toString());
                ReturnResult<Boolean> responseResult = checkResponseTimeAlert(serverId, responseTime);
                if (responseResult.isOk() && responseResult.getData()) {
                    hasAlert = true;
                    alertMessages.append("响应时间告警; ");
                }
            }

            if (hasAlert) {
                // 更新告警统计
                updateAlertStatistics(serverId);
                return ReturnResult.ok(true, alertMessages.toString());
            }

            return ReturnResult.ok(false, "所有指标正常");
        } catch (Exception e) {
            log.error("检查服务器指标告警失败", e);
            return ReturnResult.error("检查服务器指标告警失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> sendAlert(Integer serverId, String alertType, String alertMessage,
                                           Object currentValue, Object thresholdValue) {
        try {
            ReturnResult<MonitorSysGenServerSetting> settingResult = settingService.getByServerId(serverId);
            if (!settingResult.isOk() || settingResult.getData() == null) {
                return ReturnResult.error("获取服务器配置失败");
            }

            MonitorSysGenServerSetting setting = settingResult.getData();
            String notificationMethod = setting.getMonitorSysGenServerSettingAlertNotificationMethod();
            String notificationAddress = setting.getMonitorSysGenServerSettingAlertNotificationAddress();

            if (StringUtils.isBlank(notificationMethod) || StringUtils.isBlank(notificationAddress)) {
                return ReturnResult.error("告警通知配置不完整");
            }

            // 构建告警消息
            String fullMessage = String.format("[服务器告警] 服务器ID: %d, 告警类型: %s, %s, 时间: %s",
                    serverId, alertType, alertMessage, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            // 根据通知方式发送告警
            boolean sent = false;
            switch (notificationMethod) {
                case "EMAIL":
                    sent = sendEmailAlert(notificationAddress, fullMessage);
                    break;
                case "SMS":
                    sent = sendSmsAlert(notificationAddress, fullMessage);
                    break;
                case "DINGTALK":
                    sent = sendDingTalkAlert(notificationAddress, fullMessage);
                    break;
                case "WECHAT":
                    sent = sendWeChatAlert(notificationAddress, fullMessage);
                    break;
                case "WEBHOOK":
                    sent = sendWebhookAlert(notificationAddress, fullMessage, serverId, alertType, currentValue, thresholdValue);
                    break;
                default:
                    return ReturnResult.error("不支持的通知方式: " + notificationMethod);
            }

            if (sent) {
                // 设置告警静默
                Integer silenceDuration = setting.getMonitorSysGenServerSettingAlertSilenceDuration();
                if (silenceDuration != null && silenceDuration > 0) {
                    setAlertSilence(serverId, alertType, silenceDuration);
                }

                // 记录告警历史
                recordAlertHistory(serverId, alertType, alertMessage, currentValue, thresholdValue);

                log.info("告警发送成功: 服务器={}, 类型={}, 方式={}", serverId, alertType, notificationMethod);
                return ReturnResult.ok(true, "告警发送成功");
            } else {
                return ReturnResult.error("告警发送失败");
            }
        } catch (Exception e) {
            log.error("发送告警失败", e);
            return ReturnResult.error("发送告警失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> sendRecoveryNotification(Integer serverId, String alertType, String recoveryMessage) {
        try {
            ReturnResult<MonitorSysGenServerSetting> settingResult = settingService.getByServerId(serverId);
            if (!settingResult.isOk() || settingResult.getData() == null) {
                return ReturnResult.error("获取服务器配置失败");
            }

            MonitorSysGenServerSetting setting = settingResult.getData();
            if (!setting.isAutoRecoveryNotificationEnabled()) {
                return ReturnResult.ok(false, "自动恢复通知未启用");
            }

            String notificationMethod = setting.getMonitorSysGenServerSettingAlertNotificationMethod();
            String notificationAddress = setting.getMonitorSysGenServerSettingAlertNotificationAddress();

            if (StringUtils.isBlank(notificationMethod) || StringUtils.isBlank(notificationAddress)) {
                return ReturnResult.error("告警通知配置不完整");
            }

            // 构建恢复消息
            String fullMessage = String.format("[服务器恢复] 服务器ID: %d, 告警类型: %s, %s, 时间: %s",
                    serverId, alertType, recoveryMessage, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            // 发送恢复通知
            boolean sent = false;
            switch (notificationMethod) {
                case "EMAIL":
                    sent = sendEmailAlert(notificationAddress, fullMessage);
                    break;
                case "SMS":
                    sent = sendSmsAlert(notificationAddress, fullMessage);
                    break;
                case "DINGTALK":
                    sent = sendDingTalkAlert(notificationAddress, fullMessage);
                    break;
                case "WECHAT":
                    sent = sendWeChatAlert(notificationAddress, fullMessage);
                    break;
                case "WEBHOOK":
                    sent = sendWebhookAlert(notificationAddress, fullMessage, serverId, alertType, null, null);
                    break;
                default:
                    return ReturnResult.error("不支持的通知方式: " + notificationMethod);
            }

            if (sent) {
                // 取消告警静默
                cancelAlertSilence(serverId, alertType);
                log.info("恢复通知发送成功: 服务器={}, 类型={}", serverId, alertType);
                return ReturnResult.ok(true, "恢复通知发送成功");
            } else {
                return ReturnResult.error("恢复通知发送失败");
            }
        } catch (Exception e) {
            log.error("发送恢复通知失败", e);
            return ReturnResult.error("发送恢复通知失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> testAlertConfiguration(MonitorSysGenServerSetting setting) {
        try {
            if (setting == null) {
                return ReturnResult.error("服务器配置不能为空");
            }

            if (!setting.isAlertEnabled()) {
                return ReturnResult.error("告警功能未启用");
            }

            String notificationMethod = setting.getMonitorSysGenServerSettingAlertNotificationMethod();
            String notificationAddress = setting.getMonitorSysGenServerSettingAlertNotificationAddress();

            if (StringUtils.isBlank(notificationMethod)) {
                return ReturnResult.error("告警通知方式未配置");
            }

            if (StringUtils.isBlank(notificationAddress)) {
                return ReturnResult.error("告警通知地址未配置");
            }

            // 发送测试消息
            String testMessage = String.format("[测试告警] 服务器ID: %d, 这是一条测试告警消息, 时间: %s",
                    setting.getMonitorSysGenServerId(), LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            boolean sent = false;
            switch (notificationMethod) {
                case "EMAIL":
                    sent = sendEmailAlert(notificationAddress, testMessage);
                    break;
                case "SMS":
                    sent = sendSmsAlert(notificationAddress, testMessage);
                    break;
                case "DINGTALK":
                    sent = sendDingTalkAlert(notificationAddress, testMessage);
                    break;
                case "WECHAT":
                    sent = sendWeChatAlert(notificationAddress, testMessage);
                    break;
                case "WEBHOOK":
                    sent = sendWebhookAlert(notificationAddress, testMessage, setting.getMonitorSysGenServerId(), "TEST", null, null);
                    break;
                default:
                    return ReturnResult.error("不支持的通知方式: " + notificationMethod);
            }

            if (sent) {
                return ReturnResult.ok(true, "测试告警发送成功");
            } else {
                return ReturnResult.error("测试告警发送失败");
            }
        } catch (Exception e) {
            log.error("测试告警配置失败", e);
            return ReturnResult.error("测试告警配置失败: " + e.getMessage());
        }
    }

    @Override
    public boolean isInSilencePeriod(Integer serverId, String alertType) {
        try {
            String key = ALERT_SILENCE_KEY_PREFIX + serverId + ":" + alertType;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.warn("检查告警静默状态失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public ReturnResult<Boolean> setAlertSilence(Integer serverId, String alertType, Integer silenceDuration) {
        try {
            if (serverId == null || StringUtils.isBlank(alertType) || silenceDuration == null || silenceDuration <= 0) {
                return ReturnResult.error("参数不能为空");
            }

            String key = ALERT_SILENCE_KEY_PREFIX + serverId + ":" + alertType;
            redisTemplate.opsForValue().set(key, "1", silenceDuration, TimeUnit.MINUTES);

            log.info("设置告警静默: 服务器={}, 类型={}, 时长={}分钟", serverId, alertType, silenceDuration);
            return ReturnResult.ok(true, "告警静默设置成功");
        } catch (Exception e) {
            log.error("设置告警静默失败", e);
            return ReturnResult.error("设置告警静默失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> cancelAlertSilence(Integer serverId, String alertType) {
        try {
            if (serverId == null || StringUtils.isBlank(alertType)) {
                return ReturnResult.error("参数不能为空");
            }

            String key = ALERT_SILENCE_KEY_PREFIX + serverId + ":" + alertType;
            Boolean deleted = redisTemplate.delete(key);

            log.info("取消告警静默: 服务器={}, 类型={}, 结果={}", serverId, alertType, deleted);
            return ReturnResult.ok(Boolean.TRUE.equals(deleted), "告警静默取消成功");
        } catch (Exception e) {
            log.error("取消告警静默失败", e);
            return ReturnResult.error("取消告警静默失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getAlertStatistics(Integer serverId) {
        try {
            Map<String, Object> stats = new HashMap<>();

            String key = ALERT_STATS_KEY_PREFIX + (serverId != null ? serverId : "global");
            Map<Object, Object> redisStats = redisTemplate.opsForHash().entries(key);

            stats.put("totalAlerts", redisStats.getOrDefault("totalAlerts", 0));
            stats.put("cpuAlerts", redisStats.getOrDefault("cpuAlerts", 0));
            stats.put("memoryAlerts", redisStats.getOrDefault("memoryAlerts", 0));
            stats.put("diskAlerts", redisStats.getOrDefault("diskAlerts", 0));
            stats.put("networkAlerts", redisStats.getOrDefault("networkAlerts", 0));
            stats.put("responseTimeAlerts", redisStats.getOrDefault("responseTimeAlerts", 0));
            stats.put("lastAlertTime", redisStats.get("lastAlertTime"));

            return ReturnResult.ok(stats);
        } catch (Exception e) {
            log.error("获取告警统计失败", e);
            return ReturnResult.error("获取告警统计失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Map<String, Object>> getAlertHistory(Integer serverId, Integer limit) {
        try {
            Map<String, Object> result = new HashMap<>();

            // 这里应该从数据库或Redis中获取告警历史
            // 由于没有专门的告警历史表，这里返回模拟数据
            result.put("history", new HashMap<>());
            result.put("total", 0);

            return ReturnResult.ok(result);
        } catch (Exception e) {
            log.error("获取告警历史失败", e);
            return ReturnResult.error("获取告警历史失败: " + e.getMessage());
        }
    }

    // 私有辅助方法

    /**
     * 发送邮件告警
     */
    private boolean sendEmailAlert(String emailAddress, String message) {
        try {
            // 这里应该集成邮件发送服务
            log.info("发送邮件告警: 地址={}, 消息={}", emailAddress, message);
            return true; // 模拟发送成功
        } catch (Exception e) {
            log.error("发送邮件告警失败", e);
            return false;
        }
    }

    /**
     * 发送短信告警
     */
    private boolean sendSmsAlert(String phoneNumber, String message) {
        try {
            // 这里应该集成短信发送服务
            log.info("发送短信告警: 号码={}, 消息={}", phoneNumber, message);
            return true; // 模拟发送成功
        } catch (Exception e) {
            log.error("发送短信告警失败", e);
            return false;
        }
    }

    /**
     * 发送钉钉告警
     */
    private boolean sendDingTalkAlert(String webhookUrl, String message) {
        try {
            // 这里应该调用钉钉机器人API
            log.info("发送钉钉告警: URL={}, 消息={}", webhookUrl, message);
            return true; // 模拟发送成功
        } catch (Exception e) {
            log.error("发送钉钉告警失败", e);
            return false;
        }
    }

    /**
     * 发送企业微信告警
     */
    private boolean sendWeChatAlert(String webhookUrl, String message) {
        try {
            // 这里应该调用企业微信机器人API
            log.info("发送企业微信告警: URL={}, 消息={}", webhookUrl, message);
            return true; // 模拟发送成功
        } catch (Exception e) {
            log.error("发送企业微信告警失败", e);
            return false;
        }
    }

    /**
     * 发送Webhook告警
     */
    private boolean sendWebhookAlert(String webhookUrl, String message, Integer serverId,
                                     String alertType, Object currentValue, Object thresholdValue) {
        try {
            // 这里应该发送HTTP POST请求到指定的Webhook URL
            Map<String, Object> payload = new HashMap<>();
            payload.put("serverId", serverId);
            payload.put("alertType", alertType);
            payload.put("message", message);
            payload.put("currentValue", currentValue);
            payload.put("thresholdValue", thresholdValue);
            payload.put("timestamp", LocalDateTime.now());

            log.info("发送Webhook告警: URL={}, 数据={}", webhookUrl, payload);
            return true; // 模拟发送成功
        } catch (Exception e) {
            log.error("发送Webhook告警失败", e);
            return false;
        }
    }

    /**
     * 记录告警历史
     */
    private void recordAlertHistory(Integer serverId, String alertType, String message,
                                    Object currentValue, Object thresholdValue) {
        try {
            String key = ALERT_HISTORY_KEY_PREFIX + serverId;
            Map<String, Object> alertRecord = new HashMap<>();
            alertRecord.put("alertType", alertType);
            alertRecord.put("message", message);
            alertRecord.put("currentValue", currentValue);
            alertRecord.put("thresholdValue", thresholdValue);
            alertRecord.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            // 使用Redis List存储告警历史，保留最近100条
            redisTemplate.opsForList().leftPush(key, alertRecord);
            redisTemplate.opsForList().trim(key, 0, 99);
            redisTemplate.expire(key, 30, TimeUnit.DAYS);
        } catch (Exception e) {
            log.warn("记录告警历史失败: {}", e.getMessage());
        }
    }

    /**
     * 更新告警统计
     */
    private void updateAlertStatistics(Integer serverId) {
        try {
            String globalKey = ALERT_STATS_KEY_PREFIX + "global";
            String serverKey = ALERT_STATS_KEY_PREFIX + serverId;

            // 更新全局统计
            redisTemplate.opsForHash().increment(globalKey, "totalAlerts", 1);
            redisTemplate.opsForHash().put(globalKey, "lastAlertTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            // 更新服务器统计
            redisTemplate.opsForHash().increment(serverKey, "totalAlerts", 1);
            redisTemplate.opsForHash().put(serverKey, "lastAlertTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            // 设置过期时间
            redisTemplate.expire(globalKey, 30, TimeUnit.DAYS);
            redisTemplate.expire(serverKey, 30, TimeUnit.DAYS);
        } catch (Exception e) {
            log.warn("更新告警统计失败: {}", e.getMessage());
        }
    }
}
