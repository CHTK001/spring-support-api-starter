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
import com.chua.starter.oauth.sso.support.service.UserInfoService;
import com.chua.starter.service.mapper.SysUserMapper;
import com.chua.starter.system.entity.SysDept;
import com.chua.starter.system.entity.SysRole;
import com.chua.starter.system.entity.SysUser;
import com.chua.starter.system.entity.SysUserRole;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
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
 * </ul>
 *
 * @author CH
 */
@Slf4j
@Service
@LoginType("tenant")
@Tag(name = "租户用户登录", description = "多租户环境下的用户登录验证")
public class TenantUserService implements UserInfoService, Constant {
    /**
     * 数据源缓存过期时间(分钟)
     */
    private static final long DATASOURCE_CACHE_TIMEOUT = 30;
    /** 用户与数据源映射缓存 */
    private static final Map<String, String> _ENGINE_CACHE = new ConcurrentReferenceHashMap<>(1024);
    /**
     * 登录尝试失败最大次数
     */
    private final int count = 6;
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    /** 账户锁定时间(分钟) */
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
    public UserResult checkLogin(String principal, String credential, String address, Object ext) {
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
                return new UserResult().setMessage("账户已被锁定");
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
        
        DataSourceContextSupport.setDbType(dataSourceName);
        try {
            // 查询用户信息
            startTime = System.currentTimeMillis();
            SysUser user = sysUserMapper.selectOne(principal, tenantId);
            log.debug("租户用户查询耗时: {}ms", System.currentTimeMillis() - startTime);
            
            if (null == user || null == user.getSysTenantId()) {
                log.warn("租户登录失败: 用户不存在 用户={}, 租户={}", principal, tenantId);
                check(newAccessKey, hasKey);
                return new UserResult().setMessage("账号/密码不正确");
            }

            // 检查账户状态
            if (1 != user.getSysUserStatus()) {
                log.warn("租户登录失败: 账户已禁用 用户={}, 租户={}", principal, tenantId);
                return new UserResult().setMessage("账号已被禁用");
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
