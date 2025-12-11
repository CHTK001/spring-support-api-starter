package com.chua.starter.auth.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.bean.BeanUtils;
import com.chua.common.support.collection.ImmutableBuilder;
import com.chua.common.support.utils.DigestUtils;
import com.chua.common.support.utils.ObjectUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.constant.Constant;
import com.chua.starter.common.support.constant.DataFilterTypeEnum;
import com.chua.starter.oauth.client.support.annotation.LoginType;
import com.chua.starter.oauth.client.support.user.OpenIdGenerator;
import com.chua.starter.oauth.client.support.user.RoleInfo;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.sso.login.UserInfoService;
import com.chua.starter.oauth.sso.mapper.SysDeptMapper;
import com.chua.starter.entity.system.SysDept;
import com.chua.starter.entity.system.SysRole;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.entity.system.SysUserRole;
import com.chua.starter.service.mapper.SysUserMapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.chua.starter.common.support.constant.DataFilterTypeEnum.ALL;
import static com.chua.starter.oauth.client.support.contants.AuthConstant.LOGIN_ERROR;

/**
 * 系统用户信息服务实现
 * <p>为系统模块提供用户认证和信息查询功能</p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/11
 */
@Slf4j
@Service
@LoginType("WEB")
@Tag(name = "系统用户登录服务", description = "系统模块提供的用户认证服务")
public class SystemUserInfoServiceImpl implements UserInfoService, Constant {

    private static final long PERMISSION_CACHE_TIMEOUT = 30;
    private static final long ROLE_CACHE_TIMEOUT = 30;
    private final int count = 6;
    private final int timeout = 5;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysDeptMapper sysDeptMapper;

    @Override
    public UserResume checkLogin(String principal, String credential, String address, Object ext) {
        String newAccessKey = "system:" + LOGIN_ERROR + principal;
        long startTime = System.currentTimeMillis();

        String errorCountStr = stringRedisTemplate.opsForValue().get(newAccessKey);
        if (StringUtils.isNotEmpty(errorCountStr)) {
            int errorCount = Integer.parseInt(errorCountStr);
            if (errorCount >= count) {
                log.warn("账户已被锁定: {}, IP: {}", principal, address);
                return new UserResume("账户已被锁定");
            }
        }

        SysUser user = sysUserMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .select(SysUser::getSysUserId, SysUser::getSysUserUsername, SysUser::getSysUserPassword,
                        SysUser::getSysUserSalt, SysUser::getSysUserStatus, SysUser::getSysDeptId,
                        SysUser::getSysUserLastIp, SysUser::getSysUserPhone)
                .eq(StringUtils.isNotEmpty(principal), SysUser::getSysUserUsername, principal)
                .or()
                .eq(StringUtils.isNotEmpty(principal), SysUser::getSysUserPhone, principal)
                .last("limit 1"));

        log.debug("用户查询耗时: {}ms", System.currentTimeMillis() - startTime);

        if (null == user) {
            log.info("登录失败: 用户不存在 {}, IP: {}", principal, address);
            return check(newAccessKey, errorCountStr != null);
        }

        if (1 != user.getSysUserStatus()) {
            log.info("登录失败: 账户已禁用 {}, IP: {}", principal, address);
            return new UserResume("账号已被禁用");
        }

        String salt = user.getSysUserSalt();
        String md5String = DigestUtils.md5Hex(credential + salt);
        if (!StringUtils.safeEquals(md5String, user.getSysUserPassword())) {
            log.info("登录失败: 密码错误 {}, IP: {}", principal, address);
            return check(newAccessKey, errorCountStr != null);
        }

        user.setSysUserLastIp(address);
        sysUserMapper.updateById(user);

        if (errorCountStr != null) {
            stringRedisTemplate.delete(newAccessKey);
        }

        log.info("登录成功: {} (ID:{}), IP: {}, 总耗时: {}ms",
                user.getSysUserUsername(), user.getSysUserId(), address,
                System.currentTimeMillis() - startTime);

        return loginInfo(principal, user, address);
    }

    @Override
    public UserResume upgrade(UserResult userResult) {
        long startTime = System.currentTimeMillis();
        SysUser user = sysUserMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .select(SysUser::getSysUserId, SysUser::getSysUserUsername, SysUser::getSysUserStatus,
                        SysUser::getSysDeptId, SysUser::getSysUserPhone)
                .eq(SysUser::getSysUserId, userResult.getUserId())
                .last("limit 1"));

        if (null == user) {
            log.warn("用户信息刷新失败: 用户不存在 ID={}", userResult.getUserId());
            return new UserResume("账号/密码不正确");
        }

        if (1 != user.getSysUserStatus()) {
            log.warn("用户信息刷新失败: 账户已禁用 ID={}", userResult.getUserId());
            return new UserResume("账号已被禁用");
        }

        UserResume updatedResult = loginInfo(userResult.getUsername(), user, null);
        updatedResult.setLoginType(userResult.getLoginType());
        log.debug("用户信息刷新成功: {} (ID:{}), 耗时: {}ms",
                user.getSysUserUsername(), user.getSysUserId(),
                System.currentTimeMillis() - startTime);

        return updatedResult;
    }

    protected UserResume loginInfo(String accessKey, SysUser user, String address) {
        long startTime = System.currentTimeMillis();

        ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();
        String countKey = "system:login:count:" + user.getSysUserId();
        Long count = valueOps.increment(countKey);

        if (null != address && count != null) {
            user.setSysUserLastIp(address);
            user.setSysUserLongCount(count.intValue());
            sysUserMapper.updateById(user);
        }

        List<SysRole> role = getRole(user);

        LinkedHashSet<String> collect = role.stream().map(SysRole::getSysRoleCode).filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
        LinkedHashSet<RoleInfo> roleInfo =
                role.stream().map(it -> {
                    RoleInfo item = new RoleInfo();
                    item.setRoleCode(it.getSysRoleCode());
                    item.setRoleName(it.getSysRoleName());
                    item.setReadable(it.getSysRoleReadable() == null || it.getSysRoleReadable() == 1);
                    item.setWriteable(it.getSysRoleWriteable() == null || it.getSysRoleWriteable() == 1);
                    item.setExecutable(it.getSysRoleExecutable() == null || it.getSysRoleExecutable() == 1);
                    return item;
                }).collect(Collectors.toCollection(LinkedHashSet::new));

        Integer sysDeptId = user.getSysDeptId();
        String userDashboardGrid = "";
        DataFilterTypeEnum dataPermission = ALL;
        String dataPermissionRule = "";
        String userDashboard = "";
        if (null != sysDeptId) {
            SysDept sysDept = sysDeptMapper.selectOne(Wrappers.<SysDept>lambdaQuery().eq(SysDept::getSysDeptId, sysDeptId), false);
            if (null != sysDept) {
                dataPermission = ObjectUtils.defaultIfNull(DataFilterTypeEnum.getByCode(sysDept.getSysDeptDataPermission()), ALL);
                dataPermissionRule = sysDept.getSysDeptDataPermissionDeptId();
            }
        }

        startTime = System.currentTimeMillis();
        Set<String> permissions = getPermission(user);
        log.debug("权限加载耗时: {}ms", System.currentTimeMillis() - startTime);

        String userId = user.getSysUserId() + "";
        String[] ids = OpenIdGenerator.generateSystemIds("web", userId);

        return new UserResult()
                .setUserId(userId)
                .setOpenId(ids[0])
                .setUnionId(ids[1])
                .setRoles(collect)
                .setRolesByRole(roleInfo)
                .setDeptId(user.getSysDeptId() + "")
                .setDataPermission(dataPermission)
                .setPermission(permissions)
                .setDataPermissionRule(dataPermissionRule)
                .setPhone(user.getSysUserPhone())
                .setUsername(accessKey)
                .setLoginCnt(user.getSysUserLongCount())
                .setName(user.getSysUserNickname())
                .setExt(ImmutableBuilder.builderOfMap(String.class, Object.class)
                        .put(user)
                        .put("userDashboardGrid", userDashboardGrid)
                        .put("userDashboard", userDashboard)
                        .remove("password", "salt", "userEnable", "address", "lastArea", "lastLatitude", "lastIp", "lastLongitude")
                        .build()
                );
    }

    private List<SysRole> getRole(SysUser user) {
        try {
            List<Map<String, Object>> maps = sysUserMapper.selectMaps(new MPJLambdaWrapper<SysUser>()
                    .distinct()
                    .selectAll(SysRole.class)
                    .leftJoin(SysUserRole.class, SysUserRole::getSysUserId, SysUser::getSysUserId)
                    .leftJoin(SysRole.class, SysRole::getSysRoleId, SysUserRole::getSysRoleId)
                    .eq(SysUser::getSysUserId, user.getSysUserId())
                    .eq(SysRole::getSysRoleStatus, 1)
            );
            return maps.stream().map(it -> {
                SysRole sysRole = new SysRole();
                BeanUtils.copyProperties(it, sysRole);
                return sysRole;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private Set<String> getPermission(SysUser user) {
        try {
            return sysUserMapper.selectPermission(user.getSysUserId());
        } catch (Exception e) {
            return Collections.emptySet();
        }
    }

    private UserResume check(String newAccessKey, Boolean hasKey) {
        Long errorCount = stringRedisTemplate.opsForValue().increment(newAccessKey);
        if (errorCount == null) {
            errorCount = 1L;
        }

        if (errorCount == 1) {
            stringRedisTemplate.expire(newAccessKey, 24, TimeUnit.HOURS);
        }

        if (errorCount >= count) {
            stringRedisTemplate.expire(newAccessKey, timeout, TimeUnit.MINUTES);
            log.warn("账户已被锁定: {}, 错误次数: {}", newAccessKey, errorCount);
            return new UserResume("账户已被锁定，请" + timeout + "分钟后重试");
        }

        return checkLimit(errorCount.intValue());
    }

    private UserResume checkLimit(Integer errorCount) {
        int remain = count - errorCount;
        if (remain <= 3) {
            return new UserResume("账号/密码不正确，还有" + remain + "次机会");
        }
        return new UserResume("账号/密码不正确");
    }
}
