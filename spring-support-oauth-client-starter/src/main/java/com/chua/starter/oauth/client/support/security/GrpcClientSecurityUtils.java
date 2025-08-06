package com.chua.starter.oauth.client.support.security;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;

/**
 * gRPC客户端安全工具类
 * <p>
 * 为gRPC客户端协议提供安全机制支持，包括：
 * 1. 请求签名生成
 * 2. 数据加密和解密
 * 3. 防重放攻击检查
 * 4. 访问密钥验证
 * </p>
 *
 * @author CH
 */
@Slf4j
public class GrpcClientSecurityUtils {

    private static final String CHARSET = "UTF-8";

    /**
     * 生成请求签名
     * <p>
     * 基于请求参数生成MD5签名，确保请求完整性。
     * </p>
     *
     * @param requestData 请求数据
     * @return 签名字符串
     */
    public static String generateSignFromMap(Map<String, Object> requestData) {
        if (requestData == null) {
            return "";
        }

        try {
            // 构建签名字符串
            StringBuilder signBuilder = new StringBuilder();
            
            // 按键名排序
            TreeMap<String, Object> sortedData = new TreeMap<>(requestData);
            
            for (Map.Entry<String, Object> entry : sortedData.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                // 跳过签名字段本身和空值
                if ("x-oauth-sign".equals(key) || value == null) {
                    continue;
                }
                
                if (signBuilder.length() > 0) {
                    signBuilder.append("&");
                }
                signBuilder.append(key).append("=").append(value.toString());
            }
            
            // 生成MD5签名
            return md5(signBuilder.toString());
            
        } catch (Exception e) {
            log.error("生成gRPC客户端请求签名失败", e);
            return "";
        }
    }

    /**
     * 验证响应签名
     * <p>
     * 验证服务端返回的响应签名是否正确。
     * </p>
     *
     * @param sign         服务端提供的签名
     * @param responseData 响应数据
     * @return 签名是否有效
     */
    public static boolean verifyResponseSign(String sign, Map<String, Object> responseData) {
        if (sign == null || responseData == null) {
            return false;
        }

        String expectedSign = generateSignFromMap(responseData);
        return sign.equals(expectedSign);
    }

    /**
     * 检查响应时间戳，防止重放攻击
     * <p>
     * 验证响应时间戳是否在允许的时间窗口内。
     * </p>
     *
     * @param timestamp    响应时间戳
     * @param maxAgeMillis 最大允许时间差（毫秒）
     * @return 时间戳是否有效
     */
    public static boolean isValidResponseTimestamp(long timestamp, long maxAgeMillis) {
        long currentTime = System.currentTimeMillis();
        long timeDiff = Math.abs(currentTime - timestamp);
        return timeDiff <= maxAgeMillis;
    }

    /**
     * 生成随机UUID
     * <p>
     * 生成用于防重放攻击的随机UUID。
     * </p>
     *
     * @return 随机UUID字符串
     */
    public static String generateUUID() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 获取当前时间戳
     * <p>
     * 获取用于请求签名的当前时间戳。
     * </p>
     *
     * @return 当前时间戳（毫秒）
     */
    public static long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * MD5哈希
     * <p>
     * 计算字符串的MD5哈希值。
     * </p>
     *
     * @param input 输入字符串
     * @return MD5哈希值（小写十六进制）
     */
    private static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
            
        } catch (NoSuchAlgorithmException e) {
            log.error("MD5哈希计算失败", e);
            return "";
        }
    }

    /**
     * SHA-256哈希
     * <p>
     * 计算字符串的SHA-256哈希值。
     * </p>
     *
     * @param input 输入字符串
     * @return SHA-256哈希值（小写十六进制）
     */
    public static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
            
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256哈希计算失败", e);
            return "";
        }
    }

    /**
     * 构建签名数据Map
     * <p>
     * 为gRPC请求构建用于签名的数据Map。
     * </p>
     *
     * @param accessKey 访问密钥
     * @param secretKey 秘密密钥
     * @param timestamp 时间戳
     * @param uuid      请求UUID
     * @param extraData 额外数据
     * @return 签名数据Map
     */
    public static Map<String, Object> buildSignDataMap(String accessKey, String secretKey, 
                                                      long timestamp, String uuid, 
                                                      Map<String, Object> extraData) {
        Map<String, Object> signData = new TreeMap<>();
        signData.put("x-oauth-access-key", accessKey);
        signData.put("x-oauth-secret-key", secretKey);
        signData.put("x-oauth-timestamp", timestamp);
        signData.put("x-oauth-uuid", uuid);
        
        if (extraData != null) {
            signData.putAll(extraData);
        }
        
        return signData;
    }

    /**
     * 验证必要的安全参数
     * <p>
     * 检查gRPC请求的必要安全参数是否完整。
     * </p>
     *
     * @param accessKey 访问密钥
     * @param secretKey 秘密密钥
     * @param timestamp 时间戳
     * @param uuid      请求UUID
     * @param sign      请求签名
     * @return 参数是否完整
     */
    public static boolean validateSecurityParams(String accessKey, String secretKey, 
                                                long timestamp, String uuid, String sign) {
        if (accessKey == null || accessKey.trim().isEmpty()) {
            log.warn("gRPC请求缺少访问密钥");
            return false;
        }
        
        if (secretKey == null || secretKey.trim().isEmpty()) {
            log.warn("gRPC请求缺少秘密密钥");
            return false;
        }
        
        if (timestamp <= 0) {
            log.warn("gRPC请求时间戳无效");
            return false;
        }
        
        if (uuid == null || uuid.trim().isEmpty()) {
            log.warn("gRPC请求缺少UUID");
            return false;
        }
        
        if (sign == null || sign.trim().isEmpty()) {
            log.warn("gRPC请求缺少签名");
            return false;
        }
        
        return true;
    }
}
