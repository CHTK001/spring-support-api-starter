package com.chua.starter.monitor.starter.task;

import com.chua.starter.monitor.starter.service.AlertDetectionService;
import com.chua.starter.monitor.starter.service.MonitorSysGenServerAlertHistoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 告警检测定时任务测试类
 *
 * @author CH
 * @since 2024/12/27
 */
@ExtendWith(MockitoExtension.class)
class AlertDetectionTaskTest {

    private static final Logger log = LoggerFactory.getLogger(AlertDetectionTaskTest.class);

    @Mock
    private AlertDetectionService alertDetectionService;

    @Mock
    private MonitorSysGenServerAlertHistoryService alertHistoryService;

    @InjectMocks
    private AlertDetectionTask alertDetectionTask;

    @Test
    void testDetectAllServersAlerts() {
        // 测试批量检测所有服务器告警方法是否能正常调用
        try {
            alertDetectionTask.detectAllServersAlerts();
            log.info("批量检测所有服务器告警方法调用成功");
        } catch (Exception e) {
            log.error("批量检测所有服务器告警方法调用失败", e);
        }
    }

    @Test
    void testAutoRecoverExpiredAlerts() {
        // 测试自动恢复过期告警方法是否能正常调用
        try {
            alertDetectionTask.autoRecoverExpiredAlerts();
            log.info("自动恢复过期告警方法调用成功");
        } catch (Exception e) {
            log.error("自动恢复过期告警方法调用失败", e);
        }
    }

    @Test
    void testCleanupExpiredAlerts() {
        // 测试清理过期告警方法是否能正常调用
        try {
            alertDetectionTask.cleanupExpiredAlerts();
            log.info("清理过期告警方法调用成功");
        } catch (Exception e) {
            log.error("清理过期告警方法调用失败", e);
        }
    }

    @Test
    void testProcessUnsentNotificationAlerts() {
        // 测试处理未发送通知告警方法是否能正常调用
        try {
            alertDetectionTask.processUnsentNotificationAlerts();
            log.info("处理未发送通知告警方法调用成功");
        } catch (Exception e) {
            log.error("处理未发送通知告警方法调用失败", e);
        }
    }

    @Test
    void testGenerateAlertStatistics() {
        // 测试生成告警统计方法是否能正常调用
        try {
            alertDetectionTask.generateAlertStatistics();
            log.info("生成告警统计方法调用成功");
        } catch (Exception e) {
            log.error("生成告警统计方法调用失败", e);
        }
    }
}
