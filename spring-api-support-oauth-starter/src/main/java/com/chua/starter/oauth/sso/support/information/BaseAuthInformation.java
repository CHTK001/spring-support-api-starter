package com.chua.starter.oauth.sso.support.information;

import com.chua.common.support.converter.Converter;
import com.chua.starter.common.support.algorithm.crypto.Codec;
import com.chua.common.support.lang.json.Json;
import com.chua.common.support.lang.json.JsonObject;
import com.chua.common.support.utils.IdUtils;
import com.chua.starter.common.support.utils.SignUtils;
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
 * @since 4.0.0
 */
@Slf4j
public abstract class BaseAuthInformation implements AuthInformation {

    /**
     * 解密结果缓存
      * 键: 标志 + uuid (签名包含请求内容哈希)
      * 值: 解密后的主体数据
     * 过期时间: 30秒 (短期缓存，防止相同请求重复解密)
     */
    private static final Cache<String, Map<String, Object>> DECRYPT_CACHE = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .recordStats()
            .build();

    /** 认证服务器属性 */
    protected final AuthServerProperties authServerProperties;
    /** 数据 */
    private final String data;
    /** 请求 */
    protected final Object request;
    /** 响应 */
    protected final Object response;
    /** Application上下文 */
    protected final ApplicationContext applicationContext;
    /** Logincheck */
    protected final LoginCheck loginCheck;
    /** 协议 */
    private final String protocol;
    /** UUID */
    protected final String uuid;
    /** 串行 */
    private final String serial;
    /** 标志 */
    private final String sign;
    /** Access密钥 */
    private final String accessKey;
    /** isencode */
    protected final boolean isEncode;
    protected final Map<String, Object> bodyData = new LinkedHashMap<>(); // 主体数据
    /** 加密 */
    private final String encryption;
    /** 成功 */
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
            StringUtils.defaultIfBlank(getRequestHeader(request, "x-oauth-encode"), "false"), 
            Boolean.class);
        
        this.decodeData();
    }

    /**
     * 抽象方法：获取请求头信息
     * 不同协议需要实现自己的头信息获取逻辑
     * @param request 请求
     * @param headerName 头部名称
     * @return 获取请求头部的结果
     */
    protected abstract String getRequestHeader(Object request, String headerName);

    /**
     * 抽象方法：设置响应头信息
     * 不同协议需要实现自己的头信息设置逻辑
     * @param response 响应
     * @param headerName 头部名称
     * @param headerValue 头部值
     */
    protected abstract void setResponseHeader(Object response, String headerName, String headerValue);

    /**
     * 抽象方法：获取远程地址
     * 不同协议需要实现自己的地址获取逻辑
     * @param request 请求
     * @return 获取远程地址的结果
     */
    protected abstract String getRemoteAddress(Object request);

    /**
     * 抽象方法：获取请求属性
     * 不同协议需要实现自己的属性获取逻辑
     * @param request 请求
     * @param attributeName attribute名称
     * @return 获取请求attribute的结果
     */
    protected abstract Object getRequestAttribute(Object request, String attributeName);

    /**
     * 抽象方法：设置请求属性
     * 不同协议需要实现自己的属性设置逻辑
     * @param request 请求
     * @param attributeName attribute名称
     * @param attributeValue attribute值
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
        
        log.debug("[decodeData] uuid={}, isEncode={}, encryption={}, data长度={}, serial长度={}", 
                uuid, isEncode, encryption, 
                data != null ? data.length() : 0,
                serial != null ? serial.length() : 0);
        
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
                log.debug("[decodeData] 未加密模式，直接解析JSON数据");
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
            
            if (decodeKey == null) {
                log.error("[decodeData] serial 解密失败，无法获取数据密钥，uuid={}, serial长度={}", 
                        uuid, serial != null ? serial.length() : 0);
                logRequestCompletion(startTime, clientIp);
                return;
            }
            
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
            log.error("[decodeData] 认证数据解析异常: {}, uuid: {}, 耗时: {}ms", e.getMessage(), uuid, System.currentTimeMillis() - startTime);
            log.error("[decodeData] 解析异常详情", e);
        } finally {
            // 结束解密 Span
            if (traceContext != null) {
                traceContext.endSpan();
            }
        }
        
        logRequestCompletion(startTime, clientIp);
    }

    /**
      * 构建解密缓存键
     * 基于签名和uuid，因为签名是根据请求内容计算的
     * @return 构建decrypt缓存键的结果
     */
    private String buildDecryptCacheKey() {
        if (StringUtils.isBlank(sign) || StringUtils.isBlank(uuid)) {
            return null;
        }
        return sign + ":" + uuid;
    }
    
    /**
     * 获取解密缓存统计信息
     * @return 获取decrypt缓存stats的结果
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
     * @param startTime 启动时间
     * @param clientIp 客户端ip
     */
    private void logRequestCompletion(long startTime, String clientIp) {
        long cost = System.currentTimeMillis() - startTime;
        log.debug("解析耗时: {}ms, 结果: {}", cost, success.get() ? "成功" : "失败");
    }

    /**
     * 脱敏密钥
     * @param key 键
     * @return mask键的结果
     */
    private String maskKey(String key) {
        if (key == null || key.length() < 8) {
            return "***";
        }
        return key.substring(0, 4) + "..." + key.substring(key.length() - 4);
    }

    @Override
    /** 记录错误消息 */
    public String errorMessage() {
        if (!success.get()) {
            return "解析失败!";
        }
        return "sign签名认证失败!";
    }

    @Override
    /** 是否Successful */
    public boolean isSuccessful() {
        if (!success.get()) {
            return false;
        }
        if (!authServerProperties.isSignVerify()) {
            return true;
        }
        return SignUtils.verifyMd5Sign(this.bodyData, this.sign);
    }

    @Override
    /** 获取认证服务端属性 */
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
     * @return 获取响应的结果
     */
    protected Object getResponse() {
        return response;
    }

    /**
     * 获取请求对象
     * @return 获取请求的结果
     */
    protected Object getRequest() {
        return request;
    }

    /**
      * 获取登录检测服务（供认证处理器解析 令牌解析器 使用）
     * @return 获取login检查的结果
     */
    public LoginCheck getLoginCheck() {
        return loginCheck;
    }

    /**
      * 获取 Spring 应用上下文（供认证处理器解析 令牌解析器 使用）
     * @return 获取application上下文的结果
     */
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    // Getter方法
    /**
     * 获取Uuid
     *
     * @return 获取uuid的结果
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * 获取串行
     *
     * @return 获取串行的结果
     */
    public String getSerial() {
        return serial;
    }

    /**
     * 获取协议
     *
     * @return 获取协议的结果
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * 获取标志
     *
     * @return 获取标志的结果
     */
    public String getSign() {
        return sign;
    }

    /**
     * 是否编码
     *
     * @return 是否encode的结果
     */
    public boolean isEncode() {
        return isEncode;
    }

    /**
     * 获取主体数据
     *
     * @return 获取主体数据的结果
     */
    public Map<String, Object> getBodyData() {
        return bodyData;
    }

    /**
     * 获取加密
     *
     * @return 获取加密的结果
     */
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
