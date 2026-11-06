package com.chua.starter.tenant.server.support;

import com.chua.starter.sync.support.pojo.ClientInfo;
import com.chua.starter.tenant.entity.SysTenant;
import com.chua.starter.tenant.server.mapper.SysTenantMapper;
import com.chua.starter.tenant.sync.TenantSyncMessageSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 租户服务器地址解析器。
 *
 * <p>回答的是「这个租户的请求该打到哪台服务器」，数据源按可信度分两级：</p>
 *
 * <ol>
 *   <li><b>同步注册（首选）</b>——租户端应用启动后会自动连上平台的同步服务端，
 *       {@code TenantSyncClientInfoCustomizer} 把租户标识和本次可访问的基地址写进
 *       {@code clientMetadata}。这类地址有连接为证，说明进程此刻真的活着。</li>
 *   <li><b>人工登记（兜底）</b>——{@code sys_tenant_home_url}。租户端没连上来时用它，
 *       但地址只是字符串，没有任何存活证明，因此 {@link TenantEndpoint#online()} 恒为
 *       {@code false}，调用方不能据此判断服务健康。</li>
 * </ol>
 *
 * <p><b>为什么这两个都要</b>：只有注册信息时，租户端还没启动或同步通道断开就完全查不到地址；
 * 只有人工登记时，租户端扩容、换端口后平台还在往旧地址打。两者合并取「有连接的优先」，
 * 才既能自动跟随又能人工干预。</p>
 *
 * <p><b>URL 白名单</b>：只接受 {@code http://} / {@code https://}。地址最终来自
 * 租户自己的配置或平台管理员填写，若允许 {@code file:} / {@code ldap:} 之类的 scheme，
 * 下游一旦用 {@code RestTemplate} 或浏览器跳转消费就会变成任意协议访问。</p>
 *
 * @since 4.0.0.52
 * @author CH
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantEndpointResolver {

    /**
     * 地址来源：同步注册
     */
    public static final String SOURCE_SYNC = "sync";

    /**
     * 地址来源：人工登记
     */
    public static final String SOURCE_CONFIG = "config";

    /**
     * 协议前缀
     */
    private static final String HTTP_PREFIX = "http://";

    /**
     * 安全协议前缀
     */
    private static final String HTTPS_PREFIX = "https://";

    /**
     * 租户端在线目录
     */
    private final TenantClientDirectory clientDirectory;

    /**
     * 租户查询
     */
    private final SysTenantMapper sysTenantMapper;

    /**
     * 解析租户服务器地址。
     *
     * @param tenantId 租户标识
     * @return 地址；租户既没在线实例也没登记地址时返回 {@code null}
     */
    public TenantEndpoint resolve(String tenantId) {
        if (!hasText(tenantId)) {
            return null;
        }
        TenantEndpoint online = resolveFromSync(tenantId);
        if (online != null) {
            return online;
        }
        return resolveFromConfig(tenantId);
    }

    /**
     * 解析租户服务器地址，必填版本。
     *
     * @param tenantId 租户标识
     * @return 地址，一定非空
     * @throws IllegalStateException 租户没有可用地址时抛出
     */
    public TenantEndpoint require(String tenantId) {
        TenantEndpoint endpoint = resolve(tenantId);
        if (endpoint == null) {
            throw new IllegalStateException("租户 " + tenantId
                    + " 没有可用的服务器地址：既无在线实例，sys_tenant_home_url 也未登记");
        }
        return endpoint;
    }

    /**
     * 从同步注册里找在线实例。
     *
     * @param tenantId 租户标识
     * @return 地址；没有在线实例时返回 {@code null}
     */
    private TenantEndpoint resolveFromSync(String tenantId) {
        Map<String, ClientInfo> clients = clientDirectory.registeredClients();
        if (clients == null || clients.isEmpty()) {
            return null;
        }
        Set<String> connected = clientDirectory.connectedSessions();
        if (log.isDebugEnabled()) {
            // 排查「租户端明明报了地址却解析不到」时，先看注册表的真实内容：
            // metadata 为空说明注册消息没被解析成结构化数据，online 为 false 说明会话不在连接集合里
            clients.forEach((id, info) -> log.debug("[租户服务器] 已注册客户端 id={}, online={}, metadata={}",
                    id, connected != null && connected.contains(id) || (info != null && info.isClientOnline()),
                    info == null ? null : info.getClientMetadata()));
        }
        List<TenantEndpoint> candidates = new ArrayList<>();
        for (Map.Entry<String, ClientInfo> entry : clients.entrySet()) {
            ClientInfo clientInfo = entry.getValue();
            if (!belongsToTenant(clientInfo, tenantId)) {
                continue;
            }
            // 注册过但已断开的不算在线：拿旧地址去打只会换来连接超时
            boolean online = (connected != null && connected.contains(entry.getKey()))
                    || (clientInfo != null && clientInfo.isClientOnline());
            if (!online) {
                continue;
            }
            String url = resolveClientUrl(clientInfo);
            if (url == null) {
                continue;
            }
            candidates.add(new TenantEndpoint(tenantId, url, SOURCE_SYNC, true,
                    entry.getKey(),
                    normalizeApplicationName(clientInfo == null ? null : clientInfo.getClientApplicationName())));
        }
        if (candidates.isEmpty()) {
            return null;
        }
        // 会话标识排序：多个实例时结果必须稳定，否则同一租户两次解析可能落到不同节点，
        // 排查问题时看到的现象会变得无法复现
        candidates.sort(Comparator.comparing(TenantEndpoint::clientId,
                Comparator.nullsLast(Comparator.naturalOrder())));
        return candidates.getFirst();
    }

    /**
     * 从租户登记信息里取地址。
     *
     * @param tenantId 租户标识
     * @return 地址；未登记返回 {@code null}
     */
    private TenantEndpoint resolveFromConfig(String tenantId) {
        SysTenant tenant = sysTenantMapper.selectById(tenantId);
        if (tenant == null) {
            return null;
        }
        String url = normalize(tenant.getSysTenantHomeUrl());
        if (url == null) {
            log.debug("[租户服务器] 租户 {} 未登记 sys_tenant_home_url，无法解析地址", tenantId);
            return null;
        }
        return new TenantEndpoint(tenantId, url, SOURCE_CONFIG, false, null, null);
    }

    /**
     * 判断客户端是否属于该租户。
     *
     * @param clientInfo 客户端信息
     * @param tenantId   租户标识
     * @return 属于返回 true
     */
    private boolean belongsToTenant(ClientInfo clientInfo, String tenantId) {
        if (clientInfo == null) {
            return false;
        }
        String bound = TenantSyncMessageSupport.resolveTenantId(clientInfo);
        return Objects.equals(tenantId, bound);
    }

    /**
     * 取客户端可访问地址。
     *
     * <p>优先用客户端自报的基地址（{@code tenantBaseUrl}）：它已经带上了正确的对外 IP、
     * HTTP 端口与 context-path。缺失时退回「注册 IP + 端口 + context-path」拼接，
     * 那是同步通道自己的端口，只有在租户端恰好把业务服务与同步服务跑在同一端口时才对，
     * 因此拼接结果只做兜底。</p>
     *
     * @param clientInfo 客户端信息
     * @return 规范化后的地址；无法确定时返回 {@code null}
     */
    private String resolveClientUrl(ClientInfo clientInfo) {
        if (clientInfo == null) {
            return null;
        }
        Map<String, Object> metadata = clientInfo.getClientMetadata();
        if (metadata != null) {
            Object advertised = metadata.get(TenantSyncMessageSupport.METADATA_TENANT_BASE_URL);
            String normalized = advertised == null ? null : normalize(String.valueOf(advertised));
            if (normalized != null) {
                return normalized;
            }
        }

        String ip = clientInfo.getClientIpAddress();
        int port = clientInfo.getClientPort();
        if (!hasText(ip) || port <= 0) {
            return null;
        }
        String contextPath = clientInfo.getClientContextPath();
        String path = hasText(contextPath) && !"/".equals(contextPath)
                ? (contextPath.startsWith("/") ? contextPath : "/" + contextPath)
                : "";
        return normalize(HTTP_PREFIX + ip.trim() + ":" + port + path);
    }

    /**
     * 规范化地址。
     *
     * @param url 原始地址
     * @return 去掉首尾空白与末尾斜杠的地址；协议不允许或内容为空时返回 {@code null}
     */
    private String normalize(String url) {
        if (!hasText(url)) {
            return null;
        }
        String value = url.trim();
        String lower = value.toLowerCase();
        if (!lower.startsWith(HTTP_PREFIX) && !lower.startsWith(HTTPS_PREFIX)) {
            log.warn("[租户服务器] 忽略非 http(s) 地址: {}", value);
            return null;
        }
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        // "http://" 本身合法但不可用，去掉斜杠后长度为协议前缀长度
        if (value.length() <= HTTP_PREFIX.length()) {
            return null;
        }
        return value;
    }

    /**
     * 规范化应用名。
     *
     * <p>注册消息里缺失的字段经 JSON 序列化后会变成字符串 {@code "null"}，
     * 直接展示等于告诉运维这台机器叫 null。空白与 {@code "null"} 一律视为未上报。</p>
     *
     * @param applicationName 原始应用名
     * @return 规范化后的应用名；无有效值时返回 {@code null}
     */
    private static String normalizeApplicationName(String applicationName) {
        if (applicationName == null) {
            return null;
        }
        String value = applicationName.trim();
        if (value.isEmpty() || "null".equalsIgnoreCase(value)) {
            return null;
        }
        return value;
    }

    /**
     * 是否包含文本。
     *
     * @param value 待判断值
     * @return 非空白返回 true
     */
    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
