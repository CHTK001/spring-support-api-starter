package com.chua.starter.system.login;

import com.chua.common.support.core.utils.DigestUtils;
import com.chua.starter.oauth.client.support.annotation.LoginType;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.system.constant.SystemConstant;
import com.chua.starter.system.mapper.SysUserTripartiteBindMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Gitee 第三方登录服务
 * <p>处理 Gitee 用户的登录验证与授权</p>
 *
 * @author CH
 * @since 2024/01/30
 */
@Slf4j
@Service
@LoginType("gitee")
public class GiteeLoginService extends AbstractUserLoginService {

    /**
     * 登录类型前缀
     */
    private static final String LOGIN_TYPE_PREFIX = "gitee";

    @Autowired
    private SysUserTripartiteBindMapper sysUserTripartiteBindMapper;

    @Override
    protected String getLoginTypePrefix() {
        return LOGIN_TYPE_PREFIX;
    }

    @Override
    protected String[] generateUserIds(SysUser user) {
        String baseId = user.getSysUserId() + "GITEE";
        return new String[]{
                DigestUtils.md5Hex(baseId) + "GITEE",
                DigestUtils.md5Hex(baseId)
        };
    }

    @Override
    public UserResume checkLogin(String username, String password, String address, Object ext) {
        log.debug("Gitee登录请求: username={}, IP={}", username, address);

        SysUser sysUser = sysUserTripartiteBindMapper.getSysUser(username, "gitee");
        if (null == sysUser) {
            log.warn("Gitee登录失败: 未找到关联用户 username={}", username);
            return new UserResume("账号不存在");
        }

        if (SystemConstant.USER_STATUS_ENABLED != sysUser.getSysUserStatus()) {
            log.warn("Gitee登录失败: 账户已禁用 username={}", username);
            return new UserResume("账号已被禁用");
        }

        log.info("Gitee登录成功: {} (ID:{})", sysUser.getSysUserUsername(), sysUser.getSysUserId());
        return loginInfo(sysUser.getSysUserUsername(), sysUser, address);
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
