package com.chua.starter.system.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
* 高权账号可见性注解
*
* @since 2025/5/1
* @author CH
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PrivilegedVisibility {

    /**
    * 是否启用
     */
    boolean enabled() default true;

    /**
    * 目标用户表别名
     */
    String userAlias() default "";

    /**
    * 目标用户名列表达式
     */
    String usernameColumn() default "";
}
