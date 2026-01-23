package com.chua.starter.oauth.sso.support.information;

import com.chua.common.support.base.converter.Converter;
import com.chua.common.support.crypto.Codec;
import com.chua.common.support.text.json.Json;
import com.chua.common.support.text.json.JsonObject;
import com.chua.common.support.core.utils.IdUtils;
import com.chua.common.support.core.utils.SignUtils;
import com.chua.starter.oauth.sso.support.check.LoginCheck;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import com.chua.starter.oauth.sso.support.resolver.AkSkDecryptor;
import com.chua.starter.oauth.sso.support.trace.ServerTraceContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 协议无关的认证信息抽象基类
 * 
 * @author CH
 */
@Slf4j
public abstract class BaseAuthInformation implements AuthInformation {

    /**
     * 解密结果缓存
     * key: sign + uuid (签名包含请求内容hash)
     * value: 解密后的bodyData
     * 过期时间: 30秒 (短期缓存，防止相同请求重复解密)
     */
    private static final Cache<String, Map<String, Object>> DECRYPT_CACHE = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .recordStats()
            .build();

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
    private final String accessKey;
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
        this.accessKey = getRequestHeader(request, "x-oauth-ak");
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
        
        // 获取追踪上下文
        ServerTraceContext traceContext = ServerTraceContext.current();
        if (traceContext != null) {
            traceContext.startSpan("server_decrypt");
        }
        
        log.debug("开始解析认证数据, uuid: {}, isEncode: {}, encryption: {}", uuid, isEncode, encryption);
        
        try {
            // 尝试从缓存获取解密结果
            String cacheKey = buildDecryptCacheKey();
            if (cacheKey != null) {
                Map<String, Object> cached = DECRYPT_CACHE.getIfPresent(cacheKey);
                if (cached != null) {
                    this.bodyData.putAll(cached);
                    success.set(true);
                    log.debug("使用解密缓存, cacheKey: {}, 字段数: {}", cacheKey.substring(0, 16) + "...", cached.size());
                    return;
                }
            }
            
            if (!isEncode) {
                log.debug("未加密模式，直接解析JSON数据");
                JsonObject jsonObject = Json.getJsonObject(data);
                if (jsonObject != null) {
                    this.bodyData.putAll(jsonObject);
                    success.set(true);
                    log.debug("JSON数据解析成功, 字段数: {}", bodyData.size());
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
            
            // AK/SK 二次解密（如果开启）
            if (authServerProperties.isOpenCheckAkSk() && StringUtils.isNotBlank(accessKey)) {
                log.debug("开启AK/SK二次解密, ak: {}", maskKey(accessKey));
                String akSkDecrypted = AkSkDecryptor.decrypt(accessKey, decryptedData, encryption);
                if (akSkDecrypted != null) {
                    decryptedData = akSkDecrypted;
                    log.debug("AK/SK解密成功, 解密后数据长度: {}", decryptedData.length());
                } else {
                    log.warn("AK/SK解密失败, ak: {}", maskKey(accessKey));
                }
            }
            
            JsonObject jsonObject = Json.getJsonObject(decryptedData);
            if (jsonObject != null) {
                this.bodyData.putAll(jsonObject);
                success.set(true);
                
                // 缓存解密结果
                if (cacheKey != null) {
                    DECRYPT_CACHE.put(cacheKey, new LinkedHashMap<>(bodyData));
                }
                
                log.debug("加密数据解析成功, 字段数: {}, 耗时: {}ms", bodyData.size(), System.currentTimeMillis() - startTime);
            } else {
                log.warn("加密数据解析失败，解密后的JSON为空");
            }
        } catch (Exception e) {
            log.error("认证数据解析异常: {}, uuid: {}, 耗时: {}ms", e.getMessage(), uuid, System.currentTimeMillis() - startTime);
            log.debug("解析异常详情", e);
        } finally {
            // 结束解密 Span
            if (traceContext != null) {
                traceContext.endSpan();
            }
        }
        
        logRequestCompletion(startTime, clientIp);
    }

    /**
     * 构建解密缓存key
     * 基于签名和uuid，因为签名是根据请求内容计算的
     */
    private String buildDecryptCacheKey() {
        if (StringUtils.isBlank(sign) || StringUtils.isBlank(uuid)) {
            return null;
        }
        return sign + ":" + uuid;
    }
    
    /**
     * 获取解密缓存统计信息
     */
    public static String getDecryptCacheStats() {
        return DECRYPT_CACHE.stats().toString();
    }
    
    /**
     * 清除解密缓存
     */
    public static void clearDecryptCache() {
        DECRYPT_CACHE.invalidateAll();
    }

    /**
     * 记录请求完成信息
     */
    private void logRequestCompletion(long startTime, String clientIp) {
        long cost = System.currentTimeMillis() - startTime;
        log.debug("解析耗时: {}ms, 结果: {}", cost, success.get() ? "成功" : "失败");
    }

    /**
     * 脱敏密钥
     */
    private String maskKey(String key) {
        if (key == null || key.length() < 8) {
            return "***";
        }
        return key.substring(0, 4) + "..." + key.substring(key.length() - 4);
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

    /**
     * 获取原始数据（供验证器使用）
     * @return 原始数据
     */
    protected String getData() {
        return data;
    }
}
