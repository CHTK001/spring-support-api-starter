package com.chua.starter.monitor.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.starter.monitor.entity.MonitorSysGenAlertPushConfig;
import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.entity.MonitorSysGenServerAlertHistory;
import com.chua.starter.monitor.enums.AlertType;
import com.chua.starter.monitor.mapper.MonitorSysGenAlertPushConfigMapper;
import com.chua.starter.monitor.service.AlertMessagePushBridge;
import com.chua.starter.monitor.service.AlertNotificationDispatcher;
import com.chua.starter.monitor.service.MonitorSysGenMessagePushTemplateService;
import com.chua.starter.monitor.spi.MessagePushSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertNotificationDispatcherImpl implements AlertNotificationDispatcher {

    @org.springframework.beans.factory.annotation.Value("${server.monitor.alert.push-limit-seconds:60}")
    private long pushLimitSeconds;

    private final MonitorSysGenAlertPushConfigMapper configMapper;
    private final AlertMessagePushBridge bridge;
    private final MonitorSysGenMessagePushTemplateService templateService;

    @Override
    public ReturnResult<Boolean> dispatch(MonitorSysGenServer server, List<MonitorSysGenServerAlertHistory> alerts) {
        if (alerts == null || alerts.isEmpty() || server == null) {
            return ReturnResult.success(false);
        }
        // 以告警类型分组，加载各自配置
        boolean any = false;
        for (MonitorSysGenServerAlertHistory alert : alerts) {
            ReturnResult<Boolean> rr = dispatchSingle(server, alert);
            any = any || (rr.isOk() && Boolean.TRUE.equals(rr.getData()));
        }
        return ReturnResult.success(any);
    }

    // 简易限频缓存：fingerprint+channel -> lastPushEpoch
    private final java.util.concurrent.ConcurrentHashMap<String, Long> rateLimitCache = new java.util.concurrent.ConcurrentHashMap<>();

    @Override
    public ReturnResult<Boolean> dispatchSingle(MonitorSysGenServer server, MonitorSysGenServerAlertHistory alert) {
        if (server == null || alert == null || alert.getMonitorSysGenServerAlertHistoryType() == null) {
            return ReturnResult.success(false);
        }
        List<MonitorSysGenAlertPushConfig> configs = loadEnabledConfigs(server.getMonitorSysGenServerId(),
                alert.getMonitorSysGenServerAlertHistoryType());
        boolean pushed = false;
        for (MonitorSysGenAlertPushConfig cfg : configs) {
            try {
                // 限频：按 指纹+通道 维度
                String fingerprint = String.valueOf(alert.getMonitorSysGenServerAlertHistoryFingerprint());
                String channelKey = cfg.getMonitorSysGenAlertPushConfigTemplateId() != null ? "TPL" : "CFG";
                String key = fingerprint + ":" + (cfg.getMonitorSysGenAlertPushConfigChannel() == null ? "?" : cfg.getMonitorSysGenAlertPushConfigChannel().getCode()) + ":" + channelKey;
                long now = System.currentTimeMillis();
                Long last = rateLimitCache.get(key);
                if (last != null && (now - last) < pushLimitSeconds * 1000L) {
                    continue;
                }
                // 如果存在模板，优先从模板取参数并走 SPI
                if (cfg.getMonitorSysGenAlertPushConfigTemplateId() != null) {
                    var tplR = templateService.getEnabledById(cfg.getMonitorSysGenAlertPushConfigTemplateId());
                    if (tplR.isOk() && tplR.getData() != null && tplR.getData().getMonitorSysGenMessagePushTemplateChannel() != null) {
                        String channel = tplR.getData().getMonitorSysGenMessagePushTemplateChannel().getCode();
                        MessagePushSender sender = ServiceProvider.of(MessagePushSender.class).getNewExtension(channel);
                        if (sender != null) {
                            boolean ok = sender.send(tplR.getData(), alert);
                            pushed = pushed || ok;
                            if (ok) { rateLimitCache.put(key, now); }
                            continue;
                        }
                    }
                }
                // 回退：直接用配置项 + 默认Bridge（兼容旧实现）
                boolean ok = bridge.push(cfg, alert);
                pushed = pushed || ok;
                if (pushed) { rateLimitCache.put(key, now); }
            } catch (Exception e) {
                log.warn("第三方告警推送失败 serverId={} cfgId={}", server.getMonitorSysGenServerId(),
                        cfg.getMonitorSysGenAlertPushConfigId(), e);
            }
        }
        return ReturnResult.success(pushed);
    }

    @Override
    @org.springframework.cache.annotation.Cacheable(value = "alertPushConfig", key = "#serverId + ':' + (#type == null ? 'ALL' : #type.code)", unless = "#result == null || #result.isEmpty()")
    public List<MonitorSysGenAlertPushConfig> loadEnabledConfigs(Integer serverId, AlertType type) {
        try {
            if (type == null) return Collections.emptyList();
            List<MonitorSysGenAlertPushConfig> list = configMapper.selectEnabledByServerAndType(serverId, type.getCode());
            if (list == null) return Collections.emptyList();
            // 过滤掉 channel=WEB（网页推送由Socket.IO已处理）
            return list.stream()
                    .filter(Objects::nonNull)
                    .filter(cfg -> cfg.getMonitorSysGenAlertPushConfigChannel() != null)
                    .filter(cfg -> cfg.getMonitorSysGenAlertPushConfigEnabled() == null || cfg.getMonitorSysGenAlertPushConfigEnabled())
                    .filter(cfg -> !"WEB".equalsIgnoreCase(cfg.getMonitorSysGenAlertPushConfigChannel().getCode()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("加载告警推送配置失败 serverId={}", serverId, e);
            return Collections.emptyList();
        }
    }
}

