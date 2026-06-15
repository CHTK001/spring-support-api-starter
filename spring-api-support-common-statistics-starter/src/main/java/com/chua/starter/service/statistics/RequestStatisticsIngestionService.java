package com.chua.starter.service.statistics;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestStatisticsIngestionService implements DisposableBean {

    private static final int DEFAULT_BUFFER_SIZE = 100;
    private static final Duration DEFAULT_FLUSH_INTERVAL = Duration.ofSeconds(2);

    private final RequestStatisticsStorageRouter storageRouter;
    private final Sinks.Many<RequestStatisticsRecord> sink = Sinks.many().multicast().onBackpressureBuffer();
    private final AtomicLong acceptedCount = new AtomicLong();
    private final AtomicLong rejectedCount = new AtomicLong();
    private final Scheduler worker = Schedulers.newSingle("request-statistics-worker", true);

    private volatile boolean running;
    private volatile Disposable subscription;

    @PostConstruct
    public void start() {
        if (running) {
            return;
        }
        running = true;
        subscription = sink.asFlux()
                .bufferTimeout(DEFAULT_BUFFER_SIZE, DEFAULT_FLUSH_INTERVAL)
                .filter(batch -> !batch.isEmpty())
                .publishOn(worker)
                .subscribe(this::flushBatch, error -> log.error("request statistics pipeline failed", error));
    }

    public void enqueue(RequestStatisticsRecord record) {
        if (record == null) {
            return;
        }
        Sinks.EmitResult result = sink.tryEmitNext(record);
        if (result.isSuccess()) {
            acceptedCount.incrementAndGet();
            return;
        }
        rejectedCount.incrementAndGet();
        log.warn("drop request statistics record: result={}, traceId={}", result, record.traceId());
    }

    private void flushBatch(List<RequestStatisticsRecord> batch) {
        if (batch == null || batch.isEmpty()) {
            return;
        }
        RequestStatisticsStorage storage = storageRouter.storage("database");
        try {
            storage.storeBatch(batch, new RequestStatisticsStorageContext("database", Map.of()));
        } catch (Exception ex) {
            log.error("flush request statistics batch failed, size={}", batch.size(), ex);
        }
    }

    public long acceptedCount() {
        return acceptedCount.get();
    }

    public long rejectedCount() {
        return rejectedCount.get();
    }

    @Override
    public void destroy() {
        if (subscription != null) {
            subscription.dispose();
        }
        worker.dispose();
    }
}
