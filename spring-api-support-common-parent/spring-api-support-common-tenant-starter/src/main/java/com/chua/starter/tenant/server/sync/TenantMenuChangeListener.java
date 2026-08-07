package com.chua.starter.tenant.server.sync;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.starter.system.event.SysMenuChangeEvent;
import com.chua.starter.tenant.entity.SysTenant;
import com.chua.starter.tenant.properties.TenantProperties;
import com.chua.starter.tenant.server.mapper.SysTenantMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 菜单变更联动租户端同步监听器
 * <p>
 * 平台端菜单新增/修改/删除后，触发所有未删除租户的菜单重新计算与推送，
 * 使租户端菜单实时同步，无需等待定时全量推送
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 4.0.0.42
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = TenantProperties.PRE + ".server", name = "enable", havingValue = "true")
public class TenantMenuChangeListener {

    private final ObjectProvider<TenantServerSyncPublisher> syncPublisherProvider;
    private final SysTenantMapper sysTenantMapper;

    /**
     * 监听菜单变更事件，触发所有租户菜单重新推送
     *
     * @param event 菜单变更事件
     */
    @EventListener
    public void onMenuChange(SysMenuChangeEvent event) {
        TenantServerSyncPublisher publisher = syncPublisherProvider.getIfAvailable();
        if (publisher == null) {
            return;
        }

        try {
            List<SysTenant> tenants = sysTenantMapper.selectList(
                    Wrappers.<SysTenant>lambdaQuery().eq(SysTenant::getSysTenantDelete, 0)
            );
            if (tenants == null || tenants.isEmpty()) {
                return;
            }

            for (SysTenant tenant : tenants) {
                publisher.publishServiceUpdate(tenant.getSysTenantId(), null);
            }
            log.info("[租户菜单同步] 菜单变更[{}]触发 {} 个租户菜单重新推送, menuId={}",
                    event.getOperation(), tenants.size(), event.getSysMenuId());
        } catch (Exception e) {
            log.error("[租户菜单同步] 菜单变更联动推送失败", e);
        }
    }
}
