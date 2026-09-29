package com.chua.starter.system.support;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.starter.entity.system.SysPolicyConfig;
import com.chua.starter.system.mapper.SysPolicyConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * IP 策略缓存启动重建器
 *
 * <p>应用就绪后从统一策略配置表（{@code type='IP'} 且启用）全量重建 IP 策略缓存，
 * 避免重启后缓存为空导致黑白名单失效。</p>
 *
 * @since 4.0.0.43
 * @author CH
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.LOWEST_PRECEDENCE)
public class IpPolicyStartupRebuilder implements ApplicationListener<ApplicationReadyEvent> {

    /**
     * 统一策略配置映射器
     */
    private final SysPolicyConfigMapper sysPolicyConfigMapper;

    /**
     * IP 策略缓存投影器
     */
    private final IpPolicyCacheProjector ipPolicyCacheProjector;

    /**
     * 消息头拦截规则投影器
     */
    private final HeaderPolicyCacheProjector headerPolicyCacheProjector;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            List<SysPolicyConfig> entries = sysPolicyConfigMapper.selectList(
                    Wrappers.<SysPolicyConfig>lambdaQuery()
                            .eq(SysPolicyConfig::getType, SysPolicyConfig.TYPE_IP)
                            .eq(SysPolicyConfig::getStatus, SysPolicyConfig.STATUS_ENABLED));
            ipPolicyCacheProjector.rebuild(entries);
        } catch (Exception e) {
            log.error("启动时重建 IP 策略缓存失败", e);
        }
        // 消息头拦截规则同样只在内存/缓存里有投影，重启后必须重建，
        // 否则「爬虫拦截」会在每次重启后静默失效
        try {
            List<SysPolicyConfig> headerEntries = sysPolicyConfigMapper.selectList(
                    Wrappers.<SysPolicyConfig>lambdaQuery()
                            .eq(SysPolicyConfig::getType, SysPolicyConfig.TYPE_HEADER)
                            .eq(SysPolicyConfig::getStatus, SysPolicyConfig.STATUS_ENABLED));
            headerPolicyCacheProjector.rebuild(headerEntries);
        } catch (Exception e) {
            log.error("启动时重建消息头拦截规则缓存失败", e);
        }
    }
}
