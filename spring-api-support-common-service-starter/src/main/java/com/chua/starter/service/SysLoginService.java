package com.chua.starter.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.common.support.result.Result;
import com.chua.starter.oauth.client.support.provider.LoginData;
import com.chua.starter.oauth.client.support.provider.ThirdLoginData;
import com.chua.starter.service.pojo.user.RefreshToken;
import com.chua.starter.service.pojo.user.ThirdPartyUnbindRequest;
import com.chua.starter.service.result.LoginResult;
import com.chua.starter.service.result.UserInfoVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * 登录接口
 *
 * @author CH
 * @since 2024/9/12
 */
public interface SysLoginService {

    /**
     * 用户登录方法，根据登录数据进行用户身份验证
     *
     * @param loginData   登录数据对象，包含用户名、密码等信息
     * @param request     HTTP请求对象，用于获取请求相关的数据
     * @param response    HTTP响应对象，用于设置响应数据
     * @param fingerprint
     * @return 结果对象，包含登录结果和可能的附加信息
     */
    Result<LoginResult> login(LoginData loginData, HttpServletRequest request, HttpServletResponse response, String fingerprint);

    /**
     * 获取当前登录用户的信息
     *
     * @return 用户信息对象，包含用户的基本信息
     */
    UserInfoVO getUserLoginInfo();

    /**
     * 获取当前登录用户的信息
     *
     * @param token 用户的令牌，用于确定当前登录用户
     * @return 用户信息对象，包含用户的基本信息
     */
    UserInfoVO getUserLoginInfo(String token);

    /**
     * 刷新用户令牌
     *
     * @param refreshToken 刷新令牌对象，包含旧的令牌信息
     * @param request      HTTP请求对象，用于获取请求相关的数据
     * @param response     HTTP响应对象，用于设置响应数据
     * @return 结果对象，包含刷新结果和可能的附加信息
     */
    Result<LoginResult> refresh(RefreshToken refreshToken, HttpServletRequest request, HttpServletResponse response);

    /**
     * 三方登录
     *
     * @param loginData 登录数据对象，包含三方登录信息
     * @param request   HTTP请求对象，用于获取请求相关的数据
     * @param response  HTTP响应对象，用于设置响应数据
     * @return 结果对象，包含三方登录结果和可能的附加信息
     */
    ReturnResult<String> thirdLoginCode(ThirdLoginData loginData, HttpServletRequest request, HttpServletResponse response);

    /**
     * 三方绑定
     *
     * @param loginData 登录数据对象，包含三方登录信息
     * @param request   HTTP请求对象，用于获取请求相关的数据
     * @param response  HTTP响应对象，用于设置响应数据
     * @return 结果对象，包含三方绑定结果和可能的附加信息
     */
    ReturnResult<String> thirdBindCode(ThirdLoginData loginData, HttpServletRequest request, HttpServletResponse response);

    /**
     * 获取三方绑定信息
     *
     * @param userId 用户ID，用于确定要获取绑定信息的用户
     * @return 绑定信息列表，包含当前用户绑定的三方信息
     */
    ReturnResult<List<String>> thirdBindInfo(String userId);

    /**
     * 三方解绑
     *
     * @param userId                  用户ID，用于确定要解绑的用户
     * @param thirdPartyUnbindRequest 解绑请求对象，包含要解绑的三方信息
     * @return 操作结果对象，包含操作是否成功和相关提示信息
     */
    ReturnResult<Boolean> thirdUnbind(String userId, ThirdPartyUnbindRequest thirdPartyUnbindRequest);
}
