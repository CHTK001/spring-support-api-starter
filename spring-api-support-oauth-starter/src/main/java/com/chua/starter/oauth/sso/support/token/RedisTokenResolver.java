package com.chua.starter.oauth.sso.support.token;

import com.chua.common.support.annotations.Extension;
import com.chua.common.support.bean.BeanUtils;
import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.net.UserAgent;
import com.chua.common.support.objects.annotation.AutoInject;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.IdUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.utils.CookieUtil;
import com.chua.starter.oauth.client.support.enums.LogoutType;
import com.chua.starter.oauth.client.support.user.LoginResult;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.sso.service.SysUserLogService;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.generation.TokenGeneration;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import com.chua.starter.system.entity.SysUserLog;
import jakarta.servlet.http.Cookie;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

import static com.chua.starter.oauth.client.support.contants.AuthConstant.TOKEN_PRE;
import static com.chua.starter.oauth.client.support.contants.AuthConstant.TOKEN_REFRESH_PRE;

/**
 * redis token管理器
 *
 * @author CH
 */
@Extension("redis")
@SuppressWarnings("ALL")
public class RedisTokenResolver implements TokenResolver {

    @AutoInject
    private RedisTemplate<String, Object> stringRedisTemplate;

    @AutoInject
    private AuthServerProperties authServerProperties;
    @AutoInject
    private LoginCheck loginCheck;
    @AutoInject
    private SysUserLogService sysUserLogService;

    @Override
    public ReturnResult<LoginResult> createToken(String address, UserResult userResult, String authType) {
        TokenGeneration tokenGeneration = ServiceProvider.of(TokenGeneration.class).getExtension(authServerProperties.getTokenGeneration());
        //token
        String token = tokenGeneration.generation(userResult.getUid()) + "0" + DigestUtils.md5Hex(IdUtils.createTid());
        userResult.setAddress(address);
        String uid = userResult.getUid();

//        String serviceKey = PRE_KEY + uid + ":" + generation;
        String redisKey = TOKEN_PRE + token;
        String refreshToken = tokenGeneration.generation(userResult.getUid()) + "0" + DigestUtils.md5Hex(IdUtils.createTid());
        String redisRefreshKey = TOKEN_REFRESH_PRE + refreshToken;

        AuthServerProperties.Online online = authServerProperties.getOnline();
        if (online == AuthServerProperties.Online.SINGLE) {
            unRegisterSingle(token);
        }
        ValueOperations<String, Object> forValue = stringRedisTemplate.opsForValue();
        long expire = userResult.getExpireTime() == null ? authServerProperties.getExpire() : userResult.getExpireTime();
        long refreshExpire = userResult.getExpireTime() == null ? authServerProperties.getRefreshExpire() : userResult.getExpireTime() + 86400L;
        userResult.setExpireTime(expire);
        userResult.setRefreshToken(refreshToken);
        userResult.setRefreshExpireTime(refreshExpire);
        forValue.set(redisKey, userResult);
        forValue.set(redisRefreshKey, userResult);


        if (expire > 0) {
            stringRedisTemplate.expire(redisKey, Duration.ofSeconds(expire));
            stringRedisTemplate.expire(redisRefreshKey, Duration.ofSeconds(refreshExpire));
        }

        LoginResult loginResult = new LoginResult(token);
        loginResult.setRefreshToken(refreshToken);
        return ReturnResult.ok(loginResult);
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
     * 单例模式
     *
     * @param uid 用户信息
     */
    @SuppressWarnings("ALL")
    private void unRegisterUid(String uid) {
        Set<String> keys = stringRedisTemplate.keys(TOKEN_PRE + uid + "*");
        try {
            stringRedisTemplate.delete(keys);
        } catch (Throwable ignored) {
        }
        Set<String> keys1 = stringRedisTemplate.keys(TOKEN_REFRESH_PRE + uid + "*");
        try {
            stringRedisTemplate.delete(keys1);
        } catch (Throwable ignored) {
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
            // 记录登出日志
            recordLogoutLog(userResult, "WEB", LogoutType.LOGOUT);

            stringRedisTemplate.delete(TOKEN_PRE + token);
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
            // 记录注销日志
            recordLogoutLogByUid(uid, "WEB", type);

            TokenGeneration tokenGeneration = ServiceProvider.of(TokenGeneration.class).getExtension(authServerProperties.getTokenGeneration());
            unRegisterSingle(tokenGeneration.generation(uid));
            return;
        }

        if (type == LogoutType.LOGOUT) {
            logout(uid);
        }

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
        cv = TOKEN_PRE + cv;
        Object s = stringRedisTemplate.opsForValue().get(cv);
        if (null == s) {
            return ReturnResult.noAuth();
        }
        UserResult userResult = null;
        if(s instanceof Map map) {
            userResult = BeanUtils.copyProperties(map, UserResult.class);
        } else {
            userResult = (UserResult) s;
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
        if (null == newUserResult) {
            return ReturnResult.ok(userResult);
        }
        stringRedisTemplate.opsForValue().set(redisKey, newUserResult);
        return ReturnResult.ok(newUserResult);
    }

    @Override
    public ReturnResult<UserResume> upgradeForRefresh(String token, String refreshToken) {
        String redisRefreshKey = TOKEN_REFRESH_PRE + refreshToken;
        Object s = stringRedisTemplate.opsForValue().get(redisRefreshKey);
        if (null == s) {
            return ReturnResult.noAuth();
        }
        String redisKey = TOKEN_PRE + token;
        UserResult userResult = BeanUtils.copyProperties(s, UserResult.class);
        return ReturnResult.ok(userResult, userResult.getLoginType());
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
            logoutRefresh(token);
        }
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
            System.err.println("记录登出日志失败: " + e.getMessage());
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
            System.err.println("记录登出日志失败: " + e.getMessage());
        }
    }
}
