package com.chua.starter.tenant.server.sync;

import com.chua.starter.entity.system.SysMenu;
import com.chua.starter.sync.support.pojo.ClientInfo;
import com.chua.starter.sync.support.server.SyncServer;
import com.chua.starter.system.mapper.SysMenuMapper;
import com.chua.starter.tenant.entity.SysTenant;
import com.chua.starter.tenant.properties.TenantProperties;
import com.chua.starter.tenant.server.mapper.SysTenantMapper;
import com.chua.starter.tenant.server.mapper.SysTenantServiceMapper;
import com.chua.starter.tenant.sync.TenantSyncMessageSupport;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 租户服务端同步发布器
 * <p>
 * 服务端模式下，用于向客户端推送租户数据变更
 * </p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/06
 */
@Component
@RequiredArgsConstructor
@ConditionalOnBean(SyncServer.class)
@ConditionalOnProperty(prefix = TenantProperties.PRE + ".server", name = "enable", havingValue = "true")
public class TenantServerSyncPublisher implements InitializingBean, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(TenantServerSyncPublisher.class);

    /**
     * 租户同步主题
     */
    public static final String TOPIC_TENANT_UPDATE = TenantSyncMessageSupport.TOPIC_TENANT_UPDATE;
    public static final String TOPIC_TENANT_DELETE = TenantSyncMessageSupport.TOPIC_TENANT_DELETE;
    public static final String TOPIC_TENANT_SERVICE = TenantSyncMessageSupport.TOPIC_TENANT_SERVICE;
    public static final String TOPIC_TENANT_MENU = TenantSyncMessageSupport.TOPIC_TENANT_MENU;

    private static final String REASON_REGISTER = "client-register";
    private static final String REASON_SCHEDULED = "scheduled-reconcile";
    private static final String REASON_TENANT_UPDATE = "tenant-update";
    private static final String REASON_TENANT_DELETE = "tenant-delete";
    private static final String REASON_SERVICE_UPDATE = "service-update";
    private static final String REASON_MENU_UPDATE = "menu-update";

    private final TenantProperties tenantProperties;
    private final ObjectProvider<SyncServer> syncServerProvider;
    private final SysTenantMapper sysTenantMapper;
    private final SysTenantServiceMapper sysTenantServiceMapper;
    private final SysMenuMapper sysMenuMapper;
    private final TenantSyncAckTracker syncAckTracker;

    private volatile SyncServer syncServer;
    private volatile ScheduledExecutorService fullPushScheduler;
    private final ConcurrentMap<String, String> fullSyncFingerprints = new ConcurrentHashMap<>();

    @Override
    public void afterPropertiesSet() {
        syncServer = syncServerProvider.getIfAvailable();
        if (!isSyncEnabled() || syncServer == null) {
            return;
        }

        if (tenantProperties.getServer().isSyncFullPushOnRegister()) {
            syncServer.addConnectListener((sessionId, clientInfo) -> handleTenantClientRegister(sessionId, clientInfo));
        }
        syncServer.addDisconnectListener((sessionId, clientInfo) -> syncAckTracker.clearSession(sessionId));

        long intervalSeconds = tenantProperties.getServer().getSyncFullPushIntervalSeconds();
        long ackCheckIntervalSeconds = tenantProperties.isServerSyncAckEnabled()
                ? tenantProperties.getServer().getSyncAckCheckIntervalSeconds()
                : 0L;
        if (intervalSeconds > 0 || ackCheckIntervalSeconds > 0) {
            fullPushScheduler = Executors.newScheduledThreadPool(2, runnable -> {
                Thread thread = new Thread(runnable, "tenant-sync-full-push");
                thread.setDaemon(true);
                return thread;
            });
            if (intervalSeconds > 0) {
                fullPushScheduler.scheduleWithFixedDelay(
                        this::publishConnectedTenantsFullSyncSafely,
                        intervalSeconds,
                        intervalSeconds,
                        TimeUnit.SECONDS
                );
            }
            if (ackCheckIntervalSeconds > 0) {
                fullPushScheduler.scheduleWithFixedDelay(
                        this::checkPendingAcksSafely,
                        ackCheckIntervalSeconds,
                        ackCheckIntervalSeconds,
                        TimeUnit.SECONDS
                );
            }
        }
    }

    @Override
    public void destroy() {
        if (fullPushScheduler != null) {
            fullPushScheduler.shutdownNow();
        }
    }

    /**
     * 发布租户更新
     *
     * @param tenant 租户信息
     */
    public void publishTenantUpdate(SysTenant tenant) {
        if (!canPublish() || tenant == null || !TenantSyncMessageSupport.hasText(tenant.getSysTenantId())) {
            return;
        }

        try {
            Map<String, Object> data = TenantSyncMessageSupport.createEnvelope(
                    tenant.getSysTenantId(),
                    TenantSyncMessageSupport.MODE_INCREMENTAL,
                    REASON_TENANT_UPDATE
            );
            data.put(TenantSyncMessageSupport.TENANT, convertTenantToMap(tenant));
            int sent = publishToTenantClients(tenant.getSysTenantId(), TOPIC_TENANT_UPDATE, data);
            log.info("[租户服务端] 发布租户更新: tenantId={}, receivers={}", tenant.getSysTenantId(), sent);
        } catch (Exception e) {
            log.error("[租户服务端] 发布租户更新失败", e);
        }
    }

    /**
     * 发布租户删除
     *
     * @param tenantId 租户ID
     */
    public void publishTenantDelete(String tenantId) {
        if (!canPublish() || !TenantSyncMessageSupport.hasText(tenantId)) {
            return;
        }

        try {
            Map<String, Object> data = TenantSyncMessageSupport.createEnvelope(
                    tenantId,
                    TenantSyncMessageSupport.MODE_INCREMENTAL,
                    REASON_TENANT_DELETE
            );
            int sent = publishToTenantClients(tenantId, TOPIC_TENANT_DELETE, data);
            log.info("[租户服务端] 发布租户删除: tenantId={}, receivers={}", tenantId, sent);
        } catch (Exception e) {
            log.error("[租户服务端] 发布租户删除失败", e);
        }
    }

    /**
     * 发布服务/菜单更新
     *
     * @param tenantId 租户ID
     * @param menuIds  菜单ID列表
     */
    public void publishServiceUpdate(String tenantId, List<Integer> menuIds) {
        if (!canPublish() || !TenantSyncMessageSupport.hasText(tenantId)) {
            return;
        }

        try {
            publishServiceUpdateInternal(
                    tenantId,
                    menuIds == null ? loadTenantMenuIds(tenantId) : menuIds,
                    TenantSyncMessageSupport.MODE_INCREMENTAL,
                    REASON_SERVICE_UPDATE
            );
        } catch (Exception e) {
            log.error("[租户服务端] 发布服务更新失败", e);
        }
    }

    /**
     * 发布菜单数据更新
     *
     * @param tenantId  租户ID
     * @param menus     菜单数据列表
     * @param operation 操作类型: create, update, delete
     */
    public void publishMenuUpdate(String tenantId, List<Map<String, Object>> menus, String operation) {
        if (!canPublish() || !TenantSyncMessageSupport.hasText(tenantId)) {
            return;
        }

        try {
            List<Map<String, Object>> safeMenus = menus == null ? List.of() : menus;
            Map<String, Object> data = TenantSyncMessageSupport.createEnvelope(
                    tenantId,
                    TenantSyncMessageSupport.MODE_INCREMENTAL,
                    REASON_MENU_UPDATE
            );
            data.put(TenantSyncMessageSupport.MENUS, safeMenus);
            data.put(TenantSyncMessageSupport.OPERATION, operation);
            int sent = publishToTenantClients(tenantId, TOPIC_TENANT_MENU, data);
            log.info("[租户服务端] 发布菜单更新: tenantId={}, operation={}, count={}, receivers={}",
                    tenantId, operation, safeMenus.size(), sent);
        } catch (Exception e) {
            log.error("[租户服务端] 发布菜单更新失败", e);
        }
    }

    /**
     * 向指定客户端会话补发全量同步。
     *
     * @param sessionId 会话ID
     * @param tenantId  租户ID
     * @param reason    原因
     * @return 是否发送成功
     */
    public boolean publishFullSyncToSession(String sessionId, String tenantId, String reason) {
        if (!canPublish() || !TenantSyncMessageSupport.hasText(sessionId) || !TenantSyncMessageSupport.hasText(tenantId)) {
            return false;
        }

        return publishFullSyncForTenant(tenantId, reason, sessionId) > 0;
    }

    /**
     * 检查是否可以发布
     */
    private boolean canPublish() {
        if (syncServer == null || !isSyncEnabled()) {
            log.debug("[租户服务端] SyncServer 未注入，无法发布消息");
            return false;
        }
        return true;
    }

    private boolean isSyncEnabled() {
        return tenantProperties.isServerMode() && tenantProperties.getServer().isSyncEnable();
    }

    private void handleTenantClientRegister(String sessionId, ClientInfo clientInfo) {
        try {
            if (!isTenantClient(clientInfo)) {
                return;
            }

            String tenantId = resolveTenantId(clientInfo);
            if (!TenantSyncMessageSupport.hasText(tenantId)) {
                log.warn("[租户服务端] 租户客户端注册缺少 tenantId，sessionId={}", sessionId);
                return;
            }

            publishFullSyncForTenant(tenantId, REASON_REGISTER, sessionId);
        } catch (Exception e) {
            log.error("[租户服务端] 处理租户客户端注册补偿失败: sessionId={}", sessionId, e);
        }
    }

    private void publishConnectedTenantsFullSyncSafely() {
        try {
            publishConnectedTenantsFullSync();
        } catch (Exception e) {
            log.error("[租户服务端] 定时全量同步失败", e);
        }
    }

    private void checkPendingAcksSafely() {
        try {
            checkPendingAcks();
        } catch (Exception e) {
            log.error("[租户服务端] ACK 超时检查失败", e);
        }
    }

    private void publishConnectedTenantsFullSync() {
        if (!canPublish()) {
            return;
        }

        Set<String> tenantIds = new LinkedHashSet<>();
        for (ClientInfo clientInfo : syncServer.getAllClientsRaw().values()) {
            if (!isTenantClient(clientInfo)) {
                continue;
            }

            String tenantId = resolveTenantId(clientInfo);
            if (TenantSyncMessageSupport.hasText(tenantId)) {
                tenantIds.add(tenantId);
            }
        }

        for (String tenantId : tenantIds) {
            publishFullSyncForTenant(tenantId, REASON_SCHEDULED, null);
        }
    }

    private int publishFullSyncForTenant(String tenantId, String reason, String sessionId) {
        if (!TenantSyncMessageSupport.hasText(tenantId)) {
            return 0;
        }

        SysTenant tenant = sysTenantMapper.selectById(tenantId);
        if (tenant == null || Objects.equals(tenant.getSysTenantDelete(), 1)) {
            String fingerprint = buildDeletedTenantFingerprint(tenantId);
            if (shouldSkipUnchangedFullSync(tenantId, reason, sessionId, fingerprint)) {
                return 0;
            }

            Map<String, Object> deleteMessage = TenantSyncMessageSupport.createEnvelope(
                    tenantId,
                    TenantSyncMessageSupport.MODE_FULL,
                    reason
            );
            int sent = publishToTarget(sessionId, tenantId, TOPIC_TENANT_DELETE, deleteMessage);
            if (sent > 0) {
                fullSyncFingerprints.put(tenantId, fingerprint);
            }
            return sent;
        }

        List<Integer> menuIds = loadTenantMenuIds(tenantId);
        List<Map<String, Object>> menus = loadTenantMenus(menuIds);
        String fingerprint = buildTenantSnapshotFingerprint(tenantId, tenant, menuIds, menus);
        if (shouldSkipUnchangedFullSync(tenantId, reason, sessionId, fingerprint)) {
            return 0;
        }

        Map<String, Object> tenantMessage = TenantSyncMessageSupport.createEnvelope(
                tenantId,
                TenantSyncMessageSupport.MODE_FULL,
                reason
        );
        tenantMessage.put(TenantSyncMessageSupport.TENANT, convertTenantToMap(tenant));
        int sent = publishToTarget(sessionId, tenantId, TOPIC_TENANT_UPDATE, tenantMessage);

        int serviceSent = publishServiceUpdateInternal(
                tenantId,
                menuIds,
                menus,
                TenantSyncMessageSupport.MODE_FULL,
                reason,
                sessionId
        );
        if (Math.max(sent, serviceSent) > 0) {
            fullSyncFingerprints.put(tenantId, fingerprint);
        }
        return Math.max(sent, serviceSent);
    }

    private int publishServiceUpdateInternal(String tenantId, List<Integer> menuIds, String syncMode, String reason) {
        return publishServiceUpdateInternal(tenantId, menuIds, syncMode, reason, null);
    }

    private int publishServiceUpdateInternal(String tenantId, List<Integer> menuIds, String syncMode, String reason, String sessionId) {
        return publishServiceUpdateInternal(tenantId, menuIds, null, syncMode, reason, sessionId);
    }

    private int publishServiceUpdateInternal(String tenantId,
                                             List<Integer> menuIds,
                                             List<Map<String, Object>> preloadedMenus,
                                             String syncMode,
                                             String reason,
                                             String sessionId) {
        List<Integer> safeMenuIds = menuIds == null ? List.of() : menuIds.stream().filter(Objects::nonNull).distinct().toList();
        List<Map<String, Object>> menus = preloadedMenus == null ? loadTenantMenus(safeMenuIds) : preloadedMenus;

        Map<String, Object> data = TenantSyncMessageSupport.createEnvelope(tenantId, syncMode, reason);
        data.put(TenantSyncMessageSupport.MENU_IDS, safeMenuIds);
        data.put(TenantSyncMessageSupport.MENUS, menus);
        data.put(TenantSyncMessageSupport.OPERATION, TenantSyncMessageSupport.OP_REPLACE);

        int sent = publishToTarget(sessionId, tenantId, TOPIC_TENANT_SERVICE, data);
        log.info("[租户服务端] 发布服务更新: tenantId={}, menuCount={}, receivers={}, mode={}",
                tenantId, menus.size(), sent, syncMode);
        return sent;
    }

    private List<Integer> loadTenantMenuIds(String tenantId) {
        List<Integer> menuIds = sysTenantServiceMapper.getMenuByTenantId(tenantId);
        if (menuIds == null || menuIds.isEmpty()) {
            return List.of();
        }
        return menuIds.stream().filter(Objects::nonNull).distinct().toList();
    }

    private List<Map<String, Object>> loadTenantMenus(List<Integer> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return List.of();
        }

        List<SysMenu> selectedMenus = sysMenuMapper.selectBatchIds(menuIds);
        if (selectedMenus == null || selectedMenus.isEmpty()) {
            return List.of();
        }

        List<SysMenu> menus = new ArrayList<>(selectedMenus);
        menus.sort(Comparator.comparingInt(menu -> menu.getSysMenuSort() == null ? 0 : menu.getSysMenuSort()));
        return menus.stream().map(this::convertMenuToMap).toList();
    }

    private int publishToTenantClients(String tenantId, String topic, Map<String, Object> data) {
        Map<String, Object> payload = prepareOutboundMessage(data);
        List<String> sessionIds = findTenantClientSessionIds(tenantId);
        if (sessionIds.isEmpty() && tenantProperties.getServer().isSyncBroadcastFallback()) {
            log.warn("[租户服务端] 未找到租户客户端，回退广播: tenantId={}, topic={}", tenantId, topic);
            syncServer.broadcast(topic, payload);
            return 0;
        }
        if (sessionIds.isEmpty()) {
            log.debug("[租户服务端] 未找到租户客户端，跳过发送: tenantId={}, topic={}", tenantId, topic);
            return 0;
        }

        for (String sessionId : sessionIds) {
            sendTracked(sessionId, tenantId, topic, payload);
        }
        return sessionIds.size();
    }

    private int publishToTarget(String sessionId, String tenantId, String topic, Map<String, Object> data) {
        Map<String, Object> payload = prepareOutboundMessage(data);
        if (TenantSyncMessageSupport.hasText(sessionId)) {
            ClientInfo clientInfo = syncServer.getAllClientsRaw().get(sessionId);
            if (!isTenantClient(clientInfo, tenantId)) {
                log.warn("[租户服务端] 会话与租户不匹配，跳过定向补发: sessionId={}, tenantId={}, topic={}",
                        sessionId, tenantId, topic);
                return 0;
            }
            sendTracked(sessionId, tenantId, topic, payload);
            return 1;
        }
        return publishToTenantClients(tenantId, topic, payload);
    }

    private void sendTracked(String sessionId, String tenantId, String topic, Map<String, Object> payload) {
        syncServer.send(sessionId, topic, payload);
        ClientInfo clientInfo = syncServer.getAllClientsRaw().get(sessionId);
        if (supportsAck(clientInfo)) {
            syncAckTracker.track(sessionId, tenantId, topic, payload);
        }
    }

    private List<String> findTenantClientSessionIds(String tenantId) {
        List<String> sessionIds = new ArrayList<>();
        for (Map.Entry<String, ClientInfo> entry : syncServer.getAllClientsRaw().entrySet()) {
            if (isTenantClient(entry.getValue(), tenantId)) {
                sessionIds.add(entry.getKey());
            }
        }
        return sessionIds;
    }

    private boolean isTenantClient(ClientInfo clientInfo) {
        return isTenantClient(clientInfo, null);
    }

    private boolean isTenantClient(ClientInfo clientInfo, String tenantId) {
        if (clientInfo == null || clientInfo.getClientMetadata() == null) {
            return false;
        }

        String boundTenantId = resolveTenantId(clientInfo);
        Object syncRole = clientInfo.getClientMetadata().get(TenantSyncMessageSupport.METADATA_SYNC_ROLE);
        boolean explicitTenantClient = Objects.equals(String.valueOf(syncRole), TenantSyncMessageSupport.ROLE_TENANT_CLIENT);
        boolean legacyTenantClient = TenantSyncMessageSupport.hasText(boundTenantId);
        if (!explicitTenantClient && !legacyTenantClient) {
            return false;
        }

        if (!TenantSyncMessageSupport.hasText(tenantId)) {
            return true;
        }
        return Objects.equals(boundTenantId, tenantId);
    }

    private String resolveTenantId(ClientInfo clientInfo) {
        if (clientInfo == null || clientInfo.getClientMetadata() == null) {
            return null;
        }

        return TenantSyncMessageSupport.resolveTenantId(clientInfo);
    }

    private boolean supportsAck(ClientInfo clientInfo) {
        if (!tenantProperties.isServerSyncAckEnabled() || clientInfo == null) {
            return false;
        }

        Map<String, Object> metadata = clientInfo.getClientMetadata();
        if (metadata != null && Boolean.TRUE.equals(metadata.get("tenantSyncAckEnabled"))) {
            return true;
        }

        String[] capabilities = clientInfo.getClientCapabilities();
        if (capabilities == null || capabilities.length == 0) {
            return false;
        }

        for (String capability : capabilities) {
            if (Objects.equals("tenant-sync-ack", capability)) {
                return true;
            }
        }
        return false;
    }

    private Map<String, Object> prepareOutboundMessage(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return data;
        }
        if (tenantProperties.isServerSyncSignEnabled()) {
            TenantSyncMessageSupport.sign(data, tenantProperties.getServer().getSyncSignSecret());
        }
        return data;
    }

    private boolean shouldSkipUnchangedFullSync(String tenantId, String reason, String sessionId, String fingerprint) {
        if (!tenantProperties.getServer().isSyncSkipUnchangedFullPush()
                || !tenantProperties.getServer().isSyncFullPushOnRegister()
                || !REASON_SCHEDULED.equals(reason)
                || TenantSyncMessageSupport.hasText(sessionId)
                || !TenantSyncMessageSupport.hasText(fingerprint)) {
            return false;
        }

        boolean unchanged = Objects.equals(fullSyncFingerprints.get(tenantId), fingerprint);
        if (unchanged) {
            log.debug("[租户服务端] 跳过未变化的定时全量同步: tenantId={}", tenantId);
        }
        return unchanged;
    }

    private void checkPendingAcks() {
        if (!canPublish() || !tenantProperties.isServerSyncAckEnabled()) {
            return;
        }

        long timeoutMillis = Math.max(1L, tenantProperties.getServer().getSyncAckTimeoutSeconds()) * 1000L;
        long cooldownMillis = Math.max(0L, tenantProperties.getServer().getSyncAckCompensationCooldownSeconds()) * 1000L;
        List<TenantSyncAckTracker.PendingAck> expiredAcks = syncAckTracker.drainExpired(timeoutMillis);
        if (expiredAcks.isEmpty()) {
            return;
        }

        for (TenantSyncAckTracker.PendingAck pendingAck : expiredAcks) {
            ClientInfo clientInfo = syncServer.getAllClientsRaw().get(pendingAck.getSessionId());
            if (!isTenantClient(clientInfo, pendingAck.getTenantId())) {
                continue;
            }
            if (!syncAckTracker.tryMarkCompensation(pendingAck.getSessionId(), pendingAck.getTenantId(), cooldownMillis)) {
                continue;
            }

            log.warn("[租户服务端] 租户同步 ACK 超时，执行定向全量补偿: sessionId={}, tenantId={}, topic={}, messageId={}, reason={}",
                    pendingAck.getSessionId(), pendingAck.getTenantId(), pendingAck.getTopic(),
                    pendingAck.getMessageId(), pendingAck.getReason());
            publishFullSyncToSession(pendingAck.getSessionId(), pendingAck.getTenantId(), "ack-timeout");
        }
    }

    private String buildTenantSnapshotFingerprint(String tenantId,
                                                  SysTenant tenant,
                                                  List<Integer> menuIds,
                                                  List<Map<String, Object>> menus) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put(TenantSyncMessageSupport.TARGET_TENANT_ID, tenantId);
        snapshot.put(TenantSyncMessageSupport.TENANT, convertTenantToMap(tenant));
        snapshot.put(TenantSyncMessageSupport.MENU_IDS, menuIds == null ? List.of() : menuIds);
        snapshot.put(TenantSyncMessageSupport.MENUS, menus == null ? List.of() : menus);
        snapshot.put(TenantSyncMessageSupport.OPERATION, TenantSyncMessageSupport.OP_REPLACE);
        return TenantSyncMessageSupport.payloadFingerprint(snapshot);
    }

    private String buildDeletedTenantFingerprint(String tenantId) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put(TenantSyncMessageSupport.TARGET_TENANT_ID, tenantId);
        snapshot.put("deleted", true);
        return TenantSyncMessageSupport.payloadFingerprint(snapshot);
    }

    /**
     * 转换租户为 Map
     */
    private Map<String, Object> convertTenantToMap(SysTenant tenant) {
        Map<String, Object> map = new HashMap<>();
        map.put("sysTenantId", tenant.getSysTenantId());
        map.put("sysTenantCode", tenant.getSysTenantCode());
        map.put("sysTenantName", tenant.getSysTenantName());
        map.put("sysTenantUsername", tenant.getSysTenantUsername());
        map.put("sysTenantPassword", tenant.getSysTenantPassword());
        map.put("sysTenantSign", tenant.getSysTenantSign());
        map.put("sysTenantCorporation", tenant.getSysTenantCorporation());
        map.put("sysTenantContact", tenant.getSysTenantContact());
        map.put("sysTenantPhone", tenant.getSysTenantPhone());
        map.put("sysTenantAddress", tenant.getSysTenantAddress());
        map.put("sysTenantEmail", tenant.getSysTenantEmail());
        map.put("sysTenantStatus", tenant.getSysTenantStatus());
        map.put("sysTenantDelete", tenant.getSysTenantDelete());
        map.put("sysTenantGid", tenant.getSysTenantGid());
        map.put("sysTenantHomeUrl", tenant.getSysTenantHomeUrl());
        map.put("sysTenantRemark", tenant.getSysTenantRemark());
        return map;
    }

    private Map<String, Object> convertMenuToMap(SysMenu menu) {
        Map<String, Object> map = new HashMap<>();
        map.put("sysMenuId", menu.getSysMenuId());
        map.put("sysMenuName", menu.getSysMenuName());
        map.put("sysMenuPath", menu.getSysMenuPath());
        map.put("sysMenuI18n", menu.getSysMenuI18n());
        map.put("sysMenuTitle", menu.getSysMenuTitle());
        map.put("sysMenuPerm", menu.getSysMenuPerm());
        map.put("sysMenuPid", menu.getSysMenuPid());
        map.put("sysMenuTreeId", menu.getSysMenuTreeId());
        map.put("sysMenuComponent", menu.getSysMenuComponent());
        map.put("sysMenuIcon", menu.getSysMenuIcon());
        map.put("sysMenuExtraIcon", menu.getSysMenuExtraIcon());
        map.put("sysMenuType", menu.getSysMenuType());
        map.put("sysMenuSort", menu.getSysMenuSort());
        map.put("sysMenuHidden", menu.getSysMenuHidden());
        map.put("sysMenuShowParent", menu.getSysMenuShowParent());
        map.put("sysMenuHiddenTag", menu.getSysMenuHiddenTag());
        map.put("sysMenuFixedTag", menu.getSysMenuFixedTag());
        map.put("sysMenuRedirect", menu.getSysMenuRedirect());
        map.put("sysMenuEnterTransition", menu.getSysMenuEnterTransition());
        map.put("sysMenuLeaveTransition", menu.getSysMenuLeaveTransition());
        map.put("sysMenuActivePath", menu.getSysMenuActivePath());
        map.put("sysMenuFrameSrc", menu.getSysMenuFrameSrc());
        map.put("sysMenuKeepAlive", menu.getSysMenuKeepAlive());
        map.put("sysMenuRole", menu.getSysMenuRole());
        map.put("sysMenuHome", menu.getSysMenuHome());
        map.put("sysMenuEngine", menu.getSysMenuEngine());
        return map;
    }
}
