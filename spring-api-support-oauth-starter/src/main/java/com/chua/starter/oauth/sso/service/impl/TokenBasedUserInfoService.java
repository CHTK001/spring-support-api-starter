package com.chua.starter.oauth.sso.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.chua.common.support.objects.annotation.AutoInject;
import com.chua.starter.mybatis.service.SysBaseService;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.sso.mapper.SysUserMapper;
import com.chua.starter.oauth.sso.service.SysUserService;
import com.chua.starter.oauth.sso.support.user.UserInfoService;
import com.chua.starter.system.entity.SysToken;
import com.chua.starter.system.entity.SysUser;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 基于令牌的用户信息服务实现
 * <p>支持通过令牌进行用户身份验证</p>
 *
 * @author CH
 */
@Service
public class TokenBasedUserInfoService implements UserInfoService {

    @AutoInject
    private SysBaseService<SysToken> sysTokenService;

    @AutoInject
    private SysUserService sysUserService;

    @AutoInject
    private SysUserMapper sysUserMapper;

    @Override
    public UserResume checkLogin(String principal, String credential, String address, Object ext) {
        // principal为令牌值
        String tokenValue = principal;

        // 查询有效的令牌
        QueryWrapper<SysToken> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sys_token_value", tokenValue);
        queryWrapper.eq("sys_token_status", 1); // 启用状态
        queryWrapper.eq("sys_token_deleted", 0); // 未删除
        queryWrapper.gt("create_time", LocalDateTime.now().minusSeconds(getTokenDuration(queryWrapper)));

        SysToken sysToken = sysTokenService.getOne(queryWrapper);
        if (sysToken == null) {
            return null;
        }

        // 检查IP白名单
        if (sysToken.getSysTokenEnableWhitelist() != null && sysToken.getSysTokenEnableWhitelist() == 1) {
            if (!isIpInWhitelist(sysToken.getSysTokenWhitelistIps(), address)) {
                return null;
            }
        }

        // 查询用户信息
        SysUser sysUser = sysUserService.getById(sysToken.getSysUserId());
        if (sysUser == null || sysUser.getSysUserStatus() == 0) {
            // 用户不存在或被禁用
            return null;
        }

        // 构造用户结果
        UserResult userResult = new UserResult();
        userResult.setUid(sysUser.getSysUserId().toString());
        userResult.setUserId(sysUser.getSysUserId());
        userResult.setUsername(sysUser.getSysUserUsername());
        userResult.setNickname(sysUser.getSysUserNickname());
        userResult.setAvatar(sysUser.getSysUserAvatar());
        userResult.setPhone(sysUser.getSysUserPhone());
        userResult.setEmail(sysUser.getSysUserEmail());
        userResult.setAddress(address);
        userResult.setLoginType("TOKEN");

        return userResult;
    }

    @Override
    public UserResume upgrade(UserResult userResult) {
        // 直接返回用户信息，无需额外处理
        return userResult;
    }

    /**
     * 获取令牌持续时间
     *
     * @param queryWrapper 查询条件
     * @return 持续时间（秒）
     */
    private long getTokenDuration(QueryWrapper<SysToken> queryWrapper) {
        SysToken token = sysTokenService.getOne(queryWrapper.select("sys_token_duration"));
        return token != null && token.getSysTokenDuration() != null ? token.getSysTokenDuration() : 86400L; // 默认24小时
    }

    /**
     * 检查IP是否在白名单中
     *
     * @param whitelistIps 白名单IP列表
     * @param clientIp     客户端IP
     * @return 是否在白名单中
     */
    private boolean isIpInWhitelist(String whitelistIps, String clientIp) {
        if (whitelistIps == null || clientIp == null) {
            return false;
        }

        List<String> ips = Arrays.asList(whitelistIps.split(","));
        return ips.contains(clientIp);
    }
}