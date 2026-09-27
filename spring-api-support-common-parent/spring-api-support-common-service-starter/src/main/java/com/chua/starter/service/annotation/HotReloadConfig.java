package com.chua.starter.service.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 热重载配置读取注解。
 *
 * <p>标注在方法上后，{@code HotReloadConfigAspect} 会把方法第一个参数当作配置编码，
 * 从 hot_reload 分组读取配置值并按方法返回类型完成转换。读取走
 * {@code HotReloadConfigService} 的 @Cacheable，配置保存后由事件桥接器清理缓存。</p>
 *
 * <pre>{@code
 * @HotReloadConfig
 * public int pageSize() {
 *     return 20;
 * }
 * }</pre>
 *
 * @since 4.0.0.43
 * @author CH
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HotReloadConfig {

    /**
     * 配置编码；为空时使用方法的第一个参数。
     *
     * @return 配置编码
     */
    String value() default "";
}
