package com.chua.starter.oauth.sso.support.information;

import com.chua.common.support.utils.MapUtils;
import com.chua.starter.oauth.sso.service.SysUserLogService;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.parser.LoginAuthorization;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.Map;

/**
 * 登录认证信息 - 支持多协议
 *
 * @author CH
 */
@Slf4j
public class LoginAuthInformation extends BaseAuthInformation {

    private final SysUserLogService sysUserLogService;

    // HTTP协议构造函数
    public LoginAuthInformation(AuthServerProperties authServerProperties,
                                String data,
                                HttpServletRequest request,
                                HttpServletResponse response,
                                ApplicationContext applicationContext,
                                LoginCheck loginCheck) {
        super(authServerProperties, data, request, response, applicationContext, loginCheck);
        this.sysUserLogService = applicationContext.getBean(SysUserLogService.class);
    }

    // Armeria协议构造函数
    public LoginAuthInformation(AuthServerProperties authServerProperties,
                                String data,
                                ArmeriaAuthInformation.ArmeriaRequest request,
                                ArmeriaAuthInformation.ArmeriaResponse response,
                                ApplicationContext applicationContext,
                                LoginCheck loginCheck) {
        super(authServerProperties, data, request, response, applicationContext, loginCheck);
        this.sysUserLogService = applicationContext.getBean(SysUserLogService.class);
    }

    @Override
    protected String getRequestHeader(Object request, String headerName) {
        if (request instanceof HttpServletRequest) {
            return ((HttpServletRequest) request).getHeader(headerName);
        } else if (request instanceof ArmeriaAuthInformation.ArmeriaRequest) {
            return ((ArmeriaAuthInformation.ArmeriaRequest) request).getHeader(headerName);
        }
        return null;
    }

    @Override
    protected void setResponseHeader(Object response, String headerName, String headerValue) {
        if (response instanceof HttpServletResponse) {
            ((HttpServletResponse) response).setHeader(headerName, headerValue);
        } else if (response instanceof ArmeriaAuthInformation.ArmeriaResponse) {
            ((ArmeriaAuthInformation.ArmeriaResponse) response).setHeader(headerName, headerValue);
        }
    }

    @Override
    protected String getRemoteAddress(Object request) {
        if (request instanceof HttpServletRequest) {
            return ((HttpServletRequest) request).getRemoteAddr();
        } else if (request instanceof ArmeriaAuthInformation.ArmeriaRequest) {
            return ((ArmeriaAuthInformation.ArmeriaRequest) request).getRemoteAddr();
        }
        return null;
    }

    @Override
    protected Object getRequestAttribute(Object request, String attributeName) {
        if (request instanceof HttpServletRequest) {
            return ((HttpServletRequest) request).getAttribute(attributeName);
        } else if (request instanceof ArmeriaAuthInformation.ArmeriaRequest) {
            return ((ArmeriaAuthInformation.ArmeriaRequest) request).getAttribute(attributeName);
        }
        return null;
    }

    @Override
    protected void setRequestAttribute(Object request, String attributeName, Object attributeValue) {
        if (request instanceof HttpServletRequest) {
            ((HttpServletRequest) request).setAttribute(attributeName, attributeValue);
        } else if (request instanceof ArmeriaAuthInformation.ArmeriaRequest) {
            ((ArmeriaAuthInformation.ArmeriaRequest) request).setAttribute(attributeName, attributeValue);
        }
    }



    /**
     * 获取授权信息
     * @return 授权信息
     */
    @SuppressWarnings("unchecked")
    @Override
    public LoginAuthorization authorization() {
        return new LoginAuthorization(this,
                MapUtils.getString(this.getBodyData(), "x-oauth-username"),
                MapUtils.getString(this.getBodyData(), "x-oauth-password"),
                MapUtils.getString(this.getBodyData(), "x-oauth-auth-type"),
                MapUtils.getString(this.getBodyData(), "x-oauth-param-app-name"),
                (Map<String, String>) MapUtils.getObject(this.getBodyData(), "x-oauth-ext"),
                (HttpServletResponse) getResponse(),
                getAuthServerProperties(),
                sysUserLogService,
                applicationContext,
                loginCheck);
    }
}
