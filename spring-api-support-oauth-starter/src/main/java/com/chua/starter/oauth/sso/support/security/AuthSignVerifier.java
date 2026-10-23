package com.chua.starter.oauth.sso.support.security;

import com.chua.starter.common.support.utils.SignUtils;
import com.chua.starter.oauth.sso.support.properties.AuthServerProperties;

import java.util.Map;

/**
 * 请求签名校验。
 *
 * <p>MVC 的 {@code BaseAuthInformation} 与协议服务器的 {@code OauthProtocolDecoder}
 * 此前各写了一遍「sign-verify 开关 + MD5 校验」，两处语义容易分叉（历史上
 * {@code AbstractAuthInformation} 的布尔就写反过）。这里收敛成唯一实现。</p>
 *
 * <p><b>注意</b>：{@code SignUtils} 的 MD5 签名是<b>无密钥</b>的，只能证明报文未被
 * 意外损坏，<b>不能</b>证明调用方身份。真正的真实性边界是网络隔离与 AK/SK。</p>
 *
 * @since 4.0.0.42
 * @author CH
 */
public final class AuthSignVerifier {

    /**
     * 工具类禁止实例化
     */
    private AuthSignVerifier() {
    }

    /**
     * 校验请求签名。
     *
     * @param properties 认证服务端属性
     * @param bodyData   解码后的业务参数
     * @param sign       请求头 {@code x-oauth-sign}
     * @return 校验通过返回 true；开关关闭时直接放行
     */
    public static boolean verify(AuthServerProperties properties, Map<String, Object> bodyData, String sign) {
        if (properties == null || !properties.isSignVerify()) {
            return true;
        }
        return SignUtils.verifyMd5Sign(bodyData, sign);
    }
}
