package com.chua.starter.system.support;

import com.chua.starter.entity.system.SysPolicyConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

/**
 * IP 策略缓存投影器。
 *
 * <p>数据库（统一策略配置表 {@code sys_firewall_config}）是权威数据源，Spring Cache 是运行时投影；
 * 事务提交后再写入/清理缓存，避免事务回滚造成缓存脏数据。</p>
 *
 * <p>本类对外仍使用 IP 名单的旧口径（{@code type} 0白/1黑），与
 * {@link PolicyConfigConverter#toIpType(Integer)} 保持一致；
 * 统一表内部的 1黑/2白 归一由转换器负责。</p>
 *
 * @since 4.0.0.43
 * @author CH
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IpPolicyCacheProjector {

    /**
     * 策略运行时缓存
     */
    private final PolicyCacheService policyCacheService;

    /**
     * 事务提交后同步 IP 策略缓存。
     *
     * @param current 当前记录（删除时为 null）
     * @param previous 旧记录
     * @param deleted  是否删除
     */
    public void syncAfterCommit(SysPolicyConfig current, SysPolicyConfig previous, boolean deleted) {
        Runnable action = () -> {
            if (null != previous) {
                policyCacheService.clearIp(PolicyConfigConverter.toIpType(previous.getListType()), previous.getBizKey());
            }
            if (!deleted && isEnabled(current)) {
                policyCacheService.projectIp(PolicyConfigConverter.toIpType(current.getListType()), current.getBizKey());
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
     * 全量重建 IP 策略缓存。
     *
     * @param entries 启用中的统一表行
     */
    public void rebuild(List<SysPolicyConfig> entries) {
        policyCacheService.clearAllIp();
        if (null == entries) {
            return;
        }
        for (SysPolicyConfig entry : entries) {
            if (isEnabled(entry)) {
                policyCacheService.projectIp(PolicyConfigConverter.toIpType(entry.getListType()), entry.getBizKey());
            }
        }
    }

    /**
     * 判断 IP 是否命中指定类型名单。
     *
     * @param type     1 黑名单 0 白名单
     * @param clientIp 客户端 IP
     * @return 是否命中
     */
    public boolean matches(int type, String clientIp) {
        return policyCacheService.matchesIp(type, clientIp);
    }

    /**
     * 判断统一表行是否为启用中的 IP 名单。
     *
     * @param entry 统一表行
     * @return 是否启用
     */
    private boolean isEnabled(SysPolicyConfig entry) {
        return null != entry
                && null != entry.getListType()
                && (entry.getListType() == SysPolicyConfig.LIST_BLACK || entry.getListType() == SysPolicyConfig.LIST_WHITE)
                && null != entry.getStatus()
                && entry.getStatus() == SysPolicyConfig.STATUS_ENABLED;
    }
}
