package com.chua.starter.oauth.sso.support.information;

import com.chua.common.support.converter.Converter;
import com.chua.common.support.crypto.Codec;
import com.chua.common.support.json.Json;
import com.chua.common.support.json.JsonObject;
import com.chua.common.support.utils.MapUtils;
import com.chua.common.support.utils.SignUtils;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.parser.OauthAuthorization;
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
public class OauthAuthInformation extends AbstractAuthInformation {

    public OauthAuthInformation(AuthServerProperties authServerProperties, String data, HttpServletRequest request, HttpServletResponse response, ApplicationContext applicationContext, LoginCheck loginCheck) {
        super(authServerProperties, data, request, response, applicationContext, loginCheck);
    }


    /**
     * 获取授权信息
     * @return 授权信息
     */
    @Override
    public OauthAuthorization authorization() {
        return new OauthAuthorization(this,
                MapUtils.getString(this.bodyData, "x-oauth-token"),
                MapUtils.getString(this.bodyData, "x-oauth-cookie"),
                MapUtils.getString(this.bodyData, "x-oauth-sub-protocol"),
                applicationContext,
                loginCheck);
    }
}
