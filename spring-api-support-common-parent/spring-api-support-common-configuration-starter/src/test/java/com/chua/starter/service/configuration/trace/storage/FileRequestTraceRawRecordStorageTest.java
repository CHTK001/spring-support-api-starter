package com.chua.starter.service.configuration.trace.storage;

import com.chua.starter.service.configuration.trace.RequestTraceEvent;
import com.chua.starter.service.configuration.trace.RequestTraceStrategyConfig;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/** @author CH */
class FileRequestTraceRawRecordStorageTest {

    @Test
    void storeWritesExpectedFieldsEvenWhenUserIdIsMissing() throws Exception {
        Path directory = Files.createTempDirectory("request-trace-raw-");
        RequestTraceStrategyConfig config = new RequestTraceStrategyConfig();
        config.setFileStoragePath(directory.toString());
        config.normalize();

        long timestamp = System.currentTimeMillis();
        RequestTraceEvent event = new RequestTraceEvent(
                timestamp,
                "trace-file-1",
                "GET",
                "/api/public/resource",
                "/api/public/resource",
                "page=1",
                200,
                87L,
                "10.20.30.40",
                "Mozilla/5.0",
                "Chrome",
                "monitor",
                null,
                null,
                "fp-public",
                null,
                "demo:public",
                Set.of()
        );

        FileRequestTraceRawRecordStorage storage = new FileRequestTraceRawRecordStorage();
        storage.store(event, config);

        Path file = directory.resolve("request-trace-" + LocalDate.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()) + ".log");
        assertThat(file).exists();
        String content = Files.readString(file, StandardCharsets.UTF_8);
        assertThat(content).contains("\"traceId\":\"trace-file-1\"");
        assertThat(content).contains("\"clientIp\":\"10.20.30.40\"");
        assertThat(content).contains("\"browser\":\"Chrome\"");
        assertThat(content).contains("\"fingerprint\":\"fp-public\"");
        assertThat(content).contains("\"durationMs\":87");
        assertThat(content).contains("\"requestUrl\":\"/api/public/resource?page=1\"");
    }
}


