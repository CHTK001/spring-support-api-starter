package com.chua.starter.monitor.handler;

import com.chua.common.support.annotations.Spi;
import com.chua.common.support.annotations.SpiDefault;
import com.chua.common.support.discovery.ServiceDiscovery;
import com.chua.common.support.protocol.filter.ServiceDiscoveryServletFilter;
import com.chua.common.support.protocol.server.ServletFilter;
import com.chua.starter.common.support.configuration.SpringBeanUtils;
import com.chua.starter.monitor.entity.SystemServerSetting;

/**
 * 负载均衡
 *
 * @author CH
 * @since 2025/8/11 17:01
 */
@Spi("default")
@SpiDefault
public class ServiceDiscoveryServletFilterHandler implements ServletFilterHandler {

    /**
     * 注册地址限流
     *
     * @param setting 设置
     */
    @Override
    public ServletFilter handle(SystemServerSetting setting) {
        ServletFilter servletFilter = new ServiceDiscoveryServletFilter(
                SpringBeanUtils.getBean(ServiceDiscovery.class)
        );
        return servletFilter;
    }

    @Override
    public void update(ServletFilter filter, SystemServerSetting setting) {

    }

}
