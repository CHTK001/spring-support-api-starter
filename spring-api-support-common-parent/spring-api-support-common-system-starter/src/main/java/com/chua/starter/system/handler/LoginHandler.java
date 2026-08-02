package com.chua.starter.system.handler;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.oauth.client.support.provider.LoginData;
import com.chua.starter.system.pojo.LoginResult;
import com.chua.starter.system.service.impl.SysLoginServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 登录处理器接口
 *
 * @author CH
 * @version 1.0.0
 * @since 2024/12/2
 */
public interface LoginHandler {

    /**
     * 登录处理
     *
     * @param sysLoginService 登录服务
     * @param loginData       登录数据
     * @param request         请求
     * @param response        响应
     * @param fingerprint     设备指纹
     * @return 登录结果
     */
    ReturnResult<LoginResult> login(SysLoginServiceImpl sysLoginService, LoginData loginData, HttpServletRequest request, HttpServletResponse response, String fingerprint);
}
