package com.chua.starter.system.configuration;

import com.chua.common.support.json.Json;
import com.chua.redis.support.client.RedisClient;
import com.chua.redis.support.search.SearchIndex;
import com.chua.redis.support.search.SearchSchema;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.starter.common.support.configuration.SpringBeanUtils;
import com.chua.starter.common.support.project.Project;
import com.chua.starter.datasource.spy.SimpleAppender;
import com.chua.starter.redis.support.service.RedisSearchService;
import com.chua.starter.system.dto.event.SqlEvent;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static com.chua.redis.support.constant.RedisConstant.REDIS_SEARCH_PREFIX;
import static com.chua.starter.system.constant.SystemConstant.SQL_NAME;
import static com.chua.starter.system.constant.SystemConstant.SUPER_ADMIN_EVENT_SQL_MESSAGE;

/**
 * sql日志
 * @author CH
 * @since 2024/9/6
 */
public class SqlLogMessageConfiguration extends SimpleAppender {

    private final String applicationName;
    private RedisClient redisClient;
    private SocketSessionTemplate socketSessionTemplate;
    private RedisSearchService redisSearchService;
    public static final String LOG_INDEX_NAME_PREIX = REDIS_SEARCH_PREFIX + SQL_NAME + ":";

    public SqlLogMessageConfiguration() {
        ApplicationContext applicationContext = SpringBeanUtils.getApplicationContext();
        try {
            socketSessionTemplate = applicationContext.getBean(SocketSessionTemplate.class);
        } catch (BeansException ignored) {
        }
        applicationName = Project.getInstance().getApplicationName();
        try {
            redisClient = applicationContext.getBean(RedisClient.class);
        } catch (BeansException ignored) {
        }
    }

    @Override
    public String formatMessage(int connectionId, String now, long elapsed, String category, String prepared, String sql, String url) {
        if(null != socketSessionTemplate) {
            if(!sql.contains("SUPER_ADMIN")) {
                SqlEvent sqlEvent = new SqlEvent();
                sqlEvent.setSql(sql);
                socketSessionTemplate.send(SUPER_ADMIN_EVENT_SQL_MESSAGE + ":SQL", Json.toJSONString(sqlEvent));
                registerRedisSearch(sqlEvent);
            }
        }
        return super.formatMessage(connectionId, now, elapsed, category, prepared, sql, url);
    }

    private void registerRedisSearch(SqlEvent sqlEvent) {
        if(null != redisClient) {
            if(null ==  redisSearchService) {
                redisSearchService = SpringBeanUtils.getBean(RedisSearchService.class);
            }

            try {
                registerIndex();
            } catch (Exception ignored) {
            }

            try {
                registerDocument(sqlEvent);
            } catch (Exception ignored) {
            }


        }
    }

    private void registerDocument(SqlEvent sqlEvent) {

        try {
            Map<String, String> document = new HashMap<>(2);
            document.put("sql",  sqlEvent.getSql());
            document.put("timestamp", String.valueOf(System.currentTimeMillis()));
            document.put("applicationActive", sqlEvent.getApplicationActive());
            document.put("applicationHost", sqlEvent.getApplicationHost());
            document.put("applicationPort", String.valueOf(sqlEvent.getApplicationPort()));
            document.put("applicationName", sqlEvent.getApplicationName());
            redisSearchService.addDocument(LOG_INDEX_NAME_PREIX + applicationName, document);
        } catch (Exception ignored) {
        }
    }

    private void registerIndex() {
        try {
            SearchIndex searchIndex = new SearchIndex();
            searchIndex.setName(LOG_INDEX_NAME_PREIX + applicationName);
            searchIndex.setLanguage("chinese");
            SearchSchema searchSchema = new SearchSchema();
            searchSchema.addTextField("sql", 10);
            searchSchema.addTextField("applicationName", 1);
            searchSchema.addTextField("applicationPort", 1);
            searchSchema.addTextField("applicationHost", 1);
            searchSchema.addTextField("applicationActive", 1);
            searchSchema.addSortableNumericField("timestamp");
            searchIndex.setSchema(searchSchema);
            redisSearchService.createIndex(searchIndex);
        } catch (Exception ignored) {
        }
    }
}
