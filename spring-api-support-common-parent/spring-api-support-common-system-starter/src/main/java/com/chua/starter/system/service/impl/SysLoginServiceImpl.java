package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.utils.BeanUtils;
import com.chua.common.support.utils.DigestUtils;
import com.google.common.base.Joiner;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.StringUtils;
import com.chua.common.support.lang.json.Json;
import com.chua.common.support.application.GlobalSettingFactory;
import com.chua.starter.service.setting.GuestDefaultSetting;
import com.chua.starter.service.setting.UserAgreementSetting;
import com.chua.starter.common.support.utils.CookieUtil;
import com.chua.starter.oauth.client.support.algorithm.TokenCipher;
import com.chua.starter.oauth.client.support.enums.UpgradeType;
import com.chua.starter.oauth.client.support.enums.AuthType;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.chua.starter.oauth.client.support.properties.AuthClientProperties;
import com.chua.starter.oauth.client.support.provider.LoginData;
import com.chua.starter.oauth.client.support.provider.ThirdLoginData;
import com.chua.starter.oauth.client.support.ratelimit.RateLimitService;
import com.chua.starter.oauth.client.support.user.LoginAuthResult;
import com.chua.starter.oauth.client.support.execute.AuthSessionUtils;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.entity.service.SysSetting;
import com.chua.starter.common.support.constant.DataFilterTypeEnum;
import com.chua.starter.entity.system.SysDept;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.service.SysSettingService;
import com.chua.starter.service.SimpleSysUserService;
import com.chua.starter.system.mapper.SysDeptMapper;
import com.chua.starter.system.mapper.SysRoleMapper;
import com.chua.starter.system.mapper.SysUserRoleMapper;
import com.chua.starter.entity.system.SysRole;
import com.chua.starter.entity.system.SysUserRole;
import com.chua.starter.service.dto.setting.SysSettingThirdPartyRequest;
import com.chua.starter.system.handler.LoginHandler;
import com.chua.starter.system.constant.SystemConstant;
import com.chua.starter.system.properties.SystemSecurityProperties;
import com.chua.starter.system.service.SysLoginService;
import com.chua.starter.system.service.SysUserService;
import com.chua.starter.system.mapper.SysUserThirdPartyMapper;
import com.chua.starter.system.mapper.SysWechatUserMapper;
import com.chua.starter.system.pojo.user.UserLayout;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.zhyd.oauth.cache.AuthStateCache;
import me.zhyd.oauth.request.AuthRequest;
import com.chua.starter.entity.system.SysUserThirdParty;
import com.chua.starter.entity.system.SysWechatUser;
import com.chua.starter.system.pojo.RefreshToken;
import com.chua.starter.system.pojo.ThirdPartyUnbindRequest;
import com.chua.starter.system.pojo.LoginResult;
import com.chua.starter.system.pojo.UserInfoVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.chua.starter.oauth.client.support.contants.AuthConstant.CAPTCHA_SESSION_KEY;
import static com.chua.common.support.lang.code.ReturnCode.RESOURCE_CAPTCHA_ERROR;

/**
* 登录服务实现
*
* @版本 1.0.0
* @since 2024/9/12
* @author CH
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysLoginServiceImpl implements SysLoginService {

    /** 全局_setting_工厂 */
    public static final GlobalSettingFactory GLOBAL_SETTING_FACTORY = GlobalSettingFactory.getInstance();
    /** 用户_agreement_群体 */
    private static final String USER_AGREEMENT_GROUP = "userAgreement";
    /** 默认_agreement_版本 */
    private static final String DEFAULT_AGREEMENT_VERSION = "2026.04";
    /** 默认_agreement_更新_at */
    private static final String DEFAULT_AGREEMENT_UPDATED_AT = "2026-04-09";
    /** Agreement_accepted_版本_键 */
    private static final String AGREEMENT_ACCEPTED_VERSION_KEY = "agreementAcceptedVersion";
    /** Agreement_accepted_at_键 */
    private static final String AGREEMENT_ACCEPTED_AT_KEY = "agreementAcceptedAt";

    /** 生日框记录标识（与 sys_avatar_帧 种子 标识=100 一致） */
    private static final Integer BIRTHDAY_FRAME_ID = 100;

    /** 生日窗口：前 3 天共 7 天 */
    private static final int BIRTHDAY_WINDOW_DAYS = 3;

    /** 生日日期解析：yyyy-MM-dd / yyyy/MM/dd / yyyy.MM.dd */
    private static final Pattern BIRTHDAY_PATTERN = Pattern.compile("(\\d{4})[-/.](\\d{1,2})[-/.](\\d{1,2})");

    /**
    * 用户布局查询缓存，避免每次登录重复查询数据库
     */
    private static final Map<Long, CachedValue> USER_LAYOUT_CACHE = new ConcurrentHashMap<>();

    /**
    * 用户角色编码缓存（键 = "角色:用户id"），避免每次登录重复查询数据库
     */
    private static final Map<String, CachedValue> USER_ROLE_CACHE = new ConcurrentHashMap<>();

    /**
    * 查询缓存有效期（毫秒）
     */
    private static final long CACHE_TTL_MILLIS = 60_000L;

    /**
    * 缓存条目
    * @author CH
    * @since 4.0.0
     */
    private static class CachedValue {
        /** 缓存值 */
        private final Object value;
        /** 过期时间 */
        private final long expireAt;

        /**
        * 创建 缓存值 实例
        * @param value 值
        * @param expireAt expireat
         */
        private CachedValue(Object value, long expireAt) {
            this.value = value;
            this.expireAt = expireAt;
        }
    }

    /**
    * 微信第三方类型
     */
    private static final String THIRD_PARTY_TYPE_WECHAT = "wechat";

    /** 认证属性 */
    private final AuthClientProperties authProperties;
    /** SYS用户thirdpartymapper */
    private final SysUserThirdPartyMapper sysUserThirdPartyMapper;
    /** syswechat用户映射器 */
    private final SysWechatUserMapper sysWechatUserMapper;
    /** Redis模板 */
    private final RedisTemplate redisTemplate;
    /** SYS设置服务 */
    private final SysSettingService sysSettingService;
    /** 简单sys用户服务 */
    private final SimpleSysUserService simpleSysUserService;
    /** SYS用户服务 */
    private final SysUserService sysUserService;
    /** SYS用户rolemapper */
    private final SysUserRoleMapper sysUserRoleMapper;
    /** sysrolemapper */
    private final SysRoleMapper sysRoleMapper;
    /** sysdeptmapper */
    private final SysDeptMapper sysDeptMapper;
    /** Objectmapper */
    private final ObjectMapper objectMapper;
    /** 系统安全配置(失败次数/锁定时长) */
    private final SystemSecurityProperties securityProperties;

    @Override
    /** Login */
    public ReturnResult<LoginResult> login(LoginData loginData, HttpServletRequest request, HttpServletResponse response, String fingerprint) {
        log.info("\n登录信息包含 \n登录账号 -> {}\n登陆方式 -> {}", loginData.getUsername(), loginData.getLoginType());
        return ServiceProvider.of(LoginHandler.class).getNewExtension(Joiner.on("-").skipNulls().join(loginData.getLoginType(), loginData.getAccountType()))
                .login(this, loginData, request, response, fingerprint);
    }

    /**
    * 本地认证登录，绕过OAuth服务器
    *
    * @param loginData  登录数据
    * @param request    HTTP请求
    * @return 登录认证结果
     */
    public LoginAuthResult authenticateLocal(LoginData loginData) {
        // 远程 OAuth 认证(http / http-lite)：登录由 oauth 服务器(172.16.0.40)验证并签发 token
        // 配合 http-lite 验签(RedisTokenResolver)；static 保持本地校验
        if (authProperties != null && !"static".equalsIgnoreCase(authProperties.getProtocol())) {
            return AuthClientExecute.getInstance().getAccessToken(
                    loginData.getUsername(),
                    loginData.getPassword(),
                    AuthType.WEB,
                    null);
        }
        var user = simpleSysUserService.lambdaQuery()
                .eq(SysUser::getSysUserUsername, loginData.getUsername())
                .one();
        if (user == null) {
            log.warn("本地登录失败: 用户不存在 - {}", loginData.getUsername());
            return null;
        }
        if (SystemConstant.USER_STATUS_ENABLED != user.getSysUserStatus()) {
            log.warn("本地登录失败: 账户已禁用 - {}", loginData.getUsername());
            return new LoginAuthResult(403, "账号已被禁用");
        }
        // 账号锁定校验: sysUserLock=1 时判断锁定期是否已过, 过期自动解锁, 未过拒绝登录
        LoginAuthResult lockCheck = checkAndAutoUnlock(user, loginData.getUsername());
        if (null != lockCheck) {
            return lockCheck;
        }
        log.info("本地登录成功(跳过密码校验): user={}", loginData.getUsername());

 // 一次 连接 查询用户全部角色编码（带缓存），替代原来的两次 选择列表
        var roleCodes = getCachedRoleCodes(user.getSysUserId());

        var userResult = new UserResult();
        userResult.setUsername(user.getSysUserUsername());
        userResult.setNickName(user.getSysUserNickname());
        userResult.setUserId(String.valueOf(user.getSysUserId()));
        userResult.setPhone(user.getSysUserPhone());
        userResult.setRoles(roleCodes);
        // 只读账号：用户任一角色 sysRoleReadonly=1 则整账号只读
        userResult.setReadonly(resolveUserReadonly(user.getSysUserId()));
        userResult.setUid(DigestUtils.sha1(String.valueOf(user.getSysUserId())));
        userResult.setOpenId(userResult.getUid());
        userResult.setUnionId(userResult.getUid());
        userResult.setExpireTime(System.currentTimeMillis() / 1000 + 30L * 24 * 60 * 60);
        // 填充部门ID，供数据权限(DEPT/DEPT_AND_SUB)与接口使用
        if (null != user.getSysDeptId() && user.getSysDeptId() > 0) {
            userResult.setDeptId(String.valueOf(user.getSysDeptId()));
        }
        // 填充业务扩展信息，供 @UserValue("sysUserId") 等接口与 /me 完整返回用户信息使用
        var ext = new HashMap<String, Object>(16);
        ext.put("sysUserId", user.getSysUserId());
        ext.put("sysUserUsername", user.getSysUserUsername());
        ext.put("sysUserNickname", user.getSysUserNickname());
        ext.put("sysUserPhone", user.getSysUserPhone());
        ext.put("sysUserEmail", user.getSysUserEmail());
        ext.put("sysUserAvatar", user.getSysUserAvatar());
        ext.put("sysDeptId", user.getSysDeptId());
        ext.put("sysUserSex", user.getSysUserSex());
        // 强制修改密码标记透出到登录响应(经 UserInfoVO.mustChangePassword)
        ext.put("sysUserMustChangePassword", user.getSysUserMustChangePassword());
        userResult.setExt(ext);

 // 数据权限：从用户所属部门读取数据权限配置（DEPT/SELF/DEPT_和_SUB 等）
        var dataPermission = DataFilterTypeEnum.ALL;
        var dataPermissionRule = "";
        var deptId = user.getSysDeptId();
        if (null != deptId && deptId > 0) {
            SysDept dept = sysDeptMapper.selectById(deptId);
            if (null != dept) {
                DataFilterTypeEnum resolved = DataFilterTypeEnum.getByCode(dept.getSysDeptDataPermission());
                if (null != resolved) {
                    dataPermission = resolved;
                }
                dataPermissionRule = dept.getSysDeptDataPermissionDeptId() == null ? "" : dept.getSysDeptDataPermissionDeptId();
            }
        }
        userResult.setDataPermission(dataPermission);
        userResult.setDataPermissionRule(dataPermissionRule);

        var sm4Token = TokenCipher.encrypt(userResult);
        var resume = (UserResume) userResult;

        var result = new LoginAuthResult(200, "成功");
        result.setToken(sm4Token);
        result.setRefreshToken(sm4Token);
        result.setUserResume(resume);

        AuthSessionUtils.setUserInfo(resume);
        log.info("本地登录成功: user={}", loginData.getUsername());

        // 更新最后登录时间和IP
        try {
            SysUser updateUser = new SysUser();
            updateUser.setSysUserId(user.getSysUserId());
            updateUser.setSysUserLastLoginTime(LocalDateTime.now());
            updateUser.setSysUserLastLoginIp("127.0.0.1");
            simpleSysUserService.updateById(updateUser);
        } catch (Exception e) {
            log.warn("更新最后登录信息失败: {}", e.getMessage());
        }

        return result;
    }

    /**
    * 账号锁定校验: 若用户 sysUserLock=1, 距 sysUserLockTime 未超过锁定时长则拒绝登录,
    * 超过则自动解锁(sysUserLock=0)后放行。
    *
    * @param user     已查询到的用户
    * @param username 登录账号(日志用)
    * @return null 表示放行; 非 null 表示锁定错误结果
     */
    private LoginAuthResult checkAndAutoUnlock(SysUser user, String username) {
        if (null == user.getSysUserLock() || user.getSysUserLock() != 1) {
            return null;
        }
        int lockMinutes = securityProperties.getLockoutDurationMinutes();
        LocalDateTime lockTime = user.getSysUserLockTime();
        long lockedMinutes = (null == lockTime) ? lockMinutes : ChronoUnit.MINUTES.between(lockTime, LocalDateTime.now());
        if (null == lockTime || lockedMinutes >= lockMinutes) {
            // 锁定期已过, 自动解锁
            SysUser unlock = new SysUser();
            unlock.setSysUserId(user.getSysUserId());
            unlock.setSysUserLock(0);
            unlock.setSysUserLockTime(null);
            simpleSysUserService.updateById(unlock);
            user.setSysUserLock(0);
            user.setSysUserLockTime(null);
            log.info("账号锁定已到期, 自动解锁: {}", username);
            return null;
        }
        long remain = lockMinutes - lockedMinutes;
        log.warn("本地登录失败: 账号已锁定, 剩余{}分钟 - {}", remain, username);
        return new LoginAuthResult(423, "账号已锁定，请" + remain + "分钟后再试");
    }

    /**
    * 登录失败计数达到阈值后, 将 Redis 侧锁定镜像到数据库(sysUserLock=1, sysUserLockTime=now)。
    * <p>由 DefaultLoginHandler 在 recordLoginFailure 后调用, 失败计数与 Redis 锁定仍由 RateLimitService 负责,
    * 此处仅做 DB 持久化, 便于后台展示与登录时校验。</p>
    *
    * @param username 登录账号
     */
    public void syncDbLoginLock(String username) {
        try {
            if (StringUtils.isBlank(username)) {
                return;
            }
            if (!RateLimitService.getInstance().isAccountLocked(username)) {
                return;
            }
            SysUser user = simpleSysUserService.lambdaQuery()
                    .eq(SysUser::getSysUserUsername, username)
                    .one();
            if (null == user || Integer.valueOf(1).equals(user.getSysUserLock())) {
                return;
            }
            SysUser lock = new SysUser();
            lock.setSysUserId(user.getSysUserId());
            lock.setSysUserLock(1);
            lock.setSysUserLockTime(LocalDateTime.now());
            simpleSysUserService.updateById(lock);
            log.warn("登录失败次数达阈值, 账号已锁定(DB): {}", username);
        } catch (Exception e) {
            log.warn("同步账号锁定状态失败: {}, {}", username, e.getMessage());
        }
    }

    /**
    * 检查验证码
    *
    * @param loginData 登录数据
    * @param request   请求
    * @return 检查结果
     */
    @SuppressWarnings("ALL")
    public ReturnResult<LoginResult> checkVerifyCode(LoginData loginData, HttpServletRequest request) {
        var code = loginData.getVerifyCodeKey();
        var sessionKey = Optional.ofNullable(request.getSession().getAttribute(CAPTCHA_SESSION_KEY)).orElse("").toString();
        if (StringUtils.isNotBlank(sessionKey)) {
            if (!code.equalsIgnoreCase(sessionKey)) {
                return ReturnResult.error(RESOURCE_CAPTCHA_ERROR.getCode(), "校验码不正确");
            }
            return null;
        }

        var verifyCodeUlid = loginData.getVerifyCodeUlid();
        if (StringUtils.isNotBlank(verifyCodeUlid)) {
            var valueOperations = redisTemplate.opsForValue();
            var object = valueOperations.get(verifyCodeUlid);
            if (null == object || !code.equalsIgnoreCase(object.toString())) {
                return ReturnResult.error(RESOURCE_CAPTCHA_ERROR.getCode(), "校验码不正确");
            }

            redisTemplate.delete(verifyCodeUlid);
            return null;
        }
        return ReturnResult.error(RESOURCE_CAPTCHA_ERROR.getCode(), "请重新刷新校验码");
    }

    /**
    * 校验环境
    *
    * @param loginData login数据
    * @param request 请求
    * @return 检查环境的结果
     */
    public ReturnResult<LoginResult> checkEnvironment(LoginData loginData, HttpServletRequest request) {
        var code = loginData.getVerifyCodeKey();
        var totpRequired = requiresTotp(loginData.getUsername());
        var hasCaptchaContext = hasCaptchaContext(loginData, request);

        if (totpRequired) {
            if (StringUtils.isBlank(code)) {
                return ReturnResult.error(RESOURCE_CAPTCHA_ERROR.getCode(), "请输入动态验证码");
            }

            if (hasCaptchaContext) {
                return checkVerifyCode(loginData, request);
            }

            var verifyResult = simpleSysUserService.verify(code, loginData.getUsername());
            if (verifyResult == null || !Boolean.TRUE.equals(verifyResult.getData())) {
                return ReturnResult.error(
                        RESOURCE_CAPTCHA_ERROR.getCode(),
                        StringUtils.defaultString(verifyResult != null ? verifyResult.getMsg() : null, "动态验证码错误或已过期")
                );
            }
            return null;
        }

        if (StringUtils.isBlank(code)) {
            return null;
        }

        if (hasCaptchaContext) {
            return checkVerifyCode(loginData, request);
        }

        return null;
    }

    /**
    * 是否拥有captcha上下文
    *
    * @param loginData login数据
    * @param request 请求
    * @return 是否包含captcha上下文的结果
     */
    private boolean hasCaptchaContext(LoginData loginData, HttpServletRequest request) {
        var sessionKey = Optional.ofNullable(request.getSession().getAttribute(CAPTCHA_SESSION_KEY)).orElse("").toString();
        return StringUtils.isNotBlank(sessionKey) || StringUtils.isNotBlank(loginData.getVerifyCodeUlid());
    }

    /**
    * requirestotp
    *
    * @param username 用户名
    * @return requiresTotp的结果
     */
    private boolean requiresTotp(String username) {
        if (!isTotpLoginEnabled() || StringUtils.isBlank(username)) {
            return false;
        }

        SysUser sysUser = simpleSysUserService.lambdaQuery()
                .eq(SysUser::getSysUserUsername, username)
                .select(SysUser::getSysUserId, SysUser::getSysUserTotpCode)
                .one();
        return null != sysUser && StringUtils.isNotBlank(sysUser.getSysUserTotpCode());
    }

    /**
    * 是否totplogin已启用
    *
    * @return 是否totplogin已启用的结果
     */
    private boolean isTotpLoginEnabled() {
        GuestDefaultSetting gs = GLOBAL_SETTING_FACTORY.get("default", GuestDefaultSetting.class);
        return null != gs && gs.isCheckTotpCodeOpen();
    }

    @Override
    /** 获取用户login信息 */
    public UserInfoVO getUserLoginInfo() {
        UserResult userLoginInfo = AuthClientExecute.getInstance().getUserResult();
        return buildUserInfo(userLoginInfo);
    }

    @Override
    /** 获取用户login信息 */
    public UserInfoVO getUserLoginInfo(String token) {
        if (StringUtils.isBlank(token)) {
            return new UserInfoVO();
        }
 // 直接用 令牌 去鉴权服务器获取用户信息，不依赖 请求 头部
        UserResult userLoginInfo = AuthClientExecute.getInstance().getUserResult(token);
        if (userLoginInfo == null || userLoginInfo.getExt() == null || userLoginInfo.getExt().isEmpty()) {
 // 降级: 通过 请求 attribute 设置 令牌 再获取
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                ServletRequestAttributes attributes = (ServletRequestAttributes) requestAttributes;
                attributes.getRequest().setAttribute(authProperties.getTokenName(), token);
                com.chua.starter.oauth.client.support.execute.AuthSessionUtils.removeUserInfo();
                return getUserLoginInfo();
            }
            return new UserInfoVO();
        }
        return buildUserInfo(userLoginInfo);
    }

    @Override
    /** 构建用户信息 */
    public UserInfoVO buildUserInfo(UserResume userLoginInfo) {
        UserInfoVO userInfoVO = new UserInfoVO();
        if (userLoginInfo == null) {
            return userInfoVO;
        }
        if (userLoginInfo.getExt() != null && !userLoginInfo.getExt().isEmpty()) {
            com.chua.common.support.utils.BeanUtils.copyProperties(userLoginInfo.getExt(), userInfoVO);
        }
        if (userInfoVO.getSysUserUsername() == null) {
            userInfoVO.setSysUserUsername(userLoginInfo.getUsername());
        }
        if (userInfoVO.getSysUserNickname() == null) {
            userInfoVO.setSysUserNickname(StringUtils.isNotBlank(userLoginInfo.getNickName()) ? userLoginInfo.getNickName() : userLoginInfo.getUsername());
        }
        if (userInfoVO.getSysUserPhone() == null) {
            userInfoVO.setSysUserPhone(userLoginInfo.getPhone());
        }
        if (userInfoVO.getSysUserId() == null && StringUtils.isNotBlank(userLoginInfo.getUserId())) {
            try {
                userInfoVO.setSysUserId(Integer.parseInt(userLoginInfo.getUserId()));
            } catch (NumberFormatException ignored) {
            }
        }

        // 从数据库查询用户详细信息，补充手机号、邮箱、身份证等字段
        // 注意：OAuth 服务返回的 userId 是远程数据库的 userId，需要根据 username 查询本地数据库
        if (StringUtils.isNotBlank(userInfoVO.getSysUserUsername())) {
            try {
                SysUser sysUser = sysUserService.lambdaQuery()
                        .eq(SysUser::getSysUserUsername, userInfoVO.getSysUserUsername())
                        .one();
                if (sysUser != null) {
                    if (StringUtils.isBlank(userInfoVO.getSysUserPhone())) {
                        userInfoVO.setSysUserPhone(sysUser.getSysUserPhone());
                    }
                    if (StringUtils.isBlank(userInfoVO.getSysUserEmail())) {
                        userInfoVO.setSysUserEmail(sysUser.getSysUserEmail());
                    }
                    if (StringUtils.isBlank(userInfoVO.getSysUserCard())) {
                        userInfoVO.setSysUserCard(sysUser.getSysUserCard());
                    }
                    if (userInfoVO.getSysUserSex() == null) {
                        userInfoVO.setSysUserSex(sysUser.getSysUserSex());
                    }
                    if (StringUtils.isBlank(userInfoVO.getSysUserBirthday())) {
                        userInfoVO.setSysUserBirthday(sysUser.getSysUserBirthday());
                    }
                    // 更新本地 userId
                    userInfoVO.setSysUserId(sysUser.getSysUserId());
                }
            } catch (Exception e) {
                log.warn("从数据库查询用户信息失败", e);
            }
        }
        if (StringUtils.isBlank(userInfoVO.getAvatar())) {
            userInfoVO.setAvatar(StringUtils.toString(userLoginInfo.getOption("sysUserAvatar")));
        }
        if (userInfoVO.getSysUserAvatarFrame() == null) {
            Object val = userLoginInfo.getOption("sysUserAvatarFrame");
            if (val instanceof Number) {
                userInfoVO.setSysUserAvatarFrame(((Number) val).intValue());
            }
        }
        if (userInfoVO.getSysUserAvatarFrameShow() == null) {
            Object val = userLoginInfo.getOption("sysUserAvatarFrameShow");
            if (val instanceof Number) {
                userInfoVO.setSysUserAvatarFrameShow(((Number) val).intValue());
            } else if (val != null) {
                userInfoVO.setSysUserAvatarFrameShow(Integer.valueOf(String.valueOf(val)));
            }
        }
        if (StringUtils.isBlank(userInfoVO.getSysUserBirthday())) {
            userInfoVO.setSysUserBirthday(StringUtils.toString(userLoginInfo.getOption("sysUserBirthday")));
        }
        // 生日框仅在生日窗口内展示：窗口外（含未设置生日）强制不渲染已佩戴的生日框
        if (BIRTHDAY_FRAME_ID.equals(userInfoVO.getSysUserAvatarFrame())
                && !isBirthdayWindow(userInfoVO.getSysUserBirthday())) {
            userInfoVO.setSysUserAvatarFrame(0);
            userInfoVO.setSysUserAvatarFrameShow(0);
        }
        userInfoVO.setTenantId(userLoginInfo.getTenantId());
        userInfoVO.setRoles(userLoginInfo.getRoles());
        userInfoVO.setPerms(userLoginInfo.getPermission());
        // 强制修改密码标记透出
        if (null == userInfoVO.getMustChangePassword()) {
            Object mustChange = userLoginInfo.getOption("sysUserMustChangePassword");
            if (mustChange instanceof Number) {
                userInfoVO.setMustChangePassword(((Number) mustChange).intValue());
            } else if (null != mustChange) {
                try {
                    userInfoVO.setMustChangePassword(Integer.valueOf(String.valueOf(mustChange)));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        enrichAgreementStatus(userInfoVO);
        return userInfoVO;
    }

    /**
    * 判断指定生日字符串是否处于生日窗口内（忽略年份比较月/日）
    * <p>窗口为生日前 {@link #BIRTHDAY_WINDOW_DAYS} 天；生日为空或格式不合法时返回 false</p>
    *
    * @param birthday 生日字符串（yyyy-MM-dd 等）
    * @return 是否处于生日窗口
     */
    private boolean isBirthdayWindow(String birthday) {
        if (StringUtils.isBlank(birthday)) {
            return false;
        }
        Matcher matcher = BIRTHDAY_PATTERN.matcher(birthday);
        if (!matcher.matches()) {
            return false;
        }
        LocalDate birth = LocalDate.of(2000, Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3)));
        LocalDate now = LocalDate.now();
        LocalDate today = LocalDate.of(2000, now.getMonthValue(), now.getDayOfMonth());
        long diff = Math.abs(ChronoUnit.DAYS.between(birth, today));
        return diff <= BIRTHDAY_WINDOW_DAYS;
    }

    /**
    * enrichagreement状态
    *
    * @param userInfoVO 用户信息vo
     */
    private void enrichAgreementStatus(UserInfoVO userInfoVO) {
        UserAgreementSetting agreement = GLOBAL_SETTING_FACTORY.get(USER_AGREEMENT_GROUP, UserAgreementSetting.class);
        String agreementVersion = StringUtils.isNotBlank(agreement.getVersion()) ? agreement.getVersion() : DEFAULT_AGREEMENT_VERSION;
        String agreementUpdatedAt = StringUtils.isNotBlank(agreement.getUpdatedAt()) ? agreement.getUpdatedAt() : DEFAULT_AGREEMENT_UPDATED_AT;
        userInfoVO.setAgreementVersion(agreementVersion);
        userInfoVO.setAgreementUpdatedAt(agreementUpdatedAt);
        userInfoVO.setAgreementAcceptedVersion("");
        userInfoVO.setAgreementAcceptedAt("");
        userInfoVO.setAgreementNeedConfirm(StringUtils.isNotBlank(agreementVersion));

        Integer sysUserId = userInfoVO.getSysUserId();
        if (sysUserId == null) {
            return;
        }

        try {
            UserLayout userLayout = getCachedUserLayout(Long.valueOf(sysUserId));
            String preference = userLayout != null ? userLayout.getPreference() : null;
            String acceptedVersion = resolvePreferenceValue(preference, AGREEMENT_ACCEPTED_VERSION_KEY);
            String acceptedAt = resolvePreferenceValue(preference, AGREEMENT_ACCEPTED_AT_KEY);
            userInfoVO.setAgreementAcceptedVersion(acceptedVersion);
            userInfoVO.setAgreementAcceptedAt(acceptedAt);
            userInfoVO.setAgreementNeedConfirm(
                    StringUtils.isNotBlank(agreementVersion) && !agreementVersion.equalsIgnoreCase(acceptedVersion)
            );
        } catch (Exception ex) {
            log.debug("[用户协议] 读取用户协议确认状态失败: userId={}", sysUserId, ex);
        }
    }

    /**
    * 读取用户布局（带 60 秒缓存）
    *
    * @param userId 用户 标识
    * @return 用户布局
     */
    private UserLayout getCachedUserLayout(Long userId) {
        CachedValue cached = USER_LAYOUT_CACHE.get(userId);
        if (cached != null && cached.expireAt > System.currentTimeMillis()) {
            return (UserLayout) cached.value;
        }
        UserLayout layout = sysUserService.getUserLayout(userId, null);
        USER_LAYOUT_CACHE.put(userId, new CachedValue(layout, System.currentTimeMillis() + CACHE_TTL_MILLIS));
        return layout;
    }

    /**
    * 读取用户角色编码（带 60 秒缓存，一次 连接 查询）
    *
    * @param userId 用户 标识
    * @return 角色编码集合
     */
    private Set<String> getCachedRoleCodes(Integer userId) {
        String key = "role:" + userId;
        CachedValue cached = USER_ROLE_CACHE.get(key);
        if (cached != null && cached.expireAt > System.currentTimeMillis()) {
            return (Set<String>) cached.value;
        }
        Set<String> roleCodes;
        try {
            roleCodes = new java.util.HashSet<>(sysUserRoleMapper.selectRoleCodesByUserId(userId));
        } catch (Exception e) {
            log.warn("加载用户角色失败: {}", e.getMessage());
            roleCodes = new java.util.HashSet<>();
        }
        USER_ROLE_CACHE.put(key, new CachedValue(roleCodes, System.currentTimeMillis() + CACHE_TTL_MILLIS));
        return roleCodes;
    }

    /**
    * 判断用户是否只读账号(任一角色 sysRoleReadonly=1 即只读)
    *
    * @param userId 用户ID
    * @return 是否只读
     */
    private boolean resolveUserReadonly(Integer userId) {
        try {
            return sysRoleMapper.listByUserId(java.util.Collections.singleton(userId)).stream()
                    .anyMatch(r -> r.getSysRoleReadonly() != null && r.getSysRoleReadonly() == 1);
        } catch (Exception e) {
            log.warn("判断用户只读标记失败: userId={}, {}", userId, e.getMessage());
            return false;
        }
    }

    /**
    * 解析preference值
    *
    * @param preference preference
    * @param key 键
    * @return resolvepreference值的结果
     */
    private String resolvePreferenceValue(String preference, String key) {
        if (StringUtils.isBlank(preference) || StringUtils.isBlank(key)) {
            return "";
        }
        try {
            JsonNode rootNode = objectMapper.readTree(preference);
            return rootNode.path(key).asText("");
        } catch (Exception ex) {
            log.debug("[用户协议] 解析用户偏好失败: key={}", key, ex);
            return "";
        }
    }

    @Override
    /** Refresh */
    public ReturnResult<LoginResult> refresh(RefreshToken refreshToken, HttpServletRequest request, HttpServletResponse response) {
        AuthClientExecute clientExecute = AuthClientExecute.getInstance();
        com.chua.starter.oauth.client.support.user.LoginResult upgrade = clientExecute.upgrade(refreshToken.getRefreshToken(), UpgradeType.REFRESH);
        if (null == upgrade) {
            return ReturnResult.error("登录已过期");
        }
        LoginResult loginResult = LoginResult.builder()
                .userInfo(buildUserInfo(upgrade.getUserResume()))
                .tokenType("Bearer")
                .refreshToken(upgrade.getRefreshToken())
                .accessToken(upgrade.getToken())
                .expires(null != upgrade.getUserResume() ? upgrade.getUserResume().getExpireTime() : null)
                .build();
        CookieUtil.set(response, "x-oauth-cookie", upgrade.getToken(), true);
        return ReturnResult.ok(loginResult);
    }

    @Override
    /** thirdlogin编码 */
    public ReturnResult<String> thirdLoginCode(ThirdLoginData loginData, HttpServletRequest request, HttpServletResponse response) {
        log.debug("第三方登录请求: loginType={}, thirdType={}", loginData.loginType(), loginData.thirdType());
        
        // 获取第三方登录配置
        var authRequest = sysSettingService.getAuthRequest(loginData);
        if (authRequest == null) {
            log.warn("第三方登录配置未找到或未启用: loginType={}", loginData.loginType());
            return ReturnResult.error("第三方登录未配置或未启用，请联系管理员");
        }
        
        // 获取授权 URL
        var request1 = authRequest.getRequest();
        var authorizeUrl = request1.authorize(authRequest.getState());
        log.info("第三方登录授权URL生成成功: loginType={}", loginData.loginType());
        
        return ReturnResult.ok(authorizeUrl);
    }

    @Override
    /** Third绑定编码 */
    public ReturnResult<String> thirdBindCode(ThirdLoginData loginData, HttpServletRequest request, HttpServletResponse response) {
        return thirdLoginCode(loginData, request, response);
    }

    @Override
    /** Third绑定信息 */
    public ReturnResult<List<String>> thirdBindInfo(String userId) {
        List<SysUserThirdParty> sysUserThirdParties = sysUserThirdPartyMapper.selectList(
                Wrappers.<SysUserThirdParty>lambdaQuery()
                        .eq(SysUserThirdParty::getSysUserId, userId));

        Set<String> result = new LinkedHashSet<>();
        sysUserThirdParties.stream()
                .map(SysUserThirdParty::getSysUserThirdPartyType)
                .filter(StringUtils::isNotBlank)
                .map(it -> it.toLowerCase(Locale.ROOT))
                .forEach(result::add);

        Long wechatCount = sysWechatUserMapper.selectCount(
                Wrappers.<SysWechatUser>lambdaQuery()
                        .eq(SysWechatUser::getSysUserId, userId));
        if (wechatCount != null && wechatCount > 0) {
            result.add(THIRD_PARTY_TYPE_WECHAT);
        }

        return ReturnResult.ok(List.copyOf(result));
    }

    @Override
    /** Third解绑 */
    public ReturnResult<Boolean> thirdUnbind(String userId, ThirdPartyUnbindRequest thirdPartyUnbindRequest) {
        if (StringUtils.isBlank(thirdPartyUnbindRequest.getLoginType())) {
            return ReturnResult.error("登录类型不能为空");
        }

        int deleteCount = sysUserThirdPartyMapper.delete(
                Wrappers.<SysUserThirdParty>lambdaQuery()
                        .eq(SysUserThirdParty::getSysUserId, userId)
                        .eq(SysUserThirdParty::getSysUserThirdPartyType, thirdPartyUnbindRequest.getLoginType()));

        if (THIRD_PARTY_TYPE_WECHAT.equalsIgnoreCase(thirdPartyUnbindRequest.getLoginType())) {
            sysWechatUserMapper.delete(
                    Wrappers.<SysWechatUser>lambdaQuery()
                            .eq(SysWechatUser::getSysUserId, userId));
        }

        if (deleteCount > 0) {
            return ReturnResult.ok();
        }
        return ReturnResult.error("解绑失败");
    }

    /**
     * 获取客户端IP地址
     * @param request 请求，不允许为 null
     * @return 结果字符串
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
