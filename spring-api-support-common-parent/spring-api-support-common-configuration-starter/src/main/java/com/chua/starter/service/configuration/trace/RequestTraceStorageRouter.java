package com.chua.starter.service.configuration.trace;

import com.chua.common.support.spi.ServiceProvider;
import com.chua.starter.service.configuration.trace.storage.FileRequestTraceRawRecordStorage;
import com.chua.starter.service.configuration.trace.storage.InMemoryRequestTraceConfigStorage;
import com.chua.starter.service.configuration.trace.storage.MemoryRequestTraceBehaviorStorage;
import org.springframework.stereotype.Component;

/**
 * 请求追踪 SPI 路由器。
 *
 * @since 2026/03/25
 * @author CH
 */
@Component
public class RequestTraceStorageRouter {

    /** Database_config_storage */
    private static final String DATABASE_CONFIG_STORAGE =
            "com.chua.starter.service.trace.storage.DatabaseRequestTraceConfigStorage";
    /** Database_raw_storage */
    private static final String DATABASE_RAW_STORAGE =
            "com.chua.starter.service.trace.storage.DatabaseRequestTraceRawRecordStorage";
    /** Database_behavior_storage */
    private static final String DATABASE_BEHAVIOR_STORAGE =
            "com.chua.starter.service.trace.storage.DatabaseRequestTraceBehaviorStorage";

    public RequestTraceConfigStorage configStorage(String type) {
        RequestTraceConfigStorage storage = ServiceProvider.of(RequestTraceConfigStorage.class)
                .getNewExtension(RequestTraceSupport.firstNonBlank(type, "database"));
        if (storage != null) {
            return storage;
        }
        storage = instantiate(DATABASE_CONFIG_STORAGE, RequestTraceConfigStorage.class);
        if (storage != null) {
            return storage;
        }
        storage = ServiceProvider.of(RequestTraceConfigStorage.class).getNewExtension("database");
        if (storage != null) {
            return storage;
        }
        storage = ServiceProvider.of(RequestTraceConfigStorage.class).getNewExtension("redis");
        if (storage != null) {
            return storage;
        }
        storage = ServiceProvider.of(RequestTraceConfigStorage.class).getNewExtension("memory");
        return storage != null ? storage : new InMemoryRequestTraceConfigStorage();
    }

    public RequestTraceRawRecordStorage rawStorage(String type) {
        RequestTraceRawRecordStorage storage = ServiceProvider.of(RequestTraceRawRecordStorage.class)
                .getNewExtension(RequestTraceSupport.firstNonBlank(type, "file"));
        if (storage != null) {
            return storage;
        }
        if ("database".equalsIgnoreCase(type)) {
            storage = instantiate(DATABASE_RAW_STORAGE, RequestTraceRawRecordStorage.class);
            if (storage != null) {
                return storage;
            }
        }
        storage = ServiceProvider.of(RequestTraceRawRecordStorage.class).getNewExtension("file");
        return storage != null ? storage : new FileRequestTraceRawRecordStorage();
    }

    public RequestTraceBehaviorStorage behaviorStorage(String type) {
        RequestTraceBehaviorStorage storage = ServiceProvider.of(RequestTraceBehaviorStorage.class)
                .getNewExtension(RequestTraceSupport.firstNonBlank(type, "database"));
        if (storage != null) {
            return storage;
        }
        storage = instantiate(DATABASE_BEHAVIOR_STORAGE, RequestTraceBehaviorStorage.class);
        if (storage != null) {
            return storage;
        }
        storage = ServiceProvider.of(RequestTraceBehaviorStorage.class).getNewExtension("database");
        if (storage != null) {
            return storage;
        }
        storage = ServiceProvider.of(RequestTraceBehaviorStorage.class).getNewExtension("memory");
        return storage != null ? storage : new MemoryRequestTraceBehaviorStorage();
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
}
