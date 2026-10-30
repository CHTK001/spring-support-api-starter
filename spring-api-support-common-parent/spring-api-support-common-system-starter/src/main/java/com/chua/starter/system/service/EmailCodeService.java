package com.chua.starter.system.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.system.pojo.EmailCodeRequest;
import com.chua.starter.system.pojo.EmailLoginRequest;
import com.chua.starter.system.pojo.LoginResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 邮箱验证码服务
 *
 * <p>验证码生成、Redis 存储与投递复用通用 {@link VerifyCodeService}（EMAIL 渠道），
 * 本服务负责登录场景的账号校验与令牌签发。</p>
 *
 * @since 4.0.0.50
 *
 * @author CH
 */
public interface EmailCodeService {

    /**
     * 发送邮箱验证码
     *
     * @param request 邮箱验证码请求
     * @return 是否发送成功
     */
    ReturnResult<Boolean> sendCode(EmailCodeRequest request);

    /**
     * 邮箱验证码登录
     *
     * @param request      登录请求
     * @param httpRequest  HTTP 请求
     * @param response     HTTP 响应
     * @return 登录结果
     */
    ReturnResult<LoginResult> login(EmailLoginRequest request, HttpServletRequest httpRequest, HttpServletResponse response);
}
