package com.chua.starter.oauth.sso.support.protocol;

import com.chua.starter.oauth.sso.support.properties.ThirdPartyLoginProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;

/**
 * 第三方登录「系统回调地址」校验策略
 *
 * <p><b>修复一个开放重定向（Open Redirect）缺陷</b>。</p>
 *
 * <p>原实现：{@code /oauth2/authorize/{platform}?callback=https://evil.tld} 的参数
 * 原样进 state 缓存，{@code /oauth2/callback/{platform}} 取出后直接
 * {@code RedirectView#setUrl(...)} 重定向；成功后还把 Cookie 的 Domain 设成
 * {@code NetAddress.of(callback).getHost()}。三重问题：</p>
 * <ol>
 *   <li><b>开放重定向</b>：攻击者用自有合法 OAuth 应用走一遍流程，即可让
 *       {@code /oauth2/callback/gitee?code=..&state=..} 把受害者弹到任意外部站点。
 *       由于此时认证 Cookie 已经下发完毕，配合钓鱼页即可完成账号接管；</li>
 *   <li><b>Cookie 域注入</b>：请求方通过 {@code callback} 决定令牌写到哪个域，
 *       等同于把令牌交到攻击者手里；</li>
 *   <li><b>协议相对 URL 绕过</b>：只判断「是否以 / 开头」会放过
 *       {@code //evil.tld}、{@code /\evil.tld}、{@code \\evil.tld}。</li>
 * </ol>
 *
 * <p>本策略的判定顺序（任一步不通过即拒绝，绝不回退到「放行」）：</p>
 * <ol>
 *   <li>空值 → 视为 {@code "/"}（站内根路径，安全默认值）；</li>
 *   <li>含 CR/LF/空白 → 拒绝（防响应头注入与绕过）；</li>
 *   <li>以 {@code /} 开头：必须<b>恰好一个</b>斜杠且第二个字符不是 {@code /}、{@code \}，
 *       否则视为协议相对 URL，拒绝；</li>
 *   <li>非相对路径：必须是合法的绝对 URI，且 {@code scheme://host[:port]} 前缀
 *       命中 {@code allowedSystemCallbackHosts} 白名单；</li>
 *   <li>白名单为空 → 绝对地址一律拒绝（fail-closed）。</li>
 * </ol>
 *
 * @since 4.0.0.51
 * @author CH
 */
@RequiredArgsConstructor
public class SystemCallbackPolicy {

    /**
     * 第三方登录配置
     */
    private final ThirdPartyLoginProperties properties;

    /**
     * 安全默认值：站内根路径
     */
    private static final String DEFAULT_CALLBACK = "/";

    /**
     * 校验并归一化系统回调地址
     *
     * @param rawCallback 原始回调地址（可能来自请求参数或 state 缓存）
     * @return 通过校验的回调地址；不合法时返回 {@link #DEFAULT_CALLBACK}
     */
    public String sanitize(String rawCallback) {
        if (!StringUtils.hasText(rawCallback)) {
            return DEFAULT_CALLBACK;
        }
        String callback = rawCallback.trim();
        if (callback.isEmpty()) {
            return DEFAULT_CALLBACK;
        }
        // 响应头/Location 注入与解析歧义：任何控制字符或内嵌空白一律拒绝
        for (int i = 0; i < callback.length(); i++) {
            char c = callback.charAt(i);
            if (c <= 0x20 || c == 0x7F) {
                return DEFAULT_CALLBACK;
            }
        }
        if (callback.startsWith("/")) {
            return isSafeRelative(callback) ? callback : DEFAULT_CALLBACK;
        }
        return isAllowedAbsolute(callback) ? callback : DEFAULT_CALLBACK;
    }

    /**
     * 判断回调地址是否合法（不合法时给出明确原因，便于日志告警）
     *
     * @param rawCallback 原始回调地址
     * @return {@code null} 表示合法，否则返回拒绝原因
     */
    public String rejectReason(String rawCallback) {
        if (!StringUtils.hasText(rawCallback)) {
            return null;
        }
        String callback = rawCallback.trim();
        for (int i = 0; i < callback.length(); i++) {
            char c = callback.charAt(i);
            if (c <= 0x20 || c == 0x7F) {
                return "回调地址包含控制字符或空白";
            }
        }
        if (callback.startsWith("/")) {
            return isSafeRelative(callback) ? null : "回调地址为协议相对 URL，已拒绝（疑似开放重定向）";
        }
        if (getAllowedHosts().isEmpty()) {
            return "未配置 allowed-system-callback-hosts，绝对地址回调已拒绝（疑似开放重定向）";
        }
        return isAllowedAbsolute(callback) ? null : "回调地址不在 allowed-system-callback-hosts 白名单内";
    }

    /**
     * 相对路径安全性判定
     *
     * @param callback 以 / 开头的地址
     * @return 安全返回 true
     */
    private boolean isSafeRelative(String callback) {
        // 必须恰好一个前导斜杠：//evil.tld 与 /\evil.tld 都会被浏览器当作跨站地址
        if (callback.length() > 1) {
            char second = callback.charAt(1);
            if (second == '/' || second == '\\') {
                return false;
            }
        }
        // 以 / 开头的相对路径不会被当作协议或 authority，反斜杠开头的伪协议也要挡住
        return !callback.startsWith("\\");
    }

    /**
     * 绝对地址白名单判定（按 scheme://host[:port] 前缀比较）
     *
     * @param callback 绝对地址
     * @return 命中白名单返回 true
     */
    private boolean isAllowedAbsolute(String callback) {
        URI uri;
        try {
            uri = new URI(callback);
        } catch (URISyntaxException e) {
            return false;
        }
        if (!uri.isAbsolute() || !StringUtils.hasText(uri.getHost())) {
            return false;
        }
        // 只接受 http/https：javascript:、data:、file: 等一律不放行
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        if (!"http".equals(scheme) && !"https".equals(scheme)) {
            return false;
        }
        String prefix = buildPrefix(scheme, uri.getHost(), uri.getPort());
        for (String allowed : getAllowedHosts()) {
            if (prefix.equals(normalizeOrigin(allowed))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 构造 scheme://host[:port] 前缀
     *
     * @param scheme 协议
     * @param host   主机
     * @param port   端口，-1 表示未显式指定
     * @return 前缀
     */
    private String buildPrefix(String scheme, String host, int port) {
        StringBuilder sb = new StringBuilder(scheme).append("://")
                .append(host == null ? "" : host.toLowerCase(Locale.ROOT));
        if (port > 0) {
            sb.append(':').append(port);
        }
        return sb.toString();
    }

    /**
     * 归一化白名单项：取 scheme://host[:port]，忽略路径、查询、末尾斜杠与大小写
     *
     * @param entry 白名单项
     * @return 归一化前缀
     */
    private String normalizeOrigin(String entry) {
        String value = entry.trim();
        try {
            URI uri = new URI(value);
            if (StringUtils.hasText(uri.getScheme()) && StringUtils.hasText(uri.getHost())) {
                return buildPrefix(uri.getScheme().toLowerCase(Locale.ROOT), uri.getHost(), uri.getPort());
            }
        } catch (URISyntaxException ignored) {
            // 非法项按原样比较，匹配不上即拒绝
        }
        return value.toLowerCase(Locale.ROOT).replaceAll("/+$", "");
    }

    /**
     * 读取白名单
     *
     * @return 白名单列表（可能为空）
     */
    private List<String> getAllowedHosts() {
        List<String> hosts = properties.getAllowedSystemCallbackHosts();
        return hosts == null ? List.of() : hosts;
    }

    /**
     * 登录 Cookie 的域（显式配置，未配置返回 null 表示不设置 Domain）
     *
     * @return cookie 域，未配置返回 null
     */
    public String cookieDomain() {
        String domain = properties.getCookieDomain();
        return StringUtils.hasText(domain) ? domain.trim() : null;
    }
}