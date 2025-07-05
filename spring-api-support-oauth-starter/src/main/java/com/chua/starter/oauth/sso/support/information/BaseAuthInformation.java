package com.chua.starter.oauth.sso.support.information;

import com.chua.common.support.converter.Converter;
import com.chua.common.support.crypto.Codec;
import com.chua.common.support.json.Json;
import com.chua.common.support.json.JsonObject;
import com.chua.common.support.utils.IdUtils;
import com.chua.common.support.utils.SignUtils;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 协议无关的认证信息抽象基类
 * 
 * @author CH
 */
@Slf4j
public abstract class BaseAuthInformation implements AuthInformation {

    protected final AuthServerProperties authServerProperties;
    private final String data;
    protected final Object request;
    protected final Object response;
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

    /**
     * 构造函数 - 接受协议无关的请求/响应对象
     */
    public BaseAuthInformation(AuthServerProperties authServerProperties,
                              String data,
                              Object request,
                              Object response,
                              ApplicationContext applicationContext,
                              LoginCheck loginCheck) {
        this.authServerProperties = authServerProperties;
        this.encryption = authServerProperties.getEncryption();
        this.data = data;
        this.request = request;
        this.response = response;
        this.applicationContext = applicationContext;
        this.loginCheck = loginCheck;
        
        // 通过抽象方法获取协议无关的头信息
        this.uuid = getRequestHeader(request, "x-oauth-uuid");
        this.serial = getRequestHeader(request, "x-oauth-serial");
        this.protocol = getRequestHeader(request, "x-oauth-protocol");
        this.sign = getRequestHeader(request, "x-oauth-sign");
        this.isEncode = Converter.convertIfNecessary(
            StringUtils.defaultIfBlank(getRequestHeader(request, "x-oauth-encode"), "true"), 
            Boolean.class);
        
        this.decodeData();
    }

    /**
     * 抽象方法：获取请求头信息
     * 不同协议需要实现自己的头信息获取逻辑
     */
    protected abstract String getRequestHeader(Object request, String headerName);

    /**
     * 抽象方法：设置响应头信息
     * 不同协议需要实现自己的头信息设置逻辑
     */
    protected abstract void setResponseHeader(Object response, String headerName, String headerValue);

    /**
     * 抽象方法：获取远程地址
     * 不同协议需要实现自己的地址获取逻辑
     */
    protected abstract String getRemoteAddress(Object request);

    /**
     * 抽象方法：获取请求属性
     * 不同协议需要实现自己的属性获取逻辑
     */
    protected abstract Object getRequestAttribute(Object request, String attributeName);

    /**
     * 抽象方法：设置请求属性
     * 不同协议需要实现自己的属性设置逻辑
     */
    protected abstract void setRequestAttribute(Object request, String attributeName, Object attributeValue);

    /**
     * 注册数据
     */
    private void decodeData() {
        if (!isEncode) {
            JsonObject jsonObject = Json.getJsonObject(data);
            this.bodyData.putAll(jsonObject);
            return;
        }
        String decodeKey = Codec.build(encryption, uuid).decodeHex(serial);
        JsonObject jsonObject = Json.getJsonObject(Codec.build(encryption, decodeKey).decodeHex(data));
        this.bodyData.putAll(jsonObject);
        success.set(true);
    }

    @Override
    public String errorMessage() {
        if (!success.get()) {
            return "解析失败!";
        }
        return "sign签名认证失败!";
    }

    @Override
    public boolean isSuccessful() {
        return success.get() && SignUtils.verifySignFromMap(this.sign, this.bodyData);
    }

    @Override
    public AuthServerProperties getAuthServerProperties() {
        return authServerProperties;
    }

    /**
     * 16进制编码
     *
     * @param data 数据
     * @return 编码后的数据
     */
    @Override
    public String encodeHex(String data) {
        if (!isEncode) {
            return data;
        }
        String newKey = IdUtils.createSimpleUuid();
        // 通过抽象方法设置响应头
        setResponseHeader(getResponse(), "x-oauth-response-serial", newKey);
        return Codec.build(encryption, newKey).encodeHex(data);
    }

    /**
     * 获取响应对象
     */
    protected Object getResponse() {
        return response;
    }

    /**
     * 获取请求对象
     */
    protected Object getRequest() {
        return request;
    }

    // Getter方法
    public String getUuid() {
        return uuid;
    }

    public String getSerial() {
        return serial;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getSign() {
        return sign;
    }

    public boolean isEncode() {
        return isEncode;
    }

    public Map<String, Object> getBodyData() {
        return bodyData;
    }

    public String getEncryption() {
        return encryption;
    }
}
