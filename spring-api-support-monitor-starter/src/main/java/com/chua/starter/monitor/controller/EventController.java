package com.chua.starter.monitor.controller;

import com.chua.common.support.lang.code.ReturnResult;
import com.chua.redis.support.search.SearchResultItem;
import com.chua.socketio.support.session.SocketSessionTemplate;
import com.chua.socketio.support.session.SocketUser;
import com.chua.starter.monitor.service.EventService;
import com.chua.starter.mybatis.entity.Query;
import com.chua.starter.service.event.EventRequest;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 事件接口
 * @author CH
 * @since 2024/9/7
 */
@RestController
@Slf4j
@Api(tags = "事件接口")
@Tag(name = "事件接口")
@RequestMapping("/v2/event")
@RequiredArgsConstructor
public class EventController {

    final EventService eventService;


    final SocketSessionTemplate socketSessionTemplate;

    /**
     * 查询基本信息。
     *
     * @param request 监控代理的唯一标识符。
     * @return 返回操作结果，如果操作成功，返回true；否则返回false，并附带错误信息。
     */
    @Operation(summary = "查询在线用户")
    @GetMapping("online")
    public ReturnResult<List<SocketUser>> getOnline(String type) {
        return ReturnResult.success(socketSessionTemplate.getOnlineSession(type));
    }
    /**
     * 查询基本信息。
     *
     * @param request 监控代理的唯一标识符。
     * @return 返回操作结果，如果操作成功，返回true；否则返回false，并附带错误信息。
     */
    @Operation(summary = "查询日志信息")
    @GetMapping("page")
    public ReturnResult<SearchResultItem> event(@ParameterObject Query<?> query, @ParameterObject EventRequest request) {
        return eventService.event(query, request);
    }

}
