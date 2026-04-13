package com.chua.starter.oauth.sso.support.token;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.chua.starter.oauth.client.support.contants.AuthConstant.TOKEN_PRE;
import static com.chua.starter.oauth.client.support.contants.AuthConstant.TOKEN_REFRESH_PRE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisTokenResolverVersionUpgradeTest {

    @Test
    void shouldReuseCachedUserStateWhenAuthVersionIsUnchanged() {
        RedisTokenResolver resolver = new RedisTokenResolver();
        RedisTemplate<String, Object> redisTemplate = Mockito.mock(RedisTemplate.class);
        ValueOperations<String, Object> valueOperations = Mockito.mock(ValueOperations.class);
        LoginCheck loginCheck = Mockito.mock(LoginCheck.class);
        String token = "AT-demo-token";
        String refreshToken = "RT-demo-token";
        UserResult cachedUser = createUser("1001", "admin", refreshToken, "v1");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(TOKEN_PRE + token)).thenReturn(cachedUser);
        when(loginCheck.getAuthVersion(any(UserResult.class))).thenReturn("v1");

        ReflectionTestUtils.setField(resolver, "stringRedisTemplate", redisTemplate);
        ReflectionTestUtils.setField(resolver, "loginCheck", loginCheck);

        ReturnResult<UserResume> result = resolver.upgradeForVersion(token, refreshToken);

        assertThat(result.isOk()).isTrue();
        assertThat(result.getData()).isInstanceOf(UserResult.class);
        assertThat(result.getData().getUserId()).isEqualTo("1001");
        assertThat(((UserResult) result.getData()).getToken()).isEqualTo(token);
        assertThat(((UserResult) result.getData()).getRefreshToken()).isEqualTo(refreshToken);
        verify(loginCheck, never()).getUserInfo(any(UserResult.class));
        verify(valueOperations, never()).set(eq(TOKEN_PRE + token), any(UserResume.class));
    }

    @Test
    void shouldRefreshRedisUserStateWhenAuthVersionChanges() {
        RedisTokenResolver resolver = new RedisTokenResolver();
        RedisTemplate<String, Object> redisTemplate = Mockito.mock(RedisTemplate.class);
        ValueOperations<String, Object> valueOperations = Mockito.mock(ValueOperations.class);
        LoginCheck loginCheck = Mockito.mock(LoginCheck.class);
        String token = "AT-demo-token";
        String refreshToken = "RT-demo-token";
        UserResult cachedUser = createUser("1001", "admin", refreshToken, "v1");
        UserResult refreshedUser = createUser("1001", "admin", refreshToken, "v2");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(TOKEN_PRE + token)).thenReturn(cachedUser);
        when(loginCheck.getAuthVersion(any(UserResult.class))).thenReturn("v2");
        when(loginCheck.getUserInfo(any(UserResult.class))).thenReturn(refreshedUser);
        when(redisTemplate.getExpire(TOKEN_REFRESH_PRE + refreshToken)).thenReturn(300L);

        ReflectionTestUtils.setField(resolver, "stringRedisTemplate", redisTemplate);
        ReflectionTestUtils.setField(resolver, "loginCheck", loginCheck);

        ReturnResult<UserResume> result = resolver.upgradeForVersion(token, refreshToken);

        assertThat(result.isOk()).isTrue();
        assertThat(result.getData().getUserId()).isEqualTo("1001");
        assertThat(((UserResult) result.getData()).getToken()).isEqualTo(token);
        verify(loginCheck).getUserInfo(any(UserResult.class));
        verify(valueOperations).set(eq(TOKEN_PRE + token), any(UserResume.class));
        verify(valueOperations).set(eq(TOKEN_REFRESH_PRE + refreshToken), any(UserResume.class), eq(Duration.ofSeconds(300)));
    }

    private UserResult createUser(String userId, String username, String refreshToken, String authVersion) {
        UserResult userResult = new UserResult();
        userResult.setUserId(userId);
        userResult.setUsername(username);
        userResult.setRefreshToken(refreshToken);
        Map<String, Object> ext = new LinkedHashMap<>();
        ext.put("authVersion", authVersion);
        userResult.setExt(ext);
        return userResult;
    }
}
