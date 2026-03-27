package com.chua.starter.payment.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.payment.properties.PaymentConsoleProperties;
import com.chua.starter.payment.support.PaymentConsoleSessionSupport;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 支付控制台轻量登录接口。
 */
@RestController
@RequestMapping("/payment-console/auth")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "plugin.payment.ops.console", name = "enabled", havingValue = "true", matchIfMissing = true)
public class PaymentConsoleAuthController {

    private final PaymentConsoleProperties properties;

    @GetMapping("/status")
    public ReturnResult<Object> status(HttpServletRequest request) {
        return ReturnResult.ok(statusPayload(request));
    }

    @PostMapping("/login")
    public ReturnResult<Object> login(@RequestBody(required = false) ConsoleLoginRequest loginRequest,
                                      HttpServletRequest request) {
        if (!properties.isAuthEnabled()) {
            PaymentConsoleSessionSupport.markAuthenticated(request.getSession(true), properties.getUsername());
            return ReturnResult.ok(statusPayload(request));
        }

        String username = trimToNull(loginRequest == null ? null : loginRequest.getUsername());
        String password = trimToNull(loginRequest == null ? null : loginRequest.getPassword());
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return ReturnResult.error("账号和密码不能为空");
        }
        if (!username.equals(properties.getUsername()) || !password.equals(properties.getPassword())) {
            return ReturnResult.error("账号或密码错误");
        }

        HttpSession session = request.getSession(true);
        PaymentConsoleSessionSupport.markAuthenticated(session, username);
        return ReturnResult.ok(statusPayload(request));
    }

    @PostMapping("/logout")
    public ReturnResult<Object> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            PaymentConsoleSessionSupport.clear(session);
            session.invalidate();
        }
        return ReturnResult.ok("已退出登录");
    }

    private Map<String, Object> statusPayload(HttpServletRequest request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("authEnabled", properties.isAuthEnabled());
        payload.put("authenticated", !properties.isAuthEnabled() || PaymentConsoleSessionSupport.isAuthenticated(request));
        payload.put("username", PaymentConsoleSessionSupport.username(request.getSession(false)));
        return payload;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    @Data
    public static class ConsoleLoginRequest {
        private String username;
        private String password;
    }
}
