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
        * 免 CSRF 校验的路径（Ant 风格，按 结束with / equals 匹配）
         */
        private List<String> ignorePaths = new ArrayList<>(List.of(
                "/oauth",
                "/temporary-token",
                "/actuator"
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
        * 需要限流的路径，为空表示全部路径
         */
        private List<String> paths = new ArrayList<>(List.of(
                "/login",
                "/oauth",
                "/temporary-token",
                "/upgrade"
        ));

        /**
        * 免限流路径
         */
        private List<String> ignorePaths = new ArrayList<>();
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
