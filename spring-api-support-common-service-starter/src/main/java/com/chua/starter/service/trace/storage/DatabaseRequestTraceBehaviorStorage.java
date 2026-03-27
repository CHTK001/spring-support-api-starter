package com.chua.starter.service.trace.storage;

import com.chua.common.support.core.annotation.Spi;
import com.chua.common.support.core.annotation.SpiDefault;
import com.chua.starter.common.support.configuration.SpringBeanUtils;
import com.chua.starter.entity.service.SysRequestTraceBehavior;
import com.chua.starter.service.configuration.trace.RequestTraceBehaviorRecord;
import com.chua.starter.service.configuration.trace.RequestTraceBehaviorStorage;
import com.chua.starter.service.configuration.trace.RequestTraceStrategyConfig;
import com.chua.starter.service.configuration.trace.RequestTraceSupport;
import com.chua.starter.service.mapper.SysRequestTraceBehaviorMapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 数据库版请求追踪行为结果存储。
 *
 * @author CH
 * @since 2026/03/25
 */
@Spi("database")
@SpiDefault
public class DatabaseRequestTraceBehaviorStorage implements RequestTraceBehaviorStorage {

    @Override
    public String type() {
        return "database";
    }

    @Override
    public void store(RequestTraceBehaviorRecord record, RequestTraceStrategyConfig config) {
        if (record == null) {
            return;
        }
        SysRequestTraceBehaviorMapper mapper = mapper();
        if (mapper == null) {
            return;
        }
        SysRequestTraceBehavior entity = new SysRequestTraceBehavior();
        entity.setSysRequestTraceRequestTime(toDateTime(record.requestAt()));
        entity.setSysRequestTraceAnalyzedAt(toDateTime(record.analyzedAt()));
        entity.setSysRequestTraceTraceId(record.traceId());
        entity.setSysRequestTraceStrategyCode(record.strategyCode());
        entity.setSysRequestTraceRoute(record.route());
        entity.setSysRequestTraceUrl(record.requestUrl());
        entity.setSysRequestTraceStatus(record.status());
        entity.setSysRequestTraceDurationMs(record.durationMs());
        entity.setSysRequestTraceClientIp(record.clientIp());
        entity.setSysRequestTraceBrowser(record.browser());
        entity.setSysRequestTraceUserId(record.userId());
        entity.setSysRequestTraceUsername(record.username());
        entity.setSysRequestTraceFingerprint(record.fingerprint());
        entity.setSysRequestTraceUserAgent(record.userAgent());
        entity.setSysRequestTraceSignals(RequestTraceSupport.signalCodes(record.signals()));
        entity.setSysRequestTraceRelatedRequestCount(record.relatedRequestCount());
        entity.setSysRequestTraceDistinctPathCount(record.distinctPathCount());
        entity.setSysRequestTraceRiskLevel(record.riskLevel());
        entity.setSysRequestTraceSummary(record.summary());
        entity.setSysRequestTraceAiSummary(record.aiSummary());
        entity.setSysRequestTraceExtra(record.extra());
        mapper.insert(entity);
    }

    private SysRequestTraceBehaviorMapper mapper() {
        try {
            return SpringBeanUtils.getBean(SysRequestTraceBehaviorMapper.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    private LocalDateTime toDateTime(long epochMilli) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault());
    }
}
