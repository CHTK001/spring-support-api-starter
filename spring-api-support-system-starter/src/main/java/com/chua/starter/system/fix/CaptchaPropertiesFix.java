package com.chua.starter.system.fix;

import com.chua.starter.common.support.properties.CaptchaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Captcha属性Fix类，提供相关能力。
 *
 * @author CH
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(CaptchaProperties.class)
public class CaptchaPropertiesFix {
}