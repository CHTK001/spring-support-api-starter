package com.chua.starter.oauth.sso.support.information;

import com.chua.common.support.converter.Converter;
import com.chua.common.support.crypto.Codec;
import com.chua.common.support.json.Json;
import com.chua.common.support.json.JsonObject;
import com.chua.common.support.utils.MapUtils;
import com.chua.common.support.utils.SignUtils;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.parser.Authorization;
import com.chua.starter.oauth.sso.support.parser.LoginAuthorization;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 鉴权信息
 *
 * @author CH
 */
@Slf4j
public class LoginAuthInformation extends AbstractAuthInformation {


    public LoginAuthInformation(AuthServerProperties authServerProperties,
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
    public LoginAuthorization authorization() {
        return new LoginAuthorization(this,
                MapUtils.getString(this.bodyData, "x-oauth-username"),
                MapUtils.getString(this.bodyData, "x-oauth-password"),
                MapUtils.getString(this.bodyData, "x-oauth-auth-type"),
                (Map<String, String>) MapUtils.getObject(this.bodyData, "x-oauth-ext"),
                response,
                authServerProperties,
                applicationContext,
                loginCheck);
    }
}
