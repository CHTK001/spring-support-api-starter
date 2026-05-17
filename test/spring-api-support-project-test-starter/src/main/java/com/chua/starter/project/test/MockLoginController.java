package com.chua.starter.project.test;

import com.chua.common.support.lang.code.ReturnResult;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 当 OAuth 禁用时提供模拟登录端点，
 * 使前端 UI 测试能够正常通过登录页面。
 */
@RestController
@RequestMapping("/v2/user")
@ConditionalOnProperty(prefix = "plugin.oauth", name = "enable", havingValue = "false")
public class MockLoginController {

    @PostMapping("/login")
    public ReturnResult<Map<String, Object>> login(
            @RequestBody(required = false) Map<String, Object> body,
            HttpServletResponse response) {

        Map<String, Object> data = new LinkedHashMap<>();
        String fakeToken = "mock-token-" + UUID.randomUUID().toString().replace("-", "");

        data.put("accessToken", fakeToken);
        data.put("tokenType", "Bearer");
        data.put("refreshToken", "mock-refresh-" + UUID.randomUUID().toString().replace("-", ""));

        Map<String, Object> userInfo = new LinkedHashMap<>();
        userInfo.put("userId", "1");
        userInfo.put("userName", "sa");
        userInfo.put("nickname", "超级管理员");
        userInfo.put("roles", Set.of("ADMIN", "SUPER_ADMIN"));
        userInfo.put("perms", Set.of("*:*:*"));
        data.put("userInfo", userInfo);

        data.put("expires", 86400000L);
        data.put("isRemembered", true);

        response.addHeader("Set-Cookie", "x-oauth-cookie=" + fakeToken + "; path=/; HttpOnly");

        return ReturnResult.ok(data);
    }

    @PostMapping("/logout")
    public ReturnResult<Void> logout() {
        return ReturnResult.ok();
    }
}
