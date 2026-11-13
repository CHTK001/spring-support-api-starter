// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.tenant.server.support;

import com.chua.starter.common.support.oauth.AuthService;
import com.chua.starter.common.support.oauth.CurrentUser;
import com.chua.starter.datasource.support.DataSourceContextSupport;
import com.chua.starter.tenant.entity.SysTenant;
import com.chua.starter.tenant.properties.TenantProperties;
import com.chua.starter.tenant.server.mapper.SysTenantMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.Ordered;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link TenantDataSourceFilter} 单元测试。
 *
 * <p>重点在两条「错了就是安全漏洞」的规则：</p>
 * <ol>
 *   <li>默认配置下 {@code X-Tenant-Id} 请求头<b>不得</b>切库——否则改个头就能读别人的库；</li>
 *   <li>任何路径都要在请求结束时清空线程变量——否则线程池复用会把上一个请求的租户带给下一个。</li>
 * </ol>
 *
 * <p>不使用 mockito（规约禁止），依赖用手写桩：{@link StubTenantMapper}、
 * {@link FixedAuthService}、{@link StubRegistry}。</p>
 *
 * @since 4.0.0.51
 * @author CH
 */
class TenantDataSourceFilterTest {

    /**
     * 租户配置
     */
    private TenantProperties properties;

    /**
     * 请求头里声明的租户标识
     */
    private static final String HEADER_TENANT = "1950000000000000001";

    /**
     * 会话里的租户标识
     */
    private static final String SESSION_TENANT = "1950000000000000002";

    @BeforeEach
    void setUp() {
        properties = new TenantProperties();
        updateServer(server -> server.setDatasourceRoutingEnable(true));
        DataSourceContextSupport.clearDbType();
    }

    @AfterEach
    void tearDown() {
        DataSourceContextSupport.clearDbType();
    }

    @Test
    @DisplayName("默认来源=session 时，只带请求头不切库")
    void headerIgnoredByDefault() throws Exception {
        StubRegistry registry = new StubRegistry(properties, true);
        TenantDataSourceFilter filter = filter(registry, null);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v2/sys/user/page");
        request.addHeader(TenantDataSourceFilter.TENANT_ID_HEADER, HEADER_TENANT);

        filter.doFilter(request, new MockHttpServletResponse(), noopChain());

        assertFalse(registry.wasQueried, "请求头不应触发任何租户库解析");
        assertNull(DataSourceContextSupport.getDbType());
    }

    @Test
    @DisplayName("默认来源=session 时，会话里的租户生效")
    void sessionTenantSwitches() throws Exception {
        StubRegistry registry = new StubRegistry(properties, true);
        TenantDataSourceFilter filter = filter(registry, null);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v2/sys/user/page");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("tenantId", SESSION_TENANT);
        request.setSession(session);

        filter.doFilter(request, new MockHttpServletResponse(), noopChain());

        assertTrue(registry.wasQueried);
        assertEquals(SESSION_TENANT, registry.lastTenantId);
    }

    @Test
    @DisplayName("切库后请求结束必须清空线程变量")
    void clearsContextAfterRequest() throws Exception {
        StubRegistry registry = new StubRegistry(properties, true);
        TenantDataSourceFilter filter = filter(registry, null);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v2/sys/user/page");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("tenantId", SESSION_TENANT);
        request.setSession(session);

        filter.doFilter(request, new MockHttpServletResponse(), chainThatAssertsSwitched());

        assertNull(DataSourceContextSupport.getDbType(), "请求结束后不能残留租户上下文");
    }

    @Test
    @DisplayName("过滤器必须排在 AuthFilter 之后，否则新会话第一个请求读不到租户")
    void runsAfterAuthFilter() {
        TenantDataSourceFilter filter = filter(new StubRegistry(properties, true), null);
        // AuthFilter 注册顺序为 LOWEST_PRECEDENCE - 10（见 AuthClientConfiguration）。
        // 数值必须更大才是「更晚执行」——反了就会在新会话首个请求上回退主库。
        assertTrue(filter.getOrder() > Ordered.LOWEST_PRECEDENCE - 10,
                "租户数据源过滤器必须晚于 AuthFilter 执行，当前顺序: " + filter.getOrder());
    }

    @Test
    @DisplayName("业务抛异常时同样要清空线程变量")
    void clearsContextOnException() throws Exception {
        StubRegistry registry = new StubRegistry(properties, true);
        TenantDataSourceFilter filter = filter(registry, null);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v2/sys/user/page");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("tenantId", SESSION_TENANT);
        request.setSession(session);

        FilterChain failing = (req, res) -> {
            throw new IllegalStateException("模拟业务异常");
        };

        try {
            filter.doFilter(request, new MockHttpServletResponse(), failing);
        } catch (IllegalStateException ignored) {
            // 异常要原样抛出，这里只是把请求跑完
        }

        assertNull(DataSourceContextSupport.getDbType());
    }

    @Test
    @DisplayName("租户管理接口自身不切库，否则会去租户库读 sys_tenant")
    void platformPathsNeverSwitch() throws Exception {
        StubRegistry registry = new StubRegistry(properties, true);
        TenantDataSourceFilter filter = filter(registry, null);

        for (String uri : new String[]{"/v2/tenant/list", "/v2/service/list", "/v2/tenant/page"}) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
            MockHttpSession session = new MockHttpSession();
            session.setAttribute("tenantId", SESSION_TENANT);
            request.setSession(session);

            filter.doFilter(request, new MockHttpServletResponse(), noopChain());

            assertFalse(registry.wasQueried, uri + " 不应触发租户库解析");
            assertNull(DataSourceContextSupport.getDbType());
            registry.reset();
        }
    }

    @Test
    @DisplayName("已登记租户库但拿不到池时，默认拒绝请求（失败关闭）而不是回退主库")
    void rejectsWhenRegisteredTenantDataSourceUnavailable() throws Exception {
        StubRegistry registry = new StubRegistry(properties, false);
        TenantDataSourceFilter filter = filter(registry, null);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v2/sys/user/page");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("tenantId", SESSION_TENANT);
        request.setSession(session);

        AtomicBoolean reached = new AtomicBoolean(false);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> reached.set(true));

        assertFalse(reached.get(), "租户库不可用时不能把请求放行到主库");
        assertEquals(503, response.getStatus());
        assertNull(DataSourceContextSupport.getDbType());
    }

    @Test
    @DisplayName("租户库未登记（仍用共享库）时走主库，不算故障")
    void unregisteredDatabaseFallsThroughToMaster() throws Exception {
        // 租户记录存在但没有 host/dbname：这是「存量共享库租户」的正常形态
        StubRegistry registry = new StubRegistry(properties, false);
        TenantDataSourceFilter filter = filter(registry, null, false);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v2/sys/user/page");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("tenantId", SESSION_TENANT);
        request.setSession(session);

        AtomicBoolean reached = new AtomicBoolean(false);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> reached.set(true));

        assertTrue(reached.get(), "未登记库地址不等于故障，必须放行");
        assertEquals(200, response.getStatus());
        assertNull(DataSourceContextSupport.getDbType());
    }

    @Test
    @DisplayName("引用台账却解析不出实例：按配置错误拒绝，绝不回退主库")
    void rejectsBrokenHubReference() throws Exception {
        StubRegistry registry = new StubRegistry(properties, false);
        TenantDataSourceFilter filter = filter(registry, null, tenant -> {
            tenant.setSysTenantDbname("tenant_b");
            // 引用台账：本用例的解析器没有台账实现，等价于「台账模块缺失/记录被删」
            tenant.setSysTenantDatasourceId(42L);
        });

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v2/sys/user/page");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("tenantId", SESSION_TENANT);
        request.setSession(session);

        AtomicBoolean reached = new AtomicBoolean(false);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> reached.set(true));

        assertFalse(reached.get(), "引用的实例不可用时不能读平台库");
        assertEquals(503, response.getStatus());
    }

    @Test
    @DisplayName("只填库名没填地址：按存量语义走主库（同时留告警），不锁死租户")
    void halfConfiguredTenantFallsThrough() throws Exception {
        StubRegistry registry = new StubRegistry(properties, false);
        TenantDataSourceFilter filter = filter(registry, null, tenant -> tenant.setSysTenantDbname("tenant_half"));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v2/sys/user/page");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("tenantId", SESSION_TENANT);
        request.setSession(session);

        AtomicBoolean reached = new AtomicBoolean(false);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> reached.set(true));

        assertTrue(reached.get(), "半配置不是引用型配置错误，保持原有放行语义");
        assertEquals(200, response.getStatus());
    }

    @Test
    @DisplayName("关掉 fail-closed 后恢复旧行为：回退主库并放行")
    void fallsBackWhenFailClosedDisabled() throws Exception {
        updateServer(server -> server.setDatasourceRoutingFailClosed(false));
        StubRegistry registry = new StubRegistry(properties, false);
        TenantDataSourceFilter filter = filter(registry, null);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v2/sys/user/page");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("tenantId", SESSION_TENANT);
        request.setSession(session);

        AtomicBoolean reached = new AtomicBoolean(false);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> reached.set(true));

        assertTrue(reached.get(), "显式关闭 fail-closed 时应放行");
        assertEquals(200, response.getStatus());
        assertNull(DataSourceContextSupport.getDbType());
    }

    @Test
    @DisplayName("配置成 header 来源时请求头生效——这是显式选择的行为")
    void headerWorksWhenExplicitlyConfigured() throws Exception {
        updateServer(server -> server.setDatasourceRoutingTenantSource("header"));
        StubRegistry registry = new StubRegistry(properties, true);
        TenantDataSourceFilter filter = filter(registry, null);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v2/sys/user/page");
        request.addHeader(TenantDataSourceFilter.TENANT_ID_HEADER, HEADER_TENANT);

        filter.doFilter(request, new MockHttpServletResponse(), noopChain());

        assertTrue(registry.wasQueried);
        assertEquals(HEADER_TENANT, registry.lastTenantId);
    }

    @Test
    @DisplayName("session-or-header 优先会话，会话无租户时才用请求头")
    void sessionOrHeaderPrefersSession() throws Exception {
        updateServer(server -> server.setDatasourceRoutingTenantSource("session-or-header"));
        StubRegistry registry = new StubRegistry(properties, true);
        TenantDataSourceFilter filter = filter(registry, null);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v2/sys/user/page");
        request.addHeader(TenantDataSourceFilter.TENANT_ID_HEADER, HEADER_TENANT);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("tenantId", SESSION_TENANT);
        request.setSession(session);

        filter.doFilter(request, new MockHttpServletResponse(), noopChain());

        assertEquals(SESSION_TENANT, registry.lastTenantId);
    }

    @Test
    @DisplayName("session-or-header 会话无租户时用请求头")
    void sessionOrHeaderFallsBackToHeader() throws Exception {
        updateServer(server -> server.setDatasourceRoutingTenantSource("session-or-header"));
        StubRegistry registry = new StubRegistry(properties, true);
        TenantDataSourceFilter filter = filter(registry, null);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v2/sys/user/page");
        request.addHeader(TenantDataSourceFilter.TENANT_ID_HEADER, HEADER_TENANT);

        filter.doFilter(request, new MockHttpServletResponse(), noopChain());

        assertEquals(HEADER_TENANT, registry.lastTenantId);
    }

    @Test
    @DisplayName("未登录且无请求头时走主库")
    void anonymousRequestUsesMaster() throws Exception {
        StubRegistry registry = new StubRegistry(properties, true);
        TenantDataSourceFilter filter = filter(registry, null);

        filter.doFilter(new MockHttpServletRequest("GET", "/v2/sys/user/page"),
                new MockHttpServletResponse(), noopChain());

        assertFalse(registry.wasQueried);
        assertNull(DataSourceContextSupport.getDbType());
    }

    @Test
    @DisplayName("认证服务抛异常时回退会话属性，不中断请求")
    void fallsBackToSessionWhenAuthServiceFails() throws Exception {
        AuthService broken = () -> {
            throw new IllegalStateException("认证上下文损坏");
        };
        StubRegistry registry = new StubRegistry(properties, true);
        TenantDataSourceFilter filter = filter(registry, broken);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v2/sys/user/page");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("tenantId", SESSION_TENANT);
        request.setSession(session);

        filter.doFilter(request, new MockHttpServletResponse(), noopChain());

        assertEquals(SESSION_TENANT, registry.lastTenantId);
    }

    @Test
    @DisplayName("认证服务优先于会话属性")
    void authServiceWinsOverSessionAttribute() throws Exception {
        AuthService auth = () -> {
            CurrentUser user = new CurrentUser();
            user.setTenantId("from-auth-service");
            return user;
        };
        StubRegistry registry = new StubRegistry(properties, true);
        TenantDataSourceFilter filter = filter(registry, auth);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v2/sys/user/page");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("tenantId", SESSION_TENANT);
        request.setSession(session);

        filter.doFilter(request, new MockHttpServletResponse(), noopChain());

        assertEquals("from-auth-service", registry.lastTenantId);
    }

    /**
     * 修改服务端配置。
     *
     * <p>{@link TenantProperties#getServer()} 返回的是副本（防外部改到内部状态），
     * 必须改完整体回写，否则修改会被丢弃。</p>
     *
     * @param mutator 修改动作
     */
    private void updateServer(java.util.function.Consumer<TenantProperties.ServerConfig> mutator) {
        TenantProperties.ServerConfig server = properties.getServer();
        mutator.accept(server);
        properties.setServer(server);
    }

    /**
     * 构造被测过滤器。
     *
     * @param registry    注册器桩
     * @param authService 认证服务，可为空
     * @return 过滤器
     */
    private TenantDataSourceFilter filter(StubRegistry registry, AuthService authService) {
        return filter(registry, authService, true);
    }

    /**
     * 构造被测过滤器。
     *
     * @param registry     注册器桩
     * @param authService  认证服务，可为空
     * @param withDatabase 租户是否登记了独立库
     * @return 过滤器
     */
    private TenantDataSourceFilter filter(StubRegistry registry, AuthService authService, boolean withDatabase) {
        return filter(registry, authService, withDatabase
                ? tenant -> {
                    tenant.setSysTenantHost("127.0.0.1");
                    tenant.setSysTenantDbname("tenant_db");
                }
                : tenant -> {
                });
    }

    /**
     * 构造被测过滤器，并按需改写桩租户。
     *
     * @param registry   注册器桩
     * @param authService 认证服务，可为空
     * @param customizer 租户字段改写
     * @return 过滤器
     */
    private TenantDataSourceFilter filter(StubRegistry registry, AuthService authService,
                                          java.util.function.Consumer<SysTenant> customizer) {
        SysTenantMapper mapper = tenantMapperStub(customizer);
        ObjectProvider<AuthService> provider = new FixedAuthServiceProvider(authService);
        // 判定「是否登记独立库」与建池同源：这里给一个没有台账实现的解析器，
        // 桩租户只走自填字段路径，行为与改动前一致
        TenantDataSourceResolver resolver = new TenantDataSourceResolver(properties,
                new org.springframework.beans.factory.support.StaticListableBeanFactory()
                        .getBeanProvider(TenantDatasourceLookup.class));
        return new TenantDataSourceFilter(properties, mapper, registry, resolver, provider);
    }

    /**
     * 什么都不做的链路。
     *
     * @return 过滤器链
     */
    private FilterChain noopChain() {
        return (req, res) -> {
        };
    }

    /**
     * 断言「进入业务前上下文已切换」的链路。
     *
     * @return 过滤器链
     */
    private FilterChain chainThatAssertsSwitched() {
        return (req, res) -> assertTrue(DataSourceContextSupport.getDbType() != null,
                "业务代码执行时数据源必须已切换");
    }

    /**
     * 注册器桩：可以指定注册成功或失败。
     */
    private static final class StubRegistry extends TenantDataSourceRegistry {

        /**
         * 是否已注册成功
         */
        private final boolean registered;

        /**
         * 是否被查询过
         */
        private boolean wasQueried;

        /**
         * 最后一次查询的租户标识
         */
        private String lastTenantId;

        private StubRegistry(TenantProperties properties, boolean registered) {
            super(properties, new TenantPasswordCipher(
                    new com.chua.starter.system.properties.SystemSecurityProperties()));
            this.registered = registered;
        }

        @Override
        public String findRegisteredName(String tenantId) {
            this.wasQueried = true;
            this.lastTenantId = tenantId;
            return registered ? dataSourceName(tenantId) : null;
        }

        @Override
        public javax.sql.DataSource getOrCreate(SysTenant tenant) {
            this.wasQueried = true;
            this.lastTenantId = tenant.getSysTenantId();
            // 模拟建池失败：真实实现在连不上租户库时也返回 null，调用方据此回退主库
            return null;
        }

        /**
         * 重置记录，供多轮断言复用。
         */
        private void reset() {
            wasQueried = false;
            lastTenantId = null;
        }
    }

    /**
     * 租户查询桩：用 JDK 动态代理，只实现 {@code selectById}，
     * 其余 {@code BaseMapper} 方法一律返回 {@code null}。
     * <p>不手写实现类：{@code BaseMapper} 的默认方法在各版本间会增删，
     * 手写实现类在升级 MyBatis-Plus 时会直接编译失败，而这个桩本身与业务无关。</p>
     *
     * @param customizer 按用例改写租户字段（是否带库地址、是否引用台账等）
     * @return 租户查询桩
     */
    private SysTenantMapper tenantMapperStub(java.util.function.Consumer<SysTenant> customizer) {
        return (SysTenantMapper) java.lang.reflect.Proxy.newProxyInstance(
                SysTenantMapper.class.getClassLoader(),
                new Class<?>[]{SysTenantMapper.class},
                (proxy, method, args) -> {
                    if ("selectById".equals(method.getName()) && args != null && args.length == 1) {
                        SysTenant tenant = new SysTenant();
                        tenant.setSysTenantId(String.valueOf(args[0]));
                        customizer.accept(tenant);
                        return tenant;
                    }
                    if ("toString".equals(method.getName())) {
                        return "StubTenantMapper";
                    }
                    return null;
                });
    }

    /**
     * 固定认证服务提供者。
     */
    private static final class FixedAuthServiceProvider implements ObjectProvider<AuthService> {

        /**
         * 固定返回的认证服务
         */
        private final AuthService authService;

        private FixedAuthServiceProvider(AuthService authService) {
            this.authService = authService;
        }

        @Override
        public AuthService getObject(Object... args) {
            return authService;
        }

        @Override
        public AuthService getIfAvailable() {
            return authService;
        }

        @Override
        public AuthService getIfUnique() {
            return authService;
        }

        @Override
        public AuthService getObject() {
            return authService;
        }

        @Override
        public java.util.Iterator<AuthService> iterator() {
            return authService == null
                    ? java.util.Collections.emptyIterator()
                    : java.util.Collections.singleton(authService).iterator();
        }
    }
}