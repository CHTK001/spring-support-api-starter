package com.chua.starter.oauth.sso.support.information;

import com.chua.common.support.utils.MapUtils;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.parser.UpgradeAuthorization;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

/**
 * 鉴权信息
 *
 * @author CH
 */
@Slf4j
public class UpgradeAuthInformation extends AbstractAuthInformation {

    public UpgradeAuthInformation(AuthServerProperties authServerProperties, String data, HttpServletRequest request, HttpServletResponse response, ApplicationContext applicationContext, LoginCheck loginCheck) {
        super(authServerProperties, data, request, response, applicationContext, loginCheck);
    }

    /**
     * 获取授权信息
     * @return 授权信息
     */
    @Override
    public UpgradeAuthorization authorization() {
        return new UpgradeAuthorization(this,
                MapUtils.getString(this.bodyData, "x-oauth-token"),
                MapUtils.getString(this.bodyData, "x-oauth-cookie"),
                MapUtils.getString(this.bodyData, "x-oauth-refresh-token"),
                MapUtils.getString(this.bodyData, "x-oauth-upgrade-type"),
                applicationContext,
                loginCheck);
    }

}
