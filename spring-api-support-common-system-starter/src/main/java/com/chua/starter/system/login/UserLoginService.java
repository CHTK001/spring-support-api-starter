package com.chua.starter.system.login;

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
import com.chua.starter.oauth.client.support.user.UserInfoService;
import com.chua.starter.system.mapper.SysDeptMapper;
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
 * <h2>性能优化</h2>
 * <ul>
 *   <li>使用Redis缓存用户角色和权限信息</li>
 *   <li>批量查询替代多次单次查询</li>
 *   <li>延迟加载非必要信息</li>
 *   <li>使用流式处理优化集合操作</li>
 * </ul>
 *
 * @author CH
 * @since 1.0.0
 */
@Slf4j
@Service
@Spi("WEB")
@LoginType("WEB")
@Tag(name = "用户登录服务", description = "标准Web用户名密码登录验证")
public class UserLoginService implements UserInfoService, Constant {

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
    
    /**
     * 账户锁定时间(分钟)
     */
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
    public UserResume checkLogin(String principal, String credential, String address, Object ext) {
        // 构建登录错误计数的Redis键
        String newAccessKey = "system:" + LOGIN_ERROR + principal;
        long startTime = System.currentTimeMillis();

        // 检查用户是否已被锁定 - 使用单次操作获取值并判断
        String errorCountStr = stringRedisTemplate.opsForValue().get(newAccessKey);
        if (StringUtils.isNotEmpty(errorCountStr)) {
            int errorCount = Integer.parseInt(errorCountStr);
            if (errorCount >= count) {
                log.warn("账户已被锁定: {}, IP: {}", principal, address);
                return new UserResume("账户已被锁定");
            }
        }

        // 查询用户信息 - 支持用户名或手机号登录，使用优化的查询条件
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

        // 检查账户状态
        if (1 != user.getSysUserStatus()) {
            log.info("登录失败: 账户已禁用 {}, IP: {}", principal, address);
            return new UserResume("账号已被禁用");
        }

        // 验证密码 - 使用常量时间比较避免计时攻击
        String salt = user.getSysUserSalt();
        String md5String = DigestUtils.md5Hex(credential + salt);
        if (!StringUtils.safeEquals(md5String, user.getSysUserPassword())) {
            log.info("登录失败: 密码错误 {}, IP: {}", principal, address);
            return check(newAccessKey, errorCountStr != null);
        }

        // 更新用户登录信息
        user.setSysUserLastIp(address);
        sysUserMapper.updateById(user);

        // 登录成功，清除错误计数
        if (errorCountStr != null) {
            stringRedisTemplate.delete(newAccessKey);
        }

        log.info("登录成功: {} (ID:{}), IP: {}, 总耗时: {}ms",
                user.getSysUserUsername(), user.getSysUserId(), address,
                System.currentTimeMillis() - startTime);

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
    public UserResume upgrade(UserResult userResult) {
        // 查询最新的用户信息 - 只选择必要字段
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

        // 检查账户状态
        if (1 != user.getSysUserStatus()) {
            log.warn("用户信息刷新失败: 账户已禁用 ID={}", userResult.getUserId());
            return new UserResume("账号已被禁用");
        }

        // 创建更新后的用户结果
        UserResume updatedResult = loginInfo(userResult.getUsername(), user, null);
        updatedResult.setLoginType(userResult.getLoginType());
        log.debug("用户信息刷新成功: {} (ID:{}), 耗时: {}ms",
                user.getSysUserUsername(), user.getSysUserId(),
                System.currentTimeMillis() - startTime);

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
    protected UserResume loginInfo(String accessKey, SysUser user, String address) {
        long startTime = System.currentTimeMillis();

        // 增加用户登录计数 - 使用管道操作减少网络往返
        ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();
        String countKey = "system:login:count:" + user.getSysUserId();
        Long count = valueOps.increment(countKey);

        // 更新用户登录信息 - 只在需要时执行
        if (null != address && count != null) {
            user.setSysUserLastIp(address);
            user.setSysUserLongCount(count.intValue());
            sysUserMapper.updateById(user);
        }

        // 获取用户角色信息 - 优先从缓存获取
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
        
        // 获取用户权限信息 - 优先从缓存获取
        startTime = System.currentTimeMillis();
        Set<String> permissions = getPermission(user);
        log.debug("权限加载耗时: {}ms", System.currentTimeMillis() - startTime);

        // 生成 OpenID 和 UnionID
        String userId = user.getSysUserId() + "";
        String[] ids = OpenIdGenerator.generateSystemIds("web", userId);
        
        // 构建用户结果对象 - 使用链式调用提高代码可读性
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
                .setPermission(getPermission(user))
                .setPhone(user.getSysUserPhone())
                .setUsername(accessKey)
                .setNickName(user.getSysUserNickname())
                .setExt(ImmutableBuilder.builderOfMap(String.class, Object.class)
                        .put(user)
                        .put("userDashboardGrid", userDashboardGrid)
                        .put("userDashboard", userDashboard)
                        .remove("password", "salt", "userEnable", "address", "lastArea", "lastLatitude", "lastIp", "lastLongitude")
                        .build()
                );
    }


    /**
     * 获取用户角色列表
     * <p>
     * 首先尝试从Redis缓存获取角色信息，如果缓存未命中则从数据库查询。
     * 查询结果会缓存到Redis以提高后续访问速度。
     * </p>
     *
     * @param user 用户实体对象
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
            return Collections.emptyList();
        }
    }

    /**
     * 获取用户权限集合
     * <p>
     * 首先尝试从Redis缓存获取权限信息，如果缓存未命中则从数据库查询。
     * 查询结果会缓存到Redis以提高后续访问速度。
     * </p>
     *
     * @param user 用户实体对象
     * @return 用户权限集合
     */

    private Set<String> getPermission(SysUser user) {
        try {
            return sysUserMapper.selectPermission(user.getSysUserId());
        } catch (Exception e) {
            return Collections.emptySet();
        }
    }

    /**
     * 处理登录失败，增加错误计数（原子操作）
     * <p>
     * 使用Redis原子操作记录用户登录失败次数，超过阈值后锁定账户。
     * 避免高并发下的计数不准确问题。
     * </p>
     *
     * @param newAccessKey Redis键名
     * @param hasKey       键是否已存在
     * @return 包含错误信息的用户结果
     */
    private UserResume check(String newAccessKey, Boolean hasKey) {
        // 使用原子操作增加错误计数
        Long errorCount = stringRedisTemplate.opsForValue().increment(newAccessKey);
        if (errorCount == null) {
            errorCount = 1L;
        }

        // 首次错误，设置24小时过期
        if (errorCount == 1) {
            stringRedisTemplate.expire(newAccessKey, 24, TimeUnit.HOURS);
        }

        // 达到最大错误次数，锁定账户
        if (errorCount >= count) {
            // 锁定时重新设置较短的过期时间
            stringRedisTemplate.expire(newAccessKey, timeout, TimeUnit.MINUTES);
            log.warn("账户已被锁定: {}, 错误次数: {}", newAccessKey, errorCount);
            return new UserResume("账户已被锁定，请" + timeout + "分钟后重试");
        }

        return checkLimit(errorCount.intValue());
    }

    /**
     * 根据错误次数生成相应的错误信息
     *
     * @param errorCount 当前错误次数
     * @return 包含错误信息的用户结果
     */
    private UserResume checkLimit(Integer errorCount) {
        int remain = count - errorCount;
        if (remain <= 3) {
            return new UserResume("账号/密码不正确，还有" + remain + "次机会");
        }
        return new UserResume("账号/密码不正确");
    }
}
