package com.chua.starter.system.fix;

import com.chua.common.support.reflection.ReflectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisCommands;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MockRedis配置类，提供相关能力。
 *
 * @author CH
 * @since 1.0.0
 */
@Profile("h2")
@Configuration
public class MockRedisConfiguration {

    private static final ConcurrentHashMap<String, byte[]> STORE = new ConcurrentHashMap<>();

    // 始终创建 mock 工厂（@Primary 保证优先），去掉 @ConditionalOnMissingBean 避免被框架真实工厂压制
    /**
     * mockRedis连接工厂。
     *
     * @return Redis连接工厂 对象
     */
    @Bean
    @Primary
    public RedisConnectionFactory mockRedisConnectionFactory() {
        return ReflectUtils.newProxy(
                getClass().getClassLoader(),
                new Class<?>[]{RedisConnectionFactory.class},
                new FactoryHandler()
        );
    }

    /**
     * mock字符串Redis模板。
     *
     * @param factory 工厂，不允许为 null
     * @return 字符串Redis模板 对象
     */
    @Bean
    @Primary
    public StringRedisTemplate mockStringRedisTemplate(RedisConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }

    /**
     * mockRedis模板。
     *
     * @param factory 工厂，不允许为 null
     * @return 结果值
     */
    @Bean
    public org.springframework.data.redis.core.RedisTemplate<String, Object> mockRedisTemplate(RedisConnectionFactory factory) {
        org.springframework.data.redis.core.RedisTemplate<String, Object> template =
                new org.springframework.data.redis.core.RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(org.springframework.data.redis.serializer.RedisSerializer.string());
        template.setValueSerializer(org.springframework.data.redis.serializer.RedisSerializer.java());
        template.setHashKeySerializer(org.springframework.data.redis.serializer.RedisSerializer.string());
        template.setHashValueSerializer(org.springframework.data.redis.serializer.RedisSerializer.java());
        template.afterPropertiesSet();
        return template;
    }

    static class FactoryHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            switch (method.getName()) {
                case "getConnection":
                    return createConnectionProxy();
                case "getClusterConnection":
                case "getSentinelConnection":
                    throw new UnsupportedOperationException("Mock: not supported");
                case "hashCode":
                    return System.identityHashCode(proxy);
                case "equals":
                    return proxy == args[0];
                case "toString":
                    return "MockRedisConnectionFactory";
                default:
                    return null;
            }
        }

        private RedisConnection createConnectionProxy() {
            return ReflectUtils.newProxy(
                    getClass().getClassLoader(),
                    new Class<?>[]{RedisConnection.class},
                    new ConnectionHandler()
            );
        }
    }

    static class ConnectionHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            switch (method.getName()) {
                case "isClosed":
                    return false;
                case "isConnected":
                    return true;
                case "close":
                    return null;
                case "ping":
                    return "PONG";
                case "getRedisStringCommands":
                    return ReflectUtils.newProxy(
                            getClass().getClassLoader(),
                            new Class<?>[]{RedisStringCommands.class},
                            (p, m, a) -> {
                                if (m.getName().equals("get")) {
                                    return null;
                                }
                                if (m.getName().equals("set")) {
                                    return false;
                                }
                                if (m.getName().equals("exists")) {
                                    return false;
                                }
                                if (m.getName().equals("delete")) {
                                    return 0L;
                                }
                                if (m.getName().equals("expire")) {
                                    return false;
                                }
                                return null;
                            }
                    );
                case "getRedisCommands":
                    return ReflectUtils.newProxy(
                            getClass().getClassLoader(),
                            new Class<?>[]{RedisCommands.class},
                            (p, m, a) -> null
                    );
                case "getClusterCommands":
                case "getSentinelCommands":
                    return null;
                case "hashCode":
                    return System.identityHashCode(proxy);
                case "equals":
                    return proxy == args[0];
                case "toString":
                    return "MockRedisConnection";
                default:
                    return null;
            }
        }
    }
}