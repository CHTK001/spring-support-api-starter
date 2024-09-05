package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.bean.BeanUtils;
import com.chua.common.support.collection.ImmutableBuilder;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.CollectionUtils;
import com.chua.common.support.utils.DigestUtils;
import com.chua.common.support.utils.RandomUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.application.GlobalSettingFactory;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.common.support.utils.CookieUtil;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.oauth.client.support.enums.AuthType;
import com.chua.starter.oauth.client.support.enums.UpgradeType;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.chua.starter.oauth.client.support.properties.AuthClientProperties;
import com.chua.starter.oauth.client.support.provider.LoginData;
import com.chua.starter.oauth.client.support.user.LoginAuthResult;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.system.entity.*;
import com.chua.starter.system.mapper.SysRoleMapper;
import com.chua.starter.system.mapper.SysUserLayoutMapper;
import com.chua.starter.system.mapper.SysUserMapper;
import com.chua.starter.system.pojo.menu.SysMenuVO;
import com.chua.starter.system.pojo.user.*;
import com.chua.starter.system.query.SysUserQuery;
import com.chua.starter.system.result.LoginResult;
import com.chua.starter.system.result.UserInfoVO;
import com.chua.starter.system.service.SysMenuService;
import com.chua.starter.system.service.SysUserRoleService;
import com.chua.starter.system.service.SysUserService;
import com.chua.starter.system.setting.GuestDefaultSetting;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;
import java.util.stream.Collectors;

import static com.chua.common.support.constant.CommonConstant.EMPTY;
import static com.chua.common.support.constant.Constants.CAPTCHA_SESSION_KEY;
import static com.chua.common.support.lang.code.ReturnCode.*;
import static com.chua.starter.common.support.utils.RequestUtils.getIpAddress;

/**
 * @author CH
 * @since 2024/8/7
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
    final AuthClientProperties authProperties;
    final TransactionTemplate transactionTemplate;
    final SysRoleMapper sysRoleMapper;
    final SysUserRoleService sysUserRoleService;
    final RestTemplate restTemplate;
    final RedisTemplate redisTemplate;
    final SysMenuService sysMenuService;
    final SysUserLayoutMapper sysUserLayoutMapper;
    private static final GlobalSettingFactory GLOBAL_SETTING_FACTORY = GlobalSettingFactory.getInstance();

    @Override
    public IPage<SysUserVO> pageForUser(SysUserQuery sysUserQuery, Set<String> roles) {
        Page<SysUser> sysUserPage = baseMapper.selectPageUser(sysUserQuery.createPage(), AuthConstant.hasAdmin(roles), sysUserQuery);

        List<SysUser> records1 = sysUserPage.getRecords();
        Page<SysUserVO> result = new Page<>();
        result.setCurrent(sysUserPage.getCurrent())
                .setSize(sysUserPage.getSize())
                .setTotal(sysUserPage.getTotal())
                .setRecords(records1.stream().map(it -> BeanUtils.copyProperties(it, SysUserVO.class)).collect(Collectors.toList()));
        List<SysUserVO> records = result.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return result;
        }
        Set<Integer> userIds = records.stream().map(SysUserVO::getSysUserId).collect(Collectors.toUnmodifiableSet());
        List<SysRole> sysRoles = sysRoleMapper.listByUserId(userIds);
        Map<Integer, List<SysRole>> collect = sysRoles.stream().collect(Collectors.groupingBy(SysRole::getSysUserId));
        for (SysUserVO sysUser : records) {
            sysUser.setUserRoles(collect.getOrDefault(sysUser.getSysUserId(), Collections.emptyList()));
        }
        return result;
    }

    @Override
    public Result<LoginResult> login(LoginData loginData, HttpServletRequest request, HttpServletResponse response) {
        String code = loginData.getVerifyCodeKey();
        GuestDefaultSetting verifyCodeSetting = GLOBAL_SETTING_FACTORY.get(GuestDefaultSetting.class);
        if (verifyCodeSetting.isCheckCodeOpen()) {
            if (StringUtils.isEmpty(code)) {
                return Result.failed(REQUEST_PARAM_ERROR, "验证码不能为空");
            }
            Result<LoginResult> rs = checkVerifyCode(loginData, request);
            if (null != rs) {
                return rs;
            }
        }

        String address = getIpAddress(request);
        AuthClientExecute clientExecute = AuthClientExecute.getInstance();
        long startTime = System.currentTimeMillis();
        LoginAuthResult accessToken = clientExecute.getAccessToken(loginData.getUsername(), loginData.getPassword(), AuthType.valueOf(loginData.getLoginType()),
                ImmutableBuilder.<String, Object>builderOfMap().put("address", address).build()
        );

        log.info("登录耗时:{}ms", System.currentTimeMillis() - startTime);
        if (null == accessToken) {
            return Result.failed(USERNAME_OR_PASSWORD_ERROR, "账号或者密码不正确");
        }

        if (accessToken.getCode() != 200) {
            return Result.failed(REQUEST_PARAM_ERROR, accessToken.getMessage());
        }

        LoginResult loginResult = LoginResult.builder()
                .userInfo(getUserLoginInfo(accessToken.getToken()))
                .tokenType("Bearer")
                .accessToken(accessToken.getToken())
                .build();
        CookieUtil.set(response, "x-oauth-cookie", accessToken.getToken(), true);
        return Result.success(loginResult);
    }

    @SuppressWarnings("ALL")
    private Result<LoginResult> checkVerifyCode(LoginData loginData, HttpServletRequest request) {
        String code = loginData.getVerifyCodeKey();
        String sessionKey = Optional.ofNullable(request.getSession().getAttribute(CAPTCHA_SESSION_KEY)).orElse(EMPTY).toString();
        if (StringUtils.isNotBlank(sessionKey)) {
            if (!code.equalsIgnoreCase(sessionKey)) {
                return Result.failed(RESOURCE_CAPTCHA_ERROR, "校验码不正确");
            }
            return null;
        }

        String verifyCodeUlid = loginData.getVerifyCodeUlid();
        if (StringUtils.isNotBlank(verifyCodeUlid)) {
            ValueOperations valueOperations = redisTemplate.opsForValue();
            Object object = valueOperations.get(verifyCodeUlid);
            if (null == object || !code.equalsIgnoreCase(object.toString())) {
                return Result.failed(RESOURCE_CAPTCHA_ERROR, "校验码不正确");
            }

            redisTemplate.delete(verifyCodeUlid);
            return null;
        }
        return Result.failed(RESOURCE_CAPTCHA_ERROR, "请重新刷新校验码");
    }

    @Override
    public UserInfoVO getUserLoginInfo() {
        UserResult userLoginInfo = AuthClientExecute.getInstance().getUserResult();
        UserInfoVO userInfoVO = new UserInfoVO();
        com.chua.common.support.bean.BeanUtils.copyProperties(userLoginInfo.getExt(), userInfoVO);
        userInfoVO.setAvatar(userLoginInfo.getExtValue("sysUserAvatar"));
        userInfoVO.setTenantId(userLoginInfo.getTenantId());
        userInfoVO.setRoles(userLoginInfo.getRoles());
        userInfoVO.setPerms(userLoginInfo.getPermission());
        return userInfoVO;
    }

    @Override
    public ReturnResult<Boolean> delete(Long userId) {
        SysUser sysUser1 = getById(userId);
        if (null == sysUser1) {
            return Result.illegal("用户信息不存在");
        }

        if (1 == sysUser1.getSysUserInSystem()) {
            return Result.illegal("系统账号不能删除");
        }

        return ReturnResult.of(removeById(userId));
    }

    @Override
    public ReturnResult<SysUser> resetOwner(Long userId) {
        SysUser sysUser1 = getById(userId);
        if (null == sysUser1) {
            return Result.illegal("用户信息不存在, 请重新登录或者联系管理员");
        }

        String newPassword = RandomUtils.randomString(8);
        sysUser1.setSysUserPassword(DigestUtils.md5Hex(newPassword));
        sysUser1.setSysUserSalt(RandomUtils.randomString(16));
        sysUser1.setSysUserPassword(DigestUtils.md5Hex(sysUser1.getSysUserPassword() + sysUser1.getSysUserSalt()));

        updateById(sysUser1);
        return Result.ok();
    }

    @Override
    public ReturnResult<String> reset(Long userId) {
        SysUser sysUser1 = getById(userId);
        if (null == sysUser1) {
            return Result.illegal("用户信息不存在");
        }

        String password = RandomUtils.randomString(8);
        sysUser1.setSysUserPassword(DigestUtils.md5Hex(password));
        sysUser1.setSysUserSalt(RandomUtils.randomString(14));
        sysUser1.setSysUserPassword(DigestUtils.md5Hex(sysUser1.getSysUserPassword() + sysUser1.getSysUserSalt()));

        updateById(sysUser1);
        return ReturnResult.ok(password);
    }

    @Override
    public ReturnResult<SysUser> updateUser(Set<String> roles, Long userId, SysUserUpdate sysUser) {
        Integer sysUserId = sysUser.getSysUserId();
        SysUser sysUser1;
        if (null == sysUserId) {
            //更新自己账号
            sysUser1 = getById(userId);
        } else {
            //更新指定账号
            if (!AuthConstant.hasAdmin(roles)) {
                return Result.illegal("您的账号权限不足, 无法进行操作");
            }
            sysUser1 = getById(sysUserId);
        }

        BeanUtils.copyProperties(sysUser, sysUser1);

        if (StringUtils.isNotBlank(sysUser.getSysUserPassword())) {
            sysUser1.setSysUserSalt(RandomUtils.randomString(14));
            sysUser1.setSysUserPassword(DigestUtils.md5Hex(sysUser.getSysUserPassword() + sysUser1.getSysUserSalt()));
        }
        sysUser1.setSysUserUsername(null);
        sysUser1.setSysUserInSystem(null);
        try {
            return ReturnResult.ok(transactionTemplate.execute(status -> {
                updateById(sysUser1);
                if(sysUser.isUpdateRole()) {
                    sysUserRoleService.remove(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getSysUserId, sysUser1.getSysUserId()));
                }
                if (CollectionUtils.isNotEmpty(sysUser.getRoleIds())) {
                    sysUserRoleService.saveBatch(sysUser.getRoleIds().stream().map(role -> {
                        try {
                            SysUserRole rolsysUserRole = new SysUserRole();
                            rolsysUserRole.setSysUserId(sysUser1.getSysUserId());
                            rolsysUserRole.setSysRoleId(Integer.valueOf(role));
                            return rolsysUserRole;
                        } catch (NumberFormatException e) {
                            return null;
                        }
                    }).filter(Objects::nonNull).collect(Collectors.toList()));
                }
                return sysUser1;
            }));
        } finally {
            AuthClientExecute.getInstance().upgrade(UpgradeType.TIMESTAMP);
        }
    }

    @Override
    public ReturnResult<SysUser> saveUser(SysUserUpdate sysUserUpdate, HttpServletRequest request) {
        sysUserUpdate.setSysUserSalt(RandomUtils.randomString(14));
        sysUserUpdate.setSysUserPassword(DigestUtils.md5Hex(sysUserUpdate.getSysUserPassword() + sysUserUpdate.getSysUserSalt()));
        sysUserUpdate.setSysUserInSystem(0);
        sysUserUpdate.setSysUserDelete(0);
        sysUserUpdate.setSysUserStatus(1);
        sysUserUpdate.setSysUserRegisterIp(getIpAddress(request));
        sysUserUpdate.setSysUserLastIp(getIpAddress());
        sysUserUpdate.setSysUserLastAddress(null);
        return ReturnResult.ok(transactionTemplate.execute(status -> {
            save(sysUserUpdate);
            if (CollectionUtils.isNotEmpty(sysUserUpdate.getRoleIds())) {
                sysUserRoleService.saveBatch(sysUserUpdate.getRoleIds().stream().map(role -> {
                    try {
                        SysUserRole rolsysUserRole = new SysUserRole();
                        rolsysUserRole.setSysUserId(sysUserUpdate.getSysUserId());
                        rolsysUserRole.setSysRoleId(Integer.valueOf(role));
                        return rolsysUserRole;
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList()));
            }
            return sysUserUpdate;
        }));
    }

    @Override
    public ReturnResult<List<SysMenuVO>> getUserMenu(Set<String> roles) {
        return sysMenuService.list(new SysMenu(), roles, 3);
    }

    @Override
    public Result<LoginResult> refresh(RefreshToken refreshToken, HttpServletRequest request, HttpServletResponse response) {
        AuthClientExecute clientExecute = AuthClientExecute.getInstance();
        com.chua.starter.oauth.client.support.user.LoginResult upgrade = clientExecute.upgrade(UpgradeType.REFRESH);
        LoginResult loginResult = LoginResult.builder()
                .userInfo(getUserLoginInfo(upgrade.getToken()))
                .tokenType("Bearer")
                .accessToken(upgrade.getToken())
                .build();
        CookieUtil.set(response, "x-oauth-cookie", upgrade.getToken(), true);
        return Result.success(loginResult);
    }

    @Override
    public ReturnResult<SysUser> updateOwner(Set<String> roles, Long userId, SysUserPasswordUpdate sysUserPasswordUpdate) {
        SysUser sysUser = baseMapper.selectById(userId);
        if(null == sysUser) {
            return ReturnResult.illegal("当前账号信息异常, 请重新登录或者刷新后重试!");
        }

        if(null != sysUser.getSysUserStatus() && sysUser.getSysUserStatus() == 0) {
            return ReturnResult.illegal("您的账号已被禁用, 无法进行操作");
        }

        String oldPassword = sysUserPasswordUpdate.getOldPassword();
        if (StringUtils.isNotBlank(oldPassword)) {
            if (!DigestUtils.md5Hex(oldPassword + sysUser.getSysUserSalt()).equals(sysUser.getSysUserPassword())) {
                return ReturnResult.illegal("当前密码错误");
            }
        } else {
            return ReturnResult.illegal("当前密码不能为空");
        }

        if (StringUtils.isNotBlank(sysUserPasswordUpdate.getNewPassword())) {
            sysUser.setSysUserSalt(RandomUtils.randomString(14));
            sysUser.setSysUserPassword(DigestUtils.md5Hex(sysUserPasswordUpdate.getNewPassword() + sysUser.getSysUserSalt()));
        } else {
            return ReturnResult.illegal("新密码不能为空");
        }

        updateById(sysUser);
        return ReturnResult.ok(sysUser);
    }

    @Override
    public UserLayout getUserLayout(Long userId, Set<String> roles) {
        SysUserLayout sysUserLayout = sysUserLayoutMapper.selectOne(Wrappers.<SysUserLayout>lambdaQuery().eq(SysUserLayout::getSysLayoutUserId, userId));
        UserLayout userLayout = new UserLayout();
        if(null != sysUserLayout) {
            userLayout.setLayout(sysUserLayout.getSysLayoutLayout());
            userLayout.setGrid(sysUserLayout.getSysLayoutGrid());
            userLayout.setComponent(sysUserLayout.getSysLayoutComponent());
            return userLayout;
        }
        return null;
    }

    @Override
    public UserLayout saveUserLayout(Long userId, UserLayout userLayout) {
        SysUserLayout sysUserLayout = sysUserLayoutMapper.selectOne(Wrappers.<SysUserLayout>lambdaQuery().eq(SysUserLayout::getSysLayoutUserId, userId));
        if(null == sysUserLayout) {
            sysUserLayout = new SysUserLayout();
        }
        sysUserLayout.setSysLayoutComponent(userLayout.getComponent());
        sysUserLayout.setSysLayoutGrid(userLayout.getGrid());
        sysUserLayout.setSysLayoutLayout(userLayout.getLayout());
        sysUserLayout.setSysLayoutUserId(Math.toIntExact(userId));

        if(null == sysUserLayout.getSysLayoutId()) {
            sysUserLayoutMapper.insert(sysUserLayout);
        } else {
            sysUserLayoutMapper.updateById(sysUserLayout);
        }

        return userLayout;
    }

    /**
     * 获取用户信息
     *
     * @param token token
     * @return UserInfoVO
     */
    public UserInfoVO getUserLoginInfo(String token) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null || StringUtils.isBlank(token)) {
            return new UserInfoVO();
        }

        ServletRequestAttributes attributes = (ServletRequestAttributes) requestAttributes;
        attributes.getRequest().setAttribute(authProperties.getTokenName(), token);
        return getUserLoginInfo();
    }
}
