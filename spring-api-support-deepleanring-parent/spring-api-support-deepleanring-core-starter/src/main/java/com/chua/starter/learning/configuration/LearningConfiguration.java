package com.chua.starter.learning.configuration;

import com.chua.starter.learning.properties.LearningProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 配置
 *
 * @author CH
 */
@Configuration
@EnableConfigurationProperties(LearningProperties.class)
public class LearningConfiguration {

}
