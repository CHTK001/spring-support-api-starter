package com.chua.starter.monitor.handler;

import com.chua.common.support.objects.ConfigureObjectContext;
import com.chua.common.support.network.protocol.filter.FirewallServletFilter;
import com.chua.common.support.network.protocol.filter.ServletFilter;
import com.chua.common.support.core.annotation.Spi;
import com.chua.common.support.core.spi.ServiceProvider;
import com.chua.starter.monitor.entity.SystemServerSetting;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

/**
 * 防火墙过滤器专用处理器：负责首次创建和运行态热更新。
 */
@Slf4j
@Spi("firewall")
public class FirewallServletFilterHandler implements ServletFilterHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ServletFilter handle(SystemServerSetting setting, ConfigureObjectContext objectContext) {
        ServletFilter servletFilter = ServiceProvider.of(ServletFilter.class)
                .getNewExtension(setting.getSystemServerSettingType(), objectContext);
        if (servletFilter instanceof FirewallServletFilter firewallServletFilter) {
            firewallServletFilter.upgradeConfigObject(toConfig(setting));
        }
        return servletFilter;
    }

    @Override
    public void update(ServletFilter filter, SystemServerSetting setting) {
        if (filter instanceof FirewallServletFilter firewallServletFilter) {
            firewallServletFilter.upgradeConfigObject(toConfig(setting));
        }
    }

    private FirewallServletFilter.FirewallConfig toConfig(SystemServerSetting setting) {
        FirewallServletFilter.FirewallConfig config = new FirewallServletFilter.FirewallConfig();
        if (setting == null || setting.getSystemServerSettingConfig() == null || setting.getSystemServerSettingConfig().isBlank()) {
            return config;
        }
        try {
            FirewallServletFilter.FirewallConfig converted = objectMapper.readValue(
                    setting.getSystemServerSettingConfig(),
                    FirewallServletFilter.FirewallConfig.class
            );
            return normalize(converted);
        } catch (Exception e) {
            log.warn("解析防火墙配置失败，回退默认配置: settingId={}", setting == null ? null : setting.getSystemServerSettingId(), e);
            return config;
        }
    }

    private FirewallServletFilter.FirewallConfig normalize(FirewallServletFilter.FirewallConfig config) {
        FirewallServletFilter.FirewallConfig safe = config == null ? new FirewallServletFilter.FirewallConfig() : config;
        if (safe.getWhitelistIps() == null) {
            safe.setWhitelistIps(new LinkedHashSet<>());
        }
        if (safe.getBlacklistIps() == null) {
            safe.setBlacklistIps(new LinkedHashSet<>());
        }
        if (safe.getPathRateLimits() == null) {
            safe.setPathRateLimits(new LinkedHashMap<>());
        }
        if (safe.getRedirectMappings() == null) {
            safe.setRedirectMappings(new LinkedHashMap<>());
        }
        if (safe.getProxyPassMappings() == null) {
            safe.setProxyPassMappings(new LinkedHashMap<>());
        }
        return safe;
    }
}
