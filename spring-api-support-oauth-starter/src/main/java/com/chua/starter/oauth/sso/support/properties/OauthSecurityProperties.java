package com.chua.starter.oauth.sso.support.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * OAuth认证服务器安全配置属性
 *
 * <p>配置前缀：{@code plugin.oauth.server.security}</p>
 *
 * <h3>主要能力：</h3>
 * <ul>
 *   <li>安全响应头（HSTS、X-Frame-Options、CSP 等）</li>
 *   <li>CSRF 令牌校验</li>
 *   <li>IP 级请求限流</li>
 *   <li>Session Fixation 防护</li>
 *   <li>登录失败锁定参数（与 LoginAttemptService 联动）</li>
 * </ul>
 *
 * @since 2025/01/01
 * @author CH
 */
@Data
@ConfigurationProperties(prefix = OauthSecurityProperties.PRE)
public class OauthSecurityProperties {

    /**
     * 配置属性前缀
     */
    public static final String PRE = "plugin.oauth.server.security";

    /**
     * 是否启用安全能力总开关
     */
    private boolean enabled = true;

    /**
     * 安全响应头配置
     */
    private Headers headers = new Headers();

    /**
     * CSRF 防护配置
     */
    private Csrf csrf = new Csrf();

    /**
     * 管理控制台鉴权配置
     */
    private Admin admin = new Admin();

    /**
     * IP 级请求限流配置
     */
    private RateLimit rateLimit = new RateLimit();

    /**
     * 会话 Fixation 防护配置
     */
    private SessionFixation sessionFixation = new SessionFixation();

    /**
     * 登录失败锁定配置
     */
    private LoginLock loginLock = new LoginLock();

    /**
     * 安全响应头配置
     *
     * @since 2025/01/01
     * @author CH
     */
    @Data
    public static class Headers {

        /**
         * 是否启用安全响应头
         */
        private boolean enabled = true;

        /**
         * X-内容-类型-期权 值
         */
        private String contentTypeOptions = "nosniff";

        /**
         * X-帧-期权 值
         */
        private String frameOptions = "DENY";

        /**
         * X-XSS-Protection 值
         */
        private String xssProtection = "1; mode=block";

        /**
         * Referrer-Policy 值
         */
        private String referrerPolicy = "strict-origin-when-cross-origin";

        /**
         * 内容-安全性-Policy 值，为空则不设置
         */
        private String contentSecurityPolicy = "default-src 'self'";

        /**
         * Strict-Transport-安全性 值，为空则不设置
         */
        private String strictTransportSecurity = "max-age=31536000; includeSubDomains";

        /**
         * 权限-Policy 值，为空则不设置
         */
        private String permissionsPolicy = "geolocation=(), microphone=(), camera=()";

        /**
         * 缓存-Control 值，为空则不设置
         */
        private String cacheControl = "no-store, no-cache, must-revalidate, max-age=0";
    }

    /**
     * 管理控制台鉴权配置
     *
     * <p><b>存在的理由（修复一个严重缺陷）</b>：{@code AdminController} 暴露了
     * {@code /oauth/admin/**}（开关签名校验、改令牌有效期、切在线模式、改限流），
     * 但此前<b>没有任何认证与授权</b>——本服务作为认证服务端，通过标记类
     * {@code SsoServer} + {@code @ConditionalOnMissingClass} 主动关闭了自己的
     * {@code AuthFilter}，且未引入 Spring Security。结果是一次未授权的 POST 就能
     * 把 {@code signVerify} 置为 false，摘掉所有协议入口唯一的请求真实性控制。</p>
     * <p>本过滤器用独立的管理令牌保护该路径，且令牌为空时<b>默认拒绝一切</b>
     * （fail-closed），不会退化成「无令牌即放行」。</p>
     *
     * @since 4.0.0.51
     * @author CH
     */
    @Data
    public static class Admin {

        /**
         * 是否启用管理控制台鉴权
         *
         * <p>置 false 可整体关闭（仅限本地调试）。生产环境必须保持 true。</p>
         */
        private boolean enabled = true;

        /**
         * 受保护的管理路径（Ant 风格，相对上下文路径，锚定匹配）
         */
        private List<String> paths = new ArrayList<>(List.of(
                "/admin/**",
                "/oauth/admin/**"
        ));

        /**
         * 管理令牌请求头名称
         */
        private String headerName = "X-OAuth-Admin-Token";

        /**
         * 管理令牌
         *
         * <p><b>故意不提供默认值</b>：留空即视为「未配置」，过滤器将拒绝所有管理请求，
         * 逼迫部署方显式配置，避免出现「默认口令」这种一装即破的状态。</p>
         * <p>生产环境请用环境变量注入，例如：{@code plugin.oauth.security.admin.token=${OAUTH_ADMIN_TOKEN}}。</p>
         */
        private String token;

        /**
         * 额外的来源 IP 白名单（可选，CIDR）
         *
         * <p>非空时，来源 IP 必须命中才放行——用于把管理面收敛到内网/跳板机。</p>
         */
        private List<String> ipWhitelist = new ArrayList<>();
    }

    /**
     * CSRF 防护配置
     *
     * @since 2025/01/01
     * @author CH
     */
    @Data
    public static class Csrf {

        /**
         * 是否启用 CSRF 防护
         */
        private boolean enabled = true;

        /**
         * CSRF 令牌请求头名称
         */
        private String headerName = "X-CSRF-TOKEN";

        /**
         * CSRF 令牌 Cookie 名称
         */
        private String cookieName = "XSRF-TOKEN";

        /**
         * CSRF 令牌参数名称（表单提交）
         */
        private String parameterName = "_csrf";

        /**
         * CSRF 令牌有效期（秒）
         */
        private long tokenTtlSeconds = 3600L;

        /**
         * 需要 CSRF 校验的 HTTP 方法
         */
        private List<String> methods = new ArrayList<>(List.of("POST", "PUT", "PATCH", "DELETE"));

        /**
         * 免 CSRF 校验的路径（Ant 风格，<b>相对上下文路径</b>，锚定匹配）
         *
         * <p><b>修复</b>：原默认值含 {@code "/oauth"}，而 {@code getRequestURI()} 恒含上下文
         * 路径 {@code /oauth}，配合原先的 {@code contains} 判定会让全部请求被豁免。
         * 现在匹配器已改为剥离上下文路径后的锚定 Ant 匹配，因此这里只需书写<b>逻辑路径</b>。
         * 三方平台回调（{@code /oauth2/callback/**}、{@code /gitee/**}）由第三方以
         * 顶层跳转方式发起、无 Cookie 浏览器会话，不适用 double-submit，故豁免。</p>
         */
        private List<String> ignorePaths = new ArrayList<>(List.of(
                "/actuator/**",
                "/oauth2/callback/**",
                "/oauth2/**/callback",
                "/gitee/**",
                "/health"
        ));
    }

    /**
     * IP 级请求限流配置
     *
     * @since 2025/01/01
     * @author CH
     */
    @Data
    public static class RateLimit {

        /**
         * 是否启用 IP 限流
         */
        private boolean enabled = true;

        /**
         * 时间窗口内允许的最大请求数
         */
        private int maxRequests = 100;

        /**
         * 时间窗口（秒）
         */
        private int windowSeconds = 60;

        /**
         * 需要限流的路径（Ant 风格，<b>相对上下文路径</b>，锚定匹配），为空表示全部路径
         *
         * <p><b>修复</b>：原默认值含 {@code "/oauth"}（其实是上下文路径本身，不是逻辑路径），
         * 配合原先的 {@code contains} 判定会让「是否在限流范围内」退化成恒真/恒假的巧合。
         * 现改为逻辑路径 + 锚定匹配。</p>
         */
        private List<String> paths = new ArrayList<>(List.of(
                "/login",
                "/temporary-token",
                "/upgrade",
                "/verify",
                "/admin/**"
        ));

        /**
         * 免限流路径（Ant 风格，相对上下文路径，锚定匹配）
         */
        private List<String> ignorePaths = new ArrayList<>();

        /**
         * 可信反向代理 IP / CIDR 白名单
         *
         * <p><b>修复</b>：此前 {@code resolveClientIp} 无条件信任 {@code X-Forwarded-For}
         * 等 5 个请求头且不校验来源，攻击者轮换该头即可让每个请求落到不同的限流桶，
         * IP 限流形同虚设。现在只有当直接连接方命中本白名单时，才采信转发头。</p>
         * <p>直连部署请保持为空（默认），此时只用 {@code getRemoteAddr()}。</p>
         */
        private List<String> trustedProxies = new ArrayList<>();

        /**
         * 限流桶缓存的最大条目数
         *
         * <p>{@code interface} 维度以请求路径为 key，攻击者可用大量随机路径撑爆内存。</p>
         */
        private int maxRuleBuckets = 10_000;
    }

    /**
     * 会话 Fixation 防护配置
     *
     * @since 2025/01/01
     * @author CH
     */
    @Data
    public static class SessionFixation {

        /**
         * 是否启用 会话 Fixation 防护
         */
        private boolean enabled = true;

        /**
         * 登录成功后需要刷新 会话 的路径
         */
        private List<String> loginPaths = new ArrayList<>(List.of("/login"));
    }

    /**
     * 登录失败锁定配置
     *
     * @since 2025/01/01
     * @author CH
     */
    @Data
    public static class LoginLock {

        /**
         * 最大失败次数
         */
        private int maxAttempts = 5;

        /**
         * 锁定时长（分钟）
         */
        private int lockDurationMinutes = 15;

        /**
         * 失败记录过期时间（分钟）
         */
        private int attemptExpireMinutes = 30;
    }
}
