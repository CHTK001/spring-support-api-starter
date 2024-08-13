package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chua.common.support.collection.ImmutableBuilder;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.CollectionUtils;
import com.chua.common.support.utils.DigestUtils;
import com.chua.common.support.utils.RandomUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.application.GlobalSettingFactory;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.oauth.client.support.enums.AuthType;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.chua.starter.oauth.client.support.properties.AuthClientProperties;
import com.chua.starter.oauth.client.support.provider.LoginData;
import com.chua.starter.oauth.client.support.user.LoginAuthResult;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.system.entity.SysRole;
import com.chua.starter.system.entity.SysUser;
import com.chua.starter.system.entity.SysUserRole;
import com.chua.starter.system.mapper.SysRoleMapper;
import com.chua.starter.system.mapper.SysUserMapper;
import com.chua.starter.system.pojo.user.SysUserUpdate;
import com.chua.starter.system.query.SysUserQuery;
import com.chua.starter.system.result.LoginResult;
import com.chua.starter.system.result.UserInfoVO;
import com.chua.starter.system.service.SysUserRoleService;
import com.chua.starter.system.service.SysUserService;
import com.chua.starter.system.setting.VerifyCodeSetting;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
    final AuthClientProperties authProperties;
    final TransactionTemplate transactionTemplate;
    final SysRoleMapper sysRoleMapper;
    final SysUserRoleService sysUserRoleService;
    final RestTemplate restTemplate;
    final RedisTemplate redisTemplate;

    private static final GlobalSettingFactory GLOBAL_SETTING_FACTORY = GlobalSettingFactory.getInstance();

    @Override
    public IPage<SysUser> pageForUser(SysUserQuery sysUserQuery, Set<String> roles) {

        if (!AuthConstant.hasAdmin(roles)) {
            return new Page<>();
        }

        Page<SysUser> sysUserPage = baseMapper.selectPage(sysUserQuery.createPage(), new MPJLambdaWrapper<>(SysUser.class)
                .selectAll(SysUser.class)
                .innerJoin(SysUserRole.class, SysUserRole::getSysUserId, SysUser::getSysUserId)
                .innerJoin(SysRole.class, SysRole::getSysRoleId, SysUserRole::getSysRoleId)
                .like(StringUtils.isNotBlank(sysUserQuery.getUsername()), SysUser::getSysUserUsername, sysUserQuery.getUsername())
                .like(StringUtils.isNotBlank(sysUserQuery.getNickname()), SysUser::getSysUserNickname, sysUserQuery.getNickname())
                .like(StringUtils.isNotBlank(sysUserQuery.getPhone()), SysUser::getSysUserPhone, sysUserQuery.getPhone())
                .ne(AuthConstant.isAdmin(roles), SysRole::getSysRoleCode, AuthConstant.SUPER_ADMIN)
                .orderByDesc(SysUser::getCreateTime)
        );

        List<SysUser> records = sysUserPage.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return sysUserPage;
        }
        Set<Integer> userIds = records.stream().map(SysUser::getSysUserId).collect(Collectors.toUnmodifiableSet());
        List<SysRole> sysRoles = sysRoleMapper.listByUserId(userIds);
        Map<Integer, List<SysRole>> collect = sysRoles.stream().collect(Collectors.groupingBy(SysRole::getSysUserId));
        for (SysUser sysUser : records) {
            sysUser.setUserRoles(collect.getOrDefault(sysUser.getSysUserId(), Collections.emptyList()));
        }
        return sysUserPage;
    }

    @Override
    public Result<LoginResult> login(LoginData loginData, HttpServletRequest request, HttpServletResponse response) {
        String code = loginData.getVerifyCodeKey();
        VerifyCodeSetting verifyCodeSetting = GLOBAL_SETTING_FACTORY.get(VerifyCodeSetting.class);
        if(verifyCodeSetting.isOpen()) {
            if(StringUtils.isEmpty(code)) {
                return Result.failed(REQUEST_PARAM_ERROR, "验证码不能为空");
            }
            Result<LoginResult> rs = checkVerifyCode(loginData, request);
            if(null != rs) {
                return rs;
            }
        }

        String address = getIpAddress(request);
        AuthClientExecute clientExecute = AuthClientExecute.getInstance();
        LoginAuthResult accessToken = clientExecute.getAccessToken(loginData.getUsername(), loginData.getPassword(), AuthType.valueOf(loginData.getLoginType()),
                ImmutableBuilder.<String, Object>builderOfMap().put("address", address).build()
        );
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
        return Result.success(loginResult);
    }

    @SuppressWarnings("ALL")
    private Result<LoginResult> checkVerifyCode(LoginData loginData, HttpServletRequest request) {
        String code = loginData.getVerifyCodeKey();
        String sessionKey = Optional.ofNullable(request.getSession().getAttribute(CAPTCHA_SESSION_KEY)).orElse(EMPTY).toString();
        if(StringUtils.isNotBlank(sessionKey)) {
            if (!code.equalsIgnoreCase(sessionKey)) {
                return Result.failed(RESOURCE_CAPTCHA_ERROR, "校验码不正确");
            }
            return null;
        }

        String verifyCodeUlid = loginData.getVerifyCodeUlid();
        if(StringUtils.isNotBlank(verifyCodeUlid)) {
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
        String userId = userLoginInfo.getExtValue("userId");
        SysUser sysUser = getById(userId);
        UserInfoVO userInfoVO = new UserInfoVO();
        com.chua.common.support.bean.BeanUtils.copyProperties(sysUser, userInfoVO);
        userInfoVO.setRoles(userLoginInfo.getRoles());
        userInfoVO.setUserName(sysUser.getSysUserUsername());
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

        if (StringUtils.isNotBlank(sysUser1.getSysUserPassword())) {
            sysUser1.setSysUserPassword(DigestUtils.md5Hex(sysUser1.getSysUserPassword() + sysUser1.getSysUserSalt()));
        }

        sysUser1.setSysUserUsername(null);
        sysUser1.setSysUserInSystem(null);
        return ReturnResult.ok(transactionTemplate.execute(status -> {
            updateById(sysUser1);
            sysUserRoleService.remove(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getSysUserId, sysUser1.getSysUserId()));
            if (CollectionUtils.isNotEmpty(sysUser.getUserRoles())) {
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
    }

    @Override
    public ReturnResult<SysUser> saveUser(SysUserUpdate sysUserUpdate, HttpServletRequest request) {
        sysUserUpdate.setSysUserSalt(RandomUtils.randomString(14));
        sysUserUpdate.setSysUserPassword(DigestUtils.md5Hex(sysUserUpdate.getSysUserPassword() + sysUserUpdate.getSysUserSalt()));
        sysUserUpdate.setSysUserInSystem(0);
        sysUserUpdate.setSysUserRegisterIp(getIpAddress(request));
        sysUserUpdate.setSysUserLastIp(getIpAddress());
        sysUserUpdate.setSysUserLastAddress(null);
        return ReturnResult.ok(transactionTemplate.execute(status -> {
            save(sysUserUpdate);
            if (CollectionUtils.isNotEmpty(sysUserUpdate.getUserRoles())) {
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
