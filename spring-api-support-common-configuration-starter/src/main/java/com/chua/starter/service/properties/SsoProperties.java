package com.chua.starter.service.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author CH
 * @since 2024/12/4
 */
@Data
@ConfigurationProperties(prefix = SsoProperties.PRE, ignoreInvalidFields = true)
public class SsoProperties {

    public static final String PRE = "plugin.sso";

    /**
     * 临时文件路径
     */
    private String tempPath = "./temp/sso";

    /**
     * 本地模式
     */
    private LocalSsoProperties local = new LocalSsoProperties();


    @Data
    public static class LocalSsoProperties {

        /**
         * 本地存储路径
         */
        private String endpoint = "./sso/local";
    }
}
