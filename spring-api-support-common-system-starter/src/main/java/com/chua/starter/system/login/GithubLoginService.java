package com.chua.starter.system.login;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.core.utils.DigestUtils;
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
 * Github 登录服务
 * <p>处理 Github 用户的登录验证与授权</p>
 *
 * @author CH
 * @since 2024/12/28
 */
@Slf4j
@Service
@LoginType("GITHUB")
@Tag(name = "Github登录", description = "基于Github OAuth的用户登录验证")
public class GithubLoginService extends AbstractUserLoginService {

    /**
     * 登录类型前缀
     */
    private static final String LOGIN_TYPE_PREFIX = "github";

    @Autowired
    private SysUserThirdPartyMapper sysUserThirdPartyMapper;

    @Override
    protected String getLoginTypePrefix() {
        return LOGIN_TYPE_PREFIX;
    }

    @Override
    protected String[] generateUserIds(SysUser user) {
        String baseId = user.getSysUserId() + "GITHUB";
        return new String[]{
                DigestUtils.md5Hex(baseId) + "GITHUB",
                DigestUtils.md5Hex(baseId)
        };
    }

    /**
     * Github 用户登录验证
     *
     * @param githubId   Github 用户唯一标识
     * @param password   不使用，可为null
     * @param address    客户端IP地址
     * @param ext        扩展参数，可为null
     * @return 用户信息，若验证失败则message字段包含错误信息
     */
    @Override
    public UserResume checkLogin(String githubId, String password, String address, Object ext) {
        log.debug("Github登录请求: githubId={}, IP={}", githubId, address);

        // 查询第三方绑定关系
        SysUserThirdParty thirdParty = sysUserThirdPartyMapper.selectOne(
                Wrappers.<SysUserThirdParty>lambdaQuery()
                        .eq(SysUserThirdParty::getSysUserThirdPartyUid, githubId)
                        .eq(SysUserThirdParty::getSysUserThirdPartyType, SysUserThirdParty.Type.GITHUB.name())
                        .last("LIMIT 1")
        );

        if (thirdParty == null || thirdParty.getSysUserId() == null) {
            log.warn("Github登录失败: 未找到关联用户 githubId={}", githubId);
            return new UserResume("账号未绑定，请先绑定Github账号");
        }

        // 查询系统用户
        SysUser user = sysUserMapper.selectById(thirdParty.getSysUserId());
        if (null == user) {
            log.warn("Github登录失败: 关联的系统用户不存在 userId={}", thirdParty.getSysUserId());
            return new UserResume("账号不存在");
        }

        if (SystemConstant.USER_STATUS_ENABLED != user.getSysUserStatus()) {
            log.warn("Github登录失败: 账户已禁用 userId={}", thirdParty.getSysUserId());
            return new UserResume("账号已被禁用");
        }

        log.info("Github登录成功: {} (ID:{}), githubId={}", user.getSysUserUsername(), user.getSysUserId(), githubId);
        return loginInfo(user.getSysUserUsername(), user, address);
    }

    @Override
    public UserResume upgrade(UserResult userResult) {
        SysUser user = sysUserMapper.selectById(userResult.getUserId());
        if (null == user) {
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
