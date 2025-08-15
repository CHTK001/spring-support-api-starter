package com.chua.starter.oauth.sso.support.information;

import com.chua.common.support.converter.Converter;
import com.chua.common.support.crypto.Codec;
import com.chua.common.support.json.Json;
import com.chua.common.support.json.JsonObject;
import com.chua.common.support.utils.IdUtils;
import com.chua.common.support.utils.SignUtils;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
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
public abstract class AbstractAuthInformation implements AuthInformation{

    protected final AuthServerProperties authServerProperties;
    private final String data;
    private final HttpServletRequest request;
    protected final HttpServletResponse response;
    protected final ApplicationContext applicationContext;
    protected final LoginCheck loginCheck;
    private final String protocol;
    protected final String uuid;
    private final String serial;
    private final String sign;
    protected final boolean isEncode;
    protected final Map<String, Object> bodyData = new LinkedHashMap<>();
    private final String encryption;
    private AtomicBoolean success = new AtomicBoolean(false);

    public AbstractAuthInformation(AuthServerProperties authServerProperties,
                                   String data,
                                   HttpServletRequest request,
                                   HttpServletResponse response,
                                   ApplicationContext applicationContext,
                                   LoginCheck loginCheck) {
        this.authServerProperties = authServerProperties;
        this.encryption = authServerProperties.getEncryption();
        this.data = data;
        this.request = request;
        this.response = response;
        this.applicationContext = applicationContext;
        this.loginCheck = loginCheck;
        this.uuid = request.getHeader("x-oauth-uuid");
        this.serial = request.getHeader("x-oauth-serial");
        this.protocol = request.getHeader("x-oauth-protocol");
        this.sign = request.getHeader("x-oauth-sign");
        this.isEncode = Converter.convertIfNecessary(StringUtils.defaultIfBlank(request.getHeader("x-oauth-encode"), "true"), Boolean.class);
        this.decodeData();
    }

    /**
     * 注册数据
     */
    private void decodeData() {
        if(!isEncode) {
            JsonObject jsonObject = Json.getJsonObject(data);
            this.bodyData.putAll(jsonObject);
            return ;
        }
        String decodeKey = Codec.build(encryption, uuid).decodeHex(serial);
        JsonObject jsonObject = Json.getJsonObject(Codec.build(encryption, decodeKey).decodeHex(data));
        this.bodyData.putAll(jsonObject);
        success.set(true);
    }

    @Override
    public String errorMessage() {
        if(!success.get()) {
            return "解析失败!";
        }
        return "sign签名认证失败!";
    }

    @Override
    public boolean isSuccessful() {
        return success.get() && (
                authServerProperties.isSignVerify() &&
                SignUtils.verifySignFromMap(this.sign, this.bodyData));
    }

    @Override
    public AuthServerProperties getAuthServerProperties() {
        return authServerProperties;
    }

    /**
     * 16进制编码
     *
     * @param data
     * @return data
     */
    @Override
    public String encodeHex(String data) {
        if(!isEncode) {
            return data;
        }
        String newKey = IdUtils.createSimpleUuid();
        response.setHeader("x-oauth-response-serial", newKey);
        return Codec.build(encryption, newKey).encodeHex(data);
    }
}
