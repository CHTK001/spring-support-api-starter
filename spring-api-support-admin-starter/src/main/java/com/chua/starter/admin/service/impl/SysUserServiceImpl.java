package com.chua.starter.admin.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.function.Splitter;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.*;
import com.chua.starter.admin.entity.SysUser;
import com.chua.starter.admin.entity.SysUserRole;
import com.chua.starter.admin.mapper.SysUserMapper;
import com.chua.starter.admin.pojo.UserInfoVO;
import com.chua.starter.admin.query.SysUserUpdate;
import com.chua.starter.admin.query.UserPageQuery;
import com.chua.starter.admin.service.SysUserRoleService;
import com.chua.starter.admin.service.SysUserService;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.common.support.utils.RequestUtils;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.chua.starter.oauth.client.support.properties.AuthClientProperties;
import com.chua.starter.oauth.client.support.user.UserResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;

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
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private SysUserRoleService sysUserRoleService;

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

    @Override
    public ReturnResult<SysUser> updateUser(Set<String> roles1, String username, SysUserUpdate sysUser) {
        SysUser sysUser1 = baseMapper.selectById(sysUser.getUserId());
        if (null == sysUser1) {
            return Result.illegal("用户信息不存在");
        }

        if (ADMIN.equalsIgnoreCase(sysUser1.getUsername()) && !ADMIN.equalsIgnoreCase(username)) {
            return Result.illegal("您无权限修改管理员账号信息");
        }

        List<String> strings = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(sysUser.getRoleId());

        if (!roles1.contains(ADMIN) && !isAdmin()) {
            if (!sysUser1.getUsername().equalsIgnoreCase(username)) {
                return Result.illegal("您无权限修改管理员账号信息");
            }
        }

        if (!roles1.contains(ADMIN) && !sysUser1.getUsername().equalsIgnoreCase(username)) {
            return Result.illegal("您只能修改自己的账号信息");
        }

        if (StringUtils.isNotBlank(sysUser.getUserPassword()) && StringUtils.isBlank(sysUser.getUserSeRan())) {
            return Result.illegal("系统异常请联系管理员");
        }
        SysUser newSysUser = new SysUser();
        newSysUser.setUserSalt(null);
        BeanUtils.copyProperties(sysUser, newSysUser);
        newSysUser.setUsername(null);

        if (StringUtils.isNotBlank(sysUser.getUserPassword())) {
            if (StringUtils.isBlank(sysUser.getUserOldPassword())) {
                return Result.illegal("旧密码不能为空");
            }

            if (sysUser.getUserOldPassword().equals(sysUser.getUserPassword())) {
                return Result.illegal("旧密码与新密码不能相同");
            }

            Base64.Decoder decoder = Base64.getDecoder();
            String key = new String(decoder.decode(decoder.decode(sysUser.getUserSeRan())));
            String userOldPassword = DigestUtils.aesDecrypt(sysUser.getUserOldPassword(), key);

            if (!sysUser1.getUserPassword().equals(DigestUtils.md5Hex(DigestUtils.md5Hex(userOldPassword) + sysUser1.getUserSalt()))) {
                return Result.illegal("旧密码不正确");
            }

            String newPassword = DigestUtils.aesDecrypt(sysUser.getUserPassword(), key);
            newSysUser.setUserSalt(RandomUtils.randomString(16));
            newSysUser.setUserPassword(DigestUtils.md5Hex(DigestUtils.md5Hex(newPassword) + newSysUser.getUserSalt()));
        }

        if (1 == sysUser1.getUserSys()) {
            newSysUser.setUserStatus(1);
            newSysUser.setUserDeleted(0);
            newSysUser.setUserSys(1);
        }

        transactionTemplate.execute(status -> {
            updateById(newSysUser);
            sysUserRoleService.remove(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, sysUser.getUserId()));
            List<SysUserRole> roles = new ArrayList<>();
            for (String s : strings) {
                try {
                    roles.add(new SysUserRole(newSysUser.getUserId(), Long.parseLong(s)));
                } catch (NumberFormatException ignored) {
                }
            }
            sysUserRoleService.saveBatch(roles);
            return true;
        });
        refresh();
        return Result.ok();
    }

    @Override
    public ReturnResult<SysUser> saveUser(SysUser sysUser, HttpServletRequest request) {
        String username = sysUser.getUsername();
        if (count(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, username)) > 0L) {
            return Result.illegal("用户账号已存在");
        }

        Base64.Decoder decoder = Base64.getDecoder();
        String key = new String(decoder.decode(decoder.decode(sysUser.getUserSeRan())));
        String newPassword = DigestUtils.aesDecrypt(sysUser.getUserPassword(), key);

        sysUser.setUserSys(0);
        sysUser.setUserSalt(RandomUtils.randomString(16));
        sysUser.setUserPassword(DigestUtils.md5Hex(DigestUtils.md5Hex(newPassword) + sysUser.getUserSalt()));
        sysUser.setUserRegisterIp(RequestUtils.getIpAddress(request));
        sysUser.setUserCode(IdUtils.snowFlake());

        save(sysUser);
        Long userId = sysUser.getUserId();

        sysUserRoleService.remove(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, userId));
        List<SysUserRole> roles = new ArrayList<>();
        for (String s : Splitter.on(",").omitEmptyStrings().trimResults().splitToList(sysUser.getRoleId())) {
            try {
                roles.add(new SysUserRole(userId, Long.parseLong(s)));
            } catch (NumberFormatException ignored) {
            }
        }
        sysUserRoleService.saveBatch(roles);
        return Result.ok(sysUser);
    }

    private void refresh() {
        AuthClientExecute.getInstance().refreshToken();
    }

}
