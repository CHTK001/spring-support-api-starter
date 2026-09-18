package com.chua.starter.oauth.sso.support.validator;

import com.chua.common.support.spi.annotations.Spi;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.entity.system.SysAppKey;
import com.chua.starter.oauth.sso.support.information.OauthAuthInformation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * HMAC-SHA256 appid 验证器
 * <p>
 * 实现基于 HMAC-SHA256 的签名验证算法：
 * 1. 从 头部 获取 x-时间、x-随机、x-标志、app键
 * 2. 根据请求方法计算 x-主体（POST: 主体的MD5，获取: 参数排序后的MD5）
 * 3. 拼接字符串：x-主体&x-随机&x-时间（如果没有主体或参数，则不拼接x-主体）
 * 4. 使用 HMAC-SHA256 和 appsecret 对拼接字符串加密得到签名
 * 5. 验证签名是否匹配
 * </p>
 *
 * @版本 1.0.0
 * @since 2025-01-05
 * @author CH
 */
@Slf4j
@Spi("hmac-sha256")
public class HmacSha256AppIdValidator implements AppIdValidator {

    /**
     * 请求头参数名: 时间戳
     */
    private static final String HEADER_X_TIME = "x-time";

    /**
     * 请求头参数名: 随机数
     */
    private static final String HEADER_X_RANDOM = "x-random";

    /**
     * 请求头参数名: 签名
     */
    private static final String HEADER_X_SIGN = "x-sign";

    /**
     * 请求头参数名: app键
     */
    private static final String HEADER_APP_KEY = "appKey";

    /**
     * 允许的时间戳最大偏差 (毫秒), 5 分钟
     */
    private static final long MAX_TIME_DIFF_MILLIS = 5L * 60 * 1000;

    /**
     * 签名拼接连接符
     */
    private static final String SIGN_JOINER = "&";

    /**
     * 验证签名（从 oauth认证信息 中获取签名参数）
     *
     * @param authInformation 认证信息
     * @param appKey          app键 信息
     * @return 验证结果
     */
    @Override
    public ValidationResult validate(OauthAuthInformation authInformation, SysAppKey appKey) {
        if (appKey == null) {
            return ValidationResult.failure("AppKey 不存在");
        }

        try {
 // 1. 从 头部 获取签名参数
            String xTime = authInformation.getRequestHeaderValue(HEADER_X_TIME);
            String xRandom = authInformation.getRequestHeaderValue(HEADER_X_RANDOM);
            String xSign = authInformation.getRequestHeaderValue(HEADER_X_SIGN);
            String appKeyFromHeader = authInformation.getRequestHeaderValue(HEADER_APP_KEY);

            // 验证必需参数
            if (StringUtils.isBlank(xTime) || StringUtils.isBlank(xRandom) || StringUtils.isBlank(xSign)) {
                log.debug("签名参数不完整: x-time={}, x-random={}, x-sign={}", 
                        maskValue(xTime), maskValue(xRandom), maskValue(xSign));
                return ValidationResult.failure("签名参数不完整");
            }

 // 验证 app键 是否匹配
            String dbAppKey = appKey.getSysAppKeyAppKey();
            if (StringUtils.isNotBlank(appKeyFromHeader) && !appKeyFromHeader.equals(dbAppKey)) {
                log.warn("AppKey 不匹配: header={}, db={}", maskAppKey(appKeyFromHeader), maskAppKey(dbAppKey));
                return ValidationResult.failure("AppKey 不匹配");
            }

 // 2. 获取 appsecret
            String appSecret = appKey.getSysAppKeySecret();
            if (StringUtils.isBlank(appSecret)) {
                log.warn("AppSecret 为空");
                return ValidationResult.failure("AppSecret 不存在");
            }

 // 3. 计算 x-主体
            String xBody = calculateXBody(authInformation);
            log.debug("计算 x-body: {}", maskValue(xBody));

            // 4. 拼接字符串
            String signString = buildSignString(xBody, xRandom, xTime);
            log.debug("签名字符串: {}", maskValue(signString));

            // 5. 计算签名
            String calculatedSign = calculateHmacSha256(signString, appSecret);
            log.debug("计算签名: {}, 期望签名: {}", maskValue(calculatedSign), maskValue(xSign));

            // 6. 验证签名
            if (!calculatedSign.equalsIgnoreCase(xSign)) {
                log.warn("签名验证失败: calculated={}, received={}", 
                        maskValue(calculatedSign), maskValue(xSign));
                return ValidationResult.failure("签名验证失败");
            }

            // 7. 验证时间戳（可选：检查时间戳是否在合理范围内，比如5分钟内）
            long requestTime = Long.parseLong(xTime);
            long currentTime = System.currentTimeMillis();
            long timeDiff = Math.abs(currentTime - requestTime);
            if (timeDiff > MAX_TIME_DIFF_MILLIS) {
                log.warn("时间戳过期: requestTime={}, currentTime={}, diff={}ms", 
                        requestTime, currentTime, timeDiff);
                return ValidationResult.failure("请求时间戳过期");
            }

            log.debug("签名验证成功");
            return ValidationResult.success();

        } catch (Exception e) {
            log.error("签名验证异常", e);
            return ValidationResult.failure("签名验证异常: " + e.getMessage());
        }
    }

    /**
     * 计算 x-主体
     * POST 请求：对 主体 进行 MD5 加密并转16进制
     * 获取 请求：将参数按 键 降序排列，拼接后用 MD5 加密并转16进制
     * @param authInformation 认证信息
     * @return calculatex主体的结果
     */
    private String calculateXBody(OauthAuthInformation authInformation) {
        Object requestObj = authInformation.getRequest();
        
        if (!(requestObj instanceof HttpServletRequest)) {
            log.warn("请求对象不是 HttpServletRequest，无法计算 x-body");
            return null;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) requestObj;
        String method = httpRequest.getMethod();

        try {
            if ("POST".equalsIgnoreCase(method)) {
 // POST 请求：对 主体 进行 MD5
                String body = getRequestBody(authInformation);
                if (StringUtils.isBlank(body)) {
                    return null; // 没有 主体，返回 空（不拼接 x-主体）
                }
                return md5Hex(body);
            } else if ("GET".equalsIgnoreCase(method)) {
 // 获取 请求：参数按 键 降序排列，拼接后 MD5
                String queryString = httpRequest.getQueryString();
                if (StringUtils.isBlank(queryString)) {
                    return null; // 没有参数，返回 空（不拼接 x-主体）
                }
                
                // 解析查询参数
                Map<String, String> params = parseQueryString(queryString);
                if (params.isEmpty()) {
                    return null;
                }
                
 // 按 键 降序排列并拼接
                String sortedParams = params.entrySet().stream()
                        .sorted(Map.Entry.<String, String>comparingByKey().reversed())
                        .map(entry -> entry.getKey() + "=" + entry.getValue())
                        .collect(Collectors.joining("&"));
                
                return md5Hex(sortedParams);
            }
        } catch (Exception e) {
            log.error("计算 x-body 失败", e);
        }

        return null;
    }

    /**
     * 获取请求体
     * <p>
     * 对于 POST 请求，主体 应该是原始的请求体字符串（JSON格式）
     * 在我们的系统中，数据 字段可能包含加密后的数据，需要尝试获取原始 主体
     * </p>
     * @param authInformation 认证信息
     * @return 获取请求主体的结果
     */
    private String getRequestBody(OauthAuthInformation authInformation) {
        Object requestObj = authInformation.getRequest();
        
 // 尝试从 请求 对象的 attribute 中获取原始 主体
        if (requestObj instanceof HttpServletRequest) {
            Object body = authInformation.getRequestAttributeValue("requestBody");
            if (body instanceof String) {
                return (String) body;
            }
        }
        
 // 尝试从 rsocket请求 获取 主体
        if (requestObj instanceof com.chua.starter.oauth.sso.support.information.RsocketAuthInformation.RsocketRequest) {
            com.chua.starter.oauth.sso.support.information.RsocketAuthInformation.RsocketRequest rsocketRequest = 
                    (com.chua.starter.oauth.sso.support.information.RsocketAuthInformation.RsocketRequest) requestObj;
            String body = rsocketRequest.getBody();
            if (StringUtils.isNotBlank(body)) {
                return body;
            }
        }
        
 // 尝试从 armeria请求 获取 主体（如果有）
        if (requestObj instanceof com.chua.starter.oauth.sso.support.information.ArmeriaAuthInformation.ArmeriaRequest) {
 // armeria请求 可能需要特殊处理
        }
        
 // 最后尝试从 数据 字段获取（可能是原始 主体 或加密后的数据）
        String data = authInformation.getData();
        if (StringUtils.isNotBlank(data)) {
 // 如果 数据 看起来像 JSON（以 { 或 [ 开头），可能是原始 主体
            String trimmed = data.trim();
            if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
                return data;
            }
        }
        
        return null;
    }

    /**
     * 解析查询字符串
     * @param queryString 查询字符串
     * @return 解析查询字符串的结果
     */
    private Map<String, String> parseQueryString(String queryString) {
        Map<String, String> params = new LinkedHashMap<>();
        if (StringUtils.isBlank(queryString)) {
            return params;
        }
        
        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String key = pair.substring(0, idx);
                String value = idx < pair.length() - 1 ? pair.substring(idx + 1) : "";
                params.put(key, value);
            }
        }
        
        return params;
    }

    /**
     * 构建签名字符串
     * 格式：x-主体&x-随机&x-时间（如果没有 x-主体，则为 x-随机&x-时间）
     * 注意：根据算法描述，拼接顺序是 x-主体、x-随机、x-时间
     * @param xBody x主体
     * @param xRandom x随机
     * @param xTime x时间
     * @return 构建标志字符串的结果
     */
    private String buildSignString(String xBody, String xRandom, String xTime) {
        if (StringUtils.isNotBlank(xBody)) {
 // 有 x-主体：x-主体&x-随机&x-时间
            return xBody + SIGN_JOINER + xRandom + SIGN_JOINER + xTime;
        } else {
 // 没有 x-主体：x-随机&x-时间
            return xRandom + SIGN_JOINER + xTime;
        }
    }

    /**
     * 计算 HMAC-SHA256 签名
     * @param data 数据
     * @param secret secret
     * @return calculateHmacSha256的结果
     */
    private String calculateHmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA256 计算失败", e);
        }
    }

    /**
     * MD5 加密并转16进制
     * @param data 数据
     * @return md5Hex的结果
     */
    private String md5Hex(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("MD5 计算失败", e);
        }
    }

    /**
     * 字节数组转16进制字符串
     * @param bytes bytes
     * @return bytes转为hex的结果
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 脱敏值
     * @param value 值
     * @return mask值的结果
     */
    private String maskValue(String value) {
        if (value == null || value.length() < 8) {
            return "***";
        }
        return value.substring(0, 4) + "..." + value.substring(value.length() - 4);
    }

    /**
     * 脱敏 app键
     * @param appKey app键
     * @return maskapp键的结果
     */
    private String maskAppKey(String appKey) {
        return maskValue(appKey);
    }

    /**
     * 判断当前验证器是否支持该 appid 签名方式。
     *
     * @param appId 原始 appid
     * @return 默认返回 true, 实际验证时会检查参数是否存在
     */
    @Override
    public boolean supports(String appId) {
 // 检查 头部 中是否有签名参数，如果有则支持
        return true; // 默认支持，实际验证时会检查参数是否存在
    }
}

