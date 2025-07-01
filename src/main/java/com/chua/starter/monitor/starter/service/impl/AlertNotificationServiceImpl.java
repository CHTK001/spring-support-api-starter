package com.chua.starter.monitor.starter.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerAlertHistory;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.starter.service.AlertNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 告警通知服务实现类
 * 支持多种通知方式：邮件、短信、Webhook、钉钉、微信等
 * 
 * @author CH
 * @since 2024/12/25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertNotificationServiceImpl implements AlertNotificationService {

    private final JavaMailSender mailSender;
    private final RestTemplate restTemplate;

    @Override
    public ReturnResult<Boolean> sendNotification(MonitorSysGenServerAlertHistory alert, MonitorSysGenServerSetting setting) {
        try {
            String notificationMethod = setting.getMonitorSysGenServerSettingAlertNotificationMethod();
            String notificationAddress = setting.getMonitorSysGenServerSettingAlertNotificationAddress();

            if (!StringUtils.hasText(notificationMethod) || !StringUtils.hasText(notificationAddress)) {
                return ReturnResult.error("告警通知配置不完整");
            }

            boolean success = false;
            String[] methods = notificationMethod.split(",");
            String[] addresses = notificationAddress.split(",");

            for (int i = 0; i < methods.length && i < addresses.length; i++) {
                String method = methods[i].trim().toUpperCase();
                String address = addresses[i].trim();

                switch (method) {
                    case "EMAIL":
                        success = sendEmailNotification(alert, address) || success;
                        break;
                    case "SMS":
                        success = sendSmsNotification(alert, address) || success;
                        break;
                    case "WEBHOOK":
                        success = sendWebhookNotification(alert, address) || success;
                        break;
                    case "DINGTALK":
                        success = sendDingTalkNotification(alert, address) || success;
                        break;
                    case "WECHAT":
                        success = sendWeChatNotification(alert, address) || success;
                        break;
                    default:
                        log.warn("不支持的通知方式: {}", method);
                }
            }

            return success ? ReturnResult.success(true) : ReturnResult.error("所有通知方式都发送失败");

        } catch (Exception e) {
            log.error("发送告警通知失败", e);
            return ReturnResult.error("发送通知失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> sendRecoveryNotification(MonitorSysGenServerAlertHistory alert, MonitorSysGenServerSetting setting) {
        try {
            // 检查是否启用恢复通知
            if (setting.getMonitorSysGenServerSettingAutoRecoveryNotificationEnabled() == null ||
                setting.getMonitorSysGenServerSettingAutoRecoveryNotificationEnabled() != 1) {
                log.debug("恢复通知未启用，跳过发送");
                return ReturnResult.success(false);
            }

            // 创建恢复通知的告警对象
            MonitorSysGenServerAlertHistory recoveryAlert = createRecoveryAlert(alert);
            
            return sendNotification(recoveryAlert, setting);

        } catch (Exception e) {
            log.error("发送恢复通知失败", e);
            return ReturnResult.error("发送恢复通知失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> testNotificationConfiguration(MonitorSysGenServerSetting setting) {
        try {
            // 创建测试告警
            MonitorSysGenServerAlertHistory testAlert = createTestAlert(setting);
            
            return sendNotification(testAlert, setting);

        } catch (Exception e) {
            log.error("测试通知配置失败", e);
            return ReturnResult.error("测试通知配置失败: " + e.getMessage());
        }
    }

    /**
     * 发送邮件通知
     */
    private boolean sendEmailNotification(MonitorSysGenServerAlertHistory alert, String emailAddress) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(emailAddress);
            message.setSubject(buildEmailSubject(alert));
            message.setText(buildEmailContent(alert));

            mailSender.send(message);
            log.info("邮件告警通知发送成功: {}", emailAddress);
            return true;

        } catch (Exception e) {
            log.error("发送邮件告警通知失败: {}", emailAddress, e);
            return false;
        }
    }

    /**
     * 发送短信通知
     */
    private boolean sendSmsNotification(MonitorSysGenServerAlertHistory alert, String phoneNumber) {
        try {
            // TODO: 集成短信服务提供商API
            String smsContent = buildSmsContent(alert);
            
            log.info("模拟发送短信告警通知: {} -> {}", phoneNumber, smsContent);
            return true;

        } catch (Exception e) {
            log.error("发送短信告警通知失败: {}", phoneNumber, e);
            return false;
        }
    }

    /**
     * 发送Webhook通知
     */
    private boolean sendWebhookNotification(MonitorSysGenServerAlertHistory alert, String webhookUrl) {
        try {
            Map<String, Object> payload = buildWebhookPayload(alert);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(webhookUrl, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Webhook告警通知发送成功: {}", webhookUrl);
                return true;
            } else {
                log.warn("Webhook告警通知发送失败: {}, 状态码: {}", webhookUrl, response.getStatusCode());
                return false;
            }

        } catch (Exception e) {
            log.error("发送Webhook告警通知失败: {}", webhookUrl, e);
            return false;
        }
    }

    /**
     * 发送钉钉通知
     */
    private boolean sendDingTalkNotification(MonitorSysGenServerAlertHistory alert, String webhookUrl) {
        try {
            Map<String, Object> payload = buildDingTalkPayload(alert);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(webhookUrl, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("钉钉告警通知发送成功: {}", webhookUrl);
                return true;
            } else {
                log.warn("钉钉告警通知发送失败: {}, 状态码: {}", webhookUrl, response.getStatusCode());
                return false;
            }

        } catch (Exception e) {
            log.error("发送钉钉告警通知失败: {}", webhookUrl, e);
            return false;
        }
    }

    /**
     * 发送微信通知
     */
    private boolean sendWeChatNotification(MonitorSysGenServerAlertHistory alert, String webhookUrl) {
        try {
            Map<String, Object> payload = buildWeChatPayload(alert);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(webhookUrl, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("微信告警通知发送成功: {}", webhookUrl);
                return true;
            } else {
                log.warn("微信告警通知发送失败: {}, 状态码: {}", webhookUrl, response.getStatusCode());
                return false;
            }

        } catch (Exception e) {
            log.error("发送微信告警通知失败: {}", webhookUrl, e);
            return false;
        }
    }

    /**
     * 构建邮件主题
     */
    private String buildEmailSubject(MonitorSysGenServerAlertHistory alert) {
        return String.format("[%s] %s - %s", 
                alert.getMonitorSysGenServerAlertHistoryLevel().getDescription(),
                alert.getMonitorSysGenServerAlertHistoryTitle(),
                alert.getMonitorSysGenServerAlertHistoryType().getDescription());
    }

    /**
     * 构建邮件内容
     */
    private String buildEmailContent(MonitorSysGenServerAlertHistory alert) {
        return String.format(
                "告警详情：\n\n" +
                "服务器ID：%d\n" +
                "告警类型：%s\n" +
                "告警级别：%s\n" +
                "告警标题：%s\n" +
                "告警描述：%s\n" +
                "当前值：%s%s\n" +
                "阈值：%s%s\n" +
                "触发时间：%s\n\n" +
                "请及时处理！",
                alert.getMonitorSysGenServerAlertHistoryServerId(),
                alert.getMonitorSysGenServerAlertHistoryType().getDescription(),
                alert.getMonitorSysGenServerAlertHistoryLevel().getDescription(),
                alert.getMonitorSysGenServerAlertHistoryTitle(),
                alert.getMonitorSysGenServerAlertHistoryMessage(),
                alert.getMonitorSysGenServerAlertHistoryCurrentValue(),
                alert.getMonitorSysGenServerAlertHistoryUnit(),
                alert.getMonitorSysGenServerAlertHistoryThresholdValue(),
                alert.getMonitorSysGenServerAlertHistoryUnit(),
                alert.getMonitorSysGenServerAlertHistoryTriggerTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
    }

    /**
     * 构建短信内容
     */
    private String buildSmsContent(MonitorSysGenServerAlertHistory alert) {
        return String.format("[%s]服务器%d %s：%s，当前值%s%s，阈值%s%s，时间%s",
                alert.getMonitorSysGenServerAlertHistoryLevel().getDescription(),
                alert.getMonitorSysGenServerAlertHistoryServerId(),
                alert.getMonitorSysGenServerAlertHistoryType().getDescription(),
                alert.getMonitorSysGenServerAlertHistoryTitle(),
                alert.getMonitorSysGenServerAlertHistoryCurrentValue(),
                alert.getMonitorSysGenServerAlertHistoryUnit(),
                alert.getMonitorSysGenServerAlertHistoryThresholdValue(),
                alert.getMonitorSysGenServerAlertHistoryUnit(),
                alert.getMonitorSysGenServerAlertHistoryTriggerTime().format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))
        );
    }

    /**
     * 构建Webhook载荷
     */
    private Map<String, Object> buildWebhookPayload(MonitorSysGenServerAlertHistory alert) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("serverId", alert.getMonitorSysGenServerAlertHistoryServerId());
        payload.put("alertType", alert.getMonitorSysGenServerAlertHistoryType().getCode());
        payload.put("alertLevel", alert.getMonitorSysGenServerAlertHistoryLevel().getCode());
        payload.put("title", alert.getMonitorSysGenServerAlertHistoryTitle());
        payload.put("message", alert.getMonitorSysGenServerAlertHistoryMessage());
        payload.put("currentValue", alert.getMonitorSysGenServerAlertHistoryCurrentValue());
        payload.put("thresholdValue", alert.getMonitorSysGenServerAlertHistoryThresholdValue());
        payload.put("unit", alert.getMonitorSysGenServerAlertHistoryUnit());
        payload.put("triggerTime", alert.getMonitorSysGenServerAlertHistoryTriggerTime());
        payload.put("fingerprint", alert.getMonitorSysGenServerAlertHistoryFingerprint());
        return payload;
    }

    /**
     * 构建钉钉载荷
     */
    private Map<String, Object> buildDingTalkPayload(MonitorSysGenServerAlertHistory alert) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("msgtype", "text");
        
        Map<String, Object> text = new HashMap<>();
        text.put("content", String.format(
                "【监控告警】\n" +
                "服务器：%d\n" +
                "类型：%s\n" +
                "级别：%s\n" +
                "标题：%s\n" +
                "描述：%s\n" +
                "当前值：%s%s\n" +
                "阈值：%s%s\n" +
                "时间：%s",
                alert.getMonitorSysGenServerAlertHistoryServerId(),
                alert.getMonitorSysGenServerAlertHistoryType().getDescription(),
                alert.getMonitorSysGenServerAlertHistoryLevel().getDescription(),
                alert.getMonitorSysGenServerAlertHistoryTitle(),
                alert.getMonitorSysGenServerAlertHistoryMessage(),
                alert.getMonitorSysGenServerAlertHistoryCurrentValue(),
                alert.getMonitorSysGenServerAlertHistoryUnit(),
                alert.getMonitorSysGenServerAlertHistoryThresholdValue(),
                alert.getMonitorSysGenServerAlertHistoryUnit(),
                alert.getMonitorSysGenServerAlertHistoryTriggerTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        ));
        
        payload.put("text", text);
        return payload;
    }

    /**
     * 构建微信载荷
     */
    private Map<String, Object> buildWeChatPayload(MonitorSysGenServerAlertHistory alert) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("msgtype", "text");
        
        Map<String, Object> text = new HashMap<>();
        text.put("content", String.format(
                "【监控告警】\n" +
                "服务器：%d\n" +
                "类型：%s\n" +
                "级别：%s\n" +
                "标题：%s\n" +
                "当前值：%s%s（阈值：%s%s）\n" +
                "时间：%s",
                alert.getMonitorSysGenServerAlertHistoryServerId(),
                alert.getMonitorSysGenServerAlertHistoryType().getDescription(),
                alert.getMonitorSysGenServerAlertHistoryLevel().getDescription(),
                alert.getMonitorSysGenServerAlertHistoryTitle(),
                alert.getMonitorSysGenServerAlertHistoryCurrentValue(),
                alert.getMonitorSysGenServerAlertHistoryUnit(),
                alert.getMonitorSysGenServerAlertHistoryThresholdValue(),
                alert.getMonitorSysGenServerAlertHistoryUnit(),
                alert.getMonitorSysGenServerAlertHistoryTriggerTime().format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))
        ));
        
        payload.put("text", text);
        return payload;
    }

    /**
     * 创建恢复通知的告警对象
     */
    private MonitorSysGenServerAlertHistory createRecoveryAlert(MonitorSysGenServerAlertHistory originalAlert) {
        MonitorSysGenServerAlertHistory recoveryAlert = new MonitorSysGenServerAlertHistory();
        recoveryAlert.setMonitorSysGenServerAlertHistoryServerId(originalAlert.getMonitorSysGenServerAlertHistoryServerId());
        recoveryAlert.setMonitorSysGenServerAlertHistoryType(originalAlert.getMonitorSysGenServerAlertHistoryType());
        recoveryAlert.setMonitorSysGenServerAlertHistoryLevel(originalAlert.getMonitorSysGenServerAlertHistoryLevel());
        recoveryAlert.setMonitorSysGenServerAlertHistoryTitle("【恢复】" + originalAlert.getMonitorSysGenServerAlertHistoryTitle());
        recoveryAlert.setMonitorSysGenServerAlertHistoryMessage("告警已恢复正常");
        recoveryAlert.setMonitorSysGenServerAlertHistoryCurrentValue(originalAlert.getMonitorSysGenServerAlertHistoryCurrentValue());
        recoveryAlert.setMonitorSysGenServerAlertHistoryThresholdValue(originalAlert.getMonitorSysGenServerAlertHistoryThresholdValue());
        recoveryAlert.setMonitorSysGenServerAlertHistoryUnit(originalAlert.getMonitorSysGenServerAlertHistoryUnit());
        recoveryAlert.setMonitorSysGenServerAlertHistoryTriggerTime(originalAlert.getMonitorSysGenServerAlertHistoryTriggerTime());
        recoveryAlert.setMonitorSysGenServerAlertHistoryFingerprint(originalAlert.getMonitorSysGenServerAlertHistoryFingerprint());
        return recoveryAlert;
    }

    /**
     * 创建测试告警对象
     */
    private MonitorSysGenServerAlertHistory createTestAlert(MonitorSysGenServerSetting setting) {
        MonitorSysGenServerAlertHistory testAlert = new MonitorSysGenServerAlertHistory();
        testAlert.setMonitorSysGenServerAlertHistoryServerId(setting.getMonitorSysGenServerId());
        testAlert.setMonitorSysGenServerAlertHistoryTitle("测试告警通知");
        testAlert.setMonitorSysGenServerAlertHistoryMessage("这是一条测试告警通知，用于验证通知配置是否正确");
        testAlert.setMonitorSysGenServerAlertHistoryCurrentValue("100");
        testAlert.setMonitorSysGenServerAlertHistoryThresholdValue("80");
        testAlert.setMonitorSysGenServerAlertHistoryUnit("%");
        testAlert.setMonitorSysGenServerAlertHistoryTriggerTime(java.time.LocalDateTime.now());
        testAlert.setMonitorSysGenServerAlertHistoryFingerprint("test-alert-" + System.currentTimeMillis());
        return testAlert;
    }
}
