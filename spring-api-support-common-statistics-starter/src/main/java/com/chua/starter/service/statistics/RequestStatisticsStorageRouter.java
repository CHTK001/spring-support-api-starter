package com.chua.starter.service.statistics;

import com.chua.common.support.core.spi.ServiceProvider;
import com.chua.starter.service.statistics.storage.MemoryRequestStatisticsStorage;
import org.springframework.stereotype.Component;

@Component
public class RequestStatisticsStorageRouter {

    private static final String DATABASE_STORAGE = "com.chua.starter.service.statistics.storage.DatabaseRequestStatisticsStorage";
    private static final String DUCKDB_STORAGE = "com.chua.starter.service.statistics.storage.DuckDbRequestStatisticsStorage";

    public RequestStatisticsStorage storage(String type) {
        RequestStatisticsStorage storage = ServiceProvider.of(RequestStatisticsStorage.class)
                .getNewExtension(firstNonBlank(type, "database"));
        if (storage != null) {
            return storage;
        }
        if ("database".equalsIgnoreCase(type)) {
            storage = instantiate(DATABASE_STORAGE, RequestStatisticsStorage.class);
            if (storage != null) {
                return storage;
            }
        }
        if ("duckdb".equalsIgnoreCase(type)) {
            storage = instantiate(DUCKDB_STORAGE, RequestStatisticsStorage.class);
            if (storage != null) {
                return storage;
            }
        }
        storage = ServiceProvider.of(RequestStatisticsStorage.class).getNewExtension("memory");
        return storage != null ? storage : new MemoryRequestStatisticsStorage();
    }

    private <T> T instantiate(String className, Class<T> type) {
        try {
            Class<?> target = Class.forName(className);
            Object instance = target.getDeclaredConstructor().newInstance();
            if (type.isInstance(instance)) {
                return type.cast(instance);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }
}
