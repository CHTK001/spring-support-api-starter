package com.chua.starter.pay.support.handler.wechat.handler;

import com.chua.common.support.json.Json;
import com.chua.starter.pay.support.handler.entity.TransferBatchEntity;
import com.chua.starter.pay.support.handler.entity.TransferNotifyRequest;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.Base64;

/**
 * 微信支付转账回调通知处理器
 */
public class TransferNotifyHandler {
    private final String apiV3Key;
    private final X509Certificate wechatPayCertificate;

    public TransferNotifyHandler(String apiV3Key, X509Certificate certificate) {
        this.apiV3Key = apiV3Key;
        this.wechatPayCertificate = certificate;
    }

    public TransferBatchEntity handleNotify(
            String signature,
            String timestamp,
            String nonce,
            String serial,
            String requestBody
    ) throws Exception {
        // 1. 验证签名
        verifySignature(signature, timestamp, nonce, requestBody);

        // 2. 解析请求体
        TransferNotifyRequest notifyRequest = parseRequestBody(requestBody);

        // 3. 验证证书序列号
        if (!wechatPayCertificate.getSerialNumber().toString().equals(serial)) {
            throw new SecurityException("证书序列号不匹配");
        }

        // 4. 解密数据
        return decryptData(notifyRequest.getResource());
    }

    private void verifySignature(
            String signature,
            String timestamp,
            String nonce,
            String requestBody
    ) throws Exception {
        String message = buildMessage(timestamp, nonce, requestBody);
        boolean isValid = validateSignature(message, signature, wechatPayCertificate);

        if (!isValid) {
            throw new SecurityException("签名验证失败");
        }
    }

    private String buildMessage(String timestamp, String nonce, String body) {
        return timestamp + "\n" + nonce + "\n" + body + "\n";
    }

    private boolean validateSignature(
            String message,
            String signature,
            X509Certificate certificate
    ) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(certificate);
        sig.update(message.getBytes(StandardCharsets.UTF_8));
        return sig.verify(Base64.getDecoder().decode(signature));
    }

    private TransferNotifyRequest parseRequestBody(String requestBody) throws Exception {
        // 使用Gson或Jackson解析JSON
        return Json.fromJson(requestBody, TransferNotifyRequest.class);
    }

    private TransferBatchEntity decryptData(TransferNotifyRequest.Resource resource)
            throws Exception {
        // 初始化AES-GCM
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(apiV3Key.getBytes(StandardCharsets.UTF_8), "AES");
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128,
                resource.getNonce().getBytes(StandardCharsets.UTF_8));

        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
        cipher.updateAAD(resource.getAssociated_data().getBytes(StandardCharsets.UTF_8));

        byte[] decoded = Base64.getDecoder().decode(resource.getCiphertext());
        byte[] decrypted = cipher.doFinal(decoded);

        return Json.fromJson(new String(decrypted, StandardCharsets.UTF_8),
                TransferBatchEntity.class);
    }
}