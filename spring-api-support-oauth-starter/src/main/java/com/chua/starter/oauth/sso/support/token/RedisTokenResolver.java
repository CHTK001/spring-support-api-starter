package com.chua.starter.oauth.sso.support.token;

import com.chua.common.support.core.annotation.Extension;
import com.chua.common.support.base.bean.BeanUtils;
import com.chua.common.support.text.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.objects.annotation.AutoInject;
import com.chua.common.support.core.spi.ServiceProvider;
import com.chua.common.support.core.utils.IdUtils;
import com.chua.common.support.core.utils.StringUtils;
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
import jakarta.servlet.http.Cookie;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ValueOperations;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.HashSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.chua.starter.oauth.client.support.contants.AuthConstant.TOKEN_PRE;
import static com.chua.starter.oauth.client.support.contants.AuthConstant.TOKEN_REFRESH_PRE;

/**
 * redis token管理器
 *
 * @author CH
 */
@Slf4j
@com.chua.common.support.core.annotation.Spi("redis")
@SuppressWarnings("ALL")
public class RedisTokenResolver implements TokenResolver {

    /**
     * 默认Token过期时间（秒），1天
     */
    private static final long DEFAULT_EXPIRE_SECONDS = 86400L;

    /**
     * Redis SCAN 每次扫描返回的数量
     */
    private static final int SCAN_COUNT = 1000;

    /**
     * 默认登录类型
     */
    private static final String DEFAULT_LOGIN_TYPE = "WEB";

    @AutoInject
    private RedisTemplate<String, Object> stringRedisTemplate;

    @AutoInject
    private AuthServerProperties authServerProperties;
    @AutoInject
    private LoginCheck loginCheck;
    @AutoInject
    private SysUserLogService sysUserLogService;
    @AutoInject
    private TokenBlacklistService tokenBlacklistService;

    @Override
    public ReturnResult<LoginResult> createToken(String address, UserResult userResult, String authType) {
        String userId = userResult.getUserId();
        
        // 检查在线上限（基于 userId，不区分登录方式）
        ReturnResult<Void> checkResult = checkOnlineLimit(userResult);
        if (!checkResult.isOk()) {
            return ReturnResult.error(checkResult.getMsg());
        }

        // 根据authType选择TokenGeneration：jwt走jwtTokenGeneration，其他走配置的TokenGeneration
        TokenGeneration tokenGeneration;
        if ("jwt".equalsIgnoreCase(authType) || "JWT".equalsIgnoreCase(authType)) {
            tokenGeneration = ServiceProvider.of(TokenGeneration.class).getExtension("jwt");
        } else {
            // 配置为空时回退到默认实现，避免空指针导致登录直接失败
            String generationName = "default";
            if (null != authServerProperties && StringUtils.isNotBlank(authServerProperties.getTokenGeneration())) {
                generationName = authServerProperties.getTokenGeneration();
            }
            tokenGeneration = ServiceProvider.of(TokenGeneration.class).getExtension(generationName);
        }
        if (tokenGeneration == null) {
            return ReturnResult.error("Token生成器不可用");
        }
        userResult.setAddress(address);
        String uid = userResult.getUid();

        // 使用 TokenBuilder 生成 Token 和 RefreshToken
        String uidHash = tokenGeneration.generation(uid);
        String token = TokenBuilder.generateAccessToken(userId, uidHash);
        String refreshToken = TokenBuilder.generateRefreshToken(userId, uidHash);

        String redisKey = TOKEN_PRE + token;
        String redisRefreshKey = TOKEN_REFRESH_PRE + refreshToken;

        // 处理在线控制（SINGLE模式踢掉旧设备）
        handleOnlineControl(userResult, token);

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

        // 存储 Token
        ValueOperations<String, Object> forValue = stringRedisTemplate.opsForValue();
        forValue.set(redisKey, userResult);
        forValue.set(redisRefreshKey, userResult);

        // 设置过期时间
        if (expire > 0) {
            stringRedisTemplate.expire(redisKey, Duration.ofSeconds(expire));
            stringRedisTemplate.expire(redisRefreshKey, Duration.ofSeconds(refreshExpire));
        }

        LoginResult loginResult = new LoginResult(token);
        loginResult.setRefreshToken(refreshToken);
        // 设置浏览器指纹到返回结果
        loginResult.setFingerprint(userResult.getFingerprint());

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
     * 检查在线上限
     * <p>基于 userId 统计在线数量，不区分登录方式</p>
     *
     * @param userResult 用户信息
     * @return 检查结果，失败时返回错误信息
     */
    private ReturnResult<Void> checkOnlineLimit(UserResult userResult) {
        AuthServerProperties.Online onlineMode = getOnlineMode(userResult);

        // MULTIPLE 模式不限制
        if (onlineMode == AuthServerProperties.Online.MULTIPLE) {
            return ReturnResult.ok();
        }

        // SINGLE 模式允许登录（会踢掉旧设备）
        if (onlineMode == AuthServerProperties.Online.SINGLE) {
            return ReturnResult.ok();
        }

        // LIMIT 模式检查上限
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
     * 获取在线模式（用户配置 > 全局配置）
     */
    private AuthServerProperties.Online getOnlineMode(UserResult userResult) {
        String userOnlineMode = userResult.getOption("onlineMode");
        if (StringUtils.isNotBlank(userOnlineMode)) {
            try {
                return AuthServerProperties.Online.valueOf(userOnlineMode.toUpperCase());
            } catch (Exception ignored) {
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
     */
    private int getMaxOnlineCount(UserResult userResult) {
        String maxOnlineCount = userResult.getOption("maxOnlineCount");
        if (StringUtils.isNotBlank(maxOnlineCount)) {
            try {
                int count = Integer.parseInt(maxOnlineCount);
                if (count > 0) {
                    return count;
                }
            } catch (Exception ignored) {
            }
        }
        // 未注入配置时，回退到默认的在线数量限制（与 AuthServerProperties 默认值保持一致）
        if (null == authServerProperties) {
            return 3;
        }
        return authServerProperties.getMaxOnlineCount();
    }

    /**
     * 统计用户当前在线Token数量
     * <p>基于 userId 统计，不区分登录方式</p>
     *
     * @param userId 用户ID
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
     * 删除用户所有Token（基于 userIdHash，不依赖 userId，即使 token 已从 Redis 删除也能工作）
     *
     * @param userIdHash userIdHash（从 token 中提取的32位 MD5）
     */
    private void deleteUserAllTokensByHash(String userIdHash) {
        if (StringUtils.isBlank(userIdHash)) {
            return;
        }
        // 删除所有 AccessToken
        Set<String> tokenKeys = scanKeys(TOKEN_PRE + TokenBuilder.buildAccessTokenPatternByHash(userIdHash));
        if (!tokenKeys.isEmpty()) {
            stringRedisTemplate.delete(tokenKeys);
        }
        // 删除所有 RefreshToken
        Set<String> refreshKeys = scanKeys(TOKEN_REFRESH_PRE + TokenBuilder.buildRefreshTokenPatternByHash(userIdHash));
        if (!refreshKeys.isEmpty()) {
            stringRedisTemplate.delete(refreshKeys);
        }
    }

    /**
     * 删除用户所有Token（基于 userId，不区分登录方式）
     *
     * @param userId 用户ID
     */
    private void deleteUserAllTokens(String userId) {
        if (StringUtils.isBlank(userId)) {
            return;
        }
        // 删除所有 AccessToken
        Set<String> tokenKeys = scanKeys(TOKEN_PRE + TokenBuilder.buildAccessTokenPattern(userId));
        if (!tokenKeys.isEmpty()) {
            stringRedisTemplate.delete(tokenKeys);
        }
        // 删除所有 RefreshToken
        Set<String> refreshKeys = scanKeys(TOKEN_REFRESH_PRE + TokenBuilder.buildRefreshTokenPattern(userId));
        if (!refreshKeys.isEmpty()) {
            stringRedisTemplate.delete(refreshKeys);
        }
    }

    /**
     * 计算 Token 过期时间
     * <p>优先级：用户配置 > 全局配置</p>
     *
     * @param userResult 用户信息
     * @return 过期时间（秒）
     */
    private long calculateExpireTime(UserResult userResult) {
        // 用户级配置优先
        String tokenExpire = userResult.getOption("tokenExpire");
        if (StringUtils.isNotBlank(tokenExpire)) {
            try {
                long expire = Long.parseLong(tokenExpire);
                if (expire > 0) {
                    return expire;
                }
            } catch (Exception ignored) {
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
     * 计算 RefreshToken 过期时间
     * <p>优先级：用户配置 > 全局配置</p>
     *
     * @param userResult 用户信息
     * @return 过期时间（秒）
     */
    private long calculateRefreshExpireTime(UserResult userResult) {
        // 用户级配置优先
        String refreshTokenExpire = userResult.getOption("refreshTokenExpire");
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
     * 将 LocalDateTime、LocalDate 等类型转换为字符串格式，确保 Redis 序列化正常
     * </p>
     *
     * @param userResult 用户信息
     * @author CH
     * @since 1.0.0
     */
    private void sanitizeExt(UserResult userResult) {
        Map<String, Object> ext = userResult.getExt();
        if (ext == null || ext.isEmpty()) {
            return;
        }
        
        Map<String, Object> sanitized = new HashMap<>(ext.size());
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (Map.Entry<String, Object> entry : ext.entrySet()) {
            Object value = entry.getValue();
            if (value == null) {
                sanitized.put(entry.getKey(), null);
            } else if (value instanceof LocalDateTime localDateTime) {
                // LocalDateTime 转为字符串
                sanitized.put(entry.getKey(), localDateTime.format(dateTimeFormatter));
            } else if (value instanceof LocalDate localDate) {
                // LocalDate 转为字符串
                sanitized.put(entry.getKey(), localDate.format(dateFormatter));
            } else if (value instanceof Date date) {
                // Date 转为时间戳
                sanitized.put(entry.getKey(), date.getTime());
            } else if (value instanceof String || value instanceof Number || value instanceof Boolean) {
                // 基本类型直接保留
                sanitized.put(entry.getKey(), value);
            } else {
                // 其他类型尝试转为字符串
                try {
                    sanitized.put(entry.getKey(), value.toString());
                } catch (Exception e) {
                    // 转换失败则跳过
                }
            }
        }
        
        userResult.setExt(sanitized);
    }

    /**
     * 处理在线控制
     * <p>仅 SINGLE 模式需要踢掉旧设备，LIMIT 模式在 checkOnlineLimit 已拒绝</p>
     *
     * @param userResult 用户信息
     * @param newToken   新Token
     */
    private void handleOnlineControl(UserResult userResult, String newToken) {
        AuthServerProperties.Online onlineMode = getOnlineMode(userResult);

        // SINGLE 模式：踢掉该用户所有旧设备
        if (onlineMode == AuthServerProperties.Online.SINGLE) {
            String userId = userResult.getUserId();
            int before = countUserOnlineTokens(userId);
            deleteUserAllTokens(userId);
            log.info("[SINGLE][userId={}] 单点登录踢出旧设备: 踢出数量={}, 新token={}...",
                    userId, before, newToken.length() > 8 ? newToken.substring(0, 8) : newToken);
        }
        // LIMIT 模式：在 checkOnlineLimit 已检查，达到上限直接拒绝，不踢旧设备
        // MULTIPLE 模式：不限制
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
     * 删除用户的所有 Token 和 RefreshToken
     * <p>Token格式：oauth:token:AT{uid}{randomHex}</p>
     * <p>RefreshToken格式：oauth:refresh:RT{uid}{randomHex}</p>
     *
     * @param uid 用户唯一标识
     */
    @SuppressWarnings("ALL")
    private void unRegisterUid(String uid) {
        // 删除所有 Token（格式：oauth:token:AT{uid}*）
        Set<String> tokenKeys = scanKeys(TOKEN_PRE + "AT" + uid + "*");
        if (!tokenKeys.isEmpty()) {
            try {
                stringRedisTemplate.delete(tokenKeys);
            } catch (Throwable ignored) {
            }
        }
        
        // 删除所有 RefreshToken（格式：oauth:refresh:RT{uid}*）
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

    private void logoutRefresh(String token) {
        stringRedisTemplate.delete(TOKEN_REFRESH_PRE + token);
    }

    @Override
    @SuppressWarnings("ALL")
    public void logout(String token) {
        long startTime = System.currentTimeMillis();
        String tokenRedisKey = TOKEN_PRE + token;
        Object s = stringRedisTemplate.opsForValue().get(tokenRedisKey);
        UserResult userResult = null;
        if (s instanceof Map map) {
            userResult = BeanUtils.copyProperties(map, UserResult.class);
        } else if (s instanceof UserResult ur) {
            userResult = ur;
        } else if (s != null) {
            userResult = BeanUtils.copyProperties(s, UserResult.class);
        }

        // 无论 userResult 是否为 null，都删除 token（防止僵尸 token）
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
    public void logout(String uid, LogoutType type) {
        if (type == LogoutType.UN_REGISTER || type == LogoutType.LOGOUT_ALL) {
            // uid 实际上是 token，优先从 token 结构中提取 userIdHash（即使 token 已从 Redis 删除也能工作）
            String userIdHash = TokenBuilder.extractUserIdHash(uid);
            String realUserId = uid;

            // 尝试从 Redis 获取真实 userId（用于日志记录）
            Object s = stringRedisTemplate.opsForValue().get(TOKEN_PRE + uid);
            if (s != null) {
                UserResult ur = null;
                if (s instanceof Map map) {
                    ur = BeanUtils.copyProperties(map, UserResult.class);
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

            // 优先用 userIdHash 直接删除（不依赖 Redis 中的 token 是否存在）
            if (userIdHash != null) {
                deleteUserAllTokensByHash(userIdHash);
            } else {
                deleteUserAllTokens(realUserId);
            }
            return;
        }

        if (type == LogoutType.LOGOUT) {
            // 单个登出：uid 实际上是 token
            logout(uid);
            return;
        }
        
        if (type == LogoutType.KICK) {
            // 踢下线：uid 实际上是 token
            kickByToken(uid);
        }
    }
    
    /**
     * 踢下线（与 logout 类似，但记录的是 KICK 类型）
     *
     * @param token 访问令牌
     */
    @SuppressWarnings("ALL")
    private void kickByToken(String token) {
        String tokenRedisKey = TOKEN_PRE + token;
        Object s = stringRedisTemplate.opsForValue().get(tokenRedisKey);
        UserResult userResult = null;
        if (s instanceof Map map) {
            userResult = BeanUtils.copyProperties(map, UserResult.class);
        } else if (s instanceof UserResult ur) {
            userResult = ur;
        } else if (s != null) {
            userResult = BeanUtils.copyProperties(s, UserResult.class);
        }
        
        // 无论 userResult 是否为 null，都删除 token
        stringRedisTemplate.delete(tokenRedisKey);
        
        if (null != userResult) {
            // 记录踢下线日志
            String loginType = StringUtils.isNotBlank(userResult.getLoginType()) ? userResult.getLoginType() : DEFAULT_LOGIN_TYPE;
            recordLogoutLog(userResult, loginType, LogoutType.KICK);

            // 将 Token 加入黑名单
            if (tokenBlacklistService != null) {
                long expire = userResult.getExpireTime() != null ? userResult.getExpireTime() : DEFAULT_EXPIRE_SECONDS;
                tokenBlacklistService.addToBlacklist(token, TokenBlacklistService.BlacklistReason.KICKED, 
                        expire, userResult.getUserId(), userResult.getUsername());
            }
            
            // 删除 RefreshToken
            String refreshToken = userResult.getRefreshToken();
            if (StringUtils.isNotEmpty(refreshToken)) {
                logoutRefresh(refreshToken);
            }
        }
    }

    /**
     * 根据 Token 登出当前设备
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
    public ReturnResult<UserResume> resolve(String cookie, String token) {
        String cv = token;
        if (StringUtils.isEmpty(StringUtils.ifValid(cv, ""))) {
            cv = null == cookie ? null : cookie;
        }

        if (null == cv) {
            return ReturnResult.noAuth();
        }

        // 检查 Token 是否在黑名单中
        if (tokenBlacklistService != null && tokenBlacklistService.isBlacklisted(cv)) {
            return ReturnResult.noAuth("Token已失效");
        }

        String redisKey = TOKEN_PRE + cv;
        Object s = stringRedisTemplate.opsForValue().get(redisKey);
        if (null == s) {
            return ReturnResult.noAuth();
        }
        UserResult userResult = null;
        if(s instanceof Map map) {
            userResult = BeanUtils.copyProperties(map, UserResult.class);
        } else {
            userResult = (UserResult) s;
        }

        // 检查用户是否被全局拉黑
        if (tokenBlacklistService != null && userResult != null 
                && tokenBlacklistService.isUserBlacklisted(userResult.getUserId())) {
            return ReturnResult.noAuth("用户已被禁用");
        }

        if (authServerProperties.isRenew()) {
            resetExpire(userResult, token);
        }
        return ReturnResult.ok(userResult);
    }

    @Override
    public ReturnResult<UserResume> upgradeForTimestamp(String token, String refreshToken) {
        String redisKey = TOKEN_PRE + token;
        Object s = stringRedisTemplate.opsForValue().get(redisKey);
        if (null == s) {
            return ReturnResult.noAuth();
        }

        UserResult userResult = BeanUtils.copyProperties(s, UserResult.class);
        // TIMESTAMP 升级：无条件重置过期时间（续期），不刷新用户信息
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
    public ReturnResult<UserResume> upgradeForVersion(String token, String refreshToken) {
        String redisKey = TOKEN_PRE + token;
        Object s = stringRedisTemplate.opsForValue().get(redisKey);
        if (null == s) {
            return ReturnResult.noAuth();
        }

        UserResult userResult = BeanUtils.copyProperties(s, UserResult.class);
        UserResume newUserResult = loginCheck.getUserInfo(userResult);
        if (null == newUserResult || null == newUserResult.getUserId()) {
            // 无法获取新用户信息时，返回原有 userResult（保持 token 不变）
            userResult.setToken(token);
            userResult.setRefreshToken(refreshToken);
            return ReturnResult.ok(userResult);
        }

        // 确保 token 字段被设置（无论 newUserResult 是 UserResult 还是 UserResume 子类）
        if (newUserResult instanceof UserResult userResult1) {
            userResult1.setToken(token);
            // 保留原有 refreshToken（不使用传入的参数，因为调用方可能传的是 AT）
            if (StringUtils.isBlank(userResult1.getRefreshToken())) {
                userResult1.setRefreshToken(userResult.getRefreshToken());
            }
        } else {
            // 非 UserResult 类型：将新用户信息合并到原 userResult 中，保留 token
            org.springframework.beans.BeanUtils.copyProperties(newUserResult, userResult);
            userResult.setToken(token);
            // 保留原有 refreshToken
            newUserResult = userResult;
        }

        stringRedisTemplate.opsForValue().set(redisKey, newUserResult);
        log.info("[Upgrade][VERSION] 用户信息已刷新: userId={}, token={}...", newUserResult.getUserId(),
                token.length() > 8 ? token.substring(0, 8) : token);
        return ReturnResult.ok(newUserResult);
    }

    @Override
    public ReturnResult<UserResume> upgradeForRefresh(String token, String refreshToken) {
        // 验证 RefreshToken
        String redisRefreshKey = TOKEN_REFRESH_PRE + refreshToken;
        Object s = stringRedisTemplate.opsForValue().get(redisRefreshKey);
        if (null == s) {
            return ReturnResult.noAuth();
        }
        
        UserResult oldUserResult = BeanUtils.copyProperties(s, UserResult.class);
        if (oldUserResult == null || StringUtils.isBlank(oldUserResult.getUserId())) {
            return ReturnResult.noAuth();
        }

        // 使用 TokenBuilder 生成新的 AccessToken（保持相同的 uidHash）
        String userIdHash = TokenBuilder.extractUserIdHash(refreshToken);
        String uidHash = TokenBuilder.extractUidHash(refreshToken);
        if (userIdHash == null || uidHash == null) {
            return ReturnResult.noAuth();
        }
        
        String newToken = TokenBuilder.generateAccessToken(oldUserResult.getUserId(), uidHash);

        // 将旧 AccessToken 加入黑名单并删除
        // token 参数可能为 null（客户端 refresh 接口不传旧 AT），此时从 refreshToken 存储的 userResult 中获取
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

        // 判断是否轮换 refreshToken
        boolean rotation = authServerProperties != null && authServerProperties.isRefreshTokenRotation();
        String finalRefreshToken;
        if (rotation) {
            // 轮换：生成新 refreshToken，旧的加入黑名单并删除
            String newRefreshToken = TokenBuilder.generateRefreshToken(oldUserResult.getUserId(), uidHash);
            if (tokenBlacklistService != null) {
                tokenBlacklistService.addToBlacklist(refreshToken, TokenBlacklistService.BlacklistReason.REVOKED);
            }
            stringRedisTemplate.delete(redisRefreshKey);
            finalRefreshToken = newRefreshToken;
            // 存储新 RefreshToken
            String newRefreshRedisKey = TOKEN_REFRESH_PRE + newRefreshToken;
            oldUserResult.setRefreshToken(newRefreshToken);
            oldUserResult.setRefreshExpireTime(refreshExpire);
            stringRedisTemplate.opsForValue().set(newRefreshRedisKey, oldUserResult);
            if (refreshExpire > 0) {
                stringRedisTemplate.expire(newRefreshRedisKey, Duration.ofSeconds(refreshExpire));
            }
            log.debug("[OAuth] refreshToken 轮换，旧={}, 新={}", refreshToken, newRefreshToken);
        } else {
            // 不轮换：旧 refreshToken 继续有效，只更新其中存储的 token 引用
            finalRefreshToken = refreshToken;
            oldUserResult.setRefreshToken(refreshToken);
            oldUserResult.setRefreshExpireTime(refreshExpire);
            // 更新 refreshToken 对应的用户信息（token 字段指向新 accessToken）
            stringRedisTemplate.opsForValue().set(redisRefreshKey, oldUserResult);
            // refreshToken 过期时间不变（不重置）
            log.debug("[OAuth] refreshToken 不轮换，refreshToken={} 继续有效", refreshToken);
        }

        // 更新用户信息并存储新 AccessToken
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
    public ReturnResult<UserResume> resolveUserCode(String userCode) {
        if (StringUtils.isBlank(userCode)) {
            return ReturnResult.error("用户代码不能为空");
        }

        // 新格式：{uid}:{加密的userId}
        String[] split = userCode.split(":");
        if (split.length < 2) {
            return ReturnResult.error("用户代码格式无效");
        }
        
        UserResult userResult = new UserResult();
        userResult.setUserCode(userCode);
        userResult.setUid(split[0]);
        
        try {
            String decryptedUserId = com.chua.common.support.core.utils.DigestUtils.aesDecrypt(split[1], authServerProperties.getServiceKey());
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
     * token续费
     *
     * @param userResume token信息
     * @param token      token
     */
    @SuppressWarnings("unchecked")
    private void resetExpire(UserResume userResume, String token) {
        long expire = userResume.getExpireTime() == null ? authServerProperties.getExpire() : userResume.getExpireTime();
        userResume.setExpireTime(expire);
        if (expire > 0) {
            stringRedisTemplate.expire(token, Duration.ofSeconds(expire));
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

            // 从 ext 中补充浏览器/系统信息
            Map<String, Object> ext = userResult.getExt();
            if (ext != null) {
                Object ua = ext.get("ua");
                if (ua != null) {
                    userLog.setSysLogUa(ua.toString());
                    try {
                        com.chua.common.support.network.net.UserAgent userAgent =
                                com.chua.common.support.network.net.UserAgent.parseUserAgentString(ua.toString());
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
     */
    private void recordLogoutLog(UserResult userResult, String loginType, LogoutType logoutType) {
        recordLogoutLog(userResult, loginType, logoutType, -1);
    }

    /**
     * 根据用户ID记录登出日志（LOGOUT_ALL / UN_REGISTER 场景）
     * <p>uid 此时是真实的 userId（已在调用方从 token 解析出来）</p>
     *
     * @param uid        用户ID（数据库主键）
     * @param loginType  登录类型
     * @param logoutType 登出类型
     */
    private void recordLogoutLogByUid(String uid, String loginType, LogoutType logoutType) {
        if (StringUtils.isEmpty(uid) || sysUserLogService == null) {
            return;
        }

        try {
            // 尝试从 Redis 中找到该用户的任意一个 token，获取真实 username
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

            // 从 ext 补充浏览器/系统信息
            if (ext != null) {
                Object ua = ext.get("ua");
                if (ua != null) {
                    userLog.setSysLogUa(ua.toString());
                    try {
                        com.chua.common.support.network.net.UserAgent userAgent =
                                com.chua.common.support.network.net.UserAgent.parseUserAgentString(ua.toString());
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
     * @param userId 用户ID（数据库主键）
     * @return 在线状态信息
     */
    @Override
    public OnlineStatus getOnlineStatus(String userId) {
        if (StringUtils.isBlank(userId)) {
            return new OnlineStatus();
        }

        // 查询当前在线数量（基于 userId）
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
            // 使用SCAN扫描所有Token（避免KEYS命令阻塞Redis）
            Set<String> allTokenKeys = scanKeys(TOKEN_PRE + "*");
            if (allTokenKeys.isEmpty()) {
                return OnlineUserResult.empty();
            }

            java.util.List<OnlineUserInfo> allUsers = new java.util.ArrayList<>();
            
            for (String tokenKey : allTokenKeys) {
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
                    
                    // 从Token key中提取token
                    String token = tokenKey.substring(TOKEN_PRE.length());
                    info.setToken(token);
                    info.setLoginType(userResult.getLoginType());

                    allUsers.add(info);
                } catch (Exception e) {
                    // 忽略单个token解析失败
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
     * @param token 用户token
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
                // 将Token加入黑名单
                if (tokenBlacklistService != null) {
                    long expire = userResult.getExpireTime() != null ? userResult.getExpireTime() : DEFAULT_EXPIRE_SECONDS;
                    tokenBlacklistService.addToBlacklist(token, TokenBlacklistService.BlacklistReason.KICKED,
                            expire, userResult.getUserId(), userResult.getUsername());
                }

                // 删除Token
                stringRedisTemplate.delete(tokenRedisKey);

                // 删除RefreshToken
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

    private String getStringFromMap(Map<String, Object> map, String key) {
        if (map == null) {
            return null;
        }
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 使用 SCAN 命令扫描 Redis 键
     * <p>
     * SCAN 命令是渐进式迭代，不会像 KEYS 命令一样阻塞 Redis 服务器
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
