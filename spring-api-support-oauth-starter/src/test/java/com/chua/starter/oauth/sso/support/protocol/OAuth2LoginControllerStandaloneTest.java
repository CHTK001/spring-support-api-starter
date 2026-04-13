package com.chua.starter.oauth.sso.support.protocol;

import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import com.chua.starter.oauth.sso.support.properties.ThirdPartyLoginProperties;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OAuth2LoginControllerStandaloneTest {

    private OAuth2LoginController newController() {
        ThirdPartyLoginProperties properties = new ThirdPartyLoginProperties();
        properties.getGitee().setEnabled(true);
        properties.getGitee().setClientId("test-client-id");
        properties.getGitee().setClientSecret("test-client-secret");
        properties.getGitee().setRedirectUri("http://127.0.0.1:19180/oauth2/callback/gitee");
        properties.getGithub().setEnabled(false);
        return new OAuth2LoginController(properties, new AuthServerProperties(), Mockito.mock(LoginCheck.class));
    }

    @Test
    void shouldExposePlatformsAndGenerateAuthorizeUrl() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(newController())
                .addPlaceholderValue("plugin.auth.server.context-path", "")
                .build();

        mockMvc.perform(get("/oauth2/platforms"))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String body = result.getResponse().getContentAsString();
                    org.junit.jupiter.api.Assertions.assertTrue(body.contains("gitee"));
                    org.junit.jupiter.api.Assertions.assertTrue(body.contains("github"));
                });

        var authorizeResult = newController().getAuthorizeUrl("gitee", "/home");
        org.junit.jupiter.api.Assertions.assertTrue(String.valueOf(authorizeResult.getData()).contains("gitee.com/oauth/authorize"));
        org.junit.jupiter.api.Assertions.assertTrue(String.valueOf(authorizeResult.getData()).contains("test-client-id"));
    }

    @Test
    void shouldRejectDisabledPlatformAuthorizeRequest() {
        var result = newController().getAuthorizeUrl("github", "/home");
        org.junit.jupiter.api.Assertions.assertFalse(result.isOk());
        org.junit.jupiter.api.Assertions.assertTrue(String.valueOf(result.getMsg()).contains("github"));
    }

    @Test
    void shouldRejectExpiredCallbackStateForApiAndPageFlow() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(newController())
                .addPlaceholderValue("plugin.auth.server.context-path", "")
                .build();

        mockMvc.perform(post("/oauth2/callback/gitee")
                        .param("code", "dummy-code")
                        .param("state", "expired-state"))
                .andExpect(status().isOk())
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertTrue(
                        result.getResponse().getContentAsString().contains("授权已过期")));

        mockMvc.perform(get("/oauth2/callback/gitee")
                        .param("code", "dummy-code")
                        .param("state", "expired-state"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/"))
                .andExpect(cookie().exists("Access-Message"));
    }
}
