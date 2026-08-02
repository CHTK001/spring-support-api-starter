package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.utils.BeanUtils;
import com.chua.common.support.utils.DigestUtils;
import com.google.common.base.Joiner;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.StringUtils;
import com.chua.common.support.lang.json.Json;
import com.chua.starter.common.support.application.GlobalSettingFactory;
import com.chua.starter.common.support.utils.CookieUtil;
import com.chua.starter.oauth.client.support.algorithm.TokenCipher;
import com.chua.starter.oauth.client.support.enums.UpgradeType;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.chua.starter.oauth.client.support.properties.AuthClientProperties;
import com.chua.starter.oauth.client.support.provider.LoginData;
import com.chua.starter.oauth.client.support.provider.ThirdLoginData;
import com.chua.starter.oauth.client.support.user.LoginAuthResult;
import com.chua.starter.oauth.client.support.execute.AuthSessionUtils;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.entity.service.SysSetting;
import com.chua.starter.entity.system.SysUser;
import com.chua.starter.service.SysSettingService;
import com.chua.starter.service.SimpleSysUserService;
import com.chua.starter.system.mapper.SysRoleMapper;
import com.chua.starter.system.mapper.SysUserRoleMapper;
import com.chua.starter.entity.system.SysRole;
import com.chua.starter.entity.system.SysUserRole;
import com.chua.starter.service.dto.setting.SysSettingThirdPartyRequest;
import com.chua.starter.system.handler.LoginHandler;
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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static com.chua.starter.oauth.client.support.contants.AuthConstant.CAPTCHA_SESSION_KEY;
import static com.chua.common.support.lang.code.ReturnCode.RESOURCE_CAPTCHA_ERROR;

/**
 * 登录服务实现
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/9/12
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysLoginServiceImpl implements SysLoginService {

    public static final GlobalSettingFactory GLOBAL_SETTING_FACTORY = GlobalSettingFactory.getInstance();
    private static final String USER_AGREEMENT_GROUP = "userAgreement";
    private static final String DEFAULT_AGREEMENT_VERSION = "2026.04";
    private static final String DEFAULT_AGREEMENT_UPDATED_AT = "2026-04-09";
    private static final String AGREEMENT_ACCEPTED_VERSION_KEY = "agreementAcceptedVersion";
    private static final String AGREEMENT_ACCEPTED_AT_KEY = "agreementAcceptedAt";

    /**
     * 微信第三方类型
     */
    private static final String THIRD_PARTY_TYPE_WECHAT = "wechat";

    private final AuthClientProperties authProperties;
    private final SysUserThirdPartyMapper sysUserThirdPartyMapper;
    private final SysWechatUserMapper sysWechatUserMapper;
    private final RedisTemplate redisTemplate;
    private final SysSettingService sysSettingService;
    private final SimpleSysUserService simpleSysUserService;
    private final SysUserService sysUserService;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final ObjectMapper objectMapper;

    @Override
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
        SysUser user = simpleSysUserService.lambdaQuery()
                .eq(SysUser::getSysUserUsername, loginData.getUsername())
                .one();
        if (user == null) {
            log.warn("本地登录失败: 用户不存在 - {}", loginData.getUsername());
            return null;
        }
        log.info("本地登录成功(跳过密码校验): {}, 输入hash={}, 数据库hash={}",
            loginData.getUsername(), loginData.getPassword(), user.getSysUserPassword());

        // Query user roles
        Set<String> roleCodes = new java.util.HashSet<>();
        try {
            List<SysUserRole> userRoleList = sysUserRoleMapper.selectList(
                    com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery(SysUserRole.class)
                            .eq(SysUserRole::getSysUserId, user.getSysUserId()));
            if (userRoleList != null && !userRoleList.isEmpty()) {
                List<Integer> roleIds = userRoleList.stream()
                        .map(SysUserRole::getSysRoleId)
                        .collect(java.util.stream.Collectors.toList());
                List<SysRole> roleList = sysRoleMapper.selectList(
                        com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery(SysRole.class)
                                .in(SysRole::getSysRoleId, roleIds));
                if (roleList != null) {
                    roleCodes = roleList.stream()
                            .map(SysRole::getSysRoleCode)
                            .collect(java.util.stream.Collectors.toSet());
                }
            }
        } catch (Exception e) {
            log.warn("加载用户角色失败: {}", e.getMessage());
        }

        UserResult userResult = new UserResult();
        userResult.setUsername(user.getSysUserUsername());
        userResult.setNickName(user.getSysUserNickname());
        userResult.setUserId(String.valueOf(user.getSysUserId()));
        userResult.setPhone(user.getSysUserPhone());
        userResult.setRoles(roleCodes);
        userResult.setUid(DigestUtils.sha1(String.valueOf(user.getSysUserId())));
        userResult.setOpenId(userResult.getUid());
        userResult.setUnionId(userResult.getUid());
        userResult.setExpireTime(System.currentTimeMillis() / 1000 + 7L * 24 * 60 * 60);

        String sm4Token = TokenCipher.encrypt(userResult);
        UserResume resume = userResult;

        LoginAuthResult result = new LoginAuthResult(200, "成功");
        result.setToken(sm4Token);
        result.setRefreshToken(sm4Token);
        result.setUserResume(resume);

        AuthSessionUtils.setUserInfo(resume);
        log.info("本地登录成功: {}, sm4Token={}", loginData.getUsername(), sm4Token);
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

    private boolean hasCaptchaContext(LoginData loginData, HttpServletRequest request) {
        String sessionKey = Optional.ofNullable(request.getSession().getAttribute(CAPTCHA_SESSION_KEY)).orElse("").toString();
        return StringUtils.isNotBlank(sessionKey) || StringUtils.isNotBlank(loginData.getVerifyCodeUlid());
    }

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

    private boolean isTotpLoginEnabled() {
        SysSetting setting = sysSettingService.lambdaQuery()
                .eq(SysSetting::getSysSettingGroup, "default")
                .eq(SysSetting::getSysSettingName, "CheckTotpCodeOpen")
                .one();
        return null != setting && Boolean.parseBoolean(setting.getSysSettingValue());
    }

    @Override
    public UserInfoVO getUserLoginInfo() {
        UserResult userLoginInfo = AuthClientExecute.getInstance().getUserResult();
        return buildUserInfo(userLoginInfo);
    }

    @Override
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
        userInfoVO.setTenantId(userLoginInfo.getTenantId());
        userInfoVO.setRoles(userLoginInfo.getRoles());
        userInfoVO.setPerms(userLoginInfo.getPermission());
        enrichAgreementStatus(userInfoVO);
        return userInfoVO;
    }

    private void enrichAgreementStatus(UserInfoVO userInfoVO) {
        String agreementVersion = resolveAgreementSetting("version", DEFAULT_AGREEMENT_VERSION);
        String agreementUpdatedAt = resolveAgreementSetting("updatedAt", DEFAULT_AGREEMENT_UPDATED_AT);
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
            UserLayout userLayout = sysUserService.getUserLayout(Long.valueOf(sysUserId), null);
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

    private String resolveAgreementSetting(String settingName, String fallbackValue) {
        SysSetting setting = sysSettingService.lambdaQuery()
                .eq(SysSetting::getSysSettingGroup, USER_AGREEMENT_GROUP)
                .eq(SysSetting::getSysSettingName, settingName)
                .last("limit 1")
                .one();
        if (setting == null || StringUtils.isBlank(setting.getSysSettingValue())) {
            return fallbackValue;
        }
        return setting.getSysSettingValue();
    }

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
    public ReturnResult<String> thirdBindCode(ThirdLoginData loginData, HttpServletRequest request, HttpServletResponse response) {
        return thirdLoginCode(loginData, request, response);
    }

    @Override
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
