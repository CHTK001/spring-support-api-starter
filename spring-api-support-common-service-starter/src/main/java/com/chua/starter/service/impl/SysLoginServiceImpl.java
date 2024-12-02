package com.chua.starter.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.application.GlobalSettingFactory;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.common.support.utils.CookieUtil;
import com.chua.starter.oauth.client.support.enums.UpgradeType;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.chua.starter.oauth.client.support.properties.AuthClientProperties;
import com.chua.starter.oauth.client.support.provider.LoginData;
import com.chua.starter.oauth.client.support.provider.ThirdLoginData;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.service.SysLoginService;
import com.chua.starter.service.SysSettingService;
import com.chua.starter.service.impl.login.LoginHandler;
import com.chua.starter.service.mapper.SysUserThirdPartyMapper;
import com.chua.starter.system.dto.setting.SysSettingThirdPartyRequest;
import com.chua.starter.system.entity.SysUserThirdParty;
import com.chua.starter.system.pojo.user.RefreshToken;
import com.chua.starter.system.pojo.user.ThirdPartyUnbindRequest;
import com.chua.starter.system.result.LoginResult;
import com.chua.starter.system.result.UserInfoVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.cache.AuthStateCache;
import me.zhyd.oauth.request.AuthRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Optional;

import static com.chua.common.support.constant.CommonConstant.EMPTY;
import static com.chua.common.support.constant.Constants.CAPTCHA_SESSION_KEY;
import static com.chua.common.support.lang.code.ReturnCode.RESOURCE_CAPTCHA_ERROR;

/**
 * 登录接口实现
 * @author CH
 * @since 2024/9/12
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysLoginServiceImpl implements SysLoginService {

    final AuthClientProperties authProperties;
    final TransactionTemplate transactionTemplate;
    final RestTemplate restTemplate;
    final SysUserThirdPartyMapper sysUserThirdPartyMapper;
    final RedisTemplate redisTemplate;
    final SysSettingService sysSettingService;
    public static final GlobalSettingFactory GLOBAL_SETTING_FACTORY = GlobalSettingFactory.getInstance();


    @Override
    public Result<LoginResult> login(LoginData loginData, HttpServletRequest request, HttpServletResponse response) {
        return ServiceProvider.of(LoginHandler.class).getNewExtension(loginData.getLoginType())
                .login(this, loginData, request, response);
    }

    @SuppressWarnings("ALL")
    public Result<LoginResult> checkVerifyCode(LoginData loginData, HttpServletRequest request) {
        String code = loginData.getVerifyCodeKey();
        String sessionKey = Optional.ofNullable(request.getSession().getAttribute(CAPTCHA_SESSION_KEY)).orElse(EMPTY).toString();
        if (StringUtils.isNotBlank(sessionKey)) {
            if (!code.equalsIgnoreCase(sessionKey)) {
                return Result.failed(RESOURCE_CAPTCHA_ERROR, "校验码不正确");
            }
            return null;
        }

        String verifyCodeUlid = loginData.getVerifyCodeUlid();
        if (StringUtils.isNotBlank(verifyCodeUlid)) {
            ValueOperations valueOperations = redisTemplate.opsForValue();
            Object object = valueOperations.get(verifyCodeUlid);
            if (null == object || !code.equalsIgnoreCase(object.toString())) {
                return Result.failed(RESOURCE_CAPTCHA_ERROR, "校验码不正确");
            }

            redisTemplate.delete(verifyCodeUlid);
            return null;
        }
        return Result.failed(RESOURCE_CAPTCHA_ERROR, "请重新刷新校验码");
    }

    @Override
    public UserInfoVO getUserLoginInfo() {
        UserResult userLoginInfo = AuthClientExecute.getInstance().getUserResult();
        UserInfoVO userInfoVO = new UserInfoVO();
        com.chua.common.support.bean.BeanUtils.copyProperties(userLoginInfo.getExt(), userInfoVO);
        userInfoVO.setAvatar(userLoginInfo.getExtValue("sysUserAvatar"));
        userInfoVO.setTenantId(userLoginInfo.getTenantId());
        userInfoVO.setRoles(userLoginInfo.getRoles());
        userInfoVO.setPerms(userLoginInfo.getPermission());
        return userInfoVO;
    }
    @Override
    public Result<LoginResult> refresh(RefreshToken refreshToken, HttpServletRequest request, HttpServletResponse response) {
        AuthClientExecute clientExecute = AuthClientExecute.getInstance();
        com.chua.starter.oauth.client.support.user.LoginResult upgrade = clientExecute.upgrade(UpgradeType.REFRESH);
        LoginResult loginResult = LoginResult.builder()
                .userInfo(getUserLoginInfo(upgrade.getToken()))
                .tokenType("Bearer")
                .accessToken(upgrade.getToken())
                .build();
        CookieUtil.set(response, "x-oauth-cookie", upgrade.getToken(), true);
        return Result.success(loginResult);
    }

    @Override
    public ReturnResult<String> thirdLoginCode(ThirdLoginData loginData, HttpServletRequest request, HttpServletResponse response) {
        SysSettingThirdPartyRequest<AuthRequest, AuthStateCache> authRequest = sysSettingService.getAuthRequest(loginData);
        if(null == authRequest) {
            return ReturnResult.fail("第三方登录暂不支持");
        }
        long startTime = System.currentTimeMillis();
        AuthRequest requestRequest = authRequest.getRequest();
        String authorize = requestRequest.authorize(authRequest.getState());

        log.info("登录耗时:{}ms", System.currentTimeMillis() - startTime);
        return ReturnResult.ok(authorize);
    }

    @Override
    public ReturnResult<String> thirdBindCode(ThirdLoginData loginData, HttpServletRequest request, HttpServletResponse response) {
        return thirdLoginCode(loginData, request, response);
    }

    @Override
    public ReturnResult<List<String>> thirdBindInfo(String userId) {
        List<SysUserThirdParty> sysUserThirdParties = sysUserThirdPartyMapper.selectList(Wrappers.<SysUserThirdParty>lambdaQuery().eq(SysUserThirdParty::getSysUserId, userId));
        return ReturnResult.ok(sysUserThirdParties.stream().map(SysUserThirdParty::getSysUserThirdPartyType).distinct().toList());
    }

    @Override
    public ReturnResult<Boolean> thirdUnbind(String userId, ThirdPartyUnbindRequest thirdPartyUnbindRequest) {
        if(StringUtils.isBlank(thirdPartyUnbindRequest.getLoginType())) {
            return ReturnResult.fail("登录类型不能为空");
        }
        if(sysUserThirdPartyMapper.delete(Wrappers.<SysUserThirdParty>lambdaQuery().eq(SysUserThirdParty::getSysUserId, userId).eq(SysUserThirdParty::getSysUserThirdPartyType, thirdPartyUnbindRequest.getLoginType())) > 0) {
            return ReturnResult.ok();
        }
        return ReturnResult.fail("解绑失败");
    }

    /**
     * 获取用户信息
     *
     * @param token token
     * @return UserInfoVO
     */
    @Override
    public UserInfoVO getUserLoginInfo(String token) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null || StringUtils.isBlank(token)) {
            return new UserInfoVO();
        }

        ServletRequestAttributes attributes = (ServletRequestAttributes) requestAttributes;
        attributes.getRequest().setAttribute(authProperties.getTokenName(), token);
        return getUserLoginInfo();
    }
}
