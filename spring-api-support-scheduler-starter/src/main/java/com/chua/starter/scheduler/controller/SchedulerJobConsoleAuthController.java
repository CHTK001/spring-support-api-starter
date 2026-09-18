package com.chua.starter.scheduler.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.annotation.Ignore;
import com.chua.starter.scheduler.properties.SchedulerJobPlatformProperties;
import com.chua.starter.scheduler.support.SchedulerJobConsoleSessionSupport;
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
 * 控制台轻量登录接口。
 *
 * <p>提供控制台登录状态的查询、登录与退出能力,
 * 账号密码校验通过后写入 会话。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@RestController
@Ignore
@RequestMapping("/job-console/auth")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "plugin.scheduler.job-platform", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SchedulerJobConsoleAuthController {

    /**
     * 调度平台配置
     */
    private final SchedulerJobPlatformProperties properties;

    /**
     * 查询当前登录状态。
     *
     * @param request HTTP 请求
     * @return 登录状态信息
     */
    @GetMapping("/status")
    public ReturnResult<Object> status(HttpServletRequest request) {
        return ReturnResult.ok(statusPayload(request));
    }

    /**
     * 控制台登录。
     *
     * @param loginRequest 登录请求
     * @param request      HTTP 请求
     * @return 登录状态信息
     */
    @PostMapping("/login")
    public ReturnResult<Object> login(@RequestBody(required = false) ConsoleLoginRequest loginRequest,
                                      HttpServletRequest request) {
        if (!properties.getConsole().isAuthEnabled()) {
            SchedulerJobConsoleSessionSupport.markAuthenticated(request.getSession(true), properties.getConsole().getUsername());
            return ReturnResult.ok(statusPayload(request));
        }

        String username = trimToNull(loginRequest == null ? null : loginRequest.getUsername());
        String password = trimToNull(loginRequest == null ? null : loginRequest.getPassword());
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return ReturnResult.error("账号和密码不能为空");
        }
        if (!username.equals(properties.getConsole().getUsername())
                || !password.equals(properties.getConsole().getPassword())) {
            return ReturnResult.error("账号或密码错误");
        }

        HttpSession session = request.getSession(true);
        SchedulerJobConsoleSessionSupport.markAuthenticated(session, username);
        return ReturnResult.ok(statusPayload(request));
    }

    /**
     * 控制台退出登录。
     *
     * @param request HTTP 请求
     * @return 退出提示
     */
    @PostMapping("/logout")
    public ReturnResult<Object> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            SchedulerJobConsoleSessionSupport.clear(session);
            session.invalidate();
        }
        return ReturnResult.ok("已退出登录");
    }

    /**
     * 组装登录状态信息。
     *
     * @param request HTTP 请求
     * @return 登录状态 映射
     */
    private Map<String, Object> statusPayload(HttpServletRequest request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("authEnabled", properties.getConsole().isAuthEnabled());
        payload.put("authenticated", !properties.getConsole().isAuthEnabled()
                || SchedulerJobConsoleSessionSupport.isAuthenticated(request));
        payload.put("username", SchedulerJobConsoleSessionSupport.username(request.getSession(false)));
        return payload;
    }

    /**
     * 去除空白, 空白文本转换为 空。
     *
     * @param value 文本值
     * @return 去除空白后的文本
     */
    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    /**
     * 控制台登录请求。
     *
     * @since 4.0.0.42
     * @author CH
     */
    @Data
    public static class ConsoleLoginRequest {

        /**
         * 登录用户名
         */
        private String username;

        /**
         * 登录密码
         */
        private String password;
    }
}
