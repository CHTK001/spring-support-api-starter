package com.chua.starter.service.impl.login;

import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chua.common.support.annotations.Spi;
import com.chua.common.support.collection.ImmutableBuilder;
import com.chua.common.support.json.Json;
import com.chua.common.support.objects.annotation.AutoInject;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.common.support.utils.CookieUtil;
import com.chua.starter.oauth.client.support.enums.AuthType;
import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
import com.chua.starter.oauth.client.support.provider.LoginData;
import com.chua.starter.oauth.client.support.user.LoginAuthResult;
import com.chua.starter.service.SimpleSysUserService;
import com.chua.starter.service.SysUserThirdPartyService;
import com.chua.starter.system.entity.SysUser;
import com.chua.starter.system.entity.SysUserThirdParty;
import com.chua.tencent.support.miniapp.TencentMaHandler;
import com.chua.starter.service.impl.SysLoginServiceImpl;
import com.chua.starter.system.result.LoginResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static com.chua.common.support.lang.code.ReturnCode.REQUEST_PARAM_ERROR;
import static com.chua.common.support.lang.code.ReturnCode.USERNAME_OR_PASSWORD_ERROR;
import static com.chua.starter.common.support.utils.RequestUtils.getIpAddress;

/**
 * wx登录处理器
 *
 * @author CH
 * @since 2024/12/2
 */
@Spi("wx")
@Slf4j
public class WxLoginHandler implements LoginHandler {

    @AutoInject
    private TencentMaHandler tencentMaHandler;
    /**
     * 用户第三方登录
     */
    @AutoInject
    private SysUserThirdPartyService sysUserThirdPartyService;
    @AutoInject
    private SimpleSysUserService sysUserService;

    @AutoInject
    private TransactionTemplate transactionTemplate;


    @Override
    public Result<LoginResult> login(SysLoginServiceImpl sysLoginService, LoginData loginData, HttpServletRequest request, HttpServletResponse response) {
        String code = loginData.getUsername();
        String openid = null;
        Result<LoginResult> res = null;
        try {
            // 调用微信 API 获取用户的 openid 和 session_key
            WxMaJscode2SessionResult session = tencentMaHandler.getSessionInfo(code);
            openid = session.getOpenid();
            res = check(session, loginData);
        } catch (WxErrorException e) {
            return Result.failed(e.getError().getErrorMsg());
        }

        if (null != res) {
            return res;
        }

        String address = getIpAddress(request);
        AuthClientExecute clientExecute = AuthClientExecute.getInstance();
        long startTime = System.currentTimeMillis();
        LoginAuthResult accessToken = clientExecute.getAccessToken(openid, loginData.getPassword(),
                AuthType.valueOf(loginData.getLoginType()),
                ImmutableBuilder.<String, Object>builderOfMap().put("address", address).build()
        );

        log.info("登录耗时:{}ms", System.currentTimeMillis() - startTime);
        if (null == accessToken) {
            return Result.failed(USERNAME_OR_PASSWORD_ERROR, "账号或者密码不正确");
        }

        if (accessToken.getCode() != 200) {
            return Result.failed(REQUEST_PARAM_ERROR, accessToken.getMessage());
        }

        LoginResult loginResult = LoginResult.builder()
                .userInfo(sysLoginService.getUserLoginInfo(accessToken.getToken()))
                .tokenType("Bearer")
                .accessToken(accessToken.getToken())
                .build();
        CookieUtil.set(response, "x-oauth-cookie", accessToken.getToken(), true);
        return Result.success(loginResult);
    }

    private Result<LoginResult> check(WxMaJscode2SessionResult session, LoginData loginData) {
        String code = loginData.getUsername();
        String openid = session.getOpenid();
        WxMaUserInfo userInfo = null;
        String phoneNumber = null;
        try {
            // 调用微信 API 获取用户的手机号
            WxMaPhoneNumberInfo phoneInfo = tencentMaHandler.getPhoneNumber(code);
            phoneNumber = phoneInfo.getPhoneNumber();

            // 调用微信 API 获取用户的详细信息
            userInfo = tencentMaHandler.getUserInfo(session.getSessionKey(), loginData.getWxEncryptedData(), loginData.getWxIv());
        } catch (WxErrorException e) {
            return Result.failed(e.getError().getErrorMsg());
        }
        // 获取用户昵称
        String nickName = userInfo.getNickName();
        // 获取用户头像
        String avatarUrl = userInfo.getAvatarUrl();
        // 获取用户国家
        String country = userInfo.getCountry();
        // 获取用户省份
        String province = userInfo.getProvince();
        // 获取用户城市
        String city = userInfo.getCity();

        List<SysUserThirdParty> list = sysUserThirdPartyService.list(Wrappers.<SysUserThirdParty>lambdaQuery()
                .eq(SysUserThirdParty::getSysUserThirdPartyUid, openid)
                .eq(SysUserThirdParty::getSysUserThirdPartyType, SysUserThirdParty.Type.WX_MINI_APP.name()));
        if (!CollectionUtils.isEmpty(list)) {
            return null;
        }

        try {
            SysUser sysUser = new SysUser();
            sysUser.setSysUserUsername(openid);
            sysUser.setSysUserNickname(nickName);
            sysUser.setSysUserAvatar(avatarUrl);
            sysUser.setSysUserPhone(phoneNumber);
            SysUserThirdParty sysUserThirdParty = new SysUserThirdParty();
            sysUserThirdParty.setSysUserThirdPartyUid(openid);
            sysUserThirdParty.setSysUserThirdPartyType(SysUserThirdParty.Type.WX_MINI_APP.name());
            sysUserThirdParty.setSysUserThirdPartyName(Json.toJson(session));
            sysUserThirdParty.setSysUserThirdPartyValue(session.getUnionid());
            transactionTemplate.execute(status -> {
                sysUser.setSysUserDelete(0);
                sysUser.setSysUserStatus(1);
                sysUserService.save(sysUser);
                sysUserThirdParty.setSysUserId(sysUser.getSysUserId());
                sysUserThirdPartyService.save(sysUserThirdParty);
                return 0;
            });
        } catch (TransactionException e) {
            return Result.failed("绑定信息失败, 请重试!");
        }
        return null;
    }
}
