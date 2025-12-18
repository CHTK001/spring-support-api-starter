package com.chua.starter.oauth.sso.support.properties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 第三方 OAuth2 登录配置
 * <p>
 * 支持多种第三方平台的 OAuth2 登录配置，包括：
 * <ul>
 *   <li>Gitee - 码云</li>
 *   <li>Github - GitHub</li>
 *   <li>Wechat - 微信开放平台</li>
 *   <li>WechatMp - 微信公众号</li>
 *   <li>QQ - QQ互联</li>
 *   <li>Alipay - 支付宝</li>
 *   <li>DingTalk - 钉钉</li>
 *   <li>Weibo - 新浪微博</li>
 *   <li>Baidu - 百度</li>
 *   <li>Google - Google</li>
 *   <li>Facebook - Facebook</li>
 *   <li>Microsoft - 微软</li>
 * </ul>
 * </p>
 *
 * @author CH
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ConfigurationProperties(prefix = ThirdPartyLoginProperties.PRE, ignoreUnknownFields = true)
public class ThirdPartyLoginProperties {

    public static final String PRE = "plugin.oauth.third.login";

    /**
     * Gitee 码云配置
     */
    private Gitee gitee = new Gitee();

    /**
     * Github 配置
     */
    private Github github = new Github();

    /**
     * 微信开放平台配置
     */
    private Wechat wechat = new Wechat();

    /**
     * 微信公众号配置
     */
    private WechatMp wechatMp = new WechatMp();

    /**
     * QQ互联配置
     */
    private Qq qq = new Qq();

    /**
     * 支付宝配置
     */
    private Alipay alipay = new Alipay();

    /**
     * 钉钉配置
     */
    private DingTalk dingtalk = new DingTalk();

    /**
     * 企业微信配置
     */
    private WechatEnterprise wechatEnterprise = new WechatEnterprise();

    /**
     * 新浪微博配置
     */
    private Weibo weibo = new Weibo();

    /**
     * 百度配置
     */
    private Baidu baidu = new Baidu();

    /**
     * Google 配置
     */
    private Google google = new Google();

    /**
     * Microsoft 配置
     */
    private Microsoft microsoft = new Microsoft();

    // ==================== 平台配置类 ====================

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Gitee extends Base {
        public Gitee() {
            this.setClientId("f0d999a2e91e9a4edcec329fe7e6b3f1fa8b7063f8a618c3c7a2e0d0a13d5943");
            this.setClientSecret("158ced91fe30cfa06798c638d2b477d54e57b623a7e869c42d34d3125e88f1b7");
            this.setRedirectUri("http://127.0.0.1:19180/oauth/gitee/login/callback");
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Github extends Base {
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Wechat extends Base {
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class WechatMp extends Base {
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Qq extends Base {
        /**
         * 是否需要获取 unionId
         */
        private boolean unionId = false;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Alipay extends Base {
        /**
         * 支付宝公钥
         */
        private String alipayPublicKey;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class DingTalk extends Base {
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class WechatEnterprise extends Base {
        /**
         * 企业微信应用 agentId
         */
        private String agentId;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Weibo extends Base {
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Baidu extends Base {
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Google extends Base {
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Microsoft extends Base {
    }

    // ==================== 基础配置类 ====================

    @Data
    public static class Base {

        /**
         * 是否启用
         */
        private boolean enabled = false;

        /**
         * 客户端ID (appKey/appId)
         */
        private String clientId;

        /**
         * 客户端密钥 (appSecret)
         */
        private String clientSecret;

        /**
         * 登录成功后的回调地址
         */
        private String redirectUri;

        /**
         * 自定义授权 scope
         */
        private String scope;
    }
}
