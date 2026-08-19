package com.chua.starter.oauth.sso.support.information;

import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationContext;

/**
 * HTTP协议认证信息实现类
 * 
 */
public abstract class HttpAuthInformation extends BaseAuthInformation {

    /** 请求 */
    private final HttpServletRequest request;
    /** 响应 */
    private final HttpServletResponse response;

    /**
     * 创建 HttpAuthInformation 实例
     * @param authServerProperties authServerProperties
     * @param data data
     * @param request request
     * @param response response
     * @param applicationContext applicationContext
     * @param loginCheck loginCheck
     */
    public HttpAuthInformation(AuthServerProperties authServerProperties,
                              String data,
                              HttpServletRequest request,
                              HttpServletResponse response,
                              ApplicationContext applicationContext,
                              LoginCheck loginCheck) {
        super(authServerProperties, data, request, response, applicationContext, loginCheck);
        this.request = request;
        this.response = response;
    }

    @Override
    /** 获取RequestHeader */
    protected String getRequestHeader(Object request, String headerName) {
        if (request instanceof HttpServletRequest) {
            return ((HttpServletRequest) request).getHeader(headerName);
        }
        return null;
    }

    @Override
    /** 设置ResponseHeader */
    protected void setResponseHeader(Object response, String headerName, String headerValue) {
        if (response instanceof HttpServletResponse) {
            ((HttpServletResponse) response).setHeader(headerName, headerValue);
        }
    }

    @Override
    /** 获取RemoteAddress */
    protected String getRemoteAddress(Object request) {
        if (request instanceof HttpServletRequest) {
            return ((HttpServletRequest) request).getRemoteAddr();
        }
        return null;
    }

    @Override
    /** 获取RequestAttribute */
    protected Object getRequestAttribute(Object request, String attributeName) {
        if (request instanceof HttpServletRequest) {
            return ((HttpServletRequest) request).getAttribute(attributeName);
        }
        return null;
    }

    @Override
    /** 设置RequestAttribute */
    protected void setRequestAttribute(Object request, String attributeName, Object attributeValue) {
        if (request instanceof HttpServletRequest) {
            ((HttpServletRequest) request).setAttribute(attributeName, attributeValue);
        }
    }

    @Override
    /** 获取Response */
    protected Object getResponse() {
        return response;
    }

    @Override
    /** 获取Request */
    protected Object getRequest() {
        return request;
    }

    // 提供类型安全的访问方法
    /** 获取HttpRequest */
    public HttpServletRequest getHttpRequest() {
        return request;
    }

    /** 获取HttpResponse */
    public HttpServletResponse getHttpResponse() {
        return response;
    }
}
