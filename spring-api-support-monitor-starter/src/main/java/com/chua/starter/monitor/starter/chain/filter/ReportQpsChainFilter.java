package com.chua.starter.monitor.starter.chain.filter;

import com.chua.common.support.annotations.SpiDescribe;
import com.chua.common.support.chain.ChainContext;
import com.chua.common.support.chain.FilterChain;
import com.chua.common.support.chain.filter.Filter;
import com.chua.common.support.geo.GeoCity;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.common.support.objects.annotation.AutoInject;
import com.chua.common.support.objects.annotation.Config;
import com.chua.common.support.protocol.request.BadResponse;
import com.chua.common.support.protocol.request.Request;
import com.chua.common.support.utils.ThreadUtils;
import com.chua.netty.support.proxy.filter.LastNettyFinishChainFilter;
import com.chua.starter.monitor.starter.entity.MonitorProxyLog;
import com.chua.starter.monitor.starter.service.IptablesService;
import com.chua.starter.monitor.starter.service.MonitorProxyLogService;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.concurrent.ExecutorService;

/**
 * QPS限流过滤器
 * <p>
 * 该过滤器实现了基于QPS(每秒查询率)的限流功能，防止系统过载。
 * 通过ReportQpsLimitFactory配置，可以针对不同的API路径设置不同的QPS限制。
 * 同时记录详细的访问日志，包括请求URL、IP地址、地理位置、限流状态等信息。
 *
 * @author CH
 * @since 2024/7/31
 */
@SpiDescribe(value = "QPS限流")
@Slf4j
public class ReportQpsChainFilter implements Filter {

    private static final ExecutorService POOL = ThreadUtils.newVirtualThreadExecutor();
    @Config("serverId")
    private String serverId;
    @AutoInject
    private ReportFactory reportFactory;
    @AutoInject
    private IptablesService iptablesService;

    @AutoInject
    private MonitorProxyLogService monitorProxyLogService;

    @Override
    public <T> void doFilter(ChainContext<T> context, FilterChain filterChain) {
        Request request = context.getRequest();
        String url = request.url();
        String hostString = request.getIpAddress();

        // 提取浏览器和请求头信息
        String userAgent = request.getHeader("User-Agent");
        String fingerprint = request.getHeader("X-Fingerprint");

        long startTime = System.currentTimeMillis();
        boolean allowed = false;
        String message = "QPS限流";

        try {
            // 检查是否允许通过QPS限流
            if (reportFactory.tryAcquireQps(url)) {
                // 允许通过，记录日志
                allowed = true;
                message = "允许通过";
                log.debug("QPS限流允许通过: URL={}, IP={}", url, hostString);
                doRegisterLog(url, hostString, "allow", startTime, userAgent, fingerprint);
                filterChain.doFilter(context);
                return;
            } else {
                // QPS超限，记录详细信息
                long currentQps = reportFactory.getCurrentQps(url);
                log.warn("QPS限流拦截: URL={}, IP={}, 当前QPS={}", url, hostString, currentQps);
            }
        } catch (Exception e) {
            // 发生异常时，记录错误信息
            log.error("QPS限流检查异常: URL={}, IP={}, 错误={}", url, hostString, e.getMessage(), e);
            allowed = true;
            message = "检查异常，默认放行";
            doRegisterLog(url, hostString, "allow", startTime, userAgent, fingerprint);
            filterChain.doFilter(context);
            return;
        }

        // 被限流，拒绝请求
        message = "QPS超限，请求被拒绝";
        log.info("QPS限流拒绝请求: URL={}, IP={}, 耗时={}ms", url, hostString, (System.currentTimeMillis() - startTime));
        doRegisterLog(url, hostString, "deny", startTime, userAgent, fingerprint);
        context.setResponse(new BadResponse(request, 429, "请求过于频繁，请稍后再试"));
        filterChain.setFinish();
        filterChain.addLast(new LastNettyFinishChainFilter());
        filterChain.doFilter(context);
    }

    /**
     * 记录访问日志（包含浏览器信息）
     *
     * @param url         请求URL
     * @param hostString  IP地址
     * @param type        处理类型：allow-允许，deny-拒绝
     * @param startTime   请求开始时间
     * @param userAgent   User-Agent字符串
     * @param fingerprint 浏览器指纹
     */
    private void doRegisterLog(String url, String hostString, String type, long startTime, String userAgent, String fingerprint) {
        POOL.execute(() -> {
            try {
                long duration = System.currentTimeMillis() - startTime;

                MonitorProxyLog monitorProxyLog = new MonitorProxyLog();
                monitorProxyLog.setMonitorProxyLogUrl(url);
                monitorProxyLog.setMonitorProxyLogServerId(serverId);
                monitorProxyLog.setMonitorProxyLogAddress(hostString);
                monitorProxyLog.setMonitorProxyLogType("QPS");
                monitorProxyLog.setMonitorProxyLogCode("deny".equals(type) ? "-1" : "0");

                // 设置处理耗时（毫秒转换为BigDecimal）
                monitorProxyLog.setMonitorProxyLogCost(BigDecimal.valueOf(duration));

                // 设置消息内容
                String message = "deny".equals(type) ? "QPS限流拒绝请求" : "QPS限流允许通过";
                monitorProxyLog.setMonitorProxyLogMsg(message);

                // 记录User-Agent信息
                monitorProxyLog.setMonitorProxyLogUserAgent(userAgent);

                // 记录浏览器指纹
                monitorProxyLog.setMonitorProxyLogFingerprint(fingerprint);

                // 解析User-Agent获取浏览器、操作系统和设备信息
                if (userAgent != null && !userAgent.isEmpty()) {
                    try {
                        // 解析浏览器类型
                        String browser = parseBrowser(userAgent);
                        monitorProxyLog.setMonitorProxyLogBrowser(browser);

                        // 解析操作系统
                        String os = parseOperatingSystem(userAgent);
                        monitorProxyLog.setMonitorProxyLogOs(os);

                        // 解析设备类型
                        String device = parseDevice(userAgent);
                        monitorProxyLog.setMonitorProxyLogDevice(device);

                        log.debug("解析User-Agent成功: 浏览器={}, 操作系统={}, 设备={}", browser, os, device);
                    } catch (Exception e) {
                        log.debug("解析User-Agent失败: {}", e.getMessage());
                    }
                }

                // 获取IP地理位置信息
                try {
                    ReturnResult<GeoCity> geoCityReturnResult = iptablesService.transferAddress(hostString);
                    if (geoCityReturnResult.isOk()) {
                        GeoCity geoCity = geoCityReturnResult.getData();
                        monitorProxyLog.setMonitorProxyLogAddressGeo(geoCity.getCity());

                        // 记录更详细的地理位置信息
                        if (geoCity.getCountry() != null) {
                            monitorProxyLog.setMonitorProxyLogAddressCountry(geoCity.getCountry());
                        }
                        if (geoCity.getProvince() != null) {
                            monitorProxyLog.setMonitorProxyLogAddressProvince(geoCity.getProvince());
                        }
                    }
                } catch (Exception e) {
                    log.debug("获取地理位置信息失败: {}", e.getMessage());
                }

                // 设置日期和时间信息
                monitorProxyLog.setMonitorProxyLogDate(LocalDate.now().toEpochSecond(LocalTime.of(0, 0, 0), ZoneOffset.UTC));
                monitorProxyLog.setMonitorProxyLogHour(LocalDateTime.now()
                        .withMinute(0).withSecond(0).withNano(0)
                        .toEpochSecond(ZoneOffset.UTC));

                // 添加QPS相关信息
                if (url != null) {
                    long currentQps = reportFactory.getCurrentQps(url);
                    monitorProxyLog.setMonitorProxyLogQps(currentQps);
                }

                // 设置当前时间作为创建和更新时间
                Date now = new Date();
                monitorProxyLog.setCreateTime(now);
                monitorProxyLog.setUpdateTime(now);

                // 设置创建者和更新者信息
                monitorProxyLog.setCreateName("System");
                monitorProxyLog.setUpdateName("System");

                // 保存日志
                monitorProxyLogService.save(monitorProxyLog);

                if (log.isDebugEnabled()) {
                    log.debug("QPS限流日志已记录: URL={}, IP={}, 类型={}, 耗时={}ms, QPS={}, 浏览器={}",
                            url, hostString, type, duration, monitorProxyLog.getMonitorProxyLogQps(),
                            monitorProxyLog.getMonitorProxyLogBrowser());
                }
            } catch (Exception e) {
                log.error("记录QPS限流日志失败: {}", e.getMessage(), e);
            }
        });
    }

    /**
     * 从User-Agent解析浏览器类型
     *
     * @param userAgent User-Agent字符串
     * @return 浏览器类型
     */
    private String parseBrowser(String userAgent) {
        if (userAgent == null) {
            return "Unknown";
        }

        userAgent = userAgent.toLowerCase();

        if (userAgent.contains("msie") || userAgent.contains("trident")) {
            return "Internet Explorer";
        } else if (userAgent.contains("edge") || userAgent.contains("edg")) {
            return "Microsoft Edge";
        } else if (userAgent.contains("chrome") && !userAgent.contains("chromium")) {
            return "Chrome";
        } else if (userAgent.contains("firefox")) {
            return "Firefox";
        } else if (userAgent.contains("safari") && !userAgent.contains("chrome")) {
            return "Safari";
        } else if (userAgent.contains("opera") || userAgent.contains("opr")) {
            return "Opera";
        } else if (userAgent.contains("ucbrowser")) {
            return "UC Browser";
        } else if (userAgent.contains("qqbrowser") || userAgent.contains("qq")) {
            return "QQ Browser";
        } else if (userAgent.contains("wechat") || userAgent.contains("micromessenger")) {
            return "WeChat";
        } else if (userAgent.contains("baidu")) {
            return "Baidu Browser";
        } else if (userAgent.contains("maxthon")) {
            return "Maxthon";
        } else if (userAgent.contains("sogou")) {
            return "Sogou Browser";
        } else {
            return "Other";
        }
    }

    /**
     * 从User-Agent解析操作系统
     *
     * @param userAgent User-Agent字符串
     * @return 操作系统
     */
    private String parseOperatingSystem(String userAgent) {
        if (userAgent == null) {
            return "Unknown";
        }

        userAgent = userAgent.toLowerCase();

        if (userAgent.contains("windows nt 10.0")) {
            return "Windows 10";
        } else if (userAgent.contains("windows nt 6.3")) {
            return "Windows 8.1";
        } else if (userAgent.contains("windows nt 6.2")) {
            return "Windows 8";
        } else if (userAgent.contains("windows nt 6.1")) {
            return "Windows 7";
        } else if (userAgent.contains("windows nt 6.0")) {
            return "Windows Vista";
        } else if (userAgent.contains("windows nt 5.1")) {
            return "Windows XP";
        } else if (userAgent.contains("windows nt")) {
            return "Windows";
        } else if (userAgent.contains("mac os x")) {
            return "macOS";
        } else if (userAgent.contains("android")) {
            return "Android";
        } else if (userAgent.contains("iphone") || userAgent.contains("ipad") || userAgent.contains("ipod")) {
            return "iOS";
        } else if (userAgent.contains("linux")) {
            return "Linux";
        } else if (userAgent.contains("unix")) {
            return "Unix";
        } else {
            return "Other";
        }
    }

    /**
     * 从User-Agent解析设备类型
     *
     * @param userAgent User-Agent字符串
     * @return 设备类型
     */
    private String parseDevice(String userAgent) {
        if (userAgent == null) {
            return "Unknown";
        }

        userAgent = userAgent.toLowerCase();

        if (userAgent.contains("mobile") || userAgent.contains("android") ||
                userAgent.contains("iphone") || userAgent.contains("ipod") ||
                userAgent.contains("blackberry") || userAgent.contains("windows phone")) {
            return "Mobile";
        } else if (userAgent.contains("ipad") || userAgent.contains("tablet")) {
            return "Tablet";
        } else {
            return "Desktop";
        }
    }
}
