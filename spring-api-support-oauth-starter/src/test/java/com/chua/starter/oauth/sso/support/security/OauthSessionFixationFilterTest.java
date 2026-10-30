package com.chua.starter.oauth.sso.support.security;

import com.chua.starter.oauth.sso.support.properties.OauthSecurityProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link OauthSessionFixationFilter} 回归测试
 *
 * <p>本类守护的是 P1「Fixation 防护空转」的修复。原实现把
 * {@code changeSessionId()} 放在 {@code chain.doFilter()} <b>之后</b>，
 * 而登录接口早已把 {@code Set-Cookie: JSESSIONID=...} 提交掉了 ——
 * 服务端换掉的 ID 客户端根本收不到，防护等于没做，看上去却像生效了。</p>
 *
 * <p>因此这里用「记录调用顺序」的 spy 直接断言换 ID 发生在业务处理<b>之前</b>，
 * 这正是修复的全部要害；其余用例守住开关、路径匹配与顺序契约不被改坏。</p>
 *
 * @author CH
 * @since 4.0.0.51
 */
@DisplayName("OauthSessionFixationFilter: 换 SessionId 必须早于业务处理")
class OauthSessionFixationFilterTest {

    /**
     * 事件顺序记录器
     */
    private static final List<String> EVENTS = new ArrayList<>();

    /**
     * 会把 {@code changeSessionId()} 记进 {@link #EVENTS} 的请求
     */
    private static final class SpyRequest extends MockHttpServletRequest {

        SpyRequest(String method, String logicalUri) {
            super();
            setContextPath("/oauth");
            setRequestURI("/oauth" + logicalUri);
            setServletPath(logicalUri);
            setMethod(method);
            // 预先建立会话：changeSessionId() 要求「请求已有会话」
            getSession(true);
        }

        @Override
        public String changeSessionId() {
            EVENTS.add("changeSessionId");
            return super.changeSessionId();
        }
    }

    /**
     * 记录事件顺序的过滤器链
     */
    private static final class RecordingChain implements FilterChain {

        @Override
        public void doFilter(ServletRequest request, ServletResponse response) {
            EVENTS.add("chain");
        }
    }

    /**
     * 默认属性（sessionFixation.enabled = true，loginPaths = [/login]）
     *
     * @return 属性
     */
    private static OauthSecurityProperties properties() {
        return new OauthSecurityProperties();
    }

    /**
     * 跑一次过滤器
     *
     * @param props 属性
     * @param req   请求
     */
    private static void run(OauthSecurityProperties props, MockHttpServletRequest req) {
        EVENTS.clear();
        try {
            new OauthSessionFixationFilter(props).doFilter(req, new MockHttpServletResponse(), new RecordingChain());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    @DisplayName("核心反例：换 SessionId 必须发生在 chain 之前，而不是之后")
    void changeSessionIdMustPrecedeChain() {
        run(properties(), new SpyRequest("POST", "/v1/login"));
        assertEquals(List.of("changeSessionId", "chain"), EVENTS,
                "换 SessionId 必须在业务处理之前；放在之后则响应已提交，防护空转");
    }

    @Test
    @DisplayName("无会话时不抛异常、请求照常通过")
    void sessionlessRequestStillPasses() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setContextPath("/oauth");
        req.setRequestURI("/oauth/v1/login");
        req.setServletPath("/v1/login");
        req.setMethod("POST");
        // 不建立会话：changeSessionId() 会抛 IllegalStateException

        EVENTS.clear();
        try {
            new OauthSessionFixationFilter(properties())
                    .doFilter(req, new MockHttpServletResponse(), new RecordingChain());
        } catch (Exception e) {
            throw new AssertionError("无会话时不得打断请求", e);
        }
        assertEquals(List.of("chain"), EVENTS);
    }

    @Test
    @DisplayName("非登录路径不换 SessionId")
    void nonLoginPathUntouched() {
        run(properties(), new SpyRequest("POST", "/v1/token/verify"));
        assertEquals(List.of("chain"), EVENTS);
    }

    @Test
    @DisplayName("GET 登录路径不算（只对 POST 生效）")
    void getLoginNotMatched() {
        run(properties(), new SpyRequest("GET", "/v1/login"));
        assertEquals(List.of("chain"), EVENTS);
    }

    @Test
    @DisplayName("enabled=false 时不换 SessionId（修复前该属性从未被读取）")
    void disabledPropertyIsHonoured() {
        OauthSecurityProperties props = properties();
        props.getSessionFixation().setEnabled(false);
        run(props, new SpyRequest("POST", "/v1/login"));
        assertEquals(List.of("chain"), EVENTS, "enabled=false 时不应换 ID，但请求仍须通过");
    }

    @Test
    @DisplayName("协议前缀的登录入口命中约定后缀")
    void protocolPrefixedLoginMatched() {
        // 各协议认证入口形如 /{协议}/login（AuthProtocolController 拼接协议前缀），
        // 因此以 /login 结尾的配置项按后缀匹配
        run(properties(), new SpyRequest("POST", "/wechat/login"));
        assertEquals(List.of("changeSessionId", "chain"), EVENTS, "/wechat/login 应被换 ID");
    }

    @Test
    @DisplayName("显式配置完整路径：精确匹配生效，登录方法仍只认 POST")
    void explicitPathIsMatchedExactly() {
        OauthSecurityProperties props = properties();
        props.getSessionFixation().setLoginPaths(List.of("/v1/user/login"));

        run(props, new SpyRequest("POST", "/v1/user/login"));
        assertEquals(List.of("changeSessionId", "chain"), EVENTS);

        run(props, new SpyRequest("GET", "/v1/user/login"));
        assertEquals(List.of("chain"), EVENTS, "方法不匹配时不得换 ID");
    }

    @Test
    @DisplayName("上下文路径被正确剥离：/oauth/v1/login 命中 /login 约定")
    void contextPathIsStripped() {
        run(properties(), new SpyRequest("POST", "/v1/login"));
        assertEquals(List.of("changeSessionId", "chain"), EVENTS,
                "getRequestURI 为 /oauth/v1/login，应剥掉 /oauth 后命中 /login");
    }

    @Test
    @DisplayName("过滤器顺序在管理台鉴权之后")
    void orderIsPreserved() {
        OauthSessionFixationFilter filter = new OauthSessionFixationFilter(properties());
        OauthAdminAuthFilter admin = new OauthAdminAuthFilter(properties());
        assertTrue(filter.getOrder() > admin.getOrder(),
                "会话轮换应在管理台鉴权之后执行");
        assertFalse(admin.getOrder() >= Integer.MAX_VALUE);
    }
}