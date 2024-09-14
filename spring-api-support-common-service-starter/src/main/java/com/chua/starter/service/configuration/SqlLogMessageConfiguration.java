package com.chua.starter.service.configuration;

import com.chua.report.client.starter.report.event.ReportEvent;
import com.chua.report.client.starter.report.event.SqlEvent;
import com.chua.report.client.starter.service.ReportService;
import com.chua.starter.common.support.configuration.SpringBeanUtils;
import com.chua.starter.datasource.spy.SimpleAppender;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

/**
 * sql日志
 *
 * @author CH
 * @since 2024/9/6
 */
public class SqlLogMessageConfiguration extends SimpleAppender {

    private ReportService reportService;

    public SqlLogMessageConfiguration() {
        ApplicationContext applicationContext = SpringBeanUtils.getApplicationContext();
        try {
            reportService = applicationContext.getBean(ReportService.class);
        } catch (BeansException ignored) {
        }
    }

    @Override
    public String formatMessage(int connectionId, String now, long elapsed, String category, String prepared, String sql, String url) {
        if (null != reportService) {
            SqlEvent sqlEvent = new SqlEvent();
            Thread thread = Thread.currentThread();
            StackTraceElement[] stackTrace = thread.getStackTrace();
            sqlEvent.setThread(thread.getName());
            sqlEvent.setClassName(stackTrace[0].getClassName());
            String newSql = sql.toLowerCase().trim();
            if(newSql.startsWith("select")) {
                sqlEvent.setEvent("SELECT");
            } else if(newSql.startsWith("insert")) {
                sqlEvent.setEvent("INSERT");
            } else if(newSql.startsWith("update")) {
                sqlEvent.setEvent("UPDATE");
            } else if(newSql.startsWith("delete")) {
                sqlEvent.setEvent("DELETE");
            } else {
                sqlEvent.setEvent("OTHER");
            }
            sqlEvent.setSql(sql);
            ReportEvent<SqlEvent> reportData = new ReportEvent<>();
            reportData.setReportData(sqlEvent);
            reportData.setReportType(ReportEvent.ReportType.SQL);
            reportService.report(reportData);
        }
        return super.formatMessage(connectionId, now, elapsed, category, prepared, sql, url);
    }
}
