package com.chua.starter.monitor.handler;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.annotations.Spi;
import com.chua.common.support.protocol.filter.AddressRateLimitServletFilter;
import com.chua.common.support.protocol.filter.ServletFilter;
import com.chua.starter.common.support.configuration.SpringBeanUtils;
import com.chua.starter.monitor.entity.SystemServerSetting;
import com.chua.starter.monitor.entity.SystemServerSettingAddressRateLimit;
import com.chua.starter.monitor.mapper.SystemServerSettingAddressRateLimitMapper;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 地址限流
 *
 * @author CH
 * @since 2025/8/11 17:01
 */
@Spi("addressRateLimit")
public class AddressRateLimitServletFilterHandler implements ServletFilterHandler {


    private final SystemServerSettingAddressRateLimitMapper systemServerSettingAddressRateLimitMapper;

    public AddressRateLimitServletFilterHandler() {
        this.systemServerSettingAddressRateLimitMapper = SpringBeanUtils.getBean(SystemServerSettingAddressRateLimitMapper.class);
    }

    /**
     * 注册地址限流
     *
     * @param setting 设置
     */
    @Override
    public ServletFilter handle(SystemServerSetting setting) {
        AddressRateLimitServletFilter addressRateLimitServletFilter = new AddressRateLimitServletFilter();
        update(addressRateLimitServletFilter, setting);
        return addressRateLimitServletFilter;
    }

    @Override
    public void update(ServletFilter filter, SystemServerSetting setting) {
        if (filter instanceof AddressRateLimitServletFilter addressRateLimitServletFilter) {
            addressRateLimitServletFilter.clear();
            upgrade(addressRateLimitServletFilter, setting);
        }
    }

    /**
     * 刷新
     *
     * @param addressRateLimitServletFilter
     * @param setting
     */
    private void upgrade(AddressRateLimitServletFilter addressRateLimitServletFilter, SystemServerSetting setting) {
        List<SystemServerSettingAddressRateLimit> systemServerSettingAddressRateLimits = systemServerSettingAddressRateLimitMapper.selectList(
                Wrappers.<SystemServerSettingAddressRateLimit>lambdaQuery()
                        .eq(SystemServerSettingAddressRateLimit::getAddressRateLimitSettingId, setting.getSystemServerSettingId())
        );
        for (SystemServerSettingAddressRateLimit addressRateLimit : systemServerSettingAddressRateLimits) {
            String ipRateLimitType = addressRateLimit.getAddressRateLimitType();
            if ("BLACKLIST".equalsIgnoreCase(ipRateLimitType)) {
                addressRateLimitServletFilter.addBlacklist(addressRateLimit.getAddressRateLimitAddress());
                continue;
            }
            if ("WHITELIST".equalsIgnoreCase(ipRateLimitType)) {
                addressRateLimitServletFilter.addWhitelist(addressRateLimit.getAddressRateLimitAddress());
                continue;
            }
            addressRateLimitServletFilter.addPathRule(
                    addressRateLimit.getAddressRateLimitAddress(),
                    addressRateLimit.getAddressRateLimitType(),
                    addressRateLimit.getAddressRateLimitQps(),
                    addressRateLimit.getAddressRateLimitQps(),
                    TimeUnit.SECONDS
            );
        }
    }

}
