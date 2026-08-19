package com.chua.starter.oauth.sso.support.information;

import com.chua.common.support.converter.Converter;
import com.chua.starter.common.support.algorithm.crypto.Codec;
import com.chua.common.support.lang.json.Json;
import com.chua.common.support.lang.json.JsonObject;
import com.chua.common.support.utils.IdUtils;
import com.chua.starter.common.support.utils.SignUtils;
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
 */
@Slf4j
public abstract class AbstractAuthInformation implements AuthInformation{

    /** Auth服务器属性 */
    protected final AuthServerProperties authServerProperties;
    /** 数据 */
    private final String data;
    /** 请求 */
    private final HttpServletRequest request;
    /** 响应 */
    protected final HttpServletResponse response;
    /** Application上下文 */
    protected final ApplicationContext applicationContext;
    /** Logincheck */
    protected final LoginCheck loginCheck;
    /** 协议 */
    private final String protocol;
    /** UUID */
    protected final String uuid;
    /** Serial */
    private final String serial;
    /** Sign */
    private final String sign;
    /** ISencode */
    protected final boolean isEncode;
    protected final Map<String, Object> bodyData = new LinkedHashMap<>();
    /** 加密 */
    private final String encryption;
    /** Success */
    private AtomicBoolean success = new AtomicBoolean(false);

    /**
     * 创建 AbstractAuthInformation 实例
     * @param authServerProperties authServerProperties
     * @param data data
     * @param request request
     * @param response response
     * @param applicationContext applicationContext
     * @param loginCheck loginCheck
     */
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
    /** 记录错误Message */
    public String errorMessage() {
        if(!success.get()) {
            return "解析失败!";
        }
        return "sign签名认证失败!";
    }

    @Override
    /** 是否Successful */
    public boolean isSuccessful() {
        return success.get() && (
                authServerProperties.isSignVerify() &&
                SignUtils.verifyMd5Sign(this.bodyData, this.sign));
    }

    @Override
    /** 获取AuthServerProperties */
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
