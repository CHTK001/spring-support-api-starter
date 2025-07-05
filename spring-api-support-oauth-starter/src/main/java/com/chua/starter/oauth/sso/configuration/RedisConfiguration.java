package com.chua.starter.oauth.sso.configuration;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.chua.starter.datasource.support.DataSourceContextSupport;
import com.chua.starter.oauth.sso.properties.TenantDataSourceProperties;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.sql.DataSource;
import java.util.List;

/**
 * 租户数据库
 *
 * @author CH
 */
@Configuration
public class RedisConfiguration  {

    @Configuration
    public class RedisConfig {

        @Bean
        public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
            RedisTemplate<String, Object> template = new RedisTemplate<>();
            template.setConnectionFactory(factory);

            // 解决查询缓存转换异常的问题
            ObjectMapper om = new ObjectMapper();
            om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
            om.activateDefaultTyping(om.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL);
            om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            // 使用Jackson2JsonRedisSerializer来序列化value
            Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(om, Object.class);

            template.setKeySerializer(new StringRedisSerializer());
            template.setValueSerializer(serializer);
            template.setHashKeySerializer(new StringRedisSerializer());
            template.setHashValueSerializer(serializer);

            template.afterPropertiesSet();
            return template;
        }
    }
}
