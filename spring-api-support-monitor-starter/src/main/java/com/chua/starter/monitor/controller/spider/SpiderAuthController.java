package com.chua.starter.monitor.controller.spider;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.monitor.properties.SpiderProperties;
import com.chua.starter.monitor.support.SpiderConsoleSessionSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Spider 轻页面认证控制器。
 */
@RestController
@RequestMapping("/v1/spider/auth")
@Tag(name = "Spider 控制台认证")
@RequiredArgsConstructor
public class SpiderAuthController {

    private static final String EMBEDDED_ADMIN_USER_ID = "1";
    private static final String SESSION_USER_INFO = "userInfo";
    private static final String SESSION_USER_ID = "userId";
    private static final String DEFAULT_ANONYMOUS_USER = "anonymous";

    private final SpiderProperties spiderProperties;

    @GetMapping("/status")
    @Operation(summary = "获取 Spider 控制台登录状态")
    public ReturnResult<Map<String, Object>> status(HttpServletRequest request) {
        return ReturnResult.ok(buildStatusData(request));
    }

    @PostMapping("/login")
    @Operation(summary = "登录 Spider 控制台")
    public ReturnResult<Map<String, Object>> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        if (!isEmbeddedAuthEnabled()) {
            return ReturnResult.ok(buildStatusData(false, true, DEFAULT_ANONYMOUS_USER));
        }

        SpiderProperties.WebAuthConfig config = getWebAuthConfig();
        if (Objects.equals(config.getUsername(), request.getUsername())
                && Objects.equals(config.getPassword(), request.getPassword())) {
            HttpSession session = httpRequest.getSession(true);
            session.setMaxInactiveInterval(resolveSessionTimeout(config));
            SpiderConsoleSessionSupport.markAuthenticated(session, request.getUsername());
            session.setAttribute(SESSION_USER_INFO, createSessionUserInfo(session.getId(), request.getUsername()));
            session.setAttribute(SESSION_USER_ID, EMBEDDED_ADMIN_USER_ID);

            Map<String, Object> data = buildStatusData(true, true, request.getUsername());
            data.put("token", session.getId());
            return ReturnResult.ok(data);
        }

        return ReturnResult.error("用户名或密码错误");
    }

    @PostMapping("/logout")
    @Operation(summary = "退出 Spider 控制台")
    public ReturnResult<Void> logout(HttpServletRequest request) {
        if (!isEmbeddedAuthEnabled()) {
            return ReturnResult.ok();
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            SpiderConsoleSessionSupport.clear(session);
            session.invalidate();
        }
        return ReturnResult.ok();
    }

    @GetMapping("/info")
    @Operation(summary = "获取 Spider 控制台当前用户")
    public ReturnResult<Map<String, Object>> info(HttpServletRequest request) {
        if (!isEmbeddedAuthEnabled()) {
            return ReturnResult.ok(buildStatusData(false, true, DEFAULT_ANONYMOUS_USER));
        }
        HttpSession session = request.getSession(false);
        if (SpiderConsoleSessionSupport.isAuthenticated(session)) {
            return ReturnResult.ok(buildStatusData(true, true, resolveSessionUsername(session)));
        }
        return ReturnResult.error("未登录");
    }

    private Map<String, Object> buildStatusData(HttpServletRequest request) {
        if (!isEmbeddedAuthEnabled()) {
            return buildStatusData(false, true, DEFAULT_ANONYMOUS_USER);
        }

        HttpSession session = request.getSession(false);
        boolean authenticated = SpiderConsoleSessionSupport.isAuthenticated(session);
        return buildStatusData(true, authenticated, authenticated ? resolveSessionUsername(session) : null);
    }

    private Map<String, Object> buildStatusData(boolean authEnabled, boolean authenticated, String username) {
        Map<String, Object> data = new HashMap<>();
        data.put("authEnabled", authEnabled);
        data.put("authenticated", authenticated);
        if (username != null && !username.isEmpty()) {
            data.put("username", username);
        }
        return data;
    }

    private boolean isEmbeddedAuthEnabled() {
        return !"none".equalsIgnoreCase(getWebAuthConfig().getMode());
    }

    private SpiderProperties.WebAuthConfig getWebAuthConfig() {
        return spiderProperties.getWebAuth() == null
                ? new SpiderProperties.WebAuthConfig()
                : spiderProperties.getWebAuth();
    }

    private int resolveSessionTimeout(SpiderProperties.WebAuthConfig config) {
        Integer timeout = config.getSessionTimeout();
        return timeout == null || timeout <= 0 ? 3600 : timeout;
    }

    private String resolveSessionUsername(HttpSession session) {
        String username = SpiderConsoleSessionSupport.username(session);
        if (username != null) {
            return username;
        }
        return getWebAuthConfig().getUsername();
    }

    private Object createSessionUserInfo(String sessionId, String username) {
        Map<String, Object> ext = new HashMap<>();
        ext.put("sysUserId", EMBEDDED_ADMIN_USER_ID);
        ext.put("userRealName", username);

        try {
            Class<?> userResultClass = Class.forName("com.chua.starter.oauth.client.support.user.UserResult");
            Object userResult = userResultClass.getDeclaredConstructor().newInstance();
            invokeSetter(userResultClass, userResult, "setUserId", String.class, EMBEDDED_ADMIN_USER_ID);
            invokeSetter(userResultClass, userResult, "setUsername", String.class, username);
            invokeSetter(userResultClass, userResult, "setNickName", String.class, username);
            invokeSetter(userResultClass, userResult, "setLoginType", String.class, "EMBEDDED");
            invokeSetter(userResultClass, userResult, "setToken", String.class, sessionId);
            invokeSetter(userResultClass, userResult, "setExt", Map.class, ext);
            return userResult;
        } catch (Exception e) {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", EMBEDDED_ADMIN_USER_ID);
            userInfo.put("username", username);
            userInfo.put("nickName", username);
            userInfo.put("loginType", "EMBEDDED");
            userInfo.put("token", sessionId);
            userInfo.put("ext", ext);
            return userInfo;
        }
    }

    private void invokeSetter(Class<?> type, Object target, String methodName, Class<?> parameterType, Object value)
            throws Exception {
        Method method = type.getMethod(methodName, parameterType);
        method.invoke(target, value);
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
        private Boolean rememberMe;
    }
}
