package com.chua.starter.oauth.sso.support.check;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.oauth.client.support.user.LoginResult;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import com.chua.starter.oauth.sso.support.security.LoginAttemptService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 登录入口的来源 IP 失败限流测试。
 *
 * <p>守护「跨账号密码喷洒」：按账号锁定对每个账号只失败一次的攻击无效，
 * 必须再有按可信来源 IP 的失败计数。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoginCheck: 来源 IP 失败限流")
class LoginCheckIpLimitTest {

    /**
     * 登录尝试服务
     */
    @Mock
    private LoginAttemptService loginAttemptService;

    /**
     * 达到 IP 失败上限时直接拒绝，不进入用户认证。
     */
    @Test
    @DisplayName("IP 失败超限: 直接拒绝")
    void shouldRejectWhenIpBlocked() {
        AuthServerProperties properties = new AuthServerProperties();
        properties.setLoginIpFailureMaxPerMinute(3);
        when(loginAttemptService.isIpBlocked("1.2.3.4", 3)).thenReturn(true);

        LoginCheck loginCheck = new LoginCheck(properties, null, loginAttemptService);
        ReturnResult<LoginResult> result = loginCheck.doLogin("1.2.3.4", "alice", "pwd", "WEB", null);

        assertNotNull(result);
        assertEquals("登录失败次数过多，请稍后再试", result.getMsg());
        verify(loginAttemptService).isIpBlocked("1.2.3.4", 3);
    }
}
