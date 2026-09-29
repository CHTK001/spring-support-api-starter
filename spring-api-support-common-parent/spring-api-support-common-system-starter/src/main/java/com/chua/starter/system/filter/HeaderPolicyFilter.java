package com.chua.starter.system.filter;

import com.chua.common.support.utils.StringUtils;
import com.chua.starter.service.hotreload.HotReloadConfigReader;
import com.chua.starter.system.properties.HeaderPolicyProperties;
import com.chua.starter.system.support.ClientIpResolver;
import com.chua.starter.system.support.HeaderRule;
import com.chua.starter.system.support.PolicyCacheService;
import com.chua.starter.system.support.PolicyHitCollector;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

/**
 * 消息头拦截过滤器（主要是 {@code User-Agent}，用于拦爬虫与自动化客户端）。
 *
 * <p>排在鉴权之前（{@code HIGHEST_PRECEDENCE + 30}，紧随接口停用/转发之后）：
 * 爬虫和自动化脚本消耗的是「先鉴权再拒绝」的那一份开销，而它们大多没有有效凭证，
 * 放在鉴权后拦等于白跑一遍 token 校验。</p>
 *
 * <p><b>整段兜住异常，任何未预期错误都放行</b>。这是策略层：它自己的 bug
 * 不该变成全站 500。黑名单「漏放几个爬虫」的代价，远小于「策略 bug 打挂整站」。</p>
 *
 * <p>规则来自统一策略表 {@code sys_policy_config}（{@code type=HEADER}），
 * 保存/启停/删除后由 {@link com.chua.starter.system.support.HeaderPolicyCacheProjector} 投影进缓存，
 * <b>即时生效</b>，不需要重启。</p>
 *
 * @author CH
 * @since 4.0.0.43
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 30)
public class HeaderPolicyFilter implements Filter, Ordered {

    /**
     * 策略运行时缓存
     */
    private final PolicyCacheService policyCacheService;

    /**
     * 消息头拦截配置（第一方豁免硬闸与判定字段）
     */
    private final HeaderPolicyProperties properties;

    /**
     * 热重载配置读取器（策略管理页面控制的第一方豁免开关）
     */
    private final HotReloadConfigReader hotReloadConfigReader;

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 30;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        try {
            // 没有配置任何消息头规则时直接放行。
            // 顺序很重要：先判这个再判第一方，策略关闭时连一次标记比对都不做 ——
            // 既省掉每个请求两次 header 读取，也避免「策略关着却还在按标记放行」。
            Set<String> names = policyCacheService.headerNameIndex();
            if (names.isEmpty()) {
                chain.doFilter(request, response);
                return;
            }
            // 第一方豁免：自家前端与自家脚本不该被「拦爬虫」的规则误伤
            if (isFirstParty(request)) {
                chain.doFilter(request, response);
                return;
            }
            for (String name : names) {
                String ruleJson = policyCacheService.getHeaderRule(name);
                HeaderRule rule = HeaderRule.parse(name, ruleJson);
                if (null == rule) {
                    // 规则坏掉就当这条没配，绝不因为一条坏规则拦住全站
                    log.warn("[HeaderPolicy] 规则解析失败，已跳过: header={}", name);
                    continue;
                }
                String headerValue = request.getHeader(name);
                String matched = rule.firstMatch(headerValue);
                if (null != matched) {
                    log.warn("[HeaderPolicy] 命中拦截: header={}, pattern={}, uri={}",
                            name, matched, request.getRequestURI());
                    PolicyHitCollector.getInstance().record(PolicyHitCollector.TYPE_HEADER,
                            name + "|" + matched, ClientIpResolver.resolve(request));
                    writeBlocked(response, rule);
                    return;
                }
            }
        } catch (Exception e) {
            // 策略层异常一律放行
            log.warn("[HeaderPolicy] 检查异常，放行: {}", e.toString());
        }
        chain.doFilter(request, response);
    }

    /**
     * 是否第一方客户端
     *
     * <p><b>双字段判断</b>：{@code X-Trace-Id} 与 {@code X-Requested-With} 两个头
     * 的取值都要以配置的标记结尾才算第一方，单个满足不豁免。两个头都不是自创的
     * 私有头：前者本系统链路追踪本来就在用，后者是 AJAX 事实标准，藏标记不显眼。</p>
     *
     * <p>为什么不能用 {@code User-Agent} 承载标记：浏览器不允许 JS 设置它
     * （Fetch 规范的 forbidden header name，axios/XHR 写入被静默丢弃），
     * 写了也发不出去。</p>
     *
     * <p><b>是否允许豁免由策略管理控制</b>：读热重载编码
     * {@code policy.header.first-party-enabled}，在「策略管理 → 热更新配置」里改
     * 即时生效。配置文件里的 {@code first-party-enabled} 是硬闸，两者取「与」：
     * 配置文件关了谁都开不了，页面上关掉则不必改配置、不必重启。</p>
     *
     * <p><b>它不是安全凭证</b>：两个头的取值都能被伪造，凑齐两个即可整体绕过。
     * 这里的双字段只是把「要绕过就得同时注意到两处」的成本抬高一点，
     * 不是密码学意义上的防护。真正要防的定向抓取得靠鉴权、签名或限流。
     * 不接受任何后门时，把上面两个开关都关掉即可。</p>
     *
     * <p>命中会记一条 debug 日志：豁免是安全边界上的例外，事后要能分辨
     * 「没被拦」到底是规则没配、走了豁免，还是压根没触发。</p>
     *
     * @param request 当前请求
     * @return true=第一方客户端，跳过消息头规则
     */
    private boolean isFirstParty(HttpServletRequest request) {
        // 硬闸（配置文件）先判：它要是否决任何运行期开关
        if (!properties.isFirstPartyEnabled()) {
            return false;
        }
        // 运行期操作杆（策略管理页）
        if (!hotReloadConfigReader.headerFirstPartyEnabled()) {
            return false;
        }
        String marker = properties.getFirstPartyMarker();
        if (StringUtils.isBlank(marker)) {
            return false;
        }
        if (!endsWithMarker(request.getHeader(properties.getFirstPartyHeaderA()), marker)
                || !endsWithMarker(request.getHeader(properties.getFirstPartyHeaderB()), marker)) {
            return false;
        }
        log.debug("[HeaderPolicy] 第一方豁免: uri={}", request.getRequestURI());
        return true;
    }

    /**
     * 取值是否以标记结尾（忽略大小写与首尾空白）
     *
     * @param value  头取值，可为 null
     * @param marker 标记
     * @return true=命中标记
     */
    private boolean endsWithMarker(String value, String marker) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        return value.trim().toLowerCase(Locale.ROOT).endsWith(marker.trim().toLowerCase(Locale.ROOT));
    }

    /**
     * 写拒绝响应。
     *
     * @param response 响应
     * @param rule     命中的规则
     * @throws IOException 写失败
     */
    private void writeBlocked(HttpServletResponse response, HeaderRule rule) throws IOException {
        String message = rule.message().isBlank() ? "请求被客户端标识策略拒绝" : rule.message();
        byte[] body = ("{\"code\":\"" + rule.status() + "\",\"msg\":\"" + message + "\",\"ok\":false}")
                .getBytes(StandardCharsets.UTF_8);
        response.setStatus(rule.status());
        response.setContentType("application/json;charset=UTF-8");
        response.setContentLength(body.length);
        response.getOutputStream().write(body);
        response.flushBuffer();
    }
}
