package com.chua.starter.service.statistics.storage;

import com.chua.common.support.core.annotation.Spi;
import com.chua.common.support.core.annotation.SpiDefault;
import com.chua.starter.common.support.configuration.SpringBeanUtils;
import com.chua.starter.entity.service.SysRequestStatistics;
import com.chua.starter.service.mapper.SysRequestStatisticsMapper;
import com.chua.starter.service.statistics.RequestStatisticsRecord;
import com.chua.starter.service.statistics.RequestStatisticsStorage;
import com.chua.starter.service.statistics.RequestStatisticsStorageContext;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Spi("database")
@SpiDefault
public class DatabaseRequestStatisticsStorage implements RequestStatisticsStorage {

    @Override
    public String type() {
        return "database";
    }

    @Override
    public void storeBatch(List<RequestStatisticsRecord> records, RequestStatisticsStorageContext context) {
        if (records == null || records.isEmpty()) {
            return;
        }
        SysRequestStatisticsMapper mapper = mapper();
        if (mapper == null) {
            return;
        }
        List<SysRequestStatistics> entities = new ArrayList<>(records.size());
        for (RequestStatisticsRecord record : records) {
            if (record == null) {
                continue;
            }
            entities.add(toEntity(record));
        }
        if (entities.isEmpty()) {
            return;
        }
        for (SysRequestStatistics entity : entities) {
            mapper.insert(entity);
        }
    }

    private SysRequestStatisticsMapper mapper() {
        try {
            return SpringBeanUtils.getBean(SysRequestStatisticsMapper.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    private SysRequestStatistics toEntity(RequestStatisticsRecord record) {
        SysRequestStatistics entity = new SysRequestStatistics();
        entity.setSysRequestStatisticsTime(toDateTime(record.recordedAt()));
        entity.setSysRequestStatisticsTraceId(record.traceId());
        entity.setSysRequestStatisticsRoute(record.routeKey());
        entity.setSysRequestStatisticsMethod(record.method());
        entity.setSysRequestStatisticsPath(record.path());
        entity.setSysRequestStatisticsStatus(record.status());
        entity.setSysRequestStatisticsDurationMs(record.durationMs());
        entity.setSysRequestStatisticsClientIp(record.clientIp());
        entity.setSysRequestStatisticsUserId(record.userId());
        entity.setSysRequestStatisticsUsername(record.username());
        entity.setSysRequestStatisticsFingerprint(record.fingerprint());
        entity.setSysRequestStatisticsTokenHash(record.tokenHash());
        entity.setSysRequestStatisticsBrowser(record.browser());
        entity.setSysRequestStatisticsAppName(record.appName());
        entity.setSysRequestStatisticsRequestUrl(record.requestUrl());
        entity.setSysRequestStatisticsRequestQuery(record.requestQuery());
        entity.setSysRequestStatisticsRequestBodyHash(record.requestBodyHash());
        entity.setSysRequestStatisticsResponseBodyHash(record.responseBodyHash());
        entity.setSysRequestStatisticsInputTokens(record.inputTokens());
        entity.setSysRequestStatisticsOutputTokens(record.outputTokens());
        entity.setSysRequestStatisticsCachedTokens(record.cachedTokens());
        entity.setSysRequestStatisticsTotalTokens(record.totalTokens());
        entity.setSysRequestStatisticsPromptCharacters(record.promptCharacters());
        entity.setSysRequestStatisticsCompletionCharacters(record.completionCharacters());
        entity.setSysRequestStatisticsCacheHits(record.cacheHits());
        entity.setSysRequestStatisticsCacheMisses(record.cacheMisses());
        entity.setSysRequestStatisticsCostCents(record.costCents());
        entity.setSysRequestStatisticsCostCurrency(record.costCurrency());
        entity.setSysRequestStatisticsModel(record.model());
        entity.setSysRequestStatisticsProvider(record.provider());
        entity.setSysRequestStatisticsChannel(record.channel());
        entity.setSysRequestStatisticsStatusText(record.statusText());
        entity.setSysRequestStatisticsExtra(String.valueOf(record.extra()));
        return entity;
    }

    private LocalDateTime toDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    private LocalDateTime toDateTime(long epochMilli) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault());
    }
}
