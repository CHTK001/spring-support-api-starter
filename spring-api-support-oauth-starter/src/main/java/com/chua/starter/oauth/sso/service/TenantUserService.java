package com.chua.starter.oauth.sso.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.bean.BeanUtils;
import com.chua.common.support.collection.ImmutableBuilder;
import com.chua.common.support.converter.Converter;
import com.chua.common.support.utils.DigestUtils;
import com.chua.common.support.utils.NumberUtils;
import com.chua.common.support.utils.ObjectUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.constant.Constant;
import com.chua.starter.common.support.constant.DataFilterTypeEnum;
import com.chua.starter.datasource.support.DataSourceContextSupport;
import com.chua.starter.oauth.client.support.annotation.LoginType;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.sso.mapper.SysDeptMapper;
import com.chua.starter.oauth.support.service.UserInfoService;
import com.chua.starter.service.mapper.SysUserMapper;
import com.chua.starter.system.entity.SysDept;
import com.chua.starter.system.entity.SysRole;
import com.chua.starter.system.entity.SysUser;
import com.chua.starter.system.entity.SysUserRole;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.ConcurrentReferenceHashMap;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.chua.starter.common.support.constant.DataFilterTypeEnum.ALL;
import static com.chua.starter.oauth.client.support.contants.AuthConstant.LOGIN_ERROR;

/**
 * 用户信息鉴权
 *
 * @author CH
 */
@Service
@LoginType("tenant")
public class TenantUserService implements UserInfoService, Constant {
    private final int count = 6;
    private final int timeout = 5;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    private static final Map<String, String> _ENGINE_CACHE = new ConcurrentReferenceHashMap<>(1024);
    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysDeptMapper sysDeptMapper;
    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public UserResult checkLogin(String username, String password, String address, Object ext) {
        String newAccessKey = "tenant:" + LOGIN_ERROR + username;
        Map extMap = (Map) ext;
        String tenantId = (String) extMap.get("tenantId");
        Boolean boos = stringRedisTemplate.hasKey(newAccessKey);
        if (null != boos && boos) {
            int errorCount = Integer.parseInt(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(newAccessKey)));
            if (errorCount == count) {
                return new UserResult().setMessage("账户已被锁定");
            }
        }

        String dataSourceName = findDataSource(username, tenantId);
        DataSourceContextSupport.setDbType(dataSourceName);
        try {
            SysUser user = sysUserMapper.selectOne(username, tenantId);
            if (null == user || null == user.getSysTenantId()) {
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

            return loginInfo(username, user, address);
        } finally {
            DataSourceContextSupport.clearDbType();
            DataSourceContextSupport.setDbType(DataSourceContextSupport.DEFAULT_DATASOURCE);
        }
    }

    private String findDataSource(String username, String tenantId) {
        return _ENGINE_CACHE.computeIfAbsent(username, (key) -> {
            Map<String, DataSource> beansOfType = applicationContext.getBeansOfType(DataSource.class);
            Map<String, DataSource> filter = new HashMap<>(beansOfType.size());
            for (Map.Entry<String, DataSource> entry : beansOfType.entrySet()) {
                if (entry.getKey().contains("tenant")) {
                    filter.put(entry.getKey(), entry.getValue());
                }
            }
            for (Map.Entry<String, DataSource> entry : filter.entrySet()) {
                // 切换数据源
                DataSourceContextSupport.setDbType(entry.getKey());
                try {
                    SysUser user = sysUserMapper.selectOne(username, tenantId);
                    if (null != user) {
                        return entry.getKey();
                    }
                } finally {
                    DataSourceContextSupport.clearDbType();
                    DataSourceContextSupport.setDbType(DataSourceContextSupport.DEFAULT_DATASOURCE);
                }
            }
            return null;
        });
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
        UserResult userResult1 = loginInfo(userResult.getUsername(), user, null);
        userResult1.setAuthType(userResult.getAuthType());
        return userResult1;
    }

    /**
     * 登录信息
     *
     * @param accessKey 访问密钥
     * @param user      使用者
     * @param address
     * @return {@link UserResult}
     */
    protected UserResult loginInfo(String accessKey, SysUser user, String address) {
        ValueOperations stringStringValueOperations = stringRedisTemplate.opsForValue();
        String countKey = "system:user:count:" + user.getSysUserId();
        stringStringValueOperations.increment(countKey);

        if (null != address) {
            user.setSysUserLastIp(address);
            user.setSysUserLongCount(Converter.convertIfNecessary(stringStringValueOperations.get(countKey), Integer.class));
            sysUserMapper.updateById(user);
        }
        List<SysRole> role = getRole(user);

        LinkedHashSet<String> collect = role.stream().map(SysRole::getSysRoleCode).filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
        LinkedHashSet<UserResult.RoleInfo> roleInfo =
                role.stream().map(it -> {
                    UserResult.RoleInfo item = new UserResult.RoleInfo();
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
        return new UserResult()
                .setId(user.getSysUserId() + "")
                .setUserId(user.getSysUserId() + "")
                .setClientId(user.getSysClientId())
                .setOpenId(DigestUtils.md5Hex(user.getSysUserId() + user.getSysClientId()) + "SYSTEM")
                .setUnionId(DigestUtils.md5Hex(user.getSysUserId() + user.getSysClientId()))
                .setRoles(collect)
                .setTenantId(String.valueOf(user.getSysTenantId()))
                .setRolesByRole(roleInfo)
                .setDeptId(user.getSysDeptId() + "")
                .setDataPermission(dataPermission)
                .setDataPermissionRule(dataPermissionRule)
                .setPermission(getPermission(user))
                .setPhone(user.getSysUserPhone())
                .setUsername(accessKey)
                .setLoginCnt(user.getSysUserLongCount())
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
            stringRedisTemplate.opsForValue().set(newAccessKey, inte + "", timeout, TimeUnit.MINUTES);
            return checkLimit(inte);
        }
        //false,则证明redis中没有此key,密码是第一次错误,故走以下逻辑
        stringRedisTemplate.opsForValue().set(newAccessKey, 1 + "", timeout, TimeUnit.MINUTES);
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
