package com.chua.starter.monitor.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.core.utils.StringUtils;
import com.chua.starter.monitor.entity.MonitorSysGenServer;
import com.chua.starter.monitor.entity.MonitorSysGenServerSetting;
import com.chua.starter.monitor.pojo.server.ProxyConnectionInfo;
import com.chua.starter.monitor.service.server.MonitorSysGenServerSettingService;
import com.chua.starter.monitor.service.server.ProxyInstanceResolver;
import com.chua.starter.proxy.support.entity.SystemServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProxyInstanceResolverImpl implements ProxyInstanceResolver {

    private static final String SOURCE_PROXY_INSTANCE = "PROXY_INSTANCE";
    private static final String SOURCE_LEGACY_SETTING = "LEGACY_SETTING";
    private static final String SOURCE_LEGACY_SERVER = "LEGACY_SERVER";

    private final MonitorSysGenServerSettingService monitorSysGenServerSettingService;
    private final ObjectProvider<com.chua.starter.proxy.support.service.server.SystemServerService>
            proxySystemServerServiceProvider;

    @Override
    public ProxyConnectionInfo resolve(MonitorSysGenServer server) {
        if (server == null || server.getMonitorSysGenServerId() == null) {
            return null;
        }

        try {
            ReturnResult<MonitorSysGenServerSetting> settingResult =
                    monitorSysGenServerSettingService.getByServerId(server.getMonitorSysGenServerId());
            return resolve(server, settingResult.isOk() ? settingResult.getData() : null);
        } catch (Exception e) {
            log.warn("解析服务器代理配置失败，转入兼容兜底: serverId={}, error={}",
                    server.getMonitorSysGenServerId(), e.getMessage());
            return resolve(server, null);
        }
    }

    @Override
    public ProxyConnectionInfo resolve(MonitorSysGenServer server, MonitorSysGenServerSetting setting) {
        ProxyConnectionInfo resolved = resolveFromSetting(setting);
        return resolved != null ? resolved : resolveFromLegacyServer(server);
    }

    private ProxyConnectionInfo resolveFromSetting(MonitorSysGenServerSetting setting) {
        if (setting == null || !Integer.valueOf(1).equals(setting.getMonitorSysGenServerSettingProxyEnabled())) {
            return null;
        }

        Integer proxyInstanceId = setting.getMonitorSysGenServerSettingProxyInstanceId();
        if (proxyInstanceId != null) {
            ProxyConnectionInfo resolved = resolveFromProxyInstance(proxyInstanceId, SOURCE_PROXY_INSTANCE);
            if (resolved != null) {
                return resolved;
            }
        }

        if (StringUtils.isBlank(setting.getMonitorSysGenServerSettingProxyHost())
                || setting.getMonitorSysGenServerSettingProxyPort() == null) {
            return null;
        }

        return ProxyConnectionInfo.builder()
                .proxyInstanceId(proxyInstanceId)
                .proxyType(setting.getMonitorSysGenServerSettingProxyType())
                .proxyHost(setting.getMonitorSysGenServerSettingProxyHost())
                .proxyPort(setting.getMonitorSysGenServerSettingProxyPort())
                .proxyUsername(setting.getMonitorSysGenServerSettingProxyUsername())
                .proxyPassword(setting.getMonitorSysGenServerSettingProxyPassword())
                .source(SOURCE_LEGACY_SETTING)
                .build();
    }

    private ProxyConnectionInfo resolveFromLegacyServer(MonitorSysGenServer server) {
        if (server == null || server.getMonitorSysGenServerProxyId() == null) {
            return null;
        }
        return resolveFromProxyInstance(server.getMonitorSysGenServerProxyId(), SOURCE_LEGACY_SERVER);
    }

    private ProxyConnectionInfo resolveFromProxyInstance(Integer proxyInstanceId, String source) {
        com.chua.starter.proxy.support.service.server.SystemServerService proxySystemServerService =
                proxySystemServerServiceProvider.getIfAvailable();
        if (proxySystemServerService == null) {
            log.debug("代理管理模块未启用，无法解析代理实例: proxyInstanceId={}", proxyInstanceId);
            return null;
        }

        SystemServer proxyServer = proxySystemServerService.getById(proxyInstanceId);
        if (proxyServer == null) {
            log.warn("代理实例不存在: proxyInstanceId={}", proxyInstanceId);
            return null;
        }

        if (StringUtils.isBlank(proxyServer.getSystemServerHost()) || proxyServer.getSystemServerPort() == null) {
            log.warn("代理实例缺少地址或端口: proxyInstanceId={}", proxyInstanceId);
            return null;
        }

        return ProxyConnectionInfo.builder()
                .proxyInstanceId(proxyServer.getSystemServerId())
                .proxyType(proxyServer.getSystemServerType())
                .proxyHost(proxyServer.getSystemServerHost())
                .proxyPort(proxyServer.getSystemServerPort())
                .source(source)
                .build();
    }
}
