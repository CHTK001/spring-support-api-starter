package com.chua.starter.system.configuration;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import com.chua.common.support.json.Json;
import com.chua.common.support.lang.date.DateTime;
import com.chua.common.support.mdc.LogTrace;
import com.chua.redis.support.client.RedisClient;
import com.chua.redis.support.search.SearchIndex;
import com.chua.redis.support.search.SearchSchema;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.starter.common.support.configuration.SpringBeanUtils;
import com.chua.starter.common.support.project.Project;
import com.chua.starter.redis.support.service.RedisSearchService;
import com.chua.starter.system.dto.event.LogEvent;
import org.slf4j.MDC;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static com.chua.redis.support.constant.RedisConstant.REDIS_SEARCH_PREFIX;
import static com.chua.starter.system.constant.SystemConstant.LOG_NAME;
import static com.chua.starter.system.constant.SystemConstant.SUPER_ADMIN_EVENT_SQL_MESSAGE;

/**
 * 自定义日志
 *
 * @author CH
 * @since 2024/9/6
 */
public class CustomLogbackFilter extends Filter<ILoggingEvent> {

    private SocketSessionTemplate socketSessionTemplate;
    private RedisSearchService redisSearchService;
    private RedisClient redisClient;
    private String applicationName;
    public static final String LOG_INDEX_NAME_PREIX = REDIS_SEARCH_PREFIX + LOG_NAME + ":";


    @Override
    public FilterReply decide(ILoggingEvent event) {
        if(null == socketSessionTemplate) {
            check();
        }

        if(null != socketSessionTemplate) {
            String stringBuilder = "[" + DateTime.of(event.getTimeStamp()).toStandard() + "]" +
                    " [" + event.getLevel() + "] " +
                    " [" + MDC.get(LogTrace.TRACE_ID) + "] " +
                    " [" + event.getThreadName() + "] " +
                    " [" + event.getLoggerName() + "] " +
                    " - " +
                    event.getFormattedMessage();
            LogEvent logEvent = new LogEvent();
            logEvent.setLine(event.getCallerData()[0].getLineNumber());
            logEvent.setMessage(stringBuilder);
            logEvent.setTraceId(MDC.get(LogTrace.TRACE_ID));
            logEvent.setClassName(event.getCallerData()[0].getClassName());
            logEvent.setLevel(event.getLevel().toString());
            logEvent.setThread(event.getThreadName());
            logEvent.setLogger(event.getLoggerName());
            logEvent.setTimestamp(event.getTimeStamp());
            socketSessionTemplate.send(SUPER_ADMIN_EVENT_SQL_MESSAGE+ ":LOG", Json.toJSONString(logEvent));
            registerRedisSearch(logEvent);
        }
        return FilterReply.ACCEPT;
    }

    private void registerRedisSearch(LogEvent logEvent) {
        checkSearch();
        if(null != redisSearchService) {
            try {
                registerIndex();
            } catch (Exception ignored) {
            }

            try {
                registerDocument(logEvent);
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 注册文档
     * @param logEvent 日志信息
     */
    private void registerDocument(LogEvent logEvent) {
        Map<String, String> document = new HashMap<>(3);
        document.put("message",  logEvent.getMessage());
        document.put("timestamp", String.valueOf(logEvent.getTimestamp()));
        document.put("level", logEvent.getLevel());
        document.put("traceId", logEvent.getTraceId());
        document.put("className", logEvent.getClassName());
        document.put("applicationActive", logEvent.getApplicationActive());
        document.put("applicationHost", logEvent.getApplicationHost());
        document.put("applicationPort", String.valueOf(logEvent.getApplicationPort()));
        document.put("applicationName", logEvent.getApplicationName());
        redisSearchService.addDocument(LOG_INDEX_NAME_PREIX + applicationName, document);
    }

    /**
     * 注册索引
     */
    private void registerIndex() {
        SearchIndex searchIndex = new SearchIndex();
        searchIndex.setName(LOG_INDEX_NAME_PREIX + applicationName);
        searchIndex.setLanguage("chinese");
        SearchSchema searchSchema = new SearchSchema();
        searchSchema.addTextField("message", 10);
        searchSchema.addTextField("level", 1);
        searchSchema.addTextField("className", 1);
        searchSchema.addTextField("traceId", 10);
        searchSchema.addTextField("applicationName", 1);
        searchSchema.addTextField("applicationPort", 1);
        searchSchema.addTextField("applicationHost", 1);
        searchSchema.addTextField("applicationActive", 1);
        searchSchema.addSortableNumericField("timestamp");
        searchIndex.setSchema(searchSchema);
        redisSearchService.createIndex(searchIndex);
    }


    public void check() {
        if(null != socketSessionTemplate) {
            return;
        }
        try {
            ApplicationContext applicationContext = SpringBeanUtils.getApplicationContext();
            socketSessionTemplate = applicationContext.getBean(SocketSessionTemplate.class);
            this.applicationName = Project.getInstance().getApplicationName();
        } catch (Exception ignored) {
        }
    }
    public void checkSearch() {
        if(null != redisClient) {
            return;
        }
        try {
            ApplicationContext applicationContext = SpringBeanUtils.getApplicationContext();
            redisClient = applicationContext.getBean(RedisClient.class);
            redisSearchService = applicationContext.getBean(RedisSearchService.class);
        } catch (Exception ignored) {
        }
    }
}
