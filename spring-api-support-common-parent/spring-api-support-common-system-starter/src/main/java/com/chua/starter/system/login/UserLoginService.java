package com.chua.starter.system.login;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.system.util.PasswordUtils;
import com.chua.starter.oauth.client.support.annotation.LoginType;
import com.chua.starter.oauth.client.support.user.OpenIdGenerator;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.system.constant.SystemConstant;
import com.chua.starter.entity.system.SysUser;
import com.chua.common.support.spi.annotations.Spi;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


/**
 * 用户信息鉴权服务
 * <p>基于用户名/密码的标准Web登录验证服务</p>
 *
 * @since 1.0.0
 * @author CH
 */
@Slf4j
@Service
@Spi("WEB")
@LoginType("WEB")
@Tag(name = "用户登录服务", description = "标准Web用户名密码登录验证")
public class UserLoginService extends AbstractUserLoginService {

    /**
     * 登录类型前缀
     */
    private static final String LOGIN_TYPE_PREFIX = "system";

    @Override
    /**
     * 获取login类型前缀
    */
    protected String getLoginTypePrefix() {
        return LOGIN_TYPE_PREFIX;
    }

    @Override
    /**
     * generate用户标识
    */
    protected String[] generateUserIds(SysUser user) {
        String userId = String.valueOf(user.getSysUserId());
        return OpenIdGenerator.generateSystemIds("web", userId);
    }

    @Override
    /**
     * 校验Login
    */
    public UserResume checkLogin(String principal, String credential, String address, Object ext) {
        String redisKey = LOGIN_TYPE_PREFIX + ":login_error:" + principal;
        long startTime = System.currentTimeMillis();

        // 检查账户是否已被锁定
        UserResume lockedResult = checkAccountLocked(redisKey);
        if (lockedResult != null) {
            log.warn("账户已被锁定: {}, IP: {}", principal, address);
            return lockedResult;
        }

        log.debug("[UserLoginService] 开始查询用户: {}", principal);
        // 查询用户信息 - 支持用户名或手机号登录
        SysUser user = sysUserMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .select(SysUser::getSysUserId, SysUser::getSysUserUsername, SysUser::getSysUserPassword,
                        SysUser::getSysUserSalt, SysUser::getSysUserStatus, SysUser::getSysDeptId,
                        SysUser::getSysUserLastIp, SysUser::getSysUserPhone, SysUser::getSysUserNickname)
                .eq(StringUtils.isNotEmpty(principal), SysUser::getSysUserUsername, principal)
                .or()
                .eq(StringUtils.isNotEmpty(principal), SysUser::getSysUserPhone, principal)
                 ,false);

        log.debug("[UserLoginService] 用户查询耗时: {}ms, 结果: {}", System.currentTimeMillis() - startTime, user != null ? "找到" : "未找到");

        if (null == user) {
            log.info("登录失败: 用户不存在 {}, IP: {}", principal, address);
            return handleLoginFailure(redisKey, false);
        }

        // 检查账户状态
        if (SystemConstant.USER_STATUS_ENABLED != user.getSysUserStatus()) {
            log.info("登录失败: 账户已禁用 {}, IP: {}", principal, address);
            return new UserResume("账号已被禁用");
        }

        // 验证密码
        if (!PasswordUtils.matches(credential, user.getSysUserSalt(), user.getSysUserPassword())) {
            log.info("登录失败: 密码错误 {}, IP: {}", principal, address);
            return handleLoginFailure(redisKey, true);
        }

        // 登录成功，清除错误计数
        clearLoginErrorCount(redisKey);

        log.info("登录成功: {} (ID:{}), IP: {}, 总耗时: {}ms",
                user.getSysUserUsername(), user.getSysUserId(), address,
                System.currentTimeMillis() - startTime);

        return loginInfo(principal, user, address);
    }

    @Override
    /**
     * Upgrade
    */
    public UserResume upgrade(UserResult userResult) {
        long startTime = System.currentTimeMillis();
        SysUser user = sysUserMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .select(SysUser::getSysUserId, SysUser::getSysUserUsername, SysUser::getSysUserStatus,
                        SysUser::getSysDeptId, SysUser::getSysUserPhone, SysUser::getSysUserNickname)
                .eq(SysUser::getSysUserId, userResult.getUserId())
                , false);

        if (null == user) {
            log.warn("用户信息刷新失败: 用户不存在 ID={}", userResult.getUserId());
            return new UserResume("账号/密码不正确");
        }

        if (SystemConstant.USER_STATUS_ENABLED != user.getSysUserStatus()) {
            log.warn("用户信息刷新失败: 账户已禁用 ID={}", userResult.getUserId());
            return new UserResume("账号已被禁用");
        }

        UserResume updatedResult = loginInfo(userResult.getUsername(), user, null);
        updatedResult.setLoginType(userResult.getLoginType());
        log.debug("用户信息刷新成功: {} (ID:{}), 耗时: {}ms",
                user.getSysUserUsername(), user.getSysUserId(),
                System.currentTimeMillis() - startTime);

        return updatedResult;
    }
}
