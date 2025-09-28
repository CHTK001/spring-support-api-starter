package com.chua.starter.service.aspect;

import com.chua.common.support.utils.IpUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.logger.RequestUtils;
import com.chua.starter.service.SysLimitRecordService;
import com.chua.starter.system.entity.SysLimitInfo;
import com.chua.starter.system.entity.SysLimitRecord;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Date;

/**
 * 限流切面通知
 * 
 * @author CH
 * @since 2025/9/28
 */
@Slf4j
public class SysLimitPointcutAdvisor extends StaticMethodMatcherPointcutAdvisor implements InitializingBean {

    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private SysLimitRecordService sysLimitRecordService;

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        // 匹配所有被RateLimiter注解的方法
        return method.isAnnotationPresent(io.github.resilience4j.ratelimiter.annotation.RateLimiter.class);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.setAdvice(new MethodInterceptor() {
            @Nullable
            @Override
            public Object invoke(@Nullable MethodInvocation invocation) throws Throwable {
                return invokeValue(invocation);
            }
        });
    }

    /**
     * 执行值
     *
     * @param invocation 调用
     * @return {@link Object}
     * @throws Throwable 可丢弃
     */
    private Object invokeValue(MethodInvocation invocation) throws Throwable {
        Object proceed = null;
        Throwable throwable = null;
        long startTime = System.currentTimeMillis();
        
        try {
            proceed = invocation.proceed();
        } catch (RequestNotPermitted e) {
            // 限流异常，记录限流事件
            createLimitEvent(invocation, startTime);
            throw e;
        } catch (Throwable e) {
            throwable = e;
        }
        
        return proceed;
    }

    /**
     * 创建限流事件
     * 
     * @param invocation 方法调用
     * @param startTime 开始时间
     */
    void createLimitEvent(MethodInvocation invocation, long startTime) {
        try {
            Method method = invocation.getMethod();
            io.github.resilience4j.ratelimiter.annotation.RateLimiter rateLimiterAnnotation = 
                method.getAnnotation(io.github.resilience4j.ratelimiter.annotation.RateLimiter.class);
            
            if (rateLimiterAnnotation == null) {
                return;
            }
            
            String name = rateLimiterAnnotation.name();
            SysLimitInfo sysLimitInfo = new SysLimitInfo(name);
            sysLimitInfo.setSysLimitPath(RequestUtils.getUrl(getRequest()));
            sysLimitInfo.setSysLimitName(name);
            sysLimitInfo.setSysLimitTime(new Date());
            sysLimitInfo.setSysUserId(RequestUtils.getUserId());
            sysLimitInfo.setSysUserName(RequestUtils.getUsername());
            sysLimitInfo.setClientIp(RequestUtils.getIpAddress(getRequest()));
            sysLimitInfo.setRequestUrl(RequestUtils.getUrl(getRequest()));
            sysLimitInfo.setRequestMethod(getRequest().getMethod());
            sysLimitInfo.setUserAgent(RequestUtils.getHeader(getRequest(), "User-Agent"));
            
            // 发布限流事件
            applicationContext.publishEvent(sysLimitInfo);
            
            // 保存限流记录
            saveLimitRecord(sysLimitInfo);
        } catch (Exception e) {
            log.error("创建限流事件失败", e);
        }
    }
    
    /**
     * 保存限流记录
     * 
     * @param sysLimitInfo 限流信息
     */
    private void saveLimitRecord(SysLimitInfo sysLimitInfo) {
        try {
            SysLimitRecord sysLimitRecord = new SysLimitRecord();
            sysLimitRecord.setSysLimitPath(sysLimitInfo.getSysLimitPath());
            sysLimitRecord.setSysLimitName(sysLimitInfo.getSysLimitName());
            sysLimitRecord.setSysLimitTime(sysLimitInfo.getSysLimitTime());
            sysLimitRecord.setSysUserId(sysLimitInfo.getSysUserId());
            sysLimitRecord.setSysUserName(sysLimitInfo.getSysUserName());
            sysLimitRecord.setClientIp(sysLimitInfo.getClientIp());
            sysLimitRecord.setRequestUrl(sysLimitInfo.getRequestUrl());
            sysLimitRecord.setRequestMethod(sysLimitInfo.getRequestMethod());
            sysLimitRecord.setUserAgent(sysLimitInfo.getUserAgent());
            
            sysLimitRecordService.saveLimitRecord(sysLimitRecord);
        } catch (Exception e) {
            log.error("保存限流记录失败", e);
        }
    }
    
    /**
     * 获取当前请求
     * 
     * @return HttpServletRequest
     */
    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
    
    /**
     * 获取当前响应
     * 
     * @return HttpServletResponse
     */
    private HttpServletResponse getResponse() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getResponse() : null;
    }
}