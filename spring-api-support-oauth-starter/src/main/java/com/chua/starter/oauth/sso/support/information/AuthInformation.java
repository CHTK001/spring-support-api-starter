package com.chua.starter.oauth.sso.support.information;

import com.chua.common.support.converter.Converter;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.parser.Authorization;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.HashMap;
import java.util.Map;

/**
 * 鉴权信息
 *
 * @author CH
 */
public interface AuthInformation {

    /**
     * 鉴权信息
     *
     * @return 鉴权信息
     */
    Authorization authorization();


    /**
     * 错误信息
     *
     * @return 错误信息
     */
    String errorMessage();
    /**
     * 是否成功
     *
     * @return 是否成功
     */
    boolean isSuccessful();

    /**
     * 获取认证信息
     *
     * @param data 数据
     * @return 认证信息
     */
    String encodeHex(String data);
    /**
     * 获取认证信息
     *
     * @return 认证信息
     */
    AuthServerProperties getAuthServerProperties();
}
