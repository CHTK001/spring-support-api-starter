//package com.chua.starter.system.login;
//
//import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
//import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
//import com.baomidou.mybatisplus.core.toolkit.StringUtils;
//import com.baomidou.mybatisplus.core.toolkit.Wrappers;
//import com.chua.common.support.annotations.Spi;
//import com.chua.common.support.collection.ImmutableBuilder;
//import com.chua.common.support.objects.annotation.AutoInject;
//import com.chua.starter.common.support.result.Result;
//import com.chua.starter.common.support.utils.CookieUtil;
//import com.chua.starter.oauth.client.support.enums.AuthType;
//import com.chua.starter.oauth.client.support.execute.AuthClientExecute;
//import com.chua.starter.oauth.client.support.provider.LoginData;
//import com.chua.starter.oauth.client.support.login.LoginAuthResult;
//import com.chua.starter.service.SysCityService;
//import com.chua.starter.service.impl.SysLoginServiceImpl;
//import com.chua.starter.service.impl.login.LoginHandler;
//import com.chua.starter.system.entity.SysCity;
//import com.chua.tencent.support.miniapp.TencentMaHandler;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.extern.slf4j.Slf4j;
//import me.chanjar.weixin.common.error.WxErrorException;
//import org.springframework.transaction.TransactionException;
//import org.springframework.transaction.support.TransactionTemplate;
//
//import static com.chua.common.support.lang.code.ReturnCode.REQUEST_PARAM_ERROR;
//import static com.chua.common.support.lang.code.ReturnCode.USERNAME_OR_PASSWORD_ERROR;
//import static com.chua.starter.common.support.utils.RequestUtils.getIpAddress;
//
///**
// * wx鐧诲綍澶勭悊鍣?
// *
// * @author CH
// * @since 2024/12/2
// */
//@Spi("WX_MINI_APP")
//@Slf4j
//public class WxMiniAppLoginHandler implements LoginHandler {
//
//    @AutoInject
//    private TencentMaHandler tencentMaHandler;
//    /**
//     * 鐢ㄦ埛绗笁鏂圭櫥褰?
//     */
//    @AutoInject
//    private SysUserStudentService sysUserService;
//
//    @AutoInject
//    private TransactionTemplate transactionTemplate;
//
//    /**
//     * 鍩庡競
//     */
//    @AutoInject
//    private SysCityService sysCityService;
//
//
//    @Override
//    public Result<LoginResult> login(SysLoginServiceImpl sysLoginService, LoginData loginData, HttpServletRequest request, HttpServletResponse response) {
//        String openid = loginData.getUsername();
//        String code = loginData.getPassword();
//        Result<LoginResult> res = null;
//        // 璋冪敤寰俊 API 鑾峰彇鐢ㄦ埛鐨?openid 鍜?session_key
//        res = check(openid, code, loginData, "PTYH");
//        if (null != res) {
//            return res;
//        }
//
//        String address = getIpAddress(request);
//        AuthClientExecute clientExecute = AuthClientExecute.getInstance();
//        long startTime = System.currentTimeMillis();
//        LoginAuthResult accessToken = clientExecute.getAccessToken(openid, loginData.getPassword(),
//                AuthType.WX_MINI_APP,
//                ImmutableBuilder.<String, Object>builderOfMap().put("address", address).build()
//        );
//
//        log.info("鐧诲綍鑰楁椂:{}ms", System.currentTimeMillis() - startTime);
//        if (null == accessToken) {
//            return Result.failed(USERNAME_OR_PASSWORD_ERROR, "璐﹀彿鎴栬€呭瘑鐮佷笉姝ｇ‘");
//        }
//
//        if (accessToken.getCode() != 200) {
//            return Result.failed(REQUEST_PARAM_ERROR, accessToken.getMessage());
//        }
//
//        LoginResult loginResult = LoginResult.builder()
//                .userInfo(sysLoginService.getUserLoginInfo(accessToken.getToken()))
//                .tokenType("Bearer")
//                .accessToken(accessToken.getToken())
//                .build();
//        CookieUtil.set(response, "x-oauth-cookie", accessToken.getToken(), true);
//        return Result.success(loginResult);
//    }
//
//    protected Result<LoginResult> check(String openId, String code, LoginData loginData, String roleCode) {
//        WxMaUserInfo userInfo = null;
//        String phoneNumber = null;
//        String nickName = null;
//        String avatarUrl = null;
//        String province = null;
//        SysUserStudent sysUser = new SysUserStudent();
//
//
//        try {
//            // 璋冪敤寰俊 API 鑾峰彇鐢ㄦ埛鐨勬墜鏈哄彿
//            WxMaPhoneNumberInfo phoneInfo = tencentMaHandler.getPhoneNumber(code);
//            phoneNumber = phoneInfo.getPhoneNumber();
//
//            // 璋冪敤寰俊 API 鑾峰彇鐢ㄦ埛鐨勮缁嗕俊鎭?
//            userInfo = tencentMaHandler.getUserInfo(loginData.getWxSessionKey(), loginData.getWxEncryptedData(), loginData.getWxIv());
//            sysUser.setSysUserUnionId(userInfo.getUnionId());
//
//        } catch (WxErrorException e) {
//            return Result.failed(e.getError().getErrorMsg());
//        }
//        // 鑾峰彇鐢ㄦ埛鏄电О
//        nickName = userInfo.getNickName();
//        // 鑾峰彇鐢ㄦ埛澶村儚
//        avatarUrl = userInfo.getAvatarUrl();
//        // 鑾峰彇鐢ㄦ埛鍥藉
//        String country = userInfo.getCountry();
//        // 鑾峰彇鐢ㄦ埛鐪佷唤
//        province = userInfo.getProvince();
//        // 鑾峰彇鐢ㄦ埛鍩庡競
//        String city = userInfo.getCity();
//
//        sysUser.setSysUserNickname(nickName);
//        sysUser.setSysUserAvatar(avatarUrl);
//        sysUser.setSysUserPhone(phoneNumber);
//        SysUserStudent sysUserStudent = sysUserService.getOne(Wrappers.<SysUserStudent>lambdaQuery()
//                .eq(SysUserStudent::getSysUserOpenId, openId));
//        if (null != sysUserStudent) {
//            if (!"YK".equals(roleCode) && "YK".equals(sysUserStudent.getSysUserType())) {
//                sysUserStudent.setSysUserNickname(nickName);
//                sysUserStudent.setSysUserAvatar(avatarUrl);
//                sysUserStudent.setSysUserPhone(phoneNumber);
//                sysUserStudent.setSysUserType(roleCode);
//                sysUserService.updateById(sysUserStudent);
//            }
//            return null;
//        }
//
//        try {
//            sysUser.setSysUserOpenId(openId);
//            sysUser.setSysUserType(roleCode);
//            sysUser.setSysUserDelete(0);
//            sysUser.setSysUserStatus(1);
//            if (StringUtils.isNotBlank(province)) {
//                SysCity sysCity = sysCityService.getOne(Wrappers.<SysCity>lambdaQuery()
//                        .like(SysCity::getSysCityName, province)
//                        .eq(SysCity::getSysCityLevel, 0).last("limit 1"));
//                if (null != sysCity) {
//                    sysUser.setSysUserProvince(sysCity.getSysCityCode());
//                }
//            }
//            sysUserService.save(sysUser);
//        } catch (TransactionException e) {
//            return Result.failed("缁戝畾淇℃伅澶辫触, 璇烽噸璇?");
//        }
//
//        return null;
//    }
//}
