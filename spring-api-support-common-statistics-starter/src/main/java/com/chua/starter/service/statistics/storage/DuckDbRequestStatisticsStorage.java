package com.chua.starter.service.statistics.storage;

import com.chua.common.support.core.annotation.Spi;
import com.chua.starter.service.statistics.RequestStatisticsRecord;
import com.chua.starter.service.statistics.RequestStatisticsStorage;
import com.chua.starter.service.statistics.RequestStatisticsStorageContext;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Spi("duckdb")
public class DuckDbRequestStatisticsStorage implements RequestStatisticsStorage {

    @Override
    public String type() {
        return "duckdb";
    }

    @Override
    public void storeBatch(List<RequestStatisticsRecord> records, RequestStatisticsStorageContext context) {
        if (records == null || records.isEmpty()) {
            return;
        }
        log.warn("duckdb storage is not implemented yet, batch size={}", records.size());
    }
}
