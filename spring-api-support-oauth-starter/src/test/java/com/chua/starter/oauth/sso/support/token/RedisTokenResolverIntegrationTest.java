package com.chua.starter.oauth.sso.support.token;

import com.chua.starter.oauth.client.support.enums.LogoutType;
import com.chua.starter.oauth.client.support.user.LoginResult;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link RedisTokenResolver} 的 Redis 契约测试。
 *
 * <p>这是重构前的「安全网」：令牌签发、解析、刷新轮换、登出、禁用拉黑都走真实 Redis
 * （默认 127.0.0.1:16379），确保后续拆 {@code TokenStore} 时行为不变。</p>
 *
 * <p>没有可用 Redis 时自动跳过：-Doauth.test.redis.port=16379 可改端口。</p>
 *
 * <p>P3C40 去 mockito 改造：{@code LoginCheck} / {@code TokenBlacklistService} 不再用
 * {@code @Mock} + {@code verify}，改为手写的记录桩——断言真实执行了哪个调用，而不是
 * Mockito 的应声回音。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
@DisplayName("RedisTokenResolver: 真实 Redis 契约")
class RedisTokenResolverIntegrationTest {

    /**
     * Redis 端口
     */
    private static final int REDIS_PORT = Integer.getInteger("oauth.test.redis.port", 16379);

    /**
     * 测试用户
     */
    private static final String USER_ID = "1001";

    /**
     * 手写的 {@link LoginCheck} 记录桩。
     *
     * <p>{@code RedisTokenResolver} 在升级/解析时只用到 {@code getAuthVersion} 与
     * {@code getUserInfo}；SPI 无缘测试环境，所以直接返回记录的值，不再透掉
     * {@code ServiceProvider} 和 {@code UserInfoService}。</p>
     */
    private static class FakeLoginCheck extends LoginCheck {

        /** 逐笔录制的调用 */
        private final List<String> recorded = new ArrayList<>();

        FakeLoginCheck(AuthServerProperties properties) {
            super(properties);
        }

        @Override
        public String getAuthVersion(UserResult userResult) {
            recorded.add("getAuthVersion(" + userResult.getUserId() + ")");
            return null;
        }

        @Override
        public UserResume getUserInfo(UserResult userResult) {
            recorded.add("getUserInfo(" + userResult.getUserId() + ")");
            UserResume resume = new UserResume();
            BeanUtils.copyProperties(userResult, resume);
            return resume;
        }

        void assertRecorded(String name) {
            assertTrue(recorded.contains(name), "LoginCheck 未记录期望调用：recorded=" + recorded);
        }

        List<String> recorded() {
            return recorded;
        }
    }

    /**
     * 手写的 {@link TokenBlacklistService} 记录桩。
     *
     * <p>令牌、用户两级黑名单都在本地写读，`addUserToBlacklist` 时会询问当前场景：
     * 断言以「为什么调用」——而不是 mockito 的 `verify`。</p>
     */
    private static class FakeTokenBlacklistService implements TokenBlacklistService {

        /** 逐笔录制的调用 */
        private final List<String> recorded = new ArrayList<>();

        /** 失效力令牌 */
        private final Set<String> tokens = ConcurrentHashMap.newKeySet();

        /** 被全局禁用的用户 */
        private final Set<String> users = ConcurrentHashMap.newKeySet();

        @Override
        public void addToBlacklist(String token, BlacklistReason reason) {
            recorded.add("addToBlacklist(" + token + ", " + reason + ")");
            tokens.add(token);
        }

        @Override
        public void addToBlacklist(String token, BlacklistReason reason, long expireTime, String userId,
                                   String username) {
            recorded.add("addToBlacklist(" + token + ", " + reason + ", expire=" + expireTime + ")");
            tokens.add(token);
        }

        @Override
        public boolean isBlacklisted(String token) {
            recorded.add("isBlacklisted(" + token + ")");
            return tokens.contains(token);
        }

        @Override
        public void addUserToBlacklist(String userId, String reason) {
            recorded.add("addUserToBlacklist(" + userId + ", " + reason + ")");
            users.add(userId);
        }

        @Override
        public void removeUserFromBlacklist(String userId) {
            recorded.add("removeUserFromBlacklist(" + userId + ")");
            users.remove(userId);
        }

        @Override
        public boolean isUserBlacklisted(String userId) {
            recorded.add("isUserBlacklisted(" + userId + ")");
            return users.contains(userId);
        }

        @Override
        public void removeFromBlacklist(String token) {
            recorded.add("removeFromBlacklist(" + token + ")");
            tokens.remove(token);
        }

        @Override
        public void clearAll() {
            recorded.add("clearAll()");
            tokens.clear();
            users.clear();
        }

        void assertRecorded(String name) {
            assertTrue(recorded.contains(name), "黑名单未记录期望调用：recorded=" + recorded);
        }

        List<String> recorded() {
            return recorded;
        }
    }

    /**
     * 连接工厂
     */
    private JedisConnectionFactory factory;

    /**
     * 令牌模板
     */
    private RedisTemplate<String, Object> template;

    /**
     * 被测解析器
     */
    private RedisTokenResolver resolver;

    /**
     * 记录桩：登录检查
     */
    private FakeLoginCheck loginCheck;

    /**
     * 记录桩：黑名单
     */
    private FakeTokenBlacklistService tokenBlacklistService;

    /**
     * 建连并准备依赖。
     *
     * <p>没有可连接的 Redis 时直接跳过，不抛错——这些路径在 Redis 不到位时
     * 无法形成「签发→解析→续期→登出」的闭环。</p>
     */
    @BeforeEach
    void setUp() {
        Assumptions.assumeTrue(canConnect(), "Redis 未启动（端口 " + REDIS_PORT + "），跳过契约测试");

        factory = new JedisConnectionFactory(new RedisStandaloneConfiguration("127.0.0.1", REDIS_PORT));
        factory.afterPropertiesSet();
        template = redisTemplate(factory);

        try (RedisConnection connection = factory.getConnection()) {
            connection.serverCommands().flushDb();
        }

        tokenBlacklistService = new FakeTokenBlacklistService();
        loginCheck = new FakeLoginCheck(properties());

        resolver = new RedisTokenResolver();
        ReflectionTestUtils.setField(resolver, "stringRedisTemplate", template);
        ReflectionTestUtils.setField(resolver, "authServerProperties", properties());
        ReflectionTestUtils.setField(resolver, "loginCheck", loginCheck);
        ReflectionTestUtils.setField(resolver, "tokenBlacklistService", tokenBlacklistService);
        ReflectionTestUtils.setField(resolver, "sysUserLogService", null);
        ReflectionTestUtils.setField(resolver, "fingerprintStorageService", null);
    }

    /**
     * 销毁连接。
     */
    @AfterEach
    void tearDown() {
        if (factory != null) {
            factory.destroy();
        }
    }

    /**
     * 构造测试用户。
     *
     * @return 用户
     */
    private UserResult user() {
        UserResult user = new UserResult();
        user.setUserId(USER_ID);
        user.setUsername("alice");
        user.setUid("uid-contract");
        user.setLoginType("WEB");
        return user;
    }

    /**
     * 创建令牌并返回结果。
     *
     * @return 登录结果
     */
    private LoginResult create() {
        var created = resolver.createToken("1.2.3.4", user(), "WEB");
        assertTrue(created.isOk(), "创建令牌失败: " + created.getMsg());
        assertNotNull(created.getData());
        return created.getData();
    }

    /**
     * 签发 → 解析 → 在线列表。
     */
    @Test
    @DisplayName("createToken + resolve + 在线列表")
    void shouldCreateAndResolve() {
        LoginResult login = create();

        var resolved = resolver.resolve(null, login.getToken());
        assertTrue(resolved.isOk(), "刚签发的令牌应能解析: " + resolved.getMsg());
        assertTrue(USER_ID.equals(resolved.getData().getUserId()));

        var online = resolver.getOnlineUsers(new TokenResolver.OnlineUserQuery());
        if (online.getTotal() < 1) {
            java.util.Set<String> keys = template.keys("oauth:token:*");
            Object raw = keys == null || keys.isEmpty() ? null : template.opsForValue().get(keys.iterator().next());
            System.out.println("DIAG_KEYS=" + keys);
            System.out.println("DIAG_RAW_TYPE=" + (raw == null ? "null" : raw.getClass().getName()));
            System.out.println("DIAG_COPY=" + com.chua.common.support.utils.BeanUtils.copyProperties(raw, UserResult.class));
        }
        assertTrue(online.getTotal() >= 1, "在线列表应包含刚登录的会话");
        assertTrue(resolver.getOnlineStatus(USER_ID).getOnlineCount() >= 1);
    }

    /**
     * refresh 轮换：新令牌可用、旧令牌失效。
     */
    @Test
    @DisplayName("upgradeForRefresh: 轮换后旧令牌失效")
    void shouldRotateOnRefresh() {
        LoginResult login = create();
        assertTrue(resolver.resolve(null, login.getToken()).isOk());

        var upgraded = resolver.upgradeForRefresh(login.getToken(), login.getRefreshToken());
        assertTrue(upgraded.isOk(), "刷新失败: " + upgraded.getMsg());
        String newToken = ((UserResult) upgraded.getData()).getToken();
        assertNotEquals(login.getToken(), newToken, "应签发新令牌");

        assertFalse(resolver.resolve(null, login.getToken()).isOk(), "旧令牌必须失效");
        assertTrue(resolver.resolve(null, newToken).isOk(), "新令牌必须可用");

        tokenBlacklistService.assertRecorded("addToBlacklist(" + login.getToken() + ", " +
                TokenBlacklistService.BlacklistReason.REVOKED + ")");
    }

    /**
     * 单令牌登出后不可再解析。
     */
    @Test
    @DisplayName("logout(token): 登出后失效")
    void shouldInvalidateAfterLogout() {
        LoginResult login = create();

        resolver.logout(login.getToken());

        assertFalse(resolver.resolve(null, login.getToken()).isOk());
    }

    /**
     * 禁用：吊销全部会话并写入用户黑名单。
     */
    @Test
    @DisplayName("logout(userId, DISABLE): 吊销全部并拉黑")
    void shouldDisableUser() {
        LoginResult login = create();

        resolver.logout(USER_ID, LogoutType.DISABLE);

        assertFalse(resolver.resolve(null, login.getToken()).isOk(), "禁用后旧令牌必须失效");
        assertFalse(resolver.upgradeForRefresh(null, login.getRefreshToken()).isOk(),
                "禁用后 refreshToken 不得再续期");
        tokenBlacklistService.assertRecorded("addUserToBlacklist(" + USER_ID + ", 账号被禁用)");
    }

    /**
     * 用户黑名单命中时，即使令牌还在也必须拒绝。
     */
    @Test
    @DisplayName("用户黑名单: 令牌存在也拒绝")
    void shouldRejectBlacklistedUser() {
        LoginResult login = create();
        tokenBlacklistService.addUserToBlacklist(USER_ID, "测试拉黑");

        assertFalse(resolver.resolve(null, login.getToken()).isOk());
    }

    /**
     * 判断 Redis 是否可连接。
     *
     * @return 可连接返回 true
     */
    private boolean canConnect() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", REDIS_PORT), 300);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 构造与生产一致的 Redis 模板。
     *
     * @param connectionFactory 连接工厂
     * @return 模板
     */
    private RedisTemplate<String, Object> redisTemplate(JedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> result = new RedisTemplate<>();
        result.setConnectionFactory(connectionFactory);

        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.activateDefaultTyping(om.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL);
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(om, Object.class);

        result.setKeySerializer(new StringRedisSerializer());
        result.setValueSerializer(serializer);
        result.setHashKeySerializer(new StringRedisSerializer());
        result.setHashValueSerializer(serializer);
        result.afterPropertiesSet();
        return result;
    }

    /**
     * 测试用认证服务端配置。
     *
     * @return 配置
     */
    private AuthServerProperties properties() {
        AuthServerProperties properties = new AuthServerProperties();
        properties.setExpire(60);
        properties.setRefreshExpire(120);
        properties.setTokenGeneration("simple");
        properties.setOnline(AuthServerProperties.Online.MULTIPLE);
        return properties;
    }
}
