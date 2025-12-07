package com.chua.starter.oauth.sso.support.information;

import com.chua.common.support.converter.Converter;
import com.chua.common.support.crypto.Codec;
import com.chua.common.support.json.Json;
import com.chua.common.support.json.JsonObject;
import com.chua.common.support.utils.IdUtils;
import com.chua.common.support.utils.SignUtils;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.controller.OAuthAdminController;
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
        long startTime = System.currentTimeMillis();
        String clientIp = getRemoteAddress(request);
        
        // Debug 模式下输出详细信息
        if (OAuthAdminController.isDebugMode()) {
            log.info("【DEBUG】========== 认证数据解析开始 ==========");
            log.info("【DEBUG】客户端IP: {}", clientIp);
            log.info("【DEBUG】UUID: {}", uuid);
            log.info("【DEBUG】Serial: {}", serial != null ? serial.substring(0, Math.min(16, serial.length())) + "..." : "null");
            log.info("【DEBUG】Protocol: {}", protocol);
            log.info("【DEBUG】IsEncode: {}", isEncode);
            log.info("【DEBUG】Encryption: {}", encryption);
            log.info("【DEBUG】Data长度: {}", data != null ? data.length() : 0);
        }
        
        log.debug("开始解析认证数据, uuid: {}, isEncode: {}, encryption: {}", uuid, isEncode, encryption);
        
        try {
            if (!isEncode) {
                log.debug("未加密模式，直接解析JSON数据");
                JsonObject jsonObject = Json.getJsonObject(data);
                if (jsonObject != null) {
                    this.bodyData.putAll(jsonObject);
                    success.set(true);
                    log.debug("JSON数据解析成功, 字段数: {}", bodyData.size());
                    
                    if (OAuthAdminController.isDebugMode()) {
                        log.info("【DEBUG】解析成功 - 字段: {}", bodyData.keySet());
                        logSensitiveFields();
                    }
                } else {
                    log.warn("JSON数据解析失败，数据为空或格式错误");
                }
                logRequestCompletion(startTime, clientIp);
                return;
            }
            
            log.debug("加密模式，开始解密数据");
            log.debug("serial长度: {}, data长度: {}", 
                    serial != null ? serial.length() : 0, 
                    data != null ? data.length() : 0);
            
            String decodeKey = Codec.build(encryption, uuid).decodeHex(serial);
            log.debug("解密后的密钥长度: {}", decodeKey != null ? decodeKey.length() : 0);
            
            String decryptedData = Codec.build(encryption, decodeKey).decodeHex(data);
            log.debug("解密后的数据长度: {}", decryptedData != null ? decryptedData.length() : 0);
            
            if (OAuthAdminController.isDebugMode()) {
                log.info("【DEBUG】解密密钥长度: {}", decodeKey != null ? decodeKey.length() : 0);
                log.info("【DEBUG】解密后数据长度: {}", decryptedData != null ? decryptedData.length() : 0);
            }
            
            JsonObject jsonObject = Json.getJsonObject(decryptedData);
            if (jsonObject != null) {
                this.bodyData.putAll(jsonObject);
                success.set(true);
                log.debug("加密数据解析成功, 字段数: {}, 耗时: {}ms", bodyData.size(), System.currentTimeMillis() - startTime);
                
                if (OAuthAdminController.isDebugMode()) {
                    log.info("【DEBUG】解密解析成功 - 字段: {}", bodyData.keySet());
                    logSensitiveFields();
                }
            } else {
                log.warn("加密数据解析失败，解密后的JSON为空");
            }
        } catch (Exception e) {
            log.error("认证数据解析异常: {}, uuid: {}, 耗时: {}ms", e.getMessage(), uuid, System.currentTimeMillis() - startTime);
            log.debug("解析异常详情", e);
            
            if (OAuthAdminController.isDebugMode()) {
                log.info("【DEBUG】解析异常: {}", e.getMessage());
            }
        }
        
        logRequestCompletion(startTime, clientIp);
    }

    /**
     * 记录请求完成信息
     */
    private void logRequestCompletion(long startTime, String clientIp) {
        long cost = System.currentTimeMillis() - startTime;
        if (OAuthAdminController.isDebugMode()) {
            log.info("【DEBUG】解析耗时: {}ms, 结果: {}", cost, success.get() ? "成功" : "失败");
            log.info("【DEBUG】========== 认证数据解析结束 ==========");
        }
    }

    /**
     * 输出敏感字段（脱敏）
     */
    private void logSensitiveFields() {
        String username = bodyData.get("x-oauth-username") != null ? 
                bodyData.get("x-oauth-username").toString() : null;
        String loginType = bodyData.get("x-oauth-login-type") != null ? 
                bodyData.get("x-oauth-login-type").toString() : null;
        String token = bodyData.get("x-oauth-token") != null ? 
                maskToken(bodyData.get("x-oauth-token").toString()) : null;
        
        log.info("【DEBUG】用户名: {}, 登录类型: {}, Token: {}", 
                username, loginType, token);
    }

    /**
     * 脱敏 Token
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 10) {
            return "***";
        }
        return token.substring(0, 6) + "..." + token.substring(token.length() - 4);
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
        return success.get() && (authServerProperties.isSignVerify() && SignUtils.verifySignFromMap(this.sign, this.bodyData));
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
