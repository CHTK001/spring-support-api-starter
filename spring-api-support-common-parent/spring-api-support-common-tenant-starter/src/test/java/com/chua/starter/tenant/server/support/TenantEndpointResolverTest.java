// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.tenant.server.support;

import com.chua.starter.sync.support.pojo.ClientInfo;
import com.chua.starter.tenant.entity.SysTenant;
import com.chua.starter.tenant.server.mapper.SysTenantMapper;
import com.chua.starter.tenant.sync.TenantSyncMessageSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link TenantEndpointResolver} 单元测试。
 *
 * <p>回答的是「这个租户的请求该打到哪台服务器」。覆盖三类判断：</p>
 * <ol>
 *   <li>同步注册来的地址优先，且必须是<b>当前仍在线</b>的实例；</li>
 *   <li>注册地址不可用时退回 {@code sys_tenant_home_url}，并标记为「未验证」；</li>
 *   <li>非 http(s) 地址一律拒收，避免下游消费时变成任意协议访问。</li>
 * </ol>
 *
 * <p>不使用 mockito（规约禁止），依赖用手写桩：{@link StubDirectory} 与动态代理的租户查询。</p>
 *
 * @since 4.0.0.52
 * @author CH
 */
class TenantEndpointResolverTest {

    /**
     * 被测租户标识
     */
    private static final String TENANT_ID = "1950000000000000001";

    @Test
    @DisplayName("租户标识为空时直接返回 null，不查库")
    void blankTenantIdReturnsNull() {
        TenantEndpointResolver resolver = resolver(new StubDirectory(), null);

        assertNull(resolver.resolve(null));
        assertNull(resolver.resolve("  "));
    }

    @Test
    @DisplayName("在线实例自报的基地址优先，且标记为在线")
    void prefersOnlineSyncBaseUrl() {
        StubDirectory directory = new StubDirectory();
        directory.register("s-1", client(TENANT_ID, "http://10.0.0.8:9000/api/", true), true);

        TenantEndpoint endpoint = resolver(directory, null).resolve(TENANT_ID);

        assertEquals("http://10.0.0.8:9000/api", endpoint.url(), "末尾斜杠必须去掉，否则拼路径会出现双斜杠");
        assertEquals(TenantEndpointResolver.SOURCE_SYNC, endpoint.source());
        assertTrue(endpoint.online());
        assertEquals("s-1", endpoint.clientId());
    }

    @Test
    @DisplayName("注册过但已断开的实例不参与解析，退回人工登记地址")
    void ignoresOfflineClient() {
        StubDirectory directory = new StubDirectory();
        directory.register("s-1", client(TENANT_ID, "http://10.0.0.8:9000", false), false);

        SysTenant tenant = tenant("https://t1.example.com");

        TenantEndpoint endpoint = resolver(directory, tenant).resolve(TENANT_ID);

        assertEquals(TenantEndpointResolver.SOURCE_CONFIG, endpoint.source());
        assertEquals("https://t1.example.com", endpoint.url());
        assertFalse(endpoint.online(), "人工登记地址没有连接为证，不能报在线");
        assertNull(endpoint.clientId());
    }

    @Test
    @DisplayName("自报地址缺失时按「注册 IP + 端口 + context-path」兜底")
    void fallsBackToRegisteredAddress() {
        StubDirectory directory = new StubDirectory();
        ClientInfo info = new ClientInfo();
        info.setClientMetadata(Map.of(TenantSyncMessageSupport.METADATA_TENANT_ID, TENANT_ID));
        info.setClientIpAddress("10.0.0.9");
        info.setClientPort(8080);
        info.setClientContextPath("app");
        info.setClientOnline(true);
        directory.register("s-1", info, true);

        TenantEndpoint endpoint = resolver(directory, null).resolve(TENANT_ID);

        assertEquals("http://10.0.0.9:8080/app", endpoint.url());
    }

    @Test
    @DisplayName("非 http(s) 的自报地址被拒收，继续走兜底")
    void rejectsNonHttpScheme() {
        StubDirectory directory = new StubDirectory();
        ClientInfo info = client(TENANT_ID, "file:///etc/passwd", true);
        info.setClientIpAddress("10.0.0.9");
        info.setClientPort(8081);
        directory.register("s-1", info, true);

        TenantEndpoint endpoint = resolver(directory, null).resolve(TENANT_ID);

        assertEquals("http://10.0.0.9:8081", endpoint.url());
    }

    @Test
    @DisplayName("人工登记的地址同样要过协议白名单")
    void rejectsNonHttpConfigUrl() {
        SysTenant tenant = tenant("ldap://evil.example.com");

        assertNull(resolver(new StubDirectory(), tenant).resolve(TENANT_ID));
    }

    @Test
    @DisplayName("多实例时解析结果稳定，不随 map 遍历顺序漂移")
    void stableWhenMultipleInstances() {
        StubDirectory directory = new StubDirectory();
        directory.register("s-2", client(TENANT_ID, "http://10.0.0.2:9000", true), true);
        directory.register("s-1", client(TENANT_ID, "http://10.0.0.1:9000", true), true);

        TenantEndpointResolver resolver = resolver(directory, null);

        assertEquals("s-1", resolver.resolve(TENANT_ID).clientId());
        assertEquals("s-1", resolver.resolve(TENANT_ID).clientId());
    }

    @Test
    @DisplayName("其它租户的实例不能串到本租户")
    void otherTenantClientIgnored() {
        StubDirectory directory = new StubDirectory();
        directory.register("s-1", client("other-tenant", "http://10.0.0.1:9000", true), true);

        assertNull(resolver(directory, null).resolve(TENANT_ID));
    }

    @Test
    @DisplayName("完全没有地址时 resolve 返回 null，require 抛出可读异常")
    void requireThrowsWhenNothingAvailable() {
        TenantEndpointResolver resolver = resolver(new StubDirectory(), null);

        assertNull(resolver.resolve(TENANT_ID));
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> resolver.require(TENANT_ID));
        assertTrue(error.getMessage().contains(TENANT_ID));
    }

    /**
     * 构造被测解析器。
     *
     * @param directory 在线实例目录
     * @param tenant    租户登记信息，可为空表示库里没有该租户
     * @return 解析器
     */
    private TenantEndpointResolver resolver(TenantClientDirectory directory, SysTenant tenant) {
        return new TenantEndpointResolver(directory, tenantMapperStub(tenant));
    }

    /**
     * 构造一个带租户标识的客户端。
     *
     * @param tenantId 租户标识
     * @param baseUrl  自报基地址
     * @param online   是否在线
     * @return 客户端信息
     */
    private ClientInfo client(String tenantId, String baseUrl, boolean online) {
        ClientInfo info = new ClientInfo();
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put(TenantSyncMessageSupport.METADATA_TENANT_ID, tenantId);
        metadata.put(TenantSyncMessageSupport.METADATA_TENANT_BASE_URL, baseUrl);
        info.setClientMetadata(metadata);
        info.setClientApplicationName("tenant-app");
        info.setClientOnline(online);
        return info;
    }

    /**
     * 构造租户登记信息。
     *
     * @param homeUrl 租户首页地址
     * @return 租户
     */
    private SysTenant tenant(String homeUrl) {
        SysTenant tenant = new SysTenant();
        tenant.setSysTenantId(TENANT_ID);
        tenant.setSysTenantHomeUrl(homeUrl);
        return tenant;
    }

    /**
     * 租户查询桩：用 JDK 动态代理，只实现 {@code selectById}。
     *
     * @param tenant 查询结果，可为空
     * @return 租户查询桩
     */
    private SysTenantMapper tenantMapperStub(SysTenant tenant) {
        return (SysTenantMapper) java.lang.reflect.Proxy.newProxyInstance(
                SysTenantMapper.class.getClassLoader(),
                new Class<?>[]{SysTenantMapper.class},
                (proxy, method, args) -> {
                    if ("selectById".equals(method.getName())) {
                        return tenant;
                    }
                    if ("toString".equals(method.getName())) {
                        return "StubTenantMapper";
                    }
                    return null;
                });
    }

    /**
     * 在线实例目录桩。
     */
    private static final class StubDirectory implements TenantClientDirectory {

        /**
         * 注册的客户端
         */
        private final Map<String, ClientInfo> clients = new LinkedHashMap<>();

        /**
         * 仍在连接的会话
         */
        private final Set<String> connected = new java.util.LinkedHashSet<>();

        /**
         * 登记一个客户端。
         *
         * @param sessionId 会话标识
         * @param info      客户端信息
         * @param online    是否仍在连接
         */
        private void register(String sessionId, ClientInfo info, boolean online) {
            clients.put(sessionId, info);
            if (online) {
                connected.add(sessionId);
            }
        }

        @Override
        public Map<String, ClientInfo> registeredClients() {
            return clients;
        }

        @Override
        public Set<String> connectedSessions() {
            return connected;
        }
    }
}