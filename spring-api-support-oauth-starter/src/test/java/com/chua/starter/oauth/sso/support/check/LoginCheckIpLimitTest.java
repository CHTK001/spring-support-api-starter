package com.chua.starter.oauth.sso.support.check;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.oauth.client.support.user.LoginResult;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import com.chua.starter.oauth.sso.support.security.LoginAttemptService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 登录入口的来源 IP 失败限流测试。
 *
 * <p>守护「跨账号密码喷洒」：按账号锁定对每个账号只失败一次的攻击无效，
 * 必须有按可信来源 IP 的失败计数；且失败计数必须真的落到 Redis 上。
 * 量化：在 {@code oauth:login:ip-failure:<ip>} 这个键上记数，次数 >= 每分钟
 * 失败上限时 {@link LoginCheck#doLogin} 必须直接拒绝，不进入用户认证。</p>
 *
 * <p>P3C40 去 mockito 改造：原实现用 {@code @Mock LoginAttemptService} 把全部
 * Redis 交互悬空，只断言一个必然放行的密钥假（isIpBlocked 固定 true）——
 * 如今用 {@link RecordingStringRedisTemplate} 把键的读写和计数录下来，
 * 断言「失败次数是否真的落键」。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@DisplayName("LoginCheck: 来源 IP 失败限流")
class LoginCheckIpLimitTest {

    /**
     * 可观测的 StringRedisTemplate 桩。
     *
     * <p>被 {@link LoginAttemptService} 读、写、记数时用到的 Redis 方法全部录制：
     * 增加、查询、过期、删除、存在性检查，缺哪项就报「未覆写」。</p>
     */
    static class RecordingStringRedisTemplate extends StringRedisTemplate {

        /** 按键名录制的 String 型值 */
        private final Map<String, String> store = new ConcurrentHashMap<>();

        /** 记录收到过的操作 */
        private final List<String> recorded = new ArrayList<>();

        /** 最后设置的过期时间 */
        private Object lastExpire = null;

        /**
         * 记录键值对的当前值，暴露给断言使用。
         *
         * @return 该键上次塞入的值，未设置返回 null
         */
        String getStoredValue(String key) {
            return store.get(key);
        }

        /**
         * 暴露录制的操作序列，断言键是否被使用。
         *
         * @return 操作记录列表
         */
        List<String> getRecorded() {
            return recorded;
        }

        /**
         * 用动态代理录制 {@link ValueOperations}（String 型）的每次调 param。
         *
         * @return 记录可以代理的 ValueOperations
         */
        @Override
        public ValueOperations<String, String> opsForValue() {
            InvocationHandler handler = (proxy, method, args) -> {
                switch (method.getName()) {
                    case "get":
                        String retrieved = store.get(String.valueOf(args[0]));
                        recorded.add("get " + args[0] + " -> " + retrieved);
                        return retrieved;
                    case "increment": {
                        String key = (String) args[0];
                        Long count = store.containsKey(key)
                                ? Long.parseLong(store.get(key)) + 1L : 1L;
                        store.put(key, String.valueOf(count));
                        recorded.add("increment " + key + " -> " + count);
                        return count;
                    }
                    case "set": {
                        String key = (String) args[0];
                        String value = (String) args[1];
                        store.put(key, value);
                        recorded.add("set " + key + " -> " + value);
                        return null;
                    }
                    case "delete": {
                        String key = String.valueOf(args[0]);
                        recorded.add("delete " + key);
                        return store.remove(key) != null;
                    }
                    default:
                        throw new UnsupportedOperationException(
                                "本测试未覆写 ValueOperations." + method.getName()
                                        + "() —— 说明被试路径发生了意料之外的调用，请补断言而不是放宽状");
                }
            };
            return (ValueOperations<String, String>) Proxy.newProxyInstance(
                    getClass().getClassLoader(), new Class<?>[]{ValueOperations.class}, handler);
        }

        /** 录制的 expire，记录使用的是哪个键 */
        @Override
        public Boolean expire(String key, Duration timeout) {
            recorded.add("expire " + key + " ttl=" + timeout);
            lastExpire = timeout;
            return Boolean.TRUE;
        }

        /** 录制的 expire，记录使用的是哪个键 */
        @Override
        public Boolean expire(String key, long timeout, TimeUnit unit) {
            recorded.add("expire " + key + " ttl=" + timeout + " " + unit);
            return Boolean.TRUE;
        }

        /** 录制的 hasKey，记录的是哪个键 */
        @Override
        public Boolean hasKey(String key) {
            Boolean exists = store.containsKey(key);
            recorded.add("hasKey " + key + " -> " + exists);
            return exists;
        }

        /** 录制的 getExpire，记录的是哪个键 */
        @Override
        public Long getExpire(String key) {
            recorded.add("getExpire " + key);
            return lastExpire instanceof Duration
                    ? ((Duration) lastExpire).toSeconds()
                    : null;
        }

        /** 录制的 delete，记录的是哪个键 */
        @Override
        public Boolean delete(String key) {
            recorded.add("delete " + key);
            return store.remove(key) != null;
        }
    }

    /**
     * 达到 IP 失败上限时直接拒绝，不进入用户认证。
     */
    @Test
    @DisplayName("IP 失败超限: 直接拒绝")
    void shouldRejectWhenIpBlocked() {
        RecordingStringRedisTemplate redis = new RecordingStringRedisTemplate();
        // 模拟这个 IP 在 60 秒窗口内已经记了 9999 次失败
        redis.opsForValue().set("oauth:login:ip-failure:1.2.3.4", "9999");
        LoginAttemptService attemptService = new LoginAttemptService(redis);
        AuthServerProperties properties = new AuthServerProperties();
        properties.setLoginIpFailureMaxPerMinute(3);

        assertTrue(attemptService.isIpBlocked("1.2.3.4", 3), "来源 IP 失败次数已超限");

        LoginCheck loginCheck = new LoginCheck(properties, null, attemptService);
        ReturnResult<LoginResult> result =
                loginCheck.doLogin("1.2.3.4", "alice", "pwd", "WEB", null);

        assertEquals("登录失败次数过多，请稍后再试", result.getMsg(), "必须拒绝，不能进入认证");
        assertTrue(redis.getRecorded().contains("get oauth:login:ip-failure:1.2.3.4 -> 9999"),
                "一次完整的限流判定必须从 Redis 取出同一个键");
        assertFalse(result.isOk());
    }

    /**
     * 未达上限的 IP 继续认证，幂等累加。
     */
    @Test
    @DisplayName("IP 失败未超限: 累加记录")
    void shouldCountWhenIpNotBlocked() {
        RecordingStringRedisTemplate redis = new RecordingStringRedisTemplate();
        LoginAttemptService attemptService = new LoginAttemptService(redis);
        AuthServerProperties properties = new AuthServerProperties();
        properties.setLoginIpFailureMaxPerMinute(3);

        // 第一次记数
        assertEquals(1L, attemptService.recordIpFailure("1.2.3.4"));
        // 读到与刚才相同的键
        assertEquals("1", redis.getStoredValue("oauth:login:ip-failure:1.2.3.4"));

        // 再记一次，累计到上限
        assertEquals(2L, attemptService.recordIpFailure("1.2.3.4"));
        // 仍未超限
        assertFalse(attemptService.isIpBlocked("1.2.3.4", 3));

        // 第三次记数突破上限
        assertEquals(3L, attemptService.recordIpFailure("1.2.3.4"));
        assertTrue(attemptService.isIpBlocked("1.2.3.4", 3));

        // 清空计数后不再拦截
        attemptService.clearIpFailures("1.2.3.4");
        assertFalse(attemptService.isIpBlocked("1.2.3.4", 3));
    }
}
