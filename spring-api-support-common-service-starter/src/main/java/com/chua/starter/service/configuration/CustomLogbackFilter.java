package com.chua.starter.service.configuration;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import com.chua.common.support.json.Json;
import com.chua.common.support.mdc.LogTrace;
import com.chua.common.support.utils.NumberUtils;
import com.chua.common.support.utils.StringUtils;
import com.chua.redis.support.search.SearchIndex;
import com.chua.redis.support.search.SearchSchema;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.starter.common.support.application.GlobalSettingFactory;
import com.chua.starter.common.support.project.Project;
import com.chua.starter.redis.support.service.RedisSearchService;
import com.chua.starter.service.constant.SystemConstant;
import com.chua.starter.system.dto.event.LogEvent;
import com.chua.starter.system.setting.ExpireSetting;
import org.slf4j.MDC;
import org.slf4j.Marker;

import java.util.HashMap;
import java.util.Map;

import static com.chua.common.support.constant.CommonConstant.EMPTY;
import static com.chua.redis.support.constant.RedisConstant.REDIS_SEARCH_PREFIX;

/**
 * 自定义日志
 *
 * @author CH
 * @since 2024/9/6
 */
public class CustomLogbackFilter extends TurboFilter {

    private final SocketSessionTemplate socketSessionTemplate;
    private final RedisSearchService redisSearchService;
    private final String applicationName;

    public CustomLogbackFilter(SocketSessionTemplate socketSessionTemplate, RedisSearchService redisSearchService) {
        this.socketSessionTemplate = socketSessionTemplate;
        this.redisSearchService = redisSearchService;
        this.applicationName = Project.getInstance().getApplicationName();
    }

    public static final String LOG_INDEX_NAME_PREIX = REDIS_SEARCH_PREFIX + SystemConstant.LOG_NAME + ":";

    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
        if(level.levelInt <= 10000) {
            return FilterReply.DENY;
        }
        String threadName = Thread.currentThread().getName();
//        String stringBuilder = "[" + DateTime.now().toStandard() + "]" +
//                " [" + level.toString() + "] " +
//                " [" + MDC.get(LogTrace.TRACE_ID) + "] " +
//                " [" + threadName + "] " +
//                " [" + logger.getName() + "] " +
//                " - " +
//                format;
        LogEvent logEvent = new LogEvent();
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        logEvent.setLine(stackTrace[0].getLineNumber());
        logEvent.setMessage(StringUtils.format(format, params));
        logEvent.setTraceId(MDC.get(LogTrace.TRACE_ID));
        logEvent.setClassName(stackTrace[0].getClassName());
        logEvent.setLevel(level.toString());
        logEvent.setThread(threadName);
        logEvent.setLogger(logger.getName());
        logEvent.setTimestamp(System.currentTimeMillis());
        socketSessionTemplate.send(SystemConstant.SUPER_ADMIN_EVENT_SQL_MESSAGE + "_LOG", Json.toJSONString(logEvent));
        registerRedisSearch(logEvent);
        return FilterReply.ACCEPT;
    }

    private void registerRedisSearch(LogEvent logEvent) {
        try {
            registerIndex();
        } catch (Exception ignored) {
        }

        try {
            registerDocument(logEvent);
        } catch (Exception ignored) {
        }
    }

    /**
     * 注册文档
     *
     * @param logEvent 日志信息
     */
    private void registerDocument(LogEvent logEvent) {
        Map<String, String> document = new HashMap<>(3);
        document.put("text", logEvent.getMessage());
        document.put("type", "log");
        document.put("timestamp", String.valueOf(logEvent.getTimestamp()));
        document.put("level", logEvent.getLevel());
        document.put("traceId", StringUtils.defaultString(logEvent.getTraceId() , EMPTY));
        document.put("className", logEvent.getClassName());
        document.put("applicationActive", logEvent.getApplicationActive());
        document.put("applicationHost", logEvent.getApplicationHost());
        document.put("applicationPort", String.valueOf(logEvent.getApplicationPort()));
        document.put("applicationName", logEvent.getApplicationName());
        ExpireSetting expire = GlobalSettingFactory.getInstance().get("expire", ExpireSetting.class);
        document.put("expire", String.valueOf(NumberUtils.defaultIfNullOrPositive(expire.getLog(), 86400 * 7)));
        redisSearchService.addDocument(LOG_INDEX_NAME_PREIX + Project.getInstance().calcApplicationUuid(), document);
    }

    /**
     * 注册索引
     */
    private void registerIndex() {
        SearchIndex searchIndex = new SearchIndex();
        searchIndex.setName(LOG_INDEX_NAME_PREIX + Project.getInstance().calcApplicationUuid() );
        searchIndex.setLanguage("chinese");
        SearchSchema searchSchema = new SearchSchema();
        searchSchema.addTextField("text", 10);
        searchSchema.addTextField("type", 10);
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


}
