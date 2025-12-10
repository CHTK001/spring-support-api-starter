package com.chua.starter.monitor.service.alert;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.entity.MonitorSysGenServerMetrics;
import com.chua.starter.monitor.entity.MonitorSysGenServerSetting;

import java.util.Map;

/**
 * 统一告警服务接口
 * 负责统一管理所有告警相关功能，包括配置检查、告警检测、推送等
 * 
 * @author CH
 * @since 2024/12/25
 */
public interface UnifiedAlertService {

    /**
     * 检查并推送服务器指标告警
     * 只有在服务器配置中启用告警功能时才进行检测和推送
     * 
     * @param server  服务器信息
     * @param metrics 指标数据
     * @return 是否检测到告警并推送
     */
    ReturnResult<Boolean> checkAndPushServerAlerts(MonitorSysGenServer server, MonitorSysGenServerMetrics metrics);

    /**
     * 降级重载：携带已缓存的配置，避免再次访问DB
     */
    ReturnResult<Boolean> checkAndPushServerAlerts(MonitorSysGenServer server,
                                                   MonitorSysGenServerMetrics metrics,
                                                   MonitorSysGenServerSetting setting);

    /**
     * 检查并推送API上报数据告警
     * 只有在服务器配置中启用告警功能时才进行检测和推送
     * 
     * @param serverId   服务器ID
     * @param reportData API上报的数据
     * @return 是否检测到告警并推送
     */
    ReturnResult<Boolean> checkAndPushApiReportAlerts(Integer serverId, Map<String, Object> reportData);

    /**
     * 检查并推送连接告警
     * 只有在服务器配置中启用告警功能时才进行检测和推送
     * 
     * @param serverId     服务器ID
     * @param isOnline     是否在线
     * @param responseTime 响应时间
     * @return 是否检测到告警并推送
     */
    ReturnResult<Boolean> checkAndPushConnectionAlerts(Integer serverId, Boolean isOnline, Long responseTime);

    /**
     * 降级重载：携带已缓存的配置，避免再次访问DB
     */
    ReturnResult<Boolean> checkAndPushConnectionAlerts(Integer serverId, Boolean isOnline, Long responseTime, MonitorSysGenServerSetting setting);

    /**
     * 推送所有服务器的告警信息
     * 只推送启用告警功能的服务器的告警信息
     * 由定时任务调用
     */
    void pushAllServerAlerts();
}
