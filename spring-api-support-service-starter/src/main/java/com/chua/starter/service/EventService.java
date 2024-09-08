package com.chua.starter.service;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.redis.support.search.SearchResultItem;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.system.pojo.event.EventRequest;

/**
 * 事件服务
 * @author CH
 * @since 2024/9/7
 */
public interface EventService {
    /**
     * 事件
     *
     * @param query query
     * @param request request
     * @return result
     */
    ReturnResult<SearchResultItem> event(Query<?> query, EventRequest request);
}
