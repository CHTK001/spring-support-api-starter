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
 * 钉钉登录服务
 * <p>处理钉钉用户的登录验证与授权</p>
 *
 * @author CH
 * @since 2024/12/28
 */
@Slf4j
@Service
@LoginType("DINGTALK")
@Tag(name = "钉钉登录", description = "基于钉钉OAuth的用户登录验证")
public class DingtalkLoginService extends AbstractUserLoginService {

    /**
     * 登录类型前缀
     */
    private static final String LOGIN_TYPE_PREFIX = "dingtalk";

    @Autowired
    private SysUserThirdPartyMapper sysUserThirdPartyMapper;

    @Override
    protected String getLoginTypePrefix() {
        return LOGIN_TYPE_PREFIX;
    }

    @Override
    protected String[] generateUserIds(SysUser user) {
        String baseId = user.getSysUserId() + "DINGTALK";
        return new String[]{
                DigestUtils.md5Hex(baseId) + "DINGTALK",
                DigestUtils.md5Hex(baseId)
        };
    }

    /**
     * 钉钉用户登录验证
     *
     * @param dingtalkId 钉钉用户唯一标识（unionId或openId）
     * @param password   不使用，可为null
     * @param address    客户端IP地址
     * @param ext        扩展参数，可为null
     * @return 用户信息，若验证失败则message字段包含错误信息
     */
    @Override
    public UserResume checkLogin(String dingtalkId, String password, String address, Object ext) {
        log.debug("钉钉登录请求: dingtalkId={}, IP={}", dingtalkId, address);

        // 查询第三方绑定关系
        SysUserThirdParty thirdParty = sysUserThirdPartyMapper.selectOne(
                Wrappers.<SysUserThirdParty>lambdaQuery()
                        .eq(SysUserThirdParty::getSysUserThirdPartyUid, dingtalkId)
                        .eq(SysUserThirdParty::getSysUserThirdPartyType, SysUserThirdParty.Type.DINGTALK.name())
                        .last("LIMIT 1")
        );

        if (thirdParty == null || thirdParty.getSysUserId() == null) {
            log.warn("钉钉登录失败: 未找到关联用户 dingtalkId={}", dingtalkId);
            return new UserResume("账号未绑定，请先绑定钉钉账号");
        }

        // 查询系统用户
        SysUser user = sysUserMapper.selectById(thirdParty.getSysUserId());
        if (null == user) {
            log.warn("钉钉登录失败: 关联的系统用户不存在 userId={}", thirdParty.getSysUserId());
            return new UserResume("账号不存在");
        }

        if (SystemConstant.USER_STATUS_ENABLED != user.getSysUserStatus()) {
            log.warn("钉钉登录失败: 账户已禁用 userId={}", thirdParty.getSysUserId());
            return new UserResume("账号已被禁用");
        }

        log.info("钉钉登录成功: {} (ID:{}), dingtalkId={}", user.getSysUserUsername(), user.getSysUserId(), dingtalkId);
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
