package com.chua.starter.system.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.starter.system.pojo.LoginResult;
import com.chua.starter.system.pojo.SmsCodeRequest;
import com.chua.starter.system.pojo.SmsLoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 短信验证码服务接口
 *
 * @author CH
 * @since 2024/12/11
 */
public interface SmsCodeService {

    /**
     * 发送短信验证码
     *
     * @param request 发送请求
     * @return 发送结果
     */
    ReturnResult<Boolean> sendCode(SmsCodeRequest request);

    /**
     * 验证短信验证码
     *
     * @param phone   手机号
     * @param code    验证码
     * @param bizType 业务类型
     * @return 验证结果
     */
    boolean verifyCode(String phone, String code, String bizType);

    /**
     * 短信验证码登录
     *
     * @param request     登录请求
     * @param httpRequest HTTP请求
     * @param response    HTTP响应
     * @return 登录结果
     */
    ReturnResult<LoginResult> login(SmsLoginRequest request, HttpServletRequest httpRequest, HttpServletResponse response);
}
