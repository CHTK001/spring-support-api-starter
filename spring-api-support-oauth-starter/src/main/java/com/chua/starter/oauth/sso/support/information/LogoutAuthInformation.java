package com.chua.starter.oauth.sso.support.information;

import com.chua.common.support.utils.MapUtils;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.parser.LoginAuthorization;
import com.chua.starter.oauth.sso.support.parser.LogoutAuthorization;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.Map;

/**
 * 鉴权信息
 *
 * @author CH
 */
@Slf4j
public class LogoutAuthInformation extends AbstractAuthInformation {


    public LogoutAuthInformation(AuthServerProperties authServerProperties,
                                 String data,
                                 HttpServletRequest request,
                                 HttpServletResponse response,
                                 ApplicationContext applicationContext,
                                 LoginCheck loginCheck) {
        super(authServerProperties, data, request, response, applicationContext, loginCheck);
    }

    /**
     * 获取授权信息
     * @return 授权信息
     */
    @SuppressWarnings("unchecked")
    @Override
    public LogoutAuthorization authorization() {
        return new LogoutAuthorization(this,
                MapUtils.getString(this.bodyData, "x-oauth-token"),
                MapUtils.getString(this.bodyData, "x-oauth-cookie"),
                MapUtils.getString(this.bodyData, "x-oauth-uid"),
                MapUtils.getString(this.bodyData, "x-oauth-logout-type"),
                response,
                authServerProperties,
                applicationContext,
                loginCheck);
    }
}
