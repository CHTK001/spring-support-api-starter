package com.chua.starter.admin.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.utils.MapUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.chua.starter.oauth.client.support.properties.AuthClientProperties;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.admin.entity.SysUser;
import com.chua.starter.admin.mapper.SysUserMapper;
import com.chua.starter.admin.pojo.UserInfoVO;
import com.chua.starter.admin.query.UserPageQuery;
import com.chua.starter.admin.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;

import static com.chua.starter.common.support.constant.Constant.ADMIN;

/**
 * 用户业务实现类
 *
 * @author haoxr
 * @since 2022/1/14
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
    @Resource
    private AuthClientProperties authProperties;

    /**
     * 获取用户分页列表
     *
     * @param queryParams 查询条件
     * @param username
     * @return 结果
     */
    @Override
    public IPage<SysUser> getUserPage(UserPageQuery queryParams, String username) {
        return baseMapper.getUserPage(queryParams.page(), queryParams, ADMIN.equalsIgnoreCase(username));
    }

    @Override
    public UserInfoVO getUserLoginInfo(String token) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null || StringUtils.isBlank(token)) {
            return new UserInfoVO();
        }

        ServletRequestAttributes attributes = (ServletRequestAttributes) requestAttributes;
        attributes.getRequest().setAttribute(authProperties.getTokenName(), token);
        return getUserLoginInfo();
    }

    @Override
    public UserInfoVO getUserLoginInfo() {
        UserResult userLoginInfo = AuthClientExecute.getInstance().getUserResult();
        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setUserName(userLoginInfo.getUsername());
        userInfoVO.setUserRealName(userLoginInfo.getExtValue("userRealName"));
        userInfoVO.setNickname(userLoginInfo.getName());
        userInfoVO.setUserId(MapUtils.getString(userLoginInfo.getExt(), "userId"));
        userInfoVO.setUserMobile(MapUtils.getString(userLoginInfo.getExt(), "userMobile"));
        userInfoVO.setUserGender(MapUtils.getString(userLoginInfo.getExt(), "userGender"));
        userInfoVO.setUserEmail(MapUtils.getString(userLoginInfo.getExt(), "userEmail"));
        userInfoVO.setUserAddress(MapUtils.getString(userLoginInfo.getExt(), "userAddress"));
        userInfoVO.setUserSex(MapUtils.getInteger(userLoginInfo.getExt(), "userGender"));
        userInfoVO.setUserMarker(MapUtils.getString(userLoginInfo.getExt(), "userMarker"));
        userInfoVO.setAvatar(MapUtils.getString(userLoginInfo.getExt(), "userAvatar"));
        userInfoVO.setRoles(userLoginInfo.getRoles());
        userInfoVO.setPerms(userLoginInfo.getPermission());
        return userInfoVO;
    }

    @Override
    public boolean isAdmin() {
        UserResult userLoginInfo = AuthClientExecute.getInstance().getUserResult();
        return !baseMapper.isAdmin(userLoginInfo.getExtValue("userId")).isEmpty();
    }


}
