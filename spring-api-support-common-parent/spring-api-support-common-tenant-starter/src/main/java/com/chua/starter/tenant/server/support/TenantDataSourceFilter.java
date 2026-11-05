// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.tenant.server.support;

import com.chua.starter.common.support.oauth.AuthService;
import com.chua.starter.common.support.oauth.CurrentUser;
import com.chua.starter.datasource.support.DataSourceContextSupport;
import com.chua.starter.tenant.entity.SysTenant;
import com.chua.starter.tenant.properties.TenantProperties;
import com.chua.starter.tenant.server.mapper.SysTenantMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 按租户切换数据源的请求过滤器。
 *
 * <p>命中租户时把当前线程的数据源上下文切到 {@code tenant-<id>}；请求结束（含异常）一定清空，
 * 避免线程复用串库。</p>
 *
 * <h2>四个必须守住的边界</h2>
 *
 * <ol>
 *   <li><b>租户标识来自哪里，决定隔离是否成立</b>。见
 *       {@code datasource-routing-tenant-source}：默认只认登录会话里的 {@code tenantId}，
 *       <b>完全忽略 {@code X-Tenant-Id} 请求头</b>——请求头由客户端控制，改一个头就能读到
 *       别的租户库，等于没做隔离。只有显式配成 {@code header} 时才用请求头，
 *       那种模式下必须由接口层自己做租户归属校验。</li>
 *   <li><b>租户管理接口自身不切库</b>（{@code /v2/tenant}、{@code /v2/service}）——
 *       这些接口维护的就是平台主库里的 {@code sys_tenant}/{@code sys_service}，
 *       切到租户库会读到空表甚至写坏数据。</li>
 *   <li><b>没有租户身份就走主库</b>——登录、健康检查、未登录页面都属于这一类，
 *       它们本来就该读平台库。</li>
 *   <li><b>有租户身份但拿不到租户库时，默认拒绝而不是回退</b>——见下面的
 *       {@link #resolveOutcome} 与 {@code datasource-routing-fail-closed}。</li>
 * </ol>
 *
 * <h2>为什么「有租户身份却读主库」是事故而不是降级</h2>
 *
 * <p>{@code DynamicDataSource} 在上下文为空时静默返回默认数据源。若租户库抖动的那几秒
 * 里把请求放行到主库，租户用户的查询会读到<b>平台共享库</b>的数据，写操作直接把租户数据
 * 写进主库，而且全程没有任何报错——这是数据越权 + 脏数据，比 503 严重得多。
 * 因此这里区分三种情形：</p>
 *
 * <ul>
 *   <li><b>未登记租户库</b>（{@code sys_tenant_host/dbname} 为空）——这是「有意继续用共享库」
 *       的存量租户，走主库，不算故障。</li>
 *   <li><b>已登记但拿不到池</b>（连不上 / 口令错 / 建池超时）——这是故障，按
 *       {@code fail-closed} 拒绝请求。</li>
 *   <li><b>租户记录不存在</b>（会话里的租户已被删除）——身份已失效，同样拒绝，
 *       不能拿一个不存在的租户去读平台数据。</li>
 * </ul>
 *
 * <p><b>顺序</b>：默认 {@link Ordered#LOWEST_PRECEDENCE} - 20，即排在 {@code AuthFilter}
 * （{@code LOWEST_PRECEDENCE - 10}）之后。这一点在 4.0.0.52 修正过：早先放在
 * {@code HIGHEST_PRECEDENCE + 25}，比 AuthFilter 早得多，导致<b>新会话的第一个请求</b>
 * 永远读不到租户（AuthFilter 还没来得及把 {@code userInfo} 写进会话），表现为
 * 「有时切库、有时不切」。</p>
 *
 * <p>换到 AuthFilter 之后仍要早于任何业务逻辑打开连接（{@code OpenSessionInViewFilter}、
 * 事务切面、Mapper 调用），否则连接已按主库建立，后面的 {@code setDbType} 只对下一条
 * 新连接生效。</p>
 *
 * @since 4.0.0.51
 * @author CH
 */
@Slf4j
@RequiredArgsConstructor
public class TenantDataSourceFilter extends OncePerRequestFilter implements Ordered {

    /**
     * 租户标识请求头，与 {@code RequestUtils.getCurrentTenantId()} 保持一致
     */
    public static final String TENANT_ID_HEADER = "X-Tenant-Id";

    /**
     * 会话里存租户标识的键，与 {@code AuthSessionUtils.SESSION_TENANT_ID} 保持一致。
     * <p>不直接引 {@code oauth-client-starter} 的常量：本模块不该为了一个字符串
     * 硬依赖认证客户端实现。</p>
     */
    private static final String SESSION_TENANT_ID = "tenantId";

    /**
     * 租户标识来源：只认会话
     */
    private static final String SOURCE_SESSION = "session";

    /**
     * 租户标识来源：只认请求头
     */
    private static final String SOURCE_HEADER = "header";

    /**
     * 租户标识来源：会话优先，会话无租户时用请求头
     */
    private static final String SOURCE_SESSION_OR_HEADER = "session-or-header";

    /**
     * 不参与租户切库的路径前缀（平台库自身的管理接口）
     */
    private static final String[] PLATFORM_PATH_PREFIXES = {
            "/v2/tenant",
            "/v2/service"
    };

    /**
     * 租户配置
     */
    private final TenantProperties tenantProperties;

    /**
     * 租户查询
     */
    private final SysTenantMapper sysTenantMapper;

    /**
     * 租户数据源注册器
     */
    private final TenantDataSourceRegistry tenantDataSourceRegistry;

    /**
     * 认证服务；容器里没有实现时为空，此时只能依赖请求头来源
     */
    private final ObjectProvider<AuthService> authServiceProvider;

    @Override
    public int getOrder() {
        // 必须晚于 AuthFilter（LOWEST_PRECEDENCE - 10）：租户标识由认证结果写入会话，
        // 早于它执行时新会话的第一个请求读不到租户。
        // 注意数值方向：Ordered 值越小越先执行，故这里是 -5 而不是 -20。
        return Ordered.LOWEST_PRECEDENCE - 5;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        boolean switched = false;
        try {
            SwitchOutcome outcome = switchIfNecessary(request);
            if (outcome.rejectStatus() > 0) {
                writeRejected(response, outcome.rejectStatus(), outcome.rejectMessage());
                return;
            }
            switched = outcome.switched();
            filterChain.doFilter(request, response);
        } finally {
            if (switched) {
                DataSourceContextSupport.clearDbType();
            }
        }
    }

    /**
     * 按需把数据源上下文切到租户库。
     *
     * @param request 当前请求
     * @return 处理结果；拒绝时带状态码与提示
     */
    private SwitchOutcome switchIfNecessary(HttpServletRequest request) {
        if (isPlatformPath(request.getRequestURI())) {
            return SwitchOutcome.passThrough();
        }
        String tenantId = resolveTenantId(request);
        if (tenantId == null || tenantId.isBlank()) {
            return SwitchOutcome.passThrough();
        }

        String registeredName = tenantDataSourceRegistry.findRegisteredName(tenantId);
        if (registeredName == null) {
            // 平台库查租户登记信息：这一步必须走主库，此时上下文还没切，天然满足
            SysTenant tenant = sysTenantMapper.selectById(tenantId);
            if (tenant == null) {
                return rejectOrPass(request, tenantId, "租户不存在或已注销",
                        "租户 {} 在平台库不存在（会话里的租户可能已被删除）");
            }
            if (!hasDatasourceConfig(tenant)) {
                // 未登记库地址：这是「仍在共享库」的存量租户，属于正常配置而非故障
                log.debug("[租户数据源] 租户 {} 未登记独立库，走主库", tenantId);
                return SwitchOutcome.passThrough();
            }
            if (tenantDataSourceRegistry.getOrCreate(tenant) == null) {
                return rejectOrPass(request, tenantId, "租户数据库暂时不可用，请稍后重试",
                        "租户 {} 的数据库不可用（建池或连接失败）");
            }
            registeredName = tenantDataSourceRegistry.dataSourceName(tenantId);
        }

        DataSourceContextSupport.setDbType(registeredName);
        if (log.isDebugEnabled()) {
            log.debug("[租户数据源] 请求 {} 切换数据源: {}", request.getRequestURI(), registeredName);
        }
        return SwitchOutcome.tenantSwitched();
    }

    /**
     * 有租户身份但无法切到租户库时的处置。
     *
     * <p>默认失败关闭：拒绝请求，而不是静默回退主库——回退会让租户用户读到平台共享库的数据。
     * 只有显式关掉 {@code datasource-routing-fail-closed} 才回退，并留下 WARN 便于排查。</p>
     *
     * @param request   当前请求
     * @param tenantId  租户标识
     * @param message   拒绝时返回给调用方的提示
     * @param warnFormat 回退时的告警模板
     * @return 处理结果
     */
    private SwitchOutcome rejectOrPass(HttpServletRequest request, String tenantId,
                                       String message, String warnFormat) {
        if (!tenantProperties.getServer().isDatasourceRoutingFailClosed()) {
            log.warn("[租户数据源] " + warnFormat + "，按配置回退主库（fail-closed 已关闭）: {}",
                    tenantId, request.getRequestURI());
            return SwitchOutcome.passThrough();
        }
        log.warn("[租户数据源] " + warnFormat + "，已按 fail-closed 拒绝请求: {} {}",
                tenantId, request.getMethod(), request.getRequestURI());
        return SwitchOutcome.rejected(503, message);
    }

    /**
     * 租户是否登记了独立库。
     *
     * <p>与 {@link TenantJdbcUrlBuilder#build} 的判定口径保持一致：地址（host 或 ip）
     * 与库名都在才算「已登记」。两者都缺视为该租户仍使用共享库，不是故障。</p>
     *
     * @param tenant 租户记录
     * @return 登记了库地址返回 true
     */
    private boolean hasDatasourceConfig(SysTenant tenant) {
        String host = hasText(tenant.getSysTenantHost()) ? tenant.getSysTenantHost() : tenant.getSysTenantIp();
        return hasText(host) && hasText(tenant.getSysTenantDbname());
    }

    /**
     * 写出拒绝响应。
     *
     * @param response 响应
     * @param status   状态码
     * @param message  提示
     * @throws IOException 写出失败
     */
    private void writeRejected(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        String body = "{\"code\":" + status + ",\"ok\":false,\"msg\":\"" + escape(message) + "\"}";
        response.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
        response.getOutputStream().flush();
    }

    /**
     * 转义 JSON 字符串里的双引号，避免提示文案破坏响应体。
     *
     * @param message 原始提示
     * @return 转义后的提示
     */
    private String escape(String message) {
        return message == null ? "" : message.replace("\"", "'");
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

    /**
     * 一次请求的路由结果。
     *
     * @param switched     是否切换了数据源（切换了才需要在 finally 清空）
     * @param rejectStatus 拒绝时的 HTTP 状态码；0 表示不拒绝
     * @param rejectMessage 拒绝提示
     */
    private record SwitchOutcome(boolean switched, int rejectStatus, String rejectMessage) {

        /**
         * 放行到主库。
         *
         * @return 结果
         */
        private static SwitchOutcome passThrough() {
            return new SwitchOutcome(false, 0, null);
        }

        /**
         * 切到租户库。
         *
         * @return 结果
         */
        private static SwitchOutcome tenantSwitched() {
            return new SwitchOutcome(true, 0, null);
        }

        /**
         * 拒绝请求。
         *
         * @param status  状态码
         * @param message 提示
         * @return 结果
         */
        private static SwitchOutcome rejected(int status, String message) {
            return new SwitchOutcome(false, status, message);
        }
    }

    /**
     * 按配置的可信来源解析租户标识。
     *
     * @param request 当前请求
     * @return 租户标识；未配置或取不到时返回 {@code null}
     */
    private String resolveTenantId(HttpServletRequest request) {
        String source = tenantProperties.getServer().getDatasourceRoutingTenantSource();
        if (source == null) {
            source = SOURCE_SESSION;
        }

        if (SOURCE_HEADER.equalsIgnoreCase(source)) {
            return request.getHeader(TENANT_ID_HEADER);
        }
        if (SOURCE_SESSION_OR_HEADER.equalsIgnoreCase(source)) {
            String fromSession = resolveFromSession(request);
            return fromSession != null && !fromSession.isBlank()
                    ? fromSession
                    : request.getHeader(TENANT_ID_HEADER);
        }
        // 默认且最严格：只认会话
        return resolveFromSession(request);
    }

    /**
     * 从登录会话/认证上下文取租户标识。
     *
     * <p>两级取值：认证服务（OAuth 客户端会从会话里的 {@code userInfo} 解析）优先，
     * 其次直接读会话属性——前者拿不到时后者仍可用，避免因认证实现替换而失效。</p>
     *
     * @param request 当前请求
     * @return 租户标识；未登录或该用户无所属租户时返回 {@code null}
     */
    private String resolveFromSession(HttpServletRequest request) {
        AuthService authService = authServiceProvider.getIfAvailable();
        if (authService != null) {
            try {
                CurrentUser user = authService.getCurrentUser();
                if (user != null && user.getTenantId() != null && !user.getTenantId().isBlank()) {
                    return user.getTenantId();
                }
            } catch (Exception e) {
                // 认证上下文损坏不能连累请求：降级到会话属性，再不行就走主库
                log.debug("[租户数据源] 从认证上下文取租户失败，回落会话属性: {}", e.getMessage());
            }
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(SESSION_TENANT_ID);
        return value == null ? null : value.toString();
    }

    /**
     * 是否平台主库自身的管理路径。
     *
     * @param uri 请求路径
     * @return 是平台路径返回 true
     */
    private boolean isPlatformPath(String uri) {
        if (uri == null) {
            return false;
        }
        for (String prefix : PLATFORM_PATH_PREFIXES) {
            if (uri.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}