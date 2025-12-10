package com.chua.starter.monitor.service.impl;

import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.socketio.support.constant.SocketTopics;
import com.chua.socket.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.entity.MonitorSysGenServerAlertHistory;
import com.chua.starter.monitor.entity.MonitorSysGenServerMetrics;
import com.chua.starter.monitor.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.message.ServerWebSocketMessage;
import com.chua.starter.monitor.service.alert.AlertDetectionService;
import com.chua.starter.monitor.service.MonitorSysGenServerService;
import com.chua.starter.monitor.service.MonitorSysGenServerSettingService;
import com.chua.starter.monitor.service.alert.UnifiedAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一告警服务实现类
 * 负责统一管理所有告警相关功能，包括配置检查、告警检测、推送等
 *
 * @author CH
 * @since 2024/12/25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UnifiedAlertServiceImpl implements UnifiedAlertService {

    private final AlertDetectionService alertDetectionService;
    private final MonitorSysGenServerService serverService;
    private final MonitorSysGenServerSettingService serverSettingService;
    private final SocketSessionTemplate socketSessionTemplate;
    private final com.chua.starter.monitor.service.AlertNotificationDispatcher alertNotificationDispatcher;


    @Override
    public ReturnResult<Boolean> checkAndPushServerAlerts(MonitorSysGenServer server, MonitorSysGenServerMetrics metrics) {
        try {
            // 检查服务器是否启用告警
            if (!isAlertEnabled(server)) {
                log.debug("服务器 {} 未启用告警功能，跳过告警检测", server.getMonitorSysGenServerName());
                return ReturnResult.success(false);
            }

            // 获取服务器配置（默认路径，先尝试只读本地缓存）
            ReturnResult<MonitorSysGenServerSetting> settingResult = serverSettingService.getFromLocalCache(server.getMonitorSysGenServerId());
            if (!settingResult.isOk() || settingResult.getData() == null) {
                settingResult = serverSettingService.getOrCreateSetting(server.getMonitorSysGenServerId());
            }

            if (!settingResult.isOk() || settingResult.getData() == null) {
                log.warn("获取服务器 {} 配置失败，跳过告警检测", server.getMonitorSysGenServerName());
                return ReturnResult.error("获取服务器配置失败");
            }

            MonitorSysGenServerSetting setting = settingResult.getData();

            // 检查配置中是否启用告警
            if (!isAlertEnabledInSetting(setting)) {
                log.debug("服务器 {} 配置中未启用告警功能，跳过告警检测", server.getMonitorSysGenServerName());
                return ReturnResult.success(false);
            }

            // 进行告警检测
            ReturnResult<List<MonitorSysGenServerAlertHistory>> alertResult =
                    alertDetectionService.detectMetricsAlerts(server, setting, metrics);

            if (alertResult.isOk() && alertResult.getData() != null && !alertResult.getData().isEmpty()) {
                // 推送告警信息
                pushAlerts(server, alertResult.getData());
                log.info("服务器 {} 检测到 {} 个告警并已推送",
                        server.getMonitorSysGenServerName(), alertResult.getData().size());
                return ReturnResult.success(true);
            }

            return ReturnResult.success(false);

        } catch (Exception e) {
            log.error("检查并推送服务器 {} 告警失败: {}",
                    server.getMonitorSysGenServerName(), e.getMessage(), e);
            return ReturnResult.error("告警检查失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> checkAndPushServerAlerts(MonitorSysGenServer server, MonitorSysGenServerMetrics metrics, MonitorSysGenServerSetting setting) {
        try {
            if (!isAlertEnabled(server)) {
                return ReturnResult.success(false);
            }
            MonitorSysGenServerSetting effective = setting;
            if (effective == null) {
                ReturnResult<MonitorSysGenServerSetting> rr = serverSettingService.getFromLocalCache(server.getMonitorSysGenServerId());
                if (!rr.isOk() || rr.getData() == null) {
                    rr = serverSettingService.getOrCreateSetting(server.getMonitorSysGenServerId());
                }
                if (!rr.isOk() || rr.getData() == null) {
                    return ReturnResult.error("获取服务器配置失败");
                }
                effective = rr.getData();
            }
            if (!isAlertEnabledInSetting(effective)) {
                return ReturnResult.success(false);
            }
            ReturnResult<List<MonitorSysGenServerAlertHistory>> alertResult = alertDetectionService.detectMetricsAlerts(server, effective, metrics);
            if (alertResult.isOk() && alertResult.getData() != null && !alertResult.getData().isEmpty()) {
                pushAlerts(server, alertResult.getData());
                return ReturnResult.success(true);
            }
            return ReturnResult.success(false);
        } catch (Exception e) {
            log.error("检查并推送服务器 {} 告警失败(重载): {}", server.getMonitorSysGenServerName(), e.getMessage(), e);
            return ReturnResult.error("告警检查失败: " + e.getMessage());
        }
    }


    @Override
    public ReturnResult<Boolean> checkAndPushApiReportAlerts(Integer serverId, Map<String, Object> reportData) {
        try {
            // 获取服务器信息
            MonitorSysGenServer server = serverService.getById(serverId);
            if (server == null) {
                return ReturnResult.error("服务器不存在");
            }

            // 检查服务器是否启用告警
            if (!isAlertEnabled(server)) {
                log.debug("服务器 {} 未启用告警功能，跳过API上报告警检测", server.getMonitorSysGenServerName());
                return ReturnResult.success(false);
            }

            // 检查配置中是否启用告警（先尝试只读本地缓存）
            ReturnResult<MonitorSysGenServerSetting> settingResult = serverSettingService.getFromLocalCache(serverId);
            if (!settingResult.isOk() || settingResult.getData() == null) {
                settingResult = serverSettingService.getOrCreateSetting(serverId);
            }

            if (settingResult.isOk() && settingResult.getData() != null) {
                MonitorSysGenServerSetting setting = settingResult.getData();
                if (!isAlertEnabledInSetting(setting)) {
                    log.debug("服务器 {} 配置中未启用告警功能，跳过API上报告警检测", server.getMonitorSysGenServerName());
                    return ReturnResult.success(false);
                }
            }

            // 进行API上报数据告警检测
            ReturnResult<List<MonitorSysGenServerAlertHistory>> alertResult =
                    alertDetectionService.detectApiReportAlerts(serverId, reportData);

            if (alertResult.isOk() && alertResult.getData() != null && !alertResult.getData().isEmpty()) {
                // 推送告警信息
                pushAlerts(server, alertResult.getData());
                log.info("服务器 {} API上报数据检测到 {} 个告警并已推送",
                        server.getMonitorSysGenServerName(), alertResult.getData().size());
                return ReturnResult.success(true);
            }

            return ReturnResult.success(false);

        } catch (Exception e) {
            log.error("检查并推送服务器 {} API上报告警失败: {}", serverId, e.getMessage(), e);
            return ReturnResult.error("API上报告警检查失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<Boolean> checkAndPushConnectionAlerts(Integer serverId, Boolean isOnline, Long responseTime) {
        try {
            // 获取服务器信息
            MonitorSysGenServer server = serverService.getById(serverId);
            if (server == null) {
                return ReturnResult.error("服务器不存在");
            }

            // 检查服务器是否启用告警
            if (!isAlertEnabled(server)) {
                log.debug("服务器 {} 未启用告警功能，跳过连接告警检测", server.getMonitorSysGenServerName());
                return ReturnResult.success(false);
            }
        } catch (Exception e) {
            log.error("检查并推送服务器 {} 连接告警失败: {}", serverId, e.getMessage(), e);
            return ReturnResult.error("连接告警检查失败: " + e.getMessage());
        }
        return ReturnResult.success(false);
    }
    @Override
    public ReturnResult<Boolean> checkAndPushConnectionAlerts(Integer serverId, Boolean isOnline, Long responseTime, MonitorSysGenServerSetting setting) {
        try {
            MonitorSysGenServer server = serverService.getById(serverId);
            if (server == null) {
                return ReturnResult.error("服务器不存在");
            }
            if (!isAlertEnabled(server)) {
                return ReturnResult.success(false);
            }
            MonitorSysGenServerSetting effective = setting;
            if (effective == null) {
                ReturnResult<MonitorSysGenServerSetting> rr = serverSettingService.getOrCreateSetting(serverId);
                if (!rr.isOk() || rr.getData() == null) {
                    return ReturnResult.error("获取服务器配置失败");
                }
                effective = rr.getData();
            }
            if (!isAlertEnabledInSetting(effective)) {
                return ReturnResult.success(false);
            }
            ReturnResult<MonitorSysGenServerAlertHistory> alertResult = alertDetectionService.detectConnectionAlert(serverId, isOnline, responseTime);
            if (alertResult.isOk() && alertResult.getData() != null) {
                pushSingleAlert(server, alertResult.getData());
                return ReturnResult.success(true);
            }
            return ReturnResult.success(false);
        } catch (Exception e) {
            log.error("检查并推送服务器 {} 连接告警失败(重载): {}", serverId, e.getMessage(), e);
            return ReturnResult.error("连接告警检查失败: " + e.getMessage());
        }
    }


    @Override
    public void pushAllServerAlerts() {
        try {
            log.debug("开始推送所有服务器告警信息");

            List<MonitorSysGenServer> servers = serverService.list();
            int alertCount = 0;

            for (MonitorSysGenServer server : servers) {
                try {
                    // 检查服务器是否启用告警
                    if (!isAlertEnabled(server)) {
                        continue;
                    }

                    // 检查配置中是否启用告警
                    ReturnResult<MonitorSysGenServerSetting> settingResult = serverSettingService.getFromLocalCache(server.getMonitorSysGenServerId());
                    if (!settingResult.isOk() || settingResult.getData() == null) {
                        settingResult = serverSettingService.getOrCreateSetting(server.getMonitorSysGenServerId());
                    }

                    if (settingResult.isOk() && settingResult.getData() != null) {
                        MonitorSysGenServerSetting setting = settingResult.getData();
                        if (!isAlertEnabledInSetting(setting)) {
                            continue;
                        }
                    }

                    // 检测Redis中的指标数据告警
                    ReturnResult<List<MonitorSysGenServerAlertHistory>> alertResult =
                            alertDetectionService.detectRedisMetricsAlerts(server.getMonitorSysGenServerId());

                    if (alertResult.isOk() && alertResult.getData() != null && !alertResult.getData().isEmpty()) {
                        pushAlerts(server, alertResult.getData());
                        alertCount += alertResult.getData().size();
                    }

                } catch (Exception e) {
                    log.error("推送服务器 {} 告警信息失败: {}",
                            server.getMonitorSysGenServerName(), e.getMessage(), e);
                }
            }

            log.debug("所有服务器告警信息推送完成，共推送 {} 个告警", alertCount);

        } catch (Exception e) {
            log.error("推送所有服务器告警信息失败", e);
        }
    }

    /**
     * 检查服务器是否启用告警（基础检查）
     */
    private boolean isAlertEnabled(MonitorSysGenServer server) {
        // 检查服务器状态
        return server.getMonitorSysGenServerStatus() != null && server.getMonitorSysGenServerStatus() == 1;

        // 基础检查通过，详细检查在配置表中进行
    }

    /**
     * 检查配置中是否启用告警
     */
    private boolean isAlertEnabledInSetting(MonitorSysGenServerSetting setting) {
        // 检查监控是否启用
        if (setting.getMonitorSysGenServerSettingMonitorEnabled() == null ||
            setting.getMonitorSysGenServerSettingMonitorEnabled() != 1) {
            return false;
        }

        // 检查告警是否启用
        return setting.getMonitorSysGenServerSettingAlertEnabled() != null &&
                setting.getMonitorSysGenServerSettingAlertEnabled() == 1;
    }

    /**
     * 推送告警列表
     */
    private void pushAlerts(MonitorSysGenServer server, List<MonitorSysGenServerAlertHistory> alerts) {
        try {
            Map<String, Object> alertData = new HashMap<>();
            alertData.put("alerts", alerts);

            ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                    .messageType("server_alerts")
                    .serverId(server.getMonitorSysGenServerId())
                    .serverName(server.getMonitorSysGenServerName())
                    .data(alertData)
                    .timestamp(System.currentTimeMillis())
                    .build();

            socketSessionTemplate.send(SocketTopics.Monitor.Server.ALERT, Json.toJson(message));
            log.debug("推送服务器 {} 告警信息成功，告警数量: {}",
                    server.getMonitorSysGenServerName(), alerts.size());

            // 非网页通道：根据配置分发
            try { alertNotificationDispatcher.dispatch(server, alerts); } catch (Throwable ignore) {}

        } catch (Exception e) {
            log.error("推送服务器 {} 告警信息失败: {}",
                    server.getMonitorSysGenServerName(), e.getMessage(), e);
        }
    }

    /**
     * 推送单个告警
     */
    private void pushSingleAlert(MonitorSysGenServer server, MonitorSysGenServerAlertHistory alert) {
        try {
            Map<String, Object> alertData = new HashMap<>();
            alertData.put("alert", alert);

            ServerWebSocketMessage message = ServerWebSocketMessage.builder()
                    .messageType("server_alert")
                    .serverId(server.getMonitorSysGenServerId())
                    .serverName(server.getMonitorSysGenServerName())
                    .data(alertData)
                    .timestamp(System.currentTimeMillis())
                    .build();

            socketSessionTemplate.send(SocketTopics.Monitor.Server.ALERT, Json.toJson(message));
            log.debug("推送服务器 {} 单个告警信息成功: {}",
                    server.getMonitorSysGenServerName(), alert.getMonitorSysGenServerAlertHistoryType());

            // 非网页通道：根据配置分发
            try { alertNotificationDispatcher.dispatchSingle(server, alert); } catch (Throwable ignore) {}

        } catch (Exception e) {
            log.error("推送服务器 {} 单个告警信息失败: {}",
                    server.getMonitorSysGenServerName(), e.getMessage(), e);
        }
    }
}
