package com.chua.starter.sso.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.bean.BeanUtils;
import com.chua.common.support.collection.ImmutableBuilder;
import com.chua.common.support.utils.DigestUtils;
import com.chua.common.support.utils.NumberUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.constant.Constant;
import com.chua.starter.common.support.constant.DataFilterTypeEnum;
import com.chua.starter.oauth.client.support.annotation.LoginType;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.server.support.service.UserInfoService;
import com.chua.starter.sso.mapper.SysUserMapper;
import com.chua.starter.system.entity.SysRole;
import com.chua.starter.system.entity.SysUser;
import com.chua.starter.system.entity.SysUserRole;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.chua.starter.oauth.client.support.contants.AuthConstant.LOGIN_ERROR;

/**
 * 用户信息鉴权
 *
 * @author CH
 */
@Service
@LoginType("SYSTEM")
@RequiredArgsConstructor
public class UserService implements UserInfoService, Constant {

    final StringRedisTemplate stringRedisTemplate;
    private final int count = 6;
    private final int timeout = 5;


    final SysUserMapper sysUserMapper;

    @Override
    public UserResult checkLogin(String username, String password, String address, Object ext) {
        String newAccessKey = "system:" + LOGIN_ERROR + username;
        Boolean boos = stringRedisTemplate.hasKey(newAccessKey);
        if (null != boos && boos) {
            int errorCount = Integer.parseInt(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(newAccessKey)));
            if (errorCount == count) {
                return new UserResult().setMessage("账户已被锁定");
            }
        }

        SysUser user = sysUserMapper.selectOne(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getSysUserUsername, username));
        if (null == user) {
            check(newAccessKey, boos);
            return new UserResult().setMessage("账号/密码不正确");
        }

        if (1 != user.getSysUserStatus()) {
            return new UserResult().setMessage("账号已被禁用");
        }

        String salt = user.getSysUserSalt();
        String md5String = DigestUtils.md5Hex(password + salt);
        if (!StringUtils.safeEquals(md5String, user.getSysUserPassword())) {
            return check(newAccessKey, boos);
        }

        user.setSysUserLastIp(address);
        sysUserMapper.updateById(user);
        stringRedisTemplate.delete(newAccessKey);

        return loginInfo(username, user);
    }

    @Override
    public UserResult upgrade(UserResult userResult) {
        SysUser user = sysUserMapper.selectById(userResult.getId());
        if (null == user) {
            return new UserResult().setMessage("账号/密码不正确");
        }

        if (1 != user.getSysUserStatus()) {
            return new UserResult().setMessage("账号已被禁用");
        }
        UserResult userResult1 = loginInfo(userResult.getUsername(), user);
        userResult1.setAuthType(userResult.getAuthType());
        return userResult1;
    }

    /**
     * 登录信息
     *
     * @param accessKey 访问密钥
     * @param user      使用者
     * @return {@link UserResult}
     */
    protected UserResult loginInfo(String accessKey, SysUser user) {
        stringRedisTemplate.opsForValue().increment("system:user:count:" + user.getSysUserId());
        List<SysRole> role = getRole(user);

        LinkedHashSet<String> collect = role.stream().map(SysRole::getSysRoleCode).filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));

        String userDashboardGrid = "";
        DataFilterTypeEnum dataPermission = DataFilterTypeEnum.ALL;
        String dataPermissionRule = "";
        String userDashboard = "";

        return new UserResult()
                .setId(user.getSysUserId() + "")
                .setRoles(collect)
                .setDataPermission(dataPermission)
                .setDataPermissionRule(dataPermissionRule)
                .setPermission(getPermission(user))
                .setPhone(user.getSysUserPhone())
                .setUsername(accessKey)
                .setName(user.getSysUserNickname())
                .setExt(ImmutableBuilder.builderOfMap(String.class, Object.class)
                        .put(user)
                        .put("userDashboardGrid", userDashboardGrid)
                        .put("userDashboard", userDashboard)
                        .remove("password", "salt", "userEnable", "address", "lastArea", "lastLatitude", "lastIp", "lastLongitude")
                        .build());
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

    /**
     * 限制次数校验
     *
     * @param newAccessKey 账号
     * @param boos         key是否存在
     * @return 结果
     */
    private UserResult check(String newAccessKey, Boolean boos) {
        if (null == boos) {
            boos = false;
        }
        if (boos) {
            //如果boos为true,则证明redis中有此key,获取key的数值并加1
            Integer inte = NumberUtils.toInt(stringRedisTemplate.opsForValue().get(newAccessKey));
            inte++;
            stringRedisTemplate.opsForValue().set(newAccessKey, inte + "");
            return checkLimit(inte);
        }
        //false,则证明redis中没有此key,密码是第一次错误,故走以下逻辑
        stringRedisTemplate.opsForValue().set(newAccessKey, 1 + "");
        //redis中设置key的过期时间
        stringRedisTemplate.expire(newAccessKey, timeout, TimeUnit.MINUTES);
        return checkLimit(1);
    }

    private UserResult checkLimit(Integer errorCount) {
        if (errorCount == count) {
            return new UserResult().setMessage("账号已被锁定");
        }

        if (errorCount > (count - 3)) {
            return new UserResult().setMessage("密码错误, 剩余尝试次数" + (count - errorCount) + "次,账号将被锁定" + timeout + "分钟");
        }


        return new UserResult().setMessage("账号/密码不正确");
    }

}
