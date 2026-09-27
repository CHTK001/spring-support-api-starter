package com.chua.starter.service.hotreload;

import com.chua.common.support.lang.json.Json;
import com.chua.starter.service.annotation.HotReloadConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 热重载配置 AOP 切面。
 *
 * <p>把被 {@link HotReloadConfig} 标注的方法转换为「一次缓存读取」：
 * 切面拦截方法调用，从 {@link HotReloadConfigService} 读取配置值并按返回类型转换，
 * 不再执行被标注方法的原始逻辑。</p>
 *
 * @since 4.0.0.43
 * @author CH
 */
@Slf4j
@Aspect
@Component
@Order(-50)
@RequiredArgsConstructor
public class HotReloadConfigAspect {

    private final HotReloadConfigService hotReloadConfigService;

    /**
     * 拦截热重载配置读取。
     *
     * @param point 连接点
     * @return 转换后的配置值
     * @throws Throwable 原始异常
     */
    @Around("@annotation(com.chua.starter.service.annotation.HotReloadConfig)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        HotReloadConfig annotation = signature.getMethod().getAnnotation(HotReloadConfig.class);
        String code = resolveCode(annotation, point.getArgs());
        if (null == code) {
            log.warn("[HotReload] 无法解析配置编码: method={}", point.getSignature().toShortString());
            return null;
        }
        String value = hotReloadConfigService.get(code);
        Class<?> returnType = signature.getReturnType();
        if (null == value) {
            log.debug("[HotReload] 配置不存在或未启用: code={}", code);
            return defaultValue(returnType);
        }
        return convertValue(value, returnType);
    }

    private String resolveCode(HotReloadConfig annotation, Object[] args) {
        String code = annotation.value();
        if (null != code && !code.isBlank()) {
            return code.trim();
        }
        if (null != args && args.length > 0 && args[0] instanceof String first && !first.isBlank()) {
            return first.trim();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    static Object defaultValue(Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            return null;
        }
        if (boolean.class == returnType) {
            return Boolean.FALSE;
        }
        if (char.class == returnType) {
            return (char) 0;
        }
        if (byte.class == returnType) {
            return (byte) 0;
        }
        if (short.class == returnType) {
            return (short) 0;
        }
        if (int.class == returnType) {
            return 0;
        }
        if (long.class == returnType) {
            return 0L;
        }
        if (float.class == returnType) {
            return 0F;
        }
        return 0D;
    }

    @SuppressWarnings("unchecked")
    static Object convertValue(String value, Class<?> returnType) {
        if (String.class == returnType) {
            return value;
        }
        if (boolean.class == returnType || Boolean.class == returnType) {
            return Boolean.parseBoolean(value);
        }
        if (int.class == returnType || Integer.class == returnType) {
            return Integer.valueOf(value.trim());
        }
        if (long.class == returnType || Long.class == returnType) {
            return Long.valueOf(value.trim());
        }
        if (double.class == returnType || Double.class == returnType) {
            return Double.valueOf(value.trim());
        }
        if (List.class == returnType) {
            return Json.fromJson(value, List.class);
        }
        if (Map.class == returnType) {
            return Json.fromJson(value, Map.class);
        }
        return Json.fromJson(value, returnType);
    }
}
