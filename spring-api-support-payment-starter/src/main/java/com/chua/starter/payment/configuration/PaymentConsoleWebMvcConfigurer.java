package com.chua.starter.payment.configuration;

import com.chua.starter.payment.properties.PaymentConsoleProperties;
import com.chua.starter.payment.support.PaymentConsoleAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 支付控制台 MVC 配置。
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "plugin.payment.ops.console", name = "enabled", havingValue = "true", matchIfMissing = true)
public class PaymentConsoleWebMvcConfigurer implements WebMvcConfigurer {

    private final PaymentConsoleProperties properties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new PaymentConsoleAuthInterceptor(properties))
                .addPathPatterns("/payment-console", "/payment-console/", "/payment-console/**")
                .excludePathPatterns(
                        "/payment-console/login",
                        "/payment-console/login/",
                        "/payment-console/login.html",
                        "/payment-console/assets/**",
                        "/payment-console/static/**",
                        "/payment-console/auth/**"
                );
    }
}
