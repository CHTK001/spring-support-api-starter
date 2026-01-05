package com.chua.starter.oauth.sso.support.information;

import com.chua.common.support.utils.MapUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.parser.OauthAuthorization;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

/**
 * OAuth认证信息 - 支持多协议
 *
 * @author CH
 */
@Slf4j
public class OauthAuthInformation extends BaseAuthInformation {

    // HTTP协议构造函数
    public OauthAuthInformation(AuthServerProperties authServerProperties,
                               String data,
                               HttpServletRequest request,
                               HttpServletResponse response,
                               ApplicationContext applicationContext,
                               LoginCheck loginCheck) {
        super(authServerProperties, data, request, response, applicationContext, loginCheck);
    }

    // Armeria协议构造函数
    public OauthAuthInformation(AuthServerProperties authServerProperties,
                               String data,
                               ArmeriaAuthInformation.ArmeriaRequest request,
                               ArmeriaAuthInformation.ArmeriaResponse response,
                               ApplicationContext applicationContext,
                               LoginCheck loginCheck) {
        super(authServerProperties, data, request, response, applicationContext, loginCheck);
    }

    // RSocket协议构造函数
    public OauthAuthInformation(AuthServerProperties authServerProperties,
                               String data,
                               RsocketAuthInformation.RsocketRequest request,
                               RsocketAuthInformation.RsocketResponse response,
                               ApplicationContext applicationContext,
                               LoginCheck loginCheck) {
        super(authServerProperties, data, request, response, applicationContext, loginCheck);
    }

    @Override
    protected String getRequestHeader(Object request, String headerName) {
        if (request instanceof HttpServletRequest) {
            return ((HttpServletRequest) request).getHeader(headerName);
        } else if (request instanceof ArmeriaAuthInformation.ArmeriaRequest) {
            return ((ArmeriaAuthInformation.ArmeriaRequest) request).getHeader(headerName);
        } else if (request instanceof RsocketAuthInformation.RsocketRequest) {
            return ((RsocketAuthInformation.RsocketRequest) request).getHeader(headerName);
        }
        return null;
    }

    @Override
    protected void setResponseHeader(Object response, String headerName, String headerValue) {
        if (response instanceof HttpServletResponse) {
            ((HttpServletResponse) response).setHeader(headerName, headerValue);
        } else if (response instanceof ArmeriaAuthInformation.ArmeriaResponse) {
            ((ArmeriaAuthInformation.ArmeriaResponse) response).setHeader(headerName, headerValue);
        } else if (response instanceof RsocketAuthInformation.RsocketResponse) {
            ((RsocketAuthInformation.RsocketResponse) response).setHeader(headerName, headerValue);
        }
    }

    @Override
    protected String getRemoteAddress(Object request) {
        if (request instanceof HttpServletRequest) {
            return ((HttpServletRequest) request).getRemoteAddr();
        } else if (request instanceof ArmeriaAuthInformation.ArmeriaRequest) {
            return ((ArmeriaAuthInformation.ArmeriaRequest) request).getRemoteAddr();
        } else if (request instanceof RsocketAuthInformation.RsocketRequest) {
            return ((RsocketAuthInformation.RsocketRequest) request).getRemoteAddr();
        }
        return null;
    }

    @Override
    protected Object getRequestAttribute(Object request, String attributeName) {
        if (request instanceof HttpServletRequest) {
            return ((HttpServletRequest) request).getAttribute(attributeName);
        } else if (request instanceof ArmeriaAuthInformation.ArmeriaRequest) {
            return ((ArmeriaAuthInformation.ArmeriaRequest) request).getAttribute(attributeName);
        } else if (request instanceof RsocketAuthInformation.RsocketRequest) {
            return ((RsocketAuthInformation.RsocketRequest) request).getAttribute(attributeName);
        }
        return null;
    }

    @Override
    protected void setRequestAttribute(Object request, String attributeName, Object attributeValue) {
        if (request instanceof HttpServletRequest) {
            ((HttpServletRequest) request).setAttribute(attributeName, attributeValue);
        } else if (request instanceof ArmeriaAuthInformation.ArmeriaRequest) {
            ((ArmeriaAuthInformation.ArmeriaRequest) request).setAttribute(attributeName, attributeValue);
        } else if (request instanceof RsocketAuthInformation.RsocketRequest) {
            ((RsocketAuthInformation.RsocketRequest) request).setAttribute(attributeName, attributeValue);
        }
    }

    /**
     * 获取ApplicationContext
     * @return ApplicationContext
     */
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 获取LoginCheck
     * @return LoginCheck
     */
    public LoginCheck getLoginCheck() {
        return loginCheck;
    }

    /**
     * 获取请求对象（供验证器使用）
     * @return 请求对象
     */
    public Object getRequest() {
        return request;
    }

    /**
     * 获取请求头（供验证器使用）
     * @param headerName 头名称
     * @return 头值
     */
    public String getRequestHeaderValue(String headerName) {
        return getRequestHeader(request, headerName);
    }

    /**
     * 获取请求属性（供验证器使用）
     * @param attributeName 属性名称
     * @return 属性值
     */
    public Object getRequestAttributeValue(String attributeName) {
        return getRequestAttribute(request, attributeName);
    }

    /**
     * 获取原始数据（供验证器使用）
     * @return 原始数据
     */
    public String getData() {
        return super.getData();
    }

    /**
     * 获取授权信息
     * @return 授权信息
     */
    @Override
    public OauthAuthorization authorization() {
        // 从Header读取认证类型（x-oauth-type）
        String authType = getRequestHeader(request, "x-oauth-type");
        
        return new OauthAuthorization(this,
                MapUtils.getString(this.getBodyData(), "x-oauth-token"),
                MapUtils.getString(this.getBodyData(), "x-oauth-cookie"),
                authType,
                MapUtils.getString(this.getBodyData(), "x-oauth-user-code"),
                applicationContext,
                loginCheck);
    }
}
