package com.chua.starter.oauth.sso.support.protocol;

import com.chua.common.support.lang.json.Json;
import com.chua.common.support.lang.json.JsonObject;
import com.chua.starter.common.support.algorithm.crypto.Codec;
import com.chua.starter.common.support.network.UserAgent;
import com.chua.starter.common.support.utils.SignUtils;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;
import com.chua.starter.oauth.sso.support.resolver.AkSkDecryptor;
import com.chua.starter.oauth.sso.support.trace.ServerTraceContext;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * OAuth 协议报文解码器
 *
 * <p>从 common-starter 协议无关的请求头/报文体中解析并(可选)解密 OAuth 认证参数。
 * 逻辑与原 {@code BaseAuthInformation.decodeData()} 保持一致:根据
 * {@code x-oauth-encode} 头判断是否加密,加密时用 {@code x-oauth-uuid}/{@code x-oauth-serial}
 * 解出对称密钥再解密 body;可选 AK/SK 二次解密;带 30s 解密缓存与 MD5 签名校验。</p>
 *
 * @author CH
 * @since 4.0.0.42
 */
@Slf4j
public class OauthProtocolDecoder {

    /**
     * 解密结果缓存(与 BaseAuthInformation 一致的 30s 短期缓存)
     */
    private static final Cache<String, Map<String, Object>> DECRYPT_CACHE = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build();

    private final AuthServerProperties authServerProperties;
    private final String uuid;
    private final String serial;
    private final String sign;
    private final String accessKey;
    private final boolean isEncode;
    private final String encryption;
    private final Map<String, Object> bodyData = new LinkedHashMap<>();
    private boolean success = false;

    public OauthProtocolDecoder(AuthServerProperties authServerProperties,
                                String data,
                                String uuid,
                                String serial,
                                String sign,
                                String accessKey,
                                String encodeHeader) {
        this.authServerProperties = authServerProperties;
        this.encryption = authServerProperties.getEncryption();
        this.uuid = uuid;
        this.serial = serial;
        this.sign = sign;
        this.accessKey = accessKey;
        this.isEncode = Boolean.parseBoolean(encodeHeader == null ? "false" : encodeHeader);
        this.decodeData(data);
    }

    /**
     * 解密报文体(与 BaseAuthInformation.decodeData 逻辑一致)
     */
    private void decodeData(String data) {
        var startTime = System.currentTimeMillis();
        var traceContext = ServerTraceContext.current();
        if (traceContext != null) {
            traceContext.startSpan("server_decrypt");
        }
        try {
            var cacheKey = buildDecryptCacheKey();
            if (cacheKey != null) {
                var cached = DECRYPT_CACHE.getIfPresent(cacheKey);
                if (cached != null) {
                    this.bodyData.putAll(cached);
                    this.success = true;
                    return;
                }
            }

            if (!isEncode) {
                var jsonObject = Json.getJsonObject(data);
                if (jsonObject != null) {
                    this.bodyData.putAll(jsonObject);
                    this.success = true;
                }
                return;
            }

            var decodeKey = Codec.build(encryption, uuid).decodeHex(serial);
            if (decodeKey == null) {
                log.error("[OauthProtocolDecoder] serial 解密失败, uuid={}, serial长度={}",
                        uuid, serial != null ? serial.length() : 0);
                return;
            }
            var decryptedData = Codec.build(encryption, decodeKey).decodeHex(data);

            // AK/SK 二次解密
            if (authServerProperties.isOpenCheckAkSk() && accessKey != null && !accessKey.isBlank()) {
                var akSkDecrypted = AkSkDecryptor.decrypt(accessKey, decryptedData, encryption);
                if (akSkDecrypted != null) {
                    decryptedData = akSkDecrypted;
                }
            }

            var jsonObject = Json.getJsonObject(decryptedData);
            if (jsonObject != null) {
                this.bodyData.putAll(jsonObject);
                this.success = true;
                if (cacheKey != null) {
                    DECRYPT_CACHE.put(cacheKey, new LinkedHashMap<>(bodyData));
                }
            }
        } catch (Exception e) {
            log.error("[OauthProtocolDecoder] 认证数据解析异常: {}", e.getMessage());
        } finally {
            if (traceContext != null) {
                traceContext.endSpan();
            }
        }
        log.debug("[OauthProtocolDecoder] 解析耗时: {}ms, 结果: {}", System.currentTimeMillis() - startTime, success ? "成功" : "失败");
    }

    private String buildDecryptCacheKey() {
        if (sign == null || sign.isBlank() || uuid == null || uuid.isBlank()) {
            return null;
        }
        return sign + ":" + uuid;
    }

    /**
     * 是否成功解析
     */
    public boolean isSuccessful() {
        return success;
    }

    /**
     * 是否加密协议(客户端 x-oauth-encode=true 时响应也需加密)
     */
    public boolean isEncode() {
        return isEncode;
    }

    /**
     * 加密算法
     */
    public String encryption() {
        return encryption;
    }

    /**
     * 是否通过签名校验
     */
    public boolean isSignValid() {
        if (!success) {
            return false;
        }
        if (!authServerProperties.isSignVerify()) {
            return true;
        }
        return SignUtils.verifyMd5Sign(this.bodyData, this.sign);
    }

    /**
     * 获取解析后的业务参数
     */
    public Map<String, Object> getBodyData() {
        return bodyData;
    }

    /**
     * 读取业务参数
     */
    public String getString(String name) {
        Object value = bodyData.get(name);
        return value != null ? String.valueOf(value) : null;
    }

    /**
     * 读取客户端地址(从请求头 x-oauth-param-address,无则 null)
     */
    public String resolveClientAddress(String headerAddress) {
        if (headerAddress != null && !headerAddress.isBlank()) {
            return headerAddress;
        }
        String address = getString("x-oauth-param-address");
        return address != null && !address.isBlank() ? address : null;
    }
}
