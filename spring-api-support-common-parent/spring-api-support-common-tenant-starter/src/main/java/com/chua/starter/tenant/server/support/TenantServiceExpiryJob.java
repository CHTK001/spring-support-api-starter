package com.chua.starter.tenant.server.support;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.starter.tenant.entity.SysTenant;
import com.chua.starter.tenant.entity.SysTenantService;
import com.chua.starter.tenant.server.mapper.SysTenantMapper;
import com.chua.starter.tenant.server.mapper.SysTenantServiceMapper;
import com.chua.starter.tenant.server.sync.TenantServerSyncPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 租户服务到期清理。
 *
 * <p>「到期」的落点是<b>租户库里的服务记录</b>，而不只是平台库的一行绑定：</p>
 * <ol>
 *   <li>删掉平台 {@code sys_tenant_service} 里已过期的绑定；</li>
 *   <li>按剩余有效绑定重写租户库的 {@code sys_service} 等三张表（过期服务随之消失）；</li>
 *   <li>重新计算菜单并推送租户端，避免租户继续看到已过期服务的菜单。</li>
 * </ol>
 *
 * <p><b>幂等</b>：重复执行只会重复删除“已经不存在”的行，结果一致。
 * 因此既可以被定时任务调用，也可以由运维通过
 * {@code PUT /v2/tenant/services/expire} 手动触发（验收场景不必等到整点）。</p>
 *
 * <p><b>失败语义</b>：单个租户同步/推送失败不影响其它租户，异常只记日志。</p>
 *
 * @since 4.0.0.51
 * @author CH
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantServiceExpiryJob {

    /**
     * 租户 - 服务绑定
     */
    private final SysTenantServiceMapper sysTenantServiceMapper;

    /**
     * 租户
     */
    private final SysTenantMapper sysTenantMapper;

    /**
     * 租户库服务目录同步器
     */
    private final TenantServiceCatalogSynchronizer tenantServiceCatalogSynchronizer;

    /**
     * 租户端同步推送；租户端未接入时为空
     */
    @Autowired(required = false)
    private TenantServerSyncPublisher syncPublisher;

    /**
     * 每 10 分钟检查一次到期绑定。
     *
     * <p>到期粒度是天，10 分钟足够及时；整点前的最后一次检查不会漏，
     * 因为判定用的是“到期日早于今天”，跨天后的第一轮就会命中。</p>
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void scheduledPurge() {
        int removed = purgeExpired();
        if (removed > 0) {
            log.info("[租户服务到期] 定时清理完成: 删除绑定 {} 条", removed);
        }
    }

    /**
     * 清理已到期的租户服务绑定。
     *
     * @return 删除的绑定条数
     */
    public int purgeExpired() {
        LocalDate today = LocalDate.now();
        List<SysTenantService> expired = sysTenantServiceMapper.selectList(
                Wrappers.<SysTenantService>lambdaQuery()
                        .isNotNull(SysTenantService::getSysTenantServiceValidTime)
                        .lt(SysTenantService::getSysTenantServiceValidTime, today));
        if (expired == null || expired.isEmpty()) {
            return 0;
        }

        Map<String, List<SysTenantService>> grouped = expired.stream()
                .filter(item -> item.getSysTenantId() != null)
                .collect(Collectors.groupingBy(SysTenantService::getSysTenantId));

        int removed = 0;
        for (Map.Entry<String, List<SysTenantService>> entry : grouped.entrySet()) {
            String tenantId = entry.getKey();
            List<Integer> bindIds = entry.getValue().stream()
                    .map(SysTenantService::getSysTenantServiceId)
                    .filter(Objects::nonNull)
                    .toList();
            if (!bindIds.isEmpty()) {
                removed += sysTenantServiceMapper.deleteBatchIds(bindIds);
            }
            log.info("[租户服务到期] 租户 {} 到期服务已解绑: serviceIds={}",
                    tenantId, entry.getValue().stream().map(SysTenantService::getSysServiceId).toList());

            SysTenant tenant = sysTenantMapper.selectById(tenantId);
            if (tenant != null) {
                tenantServiceCatalogSynchronizer.sync(tenant);
            }
            publishMenuUpdate(tenantId);
        }
        return removed;
    }

    /**
     * 重新计算并推送租户菜单。
     *
     * @param tenantId 租户标识
     */
    private void publishMenuUpdate(String tenantId) {
        if (syncPublisher == null) {
            return;
        }
        try {
            List<Integer> menuIds = sysTenantServiceMapper.getMenuByTenantId(tenantId);
            syncPublisher.publishServiceUpdate(tenantId, menuIds);
        } catch (Exception e) {
            log.warn("[租户服务到期] 菜单推送失败，已跳过: tenantId={}, error={}", tenantId, e.getMessage());
        }
    }
}
