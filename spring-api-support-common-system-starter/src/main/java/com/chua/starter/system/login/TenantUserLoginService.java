package com.chua.starter.system.login;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.bean.BeanUtils;
import com.chua.common.support.collection.ImmutableBuilder;
import com.chua.common.support.converter.Converter;
import com.chua.common.support.utils.DigestUtils;
import com.chua.common.support.utils.NumberUtils;
import com.chua.common.support.utils.ObjectUtils;
import com.chua.common.support.utils.MapUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.constant.Constant;
import com.chua.starter.common.support.constant.DataFilterTypeEnum;
import com.chua.starter.datasource.support.DataSourceContextSupport;
import com.chua.starter.oauth.client.support.annotation.LoginType;
import com.chua.starter.oauth.client.support.user.RoleInfo;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.client.support.user.UserInfoService;
import com.chua.starter.system.mapper.SysDeptMapper;
import com.chua.starter.system.mapper.SysUserMapper;
import com.chua.starter.entity.system.SysDept;
import com.chua.starter.entity.system.SysRole;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.entity.system.SysUserRole;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.chua.common.support.annotations.Spi;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
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
 * 租户用户登录服务
 * <p>支持多租户数据源的用户登录验证与授权</p>
 *
 * <h2>功能特性</h2>
 * <ul>
 *   <li>支持动态多租户数据源切换</li>
 *   <li>自动查找包含目标用户的租户数据库</li>
 *   <li>租户用户信息与权限管理</li>
 *   <li>错误密码尝试次数限制和账号锁定机制</li>
 *   <li>自动记住上次登录的租户</li>
 * </ul>
 *
 * @author CH
 * @since 1.0.0
 */
@Slf4j
@Service
@Spi("tenant")
@LoginType("tenant")
@Tag(name = "租户用户登录", description = "多租户环境下的用户登录验证")
public class TenantUserLoginService implements UserInfoService, Constant {
    
    /**
     * 数据源缓存过期时间(分钟)
     */
    private static final long DATASOURCE_CACHE_TIMEOUT = 30;

    /**
     * 用户上次登录租户编码缓存前缀
     */
    private static final String LAST_TENANT_CODE_KEY = "user:last_tenant_code:";

    /**
     * 上次登录租户缓存过期时间（天）
     */
    private static final long LAST_TENANT_EXPIRE_DAYS = 30;

    /**
     * 用户与数据源映射缓存
     */
    private static final Map<String, String> ENGINE_CACHE = new ConcurrentReferenceHashMap<>(1024);

    /**
     * 登录尝试失败最大次数
     */
    private final int count = 6;
    
    /**
     * 账户锁定时间(分钟)
     */
    private final int timeout = 5;
    
    @Autowired
    protected StringRedisTemplate stringRedisTemplate;
    
    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysDeptMapper sysDeptMapper;
    
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 租户用户登录验证
     *
     * @param principal  用户标识（通常是用户名）
     * @param credential 用户凭证（通常是密码）
     * @param address    客户端地址
     * @param ext        扩展参数，必须包含tenantId字段
     * @return 用户信息，若验证失败则message字段包含错误信息
     */
    @Override
    @SuppressWarnings("unchecked")
    public UserResume checkLogin(String principal, String credential, String address, Object ext) {
        String newAccessKey = "tenant:" + LOGIN_ERROR + principal;

        Map<String, Object> extMap = (Map<String, Object>) ext;
        // 优先使用租户编码，兼容旧的 tenantId 参数
        String tenantCode = MapUtils.getString(extMap, "tenantCode");
        String tenantId = MapUtils.getString(extMap, "tenantId");

        // 如果未传入租户编码，尝试使用上次登录的租户
        if (StringUtils.isEmpty(tenantCode) && StringUtils.isEmpty(tenantId)) {
            tenantCode = getLastTenantCode(principal);
            log.debug("自动选择租户: 用户={}, 租户编码={}", principal, tenantCode);
        }

        log.debug("租户登录请求: 用户={}, 租户编码={}, 租户ID={}, IP={}", principal, tenantCode, tenantId, address);

        // 检查账户是否已被锁定
        Boolean hasKey = stringRedisTemplate.hasKey(newAccessKey);
        if (null != hasKey && hasKey) {
            int errorCount = Integer.parseInt(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(newAccessKey)));
            if (errorCount == count) {
                log.warn("租户账户已被锁定: 用户={}, 租户编码={}", principal, tenantCode);
                return new UserResume("账户已被锁定");
            }
        }

        // 查找包含该用户的租户数据源
        long startTime = System.currentTimeMillis();
        String effectiveKey = StringUtils.isNotEmpty(tenantCode) ? tenantCode : tenantId;
        String dataSourceName = findDataSource(principal, effectiveKey);
        log.debug("数据源查找耗时: {}ms, 结果={}", System.currentTimeMillis() - startTime, dataSourceName);

        if (dataSourceName == null) {
            log.warn("租户登录失败: 未找到包含该用户的数据源 用户={}, 租户编码={}", principal, tenantCode);
            return check(newAccessKey, hasKey);
        }

        // 切换到目标数据源
        DataSourceContextSupport.setDbType(dataSourceName);
        try {
            startTime = System.currentTimeMillis();
            // 优先使用租户编码查询，兼容租户ID
            SysUser user;
            if (StringUtils.isNotEmpty(tenantCode)) {
                user = sysUserMapper.selectByTenantCode(principal, tenantCode);
            } else {
                user = sysUserMapper.selectOne(principal, tenantId);
            }
            log.debug("租户用户查询耗时: {}ms", System.currentTimeMillis() - startTime);
            
            if (null == user || null == user.getSysTenantId()) {
                log.warn("租户登录失败: 用户不存在 用户={}, 租户编码={}", principal, tenantCode);
                check(newAccessKey, hasKey);
                return new UserResume("账号/密码不正确");
            }

            if (1 != user.getSysUserStatus()) {
                log.warn("租户登录失败: 账户已禁用 用户={}, 租户编码={}", principal, tenantCode);
                return new UserResume("账号已被禁用");
            }

            String salt = user.getSysUserSalt();
            String md5String = DigestUtils.md5Hex(credential + salt);
            if (!StringUtils.safeEquals(md5String, user.getSysUserPassword())) {
                log.warn("租户登录失败: 密码错误 用户={}, 租户编码={}", principal, tenantCode);
                return check(newAccessKey, hasKey);
            }

            user.setSysUserLastIp(address);
            sysUserMapper.updateById(user);
            stringRedisTemplate.delete(newAccessKey);

            log.info("租户登录成功: 用户={} (ID:{}), 租户编码={}, IP={}", user.getSysUserUsername(), user.getSysUserId(), tenantCode, address);

            // 保存上次登录的租户编码
            saveLastTenantCode(principal, tenantCode);

            return loginInfo(principal, user, address);
        } finally {
            DataSourceContextSupport.clearDbType();
            DataSourceContextSupport.setDbType(DataSourceContextSupport.DEFAULT_DATASOURCE);
        }
    }

    /**
     * 查找包含指定用户的租户数据源
     */
    private String findDataSource(String principal, String tenantId) {
        String cacheKey = principal + ":" + tenantId;

        String cachedDataSource = ENGINE_CACHE.get(cacheKey);
        if (cachedDataSource != null) {
            return cachedDataSource;
        }

        String redisKey = "tenant:datasource:" + cacheKey;
        String redisDataSource = stringRedisTemplate.opsForValue().get(redisKey);
        if (redisDataSource != null) {
            ENGINE_CACHE.put(cacheKey, redisDataSource);
            return redisDataSource;
        }

        return ENGINE_CACHE.computeIfAbsent(cacheKey, (key) -> {
            try {
                Map<String, DataSource> beansOfType = applicationContext.getBeansOfType(DataSource.class);
                Map<String, DataSource> tenantDataSources = new HashMap<>(beansOfType.size());
                for (Map.Entry<String, DataSource> entry : beansOfType.entrySet()) {
                    if (entry.getKey().contains("tenant")) {
                        tenantDataSources.put(entry.getKey(), entry.getValue());
                    }
                }

                if (StringUtils.isNotEmpty(tenantId)) {
                    List<Map.Entry<String, DataSource>> prioritySources = tenantDataSources.entrySet().stream()
                            .filter(entry -> entry.getKey().contains(tenantId))
                            .collect(Collectors.toList());

                    if (!prioritySources.isEmpty()) {
                        for (Map.Entry<String, DataSource> entry : prioritySources) {
                            String dataSourceName = entry.getKey();
                            DataSourceContextSupport.setDbType(dataSourceName);
                            try {
                                SysUser user = sysUserMapper.selectOne(principal, tenantId);
                                if (null != user) {
                                    stringRedisTemplate.opsForValue().set(redisKey, dataSourceName, DATASOURCE_CACHE_TIMEOUT, TimeUnit.MINUTES);
                                    return dataSourceName;
                                }
                            } finally {
                                DataSourceContextSupport.clearDbType();
                                DataSourceContextSupport.setDbType(DataSourceContextSupport.DEFAULT_DATASOURCE);
                            }
                        }
                    }
                }

                for (Map.Entry<String, DataSource> entry : tenantDataSources.entrySet()) {
                    String dataSourceName = entry.getKey();
                    DataSourceContextSupport.setDbType(dataSourceName);
                    try {
                        SysUser user = sysUserMapper.selectOne(principal, tenantId);
                        if (null != user) {
                            stringRedisTemplate.opsForValue().set(redisKey, dataSourceName, DATASOURCE_CACHE_TIMEOUT, TimeUnit.MINUTES);
                            return dataSourceName;
                        }
                    } catch (Exception e) {
                        log.warn("检查数据源时出错: {} - {}", dataSourceName, e.getMessage());
                    } finally {
                        DataSourceContextSupport.clearDbType();
                        DataSourceContextSupport.setDbType(DataSourceContextSupport.DEFAULT_DATASOURCE);
                    }
                }

                return null;
            } catch (Exception e) {
                log.error("数据源查找过程出错: 用户={}, 租户={}", principal, tenantId, e);
                return null;
            }
        });
    }

    @Override
    public UserResume upgrade(UserResult userResult) {
        SysUser user = sysUserMapper.selectById(userResult.getUserId());
        if (null == user) {
            return new UserResume("账号/密码不正确");
        }

        if (1 != user.getSysUserStatus()) {
            return new UserResume("账号已被禁用");
        }
        UserResume userResult1 = loginInfo(userResult.getUsername(), user, null);
        userResult1.setLoginType(userResult.getLoginType());
        return userResult1;
    }

    /**
     * 构建用户登录信息
     */
    protected UserResume loginInfo(String accessKey, SysUser user, String address) {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        String countKey = "system:login:count:" + user.getSysUserId();
        ops.increment(countKey);

        if (null != address) {
            user.setSysUserLastIp(address);
            user.setSysUserLongCount(Converter.convertIfNecessary(ops.get(countKey), Integer.class));
            sysUserMapper.updateById(user);
        }

        List<SysRole> roles = getRole(user);

        LinkedHashSet<String> roleCodes = roles.stream()
                .map(SysRole::getSysRoleCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        LinkedHashSet<RoleInfo> roleInfos = roles.stream()
                .map(it -> {
                    RoleInfo item = new RoleInfo();
                    item.setRoleCode(it.getSysRoleCode());
                    item.setRoleName(it.getSysRoleName());
                    item.setReadable(it.getSysRoleReadable() == null || it.getSysRoleReadable() == 1);
                    item.setWriteable(it.getSysRoleWriteable() == null || it.getSysRoleWriteable() == 1);
                    item.setExecutable(it.getSysRoleExecutable() == null || it.getSysRoleExecutable() == 1);
                    return item;
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Integer sysDeptId = user.getSysDeptId();
        String userDashboardGrid = "";
        DataFilterTypeEnum dataPermission = ALL;
        String dataPermissionRule = "";
        String userDashboard = "";

        if (null != sysDeptId) {
            SysDept sysDept = sysDeptMapper.selectOne(
                    Wrappers.<SysDept>lambdaQuery()
                            .eq(SysDept::getSysDeptId, sysDeptId)
                            .select(SysDept::getSysDeptDataPermission, SysDept::getSysDeptDataPermissionDeptId),
                    false);
            
            if (null != sysDept) {
                dataPermission = ObjectUtils.defaultIfNull(DataFilterTypeEnum.getByCode(sysDept.getSysDeptDataPermission()), ALL);
                dataPermissionRule = sysDept.getSysDeptDataPermissionDeptId();
            }
        }

        Set<String> permissions = getPermission(user);

        return new UserResume()
                .setUserId(user.getSysUserId() + "")
                .setOpenId(DigestUtils.md5Hex(user.getSysUserId() + user.getSysClientId()) + "SYSTEM")
                .setUnionId(DigestUtils.md5Hex(user.getSysUserId() + user.getSysClientId()))
                .setRoles(roleCodes)
                .setTenantId(String.valueOf(user.getSysTenantId()))
                .setRolesByRole(roleInfos)
                .setDeptId(user.getSysDeptId() + "")
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
            log.warn("获取用户角色失败: 用户ID={}, 错误={}", user.getSysUserId(), e.getMessage());
            return Collections.emptyList();
        }
    }

    private Set<String> getPermission(SysUser user) {
        try {
            return sysUserMapper.selectPermission(user.getSysUserId());
        } catch (Exception e) {
            log.warn("获取用户权限失败: 用户ID={}, 错误={}", user.getSysUserId(), e.getMessage());
            return Collections.emptySet();
        }
    }

    private UserResume check(String newAccessKey, Boolean boos) {
        if (null == boos) {
            boos = false;
        }
        if (boos) {
            Integer inte = NumberUtils.toInt(stringRedisTemplate.opsForValue().get(newAccessKey));
            inte++;
            stringRedisTemplate.opsForValue().set(newAccessKey, inte + "", timeout, TimeUnit.MINUTES);
            return checkLimit(inte);
        }
        stringRedisTemplate.opsForValue().set(newAccessKey, 1 + "", timeout, TimeUnit.MINUTES);
        return checkLimit(1);
    }

    private UserResume checkLimit(Integer errorCount) {
        if (errorCount == count) {
            return new UserResume("账号已被锁定");
        }

        if (errorCount > (count - 3)) {
            return new UserResume("密码错误, 剩余尝试次数" + (count - errorCount) + "次,账号将被锁定" + timeout + "分钟");
        }

        return new UserResume("账号/密码不正确");
    }

    /**
     * 获取用户上次登录的租户编码
     * <p>
     * 优先从 Redis 缓存获取上次登录的租户编码，
     * 如果缓存不存在则根据账号查询 SysTenant 表获取最新的租户编码
     * </p>
     *
     * @param username 用户名
     * @return 租户编码
     */
    private String getLastTenantCode(String username) {
        // 优先从缓存获取上次登录的租户编码
        try {
            String cachedTenantCode = stringRedisTemplate.opsForValue().get(LAST_TENANT_CODE_KEY + username);
            if (StringUtils.isNotEmpty(cachedTenantCode)) {
                log.debug("从缓存获取用户上次登录租户编码: 用户={}, 租户编码={}", username, cachedTenantCode);
                return cachedTenantCode;
            }
        } catch (Exception e) {
            log.warn("获取用户上次登录租户编码缓存失败: 用户={}, 错误={}", username, e.getMessage());
        }

        // 缓存不存在时，查询数据库获取最新的租户编码
        try {
            String latestTenantCode = sysUserMapper.selectLatestTenantCodeByUsername(username);
            if (StringUtils.isNotEmpty(latestTenantCode)) {
                log.debug("从数据库获取用户最新租户编码: 用户={}, 租户编码={}", username, latestTenantCode);
                return latestTenantCode;
            }
        } catch (Exception e) {
            log.warn("查询用户最新租户编码失败: 用户={}, 错误={}", username, e.getMessage());
        }

        return null;
    }

    /**
     * 保存用户上次登录的租户编码
     *
     * @param username   用户名
     * @param tenantCode 租户编码
     */
    private void saveLastTenantCode(String username, String tenantCode) {
        try {
            if (StringUtils.isNotEmpty(tenantCode)) {
                stringRedisTemplate.opsForValue().set(
                        LAST_TENANT_CODE_KEY + username,
                        tenantCode,
                        LAST_TENANT_EXPIRE_DAYS,
                        TimeUnit.DAYS
                );
                log.debug("保存用户上次登录租户编码: 用户={}, 租户编码={}", username, tenantCode);
            }
        } catch (Exception e) {
            log.warn("保存用户上次登录租户编码失败: 用户={}, 租户编码={}, 错误={}", username, tenantCode, e.getMessage());
        }
    }
}
