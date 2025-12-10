package com.chua.starter.monitor.controller.data;

import com.chua.common.support.json.Json;
import com.chua.common.support.lang.code.ReturnResult;
import com.chua.redis.support.constant.RedisConstant;
import com.chua.socket.support.session.SocketSessionTemplate;
import com.chua.starter.monitor.entity.SystemDataLogRecord;
import com.chua.starter.redis.support.service.RedisSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 系统数据事件（日志/备份）推送入口
 */
@Slf4j
@RestController
@RequestMapping("/system/data/event")
@Tag(name = "系统数据事件")
@RequiredArgsConstructor
public class SystemDataEventController {

    private final SocketSessionTemplate socketSessionTemplate;
    private final RedisSearchService redisSearchService;

    @PostMapping("/log")
    @Operation(summary = "推送日志事件（保存到RedisSearch并通过SocketIO推送）")
    public ReturnResult<Boolean> pushLog(@RequestBody SystemDataLogRecord record) {
        try {
            if (record.getTimestamp() == null) {
                record.setTimestamp(System.currentTimeMillis());
            }
            if (record.getId() == null || record.getId().isEmpty()) {
                record.setId(record.getSettingId() + ":" + record.getTimestamp());
            }
            String index = RedisConstant.REDIS_SEARCH_PREFIX + "system:data:log";
            Map<String, String> doc = BeanMap.create(record);
            redisSearchService.addDocument(index, doc);
            socketSessionTemplate.send("system/data/log", Json.toJson(record));
            return ReturnResult.ok(true);
        } catch (Exception e) {
            log.error("推送日志事件失败", e);
            return ReturnResult.error(e.getMessage());
        }
    }
}


