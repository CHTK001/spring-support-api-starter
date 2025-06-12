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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.chua.starter.common.support.constant.DataFilterTypeEnum.ALL;
import static com.chua.starter.oauth.client.support.contants.AuthConstant.LOGIN_ERROR;

/**
 * 用户信息鉴权服务
 * <p>基于用户名/密码的标准Web登录验证服务</p>
 *
 * <h2>功能特性</h2>
 * <ul>
 *   <li>支持用户名或手机号登录</li>
 *   <li>错误密码尝试次数限制和账号锁定机制</li>
 *   <li>用户权限和角色信息的动态加载</li>
 *   <li>用户登录统计和IP记录</li>
 * </ul>
 *
 * <h2>实现原理</h2>
 * <ol>
 *   <li>验证用户提供的凭证(用户名/手机号和密码)</li>
 *   <li>加载用户角色、权限和部门数据</li>
 *   <li>生成包含完整用户信息的用户结果对象</li>
 * </ol>
 *
 * @author CH
 */
@Slf4j
@Service
@LoginType("WEB")
@Tag(name = "用户登录服务", description = "标准Web用户名密码登录验证")
public class UserService implements UserInfoService, Constant {

    /**
     * 用户权限缓存过期时间(分钟)
     */
    private static final long PERMISSION_CACHE_TIMEOUT = 30;
    /**
     * 用户角色缓存过期时间(分钟)
     */
    private static final long ROLE_CACHE_TIMEOUT = 30;
    /**
     * 登录尝试失败最大次数
     */
    private final int count = 6;
    /** 账户锁定时间(分钟) */
    private final int timeout = 5;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysDeptMapper sysDeptMapper;

    /**
     * 验证用户登录凭证
     * <p>
     * 根据用户提供的登录凭证(用户名/手机号+密码)进行验证，成功后返回用户信息。
     * 支持错误密码尝试次数限制，超过指定次数会临时锁定账户。
     * </p>
     *
     * @param principal  用户标识（用户名或手机号）
     * @param credential 用户密码
     * @param address    客户端IP地址
     * @param ext        扩展参数，可为null
     * @return 用户信息，若验证失败则message字段包含错误信息
     */
    @Override
    public UserResult checkLogin(String principal, String credential, String address, Object ext) {
        // 构建登录错误计数的Redis键
        String newAccessKey = "system:" + LOGIN_ERROR + principal;

        // 检查用户是否已被锁定
        Boolean hasKey = stringRedisTemplate.hasKey(newAccessKey);
        if (null != hasKey && hasKey) {
            int errorCount = Integer.parseInt(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(newAccessKey)));
            if (errorCount == count) {
                log.warn("账户已被锁定: {}, IP: {}", principal, address);
                return new UserResult().setMessage("账户已被锁定");
            }
        }

        // 查询用户信息 - 支持用户名或手机号登录
        long startTime = System.currentTimeMillis();
        SysUser user = sysUserMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getSysUserUsername, principal)
                .or()
                .eq(SysUser::getSysUserPhone, principal)
                .last("limit 1"));
        log.debug("用户查询耗时: {}ms", System.currentTimeMillis() - startTime);

        if (null == user) {
            log.info("登录失败: 用户不存在 {}, IP: {}", principal, address);
            return check(newAccessKey, hasKey);
        }

        // 检查账户状态
        if (1 != user.getSysUserStatus()) {
            log.info("登录失败: 账户已禁用 {}, IP: {}", principal, address);
            return new UserResult().setMessage("账号已被禁用");
        }

        // 验证密码
        String salt = user.getSysUserSalt();
        String md5String = DigestUtils.md5Hex(credential + salt);
        if (!StringUtils.safeEquals(md5String, user.getSysUserPassword())) {
            log.info("登录失败: 密码错误 {}, IP: {}", principal, address);
            return check(newAccessKey, hasKey);
        }

        // 更新用户登录信息
        user.setSysUserLastIp(address);
        sysUserMapper.updateById(user);
        
        // 登录成功，清除错误计数
        stringRedisTemplate.delete(newAccessKey);
        log.info("登录成功: {} (ID:{}), IP: {}", user.getSysUserUsername(), user.getSysUserId(), address);

        return loginInfo(principal, user, address);
    }

    /**
     * 升级/刷新用户信息
     * <p>
     * 根据已有的用户信息获取最新的用户数据，包括权限、角色等信息。
     * 用于刷新用户token或会话时更新用户信息。
     * </p>
     *
     * @param userResult 已有的用户信息
     * @return 更新后的用户信息
     */
    @Override
    public UserResult upgrade(UserResult userResult) {
        // 查询最新的用户信息
        SysUser user = sysUserMapper.selectById(userResult.getId());
        if (null == user) {
            log.warn("用户信息刷新失败: 用户不存在 ID={}", userResult.getId());
            return new UserResult().setMessage("账号/密码不正确");
        }

        // 检查账户状态
        if (1 != user.getSysUserStatus()) {
            log.warn("用户信息刷新失败: 账户已禁用 ID={}", userResult.getId());
            return new UserResult().setMessage("账号已被禁用");
        }

        // 创建更新后的用户结果
        UserResult updatedResult = loginInfo(userResult.getUsername(), user, null);
        updatedResult.setAuthType(userResult.getAuthType());
        log.debug("用户信息刷新成功: {} (ID:{})", user.getSysUserUsername(), user.getSysUserId());
        return updatedResult;
    }

    /**
     * 构建用户登录信息
     * <p>
     * 根据数据库中的用户信息构建完整的用户结果对象，包括：
     * 用户基本信息、角色权限、部门信息、数据权限规则等。
     * 同时记录用户登录次数和最后登录IP。
     * </p>
     *
     * @param accessKey 访问标识（用户名或其他登录标识）
     * @param user      用户实体对象
     * @param address   客户端IP地址，可为null表示不更新登录信息
     * @return 完整的用户结果对象
     */
    protected UserResult loginInfo(String accessKey, SysUser user, String address) {
        // 增加用户登录计数
        ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();
        String countKey = "system:user:count:" + user.getSysUserId();
        valueOps.increment(countKey);

        // 更新用户登录信息
        if (null != address) {
            user.setSysUserLastIp(address);
            user.setSysUserLongCount(Converter.convertIfNecessary(valueOps.get(countKey), Integer.class));
            sysUserMapper.updateById(user);
        }

        // 获取用户角色信息
        long startTime = System.currentTimeMillis();
        List<SysRole> roles = getRole(user);
        log.debug("角色加载耗时: {}ms", System.currentTimeMillis() - startTime);

        // 处理角色数据
        LinkedHashSet<String> roleCodes = roles.stream()
                .map(SysRole::getSysRoleCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        LinkedHashSet<UserResult.RoleInfo> roleInfos = roles.stream()
                .map(this::convertToRoleInfo)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // 获取部门数据权限信息
        Integer sysDeptId = user.getSysDeptId();
        DataFilterTypeEnum dataPermission = ALL;
        String dataPermissionRule = "";
        String userDashboardGrid = "";
        String userDashboard = "";

        if (null != sysDeptId) {
            SysDept sysDept = sysDeptMapper.selectOne(
                    Wrappers.<SysDept>lambdaQuery().eq(SysDept::getSysDeptId, sysDeptId),
                    false
            );
            if (null != sysDept) {
                dataPermission = ObjectUtils.defaultIfNull(
                        DataFilterTypeEnum.getByCode(sysDept.getSysDeptDataPermission()),
                        ALL
                );
                dataPermissionRule = sysDept.getSysDeptDataPermissionDeptId();
            }
        }

        // 获取用户权限信息
        startTime = System.currentTimeMillis();
        Set<String> permissions = getPermission(user);
        log.debug("权限加载耗时: {}ms", System.currentTimeMillis() - startTime);

        // 构建用户结果对象
        return new UserResult()
                .setId(user.getSysUserId() + "")
                .setUserId(user.getSysUserId() + "")
                .setClientId(user.getSysClientId())
                .setOpenId(DigestUtils.md5Hex(user.getSysUserId() + user.getSysClientId()) + "SYSTEM")
                .setUnionId(DigestUtils.md5Hex(user.getSysUserId() + user.getSysClientId()))
                .setRoles(roleCodes)
                .setRolesByRole(roleInfos)
                .setDeptId(user.getSysDeptId() + "")
                .setDataPermission(dataPermission)
                .setDataPermissionRule(dataPermissionRule)
                .setPermission(permissions)
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

    /**
     * 将角色实体转换为角色信息对象
     *
     * @param role 角色实体
     * @return 角色信息对象
     */
    private UserResult.RoleInfo convertToRoleInfo(SysRole role) {
        UserResult.RoleInfo item = new UserResult.RoleInfo();
        item.setRoleCode(role.getSysRoleCode());
        item.setRoleName(role.getSysRoleName());
        item.setReadable(role.getSysRoleReadable() == null || role.getSysRoleReadable() == 1);
        item.setWriteable(role.getSysRoleWriteable() == null || role.getSysRoleWriteable() == 1);
        item.setExecutable(role.getSysRoleExecutable() == null || role.getSysRoleExecutable() == 1);
        return item;
    }

    /**
     * 获取用户角色列表
     * <p>
     * 通过多表关联查询获取用户的所有角色信息。
     * 查询结果会缓存在Redis中以提高性能。
     * </p>
     *
     * @param user 用户实体
     * @return 用户角色列表
     */
    private List<SysRole> getRole(SysUser user) {
        try {
            // 检查缓存
            String cacheKey = "system:user:roles:" + user.getSysUserId();
            if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(cacheKey))) {
                // TODO: 实现角色缓存机制
                // 此处可以添加角色缓存的反序列化逻辑
            }

            // 多表关联查询用户角色
            List<Map<String, Object>> maps = sysUserMapper.selectMaps(new MPJLambdaWrapper<SysUser>()
                    .distinct()
                    .selectAll(SysRole.class)
                    .leftJoin(SysUserRole.class, SysUserRole::getSysUserId, SysUser::getSysUserId)
                    .leftJoin(SysRole.class, SysRole::getSysRoleId, SysUserRole::getSysRoleId)
                    .eq(SysUser::getSysUserId, user.getSysUserId())
                    .eq(SysRole::getSysRoleStatus, 1)
            );

            List<SysRole> roles = maps.stream()
                    .map(it -> {
                        SysRole sysRole = new SysRole();
                        BeanUtils.copyProperties(it, sysRole);
                        return sysRole;
                    })
                    .collect(Collectors.toList());

            // 缓存角色结果
            // TODO: 添加角色缓存的序列化和存储逻辑
            // stringRedisTemplate.opsForValue().set(cacheKey, serializedRoles, ROLE_CACHE_TIMEOUT, TimeUnit.MINUTES);

            return roles;
        } catch (Exception e) {
            log.error("获取用户角色失败: {} (ID:{})", user.getSysUserUsername(), user.getSysUserId(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取用户权限集合
     * <p>
     * 查询用户的所有权限标识。
     * 查询结果会缓存在Redis中以提高性能。
     * </p>
     *
     * @param user 用户实体
     * @return 用户权限集合
     */
    private Set<String> getPermission(SysUser user) {
        try {
            // 检查缓存
            String cacheKey = "system:user:permissions:" + user.getSysUserId();
            if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(cacheKey))) {
                // TODO: 实现权限缓存机制
                // 此处可以添加权限缓存的反序列化逻辑
            }

            // 查询用户权限
            Set<String> permissions = sysUserMapper.selectPermission(user.getSysUserId());

            // 缓存权限结果
            // TODO: 添加权限缓存的序列化和存储逻辑
            // stringRedisTemplate.opsForValue().set(cacheKey, serializedPermissions, PERMISSION_CACHE_TIMEOUT, TimeUnit.MINUTES);

            return permissions;
        } catch (Exception e) {
            log.error("获取用户权限失败: {} (ID:{})", user.getSysUserUsername(), user.getSysUserId(), e);
            return Collections.emptySet();
        }
    }

    /**
     * 处理登录失败，增加错误计数
     * <p>
     * 记录用户登录失败次数，并在Redis中设置带过期时间的计数器。
     * 当错误次数达到阈值时，账户将被临时锁定。
     * </p>
     *
     * @param newAccessKey Redis中的计数器键
     * @param hasKey 键是否已存在
     * @return 包含错误信息的用户结果
     */
    private UserResult check(String newAccessKey, Boolean hasKey) {
        if (null == hasKey) {
            hasKey = false;
        }

        if (hasKey) {
            // 获取现有错误次数并递增
            Integer errorCount = NumberUtils.toInt(stringRedisTemplate.opsForValue().get(newAccessKey));
            errorCount++;
            stringRedisTemplate.opsForValue().set(newAccessKey, errorCount + "", timeout, TimeUnit.MINUTES);
            return checkLimit(errorCount);
        }

        // 第一次错误，设置初始计数为1
        stringRedisTemplate.opsForValue().set(newAccessKey, "1", timeout, TimeUnit.MINUTES);
        return checkLimit(1);
    }

    /**
     * 根据错误次数返回相应的错误信息
     * <p>
     * 根据当前错误计数返回适当的错误消息，包括：
     * 账号锁定通知、剩余尝试次数警告或一般错误提示。
     * </p>
     *
     * @param errorCount 当前错误次数
     * @return 包含错误信息的用户结果
     */
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
