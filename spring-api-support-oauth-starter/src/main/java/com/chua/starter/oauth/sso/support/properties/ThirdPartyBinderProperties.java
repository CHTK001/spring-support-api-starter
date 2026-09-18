package com.chua.starter.oauth.sso.support.properties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 三方绑定
 *
 * @since 4.0.0.42
 * @author CH
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ConfigurationProperties(prefix = ThirdPartyBinderProperties.PRE, ignoreUnknownFields = false)
public class ThirdPartyBinderProperties {

    /**
     * 配置前缀
     */
    public static final String PRE = "plugin.oauth.third.binder";

    /**
     * Gitee 绑定配置
     */
    private Gitee gitee = new Gitee();

    /**
     * Gitee 绑定配置。
     *
     * @since 4.0.0.42
     * @author CH
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Gitee extends Base {

        /**
         * 默认构造器, 内置演示用 客户端id/客户端secret/回调地址。
         */
        public Gitee() {
            this.setClientId("5117d2bf0e3f32c866afb7dd752cdcbc7947dafdb31bf7d9dc47bc4839609c50");
            this.setClientSecret("d467db8847cad16b1924c1210cb073b52a44b3370ea2d227b2a52957c2b6a42e");
            this.setRedirectUri("http://127.0.0.1:19180/oauth/gitee/bind/callback");
        }
    }

    /**
     * 绑定基础配置。
     *
     * @since 4.0.0.42
     * @author CH
     */
    @Data
    public static class Base {

        /**
         * 客户端标识：对应各平台的app键
         */
        private String clientId;

        /**
         * 客户端Secret：对应各平台的appsecret
         */
        private String clientSecret;

        /**
         * 登录成功后的回调地址
         */
        private String redirectUri;
    }
}
