package com.chua.starter.system.login;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.utils.DigestUtils;
import com.chua.starter.oauth.client.support.annotation.LoginType;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.entity.system.SysUserThirdParty;
import com.chua.starter.system.constant.SystemConstant;
import com.chua.starter.system.mapper.SysUserThirdPartyMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Google 登录服务
 *
 * @since 2026/03/22
 * @author CH
 */
@Slf4j
@Service
@LoginType("google")
@Tag(name = "Google登录", description = "基于Google OAuth的用户登录验证")
public class GoogleLoginService extends AbstractUserLoginService {

    /** Login_type_prefix */
    private static final String LOGIN_TYPE_PREFIX = "google";

    @Autowired
    /** SYS用户thirdpartymapper */
    private SysUserThirdPartyMapper sysUserThirdPartyMapper;

    @Override
    protected String getLoginTypePrefix() {
        return LOGIN_TYPE_PREFIX;
    }

    @Override
    protected String[] generateUserIds(SysUser user) {
        String baseId = user.getSysUserId() + "GOOGLE";
        return new String[]{
                DigestUtils.md5Hex(baseId) + "GOOGLE",
                DigestUtils.md5Hex(baseId)
        };
    }

    @Override
    public UserResume checkLogin(String googleId, String password, String address, Object ext) {
        log.debug("Google登录请求: googleId={}, IP={}", googleId, address);

        SysUserThirdParty thirdParty = sysUserThirdPartyMapper.selectOne(
                Wrappers.<SysUserThirdParty>lambdaQuery()
                        .eq(SysUserThirdParty::getSysUserThirdPartyUid, googleId)
                        .eq(SysUserThirdParty::getSysUserThirdPartyType, SysUserThirdParty.Type.GOOGLE.name())
                        .last("LIMIT 1")
        );

        if (thirdParty == null || thirdParty.getSysUserId() == null) {
            log.warn("Google登录失败: 未找到关联用户 googleId={}", googleId);
            return new UserResume("账号未绑定，请先绑定Google账号");
        }

        SysUser user = sysUserMapper.selectById(thirdParty.getSysUserId());
        if (user == null) {
            log.warn("Google登录失败: 关联的系统用户不存在 userId={}", thirdParty.getSysUserId());
            return new UserResume("账号不存在");
        }

        if (SystemConstant.USER_STATUS_ENABLED != user.getSysUserStatus()) {
            log.warn("Google登录失败: 账户已禁用 userId={}", thirdParty.getSysUserId());
            return new UserResume("账号已被禁用");
        }

        log.info("Google登录成功: {} (ID:{}), googleId={}", user.getSysUserUsername(), user.getSysUserId(), googleId);
        return loginInfo(user.getSysUserUsername(), user, address);
    }

    @Override
    public UserResume upgrade(UserResult userResult) {
        SysUser user = sysUserMapper.selectById(userResult.getUserId());
        if (user == null) {
            return new UserResume("账号不存在");
        }
        if (SystemConstant.USER_STATUS_ENABLED != user.getSysUserStatus()) {
            return new UserResume("账号已被禁用");
        }
        UserResume result = loginInfo(userResult.getUsername(), user, null);
        result.setLoginType(userResult.getLoginType());
        return result;
    }
}
