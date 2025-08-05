package com.chua.starter.monitor.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.starter.entity.MonitorSysGenServerSetting;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 告警服务接口
 *
 * @author CH
 * @since 2024/12/27
 */
public interface AlertService {

    /**
     * 检查CPU告警
     *
     * @param serverId 服务器ID
     * @param cpuUsage CPU使用率
     * @return 检查结果
     */
    ReturnResult<Boolean> checkCpuAlert(Integer serverId, BigDecimal cpuUsage);

    /**
     * 检查内存告警
     *
     * @param serverId    服务器ID
     * @param memoryUsage 内存使用率
     * @return 检查结果
     */
    ReturnResult<Boolean> checkMemoryAlert(Integer serverId, BigDecimal memoryUsage);

    /**
     * 检查磁盘告警
     *
     * @param serverId  服务器ID
     * @param diskUsage 磁盘使用率
     * @return 检查结果
     */
    ReturnResult<Boolean> checkDiskAlert(Integer serverId, BigDecimal diskUsage);

    /**
     * 检查网络告警
     *
     * @param serverId     服务器ID
     * @param networkUsage 网络使用率
     * @return 检查结果
     */
    ReturnResult<Boolean> checkNetworkAlert(Integer serverId, BigDecimal networkUsage);

    /**
     * 检查响应时间告警
     *
     * @param serverId     服务器ID
     * @param responseTime 响应时间（毫秒）
     * @return 检查结果
     */
    ReturnResult<Boolean> checkResponseTimeAlert(Integer serverId, Long responseTime);

    /**
     * 批量检查服务器指标告警
     *
     * @param serverId 服务器ID
     * @param metrics  指标数据
     * @return 检查结果
     */
    ReturnResult<Boolean> checkServerMetricsAlert(Integer serverId, Map<String, Object> metrics);

    /**
     * 发送告警通知
     *
     * @param serverId       服务器ID
     * @param alertType      告警类型
     * @param alertMessage   告警消息
     * @param currentValue   当前值
     * @param thresholdValue 阈值
     * @return 发送结果
     */
    ReturnResult<Boolean> sendAlert(Integer serverId, String alertType, String alertMessage,
                                    Object currentValue, Object thresholdValue);

    /**
     * 发送恢复通知
     *
     * @param serverId        服务器ID
     * @param alertType       告警类型
     * @param recoveryMessage 恢复消息
     * @return 发送结果
     */
    ReturnResult<Boolean> sendRecoveryNotification(Integer serverId, String alertType, String recoveryMessage);

    /**
     * 测试告警配置
     *
     * @param setting 服务器配置
     * @return 测试结果
     */
    ReturnResult<Boolean> testAlertConfiguration(MonitorSysGenServerSetting setting);

    /**
     * 检查告警静默状态
     *
     * @param serverId  服务器ID
     * @param alertType 告警类型
     * @return 是否在静默期
     */
    boolean isInSilencePeriod(Integer serverId, String alertType);

    /**
     * 设置告警静默
     *
     * @param serverId        服务器ID
     * @param alertType       告警类型
     * @param silenceDuration 静默时长（分钟）
     * @return 设置结果
     */
    ReturnResult<Boolean> setAlertSilence(Integer serverId, String alertType, Integer silenceDuration);

    /**
     * 取消告警静默
     *
     * @param serverId  服务器ID
     * @param alertType 告警类型
     * @return 取消结果
     */
    ReturnResult<Boolean> cancelAlertSilence(Integer serverId, String alertType);

    /**
     * 获取告警统计信息
     *
     * @param serverId 服务器ID（可选）
     * @return 统计信息
     */
    ReturnResult<Map<String, Object>> getAlertStatistics(Integer serverId);

    /**
     * 获取告警历史
     *
     * @param serverId 服务器ID
     * @param limit    限制数量
     * @return 告警历史
     */
    ReturnResult<Map<String, Object>> getAlertHistory(Integer serverId, Integer limit);
}
