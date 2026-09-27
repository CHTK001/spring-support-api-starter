package com.chua.starter.system.support.debounce;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 防抖切面
 * <p>
 * 基于 Redis SETNX 实现接口防抖。在指定时间窗口内，相同 key 的请求只允许第一个通过。
 * </p>
 *
 * @author system
 * @since 2026/09/13
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DebounceAspect {

    /**
     * Redis key 前缀
    */
    private static final String DEBOUNCE_PREFIX = "debounce:";

    private final StringRedisTemplate stringRedisTemplate;

    private final SpelExpressionParser parser = new SpelExpressionParser();

    /**
     * around。
     *
     * @param joinPoint 方法入参 joinPoint
     * @param debounce 方法入参 debounce
     * @return 对象 对象
     * @throws Throwable 当执行过程不满足前置条件时
     */
    @Around("@annotation(debounce)")
    public Object around(ProceedingJoinPoint joinPoint, Debounce debounce) throws Throwable {
        String key = resolveKey(joinPoint, debounce);
        String redisKey = DEBOUNCE_PREFIX + key;

        // SETNX：只有 key 不存在时才设置成功
        Boolean acquired = stringRedisTemplate.opsForValue()
                .setIfAbsent(redisKey, "1", debounce.timeout(), TimeUnit.SECONDS);

        if (Boolean.FALSE.equals(acquired)) {
            log.warn("接口防抖拦截: key={}, timeout={}s", key, debounce.timeout());
            // 返回防抖错误，保持与原 ReturnResult 结构一致
            return ReturnResult.error(debounce.message());
        }

        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            // 业务异常时释放锁，允许重试
            stringRedisTemplate.delete(redisKey);
            throw e;
        }
    }

    /**
     * 解析防抖 key：支持 SpEL 表达式引用方法参数
     * @param joinPoint 方法入参 joinPoint
     * @param debounce 方法入参 debounce
     * @return 结果字符串
     */
    private String resolveKey(ProceedingJoinPoint joinPoint, Debounce debounce) {
        String expression = debounce.key();
        if (StringUtils.isBlank(expression)) {
            // 无表达式时用方法签名+参数哈希
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            StringBuilder sb = new StringBuilder(signature.toShortString());
            for (Object arg : joinPoint.getArgs()) {
                if (arg != null) {
                    sb.append(":").append(arg.hashCode());
                }
            }
            return sb.toString();
        }

        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();

            EvaluationContext context = new StandardEvaluationContext();
            if (paramNames != null) {
                for (int i = 0; i < paramNames.length; i++) {
                    context.setVariable(paramNames[i], args[i]);
                }
            }

            Expression exp = parser.parseExpression(expression);
            Object value = exp.getValue(context);
            return value != null ? value.toString() : "default";
        } catch (Exception e) {
            log.warn("解析防抖 key 失败，使用默认key: expression={}, error={}", expression, e.getMessage());
            return "debounce:fallback:" + joinPoint.getSignature().toShortString();
        }
    }
}
