package com.chua.starter.system.support;

import com.chua.starter.entity.system.SysPolicyConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

/**
 * 消息头拦截规则的缓存投影器。
 *
 * <p>与 {@link IpPolicyCacheProjector} 同构：数据库（统一策略表 {@code sys_policy_config}）
 * 是权威数据源，Spring Cache 是运行时投影；<b>事务提交后</b>才写缓存，避免事务回滚
 * 造成缓存脏数据。保存 / 启停 / 删除后立即投影，即<b>热重载</b>，不需要重启。</p>
 *
 * @author CH
 * @since 4.0.0.43
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HeaderPolicyCacheProjector {

    /**
     * 策略运行时缓存
     */
    private final PolicyCacheService policyCacheService;

    /**
     * 事务提交后同步消息头规则缓存。
     *
     * @param current  当前记录（删除时为 null）
     * @param previous 旧记录
     * @param deleted  是否删除
     */
    public void syncAfterCommit(SysPolicyConfig current, SysPolicyConfig previous, boolean deleted) {
        Runnable action = () -> {
            if (null != previous) {
                policyCacheService.evictHeaderRule(previous.getBizKey());
            }
            if (!deleted && isEnabled(current)) {
                projectOne(current);
            }
        };
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
            return;
        }
        action.run();
    }

    /**
     * 全量重建消息头规则缓存。
     *
     * @param entries 启用中的统一表行
     */
    public void rebuild(List<SysPolicyConfig> entries) {
        policyCacheService.clearAllHeader();
        if (null == entries) {
            return;
        }
        for (SysPolicyConfig entry : entries) {
            if (isEnabled(entry)) {
                projectOne(entry);
            }
        }
        log.info("[HeaderPolicy] 规则缓存已重建: {} 条, 覆盖消息头 {}",
                entries.size(), policyCacheService.headerNameIndex());
    }

    /**
     * 投影单条规则。
     *
     * @param entry 统一表行
     */
    private void projectOne(SysPolicyConfig entry) {
        HeaderRule rule = HeaderRule.parse(entry.getBizKey(), entry.getConfig());
        if (null == rule) {
            log.warn("[HeaderPolicy] 规则无法解析，已跳过投影: header={}", entry.getBizKey());
            return;
        }
        String json = rule.toJson();
        if (null != json) {
            policyCacheService.projectHeaderRule(entry.getBizKey(), json);
        }
    }

    /**
     * 判断是否为启用中的消息头规则。
     *
     * @param entry 统一表行
     * @return 是否启用
     */
    private boolean isEnabled(SysPolicyConfig entry) {
        return null != entry
                && null != entry.getStatus()
                && entry.getStatus() == SysPolicyConfig.STATUS_ENABLED;
    }
}
