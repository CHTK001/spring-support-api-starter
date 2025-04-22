package com.chua.starter.pay.support.handler.wechat.handler;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author CH
 * @since 2025/4/16 10:08
 */
public class AesUtil {
    private static final int GCM_TAG_LENGTH = 128;
    private static final String AES_GCM_NO_PADDING = "AES/GCM/NoPadding";

    private final byte[] aesKey;

    public AesUtil(byte[] aesKey) {
        this.aesKey = aesKey;
    }

    /**
     * AES-GCM 解密
     */
    public String decryptToString(byte[] associatedData, byte[] nonce, String ciphertext)
            throws Exception {

        Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
        SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, nonce);

        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
        if (associatedData != null) {
            cipher.updateAAD(associatedData);
        }

        byte[] decoded = Base64.getDecoder().decode(ciphertext);
        byte[] decrypted = cipher.doFinal(decoded);

        return new String(decrypted, StandardCharsets.UTF_8);
    }
}