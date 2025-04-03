package com.chua.starter.service.configuration;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import com.chua.common.support.mdc.LogTrace;
import com.chua.common.support.utils.StringUtils;
import com.chua.report.client.starter.report.event.LogEvent;
import com.chua.report.client.starter.report.event.ReportEvent;
import com.chua.report.client.starter.service.ReportService;
import org.slf4j.MDC;
import org.slf4j.Marker;

/**
 * 自定义日志
 *
 * @author CH
 * @since 2024/9/6
 */
public class CustomLogbackFilter extends TurboFilter {

    private final ReportService reportService;

    public CustomLogbackFilter(ReportService reportService) {
        this.reportService = reportService;
    }


    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
        if(level.levelInt <= 10000) {
            return FilterReply.DENY;
        }

        if("com.chua.report.client.starter.service.ReportService".equalsIgnoreCase(logger.getName())) {
            return FilterReply.ACCEPT;
        }

        String name = logger.getName();
        if(name.startsWith("io.zbus") || name.startsWith("org.burningwave.core")) {
            return FilterReply.DENY;
        }

        String threadName = Thread.currentThread().getName();
        LogEvent logEvent = new LogEvent();
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        logEvent.setLine(stackTrace[0].getLineNumber());
        logEvent.setMessage(StringUtils.format(format, params));
        logEvent.setTraceId(MDC.get(LogTrace.TRACE_ID));
        logEvent.setClassName(stackTrace[0].getClassName());
        logEvent.setLevel(level.toString());
        logEvent.setThread(threadName);
        logEvent.setLogger(name);
        logEvent.setTimestamp(System.currentTimeMillis());
        ReportEvent<LogEvent> reportEvent = new ReportEvent<>();
        reportEvent.setReportData(logEvent);
        reportEvent.setReportType(ReportEvent.ReportType.LOG);
        reportService.report(reportEvent);
        return FilterReply.ACCEPT;
    }




}
