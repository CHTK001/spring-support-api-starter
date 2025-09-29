//package com.chua.starter.service.aspect;
//
//import com.chua.common.support.utils.IpUtils;
//import com.chua.common.support.utils.StringUtils;
//import com.chua.starter.common.support.logger.RequestUtils;
//import com.chua.starter.service.SysLimitConfigurationService;
//import com.chua.starter.service.SysLimitRecordService;
//import com.chua.starter.system.entity.SysLimitConfiguration;
//import com.chua.starter.system.entity.SysLimitInfo;
//import com.chua.starter.system.entity.SysLimitRecord;
//import io.github.resilience4j.ratelimiter.RateLimiter;
//import io.github.resilience4j.ratelimiter.RateLimiterConfig;
//import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
//import io.github.resilience4j.ratelimiter.RequestNotPermitted;
//import lombok.extern.slf4j.Slf4j;
//import org.aopalliance.aop.Advice;
//import org.aopalliance.intercept.MethodInterceptor;
//import org.aopalliance.intercept.MethodInvocation;
//import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
//import org.springframework.beans.factory.InitializingBean;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.ApplicationContext;
//import org.springframework.lang.Nullable;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//import org.springframework.web.servlet.HandlerMapping;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.Serializable;
//import java.lang.reflect.Method;
//import java.time.Duration;
//import java.util.Date;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * 基于数据库配置的限流切面
// *
// * 支持从SysLimitConfiguration数据表中读取限流配置，实现动态限流功能。
// * 与注解方式的限流切面不同，该切面通过匹配请求路径来应用限流规则。
// *
// * @author CH
// * @since 2025/9/28
// */
//@Slf4j
//public class SysLimitConfigurationAspect extends StaticMethodMatcherPointcutAdvisor implements InitializingBean, Serializable {
//
//    private static final long serialVersionUID = 1L;
//
//    /**
//     * 默认错误消息
//     */
//    private static final String DEFAULT_ERROR_MESSAGE = "请求过于频繁，请稍后再试";
//
//    @Autowired
//    private ApplicationContext applicationContext;
//
//    @Autowired
//    private SysLimitConfigurationService sysLimitConfigurationService;
//
//    @Autowired
//    private SysLimitRecordService sysLimitRecordService;
//
//    private final RateLimiterRegistry rateLimiterRegistry;
//
//    /**
//     * 缓存限流器实例，key为接口路径
//     */
//    private final Map<String, RateLimiter> RATE_LIMITER_CACHE = new ConcurrentHashMap<>();
//
//    /**
//     * 缓存限流配置，key为接口路径
//     */
//    private final Map<String, SysLimitConfiguration> LIMIT_CONFIG_CACHE = new ConcurrentHashMap<>();
//
//    public SysLimitConfigurationAspect(RateLimiterRegistry rateLimiterRegistry) {
//        this.rateLimiterRegistry = rateLimiterRegistry;
//    }
//
//    @Override
//    public boolean matches(Method method, Class<?> targetClass) {
//        // 匹配所有Controller中的方法
//        return targetClass.getName().endsWith("Controller");
//    }
//
//    @Override
//    public void afterPropertiesSet() throws Exception {
//        this.setAdvice(new MethodInterceptor() {
//            @Nullable
//            @Override
//            public Object invoke(@Nullable MethodInvocation invocation) throws Throwable {
//                if (invocation == null) {
//                    throw new IllegalArgumentException("MethodInvocation cannot be null");
//                }
//                return invokeWithRateLimit(invocation);
//            }
//        });
//    }
//
//    /**
//     * 带限流的执行方法
//     *
//     * @param invocation 方法调用
//     * @return 执行结果
//     * @throws Throwable 异常
//     */
//    private Object invokeWithRateLimit(MethodInvocation invocation) throws Throwable {
//        if (invocation == null) {
//            throw new IllegalArgumentException("MethodInvocation cannot be null");
//        }
//
//        // 获取当前请求路径
//        String requestPath = getCurrentRequestPath();
//        if (StringUtils.isBlank(requestPath)) {
//            log.debug("无法获取请求路径，直接执行方法");
//            // 无法获取请求路径，直接执行方法
//            return invocation.proceed();
//        }
//
//        // 获取该路径对应的限流配置
//        SysLimitConfiguration limitConfig = getLimitConfiguration(requestPath);
//        if (limitConfig == null || limitConfig.getSysLimitStatus() == null ||
//            limitConfig.getSysLimitStatus() != 1) {
//            log.debug("没有启用的限流配置，直接执行方法，请求路径: {}", requestPath);
//            // 没有启用的限流配置，直接执行方法
//            return invocation.proceed();
//        }
//
//        // 获取或创建限流器
//        RateLimiter rateLimiter = getOrCreateRateLimiter(limitConfig);
//
//        try {
//            log.debug("执行限流检查，限流器名称: {}", limitConfig.getSysLimitPath());
//            // 执行限流检查
//            return rateLimiter.executeSupplier(() -> {
//                try {
//                    return invocation.proceed();
//                } catch (Throwable throwable) {
//                    if (throwable instanceof RuntimeException) {
//                        throw (RuntimeException) throwable;
//                    }
//                    throw new RuntimeException(throwable);
//                }
//            });
//        } catch (RequestNotPermitted e) {
//            log.warn("限流触发，请求路径: {}, 限流器名称: {}", requestPath, limitConfig.getSysLimitPath());
//            // 限流触发，记录限流事件
//            createLimitEvent(limitConfig, requestPath);
//
//            // 执行降级方法或抛出异常
//            String fallbackMethod = limitConfig.getSysLimitFallbackMethod();
//            if (StringUtils.isNotBlank(fallbackMethod)) {
//                log.debug("执行降级方法: {}", fallbackMethod);
//                return executeFallback(invocation, fallbackMethod, e);
//            }
//
//            // 抛出限流异常
//            String message = limitConfig.getSysLimitMessage();
//            if (StringUtils.isBlank(message)) {
//                message = DEFAULT_ERROR_MESSAGE;
//            }
//            log.debug("抛出限流异常，消息: {}", message);
//            throw new RuntimeException(message);
//        }
//    }
//
//    /**
//     * 获取当前请求路径
//     *
//     * @return 请求路径
//     */
//    private String getCurrentRequestPath() {
//        HttpServletRequest request = getRequest();
//        if (request == null) {
//            return null;
//        }
//
//        // 获取请求URI
//        String requestURI = request.getRequestURI();
//
//        // 获取上下文路径
//        String contextPath = request.getContextPath();
//        if (StringUtils.isNotBlank(contextPath) && !"/".equals(contextPath)) {
//            // 移除上下文路径前缀
//            if (requestURI.startsWith(contextPath)) {
//                requestURI = requestURI.substring(contextPath.length());
//            }
//        }
//
//        return requestURI;
//    }
//
//    /**
//     * 获取限流配置
//     *
//     * @param requestPath 请求路径
//     * @return 限流配置
//     */
//    private SysLimitConfiguration getLimitConfiguration(String requestPath) {
//        if (StringUtils.isBlank(requestPath)) {
//            return null;
//        }
//
//        // 先从缓存中获取
//        SysLimitConfiguration cachedConfig = LIMIT_CONFIG_CACHE.get(requestPath);
//        if (cachedConfig != null) {
//            log.debug("从缓存中获取限流配置，请求路径: {}", requestPath);
//            return cachedConfig;
//        }
//
//        // 从数据库查询
//        List<SysLimitConfiguration> configs = sysLimitConfigurationService.getLimitConfigurationsByPath(requestPath);
//        if (configs != null && !configs.isEmpty()) {
//            // 使用第一个匹配的配置
//            SysLimitConfiguration config = configs.get(0);
//            LIMIT_CONFIG_CACHE.put(requestPath, config);
//            log.debug("从数据库获取限流配置并缓存，请求路径: {}", requestPath);
//            return config;
//        }
//
//        log.debug("未找到限流配置，请求路径: {}", requestPath);
//        return null;
//    }
//
//    /**
//     * 获取或创建限流器
//     *
//     * @param limitConfig 限流配置
//     * @return 限流器
//     */
//    private RateLimiter getOrCreateRateLimiter(SysLimitConfiguration limitConfig) {
//        if (limitConfig == null) {
//            throw new IllegalArgumentException("SysLimitConfiguration cannot be null");
//        }
//
//        String path = limitConfig.getSysLimitPath();
//        if (StringUtils.isBlank(path)) {
//            throw new IllegalArgumentException("SysLimitPath cannot be null or empty");
//        }
//
//        RateLimiter rateLimiter = RATE_LIMITER_CACHE.get(path);
//        if (rateLimiter != null) {
//            // 检查配置是否发生变化
//            if (isConfigChanged(rateLimiter, limitConfig)) {
//                // 配置已变化，移除旧的限流器并创建新的
//                log.debug("限流配置已变化，重新创建限流器，路径: {}", path);
//                RATE_LIMITER_CACHE.remove(path);
//                rateLimiter = null;
//            } else {
//                log.debug("使用已存在的限流器，路径: {}", path);
//                return rateLimiter;
//            }
//        }
//
//        if (rateLimiter == null) {
//            // 创建新的限流器
//            RateLimiterConfig config = buildRateLimiterConfig(limitConfig);
//            rateLimiter = rateLimiterRegistry.rateLimiter(path, config);
//            RATE_LIMITER_CACHE.put(path, rateLimiter);
//            log.debug("创建新的限流器，路径: {}", path);
//        }
//
//        return rateLimiter;
//    }
//
//    /**
//     * 构建限流器配置
//     *
//     * @param limitConfig 限流配置
//     * @return 限流器配置
//     */
//    private RateLimiterConfig buildRateLimiterConfig(SysLimitConfiguration limitConfig) {
//        if (limitConfig == null) {
//            throw new IllegalArgumentException("SysLimitConfiguration cannot be null");
//        }
//
//        RateLimiterConfig.Builder builder = RateLimiterConfig.custom();
//
//        // 设置每个周期的许可数量
//        if (limitConfig.getSysLimitForPeriod() != null) {
//            builder.limitForPeriod(limitConfig.getSysLimitForPeriod());
//        }
//
//        // 设置刷新周期
//        if (limitConfig.getSysLimitRefreshPeriodSeconds() != null) {
//            builder.limitRefreshPeriod(Duration.ofSeconds(limitConfig.getSysLimitRefreshPeriodSeconds()));
//        }
//
//        // 设置超时时间
//        if (limitConfig.getSysLimitTimeoutDurationMillis() != null) {
//            builder.timeoutDuration(Duration.ofMillis(limitConfig.getSysLimitTimeoutDurationMillis()));
//        }
//
//        RateLimiterConfig config = builder.build();
//        log.debug("构建限流器配置，周期许可数: {}, 刷新周期: {}秒, 超时时间: {}毫秒",
//                config.getLimitForPeriod(),
//                config.getLimitRefreshPeriod().getSeconds(),
//                config.getTimeoutDuration().toMillis());
//        return config;
//    }
//
//    /**
//     * 检查配置是否发生变化
//     *
//     * @param rateLimiter 限流器
//     * @param limitConfig 限流配置
//     * @return 是否发生变化
//     */
//    private boolean isConfigChanged(RateLimiter rateLimiter, SysLimitConfiguration limitConfig) {
//        if (rateLimiter == null || limitConfig == null) {
//            return true;
//        }
//
//        RateLimiterConfig currentConfig = rateLimiter.getRateLimiterConfig();
//
//        // 检查每个周期的许可数量
//        if (limitConfig.getSysLimitForPeriod() != null &&
//            !limitConfig.getSysLimitForPeriod().equals(currentConfig.getLimitForPeriod())) {
//            log.debug("限流配置变化 - 周期许可数: {} -> {}",
//                    currentConfig.getLimitForPeriod(), limitConfig.getSysLimitForPeriod());
//            return true;
//        }
//
//        // 检查刷新周期
//        if (limitConfig.getSysLimitRefreshPeriodSeconds() != null) {
//            Duration expectedRefreshPeriod = Duration.ofSeconds(limitConfig.getSysLimitRefreshPeriodSeconds());
//            if (!expectedRefreshPeriod.equals(currentConfig.getLimitRefreshPeriod())) {
//                log.debug("限流配置变化 - 刷新周期: {} -> {}",
//                        currentConfig.getLimitRefreshPeriod(), expectedRefreshPeriod);
//                return true;
//            }
//        }
//
//        // 检查超时时间
//        if (limitConfig.getSysLimitTimeoutDurationMillis() != null) {
//            Duration expectedTimeout = Duration.ofMillis(limitConfig.getSysLimitTimeoutDurationMillis());
//            if (!expectedTimeout.equals(currentConfig.getTimeoutDuration())) {
//                log.debug("限流配置变化 - 超时时间: {} -> {}",
//                        currentConfig.getTimeoutDuration(), expectedTimeout);
//                return true;
//            }
//        }
//
//        return false;
//    }
//
//    /**
//     * 创建限流事件
//     *
//     * @param limitConfig 限流配置
//     * @param requestPath 请求路径
//     */
//    private void createLimitEvent(SysLimitConfiguration limitConfig, String requestPath) {
//        if (limitConfig == null || StringUtils.isBlank(requestPath)) {
//            log.warn("无法创建限流事件，limitConfig或requestPath为空");
//            return;
//        }
//
//        try {
//            SysLimitInfo sysLimitInfo = new SysLimitInfo(limitConfig);
//            sysLimitInfo.setSysLimitConfigurationId(limitConfig.getSysLimitConfigurationId());
//            sysLimitInfo.setSysLimitPath(requestPath);
//            sysLimitInfo.setSysLimitName(limitConfig.getSysLimitName());
//            sysLimitInfo.setSysLimitDimension(limitConfig.getSysLimitDimension());
//            sysLimitInfo.setSysLimitTime(new Date());
//            sysLimitInfo.setSysUserId(RequestUtils.getUserId());
//            sysLimitInfo.setSysUserName(RequestUtils.getUsername());
//            sysLimitInfo.setClientIp(RequestUtils.getIpAddress(getRequest()));
//            sysLimitInfo.setRequestUrl(RequestUtils.getUrl(getRequest()));
//            sysLimitInfo.setRequestMethod(getRequest().getMethod());
//            sysLimitInfo.setUserAgent(RequestUtils.getHeader(getRequest(), "User-Agent"));
//
//            // 发布限流事件
//            applicationContext.publishEvent(sysLimitInfo);
//
//            // 保存限流记录
//            saveLimitRecord(sysLimitInfo, limitConfig);
//        } catch (Exception e) {
//            log.error("创建限流事件失败", e);
//        }
//    }
//
//    /**
//     * 保存限流记录
//     *
//     * @param sysLimitInfo 限流信息
//     * @param limitConfig 限流配置
//     */
//    private void saveLimitRecord(SysLimitInfo sysLimitInfo, SysLimitConfiguration limitConfig) {
//        if (sysLimitInfo == null || limitConfig == null) {
//            log.warn("无法保存限流记录，sysLimitInfo或limitConfig为空");
//            return;
//        }
//
//        try {
//            SysLimitRecord sysLimitRecord = new SysLimitRecord();
//            sysLimitRecord.setSysLimitConfigurationId(limitConfig.getSysLimitConfigurationId());
//            sysLimitRecord.setSysLimitPath(sysLimitInfo.getSysLimitPath());
//            sysLimitRecord.setSysLimitName(sysLimitInfo.getSysLimitName());
//            sysLimitRecord.setSysLimitDimension(sysLimitInfo.getSysLimitDimension());
//            sysLimitRecord.setSysLimitKey(sysLimitInfo.getSysLimitKey());
//            sysLimitRecord.setSysLimitTime(sysLimitInfo.getSysLimitTime());
//            sysLimitRecord.setSysUserId(sysLimitInfo.getSysUserId());
//            sysLimitRecord.setSysUserName(sysLimitInfo.getSysUserName());
//            sysLimitRecord.setClientIp(sysLimitInfo.getClientIp());
//            sysLimitRecord.setRequestUrl(sysLimitInfo.getRequestUrl());
//            sysLimitRecord.setRequestMethod(sysLimitInfo.getRequestMethod());
//            sysLimitRecord.setUserAgent(sysLimitInfo.getUserAgent());
//
//            sysLimitRecordService.saveLimitRecord(sysLimitRecord);
//            log.debug("保存限流记录成功，配置ID: {}", limitConfig.getSysLimitConfigurationId());
//        } catch (Exception e) {
//            log.error("保存限流记录失败，配置ID: {}", limitConfig.getSysLimitConfigurationId(), e);
//        }
//    }
//
//    /**
//     * 执行降级方法
//     *
//     * @param invocation 方法调用
//     * @param fallbackMethodName 降级方法名
//     * @param exception 异常
//     * @return 降级方法执行结果
//     */
//    private Object executeFallback(MethodInvocation invocation, String fallbackMethodName, Exception exception) {
//        if (invocation == null || StringUtils.isBlank(fallbackMethodName) || exception == null) {
//            log.warn("无法执行降级方法，参数不能为空");
//            // 如果降级方法执行失败，返回默认值
//            return getDefaultValue(invocation != null ? invocation.getMethod().getReturnType() : Object.class);
//        }
//
//        try {
//            Method method = invocation.getMethod();
//            Class<?> targetClass = method.getDeclaringClass();
//
//            // 查找降级方法（带异常参数）
//            Class<?>[] paramTypes = method.getParameterTypes();
//            Class<?>[] fallbackParamTypes = new Class[paramTypes.length + 1];
//            System.arraycopy(paramTypes, 0, fallbackParamTypes, 0, paramTypes.length);
//            fallbackParamTypes[paramTypes.length] = Exception.class;
//
//            Method fallbackMethod = targetClass.getDeclaredMethod(fallbackMethodName, fallbackParamTypes);
//            fallbackMethod.setAccessible(true);
//
//            Object[] args = invocation.getArguments();
//            Object[] fallbackArgs = new Object[args.length + 1];
//            System.arraycopy(args, 0, fallbackArgs, 0, args.length);
//            fallbackArgs[args.length] = exception;
//
//            log.debug("执行降级方法: {}", fallbackMethodName);
//            return fallbackMethod.invoke(invocation.getThis(), fallbackArgs);
//        } catch (Exception e) {
//            log.warn("执行降级方法失败: {}", fallbackMethodName, e);
//            // 如果降级方法执行失败，返回默认值
//            return getDefaultValue(invocation.getMethod().getReturnType());
//        }
//    }
//
//    /**
//     * 获取返回类型的默认值
//     *
//     * @param returnType 返回类型
//     * @return 默认值
//     */
//    private Object getDefaultValue(Class<?> returnType) {
//        if (returnType == null || returnType == void.class || returnType == Void.class) {
//            return null;
//        }
//        if (returnType.isPrimitive()) {
//            if (returnType == boolean.class) {
//                return false;
//            }
//            if (returnType == byte.class) {
//                return (byte) 0;
//            }
//            if (returnType == short.class) {
//                return (short) 0;
//            }
//            if (returnType == int.class) {
//                return 0;
//            }
//            if (returnType == long.class) {
//                return 0L;
//            }
//            if (returnType == float.class) {
//                return 0.0f;
//            }
//            if (returnType == double.class) {
//                return 0.0d;
//            }
//            if (returnType == char.class) {
//                return '\0';
//            }
//        }
//        return null;
//    }
//
//    /**
//     * 获取当前请求
//     *
//     * @return HttpServletRequest
//     */
//    private HttpServletRequest getRequest() {
//        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//        return attributes != null ? attributes.getRequest() : null;
//    }
//
//    /**
//     * 获取当前响应
//     *
//     * @return HttpServletResponse
//     */
//    private HttpServletResponse getResponse() {
//        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//        return attributes != null ? attributes.getResponse() : null;
//    }
//
//    /**
//     * 清除缓存
//     *
//     * 当限流配置发生变化时调用此方法清除缓存
//     */
//    public void clearCache() {
//        RATE_LIMITER_CACHE.clear();
//        LIMIT_CONFIG_CACHE.clear();
//        log.info("清除限流缓存");
//    }
//
//    /**
//     * 清除指定路径的缓存
//     *
//     * @param path 路径
//     */
//    public void clearCache(String path) {
//        if (StringUtils.isBlank(path)) {
//            return;
//        }
//        RATE_LIMITER_CACHE.remove(path);
//        LIMIT_CONFIG_CACHE.remove(path);
//        log.info("清除指定路径的限流缓存，路径: {}", path);
//    }
//}