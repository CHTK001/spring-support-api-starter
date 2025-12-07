package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.lang.code.Result;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.Joiner;
import com.chua.starter.oauth.client.configuration.AuthClientProperties;
import com.chua.starter.oauth.client.support.enums.UpgradeType;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.chua.starter.oauth.client.support.infomation.AuthenticationInformation;
import com.chua.starter.oauth.client.support.provider.LoginData;
import com.chua.starter.oauth.client.support.provider.ThirdLoginData;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.service.mapper.SysUserThirdPartyMapper;
import com.chua.starter.service.model.SysUserThirdParty;
import com.chua.starter.service.pojo.user.RefreshToken;
import com.chua.starter.service.pojo.user.ThirdPartyUnbindRequest;
import com.chua.starter.service.result.LoginResult;
import com.chua.starter.service.result.UserInfoVO;
import com.chua.starter.system.service.SysLoginService;
import com.chua.starter.system.service.impl.login.LoginHandler;
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

import java.util.List;
import java.util.Optional;

import static com.chua.common.support.constant.CommonConstant.EMPTY;
import static com.chua.common.support.constant.Constants.CAPTCHA_SESSION_KEY;
import static com.chua.common.support.lang.code.ReturnCode.RESOURCE_CAPTCHA_ERROR;

/**
 * 鐧诲綍鎺ュ彛瀹炵幇
 *
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

    @Override
    public Result<LoginResult> login(LoginData loginData, HttpServletRequest request, HttpServletResponse response, String fingerprint) {
        log.info("\n鐧诲綍淇℃伅鍖呭惈 \n鐧诲綍璐﹀彿 -> {}\n鐧婚檰鏂瑰紡 -> {}", loginData.getUsername(), loginData.getLoginType());
        return ServiceProvider.of(LoginHandler.class).getNewExtension(Joiner.on("-").join(loginData.getLoginType(), loginData.getAccountType()))
                .login(this, loginData, request, response, fingerprint);
    }

    @SuppressWarnings("ALL")
    public Result<LoginResult> checkVerifyCode(LoginData loginData, HttpServletRequest request) {
        String code = loginData.getVerifyCodeKey();
        String sessionKey = Optional.ofNullable(request.getSession().getAttribute(CAPTCHA_SESSION_KEY)).orElse(EMPTY).toString();
        if (StringUtils.isNotBlank(sessionKey)) {
            if (!code.equalsIgnoreCase(sessionKey)) {
                return Result.failed(RESOURCE_CAPTCHA_ERROR, "鏍￠獙鐮佷笉姝ｇ‘");
            }
            return null;
        }

        String verifyCodeUlid = loginData.getVerifyCodeUlid();
        if (StringUtils.isNotBlank(verifyCodeUlid)) {
            ValueOperations valueOperations = redisTemplate.opsForValue();
            Object object = valueOperations.get(verifyCodeUlid);
            if (null == object || !code.equalsIgnoreCase(object.toString())) {
                return Result.failed(RESOURCE_CAPTCHA_ERROR, "鏍￠獙鐮佷笉姝ｇ‘");
            }

            redisTemplate.delete(verifyCodeUlid);
            return null;
        }
        return Result.failed(RESOURCE_CAPTCHA_ERROR, "璇烽噸鏂板埛鏂版牎楠岀爜");
    }

    @Override
    public UserInfoVO getUserLoginInfo() {
        UserResult userLoginInfo = AuthClientExecute.getInstance().getUserResult();
        UserInfoVO userInfoVO = new UserInfoVO();
        com.chua.common.support.bean.BeanUtils.copyProperties(userLoginInfo.getExt(), userInfoVO);
        userInfoVO.setAvatar(userLoginInfo.getOption("sysUserAvatar"));
        userInfoVO.setTenantId(userLoginInfo.getTenantId());
        userInfoVO.setRoles(userLoginInfo.getRoles());
        userInfoVO.setPerms(userLoginInfo.getPermission());
        return userInfoVO;
    }

    @Override
    public Result<LoginResult> refresh(RefreshToken refreshToken, HttpServletRequest request, HttpServletResponse response) {
        AuthClientExecute clientExecute = AuthClientExecute.getInstance();
        com.chua.starter.oauth.client.support.user.LoginResult upgrade = clientExecute.upgrade(refreshToken.getRefreshToken(), UpgradeType.REFRESH);
        if (null == upgrade) {
            return Result.failed("鐧诲綍宸茶繃鏈");
        }
        LoginResult loginResult = LoginResult.builder()
                .userInfo(getUserLoginInfo(upgrade.getToken()))
                .tokenType("Bearer")
                .refreshToken(upgrade.getRefreshToken())
                .accessToken(upgrade.getToken())
                .build();
        AuthenticationInformation.CookieUtil.set(response, "x-oauth-cookie", upgrade.getToken(), true);
        return Result.success(loginResult);
    }

    @Override
    public ReturnResult<String> thirdLoginCode(ThirdLoginData loginData, HttpServletRequest request, HttpServletResponse response) {
        return ReturnResult.fail("绗笁鏂圭櫥褰曟殏涓嶆敮鎸");
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
        if (StringUtils.isBlank(thirdPartyUnbindRequest.getLoginType())) {
            return ReturnResult.fail("鐧诲綍绫诲瀷涓嶈兘涓虹┖");
        }
        if (sysUserThirdPartyMapper.delete(Wrappers.<SysUserThirdParty>lambdaQuery().eq(SysUserThirdParty::getSysUserId, userId).eq(SysUserThirdParty::getSysUserThirdPartyType, thirdPartyUnbindRequest.getLoginType())) > 0) {
            return ReturnResult.ok();
        }
        return ReturnResult.fail("瑙ｇ粦澶辫触");
    }

    /**
     * 鑾峰彇鐢ㄦ埛淇℃伅
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