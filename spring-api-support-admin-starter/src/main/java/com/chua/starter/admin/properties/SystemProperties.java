package com.chua.starter.admin.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 系统配置
 * @author Administrator
 * @date 2024/01/17
 */
@Data
@ConfigurationProperties(prefix = "sys", ignoreInvalidFields = true)
public class SystemProperties {

    /**
     * 版本
     */
    private String version;
}
