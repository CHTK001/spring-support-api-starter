package com.chua.starter.system.login;

import com.chua.common.support.utils.DigestUtils;
import com.chua.common.support.utils.MapUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.oauth.client.support.annotation.LoginType;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.system.constant.SystemConstant;
import com.chua.starter.entity.system.SysUser;
import com.chua.common.support.annotations.Spi;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.chua.starter.oauth.client.support.contants.AuthConstant.LOGIN_ERROR;

/**
 * 租户用户登录服务
 * <p>支持多租户环境下的用户登录验证与授权</p>
 *
 * @author CH
 * @since 1.0.0
 */
@Slf4j
@Service
@Spi("tenant")
@LoginType("tenant")
@Tag(name = "租户用户登录", description = "多租户环境下的用户登录验证")
public class TenantUserLoginService extends AbstractUserLoginService {

    /**
     * 登录类型前缀
     */
    private static final String LOGIN_TYPE_PREFIX = "tenant";

    /**
     * 用户上次登录租户编码缓存前缀
     */
    private static final String LAST_TENANT_CODE_KEY = "user:last_tenant_code:";

    /**
     * 上次登录租户缓存过期时间（天）
     */
    private static final long LAST_TENANT_EXPIRE_DAYS = 30;

    @Override
    protected String getLoginTypePrefix() {
        return LOGIN_TYPE_PREFIX;
    }

    @Override
    protected String[] generateUserIds(SysUser user) {
        String baseId = user.getSysUserId() + String.valueOf(user.getSysClientId());
        return new String[]{
                DigestUtils.md5Hex(baseId) + "SYSTEM",
                DigestUtils.md5Hex(baseId)
        };
    }

    @Override
    protected void customizeUserResult(UserResume result, SysUser user) {
        // 设置租户ID
        if (user.getSysTenantId() != null) {
            result.setTenantId(String.valueOf(user.getSysTenantId()));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public UserResume checkLogin(String principal, String credential, String address, Object ext) {
        String redisKey = LOGIN_TYPE_PREFIX + ":" + LOGIN_ERROR + principal;

        Map<String, Object> extMap = (Map<String, Object>) ext;
        String tenantCode = MapUtils.getString(extMap, "tenantCode");
        String tenantId = MapUtils.getString(extMap, "tenantId");

        // 如果未传入租户编码，尝试使用上次登录的租户
        if (StringUtils.isEmpty(tenantCode) && StringUtils.isEmpty(tenantId)) {
            tenantCode = getLastTenantCode(principal);
            log.debug("自动选择租户: 用户={}, 租户编码={}", principal, tenantCode);
        }

        log.debug("租户登录请求: 用户={}, 租户编码={}, 租户ID={}, IP={}", principal, tenantCode, tenantId, address);

        // 检查账户是否已被锁定
        UserResume lockedResult = checkAccountLocked(redisKey);
        if (lockedResult != null) {
            log.warn("租户账户已被锁定: 用户={}, 租户编码={}", principal, tenantCode);
            return lockedResult;
        }

        // 查询用户（优先使用租户编码）
        long startTime = System.currentTimeMillis();
        SysUser user;
        if (StringUtils.isNotEmpty(tenantCode)) {
            user = sysUserMapper.selectByTenantCode(principal, tenantCode);
        } else {
            user = sysUserMapper.selectOne(principal, tenantId);
        }
        log.debug("租户用户查询耗时: {}ms", System.currentTimeMillis() - startTime);

        if (null == user || null == user.getSysTenantId()) {
            log.warn("租户登录失败: 用户不存在 用户={}, 租户编码={}", principal, tenantCode);
            return handleLoginFailure(redisKey, false);
        }

        if (SystemConstant.USER_STATUS_ENABLED != user.getSysUserStatus()) {
            log.warn("租户登录失败: 账户已禁用 用户={}, 租户编码={}", principal, tenantCode);
            return new UserResume("账号已被禁用");
        }

        String salt = user.getSysUserSalt();
        String md5String = DigestUtils.md5Hex(credential + salt);
        if (!StringUtils.safeEquals(md5String, user.getSysUserPassword())) {
            log.warn("租户登录失败: 密码错误 用户={}, 租户编码={}", principal, tenantCode);
            return handleLoginFailure(redisKey, true);
        }

        // 登录成功，清除错误计数
        clearLoginErrorCount(redisKey);

        log.info("租户登录成功: 用户={} (ID:{}), 租户编码={}, IP={}",
                user.getSysUserUsername(), user.getSysUserId(), tenantCode, address);

        // 保存上次登录的租户编码
        saveLastTenantCode(principal, tenantCode);

        return loginInfo(principal, user, address);
    }

    @Override
    public UserResume upgrade(UserResult userResult) {
        SysUser user = sysUserMapper.selectById(userResult.getUserId());
        if (null == user) {
            return new UserResume("账号/密码不正确");
        }

        if (SystemConstant.USER_STATUS_ENABLED != user.getSysUserStatus()) {
            return new UserResume("账号已被禁用");
        }

        UserResume result = loginInfo(userResult.getUsername(), user, null);
        result.setLoginType(userResult.getLoginType());
        return result;
    }

    /**
     * 获取用户上次登录的租户编码
     *
     * @param username 用户名
     * @return 租户编码
     */
    private String getLastTenantCode(String username) {
        try {
            String cachedTenantCode = stringRedisTemplate.opsForValue().get(LAST_TENANT_CODE_KEY + username);
            if (StringUtils.isNotEmpty(cachedTenantCode)) {
                log.debug("从缓存获取用户上次登录租户编码: 用户={}, 租户编码={}", username, cachedTenantCode);
                return cachedTenantCode;
            }
        } catch (Exception e) {
            log.warn("获取用户上次登录租户编码缓存失败: 用户={}, 错误={}", username, e.getMessage());
        }

        try {
            String latestTenantCode = sysUserMapper.selectLatestTenantCodeByUsername(username);
            if (StringUtils.isNotEmpty(latestTenantCode)) {
                log.debug("从数据库获取用户最新租户编码: 用户={}, 租户编码={}", username, latestTenantCode);
                return latestTenantCode;
            }
        } catch (Exception e) {
            log.warn("查询用户最新租户编码失败: 用户={}, 错误={}", username, e.getMessage());
        }

        return null;
    }

    /**
     * 保存用户上次登录的租户编码
     *
     * @param username   用户名
     * @param tenantCode 租户编码
     */
    private void saveLastTenantCode(String username, String tenantCode) {
        try {
            if (StringUtils.isNotEmpty(tenantCode)) {
                stringRedisTemplate.opsForValue().set(
                        LAST_TENANT_CODE_KEY + username,
                        tenantCode,
                        LAST_TENANT_EXPIRE_DAYS,
                        TimeUnit.DAYS
                );
                log.debug("保存用户上次登录租户编码: 用户={}, 租户编码={}", username, tenantCode);
            }
        } catch (Exception e) {
            log.warn("保存用户上次登录租户编码失败: 用户={}, 租户编码={}, 错误={}", username, tenantCode, e.getMessage());
        }
    }
}
