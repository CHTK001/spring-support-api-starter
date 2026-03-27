package com.chua.starter.service.trace.storage;

import com.chua.common.support.core.annotation.Spi;
import com.chua.starter.common.support.configuration.SpringBeanUtils;
import com.chua.starter.entity.service.SysRequestTraceRaw;
import com.chua.starter.service.configuration.trace.RequestTraceEvent;
import com.chua.starter.service.configuration.trace.RequestTraceRawRecordStorage;
import com.chua.starter.service.configuration.trace.RequestTraceStrategyConfig;
import com.chua.starter.service.configuration.trace.RequestTraceSupport;
import com.chua.starter.service.mapper.SysRequestTraceRawMapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 数据库版请求追踪原始记录存储。
 *
 * @author CH
 * @since 2026/03/25
 */
@Spi("database")
public class DatabaseRequestTraceRawRecordStorage implements RequestTraceRawRecordStorage {

    @Override
    public String type() {
        return "database";
    }

    @Override
    public void store(RequestTraceEvent event, RequestTraceStrategyConfig config) {
        if (event == null) {
            return;
        }
        SysRequestTraceRawMapper mapper = mapper();
        if (mapper == null) {
            return;
        }
        SysRequestTraceRaw entity = new SysRequestTraceRaw();
        entity.setSysRequestTraceTime(toDateTime(event.timestamp()));
        entity.setSysRequestTraceTraceId(event.traceId());
        entity.setSysRequestTraceStrategyCode(event.strategyCode());
        entity.setSysRequestTraceMethod(event.method());
        entity.setSysRequestTracePath(event.path());
        entity.setSysRequestTraceRoute(event.routePattern());
        entity.setSysRequestTraceQuery(event.query());
        entity.setSysRequestTraceUrl(event.requestUrl());
        entity.setSysRequestTraceStatus(event.status());
        entity.setSysRequestTraceDurationMs(event.durationMs());
        entity.setSysRequestTraceClientIp(event.clientIp());
        entity.setSysRequestTraceBrowser(event.browser());
        entity.setSysRequestTraceUserAgent(event.userAgent());
        entity.setSysRequestTraceAppName(event.appName());
        entity.setSysRequestTraceUserId(event.userId());
        entity.setSysRequestTraceUsername(event.username());
        entity.setSysRequestTraceFingerprint(event.fingerprint());
        entity.setSysRequestTraceTokenHash(event.tokenHash());
        entity.setSysRequestTraceSignals(RequestTraceSupport.signalCodes(event.signals()));
        mapper.insert(entity);
    }

    private SysRequestTraceRawMapper mapper() {
        try {
            return SpringBeanUtils.getBean(SysRequestTraceRawMapper.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    private LocalDateTime toDateTime(long epochMilli) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault());
    }
}
