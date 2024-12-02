package com.chua.starter.system.login;

import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.chua.common.support.annotations.Spi;
import com.chua.common.support.objects.annotation.AutoInject;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.oauth.client.support.provider.LoginData;
import com.chua.starter.oauth.client.support.provider.LoginResult;
import com.chua.starter.service.SimpleSysUserService;
import com.chua.starter.service.SysUserThirdPartyService;
import com.chua.starter.service.impl.SysLoginServiceImpl;
import com.chua.starter.system.service.SysUserService;
import com.chua.tencent.support.miniapp.TencentMaHandler;
import com.qiren.starter.service.SimpleSysUserService;
import com.qiren.starter.service.SysUserThirdPartyService;
import com.qiren.starter.service.impl.SysLoginServiceImpl;
import com.qiren.starter.system.result.LoginResult;
import com.qiren.starter.system.result.UserInfoVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * wx游客登录处理器
 *
 * @author CH
 * @since 2024/12/2
 */
@Spi("WX_MINI_APP_VISITOR")
@Slf4j
public class WxMiniAppVisitorLoginHandler extends WxMiniAppLoginHandler {

    @AutoInject
    private TencentMaHandler tencentMaHandler;

    /**
     * 用户第三方登录
     */
    @AutoInject
    private SysUserThirdPartyService sysUserThirdPartyService;
    @AutoInject
    private SysUserService sysUserService;

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
            res = check(session, loginData, "YK");
        } catch (WxErrorException e) {
            return Result.failed(e.getError().getErrorMsg());
        }

        if (null != res) {
            return res;
        }


        LoginResult loginResult = LoginResult.builder()
                .userInfo(new UserInfoVO())
                .tokenType("Bearer")
                .accessToken(null)
                .build();
        return Result.success(loginResult);
    }

}
