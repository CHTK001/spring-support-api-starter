package com.chua.starter.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.function.Joiner;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.application.GlobalSettingFactory;
import com.chua.starter.common.support.utils.CookieUtil;
import com.chua.starter.oauth.client.support.enums.UpgradeType;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.chua.starter.oauth.client.support.properties.AuthClientProperties;
import com.chua.starter.oauth.client.support.provider.LoginData;
import com.chua.starter.oauth.client.support.provider.ThirdLoginData;
import com.chua.starter.oauth.client.support.user.UserResult;
import com.chua.starter.system.handler.LoginHandler;
import com.chua.starter.system.service.SysLoginService;
import com.chua.starter.system.mapper.SysUserThirdPartyMapper;
import com.chua.starter.entity.system.SysUserThirdParty;
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

import java.util.List;
import java.util.Optional;

import static com.chua.common.support.constant.CommonConstant.EMPTY;
import static com.chua.common.support.constant.Constants.CAPTCHA_SESSION_KEY;
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

    private final AuthClientProperties authProperties;
    private final SysUserThirdPartyMapper sysUserThirdPartyMapper;
    private final RedisTemplate redisTemplate;

    @Override
    public ReturnResult<LoginResult> login(LoginData loginData, HttpServletRequest request, HttpServletResponse response, String fingerprint) {
        log.info("\n登录信息包含 \n登录账号 -> {}\n登陆方式 -> {}", loginData.getUsername(), loginData.getLoginType());
        return ServiceProvider.of(LoginHandler.class).getNewExtension(Joiner.on("-").join(loginData.getLoginType(), loginData.getAccountType()))
                .login(this, loginData, request, response, fingerprint);
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
        String sessionKey = Optional.ofNullable(request.getSession().getAttribute(CAPTCHA_SESSION_KEY)).orElse(EMPTY).toString();
        if (StringUtils.isNotBlank(sessionKey)) {
            if (!code.equalsIgnoreCase(sessionKey)) {
                return ReturnResult.failure(RESOURCE_CAPTCHA_ERROR, "校验码不正确");
            }
            return null;
        }

        String verifyCodeUlid = loginData.getVerifyCodeUlid();
        if (StringUtils.isNotBlank(verifyCodeUlid)) {
            ValueOperations valueOperations = redisTemplate.opsForValue();
            Object object = valueOperations.get(verifyCodeUlid);
            if (null == object || !code.equalsIgnoreCase(object.toString())) {
                return ReturnResult.failure(RESOURCE_CAPTCHA_ERROR, "校验码不正确");
            }

            redisTemplate.delete(verifyCodeUlid);
            return null;
        }
        return ReturnResult.failure(RESOURCE_CAPTCHA_ERROR, "请重新刷新校验码");
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
    public UserInfoVO getUserLoginInfo(String token) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null || StringUtils.isBlank(token)) {
            return new UserInfoVO();
        }

        ServletRequestAttributes attributes = (ServletRequestAttributes) requestAttributes;
        attributes.getRequest().setAttribute(authProperties.getTokenName(), token);
        return getUserLoginInfo();
    }

    @Override
    public ReturnResult<LoginResult> refresh(RefreshToken refreshToken, HttpServletRequest request, HttpServletResponse response) {
        AuthClientExecute clientExecute = AuthClientExecute.getInstance();
        com.chua.starter.oauth.client.support.user.LoginResult upgrade = clientExecute.upgrade(refreshToken.getRefreshToken(), UpgradeType.REFRESH);
        if (null == upgrade) {
            return ReturnResult.failure("登录已过期");
        }
        LoginResult loginResult = LoginResult.builder()
                .userInfo(getUserLoginInfo(upgrade.getToken()))
                .tokenType("Bearer")
                .refreshToken(upgrade.getRefreshToken())
                .accessToken(upgrade.getToken())
                .build();
        CookieUtil.set(response, "x-oauth-cookie", upgrade.getToken(), true);
        return ReturnResult.success(loginResult);
    }

    @Override
    public ReturnResult<String> thirdLoginCode(ThirdLoginData loginData, HttpServletRequest request, HttpServletResponse response) {
        log.warn("第三方登录功能需要实现 SysSettingService.getAuthRequest 方法");
        return ReturnResult.fail("第三方登录暂不支持，请在 service-starter 中实现");
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
            return ReturnResult.fail("登录类型不能为空");
        }
        if (sysUserThirdPartyMapper.delete(Wrappers.<SysUserThirdParty>lambdaQuery().eq(SysUserThirdParty::getSysUserId, userId).eq(SysUserThirdParty::getSysUserThirdPartyType, thirdPartyUnbindRequest.getLoginType())) > 0) {
            return ReturnResult.ok();
        }
        return ReturnResult.fail("解绑失败");
    }
}
