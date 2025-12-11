package com.chua.starter.system.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 系统服务配置属性
 *
 * @author CH
 * @since 2024/12/11
 */
@Data
@ConfigurationProperties(prefix = SystemProperties.PRE)
public class SystemProperties {

    public static final String PRE = "plugin.system.server";

    /**
     * 是否启用系统服务
     */
    private boolean enable = true;
}
