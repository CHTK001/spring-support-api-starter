package com.chua.starter.monitor.handler;

import com.chua.common.support.annotations.Spi;
import com.chua.common.support.annotations.SpiDefault;
import com.chua.common.support.discovery.*;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.net.NetAddress;
import com.chua.common.support.objects.ConfigureObjectContext;
import com.chua.common.support.protocol.filter.ServiceDiscoveryServletFilter;
import com.chua.common.support.protocol.filter.ServletFilter;
import com.chua.common.support.utils.MapUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.configuration.SpringBeanUtils;
import com.chua.starter.monitor.entity.MonitorApplication;
import com.chua.starter.monitor.entity.SystemServerSetting;
import com.chua.starter.monitor.entity.SystemServerSettingServiceDiscovery;
import com.chua.starter.monitor.entity.SystemServerSettingServiceDiscoveryMapping;
import com.chua.starter.monitor.service.MonitorAppService;
import com.chua.starter.monitor.service.SystemServerSettingServiceDiscoveryService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * ServiceDiscovery 过滤器处理器
 * <p>
 * 支持模式：MONITOR/SPRING/TABLE/HAZELCAST，并可配置默认负载均衡策略 balance
 * <ul>
 *   <li>MONITOR: 从 Monitor 应用列表获取服务发现信息</li>
 *   <li>SPRING: 从 Spring 容器获取 ServiceDiscovery Bean</li>
 *   <li>TABLE: 从数据库表读取服务映射配置</li>
 *   <li>HAZELCAST: 使用 Hazelcast 分布式服务发现</li>
 * </ul>
 *
 * @author CH
 * @since 2025/8/11 17:01
 */
@Slf4j
@Spi("serviceDiscovery")
@SpiDefault
public class ServiceDiscoveryServletFilterHandler implements ServletFilterHandler {

    static HazelcastServiceDiscovery HAZELCAST_SERVICE_DISCOVERY = null;
    /**
     * 根据服务器配置创建 ServiceDiscoveryServletFilter
     *
     * @param setting       设置
     * @param objectContext
     */
    @Override
    public ServletFilter handle(SystemServerSetting setting, ConfigureObjectContext objectContext) {
        Integer serverId = setting.getSystemServerSettingServerId();

        SystemServerSettingServiceDiscoveryService discoveryService =
                SpringBeanUtils.getBean(SystemServerSettingServiceDiscoveryService.class);

        // 读取ServiceDiscovery主配置
        List<SystemServerSettingServiceDiscovery> configs = discoveryService.listByServerId(serverId);
        SystemServerSettingServiceDiscovery config = configs != null && !configs.isEmpty() ? configs.get(0) : null;

        String mode = config != null && StringUtils.isNotBlank(config.getServiceDiscoveryMode())
                ? config.getServiceDiscoveryMode()
                : "SPRING";
        String balance = config != null && StringUtils.isNotBlank(config.getServiceDiscoveryBalance())
                ? config.getServiceDiscoveryBalance()
                : "weight";
        boolean enabled = config == null || Boolean.TRUE.equals(config.getServiceDiscoveryEnabled());

        // 构建 ServiceDiscovery 实例
        ServiceDiscovery serviceDiscovery = createServiceDiscoveryByMode(mode, config, discoveryService, serverId);

        // 构建并返回过滤器
        ServiceDiscoveryServletFilter servletFilter = new ServiceDiscoveryServletFilter(serviceDiscovery)
                .setDefaultBalance(balance)
                .setEnabled(enabled)
                .setProtocolType("http");

        return servletFilter;
    }

    /**
     * 根据模式创建 ServiceDiscovery 实例
     *
     * @param mode             服务发现模式
     * @param config           服务发现配置
     * @param discoveryService 服务发现服务
     * @param serverId         服务器ID
     * @return ServiceDiscovery 实例
     */
    private ServiceDiscovery createServiceDiscoveryByMode(String mode,
                                                          SystemServerSettingServiceDiscovery config,
                                                          SystemServerSettingServiceDiscoveryService discoveryService,
                                                          Integer serverId) {
        // MONITOR 模式：从 Monitor 应用列表获取服务发现信息
        if ("MONITOR".equalsIgnoreCase(mode)) {
            log.debug("使用 MONITOR 模式创建服务发现");
            return createMonitorServiceDiscovery();
        }

        // TABLE / DEFAULT 模式：使用内置的 DefaultServiceDiscovery，并加载映射
        if ("TABLE".equalsIgnoreCase(mode) || "DEFAULT".equalsIgnoreCase(mode)) {
            log.debug("使用 TABLE 模式创建服务发现");
            DefaultServiceDiscovery defaultServiceDiscovery = new DefaultServiceDiscovery(new DiscoveryOption());
            List<SystemServerSettingServiceDiscoveryMapping> mappings =
                    discoveryService.listMappingsByServerId(serverId);
            if (mappings != null) {
                for (SystemServerSettingServiceDiscoveryMapping m : mappings) {
                    if (m == null || Boolean.FALSE.equals(m.getServiceDiscoveryEnabled())) {
                        continue;
                    }
                    String path = StringUtils.startWithAppend(m.getServiceDiscoveryName(), "/");
                    Discovery discovery = toDiscovery(m.getServiceDiscoveryAddress(), m.getServiceDiscoveryWeight());
                    if (discovery != null) {
                        defaultServiceDiscovery.registerService(path, discovery);
                        log.debug("TABLE模式注册服务: {} -> {}", path, m.getServiceDiscoveryAddress());
                    }
                }
            }
            return defaultServiceDiscovery;
        }

        // HAZELCAST 模式
        if ("HAZELCAST".equalsIgnoreCase(mode)) {
            if (null != HAZELCAST_SERVICE_DISCOVERY) {
                return HAZELCAST_SERVICE_DISCOVERY;
            }
            log.debug("使用 HAZELCAST 模式创建服务发现");
            HazelcastServiceDiscovery hazelcastServiceDiscovery = new HazelcastServiceDiscovery(new DiscoveryOption());
            try {
                hazelcastServiceDiscovery.start();
                HAZELCAST_SERVICE_DISCOVERY = hazelcastServiceDiscovery;
            } catch (Exception e) {
                log.warn("启动 Hazelcast 服务发现失败", e);
            }
            return hazelcastServiceDiscovery;
        }

        // SPRING 模式（默认）：从Spring容器中获取 ServiceDiscovery
        log.debug("使用 SPRING 模式创建服务发现");
        if (config != null && StringUtils.isNotBlank(config.getServiceDiscoveryBeanName())) {
            return SpringBeanUtils.getBean(config.getServiceDiscoveryBeanName(), ServiceDiscovery.class);
        }
        return SpringBeanUtils.getBean(ServiceDiscovery.class);
    }

    /**
     * 创建 MONITOR 模式的服务发现
     * <p>
     * 从 MonitorAppService 获取所有应用列表，将每个应用的实例注册到服务发现中
     *
     * @return 配置好的 ServiceDiscovery 实例
     */
    private ServiceDiscovery createMonitorServiceDiscovery() {
        DefaultServiceDiscovery serviceDiscovery = new DefaultServiceDiscovery(new DiscoveryOption());
        
        try {
            MonitorAppService monitorAppService = SpringBeanUtils.getBean(MonitorAppService.class);
            if (monitorAppService == null) {
                log.warn("无法获取 MonitorAppService，MONITOR 模式服务发现将为空");
                return serviceDiscovery;
            }

            // 获取所有应用列表
            ReturnResult<List<MonitorApplication>> listResult = monitorAppService.list(new MonitorApplication());
            if (!listResult.isOk() || listResult.getData() == null) {
                log.warn("获取 Monitor 应用列表失败");
                return serviceDiscovery;
            }

            List<MonitorApplication> applications = listResult.getData();
            log.info("MONITOR 模式加载 {} 个应用", applications.size());

            // 遍历所有应用，注册服务发现
            for (MonitorApplication app : applications) {
                String appName = app.getMonitorApplicationName();
                List<Discovery> discoveries = app.getMonitorRequests();

                if (StringUtils.isEmpty(appName) || discoveries == null || discoveries.isEmpty()) {
                    continue;
                }

                // 服务路径使用应用名称
                String servicePath = StringUtils.startWithAppend(appName, "/");

                // 注册该应用的所有实例
                for (Discovery discovery : discoveries) {
                    if (discovery == null) {
                        continue;
                    }

                    // 设置 uriSpec 为应用名称路径
                    discovery.setUriSpec(servicePath);

                    // 设置协议，默认 http
                    if (StringUtils.isEmpty(discovery.getProtocol())) {
                        discovery.setProtocol("http");
                    }

                    serviceDiscovery.registerService(servicePath, discovery);
                    log.debug("MONITOR 模式注册服务: {} -> {}:{}", servicePath, discovery.getHost(), discovery.getPort());
                }
            }

            log.info("MONITOR 模式服务发现初始化完成");
        } catch (Exception e) {
            log.error("创建 MONITOR 模式服务发现失败", e);
        }

        return serviceDiscovery;
    }

    private Discovery toDiscovery(String address, Integer weight) {
        if (StringUtils.isEmpty(address)) {
            return null;
        }
        try {
            NetAddress netAddress = NetAddress.of(address);
            String host = StringUtils.defaultString(netAddress.getHost(), "127.0.0.1");
            int port = netAddress.getPort() > 0 ? netAddress.getPort() : 80;
            String protocol = StringUtils.defaultString(netAddress.getProtocol(), "http");
            protocol = StringUtils.equalsIgnoreCase(protocol, "https") ? "http" : protocol;
            return Discovery.builder()
                    .host(host)
                    .port(port)
                    .protocol(protocol)
                    .weight(weight == null ? 1.0 : weight.doubleValue())
                    .build();
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public void update(ServletFilter filter, SystemServerSetting setting) {
        if (!(filter instanceof ServiceDiscoveryServletFilter sdf)) {
            return;
        }

        Integer serverId = setting.getSystemServerSettingServerId();
        SystemServerSettingServiceDiscoveryService discoveryService =
                SpringBeanUtils.getBean(SystemServerSettingServiceDiscoveryService.class);
        List<SystemServerSettingServiceDiscovery> configs = discoveryService.listByServerId(serverId);
        SystemServerSettingServiceDiscovery config = configs != null && !configs.isEmpty() ? configs.get(0) : null;
        String mode = config != null && StringUtils.isNotBlank(config.getServiceDiscoveryMode())
                ? config.getServiceDiscoveryMode()
                : "SPRING";
        String balance = config != null && StringUtils.isNotBlank(config.getServiceDiscoveryBalance())
                ? config.getServiceDiscoveryBalance()
                : "weight";
        boolean enabled = config == null || Boolean.TRUE.equals(config.getServiceDiscoveryEnabled());

        ServiceDiscovery serviceDiscovery = sdf.getServiceDiscovery();
        if(serviceDiscovery != null && null != HAZELCAST_SERVICE_DISCOVERY && serviceDiscovery instanceof HazelcastServiceDiscovery hazelcastableServiceDiscovery) {
            HAZELCAST_SERVICE_DISCOVERY = hazelcastableServiceDiscovery;
        }
        ServiceDiscovery newServiceDiscovery = createServiceDiscoveryByMode(mode, config, discoveryService, serverId);
        sdf.setServiceDiscovery(newServiceDiscovery)
                .setDefaultBalance(balance)
                .setEnabled(enabled);
    }
}
