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
            tokenGeneration = ServiceProvider.of(TokenGeneration.class).getExtension(authServerProperties.getTokenGeneration());
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
            deleteUserAllTokens(userResult.getUserId());
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
        String tokenRedisKey = TOKEN_PRE + token;
        Object s = stringRedisTemplate.opsForValue().get(tokenRedisKey);
        UserResult userResult = BeanUtils.copyProperties(s, UserResult.class);
        if (null != userResult) {
            // 记录登出日志（登录类型从 UserResult 获取）
            String loginType = StringUtils.isNotBlank(userResult.getLoginType()) ? userResult.getLoginType() : DEFAULT_LOGIN_TYPE;
            recordLogoutLog(userResult, loginType, LogoutType.LOGOUT);

            // 将 Token 加入黑名单
            if (tokenBlacklistService != null) {
                long expire = userResult.getExpireTime() != null ? userResult.getExpireTime() : DEFAULT_EXPIRE_SECONDS;
                tokenBlacklistService.addToBlacklist(token, TokenBlacklistService.BlacklistReason.LOGOUT, 
                        expire, userResult.getUserId(), userResult.getUsername());
            }

            // 删除 Token
            stringRedisTemplate.delete(TOKEN_PRE + token);
            
            // 删除 RefreshToken
            String refreshToken = userResult.getRefreshToken();
            if (StringUtils.isNotEmpty(refreshToken)) {
                logoutRefresh(refreshToken);
            }
        }
    }

    @Override
    @SuppressWarnings("ALL")
    public void logout(String uid, LogoutType type) {
        if (type == LogoutType.UN_REGISTER || type == LogoutType.LOGOUT_ALL) {
            // 全部登出：删除该用户的所有 Token 和 RefreshToken
            recordLogoutLogByUid(uid, DEFAULT_LOGIN_TYPE, type);
            unRegisterUid(uid);
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
        UserResult userResult = BeanUtils.copyProperties(s, UserResult.class);
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

            // 删除 Token
            stringRedisTemplate.delete(TOKEN_PRE + token);
            
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
        ReturnResult<UserResume> userResultReturnResult = upgradeForVersion(token, token);
        if (!userResultReturnResult.isOk()) {
            return userResultReturnResult;
        }

        if (authServerProperties.isRenew()) {
            resetExpire(userResultReturnResult.getData(), token);
        }
        return ReturnResult.ok(userResultReturnResult.getData());
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
            return ReturnResult.ok(userResult);
        }

        if (newUserResult instanceof UserResult userResult1) {
            userResult1.setToken(token);
            userResult1.setRefreshToken(refreshToken);
        }

        stringRedisTemplate.opsForValue().set(redisKey, newUserResult);
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

        // 使用 TokenBuilder 生成新的 Token（保持相同的 uidHash）
        String userIdHash = TokenBuilder.extractUserIdHash(refreshToken);
        String uidHash = TokenBuilder.extractUidHash(refreshToken);
        if (userIdHash == null || uidHash == null) {
            return ReturnResult.noAuth();
        }
        
        String newToken = TokenBuilder.generateAccessToken(oldUserResult.getUserId(), uidHash);
        String newRefreshToken = TokenBuilder.generateRefreshToken(oldUserResult.getUserId(), uidHash);

        // 将旧 Token 加入黑名单并删除
        if (StringUtils.isNotBlank(token)) {
            if (tokenBlacklistService != null) {
                tokenBlacklistService.addToBlacklist(token, TokenBlacklistService.BlacklistReason.REVOKED);
            }
            stringRedisTemplate.delete(TOKEN_PRE + token);
        }
        // 将旧 RefreshToken 加入黑名单并删除
        if (tokenBlacklistService != null) {
            tokenBlacklistService.addToBlacklist(refreshToken, TokenBlacklistService.BlacklistReason.REVOKED);
        }
        stringRedisTemplate.delete(redisRefreshKey);

        // 计算过期时间
        long expire = calculateExpireTime(oldUserResult);
        long refreshExpire = calculateRefreshExpireTime(oldUserResult);

        // 更新用户信息
        oldUserResult.setToken(newToken);
        oldUserResult.setRefreshToken(newRefreshToken);
        oldUserResult.setExpireTime(expire);
        oldUserResult.setRefreshExpireTime(refreshExpire);

        // 存储新 Token
        String newRedisKey = TOKEN_PRE + newToken;
        String newRefreshRedisKey = TOKEN_REFRESH_PRE + newRefreshToken;
        stringRedisTemplate.opsForValue().set(newRedisKey, oldUserResult);
        stringRedisTemplate.opsForValue().set(newRefreshRedisKey, oldUserResult);
        
        if (expire > 0) {
            stringRedisTemplate.expire(newRedisKey, Duration.ofSeconds(expire));
            stringRedisTemplate.expire(newRefreshRedisKey, Duration.ofSeconds(refreshExpire));
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
     */
    private void recordLogoutLog(UserResult userResult, String loginType, LogoutType logoutType) {
        if (userResult == null || sysUserLogService == null) {
            return;
        }

        try {
            SysUserLog userLog = new SysUserLog();
            userLog.setSysLogUsername(userResult.getUsername());
            userLog.setSysLogIp(userResult.getAddress());
            userLog.setSysLogLoginType(loginType);
            userLog.setSysLogFrom("LOGOUT");
            userLog.setSysLogStatus(1); // 登出默认成功
            userLog.setSysLogUrl("/logout");

            // 设置登出类型信息
            Map<String, Object> ext = Map.of(
                "logoutType", logoutType.name(),
                "userId", userResult.getUserId()
            );
            userLog.setSysLogParam(Json.toJson(ext));

            sysUserLogService.asyncSaveUserLog(userLog);
        } catch (Exception e) {
            // 日志记录失败不应影响登出流程，只记录错误
            log.error("记录登出日志失败, username={}, error={}", userResult.getUsername(), e.getMessage());
        }
    }

    /**
     * 根据用户ID记录登出日志
     *
     * @param uid        用户ID
     * @param loginType  登录类型
     * @param logoutType 登出类型
     */
    private void recordLogoutLogByUid(String uid, String loginType, LogoutType logoutType) {
        if (StringUtils.isEmpty(uid) || sysUserLogService == null) {
            return;
        }

        try {
            SysUserLog userLog = new SysUserLog();
            userLog.setSysLogUsername("UID:" + uid);
            userLog.setSysLogLoginType(loginType);
            userLog.setSysLogFrom("LOGOUT");
            userLog.setSysLogStatus(1); // 登出默认成功
            userLog.setSysLogUrl("/logout");

            // 设置登出类型信息
            Map<String, Object> ext = Map.of(
                "logoutType", logoutType.name(),
                "userId", uid
            );
            userLog.setSysLogParam(Json.toJson(ext));

            sysUserLogService.asyncSaveUserLog(userLog);
        } catch (Exception e) {
            // 日志记录失败不应影响登出流程，只记录错误
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
