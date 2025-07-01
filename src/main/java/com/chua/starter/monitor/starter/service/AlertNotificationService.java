package com.chua.starter.monitor.starter.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerAlertHistory;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSetting;

/**
 * 告警通知服务接口
 * 支持多种通知方式：邮件、短信、Webhook、钉钉、微信等
 * 
 * @author CH
 * @since 2024/12/25
 */
public interface AlertNotificationService {

    /**
     * 发送告警通知
     * 支持多种通知方式，根据配置自动选择合适的通知渠道
     * 
     * @param alert   告警信息
     * @param setting 服务器配置（包含通知方式和地址）
     * @return 发送结果
     */
    ReturnResult<Boolean> sendNotification(MonitorSysGenServerAlertHistory alert, MonitorSysGenServerSetting setting);

    /**
     * 发送恢复通知
     * 当告警恢复正常时发送通知
     * 
     * @param alert   原始告警信息
     * @param setting 服务器配置
     * @return 发送结果
     */
    ReturnResult<Boolean> sendRecoveryNotification(MonitorSysGenServerAlertHistory alert, MonitorSysGenServerSetting setting);

    /**
     * 测试通知配置
     * 发送测试通知以验证配置是否正确
     * 
     * @param setting 服务器配置
     * @return 测试结果
     */
    ReturnResult<Boolean> testNotificationConfiguration(MonitorSysGenServerSetting setting);
}
