package com.chua.payment.support.configuration;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.chua.payment.support.spi.UserOpenIdResolver;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * 支付模块自动配置
 */
@AutoConfiguration
@Import(PaymentComponentScanner.class)
@MapperScan("com.chua.payment.support.mapper")
public class PaymentAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(UserOpenIdResolver.class)
    public UserOpenIdResolver defaultUserOpenIdResolver() {
        return (merchantId, userId, channelType) -> null;
    }

    @Bean
    @ConditionalOnMissingBean(name = "paymentMybatisPlusInterceptor")
    public MybatisPlusInterceptor paymentMybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
