package com.chua.starter.oauth.sso.login;

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
import com.chua.starter.oauth.client.support.user.RoleInfo;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.sso.mapper.SysDeptMapper;
import com.chua.starter.entity.system.SysDept;
import com.chua.starter.entity.system.SysRole;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.entity.system.SysUserRole;
import com.chua.starter.service.mapper.SysUserMapper;
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
 * </ul>
 *
 * <h2>实现原理</h2>
 * <ol>
 *   <li>接收租户用户的登录请求和租户ID</li>
 *   <li>自动查找包含该用户的租户数据源</li>
 *   <li>动态切换数据源验证用户凭证</li>
 *   <li>加载租户特定的用户权限和角色信息</li>
 * </ol>
 *
 * <h2>性能优化</h2>
 * <ul>
 *   <li>使用ConcurrentReferenceHashMap缓存用户-数据源映射</li>
 *   <li>避免重复查询多个租户数据库</li>
 *   <li>处理完成后自动恢复默认数据源</li>
 *   <li>批量加载用户权限和角色信息</li>
 *   <li>Redis缓存数据源查找结果</li>
 *   <li>优化数据库查询语句</li>
 * </ul>
 *
 * @author CH
 */
@Slf4j
@Service
@Spi("tenant")
@LoginType("tenant")
@Tag(name = "租户用户登录", description = "多租户环境下的用户登录验证")
public class TenantUserService implements UserInfoService, Constant {
    /**
     * 数据源缓存过期时间(分钟)
     */
    private static final long DATASOURCE_CACHE_TIMEOUT = 30;

    /**
     * 用户与数据源映射缓存
     * <p>使用ConcurrentReferenceHashMap提高并发性能并减少内存占用</p>
     */
    private static final Map<String, String> _ENGINE_CACHE = new ConcurrentReferenceHashMap<>(1024);

    /**
     * 登录尝试失败最大次数
     */
    private final int count = 6;
    
    @Autowired
    protected StringRedisTemplate stringRedisTemplate;

    /**
     * 账户锁定时间(分钟)
     */
    private final int timeout = 5;
    
    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysDeptMapper sysDeptMapper;
    
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 租户用户登录验证
     * <p>
     * 根据用户凭证验证租户系统中的用户账号，支持多租户数据源动态切换。
     * 本方法会自动查找并切换到包含该用户的租户数据源。
     * </p>
     *
     * @param principal  用户标识（通常是用户名）
     * @param credential 用户凭证（通常是密码）
     * @param address    客户端地址
     * @param ext        扩展参数，必须包含tenantId字段
     * @return 用户信息，若验证失败则message字段包含错误信息
     */
    @Override
    public UserResume checkLogin(String principal, String credential, String address, Object ext) {
        // 构造登录错误计数的Redis key
        String newAccessKey = "tenant:" + LOGIN_ERROR + principal;

        // 从扩展参数中获取租户ID
        Map extMap = (Map) ext;
        String tenantId = (String) extMap.get("tenantId");

        log.debug("租户登录请求: 用户={}, 租户={}, IP={}", principal, tenantId, address);

        // 检查账户是否已被锁定
        Boolean hasKey = stringRedisTemplate.hasKey(newAccessKey);
        if (null != hasKey && hasKey) {
            int errorCount = Integer.parseInt(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(newAccessKey)));
            if (errorCount == count) {
                log.warn("租户账户已被锁定: 用户={}, 租户={}", principal, tenantId);
                return new UserResume("账户已被锁定");
            }
        }

        // 查找包含该用户的租户数据源
        long startTime = System.currentTimeMillis();
        String dataSourceName = findDataSource(principal, tenantId);
        log.debug("数据源查找耗时: {}ms, 结果={}", System.currentTimeMillis() - startTime, dataSourceName);

        if (dataSourceName == null) {
            log.warn("租户登录失败: 未找到包含该用户的数据源 用户={}, 租户={}", principal, tenantId);
            return check(newAccessKey, hasKey);
        }

        // 切换到目标数据源
        DataSourceContextSupport.setDbType(dataSourceName);
        try {
            // 查询用户信息
            startTime = System.currentTimeMillis();
            SysUser user = sysUserMapper.selectOne(principal, tenantId);
            log.debug("租户用户查询耗时: {}ms", System.currentTimeMillis() - startTime);
            
            if (null == user || null == user.getSysTenantId()) {
                log.warn("租户登录失败: 用户不存在 用户={}, 租户={}", principal, tenantId);
                check(newAccessKey, hasKey);
                return new UserResume("账号/密码不正确");
            }

            // 检查账户状态
            if (1 != user.getSysUserStatus()) {
                log.warn("租户登录失败: 账户已禁用 用户={}, 租户={}", principal, tenantId);
                return new UserResume("账号已被禁用");
            }

            // 验证密码
            String salt = user.getSysUserSalt();
            String md5String = DigestUtils.md5Hex(credential + salt);
            if (!StringUtils.safeEquals(md5String, user.getSysUserPassword())) {
                log.warn("租户登录失败: 密码错误 用户={}, 租户={}", principal, tenantId);
                return check(newAccessKey, hasKey);
            }

            // 更新登录信息
            user.setSysUserLastIp(address);
            sysUserMapper.updateById(user);

            // 清除错误计数
            stringRedisTemplate.delete(newAccessKey);

            log.info("租户登录成功: 用户={} (ID:{}), 租户={}, IP={}", user.getSysUserUsername(), user.getSysUserId(), tenantId, address);

            // 返回用户登录信息
            return loginInfo(principal, user, address);
        } finally {
            // 清除数据源设置，恢复默认数据源
            DataSourceContextSupport.clearDbType();
            DataSourceContextSupport.setDbType(DataSourceContextSupport.DEFAULT_DATASOURCE);
        }
    }

    /**
     * 查找包含指定用户的租户数据源
     * <p>
     * 通过缓存和智能搜索策略查找包含特定用户的租户数据源。
     * 首先检查缓存，然后仅在租户相关的数据源中搜索，
     * 避免不必要的数据库切换和查询操作。
     * </p>
     *
     * @param principal 用户标识（用户名）
     * @param tenantId  租户ID
     * @return 包含该用户的数据源名称，如果未找到返回null
     */
    private String findDataSource(String principal, String tenantId) {
        // 构建缓存键 - 组合用户名和租户ID以支持同一用户在不同租户的情况
        String cacheKey = principal + ":" + tenantId;

        // 首先检查内存缓存
        String cachedDataSource = _ENGINE_CACHE.get(cacheKey);
        if (cachedDataSource != null) {
            log.debug("数据源命中缓存: 用户={}, 租户={}, 数据源={}", principal, tenantId, cachedDataSource);
            return cachedDataSource;
        }

        // 检查Redis缓存
        String redisKey = "tenant:datasource:" + cacheKey;
        String redisDataSource = stringRedisTemplate.opsForValue().get(redisKey);
        if (redisDataSource != null) {
            // 更新内存缓存
            _ENGINE_CACHE.put(cacheKey, redisDataSource);
            log.debug("数据源命中Redis缓存: 用户={}, 租户={}, 数据源={}", principal, tenantId, redisDataSource);
            return redisDataSource;
        }

        log.debug("开始查找数据源: 用户={}, 租户={}", principal, tenantId);

        // 缓存未命中，执行数据源搜索
        return _ENGINE_CACHE.computeIfAbsent(cacheKey, (key) -> {
            try {
                // 获取所有数据源
                Map<String, DataSource> beansOfType = applicationContext.getBeansOfType(DataSource.class);

                // 过滤只包含租户相关的数据源
                Map<String, DataSource> tenantDataSources = new HashMap<>(beansOfType.size());
                for (Map.Entry<String, DataSource> entry : beansOfType.entrySet()) {
                    if (entry.getKey().contains("tenant")) {
                        tenantDataSources.put(entry.getKey(), entry.getValue());
                    }
                }

                log.debug("找到{}个租户数据源", tenantDataSources.size());

                // 如果提供了租户ID，优先检查包含该ID的数据源
                if (StringUtils.isNotEmpty(tenantId)) {
                    List<Map.Entry<String, DataSource>> prioritySources = tenantDataSources.entrySet().stream()
                            .filter(entry -> entry.getKey().contains(tenantId))
                            .collect(Collectors.toList());

                    if (!prioritySources.isEmpty()) {
                        log.debug("找到{}个与租户ID匹配的数据源，优先检查", prioritySources.size());
                        for (Map.Entry<String, DataSource> entry : prioritySources) {
                            String dataSourceName = entry.getKey();
                            DataSourceContextSupport.setDbType(dataSourceName);
                            try {
                                SysUser user = sysUserMapper.selectOne(principal, tenantId);
                                if (null != user) {
                                    // 找到匹配的数据源，缓存结果
                                    stringRedisTemplate.opsForValue().set(redisKey, dataSourceName, DATASOURCE_CACHE_TIMEOUT, TimeUnit.MINUTES);
                                    log.info("用户数据源匹配成功: 用户={}, 租户={}, 数据源={}", principal, tenantId, dataSourceName);
                                    return dataSourceName;
                                }
                            } finally {
                                DataSourceContextSupport.clearDbType();
                                DataSourceContextSupport.setDbType(DataSourceContextSupport.DEFAULT_DATASOURCE);
                            }
                        }
                    }
                }

                // 检查所有其他租户数据源
                for (Map.Entry<String, DataSource> entry : tenantDataSources.entrySet()) {
                    String dataSourceName = entry.getKey();
                    DataSourceContextSupport.setDbType(dataSourceName);
                    try {
                        SysUser user = sysUserMapper.selectOne(principal, tenantId);
                        if (null != user) {
                            // 找到匹配的数据源，缓存结果
                            stringRedisTemplate.opsForValue().set(redisKey, dataSourceName, DATASOURCE_CACHE_TIMEOUT, TimeUnit.MINUTES);
                            log.info("用户数据源匹配成功: 用户={}, 租户={}, 数据源={}", principal, tenantId, dataSourceName);
                            return dataSourceName;
                        }
                    } catch (Exception e) {
                        log.warn("检查数据源时出错: {} - {}", dataSourceName, e.getMessage());
                    } finally {
                        DataSourceContextSupport.clearDbType();
                        DataSourceContextSupport.setDbType(DataSourceContextSupport.DEFAULT_DATASOURCE);
                    }
                }

                log.warn("未找到包含用户的数据源: 用户={}, 租户={}", principal, tenantId);
                return null;
            } catch (Exception e) {
                log.error("数据源查找过程出错: 用户={}, 租户={}", principal, tenantId, e);
                return null;
            }
        });
    }

    /**
     * 升级/刷新租户用户信息
     * <p>
     * 根据已有的用户信息获取最新的租户用户数据，包括权限、角色等信息。
     * 租户数据刷新也遵循相同的权限检查机制，确保用户账户状态正常。
     * </p>
     *
     * @param userResult 已有的用户信息
     * @return 更新后的用户信息
     */
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
     * <p>
     * 根据用户基本信息构建完整的用户登录结果，包括角色、权限、部门等信息。
     * 使用批量查询优化数据库访问，减少多次单条查询的开销。
     * </p>
     *
     * @param accessKey 访问密钥
     * @param user      用户实体
     * @param address   客户端地址，可为null
     * @return 完整的用户登录结果
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

        // 批量查询用户角色信息
        long startTime = System.currentTimeMillis();
        List<SysRole> roles = getRole(user);
        log.debug("用户角色查询耗时: {}ms", System.currentTimeMillis() - startTime);

        // 构建角色代码集合
        LinkedHashSet<String> roleCodes = roles.stream()
                .map(SysRole::getSysRoleCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // 构建角色详细信息集合
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

        // 获取部门信息和数据权限
        Integer sysDeptId = user.getSysDeptId();
        String userDashboardGrid = "";
        DataFilterTypeEnum dataPermission = ALL;
        String dataPermissionRule = "";
        String userDashboard = "";

        if (null != sysDeptId) {
            startTime = System.currentTimeMillis();
            SysDept sysDept = sysDeptMapper.selectOne(
                    Wrappers.<SysDept>lambdaQuery()
                            .eq(SysDept::getSysDeptId, sysDeptId)
                            .select(SysDept::getSysDeptDataPermission, SysDept::getSysDeptDataPermissionDeptId),
                    false);
            log.debug("部门信息查询耗时: {}ms", System.currentTimeMillis() - startTime);
            
            if (null != sysDept) {
                dataPermission = ObjectUtils.defaultIfNull(DataFilterTypeEnum.getByCode(sysDept.getSysDeptDataPermission()), ALL);
                dataPermissionRule = sysDept.getSysDeptDataPermissionDeptId();
            }
        }

        // 获取用户权限
        startTime = System.currentTimeMillis();
        Set<String> permissions = getPermission(user);
        log.debug("用户权限查询耗时: {}ms", System.currentTimeMillis() - startTime);

        // 构建用户结果对象
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

    /**
     * 获取用户角色信息
     * <p>
     * 使用联表查询一次性获取用户的所有角色信息，减少数据库访问次数。
     * 仅查询状态为启用的角色。
     * </p>
     *
     * @param user 用户实体
     * @return 用户角色列表
     */
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

    /**
     * 获取用户权限信息
     * <p>
     * 调用专用的权限查询方法，一次性获取用户的所有权限。
     * 方法内部已处理了角色-权限的关联关系。
     * </p>
     *
     * @param user 用户实体
     * @return 用户权限集合
     */
    private Set<String> getPermission(SysUser user) {
        try {
            return sysUserMapper.selectPermission(user.getSysUserId());
        } catch (Exception e) {
            log.warn("获取用户权限失败: 用户ID={}, 错误={}", user.getSysUserId(), e.getMessage());
            return Collections.emptySet();
        }
    }

    /**
     * 登录失败次数限制检查
     * <p>
     * 跟踪用户登录失败次数，并在达到阈值时锁定账户。
     * 使用Redis存储失败计数，设置自动过期时间。
     * </p>
     *
     * @param newAccessKey Redis键名
     * @param boos         键是否已存在
     * @return 登录失败结果，包含适当的错误消息
     */
    private UserResume check(String newAccessKey, Boolean boos) {
        if (null == boos) {
            boos = false;
        }
        if (boos) {
            // 如果boos为true,则证明redis中有此key,获取key的数值并加1
            Integer inte = NumberUtils.toInt(stringRedisTemplate.opsForValue().get(newAccessKey));
            inte++;
            stringRedisTemplate.opsForValue().set(newAccessKey, inte + "", timeout, TimeUnit.MINUTES);
            return checkLimit(inte);
        }
        // false,则证明redis中没有此key,密码是第一次错误,故走以下逻辑
        stringRedisTemplate.opsForValue().set(newAccessKey, 1 + "", timeout, TimeUnit.MINUTES);
        return checkLimit(1);
    }

    /**
     * 检查登录失败次数限制
     * <p>
     * 根据失败次数返回不同的错误消息，并在达到阈值时锁定账户。
     * </p>
     *
     * @param errorCount 错误计数
     * @return 包含适当错误消息的结果
     */
    private UserResume checkLimit(Integer errorCount) {
        if (errorCount == count) {
            return new UserResume("账号已被锁定");
        }

        if (errorCount > (count - 3)) {
            return new UserResume("密码错误, 剩余尝试次数" + (count - errorCount) + "次,账号将被锁定" + timeout + "分钟");
        }

        return new UserResume("账号/密码不正确");
    }
}
