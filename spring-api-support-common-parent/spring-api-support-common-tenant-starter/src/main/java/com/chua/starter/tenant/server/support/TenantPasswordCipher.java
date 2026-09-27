package com.chua.starter.tenant.server.support;

import com.chua.common.support.lang.algorithm.cipher.AesCipher;
import com.chua.starter.system.properties.SystemSecurityProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 租户口令落库加解密。
 *
 * <p><b>为什么是可逆加密而不是摘要</b>：用户口令存 {@code md5(明文+盐)}，因为登录只需比对；
 * 租户口令还要下发给租户节点（{@code TenantServerSyncPublisher} 推送 →
 * {@code TenantClientSyncHandler} 落到租户本地），必须能还原成明文。所以本类只解决
 * 「落库不存明文」这一个风险点，<b>同步线格式保持明文不变，租户端零改动</b>。</p>
 *
 * <p><b>为什么带 ENC: 前缀</b>：存量库里是历史明文，没有前缀。加前缀后
 * {@link #decrypt(String)} 能无歧义区分两者 —— 有前缀走解密，没有就当明文原样返回，
 * 因此升级不需要数据迁移，租户改密一次即转为密文。</p>
 *
 * <p>AES/CBC/PKCS5Padding + 每次随机 IV（{@link AesCipher} 内部实现），相同明文两次加密
 * 结果不同，不会因密文相同而泄漏口令相等。</p>
 *
 * <p><b>密钥长度必须为 16/24/32 字节</b>（{@code SecretKeySpec} 的硬约束），否则
 * {@link #key()} 直接抛异常指明配置项名，而不是让加密在深处失败。</p>
 *
 * @since 4.0.0.43
 * @author CH
 */
@Component
@RequiredArgsConstructor
public class TenantPasswordCipher {

    /**
     * 密文标记前缀，无前缀视为历史明文
     */
    private static final String PREFIX = "ENC:";

    /**
     * 系统安全配置
     */
    private final SystemSecurityProperties securityProperties;

    /**
     * 加密后落库
     *
     * @param plainText 客户端提交的明文口令
     * @return 带 {@code ENC:} 前缀的 Base64 密文；入参为空时原样返回
     */
    public String encrypt(String plainText) {
        if (null == plainText || plainText.isEmpty()) {
            return plainText;
        }
        // 幂等：已经是密文就不要再加密一层
        if (isCipherText(plainText)) {
            return plainText;
        }
        try {
            return PREFIX + new AesCipher().encryptToString(key(), plainText);
        } catch (Exception e) {
            // 加密失败绝不能回落成明文落库，否则等于没改
            throw new IllegalStateException("租户口令加密失败，已中止写入（拒绝明文落库）", e);
        }
    }

    /**
     * 解密后使用
     *
     * @param stored 库中取出的值，可能是密文也可能是历史明文
     * @return 明文口令
     */
    public String decrypt(String stored) {
        if (null == stored || stored.isEmpty() || !isCipherText(stored)) {
            return stored;
        }
        try {
            return new AesCipher().decryptToString(key(), stored.substring(PREFIX.length()));
        } catch (Exception e) {
            throw new IllegalStateException("租户口令解密失败，请检查 plugin.system.security.tenant-password-cipher-key 是否被改动", e);
        }
    }

    /**
     * 是否为本类产生的密文
     *
     * @param value 待判断值
     * @return 带前缀返回 true
     */
    public boolean isCipherText(String value) {
        return null != value && value.startsWith(PREFIX);
    }

    private byte[] key() {
        String configured = securityProperties.getTenantPasswordCipherKey();
        if (null == configured || configured.isEmpty()) {
            throw new IllegalStateException("plugin.system.security.tenant-password-cipher-key 未配置");
        }
        byte[] key = configured.getBytes(StandardCharsets.UTF_8);
        // 必须在这里挡住：SecretKeySpec 只接受 16/24/32，放过去会等到第一次加密才抛
        // 「AES 加密失败」，而调用方看到的是租户接口 500，根因完全对不上
        if (16 != key.length && 24 != key.length && 32 != key.length) {
            throw new IllegalStateException("plugin.system.security.tenant-password-cipher-key 长度必须为 "
                    + "16/24/32 字节，当前为 " + key.length + " 字节");
        }
        return key;
    }
}
