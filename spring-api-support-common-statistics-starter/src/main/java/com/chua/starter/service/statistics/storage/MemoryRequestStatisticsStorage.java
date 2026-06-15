package com.chua.starter.service.statistics.storage;

import com.chua.common.support.core.annotation.Spi;
import com.chua.starter.service.statistics.RequestStatisticsRecord;
import com.chua.starter.service.statistics.RequestStatisticsStorage;
import com.chua.starter.service.statistics.RequestStatisticsStorageContext;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Spi("memory")
public class MemoryRequestStatisticsStorage implements RequestStatisticsStorage {

    @Override
    public String type() {
        return "memory";
    }

    @Override
    public void storeBatch(List<RequestStatisticsRecord> records, RequestStatisticsStorageContext context) {
        if (records == null || records.isEmpty()) {
            return;
        }
        log.debug("store request statistics batch size={}, type={}", records.size(), context == null ? "unknown" : context.storageType());
    }
}
