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
 * 账号名单缓存启动重建器
 *
 * <p>与 {@link IpPolicyStartupRebuilder} 对称：应用就绪后从统一策略配置表
 * （{@code type='ACCOUNT'} 且启用）全量重建账号黑/白名单缓存。</p>
 *
 * <p>此前只有 IP 侧有重建器，账号侧没有：缓存为空时
 * {@code matchesAccount} 恒为 false，导致<b>重启后账号黑名单静默失效</b>
 * ——库里数据还在、接口也返回成功，但拦截不再生效。
 * 账号名单与 IP 名单同源同表，重建逻辑必须成对存在。</p>
 *
 * @since 4.0.0.52
 * @author CH
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.LOWEST_PRECEDENCE)
public class AccountPolicyStartupRebuilder implements ApplicationListener<ApplicationReadyEvent> {

    /**
     * 统一策略配置映射器
     */
    private final SysPolicyConfigMapper sysPolicyConfigMapper;

    /**
     * 策略运行时缓存
     */
    private final PolicyCacheService policyCacheService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            List<SysPolicyConfig> entries = sysPolicyConfigMapper.selectList(
                    Wrappers.<SysPolicyConfig>lambdaQuery()
                            .eq(SysPolicyConfig::getType, SysPolicyConfig.TYPE_ACCOUNT)
                            .eq(SysPolicyConfig::getStatus, SysPolicyConfig.STATUS_ENABLED));
            policyCacheService.clearAllAccount();
            if (null == entries) {
                return;
            }
            for (SysPolicyConfig entry : entries) {
                if (null == entry.getListType() || null == entry.getBizKey()) {
                    continue;
                }
                policyCacheService.putAccount(entry.getListType(), entry.getBizKey());
            }
            log.info("启动时重建账号名单缓存完成: {} 条", entries.size());
        } catch (Exception e) {
            log.error("启动时重建账号名单缓存失败", e);
        }
    }
}
