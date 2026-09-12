package com.chua.starter.oauth.sso.support.information;

import com.chua.common.support.utils.MapUtils;
import com.chua.starter.system.service.SysUserLogService;
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
 * @since 4.0.0
 */
@Slf4j
public class LoginAuthInformation extends BaseAuthInformation {

    /** SYS用户日志服务 */
    private final SysUserLogService sysUserLogService;

    // HTTP协议构造函数
    /**
      * 创建 login认证信息 实例
     * @param authServerProperties 认证服务端属性
     * @param data 数据
     * @param request 请求
     * @param response 响应
     * @param applicationContext application上下文
     * @param loginCheck login检查
     */
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
    /**
      * 创建 login认证信息 实例
     * @param authServerProperties 认证服务端属性
     * @param data 数据
     * @param request 请求
     * @param response 响应
     * @param applicationContext application上下文
     * @param loginCheck login检查
     */
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
    /** 获取请求头部 */
    protected String getRequestHeader(Object request, String headerName) {
        if (request instanceof HttpServletRequest) {
            return ((HttpServletRequest) request).getHeader(headerName);
        } else if (request instanceof ArmeriaAuthInformation.ArmeriaRequest) {
            return ((ArmeriaAuthInformation.ArmeriaRequest) request).getHeader(headerName);
        }
        return null;
    }

    @Override
    /** 设置响应头部 */
    protected void setResponseHeader(Object response, String headerName, String headerValue) {
        if (response instanceof HttpServletResponse) {
            ((HttpServletResponse) response).setHeader(headerName, headerValue);
        } else if (response instanceof ArmeriaAuthInformation.ArmeriaResponse) {
            ((ArmeriaAuthInformation.ArmeriaResponse) response).setHeader(headerName, headerValue);
        }
    }

    @Override
    /** 获取远程地址 */
    protected String getRemoteAddress(Object request) {
        if (request instanceof HttpServletRequest) {
            return ((HttpServletRequest) request).getRemoteAddr();
        } else if (request instanceof ArmeriaAuthInformation.ArmeriaRequest) {
            return ((ArmeriaAuthInformation.ArmeriaRequest) request).getRemoteAddr();
        }
        return null;
    }

    @Override
    /** 获取请求attribute */
    protected Object getRequestAttribute(Object request, String attributeName) {
        if (request instanceof HttpServletRequest) {
            return ((HttpServletRequest) request).getAttribute(attributeName);
        } else if (request instanceof ArmeriaAuthInformation.ArmeriaRequest) {
            return ((ArmeriaAuthInformation.ArmeriaRequest) request).getAttribute(attributeName);
        }
        return null;
    }

    @Override
    /** 设置请求attribute */
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
 // Armeria 协议下 响应 不是 httpservlet响应，安全转换，Cookie 由客户端自行处理
        HttpServletResponse httpResponse = (getResponse() instanceof HttpServletResponse)
                ? (HttpServletResponse) getResponse() : null;
 // 安全转换 ext 映射：将所有 值 转为 字符串，避免 JSON 反序列化产生 Integer 等类型导致 类cast异常
        Object rawExt = MapUtils.getObject(this.getBodyData(), "x-oauth-ext");
        Map<String, String> extMap = null;
        if (rawExt instanceof Map) {
            extMap = new java.util.HashMap<>();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) rawExt).entrySet()) {
                if (entry.getKey() != null) {
                    extMap.put(String.valueOf(entry.getKey()),
                            entry.getValue() != null ? String.valueOf(entry.getValue()) : null);
                }
            }
        }
        return new LoginAuthorization(this,
                MapUtils.getString(this.getBodyData(), "x-oauth-username"),
                MapUtils.getString(this.getBodyData(), "x-oauth-password"),
                MapUtils.getString(this.getBodyData(), "x-oauth-auth-type"),
                MapUtils.getString(this.getBodyData(), "x-oauth-param-app-name"),
                extMap,
                httpResponse,
                getAuthServerProperties(),
                sysUserLogService,
                applicationContext,
                loginCheck);
    }
}
