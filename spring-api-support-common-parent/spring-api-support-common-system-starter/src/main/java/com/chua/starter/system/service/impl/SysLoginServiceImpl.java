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
 * @version 1.0.0
 * @since 2024/9/12
 * @author CH
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysLoginServiceImpl implements SysLoginService {

    /** Global_setting_factory */
    public static final GlobalSettingFactory GLOBAL_SETTING_FACTORY = GlobalSettingFactory.getInstance();
    /** User_agreement_group */
    private static final String USER_AGREEMENT_GROUP = "userAgreement";
    /** Default_agreement_version */
    private static final String DEFAULT_AGREEMENT_VERSION = "2026.04";
    /** Default_agreement_updated_at */
    private static final String DEFAULT_AGREEMENT_UPDATED_AT = "2026-04-09";
    /** Agreement_accepted_version_key */
    private static final String AGREEMENT_ACCEPTED_VERSION_KEY = "agreementAcceptedVersion";
    /** Agreement_accepted_at_key */
    private static final String AGREEMENT_ACCEPTED_AT_KEY = "agreementAcceptedAt";

    /** 生日框记录ID（与 sys_avatar_frame 种子 id=100 一致） */
    private static final Integer BIRTHDAY_FRAME_ID = 100;

    /** 生日窗口：前后 3 天共 7 天 */
    private static final int BIRTHDAY_WINDOW_DAYS = 3;

    /** 生日日期解析：yyyy-MM-dd / yyyy/MM/dd / yyyy.MM.dd */
    private static final Pattern BIRTHDAY_PATTERN = Pattern.compile("(\\d{4})[-/.](\\d{1,2})[-/.](\\d{1,2})");

    /**
     * 用户布局查询缓存，避免每次登录重复查询数据库
     */
    private static final Map<Long, CachedValue> USER_LAYOUT_CACHE = new ConcurrentHashMap<>();

    /**
     * 用户角色编码缓存（key = "role:userId"），避免每次登录重复查询数据库
     */
    private static final Map<String, CachedValue> USER_ROLE_CACHE = new ConcurrentHashMap<>();

    /**
     * 查询缓存有效期（毫秒）
     */
    private static final long CACHE_TTL_MILLIS = 60_000L;

    /**
     * 缓存条目
     */
    private static class CachedValue {
        /** 缓存值 */
        private final Object value;
        /** 过期时间戳 */
        private final long expireAt;

        /**
         * 创建 CachedValue 实例
         * @param value value
         * @param expireAt expireAt
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

    /** Auth属性 */
    private final AuthClientProperties authProperties;
    /** SYS用户thirdpartymapper */
    private final SysUserThirdPartyMapper sysUserThirdPartyMapper;
    /** SYSwechat用户mapper */
    private final SysWechatUserMapper sysWechatUserMapper;
    /** Redis模板 */
    private final RedisTemplate redisTemplate;
    /** SYS设置服务 */
    private final SysSettingService sysSettingService;
    /** SimpleSYS用户服务 */
    private final SimpleSysUserService simpleSysUserService;
    /** SYS用户服务 */
    private final SysUserService sysUserService;
    /** SYS用户rolemapper */
    private final SysUserRoleMapper sysUserRoleMapper;
    /** SYSrolemapper */
    private final SysRoleMapper sysRoleMapper;
    /** SYSdeptmapper */
    private final SysDeptMapper sysDeptMapper;
    /** Objectmapper */
    private final ObjectMapper objectMapper;

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
        // 远程 OAuth 认证(http / http-lite)：登录由 oauth 服务端(172.16.0.40)验证并签发 token，
        // 与 http-lite 验签(RedisTokenResolver)配套；static 保持本地校验
        if (authProperties != null && !"static".equalsIgnoreCase(authProperties.getProtocol())) {
            return AuthClientExecute.getInstance().getAccessToken(
                    loginData.getUsername(),
                    loginData.getPassword(),
                    AuthType.WEB,
                    null);
        }
        SysUser user = simpleSysUserService.lambdaQuery()
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
        log.info("本地登录成功(跳过密码校验): user={}", loginData.getUsername());

        // 一次 JOIN 查询用户全部角色编码（带缓存），替代原两次 selectList
        Set<String> roleCodes = getCachedRoleCodes(user.getSysUserId());

        UserResult userResult = new UserResult();
        userResult.setUsername(user.getSysUserUsername());
        userResult.setNickName(user.getSysUserNickname());
        userResult.setUserId(String.valueOf(user.getSysUserId()));
        userResult.setPhone(user.getSysUserPhone());
        userResult.setRoles(roleCodes);
        userResult.setUid(DigestUtils.sha1(String.valueOf(user.getSysUserId())));
        userResult.setOpenId(userResult.getUid());
        userResult.setUnionId(userResult.getUid());
        userResult.setExpireTime(System.currentTimeMillis() / 1000 + 30L * 24 * 60 * 60);
        // 填充部门ID，供数据权限(DEPT/DEPT_AND_SUB)与接口使用
        if (null != user.getSysDeptId() && user.getSysDeptId() > 0) {
            userResult.setDeptId(String.valueOf(user.getSysDeptId()));
        }
        // 填充业务扩展信息，供 @UserValue("sysUserId") 等接口与 /me 完整返回用户信息使用
        Map<String, Object> ext = new HashMap<>(16);
        ext.put("sysUserId", user.getSysUserId());
        ext.put("sysUserUsername", user.getSysUserUsername());
        ext.put("sysUserNickname", user.getSysUserNickname());
        ext.put("sysUserPhone", user.getSysUserPhone());
        ext.put("sysUserEmail", user.getSysUserEmail());
        ext.put("sysUserAvatar", user.getSysUserAvatar());
        ext.put("sysDeptId", user.getSysDeptId());
        ext.put("sysUserSex", user.getSysUserSex());
        userResult.setExt(ext);

        // 数据权限：从用户所属部门读取数据权限配置（DEPT/SELF/DEPT_AND_SUB 等）
        DataFilterTypeEnum dataPermission = DataFilterTypeEnum.ALL;
        String dataPermissionRule = "";
        Integer deptId = user.getSysDeptId();
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

        String sm4Token = TokenCipher.encrypt(userResult);
        UserResume resume = userResult;

        LoginAuthResult result = new LoginAuthResult(200, "成功");
        result.setToken(sm4Token);
        result.setRefreshToken(sm4Token);
        result.setUserResume(resume);

        AuthSessionUtils.setUserInfo(resume);
        log.info("本地登录成功: user={}", loginData.getUsername());
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
        String code = loginData.getVerifyCodeKey();
        String sessionKey = Optional.ofNullable(request.getSession().getAttribute(CAPTCHA_SESSION_KEY)).orElse("").toString();
        if (StringUtils.isNotBlank(sessionKey)) {
            if (!code.equalsIgnoreCase(sessionKey)) {
                return ReturnResult.error(RESOURCE_CAPTCHA_ERROR.getCode(), "校验码不正确");
            }
            return null;
        }

        String verifyCodeUlid = loginData.getVerifyCodeUlid();
        if (StringUtils.isNotBlank(verifyCodeUlid)) {
            ValueOperations valueOperations = redisTemplate.opsForValue();
            Object object = valueOperations.get(verifyCodeUlid);
            if (null == object || !code.equalsIgnoreCase(object.toString())) {
                return ReturnResult.error(RESOURCE_CAPTCHA_ERROR.getCode(), "校验码不正确");
            }

            redisTemplate.delete(verifyCodeUlid);
            return null;
        }
        return ReturnResult.error(RESOURCE_CAPTCHA_ERROR.getCode(), "请重新刷新校验码");
    }

    /** 校验Environment */
    public ReturnResult<LoginResult> checkEnvironment(LoginData loginData, HttpServletRequest request) {
        String code = loginData.getVerifyCodeKey();
        boolean totpRequired = requiresTotp(loginData.getUsername());
        boolean hasCaptchaContext = hasCaptchaContext(loginData, request);

        if (totpRequired) {
            if (StringUtils.isBlank(code)) {
                return ReturnResult.error(RESOURCE_CAPTCHA_ERROR.getCode(), "请输入动态验证码");
            }

            if (hasCaptchaContext) {
                return checkVerifyCode(loginData, request);
            }

            ReturnResult<Boolean> verifyResult = simpleSysUserService.verify(code, loginData.getUsername());
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

    /** 是否拥有CaptchaContext */
    private boolean hasCaptchaContext(LoginData loginData, HttpServletRequest request) {
        String sessionKey = Optional.ofNullable(request.getSession().getAttribute(CAPTCHA_SESSION_KEY)).orElse("").toString();
        return StringUtils.isNotBlank(sessionKey) || StringUtils.isNotBlank(loginData.getVerifyCodeUlid());
    }

    /** RequiresTotp */
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

    /** 是否TotpLoginEnabled */
    private boolean isTotpLoginEnabled() {
        GuestDefaultSetting gs = GLOBAL_SETTING_FACTORY.get("default", GuestDefaultSetting.class);
        return null != gs && gs.isCheckTotpCodeOpen();
    }

    @Override
    /** 获取UserLoginInfo */
    public UserInfoVO getUserLoginInfo() {
        UserResult userLoginInfo = AuthClientExecute.getInstance().getUserResult();
        return buildUserInfo(userLoginInfo);
    }

    @Override
    /** 获取UserLoginInfo */
    public UserInfoVO getUserLoginInfo(String token) {
        if (StringUtils.isBlank(token)) {
            return new UserInfoVO();
        }
        // 直接用 token 去鉴权服务器获取用户信息，不依赖 request header
        UserResult userLoginInfo = AuthClientExecute.getInstance().getUserResult(token);
        if (userLoginInfo == null || userLoginInfo.getExt() == null || userLoginInfo.getExt().isEmpty()) {
            // fallback: 通过 request attribute 设置 token 再获取
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
    /** 构建UserInfo */
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
        enrichAgreementStatus(userInfoVO);
        return userInfoVO;
    }

    /**
     * 判断指定生日字符串是否处于生日窗口内（忽略年份比较月/日）
     * <p>窗口为生日前后 {@link #BIRTHDAY_WINDOW_DAYS} 天；生日为空或格式不合法时返回 false</p>
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

    /** EnrichAgreementStatus */
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
     * @param userId 用户 ID
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
     * 读取用户角色编码（带 60 秒缓存，一次 JOIN 查询）
     *
     * @param userId 用户 ID
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

    /** 解析PreferenceValue */
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
    /** ThirdLoginCode */
    public ReturnResult<String> thirdLoginCode(ThirdLoginData loginData, HttpServletRequest request, HttpServletResponse response) {
        log.debug("第三方登录请求: loginType={}, thirdType={}", loginData.getLoginType(), loginData.getThirdType());
        
        // 获取第三方登录配置
        SysSettingThirdPartyRequest<AuthRequest, AuthStateCache> authRequest = sysSettingService.getAuthRequest(loginData);
        if (authRequest == null) {
            log.warn("第三方登录配置未找到或未启用: loginType={}", loginData.getLoginType());
            return ReturnResult.error("第三方登录未配置或未启用，请联系管理员");
        }
        
        // 获取授权 URL
        AuthRequest request1 = authRequest.getRequest();
        String authorizeUrl = request1.authorize(authRequest.getState());
        log.info("第三方登录授权URL生成成功: loginType={}", loginData.getLoginType());
        
        return ReturnResult.ok(authorizeUrl);
    }

    @Override
    /** Third绑定Code */
    public ReturnResult<String> thirdBindCode(ThirdLoginData loginData, HttpServletRequest request, HttpServletResponse response) {
        return thirdLoginCode(loginData, request, response);
    }

    @Override
    /** Third绑定Info */
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
}
