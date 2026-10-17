package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.chua.common.support.utils.BeanUtils;
import com.chua.common.support.network.ip.GeoCity;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.CollectionUtils;
import com.chua.starter.system.util.PasswordUtils;
import com.chua.common.support.utils.RandomUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.common.support.application.GlobalSettingFactory;
import com.chua.starter.common.support.service.IptablesService;
import com.chua.starter.common.support.utils.RequestUtils;
import com.chua.starter.oauth.client.support.contants.AuthConstant;
import com.chua.starter.oauth.client.support.enums.UpgradeType;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.chua.starter.oauth.client.support.properties.AuthClientProperties;
import com.chua.starter.system.mapper.SysUserMapper;
import com.chua.starter.system.mapper.SysUserLogMapper;
import com.chua.starter.system.mapper.SysUserPostMapper;
import com.chua.starter.system.mapper.SysPostMapper;
import com.chua.starter.system.mapper.SysDeptMapper;
import com.chua.starter.system.model.user.SysUserQuery;
import com.chua.starter.entity.system.*;
import com.chua.starter.system.mapper.SysRoleMapper;
import com.chua.starter.system.mapper.SysUserLayoutMapper;
import com.chua.starter.system.pojo.menu.SysMiniMenuVO;
import com.chua.starter.system.pojo.menu.SysMenuVO;
import com.chua.starter.system.pojo.user.SysUserPasswordUpdate;
import com.chua.starter.system.pojo.user.SysUserUpdate;
import com.chua.starter.system.pojo.user.SysUserVO;
import com.chua.starter.system.pojo.user.UserLayout;
import com.chua.starter.system.service.SysMiniMenuService;
import com.chua.starter.system.service.SysMenuService;
import com.chua.starter.system.service.SysUserRoleService;
import com.chua.starter.system.service.SysUserService;
import com.chua.starter.system.constant.SystemConstant;
import com.chua.starter.system.properties.SystemSecurityProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author CH
 * @since 2024/8/7
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
    final AuthClientProperties authProperties; // 认证属性
    final TransactionTemplate transactionTemplate; // transactionTemplate
    final SysRoleMapper sysRoleMapper; // sys角色映射器
    final SysUserRoleService sysUserRoleService; // sys用户角色服务
    final RestTemplate restTemplate; // restTemplate
    final RedisTemplate redisTemplate; // redisTemplate
    final SysMenuService sysMenuService; // sysmenu服务
    final SysMiniMenuService sysMiniMenuService; // sysminimenu服务
    final IptablesService iptablesService; // iptables服务
    final SysUserLayoutMapper sysUserLayoutMapper; // sys用户layout映射器
    final SysUserLogMapper sysUserLogMapper; // sys用户日志映射器
    final SysDeptMapper sysDeptMapper; // sys部门映射器
    final SysUserPostMapper sysUserPostMapper; // sys用户岗位映射器
    final SysPostMapper sysPostMapper; // syspost映射器
    final SystemSecurityProperties securityProperties; // 安全性属性
    final com.chua.starter.system.support.UserRoleCache userRoleCache; // 登录角色缓存
    final com.chua.starter.system.support.UserStateCache userStateCache; // 账号状态缓存
    final com.chua.starter.system.support.OnlineStatusManager onlineStatusManager; // 在线状态管理
    private static final GlobalSettingFactory GLOBAL_SETTING_FACTORY = GlobalSettingFactory.getInstance(); // 全局setting工厂

    @Override
    public IPage<SysUserVO> pageForUser(SysUserQuery sysUserQuery, Set<String> roles, Integer currentUserId, Integer currentDeptId) {
        // 数据权限过滤（超级管理员不过滤）
        applyDataScope(sysUserQuery, roles, currentUserId, currentDeptId);

        IPage<SysUser> sysUserPage = baseMapper.selectPageUser(sysUserQuery.createPage(), sysUserQuery);

        List<SysUser> records1 = sysUserPage.getRecords();
        Page<SysUserVO> result = new Page<>();
        List<SysUserVO> sysUserVos = records1.stream().map(it -> {
            SysUserVO item = BeanUtils.copyProperties(it, SysUserVO.class);
            normalizeLastLoginState(it, item);
            return item;
        }).collect(Collectors.toList());
        enrichLastLoginInfo(sysUserVos);
        result.setCurrent(sysUserPage.getCurrent())
                .setSize(sysUserPage.getSize())
                .setTotal(sysUserPage.getTotal())
                .setRecords(sysUserVos);
        List<SysUserVO> records = result.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return result;
        }
        Set<Integer> userIds = records.stream().map(SysUserVO::getSysUserId).collect(Collectors.toUnmodifiableSet());
        List<SysRole> sysRoles = sysRoleMapper.listByUserId(userIds);
        Map<Integer, List<SysRole>> collect = sysRoles.stream().collect(Collectors.groupingBy(SysRole::getSysUserId));
        for (SysUserVO sysUser : records) {
            sysUser.setUserRoles(collect.getOrDefault(sysUser.getSysUserId(), Collections.emptyList()));
        }
        // 填充用户岗位列表
        List<SysUserPost> userPosts = sysUserPostMapper.selectList(
                Wrappers.<SysUserPost>lambdaQuery().in(SysUserPost::getSysUserId, userIds));
        if (!CollectionUtils.isEmpty(userPosts)) {
            Set<Long> postIds = userPosts.stream().map(SysUserPost::getSysPostId).collect(Collectors.toSet());
            Map<Long, SysPost> postMap = sysPostMapper.selectBatchIds(postIds).stream()
                    .collect(Collectors.toMap(SysPost::getSysPostId, p -> p, (a, b) -> a));
            Map<Integer, List<SysPost>> postByUser = userPosts.stream()
                    .collect(Collectors.groupingBy(SysUserPost::getSysUserId,
                            Collectors.mapping(up -> postMap.get(up.getSysPostId()),
                                    Collectors.filtering(Objects::nonNull, Collectors.toList()))));
            for (SysUserVO sysUser : records) {
                sysUser.setUserPosts(postByUser.getOrDefault(sysUser.getSysUserId(), Collections.emptyList()));
            }
        }
        // 批量填充在线状态, 供头像呼吸灯展示
        Set<Integer> onlineIds = onlineStatusManager.resolveOnlineIds(
                records.stream().map(SysUserVO::getSysUserId).collect(Collectors.toList()));
        for (SysUserVO sysUser : records) {
            sysUser.setOnline(onlineIds.contains(sysUser.getSysUserId()));
        }
        return result;
    }

    /**
     * 应用数据权限过滤
     *
     * @param sysUserQuery  用户查询
     * @param roles         当前用户角色编码集合
     * @param currentUserId 当前用户ID
     * @param currentDeptId 当前用户部门ID
     */
    private void applyDataScope(SysUserQuery sysUserQuery, Set<String> roles, Integer currentUserId, Integer currentDeptId) {
        // 判断当前用户是否是 sa
        boolean isSa = roles != null && (roles.contains("SA") || roles.contains("sa"));
        
        // 非 sa 用户看不到 sa 用户
        if (!isSa) {
            sysUserQuery.setExcludeSaUser(true);
        }
        
        // 超级管理员不受数据权限限制（sa > admin，sa 可以看到所有人）
        if (roles != null && (isSa || roles.contains("ADMIN") || roles.contains("SUPER_ADMIN") || roles.contains("admin"))) {
            return;
        }
        if (currentUserId == null) {
            return;
        }

        // 查询当前用户的所有角色，取最宽松的数据权限（dataScope 最小的）
        List<SysRole> userRoles = sysRoleMapper.listByUserId(Collections.singleton(currentUserId));
        if (CollectionUtils.isEmpty(userRoles)) {
            // 无角色默认仅本人
            sysUserQuery.setDataScope(4);
            sysUserQuery.setCurrentUserId(currentUserId);
            return;
        }

        Integer minDataScope = userRoles.stream()
                .map(SysRole::getSysRoleDataScope)
                .filter(Objects::nonNull)
                .min(Integer::compareTo)
                .orElse(1);

        sysUserQuery.setDataScope(minDataScope);

        switch (minDataScope) {
            case 1 -> {
                // 全部数据，不过滤
            }
            case 2 -> {
                // 本部门数据
                sysUserQuery.setCurrentDeptId(currentDeptId);
            }
            case 3 -> {
                // 本部门及以下数据
                if (currentDeptId != null) {
                    SysDept dept = sysDeptMapper.selectById(currentDeptId);
                    if (dept != null && dept.getSysDeptTreeId() != null) {
                        sysUserQuery.setCurrentDeptTreeId(dept.getSysDeptTreeId());
                    } else {
                        sysUserQuery.setCurrentDeptId(currentDeptId);
                    }
                }
            }
            case 4 -> {
                // 仅本人数据
                sysUserQuery.setCurrentUserId(currentUserId);
            }
            case 5 -> {
                // 自定义部门数据
                List<Integer> customDeptIds = userRoles.stream()
                        .map(SysRole::getSysRoleDataScopeDeptIds)
                        .filter(StringUtils::isNotBlank)
                        .flatMap(ids -> Arrays.stream(ids.split(",")))
                        .map(String::trim)
                        .filter(StringUtils::isNotBlank)
                        .map(Integer::valueOf)
                        .distinct()
                        .collect(Collectors.toList());
                sysUserQuery.setDataScopeDeptIds(customDeptIds);
            }
            default -> {
                // 默认全部数据
            }
        }
    }

    @Override
    public ReturnResult<Boolean> delete(Long userId) {
        SysUser sysUser1 = getById(userId);
        if (null == sysUser1) {
            return ReturnResult.illegal("用户信息不存在");
        }

        if (SystemConstant.IN_SYSTEM_YES == sysUser1.getSysUserInSystem()) {
            return ReturnResult.illegal("系统账号不能删除");
        }

        return ReturnResult.of(transactionTemplate.execute(status -> {
            removeUserRelations(Collections.singletonList(userId));
            return baseMapper.physicalDeleteBatchIds(Collections.singletonList(userId)) > 0;
        }));
    }

    @Override
    public ReturnResult<SysUser> resetOwner(Long userId) {
        SysUser sysUser1 = getById(userId);
        if (null == sysUser1) {
            return ReturnResult.illegal("用户信息不存在");
        }

        String newPassword = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        sysUser1.setSysUserSalt(java.util.UUID.randomUUID().toString().replace("-", "").substring(0, securityProperties.getSaltLength()));
        sysUser1.setSysUserPassword(PasswordUtils.encrypt(newPassword, sysUser1.getSysUserSalt()));

        updateById(sysUser1);
        return ReturnResult.ok();
    }

    @Override
    public ReturnResult<String> reset(Long userId) {
        SysUser sysUser1 = getById(userId);
        if (null == sysUser1) {
            return ReturnResult.illegal("用户信息不存在");
        }

        String password = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        sysUser1.setSysUserSalt(java.util.UUID.randomUUID().toString().replace("-", "").substring(0, securityProperties.getSaltLength()));
        sysUser1.setSysUserPassword(PasswordUtils.encrypt(password, sysUser1.getSysUserSalt()));

        updateById(sysUser1);
        return ReturnResult.ok(password);
    }

    @Override
    public ReturnResult<String> resetWithPassword(Long userId, String password) {
        SysUser sysUser1 = getById(userId);
        if (null == sysUser1) {
            return ReturnResult.illegal("用户信息不存在");
        }
        if (StringUtils.isBlank(password)) {
            return ReturnResult.illegal("密码不能为空");
        }
        if (password.length() < 6) {
            return ReturnResult.illegal("密码长度不能少于6位");
        }

        sysUser1.setSysUserSalt(java.util.UUID.randomUUID().toString().replace("-", "").substring(0, securityProperties.getSaltLength()));
        sysUser1.setSysUserPassword(PasswordUtils.encrypt(password, sysUser1.getSysUserSalt()));

        updateById(sysUser1);
        return ReturnResult.ok("密码重置成功");
    }

    @Override
    public ReturnResult<SysUser> updateUser(Set<String> roles, String callerUsername, Long userId, SysUserUpdate sysUser) {
        Integer sysUserId = sysUser.getSysUserId();
        SysUser sysUser1;
        if (null == sysUserId) {
            //更新自己账号
            sysUser1 = getById(userId);
        } else {
            //更新其他账号
            if (!AuthConstant.isAdmin(roles)) {
                return ReturnResult.illegal("没有权限操作");
            }
            sysUser1 = getById(sysUserId);
        }

        // 内置 admin 超管账号：角色固定、拥有全部权限菜单，后端强制不允许修改其角色
        boolean isProtectedAdmin = AuthConstant.isBuiltinAdmin(sysUser1.getSysUserUsername());
        // 角色绑定只开放给"管理员编辑他人账号"这条路径；自助更新资料不得改动角色，避免自行提权
        boolean mayChangeRole = null != sysUserId && AuthConstant.isAdmin(roles);
        boolean roleBindingChanges = false;
        if (mayChangeRole) {
            Set<Integer> currentRoleIds = listRoleIds(sysUser1.getSysUserId());
            Set<Integer> requestedRoleIds = parseRoleIds(sysUser.getRoleIds());
            // updateRole=true 以请求集合整体替换（含清空），否则只在原绑定上追加
            Set<Integer> targetRoleIds = new HashSet<>(requestedRoleIds);
            if (!sysUser.isUpdateRole()) {
                targetRoleIds.addAll(currentRoleIds);
            }
            roleBindingChanges = !targetRoleIds.equals(currentRoleIds);
            if (roleBindingChanges) {
                if (isProtectedAdmin) {
                    return ReturnResult.illegal("内置管理员账号的角色不可修改");
                }
                Integer superAdminRoleId = superAdminRoleId();
                if (null != superAdminRoleId
                        && currentRoleIds.contains(superAdminRoleId) != targetRoleIds.contains(superAdminRoleId)
                        && !AuthConstant.isPrivileged(callerUsername, roles)) {
                    return ReturnResult.illegal("没有权限变更超级管理员角色");
                }
            }
        }

        String originalEmail = sysUser1.getSysUserEmail();
        BeanUtils.copyProperties(sysUser, sysUser1);

        if (StringUtils.isNotBlank(sysUser.getSysUserPassword())) {
            sysUser1.setSysUserSalt(java.util.UUID.randomUUID().toString().replace("-", "").substring(0, securityProperties.getSaltLength()));
            sysUser1.setSysUserPassword(PasswordUtils.encrypt(sysUser.getSysUserPassword(), sysUser1.getSysUserSalt()));
        }
        sysUser1.setSysUserUsername(null);
        sysUser1.setSysUserInSystem(null);
        // 已验证结论只能由验证码接口写入：置空让 updateById 跳过，保留库中原状态
        sysUser1.setSysUserEmailVerified(null);
        sysUser1.setSysUserEmailVerifyTime(null);
        // 换绑邮箱后旧邮箱的验证结论不再成立，回到未验证
        if (StringUtils.isNotBlank(sysUser.getSysUserEmail())
                && !sysUser.getSysUserEmail().equals(originalEmail)) {
            sysUser1.setSysUserEmailVerified(0);
        }
//        sysUser1.setUpdateTime(LocalDateTime.now());
        boolean writeRoleBinding = roleBindingChanges;
        try {
            SysUser updated = transactionTemplate.execute(status -> {
                updateById(sysUser1);
                if(writeRoleBinding && sysUser.isUpdateRole()) {
                    sysUserRoleService.remove(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getSysUserId, sysUser1.getSysUserId()));
                }
                if (writeRoleBinding && CollectionUtils.isNotEmpty(sysUser.getRoleIds())) {
                    // distinct：请求体可携带重复角色编号，直接落库会给同一用户写入重复的
                    // sys_user_role 行，进而让「角色 / 所属角色」按行数重复展示
                    sysUserRoleService.saveBatch(sysUser.getRoleIds().stream().distinct().map(role -> {
                        try {
                            SysUserRole rolsysUserRole = new SysUserRole();
                            rolsysUserRole.setSysUserId(sysUser1.getSysUserId());
                            rolsysUserRole.setSysRoleId(Integer.valueOf(role));
                            return rolsysUserRole;
                        } catch (NumberFormatException e) {
                            return null;
                        }
                    }).filter(Objects::nonNull).collect(Collectors.toList()));
                }
                // 同步用户岗位关联: 先删后插
                if (null != sysUser.getPostIds()) {
                    sysUserPostMapper.delete(Wrappers.<SysUserPost>lambdaQuery()
                            .eq(SysUserPost::getSysUserId, sysUser1.getSysUserId()));
                    if (CollectionUtils.isNotEmpty(sysUser.getPostIds())) {
                        List<SysUserPost> userPostList = sysUser.getPostIds().stream()
                                .filter(Objects::nonNull)
                                .map(postId -> {
                                    SysUserPost up = new SysUserPost();
                                    up.setSysUserId(sysUser1.getSysUserId());
                                    up.setSysPostId(postId.longValue());
                                    return up;
                                }).collect(Collectors.toList());
                                    userPostList.forEach(sysUserPostMapper::insert);
                    }
                }
                return sysUser1;
            });
            userRoleCache.evict(sysUser1.getSysUserId());
            userStateCache.evict(sysUser1.getSysUserId());
            return ReturnResult.ok(updated);
        } finally {
            AuthClientExecute.getInstance().upgrade(null, UpgradeType.VERSION);
        }
    }

    /**
     * 查询用户当前已绑定的角色标识集合。
     *
     * @param sysUserId 用户标识
     * @return 角色标识集合，无绑定时为空集合
     */
    private Set<Integer> listRoleIds(Integer sysUserId) {
        if (null == sysUserId) {
            return Collections.emptySet();
        }
        return sysUserRoleService.list(Wrappers.<SysUserRole>lambdaQuery()
                        .eq(SysUserRole::getSysUserId, sysUserId))
                .stream()
                .map(SysUserRole::getSysRoleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * 解析请求携带的角色标识列表，非数字项按无效忽略。
     *
     * @param roleIds 请求角色标识列表
     * @return 角色标识集合
     */
    private Set<Integer> parseRoleIds(List<String> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return Collections.emptySet();
        }
        Set<Integer> parsed = new HashSet<>();
        for (String roleId : roleIds) {
            if (null == roleId) {
                continue;
            }
            try {
                parsed.add(Integer.valueOf(roleId.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        return parsed;
    }

    /**
     * 解析超级管理员角色的标识。
     *
     * @return 超管角色标识，角色表中不存在时为 null
     */
    private Integer superAdminRoleId() {
        return sysRoleMapper.selectList(Wrappers.<SysRole>lambdaQuery()
                        .eq(SysRole::getSysRoleCode, AuthConstant.SUPER_ADMIN))
                .stream()
                .map(SysRole::getSysRoleId)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Override
    public ReturnResult<SysUser> saveUser(SysUserUpdate sysUserUpdate, HttpServletRequest request) {
        // 账号校验：不允许特殊字符和保留字
        String username = sysUserUpdate.getSysUserUsername() == null ? "" : sysUserUpdate.getSysUserUsername().trim();
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            return ReturnResult.illegal("账号只能使用字母、数字和下划线，不允许特殊字符");
        }
        // 不允许保留字作为账号
        String[] reservedUsernames = {"null", "void", "undefined", "admin", "sa", "root", "test", "guest", "anonymous"};
        for (String reserved : reservedUsernames) {
            if (reserved.equalsIgnoreCase(username)) {
                return ReturnResult.illegal("该名称为系统保留字，不允许作为账号");
            }
        }
        sysUserUpdate.setSysUserUsername(username);
        
        if(baseMapper.selectCount(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getSysUserUsername, sysUserUpdate.getSysUserUsername())
        ) > 0L) {
            return ReturnResult.illegal("账号已存在");
        }
        sysUserUpdate.setSysUserSalt(java.util.UUID.randomUUID().toString().replace("-", "").substring(0, securityProperties.getSaltLength()));
        sysUserUpdate.setSysUserPassword(PasswordUtils.encrypt(sysUserUpdate.getSysUserPassword(), sysUserUpdate.getSysUserSalt()));
        sysUserUpdate.setSysUserInSystem(SystemConstant.IN_SYSTEM_NO);
        sysUserUpdate.setSysUserDelete(SystemConstant.NOT_DELETED);
        sysUserUpdate.setSysUserStatus(SystemConstant.USER_STATUS_ENABLED);
        // 新建账号的邮箱一律未验证，只能由验证码接口改写
        sysUserUpdate.setSysUserEmailVerified(0);
        sysUserUpdate.setSysUserEmailVerifyTime(null);
        sysUserUpdate.setSysUserLongCount(0);
        sysUserUpdate.setSysUserRegisterIp(RequestUtils.getIpAddress(request));
        sysUserUpdate.setSysUserRegisterAddress(resolveGeoAddress(sysUserUpdate.getSysUserRegisterIp()));
        return ReturnResult.ok(transactionTemplate.execute(status -> {
            save(sysUserUpdate);
            if (CollectionUtils.isNotEmpty(sysUserUpdate.getRoleIds())) {
                sysUserRoleService.saveBatch(sysUserUpdate.getRoleIds().stream().map(role -> {
                    try {
                        SysUserRole rolsysUserRole = new SysUserRole();
                        rolsysUserRole.setSysUserId(sysUserUpdate.getSysUserId());
                        rolsysUserRole.setSysRoleId(Integer.valueOf(role));
                        return rolsysUserRole;
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList()));
            }
            // 保存用户岗位关联
            if (CollectionUtils.isNotEmpty(sysUserUpdate.getPostIds())) {
                for (Integer postId : sysUserUpdate.getPostIds()) {
                    if (null == postId) {
                        continue;
                    }
                    SysUserPost up = new SysUserPost();
                    up.setSysUserId(sysUserUpdate.getSysUserId());
                    up.setSysPostId(postId.longValue());
                    sysUserPostMapper.insert(up);
                }
            }
            return sysUserUpdate;
        }));
    }

    @Override
    public ReturnResult<List<SysMenuVO>> getUserMenu(Set<String> roles, Integer sysMenuEngine, String username) {
        SysMenu sysMenu = new SysMenu();
        sysMenu.setSysMenuEngine(null == sysMenuEngine ? 0 : sysMenuEngine);
        return sysMenuService.list(sysMenu, roles, 3, username);
    }

    @Override
    public ReturnResult<List<SysMiniMenuVO>> getUserMiniMenu(Set<String> roles, String username) {
        return sysMiniMenuService.listUserMenus(roles, username);
    }

    /**
     * resolvegeo地址。
     * @param rawIp rawIp
     * @return resolvegeo地址的结果
     */
    private String resolveGeoAddress(String rawIp) {
        String ip = normalizeClientIp(rawIp);
        if (org.apache.commons.lang3.StringUtils.isBlank(ip)) {
            return null;
        }
        try {
            ReturnResult<GeoCity> returnResult = iptablesService.transferAddress(ip);
            if (null == returnResult || !returnResult.isOk() || null == returnResult.getData()) {
                return null;
            }
            GeoCity geoCity = returnResult.getData();
            String province = Optional.ofNullable(geoCity.getProvince()).orElse("");
            String city = Optional.ofNullable(geoCity.getCity()).orElse("");
            String address = (province + " " + city).trim();
            return address.isEmpty() ? null : address;
        } catch (Exception e) {
            log.warn("[SysUser] 解析IP归属地失败: ip={}", ip, e);
            return null;
        }
    }

    /**
     * normalize客户端ip。
     * @param rawIp rawIp
     * @return normalize客户端ip的结果
     */
    private String normalizeClientIp(String rawIp) {
        if (org.apache.commons.lang3.StringUtils.isBlank(rawIp)) {
            return null;
        }
        return Arrays.stream(rawIp.split(","))
                .map(String::trim)
                .filter(org.apache.commons.lang3.StringUtils::isNotBlank)
                .findFirst()
                .orElse(null);
    }

    /**
     * normalize最后一个login状态。
     * @param source 源
     * @param target Target
     */
    private void normalizeLastLoginState(SysUser source, SysUserVO target) {
        if (null == source || null == target) {
            return;
        }
        Integer loginCount = source.getSysUserLongCount();
        if (null != loginCount && loginCount > 0) {
            return;
        }
        target.setSysUserLastIp(null);
        target.setSysUserLastAddress(null);
    }

    /**
     * enrich最后一个login信息。
     * @param records records
     */
    private void enrichLastLoginInfo(List<SysUserVO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }
        List<String> usernames = records.stream()
                .map(SysUserVO::getSysUserUsername)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .toList();
        if (CollectionUtils.isEmpty(usernames)) {
            return;
        }
        Map<String, SysUserLog> latestLogByUsername = sysUserLogMapper.listLatestLoginLogsByUsernames(usernames).stream()
                .filter(log -> StringUtils.isNotBlank(log.getSysLogUsername()))
                .collect(Collectors.toMap(
                        SysUserLog::getSysLogUsername,
                        log -> log,
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));
        if (latestLogByUsername.isEmpty()) {
            return;
        }
        for (SysUserVO record : records) {
            SysUserLog latestLog = latestLogByUsername.get(record.getSysUserUsername());
            if (null == latestLog) {
                continue;
            }
            if (StringUtils.isNotBlank(latestLog.getSysLogIp())) {
                record.setSysUserLastIp(latestLog.getSysLogIp());
            }
            if (StringUtils.isNotBlank(latestLog.getSysLogAddress())) {
                record.setSysUserLastAddress(latestLog.getSysLogAddress());
            }
            record.setSysUserLastLoginTime(latestLog.getCreateTime());
        }
    }



    @Override
    public ReturnResult<SysUser> updateOwner(Set<String> roles, Long userId, SysUserPasswordUpdate sysUserPasswordUpdate) {
        SysUser sysUser = baseMapper.selectById(userId);
        if(null == sysUser) {
            return ReturnResult.illegal("当前账号信息异常，请重新登录或者刷新后重试！");
        }

        if(null != sysUser.getSysUserStatus() && sysUser.getSysUserStatus() == SystemConstant.USER_STATUS_DISABLED) {
            return ReturnResult.illegal("当前账号已禁用，请联系管理员！");
        }

        String oldPassword = sysUserPasswordUpdate.getOldPassword();
        if (StringUtils.isNotBlank(oldPassword)) {
            if (!PasswordUtils.matches(oldPassword, sysUser.getSysUserSalt(), sysUser.getSysUserPassword())) {
                return ReturnResult.illegal("当前密码错误！");
            }
        } else {
            return ReturnResult.illegal("当前密码不能为空！");
        }

        if (StringUtils.isNotBlank(sysUserPasswordUpdate.getNewPassword())) {
            sysUser.setSysUserSalt(java.util.UUID.randomUUID().toString().replace("-", "").substring(0, securityProperties.getSaltLength()));
            sysUser.setSysUserPassword(PasswordUtils.encrypt(sysUserPasswordUpdate.getNewPassword(), sysUser.getSysUserSalt()));
        } else {
            return ReturnResult.illegal("新密码不能为空！");
        }

        try {
            updateById(sysUser);
            return ReturnResult.ok(sysUser);
        } finally {
            // 刷新OAuth用户会话信息
            AuthClientExecute.getInstance().upgrade(null, UpgradeType.VERSION);
        }
    }

    @Override
    public UserLayout getUserLayout(Long userId, Set<String> roles) {
        SysUserLayout sysUserLayout = sysUserLayoutMapper.selectOne(Wrappers.<SysUserLayout>lambdaQuery().eq(SysUserLayout::getSysLayoutUserId, userId));
        UserLayout userLayout = new UserLayout();
        if(null != sysUserLayout) {
            userLayout.setLayout(sysUserLayout.getSysLayoutLayout());
            userLayout.setGrid(sysUserLayout.getSysLayoutGrid());
            userLayout.setComponent(sysUserLayout.getSysLayoutComponent());
            userLayout.setPreference(sysUserLayout.getSysLayoutPreference());
            return userLayout;
        }
        return null;
    }

    @Override
    public UserLayout saveUserLayout(Long userId, UserLayout userLayout) {
        SysUserLayout sysUserLayout = sysUserLayoutMapper.selectOne(Wrappers.<SysUserLayout>lambdaQuery().eq(SysUserLayout::getSysLayoutUserId, userId));
        if(null == sysUserLayout) {
            sysUserLayout = new SysUserLayout();
        }
        sysUserLayout.setSysLayoutComponent(userLayout.getComponent());
        sysUserLayout.setSysLayoutGrid(userLayout.getGrid());
        sysUserLayout.setSysLayoutLayout(userLayout.getLayout());
        sysUserLayout.setSysLayoutPreference(userLayout.getPreference());
        sysUserLayout.setSysLayoutUserId(Math.toIntExact(userId));

        if(null == sysUserLayout.getSysLayoutId()) {
            sysUserLayoutMapper.insert(sysUserLayout);
        } else {
            sysUserLayoutMapper.updateById(sysUserLayout);
        }

        return userLayout;
    }

    @Override
    public void exportUsers(SysUserQuery query, Set<String> roles, jakarta.servlet.http.HttpServletResponse response) {
        try {
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=users_" + System.currentTimeMillis() + ".csv");
            response.setCharacterEncoding("UTF-8");
            var outputStream = response.getOutputStream();
 // 统一走 输出流，避免 获取输出流/获取writer 混用触发 illegal状态异常
            outputStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
            java.io.PrintWriter writer = new java.io.PrintWriter(
                    new java.io.OutputStreamWriter(outputStream, java.nio.charset.StandardCharsets.UTF_8),
                    false
            );
            // CSV头
            writer.println("用户名,昵称,邮箱,手机号,性别,部门,角色,状态,备注");
            
            // 查询数据
            query.setPageSize(securityProperties.getMaxExportCount()); // 限制最大导出数量
            IPage<SysUserVO> page = pageForUser(query, roles, null, null);
            
            for (SysUserVO user : page.getRecords()) {
                StringBuilder line = new StringBuilder();
                line.append(escapeCsv(user.getSysUserUsername())).append(",");
                line.append(escapeCsv(user.getSysUserNickname())).append(",");
                line.append(escapeCsv(user.getSysUserEmail())).append(",");
                line.append(escapeCsv(user.getSysUserPhone())).append(",");
                line.append(user.getSysUserSex() == null ? "" : (user.getSysUserSex() == 0 ? "女" : "男")).append(",");
                line.append("").append(",");
                String roleNames = user.getUserRoles() != null ? 
                        user.getUserRoles().stream().map(SysRole::getSysRoleName).collect(Collectors.joining(";")) : "";
                line.append(escapeCsv(roleNames)).append(",");
                line.append(user.getSysUserStatus() == null ? "" : (user.getSysUserStatus() == SystemConstant.USER_STATUS_ENABLED ? "启用" : "禁用")).append(",");
                line.append(escapeCsv(user.getSysUserRemark()));
                writer.println(line.toString());
            }
            writer.flush();
        } catch (Exception e) {
            log.error("导出用户失败", e);
            throw new RuntimeException("导出失败: " + e.getMessage());
        }
    }

    @Override
    public void downloadTemplate(jakarta.servlet.http.HttpServletResponse response) {
        try {
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=user_import_template.csv");
            response.setCharacterEncoding("UTF-8");
            var outputStream = response.getOutputStream();
            outputStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
            java.io.PrintWriter writer = new java.io.PrintWriter(
                    new java.io.OutputStreamWriter(outputStream, java.nio.charset.StandardCharsets.UTF_8),
                    false
            );
            // CSV头
            writer.println("用户名(*),昵称(*),邮箱,手机号,性别(0男/1女),部门ID,角色编码(多个用;分隔),状态(0禁用/1启用),备注");
            // 示例数据
            writer.println("zhangsan,张三,zhangsan@example.com,13800138000,0,1,admin;user,1,测试用户");
            writer.flush();
        } catch (Exception e) {
            log.error("下载模板失败", e);
            throw new RuntimeException("下载模板失败: " + e.getMessage());
        }
    }

    @Override
    public ReturnResult<String> importUsers(org.springframework.web.multipart.MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ReturnResult.illegal("请上传文件");
        }
        
        int successCount = 0;
        int failCount = 0;
        StringBuilder errors = new StringBuilder();
        
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(file.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            int lineNum = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNum++;
                // 跳过头行
                if (lineNum == 1) {
                    continue;
                }
                
                // 跳过空行
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                try {
                    String[] fields = parseCsvLine(line);
                    if (fields.length < 2) {
                        errors.append("第").append(lineNum).append("行: 数据格式错误\n");
                        failCount++;
                        continue;
                    }
                    
                    String username = fields[0].trim();
                    String nickname = fields[1].trim();
                    
                    if (username.isEmpty() || nickname.isEmpty()) {
                        errors.append("第").append(lineNum).append("行: 用户名或昵称不能为空\n");
                        failCount++;
                        continue;
                    }
                    
                    // 检查用户名是否已存在
                    if (baseMapper.selectCount(Wrappers.<SysUser>lambdaQuery()
                            .eq(SysUser::getSysUserUsername, username)) > 0) {
                        errors.append("第").append(lineNum).append("行: 用户名").append(username).append("已存在\n");
                        failCount++;
                        continue;
                    }
                    
                    // 创建用户
                    SysUser sysUser = new SysUser();
                    sysUser.setSysUserUsername(username);
                    sysUser.setSysUserNickname(nickname);
                    sysUser.setSysUserEmail(fields.length > 2 ? fields[2].trim() : null);
                    sysUser.setSysUserPhone(fields.length > 3 ? fields[3].trim() : null);
                    sysUser.setSysUserSex(fields.length > 4 && !fields[4].trim().isEmpty() ? 
                            Integer.parseInt(fields[4].trim()) : 0);
                    sysUser.setSysDeptId(fields.length > 5 && !fields[5].trim().isEmpty() ? 
                            Integer.parseInt(fields[5].trim()) : null);
                    sysUser.setSysUserStatus(fields.length > 7 && !fields[7].trim().isEmpty() ? 
                            Integer.parseInt(fields[7].trim()) : SystemConstant.USER_STATUS_ENABLED);
                    sysUser.setSysUserRemark(fields.length > 8 ? fields[8].trim() : null);
                    
                    // 设置默认密码
                    sysUser.setSysUserSalt(java.util.UUID.randomUUID().toString().replace("-", "").substring(0, securityProperties.getSaltLength()));
                    sysUser.setSysUserPassword(PasswordUtils.encrypt(securityProperties.getDefaultImportPassword(), sysUser.getSysUserSalt()));
                    sysUser.setSysUserInSystem(SystemConstant.IN_SYSTEM_NO);
                    sysUser.setSysUserDelete(SystemConstant.NOT_DELETED);
                    
                    save(sysUser);
                    
                    // 处理角色
                    if (fields.length > 6 && !fields[6].trim().isEmpty()) {
                        String[] roleNames = fields[6].split(";");
                        for (String roleName : roleNames) {
                            SysRole role = sysRoleMapper.selectOne(Wrappers.<SysRole>lambdaQuery()
                                    .eq(SysRole::getSysRoleCode, roleName.trim()));
                            if (role != null) {
                                SysUserRole userRole = new SysUserRole();
                                userRole.setSysUserId(sysUser.getSysUserId());
                                userRole.setSysRoleId(role.getSysRoleId());
                                sysUserRoleService.save(userRole);
                            }
                        }
                    }
                    
                    successCount++;
                } catch (Exception e) {
                    errors.append("第").append(lineNum).append("行: ").append(e.getMessage()).append("\n");
                    failCount++;
                }
            }
        } catch (Exception e) {
            log.error("导入用户失败", e);
            return ReturnResult.error("导入失败: " + e.getMessage());
        }
        
        String result = "导入完成！成功: " + successCount + " 条，失败: " + failCount + " 条";
        if (errors.length() > 0) {
            result += "\n错误详情:\n" + errors.toString();
        }
        return ReturnResult.ok(result);
    }

    /**
     * CSV字段转义
     * @param value 值
     * @return escapeCsv的结果
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * 解析CSV行
     * @param line 线
     * @return 解析csv线的结果
     */
    private String[] parseCsvLine(String line) {
        java.util.List<String> result = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        
        return result.toArray(new String[0]);
    }

    @Override
    public ReturnResult<Boolean> deleteBatch(List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return ReturnResult.illegal("请选择要删除的用户");
        }
        // 不允许删除超级管理员与系统内置账号
        List<SysUser> users = listByIds(userIds);
        for (SysUser user : users) {
            if ("admin".equalsIgnoreCase(user.getSysUserUsername())) {
                return ReturnResult.illegal("不允许删除超级管理员账号");
            }
            if (isInSystem(user)) {
                return ReturnResult.illegal("系统账号不能删除: " + user.getSysUserUsername());
            }
        }
        boolean success = transactionTemplate.execute(status -> {
            removeUserRelations(userIds);
            return baseMapper.physicalDeleteBatchIds(userIds) > 0;
        });
        return success ? ReturnResult.ok(true) : ReturnResult.error("删除失败");
    }

    /**
     * 是否为系统内置账号，内置账号不允许删除
     *
     * @param user 用户
     * @return 内置账号返回 true
     */
    private boolean isInSystem(SysUser user) {
        return null != user.getSysUserInSystem() && SystemConstant.IN_SYSTEM_YES == user.getSysUserInSystem();
    }

    /**
     * 清理用户的成员关系（角色、岗位），避免删除用户后残留孤儿关联行
     *
     * @param userIds 用户主键集合
     */
    private void removeUserRelations(List<Long> userIds) {
        sysUserRoleService.remove(Wrappers.<SysUserRole>lambdaQuery().in(SysUserRole::getSysUserId, userIds));
        sysUserPostMapper.delete(Wrappers.<SysUserPost>lambdaQuery().in(SysUserPost::getSysUserId, userIds));
        java.util.List<Integer> evictIds = userIds.stream().filter(Objects::nonNull).map(Long::intValue).collect(Collectors.toList());
        userRoleCache.evict(evictIds);
        userStateCache.evict(evictIds);
    }

    @Override
    public ReturnResult<Boolean> updateStatusBatch(List<Long> userIds, Integer status) {
        if (CollectionUtils.isEmpty(userIds)) {
            return ReturnResult.illegal("请选择要操作的用户");
        }
        if (status == null || (status != 0 && status != 1)) {
            return ReturnResult.illegal("状态值不合法");
        }
        // 禁用会直接切断登录入口，内置账号必须与删除保护同等拦截，否则可把唯一管理账号锁在系统外
        if (status == 0) {
            for (SysUser user : listByIds(userIds)) {
                if ("admin".equalsIgnoreCase(user.getSysUserUsername())) {
                    return ReturnResult.illegal("不允许禁用超级管理员账号");
                }
                if (isInSystem(user)) {
                    return ReturnResult.illegal("系统账号不能禁用: " + user.getSysUserUsername());
                }
            }
        }
        boolean success = update(Wrappers.<SysUser>lambdaUpdate()
                .set(SysUser::getSysUserStatus, status)
                .in(SysUser::getSysUserId, userIds));
        if (success) {
            userStateCache.evict(userIds.stream().filter(Objects::nonNull).map(Long::intValue).collect(Collectors.toList()));
        }
        return success ? ReturnResult.ok(true) : ReturnResult.error("更新失败");
    }
}


