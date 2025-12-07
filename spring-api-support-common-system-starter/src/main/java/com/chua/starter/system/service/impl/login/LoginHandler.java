package com.chua.starter.system.service.impl.login;

import com.chua.starter.common.support.result.Result;
import com.chua.starter.oauth.client.support.provider.LoginData;
import com.chua.starter.service.result.LoginResult;
import com.chua.starter.system.service.impl.SysLoginServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 登录处理�?
 * @author CH
 * @since 2024/12/2
 */
public interface LoginHandler {


    /**
     * 执行用户登录操作
     *
     * @param sysLoginService 登录服务实现类，用于处理登录逻辑
     * @param loginData       登录所需的数据，包含用户凭证等信�?
     * @param request         HTTP请求对象，可用于获取请求相关数据
     * @param response        HTTP响应对象，可用于设置登录后的响应数据
     * @param fingerprint
     * @return 返回登录结果，包含登录是否成功及用户相关信息
     */
    Result<LoginResult> login(SysLoginServiceImpl sysLoginService, LoginData loginData, HttpServletRequest request, HttpServletResponse response, String fingerprint);
}
