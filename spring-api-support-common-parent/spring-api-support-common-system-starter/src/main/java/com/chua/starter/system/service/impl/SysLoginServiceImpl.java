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
import com.chua.starter.oauth.client.support.user.LoginAuthResult;
import com.chua.starter.oauth.client.support.execute.AuthSessionUtils;
import com.chua.starter.oauth.client.support.enums.LogoutType;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.entity.service.SysSetting;
import com.chua.starter.common.support.constant.DataFilterTypeEnum;
import com.chua.starter.entity.system.SysDept;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.service.SysSettingService;
import com.chua.starter.service.SimpleSysUserService;
import com.chua.starter.service.utils.SystemCallbackPolicy;
import com.chua.starter.system.properties.SystemSecurityProperties;
import com.chua.starter.system.support.ThirdPartyCallbackSupport;
import com.chua.starter.system.mapper.SysDeptMapper;
import com.chua.starter.system.mapper.SysRoleMapper;
import com.chua.starter.system.mapper.SysUserRoleMapper;
import com.chua.starter.entity.system.SysRole;
import com.chua.starter.entity.system.SysUserRole;
import com.chua.starter.service.dto.setting.SysSettingThirdPartyRequest;
import com.chua.starter.system.handler.LoginHandler;
import com.chua.starter.system.constant.SystemConstant;
import com.chua.starter.system.service.SysLoginService;
import com.chua.starter.system.service.SysUserLogService;
import com.chua.starter.system.service.SysUserService;
import com.chua.starter.common.support.utils.IpUtils;
import com.chua.common.support.network.net.UserAgent;
import com.chua.starter.entity.system.SysUserLog;
import com.chua.starter.system.mapper.SysUserThirdPartyMapper;
import com.chua.starter.system.mapper.SysWechatUserMapper;
import com.chua.starter.system.util.BirthdayFramePolicy;
import com.chua.starter.system.util.PasswordUtils;
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
import com.chua.starter.system.event.SysLoginLogEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.chua.starter.common.support.logger.AbstractLoggerPointcutAdvisor.X_REQ_FINGERPRINT;
import static com.chua.starter.oauth.client.support.contants.AuthConstant.CAPTCHA_SESSION_KEY;
import static com.chua.common.support.lang.code.ReturnCode.RESOURCE_CAPTCHA_ERROR;

/**
 * 登录服务实现
 *
 * @since 2024/9/12
 * @author CH
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysLoginServiceImpl implements SysLoginService {

    /**
     * 全局_setting_工厂
    */
    public static final GlobalSettingFactory GLOBAL_SETTING_FACTORY = GlobalSettingFactory.getInstance();
    /**
     * 用户_agreement_群体
    */
    private static final String USER_AGREEMENT_GROUP = "userAgreement";
    /**
     * 默认_agreement_版本
    */
    private static final String DEFAULT_AGREEMENT_VERSION = "2026.04";
    /**
     * 默认_agreement_更新_at
    */
    private static final String DEFAULT_AGREEMENT_UPDATED_AT = "2026-04-09";
    /**
     * Agreement_accepted_版本_键
    */
    private static final String AGREEMENT_ACCEPTED_VERSION_KEY = "agreementAcceptedVersion";
    /**
     * Agreement_accepted_at_键
    */
    private static final String AGREEMENT_ACCEPTED_AT_KEY = "agreementAcceptedAt";

    /**
     * 用户布局查询缓存，避免每次登录重复查询数据库
     */
    private static final Map<Long, CachedValue> USER_LAYOUT_CACHE = new ConcurrentHashMap<>();

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
        /**
         * 缓存值
        */
        private final Object value;
        /**
         * 过期时间
        */
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

    /**
     * 认证属性
    */
    private final AuthClientProperties authProperties;
    /**
     * SYS用户thirdpartymapper
    */
    private final SysUserThirdPartyMapper sysUserThirdPartyMapper;
    /**
     * syswechat用户映射器
    */
    private final SysWechatUserMapper sysWechatUserMapper;
    /**
     * Redis模板
    */
    private final RedisTemplate redisTemplate;
    /**
     * SYS设置服务
    */
    private final SysSettingService sysSettingService;
    /**
     * 简单sys用户服务
    */
    private final SimpleSysUserService simpleSysUserService;
    /**
     * SYS用户服务
    */
    private final SysUserService sysUserService;
    /**
     * SYS用户rolemapper
    */
    private final SysUserRoleMapper sysUserRoleMapper;
    /**
     * 登录角色编码缓存
     */
    private final com.chua.starter.system.support.UserRoleCache userRoleCache;
    /**
     * sysrolemapper
    */
    private final SysRoleMapper sysRoleMapper;
    /**
     * sysdeptmapper
    */
    private final SysDeptMapper sysDeptMapper;
    /**
     * Objectmapper
    */
    private final ObjectMapper objectMapper;
    /**
     * sys用户日志服务
    */
    private final SysUserLogService sysUserLogService;
    /**
     * 在线状态管理(登录写Redis在线键, 供用户列表呼吸灯)
    */
    private final com.chua.starter.system.support.OnlineStatusManager onlineStatusManager;
    /** Spring 事件发布器（发布登录日志事件，解耦认证与日志存储） */
    private final ApplicationEventPublisher eventPublisher;
    /** 系统安全配置（三方登录回调地址白名单等） */
    private final SystemSecurityProperties securityProperties;

    @Override
    /**
     * Login
    */
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
            return new LoginAuthResult(403, "账号已被禁用，请联系管理员");
        }
        // 锁定功能暂时下线：不再校验 sysUserLock（checkAndAutoUnlock），账号状态仅由 启用/禁用 控制
        // 口令校验: 与 updateOwner/reset 写入端共用 PasswordUtils, 算法不一致会导致改密后无法登录
        if (!PasswordUtils.matches(loginData.getPassword(), user.getSysUserSalt(), user.getSysUserPassword())) {
            log.info("本地登录失败: 密码错误 - {}", loginData.getUsername());
            return null;
        }

        return issueLocalToken(user);
    }

    /**
     * 为已通过身份校验的本地用户签发访问令牌（static 协议）。
     *
     * <p>密码登录在口令校验通过后、短信 / 邮箱登录在验证码校验通过后复用本方法，
     * 保证不同登录方式得到结构一致（角色 / 部门 / 数据权限 / 扩展信息）的令牌。</p>
     *
     * @param user 已认证用户
     * @return 登录认证结果
     */
    @Override
    public LoginAuthResult issueLocalToken(SysUser user) {
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
        log.info("本地登录成功: user={}", user.getSysUserUsername());

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

        // 写入Redis在线状态, 用户列表据此显示在线呼吸灯
        onlineStatusManager.markOnline(user.getSysUserId());

        return result;
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
        // 这里仅处理图形验证码；TOTP 已移到口令校验通过之后、仅对已绑定用户生效（见 verifyBoundTotp）
        var code = loginData.getVerifyCodeKey();
        if (StringUtils.isBlank(code)) {
            return null;
        }
        if (hasCaptchaContext(loginData, request)) {
            return checkVerifyCode(loginData, request);
        }
        return null;
    }

    /**
     * 登录被拒绝（如账号禁用），携带面向用户的提示。
     */
    public static class LoginRejectException extends RuntimeException {
        private final int code;

        public LoginRejectException(int code, String message) {
            super(message);
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    /**
     * 仅校验本地账号（存在 / 启用 / 口令正确），返回已认证用户；不签发令牌。
     *
     * @return 已认证用户；用户不存在或口令错误返回 null；账号禁用抛 {@link LoginRejectException}
     */
    public SysUser verifyLocalUser(LoginData loginData) {
        SysUser user = simpleSysUserService.lambdaQuery()
                .eq(SysUser::getSysUserUsername, loginData.getUsername())
                .one();
        if (user == null) {
            log.warn("本地登录失败: 用户不存在 - {}", loginData.getUsername());
            return null;
        }
        if (SystemConstant.USER_STATUS_ENABLED != user.getSysUserStatus()) {
            log.warn("本地登录失败: 账户已禁用 - {}", loginData.getUsername());
            throw new LoginRejectException(403, "账号已被禁用，请联系管理员");
        }
        if (!PasswordUtils.matches(loginData.getPassword(), user.getSysUserSalt(), user.getSysUserPassword())) {
            log.info("本地登录失败: 密码错误 - {}", loginData.getUsername());
            return null;
        }
        return user;
    }

    /**
     * 口令校验通过后，对“已绑定 TOTP”的用户校验动态码。
     *
     * @return null 表示通过或无需校验；否则返回错误（缺少动态码时消息为“请输入动态验证码”）
     */
    public ReturnResult<LoginResult> verifyBoundTotp(SysUser user, LoginData loginData) {
        if (user == null || StringUtils.isBlank(user.getSysUserTotpCode())) {
            return null;
        }
        String otp = loginData.getOtpCode();
        if (StringUtils.isBlank(otp)) {
            return ReturnResult.error(RESOURCE_CAPTCHA_ERROR.getCode(), "请输入动态验证码");
        }
        var verifyResult = simpleSysUserService.verify(otp, user.getSysUserUsername());
        if (verifyResult == null || !Boolean.TRUE.equals(verifyResult.getData())) {
            return ReturnResult.error(
                    RESOURCE_CAPTCHA_ERROR.getCode(),
                    StringUtils.defaultString(verifyResult != null ? verifyResult.getMsg() : null, "动态验证码错误或已过期")
            );
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


    @Override
    /**
     * 获取用户login信息
    */
    public UserInfoVO getUserLoginInfo() {
        UserResult userLoginInfo = AuthClientExecute.getInstance().getUserResult();
        return buildUserInfo(userLoginInfo);
    }

    @Override
    /**
     * 获取用户login信息
    */
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
    /**
     * 构建用户信息
    */
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
                    if (userInfoVO.getSysUserEmailVerified() == null) {
                        userInfoVO.setSysUserEmailVerified(sysUser.getSysUserEmailVerified());
                        userInfoVO.setSysUserEmailVerifyTime(sysUser.getSysUserEmailVerifyTime());
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
        // 生日框仅在生日前后 7 天展示：窗口外（含未设置生日）强制不渲染已佩戴的生日框
        Integer[] effective = BirthdayFramePolicy.resolveEffective(userInfoVO.getSysUserAvatarFrame(),
                userInfoVO.getSysUserBirthday(), userInfoVO.getSysUserAvatarFrameShow());
        userInfoVO.setSysUserAvatarFrame(effective[0]);
        userInfoVO.setSysUserAvatarFrameShow(effective[1]);
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
        try {
            return userRoleCache.resolve(userId, () -> new java.util.HashSet<>(sysUserRoleMapper.selectRoleCodesByUserId(userId)));
        } catch (Exception e) {
            log.warn("加载用户角色失败: {}", e.getMessage());
            return new java.util.HashSet<>();
        }
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
    /**
     * Refresh
    */
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
    /**
     * thirdlogin编码
    */
    public ReturnResult<String> thirdLoginCode(ThirdLoginData loginData, HttpServletRequest request, HttpServletResponse response) {
        log.debug("第三方登录请求: loginType={}, thirdType={}", loginData.loginType(), loginData.thirdType());

        // 开放重定向入口防御：callback 为绝对地址时必须命中白名单，否则直接拒绝，
        // 不进入 state 缓存（绑定 thirdBindCode 委托本方法，一并受保护）
        String rejectReason = SystemCallbackPolicy.rejectReason(loginData.callback(),
                securityProperties.getAllowedSystemCallbackHosts());
        if (rejectReason != null) {
            log.warn("第三方登录回调地址被拒绝: loginType={}, callback={}, reason={}",
                    loginData.loginType(), loginData.callback(), rejectReason);
            return ReturnResult.error("回调地址不合法：" + rejectReason);
        }

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
    /**
     * Third绑定编码
    */
    public ReturnResult<String> thirdBindCode(ThirdLoginData loginData, HttpServletRequest request, HttpServletResponse response) {
        return thirdLoginCode(loginData, request, response);
    }

    @Override
    /**
     * Third绑定信息
    */
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
    /**
     * Third解绑
    */
    public ReturnResult<Boolean> thirdUnbind(String userId, ThirdPartyUnbindRequest thirdPartyUnbindRequest) {
        if (StringUtils.isBlank(thirdPartyUnbindRequest.getLoginType())) {
            return ReturnResult.error("登录类型不能为空");
        }

        // 大小写不敏感匹配：绑定写入端历史遗留既有小写（gitee/wechat）也有大写（DINGTALK/QQ），
        // 入参统一按忽略大小写过滤，避免「查得到却解不掉」
        String loginType = thirdPartyUnbindRequest.getLoginType();
        List<SysUserThirdParty> bindings = sysUserThirdPartyMapper.selectList(
                Wrappers.<SysUserThirdParty>lambdaQuery()
                        .eq(SysUserThirdParty::getSysUserId, userId));
        List<Integer> matchedIds = bindings.stream()
                .filter(it -> StringUtils.isNotBlank(it.getSysUserThirdPartyType())
                        && it.getSysUserThirdPartyType().equalsIgnoreCase(loginType))
                .map(SysUserThirdParty::getSysUserThirdPartyId)
                .filter(Objects::nonNull)
                .toList();

        int deleteCount = 0;
        if (!matchedIds.isEmpty()) {
            deleteCount = matchedIds.size();
            for (Integer id : matchedIds) {
                sysUserThirdPartyMapper.deleteById(id);
            }
        }

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

    @Override
    /**
     * 三方登录换票：用回调带回的 redirectParam（AES 密文 + 一次性 nonce）换取 LoginResult
     *
     * <p>密钥仅存在服务端，前端无法解密，只能原样回传兑换；nonce 在兑换时先置失效
     * 再解密（fail-closed），同一凭证不可重复使用、伪造密文无法通过 nonce 校验。</p>
     */
    public ReturnResult<LoginResult> thirdLoginExchange(String redirectParam) {
        if (StringUtils.isBlank(redirectParam)) {
            return ReturnResult.error("登录凭证不能为空");
        }
        LoginResult loginResult = ThirdPartyCallbackSupport.redeemRedirectParam(redirectParam, securityProperties);
        if (null == loginResult || StringUtils.isBlank(loginResult.getAccessToken())) {
            log.warn("三方登录换票失败: 凭证缺失、已使用或已过期");
            return ReturnResult.error("登录凭证已失效，请重新发起第三方登录");
        }
        return ReturnResult.ok(loginResult);
    }

    /**
     * 记录一次登录事件（成功与失败均落库），供个人中心「登录日志」按人查询
     * <p>部署为 static 本地认证时认证服务器不在类路径，登录日志只能由客户端登录路径写入</p>
     *
     * @param loginData   登录请求
     * @param request     HTTP 请求
     * @param fingerprint 客户端指纹，为空时降级为 UA 生成
     * @param accessToken 认证结果，失败时可为 null
     * @param failMessage 失败原因，成功时忽略
     * @param cost        本次登录耗时（毫秒）
     */
    public void recordLoginLog(LoginData loginData, HttpServletRequest request, String fingerprint,
                               LoginAuthResult accessToken, String failMessage, long cost) {
        boolean success = null != accessToken && accessToken.getCode() == SystemConstant.HTTP_STATUS_OK;
        UserResume resume = success ? accessToken.getUserResume() : null;

        SysUserLog userLog = new SysUserLog();
        userLog.setSysLogUsername(loginData.getUsername());
        userLog.setSysLogLoginType(StringUtils.defaultString(loginData.getLoginType(), AuthType.WEB.name()));
        userLog.setSysLogFrom(success ? "LOGIN" : "LOGIN_FAIL");
        userLog.setSysLogStatus(success ? 1 : 0);
        userLog.setSysLogMessage(success ? "登录成功" : StringUtils.defaultString(failMessage, "登录失败"));
        userLog.setSysLogUrl("/v2/user/login");
        userLog.setSysLogCost(BigDecimal.valueOf(cost));
        userLog.setSysLogIp(getClientIp(request));

        String ua = request.getHeader("User-Agent");
        userLog.setSysLogUa(ua);
        userLog.setSysLogFingerprint(StringUtils.defaultString(fingerprint, IpUtils.fingerprintFromUserAgent(ua)));
        try {
            UserAgent userAgent = UserAgent.parseUserAgentString(StringUtils.nullToEmpty(ua));
            userLog.setSysLogBrowser(userAgent.getBrowser().toString());
            userLog.setSysLogSystem(userAgent.getOperatingSystem().getName());
        } catch (Exception e) {
            log.debug("登录日志解析 UA 失败: {}", ua);
        }

        fillLoginLogPrincipal(userLog, resume, loginData.getUsername());
        try {
            sysUserLogService.asyncSaveUserLog(userLog);
        } catch (Exception e) {
            log.warn("登录日志写入失败: user={}, error={}", loginData.getUsername(), e.getMessage());
        }
    }

    /**
     * 发布登录日志事件（认证侧调用），由登录日志监听器统一消费落库。
     *
     * <p>同步事件：在当前请求线程内联执行，request 仍有效。</p>
     *
     * @param loginData   登录入参
     * @param request     当前请求
     * @param fingerprint 浏览器指纹
     * @param accessToken 认证结果（成功非空，失败为 null）
     * @param failMessage 失败原因（成功为 null）
     * @param cost        本次登录耗时（毫秒）
     */
    public void publishLoginLog(LoginData loginData, HttpServletRequest request, String fingerprint,
                                LoginAuthResult accessToken, String failMessage, long cost) {
        eventPublisher.publishEvent(new SysLoginLogEvent(this, loginData, request,
                fingerprint, accessToken, failMessage, cost));
    }

    @Override
    /**
     * 记录一次登出事件，字段口径与登录日志保持一致
     */
    public void recordLogoutLog(HttpServletRequest request, String loginType, LogoutType logoutType,
                                UserResume resume, long cost) {
        if (null == resume) {
            return;
        }

        SysUserLog userLog = new SysUserLog();
        userLog.setSysLogUsername(resume.getUsername());
        userLog.setSysLogLoginType(StringUtils.defaultString(loginType, AuthType.WEB.name()));
        userLog.setSysLogFrom("LOGOUT");
        userLog.setSysLogStatus(1);
        userLog.setSysLogMessage(null == logoutType ? "LOGOUT" : logoutType.name());
        userLog.setSysLogUrl("/logout");
        userLog.setSysLogCost(BigDecimal.valueOf(cost));
        userLog.setSysLogIp(getClientIp(request));

        String ua = request.getHeader("User-Agent");
        userLog.setSysLogUa(ua);
        userLog.setSysLogFingerprint(StringUtils.defaultString(request.getHeader(X_REQ_FINGERPRINT), IpUtils.fingerprintFromUserAgent(ua)));
        try {
            UserAgent userAgent = UserAgent.parseUserAgentString(StringUtils.nullToEmpty(ua));
            userLog.setSysLogBrowser(userAgent.getBrowser().toString());
            userLog.setSysLogSystem(userAgent.getOperatingSystem().getName());
        } catch (Exception e) {
            log.debug("登出日志解析 UA 失败: {}", ua);
        }

        fillLoginLogPrincipal(userLog, resume, resume.getUsername());
        try {
            sysUserLogService.asyncSaveUserLog(userLog);
        } catch (Exception e) {
            log.warn("登出日志写入失败: user={}, error={}", resume.getUsername(), e.getMessage());
        }
    }

    /**
     * 回填登录日志的归属信息（用户ID/昵称/部门），失败登录按用户名反查，保证本人也能看到失败记录
     *
     * @param userLog  待回填的日志实体
     * @param resume   认证成功时的用户上下文，失败为 null
     * @param username 登录用户名
     */
    private void fillLoginLogPrincipal(SysUserLog userLog, UserResume resume, String username) {
        if (null != resume) {
            userLog.setCreateName(StringUtils.defaultString(resume.getUsername(), username));
            parseLoginLogId(resume.getUserId()).ifPresent(userLog::setCreateBy);
            Map<String, Object> ext = resume.getExt();
            if (null != ext) {
                parseLoginLogId(Objects.toString(ext.get("sysUserId"), null)).ifPresent(userLog::setCreateBy);
                userLog.setSysDeptId(parseLoginLogId(Objects.toString(ext.get("sysDeptId"), null)).orElse(null));
            }
            return;
        }
        userLog.setCreateName(username);
        try {
            SysUser user = simpleSysUserService.lambdaQuery()
                    .eq(SysUser::getSysUserUsername, username)
                    .one();
            if (null != user) {
                userLog.setCreateBy(user.getSysUserId());
                userLog.setSysDeptId(user.getSysDeptId());
            }
        } catch (Exception e) {
            log.debug("登录日志反查用户失败: {}", username);
        }
    }

    /**
     * 解析登录日志中的整型标识
     *
     * @param value 原始值
     * @return 解析结果，非数字时为空
     */
    private Optional<Integer> parseLoginLogId(String value) {
        if (StringUtils.isBlank(value)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.parseInt(value.trim()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
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
