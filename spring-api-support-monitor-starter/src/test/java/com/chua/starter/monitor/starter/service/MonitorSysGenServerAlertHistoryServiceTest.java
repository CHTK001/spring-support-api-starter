package com.chua.starter.monitor.starter.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerAlertHistory;
import com.chua.starter.monitor.starter.enums.AlertLevel;
import com.chua.starter.monitor.starter.enums.AlertStatus;
import com.chua.starter.monitor.starter.enums.AlertType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 告警历史服务测试类
 *
 * @author CH
 * @since 2024/12/27
 */
@ExtendWith(MockitoExtension.class)
@SpringBootTest
@SpringJUnitConfig
public class MonitorSysGenServerAlertHistoryServiceTest {

    private static final Logger log = LoggerFactory.getLogger(MonitorSysGenServerAlertHistoryServiceTest.class);

    @Resource
    private MonitorSysGenServerAlertHistoryService alertHistoryService;

    @Test
    public void testSaveAlert() {
        // 创建测试告警记录
        MonitorSysGenServerAlertHistory alert = createTestAlert();
        
        // 保存告警记录
        ReturnResult<Boolean> result = alertHistoryService.saveAlert(alert);
        
        // 验证结果
        assertTrue(result.isOk());
        assertTrue(result.getData());
        
        log.info("保存告警记录测试通过");
    }

    @Test
    public void testGetAlertsByServerId() {
        // 测试根据服务器ID获取告警
        ReturnResult<List<MonitorSysGenServerAlertHistory>> result = 
            alertHistoryService.getAlertsByServerId(1, 10);
        
        // 验证结果
        assertTrue(result.isOk());
        assertNotNull(result.getData());
        
        log.info("根据服务器ID获取告警测试通过，获取到 {} 条记录", result.getData().size());
    }

    @Test
    public void testRecoverAlert() {
        // 创建并保存测试告警
        MonitorSysGenServerAlertHistory alert = createTestAlert();
        ReturnResult<Boolean> saveResult = alertHistoryService.saveAlert(alert);
        assertTrue(saveResult.isOk());
        
        // 恢复告警
        ReturnResult<Boolean> recoverResult = alertHistoryService.recoverAlert(alert.getMonitorSysGenServerAlertHistoryId().intValue());
        
        // 验证结果
        assertTrue(recoverResult.isOk());
        
        log.info("恢复告警测试通过");
    }

    @Test
    public void testIgnoreAlert() {
        // 创建并保存测试告警
        MonitorSysGenServerAlertHistory alert = createTestAlert();
        ReturnResult<Boolean> saveResult = alertHistoryService.saveAlert(alert);
        assertTrue(saveResult.isOk());
        
        // 忽略告警
        ReturnResult<Boolean> ignoreResult = alertHistoryService.ignoreAlert(alert.getMonitorSysGenServerAlertHistoryId().intValue());
        
        // 验证结果
        assertTrue(ignoreResult.isOk());
        
        log.info("忽略告警测试通过");
    }

    @Test
    public void testBatchRecoverAlerts() {
        // 创建测试告警ID列表
        List<Integer> alertIds = Arrays.asList(1, 2, 3);
        
        // 批量恢复告警
        ReturnResult<Boolean> result = alertHistoryService.batchRecoverAlerts(alertIds);
        
        // 验证结果
        assertTrue(result.isOk());
        
        log.info("批量恢复告警测试通过");
    }

    @Test
    public void testCountAlerts() {
        // 统计告警数量
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();
        
        ReturnResult<Long> result = alertHistoryService.countAlerts(
            1, AlertType.CPU, AlertLevel.WARNING, AlertStatus.ACTIVE, startTime, endTime);
        
        // 验证结果
        assertTrue(result.isOk());
        assertNotNull(result.getData());
        
        log.info("统计告警数量测试通过，统计结果: {}", result.getData());
    }

    @Test
    public void testGetAlertStatistics() {
        // 获取告警统计信息
        ReturnResult<Map<String, Object>> result = alertHistoryService.getAlertStatistics(1, 7);
        
        // 验证结果
        assertTrue(result.isOk());
        assertNotNull(result.getData());
        
        log.info("获取告警统计信息测试通过，统计结果: {}", result.getData());
    }

    @Test
    public void testUpdateNotificationStatus() {
        // 创建并保存测试告警
        MonitorSysGenServerAlertHistory alert = createTestAlert();
        ReturnResult<Boolean> saveResult = alertHistoryService.saveAlert(alert);
        assertTrue(saveResult.isOk());
        
        // 更新通知状态
        ReturnResult<Boolean> updateResult = alertHistoryService.updateNotificationStatus(
            alert.getMonitorSysGenServerAlertHistoryId().intValue(), true, "EMAIL", "test@example.com");
        
        // 验证结果
        assertTrue(updateResult.isOk());
        
        log.info("更新通知状态测试通过");
    }

    /**
     * 创建测试告警记录
     */
    private MonitorSysGenServerAlertHistory createTestAlert() {
        MonitorSysGenServerAlertHistory alert = new MonitorSysGenServerAlertHistory();
        alert.setMonitorSysGenServerAlertHistoryServerId(1);
        alert.setMonitorSysGenServerAlertHistoryType(AlertType.CPU);
        alert.setMonitorSysGenServerAlertHistoryLevel(AlertLevel.WARNING);
        alert.setMonitorSysGenServerAlertHistoryStatus(AlertStatus.TRIGGERED);
        alert.setMonitorSysGenServerAlertHistoryTitle("CPU使用率告警");
        alert.setMonitorSysGenServerAlertHistoryContent("CPU使用率超过阈值");
        alert.setMonitorSysGenServerAlertHistoryCurrentValue(new BigDecimal("85.5"));
        alert.setMonitorSysGenServerAlertHistoryThresholdValue(new BigDecimal("80.0"));
        alert.setMonitorSysGenServerAlertHistoryUnit("%");
        alert.setMonitorSysGenServerAlertHistoryTriggerTime(LocalDateTime.now());
        alert.setMonitorSysGenServerAlertHistoryFingerprint("test-fingerprint-" + System.currentTimeMillis());
        alert.setMonitorSysGenServerAlertHistoryNotificationSent(0);
        alert.setCreateTime(LocalDateTime.now());
        alert.setUpdateTime(LocalDateTime.now());
        alert.setDelFlag(0);
        
        return alert;
    }
}
