package com.chua.starter.learning.service;

import com.chua.common.support.lang.proxy.DelegateMethodIntercept;
import com.chua.common.support.lang.proxy.ProxyMethod;
import com.chua.common.support.lang.proxy.ProxyUtils;
import com.chua.common.support.spi.ServiceProvider;
import com.chua.starter.learning.model.ConfigProperties;
import com.chua.starter.learning.properties.LearningProperties;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

/**
 * 基础服务
 *
 * @author CH
 */
public abstract class AbstractProxyEngineService<T> implements FactoryBean<T>, InitializingBean {

    private final ConfigProperties configProperties;

    private final LearningProperties learningProperties;

    private T service;

    public AbstractProxyEngineService(ConfigProperties configProperties, LearningProperties learningProperties) {
        this.configProperties = configProperties;
        this.learningProperties = learningProperties;
    }


    @Override
    public T getObject() throws Exception {
        return service;
    }


    @Override
    @SuppressWarnings("ALL")
    public void afterPropertiesSet() throws Exception {
        EngineService engineService = ServiceProvider.of(EngineService.class)
                .getNewExtension(configProperties.getType(), configProperties, learningProperties);
        this.service = (T) ProxyUtils.proxy(getObjectType(), ClassLoader.getSystemClassLoader(),
                new DelegateMethodIntercept<>(getObjectType(), new Function<ProxyMethod, Object>() {
                    @Override
                    public Object apply(ProxyMethod proxyMethod) {
                        Object[] args = proxyMethod.getArgs();
                        try {
                            return proxyMethod.invoke(engineService, proxyMethod.getArgs());
                        } catch (InvocationTargetException e) {
                            throw new RuntimeException(e);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }));
    }
}
