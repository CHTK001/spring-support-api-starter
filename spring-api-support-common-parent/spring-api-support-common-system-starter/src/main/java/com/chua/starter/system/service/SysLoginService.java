package com.chua.starter.system.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.oauth.client.support.provider.LoginData;
import com.chua.starter.oauth.client.support.provider.ThirdLoginData;
import com.chua.starter.oauth.client.support.user.UserResume;
import com.chua.starter.system.pojo.RefreshToken;
import com.chua.starter.system.pojo.ThirdPartyUnbindRequest;
import com.chua.starter.system.pojo.LoginResult;
import com.chua.starter.system.pojo.UserInfoVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
* 登录服务接口
*
* @版本 1.0.0
* @since 2024/9/12
* @author CH
 */
public interface SysLoginService {

    /**
    * 登录
    *
    * @param loginData   登录数据
    * @param request     请求
    * @param response    响应
    * @param fingerprint 设备指纹
    * @return 登录结果
     */
    ReturnResult<LoginResult> login(LoginData loginData, HttpServletRequest request, HttpServletResponse response, String fingerprint);

    /**
    * 获取当前登录用户信息
    *
    * @return 用户信息
     */
    UserInfoVO getUserLoginInfo();

    /**
    * 获取当前登录用户信息
    *
    * @param token 令牌
    * @return 用户信息
     */
    UserInfoVO getUserLoginInfo(String token);

    /**
    * 将登录态用户信息转换为前端用户信息
    *
    * @param userResume 登录态用户信息
    * @return 用户信息
     */
    UserInfoVO buildUserInfo(UserResume userResume);

    /**
    * 刷新令牌
    *
    * @param refreshToken 刷新令牌
    * @param request      请求
    * @param response     响应
    * @return 登录结果
     */
    ReturnResult<LoginResult> refresh(RefreshToken refreshToken, HttpServletRequest request, HttpServletResponse response);

    /**
    * 第三方登录授权码
    *
    * @param loginData 登录数据
    * @param request   请求
    * @param response  响应
    * @return 授权码
     */
    ReturnResult<String> thirdLoginCode(ThirdLoginData loginData, HttpServletRequest request, HttpServletResponse response);

    /**
    * 第三方绑定授权码
    *
    * @param loginData 登录数据
    * @param request   请求
    * @param response  响应
    * @return 授权码
     */
    ReturnResult<String> thirdBindCode(ThirdLoginData loginData, HttpServletRequest request, HttpServletResponse response);

    /**
    * 获取第三方绑定信息
    *
    * @param userId 用户标识
    * @return 绑定信息
     */
    ReturnResult<List<String>> thirdBindInfo(String userId);

    /**
    * 第三方解绑
    *
    * @param userId                    用户标识
    * @param thirdPartyUnbindRequest   解绑请求
    * @return 解绑结果
     */
    ReturnResult<Boolean> thirdUnbind(String userId, ThirdPartyUnbindRequest thirdPartyUnbindRequest);
}
