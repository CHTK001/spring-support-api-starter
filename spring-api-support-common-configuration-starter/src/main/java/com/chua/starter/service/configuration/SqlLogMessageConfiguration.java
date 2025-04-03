package com.chua.starter.service.configuration;

import com.chua.starter.common.support.configuration.SpringBeanUtils;
import com.chua.starter.datasource.spy.SimpleAppender;
import org.springframework.context.ApplicationContext;

/**
 * sql日志
 *
 * @author CH
 * @since 2024/9/6
 */
public class SqlLogMessageConfiguration extends SimpleAppender {


    public SqlLogMessageConfiguration() {
        ApplicationContext applicationContext = SpringBeanUtils.getApplicationContext();
    }

    @Override
    public String formatMessage(int connectionId, String now, long elapsed, String category, String prepared, String sql, String url) {
        return super.formatMessage(connectionId, now, elapsed, category, prepared, sql, url);
    }
}
