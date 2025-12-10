package com.chua.starter.monitor.service.server;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chua.common.support.lang.code.ReturnPageResult;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenServerAlertHistory;
import com.chua.starter.monitor.enums.AlertLevel;
import com.chua.starter.monitor.enums.AlertStatus;
import com.chua.starter.monitor.enums.AlertType;
import com.chua.starter.mybatis.entity.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 告警历史服务接口
 *
 * @author CH
 * @since 2024/12/27
 */
public interface MonitorSysGenServerAlertHistoryService extends IService<MonitorSysGenServerAlertHistory> {

    /**
     * 保存告警记录
     *
     * @param alert 告警记录
     * @return 保存结果
     */
    ReturnResult<Boolean> saveAlert(MonitorSysGenServerAlertHistory alert);

    /**
     * 分页查询告警历史
     *
     * @param query 查询条件
     * @return 分页结果
     */
    ReturnPageResult<MonitorSysGenServerAlertHistory> pageAlerts(Query<MonitorSysGenServerAlertHistory> query);

    /**
     * 根据服务器ID查询告警历史
     *
     * @param serverId 服务器ID
     * @param limit    限制数量
     * @return 告警历史列表
     */
    ReturnResult<List<MonitorSysGenServerAlertHistory>> getAlertsByServerId(Integer serverId, Integer limit);

    /**
     * 恢复告警
     *
     * @param alertId 告警ID
     * @return 恢复结果
     */
    ReturnResult<Boolean> recoverAlert(Integer alertId);

    /**
     * 批量恢复告警
     *
     * @param alertIds 告警ID列表
     * @return 恢复结果
     */
    ReturnResult<Boolean> batchRecoverAlerts(List<Integer> alertIds);

    /**
     * 忽略告警
     *
     * @param alertId 告警ID
     * @return 忽略结果
     */
    ReturnResult<Boolean> ignoreAlert(Integer alertId);

    /**
     * 批量忽略告警
     *
     * @param alertIds 告警ID列表
     * @return 忽略结果
     */
    ReturnResult<Boolean> batchIgnoreAlerts(List<Integer> alertIds);

    /**
     * 静默告警
     *
     * @param alertId        告警ID
     * @param silenceMinutes 静默时长（分钟）
     * @return 静默结果
     */
    ReturnResult<Boolean> silenceAlert(Integer alertId, Integer silenceMinutes);

    /**
     * 取消静默
     *
     * @param alertId 告警ID
     * @return 取消结果
     */
    ReturnResult<Boolean> unsilenceAlert(Integer alertId);

    /**
     * 根据服务器ID获取活跃告警
     *
     * @param serverId 服务器ID
     * @return 活跃告警列表
     */
    ReturnResult<List<MonitorSysGenServerAlertHistory>> getActiveAlertsByServerId(Integer serverId);

    /**
     * 根据指纹获取最新告警
     *
     * @param fingerprint 告警指纹
     * @return 最新告警
     */
    ReturnResult<MonitorSysGenServerAlertHistory> getLatestAlertByFingerprint(String fingerprint);

    /**
     * 根据指纹获取指定时间内的告警
     *
     * @param fingerprint 告警指纹
     * @param minutes     时间范围（分钟）
     * @return 告警列表
     */
    ReturnResult<List<MonitorSysGenServerAlertHistory>> getAlertsByFingerprintInTimeRange(
            String fingerprint, Integer minutes);

    /**
     * 统计告警数量
     *
     * @param serverId   服务器ID
     * @param alertType  告警类型
     * @param alertLevel 告警级别
     * @param status     告警状态
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 告警数量
     */
    ReturnResult<Long> countAlerts(Integer serverId, AlertType alertType, AlertLevel alertLevel,
                                   AlertStatus status, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 清理过期告警记录
     *
     * @param expireDays 过期天数
     * @return 清理的记录数
     */
    ReturnResult<Integer> cleanupExpiredAlerts(Integer expireDays);

    /**
     * 更新告警通知状态
     *
     * @param alertId 告警ID
     * @param sent    是否已发送
     * @param method  通知方式
     * @param address 通知地址
     * @return 更新结果
     */
    ReturnResult<Boolean> updateNotificationStatus(Integer alertId, Boolean sent, String method, String address);

    /**
     * 获取未发送通知的告警
     *
     * @param limit 限制数量
     * @return 未发送通知的告警列表
     */
    ReturnResult<List<MonitorSysGenServerAlertHistory>> getUnsentNotificationAlerts(Integer limit);

    /**
     * 自动恢复过期的活跃告警
     *
     * @param expireMinutes 过期时间（分钟）
     * @return 恢复的告警数量
     */
    ReturnResult<Integer> autoRecoverExpiredAlerts(Integer expireMinutes);

    /**
     * 批量更新告警状态
     *
     * @param alertIds 告警ID列表
     * @param status   新状态
     * @return 更新结果
     */
    ReturnResult<Integer> batchUpdateAlertStatus(List<Long> alertIds, AlertStatus status);

    /**
     * 获取告警详情
     *
     * @param alertId 告警ID
     * @return 告警详情
     */
    ReturnResult<MonitorSysGenServerAlertHistory> getAlertDetail(Long alertId);

    /**
     * 更新告警状态
     *
     * @param alertId 告警ID
     * @param status  新状态
     * @return 更新结果
     */
    ReturnResult<Boolean> updateAlertStatus(Long alertId, AlertStatus status);

    /**
     * 忽略告警（带原因）
     *
     * @param alertId 告警ID
     * @param reason  忽略原因
     * @return 忽略结果
     */
    ReturnResult<Boolean> ignoreAlert(Long alertId, String reason);

    /**
     * 获取告警统计信息（按天数）
     *
     * @param serverId 服务器ID
     * @param days     统计天数
     * @return 统计信息
     */
    ReturnResult<Map<String, Object>> getAlertStatistics(Integer serverId, Integer days);
}
