package com.chua.starter.oauth.sso.support.security;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;

/**
 * gRPC安全工具类
 * <p>
 * 为gRPC协议提供安全机制支持，包括：
 * 1. 请求签名生成和验证
 * 2. 数据加密和解密
 * 3. 防重放攻击检查
 * 4. 访问密钥验证
 * </p>
 *
 * @author CH
 */
@Slf4j
public class GrpcSecurityUtils {

    private static final String SIGN_ALGORITHM = "SHA-256";
    private static final String ENCRYPT_ALGORITHM = "AES";
    private static final String CHARSET = "UTF-8";
    private static final long DEFAULT_MAX_AGE_MILLIS = 5 * 60 * 1000; // 5分钟

    /**
     * 生成请求签名
     * <p>
     * 基于请求参数生成MD5签名，确保请求完整性。
     * </p>
     *
     * @param requestData 请求数据
     * @param secretKey   秘密密钥
     * @return 签名字符串
     */
    public static String generateSign(Map<String, Object> requestData, String secretKey) {
        if (requestData == null || secretKey == null) {
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
                
                // 跳过签名字段本身
                if ("x-oauth-sign".equals(key) || value == null) {
                    continue;
                }
                
                if (signBuilder.length() > 0) {
                    signBuilder.append("&");
                }
                signBuilder.append(key).append("=").append(value.toString());
            }
            
            // 添加秘密密钥
            signBuilder.append("&secret=").append(secretKey);
            
            // 生成MD5签名
            return md5(signBuilder.toString());
            
        } catch (Exception e) {
            log.error("生成gRPC请求签名失败", e);
            return "";
        }
    }

    /**
     * 验证请求签名
     * <p>
     * 验证客户端提供的签名是否正确。
     * </p>
     *
     * @param sign        客户端提供的签名
     * @param requestData 请求数据
     * @param secretKey   秘密密钥
     * @return 签名是否有效
     */
    public static boolean verifySign(String sign, Map<String, Object> requestData, String secretKey) {
        if (sign == null || requestData == null || secretKey == null) {
            return false;
        }

        String expectedSign = generateSign(requestData, secretKey);
        return sign.equals(expectedSign);
    }

    /**
     * 检查请求时间戳，防止重放攻击
     * <p>
     * 验证请求时间戳是否在允许的时间窗口内。
     * </p>
     *
     * @param timestamp 请求时间戳
     * @return 时间戳是否有效
     */
    public static boolean isValidTimestamp(long timestamp) {
        return isValidTimestamp(timestamp, DEFAULT_MAX_AGE_MILLIS);
    }

    /**
     * 检查请求时间戳，防止重放攻击
     * <p>
     * 验证请求时间戳是否在允许的时间窗口内。
     * </p>
     *
     * @param timestamp   请求时间戳
     * @param maxAgeMillis 最大允许时间差（毫秒）
     * @return 时间戳是否有效
     */
    public static boolean isValidTimestamp(long timestamp, long maxAgeMillis) {
        long currentTime = System.currentTimeMillis();
        long timeDiff = Math.abs(currentTime - timestamp);
        return timeDiff <= maxAgeMillis;
    }

    /**
     * 验证访问密钥和秘密密钥
     * <p>
     * 检查客户端提供的AK/SK是否有效。
     * </p>
     *
     * @param accessKey         客户端提供的访问密钥
     * @param secretKey         客户端提供的秘密密钥
     * @param expectedAccessKey 期望的访问密钥
     * @param expectedSecretKey 期望的秘密密钥
     * @return 密钥是否有效
     */
    public static boolean verifyAccessKeys(String accessKey, String secretKey,
                                         String expectedAccessKey, String expectedSecretKey) {
        if (accessKey == null || secretKey == null ||
            expectedAccessKey == null || expectedSecretKey == null) {
            return false;
        }

        return accessKey.equals(expectedAccessKey) && secretKey.equals(expectedSecretKey);
    }

    /**
     * AES加密
     * <p>
     * 使用AES算法加密数据。
     * </p>
     *
     * @param data 待加密数据
     * @param key  加密密钥
     * @return 加密后的Base64字符串
     */
    public static String encrypt(String data, String key) {
        if (data == null || key == null) {
            return data;
        }

        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ENCRYPT_ALGORITHM);
            Cipher cipher = Cipher.getInstance(ENCRYPT_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            
            byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedData);
            
        } catch (Exception e) {
            log.error("AES加密失败", e);
            return data;
        }
    }

    /**
     * AES解密
     * <p>
     * 使用AES算法解密数据。
     * </p>
     *
     * @param encryptedData 加密的Base64字符串
     * @param key           解密密钥
     * @return 解密后的原始数据
     */
    public static String decrypt(String encryptedData, String key) {
        if (encryptedData == null || key == null) {
            return encryptedData;
        }

        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ENCRYPT_ALGORITHM);
            Cipher cipher = Cipher.getInstance(ENCRYPT_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            
            byte[] decodedData = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedData = cipher.doFinal(decodedData);
            return new String(decryptedData, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            log.error("AES解密失败", e);
            return encryptedData;
        }
    }

    /**
     * 生成随机AES密钥
     * <p>
     * 生成用于AES加密的随机密钥。
     * </p>
     *
     * @return Base64编码的AES密钥
     */
    public static String generateAESKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ENCRYPT_ALGORITHM);
            keyGenerator.init(128); // AES-128
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            log.error("生成AES密钥失败", e);
            return "";
        }
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
            MessageDigest md = MessageDigest.getInstance(SIGN_ALGORITHM);
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
}
