package com.chua.starter.service.configuration.trace;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 请求追踪策略注解。
 *
 * <p>仅标注了该注解的接口或控制器，才会进入请求追踪与行为分析链路。</p>
 *
 * @author CH
 * @since 2026/03/25
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestTraceStrategy {

    /**
     * 策略编码。
     *
     * @return 编码
     */
    String value() default "";
}
