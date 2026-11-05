// [P3C A20 豁免] A20/A30/A31: 含日志/界面/SQL 字符串字面量等非业务魔法值场景，豁免。
package com.chua.starter.system.login;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.utils.BeanUtils;
import com.chua.common.support.converter.Converter;
import com.chua.common.support.utils.ObjectUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.constant.DataFilterTypeEnum;
import com.chua.starter.oauth.client.support.user.RoleInfo;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.client.support.user.UserInfoService;
import com.chua.starter.system.constant.SystemConstant;
import com.chua.starter.system.mapper.SysDeptMapper;
import com.chua.starter.system.mapper.SysUserMapper;
import com.chua.starter.system.properties.SystemSecurityProperties;
import com.chua.starter.system.service.SysDeptPrincipalRelService;
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
 * @since 1.0.0
 * @author CH
 */
@Slf4j
public abstract class AbstractUserLoginService implements UserInfoService {
    /**
     * Dept_leader
    */
    private static final String DEPT_LEADER = "DEPT_LEADER";
    /**
     * 认证_版本
    */
    private static final String AUTH_VERSION = "authVersion";

    @Autowired
    /**
     * 安全性属性
    */
    protected SystemSecurityProperties securityProperties;

    @Autowired
    /**
     * 字符串Redis模板
    */
    protected StringRedisTemplate stringRedisTemplate;

    @Autowired
    /**
     * SYS用户映射器
    */
    protected SysUserMapper sysUserMapper;

    @Autowired
    /**
     * sysdeptmapper
    */
    protected SysDeptMapper sysDeptMapper;

    @Autowired
    /**
     * sysdeptprincipalrel服务
    */
    protected SysDeptPrincipalRelService sysDeptPrincipalRelService;

    /**
     * 构建用户登录信息
     * <p>
     * 根据数据库中的用户信息构建完整的用户结果对象，包括：
     * 用户基本信息、角色权限、部门信息、数据权限规则等。
     * </p>
     *
     * @param accessKey 访问标识（用户名或其他登录标识）
     * @param user      用户实体对象
     * @param address   客户端IP地址，可为空表示不更新登录信息
     * @return 完整的用户结果对象
     */
    protected UserResume loginInfo(String accessKey, SysUser user, String address) {
        // 增加用户登录计数
        ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();
        String countKey = "system:login:count:" + user.getSysUserId();
        Long count = valueOps.increment(countKey);

        // 更新用户登录信息:异步写库(虚拟线程),不打认证链路同步耗时;
 // 上次 更新byid 的结果在此线程 设置更新时间,避免频繁重复写
        if (null != address && count != null) {
            user.setSysUserLastIp(address);
            user.setSysUserLongCount(Converter.convertIfNecessary(count, Integer.class));
            user.setSysUserLastLoginTime(java.time.LocalDateTime.now());
            final SysUser loginUser = user;
            Thread.ofVirtual().start(() -> {
                try {
                    sysUserMapper.updateById(loginUser);
                    loginUser.setUpdateTime(java.time.LocalDateTime.now());
                } catch (Exception e) {
                    log.warn("异步更新用户登录信息失败: userId={}", loginUser.getSysUserId(), e);
                }
            });
        }

        LoginAuthProfile authProfile = resolveAuthProfile(user);
        LinkedHashSet<String> roleCodes = authProfile.roleCodes;
        LinkedHashSet<RoleInfo> roleInfos = authProfile.roleInfos;
        DataFilterTypeEnum dataPermission = authProfile.dataPermission;
        String dataPermissionRule = authProfile.dataPermissionRule;
        Set<String> permissions = authProfile.permissions;
        String userDashboardGrid = "";
        String userDashboard = "";

        List<Integer> managedDeptIds = Collections.emptyList();
        List<String> managedDeptTreeIds = Collections.emptyList();
        if (roleCodes.contains(DEPT_LEADER)) {
            List<SysDept> managedDeptScopes = sysDeptPrincipalRelService.listManagedDeptScopes(user.getSysUserId());
            managedDeptIds = managedDeptScopes.stream()
                    .map(SysDept::getSysDeptId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            managedDeptTreeIds = managedDeptScopes.stream()
                    .map(SysDept::getSysDeptTreeId)
                    .filter(StringUtils::isNotEmpty)
                    .distinct()
                    .toList();
        }

        // 生成用户唯一标识
        String[] userIds = generateUserIds(user);

        // 构建用户结果对象
        UserResume result = new UserResume()
                .setUserId(String.valueOf(user.getSysUserId()))
                .setOpenId(userIds[0])
                .setUnionId(userIds[1])
                .setRoles(roleCodes)
                .setDeptId(Converter.convertIfNecessary(user.getSysDeptId(), String.class))
                .setPermission(permissions)
                .setPhone(user.getSysUserPhone())
                .setUsername(accessKey)
                .setNickName(user.getSysUserNickname())
                .setDataPermission(dataPermission)
                .setDataPermissionRule(dataPermissionRule);

        Map<String, Object> ext = BeanUtils.objectToMap(user);
        // 实体属性名是 sysUserPassword / sysUserSalt / sysUserTotpCode 这类带前缀的写法，
        // 早期按 "password"/"salt" 删除一个都命中不了，等于把密码哈希、盐、TOTP 密钥
        // 随登录/verify 响应下发并写进 Redis。统一走按片段匹配的脱敏。
        LoginExtSanitizer.sanitize(ext);
        ext.put(AUTH_VERSION, authVersion(user));
        ext.put("sysDeptId", user.getSysDeptId());
        ext.put("managedDeptIds", managedDeptIds);
        ext.put("managedDeptTreeIds", managedDeptTreeIds);
        ext.put("userDashboardGrid", userDashboardGrid);
        ext.put("userDashboard", userDashboard);
        // 凭据类字段已由 LoginExtSanitizer 统一剔除，此处不再逐个 remove
        result.setExt(ext);

        // 子类扩展点
        customizeUserResult(result, user);

        return result;
    }

    /**
     * 认证版本
     *
     * @param userResult 用户结果
     * @return 认证版本的结果
     */
    public String authVersion(UserResult userResult) {
        if (userResult == null || StringUtils.isEmpty(userResult.getUserId())) {
            return null;
        }
        return authVersion(Converter.convertIfNecessary(userResult.getUserId(), Integer.class));
    }

    /**
     * 认证版本
     *
     * @param user 用户
     * @return 认证版本的结果
     */
    protected String authVersion(SysUser user) {
        return user == null ? null : authVersion(user.getSysUserId());
    }

    /**
     * 认证版本
     *
     * @param userId 用户id
     * @return 认证版本的结果
     */
    protected String authVersion(Integer userId) {
        if (userId == null) {
            return null;
        }
        // 缓存 authVersion(重型聚合查询),降低高并发登录对 DB 的压力;
        // 权限变更时依赖短 TTL 过期(生产可配合权限写操作主动失效)
        String cacheKey = "system:auth:ver:" + userId;
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return cached;
            }
            String version = sysUserMapper.selectAuthVersion(userId);
            if (version != null) {
                stringRedisTemplate.opsForValue().set(cacheKey, version, java.time.Duration.ofSeconds(30));
            }
            return version;
        } catch (Exception e) {
            // Redis 不可用时的降级路径：直接查库。
            // 认证版本是可选能力（UserInfoService#authVersion 默认返回 null，调用方按
            // 「无版本信息」处理），因此这里必须再兜一层：H2 等方言没有 MD5/DATE_FORMAT，
            // 或查询瞬时失败时，不能把整个登录打成 500。
            try {
                return sysUserMapper.selectAuthVersion(userId);
            } catch (Exception dbError) {
                log.warn("获取认证版本失败,userId={},已降级为无版本: {}", userId, dbError.getMessage());
                return null;
            }
        }
    }

    /**
     * 解析认证配置文件
     *
     * @param user 用户
     * @return resolve认证配置文件的结果
     */
    private LoginAuthProfile resolveAuthProfile(SysUser user) {
        if (user == null || user.getSysUserId() == null) {
            return new LoginAuthProfile();
        }
        // 缓存 selectAuthRows 结果(多表 JOIN 较重的查询),权限相对稳定,10s TTL
        String cacheKey = "system:auth:rows:" + user.getSysUserId();
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return rebuildProfileFromCache(cached);
            }
        } catch (Exception e) {
            log.warn("读取权限缓存失败,userId={}", user.getSysUserId(), e);
        }
        LoginAuthProfile profile = doResolveProfile(user);
        try {
            String json = profileToCacheJson(profile);
            stringRedisTemplate.opsForValue().set(cacheKey, json, java.time.Duration.ofSeconds(10));
        } catch (Exception e) {
            log.warn("写入权限缓存失败,userId={}", user.getSysUserId(), e);
        }
        return profile;
    }

    /**
     * 串行缓存字段
     *
     * @param p p
     * @return 配置文件转为缓存json的结果
     */
    private String profileToCacheJson(LoginAuthProfile p) {
        return String.join("|",
                String.join(",", p.roleCodes),
                String.join(",", p.permissions),
                p.dataPermission.name(),
                p.dataPermissionRule == null ? "" : p.dataPermissionRule);
    }

    /**
     * 反序列化缓存
     *
     * @param cache 缓存
     * @return rebuild配置文件从缓存的结果
     */
    private LoginAuthProfile rebuildProfileFromCache(String cache) {
        LoginAuthProfile p = new LoginAuthProfile();
        String[] parts = cache.split("\\|", 4);
        if (parts.length >= 1 && !parts[0].isEmpty()) {
            for (String s : parts[0].split(",")) {
                p.roleCodes.add(s);
            }
        }
        if (parts.length >= 2 && !parts[1].isEmpty()) {
            for (String s : parts[1].split(",")) {
                p.permissions.add(s);
            }
        }
        if (parts.length >= 3 && !parts[2].isEmpty()) {
            try {
                p.dataPermission = DataFilterTypeEnum.valueOf(parts[2]);
            } catch (Exception e) {
                log.debug("[login] 解析数据权限类型失败，已忽略: {}", e.getMessage());
            }
        }
        if (parts.length >= 4) {
            p.dataPermissionRule = parts[3];
        }
        return p;
    }

    /**
     * 实际查询DB解析权限
     *
     * @param user 用户
     * @return 执行resolve配置文件的结果
     */
    private LoginAuthProfile doResolveProfile(SysUser user) {
        LoginAuthProfile profile = new LoginAuthProfile();
        List<Map<String, Object>> authRows = sysUserMapper.selectAuthRows(user.getSysUserId());
        if (authRows == null || authRows.isEmpty()) {
            return profile;
        }

        for (Map<String, Object> authRow : authRows) {
            // MyBatis 对 resultType=HashMap 的查询在整行均为 NULL 时（如 LEFT JOIN 无匹配且过滤条件排除）会返回 null 行，需跳过防御
            if (authRow == null) {
                continue;
            }
            if (profile.dataPermission == ALL) {
                Integer permissionCode = Converter.convertIfNecessary(authRow.get("sys_dept_data_permission"), Integer.class);
                DataFilterTypeEnum resolvedPermission = DataFilterTypeEnum.getByCode(permissionCode);
                if (resolvedPermission != null) {
                    profile.dataPermission = resolvedPermission;
                }
                profile.dataPermissionRule = ObjectUtils.defaultIfNull(
                        Converter.convertIfNecessary(authRow.get("sys_dept_data_permission_dept_id"), String.class), "");
            }

            String roleCode = Converter.convertIfNecessary(authRow.get("sys_role_code"), String.class);
            if (StringUtils.isNotEmpty(roleCode)) {
                profile.roleCodes.add(roleCode);
                profile.roleInfos.add(toRoleInfo(authRow, roleCode));
            }

            String permission = Converter.convertIfNecessary(authRow.get("sys_menu_perm"), String.class);
            if (StringUtils.isNotEmpty(permission)) {
                profile.permissions.add(permission);
            }
        }
        return profile;
    }

    /**
     * 生成用户唯一标识（打开id和unionid）
     * <p>子类可覆盖此方法实现不同的ID生成策略</p>
     *
     * @param user 用户实体
     * @return String数组，[0]=OpenId, [1]=unionid
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
     * 转为角色信息
     *
     * @param authRow 认证row
     * @param roleCode 角色编码
     * @return 转为角色信息的结果
     */
    private RoleInfo toRoleInfo(Map<String, Object> authRow, String roleCode) {
        RoleInfo item = new RoleInfo();
        item.setRoleCode(roleCode);
        item.setRoleName(Converter.convertIfNecessary(authRow.get("sys_role_name"), String.class));

        Integer readable = Converter.convertIfNecessary(authRow.get("sys_role_readable"), Integer.class);
        Integer writeable = Converter.convertIfNecessary(authRow.get("sys_role_writeable"), Integer.class);
        Integer executable = Converter.convertIfNecessary(authRow.get("sys_role_executable"), Integer.class);
        item.setReadable(readable == null || readable == SystemConstant.PERMISSION_ENABLED);
        item.setWriteable(writeable == null || writeable == SystemConstant.PERMISSION_ENABLED);
        item.setExecutable(executable == null || executable == SystemConstant.PERMISSION_ENABLED);
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
            // 复用 selectAuthRows(已在 resolveAuthProfile 中调用),替代独立的 selectPermission 查询
            List<Map<String, Object>> authRows = sysUserMapper.selectAuthRows(user.getSysUserId());
            if (authRows == null || authRows.isEmpty()) {
                return Collections.emptySet();
            }
            Set<String> permissions = new java.util.LinkedHashSet<>();
            for (Map<String, Object> row : authRows) {
                if (row == null) {
                    continue;
                }
                String perm = Converter.convertIfNecessary(row.get("sys_menu_perm"), String.class);
                if (StringUtils.isNotEmpty(perm)) {
                    permissions.add(perm);
                }
            }
            return permissions;
        } catch (Exception e) {
            log.warn("获取用户权限失败: 用户ID={}, 错误={}", user.getSysUserId(), e.getMessage());
            return Collections.emptySet();
        }
    }

    private static final class LoginAuthProfile {
        /**
         * Rolecodes
        */
        private final LinkedHashSet<String> roleCodes = new LinkedHashSet<>();
        /**
         * Roleinfos
        */
        private final LinkedHashSet<RoleInfo> roleInfos = new LinkedHashSet<>();
        /**
         * 权限
        */
        private final LinkedHashSet<String> permissions = new LinkedHashSet<>();
        /**
         * 数据权限
        */
        private DataFilterTypeEnum dataPermission = ALL;
        /**
         * 数据permissionrule
        */
        private String dataPermissionRule = "";
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