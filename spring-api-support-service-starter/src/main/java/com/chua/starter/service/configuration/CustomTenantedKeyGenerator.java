package com.chua.starter.service.configuration;

import com.chua.starter.common.support.annotations.ApiCacheKey;
import com.chua.starter.common.support.properties.ControlProperties;
import com.chua.starter.common.support.utils.RequestUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.interceptor.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;

/**
 * 自定义key
 * @author CH
 * @since 2024/9/5
 */
@Configuration("customTenantedKeyGenerator")
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("ALL")
public class CustomTenantedKeyGenerator implements KeyGenerator {

    private static final String PREFIX = "cache:";

    final ControlProperties controlProperties;

    @Override
    public Object generate(Object target, Method method, Object... params) {
        ApiCacheKey apiCacheKey = method.getDeclaredAnnotation(ApiCacheKey.class);
        if(null == apiCacheKey) {
            log.warn("@ApiCacheKey is null");
            return SimpleKeyGenerator.generateKey(params);
        }

        EvaluationContext evaluationContext = createEvaluationContext(apiCacheKey, target, method, params);
        if(null == evaluationContext) {
            return SimpleKeyGenerator.generateKey(params);
        }
        StringBuilder key = new StringBuilder(PREFIX);
        key.append(controlProperties.getPlatform().getName()).append(":");
        key.append(RequestUtils.getTenantId()).append(":");
        Expression exp = (new SpelExpressionParser()).parseExpression(getKey(evaluationContext));
        String expValue = exp.getValue(evaluationContext, String.class);
        key.append(expValue);
        logprint(key, evaluationContext);
        return key.toString();
    }

    private void logprint(StringBuilder key, EvaluationContext evaluationContext) {
        Object value = evaluationContext.getRootObject().getValue();
        if(value instanceof CacheableOperation cacheableOperation) {
            log.info("缓存key: {}", key);
            return;
        }

        if(value instanceof CacheEvictOperation cacheEvictOperation) {
            log.info("删除key: {}", key);
            return;
        }

        if(value instanceof CachePutOperation cachePutOperation) {
            log.info("更新key: {}", key);
            return;
        }

    }

    private String getKey(EvaluationContext evaluationContext) {
        Object value = evaluationContext.getRootObject().getValue();
        if(value instanceof CacheableOperation cacheableOperation) {
            return cacheableOperation.getKey();
        }

        if(value instanceof CacheEvictOperation cacheEvictOperation) {
            return cacheEvictOperation.getKey();
        }

        if(value instanceof CachePutOperation cachePutOperation) {
            return cachePutOperation.getKey();
        }

        return "";
    }

    private EvaluationContext createEvaluationContext(ApiCacheKey apiCacheKey, Object target, Method method, Object[] params) {
        Cacheable cacheable = method.getDeclaredAnnotation(Cacheable.class);
        if(null != cacheable) {
            return createCacheableEvaluationContext(apiCacheKey, cacheable, method, params);
        }
        CacheEvict cacheEvict = method.getDeclaredAnnotation(CacheEvict.class);
        if(null != cacheEvict) {
            return createCacheEvictEvaluationContext(apiCacheKey, cacheEvict, method, params);
        }

        CachePut cachePut = method.getDeclaredAnnotation(CachePut.class);
        if(null != cachePut) {
            return createCachePutEvaluationContext(apiCacheKey, cachePut, method, params);
        }
        return null;
    }


    private EvaluationContext createCachePutEvaluationContext(ApiCacheKey apiCacheKey, CachePut cachePut, Method method, Object[] params) {
        ParameterNameDiscoverer paramNameDiscoverer = new DefaultParameterNameDiscoverer();
        CachePutOperation.Builder builder = new CachePutOperation.Builder();
        builder.setKey(apiCacheKey.value());
        builder.setCacheNames(cachePut.cacheNames());
        builder.setCacheManager(cachePut.cacheManager());
        builder.setCondition(cachePut.condition());
        builder.setUnless(cachePut.unless());
        builder.setCacheNames(cachePut.cacheNames());
        CachePutOperation cacheableOperation = new CachePutOperation(builder);
        return new MethodBasedEvaluationContext(cacheableOperation, method, params, paramNameDiscoverer);
    }

    private EvaluationContext createCacheEvictEvaluationContext(ApiCacheKey apiCacheKey, CacheEvict cacheEvict, Method method, Object[] params) {
        ParameterNameDiscoverer paramNameDiscoverer = new DefaultParameterNameDiscoverer();
        CacheEvictOperation.Builder builder = new CacheEvictOperation.Builder();
        builder.setKey(apiCacheKey.value());
        builder.setCacheNames(cacheEvict.cacheNames());
        builder.setCacheManager(cacheEvict.cacheManager());
        builder.setCondition(cacheEvict.condition());
        builder.setCacheNames(cacheEvict.cacheNames());
        CacheEvictOperation cacheableOperation = new CacheEvictOperation(builder);
        return new MethodBasedEvaluationContext(cacheableOperation, method, params, paramNameDiscoverer);
    }

    private EvaluationContext createCacheableEvaluationContext(ApiCacheKey apiCacheKey, Cacheable cacheable, Method method, Object[] params) {
        ParameterNameDiscoverer paramNameDiscoverer = new DefaultParameterNameDiscoverer();
        CacheableOperation.Builder builder = new CacheableOperation.Builder();
        builder.setKey(apiCacheKey.value());
        builder.setCacheNames(cacheable.cacheNames());
        builder.setCacheManager(cacheable.cacheManager());
        builder.setCondition(cacheable.condition());
        builder.setUnless(cacheable.unless());
        builder.setSync(cacheable.sync());
        builder.setCacheNames(cacheable.cacheNames());
        CacheableOperation cacheableOperation = new CacheableOperation(builder);
        return new MethodBasedEvaluationContext(cacheableOperation, method, params, paramNameDiscoverer);
    }
}
