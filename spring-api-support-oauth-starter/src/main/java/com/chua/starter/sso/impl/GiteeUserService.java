package com.chua.starter.sso.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.starter.oauth.client.support.annotation.UserLoginType;
import com.chua.starter.oauth.client.support.user.UserResult;
import lombok.AllArgsConstructor;
import me.zhyd.oauth.model.AuthUser;
import org.springframework.stereotype.Service;

/**
 * 用户信息鉴权
 *
 * @author CH
 */
@Service
@AllArgsConstructor
@UserLoginType("gitee")
public class GiteeUserService extends UserService {

    private final SysUserThirdMapper sysUserThirdMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public UserResult checkLogin(String accessKey, String secretKey, String address, Object ext) {
        AuthUser authUser = (AuthUser) ext;
        if (null == ext) {
            return new UserResult().setMessage("请重新登录");
        }

        SysUserThird selectOne = sysUserThirdMapper.selectOne(Wrappers.<SysUserThird>lambdaQuery()
                .eq(SysUserThird::getLoginCodeType, "GITEE")
                .eq(SysUserThird::getLoginUsername, accessKey)
        );

        if (selectOne == null) {
            return new UserResult().setMessage("您的账号未绑定Gitee");
        }
        SysUser user = sysUserMapper.selectById(selectOne.getUserId());
        return loginInfo(user.getUsername(), user);
    }


}
