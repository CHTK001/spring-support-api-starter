package com.chua.starter.service.configuration.trace.storage;

import com.chua.common.support.spi.annotations.Spi;
import com.chua.common.support.spi.annotations.SpiDefault;
import com.chua.common.support.lang.json.Json;
import com.chua.starter.service.configuration.trace.RequestTraceEvent;
import com.chua.starter.service.configuration.trace.RequestTraceRawRecordStorage;
import com.chua.starter.service.configuration.trace.RequestTraceStrategyConfig;
import com.chua.starter.service.configuration.trace.RequestTraceSupport;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 文件版原始记录存储。
 *
 * @since 2026/03/25
 * @author CH
 */
@Spi("file")
@SpiDefault
@Slf4j
public class FileRequestTraceRawRecordStorage implements RequestTraceRawRecordStorage {

    @Override
    /** 类型 */
    public String type() {
        return "file";
    }

    @Override
    /** 存储 */
    public void store(RequestTraceEvent event, RequestTraceStrategyConfig config) {
        if (event == null || config == null) {
            return;
        }
        Path target = null;
        try {
            Path directory = Path.of(config.getFileStoragePath()).toAbsolutePath();
            Files.createDirectories(directory);
            LocalDate day = Instant.ofEpochMilli(event.timestamp())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            target = directory.resolve("request-trace-" + day + ".log");
            Files.writeString(
                    target,
                    Json.toJson(toMap(event)) + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException ex) {
            log.warn("写入请求追踪原始记录失败: target={}", target != null ? target : config.getFileStoragePath(), ex);
        }
    }

    /**
    * 转为映射
    *
    * @param event 事件
    * @return 转为映射的结果
     */
    private Map<String, Object> toMap(RequestTraceEvent event) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("timestamp", event.timestamp());
        values.put("traceId", event.traceId());
        values.put("strategyCode", event.strategyCode());
        values.put("method", event.method());
        values.put("path", event.path());
        values.put("routePattern", event.routePattern());
        values.put("query", event.query());
        values.put("status", event.status());
        values.put("durationMs", event.durationMs());
        values.put("clientIp", event.clientIp());
        values.put("userAgent", event.userAgent());
        values.put("browser", event.browser());
        values.put("appName", event.appName());
        values.put("userId", event.userId());
        values.put("username", event.username());
        values.put("fingerprint", event.fingerprint());
        values.put("tokenHash", event.tokenHash());
        values.put("requestUrl", event.requestUrl());
        values.put("signals", RequestTraceSupport.signalCodes(event.signals()));
        return values;
    }
}
