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
import com.chua.starter.sso.impl.SysRole;
import com.chua.starter.sso.impl.SysUser;
import com.chua.starter.sso.impl.SysUserRole;
import com.chua.starter.sso.impl.SysUserThird;
import com.chua.starter.sso.mapper.SysUserMapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
@LoginType("WEB")
public class UserService implements UserInfoService, Constant {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    private final int count = 6;
    private final int timeout = 5;


    @Resource
    private SysUserMapper sysUserMapper;

    @Override
    public UserResult checkLogin(String accessKey, String secretKey, String address, Object ext) {
        String newAccessKey = LOGIN_ERROR + accessKey;
        Boolean boos = stringRedisTemplate.hasKey(newAccessKey);
        if (null != boos && boos) {
            int errorCount = Integer.parseInt(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(newAccessKey)));
            if (errorCount == count) {
                return new UserResult().setMessage("账户已被锁定");
            }
        }

        SysUser user = sysUserMapper.selectOne(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, accessKey));
        if (null == user) {
            return new UserResult().setMessage("账号/密码不正确");
        }

        if (1 != user.getUserStatus()) {
            return new UserResult().setMessage("账号已被禁用");
        }

        String salt = user.getUserSalt();
        String md5String = DigestUtils.md5Hex(secretKey + salt);
        if (!StringUtils.safeEquals(md5String, user.getUserPassword())) {
            return check(newAccessKey, boos);
        }

//        sysUserMapper.updateById(user);
        stringRedisTemplate.delete(newAccessKey);

        return loginInfo(accessKey, user);
    }

    @Override
    public UserResult getUserInfo(String accessKey) {
        SysUser user = sysUserMapper.selectOne(new MPJLambdaWrapper<SysUser>()
                .select("IFNULL(t.user_avatar, t1.login_avatar) user_avatar")
                .select("IFNULL(t.user_nickname, t1.login_name) user_nickname")
                .leftJoin(SysUserThird.class, SysUserThird::getUserId, SysUser::getUserId)
                .eq(SysUser::getUsername, accessKey));
        if (null == user) {
            return new UserResult().setMessage("账号/密码不正确");
        }

        if (1 != user.getUserStatus()) {
            return new UserResult().setMessage("账号已被禁用");
        }
        return loginInfo(accessKey, user);
    }

    /**
     * 登录信息
     *
     * @param accessKey 访问密钥
     * @param user      使用者
     * @return {@link UserResult}
     */
    protected UserResult loginInfo(String accessKey, SysUser user) {
        stringRedisTemplate.opsForValue().increment("user:count:" + user.getUserId());
        List<SysRole> role = getRole(user);

        LinkedHashSet<String> collect = role.stream().map(SysRole::getRoleCode).filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
        if (ADMIN.equalsIgnoreCase(accessKey)) {
            collect.add(ADMIN);
        }

        String userDashboardGrid = "";
        DataFilterTypeEnum dataPermission = DataFilterTypeEnum.ALL;
        String dataPermissionRule = "";
        String userDashboard = "";
        if (!role.isEmpty()) {
            role.sort(Comparator.comparing(SysRole::getRoleSort));
            SysRole sysRole = role.get(0);
            userDashboardGrid = sysRole.getRoleGrid();
            userDashboard = sysRole.getRoleDashboard();
            dataPermissionRule = sysRole.getRoleDataRule();
            dataPermission = sysRole.getRoleDataScope();
        }


        return new UserResult()
                .setId(user.getUserId() + "")
                .setRoles(collect)
                .setDataPermission(dataPermission)
                .setDataPermissionRule(dataPermissionRule)
                .setPermission(getPermission(user))
                .setPhone(user.getUserMobile())
                .setUsername(accessKey)
                .setName(user.getUserNickname())
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
                    .leftJoin(SysUserRole.class, SysUserRole::getUserId, SysUser::getUserId)
                    .leftJoin(SysRole.class, SysRole::getRoleId, SysUserRole::getRoleId)
                    .eq(SysUser::getUserId, user.getUserId())
                    .orderByAsc(SysRole::getRoleSort)
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
            return sysUserMapper.selectPermission(user.getUserId());
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
