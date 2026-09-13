package com.chua.starter.system.support.debounce;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口防抖注解
 * <p>
 * 替代 utils-support-task-starter 中的 @Debounce，基于 Redis 实现。
 * 在指定时间窗口内，相同 key 的请求只允许第一个通过，后续请求被拒绝。
 * </p>
 *
 * <pre>
 * // 用法：key 支持 SpEL 表达式，引用方法参数
 * &#064;Debounce(key = "#loginData.username #loginData.loginType", timeout = 3)
 * public ReturnResult&lt;LoginResult&gt; login(LoginData loginData) { ... }
 * </pre>
 *
 * @author system
 * @since 2026/09/13
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Debounce {

    /**
     * 防抖 key，支持 SpEL 表达式，可引用方法参数（如 #user.username）。
     * 为空时使用 方法签名 + 所有参数 的哈希作为 key。
     */
    String key() default "";

    /**
     * 防抖时间窗口（秒），默认 3 秒。
     * 在此时间内相同 key 的重复请求会被拒绝。
     */
    int timeout() default 3;

    /**
     * 被防抖拦截时返回的错误消息。
     */
    String message() default "操作过于频繁，请稍后再试";
}
