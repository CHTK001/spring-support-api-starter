package com.chua.starter.collect.controller;

import com.chua.common.support.annotation.Ignore;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.collect.dto.CollectLoginDTO;
import com.chua.starter.collect.properties.CollectProperties;
import com.chua.starter.collect.support.CollectConsoleSessionSupport;
import com.chua.starter.collect.vo.CollectConsoleStatusVO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 采集控制台登录接口。
 *
 * <p>账号密码来自 {@code plugin.collect.console}，校验通过后写入 Servlet 会话，
 * 后续接口调用带同一会话 Cookie 即可。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Ignore
@RestController
@RequestMapping("/collect-console/auth")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "plugin.collect", name = "enable", havingValue = "true", matchIfMissing = true)
public class CollectConsoleAuthController {

    /**
     * 平台配置，含控制台账号。
     */
    private final CollectProperties properties;

    /**
     * 查询当前登录状态。
     *
     * @param request HTTP 请求
     * @return 登录状态
     */
    @Operation(summary = "控制台登录状态")
    @GetMapping("/status")
    public ReturnResult<CollectConsoleStatusVO> status(HttpServletRequest request) {
        return ReturnResult.ok(statusPayload(request));
    }

    /**
     * 控制台登录。
     *
     * @param login   登录入参
     * @param request HTTP 请求
     * @return 登录状态
     */
    @Operation(summary = "控制台登录")
    @PostMapping("/login")
    public ReturnResult<CollectConsoleStatusVO> login(@RequestBody(required = false) CollectLoginDTO login,
                                                      HttpServletRequest request) {
        CollectProperties.Console console = properties.getConsole();
        if (!console.isAuthEnabled()) {
            CollectConsoleSessionSupport.markAuthenticated(request.getSession(true), console.getUsername(),
                    console.getSessionTimeoutSeconds());
            return ReturnResult.ok(statusPayload(request));
        }
        String username = login == null ? null : trimToNull(login.getUsername());
        String password = login == null ? null : trimToNull(login.getPassword());
        if (username == null || password == null) {
            return ReturnResult.error("账号和密码不能为空");
        }
        if (trimToNull(console.getPassword()) == null) {
            return ReturnResult.error("控制台未配置密码，请设置 plugin.collect.console.password");
        }
        if (!username.equals(console.getUsername()) || !password.equals(console.getPassword())) {
            return ReturnResult.error("账号或密码错误");
        }
        HttpSession session = request.getSession(true);
        CollectConsoleSessionSupport.markAuthenticated(session, username, console.getSessionTimeoutSeconds());
        return ReturnResult.ok(statusPayload(request));
    }

    /**
     * 控制台退出登录。
     *
     * @param request HTTP 请求
     * @return 退出提示
     */
    @Operation(summary = "控制台退出登录")
    @PostMapping("/logout")
    public ReturnResult<String> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            CollectConsoleSessionSupport.clear(session);
            session.invalidate();
        }
        return ReturnResult.ok("已退出登录");
    }

    /**
     * 组装登录状态出参。
     *
     * @param request HTTP 请求
     * @return 登录状态
     */
    private CollectConsoleStatusVO statusPayload(HttpServletRequest request) {
        CollectProperties.Console console = properties.getConsole();
        CollectConsoleStatusVO vo = new CollectConsoleStatusVO();
        vo.setAuthEnabled(console.isAuthEnabled());
        vo.setAuthenticated(!console.isAuthEnabled() || CollectConsoleSessionSupport.isAuthenticated(request));
        vo.setUsername(CollectConsoleSessionSupport.username(request.getSession(false)));
        return vo;
    }

    /**
     * 去空白，空白转 null。
     *
     * @param value 文本
     * @return 去除空白后的文本
     */
    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
