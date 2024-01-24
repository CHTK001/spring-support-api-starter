package com.chua.starter.learning.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "plugin.learning", ignoreInvalidFields = true)
public class LearningProperties {

    /**
     * 模型地址
     */
    private String modulePath = "../environment";
}
