package com.chua.starter.service.aspect;

import com.chua.common.support.utils.StringUtils;
import com.chua.starter.service.SysLimitConfigurationService;
import com.chua.starter.service.SysLimitRecordService;
import com.chua.starter.system.entity.SysLimitConfiguration;
import com.chua.starter.system.entity.SysLimitInfo;
import com.chua.starter.system.entity.SysLimitRecord;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于数据库配置的限流切面
 * 
 * 支持从SysLimitConfiguration数据表中读取限流配置，实现动态限流功能。
 * 与注解方式的限流切面不同，该切面通过匹配请求路径来应用限流规则。
 * 
 * 主要功能：
 * 1. 动态加载限流配置
 * 2. 实时创建和更新限流器
 * 3. 记录限流事件
 * 4. 支持降级处理
 * 
 * @author CH
 * @since 2025/9/28
 */
@Slf4j
public class SysLimitConfigurationAspect extends StaticMethodMatcherPointcutAdvisor implements InitializingBean, Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 默认错误消息
     */
    private static final String DEFAULT_ERROR_MESSAGE = "请求过于频繁，请稍后再试";
    
    /**
     * 控制器类名后缀
     */
    private static final String CONTROLLER_SUFFIX = "Controller";

    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private SysLimitConfigurationService sysLimitConfigurationService;
    
    @Autowired
    private SysLimitRecordService sysLimitRecordService;
    
    private final RateLimiterRegistry rateLimiterRegistry;
    
    /**
     * 缓存限流器实例，key为接口路径
     */
    private final Map<String, RateLimiter> RATE_LIMITER_CACHE = new ConcurrentHashMap<>();
    
    /**
     * 缓存限流配置，key为接口路径
     */
    private final Map<String, SysLimitConfiguration> LIMIT_CONFIG_CACHE = new ConcurrentHashMap<>();

    public SysLimitConfigurationAspect(RateLimiterRegistry rateLimiterRegistry) {
        this.rateLimiterRegistry = rateLimiterRegistry;
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        // 匹配所有Controller中的方法
        return targetClass.getName().endsWith(CONTROLLER_SUFFIX);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.setAdvice(new MethodInterceptor() {
            @Nullable
            @Override
            public Object invoke(@Nullable MethodInvocation invocation) throws Throwable {
                if (invocation == null) {
                    throw new IllegalArgumentException("MethodInvocation cannot be null");
                }
                return invokeWithRateLimit(invocation);
            }
        });
    }

    /**
     * 带限流的执行方法
     *
     * @param invocation 方法调用
     * @return 执行结果
     * @throws Throwable 异常
     */
    private Object invokeWithRateLimit(MethodInvocation invocation) throws Throwable {
        if (invocation == null) {
            throw new IllegalArgumentException("MethodInvocation cannot be null");
        }
        
        // 获取当前请求路径
        String requestPath = getCurrentRequestPath();
        if (StringUtils.isBlank(requestPath)) {
            log.debug("无法获取请求路径，直接执行方法");
            // 无法获取请求路径，直接执行方法
            return invocation.proceed();
        }
        
        // 获取该路径对应的限流配置
        SysLimitConfiguration limitConfig = getLimitConfiguration(requestPath);
        if (limitConfig == null || limitConfig.getSysLimitStatus() == null || 
            limitConfig.getSysLimitStatus() != 1) {
            log.debug("没有启用的限流配置，直接执行方法，请求路径: {}", requestPath);
            // 没有启用的限流配置，直接执行方法
            return invocation.proceed();
        }
        
        // 检查是否针对当前登录账号
        if (limitConfig.getSysLimitCurrentUser() != null && limitConfig.getSysLimitCurrentUser() == 1) {
            // 如果限流配置是针对当前登录账号，但用户未登录，则不应用限流
            Long currentUserId = getUserId();
            if (currentUserId == null) {
                log.debug("限流配置针对当前登录账号，但用户未登录，直接执行方法，请求路径: {}", requestPath);
                return invocation.proceed();
            }
        }
        
        // 获取或创建限流器
        RateLimiter rateLimiter = getOrCreateRateLimiter(limitConfig);
        
        try {
            log.debug("执行限流检查，限流器名称: {}", limitConfig.getSysLimitPath());
            // 执行限流检查
            return rateLimiter.executeSupplier(() -> {
                try {
                    return invocation.proceed();
                } catch (Throwable throwable) {
                    // 按照阿里编码规范，应该区分检查异常和运行时异常
                    if (throwable instanceof RuntimeException) {
                        throw (RuntimeException) throwable;
                    } else if (throwable instanceof Error) {
                        throw (Error) throwable;
                    } else {
                        throw new RuntimeException(throwable);
                    }
                }
            });
        } catch (RequestNotPermitted e) {
            log.warn("限流触发，请求路径: {}, 限流器名称: {}", requestPath, limitConfig.getSysLimitPath());
            // 限流触发，记录限流事件
            createLimitEvent(limitConfig, requestPath);
            
            // 执行降级方法或抛出异常
            String fallbackMethod = limitConfig.getSysLimitFallbackMethod();
            if (StringUtils.isNotBlank(fallbackMethod)) {
                log.debug("执行降级方法: {}", fallbackMethod);
                return executeFallback(invocation, fallbackMethod, e);
            }
            
            // 抛出限流异常
            String message = limitConfig.getSysLimitMessage();
            if (StringUtils.isBlank(message)) {
                message = DEFAULT_ERROR_MESSAGE;
            }
            log.debug("抛出限流异常，消息: {}", message);
            throw new RuntimeException(message);
        }
    }
    
    /**
     * 获取当前请求路径
     * 
     * @return 请求路径
     */
    private String getCurrentRequestPath() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        
        // 获取请求URI
        String requestURI = request.getRequestURI();
        
        // 获取上下文路径
        String contextPath = request.getContextPath();
        if (StringUtils.isNotBlank(contextPath) && !"/".equals(contextPath)) {
            // 移除上下文路径前缀
            if (requestURI.startsWith(contextPath)) {
                requestURI = requestURI.substring(contextPath.length());
            }
        }
        
        return requestURI;
    }
    
    /**
     * 获取限流配置
     * 
     * @param requestPath 请求路径
     * @return 限流配置
     */
    private SysLimitConfiguration getLimitConfiguration(String requestPath) {
        if (StringUtils.isBlank(requestPath)) {
            return null;
        }
        
        // 先从缓存中获取
        SysLimitConfiguration cachedConfig = LIMIT_CONFIG_CACHE.get(requestPath);
        if (cachedConfig != null) {
            log.debug("从缓存中获取限流配置，请求路径: {}", requestPath);
            return cachedConfig;
        }
        
        // 从数据库查询
        List<SysLimitConfiguration> configs = sysLimitConfigurationService.getLimitConfigurationsByPath(requestPath);
        if (configs != null && !configs.isEmpty()) {
            // 使用第一个匹配的配置
            SysLimitConfiguration config = configs.get(0);
            LIMIT_CONFIG_CACHE.put(requestPath, config);
            log.debug("从数据库获取限流配置并缓存，请求路径: {}", requestPath);
            return config;
        }
        
        log.debug("未找到限流配置，请求路径: {}", requestPath);
        return null;
    }
    
    /**
     * 获取或创建限流器
     * 
     * @param limitConfig 限流配置
     * @return 限流器
     */
    private RateLimiter getOrCreateRateLimiter(SysLimitConfiguration limitConfig) {
        if (limitConfig == null) {
            throw new IllegalArgumentException("SysLimitConfiguration cannot be null");
        }
        
        String path = limitConfig.getSysLimitPath();
        if (StringUtils.isBlank(path)) {
            throw new IllegalArgumentException("SysLimitPath cannot be null or empty");
        }
        
        RateLimiter rateLimiter = RATE_LIMITER_CACHE.get(path);
        if (rateLimiter != null) {
            // 检查配置是否发生变化
            if (isConfigChanged(rateLimiter, limitConfig)) {
                // 配置已变化，移除旧的限流器并创建新的
                log.debug("限流配置已变化，重新创建限流器，路径: {}", path);
                RATE_LIMITER_CACHE.remove(path);
                rateLimiter = null;
            } else {
                log.debug("使用已存在的限流器，路径: {}", path);
                return rateLimiter;
            }
        }
        
        if (rateLimiter == null) {
            // 创建新的限流器
            RateLimiterConfig config = buildRateLimiterConfig(limitConfig);
            rateLimiter = rateLimiterRegistry.rateLimiter(path, config);
            RATE_LIMITER_CACHE.put(path, rateLimiter);
            log.debug("创建新的限流器，路径: {}", path);
        }
        
        return rateLimiter;
    }
    
    /**
     * 构建限流器配置
     * 
     * @param limitConfig 限流配置
     * @return 限流器配置
     */
    private RateLimiterConfig buildRateLimiterConfig(SysLimitConfiguration limitConfig) {
        if (limitConfig == null) {
            throw new IllegalArgumentException("SysLimitConfiguration cannot be null");
        }
        
        RateLimiterConfig.Builder builder = RateLimiterConfig.custom();
        
        // 设置每个周期的许可数量
        if (limitConfig.getSysLimitForPeriod() != null) {
            builder.limitForPeriod(limitConfig.getSysLimitForPeriod());
        }
        
        // 设置刷新周期
        if (limitConfig.getSysLimitRefreshPeriodSeconds() != null) {
            builder.limitRefreshPeriod(Duration.ofSeconds(limitConfig.getSysLimitRefreshPeriodSeconds()));
        }
        
        // 设置超时时间
        if (limitConfig.getSysLimitTimeoutDurationMillis() != null) {
            builder.timeoutDuration(Duration.ofMillis(limitConfig.getSysLimitTimeoutDurationMillis()));
        }
        
        RateLimiterConfig config = builder.build();
        log.debug("构建限流器配置，周期许可数: {}, 刷新周期: {}秒, 超时时间: {}毫秒",
                config.getLimitForPeriod(),
                config.getLimitRefreshPeriod().getSeconds(),
                config.getTimeoutDuration().toMillis());
        return config;
    }
    
    /**
     * 检查配置是否发生变化
     * 
     * @param rateLimiter 限流器
     * @param limitConfig 限流配置
     * @return 是否发生变化
     */
    private boolean isConfigChanged(RateLimiter rateLimiter, SysLimitConfiguration limitConfig) {
        if (rateLimiter == null || limitConfig == null) {
            return true;
        }
        
        RateLimiterConfig currentConfig = rateLimiter.getRateLimiterConfig();
        
        // 检查每个周期的许可数量
        if (limitConfig.getSysLimitForPeriod() != null && 
            !limitConfig.getSysLimitForPeriod().equals(currentConfig.getLimitForPeriod())) {
            log.debug("限流配置变化 - 周期许可数: {} -> {}", 
                    currentConfig.getLimitForPeriod(), limitConfig.getSysLimitForPeriod());
            return true;
        }
        
        // 检查刷新周期
        if (limitConfig.getSysLimitRefreshPeriodSeconds() != null) {
            Duration expectedRefreshPeriod = Duration.ofSeconds(limitConfig.getSysLimitRefreshPeriodSeconds());
            if (!expectedRefreshPeriod.equals(currentConfig.getLimitRefreshPeriod())) {
                log.debug("限流配置变化 - 刷新周期: {} -> {}", 
                        currentConfig.getLimitRefreshPeriod(), expectedRefreshPeriod);
                return true;
            }
        }
        
        // 检查超时时间
        if (limitConfig.getSysLimitTimeoutDurationMillis() != null) {
            Duration expectedTimeout = Duration.ofMillis(limitConfig.getSysLimitTimeoutDurationMillis());
            if (!expectedTimeout.equals(currentConfig.getTimeoutDuration())) {
                log.debug("限流配置变化 - 超时时间: {} -> {}", 
                        currentConfig.getTimeoutDuration(), expectedTimeout);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 创建限流事件
     * 
     * @param limitConfig 限流配置
     * @param requestPath 请求路径
     */
    private void createLimitEvent(SysLimitConfiguration limitConfig, String requestPath) {
        if (limitConfig == null || StringUtils.isBlank(requestPath)) {
            log.warn("无法创建限流事件，limitConfig或requestPath为空");
            return;
        }
        
        try {
            SysLimitInfo sysLimitInfo = new SysLimitInfo(limitConfig);
            sysLimitInfo.setSysLimitConfigurationId(limitConfig.getSysLimitConfigurationId());
            sysLimitInfo.setSysLimitPath(requestPath);
            sysLimitInfo.setSysLimitName(limitConfig.getSysLimitName());
            sysLimitInfo.setSysLimitDimension(limitConfig.getSysLimitDimension());
            sysLimitInfo.setSysLimitTime(new Date());
            sysLimitInfo.setSysUserId(getUserId());
            sysLimitInfo.setSysUserName(getUsername());
            sysLimitInfo.setClientIp(getIpAddress(getRequest()));
            sysLimitInfo.setRequestUrl(getUrl(getRequest()));
            sysLimitInfo.setRequestMethod(getRequest().getMethod());
            sysLimitInfo.setUserAgent(getHeader(getRequest(), "User-Agent"));
            
            // 发布限流事件
            applicationContext.publishEvent(sysLimitInfo);
            
            // 保存限流记录
            saveLimitRecord(sysLimitInfo, limitConfig);
        } catch (Exception e) {
            log.error("创建限流事件失败", e);
        }
    }
    
    /**
     * 保存限流记录
     * 
     * @param sysLimitInfo 限流信息
     * @param limitConfig 限流配置
     */
    private void saveLimitRecord(SysLimitInfo sysLimitInfo, SysLimitConfiguration limitConfig) {
        if (sysLimitInfo == null || limitConfig == null) {
            log.warn("无法保存限流记录，sysLimitInfo或limitConfig为空");
            return;
        }
        
        try {
            SysLimitRecord sysLimitRecord = new SysLimitRecord();
            sysLimitRecord.setSysLimitConfigurationId(sysLimitInfo.getSysLimitConfigurationId());
            sysLimitRecord.setSysLimitPath(sysLimitInfo.getSysLimitPath());
            sysLimitRecord.setSysLimitName(sysLimitInfo.getSysLimitName());
            sysLimitRecord.setSysLimitDimension(sysLimitInfo.getSysLimitDimension());
            sysLimitRecord.setSysLimitKey(sysLimitInfo.getSysLimitKey());
            sysLimitRecord.setSysLimitTime(sysLimitInfo.getSysLimitTime());
            sysLimitRecord.setSysUserId(sysLimitInfo.getSysUserId());
            sysLimitRecord.setSysUserName(sysLimitInfo.getSysUserName());
            sysLimitRecord.setClientIp(sysLimitInfo.getClientIp());
            sysLimitRecord.setRequestUrl(sysLimitInfo.getRequestUrl());
            sysLimitRecord.setRequestMethod(sysLimitInfo.getRequestMethod());
            sysLimitRecord.setUserAgent(sysLimitInfo.getUserAgent());
            sysLimitRecord.setSysLimitCurrentUser(limitConfig.getSysLimitCurrentUser());
            
            sysLimitRecordService.saveLimitRecord(sysLimitRecord);
        } catch (Exception e) {
            log.error("保存限流记录失败", e);
        }
    }
    
    /**
     * 执行降级方法
     * 
     * @param invocation 方法调用
     * @param fallbackMethodName 降级方法名称
     * @param exception 异常
     * @return 降级方法执行结果
     */
    private Object executeFallback(MethodInvocation invocation, String fallbackMethodName, Exception exception) {
        if (invocation == null || StringUtils.isBlank(fallbackMethodName)) {
            log.warn("无法执行降级方法，invocation或fallbackMethodName为空");
            throw new RuntimeException("执行降级方法失败");
        }
        
        try {
            Method originalMethod = invocation.getMethod();
            Object target = invocation.getThis();
            if (target == null) {
                log.warn("无法执行降级方法，target为空");
                throw new RuntimeException("执行降级方法失败");
            }
            
            Class<?> targetClass = target.getClass();
            
            // 查找降级方法
            Method fallbackMethod = findFallbackMethod(targetClass, fallbackMethodName, originalMethod, exception);
            if (fallbackMethod != null) {
                Object[] args = invocation.getArguments();
                Object[] fallbackArgs = new Object[args.length + 1];
                System.arraycopy(args, 0, fallbackArgs, 0, args.length);
                fallbackArgs[args.length] = exception;
                
                return fallbackMethod.invoke(target, fallbackArgs);
            }
            
            log.warn("未找到降级方法: {}", fallbackMethodName);
            throw new RuntimeException("限流触发且未找到降级方法: " + fallbackMethodName);
            
        } catch (Exception e) {
            log.error("执行降级方法失败: {}", fallbackMethodName, e);
            throw new RuntimeException("执行降级方法失败: " + fallbackMethodName, e);
        }
    }
    
    /**
     * 查找降级方法
     * 
     * @param targetClass 目标类
     * @param fallbackMethodName 降级方法名称
     * @param originalMethod 原始方法
     * @param exception 异常
     * @return 降级方法
     */
    private Method findFallbackMethod(Class<?> targetClass, String fallbackMethodName, Method originalMethod, Exception exception) {
        try {
            // 尝试查找带异常参数的降级方法
            Class<?>[] paramTypes = originalMethod.getParameterTypes();
            Class<?>[] fallbackParamTypes = new Class[paramTypes.length + 1];
            System.arraycopy(paramTypes, 0, fallbackParamTypes, 0, paramTypes.length);
            fallbackParamTypes[paramTypes.length] = Exception.class;
            
            return targetClass.getDeclaredMethod(fallbackMethodName, fallbackParamTypes);
        } catch (NoSuchMethodException e) {
            try {
                // 尝试查找不带异常参数的降级方法
                return targetClass.getDeclaredMethod(fallbackMethodName, originalMethod.getParameterTypes());
            } catch (NoSuchMethodException ex) {
                return null;
            }
        }
    }
    
    /**
     * 获取当前用户ID
     * 
     * @return 用户ID
     */
    private Long getUserId() {
        // 简单实现，实际项目中可能需要从安全上下文获取
        return null;
    }
    
    /**
     * 获取当前用户名
     * 
     * @return 用户名
     */
    private String getUsername() {
        // 简单实现，实际项目中可能需要从安全上下文获取
        return null;
    }
    
    /**
     * 获取请求URL
     * 
     * @param request HTTP请求
     * @return 请求URL
     */
    private String getUrl(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        
        StringBuffer url = request.getRequestURL();
        String queryString = request.getQueryString();
        
        if (queryString != null && !queryString.isEmpty()) {
            url.append("?").append(queryString);
        }
        
        return url.toString();
    }
    
    /**
     * 获取请求头
     * 
     * @param request HTTP请求
     * @param headerName 请求头名称
     * @return 请求头值
     */
    private String getHeader(HttpServletRequest request, String headerName) {
        if (request == null || StringUtils.isBlank(headerName)) {
            return null;
        }
        
        return request.getHeader(headerName);
    }
    
    /**
     * 获取客户端IP地址
     * 
     * @param request HTTP请求
     * @return 客户端IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 处理多个IP的情况（逗号分隔）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0];
        }
        
        return ip;
    }
    
    /**
     * 获取当前HTTP请求
     * 
     * @return HTTP请求对象
     */
    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
    
    /**
     * 获取响应对象
     * 
     * @return HTTP响应对象
     */
    private HttpServletResponse getResponse() {
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getResponse() : null;
    }
}