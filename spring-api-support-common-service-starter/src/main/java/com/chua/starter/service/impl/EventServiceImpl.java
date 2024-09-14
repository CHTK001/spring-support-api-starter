package com.chua.starter.service.impl;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.redis.support.search.SearchQuery;
import com.chua.redis.support.search.SearchResultItem;
import com.chua.starter.common.support.project.Project;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.redis.support.service.RedisSearchService;
import com.chua.starter.service.EventService;
import com.chua.starter.service.event.EventRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.chua.redis.support.constant.RedisConstant.REDIS_SEARCH_PREFIX;

/**
 * 事件
 * @author CH
 * @since 2024/9/7
 */
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    final RedisSearchService redisSearchService;
    @Override
    public ReturnResult<SearchResultItem> event(Query<?> query, EventRequest request) {
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setIndex(REDIS_SEARCH_PREFIX + request.getEvent() + ":" + Project.getInstance().calcApplicationUuid());
        searchQuery.setLanguage("chinese");
        searchQuery.setKeyword(request.getKeyword());
        searchQuery.setSort(request.getSort());
        return redisSearchService.queryAll(searchQuery, query.offset(), query.getPageSize());
    }
}
