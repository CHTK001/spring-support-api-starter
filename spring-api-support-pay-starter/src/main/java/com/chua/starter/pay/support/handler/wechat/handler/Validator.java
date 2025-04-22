package com.chua.starter.pay.support.handler.wechat.handler;

import java.nio.charset.StandardCharsets;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.Base64;

/**
 * @author CH
 * @since 2025/4/16 10:09
 */

public class Validator {
    /**
     * 验证微信支付签名
     */
    public static boolean validateSignature(
            String message,
            String signature,
            X509Certificate certificate
    ) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(certificate);
        sig.update(message.getBytes(StandardCharsets.UTF_8));
        return sig.verify(Base64.getDecoder().decode(signature));
    }
}