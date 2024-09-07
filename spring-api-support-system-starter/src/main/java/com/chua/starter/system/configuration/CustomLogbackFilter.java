package com.chua.starter.system.configuration;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import com.chua.common.support.json.JsonObject;
import com.chua.common.support.lang.date.DateTime;
import com.chua.common.support.mdc.LogTrace;
import com.chua.redis.support.client.RedisClient;
import com.chua.redis.support.search.SearchIndex;
import com.chua.redis.support.search.SearchSchema;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.starter.common.support.configuration.SpringBeanUtils;
import com.chua.starter.common.support.project.Project;
import com.chua.starter.redis.support.service.RedisSearchService;
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
            JsonObject jsonObject = JsonObject.create()
                            .fluent("level", event.getLevel().toString())
                            .fluent("traceId", MDC.get(LogTrace.TRACE_ID))
                            .fluent("message", event.getFormattedMessage())
                            .fluent("logger", event.getLoggerName())
                            .fluent("thread", event.getThreadName())
                            .fluent("level", event.getLevel().toString())
                            .fluent("timestamp", event.getTimeStamp())
                            .fluent("class", event.getCallerData()[0].getClassName())
                            .fluent("line", event.getCallerData()[0].getLineNumber());

            socketSessionTemplate.send(SUPER_ADMIN_EVENT_SQL_MESSAGE+ ":LOG", jsonObject.toJSONString());
            registerRedisSearch(stringBuilder, event);
        }
        return FilterReply.ACCEPT;
    }

    private void registerRedisSearch(String logMessage, ILoggingEvent event) {
        checkSearch();
        if(null != redisSearchService) {
            try {
                registerIndex();
            } catch (Exception ignored) {
            }

            try {
                registerDocument(logMessage, event);
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 注册文档
     * @param logMessage 日志信息
     * @param event 事件
     */
    private void registerDocument(String logMessage, ILoggingEvent event) {
        Map<String, String> document = new HashMap<>(3);
        document.put("text",  logMessage);
        document.put("timestamp", String.valueOf(event.getTimeStamp()));
        document.put("level", event.getLevel().toString());
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
        searchSchema.addTextField("text", 10);
        searchSchema.addTextField("level", 1);
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
