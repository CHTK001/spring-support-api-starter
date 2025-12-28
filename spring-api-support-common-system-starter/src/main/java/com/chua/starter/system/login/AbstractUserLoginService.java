package com.chua.starter.system.login;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.bean.BeanUtils;
import com.chua.common.support.collection.ImmutableBuilder;
import com.chua.common.support.converter.Converter;
import com.chua.common.support.utils.ObjectUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.constant.Constant;
import com.chua.starter.common.support.constant.DataFilterTypeEnum;
import com.chua.starter.oauth.client.support.user.RoleInfo;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.client.support.user.UserInfoService;
import com.chua.starter.system.constant.SystemConstant;
import com.chua.starter.system.mapper.SysDeptMapper;
import com.chua.starter.system.mapper.SysUserMapper;
import com.chua.starter.system.properties.SystemSecurityProperties;
import com.chua.starter.entity.system.SysDept;
import com.chua.starter.entity.system.SysRole;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.entity.system.SysUserRole;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.chua.starter.common.support.constant.DataFilterTypeEnum.ALL;

/**
 * 用户登录服务抽象基类
 * <p>提供用户登录验证的公共功能，包括角色权限获取、登录信息构建、错误处理等</p>
 *
 * <h2>子类实现</h2>
 * <ul>
 *   <li>{@link UserLoginService} - 标准Web登录</li>
 *   <li>{@link TenantUserLoginService} - 多租户登录</li>
 * </ul>
 *
 * @author CH
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractUserLoginService implements UserInfoService, Constant {

    @Autowired
    protected SystemSecurityProperties securityProperties;

    @Autowired
    protected StringRedisTemplate stringRedisTemplate;

    @Autowired
    protected SysUserMapper sysUserMapper;

    @Autowired
    protected SysDeptMapper sysDeptMapper;

    /**
     * 构建用户登录信息
     * <p>
     * 根据数据库中的用户信息构建完整的用户结果对象，包括：
     * 用户基本信息、角色权限、部门信息、数据权限规则等。
     * </p>
     *
     * @param accessKey 访问标识（用户名或其他登录标识）
     * @param user      用户实体对象
     * @param address   客户端IP地址，可为null表示不更新登录信息
     * @return 完整的用户结果对象
     */
    protected UserResume loginInfo(String accessKey, SysUser user, String address) {
        // 增加用户登录计数
        ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();
        String countKey = "system:login:count:" + user.getSysUserId();
        Long count = valueOps.increment(countKey);

        // 更新用户登录信息
        if (null != address && count != null) {
            user.setSysUserLastIp(address);
            user.setSysUserLongCount(Converter.convertIfNecessary(count, Integer.class));
            sysUserMapper.updateById(user);
        }

        // 获取用户角色信息
        List<SysRole> roles = getRole(user);

        LinkedHashSet<String> roleCodes = roles.stream()
                .map(SysRole::getSysRoleCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        LinkedHashSet<RoleInfo> roleInfos = roles.stream()
                .map(this::convertToRoleInfo)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // 获取部门数据权限
        Integer sysDeptId = user.getSysDeptId();
        DataFilterTypeEnum dataPermission = ALL;
        String dataPermissionRule = "";
        String userDashboardGrid = "";
        String userDashboard = "";

        if (null != sysDeptId) {
            SysDept sysDept = sysDeptMapper.selectOne(
                    Wrappers.<SysDept>lambdaQuery()
                            .eq(SysDept::getSysDeptId, sysDeptId)
                            .select(SysDept::getSysDeptDataPermission, SysDept::getSysDeptDataPermissionDeptId),
                    false);
            if (null != sysDept) {
                dataPermission = ObjectUtils.defaultIfNull(
                        DataFilterTypeEnum.getByCode(sysDept.getSysDeptDataPermission()), ALL);
                dataPermissionRule = sysDept.getSysDeptDataPermissionDeptId();
            }
        }

        // 获取用户权限信息
        Set<String> permissions = getPermission(user);

        // 生成用户唯一标识
        String[] userIds = generateUserIds(user);

        // 构建用户结果对象
        UserResume result = new UserResume()
                .setUserId(String.valueOf(user.getSysUserId()))
                .setOpenId(userIds[0])
                .setUnionId(userIds[1])
                .setRoles(roleCodes)
                .setRolesByRole(roleInfos)
                .setDeptId(String.valueOf(user.getSysDeptId()))
                .setDataPermission(dataPermission)
                .setDataPermissionRule(dataPermissionRule)
                .setPermission(permissions)
                .setPhone(user.getSysUserPhone())
                .setUsername(accessKey)
                .setNickName(user.getSysUserNickname())
                .setExt(ImmutableBuilder.builderOfMap(String.class, Object.class)
                        .put(user)
                        .put("userDashboardGrid", userDashboardGrid)
                        .put("userDashboard", userDashboard)
                        .remove("password", "salt", "userEnable", "address", "lastArea",
                                "lastLatitude", "lastIp", "lastLongitude")
                        .build());

        // 子类扩展点
        customizeUserResult(result, user);

        return result;
    }

    /**
     * 生成用户唯一标识（OpenId和UnionId）
     * <p>子类可覆盖此方法实现不同的ID生成策略</p>
     *
     * @param user 用户实体
     * @return String数组，[0]=OpenId, [1]=UnionId
     */
    protected abstract String[] generateUserIds(SysUser user);

    /**
     * 自定义用户结果扩展点
     * <p>子类可覆盖此方法添加额外的用户信息</p>
     *
     * @param result 用户结果对象
     * @param user   用户实体
     */
    protected void customizeUserResult(UserResume result, SysUser user) {
        // 默认空实现，子类可覆盖
    }

    /**
     * 将角色实体转换为角色信息对象
     *
     * @param role 角色实体
     * @return 角色信息对象
     */
    private RoleInfo convertToRoleInfo(SysRole role) {
        RoleInfo item = new RoleInfo();
        item.setRoleCode(role.getSysRoleCode());
        item.setRoleName(role.getSysRoleName());
        item.setReadable(role.getSysRoleReadable() == null
                || role.getSysRoleReadable() == SystemConstant.PERMISSION_ENABLED);
        item.setWriteable(role.getSysRoleWriteable() == null
                || role.getSysRoleWriteable() == SystemConstant.PERMISSION_ENABLED);
        item.setExecutable(role.getSysRoleExecutable() == null
                || role.getSysRoleExecutable() == SystemConstant.PERMISSION_ENABLED);
        return item;
    }

    /**
     * 获取用户角色列表
     *
     * @param user 用户实体对象
     * @return 用户角色列表
     */
    protected List<SysRole> getRole(SysUser user) {
        try {
            List<Map<String, Object>> maps = sysUserMapper.selectMaps(new MPJLambdaWrapper<SysUser>()
                    .distinct()
                    .selectAll(SysRole.class)
                    .leftJoin(SysUserRole.class, SysUserRole::getSysUserId, SysUser::getSysUserId)
                    .leftJoin(SysRole.class, SysRole::getSysRoleId, SysUserRole::getSysRoleId)
                    .eq(SysUser::getSysUserId, user.getSysUserId())
                    .eq(SysRole::getSysRoleStatus, SystemConstant.ROLE_STATUS_ENABLED)
            );
            return maps.stream().map(it -> {
                SysRole sysRole = new SysRole();
                BeanUtils.copyProperties(it, sysRole);
                return sysRole;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("获取用户角色失败: 用户ID={}, 错误={}", user.getSysUserId(), e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 获取用户权限集合
     *
     * @param user 用户实体对象
     * @return 用户权限集合
     */
    protected Set<String> getPermission(SysUser user) {
        try {
            return sysUserMapper.selectPermission(user.getSysUserId());
        } catch (Exception e) {
            log.warn("获取用户权限失败: 用户ID={}, 错误={}", user.getSysUserId(), e.getMessage());
            return Collections.emptySet();
        }
    }

    /**
     * 处理登录失败，增加错误计数（原子操作）
     *
     * @param redisKey Redis键名
     * @param keyExists 键是否已存在
     * @return 包含错误信息的用户结果
     */
    protected UserResume handleLoginFailure(String redisKey, Boolean keyExists) {
        // 使用原子操作增加错误计数
        Long errorCount = stringRedisTemplate.opsForValue().increment(redisKey);
        if (errorCount == null) {
            errorCount = 1L;
        }

        int maxAttempts = securityProperties.getMaxLoginAttempts();
        int lockoutMinutes = securityProperties.getLockoutDurationMinutes();

        // 首次错误或达到最大次数，设置/更新过期时间
        if (errorCount == 1 || errorCount >= maxAttempts) {
            stringRedisTemplate.expire(redisKey, lockoutMinutes, TimeUnit.MINUTES);
        }

        // 达到最大错误次数，锁定账户
        if (errorCount >= maxAttempts) {
            log.warn("账户已被锁定: {}, 错误次数: {}", redisKey, errorCount);
            return new UserResume("账户已被锁定，请" + lockoutMinutes + "分钟后重试");
        }

        return buildLoginErrorResult(errorCount.intValue());
    }

    /**
     * 根据错误次数生成相应的错误信息
     *
     * @param errorCount 当前错误次数
     * @return 包含错误信息的用户结果
     */
    protected UserResume buildLoginErrorResult(int errorCount) {
        int maxAttempts = securityProperties.getMaxLoginAttempts();
        int lockoutMinutes = securityProperties.getLockoutDurationMinutes();
        int remain = maxAttempts - errorCount;

        if (remain <= 3 && remain > 0) {
            return new UserResume("账号/密码不正确，还有" + remain + "次机会，账号将被锁定" + lockoutMinutes + "分钟");
        }
        return new UserResume("账号/密码不正确");
    }

    /**
     * 检查账户是否已被锁定
     *
     * @param redisKey Redis键名
     * @return 如果被锁定返回错误结果，否则返回null
     */
    protected UserResume checkAccountLocked(String redisKey) {
        String errorCountStr = stringRedisTemplate.opsForValue().get(redisKey);
        if (StringUtils.isNotEmpty(errorCountStr)) {
            int errorCount = Integer.parseInt(errorCountStr);
            if (errorCount >= securityProperties.getMaxLoginAttempts()) {
                log.warn("账户已被锁定: {}", redisKey);
                return new UserResume("账户已被锁定");
            }
        }
        return null;
    }

    /**
     * 清除登录错误计数
     *
     * @param redisKey Redis键名
     */
    protected void clearLoginErrorCount(String redisKey) {
        stringRedisTemplate.delete(redisKey);
    }

    /**
     * 获取登录类型前缀
     * <p>用于构建Redis键名，子类应返回不同的前缀以区分不同登录类型</p>
     *
     * @return 登录类型前缀
     */
    protected abstract String getLoginTypePrefix();
}
