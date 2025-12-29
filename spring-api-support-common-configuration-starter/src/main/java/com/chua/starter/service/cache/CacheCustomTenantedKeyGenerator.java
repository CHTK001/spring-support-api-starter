package com.chua.starter.service.cache;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 自定义租户缓存Key生成器
 * <p>
 * 用于生成带有租户前缀的缓存key，支持多租户环境
 *
 * @author CH
 * @since 2024/8/8
 */
@Component("customTenantedKeyGenerator")
public class CacheCustomTenantedKeyGenerator implements KeyGenerator {

    /**
     * 默认的key前缀
     */
    private static final String DEFAULT_PREFIX = "cache:";

    @Override
    public Object generate(Object target, Method method, Object... params) {
        StringBuilder key = getKeyPref();
        
        // 添加方法名
        key.append(method.getName());
        
        // 添加参数
        if (params != null && params.length > 0) {
            key.append(":");
            key.append(Arrays.stream(params)
                    .map(p -> p == null ? "null" : p.toString())
                    .collect(Collectors.joining(":")));
        }
        
        return key.toString();
    }

    /**
     * 获取key前缀
     * <p>
     * 子类可以覆盖此方法以提供租户特定的前缀
     *
     * @return key前缀
     */
    public StringBuilder getKeyPref() {
        StringBuilder sb = new StringBuilder();
        sb.append(DEFAULT_PREFIX);
        // 可以在这里添加租户ID等信息
        // 如: sb.append(TenantContext.getTenantId()).append(":");
        return sb;
    }
}
