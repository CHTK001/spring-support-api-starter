package com.chua.starter.monitor.filter;

import com.chua.common.support.protocol.server.ServletFilter;
import com.chua.common.support.utils.StringUtils;
import com.chua.starter.common.support.configuration.SpringBeanUtils;
import com.chua.starter.monitor.entity.SystemServerLog;
import com.chua.starter.monitor.service.IptablesService;
import com.chua.starter.monitor.service.SystemServerLogService;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * ServletFilter 执行日志代理
 * <p>
 * 通过 JDK 动态代理拦截 Filter 的执行方法，采集并异步落库，不影响业务主流程。
 */
@Slf4j
public class LoggingServletFilterProxy implements InvocationHandler {

    private static final String[] PROCESS_METHOD_NAMES = new String[]{
            "filter", "doFilter", "apply", "accept", "handle"
    };

    private static final Pattern IP_PATTERN = Pattern.compile(
            "(^((25[0-5]|2[0-4]\\d|[0-1]?!?\\d?\\d)(\\.|$)){4}$)|(^([0-9a-fA-F]{0,4}:){2,7}[0-9a-fA-F]{0,4}$)"
    );

    private final ServletFilter target;
    private final Integer serverId;

    private LoggingServletFilterProxy(ServletFilter target, Integer serverId) {
        this.target = target;
        this.serverId = serverId;
    }

    /**
     * 包装代理
     */
    public static ServletFilter wrap(ServletFilter target, Integer serverId) {
        try {
            return (ServletFilter) Proxy.newProxyInstance(
                    target.getClass().getClassLoader(),
                    new Class[]{ServletFilter.class},
                    new LoggingServletFilterProxy(target, serverId)
            );
        } catch (Throwable e) {
            log.warn("创建ServletFilter日志代理失败, 将回退为原始对象: {}", e.getMessage());
            return target;
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        boolean shouldTrace = isProcessMethod(method.getName());
        long begin = 0L;
        String clientIp = null;
        if (shouldTrace) {
            begin = System.nanoTime();
            try {
                clientIp = resolveClientIp(args);
            } catch (Throwable ignore) {
            }
        }

        String processStatus = "OK";
        LocalDateTime accessTime = shouldTrace ? LocalDateTime.now() : null;
        try {
            Object result = method.invoke(target, args);
            if (shouldTrace) {
                // 根据返回值推断状态（布尔返回：PASS/BLOCK）
                if (result instanceof Boolean) {
                    processStatus = (Boolean) result ? "PASS" : "BLOCK";
                }
            }
            return result;
        } catch (Throwable ex) {
            if (shouldTrace) {
                processStatus = "ERROR";
            }
            throw ex.getCause() != null ? ex.getCause() : ex;
        } finally {
            if (shouldTrace) {
                try {
                    long durationMs = (System.nanoTime() - begin) / 1_000_000L;
                    SystemServerLog logItem = new SystemServerLog();
                    logItem.setServerId(serverId);
                    logItem.setFilterType(target.getClass().getName());
                    logItem.setProcessStatus(processStatus);
                    logItem.setClientIp(clientIp);
                    logItem.setAccessTime(accessTime);
                    logItem.setDurationMs(durationMs);
                    logItem.setStoreTime(LocalDateTime.now());

                    // 解析地理位置（仅当有 IP 时）
                    if (!StringUtils.isEmpty(clientIp)) {
                        try {
                            IptablesService iptablesService = SpringBeanUtils.getBean(IptablesService.class);
                            if (iptablesService != null) {
                                var rr = iptablesService.transferAddress(clientIp);
                                if (rr != null && rr.isSuccess() && rr.getData() != null) {
                                    var city = rr.getData();
                                    String geo = String.join(" ",
                                            defaultIfEmpty(city.getCountry(), ""),
                                            defaultIfEmpty(city.getProvince(), ""),
                                            defaultIfEmpty(city.getCity(), ""));
                                    logItem.setClientGeo(geo.trim());
                                }
                            }
                        } catch (Throwable ge) {
                            // 忽略地理位置失败
                        }
                    }

                    SystemServerLogService service = SpringBeanUtils.getBean(SystemServerLogService.class);
                    if (service != null) {
                        service.asyncRecord(logItem);
                    }
                } catch (Throwable e) {
                    // 任何日志落库异常都不影响主流程
                }
            }
        }
    }

    private boolean isProcessMethod(String name) {
        for (String n : PROCESS_METHOD_NAMES) {
            if (n.equals(name)) return true;
        }
        return false;
    }

    private String resolveClientIp(Object[] args) {
        if (null == args) return null;
        for (Object arg : args) {
            if (null == arg) continue;
            // 直接字符串匹配
            if (arg instanceof String s && IP_PATTERN.matcher(s).matches()) {
                return s;
            }
            // 反射调用常见方法
            try {
                for (String method : new String[]{
                        "getRemoteAddr", "getRemoteAddress", "getClientIp", "getIp", "getAddress", "getRemoteHost"
                }) {
                    try {
                        Method m = arg.getClass().getMethod(method);
                        Object v = m.invoke(arg);
                        if (v instanceof String sv && IP_PATTERN.matcher(sv).matches()) {
                            return sv;
                        }
                    } catch (NoSuchMethodException ignore) {
                    }
                }
            } catch (Throwable ignore) {
            }
        }
        return null;
    }

    private String defaultIfEmpty(String v, String def) {
        return StringUtils.isEmpty(v) ? def : v;
    }
}

