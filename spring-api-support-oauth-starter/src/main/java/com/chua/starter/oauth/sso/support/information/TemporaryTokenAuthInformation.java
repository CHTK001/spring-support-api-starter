package com.chua.starter.oauth.sso.support.information;

import com.chua.common.support.core.utils.MapUtils;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.parser.TemporaryTokenAuthorization;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.Map;

/**
 * 临时令牌认证信息 - 支持多协议
 * <p>用于解析临时令牌创建请求</p>
 *
 * @author CH
 * @version 1.0.0
 * @since 2025-12-11
 */
@Slf4j
public class TemporaryTokenAuthInformation extends BaseAuthInformation {

    /**
     * HTTP协议构造函数
     *
     * @param authServerProperties 认证服务器配置
     * @param data                 请求数据
     * @param request              HTTP请求
     * @param response             HTTP响应
     * @param applicationContext   Spring应用上下文
     * @param loginCheck           登录检查器
     */
    public TemporaryTokenAuthInformation(AuthServerProperties authServerProperties,
                                         String data,
                                         HttpServletRequest request,
                                         HttpServletResponse response,
                                         ApplicationContext applicationContext,
                                         LoginCheck loginCheck) {
        super(authServerProperties, data, request, response, applicationContext, loginCheck);
    }

    /**
     * Armeria协议构造函数
     *
     * @param authServerProperties 认证服务器配置
     * @param data                 请求数据
     * @param request              Armeria请求
     * @param response             Armeria响应
     * @param applicationContext   Spring应用上下文
     * @param loginCheck           登录检查器
     */
    public TemporaryTokenAuthInformation(AuthServerProperties authServerProperties,
                                         String data,
                                         ArmeriaAuthInformation.ArmeriaRequest request,
                                         ArmeriaAuthInformation.ArmeriaResponse response,
                                         ApplicationContext applicationContext,
                                         LoginCheck loginCheck) {
        super(authServerProperties, data, request, response, applicationContext, loginCheck);
    }

    @Override
    protected String getRequestHeader(Object request, String headerName) {
        if (request instanceof HttpServletRequest httpRequest) {
            return httpRequest.getHeader(headerName);
        } else if (request instanceof ArmeriaAuthInformation.ArmeriaRequest armeriaRequest) {
            return armeriaRequest.getHeader(headerName);
        }
        return null;
    }

    @Override
    protected void setResponseHeader(Object response, String headerName, String headerValue) {
        if (response instanceof HttpServletResponse httpResponse) {
            httpResponse.setHeader(headerName, headerValue);
        } else if (response instanceof ArmeriaAuthInformation.ArmeriaResponse armeriaResponse) {
            armeriaResponse.setHeader(headerName, headerValue);
        }
    }

    @Override
    protected String getRemoteAddress(Object request) {
        if (request instanceof HttpServletRequest httpRequest) {
            return httpRequest.getRemoteAddr();
        } else if (request instanceof ArmeriaAuthInformation.ArmeriaRequest armeriaRequest) {
            return armeriaRequest.getRemoteAddr();
        }
        return null;
    }

    @Override
    protected Object getRequestAttribute(Object request, String attributeName) {
        if (request instanceof HttpServletRequest httpRequest) {
            return httpRequest.getAttribute(attributeName);
        } else if (request instanceof ArmeriaAuthInformation.ArmeriaRequest armeriaRequest) {
            return armeriaRequest.getAttribute(attributeName);
        }
        return null;
    }

    @Override
    protected void setRequestAttribute(Object request, String attributeName, Object attributeValue) {
        if (request instanceof HttpServletRequest httpRequest) {
            httpRequest.setAttribute(attributeName, attributeValue);
        } else if (request instanceof ArmeriaAuthInformation.ArmeriaRequest armeriaRequest) {
            armeriaRequest.setAttribute(attributeName, attributeValue);
        }
    }

    /**
     * 获取ApplicationContext
     *
     * @return ApplicationContext
     */
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 获取LoginCheck
     *
     * @return LoginCheck
     */
    public LoginCheck getLoginCheck() {
        return loginCheck;
    }

    /**
     * 获取客户端IP地址
     *
     * @return 客户端IP地址
     */
    public String getClientIp() {
        return getRemoteAddress(getRequest());
    }

    /**
     * 获取源令牌
     *
     * @return 源令牌
     */
    public String getSourceToken() {
        return MapUtils.getString(this.getBodyData(), "x-oauth-source-token");
    }

    /**
     * 获取扩展参数
     *
     * @return 扩展参数Map
     */
    public Map<String, Object> getExtParams() {
        return MapUtils.getMap(this.getBodyData(), "x-oauth-ext");
    }

    /**
     * 获取过期时间（秒）
     *
     * @return 过期时间，默认10秒，范围1-3600秒
     */
    public long getExpireTime() {
        Map<String, Object> ext = getExtParams();
        long expireTime = MapUtils.getLong(ext, "expireTime", 10L);
        return Math.max(1, Math.min(expireTime, 3600));
    }

    /**
     * 获取允许访问的URL列表
     *
     * @return 允许访问的URL列表，为空表示允许所有
     */
    public String getAllowedUrls() {
        Map<String, Object> ext = getExtParams();
        return MapUtils.getString(ext, "allowedUrls");
    }

    /**
     * 获取授权处理器
     *
     * @return 临时令牌授权处理器
     */
    @Override
    public TemporaryTokenAuthorization authorization() {
        return new TemporaryTokenAuthorization(this, applicationContext);
    }
}
