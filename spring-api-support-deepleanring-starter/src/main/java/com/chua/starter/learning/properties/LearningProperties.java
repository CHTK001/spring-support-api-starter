package com.chua.starter.learning.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ocr配置
 *
 * @author CH
 */
@Data
@ConfigurationProperties(prefix = "plugin.learning")
public class LearningProperties {
    /**
     * 模型地址
     */
    private String modulePath = "../environment";
}
