package com.chua.starter.oauth.sso.support.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * OAuth 服务端路径匹配工具
 *
 * <p><b>存在的理由（修复一个真实的安全缺陷）</b>：此前 {@code OauthCsrfFilter#isIgnored}
 * 与 {@code OauthRateLimitFilter#matchesAny} 都使用
 * {@code uri.equals(p) || uri.endsWith(p) || uri.contains(p)} 判断「是否命中豁免路径」。
 * 而本服务的 servlet 上下文路径就是 {@code /oauth}（{@code application.yml} 的
 * {@code server.servlet.context-path}），{@link jakarta.servlet.http.HttpServletRequest#getRequestURI()}
 * 一定以 {@code /oauth} 开头。于是默认豁免列表里的 {@code "/oauth"} 这一项会让
 * {@code contains("/oauth")} 对<b>每一个</b>请求都成立，CSRF 校验被整体短路、
 * 限流范围判定也完全失控——即配置写着 {@code enabled=true}，实际一行防护都没生效。</p>
 *
 * <p>本工具改为：先剥离上下文路径，再用 Spring 的 {@link AntPathMatcher} 做<b>锚定</b>匹配
 * （{@code /admin} 只匹配 {@code /admin}，不会匹配 {@code /admin/../x} 或 {@code /adminAnything}），
 * 与客户端侧 {@code WebRequest#isPass()} 的行为保持一致，便于运维心智统一。</p>
 *
 * @since 4.0.0.51
 * @author CH
 */
@Slf4j
public final class OauthUriMatcher {

    /**
     * 锚定式 Ant 匹配器
     */
    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    private OauthUriMatcher() {
    }

    /**
     * 剥离 servlet 上下文路径，得到与配置文件里书写的、可直接比较的逻辑路径
     *
     * <p>例如上下文路径 {@code /oauth}、请求 {@code /oauth/admin/status} → {@code /admin/status}；
     * {@code /oauth} → {@code /}。</p>
     *
     * @param request 请求
     * @return 逻辑路径（不含上下文路径）
     */
    public static String logicalPath(jakarta.servlet.http.HttpServletRequest request) {
        return stripContextPath(request.getRequestURI(), request.getContextPath());
    }

    /**
     * 剥离上下文路径
     *
     * @param uri         原始 URI（通常来自 {@code getRequestURI()}）
     * @param contextPath 上下文路径，可为空
     * @return 逻辑路径（不含上下文路径）
     */
    public static String stripContextPath(String uri, String contextPath) {
        if (!StringUtils.hasLength(uri)) {
            return "/";
        }
        String result = uri;
        if (StringUtils.hasLength(contextPath) && !"/".equals(contextPath)) {
            if (result.equals(contextPath)) {
                result = "/";
            } else if (result.startsWith(contextPath + "/")) {
                result = result.substring(contextPath.length());
            }
        }
        // 归一化：去掉多余前导斜杠，保证 Ant 模式（以 / 开头）能稳定匹配
        if (!result.startsWith("/")) {
            result = "/" + result;
        }
        return result.length() > 1 && result.endsWith("/")
                ? result.substring(0, result.length() - 1)
                : result;
    }

    /**
     * 判断逻辑路径是否命中任一模式（锚定 Ant 匹配，非子串包含）
     *
     * @param logicalPath 逻辑路径（已剥离上下文路径）
     * @param patterns    模式列表，可为空
     * @return 命中返回 true
     */
    public static boolean matchesAny(String logicalPath, List<String> patterns) {
        if (patterns == null || patterns.isEmpty() || !StringUtils.hasLength(logicalPath)) {
            return false;
        }
        for (String pattern : patterns) {
            if (!StringUtils.hasText(pattern)) {
                continue;
            }
            String normalized = pattern.trim();
            if (MATCHER.match(normalized, logicalPath)) {
                return true;
            }
        }
        return false;
    }
}