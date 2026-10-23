package com.chua.starter.oauth.sso.support.token;

import com.chua.common.support.spi.annotations.Extension;
import com.chua.common.support.utils.BeanUtils;
import com.chua.common.support.lang.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.objects.annotation.AutoInject;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.IdUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.utils.CookieUtil;
import com.chua.starter.oauth.client.support.enums.LogoutType;
import com.chua.starter.oauth.client.support.user.LoginResult;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.system.service.SysUserLogService;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.generation.TokenGeneration;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import com.chua.starter.entity.system.SysUserLog;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.servlet.http.Cookie;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ValueOperations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.chua.starter.oauth.client.support.contants.AuthConstant.TOKEN_PRE;
import static com.chua.starter.oauth.client.support.contants.AuthConstant.TOKEN_REFRESH_PRE;

/**
 * Redis 令牌管理器
 *
 * @author CH
 * @since 4.0.0
 */
@Slf4j
@com.chua.common.support.spi.annotations.Spi("redis")
@SuppressWarnings("ALL")
@Service
public class RedisTokenResolver implements TokenResolver {
    /**
     * 认证_版本
    */
    private static final String AUTH_VERSION = "authVersion";

    /**
     * 默认令牌过期时间（秒），1天
     */
    private static final long DEFAULT_EXPIRE_SECONDS = 86400L;

    /**
     * 在线用户列表单次最多读取的令牌数
     *
     * <p>在线列表目前是「SCAN 全量令牌 + 逐个 GET」实现的，成本随在线会话数线性增长。
     * 这里加一个硬上限，避免管理页刷新把 Redis 打满；超过上限时结果会被截断并打 WARN。
     * 根本解是维护 userId → token 索引，见后续改造项。</p>
     */
    private static final int ONLINE_USER_SCAN_LIMIT = 20_000;

    /**
     * 令牌 解析结果本地缓存（Guava 缓存）
     * <p>
     * 用于减少高并发场景下对 Redis 的重复查询，缓存热点 令牌 的解析结果。
     * TTL 固定为 15 秒，并发场景下同一 令牌 在 15 秒内只会查一次 Redis。
     * </p>
     */
    private static final Cache<String, ReturnResult<UserResume>> TOKEN_RESOLVE_CACHE =
            CacheBuilder.newBuilder()
                    .maximumSize(10_000)
                    .expireAfterWrite(java.time.Duration.ofSeconds(15))
                    .build();

    /**
     * Redis 扫描 每次扫描返回的数量
     */
    private static final int SCAN_COUNT = 1000;

    /**
     * 默认登录类型
     */
    private static final String DEFAULT_LOGIN_TYPE = "WEB";

    @Autowired
    private RedisTemplate<String, Object> stringRedisTemplate; // 字符串redistemplate

    @Autowired
    /**
     * 认证服务器属性
    */
    private AuthServerProperties authServerProperties;
    @Autowired
    /**
     * Logincheck
    */
    private LoginCheck loginCheck;
    @Autowired
    /**
     * SYS用户日志服务
    */
    private SysUserLogService sysUserLogService;
    @Autowired
    /**
     * 令牌blacklist服务
    */
    private TokenBlacklistService tokenBlacklistService;
    @Autowired(required = false)
    /**
     * 指纹存储服务
    */
    private com.chua.starter.oauth.sso.support.service.FingerprintStorageService fingerprintStorageService;

    @Override
    /**
     * 创建令牌
    */
    public ReturnResult<LoginResult> createToken(String address, UserResult userResult, String authType) {
        String userId = userResult.getUserId();
        
 // 检查在线上限（基于 用户id，不区分登录方式）
        ReturnResult<Void> checkResult = checkOnlineLimit(userResult);
        if (!checkResult.isOk()) {
            return ReturnResult.error(checkResult.getMsg());
        }

 // 根据认证类型选择令牌generation：JWT走jwt令牌generation，其他走配置的令牌generation
        TokenGeneration tokenGeneration;
        if ("jwt".equalsIgnoreCase(authType) || "JWT".equalsIgnoreCase(authType)) {
            tokenGeneration = ServiceProvider.of(TokenGeneration.class).getExtension("jwt");
        } else {
 // 配置为空时回退到默认实现（简单），避免空指针导致登录直接失败
            String generationName = "simple";
            if (null != authServerProperties && StringUtils.isNotBlank(authServerProperties.getTokenGeneration())) {
                generationName = authServerProperties.getTokenGeneration();
            }
            tokenGeneration = ServiceProvider.of(TokenGeneration.class).getExtension(generationName);
            if (tokenGeneration == null) {
                // 回退失败时兜底取第一个可用实现
                tokenGeneration = ServiceProvider.of(TokenGeneration.class).getExtension("simple");
            }
        }
        if (tokenGeneration == null) {
            return ReturnResult.error("Token生成器不可用");
        }
        userResult.setAddress(address);
        String uid = userResult.getUid();

 // 使用 令牌构建器 生成 令牌 和 refresh令牌
        String uidHash = tokenGeneration.generation(uid);
        String token = TokenBuilder.generateAccessToken(userId, uidHash);
        String refreshToken = TokenBuilder.generateRefreshToken(userId, uidHash);

        String redisKey = TOKEN_PRE + token;
        String redisRefreshKey = TOKEN_REFRESH_PRE + refreshToken;

 // 处理在线控制（单个模式踢掉旧设备）
        handleOnlineControl(userResult, token, refreshToken);

        // 计算过期时间（用户配置 > 全局配置）
        long expire = calculateExpireTime(userResult);
        long refreshExpire = calculateRefreshExpireTime(userResult);
        
        userResult.setExpireTime(expire);
        userResult.setRefreshToken(refreshToken);
        userResult.setRefreshExpireTime(refreshExpire);
        userResult.setToken(token);

        // 从 ext 中获取并存储浏览器指纹
        extractAndSetFingerprint(userResult);

        // 清理 ext 中不可序列化的值
        sanitizeExt(userResult);

        // 存储 Token(一次性设置值和过期时间,减少 Redis 往返)
        if (expire > 0) {
            stringRedisTemplate.opsForValue().set(redisKey, userResult, Duration.ofSeconds(expire));
            stringRedisTemplate.opsForValue().set(redisRefreshKey, userResult, Duration.ofSeconds(refreshExpire));
        } else {
            stringRedisTemplate.opsForValue().set(redisKey, userResult);
            stringRedisTemplate.opsForValue().set(redisRefreshKey, userResult);
        }

        LoginResult loginResult = new LoginResult();
        loginResult.setToken(token);
        loginResult.setRefreshToken(refreshToken);
        // 设置浏览器指纹到返回结果
        loginResult.setFingerprint(userResult.getFingerprint());

        // 将指纹+IP+用户信息写入 MySQL 缓存（供反查使用）
        saveFingerprintRecord(userResult, address);

        log.info("[TOKEN][userId={}] 创建成功: mode={}, expire={}s, refreshExpire={}s, token={}...",
                userId, getOnlineMode(userResult), expire, refreshExpire,
                token.length() > 8 ? token.substring(0, 8) : token);

        return ReturnResult.ok(loginResult);
    }

    /**
     * 从 ext 中提取并设置浏览器指纹
     * <p>从用户的 ext 参数中获取 fingerprint 并存储到 UserResult</p>
     *
     * @param userResult 用户信息
     */
    private void extractAndSetFingerprint(UserResult userResult) {
        Map<String, Object> ext = userResult.getExt();
        if (ext == null) {
            return;
        }

        Object fingerprintObj = ext.get("fingerprint");
        if (fingerprintObj != null) {
            userResult.setFingerprint(fingerprintObj.toString());
        }
    }

    /**
     * 将登录时的指纹+IP+用户信息写入 MySQL + Redis 缓存，供反查接口使用
     *
     * @param userResult 已包含 fingerprint/地址/ext 信息的用户结果
     * @param address    客户端 IP 地址
     */
    private void saveFingerprintRecord(UserResult userResult, String address) {
        if (fingerprintStorageService == null) {
            return;
        }
        try {
            String fp = userResult.getFingerprint();
            if (StringUtils.isBlank(fp) || StringUtils.isBlank(userResult.getUserId())) {
                return;
            }
            Map<String, Object> ext = userResult.getExt();
            String ua = ext != null ? String.valueOf(ext.get("ua")) : null;
            com.chua.starter.common.support.network.UserAgent.UserAgentInfo uaInfo = null;
            if (StringUtils.isNotBlank(ua)) {
                uaInfo = com.chua.starter.common.support.network.UserAgent.parseUserAgentString(ua);
            }
            fingerprintStorageService.saveFingerprint(
                    new com.chua.starter.oauth.sso.support.service.FingerprintStorageService.FingerprintRecord(
                            userResult.getUserId(),
                            userResult.getUsername(),
                            fp,
                            address,
                            userResult.getLoginType(),
                            ua,
                            uaInfo != null ? uaInfo.getBrowser().toString() : null,
                            uaInfo != null ? uaInfo.getOperatingSystem().getName() : null));
        } catch (Exception e) {
            log.warn("[Fingerprint] 写入指纹映射失败: userId={}", userResult.getUserId(), e);
        }
    }

    /**
     * 检查在线上限
     * <p>基于 userId 统计在线数量，不区分登录方式</p>
     *
     * @param userResult 用户信息
     * @return 检查结果，失败时返回错误信息
     */
    private ReturnResult<Void> checkOnlineLimit(UserResult userResult) {
        AuthServerProperties.Online onlineMode = getOnlineMode(userResult);

 // 多个 模式不限制
        if (onlineMode == AuthServerProperties.Online.MULTIPLE) {
            return ReturnResult.ok();
        }

 // 单个 模式允许登录（会踢掉旧设备）
        if (onlineMode == AuthServerProperties.Online.SINGLE) {
            return ReturnResult.ok();
        }

 // 限制 模式检查上限
        if (onlineMode == AuthServerProperties.Online.LIMIT) {
            int maxOnline = getMaxOnlineCount(userResult);
            int currentOnline = countUserOnlineTokens(userResult.getUserId());
            if (currentOnline >= maxOnline) {
                return ReturnResult.error("已达到最大在线设备数量限制(" + maxOnline + ")，请先在其他设备退出登录");
            }
        }

        return ReturnResult.ok();
    }

    /**
     * 读取用户级选项的字符串值。
     *
     * <p><b>为什么不能用 {@code String.valueOf(userResult.getOption(key))}</b>：
     * 选项缺失时 {@code getOption} 返回 {@code null}，而
     * {@code String.valueOf((Object) null)} 得到的是<b>字面量 {@code "null"}</b>
     * —— 它既不是空串也不是 {@code null}，因此
     * {@link StringUtils#isNotBlank(String)} 判定为「有值」，随后
     * {@code Online.valueOf("NULL")} 抛
     * {@code IllegalArgumentException: No enum constant
     * ...AuthServerProperties.Online.NULL}，
     * {@code Long.parseInt/parseLong("null")} 抛
     * {@code NumberFormatException: For input string: "null"}。
     * 这两个异常都被各自的 catch 兜住、不会导致登录失败，但<b>每次登录都会刷一整页异常栈</b>，
     * 既污染日志，又把真实异常淹没在噪声里。</p>
     *
     * @param userResult 用户信息
     * @param key        选项名
     * @return 选项的字符串值（已去首尾空白）；选项不存在、值为 {@code null} 或全空白时返回 {@code null}
     */
    private String optionAsString(UserResult userResult, String key) {
        if (userResult == null) {
            return null;
        }
        var option = userResult.getOption(key);
        if (option == null) {
            return null;
        }
        var value = String.valueOf(option).trim();
        return value.isEmpty() ? null : value;
    }

    /**
     * 获取在线模式（用户配置 > 全局配置）
     * @param userResult 用户结果
     * @return 获取onlinemode的结果
     */
    private AuthServerProperties.Online getOnlineMode(UserResult userResult) {
        String userOnlineMode = optionAsString(userResult, "onlineMode");
        if (StringUtils.isNotBlank(userOnlineMode)) {
            try {
                return AuthServerProperties.Online.valueOf(userOnlineMode.toUpperCase());
            } catch (Exception e) {
                log.warn("解析在线模式失败,userOnlineMode={}", userOnlineMode, e);
            }
        }
        // 未注入配置时，采用默认多点登录策略，避免空指针导致登录直接失败
        if (null == authServerProperties || null == authServerProperties.getOnline()) {
            return AuthServerProperties.Online.MULTIPLE;
        }
        return authServerProperties.getOnline();
    }

    /**
     * 获取最大在线数（用户配置 > 全局配置）
     * @param userResult 用户结果
     * @return 获取最大online数量的结果
     */
    private int getMaxOnlineCount(UserResult userResult) {
        String maxOnlineCount = optionAsString(userResult, "maxOnlineCount");
        if (StringUtils.isNotBlank(maxOnlineCount)) {
            try {
                int count = Integer.parseInt(maxOnlineCount);
                if (count > 0) {
                    return count;
                }
            } catch (Exception ignored) {
            }
        }
 // 未注入配置时，回退到默认的在线数量限制（与 认证服务端属性 默认值保持一致）
        if (null == authServerProperties) {
            return 3;
        }
        return authServerProperties.getMaxOnlineCount();
    }

    /**
     * 统计用户当前在线令牌数量
     * <p>基于 userId 统计，不区分登录方式</p>
     *
     * @param userId 用户标识
     * @return 在线Token数量
     */
    private int countUserOnlineTokens(String userId) {
        if (StringUtils.isBlank(userId)) {
            return 0;
        }
        try {
            String pattern = TOKEN_PRE + TokenBuilder.buildAccessTokenPattern(userId);
            Set<String> keys = scanKeys(pattern);
            return keys.size();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 删除用户所有令牌（基于 用户id哈希，不依赖 用户id，即使 令牌 已从 Redis 删除也能工作）
     *
     * @param userIdHash 用户id哈希（从 令牌 中提取的32位 MD5）
     */
    private void deleteUserAllTokensByHash(String userIdHash) {
        if (StringUtils.isBlank(userIdHash)) {
            return;
        }
 // 删除所有 access令牌
        Set<String> tokenKeys = scanKeys(TOKEN_PRE + TokenBuilder.buildAccessTokenPatternByHash(userIdHash));
        if (!tokenKeys.isEmpty()) {
            stringRedisTemplate.delete(tokenKeys);
        }
 // 删除所有 refresh令牌
        Set<String> refreshKeys = scanKeys(TOKEN_REFRESH_PRE + TokenBuilder.buildRefreshTokenPatternByHash(userIdHash));
        if (!refreshKeys.isEmpty()) {
            stringRedisTemplate.delete(refreshKeys);
        }
    }

    /**
     * 删除用户所有令牌（基于 用户id，不区分登录方式）
     *
     * @param userId 用户标识
     */
    private void deleteUserAllTokens(String userId) {
        deleteUserAllTokens(userId, null, null);
    }

    /**
     * 删除用户所有令牌（基于 用户id，不区分登录方式），但保留指定的令牌。
     *
     * <p><b>为什么需要 保留 参数</b>：SINGLE 单点登录时，新令牌在
     * {@link #createToken} 里<b>先</b>写入 Redis，<b>后</b>由
     * {@link #handleOnlineControl} 异步踢旧设备。若这里无差别删除，
     * 新签发的令牌会被自己删掉，表现为：登录接口返回
     * {@code code=00000} 且带着一个看起来正常的 token，但该 token
     * 立刻失效 —— Redis 里查不到 {@code oauth:token:*任何键}，
     * 后续任何鉴权都返回「未认证」（表现为接口 403
     * 「用户信息不存在，请重新登录」）。</p>
     *
     * @param userId           用户标识
     * @param keepToken        需要保留的 access令牌；为 {@code null} 表示不保留
     * @param keepRefreshToken 需要保留的 refresh令牌；为 {@code null} 表示不保留
     */
    private void deleteUserAllTokens(String userId, String keepToken, String keepRefreshToken) {
        if (StringUtils.isBlank(userId)) {
            return;
        }
 // 删除所有 access令牌（剔除本次登录新签发的那一枚）
        Set<String> tokenKeys = scanKeys(TOKEN_PRE + TokenBuilder.buildAccessTokenPattern(userId));
        if (StringUtils.isNotBlank(keepToken)) {
            tokenKeys.remove(TOKEN_PRE + keepToken);
        }
        if (!tokenKeys.isEmpty()) {
            stringRedisTemplate.delete(tokenKeys);
        }
 // 删除所有 refresh令牌（剔除本次登录新签发的那一枚）
        Set<String> refreshKeys = scanKeys(TOKEN_REFRESH_PRE + TokenBuilder.buildRefreshTokenPattern(userId));
        if (StringUtils.isNotBlank(keepRefreshToken)) {
            refreshKeys.remove(TOKEN_REFRESH_PRE + keepRefreshToken);
        }
        if (!refreshKeys.isEmpty()) {
            stringRedisTemplate.delete(refreshKeys);
        }
    }

    /**
     * 计算 令牌 过期时间
     * <p>优先级：用户配置 > 全局配置</p>
     *
     * @param userResult 用户信息
     * @return 过期时间（秒）
     */
    private long calculateExpireTime(UserResult userResult) {
        // 用户级配置优先
        String tokenExpire = optionAsString(userResult, "tokenExpire");
        if (StringUtils.isNotBlank(tokenExpire)) {
            try {
                long expire = Long.parseLong(tokenExpire);
                if (expire > 0) {
                    return expire;
                }
            } catch (Exception e) {
                log.warn("解析tokenExpire失败,value={}", tokenExpire, e);
            }
        }
        // 兼容旧字段
        if (userResult.getExpireTime() != null && userResult.getExpireTime() > 0) {
            return userResult.getExpireTime();
        }
        // 全局配置
        return authServerProperties.getExpire();
    }

    /**
     * 计算 refresh令牌 过期时间
     * <p>优先级：用户配置 > 全局配置</p>
     *
     * @param userResult 用户信息
     * @return 过期时间（秒）
     */
    private long calculateRefreshExpireTime(UserResult userResult) {
        // 用户级配置优先
        String refreshTokenExpire = optionAsString(userResult, "refreshTokenExpire");
        if (StringUtils.isNotBlank(refreshTokenExpire)) {
            try {
                long expire = Long.parseLong(refreshTokenExpire);
                if (expire > 0) {
                    return expire;
                }
            } catch (Exception ignored) {
            }
        }
        // 全局配置
        return authServerProperties.getRefreshExpire();
    }

    /**
     * 清理 ext 中不可序列化的值
     * <p>
     * 将 本地日期时间、本地日期 等类型转换为字符串格式，确保 Redis 序列化正常
     * </p>
     *
     * @param userResult 用户信息
     * @since 1.0.0
     */
    private void sanitizeExt(UserResult userResult) {
        Map<String, Object> ext = userResult.getExt();
        if (ext == null || ext.isEmpty()) {
            return;
        }

        Map<String, Object> sanitized = new HashMap<>(ext.size());
        for (Map.Entry<String, Object> entry : ext.entrySet()) {
            sanitized.put(entry.getKey(), sanitizeExtValue(entry.getValue()));
        }

        userResult.setExt(sanitized);
    }

    /**
     * sanitizeext值
     *
     * @param value 值
     * @return sanitizeext值的结果
     */
    private Object sanitizeExtValue(Object value) {
        if (value == null) {
            return null;
        }

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime.format(dateTimeFormatter);
        }
        if (value instanceof LocalDate localDate) {
            return localDate.format(dateFormatter);
        }
        if (value instanceof Date date) {
            return date.getTime();
        }
        if (value instanceof String || value instanceof Number || value instanceof Boolean) {
            return value;
        }
        if (value instanceof Map<?, ?> mapValue) {
            Map<String, Object> sanitizedMap = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : mapValue.entrySet()) {
                if (entry.getKey() == null) {
                    continue;
                }
                sanitizedMap.put(String.valueOf(entry.getKey()), sanitizeExtValue(entry.getValue()));
            }
            return sanitizedMap;
        }
        if (value instanceof Collection<?> collectionValue) {
            List<Object> sanitizedList = new ArrayList<>(collectionValue.size());
            for (Object item : collectionValue) {
                sanitizedList.add(sanitizeExtValue(item));
            }
            return sanitizedList;
        }
        if (value.getClass().isArray()) {
            int length = java.lang.reflect.Array.getLength(value);
            List<Object> sanitizedList = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                sanitizedList.add(sanitizeExtValue(java.lang.reflect.Array.get(value, i)));
            }
            return sanitizedList;
        }
        try {
            return value.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 处理在线控制
     * <p>仅 SINGLE 模式需要踢掉旧设备，LIMIT 模式在 checkOnlineLimit 已拒绝</p>
     *
     * <p><b>注意</b>：本方法在 {@link #createToken} 里于「新令牌写入 Redis 之前」被调用，
     * 且踢出动作是异步的，因此实际删除发生在新令牌写入之后。
     * 所以必须把新令牌（access 与 refresh）作为「保留项」传给
     * {@link #deleteUserAllTokens}，否则新令牌会被自己的踢出动作删掉。</p>
     *
     * @param userResult    用户信息
     * @param newToken      新签发的 access令牌
     * @param newRefreshToken 新签发的 refresh令牌
     */
    private void handleOnlineControl(UserResult userResult, String newToken, String newRefreshToken) {
        AuthServerProperties.Online onlineMode = getOnlineMode(userResult);

        // SINGLE 模式：踢掉该用户所有旧设备(异步执行,不阻塞登录返回)
        if (onlineMode == AuthServerProperties.Online.SINGLE) {
            String userId = userResult.getUserId();
            String newTokenPrefix = newToken.length() > 8 ? newToken.substring(0, 8) : newToken;
            Thread.ofVirtual().start(() -> {
                try {
                    int before = countUserOnlineTokens(userId);
                    // 保留本次登录新签发的令牌，只踢旧设备
                    deleteUserAllTokens(userId, newToken, newRefreshToken);
                    log.info("[SINGLE][userId={}] 单点登录踢出旧设备: 踢出数量={}, 新token={}...(已保留)",
                            userId, before, newTokenPrefix);
                } catch (Exception e) {
                    log.warn("[SINGLE] 异步踢出旧设备失败: userId={}", userId, e);
                }
            });
        }
 // 限制 模式：在 检查online限制 已检查，达到上限直接拒绝，不踢旧设备
 // 多个 模式：不限制
    }

    /**
     * 注销其它相同账号
     *
     * @param generation 用户信息
     */
    private void unRegisterSingle(String generation) {
        String uid = generation.substring(0, 128);
        unRegisterUid(uid);
    }

    /**
     * 删除用户的所有 令牌 和 refresh令牌
     * <p>Token格式：oauth:token:AT{uid}{randomHex}</p>
     * <p>RefreshToken格式：oauth:refresh:RT{uid}{randomHex}</p>
     *
     * @param uid 用户唯一标识
     */
    @SuppressWarnings("ALL")
    private void unRegisterUid(String uid) {
 // 删除所有 令牌（格式：OAuth:令牌:AT{uid}*）
        Set<String> tokenKeys = scanKeys(TOKEN_PRE + "AT" + uid + "*");
        if (!tokenKeys.isEmpty()) {
            try {
                stringRedisTemplate.delete(tokenKeys);
            } catch (Throwable ignored) {
            }
        }
        
 // 删除所有 refresh令牌（格式：OAuth:refresh:RT{uid}*）
        Set<String> refreshKeys = scanKeys(TOKEN_REFRESH_PRE + "RT" + uid + "*");
        if (!refreshKeys.isEmpty()) {
            try {
                stringRedisTemplate.delete(refreshKeys);
            } catch (Throwable ignored) {
            }
        }
        
        // 兼容旧格式（无AT/RT前缀）
        Set<String> oldTokenKeys = scanKeys(TOKEN_PRE + uid + "*");
        if (!oldTokenKeys.isEmpty()) {
            try {
                stringRedisTemplate.delete(oldTokenKeys);
            } catch (Throwable ignored) {
            }
        }
        Set<String> oldRefreshKeys = scanKeys(TOKEN_REFRESH_PRE + uid + "*");
        if (!oldRefreshKeys.isEmpty()) {
            try {
                stringRedisTemplate.delete(oldRefreshKeys);
            } catch (Throwable ignored) {
            }
        }
    }

    /**
     * logoutrefresh
     *
     * @param token 令牌
     */
    private void logoutRefresh(String token) {
        stringRedisTemplate.delete(TOKEN_REFRESH_PRE + token);
    }

    @Override
    @SuppressWarnings("ALL")
    /**
     * Logout
     *
     * @param token 令牌
     */
    public void logout(String token) {
        long startTime = System.currentTimeMillis();
        // 清除本地缓存
        if (token != null) {
            TOKEN_RESOLVE_CACHE.invalidate(token);
        }
        String tokenRedisKey = TOKEN_PRE + token;
        Object s = stringRedisTemplate.opsForValue().get(tokenRedisKey);
        UserResult userResult = null;
        if (s instanceof Map map) {
            userResult = BeanUtils.copyProperties((Object) map, UserResult.class);
        } else if (s instanceof UserResult ur) {
            userResult = ur;
        } else if (s != null) {
            userResult = BeanUtils.copyProperties(s, UserResult.class);
        }

 // 无论 用户结果 是否为 空，都删除 令牌（防止僵尸 令牌）
        stringRedisTemplate.delete(tokenRedisKey);

        if (null != userResult) {
            String loginType = StringUtils.isNotBlank(userResult.getLoginType()) ? userResult.getLoginType() : DEFAULT_LOGIN_TYPE;
            long cost = System.currentTimeMillis() - startTime;
            log.info("[LOGOUT][userId={}] 登出: user={}, type={}, token={}...",
                    userResult.getUserId(), userResult.getUsername(), loginType,
                    token.length() > 8 ? token.substring(0, 8) : token);

            recordLogoutLog(userResult, loginType, LogoutType.LOGOUT, cost);

            if (tokenBlacklistService != null) {
                long expire = userResult.getExpireTime() != null ? userResult.getExpireTime() : DEFAULT_EXPIRE_SECONDS;
                tokenBlacklistService.addToBlacklist(token, TokenBlacklistService.BlacklistReason.LOGOUT,
                        expire, userResult.getUserId(), userResult.getUsername());
            }

            String refreshToken = userResult.getRefreshToken();
            if (StringUtils.isNotEmpty(refreshToken)) {
                logoutRefresh(refreshToken);
                log.debug("[LOGOUT][userId={}] refreshToken 已删除", userResult.getUserId());
            }
        } else {
            log.warn("[LOGOUT] token 不存在或已过期: token={}...",
                    token.length() > 8 ? token.substring(0, 8) : token);
        }
    }

    @Override
    @SuppressWarnings("ALL")
    /**
     * Logout
     *
     * @param uid uid
     * @param type 类型
     */
    public void logout(String uid, LogoutType type) {
        if (StringUtils.isBlank(uid)) {
            return;
        }
        // 账号禁用/启用：uid = 用户 id。必须优先于令牌相关分支处理，
        // 否则一个纯数字用户 id 会被当成「不存在的令牌」而静默成功。
        if (type == LogoutType.DISABLE) {
            disableUser(uid);
            return;
        }
        if (type == LogoutType.ENABLE) {
            enableUser(uid);
            return;
        }
        // 改密后踢掉其它设备：uid = 当前令牌，保留它、吊销同用户的其它会话
        if (type == LogoutType.KICK_OTHERS) {
            kickOtherSessions(uid);
            return;
        }
        if (type == LogoutType.KICK) {
            // 兼容两种历史语义：老调用方传令牌，新调用方传用户 id
            if (TokenBuilder.isAccessToken(uid) || TokenBuilder.isRefreshToken(uid)) {
                kickByToken(uid);
            } else {
                kickAllSessions(uid);
            }
            return;
        }

        if (type == LogoutType.UN_REGISTER || type == LogoutType.LOGOUT_ALL) {
 // uid 实际上是 令牌，优先从 令牌 结构中提取 用户id哈希（即使 令牌 已从 Redis 删除也能工作）
            String userIdHash = TokenBuilder.extractUserIdHash(uid);
            String realUserId = uid;

 // 尝试从 Redis 获取真实 用户id（用于日志记录）
            Object s = stringRedisTemplate.opsForValue().get(TOKEN_PRE + uid);
            if (s != null) {
                UserResult ur = null;
                if (s instanceof Map map) {
                    ur = BeanUtils.copyProperties((Object) map, UserResult.class);
                } else if (s instanceof UserResult u) {
                    ur = u;
                } else {
                    ur = BeanUtils.copyProperties(s, UserResult.class);
                }
                if (ur != null && StringUtils.isNotBlank(ur.getUserId())) {
                    realUserId = ur.getUserId();
                }
            }
            recordLogoutLogByUid(realUserId, DEFAULT_LOGIN_TYPE, type);

 // 优先用 用户id哈希 直接删除（不依赖 Redis 中的 令牌 是否存在）
            if (userIdHash != null) {
                deleteUserAllTokensByHash(userIdHash);
            } else {
                deleteUserAllTokens(realUserId);
            }
            // resolve 的本地缓存按令牌键存放：只删 Redis 不清这里，
            // 本实例 15 秒内仍会把已注销的令牌判为「认证通过」
            TOKEN_RESOLVE_CACHE.invalidateAll();
            return;
        }

        if (type == LogoutType.LOGOUT) {
 // 单个登出：uid 实际上是 令牌
            logout(uid);
        }
    }

    /**
     * 禁用账号：写入用户级黑名单并吊销其全部会话。
     *
     * <p>黑名单会被 {@code resolve} 与 {@code upgradeForRefresh} 读取，
     * 因此被禁用账号即使还持有旧 refreshToken 也无法续出新令牌。
     * 删除令牌是「立即失效」的主力，黑名单是「新令牌也签不出来」的兜底。</p>
     *
     * @param userId 用户标识（sys_user 主键）
     */
    private void disableUser(String userId) {
        if (tokenBlacklistService != null) {
            tokenBlacklistService.addUserToBlacklist(userId, "账号被禁用");
        }
        kickAllSessions(userId);
        log.info("[DISABLE][userId={}] 已加入用户黑名单并吊销全部会话", userId);
    }

    /**
     * 启用账号：移出用户级黑名单。
     *
     * @param userId 用户标识（sys_user 主键）
     */
    private void enableUser(String userId) {
        if (tokenBlacklistService != null) {
            tokenBlacklistService.removeUserFromBlacklist(userId);
        }
        log.info("[ENABLE][userId={}] 已移出用户黑名单", userId);
    }

    /**
     * 按用户标识吊销全部会话。
     *
     * <p>令牌格式为 {@code AT{md5(userId)}{uidHash}{random}}，因此拿到用户 id
     * 就能用 SCAN 前缀定位该用户的全部 access/refresh 令牌，不需要先持有其中任意一枚。</p>
     *
     * @param userId 用户标识（sys_user 主键）
     */
    private void kickAllSessions(String userId) {
        deleteUserAllTokens(userId);
        // resolve 的本地缓存按令牌键存放，删除令牌后必须整体失效，
        // 否则 15 秒内 /verify 仍会返回缓存里的「认证通过」。
        TOKEN_RESOLVE_CACHE.invalidateAll();
        recordLogoutLogByUid(userId, DEFAULT_LOGIN_TYPE, LogoutType.KICK);
        log.info("[KICK][userId={}] 已吊销该用户全部会话", userId);
    }

    /**
     * 踢掉除当前令牌外的其它会话（改密场景）。
     *
     * @param keepToken 需要保留的当前 access 令牌
     */
    private void kickOtherSessions(String keepToken) {
        Object s = stringRedisTemplate.opsForValue().get(TOKEN_PRE + keepToken);
        UserResult userResult = toUserResult(s);
        if (userResult == null || StringUtils.isBlank(userResult.getUserId())) {
            log.warn("[KICK_OTHERS] 当前令牌不存在，跳过: token={}",
                    keepToken.length() > 8 ? keepToken.substring(0, 8) : keepToken);
            return;
        }
        TOKEN_RESOLVE_CACHE.invalidateAll();
        deleteUserAllTokens(userResult.getUserId(), keepToken, userResult.getRefreshToken());
        recordLogoutLogByUid(userResult.getUserId(), DEFAULT_LOGIN_TYPE, LogoutType.KICK_OTHERS);
        log.info("[KICK_OTHERS][userId={}] 已吊销其它设备会话，保留当前令牌", userResult.getUserId());
    }

    /**
     * 把 Redis 中的令牌记录转换为 {@link UserResult}。
     *
     * @param value Redis 读出的原始值
     * @return 用户结果，无法解析时返回 {@code null}
     */
    private UserResult toUserResult(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof UserResult userResult) {
            return userResult;
        }
        if (value instanceof Map<?, ?> map) {
            return BeanUtils.copyProperties((Object) map, UserResult.class);
        }
        return BeanUtils.copyProperties(value, UserResult.class);
    }
    
    /**
     * 踢下线（与 logout 类似，但记录的是 KICK 类型）
     *
     * @param token 访问令牌
     */
    @SuppressWarnings("ALL")
    private void kickByToken(String token) {
        // 清除本地缓存
        if (token != null) {
            TOKEN_RESOLVE_CACHE.invalidate(token);
        }
        String tokenRedisKey = TOKEN_PRE + token;
        Object s = stringRedisTemplate.opsForValue().get(tokenRedisKey);
        UserResult userResult = null;
        if (s instanceof Map map) {
            userResult = BeanUtils.copyProperties((Object) map, UserResult.class);
        } else if (s instanceof UserResult ur) {
            userResult = ur;
        } else if (s != null) {
            userResult = BeanUtils.copyProperties(s, UserResult.class);
        }
        
 // 无论 用户结果 是否为 空，都删除 令牌
        stringRedisTemplate.delete(tokenRedisKey);
        
        if (null != userResult) {
            // 记录踢下线日志
            String loginType = StringUtils.isNotBlank(userResult.getLoginType()) ? userResult.getLoginType() : DEFAULT_LOGIN_TYPE;
            recordLogoutLog(userResult, loginType, LogoutType.KICK);

 // 将 令牌 加入黑名单
            if (tokenBlacklistService != null) {
                long expire = userResult.getExpireTime() != null ? userResult.getExpireTime() : DEFAULT_EXPIRE_SECONDS;
                tokenBlacklistService.addToBlacklist(token, TokenBlacklistService.BlacklistReason.KICKED, 
                        expire, userResult.getUserId(), userResult.getUsername());
            }
            
 // 删除 refresh令牌
            String refreshToken = userResult.getRefreshToken();
            if (StringUtils.isNotEmpty(refreshToken)) {
                logoutRefresh(refreshToken);
            }
        }
    }

    /**
     * 根据 令牌 登出当前设备
     * <p>删除指定的 Token 和对应的 RefreshToken</p>
     *
     * @param token 访问令牌
     */
    public void logoutByToken(String token) {
        if (StringUtils.isBlank(token)) {
            return;
        }
        logout(token);
    }

    /**
     * 登出用户所有设备
     * <p>删除该用户的所有 Token 和 RefreshToken</p>
     *
     * @param uid 用户唯一标识
     */
    public void logoutAll(String uid) {
        if (StringUtils.isBlank(uid)) {
            return;
        }
        recordLogoutLogByUid(uid, DEFAULT_LOGIN_TYPE, LogoutType.LOGOUT_ALL);
        unRegisterUid(uid);
    }

    @Override
    /**
     * 解析
    */
    public ReturnResult<UserResume> resolve(String cookie, String token) {
        String cv = token;
        if (StringUtils.isEmpty(cv)) {
            cv = cookie;
        }
        if (cv == null) {
            return ReturnResult.error("未认证");
        }

        // 检查本地缓存,防止缓存击穿(加锁同步)
        ReturnResult<UserResume> cached = null;
        synchronized (TOKEN_RESOLVE_CACHE) {
            cached = TOKEN_RESOLVE_CACHE.getIfPresent(cv);
        }
        if (cached != null) {
            return cached;
        }

        if (tokenBlacklistService == null) {
            return ReturnResult.error("黑名单服务不可用");
        }

 // 检查 令牌 是否在黑名单中
        if (tokenBlacklistService.isBlacklisted(cv)) {
            return ReturnResult.error("Token已失效");
        }

        String redisKey = TOKEN_PRE + cv;
        Object s = stringRedisTemplate.opsForValue().get(redisKey);
        if (s == null) {
            return ReturnResult.error("未认证");
        }
        UserResult userResult;
        if (s instanceof UserResult ur) {
            userResult = ur;
        } else if (s instanceof Map map) {
 // 兼容旧格式:部分 Redis 序列化配置可能反序列化为 映射
            userResult = BeanUtils.copyProperties((Object) map, UserResult.class);
        } else {
            log.warn("Token数据格式异常: key={}, type={}", redisKey, s.getClass().getName());
            return ReturnResult.error("Token数据异常");
        }

        // 检查用户是否被全局拉黑
        if (userResult != null && tokenBlacklistService.isUserBlacklisted(userResult.getUserId())) {
            return ReturnResult.error("用户已被禁用");
        }

        if (authServerProperties.isRenew()) {
            // 必须传 cv：cookie 认证时 token 参数为 null，传 token 会让续期静默失效
            resetExpire(userResult, cv);
        }

        ReturnResult<UserResume> result = ReturnResult.ok(userResult);
        synchronized (TOKEN_RESOLVE_CACHE) {
            TOKEN_RESOLVE_CACHE.put(cv, result);
        }
        return result;
    }

    @Override
    /**
     * upgradefor时间戳
    */
    public ReturnResult<UserResume> upgradeForTimestamp(String token, String refreshToken) {
        // 清除本地缓存，强制下次 resolve 从 Redis 重新读取
        if (token != null) {
            TOKEN_RESOLVE_CACHE.invalidate(token);
        }
        String redisKey = TOKEN_PRE + token;
        Object s = stringRedisTemplate.opsForValue().get(redisKey);
        if (null == s) {
            return ReturnResult.error("未认证");
        }

        UserResult userResult = BeanUtils.copyProperties(s, UserResult.class);
 // 时间戳 升级：无条件重置过期时间（续期），不刷新用户信息
        long expire = calculateExpireTime(userResult);
        userResult.setExpireTime(expire);
        stringRedisTemplate.opsForValue().set(redisKey, userResult);
        if (expire > 0) {
            stringRedisTemplate.expire(redisKey, Duration.ofSeconds(expire));
        }
        log.info("[Upgrade][TIMESTAMP] token续期成功: expire={}s, token={}...", expire,
                token.length() > 8 ? token.substring(0, 8) : token);
        return ReturnResult.ok(userResult);
    }

    @Override
    /**
     * upgradefor版本
    */
    public ReturnResult<UserResume> upgradeForVersion(String token, String refreshToken) {
        // 清除本地缓存，强制下次 resolve 从 Redis 重新读取
        if (token != null) {
            TOKEN_RESOLVE_CACHE.invalidate(token);
        }
        String redisKey = TOKEN_PRE + token;
        Object s = stringRedisTemplate.opsForValue().get(redisKey);
        if (null == s) {
            return ReturnResult.error("未认证");
        }

        UserResult userResult = BeanUtils.copyProperties(s, UserResult.class);
        // 被禁用账号的会话即使还在缓存里也不允许再升级
        if (tokenBlacklistService != null && tokenBlacklistService.isUserBlacklisted(userResult.getUserId())) {
            return ReturnResult.error("账号已被禁用");
        }
        String currentAuthVersion = resolveAuthVersion(userResult);
        String latestAuthVersion = loginCheck.getAuthVersion(userResult);
        if (StringUtils.isNotBlank(currentAuthVersion)
                && StringUtils.isNotBlank(latestAuthVersion)
                && currentAuthVersion.equals(latestAuthVersion)) {
            userResult.setToken(token);
            if (StringUtils.isBlank(userResult.getRefreshToken())) {
                userResult.setRefreshToken(refreshToken);
            }
            log.info("[Upgrade][VERSION] 用户态未变化，复用Redis缓存: userId={}, token={}...",
                    userResult.getUserId(),
                    token.length() > 8 ? token.substring(0, 8) : token);
            return ReturnResult.ok(userResult);
        }

        UserResume newUserResult = loginCheck.getUserInfo(userResult);
        if (null == newUserResult) {
            // 认证服务不可用/异常：保留旧会话，优先保证可用性
            userResult.setToken(token);
            userResult.setRefreshToken(refreshToken);
            return ReturnResult.ok(userResult);
        }
        if (StringUtils.isNotBlank(newUserResult.getMessage()) || null == newUserResult.getUserId()) {
            // 账号被禁用/删除时，UserInfoService.upgrade 返回的是「带 message、userId 为空」的结果，
            // 早期这里当作临时失败回落到旧 userResult，等于让被禁用的会话继续有效。
            String message = StringUtils.isNotBlank(newUserResult.getMessage())
                    ? newUserResult.getMessage() : "账号状态异常，请重新登录";
            log.warn("[Upgrade][VERSION] 拒绝已失效账号: userId={}, message={}", userResult.getUserId(), message);
            return ReturnResult.error(message);
        }

 // 确保 令牌 字段被设置（无论 新用户结果 是 用户结果 还是 用户resume 子类）
        if (newUserResult instanceof UserResult userResult1) {
            userResult1.setToken(token);
 // 保留原有 refresh令牌（不使用传入的参数，因为调用方可能传的是 AT）
            if (StringUtils.isBlank(userResult1.getRefreshToken())) {
                userResult1.setRefreshToken(userResult.getRefreshToken());
            }
        } else {
 // 非 用户结果 类型：将新用户信息合并到原 用户结果 中，保留 令牌
            org.springframework.beans.BeanUtils.copyProperties(newUserResult, userResult);
            userResult.setToken(token);
 // 保留原有 refresh令牌
            newUserResult = userResult;
        }

        String currentRefreshToken = userResult.getRefreshToken();
        if (newUserResult instanceof UserResult refreshedUserResult) {
            sanitizeExt(refreshedUserResult);
            if (StringUtils.isNotBlank(refreshedUserResult.getRefreshToken())) {
                currentRefreshToken = refreshedUserResult.getRefreshToken();
            }
        }
        stringRedisTemplate.opsForValue().set(redisKey, newUserResult);
        if (StringUtils.isNotBlank(currentRefreshToken)) {
            String refreshRedisKey = TOKEN_REFRESH_PRE + currentRefreshToken;
            Long refreshTtl = stringRedisTemplate.getExpire(refreshRedisKey);
            if (refreshTtl != null && refreshTtl > 0) {
                stringRedisTemplate.opsForValue().set(refreshRedisKey, newUserResult, Duration.ofSeconds(refreshTtl));
            } else {
                stringRedisTemplate.opsForValue().set(refreshRedisKey, newUserResult);
            }
        }
        log.info("[Upgrade][VERSION] 用户信息已刷新: userId={}, token={}...", newUserResult.getUserId(),
                token.length() > 8 ? token.substring(0, 8) : token);
        return ReturnResult.ok(newUserResult);
    }

    /**
     * 解析认证版本
     *
     * @param userResume 用户resume
     * @return resolve认证版本的结果
     */
    private String resolveAuthVersion(UserResume userResume) {
        if (userResume == null || userResume.getExt() == null) {
            return null;
        }
        Object authVersion = userResume.getExt().get(AUTH_VERSION);
        return authVersion == null ? null : String.valueOf(authVersion);
    }

    @Override
    /**
     * upgradeforrefresh
    */
    public ReturnResult<UserResume> upgradeForRefresh(String token, String refreshToken) {
        // 清除本地缓存（新旧两个都应失效）
        if (token != null) {
            TOKEN_RESOLVE_CACHE.invalidate(token);
        }
 // 验证 refresh令牌
        String redisRefreshKey = TOKEN_REFRESH_PRE + refreshToken;
        Object s = stringRedisTemplate.opsForValue().get(redisRefreshKey);
        if (null == s) {
            return ReturnResult.error("未认证");
        }
        
        UserResult oldUserResult;
        if (s instanceof UserResult ur) {
            oldUserResult = ur;
        } else if (s instanceof java.util.Map map) {
            oldUserResult = BeanUtils.copyProperties((Object) map, UserResult.class);
        } else {
            oldUserResult = BeanUtils.copyProperties(s, UserResult.class);
        }
        if (oldUserResult == null || StringUtils.isBlank(oldUserResult.getUserId())) {
            return ReturnResult.error("未认证");
        }
        // 账号被禁用后，旧 refreshToken 不得再续出新 accessToken
        if (tokenBlacklistService != null && tokenBlacklistService.isUserBlacklisted(oldUserResult.getUserId())) {
            return ReturnResult.error("账号已被禁用");
        }

 // 使用 令牌构建器 生成新的 access令牌（保持相同的 uid哈希）
        String userIdHash = TokenBuilder.extractUserIdHash(refreshToken);
        String uidHash = TokenBuilder.extractUidHash(refreshToken);
        if (userIdHash == null || uidHash == null) {
            return ReturnResult.error("未认证");
        }
        
        String newToken = TokenBuilder.generateAccessToken(oldUserResult.getUserId(), uidHash);

 // 将旧 access令牌 加入黑名单并删除
 // 令牌 参数可能为 空（客户端 refresh 接口不传旧 AT），此时从 refresh令牌 存储的 用户结果 中获取
        String oldToken = StringUtils.isNotBlank(token) ? token : oldUserResult.getToken();
        if (StringUtils.isNotBlank(oldToken)) {
            if (tokenBlacklistService != null) {
                tokenBlacklistService.addToBlacklist(oldToken, TokenBlacklistService.BlacklistReason.REVOKED);
            }
            Boolean deleted = stringRedisTemplate.delete(TOKEN_PRE + oldToken);
            log.debug("[OAuth] 旧 AccessToken 已删除: {}..., deleted={}", oldToken.length() > 8 ? oldToken.substring(0, 8) : oldToken, deleted);
        }

        // 计算过期时间
        long expire = calculateExpireTime(oldUserResult);
        long refreshExpire = calculateRefreshExpireTime(oldUserResult);

 // 判断是否轮换 refresh令牌
        boolean rotation = authServerProperties != null && authServerProperties.isRefreshTokenRotation();
        String finalRefreshToken;
        if (rotation) {
 // 轮换：生成新 refresh令牌，旧的加入黑名单并删除
            String newRefreshToken = TokenBuilder.generateRefreshToken(oldUserResult.getUserId(), uidHash);
            if (tokenBlacklistService != null) {
                tokenBlacklistService.addToBlacklist(refreshToken, TokenBlacklistService.BlacklistReason.REVOKED);
            }
            stringRedisTemplate.delete(redisRefreshKey);
            finalRefreshToken = newRefreshToken;
 // 存储新 refresh令牌
            String newRefreshRedisKey = TOKEN_REFRESH_PRE + newRefreshToken;
            oldUserResult.setRefreshToken(newRefreshToken);
            oldUserResult.setRefreshExpireTime(refreshExpire);
            stringRedisTemplate.opsForValue().set(newRefreshRedisKey, oldUserResult);
            if (refreshExpire > 0) {
                stringRedisTemplate.expire(newRefreshRedisKey, Duration.ofSeconds(refreshExpire));
            }
            log.debug("[OAuth] refreshToken 轮换，旧={}, 新={}", refreshToken, newRefreshToken);
        } else {
 // 不轮换：旧 refresh令牌 继续有效，只更新其中存储的 令牌 引用
            finalRefreshToken = refreshToken;
            oldUserResult.setRefreshToken(refreshToken);
            oldUserResult.setRefreshExpireTime(refreshExpire);
 // 更新 refresh令牌 对应的用户信息（令牌 字段指向新 access令牌）
            stringRedisTemplate.opsForValue().set(redisRefreshKey, oldUserResult);
 // refresh令牌 过期时间不变（不重置）
            log.debug("[OAuth] refreshToken 不轮换，refreshToken={} 继续有效", refreshToken);
        }

 // 更新用户信息并存储新 access令牌
        oldUserResult.setToken(newToken);
        oldUserResult.setExpireTime(expire);

        String newRedisKey = TOKEN_PRE + newToken;
        stringRedisTemplate.opsForValue().set(newRedisKey, oldUserResult);
        if (expire > 0) {
            stringRedisTemplate.expire(newRedisKey, Duration.ofSeconds(expire));
        }

        return ReturnResult.ok(oldUserResult, oldUserResult.getLoginType());
    }

    @Override
    /**
     * Logout
    */
    public void logout(Cookie[] cookies, String token, String refreshToken, String cookieName) {
        Cookie cookie = CookieUtil.getCookie(cookies, cookieName);
        if (null != cookie) {
            logout(cookie.getValue());
        }

        if (StringUtils.isNotBlank(token)) {
            logout(token);
        }

        if (StringUtils.isNotBlank(refreshToken)) {
            logoutRefresh(refreshToken);
        }
    }

    @Override
    /**
     * 解析用户编码
    */
    public ReturnResult<UserResume> resolveUserCode(String userCode) {
        if (StringUtils.isBlank(userCode)) {
            return ReturnResult.error("用户代码不能为空");
        }

 // 新格式：{uid}:{加密的用户id}
        String[] split = userCode.split(":");
        if (split.length < 2) {
            return ReturnResult.error("用户代码格式无效");
        }
        
        UserResult userResult = new UserResult();
        userResult.setUserCode(userCode);
        userResult.setUid(split[0]);
        
        try {
            String decryptedUserId = new com.chua.starter.common.support.crypto.AesCodec(authServerProperties.getServiceKey().getBytes())
                    .decodeHex(split[1]);
            userResult.setUserId(decryptedUserId);
        } catch (Exception e) {
            return ReturnResult.error("用户代码解析失败");
        }
        
        UserResume userResume = loginCheck.getUserInfo(userResult);
        if (null == userResume || null == userResume.getUserId()) {
            return ReturnResult.error("用户不存在");
        }

        return ReturnResult.ok(userResume);
    }


    /**
     * 令牌续费
     *
     * @param userResume 令牌信息
     * @param token      令牌
     */
    @SuppressWarnings("unchecked")
    private void resetExpire(UserResume userResume, String token) {
        long expire = userResume.getExpireTime() == null ? authServerProperties.getExpire() : userResume.getExpireTime();
        userResume.setExpireTime(expire);
        if (expire > 0) {
            // 令牌在 Redis 中的键是 oauth:token:{token}，早期这里漏了 TOKEN_PRE 前缀，
            // 续期永远打在一个不存在的键上，表现为「renew=true 也不续期」
            String redisKey = TOKEN_PRE + token;
            stringRedisTemplate.expire(redisKey, Duration.ofSeconds(expire));
            // 同步刷新记录里的 expireTime，避免 /verify 返回的过期时间与实际 TTL 不一致
            Object stored = stringRedisTemplate.opsForValue().get(redisKey);
            UserResult userResult = toUserResult(stored);
            if (userResult != null) {
                userResult.setExpireTime(expire);
                stringRedisTemplate.opsForValue().set(redisKey, userResult, Duration.ofSeconds(expire));
            }
        }
    }

    /**
     * 记录登出日志
     *
     * @param userResult 用户结果对象
     * @param loginType  登录类型
     * @param logoutType 登出类型
     * @param cost       耗时（毫秒），-1 表示未知
     */
    private void recordLogoutLog(UserResult userResult, String loginType, LogoutType logoutType, long cost) {
        if (userResult == null || sysUserLogService == null) {
            return;
        }

        try {
            SysUserLog userLog = new SysUserLog();
            userLog.setSysLogUsername(userResult.getUsername());
            userLog.setSysLogIp(userResult.getAddress());
            userLog.setSysLogLoginType(loginType);
            userLog.setSysLogFrom("LOGOUT");
            userLog.setSysLogStatus(1);
            userLog.setSysLogUrl("/logout");
            userLog.setSysLogMessage(logoutType.name());
            if (cost >= 0) {
                userLog.setSysLogCost(java.math.BigDecimal.valueOf(cost));
            }
            if (userResult.getUserId() != null) {
                try {
                    userLog.setCreateBy(Integer.parseInt(userResult.getUserId()));
                } catch (NumberFormatException ignored) {
                }
            }

            // 从 ext 中补充浏览器/系统信息
            Map<String, Object> ext = userResult.getExt();
            if (ext != null) {
                Object createBy = ext.get("sysUserId");
                if (createBy != null) {
                    try {
                        userLog.setCreateBy(Integer.parseInt(createBy.toString()));
                    } catch (NumberFormatException ignored) {
                    }
                }
                Object sysDeptId = ext.get("sysDeptId");
                if (sysDeptId != null) {
                    try {
                        userLog.setSysDeptId(Integer.parseInt(sysDeptId.toString()));
                    } catch (NumberFormatException ignored) {
                    }
                }
                Object ua = ext.get("ua");
                if (ua != null) {
                    userLog.setSysLogUa(ua.toString());
                    try {
                        com.chua.starter.common.support.network.UserAgent.UserAgentInfo userAgent =
                                com.chua.starter.common.support.network.UserAgent.parseUserAgentString(ua.toString());
                        userLog.setSysLogBrowser(userAgent.getBrowser().toString());
                        userLog.setSysLogSystem(userAgent.getOperatingSystem().getName());
                    } catch (Exception ignored) {}
                }
                Object fingerprint = ext.get("fingerprint");
                if (fingerprint != null) {
                    userLog.setSysLogFingerprint(fingerprint.toString());
                }
            }

            Map<String, Object> param = new java.util.LinkedHashMap<>();
            param.put("logoutType", logoutType.name());
            param.put("userId", userResult.getUserId());
            userLog.setSysLogParam(Json.toJson(param));

            sysUserLogService.asyncSaveUserLog(userLog);
        } catch (Exception e) {
            log.error("[LOGOUT] 记录日志失败: user={}, error={}", userResult.getUsername(), e.getMessage());
        }
    }

    /**
     * 记录登出日志（不含耗时，兼容旧调用）
     * @param userResult 用户结果
     * @param loginType login类型
     * @param logoutType logout类型
     */
    private void recordLogoutLog(UserResult userResult, String loginType, LogoutType logoutType) {
        recordLogoutLog(userResult, loginType, logoutType, -1);
    }

    /**
     * 根据用户标识记录登出日志（LOGOUT_全部 / UN_注册 场景）
     * <p>uid 此时是真实的 userId（已在调用方从 token 解析出来）</p>
     *
     * @param uid        用户标识（数据库主键）
     * @param loginType  登录类型
     * @param logoutType 登出类型
     */
    private void recordLogoutLogByUid(String uid, String loginType, LogoutType logoutType) {
        if (StringUtils.isEmpty(uid) || sysUserLogService == null) {
            return;
        }

        try {
 // 尝试从 Redis 中找到该用户的任意一个 令牌，获取真实 用户名
            String username = null;
            String address = null;
            Map<String, Object> ext = null;
            try {
                Set<String> tokenKeys = scanKeys(TOKEN_PRE + TokenBuilder.buildAccessTokenPattern(uid));
                for (String tokenKey : tokenKeys) {
                    Object val = stringRedisTemplate.opsForValue().get(tokenKey);
                    if (val != null) {
                        UserResult ur = BeanUtils.copyProperties(val, UserResult.class);
                        if (ur != null) {
                            username = ur.getUsername();
                            address = ur.getAddress();
                            ext = ur.getExt();
                            break;
                        }
                    }
                }
            } catch (Exception ignored) {}

            SysUserLog userLog = new SysUserLog();
            userLog.setSysLogUsername(username != null ? username : uid);
            userLog.setSysLogIp(address);
            userLog.setSysLogLoginType(loginType);
            userLog.setSysLogFrom("LOGOUT");
            userLog.setSysLogStatus(1);
            userLog.setSysLogUrl("/logout");
            try {
                userLog.setCreateBy(Integer.parseInt(uid));
            } catch (NumberFormatException ignored) {
            }

            // 从 ext 补充浏览器/系统信息
            if (ext != null) {
                Object sysDeptId = ext.get("sysDeptId");
                if (sysDeptId != null) {
                    try {
                        userLog.setSysDeptId(Integer.parseInt(sysDeptId.toString()));
                    } catch (NumberFormatException ignored) {
                    }
                }
                Object ua = ext.get("ua");
                if (ua != null) {
                    userLog.setSysLogUa(ua.toString());
                    try {
                        com.chua.starter.common.support.network.UserAgent.UserAgentInfo userAgent =
                                com.chua.starter.common.support.network.UserAgent.parseUserAgentString(ua.toString());
                        userLog.setSysLogBrowser(userAgent.getBrowser().toString());
                        userLog.setSysLogSystem(userAgent.getOperatingSystem().getName());
                    } catch (Exception ignored) {}
                }
                Object fingerprint = ext.get("fingerprint");
                if (fingerprint != null) {
                    userLog.setSysLogFingerprint(fingerprint.toString());
                }
            }

            Map<String, Object> param = new java.util.LinkedHashMap<>();
            param.put("logoutType", logoutType.name());
            param.put("userId", uid);
            userLog.setSysLogParam(Json.toJson(param));

            sysUserLogService.asyncSaveUserLog(userLog);
        } catch (Exception e) {
            log.error("记录登出日志失败, uid={}, error={}", uid, e.getMessage());
        }
    }

    // ==================== 在线状态查询 ====================

    /**
     * 查询用户在线状态
     * <p>注意：参数是 userId（数据库主键），不是 uid</p>
     *
     * @param userId 用户标识（数据库主键）
     * @return 在线状态信息
     */
    @Override
    public OnlineStatus getOnlineStatus(String userId) {
        if (StringUtils.isBlank(userId)) {
            return new OnlineStatus();
        }

 // 查询当前在线数量（基于 用户id）
        int onlineCount = countUserOnlineTokens(userId);

        // 获取全局配置的在线模式和最大数量
        AuthServerProperties.Online onlineMode = authServerProperties.getOnline();
        int maxOnlineCount;

        switch (onlineMode) {
            case SINGLE:
                maxOnlineCount = 1;
                break;
            case LIMIT:
                maxOnlineCount = authServerProperties.getMaxOnlineCount();
                break;
            case MULTIPLE:
            default:
                maxOnlineCount = -1; // 不限制
                break;
        }

        return new OnlineStatus(onlineCount, maxOnlineCount, onlineMode.name());
    }

    // ==================== 在线用户列表查询 ====================

    /**
     * 获取在线用户列表
     * <p>扫描Redis中所有有效的Token，返回在线用户信息</p>
     *
     * @param query 查询参数
     * @return 在线用户列表结果
     */
    @Override
    public OnlineUserResult getOnlineUsers(OnlineUserQuery query) {
        if (query == null) {
            query = new OnlineUserQuery();
        }

        try {
 // 使用扫描扫描所有令牌（避免键命令阻塞Redis）
            Set<String> allTokenKeys = scanKeys(TOKEN_PRE + "*");
            if (allTokenKeys.isEmpty()) {
                return OnlineUserResult.empty();
            }

            java.util.List<OnlineUserInfo> allUsers = new java.util.ArrayList<>();
            
            int loaded = 0;
            for (String tokenKey : allTokenKeys) {
                if (loaded >= ONLINE_USER_SCAN_LIMIT) {
                    log.warn("[在线用户] 令牌数超过 {}，本次结果已截断；建议改为维护 userId→token 索引",
                            ONLINE_USER_SCAN_LIMIT);
                    break;
                }
                loaded++;
                try {
                    Object value = stringRedisTemplate.opsForValue().get(tokenKey);
                    if (value == null) {
                        continue;
                    }

                    UserResult userResult = BeanUtils.copyProperties(value, UserResult.class);
                    if (userResult == null || StringUtils.isBlank(userResult.getUserId())) {
                        continue;
                    }

                    // 应用用户名过滤
                    if (StringUtils.isNotBlank(query.getUsername())) {
                        String username = userResult.getUsername();
                        if (username == null || !username.toLowerCase().contains(query.getUsername().toLowerCase())) {
                            continue;
                        }
                    }

                    // 应用IP过滤
                    if (StringUtils.isNotBlank(query.getIp())) {
                        String address = userResult.getAddress();
                        if (address == null || !address.contains(query.getIp())) {
                            continue;
                        }
                    }

                    // 构建在线用户信息
                    OnlineUserInfo info = new OnlineUserInfo();
                    info.setUserId(userResult.getUserId());
                    info.setUsername(userResult.getUsername());
                    info.setNickname(userResult.getNickName());
                    info.setLoginIp(userResult.getAddress());
                    // 登录地址需要通过IP查询服务获取，暂留空
                    info.setLoginAddress(null);
                    
                    // 从ext中获取浏览器和操作系统信息
                    Map<String, Object> ext = userResult.getExt();
                    if (ext != null) {
                        info.setBrowser(getStringFromMap(ext, "browser"));
                        info.setOs(getStringFromMap(ext, "os"));
                        Object loginTimeObj = ext.get("loginTime");
                        if (loginTimeObj instanceof Number) {
                            info.setLoginTime(((Number) loginTimeObj).longValue());
                        }
                    }
                    
 // 从令牌 键中提取令牌
                    String token = tokenKey.substring(TOKEN_PRE.length());
                    info.setToken(token);
                    info.setLoginType(userResult.getLoginType());

                    allUsers.add(info);
                } catch (Exception e) {
 // 忽略单个令牌解析失败
                }
            }

            // 分页处理
            int total = allUsers.size();
            int page = Math.max(1, query.getPage());
            int size = Math.max(1, Math.min(100, query.getSize()));
            int start = (page - 1) * size;
            int end = Math.min(start + size, total);

            java.util.List<OnlineUserInfo> pagedUsers;
            if (start >= total) {
                pagedUsers = java.util.Collections.emptyList();
            } else {
                pagedUsers = allUsers.subList(start, end);
            }

            return new OnlineUserResult(pagedUsers, total, page, size);
        } catch (Exception e) {
            return OnlineUserResult.empty();
        }
    }

    /**
     * 强制踢出用户
     *
     * @param token 用户令牌
     * @return 是否成功
     */
    @Override
    public boolean kickUser(String token) {
        if (StringUtils.isBlank(token)) {
            return false;
        }

        try {
            String tokenRedisKey = TOKEN_PRE + token;
            Object value = stringRedisTemplate.opsForValue().get(tokenRedisKey);
            if (value == null) {
                return false;
            }

            UserResult userResult = BeanUtils.copyProperties(value, UserResult.class);
            if (userResult != null) {
 // 将令牌加入黑名单
                if (tokenBlacklistService != null) {
                    long expire = userResult.getExpireTime() != null ? userResult.getExpireTime() : DEFAULT_EXPIRE_SECONDS;
                    tokenBlacklistService.addToBlacklist(token, TokenBlacklistService.BlacklistReason.KICKED,
                            expire, userResult.getUserId(), userResult.getUsername());
                }

 // 删除令牌
                stringRedisTemplate.delete(tokenRedisKey);

 // 删除refresh令牌
                String refreshToken = userResult.getRefreshToken();
                if (StringUtils.isNotEmpty(refreshToken)) {
                    stringRedisTemplate.delete(TOKEN_REFRESH_PRE + refreshToken);
                }

                // 记录登出日志
                String loginType = StringUtils.isNotBlank(userResult.getLoginType()) ? userResult.getLoginType() : "WEB";
                recordLogoutLog(userResult, loginType, LogoutType.LOGOUT);

                return true;
            }
        } catch (Exception e) {
            // 踢出失败
        }
        return false;
    }

    /**
     * 获取字符串从映射
     *
     * @param map 映射
     * @param key 键
     * @return 获取字符串从映射的结果
     */
    private String getStringFromMap(Map<String, Object> map, String key) {
        if (map == null) {
            return null;
        }
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 使用 扫描 命令扫描 Redis 键
     * <p>
     * 扫描 命令是渐进式迭代，不会像 键 命令一样阻塞 Redis 服务器
     * </p>
     *
     * @param pattern 匹配模式，支持通配符 * 和 ?
     * @return 匹配的键集合
     */
    private Set<String> scanKeys(String pattern) {
        Set<String> keys = new HashSet<>();
        try {
            ScanOptions options = ScanOptions.scanOptions()
                    .match(pattern)
                    .count(SCAN_COUNT)
                    .build();
            
            try (Cursor<String> cursor = stringRedisTemplate.scan(options)) {
                while (cursor.hasNext()) {
                    keys.add(cursor.next());
                }
            }
        } catch (Exception e) {
            log.error("Redis SCAN 失败, pattern={}, error={}", pattern, e.getMessage());
        }
        return keys;
    }
}
