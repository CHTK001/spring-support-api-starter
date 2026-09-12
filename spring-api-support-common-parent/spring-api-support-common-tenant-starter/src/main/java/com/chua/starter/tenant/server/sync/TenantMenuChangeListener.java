package com.chua.starter.tenant.server.sync;

import com.chua.starter.system.event.SysMenuChangeEvent;
import com.chua.starter.tenant.properties.TenantProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
* 菜单变更联动租户端同步监听器
* <p>
* 平台端菜单新增/修改/删除后，向订阅了包含该菜单的服务的租户推送增量变更
* （创建/更新 携带菜单数据 upsert，删除 仅携带菜单标识 触发租户端删除），
* 而非全量清空重录，从而保留租户端菜单记录与关联数据
* </p>
*
* @版本 1.0.0
* @since 4.0.0.42
* @author CH
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = TenantProperties.PRE + ".server", name = "enable", havingValue = "true")
public class TenantMenuChangeListener {

    /** 同步publisher提供者 */
    private final ObjectProvider<TenantServerSyncPublisher> syncPublisherProvider;

    /**
    * 监听菜单变更事件，触发关联租户菜单增量推送
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
            publisher.publishMenuChange(event.getSysMenuId(), event.getOperation());
        } catch (Exception e) {
            log.error("[租户菜单同步] 菜单变更联动推送失败", e);
        }
    }
}
