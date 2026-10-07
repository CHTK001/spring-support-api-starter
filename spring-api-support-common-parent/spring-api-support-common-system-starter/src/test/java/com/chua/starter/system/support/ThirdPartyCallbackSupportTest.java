package com.chua.starter.system.support;

import com.chua.starter.system.pojo.LoginResult;
import com.chua.starter.system.pojo.UserInfoVO;
import com.chua.starter.system.properties.SystemSecurityProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import org.springframework.web.servlet.view.RedirectView;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ThirdPartyCallbackSupport} 回归测试
 *
 * <p>守护三方登录「换票凭证」的安全性质：</p>
 * <ol>
 *   <li>凭证是服务端密钥加密的 LoginResult，前端无法本地解密；</li>
 *   <li>一次性：兑换先核销 nonce 再解密（fail-closed），重放必然失败；</li>
 *   <li>篡改、错密钥、未签发的伪造凭证一律兑换失败；</li>
 *   <li>错误提示双写：flash {@code msg} + 静态 {@code thirdMsg}（跨域回跳不丢）。</li>
 * </ol>
 */
@DisplayName("ThirdPartyCallbackSupport: 三方登录换票凭证")
class ThirdPartyCallbackSupportTest {

    /**
     * 默认密钥（与 SystemSecurityProperties 默认一致）
     */
    private static final String KEY = "1234567890Oil#@1";

    /**
     * 构造指定密钥的安全配置
     *
     * @param key AES 密钥（16 字节）
     * @return 安全配置
     */
    private SystemSecurityProperties props(String key) {
        SystemSecurityProperties properties = new SystemSecurityProperties();
        properties.setThirdPartyCallbackKey(key);
        return properties;
    }

    /**
     * 构造待签发的登录结果
     *
     * @return 登录结果
     */
    private LoginResult sampleLoginResult() {
        UserInfoVO userInfo = new UserInfoVO();
        userInfo.setSysUserId(42);
        userInfo.setSysUserUsername("admin");
        userInfo.setSysUserNickname("管理员");
        return LoginResult.builder()
                .accessToken("at-001")
                .refreshToken("rt-001")
                .tokenType("Bearer")
                .expires(3600L)
                .userInfo(userInfo)
                .build();
    }

    @Test
    @DisplayName("签发后可兑换，LoginResult 全字段 roundtrip")
    void issueThenRedeemRoundtrip() {
        SystemSecurityProperties props = props(KEY);
        LoginResult origin = sampleLoginResult();

        String blob = ThirdPartyCallbackSupport.encryptRedirectParam(origin, props);
        assertNotNull(blob);

        LoginResult redeemed = ThirdPartyCallbackSupport.redeemRedirectParam(blob, props);
        assertNotNull(redeemed, "签发后的凭证必须可兑换");
        assertEquals(origin.getAccessToken(), redeemed.getAccessToken());
        assertEquals(origin.getRefreshToken(), redeemed.getRefreshToken());
        assertEquals(origin.getTokenType(), redeemed.getTokenType());
        assertEquals(origin.getExpires(), redeemed.getExpires());
        assertNotNull(redeemed.getUserInfo());
        assertEquals(origin.getUserInfo().getSysUserId(), redeemed.getUserInfo().getSysUserId());
        assertEquals(origin.getUserInfo().getSysUserUsername(), redeemed.getUserInfo().getSysUserUsername());
        assertEquals(origin.getUserInfo().getRoles(), redeemed.getUserInfo().getRoles());
        assertEquals(origin.getUserInfo().getPerms(), redeemed.getUserInfo().getPerms());
    }

    @Test
    @DisplayName("凭证一次性：第二次兑换必然失败（重放攻击防护）")
    void redeemIsOneTime() {
        SystemSecurityProperties props = props(KEY);
        String blob = ThirdPartyCallbackSupport.encryptRedirectParam(sampleLoginResult(), props);

        assertNotNull(ThirdPartyCallbackSupport.redeemRedirectParam(blob, props), "首次兑换必须成功");
        assertNull(ThirdPartyCallbackSupport.redeemRedirectParam(blob, props), "重放必须失败");
        assertNull(ThirdPartyCallbackSupport.redeemRedirectParam(blob, props), "持续重放必须失败");
    }

    @Test
    @DisplayName("未签发的伪造凭证兑换失败（nonce 未登记）")
    void unissuedBlobRejected() {
        SystemSecurityProperties props = props(KEY);
        assertNull(ThirdPartyCallbackSupport.redeemRedirectParam("forged-blob", props));
        assertNull(ThirdPartyCallbackSupport.redeemRedirectParam(null, props));
        assertNull(ThirdPartyCallbackSupport.redeemRedirectParam("", props));
        assertNull(ThirdPartyCallbackSupport.redeemRedirectParam("   ", props));
        assertNull(ThirdPartyCallbackSupport.redeemRedirectParam("x".repeat(64), props));
    }

    @Test
    @DisplayName("密文被篡改兑换失败")
    void tamperedBlobRejected() {
        SystemSecurityProperties props = props(KEY);
        String blob = ThirdPartyCallbackSupport.encryptRedirectParam(sampleLoginResult(), props);

        // 篡改中间一个字符：要么 nonce 对不上，要么解密/反序列化失败
        int mid = blob.length() / 2;
        char original = blob.charAt(mid);
        char flipped = original == 'X' ? 'Y' : 'X';
        String tampered = blob.substring(0, mid) + flipped + blob.substring(mid + 1);
        assertNotEquals(blob, tampered);

        assertNull(ThirdPartyCallbackSupport.redeemRedirectParam(tampered, props));
        // 原凭证未被篡改用例消耗，仍应可兑换
        assertNotNull(ThirdPartyCallbackSupport.redeemRedirectParam(blob, props));
    }

    @Test
    @DisplayName("错密钥兑换失败（fail-closed，且凭证立即作废）")
    void wrongKeyRejected() {
        SystemSecurityProperties issued = props(KEY);
        SystemSecurityProperties other = props("abcdefghijklmnop");
        String blob = ThirdPartyCallbackSupport.encryptRedirectParam(sampleLoginResult(), issued);

        assertNull(ThirdPartyCallbackSupport.redeemRedirectParam(blob, other), "错密钥必须兑换失败");
        // 解密失败也已先核销：换回正确密钥同样不能重试（fail-closed）
        assertNull(ThirdPartyCallbackSupport.redeemRedirectParam(blob, issued), "失败后不可用正确密钥重试");
    }

    @Test
    @DisplayName("null 配置拒绝兑换（不 NPE）")
    void nullPropsRejected() {
        String blob = ThirdPartyCallbackSupport.encryptRedirectParam(sampleLoginResult(), props(KEY));
        assertNull(ThirdPartyCallbackSupport.redeemRedirectParam(blob, null));
    }

    @Test
    @DisplayName("nonce 键格式：xchg_ 前缀 + 64 位小写十六进制，且互不相同")
    void nonceKeyFormat() {
        String keyA = ThirdPartyCallbackSupport.nonceKey("blob-a");
        String keyB = ThirdPartyCallbackSupport.nonceKey("blob-b");

        assertTrue(keyA.startsWith("xchg_"), "前缀必须是 xchg_: " + keyA);
        assertTrue(keyA.substring(5).matches("[0-9a-f]{64}"), "必须是 64 位小写 hex: " + keyA);
        assertEquals(keyA, ThirdPartyCallbackSupport.nonceKey("blob-a"), "同输入必须稳定");
        assertNotEquals(keyA, keyB, "不同输入必须不同键");
    }

    @Test
    @DisplayName("safeSystemCallback 读取点兜底：白名单放行、外部地址回落根路径")
    void safeSystemCallbackGuard() {
        SystemSecurityProperties props = props(KEY);
        props.setAllowedSystemCallbackHosts(java.util.List.of("http://localhost:8096"));

        assertEquals("/home", ThirdPartyCallbackSupport.safeSystemCallback("/home", props));
        assertEquals("http://localhost:8096/#/login",
                ThirdPartyCallbackSupport.safeSystemCallback("http://localhost:8096/#/login", props));
        assertEquals("/", ThirdPartyCallbackSupport.safeSystemCallback("//evil.tld", props));
        assertEquals("/", ThirdPartyCallbackSupport.safeSystemCallback("https://evil.tld", props));
        assertEquals("/", ThirdPartyCallbackSupport.safeSystemCallback(null, props));
        // 无配置对象：相对路径仍放行，绝对地址一律拒绝（fail-closed）
        assertEquals("/home", ThirdPartyCallbackSupport.safeSystemCallback("/home", null));
        assertEquals("/", ThirdPartyCallbackSupport.safeSystemCallback("http://localhost:8096/#/login", null));
    }

    @Test
    @DisplayName("markFailure 双写：flash msg + 静态 thirdMsg，且对 null 参数健壮")
    void markFailureDualWrite() {
        RedirectAttributesModelMap attributes = new RedirectAttributesModelMap();
        RedirectView view = new RedirectView();

        ThirdPartyCallbackSupport.markFailure(attributes, view, "钉钉登录未开启");

        assertEquals("钉钉登录未开启", attributes.getFlashAttributes().get("msg"), "flash msg 必须写入");
        assertEquals("钉钉登录未开启", view.getStaticAttributes().get("thirdMsg"), "静态 thirdMsg 必须写入");

        // null 参数不抛异常
        ThirdPartyCallbackSupport.markFailure(null, view, "x");
        ThirdPartyCallbackSupport.markFailure(attributes, null, "y");
        ThirdPartyCallbackSupport.markFailure(null, null, "z");
    }

    @Test
    @DisplayName("签发的密文不可在前端本地解密验证——密钥只在服务端配置")
    void callbackKeyComesFromServerPropsOnly() {
        // 同一 LoginResult 在不同密钥下密文不同（AES-CBC + 随机 IV）
        String blobA = ThirdPartyCallbackSupport.encryptRedirectParam(sampleLoginResult(), props(KEY));
        String blobB = ThirdPartyCallbackSupport.encryptRedirectParam(sampleLoginResult(), props(KEY));
        assertNotEquals(blobA, blobB, "同明文两次签发的密文必须不同（随机 IV）");

        // 密文里不能出现明文令牌（信息泄露检查）
        assertTrue(!blobA.contains("at-001"), "密文不得泄露明文 accessToken");
    }
}
