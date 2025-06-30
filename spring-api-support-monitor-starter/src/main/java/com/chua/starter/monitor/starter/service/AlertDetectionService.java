package com.chua.starter.monitor.starter.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerAlertHistory;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServer;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerMetrics;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.starter.enums.AlertLevel;
import com.chua.starter.monitor.starter.enums.AlertType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 告警检测服务接口
 *
 * @author CH
 * @since 2024/12/27
 */
public interface AlertDetectionService {

    /**
     * 检测服务器指标告警
     *
     * @param server  服务器信息
     * @param setting 服务器配置
     * @param metrics 指标数据
     * @return 检测到的告警列表
     */
    ReturnResult<List<MonitorSysGenServerAlertHistory>> detectMetricsAlerts(
            MonitorSysGenServer server,
            MonitorSysGenServerSetting setting,
            MonitorSysGenServerMetrics metrics);

    /**
     * 检测单个告警
     *
     * @param serverId       服务器ID
     * @param alertType      告警类型
     * @param currentValue   当前值
     * @param thresholdValue 阈值
     * @param unit           单位
     * @return 检测到的告警
     */
    ReturnResult<MonitorSysGenServerAlertHistory> detectSingleAlert(
            Integer serverId,
            AlertType alertType,
            BigDecimal currentValue,
            BigDecimal thresholdValue,
            String unit);

    /**
     * 检测API上报数据的告警
     *
     * @param serverId   服务器ID
     * @param reportData 上报的数据
     * @return 检测到的告警列表
     */
    ReturnResult<List<MonitorSysGenServerAlertHistory>> detectApiReportAlerts(
            Integer serverId,
            Map<String, Object> reportData);

    /**
     * 检测Redis中存储的指标数据告警
     *
     * @param serverId 服务器ID
     * @return 检测到的告警列表
     */
    ReturnResult<List<MonitorSysGenServerAlertHistory>> detectRedisMetricsAlerts(Integer serverId);

    /**
     * 批量检测所有服务器的告警
     *
     * @return 检测到的告警列表
     */
    ReturnResult<List<MonitorSysGenServerAlertHistory>> detectAllServersAlerts();

    /**
     * 检查是否为重复告警
     *
     * @param fingerprint       告警指纹
     * @param timeWindowMinutes 时间窗口（分钟）
     * @return 是否重复
     */
    boolean isDuplicateAlert(String fingerprint, Integer timeWindowMinutes);

    /**
     * 恢复指定类型的告警
     *
     * @param serverId  服务器ID
     * @param alertType 告警类型
     * @return 恢复的告警列表
     */
    ReturnResult<List<MonitorSysGenServerAlertHistory>> recoverAlerts(Integer serverId, AlertType alertType);

    /**
     * 恢复服务器的所有告警
     *
     * @param serverId 服务器ID
     * @return 恢复的告警列表
     */
    ReturnResult<List<MonitorSysGenServerAlertHistory>> recoverAllServerAlerts(Integer serverId);

    /**
     * 检测连接告警
     *
     * @param serverId     服务器ID
     * @param isOnline     是否在线
     * @param responseTime 响应时间
     * @return 检测到的告警
     */
    ReturnResult<MonitorSysGenServerAlertHistory> detectConnectionAlert(
            Integer serverId, Boolean isOnline, Long responseTime);

    /**
     * 清理过期的告警记录
     *
     * @param expireDays 过期天数
     * @return 清理的记录数
     */
    ReturnResult<Integer> cleanupExpiredAlerts(Integer expireDays);

    /**
     * 计算告警级别
     *
     * @param alertType      告警类型
     * @param currentValue   当前值
     * @param thresholdValue 阈值
     * @return 告警级别
     */
    AlertLevel calculateAlertLevel(AlertType alertType, BigDecimal currentValue, BigDecimal thresholdValue);

    /**
     * 生成告警指纹
     *
     * @param serverId   服务器ID
     * @param alertType  告警类型
     * @param alertLevel 告警级别
     * @return 告警指纹
     */
    String generateAlertFingerprint(Integer serverId, AlertType alertType, AlertLevel alertLevel);

    /**
     * 生成告警标题
     *
     * @param alertType  告警类型
     * @param alertLevel 告警级别
     * @return 告警标题
     */
    String generateAlertTitle(AlertType alertType, AlertLevel alertLevel);

    /**
     * 生成告警内容
     *
     * @param alertType      告警类型
     * @param currentValue   当前值
     * @param thresholdValue 阈值
     * @param unit           单位
     * @return 告警内容
     */
    String generateAlertContent(AlertType alertType, BigDecimal currentValue, BigDecimal thresholdValue, String unit);

    /**
     * 检查告警是否在静默期
     *
     * @param fingerprint 告警指纹
     * @return 是否在静默期
     */
    boolean isAlertSilenced(String fingerprint);
}
