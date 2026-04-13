package com.chua.starter.oauth.sso.support.admin;

import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminControllerStandaloneTest {

    @Test
    void shouldExposeAndUpdateRuntimeStatus() throws Exception {
        AuthServerProperties properties = new AuthServerProperties();
        RateLimitManager rateLimitManager = new RateLimitManager();
        AdminController controller = new AdminController(properties, rateLimitManager);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/admin/status"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("signVerify")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("protocols")));

        mockMvc.perform(post("/admin/sign-verify").param("enabled", "false"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("签名校验已关闭")));

        mockMvc.perform(post("/admin/online-mode")
                        .param("mode", "LIMIT")
                        .param("maxCount", "2"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("LIMIT")));

        mockMvc.perform(post("/admin/rate-limit")
                        .contentType("application/json")
                        .content("""
                                {"enabled":true,"qps":25,"burstSize":40,"dimension":"ip"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("限流配置已更新")));
    }

    @Test
    void shouldRenderAdminConsoleHtml() throws Exception {
        AuthServerProperties properties = new AuthServerProperties();
        RateLimitManager rateLimitManager = new RateLimitManager();
        AdminController controller = new AdminController(properties, rateLimitManager);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("OAuth 管理控制台")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/oauth/admin")));
    }
}
