package com.chua.starter.system.configuration;

import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.starter.common.support.configuration.SpringBeanUtils;
import com.chua.starter.datasource.spy.SimpleAppender;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

/**
 * sql日志
 * @author CH
 * @since 2024/9/6
 */
public class SqlLogMessageConfiguration extends SimpleAppender {

    private SocketSessionTemplate socketSessionTemplate;

    public SqlLogMessageConfiguration() {
        ApplicationContext applicationContext = SpringBeanUtils.getApplicationContext();
        try {
            socketSessionTemplate = applicationContext.getBean(SocketSessionTemplate.class);
        } catch (BeansException ignored) {
        }
    }

    @Override
    public String formatMessage(int connectionId, String now, long elapsed, String category, String prepared, String sql, String url) {
        if(null != socketSessionTemplate) {
            if(!sql.contains("SUPER_ADMIN")) {
                socketSessionTemplate.send("SUPER_ADMIN_EVENT_SQL_MESSAGE", sql);
            }
        }
        return super.formatMessage(connectionId, now, elapsed, category, prepared, sql, url);
    }
}
